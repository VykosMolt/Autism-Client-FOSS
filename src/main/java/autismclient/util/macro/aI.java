package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class aI implements CacheEntry, MacroAction {
   public aI.Inner_DataUtil match = aI.Inner_DataUtil.ANY_CHANGE;
   public aI.Inner_StreamUtil gameMode = aI.Inner_StreamUtil.SURVIVAL;
   public boolean detectFake = false;
   public boolean listenDuringPreviousAction = false;
   public int timeoutMs = 0;
   public String saveAs = "";
   private boolean init = true;

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
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.match = i.init(aI.Inner_DataUtil.class, tag.getStringOr("match", aI.Inner_DataUtil.ANY_CHANGE.name()), aI.Inner_DataUtil.ANY_CHANGE);
      this.gameMode = i.init(aI.Inner_StreamUtil.class, tag.getStringOr("gameMode", aI.Inner_StreamUtil.SURVIVAL.name()), aI.Inner_StreamUtil.SURVIVAL);
      this.detectFake = tag.getBooleanOr("detectFake", false);
      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 0));
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public StreamGate getType() {
      return StreamGate.WAIT_GAMEMODE_CHANGE;
   }

   @Override
   public String getDisplayName() {
      String suffix = this.match == aI.Inner_DataUtil.TO_MODE ? " -> " + this.init_Str_6() : " Change";
      return "Wait Gamemode" + suffix + (this.detectFake ? " (fake ok)" : "");
   }

   @Override
   public String getIcon() {
      return "GM?";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public boolean init(GameType mode) {
      return mode == null ? false : this.match == aI.Inner_DataUtil.ANY_CHANGE || mode == this.init_GameType();
   }

   public GameType init_GameType() {
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
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public enum Inner_DataUtil {
      ANY_CHANGE,
      TO_MODE;
   }

   public enum Inner_StreamUtil {
      SURVIVAL,
      CREATIVE,
      ADVENTURE,
      SPECTATOR;
   }
}
