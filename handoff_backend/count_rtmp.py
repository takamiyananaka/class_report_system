"""
examples/count_rtmp.py

简单示例：从 RTMP 拉流，使用 Ultralytics 模型逐帧检测并在窗口显示计数。
依赖：ultralytics, opencv-python

用法示例 (PowerShell):
python ./examples/count_rtmp.py --weights runs/train/classroom_exp/weights/best.pt --source "rtmp://ddxk.swpu.edu.cn:1935/live/studentPanlow_102812181"

支持候选 URL 自动探测：
--candidates "rtmp://a,rtmp://b" 或 --candidates-file candidates.txt
"""
import argparse
import time
import os
import csv
import json
from pathlib import Path
from datetime import datetime
import cv2
from ultralytics import YOLO
import subprocess
import numpy as np
import shlex
import sys
import tempfile
import traceback
import typing as t
import urllib.request as _urllib
from http.client import HTTPResponse


def probe_stream_ffmpeg(url: str, timeout: int = 8, probe_duration: int = 3) -> bool:
    """用本地 ffmpeg 快速探测流是否可用。
    尝试打开流并拉取短时间的数据，超时或返回非 0 视为不可用。
    返回 True 表示探测到可用流。
    """
    cmd = [
        'ffmpeg', '-hide_banner', '-loglevel', 'error', '-i', url,
        '-t', str(probe_duration), '-f', 'null', '-'
    ]
    try:
        # 使用 subprocess.run 并设定超时，以避免长时间阻塞
        res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout)
        if res.returncode == 0:
            return True
        # 若返回非 0，可进一步通过 stderr 判断是否已拉到帧（某些服务器返回非 0 但有帧）
        stderr = (res.stderr or b'').decode('utf-8', errors='ignore')
        if 'frame=' in stderr or 'frames=' in stderr:
            return True
        return False
    except subprocess.TimeoutExpired:
        return False
    except FileNotFoundError:
        print('ffmpeg 未找到，请先安装 ffmpeg 并确保其在 PATH 中。')
        return False


def probe_stream_deep(url: str, timeout: int = 10, probe_duration: int = 4) -> t.Tuple[bool, str]:
    """对 HTTP-FLV/RTMP 等流做深度探测：
    1) 对 HTTP(S) 链接先发 HEAD 请求检查状态码（快速失败）。
    2) 使用 ffmpeg 带 HTTP-friendly 参数尝试拉取短时数据，并返回完整 stderr 以供诊断。

    返回 (ok, stderr_text)。
    """
    stderr_text = ''
    # 如果是 HTTP/HTTPS，先发 HEAD 请求快速检查
    try:
        if url.lower().startswith('http'):
            req = _urllib.Request(url, method='HEAD', headers={'User-Agent': 'curl/7.68.0'})
            try:
                with _urllib.urlopen(req, timeout=timeout) as resp:
                    status = getattr(resp, 'status', None)
                    if status and status >= 400:
                        stderr_text += f'HEAD {url} returned status {status}\n'
            except Exception as e:
                stderr_text += f'HEAD request failed: {e}\n'

        # 使用 ffmpeg 做短拉取
        cmd = [
            'ffmpeg', '-hide_banner', '-loglevel', 'error',
        ]
        # 对 HTTP(S) 使用 non-persistent 选项以避免某些服务器拒绝持久连接
        if url.lower().startswith('http'):
            cmd += ['-http_persistent', '0', '-rw_timeout', str(int(timeout * 1e6)), '-user_agent', 'curl/7.68.0']
        cmd += ['-i', url, '-t', str(probe_duration), '-f', 'null', '-']

        try:
            res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout + probe_duration + 3)
            stderr_text += (res.stderr or b'').decode('utf-8', errors='ignore')
            ok = (res.returncode == 0) or ('frame=' in stderr_text) or ('HTTP/' in stderr_text)
            return ok, stderr_text
        except subprocess.TimeoutExpired:
            return False, stderr_text + '\nffmpeg probe timeout'
        except FileNotFoundError:
            return False, 'ffmpeg not found'
    except Exception as e:
        return False, f'probe_stream_deep exception: {e}'


def run(weights, source, imgsz=640, conf=0.25, show=True, out_csv=None, save_samples=0, samples_dir='samples', out_json=None, json_interval=60, use_ffmpeg=False, max_frames: int = 0):
    # 如果传入的是本地文件路径但文件不存在，提前提示并退出，避免 ultralytics 自动下载失败引发长时间重试/异常
    if not os.path.exists(weights):
        print('\n权重文件未在本地找到:', weights)
        print('请手动下载并放到该路径，或传入正确的本地权重路径')
        print('示例预训练权重下载地址(浏览器中打开并保存):')
        print('https://github.com/ultralytics/assets/releases/download/v8.3.0/' + os.path.basename(weights))
        print('\n或者使用 pip 配置镜像后重新运行以允许自动下载（见 README 或帮助）\n')
        return

    model = YOLO(weights)

    # 准备 CSV 输出
    csv_file = None
    csv_writer = None
    if out_csv:
        Path(out_csv).parent.mkdir(parents=True, exist_ok=True)
        csv_file = open(out_csv, 'w', newline='', encoding='utf-8')
        csv_writer = csv.writer(csv_file)
        csv_writer.writerow(['timestamp', 'frame_index', 'count'])

    # 准备样本保存目录
    samples_saved = 0
    samples_dir = Path(samples_dir)
    if save_samples and not samples_dir.exists():
        samples_dir.mkdir(parents=True, exist_ok=True)

    # model.predict(..., stream=True) yields per-frame results iterator
    start = time.time()
    frame_count = 0
    counts = []  # list of (timestamp, count)
    last_json_dump = time.time()

    def dump_json(path, counts_list, fps, frame_count):
        if not path:
            return
        total = sum(c for _, c in counts_list) if counts_list else 0
        frames = len(counts_list)
        avg = total / frames if frames > 0 else 0.0
        mx = max((c for _, c in counts_list), default=0)
        mn = min((c for _, c in counts_list), default=0)
        summary = {
            'timestamp_utc': datetime.utcnow().isoformat() + 'Z',
            'frames': frames,
            'frames_processed': frame_count,
            'fps': fps,
            'total_count_sum': total,
            'average_count': avg,
            'max_count': mx,
            'min_count': mn
        }
        try:
            Path(path).parent.mkdir(parents=True, exist_ok=True)
            with open(path, 'w', encoding='utf-8') as f:
                json.dump({'summary': summary, 'samples': [{'timestamp': t, 'count': c} for t, c in counts_list]}, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print('Failed to write JSON:', e)

    try:
        if use_ffmpeg:
            # 使用 ffmpeg 从 RTMP/HTTP 拉流，输出 bgr24 原始帧，强制缩放到 imgsz x imgsz
            cmd = ['ffmpeg']
            # 对 RTMP 流明确使用 TCP 传输以提高兼容性
            if isinstance(source, str) and source.startswith('rtmp://'):
                cmd += ['-rtmp_transport', 'tcp']
            cmd += ['-i', source, '-loglevel', 'quiet', '-an', '-f', 'rawvideo', '-pix_fmt', 'bgr24', '-vf', f'scale={imgsz}:{imgsz}', '-']
            proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            frame_size = imgsz * imgsz * 3
            while True:
                raw = proc.stdout.read(frame_size)
                if not raw or len(raw) < frame_size:
                    break
                frame_count += 1
                img_np = np.frombuffer(raw, dtype=np.uint8).reshape((imgsz, imgsz, 3))

                # 直接使用模型对 numpy 图像推理
                # 仅检测指定类别（若在外层传入）。
                # 在 main() 中会将 classes 参数设置为 0 来仅检测人类。
                if hasattr(run, 'classes') and run.classes is not None:
                    results = model(img_np, classes=run.classes)
                else:
                    results = model(img_np)
                result = results[0]
                # result.plot() 返回绘制了检测框的 BGR numpy 图像
                img = result.plot()
                # boxes 信息（可能为 None）
                boxes = result.boxes
                count = 0
                if boxes is not None:
                    # 兼容不同 ultralytics 版本的 boxes 接口
                    try:
                        count = len(boxes.xyxy)  # 常见接口
                    except Exception:
                        try:
                            count = len(boxes)
                        except Exception:
                            count = 0

                # 写 CSV
                ts = datetime.utcnow().isoformat() + 'Z'
                if csv_writer:
                    csv_writer.writerow([ts, frame_count, count])

                # 记录计数到内存以便 JSON 汇总
                counts.append((ts, count))

                # 保存样本图像（优先保存有检测的帧）
                if save_samples and samples_saved < save_samples:
                    # 保存条件：优先保存 count>0 的帧，否则保存任意帧直到达到数量
                    should_save = count > 0 or (frame_count <= save_samples)
                    if should_save:
                        fname = samples_dir / f'sample_{frame_count:06d}.jpg'
                        try:
                            cv2.imwrite(str(fname), img)
                            samples_saved += 1
                            print(f'[sample saved] {fname} (count={count})')
                        except Exception as e:
                            print('Failed to save sample image:', e)

                # 在图像上写上计数和FPS
                elapsed = time.time() - start
                fps = frame_count / elapsed if elapsed > 0 else 0.0
                cv2.putText(img, f'Count: {count}', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 0), 2)
                cv2.putText(img, f'FPS: {fps:.1f}', (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2)

                if show:
                    cv2.imshow('RTMP Count', img)
                    key = cv2.waitKey(1)
                    if key == 27:  # ESC 退出
                        return

                if max_frames > 0 and frame_count >= max_frames:
                    break
        else:
            # 如果外部设置了类过滤，传给 predict
            predict_kwargs = {'source': source, 'imgsz': imgsz, 'conf': conf, 'stream': True}
            if hasattr(run, 'classes') and run.classes is not None:
                predict_kwargs['classes'] = run.classes

            for result in model.predict(**predict_kwargs):
                frame_count += 1
                # result.plot() 返回绘制了检测框的 BGR numpy 图像
                img = result.plot()
                if max_frames > 0 and frame_count >= max_frames:
                    break

            # boxes 信息（可能为 None）
            boxes = result.boxes
            count = 0
            if boxes is not None:
                # 兼容不同 ultralytics 版本的 boxes 接口
                try:
                    count = len(boxes.xyxy)  # 常见接口
                except Exception:
                    try:
                        count = len(boxes)
                    except Exception:
                        count = 0

            # 写 CSV
            ts = datetime.utcnow().isoformat() + 'Z'
            if csv_writer:
                csv_writer.writerow([ts, frame_count, count])

            # 记录计数到内存以便 JSON 汇总
            counts.append((ts, count))

            # 保存样本图像（优先保存有检测的帧）
            if save_samples and samples_saved < save_samples:
                # 保存条件：优先保存 count>0 的帧，否则保存任意帧直到达到数量
                should_save = count > 0 or (frame_count <= save_samples)
                if should_save:
                    fname = samples_dir / f'sample_{frame_count:06d}.jpg'
                    try:
                        cv2.imwrite(str(fname), img)
                        samples_saved += 1
                        print(f'[sample saved] {fname} (count={count})')
                    except Exception as e:
                        print('Failed to save sample image:', e)

            # 在图像上写上计数和FPS
            elapsed = time.time() - start
            fps = frame_count / elapsed if elapsed > 0 else 0.0
            cv2.putText(img, f'Count: {count}', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 0), 2)
            cv2.putText(img, f'FPS: {fps:.1f}', (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2)

            if show:
                cv2.imshow('RTMP Count', img)
                key = cv2.waitKey(1)
                if key == 27:  # ESC 退出
                    return

    except Exception as e:
        print('Error during streaming/detection:', e)

    finally:
        # 在退出前写一次最终 JSON
        elapsed = time.time() - start
        fps = frame_count / elapsed if elapsed > 0 else 0.0
        if out_json:
            dump_json(out_json, counts, fps, frame_count)

        if csv_file:
            csv_file.close()
        cv2.destroyAllWindows()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--weights', type=str, required=True, help='模型权重路径 (.pt) 或预训练模型')
    parser.add_argument('--source', type=str, required=False, help='RTMP 地址或本地视频文件或摄像头索引，例如 0')
    parser.add_argument('--candidates', type=str, default=None, help='候选流，逗号分隔，例如 "rtmp://a,rtmp://b"')
    parser.add_argument('--candidates-file', type=str, default=None, help='候选流文件，每行一个 URL（或 JSON 数组）')
    parser.add_argument('--imgsz', type=int, default=640)
    parser.add_argument('--conf', type=float, default=0.25)
    parser.add_argument('--no-show', dest='show', action='store_false', help='不显示窗口（用于无头部署）')
    parser.add_argument('--out-csv', type=str, default=None, help='CSV 输出路径 (timestamp, frame_index, count)')
    parser.add_argument('--save-samples', type=int, default=0, help='保存示例图片数量（0 不保存）')
    parser.add_argument('--samples-dir', type=str, default='samples', help='示例图片保存目录')
    parser.add_argument('--out-json', type=str, default=None, help='JSON 汇总输出路径')
    parser.add_argument('--json-interval', type=int, default=60, help='JSON 周期写入间隔（秒），当前脚本在结束时写入最终汇总）')
    parser.add_argument('--use-ffmpeg', action='store_true', help='使用本地 ffmpeg 拉流（绕过 OpenCV/GStreamer 的 RTMP 插件限制）')
    parser.add_argument('--person-only', action='store_true', help='只检测并展示人（等同于 --classes 0）')
    parser.add_argument('--classes', type=str, default=None, help='限制检测的类别，逗号分隔的类 id 列表，例如 "0" 或 "0,1" （覆盖 --person-only）')
    parser.add_argument('--max-frames', type=int, default=10, help='最多处理的帧数（0 表示不限制）')
    args = parser.parse_args()

    # 组装候选源列表
    candidates_list = []
    if args.candidates:
        candidates_list.extend([c.strip() for c in args.candidates.split(',') if c.strip()])
    if args.candidates_file:
        try:
            p = Path(args.candidates_file)
            txt = p.read_text(encoding='utf-8')
            # 支持 JSON 数组或换行列表
            try:
                parsed = json.loads(txt)
                if isinstance(parsed, list):
                    candidates_list.extend([str(x) for x in parsed])
            except Exception:
                # 按行解析
                for line in txt.splitlines():
                    s = line.strip()
                    if s:
                        candidates_list.append(s)
        except Exception as e:
            print('无法读取 candidates_file:', e)

    # 如果未提供 candidates，但提供了 --source，则使用它
    if not candidates_list and args.source:
        candidates_list = [args.source]

    if not candidates_list:
        parser.error('请提供 --source 或 --candidates/--candidates-file 之一')

    # 依次探测候选 URL，选择第一个可用的
    selected = None
    for url in candidates_list:
        print(f'探测候选流: {url} ...')
        ok = probe_stream_ffmpeg(url) if args.use_ffmpeg else probe_stream_ffmpeg(url)
        if ok:
            selected = url
            print(f'[OK] 选定候选流: {selected}')
            break
        else:
            print(f'[FAIL] 无法连接: {url}')

    if selected is None:
        print('未能探测到可用流，请检查网络/URL，脚本退出。候选列表为:', candidates_list)
        return

    # 解析 classes 参数（支持 --person-only 快捷键）
    classes = None
    if args.person_only:
        classes = 0
    if args.classes:
        try:
            # 支持逗号分隔多个 id
            parts = [int(x.strip()) for x in args.classes.split(',') if x.strip()]
            classes = parts if len(parts) > 1 else parts[0]
        except Exception:
            print('无法解析 --classes 参数，忽略。')

    # 将 classes 绑定到 run 函数对象，便于内部调用处读取
    setattr(run, 'classes', classes)

    run(args.weights, selected, imgsz=args.imgsz, conf=args.conf, show=args.show,
        out_csv=args.out_csv, save_samples=args.save_samples, samples_dir=args.samples_dir,
        out_json=args.out_json, json_interval=args.json_interval, use_ffmpeg=args.use_ffmpeg,
        max_frames=args.max_frames)


if __name__ == '__main__':
    main()
