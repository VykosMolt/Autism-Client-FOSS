package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class WaitForCooldownAction implements MacroAction {
   public String itemName = "";
   public MacroItemTarget itemTarget = new MacroItemTarget();
   public boolean checkMainInteractionHand = true;
   public boolean listenDuringPreviousAction = false;

   public WaitForCooldownAction() {
   }

   public WaitForCooldownAction(String itemName, boolean checkMainHand) {
      this.itemName = itemName;
      this.itemTarget = MacroItemTarget.parse(itemName);
      this.checkMainInteractionHand = checkMainHand;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      MacroItemTarget target = this.init();
      if (target.hasSlot() || target.hasItemFilter()) {
         tag.put("itemName", target.toTag());
      }

      tag.putBoolean("checkMainHand", this.checkMainInteractionHand);
      ListenDuringPreviousSerializer.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.itemTarget = tag.getCompound("itemName")
         .map(MacroItemTarget::fromTag)
         .orElseGet(() -> MacroItemTarget.parse(tag.getStringOr("itemName", "")));
      this.itemName = this.itemTarget.toSpecString();
      if (tag.contains("checkMainHand")) {
         this.checkMainInteractionHand = tag.getBooleanOr("checkMainHand", true);
      }

      ListenDuringPreviousSerializer.load(tag, this);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_COOLDOWN;
   }

   @Override
   public String getDisplayName() {
      MacroItemTarget target = this.init();
      String base = !target.hasSlot() && !target.hasItemFilter() ? (this.checkMainInteractionHand ? "Main Hand" : "Off Hand") : target.formatSummary();
      if (base.length() > 10) {
         base = base.substring(0, 8) + "..";
      }

      return "Wait Cooldown (" + base + ")";
   }

   @Override
   public String getIcon() {
      return "CD";
   }

   private MacroItemTarget init() {
      if (this.itemTarget != null && (this.itemTarget.hasSlot() || this.itemTarget.hasItemFilter())) {
         return this.itemTarget;
      }

      this.itemTarget = MacroItemTarget.parse(this.itemName);
      return this.itemTarget;
   }
}
