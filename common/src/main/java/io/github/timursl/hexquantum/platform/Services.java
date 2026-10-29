package io.github.timursl.hexquantum.platform;

import java.util.ServiceLoader;

/**
 * Looks up the loader-specific implementations of the platform interfaces via {@link ServiceLoader}.
 */
public final class Services {
    public static final IPlatform PLATFORM = load(IPlatform.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Services.class.getClassLoader())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No platform implementation found for " + clazz.getName()));
    }
}
