package io.github.timursl.hexquantum.item;

import io.github.timursl.hexquantum.platform.Services;
import io.github.timursl.hexquantum.registry.HQItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The Quantum Phial (a.k.a. Rift Phial). The item is only a bound key: the media lives in the owner's player data,
 * so the phial has infinite capacity and losing it loses nothing.
 */
public class QuantumPhialItem extends Item {
    private static final String OWNER = "hexquantum_owner";
    private static final String OWNER_NAME = "hexquantum_owner_name";
    private static final String PHIAL_ID = "hexquantum_phial_id";

    public QuantumPhialItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(ServerPlayer owner, UUID phialId) {
        ItemStack stack = new ItemStack(HQItems.QUANTUM_PHIAL.value());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putUUID(OWNER, owner.getUUID());
            tag.putString(OWNER_NAME, owner.getGameProfile().getName());
            tag.putUUID(PHIAL_ID, phialId);
        });
        return stack;
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    @Nullable
    public static UUID owner(ItemStack stack) {
        CompoundTag tag = data(stack);
        return tag.hasUUID(OWNER) ? tag.getUUID(OWNER) : null;
    }

    /**
     * A phial works only for its owner, and only if it is the most recent phial made for them.
     */
    public static boolean isValidFor(ItemStack stack, Player player) {
        if (!stack.is(HQItems.QUANTUM_PHIAL.value())) {
            return false;
        }
        CompoundTag tag = data(stack);
        if (!tag.hasUUID(OWNER) || !tag.hasUUID(PHIAL_ID) || !tag.getUUID(OWNER).equals(player.getUUID())) {
            return false;
        }
        UUID current = Services.PLATFORM.playerData(player).phialId();
        return tag.getUUID(PHIAL_ID).equals(current);
    }

    public static boolean hasValidPhial(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (isValidFor(inventory.getItem(i), player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = data(stack);
        if (tag.contains(OWNER_NAME)) {
            tooltip.add(Component.translatable("item.hexquantum.quantum_phial.bound", tag.getString(OWNER_NAME))
                .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.hexquantum.quantum_phial.unbound").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.hexquantum.quantum_phial.desc").withStyle(ChatFormatting.GRAY));
    }
}
