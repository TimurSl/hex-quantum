package io.github.timursl.hexquantum.phantom;

import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import io.github.timursl.hexquantum.casting.HexRunner;
import io.github.timursl.hexquantum.casting.HexStorage;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Runs an empowered phantom's stored hex as its creator when someone interacts with it. The interacting entity is
 * pushed onto the starting stack; ambit is centred on the phantom; media comes from the creator.
 */
public final class PhantomTriggers {
    private PhantomTriggers() {
    }

    public static boolean fire(ServerLevel level, Vec3 at, @Nullable UUID owner, @Nullable Tag hexTag, Entity interactor) {
        if (hexTag == null) {
            return false;
        }
        ServerPlayer creator = HexRunner.onlineOwner(level.getServer(), owner);
        if (creator == null) {
            return false;
        }
        List<Iota> hex = HexStorage.load(hexTag, level.registryAccess());
        return HexRunner.run(creator, level, at, hex, List.of(new EntityIota(interactor)));
    }
}
