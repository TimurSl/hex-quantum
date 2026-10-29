package io.github.timursl.hexquantum.data;

import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Opens the per-player containers with the vanilla 9x3 chest screen (no custom menu type needed).
 */
public final class QuantumMenus {
    private QuantumMenus() {
    }

    public static void openStash(ServerPlayer player) {
        Container stash = Services.PLATFORM.playerData(player).stash();
        player.openMenu(new SimpleMenuProvider(
            (id, inventory, p) -> ChestMenu.threeRows(id, inventory, stash),
            Component.translatable("container.hexquantum.pocket_stash")));
    }

    public static void openPhantomVault(ServerPlayer player) {
        Container vault = Services.PLATFORM.playerData(player).phantomVault();
        player.openMenu(new SimpleMenuProvider(
            (id, inventory, p) -> new ViewOnlyChestMenu(id, inventory, vault),
            Component.translatable("container.hexquantum.phantom_vault")));
    }

    /**
     * Phantom items must never leave the vault as real items, so the vault slots can be looked at but not touched.
     * Items come out only through Phantom Vault Release.
     */
    public static class ViewOnlyChestMenu extends ChestMenu {
        private final int vaultSlots;

        public ViewOnlyChestMenu(int id, Inventory inventory, Container vault) {
            super(MenuType.GENERIC_9x3, id, inventory, vault, 3);
            this.vaultSlots = vault.getContainerSize();
        }

        @Override
        public void clicked(int slotId, int button, ClickType clickType, Player player) {
            if (slotId >= 0 && slotId < vaultSlots) {
                return;
            }
            if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.PICKUP_ALL || clickType == ClickType.SWAP) {
                return;
            }
            super.clicked(slotId, button, clickType, player);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }
    }
}
