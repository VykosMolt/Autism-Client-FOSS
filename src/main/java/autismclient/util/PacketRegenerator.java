package autismclient.util;

import autismclient.AutismClientAddon;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PacketRegenerator {
   private static final Map<Class<? extends Packet<?>>, PacketRegenerator.Inner_DataUtil<?>> init = new HashMap<>();

   public static <T extends Packet<?>> T init(T original) {
      if (original == null) {
         return null;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return original;
      }

      PacketRegenerator.Inner_DataUtil<T> handler = (PacketRegenerator.Inner_DataUtil<T>)init.get(original.getClass());
      if (handler != null) {
         try {
            return handler.regenerate(original, mc);
         } catch (Exception e) {
            AutismClientAddon.LOG.error("[Autism] Failed to regenerate {}: {}", original.getClass().getSimpleName(), e.getMessage());
            return original;
         }
      } else {
         return original;
      }
   }

   private static void init() {
      init(
         ServerboundPlayerActionPacket.class, (packet, mc) -> new ServerboundPlayerActionPacket(packet.getAction(), packet.getPos(), packet.getDirection(), 0)
      );
      init(
         ServerboundUseItemOnPacket.class,
         (packet, mc) -> {
            BlockPos targetBlock = packet.hitResult().getBlockPos();
            Direction face = packet.hitResult().getDirection();
            Vec3 eyePos = mc.player.getEyePosition();
            Vec3 hitPos = new Vec3(
               targetBlock.getX() + 0.5 + face.getStepX() * 0.5,
               targetBlock.getY() + 0.5 + face.getStepY() * 0.5,
               targetBlock.getZ() + 0.5 + face.getStepZ() * 0.5
            );
            BlockHitResult newHit = new BlockHitResult(hitPos, face, targetBlock, false);
            return new ServerboundUseItemOnPacket(packet.hand(), newHit, 0);
         }
      );
      init(ServerboundContainerClickPacket.class, (packet, mc) -> {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler == null) {
            return packet;
         }

         int currentSyncId = handler.containerId;
         int currentRevision = handler.getStateId();

         try {
            int slot = init(packet, "slot", short.class, 0);
            int button = init(packet, "button", byte.class, 0);
            ContainerInput action = init(packet, ContainerInput.class);
            Int2ObjectMap<HashedStack> modifiedStacks = new Int2ObjectArrayMap<>();
            return new ServerboundContainerClickPacket(currentSyncId, currentRevision, (short)slot, (byte)button, action, modifiedStacks, HashedStack.EMPTY);
         } catch (Exception e) {
            AutismClientAddon.LOG.error("[Autism] ClickSlot regeneration failed: {}", e.getMessage());
            return packet;
         }
      });
      init(ServerboundContainerButtonClickPacket.class, (packet, mc) -> {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler == null) {
            return packet;
         }

         int buttonId = init(packet, "buttonId", int.class, 1);
         return new ServerboundContainerButtonClickPacket(handler.containerId, buttonId);
      });
      init(ServerboundUseItemPacket.class, (packet, mc) -> new ServerboundUseItemPacket(packet.hand(), 0, mc.player.getYRot(), mc.player.getXRot()));
      init(
         PosRot.class,
         (packet, mc) -> new PosRot(
            mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision
         )
      );
      init(Pos.class, (packet, mc) -> new Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.onGround(), mc.player.horizontalCollision));
      init(Rot.class, (packet, mc) -> new Rot(mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision));
      init(StatusOnly.class, (packet, mc) -> new StatusOnly(mc.player.onGround(), mc.player.horizontalCollision));
      init(
         ServerboundInteractPacket.class,
         (packet, mc) -> {
            int originalEntityId = packet.entityId();
            Entity originalEntity = mc.level != null ? mc.level.getEntity(originalEntityId) : null;
            if (originalEntity == null && mc.level != null) {
               Vec3 playerPos = mc.player.getEyePosition().subtract(0.0, mc.player.getEyeHeight(), 0.0);
               AABB searchAABB = new AABB(playerPos.subtract(5.0, 5.0, 5.0), playerPos.add(5.0, 5.0, 5.0));
               List<Entity> nearby = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != null && entity != mc.player && searchAABB.contains(entity.position())) {
                     nearby.add(entity);
                  }
               }

               if (!nearby.isEmpty()) {
                  originalEntity = nearby.stream().min((a, b) -> Double.compare(a.distanceToSqr(playerPos), b.distanceToSqr(playerPos))).orElse(null);
               }
            }

            return originalEntity != null
               ? new ServerboundInteractPacket(originalEntity.getId(), packet.hand(), packet.location(), packet.usingSecondaryAction())
               : packet;
         }
      );
      init(
         ServerboundAttackPacket.class,
         (packet, mc) -> {
            int originalEntityId = packet.entityId();
            Entity originalEntity = mc.level != null ? mc.level.getEntity(originalEntityId) : null;
            if (originalEntity == null && mc.level != null) {
               Vec3 playerPos = mc.player.getEyePosition().subtract(0.0, mc.player.getEyeHeight(), 0.0);
               AABB searchAABB = new AABB(playerPos.subtract(5.0, 5.0, 5.0), playerPos.add(5.0, 5.0, 5.0));
               List<Entity> nearby = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != null && entity != mc.player && searchAABB.contains(entity.position())) {
                     nearby.add(entity);
                  }
               }

               if (!nearby.isEmpty()) {
                  originalEntity = nearby.stream().min((a, b) -> Double.compare(a.distanceToSqr(playerPos), b.distanceToSqr(playerPos))).orElse(null);
               }
            }

            return originalEntity != null ? new ServerboundAttackPacket(originalEntity.getId()) : packet;
         }
      );
      init(ServerboundSetCarriedItemPacket.class, (packet, mc) -> packet);
      init(ServerboundContainerClosePacket.class, (packet, mc) -> {
         AbstractContainerMenu handler = mc.player.containerMenu;
         return handler == null ? packet : new ServerboundContainerClosePacket(handler.containerId);
      });
      init(ServerboundChatPacket.class, (packet, mc) -> packet);
      init(ServerboundCustomPayloadPacket.class, (packet, mc) -> {
         CustomPacketPayload payload = packet.payload();
         if (payload == null) {
            return null;
         }

         String channel = AutismPayloadSupport.init_Str(payload);
         byte[] rawBytes = AutismPayloadSupport.load(payload);
         if (channel != null && !channel.isBlank()) {
            return AutismPayloadSupport.init_ServerboundCustomP(channel, rawBytes);
         }

         AutismClientAddon.LOG.warn("[Autism] Cannot regenerate ServerboundCustomPayloadPacket - channel is blank");
         return null;
      });
   }

   private static <T extends Packet<?>> void init(Class<T> clazz, PacketRegenerator.Inner_DataUtil<T> handler) {
      init.put(clazz, handler);
   }

   private static int init(Object obj, String hint, Class<?> type, int index) {
      try {
         Field field = init(obj.getClass(), type, index);
         if (type == byte.class) {
            return field.getByte(obj);
         } else {
            return type == short.class ? field.getShort(obj) : field.getInt(obj);
         }
      } catch (Exception e) {
         return 0;
      }
   }

   private static void init(Object obj, String hint, Class<?> type, int index, int value) throws IllegalAccessException, NoSuchFieldException {
      Field field = init(obj.getClass(), type, index);
      field.setInt(obj, value);
   }

   private static String init(Object obj, String hint, int index) {
      try {
         Field field = init(obj.getClass(), String.class, index);
         return (String)field.get(obj);
      } catch (Exception e) {
         return null;
      }
   }

   private static <E extends Enum<E>> E init(Object obj, Class<E> enumClass) {
      try {
         Field field = init(obj.getClass(), enumClass, 0);
         return (E)field.get(obj);
      } catch (Exception e) {
         return null;
      }
   }

   private static Field init(Class<?> clazz, Class<?> type, int index) throws NoSuchFieldException {
      List<Field> matches = new ArrayList<>();

      for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
         for (Field f : current.getDeclaredFields()) {
            if (f.getType().equals(type)) {
               matches.add(f);
            }
         }
      }

      if (index >= 0 && index < matches.size()) {
         Field f = matches.get(index);
         f.setAccessible(true);
         return f;
      } else {
         throw new NoSuchFieldException("Field of type " + type.getName() + " at index " + index + " not found");
      }
   }

   static {
      init();
   }

   @FunctionalInterface
   public interface Inner_DataUtil<T extends Packet<?>> {
      T regenerate(T var1, Minecraft var2);
   }
}
