package io.github.timursl.hexquantum.registry;

import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.entity.CyclicEchoEntity;
import io.github.timursl.hexquantum.entity.CyclicWispEntity;
import io.github.timursl.hexquantum.entity.DelayedEchoEntity;
import io.github.timursl.hexquantum.entity.IllusionEntity;
import io.github.timursl.hexquantum.entity.PersonalEchoEntity;
import io.github.timursl.hexquantum.entity.PhantomItemEntity;
import io.github.timursl.hexquantum.entity.PlayerCopyEntity;
import io.github.timursl.hexquantum.entity.ProjectileWispEntity;
import io.github.timursl.hexquantum.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public final class HQEntities {
    private static final HQRegistrar<EntityType<?>> R = Services.PLATFORM.registrar(Registries.ENTITY_TYPE);

    public static final Holder<EntityType<?>> ILLUSION = R.register("illusion", () ->
        EntityType.Builder.<IllusionEntity>of(IllusionEntity::new, MobCategory.MISC).sized(0.6f, 1.8f).eyeHeight(1.62f)
            .clientTrackingRange(10).build(HexQuantum.id("illusion").toString()));
    public static final Holder<EntityType<?>> DELAYED_ECHO = R.register("delayed_echo", () ->
        EntityType.Builder.<DelayedEchoEntity>of(DelayedEchoEntity::new, MobCategory.MISC).sized(0.6f, 1.8f).eyeHeight(1.62f)
            .clientTrackingRange(10).build(HexQuantum.id("delayed_echo").toString()));
    public static final Holder<EntityType<?>> PERSONAL_ECHO = R.register("personal_echo", () ->
        EntityType.Builder.<PersonalEchoEntity>of(PersonalEchoEntity::new, MobCategory.MISC).sized(0.6f, 1.8f).eyeHeight(1.62f)
            .clientTrackingRange(10).build(HexQuantum.id("personal_echo").toString()));
    public static final Holder<EntityType<?>> CYCLIC_ECHO = R.register("cyclic_echo", () ->
        EntityType.Builder.<CyclicEchoEntity>of(CyclicEchoEntity::new, MobCategory.MISC).sized(0.6f, 1.8f).eyeHeight(1.62f)
            .clientTrackingRange(10).updateInterval(1).build(HexQuantum.id("cyclic_echo").toString()));
    public static final Holder<EntityType<?>> CYCLIC_WISP = R.register("cyclic_wisp", () ->
        EntityType.Builder.<CyclicWispEntity>of(CyclicWispEntity::new, MobCategory.MISC).sized(0.3f, 0.3f)
            .clientTrackingRange(8).build(HexQuantum.id("cyclic_wisp").toString()));
    public static final Holder<EntityType<?>> PROJECTILE_WISP = R.register("projectile_wisp", () ->
        EntityType.Builder.<ProjectileWispEntity>of(ProjectileWispEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
            .clientTrackingRange(8).updateInterval(1).build(HexQuantum.id("projectile_wisp").toString()));
    public static final Holder<EntityType<?>> PHANTOM_ITEM = R.register("phantom_item", () ->
        EntityType.Builder.<PhantomItemEntity>of(PhantomItemEntity::new, MobCategory.MISC).sized(0.25f, 0.25f)
            .clientTrackingRange(6).updateInterval(20).build(HexQuantum.id("phantom_item").toString()));

    private HQEntities() {
    }

    public static <T extends Entity> EntityType<T> type(Holder<EntityType<?>> holder) {
        return (EntityType<T>) holder.value();
    }

    /**
     * Attribute tables for the living entities; the loader feeds these to its attribute registration.
     */
    public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> sink) {
        sink.accept(type(ILLUSION), PlayerCopyEntity.createAttributes(20).build());
        sink.accept(type(DELAYED_ECHO), PlayerCopyEntity.createAttributes(20).build());
        sink.accept(type(PERSONAL_ECHO), PlayerCopyEntity.createAttributes(1).build());
        sink.accept(type(CYCLIC_ECHO), PlayerCopyEntity.createAttributes(1).build());
    }

    public static void init() {
    }
}
