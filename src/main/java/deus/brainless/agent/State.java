package deus.brainless.agent;


public final class State {
	private final double[] values;

	public State copy() {
		State s = new State(values.length);
		System.arraycopy(values, 0, s.values, 0, values.length);
		return s;
	}
	public State(int size) { values = new double[size]; }
	public double get(int index)            { return values[index]; }
	void set(int index, double value)       { values[index] = value; }
}
