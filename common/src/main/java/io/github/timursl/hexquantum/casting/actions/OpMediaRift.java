package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.item.QuantumPhialItem;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Open Media Rift (great spell): gives the caster a Quantum Phial bound to them. Any older phial stops working; the
 * stored media is kept because it lives in the player, not the item.
 */
public class OpMediaRift extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        return Result.of(HQConfig.MEDIA_RIFT_COST.media())
            .particles(ParticleSpray.burst(player.position().add(0, 1, 0), 2.0, 80))
            .effect(e -> {
                UUID id = UUID.randomUUID();
                Services.PLATFORM.playerData(player).setPhialId(id);
                ItemStack phial = QuantumPhialItem.create(player, id);
                if (!player.getInventory().add(phial)) {
                    player.drop(phial, false);
                }
            });
    }
}
