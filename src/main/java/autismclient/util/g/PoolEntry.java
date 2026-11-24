package autismclient.util.g;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.compat263.CompatSwingPacket;
import autismclient.modules.aC;
import autismclient.modules.al;
import autismclient.util.AutismMultiOverlay;
import autismclient.util.aB;
import autismclient.util.ax;
import autismclient.util.bi;
import autismclient.util.bk;
import autismclient.util.macro.F;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen.BookAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class PoolEntry {
   private static final double init_D = 1.0;
   private static final double load_D = 0.75;
   private static final double read_D = 0.85;
   private static final double write_D = 0.45;
   private static final double update_D = 0.2;
   private static volatile ClassCache init_ClassCache;
   private static volatile int init_I = -1;
   private static volatile PoolEntry.Inner_StreamUtil init_PoolEntry_StreamUt;
   private static final ThreadLocal<PoolEntry.Inner_CachePool> init_ThreadLocal = new ThreadLocal<>();
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static boolean write_Z;
   private static Input init_Input = Input.EMPTY;
   private static Input load_Input = Input.EMPTY;
   private static long init_J;
   private static long load_J;
   private static long read_J;
   private static long write_J;
   private static boolean update_Z;
   private static boolean sync_Z;
   private static boolean flush_Z;
   private static boolean close;
   private static boolean open;
   private static boolean poll;
   private static long update_J;
   private static long sync_J;
   private static Vec3 init_Vec3 = Vec3.ZERO;
   private static Vec3 load_Vec3 = Vec3.ZERO;
   private static float init_F;
   private static float load_F;
   private static boolean push = true;
   private static boolean pop;
   private static boolean tick;
   private static boolean run;
   private static boolean get;
   private static boolean set;
   private static final TableIndex init_TableIndex = new TableIndex();
   private static long flush_J = -9223372036854775808L;
   private static int load_I = -1;
   private static int read_I;
   private static boolean put;
   private static BlockPos init_BlockPos;
   private static Direction init_Direction;
   private static float read_F;
   private static int write_I;
   private static int update_I = -1;
   private static boolean add;

   private PoolEntry() {
   }

   public static boolean init_Z() {
      return init_ClassCache != null;
   }

   static ClassCache init_ClassCache() {
      return init_ClassCache;
   }

   static RemotePlayer init_RemotePlayer() {
      return init_RemotePlayer_Minecraft(Minecraft.getInstance());
   }

   public static Player init_Player() {
      ClassCache active = init_ClassCache;
      Minecraft mc = Minecraft.getInstance();
      if (active == null) {
         return mc.player;
      } else {
         return mc.level != null && mc.level.getEntity(init_I) instanceof RemotePlayer bot ? bot : null;
      }
   }

   public static long init_J() {
      ClassCache active = init_ClassCache;
      return active == null ? -1L : active.init_ClassCache_EventBu().init_J();
   }

   public static PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_J(long generation) {
      ClassCache active = init_ClassCache;
      if (active == null) {
         return null;
      }

      ClassCache.Inner_EventBus context = active.init_ClassCache_EventBu();
      if (context.init_J() > generation && context.init_I() > 0 && context.load_Z()) {
         int containerSlots = Math.max(0, context.init_List().size() - 36);
         List<ItemStack> items = new ArrayList<>(containerSlots);

         for (int i = 0; i < containerSlots; i++) {
            ItemStack stack = context.init_List().get(i);
            items.add(stack == null ? ItemStack.EMPTY : stack.copy());
         }

         return new PoolEntry.Inner_DataUtil(context.init_J(), context.init_Component().getString(), context.init_Str(), List.copyOf(items));
      } else {
         return null;
      }
   }

   public static void init_V() {
      ClassCache active = init_ClassCache;
      if (active != null) {
         load_J = active.init_J();
         update_Z = false;
         AutismMultiOverlay.sync_V_Str(active.open_Str());
         active.flush_V();
      }
   }

   public static String init_Str_ItemStack(ItemStack stack) {
      ClassCache active = init_ClassCache;
      return active == null ? null : active.init_Str_ItemStack(stack);
   }

   public static String init_Str_I(int targetHandlerSlot) {
      ClassCache active = init_ClassCache;
      return active == null ? null : active.write_Str_I(targetHandlerSlot);
   }

   static String init_Str_Str(String arguments) {
      ClassCache active = init_ClassCache;
      RemotePlayer bot = init_RemotePlayer_Minecraft(Minecraft.getInstance());
      if (active == null || bot == null || !active.next_Z()) {
         return "POV session is not ready";
      }

      if (active.put_Z()) {
         return "Macro owns the POV bot";
      }

      String[] parts = init_StrArr_Str(arguments);
      if (parts.length == 0) {
         return "Usage: hclip <blocks>";
      }

      String mode = parts[0].toLowerCase(Locale.ROOT);
      double blocks;
      int count;
      boolean forceGround;
      if (!mode.equals("forward") && !mode.equals("back")) {
         BufferHelper.Inner_StreamUtil request = BufferHelper.init_BufferHelper_Strea("hclip", arguments);
         if (request.init_BufferHelper_Cache() == null) {
            return request.init_Str();
         }

         blocks = request.init_BufferHelper_Cache().init_D();
         count = request.init_BufferHelper_Cache().init_I();
         forceGround = request.init_BufferHelper_Cache().init_Z();
      } else {
         blocks = init_D_RemotePlayer(bot, mode.equals("back") ? -1 : 1, 32);
         if (blocks == 0.0) {
            return mode.equals("back") ? "No blocking run behind you" : "No blocking run in front of you";
         }

         count = Math.max(1, (int)Math.ceil(Math.abs(blocks) / 10.0));
         if (count > 20) {
            count = 1;
         }

         forceGround = false;
      }

      double yaw = Math.toRadians(bot.getYRot());
      Vec3 delta = new Vec3(-Math.sin(yaw) * blocks, 0.0, Math.cos(yaw) * blocks);
      return init_Z_ClassCache(active, bot, delta, count, forceGround || bot.onGround()) ? "Sent" : "Clip refused";
   }

   static String load_Str(String arguments) {
      return PkgCls_a.load(arguments);
   }

   static String read_Str(String arguments) {
      ClassCache active = init_ClassCache;
      RemotePlayer bot = init_RemotePlayer_Minecraft(Minecraft.getInstance());
      if (active == null || bot == null || !active.next_Z()) {
         return "POV session is not ready";
      }

      if (active.put_Z()) {
         return "Macro owns the POV bot";
      }

      String[] parts = init_StrArr_Str(arguments);
      if (parts.length == 0) {
         return "Usage: vclip <blocks>";
      }

      String mode = parts[0].toLowerCase(Locale.ROOT);
      double blocks;
      int count;
      boolean forceGround;
      if (!mode.equals("top") && !mode.equals("bottom")) {
         BufferHelper.Inner_StreamUtil request = BufferHelper.init_BufferHelper_Strea("vclip", arguments);
         if (request.init_BufferHelper_Cache() == null) {
            return request.init_Str();
         }

         blocks = request.init_BufferHelper_Cache().init_D();
         count = request.init_BufferHelper_Cache().init_I();
         forceGround = request.init_BufferHelper_Cache().init_Z();
      } else {
         blocks = init_D_RemotePlayer_Z(bot, mode.equals("top"));
         if (blocks == 0.0) {
            return "No safe " + mode + " target";
         }

         count = Math.max(1, (int)Math.ceil(Math.abs(blocks) / 10.0));
         if (count > 20) {
            count = 1;
         }

         forceGround = true;
      }

      return init_Z_ClassCache(active, bot, new Vec3(0.0, blocks, 0.0), count, forceGround) ? "Sent" : "Clip refused";
   }

   private static boolean init_Z_ClassCache(ClassCache active, RemotePlayer bot, Vec3 delta, int segments, boolean onGround) {
      int queued = active.init_I_D(delta.x, delta.y, delta.z, segments, onGround);
      read(active, bot);
      get = queued >= 0 && active.open_Z();
      return queued >= 0;
   }

   private static void read(ClassCache active, RemotePlayer bot) {
      PositionMoveRotation accepted = active.init_PositionMoveRotati();
      if (accepted != null && accepted.position() != null) {
         init_Vec3 = accepted.position();
         load_Vec3 = Vec3.ZERO;
         init_F = accepted.yRot();
         load_F = accepted.xRot();
         push = active.start_Z();
         pop = false;
         tick = true;
         sync(bot);
         init_TableIndex.init_V(init_Vec3, System.currentTimeMillis());
      }
   }

   static void init_V_ClassCache(ClassCache active, Vec3 position, boolean onGround) {
      if (active != null && active == init_ClassCache && position != null) {
         RemotePlayer bot = init_RemotePlayer_Minecraft(Minecraft.getInstance());
         if (bot != null) {
            init_Vec3 = position;
            load_Vec3 = Vec3.ZERO;
            push = onGround;
            pop = false;
            tick = true;
            sync(bot);
            init_TableIndex.init_V(position, System.currentTimeMillis());
         }
      }
   }

   static void init_V_ClassCache_RemotePlayer(ClassCache active, RemotePlayer bot) {
      if (active != null && active == init_ClassCache && bot != null) {
         set = true;
         load_Input = Input.EMPTY;
         init_Input = Input.EMPTY;
         init_Z = false;
         load_Z = false;
         load_Vec3 = Vec3.ZERO;
         F.Inner_StreamUtil view = F.init_F_StreamUtil_Entity(bot);
         push = view != null && view.init_Z(bot.position()) && view.sync(bot.position());
         bot.setDeltaMovement(Vec3.ZERO);
         bot.setShiftKeyDown(false);
         bot.stopFallFlying();
         bot.getAbilities().flying = false;
         if (!bot.getAbilities().instabuild) {
            bot.getAbilities().mayfly = false;
         }

         bot.setOnGround(push);
         active.init_Z_Packet(new ServerboundPlayerInputPacket(Input.EMPTY));
         active.init_Z_Abilities(bot.getAbilities());
         if (read_Z || write_Z) {
            active.init_Z_Entity_ServerboundPlayerC(bot, Action.STOP_SPRINTING);
         }

         read_Z = false;
         write_Z = false;
         bot.setSprinting(false);
      }
   }

   private static double init_D_RemotePlayer(RemotePlayer bot, int direction, int radius) {
      double yaw = Math.toRadians(bot.getYRot());
      double dx = -Math.sin(yaw) * direction;
      double dz = Math.cos(yaw) * direction;
      Vec3 start = bot.position();
      boolean blocked = false;
      int lastBlocked = 0;

      for (int distance = 1; distance <= Math.max(1, radius); distance++) {
         Vec3 candidate = new Vec3(start.x + dx * distance, start.y, start.z + dz * distance);
         boolean clear = init_Z_RemotePlayer_Vec3(bot, candidate);
         if (!clear) {
            blocked = true;
            lastBlocked = distance;
         } else if (blocked) {
            return direction * distance;
         }
      }

      return blocked ? direction * Math.min(radius, lastBlocked + 1) : 0.0;
   }

   private static double init_D_RemotePlayer_Z(RemotePlayer bot, boolean upward) {
      Vec3 start = bot.position();
      boolean sawBlocked = false;

      for (double distance = 0.5; distance <= 128.0; distance += 0.5) {
         double signed = upward ? distance : -distance;
         Vec3 candidate = start.add(0.0, signed, 0.0);
         boolean clear = init_Z_RemotePlayer_Vec3(bot, candidate);
         if (!clear) {
            sawBlocked = true;
         } else if (!upward || sawBlocked) {
            Vec3 below = candidate.add(0.0, -0.0625, 0.0);
            Vec3 shift = below.subtract(bot.position());
            if (!bot.level().noCollision(bot, bot.getBoundingBox().move(shift))) {
               return signed;
            }
         }
      }

      return 0.0;
   }

   private static boolean init_Z_RemotePlayer_Vec3(RemotePlayer bot, Vec3 candidate) {
      return bot != null && bot.level() != null && bot.level().isLoaded(BlockPos.containing(candidate))
         ? bot.level().noCollision(bot, bot.getBoundingBox().move(candidate.subtract(bot.position())))
         : false;
   }

   private static String[] init_StrArr_Str(String value) {
      String trimmed = value == null ? "" : value.trim();
      return trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
   }

   public static boolean load_Z() {
      ClassCache active = init_ClassCache;
      return active != null && !active.put_Z();
   }

   public static boolean init_Z_Entity(Entity entity) {
      return init_ClassCache != null && entity != null && entity.getId() == init_I;
   }

   public static boolean init_Z_I(int entityId) {
      return init_ClassCache != null && entityId == init_I;
   }

   public static boolean load(Entity entity) {
      ClassCache s = init_ClassCache;
      return s != null && entity != null && entity.getId() == init_I && !s.put_Z();
   }

   public static boolean read(Entity entity) {
      return run && entity != null && entity.getId() == init_I;
   }

   public static Player load_Player() {
      if (init_ClassCache == null) {
         return null;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            return null;
         } else {
            return mc.level.getEntity(init_I) instanceof RemotePlayer bot ? bot : null;
         }
      }
   }

   public static void init_V_Entity(Entity entity, EntityRenderState state, float partialTick) {
      ClassCache active = init_ClassCache;
      if (active != null && entity != null && state != null) {
         Minecraft mc = Minecraft.getInstance();
         if (init_Z_Z(entity == mc.player, entity.getId(), init_I)) {
            StateCache.Inner_DataUtil truth = init_StateCache_DataUti_ClassCache_Entity(active, entity);
            if (truth != null) {
               Vec3 oldRenderPosition = new Vec3(state.x, state.y, state.z);
               Vec3 truthPosition = truth.init_Vec3();
               state.x = truthPosition.x;
               state.y = truthPosition.y;
               state.z = truthPosition.z;
               Entity camera = Minecraft.getInstance().getCameraEntity();
               if (camera != null) {
                  state.distanceToCameraSq = truthPosition.distanceToSqr(camera.getPosition(partialTick));
               }

               if (state instanceof FishingHookRenderState hookState && truth.load_I() >= 0) {
                  StateCache.Inner_DataUtil ownerTruth = active.init_StateCache_DataUti_I(truth.load_I());
                  Entity renderedOwner = init_Entity_Minecraft_StateCache_DataUti(mc, ownerTruth);
                  if (renderedOwner == null && truth.load_I() == active.flush_I()) {
                     renderedOwner = init_RemotePlayer_Minecraft(mc);
                  }

                  if (renderedOwner != null && hookState.lineOriginOffset != null) {
                     Vec3 ownerShift = ownerTruth == null ? Vec3.ZERO : ownerTruth.init_Vec3().subtract(renderedOwner.getPosition(partialTick));
                     Vec3 hookShift = truthPosition.subtract(oldRenderPosition);
                     hookState.lineOriginOffset = hookState.lineOriginOffset.add(ownerShift).subtract(hookShift);
                  }
               }
            }
         }
      }
   }

   public static Boolean init_Boolean_Entity(Entity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
      ClassCache active = init_ClassCache;
      Minecraft mc = Minecraft.getInstance();
      if (active != null && entity != null && init_Z_Z(entity == mc.player, entity.getId(), init_I)) {
         StateCache.Inner_DataUtil truth = init_StateCache_DataUti_ClassCache_Entity(active, entity);
         if (truth == null) {
            return null;
         }

         Vec3 pos = truth.init_Vec3();
         double dx = pos.x - cameraX;
         double dy = pos.y - cameraY;
         double dz = pos.z - cameraZ;
         if (!entity.shouldRenderAtSqrDistance(dx * dx + dy * dy + dz * dz)) {
            return false;
         }

         if (read_Z(truth.init_Str())) {
            return true;
         }

         AABB translated = entity.getBoundingBox().move(pos.subtract(entity.position())).inflate(0.5);
         return frustum == null || frustum.isVisible(translated);
      } else {
         return null;
      }
   }

   private static boolean read_Z(String type) {
      return type != null && (type.equals("fishing_bobber") || type.endsWith(":fishing_bobber"));
   }

   private static StateCache.Inner_DataUtil init_StateCache_DataUti_ClassCache_Entity(ClassCache active, Entity entity) {
      if (active != null && entity != null) {
         StateCache.Inner_DataUtil byId = active.init_StateCache_DataUti_I(entity.getId());
         return byId != null && (byId.init_UUID() == null || byId.init_UUID().equals(entity.getUUID()))
            ? byId
            : active.init_StateCache_DataUti_UUID(entity.getUUID());
      } else {
         return null;
      }
   }

   private static StateCache.Inner_DataUtil load(ClassCache active, Entity entity) {
      StateCache.Inner_DataUtil exact = init_StateCache_DataUti_ClassCache_Entity(active, entity);
      if (exact == null && active != null && entity != null) {
         StateCache.Inner_DataUtil byId = active.init_StateCache_DataUti_I(entity.getId());
         String renderedType = init_Str_Entity(entity);
         Vec3 renderedPosition = entity.position();
         if (byId != null && init_Z_Str_Str(byId.init_Str(), renderedType) && byId.init_Vec3().distanceToSqr(renderedPosition) <= 1.0) {
            return byId;
         }

         StateCache.Inner_DataUtil closest = null;
         double closestDistanceSq = 1.0;

         for (StateCache.Inner_DataUtil truth : active.init_Iterable()) {
            if (init_Z_Str_Str(truth.init_Str(), renderedType)) {
               double distanceSq = truth.init_Vec3().distanceToSqr(renderedPosition);
               if (distanceSq <= closestDistanceSq) {
                  closest = truth;
                  closestDistanceSq = distanceSq;
               }
            }
         }

         return closest;
      } else {
         return exact;
      }
   }

   private static String init_Str_Entity(Entity entity) {
      if (entity == null) {
         return "";
      }

      try {
         return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
      } catch (RuntimeException ignored) {
         return "";
      }
   }

   private static boolean init_Z_Str_Str(String trackedType, String renderedType) {
      if (trackedType != null && renderedType != null && !renderedType.isBlank()) {
         String normalized = trackedType.trim().toLowerCase(Locale.ROOT);
         int colon = normalized.indexOf(58);
         if (colon >= 0 && colon + 1 < normalized.length()) {
            normalized = normalized.substring(colon + 1);
         }

         return normalized.equals(renderedType);
      } else {
         return false;
      }
   }

   private static Entity init_Entity_Minecraft_StateCache_DataUti(Minecraft mc, StateCache.Inner_DataUtil truth) {
      if (mc != null && mc.level != null && truth != null) {
         Entity byId = mc.level.getEntity(truth.init_I());
         if (byId == null || truth.init_UUID() != null && !truth.init_UUID().equals(byId.getUUID())) {
            if (truth.init_UUID() == null) {
               return null;
            }

            for (Entity candidate : mc.level.entitiesForRendering()) {
               if (truth.init_UUID().equals(candidate.getUUID())) {
                  return candidate;
               }
            }

            return null;
         } else {
            return byId;
         }
      } else {
         return null;
      }
   }

   public static HitResult init_HitResult_Minecraft_F(Minecraft mc, float partialTick) {
      return init_HitResult_Minecraft(mc, partialTick, null);
   }

   public static HitResult init_HitResult_Minecraft(Minecraft mc, float partialTick, HitResult vanillaHit) {
      ClassCache active = init_ClassCache;
      RemotePlayer bot = init_RemotePlayer_Minecraft(mc);
      if (active != null && bot != null && mc.level != null) {
         double blockReach = active.write_D();
         double entityReach = active.update_D();
         float hitboxMargin = 0.0F;

         try {
            AttackRange attackRange = bot.getActiveItem().get(DataComponents.ATTACK_RANGE);
            if (attackRange != null) {
               entityReach = Math.max(entityReach, attackRange.effectiveMaxRange(bot));
               hitboxMargin = attackRange.hitboxMargin();
            }
         } catch (Throwable var44) {
         }

         double targetingReach = entityReach + 0.75;
         double rayReach = Math.max(blockReach, targetingReach);
         Vec3 eye = bot.getEyePosition(partialTick);
         Vec3 look = bot.getViewVector(partialTick);
         Vec3 end = eye.add(look.scale(rayReach));
         HitResult blockHit = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, bot));
         double blockDistanceSq = blockHit != null && blockHit.getType() != Type.MISS ? eye.distanceToSqr(blockHit.getLocation()) : rayReach * rayReach;
         double maxEntityDistanceSq = targetingReach * targetingReach;
         if (vanillaHit instanceof EntityHitResult vanillaEntityHit) {
            Entity vanillaTarget = vanillaEntityHit.getEntity();
            StateCache.Inner_DataUtil vanillaTruth = load(active, vanillaTarget);
            if (vanillaTarget != bot && vanillaTruth != null && init_Z_StateCache_DataUti_Entity(vanillaTruth, vanillaTarget)) {
               Vec3 relativeHit = vanillaEntityHit.getLocation().subtract(vanillaTarget.position());
               Vec3 translatedHit = vanillaTarget == mc.player ? vanillaEntityHit.getLocation() : vanillaTruth.init_Vec3().add(relativeHit);
               double distanceSq = eye.distanceToSqr(translatedHit);
               if (distanceSq <= maxEntityDistanceSq && distanceSq < blockDistanceSq) {
                  init_PoolEntry_StreamUt = new PoolEntry.Inner_StreamUtil(
                     vanillaTarget.getId(), vanillaTarget.getUUID(), vanillaTruth.init_I(), relativeHit, vanillaTruth.init_Str()
                  );
                  return new EntityHitResult(vanillaTarget, translatedHit);
               }
            }
         }

         double bestDistanceSq = 1.0 / 0.0;
         double bestAimErrorSq = 1.0 / 0.0;
         Entity bestEntity = null;
         StateCache.Inner_DataUtil bestTruth = null;
         Vec3 bestLocation = null;
         Map<UUID, Entity> renderedByUuid = null;
         List<Entity> renderedEntities = null;

         for (StateCache.Inner_DataUtil truth : active.init_Iterable()) {
            Entity sameIdCandidate = mc.level.getEntity(truth.init_I());
            Entity candidate = sameIdCandidate;
            if (candidate == null || truth.init_UUID() != null && !truth.init_UUID().equals(candidate.getUUID())) {
               if (renderedByUuid == null) {
                  renderedByUuid = new HashMap<>(Math.max(16, mc.level.getEntityCount()));
                  renderedEntities = new ArrayList<>(Math.max(16, mc.level.getEntityCount()));

                  for (Entity rendered : mc.level.entitiesForRendering()) {
                     renderedByUuid.put(rendered.getUUID(), rendered);
                     renderedEntities.add(rendered);
                  }
               }

               candidate = truth.init_UUID() == null ? null : renderedByUuid.get(truth.init_UUID());
               if (candidate == null && load(truth, sameIdCandidate)) {
                  candidate = sameIdCandidate;
               }

               if (candidate == null) {
                  candidate = init_Entity_StateCache_DataUti_List(truth, renderedEntities);
               }
            }

            if (candidate != bot && (candidate == null || !candidate.isRemoved())) {
               AABB box;
               float pickRadius;
               if (candidate == null) {
                  if (!init_Z_Str(truth.init_Str())) {
                     continue;
                  }

                  box = init_AABB_StateCache_DataUti(truth);
                  if (box == null) {
                     continue;
                  }

                  pickRadius = 0.0F;
               } else {
                  if (!init_Z_StateCache_DataUti_Entity(truth, candidate)) {
                     continue;
                  }

                  Vec3 shift = candidate == mc.player ? Vec3.ZERO : truth.init_Vec3().subtract(candidate.position());
                  box = init_AABB_StateCache_DataUti(truth, candidate, shift);
                  pickRadius = candidate.getPickRadius();
               }

               box = box.inflate(pickRadius + hitboxMargin);
               Vec3 location;
               if (box.contains(eye)) {
                  location = eye;
               } else {
                  Optional<Vec3> clipped = box.clip(eye, end);
                  if (clipped.isPresent()) {
                     location = clipped.get();
                  } else {
                     double aimTolerance = init_D_Str(truth.init_Str());
                     location = init_Vec3_AABB(box, eye, look, targetingReach, aimTolerance);
                     if (location == null) {
                        continue;
                     }
                  }
               }

               double distanceSq = eye.distanceToSqr(location);
               double aimErrorSq = init(location, eye, look, targetingReach);
               boolean betterAim = aimErrorSq + 1.0E-7 < bestAimErrorSq;
               boolean equalAimCloser = Math.abs(aimErrorSq - bestAimErrorSq) <= 1.0E-7 && distanceSq < bestDistanceSq;
               if (distanceSq <= maxEntityDistanceSq && distanceSq < blockDistanceSq && (betterAim || equalAimCloser)) {
                  bestDistanceSq = distanceSq;
                  bestAimErrorSq = aimErrorSq;
                  bestEntity = candidate;
                  bestTruth = truth;
                  bestLocation = location;
               }
            }
         }

         if (bestTruth != null && bestLocation != null) {
            init_PoolEntry_StreamUt = new PoolEntry.Inner_StreamUtil(
               bestEntity == null ? -1 : bestEntity.getId(),
               bestEntity == null ? null : bestEntity.getUUID(),
               bestTruth.init_I(),
               bestLocation.subtract(bestEntity == mc.player ? bestEntity.position() : bestTruth.init_Vec3()),
               bestTruth.init_Str()
            );
            return bestEntity == null ? blockHit : new EntityHitResult(bestEntity, bestLocation);
         } else {
            init_PoolEntry_StreamUt = null;
            return blockHit;
         }
      } else {
         return mc.hitResult;
      }
   }

   private static double init_D_Str(String trackedType) {
      String type = write(trackedType);
      if (type.equals("armor_stand")) {
         return 0.85;
      } else {
         return type.equals("interaction") ? 0.45 : 0.2;
      }
   }

   static Vec3 init_Vec3_AABB(AABB box, Vec3 eye, Vec3 look, double reach, double tolerance) {
      if (box != null && eye != null && look != null && !(reach <= 0.0) && !(tolerance < 0.0)) {
         double lookLengthSq = look.lengthSqr();
         if (lookLengthSq < 1.0E-8) {
            return null;
         }

         Vec3 direction = look.scale(1.0 / Math.sqrt(lookLengthSq));
         Vec3 center = box.getCenter();
         double projection = Mth.clamp(center.subtract(eye).dot(direction), 0.0, reach);
         Vec3 rayPoint = eye.add(direction.scale(projection));
         Vec3 boxPoint = new Vec3(
            Mth.clamp(rayPoint.x, box.minX, box.maxX), Mth.clamp(rayPoint.y, box.minY, box.maxY), Mth.clamp(rayPoint.z, box.minZ, box.maxZ)
         );
         return rayPoint.distanceToSqr(boxPoint) <= tolerance * tolerance ? boxPoint : null;
      } else {
         return null;
      }
   }

   private static double init(Vec3 point, Vec3 eye, Vec3 look, double reach) {
      double lookLengthSq = look.lengthSqr();
      if (lookLengthSq < 1.0E-8) {
         return 1.0 / 0.0;
      }

      Vec3 direction = look.scale(1.0 / Math.sqrt(lookLengthSq));
      double projection = Mth.clamp(point.subtract(eye).dot(direction), 0.0, reach);
      return point.distanceToSqr(eye.add(direction.scale(projection)));
   }

   static AABB init_AABB_StateCache_DataUti(StateCache.Inner_DataUtil truth) {
      if (truth == null) {
         return null;
      }

      try {
         String path = write(truth.init_Str());
         if (path.isBlank()) {
            return null;
         } else {
            Identifier id = Identifier.withDefaultNamespace(path);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            if (type == null) {
               return null;
            } else {
               AABB box = type.getDimensions().makeBoundingBox(truth.init_Vec3());
               if (box.getXsize() <= 0.0 && box.getYsize() <= 0.0 && path.equals("interaction")) {
                  Vec3 pos = truth.init_Vec3();
                  return new AABB(pos.x - 0.5, pos.y, pos.z - 0.5, pos.x + 0.5, pos.y + 1.0, pos.z + 0.5);
               } else {
                  return box;
               }
            }
         }
      } catch (RuntimeException ignored) {
         return null;
      }
   }

   static boolean init_Z_Str(String trackedType) {
      String type = write(trackedType);
      return type.isBlank()
         ? false
         : !type.equals("item")
            && !type.equals("experience_orb")
            && !type.equals("area_effect_cloud")
            && !type.equals("falling_block")
            && !type.equals("firework_rocket")
            && !type.equals("fishing_bobber")
            && !type.equals("marker")
            && !type.endsWith("_display")
            && !type.contains("arrow")
            && !type.contains("projectile")
            && !type.equals("trident")
            && !type.equals("snowball")
            && !type.equals("egg")
            && !type.equals("potion")
            && !type.equals("experience_bottle")
            && !type.contains("wind_charge")
            && !type.contains("fireball")
            && !type.equals("wither_skull")
            && !type.equals("shulker_bullet")
            && !type.equals("llama_spit")
            && !type.equals("evoker_fangs");
   }

   private static String write(String trackedType) {
      if (trackedType == null) {
         return "";
      }

      String normalized = trackedType.trim().toLowerCase(Locale.ROOT);
      int colon = normalized.indexOf(58);
      return colon >= 0 && colon + 1 < normalized.length() ? normalized.substring(colon + 1) : normalized;
   }

   private static boolean init_Z_StateCache_DataUti_Entity(StateCache.Inner_DataUtil truth, Entity candidate) {
      return candidate != null && EntitySelector.CAN_BE_PICKED.test(candidate);
   }

   private static AABB init_AABB_StateCache_DataUti(StateCache.Inner_DataUtil truth, Entity candidate, Vec3 shift) {
      return candidate.getBoundingBox().move(shift);
   }

   private static StateCache.Inner_DataUtil read(ClassCache active, Entity entity) {
      PoolEntry.Inner_StreamUtil picked = init_PoolEntry_StreamUt;
      if (picked != null && picked.init(entity)) {
         StateCache.Inner_DataUtil truth = active.init_StateCache_DataUti_I(picked.load());
         if (truth != null) {
            return truth;
         }
      }

      return load(active, entity);
   }

   private static PoolEntry.Inner_StreamUtil init_PoolEntry_StreamUt_HitResult(HitResult hit) {
      PoolEntry.Inner_StreamUtil picked = init_PoolEntry_StreamUt;
      if (picked == null) {
         return null;
      } else if (hit instanceof EntityHitResult entityHit) {
         return picked.init(entityHit.getEntity()) ? picked : null;
      } else {
         return picked.init_Z() ? picked : null;
      }
   }

   private static boolean load(StateCache.Inner_DataUtil truth, Entity candidate) {
      return truth != null
         && candidate != null
         && init_Z_Str_Str(truth.init_Str(), init_Str_Entity(candidate))
         && truth.init_Vec3().distanceToSqr(candidate.position()) <= 1.0;
   }

   private static Entity init_Entity_StateCache_DataUti_List(StateCache.Inner_DataUtil truth, List<Entity> rendered) {
      if (truth != null && rendered != null) {
         Entity closest = null;
         double closestDistanceSq = 1.0;

         for (Entity candidate : rendered) {
            if (init_Z_Str_Str(truth.init_Str(), init_Str_Entity(candidate))) {
               double distanceSq = truth.init_Vec3().distanceToSqr(candidate.position());
               if (distanceSq <= closestDistanceSq) {
                  closest = candidate;
                  closestDistanceSq = distanceSq;
               }
            }
         }

         return closest;
      } else {
         return null;
      }
   }

   public static boolean read_Z() {
      return init_Z;
   }

   static void load(ClassCache pilotSession, RemotePlayer bot) {
      PkgCls_a.init_V_Str("POV ownership changed");
      init_ClassCache = pilotSession;
      init_I = bot.getId();
      init_PoolEntry_StreamUt = null;
      RingBuffer.init(pilotSession.open_Str());
      init_J = pilotSession.load_J();
      load_J = pilotSession.init_J();
      read_J = pilotSession.poll_J();
      write_J = pilotSession.push_J();
      init_Input = Input.EMPTY;
      load_Input = Input.EMPTY;
      init_Z = false;
      load_Z = false;
      read_Z = false;
      write_Z = false;
      read_I = 0;
      add = false;
      update_Z = false;
      sync_Z = pilotSession.update_Z();
      flush_Z = pilotSession.valid_Z();
      close = AutismMultiOverlay.write_Z_Str(pilotSession.open_Str());
      poll = false;
      read_V();
      Vec3 truth = Compat263.interpPos(bot.getInterpolation());
      init_Vec3 = truth == null ? bot.position() : truth;
      load_Vec3 = Vec3.ZERO;
      init_F = Compat263.interpYRot(bot.getInterpolation());
      load_F = Compat263.interpXRot(bot.getInterpolation());
      push = bot.onGround();
      pop = false;
      tick = true;
      run = false;
      get = false;
      set = false;
      TokenStore.init_V_ClassCache_RemotePlayer(pilotSession, bot);
      init_TableIndex.init_V(init_Vec3, System.currentTimeMillis());
      Compat263.interpSetLength(bot.getInterpolation(), 1);
      Compat263.interpTo(bot.getInterpolation(), init_Vec3, init_F, load_F);
      sync(bot);
      flush_J = -9223372036854775808L;
      load_I = -1;
      flush(bot);
   }

   static void init_V_Minecraft(Minecraft mc) {
      if (mc != null && mc.player != null) {
         LocalPlayer self = mc.player;
         boolean wasSprinting = self.isSprinting();
         ax.load_V();
         if (self.input != null) {
            self.input.tick();
         }

         self.xxa = 0.0F;
         self.zza = 0.0F;
         self.setJumping(false);
         self.setShiftKeyDown(false);
         self.setSprinting(false);
         self.setDeltaMovement(init_Vec3_Vec3(self.getDeltaMovement()));
         if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundPlayerInputPacket(Input.EMPTY));
            if (wasSprinting) {
               mc.getConnection().send(new ServerboundPlayerCommandPacket(self, Action.STOP_SPRINTING));
            }
         }
      }
   }

   static Vec3 init_Vec3_Vec3(Vec3 velocity) {
      return velocity == null ? Vec3.ZERO : new Vec3(0.0, velocity.y, 0.0);
   }

   static boolean init_Z_Z(boolean mainPlayer, int entityId, int controlledEntityId) {
      return !mainPlayer && entityId != controlledEntityId;
   }

   static void init_V_RemotePlayer(RemotePlayer bot) {
      ClassCache s = init_ClassCache;
      if (s != null) {
         PkgCls_a.init_V_b_Str(s, "POV exited");
      }

      init_PoolEntry_StreamUt = null;
      RingBuffer.init();
      if (s != null && put && init_BlockPos != null) {
         try {
            s.init_Z_Packet(
               new ServerboundPlayerActionPacket(
                  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                  init_BlockPos,
                  init_Direction == null ? Direction.DOWN : init_Direction
               )
            );
         } catch (Throwable var5) {
         }
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && init_BlockPos != null) {
         mc.level.destroyBlockProgress(init_I, init_BlockPos, -1);
      }

      read_V();
      write(bot);
      TokenStore.init_V(bot);
      if (s != null && !close) {
         try {
            AutismMultiOverlay.sync_V_Str(s.open_Str());
         } catch (Throwable var4) {
         }
      }

      update_Z = false;
      sync_Z = false;
      flush_Z = false;
      if (bot != null) {
         bot.noPhysics = true;
         Compat263.interpSetLength(bot.getInterpolation(), 3);
         bot.setSprinting(false);
         bot.setShiftKeyDown(false);
      }

      init_ClassCache = null;
      init_I = -1;
      poll = false;
      tick = false;
      get = false;
      set = false;
   }

   private static void read_V() {
      put = false;
      init_BlockPos = null;
      init_Direction = null;
      read_F = 0.0F;
      write_I = 0;
      update_I = -1;
   }

   public static boolean init_Z_RemotePlayer(RemotePlayer bot) {
      open = false;
      ClassCache s = init_ClassCache;
      Minecraft mc = Minecraft.getInstance();
      if (s == null || mc.player == null) {
         return false;
      }

      if (!s.next_Z()) {
         run = false;
         load_Input = Input.EMPTY;
         init_Z = false;
         load_Z = false;
         read_Z = false;
         init_PoolEntry_StreamUt = null;
         if (put) {
            load_V(mc);
            read_V();
         }

         return false;
      } else {
         if (get) {
            read(s, bot);
            if (s.open_Z()) {
               run = false;
               load_Input = Input.EMPTY;
               init_Z = false;
               load_Z = false;
               read_Z = false;
               bot.setDeltaMovement(Vec3.ZERO);
               sync(bot);
               return false;
            }

            get = false;
         }

         boolean pacedOwned = PkgCls_a.init_Z_b(s);
         if (pacedOwned || set) {
            read(s, bot);
            set = pacedOwned;
            if (pacedOwned) {
               run = false;
               load_Input = Input.EMPTY;
               init_Z = false;
               load_Z = false;
               read_Z = false;
               bot.setDeltaMovement(Vec3.ZERO);
               sync(bot);
               return false;
            }
         }

         Vec3 observedTruth = Compat263.interpPos(bot.getInterpolation());
         if (observedTruth == null) {
            observedTruth = bot.position();
         }

         s.init_V_Vec3(observedTruth);
         if (poll) {
            poll = false;
            PositionMoveRotation authoritative = s.init_PositionMoveRotati();
            if (authoritative != null && authoritative.position() != null) {
               init_Vec3 = authoritative.position();
               load_Vec3 = authoritative.deltaMovement() == null ? Vec3.ZERO : authoritative.deltaMovement();
               init_F = authoritative.yRot();
               load_F = authoritative.xRot();
            }

            push = bot.onGround();
            pop = false;
            tick = true;
            init_TableIndex.init_V(observedTruth, System.currentTimeMillis());
            init_Input = Input.EMPTY;
            load_Input = Input.EMPTY;
            write_Z = false;
         }

         if (!tick) {
            init_Vec3 = observedTruth;
            load_Vec3 = Vec3.ZERO;
            init_F = Compat263.interpYRot(bot.getInterpolation());
            load_F = Compat263.interpXRot(bot.getInterpolation());
            push = bot.onGround();
            tick = true;
            init_TableIndex.init_V(observedTruth, System.currentTimeMillis());
         } else {
            init_TableIndex.init_TableIndex_DataUti(observedTruth, System.currentTimeMillis());
         }

         long seq = s.load_J();
         if (seq != init_J) {
            init_J = seq;
            PositionMoveRotation corrected = s.init_PositionMoveRotati();
            if (corrected != null && corrected.position() != null) {
               init_Vec3 = corrected.position();
               load_Vec3 = corrected.deltaMovement() == null ? Vec3.ZERO : corrected.deltaMovement();
               init_F = corrected.yRot();
               load_F = corrected.xRot();
               push = false;
               pop = false;
            }

            init_TableIndex.init_V(observedTruth, System.currentTimeMillis());
            sync(bot);
            return false;
         } else if (!bot.level().hasChunk(Mth.floor(init_Vec3.x) >> 4, Mth.floor(init_Vec3.z) >> 4)) {
            load_Vec3 = Vec3.ZERO;
            init_Z = false;
            load_Input = Input.EMPTY;
            if (read_Z) {
               read_Z = false;
            }

            return false;
         } else {
            Vec3 impulse = s.init_Vec3();
            if (impulse != null) {
               load_Vec3 = impulse;
            }

            run = true;
            sync(bot);
            boolean allowed = write(mc);
            boolean kf = allowed && init_Z_KeyMapping(mc.options.keyUp);
            boolean kb = allowed && init_Z_KeyMapping(mc.options.keyDown);
            boolean kl = allowed && init_Z_KeyMapping(mc.options.keyLeft);
            boolean kr = allowed && init_Z_KeyMapping(mc.options.keyRight);
            boolean kjump = allowed && init_Z_KeyMapping(mc.options.keyJump);
            boolean kshift = allowed && init_Z_KeyMapping(mc.options.keyShift);
            boolean ksprint = allowed && init_Z_KeyMapping(mc.options.keySprint);
            float forwardImpulse = (kf ? 1.0F : 0.0F) - (kb ? 1.0F : 0.0F);
            float leftImpulse = (kl ? 1.0F : 0.0F) - (kr ? 1.0F : 0.0F);
            Vec2 move = new Vec2(leftImpulse, forwardImpulse);
            if (move.lengthSquared() > 1.0F) {
               move = move.normalized();
            }

            boolean flightNoSneak = TokenStore.init_Z((Player)bot);
            boolean shiftInput = kshift && !flightNoSneak || TokenStore.init_Z(bot);
            load_Z = shiftInput && !TokenStore.load((Player)bot);
            bot.setShiftKeyDown(load_Z);
            if (move.lengthSquared() > 0.0F) {
               if (bot.isUsingItem() && !bot.isPassenger()) {
                  move = move.scale(init_F_RemotePlayer(bot));
               }

               if (load_Z) {
                  move = move.scale((float)bot.getAttributeValue(Attributes.SNEAKING_SPEED));
               }

               move = init_Vec2_Vec2(move);
            }

            boolean hungerOk = s.init_I() > 6;
            boolean wantSprint = TokenStore.init(kf, kb, kl, kr, load_Z, bot.isUsingItem(), hungerOk, bot.horizontalCollision);
            if (wantSprint != read_Z) {
               read_Z = wantSprint;
               bot.setSprinting(read_Z);
            }

            load_Input = new Input(kf, kb, kl, kr, kjump, shiftInput, ksprint || read_Z);
            bot.xxa = move.x;
            bot.zza = move.y;
            init_Z = kjump;
            TokenStore.init_V_RemotePlayer(bot, kjump, shiftInput);
            open = true;
            return true;
         }
      }
   }

   private static void sync(RemotePlayer bot) {
      bot.setPosRaw(init_Vec3.x, init_Vec3.y, init_Vec3.z);
      bot.setDeltaMovement(load_Vec3);
      bot.setYRot(init_F);
      bot.setXRot(load_F);
      bot.setYHeadRot(init_F);
      bot.setOnGround(push);
      bot.noPhysics = false;
      bot.horizontalCollision = pop;
      bot.setSprinting(read_Z);
   }

   private static boolean init_Z_KeyMapping(KeyMapping mapping) {
      return mapping != null && aB.init(mapping).autism$isActuallyDown();
   }

   private static boolean write(Minecraft mc) {
      if (aC.init_Z()) {
         return false;
      }

      try {
         if (bk.init_bk().update_Z()) {
            return false;
         }
      } catch (Throwable var2) {
      }

      Screen screen = mc.gui.screen();
      if (screen == null) {
         return true;
      } else if (screen instanceof ChatScreen) {
         return false;
      } else {
         return !(screen instanceof AbstractSignEditScreen) && !(screen instanceof BookEditScreen) ? !(screen.getFocused() instanceof EditBox) : false;
      }
   }

   public static void load(RemotePlayer bot) {
      ClassCache s = init_ClassCache;
      Minecraft mc = Minecraft.getInstance();
      if (s != null && mc.player != null) {
         try {
            if (!load_Input.equals(init_Input)) {
               init_Input = load_Input;
               s.init_Z_Packet(new ServerboundPlayerInputPacket(load_Input));
            }

            if (read_Z != write_Z) {
               write_Z = read_Z;
               s.init_Z_Entity_ServerboundPlayerC(bot, read_Z ? Action.START_SPRINTING : Action.STOP_SPRINTING);
            }

            if (open && s.load_J() == init_J) {
               init_Vec3 = bot.position();
               load_Vec3 = bot.getDeltaMovement();
               init_F = bot.getYRot();
               load_F = bot.getXRot();
               push = bot.onGround();
               pop = bot.horizontalCollision;
               s.init_V_Vec3(init_Vec3, load_Vec3, init_F, load_F, push, pop);
               init_TableIndex.load(init_Vec3, System.currentTimeMillis());
            } else if (s.load_J() != init_J) {
               PositionMoveRotation corrected = s.init_PositionMoveRotati();
               if (corrected != null && corrected.position() != null) {
                  init_Vec3 = corrected.position();
                  load_Vec3 = corrected.deltaMovement() == null ? Vec3.ZERO : corrected.deltaMovement();
                  init_F = corrected.yRot();
                  load_F = corrected.xRot();
                  push = false;
               }
            }
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("POV pilot post-physics failed", error);
         } finally {
            run = false;
         }

         try {
            init_V_Minecraft(mc, bot, s);
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("POV pilot upkeep failed", error);
         }
      } else {
         write(bot);
      }
   }

   public static void read(RemotePlayer bot) {
      ClassCache s = init_ClassCache;
      if (s != null) {
         try {
            init_V_Minecraft(Minecraft.getInstance(), bot, s);
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("POV passive upkeep failed", error);
         }
      }
   }

   public static void write(RemotePlayer bot) {
      run = false;
   }

   private static void init_V_Minecraft(Minecraft mc, RemotePlayer bot, ClassCache s) {
      if (read_I > 0) {
         read_I--;
      }

      if (s.next_Z() && !write_Z()) {
         TokenStore.init(mc, bot, put, init_BlockPos, init_Direction);
      }

      if (s.next_Z() && !write_Z()) {
         load(mc, bot, s);
      }

      flush(bot);
      if (sync_Z) {
         sync_Z = false;
         if (s.update_Z()) {
            update();
         }
      }

      if (flush_Z) {
         flush_Z = false;
         if (s.valid_Z()) {
            load(mc, s);
         }
      }

      long guiSeq = s.init_J();
      if (guiSeq != load_J) {
         load_J = guiSeq;
         update();
      }

      if (update_Z && !AutismMultiOverlay.write_Z_Str(s.open_Str())) {
         update_Z = false;
         if (!write_Z()) {
            s.flush_V();
         }
      }

      long signSeq = s.poll_J();
      if (signSeq != read_J) {
         read_J = signSeq;
         load(mc, s);
      }

      long bookSeq = s.push_J();
      if (bookSeq != write_J) {
         write_J = bookSeq;
         read(mc, bot, s);
      }
   }

   public static void update(RemotePlayer bot) {
      ClassCache s = init_ClassCache;
      if (s != null) {
         Vec3 observed = Compat263.interpPos(bot.getInterpolation());
         s.init_V_Vec3(observed == null ? bot.position() : observed);
         if (!poll) {
            poll = true;
            Minecraft mc = Minecraft.getInstance();
            if (put) {
               init_V_Minecraft_ClassCache(mc, s);
            }

            TokenStore.load(bot);
            load_Input = Input.EMPTY;
            init_Z = false;
            load_Z = false;
            read_Z = false;
            write_Z = false;
            open = false;
            bot.noPhysics = true;
         }

         load_Input = Input.EMPTY;
         init_Z = false;
         read_Z = false;
         open = false;

         try {
            init_V_Minecraft(Minecraft.getInstance(), bot, s);
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("POV macro observer upkeep failed", error);
         }
      }
   }

   private static boolean write_Z() {
      ClassCache s = init_ClassCache;
      return s != null && s.put_Z();
   }

   private static void write_V() {
      long now = System.currentTimeMillis();
      if (now - update_J >= 2000L) {
         update_J = now;
         bi.init("Macro controls this bot. Stop the macro to use manual POV controls.", -866997);
      }
   }

   private static float init_F_RemotePlayer(RemotePlayer bot) {
      try {
         return bot.getUseItem().getOrDefault(DataComponents.USE_EFFECTS, UseEffects.DEFAULT).speedMultiplier();
      } catch (Throwable ignored) {
         return 0.2F;
      }
   }

   private static Vec2 init_Vec2_Vec2(Vec2 input) {
      float length = input.length();
      if (length <= 0.0F) {
         return input;
      }

      Vec2 direction = input.scale(1.0F / length);
      float dx = Math.abs(direction.x);
      float dy = Math.abs(direction.y);
      float tan = dy > dx ? dx / dy : dy / dx;
      float distanceToUnitSquare = Mth.sqrt(1.0F + Mth.square(tan));
      return direction.scale(Math.min(length * distanceToUnitSquare, 1.0F));
   }

   private static void flush(RemotePlayer bot) {
      ClassCache s = init_ClassCache;
      if (s != null) {
         long revision = s.open_J();
         if (revision != flush_J) {
            flush_J = revision;
            List<ItemStack> inv = s.load_List();

            for (int handler = 0; handler < inv.size(); handler++) {
               int slot = init_I_I(handler);
               if (slot >= 0) {
                  ItemStack want = inv.get(handler);
                  if (!ItemStack.matches(bot.getInventory().getItem(slot), want)) {
                     bot.getInventory().setItem(slot, want);
                  }
               }
            }
         }

         int selected = s.load_I();
         if (selected != load_I && selected >= 0 && selected <= 8) {
            load_I = selected;
            bot.getInventory().setSelectedSlot(selected);
         }

         bot.getFoodData().setFoodLevel(s.init_I());
      }
   }

   private static int init_I_I(int handler) {
      if (handler >= 36 && handler <= 44) {
         return handler - 36;
      } else if (handler >= 9 && handler <= 35) {
         return handler;
      } else if (handler >= 5 && handler <= 8) {
         return 39 - (handler - 5);
      } else {
         return handler == 45 ? 40 : -1;
      }
   }

   public static boolean init_Z_Minecraft(Minecraft mc) {
      ClassCache s = init_ClassCache;
      if (s == null) {
         return false;
      }

      if (write_Z()) {
         write_V();
         return true;
      }

      if (aC.init_Z()) {
         return true;
      }

      if (!s.next_Z()) {
         return true;
      }

      if (mc.hitResult == null) {
         return true;
      }

      RemotePlayer bot = init_RemotePlayer_Minecraft(mc);
      if (bot == null) {
         return true;
      }

      try {
         HitResult pilotHit = mc.hitResult;
         PoolEntry.Inner_StreamUtil picked = init_PoolEntry_StreamUt_HitResult(pilotHit);
         if (picked != null) {
            if (!s.write_Z_I(picked.load())) {
               init_V_Str("attack packet blocked or disconnected");
            }

            Compat263.swing(bot, InteractionHand.MAIN_HAND);
            return true;
         }

         switch (pilotHit.getType()) {
            case BLOCK:
               BlockHitResult hit = (BlockHitResult)pilotHit;
               if (!TokenStore.init_Z_Minecraft_BlockPos(mc, hit.getBlockPos())) {
                  init_V_Minecraft_2(mc, bot, s, hit.getBlockPos(), hit.getDirection());
               }
               break;
            case ENTITY:
               Entity target = ((EntityHitResult)pilotHit).getEntity();
               if (target != bot) {
                  StateCache.Inner_DataUtil targetTruth = read(s, target);
                  int wireId = targetTruth == null ? -1 : targetTruth.init_I();
                  if (!s.write_Z_I(wireId)) {
                     init_V_Str("attack packet blocked or disconnected");
                  }
               }
               break;
            default:
               s.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND));
         }

         Compat263.swing(bot, InteractionHand.MAIN_HAND);
      } catch (Throwable error) {
         AutismClientAddon.LOG.warn("POV pilot attack failed", error);
      }

      return true;
   }

   private static void init_V_Str(String detail) {
      long now = System.currentTimeMillis();
      if (now - sync_J >= 2000L) {
         sync_J = now;
         String suffix = detail != null && !detail.isBlank() ? " (" + detail + ")" : "";
         bi.init("POV action was not sent. Check the Multi packet policy or connection" + suffix + ".", -42393);
      }
   }

   public static void init_V_Minecraft_Z(Minecraft mc, boolean down) {
      ClassCache s = init_ClassCache;
      if (s != null) {
         if (write_Z()) {
            if (put) {
               init_V_Minecraft_ClassCache(mc, s);
            }

            if (down) {
               write_V();
            }
         } else if (!s.next_Z()) {
            if (put) {
               load_V(mc);
               read_V();
            }
         } else {
            if (aC.init_Z()) {
               down = false;
            }

            RemotePlayer bot = init_RemotePlayer_Minecraft(mc);
            if (bot != null) {
               try {
                  if (!down || !(mc.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK)) {
                     init_V_Minecraft_ClassCache(mc, s);
                     return;
                  }

                  if (write_I > 0) {
                     write_I--;
                     return;
                  }

                  if (!put) {
                     init_V_Minecraft_2(mc, bot, s, hit.getBlockPos(), hit.getDirection());
                     if (put) {
                        init_V_RemotePlayer_ClassCache(bot, s);
                     }

                     return;
                  }

                  if (!hit.getBlockPos().equals(init_BlockPos)) {
                     init_V_Minecraft_ClassCache(mc, s);
                     init_V_Minecraft_2(mc, bot, s, hit.getBlockPos(), hit.getDirection());
                  }

                  if (put) {
                     init_V_RemotePlayer_ClassCache(bot, s);
                  }
               } catch (Throwable error) {
                  AutismClientAddon.LOG.warn("POV pilot continue-attack failed", error);
               }
            }
         }
      }
   }

   private static void init_V_RemotePlayer_ClassCache(RemotePlayer bot, ClassCache s) {
      Compat263.swing(bot, InteractionHand.MAIN_HAND);
      s.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND));
   }

   private static void init_V_Minecraft_2(Minecraft mc, RemotePlayer bot, ClassCache s, BlockPos pos, Direction dir) {
      if (mc.level != null && !mc.level.getBlockState(pos).isAir() && !TokenStore.init_Z_Minecraft_BlockPos(mc, pos)) {
         BlockState state = mc.level.getBlockState(pos);
         TokenStore.init(bot, pos, dir, state);

         for (Packet<?> packet : init_List_BlockPos(pos, dir, s.pop_I())) {
            s.init_Z_Packet(packet);
         }

         float baseProgress = state.getDestroyProgress(bot, mc.level, pos) * s.sync_F();
         if (TokenStore.init(bot, state, pos, baseProgress)) {
            s.init_Z_Packet(
               new ServerboundPlayerActionPacket(
                  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, dir, s.pop_I()
               )
            );
            TokenStore.load(pos, dir);
            write_I = 5;
            TokenStore.init(true);
            load_V(mc);
            put = false;
            init_BlockPos = null;
            init_Direction = null;
         } else {
            boolean creative = s.push_I() == 1;
            if (!creative && !(baseProgress >= 1.0F)) {
               put = true;
               init_BlockPos = pos;
               init_Direction = dir;
               read_F = 0.0F;
               update_I = -1;
            } else {
               write_I = 5;
               TokenStore.init(true);
               load_V(mc);
               put = false;
               init_BlockPos = null;
               init_Direction = null;
            }
         }
      }
   }

   static List<Packet<?>> init_List_BlockPos(BlockPos pos, Direction dir, int sequence) {
      return List.of(
         new ServerboundPlayerActionPacket(
            net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, dir == null ? Direction.UP : dir, sequence
         ),
         new CompatSwingPacket(InteractionHand.MAIN_HAND)
      );
   }

   private static void init_V_Minecraft_ClassCache(Minecraft mc, ClassCache s) {
      if (put && init_BlockPos != null) {
         s.init_Z_Packet(
            new ServerboundPlayerActionPacket(
               net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
               init_BlockPos,
               init_Direction == null ? Direction.DOWN : init_Direction
            )
         );
         TokenStore.init(true);
         load_V(mc);
         read_V();
      }
   }

   private static void load_V(Minecraft mc) {
      if (mc.level != null && init_BlockPos != null) {
         mc.level.destroyBlockProgress(init_I, init_BlockPos, -1);
      }
   }

   private static void load(Minecraft mc, RemotePlayer bot, ClassCache s) {
      if (put && init_BlockPos != null && mc.level != null) {
         BlockState state = mc.level.getBlockState(init_BlockPos);
         if (state.isAir()) {
            TokenStore.init(true);
            load_V(mc);
            read_V();
         } else {
            TokenStore.init_V_BlockPos_Direction(init_BlockPos, init_Direction);
            float delta = state.getDestroyProgress(bot, mc.level, init_BlockPos) * s.sync_F();
            delta = TokenStore.init_F_F_BlockState(delta, state);
            read_F = TokenStore.init_F_F_F(read_F, delta);
            int stage = read_F > 0.0F ? (int)(read_F * 10.0F) : -1;
            if (stage != update_I) {
               update_I = stage;
               mc.level.destroyBlockProgress(init_I, init_BlockPos, Math.min(stage, 9));
            }

            if (read_F >= 1.0F) {
               s.init_Z_Packet(
                  new ServerboundPlayerActionPacket(
                     net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, init_BlockPos, init_Direction, s.pop_I()
                  )
               );
               TokenStore.load(init_BlockPos, init_Direction);
               TokenStore.init(true);
               load_V(mc);
               read_V();
               write_I = 5;
            }
         }
      }
   }

   public static boolean load_Z(Minecraft mc) {
      ClassCache s = init_ClassCache;
      if (s == null) {
         return false;
      }

      if (write_Z()) {
         write_V();
         return true;
      }

      if (aC.init_Z()) {
         return true;
      }

      if (!s.next_Z()) {
         return true;
      }

      if (read_I > 0) {
         return true;
      }

      RemotePlayer bot = init_RemotePlayer_Minecraft(mc);
      if (bot == null) {
         return true;
      }

      if (bot.isUsingItem()) {
         return true;
      }

      read_I = 4;

      try {
         HitResult hit = mc.hitResult;
         InteractionHand hand = InteractionHand.MAIN_HAND;
         ItemStack mainStack = bot.getItemInHand(InteractionHand.MAIN_HAND);
         ItemStack offStack = bot.getItemInHand(InteractionHand.OFF_HAND);
         PoolEntry.Inner_StreamUtil picked = init_PoolEntry_StreamUt_HitResult(hit);
         if (picked != null) {
            Entity renderedTarget = hit instanceof EntityHitResult entityHit && picked.init(entityHit.getEntity()) ? entityHit.getEntity() : null;
            init_V_Minecraft(mc, s, bot, renderedTarget, picked);
            return true;
         }

         autismclient.modules.StreamUtil.Inner_DataUtil airPlacement = autismclient.modules.StreamUtil.init(bot, hit);
         if (airPlacement != null) {
            if (al.init_Z_HitResult(airPlacement.init_BlockHitResult(), airPlacement.init_InteractionHand(), "air-place")) {
               return true;
            }

            s.init_Z_Packet(new ServerboundUseItemOnPacket(airPlacement.init_InteractionHand(), airPlacement.init_BlockHitResult(), s.pop_I()));
            s.init_Z_Packet(new CompatSwingPacket(airPlacement.init_InteractionHand()));
            Compat263.swing(bot, airPlacement.init_InteractionHand());
            return true;
         }

         boolean fishingRod = mainStack != null && mainStack.is(Items.FISHING_ROD);
         if (!fishingRod && offStack != null && offStack.is(Items.FISHING_ROD)) {
            fishingRod = true;
            hand = InteractionHand.OFF_HAND;
         }

         if (fishingRod) {
            s.init_Z_Packet(new ServerboundUseItemPacket(hand, s.pop_I(), init_F, load_F));
         } else {
            if (hit instanceof EntityHitResult entityHit && hit.getType() == Type.ENTITY) {
               Entity target = entityHit.getEntity();
               StateCache.Inner_DataUtil targetTruth = read(s, target);
               Vec3 targetPosition = targetTruth == null ? target.position() : targetTruth.init_Vec3();
               Vec3 location = entityHit.getLocation().subtract(targetPosition);
               int wireId = targetTruth == null ? -1 : targetTruth.init_I();
               if (wireId >= 0) {
                  init(mc, s, bot, target, wireId, location);
               } else {
                  init_V_Str("entity is not tracked on the bot connection");
               }

               return true;
            }

            if (hit instanceof BlockHitResult blockHit && hit.getType() == Type.BLOCK) {
               s.init_Z_Packet(new ServerboundUseItemOnPacket(hand, blockHit, s.pop_I()));
            } else {
               ItemStack held = mainStack;
               if (held == null || !held.is(Items.WRITABLE_BOOK)) {
                  ItemStack off = bot.getItemInHand(InteractionHand.OFF_HAND);
                  if (off != null && off.is(Items.WRITABLE_BOOK)) {
                     hand = InteractionHand.OFF_HAND;
                     held = off;
                  }
               }

               s.init_Z_Packet(new ServerboundUseItemPacket(hand, s.pop_I(), init_F, load_F));
               if (held != null && held.is(Items.WRITABLE_BOOK)) {
                  mc.gui.setScreen(new BookEditScreen(bot, held, hand, held.getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY)));
               }
            }
         }

         s.init_Z_Packet(new CompatSwingPacket(hand));
         Compat263.swing(bot, hand);
      } catch (Throwable ignored) {
         init_V_Str("interaction failed");
      }

      return true;
   }

   private static void init(Minecraft mc, ClassCache session, RemotePlayer bot, Entity target, int wireId, Vec3 location) {
      if (mc.gameMode == null) {
         init_V_Str("game mode is unavailable");
      } else {
         boolean sentAny = false;
         EntityHitResult vanillaHit = new EntityHitResult(target, target.position().add(location));

         for (InteractionHand candidateHand : InteractionHand.values()) {
            PoolEntry.Inner_CachePool route = new PoolEntry.Inner_CachePool(session, wireId);
            init_ThreadLocal.set(route);

            InteractionResult result;
            try {
               result = mc.gameMode.interact(bot, target, vanillaHit, candidateHand);
            } finally {
               init_ThreadLocal.remove();
            }

            sentAny |= route.load;
            if (!route.init_Z) {
               init_V_Str("vanilla packet route was not captured");
               return;
            }

            if (route.load && result instanceof Success success) {
               if (success.swingSource() == SwingSource.PREDICTED) {
                  session.init_Z_Packet(new CompatSwingPacket(candidateHand));
                  Compat263.swing(bot, candidateHand);
               }

               return;
            }
         }

         if (!sentAny) {
            init_V_Str("entity interaction blocked or disconnected");
         }
      }
   }

   private static void init_V_Minecraft(Minecraft mc, ClassCache session, RemotePlayer bot, Entity renderedTarget, PoolEntry.Inner_StreamUtil picked) {
      if (renderedTarget != null) {
         init(mc, session, bot, renderedTarget, picked.load(), picked.init_Vec3());
      } else if (!session.init_Z_I(picked.load(), InteractionHand.MAIN_HAND, picked.init_Vec3(), load_Z)) {
         init_V_Str("entity interaction blocked or disconnected");
      }
   }

   public static boolean init_Z_ServerboundInterac(ServerboundInteractPacket packet) {
      PoolEntry.Inner_CachePool route = init_ThreadLocal.get();
      if (route != null && packet != null) {
         route.init_Z = true;
         route.load = route.init_ClassCache.init_Z_I(route.init_I, packet.hand(), packet.location(), packet.usingSecondaryAction());
         return true;
      } else {
         return false;
      }
   }

   private static void update() {
      ClassCache s = init_ClassCache;
      if (s != null) {
         AutismMultiOverlay.update_V_Str(s.open_Str());
         update_Z = true;
      }
   }

   private static void load(Minecraft mc, ClassCache s) {
      try {
         BlockPos pos = s.init_BlockPos();
         if (pos == null || mc.level == null) {
            return;
         }

         if (!(mc.level.getBlockEntity(pos) instanceof SignBlockEntity sign)) {
            return;
         }

         boolean front = s.prev_Z();
         net.minecraft.world.level.block.Block block = sign.getBlockState().getBlock();
         boolean hanging = block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock;
         mc.gui
            .setScreen(
               hanging
                  ? Compat263.newHangingSignEditScreen(sign, front, mc.isTextFilteringEnabled())
                  : Compat263.newSignEditScreen(sign, front, mc.isTextFilteringEnabled())
            );
      } catch (Throwable error) {
         AutismClientAddon.LOG.warn("POV pilot sign editor open failed", error);
      }
   }

   private static void read(Minecraft mc, RemotePlayer bot, ClassCache s) {
      try {
         if (bot == null) {
            return;
         }

         ItemStack stack = bot.getItemInHand(s.init_InteractionHand());
         if (stack == null || stack.isEmpty()) {
            return;
         }

         mc.gui.setScreen(new BookViewScreen(BookAccess.fromItem(stack)));
      } catch (Throwable error) {
         AutismClientAddon.LOG.warn("POV pilot book open failed", error);
      }
   }

   public static boolean init_Z_Packet(Packet<?> packet) {
      ClassCache s = init_ClassCache;
      if (s == null) {
         return false;
      }

      if (write_Z()) {
         write_V();
         return true;
      }

      try {
         if (packet instanceof ServerboundEditBookPacket book && book.slot() >= 0 && book.slot() <= 8) {
            packet = new ServerboundEditBookPacket(Math.max(0, Math.min(8, s.load_I())), book.pages(), book.title());
         }

         s.init_Z_Packet(packet);
         if (packet instanceof ServerboundSignUpdatePacket) {
            s.close_V();
         }
      } catch (Throwable error) {
         AutismClientAddon.LOG.warn("POV pilot edit reroute failed", error);
      }

      return true;
   }

   public static void load_V() {
      ClassCache s = init_ClassCache;
      if (s != null && !write_Z()) {
         s.init_Z_Packet(
            new ServerboundPlayerActionPacket(
               net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
            )
         );
      }
   }

   public static boolean read(Minecraft mc) {
      ClassCache s = init_ClassCache;
      if (s == null || mc.options == null) {
         return false;
      }

      if (aC.init_Z()) {
         return false;
      }

      try {
         if (write_Z()) {
            boolean attempted = init_Z_KeyMapping(mc.options.keyUp)
               || init_Z_KeyMapping(mc.options.keyDown)
               || init_Z_KeyMapping(mc.options.keyLeft)
               || init_Z_KeyMapping(mc.options.keyRight)
               || init_Z_KeyMapping(mc.options.keyJump)
               || init_Z_KeyMapping(mc.options.keyShift)
               || init_Z_KeyMapping(mc.options.keySprint);

            for (KeyMapping key : mc.options.keyHotbarSlots) {
               while (key.consumeClick()) {
                  attempted = true;
               }
            }

            while (mc.options.keyDrop.consumeClick()) {
               attempted = true;
            }

            while (mc.options.keySwapOffhand.consumeClick()) {
               attempted = true;
            }

            while (mc.options.keyPickItem.consumeClick()) {
               attempted = true;
            }

            boolean openViewer = false;

            while (mc.options.keyInventory.consumeClick()) {
               openViewer = true;
            }

            if (openViewer) {
               update();
            }

            if (attempted) {
               write_V();
            }

            add = init_Z_KeyMapping(mc.options.keyUse);
            return false;
         }

         for (int i = 0; i < mc.options.keyHotbarSlots.length && i < 9; i++) {
            boolean pressed = false;

            while (mc.options.keyHotbarSlots[i].consumeClick()) {
               pressed = true;
            }

            if (pressed) {
               s.read_Str_I(i);
            }
         }

         while (mc.options.keyDrop.consumeClick()) {
            s.init_Str_Z(update(mc));
         }

         while (mc.options.keySwapOffhand.consumeClick()) {
            s.init_Z_Packet(
               new ServerboundPlayerActionPacket(
                  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN
               )
            );
         }

         boolean openViewer = false;

         while (mc.options.keyInventory.consumeClick()) {
            openViewer = true;
         }

         if (openViewer) {
            update();
         }

         while (mc.options.keyPickItem.consumeClick()) {
         }

         boolean useDown = init_Z_KeyMapping(mc.options.keyUse);
         if (add && !useDown) {
            load_V();
         }

         add = useDown;
      } catch (Throwable error) {
         AutismClientAddon.LOG.warn("POV pilot keybind drain failed", error);
      }

      return false;
   }

   public static boolean init_Z_D_D(double deltaYaw, double deltaPitch) {
      ClassCache s = init_ClassCache;
      if (s == null) {
         return false;
      }

      if (write_Z()) {
         write_V();
         return true;
      }

      RemotePlayer bot = init_RemotePlayer_Minecraft(Minecraft.getInstance());
      if (bot == null) {
         return true;
      }

      init_F += (float)(deltaYaw * 0.15);
      load_F = Mth.clamp(load_F + (float)(deltaPitch * 0.15), -90.0F, 90.0F);
      bot.setYRot(init_F);
      bot.setXRot(load_F);
      bot.setYHeadRot(init_F);
      return true;
   }

   static void init_V_RemotePlayer(RemotePlayer bot, float yaw, float pitch) {
      if (bot != null && load((Entity)bot)) {
         init_F = yaw;
         load_F = Mth.clamp(pitch, -90.0F, 90.0F);
         bot.setYRot(init_F);
         bot.setXRot(load_F);
         bot.setYHeadRot(init_F);
      }
   }

   public static boolean init_Z_D(double scrollY) {
      ClassCache s = init_ClassCache;
      if (s == null || scrollY == 0.0) {
         return false;
      } else if (write_Z()) {
         write_V();
         return true;
      } else {
         int selected = Math.max(0, Math.min(8, s.load_I()));
         int step = scrollY > 0.0 ? -1 : 1;
         s.read_Str_I((selected + step + 9) % 9);
         return true;
      }
   }

   public static boolean load_Z(String line) {
      ClassCache s = init_ClassCache;
      if (s == null || line == null || line.isBlank()) {
         return false;
      } else if (write_Z()) {
         write_V();
         return true;
      } else {
         s.read_Str_Str(line);
         return true;
      }
   }

   static String init_Str() {
      ClassCache s = init_ClassCache;
      return s == null ? null : s.open_Str();
   }

   private static boolean update(Minecraft mc) {
      if (mc.getWindow() == null) {
         return false;
      }

      long window = mc.getWindow().handle();
      return CompatGLFW.glfwGetKey(window, 341) == 1 || CompatGLFW.glfwGetKey(window, 345) == 1;
   }

   private static RemotePlayer init_RemotePlayer_Minecraft(Minecraft mc) {
      if (mc.level != null && init_I >= 0) {
         return mc.level.getEntity(init_I) instanceof RemotePlayer bot ? bot : null;
      } else {
         return null;
      }
   }

   private static final class Inner_CachePool {
      final ClassCache init_ClassCache;
      final int init_I;
      boolean init_Z;
      boolean load;

      Inner_CachePool(ClassCache session, int wireId) {
         this.init_ClassCache = session;
         this.init_I = wireId;
      }
   }

   public record Inner_DataUtil(long init_J, String init_Str, String load, List<ItemStack> init_List) {
   }

   private record Inner_StreamUtil(int init_I, UUID init_UUID, int load, Vec3 init_Vec3, String init_Str) {
      boolean init(Entity entity) {
         return this.init_I >= 0 && entity != null && entity.getId() == this.init_I && (this.init_UUID == null || this.init_UUID.equals(entity.getUUID()));
      }

      boolean init_Z() {
         return this.init_I < 0;
      }
   }
}
