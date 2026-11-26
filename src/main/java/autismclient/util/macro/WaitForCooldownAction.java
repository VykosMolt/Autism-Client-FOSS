package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class WaitForCooldownAction implements MacroAction {
   public String itemName = "";
   public ArrayUtil itemTarget = new ArrayUtil();
   public boolean checkMainInteractionHand = true;
   public boolean listenDuringPreviousAction = false;

   public WaitForCooldownAction() {
   }

   public WaitForCooldownAction(String itemName, boolean checkMainHand) {
      this.itemName = itemName;
      this.itemTarget = ArrayUtil.update(itemName);
      this.checkMainInteractionHand = checkMainHand;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      ArrayUtil target = this.init();
      if (target.init_Z() || target.update()) {
         tag.put("itemName", target.init_CompoundTag());
      }

      tag.putBoolean("checkMainHand", this.checkMainInteractionHand);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.itemTarget = tag.getCompound("itemName")
         .map(ArrayUtil::init_ArrayUtil_CompoundTag)
         .orElseGet(() -> ArrayUtil.update(tag.getStringOr("itemName", "")));
      this.itemName = this.itemTarget.write_Str();
      if (tag.contains("checkMainHand")) {
         this.checkMainInteractionHand = tag.getBooleanOr("checkMainHand", true);
      }

      p.load(tag, this);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.WAIT_COOLDOWN;
   }

   @Override
   public String getDisplayName() {
      ArrayUtil target = this.init();
      String base = !target.init_Z() && !target.update() ? (this.checkMainInteractionHand ? "Main Hand" : "Off Hand") : target.load_Str();
      if (base.length() > 10) {
         base = base.substring(0, 8) + "..";
      }

      return "Wait Cooldown (" + base + ")";
   }

   @Override
   public String getIcon() {
      return "CD";
   }

   private ArrayUtil init() {
      if (this.itemTarget != null && (this.itemTarget.init_Z() || this.itemTarget.update())) {
         return this.itemTarget;
      }

      this.itemTarget = ArrayUtil.update(this.itemName);
      return this.itemTarget;
   }
}
