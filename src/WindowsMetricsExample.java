import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public class WindowsMetricsExample {
    // Define PDH interfaces
    public interface Pdh extends StdCallLibrary {
        Pdh INSTANCE = Native.load("pdh", Pdh.class);

        int PdhOpenQuery(String szDataSource, DWORD dwUserData, HANDLEByReference phQuery);
        int PdhAddCounter(HANDLE hQuery, String szFullCounterPath, DWORD dwUserData, HANDLEByReference phCounter);
        int PdhCollectQueryData(HANDLE hQuery);
        int PdhGetFormattedCounterValue(HANDLE hCounter, DWORD dwFormat, DWORDByReference lpdwType, PDH_FMT_COUNTERVALUE pValue);
        int PdhCloseQuery(HANDLE hQuery);
    }

    // PDH counter value structure
    public static class PDH_FMT_COUNTERVALUE extends com.sun.jna.Structure {
        public int CStatus;
        public double doubleValue;

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("CStatus", "doubleValue");
        }
    }

    public static void main(String[] args) {
        // Check if Windows
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("windows")) {
            System.out.println("This example is for Windows only.");
            return;
        }

        HANDLEByReference query = new HANDLEByReference();
        HANDLEByReference counter = new HANDLEByReference();

        try {
            // Open PDH query
            int result = Pdh.INSTANCE.PdhOpenQuery(null, new DWORD(0), query);
            if (result != 0) {
                System.out.println("Failed to open PDH query. Error: " + result);
                return;
            }

            // Add CPU usage counter
            result = Pdh.INSTANCE.PdhAddCounter(query.getValue(), "\\Processor(_Total)\\% Processor Time", new DWORD(0), counter);
            if (result != 0) {
                System.out.println("Failed to add counter. Error: " + result);
                return;
            }

            // Collect data
            result = Pdh.INSTANCE.PdhCollectQueryData(query.getValue());
            if (result != 0) {
                System.out.println("Failed to collect data. Error: " + result);
                return;
            }

            // Wait a bit for data to stabilize
            Thread.sleep(1000);

            // Collect data again
            result = Pdh.INSTANCE.PdhCollectQueryData(query.getValue());
            if (result != 0) {
                System.out.println("Failed to collect data second time. Error: " + result);
                return;
            }

            // Get formatted value
            PDH_FMT_COUNTERVALUE value = new PDH_FMT_COUNTERVALUE();
            result = Pdh.INSTANCE.PdhGetFormattedCounterValue(counter.getValue(), new DWORD(0x00000100), new DWORDByReference(), value);
            if (result != 0) {
                System.out.println("Failed to get counter value. Error: " + result);
                return;
            }

            System.out.printf("Windows CPU Usage: %.2f%%%n", value.doubleValue);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close query
            if (query.getValue() != null) {
                Pdh.INSTANCE.PdhCloseQuery(query.getValue());
            }
        }
    }
}