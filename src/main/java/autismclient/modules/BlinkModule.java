package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import java.util.Locale;

public final class BlinkModule extends Module {
   public BlinkModule() {
      super("blink", "Blink", ae.write, "Suspends packets to/from the server, released on disable.");
      this.add(new BoolSetting("outgoing", "Outgoing", true).description("Hold sent packets").build());
      this.add(new BoolSetting("incoming", "Incoming", false).description("Hold received packets").build());
      this.add(new BoolSetting("hold-movement", "Hold Movement", true).visibleWhen(() -> this.bool("outgoing")).description("Delay movement packets").build());
      this.add(
         new BoolSetting("hold-actions", "Hold Attack/Interact", true).visibleWhen(() -> this.bool("outgoing")).description("Delay action packets").build()
      );
      this.add(new BoolSetting("show-position", "Show Position", true).description("Draw server position").build());
      this.add(new BoolSetting("auto-reset", "Auto Reset", false).description("Flush periodically").build());
      this.add(
         new IntSetting("reset-after", "Reset After (ticks)", 50, 1, 100000, 10)
            .sliderRange(1.0, 100.0)
            .visibleWhen(() -> this.bool("auto-reset"))
            .description("Ticks between flushes")
            .build()
      );
   }

   @Override
   public void onEnable() {
      this.init();
      BlinkPacketManager.captureOriginSnapshot();
   }

   @Override
   public void onDisable() {
      BlinkPacketManager.disableAndFlush();
   }

   @Override
   public void onGameLeft() {
      BlinkPacketManager.auditDisconnect();
      if (this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   @Override
   protected void onOptionValueChanged(String optionId) {
      if (this.isEnabled()) {
         boolean wasHoldingMovement = BlinkPacketManager.auditHoldingMovement();
         this.init();
         if (!wasHoldingMovement && BlinkPacketManager.auditHoldingMovement()) {
            BlinkPacketManager.captureOriginSnapshot();
         }
      }
   }

   @Override
   public String info() {
      int held = BlinkPacketManager.getQueuedPacketCount();
      if (held <= 0) {
         return "";
      }

      int reset = BlinkPacketManager.getAutoReleaseTicksRemaining();
      return reset >= 0 ? held + " | " + String.format(Locale.ROOT, "%.1fs", reset / 20.0) : Integer.toString(held);
   }

   private void init() {
      BlinkPacketManager.setHoldDirections(this.bool("incoming"), this.bool("outgoing"));
      BlinkPacketManager.setOutgoingHoldTypes(this.bool("hold-movement"), this.bool("hold-actions"));
      BlinkPacketManager.setShowClone(this.bool("show-position"));
      BlinkPacketManager.setAutoRelease(this.bool("auto-reset"), this.integer("reset-after"));
   }
}
