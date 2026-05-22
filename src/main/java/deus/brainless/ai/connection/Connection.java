package deus.brainless.ai.connection;

import java.util.function.BiFunction;

public class Connection {
    public final Node from;
    public final Node to;
    public double weight;
    private final BiFunction<Double, Double, Double> op;

    public Connection(Node from, Node to, BiFunction<Double, Double, Double> op, double weight) {
        this.from = from;
        this.to = to;
        this.op = op;
        this.weight = weight;
    }

    public double apply(double accumulated, double incoming) {
        return op.apply(accumulated, incoming * weight);
    }

    // Built-in ops
    public static BiFunction<Double, Double, Double> ADD = (a, b) -> a + b;
    public static BiFunction<Double, Double, Double> SUB = (a, b) -> a - b;
    public static BiFunction<Double, Double, Double> MUL = (a, b) -> a * b;
    public static BiFunction<Double, Double, Double> DIV = (a, b) -> b == 0 ? 0 : a / b;
    public static BiFunction<Double, Double, Double> MAX = (a, b) -> Math.max(a, b);
    public static BiFunction<Double, Double, Double> MIN = (a, b) -> Math.min(a, b);
}
