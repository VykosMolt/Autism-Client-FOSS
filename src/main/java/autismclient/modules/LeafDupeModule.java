package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.StringSetting;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.OverlayManager;
import autismclient.util.ServerRotationTracker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class LeafDupeModule extends Module {
   public static final String init_Str = "leaf-dupe";
   public static final String load_Str = init_Str_9();
   private static final int init_I = 6;
   private static final int load_I = 5;
   private static final int read_I = 6;
   private static final int write_I = 100;
   private static final int update_I = 40;
   private static LeafDupeModule init_ac;
   private static boolean init_Z;
   private static float init_F;
   private static float load_F;
   private static int sync = -2147483648;
   private static boolean load_Z;
   private static float read_F;
   private static float write_F;
   private final EnumSetting<LeafDupeModule.Inner_BufferHelper> init_EnumSetting;
   private final EnumSetting<LeafDupeModule.Inner_BufferHelper> load_EnumSetting;
   private final EnumSetting<LeafDupeModule.Inner_BufferHelper> read_EnumSetting;
   private final RegistryListSetting init_RegistryListSettin;
   private final StringSetting init_StringSetting;
   private final StringSetting load_StringSetting;
   private final StringSetting read_StringSetting;
   private final IntSetting init_IntSetting;
   private final IntSetting load_IntSetting;
   private final IntSetting read_IntSetting;
   private final BoolSetting init_BoolSetting;
   private BlockPos init_BlockPos;
   private BlockPos load_BlockPos;
   private BlockPos read_BlockPos;
   private int flush;
   private long init_J;
   private ClientLevel init_ClientLevel;
   private LocalPlayer init_LocalPlayer;
   private String read_Str = "";
   private volatile LeafDupeModule.Inner_DataUtil init_ac_DataUtil;
   private boolean read_Z;
   private autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil init_DataUtil_StreamUti;
   private int close;
   private int open;
   private final autismclient.util.leafdupe.LeafDupeRateLimiter init_CachePool = new autismclient.util.leafdupe.LeafDupeRateLimiter();
   private boolean write_Z;
   private boolean update_Z;
   private long load_J;
   private LeafDupeModule.Inner_CachePool init_ac_CachePool;

   public LeafDupeModule() {
      super("leaf-dupe", "Leaf Dupe", ae.sync, "Automatically bone meals a small dripleaf and re-breaks a whitelisted block.");
      this.init_EnumSetting = this.add(
         new EnumSetting<>("leaf-selection", "Find Dripleaf", LeafDupeModule.Inner_BufferHelper.Automatic, LeafDupeModule.Inner_BufferHelper.values())
            .description("Finds a reachable upper half without its lower half. Custom fixes one position.")
            .group("Dripleaf")
      );
      this.init_StringSetting = this.add(
         new StringSetting("leaf-position", "Position", "")
            .description("Block coordinates: X, Y, Z.")
            .group("Dripleaf")
            .visibleWhen(() -> this.init_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom)
      );
      this.add(
         new ActionSetting(
               "set-leaf", "Leaf: Set Looked-At", () -> this.init_V_StringSetting(this.init_StringSetting, "Leaf position", this::init_Str_BlockPos_3)
            )
            .buttonLabel("Set Looked-At")
            .group("Dripleaf")
            .visibleWhen(() -> this.init_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom)
      );
      this.load_EnumSetting = this.add(
         new EnumSetting<>("dupe-selection", "Find Target", LeafDupeModule.Inner_BufferHelper.Automatic, LeafDupeModule.Inner_BufferHelper.values())
            .description("Finds a visible whitelisted block and starts it automatically with a suitable hotbar tool.")
            .group("Target")
      );
      this.init_RegistryListSettin = this.add(
         RegistryListSetting.blocks("target-blocks", "Whitelist", load_Str)
            .description("Allowed targets in both modes. Includes every shulker color by default.")
            .group("Target")
            .build()
      );
      this.load_StringSetting = this.add(
         new StringSetting("dupe-block-position", "Position", "")
            .description("Block coordinates: X, Y, Z.")
            .group("Target")
            .visibleWhen(() -> this.load_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom)
      );
      this.add(
         new ActionSetting(
               "set-dupe", "Target: Set Looked-At", () -> this.init_V_StringSetting(this.load_StringSetting, "Target position", this::read_Str_BlockPos)
            )
            .buttonLabel("Set Looked-At")
            .group("Target")
            .visibleWhen(() -> this.load_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom)
      );
      this.read_EnumSetting = this.add(
         new EnumSetting<>("reset-selection", "Find Reset Block", LeafDupeModule.Inner_BufferHelper.Automatic, LeafDupeModule.Inner_BufferHelper.values())
            .description("Finds a reachable plain block to click, preferring the block under you.")
            .group("Reset Block")
      );
      this.read_StringSetting = this.add(
         new StringSetting("reset-block-position", "Position", "")
            .description("Block coordinates: X, Y, Z.")
            .group("Reset Block")
            .visibleWhen(() -> this.read_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom)
      );
      this.add(
         new ActionSetting(
               "set-reset",
               "Reset Block: Set Looked-At",
               () -> this.init_V_StringSetting(this.read_StringSetting, "Reset block position", this::load_Str_BlockPos)
            )
            .buttonLabel("Set Looked-At")
            .group("Reset Block")
            .visibleWhen(() -> this.read_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom)
      );
      this.init_IntSetting = this.add(
         new IntSetting("delay", "Cycle Delay", 0, 0, 200, 1).sliderRange(0.0, 20.0).unit("ticks").description("Extra wait between cycles.").group("Timing")
      );
      this.load_IntSetting = this.add(
         new IntSetting("repeat", "Cycle Limit", 0, 0, 10000, 1)
            .sliderRange(0.0, 100.0)
            .unit("cycles")
            .description("Stops after this many cycles. 0 keeps running.")
            .group("Timing")
      );
      this.read_IntSetting = this.add(
         new IntSetting("packet-window-ms", "Packet Window", 350, 0, 5000, 10)
            .sliderRange(0.0, 1000.0)
            .unit("ms")
            .description("Limits interactions to nine per window. 0 removes the limit.")
            .group("Advanced")
      );
      this.init_BoolSetting = this.add(new BoolSetting("debug", "Debug", false).group("Advanced"));
      init_V_Map(AutismConfig.getGlobal().modules.get("leaf-dupe").settings);
      init_ac = this;
   }

   private static String init_Str_9() {
      List<String> ids = new ArrayList<>(List.of("minecraft:diamond_block", "minecraft:netherite_block", "minecraft:shulker_box"));

      for (DyeColor color : DyeColor.values()) {
         ids.add("minecraft:" + color.getName() + "_shulker_box");
      }

      return String.join("|", ids);
   }

   public static void init_V_Map(Map<String, String> settings) {
      if (settings.putIfAbsent("target-defaults-all-colors", "true") == null
         && "minecraft:diamond_block|minecraft:netherite_block|minecraft:shulker_box".equals(settings.get("target-blocks"))) {
         settings.put("target-blocks", load_Str);
      }
   }

   @Override
   public String info() {
      return this.read_Str == null ? "" : this.read_Str;
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.sync();
      this.flush = 0;
      flush();
      this.write_Z = false;
      if (this.init_ac_CachePool != null) {
         this.init_ac_CachePool.init_V();
      }
   }

   private void init_V_StringSetting(StringSetting setting, String label, Function<BlockPos, String> validator) {
      BlockPos pos = this.init_BlockPos_6();
      if (pos != null) {
         String problem = validator.apply(pos);
         if (problem != null) {
            this.write_V_Str_ObjArr("%s: %s", label, problem);
         } else {
            setting.set(autismclient.util.leafdupe.LeafDupeValidator.init(pos));
            this.load_V_Str_ObjArr("%s set to %s.", label, autismclient.util.leafdupe.LeafDupeValidator.init(pos));
         }
      }
   }

   @Override
   public void onEnable() {
      this.close = this.init_IntSetting.get();
      this.open = 0;
      this.update_Z = false;
      this.write_Z = false;
      this.read_Str = "";
      this.sync();
      this.flush = 0;
      this.init_J = 0L;
      this.init_BlockPos = null;
      this.load_BlockPos = null;
      this.read_BlockPos = null;
      if (this.init_ac_CachePool != null) {
         this.init_ac_CachePool.init_V();
      }

      flush();
      this.init_ClientLevel = MC == null ? null : MC.level;
      this.init_LocalPlayer = MC == null ? null : MC.player;
      if (!load_Z_13()) {
         this.read_Str = "No world";
      } else {
         this.init_V_Z_8(true);
         this.read_Str = this.load_Str_4();
      }
   }

   @Override
   public void onDisable() {
      this.sync();
      this.write_Z = false;
      this.update_Z = false;
      flush();
      this.read_Str = "";
      if (this.init_ac_CachePool != null) {
         this.init_ac_CachePool.init_V();
      }

      if (this.open != 0 && load_Z_13() && MC.level == this.init_ClientLevel && MC.player == this.init_LocalPlayer && this.write_Str_2() == null) {
         BlockPos reset = this.read_BlockPos;
         if (reset != null && this.load_Str_BlockPos(reset) == null) {
            int slot = this.load_I_11();
            if (slot >= 0) {
               this.init_ac_CachePool = new LeafDupeModule.Inner_CachePool(slot);
            }
         }
      }
   }

   @Override
   public void onGameLeft() {
      flush();
      this.close();
      if (this.isEnabled()) {
         this.setEnabledSilently(false);
      }
   }

   @Override
   public void preMovementTick() {
      this.read_Z = false;
      this.write_Z = false;
      flush();
      if (this.isEnabled()) {
         if (!load_Z_13()) {
            this.read_Str = "No world";
         } else {
            if (this.init_ClientLevel != MC.level || this.init_LocalPlayer != MC.player) {
               this.init_ClientLevel = MC.level;
               this.init_LocalPlayer = MC.player;
               this.init_BlockPos = this.load_BlockPos = this.read_BlockPos = null;
               this.sync();
               this.flush = 0;
               this.open = 0;
               this.init_J = 0L;
               this.init_CachePool.init();
               autismclient.util.leafdupe.LeafDupePacketTracker.load();
            }

            if (this.init_DataUtil_StreamUti != null) {
               if (!this.init_DataUtil_StreamUti.init_Z()) {
                  if (!this.init_DataUtil_StreamUti.load() && !this.init_DataUtil_StreamUti.init(System.nanoTime())) {
                     this.read_Str = "";
                     return;
                  }

                  this.sync();
                  this.read_Str = "Packets blocked";
                  return;
               }

               this.open++;
               this.close = 0;
               this.init_DataUtil_StreamUti = null;
               if (this.load_IntSetting.get() > 0 && this.open >= this.load_IntSetting.get()) {
                  this.update_Z = true;
                  return;
               }
            }

            String paused = this.write_Str_2();
            if (paused != null) {
               this.sync();
               this.read_Str = paused;
            } else {
               autismclient.util.leafdupe.LeafDupePacketTracker.read();
               this.init_V_Z_8(false);
               String blocked = this.load_Str_4();
               if (blocked != null) {
                  this.sync();
               }

               if (blocked != null || this.init_Z_32()) {
                  if (blocked == null && this.close < this.init_IntSetting.get()) {
                     this.close++;
                     this.read_Str = "";
                  } else {
                     if (blocked == null) {
                        blocked = this.read_Str_2();
                     }

                     this.read_Str = init_Str_Str_2(blocked);
                     if (blocked != null) {
                        this.update();
                        this.init_V_Str_ObjArr("Waiting: %s.", blocked);
                     } else {
                        int slot = this.load_I_11();
                        if (autismclient.util.InventorySlotArbiter.init(slot, "leaf-dupe")) {
                           this.read_Str = "Hand busy";
                        } else {
                           if (!this.init_Z_I_8(slot)) {
                              if (!autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
                                 this.read_Str = "Hand busy";
                                 return;
                              }

                              boolean selected;
                              try {
                                 selected = this.init_Z_I_5(slot, this.init_ac_DataUtil, null);
                              } finally {
                                 autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
                              }

                              if (!selected) {
                                 this.read_Str = "Bone meal slot";
                                 return;
                              }
                           }

                           if (slot == 40 && autismclient.util.InventorySlotArbiter.load_Z("leaf-dupe")) {
                              this.read_Str = "Hand busy";
                           } else {
                              Vec3 eye = this.init_Vec3_5();
                              float lastYaw = this.init_F_6();
                              float lastPitch = this.load_F_4();
                              float[] aim = this.init_FArr_Vec3(eye, lastYaw, lastPitch);
                              if (aim == null) {
                                 boolean resetVisible = autismclient.util.leafdupe.LeafDupeValidator.init(
                                    eye, lastYaw, lastPitch, this.read_BlockPos, MC.player.blockInteractionRange()
                                 );
                                 boolean leafVisible = autismclient.util.leafdupe.LeafDupeValidator.init(
                                    eye, lastYaw, lastPitch, this.init_BlockPos, MC.player.blockInteractionRange()
                                 );
                                 if (resetVisible) {
                                    aim = autismclient.util.leafdupe.LeafDupeValidator.init_FArr_Vec3(
                                       eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.init_BlockPos), lastYaw
                                    );
                                 } else if (leafVisible) {
                                    aim = autismclient.util.leafdupe.LeafDupeValidator.init_FArr_Vec3(
                                       eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.read_BlockPos), lastYaw
                                    );
                                 } else {
                                    aim = autismclient.util.leafdupe.LeafDupeValidator.init_FArr_Vec3(
                                       eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.read_BlockPos), lastYaw
                                    );
                                    this.read_Str = "No shared aim";
                                 }
                              }

                              init_V_F(aim[0], aim[1], 100);
                              this.write_Z = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void read_V_15() {
      if (this.write_Z) {
         this.write_Z = false;
         if (this.isEnabled() && load_Z_13() && MC.level == this.init_ClientLevel) {
            String blocked = this.load_Str_4();
            if (blocked != null) {
               this.sync();
               flush();
            }

            if (blocked == null) {
               blocked = this.read_Str_2();
            }

            this.read_Str = init_Str_Str_2(blocked);
            if (blocked == null) {
               Vec3 eye = this.init_Vec3_5();
               float[][] looks = this.init_FArr2();
               double reach = MC.player.blockInteractionRange();
               if (autismclient.util.leafdupe.LeafDupeValidator.init(eye, looks, this.read_BlockPos, reach)
                  && autismclient.util.leafdupe.LeafDupeValidator.init(eye, looks, this.init_BlockPos, reach)
                  && autismclient.util.leafdupe.LeafDupeValidator.init(MC.player.getEyePosition(), looks, this.read_BlockPos, reach)
                  && autismclient.util.leafdupe.LeafDupeValidator.init(MC.player.getEyePosition(), looks, this.init_BlockPos, reach)) {
                  int slot = this.load_I_11();
                  if (this.init_Z_I_8(slot) && !autismclient.util.InventorySlotArbiter.init(slot, "leaf-dupe") && (slot != 40 || !autismclient.util.InventorySlotArbiter.load_Z("leaf-dupe"))) {
                     if (!autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
                        this.read_Str = "Hand busy";
                     } else {
                        boolean sent;
                        try {
                           sent = this.init_Z_InteractionHand_Vec3(this.init_InteractionHand_I(slot), eye);
                        } finally {
                           autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
                        }

                        if (sent) {
                        }
                     }
                  } else {
                     this.read_Str = "Bone meal slot";
                  }
               } else {
                  this.read_Str = "Aim blocked";
                  this.init_V_Str_ObjArr("Waiting: the rotation does not reach both blocks yet (another module rotating?).");
               }
            }
         }
      }
   }

   private boolean init_Z_32() {
      BlockState state = MC.level.getBlockState(this.load_BlockPos);
      LeafDupeModule.Inner_DataUtil current = this.init_ac_DataUtil;
      if (current != null
         && current.init_BlockPos.equals(this.load_BlockPos)
         && current.init_BlockState == state
         && autismclient.util.leafdupe.LeafDupePacketTracker.init(current.init_DataUtil_StreamUti, this.load_BlockPos)) {
         return true;
      }

      if (current != null
         && (
            !current.init_BlockPos.equals(this.load_BlockPos)
               || current.init_BlockState != state
               || System.nanoTime() - current.init_J > 500000000L
               || current.init_DataUtil_StreamUti != null && current.init_DataUtil_StreamUti.init_Z()
         )) {
         this.sync();
         current = null;
      }

      if (current == null) {
         int toolSlot = this.init_I_BlockState(state);
         if (toolSlot < 0) {
            this.read_Str = "No suitable hotbar tool";
            return false;
         }

         current = new LeafDupeModule.Inner_DataUtil(this.load_BlockPos, state, toolSlot);
         this.init_ac_DataUtil = current;
      }

      if (!autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
         this.read_Str = "Hand busy";
         return false;
      }

      try {
         if (!this.init_Z_I_5(current.init_I, current, null)) {
            this.read_Str = "Tool slot blocked";
            return false;
         }
      } finally {
         autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
      }

      double damage = state.getDestroyProgress(MC.player, MC.level, current.init_BlockPos);
      if (!init_Z_D_4(damage)) {
         this.read_Str = damage >= 1.0 ? "Target breaks instantly" : "Cannot mine target";
         this.sync();
         return false;
      } else {
         BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.init(MC, current.init_BlockPos, this.init_Vec3_5());
         if (hit == null) {
            this.read_Str = "Target obstructed";
            this.update();
            return false;
         } else {
            Vec3 point = hit.getLocation().add(hit.getLocation().subtract(this.init_Vec3_5()).normalize().scale(0.01));
            float[] aim = autismclient.util.leafdupe.LeafDupeValidator.init_FArr_Vec3(this.init_Vec3_5(), point, this.init_F_6());
            init_V_F(aim[0], aim[1], 100);
            this.read_Z = true;
            this.read_Str = current.init_DataUtil_StreamUti != null && current.init_DataUtil_StreamUti.load() ? "Target start blocked" : "";
            return false;
         }
      }
   }

   private void write_V_15() {
      this.read_Z = false;
      LeafDupeModule.Inner_DataUtil current = this.init_ac_DataUtil;
      if (current != null && current.init_DataUtil_StreamUti == null && this.load_Str_4() == null) {
         if (autismclient.util.leafdupe.LeafDupePacketTracker.init_I() == current.init_I) {
            if (MC.player.getInventory().getSelectedSlot() == current.init_I
               && ItemStack.isSameItemSameComponents(current.init_ItemStack, MC.player.getMainHandItem())) {
               BlockHitResult hit = this.init_BlockHitResult_Vec3_BlockPos(this.init_Vec3_5(), current.init_BlockPos);
               if (hit != null && this.init_BlockHitResult_Vec3_BlockPos(MC.player.getEyePosition(), current.init_BlockPos) != null) {
                  double damage = current.init_BlockState.getDestroyProgress(MC.player, MC.level, current.init_BlockPos);
                  if (!init_Z_D_4(damage) || MC.player.blockActionRestricted(MC.level, current.init_BlockPos, MC.gameMode.getPlayerMode())) {
                     this.read_Str = "Cannot mine target";
                     this.sync();
                  } else if (autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
                     try (BlockStatePredictionHandler prediction = this.init_BlockStatePredicti().startPredicting()) {
                        ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                           Action.START_DESTROY_BLOCK, current.init_BlockPos, hit.getDirection(), prediction.currentSequence()
                        );
                        current.init_DataUtil_StreamUti = autismclient.util.leafdupe.LeafDupePacketTracker.init(
                           packet, this.init_DataUtil_CachePool_ac_DataUtil(current), current.init_I, damage
                        );
                        MC.getConnection().send(packet);
                     } finally {
                        autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
                     }
                  }
               }
            } else {
               this.sync();
            }
         } else {
            if (autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
               try {
                  this.init_Z_I_5(current.init_I, current, null);
               } finally {
                  autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
               }
            }

            this.read_Str = current.load_DataUtil_StreamUti != null
                  && !current.load_DataUtil_StreamUti.load()
                  && !current.load_DataUtil_StreamUti.init(System.nanoTime())
               ? ""
               : "Tool slot blocked";
         }
      }
   }

   static boolean init_Z_D_4(double damage) {
      return Double.isFinite(damage) && damage > 0.0 && damage < 1.0;
   }

   private BlockHitResult init_BlockHitResult_Vec3_BlockPos(Vec3 eye, BlockPos target) {
      Vec3 end = eye.add(Vec3.directionFromRotation(this.load_F_4(), this.init_F_6()).scale(MC.player.blockInteractionRange() - 0.05));
      BlockHitResult hit = MC.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, MC.player));
      return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(target) ? hit : null;
   }

   private int init_I_BlockState(BlockState state) {
      int best = -1;
      float bestSpeed = 1.0F;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = MC.player.getInventory().getItem(slot);
         if (!autismclient.util.InventorySlotArbiter.init(slot, "leaf-dupe") && init_Z_ItemStack_BlockState(stack, state)) {
            float speed = autismclient.util.BlockBreakSpeedHelper.init(MC, stack, state);
            if (speed > bestSpeed || speed == bestSpeed && slot == MC.player.getInventory().getSelectedSlot()) {
               best = slot;
               bestSpeed = speed;
            }
         }
      }

      return best;
   }

   static boolean init_Z_ItemStack_BlockState(ItemStack stack, BlockState state) {
      return !stack.isEmpty()
         && stack.getDestroySpeed(state) > 1.0F
         && (!state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state))
         && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 2);
   }

   private boolean init_Z_I_5(int slot, LeafDupeModule.Inner_DataUtil owner, autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil previous) {
      if (slot >= 0 && slot <= 8 && !autismclient.util.InventorySlotArbiter.init(slot, "leaf-dupe")) {
         MC.player.getInventory().setSelectedSlot(slot);
         ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$setCarriedIndex(slot);
         if (previous == null && autismclient.util.leafdupe.LeafDupePacketTracker.init_I() == slot) {
            return true;
         }

         if (previous == null
            && owner.load_DataUtil_StreamUti != null
            && owner.load_I == slot
            && !owner.load_DataUtil_StreamUti.load()
            && !owner.load_DataUtil_StreamUti.init(System.nanoTime())
            && owner.load_DataUtil_StreamUti.init_Obj() instanceof autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool proof
            && proof.init(owner, MC.player.getEyePosition(), this.init_Vec3_5())) {
            return true;
         }

         if (previous == null && owner.load_DataUtil_StreamUti != null && !owner.load_DataUtil_StreamUti.init_Z()) {
            owner.load_DataUtil_StreamUti.init_V();
         }

         ServerboundSetCarriedItemPacket packet = new ServerboundSetCarriedItemPacket(slot);
         owner.load_I = slot;
         owner.load_DataUtil_StreamUti = autismclient.util.leafdupe.LeafDupePacketTracker.init(packet, this.init_DataUtil_CachePool_ac_DataUtil(owner), previous);
         MC.getConnection().send(packet);
         return true;
      } else {
         return false;
      }
   }

   private void update() {
      if (this.init_ac_DataUtil != null
         && !autismclient.util.leafdupe.LeafDupePacketTracker.init(this.init_ac_DataUtil.init_DataUtil_StreamUti, this.init_ac_DataUtil.init_BlockPos)) {
         this.sync();
      }
   }

   private void sync() {
      this.read_Z = false;
      LeafDupeModule.Inner_DataUtil current = this.init_ac_DataUtil;
      if (current != null) {
         current.init_Z = true;
      }

      this.init_ac_DataUtil = null;
      if (current != null) {
         if (current.init_DataUtil_StreamUti != null) {
            current.init_DataUtil_StreamUti.init_V();
         }

         if (current.load_DataUtil_StreamUti != null) {
            current.load_DataUtil_StreamUti.init_V();
         }

         if (load_Z_13()
            && current.init_ClientLevel == MC.level
            && current.init_LocalPlayer == MC.player
            && current.init_Connection == MC.getConnection().getConnection()
            && MC.gameMode != null
            && MC.player.getInventory().getSelectedSlot() == current.load_I
            && autismclient.util.InventorySlotArbiter.read_Z("leaf-dupe")
            && autismclient.util.leafdupe.LeafDupePacketTracker.init_I() >= 0) {
            ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$setCarriedIndex(autismclient.util.leafdupe.LeafDupePacketTracker.init_I());
         }
      }

      if (this.init_DataUtil_StreamUti != null) {
         if (this.init_DataUtil_StreamUti.init_Z()) {
            this.open++;
            this.close = 0;
         } else {
            this.init_DataUtil_StreamUti.init_V();
         }

         this.init_DataUtil_StreamUti = null;
      }
   }

   private autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool init_DataUtil_CachePool_ac_DataUtil(LeafDupeModule.Inner_DataUtil owner) {
      return new autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool(owner, MC.player.getEyePosition(), this.init_Vec3_5());
   }

   public static boolean init_Z_Connection_Packet(Connection connection, Packet<?> packet) {
      autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil receipt = autismclient.util.leafdupe.LeafDupePacketTracker.init_DataUtil_StreamUti(packet);
      if (receipt == null) {
         return true;
      }

      LeafDupeModule module = init_ac;
      autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool proof = receipt.init_Obj() instanceof autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool value ? value : null;
      LeafDupeModule.Inner_DataUtil owner = proof != null && proof.init() instanceof LeafDupeModule.Inner_DataUtil value ? value : null;
      boolean valid = module != null
         && owner != null
         && !owner.init_Z
         && owner == module.init_ac_DataUtil
         && owner.init_Connection == connection
         && load_Z_13()
         && owner.init_ClientLevel == MC.level
         && owner.init_LocalPlayer == MC.player
         && owner.init_BlockPos.equals(module.load_BlockPos)
         && MC.level.isLoaded(owner.init_BlockPos)
         && MC.level.getBlockState(owner.init_BlockPos) == owner.init_BlockState
         && MC.gui.screen() == null
         && MC.mouseHandler.isMouseGrabbed()
         && !aD.init_Z()
         && !MC.options.keyAttack.isDown()
         && !MC.options.keyUse.isDown()
         && !MC.player.isUsingItem()
         && MC.player.isAlive()
         && !MC.player.hasInfiniteMaterials()
         && autismclient.util.InventorySlotArbiter.read_Z("leaf-dupe")
         && !autismclient.util.InventorySlotArbiter.init(receipt.init_I(), "leaf-dupe")
         && proof.init(owner, MC.player.getEyePosition(), module.init_Vec3_5());
      if (valid && packet instanceof ServerboundUseItemOnPacket use) {
         ItemStack stack = use.hand() == InteractionHand.OFF_HAND ? MC.player.getOffhandItem() : MC.player.getInventory().getItem(receipt.init_I());
         valid = stack.is(Items.BONE_MEAL)
            && autismclient.util.leafdupe.LeafDupePacketTracker.init(owner.init_DataUtil_StreamUti, owner.init_BlockPos)
            && (use.hand() != InteractionHand.OFF_HAND || !autismclient.util.InventorySlotArbiter.load_Z("leaf-dupe"));
      } else if (valid && packet instanceof ServerboundPlayerActionPacket action) {
         valid = ItemStack.isSameItemSameComponents(owner.init_ItemStack, MC.player.getInventory().getItem(owner.init_I));
         if (action.getAction() == Action.START_DESTROY_BLOCK) {
            valid &= MC.player.getInventory().getSelectedSlot() == owner.init_I
               && init_Z_D_4(owner.init_BlockState.getDestroyProgress(MC.player, MC.level, owner.init_BlockPos));
         }

         if (action.getAction() == Action.STOP_DESTROY_BLOCK) {
            valid &= autismclient.util.leafdupe.LeafDupePacketTracker.init(owner.init_DataUtil_StreamUti, owner.init_BlockPos);
         }
      }

      return autismclient.util.leafdupe.LeafDupePacketTracker.init(packet, valid);
   }

   private String load_Str_4() {
      if (!load_Z_13()) {
         return "No world";
      }

      if (MC.player.hasInfiniteMaterials()) {
         return "Survival required";
      }

      String paused = this.write_Str_2();
      if (paused != null) {
         return paused;
      }

      if (this.init_RegistryListSettin.get().isEmpty()) {
         return "Whitelist empty";
      }

      if (this.init_BlockPos == null) {
         return this.init_Str_EnumSetting(this.init_EnumSetting, this.init_StringSetting, "leaf");
      }

      if (this.load_BlockPos == null) {
         return this.init_Str_EnumSetting(this.load_EnumSetting, this.load_StringSetting, "target");
      }

      if (this.read_BlockPos == null) {
         return this.init_Str_EnumSetting(this.read_EnumSetting, this.read_StringSetting, "reset block");
      }

      if (MC.level.isLoaded(this.init_BlockPos) && MC.level.isLoaded(this.load_BlockPos) && MC.level.isLoaded(this.read_BlockPos)) {
         String leafProblem = this.init_Str_BlockPos_3(this.init_BlockPos);
         if (leafProblem != null) {
            return leafProblem.contains("lower half") ? "Remove lower half" : "Leaf invalid";
         }

         String targetProblem = this.read_Str_BlockPos(this.load_BlockPos);
         if (targetProblem != null) {
            this.init_V_Str_ObjArr("Target rejected: %s (%s)", targetProblem, MC.level.getBlockState(this.load_BlockPos));
            return "Target invalid";
         }

         if (this.read_BlockPos.equals(this.load_BlockPos)) {
            return "Reset is target";
         }

         if (this.load_Str_BlockPos(this.read_BlockPos) != null) {
            return "Reset invalid";
         }

         if (this.load_I_11() < 0) {
            return "No bone meal";
         }

         String access = autismclient.util.leafdupe.LeafDupeValidator.init(
            this.init_Vec3_5(),
            MC.player.getEyePosition(),
            List.of(this.init_BlockPos, this.load_BlockPos, this.read_BlockPos),
            MC.player.blockInteractionRange() - 0.05,
            this::init_AABB_BlockPos_2,
            (pos, eye) -> autismclient.ai.engine.AiBlockInteraction.init(MC, pos, eye) != null
         );
         return access != null ? access : null;
      } else {
         return "Out of reach";
      }
   }

   private String init_Str_EnumSetting(EnumSetting<LeafDupeModule.Inner_BufferHelper> selection, StringSetting setting, String label) {
      if (selection.get() == LeafDupeModule.Inner_BufferHelper.Automatic) {
         return "No " + label;
      } else {
         BlockPos custom = init_BlockPos_StringSetting(setting);
         if (custom == null) {
            return "Set " + label;
         } else if (MC.level != null && MC.level.isLoaded(custom)) {
            return label.equals("leaf") && this.init_Str_BlockPos_3(custom) != null
                  || label.equals("target") && this.read_Str_BlockPos(custom) != null
                  || label.equals("reset block") && this.load_Str_BlockPos(custom) != null
               ? "Invalid " + label
               : "Out of reach";
         } else {
            return "Out of reach";
         }
      }
   }

   private void init_V_Z_8(boolean force) {
      if (this.init_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom) {
         this.init_BlockPos = init_BlockPos_StringSetting(this.init_StringSetting);
      }

      if (this.load_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom) {
         this.load_BlockPos = init_BlockPos_StringSetting(this.load_StringSetting);
      }

      if (this.read_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Custom) {
         this.read_BlockPos = init_BlockPos_StringSetting(this.read_StringSetting);
      }

      if (force || --this.flush <= 0) {
         this.flush = 5;
         Vec3 eye = this.init_Vec3_5();
         boolean leaf = this.init_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Automatic && !this.init_Z_BlockPos_Vec3_2(this.init_BlockPos, eye);
         boolean target = this.load_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Automatic && !this.load_Z_BlockPos_Vec3(this.load_BlockPos, eye);
         boolean reset = this.read_EnumSetting.get() == LeafDupeModule.Inner_BufferHelper.Automatic && !this.read_Z_BlockPos_Vec3(this.read_BlockPos, eye);
         if (leaf || target || reset) {
            this.init_V_Vec3(eye, leaf, target, reset);
         }
      }
   }

   private void init_V_Vec3(Vec3 eye, boolean seekLeaf, boolean seekTarget, boolean seekReset) {
      List<LeafDupeModule.Inner_StreamUtil> leaves = new ArrayList<>();
      List<LeafDupeModule.Inner_StreamUtil> targets = new ArrayList<>();
      List<LeafDupeModule.Inner_StreamUtil> resets = new ArrayList<>();
      Set<String> allowed = new HashSet<>(this.init_RegistryListSettin.get());
      double reach = MC.player.blockInteractionRange();
      double maxDistance = (reach + 1.0) * (reach + 1.0);

      for (BlockPos pos : BlockPos.withinClippedManhattan(MC.player.blockPosition(), 6, 6, 6)) {
         if (MC.level.isLoaded(pos)) {
            double distance = eye.distanceToSqr(Vec3.atCenterOf(pos));
            if (!(distance > maxDistance)) {
               BlockState state = MC.level.getBlockState(pos);
               if (seekLeaf && state.is(Blocks.SMALL_DRIPLEAF) && this.init_Str_BlockPos_3(pos) == null) {
                  leaves.add(new LeafDupeModule.Inner_StreamUtil(pos.immutable(), distance));
               }

               if (seekTarget && allowed.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())) {
                  targets.add(new LeafDupeModule.Inner_StreamUtil(pos.immutable(), distance));
               }

               if (seekReset
                  && !pos.equals(this.load_BlockPos)
                  && !state.isAir()
                  && state.getFluidState().isEmpty()
                  && !state.hasBlockEntity()
                  && !(state.getBlock() instanceof BonemealableBlock)
                  && autismclient.util.leafdupe.LeafDupeValidator.init_Str_BlockState_2(state, true, false) == null) {
                  resets.add(new LeafDupeModule.Inner_StreamUtil(pos.immutable(), distance));
               }
            }
         }
      }

      leaves.sort(Comparator.comparingDouble(LeafDupeModule.Inner_StreamUtil::init_D));
      targets.sort(Comparator.comparingDouble(LeafDupeModule.Inner_StreamUtil::init_D));
      resets.sort(Comparator.comparingDouble(LeafDupeModule.Inner_StreamUtil::init_D));
      this.init_V_Str_ObjArr("Scan: %d leaf, %d target, %d reset candidates; whitelist=%s", leaves.size(), targets.size(), resets.size(), allowed);
      if (seekLeaf) {
         BlockPos found = this.init_BlockPos_List_Vec3(leaves, eye);
         if (found != null) {
            this.init_BlockPos = found;
         }
      }

      if (seekTarget) {
         BlockPos found = this.init_BlockPos_List_Vec3(targets, eye);
         if (found != null) {
            this.load_BlockPos = found;
         }
      }

      if (seekReset) {
         List<LeafDupeModule.Inner_StreamUtil> visible = new ArrayList<>();

         for (LeafDupeModule.Inner_StreamUtil candidate : this.init_List_List_2(resets)) {
            if (this.read_Z_BlockPos_Vec3(candidate.init_BlockPos(), eye)) {
               visible.add(candidate);
            }
         }

         BlockPos below = MC.player.blockPosition().below();
         if (this.read_Z_BlockPos_Vec3(below, eye)) {
            this.read_BlockPos = below;
         } else if (!visible.isEmpty()) {
            BlockPos found = visible.getFirst().init_BlockPos();

            for (LeafDupeModule.Inner_StreamUtil candidate : visible) {
               if (autismclient.util.leafdupe.LeafDupeValidator.init(eye, 0.0F, 0.0F, candidate.init_BlockPos(), this.init_BlockPos, reach) != null) {
                  found = candidate.init_BlockPos();
                  break;
               }
            }

            this.read_BlockPos = found;
         }
      }

      this.init_J++;
   }

   private BlockPos init_BlockPos_List_Vec3(List<LeafDupeModule.Inner_StreamUtil> candidates, Vec3 eye) {
      for (LeafDupeModule.Inner_StreamUtil candidate : this.init_List_List_2(candidates)) {
         if (autismclient.ai.engine.AiBlockInteraction.init(MC, candidate.init_BlockPos(), eye) != null) {
            return candidate.init_BlockPos();
         }
      }

      return null;
   }

   private List<LeafDupeModule.Inner_StreamUtil> init_List_List_2(List<LeafDupeModule.Inner_StreamUtil> candidates) {
      return autismclient.util.leafdupe.LeafDupeValidator.init_List_List(candidates, this.init_J, 6);
   }

   private boolean init_Z_BlockPos_Vec3_2(BlockPos pos, Vec3 eye) {
      return pos != null && this.init_Str_BlockPos_3(pos) == null && autismclient.ai.engine.AiBlockInteraction.init(MC, pos, eye) != null;
   }

   private boolean load_Z_BlockPos_Vec3(BlockPos pos, Vec3 eye) {
      return pos != null && this.read_Str_BlockPos(pos) == null && autismclient.ai.engine.AiBlockInteraction.init(MC, pos, eye) != null;
   }

   private boolean read_Z_BlockPos_Vec3(BlockPos pos, Vec3 eye) {
      return pos != null && !pos.equals(this.load_BlockPos) && this.load_Str_BlockPos(pos) == null && this.init_BlockHitResult_BlockPos_Vec3(pos, eye) != null;
   }

   private float[][] init_FArr2() {
      float lastYaw = this.init_F_6();
      float lastPitch = this.load_F_4();
      if (!load_Z) {
         return new float[][]{{lastYaw, lastPitch}};
      } else {
         return Float.compare(read_F, lastYaw) == 0 && Float.compare(write_F, lastPitch) == 0
            ? new float[][]{{lastYaw, lastPitch}}
            : new float[][]{{read_F, write_F}, {lastYaw, lastPitch}};
      }
   }

   private String read_Str_2() {
      if (this.load_BlockPos != null && autismclient.util.leafdupe.LeafDupePacketTracker.load(this.load_BlockPos)) {
         return "Restoring target";
      } else {
         long wait = autismclient.util.leafdupe.LeafDupePacketTracker.init_J();
         if (wait == 9223372036854775807L) {
            return "Use suitable tool";
         } else if (wait > 0L) {
            return "FastBreak timing";
         } else {
            return this.read_IntSetting.get() > 0 && this.init_I_21() < 2 ? "Packet window" : null;
         }
      }
   }

   private boolean init_Z_InteractionHand_Vec3(InteractionHand hand, Vec3 eye) {
      LeafDupeModule.Inner_DataUtil current = this.init_ac_DataUtil;
      if (current != null
         && autismclient.util.leafdupe.LeafDupePacketTracker.init(current.init_DataUtil_StreamUti, this.load_BlockPos)
         && init_Z_ItemStack_BlockState(MC.player.getInventory().getItem(current.init_I), current.init_BlockState)) {
         BlockHitResult reset = this.init_BlockHitResult_BlockPos_Vec3(this.read_BlockPos, eye);
         BlockHitResult leaf = autismclient.ai.engine.AiBlockInteraction.init(MC, this.init_BlockPos, eye);
         if (reset != null && leaf != null) {
            current.init_ItemStack = MC.player.getInventory().getItem(current.init_I).copy();
            int mealSlot = MC.player.getInventory().getSelectedSlot();
            if (!this.init_Z_I_5(mealSlot, current, null)) {
               return false;
            }

            autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil resetWrite = this.init_DataUtil_StreamUti_InteractionHand(hand, reset, mealSlot, current, null);
            autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil leafWrite = this.init_DataUtil_StreamUti_InteractionHand(hand, leaf, mealSlot, current, resetWrite);
            if (!this.init_Z_I_5(current.init_I, current, leafWrite)) {
               return false;
            }

            boolean abort = autismclient.util.leafdupe.LeafDupePacketTracker.init(this.load_BlockPos);
            if (abort) {
               MC.getConnection().send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.load_BlockPos, Direction.DOWN));
            }

            Direction stopFace = this.init_Direction_Vec3(eye, this.load_BlockPos, Direction.DOWN, false);

            try (BlockStatePredictionHandler prediction = this.init_BlockStatePredicti().startPredicting()) {
               ServerboundPlayerActionPacket stop = new ServerboundPlayerActionPacket(
                  Action.STOP_DESTROY_BLOCK, this.load_BlockPos, stopFace, prediction.currentSequence()
               );
               this.init_DataUtil_StreamUti = autismclient.util.leafdupe.LeafDupePacketTracker.init(
                  stop, this.init_DataUtil_CachePool_ac_DataUtil(current), current.init_I, leafWrite
               );
               MC.getConnection().send(stop);
            }

            if (this.init_BoolSetting.get()) {
               this.load_V_Str_ObjArr(
                  "Cycle queued: reset %s, leaf %s, stop %s%s.", reset.getDirection(), leaf.getDirection(), stopFace, abort ? ", abort first" : ""
               );
            }

            return true;
         } else {
            this.read_Str = reset == null ? "Reset face blocked" : "Leaf obstructed";
            this.flush = 0;
            return false;
         }
      } else {
         return false;
      }
   }

   private BlockHitResult init_BlockHitResult_BlockPos_Vec3(BlockPos pos, Vec3 eye) {
      BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.init(MC, pos, eye);
      return hit != null && MC.level.getFluidState(pos.relative(hit.getDirection())).isEmpty() ? hit : null;
   }

   private void init_V_Str_ObjArr(String message, Object... args) {
      if (this.init_BoolSetting.get()) {
         long now = System.currentTimeMillis();
         if (now - this.load_J >= 1000L) {
            this.load_J = now;
            this.load_V_Str_ObjArr(message, args);
         }
      }
   }

   private Vec3 init_Vec3_5() {
      AutismLocalPlayerAccessor player = (AutismLocalPlayerAccessor)MC.player;
      double eyeHeight = MC.player.getEyeY() - MC.player.getY();
      Vec3 written = ServerRotationTracker.init_ch_CachePool().init_Vec3();
      return written != null
         ? written.add(0.0, eyeHeight, 0.0)
         : new Vec3(player.autism$getLastSentX(), player.autism$getLastSentY() + eyeHeight, player.autism$getLastSentZ());
   }

   private float init_F_6() {
      float written = ServerRotationTracker.init_F();
      return Float.isFinite(written) ? written : ((AutismLocalPlayerAccessor)MC.player).autism$getLastSentYaw();
   }

   private float load_F_4() {
      ServerRotationTracker.Inner_CachePool written = ServerRotationTracker.init_ch_CachePool();
      return written.init_Z() ? written.write() : ((AutismLocalPlayerAccessor)MC.player).autism$getLastSentPitch();
   }

   private static boolean load_Z_13() {
      return MC != null && MC.player != null && MC.level != null && MC.getConnection() != null;
   }

   private String write_Str_2() {
      if (aD.init_Z()) {
         return "Paused";
      } else if (MC.gui.screen() != null || OverlayManager.init_bk().update_Z()) {
         return "GUI open";
      } else if (!MC.mouseHandler.isMouseGrabbed()) {
         return "Cursor released";
      } else if (!MC.player.isAlive() || MC.player.isSpectator() || MC.player.isPassenger() || MC.getCameraEntity() != MC.player) {
         return "Player unavailable";
      } else if (!MC.player.isUsingItem() && !MC.options.keyAttack.isDown() && !MC.options.keyUse.isDown()) {
         return !autismclient.util.InventorySlotArbiter.read_Z("leaf-dupe") ? "Hand busy" : null;
      } else {
         return "Manual input";
      }
   }

   private static String init_Str_Str_2(String reason) {
      return reason != null && !reason.equals("Packet window") && !reason.equals("FastBreak timing") && !reason.equals("Restoring target") ? reason : "";
   }

   private float[] init_FArr_Vec3(Vec3 eye, float lastYaw, float lastPitch) {
      return autismclient.util.leafdupe.LeafDupeValidator.init(eye, lastYaw, lastPitch, this.read_BlockPos, this.init_BlockPos, MC.player.blockInteractionRange());
   }

   private Direction init_Direction_Vec3(Vec3 eye, BlockPos pos, Direction preferred, boolean avoidFluids) {
      return autismclient.util.leafdupe.LeafDupeValidator.init(eye, pos, preferred, avoidFluids, neighbor -> !MC.level.getFluidState(neighbor).isEmpty());
   }

   private int init_I_21() {
      return this.init_CachePool.init_I(this.read_IntSetting.get(), System.currentTimeMillis());
   }

   private void init_V_InteractionHand(InteractionHand hand, BlockPos pos, Vec3 point, Direction face) {
      this.init_CachePool.init_V(this.read_IntSetting.get(), System.currentTimeMillis());
      BlockHitResult hit = new BlockHitResult(point, face, pos, false);

      try (BlockStatePredictionHandler prediction = this.init_BlockStatePredicti().startPredicting()) {
         MC.getConnection().send(new ServerboundUseItemOnPacket(hand, hit, prediction.currentSequence()));
      }
   }

   private autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil init_DataUtil_StreamUti_InteractionHand(
      InteractionHand hand, BlockHitResult hit, int slot, LeafDupeModule.Inner_DataUtil owner, autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil previous
   ) {
      this.init_CachePool.init_V(this.read_IntSetting.get(), System.currentTimeMillis());

      try (BlockStatePredictionHandler prediction = this.init_BlockStatePredicti().startPredicting()) {
         ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(hand, hit, prediction.currentSequence());
         autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil receipt = autismclient.util.leafdupe.LeafDupePacketTracker.init(
            packet, this.init_DataUtil_CachePool_ac_DataUtil(owner), slot, previous
         );
         MC.getConnection().send(packet);
         return receipt;
      }
   }

   private BlockStatePredictionHandler init_BlockStatePredicti() {
      return ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler();
   }

   private int load_I_11() {
      if (MC.player == null) {
         return -1;
      }

      if (MC.player.getOffhandItem().is(Items.BONE_MEAL)) {
         return 40;
      }

      int selected = MC.player.getInventory().getSelectedSlot();
      if (MC.player.getInventory().getItem(selected).is(Items.BONE_MEAL)) {
         return selected;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (MC.player.getInventory().getItem(slot).is(Items.BONE_MEAL)) {
            return slot;
         }
      }

      return -1;
   }

   private boolean init_Z_I_8(int slot) {
      return slot >= 0 && MC.player != null ? slot == 40 || slot == MC.player.getInventory().getSelectedSlot() : false;
   }

   private InteractionHand init_InteractionHand_I(int slot) {
      return slot == 40 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   private boolean load_Z_I_4(int slot) {
      if (slot >= 0 && slot <= 8 && MC.player != null) {
         autismclient.util.InventorySlotHelper.init_V(MC, slot);
         return MC.player.getInventory().getSelectedSlot() == slot;
      } else {
         return false;
      }
   }

   private String init_Str_BlockPos_3(BlockPos pos) {
      if (MC.level == null) {
         return "no world.";
      } else {
         return !MC.level.isLoaded(pos)
            ? "chunk is not loaded."
            : autismclient.util.leafdupe.LeafDupeValidator.init_Str_BlockState(MC.level.getBlockState(pos), MC.level.getBlockState(pos.below()), true);
      }
   }

   private String load_Str_BlockPos(BlockPos pos) {
      if (MC.level == null) {
         return "no world.";
      }

      if (!MC.level.isLoaded(pos)) {
         return "chunk is not loaded.";
      }

      BlockState state = MC.level.getBlockState(pos);
      return autismclient.util.leafdupe.LeafDupeValidator.init_Str_BlockState_2(state, true, state.getMenuProvider(MC.level, pos) != null);
   }

   private String read_Str_BlockPos(BlockPos pos) {
      if (MC.level == null) {
         return "no world.";
      } else if (!MC.level.isLoaded(pos)) {
         return "chunk is not loaded.";
      } else {
         BlockState state = MC.level.getBlockState(pos);
         if (state.isAir()) {
            return "that is air, there is nothing to break.";
         } else {
            return !this.init_RegistryListSettin.get().contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())
               ? state.getBlock().getName().getString() + " is not in the target whitelist."
               : null;
         }
      }
   }

   private BlockPos init_BlockPos_6() {
      if (MC.player != null && MC.level != null) {
         if (MC.hitResult instanceof BlockHitResult block && block.getType() == Type.BLOCK) {
            return block.getBlockPos().immutable();
         } else {
            this.write_V_Str_ObjArr("You are not looking at a block.");
            return null;
         }
      } else {
         this.write_V_Str_ObjArr("You are not in a world.");
         return null;
      }
   }

   private AABB init_AABB_BlockPos_2(BlockPos pos) {
      VoxelShape shape = MC.level.getBlockState(pos).getShape(MC.level, pos);
      return shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
   }

   private static BlockPos init_BlockPos_StringSetting(StringSetting setting) {
      return autismclient.util.leafdupe.LeafDupeValidator.init(setting.get());
   }

   private void load_V_Str_ObjArr(String message, Object... args) {
      this.init_V_ChatFormatting(ChatFormatting.GRAY, message, args);
   }

   private void read_V_Str_ObjArr(String message, Object... args) {
      this.init_V_ChatFormatting(ChatFormatting.YELLOW, message, args);
   }

   private void write_V_Str_ObjArr(String message, Object... args) {
      this.init_V_ChatFormatting(ChatFormatting.RED, message, args);
   }

   private void init_V_ChatFormatting(ChatFormatting color, String message, Object... args) {
      String text = args.length == 0 ? message : String.format(Locale.ROOT, message, args);
      AutismClientMessaging.send(Component.empty().append(AutismClientMessaging.themedTag(this.name())).append(Component.literal(text).withStyle(color)));
   }

   private static void init_V_F(float yaw, float pitch, int priority) {
      if (!init_Z || priority >= sync) {
         init_F = yaw;
         load_F = pitch;
         sync = priority;
         init_Z = true;
      }
   }

   private static void flush() {
      init_Z = false;
      load_Z = false;
      sync = -2147483648;
   }

   public static void init_V_LocalPlayer_2(LocalPlayer player) {
      LeafDupeModule module = init_ac;
      if (module != null && player == MC.player) {
         if (!init_Z && !module.write_Z && !module.read_Z && module.init_ac_CachePool == null) {
            load_Z = false;
         } else if (load_Z_13() && MC.level == module.init_ClientLevel && MC.player == module.init_LocalPlayer && module.write_Str_2() == null) {
            if (module.init_ac_CachePool != null) {
               module.init_ac_CachePool.load();
            }

            load_Z = init_Z;
            if (init_Z) {
               read_F = init_F;
               write_F = load_F;
            }

            init_Z = false;
            sync = -2147483648;
            if (module.init_ac_CachePool != null) {
               module.init_ac_CachePool.read();
            }

            if (module.read_Z) {
               module.write_V_15();
            } else {
               module.read_V_15();
            }
         } else {
            flush();
            module.write_Z = false;
            module.sync();
            if (module.init_ac_CachePool != null) {
               module.init_ac_CachePool.init_V();
            }
         }
      }
   }

   public static float init_F_LocalPlayer_F_2(LocalPlayer player, float original) {
      return player == MC.player && load_Z ? read_F : original;
   }

   public static float load_F_LocalPlayer_F_2(LocalPlayer player, float original) {
      return player == MC.player && load_Z ? write_F : original;
   }

   public static void load_V_LocalPlayer_2(LocalPlayer player) {
      load_Z = false;
      LeafDupeModule module = init_ac;
      if (module != null && player == MC.player && module.update_Z) {
         module.update_Z = false;
         module.load_V_Str_ObjArr("Finished %d cycle(s).", module.open);
         module.setEnabled(false);
      }
   }

   public static void init_V_28() {
      flush();
      if (init_ac != null) {
         init_ac.close();
      }

      autismclient.util.leafdupe.LeafDupePacketTracker.init_V();
      autismclient.util.leafdupe.LeafDupePacketTracker.init(0);
   }

   public static void load_V_21() {
      flush();
      if (init_ac != null) {
         init_ac.close();
      }

      autismclient.util.leafdupe.LeafDupePacketTracker.init_V();
   }

   private void close() {
      this.sync();
      if (this.init_ac_CachePool != null) {
         this.init_ac_CachePool.init_V();
      }

      this.write_Z = false;
      this.update_Z = false;
      this.init_ClientLevel = null;
      this.init_LocalPlayer = null;
      this.init_BlockPos = this.load_BlockPos = this.read_BlockPos = null;
      this.flush = 0;
      this.init_J = 0L;
      this.open = 0;
      this.init_CachePool.init();
   }

   private enum Inner_BufferHelper {
      Automatic,
      Custom;
   }

   private final class Inner_CachePool {
      private final InteractionHand init_InteractionHand;
      private final int init_I;
      private final ClientLevel init_ClientLevel = Module.MC.level;
      private final LocalPlayer init_LocalPlayer = Module.MC.player;
      private final ClientPacketListener init_ClientPacketListen = Module.MC.getConnection();
      private final BlockPos init_BlockPos = LeafDupeModule.this.read_BlockPos;
      private final int load = Module.MC.player.getInventory().getSelectedSlot();
      private int read = 20;

      private Inner_CachePool(int mealSlot) {
         this.init_I = mealSlot;
         this.init_InteractionHand = LeafDupeModule.this.init_InteractionHand_I(mealSlot);
      }

      private void init_V() {
         if (LeafDupeModule.this.init_ac_CachePool == this) {
            LeafDupeModule.this.init_ac_CachePool = null;
         }
      }

      private void load() {
         if (!this.init_Z()) {
            this.init_V();
         } else if (this.read-- <= 0) {
            LeafDupeModule.this.read_V_Str_ObjArr("Grim: could not send the last reset click (rotation or packet window), skipped it.");
            this.init_V();
         } else if (this.init_InteractionHand != InteractionHand.MAIN_HAND || LeafDupeModule.this.init_Z_I_8(this.init_I)) {
            Vec3 eye = LeafDupeModule.this.init_Vec3_5();
            float lastYaw = LeafDupeModule.this.init_F_6();
            float[] aim = autismclient.util.leafdupe.LeafDupeValidator.init_FArr_Vec3(eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.init_BlockPos), lastYaw);
            LeafDupeModule.init_V_F(aim[0], aim[1], 100);
         } else if (autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
            try {
               if (!LeafDupeModule.this.load_Z_I_4(this.init_I)) {
                  this.init_V();
               }
            } finally {
               autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
            }
         }
      }

      private void read() {
         if (LeafDupeModule.this.init_ac_CachePool == this) {
            if (!this.init_Z()) {
               this.init_V();
            } else {
               Vec3 eye = LeafDupeModule.this.init_Vec3_5();
               if (autismclient.util.leafdupe.LeafDupeValidator.init(eye, LeafDupeModule.this.init_FArr2(), this.init_BlockPos, Module.MC.player.blockInteractionRange())
                  && LeafDupeModule.this.init_I_21() >= 1) {
                  if (!Module.MC.player.getItemInHand(this.init_InteractionHand).is(Items.BONE_MEAL)) {
                     this.init_V();
                  } else if (autismclient.util.InventorySlotArbiter.write_Z("leaf-dupe")) {
                     try {
                        BlockHitResult hit = LeafDupeModule.this.init_BlockHitResult_BlockPos_Vec3(this.init_BlockPos, eye);
                        if (hit == null) {
                           this.init_V();
                           return;
                        }

                        LeafDupeModule.this.init_V_InteractionHand(this.init_InteractionHand, this.init_BlockPos, hit.getLocation(), hit.getDirection());
                     } finally {
                        autismclient.util.InventorySlotArbiter.read_V("leaf-dupe");
                     }

                     if (LeafDupeModule.this.init_BoolSetting.get()) {
                        LeafDupeModule.this.load_V_Str_ObjArr("Last reset click sent.");
                     }

                     this.init_V();
                     return;
                  }
               }
            }
         }
      }

      private boolean init_Z() {
         return LeafDupeModule.load_Z_13()
               && Module.MC.level == this.init_ClientLevel
               && Module.MC.player == this.init_LocalPlayer
               && Module.MC.getConnection() == this.init_ClientPacketListen
               && LeafDupeModule.this.write_Str_2() == null
               && this.init_BlockPos != null
               && LeafDupeModule.this.load_Str_BlockPos(this.init_BlockPos) == null
               && Module.MC.player.getInventory().getItem(this.init_I).is(Items.BONE_MEAL)
               && (
                  this.init_InteractionHand != InteractionHand.MAIN_HAND
                     || Module.MC.player.getInventory().getSelectedSlot() == this.load
                     || Module.MC.player.getInventory().getSelectedSlot() == this.init_I
               )
               && (this.init_InteractionHand != InteractionHand.OFF_HAND || !autismclient.util.InventorySlotArbiter.load_Z("leaf-dupe"))
               && !autismclient.util.InventorySlotArbiter.init(
                  this.init_InteractionHand == InteractionHand.OFF_HAND ? 40 : Module.MC.player.getInventory().getSelectedSlot(), "leaf-dupe"
               )
            ? autismclient.ai.engine.AiBlockInteraction.init(Module.MC, this.init_BlockPos, LeafDupeModule.this.init_Vec3_5()) != null
               && autismclient.ai.engine.AiBlockInteraction.init(Module.MC, this.init_BlockPos, Module.MC.player.getEyePosition()) != null
            : false;
      }
   }

   private final class Inner_DataUtil {
      final ClientLevel init_ClientLevel = Module.MC.level;
      final LocalPlayer init_LocalPlayer = Module.MC.player;
      final Connection init_Connection = Module.MC.getConnection().getConnection();
      final BlockPos init_BlockPos;
      final BlockState init_BlockState;
      final int init_I;
      ItemStack init_ItemStack;
      final long init_J = System.nanoTime();
      volatile autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil init_DataUtil_StreamUti;
      volatile autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil load_DataUtil_StreamUti;
      int load_I = -1;
      volatile boolean init_Z;

      Inner_DataUtil(BlockPos target, BlockState state, int slot) {
         this.init_BlockPos = target.immutable();
         this.init_BlockState = state;
         this.init_I = slot;
         this.init_ItemStack = Module.MC.player.getInventory().getItem(slot).copy();
      }
   }

   private record Inner_StreamUtil(BlockPos init_BlockPos, double init_D) {
   }
}
