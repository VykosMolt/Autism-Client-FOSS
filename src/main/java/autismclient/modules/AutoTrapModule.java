package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.util.bM;
import autismclient.util.ce;
import autismclient.util.ch;
import autismclient.util.ck;
import autismclient.util.cn;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AutoTrapModule extends Module implements cn.Inner_DataUtil {
   private static final double init_D = 10.0;
   private static final int init_I = 25;
   private static final int load_I = 10;
   private static final int read_I = 5;
   private static final double load_D = 1.5;
   private static final int write_I = 4;
   private static final double read_D = 0.001;
   private static final int update_I = 5;
   private static final double write_D = 30.0;
   private static final int sync_I = 3;
   private static final double update_D = 0.98;
   private static final int flush = 40;
   private static final int close = 10;
   private static final int open = 10;
   private static final int poll = 10;
   private static final int push = 30;
   private static final int pop = 200;
   private static final long init_J = 100L;
   private static final int tick = 25;
   private static final long load_J = 3L;
   private static final int run = 2;
   private static final float init_F = 0.05F;
   private static final double sync_D = 1.0;
   private static final Set<Item> init_Set = Set.of(Items.COBWEB);
   private static final Set<Block> load_Set = Set.of(Blocks.COBWEB);
   private static final Set<Item> read_Set = Set.of(Items.LAVA_BUCKET, Items.FLINT_AND_STEEL);
   private static final Set<Block> write_Set = Set.of(Blocks.LAVA, Blocks.FIRE);
   private final Map<Integer, ArrayDeque<AutoTrapModule.Inner_StreamUtil>> init_Map = new LinkedHashMap<>();
   private final Map<Integer, Integer> load_Map = new LinkedHashMap<>();
   private final Random init_Random = new Random();
   private volatile AutoTrapModule.Inner_BufferHelper init_PathHelper_BufferH;
   private int get;
   private int set = -1;
   private AutoTrapModule.Inner_CachePool init_PathHelper_CachePo;
   private int put = -1;
   private int add;
   private int clear;
   private int reset;
   private int start = -2147483648;
   private long read_J = -9223372036854775808L;
   private int stop;
   private int next = -1;
   private int prev = -1;
   private int valid;
   private int size = -2147483648;
   private String init_Str;
   private Set<String> update_Set = Set.of();

   AutoTrapModule() {
      super("auto-trap", "AutoTrap", ae.init_ae, "Traps enemies in blocks.");
      this.add(new IntSetting("delay", "Delay", 20, 0, 400, 5).unit("ticks").description("Cooldown after a placed trap").build());
      this.add(new BoolSetting("switch-back", "Switch Back", true).description("Return to previous hotbar slot").build());
      this.add(new BoolSetting("web", "AutoWeb", true).group("AutoWeb").description("Place cobwebs; tried before Ignite").build());
      this.add(new BoolSetting("ignite", "Ignite", true).group("Ignite").description("Place lava or set fire").build());
      this.add(new BoolSetting("predict", "Predict Landing", true).group("Target").description("Trap the predicted landing spot").build());
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").group("Target").description("Entity types to trap").build());
      this.add(new IntSetting("fov", "FOV", 180, 0, 180, 5).group("Target").unit("degrees").description("Cone width around crosshair").build());
      this.add(new IntSetting("hurt-time", "Hurt Time", 10, 0, 10, 1).group("Target").unit("ticks").description("Maximum target hurt time").build());
      this.add(new ChoiceSetting("priority", "Priority", "Type", "Type", "Health", "Distance").group("Target").description("Main target sort key").build());
   }

   @Override
   public void onEnable() {
      this.init_V_13();
   }

   @Override
   public void onDisable() {
      this.init_V_13();
      autismclient.util.aD.init(this.id());
      autismclient.util.ah.update_V(this.id());
   }

   @Override
   public void onGameLeft() {
      this.init_V_13();
      if (this.read_Z_6()) {
         autismclient.util.aD.load_V();
      }

      autismclient.util.ah.update_V(this.id());
   }

   private void init_V_13() {
      this.init_PathHelper_BufferH = null;
      this.get = 0;
      this.set = -1;
      this.init_PathHelper_CachePo = null;
      this.put = -1;
      this.add = 0;
      this.clear = 0;
      this.reset = 0;
      this.next = -1;
      this.prev = -1;
      this.valid = 0;
      this.size = -2147483648;
      this.init_Map.clear();
      this.load_Map.clear();
   }

   @Override
   public String info() {
      AutoTrapModule.Inner_BufferHelper plan = this.init_PathHelper_BufferH;
      return plan == null ? "" : (plan.load_Z() ? "Pickup" : (plan.init_Z() ? "Web" : "Ignite"));
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && this.read_Z_6()) {
         autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.aD.read_Z() && this.read_Z_6();
   }

   @Override
   public boolean load_Z_23() {
      return cn.init_Z() ? false : autismclient.util.aD.init_Z() || this.isEnabled() && this.init_Z_14();
   }

   @Override
   public void preMovementTick() {
      if (MC != null && MC.player != null && MC.level != null && MC.gameMode != null) {
         this.update_V_5();
         this.read_V_6();
         if (!this.init_Z_14()) {
            this.load_V_10();
         } else {
            if (this.put >= 0 && ++this.add > 10) {
               this.put = -1;
               this.reset = this.integer("delay");
            }

            if (this.reset > 0) {
               this.reset--;
               this.init_PathHelper_BufferH = null;
               this.get = 0;
               this.init_V_ce_DataUtil(null);
               this.write_V_6();
            } else {
               AutoTrapModule.Inner_BufferHelper plan = this.init_PathHelper_BufferH;
               boolean holdingForAura = plan != null && this.sync();
               if (plan != null && !holdingForAura) {
                  ce.Inner_DataUtil placed = this.init_ce_DataUtil_PathHelper_BufferH(plan);
                  if (placed != null) {
                     this.clear = 0;
                     this.get = 0;
                     this.init_PathHelper_BufferH = null;
                     if (plan.load_Z()) {
                        this.put = -1;
                     } else if (plan.init_Z() && this.bool("ignite") && this.init_PathHelper_ConfigS_Set(read_Set) != null && !this.init_Z_I_3(plan.init_I())) {
                        this.put = plan.init_I();
                        this.add = 0;
                     } else {
                        this.put = -1;
                        this.reset = this.integer("delay");
                     }

                     this.init_V_ce_DataUtil_Z(placed, true);
                     this.write_V_6();
                     return;
                  }
               }

               AutoTrapModule.Inner_BufferHelper planned = this.init_PathHelper_BufferH();
               if (planned != null) {
                  this.init_PathHelper_BufferH = planned;
                  this.get = 0;
               } else if (this.init_PathHelper_BufferH != null && (this.init_Z_PathHelper_BufferH(this.init_PathHelper_BufferH) || ++this.get >= 10)) {
                  this.init_PathHelper_BufferH = null;
                  this.set = -1;
                  this.get = 0;
               }

               AutoTrapModule.Inner_BufferHelper live = this.init_PathHelper_BufferH;
               this.init_V_ce_DataUtil(!holdingForAura && live != null ? live.init_ce_DataUtil() : null);
               this.write_V_6();
            }
         }
      } else {
         this.init_V_13();
      }
   }

   private boolean init_Z_14() {
      if (MC == null || MC.player == null || MC.level == null || MC.gameMode == null) {
         return false;
      } else if (aD.init_Z() || aC.init_Z()) {
         return false;
      } else if (!MC.player.isAlive() || MC.player.isSpectator()) {
         return false;
      } else if (cn.init_Z() || ScaffoldModule.update_Z_5()) {
         return false;
      } else if (!MC.player.isUsingItem() && !MC.player.isHandsBusy()) {
         return MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null ? !AutoTotemModule.init_Z_6() && !TableIndex.init_Z_20() : false;
      } else {
         return false;
      }
   }

   private void load_V_10() {
      this.init_PathHelper_BufferH = null;
      this.get = 0;
      this.clear = 0;
      this.set = -1;
      this.put = -1;
      this.write_V_6();
      if (this.read_Z_6()) {
         autismclient.util.aD.init(this.id());
         if (MC != null && MC.player != null) {
            autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
         }
      }
   }

   private boolean read_Z_6() {
      return this.id().equals(autismclient.util.aD.init_Str());
   }

   private void init_V_ce_DataUtil(ce.Inner_DataUtil goal) {
      this.init_V_ce_DataUtil_Z(goal, false);
   }

   private void init_V_ce_DataUtil_Z(ce.Inner_DataUtil goal, boolean pinQuiet) {
      if (goal != null) {
         autismclient.util.aD.init_V_Str(this.id(), 15, goal);
      } else if (this.read_Z_6()) {
         autismclient.util.aD.init(this.id());
      }

      if (this.read_Z_6()) {
         autismclient.util.aD.init_V_Str_5(this.id(), MC.player, pinQuiet);
      }
   }

   private boolean sync() {
      boolean wait = this.flush() && (MC.player.getAttackStrengthScale(0.5F) > 0.9F || this.close());
      this.clear = wait ? this.clear + 1 : 0;
      return wait && this.clear < 40;
   }

   private boolean flush() {
      return al.init_Module_Str("kill-aura") instanceof aa aura && aura.isEnabled() && aura.init_LivingEntity_3() != null;
   }

   private boolean close() {
      return MC.player.fallDistance > 0.0
         && !MC.player.onGround()
         && !MC.player.onClimbable()
         && !MC.player.isInWater()
         && !MC.player.hasEffect(MobEffects.BLINDNESS)
         && !MC.player.isPassenger();
   }

   private AutoTrapModule.Inner_BufferHelper init_PathHelper_BufferH() {
      List<LivingEntity> enemies = this.init_List_6();
      if (this.set >= 0) {
         boolean stillValid = false;

         for (LivingEntity enemy : enemies) {
            if (enemy.getId() == this.set) {
               stillValid = true;
               break;
            }
         }

         if (!stillValid) {
            this.set = -1;
         }
      }

      AutoTrapModule.Inner_BufferHelper pickup = this.load_PathHelper_BufferH();
      if (pickup != null) {
         return pickup;
      }

      if (enemies.isEmpty()) {
         return null;
      }

      if (this.put < 0) {
         AutoTrapModule.Inner_BufferHelper plan = this.bool("web") ? this.init_PathHelper_BufferH_List_Z(enemies, true) : null;
         if (plan == null && this.bool("ignite")) {
            plan = this.init_PathHelper_BufferH_List_Z(enemies, false);
         }

         return plan;
      } else {
         LivingEntity combo = null;

         for (LivingEntity enemy : enemies) {
            if (enemy.getId() == this.put) {
               combo = enemy;
               break;
            }
         }

         if (combo != null && !combo.isOnFire() && this.bool("ignite")) {
            return this.init_PathHelper_BufferH_List_Z(List.of(combo), false);
         }

         this.put = -1;
         this.reset = this.integer("delay");
         return null;
      }
   }

   private boolean init_Z_I_3(int targetId) {
      return MC.level.getEntity(targetId) instanceof LivingEntity living && living.isOnFire();
   }

   private AutoTrapModule.Inner_BufferHelper load_PathHelper_BufferH() {
      AutoTrapModule.Inner_CachePool source = this.init_PathHelper_CachePo;
      if (source == null) {
         return null;
      }

      BlockPos cell = source.init_BlockPos();
      if (!init_Z_BlockState_2(MC.level.getBlockState(cell))) {
         this.init_PathHelper_CachePo = null;
         return null;
      }

      int tick = ck.init_ck().state_I();
      if (tick - source.read() >= 200) {
         this.init_PathHelper_CachePo = null;
         return null;
      }

      if (!this.init_Z_I_3(source.init_I()) && MC.level.getEntity(source.init_I()) != null && tick - source.read() < 30) {
         return null;
      }

      ItemStack stack = source.init_InteractionHand() == InteractionHand.OFF_HAND
         ? MC.player.getOffhandItem()
         : MC.player.getInventory().getItem(source.load());
      if (!stack.is(Items.BUCKET)) {
         this.init_PathHelper_CachePo = null;
         return null;
      }

      if (source.init_InteractionHand() == InteractionHand.OFF_HAND) {
         if (autismclient.util.ah.load_Z(this.id()) || autismclient.util.s.load_Z()) {
            return null;
         }
      } else if (autismclient.util.ah.init(source.load(), this.id())) {
         return null;
      }

      Vec3 eye = MC.player.getEyePosition();
      if (Vec3.atCenterOf(cell).distanceToSqr(eye) > this.init_D_3() * this.init_D_3()) {
         return null;
      }

      ce.Inner_DataUtil rotation = ce.init(Vec3.atCenterOf(cell), eye);
      return Math.abs(rotation.load()) > autismclient.util.U.init_D()
         ? null
         : new AutoTrapModule.Inner_BufferHelper(false, source.init_I(), new AABB(cell), source.load(), source.init_InteractionHand(), rotation, true);
   }

   private static boolean init_Z_BlockState_2(BlockState state) {
      return state.is(Blocks.LAVA) && state.getFluidState().isSource();
   }

   private boolean init_Z_PathHelper_BufferH(AutoTrapModule.Inner_BufferHelper plan) {
      if (plan.load_Z()) {
         return this.init_PathHelper_CachePo == null;
      } else if (MC.level.getEntity(plan.init_I()) instanceof LivingEntity living && living.isAlive()) {
         boolean webDone = !this.bool("web") || this.init_PathHelper_ConfigS_Set(init_Set) == null || this.init_Z_AABB(living.getBoundingBox());
         boolean igniteDone = !this.bool("ignite") || this.init_PathHelper_ConfigS_Set(read_Set) == null || living.isOnFire();
         return webDone && igniteDone;
      } else {
         return true;
      }
   }

   private boolean init_Z_AABB(AABB box) {
      BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
      BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

      for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
         if (load_Set.contains(MC.level.getBlockState(pos).getBlock())) {
            return true;
         }
      }

      return false;
   }

   private AutoTrapModule.Inner_BufferHelper init_PathHelper_BufferH_List_Z(List<LivingEntity> enemies, boolean web) {
      AutoTrapModule.Inner_ConfigStore slot = this.init_PathHelper_ConfigS_Set(web ? init_Set : read_Set);
      if (slot == null) {
         return null;
      }

      Set<Block> trapBlocks = web ? load_Set : write_Set;
      BlockState placedState = this.init_BlockState_Z_ItemStack(web, slot.init_ItemStack());
      Vec3 eye = MC.player.getEyePosition();
      double reach = this.init_D_3();

      for (LivingEntity target : enemies) {
         if (web || !target.isOnFire()) {
            Vec3 trapPos = this.init_Vec3_LivingEntity_Z(target, target.getId() == this.set);
            if (trapPos != null) {
               trapPos = this.init_Vec3_Vec3(trapPos);
               BlockPos origin = BlockPos.containing(trapPos);
               if (!trapBlocks.contains(MC.level.getBlockState(origin).getBlock())) {
                  EntityDimensions dimensions = target.getDimensions(web ? Pose.STANDING : target.getPose());
                  boolean mustBeOnGround = slot.init_ItemStack().is(web ? Items.COBWEB : Items.FLINT_AND_STEEL);
                  Vec3 velocity = target.position().subtract(target.oldPosition());

                  for (BlockPos offset : this.init(trapPos, dimensions, velocity, mustBeOnGround, web, trapBlocks, eye)) {
                     ce.Inner_DataUtil rotation = this.init(origin.offset(offset), placedState, eye, reach, slot.init_ItemStack(), slot.init_InteractionHand());
                     if (rotation != null) {
                        this.set = target.getId();
                        return new AutoTrapModule.Inner_BufferHelper(
                           web, target.getId(), dimensions.makeBoundingBox(trapPos), slot.init_I(), slot.init_InteractionHand(), rotation, false
                        );
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private double init_D_3() {
      return MC.player.blockInteractionRange();
   }

   private BlockState init_BlockState_Z_ItemStack(boolean web, ItemStack stack) {
      if (web) {
         return Blocks.COBWEB.defaultBlockState();
      } else {
         return stack.is(Items.LAVA_BUCKET) ? Blocks.LAVA.defaultBlockState() : Blocks.FIRE.defaultBlockState();
      }
   }

   private AutoTrapModule.Inner_ConfigStore init_PathHelper_ConfigS_Set(Set<Item> items) {
      ItemStack offhand = MC.player.getOffhandItem();
      if (items.contains(offhand.getItem())
         && (this.init_PathHelper_CachePo == null || !offhand.is(Items.LAVA_BUCKET))
         && !autismclient.util.ah.load_Z(this.id())
         && !autismclient.util.s.load_Z()) {
         return new AutoTrapModule.Inner_ConfigStore(-1, InteractionHand.OFF_HAND, offhand);
      }

      int selected = MC.player.getInventory().getSelectedSlot();
      int best = -1;
      int bestDistance = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.ah.init(slot, this.id())) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (items.contains(stack.getItem()) && (this.init_PathHelper_CachePo == null || !stack.is(Items.LAVA_BUCKET))) {
               int distance = Math.abs(slot - selected);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = slot;
               }
            }
         }
      }

      return best < 0 ? null : new AutoTrapModule.Inner_ConfigStore(best, InteractionHand.MAIN_HAND, MC.player.getInventory().getItem(best));
   }

   private List<LivingEntity> init_List_6() {
      double minimum = 1.0;
      double reach = this.init_D_3();
      double maximum = reach * reach;
      Vec3 eyes = MC.player.getEyePosition();
      List<LivingEntity> found = new ArrayList<>();

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && this.init(living, eyes, minimum, maximum)) {
            found.add(living);
         }
      }

      if (found.size() > 1) {
         found.sort(this.init_Comparator());
      }

      return found;
   }

   private boolean init(LivingEntity entity, Vec3 eyes, double minimumSqr, double maximumSqr) {
      if (entity == MC.player || entity.isRemoved() || !entity.isAlive()) {
         return false;
      } else if (!(entity instanceof Attackable)) {
         return false;
      } else if (!EntitySelector.CAN_BE_PICKED.test(entity)) {
         return false;
      } else if (entity.hasPassenger(MC.player)) {
         return false;
      } else if (entity.hurtTime > this.integer("hurt-time")) {
         return false;
      } else if (ByteReader.load_Z(entity)) {
         return false;
      } else if (aL.init(entity, "killaura")) {
         return false;
      } else if (entity instanceof Player player && player.isSleeping()) {
         return false;
      } else {
         if (!this.init_Z_Entity_4(entity)) {
            return false;
         }

         double distanceSqr = this.init_D_Entity(entity);
         return !(distanceSqr < minimumSqr) && !(distanceSqr > maximumSqr)
            ? this.integer("fov") >= 180 || this.init_F_Entity_Vec3(entity, eyes) <= this.integer("fov") * 0.5F
            : false;
      }
   }

   private boolean init_Z_Entity_4(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.init_Set_4();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> init_Set_4() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.init_Str)) {
         return this.update_Set;
      }

      Set<String> normalized = new LinkedHashSet<>();

      for (String entry : entries) {
         if (entry != null) {
            String value = entry.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty()) {
               normalized.add(value);
               int separator = value.indexOf(58);
               if (separator >= 0 && separator + 1 < value.length()) {
                  normalized.add(value.substring(separator + 1));
               }
            }
         }
      }

      this.init_Str = source;
      this.update_Set = Set.copyOf(normalized);
      return this.update_Set;
   }

   private Comparator<LivingEntity> init_Comparator() {
      Comparator<LivingEntity> byType = Comparator.comparingInt(entity -> entity instanceof Player ? 0 : 1);
      Comparator<LivingEntity> byHealth = Comparator.comparingDouble(entity -> entity.getHealth() + entity.getAbsorptionAmount());
      Comparator<LivingEntity> byDistance = Comparator.comparingDouble(this::init_D_Entity);
      Comparator var10000;
      switch (this.choice("priority")) {
         case "Health":
            var10000 = byHealth.thenComparing(byType).thenComparing(byDistance);
            break;
         case "Distance":
            var10000 = byDistance.thenComparing(byType).thenComparing(byHealth);
            break;
         default:
            var10000 = byType.thenComparing(byHealth).thenComparing(byDistance);
      }

      return var10000;
   }

   private double init_D_Entity(Entity entity) {
      return entity.getBoundingBox().inflate(entity.getPickRadius()).distanceToSqr(MC.player.getEyePosition());
   }

   private float init_F_Entity_Vec3(Entity entity, Vec3 eyes) {
      ce.Inner_DataUtil toCenter = ce.init(entity.getBoundingBox().getCenter(), eyes);
      return ce.load(ce.init(MC.player), toCenter);
   }

   private void read_V_6() {
      if (!this.bool("predict")) {
         this.init_Map.clear();
      } else {
         Set<Integer> seen = new HashSet<>();
         double maximumSqr = 100.0;

         for (Player player : MC.level.players()) {
            if (player != MC.player && !player.isRemoved() && player.isAlive() && !(player.distanceToSqr(MC.player) > maximumSqr)) {
               seen.add(player.getId());
               ArrayDeque<AutoTrapModule.Inner_StreamUtil> history = this.init_Map.computeIfAbsent(player.getId(), key -> new ArrayDeque<>());
               history.addLast(this.init_PathHelper_StreamU_Player(player));

               while (history.size() > 10) {
                  history.removeFirst();
               }
            }
         }

         this.init_Map.keySet().retainAll(seen);
         this.load_Map.keySet().retainAll(seen);
      }
   }

   private AutoTrapModule.Inner_StreamUtil init_PathHelper_StreamU_Player(Player player) {
      Vec3 current = player.position();
      Vec3 velocity = current.subtract(player.oldPosition());
      boolean stationary = velocity.lengthSqr() < 0.001;
      if (player.onGround() && stationary) {
         Vec3[] still = new Vec3[4];
         Arrays.fill(still, current);
         return new AutoTrapModule.Inner_StreamUtil(null, 0, current, true, still);
      }

      AABB box = player.getBoundingBox();
      Vec3 position = current;
      double gravity = player.getAttributeValue(Attributes.GRAVITY);
      boolean wasAirborne = !player.onGround();
      boolean freeFalling = !player.onClimbable() && !player.isInWater();
      Vec3[] leadPath = new Vec3[4];
      Vec3 landing = null;
      int landingTick = 0;

      for (int tick = 1; tick <= 25; tick++) {
         List<VoxelShape> shapes = this.init(player, box, velocity);
         double dy = Shapes.collide(Axis.Y, box, shapes, velocity.y);
         box = box.move(0.0, dy, 0.0);
         double dx = Shapes.collide(Axis.X, box, shapes, velocity.x);
         box = box.move(dx, 0.0, 0.0);
         double dz = Shapes.collide(Axis.Z, box, shapes, velocity.z);
         box = box.move(0.0, 0.0, dz);
         position = new Vec3(position.x + dx, position.y + dy, position.z + dz);
         if (tick <= 4) {
            leadPath[tick - 1] = position;
         }

         boolean onGround = velocity.y <= 0.0 && dy != velocity.y;
         if (wasAirborne && onGround && landing == null) {
            landing = position;
            landingTick = tick;
            if (tick >= 4) {
               return new AutoTrapModule.Inner_StreamUtil(landing, landingTick, current, false, leadPath);
            }
         }

         wasAirborne = !onGround;
         double verticalSpeed = onGround ? 0.0 : (freeFalling ? (velocity.y - gravity) * 0.98 : velocity.y);
         velocity = new Vec3(dx == velocity.x ? velocity.x : 0.0, verticalSpeed, dz == velocity.z ? velocity.z : 0.0);
         if (onGround && velocity.horizontalDistanceSqr() < 1.0E-6) {
            break;
         }
      }

      for (int i = 0; i < leadPath.length; i++) {
         if (leadPath[i] == null) {
            leadPath[i] = position;
         }
      }

      return new AutoTrapModule.Inner_StreamUtil(landing, landingTick, current, stationary, leadPath);
   }

   private List<VoxelShape> init(Entity entity, AABB box, Vec3 velocity) {
      List<VoxelShape> shapes = new ArrayList<>();

      for (VoxelShape shape : MC.level.getBlockCollisions(entity, box.expandTowards(velocity))) {
         shapes.add(shape);
      }

      return shapes;
   }

   private Vec3 init_Vec3_LivingEntity_Z(LivingEntity target, boolean locked) {
      if (target instanceof Player && this.bool("predict")) {
         ArrayDeque<AutoTrapModule.Inner_StreamUtil> history = this.init_Map.get(target.getId());
         if (history != null && !history.isEmpty()) {
            AutoTrapModule.Inner_StreamUtil last = history.peekLast();
            if (init_Z_ArrayDeque(history)) {
               this.load_Map.remove(target.getId());
               return last.load();
            }

            int leadTicks = init_I_ArrayDeque(history);
            boolean longFall = last.init_Vec3() != null && last.init_I() > 4;
            List<Vec3> positions = new ArrayList<>();

            for (AutoTrapModule.Inner_StreamUtil entry : history) {
               Vec3 future = longFall ? entry.init_Vec3() : entry.init(leadTicks);
               if (future != null) {
                  positions.add(future);
               }
            }

            if (positions.size() < 5) {
               return locked ? last.load() : this.init(target, last, leadTicks);
            }

            Vec3 average = Vec3.ZERO;

            for (Vec3 position : positions) {
               average = average.add(position);
            }

            average = average.scale(1.0 / positions.size());
            double squared = 0.0;

            for (Vec3 position : positions) {
               squared += position.subtract(average).lengthSqr();
            }

            double spread = Math.sqrt(squared / positions.size());
            if (spread < 1.5) {
               this.load_Map.remove(target.getId());
               return positions.get(positions.size() - 1);
            } else {
               return this.init(target, last, leadTicks);
            }
         } else {
            return locked ? target.position() : this.init(target, null, 4);
         }
      } else {
         return target.position();
      }
   }

   private static boolean init_Z_ArrayDeque(ArrayDeque<AutoTrapModule.Inner_StreamUtil> history) {
      if (history.peekLast().init_Z()) {
         return true;
      }

      int elapsed = history.size() - 1;
      if (elapsed <= 0) {
         return false;
      }

      double perTickSqr = history.peekFirst().load().distanceToSqr(history.peekLast().load()) / (elapsed * elapsed);
      return perTickSqr < 0.001;
   }

   private static int init_I_ArrayDeque(ArrayDeque<AutoTrapModule.Inner_StreamUtil> history) {
      Vec3 previous = null;
      double walked = 0.0;
      double displacementX = 0.0;
      double displacementZ = 0.0;

      for (AutoTrapModule.Inner_StreamUtil entry : history) {
         Vec3 current = entry.load();
         if (previous != null) {
            double dx = current.x - previous.x;
            double dz = current.z - previous.z;
            walked += Math.sqrt(dx * dx + dz * dz);
            displacementX += dx;
            displacementZ += dz;
         }

         previous = current;
      }

      if (walked < 1.0E-6) {
         return 4;
      }

      double straight = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ) / walked;
      return 1 + (int)Math.round(straight * 3.0);
   }

   private Vec3 init(LivingEntity target, AutoTrapModule.Inner_StreamUtil last, int leadTicks) {
      int misses = this.load_Map.getOrDefault(target.getId(), 0) + 1;
      this.load_Map.put(target.getId(), misses);
      if (misses < 10) {
         return null;
      } else if (last == null) {
         return target.position();
      } else {
         return last.init_Vec3() != null && last.init_I() > 4 ? last.init_Vec3() : last.init(leadTicks);
      }
   }

   private List<BlockPos> init(Vec3 position, EntityDimensions dimensions, Vec3 velocity, boolean mustBeOnGround, boolean web, Set<Block> trapBlocks, Vec3 eye) {
      BlockPos origin = BlockPos.containing(position);
      AABB start = dimensions.makeBoundingBox(position).move(-origin.getX(), -origin.getY(), -origin.getZ());
      AABB end = start.move(velocity.x * 5.0, 0.0, velocity.z * 5.0);
      if (velocity.horizontalDistance() * 5.0 > 30.0) {
         return this.init(origin, BlockPos.ZERO, mustBeOnGround, web, trapBlocks) ? List.of(BlockPos.ZERO) : List.of();
      }

      record Inner_DataUtil(BlockPos init_BlockPos, double init_D, double load) {
      }

      List<Inner_DataUtil> ranked = new ArrayList<>();
      int minX = Mth.floor(start.minX);
      int maxX = Mth.ceil(start.maxX) - 1;
      int minY = Mth.floor(start.minY);
      int maxY = Mth.ceil(start.maxY) - 1;
      int minZ = Mth.floor(start.minZ);
      int maxZ = Mth.ceil(start.maxZ) - 1;

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos offset = new BlockPos(x, y, z);
               AABB unit = new AABB(offset);
               if ((start.intersects(unit) || end.intersects(unit)) && this.init(origin, offset, mustBeOnGround, web, trapBlocks)) {
                  double rank = init_D_AABB_AABB(start, unit) + init_D_AABB_AABB(end, unit) * 0.5;
                  ranked.add(new Inner_DataUtil(offset, rank, Vec3.atCenterOf(origin.offset(offset)).distanceToSqr(eye)));
               }
            }
         }
      }

      ranked.sort(Comparator.comparingDouble(Inner_DataUtil::init_D).reversed().thenComparingDouble(Inner_DataUtil::load));
      List<BlockPos> offsets = new ArrayList<>(ranked.size());

      for (Inner_DataUtil entry : ranked) {
         offsets.add(entry.init_BlockPos());
      }

      return offsets;
   }

   private Vec3 init_Vec3_Vec3(Vec3 position) {
      BlockPos pos = BlockPos.containing(position);

      int drop;
      for (drop = 0; drop < 3 && !MC.level.isOutsideBuildHeight(pos.below()) && MC.level.getBlockState(pos.below()).isAir(); drop++) {
         pos = pos.below();
      }

      return drop == 0 ? position : position.subtract(0.0, drop, 0.0);
   }

   private boolean init(BlockPos origin, BlockPos offset, boolean mustBeOnGround, boolean web, Set<Block> trapBlocks) {
      BlockPos cell = origin.offset(offset);
      if (MC.level.isOutsideBuildHeight(cell)) {
         return false;
      } else {
         BlockState state = MC.level.getBlockState(cell);
         if (!trapBlocks.contains(state.getBlock()) && state.canBeReplaced()) {
            return web && !state.getFluidState().isEmpty() ? false : !mustBeOnGround || !MC.level.getBlockState(cell.below()).isAir();
         } else {
            return false;
         }
      }
   }

   private static double init_D_AABB_AABB(AABB box, AABB unit) {
      return box.intersects(unit) ? box.intersect(unit).getSize() : 0.0;
   }

   private ce.Inner_DataUtil init(BlockPos cell, BlockState placedState, Vec3 eye, double reach, ItemStack material, InteractionHand hand) {
      if (MC.level.isOutsideBuildHeight(cell)) {
         return null;
      }

      BlockState cellState = MC.level.getBlockState(cell);
      if (!cellState.canBeReplaced()) {
         return null;
      }

      if (placedState.is(Blocks.COBWEB) && !cellState.getFluidState().isEmpty()) {
         return null;
      }

      if (Vec3.atCenterOf(cell).distanceToSqr(eye) < 1.0) {
         return null;
      }

      if (!MC.level.isUnobstructed(placedState, cell, CollisionContext.empty())) {
         return null;
      }

      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool(
         new autismclient.util.U.Inner_StateCache(cell, eye, reach, this.init_U_NetHelper_ItemStack_InteractionHand(material, hand))
            .init_U_StateCache_ce_DataUtil(ce.init(MC.player))
            .init_U_StateCache_D(autismclient.util.U.init_D())
            .init_U_StateCache_Z(MC.player.isSecondaryUseActive())
            .load(false)
            .init_U_StateCache_U_StreamUtil(new autismclient.util.U.Inner_StreamUtil(48))
      );
      return candidate == null ? null : candidate.init_U_DataUtil().init_ce_DataUtil();
   }

   private autismclient.util.U.Inner_NetHelper init_U_NetHelper_ItemStack_InteractionHand(ItemStack material, InteractionHand hand) {
      return material.getItem() instanceof BlockItem
         ? autismclient.util.U.init(material, MC.player, hand)
         : (hit, target) -> hit.getBlockPos().relative(hit.getDirection()).equals(target);
   }

   private ItemStack init_ItemStack_PathHelper_BufferH(AutoTrapModule.Inner_BufferHelper plan) {
      return plan.init_InteractionHand() == InteractionHand.OFF_HAND ? MC.player.getOffhandItem() : MC.player.getInventory().getItem(plan.load_I());
   }

   private BlockPos init_BlockPos_BlockHitResult_ItemStack(BlockHitResult ray, ItemStack stack) {
      return stack.getItem() instanceof BlockItem
         ? new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, ray).getClickedPos()
         : ray.getBlockPos().relative(ray.getDirection());
   }

   private boolean init(BlockHitResult hit, BlockPos cell, ItemStack stack) {
      if (!(stack.getItem() instanceof BlockItem)) {
         return true;
      }

      BlockPlaceContext context = new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, hit);
      return context.canPlace() && context.getClickedPos().equals(cell);
   }

   private static boolean init_Z_F(float pitch) {
      return ScaffoldModule.read_Z_F(Math.abs(pitch));
   }

   private ce.Inner_DataUtil init_ce_DataUtil_PathHelper_BufferH(AutoTrapModule.Inner_BufferHelper plan) {
      int tick = ck.init_ck().state_I();
      if (tick == this.start) {
         return null;
      }

      if (ListPool.init_Z_10()) {
         return null;
      }

      if (SurroundModule.init_Z_27()) {
         return null;
      }

      if (t.init_Z_51()) {
         return null;
      }

      if (AnchorAuraModule.init_Z_2()) {
         return null;
      }

      if (!this.open()) {
         return null;
      }

      if (HashUtil.load_Z()) {
         return null;
      }

      ch.Inner_CachePool wire = ch.init_ch_CachePool();
      if (!wire.init_Z()) {
         return null;
      }

      ce.Inner_DataUtil wireRotation = new ce.Inner_DataUtil(wire.read(), wire.write());
      if (!init_Z_F(wireRotation.load())) {
         return null;
      }

      ce.Inner_DataUtil silent = cn.init_ce_DataUtil(MC.player);
      if (silent != null && !init_Z_ce_DataUtil_ce_DataUtil_2(silent, wireRotation)) {
         return null;
      }

      if (plan.load_Z()) {
         return this.init(plan, tick, wireRotation);
      }

      double reach = this.init_D_3();
      BlockHitResult ray = ScaffoldModule.init_BlockHitResult_Vec3(MC.player.getEyePosition(), wireRotation, reach, MC.level, MC.player);
      if (!this.init_Z_PathHelper_BufferH_BlockHitResult(plan, ray)) {
         return null;
      }

      BlockPos clicked = ray.getBlockPos();
      if (!autismclient.util.U.load(MC.level.getBlockState(clicked), clicked, MC.player.isSecondaryUseActive())) {
         return null;
      }

      if (!this.load(plan)) {
         return null;
      }

      if (plan.init_InteractionHand() == InteractionHand.OFF_HAND && autismclient.util.s.load_Z()) {
         return null;
      }

      ItemStack stack = MC.player.getItemInHand(plan.init_InteractionHand());
      if (!this.init_Set_Z(plan.init_Z()).contains(stack.getItem())) {
         return null;
      }

      BlockPos placed = this.init_BlockPos_BlockHitResult_ItemStack(ray, stack);
      if (plan.init_Z() && !MC.level.getBlockState(placed).getFluidState().isEmpty()) {
         return null;
      }

      if (Vec3.atCenterOf(placed).distanceToSqr(MC.player.getEyePosition()) < 1.0) {
         return null;
      }

      if (!MC.level.isUnobstructed(this.init_BlockState_Z_ItemStack(plan.init_Z(), stack), placed, CollisionContext.empty())) {
         return null;
      }

      if (al.init_Z_HitResult(ray, plan.init_InteractionHand(), this.id())) {
         return null;
      }

      this.start = tick;
      this.read_J = System.nanoTime();
      this.stop = this.init_Random.nextInt(26);
      if (!this.init(plan, ray, stack)) {
         return null;
      }

      if (!plan.init_Z() && stack.is(Items.LAVA_BUCKET) && !MC.player.hasInfiniteMaterials()) {
         this.init_PathHelper_CachePo = new AutoTrapModule.Inner_CachePool(placed, plan.init_I(), plan.load_I(), plan.init_InteractionHand(), tick);
      }

      this.set = plan.init_I();
      return wireRotation;
   }

   private ce.Inner_DataUtil init(AutoTrapModule.Inner_BufferHelper plan, int tick, ce.Inner_DataUtil wireRotation) {
      AutoTrapModule.Inner_CachePool source = this.init_PathHelper_CachePo;
      if (source == null) {
         return null;
      }

      BlockHitResult ray = this.init_BlockHitResult_ce_DataUtil(wireRotation);
      if (ray == null || !ray.getBlockPos().equals(source.init_BlockPos())) {
         return null;
      }

      if (!init_Z_BlockState_2(MC.level.getBlockState(source.init_BlockPos()))) {
         this.init_PathHelper_CachePo = null;
         return null;
      }

      if (!this.load(plan)) {
         return null;
      }

      if (plan.init_InteractionHand() == InteractionHand.OFF_HAND && autismclient.util.s.load_Z()) {
         return null;
      }

      ItemStack stack = MC.player.getItemInHand(plan.init_InteractionHand());
      if (!stack.is(Items.BUCKET)) {
         return null;
      }

      if (al.init_Z_HitResult(ray, plan.init_InteractionHand(), this.id())) {
         return null;
      }

      this.start = tick;
      this.read_J = System.nanoTime();
      this.stop = this.init_Random.nextInt(26);
      return !this.init(plan, ray, stack) ? null : wireRotation;
   }

   private BlockHitResult init_BlockHitResult_ce_DataUtil(ce.Inner_DataUtil rotation) {
      Vec3 eye = MC.player.getEyePosition();
      Vec3 end = eye.add(Vec3.directionFromRotation(rotation.load(), rotation.init()).scale(this.init_D_3()));
      HitResult result = MC.level
         .clip(new ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.SOURCE_ONLY, CollisionContext.of(MC.player)));
      return result instanceof BlockHitResult hit && result.getType() == Type.BLOCK ? hit : null;
   }

   private boolean init_Z_PathHelper_BufferH_BlockHitResult(AutoTrapModule.Inner_BufferHelper plan, BlockHitResult ray) {
      if (ray == null) {
         return false;
      }

      BlockPos placed = this.init_BlockPos_BlockHitResult_ItemStack(ray, this.init_ItemStack_PathHelper_BufferH(plan));
      if (!new AABB(placed).intersects(plan.init_AABB())) {
         return false;
      }

      if (MC.level.isOutsideBuildHeight(placed)) {
         return false;
      }

      BlockState state = MC.level.getBlockState(placed);
      return plan.init_Z() && !state.getFluidState().isEmpty() ? false : state.canBeReplaced() && !this.load(plan.init_Z()).contains(state.getBlock());
   }

   private Set<Item> init_Set_Z(boolean web) {
      return web ? init_Set : read_Set;
   }

   private Set<Block> load(boolean web) {
      return web ? load_Set : write_Set;
   }

   private boolean open() {
      if (this.read_J == -9223372036854775808L) {
         return true;
      }

      long elapsedMs = (System.nanoTime() - this.read_J) / 1000000L;
      return elapsedMs >= 100L + this.stop - 3L;
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return this.start == ck.init_ck().state_I();
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z();
   }

   private boolean load(AutoTrapModule.Inner_BufferHelper plan) {
      if (plan.init_InteractionHand() == InteractionHand.OFF_HAND) {
         return true;
      }

      int selected = MC.player.getInventory().getSelectedSlot();
      if (selected == plan.load_I()) {
         return true;
      }

      if (!this.init_Z_I_I(plan.load_I(), selected)) {
         return false;
      }

      this.prev = plan.load_I();
      return false;
   }

   private boolean init_Z_I_I(int slot, int selected) {
      if (!ListPool.init_Z_10() && !SurroundModule.init_Z_27() && !t.init_Z_51() && !AnchorAuraModule.init_Z_2()) {
         int tick = ck.init_ck().state_I();
         if (tick == this.size) {
            return false;
         }

         if (!autismclient.util.ah.write_Z(this.id())) {
            return false;
         }

         try {
            if (this.next < 0) {
               this.next = selected;
            }

            autismclient.util.aw.init_V(MC, slot);
         } finally {
            autismclient.util.ah.read_V(this.id());
         }

         this.size = tick;
         return true;
      } else {
         return false;
      }
   }

   private boolean init(AutoTrapModule.Inner_BufferHelper plan, BlockHitResult ray, ItemStack stack) {
      if (!stack.isEmpty() && stack.isItemEnabled(MC.level.enabledFeatures())) {
         return !bM.init(this.id()) ? false : autismclient.util.s.init(ray, plan.init_InteractionHand());
      } else {
         return false;
      }
   }

   private static boolean init_Z_ce_DataUtil_ce_DataUtil_2(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
      return Math.abs(ce.init(first.init(), second.init())) <= 0.05F && Math.abs(first.load() - second.load()) <= 0.05F;
   }

   private void write_V_6() {
      if (this.next >= 0 && this.init_PathHelper_BufferH == null && this.valid <= 0) {
         this.valid = 2;
      }
   }

   private void update_V_5() {
      if (this.next >= 0) {
         if (!this.bool("switch-back")) {
            this.next = -1;
            this.prev = -1;
            this.valid = 0;
         } else if (MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null) {
            if (this.prev >= 0 && MC.player.getInventory().getSelectedSlot() != this.prev) {
               this.next = -1;
               this.prev = -1;
               this.valid = 0;
            } else if (this.valid > 0 && --this.valid <= 0) {
               if (!autismclient.util.ah.init(this.next, this.id()) && this.init_Z_I_I(this.next, MC.player.getInventory().getSelectedSlot())) {
                  this.next = -1;
                  this.prev = -1;
               } else {
                  this.valid = 1;
               }
            }
         }
      }
   }

   private record Inner_BufferHelper(
      boolean init_Z, int init_I, AABB init_AABB, int load_I, InteractionHand init_InteractionHand, ce.Inner_DataUtil init_ce_DataUtil, boolean load_Z
   ) {
   }

   private record Inner_CachePool(BlockPos init_BlockPos, int init_I, int load, InteractionHand init_InteractionHand, int read) {
   }

   private record Inner_ConfigStore(int init_I, InteractionHand init_InteractionHand, ItemStack init_ItemStack) {
   }

   private record Inner_StreamUtil(Vec3 init_Vec3, int init_I, Vec3 load, boolean init_Z, Vec3[] init_Vec3Arr) {
      Vec3 init(int ticks) {
         int index = ticks < 1 ? 0 : (ticks > this.init_Vec3Arr.length ? this.init_Vec3Arr.length - 1 : ticks - 1);
         return this.init_Vec3Arr[index];
      }
   }
}
