/**
 * SystemInfo.java
 *
 * A utility to print CPU and processor information for benchmarking.
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
        
        // Additional CPU information
        System.out.println("\n=== CPU Information ===");
        System.out.println("Total available processors: " + cores);
        
        // Memory information (useful for understanding system capacity)
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        System.out.println("\n=== Memory Information ===");
        System.out.println("Max memory (bytes): " + maxMemory + " (" + (maxMemory / (1024 * 1024)) + " MB)");
        System.out.println("Total memory (bytes): " + totalMemory + " (" + (totalMemory / (1024 * 1024)) + " MB)");
        System.out.println("Free memory (bytes): " + freeMemory + " (" + (freeMemory / (1024 * 1024)) + " MB)");
        System.out.println("Used memory (bytes): " + (totalMemory - freeMemory) + " (" + ((totalMemory - freeMemory) / (1024 * 1024)) + " MB)");
    }
}