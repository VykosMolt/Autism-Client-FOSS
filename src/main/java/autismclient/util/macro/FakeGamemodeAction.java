package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class FakeGamemodeAction implements MacroAction {
   public FakeGamemodeAction.GameModeChoice mode = FakeGamemodeAction.GameModeChoice.RESET;
   public FakeGamemodeAction.ApplyMethod method = FakeGamemodeAction.ApplyMethod.FAKE;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      autismclient.util.FakeGamemodeManager.ApplyResult result;
      if (this.mode == FakeGamemodeAction.GameModeChoice.RESET) {
         result = autismclient.util.FakeGamemodeManager.clearFake();
      } else if (this.method == FakeGamemodeAction.ApplyMethod.REAL) {
         result = autismclient.util.RealGamemodeSender.send(gameTypeOf(this.mode));
      } else {
         result = autismclient.util.FakeGamemodeManager.applyFake(gameTypeOf(this.mode));
      }

      AutismClientMessaging.sendPrefixed((result.success() ? "§a" : "§c") + result.message());
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.mode.name());
      tag.putString("method", this.method.name());
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = FakeGamemodeAction.GameModeChoice.valueOf(tag.getStringOr("mode", "RESET"));
         } catch (IllegalArgumentException ignored) {
            this.mode = FakeGamemodeAction.GameModeChoice.RESET;
         }
      }

      try {
         this.method = FakeGamemodeAction.ApplyMethod.valueOf(tag.getStringOr("method", "FAKE"));
      } catch (IllegalArgumentException ignored) {
         this.method = FakeGamemodeAction.ApplyMethod.FAKE;
      }

      this.enabled = tag.getBooleanOr("enabled", true);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.FAKE_GAMEMODE;
   }

   @Override
   public String getDisplayName() {
      return this.mode == FakeGamemodeAction.GameModeChoice.RESET
         ? "GM: Reset"
         : "GM: " + labelOf(this.mode) + (this.method == FakeGamemodeAction.ApplyMethod.REAL ? " (Real)" : " (Fake)");
   }

   @Override
   public String getIcon() {
      return "GM";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   private static GameType gameTypeOf(FakeGamemodeAction.GameModeChoice mode) {
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

   private static String labelOf(FakeGamemodeAction.GameModeChoice mode) {
      if (mode == null) {
         return "Reset";
      }

      String lower = mode.name().toLowerCase(Locale.ROOT);
      return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
   }

   public enum ApplyMethod {
      REAL,
      FAKE;
   }

   public enum GameModeChoice {
      SURVIVAL,
      CREATIVE,
      ADVENTURE,
      SPECTATOR,
      RESET;
   }
}
