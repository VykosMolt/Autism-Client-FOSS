package autismclient.util.macro;

import autismclient.mixin.accessor.AutismMinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ClickAction implements PacketOrderAware, MacroAction, GuiWaitAware {
   public ClickAction.Inner_DataUtil type;
   public int clickCount = 1;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;

   public ClickAction() {
   }

   public ClickAction(ClickAction.Inner_DataUtil type) {
      this.type = type;
      this.clickCount = 1;
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null && mc.gameMode != null) {
         if (this.type == ClickAction.Inner_DataUtil.LEFT) {
            ((AutismMinecraftAccessor)mc).autism$startAttack();
         } else if (mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
         } else if (mc.hitResult.getType() == Type.ENTITY) {
            mc.gameMode.interact(mc.player, ((EntityHitResult)mc.hitResult).getEntity(), (EntityHitResult)mc.hitResult, InteractionHand.MAIN_HAND);
         } else if (mc.hitResult.getType() == Type.BLOCK) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, (BlockHitResult)mc.hitResult);
         }
      }
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String getGuiName() {
      return this.guiName;
   }

   @Override
   public void setGuiName(String name) {
      this.guiName = name;
   }

   @Override
   public PacketOrderMode getPacketOrder() {
      return this.packetOrder;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.CLICK;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("clickType", (this.type == null ? ClickAction.Inner_DataUtil.RIGHT : this.type).name());
      tag.putInt("clickCount", this.clickCount);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      PacketOrderAware.writePacketOrder(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("clickType")) {
         try {
            this.type = ClickAction.Inner_DataUtil.valueOf(tag.getStringOr("clickType", "RIGHT"));
         } catch (IllegalArgumentException e) {
            this.type = ClickAction.Inner_DataUtil.RIGHT;
         }
      }

      if (this.type == null) {
         this.type = ClickAction.Inner_DataUtil.RIGHT;
      }

      if (tag.contains("clickCount")) {
         this.clickCount = tag.getIntOr("clickCount", 1);
      }

      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, false);
      if (tag.contains("guiName")) {
         this.guiName = tag.getStringOr("guiName", "");
      }

      this.packetOrder = PacketOrderAware.readPacketOrder(tag);
   }

   @Override
   public String getDisplayName() {
      String base = this.type == ClickAction.Inner_DataUtil.LEFT ? "Left Click" : "Right Click";
      if (this.clickCount > 1) {
         base = base + " x" + this.clickCount;
      }

      return base + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public String getIcon() {
      return this.type == ClickAction.Inner_DataUtil.LEFT ? "L" : "R";
   }

   public enum Inner_DataUtil {
      LEFT,
      RIGHT;
   }
}
