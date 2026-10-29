package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.casting.mishaps.MishapQuantum;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.item.AmethystMedia;
import io.github.timursl.hexquantum.item.QuantumPhialItem;
import io.github.timursl.hexquantum.platform.Services;

import java.util.List;

/**
 * Phial Infusion: with the Quantum Phial in the other hand, dissolve an amethyst item entity into the phial.
 */
public class OpPhialCharge extends HQAction {
    @Override
    protected int argc() {
        return 1;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        var item = HQArgs.itemEntity(args, 0, argc(), env);
        env.assertEntityInRange(item);
        if (!QuantumPhialItem.isValidFor(player.getItemInHand(env.getOtherHand()), player)
            && !QuantumPhialItem.isValidFor(player.getOffhandItem(), player)) {
            throw new MishapQuantum("no_phial");
        }
        long media = AmethystMedia.mediaOf(item.getItem());
        if (media <= 0) {
            throw MishapBadItem.of(item, "hexquantum.amethyst");
        }
        return Result.of(HQConfig.PHIAL_CHARGE_COST.media())
            .particles(ParticleSpray.cloud(item.position(), 0.5, 30))
            .effect(e -> {
                if (!item.isAlive()) {
                    return;
                }
                // Re-read: the stack may have changed between the check and the side effect.
                long now = AmethystMedia.mediaOf(item.getItem());
                Services.PLATFORM.playerData(player).addQuantumMedia(now);
                item.discard();
            });
    }
}
