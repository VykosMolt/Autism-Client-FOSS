package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;

public class UseItemAction implements PacketOrderAware, MacroAction, GuiWaitAware {
   public String itemName = "";
   public MacroItemTarget itemTarget = new MacroItemTarget();
   public int slot = -1;
   public UseItemAction.Inner_DataUtil useMode = UseItemAction.Inner_DataUtil.AUTOMATIC;
   public boolean waitForFinish = true;
   public int holdTicks = 20;
   public int useCount = 1;
   public boolean sneak = false;
   public boolean waitForGui = false;
   public String guiName = "";
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.getConnection() != null) {
         MacroItemTarget target = this.resolvedTarget();
         target = target.resolveTemplates(mc);
         if (target != null) {
            if (target.hasSlot() || target.hasItemFilter()) {
               autismclient.util.InventorySlotHelper.ensureItemInHotbar(mc, target, mc.player.getInventory().getSelectedSlot());
            }

            mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, mc.player.getYRot(), mc.player.getXRot()));
         }
      }
   }

   public void releaseUseItem(Minecraft mc) {
      if (mc.player != null && mc.getConnection() != null) {
         mc.getConnection().send(new ServerboundPlayerActionPacket(Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.USE_ITEM;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "USE_ITEM");
      MacroItemTarget target = this.resolvedTarget();
      if (target.hasSlot() || target.hasItemFilter()) {
         tag.put("itemName", target.toTag());
      }

      if (this.slot >= 0) {
         tag.putInt("slot", this.slot);
      }

      tag.putString("useMode", this.useMode.name());
      tag.putBoolean("waitForFinish", this.waitForFinish);
      tag.putInt("holdTicks", this.holdTicks);
      tag.putInt("useCount", this.useCount);
      tag.putBoolean("sneak", this.sneak);
      tag.putBoolean("waitForGui", this.waitForGui);
      tag.putString("guiName", this.guiName == null ? "" : this.guiName);
      tag.putBoolean("enabled", this.init);
      PacketOrderAware.writePacketOrder(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.itemTarget = tag.getCompound("itemName")
         .map(MacroItemTarget::fromTag)
         .orElseGet(() -> MacroItemTarget.parse(tag.getStringOr("itemName", "")));
      this.slot = tag.contains("slot") ? Math.max(-1, tag.getIntOr("slot", -1)) : this.itemTarget.slot;
      this.itemTarget.slot = -1;
      this.itemName = this.itemTarget.toSpecString();
      if (tag.contains("useMode")) {
         String modeName = tag.getStringOr("useMode", "AUTOMATIC");
         if ("INSTANT".equals(modeName)) {
            modeName = "AUTOMATIC";
         }

         if ("HOLD".equals(modeName)) {
            modeName = "CUSTOM_HOLD";
         }

         try {
            this.useMode = UseItemAction.Inner_DataUtil.valueOf(modeName);
         } catch (IllegalArgumentException ignored) {
            this.useMode = UseItemAction.Inner_DataUtil.AUTOMATIC;
         }
      }

      this.waitForFinish = tag.getBooleanOr("waitForFinish", true);
      if (tag.contains("holdTicks")) {
         this.holdTicks = tag.getIntOr("holdTicks", 20);
      }

      if (tag.contains("useCount")) {
         this.useCount = Math.max(1, tag.getIntOr("useCount", 1));
      }

      this.sneak = tag.getBooleanOr("sneak", false);
      this.waitForGui = tag.getBooleanOr("waitForGui", false);
      this.guiName = tag.getStringOr("guiName", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      this.packetOrder = PacketOrderAware.readPacketOrder(tag);
   }

   @Override
   public boolean waitsForGuiBefore() {
      return false;
   }

   @Override
   public void load(boolean v) {
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGui;
   }

   @Override
   public void read(boolean v) {
      this.waitForGui = v;
   }

   @Override
   public String getGuiName() {
      return this.guiName == null ? "" : this.guiName;
   }

   @Override
   public void setGuiName(String name) {
      this.guiName = name == null ? "" : name;
   }

   @Override
   public PacketOrderMode getPacketOrder() {
      return this.packetOrder;
   }

   @Override
   public String getDisplayName() {
      MacroItemTarget target = this.resolvedTarget();
      String item = !target.hasSlot() && !target.hasItemFilter() ? "current" : target.formatSummary();
      String count = this.useCount > 1 ? " x" + this.useCount : "";
      String wait = this.waitForFinish ? ", wait" : "";
      String sneakLabel = this.sneak ? ", sneak" : "";
      String base = this.useMode == UseItemAction.Inner_DataUtil.CUSTOM_HOLD
         ? "Use Item (" + item + ", hold " + this.holdTicks + "t" + wait + sneakLabel + ")"
         : "Use Item (" + item + count + wait + sneakLabel + ")";
      return base + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public String getIcon() {
      return "U";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   private MacroItemTarget resolvedTarget() {
      MacroItemTarget target = this.itemTarget != null ? this.itemTarget.copy() : MacroItemTarget.parse(this.itemName);
      if (!target.hasSlot() && !target.hasItemFilter()) {
         target = MacroItemTarget.parse(this.itemName);
      }

      target.slot = this.slot;
      return target;
   }

   public enum Inner_DataUtil {
      AUTOMATIC,
      CUSTOM_HOLD;
   }
}
