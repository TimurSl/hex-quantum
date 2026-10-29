package io.github.timursl.hexquantum.network;

import io.github.timursl.hexquantum.HexQuantum;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: offset the camera (and picking) by (x, y, z) for this many ticks (0 = stop).
 */
public record ShiftedOpticsPayload(double x, double y, double z, int ticks) implements CustomPacketPayload {
    public static final Type<ShiftedOpticsPayload> TYPE = new Type<>(HexQuantum.id("shifted_optics"));
    public static final StreamCodec<ByteBuf, ShiftedOpticsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, ShiftedOpticsPayload::x,
        ByteBufCodecs.DOUBLE, ShiftedOpticsPayload::y,
        ByteBufCodecs.DOUBLE, ShiftedOpticsPayload::z,
        ByteBufCodecs.VAR_INT, ShiftedOpticsPayload::ticks,
        ShiftedOpticsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
