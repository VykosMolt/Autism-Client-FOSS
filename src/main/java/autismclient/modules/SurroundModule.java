package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.util.AuditClientFixes;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.RegistryIdValidator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class SurroundModule extends Module implements SilentMovementCoordinator.Participant {
   private static final Direction[] init_DirectionArr = new Direction[]{Direction.DOWN, Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
   private static final Direction[] load_DirectionArr = new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH};
   private static final float init_F = 600.0F;
   private static final double init_D = 0.25;
   private static final int init_I = 4;
   private static final double load_D = 0.001;
   private static final double read_D = 0.001;
   private static final int load_I = 6;
   private static final int read_I = 5;
   private static final int write_I = 256;
   private static final long init_J = 2000000000L;
   private static final int update_I = 64;
   private final Map<Integer, SurroundModule.Inner_DataUtil> init_Map = new ConcurrentHashMap<>();
   private final Set<BlockPos> init_Set = new HashSet<>();
   private final List<BlockPos> init_List = new ArrayList<>();
   private final Set<BlockPos> load_Set = new LinkedHashSet<>();
   private boolean init_Z;
   private double write_D;
   private double update_D;
   private double sync_D;
   private double flush_D;
   private double close_D;
   private boolean load_Z;
   private int sync_I = -2147483648;
   private int flush_I = -2147483648;
   private final autismclient.util.BlockPlacementPlanner.RayBudget init_U_StreamUtil = new autismclient.util.BlockPlacementPlanner.RayBudget(4);
   private int close_I = -1;
   private int open = -1;
   private int poll;
   private final KillAuraModule.Inner_JsonUtil init_aa_JsonUtil = new KillAuraModule.Inner_JsonUtil();
   private String init_Str;
   private Set<Block> read_Set = Set.of();
   private BlockPos init_BlockPos;

   public SurroundModule() {
      super("surround", "Surround", ae.read, "Places blocks around your feet.");
      this.add(new BoolSetting("down", "Down", true).group("Features").description("Fill under the surround floor"));
      this.add(new ChoiceSetting("ring", "Ring", "Center", "Center", "Hitbox").group("Features").description("Ring around center or hitbox"));
      this.add(
         new BoolSetting("no-waste", "NoWaste", true)
            .group("Features")
            .visibleWhen(() -> "Hitbox".equals(this.choice("ring")))
            .description("Skip extra cells inside holes")
      );
      this.add(new BoolSetting("disable-on-y-change", "YChange", true).group("DisableOn").description("Turn off on Y change"));
      this.add(new BoolSetting("disable-on-xz-move", "XZMove", false).group("DisableOn").description("Turn off leaving start block"));
      this.add(new BoolSetting("disable-on-xz-speed", "XZSpeed", false).group("DisableOn").description("Turn off when moving fast"));
      this.add(new ChoiceSetting("filter-mode", "Filter", "Whitelist", "Whitelist", "Blacklist").group("Blocks").description("How the list is applied"));
      this.add(
         RegistryListSetting.blocks("blocks", "Blocks", "minecraft:obsidian|minecraft:crying_obsidian|minecraft:ender_chest")
            .group("Blocks")
            .description("Surround material")
      );
      this.add(new IntSetting("aim-speed", "Aim Speed", 3, 1, 5, 1).group("Placing").description("Turn and place speed"));
      this.add(new BoolSetting("switch-back", "Switch Back", true).group("Placing").description("Return to your old slot"));
      this.add(new BoolSetting("protect", "Protect", true).group("Protect").description("Refill blocks others are mining"));
      this.add(
         new IntSetting("min-destroy-progress", "Min Destroy Progress", 4, 0, 9, 1)
            .group("Protect")
            .unit("stage")
            .visibleWhen(() -> this.bool("protect"))
            .description("Stage to start reacting at")
      );
      this.add(
         new BoolSetting("extra-layer", "Extra Layer", true)
            .group("Protect")
            .visibleWhen(() -> this.bool("protect"))
            .description("Second ring around mined cells")
      );
      this.add(
         new BoolSetting("extra-layer-corners", "Corners", false)
            .group("Protect")
            .visibleWhen(() -> this.bool("protect") && this.bool("extra-layer"))
            .description("Also fill the diagonals")
      );
      this.add(new BoolSetting("force-extra-layer", "Force Extra Layer", false).group("Protect").description("Extra layer on every cell"));
   }

   @Override
   public void onEnable() {
      this.init_V_23();
   }

   @Override
   public void onDisable() {
      this.init_V_23();
      autismclient.util.SilentRotationManager.init(this.id());
   }

   @Override
   public void onGameLeft() {
      this.init_V_23();
      if (this.id().equals(autismclient.util.SilentRotationManager.init_Str())) {
         autismclient.util.SilentRotationManager.load_V();
      }
   }

   private void init_V_23() {
      this.init_Z = false;
      this.load_Z = false;
      this.init_List.clear();
      this.load_Set.clear();
      this.init_Set.clear();
      this.init_Map.clear();
      this.sync_I = -2147483648;
      this.close_I = -1;
      this.open = -1;
      this.poll = 0;
      autismclient.util.InventorySlotArbiter.update_V(this.id());
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.SilentRotationManager.read_Z() && this.id().equals(autismclient.util.SilentRotationManager.init_Str());
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && this.id().equals(autismclient.util.SilentRotationManager.init_Str())) {
         autismclient.util.SilentRotationManager.init_V_Str_LocalPlayer(this.id(), MC.player);
      }
   }

   @Override
   public String info() {
      int remaining = this.init_List.size();
      return remaining <= 0 ? "" : Integer.toString(remaining);
   }

   public static boolean init_Z_27() {
      if (!(al.init_Module_Str("surround") instanceof SurroundModule surround)) {
         return false;
      } else {
         int age = AutismSessionState.init_ck().state_I() - surround.flush_I;
         return age >= 0 && age <= 5 && "surround".equals(autismclient.util.SilentRotationManager.init_Str());
      }
   }

   @Override
   public boolean load_Z_23() {
      boolean enabled = this.isEnabled();
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.init_Z() || enabled && this.read_Z_13());
   }

   private boolean read_Z_13() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !aD.init_Z()
         && !FreecamController.init_Z()
         && !RemoteViewController.init_Z()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !MacroExecutor.write_Z()
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !SilentMovementCoordinator.isSafeWalkActive()
         && !ScaffoldModule.update_Z_5()
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.init_Z_20();
   }

   private boolean sync_Z_7() {
      return this.init_aa_JsonUtil.init(AutismSessionState.init_ck().state_I(), this::flush_Z_6);
   }

   private boolean flush_Z_6() {
      if (MC != null && MC.player != null) {
         ItemStack mainHand = MC.player.getMainHandItem();
         return KillAuraModule.init_Z_ItemStack_6(mainHand) ? true : mainHand.isEmpty() && KillAuraModule.init_Z_ItemStack_6(MC.player.getOffhandItem());
      } else {
         return false;
      }
   }

   private void load_V_19() {
      if (MC != null && MC.player != null && this.id().equals(autismclient.util.SilentRotationManager.init_Str())) {
         autismclient.util.SilentRotationManager.init(this.id());
         autismclient.util.SilentRotationManager.init_V_Str_LocalPlayer(this.id(), MC.player);
      }
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundBlockDestructionPacket destruction) {
         if (!this.isEnabled() || !this.bool("protect")) {
            return false;
         }

         if (AuditClientFixes.isLocalPlayerId(destruction.getId())) {
            return false;
         }

         int progress = destruction.getProgress();
         if (progress >= 0 && progress <= 9) {
            if (this.init_Map.size() >= 256 && !this.init_Map.containsKey(destruction.getId())) {
               return false;
            }

            this.init_Map.put(destruction.getId(), new SurroundModule.Inner_DataUtil(destruction.getPos().asLong(), progress, System.nanoTime()));
            return false;
         } else {
            this.init_Map.remove(destruction.getId());
            return false;
         }
      } else {
         return false;
      }
   }

   private void read_V_13() {
      this.init_Set.clear();
      if (!this.bool("protect")) {
         this.init_Map.clear();
      } else {
         long now = System.nanoTime();
         int minStage = this.integer("min-destroy-progress");
         this.init_Map.values().removeIf(entryx -> now - entryx.load() > 2000000000L);

         for (SurroundModule.Inner_DataUtil entry : this.init_Map.values()) {
            if (entry.init_I() >= minStage) {
               this.init_Set.add(BlockPos.of(entry.init_J()));
            }
         }
      }
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         if (MC != null && MC.player != null && MC.level != null && MC.getConnection() != null) {
            if (!this.init_Z) {
               this.write_V_13();
            }

            this.flush_V_6();
            double x = MC.player.getX();
            double y = MC.player.getY();
            double z = MC.player.getZ();
            String reason = this.init_Str_D(x, y, z);
            this.flush_D = x;
            this.close_D = z;
            this.load_Z = true;
            if (reason != null) {
               this.disableWithToggleMessage("Surround disabled: " + reason + ".");
            } else {
               this.read_V_13();
               this.update_V_11();
               this.sync_V_7();
            }
         } else {
            this.init_List.clear();
            this.load_V_19();
         }
      }
   }

   private void write_V_13() {
      this.write_D = MC.player.getY();
      BlockPos block = MC.player.blockPosition();
      this.update_D = block.getX() + 0.5;
      this.sync_D = block.getZ() + 0.5;
      this.load_Z = false;
      this.init_Z = true;
   }

   private String init_Str_D(double x, double y, double z) {
      if (this.bool("disable-on-y-change") && y != this.write_D) {
         return "your Y changed";
      }

      if (!this.bool("disable-on-xz-move") || !(Math.abs(x - this.update_D) > 0.5) && !(Math.abs(z - this.sync_D) > 0.5)) {
         if (this.bool("disable-on-xz-speed") && this.load_Z) {
            double dx = x - this.flush_D;
            double dz = z - this.close_D;
            if (dx * dx + dz * dz >= 0.25) {
               return "you moved too fast";
            }
         }

         return null;
      } else {
         return "you left the block";
      }
   }

   private void update_V_11() {
      this.init_List.clear();
      AABB box = MC.player.getBoundingBox();
      int feetY = Mth.floor(box.minY + 0.001);
      List<BlockPos> hole = this.init(box, feetY);
      Set<BlockPos> seen = new HashSet<>(hole);
      List<BlockPos> ring = new ArrayList<>();
      List<BlockPos> down = new ArrayList<>();
      List<BlockPos> extra = new ArrayList<>();
      boolean addDown = this.bool("down");
      boolean forceExtra = this.bool("force-extra-layer");
      boolean extraLayer = this.bool("protect") && this.bool("extra-layer");
      boolean corners = extraLayer && this.bool("extra-layer-corners");
      Direction[] order = this.init_DirectionArr();

      for (BlockPos holePos : hole) {
         for (Direction direction : order) {
            BlockPos pos = holePos.relative(direction);
            if (seen.add(pos)) {
               if (direction == Direction.DOWN) {
                  down.add(pos);
                  if (addDown) {
                     init_V_List_2(down, seen, holePos.below(2));
                  }
               } else {
                  ring.add(pos);
                  if (forceExtra || extraLayer && this.init_Set.contains(pos)) {
                     init_V_List_2(extra, seen, pos.relative(direction));
                     init_V_List_2(extra, seen, pos.above());
                     if (corners) {
                        init_V_List_2(extra, seen, pos.relative(direction.getClockWise()));
                     }
                  }
               }
            }
         }
      }

      this.init_V_List(ring);
      this.init_V_List(down);
      this.init_V_List(extra);
   }

   private Direction[] init_DirectionArr() {
      if (this.integer("aim-speed") <= 1) {
         return init_DirectionArr;
      }

      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.init_ch_CachePool();
      float fromYaw = wire.init_Z() ? wire.read() : MC.player.getYRot();
      int start = 0;
      float best = 3.4028235E38F;

      for (int i = 0; i < load_DirectionArr.length; i++) {
         float turn = Math.abs(RotationMath.angleDelta(load_DirectionArr[i].toYRot(), fromYaw));
         if (turn < best) {
            best = turn;
            start = i;
         }
      }

      Direction[] order = new Direction[load_DirectionArr.length + 1];
      order[0] = Direction.DOWN;

      for (int i = 0; i < load_DirectionArr.length; i++) {
         order[i + 1] = load_DirectionArr[(start + i) % load_DirectionArr.length];
      }

      return order;
   }

   private static void init_V_List_2(List<BlockPos> target, Set<BlockPos> seen, BlockPos pos) {
      if (seen.add(pos)) {
         target.add(pos);
      }
   }

   private void init_V_List(List<BlockPos> group) {
      for (BlockPos pos : group) {
         if (this.load_Set.contains(pos) && MC.level.getBlockState(pos).canBeReplaced()) {
            this.init_List.add(pos);
         }
      }

      for (BlockPos pos : group) {
         if (!this.load_Set.contains(pos) && MC.level.getBlockState(pos).canBeReplaced()) {
            this.init_List.add(pos);
         }
      }
   }

   private List<BlockPos> init(AABB box, int feetY) {
      BlockPos feet = new BlockPos(Mth.floor((box.minX + box.maxX) * 0.5), feetY, Mth.floor((box.minZ + box.maxZ) * 0.5));
      if (!"Hitbox".equals(this.choice("ring"))) {
         return List.of(feet);
      }

      if (this.bool("no-waste") && this.init_Z_BlockPos_5(feet)) {
         return List.of(feet);
      }

      int minColX = Mth.floor(box.minX + 0.001);
      int maxColX = Mth.floor(box.maxX - 0.001);
      int minColZ = Mth.floor(box.minZ + 0.001);
      int maxColZ = Mth.floor(box.maxZ - 0.001);
      List<BlockPos> columns = new ArrayList<>(4);

      for (int columnX = minColX; columnX <= maxColX; columnX++) {
         for (int columnZ = minColZ; columnZ <= maxColZ; columnZ++) {
            columns.add(new BlockPos(columnX, feetY, columnZ));
         }
      }

      return columns.isEmpty() ? List.of(feet) : columns;
   }

   private boolean init_Z_BlockPos_5(BlockPos feet) {
      for (Direction direction : init_DirectionArr) {
         float resistance = MC.level.getBlockState(feet.relative(direction)).getBlock().getExplosionResistance();
         if (resistance < 600.0F) {
            return false;
         }
      }

      return true;
   }

   private void sync_V_7() {
      if (!this.read_Z_13()) {
         this.load_V_19();
      } else {
         int slot = this.init_I_17();
         if (this.sync_Z_7()) {
            this.load_V_19();
            if (slot >= 0 && slot != MC.player.getInventory().getSelectedSlot()) {
               this.init_V_I_9(slot);
            }
         } else if (BlinkPacketManager.load_Z()) {
            this.load_V_19();
         } else if (!this.init_List.isEmpty() && slot >= 0) {
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.init_ch_CachePool();
            RotationMath.Rotation wireRotation = wire.init_Z() ? new RotationMath.Rotation(wire.read(), wire.write()) : null;
            RotationMath.Rotation from = wireRotation != null ? wireRotation : RotationMath.ofPlayer(MC.player);
            ItemStack material = MC.player.getInventory().getItem(slot);
            SurroundModule.Inner_StreamUtil plan = this.init_aK_StreamUtil_ce_DataUtil(from, material, null);
            if (plan == null) {
               this.load_V_19();
               this.open();
            } else {
               this.poll = 0;
               boolean slotReady = slot == MC.player.getInventory().getSelectedSlot();
               BlockHitResult hit = wireRotation == null ? null : this.init(plan, wireRotation);
               boolean fire = hit != null && slotReady && !BedDefenderModule.init_Z_10() && this.close_Z_5();
               boolean aimed;
               if (fire) {
                  SurroundModule.Inner_StreamUtil next = this.init_aK_StreamUtil_ce_DataUtil(from, material, plan.init_BlockPos());
                  aimed = this.init(next != null ? next.init_ce_DataUtil() : plan.init_ce_DataUtil(), true);
               } else {
                  aimed = this.init(plan.init_ce_DataUtil(), false);
               }

               if (fire && aimed) {
                  RotationMath.Rotation outgoing = SilentMovementCoordinator.getRenderRotation(MC.player);
                  if (outgoing != null && !(Math.abs(outgoing.pitch() - wireRotation.pitch()) > 0.05F)) {
                     this.init(plan, hit);
                  }
               } else if (!slotReady) {
                  this.init_V_I_9(slot);
               }
            }
         } else {
            this.load_V_19();
            this.open();
         }
      }
   }

   private boolean init(RotationMath.Rotation goal, boolean holdPitch) {
      autismclient.util.SilentRotationManager.init_V_Str(this.id(), 25, goal);
      this.flush_I = AutismSessionState.init_ck().state_I();
      if (!this.id().equals(autismclient.util.SilentRotationManager.init_Str())) {
         return false;
      }

      int speed = this.integer("aim-speed");
      autismclient.util.SilentRotationManager.init(this.id(), MC.player, init_F_I_2(speed), holdPitch ? 0.0F : init_F_I_2(speed), init_ap_StreamUtil_I(speed));
      return true;
   }

   private static autismclient.util.RotationStreamPlanner.Inner_StreamUtil init_ap_StreamUtil_I(int speed) {
      autismclient.util.RotationStreamPlanner.Inner_StreamUtil var10000;
      switch (speed) {
         case 2:
            var10000 = autismclient.util.RotationStreamPlanner.Inner_StreamUtil.SURROUND_FAST_2;
            break;
         case 3:
            var10000 = autismclient.util.RotationStreamPlanner.Inner_StreamUtil.SURROUND_FAST_3;
            break;
         case 4:
            var10000 = autismclient.util.RotationStreamPlanner.Inner_StreamUtil.SURROUND_FAST_4;
            break;
         case 5:
            var10000 = autismclient.util.RotationStreamPlanner.Inner_StreamUtil.SURROUND_FAST_5;
            break;
         default:
            var10000 = autismclient.util.RotationStreamPlanner.Inner_StreamUtil.STANDARD;
      }

      return var10000;
   }

   private static float init_F_I_2(int speed) {
      float var10000;
      switch (speed) {
         case 3:
            var10000 = 90.0F;
            break;
         case 4:
            var10000 = 135.0F;
            break;
         case 5:
            var10000 = 180.0F;
            break;
         default:
            var10000 = 72.0F;
      }

      return var10000;
   }

   private SurroundModule.Inner_StreamUtil init_aK_StreamUtil_ce_DataUtil(RotationMath.Rotation from, ItemStack material, BlockPos skip) {
      double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
      Vec3 eye = MC.player.getEyePosition();
      AABB playerBox = MC.player.getBoundingBox();
      Vec3 delta = MC.player.getDeltaMovement();
      BlockState placed = material.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
      autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(
            null, eye, reach, autismclient.util.BlockPlacementPlanner.init(material, MC.player, InteractionHand.MAIN_HAND)
         )
         .init_U_StateCache_ce_DataUtil(from)
         .init_U_StateCache_Z(MC.player.isShiftKeyDown())
         .init_U_StateCache_Vec3(eye.add(delta))
         .init_U_StateCache_U_StreamUtil(this.init_U_StreamUtil);

      for (BlockPos cell : this.init_List) {
         if (!cell.equals(skip) && !this.init(cell, placed, playerBox, delta)) {
            this.init_U_StreamUtil.init(4);
            autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.init_U_CachePool(request.init_U_StateCache_BlockPos(cell));
            if (candidate != null) {
               return new SurroundModule.Inner_StreamUtil(cell, candidate);
            }
         }
      }

      return null;
   }

   private boolean init(BlockPos cell, BlockState placed, AABB playerBox, Vec3 delta) {
      AABB box = new AABB(cell);
      if (!box.intersects(playerBox) && !box.intersects(playerBox.move(delta)) && !box.intersects(playerBox.move(delta.scale(-1.0)))) {
         return placed != null
            ? !MC.level.isUnobstructed(placed, cell, CollisionContext.empty())
            : !MC.level.getEntities(MC.player, box, EntitySelector.NO_SPECTATORS).isEmpty();
      } else {
         return true;
      }
   }

   private BlockHitResult init(SurroundModule.Inner_StreamUtil plan, RotationMath.Rotation rotation) {
      double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
      Vec3 eye = MC.player.getEyePosition();
      autismclient.util.BlockPlacementPlanner.Query gate = new autismclient.util.BlockPlacementPlanner.Query(
            plan.init_BlockPos(),
            eye,
            reach,
            autismclient.util.BlockPlacementPlanner.init(MC.player.getItemInHand(InteractionHand.MAIN_HAND), MC.player, InteractionHand.MAIN_HAND)
         )
         .init_U_StateCache_Z(MC.player.isShiftKeyDown());
      return autismclient.util.BlockPlacementPlanner.init_BlockHitResult_U_CachePool(plan.init_U_CachePool(), rotation, eye, reach, gate);
   }

   private boolean close_Z_5() {
      return AutismSessionState.init_ck().state_I() != this.sync_I;
   }

   private void init(SurroundModule.Inner_StreamUtil plan, BlockHitResult hit) {
      InteractionHand hand = InteractionHand.MAIN_HAND;
      if (!al.init_Z_HitResult(hit, hand, this.id()) && autismclient.util.InventorySlotArbiter.write_Z(this.id())) {
         try {
            if (!autismclient.util.ClientInteractionOverride.init(hit, hand)) {
               return;
            }

            if (!PerTickPlacementLock.init(this.id())) {
               autismclient.util.ClientInteractionOverride.write_V();
               return;
            }

            this.close_V_5();
            this.init_BlockPos = plan.init_BlockPos();
         } finally {
            autismclient.util.InventorySlotArbiter.read_V(this.id());
         }
      }
   }

   private void flush_V_6() {
      if (this.init_BlockPos != null) {
         BlockPos cell = this.init_BlockPos;
         this.init_BlockPos = null;
         if (!MC.level.getBlockState(cell).canBeReplaced()) {
            this.init_V_BlockPos_5(cell);
         }
      }
   }

   private void close_V_5() {
      this.sync_I = AutismSessionState.init_ck().state_I();
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return this.sync_I == AutismSessionState.init_ck().state_I();
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.SilentRotationManager.init_Str()) && autismclient.util.SilentRotationManager.read_Z();
   }

   private void init_V_BlockPos_5(BlockPos cell) {
      this.init_List.remove(cell);
      BlockPos immutable = cell.immutable();
      this.load_Set.remove(immutable);
      this.load_Set.add(immutable);

      while (this.load_Set.size() > 64) {
         this.load_Set.remove(this.load_Set.iterator().next());
      }
   }

   private int init_I_17() {
      Inventory inventory = MC.player.getInventory();
      int selected = inventory.getSelectedSlot();
      int best = -1;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.init(slot, this.id())) {
            ItemStack stack = inventory.getItem(slot);
            if (this.init_Z_ItemStack_5(stack)) {
               if (slot == selected) {
                  return slot;
               }

               if (best < 0 || stack.getCount() > inventory.getItem(best).getCount()) {
                  best = slot;
               }
            }
         }
      }

      return best;
   }

   private void init_V_I_9(int slot) {
      if (!BedDefenderModule.init_Z_10() && autismclient.util.InventorySlotArbiter.write_Z(this.id())) {
         try {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (this.close_I < 0 && this.bool("switch-back")) {
               this.close_I = selected;
            }

            autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
            this.open = slot;
         } finally {
            autismclient.util.InventorySlotArbiter.read_V(this.id());
         }
      }
   }

   private void open() {
      if (this.close_I >= 0) {
         if (!this.bool("switch-back")) {
            this.close_I = -1;
            this.open = -1;
         } else if (this.open >= 0 && MC.player.getInventory().getSelectedSlot() != this.open) {
            this.close_I = -1;
            this.open = -1;
         } else if (++this.poll >= 6 && autismclient.util.InventorySlotArbiter.write_Z(this.id())) {
            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, this.close_I);
               this.close_I = -1;
               this.open = -1;
               this.poll = 0;
            } finally {
               autismclient.util.InventorySlotArbiter.read_V(this.id());
            }
         }
      }
   }

   private boolean init_Z_ItemStack_5(ItemStack stack) {
      if (!(stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem)) {
         return false;
      }

      if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      }

      boolean listed = this.init_Set_7().contains(blockItem.getBlock());
      return "Whitelist".equals(this.choice("filter-mode")) == listed;
   }

   private Set<Block> init_Set_7() {
      String raw = this.value("blocks");
      if (raw.equals(this.init_Str)) {
         return this.read_Set;
      }

      Set<Block> blocks = new HashSet<>();

      for (String entry : this.list("blocks")) {
         Identifier identifier = Identifier.tryParse(RegistryIdValidator.init(entry));
         if (identifier != null) {
            BuiltInRegistries.BLOCK.getOptional(identifier).ifPresent(blocks::add);
         }
      }

      this.init_Str = raw;
      this.read_Set = Set.copyOf(blocks);
      return this.read_Set;
   }

   private record Inner_DataUtil(long init_J, int init_I, long load) {
   }

   private record Inner_StreamUtil(BlockPos init_BlockPos, autismclient.util.BlockPlacementPlanner.PlacementPlan init_U_CachePool) {
      RotationMath.Rotation init_ce_DataUtil() {
         return this.init_U_CachePool.init_U_DataUtil().init_ce_DataUtil();
      }
   }
}
