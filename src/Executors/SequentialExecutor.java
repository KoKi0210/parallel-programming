package Executors;

import Model.ExecutionResult;
import Model.TaskNode;

import java.util.*;

public class SequentialExecutor {

    public static ExecutionResult run(List<TaskNode> tasks) {
        List<TaskNode> executionOrder = topologicalSort(tasks);
        Map<String, Long> results = new HashMap<>();

        long startTime = System.nanoTime();

        for (TaskNode task : executionOrder) {
            List<Long> inputResults = new ArrayList<>();
            for (String depId : task.getDependencies()) {
                inputResults.add(results.get(depId));
            }

            long taskResult = task.execute(inputResults);
            results.put(task.getId(), taskResult);
        }

        long totalTimeNs = System.nanoTime() - startTime;
        long checksum = calculateChecksum(results);

        return new ExecutionResult(totalTimeNs / 1_000_000.0, checksum);
    }

    private static List<TaskNode> topologicalSort(List<TaskNode> tasks) {
        Map<String, TaskNode> taskMap = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> dependents = new HashMap<>();

        for (TaskNode t : tasks) {
            taskMap.put(t.getId(), t);
            inDegree.put(t.getId(), t.getDependencies().size());
            dependents.putIfAbsent(t.getId(), new ArrayList<>());
        }

        for (TaskNode t : tasks) {
            for (String dep : t.getDependencies()) {
                dependents.get(dep).add(t.getId());
            }
        }

        Queue<String> readyQueue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                readyQueue.add(entry.getKey());
            }
        }

        List<TaskNode> sorted = new ArrayList<>();
        while (!readyQueue.isEmpty()) {
            String currentId = readyQueue.poll();
            sorted.add(taskMap.get(currentId));

            for (String nextId : dependents.get(currentId)) {
                int updatedInDegree = inDegree.get(nextId) - 1;
                inDegree.put(nextId, updatedInDegree);
                if (updatedInDegree == 0) {
                    readyQueue.add(nextId);
                }
            }
        }

        if (sorted.size() != tasks.size()) {
            throw new IllegalStateException("Графът съдържа цикъл или невалидни зависимости!");
        }

        return sorted;
    }

    private static long calculateChecksum(Map<String, Long> results) {
        long checksum = 0L;
        for (long res : results.values()) {
            checksum += res;
        }
        return checksum;
    }
}
