package autismclient;

import autismclient.ai.AiAssistant;
import autismclient.ai.AiMacroTools;
import autismclient.ai.SchematicWorkspace;
import autismclient.ai.AiDirectOps;
import autismclient.ai.engine.AiChunkMemory;
import autismclient.protector.ServerPackSelectionRegistry;
import autismclient.protector.PackFailureGuard;
import autismclient.protector.ResourcePackSessionTracker;
import autismclient.protector.ProtectorFeatureFlags;
import autismclient.protector.ModNamespaceCatalog;
import autismclient.protector.VanillaTranslationKeyLoader;
import autismclient.gui.sdf.SdfShapeRenderer;
import autismclient.gui.f.b.k;
import autismclient.modules.NoFallPauseTimer;
import autismclient.modules.HoleEspModule;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.AutoReconnectController;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.OreSimEspScanner;
import autismclient.util.AuditJoinSend;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.AutoCraftingEngine;
import autismclient.util.BedDefenderGhostRenderer;
import autismclient.util.BlockInspectionService;
import autismclient.util.FakeGamemodeManager;
import autismclient.util.JoinMacroController;
import autismclient.util.KillAuraHitMarkerRenderer;
import autismclient.util.LanSyncManager;
import autismclient.util.LagWatchdogReporter;
import autismclient.util.MacroLibraryManager;
import autismclient.util.MapTooltipPreview;
import autismclient.util.FreecamTargetOutlineRenderer;
import autismclient.util.BlockPreviewRenderer;
import autismclient.util.OverlayManager;
import autismclient.util.PacedTpJobManager;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.WindowIconManager;
import autismclient.util.BlockSelectionOutlineRenderer;
import autismclient.util.RemoteViewController;
import autismclient.util.PlacedBlockTrailRenderer;
import autismclient.util.SkeletonEspRenderer;
import autismclient.util.OverlayScreenController;
import autismclient.util.gif.GifTextureManager;
import autismclient.util.macro.MacroExecutor;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class AutismClientMod implements ClientModInitializer {
   private static final ConcurrentHashMap<String, Long> LAST_TICK_ERROR_MS = new ConcurrentHashMap<>();

   private static void runSafe(String where, Runnable action) {
      try {
         action.run();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Autism] client-event '{}' failed; isolated to protect the client", where, t);
      }
   }

   private static void logTickError(String where, Throwable t) {
      long now = System.currentTimeMillis();
      Long last = LAST_TICK_ERROR_MS.get(where);
      if (last == null || now - last >= 5000L) {
         LAST_TICK_ERROR_MS.put(where, now);
         AutismClientAddon.LOG.warn("[Autism] tick '{}' failed; isolated to protect the client", where, t);
      }
   }

   private static void qualificationFlags() {
      Path dir = FabricLoader.getInstance().getGameDir().toAbsolutePath();
      if (dir.toString().contains("autism-ai-qualification-")) {
         Path file = dir.resolve("qualification.properties");
         if (Files.isRegularFile(file)) {
            Properties flags = new Properties();

            try (BufferedReader in = Files.newBufferedReader(file)) {
               flags.load(in);
            } catch (IOException e) {
               AutismClientAddon.LOG.warn("[Autism] qualification flags unreadable: {}", e.toString());
               return;
            }

            for (String key : System.getProperties().stringPropertyNames()) {
               if (key.startsWith("autism.")) {
                  System.clearProperty(key);
               }
            }

            for (String key : flags.stringPropertyNames()) {
               if (key.startsWith("autism.")) {
                  System.setProperty(key, flags.getProperty(key));
               }
            }

            AutismClientAddon.LOG.info("[Autism] qualification flags from {}: {}", file, flags);
         }
      }
   }

   @Override
   public void onInitializeClient() {
      qualificationFlags();
      AutismClientAddon.FOLDER.mkdirs();
      runSafe("initialize", () -> ModuleRuntimeManager.getInstance().initialize());
      ModNamespaceCatalog.initialize();
      if (!ProtectorFeatureFlags.hasExternalProtector()) {
         VanillaTranslationKeyLoader.loadAsync();
      }

      if (ProtectorFeatureFlags.hasFullExternalProtector()) {
         AutismClientAddon.LOG.info("[AutismProtector] External protection mod detected; deferring all anti-fingerprint mixins to it.");
      } else if (ProtectorFeatureFlags.hasOverlappingProtector()) {
         AutismClientAddon.LOG
            .info("[AutismProtector] ExploitPreventer detected; deferring overlapping protections while keeping brand/channel hiding active.");
      } else {
         AutismClientAddon.LOG.info("[AutismProtector] Built-in anti-fingerprint layer active.");
      }

      BlockPreviewRenderer.register();
      FreecamTargetOutlineRenderer.register();
      SkeletonEspRenderer.register();
      BlockSelectionOutlineRenderer.register();
      PlacedBlockTrailRenderer.register();
      if (!AutismLiteVariant.enabled()) {
         BedDefenderGhostRenderer.register();
      }

      KillAuraHitMarkerRenderer.register();
      HoleEspModule.register();
      autismclient.commands.CommandManager.initialize();
      if (!AutismLiteVariant.enabled()) {
         AiDirectOps.reloadToolSettings();
         AiMacroTools.install();
         AiAssistant.initialize();
         SchematicWorkspace.registerEvents();
      }

      AiChunkMemory.setBaseDir(AutismClientAddon.FOLDER.toPath().resolve("ai").resolve("chunkmemory"));
      autismclient.b.DataUtil.init_V();
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         @Override
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("autismclient", "ui_assets");
         }

         @Override
         public void onResourceManagerReload(ResourceManager manager) {
            k.init_V();
            GifTextureManager.reloadAll();
            Thread prewarm = new Thread(() -> {
               try {
                  SdfShapeRenderer.prewarm();
                  autismclient.gui.sdf.SdfIconRenderer.prewarm();
                  autismclient.gui.sdf.MsdfFontFace.prewarm();
                  PacketTypeRegistry.getC2sPackets();
               } catch (Throwable var1x) {
               }
            }, "Autism-UI-Prewarm");
            prewarm.setDaemon(true);
            prewarm.setPriority(1);
            prewarm.start();
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         try {
            if (BlockInspectionService.isBusy()) {
               BlockInspectionService.tick(client);
            }
         } catch (Throwable t) {
            logTickError("blockNbtCapture", t);
         }

         try {
            ScaffoldModule.init_V_22();
         } catch (Throwable t) {
            logTickError("scaffoldEndClick", t);
         }

         try {
            WindowIconManager.apply(client);
         } catch (Throwable var13) {
         }

         try {
            ResourcePackSessionTracker.read();
         } catch (Throwable t) {
            logTickError("packResponses", t);
         }

         try {
            ModuleRuntimeManager.getInstance().onClientTick();
         } catch (Throwable t) {
            logTickError("module", t);
         }

         try {
            OreSimEspScanner.init_V();
         } catch (Throwable t) {
            logTickError("oreSimRetention", t);
         }

         try {
            MacroLibraryManager.flushScheduledSave(false);
         } catch (Throwable t) {
            logTickError("macroSettledSave", t);
         }

         try {
            PacedTpJobManager.tick(client);
         } catch (Throwable t) {
            logTickError("pacedTp", t);
         }

         try {
            JoinMacroController.tick(client);
         } catch (Throwable t) {
            logTickError("joinMacro", t);
         }

         try {
            LagWatchdogReporter.tick(client);
         } catch (Throwable t) {
            logTickError("lagWatchdog", t);
         }

         try {
            AiChunkMemory.tick(client);
         } catch (Throwable t) {
            logTickError("chunkMemory", t);
         }

         try {
            autismclient.ai.engine.AiPlannerWarmup.tick(client);
         } catch (Throwable t) {
            logTickError("plannerWarmup", t);
         }

         try {
            RemoteViewController.tick();
         } catch (Throwable t) {
            logTickError("remoteView", t);
         }

         try {
            autismclient.ai.AuditBuilderLifeguard.tickClient(client);
         } catch (Throwable t) {
            logTickError("builderLifeguard", t);
         }
      });
      ClientTickEvents.END_LEVEL_TICK.register(level -> runSafe("lan.levelTick", () -> LanSyncManager.getInstance().onWorldTick(level.getGameTime())));
      ClientConfigurationConnectionEvents.INIT.register((listener, client) -> {
         runSafe("cfg.preserveUi", () -> OverlayScreenController.captureRetainedForegroundIfNeeded(client));
         runSafe("cfg.hideMenuOverlays", () -> ModuleRuntimeManager.getInstance().hideProfilesOverlayIfUnretained());
         runSafe("cfg.configStarted", () -> ModuleRuntimeManager.getInstance().extendJoinProbeWindowAndRefresh());
         runSafe("cfg.joinMacro", () -> JoinMacroController.onConfigurationStart(listener));
      });
      ClientConfigurationConnectionEvents.DISCONNECT.register((listener, client) -> {
         runSafe("cfg.preserveUi", () -> OverlayScreenController.captureRetainedForegroundIfNeeded(client));
         runSafe("cfg.remoteView", () -> RemoteViewController.stop(false));
         runSafe("cfg.persist", AutismConfig::enqueuePendingSaveNow);
         runSafe("cfg.pacedTp", () -> PacedTpJobManager.cancelAll("connection changed"));
         runSafe("cfg.fakeGamemode", FakeGamemodeManager::reset);
         runSafe("cfg.instaBreak", BlockPreviewRenderer::clear);
         runSafe("cfg.packFailureGuard", PackFailureGuard::reset);
         runSafe("cfg.packResponses", () -> ResourcePackSessionTracker.init(autismclient.protector.ConnectionPrivacyRegistry.getByListener(listener), null));
         runSafe("cfg.hideOverlays", () -> {
            if (!OverlayScreenController.hasRetainedForeground()) {
               OverlayManager.getInstance().minimizeAll();
            }
         });
         runSafe("cfg.onGameLeft", () -> ModuleRuntimeManager.getInstance().onGameLeave());
         runSafe("cfg.joinMacro", JoinMacroController::update);
      });
      ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
         runSafe("join.clientInfo", AuditJoinSend.create(client, handler));
         runSafe("join.preserveUi", () -> OverlayScreenController.captureRetainedForegroundIfNeeded(client));
         runSafe("join.pacedTp", () -> PacedTpJobManager.cancelAll("connection changed"));
         runSafe("join.movementGrace", NoFallPauseTimer::start);
         runSafe("join.hideMenuOverlays", () -> ModuleRuntimeManager.getInstance().hideProfilesOverlayIfUnretained());
         runSafe("join.remember", () -> AutoReconnectController.remember(client.getCurrentServer()));
         runSafe("join.onGameJoin", () -> ModuleRuntimeManager.getInstance().onGameJoin());
         runSafe("join.joinMacro", JoinMacroController::onJoin);
         runSafe("join.surfaceFailures", autismclient.b.DataUtil::load_V);
         runSafe("join.lagWatchdog", LagWatchdogReporter::reset);
         runSafe("join.macroEditor", AutismMacroEditorOverlay::onJoin);
         runSafe("join.recipes", () -> AutoCraftingEngine.prewarmCatalog(client));
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         runSafe("leave.preserveUi", () -> OverlayScreenController.captureRetainedForegroundIfNeeded(client));
         runSafe("leave.remoteView", () -> RemoteViewController.stop(false));
         runSafe("leave.persist", AutismConfig::enqueuePendingSaveNow);
         runSafe("leave.stopMacro", () -> MacroExecutor.onServerDisconnect(AutismConfig.getGlobal().stopMacroOnLeave));
         runSafe("leave.pacedTp", () -> PacedTpJobManager.cancelAll("disconnected"));
         runSafe("leave.movementGrace", NoFallPauseTimer::reset);
         runSafe("leave.fakeGamemode", FakeGamemodeManager::reset);
         runSafe("leave.instaBreak", BlockPreviewRenderer::clear);
         runSafe("leave.packStrip", ServerPackSelectionRegistry::load);
         runSafe("leave.packResponses", () -> ResourcePackSessionTracker.init(autismclient.protector.ConnectionPrivacyRegistry.getByListener(handler), null));
         runSafe("leave.packFailureGuard", PackFailureGuard::reset);
         runSafe("leave.macroEditor", AutismMacroEditorOverlay::onLeave);
         runSafe("leave.lanSync", () -> LanSyncManager.getInstance().resetActiveExecution());
         runSafe("leave.hideOverlays", () -> {
            if (!OverlayScreenController.hasRetainedForeground()) {
               OverlayManager.getInstance().minimizeAll();
            }
         });
         runSafe("leave.joinMacro", JoinMacroController::reset);
         runSafe("leave.onGameLeft", () -> ModuleRuntimeManager.getInstance().onGameLeave());
         runSafe("leave.pingSpoof", autismclient.util.macro.PingSpoofScheduler::clearQueues);
         runSafe("leave.blink", autismclient.modules.BlinkPacketManager::auditDisconnect);
         runSafe("leave.lagWatchdog", LagWatchdogReporter::reset);
      });
      runSafe("init.mapPreview", MapTooltipPreview::register);
      ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
         autismclient.protector.TooltipNbtGuard.sanitize(lines);
         autismclient.protector.TooltipNbtGuard.truncate(lines);
         ModuleRuntimeManager.getInstance().appendItemTooltipLines(stack, lines);
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
         try {
            MacroLibraryManager.flushScheduledSave(true);
         } catch (Throwable t) {
            logTickError("macroSettledSave", t);
         }

         AutismConfig.flushPendingSaves(2000L);
      });
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         AutismConfig.flushPendingSaves(2000L);
      }, "autism-shutdown-save"));
   }
}
