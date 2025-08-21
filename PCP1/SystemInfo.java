/**
 * SystemInfo.java
 *
 * A utility to print CPU and thread information for benchmarking.
 *
 * Usage:
 *   javac SystemInfo.java
 *   java SystemInfo
 */

public class SystemInfo {
    public static void main(String[] args) {
        // Number of processors available to the JVM
        int cores = Runtime.getRuntime().availableProcessors();

        // JVM and system info
        System.out.println("=== System Information ===");
        System.out.println("Available processors (cores) to JVM: " + cores);
        System.out.println("Java Runtime version: " + System.getProperty("java.runtime.version"));
        System.out.println("Java VM name: " + System.getProperty("java.vm.name"));
        System.out.println("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        
        // Safer thread creation test with cap
        System.out.println("\n=== Thread Creation Test (up to 10,000) ===");
        int maxThreads = 0;
        try {
            for (int i = 0; i < 10000; i++) {
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        // ignored
                    }
                });
                t.start();
                maxThreads++;
                if (maxThreads % 500 == 0) {
                    System.out.println("Created " + maxThreads + " threads so far...");
                }
            }
            System.out.println("Created " + maxThreads + " threads successfully (limit not reached).");
        } catch (OutOfMemoryError e) {
            System.out.println("Stopped at ~" + maxThreads + " threads due to OutOfMemoryError.");
        } catch (Throwable t) {
            System.out.println("Thread creation stopped at ~" + maxThreads + " threads. Reason: " + t);
        }
    }
}
