package deus.brainless.goap.arbitration;

import deus.brainless.drive.Drive;

/**
 * @param minPriority proposals below this are not worth acting on at all
 * @param fallback what is wanted when nothing else is — the default activity,
 *                not an error handler: it is returned from the very first tick
 */
public record ArbiterConfig(double minPriority, Drive fallback) {}