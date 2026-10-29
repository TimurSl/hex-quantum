package io.github.timursl.hexquantum.network;

import io.github.timursl.hexquantum.HexQuantum;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: the local player is (ticks &gt; 0) or is no longer (0) in Quantum Shift.
 */
public record QuantumShiftPayload(int ticks) implements CustomPacketPayload {
    public static final Type<QuantumShiftPayload> TYPE = new Type<>(HexQuantum.id("quantum_shift"));
    public static final StreamCodec<ByteBuf, QuantumShiftPayload> STREAM_CODEC =
        ByteBufCodecs.VAR_INT.map(QuantumShiftPayload::new, QuantumShiftPayload::ticks);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
