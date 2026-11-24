package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class IoWorker implements MacroAction {
   public IoWorker.Inner_StreamUtil mode = IoWorker.Inner_StreamUtil.update;
   public IoWorker.Inner_DataUtil method = IoWorker.Inner_DataUtil.load;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      autismclient.util.W.Inner_DataUtil result;
      if (this.mode == IoWorker.Inner_StreamUtil.update) {
         result = autismclient.util.W.init_W_DataUtil();
      } else if (this.method == IoWorker.Inner_DataUtil.init) {
         result = autismclient.util.ae.init(init_GameType(this.mode));
      } else {
         result = autismclient.util.W.init_W_DataUtil(init_GameType(this.mode));
      }

      AutismClientMessaging.sendPrefixed((result.init_Z() ? "§a" : "§c") + result.init_Str());
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.mode.name());
      tag.putString("method", this.method.name());
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = IoWorker.Inner_StreamUtil.valueOf(tag.getStringOr("mode", "RESET"));
         } catch (IllegalArgumentException ignored) {
            this.mode = IoWorker.Inner_StreamUtil.update;
         }
      }

      try {
         this.method = IoWorker.Inner_DataUtil.valueOf(tag.getStringOr("method", "FAKE"));
      } catch (IllegalArgumentException ignored) {
         this.method = IoWorker.Inner_DataUtil.load;
      }

      this.init = tag.getBooleanOr("enabled", true);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_M_StreamGate;
   }

   @Override
   public String getDisplayName() {
      return this.mode == IoWorker.Inner_StreamUtil.update
         ? "GM: Reset"
         : "GM: " + init_Str(this.mode) + (this.method == IoWorker.Inner_DataUtil.init ? " (Real)" : " (Fake)");
   }

   @Override
   public String getIcon() {
      return "GM";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init = enabled;
   }

   private static GameType init_GameType(IoWorker.Inner_StreamUtil mode) {
      GameType var10000;
      switch (mode) {
         case init:
         case update:
            var10000 = GameType.SURVIVAL;
            break;
         case load:
            var10000 = GameType.CREATIVE;
            break;
         case read:
            var10000 = GameType.ADVENTURE;
            break;
         case write:
            var10000 = GameType.SPECTATOR;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str(IoWorker.Inner_StreamUtil mode) {
      if (mode == null) {
         return "Reset";
      }

      String lower = mode.name().toLowerCase(Locale.ROOT);
      return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
   }

   public enum Inner_DataUtil {
      init,
      load;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
