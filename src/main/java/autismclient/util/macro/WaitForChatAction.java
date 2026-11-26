package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class WaitForChatAction implements MacroAction, GuiWaitAware {
   public String pattern = "";
   public String patternJson = "";
   public boolean useRegex = false;
   public PatternCaptureMatcher.MatchMode matchMode = PatternCaptureMatcher.MatchMode.MATCH;
   public String saveAs = "";
   public int fuzzyPercent = 100;
   public boolean serverMessageOnly = false;
   public int timeoutMs = 0;
   private boolean init = true;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String waitGuiName = "";
   public boolean listenDuringPreviousAction = false;

   public WaitForChatAction() {
   }

   public WaitForChatAction(String pattern) {
      this.pattern = pattern;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("pattern", this.patternJson != null && !this.patternJson.isBlank() ? this.pattern : MacroExecutor.cleanVisibleChatText(this.pattern));
      tag.putString("patternJson", this.patternJson == null ? "" : this.patternJson);
      tag.putBoolean("useRegex", this.useRegex);
      tag.putString("matchMode", this.init_UtilCore_DataUtil().name());
      tag.putString("saveAs", this.saveAs == null ? "" : this.saveAs);
      tag.putInt("fuzzyPercent", this.fuzzyPercent);
      tag.putBoolean("serverMessageOnly", false);
      tag.putInt("timeoutMs", this.timeoutMs);
      tag.putBoolean("enabled", this.init);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("waitGuiName", this.waitGuiName);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("patternJson")) {
         this.patternJson = tag.getStringOr("patternJson", "");
      } else {
         this.patternJson = "";
      }

      if (tag.contains("pattern")) {
         String loadedPattern = tag.getStringOr("pattern", "");
         this.pattern = this.patternJson != null && !this.patternJson.isBlank() ? loadedPattern : MacroExecutor.cleanVisibleChatText(loadedPattern);
      }

      if (tag.contains("useRegex")) {
         this.useRegex = tag.getBooleanOr("useRegex", false);
      }

      this.matchMode = MacroNbtCodecHelper.parseEnum(
         PatternCaptureMatcher.MatchMode.class,
         tag.getStringOr("matchMode", this.useRegex ? "REGEX" : "MATCH"),
         this.useRegex ? PatternCaptureMatcher.MatchMode.REGEX : PatternCaptureMatcher.MatchMode.MATCH
      );
      this.useRegex = this.matchMode == PatternCaptureMatcher.MatchMode.REGEX;
      this.saveAs = tag.getStringOr("saveAs", "");
      if (tag.contains("fuzzyPercent")) {
         this.fuzzyPercent = init_I_I_8(tag.getIntOr("fuzzyPercent", 100));
      } else {
         this.fuzzyPercent = 100;
      }

      this.serverMessageOnly = false;
      if (tag.contains("timeoutMs")) {
         this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.waitForGuiBefore = GuiWaitAware.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = GuiWaitAware.load_Z_CompoundTag_Z(tag, false);
      if (tag.contains("waitGuiName")) {
         this.waitGuiName = tag.getStringOr("waitGuiName", "");
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_CHAT;
   }

   @Override
   public String getDisplayName() {
      String desc = this.pattern.isEmpty() ? "Any Chat" : this.pattern;
      if (this.init_UtilCore_DataUtil() == PatternCaptureMatcher.MatchMode.MATCH) {
         desc = desc + " (~" + init_I_I_8(this.fuzzyPercent) + "%)";
      } else {
         desc = desc + " [" + this.init_UtilCore_DataUtil().name().toLowerCase(Locale.ROOT) + "]";
      }

      if (this.timeoutMs > 0) {
         desc = desc + " (" + this.timeoutMs + "ms)";
      }

      return "Wait Chat: " + desc + GuiWaitAware.init_Str_aP(this);
   }

   @Override
   public String getIcon() {
      return "WCH";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return this.waitGuiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.waitGuiName = name;
   }

   public static int init_I_I_8(int percent) {
      int clamped = Math.max(40, Math.min(100, percent));
      int snapped = Math.round(clamped / 10.0F) * 10;
      return Math.max(40, Math.min(100, snapped));
   }

   public PatternCaptureMatcher.MatchMode init_UtilCore_DataUtil() {
      if (this.matchMode == null) {
         return this.useRegex ? PatternCaptureMatcher.MatchMode.REGEX : PatternCaptureMatcher.MatchMode.MATCH;
      } else {
         return this.useRegex && this.matchMode == PatternCaptureMatcher.MatchMode.MATCH ? PatternCaptureMatcher.MatchMode.REGEX : this.matchMode;
      }
   }
}
