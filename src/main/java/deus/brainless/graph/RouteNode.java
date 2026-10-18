package deus.brainless.graph;

import org.jspecify.annotations.NonNull;

public class RouteNode<T extends GraphNode> implements Comparable<RouteNode<?>> {
    private T current;
    private T previous;
    private double routeScore;
    private double estimatedScore;

    public RouteNode(T current) {
        this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public RouteNode(T current, T previous, double routeScore, double estimatedScore) {
        this.current = current;
        this.previous = previous;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

	@Override
	public int compareTo(RouteNode other) {
		return Double.compare(this.estimatedScore, other.estimatedScore);
	}

	public double getRouteScore() {
		return routeScore;
	}

	public T getCurrent() {
		return current;
	}

	public T getPrevious() {
		return previous;
	}

	public void setPrevious(T node) {
		this.previous = node;
	}

	public void setCurrent(T node) {
		this.current = node;
	}

	public void setRouteScore(double score) {
		this.routeScore = score;
	}

	public void setEstimatedScore(double score) {
		this.estimatedScore = score;
	}
}
