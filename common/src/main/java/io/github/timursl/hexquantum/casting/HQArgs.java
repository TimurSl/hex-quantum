package io.github.timursl.hexquantum.casting;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapEntityNotFound;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Argument readers. Indices count from the bottom of the popped arguments (0 = deepest), like Hex Casting's own
 * helpers, and every bad argument becomes the matching mishap.
 */
public final class HQArgs {
    private HQArgs() {
    }

    private static int rev(int idx, int argc) {
        return argc - (idx + 1);
    }

    public static Iota at(List<Iota> args, int idx) {
        if (idx >= args.size()) {
            throw new MishapNotEnoughArgs(idx + 1, args.size());
        }
        return args.get(idx);
    }

    public static double number(List<Iota> args, int idx, int argc) {
        Iota x = at(args, idx);
        if (x instanceof DoubleIota d) {
            return d.getDouble();
        }
        throw MishapInvalidIota.ofType(x, rev(idx, argc), "double");
    }

    public static double numberBetween(List<Iota> args, int idx, int argc, double min, double max) {
        double v = number(args, idx, argc);
        if (!(v >= min && v <= max)) {
            throw MishapInvalidIota.of(at(args, idx), rev(idx, argc), "double.between", min, max);
        }
        return v;
    }

    /**
     * A duration in seconds, strictly positive and at most {@code max}.
     */
    public static double seconds(List<Iota> args, int idx, int argc, double max) {
        double v = number(args, idx, argc);
        if (!(v > 0 && v <= max)) {
            throw MishapInvalidIota.of(at(args, idx), rev(idx, argc), "hexquantum.seconds", max);
        }
        return v;
    }

    public static int nonNegativeInt(List<Iota> args, int idx, int argc, int maxExclusive) {
        Iota x = at(args, idx);
        double v = number(args, idx, argc);
        int rounded = (int) Math.round(v);
        if (Math.abs(v - rounded) > DoubleIota.TOLERANCE || rounded < 0 || rounded >= maxExclusive) {
            throw MishapInvalidIota.of(x, rev(idx, argc), "int.between", 0, maxExclusive - 1);
        }
        return rounded;
    }

    public static int positiveInt(List<Iota> args, int idx, int argc, int max) {
        Iota x = at(args, idx);
        double v = number(args, idx, argc);
        int rounded = (int) Math.round(v);
        if (Math.abs(v - rounded) > DoubleIota.TOLERANCE || rounded < 1 || rounded > max) {
            throw MishapInvalidIota.of(x, rev(idx, argc), "int.between", 1, max);
        }
        return rounded;
    }

    public static Vec3 vector(List<Iota> args, int idx, int argc) {
        Iota x = at(args, idx);
        if (x instanceof Vec3Iota v) {
            return v.getVec3();
        }
        throw MishapInvalidIota.ofType(x, rev(idx, argc), "vector");
    }

    /**
     * A list iota used as a hex (it is only checked for being a list; Hex Casting validates the contents on eval).
     */
    public static List<Iota> hex(List<Iota> args, int idx, int argc) {
        Iota x = at(args, idx);
        if (x instanceof ListIota list) {
            return new ArrayList<>(list.getList());
        }
        throw MishapInvalidIota.of(x, rev(idx, argc), "hexquantum.hex");
    }

    public static Entity entity(List<Iota> args, int idx, int argc, CastingEnvironment env) {
        Iota x = at(args, idx);
        if (x instanceof EntityIota e) {
            Entity entity = e.getEntity(env.getWorld());
            if (entity == null) {
                throw new MishapEntityNotFound(e.getEntityId(), e.getEntityName());
            }
            return entity;
        }
        throw MishapInvalidIota.ofType(x, rev(idx, argc), "entity");
    }

    public static ItemEntity itemEntity(List<Iota> args, int idx, int argc, CastingEnvironment env) {
        Entity e = entity(args, idx, argc, env);
        if (e instanceof ItemEntity item) {
            return item;
        }
        throw MishapInvalidIota.ofType(at(args, idx), rev(idx, argc), "entity.item");
    }

    public static Mob mob(List<Iota> args, int idx, int argc, CastingEnvironment env) {
        Entity e = entity(args, idx, argc, env);
        if (e instanceof Mob mob) {
            return mob;
        }
        throw MishapInvalidIota.ofType(at(args, idx), rev(idx, argc), "entity.mob");
    }

    public static ServerPlayer player(List<Iota> args, int idx, int argc, CastingEnvironment env) {
        Entity e = entity(args, idx, argc, env);
        if (e instanceof ServerPlayer player) {
            return player;
        }
        throw MishapInvalidIota.ofType(at(args, idx), rev(idx, argc), "entity.player");
    }

    /**
     * A list made only of living entities that currently exist.
     */
    public static List<LivingEntity> livingList(List<Iota> args, int idx, int argc, CastingEnvironment env) {
        Iota x = at(args, idx);
        if (!(x instanceof ListIota list)) {
            throw MishapInvalidIota.of(x, rev(idx, argc), "hexquantum.entity_list");
        }
        List<LivingEntity> out = new ArrayList<>();
        for (Iota element : list.getList()) {
            if (!(element instanceof EntityIota e)) {
                throw MishapInvalidIota.of(x, rev(idx, argc), "hexquantum.entity_list");
            }
            Entity entity = e.getEntity(env.getWorld());
            if (entity == null) {
                throw new MishapEntityNotFound(e.getEntityId(), e.getEntityName());
            }
            if (!(entity instanceof LivingEntity living)) {
                throw MishapInvalidIota.of(x, rev(idx, argc), "hexquantum.entity_list");
            }
            if (!out.contains(living)) {
                out.add(living);
            }
        }
        return out;
    }
}
