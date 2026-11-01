package Graph;

import Model.TaskNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GraphLoader {
    public static List<TaskNode> loadGraph(String filePath) throws IOException {
        List<TaskNode> tasks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                String id = parts[0].trim();
                int weight = Integer.parseInt(parts[1].trim());

                List<String> dependencies = new ArrayList<>();
                if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                    String[] deps = parts[2].split(";");
                    for (String dep : deps) {
                        String cleanDep = dep.trim();
                        if (!cleanDep.isEmpty()) {
                            dependencies.add(cleanDep);
                        }
                    }
                }

                tasks.add(new TaskNode(id, weight, dependencies));
            }
        }

        return tasks;
    }
}

