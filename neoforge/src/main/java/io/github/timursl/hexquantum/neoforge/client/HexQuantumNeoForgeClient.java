package io.github.timursl.hexquantum.neoforge.client;

import io.github.timursl.hexquantum.client.MirrorRenderer;
import io.github.timursl.hexquantum.client.render.PhantomBlockRenderer;
import io.github.timursl.hexquantum.client.render.PhantomItemRenderer;
import io.github.timursl.hexquantum.client.render.PlayerCopyRenderer;
import io.github.timursl.hexquantum.entity.CyclicEchoEntity;
import io.github.timursl.hexquantum.entity.CyclicWispEntity;
import io.github.timursl.hexquantum.entity.DelayedEchoEntity;
import io.github.timursl.hexquantum.entity.IllusionEntity;
import io.github.timursl.hexquantum.entity.PersonalEchoEntity;
import io.github.timursl.hexquantum.entity.PhantomItemEntity;
import io.github.timursl.hexquantum.entity.ProjectileWispEntity;
import io.github.timursl.hexquantum.network.ClientQuantumState;
import io.github.timursl.hexquantum.registry.HQBlocks;
import io.github.timursl.hexquantum.registry.HQEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only NeoForge wiring.
 */
public final class HexQuantumNeoForgeClient {
    private HexQuantumNeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(HexQuantumNeoForgeClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> ClientQuantumState.tick());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> ClientQuantumState.reset());
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent e) -> {
            if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
                MirrorRenderer.render(e.getLevelRenderer(), e.getModelViewMatrix(), e.getProjectionMatrix(), e.getCamera());
            }
        });
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.<IllusionEntity>registerEntityRenderer(HQEntities.<IllusionEntity>type(HQEntities.ILLUSION), PlayerCopyRenderer::new);
        event.<DelayedEchoEntity>registerEntityRenderer(HQEntities.<DelayedEchoEntity>type(HQEntities.DELAYED_ECHO), PlayerCopyRenderer::new);
        event.<PersonalEchoEntity>registerEntityRenderer(HQEntities.<PersonalEchoEntity>type(HQEntities.PERSONAL_ECHO), PlayerCopyRenderer::new);
        event.<CyclicEchoEntity>registerEntityRenderer(HQEntities.<CyclicEchoEntity>type(HQEntities.CYCLIC_ECHO), PlayerCopyRenderer::new);
        event.<CyclicWispEntity>registerEntityRenderer(HQEntities.<CyclicWispEntity>type(HQEntities.CYCLIC_WISP), NoopRenderer::new);
        event.<ProjectileWispEntity>registerEntityRenderer(HQEntities.<ProjectileWispEntity>type(HQEntities.PROJECTILE_WISP), NoopRenderer::new);
        event.<PhantomItemEntity>registerEntityRenderer(HQEntities.<PhantomItemEntity>type(HQEntities.PHANTOM_ITEM), PhantomItemRenderer::new);
        event.registerBlockEntityRenderer(HQBlocks.phantomBlockEntity(), PhantomBlockRenderer::new);
    }
}
