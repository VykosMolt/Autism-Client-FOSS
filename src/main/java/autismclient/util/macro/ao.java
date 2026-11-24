package autismclient.util.macro;

import autismclient.compat263.CompatSwingPacket;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ao implements MacroAction {
   public ao.Inner_DataUtil phase = ao.Inner_DataUtil.init;
   public String itemName = "";
   public String hand = "MAIN_HAND";
   public int holdTicks = 0;
   public int repeat = 1;
   public boolean gateDuringHold = false;
   public boolean gatePlayerActions = true;
   public boolean gateContainerClicks = true;
   public boolean releaseAfterHold = false;
   public boolean useCustomSlotMapping = true;
   public int swapSlotBeforeRelease = -1;
   public int swapButton = 0;
   public boolean dropSlotAfterRelease = false;
   public int dropSlot = 44;
   public String gateId = "use-phase";

   @Override
   public void execute(Minecraft mc) {
      int times = this.init_I_6();
      boolean stopped = false;

      for (int i = 0; i < times && !stopped; i++) {
         this.init_V_2(mc);
         boolean gateInstalled = this.init_Z_4(mc);

         try {
            for (int t = 0; t < this.holdTicks; t++) {
               if (!MacroExecutor.init_Z()) {
                  stopped = true;
                  break;
               }

               try {
                  b.load_CompletableFuture().get(100L, TimeUnit.MILLISECONDS);
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  stopped = true;
                  break;
               } catch (Exception ignored) {
               }
            }
         } finally {
            if (gateInstalled) {
               this.init_V_14();
            }
         }

         if (!stopped) {
            this.load(mc);
         }
      }
   }

   public int init_I_6() {
      return Math.max(1, Math.min(1000, this.repeat));
   }

   public boolean init_Z_8() {
      return this.gateDuringHold && this.holdTicks > 0;
   }

   private InteractionHand init_InteractionHand() {
      return "OFF_HAND".equalsIgnoreCase(this.hand) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   public void init_V_2(Minecraft mc) {
      if (mc != null && mc.player != null && mc.getConnection() != null) {
         ArrayUtil target = ArrayUtil.update(this.itemName);
         if (target.update()) {
            autismclient.util.aw.init(mc, target, mc.player.getInventory().getSelectedSlot());
         }

         InteractionHand h = this.init_InteractionHand();
         switch (this.phase) {
            case init:
            case load:
               if (mc.gameMode != null) {
                  mc.gameMode.useItem(mc.player, h);
               } else {
                  this.init(mc, h);
               }
               break;
            case read:
               mc.getConnection().send(new ServerboundPlayerActionPacket(Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
               break;
            case write:
               if (mc.hitResult instanceof BlockHitResult bhr && mc.hitResult.getType() == Type.BLOCK) {
                  if (mc.gameMode != null) {
                     mc.gameMode.useItemOn(mc.player, h, bhr);
                  } else {
                     mc.getConnection().send(new ServerboundUseItemOnPacket(h, bhr, 0));
                  }
               } else if (mc.gameMode != null) {
                  mc.gameMode.useItem(mc.player, h);
               } else {
                  this.init(mc, h);
               }
               break;
            case update:
               mc.getConnection().send(new CompatSwingPacket(h));
         }
      }
   }

   private void init(Minecraft mc, InteractionHand hand) {
      mc.getConnection().send(new ServerboundUseItemPacket(hand, 0, mc.player.getYRot(), mc.player.getXRot()));
   }

   public boolean init_Z_4(Minecraft mc) {
      if (!this.init_Z_8()) {
         return false;
      }

      B gate = new B();
      gate.gateId = this.gateId;
      gate.mode = B.Inner_CachePool.init;
      if (this.gatePlayerActions) {
         gate.packetNames.add("ServerboundPlayerActionPacket");
      }

      if (this.gateContainerClicks) {
         gate.packetNames.add("ServerboundContainerClickPacket");
      }

      gate.execute(mc);
      return true;
   }

   public void init_V_14() {
      C.init_V(this.gateId);
   }

   public void load(Minecraft mc) {
      if (mc != null && mc.player != null && mc.getConnection() != null) {
         if (this.swapSlotBeforeRelease >= 0) {
            this.init(mc, this.swapSlotBeforeRelease, this.swapButton, ContainerInput.SWAP);
         }

         if (this.releaseAfterHold) {
            mc.getConnection().send(new ServerboundPlayerActionPacket(Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
         }

         if (this.dropSlotAfterRelease) {
            this.init(mc, this.dropSlot, 0, ContainerInput.THROW);
         }
      }
   }

   private void init(Minecraft mc, int slot, int button, ContainerInput input) {
      int handlerSlot = this.useCustomSlotMapping ? autismclient.util.aw.read_I(mc, slot) : slot;
      if (handlerSlot >= 0) {
         autismclient.util.av.init(mc, handlerSlot, button, input);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_z_StreamGate;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "USE_ITEM_PHASE");
      tag.putString("phase", this.phase.name());
      tag.putString("itemName", this.itemName);
      tag.putString("hand", this.hand);
      tag.putInt("holdTicks", this.holdTicks);
      tag.putInt("repeat", this.repeat);
      tag.putBoolean("gateDuringHold", this.gateDuringHold);
      tag.putBoolean("gatePlayerActions", this.gatePlayerActions);
      tag.putBoolean("gateContainerClicks", this.gateContainerClicks);
      tag.putBoolean("releaseAfterHold", this.releaseAfterHold);
      tag.putBoolean("useCustomSlotMapping", this.useCustomSlotMapping);
      tag.putInt("swapSlotBeforeRelease", this.swapSlotBeforeRelease);
      tag.putInt("swapButton", this.swapButton);
      tag.putBoolean("dropSlotAfterRelease", this.dropSlotAfterRelease);
      tag.putInt("dropSlot", this.dropSlot);
      tag.putString("gateId", this.gateId);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.phase = i.init(ao.Inner_DataUtil.class, tag.getStringOr("phase", "USE_ONCE"), ao.Inner_DataUtil.init);
      this.itemName = tag.getStringOr("itemName", "");
      this.hand = tag.getStringOr("hand", "MAIN_HAND");
      this.holdTicks = tag.getIntOr("holdTicks", 0);
      this.repeat = tag.getIntOr("repeat", 1);
      this.gateDuringHold = tag.getBooleanOr("gateDuringHold", false);
      this.gatePlayerActions = tag.getBooleanOr("gatePlayerActions", true);
      this.gateContainerClicks = tag.getBooleanOr("gateContainerClicks", true);
      this.releaseAfterHold = tag.getBooleanOr("releaseAfterHold", false);
      this.useCustomSlotMapping = tag.getBooleanOr("useCustomSlotMapping", true);
      this.swapSlotBeforeRelease = tag.getIntOr("swapSlotBeforeRelease", -1);
      this.swapButton = tag.getIntOr("swapButton", 0);
      this.dropSlotAfterRelease = tag.getBooleanOr("dropSlotAfterRelease", false);
      this.dropSlot = tag.getIntOr("dropSlot", 44);
      this.gateId = tag.getStringOr("gateId", "use-phase");
   }

   @Override
   public String getDisplayName() {
      return "Use item " + this.phase + (this.releaseAfterHold ? " + release" : "");
   }

   @Override
   public String getIcon() {
      return "U";
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
