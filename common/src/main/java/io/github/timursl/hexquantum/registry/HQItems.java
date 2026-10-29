package io.github.timursl.hexquantum.registry;

import io.github.timursl.hexquantum.item.QuantumPhialItem;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class HQItems {
    private static final HQRegistrar<Item> R = Services.PLATFORM.registrar(Registries.ITEM);

    public static final Holder<Item> QUANTUM_PHIAL = R.register("quantum_phial",
        () -> new QuantumPhialItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC)));

    public static final Holder<Item> PORTAL_DOOR = R.register("portal_door",
        () -> new BlockItem(HQBlocks.PORTAL_DOOR.value(), new Item.Properties().rarity(Rarity.RARE)));

    private HQItems() {
    }

    public static void init() {
    }
}
