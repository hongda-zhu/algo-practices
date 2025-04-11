package supermarket.domain.algorithm;

import java.util.ArrayList;

/**
 * Utility class for graph operations.
 * @author Pau Martí Bisoca
 */
public class GraphUtils {
    record WeightedEdge(int u, int v, float weight) {
    }

    /**
     * Coverts a weighted directed graph definition from list of edges to adjacency matrix
     * @param edges list of edges that define the graph
     * @param n dimension of result matrix
     * @return the same graph as the input, but defined as a directed adjacency matrix
     */
    static float[][] edgeListToMatrix(ArrayList<WeightedEdge> edges, int n) {
        float[][] matrix = new float[n][n];
        for (WeightedEdge e : edges) {
            matrix[e.u][e.v] = e.weight;
        }
        return matrix;
    }

    /**
     * Converts a weighted directed graph definition from an adjacency matrix to a list of edges
     * @param matrix adjacency matrix that defines the graph
     * @return the same graph as the input, but defined as a list of weighted edges
     */
    static ArrayList<WeightedEdge> matrixToEdgeList(float[][] matrix) {
        ArrayList<WeightedEdge> edges = new ArrayList<>();
        int n = matrix.length;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < matrix[i].length; ++j) {
                if (i == j) continue;
                edges.add(new WeightedEdge(i, j, matrix[i][j]));
            }
        }
        return edges;
    }

    /**
     * Inverts (x:=1/x) all values of the given matrix. Zero will be "inverted" to a considerably large number.
     * @param matrix    Matrix to invert all values.
     * @return          The same matrix, after inverting all values (same instance).
     */
    public static float[][] invertMatrixValues(float[][] matrix) {
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[i].length; ++j) {
                if (matrix[i][j] != 0) matrix[i][j] = 1/matrix[i][j];
                else matrix[i][j] = 1e9f; //"infinity"
            }
        }
        return matrix;
    }
}
