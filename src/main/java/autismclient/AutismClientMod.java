package autismclient;

import autismclient.ai.ConfigStore;
import autismclient.ai.am;
import autismclient.ai.cV;
import autismclient.ai.o;
import autismclient.ai.a.StateCache;
import autismclient.g.ServerPackSelectionRegistry;
import autismclient.g.PackFailureGuard;
import autismclient.g.ResourcePackSessionTracker;
import autismclient.g.ProtectorFeatureFlags;
import autismclient.g.ModNamespaceCatalog;
import autismclient.g.VanillaTranslationKeyLoader;
import autismclient.gui.e.BufferHelper;
import autismclient.gui.f.b.k;
import autismclient.modules.IoWorker;
import autismclient.modules.HoleEspModule;
import autismclient.modules.PoolEntry;
import autismclient.modules.aB;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.ak;
import autismclient.util.AuditJoinSend;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.J;
import autismclient.util.KeyHolder;
import autismclient.util.StreamGate;
import autismclient.util.W;
import autismclient.util.aA;
import autismclient.util.aC;
import autismclient.util.aE;
import autismclient.util.aF;
import autismclient.util.aN;
import autismclient.util.aQ;
import autismclient.util.ad;
import autismclient.util.au;
import autismclient.util.bk;
import autismclient.util.bl;
import autismclient.util.bw;
import autismclient.util.cG;
import autismclient.util.cJ;
import autismclient.util.cd;
import autismclient.util.cg;
import autismclient.util.co;
import autismclient.util.cx;
import autismclient.util.c.GifTextureManager;
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
      runSafe("initialize", () -> PoolEntry.init_PoolEntry().init_V());
      ModNamespaceCatalog.init_V();
      if (!ProtectorFeatureFlags.read()) {
         VanillaTranslationKeyLoader.init_V();
      }

      if (ProtectorFeatureFlags.load()) {
         AutismClientAddon.LOG.info("[AutismProtector] External protection mod detected; deferring all anti-fingerprint mixins to it.");
      } else if (ProtectorFeatureFlags.write()) {
         AutismClientAddon.LOG
            .info("[AutismProtector] ExploitPreventer detected; deferring overlapping protections while keeping brand/channel hiding active.");
      } else {
         AutismClientAddon.LOG.info("[AutismProtector] Built-in anti-fingerprint layer active.");
      }

      au.init();
      ad.init_V();
      co.init_V();
      cJ.load();
      cg.load();
      if (!AutismLiteVariant.enabled()) {
         KeyHolder.init();
      }

      aC.init_V();
      HoleEspModule.init_V_12();
      autismclient.commands.CommandManager.init_V();
      if (!AutismLiteVariant.enabled()) {
         o.init_V();
         am.init_V();
         ConfigStore.init_V();
         cV.init_V();
      }

      StateCache.init(AutismClientAddon.FOLDER.toPath().resolve("ai").resolve("chunkmemory"));
      autismclient.b.DataUtil.init_V();
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         @Override
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("autismclient", "ui_assets");
         }

         @Override
         public void onResourceManagerReload(ResourceManager manager) {
            k.init_V();
            GifTextureManager.init();
            Thread prewarm = new Thread(() -> {
               try {
                  BufferHelper.prewarm();
                  autismclient.gui.e.CachePool.prewarm();
                  autismclient.gui.e.ConfigStore.prewarm();
                  bw.load();
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
            if (StreamGate.init()) {
               StreamGate.init_V(client);
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
            cG.init_V(client);
         } catch (Throwable var13) {
         }

         try {
            ResourcePackSessionTracker.read();
         } catch (Throwable t) {
            logTickError("packResponses", t);
         }

         try {
            PoolEntry.init_PoolEntry().load_V();
         } catch (Throwable t) {
            logTickError("module", t);
         }

         try {
            ak.init_V();
         } catch (Throwable t) {
            logTickError("oreSimRetention", t);
         }

         try {
            aN.init_V_Z(false);
         } catch (Throwable t) {
            logTickError("macroSettledSave", t);
         }

         try {
            bl.init(client);
         } catch (Throwable t) {
            logTickError("pacedTp", t);
         }

         try {
            aA.init_V(client);
         } catch (Throwable t) {
            logTickError("joinMacro", t);
         }

         try {
            aF.init(client);
         } catch (Throwable t) {
            logTickError("lagWatchdog", t);
         }

         try {
            StateCache.init(client);
         } catch (Throwable t) {
            logTickError("chunkMemory", t);
         }

         try {
            autismclient.ai.a.AiPlannerWarmup.putAt(client);
         } catch (Throwable t) {
            logTickError("plannerWarmup", t);
         }

         try {
            cd.init_V();
         } catch (Throwable t) {
            logTickError("remoteView", t);
         }

         try {
            autismclient.ai.AuditBuilderLifeguard.tickClient(client);
         } catch (Throwable t) {
            logTickError("builderLifeguard", t);
         }
      });
      ClientTickEvents.END_LEVEL_TICK.register(level -> runSafe("lan.levelTick", () -> aE.init_aE().init_V_J(level.getGameTime())));
      ClientConfigurationConnectionEvents.INIT.register((listener, client) -> {
         runSafe("cfg.preserveUi", () -> cx.init_V(client));
         runSafe("cfg.hideMenuOverlays", () -> PoolEntry.init_PoolEntry().close_V());
         runSafe("cfg.configStarted", () -> PoolEntry.init_PoolEntry().sync_V());
         runSafe("cfg.joinMacro", () -> aA.init(listener));
      });
      ClientConfigurationConnectionEvents.DISCONNECT.register((listener, client) -> {
         runSafe("cfg.preserveUi", () -> cx.init_V(client));
         runSafe("cfg.remoteView", () -> cd.init(false));
         runSafe("cfg.persist", AutismConfig::enqueuePendingSaveNow);
         runSafe("cfg.pacedTp", () -> bl.init("connection changed"));
         runSafe("cfg.fakeGamemode", W::init_V);
         runSafe("cfg.instaBreak", au::load);
         runSafe("cfg.packFailureGuard", PackFailureGuard::load);
         runSafe("cfg.packResponses", () -> ResourcePackSessionTracker.init(autismclient.g.ConnectionPrivacyRegistry.init(listener), null));
         runSafe("cfg.hideOverlays", () -> {
            if (!cx.load_Z()) {
               bk.init_bk().sync_V();
            }
         });
         runSafe("cfg.onGameLeft", () -> PoolEntry.init_PoolEntry().write_V());
         runSafe("cfg.joinMacro", aA::update);
      });
      ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
         runSafe("join.clientInfo", AuditJoinSend.create(client, handler));
         runSafe("join.preserveUi", () -> cx.init_V(client));
         runSafe("join.pacedTp", () -> bl.init("connection changed"));
         runSafe("join.movementGrace", IoWorker::init_V);
         runSafe("join.hideMenuOverlays", () -> PoolEntry.init_PoolEntry().close_V());
         runSafe("join.remember", () -> aB.init(client.getCurrentServer()));
         runSafe("join.onGameJoin", () -> PoolEntry.init_PoolEntry().read_V());
         runSafe("join.joinMacro", aA::load_V);
         runSafe("join.surfaceFailures", autismclient.b.DataUtil::load_V);
         runSafe("join.lagWatchdog", aF::init);
         runSafe("join.macroEditor", AutismMacroEditorOverlay::close_V_8);
         runSafe("join.recipes", () -> J.init_V(client));
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         runSafe("leave.preserveUi", () -> cx.init_V(client));
         runSafe("leave.remoteView", () -> cd.init(false));
         runSafe("leave.persist", AutismConfig::enqueuePendingSaveNow);
         runSafe("leave.stopMacro", () -> MacroExecutor.init_V_Z(AutismConfig.getGlobal().stopMacroOnLeave));
         runSafe("leave.pacedTp", () -> bl.init("disconnected"));
         runSafe("leave.movementGrace", IoWorker::load);
         runSafe("leave.fakeGamemode", W::init_V);
         runSafe("leave.instaBreak", au::load);
         runSafe("leave.packStrip", ServerPackSelectionRegistry::load);
         runSafe("leave.packResponses", () -> ResourcePackSessionTracker.init(autismclient.g.ConnectionPrivacyRegistry.init(handler), null));
         runSafe("leave.packFailureGuard", PackFailureGuard::load);
         runSafe("leave.macroEditor", AutismMacroEditorOverlay::flush_V_9);
         runSafe("leave.lanSync", () -> aE.init_aE().sync_V());
         runSafe("leave.hideOverlays", () -> {
            if (!cx.load_Z()) {
               bk.init_bk().sync_V();
            }
         });
         runSafe("leave.joinMacro", aA::write);
         runSafe("leave.onGameLeft", () -> PoolEntry.init_PoolEntry().write_V());
         runSafe("leave.pingSpoof", autismclient.util.macro.J::write);
         runSafe("leave.blink", autismclient.modules.HashUtil::auditDisconnect);
         runSafe("leave.lagWatchdog", aF::init);
      });
      runSafe("init.mapPreview", aQ::init_V);
      ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
         autismclient.g.TooltipNbtGuard.init_I(lines);
         autismclient.g.TooltipNbtGuard.init_Z(lines);
         PoolEntry.init_PoolEntry().init(stack, lines);
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
         try {
            aN.init_V_Z(true);
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
