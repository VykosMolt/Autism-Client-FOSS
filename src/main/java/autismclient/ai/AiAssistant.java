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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final AiAssistant init_ConfigStore = new AiAssistant();
   private boolean init_Z;
   private boolean load;
   private long init_J;
   private AiAssistantOverlay init_AiAssistantOverlay;

   public static AiAssistant getInstance() {
      return init_ConfigStore;
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
      return this.init_AiAssistantOverlay;
   }

   public void restoreOverlay() {
      if (this.init_AiAssistantOverlay != null && this.init_AiAssistantOverlay.isVisible()) {
         OverlayManager.getInstance().register(this.init_AiAssistantOverlay);
      }
   }

   public void read() {
      OverlayManager manager = OverlayManager.getInstance();
      if (this.init_AiAssistantOverlay != null
         && this.init_AiAssistantOverlay.isVisible()
         && manager.getOverlays().contains(this.init_AiAssistantOverlay)
         && !manager.isHidden(this.init_AiAssistantOverlay)) {
         this.init_AiAssistantOverlay.setVisible(false);
      } else {
         this.openOverlay();
      }
   }

   public void openOverlay() {
      if (!autismclient.modules.aD.init_Z() && autismclient.e.SyncManager.init()) {
         if (this.init_AiAssistantOverlay == null) {
            this.init_AiAssistantOverlay = new AiAssistantOverlay(AiService.getInstance());
         }

         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this.init_AiAssistantOverlay);
         manager.setHidden(this.init_AiAssistantOverlay, false);
         this.init_AiAssistantOverlay.setVisible(true);
         this.init_AiAssistantOverlay.setCollapsed(false);
         manager.bringToFront(this.init_AiAssistantOverlay);
         if (init_Minecraft.gui.screen() == null) {
            init_Minecraft.gui.setScreen(new autismclient.gui.screens.AutismOverlaysScreen(this.init_AiAssistantOverlay, null, false, false));
         }
      }
   }

   public void init(String view) {
      this.openOverlay();
      this.init_AiAssistantOverlay.load(view);
   }

   public void update_V() {
      this.openOverlay();
      this.init_AiAssistantOverlay.sync_V();
   }

   public void init(String action, String query) {
      this.openOverlay();
      this.init_AiAssistantOverlay.openAction(action, query);
   }

   public void sync() {
      if (this.init_AiAssistantOverlay != null) {
         this.init_AiAssistantOverlay.update_V();
      }
   }

   public JsonObject init_JsonObject() {
      return aP.init_JsonObject();
   }

   public void flush() {
      aP.init_V();
   }

   public void close() {
      if (this.init_AiAssistantOverlay != null) {
         this.init_AiAssistantOverlay.flush_V();
      }
   }

   public void open() {
      if (this.init_AiAssistantOverlay != null) {
         this.init_AiAssistantOverlay.close_V();
      }
   }

   public JsonObject load_JsonObject() {
      return this.init_AiAssistantOverlay == null ? new JsonObject() : this.init_AiAssistantOverlay.init_JsonObject();
   }

   void poll() {
      if (this.init_AiAssistantOverlay != null) {
         this.init_AiAssistantOverlay.hide();
      }
   }

   boolean isShown() {
      return this.init_AiAssistantOverlay != null && this.init_AiAssistantOverlay.isVisible() || aP.init_Z();
   }

   void push() {
      if (this.init_AiAssistantOverlay != null) {
         this.init_AiAssistantOverlay.poll_V();
      }
   }

   public void pop() {
      if (this.init_AiAssistantOverlay != null
         && this.init_AiAssistantOverlay.isVisible()
         && init_Minecraft.gui.screen() != null
         && OverlayManager.getInstance().getOverlays().contains(this.init_AiAssistantOverlay)) {
         this.init_AiAssistantOverlay.clearInputFocus();
         init_Minecraft.gui.setScreen(null);
      }
   }

   private void tick() {
      int bind = AutismConfig.getGlobal().keybindAiAssistant;
      boolean pressed = bind != -1 && autismclient.util.ValueSink.init(init_Minecraft, bind);
      boolean openFromKey = pressed && !this.load;
      this.load = pressed;
      if (!autismclient.modules.aD.init_Z() && autismclient.e.RefQueue.init()) {
         AiWorldMemory.init(init_Minecraft);
         if (openFromKey && autismclient.util.at.init()) {
            this.read();
         }

         if ("ai-assistant".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) || autismclient.util.SilentRotationManager.isReturningToPlayer()) {
            autismclient.util.SilentRotationManager.tick("ai-assistant", init_Minecraft.player);
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
