package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class ByteReader implements MacroAction {
   private boolean init_Z = true;
   public int delayMs = 0;
   public ByteReader.Inner_StreamUtil mode = ByteReader.Inner_StreamUtil.DISCONNECT;
   public ByteReader.Inner_BufferHelper lagMethod = ByteReader.Inner_BufferHelper.CLICK_SLOT;
   public ByteReader.Inner_CachePool kickMethod = ByteReader.Inner_CachePool.HURT;
   public int packetCount = 200;
   public boolean useNextAction = false;
   private transient List<MacroAction> init_List = null;
   public ByteReader.Inner_DataUtil trigger = ByteReader.Inner_DataUtil.TELEPORT;
   public double targetX = 0.0;
   public double targetY = 0.0;
   public double targetZ = 0.0;
   public double tolerance = 10.0;
   public int bufferMs = 50;
   public int timeoutSec = 60;
   public boolean useExactPosition = false;

   @Override
   public void execute(Minecraft mc) {
      switch (this.mode) {
         case DISCONNECT:
            this.init_V(mc);
            break;
         case KICK:
            this.read_V(mc);
            break;
         case KICK_DUPE:
            this.write(mc);
            break;
         case AUTO_DISCONNECT:
            this.load_V(mc);
      }
   }

   private void init_V(Minecraft mc) {
      if (mc.level != null && mc.getConnection() != null) {
         mc.getConnection().getConnection().disconnect(Component.literal("Disconnected by Macro"));
      }
   }

   private void load_V(Minecraft mc) {
      if (mc.player != null && mc.getConnection() != null) {
         AutismClientMessaging.sendPrefixed("§eAuto Disconnect: Monitoring for " + this.trigger.name() + "...");
         String initialWorld = mc.level != null ? mc.level.dimension().identifier().toString() : "";
         double initialX = mc.player.getX();
         double initialY = mc.player.getY();
         double initialZ = mc.player.getZ();
         Screen initialScreen = mc.gui.screen();
         boolean initialScreenOpen = mc.gui.screen() != null;
         boolean initialInventoryEmpty = this.init_Z_2(mc);
         long startTime = System.currentTimeMillis();
         boolean triggered = false;
         long lastPositionCheck = startTime;
         Screen prevScreen = initialScreen;

         while (!triggered && MacroExecutor.init_Z() && System.currentTimeMillis() - startTime < this.timeoutSec * 1000L) {
            if (mc.player == null || mc.getConnection() == null) {
               AutismClientMessaging.sendPrefixed("§cConnection lost during wait!");
               return;
            }

            switch (this.trigger) {
               case TELEPORT:
                  long now = System.currentTimeMillis();
                  if (now - lastPositionCheck >= 50L) {
                     double dist = Math.sqrt(
                        Math.pow(mc.player.getX() - initialX, 2.0) + Math.pow(mc.player.getY() - initialY, 2.0) + Math.pow(mc.player.getZ() - initialZ, 2.0)
                     );
                     if (dist > 5.0) {
                        AutismClientMessaging.sendPrefixed(String.format("§aTeleport detected! Jumped %.1f blocks.", dist));
                        triggered = true;
                     }

                     if (!triggered && mc.level != null) {
                        String cw = mc.level.dimension().identifier().toString();
                        if (!cw.isEmpty() && !cw.equals(initialWorld)) {
                           AutismClientMessaging.sendPrefixed("§aWorld change detected during teleport!");
                           triggered = true;
                        }
                     }

                     initialX = mc.player.getX();
                     initialY = mc.player.getY();
                     initialZ = mc.player.getZ();
                     lastPositionCheck = now;
                  }
                  break;
               case POSITION:
                  double dx = mc.player.getX() - initialX;
                  double dy = mc.player.getY() - initialY;
                  double dz = mc.player.getZ() - initialZ;
                  double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  if (dist > this.tolerance) {
                     AutismClientMessaging.sendPrefixed(String.format("§aPosition jump detected! Moved %.1f blocks (tolerance: %.0f)", dist, this.tolerance));
                     triggered = true;
                  }

                  long nowPos = System.currentTimeMillis();
                  if (nowPos - lastPositionCheck >= 500L) {
                     initialX = mc.player.getX();
                     initialY = mc.player.getY();
                     initialZ = mc.player.getZ();
                     lastPositionCheck = nowPos;
                  }
                  break;
               case WORLD_CHANGE:
                  String currentWorld = mc.level != null ? mc.level.dimension().identifier().toString() : "";
                  if (!currentWorld.equals(initialWorld) && !currentWorld.isEmpty()) {
                     AutismClientMessaging.sendPrefixed("§aWorld change detected!");
                     triggered = true;
                  }
                  break;
               case GUI_CLOSE:
                  Screen curScreen = mc.gui.screen();
                  if (prevScreen != null && curScreen == null) {
                     AutismClientMessaging.sendPrefixed("§aGUI close detected!");
                     triggered = true;
                  }

                  prevScreen = curScreen;
                  break;
               case INVENTORY_CLEAR:
                  if (!initialInventoryEmpty && this.init_Z_2(mc)) {
                     AutismClientMessaging.sendPrefixed("§aInventory clear detected!");
                     triggered = true;
                  }
            }

            if (!triggered) {
               try {
                  Thread.sleep(5L);
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  return;
               }
            }
         }

         if (!triggered) {
            AutismClientMessaging.sendPrefixed("§cAuto Disconnect: Timeout after " + this.timeoutSec + "s");
            return;
         }

         if (this.bufferMs > 0) {
            AutismClientMessaging.sendPrefixed("§eBuffer delay: " + this.bufferMs + "ms");

            try {
               Thread.sleep(this.bufferMs);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return;
            }
         }

         AutismClientMessaging.sendPrefixed("§cExecuting disconnect!");
         this.init_V(mc);
         return;
      } else {
         AutismClientMessaging.sendPrefixed("§cNo player or network connection!");
      }
   }

   private boolean init_Z_2(Minecraft mc) {
      if (mc.player == null) {
         return true;
      }

      for (int i = 0; i < 36; i++) {
         if (!mc.player.getInventory().getItem(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private void read_V(Minecraft mc) {
      if (mc.player == null || mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("§cNo player or network connection!");
      } else if (this.read_Z(mc)) {
         this.update(mc);
         this.sync(mc);
         this.update(mc);
         AutismClientMessaging.sendPrefixed("§aKick executed!");
      }
   }

   public void init(List<MacroAction> actions) {
      this.init_List = actions;
   }

   private void write(Minecraft mc) {
      if (mc.player == null || mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("§cNo player or network connection!");
      } else if (this.read_Z(mc)) {
         if (this.useNextAction) {
            List<MacroAction> actions = this.init_List;
            this.init_List = null;
            if (actions == null || actions.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cKick Dupe: no next action in macro! Add action(s) after this one.");
               return;
            }

            this.update(mc);

            for (MacroAction act : actions) {
               if (blocksClientThread(act)) {
                  AutismClientMessaging.sendPrefixed("§cKick Dupe: skipped '" + act.getDisplayName() + "', it waits and would freeze the game here.");
                  continue;
               }

               try {
                  act.execute(mc);
               } catch (Exception e) {
                  AutismClientMessaging.sendPrefixed("§cError in dupe action '" + act.getDisplayName() + "': " + e.getMessage());
               }
            }

            this.sync(mc);
            this.update(mc);
            String actNames = actions.size() == 1 ? actions.get(0).getDisplayName() : actions.size() + " actions";
            AutismClientMessaging.sendPrefixed("§aKick Dupe executed! (" + actNames + ")");
         } else {
            this.update(mc);
            if (!this.load_Z(mc)) {
               return;
            }

            this.sync(mc);
            this.update(mc);
            AutismClientMessaging.sendPrefixed("§aKick Dupe executed! (bundle)");
         }
      }
   }

   // The follow-up actions run on the client thread, so anything that waits for a later tick would hang it.
   static boolean blocksClientThread(MacroAction act) {
      return act instanceof ao use && use.holdTicks > 0
         || act instanceof BufferHelper
         || act instanceof UiHelper
         || act instanceof O
         || act instanceof ByteReader reader && reader.mode == ByteReader.Inner_StreamUtil.AUTO_DISCONNECT;
   }

   private boolean load_Z(Minecraft mc) {
      InteractionHand activeInteractionHand = null;
      if (!mc.player.getMainHandItem().isEmpty() && BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).getPath().endsWith("bundle")) {
         activeInteractionHand = InteractionHand.MAIN_HAND;
      } else if (!mc.player.getOffhandItem().isEmpty() && BuiltInRegistries.ITEM.getKey(mc.player.getOffhandItem().getItem()).getPath().endsWith("bundle")) {
         activeInteractionHand = InteractionHand.OFF_HAND;
      }

      if (activeInteractionHand != null) {
         mc.getConnection().send(new ServerboundUseItemPacket(activeInteractionHand, 0, mc.player.getYRot(), mc.player.getXRot()));
         return true;
      }

      int bundleSlot = -1;

      for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("bundle")) {
            bundleSlot = i;
            break;
         }
      }

      if (bundleSlot < 0) {
         AutismClientMessaging.sendPrefixed("§cNo bundle found in inventory or hands!");
         return false;
      }

      int hotbarSlot = bundleSlot;
      if (bundleSlot > 8) {
         int target = 0;

         for (int i = 0; i <= 8; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
               target = i;
               break;
            }
         }

         autismclient.util.aw.read(mc, bundleSlot, target);
         hotbarSlot = target;
      }

      autismclient.util.aw.init_V(mc, hotbarSlot);
      mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, mc.player.getYRot(), mc.player.getXRot()));
      return true;
   }

   private boolean read_Z(Minecraft mc) {
      switch (this.lagMethod) {
         case BOAT_NBT:
            Entity vehicle = mc.player.getVehicle();
            if (!(vehicle instanceof ChestBoat) && !(vehicle instanceof AbstractMinecartContainer)) {
               AutismClientMessaging.sendPrefixed("§cYou must be in a Chest Boat or Minecart with Chest for BoatNBT!");
               return false;
            }
            break;
         case ENTITY_NBT:
            if (mc.hitResult == null || mc.hitResult.getType() != Type.ENTITY) {
               AutismClientMessaging.sendPrefixed("§cYou must be looking at an entity for EntityNBT!");
               return false;
            }

            Entity target = ((EntityHitResult)mc.hitResult).getEntity();
            if (!(target instanceof ChestBoat) && !(target instanceof AbstractMinecartContainer)) {
               AutismClientMessaging.sendPrefixed("§cTarget must be a Chest Boat or Minecart with Chest!");
               return false;
            }
      }

      return true;
   }

   private void update(Minecraft mc) {
      switch (this.lagMethod) {
         case CLICK_SLOT:
            for (int i = 0; i < this.packetCount; i++) {
               mc.getConnection()
                  .send(new ServerboundContainerClickPacket(0, 0, (short)0, (byte)0, ContainerInput.PICKUP, new Int2ObjectArrayMap<>(), HashedStack.EMPTY));
            }
            break;
         case BOAT_NBT:
            for (int i = 0; i < this.packetCount; i++) {
               mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, Action.OPEN_INVENTORY));
            }
            break;
         case ENTITY_NBT:
            if (mc.hitResult instanceof EntityHitResult ehr) {
               Entity target = ehr.getEntity();

               for (int i = 0; i < this.packetCount; i++) {
                  mc.getConnection()
                     .send(
                        new ServerboundInteractPacket(target.getId(), InteractionHand.MAIN_HAND, new Vec3(target.getX(), target.getY(), target.getZ()), true)
                     );
               }
            }
      }
   }

   private void sync(Minecraft mc) {
      switch (this.kickMethod) {
         case HURT:
            mc.getConnection().send(new ServerboundInteractPacket(mc.player.getId(), InteractionHand.MAIN_HAND, Vec3.ZERO, false));
            break;
         case CLIENT_SETTINGS:
            ClientInformation opts = new ClientInformation(
               mc.options.languageCode,
               -2,
               ChatVisiblity.FULL,
               mc.options.chatColors().get(),
               127,
               mc.options.mainHand().get(),
               false,
               false,
               mc.options.particles().get()
            );
            mc.getConnection().send(new ServerboundClientInformationPacket(opts));
            break;
         case INVALID_SLOT:
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(-1));
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("delayMs", this.delayMs);
      tag.putString("mode", this.mode.name());
      tag.putString("lagMethod", this.lagMethod.name());
      tag.putString("kickMethod", this.kickMethod.name());
      tag.putInt("packetCount", this.packetCount);
      tag.putBoolean("useNextAction", this.useNextAction);
      tag.putBoolean("enabled", this.init_Z);
      tag.putString("trigger", this.trigger.name());
      tag.putDouble("targetX", this.targetX);
      tag.putDouble("targetY", this.targetY);
      tag.putDouble("targetZ", this.targetZ);
      tag.putDouble("tolerance", this.tolerance);
      tag.putInt("bufferMs", this.bufferMs);
      tag.putInt("timeoutSec", this.timeoutSec);
      tag.putBoolean("useExactPosition", this.useExactPosition);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("delayMs")) {
         this.delayMs = tag.getIntOr("delayMs", 0);
      }

      if (tag.contains("enabled")) {
         this.init_Z = tag.getBooleanOr("enabled", true);
      }

      if (tag.contains("mode")) {
         try {
            this.mode = ByteReader.Inner_StreamUtil.valueOf(tag.getStringOr("mode", "DISCONNECT"));
         } catch (IllegalArgumentException ignored) {
            this.mode = ByteReader.Inner_StreamUtil.DISCONNECT;
         }
      }

      if (tag.contains("lagMethod")) {
         try {
            this.lagMethod = ByteReader.Inner_BufferHelper.valueOf(tag.getStringOr("lagMethod", "CLICK_SLOT"));
         } catch (IllegalArgumentException ignored) {
            this.lagMethod = ByteReader.Inner_BufferHelper.CLICK_SLOT;
         }
      }

      if (tag.contains("kickMethod")) {
         try {
            this.kickMethod = ByteReader.Inner_CachePool.valueOf(tag.getStringOr("kickMethod", "HURT"));
         } catch (IllegalArgumentException ignored) {
            this.kickMethod = ByteReader.Inner_CachePool.HURT;
         }
      }

      if (tag.contains("packetCount")) {
         this.packetCount = tag.getIntOr("packetCount", 200);
      }

      if (tag.contains("useNextAction")) {
         this.useNextAction = tag.getBooleanOr("useNextAction", false);
      }

      if (tag.contains("trigger")) {
         try {
            this.trigger = ByteReader.Inner_DataUtil.valueOf(tag.getStringOr("trigger", "TELEPORT"));
         } catch (IllegalArgumentException ignored) {
            this.trigger = ByteReader.Inner_DataUtil.TELEPORT;
         }
      }

      if (tag.contains("targetX")) {
         this.targetX = tag.getDoubleOr("targetX", 0.0);
      }

      if (tag.contains("targetY")) {
         this.targetY = tag.getDoubleOr("targetY", 0.0);
      }

      if (tag.contains("targetZ")) {
         this.targetZ = tag.getDoubleOr("targetZ", 0.0);
      }

      if (tag.contains("tolerance")) {
         this.tolerance = tag.getDoubleOr("tolerance", 2.0);
      }

      if (tag.contains("bufferMs")) {
         this.bufferMs = tag.getIntOr("bufferMs", 50);
      }

      if (tag.contains("timeoutSec")) {
         this.timeoutSec = tag.getIntOr("timeoutSec", 60);
      }

      if (tag.contains("useExactPosition")) {
         this.useExactPosition = tag.getBooleanOr("useExactPosition", false);
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.DISCONNECT;
   }

   @Override
   public String getDisplayName() {
      String var3;
      switch (this.mode) {
         case DISCONNECT:
            var3 = this.delayMs > 0 ? "Disconnect (" + this.delayMs + "ms)" : "Disconnect";
            break;
         case KICK:
            var3 = "Kick (" + this.lagMethod.name() + ", " + this.kickMethod.name() + ", " + this.packetCount + ")";
            break;
         case KICK_DUPE:
            String action = this.useNextAction ? "Next Actions" : "Bundle";
            var3 = "Kick Dupe (" + this.lagMethod.name() + ", " + this.kickMethod.name() + ", " + this.packetCount + ", " + action + ")";
            break;
         case AUTO_DISCONNECT:
            switch (this.trigger) {
               case TELEPORT:
                  var3 = "Loading Screen";
                  break;
               case POSITION:
                  var3 = String.format("Pos Jump > %.0f blocks", this.tolerance);
                  break;
               case WORLD_CHANGE:
                  var3 = "World Change";
                  break;
               case GUI_CLOSE:
                  var3 = "GUI Close";
                  break;
               case INVENTORY_CLEAR:
                  var3 = "Inv Clear";
                  break;
               default:
                  throw new MatchException(null, null);
            }

            String triggerInfo = var3;
            var3 = "Auto DC (" + triggerInfo + ", +" + this.bufferMs + "ms)";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var3;
   }

   @Override
   public String getIcon() {
      return this.mode == ByteReader.Inner_StreamUtil.AUTO_DISCONNECT ? "ADC" : "DC";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init_Z = e;
   }

   public enum Inner_BufferHelper {
      CLICK_SLOT,
      BOAT_NBT,
      ENTITY_NBT;
   }

   public enum Inner_CachePool {
      HURT,
      CLIENT_SETTINGS,
      INVALID_SLOT;
   }

   public enum Inner_DataUtil {
      TELEPORT,
      POSITION,
      WORLD_CHANGE,
      GUI_CLOSE,
      INVENTORY_CLEAR;
   }

   public enum Inner_StreamUtil {
      DISCONNECT,
      KICK,
      KICK_DUPE,
      AUTO_DISCONNECT;
   }
}
