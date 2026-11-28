package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class SPingAction implements MacroAction {
   public int pingDelayMs = 250;
   public boolean realIncoming = false;
   public boolean realOutgoing = false;
   public String endMode = SPingAction.Inner_DataUtil.TIMEOUT.name();
   public int stepCount = 1;
   public boolean useTicks = false;
   public int durationMs = 1000;
   public int durationTicks = 20;
   public boolean continueNextActions = false;
   private boolean enabled = true;

   public SPingAction() {
   }

   public SPingAction(int pingDelayMs, int durationMs) {
      this.pingDelayMs = Math.max(0, pingDelayMs);
      this.durationMs = Math.max(0, durationMs);
   }

   public SPingAction.Inner_DataUtil getEndMode() {
      return SPingAction.Inner_DataUtil.parse(this.endMode);
   }

   public int getStepCount() {
      return Math.max(1, this.stepCount);
   }

   public long getDurationMs() {
      return this.useTicks ? Math.max(0, this.durationTicks) * 50L : Math.max(0, this.durationMs);
   }

   @Override
   public void execute(Minecraft mc) {
      long owner = MacroExecutor.getCurrentRunId();
      PingSpoofScheduler.start(owner < 0L ? 0L : owner, Math.max(0, this.pingDelayMs), this.realIncoming, this.realOutgoing, this.getDurationMs() * 1000000L);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("pingDelayMs", this.pingDelayMs);
      tag.putBoolean("realIncoming", this.realIncoming);
      tag.putBoolean("realOutgoing", this.realOutgoing);
      tag.putString("endMode", this.getEndMode().name());
      tag.putInt("stepCount", Math.max(1, this.stepCount));
      tag.putBoolean("useTicks", this.useTicks);
      tag.putInt("durationMs", this.durationMs);
      tag.putInt("durationTicks", this.durationTicks);
      tag.putBoolean("continueNextActions", this.continueNextActions);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("pingDelayMs")) {
         this.pingDelayMs = Math.max(0, tag.getIntOr("pingDelayMs", 250));
      }

      if (tag.contains("realIncoming")) {
         this.realIncoming = tag.getBooleanOr("realIncoming", false);
      }

      if (tag.contains("realOutgoing")) {
         this.realOutgoing = tag.getBooleanOr("realOutgoing", false);
      }

      if (tag.contains("endMode")) {
         this.endMode = SPingAction.Inner_DataUtil.parse(tag.getStringOr("endMode", SPingAction.Inner_DataUtil.TIMEOUT.name())).name();
      }

      if (tag.contains("stepCount")) {
         this.stepCount = Math.max(1, tag.getIntOr("stepCount", 1));
      }

      if (tag.contains("useTicks")) {
         this.useTicks = tag.getBooleanOr("useTicks", false);
      }

      if (tag.contains("durationMs")) {
         this.durationMs = Math.max(0, tag.getIntOr("durationMs", 1000));
      }

      if (tag.contains("durationTicks")) {
         this.durationTicks = Math.max(0, tag.getIntOr("durationTicks", 20));
      }

      if (tag.contains("continueNextActions")) {
         this.continueNextActions = tag.getBooleanOr("continueNextActions", false);
      }

      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SPING;
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.getEndMode()) {
         case TIMEOUT:
            var10000 = this.useTicks ? Math.max(0, this.durationTicks) + " ticks" : Math.max(0, this.durationMs) + " ms";
            break;
         case STEPS:
            var10000 = this.getStepCount() + (this.getStepCount() == 1 ? " step" : " steps");
            break;
         case END:
            var10000 = "until end";
            break;
         default:
            throw new MatchException(null, null);
      }

      String window = var10000;
      String real = this.realIncoming && this.realOutgoing ? " [real]" : (this.realIncoming ? " [real in]" : (this.realOutgoing ? " [real out]" : ""));
      String suffix = this.getEndMode() == SPingAction.Inner_DataUtil.TIMEOUT && this.continueNextActions ? " [cont]" : "";
      return "Ping: +" + Math.max(0, this.pingDelayMs) + " ms (" + window + ")" + real + suffix;
   }

   @Override
   public String getIcon() {
      return "PING";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   public enum Inner_DataUtil {
      TIMEOUT,
      STEPS,
      END;

      public static SPingAction.Inner_DataUtil parse(String raw) {
         if (raw == null) {
            return TIMEOUT;
         }

         try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return TIMEOUT;
         }
      }
   }
}
