package io.github.timursl.hexquantum.registry;

import net.minecraft.core.Holder;

import java.util.function.Supplier;

/**
 * Loader-independent view of a deferred registry.
 */
public interface HQRegistrar<T> {
    <I extends T> Holder<T> register(String name, Supplier<I> factory);
}
