/*
 * Name:       Sandaru Senevirathne
 * Student ID: 20232351
 * Module:     5SENG003W Algorithms
 * Class:      PerformanceTest
 * Purpose:    Times the acyclicity checker across benchmark files and prints a performance table.
 */
package edu.iit;

import java.util.ArrayList;
import java.util.List;

/**
 * Measures and displays the runtime performance of the acyclicity checker.
 */
public class PerformanceTest {

    private static final int    COL_FILE      = 30;
    private static final int    COL_VERTICES  = 16;
    private static final int    COL_EXEC      = 22;
    private static final int    COL_RESULT    = 10;
    private static final int[]  VERTEX_COUNTS = {40, 80, 160, 320};
    private static final int    MAX_VARIANT   = 4;

    private static final String ACYCLIC_FOLDER = "benchmarks/acyclic/";
    private static final String CYCLIC_FOLDER  = "benchmarks/cyclic/";

    /**
     * Runs the performance table then the doubling hypothesis table.
     *
     * @param args  not used
     */
    public static void main(String[] args) {
        runPerformanceTable();
    }

    /**
     * Loads every available benchmark file, times a single run, and prints
     * a formatted table. Times are auto-scaled so the smallest value appears
     * as x.xx - larger values appear as xx.xx or xxx.xx using the same scale.
     */
    private static void runPerformanceTable() {
        System.out.println("==========================================");
        System.out.println(" Graph Performance Test");
        System.out.println("==========================================");
        System.out.println();

        List<String>  names    = collectBenchmarkFiles();
        List<Long>    timesNs  = new ArrayList<>();
        List<Graph>   graphs   = new ArrayList<>();
        List<Boolean> acyclics = new ArrayList<>();

        for (String filename : names) {
            Graph   graph     = Main.parseFromClasspathSilent(ACYCLIC_FOLDER + filename);
            boolean isAcyclic = (graph != null);
            if (!isAcyclic) graph = Main.parseFromClasspathSilent(CYCLIC_FOLDER + filename);
            if (graph == null) continue;

            long startNs = System.nanoTime();
            AcyclicityChecker.isAcyclic(graph, false);
            timesNs.add(System.nanoTime() - startNs);
            graphs.add(graph);
            acyclics.add(isAcyclic);
        }

        //  auto-scale so the smallest value appears as x.xx
        long   minNs      = timesNs.stream().mapToLong(Long::longValue).min().orElse(1L);
        double minMs      = minNs / 1_000_000.0;
        int    scalePower = (int) Math.ceil(-Math.log10(minMs));
        double scale      = Math.pow(10, scalePower);
        String unitLabel  = String.format("Time (ms x10^-%d)", scalePower + 3);

        String border = buildBorder();
        System.out.println(border);
        System.out.println(buildRow("File Name", "Total Vertices", unitLabel, "Acyclic?"));
        System.out.println(border);

        for (int i = 0; i < graphs.size(); i++) {
            double scaledTime = (timesNs.get(i) / 1_000_000.0) * scale;
            System.out.println(buildRow(
                names.get(i),
                String.valueOf(graphs.get(i).vertexCount()),
                String.format("%.2f", scaledTime),
                acyclics.get(i) ? "YES" : "NO"
            ));
        }

        System.out.println(border);
        System.out.printf("  * Scale: x10^-%d s  (minimum value normalised to x.xx format)%n",
                scalePower + 3);
    }

    /**
     * Builds the separator border line for the per-file performance table.
     *
     * @return  the border string
     */
    private static String buildBorder() {
        return "+" + "-".repeat(COL_FILE)
             + "+" + "-".repeat(COL_VERTICES)
             + "+" + "-".repeat(COL_EXEC)
             + "+" + "-".repeat(COL_RESULT) + "+";
    }

    /**
     * Builds one formatted row for the per-file performance table.
     *
     * @param file      file name label
     * @param vertices  vertex count as a string
     * @param exec      execution time as a formatted string
     * @param result    YES or NO indicating acyclicity
     * @return  the formatted row string
     */
    private static String buildRow(String file, String vertices,
                                   String exec, String result) {
        return String.format(
            "| %-" + (COL_FILE - 2)  + "s | "
          + "%" + (COL_VERTICES - 2) + "s | "
          + "%" + (COL_EXEC - 2)     + "s | "
          + "%-" + (COL_RESULT - 2)  + "s |",
            file, vertices, exec, result);
    }

    /**
     * Scans all vertex count and variant combinations and returns filenames
     * that exist on the classpath. Acyclic files are listed before cyclic
     * files for each size and variant.
     *
     * @return  ordered list of available benchmark filenames
     */
    private static List<String> collectBenchmarkFiles() {
        List<String> found = new ArrayList<>();

        for (int size : VERTEX_COUNTS) {
            for (int variant = 0; variant <= MAX_VARIANT; variant++) {
                String a = "a_" + size + "_" + variant + ".txt";
                String c = "c_" + size + "_" + variant + ".txt";
                if (Main.parseFromClasspathSilent(ACYCLIC_FOLDER + a) != null) found.add(a);
                if (Main.parseFromClasspathSilent(CYCLIC_FOLDER  + c) != null) found.add(c);
            }
        }
        return found;
    }
}
