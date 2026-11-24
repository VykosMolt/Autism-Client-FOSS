package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class TableIndex implements MacroAction {
   public TableIndex.Inner_StreamUtil target = TableIndex.Inner_StreamUtil.write;
   public int amount = 1;
   public String labelName = "";
   public TableIndex.Inner_DataUtil onMissingLabel = TableIndex.Inner_DataUtil.init;
   public boolean conditional = false;
   public a condition = a.init_a();
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_U_StreamGate;
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
      a c = this.condition == null ? a.init_a() : this.condition;
      tag.put("condition", c.init_CompoundTag());
      c.init_V_CompoundTag_Str(tag, "cond_");
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.target = i.init(TableIndex.Inner_StreamUtil.class, tag.getStringOr("target", "LABEL"), TableIndex.Inner_StreamUtil.write);
      this.amount = Math.max(0, tag.getIntOr("amount", 1));
      this.labelName = tag.getStringOr("labelName", "");
      this.onMissingLabel = i.init(TableIndex.Inner_DataUtil.class, tag.getStringOr("onMissingLabel", "CONTINUE"), TableIndex.Inner_DataUtil.init);
      this.conditional = tag.getBooleanOr("conditional", false);
      this.condition = a.init(tag, "condition", "cond_");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.target) {
         case init:
            var10000 = "skip " + this.amount;
            break;
         case load:
            var10000 = "back " + this.amount;
            break;
         case read:
            var10000 = "step " + this.amount;
            break;
         case write:
            var10000 = "→ " + (this.labelName != null && !this.labelName.isBlank() ? this.labelName : "?");
            break;
         case update:
            var10000 = "→ top";
            break;
         case sync:
            var10000 = "→ end";
            break;
         case flush:
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
      init,
      load;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }
}
