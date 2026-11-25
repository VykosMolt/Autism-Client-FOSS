package autismclient.modules;

import autismclient.util.AuditClientFixes;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PkgCls_g;
import autismclient.util.PkgCls_h;
import autismclient.util.bk;
import autismclient.util.dm;
import java.awt.Color;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public final class am {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static volatile am.Inner_JsonUtil init_am_JsonUtil = am.Inner_JsonUtil.init(-1, false);
   private static volatile am.Inner_BufferHelper init_am_BufferHelper = am.Inner_BufferHelper.init(-1, false);
   private static volatile am.Inner_NetHelper init_am_NetHelper = am.Inner_NetHelper.init(-1, false);
   private static volatile am.Inner_StreamUtil init_am_StreamUtil = am.Inner_StreamUtil.init("esp", -1, false);
   private static volatile am.Inner_ConfigStore init_am_ConfigStore = am.Inner_ConfigStore.init(-1, false);
   private static volatile am.Inner_StreamUtil load_am_StreamUtil = am.Inner_StreamUtil.init("tracers", -1, false);
   private static volatile am.Inner_DataUtil init_am_DataUtil = am.Inner_DataUtil.init(-1, false);
   private static final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
   private static final long init_J = 200L;
   private static volatile long load_J = -1L;
   private static boolean init_Z;
   private static final ClassValue<am.Inner_TaskQueue> init_ClassValue = new ClassValue<am.Inner_TaskQueue>() {
      protected am.Inner_TaskQueue computeValue(Class<?> type) {
         return this.init(type);
      }

      protected am.Inner_TaskQueue init(Class<?> type) {
         try {
            Lookup lookup = MethodHandles.publicLookup();
            Method mBaseColor = type.getMethod("baseColor", int.class);
            Method mSetColor = type.getMethod("setColor", int.class, int.class);
            Method mSetRenderType = type.getMethod("setRenderType", ChunkSectionLayer.class);
            mBaseColor.setAccessible(true);
            mSetColor.setAccessible(true);
            mSetRenderType.setAccessible(true);
            return new am.Inner_TaskQueue(
               lookup.unreflect(mBaseColor).asType(MethodType.methodType(int.class, Object.class, int.class)),
               lookup.unreflect(mSetColor).asType(MethodType.methodType(void.class, Object.class, int.class, int.class)),
               lookup.unreflect(mSetRenderType).asType(MethodType.methodType(void.class, Object.class, ChunkSectionLayer.class))
            );
         } catch (ReflectiveOperationException ignored) {
            return am.Inner_TaskQueue.init;
         }
      }
   };
   private static final ClassValue<Optional<Method>> load_ClassValue = new ClassValue<Optional<Method>>() {
      protected Optional<Method> computeValue(Class<?> type) {
         return this.init(type);
      }

      protected Optional<Method> init(Class<?> type) {
         try {
            return Optional.of(type.getMethod("getResources"));
         } catch (ReflectiveOperationException ignored) {
            return Optional.empty();
         }
      }
   };
   private static final Object init_Obj = new Object();
   private static volatile Object load_Obj;
   private static final Direction[] init_DirectionArr = Direction.values();
   private static final ThreadLocal<am.Inner_CachePool> init_ThreadLocal = ThreadLocal.withInitial(am.Inner_CachePool::new);

   private am() {
   }

   static boolean init_Z() {
      return aD.init_Z() ? true : !AutismLiteVariant.enabled() && !autismclient.e.RenderState.init();
   }

   public static boolean load_Z() {
      return init_am_JsonUtil().load_Z();
   }

   public static int init_I() {
      return init_Minecraft != null && init_Minecraft.options != null ? Math.max(1, init_Minecraft.options.getEffectiveRenderDistance()) : 8;
   }

   public static boolean read_Z() {
      return init_am_JsonUtil().load_Z();
   }

   public static boolean write_Z() {
      return init_am_NetHelper().load_Z();
   }

   public static int init_I_BlockState_BlockPos(BlockState state, BlockPos pos) {
      am.Inner_NetHelper snapshot = init_am_NetHelper();
      if (snapshot.load_Z() && state != null && !state.isAir()) {
         boolean matched = init_Z_BlockState_am_NetHelper(state, snapshot);
         boolean darken = snapshot.read() ? matched : !matched;
         return darken ? snapshot.load_I() : -1;
      } else {
         return -1;
      }
   }

   public static boolean update() {
      return init_am_JsonUtil().load_Z() || aC.init_Z();
   }

   public static boolean sync() {
      return init_am_BufferHelper().load_Z();
   }

   public static boolean flush() {
      return init_am_BufferHelper().read_Z();
   }

   public static boolean close() {
      return init_am_BufferHelper().read_Z();
   }

   public static boolean open() {
      return init_am_BufferHelper().load_Z() || init_am_JsonUtil().load_Z();
   }

   public static boolean poll() {
      return init_am_BufferHelper().load_Z() || init_am_JsonUtil().load_Z();
   }

   public static int init(LightLayer lightLayer) {
      am.Inner_BufferHelper snapshot = init_am_BufferHelper();
      if (snapshot.read_Z() && lightLayer != null) {
         return lightLayer == LightLayer.SKY ? snapshot.read_I() : (lightLayer == LightLayer.BLOCK ? snapshot.write() : 0);
      } else {
         return 0;
      }
   }

   public static boolean init_Z_BlockAndTintGetter(BlockAndTintGetter level, BlockPos pos, BlockState state) {
      return !init(init_am_JsonUtil(), level, state, pos);
   }

   public static int init_I_BlockAndTintGetter(BlockAndTintGetter level, BlockPos pos, BlockState state) {
      am.Inner_JsonUtil snapshot = init_am_JsonUtil();
      if (!snapshot.load_Z() || state == null || state.isAir()) {
         return -1;
      } else if (!init(snapshot, level, state, pos)) {
         return -1;
      } else {
         return snapshot.write() ? 0 : snapshot.load_I();
      }
   }

   public static int load(BlockState state, BlockPos pos) {
      return init_I_BlockAndTintGetter(null, pos, state);
   }

   public static boolean init(BlockAndTintGetter level, BlockState state, Direction direction, BlockPos originalPos, boolean original) {
      am.Inner_JsonUtil snapshot = init_am_JsonUtil();
      if (!snapshot.load_Z()) {
         return original;
      } else if (!original && !init(snapshot, level, state, originalPos)) {
         BlockPos adjPos = originalPos == null ? null : originalPos.relative(direction);
         BlockState adjState = level != null && adjPos != null ? level.getBlockState(adjPos) : null;
         return !init(snapshot, level, adjState, adjPos, direction);
      } else {
         return original;
      }
   }

   private static boolean init(am.Inner_JsonUtil snapshot, BlockGetter level, BlockState adjState, BlockPos adjPos, Direction direction) {
      return adjState != null
         && adjState.isSolidRender()
         && adjState.getFaceOcclusionShape(direction.getOpposite()) == Shapes.block()
         && !init(snapshot, level, adjState, adjPos);
   }

   public static boolean init_Z_BlockState(BlockState state, BlockState neighborState, Direction direction) {
      am.Inner_JsonUtil snapshot = init_am_JsonUtil();
      return snapshot.load_Z() && !init(snapshot, null, state, null) ? !init(snapshot, null, neighborState, null, direction) : false;
   }

   public static boolean init_Z_BlockState_BlockPos(BlockState state, BlockPos pos) {
      return init_Z_am_JsonUtil_2(init_am_JsonUtil(), state, pos);
   }

   public static int init_I_BlockAndTintGetter_2(BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
      return init_I_FluidState_BlockPos(fluidState, pos);
   }

   public static int init_I_FluidState_BlockPos(FluidState fluidState, BlockPos pos) {
      am.Inner_JsonUtil snapshot = init_am_JsonUtil();
      if (snapshot.load_Z() && fluidState != null && !fluidState.isEmpty()) {
         boolean water = fluidState.is(FluidTags.WATER);
         boolean lava = fluidState.is(FluidTags.LAVA);
         boolean var10000;
         switch (snapshot.init_Str()) {
            case "None":
               var10000 = false;
               break;
            case "Water":
               var10000 = water;
               break;
            case "Lava":
               var10000 = lava;
               break;
            default:
               var10000 = water || lava;
         }

         boolean apply = var10000;
         if (!apply) {
            return -1;
         } else {
            BlockState fluidBlock = fluidState.createLegacyBlock();
            if (!init_Z_am_JsonUtil_2(snapshot, fluidBlock, pos)) {
               return -1;
            } else {
               return snapshot.write() ? 0 : snapshot.load_I();
            }
         }
      } else {
         return -1;
      }
   }

   public static boolean push() {
      return load_Z();
   }

   public static boolean pop() {
      am.Inner_JsonUtil snapshot = init_am_JsonUtil();
      return snapshot.load_Z() && snapshot.write();
   }

   public static boolean init(BlockState neighborState) {
      return init_Z_am_JsonUtil_2(init_am_JsonUtil(), neighborState, null);
   }

   public static int load_I() {
      return 4095;
   }

   public static int init_I_I(int current, BlockState state, BlockPos pos) {
      am.Inner_JsonUtil snapshot = init_am_JsonUtil();
      if (snapshot.load_Z() && !init_Z_am_JsonUtil_2(snapshot, state, pos)) {
         return load_I();
      }

      am.Inner_BufferHelper fullbright = init_am_BufferHelper();
      return fullbright.read_Z() && "BLOCK".equals(fullbright.init_Str()) ? Math.max(current, fullbright.load_I()) : current;
   }

   public static boolean init_Z(Object region) {
      if (region == null) {
         return true;
      }

      Method getResources = load_ClassValue.get(region.getClass()).orElse(null);
      if (getResources == null) {
         return false;
      }

      try {
         return getResources.invoke(region) == null;
      } catch (Throwable ignored) {
         return false;
      }
   }

   public static void init_V_Obj_I(Object quad, int alpha) {
      if (quad != null && alpha >= 0) {
         init_ClassValue.get(quad.getClass()).init(quad, alpha);
      }
   }

   public static void load(Object quad, int tint) {
      if (quad != null) {
         init_ClassValue.get(quad.getClass()).load(quad, tint);
      }
   }

   public static void init_V_Obj_ChunkSectionLayer(Object quad, ChunkSectionLayer layer) {
      if (quad != null && layer != null) {
         init_ClassValue.get(quad.getClass()).init(quad, layer);
      }
   }

   public static Object init_Obj(Object fallback) {
      try {
         Class<?> defaultMaterials = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials");
         Field translucent = defaultMaterials.getField("TRANSLUCENT");
         Object value = translucent.get(null);
         return value == null ? fallback : value;
      } catch (Throwable ignored) {
         return fallback;
      }
   }

   public static Object load(Object fallback) {
      Object resolved = load_Obj;
      if (resolved == null) {
         resolved = init_Obj();
         load_Obj = resolved;
      }

      return resolved == init_Obj ? fallback : resolved;
   }

   private static Object init_Obj() {
      try {
         Class<?> sortType = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortType");
         Object[] constants = sortType.getEnumConstants();
         if (constants != null) {
            for (Object constant : constants) {
               if (constant instanceof Enum<?> value && "NONE".equals(value.name())) {
                  return constant;
               }
            }
         }
      } catch (Throwable var7) {
      }

      return init_Obj;
   }

   public static void init_V() {
      if (init_Minecraft != null && init_Minecraft.level != null) {
         init_AtomicBoolean.set(true);
      }
   }

   public static void load_V() {
      if (init_Minecraft != null && init_Minecraft.level != null) {
         load_J = System.currentTimeMillis() + 200L;
      }
   }

   public static void read_V() {
      boolean suppressed = init_Z();
      if (suppressed != init_Z) {
         init_Z = suppressed;
         write_V();
         init_V();
      }

      long settledDue = load_J;
      if (settledDue >= 0L && System.currentTimeMillis() >= settledDue) {
         init_AtomicBoolean.set(true);
      }

      if (init_AtomicBoolean.getAndSet(false)) {
         load_J = -1L;
         if (init_Minecraft != null && init_Minecraft.level != null && init_Minecraft.levelExtractor != null) {
            dm.init_V();
            AuditClientFixes.refreshChunksSoft(init_Minecraft);
         }
      }
   }

   public static int init_I_BlockAndLightGette(BlockAndLightGetter level, BlockPos pos, int packedBrightness) {
      am.Inner_BufferHelper snapshot = init_am_BufferHelper();
      if (!snapshot.read_Z()) {
         return packedBrightness;
      }

      int sky = snapshot.read_I();
      int block = snapshot.write();
      int originalSky = level != null && pos != null ? level.getBrightness(LightLayer.SKY, pos) : 0;
      int originalBlock = level != null && pos != null ? level.getBrightness(LightLayer.BLOCK, pos) : 0;
      return LightCoordsUtil.pack(Math.max(block, originalBlock), Math.max(sky, originalSky));
   }

   public static boolean init_Z(Entity entity) {
      return init_Z_am_StreamUtil_Entity(load_am_StreamUtil(), entity);
   }

   public static boolean tick() {
      return load_am_StreamUtil().load_Z();
   }

   public static boolean load_Z(Entity entity) {
      if (entity instanceof ItemEntity) {
         return false;
      } else {
         am.Inner_StreamUtil snapshot = init_am_StreamUtil();
         if (!snapshot.load_Z()) {
            return false;
         } else {
            return clear() ? false : init_Z_am_StreamUtil_Entity(snapshot, entity) && init_D_am_StreamUtil_Entity(snapshot, entity) > 0.0;
         }
      }
   }

   public static float init_F(Entity entity) {
      if (entity instanceof ItemEntity) {
         return 0.0F;
      }

      am.Inner_StreamUtil snapshot = init_am_StreamUtil();
      return snapshot.load_Z() && init_Z_am_StreamUtil_Entity(snapshot, entity) ? (float)init_D_am_StreamUtil_Entity(snapshot, entity) : 0.0F;
   }

   static boolean auditMatchesEsp(Entity entity) {
      if (entity instanceof ItemEntity) {
         return false;
      }

      return init_Z_am_StreamUtil_Entity(init_am_StreamUtil(), entity);
   }

   public static int init_I(Entity entity) {
      return init_I_am_StreamUtil(init_am_StreamUtil(), entity, -855638017) & 16777215;
   }

   public static int load_I(Entity entity) {
      return init_I_am_StreamUtil(load_am_StreamUtil(), entity, -855638017);
   }

   public static int read_I(Entity entity) {
      am.Inner_StreamUtil snapshot = init_am_StreamUtil();
      int color = init_I_am_StreamUtil(snapshot, entity, -855638017);
      return init_I_I_D(color, init_D_am_StreamUtil_Entity(snapshot, entity));
   }

   public static int write(Entity entity) {
      return read_I(entity) | 0xFF000000;
   }

   public static boolean run() {
      return init_am_DataUtil().load_Z();
   }

   public static void init_V_Entity_EntityRenderState(Entity entity, EntityRenderState state) {
      if (state instanceof PkgCls_h holder) {
         am.Inner_DataUtil snapshot = init_am_DataUtil();
         if (!snapshot.load_Z() || entity instanceof ItemEntity || !init_Z_am_DataUtil_Entity(snapshot, entity)) {
            holder.autism$setChams(false, 0, 0);
         } else if (snapshot.write_Z() && PkgCls_g.init_Z(entity)) {
            int hit = snapshot.update_I() | 0xFF000000;
            holder.autism$setChams(true, hit, hit);
         } else if (snapshot.read_Z()) {
            holder.autism$setChams(true, 16777215, 16777215);
         } else {
            int alpha = Math.round(255.0F * Math.max(0.0F, Math.min(100.0F, snapshot.write_I())) / 100.0F);
            holder.autism$setChams(true, alpha << 24 | snapshot.load_I() & 16777215, alpha << 24 | snapshot.read_I() & 16777215);
         }
      }
   }

   private static boolean init_Z_am_DataUtil_Entity(am.Inner_DataUtil snapshot, Entity entity) {
      if (snapshot == null || !snapshot.load_Z()) {
         return false;
      }

      if (init_Minecraft == null || init_Minecraft.player == null || entity == null) {
         return false;
      }

      if (entity == init_Minecraft.player) {
         return false;
      }

      if (ByteReader.load_Z(entity)) {
         return false;
      }

      if (entity == init_Minecraft.getCameraEntity() && init_Minecraft.options.getCameraType().isFirstPerson()) {
         return false;
      }

      double maxDistance = snapshot.init_D();
      if (maxDistance > 0.0 && entity.distanceToSqr(init_Minecraft.player) > maxDistance * maxDistance) {
         return false;
      }

      if (snapshot.init_Set().isEmpty() && snapshot.load_Set().isEmpty()) {
         return true;
      }

      EntityType<?> entityType = entity.getType();
      Boolean matched = snapshot.init_ConcurrentHashMap().get(entityType);
      return matched != null ? matched : snapshot.init_ConcurrentHashMap().computeIfAbsent(entityType, type -> {
         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)type);
         return snapshot.init_Set().contains(id) || snapshot.load_Set().contains(id.getPath());
      });
   }

   public static boolean read_Z(Entity entity) {
      if (entity instanceof ItemEntity itemEntity) {
         am.Inner_ConfigStore snapshot = init_am_ConfigStore();
         if (!snapshot.load_Z()) {
            return false;
         } else {
            return clear() ? false : init_Z_am_ConfigStore_ItemEntity(snapshot, itemEntity) && init_D_am_ConfigStore_ItemEntity(snapshot, itemEntity) > 0.0;
         }
      } else {
         return false;
      }
   }

   public static int update(Entity entity) {
      am.Inner_ConfigStore snapshot = init_am_ConfigStore();
      if (entity instanceof ItemEntity itemEntity) {
         int base = snapshot.write() ? init_I_am_ConfigStore(snapshot, itemEntity.getItem(), snapshot.load_I()) : snapshot.load_I();
         return init_I_I_D(base, init_D_am_ConfigStore_ItemEntity(snapshot, itemEntity));
      } else {
         return snapshot.load_I();
      }
   }

   private static int init_I_am_ConfigStore(am.Inner_ConfigStore snapshot, ItemStack stack, int fallbackArgb) {
      int alpha = fallbackArgb >>> 24 & 0xFF;
      int rgb = stack != null && !stack.isEmpty() ? snapshot.init_ConcurrentHashMap().computeIfAbsent(stack.getItem(), ignored -> load(stack)) : 16766826;
      return alpha << 24 | rgb & 16777215;
   }

   private static int load(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         String path = id == null ? "" : id.getPath();
         int named = init(path);
         if (named >= 0) {
            return named;
         }

         if (stack.getItem() instanceof BlockItem blockItem) {
            try {
               int col = blockItem.getBlock().defaultMapColor().col;
               if (col != 0) {
                  return col & 16777215;
               }
            } catch (Throwable var6) {
            }
         }

         int hash = id == null ? path.hashCode() : id.toString().hashCode();
         float hue = (hash & 2147483647) % 360 / 360.0F;
         return Color.HSBtoRGB(hue, 0.65F, 0.95F) & 16777215;
      } else {
         return 16766826;
      }
   }

   static int init(Block block) {
      Identifier id = BuiltInRegistries.BLOCK.getKey(block);
      String path = id == null ? "" : id.getPath();
      int named = init(path);
      if (named >= 0) {
         return named;
      }

      try {
         int col = block.defaultMapColor().col;
         if (col != 0) {
            return col & 16777215;
         }
      } catch (Throwable var6) {
      }

      int hash = id == null ? path.hashCode() : id.toString().hashCode();
      float hue = (hash & 2147483647) % 360 / 360.0F;
      return Color.HSBtoRGB(hue, 0.65F, 0.95F) & 16777215;
   }

   private static int init(String path) {
      if (path.contains("ancient_debris")) {
         return 12611646;
      } else if (path.contains("netherite")) {
         return 12038057;
      } else if (path.contains("diamond")) {
         return 4907990;
      } else if (path.contains("emerald")) {
         return 3137626;
      } else if (path.contains("lapis")) {
         return 2774496;
      } else if (path.contains("redstone")) {
         return 16724016;
      } else if (path.contains("amethyst")) {
         return 11832562;
      } else if (path.contains("copper")) {
         return 14715482;
      } else if (path.contains("gold") || path.contains("golden") || path.contains("raw_gold")) {
         return 16573003;
      } else if (path.contains("iron")) {
         return 14342874;
      } else if (path.contains("coal")) {
         return 2894892;
      } else if (path.contains("quartz")) {
         return 15919840;
      } else if (path.contains("melon")) {
         return 6801483;
      } else if (path.contains("pumpkin")) {
         return 14712864;
      } else if (path.contains("ender")) {
         return 1226650;
      } else if (path.contains("blaze")) {
         return 16758062;
      } else if (path.contains("slime")) {
         return 8377434;
      } else if (path.contains("bone")) {
         return 15328464;
      } else {
         return !path.contains("netherrack") && !path.contains("nether_brick") ? -1 : 8010555;
      }
   }

   static int init(ItemStack stack) {
      return load(stack);
   }

   public static int sync(Entity entity) {
      return update(entity) | 0xFF000000;
   }

   public static int flush(Entity entity) {
      if (entity instanceof ItemEntity itemEntity) {
         am.Inner_ConfigStore snapshot = init_am_ConfigStore();
         if (!snapshot.load_Z() || !"Shader".equals(snapshot.init_Str()) || clear()) {
            return 0;
         }

         if (!init_Z_am_ConfigStore_ItemEntity(snapshot, itemEntity)) {
            return 0;
         }

         double fade = init_D_am_ConfigStore_ItemEntity(snapshot, itemEntity);
         if (fade <= 0.0) {
            return 0;
         }

         int base = snapshot.write() ? init_I_am_ConfigStore(snapshot, itemEntity.getItem(), snapshot.load_I()) : snapshot.load_I();
         return init_I_I_D(base, fade) | 0xFF000000;
      } else {
         return 0;
      }
   }

   public static int close(Entity entity) {
      if (entity instanceof ItemEntity) {
         return 0;
      }

      am.Inner_StreamUtil snapshot = init_am_StreamUtil();
      if (!snapshot.load_Z() || !"Shader".equals(snapshot.load_Str()) || clear()) {
         return 0;
      }

      if (!init_Z_am_StreamUtil_Entity(snapshot, entity)) {
         return 0;
      }

      double fade = init_D_am_StreamUtil_Entity(snapshot, entity);
      return fade <= 0.0 ? 0 : init_I_I_D(init_I_am_StreamUtil(snapshot, entity, -855638017), fade) | 0xFF000000;
   }

   public static boolean get() {
      am.Inner_ConfigStore snapshot = init_am_ConfigStore();
      return snapshot.load_Z() && "Shader".equals(snapshot.init_Str()) ? !clear() : false;
   }

   public static boolean set() {
      am.Inner_StreamUtil snapshot = init_am_StreamUtil();
      return snapshot.load_Z() && "Shader".equals(snapshot.load_Str()) ? !clear() : false;
   }

   public static boolean put() {
      if (ah.init_Z()) {
         return true;
      }

      am.Inner_ConfigStore itemSnapshot = init_am_ConfigStore();
      if (itemSnapshot.load_Z() && "Shader".equals(itemSnapshot.init_Str())) {
         return true;
      }

      am.Inner_StreamUtil entitySnapshot = init_am_StreamUtil();
      return entitySnapshot.load_Z() && "Shader".equals(entitySnapshot.load_Str());
   }

   public static boolean add() {
      am.Inner_StreamUtil snapshot = init_am_StreamUtil();
      return snapshot.load_Z() && "2D".equals(snapshot.load_Str());
   }

   public static void write_V() {
      int revision = al.init_I();
      boolean hidden = init_Z();
      synchronized (am.class) {
         init_am_JsonUtil = init_am_JsonUtil_I_Z(revision, hidden);
         init_am_NetHelper = init_am_NetHelper_I_Z(revision, hidden);
         init_am_BufferHelper = init_am_BufferHelper_I_Z(revision, hidden);
         init_am_StreamUtil = init("esp", revision, hidden, false);
         init_am_ConfigStore = init_am_ConfigStore_I_Z(revision, hidden);
         load_am_StreamUtil = init("tracers", revision, hidden, true);
         init_am_DataUtil = init_am_DataUtil_I_Z(revision, hidden);
         dm.init(init_am_JsonUtil.load_Z());
      }
   }

   public static boolean clear() {
      if (aD.init_Z()) {
         return true;
      }

      if (init_Minecraft == null) {
         return false;
      }

      if (init_Minecraft.gui.screen() != null
         && !(init_Minecraft.gui.screen() instanceof ChatScreen)
         && !(init_Minecraft.gui.screen() instanceof InBedChatScreen)) {
         return true;
      }

      bk overlays = bk.init_bk();
      return overlays.init_Z() && overlays.load_Z();
   }

   private static boolean init_Z_am_StreamUtil_Entity(am.Inner_StreamUtil snapshot, Entity entity) {
      if (snapshot == null || !snapshot.load_Z()) {
         return false;
      }

      if (init_Minecraft == null || init_Minecraft.player == null || entity == null) {
         return false;
      }

      if (entity == init_Minecraft.player) {
         return false;
      }

      if (ByteReader.load_Z(entity)) {
         return false;
      }

      if (aL.init_Z_Entity_5(entity) && !snapshot.write_Z()) {
         return false;
      }

      if (entity == init_Minecraft.getCameraEntity() && init_Minecraft.options.getCameraType().isFirstPerson()) {
         return false;
      }

      double maxDistance = snapshot.init_D();
      if (maxDistance > 0.0 && entity.distanceToSqr(init_Minecraft.player) > maxDistance * maxDistance) {
         return false;
      }

      if (snapshot.init_Set().isEmpty() && snapshot.load_Set().isEmpty()) {
         return true;
      }

      EntityType<?> entityType = entity.getType();
      Boolean matched = snapshot.init_ConcurrentHashMap().get(entityType);
      return matched != null ? matched : snapshot.init_ConcurrentHashMap().computeIfAbsent(entityType, type -> {
         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)type);
         return snapshot.init_Set().contains(id) || snapshot.load_Set().contains(id.getPath());
      });
   }

   private static boolean init_Z_am_ConfigStore_ItemEntity(am.Inner_ConfigStore snapshot, ItemEntity entity) {
      if (snapshot == null || !snapshot.load_Z()) {
         return false;
      }

      if (init_Minecraft == null || init_Minecraft.player == null || entity == null) {
         return false;
      }

      if (entity.getItem() != null && !entity.getItem().isEmpty()) {
         Vec3 camera = init_Minecraft.gameRenderer.mainCamera().position();
         if (!entity.shouldRender(camera.x, camera.y, camera.z)) {
            return false;
         }

         double maxDistance = snapshot.init_D();
         if (maxDistance > 0.0 && entity.distanceToSqr(init_Minecraft.player) > maxDistance * maxDistance) {
            return false;
         }

         if (!snapshot.read()) {
            return true;
         }

         Item itemKey = entity.getItem().getItem();
         Boolean matched = snapshot.load_ConcurrentHashMap().get(itemKey);
         return matched != null ? matched : snapshot.load_ConcurrentHashMap().computeIfAbsent(itemKey, item -> {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            return snapshot.init_Set().contains(id) || snapshot.load_Set().contains(id.getPath());
         });
      } else {
         return false;
      }
   }

   private static am.Inner_StreamUtil init_am_StreamUtil() {
      am.Inner_StreamUtil snapshot = init_am_StreamUtil;
      int revision = al.init_I();
      boolean hidden = init_Z();
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = init_am_StreamUtil;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init("esp", revision, hidden, false);
         init_am_StreamUtil = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_StreamUtil load_am_StreamUtil() {
      am.Inner_StreamUtil snapshot = load_am_StreamUtil;
      int revision = al.init_I();
      boolean hidden = init_Z();
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = load_am_StreamUtil;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init("tracers", revision, hidden, true);
         load_am_StreamUtil = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_DataUtil init_am_DataUtil() {
      am.Inner_DataUtil snapshot = init_am_DataUtil;
      int revision = al.init_I();
      boolean hidden = init_Z();
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = init_am_DataUtil;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init_am_DataUtil_I_Z(revision, hidden);
         init_am_DataUtil = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_DataUtil init_am_DataUtil_I_Z(int revision, boolean hidden) {
      if (hidden) {
         return am.Inner_DataUtil.init(revision, true);
      }

      Module module = al.init_Module_Str("chams");
      if (module != null && module.isEnabled()) {
         Set<Identifier> ids = new HashSet<>();
         Set<String> paths = new HashSet<>();

         for (String entry : module.list("entities")) {
            if (entry != null) {
               String normalized = entry.trim().toLowerCase(Locale.ROOT);
               if (!normalized.isEmpty()) {
                  Identifier identifier = normalized.contains(":") ? Identifier.tryParse(normalized) : null;
                  if (identifier != null) {
                     ids.add(identifier);
                  } else {
                     paths.add(normalized.contains(":") ? normalized.substring(normalized.indexOf(58) + 1) : normalized);
                  }
               }
            }
         }

         return new am.Inner_DataUtil(
            revision,
            false,
            true,
            "Texture".equals(module.value("style")),
            init_D_Str_D(module.value("max-distance"), 128.0),
            Set.copyOf(ids),
            Set.copyOf(paths),
            init_I_Module(module, "visible-color", -13238437),
            init_I_Module(module, "occluded-color", -5230337),
            (int)init_D_Str_D(module.value("opacity"), 100.0),
            Boolean.parseBoolean(module.value("hit-color")),
            init_I_Module(module, "hit-color-value", -50373),
            Boolean.parseBoolean(module.value("draw-armor")),
            new ConcurrentHashMap<>()
         );
      } else {
         return am.Inner_DataUtil.init(revision, false);
      }
   }

   public static boolean reset() {
      return init_am_DataUtil().update_Z();
   }

   private static am.Inner_ConfigStore init_am_ConfigStore() {
      am.Inner_ConfigStore snapshot = init_am_ConfigStore;
      int revision = al.init_I();
      boolean hidden = init_Z();
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = init_am_ConfigStore;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init_am_ConfigStore_I_Z(revision, hidden);
         init_am_ConfigStore = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_ConfigStore init_am_ConfigStore_I_Z(int revision, boolean hidden) {
      if (hidden) {
         return am.Inner_ConfigStore.init(revision, true);
      }

      Module module = al.init_Module_Str("item-esp");
      if (module != null && module.isEnabled()) {
         Set<Identifier> ids = new HashSet<>();
         Set<String> paths = new HashSet<>();

         for (String entry : module.list("items")) {
            if (entry != null) {
               String normalized = entry.trim().toLowerCase(Locale.ROOT);
               if (!normalized.isEmpty()) {
                  Identifier identifier = normalized.contains(":") ? Identifier.tryParse(normalized) : null;
                  if (identifier != null) {
                     ids.add(identifier);
                  } else {
                     paths.add(normalized.contains(":") ? normalized.substring(normalized.indexOf(58) + 1) : normalized);
                  }
               }
            }
         }

         return new am.Inner_ConfigStore(
            revision,
            false,
            true,
            module.value("mode"),
            "Some".equals(module.value("items-mode")),
            init_D_Str_D(module.value("max-distance"), 64.0),
            init_D_Str_D(module.value("fade-distance"), 3.0),
            Set.copyOf(ids),
            Set.copyOf(paths),
            init_I_Module(module, "color", -855648406),
            !"Static".equals(module.value("color-mode")),
            new ConcurrentHashMap<>(),
            new ConcurrentHashMap<>()
         );
      } else {
         return am.Inner_ConfigStore.init(revision, false);
      }
   }

   private static am.Inner_StreamUtil init(String moduleId, int revision, boolean hidden, boolean useMaxDistance) {
      if (hidden) {
         return am.Inner_StreamUtil.init(moduleId, revision, true);
      }

      Module module = al.init_Module_Str(moduleId);
      if (module != null && module.isEnabled()) {
         Set<Identifier> ids = new HashSet<>();
         Set<String> paths = new HashSet<>();

         for (String entry : module.list("entities")) {
            if (entry != null) {
               String normalized = entry.trim().toLowerCase(Locale.ROOT);
               if (!normalized.isEmpty()) {
                  Identifier identifier = normalized.contains(":") ? Identifier.tryParse(normalized) : null;
                  if (identifier != null) {
                     ids.add(identifier);
                  } else {
                     paths.add(normalized.contains(":") ? normalized.substring(normalized.indexOf(58) + 1) : normalized);
                  }
               }
            }
         }

         return new am.Inner_StreamUtil(
            moduleId,
            revision,
            false,
            true,
            module.value("mode"),
            useMaxDistance ? init_D_Str_D(module.value("max-distance"), 256.0) : 0.0,
            init_D_Str_D(module.value("fade-distance"), 3.0),
            Set.copyOf(ids),
            Set.copyOf(paths),
            init_I_Module(module, "players-color", -855638017),
            init_I_Module(module, "monsters-color", -855684534),
            init_I_Module(module, "animals-color", -864747633),
            init_I_Module(module, "water-animals-color", -865674753),
            init_I_Module(module, "ambient-color", -860386049),
            init_I_Module(module, "misc-color", -858993460),
            Boolean.parseBoolean(module.value("distance-color")),
            init_D_Str_D(module.value("color-distance"), 40.0),
            aL.read_Z_Str(moduleId),
            aL.init_I_18(),
            new ConcurrentHashMap<>()
         );
      } else {
         return am.Inner_StreamUtil.init(moduleId, revision, false);
      }
   }

   private static boolean init_Z_am_JsonUtil_2(am.Inner_JsonUtil snapshot, BlockState state, BlockPos pos) {
      return init(snapshot, null, state, pos);
   }

   private static boolean init(am.Inner_JsonUtil snapshot, BlockGetter level, BlockState state, BlockPos pos) {
      return snapshot.load_Z() && state != null && !state.isAir()
         ? !init_Z_BlockState_am_JsonUtil(state, snapshot) || snapshot.read() && pos != null && !init_Z_am_JsonUtil(snapshot, level, pos)
         : false;
   }

   private static boolean init_Z_BlockState_am_JsonUtil(BlockState state, am.Inner_JsonUtil snapshot) {
      if (state == null || state.isAir()) {
         return false;
      }

      if (snapshot.init_Set().isEmpty() && snapshot.load_Set().isEmpty()) {
         return false;
      }

      Block block = state.getBlock();
      Boolean cached = snapshot.init_ConcurrentHashMap().get(block);
      if (cached != null) {
         return cached;
      }

      Identifier id = BuiltInRegistries.BLOCK.getKey(block);
      boolean match = snapshot.init_Set().contains(id) || snapshot.load_Set().contains(id.getPath());
      snapshot.init_ConcurrentHashMap().put(block, match);
      return match;
   }

   private static boolean init_Z_BlockState_am_NetHelper(BlockState state, am.Inner_NetHelper snapshot) {
      if (state != null && !state.isAir()) {
         Block block = state.getBlock();
         Boolean cached = snapshot.init_ConcurrentHashMap().get(block);
         if (cached != null) {
            return cached;
         }

         Identifier id = BuiltInRegistries.BLOCK.getKey(block);
         boolean match = snapshot.init_Set().contains(id) || snapshot.load_Set().contains(id.getPath());
         snapshot.init_ConcurrentHashMap().put(block, match);
         return match;
      } else {
         return false;
      }
   }

   private static boolean init_Z_BlockState_List(BlockState state, List<String> entries) {
      if (state == null || state.isAir()) {
         return false;
      }

      if (entries != null && !entries.isEmpty()) {
         String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

         for (String entry : entries) {
            String normalized = entry.toLowerCase(Locale.ROOT);
            if (id.equals(normalized) || id.endsWith(":" + normalized)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init_Z_am_JsonUtil(am.Inner_JsonUtil snapshot, BlockGetter level, BlockPos pos) {
      if (pos == null) {
         return true;
      }

      BlockGetter source = level != null ? level : (init_Minecraft == null ? null : init_Minecraft.level);
      if (source == null) {
         return true;
      }

      am.Inner_CachePool memo = init_ThreadLocal.get();
      if (memo.load_I != snapshot.init_I()) {
         Arrays.fill(memo.load_ZArr, false);
         memo.load_I = snapshot.init_I();
      }

      long key = pos.asLong();
      int slot = (int)(key * -7046029254386353131L >>> 60) & 15;
      if (memo.load_ZArr[slot] && memo.init_JArr[slot] == key) {
         return memo.init_ZArr[slot];
      }

      boolean exposed = init_Z_BlockGetter(source, pos, memo.init_BlockPos_MutableBl);
      memo.init_JArr[slot] = key;
      memo.init_ZArr[slot] = exposed;
      memo.load_ZArr[slot] = true;
      return exposed;
   }

   private static boolean init_Z_BlockGetter(BlockGetter level, BlockPos pos, MutableBlockPos cursor) {
      for (Direction direction : init_DirectionArr) {
         cursor.setWithOffset(pos, direction);
         BlockState neighbor = level.getBlockState(cursor);
         if (neighbor == null || neighbor.isAir() || !neighbor.isSolidRender()) {
            return true;
         }

         FluidState fluid = neighbor.getFluidState();
         if (fluid != null && !fluid.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private static int init_I_am_StreamUtil(am.Inner_StreamUtil snapshot, Entity entity, int fallback) {
      if (snapshot == null || entity == null) {
         return fallback;
      }

      if (snapshot.write_Z() && aL.init_Z_Entity_5(entity)) {
         return snapshot.close();
      }

      if (snapshot.read_Z()) {
         return init_I_Entity_D(entity, snapshot.read_D());
      }

      if (entity.getType() == EntityTypes.PLAYER) {
         return snapshot.load_I();
      }

      MobCategory category = entity.getType().getCategory();
      int var10000;
      switch (category) {
         case MONSTER:
            var10000 = snapshot.read_I();
            break;
         case CREATURE:
            var10000 = snapshot.write_I();
            break;
         case WATER_CREATURE:
         case WATER_AMBIENT:
         case UNDERGROUND_WATER_CREATURE:
         case AXOLOTLS:
            var10000 = snapshot.update();
            break;
         case AMBIENT:
            var10000 = snapshot.sync();
            break;
         default:
            var10000 = snapshot.flush();
      }

      return var10000;
   }

   private static int init_I_Entity_D(Entity entity, double span) {
      double distance = 0.0;
      if (init_Minecraft != null && init_Minecraft.gameRenderer != null && entity != null) {
         Vec3 camera = init_Minecraft.gameRenderer.mainCamera().position();
         double dx = entity.getX() - camera.x;
         double dy = entity.getY() + entity.getBbHeight() * 0.5 - camera.y;
         double dz = entity.getZ() - camera.z;
         distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      }

      return init_I_D_D(distance, span);
   }

   public static int init_I_D_D(double distance, double span) {
      if (span <= 0.0) {
         span = 40.0;
      }

      float t = (float)Math.max(0.0, Math.min(1.0, distance / span));
      float hue = t * 0.33333334F;
      return 0xFF000000 | init_I_F(hue, 1.0F, 1.0F) & 16777215;
   }

   private static int init_I_F(float h, float s, float b) {
      h = (h % 1.0F + 1.0F) % 1.0F;
      int i = (int)(h * 6.0F);
      float f = h * 6.0F - i;
      float p = b * (1.0F - s);
      float q = b * (1.0F - s * f);
      float t = b * (1.0F - s * (1.0F - f));
      float r;
      float g;
      float bl;
      switch (i % 6) {
         case 0:
            r = b;
            g = t;
            bl = p;
            break;
         case 1:
            r = q;
            g = b;
            bl = p;
            break;
         case 2:
            r = p;
            g = b;
            bl = t;
            break;
         case 3:
            r = p;
            g = q;
            bl = b;
            break;
         case 4:
            r = t;
            g = p;
            bl = b;
            break;
         default:
            r = b;
            g = p;
            bl = q;
      }

      int ri = Math.round(r * 255.0F);
      int gi = Math.round(g * 255.0F);
      int bi = Math.round(bl * 255.0F);
      return ri << 16 | gi << 8 | bi;
   }

   private static double init_D_am_StreamUtil_Entity(am.Inner_StreamUtil snapshot, Entity entity) {
      if (init_Minecraft != null && init_Minecraft.gameRenderer != null && entity != null) {
         double fadeDistance = snapshot == null ? 3.0 : snapshot.load_D();
         if (fadeDistance <= 0.0) {
            return 1.0;
         }

         Vec3 camera = init_Minecraft.gameRenderer.mainCamera().position();
         double dx = entity.getX() - camera.x;
         double dy = entity.getY() + entity.getBbHeight() * 0.5 - camera.y;
         double dz = entity.getZ() - camera.z;
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         double alpha = Math.min(1.0, distance / fadeDistance);
         return alpha <= 0.075 ? 0.0 : alpha;
      } else {
         return 1.0;
      }
   }

   private static double init_D_am_ConfigStore_ItemEntity(am.Inner_ConfigStore snapshot, ItemEntity entity) {
      if (init_Minecraft != null && init_Minecraft.gameRenderer != null && entity != null) {
         double fadeDistance = snapshot == null ? 3.0 : snapshot.load_D();
         if (fadeDistance <= 0.0) {
            return 1.0;
         }

         Vec3 camera = init_Minecraft.gameRenderer.mainCamera().position();
         double dx = entity.getX() - camera.x;
         double dy = entity.getY() + entity.getBbHeight() * 0.5 - camera.y;
         double dz = entity.getZ() - camera.z;
         double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
         double alpha = Math.min(1.0, distance / fadeDistance);
         return alpha <= 0.075 ? 0.0 : alpha;
      } else {
         return 1.0;
      }
   }

   private static int init_I_I_D(int color, double multiplier) {
      int alpha = Math.max(0, Math.min(255, (int)((color >>> 24 & 0xFF) * multiplier)));
      return alpha << 24 | color & 16777215;
   }

   public static int init_I_Module(Module module, String option, int fallback) {
      try {
         String value = module.value(option).replace("#", "");
         if (value.length() == 6) {
            value = "CC" + value;
         }

         return (int)Long.parseLong(value, 16);
      } catch (Throwable ignored) {
         return fallback;
      }
   }

   private static double init_D_Str_D(String value, double fallback) {
      try {
         return Double.parseDouble(value);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static int init_I_Str_I(String value, int fallback) {
      try {
         return Integer.parseInt(value);
      } catch (Exception ignored) {
         return fallback;
      }
   }

   private static boolean start() {
      try {
         Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         Object instance = irisApiClass.getMethod("getInstance").invoke(null);
         return irisApiClass.getMethod("isShaderPackInUse").invoke(instance) instanceof Boolean bool && bool;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private static am.Inner_JsonUtil init_am_JsonUtil() {
      int revision = al.init_I();
      boolean hidden = init_Z();
      am.Inner_JsonUtil snapshot = init_am_JsonUtil;
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = init_am_JsonUtil;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init_am_JsonUtil_I_Z(revision, hidden);
         init_am_JsonUtil = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_NetHelper init_am_NetHelper() {
      int revision = al.init_I();
      boolean hidden = init_Z();
      am.Inner_NetHelper snapshot = init_am_NetHelper;
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = init_am_NetHelper;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init_am_NetHelper_I_Z(revision, hidden);
         init_am_NetHelper = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_BufferHelper init_am_BufferHelper() {
      int revision = al.init_I();
      boolean hidden = init_Z();
      am.Inner_BufferHelper snapshot = init_am_BufferHelper;
      if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
         return snapshot;
      }

      synchronized (am.class) {
         snapshot = init_am_BufferHelper;
         if (snapshot.init_I() == revision && snapshot.init_Z() == hidden) {
            return snapshot;
         }

         snapshot = init_am_BufferHelper_I_Z(revision, hidden);
         init_am_BufferHelper = snapshot;
         return snapshot;
      }
   }

   private static am.Inner_BufferHelper init_am_BufferHelper_I_Z(int revision, boolean hidden) {
      if (hidden) {
         return am.Inner_BufferHelper.init(revision, true);
      } else {
         Module module = al.init_Module_Str("fullbright");
         if (module != null && module.isEnabled()) {
            String mode = module.value("mode");
            boolean gamma = "Gamma".equals(mode);
            boolean luminance = "Luminance".equals(mode);
            String lightType = module.value("light-type");
            int minLight = Math.max(0, Math.min(15, init_I_Str_I(module.value("minimum-light-level"), 8)));
            return new am.Inner_BufferHelper(
               revision, false, gamma, luminance, lightType, minLight, "SKY".equals(lightType) ? minLight : 0, "BLOCK".equals(lightType) ? minLight : 0
            );
         } else {
            return am.Inner_BufferHelper.init(revision, false);
         }
      }
   }

   private static am.Inner_NetHelper init_am_NetHelper_I_Z(int revision, boolean hidden) {
      if (hidden) {
         return am.Inner_NetHelper.init(revision, true);
      }

      Module module = al.init_Module_Str("world");
      if (module != null && module.isEnabled() && Boolean.parseBoolean(module.value("darken-blocks"))) {
         int darkness = Math.max(0, Math.min(100, init_I_Str_I(module.value("darkness"), 50)));
         if (darkness <= 0) {
            return am.Inner_NetHelper.init(revision, false);
         }

         double t = darkness / 100.0;
         int picked = init_I_Module(module, "tint-color", -16777216);
         int cr = Math.max(0, Math.min(255, (int)Math.round(255.0 + ((picked >> 16 & 0xFF) - 255) * t)));
         int cg = Math.max(0, Math.min(255, (int)Math.round(255.0 + ((picked >> 8 & 0xFF) - 255) * t)));
         int cb = Math.max(0, Math.min(255, (int)Math.round(255.0 + ((picked & 0xFF) - 255) * t)));
         int tint = 0xFF000000 | cr << 16 | cg << 8 | cb;
         boolean whitelist = "Whitelist".equalsIgnoreCase(module.value("mode"));
         Set<Identifier> ids = new HashSet<>();
         Set<String> paths = new HashSet<>();

         for (String entry : module.list("blocks")) {
            if (entry != null) {
               String normalized = entry.trim().toLowerCase(Locale.ROOT);
               if (!normalized.isEmpty()) {
                  Identifier identifier = normalized.contains(":") ? Identifier.tryParse(normalized) : null;
                  if (identifier != null) {
                     ids.add(identifier);
                  } else {
                     paths.add(normalized.contains(":") ? normalized.substring(normalized.indexOf(58) + 1) : normalized);
                  }
               }
            }
         }

         return new am.Inner_NetHelper(revision, false, true, tint, whitelist, Set.copyOf(ids), Set.copyOf(paths), new ConcurrentHashMap<>());
      } else {
         return am.Inner_NetHelper.init(revision, false);
      }
   }

   private static am.Inner_JsonUtil init_am_JsonUtil_I_Z(int revision, boolean hidden) {
      if (hidden) {
         return am.Inner_JsonUtil.init(revision, true);
      }

      Module module = al.init_Module_Str("xray");
      if (module == null || !module.isEnabled()) {
         return am.Inner_JsonUtil.init(revision, false);
      }

      if (!ak.update(module)) {
         return am.Inner_JsonUtil.init(revision, false);
      }

      Set<Identifier> ids = new HashSet<>();
      Set<String> paths = new HashSet<>();
      boolean oreSim = ak.init_Z(module);
      if (!oreSim) {
         for (String entry : module.list("whitelist")) {
            if (entry != null) {
               String normalized = entry.trim().toLowerCase(Locale.ROOT);
               if (!normalized.isEmpty()) {
                  Identifier identifier = normalized.contains(":") ? Identifier.tryParse(normalized) : null;
                  if (identifier != null) {
                     ids.add(identifier);
                  } else {
                     paths.add(normalized.contains(":") ? normalized.substring(normalized.indexOf(58) + 1) : normalized);
                  }
               }
            }
         }
      }

      return new am.Inner_JsonUtil(
         revision,
         false,
         true,
         Math.max(0, Math.min(255, init_I_Str_I(module.value("opacity"), 25))),
         !oreSim && Boolean.parseBoolean(module.value("exposed-only")),
         start(),
         module.value("fluid-opacity"),
         Set.copyOf(ids),
         Set.copyOf(paths),
         new ConcurrentHashMap<>()
      );
   }

   private record Inner_BufferHelper(int init_I, boolean init_Z, boolean load_Z, boolean read_Z, String init_Str, int load_I, int read_I, int write) {
      static am.Inner_BufferHelper init(int revision, boolean hidden) {
         return new am.Inner_BufferHelper(revision, hidden, false, false, "", 0, 0, 0);
      }
   }

   private static final class Inner_CachePool {
      static final int init_I = 15;
      final long[] init_JArr = new long[16];
      final boolean[] init_ZArr = new boolean[16];
      final boolean[] load_ZArr = new boolean[16];
      final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
      int load_I = -2147483648;
   }

   private record Inner_ConfigStore(
      int init_I,
      boolean init_Z,
      boolean load_Z,
      String init_Str,
      boolean read,
      double init_D,
      double load_D,
      Set<Identifier> init_Set,
      Set<String> load_Set,
      int load_I,
      boolean write,
      ConcurrentHashMap<Item, Integer> init_ConcurrentHashMap,
      ConcurrentHashMap<Item, Boolean> load_ConcurrentHashMap
   ) {
      static am.Inner_ConfigStore init(int revision, boolean hidden) {
         return new am.Inner_ConfigStore(
            revision, hidden, false, "", false, 0.0, 0.0, Set.of(), Set.of(), -855648406, true, new ConcurrentHashMap<>(), new ConcurrentHashMap<>()
         );
      }
   }

   private record Inner_DataUtil(
      int init_I,
      boolean init_Z,
      boolean load_Z,
      boolean read_Z,
      double init_D,
      Set<Identifier> init_Set,
      Set<String> load_Set,
      int load_I,
      int read_I,
      int write_I,
      boolean write_Z,
      int update_I,
      boolean update_Z,
      ConcurrentHashMap<EntityType<?>, Boolean> init_ConcurrentHashMap
   ) {
      static am.Inner_DataUtil init(int revision, boolean hidden) {
         return new am.Inner_DataUtil(
            revision, hidden, false, false, 0.0, Set.of(), Set.of(), -13238437, -5230337, 100, true, -50373, true, new ConcurrentHashMap<>()
         );
      }
   }

   private record Inner_JsonUtil(
      int init_I,
      boolean init_Z,
      boolean load_Z,
      int load_I,
      boolean read,
      boolean write,
      String init_Str,
      Set<Identifier> init_Set,
      Set<String> load_Set,
      ConcurrentHashMap<Block, Boolean> init_ConcurrentHashMap
   ) {
      static am.Inner_JsonUtil init(int revision, boolean hidden) {
         return new am.Inner_JsonUtil(revision, hidden, false, -1, false, false, "Both", Set.of(), Set.of(), new ConcurrentHashMap<>());
      }
   }

   private record Inner_NetHelper(
      int init_I,
      boolean init_Z,
      boolean load_Z,
      int load_I,
      boolean read,
      Set<Identifier> init_Set,
      Set<String> load_Set,
      ConcurrentHashMap<Block, Boolean> init_ConcurrentHashMap
   ) {
      static am.Inner_NetHelper init(int revision, boolean hidden) {
         return new am.Inner_NetHelper(revision, hidden, false, -1, false, Set.of(), Set.of(), new ConcurrentHashMap<>());
      }
   }

   private record Inner_StreamUtil(
      String init_Str,
      int init_I,
      boolean init_Z,
      boolean load_Z,
      String load_Str,
      double init_D,
      double load_D,
      Set<Identifier> init_Set,
      Set<String> load_Set,
      int load_I,
      int read_I,
      int write_I,
      int update,
      int sync,
      int flush,
      boolean read_Z,
      double read_D,
      boolean write_Z,
      int close,
      ConcurrentHashMap<EntityType<?>, Boolean> init_ConcurrentHashMap
   ) {
      static am.Inner_StreamUtil init(String moduleId, int revision, boolean hidden) {
         return new am.Inner_StreamUtil(
            moduleId,
            revision,
            hidden,
            false,
            "",
            0.0,
            0.0,
            Set.of(),
            Set.of(),
            -855638017,
            -855684534,
            -864747633,
            -865674753,
            -860386049,
            -858993460,
            false,
            40.0,
            true,
            -866779137,
            new ConcurrentHashMap<>()
         );
      }
   }

   private record Inner_TaskQueue(MethodHandle init_Method, MethodHandle load, MethodHandle read) {
      private static final am.Inner_TaskQueue init = new am.Inner_TaskQueue(null, null, null);

      private void init(Object quad, int alpha) {
         if (this.init_Method != null && this.load != null) {
            try {
               int aMask = (alpha & 0xFF) << 24;

               for (int i = 0; i < 4; i++) {
                  int color = (int)this.init_Method.invokeExact((Object)quad, (int)i);
                  this.load.invokeExact((Object)quad, (int)i, (int)(aMask | color & 16777215));
               }
            } catch (Throwable var6) {
            }
         }
      }

      private void load(Object quad, int tint) {
         if (this.init_Method != null && this.load != null) {
            try {
               for (int i = 0; i < 4; i++) {
                  int color = (int)this.init_Method.invokeExact((Object)quad, (int)i);
                  this.load.invokeExact((Object)quad, (int)i, (int)ARGB.multiply(color, tint));
               }
            } catch (Throwable var5) {
            }
         }
      }

      private void init(Object quad, ChunkSectionLayer layer) {
         if (this.read != null) {
            try {
               this.read.invokeExact((Object)quad, (ChunkSectionLayer)layer);
            } catch (Throwable var4) {
            }
         }
      }
   }
}
