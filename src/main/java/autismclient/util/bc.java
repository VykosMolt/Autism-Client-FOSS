package autismclient.util;

import autismclient.modules.Module;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

final class bc {
   private bc() {
   }

   static void init(Minecraft mc) {
      LocalPlayer player = mc.player;
      if (player != null && mc.getSingleplayerServer() != null) {
         Vec3 oldPosition = player.position();
         Vec3 oldVelocity = player.getDeltaMovement();
         ClientInput oldInput = player.input;
         boolean oldGrounded = player.onGround();
         boolean oldFlying = player.getAbilities().flying;

         try {
            player.getAbilities().flying = false;
            init(player);
            load(player);
            init(mc, player);
            read(player);
            load(mc, player);
         } catch (Exception e) {
            throw new IllegalStateException("Movement module qualification failed", e);
         } finally {
            player.setPos(oldPosition);
            player.setDeltaMovement(oldVelocity);
            player.setOnGround(oldGrounded);
            player.getAbilities().flying = oldFlying;
            player.input = oldInput;
         }
      } else {
         throw new IllegalStateException("Missing fixture world");
      }
   }

   private static void init(LocalPlayer player) {
      Cow cow = EntityTypes.COW.create(player.level(), EntitySpawnReason.COMMAND);
      if (cow == null) {
         throw new IllegalStateException("Missing cow fixture");
      }

      cow.setPos(player.position().add(0.25, 0.0, 0.0));
      Module module = autismclient.modules.al.init_Module_Str("no-push");
      String saved = module.value("entities");
      module.setValueTransient("entities", "true");

      try {
         player.setDeltaMovement(Vec3.ZERO);

         try (autismclient.modules.aP.Inner_StreamUtil ignored = module.borrowEnabled(false)) {
            player.push(cow);
         }

         init(player.getDeltaMovement().horizontalDistanceSqr() > 0.0, "vanilla entity push");
         player.setDeltaMovement(Vec3.ZERO);

         try (autismclient.modules.aP.Inner_StreamUtil ignored = module.borrowEnabled(true)) {
            player.push(cow);
         }

         init(player.getDeltaMovement().equals(Vec3.ZERO), "No Push");
      } finally {
         module.setValueTransient("entities", saved);
      }

      Vec3 var271 = player.position();

      try (autismclient.modules.aP.Inner_StreamUtil ignored = autismclient.modules.al.init_Module_Str("air-stuck").borrowEnabled(true)) {
         player.move(MoverType.SELF, new Vec3(0.0, 0.2, 0.0));
         init(player.position().equals(var271), "Air Stuck");
      }

      try (autismclient.modules.aP.Inner_StreamUtil ignored = autismclient.modules.al.init_Module_Str("air-stuck").borrowEnabled(false)) {
         player.move(MoverType.SELF, new Vec3(0.0, 0.2, 0.0));
         init(player.getY() > var271.y, "Air Stuck release");
      }

      player.setPos(var271);
   }

   private static void load(LocalPlayer player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Method modifyInput = LocalPlayer.class.getDeclaredMethod("modifyInput", Vec2.class);
      modifyInput.setAccessible(true);
      ItemStack old = player.getMainHandItem();
      player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
      player.startUsingItem(InteractionHand.MAIN_HAND);

      try {
         Module module = autismclient.modules.al.init_Module_Str("no-slow");
         Vec2 forward = new Vec2(0.0F, 1.0F);

         try (autismclient.modules.aP.Inner_StreamUtil ignored = module.borrowEnabled(false)) {
            init(((Vec2)modifyInput.invoke(player, forward)).y < 0.4, "vanilla eating slowdown");
         }

         try (autismclient.modules.aP.Inner_StreamUtil ignored = module.borrowEnabled(true)) {
            init(((Vec2)modifyInput.invoke(player, forward)).y > 0.9, "No Slow");
         }
      } finally {
         player.stopUsingItem();
         player.setItemInHand(InteractionHand.MAIN_HAND, old);
      }
   }

   private static void init(Minecraft mc, LocalPlayer player) throws IllegalAccessException, NoSuchFieldException {
      Field stuck = init(Entity.class, "stuckSpeedMultiplier");
      BlockState cobweb = Blocks.COBWEB.defaultBlockState();

      try (autismclient.modules.aP.Inner_StreamUtil ignored = autismclient.modules.al.init_Module_Str("anti-web").borrowEnabled(false)) {
         player.makeStuckInBlock(cobweb, new Vec3(0.25, 0.05, 0.25));
         init(((Vec3)stuck.get(player)).x == 0.25, "vanilla web speed");
      }

      stuck.set(player, Vec3.ZERO);
      Module antiWeb = autismclient.modules.al.init_Module_Str("anti-web");
      String savedMode = antiWeb.value("mode");

      try (autismclient.modules.aP.Inner_StreamUtil ignored = antiWeb.borrowEnabled(true)) {
         player.makeStuckInBlock(cobweb, new Vec3(0.25, 0.05, 0.25));
         init(stuck.get(player).equals(Vec3.ZERO), "AntiWeb slowdown");
         antiWeb.setValueTransient("mode", "Solid");
         init(!cobweb.getCollisionShape(mc.level, player.blockPosition(), CollisionContext.of(player)).isEmpty(), "AntiWeb solid");
      } finally {
         antiWeb.setValueTransient("mode", savedMode);
      }

      float var451 = player.getYRot();
      float pitch = player.getXRot();
      Vec3 playerPosition = player.position();
      BlockPos base = player.blockPosition().above(4);
      player.setPos(base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
      player.setYRot(0.0F);
      player.setXRot(0.0F);
      BlockPos webPos = base.above().south();
      BlockPos stonePos = webPos.south();
      BlockState oldWeb = mc.level.getBlockState(webPos);
      BlockState oldStone = mc.level.getBlockState(stonePos);

      try (autismclient.modules.aP.Inner_StreamUtil ignored = autismclient.modules.al.init_Module_Str("hand-no-clip").borrowEnabled(true)) {
         mc.level.setBlock(webPos, cobweb, 3);
         mc.level.setBlock(stonePos, Blocks.STONE.defaultBlockState(), 3);
         Vec3 eye = player.getEyePosition();
         BlockHitResult pick = mc.level.clip(new ClipContext(eye, eye.add(0.0, 0.0, 4.0), Block.OUTLINE, Fluid.NONE, player));
         init(pick.getBlockPos().equals(webPos), "vanilla web pick");
         init(autismclient.modules.N.init(mc, pick, false, false) instanceof BlockHitResult hit && hit.getBlockPos().equals(stonePos), "Hand No Clip");
      } finally {
         mc.level.setBlock(webPos, oldWeb, 3);
         mc.level.setBlock(stonePos, oldStone, 3);
         player.setYRot(var451);
         player.setXRot(pitch);
         player.setPos(playerPosition);
      }
   }

   private static void read(LocalPlayer player) throws IllegalAccessException, NoSuchFieldException {
      Field delay = init(LivingEntity.class, "noJumpDelay");
      Vec3 original = player.position();
      int previous = delay.getInt(player);
      player.input = new ClientInput();
      player.input.keyPresses = new Input(false, false, false, false, true, false, false);

      try (autismclient.modules.aP.Inner_StreamUtil ignored = autismclient.modules.al.init_Module_Str("no-jump-delay").borrowEnabled(true)) {
         delay.setInt(player, 7);
         player.setOnGround(true);
         player.setDeltaMovement(Vec3.ZERO);
         player.aiStep();
         init(delay.getInt(player) == 10 && player.getY() > original.y, "No Jump Delay");
      } finally {
         delay.setInt(player, previous);
         player.setPos(original);
      }
   }

   private static void load(Minecraft mc, LocalPlayer player) throws InterruptedException, ExecutionException, TimeoutException {
      IntegratedServer server = mc.getSingleplayerServer();
      UUID uuid = player.getUUID();

      try (autismclient.modules.aP.Inner_StreamUtil ignored = autismclient.modules.al.init_Module_Str("trident-boost").borrowEnabled(true)) {
         boolean launched = server.<Boolean>submit(() -> {
            ServerPlayer authoritative = server.getPlayerList().getPlayer(uuid);
            return authoritative != null && init(authoritative);
         }).get(3L, TimeUnit.SECONDS);
         init(launched, "Trident Boost on integrated server");
      }
   }

   private static boolean init(Player player) {
      if (player.isInWaterOrRain()) {
         return false;
      }

      ItemStack old = player.getMainHandItem();
      Vec3 start = player.position();
      Vec3 velocity = player.getDeltaMovement();
      ItemStack stack = new ItemStack(Items.TRIDENT);
      Reference<Enchantment> riptide = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.RIPTIDE);
      stack.enchant(riptide, 1);
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);

      try {
         Item item = stack.getItem();
         InteractionResult use = item.use(player.level(), player, InteractionHand.MAIN_HAND);
         return use.consumesAction() && item.releaseUsing(stack, player.level(), player, item.getUseDuration(stack, player) - 12) && player.isAutoSpinAttack();
      } finally {
         player.stopUsingItem();
         player.startAutoSpinAttack(0, 0.0F, ItemStack.EMPTY);
         player.setItemInHand(InteractionHand.MAIN_HAND, old);
         player.setPos(start);
         player.setDeltaMovement(velocity);
      }
   }

   private static Field init(Class<?> owner, String name) throws NoSuchFieldException {
      Field field = owner.getDeclaredField(name);
      field.setAccessible(true);
      return field;
   }

   private static void init(boolean condition, String name) {
      if (!condition) {
         throw new IllegalStateException(name + " failed");
      }
   }
}
