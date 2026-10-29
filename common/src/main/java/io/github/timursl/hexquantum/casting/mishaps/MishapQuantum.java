package io.github.timursl.hexquantum.casting.mishaps;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.TreeList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

/**
 * Generic HexQuantum mishap: leaves the stack alone and prints {@code hexcasting.mishap.hexquantum.<key>}.
 */
public class MishapQuantum extends Mishap {
    private final String key;
    private final Object[] args;

    public MishapQuantum(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    @Override
    public FrozenPigment accentColor(CastingEnvironment env, Context errorCtx) {
        return dyeColor(DyeColor.PURPLE);
    }

    @Override
    public TreeList<Iota> execute(CastingEnvironment env, Context errorCtx, TreeList<Iota> stack) {
        return stack;
    }

    @Override
    protected Component errorMessage(CastingEnvironment env, Context errorCtx) {
        return error("hexquantum." + key, args);
    }
}
