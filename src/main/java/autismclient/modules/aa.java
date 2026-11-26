package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.ValueRange;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.cd;
import autismclient.util.ce;
import autismclient.util.ch;
import autismclient.util.ci;
import autismclient.util.cj;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.BlocksAttacks.DamageReduction;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

public final class aa extends Module implements cn.Inner_DataUtil {
   private String init_Str = "not_evaluated";
   private JsonArray init_JsonArray;
   private int push_I = -1;
   static final int init_I = 10;
   static final double init_D = 0.5;
   static final double load_D = 2.0;
   static final double read_D = 3.0;
   static final double write_D = 0.0;
   static final int load_I = 20;
   static final int read_I = 2;
   static final long init_J = 1000L;
   static final ValueRange init_ValueRange = new ValueRange(-100, 50);
   static final int write_I = 0;
   static final ValueRange load_ValueRange = new ValueRange(9, 13);
   static final ValueRange read_ValueRange = ba.update_ValueRange;
   static final int update_I = 20;
   static final int sync_I = 10;
   static final int flush_I = 100;
   static final int close_I = 3;
   static final int open_I = 8;
   static final double update_D = 0.1;
   static final double sync_D = 0.2;
   static final double flush_D = 0.1;
   static final double close_D = 0.02;
   static final long load_J = 60L;
   static final float init_F = 12.0F;
   static final float load_F = 6.0F;
   static final float read_F = 5.5F;
   static final float write_F = 1.2F;
   static final double open_D = 0.12;
   static final double poll_D = 0.2;
   private static final double[] init_DArr = new double[]{0.12, 0.31, 0.5, 0.69, 0.88};
   private static final double[] load_DArr = new double[]{0.2, 0.35, 0.5, 0.65, 0.8};
   private static long read_J;
   private static final SoundEvent init_SoundEvent = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("autismclient", "hitsound"));
   private final Random init_Random = new Random();
   private final aa.Inner_StreamUtil init_aa_StreamUtil = new aa.Inner_StreamUtil(new Random());
   private final aa.Inner_CachePool init_aa_CachePool = new aa.Inner_CachePool();
   private final h init_h;
   private final Random load_Random = new Random();
   private final aa.Inner_ConfigStore init_aa_ConfigStore = new aa.Inner_ConfigStore();
   private final aa.Inner_DataUtil init_aa_DataUtil = new aa.Inner_DataUtil();
   private final cj init_cj = new cj();
   private autismclient.util.q init_q;
   private final autismclient.util.q.Inner_DataUtil init_q_DataUtil = new autismclient.util.q.Inner_DataUtil();
   private Vec3 init_Vec3;
   private int pop_I = -1;
   private int tick_I = -1;
   private double run_D = 0.0 / 0.0;
   private int run_I = -1;
   private double get_D;
   private double set_D;
   private boolean init_Z;
   private boolean load_Z;
   private int get_I = 1000;
   private boolean read_Z;
   private final autismclient.util.a.TaskQueue init_TaskQueue = new autismclient.util.a.TaskQueue(new Random());
   private int set_I = -2147483648;
   private int put_I = -1;
   private int add_I = 1000;
   static final double push_D = 30.0;
   private LivingEntity init_LivingEntity;
   private double put_D;
   private double add_D = this.sync_D();
   private int clear_I = -1;
   private int reset_I = -1;
   private int start_I;
   private int stop_I = -2147483648;
   private int next = -2147483648;
   private int prev = -1;
   private int valid = -2147483648;
   private int size = -1;
   private int data;
   private static final int value = 4;
   private int cache = -1;
   private int state = -2147483648;
   private int queue = -1;
   private int buffer;
   private int entry;
   private final l.Inner_BufferHelper init_l_BufferHelper = new l.Inner_BufferHelper();
   private InteractionHand init_InteractionHand;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int node = -1;
   private int task = -1;
   private InteractionHand load_InteractionHand;
   private LivingEntity load_LivingEntity;
   private int pool = -2147483648;
   private InteractionHand read_InteractionHand;
   private ItemStack load_ItemStack = ItemStack.EMPTY;
   private int index = -1;
   private int count;
   private String load_Str = "";
   private boolean write_Z;
   private String read_Str = "";
   private Set<String> init_Set = Set.of();
   private final O init_O;
   private final aa.Inner_JsonUtil init_aa_JsonUtil = new aa.Inner_JsonUtil();
   private LivingEntity read_LivingEntity;
   private int flags = -2147483648;
   private int level = -2147483648;
   private double clear_D = 0.0 / 0.0;
   private int frame = -2147483648;
   private static final double reset_D = 0.25;
   private static final double start_D = 3.995;
   private static final double stop_D = 0.1;
   private static final int block = 10;
   private static final int chunk = 200;
   static final double pop_D = 12.0;
   static final double tick_D = 4.0;
   private int table = -2147483648;
   private int store = 2147483647;
   private final aa.Inner_JsonUtil load_aa_JsonUtil = new aa.Inner_JsonUtil();
   private boolean update_Z;
   static final int poll_I = 12;
   private int drain;
   private static final float update_F = 1.0E-4F;
   private float sync_F = 1.0F;
   private final autismclient.util.a.JsonUtil init_JsonUtil = new autismclient.util.a.JsonUtil();
   private float flush_F = this.read_F_2();

   static float init_F_D_Random(double lastStep, Random random) {
      float low = 0.90000004F;
      float cap = low + (1.2F - low) * random.nextFloat();
      if (lastStep > 0.0 && Math.abs(cap - lastStep) <= 0.1 * Math.max(cap, lastStep)) {
         cap = lastStep > 0.5 * (low + 1.2F) ? low : 1.2F;
      }

      return cap;
   }

   public aa() {
      super("kill-aura", "KillAura", ae.init_ae, "Automatically attacks configured entities.");
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").build());
      this.add(new ChoiceSetting("attack-mode", "Attack Mode", "Real Input", "Real Input", "Packet").description("How attacks are sent").build());
      if (!AutismLiteVariant.enabled()) {
         this.add(
            new ChoiceSetting("interactions", "Interactions", "Legit", "Legit", "Instant", "Block").description("Choose human, instant, or block.").build()
         );
      }

      this.add(
         new ChoiceSetting("targeting", "Targeting", "Smart", "Smart", "Type", "HP", "Distance", "FOV", "Hurt Time", "Age")
            .description("Who gets hit first")
            .build()
      );
      this.add(new IntSetting("fov", "FOV", 360, 10, 360, 10).group("Aim").description("Attack cone in degrees").build());
      this.add(new BoolSetting("fov-circle", "FOV Circle", false).group("Aim").description("Draw the attack cone when its edge is on screen").build());
      this.add(new ChoiceSetting("aim", "Aim", "Human", "Human", "Classic").group("Aim").description("Human: flicks, tracking, timing").build());
      this.add(autismclient.util.StateCache.init_DoubleSetting().build());
      this.add(new BoolSetting("silent", "Silent", true).group("Aim").build());
      this.add(new BoolSetting("move-fix", "Move Fix", true).group("Aim").description("Walk where you look").visibleWhen(() -> this.bool("silent")).build());
      this.add(new IntSetting("miss-chance", "Miss Chance", 0, 0, 20, 1).description("Miss rate after the opening hit; 0 = off").build());
      this.add(
         new RangeSetting("swing-offset", "Swing Offset", init_ValueRange, -150.0, 300.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Delay after full charge")
            .build()
      );
      this.init_h = new h(this, "1.8 Combat", h.init_BufferHelper, () -> true, h.init_ValueRange);
      this.add(
         new RangeSetting("pre-reach", "Pre-Reach", read_ValueRange, 0.0, 1.5, 0.05).group("1.8 Combat").description("Swing before reach, blocks").build()
      );
      this.add(new BoolSetting("criticals", "Criticals", false).description("Smart critical hits").build());
      this.init_O = new O(this);
      this.add(new BoolSetting("auto-sword", "Auto Sword", true).visibleWhen(() -> !this.init_O.init_Z()).build());
      this.add(new BoolSetting("shield-break", "Shield Break", true).build());
      AutismConfig.ModuleState saved = AutismConfig.getGlobal().modules.get(this.id());
      if (saved != null && !saved.settings.containsKey("block-hit")) {
         saved.settings.put("block-hit", Boolean.toString(init_Z_Map(saved.settings)));
      }

      this.add(new BoolSetting("block-hit", "Block Hit", false).description("Holds block between damaging hits.").build());
      this.add(
         new BoolSetting("block-hit-cps", "Respect CPS", false).description("Keep 1.8 CPS between blocks").visibleWhen(() -> this.bool("block-hit")).build()
      );
      this.add(
         new BoolSetting("block-hit-after-hit", "After First Hit", true)
            .description("Requires hit within 60 ticks.")
            .visibleWhen(() -> this.bool("block-hit"))
            .build()
      );
      this.add(new BoolSetting("switch-back", "Switch Back", false).visibleWhen(() -> this.bool("auto-sword") && !this.init_O.init_Z()).build());
      this.add(new ChoiceSetting("throwables", "Throwables", "MainHand", "MainHand", "BothHands").description("Hands checked for throwables").build());
      this.add(new BoolSetting("hit-marker", "Render", true).build());
      this.add(new BoolSetting("hitsound", "Hitsound", true).build());
      this.init_h.init_V();
   }

   static boolean init_Z_Map(Map<String, String> settings) {
      String current = settings.get("block-hit");
      return current != null
         ? Boolean.parseBoolean(current)
         : Boolean.parseBoolean(settings.get("auto-block")) || Boolean.parseBoolean(settings.get("perfect-hit"));
   }

   @Override
   protected String externalSettingValue(String settingId) {
      if (!"auto-block".equals(settingId) && !"perfect-hit".equals(settingId)) {
         return this.init_h == null ? null : this.init_h.init_Str_Str(settingId);
      } else {
         return this.value("block-hit");
      }
   }

   @Override
   protected boolean setExternalSettingValue(String settingId, String value) {
      if (!"auto-block".equals(settingId) && !"perfect-hit".equals(settingId)) {
         return this.init_h != null && this.init_h.init(settingId, value);
      }

      this.setValueTransient("block-hit", value);
      return true;
   }

   @Override
   public void onInputFrame() {
      if (MC != null && MC.player != null) {
         this.init_aa_CachePool.init_V_J(System.nanoTime());
      }
   }

   @Override
   public void onEnable() {
      this.stop_V_2();
   }

   @Override
   public void onDisable() {
      int back = this.clear_I;
      int borrowed = this.reset_I;
      this.stop_V_2();
      if (back >= 0 && borrowed >= 0 && this.add_Z_2()) {
         // Switch Back finishes in tick() once no screen is open.
         this.clear_I = back;
         this.reset_I = borrowed;
      }

      autismclient.util.s.init_V(this.id());
      autismclient.util.s.load_V();
      autismclient.util.aD.init("kill-aura");
   }

   @Override
   public void onGameLeft() {
      this.stop_V_2();
      if ("kill-aura".equals(autismclient.util.aD.init_Str())) {
         autismclient.util.aD.load_V();
      }

      this.init_aa_DataUtil.read_V();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (!AutismLiteVariant.enabled() && "interactions".equals(settingId)) {
         autismclient.util.ListPool.init_V();
      }

      if ("block-hit".equals(settingId)) {
         if (!this.bool("block-hit")) {
            this.load_V_Z_2(false);
         }

         this.init_l_BufferHelper.load();
      }

      if ("block-hit-after-hit".equals(settingId)) {
         this.load_V_Z_2(false);
      }

      if ("block-hit-cps".equals(settingId)) {
         this.load_V_Z_2(false);
         this.init_aa_CachePool.read_V();
      }

      if ("entities".equals(settingId)) {
         this.read_Str = null;
      }

      if ("auto-sword".equals(settingId) && !this.bool("auto-sword") || "held-items-only".equals(settingId) && this.init_O != null && this.init_O.init_Z()) {
         if (this.clear_I >= 0
            && MC != null
            && MC.player != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !HashUtil.load_Z()
            && autismclient.util.ah.write_Z(this.id())) {
            try {
               autismclient.util.aw.init_V(MC, this.clear_I);
            } finally {
               autismclient.util.ah.read_V(this.id());
            }
         }

         this.clear_I = -1;
         this.start_I = 0;
      }

      if ("switch-back".equals(settingId) && !this.bool("switch-back")) {
         this.clear_I = -1;
         this.start_I = 0;
      }

      if ("shield-break".equals(settingId) && !this.bool("shield-break")) {
         if (this.clear_I >= 0
            && MC != null
            && MC.player != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !HashUtil.load_Z()
            && autismclient.util.ah.write_Z(this.id())) {
            try {
               autismclient.util.aw.init_V(MC, this.clear_I);
            } finally {
               autismclient.util.ah.read_V(this.id());
            }
         }

         this.clear_I = -1;
         this.reset_I = -1;
         this.start_I = 0;
         this.prev = -1;
         this.next = -2147483648;
         this.valid = -2147483648;
         this.size = -1;
      }
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null) {
         autismclient.util.aD.init_V_Str_LocalPlayer("kill-aura", MC.player);
         if (this.clear_I >= 0) {
            if (!this.add_Z_2()) {
               this.clear_I = -1;
               this.reset_I = -1;
            } else if (MC.gui.screen() == null && MC.gui.overlay() == null && !HashUtil.load_Z()) {
               this.valid();
            }
         }
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.aD.read_Z() || this.clear_I >= 0;
   }

   @Override
   public void preMovementTick() {
      if (MC != null && MC.player != null && MC.level != null) {
         this.init_Str = "paused";
         this.push_I = -1;
         this.load_Str = "not_evaluated";
         this.init_JsonArray = null;
         if (!AutismLiteVariant.enabled()) {
            this.init_JsonArray = autismclient.util.v.init_Z() ? new JsonArray() : null;
         }

         this.write_Z = true;

         try {
            this.write_V_14();
         } finally {
            this.write_Z = false;
            if (!AutismLiteVariant.enabled() && autismclient.util.v.init_Z()) {
               this.get_V_2();
            }
         }
      }
   }

   private void write_V_14() {
      this.init_l_BufferHelper.init(this.load_D_2());
      this.poll_V_4();
      if (MC.gui.screen() == null && MC.gui.overlay() == null && (AutismLiteVariant.enabled() || !autismclient.ai.aP.load_Z())) {
         if (!cn.init_Z() && !aI.update_Z_5()) {
            if (!AutismLiteVariant.enabled() && autismclient.util.ListPool.write()) {
               this.load_Str = "manualInteraction";
               this.load_V_Z_2(false);
               this.init_LivingEntity = null;
               this.init_Vec3 = null;
               this.init_aa_ConfigStore.read_V();
               this.init_aa_CachePool.load_V();
               this.add_V_2();
               if (MC.player.isUsingItem()) {
                  this.data = 3;
               }

               this.init_aa_DataUtil.load_V();
            } else {
               this.push_V_3();
               if (!AutismLiteVariant.enabled() && !UtilCore.init_Z_23() && UtilCore.sync() && Double.isFinite(init_D_9()) && UtilCore.flush()) {
                  this.level = ck.init_ck().state_I();
               }

               if (MC.gui.screen() == null && MC.missTime > 10) {
                  MC.missTime = 0;
               }

               boolean usingItem = this.open_Z_4();
               if (usingItem) {
                  this.data = 3;
               } else if (this.data > 0) {
                  this.data--;
               }

               boolean throwable = this.run_Z_2();
               if (autismclient.util.aB.init(MC.options.keyAttack).autism$isActuallyDown() && this.stop_Z_2()) {
                  autismclient.util.t.init();
               }

               boolean wrongItem = !this.get_Z_2();
               if (!autismclient.util.DataPipe.init() && !usingItem && !throwable && !wrongItem) {
                  if (!AutismLiteVariant.enabled() && X.init_Z_24()) {
                     this.load_V_Z_2(false);
                     this.init_aa_CachePool.load_V();
                     this.add_V_2();
                     this.init_V_LivingEntity_3(this.init_LivingEntity);
                     this.init_Str = "displacement";
                  } else {
                     this.init_aa_CachePool.init_V();
                     this.add_V_2();
                     this.get_I = Math.min(this.get_I + 1, 1000);
                     this.add_I = Math.min(this.add_I + 1, 1000);
                     boolean postAttackWindow = this.init_aa_DataUtil.init_Z();
                     if (this.clear_Z_2()) {
                        this.read_V_Z(postAttackWindow);
                     } else {
                        this.load_V_Z_2(false);
                        this.init_LivingEntity = null;
                        this.init_Vec3 = null;
                        this.init_aa_ConfigStore.read_V();
                     }

                     this.init_V_LivingEntity_3(this.init_LivingEntity);
                     this.init_aa_CachePool.init_V_Z(this.init_LivingEntity != null && this.init_Z_Entity_6(this.init_LivingEntity));
                     if (this.init_LivingEntity != null && this.init_Z_Entity_6(this.init_LivingEntity)) {
                        autismclient.util.t.init();
                     }

                     this.start_V_2();
                     boolean inAttackWindow = postAttackWindow || this.prev();
                     boolean throttle = inAttackWindow && this.init_aa_DataUtil.load_Z();
                     this.init_q_DataUtil.pop_D = autismclient.util.StateCache.init_D(this.decimal("rotation-speed-multiplier"));
                     if ("Human".equals(this.choice("aim"))) {
                        this.init_V_Z_7(inAttackWindow);
                     }

                     float turnCap = autismclient.util.StateCache.init_F(this.init_q_DataUtil.pop_D);
                     double[] lastStep = autismclient.util.aD.init_DArr();
                     autismclient.util.aD.init(
                        "kill-aura",
                        MC.player,
                        !inAttackWindow ? turnCap : (throttle ? 5.5F : 12.0F),
                        !inAttackWindow ? turnCap : (throttle ? init_F_D_Random(lastStep[1], this.init_Random) : 6.0F)
                     );
                     this.run_V_2();
                     if (this.clear_Z_2()) {
                        this.set_V_2();
                        this.update_V_12();
                        this.sync_V_8();
                     } else {
                        this.init_aa_CachePool.load("canRun");
                     }

                     this.pop_V_2();
                     this.put_V_2();
                     if (!this.read_Z) {
                        this.close_V_6();
                     }
                  }
               } else {
                  this.load_Str = this.poll_Z_4()
                     ? "physicalUse"
                     : (usingItem ? "usingItem" : (throwable ? "throwable" : (wrongItem ? "wrongItem" : "blockIntent")));
                  this.load_V_Z_2(this.poll_Z_4());
                  this.init_LivingEntity = null;
                  this.init_aa_ConfigStore.read_V();
                  autismclient.util.aD.init("kill-aura");
                  this.init_aa_CachePool.load_V();
                  this.add_V_2();
                  autismclient.util.aD.init_V_Str_LocalPlayer("kill-aura", MC.player);
                  this.close_V_6();
               }
            }
         } else {
            if (!AutismLiteVariant.enabled()) {
               autismclient.util.ListPool.init_V();
            }

            this.load_V_Z_2(false);
            this.init_LivingEntity = null;
            this.init_aa_ConfigStore.read_V();
            this.init_aa_CachePool.init_V_Z(false);
            if ("kill-aura".equals(autismclient.util.aD.init_Str())) {
               autismclient.util.aD.load_V();
            }

            this.init_aa_DataUtil.load_V();
         }
      } else {
         autismclient.util.s.init_V(this.id());
         this.load_V_Z_2(false);
         if (!AutismLiteVariant.enabled()) {
            autismclient.util.ListPool.init_V();
         }

         this.init_LivingEntity = null;
         this.init_Vec3 = null;
         this.pop_I = -1;
         this.init_aa_StreamUtil.init_V();
         this.init_aa_ConfigStore.read_V();
         this.drain = 0;
         this.cache = -1;
         this.put_I = -1;
         this.init_aa_CachePool.read_V();
         this.add_V_2();
         autismclient.util.aD.init("kill-aura");
         autismclient.util.aD.init_V_Str_LocalPlayer("kill-aura", MC.player);
         this.close_V_6();
      }
   }

   private double init_D_Entity_2(Entity target) {
      if (target.getId() != this.frame || Double.isNaN(this.clear_D)) {
         this.frame = target.getId();
         this.clear_D = ValueRange.parse(this.value("pre-reach"), read_ValueRange).random(this.init_Random);
      }

      return this.clear_D;
   }

   private boolean init_Z_Entity_6(Entity target) {
      double reach = this.write_D() + ValueRange.parse(this.value("pre-reach"), read_ValueRange).max() + 0.5;
      return target.getBoundingBox().distanceToSqr(MC.player.getEyePosition()) <= reach * reach;
   }

   private double init_D_LivingEntity(LivingEntity target) {
      if (target != null && MC.player != null) {
         double distance = Math.max(0.5, MC.player.getEyePosition().distanceTo(target.getBoundingBox().getCenter()));
         double radius = target.getBbWidth() * 0.5;
         return Math.max(1.5, Math.min(20.0, Math.toDegrees(Math.atan2(radius, distance))));
      } else {
         return 4.0;
      }
   }

   private void init_V_Z_7(boolean inAttackWindow) {
      LivingEntity target = this.init_LivingEntity;
      if (target != null && this.init_Vec3 != null) {
         autismclient.util.q hand = this.init_q();
         boolean opening = autismclient.util.t.init(target);
         hand.init_V_D(this.integer("miss-chance") * 0.01);
         Vec3 eyes = MC.player.getEyePosition();
         AABB targetBox = target.getBoundingBox();
         Vec3 anchor = autismclient.util.q.Inner_DataUtil.init_Vec3_Vec3(eyes, targetBox, this.init_Vec3, this.write_D());
         AABB region = autismclient.util.q.Inner_DataUtil.init_AABB_Vec3(eyes, targetBox, anchor, this.write_D());
         AABB receivedBox = null;
         Vec3 heldPose = TaskTimer.init_Vec3_LivingEntity_Vec3(target, eyes);
         Vec3 sampled = heldPose != null ? heldPose : (this.init_cj.init_Z_LivingEntity(target) ? this.init_Vec3_4() : null);
         if (sampled != null) {
            receivedBox = targetBox.move(sampled.subtract(target.position()));
            double bottom = Math.max(region.minY, receivedBox.minY);
            double top = Math.min(region.maxY, receivedBox.maxY);
            if (top > bottom + 0.04) {
               region = new AABB(region.minX, bottom, region.minZ, region.maxX, top, region.maxZ);
               anchor = new Vec3(anchor.x, Mth.clamp(anchor.y, bottom + 0.02, top - 0.02), anchor.z);
            }
         }

         this.init_q_DataUtil.init(eyes, targetBox, region, anchor, sampled, receivedBox);
         this.init_q_DataUtil.poll_Z = true;
         this.init_q_DataUtil.open_D = 0.0;
         autismclient.util.aD.init_V_F_F(this.init_q_DataUtil.init_F, this.init_q_DataUtil.load_F);
         this.init_q_DataUtil.update_Z = true;
         this.init_q_DataUtil.sync_Z = true;
         this.init_q_DataUtil.flush_D = 12.0;
         this.init_q_DataUtil.close_D = 4.0;
         this.init_q_DataUtil.flush_Z = true;
         this.init_q_DataUtil.close_Z = true;
         this.init_q_DataUtil.open_Z = true;
         boolean stalled = this.drain >= 12;
         if (stalled) {
            this.init_aa_ConfigStore.read_V();
         }

         double ready = this.load_D_LivingEntity(target);
         boolean heldHitDue = heldPose != null && ready <= 2.0;
         this.init_q_DataUtil.read_Z = ready > 3.0 && !opening && !stalled && !this.init_aa_ConfigStore.init_Z() && !this.init_aa_DataUtil.read_Z();
         this.init_q_DataUtil.write_Z = opening || stalled || heldHitDue;
         this.init_q_DataUtil.sync_D = Math.max(0.0, ready - 1.0);
         this.init_q_DataUtil.init_I = this.get_I;
         this.init_q_DataUtil.init_Z = inAttackWindow;
         this.init_q_DataUtil.poll_D = ce.init();
         this.init_q_DataUtil.pop_Z = !this.buffer();
         this.init_q_DataUtil.push_D = this.get_I >= 4 && ready >= 3.5 ? 30.0 : 18.0;
         this.init_q_DataUtil.push_Z = !opening
            && !stalled
            && !heldHitDue
            && this.integer("miss-chance") > 0
            && this.init_Z_LivingEntity_Vec3(target, MC.player.getEyePosition())
            && (this.init_aa_ConfigStore.init_Z() || this.init_aa_ConfigStore.load_Z() && hand.init_Z_q_DataUtil(this.init_q_DataUtil));
         int id = target.getId();
         autismclient.util.aD.init_V_Str_2("kill-aura", (at, maxYaw, maxPitch) -> {
            this.init_q_DataUtil.load_Z = id != this.pop_I;
            this.pop_I = id;
            return hand.init(this.init_q_DataUtil, at.init(), at.load(), maxYaw, maxPitch);
         }, this.init_q_DataUtil.push_D);
      } else {
         if (target == null || this.init_Vec3 == null) {
            this.pop_I = -1;
         }

         autismclient.util.aD.init_V_Str_D("kill-aura", this.init_D_LivingEntity(target));
      }
   }

   public static void init_V_27() {
      if (!AutismLiteVariant.enabled()) {
         aa aura = init_aa();
         if (aura != null && aura.push_Z_3()) {
            aura.load_V_Z_2(false);
         }
      }
   }

   public static boolean init_Z_ce_DataUtil(ce.Inner_DataUtil goal, autismclient.util.q.Inner_DataUtil frame, boolean pinQuiet) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      aa aura = init_aa();
      if (aura != null && MC.player != null) {
         autismclient.util.aD.init_V_Str("kill-aura", 10, goal);
         if (!"kill-aura".equals(autismclient.util.aD.init_Str())) {
            return false;
         }

         aura.pop_I = -1;
         if (!pinQuiet) {
            autismclient.util.aD.init_V_F_F(frame.init_F, frame.load_F);
            autismclient.util.aD.init_V_Str_2("kill-aura", (at, yaw, pitch) -> aura.init_q().init(frame, at.init(), at.load(), yaw, pitch), frame.push_D);
         }

         autismclient.util.aD.init_V_Str_5("kill-aura", MC.player, pinQuiet);
         return true;
      } else {
         return false;
      }
   }

   private void init_V_Entity_Z(Entity target, boolean lastChance) {
      int gap = lastChance ? 0 : this.init_TaskQueue.init();
      this.set_I = gap > 0 ? ck.init_ck().state_I() + gap : -2147483648;
      this.put_I = target.getId();
   }

   private void update_V_12() {
      int tick = ck.init_ck().state_I();
      if (tick == this.set_I) {
         this.set_I = -2147483648;
         LivingEntity target = this.init_LivingEntity;
         if (!autismclient.util.s.close_Z()
            && this.buffer()
            && target != null
            && target.getId() == this.put_I
            && this.get_I != 0
            && this.add_I != 0
            && !this.push_Z_3()
            && this.load_LivingEntity == null
            && tick != this.pool
            && tick != this.stop_I
            && !MC.player.isUsingItem()
            && !this.init_aa_ConfigStore.init_Z()) {
            ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
            if (rotation == null) {
               rotation = ce.init(MC.player);
            }

            EntityHitResult hit = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
            if (hit != null && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), hit.getLocation()) && this.init_Z_Entity(target, hit, true)) {
            }
         }
      }
   }

   private void sync_V_8() {
      if (!autismclient.util.s.close_Z()) {
         LivingEntity target = this.init_LivingEntity;
         if (this.load_LivingEntity == null
            && ck.init_ck().state_I() != this.pool
            && !this.push_Z_3()
            && (target == null || this.init_Z_LivingEntity_3(target))
            && this.init_q != null
            && target != null
            && this.init_Vec3 != null
            && this.init_q_DataUtil.push_Z
            && !autismclient.util.t.init(target)
            && this.get_I != 0
            && this.add_I != 0
            && this.init_aa_ConfigStore.load_Z()
            && "Human".equals(this.choice("aim"))
            && (this.buffer() || this.init_aa_CachePool.init_Z())) {
            ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
            if (rotation != null && "kill-aura".equals(autismclient.util.aD.init_Str())) {
               Vec3 eyes = MC.player.getEyePosition();
               AABB box = target.getBoundingBox();
               if (this.init_Z_LivingEntity_Vec3(target, eyes) && init_Z_Vec3_6(eyes, box, rotation) && load_Z_Vec3_3(eyes, box, rotation)) {
                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  autismclient.util.G.init_V();
                  this.init_aa_CachePool.init_V_Str("nearMiss");
                  this.flush_V_7();
               }
            }
         }
      }
   }

   private void flush_V_7() {
      if (this.init_q != null) {
         this.init_q.read_V();
      }

      this.add_I = 0;
      this.init_aa_ConfigStore.load_V();
   }

   static boolean init_Z_Vec3_6(Vec3 eyes, AABB box, ce.Inner_DataUtil rotation) {
      Vec3 direction = Vec3.directionFromRotation(rotation.load(), rotation.init());
      Vec3 end = eyes.add(direction.scale(4.0));
      return !box.inflate(0.05).contains(eyes) && box.inflate(0.05).clip(eyes, end).isEmpty();
   }

   static boolean load_Z_Vec3_3(Vec3 eyes, AABB box, ce.Inner_DataUtil rotation) {
      Vec3 direction = Vec3.directionFromRotation(rotation.load(), rotation.init());
      Vec3 end = eyes.add(direction.scale(3.995));
      return box.inflate(0.25).clip(eyes, end).isPresent();
   }

   private boolean init_Z_LivingEntity_Vec3(LivingEntity target, Vec3 eyes) {
      return this.load_Z && this.get_I <= 10 ? target.tickCount >= 200 : false;
   }

   private autismclient.util.q init_q() {
      if (this.init_q == null) {
         this.init_q = autismclient.util.q.init_q();
      }

      return this.init_q;
   }

   private double load_D_LivingEntity(LivingEntity target) {
      double ticks;
      if (this.buffer()) {
         float delay = this.load_F_3();
         float offset = this.init_F_F_Entity(delay, target);
         ticks = Math.max(0.0, delay * this.sync_F + offset - 0.5 - this.update_I_2() - 1.0E-4F * delay);
      } else {
         ticks = this.init_aa_CachePool.init_I();
      }

      if (target.hurtTime > 1) {
         ticks = Math.max(ticks, target.hurtTime - 1);
      }

      return ticks;
   }

   private void init_V_LivingEntity_3(LivingEntity target) {
      this.init_cj.init(target, ck.init_ck().state_I());
      if (target == null) {
         this.run_I = -1;
         this.init_Z = false;
         this.load_Z = false;
      } else {
         if (target.getId() != this.run_I) {
            this.run_I = target.getId();
            this.init_Z = false;
            this.load_Z = false;
         } else {
            double moved = Math.hypot(target.getX() - this.get_D, target.getZ() - this.set_D);
            this.init_Z = moved >= 0.05;
            this.load_Z = moved >= 0.1;
         }

         this.get_D = target.getX();
         this.set_D = target.getZ();
         if (target.getId() == this.tick_I && !Double.isNaN(this.run_D)) {
            if (target.onGround() || target.getY() < this.run_D) {
               this.run_D = target.getY();
            }
         } else {
            this.tick_I = target.getId();
            this.run_D = target.getY();
         }
      }
   }

   private Vec3 init_Vec3_LivingEntity_D(LivingEntity entity, double range) {
      AABB box = entity.getBoundingBox();
      Vec3 eyes = MC.player.getEyePosition();
      double ground = entity.getId() == this.tick_I && !Double.isNaN(this.run_D) ? Math.min(this.run_D, entity.getY()) : entity.getY();
      Vec3 anchor = autismclient.util.q.Inner_DataUtil.init_Vec3_Vec3(
         eyes, box, autismclient.util.q.Inner_DataUtil.init(box, ground + this.init_q().init_D()), this.write_D()
      );
      return this.init_ce_DataUtil_Vec3(eyes, box, anchor, range * range) != null ? anchor : this.init_Vec3_Entity(entity, range, anchor);
   }

   private void close_V_6() {
      ce.Inner_DataUtil sent = autismclient.util.aD.init_ce_DataUtil();
      if (sent == null) {
         sent = ce.init(MC.player);
      }

      float perfectYaw = this.init_F_LivingEntity(this.init_LivingEntity);
      this.init_aa_DataUtil.init(sent.init(), sent.load(), perfectYaw);
      if (this.init_q != null) {
         boolean sampled = this.init_aa_DataUtil.init_J() <= 1050L && this.init_Z;
         this.init_q.init_V_D(sent.init(), sent.load(), perfectYaw, sampled);
      }
   }

   private float init_F_LivingEntity(LivingEntity target) {
      if (target != null && MC.player != null) {
         double diffX = target.getX() - MC.player.getX();
         double diffZ = target.getZ() - MC.player.getZ();
         return (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
      } else {
         return 0.0F / 0.0F;
      }
   }

   private boolean open_Z_4() {
      return MC.player != null && (MC.player.isUsingItem() && !this.push_Z_3() || this.poll_Z_4());
   }

   private boolean poll_Z_4() {
      if (!AutismLiteVariant.enabled()) {
         if (autismclient.util.ListPool.load()) {
            return false;
         }

         if (autismclient.util.ListPool.init_Z()) {
            return false;
         }

         if (autismclient.util.ListPool.read()) {
            return true;
         }
      }

      return MC != null && MC.options != null && autismclient.util.aB.init(MC.options.keyUse).autism$physicalForBody();
   }

   private boolean push_Z_3() {
      if (this.init_InteractionHand != null && this.id().equals(autismclient.util.s.init_Str()) && MC.player != null) {
         return !MC.player.isUsingItem()
            ? true
            : MC.player.getUsedItemHand() == this.init_InteractionHand && ItemStack.isSameItem(MC.player.getUseItem(), this.init_ItemStack);
      } else {
         return false;
      }
   }

   private void open_V_4() {
      this.read_InteractionHand = null;
      this.load_ItemStack = ItemStack.EMPTY;
      this.index = -1;
   }

   private void poll_V_4() {
      if (this.read_InteractionHand != null) {
         int tick = ck.init_ck().state_I();
         long age = (long)tick - this.pool;
         if (!AutismLiteVariant.enabled() && aC.write()) {
            this.open_V_4();
         } else if (!this.pop_Z_2()
            || !this.init_Z_Z_3(true)
            || !autismclient.util.ah.read_Z(this.id())
            || age < 0L
            || age >= 40L
            || this.poll_Z_4()
            || MC.player.getInventory().getSelectedSlot() != this.index
            || !ItemStack.isSameItem(MC.player.getItemInHand(this.read_InteractionHand), this.load_ItemStack)) {
            this.open_V_4();
         } else if (this.init_InteractionHand == null
            && MC.player.isUsingItem()
            && MC.player.getUsedItemHand() == this.read_InteractionHand
            && ItemStack.isSameItem(MC.player.getUseItem(), this.load_ItemStack)) {
            MC.gameMode.releaseUsingItem(MC.player);
            this.pool = tick;
            this.count++;
         }
      }
   }

   private void push_V_3() {
      if (this.init_InteractionHand != null) {
         if (this.poll_Z_4()) {
            this.load_V_Z_2(true);
         } else if (this.bool("block-hit")
            && ItemStack.isSameItem(MC.player.getItemInHand(this.init_InteractionHand), this.init_ItemStack)
            && this.load_Z_ItemStack_2(MC.player.getItemInHand(this.init_InteractionHand))
            && (this.init_InteractionHand != InteractionHand.MAIN_HAND || MC.player.getInventory().getSelectedSlot() == this.node)
            && this.id().equals(autismclient.util.s.init_Str())) {
            this.init_ItemStack = MC.player.getItemInHand(this.init_InteractionHand);
            if (this.pop_Z_2() && autismclient.util.s.init_Z(this.id()) && !MC.player.isUsingItem()) {
               this.load_V_Z_2(false);
            }
         } else {
            this.load_V_Z_2(false);
         }
      }
   }

   public static boolean init_Z_31() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      aa aura = init_aa();
      if (aura != null && MC.player != null && MC.level != null) {
         HitResult hit = MC.hitResult;
         if (hit instanceof EntityHitResult npc && ByteReader.init_Z(npc.getEntity())) {
            return false;
         } else if (hit != null && hit.getType() == Type.BLOCK) {
            return false;
         } else {
            return hit instanceof EntityHitResult entity && !(entity.getEntity() instanceof Player) ? false : aura.init_InteractionHand_2() != null;
         }
      } else {
         return false;
      }
   }

   public static boolean read_Z_14() {
      return al.init_Module_Str("kill-aura") instanceof aa aura && aura.isEnabled() && aura.push_Z_3() && !aura.poll_Z_4();
   }

   public static void load_V_20() {
      if (read_Z_14()) {
         ((aa)al.init_Module_Str("kill-aura")).init_V_Z_Z_3(false, true);
      }
   }

   private void load_V_Z_2(boolean handToPlayer) {
      this.init_V_Z_Z_3(handToPlayer, this.write_Z);
   }

   private void init_V_Z_Z_3(boolean handToPlayer, boolean tickHead) {
      InteractionHand hand = this.init_InteractionHand;
      ItemStack stack = this.init_ItemStack;
      boolean dispatched = autismclient.util.s.init_Z(this.id());
      if (hand != null && dispatched && MC.player != null) {
         this.pool = ck.init_ck().state_I();
      }

      if (hand != null) {
         if (!handToPlayer && dispatched && this.pop_Z_2()) {
            this.read_InteractionHand = hand;
            this.load_ItemStack = stack.copy();
            this.index = this.node;
         } else {
            this.open_V_4();
         }
      }

      this.init_InteractionHand = null;
      this.init_ItemStack = ItemStack.EMPTY;
      this.node = -1;
      this.task = -1;
      autismclient.util.s.load(this.id());
      if (!handToPlayer
         && hand != null
         && MC != null
         && MC.player != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && (
            dispatched && !MC.player.isUsingItem()
               || MC.player.isUsingItem() && MC.player.getUsedItemHand() == hand && ItemStack.isSameItem(MC.player.getUseItem(), stack)
         )) {
         if (tickHead) {
            MC.gameMode.releaseUsingItem(MC.player);
         } else {
            autismclient.util.s.init(hand, stack);
         }
      }
   }

   private InteractionHand init_InteractionHand_2() {
      ItemStack main = MC.player.getMainHandItem();
      if (this.load_Z_ItemStack_2(main)) {
         return InteractionHand.MAIN_HAND;
      }

      boolean useOnOnlyTool = main.is(ItemTags.AXES) || main.is(ItemTags.SHOVELS) || main.is(ItemTags.HOES);
      return this.load_Z_ItemStack_2(MC.player.getOffhandItem()) && (useOnOnlyTool || !autismclient.util.s.load_Z()) ? InteractionHand.OFF_HAND : null;
   }

   private boolean load_Z_ItemStack_2(ItemStack stack) {
      return !stack.isEmpty()
         && stack.getUseAnimation() == ItemUseAnimation.BLOCK
         && !MC.player.getCooldowns().isOnCooldown(stack)
         && stack.isItemEnabled(MC.level.enabledFeatures());
   }

   private static int init_I_ItemStack_2(ItemStack stack) {
      BlocksAttacks blocks = stack.get(DataComponents.BLOCKS_ATTACKS);
      return blocks == null ? 0 : Math.max(0, blocks.blockDelayTicks());
   }

   private void pop_V_2() {
      if (this.bool("block-hit")
         && this.clear_Z_2()
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && this.get_I != 0
         && this.add_I != 0
         && ck.init_ck().state_I() != this.stop_I
         && !autismclient.util.s.close_Z()) {
         InteractionHand hand = this.load_InteractionHand;
         LivingEntity threat = this.load_LivingEntity;
         if (threat == null || hand == null) {
            this.load_V_Z_2(false);
         } else if (!AutismLiteVariant.enabled() && j.read_Z_20()) {
            this.load_Str = "clutch";
            this.load_V_Z_2(false);
         } else {
            if (this.init_InteractionHand != null && hand != this.init_InteractionHand) {
               this.load_V_Z_2(false);
            }

            int raise = init_I_ItemStack_2(MC.player.getItemInHand(hand));
            int ownHit = this.read_I_6();
            this.push_I = ownHit;
            l.Inner_CachePool decision = this.pop_Z_2()
               ? l.init(true, this.init_InteractionHand != null, raise, ownHit)
               : l.init_l_CachePool_Z(true, this.init_InteractionHand != null, MC.player.hurtTime, raise, ownHit);
            if (ck.init_ck().state_I() == this.pool) {
               decision = l.Inner_CachePool.init;
            }

            if (decision == l.Inner_CachePool.init) {
               this.load_Str = this.init_InteractionHand != null ? "releaseForHit" : "attackSoon";
               this.load_V_Z_2(false);
            } else if (this.init_InteractionHand != null
               && MC.player.isUsingItem()
               && MC.player.getUseItem().has(DataComponents.CONSUMABLE)
               && MC.player.getUseItemRemainingTicks() <= 2) {
               this.load_Str = "renew";
               this.load_V_Z_2(false);
            } else if (autismclient.util.ah.read_Z(this.id())
               && (hand != InteractionHand.OFF_HAND || !autismclient.util.ah.load_Z(this.id()))
               && autismclient.util.s.init(this.id(), hand)) {
               this.load_Str = "held";
               this.init_InteractionHand = hand;
               this.init_ItemStack = MC.player.getItemInHand(hand);
               this.node = MC.player.getInventory().getSelectedSlot();
               this.task = threat.getId();
            } else {
               this.load_Str = "holdRefused";
               this.load_V_Z_2(false);
            }
         }
      } else {
         this.load_Str = !this.clear_Z_2()
            ? "canRun"
            : (this.get_I == 0 ? "attack" : (this.add_I == 0 ? "whiff" : (autismclient.util.s.close_Z() ? "keyAttack" : "other")));
         this.load_V_Z_2(this.poll_Z_4());
      }
   }

   private int read_I_6() {
      LivingEntity target = this.init_LivingEntity;
      if (this.pop_Z_2()) {
         return target == null ? 1 : Math.max(1, this.init_aa_CachePool.init_I());
      }

      if (target != null && target.isAlive()) {
         ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
         if (rotation == null) {
            rotation = ce.init(MC.player);
         }

         EntityHitResult hit = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
         int tick = ck.init_ck().state_I();
         int open = this.init_l_BufferHelper.init_I(target.getUUID(), target.hurtTime, tick, this.read_D());
         int falling = this.write_I_6();
         boolean strongerSoon = falling <= 1
            && this.init_l_BufferHelper.init_I(target.getUUID(), target.hurtTime, tick, this.init_D_Z_2(true)) == 0
            && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), l.init(target.getBoundingBox(), MC.player.getEyePosition()));
         if (strongerSoon) {
            open = Math.min(open, falling + 1);
         }

         if (hit != null && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), hit.getLocation())
            || strongerSoon && this.init_Z_LivingEntity(MC.player, MC.player.getEyePosition(), target.getBoundingBox())) {
            double click;
            if (this.buffer()) {
               float delay = this.load_F_3();
               click = Math.max(0.0, delay * this.sync_F + this.init_F_F_Entity(delay, target) - 0.5 - this.update_I_2() - 1.0E-4F * delay);
            } else {
               click = this.init_aa_CachePool.init_I();
            }

            return Math.max(1, (int)Math.ceil(Math.max(open, click)));
         } else {
            return 2147483647;
         }
      } else {
         return 2147483647;
      }
   }

   private void run_V_2() {
      this.load_InteractionHand = this.bool("block-hit") && this.clear_Z_2() ? this.init_InteractionHand_2() : null;
      this.load_Str = this.load_InteractionHand == null ? "noHand" : "";
      this.load_LivingEntity = this.load_InteractionHand == null ? null : this.init_LivingEntity_ItemStack(MC.player.getItemInHand(this.load_InteractionHand));
      if (this.load_LivingEntity != null) {
         this.load_Str = "threat";
      } else {
         this.load_V_Z_2(this.poll_Z_4());
      }

      if (this.init_LivingEntity == null && this.load_LivingEntity == null) {
         this.init_l_BufferHelper.init();
      }
   }

   private static double[] init_DArr_LivingEntity(LivingEntity enemy) {
      AttackRange range = enemy.getMainHandItem().get(DataComponents.ATTACK_RANGE);
      double reach = enemy.getAttribute(Attributes.ENTITY_INTERACTION_RANGE) == null ? 3.0 : enemy.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
      boolean creativeReach = enemy instanceof Player p && p.hasInfiniteMaterials();
      double minimum = range == null ? 0.0 : (creativeReach ? range.minCreativeReach() : range.minReach());
      if (range != null) {
         reach = creativeReach ? range.maxCreativeReach() : range.maxReach() * (enemy instanceof Player ? 1.0F : range.mobFactor());
      }

      double margin = range == null ? 0.0 : range.hitboxMargin();
      return new double[]{minimum, reach, margin};
   }

   private static boolean read_Z_ItemStack_2(ItemStack guard) {
      return guard.is(ItemTags.SWORDS) && init_Z_BlocksAttacks(guard.get(DataComponents.BLOCKS_ATTACKS));
   }

   static boolean init_Z_BlocksAttacks(BlocksAttacks blocks) {
      if (blocks != null && blocks.blockDelayTicks() == 0 && blocks.damageReductions().size() == 1) {
         DamageReduction reduction = blocks.damageReductions().getFirst();
         return reduction.type().isEmpty() && Math.abs(reduction.base() + 0.5F) < 1.0E-4F && Math.abs(reduction.factor() - 0.5F) < 1.0E-4F;
      } else {
         return false;
      }
   }

   static boolean init_Z_BlocksAttacks(BlocksAttacks blocks, Vec3 from, Vec3 threat, float yaw) {
      double angle = Math.abs(Mth.wrapDegrees(ce.init(threat, from).init() - yaw));

      for (DamageReduction reduction : blocks.damageReductions()) {
         if (angle <= reduction.horizontalBlockingAngle()) {
            return true;
         }
      }

      return false;
   }

   private LivingEntity init_LivingEntity_ItemStack(ItemStack guard) {
      BlocksAttacks blocks = guard.get(DataComponents.BLOCKS_ATTACKS);
      boolean everySide = read_Z_ItemStack_2(guard) || blocks == null;
      ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
      float yaw = rotation == null ? MC.player.getYRot() : rotation.init();
      AABB body = MC.player.getBoundingBox();
      LivingEntity best = null;
      double bestDistance = 1.7976931348623157E308;
      double nearestRejected = 1.7976931348623157E308;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity enemy && this.load_Z_LivingEntity(enemy)) {
            Vec3 eye = init_Vec3_LivingEntity_3(enemy).add(0.0, enemy.getEyeHeight(), 0.0);
            Vec3 view = load_Vec3_LivingEntity(enemy);
            double[] reach = init_DArr_LivingEntity(enemy);
            double distance = body.distanceToSqr(eye);
            String why = "threat";
            if (!l.init_Z_Vec3(eye, body, reach[0], reach[1] + 0.5, reach[2])) {
               why = "far";
            } else if (!l.init(eye, view, body)) {
               why = "facing";
            } else if (!this.init_Z_LivingEntity(enemy, eye, body)) {
               why = "sight";
            } else if (!this.init_l_BufferHelper.init_Z(enemy.getUUID(), enemy.hurtTime, ck.init_ck().state_I(), this.bool("block-hit-after-hit"))) {
               why = "beforeHit";
            } else if (!everySide && !init_Z_BlocksAttacks(blocks, MC.player.getEyePosition(), eye, yaw)) {
               why = "arc";
            }

            if (!AutismLiteVariant.enabled() && this.init_JsonArray != null && this.init_JsonArray.size() < 32) {
               JsonObject observation = new JsonObject();
               observation.addProperty("entity_id", enemy.getId());
               observation.add("eye", autismclient.util.v.load(eye));
               observation.add("view", autismclient.util.v.init(view));
               observation.add("victim_box", autismclient.util.v.init(body));
               observation.addProperty("near", Math.sqrt(distance));
               observation.addProperty("min_reach", reach[0]);
               observation.addProperty("reach", reach[1]);
               observation.addProperty("margin", reach[2] + 0.5);
               observation.addProperty("result", why);
               this.init_JsonArray.add(observation);
            }

            if ("threat".equals(why) && distance < bestDistance) {
               best = enemy;
               bestDistance = distance;
            } else if (!"threat".equals(why) && distance < nearestRejected) {
               nearestRejected = distance;
               this.load_Str = why;
            }
         }
      }

      return best;
   }

   private static Vec3 init_Vec3_LivingEntity_3(LivingEntity enemy) {
      return cj.init_Vec3_LivingEntity(enemy);
   }

   private static Vec3 load_Vec3_LivingEntity(LivingEntity enemy) {
      InterpolationHandler interpolation = enemy.getInterpolation();
      boolean live = interpolation != null && interpolation.hasActiveInterpolation();
      float yaw = enemy instanceof Player ? (live ? Compat263.interpYRot(interpolation) : enemy.getYRot()) : enemy.getYHeadRot();
      return Vec3.directionFromRotation(live ? Compat263.interpXRot(interpolation) : enemy.getXRot(), yaw);
   }

   private boolean init_Z_LivingEntity(LivingEntity enemy, Vec3 eye, AABB victim) {
      Vec3 nearest = l.init(victim, eye);
      Vec3 center = victim.getCenter();
      Vec3[] points = new Vec3[]{
         nearest,
         center,
         new Vec3(center.x, victim.maxY - 0.1, center.z),
         new Vec3(center.x, victim.minY + 0.1, center.z),
         new Vec3(victim.minX + 0.05, center.y, victim.minZ + 0.05),
         new Vec3(victim.maxX - 0.05, center.y, victim.maxZ - 0.05)
      };

      for (Vec3 point : points) {
         if (MC.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, enemy)).getType() == Type.MISS) {
            return true;
         }
      }

      return false;
   }

   private double load_D_2() {
      double clientTickMs = MC.level.tickRateManager().millisecondsPerTick() / Math.max(0.05, ai.init_F());
      return l.load(clientTickMs);
   }

   private boolean pop_Z_2() {
      return this.bool("block-hit") && this.bool("block-hit-cps") && !this.buffer();
   }

   private boolean init_Z_LivingEntity_3(LivingEntity target) {
      return this.bool("block-hit") && !this.pop_Z_2() && !this.sync(target)
         ? this.init_l_BufferHelper.load(target.getUUID(), target.hurtTime, ck.init_ck().state_I(), this.read_D())
         : true;
   }

   private double read_D() {
      return this.init_D_Z_2(false);
   }

   private double init_D_Z_2(boolean forceCritical) {
      ItemStack stack = MC.player.getMainHandItem();
      double base = MC.player.getAttributeValue(Attributes.ATTACK_DAMAGE);
      double charge = this.buffer() ? MC.player.getAttackStrengthScale(0.5F) : 1.0;
      base *= 0.2 + charge * charge * 0.8;
      if (forceCritical || this.cache() && MC.player.fallDistance > 0.0 && (!this.buffer() || !MC.player.isSprinting())) {
         base *= 1.5;
      }

      int sharp = this.init_I_ItemStack_ResourceKey(stack, Enchantments.SHARPNESS);
      return base + (sharp == 0 ? 0.0 : (this.buffer() ? (0.5 * sharp + 0.5) * charge : 1.25 * sharp));
   }

   private int write_I_6() {
      int tick = ck.init_ck().state_I();
      if (tick == this.table) {
         return this.store;
      }

      this.table = tick;
      this.store = 2147483647;
      Vec3 motion = MC.player.getDeltaMovement();
      if (this.load_Z_Z_2(false) && (!this.buffer() || !(MC.player.getAttackStrengthScale(0.5F) <= 0.9F) && !MC.player.isSprinting())) {
         if (MC.player.onGround() && MC.options.keyJump.isDown() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() <= 1) {
            motion = new Vec3(motion.x, Math.max(motion.y, ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower()), motion.z);
         }

         return this.store = l.init(MC.player.getBoundingBox(), motion, (body, step) -> Entity.collideBoundingBox(MC.player, step, body, MC.level, List.of()));
      } else {
         return this.store;
      }
   }

   public boolean sync_Z_8() {
      return this.push_Z_3();
   }

   public int init_I_20() {
      return this.task;
   }

   public int load_I_10() {
      return this.count;
   }

   private void get_V_2() {
      if (!AutismLiteVariant.enabled()) {
         try {
            JsonObject row = autismclient.util.v.init_JsonObject("aura_decision");
            row.add("self", autismclient.util.v.init_JsonObject(MC.player));
            row.addProperty("target_id", this.init_LivingEntity == null ? -1 : this.init_LivingEntity.getId());
            row.addProperty("threat_id", this.load_LivingEntity == null ? -1 : this.load_LivingEntity.getId());
            row.addProperty("attack_decision", this.init_Str);
            row.addProperty("guard_decision", this.load_Str);
            row.addProperty("guard_requested", this.init_InteractionHand != null);
            row.addProperty("guard_owned", this.push_Z_3());
            row.addProperty("guard_use_dispatched", autismclient.util.s.init_Z(this.id()));
            row.addProperty("guard_release_tick", this.pool);
            row.addProperty("ticks_until_own_hit", this.push_I);
            row.addProperty("block_hit", this.bool("block-hit"));
            row.addProperty("block_hit_after_hit", this.bool("block-hit-after-hit"));
            row.addProperty("block_hit_respect_cps", this.bool("block-hit-cps"));
            row.addProperty("block_hit_cps_active", this.pop_Z_2());
            row.addProperty("auto_block", this.bool("block-hit"));
            row.addProperty("perfect_hit", this.bool("block-hit"));
            row.addProperty("charge", MC.player.getAttackStrengthScale(0.5F));
            row.addProperty("attack_delay", this.load_F_3());
            row.addProperty("ticker", this.update_I_2());
            row.addProperty("post_use_suppress", this.data);
            row.addProperty("crit_wait_start", this.state);
            row.addProperty("reach", this.write_D());
            row.add("threats", this.init_JsonArray == null ? new JsonArray() : this.init_JsonArray);
            if (this.init_LivingEntity != null) {
               row.add("target", autismclient.util.v.init_JsonObject(this.init_LivingEntity));
               row.add("hit_window", this.init_l_BufferHelper.init_JsonObject_UUID_I(this.init_LivingEntity.getUUID(), ck.init_ck().state_I()));
               ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
               if (rotation == null) {
                  rotation = ce.init(MC.player);
               }

               row.addProperty("aim_yaw", rotation.init());
               row.addProperty("aim_pitch", rotation.load());
               EntityHitResult hit = this.init_EntityHitResult_Entity(this.init_LivingEntity, rotation, this.write_D(), 0.0);
               row.addProperty("ray_hits_target", hit != null);
               row.addProperty("item_range_legal", hit != null && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), hit.getLocation()));
               if (hit != null) {
                  row.add("ray_hit", autismclient.util.v.load(hit.getLocation()));
               }
            }

            autismclient.util.v.init(row);
         } catch (RuntimeException failure) {
            autismclient.util.v.init(failure);
         }
      }
   }

   static boolean init_Z_ItemStack_6(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      Item item = stack.getItem();
      return item instanceof EnderpearlItem
         || item instanceof SnowballItem
         || item instanceof EggItem
         || item instanceof ExperienceBottleItem
         || item instanceof ThrowablePotionItem
         || item instanceof WindChargeItem;
   }

   private boolean tick_Z_5() {
      if (MC.player == null) {
         return false;
      } else {
         ItemStack mainHand = MC.player.getMainHandItem();
         if (init_Z_ItemStack_6(mainHand)) {
            return true;
         } else {
            return !"BothHands".equals(this.choice("throwables")) && !mainHand.isEmpty() ? false : init_Z_ItemStack_6(MC.player.getOffhandItem());
         }
      }
   }

   private boolean run_Z_2() {
      return this.load_aa_JsonUtil.init(ck.init_ck().state_I(), this::tick_Z_5);
   }

   private boolean get_Z_2() {
      return this.init_aa_JsonUtil.init(ck.init_ck().state_I(), this::set_Z_2);
   }

   private boolean set_Z_2() {
      if (MC.player != null && this.init_O.init_Z()) {
         return this.clear_I >= 0 && this.reset_I == MC.player.getInventory().getSelectedSlot()
            ? true
            : this.init_O.init_Z_ItemStack(MC.player.getMainHandItem());
      } else {
         return true;
      }
   }

   private boolean put_Z_2() {
      return this.bool("auto-sword") && !this.init_O.init_Z();
   }

   private boolean add_Z_2() {
      return this.init_O.init_Z() ? this.bool("shield-break") : this.bool("switch-back");
   }

   private boolean clear_Z_2() {
      return this.init_Z_Z_3(false);
   }

   private boolean init_Z_Z_3(boolean allowReleasedUse) {
      return this.init_Z_Z_Z_2(allowReleasedUse, false);
   }

   private boolean init_Z_Z_Z_2(boolean allowReleasedUse, boolean bedBudget) {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && (AutismLiteVariant.enabled() || !autismclient.ai.aP.load_Z())
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && (AutismLiteVariant.enabled() || !autismclient.util.ListPool.update())
         && (allowReleasedUse || !this.open_Z_4())
         && !this.run_Z_2()
         && (bedBudget ? this.next() : this.get_Z_2())
         && (bedBudget || this.level != ck.init_ck().state_I())
         && !aD.init_Z()
         && !aC.init_Z()
         && !cd.init_Z()
         && !MacroExecutor.write_Z()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !f.load()
         && (AutismLiteVariant.enabled() || !z.sync_Z_12() && !j.init_Z_49())
         && !aI.write_Z_5()
         && !aI.flush_Z_5()
         && !aI.update_Z_5()
         && !ListPool.init_Z_10()
         && !aK.init_Z_27()
         && !t.init_Z_51()
         && !BufferHelper.init_Z_2()
         && !start_Z_2()
         && (AutismLiteVariant.enabled() || bedBudget || !X.init_Z_24())
         && (AutismLiteVariant.enabled() || bedBudget || !UtilCore.init_Z_23())
         && !HashUtil.load_Z()
         && !FieldCache.init_Z_6()
         && !TableIndex.init_Z_20();
   }

   private static boolean reset_Z_2() {
      return ((AutismMinecraftAccessor)MC).autism$getMissTime() > 0;
   }

   private static boolean start_Z_2() {
      return "auto-trap".equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z();
   }

   static boolean init_Z_Z_6(boolean windingDown, boolean enabled, boolean canRun, boolean scaffoldOwnsRotation) {
      return !scaffoldOwnsRotation && (windingDown || enabled && canRun);
   }

   @Override
   public boolean load_Z_23() {
      if (!AutismLiteVariant.enabled() && autismclient.util.ListPool.update()) {
         return !cn.init_Z();
      }

      boolean enabled = this.isEnabled();
      return init_Z_Z_6(autismclient.util.aD.init_Z(), enabled, enabled && this.clear_Z_2(), cn.init_Z());
   }

   public static boolean flush_Z_7() {
      return !(al.init_Module_Str("kill-aura") instanceof aa aura && !aura.bool("move-fix"));
   }

   @Override
   public boolean write_Z_10() {
      return flush_Z_7();
   }

   @Override
   public boolean update_Z_9() {
      return this.isEnabled() && !this.bool("silent");
   }

   public static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics) {
      if (!AutismLiteVariant.enabled()) {
         aa aura = init_aa();
         if (aura != null && aura.bool("fov-circle") && MC.player != null && !MC.player.isSpectator() && autismclient.e.HashUtil.init()) {
            CameraRenderState camera = MC.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
            if (camera.initialized) {
               Window window = MC.getWindow();
               int cone = aura.integer("fov");
               float radiusX = init_F_I(cone, window.getWidth(), camera.projectionMatrix.m00());
               float radiusY = init_F_I(cone, window.getHeight(), camera.projectionMatrix.m11());
               if (radiusX > 0.0F && radiusY > 0.0F && !(Math.hypot(window.getWidth() / (2.0F * radiusX), window.getHeight() / (2.0F * radiusY)) < 0.99)) {
                  graphics.pose().pushMatrix();

                  try {
                     float pixel = 1.0F / window.getGuiScale();
                     graphics.pose().scale(pixel, pixel);
                     r.init_V_GuiGraphicsExtract(graphics, window.getWidth() / 2.0F, window.getHeight() / 2.0F, radiusX, radiusY);
                  } finally {
                     graphics.pose().popMatrix();
                  }

                  graphics.nextStratum();
               }
            }
         }
      }
   }

   static float init_F_I(int cone, int pixels, float projectionScale) {
      return cone > 0 && cone < 180 && pixels > 0 && projectionScale > 0.0F
         ? (float)(Math.tan(Math.toRadians(cone / 2.0)) * pixels / 2.0 * projectionScale)
         : 0.0F;
   }

   private static aa init_aa() {
      return al.init_Module_Str("kill-aura") instanceof aa aura && aura.isEnabled() ? aura : null;
   }

   public LivingEntity init_LivingEntity_3() {
      return this.init_LivingEntity;
   }

   LivingEntity load_LivingEntity_3() {
      return this.isEnabled() && this.clear_Z_2() ? this.init_LivingEntity : null;
   }

   boolean init_Z_I_7(int holdMillis) {
      return this.init_LivingEntity != null && TaskTimer.init_Z_D_I(this.load_D_LivingEntity(this.init_LivingEntity), holdMillis);
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return this.isEnabled() && this.init_LivingEntity != null && this.clear_Z_2() ? hitResult == null || hitResult.getType() != Type.BLOCK : false;
   }

   private void read_V_Z(boolean inAttackWindow) {
      boolean allowExcursion = !this.init_aa_DataUtil.read_Z() && !this.init_aa_ConfigStore.init_Z();
      double interactionRange = this.write_D();
      double normalRangeSq = interactionRange * interactionRange;
      List<LivingEntity> targets = this.init_List_13();
      double maximumRange = init_D_D_7(this.put_D, interactionRange, this.update_D());
      double keepRange = maximumRange + 0.5;
      List<LivingEntity> filtered = new ArrayList<>();

      for (LivingEntity entity : targets) {
         if (init_Z_D(this.load_D_Entity(entity), maximumRange, entity == this.init_LivingEntity)) {
            filtered.add(entity);
         }
      }

      filtered.sort(Comparator.comparingInt(entityx -> this.load_D_Entity(entityx) <= normalRangeSq ? 0 : 1));
      LivingEntity chosen = null;
      ce.Inner_DataUtil chosenRotation = null;
      boolean human = "Human".equals(this.choice("aim"));
      this.init_Vec3 = null;
      int bestRangeBucket = filtered.isEmpty() ? 2147483647 : (this.load_D_Entity(filtered.get(0)) <= normalRangeSq ? 0 : 1);
      if (this.init_LivingEntity != null) {
         for (LivingEntity entity : filtered) {
            if (entity == this.init_LivingEntity) {
               int currentRangeBucket = this.load_D_Entity(entity) <= normalRangeSq ? 0 : 1;
               if (currentRangeBucket == bestRangeBucket) {
                  if (human) {
                     Vec3 anchor = this.init_Vec3_LivingEntity_D(entity, keepRange);
                     if (anchor != null) {
                        chosen = entity;
                        this.init_Vec3 = anchor;
                        chosenRotation = ce.init(anchor, MC.player.getEyePosition());
                     }
                  } else {
                     Vec3 preferred = this.init_Vec3_LivingEntity(entity, inAttackWindow, allowExcursion);
                     chosenRotation = this.load_ce_DataUtil_Entity(entity, keepRange, preferred);
                     if (chosenRotation != null) {
                        chosen = entity;
                     }
                  }
               }
               break;
            }
         }
      }

      if (chosen == null) {
         for (LivingEntity entity : filtered) {
            if (entity != this.init_LivingEntity) {
               AABB box = entity.getBoundingBox();
               Vec3 acquisition = init_Vec3_AABB_2(box, 0.5, 0.56, 0.5);
               ce.Inner_DataUtil rotation = this.init_ce_DataUtil_Entity(entity, maximumRange, acquisition);
               if (rotation != null) {
                  if (!human) {
                     Vec3 preferred = this.init_Vec3_LivingEntity(entity, inAttackWindow, allowExcursion);
                     ce.Inner_DataUtil tracked = this.load_ce_DataUtil_Entity(entity, maximumRange, preferred);
                     chosen = entity;
                     chosenRotation = tracked != null ? tracked : rotation;
                     break;
                  }

                  Vec3 anchor = this.init_Vec3_LivingEntity_D(entity, maximumRange);
                  if (anchor != null) {
                     chosen = entity;
                     this.init_Vec3 = anchor;
                     chosenRotation = ce.init(anchor, MC.player.getEyePosition());
                     break;
                  }
               }
            }
         }
      }

      if (chosenRotation != null) {
         chosenRotation = this.init_ce_DataUtil_LivingEntity_ce_DataUtil(chosen, chosenRotation);
         autismclient.util.aD.init_V(chosenRotation);
      } else {
         this.init_aa_StreamUtil.init_V();
         this.init_aa_ConfigStore.read_V();
      }

      this.init_LivingEntity = chosen;
   }

   private Vec3 init_Vec3_LivingEntity(LivingEntity entity, boolean inAttackWindow, boolean allowExcursion) {
      boolean ready = this.load_D_LivingEntity(entity) <= 3.0;
      AABB box = entity.getBoundingBox();
      Vec3 point = this.init_aa_StreamUtil.load(entity.getId(), box, MC.player.getEyePosition(), inAttackWindow || ready, allowExcursion && !ready);
      return ready ? init_Vec3_AABB_Vec3(box, point) : point;
   }

   private boolean stop_Z_2() {
      Vec3 eyes = MC.player.getEyePosition();
      double reach = this.write_D() + 1.0;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && living.getBoundingBox().distanceToSqr(eyes) <= reach * reach && this.load(living, eyes)) {
            return true;
         }
      }

      return false;
   }

   static double init_D_9() {
      aa aura = init_aa();
      if (aura == null) {
         return 1.0 / 0.0;
      }

      if (aura.init_Z_Z_Z_2(false, true) && !autismclient.util.DataPipe.init()) {
         int tick = ck.init_ck().state_I();
         Vec3 eyes = MC.player.getEyePosition();
         double range = aura.write_D();

         for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living
               && living.isAlive()
               && aura.init_Z_LivingEntity_2(living, eyes, true)
               && living.getBoundingBox().distanceToSqr(eyes) <= range * range
               && MC.player.hasLineOfSight(living)) {
               aura.read_LivingEntity = living;
               aura.flags = tick;
               break;
            }
         }

         LivingEntity threat = aura.read_LivingEntity;
         boolean relevant = threat != null
            && threat.isAlive()
            && threat.level() == MC.level
            && aura.init_Z_LivingEntity_2(threat, eyes, true)
            && threat.getBoundingBox().distanceToSqr(eyes) <= (range + 1.0) * (range + 1.0);
         if (!init_Z_Z_J(relevant, (long)tick - aura.flags)) {
            aura.read_LivingEntity = null;
            return 1.0 / 0.0;
         } else {
            return init_I_I_4(MC.player.hurtTime);
         }
      } else {
         aura.read_LivingEntity = null;
         return 1.0 / 0.0;
      }
   }

   private boolean next() {
      return AutismLiteVariant.enabled() ? this.set_Z_2() : !this.init_O.init_Z() || this.set_Z_2() || this.init_O.init_Z_ItemStack(UtilCore.init_ItemStack());
   }

   static boolean init_Z_Z_J(boolean relevant, long ticksSinceSeen) {
      return relevant && ticksSinceSeen >= 0L && ticksSinceSeen <= 6L;
   }

   static int init_I_I_4(int hurtTime) {
      return hurtTime > 2 ? Math.min(6, hurtTime - 2) : 2;
   }

   static boolean init_Z_D(double boxedDistanceSqr, double range, boolean alreadyTargeted) {
      double limit = alreadyTargeted ? range + 0.5 : range;
      return boxedDistanceSqr <= limit * limit;
   }

   static double init_D_D_7(double closestDistanceSqr, double interactionRange, double scanRange) {
      return closestDistanceSqr > interactionRange * interactionRange ? scanRange : interactionRange;
   }

   private List<LivingEntity> init_List_13() {
      List<LivingEntity> entities = new ArrayList<>();
      Vec3 eyes = MC.player.getEyePosition();

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && this.load(living, eyes)) {
            entities.add(living);
         }
      }

      if (entities.isEmpty()) {
         return entities;
      }

      entities.sort(this.init_Comparator_2());
      double closest = 1.7976931348623157E308;

      for (LivingEntity entity : entities) {
         closest = Math.min(closest, this.load_D_Entity(entity));
      }

      this.put_D = closest;
      return entities;
   }

   private boolean load(LivingEntity entity, Vec3 eyes) {
      return this.init_Z_LivingEntity_2(entity, eyes, false);
   }

   private boolean init_Z_LivingEntity_2(LivingEntity entity, Vec3 eyes, boolean priorityThreat) {
      if (entity == MC.player) {
         return false;
      } else if (entity.isRemoved()) {
         return false;
      } else if (!priorityThreat && entity.hurtTime > 10) {
         return false;
      } else {
         return !this.load_Z_LivingEntity(entity) ? false : this.init_F_Entity_Vec3_2(entity, eyes) <= this.integer("fov") * 0.5F;
      }
   }

   private boolean load_Z_LivingEntity(LivingEntity entity) {
      if (!(entity instanceof Attackable)) {
         return false;
      } else if (!EntitySelector.CAN_BE_PICKED.test(entity)) {
         return false;
      } else if (entity == MC.player || entity.hasPassenger(MC.player)) {
         return false;
      } else if (ByteReader.load_Z(entity)) {
         return false;
      } else if (aL.init(entity, "killaura")) {
         return false;
      } else if (!entity.isAlive()) {
         return false;
      } else {
         return entity instanceof Player player && player.isSleeping() ? false : this.load_Z_Entity(entity);
      }
   }

   private float init_F_Entity_Vec3_2(Entity entity, Vec3 eyes) {
      ce.Inner_DataUtil toCenter = ce.init(entity.getBoundingBox().getCenter(), eyes);
      return ce.load(ce.init(MC.player), toCenter);
   }

   private boolean load_Z_Entity(Entity entity) {
      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      Set<String> ids = this.init_Set_9();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> init_Set_9() {
      String source = this.value("entities");
      if (Objects.equals(source, this.read_Str)) {
         return this.init_Set;
      }

      List<String> entries = this.list("entities");
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

   private int init_I_LivingEntity_2(LivingEntity entity) {
      if (entity instanceof Player) {
         return 0;
      }

      if (entity instanceof Enemy) {
         return 1;
      }

      if (entity instanceof NeutralMob neutral) {
         EntityReference<LivingEntity> angerTarget = neutral.getPersistentAngerTarget();
         if (angerTarget != null && angerTarget.matches(MC.player)) {
            return 2;
         }
      }

      return 2147483647;
   }

   private Comparator<LivingEntity> init_Comparator_2() {
      Comparator var10000;
      switch (this.choice("targeting")) {
         case "Smart":
            var10000 = Comparator.comparingDouble(this::read_D_LivingEntity);
            break;
         case "HP":
            var10000 = Comparator.comparingDouble(this::load_F_LivingEntity);
            break;
         case "Distance":
            var10000 = Comparator.comparingDouble(this::load_D_Entity);
            break;
         case "FOV":
            var10000 = Comparator.<LivingEntity>comparingDouble(entity -> this.init_F_Entity_Vec3_2(entity, MC.player.getEyePosition()));
            break;
         case "Hurt Time":
            var10000 = Comparator.<LivingEntity>comparingInt(entity -> entity.hurtTime);
            break;
         case "Age":
            var10000 = Comparator.<LivingEntity>comparingInt(entity -> -entity.tickCount);
            break;
         default:
            var10000 = Comparator.comparingInt(this::init_I_LivingEntity_2);
      }

      return var10000;
   }

   private double read_D_LivingEntity(LivingEntity entity) {
      ce.Inner_DataUtil look = "kill-aura".equals(autismclient.util.aD.init_Str()) ? autismclient.util.aD.init_ce_DataUtil() : null;
      if (look == null) {
         look = ce.init(MC.player);
      }

      return autismclient.util.x.init(entity, MC.player, look);
   }

   private float load_F_LivingEntity(LivingEntity entity) {
      try {
         Scoreboard scoreboard = entity.level().getScoreboard();
         Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
         if (objective != null) {
            String displayName = objective.getDisplayName().getString();
            if (displayName.contains("❤")
               || displayName.contains("HP")
               || displayName.contains("Health")
               || displayName.contains("Здоровья")
               || displayName.contains("Здоровье")) {
               ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(entity, objective);
               if (score != null) {
                  return (float)score.value();
               }
            }
         }
      } catch (Throwable var6) {
      }

      return entity.getHealth();
   }

   private double load_D_Entity(Entity entity) {
      return entity.getBoundingBox().inflate(entity.getPickRadius()).distanceToSqr(MC.player.getEyePosition());
   }

   private ce.Inner_DataUtil init_ce_DataUtil_Entity(Entity entity, double range, Vec3 preferredPoint) {
      Vec3 point = this.init_Vec3_Entity(entity, range, preferredPoint);
      return point == null ? null : ce.init(point, MC.player.getEyePosition());
   }

   private Vec3 init_Vec3_Entity(Entity entity, double range, Vec3 preferredPoint) {
      Vec3 eyes = MC.player.getEyePosition();
      AABB box = entity.getBoundingBox();
      double rangeSq = range * range;
      Vec3 preferred = init_Vec3_AABB_Vec3(box, preferredPoint);
      if (this.init_ce_DataUtil_Vec3(eyes, box, preferred, rangeSq) != null) {
         return preferred;
      }

      ce.Inner_DataUtil reference = autismclient.util.aD.init_ce_DataUtil();
      if (reference == null) {
         reference = ce.init(MC.player);
      }

      Vec3 best = null;
      double bestScore = 1.0 / 0.0;
      double diagonal = Math.sqrt(box.getXsize() * box.getXsize() + box.getYsize() * box.getYsize() + box.getZsize() * box.getZsize());
      diagonal = Math.max(diagonal, 1.0E-6);

      for (double x : init_DArr) {
         for (double y : load_DArr) {
            for (double z : init_DArr) {
               Vec3 point = init_Vec3_AABB_2(box, x, y, z);
               ce.Inner_DataUtil candidate = this.init_ce_DataUtil_Vec3(eyes, box, point, rangeSq);
               if (candidate != null) {
                  double pointDistance = Math.sqrt(point.distanceToSqr(preferred)) / diagonal;
                  double score = ce.load(reference, candidate) + pointDistance * 8.0;
                  if (score < bestScore) {
                     best = point;
                     bestScore = score;
                  }
               }
            }
         }
      }

      return best;
   }

   private ce.Inner_DataUtil load_ce_DataUtil_Entity(Entity entity, double range, Vec3 point) {
      return !entity.getBoundingBox().contains(point) ? ce.init(point, MC.player.getEyePosition()) : this.init_ce_DataUtil_Entity(entity, range, point);
   }

   private ce.Inner_DataUtil init_ce_DataUtil_Vec3(Vec3 eyes, AABB box, Vec3 point, double rangeSq) {
      if (eyes.distanceToSqr(point) <= 1.0E-12) {
         return null;
      } else {
         Vec3 hit = box.contains(eyes) ? eyes : init_Vec3_AABB_3(box, eyes, init_Vec3_Vec3_4(eyes, 1.25, point.subtract(eyes)));
         if (hit != null && eyes.distanceToSqr(hit) < rangeSq) {
            return !this.init_Z_Vec3_Vec3_2(eyes, hit) ? null : ce.init(point, eyes);
         } else {
            return null;
         }
      }
   }

   static Vec3 init_Vec3_AABB_2(AABB box, double x, double y, double z) {
      double safeX = Mth.clamp(x, 0.12, 0.88);
      double safeY = Mth.clamp(y, 0.2, 0.8);
      double safeZ = Mth.clamp(z, 0.12, 0.88);
      return new Vec3(Math.fma(box.getXsize(), safeX, box.minX), Math.fma(box.getYsize(), safeY, box.minY), Math.fma(box.getZsize(), safeZ, box.minZ));
   }

   static Vec3 init_Vec3_AABB_Vec3(AABB box, Vec3 point) {
      return point == null
         ? init_Vec3_AABB_2(box, 0.5, 0.56, 0.5)
         : init_Vec3_AABB_2(box, load_D_D(point.x, box.minX, box.maxX), load_D_D(point.y, box.minY, box.maxY), load_D_D(point.z, box.minZ, box.maxZ));
   }

   private static double load_D_D(double value, double min, double max) {
      double size = max - min;
      return size > 1.0E-9 ? (value - min) / size : 0.5;
   }

   private static Vec3 init_Vec3_AABB_3(AABB box, Vec3 from, Vec3 to) {
      return (box.contains(from) ? box.clip(to, from) : box.clip(from, to)).orElse(null);
   }

   private static Vec3 init_Vec3_Vec3_4(Vec3 base, double scale, Vec3 other) {
      return new Vec3(Math.fma(scale, other.x, base.x), Math.fma(scale, other.y, base.y), Math.fma(scale, other.z, base.z));
   }

   private boolean init_Z_Vec3_Vec3_2(Vec3 eyes, Vec3 point) {
      return this.init_Z_Vec3_8(eyes, point, MC.player);
   }

   private boolean init_Z_Vec3_8(Vec3 eyes, Vec3 point, Entity entity) {
      return MC.level.clip(new ClipContext(eyes, point, Block.COLLIDER, Fluid.NONE, entity)).getType() == Type.MISS;
   }

   private EntityHitResult init_EntityHitResult_D(double range, ce.Inner_DataUtil rotation, Predicate<Entity> predicate) {
      Entity camera = MC.getCameraEntity();
      if (camera == null) {
         return null;
      } else {
         Vec3 eyes = camera.getEyePosition();
         Vec3 direction = Vec3.directionFromRotation(rotation.load(), rotation.init());
         AttackRange itemRange = MC.player.getMainHandItem().get(DataComponents.ATTACK_RANGE);
         if (itemRange != null) {
            Vec3 from = eyes.add(direction.scale(itemRange.effectiveMinRange(MC.player)));
            Vec3 to = eyes.add(direction.scale(autismclient.util.u.init(MC.player, direction)));
            AABB area = camera.getBoundingBox().expandTowards(to.subtract(eyes)).inflate(1.0);
            return ProjectileUtil.getManyEntityHitResult(
                  MC.level, camera, from, to, area, EntitySelector.CAN_BE_PICKED.and(predicate), itemRange.hitboxMargin(), Block.OUTLINE, true, true
               )
               .stream()
               .min(Comparator.comparingDouble(hit -> eyes.distanceToSqr(hit.getLocation())))
               .orElse(null);
         } else {
            Vec3 end = eyes.add(direction.x * range, direction.y * range, direction.z * range);
            AABB search = camera.getBoundingBox().expandTowards(direction.scale(range)).inflate(1.0, 1.0, 1.0);
            return ProjectileUtil.getEntityHitResult(camera, eyes, end, search, EntitySelector.CAN_BE_PICKED.and(predicate), range * range);
         }
      }
   }

   private EntityHitResult init_EntityHitResult_Entity(Entity target, ce.Inner_DataUtil rotation, double range, double wallsRange) {
      Entity camera = MC.getCameraEntity();
      if (camera == null) {
         return null;
      }

      EntityHitResult hit = this.init_EntityHitResult_D(range, rotation, entity -> entity == target);
      if (hit != null && hit.getEntity() == target) {
         Vec3 eyes = camera.getEyePosition();
         double distanceSq = eyes.distanceToSqr(hit.getLocation());
         if (MC.player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)) {
            range = Math.max(range, autismclient.util.u.init(MC.player, Vec3.directionFromRotation(rotation.load(), rotation.init())));
         }

         return distanceSq <= wallsRange * wallsRange || distanceSq <= range * range && this.init_Z_Vec3_8(eyes, hit.getLocation(), camera) ? hit : null;
      } else {
         return null;
      }
   }

   private void set_V_2() {
      LivingEntity target = this.init_LivingEntity;
      if (target == null) {
         this.init_aa_CachePool.load("noTarget");
      } else if (!this.init_Z_LivingEntity_3(target)) {
         this.init_aa_ConfigStore.read_V();
         this.init_aa_CachePool.write_V();
      } else if (aI.flush_Z_5()) {
         this.init_aa_CachePool.load("scaffold");
         this.init_aa_ConfigStore.read_V();
      } else if (this.push_Z_3() || !this.init_aa_ConfigStore.init_Z() || !this.read_Z_LivingEntity(target)) {
         ce.Inner_DataUtil rotation = this.init_ce_DataUtil_4();
         this.init_V_Entity_ce_DataUtil(target, rotation);
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_4() {
      ce.Inner_DataUtil planned = autismclient.util.aD.init_ce_DataUtil();
      if (planned == null) {
         planned = ce.init(MC.player);
      }

      return init_ce_DataUtil_ce_DataUtil_ch_CachePool(planned, ch.init_ch_CachePool());
   }

   static ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil_ch_CachePool(ce.Inner_DataUtil planned, ch.Inner_CachePool wire) {
      return wire.init_Z() ? new ce.Inner_DataUtil(wire.read(), wire.write()) : planned;
   }

   public static EntityHitResult init_EntityHitResult_EntityHitResult_ch_CachePool(EntityHitResult queued, ch.Inner_CachePool wire) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      aa aura = init_aa();
      if (aura != null
         && aura.clear_Z_2()
         && queued != null
         && queued.getEntity() == aura.init_LivingEntity
         && aura.init_LivingEntity != null
         && aura.init_LivingEntity.isAlive()) {
         ce.Inner_DataUtil look = init_ce_DataUtil_ce_DataUtil_ch_CachePool(ce.init(MC.player), wire);
         if (aura.init_LivingEntity instanceof Player player && !aura.init_Z_Player_2(player, look, wire.init_Vec3())) {
            return null;
         } else {
            EntityHitResult hit = aura.init_EntityHitResult_Entity(aura.init_LivingEntity, look, aura.write_D(), 0.0);
            return hit != null && aura.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), hit.getLocation()) ? hit : null;
         }
      } else {
         return null;
      }
   }

   private boolean read_Z_LivingEntity(LivingEntity target) {
      if (ck.init_ck().state_I() != this.pool && this.load_LivingEntity == null) {
         ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
         if (rotation == null) {
            rotation = ce.init(MC.player);
         }

         EntityHitResult hit = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
         if (hit != null && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), hit.getLocation())) {
            this.init_aa_ConfigStore.load_V();
            return false;
         }

         Vec3 eyes = MC.player.getEyePosition();
         double reach = this.write_D();
         if (this.init_Z_LivingEntity_Vec3(target, eyes)
            && init_Z_Vec3_6(eyes, target.getBoundingBox(), rotation)
            && load_Z_Vec3_3(eyes, target.getBoundingBox(), rotation)
            && !(target.getBoundingBox().distanceToSqr(eyes) > reach * reach)
            && this.init_Z_Entity_ItemStack(target, MC.player.getMainHandItem())) {
            if (!this.buffer()) {
               if (!this.init_aa_CachePool.init_Z()) {
                  return false;
               }

               if (!autismclient.util.s.init(this.id(), BlockHitResult.miss(eyes, Direction.DOWN, MC.player.blockPosition()), true)) {
                  this.init_aa_ConfigStore.load_V();
                  this.init_aa_CachePool.load("whiffQueue");
                  return false;
               }
            } else {
               Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
               autismclient.util.G.init_V();
            }

            this.init_aa_CachePool.init_V_Str("whiff");
            this.flush_V_7();
            return true;
         } else {
            this.init_aa_ConfigStore.load_V();
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean prev() {
      LivingEntity target = this.init_LivingEntity;
      if (target != null && ck.init_ck().state_I() != this.stop_I) {
         boolean shieldBreak = this.buffer() && this.sync(target);
         boolean lastChance = !shieldBreak && this.read_Z_Entity(target);
         if (!reset_Z_2() && (shieldBreak || lastChance || this.init_aa_CachePool.load_Z())) {
            ItemStack stack = MC.player.getMainHandItem();
            boolean previousLastChance = this.update_Z;

            try {
               this.update_Z = lastChance;
               if (!this.init_Z_Entity_ItemStack(target, stack)) {
                  return false;
               }
            } finally {
               this.update_Z = previousLastChance;
            }

            ce.Inner_DataUtil rotation = this.init_ce_DataUtil_4();
            EntityHitResult hit = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
            return hit != null
               && this.init_Z_ItemStack_Vec3(stack, hit.getLocation())
               && !(target instanceof Player player && !this.init_Z_Player_ce_DataUtil(player, rotation));
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void init_V_Entity_ce_DataUtil(Entity target, ce.Inner_DataUtil rotation) {
      EntityHitResult attackHit = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
      boolean isInRange = attackHit != null && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), attackHit.getLocation());
      if (!isInRange) {
         if (!this.buffer() && !this.push_Z_3()) {
            Vec3 eyes = MC.player.getEyePosition();
            double distance = Math.sqrt(target.getBoundingBox().distanceToSqr(eyes));
            boolean legalSwing = distance > this.write_D() || init_Z_Vec3_6(eyes, target.getBoundingBox(), rotation);
            if (!autismclient.util.t.init(target) && this.init_aa_CachePool.init_Z() && legalSwing && distance <= this.write_D() + this.init_D_Entity_2(target)
               )
             {
               boolean nearMiss = distance <= this.write_D();

               while (this.init_aa_CachePool.init_Z()) {
                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  autismclient.util.G.init_V();
                  this.init_aa_CachePool.init_V_Str(nearMiss ? "nearMiss" : "poke");
               }

               if (nearMiss) {
                  this.flush_V_7();
               }
            } else if (!legalSwing) {
               this.init_aa_CachePool.read("ring");
            } else {
               this.init_aa_CachePool.load("far");
            }
         }

         this.init_V_Entity(target);
      } else if (target instanceof Player player && !this.init_Z_Player_ce_DataUtil(player, rotation)) {
         this.init_aa_CachePool.read("trackedYaw");
      } else if (ck.init_ck().state_I() != this.stop_I && !this.write(target) && (this.update(target) || !this.valid())) {
         if (target instanceof Player && ci.read() && MC.player.getAttackStrengthScale(0.5F) > 0.9F) {
            this.init_aa_CachePool.write_V();
         } else {
            ItemStack mainHandStack = MC.player.getMainHandItem();
            boolean shieldBreakHit = target instanceof LivingEntity living && this.sync(living);
            this.update_Z = !shieldBreakHit && this.read_Z_Entity(target);
            boolean offSchedule = this.buffer() && (shieldBreakHit || this.update_Z);

            try {
               if ((this.init_aa_CachePool.init_Z() || offSchedule) && this.init_Z_Entity_ItemStack(target, mainHandStack)) {
                  this.init_aa_CachePool.read_Z = shieldBreakHit || this.update_Z;
                  this.init_V_Entity(target, attackHit, mainHandStack, shieldBreakHit);
                  return;
               }

               this.init_aa_CachePool.load("cannotAttack");
            } finally {
               this.update_Z = false;
            }

            return;
         }
      } else {
         this.init_aa_CachePool.write_V();
      }
   }

   public String init_Str_Z(boolean reset) {
      return this.init_aa_CachePool.init_Str_Z(reset);
   }

   private boolean read_Z_Entity(Entity target) {
      if (this.buffer() && !this.write(0) && !(MC.player.getAttackStrengthScale(0.5F) <= 0.9F)) {
         Vec3 motion = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
         return autismclient.util.u.init(
            MC.player.getEyePosition(), target.getBoundingBox(), l.init(MC.player.getDeltaMovement()), l.init(motion), this.write_D()
         );
      } else {
         return false;
      }
   }

   private boolean init_Z_Player_ce_DataUtil(Player target, ce.Inner_DataUtil look) {
      return this.init_Z_Player_2(target, look, ch.init_ch_CachePool().init_Vec3());
   }

   private boolean init_Z_Player_2(Player target, ce.Inner_DataUtil look, Vec3 sentPosition) {
      if (!this.init_cj.init_Z_LivingEntity(target)) {
         return true;
      }

      Vec3 eye = sentPosition == null ? MC.player.position() : sentPosition;
      Vec3 eyes = eye.add(0.0, MC.player.getEyeHeight(), 0.0);
      Vec3 sampled = TaskTimer.init_Vec3_LivingEntity_Vec3(target, eyes);
      if (sampled == null) {
         sampled = this.init_Vec3_4();
      }

      if (init_Z_Vec3_7(eye, sampled, look.init()) && init_Z_Vec3_7(eye, target.position(), look.init())) {
         AABB body = target.getBoundingBox();
         AABB received = body.move(sampled.subtract(target.position()));
         Vec3 bodyHit = init_Vec3_Vec3_6(eyes, look, this.write_D(), body);
         Vec3 receivedHit = init_Vec3_Vec3_6(eyes, look, this.write_D(), received);
         if (bodyHit != null && receivedHit != null && this.init_Z_Vec3_8(eyes, receivedHit, MC.player)) {
            ce.Inner_DataUtil planned = autismclient.util.aD.init_ce_DataUtil();
            if (planned == null) {
               return true;
            }

            Vec3 nowEyes = MC.player.getEyePosition();
            Vec3 motion = MC.player.getDeltaMovement();
            double rise = motion.y;
            if (MC.player.onGround()) {
               rise = Math.max(0.0, rise);
               if (MC.options.keyJump.isDown() && !MC.player.isInWater() && !MC.player.isInLava()) {
                  rise = Math.max(rise, ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower());
               }
            }

            Vec3 nextEyes = nowEyes.add(motion.x, rise, motion.z);
            return init_Vec3_Vec3_6(nowEyes, planned, this.write_D(), body) != null
               && init_Vec3_Vec3_6(nowEyes, planned, this.write_D(), received) != null
               && init_Vec3_Vec3_6(nextEyes, planned, this.write_D(), body) != null
               && init_Vec3_Vec3_6(nextEyes, planned, this.write_D(), received) != null
               && init_Z_Vec3_7(nextEyes, sampled, planned.init());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static Vec3 init_Vec3_Vec3_6(Vec3 eyes, ce.Inner_DataUtil look, double reach, AABB box) {
      AABB inset = box.deflate(0.003);
      if (inset.contains(eyes)) {
         return eyes;
      }

      Vec3 end = eyes.add(Vec3.directionFromRotation(look.load(), look.init()).scale(Math.max(0.0, reach)));
      return inset.clip(eyes, end).orElse(null);
   }

   private Vec3 init_Vec3_4() {
      int latency = 0;
      if (MC.getConnection() != null && MC.player != null) {
         PlayerInfo info = MC.getConnection().getPlayerInfo(MC.player.getUUID());
         if (info != null) {
            latency = Math.max(0, info.getLatency());
         }
      }

      int age = 2 + Math.min(10, latency / 100);
      return this.init_cj.init(ck.init_ck().state_I(), age);
   }

   static boolean init_Z_Vec3_7(Vec3 eye, Vec3 target, float yaw) {
      double dx = target.x - eye.x;
      double dz = target.z - eye.z;
      if (dx * dx + dz * dz < 1.0E-8) {
         return true;
      }

      double bearing = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
      return Math.abs(Mth.wrapDegrees(bearing - yaw)) <= Math.toDegrees(0.3);
   }

   private void init_V_Entity(Entity target, EntityHitResult attackHit, ItemStack mainHandStack, boolean shieldBreakHit) {
      this.init_V_BooleanSupplier(() -> !this.init_Z_Entity_ItemStack(target, mainHandStack) ? false : this.init_Z_Entity_EntityHitResult(target, attackHit));
   }

   private ce.Inner_DataUtil init_ce_DataUtil_LivingEntity_ce_DataUtil(LivingEntity target, ce.Inner_DataUtil rotation) {
      if (this.push_Z_3() || !this.init_Z_LivingEntity_3(target)) {
         this.init_aa_ConfigStore.read_V();
         return rotation;
      }

      if (autismclient.util.t.init(target)) {
         this.init_aa_ConfigStore.read_V();
         return rotation;
      }

      if (this.init_aa_ConfigStore.init_Z()) {
         if (this.integer("miss-chance") <= 0 || this.init_aa_ConfigStore.read_Z() == this.buffer()) {
            this.init_aa_ConfigStore.read_V();
            return rotation;
         } else if (!this.init_aa_ConfigStore.load(target.getId())) {
            this.init_aa_ConfigStore.read_V();
            return rotation;
         } else if (this.init_aa_ConfigStore.init(ck.init_ck().state_I())) {
            this.init_aa_ConfigStore.load_V();
            return rotation;
         } else {
            this.init_aa_ConfigStore.init(target.getBoundingBox().getCenter());
            return "Human".equals(this.choice("aim")) ? rotation : ce.init(this.init_aa_ConfigStore.init_Vec3(), MC.player.getEyePosition());
         }
      } else {
         if (!this.init_aa_ConfigStore.load_Z() || this.integer("miss-chance") <= 0 || this.drain >= 12 || !this.init_aa_CachePool.load_Z()) {
            return rotation;
         }

         if (!this.init_Z_Entity_ItemStack(target, MC.player.getMainHandItem())) {
            return rotation;
         }

         if (this.write(target)) {
            return rotation;
         }

         if (!this.sync(target) && !this.read_Z_Entity(target)) {
            EntityHitResult reachable = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
            if (reachable != null && this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), reachable.getLocation())) {
               if (this.load_Random.nextDouble() >= this.integer("miss-chance") * 0.01) {
                  return rotation;
               }

               this.init_aa_ConfigStore
                  .init(
                     target.getId(),
                     init_Vec3_AABB_2(target.getBoundingBox(), MC.player.getEyePosition(), this.load_Random),
                     !this.buffer(),
                     ck.init_ck().state_I()
                  );
               this.init_aa_ConfigStore.init(target.getBoundingBox().getCenter());
               return "Human".equals(this.choice("aim")) ? rotation : ce.init(this.init_aa_ConfigStore.init_Vec3(), MC.player.getEyePosition());
            } else {
               return rotation;
            }
         } else {
            return rotation;
         }
      }
   }

   private void put_V_2() {
      LivingEntity target = this.init_LivingEntity;
      double reach = this.write_D();
      boolean ready = target != null
         && this.get_I > 0
         && target.getBoundingBox().distanceToSqr(MC.player.getEyePosition()) <= reach * reach
         && (this.buffer() ? this.write(0) : this.get_I > 6);
      this.drain = ready ? Math.min(this.drain + 1, 1000) : 0;
      if (this.drain >= 12 && MC.gui.screen() == null && reset_Z_2()) {
         MC.missTime = 0;
      }
   }

   private boolean write(LivingEntity target) {
      return target.getHealth() <= MC.player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5;
   }

   static Vec3 init_Vec3_AABB_2(AABB box, Vec3 eyes, Random random) {
      Vec3 center = box.getCenter();
      double halfWidth = Math.min(box.getXsize(), box.getZsize()) * 0.5;
      double lateral = halfWidth + 0.1 + random.nextDouble() * 0.1;
      if (random.nextBoolean()) {
         lateral = -lateral;
      }

      double depth = (random.nextDouble() * 2.0 - 1.0) * 0.1;
      double dy = (random.nextDouble() * 2.0 - 1.0) * 0.02;
      if (dy == 0.0) {
         dy = 0.01;
      }

      double dx = center.x - eyes.x;
      double dz = center.z - eyes.z;
      double horizontal = Math.max(Math.sqrt(dx * dx + dz * dz), 0.3);
      double dirX = dx / horizontal;
      double dirZ = dz / horizontal;
      return new Vec3(center.x + dirX * depth - dirZ * lateral, center.y + dy, center.z + dirZ * depth + dirX * lateral);
   }

   private boolean write(Entity target) {
      if (target instanceof LivingEntity living) {
         boolean shieldBreak = this.update(living);
         if (!this.put_Z_2() && !shieldBreak) {
            return false;
         }

         Integer slot = this.init_Integer_LivingEntity_Z(living, shieldBreak);
         if (slot != null && !this.task()) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (selected == slot) {
               return false;
            }

            if (this.add_Z_2()) {
               if (this.clear_I < 0) {
                  this.clear_I = selected;
               }

               this.start_I = 20;
            }

            int tick = ck.init_ck().state_I();
            if (tick == this.stop_I) {
               return true;
            }

            if (!autismclient.util.ah.write_Z(this.id())) {
               return true;
            }

            try {
               autismclient.util.aw.init_V(MC, slot);
               this.reset_I = slot;
               this.stop_I = tick;
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void init_V_Entity(Entity target) {
      if (!this.write(target)) {
         int selected = MC.player.getInventory().getSelectedSlot();
         if (this.add_Z_2() && this.clear_I >= 0 && this.reset_I == selected) {
            this.start_I = 20;
         }
      }
   }

   private boolean update(Entity target) {
      if (this.add_Z_2() && this.clear_I >= 0 && this.reset_I == MC.player.getInventory().getSelectedSlot()) {
         boolean engaged = this.put_Z_2() || this.start_I > 0 && target instanceof LivingEntity living && this.update(living);
         if (!engaged) {
            return false;
         }

         this.start_I = 20;
         return true;
      } else {
         return false;
      }
   }

   private boolean update(LivingEntity target) {
      if (!this.bool("shield-break")) {
         return false;
      } else {
         int tick = ck.init_ck().state_I();
         if (this.close(target)) {
            this.next = tick;
            this.prev = target.getId();
            return true;
         } else {
            return target instanceof Player && target.getId() == this.size && tick - this.valid <= 100
               ? false
               : target.getId() == this.prev && tick - this.next <= 10;
         }
      }
   }

   private boolean sync(LivingEntity target) {
      return this.bool("shield-break") && this.close(target);
   }

   private boolean valid() {
      if (this.add_Z_2() && this.clear_I >= 0 && this.start_I <= 0) {
         if (this.reset_I >= 0 && MC.player.getInventory().getSelectedSlot() != this.reset_I) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!t.init_Z_I_10(selected) && !BufferHelper.init_Z_I(selected)) {
               this.clear_I = -1;
               this.reset_I = -1;
               return false;
            } else {
               return false;
            }
         } else {
            int back = this.clear_I;
            if (MC.player.getInventory().getSelectedSlot() == back) {
               this.clear_I = -1;
               this.reset_I = -1;
               return false;
            }

            int tick = ck.init_ck().state_I();
            if (tick == this.stop_I) {
               return false;
            }

            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            try {
               autismclient.util.aw.init_V(MC, back);
               this.stop_I = tick;
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            this.clear_I = -1;
            this.reset_I = -1;
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_Entity_ItemStack(Entity target, ItemStack stack) {
      if (target instanceof LivingEntity living && !this.init_Z_LivingEntity_3(living)) {
         return false;
      } else if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      } else if (MC.player.cannotAttackWithItem(stack, 0)) {
         return false;
      } else if (aI.flush_Z_5()) {
         return false;
      } else if (this.data > 0) {
         return false;
      } else if (this.add_I == 0) {
         return false;
      } else {
         return this.update_Z
            ? true
            : !(this.bool("criticals") && target instanceof LivingEntity living) || MC.player.isFallFlying() || this.sync(living) || !this.flush(living);
      }
   }

   private boolean flush(LivingEntity target) {
      if (!this.size()) {
         this.cache = -1;
         this.state = -2147483648;
         return false;
      }

      int tick = ck.init_ck().state_I();
      if (this.cache != target.getId()) {
         this.cache = target.getId();
         this.state = tick;
      }

      return tick - this.state < 4;
   }

   private boolean size() {
      double motionY = MC.player.getDeltaMovement().y;
      if (this.state() && !(motionY < -0.08)) {
         float ticksTillCrit = Math.max(this.init_F_5(), (float)(motionY / 0.08));
         float damageOnCrit = 0.375F;
         return damageOnCrit <= this.init_F_F_4(ticksTillCrit) ? false : this.read_Z_I_2((int)(ticksTillCrit * 1.3F));
      } else {
         return false;
      }
   }

   private float init_F_5() {
      return Math.max(this.load_F_3() * 0.9F - 0.5F - this.update_I_2(), 0.0F);
   }

   private float init_F_F_4(float ticks) {
      float base = (ticks + 0.5F) / this.load_F_3();
      return Math.min(0.2F + base * base * 0.8F, 1.0F);
   }

   private boolean read_Z_I_2(int ticks) {
      double motionY = MC.player.getDeltaMovement().y;
      AABB box = MC.player.getBoundingBox();

      for (int i = 0; i < ticks; i++) {
         motionY = (motionY - 0.08) * 0.98;
         box = box.move(0.0, motionY, 0.0);
         if (!MC.level.noCollision(MC.player, box)) {
            return false;
         }
      }

      return true;
   }

   private boolean data() {
      return this.bool("criticals")
         && MC.player != null
         && !MC.player.onGround()
         && this.init_LivingEntity != null
         && this.init_aa_CachePool.init_Z_I(1)
         && !(this.init_LivingEntity instanceof LivingEntity living && this.sync(living));
   }

   public static boolean close_Z_6() {
      aa aura = init_aa();
      return aura != null && aura.data();
   }

   public static boolean load_Z_I_3(int slot) {
      return al.init_Module_Str("kill-aura") instanceof aa aura && aura.isEnabled() ? aura.clear_I >= 0 && aura.reset_I == slot : false;
   }

   private void init_V_BooleanSupplier(BooleanSupplier attack) {
      if (this.init_aa_CachePool.load_Z()) {
         if (this.push_Z_3()) {
            this.load_V_Z_2(false);
            this.init_aa_CachePool.write_V();
            this.init_Str = "guard_release";
         } else if (ck.init_ck().state_I() == this.pool) {
            this.init_aa_CachePool.write_V();
            this.init_Str = "guard_release";
         } else if (!MC.player.isBlocking() && !MC.player.isUsingItem()) {
            this.init_aa_CachePool.init_V_BooleanSupplier(attack);
         }
      }
   }

   private boolean init_Z_Entity_EntityHitResult(Entity target, EntityHitResult hit) {
      return this.init_Z_Entity(target, hit, false);
   }

   private boolean init_Z_Entity(Entity target, EntityHitResult hit, boolean followUp) {
      if (AutismLiteVariant.enabled()) {
         return false;
      } else {
         ce.Inner_DataUtil look = this.init_ce_DataUtil_4();
         if (target instanceof Player player && !this.init_Z_Player_ce_DataUtil(player, look)) {
            return false;
         } else {
            ItemStack stack = MC.player.getMainHandItem();
            double damage = this.read_D();
            boolean brokeShield = target instanceof LivingEntity living && this.sync(living) && stack.is(ItemTags.AXES);
            boolean lastChance = this.update_Z;
            Runnable dispatched = () -> this.init(target, damage, followUp, brokeShield, lastChance);
            PiercingWeapon piercing = stack.get(DataComponents.PIERCING_WEAPON);
            if (piercing != null && followUp) {
               return false;
            }

            if (piercing != null && !MC.gameMode.isSpectator()) {
               Compat263.piercingAttack(MC.gameMode, piercing);
               autismclient.util.t.init(MC.player, target);
               Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
               autismclient.util.G.init_V();
               dispatched.run();
               return true;
            }

            if (!this.sync(target)) {
               return false;
            }

            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            try {
               ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$ensureHasSentCarriedItem();
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            if ("Packet".equals(this.choice("attack-mode"))) {
               if (MC.gameMode.isSpectator()) {
                  MC.getConnection().send(new ServerboundAttackPacket(target.getId()));
                  autismclient.util.t.init(MC.player, target);
               } else {
                  autismclient.util.r.init(target, () -> MC.gameMode.attack(MC.player, target), dispatched);
               }

               Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
               autismclient.util.G.init_V();
               if (MC.gameMode.isSpectator()) {
                  dispatched.run();
               }

               return true;
            } else {
               if (!autismclient.util.r.init(this.id(), hit, dispatched)) {
                  return false;
               }

               this.read_Z = true;
               return true;
            }
         }
      }
   }

   private void init(Entity target, double damage, boolean followUp, boolean brokeShield, boolean lastChance) {
      if (followUp) {
         this.init_aa_DataUtil.init_V();
         this.get_I = 0;
         this.flush_F = -1.0F / 0.0F;
      } else {
         this.init_V_Entity_D(target, damage);
         this.drain = 0;
         if (brokeShield) {
            this.valid = ck.init_ck().state_I();
            this.size = target.getId();
         }

         this.init_aa_DataUtil.init_V();
         this.get_I = 0;
         this.cache = -1;
         this.state = -2147483648;
         if (this.init_q != null) {
            this.init_q.load_V();
         }

         this.init_aa_ConfigStore.init_V();
         if (this.buffer()) {
            this.init_V_Entity_Z(target, lastChance);
         }

         if (this.add_Z_2() && this.clear_I >= 0) {
            this.start_I = brokeShield ? 0 : 20;
         }

         this.add_D = this.sync_D();
         this.reset_V_2();
         read_J = System.currentTimeMillis();
         this.init_aa_CachePool.read_I = 0;
      }
   }

   public static void read_V_14() {
      if (!AutismLiteVariant.enabled()) {
         aa aura = init_aa();
         if (aura != null && aura.read_Z && MC.player != null) {
            aura.read_Z = false;
            aura.close_V_6();
         }
      }
   }

   private boolean sync(Entity target) {
      return target != null
         && target != MC.player
         && !(target instanceof ItemEntity)
         && !(target instanceof ExperienceOrb)
         && (!(target instanceof AbstractArrow) || target.isAttackable());
   }

   private void init_V_Entity_D(Entity target, double damage) {
      if (target instanceof LivingEntity living) {
         this.init_l_BufferHelper.init_V(living.getUUID(), living.hurtTime, ck.init_ck().state_I(), damage);
         this.queue = living.getId();
         this.buffer = living.hurtTime;
         this.entry = 8;
      }
   }

   private void add_V_2() {
      if (this.queue >= 0) {
         Entity entity = MC.level.getEntity(this.queue);
         boolean landed = entity instanceof LivingEntity living && (living.hurtTime > this.buffer || this.buffer >= 10 && living.hurtTime >= 10);
         if (landed) {
            if (entity instanceof LivingEntity living && living.hurtTime > this.buffer) {
               this.init_l_BufferHelper.init_V_UUID_I(living.getUUID(), ck.init_ck().state_I());
            }

            if (this.bool("hitsound") || this.bool("hit-marker")) {
               this.load_V_Entity(entity);
               this.clear_V_2();
            }
         }

         if (landed || entity == null || --this.entry <= 0) {
            this.queue = -1;
         }
      }
   }

   private void load_V_Entity(Entity target) {
      if (this.bool("hit-marker")) {
         autismclient.util.aC.init(target.getBoundingBox().inflate(target.getPickRadius()));
      }
   }

   private void clear_V_2() {
      if (this.bool("hitsound")) {
         MC.getSoundManager().play(SimpleSoundInstance.forUI(init_SoundEvent, 1.0F, 0.7F));
      }
   }

   private boolean value() {
      return this.cache() && MC.player.fallDistance > 0.0;
   }

   private boolean cache() {
      return this.state() && (!this.buffer() || MC.player.getAttackStrengthScale(0.5F) > 0.9F);
   }

   private boolean state() {
      return this.load_Z_Z_2(true);
   }

   private boolean load_Z_Z_2(boolean requireAir) {
      Module flight = al.init_Module_Str("flight");
      boolean flyRunning = flight != null && flight.isEnabled();
      return !flyRunning
         && !MC.player.isInLiquid()
         && !MC.player.isPassenger()
         && !this.queue()
         && !MC.player.hasEffect(MobEffects.LEVITATION)
         && !MC.player.hasEffect(MobEffects.BLINDNESS)
         && !MC.player.hasEffect(MobEffects.SLOW_FALLING)
         && !MC.player.onClimbable()
         && !MC.player.isNoGravity()
         && !MC.player.isHandsBusy()
         && !MC.player.getAbilities().flying
         && (!requireAir || !MC.player.onGround());
   }

   private boolean queue() {
      return MC.level.getBlockStates(MC.player.getBoundingBox()).anyMatch(state -> state.getBlock() instanceof WebBlock);
   }

   private double write_D() {
      return autismclient.util.u.init_D(MC.player);
   }

   private double update_D() {
      return Math.max(this.write_D(), 0.0) + this.add_D;
   }

   private boolean init_Z_ItemStack_Vec3(ItemStack stack, Vec3 pos) {
      if (stack.has(DataComponents.PIERCING_WEAPON)) {
         return true;
      }

      AttackRange attackRange = stack.get(DataComponents.ATTACK_RANGE);
      if (attackRange == null) {
         attackRange = AttackRange.defaultFor(MC.player);
      }

      return attackRange.isInRange(MC.player, pos);
   }

   private double sync_D() {
      return 2.0 + this.init_Random.nextDouble() * 1.0;
   }

   private boolean buffer() {
      return !autismclient.util.aI.init(MC.player);
   }

   private float load_F_3() {
      double attackSpeed = MC.player.getAttributeValue(Attributes.ATTACK_SPEED);
      if (this.put_Z_2() && this.buffer() && this.reset_I < 0) {
         Integer slot = this.init_Integer_LivingEntity_Z(null, false);
         if (slot != null) {
            attackSpeed = this.init_D_ItemStack(
               MC.player.getInventory().getItem(slot), Attributes.ATTACK_SPEED, MC.player.getAttributeBaseValue(Attributes.ATTACK_SPEED)
            );
         }
      }

      return (float)(1.0 / attackSpeed * 20.0);
   }

   private int update_I_2() {
      return ((AutismLivingEntityAccessor)MC.player).autism$getAttackStrengthTicker();
   }

   private boolean write(int ticks) {
      if (!this.buffer()) {
         return true;
      }

      float delay = this.load_F_3();
      float offset = this.init_F_F_Entity(delay, this.init_LivingEntity);
      return (this.update_I_2() + ticks + 0.5F) / delay >= this.sync_F + offset / delay - 1.0E-4F;
   }

   private float init_F_F_Entity(float delay, Entity target) {
      return autismclient.util.t.init(target) ? 0.0F : Math.max(this.flush_F, -0.08F * delay);
   }

   private void reset_V_2() {
      this.sync_F = 1.0F;
      this.flush_F = this.read_F_2();
   }

   private float read_F_2() {
      ValueRange offset = ValueRange.parse(this.value("swing-offset"), init_ValueRange);
      return (float)(this.init_JsonUtil.init(offset.min(), offset.max(), 0.0) / 50.0);
   }

   private boolean close(LivingEntity target) {
      DamageSource source = target.level().damageSources().playerAttack(MC.player);
      return this.init_F_LivingEntity(target, source, 1.0F) > 0.0F;
   }

   private float init_F_LivingEntity(LivingEntity target, DamageSource source, float amount) {
      if (amount <= 0.0F) {
         return 0.0F;
      } else {
         ItemStack blockingStack = target.getItemBlockingWith();
         if (blockingStack == null) {
            return 0.0F;
         } else {
            BlocksAttacks blocksAttacks = blockingStack.get(DataComponents.BLOCKS_ATTACKS);
            if (blocksAttacks == null) {
               return 0.0F;
            } else if (blocksAttacks.bypassedBy().map(tag -> tag.contains(source.typeHolder())).orElse(false)) {
               return 0.0F;
            } else if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
               return 0.0F;
            } else {
               double horizontalAngle = 3.141592653589793;
               Vec3 sourcePosition = source.getSourcePosition();
               if (sourcePosition != null) {
                  Vec3 view = LivingEntity.calculateViewVector(0.0F, target.getYHeadRot());
                  Vec3 to = sourcePosition.subtract(target.position());
                  Vec3 sourceDirection = new Vec3(to.x, 0.0, to.z).normalize();
                  horizontalAngle = Math.acos(sourceDirection.dot(view));
               }

               return blocksAttacks.resolveBlockedDamage(source, amount, horizontalAngle);
            }
         }
      }
   }

   static void init_V_IArr(int[] cycle, Random random, int cpsMin, int cpsMax) {
      if (cycle.length != 0) {
         int clicks = Math.min(cycle.length, cpsMin + random.nextInt(cpsMax - cpsMin + 1));
         int[] gaps = new int[clicks];
         int baseGap = cycle.length / clicks;
         int remainder = cycle.length % clicks;
         Arrays.fill(gaps, baseGap);

         for (int i = 0; i < remainder; i++) {
            gaps[i]++;
         }

         init_V_IArr_Random(gaps, random);
         boolean allEqual = true;

         for (int i = 1; i < gaps.length; i++) {
            if (gaps[i] != gaps[0]) {
               allEqual = false;
               break;
            }
         }

         if (allEqual && gaps.length > 1 && gaps[0] > 1) {
            int donor = random.nextInt(gaps.length);
            int receiver = (donor + 1 + random.nextInt(gaps.length - 1)) % gaps.length;
            gaps[donor]--;
            gaps[receiver]++;
         }

         int transfers = random.nextInt(gaps.length + 1) + random.nextInt(gaps.length + 1);

         for (int i = 0; i < transfers; i++) {
            int donor = random.nextInt(gaps.length);
            int receiver = random.nextInt(gaps.length);
            if (donor != receiver && gaps[donor] > 1) {
               gaps[donor]--;
               gaps[receiver]++;
            }
         }

         init_V_IArr_Random(gaps, random);
         int position = random.nextInt(cycle.length);

         for (int gap : gaps) {
            cycle[position]++;
            position = (position + gap) % cycle.length;
         }
      }
   }

   private static void init_V_IArr_Random(int[] values, Random random) {
      for (int i = values.length - 1; i > 0; i--) {
         int other = random.nextInt(i + 1);
         int value = values[i];
         values[i] = values[other];
         values[other] = value;
      }
   }

   private Integer init_Integer_LivingEntity_Z(LivingEntity target, boolean enforceShield) {
      boolean requiresShield = enforceShield || target != null && this.close(target);
      boolean requiresMace = this.node() && (this.entry() || !requiresShield);
      Integer bestSlot = null;
      ItemStack bestStack = null;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.ah.init(slot, this.id())) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               boolean eligible = requiresMace ? stack.getItem() instanceof MaceItem : (requiresShield ? stack.is(ItemTags.AXES) : stack.is(ItemTags.SWORDS));
               if (eligible
                  && (bestStack == null || (requiresMace ? this.load(stack, bestStack) > 0 : this.init_I_ItemStack_ItemStack_2(stack, bestStack) > 0))) {
                  bestSlot = slot;
                  bestStack = stack;
               }
            }
         }
      }

      return bestSlot;
   }

   private boolean entry() {
      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.ah.init(slot, this.id()) && MC.player.getInventory().getItem(slot).getItem() instanceof MaceItem) {
            return true;
         }
      }

      return false;
   }

   private boolean node() {
      return MaceItem.canSmashAttack(MC.player);
   }

   private boolean task() {
      return MC.player.isUsingItem() && MC.player.getUsedItemHand() == InteractionHand.MAIN_HAND && MC.player.getUseItem().has(DataComponents.CONSUMABLE);
   }

   private void start_V_2() {
      if (this.add_Z_2() && this.clear_I >= 0 && this.start_I > 0) {
         if (this.reset_I >= 0 && MC.player.getInventory().getSelectedSlot() != this.reset_I) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!t.init_Z_I_10(selected) && !BufferHelper.init_Z_I(selected)) {
               this.clear_I = -1;
               this.reset_I = -1;
               this.start_I = 0;
            }
         } else {
            this.start_I--;
            if (this.start_I == 0
               && (!this.clear_Z_2() || this.open(this.init_LivingEntity))
               && !HashUtil.load_Z()
               && (AutismLiteVariant.enabled() || !UtilCore.init_Z_23())
               && !t.init_Z_51()
               && !t.sync_Z_11()
               && !BufferHelper.init_Z_2()
               && !autismclient.util.ah.init(this.clear_I, this.id())) {
               int tick = ck.init_ck().state_I();
               if (tick != this.stop_I && autismclient.util.ah.write_Z(this.id())) {
                  try {
                     int back = this.clear_I;
                     this.clear_I = -1;
                     this.reset_I = -1;
                     autismclient.util.aw.init_V(MC, back);
                     this.stop_I = tick;
                  } finally {
                     autismclient.util.ah.read_V(this.id());
                  }
               }
            }
         }
      }
   }

   private boolean open(LivingEntity target) {
      if (target == null) {
         return true;
      }

      ce.Inner_DataUtil rotation = autismclient.util.aD.init_ce_DataUtil();
      if (rotation == null) {
         rotation = ce.init(MC.player);
      }

      EntityHitResult hit = this.init_EntityHitResult_Entity(target, rotation, this.write_D(), 0.0);
      return hit == null || !this.init_Z_ItemStack_Vec3(MC.player.getMainHandItem(), hit.getLocation());
   }

   private int init_I_ItemStack_ItemStack_2(ItemStack first, ItemStack second) {
      int result = Double.compare(this.init_D_ItemStack_2(first), this.init_D_ItemStack_2(second));
      if (result != 0) {
         return result;
      }

      result = Double.compare(this.read_D_ItemStack(first), this.read_D_ItemStack(second));
      if (result != 0) {
         return result;
      }

      result = Boolean.compare(first.is(ItemTags.SWORDS), second.is(ItemTags.SWORDS));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(load_I_ItemStack(first), load_I_ItemStack(second));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(read_I_ItemStack(first), read_I_ItemStack(second));
      return result != 0 ? result : Integer.compare(first.hashCode(), second.hashCode());
   }

   private int load(ItemStack first, ItemStack second) {
      int result = Double.compare(this.load_D_ItemStack(first), this.load_D_ItemStack(second));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(load_I_ItemStack(first), load_I_ItemStack(second));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(read_I_ItemStack(first), read_I_ItemStack(second));
      return result != 0 ? result : Integer.compare(first.hashCode(), second.hashCode());
   }

   private double init_D_ItemStack_2(ItemStack stack) {
      double damage = this.init_D_ItemStack(stack, Attributes.ATTACK_DAMAGE, MC.player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
      int sharpness = this.init_I_ItemStack_ResourceKey(stack, Enchantments.SHARPNESS);
      if (sharpness > 0) {
         damage += 0.5 * sharpness + 0.5;
      }

      double speed = this.init_D_ItemStack(stack, Attributes.ATTACK_SPEED, MC.player.getAttributeBaseValue(Attributes.ATTACK_SPEED));
      double probability = Math.pow(0.85, 0.05);
      double adjusted = Math.pow(probability, Math.ceil(20.0 / speed * 0.9));
      double fire = Math.max(0.0, this.init_I_ItemStack_ResourceKey(stack, Enchantments.FIRE_ASPECT) * 4.0 - 1.0) * 0.33;
      double factor = this.init_I_ItemStack_ResourceKey(stack, Enchantments.SMITE) * 0.2
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.BANE_OF_ARTHROPODS) * 0.2
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.KNOCKBACK) * 0.2;
      return damage * speed * adjusted * (1.0 + factor) + fire;
   }

   private double load_D_ItemStack(ItemStack stack) {
      double damage = this.init_D_ItemStack(stack, Attributes.ATTACK_DAMAGE, MC.player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
      int sharpness = this.init_I_ItemStack_ResourceKey(stack, Enchantments.SHARPNESS);
      if (sharpness > 0) {
         damage += 0.5 * sharpness + 0.5;
      }

      double speed = this.init_D_ItemStack(stack, Attributes.ATTACK_SPEED, MC.player.getAttributeBaseValue(Attributes.ATTACK_SPEED));
      double probability = Math.pow(0.85, 0.05);
      double adjusted = Math.pow(probability, Math.ceil(20.0 / speed * 0.9));
      double factor = this.init_I_ItemStack_ResourceKey(stack, Enchantments.DENSITY) * 0.5
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.BREACH) * 0.15
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.SMITE) * 0.2
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.BANE_OF_ARTHROPODS) * 0.2
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.WIND_BURST) * 0.2;
      return damage * speed * adjusted + factor + 29.0;
   }

   private double read_D_ItemStack(ItemStack stack) {
      return this.init_I_ItemStack_ResourceKey(stack, Enchantments.LOOTING) * 0.05
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.UNBREAKING) * 0.05
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.MENDING) * 0.1
         - this.init_I_ItemStack_ResourceKey(stack, Enchantments.VANISHING_CURSE) * 0.1
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.SWEEPING_EDGE) * 0.2
         + this.init_I_ItemStack_ResourceKey(stack, Enchantments.KNOCKBACK) * 0.25;
   }

   private static int load_I_ItemStack(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   private static int read_I_ItemStack(ItemStack stack) {
      return stack.has(DataComponents.ENCHANTABLE) ? stack.get(DataComponents.ENCHANTABLE).value() : 0;
   }

   private double init_D_ItemStack(ItemStack stack, Holder<Attribute> attribute, double base) {
      ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
      return modifiers == null
         ? attribute.value().sanitizeValue(base)
         : attribute.value().sanitizeValue(modifiers.compute(attribute, base, EquipmentSlot.MAINHAND));
   }

   private int init_I_ItemStack_ResourceKey(ItemStack stack, ResourceKey<Enchantment> enchantment) {
      try {
         Holder<Enchantment> holder = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
         return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
      } catch (Throwable ignored) {
         return 0;
      }
   }

   private void stop_V_2() {
      this.read_LivingEntity = null;
      this.flags = this.level = -2147483648;
      if (!AutismLiteVariant.enabled()) {
         autismclient.util.ListPool.init_V();
      }

      this.load_V_Z_2(this.poll_Z_4());
      this.open_V_4();
      this.count = 0;
      this.load_InteractionHand = null;
      this.load_LivingEntity = null;
      this.init_l_BufferHelper.load();
      this.table = -2147483648;
      this.store = 2147483647;
      this.init_LivingEntity = null;
      this.read_Z = false;
      this.clear_I = -1;
      this.reset_I = -1;
      this.start_I = 0;
      this.next = -2147483648;
      this.prev = -1;
      this.valid = -2147483648;
      this.size = -1;
      this.data = 0;
      this.cache = -1;
      this.state = -2147483648;
      this.drain = 0;
      this.queue = -1;
      this.init_aa_StreamUtil.init_V();
      this.init_aa_ConfigStore.read_V();
      this.init_Vec3 = null;
      this.pop_I = -1;
      this.tick_I = -1;
      this.run_D = 0.0 / 0.0;
      this.run_I = -1;
      this.init_cj.init_V();
      this.init_Z = false;
      this.get_I = 1000;
      this.add_I = 1000;
      this.init_aa_CachePool.read_V();
      if (this.init_q != null) {
         this.init_q.init_V();
      }

      autismclient.util.aC.load();
   }

   static final class Inner_BufferHelper {
      private static final long init = 1000000L;
      private static final long load = 150000000L;
      private long read = -9223372036854775808L;

      void init(long now) {
         this.read = now;
      }

      void init() {
         this.read = -9223372036854775808L;
      }

      void init(
         autismclient.util.a.DataUtil engine,
         long now,
         boolean active,
         autismclient.util.a.CachePool profile,
         autismclient.util.a.DataUtil.Inner_NetHelper sink
      ) {
         if (active && this.read != -9223372036854775808L && now >= this.read && now - this.read <= 150000000L) {
            while (this.read < now) {
               this.read = Math.min(now, this.read + 1000000L);
               engine.init(this.read, true, profile, sink);
            }
         } else {
            this.read = now;
            engine.init(now, active, profile, sink);
         }
      }
   }

   private final class Inner_CachePool {
      private final aa.Inner_NetHelper init_aa_NetHelper = new aa.Inner_NetHelper(20, 2);
      private final autismclient.util.a.DataUtil init_DataUtil = new autismclient.util.a.DataUtil();
      private final aa.Inner_TaskQueue init_aa_TaskQueue = new aa.Inner_TaskQueue();
      private final aa.Inner_BufferHelper init_aa_BufferHelper = new aa.Inner_BufferHelper();
      private boolean init_Z;
      private int init_I;
      private int load_I;
      private boolean load_Z;
      private int read_I;
      private boolean read_Z;
      private final Map<String, Integer> init_Map = new TreeMap<>();
      private String init_Str = "";

      Inner_CachePool() {
         this.init_DataUtil.init_V_D(2.0);
         this.sync();
      }

      void init_V() {
         this.read_Z = false;
         this.read_I++;
         this.update();
         boolean stale = this.init_aa_TaskQueue.load_Z(System.nanoTime());
         this.load_Z = this.init_aa_TaskQueue.init_Z;
         int taken = this.init_aa_TaskQueue.init_I(System.nanoTime());
         this.init_I = aa.this.buffer() ? 0 : taken;
         if (stale && !aa.this.buffer()) {
            this.write("lost:stale");
         }

         if (aa.this.buffer() && this.init_aa_NetHelper.init_Z(1)) {
            int[] cycle = new int[20];
            aa.init_V_IArr(cycle, aa.this.init_Random, this.read_I(), this.write_I());
            this.init_aa_NetHelper.init(cycle);
         }
      }

      void load_V() {
         this.read_Z = false;
         this.update();
         if (this.init_aa_TaskQueue.load_I > 0 && !aa.this.buffer()) {
            this.write("lost:paused");
         }

         this.init_I = 0;
         this.init_aa_TaskQueue.init();
         this.init_Z = false;
         if (aa.this.buffer() && this.init_aa_NetHelper.init_Z(1)) {
            int[] cycle = new int[20];
            aa.init_V_IArr(cycle, aa.this.init_Random, this.read_I(), this.write_I());
            this.init_aa_NetHelper.init(cycle);
         }
      }

      void init_V_J(long now) {
         if (aa.this.init_h != null) {
            this.init_DataUtil.init_V_DataUtil_ConfigSto(aa.this.init_h.init_DataUtil_ConfigSto());
            autismclient.util.a.CachePool profile = aa.this.init_h.init_CachePool();
            this.init_aa_TaskQueue.init_I = (profile.read > 20.000000001 ? 2 : 1) + (aa.this.pop_Z_2() ? 1 : 0);
            if (aa.this.pop_Z_2()) {
               this.init_aa_BufferHelper.init(this.init_DataUtil, now, this.init_Z, profile, this.init_aa_TaskQueue);
            } else {
               this.init_aa_BufferHelper.init();
               this.init_DataUtil.init(now, this.init_Z && !aa.this.buffer(), profile, this.init_aa_TaskQueue);
            }
         }
      }

      void init_V_Z(boolean active) {
         if (active && !this.init_Z) {
            this.init_aa_BufferHelper.init(System.nanoTime());
         }

         this.init_Z = active;
      }

      void init_V_Str(String how) {
         aa.this.init_Str = how;
         if (this.init_I > 0) {
            this.write(how);
            this.init_I--;
            this.load_I++;
            if (aa.this.pop_Z_2() && this.init_I > 0 && this.load_I >= this.load_I()) {
               this.write_V();
            }
         }
      }

      private int load_I() {
         return ci.init_Z() ? 1 : 2;
      }

      void load(String why) {
         aa.this.init_Str = "lost:" + why;
         if (this.init_I > 0) {
            this.init_Str = why;
         }
      }

      private void update() {
         for (int i = 0; i < this.init_I; i++) {
            this.write("lost:" + (this.init_Str.isEmpty() ? "unknown" : this.init_Str));
         }

         this.init_I = 0;
         this.load_I = 0;
         this.init_Str = "";
      }

      private void write(String key) {
         this.init_Map.merge(key, 1, Integer::sum);
         aa.this.init_Str = key;
      }

      String init_Str_Z(boolean reset) {
         String out = this.init_Map.toString()
            + " engine accepted="
            + this.init_DataUtil.read_J()
            + " refused="
            + this.init_DataUtil.update()
            + " obsolete="
            + this.init_DataUtil.sync();
         if (reset) {
            this.init_Map.clear();
         }

         return out;
      }

      void read_V() {
         this.init_DataUtil.init_V_J_DataUtil_NetHelper(System.nanoTime(), this.init_aa_TaskQueue);
         this.init_aa_BufferHelper.init();
         this.init_aa_TaskQueue.init();
         this.init_I = 0;
         this.load_Z = false;
         this.read_Z = false;
         this.init_Z = false;
      }

      void write_V() {
         if (this.init_I > 0 && !aa.this.buffer()) {
            this.init_aa_TaskQueue.init(System.nanoTime(), this.init_I);
            this.write("carried");
         }

         this.init_I = 0;
      }

      void read(String why) {
         if (this.init_I > 0) {
            if (this.load_Z) {
               this.load(why);
            } else {
               this.write_V();
            }
         }
      }

      boolean init_Z_I(int ticks) {
         return aa.this.buffer() ? this.load(ticks) : this.init_I() <= ticks;
      }

      private void sync() {
         this.init_aa_NetHelper.init();
         int[] cycle = new int[20];

         for (int i = 0; i < this.init_aa_NetHelper.init_I; i++) {
            Arrays.fill(cycle, 0);
            aa.init_V_IArr(cycle, aa.this.init_Random, this.read_I(), this.write_I());
            this.init_aa_NetHelper.init(cycle);
            this.init_aa_NetHelper.init_Z(20);
         }
      }

      int init_I_I(int tick) {
         if (!aa.this.buffer()) {
            return tick != 0 ? 0 : (aa.this.pop_Z_2() ? Math.min(this.init_I, Math.max(0, this.load_I() - this.load_I)) : this.init_I);
         } else {
            return this.read_Z() ? 1 : this.init_aa_NetHelper.init_I(tick);
         }
      }

      int init_I() {
         if (!aa.this.buffer()) {
            if (this.init_I > 0) {
               return 0;
            } else {
               long nanos = this.init_DataUtil.init_J_J(System.nanoTime());
               if (nanos == 9223372036854775807L) {
                  return this.init_Z ? 1 : 20;
               } else {
                  return (int)Math.min(20L, (nanos + 49999999L) / 50000000L);
               }
            }
         } else {
            if (this.read_Z()) {
               return 0;
            }

            for (int tick = 0; tick < 20; tick++) {
               if (this.init_aa_NetHelper.init_I(tick) > 0) {
                  return tick;
               }
            }

            return 20;
         }
      }

      private int read_I() {
         return (int)Math.round(aa.load_ValueRange.min());
      }

      private int write_I() {
         return (int)Math.round(aa.load_ValueRange.max());
      }

      private boolean read_Z() {
         if (!aa.this.buffer()) {
            return false;
         } else {
            return aa.this.write(0) ? true : System.currentTimeMillis() - aa.read_J >= 1000L;
         }
      }

      boolean load(int tick) {
         return this.init_I_I(tick) > 0;
      }

      boolean init_Z() {
         return this.load(0);
      }

      private boolean write_Z() {
         return this.read_Z && aa.this.buffer();
      }

      boolean load_Z() {
         if (!this.write_Z() && this.init_I_I(0) <= 0) {
            return false;
         } else if (aa.reset_Z_2()) {
            this.load("missTime");
            return false;
         } else {
            return this.write_Z() || aa.this.write(0);
         }
      }

      void init_V_BooleanSupplier(BooleanSupplier attack) {
         boolean bypass = this.write_Z();
         int amount = aa.this.buffer() ? (!bypass && this.init_I_I(0) <= 0 ? 0 : 1) : this.init_I_I(0);

         for (int i = 0; i < amount; i++) {
            if (aa.reset_Z_2()) {
               this.load("missTime");
            } else if ((bypass || aa.this.write(0)) && attack.getAsBoolean()) {
               this.init_V_Str("attack");
            } else {
               this.load("attackRefused");
            }
         }
      }
   }

   static final class Inner_ConfigStore {
      static final int init_I = 2;
      private static final long init_J = 150000000L;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private int load_I = -1;
      private Vec3 init_Vec3;
      private Vec3 load_Vec3;
      private int read_I;
      private long load_J;

      boolean init_Z() {
         return this.init_Z;
      }

      boolean load_Z() {
         return !this.init_Z && !this.read_Z;
      }

      void init(int entityId, Vec3 missPoint, boolean legacy, int tick) {
         this.init(entityId, missPoint, legacy, tick, System.nanoTime());
      }

      void init(int entityId, Vec3 missPoint, boolean legacy, int tick, long now) {
         this.load_Z = legacy;
         this.init_Z = true;
         this.load_I = entityId;
         this.init_Vec3 = missPoint;
         this.load_Vec3 = null;
         this.read_I = tick;
         this.load_J = now;
      }

      boolean init(int tick) {
         return this.init(tick, System.nanoTime());
      }

      boolean init(int tick, long now) {
         return this.init_Z && (tick - this.read_I >= 2 || now - this.load_J >= 150000000L);
      }

      void init(Vec3 center) {
         if (this.init_Z) {
            if (this.load_Vec3 != null) {
               this.init_Vec3 = this.init_Vec3.add(center.subtract(this.load_Vec3));
            }

            this.load_Vec3 = center;
         }
      }

      boolean load(int entityId) {
         return this.load_I == entityId;
      }

      Vec3 init_Vec3() {
         return this.init_Vec3;
      }

      void init_V() {
         this.read_V();
      }

      boolean read_Z() {
         return this.load_Z;
      }

      void load_V() {
         this.read_V();
         this.read_Z = true;
      }

      void read_V() {
         this.init_Z = false;
         this.read_Z = false;
         this.load_I = -1;
         this.init_Vec3 = null;
         this.load_Vec3 = null;
         this.read_I = 0;
         this.load_J = 0L;
      }
   }

   static final class Inner_DataUtil {
      static final double init_D = 5.0;
      static final int init_I = 21;
      static final int load_I = 30;
      static final double load_D = 8.5;
      static final double read_D = 6.699999809265137;
      static final double write_D = 6.5;
      private final LongSupplier init_LongSupplier;
      private long init_J = -9223372036854775808L;
      private int read_I;
      private double update;
      private double sync;
      private float init_F;
      private float load_F;
      private boolean init_Z;
      private int write_I;

      Inner_DataUtil() {
         this(System::currentTimeMillis);
      }

      Inner_DataUtil(LongSupplier clock) {
         this.init_LongSupplier = clock;
      }

      void init_V() {
         this.init_J = this.init_LongSupplier.getAsLong();
      }

      long init_J() {
         return this.init_J == -9223372036854775808L ? 9223372036854775807L : this.init_LongSupplier.getAsLong() - this.init_J;
      }

      boolean init_Z() {
         return this.init_J != -9223372036854775808L && this.init_LongSupplier.getAsLong() - this.init_J <= 60L;
      }

      void init(float yaw, float pitch, float perfectYaw) {
         if (!this.init_Z) {
            this.init_F = yaw;
            this.load_F = pitch;
            this.init_Z = true;
         } else {
            double yawSpeed = Math.abs(Mth.wrapDegrees(yaw - this.init_F));
            double pitchSpeed = Math.abs(Mth.wrapDegrees(pitch - this.load_F));
            this.init_F = yaw;
            this.load_F = pitch;
            if (!(yawSpeed <= 5.0) && !Float.isNaN(perfectYaw) && this.init_Z()) {
               this.write_I = 0;
               if (this.read_I >= 21) {
                  this.read_I = 0;
                  this.update = 0.0;
                  this.sync = 0.0;
               }

               this.read_I++;
               this.update += yawSpeed + pitchSpeed;
               this.sync = this.sync + Math.abs(Mth.wrapDegrees(yaw - perfectYaw));
            } else {
               this.write_I++;
            }
         }
      }

      void load_V() {
         this.init_Z = false;
      }

      boolean load_Z() {
         return !this.write() && this.init(this.update, 6.699999809265137) > 8.5;
      }

      boolean read_Z() {
         return !this.write() && this.init(this.sync, 6.5) > 8.5;
      }

      private boolean write() {
         if (this.read_I > 0 && this.write_I > 30) {
            this.read_I = 0;
            this.update = 0.0;
            this.sync = 0.0;
            return true;
         } else {
            return false;
         }
      }

      private double init(double sum, double remainingSample) {
         return this.read_I <= 0 ? 0.0 : (sum + (21 - this.read_I) * remainingSample) / 21.0;
      }

      void read_V() {
         this.init_J = -9223372036854775808L;
         this.read_I = 0;
         this.update = 0.0;
         this.sync = 0.0;
         this.init_Z = false;
         this.write_I = 0;
      }
   }

   static final class Inner_JsonUtil {
      private int init_I = -2147483648;
      private boolean init_Z;

      boolean init(int clientTick, BooleanSupplier live) {
         if (clientTick != this.init_I) {
            this.init_I = clientTick;
            this.init_Z = live.getAsBoolean();
         }

         return this.init_Z;
      }
   }

   static final class Inner_NetHelper {
      private final int load;
      final int init_I;
      private final int[] init_IArr;
      private int read;

      Inner_NetHelper(int cycleLength, int iterations) {
         this.load = cycleLength;
         this.init_I = iterations;
         this.init_IArr = new int[cycleLength * iterations];
      }

      int init_I(int relativeIndex) {
         return this.init_IArr[(this.read + relativeIndex) % this.init_IArr.length];
      }

      boolean init_Z(int amount) {
         this.read = (this.read + amount) % this.init_IArr.length;
         return this.read % this.load == 0;
      }

      void init() {
         Arrays.fill(this.init_IArr, 0);
         this.read = 0;
      }

      void init(int[] cycle) {
         if (cycle.length != this.load) {
            throw new IllegalArgumentException("Array size must match cycle length");
         }

         if (this.read == 0) {
            System.arraycopy(cycle, 0, this.init_IArr, this.load, this.load);
         } else {
            if (this.read != this.load) {
               throw new IllegalStateException("Head must be at 0 or cycle length");
            }

            System.arraycopy(cycle, 0, this.init_IArr, 0, this.load);
         }
      }

      int load(int offset) {
         int total = 0;

         for (int index = offset; index < offset + this.load; index++) {
            total += this.init_IArr[index];
         }

         return total;
      }
   }

   static final class Inner_StreamUtil {
      private static final double read_D = 0.3;
      private static final double write_D = 1.45;
      private static final double update_D = 1.45 * Math.sqrt(0.6);
      private static final double sync_D = 2.0;
      private static final double flush_D = 3.4;
      private static final double close_D = 0.3;
      private static final int init_I = 20;
      private static final int load_I = 41;
      private static final double open_D = 1.2;
      private static final double poll_D = 6.0;
      private static final double push_D = 0.04;
      private static final double pop_D = 0.05;
      private static final double tick_D = 0.06;
      private static final double run_D = 1.0;
      private static final int read_I = 2;
      private static final int write_I = 10;
      private static final double get_D = 0.18;
      private static final double set_D = 0.55;
      private static final double put = 0.55 * Math.sqrt(0.36);
      private static final double add = 0.45;
      private static final double clear = 2.5;
      private static final int update_I = 26;
      private static final int sync_I = 15;
      private static final int flush_I = 1;
      private static final int close_I = 2;
      private static final int open_I = 8;
      private static final double reset = 12.5;
      private static final double start = 5.5;
      private static final double stop = 0.55;
      static final double init_D = 30.0;
      static final double load_D = 4.0;
      private final Random init_Random;
      private int poll_I = -2147483648;
      private double next = 0.0 / 0.0;
      private double prev = 1.0;
      private double valid = 2.7;
      private int push_I;
      private int pop_I;
      private int tick_I;
      private int run_I;
      private int get_I;
      private int set_I;
      private double size;
      private double data;
      private double value;
      private double cache;
      private double state;

      Inner_StreamUtil(Random random) {
         this.init_Random = random;
      }

      Vec3 init(int targetEntityId, AABB box, Vec3 eyes, boolean inAttackWindow, boolean allowExcursion) {
         if (this.poll_I != targetEntityId) {
            this.init_V_I_AABB(targetEntityId, box);
         }

         return this.init(box, eyes, inAttackWindow, allowExcursion);
      }

      Vec3 load(int targetEntityId, AABB box, Vec3 eyes, boolean inAttackWindow, boolean allowExcursion) {
         if (this.poll_I != targetEntityId) {
            return this.init(targetEntityId, box, eyes, inAttackWindow, allowExcursion);
         }

         this.init_V_Z_Z(inAttackWindow, allowExcursion);
         return this.init(box, eyes, inAttackWindow, allowExcursion);
      }

      void init_V() {
         this.poll_I = -2147483648;
      }

      private void init_V_I_AABB(int targetEntityId, AABB box) {
         this.poll_I = targetEntityId;
         this.prev = this.init_Random.nextBoolean() ? 1.0 : -1.0;
         this.valid = 2.0 + this.init_Random.nextDouble() * 1.4;
         this.push_I = init_I(this.init_Random);
         this.pop_I = 0;
         this.tick_I = 10;
         this.run_I = load(this.init_Random);
         this.get_I = 0;
         this.set_I = 0;
         this.size = 0.0;
         this.next = 0.0 / 0.0;
         this.data = (this.init_Random.nextDouble() - 0.5) * 2.0 * 0.55;
         Vec3 center = box.getCenter();
         this.value = center.x;
         this.cache = center.y;
         this.state = center.z;
      }

      private void init_V_Z_Z(boolean inAttackWindow, boolean allowExcursion) {
         if (this.tick_I > 0) {
            this.tick_I--;
         }

         boolean excursion = allowExcursion && this.get_I > 0;
         if (excursion && Math.abs(this.next) >= this.size - 1.0E-9) {
            this.get_I--;
         }

         if (--this.run_I <= 0) {
            if (!allowExcursion || inAttackWindow && this.set_I < 8) {
               this.run_I = 1;
               this.set_I++;
            } else {
               this.get_I = 1 + this.init_Random.nextInt(3);
               this.size = init_D(this.init_Random);
               this.run_I = load(this.init_Random);
               this.set_I = 0;
               this.pop_I = 0;
            }
         }

         if (!excursion && --this.push_I <= 0) {
            this.prev = -this.prev;
            this.push_I = init_I(this.init_Random);
         }

         this.valid = this.valid + (this.init_Random.nextDouble() - 0.5) * 0.3;
         this.valid = Math.max(2.0, Math.min(3.4, this.valid));
         if (this.pop_I <= 0 && !excursion && this.tick_I <= 0) {
            double p = 0.05 + (Math.abs(this.next) < 1.0 ? 0.06 : 0.0);
            if (this.init_Random.nextDouble() < p) {
               this.pop_I = 1 + this.init_Random.nextInt(2);
            }
         }
      }

      private Vec3 init(AABB box, Vec3 eyes, boolean inAttackWindow, boolean allowExcursion) {
         Vec3 live = box.getCenter();
         this.value = this.value + 0.55 * (live.x - this.value);
         this.cache = this.cache + 0.55 * (live.y - this.cache);
         this.state = this.state + 0.55 * (live.z - this.state);
         double dx = this.value - eyes.x;
         double dz = this.state - eyes.z;
         double horizontal = Math.max(Math.sqrt(dx * dx + dz * dz), 0.3);
         double halfWidth = Math.min(box.getXsize(), box.getZsize()) * 0.5;
         double hwa = init_D_D_D(halfWidth, horizontal);
         boolean fixated = this.pop_I > 0;
         if (fixated) {
            this.pop_I--;
         }

         double signed = fixated ? this.next : this.init(hwa, inAttackWindow, allowExcursion);
         double vertical = fixated ? this.data : this.init_D();
         double perpX = -dz / horizontal;
         double perpZ = dx / horizontal;
         double lateral = horizontal * Math.tan(Math.toRadians(signed));
         double dy = horizontal * Math.tan(Math.toRadians(vertical));
         return new Vec3(this.value + perpX * lateral, this.cache + dy, this.state + perpZ * lateral);
      }

      private double init(double hwa, boolean inAttackWindow, boolean allowExcursion) {
         if (Double.isNaN(this.next)) {
            this.next = this.prev * this.valid;
            return this.next;
         }

         double cap = inAttackWindow ? 4.0 : 30.0;
         if (allowExcursion && this.get_I > 0) {
            double goal = this.prev * this.size;
            double delta = goal - this.next;
            this.next = Math.abs(delta) > cap ? this.next + Math.copySign(cap, delta) : goal;
            return this.next;
         }

         double goal = this.prev * this.valid;
         double next = this.next + 0.3 * (goal - this.next) + update_D * this.init_Random.nextGaussian();
         double softCap = Math.min(6.0, 1.2 * hwa);
         if (Math.abs(next) > softCap && Math.abs(next) > Math.abs(this.next)) {
            next = Math.copySign(softCap, next);
         }

         double step = next - this.next;
         if (Math.abs(step) > cap) {
            next = this.next + Math.copySign(cap, step);
         }

         this.next = next;
         return this.next;
      }

      private double init_D() {
         double delta = -0.18 * this.data + put * this.init_Random.nextGaussian();
         if (Math.abs(delta) > 0.45) {
            delta = Math.copySign(0.45, delta);
         }

         this.data += delta;
         if (Math.abs(this.data) > 2.5) {
            this.data = Math.copySign(2.5, this.data);
         }

         return this.data;
      }

      static double init_D_D_D(double halfWidth, double distance) {
         return Math.toDegrees(Math.atan(Math.max(halfWidth - 0.04, 0.01) / Math.max(distance, 0.3)));
      }

      static int init_I(Random random) {
         return 20 + random.nextInt(41);
      }

      static int load(Random random) {
         return 26 + random.nextInt(15);
      }

      static double init_D(Random random) {
         return 12.5 + random.nextDouble() * 5.5;
      }
   }

   static final class Inner_TaskQueue implements autismclient.util.a.DataUtil.Inner_NetHelper {
      private static final long init_J = 150000000L;
      int init_I = 1;
      private int load_I;
      private boolean init_Z;
      private long load_J;

      @Override
      public boolean init_Z_J(long now) {
         if (this.load_I >= this.init_I) {
            return false;
         }

         if (this.load_I == 0) {
            this.load_J = now;
            this.init_Z = false;
         }

         this.load_I++;
         return true;
      }

      @Override
      public void init_V_J(long now) {
      }

      boolean load_Z(long now) {
         return this.load_I > 0 && now - this.load_J > 150000000L;
      }

      int init_I(long now) {
         int fresh = this.load_I > 0 && now - this.load_J <= 150000000L ? this.load_I : 0;
         this.load_I = 0;
         return fresh;
      }

      void init(long now, int clicks) {
         this.load_I = Math.max(this.load_I, Math.min(Math.max(1, clicks), this.init_I));
         this.init_Z = true;
         this.load_J = now;
      }

      void init() {
         this.load_I = 0;
      }
   }
}
