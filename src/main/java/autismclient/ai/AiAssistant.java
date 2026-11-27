package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.util.AutismConfig;
import autismclient.util.OverlayManager;
import autismclient.util.AutismOverlay;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public final class AiAssistant implements autismclient.util.SilentMovementCoordinator.Participant {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final AiAssistant INSTANCE = new AiAssistant();
   private boolean init_Z;
   private boolean load;
   private long init_J;
   private AiAssistantOverlay overlay;

   public static AiAssistant getInstance() {
      return INSTANCE;
   }

   private AiAssistant() {
   }

   public static void initialize() {
      AiAssistant assistant = getInstance();
      if (!assistant.init_Z) {
         assistant.init_Z = true;
         AiVisualization.initialize();
         AiSchematicPlacement.ensureTickHandler();
         AutismConfig config = AutismConfig.getGlobal();
         AutismConfig.ModuleState legacy = config.modules.remove("ai-assistant");
         if (legacy != null) {
            if (config.keybindAiAssistant == -1) {
               config.keybindAiAssistant = legacy.keybind;
            }

            config.save();
         }

         ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
               if (AiMenuClose.load()) {
                  AiMenuClose.receiptState();
               }

               AiContainerTransactions.tickActiveSwap();
               assistant.tick();
            } catch (Throwable error) {
               long now = System.currentTimeMillis();
               if (now - assistant.init_J >= 5000L) {
                  assistant.init_J = now;
                  AutismClientAddon.LOG.warn("[AI] utility lifecycle tick failed", error);
               }
            }
         });
         ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> assistant.run());
      }
   }

   public AutismOverlay getOverlay() {
      return this.overlay;
   }

   public void restoreOverlay() {
      if (this.overlay != null && this.overlay.isVisible()) {
         OverlayManager.getInstance().register(this.overlay);
      }
   }

   public void read() {
      OverlayManager manager = OverlayManager.getInstance();
      if (this.overlay != null
         && this.overlay.isVisible()
         && manager.getOverlays().contains(this.overlay)
         && !manager.isHidden(this.overlay)) {
         this.overlay.setVisible(false);
      } else {
         this.openOverlay();
      }
   }

   public void openOverlay() {
      if (!autismclient.modules.aD.init_Z() && autismclient.e.SyncManager.init()) {
         if (this.overlay == null) {
            this.overlay = new AiAssistantOverlay(AiService.getInstance());
         }

         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this.overlay);
         manager.setHidden(this.overlay, false);
         this.overlay.setVisible(true);
         this.overlay.setCollapsed(false);
         manager.bringToFront(this.overlay);
         if (MC.gui.screen() == null) {
            MC.gui.setScreen(new autismclient.gui.screens.AutismOverlaysScreen(this.overlay, null, false, false));
         }
      }
   }

   public void init(String view) {
      this.openOverlay();
      this.overlay.load(view);
   }

   public void update_V() {
      this.openOverlay();
      this.overlay.sync_V();
   }

   public void init(String action, String query) {
      this.openOverlay();
      this.overlay.openAction(action, query);
   }

   public void sync() {
      if (this.overlay != null) {
         this.overlay.update_V();
      }
   }

   public JsonObject init_JsonObject() {
      return aP.init_JsonObject();
   }

   public void flush() {
      aP.init_V();
   }

   public void close() {
      if (this.overlay != null) {
         this.overlay.flush_V();
      }
   }

   public void open() {
      if (this.overlay != null) {
         this.overlay.close_V();
      }
   }

   public JsonObject load_JsonObject() {
      return this.overlay == null ? new JsonObject() : this.overlay.init_JsonObject();
   }

   void poll() {
      if (this.overlay != null) {
         this.overlay.hide();
      }
   }

   boolean isShown() {
      return this.overlay != null && this.overlay.isVisible() || aP.init_Z();
   }

   void push() {
      if (this.overlay != null) {
         this.overlay.poll_V();
      }
   }

   public void pop() {
      if (this.overlay != null
         && this.overlay.isVisible()
         && MC.gui.screen() != null
         && OverlayManager.getInstance().getOverlays().contains(this.overlay)) {
         this.overlay.clearInputFocus();
         MC.gui.setScreen(null);
      }
   }

   private void tick() {
      int bind = AutismConfig.getGlobal().keybindAiAssistant;
      boolean pressed = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      boolean openFromKey = pressed && !this.load;
      this.load = pressed;
      if (!autismclient.modules.aD.init_Z() && autismclient.e.RefQueue.init()) {
         AiWorldMemory.init(MC);
         if (openFromKey && autismclient.util.at.init()) {
            this.read();
         }

         if ("ai-assistant".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) || autismclient.util.SilentRotationManager.isReturningToPlayer()) {
            autismclient.util.SilentRotationManager.tick("ai-assistant", MC.player);
         }
      }
   }

   private void run() {
      if (AiService.getExistingInstance() != null) {
         AiService.getExistingInstance().onWorldDisconnect();
      }
   }

   @Override
   public boolean isSilentMovementActive() {
      return autismclient.ai.engine.AiSilentAim.init(autismclient.util.SilentRotationManager.isReturningToPlayer(), true, autismclient.util.SilentMovementCoordinator.isSafeWalkActive());
   }
}
