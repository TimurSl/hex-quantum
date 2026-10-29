package io.github.timursl.hexquantum.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Everything HexQuantum remembers about a player. Stored by the loader (NeoForge: a data attachment that is copied
 * on death).
 */
public class PlayerQuantumData {
    public static final int STASH_SIZE = 27;

    /**
     * Pocket Stash contents.
     */
    private final SimpleContainer stash = new SimpleContainer(STASH_SIZE);
    /**
     * Phantom Vault contents (phantom items; view-only in the GUI).
     */
    private final SimpleContainer phantomVault = new SimpleContainer(STASH_SIZE);
    /**
     * Media stored "in" the Quantum Phial. Lives here, not on the item.
     */
    private long quantumMedia;
    /**
     * Identity of the currently valid phial; older phials stop working when a new one is made.
     */
    @Nullable
    private UUID phialId;
    /**
     * Set when a phial was held back on death; it is given back on respawn.
     */
    private boolean restorePhial;
    @Nullable
    private ResourceKey<Level> pocketEntryDimension;
    @Nullable
    private Vec3 pocketEntryPos;

    public SimpleContainer stash() {
        return stash;
    }

    public SimpleContainer phantomVault() {
        return phantomVault;
    }

    public long quantumMedia() {
        return quantumMedia;
    }

    public void setQuantumMedia(long media) {
        this.quantumMedia = Math.max(0, media);
    }

    public void addQuantumMedia(long media) {
        // Saturating add: the phial is "infinite", but a long is not.
        long sum = quantumMedia + media;
        this.quantumMedia = sum < 0 ? Long.MAX_VALUE : sum;
    }

    @Nullable
    public UUID phialId() {
        return phialId;
    }

    public void setPhialId(@Nullable UUID phialId) {
        this.phialId = phialId;
    }

    public boolean restorePhial() {
        return restorePhial;
    }

    public void setRestorePhial(boolean restorePhial) {
        this.restorePhial = restorePhial;
    }

    @Nullable
    public ResourceKey<Level> pocketEntryDimension() {
        return pocketEntryDimension;
    }

    @Nullable
    public Vec3 pocketEntryPos() {
        return pocketEntryPos;
    }

    public void setPocketEntry(@Nullable ResourceKey<Level> dimension, @Nullable Vec3 pos) {
        this.pocketEntryDimension = dimension;
        this.pocketEntryPos = pos;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("stash", ContainerHelper.saveAllItems(new CompoundTag(), stash.getItems(), registries));
        tag.put("phantom_vault", ContainerHelper.saveAllItems(new CompoundTag(), phantomVault.getItems(), registries));
        tag.putLong("quantum_media", quantumMedia);
        if (phialId != null) {
            tag.putUUID("phial_id", phialId);
        }
        tag.putBoolean("restore_phial", restorePhial);
        if (pocketEntryDimension != null && pocketEntryPos != null) {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", pocketEntryDimension.location().toString());
            entry.putDouble("x", pocketEntryPos.x);
            entry.putDouble("y", pocketEntryPos.y);
            entry.putDouble("z", pocketEntryPos.z);
            tag.put("pocket_entry", entry);
        }
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        stash.clearContent();
        phantomVault.clearContent();
        // ContainerHelper keeps slot positions, which Phantom Vault Release relies on.
        ContainerHelper.loadAllItems(tag.getCompound("stash"), stash.getItems(), registries);
        ContainerHelper.loadAllItems(tag.getCompound("phantom_vault"), phantomVault.getItems(), registries);
        quantumMedia = tag.getLong("quantum_media");
        phialId = tag.hasUUID("phial_id") ? tag.getUUID("phial_id") : null;
        restorePhial = tag.getBoolean("restore_phial");
        if (tag.contains("pocket_entry", Tag.TAG_COMPOUND)) {
            CompoundTag entry = tag.getCompound("pocket_entry");
            ResourceLocation dim = ResourceLocation.tryParse(entry.getString("dimension"));
            if (dim != null) {
                pocketEntryDimension = ResourceKey.create(Registries.DIMENSION, dim);
                pocketEntryPos = new Vec3(entry.getDouble("x"), entry.getDouble("y"), entry.getDouble("z"));
            }
        } else {
            pocketEntryDimension = null;
            pocketEntryPos = null;
        }
    }
}
