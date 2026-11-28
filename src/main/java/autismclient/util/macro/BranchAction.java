package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class BranchAction implements MacroAction {
   public BranchAction.ConditionKind conditionKind = BranchAction.ConditionKind.ALWAYS;
   public String value = "";
   public int thenSteps = 1;
   public int elseSteps = 0;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.BRANCH;
   }

   public boolean matches(Minecraft mc) {
      if (mc == null) {
         return false;
      }

      MacroTemplateResolver.ResolveResult resolution = MacroVariableContext.resolveTemplate(this.value, mc);
      if (!resolution.success()) {
         return false;
      }

      String resolved = resolution.value();
      boolean var10000;
      switch (this.conditionKind) {
         case ALWAYS:
            var10000 = true;
            break;
         case GUI_TYPE:
            var10000 = GuiScreenMatcher.matchesTitle(mc.gui.screen(), resolved);
            break;
         case INVENTORY_ITEM:
            var10000 = WaitInventoryPredicateAction.hasItem(mc, MacroItemTarget.parse(resolved));
            break;
         case ENTITY_TARGET:
            var10000 = mc.crosshairPickEntity != null;
            break;
         case HELD_ITEM:
            var10000 = mc.player != null
               && MacroItemTarget.parse(resolved).matchScore(mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot()) >= 0;
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
      this.conditionKind = MacroNbtCodecHelper.parseEnum(BranchAction.ConditionKind.class, tag.getStringOr("conditionKind", "ALWAYS"), BranchAction.ConditionKind.ALWAYS);
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

   public enum ConditionKind {
      ALWAYS,
      GUI_TYPE,
      INVENTORY_ITEM,
      ENTITY_TARGET,
      HELD_ITEM;
   }
}
