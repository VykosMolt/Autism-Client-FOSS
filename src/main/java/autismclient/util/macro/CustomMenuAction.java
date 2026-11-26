package autismclient.util.macro;

import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.util.bi;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuAction implements MacroAction {
   private static final long init_J = 15000L;
   private static String init_Str = "";
   private static long load;
   public final ArrayList<String> fieldValues = new ArrayList<>();
   public String clickButton = "";
   public int timeoutMs = 30000;
   private boolean init_Z = true;

   @Override
   public void execute(Minecraft mc) {
      long deadline = System.currentTimeMillis() + this.init();
      boolean gateOnRun = MacroExecutor.init_Z();
      String lastError = "";

      while (System.currentTimeMillis() <= deadline && !Thread.currentThread().isInterrupted()) {
         if (gateOnRun && !MacroExecutor.init_Z()) {
            return;
         }

         long seenGeneration = autismclient.util.b.CachePool.init_J();
         CustomMenuSnapshot snapshot = autismclient.util.b.CachePool.init_CustomMenuSnapshot();
         if (snapshot != null) {
            seenGeneration = snapshot.generation();
         } else {
            snapshot = autismclient.util.b.DataUtil.init_CustomMenuSnapshot(mc);
         }

         if (snapshot == null) {
            LockSupport.parkNanos(20000000L);
         } else {
            RenderState.Inner_DataUtil prepared = RenderState.init(this, snapshot, value -> {
               String withSecrets = autismclient.util.aA.init_Str(value);
               if (withSecrets == null) {
                  throw new IllegalStateException("Missing form value");
               } else {
                  j.Inner_CachePool resolved = n.init_j_CachePool_Str_Minecraft(withSecrets, mc);
                  if (!resolved.init_Z()) {
                     throw new IllegalStateException("Missing macro value");
                  } else {
                     return resolved.init_Str();
                  }
               }
            });
            if (!prepared.init_Z()) {
               lastError = prepared.init_Str() == null ? "" : prepared.init_Str();
               if (prepared.init_Str() != null && prepared.init_Str().contains("unavailable")) {
                  LockSupport.parkNanos(20000000L);
                  continue;
               }

               this.init(prepared.init_Str());
               return;
            }

            Screen answered = autismclient.util.b.DataUtil.init_Screen(mc);
            CustomMenuSubmitResult result = CustomMenuAdapterRegistry.submit(snapshot, prepared.init_CustomMenuSubmissi());
            if (!result.success()) {
               this.init(result.error());
               return;
            }

            for (Packet<?> packet : result.packets()) {
               if (!autismclient.util.aA.init(packet)) {
                  this.init("Connection closed before custom-menu submission");
                  return;
               }
            }

            autismclient.util.b.CachePool.init(seenGeneration, result.replacement(), snapshot.phase());
            autismclient.util.b.DataUtil.init(mc, result.clientAction(), answered);
            return;
         }
      }

      this.init(lastError.isBlank() ? "Custom screen never appeared (timed out)" : lastError + " (timed out)");
   }

   private void init(String reason) {
      load(reason != null && !reason.isBlank() ? reason : "Custom screen action failed");
   }

   private static void load(String message) {
      long now = System.currentTimeMillis();
      synchronized (CustomMenuAction.class) {
         if (message.equals(init_Str) && now - load < 15000L) {
            return;
         }

         init_Str = message;
         load = now;
      }

      bi.read(message);
   }

   public int init() {
      return Math.max(100, Math.min(120000, this.timeoutMs));
   }

   @Override
   public StreamGate getType() {
      return StreamGate.CUSTOM_MENU;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", StreamGate.CUSTOM_MENU.name());
      tag.put("fieldValues", i.init(this.fieldValues));
      tag.putString("clickButton", this.clickButton);
      tag.putInt("timeoutMs", this.init());
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.fieldValues.clear();
      this.clickButton = tag.getStringOr("clickButton", "");
      if (tag.getList("fieldValues").isPresent()) {
         this.fieldValues.addAll(i.init(tag.getList("fieldValues").orElse(new ListTag())));
      } else {
         String primary = tag.getStringOr("primaryValue", "");
         if (!primary.isBlank()) {
            this.fieldValues.add(primary);
         }

         this.fieldValues.addAll(i.init(tag.getList("inputValues").orElse(new ListTag())));
         if (this.clickButton.isBlank()) {
            String buttonMode = tag.getStringOr("buttonMode", "SAFE_AUTO");
            String selector = tag.getStringOr("buttonSelector", "");
            int index = Math.max(1, tag.getIntOr("buttonIndex", 1));
            String var10001;
            switch (buttonMode.toUpperCase(Locale.ROOT)) {
               case "ACTION_ID":
               case "LABEL":
                  var10001 = selector;
                  break;
               case "INDEX":
                  var10001 = "#" + index;
                  break;
               default:
                  var10001 = "";
            }

            this.clickButton = var10001;
         }
      }

      this.timeoutMs = Math.max(100, Math.min(120000, tag.getIntOr("timeoutMs", 30000)));
      this.init_Z = tag.getBooleanOr("enabled", true);
   }

   @Override
   public String getDisplayName() {
      String button = this.clickButton.isBlank() ? "auto" : this.clickButton;
      return "Custom Screen (press " + button + ")";
   }

   @Override
   public String getIcon() {
      return "CS";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init_Z = enabled;
   }
}
