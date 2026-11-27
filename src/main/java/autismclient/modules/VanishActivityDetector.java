package autismclient.modules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.status.ChunkStatus;

final class VanishActivityDetector {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private final autismclient.util.UnexplainedBlockTracker tracker = new autismclient.util.UnexplainedBlockTracker();
   private final Map<Long, Long> suppressedUntil = new HashMap<>();
   private final Map<Long, VanishActivityDetector.Inner_CachePool> pendingChanges = new LinkedHashMap<>();
   private final ArrayDeque<VanishActivityDetector.Inner_DataUtil> actors = new ArrayDeque<>();
   private final ArrayDeque<VanishActivityDetector.Inner_StreamUtil> hazards = new ArrayDeque<>();
   private long lastExpire = -1L;
   private long quietUntil;

   static boolean isTrackableBlock(BlockState state, boolean placement) {
      Block block = state.getBlock();
      if (!state.isAir()
         && state.getFluidState().isEmpty()
         && block.asItem() != Items.AIR
         && !state.canBeReplaced()
         && !(block instanceof BushBlock)
         && !(block instanceof LeavesBlock)
         && !(block instanceof GrowingPlantBlock)
         && !(block instanceof FallingBlock)
         && !(block instanceof BaseFireBlock)
         && !(block instanceof LiquidBlock)
         && !(block instanceof InfestedBlock)
         && !(block instanceof IceBlock)
         && (!(block instanceof TntBlock) || placement)
         && !(block instanceof MovingPistonBlock)
         && !(block instanceof SnowLayerBlock)) {
         String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
         return !placement
               || (
                     !(block instanceof RotatedPillarBlock)
                        || !state.is(BlockTags.LOGS)
                           && !path.endsWith("_log")
                           && !path.endsWith("_wood")
                           && !path.endsWith("_stem")
                           && !path.endsWith("_hyphae")
                  )
                  && !(block instanceof HugeMushroomBlock)
                  && !(block instanceof BeehiveBlock)
                  && block != Blocks.NETHER_WART_BLOCK
                  && block != Blocks.WARPED_WART_BLOCK
                  && block != Blocks.SHROOMLIGHT
                  && block != Blocks.MANGROVE_ROOTS
                  && block != Blocks.MUDDY_MANGROVE_ROOTS
                  && block != Blocks.ROOTED_DIRT
                  && block != Blocks.CREAKING_HEART
            ? state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
               || block instanceof StairBlock
               || block instanceof SlabBlock
               || block instanceof FenceBlock
               || block instanceof WallBlock
               || block instanceof ChestBlock
               || block instanceof BarrelBlock
            : false;
      } else {
         return false;
      }
   }

   static autismclient.util.UnexplainedBlockTracker.Inner_CachePool classifyChange(BlockState before, BlockState after) {
      if (before.getBlock() == after.getBlock()) {
         return null;
      } else if (before.isAir() && isTrackableBlock(after, true)) {
         return autismclient.util.UnexplainedBlockTracker.Inner_CachePool.PLACE;
      } else {
         return after.isAir() && isTrackableBlock(before, false) ? autismclient.util.UnexplainedBlockTracker.Inner_CachePool.BREAK : null;
      }
   }

   void init(BlockPos pos, BlockState after, long at) {
      if (isChunkLoaded(pos) && !(MINECRAFT.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 1024.0)) {
         BlockState before = MINECRAFT.level.getBlockState(pos);
         this.tracker.init(pos, blockId(after));
         if (before.getBlock() instanceof MovingPistonBlock || after.getBlock() instanceof MovingPistonBlock) {
            this.tracker.init(pos, 1.5, at + 3000L);
         }

         this.load(pos, before, at);
         this.load(pos, after, at);
         autismclient.util.UnexplainedBlockTracker.Inner_CachePool kind = classifyChange(before, after);
         if (kind != null) {
            if (this.pendingChanges.size() >= 256) {
               this.pendingChanges.remove(this.pendingChanges.keySet().iterator().next());
            }

            this.pendingChanges
               .put(pos.asLong(), new VanishActivityDetector.Inner_CachePool(kind == autismclient.util.UnexplainedBlockTracker.Inner_CachePool.PLACE ? after : before, kind, at + 10000L));
            if (this.suppressedUntil.getOrDefault(pos.asLong(), 0L) <= at && at >= this.quietUntil && !this.isExplained(pos, at)) {
               this.tracker
                  .onChange(
                     new autismclient.util.UnexplainedBlockTracker.Inner_StreamUtil(pos.immutable(), blockId(before), blockId(after), kind, at)
                  );
            } else {
               this.tracker.init(pos, 1.5, at + 2000L);
            }
         }
      }
   }

   void init(ClientboundSectionBlocksUpdatePacket packet, long at, boolean armed) {
      List<Entry<BlockPos, BlockState>> updates = new ArrayList<>();
      int[] total = new int[]{0};
      BlockPos[] first = new BlockPos[]{null};
      packet.runUpdates((pos, state) -> {
         if (first[0] == null) {
            first[0] = pos.immutable();
         }

         total[0]++;
         if (updates.size() < 4) {
            updates.add(Map.entry(pos.immutable(), state));
         }
      });
      if (total[0] <= 4 && armed) {
         for (Entry<BlockPos, BlockState> update : updates) {
            this.init(update.getKey(), update.getValue(), at);
         }
      } else if (first[0] != null) {
         this.tracker.suppress(first[0], at + 3500L);
      }
   }

   boolean init_Z_Packet_J(Packet<?> packet, long at) {
      if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         this.tracker.suppress(new BlockPos(chunk.x() << 4, 0, chunk.z() << 4), at + 3000L);
         return true;
      } else if (packet instanceof ClientboundForgetLevelChunkPacket chunk) {
         this.tracker.suppress(new BlockPos(chunk.pos().x() << 4, 0, chunk.pos().z() << 4), at + 3000L);
         return true;
      } else if (packet instanceof ClientboundBlockEventPacket event) {
         if (event.getBlock() instanceof PistonBaseBlock) {
            Direction direction = Direction.from3DDataValue(event.getB1() & 7);
            PistonStructureResolver resolver = new PistonStructureResolver(MINECRAFT.level, event.getPos(), direction, event.getB0() == 0);
            resolver.resolve();
            Direction moving = event.getB0() == 0 ? direction : direction.getOpposite();

            for (BlockPos p : resolver.getToPush()) {
               this.tracker.init(p, 1.5, at + 3000L);
               this.tracker.init(p.relative(moving), 1.5, at + 3000L);
            }

            for (BlockPos p : resolver.getToDestroy()) {
               this.tracker.init(p, 1.5, at + 3000L);
            }

            for (int i = 0; i <= 13; i++) {
               this.tracker.init(event.getPos().relative(direction, i), 1.5, at + 3000L);
            }
         }

         return true;
      } else if (packet instanceof ClientboundExplodePacket explosion) {
         this.tracker.init(BlockPos.containing(explosion.center()), Math.max(12.0F, explosion.radius() * 2.0F + 6.0F), at + 4000L);
         return true;
      } else if (!(packet instanceof ClientboundLevelEventPacket event)) {
         return false;
      } else {
         if ((event.getType() == 1000 || event.getType() == 1001 || event.getType() == 1002) && isChunkLoaded(event.getPos())) {
            BlockState source = MINECRAFT.level.getBlockState(event.getPos());
            if (source.getBlock() instanceof DispenserBlock && source.hasProperty(BlockStateProperties.FACING)) {
               this.addHazard(
                  new VanishActivityDetector.Inner_StreamUtil(event.getPos().relative(source.getValue(BlockStateProperties.FACING)), false, false, true, at + 3000L)
               );
            }
         }

         return true;
      }
   }

   void init_V_Packet_J(Packet<?> packet, long at) {
      if (packet instanceof ServerboundPlayerActionPacket action) {
         if (action.getAction() == Action.START_DESTROY_BLOCK
            || action.getAction() == Action.STOP_DESTROY_BLOCK
            || action.getAction() == Action.ABORT_DESTROY_BLOCK) {
            this.suppressCell(action.getPos(), at);
            this.init(action.getPos(), MINECRAFT.level.getBlockState(action.getPos()).getBlock(), at);
         }
      } else if (packet instanceof ServerboundUseItemOnPacket use) {
         BlockPos clicked = use.hitResult().getBlockPos();
         BlockPos adjacent = clicked.relative(use.hitResult().getDirection());
         this.suppressCell(clicked, at);
         this.suppressCell(adjacent, at);
         if (MINECRAFT.player.getItemInHand(use.hand()).getItem() instanceof BlockItem item) {
            this.init(clicked, item.getBlock(), at);
            this.init(adjacent, item.getBlock(), at);
         }
      } else if (packet instanceof ServerboundChatCommandPacket command) {
         String text = command.command().replace("minecraft:", "");
         if (text.matches("(?s)(setblock|fill|clone|place) .*")) {
            this.quietUntil = at + 3000L;
         }
      }
   }

   private void init(BlockPos p, Block block, long at) {
      if (block instanceof DoorBlock) {
         this.suppressCell(p.above(), at);
         this.suppressCell(p.below(), at);
      }

      if (block instanceof BedBlock) {
         for (Direction direction : Plane.HORIZONTAL) {
            this.suppressCell(p.relative(direction), at);
         }
      }
   }

   private void suppressCell(BlockPos pos, long at) {
      if (this.suppressedUntil.size() >= 8192) {
         this.suppressedUntil.remove(this.suppressedUntil.keySet().iterator().next());
      }

      this.suppressedUntil.put(pos.asLong(), at + 30000L);
      this.tracker.init(pos, 0.0, at + 30000L);
   }

   void expire(long at) {
      this.suppressedUntil.values().removeIf(until -> until <= at);
      this.pendingChanges.values().removeIf(w -> w.expiresAt <= at);

      while (!this.actors.isEmpty() && this.actors.peekFirst().expiresAt <= at) {
         this.actors.removeFirst();
      }

      this.hazards.removeIf(h -> h.expiresAt <= at);
      if (this.lastExpire < 0L || at - this.lastExpire >= 100L) {
         this.lastExpire = at;

         for (Entity entity : MINECRAFT.level.entitiesForRendering()) {
            if (entity != null && entity != MINECRAFT.player && !(entity.distanceToSqr(MINECRAFT.player) > 4096.0)) {
               BlockPos pos = entity.blockPosition();
               if (entity instanceof Player) {
                  this.tracker.init(pos, 8.0, at + 1500L);
               } else {
                  int kind = entity instanceof Enderman
                     ? 1
                     : (
                        entity instanceof EnderDragon
                           ? 2
                           : (
                              entity instanceof WitherBoss
                                 ? 3
                                 : (
                                    !(entity instanceof PrimedTnt)
                                          && !(entity instanceof Creeper creeper && (creeper.isIgnited() || creeper.getSwellDir() > 0))
                                       ? (
                                          entity instanceof FallingBlockEntity
                                             ? 5
                                             : ((entity instanceof IronGolem || entity instanceof SnowGolem) && entity.tickCount < 60 ? 6 : 0)
                                       )
                                       : 4
                                 )
                           )
                     );
                  if (kind != 0) {
                     this.actors
                        .addLast(
                           new VanishActivityDetector.Inner_DataUtil(pos.immutable(), kind, kind == 2 ? 24.0 : (kind == 3 ? 6.0 : (kind == 4 ? 12.0 : 6.0)), at + 2500L)
                        );
                  }

                  if (this.actors.size() > 2048) {
                     this.actors.removeFirst();
                  }
               }
            }
         }
      }

      for (Entry<Long, VanishActivityDetector.Inner_CachePool> entry : this.pendingChanges.entrySet()) {
         BlockPos pos = BlockPos.of(entry.getKey());
         if (this.load(pos, at) || this.read(pos, at)) {
            this.tracker.init(pos, 0.0, at + 1000L);
         }
      }

      this.tracker
         .init(
            at,
            (posx, after) -> isChunkLoaded(posx) && blockId(MINECRAFT.level.getBlockState(posx)).equals(after),
            posx -> this.isExplained(posx, at)
         );
   }

   private boolean isExplained(BlockPos pos, long at) {
      if (at < this.quietUntil || this.suppressedUntil.getOrDefault(pos.asLong(), 0L) > at || !isChunkLoaded(pos)) {
         return true;
      }

      if (this.load(pos, at)) {
         return true;
      }

      if (this.read(pos, at)) {
         return true;
      }

      VanishActivityDetector.Inner_CachePool focus = this.pendingChanges.get(pos.asLong());
      // Only the fluid/heat test can match beyond distSqr 4; skip the outer cells when no fluid/heat state can explain this block.
      int r = focus != null
            && (
               init(focus.state, focus.change, true, true)
                  || init(focus.state, focus.change, true, false)
                  || init(focus.state, focus.change, false, true)
            )
         ? 3
         : 2;

      for (int dx = -r; dx <= r; dx++) {
         for (int dy = -r; dy <= r; dy++) {
            for (int dz = -r; dz <= r; dz++) {
               BlockPos p = pos.offset(dx, dy, dz);
               if ((r == 3 || dx * dx + dy * dy + dz * dz <= 4) && isChunkLoaded(p)) {
                  BlockState state = MINECRAFT.level.getBlockState(p);
                  if (state.getBlock() instanceof MovingPistonBlock && p.distSqr(pos) <= 4.0) {
                     return true;
                  }

                  if (focus != null
                     && focus.change == autismclient.util.UnexplainedBlockTracker.Inner_CachePool.PLACE
                     && p.distManhattan(pos) == 1
                     && (focus.state.getBlock() == Blocks.MELON || focus.state.getBlock() == Blocks.PUMPKIN)
                     && (state.getBlock() instanceof StemBlock || state.getBlock() instanceof AttachedStemBlock)) {
                     return true;
                  }

                  if (focus != null
                     && init(
                        focus.state,
                        focus.change,
                        !state.getFluidState().isEmpty(),
                        state.getBlock() instanceof BaseFireBlock || state.getFluidState().is(FluidTags.LAVA)
                     )) {
                     return true;
                  }

                  if (focus != null
                     && load(focus.state)
                     && state.getBlock() instanceof DispenserBlock
                     && state.hasProperty(BlockStateProperties.TRIGGERED)
                     && state.getValue(BlockStateProperties.TRIGGERED)
                     && p.relative(state.getValue(BlockStateProperties.FACING)).equals(pos)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean load(BlockPos pos, long at) {
      VanishActivityDetector.Inner_CachePool focus = this.pendingChanges.get(pos.asLong());

      for (VanishActivityDetector.Inner_DataUtil actor : this.actors) {
         if (actor.expiresAt > at
            && !(actor.pos.distSqr(pos) > actor.radius * actor.radius)
            && (actor.kind != 1 || focus == null || isEndermanBlock(focus.state))
            && (actor.kind != 5 || focus == null || BuiltInRegistries.BLOCK.getKey(focus.state.getBlock()).getPath().endsWith("_concrete"))
            && (
               actor.kind != 6
                  || focus == null
                  || focus.state.getBlock() == Blocks.IRON_BLOCK
                  || focus.state.getBlock() == Blocks.SNOW_BLOCK
                  || focus.state.getBlock() instanceof CarvedPumpkinBlock
            )) {
            return true;
         }
      }

      return false;
   }

   static boolean isEndermanBlock(BlockState state) {
      Block b = state.getBlock();
      return state.is(BlockTags.ENDERMAN_HOLDABLE)
         || b == Blocks.DIRT
         || b == Blocks.GRASS_BLOCK
         || b == Blocks.PODZOL
         || b == Blocks.MYCELIUM
         || b == Blocks.CLAY
         || b == Blocks.MELON
         || b == Blocks.PUMPKIN;
   }

   private void load(BlockPos pos, BlockState state, long at) {
      boolean fluid = !state.getFluidState().isEmpty();
      boolean heat = state.getBlock() instanceof BaseFireBlock || state.getFluidState().is(FluidTags.LAVA);
      if (fluid || heat) {
         this.addHazard(new VanishActivityDetector.Inner_StreamUtil(pos.immutable(), fluid, heat, false, at + 4000L));
      }
   }

   private void addHazard(VanishActivityDetector.Inner_StreamUtil hazard) {
      if (this.hazards.size() >= 1024) {
         this.hazards.removeFirst();
      }

      this.hazards.addLast(hazard);
   }

   private boolean read(BlockPos pos, long at) {
      VanishActivityDetector.Inner_CachePool focus = this.pendingChanges.get(pos.asLong());
      if (focus == null) {
         return false;
      }

      for (VanishActivityDetector.Inner_StreamUtil h : this.hazards) {
         if (h.expiresAt > at
            && h.pos.distSqr(pos) <= (h.read ? 0 : 12)
            && (h.read ? load(focus.state) : init(focus.state, focus.change, h.init_Z, h.load))) {
            return true;
         }
      }

      return false;
   }

   static boolean load(BlockState state) {
      return state.getBlock() instanceof CarvedPumpkinBlock || state.getBlock() instanceof ShulkerBoxBlock || state.getBlock() instanceof TntBlock;
   }

   static boolean init(BlockState state, autismclient.util.UnexplainedBlockTracker.Inner_CachePool kind, boolean fluid, boolean heat) {
      if (kind == autismclient.util.UnexplainedBlockTracker.Inner_CachePool.BREAK) {
         return heat && state.ignitedByLava();
      }

      Block b = state.getBlock();
      String path = BuiltInRegistries.BLOCK.getKey(b).getPath();
      return fluid
         && (
            path.endsWith("_concrete")
               ? !heat
               : heat && (b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.OBSIDIAN || b == Blocks.BASALT || b == Blocks.SMOOTH_BASALT)
         );
   }

   private static boolean isChunkLoaded(BlockPos p) {
      return MINECRAFT.level != null && MINECRAFT.level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, ChunkStatus.FULL, false) != null;
   }

   private static String blockId(BlockState state) {
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
   }

   List<autismclient.util.UnexplainedBlockTracker.Inner_BufferHelper> unexplained(long at) {
      return this.tracker.activeReadings(at);
   }

   void init() {
      this.tracker.clear();
      this.suppressedUntil.clear();
      this.pendingChanges.clear();
      this.actors.clear();
      this.hazards.clear();
      this.lastExpire = -1L;
      this.quietUntil = 0L;
   }

   private record Inner_CachePool(BlockState state, autismclient.util.UnexplainedBlockTracker.Inner_CachePool change, long expiresAt) {
   }

   private record Inner_DataUtil(BlockPos pos, int kind, double radius, long expiresAt) {
   }

   private record Inner_StreamUtil(BlockPos pos, boolean init_Z, boolean load, boolean read, long expiresAt) {
   }
}
