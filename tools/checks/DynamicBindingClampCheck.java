package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class DynamicBindingClampCheck {
   public static void main(String[] args) {
      UseItemPhaseAction use = new UseItemPhaseAction();
      check(d.clampToEditorRange(use, "holdTicks", 5000).equals(2000), "holdTicks clamps to 2000");
      check(d.clampToEditorRange(use, "holdTicks", -3).equals(0), "holdTicks clamps to 0");
      check(d.clampToEditorRange(use, "repeat", 0).equals(1), "repeat clamps to 1");
      check(d.clampToEditorRange(use, "repeat", 7).equals(7), "in-range value is kept");
      check(d.clampToEditorRange(use, "swapButton", 99).equals(8), "swapButton clamps to 8");
      check(d.clampToEditorRange(use, "notInSchema", 99).equals(99), "field with no schema entry is kept");
      check(d.clampToEditorRange(use, "holdTicks", 5000L).equals(2000L), "long clamps with the int range");

      // Through the binding call site (literal templates need no game).
      UseItemPhaseAction bound = new UseItemPhaseAction();
      d.init_V(bound, "holdTicks", "5000");
      DisconnectAction reader = new DisconnectAction();
      d.init_V(reader, "tolerance", "250.5");
      WaitForHealthAction health = new WaitForHealthAction();
      d.init_V(health, "healthThreshold", "-4");
      Minecraft noGame = null;
      check(d.init(bound, noGame) && bound.holdTicks == 2000, "bound int field is clamped, got " + bound.holdTicks);
      check(d.init(reader, noGame) && reader.tolerance == 100.0, "bound double field is clamped, got " + reader.tolerance);
      check(d.init(health, noGame) && health.healthThreshold == 0.0F, "bound float field is clamped, got " + health.healthThreshold);

      CompoundTag fromOldSave = new CompoundTag();
      fromOldSave.putString("type", "USE_ITEM_PHASE");
      UseItemPhaseAction loaded = new UseItemPhaseAction();
      loaded.fromTag(fromOldSave);
      check(loaded.useCustomSlotMapping, "missing useCustomSlotMapping defaults to true");
      System.out.println("PASS DynamicBindingClampCheck");
   }

   private static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL " + what);
         System.exit(1);
      }
   }
}
