package io.github.timursl.hexquantum.link;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Permanent damage links (Shared Damage / Distributed Damage). A link lasts until fewer than two of its members are
 * alive and loaded; dead or unloaded members are dropped whenever the link is used, so the split is recalculated.
 * An entity is in at most one link at a time.
 */
public class DamageLinks extends SavedData {
    public enum Mode {
        /**
         * Everyone takes the full damage dealt to any member.
         */
        SHARED,
        /**
         * Damage to any member is split evenly among all living members.
         */
        DISTRIBUTED
    }

    private static final String NAME = "hexquantum_damage_links";
    private static final SavedData.Factory<DamageLinks> FACTORY = new SavedData.Factory<>(DamageLinks::new, DamageLinks::load, null);

    private record Link(Mode mode, Set<UUID> members) {
    }

    private final List<Link> links = new ArrayList<>();
    private final Map<UUID, Link> byMember = new HashMap<>();
    /**
     * Guards against linked damage re-triggering the links.
     */
    private static boolean applying;

    public static DamageLinks get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    public void link(Mode mode, List<? extends Entity> entities) {
        Set<UUID> members = new LinkedHashSet<>();
        for (Entity e : entities) {
            unlink(e.getUUID());
            members.add(e.getUUID());
        }
        Link link = new Link(mode, members);
        links.add(link);
        for (UUID id : members) {
            byMember.put(id, link);
        }
        setDirty();
    }

    public void unlink(UUID member) {
        Link link = byMember.remove(member);
        if (link == null) {
            return;
        }
        link.members.remove(member);
        if (link.members.size() < 2) {
            dissolve(link);
        }
        setDirty();
    }

    private void dissolve(Link link) {
        links.remove(link);
        for (UUID id : link.members) {
            byMember.remove(id);
        }
        link.members.clear();
    }

    /**
     * Called when {@code victim} is about to take {@code amount} damage. Applies the link and returns the damage the
     * victim itself should take.
     */
    public float onDamage(LivingEntity victim, DamageSource source, float amount) {
        if (applying || amount <= 0) {
            return amount;
        }
        Link link = byMember.get(victim.getUUID());
        if (link == null || victim.level().getServer() == null) {
            return amount;
        }
        List<LivingEntity> others = resolveOthers(victim.level().getServer(), link, victim);
        if (others.isEmpty()) {
            return amount;
        }
        applying = true;
        try {
            if (link.mode == Mode.SHARED) {
                for (LivingEntity other : others) {
                    hurt(other, amount);
                }
                return amount;
            }
            float share = amount / (others.size() + 1);
            for (LivingEntity other : others) {
                hurt(other, share);
            }
            return share;
        } finally {
            applying = false;
        }
    }

    private static void hurt(LivingEntity target, float amount) {
        target.invulnerableTime = 0;
        target.hurt(target.damageSources().magic(), amount);
    }

    /**
     * Living, loaded members other than {@code victim}. Drops everyone else from the link.
     */
    private List<LivingEntity> resolveOthers(MinecraftServer server, Link link, LivingEntity victim) {
        List<LivingEntity> others = new ArrayList<>();
        List<UUID> gone = new ArrayList<>();
        for (UUID id : link.members) {
            if (id.equals(victim.getUUID())) {
                continue;
            }
            LivingEntity found = find(server, id);
            if (found == null || !found.isAlive()) {
                gone.add(id);
            } else {
                others.add(found);
            }
        }
        for (UUID id : gone) {
            unlink(id);
        }
        return byMember.get(victim.getUUID()) == link ? others : List.of();
    }

    @Nullable
    private static LivingEntity find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Link link : links) {
            CompoundTag entry = new CompoundTag();
            entry.putString("mode", link.mode.name());
            ListTag members = new ListTag();
            for (UUID id : link.members) {
                members.add(NbtUtils.createUUID(id));
            }
            entry.put("members", members);
            list.add(entry);
        }
        tag.put("links", list);
        return tag;
    }

    private static DamageLinks load(CompoundTag tag, HolderLookup.Provider registries) {
        DamageLinks data = new DamageLinks();
        for (Tag t : tag.getList("links", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            Mode mode;
            try {
                mode = Mode.valueOf(entry.getString("mode"));
            } catch (IllegalArgumentException e) {
                continue;
            }
            Set<UUID> members = new LinkedHashSet<>();
            for (Tag m : entry.getList("members", Tag.TAG_INT_ARRAY)) {
                members.add(NbtUtils.loadUUID(m));
            }
            if (members.size() < 2) {
                continue;
            }
            Link link = new Link(mode, members);
            data.links.add(link);
            for (UUID id : members) {
                data.byMember.put(id, link);
            }
        }
        return data;
    }
}
