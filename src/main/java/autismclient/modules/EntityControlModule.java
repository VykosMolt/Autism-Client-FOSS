package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.compat263.Compat263;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.phys.Vec3;

public final class EntityControlModule extends Module {
   private static final String[] DEFAULT_ENTITIES = new String[]{
      "minecraft:pig",
      "minecraft:strider",
      "minecraft:horse",
      "minecraft:donkey",
      "minecraft:mule",
      "minecraft:skeleton_horse",
      "minecraft:zombie_horse",
      "minecraft:camel",
      "minecraft:happy_ghast",
      "minecraft:oak_boat",
      "minecraft:spruce_boat",
      "minecraft:birch_boat",
      "minecraft:jungle_boat",
      "minecraft:acacia_boat",
      "minecraft:cherry_boat",
      "minecraft:dark_oak_boat",
      "minecraft:pale_oak_boat",
      "minecraft:mangrove_boat",
      "minecraft:bamboo_raft",
      "minecraft:oak_chest_boat",
      "minecraft:spruce_chest_boat",
      "minecraft:birch_chest_boat",
      "minecraft:jungle_chest_boat",
      "minecraft:acacia_chest_boat",
      "minecraft:cherry_chest_boat",
      "minecraft:dark_oak_chest_boat",
      "minecraft:pale_oak_chest_boat",
      "minecraft:mangrove_chest_boat",
      "minecraft:bamboo_chest_raft"
   };
   private static EntityControlModule INSTANCE;
   private final Set<String> allowedEntities = new HashSet<>();
   private String lastEntitiesValue = "";
   private int antiKickDelay;
   private double init_D = 1.7976931348623157E308;
   private boolean init_Z;
   private boolean load;

   public EntityControlModule() {
      super("entity-control", "EntityControl", ae.load_ae, "Controls selected rideable entities.");
      INSTANCE = this;
      this.add(RegistryListSetting.entityTypes("entities", "Entities", defaultEntitiesValue()).group("Control").description("Choose controlled mounts.").build());
      this.add(new BoolSetting("spoof-saddle", "Spoof Saddle", true).group("Control").description("Control without saddles.").build());
      this.add(new BoolSetting("max-jump", "Max Jump", true).group("Control").description("Always charge fully.").build());
      this.add(new BoolSetting("lock-yaw", "Lock Yaw", true).group("Control").description("Match your view.").build());
      this.add(new BoolSetting("cancel-server-packets", "Cancel Server Packets", true).group("Control").description("Ignore server corrections.").build());
      this.add(new BoolSetting("speed", "Speed", false).group("Speed").description("Boost mount speed.").build());
      this.add(
         new DoubleSetting("horizontal-speed", "Horizontal Speed", 10.0, 0.0, 100.0, 0.5)
            .sliderRange(0.0, 50.0)
            .group("Speed")
            .visibleWhen(() -> this.bool("speed"))
            .description("Sets horizontal speed.")
            .build()
      );
      this.add(
         new BoolSetting("only-on-ground", "Only Ground", false)
            .group("Speed")
            .visibleWhen(() -> this.bool("speed"))
            .description("Require ground contact.")
            .build()
      );
      this.add(new BoolSetting("in-water", "In Water", true).group("Speed").visibleWhen(() -> this.bool("speed")).description("Allow water speed.").build());
      this.add(new BoolSetting("fly", "Fly", false).group("Flight").description("Enable mount flight.").build());
      this.add(
         new DoubleSetting("vertical-speed", "Vertical Speed", 6.0, 0.0, 100.0, 0.5)
            .sliderRange(0.0, 20.0)
            .group("Flight")
            .visibleWhen(() -> this.bool("fly"))
            .description("Sets vertical speed.")
            .build()
      );
      this.add(
         new DoubleSetting("fall-speed", "Fall Speed", 0.0, 0.0, 100.0, 0.25)
            .sliderRange(0.0, 20.0)
            .group("Flight")
            .visibleWhen(() -> this.bool("fly"))
            .description("Sets downward drift.")
            .build()
      );
      this.add(new BoolSetting("anti-kick", "Anti Kick", true).group("Flight").visibleWhen(() -> this.bool("fly")).description("Reduce flight kicks.").build());
      this.add(
         new IntSetting("anti-kick-delay", "Anti Kick Delay", 40, 1, 80, 1)
            .group("Flight")
            .visibleWhen(() -> this.bool("fly") && this.bool("anti-kick"))
            .description("Sets anti-kick interval.")
            .build()
      );
   }

   @Override
   public void onEnable() {
      this.resetAntiKick();
      this.refreshEntityFilter();
   }

   @Override
   public void onDisable() {
      this.resetAntiKick();
   }

   @Override
   public void onGameLeft() {
      this.resetAntiKick();
   }

   @Override
   protected void onOptionValueChanged(String optionId) {
      if ("entities".equals(optionId)) {
         this.refreshEntityFilter();
      }

      if ("anti-kick-delay".equals(optionId)) {
         this.antiKickDelay = this.integer("anti-kick-delay");
      }
   }

   @Override
   protected void onSettingsReset() {
      this.refreshEntityFilter();
      this.resetAntiKick();
   }

   @Override
   public void preMovementTick() {
      if (MC.player != null && MC.getConnection() != null) {
         if (this.init_Z) {
            Entity vehicle = MC.player.getVehicle();
            if (this.load(vehicle)) {
               this.init_V_ServerboundMoveVeh(
                  Compat263.newServerboundMoveVehiclePacket(
                     new Vec3(vehicle.getX(), this.init_D, vehicle.getZ()), vehicle.getYRot(), vehicle.getXRot(), vehicle.onGround()
                  )
               );
            }

            this.init_Z = false;
         }

         if (this.antiKickDelay > 0) {
            this.antiKickDelay--;
         }
      }
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (this.load || !this.bool("fly") || !this.bool("anti-kick")) {
         return false;
      }

      if (packet instanceof ServerboundMoveVehiclePacket movePacket) {
         Entity vehicle = MC.player == null ? null : MC.player.getVehicle();
         if (this.load(vehicle) && !vehicle.isFlyingVehicle() && write(vehicle)) {
            double currentY = Compat263.sbMoveVehiclePos(movePacket).y;
            if (this.antiKickDelay <= 0 && !this.init_Z && this.init_Z_D(currentY)) {
               double baseline = this.init_D == 1.7976931348623157E308 ? currentY : this.init_D;
               ServerboundMoveVehiclePacket lowered = Compat263.newServerboundMoveVehiclePacket(
                  new Vec3(Compat263.sbMoveVehiclePos(movePacket).x, baseline - 0.0313, Compat263.sbMoveVehiclePos(movePacket).z),
                  Compat263.sbMoveVehicleYRot(movePacket),
                  Compat263.sbMoveVehicleXRot(movePacket),
                  movePacket.onGround()
               );
               this.init_V_ServerboundMoveVeh(lowered);
               this.init_Z = true;
               this.antiKickDelay = this.integer("anti-kick-delay");
               this.init_D = currentY;
               return true;
            } else {
               this.init_D = currentY;
               return false;
            }
         } else {
            this.init_D = Compat263.sbMoveVehiclePos(movePacket).y;
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      return this.bool("cancel-server-packets") && packet instanceof ClientboundMoveVehiclePacket;
   }

   static Vec3 init(Entity vehicle, MoverType type, Vec3 movement) {
      EntityControlModule module = INSTANCE;
      if (isActiveFor(module) && type == MoverType.SELF && module.load(vehicle)) {
         double velocityX = movement.x;
         double velocityY = movement.y;
         double velocityZ = movement.z;
         if (module.bool("lock-yaw")) {
            vehicle.setYRot(MC.player.getYRot());
         }

         if (module.bool("speed")
            && (!module.bool("only-on-ground") || vehicle.onGround() || vehicle.isFlyingVehicle())
            && (module.bool("in-water") || !vehicle.isInWater())) {
            Vec3 horizontal = inputVelocity(module.decimal("horizontal-speed"));
            velocityX = horizontal.x;
            velocityZ = horizontal.z;
         }

         if (module.bool("fly")) {
            velocityY = -module.decimal("fall-speed") / 20.0;
            if (MC.options.keyJump.isDown()) {
               velocityY += module.decimal("vertical-speed") / 20.0;
            }

            if (MC.options.keySprint.isDown()) {
               velocityY -= module.decimal("vertical-speed") / 20.0;
            }
         }

         return new Vec3(velocityX, velocityY, velocityZ);
      } else {
         return movement;
      }
   }

   public static boolean init_Z_7() {
      EntityControlModule module = INSTANCE;
      return isActiveFor(module) && module.bool("lock-yaw") && module.load(MC.player == null ? null : MC.player.getVehicle());
   }

   public static boolean spoofsSaddle(Mob mob) {
      EntityControlModule module = INSTANCE;
      return isActiveFor(module) && module.bool("spoof-saddle") && module.read(mob);
   }

   public static boolean load_Z_3() {
      EntityControlModule module = INSTANCE;
      Entity vehicle = MC.player == null ? null : MC.player.getVehicle();
      return isActiveFor(module) && module.bool("max-jump") && module.load(vehicle);
   }

   public static boolean read() {
      EntityControlModule module = INSTANCE;
      Entity vehicle = MC.player == null ? null : MC.player.getVehicle();
      return isActiveFor(module) && module.bool("fly") && vehicle instanceof PlayerRideableJumping && module.load(vehicle);
   }

   public static boolean init_Z_Entity_2(Entity entity) {
      EntityControlModule module = INSTANCE;
      return isActiveFor(module)
         && entity != null
         && MC.player != null
         && MC.player.getVehicle() == entity
         && MC.player == entity.getFirstPassenger()
         && module.read(entity);
   }

   private static String defaultEntitiesValue() {
      StringBuilder out = new StringBuilder();

      for (String id : DEFAULT_ENTITIES) {
         Identifier parsed = Identifier.tryParse(id);
         if (parsed != null && BuiltInRegistries.ENTITY_TYPE.getOptional(parsed).isPresent()) {
            if (!out.isEmpty()) {
               out.append('|');
            }

            out.append(id);
         }
      }

      return out.toString();
   }

   private static boolean isActiveFor(EntityControlModule module) {
      return module != null && module.isEnabled() && !aD.read_Z() && MC.player != null;
   }

   private boolean load(Entity vehicle) {
      return vehicle != null && MC.player != null && MC.player.getVehicle() == vehicle && vehicle.getControllingPassenger() == MC.player && this.read(vehicle);
   }

   private boolean read(Entity entity) {
      if (entity == null) {
         return false;
      }

      this.refreshEntityFilter();
      Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return id != null && this.allowedEntities.contains(id.toString());
   }

   private void refreshEntityFilter() {
      String current = this.value("entities");
      if (!current.equals(this.lastEntitiesValue)) {
         this.lastEntitiesValue = current;
         this.allowedEntities.clear();
         this.allowedEntities.addAll(this.list("entities"));
      }
   }

   private void resetAntiKick() {
      this.antiKickDelay = this.integer("anti-kick-delay");
      this.init_D = 1.7976931348623157E308;
      this.init_Z = false;
      this.load = false;
   }

   private void init_V_ServerboundMoveVeh(ServerboundMoveVehiclePacket packet) {
      if (MC.getConnection() != null) {
         this.load = true;

         try {
            MC.getConnection().send(packet);
         } finally {
            this.load = false;
         }
      }
   }

   private boolean init_Z_D(double currentY) {
      return currentY >= this.init_D || this.init_D - currentY < 0.0313;
   }

   private static boolean write(Entity entity) {
      return entity.level().getBlockStates(entity.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockStateBase::isAir);
   }

   private static Vec3 inputVelocity(double blocksPerSecond) {
      double speed = blocksPerSecond / 20.0;
      float forward = 0.0F;
      float sideways = 0.0F;
      if (MC.options.keyUp.isDown()) {
         forward++;
      }

      if (MC.options.keyDown.isDown()) {
         forward--;
      }

      if (MC.options.keyLeft.isDown()) {
         sideways++;
      }

      if (MC.options.keyRight.isDown()) {
         sideways--;
      }

      if (forward == 0.0F && sideways == 0.0F) {
         return Vec3.ZERO;
      }

      double length = Math.sqrt(forward * forward + sideways * sideways);
      forward /= (float)length;
      sideways /= (float)length;
      double yaw = Math.toRadians(MC.player.getYRot());
      double sin = Math.sin(yaw);
      double cos = Math.cos(yaw);
      return new Vec3((sideways * cos - forward * sin) * speed, 0.0, (forward * cos + sideways * sin) * speed);
   }
}
