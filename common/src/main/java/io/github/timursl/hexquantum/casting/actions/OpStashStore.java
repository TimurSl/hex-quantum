package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HQArgs;
import io.github.timursl.hexquantum.config.HQConfig;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Pocket Stash: put an item entity into the caster's stash. Whatever does not fit stays on the ground.
 */
public class OpStashStore extends HQAction {
    @Override
    protected int argc() {
        return 1;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        var item = HQArgs.itemEntity(args, 0, argc(), env);
        env.assertEntityInRange(item);
        return Result.of(HQConfig.STASH_STORE_COST.media())
            .particles(ParticleSpray.burst(item.position(), 0.5, 20))
            .effect(e -> {
                if (!item.isAlive()) {
                    return;
                }
                ItemStack rest = Services.PLATFORM.playerData(player).stash().addItem(item.getItem().copy());
                if (rest.isEmpty()) {
                    item.discard();
                } else {
                    item.setItem(rest);
                }
            });
    }
}
