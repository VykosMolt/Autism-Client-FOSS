package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class FakeGamemodeAction implements MacroAction {
   public FakeGamemodeAction.Inner_StreamUtil mode = FakeGamemodeAction.Inner_StreamUtil.RESET;
   public FakeGamemodeAction.Inner_DataUtil method = FakeGamemodeAction.Inner_DataUtil.FAKE;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      autismclient.util.W.Inner_DataUtil result;
      if (this.mode == FakeGamemodeAction.Inner_StreamUtil.RESET) {
         result = autismclient.util.W.init_W_DataUtil();
      } else if (this.method == FakeGamemodeAction.Inner_DataUtil.REAL) {
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
            this.mode = FakeGamemodeAction.Inner_StreamUtil.valueOf(tag.getStringOr("mode", "RESET"));
         } catch (IllegalArgumentException ignored) {
            this.mode = FakeGamemodeAction.Inner_StreamUtil.RESET;
         }
      }

      try {
         this.method = FakeGamemodeAction.Inner_DataUtil.valueOf(tag.getStringOr("method", "FAKE"));
      } catch (IllegalArgumentException ignored) {
         this.method = FakeGamemodeAction.Inner_DataUtil.FAKE;
      }

      this.init = tag.getBooleanOr("enabled", true);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.FAKE_GAMEMODE;
   }

   @Override
   public String getDisplayName() {
      return this.mode == FakeGamemodeAction.Inner_StreamUtil.RESET
         ? "GM: Reset"
         : "GM: " + init_Str(this.mode) + (this.method == FakeGamemodeAction.Inner_DataUtil.REAL ? " (Real)" : " (Fake)");
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

   private static GameType init_GameType(FakeGamemodeAction.Inner_StreamUtil mode) {
      GameType var10000;
      switch (mode) {
         case SURVIVAL:
         case RESET:
            var10000 = GameType.SURVIVAL;
            break;
         case CREATIVE:
            var10000 = GameType.CREATIVE;
            break;
         case ADVENTURE:
            var10000 = GameType.ADVENTURE;
            break;
         case SPECTATOR:
            var10000 = GameType.SPECTATOR;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str(FakeGamemodeAction.Inner_StreamUtil mode) {
      if (mode == null) {
         return "Reset";
      }

      String lower = mode.name().toLowerCase(Locale.ROOT);
      return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
   }

   public enum Inner_DataUtil {
      REAL,
      FAKE;
   }

   public enum Inner_StreamUtil {
      SURVIVAL,
      CREATIVE,
      ADVENTURE,
      SPECTATOR,
      RESET;
   }
}
