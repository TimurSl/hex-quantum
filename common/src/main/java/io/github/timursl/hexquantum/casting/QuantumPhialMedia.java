package io.github.timursl.hexquantum.casting;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironmentComponent;
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.item.QuantumPhialItem;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.server.level.ServerPlayer;

/**
 * Pays casting costs from the Quantum Phial whenever its owner casts with a valid phial anywhere in their inventory.
 * Runs before Hex Casting's own media search, so the phial is used before amethyst items and before overcasting.
 */
public final class QuantumPhialMedia implements CastingEnvironmentComponent.ExtractMedia.Pre {
    public static final CastingEnvironmentComponent.Key<QuantumPhialMedia> KEY = new CastingEnvironmentComponent.Key<>() {
    };

    private final ServerPlayer player;

    private QuantumPhialMedia(ServerPlayer player) {
        this.player = player;
    }

    public static void init() {
        CastingEnvironment.addCreateEventListener((env, userData) -> {
            if (env instanceof PlayerBasedCastEnv && env.getCastingEntity() instanceof ServerPlayer player
                && !Services.PLATFORM.isFakePlayer(player)) {
                env.addExtension(new QuantumPhialMedia(player));
            }
        });
    }

    @Override
    public CastingEnvironmentComponent.Key<?> getKey() {
        return KEY;
    }

    @Override
    public long onExtractMedia(long cost, boolean simulate) {
        if (cost <= 0 || player.isCreative() || !QuantumPhialItem.hasValidPhial(player)) {
            return cost;
        }
        PlayerQuantumData data = Services.PLATFORM.playerData(player);
        long taken = Math.min(cost, data.quantumMedia());
        if (!simulate) {
            data.setQuantumMedia(data.quantumMedia() - taken);
        }
        return cost - taken;
    }
}
