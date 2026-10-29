package io.github.timursl.hexquantum.casting;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;

import java.util.ArrayList;
import java.util.List;

/**
 * NBT (de)serialisation of stored hexes for entities, block entities and saved data.
 */
public final class HexStorage {
    private static final Codec<List<Iota>> CODEC = IotaType.TYPED_CODEC.listOf();

    private HexStorage() {
    }

    public static Tag save(List<Iota> hex, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return CODEC.encodeStart(ops, hex).result().orElseGet(ListTag::new);
    }

    public static List<Iota> load(Tag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return CODEC.parse(ops, tag).result().map(l -> (List<Iota>) new ArrayList<>(l)).orElseGet(ArrayList::new);
    }
}
