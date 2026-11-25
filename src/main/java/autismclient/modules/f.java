package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.PacketListSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.Setting;
import autismclient.api.module.StringListSetting;
import autismclient.api.module.StringSetting;
import autismclient.api.module.ValueRange;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatSwingPacket;
import autismclient.mixin.accessor.AutismFishingHookAccessor;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.mixin.accessor.AutismMobEffectInstanceAccessor;
import autismclient.mixin.accessor.AutismMovePlayerPacketAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AuditClientFixes;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismWaypoints;
import autismclient.util.PkgCls_b;
import autismclient.util.bM;
import autismclient.util.bw;
import autismclient.util.cG;
import autismclient.util.cL;
import autismclient.util.cM;
import autismclient.util.cc;
import autismclient.util.cd;
import autismclient.util.ce;
import autismclient.util.cj;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.dd;
import autismclient.util.di;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.oresim.AutismOreSimSeedStore;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class f {
   private static f.Inner_EventBus init;

   private f() {
   }

   static void init_V() {
      init("inv-move", AutismConfig.getGlobal().inventoryMove);
      init("xcarry", AutismConfig.getGlobal().xCarry);
      init("golden-lever", true);
      if (AutismLiteVariant.enabled()) {
         al.init_V_Module(new f.Inner_HashUtil());
      } else {
         al.init_V_Module(new f.Inner_FileCache());
         al.init_V_Module(new f.Inner_MapStore());
         al.init_V_Module(new f.Inner_MethodUtil());
         al.init_V_Module(new f.Inner_ListPool());
         al.init_V_Module(new e());
         al.init_V_Module(new G());
         al.init_V_Module(new f.Inner_TableIndex());
         al.init_V_Module(new f.Inner_PathHelper());
         al.init_V_Module(new DataUtil());
         al.init_V_Module(new aw());
         al.init_V_Module(new ax());
         al.init_V_Module(new aA());
         al.init_V_Module(new RenderState());
         al.init_V_Module(new N());
         al.init_V_Module(new aZ());
         al.init_V_Module(new av());
         al.init_V_Module(new PhaseModule());
         al.init_V_Module(new CachePool());
         al.init_V_Module(new bh());
         al.init_V_Module(new f.Inner_EventBus());
         al.init_V_Module(new f.Inner_RenderState());
         al.init_V_Module(new f.Inner_StreamUtil());
         al.init_V_Module(new f.Inner_CachePool());
         al.init_V_Module(new ba());
         al.init_V_Module(new AutoTotemModule());
         al.init_V_Module(new j());
         al.init_V_Module(new StateCache());
         al.init_V_Module(new bg());
         al.init_V_Module(new z());
         al.init_V_Module(new ListPool());
         al.init_V_Module(new SurroundModule());
         al.init_V_Module(new AnchorAuraModule());
         al.init_V_Module(new t());
         al.init_V_Module(new TaskTimer());
         al.init_V_Module(new aQ());
         al.init_V_Module(new AutoTrapModule());
         al.init_V_Module(new UtilCore());
         al.init_V_Module(new X());
         al.init_V_Module(new aa());
         al.init_V_Module(new AutoFarmModule());
         al.init_V_Module(new MethodUtil());
         al.init_V_Module(new bd());
         AutismConfig leafConfig = AutismConfig.getGlobal();
         if (!leafConfig.modules.containsKey("leaf-dupe") && leafConfig.modules.containsKey("exploit-addon:leaf-dupe")) {
            leafConfig.modules.put("leaf-dupe", leafConfig.modules.remove("exploit-addon:leaf-dupe"));
         }

         al.init_V_Module(new ac());
         al.init_V_Module(new TableIndex());
         al.init_V_Module(new TokenStore());
         al.init_V_Module(new CharEncoder());
         al.init_V_Module(new aR());
         init("antibot", true);
         al.init_V_Module(new NetHelper());
         al.init_V_Module(new SyncManager());
         al.init_V_Module(new f.Inner_BufferHelper());
         al.init_V_Module(new f.Inner_PoolEntry());
         al.init_V_Module(new f.Inner_TaskQueue());
         al.init_V_Module(new ScaffoldModule());
         al.init_V_Module(new f.Inner_BitSetUtil());
         al.init_V_Module(new f.Inner_CharEncoder());
         al.init_V_Module(new StreamUtil());
         al.init_V_Module(new J());
         al.init_V_Module(new f.Inner_TokenStore());
         al.init_V_Module(new f.Inner_TaskTimer());
         al.init_V_Module(new aE());
         al.init_V_Module(new f.Inner_StateCache());
         al.init_V_Module(new f.Inner_FieldCache());
         al.init_V_Module(new f.Inner_ConfigStore());
         al.init_V_Module(new f.Inner_HashUtil());
         al.init_V_Module(new R());
         al.init_V_Module(new JsonUtil());
         al.init_V_Module(new aH());
         al.init_V_Module(new f.Inner_RefQueue());
         al.init_V_Module(new f.Inner_TextFormat());
         al.init_V_Module(new f.Inner_NetHelper());
         al.init_V_Module(new f.Inner_PacketUtil());
         al.init_V_Module(new f.Inner_UiHelper());
         al.init_V_Module(new f.Inner_KeyHolder());
         al.init_V_Module(new bi());
         al.init_V_Module(new K());
         al.init_V_Module(new f.Inner_ByteReader());
         al.init_V_Module(new I());
         al.init_V_Module(new f.Inner_SyncManager());
         al.init_V_Module(new g());
         al.init_V_Module(new f.Inner_RingBuffer());
         al.init_V_Module(new au());
         al.init_V_Module(new f.Inner_ClassCache());
         al.init_V_Module(new TrajectoriesModule());
         al.init_V_Module(new f.Inner_ArrayUtil());
         al.init_V_Module(new f.Inner_JsonUtil());
         al.init_V_Module(new ClassCache());
         al.init_V_Module(new E());
         al.init_V_Module(new P());
         al.init_V_Module(new ay());
         al.init_V_Module(new n());
         al.init_V_Module(new EventBus());
         al.init_V_Module(new f.Inner_NodeWalker());
         al.init_V_Module(new f.Inner_IoWorker());
         al.init_V_Module(new f.Inner_DataUtil());
         al.init_V_Module(new M());
         al.init_V_Module(new as());
         init("teams", true);
         al.init_V_Module(new aL());
         al.init_V_Module(new aG());
         al.init_V_Module(new d());
      }
   }

   private static void init(String id, boolean enabled) {
      AutismConfig config = AutismConfig.getGlobal();
      if (!config.modules.containsKey(id)) {
         AutismConfig.ModuleState state = config.modules.computeIfAbsent(id, ignored -> new AutismConfig.ModuleState());
         state.enabled = enabled;
      }
   }

   public static boolean init_Z() {
      f.Inner_EventBus fastUse = init_f_EventBus();
      return fastUse != null && fastUse.init_Z_41();
   }

   public static boolean load() {
      f.Inner_EventBus fastUse = init_f_EventBus();
      return fastUse != null && fastUse.load_Z;
   }

   public static boolean read() {
      f.Inner_EventBus fastUse = init_f_EventBus();
      return fastUse != null && fastUse.load_Z_20();
   }

   public static float init(float vanillaYaw) {
      f.Inner_EventBus fastUse = init_f_EventBus();
      ce.Inner_DataUtil rotation = fastUse == null ? null : fastUse.load_ce_DataUtil_4();
      return rotation == null ? vanillaYaw : rotation.init();
   }

   public static float load(float vanillaPitch) {
      f.Inner_EventBus fastUse = init_f_EventBus();
      ce.Inner_DataUtil rotation = fastUse == null ? null : fastUse.load_ce_DataUtil_4();
      return rotation == null ? vanillaPitch : rotation.load();
   }

   public static float init(LocalPlayer player, float vanillaYaw) {
      ce.Inner_DataUtil rotation = init(player);
      return rotation == null ? vanillaYaw : rotation.init();
   }

   public static float load(LocalPlayer player, float vanillaPitch) {
      ce.Inner_DataUtil rotation = init(player);
      return rotation == null ? vanillaPitch : rotation.load();
   }

   private static ce.Inner_DataUtil init(LocalPlayer player) {
      Minecraft client = Minecraft.getInstance();
      if (player != null && client != null && player == client.player) {
         f.Inner_EventBus fastUse = init_f_EventBus();
         return fastUse == null ? null : fastUse.load_ce_DataUtil_4();
      } else {
         return null;
      }
   }

   private static f.Inner_EventBus init_f_EventBus() {
      f.Inner_EventBus fastUse = init;
      return fastUse != null && fastUse.isEnabled() ? fastUse : null;
   }

   static final class Inner_ArrayUtil extends Module {
      Inner_ArrayUtil() {
         super("storage-esp", "StorageESP", ae.update, "Highlights chests, shulkers, and other storage.");
         this.add(new BoolSetting("fill", "Fill", false).group("General").description("Draw translucent filled boxes."));
         this.add(new BoolSetting("meshing", "Meshing", true).group("General").description("Merge adjacent storage."));
         this.add(new BoolSetting("tracers", "Tracers", false).group("Tracers").description("Line to each target"));
         this.add(RegistryListSetting.storages("storage-list", "Storage", ap.init_Str).group("Storage").description("Blocks and entities"));
         this.add(new ColorSetting("chest-color", "Chest", -855662592).group("Colors").description("Chest + chest minecart/boat."));
         this.add(new ColorSetting("trapped-chest-color", "Trapped Chest", -855695328).group("Colors"));
         this.add(new ColorSetting("ender-chest-color", "Ender Chest", -864550657).group("Colors"));
         this.add(new ColorSetting("barrel-color", "Barrel", -855662592).group("Colors"));
         this.add(new ColorSetting("shulker-color", "Shulker Box", -860395777).group("Colors"));
         this.add(new ColorSetting("hopper-color", "Hopper", -864253185).group("Colors").description("Hopper + minecart."));
         this.add(new ColorSetting("dispenser-color", "Dispenser", -860862392).group("Colors").description("Dispenser + dropper."));
         this.add(new ColorSetting("furnace-color", "Furnace", -859000218).group("Colors").description("Furnaces + brewing stand."));
         this.add(new ColorSetting("crafter-color", "Crafter", -858214805).group("Colors").description("Crafter, pot, bookshelf"));
         this.add(new ColorSetting("other-color", "Other", -7566196).group("Colors").description("Fallback."));

         for (aq.Inner_DataUtil structure : aq.init) {
            this.add(new BoolSetting(structure.init_Str(), structure.load(), false).group("Ignore Structures"));
         }
      }
   }

   static final class Inner_BitSetUtil extends Module {
      private boolean init_Z;
      private BlockPos init_BlockPos;
      private int init_I;

      Inner_BitSetUtil() {
         super("no-fall", "NoFall", ae.load_ae, "Reduces fall damage.");
         this.add(new ChoiceSetting("mode", "Mode", "Packet", "Packet", "AirPlace", "Place").description("Fall protection method.").build());
         this.add(
            new ChoiceSetting("placed-item", "Placed Item", "Bucket", "Bucket", "PowderSnow", "HayBale", "Cobweb", "SlimeBlock")
               .visibleWhen(() -> "Place".equals(this.value("mode")) || "AirPlace".equals(this.value("mode")))
               .build()
         );
         this.add(
            new ChoiceSetting("air-place-mode", "Air Place Mode", "BeforeDeath", "BeforeDamage", "BeforeDeath")
               .visibleWhen(() -> "AirPlace".equals(this.value("mode")))
               .build()
         );
         this.add(new BoolSetting("anchor", "Anchor", true).visibleWhen(() -> !"Packet".equals(this.value("mode"))).description("Center on the block before placing.").build());
         this.add(new BoolSetting("anti-bounce", "Anti Bounce", true).description("Stop bounce effects."));
         this.add(new BoolSetting("pause-on-mace", "Pause On Mace", true).description("Pause holding mace"));
         this.add(
            new BoolSetting("client-rotate", "Client Rotation", false)
               .visibleWhen(() -> !"Packet".equals(this.value("mode")))
               .description("Also rotate camera client-side.")
               .build()
         );
      }

      @Override
      public void onEnable() {
         this.init_Z = false;
         this.init_BlockPos = null;
         this.init_I = 0;
      }

      @Override
      public void onDisable() {
         this.init_Z = false;
         this.init_BlockPos = null;
         this.init_I = 0;
      }

      @Override
      public void tick() {
         if ((AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && !IoWorker.init_Z()
            && MC.player != null
            && MC.gameMode != null
            && !"Packet".equals(this.choice("mode"))
            && (autismclient.util.aD.init_Str() == null || !autismclient.util.aD.read_Z())) {
            this.init_V_34();
            if (this.load_Z_16()) {
               if (this.bool("anchor")) {
                  this.load_V_24();
               }

               if ("AirPlace".equals(this.choice("mode"))) {
                  this.write();
               } else {
                  this.read();
               }
            }
         }
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            return false;
         } else if (!("Packet".equals(this.choice("mode")) && packet instanceof ServerboundMovePlayerPacket move) || !this.init_Z_37()) {
            return false;
         } else {
            if (move.isOnGround()) {
               return false;
            }

            ((AutismMovePlayerPacketAccessor)move).autism$setOnGround(true);
            return false;
         }
      }

      private boolean init_Z_37() {
         if (IoWorker.init_Z()) {
            return false;
         }

         if (MC.player != null && !MC.player.getAbilities().instabuild) {
            if (this.bool("pause-on-mace") && MC.player.getMainHandItem().is(Items.MACE)) {
               return false;
            } else {
               Module flight = al.init_Module_Str("flight");
               if (flight != null && flight.isEnabled()) {
                  return true;
               } else {
                  return MC.player.isFallFlying() ? false : MC.player.getDeltaMovement().y <= -0.5;
               }
            }
         } else {
            return false;
         }
      }

      private boolean load_Z_16() {
         if (MC.player != null && !MC.player.getAbilities().instabuild && !MC.player.isFallFlying() && !MC.player.isInWater() && !MC.player.isInLava()) {
            if (this.bool("pause-on-mace") && MC.player.getMainHandItem().is(Items.MACE)) {
               return false;
            } else {
               double fall = MC.player.fallDistance;
               if ("AirPlace".equals(this.choice("mode"))) {
                  return "BeforeDeath".equals(this.choice("air-place-mode"))
                     ? fall > Math.max(2.0, MC.player.getHealth() + MC.player.getAbsorptionAmount())
                     : fall > 2.0;
               } else {
                  return fall > 3.0 && !this.update();
               }
            }
         } else {
            return false;
         }
      }

      private boolean read() {
         if (HashUtil.load_Z()) {
            return false;
         }

         int oldSlot = MC.player.getInventory().getSelectedSlot();
         int slot = this.init_I_24();
         if (slot < 0) {
            return false;
         }

         BlockHitResult ray = MC.level
            .clip(new ClipContext(MC.player.position(), MC.player.position().subtract(0.0, 5.0, 0.0), Block.OUTLINE, Fluid.NONE, MC.player));
         if (ray != null && ray.getType() == Type.BLOCK) {
            BlockPos target = ray.getBlockPos().above();
            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            try {
               autismclient.util.aw.init_V(MC, slot);

               try {
                  Item item = this.init_Item();
                  if (item != Items.WATER_BUCKET && item != Items.POWDER_SNOW_BUCKET) {
                     BlockPos below = target.below();
                     Vec3 hitVec = Vec3.atCenterOf(below).add(0.0, 0.5, 0.0);
                     BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below, false);
                     if (al.init_Z_HitResult(hit, InteractionHand.MAIN_HAND, this.id())) {
                        return false;
                     }

                     if (!bM.init(this.id())) {
                        return false;
                     }

                     this.init(init_D_Vec3_2(hitVec), load(hitVec), () -> MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, hit));
                  } else {
                     Vec3 hitVec = Vec3.atCenterOf(target);
                     if (al.init_Z_HitResult(ray, InteractionHand.MAIN_HAND, this.id())) {
                        return false;
                     }

                     if (!bM.init(this.id())) {
                        return false;
                     }

                     this.init(init_D_Vec3_2(hitVec), load(hitVec), () -> MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND));
                     this.init_Z = true;
                     this.init_BlockPos = target;
                     this.init_I = 0;
                  }

                  return true;
               } finally {
                  autismclient.util.aw.init_V(MC, oldSlot);
               }
            } finally {
               autismclient.util.ah.read_V(this.id());
            }
         } else {
            return false;
         }
      }

      private boolean write() {
         if (HashUtil.load_Z()) {
            return false;
         }

         int oldSlot = MC.player.getInventory().getSelectedSlot();
         int slot = this.load_I_12();
         if (slot < 0) {
            return false;
         }

         Vec3 preVelocity = MC.player.getDeltaMovement();
         if (!autismclient.util.ah.write_Z(this.id())) {
            return false;
         }

         try {
            autismclient.util.aw.init_V(MC, slot);

            try {
               MC.player.setDeltaMovement(preVelocity.x, 0.0, preVelocity.z);
               BlockPos below = MC.player.blockPosition().below();
               Vec3 hitVec = Vec3.atCenterOf(below).add(0.0, 0.5, 0.0);
               BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below, false);
               if (al.init_Z_HitResult(hit, InteractionHand.MAIN_HAND, this.id())) {
                  return false;
               }

               if (!bM.init(this.id())) {
                  return false;
               }

               this.init(MC.player.getYRot(), 90.0, () -> MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, hit));
               return true;
            } finally {
               MC.player.setDeltaMovement(preVelocity);
               autismclient.util.aw.init_V(MC, oldSlot);
            }
         } finally {
            autismclient.util.ah.read_V(this.id());
         }
      }

      private void init(double yaw, double pitch, Runnable action) {
         if (MC.player != null && MC.getConnection() != null) {
            boolean clientRotate = this.bool("client-rotate");
            float ny = (float)yaw;
            float np = (float)pitch;
            float oldYaw = MC.player.getYRot();
            float oldPitch = MC.player.getXRot();
            float oldYawO = MC.player.yRotO;
            float oldPitchO = MC.player.xRotO;
            float oldHeadYaw = MC.player.yHeadRot;
            float oldHeadYawO = MC.player.yHeadRotO;
            float oldBodyYaw = MC.player.yBodyRot;
            float oldBodyYawO = MC.player.yBodyRotO;
            MC.player.setYRot(ny);
            MC.player.setXRot(np);
            if (clientRotate) {
               MC.player.yRotO = ny;
               MC.player.xRotO = np;
               MC.player.yHeadRot = ny;
               MC.player.yHeadRotO = ny;
               MC.player.yBodyRot = ny;
               MC.player.yBodyRotO = ny;
            }

            MC.getConnection().send(new Rot(ny, np, MC.player.onGround(), MC.player.horizontalCollision));

            try {
               action.run();
            } finally {
               if (!clientRotate) {
                  MC.player.setYRot(oldYaw);
                  MC.player.setXRot(oldPitch);
                  MC.player.yRotO = oldYawO;
                  MC.player.xRotO = oldPitchO;
                  MC.player.yHeadRot = oldHeadYaw;
                  MC.player.yHeadRotO = oldHeadYawO;
                  MC.player.yBodyRot = oldBodyYaw;
                  MC.player.yBodyRotO = oldBodyYawO;
               }
            }
         } else {
            action.run();
         }
      }

      private static double init_D_Vec3_2(Vec3 target) {
         double dx = target.x - MC.player.getX();
         double dz = target.z - MC.player.getZ();
         return Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
      }

      private static double load(Vec3 target) {
         Vec3 eye = MC.player.getEyePosition();
         double dx = target.x - eye.x;
         double dy = target.y - eye.y;
         double dz = target.z - eye.z;
         double horiz = Math.sqrt(dx * dx + dz * dz);
         return -Math.toDegrees(Math.atan2(dy, horiz));
      }

      private void init_V_34() {
         if (!HashUtil.load_Z() && this.init_Z && this.init_BlockPos != null && MC.level != null && MC.player != null && MC.gameMode != null) {
            if (++this.init_I > 20) {
               this.init_Z = false;
               this.init_BlockPos = null;
               this.init_I = 0;
            } else {
               boolean insidePlaced = MC.player.getInBlockState().is(Blocks.WATER) || MC.player.getInBlockState().is(Blocks.POWDER_SNOW);
               boolean snowBelow = MC.level.getBlockState(MC.player.blockPosition().below()).is(Blocks.POWDER_SNOW) && MC.player.fallDistance == 0.0;
               if (insidePlaced || snowBelow) {
                  int oldSlot = MC.player.getInventory().getSelectedSlot();
                  int bucket = this.init_I_Item_2(Items.BUCKET);
                  if (bucket >= 0 && autismclient.util.ah.write_Z(this.id())) {
                     try {
                        autismclient.util.aw.init_V(MC, bucket);

                        try {
                           Vec3 hitVec = Vec3.atCenterOf(this.init_BlockPos);
                           BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, this.init_BlockPos, false);
                           if (al.init_Z_HitResult(hit, InteractionHand.MAIN_HAND, this.id())) {
                              return;
                           }

                           if (!bM.init(this.id())) {
                              return;
                           }

                           this.init(init_D_Vec3_2(hitVec), load(hitVec), () -> MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND));
                           this.init_Z = false;
                           this.init_BlockPos = null;
                           this.init_I = 0;
                        } finally {
                           autismclient.util.aw.init_V(MC, oldSlot);
                        }
                     } finally {
                        autismclient.util.ah.read_V(this.id());
                     }

                     return;
                  }
               }
            }
         }
      }

      private void load_V_24() {
         double x = Math.floor(MC.player.getX()) + 0.5;
         double z = Math.floor(MC.player.getZ()) + 0.5;
         MC.player.setPos(x, MC.player.getY(), z);
         if (!HashUtil.init_Z() && MC.getConnection() != null) {
            MC.getConnection().send(new Pos(MC.player.getX(), MC.player.getY(), MC.player.getZ(), MC.player.onGround(), MC.player.horizontalCollision));
         }
      }

      private boolean update() {
         if (MC.level != null && MC.player != null) {
            MutableBlockPos pos = MC.player.blockPosition().mutable();
            int bottom = MC.level.getMinY();

            while (pos.getY() > bottom) {
               BlockState state = MC.level.getBlockState(pos);
               if (state.isSolid()) {
                  break;
               }

               if (state.getFluidState().is(FluidTags.WATER)) {
                  return true;
               }

               pos.move(Direction.DOWN);
            }

            return false;
         } else {
            return false;
         }
      }

      private int init_I_24() {
         Item item = this.init_Item();
         return this.init_I_Item_2(item);
      }

      private int load_I_12() {
         Item preferred = this.init_Item();

         for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(preferred) && stack.getItem() instanceof BlockItem) {
               return i;
            }
         }

         for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
               return i;
            }
         }

         return -1;
      }

      private int init_I_Item_2(Item item) {
         for (int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
               return i;
            }
         }

         return -1;
      }

      private Item init_Item() {
         Item var10000;
         switch (this.choice("placed-item")) {
            case "PowderSnow":
               var10000 = Items.POWDER_SNOW_BUCKET;
               break;
            case "HayBale":
               var10000 = Items.HAY_BLOCK;
               break;
            case "Cobweb":
               var10000 = Items.COBWEB;
               break;
            case "SlimeBlock":
               var10000 = Items.SLIME_BLOCK;
               break;
            default:
               var10000 = MC.level != null && "the_nether".equals(MC.level.dimension().identifier().getPath()) ? Items.POWDER_SNOW_BUCKET : Items.WATER_BUCKET;
         }

         return var10000;
      }

      @Override
      public void onGameLeft() {
         this.init_Z = false;
         this.init_BlockPos = null;
         this.init_I = 0;
      }
   }

   static final class Inner_BufferHelper extends Module {
      private final Random init_Random = new Random();
      private String init_Str = "";
      private Set<String> init_Set = Set.of();
      private final BitSetUtil init_BitSetUtil = new BitSetUtil();
      private Object init_Obj;
      private BitSetUtil.Inner_CachePool init_BitSetUtil_CachePo;
      private InteractionHand init_InteractionHand;
      private ItemStack init_ItemStack = ItemStack.EMPTY;
      private int init_I = -1;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private int load_I;
      private cM init_cM;
      private float init_F;
      private boolean write_Z;
      private int read_I;
      private int write_I = 1;
      private boolean update_Z;
      private long init_J = -1L;
      private boolean sync_Z;
      private int update_I;
      private int sync_I;
      private boolean flush;

      Inner_BufferHelper() {
         super("auto-fish", "AutoFish", ae.read, "Automatically reels and recasts fishing rods.");
         this.add(new ChoiceSetting("trigger-mode", "Trigger", "Sound", "Sound", "Bobber").build());
         this.add(
            RegistryListSetting.soundEvents("sounds", "Sounds", "minecraft:entity.fishing_bobber.splash")
               .visibleWhen(() -> "Sound".equals(this.value("trigger-mode")))
               .build()
         );
         this.add(
            new BoolSetting("sound-distance", "Sound Distance", true)
               .visibleWhen(() -> "Sound".equals(this.value("trigger-mode")))
               .description("Require sound near player.")
         );
         this.add(
            new DoubleSetting("max-sound-distance", "Max Distance", 12.0, 1.0, 128.0, 0.5)
               .visibleWhen(() -> "Sound".equals(this.value("trigger-mode")) && Boolean.parseBoolean(this.value("sound-distance")))
               .description("Max player sound range.")
               .build()
         );
         this.add(new ChoiceSetting("click-mode", "Click Mode", "Real Input", "Real Input", "Interaction API").build());
         this.add(new BoolSetting("auto-cast", "Auto Cast", true).group("Casting").description("Cast and recast automatically").build());
         this.add(new IntSetting("cast-timeout", "No Bite Retry", 60, 0, 300, 1).group("Casting").description("0 = wait forever.").unit("s"));
         this.add(new BoolSetting("randomize-delays", "Randomize Delays", true).group("Delays").build());
         this.add(
            new IntSetting("catch-min", "Catch Min", 2, 0, 32, 1)
               .group("Delays")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("randomize-delays")))
               .build()
         );
         this.add(
            new IntSetting("catch-max", "Catch Max", 5, 0, 40, 1)
               .group("Delays")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("randomize-delays")))
               .build()
         );
         this.add(
            new IntSetting("recast-min", "Recast Min", 8, 0, 32, 1)
               .group("Delays")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("randomize-delays")))
               .build()
         );
         this.add(
            new IntSetting("recast-max", "Recast Max", 12, 0, 40, 1)
               .group("Delays")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("randomize-delays")))
               .build()
         );
         this.add(
            new IntSetting("catch-delay", "Catch Delay", 3, 0, 40, 1)
               .group("Delays")
               .visibleWhen(() -> !Boolean.parseBoolean(this.value("randomize-delays")))
               .build()
         );
         this.add(
            new IntSetting("recast-delay", "Recast Delay", 10, 0, 40, 1)
               .group("Delays")
               .visibleWhen(() -> !Boolean.parseBoolean(this.value("randomize-delays")))
               .build()
         );
         this.add(new IntSetting("movement-steps", "Steps", 6, 0, 6, 1).group("Movement").build());
         this.add(
            new DoubleSetting("degrees-per-step", "Degrees / Step", 5.0, 0.1, 20.0, 0.1)
               .group("Movement")
               .visibleWhen(() -> !"0".equals(this.value("movement-steps")))
               .build()
         );
         this.add(
            new IntSetting("movement-duration", "Duration", 6, 1, 30, 1).group("Movement").visibleWhen(() -> !"0".equals(this.value("movement-steps"))).build()
         );
         this.add(new BoolSetting("anti-break", "Anti Break", true).group("Rod Safety").build());
         this.add(
            new IntSetting("min-durability", "Min Durability", 2, 1, 32, 1)
               .group("Rod Safety")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("anti-break")))
               .build()
         );
         this.add(new BoolSetting("auto-select-rod", "Auto Select Best Rod", true).group("Rod Safety").build());
         this.add(new BoolSetting("stop-macro-enabled", "Auto Stop", false).group("Auto Stop").description("Silent conditional rule").build());
         this.add(
            new StringSetting("stop-macro", "Rule", "")
               .group("Auto Stop")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("stop-macro-enabled")))
               .conditionalMacroPicker()
         );
         this.add(
            new ChoiceSetting("stop-macro-trigger", "On Trigger", "Stop AutoFish", "Stop AutoFish", "Run Steps Only", "Stop, Run, Resume")
               .group("Auto Stop")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("stop-macro-enabled")))
               .description("Action on condition")
               .build()
         );
      }

      @Override
      protected void onOptionValueChanged(String optionId) {
         this.init_V_Str_5(optionId);
         if ("trigger-mode".equals(optionId) || "sounds".equals(optionId) || "click-mode".equals(optionId)) {
            this.read_V_17();
            if ("trigger-mode".equals(optionId)) {
               this.init_BitSetUtil.read();
            }
         }

         if ("stop-macro".equals(optionId)
            || "stop-macro-enabled".equals(optionId)
            || "stop-macro-trigger".equals(optionId)
            || "anti-break".equals(optionId)
            || "min-durability".equals(optionId)
            || "auto-select-rod".equals(optionId)) {
            this.run();
            this.set();
         }
      }

      @Override
      public void onEnable() {
         this.poll();
         this.push();
         this.run();
         this.get();
      }

      @Override
      public void onDisable() {
         this.set();
         this.open();
      }

      @Override
      public void onGameLeft() {
         this.put();
         this.open();
      }

      @Override
      public void tick() {
         this.poll();
         if (MC != null && MC.player != null && MC.level != null) {
            if (this.init_Obj != MC.level) {
               this.close();
               this.init_Obj = MC.level;
            }

            if (!AutoTotemModule.init_Z_6() && (autismclient.util.aD.init_Str() == null || !autismclient.util.aD.read_Z())) {
               this.pop();
               if (this.isEnabled()) {
                  if (MC.gui.screen() == null && MC.gui.overlay() == null) {
                     if (this.flush) {
                        this.update();
                     }

                     if (this.sync_I > 0) {
                        this.sync_I--;
                        this.read_V_17();
                     } else if (!this.init_f_BufferHelper_Buf().init_Z()) {
                        this.read_V_17();
                     } else {
                        BitSetUtil.Inner_CachePool use = this.init_BitSetUtil
                           .init(
                              this.init_I_25(),
                              this.bool("auto-cast"),
                              this.read_I_7(),
                              this.load_I_13(),
                              this.read_Z,
                              () -> this.init_I("recast-delay", "recast-min", "recast-max")
                           );
                        if (this.init_BitSetUtil.load_Z()) {
                           this.sync();
                        }

                        if ("Bobber".equals(this.choice("trigger-mode"))) {
                           FishingHook hook = this.init_FishingHook();
                           boolean biting = hook != null && ((AutismFishingHookAccessor)hook).autism$isBiting();
                           if (biting) {
                              this.init_V_35();
                           }
                        }

                        this.flush();
                        if (use != null && !this.init_Z_BitSetUtil_CachePo(use)) {
                           this.init(use, false);
                        }
                     }
                  } else {
                     this.write();
                  }
               }
            } else {
               this.read_V_17();
            }
         } else {
            this.close();
            this.init_Obj = null;
         }
      }

      @Override
      public void onRenderLevel(float partialTick) {
         if (this.read_Z) {
            if (!this.load_Z_17() || !autismclient.util.ba.read()) {
               this.init_V_Z_9(false);
            } else if (autismclient.util.aZ.init(0, ck.init_ck().state_I())) {
               this.init_V_Z_9(true);
            } else {
               this.init_V_D(Math.max(0, this.load_I - 1) + partialTick);
            }
         }
      }

      @Override
      public void onMouseRotation(double deltaYaw, double deltaPitch) {
         if (this.read_Z) {
            if (autismclient.util.aZ.init(0, ck.init_ck().state_I())) {
               this.init_V_Z_9(true);
            } else {
               this.init_V_D(Math.max(0, this.load_I - 1));
            }
         }
      }

      @Override
      public void onSoundPacket(ClientboundSoundPacket packet) {
         if ("Sound".equals(this.choice("trigger-mode")) && this.load_Z_17() && this.sync_I <= 0 && !this.flush && this.init_Z_ClientboundSoundPa(packet)) {
            if (this.bool("sound-distance")) {
               double max = this.init_D_ClientboundSoundPa(packet);
               if (MC.player.position().distanceToSqr(packet.getX(), packet.getY(), packet.getZ()) > max * max) {
                  return;
               }
            }

            this.init_BitSetUtil.init_Z_I_J(this.init_I("catch-delay", "catch-min", "catch-max"), ck.init_ck().state_I());
         }
      }

      @Override
      public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
         return autismclient.util.as.init_Z_Str(this.id()) && !this.read_Z_16();
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (packet instanceof ServerboundUseItemPacket usePacket && this.init_Z_InteractionHand(usePacket.hand())) {
            if (autismclient.util.as.init_Z_Str(this.id())) {
               if (usePacket.hand() == this.init_InteractionHand) {
                  autismclient.util.as.init_V_Str(this.id());
               }

               return false;
            }

            if (this.init_Z) {
               if (usePacket.hand() == this.init_InteractionHand) {
                  this.load_Z = true;
               }

               return false;
            }

            if (autismclient.util.ah.load() == null) {
               this.load_V_25();
            }
         } else if (packet instanceof ServerboundUseItemOnPacket useOnPacket) {
            if (!this.init_Z
               && !autismclient.util.as.init_Z_Str(this.id())
               && autismclient.util.ah.load() == null
               && this.init_Z_InteractionHand(useOnPacket.hand())) {
               this.read_V_17();
               this.sync_I = Math.max(this.sync_I, 3);
            }
         } else if (packet instanceof ServerboundSetCarriedItemPacket carriedPacket) {
            if (autismclient.util.ah.load() != null) {
               return false;
            }

            this.init_V_I_10(carriedPacket.getSlot());
         }

         return false;
      }

      private double init_D_ClientboundSoundPa(ClientboundSoundPacket packet) {
         double configured = Math.max(1.0, this.decimal("max-sound-distance"));
         if (packet == null) {
            return configured;
         }

         double audible = Math.max(1.0, packet.getVolume()) * 16.0;
         return Math.min(configured, audible);
      }

      private void init_V_35() {
         this.init_BitSetUtil.init_Z(this.init_I("catch-delay", "catch-min", "catch-max"));
      }

      private int init_I(String fixedOption, String minOption, String maxOption) {
         this.poll();
         if (!this.bool("randomize-delays")) {
            return Math.max(0, this.integer(fixedOption));
         }

         int min = Math.max(0, this.integer(minOption));
         int max = Math.max(min, this.integer(maxOption));
         return min + this.init_Random.nextInt(max - min + 1);
      }

      private boolean init_Z_ClientboundSoundPa(ClientboundSoundPacket packet) {
         if (packet != null && packet.getSound() != null && packet.getSound().value() != null) {
            Identifier id = packet.getSound().value().location();
            if (id == null) {
               return false;
            }

            String soundId = id.toString().toLowerCase(Locale.ROOT);
            Set<String> sounds = this.init_Set_11();
            return sounds.contains(soundId) || sounds.contains(soundId.substring(soundId.indexOf(58) + 1));
         } else {
            return false;
         }
      }

      private Set<String> init_Set_11() {
         List<String> sounds = this.list("sounds");
         if (sounds.isEmpty()) {
            sounds = List.of("minecraft:entity.fishing_bobber.splash");
         }

         String source = String.join("|", sounds);
         if (source.equals(this.init_Str)) {
            return this.init_Set;
         }

         Set<String> normalized = new LinkedHashSet<>();

         for (String entry : sounds) {
            if (entry != null) {
               String value = entry.trim().toLowerCase(Locale.ROOT);
               if (!value.isEmpty()) {
                  normalized.add(value);
                  int split = value.indexOf(58);
                  if (split >= 0 && split + 1 < value.length()) {
                     normalized.add(value.substring(split + 1));
                  }
               }
            }
         }

         this.init_Str = source;
         this.init_Set = Set.copyOf(normalized);
         return this.init_Set;
      }

      private boolean init_Z_38() {
         InteractionHand hand = this.init_InteractionHand_3();
         return hand == null ? false : this.init_Z_ItemStack_7(MC.player.getItemInHand(hand));
      }

      private int init_I_25() {
         if ("Sound".equals(this.choice("trigger-mode"))) {
            return -1;
         }

         FishingHook hook = this.init_FishingHook();
         return hook == null ? -1 : hook.getId();
      }

      private InteractionHand init_InteractionHand_3() {
         if (MC.player == null) {
            return null;
         } else if (MC.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem) {
            return InteractionHand.MAIN_HAND;
         } else if (autismclient.util.ah.load_Z(this.id())) {
            return null;
         } else {
            return MC.player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof FishingRodItem ? InteractionHand.OFF_HAND : null;
         }
      }

      private boolean init_Z_InteractionHand(InteractionHand hand) {
         return MC.player != null && hand != null && MC.player.getItemInHand(hand).getItem() instanceof FishingRodItem;
      }

      private boolean load_Z_17() {
         return this.isEnabled()
            && !aD.init_Z()
            && MC != null
            && MC.player != null
            && MC.level != null
            && MC.level == this.init_Obj
            && MC.gameMode != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !MC.player.isDeadOrDying()
            && !MC.player.isUsingItem()
            && !MC.player.isHandsBusy()
            && !HashUtil.load_Z()
            && !AutoTotemModule.init_Z_6()
            && (autismclient.util.aD.init_Str() == null || !autismclient.util.aD.read_Z())
            && autismclient.util.ah.read_Z(this.id())
            && this.init_Z_38();
      }

      private boolean read_Z_16() {
         return this.init_BitSetUtil_CachePo != null
            && this.load_Z_17()
            && this.init_BitSetUtil.init_Z_BitSetUtil_CachePo_I(this.init_BitSetUtil_CachePo, this.init_I_25())
            && this.init_InteractionHand_3() == this.init_InteractionHand
            && (this.init_InteractionHand != InteractionHand.MAIN_HAND || MC.player.getInventory().getSelectedSlot() == this.init_I)
            && ItemStack.isSameItemSameComponents(this.init_ItemStack, MC.player.getItemInHand(this.init_InteractionHand));
      }

      private boolean init_Z_BitSetUtil_CachePo(BitSetUtil.Inner_CachePool use) {
         if (!this.load_Z_17()) {
            return false;
         }

         this.init_BitSetUtil_CachePo = use;
         this.init_InteractionHand = this.init_InteractionHand_3();
         this.init_I = MC.player.getInventory().getSelectedSlot();
         this.init_ItemStack = MC.player.getItemInHand(this.init_InteractionHand).copy();
         if (!this.read_Z_16()) {
            return false;
         }

         if (!"Interaction API".equals(this.choice("click-mode"))) {
            return autismclient.util.as.init(this.id(), this::read_Z_16, sent -> this.init(use, sent));
         }

         if (!al.init_Z_HitResult(MC.hitResult, this.init_InteractionHand, this.id()) && autismclient.util.ah.write_Z(this.id())) {
            this.init_Z = true;
            this.load_Z = false;

            boolean dispatched;
            try {
               InteractionResult result = MC.gameMode.useItem(MC.player, this.init_InteractionHand);
               dispatched = result != null && result.consumesAction() && this.load_Z;
               if (dispatched) {
                  Compat263.swing(MC.player, this.init_InteractionHand);
               }
            } finally {
               this.init_Z = false;
               autismclient.util.ah.read_V(this.id());
            }

            this.init(use, dispatched);
            return true;
         } else {
            return false;
         }
      }

      private void init(BitSetUtil.Inner_CachePool use, boolean dispatched) {
         this.init_BitSetUtil.init(use, dispatched, this.init_I_25());
         if (!dispatched && !this.load_Z_17()) {
            this.init_BitSetUtil.load_V();
         }

         if (use.equals(this.init_BitSetUtil_CachePo)) {
            this.init_BitSetUtil_CachePo = null;
            this.init_InteractionHand = null;
            this.init_ItemStack = ItemStack.EMPTY;
            this.init_I = -1;
         }
      }

      private int load_I_13() {
         return 8;
      }

      private int read_I_7() {
         return Math.max(0, this.integer("cast-timeout")) * 20;
      }

      private void load_V_25() {
         if (MC != null && MC.player != null && MC.level != null) {
            this.read_V_17();
            if ("Sound".equals(this.choice("trigger-mode"))) {
               this.init_BitSetUtil.init_V();
            } else {
               this.init_BitSetUtil.init_V(this.init_I_25());
            }

            this.sync_I = Math.max(this.sync_I, 3);
         }
      }

      private void init_V_I_10(int slot) {
         this.read_V_17();
         this.sync_I = Math.max(this.sync_I, 8);
      }

      private void read_V_17() {
         autismclient.util.as.load(this.id());
         this.init_BitSetUtil.load_V();
         this.init_BitSetUtil_CachePo = null;
         this.init_InteractionHand = null;
         this.init_ItemStack = ItemStack.EMPTY;
         this.init_I = -1;
         this.init_V_Z_9(true);
      }

      private void write() {
         this.flush = true;
         this.read_V_17();
      }

      private void update() {
         this.flush = false;
         this.sync_I = Math.max(this.sync_I, 4);
      }

      private FishingHook init_FishingHook() {
         if (MC != null && MC.player != null && MC.level != null) {
            FishingHook playerHook = MC.player.fishing;
            if (this.init_Z_FishingHook(playerHook)) {
               return playerHook;
            }

            FishingHook fallback = null;

            for (Entity entity : MC.level.entitiesForRendering()) {
               if (entity instanceof FishingHook hook
                  && this.init_Z_FishingHook(hook)
                  && (fallback == null || hook.distanceToSqr(MC.player) < fallback.distanceToSqr(MC.player))) {
                  fallback = hook;
               }
            }

            return fallback;
         } else {
            return null;
         }
      }

      private boolean init_Z_FishingHook(FishingHook hook) {
         if (hook != null && !hook.isRemoved() && MC.player != null) {
            if (hook.distanceToSqr(MC.player) > 1024.0) {
               return false;
            }

            Entity owner = hook.getOwner();
            return owner == MC.player || owner != null && owner.getUUID().equals(MC.player.getUUID());
         } else {
            return false;
         }
      }

      private void sync() {
         int steps = this.integer("movement-steps");
         if (steps > 0 && MC.player != null && autismclient.util.ba.read()) {
            if (!this.write_Z) {
               this.init_F = MC.player.getYRot();
               this.write_Z = true;
            }

            if (this.write_I == 0) {
               this.write_I = 1;
            }

            this.read_I = this.read_I + this.write_I;
            if (this.read_I >= steps) {
               this.read_I = steps;
               this.write_I = -1;
            } else if (this.read_I <= -steps) {
               this.read_I = -steps;
               this.write_I = 1;
            }

            if (this.init_cM == null) {
               this.init_cM = new cM(autismclient.util.am.init_am().init_am_Random(this.init_Random), this.init_Random);
            }

            autismclient.util.ba.init_V(autismclient.util.ba.Inner_StreamUtil.read);
            this.init_cM
               .init_V_F(
                  MC.player.getYRot(),
                  MC.player.getXRot(),
                  this.init_F + (float)(this.read_I * this.decimal("degrees-per-step")),
                  this.integer("movement-duration")
               );
            this.read_Z = this.init_cM.init_Z();
            this.load_I = 0;
            autismclient.util.ba.init(this.read_Z);
         }
      }

      private void flush() {
         if (this.read_Z && MC.player != null) {
            if (autismclient.util.ba.read() && !(this.load_I > Math.ceil(this.init_cM.init_D()) + 20.0)) {
               this.init_V_D(this.load_I);
               if (this.read_Z) {
                  this.load_I++;
               }
            } else {
               this.init_V_Z_9(false);
            }
         }
      }

      private void init_V_D(double elapsedTicks) {
         if (this.read_Z && this.init_cM != null && MC.player != null) {
            cM.Inner_DataUtil step = this.init_cM.init_cM_DataUtil_D(elapsedTicks, MC.player.getYRot(), MC.player.getXRot(), ce.load());
            if (step.init_Z()) {
               this.init_V_Z_9(false);
            } else {
               autismclient.util.ba.init_V_ba_StreamUtil_3(autismclient.util.ba.Inner_StreamUtil.read, step.init_J(), step.load());
            }
         }
      }

      private void init_V_Z_9(boolean newAnchor) {
         this.read_Z = false;
         this.load_I = 0;
         if (this.init_cM != null) {
            this.init_cM.init_V();
         }

         autismclient.util.ba.init_V(autismclient.util.ba.Inner_StreamUtil.read);
         if (newAnchor) {
            this.write_Z = false;
            this.read_I = 0;
            this.write_I = 1;
         }
      }

      private void close() {
         this.read_V_17();
         this.init_BitSetUtil.read();
         this.sync_I = 0;
         this.flush = false;
         this.init_Z = false;
         this.load_Z = false;
      }

      private void open() {
         this.close();
         this.init_Obj = null;
         this.write_Z = false;
         this.init_F = 0.0F;
         this.read_I = 0;
         this.write_I = 1;
         this.update_I = 0;
      }

      private void poll() {
         this.init("catch-min", "catch-max");
         this.init("recast-min", "recast-max");
      }

      private void init_V_Str_5(String optionId) {
         if ("catch-min".equals(optionId) || "catch-max".equals(optionId)) {
            this.init_V("catch-min", "catch-max", optionId);
         } else if ("recast-min".equals(optionId) || "recast-max".equals(optionId)) {
            this.init_V("recast-min", "recast-max", optionId);
         }
      }

      private void init(String minId, String maxId) {
         this.init_V(minId, maxId, "");
      }

      private void init_V(String minId, String maxId, String editedId) {
         int min = this.integer(minId);
         int max = this.integer(maxId);
         if (min > max) {
            if (minId.equals(editedId)) {
               this.setValue(maxId, Integer.toString(min));
            } else {
               this.setValue(minId, Integer.toString(max));
            }
         }
      }

      private f.Inner_BufferHelper.Inner_BufferHelper_2 init_f_BufferHelper_Buf() {
         if (MC.player == null) {
            return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
         }

         if (HashUtil.load_Z()) {
            return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
         }

         f.Inner_BufferHelper.Inner_CachePool best = this.init_f_BufferHelper_Cac_Z(true);
         if (best == null) {
            return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
         }

         if (this.bool("auto-select-rod")) {
            f.Inner_BufferHelper.Inner_CachePool selected = this.init_f_BufferHelper_Cac();
            if (selected == null || !selected.init_Z() || best.load() > selected.load()) {
               if (best.init_I() >= 0 && best.init_I() < 9 && !autismclient.util.ah.init(best.init_I(), this.id())) {
                  if (!autismclient.util.ah.write_Z(this.id())) {
                     return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
                  }

                  try {
                     autismclient.util.aw.init_V(MC, best.init_I());
                     this.update_I = 1;
                  } finally {
                     autismclient.util.ah.read_V(this.id());
                  }

                  return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
               }

               if (best.init_I() >= 9 && best.init_I() < 36 && !autismclient.util.ah.init(best.init_I(), this.id())) {
                  int target = MC.player.getInventory().getSelectedSlot();
                  if (!autismclient.util.ah.init(target, this.id())) {
                     if (!autismclient.util.ah.write_Z(this.id())) {
                        return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
                     }

                     try {
                        if (autismclient.util.aw.read(MC, best.init_I(), target)) {
                           autismclient.util.aw.init_V(MC, target);
                           this.update_I = 1;
                           return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
                        }
                     } finally {
                        autismclient.util.ah.read_V(this.id());
                     }
                  }
               }
            }
         }

         if (this.update_I > 0) {
            this.update_I--;
            return f.Inner_BufferHelper.Inner_BufferHelper_2.load();
         } else {
            return this.init_Z_38() ? f.Inner_BufferHelper.Inner_BufferHelper_2.init_f_BufferHelper_Buf() : f.Inner_BufferHelper.Inner_BufferHelper_2.load();
         }
      }

      private f.Inner_BufferHelper.Inner_CachePool init_f_BufferHelper_Cac() {
         if (MC.player == null) {
            return null;
         }

         int slot = MC.player.getInventory().getSelectedSlot();
         return this.init(slot, MC.player.getInventory().getItem(slot), false);
      }

      private f.Inner_BufferHelper.Inner_CachePool init_f_BufferHelper_Cac_Z(boolean safeOnly) {
         if (MC.player == null) {
            return null;
         }

         f.Inner_BufferHelper.Inner_CachePool best = null;
         int size = MC.player.getInventory().getContainerSize();

         for (int slot = 0; slot < Math.min(36, size); slot++) {
            f.Inner_BufferHelper.Inner_CachePool choice = this.init(slot, MC.player.getInventory().getItem(slot), safeOnly);
            if (choice != null && (best == null || choice.load() > best.load())) {
               best = choice;
            }
         }

         f.Inner_BufferHelper.Inner_CachePool offhand = this.init(40, MC.player.getOffhandItem(), safeOnly);
         if (offhand != null && (best == null || offhand.load() > best.load())) {
            best = offhand;
         }

         return best;
      }

      private f.Inner_BufferHelper.Inner_CachePool init(int slot, ItemStack stack, boolean safeOnly) {
         if (stack != null && !stack.isEmpty() && stack.getItem() instanceof FishingRodItem) {
            boolean safe = this.init_Z_ItemStack_7(stack);
            if (safeOnly && !safe) {
               return null;
            }

            int durability = stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 999;
            int score = this.init(stack, Enchantments.LUCK_OF_THE_SEA) * 900
               + this.init(stack, Enchantments.LURE) * 900
               + this.init(stack, Enchantments.UNBREAKING) * 200
               + this.init(stack, Enchantments.MENDING) * 100
               + this.init_I_ItemStack_3(stack) * 50
               + Math.min(49, durability);
            return new f.Inner_BufferHelper.Inner_CachePool(slot, score, safe);
         } else {
            return null;
         }
      }

      private boolean init_Z_ItemStack_7(ItemStack stack) {
         if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof FishingRodItem)) {
            return false;
         } else if (this.bool("anti-break") && stack.isDamageableItem()) {
            int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
            return remaining > Math.max(1, this.integer("min-durability"));
         } else {
            return true;
         }
      }

      private int init(ItemStack stack, ResourceKey<Enchantment> key) {
         try {
            if (MC != null && MC.level != null && stack != null && !stack.isEmpty()) {
               Reference<Enchantment> holder = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
               return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
            } else {
               return 0;
            }
         } catch (Throwable ignored) {
            return 0;
         }
      }

      private int init_I_ItemStack_3(ItemStack stack) {
         try {
            return EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) ? 0 : 1;
         } catch (Throwable ignored) {
            return 1;
         }
      }

      private void push() {
         if (!this.bool("stop-macro-enabled") && this.value("stop-macro").trim().isEmpty()) {
            if (Boolean.parseBoolean(this.value("stop-inv-slots"))) {
               autismclient.util.aM macro = cL.init(cL.Inner_DataUtil.init);
               if (macro != null) {
                  this.init(macro, cL.Inner_DataUtil.init);
                  this.setValue("stop-macro-enabled", "true");
                  this.setValue("stop-macro", macro.init_Str);
               }
            } else if (Boolean.parseBoolean(this.value("stop-out-of-rods"))) {
               autismclient.util.aM macro = cL.init(cL.Inner_DataUtil.load);
               if (macro != null) {
                  this.init(macro, cL.Inner_DataUtil.load);
                  this.setValue("stop-macro-enabled", "true");
                  this.setValue("stop-macro", macro.init_Str);
               }
            }
         }
      }

      private void init(autismclient.util.aM macro, cL.Inner_DataUtil preset) {
         if (macro != null && macro.init_List != null && !macro.init_List.isEmpty() && preset != null) {
            if (preset == cL.Inner_DataUtil.init && macro.init_List.get(0) instanceof autismclient.util.macro.aH wait) {
               int slots = AuditClientFixes.autoFishMinFreeSlots(this, wait.slots);
               boolean changed = wait.countMode != autismclient.util.macro.aH.Inner_StreamUtil.init
                  || wait.comparison != autismclient.util.macro.aH.Inner_DataUtil.load
                  || wait.slots != slots;
               wait.countMode = autismclient.util.macro.aH.Inner_StreamUtil.init;
               wait.comparison = autismclient.util.macro.aH.Inner_DataUtil.load;
               wait.slots = slots;
               if (changed) {
                  autismclient.util.aN.init_aN().read_V();
               }
            } else if (preset == cL.Inner_DataUtil.load && macro.init_List.get(0) instanceof autismclient.util.macro.ar wait) {
               int value = Math.max(1, this.integer("min-durability"));
               boolean changed = wait.targetMode != autismclient.util.macro.ar.Inner_CachePool.load
                  || !"minecraft:fishing_rod".equals(wait.itemName)
                  || wait.measurement != autismclient.util.macro.ar.Inner_StreamUtil.init
                  || wait.comparison != autismclient.util.macro.ar.Inner_DataUtil.load
                  || wait.value != value
                  || !wait.useNext;
               wait.targetMode = autismclient.util.macro.ar.Inner_CachePool.load;
               wait.itemName = "minecraft:fishing_rod";
               wait.measurement = autismclient.util.macro.ar.Inner_StreamUtil.init;
               wait.comparison = autismclient.util.macro.ar.Inner_DataUtil.load;
               wait.value = value;
               wait.useNext = true;
               if (changed) {
                  autismclient.util.aN.init_aN().read_V();
               }
            }
         }
      }

      private void pop() {
         if (this.bool("stop-macro-enabled") && !this.update_Z) {
            this.update_Z = true;
            this.run();
            String macroName = this.value("stop-macro").trim();
            if (macroName.isBlank()) {
               this.load("AutoFish auto stop rule missing.");
            } else {
               autismclient.util.aM macro = autismclient.util.aN.init_aN().init_aM_Str(macroName);
               if (macro == null) {
                  this.load("AutoFish auto stop rule not found: " + macroName);
               } else if (!cL.init(macro)) {
                  this.load("AutoFish auto stop rule must start with a conditional.");
               } else {
                  autismclient.util.aM runtimeMacro = this.init_aM_aM(macro);
                  if (runtimeMacro != null && runtimeMacro.init_List != null && !runtimeMacro.init_List.isEmpty()) {
                     runtimeMacro.load_V();
                     this.init_J = MacroExecutor.init_J_aM_MacroExecutor_Pool(runtimeMacro, MacroExecutor.Inner_PoolEntry.init_MacroExecutor_Pool());
                     if (this.init_J < 0L) {
                        this.load("AutoFish auto stop could not start.");
                     }
                  } else {
                     this.load("AutoFish auto stop could not build a valid rule.");
                  }
               }
            }
         }
      }

      private void run() {
         String macroName = this.value("stop-macro").trim();
         cL.Inner_DataUtil preset = cL.init_cL_DataUtil(macroName);
         if (preset != null) {
            autismclient.util.aM macro = autismclient.util.aN.init_aN().init_aM_Str(macroName);
            if (macro != null) {
               this.init(macro, preset);
            }
         }
      }

      private autismclient.util.aM init_aM_aM(autismclient.util.aM source) {
         if (source != null && source.init_List != null) {
            autismclient.util.aM runtime = source.init_aM_Str(source.init_Str);
            runtime.load_I = -1;
            runtime.init_Z = false;
            runtime.init_I = -1;
            int conditionIndex = this.init_I_List_2(runtime.init_List);
            if (conditionIndex < 0) {
               return null;
            }

            f.Inner_BufferHelper.Inner_StreamUtil trigger = f.Inner_BufferHelper.Inner_StreamUtil.init(this.choice("stop-macro-trigger"));
            boolean injectStop = trigger != f.Inner_BufferHelper.Inner_StreamUtil.load;
            boolean skipImmediateDuplicateDisable = injectStop
               && conditionIndex + 1 < runtime.init_List.size()
               && cL.load(runtime.init_List.get(conditionIndex + 1));
            boolean injectResume = trigger == f.Inner_BufferHelper.Inner_StreamUtil.read
               && !this.init(runtime.init_List, conditionIndex, skipImmediateDuplicateDisable);
            ArrayList<MacroAction> rebuilt = new ArrayList<>();

            for (int i = 0; i < runtime.init_List.size(); i++) {
               MacroAction action = runtime.init_List.get(i);
               if (i == conditionIndex) {
                  rebuilt.add(action);
                  if (injectStop) {
                     rebuilt.add(new f.Inner_BufferHelper.Inner_DataUtil(this, false));
                  }
               } else if (!skipImmediateDuplicateDisable || i != conditionIndex + 1) {
                  rebuilt.add(action);
               }
            }

            if (injectResume) {
               rebuilt.add(new f.Inner_BufferHelper.Inner_DataUtil(this, true));
            }

            runtime.init_List = rebuilt;
            return runtime;
         } else {
            return null;
         }
      }

      private int init_I_List_2(List<MacroAction> actions) {
         if (actions == null) {
            return -1;
         }

         for (int i = 0; i < actions.size(); i++) {
            MacroAction action = actions.get(i);
            if (action != null && action.isEnabled()) {
               return autismclient.util.macro.c.init(action) ? i : -1;
            }
         }

         return -1;
      }

      private boolean init(List<MacroAction> actions, int conditionIndex, boolean ignoreImmediateDuplicateDisable) {
         if (actions == null) {
            return false;
         }

         for (int i = Math.max(0, conditionIndex + 1); i < actions.size(); i++) {
            if (!ignoreImmediateDuplicateDisable || i != conditionIndex + 1) {
               MacroAction action = actions.get(i);
               if (action != null && action.isEnabled() && cL.init(action)) {
                  return true;
               }
            }
         }

         return false;
      }

      private void load(String message) {
         if (!this.sync_Z) {
            this.sync_Z = true;
            autismclient.util.bi.read(message);
         }
      }

      private void get() {
         this.update_Z = false;
         this.sync_Z = false;
         this.init_J = -1L;
      }

      private void set() {
         long runId = this.init_J;
         if (runId >= 0L && MacroExecutor.init_Z_J(runId) && MacroExecutor.load_J() != runId) {
            MacroExecutor.init_V_J(runId);
         }

         this.get();
      }

      private void put() {
         long runId = this.init_J;
         if (runId >= 0L && MacroExecutor.init_Z_J(runId)) {
            MacroExecutor.init_V_J(runId);
         }

         this.get();
      }

      private record Inner_BufferHelper_2(boolean init) {
         static f.Inner_BufferHelper.Inner_BufferHelper_2 init_f_BufferHelper_Buf() {
            return new f.Inner_BufferHelper.Inner_BufferHelper_2(true);
         }

         static f.Inner_BufferHelper.Inner_BufferHelper_2 load() {
            return new f.Inner_BufferHelper.Inner_BufferHelper_2(false);
         }

         public boolean init_Z() {
            return this.init;
         }
      }

      private record Inner_CachePool(int init_I, int load, boolean init_Z) {
      }

      private static final class Inner_DataUtil implements MacroAction {
         private final f.Inner_BufferHelper init_f_BufferHelper;
         private final boolean init_Z;

         private Inner_DataUtil(f.Inner_BufferHelper owner, boolean enable) {
            this.init_f_BufferHelper = owner;
            this.init_Z = enable;
         }

         @Override
         public void execute(Minecraft mc) {
            if (this.init_f_BufferHelper != null) {
               this.init_f_BufferHelper.setEnabledSilently(this.init_Z);
            }
         }

         @Override
         public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", autismclient.util.macro.StreamGate.entry.name());
            return tag;
         }

         @Override
         public void fromTag(CompoundTag tag) {
         }

         @Override
         public autismclient.util.macro.StreamGate getType() {
            return autismclient.util.macro.StreamGate.entry;
         }

         @Override
         public String getDisplayName() {
            return this.init_Z ? "Resume AutoFish" : "Stop AutoFish";
         }

         @Override
         public String getIcon() {
            return "M";
         }
      }

      private enum Inner_StreamUtil {
         init,
         load,
         read;

         static f.Inner_BufferHelper.Inner_StreamUtil init(String value) {
            String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
            if ("run steps only".equals(normalized)) {
               return load;
            } else {
               return "stop, run, resume".equals(normalized) ? read : init;
            }
         }
      }
   }

   static final class Inner_ByteReader extends Module {
      Inner_ByteReader() {
         super("freecam", "Freecam", ae.update, "Move the camera away from the player.");
         this.add(new DoubleSetting("speed", "Speed", 1.0, 0.05, 30.0, 0.05).description("Horizontal camera speed."));
         this.add(new DoubleSetting("vertical-speed", "Vertical Speed", 1.0, 0.05, 30.0, 0.05).description("Vertical camera speed."));
         this.add(new BoolSetting("reload-chunks", "Reload Chunks", true).description("Refresh chunk culling."));
         this.add(new BoolSetting("interact", "Interact", true).description("Interact from freecam view"));
      }

      @Override
      public void onEnable() {
         aC.load_V_Z(this.bool("reload-chunks"));
         aC.init_V_Z(this.bool("interact"));
      }

      @Override
      public void onDisable() {
         aC.init_V();
      }

      @Override
      public void onOptionValueChanged(String settingId) {
         if ("interact".equals(settingId)) {
            aC.init_V_Z(this.bool("interact"));
         }
      }

      @Override
      public void onGameLeft() {
         this.setEnabledSilently(false);
      }

      @Override
      public void tick() {
         if (MC.player != null && MC.player.isDeadOrDying()) {
            this.disableWithToggleMessage("Freecam disabled: you died.");
         }
      }

      @Override
      public void preMovementTick() {
         aC.init(this.decimal("speed"), this.decimal("vertical-speed"));
      }

      @Override
      public Vec3 onPlayerMove(MoverType type, Vec3 movement) {
         return aC.init(type, movement);
      }

      @Override
      public String info() {
         return String.format(Locale.ROOT, "%.2f", this.decimal("speed"));
      }
   }

   static final class Inner_CachePool extends Module {
      private static final ValueRange init_ValueRange = new ValueRange(25, 50);
      private static final double init_D = 250.0;
      private final h init_h;
      private final autismclient.util.a.DataUtil init_DataUtil = new autismclient.util.a.DataUtil();
      private final autismclient.util.a.StreamUtil init_StreamUtil = autismclient.util.a.StreamUtil.init_StreamUtil();
      private final autismclient.util.a.StreamUtil load_StreamUtil = autismclient.util.a.StreamUtil.load_StreamUtil();
      private final Random init_Random = new Random();
      private boolean init_Z;
      private long init_J = -9223372036854775808L;
      private long load_J;
      private boolean load_Z;
      private final O init_O;

      Inner_CachePool() {
         super("auto-clicker", "AutoClicker", ae.init_ae, "Auto clicks or holds");
         this.add(new ChoiceSetting("mode", "Mode", "Hold Down", "Constant", "Hold Down", "Hold").description("Click or hold").build());
         this.add(new ChoiceSetting("button", "Button", "Left", "Left", "Right").description("Mouse button").build());
         this.init_O = new O(this);
         this.add(new BoolSetting("break-blocks", "Break Blocks", true));
         this.add(new RangeSetting("break-delay", "Break Delay", init_ValueRange, 0.0, 250.0, 5.0).unit("ms").visibleWhen(() -> this.bool("break-blocks")));
         this.init_h = new h(this, "General", h.init_BufferHelper, () -> !"Hold".equals(this.value("mode")), h.init_ValueRange);
         this.init_h.init_V();
      }

      @Override
      public void onEnable() {
         this.init_Z = false;
      }

      @Override
      public void onDisable() {
         this.load_V_26();
      }

      @Override
      public void onGameLeft() {
         this.load_V_26();
      }

      @Override
      protected String externalSettingValue(String settingId) {
         return this.init_h == null ? null : this.init_h.init_Str_Str(settingId);
      }

      @Override
      protected boolean setExternalSettingValue(String settingId, String value) {
         return this.init_h != null && this.init_h.init(settingId, value);
      }

      @Override
      public void tick() {
         if (!this.load_Z_18() && "Hold".equals(this.choice("mode"))) {
            this.read();
         } else {
            this.write();
         }
      }

      @Override
      public void onInputFrame() {
         long now = System.nanoTime();
         boolean right = "Right".equals(this.choice("button"));
         autismclient.util.a.StreamUtil output = right ? this.load_StreamUtil : this.init_StreamUtil;
         autismclient.util.a.StreamUtil other = right ? this.init_StreamUtil : this.load_StreamUtil;
         output.init_V();
         other.init_V();
         if (other.init_Z()) {
            other.load_V();
         }

         if (!this.load_Z_18() && !"Hold".equals(this.choice("mode"))) {
            boolean holdDown = "Hold Down".equals(this.choice("mode"));
            boolean physical = output.load_Z();
            autismclient.util.a.CachePool profile = this.init_h.init_CachePool();
            this.init_DataUtil.init_V_DataUtil_ConfigSto(this.init_h.init_DataUtil_ConfigSto());
            output.init_V_Z(holdDown && physical);
            if (physical && !this.init_Z) {
               if (holdDown) {
                  this.init_DataUtil.init_V_J_2(now, profile, output);
               } else {
                  this.init_DataUtil.init_V_J(now);
               }
            }

            this.init_Z = physical;
            if (!this.init(now, right, output)) {
               this.init_DataUtil.init(now, !holdDown || physical, profile, output);
            }
         } else {
            output.init_V_Z(false);
            this.init_DataUtil.init_V_J_DataUtil_NetHelper(now, output);
            this.init_Z = false;
            this.init_V_36();
         }
      }

      private boolean init(long now, boolean right, autismclient.util.a.StreamUtil output) {
         boolean working = !right && this.bool("break-blocks") && this.init_Z_39();
         if (!working) {
            this.init_V_36();
            return false;
         }

         if (this.init_J == -9223372036854775808L) {
            this.init_J = now;
            ValueRange band = ValueRange.parse(this.value("break-delay"), init_ValueRange);
            this.load_J = (long)(band.random(this.init_Random) * 1000000.0);
         }

         if (now - this.init_J < this.load_J) {
            return false;
         }

         output.load_V();
         output.init_V_Z(false);
         this.load_Z = true;
         autismclient.util.as.init_V_Z(true);
         return true;
      }

      private boolean init_Z_39() {
         if (MC != null && MC.player != null && MC.options != null && MC.options.keyAttack.isDown()) {
            return MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor && accessor.autism$isDestroying()
               ? true
               : MC.hitResult != null && MC.hitResult.getType() == Type.BLOCK;
         } else {
            return false;
         }
      }

      private void init_V_36() {
         this.init_J = -9223372036854775808L;
         if (this.load_Z) {
            this.load_Z = false;
            autismclient.util.as.init_V_Z(false);
         }
      }

      private boolean load_Z_18() {
         return MC == null
            || MC.player == null
            || MC.level == null
            || MC.gui.screen() != null
            || ScaffoldModule.read_Z_12()
            || ScaffoldModule.update_Z_5()
            || cn.init_ce_DataUtil() != null
            || ScaffoldModule.flush_Z_5()
            || AutoTotemModule.init_Z_6()
            || !this.init_O.init_Z_ItemStack(MC.player.getMainHandItem());
      }

      private void load_V_26() {
         this.init_StreamUtil.load_V();
         this.load_StreamUtil.load_V();
         this.init_DataUtil.init_V_J_DataUtil_NetHelper(System.nanoTime(), "Right".equals(this.choice("button")) ? this.load_StreamUtil : this.init_StreamUtil);
         this.init_Z = false;
         this.init_V_36();
         this.write();
      }

      private void read() {
         boolean right = "Right".equals(this.choice("button"));
         autismclient.util.as.init_V_Z(!right);
         autismclient.util.as.load(right);
      }

      private void write() {
         autismclient.util.as.init_V_Z(false);
         autismclient.util.as.load(false);
      }
   }

   static final class Inner_CharEncoder extends Module {
      Inner_CharEncoder() {
         super("no-interact", "NoInteract", ae.read, "Cancels selected inputs.");
         this.add(RegistryListSetting.blocks("block-mine", "Block Mine", "").group("Blocks").description("Mining block list.").build());
         this.add(new ChoiceSetting("block-mine-mode", "Mine Mode", "BlackList", "WhiteList", "BlackList").group("Blocks").build());
         this.add(RegistryListSetting.blocks("block-interact", "Block Interact", "").group("Blocks").description("Use block list.").build());
         this.add(new ChoiceSetting("block-interact-mode", "Block Mode", "BlackList", "WhiteList", "BlackList").group("Blocks").build());
         this.add(new ChoiceSetting("block-interact-hand", "Block Hand", "Both", "Mainhand", "Offhand", "Both", "None").group("Blocks").build());
         this.add(RegistryListSetting.entityTypes("entity-interact", "Entity Interact", "").group("Entities").description("Use entity list.").build());
         this.add(new ChoiceSetting("entity-interact-mode", "Entity Mode", "BlackList", "WhiteList", "BlackList").group("Entities").build());
         this.add(new ChoiceSetting("entity-interact-hand", "Entity Hand", "Both", "Mainhand", "Offhand", "Both", "None").group("Entities").build());
         this.add(RegistryListSetting.entityTypes("entity-hit", "Entity Hit", "").group("Attacks").build());
         this.add(new ChoiceSetting("entity-hit-mode", "Hit Mode", "WhiteList", "WhiteList", "BlackList").group("Attacks").build());
         this.add(new BoolSetting("friends", "Friends", false).group("Entities"));
         this.add(new BoolSetting("babies", "Babies", false).group("Entities"));
         this.add(new BoolSetting("nametagged", "Nametagged", false).group("Entities"));
      }

      @Override
      public boolean shouldCancelStartBreakingBlock(BlockPos pos, Direction direction) {
         if (MC.level != null && pos != null) {
            String id = BuiltInRegistries.BLOCK.getKey(MC.level.getBlockState(pos).getBlock()).toString();
            return this.load(id, this.list("block-mine"), this.choice("block-mine-mode"));
         } else {
            return this.load("", this.list("block-mine"), this.choice("block-mine-mode"));
         }
      }

      @Override
      public boolean shouldCancelAttack(HitResult hitResult) {
         if (hitResult instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            return !this.init(entity)
               ? false
               : this.load(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), this.list("entity-hit"), this.choice("entity-hit-mode"));
         } else {
            return false;
         }
      }

      @Override
      public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
         if (hitResult instanceof BlockHitResult blockHit) {
            if (!this.init(this.choice("block-interact-hand"), hand)) {
               return false;
            }

            if (MC.level == null) {
               return this.load("", this.list("block-interact"), this.choice("block-interact-mode"));
            }

            String id = BuiltInRegistries.BLOCK.getKey(MC.level.getBlockState(blockHit.getBlockPos()).getBlock()).toString();
            return this.load(id, this.list("block-interact"), this.choice("block-interact-mode"));
         } else if (hitResult instanceof EntityHitResult entityHit) {
            if (!this.init(this.choice("entity-interact-hand"), hand)) {
               return false;
            }

            if (!this.init(entityHit.getEntity())) {
               return false;
            }

            String id = BuiltInRegistries.ENTITY_TYPE.getKey(entityHit.getEntity().getType()).toString();
            return this.load(id, this.list("entity-interact"), this.choice("entity-interact-mode"));
         } else {
            return false;
         }
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (packet instanceof ServerboundUseItemOnPacket use) {
            if (!this.init(this.choice("block-interact-hand"), use.hand())) {
               return false;
            }

            if (MC.level == null) {
               return this.load("", this.list("block-interact"), this.choice("block-interact-mode"));
            }

            String id = BuiltInRegistries.BLOCK.getKey(MC.level.getBlockState(use.hitResult().getBlockPos()).getBlock()).toString();
            return this.load(id, this.list("block-interact"), this.choice("block-interact-mode"));
         } else if (packet instanceof ServerboundPlayerActionPacket action && action.getAction() == Action.START_DESTROY_BLOCK) {
            return this.shouldCancelStartBreakingBlock(action.getPos(), action.getDirection());
         } else if (packet instanceof ServerboundInteractPacket interact) {
            if (!this.init(this.choice("entity-interact-hand"), interact.hand())) {
               return false;
            } else if (MC.level == null) {
               return false;
            } else {
               Entity entity = MC.level.getEntity(interact.entityId());
               if (entity != null && this.init(entity)) {
                  String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                  return this.load(id, this.list("entity-interact"), this.choice("entity-interact-mode"));
               } else {
                  return false;
               }
            }
         } else if (packet instanceof ServerboundAttackPacket attack) {
            if (MC.level == null) {
               return false;
            } else {
               Entity entity = MC.level.getEntity(attack.entityId());
               if (entity != null && this.init(entity)) {
                  String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                  return this.load(id, this.list("entity-hit"), this.choice("entity-hit-mode"));
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }

      private boolean init(String mode, InteractionHand hand) {
         if ("None".equals(mode)) {
            return false;
         } else {
            return "Both".equals(mode)
               ? true
               : "Mainhand".equals(mode) && hand == InteractionHand.MAIN_HAND || "Offhand".equals(mode) && hand == InteractionHand.OFF_HAND;
         }
      }

      private boolean load(String id, List<String> entries, String mode) {
         return init(id, entries, mode);
      }

      static boolean init(String id, List<String> entries, String mode) {
         boolean whitelist = "WhiteList".equalsIgnoreCase(mode);
         if (entries != null && !entries.isEmpty()) {
            boolean listed = false;

            for (String entry : entries) {
               if (di.init(id, entry)) {
                  listed = true;
                  break;
               }
            }

            return whitelist ? !listed : listed;
         } else {
            return false;
         }
      }

      private boolean init(Entity entity) {
         if (entity == null) {
            return false;
         } else {
            return !this.bool("nametagged") && entity.hasCustomName()
               ? false
               : !(!this.bool("babies") && entity instanceof LivingEntity living) || !living.isBaby();
         }
      }
   }

   static final class Inner_ClassCache extends Module {
      Inner_ClassCache() {
         super("tracers", "Tracers", ae.update, "Draws tracer lines to entities.");
         this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").description("Traced entities.").build());
         this.add(new IntSetting("max-distance", "Max Distance", 256, 0, 512, 16));
         this.add(new DoubleSetting("line-width", "Line Width", 2.0, 2.0, 6.0, 0.25).group("General").description("Tracer thickness."));
         this.add(new BoolSetting("height-line", "Height Line", false).group("General").description("Vertical line up the entity."));
         this.add(new BoolSetting("distance-color", "Distance Color", false).group("Colors").description("Color by distance"));
         this.add(
            new IntSetting("color-distance", "Color Distance", 40, 8, 256, 4)
               .group("Colors")
               .description("Far color distance")
               .visibleWhen(() -> this.bool("distance-color"))
         );
         this.add(new ColorSetting("players-color", "Players", -855638017).group("Colors").visibleWhen(() -> !this.bool("distance-color")));
         this.add(new ColorSetting("animals-color", "Animals", -864747633).group("Colors").visibleWhen(() -> !this.bool("distance-color")));
         this.add(new ColorSetting("water-animals-color", "Water Animals", -865674753).group("Colors").visibleWhen(() -> !this.bool("distance-color")));
         this.add(new ColorSetting("monsters-color", "Monsters", -855684534).group("Colors").visibleWhen(() -> !this.bool("distance-color")));
         this.add(new ColorSetting("ambient-color", "Ambient", -860386049).group("Colors").visibleWhen(() -> !this.bool("distance-color")));
         this.add(new ColorSetting("misc-color", "Misc", -858993460).group("Colors").visibleWhen(() -> !this.bool("distance-color")));
      }

      @Override
      public boolean shouldTraceEntity(Entity entity) {
         return am.init_Z(entity);
      }

      @Override
      public int traceColor(Entity entity) {
         return am.load_I(entity);
      }
   }

   static final class Inner_ConfigStore extends Module {
      Inner_ConfigStore() {
         super("auto-reconnect", "AutoReconnect", ae.write, "Reconnects after disconnect.");
         this.add(new DoubleSetting("delay", "Delay Sec", 3.5, 0.0, 60.0, 0.5));
      }
   }

   public static final class Inner_DataUtil extends Module {
      private static final List<String> init_List = List.of(
         "nbt-item-id",
         "nbt-item-count",
         "nbt-item-name",
         "nbt-item-lore",
         "nbt-unbreakable",
         "nbt-glint",
         "nbt-rarity",
         "nbt-max-damage",
         "nbt-damage",
         "nbt-max-stack",
         "nbt-enchants",
         "nbt-attributes",
         "nbt-custom-data",
         "nbt-command"
      );
      private final List<String> load_List = new ArrayList<>();
      private int init_I;
      private int load_I;
      private String init_Str = "fireball-delay";
      private final Random init_Random = new Random();
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private boolean write_Z;
      private int read_I;
      private int write_I;
      private int update_I;
      private long init_J;
      private boolean update_Z;
      private Thread init_Thread;
      private int sync_I;
      private volatile boolean sync_Z;
      private static final String[] init_StrArr = new String[]{
         "password",
         "passwort",
         "password1",
         "passwort1",
         "password123",
         "password1234",
         "passwort123",
         "pass",
         "pw",
         "pw1",
         "pw123",
         "hallo",
         "1122",
         "112233",
         "1234",
         "12345",
         "123456",
         "1234567",
         "12345678",
         "123456789",
         "login",
         "register",
         "test",
         "sicher",
         "me",
         "minecraft",
         "minecraft1",
         "minecraft123",
         "mc",
         "admin",
         "server",
         "tester",
         "account",
         "creeper",
         "gronkh",
         "lol",
         "auth",
         "authme",
         "qwerty",
         "qwertz",
         "2112",
         "1212",
         "cocacola",
         "xavier",
         "dolphin",
         "testing",
         "dragon",
         "baseball",
         "football",
         "letmein",
         "monkey",
         "696969",
         "abc123",
         "mustang",
         "michael",
         "shadow",
         "master",
         "jennifer",
         "111111",
         "2000",
         "jordan",
         "superman",
         "harley",
         "hunter",
         "trustno1",
         "ranger",
         "buster",
         "thomas",
         "tigger",
         "robert",
         "soccer",
         "batman",
         "killer",
         "hockey",
         "george",
         "charlie",
         "andrew",
         "michelle",
         "love",
         "sunshine",
         "jessica",
         "6969",
         "pepper",
         "daniel",
         "access",
         "654321",
         "joshua",
         "maggie",
         "starwars",
         "silver",
         "william",
         "dallas",
         "yankees",
         "123123",
         "ashley",
         "666666",
         "hello",
         "amanda",
         "orange",
         "freedom",
         "computer",
         "thunder",
         "nicole",
         "ginger",
         "heather",
         "hammer",
         "summer",
         "corvette",
         "taylor",
         "austin",
         "1111",
         "merlin",
         "matthew",
         "121212",
         "golfer",
         "cheese",
         "princess",
         "martin",
         "chelsea",
         "patrick",
         "richard",
         "diamond",
         "yellow",
         "bigdog",
         "secret",
         "asdfgh",
         "sparky",
         "cowboy",
         "camaro",
         "anthony",
         "matrix",
         "falcon",
         "iloveyou",
         "bailey",
         "guitar",
         "jackson",
         "purple",
         "scooter",
         "phoenix",
         "aaaaaa",
         "morgan",
         "tigers",
         "porsche",
         "mickey",
         "maverick",
         "cookie",
         "nascar",
         "peanut",
         "justin",
         "131313",
         "money",
         "123456",
         "password",
         "12345678",
         "qwerty",
         "123456789",
         "12345",
         "1234",
         "111111",
         "1234567",
         "admin",
         "1234567890",
         "123123",
         "000000",
         "123321",
         "654321",
         "qwertyuiop",
         "qwertzuiop",
         "1q2w3e4r5t",
         "1qaz2wsx",
         "zxcvbnm",
         "asdfgh",
         "7777777",
         "666666",
         "555555",
         "888888",
         "123qwe",
         "abc123",
         "aa123456",
         "password123",
         "password1",
         "secret"
      };
      private final String[] load_StrArr = Arrays.stream(init_StrArr).distinct().toArray(String[]::new);
      private String[] read_StrArr = this.load_StrArr;

      Inner_DataUtil() {
         super("admin-tools", "AdminTools", ae.write, "Creative/admin helpers.");
         this.add(new ActionSetting("admin-status", "Show Permission Status", this::close).group("Status").build());
         this.add(new ActionSetting("admin-stop-queue", "Stop Queued Commands", this::open).group("Status").build());
         this.add(new IntSetting("fireball-count", "Count", 12, 1, 256, 1).group("Fireball Storm").build());
         this.add(new DoubleSetting("fireball-spread", "Spread", 8.0, 0.0, 90.0, 1.0).group("Fireball Storm").build());
         this.add(new DoubleSetting("fireball-speed", "Speed", 1.8, 0.1, 10.0, 0.1).group("Fireball Storm").build());
         this.add(new IntSetting("fireball-power", "Power", 3, 0, 127, 1).group("Fireball Storm").build());
         this.add(new IntSetting("fireball-delay", "Delay", 1, 0, 20, 1).group("Fireball Storm").build());
         this.add(new DoubleSetting("fireball-distance", "Spawn Dist", 2.0, 0.5, 16.0, 0.5).group("Fireball Storm").build());
         this.add(new ChoiceSetting("fireball-aim", "Aim", "Cone", "Look", "Cone", "Ring").group("Fireball Storm").build());
         this.add(new BoolSetting("fireball-randomize", "Randomize", true).group("Fireball Storm").build());
         this.add(new KeybindSetting("fireball-stream-bind", "Stream Bind", -1).group("Fireball Storm").build());
         this.add(new KeybindSetting("firestorm-bind", "Firestorm Bind", -1).group("Fireball Storm").build());
         this.add(new DoubleSetting("firestorm-distance", "Firestorm Dist", 8.0, 2.0, 48.0, 1.0).group("Fireball Storm").build());
         this.add(new DoubleSetting("firestorm-height", "Firestorm Height", 2.0, 0.0, 32.0, 1.0).group("Fireball Storm").build());
         this.add(new BoolSetting("fireball-stream-active", "Stream Active", false).visibleWhen(() -> false).build());
         this.add(new BoolSetting("firestorm-active", "Firestorm Active", false).visibleWhen(() -> false).build());
         this.add(new ActionSetting("fireball-preset-max", "Preset Max Storm", this::clear).group("Fireball Storm").build());
         this.add(new ActionSetting("fireball-preview", "Preview Command", this::set).group("Fireball Storm").build());
         this.add(new ActionSetting("fireball-copy", "Copy Command", this::put).group("Fireball Storm").build());
         this.add(new ActionSetting("fireball-storm", "Run Storm", this::get).group("Fireball Storm").build());
         this.add(new IntSetting("airstrike-count", "Count", 24, 1, 256, 1).group("Airstrike").build());
         this.add(new DoubleSetting("airstrike-radius", "Radius", 12.0, 0.0, 96.0, 1.0).group("Airstrike").build());
         this.add(new DoubleSetting("airstrike-height", "Height", 48.0, 8.0, 256.0, 4.0).group("Airstrike").build());
         this.add(new DoubleSetting("airstrike-speed", "Fall Speed", 2.0, 0.1, 10.0, 0.1).group("Airstrike").build());
         this.add(new IntSetting("airstrike-power", "Power", 4, 0, 127, 1).group("Airstrike").build());
         this.add(new IntSetting("airstrike-delay", "Delay", 1, 0, 20, 1).group("Airstrike").build());
         this.add(new ChoiceSetting("airstrike-target", "Target", "Look", "Look", "Self").group("Airstrike").build());
         this.add(new ActionSetting("airstrike-preset-small", "Preset Tight Strike", this::reset).group("Airstrike").build());
         this.add(new ActionSetting("airstrike-preset-max", "Preset Max Strike", this::start).group("Airstrike").build());
         this.add(new ActionSetting("airstrike-run", "Run Airstrike", this::add).group("Airstrike").build());
         this.add(new IntSetting("forceop-delay", "Delay (ms)", 500, 0, 5000, 50).group("ForceOP").build());
         this.add(new BoolSetting("forceop-wait", "Wait for Chat", true).group("ForceOP").build());
         this.add(new IntSetting("forceop-min-length", "Min Length", 0, 0, 32, 1).group("ForceOP").build());
         this.add(new IntSetting("forceop-max-length", "Max Length", 0, 0, 32, 1).group("ForceOP").build());
         this.add(new StringSetting("nbt-item-id", "Item ID", "minecraft:stick").group("NBT Item Editor").build());
         this.add(new IntSetting("nbt-item-count", "Count", 1, 1, 99, 1).group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-item-name", "Display Name", "Custom Item").group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-item-lore", "Lore Lines", "").group("NBT Item Editor").build());
         this.add(new BoolSetting("nbt-unbreakable", "Unbreakable", false).group("NBT Item Editor").build());
         this.add(new ChoiceSetting("nbt-glint", "Glint", "Default", "Default", "On", "Off").group("NBT Item Editor").build());
         this.add(new ChoiceSetting("nbt-rarity", "Rarity", "Default", "Default", "Common", "Uncommon", "Rare", "Epic").group("NBT Item Editor").build());
         this.add(new IntSetting("nbt-max-damage", "Max Damage", 0, 0, 100000, 1).group("NBT Item Editor").build());
         this.add(new IntSetting("nbt-damage", "Damage", 0, 0, 100000, 1).group("NBT Item Editor").build());
         this.add(new IntSetting("nbt-max-stack", "Max Stack", 64, 1, 99, 1).group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-enchants", "Enchantments", "").group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-attributes", "Attributes", "").group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-custom-data", "Custom Data", "").group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-command", "Embedded Command", "").group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-item-components", "Raw Components", "").group("NBT Item Editor").build());
         this.add(new StringSetting("nbt-imported-components", "Imported Components", "").visibleWhen(() -> false).build());
         this.add(new StringSetting("nbt-imported-signature", "Imported Signature", "").visibleWhen(() -> false).build());
         this.add(new StringSetting("nbt-item-stack", "Full Item SNBT", "").visibleWhen(() -> false).build());
         this.add(new StringSetting("nbt-item-stack-signature", "Full Item Signature", "").visibleWhen(() -> false).build());
         this.add(new ActionSetting("fill-held-item", "Fill Held", this::next).group("NBT Item Editor").build());
         this.add(new ActionSetting("copy-target-item", "Copy Target", this::prev).group("NBT Item Editor").build());
         this.add(new ActionSetting("apply-held-item", "Apply to Held", this::valid).group("NBT Item Editor").build());
         this.add(new StringSetting("fireballPower", "Fireball Power Legacy", "3").visibleWhen(() -> false).build());
      }

      @Override
      public boolean opensSettingsOnClick() {
         return true;
      }

      @Override
      public boolean hasActivationToggle() {
         return false;
      }

      @Override
      public boolean showInModuleMenu() {
         return false;
      }

      @Override
      public boolean ticksWhenDisabled() {
         return true;
      }

      @Override
      public boolean hasDisabledTickWork() {
         boolean hasFireballBind = this.integer("fireball-stream-bind") != -1 || this.integer("firestorm-bind") != -1;
         return hasFireballBind || this.init_Z || this.load_Z || !this.load_List.isEmpty();
      }

      @Override
      protected String displayValueOverride(Setting<?, ?> option) {
         return option != null && "nbt-item-components".equals(option.id()) ? this.init_Str_11() : null;
      }

      @Override
      public void tick() {
         boolean hasFireballBind = this.integer("fireball-stream-bind") != -1 || this.integer("firestorm-bind") != -1;
         boolean fireballActive = this.init_Z || this.load_Z;
         if (hasFireballBind || fireballActive || !this.load_List.isEmpty()) {
            if (hasFireballBind || fireballActive) {
               this.poll();
            }

            if (fireballActive) {
               this.push();
            }

            if (!this.load_List.isEmpty() && this.init_I-- <= 0) {
               if (!this.init_Z(this.load_List.remove(0), true)) {
                  this.load_List.clear();
                  this.init_I = 0;
                  this.load_I = 0;
               } else {
                  this.init_I = this.integer(this.init_Str);
                  this.load_I++;
               }
            }
         }
      }

      @Override
      public void onGameLeft() {
         this.init_Z = false;
         this.load_Z = false;
         this.setValue("fireball-stream-active", "false");
         this.setValue("firestorm-active", "false");
         this.read_Z = false;
         this.write_Z = false;
         this.load_List.clear();
         this.init_I = 0;
         this.load_I = 0;
      }

      @Override
      public boolean onPacketReceive(Packet<?> packet) {
         if (packet instanceof ClientboundSystemChatPacket chatPacket) {
            String text = chatPacket.content().getString();
            if (text == null) {
               return false;
            }

            String lower = text.toLowerCase(Locale.ROOT);
            if (this.update_Z
               && (
                  lower.contains("wrong")
                     || lower.contains("incorrect")
                     || lower.contains("falsch")
                     || lower.contains("mauvais")
                     || lower.contains("mal")
                     || lower.contains("sbagliato")
               )) {
               this.sync_Z = true;
            }

            return System.currentTimeMillis() > this.init_J && !this.init_Z && !this.load_Z ? false : lower.contains("fireball") || lower.contains("summoned");
         } else {
            return false;
         }
      }

      private void close() {
         AutismClientMessaging.sendPrefixed(
            "Admin Tools: gamemaster=" + this.isAdminContext() + ", creative=" + this.isCreativeContext() + ", queued=" + this.load_List.size() + "."
         );
      }

      private void open() {
         int count = this.load_List.size();
         this.load_List.clear();
         this.init_I = 0;
         this.load_I = 0;
         AutismClientMessaging.sendPrefixed(count == 0 ? "Admin Tools: no queued commands." : "Admin Tools: stopped " + count + " queued commands.");
      }

      private void poll() {
         if (!autismclient.util.at.init()) {
            this.read_Z = false;
         } else {
            boolean streamPressed = this.load_Z("fireball-stream-bind");
            if (streamPressed && !this.read_Z) {
               this.pop();
            }

            this.read_Z = streamPressed;
            boolean stormPressed = this.load_Z("firestorm-bind");
            if (stormPressed && !this.write_Z) {
               this.run();
            }

            this.write_Z = stormPressed;
         }
      }

      private boolean load_Z(String optionId) {
         int bind = this.integer(optionId);
         return bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      }

      private void push() {
         if (MC.player != null && MC.getConnection() != null) {
            if (this.init_Z && this.read_I-- <= 0) {
               this.init(0, 1, false);
               this.read_I = Math.max(0, this.integer("fireball-delay"));
            }

            if (this.load_Z && this.write_I-- <= 0) {
               int count = Math.max(1, this.integer("fireball-count"));
               int burst = Math.max(1, Math.min(8, count));

               for (int i = 0; i < burst; i++) {
                  this.init(this.update_I++, count, true);
               }

               this.write_I = Math.max(0, this.integer("fireball-delay"));
            }
         }
      }

      private void init(int index, int total, boolean firestorm) {
         String command = firestorm ? this.init(index, total, this.init_D_11(), this.load_D_3()) : this.init(index, total);
         if (!command.isBlank()) {
            this.init(command, true, false);
         }
      }

      private void pop() {
         this.init_Z = !this.init_Z;
         this.setValue("fireball-stream-active", Boolean.toString(this.init_Z));
         this.read_I = 0;
         AutismClientMessaging.sendPrefixed("Admin Tools: fireball stream " + (this.init_Z ? "enabled." : "disabled."));
      }

      private void run() {
         this.load_Z = !this.load_Z;
         this.setValue("firestorm-active", Boolean.toString(this.load_Z));
         this.write_I = 0;
         this.update_I = 0;
         AutismClientMessaging.sendPrefixed("Admin Tools: firestorm " + (this.load_Z ? "enabled." : "disabled."));
      }

      public boolean init_Z_40() {
         return this.init_Z;
      }

      public boolean load_Z_19() {
         return this.load_Z;
      }

      private void get() {
         this.load_List.clear();
         this.load_I = 0;
         this.init_Str = "fireball-delay";
         int count = this.integer("fireball-count");

         for (int i = 0; i < count; i++) {
            String command = this.init(i, count, this.init_D_11(), this.load_D_3());
            if (!command.isBlank()) {
               this.load_List.add(command);
            }
         }

         if (this.load_List.isEmpty()) {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not queue fireballs, join a world first.");
         } else {
            this.init_I = 0;
            AutismClientMessaging.sendPrefixed("Admin Tools: queued " + count + " fireballs.");
         }
      }

      private void set() {
         String command = this.init(0, Math.max(1, this.integer("fireball-count")));
         AutismClientMessaging.sendPrefixed(command.isBlank() ? "Admin Tools: join a world first." : "Preview: /" + command);
      }

      private void put() {
         this.init_V(this.init(0, Math.max(1, this.integer("fireball-count"))), "fireball command");
      }

      private void add() {
         this.load_List.clear();
         this.load_I = 0;
         this.init_Str = "airstrike-delay";
         int count = this.integer("airstrike-count");

         for (int i = 0; i < count; i++) {
            String command = this.load(i, count);
            if (!command.isBlank()) {
               this.load_List.add(command);
            }
         }

         if (this.load_List.isEmpty()) {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not queue airstrike, join a world first.");
         } else {
            this.init_I = 0;
            AutismClientMessaging.sendPrefixed("Admin Tools: queued " + count + " airstrike fireballs.");
         }
      }

      public void init_V_37() {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            PointerBuffer filters = stack.mallocPointer(1);
            ByteBuffer txt = MemoryUtil.memASCII("*.txt");
            filters.put(txt).rewind();

            try {
               String selected = TinyFileDialogs.tinyfd_openFileDialog("Load Passwords", null, filters, "Text files", false);
               if (selected != null && !selected.isBlank()) {
                  List<String> loadedPWs = Files.readAllLines(Paths.get(selected), StandardCharsets.UTF_8);
                  Set<String> parsed = new LinkedHashSet<>();

                  for (String line : loadedPWs) {
                     String pw = line.trim();
                     if (!pw.isEmpty()) {
                        if (pw.contains(":")) {
                           String[] parts = pw.split(":");
                           pw = parts[parts.length - 1].trim();
                        }

                        if (!pw.isEmpty()) {
                           parsed.add(pw);
                        }
                     }
                  }

                  this.read_StrArr = parsed.toArray(new String[0]);
                  this.sync_I = 0;
                  AutismClientMessaging.sendPrefixed("Admin Tools: Loaded " + this.read_StrArr.length + " passwords.");
               }
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("Admin Tools: Failed to load passwords.");
               this.read_StrArr = this.load_StrArr;
               this.sync_I = 0;
            } finally {
               MemoryUtil.memFree(txt);
            }
         } finally {
            stack.close();
         }
      }

      public void load_V_27() {
         this.read_StrArr = this.load_StrArr;
         this.sync_I = 0;
         AutismClientMessaging.sendPrefixed("Admin Tools: Unloaded custom passwords. Reverted to default.");
      }

      public String[] init_StrArr_2() {
         return this.read_StrArr;
      }

      public void read_V_18() {
         if (!this.update_Z) {
            if (MC.player == null) {
               AutismClientMessaging.sendPrefixed("Admin Tools: join a world first.");
            } else {
               this.update_Z = true;
               this.sync_I = 0;
               int delay = this.integer("forceop-delay");
               boolean waitForMsg = this.bool("forceop-wait");
               this.init_Thread = new Thread(() -> {
                  MC.execute(() -> MC.getConnection().sendCommand("login " + MC.getUser().getName()));

                  for (int i = 0; i < this.read_StrArr.length; i++) {
                     if (!this.update_Z) {
                        return;
                     }

                     if (waitForMsg) {
                        this.sync_Z = false;
                     }

                     long startWait = System.currentTimeMillis();

                     while (waitForMsg && !this.sync_Z && MC.player != null) {
                        if (!this.update_Z) {
                           return;
                        }

                        if (System.currentTimeMillis() - startWait > 2000L) {
                           AutismClientMessaging.sendPrefixed("Admin Tools: Timed out waiting for chat message. Stopping ForceOP.");
                           this.update_Z = false;
                           return;
                        }

                        try {
                           Thread.sleep(50L);
                        } catch (InterruptedException var10) {
                        }
                     }

                     try {
                        Thread.sleep(delay);
                     } catch (InterruptedException var12) {
                     }

                     if (!this.update_Z) {
                        return;
                     }

                     boolean sent = false;

                     while (!sent && this.update_Z && MC.player != null) {
                        try {
                           String pw = this.read_StrArr[i];
                           int min = this.integer("forceop-min-length");
                           int max = this.integer("forceop-max-length");
                           if (min > 0 && pw.length() < min) {
                              sent = true;
                           } else if (max > 0 && pw.length() > max) {
                              sent = true;
                           } else {
                              MC.execute(() -> MC.getConnection().sendCommand("login " + pw));
                              sent = true;
                           }
                        } catch (Exception e) {
                           try {
                              Thread.sleep(50L);
                           } catch (InterruptedException var11) {
                           }
                        }
                     }

                     this.sync_I = i + 1;
                  }

                  if (this.update_Z) {
                     MC.execute(() -> AutismClientMessaging.sendPrefixed("§c[§4§lFAILURE§c]§f All " + this.sync_I + " passwords were wrong."));
                     this.update_Z = false;
                  }
               }, "ForceOP");
               this.init_Thread.start();
               AutismClientMessaging.sendPrefixed("Admin Tools: ForceOP started.");
            }
         }
      }

      public void write_V_17() {
         if (this.update_Z) {
            this.update_Z = false;
            if (this.init_Thread != null) {
               this.init_Thread.interrupt();
            }

            AutismClientMessaging.sendPrefixed("Admin Tools: ForceOP stopped.");
         }
      }

      public int init_I_26() {
         return this.read_StrArr.length;
      }

      public int load_I_14() {
         return this.sync_I;
      }

      public boolean read_Z_17() {
         return this.update_Z;
      }

      private void clear() {
         this.setValue("fireball-count", "256");
         this.setValue("fireball-spread", "16.0");
         this.setValue("fireball-speed", "3.5");
         this.setValue("fireball-power", "8");
         this.setValue("fireball-delay", "0");
         this.setValue("fireball-distance", "3.0");
         this.setValue("fireball-aim", "Cone");
         this.setValue("fireball-randomize", "true");
         AutismClientMessaging.sendPrefixed("Admin Tools: loaded max fireball storm preset.");
      }

      private void reset() {
         this.setValue("airstrike-count", "32");
         this.setValue("airstrike-radius", "8.0");
         this.setValue("airstrike-height", "56.0");
         this.setValue("airstrike-speed", "2.4");
         this.setValue("airstrike-power", "5");
         this.setValue("airstrike-delay", "1");
         this.setValue("airstrike-target", "Look");
         AutismClientMessaging.sendPrefixed("Admin Tools: loaded tight airstrike preset.");
      }

      private void start() {
         this.setValue("airstrike-count", "256");
         this.setValue("airstrike-radius", "48.0");
         this.setValue("airstrike-height", "128.0");
         this.setValue("airstrike-speed", "4.5");
         this.setValue("airstrike-power", "10");
         this.setValue("airstrike-delay", "0");
         this.setValue("airstrike-target", "Look");
         AutismClientMessaging.sendPrefixed("Admin Tools: loaded max airstrike preset.");
      }

      private String init(int index, int total) {
         return this.init(index, total, this.decimal("fireball-distance"), 0.0);
      }

      private double init_D_11() {
         return Math.max(2.0, this.decimal("firestorm-distance"));
      }

      private double load_D_3() {
         return Math.max(0.0, this.decimal("firestorm-height"));
      }

      private String init(int index, int total, double spawnDistance, double upOffset) {
         if (MC.player == null) {
            return "";
         }

         Vec3 look = MC.player.getLookAngle();
         Vec3 dir = this.init(look, index, total).normalize();
         double speed = this.decimal("fireball-speed");
         Vec3 motion = dir.scale(speed);
         Vec3 pos = MC.player.getEyePosition().add(look.scale(spawnDistance)).add(0.0, upOffset, 0.0);
         return String.format(
            Locale.ROOT,
            "summon minecraft:fireball %.2f %.2f %.2f {ExplosionPower:%db,power:[%.4f,%.4f,%.4f],Motion:[%.4f,%.4f,%.4f]}",
            pos.x,
            pos.y,
            pos.z,
            this.integer("fireball-power"),
            dir.x,
            dir.y,
            dir.z,
            motion.x,
            motion.y,
            motion.z
         );
      }

      private Vec3 init(Vec3 look, int index, int total) {
         double spread = Math.toRadians(this.decimal("fireball-spread"));
         if (!(spread <= 0.0) && !"Look".equals(this.choice("fireball-aim"))) {
            double yaw;
            double pitch;
            if ("Ring".equals(this.choice("fireball-aim"))) {
               double angle = 6.283185307179586 * index / Math.max(1, total);
               yaw = Math.cos(angle) * spread;
               pitch = Math.sin(angle) * spread;
            } else {
               yaw = (this.init_Random.nextDouble() * 2.0 - 1.0) * spread;
               pitch = (this.init_Random.nextDouble() * 2.0 - 1.0) * spread;
               if (!this.bool("fireball-randomize")) {
                  double step = total <= 1 ? 0.0 : (double)index / (total - 1) * 2.0 - 1.0;
                  yaw = step * spread;
                  pitch = 0.0;
               }
            }

            Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
            if (right.lengthSqr() < 1.0E-4) {
               right = new Vec3(1.0, 0.0, 0.0);
            }

            Vec3 up = right.cross(look).normalize();
            return look.add(right.scale(Math.sin(yaw))).add(up.scale(Math.sin(pitch)));
         } else {
            return look;
         }
      }

      private String load(int index, int total) {
         if (MC.player == null) {
            return "";
         }

         Vec3 center = this.init_Vec3_6();
         double radius = this.decimal("airstrike-radius");
         double angle = total <= 1 ? 0.0 : 6.283185307179586 * ((double)index / total);
         double distance = radius <= 0.0 ? 0.0 : Math.sqrt(this.init_Random.nextDouble()) * radius;
         double x = center.x + Math.cos(angle) * distance;
         double z = center.z + Math.sin(angle) * distance;
         double y = center.y + this.decimal("airstrike-height");
         double speed = -Math.abs(this.decimal("airstrike-speed"));
         return String.format(
            Locale.ROOT,
            "summon minecraft:fireball %.2f %.2f %.2f {ExplosionPower:%db,power:[0.0000,%.4f,0.0000],Motion:[0.0000,%.4f,0.0000]}",
            x,
            y,
            z,
            this.integer("airstrike-power"),
            speed,
            speed
         );
      }

      private Vec3 init_Vec3_6() {
         if (MC.player == null) {
            return Vec3.ZERO;
         }

         if ("Self".equals(this.choice("airstrike-target"))) {
            return MC.player.position();
         }

         HitResult hit = MC.hitResult;
         return hit != null && hit.getType() != Type.MISS ? hit.getLocation() : MC.player.getEyePosition().add(MC.player.getLookAngle().scale(32.0));
      }

      private void stop() {
         if (MC.player != null && MC.getConnection() != null) {
            ItemStack stack = this.init_ItemStack(true);
            if (!stack.isEmpty() && this.init_Z(stack)) {
               AutismClientMessaging.sendPrefixed("Sent edited item packet.");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not send item packet, join a world first.");
         }
      }

      private void next() {
         if (MC.player == null) {
            AutismClientMessaging.sendPrefixed("Admin Tools: join a world first.");
         } else {
            ItemStack stack = MC.player.getMainHandItem();
            if (stack.isEmpty()) {
               AutismClientMessaging.sendPrefixed("Admin Tools: hold an item first.");
            } else if (this.init(stack, true)) {
               AutismClientMessaging.sendPrefixed("Admin Tools: filled editor from held item.");
            }
         }
      }

      private void prev() {
         if (MC.player == null || MC.getConnection() == null) {
            AutismClientMessaging.sendPrefixed("Admin Tools: join a world first.");
         } else if (!this.isCreativeContext()) {
            AutismClientMessaging.sendPrefixed("Admin Tools: creative mode is required.");
         } else if (MC.hitResult instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            ItemStack source = entity instanceof ItemEntity itemEntity
               ? itemEntity.getItem()
               : (entity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY);
            if (source.isEmpty()) {
               AutismClientMessaging.sendPrefixed("Admin Tools: targeted entity has no visible held item.");
            } else {
               ItemStack copy = source.copy();
               if (this.init(copy, false) && this.init_Z(copy)) {
                  AutismClientMessaging.sendPrefixed("Copied targeted item with all client-synced data.");
               }
            }
         } else {
            AutismClientMessaging.sendPrefixed("Admin Tools: look directly at an entity holding the item.");
         }
      }

      public boolean init(ItemStack stack, boolean includeStatusMessage) {
         if (stack != null && !stack.isEmpty()) {
            this.init_V(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), Math.max(1, Math.min(99, stack.getMaxStackSize())));
            this.setValue("nbt-item-count", Integer.toString(Math.max(1, Math.min(99, stack.getCount()))));
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            this.setValue("nbt-item-name", name == null ? "" : name.getString());
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null && !lore.lines().isEmpty()) {
               List<String> lines = new ArrayList<>();

               for (Component line : lore.lines()) {
                  lines.add(line.getString());
               }

               this.setValue("nbt-item-lore", String.join("|", lines));
            } else {
               this.setValue("nbt-item-lore", "");
            }

            this.setValue("nbt-unbreakable", Boolean.toString(stack.has(DataComponents.UNBREAKABLE)));
            Boolean glint = stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            this.setValue("nbt-glint", glint == null ? "Default" : (glint ? "On" : "Off"));
            Rarity rarity = this.init(stack, DataComponents.RARITY);
            String var10002;
            if (rarity == null) {
               var10002 = "Default";
            } else {
               switch (rarity) {
                  case COMMON:
                     var10002 = "Common";
                     break;
                  case UNCOMMON:
                     var10002 = "Uncommon";
                     break;
                  case RARE:
                     var10002 = "Rare";
                     break;
                  case EPIC:
                     var10002 = "Epic";
                     break;
                  default:
                     throw new MatchException(null, null);
               }
            }

            this.setValue("nbt-rarity", var10002);
            this.setValue("nbt-max-damage", Integer.toString(Math.max(0, stack.getMaxDamage())));
            this.setValue("nbt-damage", Integer.toString(Math.max(0, stack.getDamageValue())));
            this.setValue("nbt-enchants", this.init_Str(stack));
            this.setValue("nbt-attributes", this.load(stack));
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag customTag = customData == null ? new CompoundTag() : customData.copyTag();
            this.setValue("nbt-custom-data", customTag.isEmpty() ? "" : customTag.toString());
            String embeddedCommand = customTag.getStringOr("command", "");
            TypedEntityData<?> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (embeddedCommand.isBlank() && blockEntityData != null) {
               embeddedCommand = blockEntityData.copyTagWithoutId().getStringOr("Command", "");
            }

            this.setValue("nbt-command", embeddedCommand);
            String imported = autismclient.util.ay.write(stack);
            this.setValue("nbt-imported-components", imported);
            this.setValue("nbt-imported-signature", this.read_Str_3());
            this.setValue("nbt-item-components", imported);
            this.setValue("nbt-item-stack", autismclient.util.az.init_Str_Str(autismclient.util.ay.load(stack)));
            this.setValue("nbt-item-stack-signature", this.read_Str_3());
            return true;
         } else {
            return false;
         }
      }

      public void update_V_13() {
         this.init_V_2(this.value("nbt-item-id"));
      }

      public void init_V_2(String itemId) {
         Identifier id = Identifier.tryParse(itemId == null ? "" : itemId.trim());
         Item item = id == null ? Items.STICK : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.STICK);
         this.init_V(BuiltInRegistries.ITEM.getKey(item).toString(), this.init(item));
      }

      private void init_V(String itemId, int newMaxStack) {
         int oldMaxStack = Math.max(1, Math.min(99, this.integer("nbt-max-stack")));
         int currentCount = Math.max(1, Math.min(99, this.integer("nbt-item-count")));
         int clampedNewMax = Math.max(1, Math.min(99, newMaxStack));
         int nextCount = currentCount;
         if (currentCount > clampedNewMax || currentCount == oldMaxStack) {
            nextCount = clampedNewMax;
         }

         this.setValue("nbt-item-id", itemId != null && !itemId.isBlank() ? itemId : BuiltInRegistries.ITEM.getKey(Items.STICK).toString());
         this.setValue("nbt-max-stack", Integer.toString(clampedNewMax));
         this.setValue("nbt-item-count", Integer.toString(nextCount));
      }

      private void valid() {
         if (MC.player != null && MC.getConnection() != null) {
            ItemStack stack = this.init_ItemStack(true);
            if (!stack.isEmpty() && this.init_Z(stack)) {
               this.init(stack, false);
               AutismClientMessaging.sendPrefixed("Sent edited held item packet.");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not send item packet, join a world first.");
         }
      }

      private ItemStack init_ItemStack_3() {
         return this.init_ItemStack(false);
      }

      private ItemStack init_ItemStack(boolean notifyErrors) {
         String rawStack = this.value("nbt-item-stack").trim();
         boolean hasRawBase = !rawStack.isBlank();
         ItemStack rawBase = hasRawBase ? autismclient.util.ay.init_ItemStack(rawStack) : ItemStack.EMPTY;
         if (hasRawBase && rawBase.isEmpty()) {
            if (notifyErrors) {
               AutismClientMessaging.sendPrefixed("Admin Tools: invalid full item SNBT.");
            }

            return ItemStack.EMPTY;
         } else {
            Identifier id = Identifier.tryParse(this.value("nbt-item-id").trim());
            Item item = id == null ? Items.STICK : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.STICK);
            boolean itemChanged = hasRawBase && rawBase.getItem() != item;
            ItemStack stack;
            if (hasRawBase && !itemChanged) {
               stack = rawBase.copy();
            } else {
               stack = new ItemStack(item);
               if (hasRawBase) {
                  stack.applyComponents(rawBase.getComponentsPatch());
               }
            }

            boolean applyAll = !hasRawBase;
            if (applyAll || itemChanged || this.read_Z("nbt-item-count")) {
               stack.setCount(Math.max(1, Math.min(99, this.integer("nbt-item-count"))));
            }

            if (!this.init(stack, item, applyAll, notifyErrors)) {
               return ItemStack.EMPTY;
            }

            String validationError = autismclient.util.ay.read(stack);
            if (!validationError.isBlank()) {
               if (notifyErrors) {
                  AutismClientMessaging.sendPrefixed("Admin Tools: invalid item data: " + validationError);
               }

               return ItemStack.EMPTY;
            } else {
               return stack;
            }
         }
      }

      private boolean init(ItemStack stack, Item item, boolean applyAll, boolean notifyErrors) {
         if (applyAll || this.read_Z("nbt-item-name")) {
            String name = this.value("nbt-item-name").trim();
            if (name.isBlank()) {
               stack.remove(DataComponents.CUSTOM_NAME);
            } else {
               stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            }
         }

         if (applyAll || this.read_Z("nbt-item-lore")) {
            List<Component> lore = this.init_List_15();
            if (lore.isEmpty()) {
               stack.remove(DataComponents.LORE);
            } else {
               stack.set(DataComponents.LORE, new ItemLore(lore));
            }
         }

         if (applyAll || this.read_Z("nbt-unbreakable")) {
            if (this.bool("nbt-unbreakable")) {
               stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            } else {
               stack.remove(DataComponents.UNBREAKABLE);
            }
         }

         if (applyAll || this.read_Z("nbt-glint")) {
            switch (this.choice("nbt-glint")) {
               case "On":
                  stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                  break;
               case "Off":
                  stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
                  break;
               default:
                  stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
            }
         }

         if (applyAll || this.read_Z("nbt-rarity")) {
            switch (this.choice("nbt-rarity")) {
               case "Common":
                  stack.set(DataComponents.RARITY, Rarity.COMMON);
                  break;
               case "Uncommon":
                  stack.set(DataComponents.RARITY, Rarity.UNCOMMON);
                  break;
               case "Rare":
                  stack.set(DataComponents.RARITY, Rarity.RARE);
                  break;
               case "Epic":
                  stack.set(DataComponents.RARITY, Rarity.EPIC);
                  break;
               default:
                  stack.remove(DataComponents.RARITY);
            }
         }

         boolean maxDamageChanged = applyAll || this.read_Z("nbt-max-damage");
         boolean damageChanged = applyAll || this.read_Z("nbt-damage");
         if (maxDamageChanged) {
            int maxDamage = this.integer("nbt-max-damage");
            if (maxDamage > 0) {
               stack.set(DataComponents.MAX_DAMAGE, maxDamage);
            } else {
               stack.remove(DataComponents.MAX_DAMAGE);
               stack.remove(DataComponents.DAMAGE);
            }
         }

         if (damageChanged && stack.getMaxDamage() > 0) {
            stack.set(DataComponents.DAMAGE, Math.min(Math.max(0, this.integer("nbt-damage")), stack.getMaxDamage()));
         }

         if (applyAll || this.read_Z("nbt-max-stack")) {
            int maxStack = Math.max(1, Math.min(99, this.integer("nbt-max-stack")));
            int defaultMaxStack = this.init(item);
            if (maxStack == defaultMaxStack) {
               stack.remove(DataComponents.MAX_STACK_SIZE);
            } else {
               stack.set(DataComponents.MAX_STACK_SIZE, maxStack);
            }
         }

         if ((applyAll || this.read_Z("nbt-enchants")) && !this.load(stack, notifyErrors)) {
            return false;
         }

         if (applyAll || this.read_Z("nbt-attributes")) {
            ItemAttributeModifiers attributes = this.init_ItemAttributeModif(this.value("nbt-attributes"), notifyErrors);
            if (attributes == null) {
               return false;
            }

            if (attributes.modifiers().isEmpty()) {
               stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            } else {
               stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
            }
         }

         boolean customChanged = applyAll || this.read_Z("nbt-custom-data") || this.read_Z("nbt-command");
         if (customChanged) {
            CompoundTag customData = this.init_CompoundTag(notifyErrors);
            if (customData == null) {
               return false;
            }

            if (customData.isEmpty()) {
               stack.remove(DataComponents.CUSTOM_DATA);
            } else {
               stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
            }

            String embeddedCommand = this.open(this.value("nbt-command"));
            if (this.update_Z_6() && !embeddedCommand.isBlank()) {
               stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityTypes.COMMAND_BLOCK, this.init_CompoundTag(embeddedCommand)));
            } else if (this.read_Z("nbt-command")) {
               stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            }
         }

         return true;
      }

      private int read_I_8() {
         Identifier id = Identifier.tryParse(this.value("nbt-item-id").trim());
         Item item = id == null ? Items.STICK : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.STICK);
         return this.init(item);
      }

      private int init(Item item) {
         ItemStack stack = new ItemStack(item == null ? Items.STICK : item);
         return Math.max(1, Math.min(99, stack.getMaxStackSize()));
      }

      private List<Component> init_List_15() {
         String raw = this.value("nbt-item-lore");
         if (raw != null && !raw.isBlank()) {
            List<Component> lines = new ArrayList<>();

            for (String part : raw.split("\\|")) {
               String line = part.trim();
               if (!line.isEmpty()) {
                  lines.add(Component.literal(line));
               }
            }

            return lines;
         } else {
            return List.of();
         }
      }

      private void size() {
         this.setValue("nbt-item-components", this.init_Str_11());
         this.setValue("nbt-imported-components", "");
         this.setValue("nbt-imported-signature", "");
         this.setValue("nbt-item-stack", "");
         this.setValue("nbt-item-stack-signature", "");
      }

      public void load_V_2(String rawComponents) {
         String raw = rawComponents == null ? "" : rawComponents.trim();
         this.setValue("nbt-item-components", raw);
         this.setValue("nbt-imported-components", raw);
         this.setValue("nbt-imported-signature", this.read_Str_3());
         this.setValue("nbt-item-stack", "");
         this.setValue("nbt-item-stack-signature", "");
      }

      public boolean init_Z(String rawSnbt) {
         ItemStack parsed = autismclient.util.ay.init_ItemStack(rawSnbt);
         return !parsed.isEmpty() && autismclient.util.ay.read(parsed).isBlank() ? this.init(parsed, false) : false;
      }

      public void sync_V_9() {
         if (!this.write_Z_7()) {
            ItemStack stack = this.init_ItemStack(false);
            if (!stack.isEmpty()) {
               this.init(stack, false);
            }
         }
      }

      private boolean write_Z_7() {
         String raw = this.value("nbt-item-stack").trim();
         return !raw.isBlank() && this.value("nbt-item-stack-signature").equals(this.read_Str_3());
      }

      private String init_Str_11() {
         String components = this.init_Str(false);
         return components == null ? "" : components;
      }

      private String init_Str(boolean notifyErrors) {
         String imported = this.load_Str_5();
         return !imported.isBlank() ? imported : this.load(notifyErrors);
      }

      private String load(boolean notifyErrors) {
         List<String> parts = new ArrayList<>();
         String name = this.value("nbt-item-name").trim();
         if (!name.isBlank()) {
            parts.add("custom_name=[" + this.write(name, "white") + "]");
         }

         String lore = this.write_Str_3();
         if (!lore.isBlank()) {
            parts.add("lore=" + lore);
         }

         if (this.bool("nbt-unbreakable")) {
            parts.add("unbreakable={}");
         }

         String glint = this.choice("nbt-glint");
         if ("On".equals(glint)) {
            parts.add("enchantment_glint_override=true");
         } else if ("Off".equals(glint)) {
            parts.add("enchantment_glint_override=false");
         }

         String rarity = this.choice("nbt-rarity").toLowerCase(Locale.ROOT);
         if (!"default".equals(rarity)) {
            parts.add("rarity=" + rarity);
         }

         int maxDamage = this.integer("nbt-max-damage");
         if (maxDamage > 0) {
            parts.add("max_damage=" + maxDamage);
            parts.add("damage=" + Math.min(this.integer("nbt-damage"), maxDamage));
         } else if (this.integer("nbt-max-stack") != this.read_I_8()) {
            parts.add("max_stack_size=" + Math.max(1, Math.min(99, this.integer("nbt-max-stack"))));
         }

         String enchantments = this.init_Str(this.value("nbt-enchants"), notifyErrors);
         if (enchantments == null) {
            return null;
         }

         if (!enchantments.isBlank()) {
            parts.add("enchantments=" + enchantments);
         }

         this.init(parts, "attribute_modifiers", this.value("nbt-attributes"));
         String embeddedCommand = this.open(this.value("nbt-command"));
         boolean commandBlockCommand = !embeddedCommand.isBlank() && this.update_Z_6();
         if (commandBlockCommand) {
            parts.add("block_entity_data=" + this.load_Str_2(embeddedCommand));
         }

         String customData = this.init_Str_3(commandBlockCommand ? "" : embeddedCommand);
         if (!customData.isBlank()) {
            parts.add("custom_data=" + customData);
         }

         return parts.isEmpty() ? "" : "[" + String.join(",", parts) + "]";
      }

      private String load_Str_5() {
         String imported = this.value("nbt-imported-components").trim();
         if (imported.isBlank()) {
            return "";
         } else {
            String signature = this.value("nbt-imported-signature");
            if (!signature.equals(this.read_Str_3())) {
               return "";
            } else {
               return imported.startsWith("[") ? imported : "[" + imported + "]";
            }
         }
      }

      private String read_Str_3() {
         StringBuilder signature = new StringBuilder();

         for (String field : init_List) {
            String fieldValue = this.value(field);
            signature.append(fieldValue.length()).append(':').append(fieldValue);
         }

         return signature.toString();
      }

      private boolean read_Z(String field) {
         int index = init_List.indexOf(field);
         if (index < 0) {
            return true;
         } else {
            String signature = this.value("nbt-item-stack-signature");
            if (signature.isBlank()) {
               return true;
            } else if (signature.indexOf(31) >= 0) {
               String[] legacyBaseline = signature.split("\u001f", -1);
               return index >= legacyBaseline.length || !this.value(field).equals(legacyBaseline[index]);
            } else {
               String baseline = this.init_Str(signature, index);
               return baseline == null || !this.value(field).equals(baseline);
            }
         }
      }

      private String init_Str(String signature, int wantedIndex) {
         int cursor = 0;

         for (int index = 0; index <= wantedIndex; index++) {
            int separator = signature.indexOf(58, cursor);
            if (separator < cursor) {
               return null;
            }

            int length;
            try {
               length = Integer.parseInt(signature.substring(cursor, separator));
            } catch (NumberFormatException ignored) {
               return null;
            }

            int start = separator + 1;
            int end = start + length;
            if (length < 0 || end < start || end > signature.length()) {
               return null;
            }

            if (index == wantedIndex) {
               return signature.substring(start, end);
            }

            cursor = end;
         }

         return null;
      }

      private <T> T init(ItemStack stack, DataComponentType<T> type) {
         if (stack != null && !stack.isEmpty() && type != null) {
            for (Entry<DataComponentType<?>, Optional<?>> entry : Compat263.dataComponentPatchEntrySet(stack.getComponentsPatch())) {
               if (entry.getKey() == type) {
                  return (T)entry.getValue().orElse(null);
               }
            }

            return null;
         } else {
            return null;
         }
      }

      private String init_Str(ItemStack stack) {
         List<String> entries = new ArrayList<>();
         this.init(entries, stack.get(DataComponents.ENCHANTMENTS));
         this.init(entries, stack.get(DataComponents.STORED_ENCHANTMENTS));
         return String.join(",", entries);
      }

      private boolean load(ItemStack stack, boolean notifyErrors) {
         List<String> normalized = this.init_List(this.value("nbt-enchants"), notifyErrors);
         if (normalized == null) {
            return false;
         }

         Map<String, Integer> desired = new LinkedHashMap<>();

         for (String entry : normalized) {
            int split = entry.lastIndexOf(58);
            if (split <= 0 || split >= entry.length() - 1) {
               return false;
            }

            String id = this.update(entry.substring(0, split));
            if (!id.contains(":")) {
               id = "minecraft:" + id;
            }

            try {
               desired.put(id, Integer.parseInt(entry.substring(split + 1)));
            } catch (NumberFormatException e) {
               return false;
            }
         }

         ItemEnchantments regular = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
         ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
         Mutable regularMutable = new Mutable(regular);
         Mutable storedMutable = new Mutable(stored);
         Set<String> assigned = new LinkedHashSet<>();

         for (Holder<Enchantment> holder : List.copyOf(regular.keySet())) {
            String id = this.init(holder);
            Integer level = desired.get(id);
            regularMutable.set(holder, level == null ? 0 : level);
            if (level != null) {
               assigned.add(id);
            }
         }

         for (Holder<Enchantment> holder : List.copyOf(stored.keySet())) {
            String id = this.init(holder);
            Integer level = desired.get(id);
            storedMutable.set(holder, level == null ? 0 : level);
            if (level != null) {
               assigned.add(id);
            }
         }

         boolean preferStored = stack.is(Items.ENCHANTED_BOOK) || !stored.isEmpty() && regular.isEmpty();

         for (Entry<String, Integer> entry : desired.entrySet()) {
            if (!assigned.contains(entry.getKey())) {
               Holder<Enchantment> holder = this.init_Holder(entry.getKey());
               if (holder == null) {
                  if (notifyErrors) {
                     AutismClientMessaging.sendPrefixed("Admin Tools: unknown enchantment '" + entry.getKey() + "'.");
                  }

                  return false;
               }

               if (preferStored) {
                  storedMutable.set(holder, entry.getValue());
               } else {
                  regularMutable.set(holder, entry.getValue());
               }
            }
         }

         ItemEnchantments updatedRegular = regularMutable.toImmutable();
         ItemEnchantments updatedStored = storedMutable.toImmutable();
         if (updatedRegular.isEmpty()) {
            stack.remove(DataComponents.ENCHANTMENTS);
         } else {
            stack.set(DataComponents.ENCHANTMENTS, updatedRegular);
         }

         if (updatedStored.isEmpty()) {
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
         } else {
            stack.set(DataComponents.STORED_ENCHANTMENTS, updatedStored);
         }

         return true;
      }

      private String init(Holder<Enchantment> holder) {
         return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("");
      }

      private Holder<Enchantment> init_Holder(String rawId) {
         try {
            if (MC.player == null) {
               return null;
            }

            Identifier id = Identifier.tryParse(rawId);
            if (id == null) {
               return null;
            }

            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            return MC.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
         } catch (RuntimeException ignored) {
            return null;
         }
      }

      private String load(ItemStack stack) {
         ItemAttributeModifiers attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (attributes != null && !attributes.modifiers().isEmpty() && MC.player != null) {
            try {
               Tag encoded = ItemAttributeModifiers.CODEC
                  .encodeStart(MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), attributes)
                  .getOrThrow();
               String raw = encoded.toString();
               return raw.length() >= 2 && raw.startsWith("[") && raw.endsWith("]") ? raw.substring(1, raw.length() - 1) : raw;
            } catch (RuntimeException ignored) {
               return "";
            }
         } else {
            return "";
         }
      }

      private ItemAttributeModifiers init_ItemAttributeModif(String raw, boolean notifyErrors) {
         if (raw == null || raw.isBlank()) {
            return ItemAttributeModifiers.EMPTY;
         }

         if (MC.player == null) {
            return null;
         }

         try {
            CompoundTag wrapper = TagParser.parseCompoundFully("{value:[" + raw + "]}");
            Tag encoded = wrapper.get("value");
            return encoded == null
               ? ItemAttributeModifiers.EMPTY
               : ItemAttributeModifiers.CODEC.parse(MC.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), encoded).getOrThrow();
         } catch (Throwable ignored) {
            if (notifyErrors) {
               AutismClientMessaging.sendPrefixed("Admin Tools: invalid attribute modifiers.");
            }

            return null;
         }
      }

      private void init(List<String> entries, ItemEnchantments enchantments) {
         if (enchantments != null && !enchantments.isEmpty()) {
            for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
               String id = entry.getKey().unwrapKey().map(key -> key.identifier().toString()).orElse("").trim();
               if (id.startsWith("minecraft:")) {
                  id = id.substring("minecraft:".length());
               }

               if (!id.isBlank()) {
                  entries.add(id + ":" + entry.getIntValue());
               }
            }
         }
      }

      private String init_Str_3(String embeddedCommand) {
         String raw = this.value("nbt-custom-data").trim();
         String merged = this.init_Str(raw, embeddedCommand);
         return merged.isBlank() ? "" : this.sync(merged);
      }

      private CompoundTag init_CompoundTag(boolean notifyErrors) {
         String embeddedCommand = this.open(this.value("nbt-command"));
         String payload = this.init_Str_3(this.update_Z_6() ? "" : embeddedCommand);
         if (payload.isBlank()) {
            return new CompoundTag();
         }

         try {
            return TagParser.parseCompoundFully(payload);
         } catch (Exception e) {
            if (notifyErrors) {
               AutismClientMessaging.sendPrefixed("Admin Tools: invalid custom data / embedded command data.");
            }

            return null;
         }
      }

      private String init_Str(String rawCustomData, String embeddedCommand) {
         String raw = rawCustomData == null ? "" : rawCustomData.trim();
         String command = this.open(embeddedCommand);
         if (command.isBlank()) {
            return raw;
         }

         List<String> entries = new ArrayList<>();
         String inner = this.flush(raw);
         if (!inner.isBlank()) {
            for (String entry : this.init_List(inner)) {
               String key = this.close(entry);
               if (!"autism_admin_tool".equals(key) && !"command".equals(key)) {
                  entries.add(entry.trim());
               }
            }
         }

         entries.add("autism_admin_tool:1b");
         entries.add("command:\"" + this.pop(command) + "\"");
         return String.join(",", entries);
      }

      private String load_Str_2(String command) {
         String normalized = this.open(command);
         if (normalized.isBlank()) {
            normalized = "say Autism";
         }

         return "{id:\"minecraft:command_block\",Command:\"" + this.pop(normalized) + "\",auto:0b,TrackOutput:1b}";
      }

      private CompoundTag init_CompoundTag(String command) {
         CompoundTag tag = new CompoundTag();
         tag.putString("id", "minecraft:command_block");
         tag.putString("Command", this.open(command).isBlank() ? "say Autism" : this.open(command));
         tag.putByte("auto", (byte)0);
         tag.putByte("TrackOutput", (byte)1);
         return tag;
      }

      private boolean update_Z_6() {
         String item = this.value("nbt-item-id").trim();
         return "minecraft:command_block".equals(item)
            || "command_block".equals(item)
            || "minecraft:chain_command_block".equals(item)
            || "chain_command_block".equals(item)
            || "minecraft:repeating_command_block".equals(item)
            || "repeating_command_block".equals(item);
      }

      private String write_Str_3() {
         List<String> lines = new ArrayList<>();
         String raw = this.value("nbt-item-lore");
         if (raw != null && !raw.isBlank()) {
            for (String part : raw.split("\\|")) {
               String line = part.trim();
               if (!line.isEmpty()) {
                  lines.add(this.write(line, "dark_purple"));
               }
            }

            if (lines.isEmpty()) {
               return "";
            }

            List<String> wrapped = new ArrayList<>();

            for (String line : lines) {
               wrapped.add("[" + line + "]");
            }

            return "[" + String.join(",", wrapped) + "]";
         } else {
            return "";
         }
      }

      private void init(List<String> parts, String name, String raw) {
         if (raw != null && !raw.isBlank()) {
            String trimmed = raw.trim();
            parts.add(name + "=" + (trimmed.startsWith("[") ? trimmed : "[" + trimmed + "]"));
         }
      }

      private String init_Str(String raw, boolean notifyErrors) {
         List<String> entries = this.init_List(raw, notifyErrors);
         if (entries == null) {
            return null;
         } else {
            return entries.isEmpty() ? "" : "{" + String.join(",", entries) + "}";
         }
      }

      private List<String> init_List(String raw, boolean notifyErrors) {
         List<String> entries = new ArrayList<>();
         if (raw != null && !raw.isBlank()) {
            for (String entry : this.init_List(raw)) {
               String normalized = this.read_Str_2(entry);
               if (normalized == null) {
                  if (notifyErrors) {
                     AutismClientMessaging.sendPrefixed("Admin Tools: invalid enchantment entry '" + entry + "'. Use id:level, for example binding_curse:1.");
                  }

                  return null;
               }

               entries.add(normalized);
            }

            return entries;
         } else {
            return entries;
         }
      }

      private String read_Str_2(String entry) {
         if (entry == null) {
            return null;
         }

         String text = entry.trim();
         if (text.isBlank()) {
            return null;
         }

         if (text.startsWith("[") && text.endsWith("]") && text.length() > 2) {
            text = text.substring(1, text.length() - 1).trim();
         }

         if (text.startsWith("{") && text.endsWith("}") && text.contains("id:") && text.contains("level:")) {
            String id = this.read(text, "id");
            String level = this.read(text, "level");
            return this.load_Str(id, level);
         }

         int oldLevelIndex = text.indexOf(":{level:");
         if (oldLevelIndex > 0) {
            String id = text.substring(0, oldLevelIndex);
            String levelPart = text.substring(oldLevelIndex + ":{level:".length());
            int levelEnd = levelPart.indexOf(125);
            String level = levelEnd >= 0 ? levelPart.substring(0, levelEnd) : levelPart;
            return this.load_Str(id, level);
         }

         int levelIndex = text.lastIndexOf(58);
         if (levelIndex > 0 && levelIndex < text.length() - 1) {
            String id = text.substring(0, levelIndex);

            while (id.endsWith(":")) {
               id = id.substring(0, id.length() - 1);
            }

            String level = text.substring(levelIndex + 1);
            return this.load_Str(id, level);
         } else {
            return null;
         }
      }

      private String load_Str(String rawId, String rawLevel) {
         String id = this.write(rawId);
         if (id.isBlank()) {
            return null;
         }

         String levelText = this.update(rawLevel).trim();

         try {
            int level = Integer.parseInt(levelText);
            if (level >= 1 && level <= 255) {
               String commandId = id.contains(":") ? "\"" + this.pop(id) + "\"" : id;
               return commandId + ":" + level;
            } else {
               return null;
            }
         } catch (NumberFormatException ignored) {
            return null;
         }
      }

      private String write(String rawId) {
         String id = this.update(rawId).trim();
         if (id.isBlank()) {
            return "";
         }

         if (id.startsWith("minecraft:")) {
            id = id.substring("minecraft:".length());
         }

         Identifier parsed = id.contains(":") ? Identifier.tryParse(id) : Identifier.tryParse("minecraft:" + id);
         return parsed == null ? "" : id;
      }

      private String read(String entry, String key) {
         String needle = key + ":";
         int start = entry.indexOf(needle);
         if (start < 0) {
            return "";
         }

         start += needle.length();
         int end = start;
         boolean quoted = false;
         char quote = 0;
         boolean escaped = false;

         while (end < entry.length()) {
            char c = entry.charAt(end);
            if (escaped) {
               escaped = false;
            } else if (c == '\\') {
               escaped = true;
            } else if (quoted) {
               if (c == quote) {
                  quoted = false;
               }
            } else if (c != '\'' && c != '"') {
               if (c == ',' || c == '}' || c == ']') {
                  break;
               }
            } else {
               quoted = true;
               quote = c;
            }

            end++;
         }

         return entry.substring(start, end).trim();
      }

      private List<String> init_List(String raw) {
         List<String> out = new ArrayList<>();
         if (raw != null && !raw.isBlank()) {
            String text = raw.trim();
            if (text.startsWith("[") && text.endsWith("]") || text.startsWith("{") && text.endsWith("}")) {
               text = text.substring(1, text.length() - 1);
            }

            StringBuilder current = new StringBuilder();
            int depth = 0;
            boolean quoted = false;
            char quote = 0;
            boolean escaped = false;

            for (int i = 0; i < text.length(); i++) {
               char c = text.charAt(i);
               if (escaped) {
                  current.append(c);
                  escaped = false;
               } else if (c == '\\') {
                  current.append(c);
                  escaped = true;
               } else if (quoted) {
                  current.append(c);
                  if (c == quote) {
                     quoted = false;
                  }
               } else if (c != '\'' && c != '"') {
                  if (c == '{' || c == '[' || c == '(') {
                     depth++;
                  } else if (c == '}' || c == ']' || c == ')') {
                     depth = Math.max(0, depth - 1);
                  }

                  if (c == ',' && depth == 0) {
                     String entry = current.toString().trim();
                     if (!entry.isBlank()) {
                        out.add(entry);
                     }

                     current.setLength(0);
                  } else {
                     current.append(c);
                  }
               } else {
                  quoted = true;
                  quote = c;
                  current.append(c);
               }
            }

            String entry = current.toString().trim();
            if (!entry.isBlank()) {
               out.add(entry);
            }

            return out;
         } else {
            return out;
         }
      }

      private String update(String text) {
         if (text == null) {
            return "";
         }

         String value = text.trim();
         return value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'"))
            ? value.substring(1, value.length() - 1)
            : value;
      }

      private String sync(String raw) {
         String trimmed = raw.trim();
         return trimmed.startsWith("{") ? trimmed : "{" + trimmed + "}";
      }

      private String flush(String raw) {
         String trimmed = raw == null ? "" : raw.trim();
         return trimmed.length() >= 2 && trimmed.startsWith("{") && trimmed.endsWith("}") ? trimmed.substring(1, trimmed.length() - 1).trim() : trimmed;
      }

      private String close(String entry) {
         if (entry == null) {
            return "";
         }

         String text = entry.trim();
         int separator = text.indexOf(58);
         return separator <= 0 ? "" : this.update(text.substring(0, separator)).trim();
      }

      private String open(String command) {
         if (command == null) {
            return "";
         }

         command = command.trim();
         return command.startsWith("/") ? command.substring(1) : command;
      }

      private void data() {
         this.setValue("nbt-item-id", "minecraft:command_block");
         this.setValue("nbt-item-count", "1");
         this.setValue("nbt-item-name", "Admin Command Block");
         this.setValue("nbt-command", this.sync_Str_2().isBlank() ? "say Autism" : this.sync_Str_2());
         this.size();
         if (MC.player != null && MC.getConnection() != null) {
            ItemStack stack = this.init_ItemStack(true);
            if (!stack.isEmpty() && this.init_Z(stack)) {
               AutismClientMessaging.sendPrefixed("Loaded preset and sent command block item packet.");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Loaded command block preset. Join a world to send the item packet.");
         }
      }

      private String update_Str_2() {
         return MC.player == null ? "" : MC.player.getGameProfile().name();
      }

      private void value() {
         String target = this.update_Str_2().isBlank() ? "{target}" : this.update_Str_2();
         this.setValue("nbt-item-id", "minecraft:stick");
         this.setValue("nbt-item-count", "1");
         this.setValue("nbt-item-name", "ForceOP Tool");
         this.setValue("nbt-command", "op " + target);
         this.setValue("nbt-custom-data", "autism_admin_tool:1b,command:\"" + this.pop("op " + target) + "\"");
         this.size();
         if (MC.player != null && MC.getConnection() != null) {
            ItemStack stack = this.init_ItemStack(true);
            if (!stack.isEmpty() && this.init_Z(stack)) {
               AutismClientMessaging.sendPrefixed("Loaded preset and sent ForceOP tool item packet.");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Loaded ForceOP stick preset. Join a world to send the item packet.");
         }
      }

      private void cache() {
         String target = this.update_Str_2().isBlank() ? "{target}" : this.update_Str_2();
         this.setValue("nbt-item-id", "minecraft:command_block");
         this.setValue("nbt-item-count", "1");
         this.setValue("nbt-item-name", "ForceOP Command Block");
         this.setValue("nbt-command", "op " + target);
         this.size();
         AutismClientMessaging.sendPrefixed("Loaded ForceOP command block preset. Use Apply Item in creative mode.");
      }

      private ItemStack init(Item item, int count, String name) {
         ItemStack stack = new ItemStack(item == null ? Items.STICK : item, Math.max(1, Math.min(99, count)));
         if (name != null && !name.isBlank()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
         }

         return stack;
      }

      private boolean init_Z(ItemStack stack) {
         if (MC.player == null || MC.getConnection() == null) {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not send item packet, join a world first.");
            return false;
         }

         if (!this.isCreativeContext()) {
            AutismClientMessaging.sendPrefixed("Admin Tools: creative mode is required.");
            return false;
         }

         if (stack != null && !stack.isEmpty()) {
            String validationError = autismclient.util.ay.read(stack);
            if (!validationError.isBlank()) {
               AutismClientMessaging.sendPrefixed("Admin Tools: invalid item data: " + validationError);
               return false;
            }

            int selected = MC.player.getInventory().getSelectedSlot();
            int slot = 36 + selected;
            MC.player.getInventory().setItem(selected, stack.copy());

            try {
               MC.getConnection().send(new ServerboundSetCreativeModeSlotPacket(slot, stack));
               return true;
            } catch (RuntimeException e) {
               AutismClientMessaging.sendPrefixed("Admin Tools: could not send item packet: " + e.getClass().getSimpleName());
               return false;
            }
         } else {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not send item packet, item stack is empty.");
            return false;
         }
      }

      private void state() {
         if (MC.player != null && MC.getConnection() != null) {
            ItemStack stack = new ItemStack(Items.FIRE_CHARGE, 64);
            stack.set(DataComponents.CUSTOM_NAME, Component.literal("Fireball Storm"));
            if (this.init_Z(stack)) {
               AutismClientMessaging.sendPrefixed("Sent Fireball Storm stack item packet.");
            }
         } else {
            AutismClientMessaging.sendPrefixed("Admin Tools: could not send item packet, join a world first.");
         }
      }

      public void flush() {
         String id = this.value("nbt-item-id").trim();
         if (id.startsWith("/")) {
            id = id.substring(1);
         }

         if (id.isBlank()) {
            AutismClientMessaging.sendPrefixed("Admin Tools: set an item id first.");
         } else {
            String components = this.init_Str(true);
            if (components != null) {
               int count = Math.max(1, Math.min(99, this.integer("nbt-item-count")));
               this.init_Z("give @p " + id + components + " " + count, false);
            }
         }
      }

      private boolean init_Z(String command, boolean queued) {
         return this.init(command, queued, true);
      }

      private boolean init(String command, boolean queued, boolean notifyFailures) {
         if (MC != null && MC.getConnection() != null) {
            if (command != null && !command.isBlank()) {
               String normalized = command.startsWith("/") ? command.substring(1) : command;

               try {
                  MC.getConnection().sendCommand(normalized);
                  String lowerCmd = normalized.toLowerCase(Locale.ROOT);
                  if (lowerCmd.startsWith("summon") && lowerCmd.contains("fireball")) {
                     this.init_J = System.currentTimeMillis() + 5000L;
                  }

                  return true;
               } catch (RuntimeException e) {
                  if (notifyFailures) {
                     AutismClientMessaging.sendPrefixed("Admin Tools: could not send " + (queued ? "queued " : "") + "command: " + e.getClass().getSimpleName());
                  }

                  return false;
               }
            } else {
               if (notifyFailures) {
                  AutismClientMessaging.sendPrefixed("Admin Tools: could not send command, command is empty.");
               }

               return false;
            }
         } else {
            if (notifyFailures) {
               AutismClientMessaging.sendPrefixed("Admin Tools: could not send command, join a world first.");
            }

            return false;
         }
      }

      private void init_V(String command, String label) {
         if (command != null && !command.isBlank()) {
            this.load_V("/" + command, "Copied " + label + ".");
         } else {
            AutismClientMessaging.sendPrefixed("Admin Tools: join a world first.");
         }
      }

      private void load_V(String text, String message) {
         if (MC != null && MC.keyboardHandler != null && text != null && !text.isBlank()) {
            MC.keyboardHandler.setClipboard(text);
            autismclient.util.bi.init(message);
         } else {
            autismclient.util.bi.write("Nothing to copy.");
         }
      }

      private String sync_Str_2() {
         String command = this.value("nbt-command").trim();
         if (command.isBlank()) {
            command = "op {target}";
         }

         String target = this.update_Str_2();
         if (!target.isBlank()) {
            command = command.replace("{target}", target);
         }

         return command.startsWith("/") ? command.substring(1) : command;
      }

      private String init(String name, String lore, String command) {
         return "[custom_name=["
            + this.write(name, "red")
            + "],lore=[["
            + this.write(lore, "gray")
            + "]],custom_data={autism_admin_tool:1b,command:\""
            + this.pop(command)
            + "\"}]";
      }

      private String poll(String command) {
         String normalized = command != null && !command.isBlank() ? command : "say Autism";
         normalized = normalized.startsWith("/") ? normalized.substring(1) : normalized;
         return "[custom_name=["
            + this.write(this.value("nbt-item-name").isBlank() ? "Admin Command Block" : this.value("nbt-item-name"), "red")
            + "],block_entity_data={id:\"minecraft:command_block\",Command:\""
            + this.pop(normalized)
            + "\",auto:0b,TrackOutput:1b}]";
      }

      private String write(String text, String color) {
         return "{\"text\":\"" + this.push(text) + "\",\"color\":\"" + this.push(color) + "\",\"italic\":false}";
      }

      private String push(String text) {
         return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
      }

      private String pop(String text) {
         return this.push(text);
      }
   }

   static final class Inner_EventBus extends Module {
      private static final int init_I = 5;
      private static final double init_D = 12.0;
      private static final ValueRange init_ValueRange = new ValueRange(50, 80);
      private static final double load_D = 250.0;
      private final h init_h;
      private final autismclient.util.a.DataUtil init_DataUtil = new autismclient.util.a.DataUtil();
      private final f.Inner_EventBus.Inner_DataUtil init_f_EventBus_DataUti = new f.Inner_EventBus.Inner_DataUtil();
      private final Random init_Random = new Random();
      private boolean init_Z;
      private long init_J = -9223372036854775808L;
      private long load_J;
      private boolean load_Z;
      private boolean read;
      private int load_I;
      private ce.Inner_DataUtil init_ce_DataUtil;
      private ce.Inner_DataUtil load_ce_DataUtil;

      Inner_EventBus() {
         super("fast-use", "FastUse", ae.read, "Speeds up item use.");
         f.init = this;
         this.add(new BoolSetting("exp", "Fast EXP", true).description("Speed experience bottles."));
         this.add(new BoolSetting("blocks", "Fast Blocks", true).description("Speed block use."));
         this.add(
            new RangeSetting("hold-delay", "Hold Delay", init_ValueRange, 0.0, 250.0, 5.0)
               .unit("ms")
               .description("How long right-click is held before the clicks start")
               .visibleWhen(() -> this.bool("exp") || this.bool("blocks"))
         );
         this.init_h = new h(this, "General", h.init_BufferHelper, () -> this.bool("exp") || this.bool("blocks"), 12.0);
         this.init_h.init_V();
      }

      @Override
      protected String externalSettingValue(String settingId) {
         return this.init_h == null ? null : this.init_h.init_Str_Str(settingId);
      }

      @Override
      protected boolean setExternalSettingValue(String settingId, String value) {
         return this.init_h != null && this.init_h.init(settingId, value);
      }

      @Override
      public void onInputFrame() {
         long now = System.nanoTime();
         boolean physical = this.write_Z_8() && this.init_Z_41();
         if (!physical) {
            if (this.init_Z) {
               this.init_DataUtil.init_V_J_DataUtil_NetHelper(now, this.init_f_EventBus_DataUti);
            }

            this.init_Z = false;
            this.init_J = -9223372036854775808L;
         } else {
            autismclient.util.a.CachePool profile = this.init_h.init_CachePool();
            this.init_DataUtil.init_V_DataUtil_ConfigSto(this.init_h.init_DataUtil_ConfigSto());
            if (!this.init_Z) {
               this.init_DataUtil.init_V_J_2(now, profile, this.init_f_EventBus_DataUti);
               this.init_J = now;
               this.load_J = this.init_J_8();
            }

            this.init_Z = true;
            if (this.init_J != -9223372036854775808L && now - this.init_J >= this.load_J) {
               this.init_DataUtil.init(now, true, profile, this.init_f_EventBus_DataUti);
            }
         }
      }

      private long init_J_8() {
         ValueRange band = ValueRange.parse(this.value("hold-delay"), init_ValueRange);
         double ms = band.max() <= band.min() ? band.min() : band.min() + this.init_Random.nextDouble() * (band.max() - band.min());
         return (long)Math.max(0.0, ms * 1000000.0);
      }

      @Override
      public void preMovementTick() {
         if (MC == null || MC.player == null || MC.gameMode == null || MC.getConnection() == null) {
            this.update_V_14();
         } else if (AutoTotemModule.init_Z_6()) {
            this.read_V_19();
         } else if (MC.player.isUsingItem()) {
            this.read_V_19();
         } else {
            InteractionHand expHand = this.bool("exp") && this.read_Z_18() && !this.init_Z(true) ? this.init_InteractionHand_4() : null;
            if (expHand != null && this.write_Z_8()) {
               this.init_V(true);
               this.init_V_38();
            } else if (this.bool("blocks") && this.read_Z_18() && this.sync() && this.write_Z_8() && !this.init_Z(false)) {
               this.init_V(false);
               this.write_V_18();
            } else {
               this.read_V_19();
            }
         }
      }

      @Override
      public void onDisable() {
         this.update_V_14();
      }

      @Override
      public void onGameJoin() {
         this.update_V_14();
      }

      @Override
      public void onGameLeft() {
         this.update_V_14();
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (packet instanceof ServerboundMovePlayerPacket movement && movement.hasRotation() && MC != null && MC.player != null) {
            ce.Inner_DataUtil base = this.init_ce_DataUtil_5();
            this.load_ce_DataUtil = new ce.Inner_DataUtil(movement.getYRot(base.init()), movement.getXRot(base.load()));
         }

         return false;
      }

      private void init_V_38() {
         if (this.init_ce_DataUtil == null) {
            ce.Inner_DataUtil accepted = this.init_ce_DataUtil_5();
            this.init_ce_DataUtil = ce.init_ce_DataUtil(new ce.Inner_DataUtil(accepted.init(), 90.0F), accepted);
            this.load_I = 5;
         }

         this.write_V_18();
         if (cn.init_ce_DataUtil() == null && !cn.init_Z()) {
            this.load_I = 5;
         }
      }

      private boolean init_Z_41() {
         return this.load_Z || this.read;
      }

      private boolean load_Z_20() {
         if (cn.init_ce_DataUtil() == null && !cn.init_Z() && !ScaffoldModule.update_Z_5() && !AutoTotemModule.init_Z_6()) {
            return this.load_Z
               ? this.init_InteractionHand_4() != null && this.load_ce_DataUtil_4() != null && autismclient.util.as.init_Z()
               : this.read && this.sync() && autismclient.util.as.load_Z();
         } else {
            return false;
         }
      }

      private boolean read_Z_18() {
         return MC.options != null
            && MC.level != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !MC.player.isDeadOrDying()
            && !MC.player.isSpectator()
            && !aD.read_Z()
            && !aC.init_Z()
            && !cd.init_Z()
            && (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
            && !MacroExecutor.write_Z();
      }

      private boolean write_Z_8() {
         return MC.options != null && autismclient.util.aB.init(MC.options.keyUse).autism$isActuallyDown();
      }

      private InteractionHand init_InteractionHand_4() {
         if (MC.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
            return InteractionHand.MAIN_HAND;
         } else {
            return MC.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE) ? InteractionHand.OFF_HAND : null;
         }
      }

      private InteractionHand init(InteractionHand preferredHand) {
         return MC.player.getItemInHand(preferredHand).is(Items.EXPERIENCE_BOTTLE) ? preferredHand : this.init_InteractionHand_4();
      }

      private void init_V(boolean experience) {
         if ((!experience || !this.load_Z) && (experience || !this.read)) {
            this.read_V_19();
            this.load_Z = experience;
            this.read = !experience;
            this.load_V_28();
         }
      }

      private boolean update_Z_7() {
         if (MC == null || MC.player == null) {
            return false;
         } else if (this.load_Z) {
            return this.init_InteractionHand_4() != null && init(this.init_ce_DataUtil, this.init_ce_DataUtil_5());
         } else {
            return !this.read ? false : this.sync() && this.load_ce_DataUtil_4() == null && cn.init_ce_DataUtil() == null && !cn.init_Z();
         }
      }

      private void load_V_28() {
         this.init_DataUtil.init_V_J_DataUtil_NetHelper(System.nanoTime(), this.init_f_EventBus_DataUti);
         this.init_Z = false;
         this.init_J = -9223372036854775808L;
      }

      private void read_V_19() {
         if (this.load_Z) {
            autismclient.util.as.sync_V();
         }

         if (this.read) {
            autismclient.util.as.flush_V();
         }

         this.load_Z = false;
         this.read = false;
         this.load_V_28();
         this.write_V_18();
      }

      private void write_V_18() {
         if (this.load_I > 0) {
            this.load_I--;
            if (this.load_I <= 0) {
               this.init_ce_DataUtil = null;
            }
         }
      }

      private void update_V_14() {
         autismclient.util.as.sync_V();
         autismclient.util.as.flush_V();
         this.load_Z = false;
         this.read = false;
         this.load_V_28();
         this.load_I = 0;
         this.init_ce_DataUtil = null;
         this.load_ce_DataUtil = null;
      }

      private ce.Inner_DataUtil init_ce_DataUtil_5() {
         if (this.load_ce_DataUtil != null) {
            return this.load_ce_DataUtil;
         } else {
            return MC != null && MC.player != null ? new ce.Inner_DataUtil(MC.player.getYRot(), MC.player.getXRot()) : new ce.Inner_DataUtil(0.0F, 0.0F);
         }
      }

      private ce.Inner_DataUtil load_ce_DataUtil_4() {
         return this.load_I > 0 ? this.init_ce_DataUtil : null;
      }

      private static boolean init(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
         return first != null && second != null && Float.compare(first.init(), second.init()) == 0 && Float.compare(first.load(), second.load()) == 0;
      }

      private boolean init_Z(boolean bottlesIntended) {
         ItemStack main = MC.player.getMainHandItem();
         if (main.isEmpty()) {
            return false;
         } else if (main.getItem() instanceof BlockItem) {
            return bottlesIntended && !MC.player.onGround() && MC.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE);
         } else if (bottlesIntended && main.is(Items.EXPERIENCE_BOTTLE)) {
            return false;
         } else {
            return main.getUseAnimation() != ItemUseAnimation.NONE
               ? true
               : main.is(Items.EXPERIENCE_BOTTLE)
                  || main.is(Items.ENDER_PEARL)
                  || main.is(Items.SNOWBALL)
                  || main.is(Items.EGG)
                  || main.is(Items.FISHING_ROD)
                  || main.getItem() instanceof BucketItem
                  || main.is(Items.WIND_CHARGE)
                  || main.is(Items.FIRE_CHARGE)
                  || main.is(Items.ENDER_EYE)
                  || main.is(Items.SPLASH_POTION)
                  || main.is(Items.LINGERING_POTION);
         }
      }

      private boolean sync() {
         return MC.player.getMainHandItem().getItem() instanceof BlockItem || MC.player.getOffhandItem().getItem() instanceof BlockItem;
      }

      private final class Inner_DataUtil implements autismclient.util.a.DataUtil.Inner_NetHelper {
         @Override
         public boolean init_Z_J(long now) {
            if (!Inner_EventBus.this.update_Z_7()) {
               return false;
            }

            if (Inner_EventBus.this.load_Z) {
               autismclient.util.as.read_V();
            } else {
               autismclient.util.as.write_V();
            }

            return true;
         }

         @Override
         public void init_V_J(long now) {
         }
      }
   }

   static final class Inner_FieldCache extends Module {
      private String init_Str = null;
      private String load_Str = null;
      private Set<Class<? extends Packet<?>>> init_Set = Set.of();
      private Set<Class<? extends Packet<?>>> load_Set = Set.of();

      Inner_FieldCache() {
         super("packet-canceller", "PacketCanceller", ae.write, "Cancels selected packets.");
         this.add(new PacketListSetting("C2S-packets", "C2S Packets", "").description("Outgoing packet list."));
         this.add(new PacketListSetting("S2C-packets", "S2C Packets", "").description("Incoming packet list."));
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         return this.init(packet, true);
      }

      @Override
      public boolean onPacketReceive(Packet<?> packet) {
         return this.init(packet, false);
      }

      @Override
      public String info() {
         return "C2S " + this.init(true).size() + " | S2C " + this.init(false).size();
      }

      private boolean init(Packet<?> packet, boolean c2s) {
         if (packet == null) {
            return false;
         } else {
            return this.init(c2s).contains(packet.getClass()) ? true : this.init(c2s ? this.text("C2S-packets") : this.text("S2C-packets"), packet);
         }
      }

      private Set<Class<? extends Packet<?>>> init(boolean c2s) {
         String value = c2s ? this.text("C2S-packets") : this.text("S2C-packets");
         if (c2s) {
            if (!value.equals(this.init_Str)) {
               this.init_Str = value;
               this.init_Set = dd.init_Set(value, true);
            }

            return this.init_Set;
         } else {
            if (!value.equals(this.load_Str)) {
               this.load_Str = value;
               this.load_Set = dd.init_Set(value, false);
            }

            return this.load_Set;
         }
      }

      private boolean init(String list, Packet<?> packet) {
         if (packet != null && list != null && !list.isBlank()) {
            String simple = packet.getClass().getSimpleName().toLowerCase(Locale.ROOT);
            String full = packet.getClass().getName().toLowerCase(Locale.ROOT);
            Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)packet.getClass();
            String registry = String.valueOf(bw.init_Str_Class(packetClass)).toLowerCase(Locale.ROOT);

            for (String token : list.split("[,|]")) {
               String needle = token.trim().toLowerCase(Locale.ROOT);
               if (!needle.isEmpty() && (simple.equals(needle) || full.equals(needle) || registry.equals(needle))) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }
   }

   static final class Inner_FileCache extends Module {
      private int init_I;
      private int load_I;
      private double init_D = 1.7976931348623157E308;
      private boolean init_Z;
      private String init_Str = "Abilities";
      private double load_D;
      private int read_I;
      private int write_I;
      private boolean load_Z;
      private Boolean init_Boolean;
      private long init_J;
      private int update_I = -2147483648;
      private String load_Str = "Abilities";
      private String read_Str = "Packet";
      private double read_D = 0.1;
      private boolean read_Z;
      private boolean write_Z;
      private int sync_I = 20;
      private int flush_I = 1;
      private long load_J = -9223372036854775808L;
      private boolean update_Z;
      private boolean sync_Z;
      private boolean flush_Z;

      Inner_FileCache() {
         super("flight", "Flight", ae.load_ae, "Client-side flight.");
         this.add(new ChoiceSetting("mode", "Mode", "Abilities", "Abilities", "Velocity", "Vulcan").description("Flight method").build());
         this.add(
            new DoubleSetting("speed", "Speed", 0.1, 0.0, 2.0, 0.01)
               .visibleWhen(() -> !"Vulcan".equals(this.value("mode")))
               .description("Your speed when flying.")
               .build()
         );
         this.add(
            new BoolSetting("vertical-speed-match", "Vertical Speed Match", false)
               .description("Match vertical speed.")
               .visibleWhen(() -> "Velocity".equals(this.value("mode")))
               .build()
         );
         this.add(
            new BoolSetting("no-sneak", "No Sneak", false).description("Ignore sneak input.").visibleWhen(() -> "Velocity".equals(this.value("mode"))).build()
         );
         this.add(
            new BoolSetting("anti-break-elytra", "Anti Break Elytra", true)
               .visibleWhen(() -> "Vulcan".equals(this.value("mode")))
               .description("Preserve low-durability elytra.")
               .build()
         );
         this.add(new BoolSetting("anti-hunger", "AntiHunger", false).description("Hide sprint from server.").build());
         this.add(
            new ChoiceSetting("anti-kick-mode", "Anti Kick", "Packet", "Normal", "Packet", "None")
               .group("Anti Kick")
               .visibleWhen(() -> !"Vulcan".equals(this.value("mode")))
               .description("Reduce floating kicks")
               .build()
         );
         this.add(
            new IntSetting("delay", "Delay", 20, 1, 200, 1)
               .group("Anti Kick")
               .visibleWhen(() -> !"Vulcan".equals(this.value("mode")))
               .description("Ticks between anti-kick nudges.")
               .build()
         );
         this.add(
            new IntSetting("off-time", "Off Time", 1, 1, 20, 1)
               .group("Anti Kick")
               .visibleWhen(() -> !"Vulcan".equals(this.value("mode")))
               .description("Ticks spent nudging down.")
               .build()
         );
      }

      @Override
      public void onEnable() {
         this.close_V_7();
         this.flush_V_8();
         this.init_Str = this.load_Str;
         this.flush_Z = false;
         if (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z()) {
            if ("Vulcan".equals(this.init_Str)) {
               if (!this.sync_Z_9()) {
                  this.init_V_2(true);
               } else {
                  this.load_D = MC.player.getY();
                  boolean airborne = !MC.player.onGround();
                  this.read_I = airborne ? 10 : 0;
                  this.write_I = 0;
                  this.load_Z = MC.player.isFallFlying();
                  if (airborne) {
                     this.update_V_15();
                  } else {
                     this.write_V_19();
                  }
               }
            } else if ("Abilities".equals(this.init_Str)) {
               this.init_V_39();
            }
         }
      }

      @Override
      public void tick() {
         this.close_V_7();
         if (MC.player != null && !this.flush_Z_8() && "Vulcan".equals(this.load_Str)) {
         }
      }

      @Override
      public void preMovementTick() {
         if ((AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && MC.player != null
            && MC.gameMode != null
            && !MC.player.isSpectator()
            && !this.flush_Z_8()) {
            if (IoWorker.init_Z()) {
               if (this.init_Z) {
                  this.load_V_29();
               }
            } else {
               this.close_V_7();
               String mode = this.load_Str;
               this.init(mode);
               if ("Vulcan".equals(mode)) {
                  this.read_V_20();
               } else {
                  if (this.init_I > 0) {
                     this.init_I--;
                  }

                  if (this.load_I <= 0 && this.init_I <= 0) {
                     this.init_I = this.sync_I;
                     this.load_I = this.flush_I;
                     if (this.update_Z_8()) {
                        ((AutismLocalPlayerAccessor)MC.player).autism$setPositionReminder(20);
                     }
                  } else if (this.init_I <= 0) {
                     boolean shouldReturn = false;
                     if ("Normal".equals(this.read_Str) && "Abilities".equals(mode)) {
                        this.load_V_29();
                        shouldReturn = true;
                     } else if (this.update_Z_8() && this.load_I == this.flush_I) {
                        ((AutismLocalPlayerAccessor)MC.player).autism$setPositionReminder(20);
                     }

                     this.load_I--;
                     if (shouldReturn) {
                        return;
                     }
                  }

                  if ("Velocity".equals(mode)) {
                     MC.player.getAbilities().flying = false;
                     MC.player.setDeltaMovement(Vec3.ZERO);
                     Vec3 velocity = MC.player.getDeltaMovement();
                     double vertical = this.read_D * (this.read_Z ? 10.0 : 5.0);
                     if (MC.options.keyJump.isDown()) {
                        velocity = velocity.add(0.0, vertical, 0.0);
                     }

                     if (MC.options.keyShift.isDown()) {
                        velocity = velocity.subtract(0.0, vertical, 0.0);
                     }

                     MC.player.setDeltaMovement(velocity);
                     if (this.write_Z) {
                        MC.player.setOnGround(false);
                     }
                  } else if ("Abilities".equals(mode)) {
                     if (MC.player.isSpectator()) {
                        return;
                     }

                     MC.player.getAbilities().setFlyingSpeed((float)this.read_D);
                     MC.player.getAbilities().flying = true;
                     this.init_Z = true;
                     if (!MC.player.getAbilities().instabuild) {
                        MC.player.getAbilities().mayfly = true;
                     }
                  }
               }
            }
         }
      }

      @Override
      public void onDisable() {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            this.flush_V_8();
            this.init_Z = false;
         } else {
            boolean wasVulcan = "Vulcan".equals(this.init_Str) || "Vulcan".equals(this.load_Str);
            if (wasVulcan) {
               this.sync_V_10();
            }

            this.flush_V_8();
            if (MC.player != null && !MC.player.isSpectator() && (this.init_Z || "Abilities".equals(this.init_Str))) {
               this.load_V_29();
            }
         }
      }

      @Override
      public void onGameLeft() {
         this.flush_V_8();
         this.sync_Z = false;
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            return false;
         }

         if (this.sync_Z) {
            return false;
         }

         if (IoWorker.init_Z()) {
            return false;
         }

         if (MC.player != null && MC.getConnection() != null && !this.flush_Z_8()) {
            this.close_V_7();
            if ("Vulcan".equals(this.load_Str)) {
               return false;
            }

            if (this.update_Z_8() && packet instanceof ServerboundMovePlayerPacket move) {
               double currentY = move.getY(1.7976931348623157E308);
               if (currentY != 1.7976931348623157E308) {
                  this.init(move, currentY);
                  return false;
               }

               ServerboundMovePlayerPacket fullPacket = move.hasRotation()
                  ? new PosRot(
                     MC.player.getX(),
                     MC.player.getY(),
                     MC.player.getZ(),
                     move.getYRot(0.0F),
                     move.getXRot(0.0F),
                     move.isOnGround(),
                     MC.player.horizontalCollision
                  )
                  : new Pos(MC.player.getX(), MC.player.getY(), MC.player.getZ(), move.isOnGround(), MC.player.horizontalCollision);
               this.init(fullPacket, MC.player.getY());
               this.sync_Z = true;

               try {
                  MC.getConnection().send(fullPacket);
               } finally {
                  this.sync_Z = false;
               }

               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public boolean onPacketReceive(Packet<?> packet) {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            return false;
         }

         if (MC.player != null && !this.flush_Z_8()) {
            this.close_V_7();
            if (packet instanceof ClientboundPlayerAbilitiesPacket abilities && "Abilities".equals(this.load_Str)) {
               AuditClientFixes.applyFlightAbilities(this, abilities);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      float init_F_7() {
         if (IoWorker.init_Z()) {
            return -1.0F;
         }

         this.close_V_7();
         return this.isEnabled() && !this.flush_Z_8() && "Velocity".equals(this.load_Str)
            ? (float)this.read_D * (MC.player != null && MC.player.isSprinting() ? 15.0F : 10.0F)
            : -1.0F;
      }

      boolean init_Z_42() {
         return this.bool("anti-hunger");
      }

      boolean load_Z_21() {
         if (IoWorker.init_Z()) {
            return false;
         }

         this.close_V_7();
         return this.isEnabled() && !this.flush_Z_8() && "Velocity".equals(this.load_Str) && this.write_Z;
      }

      private void init_V_39() {
         if (MC.player != null && !MC.player.isSpectator()) {
            MC.player.getAbilities().flying = true;
            this.init_Z = true;
            if (!MC.player.getAbilities().instabuild) {
               MC.player.getAbilities().mayfly = true;
            }
         }
      }

      private void load_V_29() {
         if (MC.player != null && !MC.player.isSpectator()) {
            MC.player.getAbilities().setFlyingSpeed(0.05F);
            MC.player.getAbilities().flying = false;
            if (!MC.player.getAbilities().instabuild) {
               MC.player.getAbilities().mayfly = false;
            }

            this.init_Z = false;
         }
      }

      private void init(ServerboundMovePlayerPacket packet, double currentY) {
         if (this.init_I <= 0 && this.init_D != 1.7976931348623157E308 && this.init(currentY, this.init_D) && this.read_Z_19()) {
            ((AutismMovePlayerPacketAccessor)packet).autism$setY(this.init_D - 0.0313);
         } else {
            this.init_D = currentY;
         }
      }

      private boolean init(double currentY, double lastY) {
         return currentY >= lastY || lastY - currentY < 0.0313;
      }

      private boolean read_Z_19() {
         long tick = MC.level == null ? -9223372036854775808L : MC.level.getGameTime();
         if (this.load_J == tick) {
            return this.update_Z;
         }

         this.load_J = tick;
         this.update_Z = this.write_Z_9();
         return this.update_Z;
      }

      private boolean write_Z_9() {
         if (MC.level != null && MC.player != null) {
            AABB box = MC.player.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0);
            return MC.level.getBlockStates(box).allMatch(state -> state.isAir());
         } else {
            return false;
         }
      }

      private void init(String mode) {
         if (!mode.equals(this.init_Str)) {
            if ("Abilities".equals(this.init_Str)) {
               this.load_V_29();
            }

            this.init_Str = mode;
            if ("Abilities".equals(mode)) {
               this.init_V_39();
            }

            if ("Vulcan".equals(mode)) {
               this.flush_V_8();
            }
         }
      }

      private boolean update_Z_8() {
         return "Packet".equals(this.read_Str);
      }

      private void read_V_20() {
         if (!this.sync_Z_9()) {
            this.init_V_2(false);
         } else {
            if (this.read_I > 0) {
               this.read_I--;
            }

            if (this.write_I > 0) {
               this.write_I--;
            }

            if (MC.player.onGround()) {
               this.load_Z = false;
            }

            this.update_V_15();
            boolean jump = MC.options != null && MC.options.keyJump.isDown();
            boolean shift = MC.options != null && MC.options.keyShift.isDown();
            boolean gliding = MC.player.isFallFlying();
            if (jump) {
               this.load_D = Math.max(this.load_D + (gliding ? 0.04 : 0.12), MC.player.getY());
               if (!gliding || MC.player.getY() < this.load_D - 0.08 || MC.player.getDeltaMovement().y < -0.055) {
                  this.write_V_19();
               }
            } else if (shift) {
               this.load_D = MC.player.getY() - 0.35;
            } else if (MC.player.getY() < this.load_D - 0.22 && MC.player.getDeltaMovement().y < -0.02) {
               this.write_V_19();
            }
         }
      }

      private void init_V_2(boolean duringEnable) {
         if (!this.flush_Z) {
            this.flush_Z = true;
            String message = "Flight disabled: elytra not found.";
            if (duringEnable) {
               this.disableSilentlyWithToggleMessage(message);
            } else {
               this.disableWithToggleMessage(message);
            }

            this.flush_Z = false;
         }
      }

      private void write_V_19() {
         if (this.read_I <= 0 && MC.player != null) {
            if (MC.player.onGround()) {
               if (MC.player.isFallFlying()) {
                  MC.player.stopFallFlying();
               }

               MC.player.jumpFromGround();
               this.read_I = 3;
            } else {
               MC.player.jumpFromGround();
               this.read_I = MC.player.isFallFlying() ? 12 : 3;
            }
         }
      }

      private void update_V_15() {
         if (this.write_I <= 0 && MC.player != null && MC.getConnection() != null && !MC.player.onGround() && !MC.player.isFallFlying() && !this.load_Z) {
            MC.player.tryToStartFallFlying();
            MC.getConnection()
               .send(new ServerboundPlayerCommandPacket(MC.player, net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            this.load_Z = true;
            this.write_I = 20;
         }
      }

      private void sync_V_10() {
         if (MC.player != null) {
            if (MC.player.isFallFlying()) {
               MC.player.stopFallFlying();
            }

            this.write_I = 0;
            this.read_I = 0;
            this.load_Z = false;
         }
      }

      private boolean sync_Z_9() {
         if (MC.player == null) {
            return false;
         }

         ItemStack chest = MC.player.getItemBySlot(EquipmentSlot.CHEST);
         if (this.init_Z_2(chest)) {
            if (this.bool("anti-break-elytra") && !this.load_Z(chest)) {
               int replacement = this.init_I(true);
               if (replacement >= 0 && autismclient.util.aw.read(MC, replacement, 38)) {
                  return true;
               }

               int emptySlot = this.init_I_27();
               if (emptySlot >= 0) {
                  autismclient.util.aw.read(MC, 38, emptySlot);
               }

               return false;
            } else {
               return true;
            }
         } else {
            int elytraSlot = this.init_I(this.bool("anti-break-elytra"));
            return elytraSlot < 0 ? false : autismclient.util.aw.read(MC, elytraSlot, 38) && this.init_Z_2(MC.player.getItemBySlot(EquipmentSlot.CHEST));
         }
      }

      private int init_I(boolean safeOnly) {
         if (MC.player == null) {
            return -1;
         }

         int bestSlot = -1;
         int bestRemaining = -1;

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (this.init_Z_2(stack) && (!safeOnly || this.load_Z(stack))) {
               int remaining = this.init_I(stack);
               if (remaining > bestRemaining) {
                  bestRemaining = remaining;
                  bestSlot = slot;
               }
            }
         }

         return bestSlot;
      }

      private int init_I_27() {
         if (MC.player == null) {
            return -1;
         }

         for (int slot = 0; slot < 36; slot++) {
            if (MC.player.getInventory().getItem(slot).isEmpty()) {
               return slot;
            }
         }

         return -1;
      }

      private boolean init_Z_2(ItemStack stack) {
         return stack != null && stack.is(Items.ELYTRA) && !stack.isBroken() && this.init_I(stack) > 0;
      }

      private boolean load_Z(ItemStack stack) {
         return this.init_Z_2(stack) && this.init_I(stack) > this.load_I(stack);
      }

      private int init_I(ItemStack stack) {
         return stack != null && stack.isDamageableItem() ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 2147483647;
      }

      private int load_I(ItemStack stack) {
         int max = stack != null && stack.isDamageableItem() ? stack.getMaxDamage() : 0;
         return Math.max(40, (int)Math.ceil(max * 0.1));
      }

      private void flush_V_8() {
         this.close_V_7();
         this.init_I = this.sync_I;
         this.load_I = this.flush_I;
         this.init_D = 1.7976931348623157E308;
         this.load_J = -9223372036854775808L;
         this.update_Z = false;
         this.write_I = 0;
         this.load_Z = false;
      }

      private void close_V_7() {
         int revision = al.init_I();
         if (this.update_I != revision) {
            this.update_I = revision;
            this.load_Str = this.choice("mode");
            this.read_Str = this.choice("anti-kick-mode");
            this.read_D = this.decimal("speed");
            this.read_Z = this.bool("vertical-speed-match");
            this.write_Z = this.bool("no-sneak");
            this.sync_I = Math.max(1, this.integer("delay"));
            this.flush_I = Math.max(1, this.integer("off-time"));
         }
      }

      private boolean flush_Z_8() {
         long now = System.currentTimeMillis();
         if (this.init_Boolean != null && now - this.init_J < 250L) {
            return this.init_Boolean;
         }

         this.init_J = now;
         this.init_Boolean = this.close_Z_7();
         return this.init_Boolean;
      }

      private boolean close_Z_7() {
         try {
            Class<?> modulesClass = Class.forName("meteordevelopment.meteorclient.systems.modules.Modules", false, f.Inner_FileCache.class.getClassLoader());
            Class<?> flightClass = Class.forName(
               "meteordevelopment.meteorclient.systems.modules.movement.Flight", false, f.Inner_FileCache.class.getClassLoader()
            );
            Object modules = modulesClass.getMethod("get").invoke(null);
            Object flight = modulesClass.getMethod("get", Class.class).invoke(modules, flightClass);
            return flight != null && Boolean.TRUE.equals(flight.getClass().getMethod("isActive").invoke(flight));
         } catch (Throwable ignored) {
            return false;
         }
      }
   }

   static final class Inner_HashUtil extends Module {
      Inner_HashUtil() {
         super("hide", "PanicMode", ae.write, "Temporarily hides client modules and UI.");
      }

      @Override
      public void onEnable() {
         aD.init_V(this);
         this.init();
      }

      @Override
      public void onDisable() {
         aD.load_V(this);
         this.init();
      }

      private void init() {
         if (MC != null) {
            cG.load(MC);
            if (!AutismLiteVariant.enabled() && MC.gui.screen() instanceof autismclient.gui.d.UtilCore) {
               MC.gui.setScreen(new autismclient.gui.d.UtilCore());
            }
         }
      }

      @Override
      public boolean emitsToggleMessage() {
         return false;
      }
   }

   static final class Inner_IoWorker extends Module {
      Inner_IoWorker() {
         super("hud", "HUD", ae.write, "Shows HUD elements.");
         boolean firstInstall = !AutismConfig.getGlobal().modules.containsKey("hud");
         this.add(new ActionSetting("edit-hud", "Edit HUD", this::init).availableOffline().description("Open HUD editor.").group("Editor"));
         this.add(new BoolSetting("editor-grid", "Editor Grid", true).description("Show editor grid.").group("Editor"));
         this.add(
            new BoolSetting("custom-font", "Custom Font", false)
               .description("Render HUD text with the client's custom font instead of the default Minecraft font.")
               .group("Font")
         );
         this.add(new BoolSetting("hide-in-guis", "Hide In GUIs", true).description("Hide in screens.").group("Visibility"));
         this.add(new BoolSetting("show-in-chat", "Show In Chat", false).description("Show in chat.").group("Visibility"));
         this.add(new BoolSetting("show-in-pause", "Show In Pause", false).description("Show in pause.").group("Visibility"));
         if (firstInstall) {
            this.setEnabledSilently(true);
         }
      }

      @Override
      public void onEnable() {
         autismclient.util.al.init_V();
         autismclient.util.macro.ab.load_V();
      }

      @Override
      public void tick() {
         autismclient.util.al.read_V();
      }

      @Override
      protected void onSettingsReset() {
         autismclient.util.al.update_V();
      }

      private void init() {
         autismclient.util.al.init_V();
         MC.gui.setScreen(new autismclient.gui.d.RenderState(MC.gui.screen()));
      }
   }

   static final class Inner_JsonUtil extends Module {
      Inner_JsonUtil() {
         super("block-esp", "BlockESP", ae.update, "Highlights selected blocks.");
         this.add(new IntSetting("max-targets", "Max Blocks", 1024, 64, 8192, 64).group("General").description("Max rendered blocks"));
         this.add(new BoolSetting("fill", "Fill", false).group("General").description("Draw translucent filled boxes."));
         this.add(new BoolSetting("meshing", "Meshing", true).group("General").description("Merge adjacent targets."));
         this.add(new BoolSetting("tracers", "Tracers", false).group("Tracers").description("Line to each target"));
         this.add(
            RegistryListSetting.blocks("blocks", "Blocks", "minecraft:diamond_ore")
               .group("Blocks")
               .description("Blocks to highlight; each gets its own color.")
         );
         this.add(new BoolSetting("barriers", "Barriers", false).group("Blocks").description("Show invisible barrier blocks."));
         this.add(new ColorSetting("color", "Color", -855688389).group("Colors").visibleWhen(() -> false));
         this.init();
      }

      @Override
      public void onOptionValueChanged(String settingId) {
         if ("blocks".equals(settingId)) {
            this.init();
         }
      }

      private void init() {
         int legacy = am.init_I_Module(this, "color", -855688389);
         boolean carry = legacy != -855688389;

         for (String id : ad.init(this)) {
            if (ad.init_Block(id) != null) {
               String key = "color-" + id;
               boolean fresh = this.value(key) == null;
               if (this.setting(key) == null) {
                  String label = cc.load(id);
                  this.add(new ColorSetting(key, label.isBlank() ? id : label, ad.init_I(id)).group("Colors").visibleWhen(() -> ad.init_Z(this, id)));
               }

               if (carry && fresh) {
                  this.setValueTransient(key, String.format(Locale.ROOT, "%08X", legacy));
               }
            }
         }

         if (carry) {
            this.setValueTransient("color", String.format(Locale.ROOT, "%08X", -855688389));
         }
      }
   }

   static final class Inner_KeyHolder extends Module {
      private String init_Str = "";
      private int init_I = -1;

      Inner_KeyHolder() {
         super("xray", "Xray", ae.update, "Shows selected blocks.");
         this.add(new ChoiceSetting("render-style", "Rendering Style", "Default", "Default", "ESP").description("See-through or boxes.").build());
         this.add(RegistryListSetting.blocks("whitelist", "Whitelist", init_Str_12()).description("Blocks to show.").build());
         this.add(new IntSetting("opacity", "Opacity", 25, 0, 255, 1).description("Hidden block alpha.").visibleWhen(this::init_Z_43).build());
         this.add(
            new ChoiceSetting("fluid-opacity", "Fluid Opacity", "Both", "None", "Water", "Lava", "Both")
               .description("Visible fluids.")
               .visibleWhen(this::init_Z_43)
               .build()
         );
         this.add(new BoolSetting("exposed-only", "Exposed Only", false).description("Only exposed ores.").visibleWhen(this::init_Z_43).build());
         this.add(new BoolSetting("fill", "Fill", true).description("Shade box interiors.").visibleWhen(this::load).build());

         for (autismclient.util.oresim.CachePool.Inner_DataUtil kind : autismclient.util.oresim.CachePool.Inner_DataUtil.values()) {
            this.add(new ColorSetting(kind.init(), kind.load_Str, kind.init_I).group("Colors").visibleWhen(() -> this.load() && ak.init_Z(this, kind)).build());
         }

         this.init_V_40();
      }

      private boolean init_Z_43() {
         return !this.load();
      }

      private boolean load() {
         return "ESP".equals(this.value("render-style"));
      }

      private boolean read() {
         return false;
      }

      @Override
      protected String externalSettingValue(String settingId) {
         if (!"oresim-seed".equals(settingId)) {
            return null;
         }

         AutismConfig.ModuleState configured = AutismConfig.getGlobal().modules.get(this.id());
         String legacy = configured != null && configured.settings != null ? configured.settings.getOrDefault(settingId, "") : "";
         return AutismOreSimSeedStore.init_AutismOreSimSeedSt().init_Str(AutismWaypoints.init_Str_Minecraft(MC), legacy);
      }

      @Override
      protected boolean setExternalSettingValue(String settingId, String value) {
         if (!"oresim-seed".equals(settingId)) {
            return false;
         }

         AutismOreSimSeedStore.init_AutismOreSimSeedSt().init_V(AutismWaypoints.init_Str_Minecraft(MC), value);
         return true;
      }

      @Override
      public void onOptionValueChanged(String settingId) {
         if ("whitelist".equals(settingId)) {
            this.init_V_40();
         }

         if ("oresim-seed".equals(settingId)) {
            autismclient.util.oresim.StreamUtil.init_V();
         }
      }

      private void init_V_40() {
         for (String id : ak.init_List(this)) {
            String key = "block-color-" + id;
            if (this.setting(key) == null) {
               String label = cc.load(id);
               this.add(
                  new ColorSetting(key, label.isBlank() ? id : label, -3355444)
                     .group("Colors")
                     .visibleWhen(() -> this.load() && !this.read() && ak.init(this, id))
                     .build()
               );
            }
         }
      }

      @Override
      public void onEnable() {
         if (this.init_Z_43()) {
            am.init_V();
         }
      }

      @Override
      public void onDisable() {
         this.init_Str = "";
         autismclient.util.oresim.StreamUtil.load_V();
         if (this.init_Z_43()) {
            am.init_V();
         }
      }

      @Override
      public void onGameLeft() {
         autismclient.util.oresim.StreamUtil.init_V();
         autismclient.util.oresim.StreamUtil.write_V();
         autismclient.util.oresim.DataUtil.init();
      }

      @Override
      public void tick() {
         String tintKey = this.value("mode")
            + "|"
            + this.value("render-style")
            + "|"
            + this.value("fluid-opacity")
            + "|"
            + this.bool("exposed-only")
            + "|"
            + (this.read() ? "" : this.value("whitelist"));
         int opacity = this.integer("opacity");
         if (!tintKey.equals(this.init_Str)) {
            boolean var10000 = this.init_Str.isEmpty();
            this.init_Str = tintKey;
            this.init_I = opacity;
            if (!var10000) {
               am.init_V();
            }
         } else if (opacity != this.init_I) {
            this.init_I = opacity;
            am.load_V();
         }

         ak.init(this, MC.level, MC.player);
      }

      @Override
      public String info() {
         return ak.init_Str(this);
      }

      private static String init_Str_12() {
         return String.join("|", autismclient.util.oresim.CachePool.init_List);
      }
   }

   static final class Inner_ListPool extends Module {
      private int init_I;
      private double init_D;
      private double load_D;
      private long init_J;
      private String init_Str = "Vanilla";
      private Boolean init_Boolean;
      private long load_J;

      Inner_ListPool() {
         super("speed", "Speed", ae.load_ae, "Boosts movement speed.");
         this.add(new ChoiceSetting("mode", "Mode", "Vanilla", "Vanilla", "Strafe").description("Speed method").build());
         this.add(
            new DoubleSetting("vanilla-speed", "Vanilla Speed", 5.6, 0.0, 20.0, 0.1)
               .visibleWhen(() -> "Vanilla".equals(this.value("mode")))
               .description("Blocks per second")
               .build()
         );
         this.add(
            new DoubleSetting("strafe-speed", "Strafe Speed", 1.6, 0.0, 3.0, 0.05)
               .visibleWhen(() -> "Strafe".equals(this.value("mode")))
               .description("Strafe speed multiplier.")
               .build()
         );
         this.add(
            new BoolSetting("speed-limit", "Speed Limit", false).visibleWhen(() -> "Strafe".equals(this.value("mode"))).description("Limit max speed.").build()
         );
         this.add(new DoubleSetting("timer", "Timer", 1.0, 0.01, 10.0, 0.05).description("Client timer speed."));
         this.add(new BoolSetting("in-liquids", "In Liquids", false));
         this.add(new BoolSetting("when-sneaking", "When Sneaking", false));
         this.add(new BoolSetting("only-on-ground", "Only Ground", false).visibleWhen(() -> "Vanilla".equals(this.value("mode"))).build());
      }

      @Override
      public void onEnable() {
         this.init_Str = this.choice("mode");
         this.load_V_30();
      }

      @Override
      public void onDisable() {
         this.load_V_30();
      }

      @Override
      public void preMovementTick() {
         if (MC.player != null && !this.update() && !this.load_Z_22()) {
            this.init_V_41();
            if (this.write()) {
               this.load_V_30();
            } else {
               this.load_D = Math.sqrt(
                  (MC.player.getX() - MC.player.xo) * (MC.player.getX() - MC.player.xo) + (MC.player.getZ() - MC.player.zo) * (MC.player.getZ() - MC.player.zo)
               );
            }
         }
      }

      @Override
      public Vec3 onPlayerMove(MoverType type, Vec3 movement) {
         if (type != MoverType.SELF || MC.player == null || MC.options == null || movement == null) {
            return movement;
         }

         if (!this.update() && !this.load_Z_22()) {
            this.init_V_41();
            if (this.write()) {
               return this.read(movement);
            } else {
               return "Strafe".equals(this.choice("mode")) ? this.write(movement) : this.load(movement);
            }
         } else {
            return movement;
         }
      }

      @Override
      public boolean onPacketReceive(Packet<?> packet) {
         if (packet instanceof ClientboundPlayerPositionPacket) {
            this.load_V_30();
         }

         return false;
      }

      @Override
      public boolean shouldApplySpeedTimer() {
         return !this.update() && !this.load_Z_22() && this.read();
      }

      @Override
      public float speedTimerMultiplier() {
         if (!this.shouldApplySpeedTimer()) {
            return 1.0F;
         }

         try {
            return Math.max(0.01F, Math.min(10.0F, (float)this.decimal("timer")));
         } catch (Exception ignored) {
            return 1.0F;
         }
      }

      private Vec3 load(Vec3 movement) {
         Vec3 horizontal = this.init_Vec3(this.decimal("vanilla-speed"));
         double x = horizontal.x;
         double z = horizontal.z;
         if (MC.player.hasEffect(MobEffects.SPEED)) {
            MobEffectInstance effect = MC.player.getEffect(MobEffects.SPEED);
            if (effect != null) {
               double multiplier = (effect.getAmplifier() + 1) * 0.205;
               x += x * multiplier;
               z += z * multiplier;
            }
         }

         return new Vec3(x, movement.y, z);
      }

      private Vec3 read(Vec3 movement) {
         if (!"Strafe".equals(this.choice("mode"))) {
            return this.load(movement);
         }

         double liquidSpeed = this.init_D_12() * Math.max(0.0, this.decimal("strafe-speed"));
         Vec3 horizontal = this.load(liquidSpeed);
         return new Vec3(horizontal.x, movement.y, horizontal.z);
      }

      Vec3 init(Vec3 movement) {
         if (movement == null || MC.player == null || !this.bool("in-liquids") || !this.write()) {
            return movement;
         } else if (!this.update() && !this.load_Z_22()) {
            this.init_V_41();
            return this.read(movement);
         } else {
            return movement;
         }
      }

      private Vec3 write(Vec3 movement) {
         if (this.init_I == 0 && this.read()) {
            this.init_I = 1;
            this.init_D = 1.1799999475479126 * this.init_D_12() - 0.01;
         }

         if (this.init_I == 1) {
            if (this.read() && MC.player.onGround()) {
               movement = new Vec3(movement.x, this.init_D(0.40123128), movement.z);
               this.init_D = this.init_D * this.decimal("strafe-speed");
               this.init_I = 2;
            }
         } else if (this.init_I == 2) {
            this.init_D = this.load_D - 0.76 * (this.load_D - this.init_D_12());
            this.init_I = 3;
         } else if (this.init_I == 3) {
            if (!this.init_Z_44() || MC.player.verticalCollision && this.init_I > 0) {
               this.init_I = 0;
            }

            this.init_D = this.load_D - this.load_D / 159.0;
         } else if (this.init_I > 3 || this.init_I < 0) {
            this.init_I = 0;
         }

         this.init_D = Math.max(this.init_D, this.init_D_12());
         if (this.bool("speed-limit")) {
            long now = System.currentTimeMillis();
            if (now - this.init_J > 2500L) {
               this.init_J = now;
            }

            this.init_D = Math.min(this.init_D, now - this.init_J > 1250L ? 0.44 : 0.43);
         }

         Vec3 horizontal = this.load(this.init_D);
         return new Vec3(horizontal.x, movement.y, horizontal.z);
      }

      private Vec3 init_Vec3(double blocksPerSecond) {
         if (MC.player != null && MC.player.input != null) {
            double speed = blocksPerSecond / 20.0;
            Vec3 forward = Vec3.directionFromRotation(0.0F, MC.player.getYRot());
            Vec3 right = Vec3.directionFromRotation(0.0F, MC.player.getYRot() + 90.0F);
            double x = 0.0;
            double z = 0.0;
            boolean hasForward = false;
            if (MC.options.keyUp.isDown()) {
               x += forward.x * speed;
               z += forward.z * speed;
               hasForward = true;
            }

            if (MC.options.keyDown.isDown()) {
               x -= forward.x * speed;
               z -= forward.z * speed;
               hasForward = true;
            }

            boolean hasSide = false;
            if (MC.options.keyRight.isDown()) {
               x += right.x * speed;
               z += right.z * speed;
               hasSide = true;
            }

            if (MC.options.keyLeft.isDown()) {
               x -= right.x * speed;
               z -= right.z * speed;
               hasSide = true;
            }

            if (hasForward && hasSide) {
               double diagonal = 1.0 / Math.sqrt(2.0);
               x *= diagonal;
               z *= diagonal;
            }

            return new Vec3(x, 0.0, z);
         } else {
            return Vec3.ZERO;
         }
      }

      private Vec3 load(double speed) {
         if (MC.player != null && MC.player.input != null) {
            Vec2 input = MC.player.input.getMoveVector();
            float forward = Math.signum(input.y);
            float side = Math.signum(input.x);
            if (forward == 0.0F && side == 0.0F) {
               return Vec3.ZERO;
            }

            float yaw = MC.player.getYRot(this.init_F_8());
            float strafe = 90.0F * side;
            if (forward != 0.0F) {
               strafe *= forward * 0.5F;
            }

            yaw -= strafe;
            if (forward < 0.0F) {
               yaw -= 180.0F;
            }

            double radians = Math.toRadians(yaw);
            return new Vec3(-Math.sin(radians) * speed, 0.0, Math.cos(radians) * speed);
         } else {
            return Vec3.ZERO;
         }
      }

      private float init_F_8() {
         try {
            return MC.getDeltaTracker().getGameTimeDeltaPartialTick(true);
         } catch (Exception ignored) {
            return 1.0F;
         }
      }

      private double init_D_12() {
         double speed = 0.2873;
         if (MC.player.hasEffect(MobEffects.SPEED)) {
            MobEffectInstance effect = MC.player.getEffect(MobEffects.SPEED);
            if (effect != null) {
               speed *= 1.0 + 0.2 * (effect.getAmplifier() + 1);
            }
         }

         if (MC.player.hasEffect(MobEffects.SLOWNESS)) {
            MobEffectInstance effect = MC.player.getEffect(MobEffects.SLOWNESS);
            if (effect != null) {
               speed /= 1.0 + 0.2 * (effect.getAmplifier() + 1);
            }
         }

         return speed;
      }

      private double init_D(double base) {
         MobEffectInstance effect = MC.player.getEffect(MobEffects.JUMP_BOOST);
         return effect == null ? base : base + (effect.getAmplifier() + 1) * 0.1F;
      }

      private boolean init_Z_44() {
         return MC.level != null && MC.level.noCollision(MC.player.getBoundingBox().move(0.0, MC.player.getDeltaMovement().y, 0.0));
      }

      private boolean load_Z_22() {
         if (MC.player == null) {
            return true;
         }

         if (MC.player.isFallFlying() || MC.player.onClimbable() || MC.player.getVehicle() != null) {
            return true;
         }

         if (!this.bool("when-sneaking") && MC.player.isShiftKeyDown()) {
            return true;
         }

         boolean inLiquid = this.write();
         boolean liquidSpeedAllowed = inLiquid && this.bool("in-liquids");
         return this.bool("only-on-ground") && !MC.player.onGround() && "Vanilla".equals(this.choice("mode")) && !liquidSpeedAllowed
            ? true
            : inLiquid && !liquidSpeedAllowed;
      }

      private boolean read() {
         if (MC.player == null) {
            return false;
         }

         if (MC.player.input != null) {
            Vec2 input = MC.player.input.getMoveVector();
            if (input.x != 0.0F || input.y != 0.0F) {
               return true;
            }
         }

         return MC.player.xxa != 0.0F || MC.player.zza != 0.0F;
      }

      private boolean write() {
         return MC.player != null && (MC.player.isInWater() || MC.player.isInLava());
      }

      private void init_V_41() {
         String mode = this.choice("mode");
         if (!mode.equals(this.init_Str)) {
            this.init_Str = mode;
            this.load_V_30();
         }
      }

      private boolean update() {
         long now = System.currentTimeMillis();
         if (this.init_Boolean != null && now - this.load_J < 250L) {
            return this.init_Boolean;
         }

         this.load_J = now;
         this.init_Boolean = this.sync();
         return this.init_Boolean;
      }

      private boolean sync() {
         try {
            Class<?> modulesClass = Class.forName("meteordevelopment.meteorclient.systems.modules.Modules", false, f.Inner_ListPool.class.getClassLoader());
            Class<?> speedClass = Class.forName(
               "meteordevelopment.meteorclient.systems.modules.movement.speed.Speed", false, f.Inner_ListPool.class.getClassLoader()
            );
            Object modules = modulesClass.getMethod("get").invoke(null);
            Object speed = modulesClass.getMethod("get", Class.class).invoke(modules, speedClass);
            return speed != null && Boolean.TRUE.equals(speed.getClass().getMethod("isActive").invoke(speed));
         } catch (Throwable ignored) {
            return false;
         }
      }

      private void load_V_30() {
         this.init_I = 0;
         this.init_D = 0.2873;
         this.load_D = 0.0;
      }
   }

   static final class Inner_MapStore extends Module {
      Inner_MapStore() {
         super("sprint", "Sprint", ae.load_ae, "Sprints automatically.");
         this.add(new BoolSetting("ignore-blindness", "Blindness", false).description("Sprint while blind").group("Ignore").build());
         this.add(new BoolSetting("ignore-collision", "Collision", false).description("Sprint against walls").group("Ignore").build());
      }

      boolean init(boolean original, boolean movementTick) {
         boolean sprint = original;
         if (this.flush()) {
            sprint = true;
         }

         if (movementTick && this.sync()) {
            sprint = false;
         }

         return sprint;
      }

      private boolean sync() {
         if (((AutismLocalPlayerAccessor)MC.player).autism$isSlowDueToUsingItem()) {
            return true;
         }

         if (MC.player.isShiftKeyDown()) {
            return true;
         }

         ce.Inner_DataUtil current = cn.init_ce_DataUtil();
         if (current == null) {
            return false;
         }

         float deltaYawRad = (MC.player.getYRot() - current.init()) * 0.017453292F;
         Vec2 move = MC.player.input.getMoveVector();
         boolean hasForwardMovement = move.y * Mth.cos(deltaYawRad) + move.x * Mth.sin(deltaYawRad) > 1.0E-5F;
         return !hasForwardMovement;
      }

      boolean init() {
         return this.sync();
      }

      boolean load() {
         return false;
      }

      boolean read() {
         return false;
      }

      boolean write() {
         return this.bool("ignore-collision");
      }

      boolean update() {
         return this.bool("ignore-blindness");
      }

      private boolean flush() {
         return MC.player.input.getMoveVector().lengthSquared() > 0.0F;
      }
   }

   static final class Inner_MethodUtil extends Module {
      private boolean init;

      Inner_MethodUtil() {
         super("parkour", "Parkour", ae.load_ae, "Jumps at block edges.");
      }

      @Override
      public void tick() {
         if (MC.player != null && MC.level != null && (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())) {
            if (this.init) {
               autismclient.util.aB.init(MC.options.keyJump).autism$simulatePress(false);
               this.init = false;
            } else if (MC.player.onGround()
               && !MC.player.isShiftKeyDown()
               && !MC.options.keyShift.isDown()
               && !MC.options.keyJump.isDown()
               && !(MC.player.input.getMoveVector().lengthSquared() <= 0.0F)
               && !this.init()) {
               autismclient.util.aB.init(MC.options.keyJump).autism$simulatePress(true);
               this.init = true;
            }
         }
      }

      private boolean init() {
         Vec3 d = MC.player.getDeltaMovement();
         AABB next = MC.player.getBoundingBox().move(d.x, d.y - 0.08, d.z).expandTowards(0.0, -0.02, 0.0);
         return !MC.level.noCollision(MC.player, next);
      }

      @Override
      public void onDisable() {
         if (this.init) {
            this.init = false;
            if (MC != null && MC.options != null) {
               autismclient.util.aB.init(MC.options.keyJump).autism$simulatePress(false);
            }
         }
      }
   }

   static final class Inner_NetHelper extends Module {
      private ItemStack init_ItemStack = ItemStack.EMPTY;
      private int init_I = -1;

      Inner_NetHelper() {
         super("better-tooltips", "BetterTooltips", ae.update, "Adds item tooltip info.");
         boolean firstInstall = !AutismConfig.getGlobal().modules.containsKey("better-tooltips");
         this.add(new StringSetting("nbt-shortcut", "NBT Shortcut", "Ctrl+Shift+Right-click").group("Info").readonlySummary());
         this.add(new BoolSetting("block-nbt-inspect", "Block NBT Inspect", false).group("Info").description("Ctrl+Shift+Right-click blocks.").build());
         this.add(new BoolSetting("containers", "Containers", true).group("Containers"));
         this.add(new BoolSetting("maps", "Maps", true).group("Items"));
         this.add(new BoolSetting("books", "Books", true).group("Items"));
         this.add(new BoolSetting("banners", "Banners", true).group("Items"));
         this.add(new BoolSetting("buckets", "Buckets", true).group("Items"));
         this.add(new BoolSetting("bundles", "Bundles", true).group("Items"));
         this.add(new BoolSetting("minecraft-name", "Minecraft Name", true).group("Info"));
         this.add(new BoolSetting("durability", "Durability", true).group("Info"));
         this.add(new BoolSetting("food-info", "Food Info", true).group("Info"));
         this.add(new BoolSetting("byte-size", "Byte Size", true).group("Info"));
         this.add(new ChoiceSetting("byte-size-format", "Byte Format", "Both", "Raw", "Rounded", "Both").group("Info").build());
         if (firstInstall) {
            this.setEnabledSilently(true);
         }
      }

      @Override
      public void appendTooltip(ItemStack stack, List<?> lines) {
         if (stack != null && !stack.isEmpty()) {
            List raw = lines;
            raw.add(Component.literal("NBT: Ctrl+Shift+Right-click").withStyle(ChatFormatting.DARK_GRAY));
            if (this.bool("minecraft-name")) {
               raw.add(Component.literal(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            }

            if (this.bool("durability") && stack.isDamageableItem()) {
               int maxDamage = Math.max(1, stack.getMaxDamage());
               int remaining = Math.max(0, maxDamage - stack.getDamageValue());
               raw.add(Component.literal("Durability: " + remaining + " / " + maxDamage).withStyle(ChatFormatting.GRAY));
            }

            if (this.bool("food-info") && stack.has(DataComponents.FOOD)) {
               FoodProperties food = stack.get(DataComponents.FOOD);
               if (food != null) {
                  raw.add(
                     Component.literal("Food: " + food.nutrition() + " / " + String.format(Locale.ROOT, "%.1f", food.saturation()))
                        .withStyle(ChatFormatting.GRAY)
                  );
               }
            }

            if (this.bool("containers") && stack.has(DataComponents.CONTAINER)) {
               ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
               if (contents != null) {
                  long stacks = contents.nonEmptyItemCopyStream().count();
                  raw.add(Component.literal("Container: " + stacks + " stacks").withStyle(ChatFormatting.DARK_GRAY));
               }
            }

            if (this.bool("bundles") && stack.has(DataComponents.BUNDLE_CONTENTS)) {
               BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
               if (contents != null) {
                  raw.add(Component.literal("Bundle: " + contents.size() + " stacks").withStyle(ChatFormatting.DARK_GRAY));
               }
            }

            if (this.bool("maps") && stack.has(DataComponents.MAP_ID)) {
               raw.add(Component.literal("Map: " + stack.get(DataComponents.MAP_ID)).withStyle(ChatFormatting.DARK_GRAY));
            }

            if (this.bool("banners") && stack.has(DataComponents.BANNER_PATTERNS)) {
               raw.add(Component.literal("Banner patterns: " + stack.get(DataComponents.BANNER_PATTERNS)).withStyle(ChatFormatting.DARK_GRAY));
            }

            if (this.bool("buckets") && stack.has(DataComponents.BUCKET_ENTITY_DATA)) {
               raw.add(Component.literal("Bucket entity data").withStyle(ChatFormatting.DARK_GRAY));
            }

            if (this.bool("books") && stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
               WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
               if (content != null) {
                  raw.add(Component.literal("Pages: " + content.pages().size()).withStyle(ChatFormatting.DARK_GRAY));
               }
            } else if (this.bool("books") && stack.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
               WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
               if (content != null) {
                  raw.add(Component.literal("Pages: " + content.pages().size()).withStyle(ChatFormatting.DARK_GRAY));
               }
            }

            if (this.bool("byte-size")) {
               int bytes = this.init(stack);
               if (bytes >= 0) {
                  raw.add(Component.literal("Bytes: " + this.init(bytes)).withStyle(ChatFormatting.DARK_GRAY));
               } else {
                  raw.add(Component.literal("Bytes: unreadable (protected)").withStyle(ChatFormatting.DARK_GRAY));
               }
            }
         }
      }

      private String init(int bytes) {
         String mode = this.choice("byte-size-format");
         double kib = bytes / 1024.0;
         String var10000;
         switch (mode) {
            case "Raw":
               var10000 = Integer.toString(bytes);
               break;
            case "Rounded":
               var10000 = String.format(Locale.ROOT, "%.1f KiB", kib);
               break;
            default:
               var10000 = bytes + " (" + String.format(Locale.ROOT, "%.1f KiB", kib) + ")";
         }

         return var10000;
      }

      private int init(ItemStack stack) {
         if (!this.init_ItemStack.isEmpty()
            && this.init_ItemStack.getCount() == stack.getCount()
            && ItemStack.isSameItemSameComponents(this.init_ItemStack, stack)) {
            return this.init_I;
         } else if (MC != null && MC.player != null) {
            int bytes = autismclient.g.StateCache.init(stack, MC.player.registryAccess());
            this.init_ItemStack = stack.copy();
            this.init_I = bytes;
            return bytes;
         } else {
            return -1;
         }
      }
   }

   static final class Inner_NodeWalker extends Module {
      Inner_NodeWalker() {
         super("xcarry", "XCarry", ae.read, "Keeps stored items in crafting / result / armor / offhand slots when closing inventory.");
         this.add(new BoolSetting("use-crafting", "Use Crafting Grid", true).description("Use all craft slots").build());
         this.add(new BoolSetting("use-armor", "Use Armor Slots", true).description("Use armor slots 5-8.").build());
         this.add(new BoolSetting("use-offhand", "Use Offhand", true).description("Use offhand slot.").build());
         this.add(new BoolSetting("carry-cursor", "Carry Cursor", true).description("Keep cursor stack.").build());
      }

      @Override
      public void onEnable() {
         AutismConfig.getGlobal().xCarry = true;
      }

      @Override
      public void onDisable() {
         AutismConfig.getGlobal().xCarry = false;
      }
   }

   static final class Inner_PacketUtil extends Module {
      private String init_Str = "";
      private String load = "";
      private int init_I = -2147483648;
      private boolean init_Z;

      Inner_PacketUtil() {
         super("fullbright", "Fullbright", ae.update, "Brightens the world.");
         boolean firstInstall = !AutismConfig.getGlobal().modules.containsKey("fullbright");
         this.add(new ChoiceSetting("mode", "Mode", "Gamma", "Gamma", "Potion", "Luminance").description("How lighting is boosted.").build());
         this.add(new ChoiceSetting("light-type", "Light Type", "BLOCK", "SKY", "BLOCK").visibleWhen(() -> "Luminance".equals(this.value("mode"))).build());
         this.add(new IntSetting("minimum-light-level", "Min Light", 8, 0, 15, 1).visibleWhen(() -> "Luminance".equals(this.value("mode"))).build());
         if (firstInstall) {
            this.setEnabledSilently(true);
         }
      }

      @Override
      public void onEnable() {
         if ("Luminance".equals(this.choice("mode"))) {
            am.init_V();
         }
      }

      @Override
      public void onDisable() {
         if (this.init_Z) {
            this.load();
         }

         this.init_Z = false;
         if ("Luminance".equals(this.choice("mode")) || "Luminance".equals(this.init_Str)) {
            am.init_V();
         }
      }

      @Override
      public void tick() {
         String mode = this.choice("mode");
         String lightType = this.choice("light-type");
         int minimumLightLevel = this.integer("minimum-light-level");
         String prevMode = this.init_Str;
         boolean switched = !mode.equals(prevMode) || !lightType.equals(this.load);
         if (switched || minimumLightLevel != this.init_I) {
            this.init_Str = mode;
            this.load = lightType;
            this.init_I = minimumLightLevel;
            if (!"Potion".equals(mode) && this.init_Z) {
               this.load();
               this.init_Z = false;
            }

            if ("Luminance".equals(mode) || "Luminance".equals(prevMode)) {
               if (switched) {
                  am.init_V();
               } else {
                  am.load_V();
               }
            }
         }

         if ("Potion".equals(mode)) {
            this.init_V_42();
         }
      }

      public boolean init_Z_45() {
         return this.isEnabled() && "Gamma".equals(this.choice("mode"));
      }

      public int init(String lightType) {
         return this.isEnabled() && "Luminance".equals(this.choice("mode")) && this.choice("light-type").equals(lightType)
            ? this.integer("minimum-light-level")
            : 0;
      }

      private void init_V_42() {
         if (MC.player != null) {
            MobEffectInstance instance = MC.player.getEffect(MobEffects.NIGHT_VISION);
            if (instance != null) {
               if (instance.getDuration() < 420) {
                  ((AutismMobEffectInstanceAccessor)instance).autism$setDuration(420);
               }
            } else {
               MC.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 420, 0));
            }

            this.init_Z = true;
         }
      }

      private void load() {
         if (MC.player != null && MC.player.hasEffect(MobEffects.NIGHT_VISION)) {
            MC.player.removeEffect(MobEffects.NIGHT_VISION);
         }
      }
   }

   static final class Inner_PathHelper extends Module {
      private boolean init;

      Inner_PathHelper() {
         super("sneak", "Sneak", ae.load_ae, "Keeps sneak held for you.");
         this.add(new ChoiceSetting("mode", "Mode", "Vanilla", "Vanilla", "Packet").description("Which method to sneak.").build());
      }

      boolean init_Z_46() {
         return this.isEnabled()
            && (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
            && !"Packet".equals(this.choice("mode"))
            && MC != null
            && MC.options != null
            && MC.player != null
            && !MC.player.getAbilities().flying;
      }

      @Override
      public void tick() {
         this.init_V_43();
      }

      @Override
      public void preMovementTick() {
         if (!"Packet".equals(this.choice("mode"))) {
            this.init_V_43();
         }
      }

      private void init_V_43() {
         if ((AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
            && MC.options != null
            && MC.player != null
            && !MC.player.getAbilities().flying) {
            if ("Packet".equals(this.choice("mode"))) {
               this.init(true);
               this.init = true;
            } else {
               MC.options.keyShift.setDown(true);
            }
         }
      }

      @Override
      public void onDisable() {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            this.init = false;
         } else {
            if (this.init) {
               this.init(false);
               this.init = false;
            }

            if (MC.options != null) {
               MC.options.keyShift.setDown(false);
            }
         }
      }

      private void init(boolean sneak) {
         if (MC.options != null && MC.getConnection() != null) {
            Input input = new Input(
               MC.options.keyUp.isDown(),
               MC.options.keyDown.isDown(),
               MC.options.keyLeft.isDown(),
               MC.options.keyRight.isDown(),
               MC.options.keyJump.isDown(),
               sneak,
               MC.options.keySprint.isDown()
            );
            MC.getConnection().send(new ServerboundPlayerInputPacket(input));
         }
      }

      @Override
      public void onGameLeft() {
         this.init = false;
         AuditClientFixes.releaseShiftKey();
      }

      @Override
      protected void onOptionValueChanged(String var1) {
         if ("mode".equals(var1)) {
            if ("Packet".equals(this.choice("mode"))) {
               if (this.isEnabled()) {
                  AuditClientFixes.releaseShiftKey();
               }
            } else if (this.init) {
               if (Module.MC != null) {
                  this.init(false);
               }

               this.init = false;
            }
         }
      }

      @Override
      protected void onSettingsReset() {
         this.onOptionValueChanged("mode");
      }
   }

   static final class Inner_PoolEntry extends Module {
      private BlockPos init_BlockPos;
      private Direction init_Direction = Direction.UP;
      private int init_I;

      Inner_PoolEntry() {
         super("instant-rebreak", "InstantRebreak", ae.read, "Rebreaks the last block.");
         this.add(new IntSetting("delay", "Delay", 0, 0, 20, 1).description("Delay between rebreak packets."));
         this.add(new BoolSetting("only-pick", "Only Pickaxe", true).description("Require pickaxe."));
         this.add(new BoolSetting("rotate", "Rotate", false).description("Face target first."));
         this.add(new BoolSetting("render", "Render", true).group("Render").description("Show the rebreak target."));
         this.add(new ColorSetting("color", "Color", -50373).group("Render").description("Single outline color.").build());
      }

      @Override
      public void onDisable() {
         autismclient.util.au.load();
         this.init_BlockPos = null;
      }

      @Override
      public void onStartBreakingBlock(BlockPos pos, Direction direction) {
         if (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z()) {
            this.init_BlockPos = pos == null ? null : pos.immutable();
            this.init_Direction = direction == null ? Direction.UP : direction;
            if (this.bool("render")) {
               autismclient.util.au.init(this.init_BlockPos, this.init("color", this.init("line-color", -12986502)));
            }

            this.init_I = 0;
         }
      }

      @Override
      public void tick() {
         if ((AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && MC.player != null
            && MC.level != null
            && MC.getConnection() != null
            && this.init_BlockPos != null
            && !AutoTotemModule.init_Z_6()) {
            if (!this.bool("render")) {
               autismclient.util.au.load();
            } else {
               autismclient.util.au.init(this.init_BlockPos, this.init("color", this.init("line-color", -12986502)));
            }

            if (!MC.level.isOutsideBuildHeight(this.init_BlockPos)
               && !MC.level.getBlockState(this.init_BlockPos).isAir()
               && (!this.bool("only-pick") || MC.player.getMainHandItem().is(ItemTags.PICKAXES))
               && this.init_I++ >= this.integer("delay")) {
               this.init_I = 0;
               if (this.bool("rotate")) {
                  MC.player.lookAt(Anchor.EYES, Vec3.atCenterOf(this.init_BlockPos));
               }

               BlockPos pos = this.init_BlockPos;
               Direction dir = this.init_Direction == null ? Direction.UP : this.init_Direction;
               if (MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
                  accessor.autism$startPrediction(MC.level, sequence -> new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, dir, sequence));
               } else {
                  MC.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos, dir));
               }

               MC.getConnection().send(new CompatSwingPacket(InteractionHand.MAIN_HAND));
            }
         }
      }

      private int init(String id, int fallback) {
         try {
            String value = this.value(id).replace("#", "");
            if (value.length() == 6) {
               value = "FF" + value;
            }

            return (int)Long.parseLong(value, 16);
         } catch (NumberFormatException ignored) {
            return fallback;
         }
      }

      @Override
      public void onGameLeft() {
         autismclient.util.au.load();
         this.init_BlockPos = null;
      }
   }

   static final class Inner_RefQueue extends Module {
      Inner_RefQueue() {
         super("spawner-esp", "SpawnerESP", ae.update, "Highlights monster spawners by spawned mob.");
         this.add(new BoolSetting("fill", "Fill", false).group("General").description("Draw translucent filled boxes."));
         this.add(new BoolSetting("meshing", "Meshing", true).group("General").description("Merge adjacent spawners."));
         this.add(new BoolSetting("tracers", "Tracers", false).group("Tracers").description("Line to each spawner"));
         this.add(RegistryListSetting.entityTypes("spawner-list", "Spawners", ao.init_Str).group("Spawners").description("Mobs to highlight."));

         for (ao.Inner_DataUtil target : ao.init_List) {
            String id = target.init_Str();
            this.add(new ColorSetting("color-" + id, target.load(), target.init_I()).group("Colors").visibleWhen(() -> ao.init_Z(this, id)));
         }
      }

      @Override
      public void onOptionValueChanged(String settingId) {
         if ("spawner-list".equals(settingId)) {
            this.init();
         }
      }

      private void init() {
         for (String id : ao.init(this)) {
            if (!ao.init(id)) {
               String key = "color-" + id;
               if (this.setting(key) == null) {
                  String label = cc.read(id);
                  this.add(new ColorSetting(key, label.isBlank() ? id : label, -855688389).group("Colors").visibleWhen(() -> ao.init_Z(this, id)));
               }
            }
         }
      }
   }

   static final class Inner_RenderState extends Module {
      private final Random init_Random = new Random();
      private BlockPos init_BlockPos;
      private boolean init_Z;

      Inner_RenderState() {
         super("fast-break", "FastBreak", ae.read, "Speeds up block breaking.");
         this.add(new ChoiceSetting("mode", "Mode", "Damage", "Normal", "Haste", "Damage", "Packet").description("Break method.").build());
         this.add(
            RegistryListSetting.blocks("blocks", "Blocks", "").visibleWhen(() -> !"Haste".equals(this.value("mode"))).description("Selected blocks.").build()
         );
         this.add(
            new ChoiceSetting("blocks-filter", "Blocks Filter", "Blacklist", "Whitelist", "Blacklist")
               .visibleWhen(() -> !"Haste".equals(this.value("mode")))
               .description("List mode.")
               .build()
         );
         this.add(
            new DoubleSetting("modifier", "Modifier", 1.4, 0.0, 999.0, 0.1)
               .sliderRange(0.0, 10.0)
               .visibleWhen(() -> init_Z_Module(this))
               .description("Speed multiplier.")
               .build()
         );
         this.add(
            new IntSetting("haste-amplifier", "Haste Amplifier", 2, 1, 10, 1).visibleWhen(() -> load(this)).description("Haste amplifier to apply.").build()
         );
         this.add(new BoolSetting("instamine", "Instamine", true).visibleWhen(() -> read(this)).description("Finish blocks early.").build());
         this.add(new BoolSetting("grim-bypass", "Grim Bypass", false).visibleWhen(() -> read(this)).description("Abort after stop.").build());
         this.add(
            new IntSetting("activation-chance", "Activation Chance", 100, 0, 100, 1)
               .visibleWhen(() -> write(this))
               .unit("%")
               .formatter(v -> v + "%")
               .description("% to fast-break.")
               .build()
         );
      }

      @Override
      public String info() {
         return this.choice("mode");
      }

      @Override
      public void onDisable() {
         this.load();
         this.init_BlockPos = null;
         this.init_Z = false;
      }

      @Override
      public void tick() {
         if (AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z()) {
            String mode = this.choice("mode");
            if ("Haste".equals(mode)) {
               this.init_V_44();
            } else {
               this.load();
            }

            if ("Damage".equals(mode)) {
               this.read();
            } else if ("Packet".equals(mode) && MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
               accessor.autism$setDestroyDelay(0);
            }
         }
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            return false;
         }

         if ("Damage".equals(this.choice("mode")) && this.bool("grim-bypass") && MC.getConnection() != null) {
            if (packet instanceof ServerboundPlayerActionPacket action && action.getAction() == Action.STOP_DESTROY_BLOCK) {
               MC.getConnection().send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, action.getPos().above(), action.getDirection()));
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public boolean onStartDestroyBlock(BlockPos pos, Direction direction) {
         if (!AutismLiteVariant.enabled() && autismclient.util.g.PoolEntry.init_Z()) {
            return false;
         }

         if (!"Damage".equals(this.choice("mode")) || !this.bool("instamine")) {
            return false;
         }

         if (MC.player != null && MC.level != null && MC.gameMode != null && MC.getConnection() != null && pos != null) {
            BlockState state = MC.level.getBlockState(pos);
            if (this.init(state, pos) && this.init_Z_BlockState_4(state)) {
               if (state.getDestroyProgress(MC.player, MC.level, pos) <= 0.5F) {
                  return false;
               } else if (MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
                  Direction dir = direction == null ? Direction.UP : direction;
                  MC.gameMode.destroyBlock(pos);
                  accessor.autism$startPrediction(
                     MC.level, sequence -> new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos.immutable(), dir, sequence)
                  );
                  accessor.autism$startPrediction(
                     MC.level, sequence -> new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos.immutable(), dir, sequence)
                  );
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

      @Override
      public void onBlockBreakingProgress(BlockPos pos, Direction direction) {
         if ((AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && "Packet".equals(this.choice("mode"))
            && MC.player != null
            && MC.level != null
            && MC.getConnection() != null
            && pos != null
            && MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor
            && !(accessor.autism$getDestroyProgress() >= 1.0F)) {
            BlockState state = MC.level.getBlockState(pos);
            if (this.init(state, pos) && this.init_Z_BlockState_4(state)) {
               if (!pos.equals(this.init_BlockPos)) {
                  this.init_BlockPos = pos.immutable();
                  this.init_Z = this.init_Random.nextDouble() * 100.0 < Math.max(0, this.integer("activation-chance"));
               }

               if (this.init_Z) {
                  Direction dir = direction == null ? Direction.UP : direction;
                  MC.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, pos.immutable(), dir));
               }
            }
         }
      }

      @Override
      public boolean shouldCancelStartBreakingBlock(BlockPos pos, Direction direction) {
         return false;
      }

      float init(float original, BlockState state, BlockPos pos) {
         if (!"Normal".equals(this.choice("mode"))) {
            return original;
         } else if (MC.player != null && MC.level != null && pos != null) {
            return this.init(state, pos) && this.init_Z_BlockState_4(state) ? (float)(original * Math.max(0.0, this.decimal("modifier"))) : original;
         } else {
            return original;
         }
      }

      boolean init_Z_47() {
         return "Normal".equals(this.choice("mode"));
      }

      private void init_V_44() {
         if (MC.player != null) {
            int amplifier = Math.max(1, this.integer("haste-amplifier"));
            MobEffectInstance haste = MC.player.getEffect(MobEffects.HASTE);
            if (haste == null || !haste.showIcon() || haste.getAmplifier() <= amplifier - 1) {
               MC.player.addEffect(new MobEffectInstance(MobEffects.HASTE, -1, amplifier - 1, false, false, false), null);
            }
         }
      }

      private void load() {
         if (MC.player != null) {
            MobEffectInstance haste = MC.player.getEffect(MobEffects.HASTE);
            if (haste != null && !haste.showIcon()) {
               MC.player.removeEffect(MobEffects.HASTE);
            }
         }
      }

      private void read() {
         if (MC.player != null
            && MC.level != null
            && MC.gameMode != null
            && this.bool("instamine")
            && MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
            BlockPos pos = accessor.autism$getDestroyBlockPos();
            if (pos != null && !(accessor.autism$getDestroyProgress() <= 0.0F)) {
               this.init_V_BlockPos_6(pos);
            }
         }
      }

      private void init_V_BlockPos_6(BlockPos pos) {
         if (MC.player != null && MC.level != null && MC.gameMode != null && pos != null && MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
            BlockState var6 = MC.level.getBlockState(pos);
            if (this.init(var6, pos) && this.init_Z_BlockState_4(var6)) {
               float progress = accessor.autism$getDestroyProgress();
               float delta = var6.getDestroyProgress(MC.player, MC.level, pos);
               if (progress > 0.0F && progress + delta >= 0.7F) {
                  accessor.autism$setDestroyProgress(1.0F);
               }
            }
         }
      }

      private boolean init(BlockState state, BlockPos pos) {
         return state != null && !state.isAir() && MC.level != null && state.getDestroySpeed(MC.level, pos) >= 0.0F;
      }

      private boolean init_Z_BlockState_4(BlockState state) {
         if (state == null) {
            return false;
         }

         List<String> configured = this.list("blocks");
         if (configured.isEmpty()) {
            return "Blacklist".equals(this.choice("blocks-filter"));
         }

         String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().toLowerCase(Locale.ROOT);
         String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
         boolean listed = false;

         for (String raw : configured) {
            String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty() && (value.equals(id) || value.equals(path))) {
               listed = true;
               break;
            }
         }

         return "Whitelist".equals(this.choice("blocks-filter")) ? listed : !listed;
      }

      private static boolean init_Z_Module(Module module) {
         return module != null && "Normal".equals(module.value("mode"));
      }

      private static boolean load(Module module) {
         return module != null && "Haste".equals(module.value("mode"));
      }

      private static boolean read(Module module) {
         return module != null && "Damage".equals(module.value("mode"));
      }

      private static boolean write(Module module) {
         return module != null && "Packet".equals(module.value("mode"));
      }

      @Override
      public void onGameLeft() {
         this.init_BlockPos = null;
         this.init_Z = false;
      }
   }

   static final class Inner_RingBuffer extends Module {
      Inner_RingBuffer() {
         super("item-esp", "ItemESP", ae.update, "Outlines dropped items with the shader outline.");
         this.add(new ChoiceSetting("mode", "Mode", "Shader", "Shader").description("Dropped item highlight style.").build());
         this.add(new ChoiceSetting("items-mode", "Items", "All", "All", "Some").group("General").description("Which items to outline").build());
         this.add(
            RegistryListSetting.items("items", "Item List", "")
               .group("General")
               .visibleWhen(() -> "Some".equals(this.value("items-mode")))
               .description("Item ids to outline")
               .build()
         );
         this.add(new DoubleSetting("max-distance", "Max Distance", 64.0, 0.0, 256.0, 1.0).group("General").description("0 = unlimited.").build());
         this.add(new DoubleSetting("fade-distance", "Fade Distance", 3.0, 0.0, 12.0, 0.25).group("General"));
         this.add(new ChoiceSetting("color-mode", "Color Mode", "Dynamic", "Dynamic", "Static").group("Colors").description("Color mode").build());
         this.add(new ColorSetting("color", "Color", -855648406).group("Colors").visibleWhen(() -> "Static".equals(this.value("color-mode"))));
      }
   }

   static final class Inner_StateCache extends Module {
      private int init_I;
      private int load;
      private final Random init_Random = new Random();

      Inner_StateCache() {
         super("bookbot", "BookBot", ae.write, "Writes into books.");
         this.add(new ChoiceSetting("mode", "Mode", "Random", "Random", "File").description("Random or file text.").build());
         this.add(
            new ChoiceSetting("random-type", "Random Type", "Utf8", "Utf8", "Ascii", "PaperMC").visibleWhen(() -> "Random".equals(this.value("mode"))).build()
         );
         this.add(
            new IntSetting("pages", "Pages", 100, 1, 100, 1)
               .visibleWhen(() -> "Random".equals(this.value("mode")) && !"PaperMC".equals(this.value("random-type")))
               .build()
         );
         this.add(
            new IntSetting("characters", "Chars/Page", 1024, 1, 1024, 16)
               .visibleWhen(() -> "Random".equals(this.value("mode")) && !"PaperMC".equals(this.value("random-type")))
               .build()
         );
         this.add(new IntSetting("delay", "Delay", 20, 1, 200, 1));
         this.add(new BoolSetting("sign", "Sign", true));
         this.add(new StringSetting("name", "Name", "Autism").visibleWhen(() -> Boolean.parseBoolean(this.value("sign"))).build());
         this.add(new BoolSetting("append-count", "Append Count", true).visibleWhen(() -> Boolean.parseBoolean(this.value("sign"))).build());
         this.add(new BoolSetting("word-wrap", "Word Wrap", true).visibleWhen(() -> "File".equals(this.value("mode"))).build());
         this.add(
            new StringSetting("file-path", "File", "")
               .visibleWhen(() -> "File".equals(this.value("mode")))
               .formatter(f.Inner_StateCache::init_Str_Str_3)
               .description("Selected file.")
               .filePicker("pick-file")
         );
         this.add(
            new ActionSetting("pick-file", "Pick File", this::load_V_31).availableOffline().visibleWhen(() -> false).description("Choose text file.").build()
         );
         this.add(new StringSetting("text", "Text", "").visibleWhen(() -> false).build());
         this.add(new BoolSetting("full-default-migrated", "Full Default Migrated", false).visibleWhen(() -> false).build());
      }

      @Override
      public void onEnable() {
         this.init_V_45();
         this.init_I = this.integer("delay");
         this.load = 0;
      }

      @Override
      public void tick() {
         if (MC.player != null && MC.getConnection() != null) {
            if ("File".equals(this.choice("mode"))) {
               String fileFailure = this.load_Str_6();
               if (fileFailure != null) {
                  this.disableWithToggleMessage(fileFailure);
                  return;
               }
            }

            if (this.init_I-- <= 0) {
               this.init_I = this.integer("delay");
               if (!this.init_Z_48()) {
                  this.disableWithToggleMessage("BookBot disabled: no empty writable book.");
               } else {
                  ItemStack stack = MC.player.getMainHandItem();
                  List<String> pages = this.init_List_16();
                  List<Filterable<Component>> filtered = new ArrayList<>();

                  for (String page : pages) {
                     filtered.add(Filterable.passThrough(Component.literal(page)));
                  }

                  String title = this.text("name").isBlank() ? "Autism" : this.text("name");
                  if (this.bool("append-count") && this.load > 0) {
                     title = title + " #" + this.load;
                  }

                  if (this.bool("sign")) {
                     stack.set(
                        DataComponents.WRITTEN_BOOK_CONTENT,
                        new WrittenBookContent(Filterable.passThrough(title), MC.player.getGameProfile().name(), 0, filtered, true)
                     );
                  } else {
                     List<Filterable<String>> writablePages = new ArrayList<>();

                     for (String page : pages) {
                        writablePages.add(Filterable.passThrough(page));
                     }

                     stack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(writablePages));
                  }

                  MC.getConnection()
                     .send(
                        new ServerboundEditBookPacket(
                           MC.player.getInventory().getSelectedSlot(), pages, this.bool("sign") ? Optional.of(title) : Optional.empty()
                        )
                     );
                  this.load++;
               }
            }
         }
      }

      private boolean init_Z_48() {
         if (MC.player == null) {
            return false;
         } else if (this.init_Z_ItemStack_8(MC.player.getMainHandItem())) {
            return true;
         } else {
            int slot = this.init_I_28();
            if (slot < 0) {
               return false;
            } else {
               int selected = MC.player.getInventory().getSelectedSlot();
               if (slot > 8) {
                  autismclient.util.aw.read(MC, slot, selected);
                  return true;
               } else {
                  autismclient.util.aw.init_V(MC, slot);
                  return true;
               }
            }
         }
      }

      private int init_I_28() {
         if (MC.player == null) {
            return -1;
         }

         for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            if (this.init_Z_ItemStack_8(MC.player.getInventory().getItem(i))) {
               return i;
            }
         }

         return -1;
      }

      private boolean init_Z_ItemStack_8(ItemStack stack) {
         if (stack != null && stack.is(Items.WRITABLE_BOOK)) {
            WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            return content == null || content.pages().isEmpty();
         } else {
            return false;
         }
      }

      private List<String> init_List_16() {
         List<String> pages = new ArrayList<>();
         String custom = "File".equals(this.choice("mode")) ? this.init_Str_13() : "";
         boolean randomMode = "Random".equals(this.choice("mode"));
         int pageCount = "File".equals(this.choice("mode")) ? 100 : Math.min(100, Math.max(1, this.integer("pages")));
         if (randomMode) {
            return PkgCls_b.init(pageCount, this.integer("characters"), this.choice("random-type"), this.init_Random);
         } else {
            int chars = this.integer("characters");
            if (!custom.isBlank() && this.bool("word-wrap")) {
               pages.addAll(autismclient.util.macro.u.init_List_Font(MC.font, custom, pageCount));
               return pages.isEmpty() ? List.of("Autism") : pages;
            } else {
               pages.addAll(autismclient.util.macro.u.init_List_Str(custom, pageCount, chars));
               return pages.isEmpty() ? List.of("Autism") : pages;
            }
         }
      }

      private void init_V_45() {
         if ("Random".equals(this.choice("mode")) && !this.bool("full-default-migrated")) {
            if (this.integer("pages") == 50) {
               this.setValue("pages", "100");
            }

            if (this.integer("characters") == 128 || this.integer("characters") == 512) {
               this.setValue("characters", Integer.toString(1024));
            }

            if ("Ascii".equals(this.choice("random-type"))) {
               this.setValue("random-type", "Utf8");
            }

            this.setValue("full-default-migrated", "true");
         }
      }

      private String init_Str_13() {
         String path = this.text("file-path").trim();
         if (path.isBlank()) {
            return "";
         }

         try {
            return autismclient.util.PkgCls_a.init(Path.of(path));
         } catch (Exception ex) {
            AutismClientMessaging.sendPrefixed("BookBot file read failed: " + ex.getMessage());
            return "";
         }
      }

      private void load_V_31() {
         String current = this.text("file-path").trim();
         PointerBuffer filters = BufferUtils.createPointerBuffer(4);
         ByteBuffer txt = MemoryUtil.memASCII("*.txt");
         ByteBuffer md = MemoryUtil.memASCII("*.md");
         ByteBuffer json = MemoryUtil.memASCII("*.json");
         ByteBuffer nbtTxt = MemoryUtil.memASCII("*.nbt.txt");
         filters.put(txt).put(md).put(json).put(nbtTxt).rewind();

         try {
            String selected = TinyFileDialogs.tinyfd_openFileDialog("BookBot Text File", current.isBlank() ? null : current, filters, "Text files", false);
            if (selected != null && !selected.isBlank()) {
               this.setValue("file-path", selected);
            }
         } finally {
            MemoryUtil.memFree(txt);
            MemoryUtil.memFree(md);
            MemoryUtil.memFree(json);
            MemoryUtil.memFree(nbtTxt);
         }
      }

      private String load_Str_6() {
         String path = this.text("file-path").trim();
         if (path.isBlank()) {
            return "BookBot disabled: choose a file first.";
         }

         try {
            Path file = Path.of(path);
            if (!Files.isRegularFile(file)) {
               return "BookBot disabled: file missing.";
            } else {
               return Files.size(file) <= 0L ? "BookBot disabled: file is empty." : null;
            }
         } catch (Exception ex) {
            return "BookBot disabled: invalid file.";
         }
      }

      private static String init_Str_Str_3(String value) {
         if (value != null && !value.isBlank()) {
            try {
               Path name = Paths.get(value).getFileName();
               return name == null ? value : name.toString();
            } catch (Exception ignored) {
               return value;
            }
         } else {
            return "No file selected";
         }
      }
   }

   static final class Inner_StreamUtil extends Module {
      private static final int read_I = 4;
      static final double init_D = 0.8;
      static final double load_D = 0.2;
      static final int init_I = 100;
      static final int load_I = 80;
      static final double read_D = 0.3;
      static final double write_D = 0.15;
      static final double update_D = 0.1;
      private autismclient.util.q init_q;
      private final autismclient.util.q.Inner_DataUtil init_q_DataUtil = new autismclient.util.q.Inner_DataUtil();
      private autismclient.util.ap.Inner_TaskQueue init_ap_TaskQueue;
      private double sync_D;
      private double flush_D;
      private int write_I = -1;
      private int update_I = 1000;
      private int sync_I = -1;
      private int flush_I = -1;
      private double close_D;
      private double open_D;
      private boolean init_Z;
      private final cj init_cj = new cj();
      private float init_F;
      private boolean load_Z;
      private int close_I = -2147483648;
      private int open_I = -2147483648;
      private int poll;
      private String init_Str = "";
      private Set<String> init_Set = Set.of();
      private final O init_O;

      Inner_StreamUtil() {
         super("aim-assist", "AimAssist", ae.init_ae, "Smoothly assists your aim toward configured entities.");
         this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").build());
         this.add(new DoubleSetting("range", "Range", 4.5, 1.0, 8.0, 0.1).build());
         this.add(new IntSetting("fov", "FOV", 80, 0, 180, 1).build());
         this.add(new ChoiceSetting("target-point", "Target Point", "Nearest", "Nearest", "Center", "Head", "Body", "Feet").build());
         this.add(new IntSetting("safe-zone", "Safe Zone", 5, 0, 100, 1).formatter(v -> v + "%").description("Deadzone around aim point.").build());
         this.add(new ChoiceSetting("priority", "Priority", "Smart", "Smart", "Direction", "Type", "Health", "Distance", "HurtTime", "Age").build());
         this.add(new BoolSetting("x-axis", "X Axis", true).description("Turns your aim sideways").build());
         this.add(new BoolSetting("y-axis", "Y Axis", false).description("Turns your aim up and down").build());
         this.add(new IntSetting("hurt-time", "Hurt Time", 10, 0, 10, 1).build());
         this.add(new BoolSetting("only-while-moving", "Only While Moving", false).description("Only while you move the mouse").build());
         this.init_O = new O(this);
         this.add(
            new ChoiceSetting("smooth-mode", "Smooth", "Human", "Human", "Instant")
               .group("Smoothing")
               .description("Human: reaction time, curved flicks, your own hand")
               .build()
         );
         this.add(
            new IntSetting("horizontal-speed", "Horizontal", 80, 1, 100, 1)
               .group("Smoothing")
               .formatter(v -> v + "%")
               .description("Sideways pull strength")
               .visibleWhen(() -> init_Z_Module_2(this) && this.bool("x-axis"))
               .build()
         );
         this.add(
            new IntSetting("vertical-speed", "Vertical", 80, 1, 100, 1)
               .group("Smoothing")
               .formatter(v -> v + "%")
               .description("Up and down pull strength")
               .visibleWhen(() -> init_Z_Module_2(this) && this.bool("y-axis"))
               .build()
         );
      }

      private static boolean init_Z_Module_2(Module module) {
         return !"Instant".equals(module.value("smooth-mode"));
      }

      private String init_Str_14() {
         boolean x = this.bool("x-axis");
         boolean y = this.bool("y-axis");
         if (x && y) {
            return "Both";
         } else if (x) {
            return "Horizontal";
         } else {
            return y ? "Vertical" : null;
         }
      }

      @Override
      public void onDisable() {
         this.init_V_46();
      }

      @Override
      public void tick() {
         if (MC != null
            && MC.player != null
            && MC.level != null
            && MC.gui.screen() == null
            && !ScaffoldModule.read_Z_12()
            && !ScaffoldModule.update_Z_5()
            && cn.init_ce_DataUtil() == null
            && !ScaffoldModule.flush_Z_5()
            && !AutoTotemModule.init_Z_6()
            && (AutismLiteVariant.enabled() || !j.init_Z_49())
            && !autismclient.util.DataPipe.init()
            && this.init_O.init_Z_ItemStack(MC.player.getMainHandItem())
            && (!this.bool("only-while-moving") || autismclient.util.aZ.init(4, ck.init_ck().state_I()))) {
            LivingEntity target = this.init_LivingEntity_4();
            if (target == null) {
               this.init_V_46();
            } else {
               Vec3 eyes = MC.player.getEyePosition();
               Vec3 point = this.init_Vec3(eyes, target);
               if (point == null) {
                  this.init_V_46();
               } else {
                  ce.Inner_DataUtil wanted = ce.init(point, eyes);
                  ce.Inner_DataUtil current = ce.init(MC.player);
                  if (this.close_I != target.getId()) {
                     this.close_I = target.getId();
                     this.open_I = -2147483648;
                     this.poll = 0;
                     autismclient.util.ba.init_V(autismclient.util.ba.Inner_StreamUtil.load);
                  }

                  String axis = this.init_Str_14();
                  if (axis == null) {
                     this.init_V_46();
                  } else {
                     boolean safe = this.init_Z_ce_DataUtil(current, wanted, eyes, point, target, axis);
                     if (init_Z_Module_2(this)) {
                        this.init_V_LivingEntity(target, eyes, point, current, axis, safe);
                     } else if (safe) {
                        autismclient.util.ba.init_V(autismclient.util.ba.Inner_StreamUtil.load);
                     } else {
                        ce.Inner_DataUtil applied = new ce.Inner_DataUtil(
                           "Vertical".equals(axis) ? current.init() : wanted.init(), "Horizontal".equals(axis) ? current.load() : wanted.load()
                        );
                        autismclient.util.ba.init_V_ba_StreamUtil_4(autismclient.util.ba.Inner_StreamUtil.load, current, applied);
                     }
                  }
               }
            }
         } else {
            this.init_V_46();
         }
      }

      private void init_V_LivingEntity(LivingEntity target, Vec3 eyes, Vec3 point, ce.Inner_DataUtil current, String axis, boolean inSafeZone) {
         autismclient.util.q hand = this.init_q_2();
         if (this.init_ap_TaskQueue == null) {
            this.init_ap_TaskQueue = new autismclient.util.ap.Inner_TaskQueue(new Random());
         }

         int ticker = ((AutismLivingEntityAccessor)MC.player).autism$getAttackStrengthTicker();
         if (this.sync_I >= 0 && ticker < this.sync_I) {
            this.update_I = 0;
         }

         this.sync_I = ticker;
         this.init_V_LivingEntity_4(target);
         double realized = this.load_Z ? Math.abs(ce.init(current.init(), this.init_F)) : 0.0;
         this.init_F = current.init();
         this.load_Z = true;
         double gcd = ce.init();
         boolean noCooldown = autismclient.util.aI.init(MC.player);
         this.init_q_DataUtil.init_q_DataUtil_Vec3_2(eyes, target.getBoundingBox(), autismclient.util.q.Inner_DataUtil.init(target.getBoundingBox(), point.y));
         this.init_q_DataUtil.write_Z = true;
         this.init_q_DataUtil.open_D = target instanceof Player && this.init_cj.init_Z_LivingEntity(target)
            ? this.init_cj.init(eyes, this.init_q_DataUtil.init_D, ck.init_ck().state_I())
            : 0.0;
         if (noCooldown) {
            this.init_q_DataUtil.sync_D = Math.max(2.0, target.hurtTime - 1);
         } else {
            float delay = (float)(1.0 / MC.player.getAttributeValue(Attributes.ATTACK_SPEED) * 20.0);
            this.init_q_DataUtil.sync_D = Math.max(0.0, delay - 0.5 - ticker);
            if (target.hurtTime > 1) {
               this.init_q_DataUtil.sync_D = Math.max(this.init_q_DataUtil.sync_D, target.hurtTime - 1);
            }
         }

         this.init_q_DataUtil.init_I = this.update_I;
         this.init_q_DataUtil.init_Z = this.update_I <= 1;
         this.init_q_DataUtil.poll_D = gcd;
         this.init_q_DataUtil.pop_Z = noCooldown;
         this.init_q_DataUtil.push_D = 18.0;
         this.init_q_DataUtil.push_Z = false;
         this.init_q_DataUtil.read_Z = false;
         int id = target.getId();
         this.init_q_DataUtil.load_Z = id != this.write_I;
         this.write_I = id;
         if (!autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue)) {
            autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, current);
         } else {
            autismclient.util.ap.init_V_ap_TaskQueue_3(this.init_ap_TaskQueue, current, this.sync_D, this.flush_D, gcd);
         }

         double[] want = hand.init(this.init_q_DataUtil, current.init(), current.load(), 40.0, 40.0);
         double horizontal = "Vertical".equals(axis) ? 0.0 : this.init("horizontal-speed", 0.8);
         double vertical = "Horizontal".equals(axis) ? 0.0 : this.init("vertical-speed", 0.2);
         double yaw = want[0] * horizontal;
         double pitch = init_D_D(want[1], vertical, yaw, realized, current.load(), this.init_q_DataUtil);
         double[] own = autismclient.util.aZ.init(gcd, MC.options.invertMouseX().get(), MC.options.invertMouseY().get());
         double[] pull = init_DArr_D(yaw, pitch, own[0], own[1], horizontal, vertical);
         yaw = pull[0];
         pitch = pull[1];
         this.update_I = Math.min(this.update_I + 1, 1000);
         if (!inSafeZone && (Math.abs(yaw) > 1.0E-6 || Math.abs(pitch) > 1.0E-6)) {
            ce.Inner_DataUtil out = autismclient.util.ap.init_ce_DataUtil_ap_TaskQueue(this.init_ap_TaskQueue, yaw, pitch, 40.0F, 40.0F, gcd, 18.0);
            double emitYaw = ce.init(out.init(), current.init());
            double emitPitch = out.load() - current.load();
            if (Math.abs(emitYaw) > 1.0E-6 || Math.abs(emitPitch) > 1.0E-6) {
               autismclient.util.ba.init_V_ba_StreamUtil_2(autismclient.util.ba.Inner_StreamUtil.load, (float)emitYaw, (float)emitPitch);
               this.sync_D = emitYaw;
               this.flush_D = emitPitch;
               this.init(hand, target, out);
               return;
            }
         }

         this.sync_D = 0.0;
         this.flush_D = 0.0;
         this.init(hand, target, current);
         autismclient.util.ba.init_V(autismclient.util.ba.Inner_StreamUtil.load);
      }

      static double[] init_DArr_D(double yaw, double pitch, double ownYaw, double ownPitch, double horizontal, double vertical) {
         double h = horizontal / 0.8;
         double v = vertical / 0.2;
         double outYaw = h > 0.0 ? load(yaw, pitch, ownYaw, ownPitch, 0.3 * h, 0.1 * h)[0] : 0.0;
         double outPitch = v > 0.0 ? load(yaw, pitch, ownYaw, ownPitch, 0.15 * v, 0.1 * v)[1] : 0.0;
         return new double[]{outYaw, outPitch};
      }

      static double[] load(double yaw, double pitch, double ownYaw, double ownPitch, double gain, double drag) {
         double own = Math.hypot(ownYaw, ownPitch);
         double want = Math.hypot(yaw, pitch);
         if (!(own < 1.0E-6) && !(want < 1.0E-9)) {
            double cos = (yaw * ownYaw + pitch * ownPitch) / (own * want);
            double add = cos >= 0.0 ? Math.min(gain * cos * own, want * cos) : drag * cos * own;
            return new double[]{ownYaw / own * add, ownPitch / own * add};
         } else {
            return new double[]{0.0, 0.0};
         }
      }

      static double init_D_D(double want, double share, double yaw, double realized, double atPitch, autismclient.util.q.Inner_DataUtil frame) {
         if (share <= 0.0) {
            return 0.0;
         }

         if (atPitch >= frame.load_D - frame.write_D && atPitch <= frame.load_D + frame.update_D) {
            return want;
         }

         double gcd = frame.poll_D;
         double pitch = want * share;
         if (!(gcd > 0.5) && !(Math.abs(pitch) > 0.5)) {
            double fastYaw = Math.min(2.6, 2.75 - 1.6 * gcd);
            if (Math.abs(pitch) < 0.13 && (Math.abs(yaw) > fastYaw || realized > 20.4)) {
               double toward = frame.load_D - atPitch;
               double sign = want != 0.0 ? Math.signum(want) : (toward != 0.0 ? Math.signum(toward) : 1.0);
               pitch = sign * Math.min(0.42, Math.max(0.13, gcd * 1.02));
            }

            return pitch;
         } else {
            return want;
         }
      }

      private autismclient.util.q init_q_2() {
         if (this.init_q == null) {
            this.init_q = autismclient.util.q.init_q();
         }

         return this.init_q;
      }

      private void init_V_LivingEntity_4(LivingEntity target) {
         this.init_cj.init(target, ck.init_ck().state_I());
         if (target.getId() != this.flush_I) {
            this.flush_I = target.getId();
            this.init_Z = false;
         } else {
            this.init_Z = Math.hypot(target.getX() - this.close_D, target.getZ() - this.open_D) >= 0.05;
         }

         this.close_D = target.getX();
         this.open_D = target.getZ();
      }

      private void init(autismclient.util.q hand, LivingEntity target, ce.Inner_DataUtil current) {
         float perfectYaw = (float)(Math.toDegrees(Math.atan2(target.getZ() - MC.player.getZ(), target.getX() - MC.player.getX())) - 90.0);
         hand.init_V_D(current.init(), current.load(), perfectYaw, this.update_I * 50L <= 1050L && this.init_Z);
      }

      private void init_V_46() {
         autismclient.util.aZ.init(0.0, false, false);
         if (this.init_q != null) {
            this.init_q.init_V();
         }

         if (this.init_ap_TaskQueue != null) {
            autismclient.util.ap.init_V_ap_TaskQueue(this.init_ap_TaskQueue);
         }

         this.sync_D = 0.0;
         this.flush_D = 0.0;
         this.write_I = -1;
         this.update_I = 1000;
         this.sync_I = -1;
         this.flush_I = -1;
         this.init_Z = false;
         this.init_cj.init_V();
         this.load_Z = false;
         this.close_I = -2147483648;
         this.open_I = -2147483648;
         this.poll = 0;
         autismclient.util.ba.init_V(autismclient.util.ba.Inner_StreamUtil.load);
      }

      private LivingEntity init_LivingEntity_4() {
         double rangeSq = this.decimal("range") * this.decimal("range");
         ce.Inner_DataUtil current = ce.init(MC.player);
         Vec3 eyes = MC.player.getEyePosition();
         String priority = this.choice("priority");
         LivingEntity best = null;
         LivingEntity tracked = null;

         for (Entity entity : MC.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && this.init(living, rangeSq, current)) {
               if (living.getId() == this.close_I) {
                  tracked = living;
               }

               if (best == null || this.init(living, best, priority, current, eyes) < 0) {
                  best = living;
               }
            }
         }

         if (tracked == null || best == null || best == tracked) {
            this.open_I = -2147483648;
            this.poll = 0;
            return tracked == null ? best : tracked;
         }

         if (this.init(best, tracked, priority, current, eyes) >= 0) {
            this.open_I = -2147483648;
            this.poll = 0;
            return tracked;
         }

         if (this.open_I == best.getId()) {
            this.poll++;
         } else {
            this.open_I = best.getId();
            this.poll = 1;
         }

         if (this.poll < 4) {
            return tracked;
         }

         this.open_I = -2147483648;
         this.poll = 0;
         return best;
      }

      private boolean init(LivingEntity entity, double rangeSq, ce.Inner_DataUtil current) {
         if (entity == MC.player || entity.isRemoved() || !entity.isAlive()) {
            return false;
         }

         if (ByteReader.load_Z(entity)) {
            return false;
         }

         if (aL.init(entity, "aimassist")) {
            return false;
         }

         if (entity.hurtTime > this.integer("hurt-time")) {
            return false;
         }

         if (!this.init_Z_Entity_7(entity)) {
            return false;
         }

         if (entity.distanceToSqr(MC.player) > rangeSq) {
            return false;
         }

         ce.Inner_DataUtil toEntity = ce.init(entity.getBoundingBox().getCenter(), MC.player.getEyePosition());
         return ce.init_F(current, toEntity) <= this.integer("fov");
      }

      private int init(LivingEntity a, LivingEntity b, String priority, ce.Inner_DataUtil current, Vec3 eyes) {
         int var10000;
         switch (priority) {
            case "Smart":
               var10000 = Double.compare(autismclient.util.x.init(a, MC.player, current), autismclient.util.x.init(b, MC.player, current));
               break;
            case "Type":
               var10000 = Integer.compare(this.init_I_LivingEntity_3(a), this.init_I_LivingEntity_3(b));
               break;
            case "Health":
               var10000 = Float.compare(a.getHealth() + a.getAbsorptionAmount(), b.getHealth() + b.getAbsorptionAmount());
               break;
            case "Distance":
               var10000 = Double.compare(a.distanceToSqr(MC.player), b.distanceToSqr(MC.player));
               break;
            case "HurtTime":
               var10000 = Integer.compare(a.hurtTime, b.hurtTime);
               break;
            case "Age":
               var10000 = Integer.compare(b.tickCount, a.tickCount);
               break;
            default:
               var10000 = Float.compare(this.init(a, current, eyes), this.init(b, current, eyes));
         }

         return var10000;
      }

      private int init_I_LivingEntity_3(LivingEntity entity) {
         if (entity instanceof Player) {
            return 0;
         } else {
            return entity instanceof Enemy ? 1 : 100;
         }
      }

      private float init(LivingEntity entity, ce.Inner_DataUtil current, Vec3 eyes) {
         ce.Inner_DataUtil toEntity = ce.init(entity.getBoundingBox().getCenter(), eyes);
         return ce.init_F(current, toEntity);
      }

      private boolean init_Z_Entity_7(Entity entity) {
         String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
         Set<String> ids = this.init_Set_12();
         return ids.contains(id) || ids.contains(id.substring(id.indexOf(58) + 1));
      }

      private Set<String> init_Set_12() {
         List<String> entries = this.list("entities");
         String source = String.join("|", entries);
         if (source.equals(this.init_Str)) {
            return this.init_Set;
         }

         Set<String> normalized = new LinkedHashSet<>();

         for (String entry : entries) {
            if (entry != null) {
               String value = entry.trim().toLowerCase(Locale.ROOT);
               if (!value.isEmpty()) {
                  normalized.add(value);
                  int split = value.indexOf(58);
                  if (split >= 0 && split + 1 < value.length()) {
                     normalized.add(value.substring(split + 1));
                  }
               }
            }
         }

         this.init_Str = source;
         this.init_Set = Set.copyOf(normalized);
         return this.init_Set;
      }

      private Vec3 init_Vec3(Vec3 eyes, LivingEntity entity) {
         if (entity instanceof Player) {
            return this.load(eyes, entity);
         }

         AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
         double cx = (box.minX + box.maxX) * 0.5;
         double cz = (box.minZ + box.maxZ) * 0.5;
         Vec3 var10000;
         switch (this.choice("target-point")) {
            case "Center":
            case "Body":
               var10000 = box.getCenter();
               break;
            case "Head":
               var10000 = entity.getEyePosition();
               break;
            case "Feet":
               var10000 = new Vec3(cx, box.minY + 0.1, cz);
               break;
            default:
               var10000 = this.init(eyes, box);
         }

         Vec3 preferred = var10000;
         return this.init(eyes, preferred) ? preferred : this.load(eyes, box);
      }

      private Vec3 load(Vec3 eyes, LivingEntity player) {
         AABB modelBox = player.getBoundingBox();
         Vec3 preferred = init_Vec3_Vec3_4(eyes, modelBox, player.getX(), player.getEyeY(), player.getZ(), this.choice("target-point"));
         return this.init(eyes, preferred) ? preferred : this.init(eyes, modelBox, player.getX(), player.getZ(), preferred.y);
      }

      static Vec3 init_Vec3_Vec3_4(Vec3 eyes, AABB modelBox, double modelX, double eyeY, double modelZ, String targetPoint) {
         double height = Math.max(1.0E-9, modelBox.maxY - modelBox.minY);
         double low = modelBox.minY + Math.min(0.1, height * 0.1);
         double high = modelBox.maxY - Math.min(0.1, height * 0.1);
         String point = targetPoint == null ? "nearest" : targetPoint.trim().toLowerCase(Locale.ROOT);
         double var10000;
         switch (point) {
            case "center":
            case "body":
               var10000 = (modelBox.minY + modelBox.maxY) * 0.5;
               break;
            case "head":
               var10000 = Mth.clamp(Double.isFinite(eyeY) ? eyeY : modelBox.maxY, low, high);
               break;
            case "feet":
               var10000 = low;
               break;
            default:
               var10000 = Mth.clamp(eyes == null ? (modelBox.minY + modelBox.maxY) * 0.5 : eyes.y, low, high);
         }

         double y = var10000;
         return new Vec3(modelX, y, modelZ);
      }

      private Vec3 init(Vec3 eyes, AABB modelBox, double modelX, double modelZ, double preferredY) {
         double height = Math.max(0.001, modelBox.maxY - modelBox.minY);
         Vec3 best = null;
         double bestVerticalDistance = 1.7976931348623157E308;

         for (int iy = 0; iy < 9; iy++) {
            double y = modelBox.minY + height * ((iy + 0.5) / 9.0);
            Vec3 point = new Vec3(modelX, y, modelZ);
            if (this.init(eyes, point)) {
               double verticalDistance = Math.abs(y - preferredY);
               if (verticalDistance < bestVerticalDistance) {
                  bestVerticalDistance = verticalDistance;
                  best = point;
               }
            }
         }

         return best;
      }

      private Vec3 init(Vec3 eyes, AABB box) {
         Vec3 closest = new Vec3(Mth.clamp(eyes.x, box.minX, box.maxX), Mth.clamp(eyes.y, box.minY, box.maxY), Mth.clamp(eyes.z, box.minZ, box.maxZ));
         return closest.distanceToSqr(eyes) < 1.0E-6 ? box.getCenter() : closest;
      }

      private Vec3 load(Vec3 eyes, AABB box) {
         ce.Inner_DataUtil current = ce.init(MC.player);
         Vec3 best = null;
         float bestAngle = 3.4028235E38F;

         for (int ix = 0; ix <= 2; ix++) {
            double x = Mth.lerp(ix / 2.0, box.minX, box.maxX);

            for (int iy = 0; iy <= 2; iy++) {
               double y = Mth.lerp(iy / 2.0, box.minY, box.maxY);

               for (int iz = 0; iz <= 2; iz++) {
                  double z = Mth.lerp(iz / 2.0, box.minZ, box.maxZ);
                  Vec3 point = new Vec3(x, y, z);
                  if (this.init(eyes, point)) {
                     float angle = ce.init_F(current, ce.init(point, eyes));
                     if (angle < bestAngle) {
                        bestAngle = angle;
                        best = point;
                     }
                  }
               }
            }
         }

         return best;
      }

      private boolean init(Vec3 eyes, Vec3 point) {
         if (MC.level != null && MC.player != null && point != null) {
            HitResult hit = MC.level.clip(new ClipContext(eyes, point, Block.COLLIDER, Fluid.NONE, MC.player));
            return hit == null || hit.getType() == Type.MISS || hit.getLocation().distanceToSqr(point) < 0.05;
         } else {
            return false;
         }
      }

      private boolean init_Z_ce_DataUtil(
         ce.Inner_DataUtil current, ce.Inner_DataUtil targetRotation, Vec3 eyes, Vec3 targetPoint, LivingEntity target, String axis
      ) {
         int configured = this.integer("safe-zone");
         if (configured <= 0 || current == null || targetRotation == null || eyes == null || targetPoint == null || target == null) {
            return false;
         }

         if (!this.init_Z(eyes, target)) {
            return false;
         }

         AABB box = target.getBoundingBox().inflate(target.getPickRadius());
         double targetWidth = Math.max(box.maxX - box.minX, box.maxZ - box.minZ);
         double radius = targetWidth * (configured / 100.0);
         double distance = Math.max(1.0E-4, eyes.distanceTo(targetPoint));
         double angularRadius = Math.toDegrees(Math.atan2(radius, distance));
         double hardwareStep = ce.load();
         if (hardwareStep > 0.0 && Double.isFinite(hardwareStep)) {
            angularRadius = Math.max(angularRadius, hardwareStep);
         }

         boolean yawInside = "Vertical".equals(axis) || Math.abs(ce.init(targetRotation.init(), current.init())) <= angularRadius;
         boolean pitchInside = "Horizontal".equals(axis) || Math.abs(ce.init(targetRotation.load(), current.load())) <= angularRadius;
         return yawInside && pitchInside;
      }

      private boolean init_Z(Vec3 eyes, LivingEntity target) {
         if (MC.player != null && eyes != null && target != null) {
            AABB hitbox = target.getBoundingBox().inflate(target.getPickRadius());
            double rayLength = Math.max(this.decimal("range"), eyes.distanceTo(hitbox.getCenter()) + hitbox.getSize());
            Vec3 rayEnd = eyes.add(MC.player.getViewVector(1.0F).scale(rayLength));
            Optional<Vec3> intersection = hitbox.clip(eyes, rayEnd);
            return intersection.isPresent() && this.init(eyes, intersection.get());
         } else {
            return false;
         }
      }

      private double init(String option, double maxShare) {
         return Mth.clamp(this.integer(option) / 100.0, 0.01, 1.0) * maxShare;
      }
   }

   static final class Inner_SyncManager extends Module {
      private static final java.time.format.DateTimeFormatter AUDIT_ESP_TIME = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
         .withZone(java.time.ZoneId.systemDefault());
      private final Map<java.util.UUID, AuditEspSighting> auditTrackedPlayers = new LinkedHashMap<>();
      private Object auditLastLevel;
      private static final int AUDIT_LOST_TICKS = 20;

      Inner_SyncManager() {
         super("esp", "ESP", ae.update, "Highlights entities.");
         this.add(new ChoiceSetting("mode", "Mode", "2D", "Shader", "2D").description("Entity highlight style.").build());
         this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").group("General").build());
         this.add(new BoolSetting("fill", "Fill", false).group("General").visibleWhen(() -> "2D".equals(this.choice("mode"))).build());
         this.add(new DoubleSetting("fade-distance", "Fade Distance", 3.0, 0.0, 12.0, 0.25).group("General"));
         this.add(new ColorSetting("players-color", "Players", -855638017).group("Colors"));
         this.add(new ColorSetting("animals-color", "Animals", -864747633).group("Colors"));
         this.add(new ColorSetting("water-animals-color", "Water Animals", -865674753).group("Colors"));
         this.add(new ColorSetting("monsters-color", "Monsters", -855684534).group("Colors"));
         this.add(new ColorSetting("ambient-color", "Ambient", -860386049).group("Colors"));
         this.add(new ColorSetting("misc-color", "Misc", -858993460).group("Colors"));
         this.add(new BoolSetting("skeleton", "Skeleton", false).group("Skeleton").description("Draw animated skeletons.").build());
         this.add(
            new ChoiceSetting("skeleton-color-mode", "Color Mode", "Static", "Static", "Distance")
               .group("Skeleton")
               .visibleWhen(() -> this.bool("skeleton"))
               .description("Skeleton color source.")
               .build()
         );
         this.add(
            new ColorSetting("skeleton-color", "Color", -1)
               .group("Skeleton")
               .visibleWhen(() -> this.bool("skeleton") && "Static".equals(this.value("skeleton-color-mode")))
         );
         this.add(
            new DoubleSetting("skeleton-width", "Line Width", 2.0, 1.0, 6.0, 0.25)
               .group("Skeleton")
               .visibleWhen(() -> this.bool("skeleton"))
               .description("Skeleton line thickness.")
               .build()
         );
      }

      @Override
      public void onDisable() {
         this.auditFlushSightings("esp-disabled", System.currentTimeMillis());
         this.auditLastLevel = null;
      }

      @Override
      public void onGameJoin() {
         this.auditFlushSightings("world-change", System.currentTimeMillis());
         this.auditLastLevel = MC == null ? null : MC.level;
      }

      @Override
      public void onGameLeft() {
         this.auditFlushSightings("disconnect", System.currentTimeMillis());
         this.auditLastLevel = null;
      }

      @Override
      public void tick() {
         if (aD.read_Z()) {
            this.auditTrackedPlayers.clear();
            this.auditLastLevel = MC == null ? null : MC.level;
            return;
         }

         if (MC == null || MC.level == null || MC.player == null) {
            this.auditFlushSightings("disconnect", System.currentTimeMillis());
            this.auditLastLevel = null;
            return;
         }

         long now = System.currentTimeMillis();
         if (this.auditLastLevel != MC.level) {
            this.auditFlushSightings("world-change", now);
            this.auditLastLevel = MC.level;
         }

         String dim = MC.level.dimension().identifier().toString();
         Set<java.util.UUID> seenNow = new java.util.HashSet<>();

         for (net.minecraft.world.entity.player.Player player : MC.level.players()) {
            if (player != null && player != MC.player && am.auditMatchesEsp(player)) {
               java.util.UUID uuid = player.getUUID();
               seenNow.add(uuid);
               double dist = player.distanceTo(MC.player);
               int px = player.getBlockX();
               int py = player.getBlockY();
               int pz = player.getBlockZ();
               String name = auditPlayerName(player);
               AuditEspSighting existing = this.auditTrackedPlayers.get(uuid);
               if (existing == null) {
                  AuditEspSighting created = new AuditEspSighting(name, uuid, dim, now, dist, px, py, pz);
                  this.auditTrackedPlayers.put(uuid, created);
                  autismclient.AutismClientAddon.LOG.info(
                     "[ESP] Player detected: {} ({}) | at {} | dist={}m | pos={},{},{} | dim={}",
                     name,
                     uuid,
                     auditFormatTime(now),
                     auditFormatDist(dist),
                     px,
                     py,
                     pz,
                     dim
                  );
               } else {
                  existing.name = name;
                  existing.lastSeenMs = now;
                  existing.lastDistance = dist;
                  if (dist < existing.minDistance) {
                     existing.minDistance = dist;
                  }
                  if (dist > existing.maxDistance) {
                     existing.maxDistance = dist;
                  }
                  existing.lastX = px;
                  existing.lastY = py;
                  existing.lastZ = pz;
               }
            }
         }

         if (!this.auditTrackedPlayers.isEmpty()) {
            var it = this.auditTrackedPlayers.values().iterator();
            while (it.hasNext()) {
               AuditEspSighting sighting = it.next();
               if (seenNow.contains(sighting.uuid)) {
                  sighting.missedTicks = 0;
               } else if (++sighting.missedTicks >= AUDIT_LOST_TICKS) {
                  it.remove();
                  auditLogLost(sighting, sighting.lastSeenMs, "out-of-range");
               }
            }
         }
      }

      private void auditFlushSightings(String reason, long now) {
         if (!this.auditTrackedPlayers.isEmpty()) {
            if (!aD.read_Z()) {
               for (AuditEspSighting sighting : this.auditTrackedPlayers.values()) {
                  auditLogLost(sighting, now, reason);
               }
            }
            this.auditTrackedPlayers.clear();
         }
      }

      private static void auditLogLost(AuditEspSighting s, long endMs, String reason) {
         long toMs = Math.max(s.lastSeenMs, endMs);
         autismclient.AutismClientAddon.LOG.info(
            "[ESP] Player lost: {} ({}) | from={} to={} ({}) | dist first={}m min={}m max={}m last={}m | lastPos={},{},{} | dim={} | reason={}",
            s.name,
            s.uuid,
            auditFormatTime(s.firstSeenMs),
            auditFormatTime(toMs),
            auditFormatDuration(Math.max(0L, toMs - s.firstSeenMs)),
            auditFormatDist(s.firstDistance),
            auditFormatDist(s.minDistance),
            auditFormatDist(s.maxDistance),
            auditFormatDist(s.lastDistance),
            s.lastX,
            s.lastY,
            s.lastZ,
            s.dimension,
            reason
         );
      }

      private static String auditPlayerName(net.minecraft.world.entity.player.Player player) {
         String profileName = player.getGameProfile() == null ? null : player.getGameProfile().name();
         if (profileName != null && !profileName.isBlank()) {
            return profileName;
         }
         String sb = player.getScoreboardName();
         return sb != null && !sb.isBlank() ? sb : player.getUUID().toString();
      }

      private static String auditFormatTime(long epochMs) {
         return AUDIT_ESP_TIME.format(java.time.Instant.ofEpochMilli(epochMs));
      }

      private static String auditFormatDist(double meters) {
         return String.format(Locale.ROOT, "%.1f", meters);
      }

      private static String auditFormatDuration(long ms) {
         if (ms < 1000L) {
            return ms + "ms";
         }
         long totalSec = ms / 1000L;
         long remTenth = (ms % 1000L) / 100L;
         if (totalSec < 60L) {
            return totalSec + "." + remTenth + "s";
         }
         long min = totalSec / 60L;
         long sec = totalSec % 60L;
         if (min < 60L) {
            return min + "m " + sec + "." + remTenth + "s";
         }
         long hrs = min / 60L;
         long remMin = min % 60L;
         return String.format(Locale.ROOT, "%dh %02dm %02ds", hrs, remMin, sec);
      }
   }

   private static final class AuditEspSighting {
      String name;
      final java.util.UUID uuid;
      final String dimension;
      final long firstSeenMs;
      long lastSeenMs;
      final double firstDistance;
      double minDistance;
      double maxDistance;
      double lastDistance;
      int lastX;
      int lastY;
      int lastZ;
      int missedTicks;

      AuditEspSighting(String name, java.util.UUID uuid, String dimension, long now, double dist, int x, int y, int z) {
         this.name = name;
         this.uuid = uuid;
         this.dimension = dimension;
         this.firstSeenMs = now;
         this.lastSeenMs = now;
         this.firstDistance = dist;
         this.minDistance = dist;
         this.maxDistance = dist;
         this.lastDistance = dist;
         this.lastX = x;
         this.lastY = y;
         this.lastZ = z;
      }
   }

   static final class Inner_TableIndex extends Module {
      Inner_TableIndex() {
         super("inv-move", "InvMove", ae.load_ae, "Allows movement keys in normal container screens.");
      }

      @Override
      public void onEnable() {
         AutismConfig.getGlobal().inventoryMove = true;
      }

      @Override
      public void onDisable() {
         AutismConfig.getGlobal().inventoryMove = false;
      }

      @Override
      public void tick() {
         autismclient.util.ax.init_V();
      }
   }

   static final class Inner_TaskQueue extends Module {
      static final int init_I = 12;
      private final Random init_Random = new Random();
      private BlockPos init_BlockPos;
      private boolean init_Z;
      private long init_J;
      private long load_J;
      private int load_I = -1;
      private int read_I = -1;
      private long read_J;
      private long write = -1L;

      Inner_TaskQueue() {
         super("auto-tool", "AutoTool", ae.read, "Switches to the best tool to mine a block.");
         this.add(new IntSetting("switch-delay-ms", "Switch Delay", 12, 0, 1000, 1).description("Delay before switching.").build());
         this.add(new BoolSetting("consider-inventory", "Consider Inventory", false));
         this.add(new BoolSetting("ignore-durability", "Ignore Durability", true));
         this.add(new BoolSetting("prefer-silk-touch", "Prefer Silk Touch", false));
         this.add(new BoolSetting("require-sneaking", "Require Sneaking", false));
         this.add(new BoolSetting("switch-back", "Switch Back", false));
         this.add(new IntSetting("switch-back-delay-ms", "Restore Delay", 300, 0, 1000, 25).description("Delay before restoring.").build());
      }

      @Override
      public void onDisable() {
         this.init_V_47();
      }

      @Override
      public void onGameJoin() {
         this.init_V_47();
      }

      @Override
      public void onGameLeft() {
         this.init_V_47();
      }

      private void init_V_47() {
         this.load_I = -1;
         this.read_I = -1;
         this.init_BlockPos = null;
         this.init_Z = false;
         this.load_J = 0L;
         this.write = -1L;
      }

      @Override
      public void tick() {
         if (!AutismLiteVariant.enabled() && UtilCore.init_Z_23()) {
            this.init_V_47();
         } else if ((AutismLiteVariant.enabled() || !autismclient.util.g.PoolEntry.init_Z())
            && MC.player != null
            && MC.level != null
            && MC.gameMode != null
            && !AutoTotemModule.init_Z_6()
            && MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) {
            boolean destroying = accessor.autism$isDestroying();
            if (destroying) {
               this.write = -1L;
               if (this.bool("require-sneaking") && !MC.player.isShiftKeyDown()) {
                  return;
               }

               BlockPos pos = accessor.autism$getDestroyBlockPos();
               if (pos == null) {
                  return;
               }

               BlockState state = MC.level.getBlockState(pos);
               if (state.isAir()) {
                  return;
               }

               this.read_J = init_J_9();
               if (!pos.equals(this.init_BlockPos)) {
                  this.init_BlockPos = pos;
                  this.init_Z = false;
                  this.init_J = init_J_9();
                  this.load_J = this.init_J + this.init(this.integer("switch-delay-ms"));
                  if (this.load_I < 0) {
                     this.load_I = MC.player.getInventory().getSelectedSlot();
                  }
               }

               if (!this.init_Z && init_J_9() >= this.load_J) {
                  int slot = this.init(state);
                  if (slot >= 0) {
                     this.init(slot, true);
                  }
               }
            } else if (this.bool("switch-back")) {
               if (this.read_I >= 0 && MC.player.getInventory().getSelectedSlot() != this.read_I) {
                  this.load_I = -1;
                  this.read_I = -1;
                  this.write = -1L;
               }

               if (this.load_I >= 0 && this.write < 0L) {
                  this.write = this.read_J + this.init(this.integer("switch-back-delay-ms"));
               }

               if (this.load_I >= 0 && init_J_9() >= this.write) {
                  if (!this.init(this.load_I, false)) {
                     return;
                  }

                  this.init_BlockPos = null;
                  this.init_Z = false;
                  this.load_J = 0L;
                  this.write = -1L;
                  this.load_I = -1;
                  this.read_I = -1;
               }
            } else {
               this.load_I = -1;
               this.read_I = -1;
               this.init_BlockPos = null;
               this.init_Z = false;
               this.load_J = 0L;
               this.write = -1L;
            }
         }
      }

      private boolean init(int slot, boolean forward) {
         if (MC.player == null) {
            return false;
         }

         int clamped = Math.max(0, Math.min(8, slot));
         if (MC.player.getInventory().getSelectedSlot() == clamped) {
            this.init_Z = true;
            if (forward) {
               this.read_I = clamped;
            }

            return true;
         } else {
            if (!autismclient.util.ah.write_Z(this.id())) {
               return false;
            }

            try {
               if (forward) {
                  autismclient.util.as.init_V_I(clamped);
               } else {
                  autismclient.util.as.init(clamped, this.read_I);
               }

               if (forward) {
                  this.read_I = clamped;
               }

               return true;
            } finally {
               autismclient.util.ah.read_V(this.id());
            }
         }
      }

      private int init(BlockState state) {
         if (MC.player != null && state != null) {
            int limit = this.bool("consider-inventory") ? 36 : 9;
            int best = -1;
            if (this.bool("prefer-silk-touch")) {
               best = autismclient.util.ArrayUtil.init(MC, state, limit, this.bool("ignore-durability"), true);
            }

            if (best < 0) {
               best = autismclient.util.ArrayUtil.init(MC, state, limit, this.bool("ignore-durability"), false);
            }

            if (best < 0) {
               return -1;
            }

            if (autismclient.util.ah.init(best, this.id())) {
               return -1;
            }

            int selectedSlot = MC.player.getInventory().getSelectedSlot();
            if (best < 9) {
               return !this.init(state, best, selectedSlot) ? selectedSlot : best;
            }

            int selected = MC.player.getInventory().getSelectedSlot();
            if (!this.bool("consider-inventory")) {
               return -1;
            }

            if (!autismclient.util.ah.write_Z(this.id())) {
               return -1;
            }

            try {
               if (!autismclient.util.aw.read(MC, best, selected)) {
                  return -1;
               }
            } finally {
               autismclient.util.ah.read_V(this.id());
            }

            return selected;
         } else {
            return -1;
         }
      }

      private boolean init(BlockState state, int candidateSlot, int selectedSlot) {
         if (MC.player == null) {
            return false;
         }

         if (candidateSlot == selectedSlot) {
            return true;
         }

         ItemStack candidate = MC.player.getInventory().getItem(candidateSlot);
         ItemStack current = MC.player.getInventory().getItem(Math.max(0, Math.min(8, selectedSlot)));
         float candidateSpeed = autismclient.util.ArrayUtil.init(MC, candidate, state);
         float currentSpeed = autismclient.util.ArrayUtil.init(MC, current, state);
         return this.bool("prefer-silk-touch") && candidateSpeed > 1.0F && autismclient.util.ArrayUtil.init_Z(MC, candidate)
            ? !(currentSpeed > 1.0F) || !autismclient.util.ArrayUtil.init_Z(MC, current)
            : candidateSpeed > currentSpeed;
      }

      private int init(int baseMs) {
         if (baseMs <= 0) {
            return 0;
         }

         int spread = Math.max(15, Math.min(180, Math.round(baseMs * 0.55F)));
         int extra = this.init_Random.nextInt(spread + 1);
         if (this.init_Random.nextInt(12) == 0) {
            extra += 15 + this.init_Random.nextInt(Math.max(16, spread / 2 + 1));
         }

         return Math.min(1000, baseMs + extra);
      }

      private static long init_J_9() {
         return System.currentTimeMillis();
      }
   }

   static final class Inner_TaskTimer extends Module {
      private int init;
      private int load;
      private String auditSplitRest = "";

      Inner_TaskTimer() {
         super("spam", "Spam", ae.write, "Sends timed chat.");
         this.add(new StringListSetting("messages", "Messages", "你好，世界！").description("Messages separated by |."));
         this.add(new IntSetting("delay", "Delay", 20, 0, 200, 1));
         this.add(new BoolSetting("disable-on-leave", "Disable On Leave", true).group("Safety"));
         this.add(new BoolSetting("disable-on-disconnect", "Disable On Disconnect", true).group("Safety"));
         this.add(new BoolSetting("randomise", "Randomise", false));
         this.add(new BoolSetting("auto-split-messages", "Auto Split", false).group("Splitting"));
         this.add(
            new IntSetting("split-length", "Split Length", 256, 8, 256, 8)
               .group("Splitting")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("auto-split-messages")))
               .build()
         );
         this.add(
            new IntSetting("split-delay", "Split Delay", 20, 0, 200, 1)
               .group("Splitting")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("auto-split-messages")))
               .build()
         );
         this.add(new BoolSetting("bypass", "Bypass", false).group("Bypass"));
         this.add(
            new BoolSetting("include-uppercase-characters", "Uppercase", false)
               .group("Bypass")
               .visibleWhen(() -> Boolean.parseBoolean(this.value("bypass")))
               .build()
         );
         this.add(new IntSetting("length", "Bypass Length", 4, 1, 16, 1).group("Bypass").visibleWhen(() -> Boolean.parseBoolean(this.value("bypass"))).build());
         this.add(new BoolSetting("uppercase", "Uppercase Legacy", false).group("Bypass").visibleWhen(() -> false).build());
         this.add(new IntSetting("bypass-length", "Bypass Length Legacy", 4, 1, 16, 1).group("Bypass").visibleWhen(() -> false).build());
      }

      @Override
      public void onEnable() {
         this.init = this.integer("delay");
         this.load = 0;
         this.auditSplitRest = "";
      }

      @Override
      public void tick() {
         if (MC.getConnection() != null && this.init-- <= 0) {
            this.init = this.integer("delay");
            List<String> messages = this.init_List_17();
            if (!this.auditSplitRest.isEmpty() || !messages.isEmpty()) {
               String msg;
               if (!this.auditSplitRest.isEmpty()) {
                  msg = this.auditSplitRest;
                  this.auditSplitRest = "";
               } else {
                  int selected = this.bool("randomise") ? new Random().nextInt(messages.size()) : this.load++ % messages.size();
                  msg = messages.get(selected);
                  if (this.bool("bypass")) {
                     msg = msg + " " + this.init_Str_15();
                  }
               }

               if (this.bool("auto-split-messages") && msg.length() > this.integer("split-length")) {
                  int split = Math.max(1, this.integer("split-length"));
                  // The rest goes out next, after the split delay; a command is cut, never continued as chat.
                  // A rest that looks like a command is dropped: some chat paths run "/" text as a command.
                  if (!msg.startsWith("/") && !msg.substring(split).trim().startsWith("/")) {
                     this.auditSplitRest = msg.substring(split);
                  }

                  msg = msg.substring(0, split);
                  this.init = this.integer("split-delay");
               } else if (msg.length() > 256) {
                  msg = msg.substring(0, 256);
               }

               if (msg.startsWith("/") && msg.length() > 1) {
                  MC.getConnection().sendCommand(msg.substring(1));
               } else {
                  autismclient.commands.DataUtil.init(MC.getConnection(), msg);
               }
            }
         }
      }

      @Override
      public void onDisable() {
         this.auditSplitRest = "";
      }

      @Override
      public void onGameLeft() {
         this.auditSplitRest = "";
         if (this.bool("disable-on-leave") || this.bool("disable-on-disconnect")) {
            this.setEnabled(false);
         }
      }

      private List<String> init_List_17() {
         List<String> messages = new ArrayList<>();

         for (String raw : this.text("messages").split("\\|")) {
            String msg = raw.trim();
            if (!msg.isEmpty()) {
               messages.add(msg);
            }
         }

         return messages;
      }

      private String init_Str_15() {
         String chars = !this.bool("include-uppercase-characters") && !this.bool("uppercase")
            ? "abcdefghijklmnopqrstuvwxyz0123456789"
            : "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
         Random rng = new Random();
         StringBuilder out = new StringBuilder();
         int length = this.integer("length") == 4 && this.integer("bypass-length") != 4 ? this.integer("bypass-length") : this.integer("length");

         for (int i = 0; i < length; i++) {
            out.append(chars.charAt(rng.nextInt(chars.length())));
         }

         return out.toString();
      }
   }

   static final class Inner_TextFormat extends Module {
      Inner_TextFormat() {
         super("frame-esp", "FrameESP", ae.update, "Highlights item frames by the item in them.");
         this.add(
            new ChoiceSetting("mode", "Mode", "Shader", "Shader", "Box")
               .group("General")
               .description("Shader: a glowing outline around the frame and its item, through walls. Box: boxes like SpawnerESP.")
               .build()
         );
         this.add(new BoolSetting("fill", "Fill", false).group("General").description("Draw a translucent plate over each frame."));
         this.add(new BoolSetting("tracers", "Tracers", false).group("Tracers").description("Line to each frame"));
         this.add(RegistryListSetting.items("item-list", "Items", ah.load_Str).group("Items").description("Items to highlight; each gets its own color."));
         this.add(
            new BoolSetting("other-frames", "Other Frames", false).group("Items").description("Also highlight frames holding an item that is not on the list.")
         );
         this.add(new BoolSetting("empty-frames", "Empty Frames", false).group("Items").description("Also highlight empty frames."));

         for (ah.Inner_StreamUtil target : ah.init_List) {
            String id = target.init_Str();
            this.add(new ColorSetting("color-" + id, target.load(), target.init_I()).group("Colors").visibleWhen(() -> ah.init_Z(this, id)));
         }

         this.add(
            new ColorSetting("other-color", "Other Frames", -859848769)
               .group("Colors")
               .visibleWhen(() -> this.bool("other-frames") || this.bool("empty-frames"))
         );
         this.init();
      }

      @Override
      public void onOptionValueChanged(String settingId) {
         if ("item-list".equals(settingId)) {
            this.init();
         }
      }

      private void init() {
         for (String id : ah.init(this)) {
            if (!ah.init_Z(id)) {
               String key = "color-" + id;
               if (this.setting(key) == null) {
                  String label = cc.init(id);
                  this.add(new ColorSetting(key, label.isBlank() ? id : label, ah.init_I(id)).group("Colors").visibleWhen(() -> ah.init_Z(this, id)));
               }
            }
         }
      }
   }

   static final class Inner_TokenStore extends Module {
      private boolean init;

      Inner_TokenStore() {
         super("offhand-interact-placement", "OffhandInteract", ae.read, "Uses the offhand too.");
         this.add(new BoolSetting("cancelMainhand", "Cancel Mainhand", false));
      }

      @Override
      public boolean onPacketSend(Packet<?> packet) {
         if (!this.init && MC.gameMode != null && MC.player != null) {
            if (!(packet instanceof ServerboundUseItemOnPacket use && use.hand() == InteractionHand.MAIN_HAND)) {
               return false;
            } else {
               if (HashUtil.load_Z()) {
                  return false;
               }

               if (autismclient.util.ah.load_Z(this.id())) {
                  return false;
               }

               if (AutoTotemModule.init_Z_6()) {
                  return false;
               }

               if (al.init_Z_HitResult(use.hitResult(), InteractionHand.OFF_HAND, this.id())) {
                  return false;
               }

               String placementOwner = bM.init();
               if (placementOwner != null && !placementOwner.equals(this.id())) {
                  return false;
               }

               if (!bM.init(this.id())) {
                  return false;
               }

               this.init = true;

               try {
                  MC.gameMode.useItemOn(MC.player, InteractionHand.OFF_HAND, use.hitResult());
               } finally {
                  this.init = false;
               }

               return this.bool("cancelMainhand");
            }
         } else {
            return false;
         }
      }
   }

   static final class Inner_UiHelper extends Module {
      Inner_UiHelper() {
         super("fake-coords", "FakeCoords", ae.update, "Spoofs displayed coordinates only.");
         this.add(
            new ChoiceSetting("mode", "Mode", "Offset", "Offset", "Scaled", "Rotated", "Scrambled", "Frozen", "Custom")
               .description("Choose spoofing mode.")
               .build()
         );
         this.add(new BoolSetting("fake-y", "Fake Y", false).description("Spoof vertical coordinate.").build());
         this.add(
            new DoubleSetting("custom-x", "Custom X", 0.0, -3.0E7, 3.0E7, 1.0)
               .description("Set displayed X.")
               .numericTextField()
               .visibleWhen(() -> "Custom".equals(this.choice("mode")))
               .build()
         );
         this.add(
            new DoubleSetting("custom-y", "Custom Y", 64.0, -3.0E7, 3.0E7, 1.0)
               .description("Set displayed Y.")
               .numericTextField()
               .visibleWhen(() -> "Custom".equals(this.choice("mode")))
               .build()
         );
         this.add(
            new DoubleSetting("custom-z", "Custom Z", 0.0, -3.0E7, 3.0E7, 1.0)
               .description("Set displayed Z.")
               .numericTextField()
               .visibleWhen(() -> "Custom".equals(this.choice("mode")))
               .build()
         );
         this.add(
            new ActionSetting("anchor", "Anchor", this::load)
               .buttonLabel("Capture Position")
               .description("Use current position.")
               .visibleWhen(() -> "Custom".equals(this.choice("mode")))
               .build()
         );
      }

      @Override
      public boolean showInArrayList() {
         return false;
      }

      @Override
      public void onEnable() {
         autismclient.util.V.Inner_DataUtil m = this.init_V_DataUtil();
         autismclient.util.V.init(m, this.bool("fake-y"));
         if (m == autismclient.util.V.Inner_DataUtil.sync) {
            this.init_V_48();
         }
      }

      @Override
      public void onDisable() {
         autismclient.util.V.init_V();
      }

      @Override
      public void tick() {
         autismclient.util.V.Inner_DataUtil m = this.init_V_DataUtil();
         autismclient.util.V.init(m);
         autismclient.util.V.init(this.bool("fake-y"));
         if (m == autismclient.util.V.Inner_DataUtil.sync) {
            this.init_V_48();
         }
      }

      private void init_V_48() {
         autismclient.util.V.init_V(this.decimal("custom-x"), this.decimal("custom-y"), this.decimal("custom-z"));
      }

      private void load() {
         if (MC.player != null) {
            this.setValue("custom-x", Double.toString(MC.player.getX()));
            this.setValue("custom-y", Double.toString(MC.player.getY()));
            this.setValue("custom-z", Double.toString(MC.player.getZ()));
            this.init_V_48();
         }
      }

      private autismclient.util.V.Inner_DataUtil init_V_DataUtil() {
         autismclient.util.V.Inner_DataUtil var10000;
         switch (this.choice("mode")) {
            case "Scaled":
               var10000 = autismclient.util.V.Inner_DataUtil.load;
               break;
            case "Rotated":
               var10000 = autismclient.util.V.Inner_DataUtil.read;
               break;
            case "Scrambled":
               var10000 = autismclient.util.V.Inner_DataUtil.write;
               break;
            case "Frozen":
               var10000 = autismclient.util.V.Inner_DataUtil.update;
               break;
            case "Custom":
               var10000 = autismclient.util.V.Inner_DataUtil.sync;
               break;
            default:
               var10000 = autismclient.util.V.Inner_DataUtil.init;
         }

         return var10000;
      }
   }
}
