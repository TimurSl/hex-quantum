package io.github.timursl.hexquantum.casting.iota;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.serialization.MapCodec;
import io.github.timursl.hexquantum.registry.HQIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reference to a dimension (pushed by Exit Anchor). Hex Casting has no dimension iota of its own.
 */
public class DimensionIota extends Iota {
    private final ResourceKey<Level> dimension;

    public DimensionIota(@NotNull ResourceKey<Level> dimension) {
        super(HQIotaTypes::dimension);
        this.dimension = dimension;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Override
    public boolean isTruthy(@Nullable CastingEnvironment env) {
        return true;
    }

    /**
     * Hex Casting builds before the "entity nulling rework" declare {@code isTruthy()} without an environment.
     */
    public boolean isTruthy() {
        return true;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return typesMatch(this, that) && that instanceof DimensionIota other && other.dimension.equals(this.dimension);
    }

    @Override
    public int hashCode() {
        return dimension.hashCode();
    }

    @Override
    public @NotNull Component display() {
        return Component.translatable("hexquantum.iota.dimension.display", dimension.location().toString())
            .withStyle(ChatFormatting.DARK_AQUA);
    }

    public static final IotaType<DimensionIota> TYPE = new IotaType<>() {
        private final MapCodec<DimensionIota> codec = ResourceKey.codec(Registries.DIMENSION)
            .xmap(DimensionIota::new, DimensionIota::getDimension)
            .fieldOf("dimension");
        private final StreamCodec<RegistryFriendlyByteBuf, DimensionIota> streamCodec =
            ResourceKey.streamCodec(Registries.DIMENSION).map(DimensionIota::new, DimensionIota::getDimension)
                .mapStream(b -> b);

        @Override
        public MapCodec<DimensionIota> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DimensionIota> streamCodec() {
            return streamCodec;
        }

        @Override
        public int color() {
            return 0xff_2ad3c8;
        }
    };
}
