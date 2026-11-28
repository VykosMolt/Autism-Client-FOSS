package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BedEspModule extends Module {
   private static final Set<Block> COVER_BLOCKS = buildCoverBlocks();
   private volatile List<BedEspModule.Inner_DataUtil> beds = List.of();
   private final Map<BlockPos, BedEspModule.Inner_DataUtil> bedsByPos = new HashMap<>();
   private final Map<BlockPos, Long> bedInputStamps = new HashMap<>();
   private final Map<BlockPos, BlockState[]> observedSurroundings = new HashMap<>();
   private final Map<BlockPos, BedCoverPathSolver.Inner_CachePool> coverCache = new HashMap<>();
   private final IdentityHashMap<ItemStack, Double> breakSpeedByStack = new IdentityHashMap<>();
   private List<BlockPos> bedPositions = List.of();
   private Object lastLevel;
   private int tickCounter;
   private int refreshCursor;
   private volatile int blockUpdateRevision;
   private int scannedRevision = -1;

   public BedEspModule() {
      super("bed-esp", "BedESP", ae.update, "Beds, cover and the quickest way in.");
      this.add(new BoolSetting("own-bed", "Own Bed", false).description("Show your bed too"));
      this.add(new BoolSetting("my-tools", "My Tools", true).description("Plan with your hotbar tools"));
      this.add(new BoolSetting("path", "Path", true).description("Mark blocks to break"));
      this.add(new BoolSetting("tags", "Tags", true).description("Show cover and tools"));
      this.add(new IntSetting("range", "Range", 96, 16, 256, 8).description("View range"));
   }

   static boolean isActive() {
      Module m = al.init_Module_Str("bed-esp");
      return m instanceof BedEspModule && m.isEnabled();
   }

   static BedEspModule active() {
      Module m = al.init_Module_Str("bed-esp");
      return m instanceof BedEspModule b && m.isEnabled() ? b : null;
   }

   static BedEspModule instance() {
      return al.init_Module_Str("bed-esp") instanceof BedEspModule b ? b : null;
   }

   static BedEspModule.Inner_CachePool policy() {
      BedEspModule m = instance();
      return m == null
         ? new BedEspModule.Inner_CachePool(true, true, true, 96)
         : new BedEspModule.Inner_CachePool(m.bool("my-tools"), m.bool("path"), m.bool("tags"), m.integer("range"));
   }

   List<BedEspModule.Inner_DataUtil> beds() {
      return BedNukerModule.active() == null
         ? this.beds
         : this.beds
            .stream()
            .map(
               b -> {
                  BedBreachRouteSolver.Inner_BufferHelper route = BedNukerModule.routeFor(b.pos());
                  if (route == null) {
                     return (BedEspModule.Inner_DataUtil)b;
                  }

                  List<BlockPos> cuts = route.cells().stream().filter(c -> !c.init_Z()).map(BedBreachRouteSolver.Inner_DataUtil::pos).toList();
                  return new BedEspModule.Inner_DataUtil(
                     b.pos(),
                     b.partner(),
                     b.color(),
                     b.box(),
                     b.labelPos(),
                     b.materials(),
                     cuts,
                     b.routeTools(),
                     route.ticks(),
                     cuts.isEmpty() ? "Open" : "",
                     !cuts.isEmpty(),
                     List.of(),
                     MC.player.getEyePosition(),
                     route.eye()
                  );
               }
            )
            .toList();
   }

   public static boolean init_Z_Block(Block block) {
      return COVER_BLOCKS.contains(block);
   }

   static List<EspChunkWorker.EspBox> scanChunk(LevelChunk chunk) {
      return EspChunkWorker.load
         .init(
            chunk,
            MC.level.getGameTime(),
            "beds",
            (c, out) -> {
               LevelChunkSection[] sections = c.getSections();

               for (int section = 0; section < sections.length; section++) {
                  LevelChunkSection blocks = sections[section];
                  if (blocks != null && !blocks.hasOnlyAir() && blocks.maybeHas(s -> s.getBlock() instanceof BedBlock)) {
                     for (int y = 0; y < 16; y++) {
                        for (int x = 0; x < 16; x++) {
                           for (int z = 0; z < 16; z++) {
                              if (blocks.getBlockState(x, y, z).getBlock() instanceof BedBlock) {
                                 BlockPos p = new BlockPos(
                                    c.getPos().getMinBlockX() + x, (c.getSectionYFromSectionIndex(section) << 4) + y, c.getPos().getMinBlockZ() + z
                                 );
                                 out.add(new EspChunkWorker.EspBox(new AABB(p), Vec3.atCenterOf(p), 0));
                                 if (out.size() >= 128) {
                                    return;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         );
   }

   private static Set<Block> buildCoverBlocks() {
      Set<Block> blocks = new HashSet<>(List.of(Blocks.GLASS, Blocks.OBSIDIAN, Blocks.OAK_PLANKS, Blocks.END_STONE));

      for (DyeColor color : DyeColor.values()) {
         for (String suffix : List.of("_wool", "_terracotta", "_stained_glass")) {
            BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(color.getName() + suffix)).ifPresent(blocks::add);
         }
      }

      return Set.copyOf(blocks);
   }

   @Override
   public void onDisable() {
      this.resetCaches();
   }

   @Override
   public void onGameLeft() {
      this.resetCaches();
   }

   @Override
   public void onOptionValueChanged(String id) {
      this.resetCaches();
   }

   private void resetCaches() {
      BedMiningEstimator.init();
      this.beds = List.of();
      this.bedsByPos.clear();
      this.bedInputStamps.clear();
      this.observedSurroundings.clear();
      this.coverCache.clear();
      this.breakSpeedByStack.clear();
      this.bedPositions = List.of();
      this.lastLevel = null;
      this.tickCounter = this.refreshCursor = 0;
      this.scannedRevision = -1;
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundBlockUpdatePacket
         || packet instanceof ClientboundSectionBlocksUpdatePacket
         || packet instanceof ClientboundLevelChunkWithLightPacket
         || packet instanceof ClientboundForgetLevelChunkPacket) {
         this.blockUpdateRevision++;
      }

      return false;
   }

   @Override
   public void tick() {
      if (MC.level != null && MC.player != null) {
         if (this.lastLevel != MC.level) {
            this.resetCaches();
            this.lastLevel = MC.level;
         }

         EspChunkWorker.setLevel(MC.level);
         this.tickCounter++;
         DyeColor own = TeamDyeColorResolver.init();
         if (this.scannedRevision != this.blockUpdateRevision || this.tickCounter % 20 == 1) {
            this.scan();
            this.scannedRevision = this.blockUpdateRevision;
         }

         this.bedsByPos.keySet().removeIf(px -> !this.bedPositions.contains(px));
         this.bedInputStamps.keySet().retainAll(this.bedsByPos.keySet());
         this.observedSurroundings.keySet().retainAll(this.bedPositions);
         this.coverCache.keySet().retainAll(this.bedPositions);
         if (!this.bedPositions.isEmpty()) {
            BlockPos p = this.bedPositions.get(Math.floorMod(this.refreshCursor++, this.bedPositions.size()));
            BlockState state = MC.level.getBlockState(p);
            if (state.getBlock() instanceof BedBlock bed && this.shouldShow(bed.getColor(), own)) {
               long stamp = this.bedInputStamp(p);
               if (!Objects.equals(this.bedInputStamps.get(p), stamp)) {
                  this.bedsByPos.put(p, this.analyzeBed(p, state));
                  this.bedInputStamps.put(p, stamp);
               }
            }
         }

         ArrayList<BedEspModule.Inner_DataUtil> visible = new ArrayList<>();
         double range = this.integer("range");

         for (BlockPos p : this.bedPositions) {
            BedEspModule.Inner_DataUtil bed = this.bedsByPos.get(p);
            if (bed != null
               && MC.level.isLoaded(p)
               && MC.level.getBlockState(p).getBlock() instanceof BedBlock
               && this.shouldShow(bed.color(), own)
               && bed.box().distanceToSqr(MC.player.position()) <= range * range) {
               visible.add(bed);
            }
         }

         this.beds = List.copyOf(visible);
      } else {
         this.resetCaches();
      }
   }

   private boolean shouldShow(DyeColor color, DyeColor own) {
      return this.bool("own-bed") || own != null && color != own;
   }

   private void scan() {
      Set<BlockPos> found = new LinkedHashSet<>();
      int radius = Math.min(am.init_I(), (this.integer("range") + 15) / 16);
      int cx = MC.player.chunkPosition().x();
      int cz = MC.player.chunkPosition().z();

      for (int r = 0; r <= radius && found.size() < 64; r++) {
         for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                  LevelChunk chunk = MC.level.getChunkSource().getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
                  if (chunk != null) {
                     for (EspChunkWorker.EspBox entry : scanChunk(chunk)) {
                        BlockPos p = BlockPos.containing(entry.tracer());
                        BlockState s = MC.level.getBlockState(p);
                        if (s.getBlock() instanceof BedBlock) {
                           if (s.getValue(BedBlock.PART) == BedPart.HEAD) {
                              BlockPos foot = p.relative(BedBlock.getConnectedDirection(s));
                              if (MC.level.isLoaded(foot) && isPartnerHalf(s, MC.level.getBlockState(foot))) {
                                 continue;
                              }
                           }

                           if (p.distToCenterSqr(MC.player.position()) <= (double)this.integer("range") * this.integer("range")) {
                              found.add(p);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      this.bedPositions = List.copyOf(found);
   }

   private long bedInputStamp(BlockPos bed) {
      long h = this.bool("my-tools") ? 1L : 2L;

      for (int i = 0; i < 9; i++) {
         h = h * 31L + ItemStack.hashItemAndComponents(MC.player.getInventory().getItem(i));
      }

      BlockPos origin = bed.offset(-8, -3, -8);
      int minX = origin.getX() >> 4;
      int minZ = origin.getZ() >> 4;
      LevelChunk[][] chunks = new LevelChunk[3][3];
      int nx = (origin.getX() + 17 >> 4) - minX + 1;
      int nz = (origin.getZ() + 17 >> 4) - minZ + 1;

      for (int z = 0; z < nz; z++) {
         for (int x = 0; x < nx; x++) {
            chunks[z][x] = MC.level.getChunkSource().getChunk(minX + x, minZ + z, ChunkStatus.FULL, false);
         }
      }

      BlockState[] observed = new BlockState[3888];
      int at = 0;
      MutableBlockPos pos = new MutableBlockPos();

      for (int y = 0; y < 12; y++) {
         for (int z = 0; z < 18; z++) {
            for (int x = 0; x < 18; x++) {
               pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
               LevelChunk chunk = chunks[(pos.getZ() >> 4) - minZ][(pos.getX() >> 4) - minX];
               BlockState state = chunk == null ? null : chunk.getBlockState(pos);
               observed[at++] = state;
               h = h * 31L + (state == null ? -1 : Block.getId(state));
            }
         }
      }

      if (!Arrays.equals(observed, this.observedSurroundings.get(bed))) {
         this.observedSurroundings.put(bed, observed);
         this.coverCache.remove(bed);
      }

      h = h * 31L + MC.player.blockPosition().hashCode();
      double reach = MC.player.blockInteractionRange() + 1.0;
      if (new AABB(bed).distanceToSqr(MC.player.getEyePosition()) <= reach * reach) {
         h = h * 31L + Double.doubleToLongBits(MC.player.getEyePosition().x);
         h = h * 31L + Double.doubleToLongBits(MC.player.getEyePosition().y);
         h = h * 31L + Double.doubleToLongBits(MC.player.getEyePosition().z);
      }

      h = h * 31L + Double.doubleToLongBits(MC.player.blockInteractionRange());
      h = h * 31L + (MC.player.hasInfiniteMaterials() ? 1 : 0);

      for (MobEffectInstance effect : MC.player.getActiveEffects()) {
         h = h * 31L + effect.getEffect().hashCode() * 31 + effect.getAmplifier();
      }

      return h * 31L + Double.doubleToLongBits(MC.player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED));
   }

   private BedEspModule.Inner_DataUtil analyzeBed(BlockPos p, BlockState state) {
      BlockPos partner = p.relative(BedBlock.getConnectedDirection(state));
      boolean hasPartner = MC.level.isLoaded(partner) && isPartnerHalf(state, MC.level.getBlockState(partner));
      BedCoverPathSolver.Inner_ConfigStore grid = new BedCoverPathSolver.Inner_ConfigStore(p.offset(-8, -3, -8), 18, 12, 18);
      Map<Block, BedEspModule.Inner_BufferHelper> toolCache = new HashMap<>();
      boolean loaded = true;
      this.breakSpeedByStack.clear();
      List<ItemStack> hotbar = this.hotbarTools();
      List<ItemStack> recommended = this.recommendedToolList();
      List<ItemStack> tools = this.bool("my-tools") ? hotbar : recommended;
      BlockState[] states = this.observedSurroundings.get(p);

      for (int i = 0; i < grid.write; i++) {
         BlockPos at = grid.posAt(i);
         BlockState s = states[i];
         if (s == null) {
            loaded = false;
         } else if (!at.equals(p) && (!hasPartner || !at.equals(partner))) {
            if (s.isAir()) {
               grid.nodes[i] = new BedCoverPathSolver.Inner_StreamUtil(0, 0.0, -1);
            } else if (init_Z_Block(s.getBlock())) {
               BedEspModule.Inner_BufferHelper best = toolCache.computeIfAbsent(s.getBlock(), b -> this.bestTool(s, at, tools));
               grid.nodes[i] = new BedCoverPathSolver.Inner_StreamUtil(2, best.breakTicks(), tools.indexOf(best.tool()));
            }
         } else {
            grid.nodes[i] = new BedCoverPathSolver.Inner_StreamUtil(3, 0.0, -1);
            grid.init_List.add(i);
         }
      }

      BedCoverPathSolver.Inner_CachePool cover = this.coverCache.computeIfAbsent(p, ignored -> BedCoverPathSolver.minimalCover(grid));
      BedBreachRouteSolver.Inner_BufferHelper local = BedMiningEstimator.init(p, true, null, 0.0F);
      BedCoverPathSolver.Inner_NetHelper plan = local == null
         ? BedCoverPathSolver.plan(grid, MC.player.position(), MC.player.blockInteractionRange(), cover)
         : new BedCoverPathSolver.Inner_NetHelper(
            cover.cells(),
            local.cells().stream().filter(c -> !c.init_Z()).map(BedBreachRouteSolver.Inner_DataUtil::pos).toList(),
            List.of(),
            local.ticks(),
            true,
            cover.init_I(),
            MC.player.getEyePosition(),
            local.eye()
         );
      Map<String, List<BlockPos>> groups = new LinkedHashMap<>();

      for (BlockPos at : plan.init_Set()) {
         groups.computeIfAbsent(materialGroupName(MC.level.getBlockState(at).getBlock()), k -> new ArrayList<>()).add(at);
      }

      List<BedEspModule.Inner_StreamUtil> materials = new ArrayList<>();

      for (Entry<String, List<BlockPos>> entry : groups.entrySet()) {
         Block b = MC.level.getBlockState(entry.getValue().getFirst()).getBlock();
         BlockPos at = entry.getValue().getFirst();
         materials.add(
            new BedEspModule.Inner_StreamUtil(entry.getKey(), b, entry.getValue().size(), this.bestTool(MC.level.getBlockState(at), at, recommended).tool())
         );
      }

      List<ItemStack> routeTools = new ArrayList<>();
      Map<Block, BedEspModule.Inner_BufferHelper> actualTools = this.bool("my-tools") ? toolCache : new HashMap<>();
      double actualTicks = plan.load_Z() ? Math.max(0, plan.path().size() - 1) * 5 : 1.0 / 0.0;

      for (BlockPos at : plan.path()) {
         BlockState block = MC.level.getBlockState(at);
         BedEspModule.Inner_BufferHelper actual = actualTools.computeIfAbsent(block.getBlock(), b -> this.bestTool(block, at, hotbar));
         actualTicks += actual.breakTicks();
         ItemStack tool = actual.tool();
         if (!tool.isEmpty() && routeTools.stream().noneMatch(t -> ItemStack.isSameItemSameComponents(t, tool))) {
            routeTools.add(tool);
         }
      }

      AABB box = (hasPartner ? new AABB(p).minmax(new AABB(partner)) : new AABB(p)).setMaxY(p.getY() + 0.5625);
      if (local != null) {
         List<BlockPos> cuts = local.cells().stream().filter(c -> !c.init_Z()).map(BedBreachRouteSolver.Inner_DataUtil::pos).toList();
         return new BedEspModule.Inner_DataUtil(
            p,
            partner,
            ((BedBlock)state.getBlock()).getColor(),
            box,
            box.getCenter().add(0.0, 1.25, 0.0),
            List.copyOf(materials),
            cuts,
            List.copyOf(routeTools),
            local.ticks(),
            cuts.isEmpty() ? "Open" : "",
            !cuts.isEmpty(),
            List.of(),
            MC.player.getEyePosition(),
            local.eye()
         );
      } else {
         String status = !plan.load_Z() ? (!loaded ? "Loading" : "No way in") : (plan.path().isEmpty() ? "Open" : "");
         return new BedEspModule.Inner_DataUtil(
            p,
            partner,
            ((BedBlock)state.getBlock()).getColor(),
            box,
            box.getCenter().add(0.0, 1.25, 0.0),
            List.copyOf(materials),
            plan.path(),
            List.copyOf(routeTools),
            actualTicks,
            status,
            plan.init_Z_2(),
            plan.load_List(),
            plan.init_Vec3(),
            plan.load_Vec3()
         );
      }
   }

   private static boolean isPartnerHalf(BlockState first, BlockState other) {
      return other.is(first.getBlock())
         && other.hasProperty(BedBlock.PART)
         && other.getValue(BedBlock.PART) != first.getValue(BedBlock.PART)
         && other.getValue(BedBlock.FACING) == first.getValue(BedBlock.FACING);
   }

   private List<ItemStack> hotbarTools() {
      List<ItemStack> tools = new ArrayList<>();
      tools.add(ItemStack.EMPTY);

      for (int i = 0; i < 9; i++) {
         tools.add(MC.player.getInventory().getItem(i).copy());
      }

      return tools;
   }

   private List<ItemStack> recommendedToolList() {
      return recommendedTools();
   }

   static List<ItemStack> recommendedTools() {
      List<ItemStack> tools = new ArrayList<>();
      tools.add(ItemStack.EMPTY);
      boolean legacy = autismclient.util.LegacyCombatDetector.init(MC.player);

      for (Item item : List.of(legacy ? Items.DIAMOND_PICKAXE : Items.NETHERITE_PICKAXE, legacy ? Items.DIAMOND_AXE : Items.NETHERITE_AXE, Items.SHEARS)) {
         ItemStack stack = new ItemStack(item);
         stack.enchant(MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), 5);
         tools.add(stack);
      }

      return tools;
   }

   private BedEspModule.Inner_BufferHelper bestTool(BlockState state, BlockPos pos, List<ItemStack> tools) {
      double best = 1.0 / 0.0;
      ItemStack chosen = ItemStack.EMPTY;
      float hardness = state.getDestroySpeed(MC.level, pos);

      for (ItemStack stack : tools) {
         if (stack.isEmpty()
            || stack.isItemEnabled(MC.level.enabledFeatures()) && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() >= 2)) {
            double speed = stack.isEmpty() ? 1.0 : autismclient.util.BlockBreakSpeedHelper.init(MC, stack, state);
            int haste = MobEffectUtil.hasDigSpeed(MC.player) ? MobEffectUtil.getDigSpeedAmplification(MC.player) + 1 : 0;
            speed *= 1.0 + 0.2 * haste;
            MobEffectInstance fatigue = MC.player.getEffect(MobEffects.MINING_FATIGUE);
            if (fatigue != null) {
               double var10001;
               switch (fatigue.getAmplifier()) {
                  case 0:
                     var10001 = 0.3;
                     break;
                  case 1:
                     var10001 = 0.09;
                     break;
                  case 2:
                     var10001 = 0.0027;
                     break;
                  default:
                     var10001 = 8.1E-4;
               }

               speed *= var10001;
            }

            speed *= this.breakSpeedByStack.computeIfAbsent(stack, this::breakSpeedWith);
            boolean correct = !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state);
            double ticks = init(hardness, speed, correct, MC.player.hasInfiniteMaterials());
            if (ticks < best) {
               best = ticks;
               chosen = stack;
            }
         }
      }

      return new BedEspModule.Inner_BufferHelper(chosen, best);
   }

   private double breakSpeedWith(ItemStack stack) {
      Holder<Attribute> attribute = Attributes.BLOCK_BREAK_SPEED;
      AttributeInstance live = MC.player.getAttribute(attribute);
      if (live == null) {
         return attribute.value().getDefaultValue();
      }

      AttributeInstance candidate = new AttributeInstance(attribute, ignored -> {});
      candidate.replaceFrom(live);
      MC.player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
         if (type.equals(attribute)) {
            candidate.removeModifier(modifier.id());
         }
      });
      stack.forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
         if (type.equals(attribute)) {
            candidate.removeModifier(modifier.id());
            candidate.addTransientModifier(modifier);
         }
      });
      return candidate.getValue();
   }

   static double init(float hardness, double speed, boolean correct, boolean creative) {
      if (!(hardness < 0.0F) && !(speed <= 0.0)) {
         return !creative && hardness != 0.0F ? Math.max(1.0, Math.ceil(hardness * (correct ? 30 : 100) / speed)) : 1.0;
      } else {
         return 1.0 / 0.0;
      }
   }

   private static String materialGroupName(Block block) {
      String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
      return path.endsWith("_wool")
         ? "Wool"
         : (
            path.endsWith("_terracotta")
               ? "Clay"
               : (path.contains("glass") ? "Glass" : (block == Blocks.OBSIDIAN ? "Obsidian" : (block == Blocks.END_STONE ? "End Stone" : "Oak")))
         );
   }

   record Inner_BufferHelper(ItemStack tool, double breakTicks) {
   }

   record Inner_CachePool(boolean init_Z, boolean load, boolean read, int range) {
   }

   record Inner_DataUtil(
      BlockPos pos,
      BlockPos partner,
      DyeColor color,
      AABB box,
      Vec3 labelPos,
      List<BedEspModule.Inner_StreamUtil> materials,
      List<BlockPos> load_List,
      List<ItemStack> routeTools,
      double ticks,
      String init_Str,
      boolean init_Z,
      List<BlockPos> write,
      Vec3 fromEye,
      Vec3 routeEye
   ) {
   }

   record Inner_StreamUtil(String name, Block block, int count, ItemStack icon) {
   }
}
