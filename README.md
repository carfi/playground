# HTTPS Header-Inspection Proxy for VSCode API calls

This repository contains a **man-in-the-middle HTTPS proxy** setup using `mitmproxy` so you can inspect incoming custom headers and generate usage statistics.

## 1) Install

```bash
python -m venv .venv
source .venv/bin/activate
pip install mitmproxy
```

## 2) Start the proxy

```bash
mitmproxy -s proxy/header_stats_addon.py \
  --listen-host 127.0.0.1 \
  --listen-port 8080 \
  --set header_stats_file=proxy/header-stats.json \
  --set target_header=x-my-custom-header,x-client-version
```

- `target_header` is optional. If omitted, all request headers are tracked.
- Stats are written continuously to `proxy/header-stats.json`.

## 3) Configure VSCode / HTTP client to use the proxy

Set your client proxy to:

- HTTP proxy: `http://127.0.0.1:8080`
- HTTPS proxy: `http://127.0.0.1:8080`

For tools that verify TLS, install and trust mitmproxy's local CA certificate:

```bash
mitmproxy
# then open http://mitm.it in the client environment and install certificate
```

## 4) Example output

`proxy/header-stats.json` will contain:

- `total_requests`
- `requests_by_host`
- `headers_present`
- `header_values` (top 20 values per tracked header)

## Security notes

- Only run this in environments you own and control.
- MITM proxies can capture secrets; avoid using production credentials.
- Restrict binding to localhost unless you explicitly need remote access.
