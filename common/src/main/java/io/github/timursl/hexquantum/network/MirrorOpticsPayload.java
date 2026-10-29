package io.github.timursl.hexquantum.network;

import io.github.timursl.hexquantum.HexQuantum;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: show the mirrored world for this many ticks (0 = stop).
 */
public record MirrorOpticsPayload(int ticks) implements CustomPacketPayload {
    public static final Type<MirrorOpticsPayload> TYPE = new Type<>(HexQuantum.id("mirror_optics"));
    public static final StreamCodec<ByteBuf, MirrorOpticsPayload> STREAM_CODEC =
        ByteBufCodecs.VAR_INT.map(MirrorOpticsPayload::new, MirrorOpticsPayload::ticks);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
