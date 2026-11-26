package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class BranchAction implements MacroAction {
   public BranchAction.Inner_DataUtil conditionKind = BranchAction.Inner_DataUtil.ALWAYS;
   public String value = "";
   public int thenSteps = 1;
   public int elseSteps = 0;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.BRANCH;
   }

   public boolean init(Minecraft mc) {
      if (mc == null) {
         return false;
      }

      j.Inner_CachePool resolution = n.init_j_CachePool_Str_Minecraft(this.value, mc);
      if (!resolution.init_Z()) {
         return false;
      }

      String resolved = resolution.init_Str();
      boolean var10000;
      switch (this.conditionKind) {
         case ALWAYS:
            var10000 = true;
            break;
         case GUI_TYPE:
            var10000 = f.init(mc.gui.screen(), resolved);
            break;
         case INVENTORY_ITEM:
            var10000 = WaitInventoryPredicateAction.init_Z_Minecraft_ArrayUtil(mc, ArrayUtil.update(resolved));
            break;
         case ENTITY_TARGET:
            var10000 = mc.crosshairPickEntity != null;
            break;
         case HELD_ITEM:
            var10000 = mc.player != null
               && ArrayUtil.update(resolved).init_I_ItemStack_I(mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot()) >= 0;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "BRANCH");
      tag.putString("conditionKind", this.conditionKind.name());
      tag.putString("value", this.value);
      tag.putInt("thenSteps", this.thenSteps);
      tag.putInt("elseSteps", this.elseSteps);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.conditionKind = i.init(BranchAction.Inner_DataUtil.class, tag.getStringOr("conditionKind", "ALWAYS"), BranchAction.Inner_DataUtil.ALWAYS);
      this.value = tag.getStringOr("value", "");
      this.thenSteps = tag.getIntOr("thenSteps", 1);
      this.elseSteps = tag.getIntOr("elseSteps", 0);
   }

   @Override
   public String getDisplayName() {
      return "If " + this.conditionKind + " then " + this.thenSteps + " else " + this.elseSteps;
   }

   @Override
   public String getIcon() {
      return "?";
   }

   public enum Inner_DataUtil {
      ALWAYS,
      GUI_TYPE,
      INVENTORY_ITEM,
      ENTITY_TARGET,
      HELD_ITEM;
   }
}
