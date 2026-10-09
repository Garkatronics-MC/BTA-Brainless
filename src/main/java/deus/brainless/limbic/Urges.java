package deus.brainless.limbic;


public final class Urges {
	private final double[] values;

	public Urges copy() {
		Urges s = new Urges(values.length);
		System.arraycopy(values, 0, s.values, 0, values.length);
		return s;
	}
	public Urges(int size) { values = new double[size]; }
	public double get(int index)            { return values[index]; }
	void set(int index, double value)       { values[index] = value; }
}
