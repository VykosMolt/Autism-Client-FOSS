package autismclient.modules;

import autismclient.ai.ca;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
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

public final class t extends Module implements cn.Inner_DataUtil {
   public static final String init_Str = "crystal-aura";
   private static final float init_F = 6.0F;
   private static final double init_D = 2.0;
   private static final int init_I = 48;
   private static final double load_D = 8.0;
   private static final double read_D = 5.0;
   private static final double write_D = 2.0;
   private static final int load_I = 12;
   private static final int read_I = 6;
   private static final int write_I = 4;
   private static final int update_I = 2;
   private static final int sync_I = 1;
   private static final double update_D = 0.0625;
   private static final double sync_D = 2.0;
   private static final int flush_I = 1;
   private static final double flush_D = 1.0;
   private static final double close_D = 5.0;
   private static final int close_I = 2;
   private static final double open_D = 5.0;
   private static final double poll_D = 2.0;
   private static final int open_I = 3;
   private static final int poll_I = 20;
   private static final int push_I = 2;
   private static final int pop_I = 4;
   private static final int tick = 2;
   private static final int run = 40;
   private static final int get = 16;
   private autismclient.util.S.Inner_CachePool init_S_CachePool;
   private static final EnumSet<Direction> init_EnumSet = EnumSet.allOf(Direction.class);
   private static final double[] init_DArr = new double[]{0.3, 0.5, 0.7};
   private static final int set = 4;
   private static final int put = 32;
   private static int[] init_IArr = new int[0];
   private static int add = -1;
   private static volatile boolean init_Z;
   private static volatile float load_F = 0.3F;
   private static volatile float read_F = -0.5F;
   private static volatile float write_F;
   private static volatile float update_F = 0.25F;
   private final Random init_Random;
   private long init_J;
   private long load_J;
   private int clear;
   private LivingEntity init_LivingEntity;
   private String load_Str;
   private int reset;
   private BlockPos init_BlockPos;
   private final autismclient.util.NetHelper init_NetHelper;
   private final autismclient.util.StateCache init_StateCache;
   private final autismclient.util.PoolEntry init_PoolEntry;
   private final autismclient.util.TableIndex init_TableIndex;
   private final autismclient.util.JsonUtil init_JsonUtil;
   private int start;
   private long read_J;
   private long write_J;
   private long update_J;
   private boolean auditHasReadDeadline;
   private boolean auditHasWriteDeadline;
   private boolean auditHasUpdateDeadline;
   private HitResult init_HitResult;
   private String read_Str;
   private Set<String> init_Set;
   private int stop;
   private int next;
   private int prev;
   private int valid;
   private int size;
   private BlockPos load_BlockPos;
   private int data;
   private LivingEntity load_LivingEntity;
   private int value;
   private double push_D;
   private BlockPos read_BlockPos;
   private LivingEntity read_LivingEntity;
   private final autismclient.util.HashUtil init_HashUtil;
   private int cache;
   private BlockPos write_BlockPos;
   private BlockPos update_BlockPos;
   private int state;
   private BlockPos sync_BlockPos;
   private int queue;
   private int buffer;
   private int entry;
   private int node;
   private int task;
   private int pool;
   private BlockPos flush_BlockPos;
   private int index;
   private int count;
   private final autismclient.util.FileCache<BlockPos> init_FileCache;
   private final autismclient.util.FileCache<EndCrystal> load_FileCache;
   private final autismclient.util.RenderState init_RenderState;
   private int flags;
   private BlockPos close_BlockPos;
   private int level;
   private int frame;
   private BlockPos open_BlockPos;
   private BlockPos poll_BlockPos;
   private BlockPos push_BlockPos;
   private BlockPos pop_BlockPos;
   private long sync_J;
   private long flush_J;
   private int block;
   private List<BlockPos> init_List;
   private final List<t.Inner_StateCache> load_List;
   private int chunk;
   private final Map<BlockPos, Integer> init_Map;
   private static volatile int table = -2147483648;

   public t() {
      super("crystal-aura", "CrystalAura", ae.init_ae, "Places and detonates end crystals.");
      this.init_S_CachePool = autismclient.util.S.Inner_CachePool.init_S_CachePool.init(true);
      this.init_Random = new Random();
      this.load_Str = "";
      this.reset = -2147483648;
      this.init_NetHelper = new autismclient.util.NetHelper();
      this.init_StateCache = new autismclient.util.StateCache();
      this.init_PoolEntry = new autismclient.util.PoolEntry();
      this.init_TableIndex = new autismclient.util.TableIndex();
      this.init_JsonUtil = new autismclient.util.JsonUtil();
      this.init_Set = Set.of();
      this.stop = -1;
      this.next = -1;
      this.valid = -2147483648;
      this.data = -1;
      this.value = -2147483648;
      this.init_HashUtil = new autismclient.util.HashUtil();
      this.cache = -2147483648;
      this.state = -2147483648;
      this.node = -1;
      this.pool = -2147483648;
      this.index = -2147483648;
      this.init_FileCache = new autismclient.util.FileCache<>(16);
      this.load_FileCache = new autismclient.util.FileCache<>(16);
      this.init_RenderState = new autismclient.util.RenderState();
      this.flags = -2147483648;
      this.level = -1;
      this.block = -2147483648;
      this.init_List = List.of();
      this.load_List = new ArrayList<>(4);
      this.chunk = -2147483648;
      this.init_Map = new LinkedHashMap<>();
      this.add(new BoolSetting("place", "Place", true).description("Place on obsidian and bedrock.").build());
      this.add(new BoolSetting("destroy", "Destroy", true).description("Attack crystals in range.").build());
      this.add(
         new BoolSetting("fast-replace", "Fast Replace", true)
            .description("Replace a crystal after our dispatched break without waiting for its removal update.")
            .group("Timing")
            .build()
      );
      this.add(
         new ChoiceSetting("aim-mode", "Aim Mode", "Auto", "Auto", "Aimed")
            .description("Auto chooses blast positions; Aimed starts combos at your crosshair.")
            .build()
      );
      this.add(autismclient.util.StateCache.init_DoubleSetting().build());
      this.add(new BoolSetting("silent", "Silent", true).group("Aim").build());
      this.add(new BoolSetting("move-fix", "Move Fix", true).group("Aim").description("Walk where you look").visibleWhen(() -> this.bool("silent")).build());
      this.add(
         new BoolSetting("anti-weakness", "Anti Weakness", true)
            .description("Borrow a damaging hotbar weapon when weakness prevents crystal breaks.")
            .group("Damage")
            .build()
      );
      this.add(
         new BoolSetting("protect-friends", "Protect Friends", true)
            .description("Check collateral blast damage to friends and teammates.")
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
      this.add(new BoolSetting("obsidian", "Place Obsidian", true).description("Build a support when none exists.").build());
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
         new ChoiceSetting("burst-limit", "Burst Limit", "Unlimited", "Unlimited", "1", "2")
            .description("Crystals to break before a short burst reset.")
            .group("Engagement")
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
      this.add(
         new IntSetting("switch-back-delay", "Switch Back Delay", 20, 1, 100, 1)
            .description("Idle ticks before switching back.")
            .group("Place")
            .visibleWhen(() -> this.bool("place"))
            .build()
      );
      this.add(
         new BoolSetting("only-above", "Only Above", false).description("Place above target.").group("Place").visibleWhen(() -> this.bool("place")).build()
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
      this.add(new IntSetting("place-delay", "Place Delay", 0, 0, 1000, 10).description("Floor between places.").unit("ms").group("Timing").build());
      this.add(new IntSetting("destroy-delay", "Destroy Delay", 0, 0, 1000, 10).description("Floor between breaks.").unit("ms").group("Timing").build());
      this.add(new IntSetting("jitter", "Delay Jitter", 0, 0, 200, 5).description("Random extra delay.").unit("ms").group("Timing").build());
      this.add(
         new IntSetting("switch-delay", "Switch Delay", 0, 0, 1000, 10)
            .description("Extra settling time after an automatic hotbar switch.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(
         new IntSetting("burst-reset", "Burst Reset", 500, 0, 2000, 50)
            .description("Pause after the selected crystal burst limit.")
            .unit("ms")
            .group("Timing")
            .visibleWhen(() -> !"Unlimited".equals(this.choice("burst-limit")))
            .build()
      );
      this.add(new BoolSetting("spam", "Spam", true).description("Run at the tick floor in a prime position.").group("Timing").build());
      this.add(
         new IntSetting("spam-delay", "Spam Delay", 0, 0, 200, 10)
            .description("Floor between actions while spamming.")
            .unit("ms")
            .group("Timing")
            .visibleWhen(() -> this.bool("spam"))
            .build()
      );
      this.add(
         new BoolSetting("vulnerable-spam", "Vulnerable Spam", true)
            .description("Prime fast with the enemy trapped above; gates cadence and anchor priority.")
            .group("Timing")
            .build()
      );
      this.add(
         new BoolSetting("defend", "Blast Protection", true)
            .description("Build damage-ranked obsidian cover against crystals and charged anchors, including level fights.")
            .group("Defend")
            .build()
      );
      this.add(new BoolSetting("preview", "Placement Preview", false).description("Show committed crystal and obsidian cells.").group("Preview").build());
      this.add(
         new ColorSetting("preview-color", "Preview Color", 1728002875)
            .description("Fill and outline for placement previews.")
            .group("Preview")
            .visibleWhen(() -> this.bool("preview"))
            .build()
      );
      this.add(new BoolSetting("crystal-view", "CrystalView", false).description("Shrink and slow crystal models.").group("CrystalView").build());
      this.add(
         new DoubleSetting("view-size", "Size", 0.3, 0.1, 1.5, 0.05)
            .description("Crystal model scale.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      this.add(
         new DoubleSetting("view-y", "Y Translate", -0.5, -2.0, 2.0, 0.05)
            .description("Vertical offset in blocks.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      this.add(
         new DoubleSetting("view-spin", "Spin Speed", 0.0, 0.0, 5.0, 0.05)
            .description("Rotation speed. 0 freezes it.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      this.add(
         new DoubleSetting("view-bounce", "Bounce", 0.25, -1.0, 1.0, 0.05)
            .description("Bob height. Negative inverts it.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      autismclient.aggressivepvp.Versions.crystalAuraCreated(this);
   }

   @Override
   public String settingsHint() {
      String hint = autismclient.aggressivepvp.Versions.hint(this);
      return hint != null ? hint : super.settingsHint();
   }

   @Override
   public void onEnable() {
      if (autismclient.aggressivepvp.Versions.onEnable(autismclient.aggressivepvp.Versions.crystal)) {
         this.stop = -1;
         this.next = -1;
         this.prev = 0;
         this.sync_V_12();
         return;
      }

      this.init_V_51();
      this.sync_V_12();
   }

   @Override
   public void onDisable() {
      if (autismclient.aggressivepvp.Versions.onDisable(autismclient.aggressivepvp.Versions.crystal)) {
         this.stop = -1;
         this.next = -1;
         this.prev = 0;
         this.sync_V_12();
         return;
      }

      int back = this.stop;
      int borrowed = this.next;
      int delay = this.prev;
      this.init_V_51();
      this.sync_V_12();
      // Do not preserve Default's deferred switch-back when handing over to Aggressive.
      boolean handingOverToAggressive = autismclient.aggressivepvp.Versions.crystal != null
         && autismclient.aggressivepvp.Versions.crystal.aggressive()
         && !autismclient.aggressivepvp.Versions.crystal.failed;
      if (back >= 0 && !handingOverToAggressive) {
         // The switch-back still runs after its delay, from tick().
         this.stop = back;
         this.next = borrowed;
         this.prev = delay;
      }

      autismclient.util.aD.init("crystal-aura");
   }

   @Override
   public void onGameLeft() {
      if (autismclient.aggressivepvp.Versions.onGameLeft(autismclient.aggressivepvp.Versions.crystal)) {
         this.stop = -1;
         this.next = -1;
         this.prev = 0;
         return;
      }

      this.init_V_51();
      if ("crystal-aura".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.load_V();
      }
   }

   private void init_V_51() {
      this.init_StateCache.init_V();
      autismclient.util.EventBus.init("crystal-aura");
      this.init_PoolEntry.init();
      this.update_J = 0L;
      this.auditHasUpdateDeadline = false;
      this.init_J = 0L;
      this.load_J = 0L;
      this.clear = 0;
      this.init_LivingEntity = null;
      this.reset = -2147483648;
      this.init_BlockPos = null;
      this.init_NetHelper.init();
      this.init_TableIndex.init();
      this.init_JsonUtil.init();
      this.start = 0;
      this.read_J = 0L;
      this.write_J = 0L;
      this.auditHasReadDeadline = false;
      this.auditHasWriteDeadline = false;
      this.stop = -1;
      this.next = -1;
      this.prev = 0;
      this.valid = -2147483648;
      this.size = 0;
      this.sync_BlockPos = null;
      this.queue = 0;
      this.buffer = 0;
      this.entry = 0;
      this.node = -1;
      this.task = 0;
      this.pool = -2147483648;
      this.flush_BlockPos = null;
      this.index = -2147483648;
      this.count = 0;
      this.init_FileCache.init_V();
      this.load_FileCache.init_V();
      this.init_RenderState.init();
      this.flags = -2147483648;
      this.close_BlockPos = null;
      this.level = -1;
      this.frame = 0;
      table = -2147483648;
      this.open_BlockPos = null;
      this.poll_BlockPos = null;
      this.push_BlockPos = null;
      this.pop_BlockPos = null;
      this.block = -2147483648;
      this.init_List = List.of();
      this.load_List.clear();
      this.chunk = -2147483648;
      this.write_BlockPos = null;
      this.update_BlockPos = null;
      this.state = -2147483648;
      this.init_Map.clear();
      this.read_V_22();
      autismclient.util.ah.update_V("crystal-aura");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return init_Z || this.bool("crystal-view") || "crystal-aura".equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z() || this.stop >= 0;
   }

   @Override
   public void tick() {
      this.sync_V_12();
      if (!this.isEnabled() && MC != null && MC.player != null) {
         if ("crystal-aura".equals(autismclient.util.aD.init_Str())) {
            autismclient.util.aD.init_V_Str_LocalPlayer("crystal-aura", MC.player);
         }

         if (MC.level != null) {
            this.update_V_17();
         }
      }
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.sync_V_12();
      this.block = -2147483648;
      if ("vulnerable-spam".equals(settingId)) {
         this.sync_BlockPos = null;
         this.queue = 0;
         this.buffer = 0;
         this.entry = 0;
         this.node = -1;
         this.task = 0;
      }
   }

   @Override
   public String info() {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)) {
         return autismclient.aggressivepvp.Versions.info(autismclient.aggressivepvp.Versions.crystal);
      }

      LivingEntity current = this.init_LivingEntity;
      return current == null ? "" : current.getName().getString();
   }

   public LivingEntity init_LivingEntity_5() {
      return this.init_LivingEntity;
   }

   LivingEntity load_LivingEntity_4() {
      int age = ck.init_ck().state_I() - this.reset;
      return this.isEnabled() && age >= 0 && age <= 2 && this.push() ? this.init_LivingEntity : null;
   }

   public static double init_D_15() {
      return al.init_Module_Str("crystal-aura") instanceof t aura && aura.clear > 0 ? aura.init_J / aura.clear / 1000.0 : 0.0;
   }

   public static double load_D_6() {
      return al.init_Module_Str("crystal-aura") instanceof t aura ? aura.load_J / 1000.0 : 0.0;
   }

   public static List<FileCache> init_List_18() {
      if (al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled() && aura.bool("preview")) {
         int color = am.init_I_Module(aura, "preview-color", 1728002875);
         List<FileCache> boxes = new ArrayList<>(2);
         int tick = ck.init_ck().state_I();
         if (aura.read_BlockPos == null && aura.load_BlockPos == null && tick - aura.state < 0) {
            if (aura.write_BlockPos != null) {
               boxes.add(new FileCache(new AABB(aura.write_BlockPos), color));
            }

            if (aura.update_BlockPos != null) {
               boxes.add(new FileCache(new AABB(aura.update_BlockPos), color));
            }
         }

         if (aura.read_BlockPos != null) {
            boxes.add(new FileCache(new AABB(aura.read_BlockPos), color));
         }

         if (aura.load_BlockPos != null) {
            boxes.add(new FileCache(new AABB(aura.load_BlockPos), color));
         }

         return boxes;
      } else {
         return List.of();
      }
   }

   public static boolean init_Z_51() {
      if (al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled()) {
         int age = ck.init_ck().state_I() - aura.reset;
         return age >= 0 && age <= 5;
      } else {
         return false;
      }
   }

   public static boolean read_Z_21() {
      return init_Z_51() && "crystal-aura".equals(autismclient.util.aD.init_Str());
   }

   public static boolean init_Z_BlockPos_6(BlockPos cell) {
      if (cell == null) {
         return false;
      } else if (!(al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled())) {
         return false;
      } else {
         if (aura.reset != ck.init_ck().state_I()) {
            return false;
         }

         BlockPos reserved = aura.init_BlockPos;
         return reserved == null
            ? false
            : cell.getX() == reserved.getX() && cell.getZ() == reserved.getZ() && cell.getY() >= reserved.getY() && cell.getY() < reserved.getY() + 2;
      }
   }

   public static boolean init_Z_I_10(int slot) {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)) {
         return false;
      }
      return al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled() ? aura.stop >= 0 && aura.next == slot : false;
   }

   public static boolean sync_Z_11() {
      return al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled() ? aura.load_BlockPos != null || aura.read_BlockPos != null : false;
   }

   public static boolean flush() {
      return al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled() ? aura.pop() : false;
   }

   public static boolean close() {
      int age = ck.init_ck().state_I() - table;
      return age >= 0 && age <= 1;
   }

   public static boolean load_Z_BlockPos_3(BlockPos cell) {
      if (cell == null) {
         return false;
      }

      if (al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled()) {
         BlockPos obsidian = aura.load_BlockPos;
         if (obsidian != null
            && cell.getX() == obsidian.getX()
            && cell.getZ() == obsidian.getZ()
            && cell.getY() >= obsidian.getY()
            && cell.getY() < obsidian.getY() + 2) {
            return true;
         }

         BlockPos support = aura.read_BlockPos;
         return support != null && cell.getX() == support.getX() && cell.getZ() == support.getZ() && cell.getY() == support.getY() + 1;
      } else {
         return false;
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
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)) {
         return false;
      }

      boolean enabled = this.isEnabled();
      return !cn.init_Z() && (autismclient.util.aD.init_Z() || enabled && this.push());
   }

   private static boolean auditBeforeDeadline(boolean active, long deadline) {
      return active && System.nanoTime() - deadline < 0L;
   }

   private boolean poll() {
      if (auditBeforeDeadline(this.auditHasWriteDeadline, this.write_J)) {
         return false;
      }

      int bind = this.integer("activation-key");
      boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      if (!this.init_JsonUtil.init(this.choice("activation"), down)) {
         this.start = 0;
         return false;
      }

      byte var10000;
      switch (this.choice("burst-limit")) {
         case "1":
            var10000 = 1;
            break;
         case "2":
            var10000 = 2;
            break;
         default:
            var10000 = 0;
      }

      int limit = var10000;
      if (limit != 0 && this.start >= limit) {
         if (auditBeforeDeadline(this.auditHasReadDeadline, this.read_J)) {
            return false;
         }

         this.start = 0;
         return true;
      } else {
         return true;
      }
   }

   private void init_V_LivingEntity_6(LivingEntity previous) {
      if (previous != null && (!previous.isAlive() || previous.isRemoved() || !(autismclient.util.S.init_D_LivingEntity(previous) > 0.0))) {
         this.start = 0;
         if (this.bool("stop-on-kill")) {
            this.write_J = System.nanoTime() + this.integer("stop-delay") * 1000000L;
            this.auditHasWriteDeadline = true;
            this.init_JsonUtil.init("Press");
         }
      }
   }

   private boolean push() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
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
         && !ScaffoldModule.update_Z_5()
         && !AutoTotemModule.init_Z_6()
         && !TableIndex.init_Z_20();
   }

   @Override
   public void preMovementTick() {
      if (autismclient.aggressivepvp.Versions.preMovementTick(autismclient.aggressivepvp.Versions.crystal)) {
         return;
      }

      long profileStarted = System.nanoTime();

      try {
         if (AutismLiteVariant.enabled()) {
            return;
         }

         if (MC == null || MC.player == null || MC.level == null) {
            return;
         }

         this.init_PoolEntry.init(MC.level, MC.player, MC.getConnection(), () -> {
            this.init_V_51();
            if ("crystal-aura".equals(autismclient.util.aD.init_Str())) {
               autismclient.util.aD.load_V();
            }
         });
         if (MC.gui.screen() == null && MC.missTime > 10) {
            MC.missTime = 0;
         }

         this.update_V_17();
         if (!this.push()) {
            this.init_LivingEntity = null;
            this.read_V_22();
            this.sync_BlockPos = null;
            this.queue = 0;
            this.buffer = 0;
            this.entry = 0;
            this.node = -1;
            this.task = 0;
            this.load_V_34();
            return;
         }

         LivingEntity previousTarget = this.init_LivingEntity;
         this.init_V_LivingEntity_6(previousTarget);
         if (!this.poll()) {
            this.init_LivingEntity = null;
            this.read_V_22();
            this.load_V_34();
            return;
         }

         LivingEntity enemy = this.read_LivingEntity_2();
         if (enemy != previousTarget) {
            this.start = 0;
         }

         this.init_LivingEntity = enemy;
         if (enemy != null) {
            this.init_TableIndex.init(enemy, ck.init_ck().state_I());
         }

         if (enemy == null) {
            this.read_V_22();
            this.sync_BlockPos = null;
            this.queue = 0;
            this.buffer = 0;
            this.entry = 0;
            this.node = -1;
            this.task = 0;
            this.load_V_34();
            return;
         }

         this.init_S_CachePool = autismclient.util.S.Inner_CachePool.init_S_CachePool.init(this.bool("terrain"));
         if (this.flush_BlockPos != null) {
            if (MC.level.getBlockState(this.flush_BlockPos) != autismclient.util.IoWorker.init(MC.level, this.flush_BlockPos)) {
               if (!this.init_Z_LivingEntity_4(enemy)) {
                  this.load_V_34();
               }

               return;
            }

            this.flush_BlockPos = null;
         }

         this.init_HitResult = "Aimed".equals(this.choice("aim-mode")) ? autismclient.util.UiHelper.init() : null;
         t.Inner_NetHelper place = null;
         t.Inner_TaskQueue obsidian = null;
         t.Inner_TaskQueue defend = null;

         t.Inner_ConfigStore destroy;
         try (autismclient.util.S.Inner_ConfigStore pass = autismclient.util.S.init_S_ConfigStore()) {
            defend = this.init_t_TaskQueue_U_StreamUtil_Z(new autismclient.util.U.Inner_StreamUtil(48), true);
            if (defend != null) {
               this.read_V_22();
               table = ck.init_ck().state_I();
            }

            destroy = defend == null ? this.init_t_ConfigStore_LivingEntity(enemy) : null;
            t.Inner_ConfigStore waitingDestroy = null;
            if (destroy != null && !this.init_NetHelper.init_Z_Str("destroy", "place", ck.init_ck().state_I(), System.nanoTime())) {
               waitingDestroy = destroy;
               destroy = null;
            }

            if (destroy == null && defend == null) {
               autismclient.util.U.Inner_StreamUtil budget = new autismclient.util.U.Inner_StreamUtil(48);
               if (this.read(enemy)) {
                  obsidian = this.init_t_TaskQueue_U_StreamUtil(budget);
               } else if (this.update(enemy)) {
                  place = this.load(enemy, budget);
               }

               if (obsidian == null && place == null && this.load_BlockPos == null && this.read_BlockPos == null) {
                  place = this.init_t_NetHelper_LivingEntity_U_StreamUtil(enemy, budget);
                  boolean builderSuppressed = false;
                  if (this.bool("place") && this.bool("obsidian") && this.put() && (place == null || place.load() < 8.0)) {
                     if (this.write(enemy)) {
                        builderSuppressed = true;
                     } else if (!this.run()) {
                        obsidian = this.init_t_TaskQueue_LivingEntity(enemy, budget, place == null ? -1.0 : place.load());
                        if (obsidian != null) {
                           place = null;
                           this.init_V_LivingEntity_t_TaskQueue(enemy, obsidian);
                        }
                     }
                  }

                  if (obsidian == null && place != null) {
                     this.init_V_LivingEntity_t_NetHelper(enemy, place);
                  }

                  if (builderSuppressed && place == null) {
                     if (++this.frame >= 4) {
                        this.close_BlockPos = null;
                        this.frame = 0;
                     }
                  } else {
                     this.frame = 0;
                  }
               }

               if (place == null && obsidian == null && this.load_BlockPos == null && this.read_BlockPos == null) {
                  defend = this.init_t_TaskQueue_LivingEntity_U_StreamUtil(enemy, budget);
                  if (defend == null) {
                     defend = this.init_t_TaskQueue_U_StreamUtil_Z(budget, false);
                  }

                  if (defend != null) {
                     table = ck.init_ck().state_I();
                  }
               }

               if (place == null && obsidian == null && defend == null) {
                  destroy = waitingDestroy;
               }
            }
         }

         this.init(enemy, destroy, place, obsidian);
         if (destroy != null || place != null || obsidian != null || defend != null) {
            if (defend != null ? AnchorAuraModule.read_Z() : !this.pop() && AnchorAuraModule.init_Z_2()) {
               this.load_V_34();
               return;
            }

            this.reset = ck.init_ck().state_I();
            this.init_BlockPos = null;
            boolean borrowsHand = place != null && place.init_InteractionHand() != InteractionHand.OFF_HAND
               || obsidian != null && obsidian.init_InteractionHand() != InteractionHand.OFF_HAND
               || defend != null && defend.init_InteractionHand() != InteractionHand.OFF_HAND
               || this.stop >= 0;
            if (borrowsHand) {
               autismclient.util.ah.update_Z("crystal-aura");
            } else {
               autismclient.util.ah.write_V("crystal-aura");
            }

            if (destroy != null) {
               this.write_V_21();
            }

            ce.Inner_DataUtil wire = this.init_ce_DataUtil_7();
            ce.Inner_DataUtil goal = destroy != null
               ? destroy.init_ce_DataUtil()
               : (
                  place != null
                     ? place.init_U_CachePool().init_U_DataUtil().init_ce_DataUtil()
                     : (
                        obsidian != null
                           ? obsidian.init_U_CachePool().init_U_DataUtil().init_ce_DataUtil()
                           : defend.init_U_CachePool().init_U_DataUtil().init_ce_DataUtil()
                     )
               );
            boolean ready = wire != null && this.init_Z_Z_4(destroy != null) && (destroy == null || this.set());
            Vec3 destroyHit = ready && destroy != null ? this.init_Vec3_t_ConfigStore_ce_DataUtil(destroy, wire) : null;
            BlockHitResult placeHit = ready && destroy == null && place != null ? this.init_BlockHitResult_t_NetHelper_ce_DataUtil(place, wire) : null;
            BlockHitResult obsidianHit = ready && destroy == null && place == null && obsidian != null
               ? this.init_BlockHitResult_t_TaskQueue_ce_DataUtil(obsidian, wire)
               : null;
            BlockHitResult defendHit = ready && destroy == null && place == null && obsidian == null && defend != null
               ? this.init_BlockHitResult_t_TaskQueue_ce_DataUtil(defend, wire)
               : null;
            boolean fire = ready
               && (destroy != null ? destroyHit != null : (place != null ? placeHit != null : (obsidian != null ? obsidianHit != null : defendHit != null)));
            fire = fire && !ListPool.init_Z_10() && !SurroundModule.init_Z_27();
            this.init_StateCache.init("crystal-aura", 18, MC.player, enemy, this.decimal("rotation-speed-multiplier"), fire ? wire : goal, fire);
            if (!fire) {
               return;
            }

            ce.Inner_DataUtil outgoing = cn.init_ce_DataUtil(MC.player);
            if (outgoing == null || !init_Z_ce_DataUtil_ce_DataUtil_5(outgoing, wire)) {
               if (destroy != null) {
                  this.load_Str = "outgoing";
               }

               return;
            }

            if (destroy != null) {
               this.init_Z_t_ConfigStore_Vec3(destroy, destroyHit);
               return;
            }

            if (place != null) {
               this.init_Z_t_NetHelper_BlockHitResult(place, placeHit);
               return;
            }

            if (obsidian != null) {
               this.init_Z_t_TaskQueue_BlockHitResult(obsidian, obsidianHit);
            } else {
               this.load(defend, defendHit);
            }

            return;
         }

         if (!this.init_Z_LivingEntity_4(enemy)) {
            this.load_V_34();
         }
      } finally {
         long elapsed = System.nanoTime() - profileStarted;
         this.init_J += elapsed;
         this.load_J = Math.max(this.load_J, elapsed);
         this.clear++;
      }
   }

   private static boolean init_Z_ce_DataUtil_ce_DataUtil_5(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
      return Math.abs(ce.init(first.init(), second.init())) <= 0.05F && Math.abs(first.load() - second.load()) <= 0.05F;
   }

   private boolean init_Z_LivingEntity_4(LivingEntity enemy) {
      return !SurroundModule.init_Z_27()
         && !ListPool.init_Z_10()
         && (this.pop() || !AnchorAuraModule.init_Z_2())
         && this.init_StateCache.init("crystal-aura", 18, MC.player, enemy, this.init_ce_DataUtil_7());
   }

   private void load_V_34() {
      this.init_StateCache.init_V();
      autismclient.util.ah.write_V("crystal-aura");
      if ("crystal-aura".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.init("crystal-aura");
         autismclient.util.aD.init_V_Str_LocalPlayer("crystal-aura", MC.player);
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_7() {
      ch.Inner_CachePool snapshot = ch.init_ch_CachePool();
      return !snapshot.init_Z() ? null : new ce.Inner_DataUtil(snapshot.read(), snapshot.write());
   }

   private ce.Inner_DataUtil load_ce_DataUtil_5() {
      ce.Inner_DataUtil wire = this.init_ce_DataUtil_7();
      return wire != null ? wire : ce.init(MC.player);
   }

   private boolean init_Z_Z_4(boolean destroy) {
      int tick = ck.init_ck().state_I();
      return this.init_NetHelper.init_Z_Str(destroy ? "destroy" : "place", tick, System.nanoTime());
   }

   private void init_V_Z_11(boolean destroy) {
      int tick = ck.init_ck().state_I();
      this.init_StateCache.load(this.init_LivingEntity, tick, autismclient.util.macro.ab.init_I());
      long now = System.nanoTime();
      if (this.tick_Z_7()) {
         this.init_NetHelper.init_V_Str(destroy ? "destroy" : "place", tick, now, this.integer("spam-delay") * 1000000L);
      } else {
         this.init_NetHelper
            .init_V_Str(
               destroy ? "destroy" : "place",
               tick,
               now,
               autismclient.util.NetHelper.init_J_I(this.integer(destroy ? "destroy-delay" : "place-delay"), this.integer("jitter"), this.init_Random)
            );
      }

      this.prev = this.integer("switch-back-delay");
   }

   private void init(LivingEntity enemy, t.Inner_ConfigStore destroy, t.Inner_NetHelper place, t.Inner_TaskQueue obsidian) {
      if (place == null) {
         boolean held = destroy != null || obsidian != null || this.load_BlockPos != null || this.read_BlockPos != null;
         if (held) {
            this.buffer = 0;
            this.task = 0;
         } else {
            if (++this.buffer > 1) {
               this.sync_BlockPos = null;
               this.queue = 0;
            }

            if (++this.task > 1) {
               this.entry = 0;
               this.node = -1;
            }
         }
      } else {
         this.buffer = 0;
         BlockPos support = place.init_BlockPos();
         boolean strong = place.load() >= this.decimal("min-target-damage") + 2.0;
         boolean sameArea = this.sync_BlockPos != null
            && Math.abs(support.getX() - this.sync_BlockPos.getX()) <= 1
            && Math.abs(support.getY() - this.sync_BlockPos.getY()) <= 1
            && Math.abs(support.getZ() - this.sync_BlockPos.getZ()) <= 1;
         this.sync_BlockPos = support;
         if (!strong) {
            this.queue = 0;
         } else {
            this.queue = sameArea ? this.queue + 1 : 1;
         }

         if (this.load_Z_LivingEntity_2(enemy)) {
            this.task = 0;
            this.entry = enemy.getId() == this.node ? this.entry + 1 : 1;
            this.node = enemy.getId();
         } else if (++this.task > 1) {
            this.entry = 0;
            this.node = -1;
         }
      }
   }

   private boolean pop() {
      LivingEntity enemy = this.init_LivingEntity;
      if (enemy != null && MC.player != null) {
         int age = ck.init_ck().state_I() - this.flags;
         boolean blastGrace = age >= 0 && age <= 2;
         if (!this.bool("vulnerable-spam") || this.entry < 2 || !this.load_Z_LivingEntity_2(enemy) && this.task > 1) {
            if (this.queue < 2) {
               return false;
            } else {
               return blastGrace ? true : MC.player.getDeltaMovement().lengthSqr() <= 0.0625 && enemy.getDeltaMovement().lengthSqr() <= 0.0625;
            }
         } else {
            return blastGrace ? true : init_D_Entity_3(MC.player) <= 0.0625 && init_D_Entity_3(enemy) <= 0.0625;
         }
      } else {
         return false;
      }
   }

   private boolean load_Z_LivingEntity_2(LivingEntity enemy) {
      double drop = enemy.getY() - MC.player.getY();
      if (!(drop < 1.0) && !(drop > 5.0)) {
         double dx = enemy.getX() - MC.player.getX();
         double dz = enemy.getZ() - MC.player.getZ();
         double range = MC.player.blockInteractionRange();
         return dx * dx + dz * dz <= range * range;
      } else {
         return false;
      }
   }

   private static double init_D_Entity_3(Entity entity) {
      Vec3 delta = entity.getDeltaMovement();
      return delta.x * delta.x + delta.z * delta.z;
   }

   private boolean tick_Z_7() {
      return this.bool("spam") && this.pop();
   }

   private LivingEntity read_LivingEntity_2() {
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
               && this.init_Z_Entity_8(entity)
               && !ByteReader.load_Z(entity)
               && !aL.init(entity, "killaura")
               && entity.hurtTime <= this.integer("hurt-time")
               && autismclient.util.S.init_D_LivingEntity(entity) > 0.0
         );
      if (found.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      double rangeSq = range * range;
      String mode = this.choice("targeting");
      LivingEntity best = null;
      double bestScore = 1.7976931348623157E308;

      for (LivingEntity entity : found) {
         double distanceSq = init_D_AABB_Vec3_3(entity.getBoundingBox(), eye);
         if (!(distanceSq > rangeSq)) {
            double score = this.init(mode, entity, distanceSq, eye);
            if (score < bestScore) {
               bestScore = score;
               best = entity;
            }
         }
      }

      LivingEntity incumbent = this.init_LivingEntity;
      if (incumbent != null && incumbent != best && found.contains(incumbent)) {
         double grace = range + 0.5;
         double incumbentDistanceSq = init_D_AABB_Vec3_3(incumbent.getBoundingBox(), eye);
         if (incumbentDistanceSq <= grace * grace
            && (best == null || !autismclient.util.RingBuffer.init(mode, bestScore, this.init(mode, incumbent, incumbentDistanceSq, eye)))) {
            return incumbent;
         }
      }

      return best;
   }

   private double init(String mode, LivingEntity entity, double distanceSq, Vec3 eye) {
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

   private boolean init_Z_Entity_8(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.init_Set_13();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> init_Set_13() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.read_Str)) {
         return this.init_Set;
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

      this.read_Str = source;
      this.init_Set = Set.copyOf(normalized);
      return this.init_Set;
   }

   private t.Inner_ConfigStore init_t_ConfigStore_LivingEntity(LivingEntity enemy) {
      if (!this.bool("destroy")) {
         return null;
      }

      LocalPlayer player = MC.player;
      double range = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
      if (range <= 0.0) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      AABB search = player.getBoundingBox().inflate(range + 2.0);
      List<EndCrystal> crystals = MC.level.getEntitiesOfClass(EndCrystal.class, search, Entity::isAlive);
      if (crystals.isEmpty()) {
         return null;
      }

      double rangeSq = range * range;
      int tick = ck.init_ck().state_I();

      for (EndCrystal tracked : new ArrayList<>(this.load_FileCache.init_Set_I(tick))) {
         if (MC.level.getEntity(tracked.getId()) != tracked || !tracked.isAlive()) {
            this.load_FileCache.init_V_Obj(tracked);
         }
      }

      if (this.init_FileCache.init_I() > 0) {
         for (EndCrystal crystal : crystals) {
            if (!this.init_RenderState.init(crystal, tick)) {
               this.init_FileCache.init_V_Obj(BlockPos.containing(crystal.position()));
            }
         }
      }

      List<t.Inner_BufferHelper> choices = new ArrayList<>();
      AABB blastTarget = this.init_AABB_LivingEntity(enemy, 0, 0.5);

      for (EndCrystal crystal : crystals) {
         if (!this.load_FileCache.init(crystal, tick)
            && (!"Aimed".equals(this.choice("aim-mode")) || this.init_HitResult instanceof EntityHitResult aimed && aimed.getEntity() == crystal)
            && !(init_D_AABB_Vec3_3(crystal.getBoundingBox(), eye) > rangeSq)) {
            autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(enemy, crystal.position(), blastTarget);
            if (this.init_Z_LivingEntity_S_BufferHelper(enemy, rank) && this.init_Z_Vec3_4(crystal.position())) {
               choices.add(new t.Inner_BufferHelper(crystal, rank));
            }
         }
      }

      choices.sort(
         (first, second) -> this.init_Z_S_BufferHelper_S_BufferHelper(first.init_S_BufferHelper(), second.init_S_BufferHelper())
            ? -1
            : (this.init_Z_S_BufferHelper_S_BufferHelper(second.init_S_BufferHelper(), first.init_S_BufferHelper()) ? 1 : 0)
      );
      int tried = 0;

      for (t.Inner_BufferHelper choice : choices) {
         if (tried++ >= 4) {
            break;
         }

         ce.Inner_DataUtil goal = this.init_ce_DataUtil_EndCrystal_D(choice.init_EndCrystal(), range);
         if (goal != null) {
            return new t.Inner_ConfigStore(choice.init_EndCrystal(), goal, range);
         }
      }

      return null;
   }

   private ce.Inner_DataUtil init_ce_DataUtil_EndCrystal_D(EndCrystal crystal, double range) {
      Vec3 eye = MC.player.getEyePosition();
      ce.Inner_DataUtil wire = this.load_ce_DataUtil_5();
      AABB box = crystal.getBoundingBox();
      if (autismclient.util.PacketUtil.init(
            box, eye, Vec3.directionFromRotation(wire.load(), wire.init()), range, pointx -> this.init_Z_Vec3_Vec3_3(eye, pointx)
         )
         != null) {
         return wire;
      }

      ce.Inner_DataUtil best = null;
      float bestDelta = 3.4028235E38F;
      Vec3 nearest = autismclient.util.PacketUtil.init(box, eye);
      ce.Inner_DataUtil nearAim = ce.init(nearest, eye);
      if (autismclient.util.PacketUtil.init(
            box, eye, Vec3.directionFromRotation(nearAim.load(), nearAim.init()), range, pointx -> this.init_Z_Vec3_Vec3_3(eye, pointx)
         )
         != null) {
         best = nearAim;
         bestDelta = ce.load(nearAim, wire);
      }

      for (double fx : init_DArr) {
         for (double fy : init_DArr) {
            for (double fz : init_DArr) {
               Vec3 point = new Vec3(Mth.lerp(fx, box.minX, box.maxX), Mth.lerp(fy, box.minY, box.maxY), Mth.lerp(fz, box.minZ, box.maxZ));
               double distance = eye.distanceTo(point);
               if (!(distance > range) && !this.init_Z_Vec3_Vec3_3(eye, point)) {
                  ce.Inner_DataUtil rotation = ce.init(point, eye);
                  float delta = ce.load(rotation, wire);
                  if (delta < bestDelta) {
                     bestDelta = delta;
                     best = rotation;
                  }
               }
            }
         }
      }

      return best;
   }

   private Vec3 init_Vec3_t_ConfigStore_ce_DataUtil(t.Inner_ConfigStore plan, ce.Inner_DataUtil wire) {
      if (!plan.init_EndCrystal().isAlive()) {
         return null;
      }

      if (((AutismMinecraftAccessor)MC).autism$getMissTime() > 0) {
         return null;
      }

      Vec3 eye = MC.player.getEyePosition();
      Vec3 look = Vec3.directionFromRotation(wire.load(), wire.init());
      Vec3 hit = autismclient.util.PacketUtil.init(
         plan.init_EndCrystal().getBoundingBox(), eye, look, plan.init_D(), point -> this.init_Z_Vec3_Vec3_3(eye, point)
      );
      if (hit == null) {
         this.load_Str = "ray";
      }

      return hit;
   }

   private boolean init_Z_t_ConfigStore_Vec3(t.Inner_ConfigStore plan, Vec3 hitPoint) {
      EndCrystal crystal = plan.init_EndCrystal();
      if (!crystal.isAlive()) {
         return false;
      }

      LivingEntity enemy = this.init_LivingEntity;
      if (!autismclient.util.ah.write_Z("crystal-aura")) {
         this.load_Str = "hand-group";
         return false;
      }

      try {
         ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$ensureHasSentCarriedItem();
      } finally {
         autismclient.util.ah.read_V("crystal-aura");
      }

      if (!autismclient.util.EventBus.init(
         "crystal-aura",
         new EntityHitResult(crystal, hitPoint),
         () -> this.push()
            && crystal.isAlive()
            && this.set()
            && this.init_ce_DataUtil_7() != null
            && this.init_Vec3_t_ConfigStore_ce_DataUtil(plan, this.init_ce_DataUtil_7()) != null
            && this.init_Z_LivingEntity_3(enemy, crystal.position(), false),
         () -> this.init_V_EndCrystal(crystal)
      )) {
         this.load_Str = "queue";
         return false;
      } else {
         this.load_Str = "queued";
         return true;
      }
   }

   private void init_V_EndCrystal(EndCrystal crystal) {
      this.load_Str = "dispatched";
      this.read_V_22();
      this.init_V_Z_11(true);
      this.load_FileCache.init(crystal, ck.init_ck().state_I(), autismclient.util.macro.ab.init_I());
      this.init_RenderState.init(crystal, BlockPos.containing(crystal.position()), ck.init_ck().state_I(), autismclient.util.macro.ab.init_I());
      this.flags = ck.init_ck().state_I();
      this.start++;
      if (!"Unlimited".equals(this.choice("burst-limit")) && this.start >= Integer.parseInt(this.choice("burst-limit"))) {
         this.read_J = System.nanoTime() + this.integer("burst-reset") * 1000000L;
         this.auditHasReadDeadline = true;
         this.init_JsonUtil.init("Press");
      }
   }

   private t.Inner_NetHelper init_t_NetHelper_LivingEntity_U_StreamUtil(LivingEntity enemy, autismclient.util.U.Inner_StreamUtil budget) {
      if (!this.bool("place")) {
         return null;
      }

      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      if (!this.put()) {
         return null;
      }

      List<BlockPos> supports = this.init_List_LivingEntity_D_2(enemy, range);
      if (supports.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      ce.Inner_DataUtil wire = this.load_ce_DataUtil_5();
      autismclient.util.U.Inner_JsonUtil[] refusal = new autismclient.util.U.Inner_JsonUtil[1];
      List<t.Inner_JsonUtil> entries = new ArrayList<>();
      List<autismclient.util.U.Inner_TaskQueue> scan = new ArrayList<>();

      for (BlockPos support : supports) {
         BlockPos cell = support.above();
         BlockState state = MC.level.getBlockState(support);
         autismclient.util.U.Inner_StateCache request = new autismclient.util.U.Inner_StateCache(
               cell, eye, range, autismclient.util.U.init(support, init_EnumSet)
            )
            .init_U_StateCache_ce_DataUtil(wire)
            .init_U_StateCache_Vec3(eye.add(player.getDeltaMovement()))
            .init_U_StateCache_U_StreamUtil(budget);
         scan.clear();
         this.init(support, state, cell, eye, wire, scan);

         for (autismclient.util.U.Inner_TaskQueue option : scan) {
            entries.add(new t.Inner_JsonUtil(support, request, option));
         }
      }

      if (entries.isEmpty()) {
         return null;
      }

      int size = entries.size();
      int start = this.size >= 0 && this.size < size ? this.size : 0;

      for (int step = 0; step < size; step++) {
         int index = (start + step) % size;
         t.Inner_JsonUtil entry = entries.get(index);
         autismclient.util.U.Inner_DataUtil aim = autismclient.util.U.init(entry.init_U_TaskQueue(), entry.init_U_StateCache(), refusal);
         if (aim != null) {
            autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool_U_TaskQueue(
               entry.init_U_TaskQueue(), aim, entry.init_U_StateCache(), refusal
            );
            if (candidate != null) {
               InteractionHand hand = this.init_InteractionHand_5();
               this.size = 0;
               BlockPos chosen = entry.init_BlockPos();
               Vec3 chosenSource = new Vec3(chosen.getX() + 0.5, chosen.getY() + 1.0, chosen.getZ() + 0.5);
               double chosenDamage = this.init_S_BufferHelper_LivingEntity(enemy, chosenSource, this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5))
                  .load();
               return new t.Inner_NetHelper(chosen, hand, range, candidate, entry.init_U_StateCache(), chosenDamage);
            }

            if (refusal[0] == autismclient.util.U.Inner_JsonUtil.push) {
               this.size = index;
               return null;
            }
         }
      }

      this.size = 0;
      return null;
   }

   private void init(BlockPos support, BlockState state, BlockPos cell, Vec3 eye, ce.Inner_DataUtil wire, List<autismclient.util.U.Inner_TaskQueue> out) {
      boolean requiresSneak = autismclient.util.U.init_Z(state, support);
      List<t.Inner_SyncManager> keyed = new ArrayList<>();

      for (Direction face : autismclient.util.U.init_DirectionArr) {
         autismclient.util.U.Inner_BufferHelper intent = new autismclient.util.U.Inner_BufferHelper(support, face);

         for (AABB rect : autismclient.util.U.init_AABBArr_BlockState(state, support, face, 3)) {
            autismclient.util.U.Inner_TaskQueue option = new autismclient.util.U.Inner_TaskQueue(
               cell, support, face, 1, state, rect, init_D_AABB_Direction_2(rect, face), autismclient.util.U.init(rect, face, eye), requiresSneak, intent
            );
            double turn = ce.load(ce.init(autismclient.util.U.init_Vec3(rect, face), eye), wire);
            keyed.add(new t.Inner_SyncManager(option, turn));
         }
      }

      keyed.sort(Comparator.comparingDouble(t.Inner_SyncManager::init_D));

      for (t.Inner_SyncManager entry : keyed) {
         out.add(entry.init_U_TaskQueue());
      }
   }

   private static double init_D_AABB_Direction_2(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = autismclient.util.U.init(normal, true);
      Axis second = autismclient.util.U.init(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private List<BlockPos> init_List_LivingEntity_D_2(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int tick = ck.init_ck().state_I();
      long worldKey = ca.init_J_BlockPos(origin) ^ ca.init_J_BlockPos(enemy.blockPosition());
      long optionsKey = Objects.hash(
         this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"), this.bool("only-above"), this.bool("terrain")
      );
      if (tick == this.block
         && origin.equals(this.push_BlockPos)
         && enemy.blockPosition().equals(this.pop_BlockPos)
         && worldKey == this.sync_J
         && optionsKey == this.flush_J) {
         return this.init_List;
      }

      this.load_List.clear();
      this.chunk = tick;
      int[] offsets = init_IArr_I_3(Mth.ceil(range));
      double floor = this.decimal("min-target-damage");
      double rangeSq = range * range;
      AABB nextBox = player.getBoundingBox().move(player.getDeltaMovement());
      AABB blastTarget = this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5);
      autismclient.util.S.Inner_CachePool targetOptions = this.init_S_CachePool.init(blastTarget);
      List<t.Inner_DataUtil> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (this.read(cursor)
            && (!"Aimed".equals(this.choice("aim-mode")) || this.init_HitResult instanceof BlockHitResult aimed && aimed.getBlockPos().equals(cursor))
            && MC.level.isEmptyBlock(cursor.above())) {
            Vec3 source = new Vec3(cursor.getX() + 0.5, cursor.getY() + 1.0, cursor.getZ() + 0.5);
            if (!this.bool("only-above") || !(source.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(source);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.S.load_D(enemy, source, 6.0F, targetOptions);
                  if (!(bound < floor) && this.init_Z_BlockPos_AABB_3(cursor, nextBox) && !this.write(cursor)) {
                     candidates.add(new t.Inner_DataUtil(cursor.immutable(), source, bound, distanceSq));
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return List.of();
      }

      candidates.sort(Comparator.comparingDouble(t.Inner_DataUtil::init_D).reversed());
      List<t.Inner_StateCache> accepted = new ArrayList<>();
      double bestDamage = -1.0;
      int evaluated = 0;

      for (t.Inner_DataUtil candidate : candidates) {
         if (accepted.size() >= 4 && candidate.init_D() <= bestDamage || evaluated >= 32) {
            break;
         }

         if (this.init_Z_Vec3_9(eye, candidate.init_BlockPos(), candidate.init_Vec3())) {
            evaluated++;
            autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(enemy, candidate.init_Vec3(), blastTarget);
            if (!this.init_Z_LivingEntity_S_BufferHelper(enemy, rank)) {
               if (this.bool("defend") && this.load_List.size() < 4 && rank.load() >= floor && rank.read() > this.decimal("max-self-damage")) {
                  this.load_List.add(new t.Inner_StateCache(candidate.init_BlockPos(), rank, candidate.load()));
               }
            } else if (this.init_Z_Vec3_4(candidate.init_Vec3())) {
               accepted.add(new t.Inner_StateCache(candidate.init_BlockPos(), rank, candidate.load()));
               bestDamage = Math.max(bestDamage, rank.load());
            }
         }
      }

      if (accepted.isEmpty()) {
         this.init_List = List.of();
         return List.of();
      }

      accepted.sort((first, second) -> {
         int byBucket = Double.compare(init_D_D_4(second.init_S_BufferHelper().load()), init_D_D_4(first.init_S_BufferHelper().load()));
         if (byBucket != 0) {
            return byBucket;
         }

         int bySelf = Double.compare(first.init_S_BufferHelper().read(), second.init_S_BufferHelper().read());
         return bySelf != 0 ? bySelf : Double.compare(second.init_D(), first.init_D());
      });
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (t.Inner_StateCache scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.init_BlockPos());
      }

      this.init_List = result;
      this.push_BlockPos = origin.immutable();
      this.pop_BlockPos = enemy.blockPosition().immutable();
      this.sync_J = worldKey;
      this.flush_J = optionsKey;
      this.block = tick;
      return result;
   }

   private static double init_D_D_4(double targetDamage) {
      return Math.floor(targetDamage * 2.0);
   }

   private boolean read(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK;
   }

   private boolean init_Z_BlockPos_AABB_3(BlockPos support, AABB nextBox) {
      BlockPos cell = support.above();
      if (!MC.level.isEmptyBlock(cell)) {
         return false;
      }

      AABB box = new AABB(cell.getX(), cell.getY(), cell.getZ(), cell.getX() + 1.0, cell.getY() + 2.0, cell.getZ() + 1.0);
      if (nextBox.intersects(box)) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && TaskTimer.init_Z_AABB_2(box)) {
         return false;
      }

      List<Entity> occupants = MC.level.getEntities((Entity)null, box);
      if (!occupants.isEmpty()) {
         this.init_V_BlockPos_List(cell, occupants);
      }

      for (Entity occupant : occupants) {
         if (!this.init_Z_Entity_BlockPos(occupant, cell)) {
            return false;
         }
      }

      return true;
   }

   private boolean write(BlockPos support) {
      return this.init_FileCache.init(support.above(), ck.init_ck().state_I());
   }

   private void init_V_BlockPos_List(BlockPos cell, List<Entity> occupants) {
      if (this.init_FileCache.init_I() != 0) {
         for (Entity occupant : occupants) {
            if (occupant instanceof EndCrystal && !this.init_RenderState.init(occupant, ck.init_ck().state_I())) {
               this.init_FileCache.init_V_Obj(cell);
               return;
            }
         }
      }
   }

   private void init_V_BlockPos_7(BlockPos cell) {
      int tick = ck.init_ck().state_I();
      this.init_RenderState.init(cell, tick);
      this.init_FileCache.init(cell, tick, autismclient.util.macro.ab.init_I());
   }

   private boolean init_Z_Entity_BlockPos(Entity occupant, BlockPos cell) {
      return this.bool("fast-replace") && occupant instanceof EndCrystal && this.init_RenderState.init(occupant, cell, ck.init_ck().state_I());
   }

   public static List<Entity> init_List_UseOnContext_List(UseOnContext context, List<Entity> occupants) {
      if (AutismLiteVariant.enabled()) {
         return occupants;
      }

      if (!occupants.isEmpty()
         && context.getLevel() == MC.level
         && context.getPlayer() == MC.player
         && autismclient.util.EventBus.init("crystal-aura", context.getClickedPos())) {
         if (al.init_Module_Str("crystal-aura") instanceof t aura && aura.isEnabled() && aura.bool("fast-replace")) {
            BlockPos cell = context.getClickedPos().above();
            return occupants.stream().filter(entity -> !aura.init_Z_Entity_BlockPos(entity, cell)).toList();
         } else {
            return occupants;
         }
      } else {
         return occupants;
      }
   }

   private boolean run() {
      int tick = ck.init_ck().state_I();
      int radius = Mth.clamp(Mth.ceil(MC.player.blockInteractionRange()), 0, 8);
      BlockPos origin = MC.player.blockPosition();

      for (EndCrystal dying : this.load_FileCache.init_Set_I(tick)) {
         if (MC.level.getEntity(dying.getId()) == dying && init_Z_BlockPos_8(origin, BlockPos.containing(dying.position()).below(), radius)) {
            return true;
         }
      }

      for (BlockPos cell : this.init_FileCache.init_Set_I(tick)) {
         if (init_Z_BlockPos_8(origin, cell.below(), radius)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init_Z_BlockPos_8(BlockPos origin, BlockPos support, int radius) {
      int dx = support.getX() - origin.getX();
      int dy = support.getY() - origin.getY();
      int dz = support.getZ() - origin.getZ();
      return dx * dx + dy * dy + dz * dz <= radius * radius;
   }

   private BlockHitResult init_BlockHitResult_t_NetHelper_ce_DataUtil(t.Inner_NetHelper plan, ce.Inner_DataUtil wire) {
      if (plan.init_InteractionHand() == null) {
         return null;
      } else if (plan.init_InteractionHand() == InteractionHand.OFF_HAND && autismclient.util.s.load_Z()) {
         return null;
      } else {
         BlockPos support = plan.init_BlockPos();
         if (!this.read(support)) {
            return null;
         } else {
            AABB nextBox = MC.player.getBoundingBox().move(MC.player.getDeltaMovement());
            if (this.init_Z_BlockPos_AABB_3(support, nextBox) && !this.write(support)) {
               return !load_Z_ItemStack_3(MC.player.getItemInHand(plan.init_InteractionHand()))
                  ? null
                  : autismclient.util.U.init_BlockHitResult_U_CachePool(
                     plan.init_U_CachePool(), wire, MC.player.getEyePosition(), plan.init_D(), plan.init_U_StateCache()
                  );
            } else {
               return null;
            }
         }
      }
   }

   private boolean init_Z_t_NetHelper_BlockHitResult(t.Inner_NetHelper plan, BlockHitResult hit) {
      if (hit == null) {
         return false;
      } else {
         InteractionHand hand = plan.init_InteractionHand();
         LivingEntity enemy = this.init_LivingEntity;
         if (al.init_Z_HitResult(hit, hand, "crystal-aura")) {
            return false;
         } else if (!autismclient.util.EventBus.init(
            "crystal-aura",
            hit,
            hand,
            () -> this.push()
               && this.init_ce_DataUtil_7() != null
               && this.init_BlockHitResult_t_NetHelper_ce_DataUtil(plan, this.init_ce_DataUtil_7()) != null
               && this.init_Z_LivingEntity_3(
                  enemy, new Vec3(plan.init_BlockPos().getX() + 0.5, plan.init_BlockPos().getY() + 1.0, plan.init_BlockPos().getZ() + 0.5), true
               ),
            () -> this.init_V_t_NetHelper(plan)
         )) {
            return false;
         } else if (!bM.init("crystal-aura")) {
            autismclient.util.s.write_V();
            return false;
         } else {
            return true;
         }
      }
   }

   private void init_V_t_NetHelper(t.Inner_NetHelper plan) {
      this.load_V_BlockPos_2(plan.init_BlockPos());
      this.read_V_22();
      this.init_BlockPos = plan.init_BlockPos().above();
      this.init_V_BlockPos_7(this.init_BlockPos);
      this.init_V_Z_11(false);
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)
         ? false
         : this.init_NetHelper.init(ck.init_ck().state_I());
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)
         ? false
         : hitResult instanceof EntityHitResult && "crystal-aura".equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z();
   }

   private t.Inner_TaskQueue init_t_TaskQueue_LivingEntity(LivingEntity enemy, autismclient.util.U.Inner_StreamUtil budget, double rivalDamage) {
      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      boolean offhand = init_Z_ItemStack_10(player.getOffhandItem()) && !autismclient.util.ah.load_Z("crystal-aura") && !autismclient.util.s.load_Z();
      int slot = offhand ? -1 : this.init_I_30();
      if (!offhand && slot < 0) {
         return null;
      }

      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      ItemStack material = offhand ? player.getOffhandItem() : player.getInventory().getItem(slot);
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = init_IArr_I_3(Mth.ceil(range));
      double floor = this.decimal("min-target-damage");
      double rangeSq = range * range;
      AABB nextBox = player.getBoundingBox().move(player.getDeltaMovement());
      AABB blastTarget = this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5);
      autismclient.util.S.Inner_CachePool targetOptions = this.init_S_CachePool.init(blastTarget);
      BlockState obsidianState = Blocks.OBSIDIAN.defaultBlockState();
      List<autismclient.util.U.Inner_TaskQueue> supportFaces = new ArrayList<>();
      AABB solidNextBox = autismclient.util.ByteReader.init(player);
      List<t.Inner_DataUtil> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         Vec3 source = new Vec3(cursor.getX() + 0.5, cursor.getY() + 1.0, cursor.getZ() + 0.5);
         if ((!"Aimed".equals(this.choice("aim-mode")) || cursor.equals(this.init_BlockPos_8())) && (!this.bool("only-above") || !(source.y < enemy.getY()))) {
            if (this.read(cursor)) {
               double supportBound = autismclient.util.S.load_D(enemy, source, 6.0F, targetOptions);
               if (supportBound >= floor && this.init_Z_BlockPos_AABB_3(cursor, nextBox) && this.init_Z_Vec3_9(eye, cursor, source)) {
                  autismclient.util.S.Inner_BufferHelper supportRank = this.init_S_BufferHelper_LivingEntity(enemy, source, blastTarget);
                  if (this.init_Z_LivingEntity_S_BufferHelper(enemy, supportRank)) {
                     rivalDamage = Math.max(rivalDamage, supportRank.load());
                  }
               }
            } else if (MC.level.getBlockState(cursor).canBeReplaced()
               && !AnchorAuraModule.init_Z_BlockPos(cursor)
               && MC.level.isUnobstructed(obsidianState, cursor, CollisionContext.empty())
               && !solidNextBox.intersects(new AABB(cursor))
               && (AutismLiteVariant.enabled() || !TaskTimer.init_Z_AABB_2(new AABB(cursor)))) {
               double distanceSq = eye.distanceToSqr(source);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.S.load_D(enemy, source, 6.0F, targetOptions);
                  if (!(bound < floor) && this.init_Z_BlockPos_AABB_3(cursor, nextBox) && !this.write(cursor)) {
                     supportFaces.clear();
                     autismclient.util.U.init(this.init(cursor.immutable(), material, hand, range, eye, this.load_ce_DataUtil_5()), supportFaces);
                     if (!supportFaces.isEmpty()) {
                        candidates.add(new t.Inner_DataUtil(cursor.immutable(), source, bound, distanceSq));
                     }
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return null;
      }

      candidates.sort(Comparator.comparingDouble(t.Inner_DataUtil::init_D).reversed());
      List<t.Inner_StateCache> accepted = new ArrayList<>();
      double bestDamage = -1.0;
      int evaluated = 0;

      for (t.Inner_DataUtil candidate : candidates) {
         if (accepted.size() >= 4 && candidate.init_D() <= bestDamage || evaluated >= 32) {
            break;
         }

         evaluated++;
         autismclient.util.S.Inner_BufferHelper rank = autismclient.util.TableIndex.init(
            enemy, candidate.init_Vec3(), 6.0F, this.init_S_CachePool.init(candidate.init_BlockPos()), blastTarget
         );
         if (this.init_Z_LivingEntity_S_BufferHelper(enemy, rank) && this.init_Z_Vec3_4(candidate.init_Vec3())) {
            accepted.add(new t.Inner_StateCache(candidate.init_BlockPos(), rank, candidate.load()));
            bestDamage = Math.max(bestDamage, rank.load());
         }
      }

      if (accepted.isEmpty()) {
         return null;
      }

      accepted.sort((first, second) -> {
         int byBucket = Double.compare(init_D_D_4(second.init_S_BufferHelper().load()), init_D_D_4(first.init_S_BufferHelper().load()));
         if (byBucket != 0) {
            return byBucket;
         }

         int bySelf = Double.compare(first.init_S_BufferHelper().read(), second.init_S_BufferHelper().read());
         return bySelf != 0 ? bySelf : Double.compare(second.init_D(), first.init_D());
      });
      double builderBest = accepted.get(0).init_S_BufferHelper().load();
      if (rivalDamage >= 0.0 && !init_Z_D_D_3(rivalDamage, builderBest)) {
         return null;
      }

      ce.Inner_DataUtil wire = this.load_ce_DataUtil_5();
      autismclient.util.U.Inner_JsonUtil[] refusal = new autismclient.util.U.Inner_JsonUtil[1];
      int limit = Math.min(4, accepted.size());

      for (int i = 0; i < limit; i++) {
         BlockPos cell = accepted.get(i).init_BlockPos();
         autismclient.util.U.Inner_StateCache request = this.init(cell, material, hand, range, eye, wire).init_U_StateCache_U_StreamUtil(budget);
         autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init(request, refusal);
         if (candidate != null) {
            InteractionHand resolved = offhand ? InteractionHand.OFF_HAND : this.init_InteractionHand_I_2(slot);
            return new t.Inner_TaskQueue(cell, resolved, range, candidate, accepted.get(i).init_S_BufferHelper().load(), slot);
         }

         if (refusal[0] == autismclient.util.U.Inner_JsonUtil.push) {
            return null;
         }

         refusal[0] = null;
      }

      return null;
   }

   private static boolean init_Z_D_D_3(double rivalDamage, double builderDamage) {
      return builderDamage >= rivalDamage + 5.0 && builderDamage >= rivalDamage * 2.0;
   }

   private autismclient.util.U.Inner_StateCache init(BlockPos cell, ItemStack stack, InteractionHand hand, double range, Vec3 eye, ce.Inner_DataUtil wire) {
      return new autismclient.util.U.Inner_StateCache(cell, eye, range, autismclient.util.U.init(stack, MC.player, hand))
         .init_U_StateCache_ce_DataUtil(wire)
         .init_U_StateCache_D(autismclient.util.U.init_D())
         .init_U_StateCache_Vec3(eye.add(MC.player.getDeltaMovement()))
         .init_U_StateCache_Z(MC.player.isSecondaryUseActive())
         .load(false);
   }

   private BlockHitResult init_BlockHitResult_t_TaskQueue_ce_DataUtil(t.Inner_TaskQueue plan, ce.Inner_DataUtil wire) {
      if (!AutismLiteVariant.enabled() && TaskTimer.init_Z_AABB_2(new AABB(plan.init_BlockPos()))) {
         return null;
      }

      if (plan.init_InteractionHand() == null) {
         return null;
      }

      if (plan.init_InteractionHand() == InteractionHand.OFF_HAND && autismclient.util.s.load_Z()) {
         return null;
      }

      BlockPos cell = plan.init_BlockPos();
      if (autismclient.util.ByteReader.init(MC.player).intersects(new AABB(cell))) {
         return null;
      }

      if (!MC.level.getBlockState(cell).canBeReplaced()) {
         return null;
      }

      if (!MC.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), cell, CollisionContext.empty())) {
         return null;
      }

      ItemStack held = MC.player.getItemInHand(plan.init_InteractionHand());
      if (!init_Z_ItemStack_10(held)) {
         return null;
      }

      Vec3 eye = MC.player.getEyePosition();
      return autismclient.util.U.init_BlockHitResult_U_CachePool(
         plan.init_U_CachePool(), wire, eye, plan.init_D(), this.init(cell, held, plan.init_InteractionHand(), plan.init_D(), eye, this.load_ce_DataUtil_5())
      );
   }

   private boolean init_Z_t_TaskQueue_BlockHitResult(t.Inner_TaskQueue plan, BlockHitResult hit) {
      return this.init_Z_t_TaskQueue(plan, hit, () -> {});
   }

   private boolean init_Z_t_TaskQueue(t.Inner_TaskQueue plan, BlockHitResult hit, Runnable dispatched) {
      if (hit == null) {
         return false;
      } else {
         InteractionHand hand = plan.init_InteractionHand();
         if (al.init_Z_HitResult(hit, hand, "crystal-aura")) {
            return false;
         } else if (!autismclient.util.EventBus.init(
            "crystal-aura",
            hit,
            hand,
            () -> this.push() && this.init_ce_DataUtil_7() != null && this.init_BlockHitResult_t_TaskQueue_ce_DataUtil(plan, this.init_ce_DataUtil_7()) != null,
            () -> {
               this.init_V_t_TaskQueue(plan);
               dispatched.run();
            }
         )) {
            return false;
         } else if (!bM.init("crystal-aura")) {
            autismclient.util.s.write_V();
            return false;
         } else {
            return true;
         }
      }
   }

   private void init_V_t_TaskQueue(t.Inner_TaskQueue plan) {
      this.flush_BlockPos = plan.init_BlockPos().immutable();
      this.load_V_BlockPos_2(plan.init_BlockPos());
      this.read_V_22();
      this.init_BlockPos = plan.init_BlockPos().above();
      this.init_Map.put(plan.init_BlockPos().immutable(), ck.init_ck().state_I() + 40);

      while (this.init_Map.size() > 16) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }

      this.init_V_Z_11(false);
   }

   private boolean update(BlockPos cell) {
      Integer until = this.init_Map.get(cell);
      if (until == null) {
         return false;
      } else if (ck.init_ck().state_I() - until >= 0) {
         this.init_Map.remove(cell);
         return false;
      } else {
         return true;
      }
   }

   private static boolean init_Z_ItemStack_10(ItemStack stack) {
      return !stack.isEmpty() && stack.is(Items.OBSIDIAN);
   }

   private int init_I_30() {
      LocalPlayer player = MC.player;
      int selected = player.getInventory().getSelectedSlot();
      int best = -1;
      int bestSteps = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (init_Z_ItemStack_10(player.getInventory().getItem(slot)) && !autismclient.util.ah.init(slot, "crystal-aura")) {
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

   private InteractionHand init_InteractionHand_I_2(int slot) {
      return this.init_InteractionHand_I_Z(slot, false);
   }

   private InteractionHand init_InteractionHand_I_Z(int slot, boolean defensive) {
      LocalPlayer player = MC.player;
      if (player.getInventory().getSelectedSlot() == slot) {
         if (auditBeforeDeadline(this.auditHasUpdateDeadline, this.update_J)) {
            return null;
         }

         this.prev = this.integer("switch-back-delay");
         return InteractionHand.MAIN_HAND;
      } else {
         int held = player.getInventory().getSelectedSlot();
         if (this.init_Z_I_Z_3(slot, defensive)) {
            if (this.stop < 0) {
               this.stop = held;
            }

            this.next = slot;
         }

         this.prev = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && !auditBeforeDeadline(this.auditHasUpdateDeadline, this.update_J) ? InteractionHand.MAIN_HAND : null;
      }
   }

   private t.Inner_TaskQueue init_t_TaskQueue_U_StreamUtil_Z(autismclient.util.U.Inner_StreamUtil budget, boolean imminentOnly) {
      if (!this.bool("defend")) {
         return null;
      }

      LocalPlayer player = MC.player;
      if (!SurroundModule.init_Z_27() && !ListPool.init_Z_10()) {
         if (!imminentOnly && this.pop()) {
            return null;
         }

         int tick = ck.init_ck().state_I();
         int cooldownAge = tick - this.pool;
         if (cooldownAge >= 0 && cooldownAge < 2) {
            return null;
         }

         int windowAge = tick - this.index;
         if (windowAge < 0 || windowAge >= 20) {
            this.index = tick;
            this.count = 0;
         }

         if (this.count >= 3) {
            return null;
         }

         Player enemy = this.init_Player();
         if (enemy == null) {
            return null;
         }

         double range = player.blockInteractionRange();
         if (range <= 0.0) {
            return null;
         }

         List<t.Inner_CachePool> threats = this.init_List_LocalPlayer(player, enemy, imminentOnly);
         if (threats.isEmpty()) {
            return null;
         }

         boolean offhand = init_Z_ItemStack_10(player.getOffhandItem()) && !autismclient.util.ah.load_Z("crystal-aura") && !autismclient.util.s.load_Z();
         int slot = offhand ? -1 : this.init_I_30();
         if (!offhand && slot < 0) {
            return null;
         }

         InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack material = offhand ? player.getOffhandItem() : player.getInventory().getItem(slot);
         Vec3 eye = player.getEyePosition();
         double rangeSq = range * range;
         AABB playerBox = player.getBoundingBox();
         Vec3 delta = player.getDeltaMovement();
         AABB nextBox = autismclient.util.ByteReader.init(player);
         double worst = threats.get(0).init_D();
         LinkedHashSet<BlockPos> candidates = new LinkedHashSet<>();
         List<List<BlockPos>> rays = threats.stream().map(threatx -> autismclient.util.SyncManager.init(playerBox, nextBox, threatx.init_Vec3())).toList();

         for (int i = 0; i < 24 && candidates.size() < 24; i++) {
            for (List<BlockPos> ray : rays) {
               if (i < ray.size() && candidates.size() < 24) {
                  candidates.add(ray.get(i));
               }
            }
         }

         List<t.Inner_StreamUtil> cells = new ArrayList<>();

         for (BlockPos cell : candidates) {
            if (!new AABB(cell).intersects(playerBox.move(delta.scale(-1.0))) && this.init_Z_BlockPos_9(cell, eye, rangeSq)) {
               double after = 0.0;

               for (t.Inner_CachePool threat : threats) {
                  after = Math.max(after, autismclient.util.S.read(player, threat.init_Vec3(), threat.init_F(), threat.init_S_CachePool().init(cell)));
               }

               double reduction = worst - after;
               if (reduction >= 2.0) {
                  cells.add(new t.Inner_StreamUtil(cell, reduction));
               }
            }
         }

         if (cells.isEmpty()) {
            return null;
         }

         cells.sort(Comparator.comparingDouble(t.Inner_StreamUtil::init_D).reversed());
         return this.init(cells, material, hand, slot, range, eye, budget);
      } else {
         return null;
      }
   }

   private t.Inner_TaskQueue init_t_TaskQueue_LivingEntity_U_StreamUtil(LivingEntity enemy, autismclient.util.U.Inner_StreamUtil budget) {
      if (this.bool("defend")
         && this.bool("place")
         && this.put()
         && !SurroundModule.init_Z_27()
         && !ListPool.init_Z_10()
         && !AnchorAuraModule.read_Z()
         && !this.load_List.isEmpty()) {
         int tick = ck.init_ck().state_I();
         if (this.chunk != tick) {
            return null;
         }

         if (tick - this.pool >= 0 && tick - this.pool < 2) {
            return null;
         }

         if (tick - this.index < 0 || tick - this.index >= 20) {
            this.index = tick;
            this.count = 0;
         }

         if (this.count >= 3) {
            return null;
         }

         LocalPlayer player = MC.player;
         boolean offhand = init_Z_ItemStack_10(player.getOffhandItem()) && !autismclient.util.ah.load_Z("crystal-aura") && !autismclient.util.s.load_Z();
         int slot = offhand ? -1 : this.init_I_30();
         if (!offhand && slot < 0) {
            return null;
         }

         InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack stack = offhand ? player.getOffhandItem() : player.getInventory().getItem(slot);
         Vec3 eye = player.getEyePosition();
         double range = player.blockInteractionRange();
         AABB next = autismclient.util.ByteReader.init(player);
         AABB targetBox = this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5);
         ArrayList<t.Inner_StreamUtil> viable = new ArrayList<>();

         for (t.Inner_StateCache support : this.load_List) {
            Vec3 source = Vec3.atBottomCenterOf(support.init_BlockPos().above());
            int tested = 0;

            for (BlockPos cell : autismclient.util.SyncManager.init(player.getBoundingBox(), next, source)) {
               if (!cell.equals(support.init_BlockPos())
                  && !cell.equals(support.init_BlockPos().above())
                  && !new AABB(cell).clip(eye, source).isPresent()
                  && this.init_Z_BlockPos_9(cell, eye, range * range)) {
                  if (++tested > 6) {
                     break;
                  }

                  autismclient.util.S.Inner_CachePool covered = this.init_S_CachePool.init(cell);
                  autismclient.util.S.Inner_BufferHelper rank = autismclient.util.TableIndex.init(enemy, source, 6.0F, covered, targetBox);
                  if (this.init_Z_LivingEntity_S_BufferHelper(enemy, rank)
                     && autismclient.util.SyncManager.init(
                        support.init_S_BufferHelper().load(),
                        support.init_S_BufferHelper().read(),
                        rank.load(),
                        rank.read(),
                        this.decimal("min-target-damage"),
                        this.decimal("max-self-damage"),
                        this.bool("efficient")
                     )
                     && (!this.bool("protect-friends") || autismclient.util.UiHelper.init(source, 6.0F, covered, this.decimal("max-friend-damage")))) {
                     viable.add(new t.Inner_StreamUtil(cell, rank.load() - rank.read()));
                  }
               }
            }
         }

         viable.sort(Comparator.comparingDouble(t.Inner_StreamUtil::init_D).reversed());
         return this.init(viable, stack, hand, slot, range, eye, budget);
      } else {
         return null;
      }
   }

   private boolean init_Z_BlockPos_9(BlockPos cell, Vec3 eye, double rangeSq) {
      if (MC.level.getBlockState(cell).canBeReplaced()
         && !AnchorAuraModule.init_Z_BlockPos(cell)
         && MC.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), cell, CollisionContext.empty())
         && !(eye.distanceToSqr(Vec3.atCenterOf(cell)) > rangeSq)) {
         if (!AutismLiteVariant.enabled() && TaskTimer.init_Z_AABB_2(new AABB(cell))) {
            return false;
         }

         for (Direction face : Direction.values()) {
            BlockPos support = cell.relative(face);
            if (autismclient.util.U.load(MC.level.getBlockState(support), support, false)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private t.Inner_TaskQueue init(
      List<t.Inner_StreamUtil> cells, ItemStack material, InteractionHand hand, int slot, double range, Vec3 eye, autismclient.util.U.Inner_StreamUtil budget
   ) {
      autismclient.util.U.Inner_JsonUtil[] refusal = new autismclient.util.U.Inner_JsonUtil[1];
      int limit = Math.min(4, cells.size());

      for (int i = 0; i < limit; i++) {
         t.Inner_StreamUtil cell = cells.get(i);
         autismclient.util.U.Inner_StateCache request = this.init(cell.init_BlockPos(), material, hand, range, eye, this.load_ce_DataUtil_5())
            .init_U_StateCache_U_StreamUtil(budget);
         autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init(request, refusal);
         if (candidate != null) {
            InteractionHand resolved = hand == InteractionHand.OFF_HAND ? hand : this.init_InteractionHand_I_Z(slot, true);
            return new t.Inner_TaskQueue(cell.init_BlockPos(), resolved, range, candidate, cell.init_D(), slot);
         }

         if (refusal[0] == autismclient.util.U.Inner_JsonUtil.push) {
            return null;
         }

         refusal[0] = null;
      }

      return null;
   }

   private Player init_Player() {
      LocalPlayer player = MC.player;
      double range = this.decimal("target-range");
      AABB search = player.getBoundingBox().inflate(range);
      List<Player> found = MC.level
         .getEntitiesOfClass(
            Player.class,
            search,
            entityx -> entityx != player && entityx.isAlive() && !entityx.isSpectator() && !ByteReader.load_Z(entityx) && !aL.init(entityx, "killaura")
         );
      Vec3 eye = player.getEyePosition();
      double rangeSq = range * range;
      Player best = null;
      double bestDistanceSq = 1.7976931348623157E308;

      for (Player entity : found) {
         double distanceSq = init_D_AABB_Vec3_3(entity.getBoundingBox(), eye);
         if (!(distanceSq > rangeSq) && distanceSq < bestDistanceSq) {
            bestDistanceSq = distanceSq;
            best = entity;
         }
      }

      return best;
   }

   private List<t.Inner_CachePool> init_List_LocalPlayer(LocalPlayer player, Player enemy, boolean imminentOnly) {
      double maximum = this.decimal("max-self-damage");
      ArrayList<t.Inner_CachePool> sources = new ArrayList<>();
      AABB near = player.getBoundingBox().inflate(5.0);

      for (EndCrystal crystal : MC.level.getEntitiesOfClass(EndCrystal.class, near, Entity::isAlive)) {
         if (!this.init_RenderState.init(crystal, ck.init_ck().state_I())) {
            double bound = autismclient.util.S.load(crystal.position(), 6.0F, this.init_S_CachePool);
            if (bound > maximum) {
               sources.add(new t.Inner_CachePool(crystal.position(), 6.0F, this.init_S_CachePool, bound));
            }
         }
      }

      BlockPos origin = player.blockPosition();
      int[] offsets = init_IArr_I_3(Mth.ceil(5.0));
      AABB nextBox = player.getBoundingBox().move(player.getDeltaMovement());
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         BlockState state = MC.level.getBlockState(cursor);
         if (state.getBlock() instanceof RespawnAnchorBlock && state.getValue(RespawnAnchorBlock.CHARGE) > 0 && !AnchorAuraModule.init_Z_BlockPos(cursor)) {
            Boolean works = MC.level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, cursor);
            if (!Boolean.TRUE.equals(works)) {
               BlockPos cell = cursor.immutable();
               autismclient.util.S.Inner_CachePool blast = this.init_S_CachePool.init(List.of(cell));
               Vec3 source = Vec3.atCenterOf(cell);
               double bound = autismclient.util.S.load(source, 5.0F, blast);
               if (bound > maximum) {
                  sources.add(new t.Inner_CachePool(source, 5.0F, blast, bound));
               }
            }
         } else if (!imminentOnly
            && this.read(cursor)
            && MC.level.isEmptyBlock(cursor.above())
            && !this.update(cursor)
            && this.init_Z_BlockPos_AABB_3(cursor, nextBox)) {
            Vec3 source = new Vec3(cursor.getX() + 0.5, cursor.getY() + 1, cursor.getZ() + 0.5);
            Vec3 direction = enemy.position().subtract(player.position());
            Vec3 offset = source.subtract(player.position());
            if ((!(direction.horizontalDistanceSqr() > 0.25) || !(offset.x * direction.x + offset.z * direction.z <= 0.0))
               && !(enemy.getEyePosition().distanceToSqr(Vec3.atCenterOf(cursor)) > enemy.blockInteractionRange() * enemy.blockInteractionRange())) {
               double bound = autismclient.util.S.load(source, 6.0F, this.init_S_CachePool);
               if (bound > maximum) {
                  sources.add(new t.Inner_CachePool(source, 6.0F, this.init_S_CachePool, bound));
               }
            }
         }
      }

      sources.sort(Comparator.comparingDouble(t.Inner_CachePool::init_D).reversed());
      ArrayList<t.Inner_CachePool> threats = new ArrayList<>();

      for (int i = 0; i < Math.min(12, sources.size()); i++) {
         t.Inner_CachePool source = sources.get(i);
         double damage = autismclient.util.S.read(player, source.init_Vec3(), source.init_F(), source.init_S_CachePool());
         if (damage > maximum) {
            threats.add(new t.Inner_CachePool(source.init_Vec3(), source.init_F(), source.init_S_CachePool(), damage));
         }
      }

      threats.sort(Comparator.comparingDouble(t.Inner_CachePool::init_D).reversed());
      return List.copyOf(threats.subList(0, Math.min(4, threats.size())));
   }

   private boolean load(t.Inner_TaskQueue plan, BlockHitResult hit) {
      return this.init_Z_t_TaskQueue(plan, hit, () -> {
         this.pool = ck.init_ck().state_I();
         this.count++;
      });
   }

   private void init_V_LivingEntity_t_TaskQueue(LivingEntity enemy, t.Inner_TaskQueue plan) {
      this.load_BlockPos = plan.init_BlockPos();
      this.data = plan.init_I();
      this.load_LivingEntity = enemy;
      this.push_D = plan.load();
      this.value = ck.init_ck().state_I() + 12;
      this.read_BlockPos = null;
   }

   private void init_V_LivingEntity_t_NetHelper(LivingEntity enemy, t.Inner_NetHelper plan) {
      this.read_BlockPos = plan.init_BlockPos();
      this.read_LivingEntity = enemy;
      this.init_HashUtil.init();
      this.cache = ck.init_ck().state_I() + 6;
      this.load_BlockPos = null;
   }

   private void load_V_BlockPos_2(BlockPos support) {
      this.write_BlockPos = support.immutable();
      this.update_BlockPos = support.above().immutable();
      this.state = ck.init_ck().state_I() + 10;
   }

   private void read_V_22() {
      this.load_BlockPos = null;
      this.read_BlockPos = null;
      this.load_LivingEntity = null;
      this.read_LivingEntity = null;
      this.init_HashUtil.init();
   }

   private boolean read(LivingEntity enemy) {
      if (this.load_BlockPos == null) {
         return false;
      }

      if (ck.init_ck().state_I() - this.value <= 0
         && enemy == this.load_LivingEntity
         && this.bool("place")
         && this.bool("obsidian")
         && this.put()
         && MC.level.getBlockState(this.load_BlockPos).canBeReplaced()
         && MC.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), this.load_BlockPos, CollisionContext.empty())
         && this.init_Z_BlockPos_AABB_3(this.load_BlockPos, MC.player.getBoundingBox().move(MC.player.getDeltaMovement()))
         && this.get()) {
         Vec3 heldSource = new Vec3(this.load_BlockPos.getX() + 0.5, this.load_BlockPos.getY() + 1.0, this.load_BlockPos.getZ() + 0.5);
         if (this.init_Z_LivingEntity_S_BufferHelper(
               enemy,
               autismclient.util.TableIndex.init(
                  enemy, heldSource, 6.0F, this.init_S_CachePool.init(this.load_BlockPos), this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5)
               )
            )
            && this.init_Z_Vec3_4(heldSource)) {
            BlockPos playerPos = MC.player.blockPosition();
            BlockPos enemyPos = enemy.blockPosition();
            if (!playerPos.equals(this.open_BlockPos) || !enemyPos.equals(this.poll_BlockPos)) {
               this.open_BlockPos = playerPos.immutable();
               this.poll_BlockPos = enemyPos.immutable();
               List<BlockPos> supports = this.init_List_LivingEntity_D_2(enemy, MC.player.blockInteractionRange());
               if (!supports.isEmpty()) {
                  this.level = enemy.getId();
                  this.close_BlockPos = supports.get(0);
                  this.frame = 0;
                  this.load_BlockPos = null;
                  return false;
               }
            }

            return true;
         } else {
            this.load_BlockPos = null;
            return false;
         }
      } else {
         this.load_BlockPos = null;
         return false;
      }
   }

   private boolean write(LivingEntity enemy) {
      if (this.close_BlockPos != null && enemy.getId() == this.level) {
         if (this.read(this.close_BlockPos)
            && this.init_Z_BlockPos_AABB_3(this.close_BlockPos, MC.player.getBoundingBox().move(MC.player.getDeltaMovement()))
            && this.init_Z_LivingEntity_S_BufferHelper(
               enemy,
               this.init_S_BufferHelper_LivingEntity(
                  enemy,
                  new Vec3(this.close_BlockPos.getX() + 0.5, this.close_BlockPos.getY() + 1.0, this.close_BlockPos.getZ() + 0.5),
                  this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5)
               )
            )) {
            return true;
         }

         this.close_BlockPos = null;
         return false;
      } else {
         return false;
      }
   }

   private boolean get() {
      LocalPlayer player = MC.player;
      return this.data < 0
         ? init_Z_ItemStack_10(player.getOffhandItem()) && !autismclient.util.ah.load_Z("crystal-aura")
         : !autismclient.util.ah.init(this.data, "crystal-aura") && init_Z_ItemStack_10(player.getInventory().getItem(this.data));
   }

   private t.Inner_TaskQueue init_t_TaskQueue_U_StreamUtil(autismclient.util.U.Inner_StreamUtil budget) {
      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      boolean offhand = this.data < 0;
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      ItemStack material = offhand ? player.getOffhandItem() : player.getInventory().getItem(this.data);
      autismclient.util.U.Inner_StateCache request = this.init(this.load_BlockPos, material, hand, range, eye, this.load_ce_DataUtil_5())
         .init_U_StateCache_U_StreamUtil(budget);
      autismclient.util.U.Inner_JsonUtil[] refusal = new autismclient.util.U.Inner_JsonUtil[1];
      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init(request, refusal);
      if (candidate == null) {
         if (refusal[0] != autismclient.util.U.Inner_JsonUtil.push) {
            this.load_BlockPos = null;
         }

         return null;
      } else {
         InteractionHand resolved = offhand ? InteractionHand.OFF_HAND : this.init_InteractionHand_I_2(this.data);
         return new t.Inner_TaskQueue(this.load_BlockPos, resolved, range, candidate, this.push_D, this.data);
      }
   }

   private boolean update(LivingEntity enemy) {
      if (this.read_BlockPos == null) {
         return false;
      }

      if (ck.init_ck().state_I() - this.cache <= 0
         && enemy == this.read_LivingEntity
         && this.bool("place")
         && this.put()
         && this.read(this.read_BlockPos)
         && this.init_Z_BlockPos_AABB_3(this.read_BlockPos, MC.player.getBoundingBox().move(MC.player.getDeltaMovement()))) {
         autismclient.util.S.Inner_BufferHelper rank = this.init_S_BufferHelper_LivingEntity(
            enemy,
            new Vec3(this.read_BlockPos.getX() + 0.5, this.read_BlockPos.getY() + 1.0, this.read_BlockPos.getZ() + 0.5),
            this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5)
         );
         if (this.init_Z_LivingEntity_S_BufferHelper(enemy, rank)
            && this.init_Z_Vec3_4(new Vec3(this.read_BlockPos.getX() + 0.5, this.read_BlockPos.getY() + 1.0, this.read_BlockPos.getZ() + 0.5))) {
            return true;
         }

         this.read_BlockPos = null;
         return false;
      } else {
         this.read_BlockPos = null;
         return false;
      }
   }

   private t.Inner_NetHelper load(LivingEntity enemy, autismclient.util.U.Inner_StreamUtil budget) {
      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      ce.Inner_DataUtil wire = this.load_ce_DataUtil_5();
      BlockPos support = this.read_BlockPos;
      BlockPos cell = support.above();
      BlockState state = MC.level.getBlockState(support);
      autismclient.util.U.Inner_StateCache request = new autismclient.util.U.Inner_StateCache(cell, eye, range, autismclient.util.U.init(support, init_EnumSet))
         .init_U_StateCache_ce_DataUtil(wire)
         .init_U_StateCache_Vec3(eye.add(player.getDeltaMovement()))
         .init_U_StateCache_U_StreamUtil(budget);
      List<autismclient.util.U.Inner_TaskQueue> scan = new ArrayList<>();
      this.init(support, state, cell, eye, wire, scan);
      autismclient.util.U.Inner_JsonUtil[] refusal = new autismclient.util.U.Inner_JsonUtil[1];
      int start = this.init_HashUtil.init(scan, optionx -> optionx);

      for (int step = 0; step < scan.size(); step++) {
         autismclient.util.U.Inner_TaskQueue option = scan.get((start + step) % scan.size());
         autismclient.util.U.Inner_DataUtil aim = autismclient.util.U.init(option, request, refusal);
         if (aim != null) {
            autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool_U_TaskQueue(option, aim, request, refusal);
            if (candidate != null) {
               this.init_HashUtil.init();
               InteractionHand hand = this.init_InteractionHand_5();
               Vec3 source = new Vec3(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5);
               double damage = this.init_S_BufferHelper_LivingEntity(enemy, source, this.init_AABB_LivingEntity(enemy, this.read_I_10(), 1.5)).load();
               return new t.Inner_NetHelper(support, hand, range, candidate, request, damage);
            }

            if (refusal[0] == autismclient.util.U.Inner_JsonUtil.push) {
               this.init_HashUtil.init(option);
               return null;
            }
         }
      }

      this.read_BlockPos = null;
      this.init_HashUtil.init();
      return null;
   }

   private static boolean load_Z_ItemStack_3(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL;
   }

   private BlockPos init_BlockPos_8() {
      if (this.init_HitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
         return MC.level.getBlockState(hit.getBlockPos()).canBeReplaced() ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());
      } else {
         return null;
      }
   }

   private boolean set() {
      return this.valid != ck.init_ck().state_I()
         && !auditBeforeDeadline(this.auditHasUpdateDeadline, this.update_J)
         && autismclient.util.UiHelper.init(MC.player, MC.player.getMainHandItem()) > 0.0;
   }

   private void write_V_21() {
      if (!this.set() && this.bool("anti-weakness") && this.valid != ck.init_ck().state_I()) {
         int best = -1;
         double bestDamage = 0.0;

         for (int slot = 0; slot < 9; slot++) {
            if (!autismclient.util.ah.init(slot, "crystal-aura")) {
               double damage = autismclient.util.UiHelper.init(MC.player, MC.player.getInventory().getItem(slot));
               if (damage > bestDamage) {
                  best = slot;
                  bestDamage = damage;
               }
            }
         }

         if (best < 0) {
            this.load_Str = "weakness";
         } else {
            int held = MC.player.getInventory().getSelectedSlot();
            if (best != held && this.load_Z_I_6(best)) {
               if (this.stop < 0) {
                  this.stop = held;
               }

               this.next = best;
            }

            this.prev = this.integer("switch-back-delay");
         }
      }
   }

   private boolean init_Z_Vec3_4(Vec3 source) {
      return !this.bool("protect-friends") || autismclient.util.UiHelper.init(source, 6.0F, this.init_S_CachePool, this.decimal("max-friend-damage"));
   }

   private boolean put() {
      LocalPlayer player = MC.player;
      return load_Z_ItemStack_3(player.getOffhandItem()) && !autismclient.util.ah.load_Z("crystal-aura") ? true : this.load_I_16() >= 0;
   }

   private InteractionHand init_InteractionHand_5() {
      LocalPlayer player = MC.player;
      if (load_Z_ItemStack_3(player.getOffhandItem()) && !autismclient.util.ah.load_Z("crystal-aura") && !autismclient.util.s.load_Z()) {
         return InteractionHand.OFF_HAND;
      }

      if (load_Z_ItemStack_3(player.getMainHandItem())) {
         if (auditBeforeDeadline(this.auditHasUpdateDeadline, this.update_J)) {
            return null;
         }

         this.prev = this.integer("switch-back-delay");
         return InteractionHand.MAIN_HAND;
      } else {
         int slot = this.load_I_16();
         if (slot < 0) {
            return null;
         }

         int held = player.getInventory().getSelectedSlot();
         if (this.load_Z_I_6(slot)) {
            if (this.stop < 0) {
               this.stop = held;
            }

            this.next = slot;
         }

         this.prev = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && !auditBeforeDeadline(this.auditHasUpdateDeadline, this.update_J) ? InteractionHand.MAIN_HAND : null;
      }
   }

   private boolean load_Z_I_6(int slot) {
      return this.init_Z_I_Z_3(slot, false);
   }

   private boolean init_Z_I_Z_3(int slot, boolean defensive) {
      if (!ListPool.init_Z_10() && !SurroundModule.init_Z_27() && (this.pop() || (defensive ? !AnchorAuraModule.read_Z() : !AnchorAuraModule.init_Z_2()))) {
         int tick = ck.init_ck().state_I();
         if (tick == this.valid) {
            return false;
         }

         if (!autismclient.util.ah.write_Z("crystal-aura")) {
            return false;
         }

         try {
            autismclient.util.aw.init_V(MC, slot);
            this.update_J = System.nanoTime() + this.integer("switch-delay") * 1000000L;
            this.auditHasUpdateDeadline = true;
         } finally {
            autismclient.util.ah.read_V("crystal-aura");
         }

         this.valid = tick;
         return true;
      } else {
         return false;
      }
   }

   private int load_I_16() {
      LocalPlayer player = MC.player;
      int selected = player.getInventory().getSelectedSlot();
      int best = -1;
      int bestSteps = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (load_Z_ItemStack_3(player.getInventory().getItem(slot)) && !autismclient.util.ah.init(slot, "crystal-aura")) {
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

   private void update_V_17() {
      if (this.stop >= 0 && MC.gui.screen() == null && MC.gui.overlay() == null) {
         if (this.next >= 0 && MC.player.getInventory().getSelectedSlot() != this.next) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!AnchorAuraModule.init_Z_I(selected) && !aa.load_Z_I_3(selected)) {
               this.stop = -1;
               this.next = -1;
               this.prev = 0;
            }
         } else if (this.prev > 0) {
            this.prev--;
         } else if (!autismclient.util.ah.init(this.stop, "crystal-aura") && this.load_Z_I_6(this.stop)) {
            this.stop = -1;
            this.next = -1;
         }
      }
   }

   private int read_I_10() {
      return Math.min(12, 2 + (int)Math.ceil(autismclient.util.macro.ab.init_I() / 50.0));
   }

   private AABB init_AABB_LivingEntity(LivingEntity enemy, int horizonTicks, double maxLead) {
      return this.init_TableIndex.init_AABB_LivingEntity(enemy, ck.init_ck().state_I(), horizonTicks, maxLead);
   }

   private autismclient.util.S.Inner_BufferHelper init_S_BufferHelper_LivingEntity(LivingEntity enemy, Vec3 source, AABB targetBox) {
      return autismclient.util.TableIndex.init(enemy, source, 6.0F, this.init_S_CachePool, targetBox);
   }

   private boolean init_Z_LivingEntity_S_BufferHelper(LivingEntity enemy, autismclient.util.S.Inner_BufferHelper rank) {
      return autismclient.util.S.init_Z_D(rank.read())
         ? false
         : autismclient.util.UiHelper.init(rank.load(), rank.read(), this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"));
   }

   private boolean init_Z_LivingEntity_3(LivingEntity enemy, Vec3 source, boolean placing) {
      return enemy != null
         && enemy.isAlive()
         && MC.level.getEntity(enemy.getId()) == enemy
         && !aL.init(enemy, "killaura")
         && !ByteReader.load_Z(enemy)
         && this.init_Z_LivingEntity_S_BufferHelper(
            enemy,
            this.init_S_BufferHelper_LivingEntity(enemy, source, this.init_AABB_LivingEntity(enemy, placing ? this.read_I_10() : 0, placing ? 1.5 : 0.5))
         )
         && this.init_Z_Vec3_4(source);
   }

   private boolean init_Z_S_BufferHelper_S_BufferHelper(autismclient.util.S.Inner_BufferHelper candidate, autismclient.util.S.Inner_BufferHelper incumbent) {
      int byTarget = Double.compare(candidate.load(), incumbent.load());
      return byTarget != 0 ? byTarget > 0 : candidate.read() < incumbent.read();
   }

   private boolean init_Z_Vec3_9(Vec3 eye, BlockPos support, Vec3 source) {
      HitResult hit = MC.level.clip(new ClipContext(eye, source, Block.COLLIDER, Fluid.NONE, MC.player));
      return hit.getType() == Type.MISS ? true : hit instanceof BlockHitResult block && block.getBlockPos().equals(support);
   }

   private boolean init_Z_Vec3_Vec3_3(Vec3 eye, Vec3 point) {
      HitResult hit = MC.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, MC.player));
      return hit.getType() != Type.MISS;
   }

   private static double init_D_AABB_Vec3_3(AABB box, Vec3 point) {
      double dx = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
      double dy = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
      double dz = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
      return dx * dx + dy * dy + dz * dz;
   }

   private static int[] init_IArr_I_3(int radius) {
      int clamped = Mth.clamp(radius, 0, 8);
      if (clamped == add) {
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
      add = clamped;
      return flat;
   }

   private void sync_V_12() {
      if (!this.bool("crystal-view")) {
         init_Z = false;
      } else {
         load_F = (float)this.decimal("view-size");
         read_F = (float)this.decimal("view-y");
         write_F = (float)this.decimal("view-spin");
         update_F = (float)this.decimal("view-bounce");
         init_Z = true;
      }
   }

   public static boolean open() {
      return init_Z;
   }

   public static float init_F_9() {
      return load_F;
   }

   public static float load_F_5() {
      return read_F;
   }

   public static float read_F_3() {
      return write_F;
   }

   public static float write_F_2() {
      return update_F;
   }

   private record Inner_BufferHelper(EndCrystal init_EndCrystal, autismclient.util.S.Inner_BufferHelper init_S_BufferHelper) {
   }

   private record Inner_CachePool(Vec3 init_Vec3, float init_F, autismclient.util.S.Inner_CachePool init_S_CachePool, double init_D) {
   }

   private record Inner_ConfigStore(EndCrystal init_EndCrystal, ce.Inner_DataUtil init_ce_DataUtil, double init_D) {
   }

   private record Inner_DataUtil(BlockPos init_BlockPos, Vec3 init_Vec3, double init_D, double load) {
   }

   private record Inner_JsonUtil(
      BlockPos init_BlockPos, autismclient.util.U.Inner_StateCache init_U_StateCache, autismclient.util.U.Inner_TaskQueue init_U_TaskQueue
   ) {
   }

   private record Inner_NetHelper(
      BlockPos init_BlockPos,
      InteractionHand init_InteractionHand,
      double init_D,
      autismclient.util.U.Inner_CachePool init_U_CachePool,
      autismclient.util.U.Inner_StateCache init_U_StateCache,
      double load
   ) {
   }

   private record Inner_StateCache(BlockPos init_BlockPos, autismclient.util.S.Inner_BufferHelper init_S_BufferHelper, double init_D) {
   }

   private record Inner_StreamUtil(BlockPos init_BlockPos, double init_D) {
   }

   private record Inner_SyncManager(autismclient.util.U.Inner_TaskQueue init_U_TaskQueue, double init_D) {
   }

   private record Inner_TaskQueue(
      BlockPos init_BlockPos,
      InteractionHand init_InteractionHand,
      double init_D,
      autismclient.util.U.Inner_CachePool init_U_CachePool,
      double load,
      int init_I
   ) {
   }
}
