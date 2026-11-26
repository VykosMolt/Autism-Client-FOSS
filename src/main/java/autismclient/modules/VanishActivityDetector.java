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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private final autismclient.util.DataUtil init_DataUtil = new autismclient.util.DataUtil();
   private final Map<Long, Long> init_Map = new HashMap<>();
   private final Map<Long, VanishActivityDetector.Inner_CachePool> load_Map = new LinkedHashMap<>();
   private final ArrayDeque<VanishActivityDetector.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>();
   private final ArrayDeque<VanishActivityDetector.Inner_StreamUtil> load_ArrayDeque = new ArrayDeque<>();
   private long init_J = -1L;
   private long load_J;

   static boolean init_Z_BlockState_Z(BlockState state, boolean placement) {
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

   static autismclient.util.DataUtil.Inner_CachePool init_DataUtil_CachePool_BlockState_BlockState(BlockState before, BlockState after) {
      if (before.getBlock() == after.getBlock()) {
         return null;
      } else if (before.isAir() && init_Z_BlockState_Z(after, true)) {
         return autismclient.util.DataUtil.Inner_CachePool.PLACE;
      } else {
         return after.isAir() && init_Z_BlockState_Z(before, false) ? autismclient.util.DataUtil.Inner_CachePool.BREAK : null;
      }
   }

   void init(BlockPos pos, BlockState after, long at) {
      if (init_Z_BlockPos(pos) && !(init_Minecraft.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 1024.0)) {
         BlockState before = init_Minecraft.level.getBlockState(pos);
         this.init_DataUtil.init(pos, init_Str_BlockState(after));
         if (before.getBlock() instanceof MovingPistonBlock || after.getBlock() instanceof MovingPistonBlock) {
            this.init_DataUtil.init(pos, 1.5, at + 3000L);
         }

         this.load(pos, before, at);
         this.load(pos, after, at);
         autismclient.util.DataUtil.Inner_CachePool kind = init_DataUtil_CachePool_BlockState_BlockState(before, after);
         if (kind != null) {
            if (this.load_Map.size() >= 256) {
               this.load_Map.remove(this.load_Map.keySet().iterator().next());
            }

            this.load_Map
               .put(pos.asLong(), new VanishActivityDetector.Inner_CachePool(kind == autismclient.util.DataUtil.Inner_CachePool.PLACE ? after : before, kind, at + 10000L));
            if (this.init_Map.getOrDefault(pos.asLong(), 0L) <= at && at >= this.load_J && !this.init_Z_BlockPos_J(pos, at)) {
               this.init_DataUtil
                  .init_V_DataUtil_StreamUti(
                     new autismclient.util.DataUtil.Inner_StreamUtil(pos.immutable(), init_Str_BlockState(before), init_Str_BlockState(after), kind, at)
                  );
            } else {
               this.init_DataUtil.init(pos, 1.5, at + 2000L);
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
         this.init_DataUtil.init_V(first[0], at + 3500L);
      }
   }

   boolean init_Z_Packet_J(Packet<?> packet, long at) {
      if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         this.init_DataUtil.init_V(new BlockPos(chunk.x() << 4, 0, chunk.z() << 4), at + 3000L);
         return true;
      } else if (packet instanceof ClientboundForgetLevelChunkPacket chunk) {
         this.init_DataUtil.init_V(new BlockPos(chunk.pos().x() << 4, 0, chunk.pos().z() << 4), at + 3000L);
         return true;
      } else if (packet instanceof ClientboundBlockEventPacket event) {
         if (event.getBlock() instanceof PistonBaseBlock) {
            Direction direction = Direction.from3DDataValue(event.getB1() & 7);
            PistonStructureResolver resolver = new PistonStructureResolver(init_Minecraft.level, event.getPos(), direction, event.getB0() == 0);
            resolver.resolve();
            Direction moving = event.getB0() == 0 ? direction : direction.getOpposite();

            for (BlockPos p : resolver.getToPush()) {
               this.init_DataUtil.init(p, 1.5, at + 3000L);
               this.init_DataUtil.init(p.relative(moving), 1.5, at + 3000L);
            }

            for (BlockPos p : resolver.getToDestroy()) {
               this.init_DataUtil.init(p, 1.5, at + 3000L);
            }

            for (int i = 0; i <= 13; i++) {
               this.init_DataUtil.init(event.getPos().relative(direction, i), 1.5, at + 3000L);
            }
         }

         return true;
      } else if (packet instanceof ClientboundExplodePacket explosion) {
         this.init_DataUtil.init(BlockPos.containing(explosion.center()), Math.max(12.0F, explosion.radius() * 2.0F + 6.0F), at + 4000L);
         return true;
      } else if (!(packet instanceof ClientboundLevelEventPacket event)) {
         return false;
      } else {
         if ((event.getType() == 1000 || event.getType() == 1001 || event.getType() == 1002) && init_Z_BlockPos(event.getPos())) {
            BlockState source = init_Minecraft.level.getBlockState(event.getPos());
            if (source.getBlock() instanceof DispenserBlock && source.hasProperty(BlockStateProperties.FACING)) {
               this.init_V_UiHelper_StreamUti(
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
            this.init_V_BlockPos_J(action.getPos(), at);
            this.init(action.getPos(), init_Minecraft.level.getBlockState(action.getPos()).getBlock(), at);
         }
      } else if (packet instanceof ServerboundUseItemOnPacket use) {
         BlockPos clicked = use.hitResult().getBlockPos();
         BlockPos adjacent = clicked.relative(use.hitResult().getDirection());
         this.init_V_BlockPos_J(clicked, at);
         this.init_V_BlockPos_J(adjacent, at);
         if (init_Minecraft.player.getItemInHand(use.hand()).getItem() instanceof BlockItem item) {
            this.init(clicked, item.getBlock(), at);
            this.init(adjacent, item.getBlock(), at);
         }
      } else if (packet instanceof ServerboundChatCommandPacket command) {
         String text = command.command().replace("minecraft:", "");
         if (text.matches("(?s)(setblock|fill|clone|place) .*")) {
            this.load_J = at + 3000L;
         }
      }
   }

   private void init(BlockPos p, Block block, long at) {
      if (block instanceof DoorBlock) {
         this.init_V_BlockPos_J(p.above(), at);
         this.init_V_BlockPos_J(p.below(), at);
      }

      if (block instanceof BedBlock) {
         for (Direction direction : Plane.HORIZONTAL) {
            this.init_V_BlockPos_J(p.relative(direction), at);
         }
      }
   }

   private void init_V_BlockPos_J(BlockPos pos, long at) {
      if (this.init_Map.size() >= 8192) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }

      this.init_Map.put(pos.asLong(), at + 30000L);
      this.init_DataUtil.init(pos, 0.0, at + 30000L);
   }

   void init_V_J(long at) {
      this.init_Map.values().removeIf(until -> until <= at);
      this.load_Map.values().removeIf(w -> w.init_J <= at);

      while (!this.init_ArrayDeque.isEmpty() && this.init_ArrayDeque.peekFirst().init_J <= at) {
         this.init_ArrayDeque.removeFirst();
      }

      this.load_ArrayDeque.removeIf(h -> h.init_J <= at);
      if (this.init_J < 0L || at - this.init_J >= 100L) {
         this.init_J = at;

         for (Entity entity : init_Minecraft.level.entitiesForRendering()) {
            if (entity != null && entity != init_Minecraft.player && !(entity.distanceToSqr(init_Minecraft.player) > 4096.0)) {
               BlockPos pos = entity.blockPosition();
               if (entity instanceof Player) {
                  this.init_DataUtil.init(pos, 8.0, at + 1500L);
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
                     this.init_ArrayDeque
                        .addLast(
                           new VanishActivityDetector.Inner_DataUtil(pos.immutable(), kind, kind == 2 ? 24.0 : (kind == 3 ? 6.0 : (kind == 4 ? 12.0 : 6.0)), at + 2500L)
                        );
                  }

                  if (this.init_ArrayDeque.size() > 2048) {
                     this.init_ArrayDeque.removeFirst();
                  }
               }
            }
         }
      }

      for (Entry<Long, VanishActivityDetector.Inner_CachePool> entry : this.load_Map.entrySet()) {
         BlockPos pos = BlockPos.of(entry.getKey());
         if (this.load(pos, at) || this.read(pos, at)) {
            this.init_DataUtil.init(pos, 0.0, at + 1000L);
         }
      }

      this.init_DataUtil
         .init(
            at,
            (posx, after) -> init_Z_BlockPos(posx) && init_Str_BlockState(init_Minecraft.level.getBlockState(posx)).equals(after),
            posx -> this.init_Z_BlockPos_J(posx, at)
         );
   }

   private boolean init_Z_BlockPos_J(BlockPos pos, long at) {
      if (at < this.load_J || this.init_Map.getOrDefault(pos.asLong(), 0L) > at || !init_Z_BlockPos(pos)) {
         return true;
      }

      if (this.load(pos, at)) {
         return true;
      }

      if (this.read(pos, at)) {
         return true;
      }

      VanishActivityDetector.Inner_CachePool focus = this.load_Map.get(pos.asLong());
      // Only the fluid/heat test can match beyond distSqr 4; skip the outer cells when no fluid/heat state can explain this block.
      int r = focus != null
            && (
               init(focus.init_BlockState, focus.init_DataUtil_CachePool, true, true)
                  || init(focus.init_BlockState, focus.init_DataUtil_CachePool, true, false)
                  || init(focus.init_BlockState, focus.init_DataUtil_CachePool, false, true)
            )
         ? 3
         : 2;

      for (int dx = -r; dx <= r; dx++) {
         for (int dy = -r; dy <= r; dy++) {
            for (int dz = -r; dz <= r; dz++) {
               BlockPos p = pos.offset(dx, dy, dz);
               if ((r == 3 || dx * dx + dy * dy + dz * dz <= 4) && init_Z_BlockPos(p)) {
                  BlockState state = init_Minecraft.level.getBlockState(p);
                  if (state.getBlock() instanceof MovingPistonBlock && p.distSqr(pos) <= 4.0) {
                     return true;
                  }

                  if (focus != null
                     && focus.init_DataUtil_CachePool == autismclient.util.DataUtil.Inner_CachePool.PLACE
                     && p.distManhattan(pos) == 1
                     && (focus.init_BlockState.getBlock() == Blocks.MELON || focus.init_BlockState.getBlock() == Blocks.PUMPKIN)
                     && (state.getBlock() instanceof StemBlock || state.getBlock() instanceof AttachedStemBlock)) {
                     return true;
                  }

                  if (focus != null
                     && init(
                        focus.init_BlockState,
                        focus.init_DataUtil_CachePool,
                        !state.getFluidState().isEmpty(),
                        state.getBlock() instanceof BaseFireBlock || state.getFluidState().is(FluidTags.LAVA)
                     )) {
                     return true;
                  }

                  if (focus != null
                     && load(focus.init_BlockState)
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
      VanishActivityDetector.Inner_CachePool focus = this.load_Map.get(pos.asLong());

      for (VanishActivityDetector.Inner_DataUtil actor : this.init_ArrayDeque) {
         if (actor.init_J > at
            && !(actor.init_BlockPos.distSqr(pos) > actor.init_D * actor.init_D)
            && (actor.init_I != 1 || focus == null || init_Z_BlockState(focus.init_BlockState))
            && (actor.init_I != 5 || focus == null || BuiltInRegistries.BLOCK.getKey(focus.init_BlockState.getBlock()).getPath().endsWith("_concrete"))
            && (
               actor.init_I != 6
                  || focus == null
                  || focus.init_BlockState.getBlock() == Blocks.IRON_BLOCK
                  || focus.init_BlockState.getBlock() == Blocks.SNOW_BLOCK
                  || focus.init_BlockState.getBlock() instanceof CarvedPumpkinBlock
            )) {
            return true;
         }
      }

      return false;
   }

   static boolean init_Z_BlockState(BlockState state) {
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
         this.init_V_UiHelper_StreamUti(new VanishActivityDetector.Inner_StreamUtil(pos.immutable(), fluid, heat, false, at + 4000L));
      }
   }

   private void init_V_UiHelper_StreamUti(VanishActivityDetector.Inner_StreamUtil hazard) {
      if (this.load_ArrayDeque.size() >= 1024) {
         this.load_ArrayDeque.removeFirst();
      }

      this.load_ArrayDeque.addLast(hazard);
   }

   private boolean read(BlockPos pos, long at) {
      VanishActivityDetector.Inner_CachePool focus = this.load_Map.get(pos.asLong());
      if (focus == null) {
         return false;
      }

      for (VanishActivityDetector.Inner_StreamUtil h : this.load_ArrayDeque) {
         if (h.init_J > at
            && h.init_BlockPos.distSqr(pos) <= (h.read ? 0 : 12)
            && (h.read ? load(focus.init_BlockState) : init(focus.init_BlockState, focus.init_DataUtil_CachePool, h.init_Z, h.load))) {
            return true;
         }
      }

      return false;
   }

   static boolean load(BlockState state) {
      return state.getBlock() instanceof CarvedPumpkinBlock || state.getBlock() instanceof ShulkerBoxBlock || state.getBlock() instanceof TntBlock;
   }

   static boolean init(BlockState state, autismclient.util.DataUtil.Inner_CachePool kind, boolean fluid, boolean heat) {
      if (kind == autismclient.util.DataUtil.Inner_CachePool.BREAK) {
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

   private static boolean init_Z_BlockPos(BlockPos p) {
      return init_Minecraft.level != null && init_Minecraft.level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, ChunkStatus.FULL, false) != null;
   }

   private static String init_Str_BlockState(BlockState state) {
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
   }

   List<autismclient.util.DataUtil.Inner_BufferHelper> init_List_J(long at) {
      return this.init_DataUtil.init_List_J(at);
   }

   void init() {
      this.init_DataUtil.init_V();
      this.init_Map.clear();
      this.load_Map.clear();
      this.init_ArrayDeque.clear();
      this.load_ArrayDeque.clear();
      this.init_J = -1L;
      this.load_J = 0L;
   }

   private record Inner_CachePool(BlockState init_BlockState, autismclient.util.DataUtil.Inner_CachePool init_DataUtil_CachePool, long init_J) {
   }

   private record Inner_DataUtil(BlockPos init_BlockPos, int init_I, double init_D, long init_J) {
   }

   private record Inner_StreamUtil(BlockPos init_BlockPos, boolean init_Z, boolean load, boolean read, long init_J) {
   }
}
