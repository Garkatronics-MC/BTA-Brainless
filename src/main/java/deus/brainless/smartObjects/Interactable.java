package deus.brainless.smartObjects;

import deus.brainless.goap.GoapAction;

import java.util.Set;

/**
 * Defines a thing that provides a set of actions that can work with it.
 * Meant to be used with {@link deus.brainless.goap.GoapPlanner}
 */
public interface Interactable {
	Set<GoapAction> actions();
}
