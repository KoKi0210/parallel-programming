package Executors;

import Model.ExecutionResult;
import Model.TaskNode;

import java.util.*;
import java.util.concurrent.*;

public class AsyncExecutor {

    public static ExecutionResult run(List<TaskNode> tasks, ExecutorService executor) {
        Map<String, TaskNode> taskMap = new HashMap<>();
        for (TaskNode task : tasks) {
            taskMap.put(task.getId(), task);
        }

        Map<String, CompletableFuture<Long>> futures = new HashMap<>();

        long startTime = System.nanoTime();

        for (TaskNode task : tasks) {
            getOrCreateFuture(task.getId(), taskMap, futures, executor);
        }

        CompletableFuture<?>[] allFuturesArray = futures.values().toArray(new CompletableFuture[0]);
        CompletableFuture<Void> graphCompletion = CompletableFuture.allOf(allFuturesArray);

        graphCompletion.join();

        long totalTimeNs = System.nanoTime() - startTime;

        Map<String, Long> results = new HashMap<>();
        for (Map.Entry<String, CompletableFuture<Long>> entry : futures.entrySet()) {
            results.put(entry.getKey(), entry.getValue().join());
        }

        long checksum = calculateChecksum(results);

        return new ExecutionResult(totalTimeNs / 1_000_000.0, checksum);
    }

    private static CompletableFuture<Long> getOrCreateFuture(
            String taskId,
            Map<String, TaskNode> taskMap,
            Map<String, CompletableFuture<Long>> futures,
            ExecutorService executor) {

        if (futures.containsKey(taskId)) {
            return futures.get(taskId);
        }

        TaskNode task = taskMap.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Несъществуваща задача с ID: " + taskId);
        }

        List<String> deps = task.getDependencies();
        CompletableFuture<Long> taskFuture;

        if (deps.isEmpty()) {
            taskFuture = CompletableFuture.supplyAsync(
                    () -> task.execute(Collections.emptyList()),
                    executor
            );
        } else {
            List<CompletableFuture<Long>> depFutures = new ArrayList<>();
            for (String depId : deps) {
                depFutures.add(getOrCreateFuture(depId, taskMap, futures, executor));
            }

            CompletableFuture<?>[] depArray = depFutures.toArray(new CompletableFuture[0]);

            taskFuture = CompletableFuture.allOf(depArray).thenApplyAsync(voidResult -> {
                List<Long> inputResults = new ArrayList<>();
                for (CompletableFuture<Long> df : depFutures) {
                    inputResults.add(df.join());
                }
                return task.execute(inputResults);
            }, executor);
        }

        futures.put(taskId, taskFuture);
        return taskFuture;
    }

    private static long calculateChecksum(Map<String, Long> results) {
        long checksum = 0L;
        for (long res : results.values()) {
            checksum += res;
        }
        return checksum;
    }
}

