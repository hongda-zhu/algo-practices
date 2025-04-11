package supermarket;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Set;

public class TestRunnerDomain {
    static void printTestSuccess(boolean success) {
        if (success)
            System.out.println("\u001B[32m" + "Test successful" + "\u001B[0m");
        else
            System.out.println("\u001B[31m" + "Test failed" + "\u001B[0m");
    }
    public static void main(String[] args) {
        Reflections reflections = new Reflections("supermarket.tests", new SubTypesScanner(false));
        Set<Class<?>> testClasses = reflections.getSubTypesOf(Object.class);

        for (Class<?> testClass : testClasses) {
            Result result = JUnitCore.runClasses(testClass);

            System.out.println("Running tests for: " + testClass.getName());

            for (Failure failure : result.getFailures()) {
                System.out.println(failure.toString());
            }
            printTestSuccess(result.wasSuccessful());
            System.out.println();
        }
    }
}
