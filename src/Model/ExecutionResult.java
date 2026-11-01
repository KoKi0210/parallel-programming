package Model;

public record ExecutionResult(double executionTimeMs, long checksum) {

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public long getChecksum() {
        return checksum;
    }
}

