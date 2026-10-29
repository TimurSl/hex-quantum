package io.github.timursl.hexquantum.item;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Media value of the amethyst forms the Quantum Phial accepts (configurable, see {@code amethystMediaValues}).
 */
public final class AmethystMedia {
    private static List<String> parsedFrom;
    private static Map<Item, Long> values = Map.of();

    private AmethystMedia() {
    }

    /**
     * Total media of the stack, or 0 if it is not an accepted amethyst form.
     */
    public static long mediaOf(ItemStack stack) {
        Long each = table().get(stack.getItem());
        return each == null ? 0 : each * stack.getCount();
    }

    private static synchronized Map<Item, Long> table() {
        List<String> config = HQConfig.AMETHYST_MEDIA_VALUES.get();
        if (config != parsedFrom) {
            Map<Item, Long> parsed = new HashMap<>();
            for (String line : config) {
                int eq = line.indexOf('=');
                ResourceLocation id = eq > 0 ? ResourceLocation.tryParse(line.substring(0, eq).trim()) : null;
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                    HexQuantum.LOGGER.warn("Ignoring invalid amethystMediaValues entry '{}'", line);
                    continue;
                }
                try {
                    parsed.put(BuiltInRegistries.ITEM.get(id), HQConfig.dust(Double.parseDouble(line.substring(eq + 1).trim())));
                } catch (NumberFormatException e) {
                    HexQuantum.LOGGER.warn("Ignoring invalid amethystMediaValues entry '{}'", line);
                }
            }
            values = parsed;
            parsedFrom = config;
        }
        return values;
    }
}
