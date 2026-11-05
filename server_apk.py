import http.server
import os
import sys
import socket
import qrcode
import io

PORT = 8080
APK_NAME = "al-muhasib-debug.apk"
APK_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), APK_NAME)

def get_local_ip():
    try:
        for info in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET):
            ip = info[4][0]
            if ip.startswith("192.168."):
                return ip
    except Exception:
        pass
    return "192.168.1.11"

LOCAL_IP = get_local_ip()
DOWNLOAD_URL = f"http://{LOCAL_IP}:{PORT}/{APK_NAME}"

HTML_PAGE = f"""<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تطبيق المُحاسِب - تحميل APK</title>
    <style>
        body {{
            background: #060e0a;
            color: #ecfdf5;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            padding: 20px;
            box-sizing: border-box;
            text-align: center;
        }}
        .card {{
            background: rgba(16, 185, 129, 0.08);
            border: 1px solid rgba(52, 211, 153, 0.25);
            border-radius: 24px;
            padding: 32px 24px;
            max-width: 420px;
            width: 100%;
            box-shadow: 0 20px 40px rgba(0,0,0,0.6);
            backdrop-filter: blur(12px);
        }}
        .badge {{
            display: inline-block;
            background: rgba(16, 185, 129, 0.2);
            color: #34d399;
            padding: 6px 14px;
            border-radius: 9999px;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 16px;
            border: 1px solid rgba(52, 211, 153, 0.3);
        }}
        h1 {{
            font-size: 26px;
            margin: 0 0 8px 0;
            color: #ffffff;
            font-weight: 800;
        }}
        p {{
            color: #a7f3d0;
            font-size: 14px;
            line-height: 1.6;
            margin: 0 0 24px 0;
        }}
        .btn {{
            display: block;
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: #ffffff;
            text-decoration: none;
            padding: 16px 28px;
            border-radius: 16px;
            font-size: 17px;
            font-weight: 700;
            box-shadow: 0 8px 20px rgba(16, 185, 129, 0.4);
            transition: all 0.2s ease;
        }}
        .btn:active {{
            transform: scale(0.97);
        }}
        .details {{
            margin-top: 24px;
            padding-top: 16px;
            border-top: 1px solid rgba(255,255,255,0.08);
            font-size: 12px;
            color: #6ee7b7;
        }}
    </style>
</head>
<body>
    <div class="card">
        <div class="badge">🚀 أحدث إصدار v1.0.1 جاهز</div>
        <h1>المُحَاسِب</h1>
        <p>الأذان السنّي • المصحف الشريف • إشعارات AOD الملونة • شاشة القفل الأصلية</p>
        <a href="/{APK_NAME}" class="btn" download>📥 تحميل وتثبيت الآن (APK)</a>
        <div class="details">
            الحجم: ~25.5 ميغابايت • رابط مباشر:<br>
            <span style="font-family: monospace; font-size: 11px;">{DOWNLOAD_URL}</span>
        </div>
    </div>
</body>
</html>
"""

class MultiThreadedApkHandler(http.server.BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def do_HEAD(self):
        self.handle_request(is_head=True)

    def do_GET(self):
        self.handle_request(is_head=False)

    def handle_request(self, is_head=False):
        clean_path = self.path.split("?")[0]

        if clean_path in ("/", "/index.html"):
            payload = HTML_PAGE.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(payload)))
            self.send_header("Connection", "close")
            self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
            self.end_headers()
            if not is_head:
                self.wfile.write(payload)

        elif clean_path in (f"/{APK_NAME}", "/al-muhasib-debug.apk", "/al-muhasib-release.apk", "/al-muhasib-app.apk", "/download", "/apk"):
            target_path = APK_PATH
            file_name = clean_path.lstrip("/")
            if not file_name.endswith(".apk"):
                file_name = APK_NAME
            if os.path.exists(target_path):
                file_size = os.path.getsize(target_path)
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", f'attachment; filename="{file_name}"')
                self.send_header("Content-Length", str(file_size))
                self.send_header("Connection", "close")
                self.send_header("Cache-Control", "no-cache")
                self.end_headers()
                if not is_head:
                    with open(APK_PATH, "rb") as f:
                        while chunk := f.read(262144): # 256KB chunks
                            try:
                                self.wfile.write(chunk)
                            except (BrokenPipeError, ConnectionResetError):
                                break
            else:
                self.send_error(404, "APK not found")

        elif clean_path == "/favicon.ico":
            self.send_response(204)
            self.send_header("Content-Length", "0")
            self.send_header("Connection", "close")
            self.end_headers()

        else:
            self.send_error(404, "Not Found")

    def log_message(self, format, *args):
        sys.stderr.write(f"[{self.client_address[0]}] {format % args}\n")
        sys.stderr.flush()

class ThreadedServer(http.server.ThreadingHTTPServer):
    daemon_threads = True
    allow_reuse_address = True

if __name__ == "__main__":
    print("=" * 60)
    print(f" AL-MUHASIB HIGH-SPEED MULTI-THREADED SERVER ")
    print(f" Local IP: {LOCAL_IP}")
    print(f" Direct Download URL: {DOWNLOAD_URL}")
    print("=" * 60)
    sys.stdout.flush()

    server_address = ("0.0.0.0", PORT)
    with ThreadedServer(server_address, MultiThreadedApkHandler) as httpd:
        print(f"Serving on 0.0.0.0:{PORT} (threads enabled)...")
        sys.stdout.flush()
        httpd.serve_forever()
