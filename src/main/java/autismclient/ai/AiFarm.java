package autismclient.ai;

import autismclient.ai.a.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiFarm {
   private static final int init_I = 10;
   private static final int load_I = 40;
   private static final double init_D = 16.0;
   private static final Map<String, String> init_Map = new LinkedHashMap<>();
   private static final Map<String, String> load_Map = new LinkedHashMap<>();
   private static final Map<String, String> read_Map = Map.ofEntries(
      Map.entry("minecraft:wheat", "minecraft:wheat_seeds"),
      Map.entry("minecraft:carrots", "minecraft:carrot"),
      Map.entry("minecraft:potatoes", "minecraft:potato"),
      Map.entry("minecraft:beetroots", "minecraft:beetroot_seeds"),
      Map.entry("minecraft:nether_wart", "minecraft:nether_wart"),
      Map.entry("minecraft:cocoa", "minecraft:cocoa_beans")
   );
   private static final Map<String, List<String>> write_Map = Map.ofEntries(
      Map.entry("minecraft:cow", List.of("minecraft:wheat")),
      Map.entry("minecraft:mooshroom", List.of("minecraft:wheat")),
      Map.entry("minecraft:sheep", List.of("minecraft:wheat")),
      Map.entry("minecraft:goat", List.of("minecraft:wheat")),
      Map.entry("minecraft:pig", List.of("minecraft:carrot", "minecraft:potato", "minecraft:beetroot")),
      Map.entry("minecraft:chicken", List.of("minecraft:wheat_seeds", "minecraft:beetroot_seeds", "minecraft:melon_seeds", "minecraft:pumpkin_seeds")),
      Map.entry("minecraft:rabbit", List.of("minecraft:carrot", "minecraft:golden_carrot", "minecraft:dandelion")),
      Map.entry("minecraft:wolf", List.of("minecraft:beef", "minecraft:porkchop", "minecraft:mutton")),
      Map.entry("minecraft:cat", List.of("minecraft:cod", "minecraft:salmon")),
      Map.entry("minecraft:horse", List.of("minecraft:golden_apple", "minecraft:golden_carrot")),
      Map.entry("minecraft:llama", List.of("minecraft:hay_block")),
      Map.entry("minecraft:turtle", List.of("minecraft:seagrass")),
      Map.entry("minecraft:panda", List.of("minecraft:bamboo")),
      Map.entry("minecraft:fox", List.of("minecraft:sweet_berries", "minecraft:glow_berries")),
      Map.entry("minecraft:bee", List.of("minecraft:dandelion", "minecraft:poppy", "minecraft:cornflower")),
      Map.entry("minecraft:frog", List.of("minecraft:slime_ball")),
      Map.entry("minecraft:camel", List.of("minecraft:cactus")),
      Map.entry("minecraft:armadillo", List.of("minecraft:spider_eye")),
      Map.entry("minecraft:sniffer", List.of("minecraft:torchflower_seeds"))
   );
   private static final int read_I = 4096;
   private static final long init_J = 750000L;
   private static final int write_I = 32;
   private static final int update = 16;
   private static final int sync = 3;
   private static final int flush = 32;
   private static final int close = 1200;
   private static final int open = 100;
   private static final int poll = 16;
   private static final int push = 40;
   private static final double load_D = 64.0;
   private static final double read_D = 2.25;
   private static final String init_Str = "minecraft:bone_meal";
   private static final int[][] init_IArr2 = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {0, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
   static final Set<String> init_Set = Set.of("minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots", "minecraft:nether_wart");

   private AiFarm() {
   }

   static AiOperation init(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         AiFarm.Inner_DataUtil action = init_B_DataUtil(AiJson.init_Str_JsonObject(args, "action", "harvest"));
         if (action == AiFarm.Inner_DataUtil.init) {
            String spec = AiJson.init_Str_JsonObject(args, "crop", AiJson.init_Str_JsonObject(args, "item", "minecraft:wheat"));
            String crop = init_Str(spec);
            return init_Set.contains(crop) ? load(args, crop, init_Map.get(crop)) : init(args, crop, init_Map.get(crop));
         } else if (action == AiFarm.Inner_DataUtil.read) {
            String crop = init_Str(AiJson.init_Str_JsonObject(args, "crop", AiJson.init_Str_JsonObject(args, "item", "minecraft:wheat")));
            return AiFarmPlant.init(args, crop, load(crop));
         } else if (action == AiFarm.Inner_DataUtil.write) {
            String crop = init_Str(AiJson.init_Str_JsonObject(args, "crop", AiJson.init_Str_JsonObject(args, "item", "minecraft:wheat")));
            return AiFarmBuild.init(args, crop, load(crop));
         } else {
            return load(args);
         }
      } else {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }
   }

   static String init_Str(String spec) {
      String clean = spec.trim().toLowerCase(Locale.ROOT);
      String full = clean.contains(":") ? clean : "minecraft:" + clean;
      if (init_Map.containsKey(full)) {
         return full;
      } else if (load_Map.containsKey(full)) {
         return load_Map.get(full);
      } else {
         throw new IllegalArgumentException("'" + spec + "' is not a farmable crop; known crops: " + String.join(", ", init_Map.keySet()));
      }
   }

   static AiFarm.Inner_StreamUtil init(Block block) {
      if (block == Blocks.SWEET_BERRY_BUSH) {
         return AiFarm.Inner_StreamUtil.read;
      } else if (autismclient.util.FarmCropCatalog.load(block)) {
         return AiFarm.Inner_StreamUtil.write;
      } else {
         return autismclient.util.FarmCropCatalog.read(block) ? AiFarm.Inner_StreamUtil.load : AiFarm.Inner_StreamUtil.init;
      }
   }

   static String load(String cropBlockId) {
      return read_Map.get(cropBlockId);
   }

   static int init_I(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
            return state.getValue(age);
         }
      }

      return -1;
   }

   static int load_I(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
            return Collections.max(age.getPossibleValues());
         }
      }

      return -1;
   }

   static boolean init_Z(BlockState state) {
      int max = load_I(state);
      return max < 0 || init_I(state) >= max;
   }

   static boolean load_Z(BlockState state) {
      int max = load_I(state);
      return state.getBlock() instanceof BonemealableBlock && max >= 0 && init_I(state) < max;
   }

   static BlockPos init(Function<BlockPos, BlockState> at, BlockPos pos) {
      BlockState state = at.apply(pos);
      if (state != null && autismclient.util.FarmCropCatalog.load(state.getBlock())) {
         BlockPos base = pos;

         while (at.apply(base.below()) != null && at.apply(base.below()).is(state.getBlock())) {
            base = base.below();
         }

         BlockPos segment = base.above();
         return at.apply(segment) != null && at.apply(segment).is(state.getBlock()) ? segment : null;
      } else {
         return null;
      }
   }

   static Direction init_Direction(BlockState state) {
      return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
   }

   static AiOperation init(final JsonObject args, final String cropBlock, final String cropItem) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final Block crop = BuiltInRegistries.BLOCK
            .getOptional(Identifier.parse(cropBlock))
            .orElseThrow(() -> new IllegalArgumentException("Unknown crop block " + cropBlock));
         final AiFarm.Inner_StreamUtil kind = init(crop);
         final String seed = load(cropBlock);
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final BlockPos init_BlockPos = this.init_Minecraft.player.blockPosition();
            final int init_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
            final int load_I = AiGameState.init_I_Str(cropItem);
            final int read_I = AiJson.init_I_JsonObject(args, "count", 53) + (AiJson.init_Z_JsonObject(args, "additional", false) ? this.load_I : 0);
            final boolean init_Z = AiJson.init_Z_JsonObject(args, "exact", false);
            final boolean load_Z = AiJson.init_Z_JsonObject(args, "bonemeal", false);
            final boolean read_Z = AiJson.init_Z_JsonObject(args, "replant", true);
            final long init_J = System.nanoTime() + Math.max(1, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 600))) * 1000000000L;
            final int write_I = 2;
            final int update_I = 5;
            final int sync = 25;
            final int flush = Math.max(this.init_Minecraft.level.getMinY(), this.init_BlockPos.getY() - 16);
            final int close = Math.min(this.init_Minecraft.level.getMaxY() - 1, this.init_BlockPos.getY() + 16);
            final List<AiFarm.Inner_CachePool> init_List = new ArrayList<>();
            final List<AiFarm.Inner_CachePool> load_List = new ArrayList<>();
            final ArrayDeque<AiFarm.Inner_CachePool> init_ArrayDeque = new ArrayDeque<>();
            final Set<Long> init_Set = new LinkedHashSet<>();
            final List<JsonObject> read_List = new ArrayList<>();
            final Map<String, Integer> init_Map = new HashMap<>();
            final Map<Long, Integer> load_Map = new HashMap<>();
            final Map<Long, Long> read_Map = new HashMap<>();
            long load_J = AiServerEvidence.init_J();
            BlockPos load_BlockPos = this.init_BlockPos;
            AiFarm.Inner_CachePool init_B_CachePool;
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            BlockPos read_BlockPos;
            Direction init_Direction;
            final autismclient.ai.a.AiAim init_DataUtil = new autismclient.ai.a.AiAim();
            BlockPos write_BlockPos;
            double init_D;
            double load_D = -1.0;
            int open;
            final Deque<AiFarm.Inner_CachePool> init_Deque = new ArrayDeque<>();
            final List<AiOperation> write_List = new ArrayList<>();
            final List<AiFarm.Inner_CachePool> update_List = new ArrayList<>();
            final IdentityHashMap<LevelChunkSection, Boolean> init_IdentityHashMap = new IdentityHashMap<>();
            long read_J = -1L;
            int poll;
            int push;
            int pop;
            int tick;
            int run;
            int get;
            int set;
            int put;
            int add = this.load_I;
            int clear;
            int reset;
            int start;
            int stop;
            int next;
            int prev;
            int valid;
            int size;
            int data;
            int value;
            boolean write_Z;
            boolean update_Z;
            String init_Str = "";

            @Override
            public AiOperation.Result tick() {
               if (this.update_Z || this.init_Minecraft.player == null || this.init_Minecraft.level == null) {
                  return this.init(AiOperation.Status.CANCELLED, "Harvest cancelled");
               }

               if (this.read_I >= 0 && this.read_I <= 2304) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Harvest target not reached before deadline");
                  }

                  if (this.init_Minecraft.gui.screen() != null) {
                     autismclient.util.BlockAttackGraceWindow.init();
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     this.init_DataUtil.init_V();
                     return this.read("Harvest paused while a screen is open");
                  }

                  if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.close()) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        AiGuard.init_Str_Str("harvesting", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
                     );
                  }

                  int count = AiGameState.init_I_Str(cropItem);

                  for (int i = this.write_List.size() - 1; i >= 0; i--) {
                     AiOperation.Result v = this.write_List.get(i).tick();
                     if (v.terminal()) {
                        this.write_List.remove(i);
                        AiFarm.Inner_CachePool vw = this.update_List.remove(i);
                        if (v.status() == AiOperation.Status.SUCCEEDED) {
                           this.next++;
                        } else {
                           this.valid++;
                           this.init(vw.cell(), v.message());
                        }
                     }
                  }

                  boolean owesReplant = this.init_B_CachePool != null
                        && (this.init_B_CachePool.kind() == AiFarm.Inner_BufferHelper.init || this.init_B_CachePool.kind() == AiFarm.Inner_BufferHelper.load)
                     || this.init_ArrayDeque.stream().anyMatch(w -> w.kind() == AiFarm.Inner_BufferHelper.load);
                  if (count < this.read_I || owesReplant) {
                     this.start = 0;
                     this.reset = 0;
                     this.set = 0;
                     if (count < this.read_I && !this.init_Z(cropItem)) {
                        return this.init(AiOperation.Status.FAILED, "Inventory full: make room for " + cropItem + " before harvesting");
                     }

                     if (count > this.add) {
                        this.clear = 0;
                     }

                     this.add = count;
                     if (!this.write_Z) {
                        return this.init_AiOperation_Result();
                     }

                     if (this.init_AiOperation != null) {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return this.read("Walking to the next plant");
                        }

                        this.init_AiOperation = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           return this.load("no reachable approach");
                        }

                        if (this.init_B_CachePool != null && this.init_B_CachePool.kind() != AiFarm.Inner_BufferHelper.write) {
                           BlockPos cell = this.init_B_CachePool.kind() == AiFarm.Inner_BufferHelper.load
                              ? this.init_B_CachePool.base()
                              : this.init_B_CachePool.cell();
                           if (!this.load(cell) || !this.load(this.init_B_CachePool)) {
                              return this.load("no clear line of sight after approach");
                           }
                        }

                        return this.read("Approach finished; re-evaluating");
                     } else {
                        if (this.write_BlockPos != null) {
                           return this.load_AiOperation_Result();
                        }

                        if (this.load_AiOperation != null) {
                           AiOperation.Result r = this.load_AiOperation.tick();
                           if (!r.terminal()) {
                              if (this.init_B_CachePool == null
                                 || this.init_B_CachePool.kind() != AiFarm.Inner_BufferHelper.load
                                 || !(this.load_AiOperation instanceof AiActedOperation actedOp)
                                 || !actedOp.init()) {
                                 return this.read(this.init_Str());
                              }

                              this.write_List.add(this.load_AiOperation);
                              this.update_List.add(this.init_B_CachePool);
                              this.load_AiOperation = null;
                              this.init_B_CachePool = null;
                              this.tick = 0;
                              this.run = 0;
                           } else {
                              this.load_AiOperation.cancel();
                              this.load_AiOperation = null;
                              AiOperation.Result settled = this.init(this.init_B_CachePool, r);
                              if (settled.terminal()) {
                                 return settled;
                              }
                           }
                        }

                        if (this.init_B_CachePool == null || !this.init_Z(this.init_B_CachePool)) {
                           if (this.init_B_CachePool != null && this.init_B_CachePool.kind() == AiFarm.Inner_BufferHelper.write) {
                              this.init_V(this.init_B_CachePool);
                           }

                           this.init_B_CachePool = null;
                           this.tick = 0;
                           this.run = 0;
                           this.init_B_CachePool = this.init_B_CachePool();
                           if (!this.init_Str.isEmpty()) {
                              return this.init(AiOperation.Status.FAILED, this.init_Str);
                           }
                        }

                        if (this.init_B_CachePool != null) {
                           this.clear = 0;
                           this.pop = 0;
                           return this.init_AiOperation_Result(this.init_B_CachePool);
                        }

                        if (!this.write_List.isEmpty()) {
                           return this.read("Confirming the last replant");
                        }

                        if (++this.pop >= 100) {
                           this.init_V();
                        }

                        return ++this.clear > 1200
                           ? this.init(
                              AiOperation.Status.FAILED, "Nothing harvested for 60 seconds; locate reachable mature " + cropBlock + " or free inventory space"
                           )
                           : this.read(this.stop == 0 ? "Scanning for " + cropBlock : "Waiting for regrowth");
                     }
                  } else if (!this.write_List.isEmpty()) {
                     return ++this.set > 200
                        ? this.init(AiOperation.Status.UNCERTAIN, "The last replant is still unconfirmed; inspect the field before repeating")
                        : this.read("Confirming the last replants");
                  } else if (count > this.load_I && AiServerEvidence.init_J() <= this.load_J) {
                     return ++this.start > 200
                        ? this.init(
                           AiOperation.Status.UNCERTAIN, "Harvest target observed locally, but server confirmation not received; inspect before repeating"
                        )
                        : this.read("Verifying harvested inventory after server updates");
                  } else if (count > this.load_I && ++this.reset < 5) {
                     return this.read("Verifying harvested inventory after server updates");
                  } else {
                     return this.valid > 0
                        ? this.init(
                           AiOperation.Status.FAILED, "Harvested the target, but " + this.valid + " crops could not be replanted; inspect the reported cells"
                        )
                        : this.init(
                           this.init_Z && count != this.read_I ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
                           this.init_Z && count != this.read_I
                              ? "Surplus retained; exact target requires storage reconciliation"
                              : "Harvest inventory target reached"
                        );
                  }
               } else {
                  return this.init(AiOperation.Status.FAILED, "Harvest target must fit player inventory");
               }
            }

            boolean init_Z(String itemId) {
               AiItems.Inner_DataUtil wanted = AiItems.load_af_DataUtil(itemId);
               Inventory inventory = this.init_Minecraft.player.getInventory();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = inventory.getItem(slot);
                  if (stack.isEmpty() || wanted.matches(stack) && stack.getCount() < stack.getMaxStackSize()) {
                     return true;
                  }
               }

               return false;
            }

            AiOperation.Result init_AiOperation_Result() {
               long revision = autismclient.ai.a.AiGridRevisions.init_J();
               if (this.read_J != revision) {
                  this.init_IdentityHashMap.clear();
                  this.read_J = revision;
               }

               long stop = System.nanoTime() + 750000L;
               int height = this.close - this.flush + 1;

               for (int checked = 0; checked < 4096 && this.poll < 25; checked++) {
                  if ((checked & 63) == 0 && System.nanoTime() >= stop) {
                     return this.read("Scanning for " + cropBlock);
                  }

                  int cx = (this.load_BlockPos.getX() >> 4) - 2 + this.poll % 5;
                  int cz = (this.load_BlockPos.getZ() >> 4) - 2 + this.poll / 5;
                  LevelChunk chunk = this.init_Minecraft.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (chunk != null && this.push < 256 * height) {
                     int column = this.push / height;
                     int y = this.flush + this.push % height;
                     int x = (cx << 4) + column / 16;
                     int z = (cz << 4) + column % 16;
                     if (Math.abs(x - this.load_BlockPos.getX()) <= 32 && Math.abs(z - this.load_BlockPos.getZ()) <= 32) {
                        LevelChunkSection section = chunk.getSections()[this.init_Minecraft.level.getSectionIndex(y)];
                        boolean contains = this.init_IdentityHashMap
                           .computeIfAbsent(section, part -> !part.hasOnlyAir() && part.maybeHas(statex -> statex.is(crop)));
                        if (!contains) {
                           this.push = this.push + (Math.min(this.close + 1, (y >> 4) + 1 << 4) - y);
                        } else {
                           this.push++;
                           BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                           if (state.is(crop)) {
                              BlockPos cell = new BlockPos(x, y, z);
                              if (!this.init_Set.contains(cell.asLong()) && !this.init_Z(cell)) {
                                 if (kind == AiFarm.Inner_StreamUtil.write) {
                                    BlockPos segment = AiFarm.init(pos -> this.init_Minecraft.level.getBlockState(pos), cell);
                                    if (segment != null && !this.init_Set.contains(segment.asLong()) && !this.init_Z(segment)) {
                                       this.init_List
                                          .add(AiFarm.Inner_CachePool.harvest(segment, this.init_Minecraft.player.distanceToSqr(Vec3.atCenterOf(segment))));
                                    }
                                 } else if (AiFarm.init_Z(state)) {
                                    this.init_List.add(AiFarm.Inner_CachePool.harvest(cell, this.init_Minecraft.player.distanceToSqr(Vec3.atCenterOf(cell))));
                                 } else if (this.load_Z && AiFarm.load_Z(state)) {
                                    this.load_List.add(AiFarm.Inner_CachePool.bonemeal(cell, this.init_Minecraft.player.distanceToSqr(Vec3.atCenterOf(cell))));
                                 }
                              }
                           }
                        }
                     } else {
                        this.push = (column + 1) * height;
                     }
                  } else {
                     this.poll++;
                     this.push = 0;
                  }
               }

               if (this.poll < 25) {
                  return this.read("Scanning for " + cropBlock);
               }

               this.write_Z = true;
               Comparator<AiFarm.Inner_CachePool> nearest = Comparator.comparingDouble(AiFarm.Inner_CachePool::distanceSqr);
               this.init_List.sort(nearest);
               this.load_List.sort(nearest);
               return this.read(
                  "Found " + this.init_List.size() + " mature " + cropBlock + (this.load_List.isEmpty() ? "" : " and " + this.load_List.size() + " growing")
               );
            }

            void init_V() {
               this.load_BlockPos = this.init_Minecraft.player.blockPosition();
               this.poll = 0;
               this.push = 0;
               this.pop = 0;
               this.init_List.clear();
               this.load_List.clear();
               this.write_Z = false;
            }

            boolean init_Z(BlockPos cell) {
               for (AiFarm.Inner_CachePool w : this.init_List) {
                  if (w.cell().equals(cell)) {
                     return true;
                  }
               }

               for (AiFarm.Inner_CachePool w : this.load_List) {
                  if (w.cell().equals(cell)) {
                     return true;
                  }
               }

               return this.init_B_CachePool != null && this.init_B_CachePool.cell().equals(cell);
            }

            boolean init_Z(AiFarm.Inner_CachePool w) {
               switch (w.kind()) {
                  case init:
                     if (!AiTerrain.init(this.init_Minecraft.level, w.cell())) {
                        return false;
                     }

                     BlockState state = this.init_Minecraft.level.getBlockState(w.cell());
                     return kind == AiFarm.Inner_StreamUtil.write ? state.is(crop) : state.is(crop) && AiFarm.init_Z(state);
                  case load:
                     return seed != null && this.init_Minecraft.level.getBlockState(w.cell()).isAir();
                  case read:
                     return AiTerrain.init(this.init_Minecraft.level, w.cell())
                        && AiFarm.load_Z(this.init_Minecraft.level.getBlockState(w.cell()))
                        && AiGameState.init_I_Str("minecraft:bone_meal") > 0;
                  case write:
                     return this.init_Minecraft.level.getEntity(w.entityId()) instanceof ItemEntity drop && drop.isAlive();
                  default:
                     return false;
               }
            }

            AiFarm.Inner_CachePool init_B_CachePool() {
               int queued = this.init_ArrayDeque.size();
               boolean enough = AiGameState.init_I_Str(cropItem) >= this.read_I;
               boolean needsSeed = seed != null
                  && AiGameState.init_I_Str(seed) == 0
                  && this.init_ArrayDeque.stream().anyMatch(wx -> wx.kind() == AiFarm.Inner_BufferHelper.load);

               while (queued-- > 0) {
                  AiFarm.Inner_CachePool w = this.init_ArrayDeque.poll();
                  if (w.kind() == AiFarm.Inner_BufferHelper.load) {
                     AiServerEvidence.Inner_DataUtil update = AiServerEvidence.init_ca_DataUtil_BlockPos(w.cell());
                     if (AiReplantEvidence.init(
                           this.init_Minecraft.level.getBlockState(w.cell()),
                           update,
                           this.read_Map.getOrDefault(w.cell().asLong(), 0L),
                           crop,
                           false,
                           AiGameState.init_I_Str(seed)
                        )
                        == AiReplantEvidence.Inner_DataUtil.read) {
                        this.prev++;
                        this.load_Map.remove(w.cell().asLong());
                        this.read_Map.remove(w.cell().asLong());
                        continue;
                     }

                     if (AiGameState.init_I_Str(seed) <= 0) {
                        if (this.init_Minecraft.player.tickCount - this.load_Map.getOrDefault(w.cell().asLong(), this.init_Minecraft.player.tickCount) > 120) {
                           this.init_Str = "No " + seed + " to finish the replant at " + w.cell().toShortString();
                           return null;
                        }

                        this.init_ArrayDeque.addLast(w);
                        continue;
                     }

                     if (this.init_Minecraft.player.tickCount < this.load_Map.getOrDefault(w.cell().asLong(), 0)
                        || update == null
                        || update.revision() <= this.read_Map.getOrDefault(w.cell().asLong(), 0L)
                        || !update.state().isAir()) {
                        this.init_ArrayDeque.addLast(w);
                        continue;
                     }
                  }

                  if (w.kind() != AiFarm.Inner_BufferHelper.write || enough || needsSeed || this.init_List.isEmpty() && (!this.load_Z || this.load_List.isEmpty())) {
                     if (this.init_Z(w)) {
                        return w;
                     }

                     if (w.kind() == AiFarm.Inner_BufferHelper.load) {
                        this.valid++;
                     }

                     if (w.kind() == AiFarm.Inner_BufferHelper.write) {
                        this.init_V(w);
                     }
                  } else {
                     this.init_Deque.addLast(w);
                  }
               }

               while (!enough && !this.init_List.isEmpty()) {
                  AiFarm.Inner_CachePool w = this.init(this.init_List);
                  if (!this.init_Set.contains(w.cell().asLong()) && this.init_Z(w)) {
                     return w;
                  }
               }

               if (this.load_Z && !enough) {
                  while (!this.load_List.isEmpty()) {
                     AiFarm.Inner_CachePool w = this.init(this.load_List);
                     if (!this.init_Set.contains(w.cell().asLong()) && this.init_Z(w)) {
                        return w;
                     }
                  }
               }

               while (!this.init_Deque.isEmpty()) {
                  AiFarm.Inner_CachePool w = this.init_Deque.poll();
                  if (this.init_Z(w)) {
                     return w;
                  }

                  this.init_V(w);
               }

               return null;
            }

            AiFarm.Inner_CachePool init(List<AiFarm.Inner_CachePool> works) {
               int best = -1;
               double bestSqr = 1.7976931348623157E308;

               for (int i = 0; i < works.size(); i++) {
                  double d = this.init_Minecraft.player.distanceToSqr(Vec3.atCenterOf(works.get(i).cell()));
                  if (d < bestSqr) {
                     bestSqr = d;
                     best = i;
                  }
               }

               return works.remove(best);
            }

            AiOperation.Result init_AiOperation_Result(AiFarm.Inner_CachePool w) {
               BlockPos cell = w.kind() == AiFarm.Inner_BufferHelper.load ? w.base() : w.cell();
               if (w.kind() == AiFarm.Inner_BufferHelper.write) {
                  Entity entity = this.init_Minecraft.level.getEntity(w.entityId());
                  if (!(entity instanceof ItemEntity)) {
                     this.init_V(w);
                     return this.read();
                  }

                  if (this.init_Minecraft.player.distanceToSqr(entity) <= 2.25) {
                     if (++this.get > 40) {
                        this.init_V(w);
                        return this.read();
                     } else {
                        return this.read("Waiting for drop pickup; check inventory space");
                     }
                  } else {
                     this.get = 0;
                     return this.init(AiGoto.init_BlockPos(BlockPos.containing(entity.position().add(0.0, 0.2, 0.0)), 24), 0.7, 45, "Sweeping up the drops");
                  }
               } else if (this.load(cell) && this.load(w)) {
                  switch (w.kind()) {
                     case init:
                        this.read_Map.put(w.cell().asLong(), AiServerEvidence.init_J_BlockPos(w.cell()));
                        if (this.read_Z && kind == AiFarm.Inner_StreamUtil.init && seed != null) {
                           BlockState before = this.init_Minecraft.level.getBlockState(w.cell());
                           if (crop == Blocks.COCOA) {
                              Direction facing = AiFarm.init_Direction(before);
                              this.read_BlockPos = w.cell().relative(facing);
                              this.init_Direction = facing.getOpposite();
                           } else {
                              this.read_BlockPos = w.cell().below();
                              this.init_Direction = Direction.UP;
                           }
                        }

                        if (kind == AiFarm.Inner_StreamUtil.read) {
                           this.load_AiOperation = AiGameOperations.init("use", this.init_JsonObject(w.cell()));
                           return this.read("Picking " + w.cell().toShortString());
                        } else {
                           this.load_AiOperation = AiGameOperations.init("break", this.init_JsonObject(w.cell()));
                           AiOperation.Result first = this.load_AiOperation.tick();
                           if (!first.terminal()) {
                              return this.read("Breaking " + w.cell().toShortString());
                           }

                           this.load_AiOperation.cancel();
                           this.load_AiOperation = null;
                           return this.init(this.init_B_CachePool, first);
                        }
                     case load:
                        int slot = AiItems.read(AiItems.load_af_DataUtil(seed));
                        if (slot < 0) {
                           if (AiGameState.init_I_Str(seed) <= 0) {
                              this.init_ArrayDeque.addLast(w);
                              this.init_B_CachePool = null;
                              this.put = 0;
                              return this.read("Picking up seed for the unfinished replant");
                           } else {
                              if (++this.put > 80) {
                                 return this.init(
                                    AiOperation.Status.FAILED, "The seed hand did not settle; the replant remains unfinished at " + w.cell().toShortString()
                                 );
                              }

                              return this.read("Preparing the seed hand");
                           }
                        } else {
                           this.put = 0;
                           JsonObject placeArgs = this.init_JsonObject(w.base());
                           placeArgs.addProperty("face", w.face().name());
                           placeArgs.addProperty("slot", slot);
                           this.load_AiOperation = AiGameOperations.init("place", placeArgs);
                           AiOperation.Result opened = this.load_AiOperation.tick();
                           if (!opened.terminal()) {
                              return this.read("Replanting " + w.cell().toShortString());
                           }

                           this.load_AiOperation.cancel();
                           this.load_AiOperation = null;
                           return this.init(this.init_B_CachePool, opened);
                        }
                     case read:
                        int mealSlot = AiItems.read(AiItems.load_af_DataUtil("minecraft:bone_meal"));
                        if (mealSlot < 0) {
                           return this.read();
                        }

                        JsonObject useArgs = this.init_JsonObject(w.cell());
                        useArgs.addProperty("slot", mealSlot);
                        useArgs.addProperty("aimY", this.init_D(w.cell()));
                        this.load_AiOperation = AiGameOperations.init("use", useArgs);
                        return this.read("Feeding " + w.cell().toShortString());
                     default:
                        return this.read();
                  }
               } else {
                  if (w.kind() == AiFarm.Inner_BufferHelper.load) {
                     return this.init(AiGoto.init_BlockPos(w.cell(), 24), 0.45, 30, "Lining up the replant face");
                  }

                  int[] column = AiFarm.init_IArr2[this.run++ % AiFarm.init_IArr2.length];
                  BlockPos stance = AiGoto.init_BlockPos(new BlockPos(cell.getX() + column[0], cell.getY() - 1, cell.getZ() + column[1]), 24);
                  return this.init(stance, 2.0, 60, "Moving into reach of " + cell.toShortString());
               }
            }

            AiOperation.Result init(BlockPos stance, double range, int timeoutSeconds, String message) {
               if (this.write_BlockPos == null && this.init(this.init_Minecraft.player.blockPosition(), stance)) {
                  this.write_BlockPos = stance;
                  this.init_D = range;
                  this.open = 0;
                  this.load_D = -1.0;
                  return this.read(message);
               } else {
                  this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(stance, range, false, false, false, timeoutSeconds);
                  return this.read(message);
               }
            }

            AiOperation.Result load_AiOperation_Result() {
               Vec3 center = Vec3.atBottomCenterOf(this.write_BlockPos);
               double distSqr = this.init_Minecraft.player.distanceToSqr(center);
               if (distSqr <= Math.max(0.16, this.init_D * this.init_D)) {
                  this.load_V();
                  return this.read("Arrived; re-evaluating");
               }

               BlockPos feet = this.init_Minecraft.player.blockPosition();
               int sx = Integer.signum(this.write_BlockPos.getX() - feet.getX());
               int sz = Integer.signum(this.write_BlockPos.getZ() - feet.getZ());
               boolean blocked = (sx != 0 || sz != 0) && !AiGoto.init_Z(feet.offset(sx, 0, sz));
               if (this.load_D >= 0.0 && distSqr > this.load_D - 0.004) {
                  if (++this.open > 40) {
                     blocked = true;
                  }
               } else {
                  this.open = 0;
               }

               if (blocked) {
                  this.load_V();
                  return this.read("Direct walk blocked; pathing around");
               } else {
                  this.load_D = distSqr;
                  this.init_DataUtil.init_V_Vec3(center);
                  this.init_DataUtil.load_V();
                  this.init_Minecraft.options.keyUp.setDown(true);
                  this.init_Minecraft.player.setSprinting(distSqr > 9.0);
                  return this.read("Walking straight to the next plant");
               }
            }

            void load_V() {
               this.write_BlockPos = null;
               this.init_Minecraft.options.keyUp.setDown(false);
               if (this.init_Minecraft.player != null) {
                  this.init_Minecraft.player.setSprinting(false);
               }
            }

            boolean init(BlockPos from, BlockPos stance) {
               if (from.getY() != stance.getY()) {
                  return false;
               }

               int dx = stance.getX() - from.getX();
               int dz = stance.getZ() - from.getZ();
               int ax = Math.abs(dx);
               int az = Math.abs(dz);
               if (Math.max(ax, az) <= 8 && (ax == 0 || az == 0 || ax == az)) {
                  int x = from.getX();
                  int z = from.getZ();
                  int i = 0;

                  for (int steps = Math.max(ax, az); i < steps; i++) {
                     x += Integer.signum(dx);
                     z += Integer.signum(dz);
                     if (!AiGoto.init_Z(new BlockPos(x, from.getY(), z))) {
                        return false;
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            }

            AiOperation.Result init(AiFarm.Inner_CachePool w, AiOperation.Result r) {
               if (w == null) {
                  return this.read("Re-evaluating the field");
               }

               switch (w.kind()) {
                  case init:
                     if (r.status() == AiOperation.Status.SUCCEEDED
                        || r.status() == AiOperation.Status.TIMED_OUT && !this.init_Minecraft.level.getBlockState(w.cell()).is(crop)) {
                        this.stop++;
                        this.init_V(w.cell());
                        if (kind == AiFarm.Inner_StreamUtil.init && seed != null && this.read_BlockPos != null) {
                           ClientPacketListener connection = this.init_Minecraft.getConnection();
                           PlayerInfo info = connection == null ? null : connection.getPlayerInfo(this.init_Minecraft.player.getUUID());
                           this.load_Map
                              .put(w.cell().asLong(), this.init_Minecraft.player.tickCount + autismclient.util.BlockBreakGraceCalc.init(info == null ? 0 : info.getLatency()));
                           this.init_ArrayDeque.addFirst(AiFarm.Inner_CachePool.replant(w.cell(), this.read_BlockPos, this.init_Direction));
                        }

                        this.read_BlockPos = null;
                        this.init_Direction = null;
                        return this.read();
                     } else {
                        this.read_BlockPos = null;
                        this.init_Direction = null;
                        return this.load(r.message());
                     }
                  case load:
                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.next++;
                        return this.read();
                     }

                     return this.init_AiOperation_Result(r.message());
                  case read:
                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.data++;
                        return this.read();
                     }

                     return this.load(r.message());
                  default:
                     return this.read();
               }
            }

            AiOperation.Result read() {
               this.init_B_CachePool = null;
               this.tick = 0;
               this.run = 0;
               this.get = 0;
               this.put = 0;
               return this.read("Harvested " + this.stop + ", " + cropItem + " " + this.add + "/" + this.read_I);
            }

            AiOperation.Result init_AiOperation_Result(String reason) {
               this.valid++;
               if (this.init_B_CachePool != null) {
                  this.init(this.init_B_CachePool.cell(), reason);
               }

               return this.read();
            }

            AiOperation.Result load(String reason) {
               if (this.init_B_CachePool == null) {
                  return this.read("Re-evaluating the field");
               }

               if (++this.tick >= 3) {
                  if (this.init_B_CachePool.kind() == AiFarm.Inner_BufferHelper.load) {
                     this.valid++;
                  }

                  this.init(this.init_B_CachePool.cell(), reason);
                  this.init_Set.add(this.init_B_CachePool.cell().asLong());
                  return this.read();
               } else {
                  return this.read("Attempt " + this.tick + " failed: " + reason);
               }
            }

            void init(BlockPos cell, String reason) {
               if (this.read_List.size() < 32) {
                  this.read_List.add(AiJson.init_JsonObject_ObjArr("x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "reason", reason));
               }

               this.value++;
            }

            void init_V(BlockPos cell) {
               Vec3 center = Vec3.atCenterOf(cell);
               List<AiFarm.Inner_CachePool> found = new ArrayList<>();

               for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item && item.isAlive() && !(item.position().distanceToSqr(center) > 64.0)) {
                     if (found.size() >= 16) {
                        break;
                     }

                     String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                     if (id.equals(cropItem) || seed != null && id.equals(seed)) {
                        this.init_Map.putIfAbsent(id, AiGameState.init_I_Str(id));
                        found.add(AiFarm.Inner_CachePool.drop(item.getId(), id, item.blockPosition(), this.init_Minecraft.player.distanceToSqr(item)));
                     }
                  }
               }

               found.sort(Comparator.comparingDouble(AiFarm.Inner_CachePool::distanceSqr));

               for (AiFarm.Inner_CachePool drop : found) {
                  this.init_ArrayDeque.addLast(drop);
               }
            }

            void init_V(AiFarm.Inner_CachePool w) {
               int now = AiGameState.init_I_Str(w.item());
               int base = this.init_Map.getOrDefault(w.item(), now);
               if (now > base) {
                  this.size += now - base;
               }

               this.init_Map.put(w.item(), now);
            }

            JsonObject init_JsonObject(BlockPos p) {
               return AiJson.init_JsonObject_ObjArr("x", p.getX(), "y", p.getY(), "z", p.getZ());
            }

            boolean load(BlockPos pos) {
               return this.init_Minecraft.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) <= 25.0
                  && autismclient.util.InteractionTargetRef.init_Z(this.init_Minecraft, pos);
            }

            boolean read(BlockPos pos) {
               return this.init(pos, 0.5);
            }

            boolean load(AiFarm.Inner_CachePool w) {
               BlockPos cell = w.kind() == AiFarm.Inner_BufferHelper.load ? w.base() : w.cell();
               if (w.kind() == AiFarm.Inner_BufferHelper.load) {
                  return autismclient.ai.a.AiBlockInteraction.init(this.init_Minecraft, w.base(), w.face()) != null;
               } else {
                  return w.kind() == AiFarm.Inner_BufferHelper.read ? this.init(cell, this.init_D(cell)) : this.read(cell);
               }
            }

            boolean init(BlockPos pos, double aimY) {
               BlockHitResult hit = this.init_Minecraft
                  .level
                  .clip(
                     new ClipContext(
                        this.init_Minecraft.player.getEyePosition(),
                        new Vec3(pos.getX() + 0.5, pos.getY() + aimY, pos.getZ() + 0.5),
                        net.minecraft.world.level.ClipContext.Block.OUTLINE,
                        Fluid.NONE,
                        this.init_Minecraft.player
                     )
                  );
               return hit.getType() != Type.MISS && hit.getBlockPos().equals(pos);
            }

            double init_D(BlockPos pos) {
               VoxelShape shape = this.init_Minecraft.level.getBlockState(pos).getShape(this.init_Minecraft.level, pos);
               double h = shape.isEmpty() ? 1.0 : shape.max(Axis.Y);
               return Math.max(0.05, Math.min(0.95, h / 2.0));
            }

            String init_Str() {
               if (this.init_B_CachePool == null) {
                  return "Working the field";
               }

               String var10000;
               switch (this.init_B_CachePool.kind()) {
                  case init:
                     var10000 = (kind == AiFarm.Inner_StreamUtil.read ? "Picking " : "Breaking ") + this.init_B_CachePool.cell().toShortString();
                     break;
                  case load:
                     var10000 = "Replanting " + this.init_B_CachePool.cell().toShortString();
                     break;
                  case read:
                     var10000 = "Feeding " + this.init_B_CachePool.cell().toShortString();
                     break;
                  case write:
                     var10000 = "Sweeping up the drops";
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               return var10000;
            }

            JsonObject init_JsonObject() {
               JsonArray skipList = new JsonArray();

               for (JsonObject entry : this.read_List) {
                  skipList.add(entry);
               }

               return AiJson.init_JsonObject_ObjArr(
                  "crop",
                  cropBlock,
                  "count",
                  AiGameState.init_I_Str(cropItem),
                  "target",
                  this.read_I,
                  "harvested",
                  this.stop,
                  "replanted",
                  this.next,
                  "externalReplants",
                  this.prev,
                  "replantSkipped",
                  this.valid,
                  "drops",
                  this.size,
                  "bonemeal",
                  this.data,
                  "skipped",
                  skipList,
                  "skippedTotal",
                  this.value,
                  "queued",
                  this.init_List.size() + this.init_ArrayDeque.size(),
                  "confirmingReplants",
                  this.write_List.size(),
                  "activeWork",
                  this.init_B_CachePool == null ? "" : this.init_B_CachePool.kind().name()
               );
            }

            AiOperation.Result read(String message) {
               return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.init_AiOperation_Result(status, message, this.init_JsonObject());
            }

            @Override
            public void cancel() {
               if (!this.update_Z) {
                  this.update_Z = true;
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  for (AiOperation op : this.write_List) {
                     op.cancel();
                  }

                  this.write_List.clear();
                  this.update_List.clear();
                  this.write_BlockPos = null;
                  autismclient.ai.a.AiMotionSafety.putAt_V();
                  if (this.init_Minecraft.player != null && this.init_Minecraft.player.getInventory().getSelectedSlot() != this.init_I) {
                     autismclient.util.InventorySlotHelper.init_V(this.init_Minecraft, this.init_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Farming requires a connected living player");
      }
   }

   static List<String> init_List(String entityTypeId) {
      return write_Map.getOrDefault(entityTypeId, List.of());
   }

   private static AiOperation load(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      final String species = AiJson.init_Str_JsonObject(args, "animal", "").trim().toLowerCase(Locale.ROOT);
      if (species.isEmpty()) {
         throw new IllegalArgumentException("breed needs animal, the species to feed");
      }

      final String typeId = species.contains(":") ? species : "minecraft:" + species;
      final List<String> foods = init_List(typeId);
      if (foods.isEmpty()) {
         throw new IllegalArgumentException("No known breeding food for '" + species + "'; breedable species: " + String.join(", ", write_Map.keySet()));
      }

      final int feeds = Math.max(2, Math.min(64, AiJson.init_I_JsonObject(args, "count", 2)));
      final int timeoutSeconds = Math.max(10, Math.min(900, AiJson.init_I_JsonObject(args, "timeoutSeconds", 180)));
      return new AiOperation() {
         final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         final autismclient.ai.a.AiAim init_DataUtil = new autismclient.ai.a.AiAim();
         final Set<Integer> init_Set = new LinkedHashSet<>();
         final Set<Integer> load_Set = new LinkedHashSet<>();
         AiOperation init_AiOperation;
         Entity init_Entity;
         Entity load_Entity;
         int init_I;
         int load_I;
         int read;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(
                     this.init_Set.size() >= 2 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.TIMED_OUT,
                     "Breeding deadline reached after " + this.init_Set.size() + " feed(s)"
                  );
               }

               if (this.init_Set.size() >= feeds) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Fed " + this.init_Set.size() + " " + species);
               }

               if (this.load_Entity != null) {
                  if (this.load_Entity instanceof Animal animal && animal.isInLove()) {
                     this.init_Set.add(this.load_Entity.getId());
                     this.load_Entity = null;
                     this.init_I = 0;
                     return this.init_AiOperation_Result_2("Fed " + this.init_Set.size() + "/" + feeds);
                  } else if (++this.init_I > 10) {
                     this.load_Set.add(this.load_Entity.getId());
                     this.load_Entity = null;
                     this.init_I = 0;
                     return this.init_AiOperation_Result_2("No love mode from that " + species + " (healing, growing or already fed); skipping it");
                  } else {
                     return this.init_AiOperation_Result_2("Confirming the feed");
                  }
               } else {
                  String food = this.init_Str_2();
                  if (food == null) {
                     return this.init(
                        this.init_Set.size() >= 2 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                        this.init_Set.size() >= 2
                           ? "Ran out of breeding food after " + this.init_Set.size() + " feed(s)"
                           : "No breeding food for " + species + " in inventory (needs " + String.join(" or ", foods) + ")"
                     );
                  }

                  if (this.read > 0) {
                     this.read--;
                     return this.init_AiOperation_Result_2("Waiting between feeds");
                  }

                  if (this.init_Entity == null
                     || !this.init_Entity.isAlive()
                     || this.init_Set.contains(this.init_Entity.getId())
                     || this.load_Set.contains(this.init_Entity.getId())) {
                     this.init_Entity = this.init_Entity(typeId);
                     this.load_I = 0;
                     if (this.init_Entity == null) {
                        if (this.init_Set.size() >= 2) {
                           return this.init(AiOperation.Status.SUCCEEDED, "No more unfed " + species + " nearby after " + this.init_Set.size() + " feed(s)");
                        }

                        String untamed = this.init_Str(typeId);
                        if (untamed != null) {
                           return this.init(AiOperation.Status.FAILED, untamed);
                        }

                        return this.init(AiOperation.Status.FAILED, "No breedable " + species + " within 16 blocks");
                     }
                  }

                  double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
                  if (mc.player.distanceTo(this.init_Entity) > reach * 0.9) {
                     if (this.init_AiOperation == null) {
                        this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(this.init_Entity.blockPosition(), 1.8, false, false, true, 20);
                     }

                     AiOperation.Result legResult = this.init_AiOperation.tick();
                     if (!legResult.terminal()) {
                        return this.init_AiOperation_Result_2("Walking to a " + species);
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                        this.load_Set.add(this.init_Entity.getId());
                        this.init_Entity = null;
                     }

                     return this.init_AiOperation_Result_2("Re-evaluating the pen");
                  } else {
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     this.init_DataUtil.init_V_Vec3(autismclient.ai.a.AiAim.load_Vec3_Entity(this.init_Entity));
                     this.init_DataUtil.load_V();
                     if (!autismclient.ai.a.AiAim.init_Z_Entity(this.init_Entity)) {
                        if (++this.load_I > 40) {
                           this.load_Set.add(this.init_Entity.getId());
                           this.init_Entity = null;
                        }

                        return this.init_AiOperation_Result_2("Turning toward a " + species);
                     } else {
                        if (AiItems.read(AiItems.load_af_DataUtil(food)) < 0) {
                           return this.init(AiOperation.Status.FAILED, "Could not hold " + food);
                        }

                        mc.gameMode
                           .interact(
                              mc.player,
                              this.init_Entity,
                              new EntityHitResult(this.init_Entity, autismclient.ai.a.AiAim.init_Vec3_Entity(this.init_Entity)),
                              InteractionHand.MAIN_HAND
                           );
                        Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                        this.load_Entity = this.init_Entity;
                        this.init_I = 0;
                        this.init_Entity = null;
                        this.read = 10;
                        return this.init_AiOperation_Result_2("Feeding a " + species);
                     }
                  }
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Breeding cancelled");
            }
         }

         String init_Str(String wanted) {
            if (!wanted.equals("minecraft:wolf") && !wanted.equals("minecraft:cat")) {
               return null;
            }

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof TamableAnimal tamable
                  && !tamable.isTame()
                  && entity.isAlive()
                  && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(wanted)
                  && entity.distanceToSqr(mc.player) <= 256.0) {
                  return "Nearby "
                     + wanted.substring(wanted.indexOf(58) + 1)
                     + " are not tamed; tame them first (wolves with bones, cats with raw fish) - food only heals an untamed one";
               }
            }

            return null;
         }

         String init_Str_2() {
            for (String food : foods) {
               if (AiGameState.init_I_Str(food) > 0) {
                  return food;
               }
            }

            return null;
         }

         Entity init_Entity(String wanted) {
            Entity best = null;
            double bestDistance = 256.0;

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Animal animal
                  && animal.isAlive()
                  && !animal.isBaby()
                  && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals(wanted)
                  && !this.init_Set.contains(entity.getId())
                  && !this.load_Set.contains(entity.getId())) {
                  double distance = entity.distanceToSqr(mc.player);
                  if (distance < bestDistance) {
                     bestDistance = distance;
                     best = entity;
                  }
               }
            }

            return best;
         }

         JsonObject init_JsonObject_2() {
            return AiJson.init_JsonObject_ObjArr(
               "action",
               "breed",
               "animal",
               typeId,
               "fed",
               this.init_Set.size(),
               "requested",
               feeds,
               "skipped",
               this.load_Set.size(),
               "food",
               AiJson.init.toJsonTree(foods)
            );
         }

         AiOperation.Result init_AiOperation_Result_2(String message) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_2().toString(), List.of());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            JsonObject data = this.init_JsonObject_2();
            this.cancel();
            return new AiOperation.Result(status, message, data.toString(), List.of());
         }

         @Override
         public void cancel() {
            if (!this.init_Z) {
               this.init_Z = true;
               this.init_DataUtil.init_V();
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }
         }
      };
   }

   static List<BlockPos> init(Minecraft mc, Block crop) {
      BlockPos origin = mc.player.blockPosition();
      BlockPos seed = null;
      double best = 1.7976931348623157E308;
      MutableBlockPos p = new MutableBlockPos();

      for (int dx = -32; dx <= 32; dx++) {
         for (int dz = -32; dz <= 32; dz++) {
            for (int dy = -8; dy <= 8; dy++) {
               p.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
               if (AiTerrain.init(mc.level, p)) {
                  BlockState state = mc.level.getBlockState(p);
                  if (state.is(crop) && init_Z(state)) {
                     double sq = p.distSqr(origin);
                     if (sq < best) {
                        best = sq;
                        seed = p.immutable();
                     }
                  }
               }
            }
         }
      }

      if (seed == null) {
         return List.of();
      }

      List<BlockPos> field = new ArrayList<>();
      Set<Long> seen = new HashSet<>();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>(List.of(seed.below()));
      seen.add(seed.below().asLong());

      while (!queue.isEmpty() && field.size() < 512) {
         BlockPos ground = queue.poll();
         field.add(ground);

         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos next = ground.offset(dx, 0, dz);
               if (seen.add(next.asLong())
                  && AiTerrain.init(mc.level, next)
                  && autismclient.util.FarmCropCatalog.init_Z_Block_BlockState(crop, mc.level.getBlockState(next))
                  && (mc.level.getBlockState(next.above()).isAir() || mc.level.getBlockState(next.above()).is(crop))) {
                  queue.add(next);
               }
            }
         }
      }

      return field;
   }

   private static AiOperation load(final JsonObject args, final String cropBlock, final String cropItem) {
      final Minecraft mc = Minecraft.getInstance();
      Block crop = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cropBlock));
      int baseline = AiGameState.init_I_Str(cropItem);
      final int target = AiJson.init_I_JsonObject(args, "count", 53) + (AiJson.init_Z_JsonObject(args, "additional", false) ? baseline : 0);
      final List<BlockPos> field = init(mc, crop);
      if (!field.isEmpty() && !AiJson.init_Z_JsonObject(args, "bonemeal", false)) {
         final JsonObject rest = args.deepCopy();
         rest.addProperty("count", target);
         rest.addProperty("additional", false);
         return new AiOperation() {
            AiOperation init_AiOperation = AiFieldWork.init(field, cropBlock, AiFarm.load(cropBlock), cropItem, target, AiJson.init_Z_JsonObject(args, "replant", true));
            boolean init_Z;
            boolean load;

            @Override
            public AiOperation.Result tick() {
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return r;
               }

               if (!this.load && r.status() == AiOperation.Status.SUCCEEDED && AiGameState.init_I_Str(cropItem) >= target && this.init()) {
                  this.init_AiOperation.cancel();
                  this.load = true;
                  this.init_AiOperation = AiFieldWork.init(field, cropBlock, AiFarm.load(cropBlock), cropItem, AiGameState.init_I_Str(cropItem));
                  return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Collecting the field's remaining drops", new JsonObject());
               }

               if (this.load
                  || this.init_Z
                  || r.status() == AiOperation.Status.CANCELLED
                  || AiGameState.init_I_Str(cropItem) >= target
                  || r.status() == AiOperation.Status.FAILED && r.message().contains("Inventory full")) {
                  return r;
               }

               this.init_AiOperation.cancel();
               this.init_Z = true;
               this.init_AiOperation = AiFarm.init(rest, cropBlock, cropItem);
               return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Field done; looking for more " + cropItem, new JsonObject());
            }

            boolean init() {
               int minX = 2147483647;
               int minZ = 2147483647;
               int maxX = -2147483648;
               int maxZ = -2147483648;

               for (BlockPos p : field) {
                  minX = Math.min(minX, p.getX());
                  minZ = Math.min(minZ, p.getZ());
                  maxX = Math.max(maxX, p.getX());
                  maxZ = Math.max(maxZ, p.getZ());
               }

               int y = field.getFirst().getY() + 1;
               String seed = AiFarm.load(cropBlock);
               AABB box = new AABB(minX - 2, y - 2, minZ - 2, maxX + 3, y + 3, maxZ + 3);
               return !mc.level.getEntitiesOfClass(ItemEntity.class, box, item -> {
                  if (!item.isAlive()) {
                     return false;
                  }

                  String id = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                  return id.equals(cropItem) || id.equals(seed);
               }).isEmpty();
            }

            @Override
            public void cancel() {
               this.init_AiOperation.cancel();
            }
         };
      } else {
         return init(args, cropBlock, cropItem);
      }
   }

   private static AiFarm.Inner_DataUtil init_B_DataUtil(String text) {
      String t = text.trim().toLowerCase(Locale.ROOT);
      if (Set.of("sow", "till").contains(t)) {
         return AiFarm.Inner_DataUtil.read;
      }

      if (Set.of("create", "make", "start", "farm").contains(t)) {
         return AiFarm.Inner_DataUtil.write;
      }

      try {
         return AiFarm.Inner_DataUtil.valueOf(t.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be harvest, breed, plant or build");
      }
   }

   static {
      init_Map.put("minecraft:wheat", "minecraft:wheat");
      init_Map.put("minecraft:carrots", "minecraft:carrot");
      init_Map.put("minecraft:potatoes", "minecraft:potato");
      init_Map.put("minecraft:beetroots", "minecraft:beetroot");
      init_Map.put("minecraft:nether_wart", "minecraft:nether_wart");
      init_Map.put("minecraft:melon", "minecraft:melon_slice");
      init_Map.put("minecraft:pumpkin", "minecraft:pumpkin");
      init_Map.put("minecraft:sweet_berry_bush", "minecraft:sweet_berries");
      init_Map.put("minecraft:cocoa", "minecraft:cocoa_beans");
      init_Map.put("minecraft:sugar_cane", "minecraft:sugar_cane");
      init_Map.put("minecraft:cactus", "minecraft:cactus");
      init_Map.put("minecraft:bamboo", "minecraft:bamboo");
      init_Map.forEach((crop, yield) -> load_Map.putIfAbsent(yield, crop));
   }

   private enum Inner_BufferHelper {
      init,
      load,
      read,
      write;
   }

   private record Inner_CachePool(AiFarm.Inner_BufferHelper kind, BlockPos cell, BlockPos base, Direction face, int entityId, String item, double distanceSqr) {
      static AiFarm.Inner_CachePool harvest(BlockPos cell, double distanceSqr) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.init, cell, null, null, -1, null, distanceSqr);
      }

      static AiFarm.Inner_CachePool replant(BlockPos cell, BlockPos base, Direction face) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.load, cell, base, face, -1, null, 0.0);
      }

      static AiFarm.Inner_CachePool bonemeal(BlockPos cell, double distanceSqr) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.read, cell, null, null, -1, null, distanceSqr);
      }

      static AiFarm.Inner_CachePool drop(int entityId, String item, BlockPos cell, double distanceSqr) {
         return new AiFarm.Inner_CachePool(AiFarm.Inner_BufferHelper.write, cell, null, null, entityId, item, distanceSqr);
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
