package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PlaceAction implements PacketOrderAware, AimTargetAction, MacroAction, GuiWaitAware {
   public String itemName = "";
   public MacroItemTarget itemTarget = new MacroItemTarget();
   public BlockPos blockPos = BlockPos.ZERO;
   public Direction direction = Direction.UP;
   public boolean manualDirection = false;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public boolean waitForItem = true;
   public boolean silentSwitch = false;
   public boolean sneak = false;
   public String sneakMode = "Packet";
   public boolean interact = false;
   public InteractTimingMode interactTiming = InteractTimingMode.AFTER;
   public int interactCustomMs = 0;
   public String guiName = "";
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;
   private boolean init = true;
   public boolean raycast = false;

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null
         && mc.gameMode != null
         && mc.level != null
         && this.blockPos != null
         && !mc.level.isOutsideBuildHeight(this.blockPos)
         && autismclient.util.E.init_Z(mc, this.blockPos)) {
         BlockHitResult hit = init(mc, this.blockPos, this.manualDirection, this.direction);
         if (hit != null) {
            int originalSlot = mc.player.getInventory().getSelectedSlot();
            MacroItemTarget target = this.init_ArrayUtil().init_ArrayUtil_Minecraft(mc);
            if (target != null) {
               init(mc, target);
               boolean sneaking = this.sneak;
               if (sneaking) {
                  autismclient.util.SneakPacketController.init(mc, this.sneakMode);
               }

               try {
                  mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
               } finally {
                  if (sneaking) {
                     autismclient.util.SneakPacketController.load(mc, this.sneakMode);
                  }
               }

               if (this.silentSwitch && mc.player.getInventory().getSelectedSlot() != originalSlot) {
                  autismclient.util.aw.init_V(mc, originalSlot);
               }
            }
         }
      }
   }

   static BlockHitResult init(Minecraft mc, BlockPos targetPos, boolean manualDirection, Direction manualFace) {
      if (mc != null && mc.level != null && targetPos != null) {
         if (mc.level.isOutsideBuildHeight(targetPos)) {
            return null;
         }

         Direction[] order;
         if (manualDirection) {
            Direction face = manualFace == null ? Direction.UP : manualFace;
            order = new Direction[]{face};
         } else {
            order = Direction.values();
         }

         for (Direction face : order) {
            BlockPos supportPos = targetPos.relative(face.getOpposite());
            if (!mc.level.isOutsideBuildHeight(supportPos)
               && !mc.level.getBlockState(supportPos).isAir()
               && autismclient.util.E.init_Z(mc, supportPos)
               && mc.level.getBlockState(targetPos).isAir()) {
               Vec3 hitPos = Vec3.atCenterOf(supportPos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
               return new BlockHitResult(hitPos, face, supportPos, false);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private BlockHitResult init_BlockHitResult_Minecraft(Minecraft mc) {
      return init(mc, this.blockPos, this.manualDirection, this.direction);
   }

   private static void init(Minecraft mc, MacroItemTarget target) {
      if (target != null && (target.init_Z() || target.update())) {
         if (target.update()) {
            autismclient.util.aw.init(mc, target, mc.player.getInventory().getSelectedSlot());
         } else {
            int visibleSlot = target.init_I;
            if (visibleSlot >= 0 && visibleSlot <= 8) {
               autismclient.util.aw.init_V(mc, visibleSlot);
            } else {
               int handlerSlot = autismclient.util.aw.read_I(mc, visibleSlot);
               if (handlerSlot >= 0) {
                  int hotbarSlot = mc.player.getInventory().getSelectedSlot();
                  int hotbarHandlerSlot = autismclient.util.aw.read_I(mc, hotbarSlot);
                  if (hotbarHandlerSlot >= 0) {
                     autismclient.util.aw.write(mc, handlerSlot, hotbarHandlerSlot);
                  }
               }
            }
         }
      }
   }

   public void init_V_Minecraft(Minecraft mc) {
      if (mc != null && mc.hitResult instanceof BlockHitResult blockHit) {
         this.blockPos = blockHit.getBlockPos();
      }
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return this.guiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.guiName = name;
   }

   @Override
   public PacketOrderMode init_D() {
      return this.packetOrder;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PLACE;
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public String getIcon() {
      return "P";
   }

   @Override
   public String getDisplayName() {
      MacroItemTarget target = this.init_ArrayUtil();
      String item = !target.init_Z() && !target.update() ? "current" : target.load_Str();
      String pos = this.blockPos.getX() + "," + this.blockPos.getY() + "," + this.blockPos.getZ();
      String face = this.manualDirection ? this.direction.name().toLowerCase() : "auto";
      return "Place " + item + " at " + pos + " " + face + GuiWaitAware.init_Str_aP(this);
   }

   @Override
   public boolean init_Z_3() {
      return this.raycast;
   }

   @Override
   public void init_V_Z(boolean value) {
      this.raycast = value;
   }

   @Override
   public AimTargetAction.Inner_DataUtil init_M_DataUtil_Minecraft(Minecraft mc) {
      return AimTargetAction.Inner_DataUtil.init(this.blockPos);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "PLACE");
      MacroItemTarget target = this.init_ArrayUtil();
      if (target.init_Z() || target.update()) {
         tag.put("itemName", target.init_CompoundTag());
      }

      tag.putInt("blockX", this.blockPos.getX());
      tag.putInt("blockY", this.blockPos.getY());
      tag.putInt("blockZ", this.blockPos.getZ());
      tag.putBoolean("manualDirection", this.manualDirection);
      if (this.manualDirection) {
         tag.putString("direction", this.direction.name());
      }

      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putBoolean("waitForItem", this.waitForItem);
      tag.putBoolean("silentSwitch", this.silentSwitch);
      tag.putBoolean("sneak", this.sneak);
      tag.putBoolean("raycast", this.raycast);
      tag.putString("sneakMode", this.sneakMode);
      tag.putBoolean("interact", this.interact);
      tag.putString("interactTiming", this.interactTiming.name());
      tag.putInt("interactCustomMs", this.interactCustomMs);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("enabled", this.init);
      PacketOrderAware.init_V_CompoundTag_D(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.itemTarget = tag.getCompound("itemName").map(MacroItemTarget::init_ArrayUtil_CompoundTag).orElseGet(() -> {
         String legacyName = tag.getStringOr("itemName", "");
         if (!legacyName.isBlank()) {
            return MacroItemTarget.update(legacyName);
         }

         int legacySlot = tag.getIntOr("slot", -1);
         return legacySlot >= 0 ? MacroItemTarget.init_ArrayUtil_I(legacySlot) : new MacroItemTarget();
      });
      this.itemName = this.itemTarget.write_Str();
      int x = tag.getIntOr("blockX", 0);
      int y = tag.getIntOr("blockY", 0);
      int z = tag.getIntOr("blockZ", 0);
      this.blockPos = new BlockPos(x, y, z);
      this.manualDirection = tag.getBooleanOr("manualDirection", false);
      this.direction = this.manualDirection ? init_Direction_CompoundTag(tag) : Direction.UP;
      this.waitForGuiBefore = GuiWaitAware.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = GuiWaitAware.load_Z_CompoundTag_Z(tag, false);
      this.waitForItem = tag.getBooleanOr("waitForItem", true);
      this.silentSwitch = tag.getBooleanOr("silentSwitch", false);
      this.sneak = tag.getBooleanOr("sneak", false);
      this.raycast = tag.getBooleanOr("raycast", false);
      this.sneakMode = tag.getStringOr("sneakMode", "Packet");
      this.interact = tag.getBooleanOr("interact", false);
      this.interactTiming = InteractTimingMode.init(tag.getStringOr("interactTiming", "AFTER"), InteractTimingMode.AFTER);
      this.interactCustomMs = Math.max(-5000, Math.min(5000, tag.getIntOr("interactCustomMs", 0)));
      this.guiName = tag.getStringOr("guiName", "");
      this.init = tag.getBooleanOr("enabled", true);
      this.packetOrder = PacketOrderAware.init_D_CompoundTag(tag);
   }

   private static Direction init_Direction_CompoundTag(CompoundTag tag) {
      if (!tag.contains("direction")) {
         return Direction.UP;
      }

      String named = tag.getStringOr("direction", "");
      if (!named.isBlank()) {
         try {
            return Direction.valueOf(named.trim().toUpperCase());
         } catch (IllegalArgumentException var4) {
         }
      }

      int dirOrd = tag.getIntOr("direction", Direction.UP.ordinal());
      Direction[] values = Direction.values();
      return values[Math.max(0, Math.min(values.length - 1, dirOrd))];
   }

   public MacroItemTarget init_ArrayUtil() {
      if (this.itemTarget != null && (this.itemTarget.init_Z() || this.itemTarget.update())) {
         return this.itemTarget;
      }

      this.itemTarget = MacroItemTarget.update(this.itemName);
      return this.itemTarget;
   }
}
