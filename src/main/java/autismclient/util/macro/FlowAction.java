package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class FlowAction implements MacroAction {
   public FlowAction.Inner_StreamUtil target = FlowAction.Inner_StreamUtil.LABEL;
   public int amount = 1;
   public String labelName = "";
   public FlowAction.Inner_DataUtil onMissingLabel = FlowAction.Inner_DataUtil.CONTINUE;
   public boolean conditional = false;
   public MacroCondition condition = MacroCondition.init_a();
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.FLOW;
   }

   public boolean init(Minecraft mc) {
      return !this.conditional || this.condition == null || this.condition.init_Z_Minecraft(mc);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "FLOW");
      tag.putString("target", this.target.name());
      tag.putInt("amount", this.amount);
      tag.putString("labelName", this.labelName == null ? "" : this.labelName);
      tag.putString("onMissingLabel", this.onMissingLabel.name());
      tag.putBoolean("conditional", this.conditional);
      MacroCondition c = this.condition == null ? MacroCondition.init_a() : this.condition;
      tag.put("condition", c.init_CompoundTag());
      c.init_V_CompoundTag_Str(tag, "cond_");
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.target = MacroNbtCodecHelper.parseEnum(FlowAction.Inner_StreamUtil.class, tag.getStringOr("target", "LABEL"), FlowAction.Inner_StreamUtil.LABEL);
      this.amount = Math.max(0, tag.getIntOr("amount", 1));
      this.labelName = tag.getStringOr("labelName", "");
      this.onMissingLabel = MacroNbtCodecHelper.parseEnum(FlowAction.Inner_DataUtil.class, tag.getStringOr("onMissingLabel", "CONTINUE"), FlowAction.Inner_DataUtil.CONTINUE);
      this.conditional = tag.getBooleanOr("conditional", false);
      this.condition = MacroCondition.init(tag, "condition", "cond_");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.target) {
         case FORWARD:
            var10000 = "skip " + this.amount;
            break;
         case BACK:
            var10000 = "back " + this.amount;
            break;
         case STEP:
            var10000 = "step " + this.amount;
            break;
         case LABEL:
            var10000 = "→ " + (this.labelName != null && !this.labelName.isBlank() ? this.labelName : "?");
            break;
         case TOP:
            var10000 = "→ top";
            break;
         case END:
            var10000 = "→ end";
            break;
         case STOP:
            var10000 = "stop";
            break;
         default:
            throw new MatchException(null, null);
      }

      String where = var10000;
      return "Goto " + where + (this.conditional ? " if " + (this.condition == null ? "" : this.condition.init_Str()) : "");
   }

   @Override
   public String getIcon() {
      return "GO";
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
      CONTINUE,
      STOP;
   }

   public enum Inner_StreamUtil {
      FORWARD,
      BACK,
      STEP,
      LABEL,
      TOP,
      END,
      STOP;
   }
}
