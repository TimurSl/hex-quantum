package io.github.timursl.hexquantum.neoforge;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.data.PlayerQuantumData;
import io.github.timursl.hexquantum.platform.IPlatform;
import io.github.timursl.hexquantum.registry.HQRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * NeoForge implementation of the platform services (found through META-INF/services).
 */
public class NeoForgePlatform implements IPlatform {
    private static final List<DeferredRegister<?>> REGISTERS = new ArrayList<>();

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = track(
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, HexQuantum.MOD_ID));

    public static final Supplier<AttachmentType<PlayerQuantumData>> PLAYER_DATA = ATTACHMENTS.register("player_data",
        () -> AttachmentType.builder(() -> new PlayerQuantumData())
            .serialize(new IAttachmentSerializer<CompoundTag, PlayerQuantumData>() {
                @Override
                public PlayerQuantumData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    PlayerQuantumData data = new PlayerQuantumData();
                    data.load(tag, provider);
                    return data;
                }

                @Override
                public CompoundTag write(PlayerQuantumData attachment, HolderLookup.Provider provider) {
                    return attachment.save(provider);
                }
            })
            .copyOnDeath()
            .build());

    private static <T> DeferredRegister<T> track(DeferredRegister<T> register) {
        REGISTERS.add(register);
        return register;
    }

    /**
     * Hook every deferred register created so far (common registries included) up to the mod bus.
     */
    static void registerAll(IEventBus modBus) {
        for (DeferredRegister<?> register : REGISTERS) {
            register.register(modBus);
        }
    }

    @Override
    public <T> HQRegistrar<T> registrar(ResourceKey<? extends Registry<T>> registry) {
        DeferredRegister<T> register = track(DeferredRegister.create(registry, HexQuantum.MOD_ID));
        return new HQRegistrar<>() {
            @Override
            public <I extends T> Holder<T> register(String name, Supplier<I> factory) {
                return register.register(name, factory);
            }
        };
    }

    @Override
    public PlayerQuantumData playerData(Player player) {
        return player.getData(PLAYER_DATA);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }
}
