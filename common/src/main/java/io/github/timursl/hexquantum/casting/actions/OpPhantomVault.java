package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.casting.mishaps.MishapQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.data.QuantumMenus;
import io.github.timursl.hexquantum.entity.PhantomItemEntity;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Phantom Vault: a personal storage for phantom items.
 * <ul>
 *     <li>OPEN: () -> (), look into the vault (view-only).</li>
 *     <li>STORE: [phantom item] -> (), put a phantom item entity into the vault (an empowered one loses its hex).</li>
 *     <li>RELEASE: [slot, position] -> phantom item, drop the phantom item in that slot (0-26) as a phantom again.</li>
 * </ul>
 */
public class OpPhantomVault extends HQAction {
    public enum Mode { OPEN, STORE, RELEASE }

    private final Mode mode;

    public OpPhantomVault(Mode mode) {
        this.mode = mode;
    }

    @Override
    protected int argc() {
        return switch (mode) {
            case OPEN -> 0;
            case STORE -> 1;
            case RELEASE -> 2;
        };
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        PlayerQuantumData data = Services.PLATFORM.playerData(player);
        switch (mode) {
            case OPEN -> {
                return Result.of(HQConfig.PHANTOM_STORAGE_OPEN_COST.media()).effect(e -> QuantumMenus.openPhantomVault(player));
            }
            case STORE -> {
                Entity entity = HQArgs.entity(args, 0, argc(), env);
                if (!(entity instanceof PhantomItemEntity phantom)) {
                    throw MishapBadEntity.of(entity, "hexquantum.phantom_item");
                }
                env.assertEntityInRange(phantom);
                if (!data.phantomVault().canAddItem(phantom.getItem())) {
                    throw new MishapQuantum("vault_full");
                }
                return Result.of(HQConfig.PHANTOM_STORAGE_STORE_COST.media())
                    .particles(ParticleSpray.burst(phantom.position(), 0.5, 15))
                    .effect(e -> {
                        if (phantom.isAlive()) {
                            ItemStack rest = data.phantomVault().addItem(phantom.getItem().copy());
                            if (rest.isEmpty()) {
                                phantom.discard();
                            } else {
                                phantom.setItem(rest);
                            }
                        }
                    });
            }
            default -> {
                int slot = HQArgs.nonNegativeInt(args, 0, argc(), PlayerQuantumData.STASH_SIZE);
                Vec3 pos = HQArgs.vector(args, 1, argc());
                env.assertVecInRange(pos);
                ItemStack stack = data.phantomVault().getItem(slot);
                if (stack.isEmpty()) {
                    throw new MishapQuantum("vault_slot_empty", slot);
                }
                PhantomItemEntity released = PhantomItemEntity.create(env.getWorld(), pos, stack, player.getUUID(), null);
                return Result.of(HQConfig.PHANTOM_STORAGE_RELEASE_COST.media())
                    .push(new EntityIota(released))
                    .particles(ParticleSpray.cloud(pos, 0.4, 15))
                    .effect(e -> {
                        ItemStack taken = data.phantomVault().removeItemNoUpdate(slot);
                        if (!taken.isEmpty()) {
                            released.setItem(taken);
                            env.getWorld().addFreshEntity(released);
                        }
                    });
            }
        }
    }
}
