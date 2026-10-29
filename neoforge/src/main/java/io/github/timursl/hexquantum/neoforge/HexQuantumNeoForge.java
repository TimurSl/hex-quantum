package io.github.timursl.hexquantum.neoforge;

import io.github.timursl.hexquantum.HQEvents;
import io.github.timursl.hexquantum.HexQuantum;
import io.github.timursl.hexquantum.network.ClientQuantumState;
import io.github.timursl.hexquantum.network.MirrorOpticsPayload;
import io.github.timursl.hexquantum.network.QuantumShiftPayload;
import io.github.timursl.hexquantum.network.ShiftedOpticsPayload;
import io.github.timursl.hexquantum.neoforge.client.HexQuantumNeoForgeClient;
import io.github.timursl.hexquantum.registry.HQEntities;
import io.github.timursl.hexquantum.registry.HQItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge entry point. Everything here only forwards to the loader-independent code in the common project.
 */
@Mod(HexQuantum.MOD_ID)
public class HexQuantumNeoForge {
    public HexQuantumNeoForge(IEventBus modBus, ModContainer container) {
        HexQuantum.init();
        NeoForgePlatform.registerAll(modBus);
        container.registerConfig(ModConfig.Type.SERVER, NeoForgeConfig.build());

        modBus.addListener(HexQuantumNeoForge::registerPayloads);
        modBus.addListener(HexQuantumNeoForge::registerAttributes);
        modBus.addListener(HexQuantumNeoForge::creativeTabs);

        IEventBus game = NeoForge.EVENT_BUS;
        game.addListener((ServerTickEvent.Post e) -> HQEvents.onServerTick(e.getServer()));
        game.addListener((ServerStoppedEvent e) -> HQEvents.onServerStopped());
        game.addListener((EntityTickEvent.Post e) -> {
            if (e.getEntity() instanceof LivingEntity living) {
                HQEvents.onLivingTick(living);
            }
        });
        game.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                HQEvents.onPlayerLogout(player);
            }
        });
        game.addListener((PlayerEvent.PlayerChangedDimensionEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                HQEvents.onPlayerChangedDimension(player);
            }
        });
        game.addListener((PlayerEvent.PlayerRespawnEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                HQEvents.onPlayerRespawn(player);
            }
        });
        game.addListener(HexQuantumNeoForge::onDrops);
        game.addListener((LivingExperienceDropEvent e) -> {
            if (HQEvents.shouldSuppressDrops(e.getEntity())) {
                e.setDroppedExperience(0);
            }
        });
        game.addListener((LivingIncomingDamageEvent e) -> {
            float amount = HQEvents.onIncomingDamage(e.getEntity(), e.getSource(), e.getAmount());
            if (amount < 0) {
                e.setCanceled(true);
            } else if (amount != e.getAmount()) {
                e.setAmount(amount);
            }
        });
        game.addListener((LivingDeathEvent e) -> HQEvents.onLivingDeath(e.getEntity()));
        game.addListener((LivingChangeTargetEvent e) -> {
            if (HQEvents.onChangeTarget(e.getEntity(), e.getNewAboutToBeSetTarget())) {
                e.setCanceled(true);
            }
        });
        game.addListener((ProjectileImpactEvent e) -> {
            if (HQEvents.onProjectileImpact(e.getProjectile(), e.getRayTraceResult())) {
                e.setCanceled(true);
            }
        });
        game.addListener((ExplosionEvent.Start e) -> {
            if (HQEvents.onExplosionStart(e.getLevel(), e.getExplosion())) {
                e.setCanceled(true);
            }
        });
        game.addListener((AttackEntityEvent e) -> {
            if (HQEvents.onAttackEntity(e.getEntity(), e.getTarget())) {
                e.setCanceled(true);
            }
        });
        game.addListener((PlayerInteractEvent.EntityInteract e) -> {
            if (HQEvents.onInteractEntity(e.getEntity(), e.getTarget())) {
                e.setCanceled(true);
            }
        });
        game.addListener((PlayerInteractEvent.EntityInteractSpecific e) -> {
            if (HQEvents.onBlockInteraction(e.getEntity())) {
                e.setCanceled(true);
            }
        });
        game.addListener((PlayerInteractEvent.RightClickBlock e) -> {
            if (HQEvents.onBlockInteraction(e.getEntity())) {
                e.setCanceled(true);
            }
        });
        game.addListener((PlayerInteractEvent.LeftClickBlock e) -> {
            if (HQEvents.onBlockInteraction(e.getEntity())) {
                e.setCanceled(true);
            }
        });
        game.addListener((PlayerInteractEvent.RightClickItem e) -> {
            if (HQEvents.onUseItem(e.getEntity(), e.getItemStack())) {
                e.setCanceled(true);
            }
        });
        game.addListener((BlockEvent.BreakEvent e) -> {
            if (HQEvents.onBlockInteraction(e.getPlayer())) {
                e.setCanceled(true);
            }
        });
        game.addListener((BlockEvent.EntityPlaceEvent e) -> {
            if (e.getEntity() instanceof Player player && HQEvents.onBlockInteraction(player)) {
                e.setCanceled(true);
            }
        });

        if (FMLEnvironment.dist == Dist.CLIENT) {
            HexQuantumNeoForgeClient.init(modBus);
        }
    }

    private static void onDrops(LivingDropsEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            HQEvents.onPlayerDrops(player, e.getDrops());
        }
        if (HQEvents.shouldSuppressDrops(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(QuantumShiftPayload.TYPE, QuantumShiftPayload.STREAM_CODEC,
            (payload, context) -> ClientQuantumState.handle(payload));
        registrar.playToClient(MirrorOpticsPayload.TYPE, MirrorOpticsPayload.STREAM_CODEC,
            (payload, context) -> ClientQuantumState.handle(payload));
        registrar.playToClient(ShiftedOpticsPayload.TYPE, ShiftedOpticsPayload.STREAM_CODEC,
            (payload, context) -> ClientQuantumState.handle(payload));
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        HQEntities.registerAttributes(event::put);
    }

    private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(HQItems.QUANTUM_PHIAL.value());
        }
    }
}
