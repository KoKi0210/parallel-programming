import Executors.AsyncExecutor;
import Executors.SequentialExecutor;
import Graph.GraphAnalyzer;
import Graph.GraphLoader;
import Model.ExecutionResult;
import Model.TaskNode;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int WARMUP_RUNS = 5;
    private static final int BENCHMARK_RUNS = 10;
    private static final int[] THREAD_CONFIGS = {1, 2, 4, 8};

    public static void main(String[] args) {
        try {
            runExperiment("graph1_wide.csv", "Граф 1 (Широк - висок паралелизъм)");
            System.out.println("\n" + "=".repeat(75) + "\n");
            runExperiment("graph2_deep.csv", "Граф 2 (Дълбок - нисък паралелизъм)");
        } catch (IOException e) {
            System.err.println("Грешка при зареждане на CSV файловете: " + e.getMessage());
        }
    }

    private static void runExperiment(String filePath, String graphName) throws IOException {
        System.out.println("Изследване: " + graphName);
        System.out.println("Файл: " + filePath);

        List<TaskNode> tasks = GraphLoader.loadGraph(filePath);

        GraphAnalyzer.printGraphMetrics(tasks);
        System.out.println();

        // Warm-up
        System.out.print("Warm-up на JVM...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            SequentialExecutor.run(tasks);
            ExecutorService tempPool = Executors.newFixedThreadPool(4);
            AsyncExecutor.run(tasks, tempPool);
            tempPool.shutdown();
        }
        System.out.println(" Готово.");

        // Sequential execution benchmark
        double totalSeqTime = 0.0;
        long seqChecksum = 0L;
        for (int i = 0; i < BENCHMARK_RUNS; i++) {
            ExecutionResult res = SequentialExecutor.run(tasks);
            totalSeqTime += res.getExecutionTimeMs();
            seqChecksum = res.getChecksum();
        }
        double avgSeqTime = totalSeqTime / BENCHMARK_RUNS;

        System.out.printf("Последователно време (T_seq / T_1_seq): %.2f ms (Checksum: %d)%n", avgSeqTime, seqChecksum);
        System.out.println("-".repeat(75));
        System.out.printf("%-10s | %-15s | %-15s | %-15s | %-10s%n",
                "Нишки (P)", "Време (ms)", "Ускорение (Sp)", "Ефективност (Ep)", "Checksum OK");
        System.out.println("-".repeat(75));

        // Parallel execution benchmark
        for (int threads : THREAD_CONFIGS) {
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            double totalAsyncTime = 0.0;
            long asyncChecksum = 0L;

            try {
                for (int i = 0; i < BENCHMARK_RUNS; i++) {
                    ExecutionResult res = AsyncExecutor.run(tasks, pool);
                    totalAsyncTime += res.getExecutionTimeMs();
                    asyncChecksum = res.getChecksum();
                }
            } finally {
                pool.shutdown();
            }

            double avgAsyncTime = totalAsyncTime / BENCHMARK_RUNS;
            double speedup = avgSeqTime / avgAsyncTime;
            double efficiency = speedup / threads;
            boolean checksumMatches = (seqChecksum == asyncChecksum);

            System.out.printf("%-10d | %-15.2f | %-15.2fx | %-15.2f | %-10s%n",
                    threads, avgAsyncTime, speedup, efficiency, checksumMatches ? "ДА" : "НЕ");
        }
    }
}
