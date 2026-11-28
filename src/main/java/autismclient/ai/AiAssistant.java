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
   private boolean initialized;
   private boolean keyWasDown;
   private long lastLifecycleWarnAt;
   private AiAssistantOverlay overlay;

   public static AiAssistant getInstance() {
      return INSTANCE;
   }

   private AiAssistant() {
   }

   public static void initialize() {
      AiAssistant assistant = getInstance();
      if (!assistant.initialized) {
         assistant.initialized = true;
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
               assistant.pollKeybind();
            } catch (Throwable error) {
               long now = System.currentTimeMillis();
               if (now - assistant.lastLifecycleWarnAt >= 5000L) {
                  assistant.lastLifecycleWarnAt = now;
                  AutismClientAddon.LOG.warn("[AI] utility lifecycle tick failed", error);
               }
            }
         });
         ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> assistant.onDisconnect());
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

   public void toggle() {
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

   public void openView(String view) {
      this.openOverlay();
      this.overlay.showView(view);
   }

   public void update_V() {
      this.openOverlay();
      this.overlay.openSettings();
   }

   public void openAction(String action, String query) {
      this.openOverlay();
      this.overlay.openAction(action, query);
   }

   public void sync() {
      if (this.overlay != null) {
         this.overlay.confirmActionMenu();
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
         this.overlay.confirmPlacement();
      }
   }

   public void open() {
      if (this.overlay != null) {
         this.overlay.close_V();
      }
   }

   public JsonObject load_JsonObject() {
      return this.overlay == null ? new JsonObject() : this.overlay.actionMenuStatus();
   }

   void hideOverlay() {
      if (this.overlay != null) {
         this.overlay.hide();
      }
   }

   boolean isShown() {
      return this.overlay != null && this.overlay.isVisible() || aP.init_Z();
   }

   void push() {
      if (this.overlay != null) {
         this.overlay.cancelActionMenu();
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

   private void pollKeybind() {
      int bind = AutismConfig.getGlobal().keybindAiAssistant;
      boolean pressed = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      boolean openFromKey = pressed && !this.keyWasDown;
      this.keyWasDown = pressed;
      if (!autismclient.modules.aD.init_Z() && autismclient.e.RefQueue.init()) {
         AiWorldMemory.tick(MC);
         if (openFromKey && autismclient.util.at.init()) {
            this.toggle();
         }

         if ("ai-assistant".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) || autismclient.util.SilentRotationManager.isReturningToPlayer()) {
            autismclient.util.SilentRotationManager.tick("ai-assistant", MC.player);
         }
      }
   }

   private void onDisconnect() {
      if (AiService.getExistingInstance() != null) {
         AiService.getExistingInstance().onWorldDisconnect();
      }
   }

   @Override
   public boolean isSilentMovementActive() {
      return autismclient.ai.engine.AiSilentAim.active(autismclient.util.SilentRotationManager.isReturningToPlayer(), true, autismclient.util.SilentMovementCoordinator.isSafeWalkActive());
   }
}
