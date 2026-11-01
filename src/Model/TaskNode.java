package Model;

import java.util.Collections;
import java.util.List;

public class TaskNode {
    private final String id;
    private final int weight;
    private final List<String> dependencies;

    public TaskNode(String id, int weight, List<String> dependencies) {
        this.id = id;
        this.weight = weight;
        this.dependencies = Collections.unmodifiableList(dependencies);
    }

    public String getId() {
        return id;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    /**
     * Детерминирана CPU-bound работа:
     * Комбинира резултатите от предходните задачи и завърта цикъл според тежестта.
     */
    public long execute(List<Long> inputResults) {
        long accumulator = 17L;
        for (Long res : inputResults) {
            accumulator = accumulator * 31L + (res != null ? res : 0L);
        }

        // Базов множител за постигане на измеримо време (напр. 50-150 ms на задача)
        long iterations = (long) weight * 100_000L;
        for (long i = 0; i < iterations; i++) {
            accumulator ^= (accumulator << 13);
            accumulator ^= (accumulator >>> 17);
            accumulator ^= (accumulator << 5);
            accumulator += (i * 31L);
        }

        return accumulator;
    }
}

