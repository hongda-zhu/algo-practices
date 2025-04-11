package supermarket.domain.algorithm;

import supermarket.tuples.AlgorithmType;
import supermarket.tuples.AlgorithmParameter;
import java.util.*;

/**
 * SimulatedAnnealing is an implementation of the Simulated Annealing optimization algorithm.
 * This class is used to find an optimal arrangement of elements that minimizes the cost function
 * based on the input distance matrix. The algorithm uses a stochastic process to escape local optima
 * by occasionally accepting less optimal solutions based on a probability that decreases over time.
 *
 * @author Eric Medina León
 */
public class SimulatedAnnealingAlgorithm extends AbstractAlgorithm {

    /** Initial temperature for the simulated annealing process. */
    private static double INITIAL_TEMPERATURE = 1000;
    /** Number of iterations per temperature level. */
    private static double K = 600.0;
    /** Rate at which the temperature decreases after each iteration. */
    private static double COOLING_RATE = 0.9;

    private final Random random;

    /**
     * Constructor for SimulatedAnnealing.
     * Initializes the algorithm with a given distance matrix.
     *
     * @param distanceMatrix The matrix representing the distances or costs between pairs of elements.
     */
    public SimulatedAnnealingAlgorithm(float[][] distanceMatrix) {
        super(distanceMatrix);
        this.random = new Random(1);
    }

    /**
     * Returns the type of algorithm.
     *
     * @return AlgorithmType.SIMULATED_ANNEALING representing this algorithm's type.
     */
    public AlgorithmType getType() {
        return AlgorithmType.SIMULATED_ANNEALING;
    }

    /**
     * Checks if the distanceMatrix data is a valid input for the implemented algorithm.
     *
     * @return If the implemented algorithm can be used with the given distanceMatrix data.
     */
    public boolean canUseAlgorithm() {
       return super.canUseAlgorithm();
    }

    /**
     * Gives all available parameters for the algorithm.
     *
     * @return List of algorithm parameters, which contain name, description and list of possible values.
     */
    public ArrayList<AlgorithmParameter> getAvailableParameters() {
        ArrayList<AlgorithmParameter> params = super.getAvailableParameters();
        params.add(new AlgorithmParameter(
                "Initial Temperature",
                """
                        The temperature controls the algorithm ability to accept worse solutions during optimization. It starts high to allow the algorithm to explore the solution space broadly, including less optimal solutions, avoiding being trapped in local minima. As the temperature decreases, the algorithm becomes more selective, favoring better solutions.
                        - High initial temperature: Encourages extensive exploration of the solution space, but may require more iterations to converge.
                        - Low initial temperature: Limits exploration but offers a faster execution.
                        Default value: 1000. Recommended range: [500, 2000]""",
                "double"
        ));
        params.add(new AlgorithmParameter(
                "Cooling Rate",
                """
                        The cooling rate (a value between 0 and 1) determines how quickly the temperature decreases at each iteration, typically with a formula like Temperature = Temperature * Cooling_Rate.\s
                        - Slow cooling (close to 1): Allows more exhaustive exploration of the solution space, improving the probability of finding a global optimum, but increases computational time.
                        - Fast cooling (significantly less than 1): Reduces computation time but risks missing the global optimum due to insufficient exploration.
                        Default value: 0.9. Recommended range: [0.80, 0.99]""",
                "double"
        ));
        params.add(new AlgorithmParameter(
                "K",
                """
                        Represents the number of iterations per temperature level. Determines how many candidate solutions are evaluated at each temperature level before reducing the temperature. This parameter directly controls the depth of exploration at a given temperature.
                        - High K: Allows the algorithm to exhaustively explore the solution space, increases the probability of escaping local minima but significantly increases computation time.
                        - Low K: Reduces the number of iterations at each temperature level, increases the risk of getting stuck in local minima but offers a quicker execution.
                        Default value: 600. Recommended range: [200, 1000]""",
                "double"
        ));
        return params;
    }

    /**
     * Sets all algorithm parameters to the specified value.
     *
     * @param parameters Array of strings corresponding to the new values (or null) for each algorithm parameter.
     */
    public void setParameters(List<String> parameters) {
        super.setParameters(parameters);
        String param;
        for (int i = 0; i < 3; i++) {
            param = parameters.removeFirst();
            String paramName;
            if (i == 0) {
                paramName = "Initial Temperature";
                INITIAL_TEMPERATURE = 1000;
            }
            else if (i == 1) {
                COOLING_RATE = 0.9;
                paramName = "Cooling Rate";
            }
            else {
                K = 600;
                paramName = "K";
            }
            try {
                double value = Double.parseDouble(param);
                if (i == 0) {
                    if (value < 10) {
                        System.err.println("Received invalid value for " + paramName + ". Using default value instead.");
                    }
                    else INITIAL_TEMPERATURE = value;
                }
                else if (i == 1) {
                    if (value <= 0 || value >= 1) {
                        System.err.println("Received invalid value for " + paramName + ". Using default value instead.");
                    }
                    else COOLING_RATE = value;
                }
                else {
                    if (value <= 0) {
                        System.err.println("Received invalid value for " + paramName + ". Using default value instead.");
                    }
                    else K = value;
                }
            } catch (NumberFormatException e) {
                System.err.println("Received non-numeric value for " + paramName + ". Using default value instead.");
            }
        }
    }

    /**
     * Generates an initial solution by creating a randomized list of element indices.
     *
     * @return A randomized ArrayList representing the initial solution.
     */
    public ArrayList<Integer> generateInitialSolution() {
        ArrayList<Integer> solution = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            solution.add(i);
        }
        Collections.shuffle(solution, random); // Randomize initial distribution order
        return solution;
    }

    /**
     * Generates a random neighboring solution by swapping two elements in the current solution.
     *
     * @param solution The current solution represented as an int array.
     * @return A new int array representing a neighboring solution.
     */
    private int[] generateRandomNeighbor(int[] solution) {
        int[] neighbor = solution.clone();
        int pos1 = random.nextInt(neighbor.length);
        int pos2 = random.nextInt(neighbor.length);
        // Swap two elements in the solution
        int temp = neighbor[pos1];
        neighbor[pos1] = neighbor[pos2];
        neighbor[pos2] = temp;
        return neighbor;
    }

    /**
     * Calculates the acceptance probability of a new solution based on the change in cost and current temperature.
     *
     * @param deltaCost The difference in cost between the new solution and the current solution.
     * @param temperature The current temperature in the simulated annealing process.
     * @return The acceptance probability of the new solution.
     */
    private double acceptanceProbability(double deltaCost, double temperature) {
        return Math.min(1.0, Math.exp(-deltaCost / (K*temperature)));
    }

    /**
     * Executes the Simulated Annealing algorithm to find an optimal distribution of elements.
     * The process begins with a random solution and iteratively attempts to find better solutions.
     *
     * @return An int array representing the optimal distribution of elements found by the algorithm.
     */
    public int[] calculateDistribution() {
        ArrayList<Integer> initialSolutionList = generateInitialSolution();
        int[] currentSolution = initialSolutionList.stream().mapToInt(i -> i).toArray();
        double currentCost = calculateCycleCost(currentSolution);

        int[] bestSolution = currentSolution.clone();
        double bestCost = currentCost;

        double temperature = INITIAL_TEMPERATURE;
        while (temperature > 1.0) {
            for (int i = 0; i < K; i++) {
                int[] newSolution = generateRandomNeighbor(currentSolution);
                double newCost = calculateCycleCost(newSolution);
                double deltaCost = newCost - currentCost;

                if (deltaCost < 0 || acceptanceProbability(deltaCost, temperature) > random.nextDouble()) {
                    currentSolution = newSolution;
                    currentCost = newCost;

                    if (currentCost < bestCost) {
                        bestSolution = currentSolution.clone();
                        bestCost = currentCost;
                    }
                }
            }
            temperature *= COOLING_RATE;
        }
        return bestSolution;
    }
}
