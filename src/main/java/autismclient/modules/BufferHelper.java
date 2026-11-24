package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bM;
import autismclient.util.cd;
import autismclient.util.ce;
import autismclient.util.ch;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class BufferHelper extends Module implements cn.Inner_DataUtil {
   public static final String init_Str = "anchor-aura";
   private static final float init_F = 5.0F;
   private static final int init_I = 4;
   private static final int load_I = 4;
   private static final int read_I = 32;
   private static final int write_I = 6;
   private static final double init_D = 2.0;
   private static final int update_I = 48;
   private static final int sync_I = 40;
   private static final int flush_I = 3;
   private static final int close_I = 3;
   private static final int open_I = 10;
   private static final int poll_I = 3;
   private static final int push_I = 3;
   private static final int pop_I = 2;
   private static final int tick = 4;
   private static final int run = 12;
   private static int[] init_IArr = new int[0];
   private static int get = -1;
   private final Random init_Random = new Random();
   private long init_J;
   private long load_J;
   private int set;
   private final Map<BlockPos, Integer> init_Map = new HashMap<>();
   private LivingEntity init_LivingEntity;
   private int put = -2147483648;
   private int add = -2147483648;
   private final autismclient.util.NetHelper init_NetHelper = new autismclient.util.NetHelper();
   private final autismclient.util.StateCache init_StateCache = new autismclient.util.StateCache();
   private final autismclient.util.PoolEntry init_PoolEntry = new autismclient.util.PoolEntry();
   private final autismclient.util.HashUtil init_HashUtil = new autismclient.util.HashUtil();
   private final autismclient.util.TableIndex init_TableIndex = new autismclient.util.TableIndex();
   private final autismclient.util.JsonUtil init_JsonUtil = new autismclient.util.JsonUtil();
   private BlockPos init_BlockPos;
   private long read_J;
   private long write_J;
   private final Set<BlockPos> init_Set = new LinkedHashSet<>();
   private String load_Str;
   private Set<String> load_Set = Set.of();
   private int clear = -1;
   private int reset = -1;
   private int start;
   private int stop = -2147483648;
   private BlockPos load_BlockPos;
   private LivingEntity load_LivingEntity;
   private final autismclient.util.TextFormat init_TextFormat = new autismclient.util.TextFormat();
   private int next;
   private int prev;
   private boolean init_Z;
   private boolean load_Z;
   private int valid = -2147483648;
   private boolean read_Z;
   private int size = -2147483648;
   private int data;
   private BlockPos read_BlockPos;
   private BlockPos write_BlockPos;
   private int value;
   private BlockPos update_BlockPos;
   private int cache = -1;
   private LivingEntity read_LivingEntity;
   private int state;
   private int queue;
   private BlockPos sync_BlockPos;
   private boolean write_Z;
   private BlockPos flush_BlockPos;
   private BlockPos close_BlockPos;
   private BlockPos open_BlockPos;
   private int buffer = -2147483648;
   private BlockPos poll_BlockPos;
   private boolean update_Z;
   private BlockPos push_BlockPos;
   private BlockPos pop_BlockPos;
   private int entry;
   private int node;
   private int task;
   private autismclient.util.S.Inner_CachePool init_S_CachePool;
   private final Map<BlockPos, autismclient.util.S.Inner_CachePool> load_Map;
   private String read_Str;
   private StringBuilder init_StringBuilder;
   private final Map<String, Integer> read_Map;

   private static int init_I() {
      return autismclient.util.FileCache.init_I_I(autismclient.util.macro.ab.init_I());
   }

   public BufferHelper() {
      super("anchor-aura", "AnchorAura", ae.init_ae, "Blows targets up with respawn anchors.");
      this.init_S_CachePool = autismclient.util.S.Inner_CachePool.init_S_CachePool;
      this.load_Map = new HashMap<>();
      this.read_Str = "";
      this.read_Map = new HashMap<>();
      this.add(new BoolSetting("place", "Place", true).description("Place new respawn anchors.").build());
      this.add(new BoolSetting("charge", "Charge", true).description("Charge anchors with glowstone.").build());
      this.add(new BoolSetting("detonate", "Detonate", true).description("Detonate charged anchors.").build());
      this.add(
         new ChoiceSetting("aim-mode", "Aim Mode", "Auto", "Auto", "Aimed")
            .description("Auto chooses blast positions; Aimed starts at your crosshair and finishes that anchor.")
            .build()
      );
      this.add(autismclient.util.StateCache.init_DoubleSetting().build());
      this.add(new BoolSetting("silent", "Silent", true).group("Aim").build());
      this.add(new BoolSetting("move-fix", "Move Fix", true).group("Aim").description("Walk where you look").visibleWhen(() -> this.bool("silent")).build());
      this.add(
         new BoolSetting("repeat", "Repeat", true)
            .description("Start another anchor cycle while activation remains active.")
            .group("Engagement")
            .visibleWhen(() -> !"Always".equals(this.choice("activation")))
            .build()
      );
      this.add(
         new BoolSetting("protect-friends", "Protect Friends", true)
            .description("Check collateral blast damage to friends and teammates, including follow-through.")
            .group("Damage")
            .build()
      );
      this.add(
         new DoubleSetting("max-friend-damage", "Max Friend Damage", 4.0, 0.0, 20.0, 0.5)
            .description("Maximum collateral damage; lethal damage is always refused.")
            .group("Damage")
            .visibleWhen(() -> this.bool("protect-friends"))
            .build()
      );
      this.add(new DoubleSetting("target-range", "Target Range", 8.0, 1.0, 16.0, 0.5).description("Enemy search radius.").build());
      this.add(new ChoiceSetting("targeting", "Targeting", "Distance", "Distance", "HP", "FOV").description("How the enemy is picked.").build());
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").description("Entity types to blast.").build());
      this.add(new IntSetting("hurt-time", "Hurt Time", 10, 0, 10, 1).description("Maximum hurt time.").build());
      this.add(
         new ChoiceSetting("activation", "Activation", "Always", "Always", "Hold", "Press")
            .description("Always run, hold a key, or toggle with a key press.")
            .group("Engagement")
            .build()
      );
      this.add(
         new KeybindSetting("activation-key", "Activation Key", -1)
            .description("Key used by Hold or Press activation.")
            .group("Engagement")
            .visibleWhen(() -> !"Always".equals(this.choice("activation")))
            .build()
      );
      this.add(
         new BoolSetting("stop-on-kill", "Stop On Kill", false).description("Pause before acquiring another target after a kill.").group("Engagement").build()
      );
      this.add(
         new IntSetting("stop-delay", "Stop Delay", 500, 0, 8000, 50)
            .description("Pause after a confirmed kill.")
            .unit("ms")
            .group("Engagement")
            .visibleWhen(() -> this.bool("stop-on-kill"))
            .build()
      );
      this.add(new BoolSetting("own-only", "Own Anchors Only", false).description("Adopt only anchors this module placed.").group("Engagement").build());
      this.add(new IntSetting("switch-back-delay", "Switch Back Delay", 20, 1, 100, 1).description("Idle ticks before switching back.").group("Place").build());
      this.add(new BoolSetting("only-above", "Only Above", false).description("Place above target.").group("Place").build());
      this.add(new BoolSetting("force-engage", "Force Engage", false).description("Engage anyway when nothing passes the damage gates.").group("Place").build());
      this.add(
         new BoolSetting("support-blocks", "Support Blocks", true)
            .description("Build a real obsidian support before placing an airborne anchor.")
            .group("Place")
            .build()
      );
      this.add(
         new DoubleSetting("min-target-damage", "Min Target Damage", 5.0, 0.0, 20.0, 0.5)
            .description("Minimum blast damage to target.")
            .group("Damage")
            .build()
      );
      this.add(
         new DoubleSetting("max-self-damage", "Max Self Damage", 4.0, 0.0, 20.0, 0.5).description("Maximum blast damage to self.").group("Damage").build()
      );
      this.add(new BoolSetting("efficient", "Efficient", true).description("Require efficient trades.").group("Damage").build());
      this.add(new BoolSetting("terrain", "Terrain", true).description("Model terrain damage.").group("Damage").build());
      this.add(
         new BoolSetting("glowstone-shield", "Blast Protection", true)
            .description("Build verified cover before charging when it improves the damage trade.")
            .group("Damage")
            .build()
      );
      this.add(
         new ChoiceSetting("cover-material", "Cover Material", "Adaptive", "Adaptive", "Durable", "Glowstone")
            .description("Adaptive uses obsidian when the next anchor still has a clear placement face. Otherwise it prefers glowstone.")
            .group("Damage")
            .visibleWhen(() -> this.bool("glowstone-shield"))
            .build()
      );
      this.add(
         new IntSetting("step-delay", "Default Step Delay", 0, 0, 1000, 10)
            .description("Fallback floor for stages without a custom delay.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(
         new IntSetting("place-delay", "Place Delay", 0, 0, 1000, 10)
            .description("Floor between anchor and support placements.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(new IntSetting("charge-delay", "Charge Delay", 0, 0, 1000, 10).description("Floor between charge clicks.").unit("ms").group("Timing").build());
      this.add(new IntSetting("detonate-delay", "Detonate Delay", 0, 0, 1000, 10).description("Floor between detonations.").unit("ms").group("Timing").build());
      this.add(
         new IntSetting("switch-delay", "Switch Delay", 0, 0, 1000, 10)
            .description("Extra settling time after a hotbar change.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(
         new ChoiceSetting("detonate-slot", "Detonate Slot", "Auto", "Auto", "1", "2", "3", "4", "5", "6", "7", "8", "9")
            .description("Hotbar slot for the trigger click; Auto picks any non-fuel slot.")
            .group("Timing")
            .build()
      );
      this.add(new IntSetting("step-jitter", "Step Jitter", 0, 0, 200, 5).description("Random extra delay per click.").unit("ms").group("Timing").build());
      this.add(new BoolSetting("preview", "Placement Preview", false).description("Show pending anchor, support, and shield cells.").group("Preview").build());
      this.add(
         new ColorSetting("preview-color", "Preview Color", 1728031283)
            .description("Fill and outline for placement previews.")
            .group("Preview")
            .visibleWhen(() -> this.bool("preview"))
            .build()
      );
   }

   @Override
   public void onEnable() {
      this.init_V_2();
   }

   @Override
   public void onDisable() {
      this.init_V_2();
      autismclient.util.aD.init("anchor-aura");
   }

   @Override
   public void onGameLeft() {
      this.init_V_2();
      if ("anchor-aura".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.load_V();
      }
   }

   private void init_V_2() {
      this.init_StateCache.init_V();
      this.init_PoolEntry.init();
      this.init_HashUtil.init();
      autismclient.util.EventBus.init("anchor-aura");
      this.init_BlockPos = null;
      this.init_J = 0L;
      this.load_J = 0L;
      this.set = 0;
      this.init_LivingEntity = null;
      this.put = -2147483648;
      this.add = -2147483648;
      this.init_NetHelper.init();
      this.init_TableIndex.init();
      this.init_JsonUtil.init();
      this.read_J = 0L;
      this.write_J = 0L;
      this.init_Set.clear();
      this.clear = -1;
      this.reset = -1;
      this.start = 0;
      this.stop = -2147483648;
      this.load_BlockPos = null;
      this.load_LivingEntity = null;
      this.init_TextFormat.init();
      this.init_Z = false;
      this.load_Z = false;
      this.valid = -2147483648;
      this.read_Z = false;
      this.size = -2147483648;
      this.read_BlockPos = null;
      this.write_BlockPos = null;
      this.value = 0;
      this.poll_BlockPos = null;
      this.update_Z = false;
      this.push_BlockPos = null;
      this.pop_BlockPos = null;
      this.entry = 0;
      this.node = 0;
      this.flush_BlockPos = null;
      this.close_BlockPos = null;
      this.open_BlockPos = null;
      this.buffer = -2147483648;
      this.task = 0;
      this.read_V();
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Str = "";
      autismclient.util.ah.update_V("anchor-aura");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return "anchor-aura".equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z();
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && "anchor-aura".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.init_V_Str_LocalPlayer("anchor-aura", MC.player);
      }
   }

   @Override
   public String info() {
      if (!this.read_Str.isEmpty()) {
         return this.read_Str;
      }

      LivingEntity current = this.init_LivingEntity;
      return current == null ? "" : current.getName().getString();
   }

   public LivingEntity init_LivingEntity() {
      return this.init_LivingEntity;
   }

   LivingEntity load_LivingEntity() {
      int age = ck.init_ck().state_I() - this.add;
      return this.isEnabled() && age >= 0 && age <= 2 && this.flush() ? this.init_LivingEntity : null;
   }

   public static double init_D() {
      return al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.set > 0 ? aura.init_J / aura.set / 1000.0 : 0.0;
   }

   public static double load_D() {
      return al.init_Module_Str("anchor-aura") instanceof BufferHelper aura ? aura.load_J / 1000.0 : 0.0;
   }

   public static List<FileCache> init_List() {
      if (al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled() && aura.bool("preview")) {
         int color = am.init_I_Module(aura, "preview-color", 1728031283);
         List<FileCache> boxes = new ArrayList<>(3);
         BlockPos anchor = aura.update_BlockPos != null ? aura.update_BlockPos : aura.load_BlockPos;
         BlockPos support = aura.push_BlockPos != null ? aura.push_BlockPos : aura.pop_BlockPos;
         BlockPos shield = aura.sync_BlockPos != null ? aura.sync_BlockPos : aura.poll_BlockPos;
         int tick = ck.init_ck().state_I();
         if (anchor == null && support == null && shield == null && tick - aura.buffer < 0) {
            anchor = aura.flush_BlockPos;
            support = aura.close_BlockPos;
            shield = aura.open_BlockPos;
         }

         if (anchor != null) {
            boxes.add(new FileCache(new AABB(anchor), color));
         }

         if (support != null) {
            boxes.add(new FileCache(new AABB(support), color));
         }

         if (shield != null) {
            boxes.add(new FileCache(new AABB(shield), color));
         }

         return boxes;
      } else {
         return List.of();
      }
   }

   public static boolean init_Z_2() {
      if (al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled()) {
         int age = ck.init_ck().state_I() - aura.put;
         return age >= 0 && age <= 5;
      } else {
         return false;
      }
   }

   public static boolean read_Z() {
      return al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled() ? aura.add == ck.init_ck().state_I() : false;
   }

   public static boolean init_Z_I(int slot) {
      return al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled() ? aura.clear >= 0 && aura.reset == slot : false;
   }

   public static boolean init_Z_BlockPos(BlockPos cell) {
      if (cell == null) {
         return false;
      } else {
         return al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled()
            ? cell.equals(aura.update_BlockPos)
               || cell.equals(aura.load_BlockPos)
               || cell.equals(aura.sync_BlockPos)
               || cell.equals(aura.poll_BlockPos)
               || cell.equals(aura.push_BlockPos)
               || cell.equals(aura.pop_BlockPos)
            : false;
      }
   }

   @Override
   public boolean write_Z_10() {
      return this.bool("move-fix");
   }

   @Override
   public boolean update_Z_9() {
      return !this.bool("silent");
   }

   @Override
   public boolean load_Z_23() {
      boolean enabled = this.isEnabled();
      return !cn.init_Z() && (autismclient.util.aD.init_Z() || enabled && this.flush());
   }

   private boolean sync() {
      if (System.nanoTime() < this.write_J) {
         return false;
      }

      int bind = this.integer("activation-key");
      boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      return this.init_JsonUtil.init(this.choice("activation"), down);
   }

   private void init_V_LivingEntity(LivingEntity previous) {
      if (previous != null
         && (!previous.isAlive() || previous.isRemoved() || !(autismclient.util.S.init_D_LivingEntity(previous) > 0.0))
         && this.bool("stop-on-kill")) {
         this.write_J = System.nanoTime() + this.integer("stop-delay") * 1000000L;
         this.init_JsonUtil.init("Press");
      }
   }

   private boolean flush() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && !MC.player.isSecondaryUseActive()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !aD.init_Z()
         && !aC.init_Z()
         && !cd.init_Z()
         && (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
         && !MacroExecutor.write_Z()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !HashUtil.load_Z()
         && !cn.init_Z()
         && !aI.update_Z_5()
         && !FieldCache.init_Z_6()
         && !TableIndex.init_Z_20();
   }

   private static boolean close() {
      return ListPool.init_Z_10() || aK.init_Z_27() || t.flush() && t.init_Z_51();
   }

   private boolean init_Z_BufferHelper_Cache(BufferHelper.Inner_CachePool plan) {
      return plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.update
         ? true
         : plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.write && this.init_Z && plan.init_BlockPos().equals(this.load_BlockPos);
   }

   @Override
   public void preMovementTick() {
      long profileStarted = System.nanoTime();

      try {
         if (AutismLiteVariant.enabled()) {
            return;
         }

         if (MC == null || MC.player == null || MC.level == null) {
            return;
         }

         this.init_PoolEntry.init(MC.level, MC.player, MC.getConnection(), () -> {
            this.init_V_2();
            if ("anchor-aura".equals(autismclient.util.aD.init_Str())) {
               autismclient.util.aD.load_V();
            }
         });
         int tick = ck.init_ck().state_I();
         this.read_V_I(tick);
         this.write_V_2();
         this.load_V_I(tick);
         if (!this.flush()) {
            this.init_LivingEntity = null;
            this.read_V();
            this.load_V_2();
            return;
         }

         this.init_V_LivingEntity(this.init_LivingEntity);
         if (!this.sync()) {
            this.init_LivingEntity = null;
            this.read_V();
            this.load_V_2();
            return;
         }

         LivingEntity enemy = this.read_LivingEntity();
         if (enemy != null) {
            this.init_TableIndex.init(enemy, tick);
         }

         if (enemy == null || enemy != this.init_LivingEntity) {
            this.task = 0;
         }

         this.init_LivingEntity = enemy;
         if (enemy == null) {
            this.read_V();
         }

         double range = MC.player.blockInteractionRange();
         this.init_BlockPos = null;
         if ("Aimed".equals(this.choice("aim-mode")) && autismclient.util.UiHelper.init() instanceof BlockHitResult hit) {
            BlockState pointed = MC.level.getBlockState(hit.getBlockPos());
            this.init_BlockPos = !(pointed.getBlock() instanceof RespawnAnchorBlock) && !pointed.canBeReplaced()
               ? hit.getBlockPos().relative(hit.getDirection())
               : hit.getBlockPos();
         }

         autismclient.util.U.Inner_StreamUtil budget = new autismclient.util.U.Inner_StreamUtil(48);
         this.update_V_2();
         boolean higherRung = close();
         boolean caPrime = t.flush();
         boolean caCommitted = caPrime && t.sync_Z_11();
         if (caPrime) {
            this.read_V();
         }

         BufferHelper.Inner_CachePool plan;
         try (autismclient.util.S.Inner_ConfigStore pass = autismclient.util.S.init_S_ConfigStore()) {
            plan = this.init_BufferHelper_Cache_I(tick, range, budget);
            if (plan == null && this.load_BlockPos == null && enemy != null && !caPrime && this.sync()) {
               plan = this.init(enemy, tick, range, budget, higherRung || caCommitted);
            }
         }

         if (plan == null) {
            if (!higherRung
               && !caPrime
               && !t.close()
               && (this.load_BlockPos != null || this.bool("repeat"))
               && this.init_StateCache.init("anchor-aura", 20, MC.player, this.init_Obj(), this.init_ce_DataUtil())) {
               this.read_Str = "waiting";
               return;
            }

            this.load_V_2();
            return;
         }

         this.read_Str = plan.init_BufferHelper_Confi().name().toLowerCase(Locale.ROOT);
         boolean finishing = this.init_Z_BufferHelper_Cache(plan);
         boolean mayAct = !higherRung && !caCommitted || finishing;
         if (mayAct) {
            this.put = tick;
         }

         boolean releasedToDefend = mayAct && !finishing && !this.init_Z_BufferHelper_Confi(plan.init_BufferHelper_Confi()) && t.close();
         if (mayAct && !releasedToDefend) {
            this.add = tick;
         }

         autismclient.util.ah.update_Z("anchor-aura");
         ce.Inner_DataUtil wire = this.init_ce_DataUtil();
         boolean allowed = wire != null && this.init_Z_BufferHelper_Confi(plan.init_BufferHelper_Confi()) && mayAct;
         boolean slotReady = allowed && this.init_Z_I_Z(plan.init_I(), finishing);
         boolean finishSwitchRefused = finishing && allowed && !slotReady && MC.player.getInventory().getSelectedSlot() != plan.init_I();
         boolean useStep = plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.write
            || plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.update;
         BlockHitResult alignedUse = wire != null && useStep ? this.init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(plan, wire) : null;
         BlockHitResult hit = slotReady ? (alignedUse != null ? alignedUse : this.init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(plan, wire)) : null;
         if (mayAct && !releasedToDefend && !finishSwitchRefused) {
            boolean pinCommittedFire = hit != null && (higherRung || caCommitted);
            boolean pin = hit != null || alignedUse != null;
            this.init_StateCache
               .init(
                  "anchor-aura",
                  pinCommittedFire ? 30 : 20,
                  MC.player,
                  this.init_Obj(),
                  this.decimal("rotation-speed-multiplier"),
                  pin ? wire : plan.init_U_CachePool().init_U_DataUtil().init_ce_DataUtil(),
                  pin
               );
         }

         if (hit == null) {
            return;
         }

         ce.Inner_DataUtil outgoing = cn.init_ce_DataUtil(MC.player);
         if (outgoing == null || !init_Z_ce_DataUtil_ce_DataUtil(outgoing, wire)) {
            return;
         }

         this.init_V_BufferHelper_Cache(plan, hit, tick);
      } finally {
         long elapsed = System.nanoTime() - profileStarted;
         this.init_J += elapsed;
         this.load_J = Math.max(this.load_J, elapsed);
         this.set++;
      }
   }

   private static boolean init_Z_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
      return Math.abs(ce.init(first.init(), second.init())) <= 0.05F && Math.abs(first.load() - second.load()) <= 0.05F;
   }

   private void load_V_2() {
      this.init_StateCache.init_V();
      this.read_Str = "";
      if (this.load_BlockPos == null) {
         autismclient.util.ah.write_V("anchor-aura");
         autismclient.util.ah.load_V("anchor-aura");
      }

      if ("anchor-aura".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.init("anchor-aura");
         autismclient.util.aD.init_V_Str_LocalPlayer("anchor-aura", MC.player);
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil() {
      ch.Inner_CachePool snapshot = ch.init_ch_CachePool();
      return !snapshot.init_Z() ? null : new ce.Inner_DataUtil(snapshot.read(), snapshot.write());
   }

   private ce.Inner_DataUtil load_ce_DataUtil() {
      ce.Inner_DataUtil wire = this.init_ce_DataUtil();
      return wire != null ? wire : ce.init(MC.player);
   }

   private boolean init_Z_BufferHelper_Confi(BufferHelper.Inner_ConfigStore step) {
      int tick = ck.init_ck().state_I();
      return this.init_NetHelper.init_Z_Str(step.name(), tick, System.nanoTime());
   }

   private void init_V_BufferHelper_Confi(BufferHelper.Inner_ConfigStore step) {
      int tick = ck.init_ck().state_I();
      this.init_NetHelper
         .init_V_Str(
            step.name(),
            ck.init_ck().state_I(),
            System.nanoTime(),
            autismclient.util.NetHelper.init_J_I(this.init_I_BufferHelper_Confi(step), this.integer("step-jitter"), this.init_Random)
         );
      this.start = this.integer("switch-back-delay");
   }

   private int init_I_BufferHelper_Confi(BufferHelper.Inner_ConfigStore step) {
      String var10000;
      switch (step) {
         case init:
         case load:
         case read:
            var10000 = "place-delay";
            break;
         case write:
            var10000 = "charge-delay";
            break;
         case update:
            var10000 = "detonate-delay";
            break;
         default:
            throw new MatchException(null, null);
      }

      String id = var10000;
      int configured = this.integer(id);
      return configured == 75 ? this.integer("step-delay") : configured;
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      if (this.init_NetHelper.init(ck.init_ck().state_I())) {
         return true;
      }

      if (this.load_BlockPos != null && this.flush() && hitResult instanceof BlockHitResult hit) {
         ItemStack held = MC.player.getItemInHand(hand);
         if (!init_Z_ItemStack(held) && !load_Z_ItemStack(held) && !read_Z_ItemStack(held)) {
            return false;
         }

         BlockPos cell = hit.getBlockPos();
         return (cell.equals(this.load_BlockPos) || cell.equals(this.pop_BlockPos) || cell.equals(this.poll_BlockPos))
            && MC.level.getBlockState(cell) != autismclient.util.IoWorker.init(MC.level, cell);
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && "anchor-aura".equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z();
   }

   private BufferHelper.Inner_CachePool init_BufferHelper_Cache_I(int tick, double range, autismclient.util.U.Inner_StreamUtil budget) {
      BlockPos cell = this.load_BlockPos;
      if (cell == null) {
         return null;
      }

      if (this.init_TextFormat.init(MC.level)) {
         return null;
      }

      BlockState state = MC.level.getBlockState(cell);
      if (state.getBlock() instanceof RespawnAnchorBlock && !(autismclient.util.IoWorker.init(MC.level, cell).getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      boolean anchor = state.getBlock() instanceof RespawnAnchorBlock;
      if (this.pop_BlockPos != null) {
         if (MC.level.getBlockState(this.pop_BlockPos).canBeReplaced()) {
            if (!this.init_Z && this.entry < 3) {
               return this.init_BufferHelper_Cache_BlockPos_2(this.load_BlockPos, this.pop_BlockPos, range, budget);
            }

            this.init_V_Z(true);
            return null;
         }

         if (autismclient.util.IoWorker.init(MC.level, this.pop_BlockPos).canBeReplaced()) {
            return null;
         }

         this.pop_BlockPos = null;
      }

      if (!anchor) {
         if (this.load_Z) {
            this.init_V_Z(false);
            return null;
         } else if (this.bool("place") && !this.init_Z && this.data < 3) {
            this.read_Z = false;
            this.size = -2147483648;
            return this.init_BufferHelper_Cache_BlockPos(cell, range, budget);
         } else {
            this.init_V_Z(true);
            return null;
         }
      } else {
         int charge = state.getValue(RespawnAnchorBlock.CHARGE);
         if (this.poll_BlockPos != null
            && !MC.level.getBlockState(this.poll_BlockPos).canBeReplaced()
            && autismclient.util.IoWorker.init(MC.level, this.poll_BlockPos).canBeReplaced()) {
            return null;
         }

         if (this.poll_BlockPos != null && MC.level.getBlockState(this.poll_BlockPos).canBeReplaced()) {
            if (!this.init_Z && this.node < 3 && this.init_Z_Z(this.update_Z)) {
               BufferHelper.Inner_CachePool shield = this.load(this.poll_BlockPos, range, budget);
               if (charge <= 0 || shield != null && !this.sync(cell)) {
                  return shield;
               }

               this.poll_BlockPos = null;
               this.update_Z = false;
            } else {
               if (charge <= 0 || autismclient.util.IoWorker.init(MC.level, cell).getValue(RespawnAnchorBlock.CHARGE) <= 0) {
                  this.init_V_Z(true);
                  return null;
               }

               this.poll_BlockPos = null;
               this.update_Z = false;
            }
         }

         if (charge <= 0) {
            if (!this.poll(cell)) {
               if (this.poll_BlockPos != null && MC.level.getBlockState(this.poll_BlockPos) == autismclient.util.IoWorker.init(MC.level, this.poll_BlockPos)) {
                  this.init_V_Z(true);
               }

               return null;
            } else if (!this.bool("charge")) {
               return null;
            } else {
               return this.read_Z && tick - this.size < init_I()
                  ? null
                  : this.init_BufferHelper_Cache_BlockPos(cell, BufferHelper.Inner_ConfigStore.write, range, budget);
            }
         } else if (this.bool("detonate") && this.init_Z_BlockPos_BlockState(cell, state)) {
            return this.load_Z && tick - this.valid < init_I()
               ? null
               : this.init_BufferHelper_Cache_BlockPos(cell, BufferHelper.Inner_ConfigStore.update, range, budget);
         } else {
            return null;
         }
      }
   }

   private BufferHelper.Inner_CachePool init_BufferHelper_Cache_LivingEntity(
      LivingEntity enemy, int tick, double range, autismclient.util.U.Inner_StreamUtil budget
   ) {
      BlockPos adopted = this.init_BlockPos_LivingEntity_D(enemy, range);
      if (adopted != null) {
         BlockState state = MC.level.getBlockState(adopted);
         BufferHelper.Inner_ConfigStore step = state.getValue(RespawnAnchorBlock.CHARGE) > 0
            ? BufferHelper.Inner_ConfigStore.update
            : BufferHelper.Inner_ConfigStore.write;
         boolean usable = step == BufferHelper.Inner_ConfigStore.update
            ? this.bool("detonate") && this.init_Z_BlockPos_BlockState(adopted, state)
            : this.open();
         boolean refused = true;
         if (usable) {
            autismclient.util.U.Inner_JsonUtil[] outcome = new autismclient.util.U.Inner_JsonUtil[1];
            BufferHelper.Inner_CachePool plan = this.init(adopted, step, range, budget, outcome);
            if (plan != null) {
               this.init_V_BlockPos(adopted, tick, null, false);
               return plan;
            }

            refused = outcome[0] != autismclient.util.U.Inner_JsonUtil.push || this.load_Z_BlockPos(adopted);
         }

         if (refused) {
            this.init_Map.put(adopted, tick + 40);
         }
      }

      if (!this.bool("place")) {
         return null;
      }

      if (!this.open()) {
         return null;
      }

      List<BlockPos> cells = this.init_List_LivingEntity_D(enemy, range);
      if (cells.isEmpty()) {
         if (this.bool("glowstone-shield")) {
            BufferHelper.Inner_CachePool shielded = this.load(enemy, range, budget);
            if (shielded != null) {
               return shielded;
            }
         }

         if (this.task < 3) {
            this.task++;
         }

         if (this.bool("force-engage") && this.task >= 3) {
            cells = this.load(enemy, range);
         }
      } else {
         this.task = 0;
      }

      if (cells.isEmpty() && this.bool("support-blocks")) {
         BufferHelper.Inner_CachePool supported = this.init_BufferHelper_Cache_LivingEntity(enemy, range, budget);
         if (supported != null) {
            return supported;
         }
      }

      if (cells.isEmpty()) {
         return null;
      }

      autismclient.util.U.Inner_JsonUtil[] outcome = new autismclient.util.U.Inner_JsonUtil[1];
      int start = this.init_I_List(cells);

      for (int step = 0; step < cells.size(); step++) {
         int index = start + step;
         if (index >= cells.size()) {
            index -= cells.size();
         }

         BlockPos cell = cells.get(index);
         outcome[0] = null;
         BlockPos cover = step == 0 && this.bool("glowstone-shield")
            ? this.init_BlockPos_LivingEntity(enemy, cell, this.decimal("min-target-damage"), this.decimal("max-self-damage"))
            : null;
         BufferHelper.Inner_CachePool plan = this.init(cell, cover, cover != null && this.init_I_BlockPos_BlockPos(cover, cell) == 1, range, budget, outcome);
         if (plan != null) {
            this.read_BlockPos = cell;
            return plan;
         }

         if (outcome[0] == autismclient.util.U.Inner_JsonUtil.push) {
            this.read_BlockPos = cell;
            return null;
         }

         this.read_BlockPos = cells.get(index + 1 == cells.size() ? 0 : index + 1);
      }

      return this.bool("support-blocks") ? this.init_BufferHelper_Cache_LivingEntity(enemy, range, budget) : null;
   }

   private int init_I_List(List<BlockPos> cells) {
      if (this.read_BlockPos == null) {
         return 0;
      }

      int index = cells.indexOf(this.read_BlockPos);
      return index < 0 ? 0 : index;
   }

   private boolean load_Z_BlockPos(BlockPos cell) {
      if (cell.equals(this.write_BlockPos)) {
         this.value++;
      } else {
         this.write_BlockPos = cell.immutable();
         this.value = 1;
      }

      if (this.value < 3) {
         return false;
      }

      this.write_BlockPos = null;
      this.value = 0;
      return true;
   }

   private BufferHelper.Inner_CachePool init(LivingEntity enemy, int tick, double range, autismclient.util.U.Inner_StreamUtil budget, boolean hotbarOwnedAbove) {
      if (this.update_BlockPos != null) {
         boolean valid = this.bool("place")
            && enemy == this.read_LivingEntity
            && !autismclient.util.ah.init(this.cache, "anchor-aura")
            && (
               this.push_BlockPos != null
                  ? read_Z_ItemStack(MC.player.getInventory().getItem(this.cache))
                     && this.init_Z_BlockPos_AABB(this.update_BlockPos, this.init_AABB())
                     && this.init_Z_BlockState(Blocks.OBSIDIAN.defaultBlockState(), this.push_BlockPos, this.init_AABB())
                  : init_Z_ItemStack(MC.player.getInventory().getItem(this.cache))
                     && this.init_Z_BlockState(Blocks.RESPAWN_ANCHOR.defaultBlockState(), this.update_BlockPos, this.init_AABB())
            )
            && (
               this.sync_BlockPos == null
                  || this.init_Z_Z(this.write_Z) && this.init_Z_BlockState(init_BlockState_Z(this.write_Z), this.sync_BlockPos, this.init_AABB())
            );
         valid = valid && this.init_Z_LivingEntity(enemy, this.update_BlockPos, this.push_BlockPos, this.sync_BlockPos);
         if (valid) {
            if (hotbarOwnedAbove) {
               this.state = tick + this.load_I();
            }

            if (tick - this.state < 0) {
               BufferHelper.Inner_CachePool held = this.push_BlockPos != null
                  ? this.init_BufferHelper_Cache_BlockPos_2(this.update_BlockPos, this.push_BlockPos, range, budget)
                  : this.init(this.update_BlockPos, this.sync_BlockPos, this.write_Z, range, budget, null);
               if (held != null) {
                  this.queue = 0;
                  return held;
               }

               if (++this.queue < 3) {
                  return null;
               }
            }

            this.init_Map.put(this.update_BlockPos, tick + 10);
         }

         this.read_V();
      }

      BufferHelper.Inner_CachePool plan = this.init_BufferHelper_Cache_LivingEntity(enemy, tick, range, budget);
      if (plan != null
         && (plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.load || plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.init)) {
         this.update_BlockPos = plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.init ? plan.read() : plan.init_BlockPos();
         this.push_BlockPos = plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.init ? plan.init_BlockPos() : null;
         this.sync_BlockPos = plan.load();
         this.write_Z = plan.init_Z();
         this.cache = plan.init_I();
         this.read_LivingEntity = enemy;
         this.state = tick + this.load_I();
      }

      return plan;
   }

   private boolean init_Z_LivingEntity(LivingEntity enemy, BlockPos cell, BlockPos support, BlockPos shield) {
      if (enemy != null && enemy.isAlive() && this.read_Z_BlockPos(cell)) {
         autismclient.util.S.Inner_CachePool verify = this.init_S_CachePool_BlockPos(cell);
         if (shield != null || support != null) {
            verify = verify.init(shield != null ? shield : support);
         }

         autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(
            enemy, init_Vec3_BlockPos(cell), verify, this.init_AABB_LivingEntity(enemy)
         );
         return this.init_Z_S_BufferHelper(rank)
            ? true
            : shield == null
               && this.bool("force-engage")
               && this.task >= 3
               && this.init_Z_S_BufferHelper(
                  autismclient.util.TableIndex.init(enemy, init_Vec3_BlockPos(cell), 5.0F, verify, autismclient.util.TableIndex.init(enemy))
               )
               && (!this.bool("protect-friends") || autismclient.util.UiHelper.init(init_Vec3_BlockPos(cell), 5.0F, verify, this.decimal("max-friend-damage")));
      } else {
         return false;
      }
   }

   private void read_V() {
      this.update_BlockPos = null;
      this.cache = -1;
      this.read_LivingEntity = null;
      this.state = 0;
      this.queue = 0;
      this.sync_BlockPos = null;
      this.write_Z = false;
      this.push_BlockPos = null;
   }

   private boolean open() {
      if (!this.bool("charge") || !this.bool("detonate")) {
         return false;
      } else if (this.init_I_Predicate(BufferHelper::load_Z_ItemStack) < 0) {
         return false;
      } else {
         return load_Z_ItemStack(MC.player.getOffhandItem()) ? false : this.write_I() >= 0;
      }
   }

   private int load_I() {
      return autismclient.util.NetHelper.init_I_I(
         this.init_I_BufferHelper_Confi(BufferHelper.Inner_ConfigStore.load), this.integer("switch-delay"), autismclient.util.macro.ab.init_I()
      );
   }

   private int init_I_Z(boolean shielded) {
      return autismclient.util.NetHelper.init(
         this.init_I_BufferHelper_Confi(BufferHelper.Inner_ConfigStore.load),
         this.init_I_BufferHelper_Confi(BufferHelper.Inner_ConfigStore.write),
         this.init_I_BufferHelper_Confi(BufferHelper.Inner_ConfigStore.update),
         this.integer("switch-delay"),
         autismclient.util.macro.ab.init_I(),
         shielded
      );
   }

   private void init_V_BlockPos(BlockPos anchorCell, BlockPos supportCell, int tick) {
      this.init_V_BlockPos(anchorCell, tick, null, false);
      this.pop_BlockPos = supportCell.immutable();
      this.entry = 0;
      this.next = tick + this.init_I_Z(true);
   }

   private void init_V_BlockPos(BlockPos cell, int tick, BlockPos shield, boolean shieldObsidian) {
      this.read_V();
      this.load_BlockPos = cell.immutable();
      this.load_LivingEntity = this.init_LivingEntity;
      this.init_TextFormat.init();
      this.poll_BlockPos = shield;
      this.update_Z = shieldObsidian;
      this.node = 0;
      this.next = tick + this.init_I_Z(shield != null);
      this.init_Z = false;
      this.load_Z = false;
      this.valid = -2147483648;
      this.read_Z = false;
      this.size = -2147483648;
      this.data = 0;
      this.task = 0;
      this.read_BlockPos = null;
   }

   private void load_V_I(int tick) {
      if (this.load_BlockPos != null) {
         if (!this.init_Z) {
            if (tick - this.next > 0) {
               this.init_Z = true;
               this.prev = tick + this.init_I_Z(this.poll_BlockPos != null);
            }
         } else if (tick - this.prev > 0) {
            this.init_V_Z(true);
         }
      }
   }

   private void init_V_Z(boolean ban) {
      if (!ban && !this.bool("repeat")) {
         this.init_JsonUtil.init(this.choice("activation"));
      }

      BlockPos cell = this.load_BlockPos;
      this.init_StateCache.init(cell, !ban && this.bool("repeat") && this.init_LivingEntity == this.load_LivingEntity ? this.init_LivingEntity : null);
      if (cell != null && ban) {
         this.init_Map.put(cell, ck.init_ck().state_I() + 40);
      }

      this.load_BlockPos = null;
      this.load_LivingEntity = null;
      this.init_TextFormat.init();
      this.poll_BlockPos = null;
      this.update_Z = false;
      this.push_BlockPos = null;
      this.pop_BlockPos = null;
      this.entry = 0;
      this.node = 0;
      this.init_Z = false;
      this.load_Z = false;
      this.valid = -2147483648;
      this.read_Z = false;
      this.size = -2147483648;
      autismclient.util.ah.load_V("anchor-aura");
      autismclient.util.ah.write_V("anchor-aura");
   }

   private void read_V_I(int tick) {
      if (!this.init_Map.isEmpty()) {
         Iterator<Entry<BlockPos, Integer>> entries = this.init_Map.entrySet().iterator();

         while (entries.hasNext()) {
            if (tick - entries.next().getValue() >= 0) {
               entries.remove();
            }
         }
      }
   }

   private BufferHelper.Inner_CachePool init_BufferHelper_Cache_BlockPos(BlockPos cell, double range, autismclient.util.U.Inner_StreamUtil budget) {
      return this.init(cell, null, false, range, budget, null);
   }

   private BufferHelper.Inner_CachePool init(
      BlockPos cell,
      BlockPos shield,
      boolean shieldObsidian,
      double range,
      autismclient.util.U.Inner_StreamUtil budget,
      autismclient.util.U.Inner_JsonUtil[] outcome
   ) {
      if (!this.init_Z_BlockState(Blocks.RESPAWN_ANCHOR.defaultBlockState(), cell, this.init_AABB())) {
         return null;
      }

      int slot = this.init_I_Predicate(BufferHelper::init_Z_ItemStack);
      if (slot < 0) {
         return null;
      }

      ItemStack material = MC.player.getInventory().getItem(slot);
      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init(
         this.init_U_StateCache_BlockPos(cell, material, range).init_U_StateCache_U_StreamUtil(budget), outcome
      );
      return candidate == null
         ? null
         : new BufferHelper.Inner_CachePool(cell, BufferHelper.Inner_ConfigStore.load, slot, candidate, range, shield, shieldObsidian, null);
   }

   private BufferHelper.Inner_CachePool init_BufferHelper_Cache_BlockPos_2(
      BlockPos anchorCell, BlockPos supportCell, double range, autismclient.util.U.Inner_StreamUtil budget
   ) {
      if (this.init_Z_BlockPos_AABB(anchorCell, this.init_AABB()) && this.init_Z_BlockState(Blocks.OBSIDIAN.defaultBlockState(), supportCell, this.init_AABB())
         )
       {
         int slot = this.init_I_Predicate(BufferHelper::read_Z_ItemStack);
         if (slot < 0) {
            return null;
         }

         ItemStack material = MC.player.getInventory().getItem(slot);
         autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool(
            this.init_U_StateCache_BlockPos(supportCell, material, range).init_U_StateCache_U_StreamUtil(budget)
         );
         return candidate == null
            ? null
            : new BufferHelper.Inner_CachePool(supportCell, BufferHelper.Inner_ConfigStore.init, slot, candidate, range, null, false, anchorCell);
      } else {
         return null;
      }
   }

   private BufferHelper.Inner_CachePool init_BufferHelper_Cache_BlockPos(
      BlockPos cell, BufferHelper.Inner_ConfigStore step, double range, autismclient.util.U.Inner_StreamUtil budget
   ) {
      return this.init(cell, step, range, budget, null);
   }

   private BufferHelper.Inner_CachePool init(
      BlockPos cell,
      BufferHelper.Inner_ConfigStore step,
      double range,
      autismclient.util.U.Inner_StreamUtil budget,
      autismclient.util.U.Inner_JsonUtil[] outcome
   ) {
      int slot = step == BufferHelper.Inner_ConfigStore.write ? this.init_I_Predicate(BufferHelper::load_Z_ItemStack) : this.write_I();
      if (slot < 0) {
         return null;
      }

      autismclient.util.U.Inner_CachePool candidate = this.init_U_CachePool_BlockPos(cell, range, budget, outcome);
      return candidate == null ? null : new BufferHelper.Inner_CachePool(cell, step, slot, candidate, range, null, false, null);
   }

   private BufferHelper.Inner_CachePool load(BlockPos cell, double range, autismclient.util.U.Inner_StreamUtil budget) {
      boolean obsidian = this.update_Z;
      if (!this.init_Z_BlockState(init_BlockState_Z(obsidian), cell, this.init_AABB())) {
         return null;
      }

      Predicate<ItemStack> material = obsidian ? BufferHelper::read_Z_ItemStack : BufferHelper::load_Z_ItemStack;
      int slot = this.init_I_Predicate(material);
      if (slot < 0) {
         return null;
      }

      ItemStack stack = MC.player.getInventory().getItem(slot);
      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool(
         this.init_U_StateCache_BlockPos(cell, stack, range, this.init_U_NetHelper_BlockPos_ItemStack(this.load_BlockPos, stack))
            .init_U_StateCache_U_StreamUtil(budget)
      );
      return candidate == null
         ? null
         : new BufferHelper.Inner_CachePool(cell, BufferHelper.Inner_ConfigStore.read, slot, candidate, range, null, obsidian, null);
   }

   private autismclient.util.U.Inner_StateCache init_U_StateCache_BlockPos(BlockPos cell, ItemStack stack, double range) {
      return this.init_U_StateCache_BlockPos(cell, stack, range, autismclient.util.U.init(stack, MC.player, InteractionHand.MAIN_HAND));
   }

   private autismclient.util.U.Inner_StateCache init_U_StateCache_BlockPos(
      BlockPos cell, ItemStack stack, double range, autismclient.util.U.Inner_NetHelper placement
   ) {
      LocalPlayer player = MC.player;
      autismclient.util.U.Inner_StateCache request = new autismclient.util.U.Inner_StateCache(cell, player.getEyePosition(), range, placement)
         .init_U_StateCache_ce_DataUtil(this.load_ce_DataUtil())
         .init_U_StateCache_D(autismclient.util.U.init_D())
         .init_U_StateCache_Vec3(player.getEyePosition().add(player.getDeltaMovement()))
         .init_U_StateCache_Z(player.isSecondaryUseActive())
         .load(false);
      if (autismclient.util.w.write()) {
         request.init_U_StateCache_StringBuilder(this.init_StringBuilder = new StringBuilder());
      }

      return request;
   }

   private autismclient.util.U.Inner_StateCache init_U_StateCache_BlockPos_D(BlockPos anchor, double range) {
      LocalPlayer player = MC.player;
      autismclient.util.U.Inner_StateCache request = new autismclient.util.U.Inner_StateCache(
            anchor, player.getEyePosition(), range, new BufferHelper.Inner_DataUtil(anchor)
         )
         .init_U_StateCache_ce_DataUtil(this.load_ce_DataUtil())
         .init_U_StateCache_D(autismclient.util.U.init_D())
         .init_U_StateCache_Vec3(player.getEyePosition().add(player.getDeltaMovement()))
         .init_U_StateCache_Z(player.isSecondaryUseActive())
         .load(false);
      if (autismclient.util.w.write()) {
         request.init_U_StateCache_StringBuilder(this.init_StringBuilder = new StringBuilder());
      }

      return request;
   }

   private autismclient.util.U.Inner_NetHelper init_U_NetHelper_BlockPos_ItemStack(final BlockPos anchor, final ItemStack held) {
      final LocalPlayer player = MC.player;
      return new autismclient.util.U.Inner_NetHelper() {
         @Override
         public boolean lands(BlockHitResult hit, BlockPos cell) {
            if (hit != null && cell != null && held.getItem() instanceof BlockItem) {
               BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, held, hit);
               return context.canPlace() && context.getClickedPos().equals(cell);
            } else {
               return false;
            }
         }

         @Override
         public boolean init(BlockState state, BlockPos pos, boolean sneaking) {
            return !(state.getBlock() instanceof RespawnAnchorBlock)
               ? !autismclient.util.U.init(state, pos, sneaking)
               : anchor != null && anchor.equals(pos) && BufferHelper.read_Z_ItemStack(held) && state.getValue(RespawnAnchorBlock.CHARGE) <= 0;
         }
      };
   }

   private autismclient.util.U.Inner_CachePool init_U_CachePool_BlockPos(
      BlockPos anchor, double range, autismclient.util.U.Inner_StreamUtil budget, autismclient.util.U.Inner_JsonUtil[] outcome
   ) {
      BlockState state = MC.level.getBlockState(anchor);
      if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      if (!(autismclient.util.IoWorker.init(MC.level, anchor).getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      autismclient.util.U.Inner_StateCache request = this.init_U_StateCache_BlockPos_D(anchor, range).init_U_StateCache_U_StreamUtil(budget);
      Vec3 eye = MC.player.getEyePosition();
      autismclient.util.U.Inner_JsonUtil[] refusal = outcome != null ? outcome : new autismclient.util.U.Inner_JsonUtil[1];
      List<BufferHelper.Inner_NetHelper> aims = new ArrayList<>();

      for (Direction face : autismclient.util.U.init_DirectionArr) {
         for (AABB rect : autismclient.util.U.init_AABBArr_BlockState(state, anchor, face, 3)) {
            autismclient.util.U.Inner_TaskQueue option = new autismclient.util.U.Inner_TaskQueue(
               anchor,
               anchor,
               face,
               1,
               state,
               rect,
               init_D_AABB_Direction(rect, face),
               autismclient.util.U.init(rect, face, eye),
               false,
               new autismclient.util.U.Inner_BufferHelper(anchor, face)
            );
            autismclient.util.U.Inner_DataUtil aim = autismclient.util.U.init(option, request, refusal);
            if (aim != null) {
               aims.add(new BufferHelper.Inner_NetHelper(option, aim));
            }
         }
      }

      aims.sort(Comparator.comparingDouble(candidatex -> candidatex.init_U_DataUtil().init_F()));
      int start = this.init_HashUtil.init(aims, BufferHelper.Inner_NetHelper::init_U_TaskQueue);

      for (int step = 0; step < aims.size(); step++) {
         BufferHelper.Inner_NetHelper aim = aims.get((start + step) % aims.size());
         autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool_U_TaskQueue(
            aim.init_U_TaskQueue(), aim.init_U_DataUtil(), request, refusal
         );
         if (candidate != null) {
            this.init_HashUtil.init();
            return candidate;
         }

         if (refusal[0] == autismclient.util.U.Inner_JsonUtil.push) {
            this.init_HashUtil.init(aim.init_U_TaskQueue());
            return null;
         }
      }

      this.init_HashUtil.init();
      return null;
   }

   private static double init_D_AABB_Direction(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = autismclient.util.U.init(normal, true);
      Axis second = autismclient.util.U.init(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private BlockHitResult init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(BufferHelper.Inner_CachePool plan, ce.Inner_DataUtil wire) {
      LocalPlayer player = MC.player;
      autismclient.util.U.Inner_StateCache var10000;
      switch (plan.init_BufferHelper_Confi()) {
         case init:
         case load:
            var10000 = this.init_U_StateCache_BlockPos(plan.init_BlockPos(), player.getMainHandItem(), plan.init_D());
            break;
         case read:
            var10000 = this.init_U_StateCache_BlockPos(
               plan.init_BlockPos(),
               player.getMainHandItem(),
               plan.init_D(),
               this.init_U_NetHelper_BlockPos_ItemStack(this.load_BlockPos, player.getMainHandItem())
            );
            break;
         case write:
         case update:
            var10000 = this.init_U_StateCache_BlockPos_D(plan.init_BlockPos(), plan.init_D());
            break;
         default:
            throw new MatchException(null, null);
      }

      autismclient.util.U.Inner_StateCache gate = var10000;
      return autismclient.util.U.init_BlockHitResult_U_CachePool(plan.init_U_CachePool(), wire, player.getEyePosition(), plan.init_D(), gate);
   }

   private void init_V_BufferHelper_Cache(BufferHelper.Inner_CachePool plan, BlockHitResult hit, int tick) {
      if (this.load_Z_BufferHelper_Cache(plan)
         && !al.init_Z_HitResult(hit, InteractionHand.MAIN_HAND, "anchor-aura")
         && autismclient.util.EventBus.init(
            "anchor-aura",
            hit,
            InteractionHand.MAIN_HAND,
            () -> this.flush()
               && this.load_Z_BufferHelper_Cache(plan)
               && this.init_ce_DataUtil() != null
               && this.init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(plan, this.init_ce_DataUtil()) != null,
            () -> this.init_V_BufferHelper_Cache_I(plan, ck.init_ck().state_I())
         )
         && !bM.init("anchor-aura")) {
         autismclient.util.s.write_V();
      }
   }

   private void init_V_BufferHelper_Cache_I(BufferHelper.Inner_CachePool plan, int tick) {
      if (this.update_BlockPos != null
         || this.load_BlockPos != null
         || this.push_BlockPos != null
         || this.pop_BlockPos != null
         || this.sync_BlockPos != null
         || this.poll_BlockPos != null) {
         this.flush_BlockPos = this.update_BlockPos != null ? this.update_BlockPos : this.load_BlockPos;
         this.close_BlockPos = this.push_BlockPos != null ? this.push_BlockPos : this.pop_BlockPos;
         this.open_BlockPos = this.sync_BlockPos != null ? this.sync_BlockPos : this.poll_BlockPos;
         this.buffer = tick + 10;
      }

      this.init_V_BufferHelper_Confi(plan.init_BufferHelper_Confi());
      if (plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.init) {
         this.init_V_BlockPos(plan.read(), plan.init_BlockPos(), tick);
         this.entry++;
      }

      if (plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.load) {
         if (this.load_BlockPos == null || !this.load_BlockPos.equals(plan.init_BlockPos())) {
            this.init_V_BlockPos(plan.init_BlockPos(), tick, plan.load(), plan.init_Z());
         }

         this.data++;
         this.init_TextFormat.init(MC.level, ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler().currentSequence());
         this.init_Set.add(plan.init_BlockPos().immutable());

         while (this.init_Set.size() > 64) {
            this.init_Set.remove(this.init_Set.iterator().next());
         }
      }

      if (plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.read) {
         this.node++;
      }

      if (plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.write) {
         this.read_Z = true;
         this.size = tick;
      }

      if (plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.update) {
         this.load_Z = true;
         this.valid = tick;
      }

      this.init_StateCache.load(this.init_Obj(), tick, autismclient.util.macro.ab.init_I());
   }

   private Object init_Obj() {
      return this.load_BlockPos != null ? this.load_BlockPos : this.init_LivingEntity;
   }

   private boolean load_Z_BufferHelper_Cache(BufferHelper.Inner_CachePool plan) {
      if (!AutismLiteVariant.enabled()
         && (
            plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.init
               || plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.load
               || plan.init_BufferHelper_Confi() == BufferHelper.Inner_ConfigStore.read
         )
         && TaskTimer.init_Z_AABB_2(new AABB(plan.init_BlockPos()))) {
         return false;
      }

      BlockState state = MC.level.getBlockState(plan.init_BlockPos());
      boolean anchor = state.getBlock() instanceof RespawnAnchorBlock
         && autismclient.util.IoWorker.init(MC.level, plan.init_BlockPos()).getBlock() instanceof RespawnAnchorBlock;
      boolean var10000;
      switch (plan.init_BufferHelper_Confi()) {
         case init:
            var10000 = !anchor
               && read_Z_ItemStack(MC.player.getMainHandItem())
               && this.init_Z_BlockState(Blocks.OBSIDIAN.defaultBlockState(), plan.init_BlockPos(), this.init_AABB())
               && this.init_Z_BlockPos_AABB(plan.read(), this.init_AABB())
               && this.init_Z_LivingEntity(this.init_LivingEntity, plan.read(), plan.init_BlockPos(), null);
            break;
         case load:
            var10000 = !anchor
               && init_Z_ItemStack(MC.player.getMainHandItem())
               && this.init_Z_BlockState(Blocks.RESPAWN_ANCHOR.defaultBlockState(), plan.init_BlockPos(), this.init_AABB())
               && this.init_Z_LivingEntity(this.init_LivingEntity, plan.init_BlockPos(), null, plan.load());
            break;
         case read:
            var10000 = state.canBeReplaced()
               && this.init_Z_BlockState(init_BlockState_Z(plan.init_Z()), plan.init_BlockPos(), this.init_AABB())
               && (plan.init_Z() ? read_Z_ItemStack(MC.player.getMainHandItem()) : load_Z_ItemStack(MC.player.getMainHandItem()));
            break;
         case write:
            var10000 = !this.init_TextFormat.init(MC.level)
               && anchor
               && state.getValue(RespawnAnchorBlock.CHARGE) < 4
               && load_Z_ItemStack(MC.player.getMainHandItem())
               && this.poll(plan.init_BlockPos());
            break;
         case update:
            var10000 = !this.init_TextFormat.init(MC.level)
               && anchor
               && !load_Z_ItemStack(MC.player.getMainHandItem())
               && this.init_Z_BlockPos_BlockState(plan.init_BlockPos(), state);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean init_Z_BlockPos_BlockState(BlockPos cell, BlockState state) {
      if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
         return false;
      } else {
         int charge = state.getValue(RespawnAnchorBlock.CHARGE);
         BlockState received = autismclient.util.IoWorker.init(MC.level, cell);
         if (!(received.getBlock() instanceof RespawnAnchorBlock)
            || !autismclient.util.UiHelper.init_Z_I(charge, received.getValue(RespawnAnchorBlock.CHARGE), load_Z_ItemStack(MC.player.getOffhandItem()))) {
            return false;
         } else if (charge <= 0) {
            return false;
         } else if (load_Z_ItemStack(MC.player.getOffhandItem()) && charge < 4) {
            return false;
         } else if (load_Z_ItemStack(MC.player.getMainHandItem()) && this.write_I() < 0) {
            return false;
         } else {
            return !this.read_Z_BlockPos(cell) ? false : this.sync(cell) && this.close(cell);
         }
      }
   }

   public static void init_V_BlockPos_BlockState(BlockPos cell, BlockState state) {
      if (al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled() && cell.equals(aura.load_BlockPos)) {
         aura.init_TextFormat.init(MC.level, state.getBlock() instanceof RespawnAnchorBlock);
      }
   }

   public static void init_V_I(int sequence) {
      if (al.init_Module_Str("anchor-aura") instanceof BufferHelper aura && aura.isEnabled()) {
         aura.init_TextFormat.load(MC.level, sequence);
      }
   }

   private boolean read_Z_BlockPos(BlockPos pos) {
      Boolean works = MC.level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, pos);
      return works == null || !works;
   }

   private LivingEntity read_LivingEntity() {
      LocalPlayer player = MC.player;
      double range = this.decimal("target-range");
      AABB search = player.getBoundingBox().inflate(range);
      List<LivingEntity> found = MC.level
         .getEntitiesOfClass(
            LivingEntity.class,
            search,
            entity -> entity != player
               && entity.isAlive()
               && !entity.isSpectator()
               && this.init_Z_Entity(entity)
               && !ByteReader.load_Z(entity)
               && !aL.init(entity, "killaura")
               && autismclient.util.S.init_D_LivingEntity(entity) > 0.0
               && entity.hurtTime <= this.integer("hurt-time")
         );
      if (found.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      double rangeSq = range * range;
      LivingEntity best = null;
      double bestScore = 1.7976931348623157E308;
      String mode = this.choice("targeting");

      for (LivingEntity entity : found) {
         double distanceSq = init_D_AABB_Vec3(entity.getBoundingBox(), eye);
         if (!(distanceSq > rangeSq)) {
            double score = this.init_D_Str(mode, entity, distanceSq, eye);
            if (score < bestScore) {
               bestScore = score;
               best = entity;
            }
         }
      }

      LivingEntity incumbent = this.init_LivingEntity;
      if (incumbent != null && incumbent != best && found.contains(incumbent)) {
         double grace = range + 0.5;
         double incumbentDistanceSq = init_D_AABB_Vec3(incumbent.getBoundingBox(), eye);
         if (incumbentDistanceSq <= grace * grace
            && (best == null || !autismclient.util.RingBuffer.init(mode, bestScore, this.init_D_Str(mode, incumbent, incumbentDistanceSq, eye)))) {
            return incumbent;
         }
      }

      return best;
   }

   private double init_D_Str(String mode, LivingEntity entity, double distanceSq, Vec3 eye) {
      double var10000;
      switch (mode) {
         case "HP":
            var10000 = autismclient.util.S.init_D_LivingEntity(entity);
            break;
         case "FOV":
            var10000 = ce.load(ce.init(MC.player), ce.init(entity.getBoundingBox().getCenter(), eye));
            break;
         default:
            var10000 = distanceSq;
      }

      return var10000;
   }

   private boolean init_Z_Entity(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.init_Set();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> init_Set() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.load_Str)) {
         return this.load_Set;
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

      this.load_Str = source;
      this.load_Set = Set.copyOf(normalized);
      return this.load_Set;
   }

   private List<BlockPos> init_List_LivingEntity_D(LivingEntity enemy, double range) {
      boolean efficient = this.bool("efficient");
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = init_IArr_I(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double damageFloor = this.decimal("min-target-damage");
      double rangeSq = range * range;
      AABB nextBox = this.init_AABB();
      AABB lead = this.init_AABB_LivingEntity(enemy);
      List<BufferHelper.Inner_StreamUtil> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.init_Map.containsKey(cursor) && this.flush(cursor) && this.read_Z_BlockPos(cursor) && this.init_Z_BlockState(anchor, cursor, nextBox)) {
            Vec3 centre = init_Vec3_BlockPos(cursor);
            if (!this.bool("only-above") || !(centre.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.S.load_D(enemy, centre, 5.0F, this.init_S_CachePool_BlockPos(cursor).init(lead));
                  if (!(bound < damageFloor)) {
                     candidates.add(new BufferHelper.Inner_StreamUtil(cursor.immutable(), centre, bound, distanceSq));
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return List.of();
      }

      candidates.sort(Comparator.comparingDouble(BufferHelper.Inner_StreamUtil::init_D).reversed());
      List<BufferHelper.Inner_BufferHelper> accepted = new ArrayList<>();
      double bestValue = -1.0;
      int evaluated = 0;

      for (BufferHelper.Inner_StreamUtil candidate : candidates) {
         if (accepted.size() >= 4 && candidate.init_D() <= bestValue || evaluated >= 32) {
            break;
         }

         evaluated++;
         autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(
            enemy, candidate.init_Vec3(), this.init_S_CachePool_BlockPos(candidate.init_BlockPos()), lead
         );
         if (this.init_Z_S_BufferHelper(rank)) {
            accepted.add(new BufferHelper.Inner_BufferHelper(candidate.init_BlockPos(), rank, candidate.load()));
            bestValue = Math.max(bestValue, init_D_S_BufferHelper_Z(rank, efficient));
         }
      }

      if (accepted.isEmpty()) {
         return List.of();
      }

      accepted.sort(
         (first, second) -> {
            int byValue = Double.compare(
               init_D_S_BufferHelper_Z(second.init_S_BufferHelper(), efficient), init_D_S_BufferHelper_Z(first.init_S_BufferHelper(), efficient)
            );
            if (byValue != 0) {
               return byValue;
            }

            int byPreference = init_I_S_BufferHelper_S_BufferHelper(first.init_S_BufferHelper(), second.init_S_BufferHelper());
            return byPreference != 0 ? byPreference : Double.compare(second.init_D(), first.init_D());
         }
      );
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (BufferHelper.Inner_BufferHelper scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.init_BlockPos());
      }

      return result;
   }

   private static double init_D_S_BufferHelper_Z(autismclient.util.S.Inner_BufferHelper rank, boolean efficient) {
      return efficient ? rank.load() - 2.5 * rank.read() : rank.load();
   }

   private void init_V_Str(String reason) {
      this.read_Map.merge(reason, 1, Integer::sum);
   }

   private BufferHelper.Inner_CachePool init_BufferHelper_Cache_LivingEntity(LivingEntity enemy, double range, autismclient.util.U.Inner_StreamUtil budget) {
      this.read_Map.clear();
      if (this.init_I_Predicate(BufferHelper::read_Z_ItemStack) >= 0 && this.init_I_Predicate(BufferHelper::init_Z_ItemStack) >= 0) {
         LocalPlayer player = MC.player;
         Vec3 eye = player.getEyePosition();
         BlockPos origin = player.blockPosition();
         int[] offsets = init_IArr_I(Mth.ceil(range));
         BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
         BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
         double floor = this.decimal("min-target-damage");
         double rangeSq = range * range;
         AABB nextBox = this.init_AABB();
         AABB lead = this.init_AABB_LivingEntity(enemy);
         List<BufferHelper.Inner_TaskQueue> accepted = new ArrayList<>();
         MutableBlockPos cursor = new MutableBlockPos();
         int evaluated = 0;

         for (int i = 0; i + 2 < offsets.length; i += 3) {
            cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
            BlockPos anchorCell = cursor.above();
            if (this.flush(anchorCell)) {
               if (this.init_Map.containsKey(cursor) || this.init_Map.containsKey(anchorCell)) {
                  this.init_V_Str("banned");
               } else if (this.read_Z_BlockPos(anchorCell)
                  && this.init_Z_BlockPos_AABB(anchorCell, nextBox)
                  && this.init_Z_BlockState(obsidian, cursor, nextBox)) {
                  Vec3 centre = init_Vec3_BlockPos(anchorCell);
                  if (this.bool("only-above") && centre.y < enemy.getY()) {
                     this.init_V_Str("above");
                  } else {
                     double distanceSq = eye.distanceToSqr(centre);
                     if (distanceSq > rangeSq) {
                        this.init_V_Str("reach");
                     } else {
                        autismclient.util.S.Inner_CachePool supported = this.init_S_CachePool_BlockPos(anchorCell).init(cursor.immutable());
                        if (autismclient.util.S.load_D(enemy, centre, 5.0F, supported.init(lead)) < floor) {
                           this.init_V_Str("bound");
                        } else {
                           if (++evaluated > 32) {
                              break;
                           }

                           autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(enemy, centre, supported, lead);
                           if (!this.init_Z_S_BufferHelper(rank)) {
                              this.init_V_Str(String.format(Locale.ROOT, "damage:%.1f/%.1f", rank.load(), rank.read()));
                           } else {
                              accepted.add(new BufferHelper.Inner_TaskQueue(anchorCell.immutable(), cursor.immutable(), rank, distanceSq));
                           }
                        }
                     }
                  }
               } else {
                  this.init_V_Str("geometry");
               }
            }
         }

         accepted.sort((first, second) -> {
            int preference = init_I_S_BufferHelper_S_BufferHelper(first.init_S_BufferHelper(), second.init_S_BufferHelper());
            return preference != 0 ? preference : Double.compare(second.init_D(), first.init_D());
         });
         int limit = Math.min(4, accepted.size());

         for (int i = 0; i < limit; i++) {
            BufferHelper.Inner_TaskQueue pair = accepted.get(i);
            BufferHelper.Inner_CachePool plan = this.init_BufferHelper_Cache_BlockPos_2(pair.init_BlockPos(), pair.load(), range, budget);
            if (plan != null) {
               return plan;
            }

            this.init_V_Str("scan");
         }

         return null;
      } else {
         this.init_V_Str("material");
         return null;
      }
   }

   private List<BlockPos> load(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = init_IArr_I(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double rangeSq = range * range;
      AABB nextBox = this.init_AABB();
      AABB enemyRing = enemy.getBoundingBox().inflate(1.0);
      int feetY = Mth.floor(enemy.getY());
      List<BufferHelper.Inner_BufferHelper> accepted = new ArrayList<>();
      int evaluated = 0;
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.init_Map.containsKey(cursor) && this.flush(cursor) && this.read_Z_BlockPos(cursor) && this.init_Z_BlockState(anchor, cursor, nextBox)) {
            Vec3 centre = init_Vec3_BlockPos(cursor);
            double distanceSq = eye.distanceToSqr(centre);
            if (!(distanceSq > rangeSq) && new AABB(cursor).intersects(enemyRing)) {
               if (evaluated >= 32) {
                  break;
               }

               evaluated++;
               autismclient.util.S.Inner_BufferHelper rank = autismclient.util.TableIndex.init(
                  enemy, centre, 5.0F, this.init_S_CachePool_BlockPos(cursor), autismclient.util.TableIndex.init(enemy)
               );
               if (this.init_Z_S_BufferHelper(rank) && this.close(cursor)) {
                  accepted.add(new BufferHelper.Inner_BufferHelper(cursor.immutable(), rank, distanceSq));
               }
            }
         }
      }

      if (accepted.isEmpty()) {
         return List.of();
      }

      accepted.sort((first, second) -> {
         boolean firstLow = first.init_BlockPos().getY() <= feetY;
         boolean secondLow = second.init_BlockPos().getY() <= feetY;
         if (firstLow != secondLow) {
            return firstLow ? -1 : 1;
         }

         int byPreference = init_I_S_BufferHelper_S_BufferHelper(first.init_S_BufferHelper(), second.init_S_BufferHelper());
         return byPreference != 0 ? byPreference : Double.compare(second.init_D(), first.init_D());
      });
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (BufferHelper.Inner_BufferHelper scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.init_BlockPos());
      }

      return result;
   }

   private BufferHelper.Inner_CachePool load(LivingEntity enemy, double range, autismclient.util.U.Inner_StreamUtil budget) {
      if (!this.init_Z_Z(false) && !this.init_Z_Z(true)) {
         return null;
      }

      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = init_IArr_I(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double damageFloor = this.decimal("min-target-damage");
      double selfCeiling = this.decimal("max-self-damage");
      double rangeSq = range * range;
      AABB nextBox = this.init_AABB();
      List<BufferHelper.Inner_BufferHelper> needsShield = new ArrayList<>();
      int evaluated = 0;
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.init_Map.containsKey(cursor) && this.flush(cursor) && this.read_Z_BlockPos(cursor) && this.init_Z_BlockState(anchor, cursor, nextBox)) {
            Vec3 centre = init_Vec3_BlockPos(cursor);
            if (!this.bool("only-above") || !(centre.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq) && !(autismclient.util.S.load_D(enemy, centre, 5.0F, this.init_S_CachePool_BlockPos(cursor)) < damageFloor)) {
                  if (evaluated >= 32) {
                     break;
                  }

                  evaluated++;
                  autismclient.util.S.Inner_BufferHelper rank = autismclient.util.TableIndex.init(
                     enemy, centre, 5.0F, this.init_S_CachePool_BlockPos(cursor), autismclient.util.TableIndex.init(enemy)
                  );
                  if (!(rank.load() < damageFloor) && !(rank.read() <= selfCeiling)) {
                     needsShield.add(new BufferHelper.Inner_BufferHelper(cursor.immutable(), rank, distanceSq));
                  }
               }
            }
         }
      }

      if (needsShield.isEmpty()) {
         return null;
      }

      needsShield.sort((first, second) -> {
         int byPreference = init_I_S_BufferHelper_S_BufferHelper(first.init_S_BufferHelper(), second.init_S_BufferHelper());
         return byPreference != 0 ? byPreference : Double.compare(second.init_D(), first.init_D());
      });
      int tried = 0;

      for (BufferHelper.Inner_BufferHelper scored : needsShield) {
         if (tried >= 4) {
            break;
         }

         tried++;
         BlockPos shield = this.init_BlockPos_LivingEntity(enemy, scored.init_BlockPos(), damageFloor, selfCeiling);
         if (shield != null) {
            int material = this.init_I_BlockPos_BlockPos(shield, scored.init_BlockPos());
            if (material >= 0) {
               boolean obsidian = material == 1;
               return this.init(scored.init_BlockPos(), shield, obsidian, range, budget, null);
            }
         }
      }

      return null;
   }

   private BlockPos init_BlockPos_LivingEntity(LivingEntity enemy, BlockPos anchorCell, double damageFloor, double selfCeiling) {
      Vec3 centre = init_Vec3_BlockPos(anchorCell);
      autismclient.util.S.Inner_BufferHelper before = autismclient.util.TableIndex.init(
         enemy, centre, 5.0F, this.init_S_CachePool_BlockPos(anchorCell), autismclient.util.TableIndex.init(enemy)
      );
      if (before.read() < 2.0) {
         return null;
      }

      boolean obsidianAvailable = this.init_Z_Z(true);
      boolean glowstoneAvailable = this.init_Z_Z(false);
      if (!obsidianAvailable && !glowstoneAvailable) {
         return null;
      }

      BlockPos best = null;
      double bestValue = -1.0 / 0.0;

      for (BlockPos shieldCell : this.init_List_BlockPos(anchorCell)) {
         if (obsidianAvailable && this.init_Z_BlockPos_BlockPos(shieldCell, anchorCell) || glowstoneAvailable && this.update(shieldCell)) {
            autismclient.util.S.Inner_CachePool verify = this.init_S_CachePool_BlockPos(anchorCell).init(shieldCell);
            autismclient.util.S.Inner_BufferHelper rank = autismclient.util.TableIndex.init(
               enemy, centre, 5.0F, verify, autismclient.util.TableIndex.init(enemy)
            );
            if (!autismclient.util.S.init_Z_D(rank.read())
               && autismclient.util.SyncManager.init(before.load(), before.read(), rank.load(), rank.read(), damageFloor, selfCeiling, this.bool("efficient"))
               && (!this.bool("protect-friends") || autismclient.util.UiHelper.init(centre, 5.0F, verify, this.decimal("max-friend-damage")))) {
               double value = rank.load() - rank.read();
               if (value > bestValue) {
                  bestValue = value;
                  best = shieldCell;
               }
            }
         }
      }

      return best;
   }

   private List<BlockPos> init_List_BlockPos(BlockPos anchorCell) {
      LocalPlayer player = MC.player;
      AABB box = player.getBoundingBox();
      AABB nextBox = this.init_AABB();
      Vec3 playerCentre = box.getCenter();
      Vec3 toAnchor = init_Vec3_BlockPos(anchorCell).subtract(playerCentre).normalize();
      List<BlockPos> cells = new ArrayList<>();

      for (BlockPos cell : autismclient.util.SyncManager.init(box, nextBox, init_Vec3_BlockPos(anchorCell))) {
         if (!cell.equals(anchorCell) && this.init_Z_BlockState(Blocks.GLOWSTONE.defaultBlockState(), cell, nextBox)) {
            cells.add(cell);
         }
      }

      cells.sort(
         (first, second) -> {
            boolean firstGround = this.write(first);
            boolean secondGround = this.write(second);
            if (firstGround != secondGround) {
               return firstGround ? -1 : 1;
            } else {
               return Double.compare(
                  init_Vec3_BlockPos(second).subtract(playerCentre).normalize().dot(toAnchor),
                  init_Vec3_BlockPos(first).subtract(playerCentre).normalize().dot(toAnchor)
               );
            }
         }
      );
      return cells.size() <= 12 ? cells : new ArrayList<>(cells.subList(0, 12));
   }

   private boolean write(BlockPos cell) {
      BlockPos below = cell.below();
      return autismclient.util.U.load(MC.level.getBlockState(below), below, false);
   }

   private boolean update(BlockPos cell) {
      for (Direction face : Direction.values()) {
         BlockPos support = cell.relative(face);
         if (autismclient.util.U.load(MC.level.getBlockState(support), support, false)) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_BlockPos_BlockPos(BlockPos cell, BlockPos anchorCell) {
      return this.update(cell) || cell.distManhattan(anchorCell) == 1;
   }

   private int init_I_BlockPos_BlockPos(BlockPos cell, BlockPos anchor) {
      boolean durable = this.init_Z_Z(true) && this.init_Z_BlockPos_BlockPos(cell, anchor);
      boolean fuel = this.init_Z_Z(false) && this.update(cell);
      if (!durable
         || fuel && !"Durable".equals(this.choice("cover-material")) && (!"Adaptive".equals(this.choice("cover-material")) || !this.load(cell, anchor))) {
         return fuel ? 0 : (durable ? 1 : -1);
      } else {
         return 1;
      }
   }

   private boolean load(BlockPos cover, BlockPos anchor) {
      Vec3 eye = MC.player.getEyePosition();
      double range = MC.player.blockInteractionRange();
      AABB box = new AABB(cover);

      for (Direction side : Direction.values()) {
         BlockPos support = anchor.relative(side);
         if (!support.equals(cover) && autismclient.util.U.load(MC.level.getBlockState(support), support, false)) {
            Direction face = side.getOpposite();
            Vec3 point = Vec3.atCenterOf(support).add(face.getStepX() * 0.499, face.getStepY() * 0.499, face.getStepZ() * 0.499);
            if (!(eye.distanceToSqr(point) > range * range) && !box.clip(eye, point).isPresent()) {
               BlockHitResult hit = MC.level.clip(new ClipContext(eye, point, Block.OUTLINE, Fluid.NONE, MC.player));
               if (hit.getBlockPos().equals(support) && hit.getDirection() == face && hit.getType() == Type.BLOCK) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private AABB init_AABB() {
      return autismclient.util.ByteReader.init(MC.player);
   }

   private boolean init_Z_BlockPos_AABB(BlockPos cell, AABB nextBox) {
      if (t.init_Z_BlockPos_6(cell) || t.load_Z_BlockPos_3(cell)) {
         return false;
      } else if (!MC.level.getBlockState(cell).canBeReplaced()) {
         return false;
      } else if (!MC.level.isUnobstructed(Blocks.RESPAWN_ANCHOR.defaultBlockState(), cell, CollisionContext.placementContext(MC.player))) {
         return false;
      } else {
         return !AutismLiteVariant.enabled() && TaskTimer.init_Z_AABB_2(new AABB(cell)) ? false : !nextBox.intersects(new AABB(cell));
      }
   }

   private boolean init_Z_BlockState(BlockState anchor, BlockPos cell, AABB nextBox) {
      if (t.init_Z_BlockPos_6(cell)) {
         return false;
      } else if (t.load_Z_BlockPos_3(cell)) {
         return false;
      } else if (!MC.level.getBlockState(cell).canBeReplaced()) {
         return false;
      } else if (!anchor.canSurvive(MC.level, cell)) {
         return false;
      } else {
         return !MC.level.isUnobstructed(anchor, cell, CollisionContext.placementContext(MC.player)) ? false : !nextBox.intersects(new AABB(cell));
      }
   }

   private BlockPos init_BlockPos_LivingEntity_D(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = init_IArr_I(Mth.ceil(range));
      double rangeSq = range * range;
      BlockPos best = null;
      autismclient.util.S.Inner_BufferHelper bestRank = null;
      double bestDistanceSq = 0.0;
      AABB lead = this.init_AABB_LivingEntity(enemy);
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.init_Map.containsKey(cursor) && this.flush(cursor) && (!this.bool("own-only") || this.init_Set.contains(cursor))) {
            BlockState state = MC.level.getBlockState(cursor);
            if (state.getBlock() instanceof RespawnAnchorBlock && this.read_Z_BlockPos(cursor)) {
               Vec3 centre = init_Vec3_BlockPos(cursor);
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq)) {
                  autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(
                     enemy, centre, this.init_S_CachePool_BlockPos(cursor), lead
                  );
                  if (this.init_Z_S_BufferHelper(rank)) {
                     int preference = bestRank == null ? -1 : init_I_S_BufferHelper_S_BufferHelper(rank, bestRank);
                     if (preference < 0 || preference == 0 && distanceSq > bestDistanceSq) {
                        best = cursor.immutable();
                        bestRank = rank;
                        bestDistanceSq = distanceSq;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private static boolean init_Z_ItemStack(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.RESPAWN_ANCHOR;
   }

   private static boolean load_Z_ItemStack(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.GLOWSTONE;
   }

   private static boolean read_Z_ItemStack(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.OBSIDIAN;
   }

   private static BlockState init_BlockState_Z(boolean obsidian) {
      return (obsidian ? Blocks.OBSIDIAN : Blocks.GLOWSTONE).defaultBlockState();
   }

   private boolean init_Z_Z(boolean obsidian) {
      return !obsidian
         ? this.read_I() >= 2
         : this.init_I_Predicate(BufferHelper::load_Z_ItemStack) >= 0 && this.init_I_Predicate(BufferHelper::read_Z_ItemStack) >= 0;
   }

   private int read_I() {
      LocalPlayer player = MC.player;
      int count = 0;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = player.getInventory().getItem(slot);
         if (load_Z_ItemStack(stack)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   private int write_I() {
      LocalPlayer player = MC.player;
      String configured = this.choice("detonate-slot");
      if ("Auto".equals(configured)) {
         int selected = player.getInventory().getSelectedSlot();
         if (!load_Z_ItemStack(player.getMainHandItem()) && !autismclient.util.ah.init(selected, "anchor-aura")) {
            return selected;
         }

         int anchor = this.init_I_Predicate(BufferHelper::init_Z_ItemStack);
         return anchor >= 0 ? anchor : this.init_I_Predicate(stack -> !load_Z_ItemStack(stack));
      } else {
         int slot = Integer.parseInt(configured) - 1;
         return !load_Z_ItemStack(player.getInventory().getItem(slot)) && !autismclient.util.ah.init(slot, "anchor-aura") ? slot : -1;
      }
   }

   private boolean init_Z_I_Z(int slot, boolean finishingWork) {
      if (slot < 0) {
         return true;
      }

      LocalPlayer player = MC.player;
      if (!autismclient.util.ah.init_Z("anchor-aura", slot)) {
         return false;
      }

      int held = player.getInventory().getSelectedSlot();
      if (held == slot) {
         if (System.nanoTime() < this.read_J) {
            return false;
         }

         this.start = this.integer("switch-back-delay");
         return true;
      } else {
         if (this.load(slot, finishingWork)) {
            if (this.clear < 0) {
               this.clear = held;
            }

            this.reset = slot;
         }

         this.start = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && System.nanoTime() >= this.read_J;
      }
   }

   private boolean load(int slot, boolean finishingWork) {
      if (!ListPool.init_Z_10() && !aK.init_Z_27()) {
         if (finishingWork || !t.flush() || !t.init_Z_51() && !t.sync_Z_11()) {
            int tick = ck.init_ck().state_I();
            if (tick == this.stop) {
               return false;
            }

            if (!autismclient.util.ah.write_Z("anchor-aura")) {
               return false;
            }

            try {
               autismclient.util.aw.init_V(MC, slot);
               this.read_J = System.nanoTime() + this.integer("switch-delay") * 1000000L;
            } finally {
               autismclient.util.ah.read_V("anchor-aura");
            }

            this.stop = tick;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int init_I_Predicate(Predicate<ItemStack> match) {
      LocalPlayer player = MC.player;
      int selected = player.getInventory().getSelectedSlot();
      int best = -1;
      int bestSteps = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (match.test(player.getInventory().getItem(slot)) && !autismclient.util.ah.init(slot, "anchor-aura")) {
            int steps = Math.abs(slot - selected);
            steps = Math.min(steps, 9 - steps);
            if (steps < bestSteps) {
               bestSteps = steps;
               best = slot;
            }
         }
      }

      return best;
   }

   private void write_V_2() {
      if (this.clear >= 0 && MC.gui.screen() == null && MC.gui.overlay() == null) {
         if (this.reset >= 0 && MC.player.getInventory().getSelectedSlot() != this.reset) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!t.init_Z_I_10(selected) && !aa.load_Z_I_3(selected)) {
               this.clear = -1;
               this.reset = -1;
               this.start = 0;
            }
         } else if (this.start > 0) {
            this.start--;
         } else if (!autismclient.util.ah.init(this.clear, "anchor-aura") && this.load(this.clear, false)) {
            this.clear = -1;
            this.reset = -1;
         }
      }
   }

   private void update_V_2() {
      this.init_S_CachePool = autismclient.util.S.Inner_CachePool.init_S_CachePool.init(this.bool("terrain")).load(true);
      this.load_Map.clear();
   }

   private autismclient.util.S.Inner_CachePool init_S_CachePool_BlockPos(BlockPos cell) {
      BlockPos key = cell.immutable();
      autismclient.util.S.Inner_CachePool cached = this.load_Map.get(key);
      if (cached != null) {
         return cached;
      }

      autismclient.util.S.Inner_CachePool options = this.init_S_CachePool.init(List.of(key)).init(autismclient.util.S.load(MC.level, init_Vec3_BlockPos(key)));
      this.load_Map.put(key, options);
      return options;
   }

   private AABB init_AABB_LivingEntity(LivingEntity enemy) {
      int horizon = Math.min(12, 6 + (int)Math.ceil(autismclient.util.macro.ab.init_I() / 50.0));
      return this.init_TableIndex.init_AABB_LivingEntity(enemy, ck.init_ck().state_I(), horizon, 2.0);
   }

   private autismclient.util.S.Inner_BufferHelper init_S_BufferHelper_LivingEntity(
      LivingEntity enemy, Vec3 centre, autismclient.util.S.Inner_CachePool options, AABB lead
   ) {
      if (this.bool("protect-friends") && !autismclient.util.UiHelper.init(centre, 5.0F, options, this.decimal("max-friend-damage"))) {
         return new autismclient.util.S.Inner_BufferHelper(0.0, 1.0 / 0.0);
      }

      double target = autismclient.util.S.read(enemy, centre, 5.0F, options.init(lead));
      LocalPlayer player = MC.player;
      double self = enemy == player ? target : autismclient.util.S.read(centre, 5.0F, options);
      return new autismclient.util.S.Inner_BufferHelper(target, self);
   }

   private boolean init_Z_S_BufferHelper(autismclient.util.S.Inner_BufferHelper rank) {
      return autismclient.util.S.init_Z_D(rank.read())
         ? false
         : autismclient.util.UiHelper.init(rank.load(), rank.read(), this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"));
   }

   private boolean sync(BlockPos cell) {
      return !autismclient.util.S.init_Z_D(this.init_D_BlockPos(cell));
   }

   private boolean flush(BlockPos cell) {
      return !"Aimed".equals(this.choice("aim-mode")) || cell.equals(this.init_BlockPos);
   }

   private boolean close(BlockPos cell) {
      return !this.bool("protect-friends")
         || autismclient.util.UiHelper.init(init_Vec3_BlockPos(cell), 5.0F, this.load_S_CachePool_BlockPos(cell), this.decimal("max-friend-damage"));
   }

   private double init_D_BlockPos(BlockPos cell) {
      return autismclient.util.S.read(init_Vec3_BlockPos(cell), 5.0F, this.load_S_CachePool_BlockPos(cell));
   }

   private autismclient.util.S.Inner_CachePool load_S_CachePool_BlockPos(BlockPos cell) {
      autismclient.util.S.Inner_CachePool options = this.init_S_CachePool_BlockPos(cell);
      if (this.poll_BlockPos == null || !cell.equals(this.load_BlockPos)) {
         return options;
      } else if (this.open(cell)) {
         return options.init(this.poll_BlockPos);
      } else {
         return MC.level.getBlockState(this.poll_BlockPos) != autismclient.util.IoWorker.init(MC.level, this.poll_BlockPos)
            ? options.init(List.of(cell, this.poll_BlockPos))
            : options;
      }
   }

   private boolean open(BlockPos cell) {
      if (this.poll_BlockPos != null && cell.equals(this.load_BlockPos)) {
         net.minecraft.world.level.block.Block material = init_BlockState_Z(this.update_Z).getBlock();
         return MC.level.getBlockState(this.poll_BlockPos).is(material) && autismclient.util.IoWorker.init(MC.level, this.poll_BlockPos).is(material);
      } else {
         return false;
      }
   }

   private boolean poll(BlockPos cell) {
      if (this.poll_BlockPos == null || !cell.equals(this.load_BlockPos)) {
         return true;
      }

      if (!this.open(cell)) {
         return false;
      }

      double self = this.init_D_BlockPos(cell);
      return self <= this.decimal("max-self-damage") && !autismclient.util.S.init_Z_D(self) && this.close(cell);
   }

   private static int init_I_S_BufferHelper_S_BufferHelper(autismclient.util.S.Inner_BufferHelper first, autismclient.util.S.Inner_BufferHelper second) {
      int byBucket = Double.compare(init_D_D(second.load()), init_D_D(first.load()));
      return byBucket != 0 ? byBucket : Double.compare(first.read(), second.read());
   }

   private static double init_D_D(double targetDamage) {
      return Math.floor(targetDamage * 2.0);
   }

   private static Vec3 init_Vec3_BlockPos(BlockPos pos) {
      return Vec3.atCenterOf(pos);
   }

   private static double init_D_AABB_Vec3(AABB box, Vec3 point) {
      double dx = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
      double dy = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
      double dz = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
      return dx * dx + dy * dy + dz * dz;
   }

   private static int[] init_IArr_I(int radius) {
      int clamped = Mth.clamp(radius, 0, 8);
      if (clamped == get) {
         return init_IArr;
      }

      int limit = clamped * clamped;
      List<int[]> cells = new ArrayList<>();

      for (int x = -clamped; x <= clamped; x++) {
         for (int y = -clamped; y <= clamped; y++) {
            for (int z = -clamped; z <= clamped; z++) {
               int distance = x * x + y * y + z * z;
               if (distance <= limit) {
                  cells.add(new int[]{x, y, z, distance});
               }
            }
         }
      }

      cells.sort(Comparator.comparingInt(cellx -> cellx[3]));
      int[] flat = new int[cells.size() * 3];

      for (int i = 0; i < cells.size(); i++) {
         int[] cell = cells.get(i);
         flat[i * 3] = cell[0];
         flat[i * 3 + 1] = cell[1];
         flat[i * 3 + 2] = cell[2];
      }

      init_IArr = flat;
      get = clamped;
      return flat;
   }

   private record Inner_BufferHelper(BlockPos init_BlockPos, autismclient.util.S.Inner_BufferHelper init_S_BufferHelper, double init_D) {
   }

   private record Inner_CachePool(
      BlockPos init_BlockPos,
      BufferHelper.Inner_ConfigStore init_BufferHelper_Confi,
      int init_I,
      autismclient.util.U.Inner_CachePool init_U_CachePool,
      double init_D,
      BlockPos load,
      boolean init_Z,
      BlockPos read
   ) {
   }

   private enum Inner_ConfigStore {
      init,
      load,
      read,
      write,
      update;
   }

   private record Inner_DataUtil(BlockPos init) implements autismclient.util.U.Inner_NetHelper {
      @Override
      public boolean lands(BlockHitResult hit, BlockPos cell) {
         return hit != null && this.init.equals(hit.getBlockPos());
      }

      @Override
      public boolean init(BlockState state, BlockPos pos, boolean sneaking) {
         return this.init.equals(pos) && state.getBlock() instanceof RespawnAnchorBlock;
      }
   }

   private record Inner_NetHelper(autismclient.util.U.Inner_TaskQueue init_U_TaskQueue, autismclient.util.U.Inner_DataUtil init_U_DataUtil) {
   }

   private record Inner_StreamUtil(BlockPos init_BlockPos, Vec3 init_Vec3, double init_D, double load) {
   }

   private record Inner_TaskQueue(BlockPos init_BlockPos, BlockPos load, autismclient.util.S.Inner_BufferHelper init_S_BufferHelper, double init_D) {
   }
}
