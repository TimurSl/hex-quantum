package io.github.timursl.hexquantum.casting.actions;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import io.github.timursl.hexquantum.casting.iota.DimensionIota;
import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.platform.Services;

import java.util.List;

/**
 * Exit Anchor: push the dimension and position the caster last entered their pocket from (two nulls if never).
 * It only reads; it teleports nothing and stores nothing.
 */
public class OpExitAnchor extends HQAction {
    @Override
    protected int argc() {
        return 0;
    }

    @Override
    protected Result run(List<Iota> args, CastingEnvironment env, CastingImage image) {
        var player = Casters.player(env);
        PlayerQuantumData data = Services.PLATFORM.playerData(player);
        if (data.pocketEntryDimension() == null || data.pocketEntryPos() == null) {
            return Result.of(0).push(new NullIota(), new NullIota());
        }
        return Result.of(0).push(new DimensionIota(data.pocketEntryDimension()), new Vec3Iota(data.pocketEntryPos()));
    }
}
