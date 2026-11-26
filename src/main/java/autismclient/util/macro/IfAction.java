package autismclient.util.macro;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class IfAction implements MacroAction {
   public MacroCondition condition = MacroCondition.init_a();
   public int thenSteps = 1;
   public int elseSteps = 0;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.IF;
   }

   public boolean init(Minecraft mc) {
      return this.condition == null || this.condition.init_Z_Minecraft(mc);
   }

   public int init(List<MacroAction> actions, int headerIndex) {
      if (actions != null && headerIndex >= 0 && headerIndex < actions.size()) {
         int want = Math.max(0, this.thenSteps) + Math.max(0, this.elseSteps);
         int max = Math.min(want, actions.size() - headerIndex - 1);
         int count = 0;

         for (int i = headerIndex + 1; i < actions.size() && count < max; i++) {
            MacroAction a = actions.get(i);
            if (a instanceof IfAction || a instanceof RaceAction || a instanceof ReportAction || a instanceof PacketGateAction) {
               break;
            }

            count++;
         }

         return count;
      } else {
         return 0;
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "IF");
      MacroCondition c = this.condition == null ? MacroCondition.init_a() : this.condition;
      tag.put("condition", c.init_CompoundTag());
      c.init_V_CompoundTag_Str(tag, "cond_");
      tag.putInt("thenSteps", this.thenSteps);
      tag.putInt("elseSteps", this.elseSteps);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.condition = MacroCondition.init(tag, "condition", "cond_");
      this.thenSteps = Math.max(0, tag.getIntOr("thenSteps", 1));
      this.elseSteps = Math.max(0, tag.getIntOr("elseSteps", 0));
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String cond = this.condition == null ? "always" : this.condition.init_Str();
      return "If " + cond + " → " + this.thenSteps + (this.elseSteps > 0 ? " / else " + this.elseSteps : "");
   }

   @Override
   public String getIcon() {
      return "IF";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }
}
