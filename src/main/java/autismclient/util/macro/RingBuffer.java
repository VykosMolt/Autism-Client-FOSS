package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class RingBuffer implements MacroAction {
   public int maxFps = 60;
   public String endMode = RingBuffer.Inner_DataUtil.init.name();
   public int stepCount = 1;
   public boolean useTicks = false;
   public int durationMs = 1000;
   public int durationTicks = 20;
   public boolean continueNextActions = false;
   private boolean init = true;

   public RingBuffer() {
   }

   public RingBuffer(int maxFps, int durationMs) {
      this.maxFps = Math.max(0, maxFps);
      this.durationMs = Math.max(0, durationMs);
   }

   public RingBuffer.Inner_DataUtil init_RingBuffer_DataUti() {
      return RingBuffer.Inner_DataUtil.init(this.endMode);
   }

   public int init_I_3() {
      return Math.max(1, this.stepCount);
   }

   public long init_J_2() {
      return this.useTicks ? Math.max(0, this.durationTicks) * 50L : Math.max(0, this.durationMs);
   }

   @Override
   public void execute(Minecraft mc) {
      long owner = MacroExecutor.load_J();
      BitSetUtil.init(owner < 0L ? 0L : owner, Math.max(0, this.maxFps), this.init_J_2() * 1000000L);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("maxFps", this.maxFps);
      tag.putString("endMode", this.init_RingBuffer_DataUti().name());
      tag.putInt("stepCount", Math.max(1, this.stepCount));
      tag.putBoolean("useTicks", this.useTicks);
      tag.putInt("durationMs", this.durationMs);
      tag.putInt("durationTicks", this.durationTicks);
      tag.putBoolean("continueNextActions", this.continueNextActions);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("maxFps")) {
         this.maxFps = Math.max(0, tag.getIntOr("maxFps", 60));
      }

      if (tag.contains("endMode")) {
         this.endMode = RingBuffer.Inner_DataUtil.init(tag.getStringOr("endMode", RingBuffer.Inner_DataUtil.init.name())).name();
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
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_R_StreamGate;
   }

   @Override
   public String getDisplayName() {
      String cap = this.maxFps <= 0 ? "Freeze" : this.maxFps + " fps";
      String var10000;
      switch (this.init_RingBuffer_DataUti()) {
         case init:
            var10000 = this.useTicks ? Math.max(0, this.durationTicks) + " ticks" : Math.max(0, this.durationMs) + " ms";
            break;
         case load:
            var10000 = this.init_I_3() + (this.init_I_3() == 1 ? " step" : " steps");
            break;
         case read:
            var10000 = "until end";
            break;
         default:
            throw new MatchException(null, null);
      }

      String window = var10000;
      String suffix = this.init_RingBuffer_DataUti() == RingBuffer.Inner_DataUtil.init && this.continueNextActions ? " [cont]" : "";
      return "FPS: " + cap + " (" + window + ")" + suffix;
   }

   @Override
   public String getIcon() {
      return "FPS";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;

      public static RingBuffer.Inner_DataUtil init(String raw) {
         if (raw == null) {
            return init;
         }

         try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return init;
         }
      }
   }
}
