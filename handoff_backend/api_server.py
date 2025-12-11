"""Simple HTTP API wrapper to run people counting on a stream.

Endpoints:
- GET /health -> {"status": "ok"}
- POST /count -> run counting on given URL and return JSON summary.

This wraps the existing count_rtmp.run with ffmpeg pulling. It limits frames via max_frames to keep
request time bounded. Designed for short/diagnostic runs; for long-running services you may want
job queues/background workers.

Run (development):
  uvicorn api_server:app --host 0.0.0.0 --port 8000

Dependencies: fastapi, uvicorn, ultralytics, opencv-python, ffmpeg in PATH.
"""
from fastapi import FastAPI, HTTPException
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field
from typing import List, Optional
from pathlib import Path
import tempfile
import json
import os
import time

from pydantic_core.core_schema import none_schema

from count_rtmp import run as count_run

app = FastAPI(title="People Count API", version="0.1.0")

# 确保samples目录存在
samples_dir = Path(__file__).resolve().parent / "samples"
samples_dir.mkdir(exist_ok=True)

# 提供样本图片的静态文件服务
app.mount("/samples", StaticFiles(directory=samples_dir), name="samples")


class CountRequest(BaseModel):
    source: str = Field(..., description="RTMP/HTTP-FLV URL")
    weights: str = Field(..., description="Path to YOLO weights (.pt/.pth)")
    imgsz: int = Field(1280, description="Inference image size")
    conf: float = Field(0.2, description="Confidence threshold")
    max_frames: int = Field(20, description="Max frames to process (0=unlimited, not recommended for API)")
    person_only: bool = Field(True, description="If true, restrict to person class (id=0)")
    classes: Optional[List[int]] = Field(None, description="Override classes list, e.g. [0,1]; overrides person_only")
    use_ffmpeg: bool = Field(True, description="Use ffmpeg pulling; recommended for RTMP/HTTP-FLV")
    save_samples: int = Field(1, description="How many sample images to save (0 to skip)")  # 默认改为1
    device_id: str = Field(..., description="Device ID for multi-GPU")


@app.get("/health")
def health():
    return {"status": "ok", "time": time.time()}


@app.post("/count")
def count(req: CountRequest):
    # Resolve paths relative to repo
    repo_root = Path(__file__).resolve().parent
    weights_path = Path(req.weights)
    if not weights_path.is_absolute():
        weights_path = (repo_root / weights_path).resolve()

    if not weights_path.exists():
        raise HTTPException(status_code=400, detail=f"weights not found: {weights_path}")

    # Set class filter
    classes = None
    if req.person_only:
        classes = 0
    if req.classes:
        classes = req.classes if len(req.classes) > 1 else req.classes[0]

    setattr(count_run, "classes", classes)

    with tempfile.TemporaryDirectory() as td:
        out_json = Path(td) / "summary.json"
        out_csv = None  # not returned in API; can be enabled if needed
        try:
            count_run(
                str(weights_path),
                req.source,
                imgsz=req.imgsz,
                conf=req.conf,
                show=False,
                out_csv=out_csv,
                save_samples=req.save_samples,
                samples_dir=str("samples/"+req.device_id),
                out_json=str(out_json),
                json_interval=60,
                use_ffmpeg=req.use_ffmpeg,
                max_frames=req.max_frames,
            )
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"inference failed: {e}")

        if not out_json.exists():
            raise HTTPException(status_code=500, detail="inference finished but summary.json not found")

        try:
            data = json.loads(out_json.read_text(encoding="utf-8"))
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"failed to parse summary: {e}")

        # 处理样本图片，将其重命名为 sample_<device_id>.jpg 并添加到返回数据中
        sample_url = None
        samples_dir = Path("samples") / req.device_id
        if req.device_id :
            sample_files = list(samples_dir.glob("*.jpg"))
            if sample_files:
                # 确保目标目录存在
                device_sample_dir = samples_dir
                device_sample_dir.mkdir(exist_ok=True)
                
                # 选择第一张图片作为样本
                sample_file = sample_files[0]
                # 创建新的文件名
                new_filename = f"sample_{req.device_id}.jpg"
                new_filepath = device_sample_dir / new_filename
                
                # 复制文件到目标位置
                new_filepath.write_bytes(sample_file.read_bytes())

                # 删除原始文件
                sample_file.unlink()
                # 添加URL到返回结果中
                sample_url = f"/samples/{req.device_id}/{new_filename}"
                
        # 将样本图片URL添加到返回数据中
        if sample_url:
            data["sample_url"] = sample_url

    return data


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("api_server:app", host="0.0.0.0", port=8000, reload=False)
