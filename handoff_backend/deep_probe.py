import sys
from pathlib import Path
# Ensure repository root is on sys.path so `examples` can be imported
repo_root = str(Path(__file__).resolve().parents[1])
if repo_root not in sys.path:
    sys.path.insert(0, repo_root)

from count_rtmp import probe_stream_deep

urls = [
    # HTTP-FLV (HH device)
    'https://ddxk.swpu.edu.cn:80/live/phc0_1152611891214962688.live.flv',
    'https://ddxk.swpu.edu.cn:80/live/phc2_1152611891214962688.live.flv',
    'https://ddxk.swpu.edu.cn:80/live/phc1_1152611891214962688.live.flv',
    # RTMP (PC device)
    'rtmp://ddxk.swpu.edu.cn:1935/live/teacherPanlow_102812181',
    'rtmp://ddxk.swpu.edu.cn:1935/live/teacherPanhig_hdr_102812181',
    'rtmp://ddxk.swpu.edu.cn:1935/live/studentPanlow_102812181',
    'rtmp://ddxk.swpu.edu.cn:1935/live/studentPanhig_102812181',
    'rtmp://ddxk.swpu.edu.cn:1935/live/teacherPchig_102812181'
]

def generate_variants(url: str) -> list:
    s = url.strip()
    variants = []
    variants.append(s)
    try:
        if s.startswith('https://'):
            variants.append(s.replace('https://', 'http://', 1))
            if ':80' in s:
                variants.append(s.replace(':80', '', 1))
                variants.append(s.replace('https://', 'http://', 1).replace(':80', '', 1))
        elif s.startswith('http://'):
            variants.append(s.replace('http://', 'https://', 1))
            if ':80' in s:
                variants.append(s.replace(':80', '', 1))
                variants.append(s.replace('http://', 'https://', 1).replace(':80', '', 1))
        else:
            # not starting with scheme — try both
            variants.append('https://' + s)
            variants.append('http://' + s)
    except Exception:
        pass
    # dedupe while preserving order
    seen = set()
    out = []
    for v in variants:
        if v not in seen:
            seen.add(v)
            out.append(v)
    return out


for u in urls:
    print('\n==== Base URL:', u)
    variants = generate_variants(u)
    for v in variants:
        print('\n---- Variant:', v)
        ok, err = probe_stream_deep(v, timeout=8, probe_duration=4)
        print('OK:', ok)
        print('--- stderr ---')
        print(err)
    print('\n==============')
