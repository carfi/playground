"""mitmproxy addon for tracking custom request-header usage.

Usage:
  mitmproxy -s proxy/header_stats_addon.py \
    --set header_stats_file=proxy/header-stats.json \
    --set target_header=x-my-custom-header,x-client-version
"""

from __future__ import annotations

import json
import threading
import time
from collections import Counter, defaultdict
from dataclasses import dataclass, field
from pathlib import Path
from typing import DefaultDict

from mitmproxy import ctx, http


@dataclass
class HeaderStats:
    output_path: Path
    target_headers: set[str] = field(default_factory=set)
    total_requests: int = 0
    requests_by_host: Counter[str] = field(default_factory=Counter)
    headers_present: Counter[str] = field(default_factory=Counter)
    header_values: DefaultDict[str, Counter[str]] = field(default_factory=lambda: defaultdict(Counter))

    def observe(self, flow: http.HTTPFlow) -> None:
        self.total_requests += 1
        host = flow.request.host or "unknown"
        self.requests_by_host[host] += 1

        for name, value in flow.request.headers.items(multi=True):
            header_name = name.lower()
            if self.target_headers and header_name not in self.target_headers:
                continue

            self.headers_present[header_name] += 1
            self.header_values[header_name][value] += 1

    def snapshot(self) -> dict:
        return {
            "timestamp": int(time.time()),
            "total_requests": self.total_requests,
            "requests_by_host": self.requests_by_host,
            "headers_present": self.headers_present,
            "header_values": {
                header: values.most_common(20)
                for header, values in self.header_values.items()
            },
        }

    def save(self) -> None:
        payload = self.snapshot()
        self.output_path.parent.mkdir(parents=True, exist_ok=True)
        self.output_path.write_text(json.dumps(payload, indent=2), encoding="utf-8")


class HeaderStatsAddon:
    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._stats: HeaderStats | None = None

    def load(self, loader) -> None:
        loader.add_option(
            name="header_stats_file",
            typespec=str,
            default="header-stats.json",
            help="Where to save aggregated custom-header statistics as JSON.",
        )
        loader.add_option(
            name="target_header",
            typespec=str,
            default="",
            help="Comma-separated list of lowercase headers to track. Empty means all headers.",
        )

    def configure(self, updates) -> None:
        output_path = Path(ctx.options.header_stats_file).expanduser().resolve()
        target_headers = {
            header.strip().lower()
            for header in str(ctx.options.target_header).split(",")
            if header.strip()
        }

        with self._lock:
            self._stats = HeaderStats(output_path=output_path, target_headers=target_headers)

        tracked = ", ".join(sorted(target_headers)) if target_headers else "all headers"
        ctx.log.info(f"Header stats enabled. Tracking {tracked}; writing to {output_path}")

    def request(self, flow: http.HTTPFlow) -> None:
        with self._lock:
            if self._stats is None:
                return
            self._stats.observe(flow)
            self._stats.save()

    def done(self) -> None:
        with self._lock:
            if self._stats is not None:
                self._stats.save()


addons = [HeaderStatsAddon()]
