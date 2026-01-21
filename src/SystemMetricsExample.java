import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class SystemMetricsExample {
    public static void main(String[] args) {
        OperatingSystemMXBean baseBean = ManagementFactory.getOperatingSystemMXBean();
        com.sun.management.OperatingSystemMXBean sunBean =
                (baseBean instanceof com.sun.management.OperatingSystemMXBean)
                        ? (com.sun.management.OperatingSystemMXBean) baseBean
                        : null;

        System.out.println("OS Name: " + baseBean.getName());
        System.out.println("OS Version: " + baseBean.getVersion());
        System.out.println("Architecture: " + baseBean.getArch());
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Available processors: " + baseBean.getAvailableProcessors());
        double loadAvg = baseBean.getSystemLoadAverage();
        if (loadAvg >= 0) {
            System.out.printf("System load average (1 min): %.2f%n", loadAvg);
        } else {
            String osName = baseBean.getName().toLowerCase();
            if (osName.contains("windows")) {
                System.out.println("System load average is not available on Windows. Use the CPU load metrics below as an alternative.");
            } else {
                System.out.println("System load average is not available on this platform.");
            }
        }

        if (sunBean != null) {
            double processCpuLoad = sunBean.getProcessCpuLoad();
            double systemCpuLoad = sunBean.getSystemCpuLoad();
            long totalMemory = sunBean.getTotalPhysicalMemorySize();
            long freeMemory = sunBean.getFreePhysicalMemorySize();

            System.out.printf("Process CPU load: %.2f%%%n", processCpuLoad * 100.0);
            System.out.printf("System CPU load: %.2f%%%n", systemCpuLoad * 100.0);
            System.out.printf("Total physical memory: %.2f GB%n", bytesToGb(totalMemory));
            System.out.printf("Free physical memory: %.2f GB%n", bytesToGb(freeMemory));
        } else {
            System.out.println("Extended OS metrics are not available on this JVM.");
        }
    }

    private static double bytesToGb(long bytes) {
        return bytes / 1024.0 / 1024.0 / 1024.0;
    }
}
