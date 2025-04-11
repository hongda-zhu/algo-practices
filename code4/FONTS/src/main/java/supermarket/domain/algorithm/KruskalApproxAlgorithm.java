package supermarket.domain.algorithm;

import java.util.*;

import supermarket.domain.algorithm.GraphUtils.WeightedEdge;
import supermarket.tuples.AlgorithmParameter;
import supermarket.tuples.AlgorithmType;

import static supermarket.domain.algorithm.GraphUtils.matrixToEdgeList;

/**
 * Class that implements the Kruskal Algorithm for solving the TSP with 2-approximation.
 * @author Pau Martí Bisoca
 */
public class KruskalApproxAlgorithm extends AbstractAlgorithm {
    private final ArrayList<WeightedEdge> edges;
    private final int[] parent, rank;

    private String eliminationType = "FirstStartingNode";
    public static final String[] elimTypes = {"FirstStartingNode", "BestStartingNode", "FastBestStartingNode"};

    public KruskalApproxAlgorithm(float[][] distanceMatrix) {
        super(distanceMatrix);

        //create sorted undirected edge list array
        edges = matrixToEdgeList(distanceMatrix);
        edges.sort((o1, o2) -> Float.compare(o1.weight(), o2.weight()));

        parent = new int[n];
        rank = new int[n];
    }

    public AlgorithmType getType() {
        return AlgorithmType.KRUSKAL_APPROX;
    }

    public ArrayList<AlgorithmParameter> getAvailableParameters() {
        ArrayList<AlgorithmParameter> params = super.getAvailableParameters();
        params.add(new AlgorithmParameter(
                "Edge repetition elimination type",
                """
                        The last step of this algorithm converts an eulerian path to a hamiltonian path.
                        FirstStartingNode is a greedy process that starts from node 0, follows the eulerian cycle and adds new nodes when found (default option)
                        BestStartingNode is similar to the previous process, but tries to start from all nodes, and saves the best result
                        FastBestStartingNode removes consecutive nodes before trying to find the best starting node (faster execution)""",
                "{" + elimTypes[0] + "," + elimTypes[1] + "," + elimTypes[2] + "}"
        ));
        return params;
    }

    public void setParameters(List<String> parameters) {
        super.setParameters(parameters);
        //only 1 parameter
        String param = parameters.removeFirst();
        if (!Arrays.asList(elimTypes).contains(param)) {
            System.err.println("Received unknown param for \"Edge repetition elimination type\": " + param + ". Using default value instead.");
            return;
        }
        eliminationType = param;
    }

    public boolean canUseAlgorithm() {
        if (!super.canUseAlgorithm()) return false;
        boolean asymmetric = false;
        //check triangle inequality and symmetry
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (j == i) continue;
                if (distanceMatrix[i][j] != distanceMatrix[j][i]) asymmetric = true;
                for (int k = 0; k < n; ++k) {
                    if (i == k || j == k) continue;
                    if (distanceMatrix[i][j] > distanceMatrix[i][k] + distanceMatrix[k][j]) return false;
                }
            }
        }
        if (asymmetric) {
            System.err.println("Warning: given distance matrix is asymmetric. The algorithm can still be executed, but symmetry will be assumed for the creation of MST.");
        }
        return true;
    }

    private int findSet(int v) {
        if (v == parent[v]) return v;
        return parent[v] = findSet(parent[v]);
    }

    private void UnionSets(int a, int b) {
        a = findSet(a);
        b = findSet(b);
        if (a != b) {
            if (rank[a] < rank[b]) {
                int temp = a; //swap a and b
                a = b;
                b = temp;
            }
            parent[b] = a;
            if (rank[a] == rank[b]) rank[a]++;
        }
    }

    private ArrayList<WeightedEdge> getMST() {
        for (int i = 0; i < n; ++i) {
            parent[i] = i;
            rank[i] = 0;
        }

        ArrayList<WeightedEdge> result = new ArrayList<>();

        for (WeightedEdge e : edges) {
            if (findSet(e.u()) != findSet(e.v())) {
                result.add(e);
                UnionSets(e.u(), e.v());
            }
        }
        return result;
    }

    private ArrayList<Integer> getEulerianPath(boolean[][] adjacencyMatrix) { //assumes an eulerian path exists. DESTRUCTIVE for adjacencyMatrix!
        Stack<Integer> s = new Stack<>();
        s.push(0); //arbitrary first element
        ArrayList<Integer> result = new ArrayList<>();

        while (!s.empty()) {
            int v = s.peek();
            int i;
            for (i = 0; i < n; ++i) if (adjacencyMatrix[v][i]) break;
            if (i == n) {
                result.add(v);
                s.pop();
            }
            else {
                adjacencyMatrix[v][i] = false;
                adjacencyMatrix[i][v] = false;
                s.push(i);
            }
        }
        return result;
    }

    private int[] calculateNodeOrder(ArrayList<Integer> eulerianPath, int startingNode) {
        int[] result = new int[n];
        int pos = 0;
        boolean[] visited = new boolean[n];
        int offset = 0;
        while (eulerianPath.get(offset) != startingNode) ++offset;
        for (int i = 0; i < eulerianPath.size(); ++i) {
            int v = eulerianPath.get((i + offset)%eulerianPath.size());
            if (!visited[v]) {
                visited[v] = true;
                result[pos] = v;
                ++pos;
                if (pos == n) break;
            }
        }
        return result;
    }

    private int[] findByBestStartingNode(ArrayList<Integer> eulerianPath) {
        int[] bestOrder = calculateNodeOrder(eulerianPath, 0);
        float lowestCost = calculateCycleCost(bestOrder);
        for (int i = 1; i < n; ++i) {
            int[] possibleOrder = calculateNodeOrder(eulerianPath, i);
            float cost = calculateCycleCost(possibleOrder);
            if (cost < lowestCost) {
                lowestCost = cost;
                bestOrder = possibleOrder;
            }
        }
        return bestOrder;
    }

    public int[] calculateDistribution() {
        ArrayList<WeightedEdge> mst = getMST();
        boolean[][] adjacencyMatrix = new boolean[n][n];
        for (WeightedEdge e : mst) {
            adjacencyMatrix[e.u()][e.v()] = true;
            adjacencyMatrix[e.v()][e.u()] = true;
        }

        ArrayList<Integer> eulerianPath = getEulerianPath(adjacencyMatrix);

        switch (eliminationType) {
            case "FirstStartingNode": {
                return calculateNodeOrder(eulerianPath, eulerianPath.getFirst());
            }
            case "BestStartingNode": {
                //try all vertex ordering with different starting vertices
                return findByBestStartingNode(eulerianPath);
            }
            case "FastBestStartingNode": {
                ArrayList<Integer> shorterPath = new ArrayList<>();
                shorterPath.add(eulerianPath.getFirst());
                for (int i = 1; i < eulerianPath.size(); i++) {
                    if (i < eulerianPath.size() - 2) {
                        if (eulerianPath.get(i+1).equals(eulerianPath.get(i-1)) && eulerianPath.get(i+2).equals(eulerianPath.get(i))) {
                            //skip this node and the next one
                            i++;
                            continue;
                        }
                    }
                    shorterPath.add(eulerianPath.get(i));
                }
                return findByBestStartingNode(shorterPath);
            }
            default: {
                System.err.println("eliminationType has an unexpected value: " + eliminationType);
                return new int[n];
            }
        }
    }
}
