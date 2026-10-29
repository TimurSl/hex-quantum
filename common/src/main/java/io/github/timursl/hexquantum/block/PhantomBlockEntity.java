package io.github.timursl.hexquantum.block;

import io.github.timursl.hexquantum.registry.HQBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Holds what a phantom block looks like, who made it, and (if empowered) the hex it runs.
 */
public class PhantomBlockEntity extends BlockEntity {
    private BlockState mimic = Blocks.STONE.defaultBlockState();
    @Nullable
    private UUID owner;
    @Nullable
    private Tag hex;
    private long lastTrigger = Long.MIN_VALUE;

    public PhantomBlockEntity(BlockPos pos, BlockState state) {
        super(HQBlocks.PHANTOM_BLOCK_ENTITY.value(), pos, state);
    }

    public void setup(BlockState mimic, UUID owner, @Nullable Tag hex) {
        this.mimic = mimic;
        this.owner = owner;
        this.hex = hex;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockState getMimic() {
        return mimic;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Nullable
    public Tag getHex() {
        return hex;
    }

    public boolean isEmpowered() {
        return hex != null;
    }

    /**
     * Rate limit for empowered triggers.
     */
    public boolean tryTrigger(long gameTime, int cooldown) {
        if (lastTrigger != Long.MIN_VALUE && gameTime - lastTrigger < cooldown) {
            return false;
        }
        lastTrigger = gameTime;
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("mimic", NbtUtils.writeBlockState(mimic));
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        if (hex != null) {
            tag.put("hex", hex.copy());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mimic = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("mimic"));
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        hex = tag.contains("hex") ? tag.get("hex") : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Clients only need the look and whether it is empowered (for particles), never the hex itself.
        CompoundTag tag = new CompoundTag();
        tag.put("mimic", NbtUtils.writeBlockState(mimic));
        if (hex != null) {
            tag.put("hex", new CompoundTag());
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
