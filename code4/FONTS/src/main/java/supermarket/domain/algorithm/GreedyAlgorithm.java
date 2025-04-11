
package supermarket.domain.algorithm;

import supermarket.tuples.AlgorithmParameter;
import supermarket.tuples.AlgorithmType;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that implements the Greedy Algorithm for solving the TSP.
 * @author Dídac Dalmases Valcárcel
 */
public class GreedyAlgorithm extends AbstractAlgorithm {
    boolean testAllStartingNodes = false;
    public GreedyAlgorithm(float[][] relationMatrix) {
        super(relationMatrix);
    }

    public AlgorithmType getType() {
        return AlgorithmType.GREEDY;
    }


    public boolean canUseAlgorithm() {
        return super.canUseAlgorithm();
    }

    public ArrayList<AlgorithmParameter> getAvailableParameters() {
        ArrayList<AlgorithmParameter> params = super.getAvailableParameters();
        params.add(new AlgorithmParameter(
                "Test All Starting Nodes",
                "If true, it will run the algorithm n times, one for each starting node. Otherwise, it will only try with 0 as starting node (default).",
                "{true, false}"
        ));
        return params;
    }

    public void setParameters(List<String> parameters) {
        super.setParameters(parameters);
        String param = parameters.removeFirst();
        if (param.equals("true")) testAllStartingNodes = true;
        else if (param.equals("false")) testAllStartingNodes = false;
        else System.err.println("Received unknown param for \"Test all starting nodes\": " + param + ". Using default value instead.");
    }

    private int findBestNext(int current, boolean[] visited) {
        float minRelation = Float.MAX_VALUE; //largest number
        int next = -1;

        for (int i = 0; i < n; i++) {
            if (current != i && !visited[i] && distanceMatrix[current][i] < minRelation) {
                minRelation = distanceMatrix[current][i];
                next = i;
            }
        }
        return next;
    }

    int[] findOrder(int startingNode) {
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int current = startingNode;
        order[0] = current;
        visited[current] = true;

        for (int i = 1; i < n; i++) {
            int next = findBestNext(current, visited);
            if (next != -1) {
                order[i] = next;
                visited[next] = true;
                current = next;
            }
        }
        return order;
    }

    public int[] calculateDistribution() {
        if (distanceMatrix.length == 0) return new int[] {};
        if (!testAllStartingNodes) return findOrder(0);
        else {
            int[] bestOrder = findOrder(0);
            float bestScore = calculateCycleCost(bestOrder);
            for (int i = 1; i < n; ++i) {
                int[] newOrder = findOrder(i);
                float newScore = calculateCycleCost(newOrder);
                if (newScore < bestScore) {
                    bestScore = newScore;
                    bestOrder = newOrder;
                }
            }
            return bestOrder;
        }
    }
}
