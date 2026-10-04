package deus.brainless.agent;


import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;

public class MixBuilder<C> {

	private final Node node;
	private final AgentTemplate agentTemplate;

	public MixBuilder(Node node, AgentTemplate agentTemplate) {
		this.node = node;
		this.agentTemplate = agentTemplate;
	}

	public MixBuilder<C> add(String input, double weight) {
		return connect(input, Connection.ADD, weight);
	}

	public MixBuilder<C> sub(String input, double weight) {
		return connect(input, Connection.SUB, weight);
	}

	public MixBuilder<C> mul(String input, double weight) {
		return connect(input, Connection.MUL, weight);
	}

	public MixBuilder<C> div(String input, double weight) {
		return connect(input, Connection.DIV, weight);
	}

	public MixBuilder<C> max(String input, double weight) {
		return connect(input, Connection.MAX, weight);
	}

	public MixBuilder<C> min(String input, double weight) {
		return connect(input, Connection.MIN, weight);
	}

	public MixBuilder<C> op(String input, BiFunction<Double, Double, Double> op, double weight) {
		return connect(input, op, weight);
	}

	private MixBuilder<C> connect(String inputName, BiFunction<Double, Double, Double> op, double weight) {
		Node inputNode = agentTemplate.getNode(inputName);
		inputNode.connect(node, op, weight);
		return this;
	}

	public MixBuilder<C> sigmoid(double factor) {
		node.sigmoid(factor);
		return this;
	}

	public MixBuilder<C> canceledBy(String inhibitor, double threshold) {
		Node inh = agentTemplate.getNode(inhibitor);
		node.canceledBy(threshold, 1.0, inh);
		return this;
	}

	public MixBuilder<C> debug(String label) {
		node.onCompute(v -> System.out.printf("  [%s] = %.4f%n", label, v));
		return this;
	}

	@SuppressWarnings("unchecked")
	public MixBuilder<C> onRange(double min, double max, Action<C> action) {
		node.output(min, max, (ctx, v) -> action.run((C) ctx, v));
		return this;
	}

	public MixBuilder<C> onHigh(Action<C> action)   { return onRange(0.7, 1.0, action); }
	public MixBuilder<C> onMedium(Action<C> action) { return onRange(0.3, 0.7, action); }
	public MixBuilder<C> onLow(Action<C> action)    { return onRange(0.0, 0.3, action); }

	public MixBuilder<C> above(double t, Action<C> action) { return onRange(t, 1.01, action); }
	public MixBuilder<C> below(double t, Action<C> action) { return onRange(0.0, t, action); }

	public MixBuilder<C> onExact(double value, double epsilon, Action<C> action) {
		return onRange(value - epsilon, value + epsilon, action);
	}

	public Node node() { return node; }
}
