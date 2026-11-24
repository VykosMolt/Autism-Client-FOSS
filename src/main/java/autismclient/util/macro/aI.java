package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;

public class aI implements CacheEntry, MacroAction {
   public aI.Inner_DataUtil match = aI.Inner_DataUtil.init;
   public aI.Inner_StreamUtil gameMode = aI.Inner_StreamUtil.init;
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
      this.match = i.init(aI.Inner_DataUtil.class, tag.getStringOr("match", aI.Inner_DataUtil.init.name()), aI.Inner_DataUtil.init);
      this.gameMode = i.init(aI.Inner_StreamUtil.class, tag.getStringOr("gameMode", aI.Inner_StreamUtil.init.name()), aI.Inner_StreamUtil.init);
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
      return StreamGate.f_o_StreamGate;
   }

   @Override
   public String getDisplayName() {
      String suffix = this.match == aI.Inner_DataUtil.load ? " -> " + this.init_Str_6() : " Change";
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
      return mode == null ? false : this.match == aI.Inner_DataUtil.init || mode == this.init_GameType();
   }

   public GameType init_GameType() {
      GameType var10000;
      switch (this.gameMode) {
         case init:
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
      init,
      load;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
