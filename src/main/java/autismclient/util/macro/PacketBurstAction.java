package autismclient.util.macro;

import autismclient.util.AutismSessionState;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.Vec3;

public class PacketBurstAction implements MacroAction {
   public PacketBurstAction.BurstMode mode = PacketBurstAction.BurstMode.CONTAINER_CLICK;
   public int count = 1;
   public int delayTicks = 0;
   public int slot = 0;
   public int button = 0;
   public String containerInput = "PICKUP";
   public int entityId = -1;
   public String hand = "MAIN_HAND";
   public String clientCommand = "REQUEST_STATS";
   public String playerCommand = "OPEN_INVENTORY";
   public int bundleIndex = 0;
   public int carriedSlot = 0;
   public int containerId = 0;
   public boolean flushBefore = false;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.getConnection() != null) {
         if (this.flushBefore) {
            AutismSessionState.getInstance().flushDelayedPackets(mc.getConnection());
         }

         int times = this.getClampedCount();

         for (int i = 0; i < times; i++) {
            if (autismclient.modules.aD.read_Z()) {
               return;
            }

            this.sendOnce(mc);
         }
      }
   }

   int getClampedCount() {
      return Math.max(1, Math.min(10000, this.count));
   }

   boolean sendOnce(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && mc != null && mc.getConnection() != null) {
         Packet<?> packet = this.init_Packet(mc);
         if (packet == null) {
            return false;
         }

         mc.getConnection().send(packet);
         return true;
      } else {
         return false;
      }
   }

   private Packet<?> init_Packet(Minecraft mc) {
      Packet var10000;
      switch (this.mode) {
         case CONTAINER_CLICK:
            var10000 = new ServerboundContainerClickPacket(
               mc.player != null && mc.player.containerMenu != null ? mc.player.containerMenu.containerId : 0,
               mc.player != null && mc.player.containerMenu != null ? mc.player.containerMenu.getStateId() : 0,
               (short)this.slot,
               (byte)this.button,
               this.parsedContainerInput(),
               new Int2ObjectOpenHashMap<>(),
               HashedStack.EMPTY
            );
            break;
         case ENTITY_INTERACT:
            int id = this.entityId;
            if (id < 0 && mc.crosshairPickEntity != null) {
               id = mc.crosshairPickEntity.getId();
            }

            var10000 = id < 0 ? null : new ServerboundInteractPacket(id, this.parsedHand(), Vec3.ZERO, false);
            break;
         case CLIENT_COMMAND:
            var10000 = mc.player == null
               ? null
               : new ServerboundPlayerCommandPacket(mc.player, MacroNbtCodecHelper.parseEnum(Action.class, this.playerCommand, Action.OPEN_INVENTORY));
            break;
         case BUNDLE_SELECT:
            var10000 = new ServerboundSelectBundleItemPacket(
               this.slot >= 0 ? this.slot : 36 + (mc.player == null ? 0 : mc.player.getInventory().getSelectedSlot()), this.bundleIndex
            );
            break;
         case USE_ITEM:
            var10000 = new ServerboundUseItemPacket(
               this.parsedHand(), 0, mc.player == null ? 0.0F : mc.player.getYRot(), mc.player == null ? 0.0F : mc.player.getXRot()
            );
            break;
         case RELEASE_ITEM:
            var10000 = new ServerboundPlayerActionPacket(
               net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
            );
            break;
         case SET_CARRIED_ITEM:
            var10000 = new ServerboundSetCarriedItemPacket(Math.max(0, Math.min(8, this.carriedSlot)));
            break;
         case CLIENT_INFORMATION:
            var10000 = new ServerboundClientInformationPacket(ClientInformation.createDefault());
            break;
         case CLOSE_CONTAINER:
            var10000 = new ServerboundContainerClosePacket(this.containerId);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private ContainerInput parsedContainerInput() {
      return MacroNbtCodecHelper.parseEnum(ContainerInput.class, this.containerInput, ContainerInput.PICKUP);
   }

   private InteractionHand parsedHand() {
      return "OFF_HAND".equalsIgnoreCase(this.hand) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PACKET_BURST;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "PACKET_BURST");
      tag.putString("mode", this.mode.name());
      tag.putInt("count", this.count);
      tag.putInt("delayTicks", this.delayTicks);
      tag.putInt("slot", this.slot);
      tag.putInt("button", this.button);
      tag.putString("containerInput", this.containerInput);
      tag.putInt("entityId", this.entityId);
      tag.putString("hand", this.hand);
      tag.putString("clientCommand", this.clientCommand);
      tag.putString("playerCommand", this.playerCommand);
      tag.putInt("bundleIndex", this.bundleIndex);
      tag.putInt("carriedSlot", this.carriedSlot);
      tag.putInt("containerId", this.containerId);
      tag.putBoolean("flushBefore", this.flushBefore);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.mode = MacroNbtCodecHelper.parseEnum(PacketBurstAction.BurstMode.class, tag.getStringOr("mode", "CONTAINER_CLICK"), PacketBurstAction.BurstMode.CONTAINER_CLICK);
      this.count = tag.getIntOr("count", 1);
      this.delayTicks = tag.getIntOr("delayTicks", 0);
      this.slot = tag.getIntOr("slot", 0);
      this.button = tag.getIntOr("button", 0);
      this.containerInput = tag.getStringOr("containerInput", "PICKUP");
      this.entityId = tag.getIntOr("entityId", -1);
      this.hand = tag.getStringOr("hand", "MAIN_HAND");
      this.clientCommand = tag.getStringOr("clientCommand", "REQUEST_STATS");
      this.playerCommand = tag.getStringOr("playerCommand", "OPEN_INVENTORY");
      this.bundleIndex = tag.getIntOr("bundleIndex", 0);
      this.carriedSlot = tag.getIntOr("carriedSlot", 0);
      this.containerId = tag.getIntOr("containerId", 0);
      this.flushBefore = tag.getBooleanOr("flushBefore", false);
   }

   @Override
   public String getDisplayName() {
      return "Packet burst " + this.mode + " x" + this.count;
   }

   @Override
   public String getIcon() {
      return "B";
   }

   public enum BurstMode {
      CONTAINER_CLICK,
      ENTITY_INTERACT,
      CLIENT_COMMAND,
      BUNDLE_SELECT,
      USE_ITEM,
      RELEASE_ITEM,
      SET_CARRIED_ITEM,
      CLIENT_INFORMATION,
      CLOSE_CONTAINER;
   }
}
