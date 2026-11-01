package Graph;

import Model.TaskNode;

import java.util.*;

public class GraphAnalyzer {

    public static void printGraphMetrics(List<TaskNode> tasks) {
        Map<String, TaskNode> taskMap = new HashMap<>();
        Map<String, List<String>> dependents = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        int totalWork = 0;
        for (TaskNode t : tasks) {
            taskMap.put(t.getId(), t);
            totalWork += t.getWeight();
            inDegree.put(t.getId(), t.getDependencies().size());
            dependents.putIfAbsent(t.getId(), new ArrayList<>());
        }

        for (TaskNode t : tasks) {
            for (String dep : t.getDependencies()) {
                dependents.get(dep).add(t.getId());
            }
        }

        Map<String, Integer> levelMap = new HashMap<>();
        Map<String, Integer> finishTimeMap = new HashMap<>();

        Queue<String> queue = new LinkedList<>();
        for (TaskNode t : tasks) {
            if (t.getDependencies().isEmpty()) {
                queue.add(t.getId());
                levelMap.put(t.getId(), 1);
                finishTimeMap.put(t.getId(), t.getWeight());
            }
        }

        int criticalPath = 0;
        Map<Integer, List<String>> tasksByLevel = new TreeMap<>();

        while (!queue.isEmpty()) {
            String currId = queue.poll();
            int currLevel = levelMap.get(currId);
            int currFinish = finishTimeMap.get(currId);

            tasksByLevel.computeIfAbsent(currLevel, k -> new ArrayList<>()).add(currId);
            criticalPath = Math.max(criticalPath, currFinish);

            for (String nextId : dependents.get(currId)) {
                int nextInDegree = inDegree.get(nextId) - 1;
                inDegree.put(nextId, nextInDegree);

                levelMap.put(nextId, Math.max(levelMap.getOrDefault(nextId, 1), currLevel + 1));
                TaskNode nextTask = taskMap.get(nextId);
                int potentialFinish = currFinish + nextTask.getWeight();
                finishTimeMap.put(nextId, Math.max(finishTimeMap.getOrDefault(nextId, 0), potentialFinish));

                if (nextInDegree == 0) {
                    queue.add(nextId);
                }
            }
        }

        double theoreticalSpeedup = (double) totalWork / criticalPath;

        System.out.println("--- Теоретични характеристики на графа ---");
        System.out.printf("Обща изчислителна работа (W): %d единици%n", totalWork);
        System.out.printf("Дължина на критичния път (T_inf): %d единици%n", criticalPath);
        System.out.printf("Максимално теоретично ускорение (S_max = W / T_inf): %.2fx%n", theoreticalSpeedup);
        System.out.println("Едновременни задачи по етапи (паралелизъм по слоеве):");
        for (Map.Entry<Integer, List<String>> entry : tasksByLevel.entrySet()) {
            System.out.printf("  Етап %d: %d задачи едновременно %s%n",
                    entry.getKey(), entry.getValue().size(), entry.getValue());
        }
    }
}

