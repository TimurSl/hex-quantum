package io.github.timursl.hexquantum.phantom;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Creator and hex of every empowered phantom mob (vanilla mobs have nowhere to store a hex themselves).
 */
public class PhantomRegistry extends SavedData {
    public record Entry(UUID owner, Tag hex) {
    }

    private static final String NAME = "hexquantum_phantoms";
    private static final SavedData.Factory<PhantomRegistry> FACTORY = new SavedData.Factory<>(PhantomRegistry::new, PhantomRegistry::load, null);

    private final Map<UUID, Entry> entries = new HashMap<>();

    public static PhantomRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    @Nullable
    public Entry get(UUID mob) {
        return entries.get(mob);
    }

    public void put(UUID mob, UUID owner, Tag hex) {
        entries.put(mob, new Entry(owner, hex));
        setDirty();
    }

    public void remove(UUID mob) {
        if (entries.remove(mob) != null) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        entries.forEach((mob, entry) -> {
            CompoundTag e = new CompoundTag();
            e.putUUID("mob", mob);
            e.putUUID("owner", entry.owner());
            e.put("hex", entry.hex().copy());
            list.add(e);
        });
        tag.put("phantoms", list);
        return tag;
    }

    private static PhantomRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        PhantomRegistry data = new PhantomRegistry();
        for (Tag t : tag.getList("phantoms", Tag.TAG_COMPOUND)) {
            CompoundTag e = (CompoundTag) t;
            if (e.hasUUID("mob") && e.hasUUID("owner") && e.contains("hex")) {
                data.entries.put(e.getUUID("mob"), new Entry(e.getUUID("owner"), e.get("hex")));
            }
        }
        return data;
    }
}
