package pruning;

import java.util.*;

public class PathPruner {

    private static Map<Integer, List<Deque<Integer>>> pathsBySize = new HashMap<>();

    /**
     * Samples the given path collection according to their probability distribution.
     *
     * @param paths    path collection.
     * @param samples  number of paths to sample.
     * @return a list of the sample paths of size [samples].
     */
    public static List<Deque<Integer>> sample(List<Deque<Integer>> paths, int samples) {
        List<Deque<Integer>> sampledPaths = new ArrayList<>(samples);
        int minSize = shortestPathSize(paths);
        int maxSize = largestPathSize(paths);
        Map<Integer, Double> cumulative = getCumulativeProbabilities(paths, minSize, maxSize);

        // choosing a path based on its cumulative probability
        double rnd;
        for (int i = 0; i < samples; i++) {
            rnd = Math.random();

            int pathSize = (int) averagePathSize(paths);
            double prob;
            for (Map.Entry<Integer, Double> e : cumulative.entrySet()) {
                prob = e.getValue();
                if (prob >= rnd) {
                    pathSize = e.getKey();
                    break;
                }
            }

            // Randomly choosing a path of the found size
            int pathIndex = (int) (Math.random() * pathsBySize.get(pathSize).size());
            sampledPaths.add(pathsBySize.get(pathSize).get(pathIndex));
        }

        return sampledPaths;
    }

    /**
     * Calculates the cumulative probability of the paths by size.
     *
     * @param paths path collection
     * @param min   shortest path size
     * @param max   largest path size
     * @return cumulative probabilities.
     */
    private static Map<Integer, Double> getCumulativeProbabilities(List<Deque<Integer>> paths, int min,
                                                            int max) {
        Map<Integer, Integer> absolute = getAbsoluteFrequencies(paths, min, max);
        Map<Integer, Double> cumulative = new HashMap<>();

        // map initialisation
        for (int i = min; i <= max; i++)
            cumulative.put(i, 0.0);

        // sorting the keys in ascending order
        List<Integer> sortedKeys = new ArrayList<>(cumulative.keySet());
        Collections.sort(sortedKeys);

        // dealing with the first value of the map
        double initProb = (double) absolute.get(min) / paths.size();
        cumulative.put(min, initProb);

        // computing cumulative probabilities
        double prevProb, currProb;
        int curr;
        for (int i = 1; i < sortedKeys.size(); i++) {
            curr = sortedKeys.get(i);
            prevProb = cumulative.get(sortedKeys.get(i - 1));
            currProb = (double) absolute.get(curr) / paths.size();
            cumulative.put(curr, currProb + prevProb);
        }

        return cumulative;
    }

    /**
     * Calculates the absolute frequencies by path size (e.g, there are 190 paths of size 9).
     *
     * @param paths path collection
     * @param min   shortest path size
     * @param max   largest path size
     * @return absolute frequencies
     */
    private static Map<Integer, Integer> getAbsoluteFrequencies(List<Deque<Integer>> paths,
                                                               int min, int max) {
        Map<Integer, Integer> absoluteFrequency = new HashMap<>();
        pathsBySize = new HashMap<>();

        // map initialisation
        for (int i = min; i <= max; i++) {
            absoluteFrequency.put(i, 0);
            pathsBySize.put(i, new ArrayList<>());
        }

        List<Deque<Integer>> current;
        for (Deque<Integer> path : paths) {
            absoluteFrequency.put(path.size(), absoluteFrequency.get(path.size()) + 1);
            current = pathsBySize.get(path.size());
            current.add(path);
            pathsBySize.put(path.size(), current);
        }

        return absoluteFrequency;
    }

    /**
     * Finds the largest path size in the given collection.
     *
     * @param paths collection
     * @return largest path size.
     */
    public static int largestPathSize(List<Deque<Integer>> paths) {
        int max = 0;

        for (Deque<Integer> path: paths)
            if(path.size() > max)
                max = path.size();

        return max;
    }

    /**
     * Finds the shortest path size in the given collection.
     *
     * @param paths collection
     * @return shortest path size.
     */
    public static int shortestPathSize(List<Deque<Integer>> paths) {
        int min = (int) averagePathSize(paths);

        for (Deque<Integer> path: paths)
            if(path.size() < min)
                min = path.size();

        return min;
    }

    /**
     * Computes the average path size.
     *
     * @param paths collection
     * @return average path size.
     */
    public static double averagePathSize(List<Deque<Integer>> paths) {
        long sum = 0L;

        for (Deque<Integer> path: paths)
            sum += path.size();

        return (double) sum / paths.size();
    }


}
