package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class WaitGamemodeChangeAction implements CaptureOutputAction, MacroAction {
   public WaitGamemodeChangeAction.MatchMode match = WaitGamemodeChangeAction.MatchMode.ANY_CHANGE;
   public WaitGamemodeChangeAction.GameModeChoice gameMode = WaitGamemodeChangeAction.GameModeChoice.SURVIVAL;
   public boolean detectFake = false;
   public boolean listenDuringPreviousAction = false;
   public int timeoutMs = 0;
   public String saveAs = "";
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("match", this.match.name());
      tag.putString("gameMode", this.gameMode.name());
      tag.putBoolean("detectFake", this.detectFake);
      tag.putInt("timeoutMs", Math.max(0, this.timeoutMs));
      tag.putBoolean("enabled", this.enabled);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.match = MacroNbtCodecHelper.parseEnum(WaitGamemodeChangeAction.MatchMode.class, tag.getStringOr("match", WaitGamemodeChangeAction.MatchMode.ANY_CHANGE.name()), WaitGamemodeChangeAction.MatchMode.ANY_CHANGE);
      this.gameMode = MacroNbtCodecHelper.parseEnum(WaitGamemodeChangeAction.GameModeChoice.class, tag.getStringOr("gameMode", WaitGamemodeChangeAction.GameModeChoice.SURVIVAL.name()), WaitGamemodeChangeAction.GameModeChoice.SURVIVAL);
      this.detectFake = tag.getBooleanOr("detectFake", false);
      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 0));
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_GAMEMODE_CHANGE;
   }

   @Override
   public String getDisplayName() {
      String suffix = this.match == WaitGamemodeChangeAction.MatchMode.TO_MODE ? " -> " + this.init_Str_6() : " Change";
      return "Wait Gamemode" + suffix + (this.detectFake ? " (fake ok)" : "");
   }

   @Override
   public String getIcon() {
      return "GM?";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   public boolean matches(GameType mode) {
      return mode == null ? false : this.match == WaitGamemodeChangeAction.MatchMode.ANY_CHANGE || mode == this.targetGameType();
   }

   public GameType targetGameType() {
      GameType var10000;
      switch (this.gameMode) {
         case SURVIVAL:
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

   private String init_Str_6() {
      String lower = this.gameMode.name().toLowerCase(Locale.ROOT);
      return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
   }

   @Override
   public String getSaveAs() {
      return this.saveAs;
   }

   @Override
   public void setSaveAs(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public enum MatchMode {
      ANY_CHANGE,
      TO_MODE;
   }

   public enum GameModeChoice {
      SURVIVAL,
      CREATIVE,
      ADVENTURE,
      SPECTATOR;
   }
}
