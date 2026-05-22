package deus.brainless.ai;

import deus.brainless.ai.interfaces.JobTask;

import java.util.*;

/**
 * Pre-calculates a sorted job queue from node desires.
 * On each update, compares current desires against snapshot.
 * If total delta exceeds threshold, discards and recalculates.
 */
public class JobQueue<CTX> {

    public enum Mode {
        /** Highest desire always goes first. */
        HIGHEST_WINS,
        /** Sorted into tiers (low/mid/high), FIFO within each tier. */
        FIFO_TIERED,
        /** Higher desire = higher chance of being picked, not guaranteed. */
        WEIGHTED_RANDOM
    }

    private final Mode mode;
    private final double discardThreshold;
    private final int preCalculateCount;
    private final Random random = new Random();

    private final List<JobDefinition<CTX>> definitions = new ArrayList<>();
    private final Deque<Job<CTX>> queue = new ArrayDeque<>();
    private final Map<String, Double> snapshot = new HashMap<>();

    private Job<CTX> current = null;

    /**
     * @param mode              how jobs are sorted
     * @param discardThreshold  total desire delta to trigger full recalculation (0..1 range, e.g. 0.2)
     * @param preCalculateCount how many jobs to pre-calculate ahead
     */
    public JobQueue(Mode mode, double discardThreshold, int preCalculateCount) {
        this.mode = mode;
        this.discardThreshold = discardThreshold;
        this.preCalculateCount = preCalculateCount;
    }

    public JobQueue(Mode mode) {
        this(mode, 0.25, 3);
    }

    public void register(String name, Node desireNode, JobTask<CTX> task) {
        register(name, desireNode, task, null);
    }

    public void register(String name, Node desireNode, JobTask<CTX> task, JobTask<CTX> onInterrupt) {
        definitions.add(new JobDefinition<CTX>(name, desireNode, task, onInterrupt));
    }

    /**
     * Called after Brain.compute(). Checks delta, maybe rebuilds queue, executes next job.
     */
    public void update(CTX context) {
        if (shouldDiscard()) {
            if (current != null) {
                current.interrupt(context);
                current = null;
            }
            rebuildQueue();
        }

        if (queue.isEmpty()) rebuildQueue();

        if (!queue.isEmpty()) {
            current = queue.poll();
            current.run(context);
        }
    }

    public Job<CTX> getCurrent() {
        return current;
    }

    public List<Job<CTX>> peekQueue() {
        return List.copyOf(queue);
    }

    private boolean shouldDiscard() {
        if (snapshot.isEmpty()) return true;

        double totalDelta = 0;
        for (JobDefinition<CTX> def : definitions) {
            double prev = snapshot.getOrDefault(def.name, 0.0);
            totalDelta += Math.abs(def.desireNode.value - prev);
        }
        return totalDelta >= discardThreshold;
    }

    private void rebuildQueue() {
        // Take snapshot of current desires
        for (JobDefinition<CTX> def : definitions) {
            snapshot.put(def.name, def.desireNode.value);
        }

        List<JobDefinition<CTX>> candidates = new ArrayList<>(definitions);

        queue.clear();

        List<Job<CTX>> built = switch (mode) {
            case HIGHEST_WINS -> buildHighestWins(candidates);
            case FIFO_TIERED  -> buildFifoTiered(candidates);
            case WEIGHTED_RANDOM -> buildWeightedRandom(candidates);
        };

        int count = Math.min(preCalculateCount, built.size());
        for (int i = 0; i < count; i++) {
            queue.add(built.get(i));
        }
    }

    private List<Job<CTX>> buildHighestWins(List<JobDefinition<CTX>> defs) {
        return defs.stream()
                .filter(d -> d.desireNode.value > 0)
                .sorted((a, b) -> Double.compare(b.desireNode.value, a.desireNode.value))
                .map(d -> new Job<CTX>(d.name, d.desireNode.value, d.task, d.onInterrupt))
                .toList();
    }

    private List<Job<CTX>> buildFifoTiered(List<JobDefinition<CTX>> defs) {
        List<JobDefinition<CTX>> high = new ArrayList<>(), mid = new ArrayList<>(), low = new ArrayList<>();
        for (JobDefinition<CTX> d : defs) {
            if (d.desireNode.value <= 0) continue;
            if (d.desireNode.value >= 0.7)      high.add(d);
            else if (d.desireNode.value >= 0.3)  mid.add(d);
            else                                  low.add(d);
        }
        List<Job<CTX>> result = new ArrayList<>();
        for (JobDefinition<CTX> d : high) result.add(new Job<CTX>(d.name, d.desireNode.value, d.task, d.onInterrupt));
        for (JobDefinition<CTX> d : mid)  result.add(new Job<CTX>(d.name, d.desireNode.value, d.task, d.onInterrupt));
        for (JobDefinition<CTX> d : low)  result.add(new Job<CTX>(d.name, d.desireNode.value, d.task, d.onInterrupt));
        return result;
    }

    private List<Job<CTX>> buildWeightedRandom(List<JobDefinition<CTX>> defs) {
        List<JobDefinition<CTX>> pool = defs.stream()
                .filter(d -> d.desireNode.value > 0)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        List<Job<CTX>> result = new ArrayList<>();
        int picks = Math.min(preCalculateCount, pool.size());

        for (int i = 0; i < picks && !pool.isEmpty(); i++) {
            double total = pool.stream().mapToDouble(d -> d.desireNode.value).sum();
            double roll = random.nextDouble() * total;
            double acc = 0;
            for (int j = 0; j < pool.size(); j++) {
                acc += pool.get(j).desireNode.value;
                if (acc >= roll) {
					JobDefinition<CTX> chosen = pool.remove(j);
                    result.add(new Job<CTX>(chosen.name, chosen.desireNode.value, chosen.task, chosen.onInterrupt));
                    break;
                }
            }
        }
        return result;
    }


    private record JobDefinition<CTX>(String name, Node desireNode, JobTask<CTX> task, JobTask<CTX> onInterrupt) {}
}
