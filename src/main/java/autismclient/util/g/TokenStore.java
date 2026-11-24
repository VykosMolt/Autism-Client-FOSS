package autismclient.util.g;

import autismclient.compat263.CompatSwingPacket;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.bi;
import autismclient.util.di;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class TokenStore {
   private static ClassCache init_ClassCache;
   private static CharEncoder init_CharEncoder;
   private static int init_I = -1;
   private static boolean init_Z;
   private static Boolean init_Boolean;
   private static String init_Str = "";
   private static int load_I;
   private static int read_I;
   private static double init_D = 0.0 / 0.0;
   private static int write_I;
   private static int update_I;
   private static boolean load_Z;
   private static double load_D;
   private static long init_J = -9223372036854775808L;
   private static long load_J;
   private static int sync;
   private static double read_D = 0.2873;
   private static double write_D;
   private static Vec3 init_Vec3;
   private static long read_J;
   private static BlockPos init_BlockPos;
   private static int flush = -1;
   private static boolean read_Z;
   private static int close = -1;
   private static long write_J;
   private static long update_J = 9223372036854775807L;
   private static BlockPos load_BlockPos;
   private static boolean write_Z;
   private static BlockPos read_BlockPos;
   private static Direction init_Direction = Direction.UP;
   private static int open;
   private static int poll;

   private TokenStore() {
   }

   static void init_V_ClassCache_RemotePlayer(ClassCache active, RemotePlayer bot) {
      init_ClassCache = active;
      init_I = bot == null ? -1 : bot.getId();
      init_CharEncoder = CharEncoder.init_CharEncoder_ClassCache_I(active, init_I);
      init_V();
      Minecraft mc = Minecraft.getInstance();
      if (init_Z("flight") && mc.player != null && !mc.player.isSpectator()) {
         mc.player.getAbilities().flying = false;
         mc.player.getAbilities().setFlyingSpeed(0.05F);
         if (!mc.player.getAbilities().instabuild) {
            mc.player.getAbilities().mayfly = false;
         }
      }
   }

   static void init_V(RemotePlayer bot) {
      load(true);
      write(bot);
      init_ClassCache = null;
      init_I = -1;
      init_CharEncoder = CharEncoder.init_CharEncoder_ClassCache_I(null, -1);
      init_V();
   }

   static void load(RemotePlayer bot) {
      load(false);
      write(bot);
      load_Z = false;
      update_I = 0;
      load_BlockPos = null;
      write_Z = false;
      read_BlockPos = null;
      poll = 0;
   }

   private static void init_V() {
      init_Z = false;
      init_Boolean = null;
      init_Str = "";
      load_I = 0;
      read_I = 0;
      init_D = 0.0 / 0.0;
      write_I = 0;
      update_I = 0;
      load_Z = false;
      load_D = 0.0;
      init_J = -9223372036854775808L;
      sync = 0;
      read_D = 0.2873;
      write_D = 0.0;
      init_Vec3 = null;
      read_J = 0L;
      init_BlockPos = null;
      flush = -1;
      read_Z = false;
      close = -1;
      write_J = 0L;
      update_J = 9223372036854775807L;
      load_BlockPos = null;
      write_Z = false;
      read_BlockPos = null;
      init_Direction = Direction.UP;
      open = 0;
      poll = 0;
   }

   public static boolean init(Entity entity) {
      CharEncoder active = init_CharEncoder;
      return init_ClassCache != null && active != null && active.init(entity) && entity.getId() == init_I && PoolEntry.load(entity);
   }

   static void init_V_RemotePlayer(RemotePlayer bot, boolean jump, boolean shift) {
      if (init(bot)) {
         sync(bot);
         Module flight = init_Module("flight");
         if (flight == null) {
            write(bot);
            init_Str = "";
         } else {
            String mode = init_Str_Module(flight, "mode", "Abilities");
            if (!mode.equals(init_Str)) {
               write(bot);
               init_Str = mode;
               load_I = init_I_Module(flight, "delay", 20);
               read_I = 0;
               init_D = 0.0 / 0.0;
               load_D = bot.getY();
               write_I = 0;
               update_I = 0;
               load_Z = bot.isFallFlying();
            }

            if (write_I > 0) {
               write_I--;
            }

            if (update_I > 0) {
               update_I--;
            }

            switch (mode) {
               case "Velocity":
                  load(bot, flight, jump, shift);
                  break;
               case "Vulcan":
                  read(bot, flight, jump, shift);
                  break;
               default:
                  init(bot, flight, jump, shift);
            }
         }
      }
   }

   private static void init(RemotePlayer bot, Module flight, boolean jump, boolean shift) {
      init(flight);
      boolean flying = read_I <= 0;
      bot.getAbilities().setFlyingSpeed((float)init_D_Module(flight, "speed", 0.1));
      bot.getAbilities().flying = flying;
      if (!bot.getAbilities().instabuild) {
         bot.getAbilities().mayfly = true;
      }

      init_Z = true;
      if (flying) {
         double vertical = init_D_D(bot.getAbilities().getFlyingSpeed(), jump, shift);
         if (vertical != 0.0) {
            bot.setDeltaMovement(bot.getDeltaMovement().add(0.0, vertical, 0.0));
         }
      }

      init_V_RemotePlayer_Z(bot, flying);
   }

   static double init_D_D(double flyingSpeed, boolean jump, boolean shift) {
      return ((jump ? 1.0 : 0.0) - (shift ? 1.0 : 0.0)) * flyingSpeed * 3.0;
   }

   private static void load(RemotePlayer bot, Module flight, boolean jump, boolean shift) {
      init(flight);
      bot.getAbilities().flying = false;
      if (!bot.getAbilities().instabuild) {
         bot.getAbilities().mayfly = false;
      }

      init_Z = true;
      double speed = init_D_Module(flight, "speed", 0.1);
      double vertical = speed * (init_Z_Module(flight, "vertical-speed-match", false) ? 10.0 : 5.0);
      double y = 0.0;
      if (jump) {
         y += vertical;
      }

      if (shift && !init_Z_Module(flight, "no-sneak", false)) {
         y -= vertical;
      }

      bot.setDeltaMovement(0.0, y, 0.0);
      init_V_RemotePlayer_Z(bot, false);
   }

   private static void init(Module flight) {
      if (!"Normal".equals(init_Str_Module(flight, "anti-kick-mode", "Packet"))) {
         read_I = 0;
      } else {
         if (read_I > 0) {
            read_I--;
         }

         if (load_I > 0) {
            load_I--;
         }

         if (load_I <= 0) {
            load_I = Math.max(1, init_I_Module(flight, "delay", 20));
            read_I = Math.max(1, init_I_Module(flight, "off-time", 1));
         }
      }
   }

   private static void read(RemotePlayer bot, Module flight, boolean jump, boolean shift) {
      update(bot);
      if (init_Z_RemotePlayer_Module(bot, flight)) {
         if (bot.onGround()) {
            load_Z = false;
         }

         if (bot.onGround() && write_I <= 0) {
            bot.jumpFromGround();
            write_I = 3;
         }

         read(bot);
         if (jump) {
            load_D = Math.max(load_D + (bot.isFallFlying() ? 0.04 : 0.12), bot.getY());
            if ((!bot.isFallFlying() || bot.getY() < load_D - 0.08 || bot.getDeltaMovement().y < -0.055) && write_I <= 0) {
               bot.jumpFromGround();
               write_I = bot.isFallFlying() ? 12 : 3;
            }
         } else if (shift) {
            load_D = bot.getY() - 0.35;
         } else if (bot.getY() < load_D - 0.22 && bot.getDeltaMovement().y < -0.02 && write_I <= 0) {
            bot.jumpFromGround();
            write_I = bot.isFallFlying() ? 12 : 3;
         }
      }
   }

   private static void read(RemotePlayer bot) {
      if (init_ClassCache != null && !bot.onGround() && !bot.isFallFlying() && !load_Z && update_I <= 0) {
         bot.tryToStartFallFlying();
         if (init_ClassCache.init_Z_Entity_ServerboundPlayerC(bot, Action.START_FALL_FLYING)) {
            load_Z = true;
            update_I = 20;
         }
      }
   }

   private static boolean init_Z_RemotePlayer_Module(RemotePlayer bot, Module flight) {
      ItemStack chest = bot.getItemBySlot(EquipmentSlot.CHEST);
      boolean antiBreak = init_Z_Module(flight, "anti-break-elytra", true);
      if (!init_Z(chest) || antiBreak && !load(chest)) {
         int replacement = init_I_RemotePlayer_Z(bot, antiBreak);
         if (replacement >= 0 && init_ClassCache != null) {
            long revision = init_ClassCache.open_J();
            if (revision != init_J) {
               init_J = revision;
               init_ClassCache.read_Z_I(replacement);
            }

            return false;
         } else {
            long now = System.currentTimeMillis();
            if (now - load_J >= 2500L) {
               load_J = now;
               bi.init("POV Flight needs a usable bot elytra.", -42393);
            }

            return false;
         }
      } else {
         return true;
      }
   }

   private static int init_I_RemotePlayer_Z(RemotePlayer bot, boolean safeOnly) {
      int best = -1;
      int durability = -1;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = bot.getInventory().getItem(slot);
         if (init_Z(stack) && (!safeOnly || load(stack))) {
            int remaining = init_I(stack);
            if (remaining > durability) {
               best = slot;
               durability = remaining;
            }
         }
      }

      return best;
   }

   private static boolean init_Z(ItemStack stack) {
      return stack != null && stack.is(Items.ELYTRA) && !stack.isBroken() && init_I(stack) > 0;
   }

   private static boolean load(ItemStack stack) {
      int threshold = Math.max(40, (int)Math.ceil(stack.getMaxDamage() * 0.1));
      return init_Z(stack) && init_I(stack) > threshold;
   }

   private static int init_I(ItemStack stack) {
      return stack != null && stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 2147483647;
   }

   private static void init_V_RemotePlayer_Z(RemotePlayer bot, boolean flying) {
      if (init_ClassCache != null && !Objects.equals(init_Boolean, flying)) {
         init_Boolean = flying;
         init_ClassCache.init_Z_Abilities(bot.getAbilities());
      }
   }

   private static void write(RemotePlayer bot) {
      if (bot != null) {
         update(bot);
         if ("Vulcan".equals(init_Str) && bot.isFallFlying()) {
            bot.stopFallFlying();
         }
      }
   }

   private static void update(RemotePlayer bot) {
      if (bot != null) {
         if (init_Z) {
            bot.getAbilities().setFlyingSpeed(0.05F);
            bot.getAbilities().flying = false;
            if (!bot.getAbilities().instabuild) {
               bot.getAbilities().mayfly = false;
            }

            if (init_ClassCache != null) {
               init_ClassCache.init_Z_Abilities(bot.getAbilities());
            }
         }

         init_Z = false;
         init_Boolean = null;
      }
   }

   static boolean init_Z(RemotePlayer bot) {
      return init(bot) && init_Z("sneak") && !bot.getAbilities().flying;
   }

   static boolean init(
      boolean forward, boolean back, boolean left, boolean right, boolean sneaking, boolean usingItem, boolean hungerOk, boolean horizontalCollision
   ) {
      if (!sneaking && !usingItem && hungerOk) {
         Module sprint = init_Module("sprint");
         boolean omni = sprint != null
            && ("Omnidirectional".equals(init_Str_Module(sprint, "mode", "Legit")) || "Omnirotational".equals(init_Str_Module(sprint, "mode", "Legit")));
         return !horizontalCollision && (forward || omni && (back || left || right));
      } else {
         return false;
      }
   }

   public static float init_F(Player player) {
      if (!init(player)) {
         return -1.0F;
      }

      Module flight = init_Module("flight");
      return flight != null && "Velocity".equals(init_Str_Module(flight, "mode", "Abilities"))
         ? (float)init_D_Module(flight, "speed", 0.1) * (player.isSprinting() ? 15.0F : 10.0F)
         : -1.0F;
   }

   public static boolean init_Z(Player player) {
      Module flight = init(player) ? init_Module("flight") : null;
      return flight != null && "Velocity".equals(init_Str_Module(flight, "mode", "Abilities")) && init_Z_Module(flight, "no-sneak", false);
   }

   static boolean load(Player player) {
      Module flight = init(player) ? init_Module("flight") : null;
      return flight != null && "Abilities".equals(init_Str_Module(flight, "mode", "Abilities"));
   }

   public static Vec3 init_Vec3_Entity(Entity entity, MoverType type, Vec3 movement) {
      if (init(entity) && type == MoverType.SELF && movement != null && entity instanceof Player bot) {
         Module speed = init_Module("speed");
         if (speed != null && init_Z_Player_Module(bot, speed)) {
            boolean liquid = bot.isInWater() || bot.isInLava();
            if (liquid && !init_Z_Module(speed, "in-liquids", false)) {
               return movement;
            } else {
               return "Strafe".equals(init_Str_Module(speed, "mode", "Vanilla")) ? load(bot, speed, movement) : init_Vec3_Player(bot, speed, movement);
            }
         } else {
            return movement;
         }
      } else {
         return movement;
      }
   }

   public static Vec3 init_Vec3_Entity_Vec3(Entity entity, Vec3 movement) {
      if (init(entity) && entity instanceof Player bot && movement != null) {
         Module speed = init_Module("speed");
         if (speed != null && init_Z_Module(speed, "in-liquids", false) && init_Z_Player_Module(bot, speed)) {
            return "Strafe".equals(init_Str_Module(speed, "mode", "Vanilla")) ? load(bot, speed, movement) : init_Vec3_Player(bot, speed, movement);
         } else {
            return movement;
         }
      } else {
         return movement;
      }
   }

   private static boolean init_Z_Player_Module(Player bot, Module speed) {
      if (bot.isFallFlying() || bot.onClimbable() || bot.getVehicle() != null) {
         return false;
      }

      if (!init_Z_Module(speed, "when-sneaking", false) && bot.isShiftKeyDown()) {
         return false;
      }

      boolean liquid = bot.isInWater() || bot.isInLava();
      return !init_Z_Module(speed, "only-on-ground", false)
         || bot.onGround()
         || liquid && init_Z_Module(speed, "in-liquids", false)
         || !"Vanilla".equals(init_Str_Module(speed, "mode", "Vanilla"));
   }

   private static Vec3 init_Vec3_Player(Player bot, Module speed, Vec3 movement) {
      Vec3 horizontal = init_Vec3_Player_D(bot, init_D_Module(speed, "vanilla-speed", 5.6) / 20.0);
      return new Vec3(horizontal.x, movement.y, horizontal.z);
   }

   private static Vec3 load(Player bot, Module speed, Vec3 movement) {
      boolean moving = Math.abs(bot.xxa) > 1.0E-5F || Math.abs(bot.zza) > 1.0E-5F;
      if (!moving) {
         sync = 0;
         return movement;
      }

      double base = init_D(bot);
      if (sync == 0) {
         sync = 1;
         read_D = 1.1799999475479126 * base - 0.01;
      } else if (sync == 1 && bot.onGround()) {
         movement = new Vec3(movement.x, init_D_Player_D(bot, 0.40123128), movement.z);
         read_D = read_D * init_D_Module(speed, "strafe-speed", 1.6);
         sync = 2;
      } else if (sync == 2) {
         read_D = write_D - 0.76 * (write_D - base);
         sync = 3;
      } else {
         read_D = write_D - write_D / 159.0;
      }

      read_D = Math.max(base, read_D);
      if (init_Z_Module(speed, "speed-limit", false)) {
         long now = System.currentTimeMillis();
         if (now - read_J > 2500L) {
            read_J = now;
         }

         read_D = Math.min(read_D, now - read_J > 1250L ? 0.44 : 0.43);
      }

      Vec3 horizontal = load(bot, read_D);
      return new Vec3(horizontal.x, movement.y, horizontal.z);
   }

   private static Vec3 init_Vec3_Player_D(Player player, double speed) {
      double forward = Math.signum(player.zza);
      double side = Math.signum(player.xxa);
      if (forward == 0.0 && side == 0.0) {
         return Vec3.ZERO;
      }

      double length = forward != 0.0 && side != 0.0 ? speed / Math.sqrt(2.0) : speed;
      double yaw = Math.toRadians(player.getYRot());
      double sin = Math.sin(yaw);
      double cos = Math.cos(yaw);
      return new Vec3((-sin * forward + cos * side) * length, 0.0, (cos * forward + sin * side) * length);
   }

   private static Vec3 load(Player player, double speed) {
      float forward = Math.signum(player.zza);
      float side = Math.signum(player.xxa);
      if (forward == 0.0F && side == 0.0F) {
         return Vec3.ZERO;
      }

      float yaw = player.getYRot();
      float strafe = 90.0F * side;
      if (forward != 0.0F) {
         strafe *= forward * 0.5F;
      }

      yaw -= strafe;
      if (forward < 0.0F) {
         yaw -= 180.0F;
      }

      double radians = Math.toRadians(yaw);
      return new Vec3(-Math.sin(radians) * speed, 0.0, Math.cos(radians) * speed);
   }

   private static double init_D(Player bot) {
      double speed = 0.2873;
      MobEffectInstance fast = bot.getEffect(MobEffects.SPEED);
      if (fast != null) {
         speed *= 1.0 + 0.2 * (fast.getAmplifier() + 1);
      }

      MobEffectInstance slow = bot.getEffect(MobEffects.SLOWNESS);
      if (slow != null) {
         speed /= 1.0 + 0.2 * (slow.getAmplifier() + 1);
      }

      return speed;
   }

   private static double init_D_Player_D(Player bot, double base) {
      MobEffectInstance jump = bot.getEffect(MobEffects.JUMP_BOOST);
      return jump == null ? base : base + (jump.getAmplifier() + 1) * 0.1;
   }

   private static void sync(RemotePlayer bot) {
      Vec3 current = bot.position();
      if (init_Vec3 != null) {
         double dx = current.x - init_Vec3.x;
         double dz = current.z - init_Vec3.z;
         write_D = Math.sqrt(dx * dx + dz * dz);
      }

      init_Vec3 = current;
   }

   public static boolean load(Entity entity) {
      Module noFall = init(entity) ? init_Module("no-fall") : null;
      return noFall != null && init_Z_Module(noFall, "anti-bounce", true);
   }

   static TokenStore.Inner_DataUtil init_TokenStore_DataUti_Vec3(Vec3 position, boolean onGround, Vec3 deltaMovement) {
      Vec3 wirePosition = position;
      boolean wireGround = onGround;
      Module flight = init_Module("flight");
      if (flight != null
         && !"Vulcan".equals(init_Str_Module(flight, "mode", "Abilities"))
         && "Packet".equals(init_Str_Module(flight, "anti-kick-mode", "Packet"))
         && !onGround) {
         if (load_I > 0) {
            load_I--;
         }

         if (load_I <= 0) {
            double baseline = Double.isFinite(init_D) ? init_D : position.y;
            wirePosition = new Vec3(position.x, baseline - 0.0313, position.z);
            load_I = Math.max(1, init_I_Module(flight, "delay", 20));
         }

         init_D = position.y;
      } else {
         init_D = position.y;
      }

      Module noFall = init_Module("no-fall");
      if (noFall != null && "Packet".equals(init_Str_Module(noFall, "mode", "Packet")) && !wireGround && init_Z_Module_Vec3(noFall, deltaMovement)) {
         wireGround = true;
      }

      return new TokenStore.Inner_DataUtil(wirePosition, wireGround);
   }

   private static boolean init_Z_Module_Vec3(Module noFall, Vec3 delta) {
      RemotePlayer bot = init_RemotePlayer();
      if (bot != null && !bot.getAbilities().instabuild && !bot.isFallFlying()) {
         return init_Z_Module(noFall, "pause-on-mace", true) && bot.getMainHandItem().is(Items.MACE)
            ? false
            : init_Module("flight") != null || delta != null && delta.y <= -0.5;
      } else {
         return false;
      }
   }

   static boolean init_Z_Minecraft_BlockPos(Minecraft mc, BlockPos pos) {
      Module noInteract = init_Module("no-interact");
      if (noInteract != null && mc != null && mc.level != null && pos != null) {
         List<String> configured = init_List_Module_Str(noInteract, "block-mine");
         if (configured.isEmpty()) {
            return false;
         }

         String id = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).toString();
         boolean listed = configured.stream().anyMatch(entry -> di.init(id, entry));
         return "WhiteList".equalsIgnoreCase(init_Str_Module(noInteract, "block-mine-mode", "BlackList")) ? !listed : listed;
      } else {
         return false;
      }
   }

   static void init(RemotePlayer bot, BlockPos pos, Direction direction, BlockState state) {
      if (init(bot) && pos != null) {
         long now = System.currentTimeMillis();
         Module autoTool = init_Module("auto-tool");
         if (autoTool != null && (!init_Z_Module(autoTool, "require-sneaking", false) || bot.isShiftKeyDown())) {
            if (!pos.equals(init_BlockPos)) {
               init_BlockPos = pos.immutable();
               read_Z = false;
               if (flush < 0) {
                  flush = init_ClassCache == null ? -1 : init_ClassCache.load_I();
               }

               write_J = now + init(init_I_Module(autoTool, "switch-delay-ms", 25));
               update_J = 9223372036854775807L;
            }
         } else {
            load(false);
         }

         Module fast = init_Module("fast-break");
         if (fast == null || !"Packet".equals(init_Str_Module(fast, "mode", "Damage")) || !init_Z_Module_BlockState(fast, state)) {
            load_BlockPos = null;
            write_Z = false;
         } else if (!pos.equals(load_BlockPos)) {
            load_BlockPos = pos.immutable();
            int chance = Math.max(0, Math.min(100, init_I_Module(fast, "activation-chance", 100)));
            write_Z = ThreadLocalRandom.current().nextInt(100) < chance;
         }

         if (init_Module("instant-rebreak") != null) {
            read_BlockPos = pos.immutable();
            init_Direction = direction == null ? Direction.UP : direction;
            open = 0;
         }
      }
   }

   static void init(boolean restoreAutoTool) {
      init_BlockPos = null;
      if (restoreAutoTool && flush >= 0) {
         Module autoTool = init_Module("auto-tool");
         if (autoTool != null && init_Z_Module(autoTool, "switch-back", false)) {
            update_J = System.currentTimeMillis() + init(init_I_Module(autoTool, "switch-back-delay-ms", 100));
         } else {
            load(false);
         }
      } else {
         load(false);
      }
   }

   private static void load(boolean restore) {
      if (restore && init_ClassCache != null && flush >= 0) {
         if (close >= 9) {
            init_ClassCache.init_Z_I_I(close, flush);
         }

         init_ClassCache.load_Z_I(flush);
      }

      init_BlockPos = null;
      flush = -1;
      read_Z = false;
      close = -1;
      write_J = 0L;
      update_J = 9223372036854775807L;
   }

   static boolean init(RemotePlayer bot, BlockState state, BlockPos pos, float baseProgress) {
      Module fast = init_Module("fast-break");
      return fast != null
         && "Damage".equals(init_Str_Module(fast, "mode", "Damage"))
         && init_Z_Module(fast, "instamine", true)
         && baseProgress > 0.5F
         && init_Z_Module_BlockState(fast, state);
   }

   static float init_F_F_BlockState(float progress, BlockState state) {
      Module fast = init_Module("fast-break");
      if (fast == null) {
         return progress;
      } else {
         String mode = init_Str_Module(fast, "mode", "Damage");
         if ("Normal".equals(mode) && init_Z_Module_BlockState(fast, state)) {
            return (float)(progress * Math.max(0.0, init_D_Module(fast, "modifier", 1.4)));
         } else if ("Haste".equals(mode)) {
            int amplifier = Math.max(1, init_I_Module(fast, "haste-amplifier", 2));
            return progress * (1.0F + amplifier * 0.2F);
         } else {
            return progress;
         }
      }
   }

   static float init_F_F_F(float accumulated, float delta) {
      Module fast = init_Module("fast-break");
      return fast != null
            && "Damage".equals(init_Str_Module(fast, "mode", "Damage"))
            && init_Z_Module(fast, "instamine", true)
            && accumulated > 0.0F
            && accumulated + delta >= 0.7F
         ? 1.0F
         : accumulated + delta;
   }

   static void init_V_BlockPos_Direction(BlockPos pos, Direction direction) {
      Module fast = init_Module("fast-break");
      if (init_ClassCache != null
         && fast != null
         && "Packet".equals(init_Str_Module(fast, "mode", "Damage"))
         && write_Z
         && pos != null
         && pos.equals(load_BlockPos)) {
         init_V_BlockPos(pos, direction, false);
      }
   }

   static void load(BlockPos pos, Direction direction) {
      Module fast = init_Module("fast-break");
      if (init_ClassCache != null
         && fast != null
         && "Damage".equals(init_Str_Module(fast, "mode", "Damage"))
         && init_Z_Module(fast, "grim-bypass", false)
         && pos != null) {
         init_ClassCache.init_Z_Packet(
            new ServerboundPlayerActionPacket(
               net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
               pos.above(),
               direction == null ? Direction.UP : direction
            )
         );
      }
   }

   static void init(Minecraft mc, RemotePlayer bot, boolean breaking, BlockPos breakPos, Direction breakDir) {
      if (init(bot) && init_ClassCache != null && mc != null && mc.level != null && !PkgCls_a.init_Z_b(init_ClassCache)) {
         init(mc, bot, breaking, breakPos);
         init_V_Minecraft_RemotePlayer(mc, bot);
         load(mc, bot);
      }
   }

   private static void init(Minecraft mc, RemotePlayer bot, boolean breaking, BlockPos breakPos) {
      if (update_J != 9223372036854775807L && System.currentTimeMillis() >= update_J) {
         load(true);
      }

      Module autoTool = init_Module("auto-tool");
      if (autoTool == null) {
         load(true);
      } else if (breaking
         && breakPos != null
         && breakPos.equals(init_BlockPos)
         && !read_Z
         && System.currentTimeMillis() >= write_J
         && (!init_Z_Module(autoTool, "require-sneaking", false) || bot.isShiftKeyDown())) {
         BlockState state = mc.level.getBlockState(breakPos);
         int selected = init_CharEncoder == null ? init_ClassCache.load_I() : init_CharEncoder.load();
         int limit = init_Z_Module(autoTool, "consider-inventory", false) ? 36 : 9;
         int best = init(
            mc, bot, state, limit, init_Z_Module(autoTool, "ignore-durability", true), init_Z_Module(autoTool, "prefer-silk-touch", false), selected
         );
         if (best >= 0 && best != selected) {
            boolean sent = best < 9 ? init_ClassCache.load_Z_I(best) : init_ClassCache.init_Z_I_I(best, selected);
            if (sent) {
               read_Z = true;
               close = best >= 9 ? best : -1;
            }
         } else {
            read_Z = true;
         }
      }
   }

   private static int init(Minecraft mc, RemotePlayer bot, BlockState state, int limit, boolean ignoreDurability, boolean preferSilk, int selected) {
      int best = -1;
      float bestSpeed = -1.0F;

      for (int slot = 0; slot < Math.min(36, limit); slot++) {
         ItemStack stack = bot.getInventory().getItem(slot);
         if (!stack.isEmpty() && (ignoreDurability || !stack.isDamageableItem() || init_I(stack) > 2)) {
            float speed = autismclient.util.ArrayUtil.init(mc, stack, state);
            if (!(speed <= 1.0F)
               && (!preferSilk || autismclient.util.ArrayUtil.init_Z(mc, stack))
               && (speed > bestSpeed || speed == bestSpeed && init_I_I_I(slot, selected) < init_I_I_I(best, selected))) {
               best = slot;
               bestSpeed = speed;
            }
         }
      }

      if (best < 0 && preferSilk) {
         return init(mc, bot, state, limit, ignoreDurability, false, selected);
      }

      if (best >= 0) {
         float held = autismclient.util.ArrayUtil.init(mc, bot.getInventory().getItem(selected), state);
         if (bestSpeed <= held && (!preferSilk || autismclient.util.ArrayUtil.init_Z(mc, bot.getInventory().getItem(selected)))) {
            return selected;
         }
      }

      return best;
   }

   private static int init_I_I_I(int slot, int selected) {
      if (slot < 0) {
         return 2147483647;
      } else {
         return slot < 9 ? Math.abs(slot - selected) : 100 + slot;
      }
   }

   private static void init_V_Minecraft_RemotePlayer(Minecraft mc, RemotePlayer bot) {
      Module instant = init_Module("instant-rebreak");
      if (instant == null) {
         read_BlockPos = null;
      } else if (read_BlockPos != null
         && !mc.level.isOutsideBuildHeight(read_BlockPos)
         && !mc.level.getBlockState(read_BlockPos).isAir()
         && (!init_Z_Module(instant, "only-pick", true) || bot.getMainHandItem().is(ItemTags.PICKAXES))
         && open++ >= Math.max(0, init_I_Module(instant, "delay", 0))) {
         open = 0;
         if (init_Z_Module(instant, "rotate", false)) {
            Vec3 center = Vec3.atCenterOf(read_BlockPos);
            float yaw = init_F_Player_Vec3(bot, center);
            float pitch = load(bot, center);
            init_ClassCache.init_Z_Packet(new Rot(yaw, pitch, bot.onGround(), bot.horizontalCollision));
         }

         init_V_BlockPos(read_BlockPos, init_Direction, true);
      }
   }

   private static void init_V_BlockPos(BlockPos pos, Direction direction, boolean swing) {
      if (init_ClassCache != null && pos != null) {
         init_ClassCache.init_Z_Packet(
            new ServerboundPlayerActionPacket(
               net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
               pos,
               direction == null ? Direction.UP : direction,
               init_ClassCache.pop_I()
            )
         );
         if (swing) {
            init_ClassCache.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND));
         }
      }
   }

   private static void load(Minecraft mc, RemotePlayer bot) {
      if (poll > 0) {
         poll--;
      }

      Module noFall = init_Module("no-fall");
      if (noFall != null && poll <= 0 && !bot.getAbilities().instabuild && !bot.isFallFlying() && !bot.isInWater() && !bot.isInLava()) {
         String mode = init_Str_Module(noFall, "mode", "Packet");
         if (!"Packet".equals(mode) && (!init_Z_Module(noFall, "pause-on-mace", true) || !bot.getMainHandItem().is(Items.MACE))) {
            boolean airPlace = "AirPlace".equals(mode);
            double threshold = airPlace && "BeforeDeath".equals(init_Str_Module(noFall, "air-place-mode", "BeforeDeath"))
               ? Math.max(2.0, bot.getHealth() + bot.getAbsorptionAmount())
               : (airPlace ? 2.0 : 3.0);
            if (!(bot.fallDistance <= threshold)) {
               if (init_Z_Module(noFall, "anchor", true)) {
                  bot.setPos(Math.floor(bot.getX()) + 0.5, bot.getY(), Math.floor(bot.getZ()) + 0.5);
               }

               int old = init_ClassCache.load_I();
               int slot = init_I_RemotePlayer(bot, noFall, airPlace);
               if (slot >= 0 && init_ClassCache.load_Z_I(slot)) {
                  try {
                     ItemStack stack = bot.getInventory().getItem(slot);
                     BlockHitResult placementHit;
                     if (airPlace) {
                        BlockPos below = bot.blockPosition().below();
                        placementHit = new BlockHitResult(Vec3.atCenterOf(below).add(0.0, 0.5, 0.0), Direction.UP, below, false);
                     } else {
                        HitResult ray = mc.level.clip(new ClipContext(bot.position(), bot.position().subtract(0.0, 5.0, 0.0), Block.OUTLINE, Fluid.NONE, bot));
                        if (!(ray instanceof BlockHitResult blockRay) || ray.getType() != Type.BLOCK) {
                           return;
                        }

                        placementHit = new BlockHitResult(
                           Vec3.atCenterOf(blockRay.getBlockPos()).add(0.0, 0.5, 0.0), Direction.UP, blockRay.getBlockPos(), false
                        );
                     }

                     Vec3 aim = placementHit.getLocation();
                     float yaw = init_F_Player_Vec3(bot, aim);
                     float pitch = load(bot, aim);
                     init_ClassCache.init_Z_Packet(new Rot(yaw, pitch, bot.onGround(), bot.horizontalCollision));
                     if (init_Z_Module(noFall, "client-rotate", false)) {
                        PoolEntry.init_V_RemotePlayer(bot, yaw, pitch);
                     }

                     if (!airPlace && (stack.is(Items.WATER_BUCKET) || stack.is(Items.POWDER_SNOW_BUCKET))) {
                        init_ClassCache.init_Z_Packet(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, init_ClassCache.pop_I(), yaw, pitch));
                     } else {
                        init_ClassCache.init_Z_Packet(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, placementHit, init_ClassCache.pop_I()));
                     }

                     init_ClassCache.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND));
                     poll = 10;
                     return;
                  } finally {
                     init_ClassCache.load_Z_I(old);
                  }
               }
            }
         }
      }
   }

   private static int init_I_RemotePlayer(RemotePlayer bot, Module noFall, boolean blockOnly) {
      Item var10000;
      switch (init_Str_Module(noFall, "placed-item", "Bucket")) {
         case "PowderSnow":
            var10000 = Items.POWDER_SNOW_BUCKET;
            break;
         case "HayBale":
            var10000 = Items.HAY_BLOCK;
            break;
         case "Cobweb":
            var10000 = Items.COBWEB;
            break;
         case "SlimeBlock":
            var10000 = Items.SLIME_BLOCK;
            break;
         default:
            var10000 = init_ClassCache != null && init_ClassCache.start_Str().endsWith("the_nether") ? Items.POWDER_SNOW_BUCKET : Items.WATER_BUCKET;
      }

      Item preferred = var10000;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = bot.getInventory().getItem(i);
         if (!stack.isEmpty() && stack.is(preferred) && (!blockOnly || stack.getItem() instanceof BlockItem)) {
            return i;
         }
      }

      if (blockOnly) {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = bot.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
               return i;
            }
         }
      }

      return -1;
   }

   private static boolean init_Z_Module_BlockState(Module fast, BlockState state) {
      if (fast != null && state != null && !state.isAir()) {
         List<String> values = init_List_Module_Str(fast, "blocks");
         boolean whitelist = "Whitelist".equalsIgnoreCase(init_Str_Module(fast, "blocks-filter", "Blacklist"));
         if (values.isEmpty()) {
            return !whitelist;
         }

         String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
         boolean listed = values.stream().anyMatch(entry -> di.init(id, entry));
         return whitelist ? listed : !listed;
      } else {
         return false;
      }
   }

   private static long init(int base) {
      if (base <= 0) {
         return 0L;
      }

      int spread = Math.max(15, Math.min(180, Math.round(base * 0.55F)));
      return base + ThreadLocalRandom.current().nextInt(spread + 1);
   }

   private static float init_F_Player_Vec3(Player player, Vec3 target) {
      return (float)(Math.toDegrees(Math.atan2(target.z - player.getZ(), target.x - player.getX())) - 90.0);
   }

   private static float load(Player player, Vec3 target) {
      Vec3 eye = player.getEyePosition();
      double dx = target.x - eye.x;
      double dy = target.y - eye.y;
      double dz = target.z - eye.z;
      return (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
   }

   private static RemotePlayer init_RemotePlayer() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && mc.level.getEntity(init_I) instanceof RemotePlayer bot ? bot : null;
   }

   private static Module init_Module(String id) {
      Module module = al.init_Module_Str(id);
      return module != null && module.isEnabled() ? module : null;
   }

   private static boolean init_Z(String id) {
      return init_Module(id) != null;
   }

   private static String init_Str_Module(Module module, String id, String fallback) {
      if (module == null) {
         return fallback;
      }

      String value = module.value(id);
      return value != null && !value.isBlank() ? value : fallback;
   }

   private static boolean init_Z_Module(Module module, String id, boolean fallback) {
      String value = init_Str_Module(module, id, Boolean.toString(fallback));
      return "true".equalsIgnoreCase(value);
   }

   private static int init_I_Module(Module module, String id, int fallback) {
      try {
         return Integer.parseInt(init_Str_Module(module, id, Integer.toString(fallback)));
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private static double init_D_Module(Module module, String id, double fallback) {
      try {
         return Double.parseDouble(init_Str_Module(module, id, Double.toString(fallback)));
      } catch (NumberFormatException ignored) {
         return fallback;
      }
   }

   private static List<String> init_List_Module_Str(Module module, String id) {
      String raw = init_Str_Module(module, id, "");
      return raw.isBlank() ? List.of() : Arrays.stream(raw.split("\\|")).map(String::trim).filter(value -> !value.isEmpty()).toList();
   }

   public record Inner_DataUtil(Vec3 init_Vec3, boolean init_Z) {
   }
}
