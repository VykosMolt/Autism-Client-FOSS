package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiBlazeArena implements AiOperation {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft.getConnection();
   private final Object read_Obj = this.init_Minecraft.player;
   private final BlockPos init_BlockPos;
   private final List<BlockPos> init_List;
   private final BlockPos load_BlockPos;
   private final List<BlockPos> load_List = new ArrayList<>();
   private int init_I;
   private int load_I;
   private int read_I;
   private AiOperation init_AiOperation;
   private AiOperation load_AiOperation;
   private final Set<BlockPos> init_Set = new HashSet<>();
   private final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer().init_DataPipe();
   private final AiFireControl init_K = new AiFireControl();
   private final long init_J;
   private int write;
   private int update;
   private int sync;
   private int flush;
   private final JsonArray init_JsonArray = new JsonArray();
   private final JsonArray load_JsonArray = new JsonArray();
   private boolean init_Z;
   private boolean load_Z;

   AiBlazeArena(BlockPos spawner, int seconds) {
      this.init_BlockPos = spawner.immutable();
      this.init_J = System.nanoTime() + Math.max(1, seconds) * 1000000000L;
      this.load_BlockPos = this.init_Minecraft.player.blockPosition();
      this.init_List = new ArrayList<>();
      int rise = Math.max(0, Math.min(2, (int)Math.ceil(spawner.getY() + 2.05 - this.init_Minecraft.player.getEyeY())));
      if (init_List_BlockPos(spawner)
         .stream()
         .filter(p -> p.getY() == spawner.getY() + 2)
         .noneMatch(p -> this.init_Minecraft.level.getBlockState(p).canBeReplaced())) {
         rise = 0;
      }

      if (rise > 0) {
         for (Direction direction : Plane.HORIZONTAL) {
            List<List<BlockPos>> columns = new ArrayList<>();
            List<BlockPos> stands = new ArrayList<>();
            boolean safe = true;

            for (int n = 1; n <= rise; n++) {
               BlockPos base = this.load_BlockPos.relative(direction, n);
               if (!AiTerrain.isLoaded(this.init_Minecraft.level, base)
                  || !this.init_Minecraft.level.getBlockState(base.below()).isCollisionShapeFullBlock(this.init_Minecraft.level, base.below())) {
                  safe = false;
                  break;
               }

               for (int y = 0; y <= n + 1; y++) {
                  if (!this.init_Minecraft.level.getBlockState(base.above(y)).isAir() || !this.init_Minecraft.level.getFluidState(base.above(y)).isEmpty()) {
                     safe = false;
                  }
               }

               if (!safe) {
                  break;
               }

               List<BlockPos> column = new ArrayList<>();

               for (int y = 0; y < n; y++) {
                  column.add(base.above(y));
               }

               columns.add(column);
               stands.add(base.above(n));
            }

            if (safe) {
               for (int n = columns.size() - 1; n >= 0; n--) {
                  this.init_List.addAll(columns.get(n));
               }

               this.load_List.addAll(stands);
               break;
            }
         }
      }

      this.init_I = this.init_List.size();
      this.init_List.addAll(init_List_BlockPos(spawner));

      for (Entity e : AiWorldScan.init_List_Str_D("minecraft:blaze", 20.0)) {
         if (!(e.distanceToSqr(this.init_Minecraft.player) < 25.0) && this.init_Minecraft.player.hasLineOfSight(e)) {
            Vec3 line = e.getEyePosition().subtract(this.init_Minecraft.player.getEyePosition()).normalize();
            BlockPos screen = BlockPos.containing(this.init_Minecraft.player.position().add(line.x * 2.0, 0.0, line.z * 2.0));
            if (!(screen.distSqr(spawner) < 4.0) && !screen.equals(this.init_Minecraft.player.blockPosition())) {
               this.init_List.add(screen);
               this.init_List.add(screen.above());
               break;
            }
         }
      }
   }

   static List<BlockPos> init_List_BlockPos(BlockPos spawner) {
      List<BlockPos> cells = new ArrayList<>();
      cells.add(spawner.above());
      cells.add(spawner.above(2));

      for (int r = 1; r <= 2; r++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               if (Math.abs(x) + Math.abs(z) == r) {
                  cells.add(spawner.offset(x, 2, z));
               }
            }
         }
      }

      return cells;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.load_Z
         || this.init_Minecraft.player != this.read_Obj
         || this.init_Minecraft.level != this.init_Obj
         || this.init_Minecraft.getConnection() != this.load_Obj) {
         return this.init(AiOperation.Status.CANCELLED, "Spawner setup world/player changed");
      }

      if (!this.init_Minecraft.player.isAlive()) {
         return this.init(AiOperation.Status.FAILED, "Player died during spawner setup");
      }

      this.flush++;
      if (System.nanoTime() > this.init_J && this.canInterruptNow()) {
         return this.init(AiOperation.Status.SUCCEEDED, "Spawner setup budget ended; partial geometry recorded");
      }

      if (this.init_Minecraft.level.getBlockState(this.init_BlockPos).is(Blocks.SPAWNER)
         && this.init_Minecraft.level.getBlockEntity(this.init_BlockPos) instanceof SpawnerBlockEntity be
         && "minecraft:blaze".equals(AiWorldScan.init(be))) {
         if (!this.init_Z_7()
            && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
            && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())) {
            for (Entity e : AiWorldScan.init_List_Str_D("minecraft:blaze", 5.0)) {
               if (e instanceof Blaze && AiDefense.load(this.init_Minecraft, e) || e.distanceToSqr(this.init_Minecraft.player) < 9.0) {
                  return this.init(AiOperation.Status.SUCCEEDED, "A blaze is in melee range; prioritize its kill over more construction");
               }
            }

            if (AiCombat.init_by_StreamUtil(this.init_Minecraft) != null) {
               return this.init(AiOperation.Status.SUCCEEDED, "Incoming shot; hand control back to combat");
            }

            if (this.init_K.init(false, null)) {
               return this.init_AiOperation_Result_Str_4(this.init_K.init_Str());
            }
         }

         if (this.write >= this.init_List.size()) {
            return this.init(AiOperation.Status.SUCCEEDED, "Small spawner cap and observed shooter screen settled");
         }

         if (this.load_AiOperation != null) {
            AiOperation.Result r = this.load_AiOperation.tick();
            if (r.terminal()) {
               this.load_JsonArray
                  .add(
                     AiJson.objectOf(
                        "kind",
                        "placement-face",
                        "cell",
                        this.init_List.get(this.write).toShortString(),
                        "result",
                        r.status(),
                        "message",
                        r.message(),
                        "body",
                        this.init_Minecraft.player.position().toString()
                     )
                  );
               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  this.init_JsonArray
                     .add(AiJson.objectOf("cell", this.init_List.get(this.write).toShortString(), "result", "kept", "reason", r.message()));
                  this.sync++;
                  this.write++;
               }
            }

            return this.init_AiOperation_Result_Str_4("Reach the roof's actual support side over existing floor");
         } else if (this.load_I < this.load_List.size() && this.write >= this.init_I) {
            BlockPos stand = this.load_List.get(this.load_I);
            if (!this.init_Minecraft.level.getBlockState(stand.below()).isCollisionShapeFullBlock(this.init_Minecraft.level, stand.below())) {
               this.load_I = this.load_List.size();
               return this.init_AiOperation_Result_Str_4("Construction step unavailable; keep reachable existing cover");
            }

            if (this.init_AiOperation == null) {
               this.init_AiOperation = AiGoto.init(
                  stand,
                  0.6,
                  false,
                  false,
                  false,
                  3,
                  autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                  false,
                  0.0,
                  null,
                  new AiGoto.GotoOptions(false, false, 1, true, false, false)
               );
            }

            AiOperation.Result r = this.init_AiOperation.tick();
            if (r.terminal()) {
               this.load_JsonArray
                  .add(
                     AiJson.objectOf(
                        "stand", stand.toShortString(), "result", r.status(), "message", r.message(), "body", this.init_Minecraft.player.position().toString()
                     )
                  );
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  this.load_I++;
                  this.read_I++;
               } else {
                  this.load_I = this.load_List.size();
               }
            }

            return this.init_AiOperation_Result_Str_4("Walk onto the confirmed construction stair to reach roof faces");
         } else {
            BlockPos cell = this.init_List.get(this.write);
            if (!this.init_Z) {
               String refusal = this.init_Str_BlockPos_2(cell);
               if (!refusal.isEmpty() && !refusal.equals("outside current placement reach")) {
                  this.init_JsonArray.add(AiJson.objectOf("cell", cell.toShortString(), "result", "kept", "reason", refusal));
                  this.sync++;
                  this.write++;
                  return this.init_AiOperation_Result_Str_4("Reusing existing cover or preserving the open exit");
               }

               Item material = this.init_Item();
               if (material == null) {
                  return this.init(AiOperation.Status.SUCCEEDED, "No expendable nonflammable blocks; use existing fortress cover");
               }

               if (!autismclient.ai.engine.AiPlacer.init_Z_BlockPos_Item(cell, material)) {
                  if (this.write >= this.init_I && this.init_Set.add(cell)) {
                     this.load_AiOperation = AiGoto.init_AiOperation_BlockPos_2(cell, material, 2);
                     return this.init_AiOperation_Result_Str_4("Find a visible support face without more scaffolding");
                  }

                  this.init_JsonArray
                     .add(
                        AiJson.objectOf("cell", cell.toShortString(), "result", "kept", "reason", "no reachable support face after bounded approach")
                     );
                  this.sync++;
                  this.write++;
                  return this.init_AiOperation_Result_Str_4("Keep a partial cap where no legal support side is reachable");
               }

               this.init_DataPipe.init_V_BlockPos_Item(cell, material);
               this.init_Z = true;
            }

            autismclient.ai.engine.AiPlacer.PlaceStatus status = this.init_DataPipe.init_DataPipe_DataUtil();
            if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING) {
               if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                  this.update++;
               } else {
                  this.sync++;
               }

               this.init_JsonArray
                  .add(
                     AiJson.objectOf(
                        "cell",
                        cell.toShortString(),
                        "result",
                        status,
                        "reason",
                        status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED ? this.init_DataPipe.init_Str() : "server block change confirmed",
                        "body",
                        this.init_Minecraft.player.position().toString(),
                        "eyeY",
                        this.init_Minecraft.player.getEyeY(),
                        "onGround",
                        this.init_Minecraft.player.onGround(),
                        "velocity",
                        this.init_Minecraft.player.getDeltaMovement().toString()
                     )
                  );
               this.init_DataPipe.init_V();
               this.init_Z = false;
               this.write++;
            }

            return this.init_AiOperation_Result_Str_4("Set minimal spawner escape cap / fireball screen (" + this.update + " blocks confirmed)");
         }
      } else {
         return this.init(AiOperation.Status.FAILED, "Observed blaze spawner no longer present");
      }
   }

   private String init_Str_BlockPos_2(BlockPos cell) {
      if (!AiTerrain.isLoaded(this.init_Minecraft.level, cell)) {
         return "unobserved cell";
      }

      if (!this.init_Minecraft.level.getBlockState(cell).canBeReplaced()) {
         return "existing block retained";
      }

      if (!this.init_Minecraft.level.getFluidState(cell).isEmpty()) {
         return "fluid preserved";
      }

      if (new AABB(cell).inflate(0.15).intersects(this.init_Minecraft.player.getBoundingBox())) {
         return "body/exit clearance";
      }

      if (!AiCombat.init_Z_Minecraft_BlockPos(this.init_Minecraft, this.init_Minecraft.player.blockPosition())) {
         return "unsafe current footing";
      }

      if (this.init_Minecraft.player.getEyePosition().distanceTo(Vec3.atCenterOf(cell)) > this.init_Minecraft.player.blockInteractionRange() - 0.2) {
         return "outside current placement reach";
      }

      if (!this.init_Minecraft.level.getEntitiesOfClass(LivingEntity.class, new AABB(cell)).isEmpty()) {
         return "living entity occupies cell";
      }

      if (cell.getY() < this.init_BlockPos.getY() + 1
         && new AABB(cell).clip(this.init_Minecraft.player.getEyePosition(), Vec3.atCenterOf(this.init_BlockPos)).isPresent()) {
         return "melee lane preserved";
      }

      for (Direction d : Direction.values()) {
         if (!this.init_Minecraft.level.getBlockState(cell.relative(d)).getCollisionShape(this.init_Minecraft.level, cell.relative(d)).isEmpty()) {
            return "";
         }
      }

      return "no existing support face";
   }

   private Item init_Item() {
      if (AiRunResources.isCampaignActive() && AiRunResources.spareBuildingBlockCount() <= 32) {
         return null;
      }

      for (int i = 0; i < 36; i++) {
         ItemStack stack = this.init_Minecraft.player.getInventory().getItem(i);
         if (AiItems.isPlainItem(stack) && stack.getItem() instanceof BlockItem block) {
            Block b = block.getBlock();
            if (b == Blocks.COBBLESTONE || b == Blocks.COBBLED_DEEPSLATE || b == Blocks.NETHERRACK || b == Blocks.NETHER_BRICKS || b == Blocks.STONE) {
               return stack.getItem();
            }
         }
      }

      return null;
   }

   private JsonObject init_JsonObject_16() {
      return AiJson.objectOf(
         "spawner",
         this.init_BlockPos.toShortString(),
         "blocksConfirmed",
         this.update,
         "skipped",
         this.sync,
         "planned",
         this.init_List.size(),
         "visited",
         this.write,
         "ticks",
         this.flush,
         "completeEnclosure",
         false,
         "placementOutcomes",
         this.init_JsonArray,
         "constructionStairs",
         this.load_List.size(),
         "constructionStairsReached",
         this.read_I,
         "constructionWalks",
         this.load_JsonArray,
         "home",
         this.load_BlockPos.toShortString(),
         "purpose",
         "limit hover/escape locally and screen observed fireball lane"
      );
   }

   private AiOperation.Result init_AiOperation_Result_Str_4(String m) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, m, this.init_JsonObject_16());
   }

   private AiOperation.Result init(AiOperation.Status s, String m) {
      JsonObject d = this.init_JsonObject_16();
      this.cancel();
      return AiGameOperations.result(s, m, d);
   }

   private boolean init_Z_7() {
      return this.init_Z && this.init_DataPipe.load_Z();
   }

   @Override
   public boolean canInterruptNow() {
      return !this.init_Z_7()
         && this.init_K.load_Z()
         && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_Z_7()
         || this.init_K.read()
         || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      this.init_DataPipe.init_V();
      this.init_K.init_V();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      if (this.load_AiOperation != null) {
         this.load_AiOperation.cancel();
      }
   }
}
