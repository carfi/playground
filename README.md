# Java OS Metrics Example

This example shows how to read basic OS information and CPU/memory metrics using the JDK's management APIs.

## Build and run

```bash
javac src/SystemMetricsExample.java
java -cp src SystemMetricsExample
```

## Notes

- `OperatingSystemMXBean` provides portable metrics like OS name, version, architecture, and system load average.
- The `com.sun.management.OperatingSystemMXBean` extension exposes CPU load and physical memory metrics on HotSpot-based JVMs.
