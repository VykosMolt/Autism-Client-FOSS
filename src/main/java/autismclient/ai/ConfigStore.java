package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.util.AutismConfig;
import autismclient.util.bk;
import autismclient.util.cX;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public final class ConfigStore implements autismclient.util.cn.Inner_DataUtil {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final ConfigStore init_ConfigStore = new ConfigStore();
   private boolean init_Z;
   private boolean load;
   private long init_J;
   private AiAssistantOverlay init_AiAssistantOverlay;

   public static ConfigStore init_ConfigStore() {
      return init_ConfigStore;
   }

   private ConfigStore() {
   }

   public static void init_V() {
      ConfigStore assistant = init_ConfigStore();
      if (!assistant.init_Z) {
         assistant.init_Z = true;
         cG.init_V();
         bW.write_V_2();
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
               if (aq.load()) {
                  aq.init_aq_StreamUtil();
               }

               PkgCls_b.init_V();
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

   public cX init_cX() {
      return this.init_AiAssistantOverlay;
   }

   public void load_V() {
      if (this.init_AiAssistantOverlay != null && this.init_AiAssistantOverlay.sync_Z()) {
         bk.init_bk().init_V(this.init_AiAssistantOverlay);
      }
   }

   public void read() {
      bk manager = bk.init_bk();
      if (this.init_AiAssistantOverlay != null
         && this.init_AiAssistantOverlay.sync_Z()
         && manager.init_List().contains(this.init_AiAssistantOverlay)
         && !manager.load_Z(this.init_AiAssistantOverlay)) {
         this.init_AiAssistantOverlay.init_V_Z(false);
      } else {
         this.write_V();
      }
   }

   public void write_V() {
      if (!autismclient.modules.aD.init_Z() && autismclient.e.SyncManager.init()) {
         if (this.init_AiAssistantOverlay == null) {
            this.init_AiAssistantOverlay = new AiAssistantOverlay(cc.init_cc());
         }

         bk manager = bk.init_bk();
         manager.init_V(this.init_AiAssistantOverlay);
         manager.init_V_cX_Z(this.init_AiAssistantOverlay, false);
         this.init_AiAssistantOverlay.init_V_Z(true);
         this.init_AiAssistantOverlay.write_V_Z(false);
         manager.update_V(this.init_AiAssistantOverlay);
         if (init_Minecraft.gui.screen() == null) {
            init_Minecraft.gui.setScreen(new autismclient.gui.d.AutismOverlaysScreen(this.init_AiAssistantOverlay, null, false, false));
         }
      }
   }

   public void init(String view) {
      this.write_V();
      this.init_AiAssistantOverlay.load(view);
   }

   public void update_V() {
      this.write_V();
      this.init_AiAssistantOverlay.sync_V();
   }

   public void init(String action, String query) {
      this.write_V();
      this.init_AiAssistantOverlay.init_V_Str_Str(action, query);
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
         this.init_AiAssistantOverlay.open_V();
      }
   }

   boolean init_Z() {
      return this.init_AiAssistantOverlay != null && this.init_AiAssistantOverlay.sync_Z() || aP.init_Z();
   }

   void push() {
      if (this.init_AiAssistantOverlay != null) {
         this.init_AiAssistantOverlay.poll_V();
      }
   }

   public void pop() {
      if (this.init_AiAssistantOverlay != null
         && this.init_AiAssistantOverlay.sync_Z()
         && init_Minecraft.gui.screen() != null
         && bk.init_bk().init_List().contains(this.init_AiAssistantOverlay)) {
         this.init_AiAssistantOverlay.push_V();
         init_Minecraft.gui.setScreen(null);
      }
   }

   private void tick() {
      int bind = AutismConfig.getGlobal().keybindAiAssistant;
      boolean pressed = bind != -1 && autismclient.util.ValueSink.init(init_Minecraft, bind);
      boolean openFromKey = pressed && !this.load;
      this.load = pressed;
      if (!autismclient.modules.aD.init_Z() && autismclient.e.RefQueue.init()) {
         cM.init(init_Minecraft);
         if (openFromKey && autismclient.util.at.init()) {
            this.read();
         }

         if ("ai-assistant".equals(autismclient.util.aD.init_Str()) || autismclient.util.aD.init_Z()) {
            autismclient.util.aD.init_V_Str_LocalPlayer("ai-assistant", init_Minecraft.player);
         }
      }
   }

   private void run() {
      if (cc.load_cc() != null) {
         cc.load_cc().sync_V();
      }
   }

   @Override
   public boolean load_Z_23() {
      return autismclient.ai.a.g.init(autismclient.util.aD.init_Z(), true, autismclient.util.cn.init_Z());
   }
}
