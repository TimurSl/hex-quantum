package io.github.timursl.hexquantum.neoforge;

import io.github.timursl.hexquantum.config.HQConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Builds config/hexquantum-server.toml (per world, synced to clients) from the loader-independent
 * {@link HQConfig} entries.
 */
public final class NeoForgeConfig {
    private NeoForgeConfig() {
    }

    @SuppressWarnings("unchecked")
    public static ModConfigSpec build() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Media costs, in amethyst dust (1 dust = 10000 media units).").push("costs");
        for (HQConfig.Entry<?> entry : HQConfig.ALL) {
            if (entry instanceof HQConfig.Cost cost) {
                ModConfigSpec.DoubleValue value = builder.comment(cost.comment)
                    .defineInRange(cost.key, cost.defaultValue, cost.min, cost.max);
                cost.bind(value::get);
            }
        }
        builder.pop();

        builder.comment("Durations, ranges and other limits.").push("limits");
        for (HQConfig.Entry<?> entry : HQConfig.ALL) {
            if (entry instanceof HQConfig.Cost) {
                continue;
            }
            if (entry instanceof HQConfig.DoubleEntry d) {
                ModConfigSpec.DoubleValue value = builder.comment(d.comment).defineInRange(d.key, d.defaultValue, d.min, d.max);
                d.bind(value::get);
            } else if (entry instanceof HQConfig.IntEntry i) {
                ModConfigSpec.IntValue value = builder.comment(i.comment).defineInRange(i.key, i.defaultValue, i.min, i.max);
                i.bind(value::get);
            } else if (entry instanceof HQConfig.StringListEntry s) {
                ModConfigSpec.ConfigValue<List<? extends String>> value = builder.comment(s.comment)
                    .defineListAllowEmpty(s.key, s.defaultValue, () -> "", o -> o instanceof String);
                s.bind(() -> (List<String>) value.get());
            }
        }
        builder.pop();

        return builder.build();
    }
}
