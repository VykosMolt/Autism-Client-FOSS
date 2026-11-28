package autismclient.util.macro;

import autismclient.util.AutismMacro;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

public final class LegacyMultiCheck {
   public static void main(String[] args) throws ReflectiveOperationException {
      CompoundTag legacy = new CompoundTag();
      legacy.putString("type", MacroActionType.MULTI.name());
      ListTag accounts = new ListTag();
      accounts.add(StringTag.valueOf("legacy-account"));
      legacy.put("accounts", accounts);
      legacy.putInt("stepCount", 10);
      legacy.putBoolean("connectIfDown", false);
      legacy.putBoolean("disconnectAfter", true);
      legacy.putString("waitMode", "Fixed delay");
      legacy.putInt("waitMs", 1200);

      AutismMacro saved = new AutismMacro("Legacy Multi fixture");
      saved.actions.add(AutismMacro.deserializeAction(legacy));
      saved.actions.add(new DelayAction(0));
      saved.actions.add(new JoinAction());
      saved.actions.add(new RaceAction());
      saved.actions.add(new DelayAction(0));
      CompoundTag before = saved.toTag();

      AutismMacro running = saved.copy();
      check(running.actions.get(0) instanceof MultiAction, "normal macro codec reads the legacy header");
      check(legacy.equals(running.actions.get(0).toTag()), "legacy account and scope fields round-trip");
      check(MacroExecutor.suppressLegacyMultiSteps(running.actions), "run reports a legacy block");
      check(running.actions.size() == saved.actions.size(), "run keeps physical indices");
      check(!running.actions.get(1).isEnabled(), "covered action is disabled");
      check(!running.actions.get(2).isEnabled() && !JoinAction.hasJoinAhead(running.actions, 0, false), "covered actions cannot affect Join lookahead");
      check(running.actions.get(3) instanceof RaceAction && running.actions.get(4) instanceof DelayAction, "scope stops at the next block and preserves its rows");
      check(before.equals(saved.toTag()), "preparing the run leaves saved macro data intact");

      MultiAction disabled = new MultiAction() {
         @Override
         public boolean isEnabled() {
            return false;
         }
      };
      MultiAction truncated = new MultiAction();
      truncated.stepCount = 10;
      MacroAction probe = new DelayAction(0) {
         @Override
         public void execute(Minecraft mc) {
            throw new AssertionError("A covered action executed after direct dispatch");
         }
      };
      LabelAction label = new LabelAction();
      label.name = "inside Multi";
      List<MacroAction> direct = new ArrayList<>(List.of(disabled, probe, label, truncated, probe));
      disabled.stepCount = 10;
      MacroExecutor.suppressLegacyMultiSteps(direct);
      check(!direct.get(1).isEnabled() && direct.get(3) == truncated && !direct.get(4).isEnabled(),
         "adjacent and truncated blocks keep their boundaries even when a header is disabled");
      check(direct.get(2) == label && label.init().equals("inside multi") && !label.isEnabled(),
         "covered labels remain valid jump targets and are excluded from action collectors");
      direct.get(1).execute(null);
      direct.get(4).execute(null);
      check(!MacroExecutor.suppressLegacyMultiSteps(List.of(new DelayAction(0))), "ordinary macros require no suppression");

      MultiAction coversJoin = new MultiAction();
      List<MacroAction> breakSteps = new ArrayList<>(List.of(new autismclient.util.macro.BreakAction(), coversJoin, new JoinAction(), new SendChatAction()));
      MacroExecutor.suppressLegacyMultiSteps(breakSteps);
      var collectBreakSteps = MacroExecutor.class.getDeclaredMethod("init_MacroExecutor_Stat_List_I", List.class, int.class);
      collectBreakSteps.setAccessible(true);
      Object collected = collectBreakSteps.invoke(null, breakSteps, 0);
      var nextSteps = collected.getClass().getDeclaredMethod("init_List");
      nextSteps.setAccessible(true);
      check(((List<?>)nextSteps.invoke(collected)).isEmpty(), "Break still stops at a covered Join boundary");
      System.out.println("PASS LegacyMultiCheck");
   }

   private static void check(boolean condition, String detail) {
      if (!condition) {
         throw new AssertionError(detail);
      }
   }
}
