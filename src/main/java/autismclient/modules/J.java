package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.util.AutismClientMessaging;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public final class J extends Module {
   static final String init_Str = "Places blocks that only exist for you. Creative mode fakes a creative gamemode so anything you place becomes a ghost; right-click places, left-click removes, sneak keeps it real.";
   static final String load_Str = "Creative fakes your gamemode";
   static final String read_Str = "Ticks between placements";
   static final String write_Str = "Click ghosts to remove";
   static final String update_Str = "Sneak places real blocks";
   static final String sync = "Clear ghosts on disable";
   static final String flush = "Key clears all ghosts";
   static final String close = "Outline ghost blocks";
   static final String open = "Translucent ghost fill";
   static final String poll = "Ghost highlight color";
   private static final int init_I = 4096;
   private static final int load_I = 2001;
   private static final int read_I = 20;
   private static final int write_I = -6381922;
   private final Map<BlockPos, BlockState> init_Map = new LinkedHashMap<>();
   private final Map<BlockPos, BlockState> load_Map = new LinkedHashMap<>();
   private Block init_Block;
   private ClientLevel init_ClientLevel;
   private boolean init_Z;
   private boolean load_Z;
   private int update_I;
   private volatile List<AABB> init_List = List.of();
   private volatile boolean read_Z;

   J() {
      super(
         "ghostblock",
         "GhostBlock",
         ae.read,
         "Places blocks that only exist for you. Creative mode fakes a creative gamemode so anything you place becomes a ghost; right-click places, left-click removes, sneak keeps it real."
      );
      this.add(new ChoiceSetting("mode", "Mode", "Creative", "Creative", "Survival").description("Creative fakes your gamemode").build());
      this.add(new IntSetting("place-delay", "Place Delay", 4, 1, 20, 1).description("Ticks between placements").build());
      this.add(new BoolSetting("break-ghosts", "Click Removes", true).description("Click ghosts to remove").build());
      this.add(new BoolSetting("sneak-bypass", "Sneak Bypass", true).description("Sneak places real blocks").build());
      this.add(new BoolSetting("clear-on-disable", "Clear On Disable", false).description("Clear ghosts on disable").build());
      this.add(new KeybindSetting("clear-key", "Clear Key", -1).description("Key clears all ghosts").build());
      this.add(new BoolSetting("highlight", "Highlight", true).description("Outline ghost blocks").build());
      this.add(new BoolSetting("highlight-fill", "Highlight Fill", true).description("Translucent ghost fill").build());
      this.add(new ColorSetting("highlight-color", "Highlight Color", -6381922).description("Ghost highlight color").build());
   }

   @Override
   public void onEnable() {
      this.init_V_7();
   }

   @Override
   public void onDisable() {
      if (this.bool("clear-on-disable")) {
         this.init_I_5();
      }

      this.load_V_6();
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.load_I_4() > 0;
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if ("mode".equals(settingId)) {
         this.init_V_7();
      }
   }

   private void init_V_7() {
      if (this.isEnabled() && MC != null && MC.player != null && MC.gameMode != null) {
         if (!this.init_Z_9()) {
            this.load_V_6();
         } else if (!this.load_Z || autismclient.util.W.init_W_StreamUtil().load_GameType() != GameType.CREATIVE) {
            this.load_Z = autismclient.util.W.init_W_DataUtil(GameType.CREATIVE).init_Z();
         }
      }
   }

   private void load_V_6() {
      if (this.load_Z) {
         this.load_Z = false;
         autismclient.util.W.init_W_DataUtil();
      }
   }

   @Override
   public void onGameJoin() {
      this.init_V_7();
   }

   @Override
   public void onGameLeft() {
      this.sync();
      this.load_V_6();
      this.init_ClientLevel = null;
      this.init_Z = false;
   }

   @Override
   public void tick() {
      if (MC != null && MC.level != null && MC.player != null) {
         if (MC.level != this.init_ClientLevel) {
            this.sync();
            this.init_ClientLevel = MC.level;
         }

         this.read_V_4();
         if (!this.isEnabled()) {
            this.update();
         } else {
            this.write_V_4();
            if (++this.update_I >= 20) {
               this.update_I = 0;
               this.init_V_7();
            }
         }
      }
   }

   private void read_V_4() {
      int bind = this.write_I_3();
      boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      if (down && !this.init_Z && MC.gui.screen() == null) {
         int removed = this.init_I_5();
         if (removed > 0) {
            AutismClientMessaging.sendPrefixed("§aCleared §f" + removed + "§a ghost block" + (removed == 1 ? "" : "s") + ".");
         }
      }

      this.init_Z = down;
   }

   private int write_I_3() {
      try {
         return Integer.parseInt(this.value("clear-key"));
      } catch (NumberFormatException ignored) {
         return -1;
      }
   }

   private void write_V_4() {
      List<Entry<BlockPos, BlockState>> snapshot;
      synchronized (this.init_Map) {
         if (this.init_Map.isEmpty()) {
            return;
         }

         snapshot = new ArrayList<>(this.init_Map.entrySet());
      }

      for (Entry<BlockPos, BlockState> entry : snapshot) {
         BlockPos pos = entry.getKey();
         if (this.read(pos) && MC.level.getBlockState(pos) != entry.getValue()) {
            MC.level.setBlock(pos, entry.getValue(), 3);
         }
      }
   }

   private void update() {
      List<Entry<BlockPos, BlockState>> snapshot;
      synchronized (this.init_Map) {
         if (this.init_Map.isEmpty()) {
            return;
         }

         snapshot = new ArrayList<>(this.init_Map.entrySet());
      }

      List<BlockPos> lost = null;

      for (Entry<BlockPos, BlockState> entry : snapshot) {
         BlockPos pos = entry.getKey();
         if (this.read(pos) && MC.level.getBlockState(pos) != entry.getValue()) {
            if (lost == null) {
               lost = new ArrayList<>();
            }

            lost.add(pos);
         }
      }

      if (lost != null) {
         synchronized (this.init_Map) {
            for (BlockPos pos : lost) {
               this.init_Map.remove(pos);
               this.load_Map.remove(pos);
            }

            this.read_Z = true;
         }
      }
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand ignoredHand) {
      if (MC != null && MC.level != null && MC.player != null) {
         if (FieldCache.init_Z_6()) {
            return false;
         }

         if (hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
            boolean sneaking = MC.player.isShiftKeyDown();
            if (sneaking && this.bool("sneak-bypass")) {
               return false;
            }

            if (!sneaking && this.init(MC.level.getBlockState(hit.getBlockPos()), hit.getBlockPos())) {
               return false;
            }

            InteractionHand hand = InteractionHand.MAIN_HAND;
            ItemStack stack = MC.player.getMainHandItem();
            if (!(stack.getItem() instanceof BlockItem)) {
               stack = MC.player.getOffhandItem();
               hand = InteractionHand.OFF_HAND;
            }

            boolean fromHand = stack.getItem() instanceof BlockItem;
            if (!fromHand) {
               if (!this.init_Z_9() || this.init_Block == null) {
                  return false;
               }

               hand = InteractionHand.MAIN_HAND;
               stack = new ItemStack(this.init_Block);
            }

            if (stack.getItem() instanceof BlockItem blockItem) {
               ((AutismMinecraftAccessor)MC).autism$setRightClickDelay(Math.max(1, this.integer("place-delay")));
               this.init(blockItem, hand, stack, hit, fromHand);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void init(BlockItem blockItem, InteractionHand hand, ItemStack stack, BlockHitResult hit, boolean fromHand) {
      BlockPlaceContext context = new BlockPlaceContext(MC.player, hand, stack, hit);
      if (context.canPlace()) {
         BlockPos clicked = context.getClickedPos();
         if (!MC.level.isOutsideBuildHeight(clicked)) {
            Map<BlockPos, BlockState> before = this.init_Map(clicked);
            ItemStack held = fromHand ? stack.copy() : null;
            boolean placed = blockItem.place(context).consumesAction();
            if (held != null) {
               MC.player.setItemInHand(hand, held);
            }

            if (placed) {
               for (Entry<BlockPos, BlockState> entry : before.entrySet()) {
                  BlockState current = MC.level.getBlockState(entry.getKey());
                  if (current != entry.getValue()) {
                     this.init(entry.getKey(), entry.getValue());
                     this.load(entry.getKey(), current);
                  }
               }

               Compat263.swing(MC.player, hand);
            }
         }
      }
   }

   private Map<BlockPos, BlockState> init_Map(BlockPos pos) {
      Map<BlockPos, BlockState> states = new LinkedHashMap<>();
      states.put(pos.immutable(), MC.level.getBlockState(pos));

      for (Direction direction : Direction.values()) {
         BlockPos neighbour = pos.relative(direction).immutable();
         states.put(neighbour, MC.level.getBlockState(neighbour));
      }

      return states;
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK ? this.load(hit.getBlockPos()) : false;
   }

   @Override
   public boolean onStartDestroyBlock(BlockPos pos, Direction direction) {
      return this.load(pos);
   }

   @Override
   public boolean shouldCancelStartBreakingBlock(BlockPos pos, Direction direction) {
      return this.load(pos);
   }

   private boolean load(BlockPos pos) {
      if (MC == null || MC.level == null || MC.player == null) {
         return false;
      }

      if (FieldCache.init_Z_6()) {
         return false;
      }

      if (this.init_Z(pos)) {
         if (this.bool("break-ghosts")) {
            this.load(pos, true);
         }

         return true;
      } else if (!this.init_Z_9()) {
         return false;
      } else {
         BlockState state = MC.level.getBlockState(pos);
         if (!state.isAir() && !MC.level.isOutsideBuildHeight(pos)) {
            this.init(pos, state);
            MC.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            this.load(pos, MC.level.getBlockState(pos));
            MC.level.levelEvent(null, 2001, pos, Block.getId(state));
            return true;
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (!this.init_Z_9()) {
         return false;
      }

      if (packet instanceof ServerboundSetCreativeModeSlotPacket) {
         return true;
      }

      if (packet instanceof ServerboundPickItemFromBlockPacket pick) {
         if (!FieldCache.init_Z_6()) {
            this.init(pick.pos(), pick.includeData());
         }

         return true;
      } else if (!(packet instanceof ServerboundPlayerActionPacket action)) {
         return false;
      } else {
         Action kind = action.getAction();
         return kind == Action.DROP_ITEM || kind == Action.DROP_ALL_ITEMS;
      }
   }

   private void init(BlockPos pos, boolean includeData) {
      if (MC != null && MC.level != null && MC.player != null) {
         BlockState state = MC.level.getBlockState(pos);
         if (!state.isAir()) {
            ItemStack picked = state.getCloneItemStack(MC.level, pos, includeData);
            if (!picked.isEmpty()) {
               Inventory inventory = MC.player.getInventory();
               int existing = inventory.findSlotMatchingItem(picked);
               if (Inventory.isHotbarSlot(existing)) {
                  inventory.setSelectedSlot(existing);
               } else {
                  inventory.setSelectedItem(picked);
               }
            }
         }
      }
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (!this.isEnabled()) {
         return false;
      }

      if (packet instanceof ClientboundBlockUpdatePacket update) {
         synchronized (this.init_Map) {
            if (!this.init_Map.containsKey(update.getPos())) {
               return false;
            }

            this.load_Map.put(update.getPos(), update.getBlockState());
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean read(BlockPos pos) {
      return MC.level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
   }

   private void init(BlockPos pos, BlockState original) {
      synchronized (this.init_Map) {
         this.load_Map.putIfAbsent(pos.immutable(), original);
      }
   }

   private void load(BlockPos pos, BlockState state) {
      synchronized (this.init_Map) {
         this.init_Map.put(pos.immutable(), state);

         while (this.init_Map.size() > 4096) {
            BlockPos eldest = this.init_Map.keySet().iterator().next();
            this.init_Map.remove(eldest);
            BlockState original = this.load_Map.remove(eldest);
            if (original != null && this.read(eldest)) {
               MC.level.setBlock(eldest, original, 3);
            }
         }

         this.read_Z = true;
      }
   }

   private void sync() {
      synchronized (this.init_Map) {
         this.init_Map.clear();
         this.load_Map.clear();
         this.read_Z = true;
      }
   }

   private void load(BlockPos pos, boolean effects) {
      BlockState ghost;
      BlockState original;
      synchronized (this.init_Map) {
         ghost = this.init_Map.remove(pos);
         original = this.load_Map.remove(pos);
         if (ghost != null) {
            this.read_Z = true;
         }
      }

      if (ghost != null && MC != null && MC.level != null && this.read(pos)) {
         if (original != null) {
            MC.level.setBlock(pos, original, 3);
         }

         if (effects) {
            MC.level.levelEvent(null, 2001, pos, Block.getId(ghost));
         }
      }
   }

   public int init_I_5() {
      List<BlockPos> positions;
      synchronized (this.init_Map) {
         positions = new ArrayList<>(this.init_Map.keySet());
      }

      for (BlockPos pos : positions) {
         this.load(pos, false);
      }

      return positions.size();
   }

   public boolean init_Z(BlockPos pos) {
      synchronized (this.init_Map) {
         return this.init_Map.containsKey(pos);
      }
   }

   public int load_I_4() {
      synchronized (this.init_Map) {
         return this.init_Map.size();
      }
   }

   public boolean init_Z_9() {
      return "Creative".equals(this.choice("mode"));
   }

   public void init(boolean creative) {
      this.setValue("mode", creative ? "Creative" : "Survival");
   }

   public Block init_Block() {
      return this.init_Block;
   }

   public boolean load_Z_5() {
      return this.bool("highlight");
   }

   public boolean read_Z_4() {
      return this.bool("highlight-fill");
   }

   public int read_I_3() {
      try {
         String value = this.value("highlight-color").replace("#", "");
         if (value.length() == 6) {
            value = "FF" + value;
         }

         return (int)Long.parseLong(value, 16);
      } catch (RuntimeException ignored) {
         return -6381922;
      }
   }

   public List<AABB> init_List_4() {
      if (this.read_Z) {
         synchronized (this.init_Map) {
            List<AABB> boxes = new ArrayList<>(this.init_Map.size());

            for (BlockPos pos : this.init_Map.keySet()) {
               boxes.add(new AABB(pos).inflate(0.002));
            }

            this.init_List = Collections.unmodifiableList(boxes);
            this.read_Z = false;
         }
      }

      return this.init_List;
   }

   public void init(Block block, boolean announce) {
      this.init_Block = block;
      if (announce) {
         AutismClientMessaging.sendPrefixed(block == null ? "§7Ghost block disarmed." : "§aGhost block: §f" + BuiltInRegistries.BLOCK.getKey(block));
      }
   }

   private boolean init(BlockState state, BlockPos pos) {
      if (state.getMenuProvider(MC.level, pos) != null) {
         return true;
      }

      Block block = state.getBlock();
      return block instanceof DoorBlock
         || block instanceof TrapDoorBlock
         || block instanceof FenceGateBlock
         || block instanceof ButtonBlock
         || block instanceof LeverBlock
         || block instanceof NoteBlock
         || block instanceof BedBlock
         || block instanceof BellBlock
         || block instanceof CakeBlock
         || block instanceof RespawnAnchorBlock
         || block instanceof DiodeBlock
         || block instanceof DaylightDetectorBlock
         || block instanceof DragonEggBlock
         || block instanceof RedstoneWireBlock
         || block instanceof JukeboxBlock
         || block instanceof FlowerPotBlock
         || block instanceof BeehiveBlock
         || block instanceof ChiseledBookShelfBlock
         || block instanceof CandleCakeBlock;
   }

   @Override
   public String info() {
      int count = this.load_I_4();
      if (count > 0) {
         return Integer.toString(count);
      } else {
         return this.init_Block == null ? "" : BuiltInRegistries.BLOCK.getKey(this.init_Block).getPath();
      }
   }
}
