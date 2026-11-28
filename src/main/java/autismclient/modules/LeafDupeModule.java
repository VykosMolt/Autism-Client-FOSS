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
   public static final String MODULE_ID = "leaf-dupe";
   public static final String DEFAULT_TARGET_BLOCKS = defaultTargetBlocks();
   private static final int init_I = 6;
   private static final int POSITION_REFRESH_TICKS = 5;
   private static final int read_I = 6;
   private static final int AIM_PRIORITY = 100;
   private static final int update_I = 40;
   private static LeafDupeModule instance;
   private static boolean rotationRequested;
   private static float requestedYaw;
   private static float requestedPitch;
   private static int rotationPriority = -2147483648;
   private static boolean rotationOverrideActive;
   private static float appliedYaw;
   private static float appliedPitch;
   private final EnumSetting<LeafDupeModule.SelectionMode> leafMode;
   private final EnumSetting<LeafDupeModule.SelectionMode> targetMode;
   private final EnumSetting<LeafDupeModule.SelectionMode> resetMode;
   private final RegistryListSetting targetBlocks;
   private final StringSetting leafPosition;
   private final StringSetting targetPosition;
   private final StringSetting resetPosition;
   private final IntSetting cycleDelay;
   private final IntSetting cycleLimit;
   private final IntSetting packetWindowMs;
   private final BoolSetting debug;
   private BlockPos leafPos;
   private BlockPos targetPos;
   private BlockPos resetPos;
   private int flush;
   private long candidatePage;
   private ClientLevel sessionLevel;
   private LocalPlayer sessionPlayer;
   private String status = "";
   private volatile LeafDupeModule.BreakAttempt breakAttempt;
   private boolean breakStartPending;
   private autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil cycleReceipt;
   private int cycleDelayTicks;
   private int cycleCount;
   private final autismclient.util.leafdupe.LeafDupeRateLimiter rateLimiter = new autismclient.util.leafdupe.LeafDupeRateLimiter();
   private boolean boneMealPending;
   private boolean cycleLimitReached;
   private long lastDebugMillis;
   private LeafDupeModule.PendingResetClick pendingResetClick;

   public LeafDupeModule() {
      super("leaf-dupe", "Leaf Dupe", ae.sync, "Automatically bone meals a small dripleaf and re-breaks a whitelisted block.");
      this.leafMode = this.add(
         new EnumSetting<>("leaf-selection", "Find Dripleaf", LeafDupeModule.SelectionMode.Automatic, LeafDupeModule.SelectionMode.values())
            .description("Finds a reachable upper half without its lower half. Custom fixes one position.")
            .group("Dripleaf")
      );
      this.leafPosition = this.add(
         new StringSetting("leaf-position", "Position", "")
            .description("Block coordinates: X, Y, Z.")
            .group("Dripleaf")
            .visibleWhen(() -> this.leafMode.get() == LeafDupeModule.SelectionMode.Custom)
      );
      this.add(
         new ActionSetting(
               "set-leaf", "Leaf: Set Looked-At", () -> this.setFromLookedAt(this.leafPosition, "Leaf position", this::leafProblem)
            )
            .buttonLabel("Set Looked-At")
            .group("Dripleaf")
            .visibleWhen(() -> this.leafMode.get() == LeafDupeModule.SelectionMode.Custom)
      );
      this.targetMode = this.add(
         new EnumSetting<>("dupe-selection", "Find Target", LeafDupeModule.SelectionMode.Automatic, LeafDupeModule.SelectionMode.values())
            .description("Finds a visible whitelisted block and starts it automatically with a suitable hotbar tool.")
            .group("Target")
      );
      this.targetBlocks = this.add(
         RegistryListSetting.blocks("target-blocks", "Whitelist", DEFAULT_TARGET_BLOCKS)
            .description("Allowed targets in both modes. Includes every shulker color by default.")
            .group("Target")
            .build()
      );
      this.targetPosition = this.add(
         new StringSetting("dupe-block-position", "Position", "")
            .description("Block coordinates: X, Y, Z.")
            .group("Target")
            .visibleWhen(() -> this.targetMode.get() == LeafDupeModule.SelectionMode.Custom)
      );
      this.add(
         new ActionSetting(
               "set-dupe", "Target: Set Looked-At", () -> this.setFromLookedAt(this.targetPosition, "Target position", this::targetProblem)
            )
            .buttonLabel("Set Looked-At")
            .group("Target")
            .visibleWhen(() -> this.targetMode.get() == LeafDupeModule.SelectionMode.Custom)
      );
      this.resetMode = this.add(
         new EnumSetting<>("reset-selection", "Find Reset Block", LeafDupeModule.SelectionMode.Automatic, LeafDupeModule.SelectionMode.values())
            .description("Finds a reachable plain block to click, preferring the block under you.")
            .group("Reset Block")
      );
      this.resetPosition = this.add(
         new StringSetting("reset-block-position", "Position", "")
            .description("Block coordinates: X, Y, Z.")
            .group("Reset Block")
            .visibleWhen(() -> this.resetMode.get() == LeafDupeModule.SelectionMode.Custom)
      );
      this.add(
         new ActionSetting(
               "set-reset",
               "Reset Block: Set Looked-At",
               () -> this.setFromLookedAt(this.resetPosition, "Reset block position", this::resetProblem)
            )
            .buttonLabel("Set Looked-At")
            .group("Reset Block")
            .visibleWhen(() -> this.resetMode.get() == LeafDupeModule.SelectionMode.Custom)
      );
      this.cycleDelay = this.add(
         new IntSetting("delay", "Cycle Delay", 0, 0, 200, 1).sliderRange(0.0, 20.0).unit("ticks").description("Extra wait between cycles.").group("Timing")
      );
      this.cycleLimit = this.add(
         new IntSetting("repeat", "Cycle Limit", 0, 0, 10000, 1)
            .sliderRange(0.0, 100.0)
            .unit("cycles")
            .description("Stops after this many cycles. 0 keeps running.")
            .group("Timing")
      );
      this.packetWindowMs = this.add(
         new IntSetting("packet-window-ms", "Packet Window", 350, 0, 5000, 10)
            .sliderRange(0.0, 1000.0)
            .unit("ms")
            .description("Limits interactions to nine per window. 0 removes the limit.")
            .group("Advanced")
      );
      this.debug = this.add(new BoolSetting("debug", "Debug", false).group("Advanced"));
      applySettingDefaults(AutismConfig.getGlobal().modules.get("leaf-dupe").settings);
      instance = this;
   }

   private static String defaultTargetBlocks() {
      List<String> ids = new ArrayList<>(List.of("minecraft:diamond_block", "minecraft:netherite_block", "minecraft:shulker_box"));

      for (DyeColor color : DyeColor.values()) {
         ids.add("minecraft:" + color.getName() + "_shulker_box");
      }

      return String.join("|", ids);
   }

   public static void applySettingDefaults(Map<String, String> settings) {
      if (settings.putIfAbsent("target-defaults-all-colors", "true") == null
         && "minecraft:diamond_block|minecraft:netherite_block|minecraft:shulker_box".equals(settings.get("target-blocks"))) {
         settings.put("target-blocks", DEFAULT_TARGET_BLOCKS);
      }
   }

   @Override
   public String info() {
      return this.status == null ? "" : this.status;
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.cancelBreakAttempt();
      this.flush = 0;
      releaseRotation();
      this.boneMealPending = false;
      if (this.pendingResetClick != null) {
         this.pendingResetClick.detach();
      }
   }

   private void setFromLookedAt(StringSetting setting, String label, Function<BlockPos, String> validator) {
      BlockPos pos = this.lookedAtBlock();
      if (pos != null) {
         String problem = validator.apply(pos);
         if (problem != null) {
            this.chatError("%s: %s", label, problem);
         } else {
            setting.set(autismclient.util.leafdupe.LeafDupeValidator.init(pos));
            this.chatInfo("%s set to %s.", label, autismclient.util.leafdupe.LeafDupeValidator.init(pos));
         }
      }
   }

   @Override
   public void onEnable() {
      this.cycleDelayTicks = this.cycleDelay.get();
      this.cycleCount = 0;
      this.cycleLimitReached = false;
      this.boneMealPending = false;
      this.status = "";
      this.cancelBreakAttempt();
      this.flush = 0;
      this.candidatePage = 0L;
      this.leafPos = null;
      this.targetPos = null;
      this.resetPos = null;
      if (this.pendingResetClick != null) {
         this.pendingResetClick.detach();
      }

      releaseRotation();
      this.sessionLevel = MC == null ? null : MC.level;
      this.sessionPlayer = MC == null ? null : MC.player;
      if (!inWorld()) {
         this.status = "No world";
      } else {
         this.refreshPositions(true);
         this.status = this.computeStatus();
      }
   }

   @Override
   public void onDisable() {
      this.cancelBreakAttempt();
      this.boneMealPending = false;
      this.cycleLimitReached = false;
      releaseRotation();
      this.status = "";
      if (this.pendingResetClick != null) {
         this.pendingResetClick.detach();
      }

      if (this.cycleCount != 0 && inWorld() && MC.level == this.sessionLevel && MC.player == this.sessionPlayer && this.pauseReason() == null) {
         BlockPos reset = this.resetPos;
         if (reset != null && this.resetProblem(reset) == null) {
            int slot = this.boneMealSlot();
            if (slot >= 0) {
               this.pendingResetClick = new LeafDupeModule.PendingResetClick(slot);
            }
         }
      }
   }

   @Override
   public void onGameLeft() {
      releaseRotation();
      this.endSession();
      if (this.isEnabled()) {
         this.setEnabledSilently(false);
      }
   }

   @Override
   public void preMovementTick() {
      this.breakStartPending = false;
      this.boneMealPending = false;
      releaseRotation();
      if (this.isEnabled()) {
         if (!inWorld()) {
            this.status = "No world";
         } else {
            if (this.sessionLevel != MC.level || this.sessionPlayer != MC.player) {
               this.sessionLevel = MC.level;
               this.sessionPlayer = MC.player;
               this.leafPos = this.targetPos = this.resetPos = null;
               this.cancelBreakAttempt();
               this.flush = 0;
               this.cycleCount = 0;
               this.candidatePage = 0L;
               this.rateLimiter.init();
               autismclient.util.leafdupe.LeafDupePacketTracker.load();
            }

            if (this.cycleReceipt != null) {
               if (!this.cycleReceipt.isSent()) {
                  if (!this.cycleReceipt.load() && !this.cycleReceipt.init(System.nanoTime())) {
                     this.status = "";
                     return;
                  }

                  this.cancelBreakAttempt();
                  this.status = "Packets blocked";
                  return;
               }

               this.cycleCount++;
               this.cycleDelayTicks = 0;
               this.cycleReceipt = null;
               if (this.cycleLimit.get() > 0 && this.cycleCount >= this.cycleLimit.get()) {
                  this.cycleLimitReached = true;
                  return;
               }
            }

            String paused = this.pauseReason();
            if (paused != null) {
               this.cancelBreakAttempt();
               this.status = paused;
            } else {
               autismclient.util.leafdupe.LeafDupePacketTracker.read();
               this.refreshPositions(false);
               String blocked = this.computeStatus();
               if (blocked != null) {
                  this.cancelBreakAttempt();
               }

               if (blocked != null || this.prepareTargetBreak()) {
                  if (blocked == null && this.cycleDelayTicks < this.cycleDelay.get()) {
                     this.cycleDelayTicks++;
                     this.status = "";
                  } else {
                     if (blocked == null) {
                        blocked = this.blockReason();
                     }

                     this.status = visibleStatus(blocked);
                     if (blocked != null) {
                        this.cancelIfReceiptLost();
                        this.debugLog("Waiting: %s.", blocked);
                     } else {
                        int slot = this.boneMealSlot();
                        if (autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "leaf-dupe")) {
                           this.status = "Hand busy";
                        } else {
                           if (!this.isSlotSelected(slot)) {
                              if (!autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
                                 this.status = "Hand busy";
                                 return;
                              }

                              boolean selected;
                              try {
                                 selected = this.selectSlotTracked(slot, this.breakAttempt, null);
                              } finally {
                                 autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
                              }

                              if (!selected) {
                                 this.status = "Bone meal slot";
                                 return;
                              }
                           }

                           if (slot == 40 && autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("leaf-dupe")) {
                              this.status = "Hand busy";
                           } else {
                              Vec3 eye = this.eyePosition();
                              float lastYaw = this.lastWireYaw();
                              float lastPitch = this.lastWirePitch();
                              float[] aim = this.aimForResetAndLeaf(eye, lastYaw, lastPitch);
                              if (aim == null) {
                                 boolean resetVisible = autismclient.util.leafdupe.LeafDupeValidator.init(
                                    eye, lastYaw, lastPitch, this.resetPos, MC.player.blockInteractionRange()
                                 );
                                 boolean leafVisible = autismclient.util.leafdupe.LeafDupeValidator.init(
                                    eye, lastYaw, lastPitch, this.leafPos, MC.player.blockInteractionRange()
                                 );
                                 if (resetVisible) {
                                    aim = autismclient.util.leafdupe.LeafDupeValidator.aimAt(
                                       eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.leafPos), lastYaw
                                    );
                                 } else if (leafVisible) {
                                    aim = autismclient.util.leafdupe.LeafDupeValidator.aimAt(
                                       eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.resetPos), lastYaw
                                    );
                                 } else {
                                    aim = autismclient.util.leafdupe.LeafDupeValidator.aimAt(
                                       eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.resetPos), lastYaw
                                    );
                                    this.status = "No shared aim";
                                 }
                              }

                              requestRotation(aim[0], aim[1], 100);
                              this.boneMealPending = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void sendBoneMealCycle() {
      if (this.boneMealPending) {
         this.boneMealPending = false;
         if (this.isEnabled() && inWorld() && MC.level == this.sessionLevel) {
            String blocked = this.computeStatus();
            if (blocked != null) {
               this.cancelBreakAttempt();
               releaseRotation();
            }

            if (blocked == null) {
               blocked = this.blockReason();
            }

            this.status = visibleStatus(blocked);
            if (blocked == null) {
               Vec3 eye = this.eyePosition();
               float[][] looks = this.candidateLooks();
               double reach = MC.player.blockInteractionRange();
               if (autismclient.util.leafdupe.LeafDupeValidator.init(eye, looks, this.resetPos, reach)
                  && autismclient.util.leafdupe.LeafDupeValidator.init(eye, looks, this.leafPos, reach)
                  && autismclient.util.leafdupe.LeafDupeValidator.init(MC.player.getEyePosition(), looks, this.resetPos, reach)
                  && autismclient.util.leafdupe.LeafDupeValidator.init(MC.player.getEyePosition(), looks, this.leafPos, reach)) {
                  int slot = this.boneMealSlot();
                  if (this.isSlotSelected(slot) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "leaf-dupe") && (slot != 40 || !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("leaf-dupe"))) {
                     if (!autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
                        this.status = "Hand busy";
                     } else {
                        boolean sent;
                        try {
                           sent = this.sendUseSequence(this.handForSlot(slot), eye);
                        } finally {
                           autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
                        }

                        if (sent) {
                        }
                     }
                  } else {
                     this.status = "Bone meal slot";
                  }
               } else {
                  this.status = "Aim blocked";
                  this.debugLog("Waiting: the rotation does not reach both blocks yet (another module rotating?).");
               }
            }
         }
      }
   }

   private boolean prepareTargetBreak() {
      BlockState state = MC.level.getBlockState(this.targetPos);
      LeafDupeModule.BreakAttempt current = this.breakAttempt;
      if (current != null
         && current.pos.equals(this.targetPos)
         && current.state == state
         && autismclient.util.leafdupe.LeafDupePacketTracker.init(current.breakReceipt, this.targetPos)) {
         return true;
      }

      if (current != null
         && (
            !current.pos.equals(this.targetPos)
               || current.state != state
               || System.nanoTime() - current.createdNanos > 500000000L
               || current.breakReceipt != null && current.breakReceipt.isSent()
         )) {
         this.cancelBreakAttempt();
         current = null;
      }

      if (current == null) {
         int toolSlot = this.bestToolSlot(state);
         if (toolSlot < 0) {
            this.status = "No suitable hotbar tool";
            return false;
         }

         current = new LeafDupeModule.BreakAttempt(this.targetPos, state, toolSlot);
         this.breakAttempt = current;
      }

      if (!autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
         this.status = "Hand busy";
         return false;
      }

      try {
         if (!this.selectSlotTracked(current.slot, current, null)) {
            this.status = "Tool slot blocked";
            return false;
         }
      } finally {
         autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
      }

      double damage = state.getDestroyProgress(MC.player, MC.level, current.pos);
      if (!isPartialDamage(damage)) {
         this.status = damage >= 1.0 ? "Target breaks instantly" : "Cannot mine target";
         this.cancelBreakAttempt();
         return false;
      } else {
         BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, current.pos, this.eyePosition());
         if (hit == null) {
            this.status = "Target obstructed";
            this.cancelIfReceiptLost();
            return false;
         } else {
            Vec3 point = hit.getLocation().add(hit.getLocation().subtract(this.eyePosition()).normalize().scale(0.01));
            float[] aim = autismclient.util.leafdupe.LeafDupeValidator.aimAt(this.eyePosition(), point, this.lastWireYaw());
            requestRotation(aim[0], aim[1], 100);
            this.breakStartPending = true;
            this.status = current.breakReceipt != null && current.breakReceipt.load() ? "Target start blocked" : "";
            return false;
         }
      }
   }

   private void sendBreakStart() {
      this.breakStartPending = false;
      LeafDupeModule.BreakAttempt current = this.breakAttempt;
      if (current != null && current.breakReceipt == null && this.computeStatus() == null) {
         if (autismclient.util.leafdupe.LeafDupePacketTracker.trackedSlot() == current.slot) {
            if (MC.player.getInventory().getSelectedSlot() == current.slot
               && ItemStack.isSameItemSameComponents(current.heldStack, MC.player.getMainHandItem())) {
               BlockHitResult hit = this.raycastTo(this.eyePosition(), current.pos);
               if (hit != null && this.raycastTo(MC.player.getEyePosition(), current.pos) != null) {
                  double damage = current.state.getDestroyProgress(MC.player, MC.level, current.pos);
                  if (!isPartialDamage(damage) || MC.player.blockActionRestricted(MC.level, current.pos, MC.gameMode.getPlayerMode())) {
                     this.status = "Cannot mine target";
                     this.cancelBreakAttempt();
                  } else if (autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
                     try (BlockStatePredictionHandler prediction = this.predictionHandler().startPredicting()) {
                        ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                           Action.START_DESTROY_BLOCK, current.pos, hit.getDirection(), prediction.currentSequence()
                        );
                        current.breakReceipt = autismclient.util.leafdupe.LeafDupePacketTracker.init(
                           packet, this.proofFor(current), current.slot, damage
                        );
                        MC.getConnection().send(packet);
                     } finally {
                        autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
                     }
                  }
               }
            } else {
               this.cancelBreakAttempt();
            }
         } else {
            if (autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
               try {
                  this.selectSlotTracked(current.slot, current, null);
               } finally {
                  autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
               }
            }

            this.status = current.slotReceipt != null
                  && !current.slotReceipt.load()
                  && !current.slotReceipt.init(System.nanoTime())
               ? ""
               : "Tool slot blocked";
         }
      }
   }

   static boolean isPartialDamage(double damage) {
      return Double.isFinite(damage) && damage > 0.0 && damage < 1.0;
   }

   private BlockHitResult raycastTo(Vec3 eye, BlockPos target) {
      Vec3 end = eye.add(Vec3.directionFromRotation(this.lastWirePitch(), this.lastWireYaw()).scale(MC.player.blockInteractionRange() - 0.05));
      BlockHitResult hit = MC.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, MC.player));
      return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(target) ? hit : null;
   }

   private int bestToolSlot(BlockState state) {
      int best = -1;
      float bestSpeed = 1.0F;

      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = MC.player.getInventory().getItem(slot);
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "leaf-dupe") && isFasterTool(stack, state)) {
            float speed = autismclient.util.BlockBreakSpeedHelper.init(MC, stack, state);
            if (speed > bestSpeed || speed == bestSpeed && slot == MC.player.getInventory().getSelectedSlot()) {
               best = slot;
               bestSpeed = speed;
            }
         }
      }

      return best;
   }

   static boolean isFasterTool(ItemStack stack, BlockState state) {
      return !stack.isEmpty()
         && stack.getDestroySpeed(state) > 1.0F
         && (!state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state))
         && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 2);
   }

   private boolean selectSlotTracked(int slot, LeafDupeModule.BreakAttempt owner, autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil previous) {
      if (slot >= 0 && slot <= 8 && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "leaf-dupe")) {
         MC.player.getInventory().setSelectedSlot(slot);
         ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$setCarriedIndex(slot);
         if (previous == null && autismclient.util.leafdupe.LeafDupePacketTracker.trackedSlot() == slot) {
            return true;
         }

         if (previous == null
            && owner.slotReceipt != null
            && owner.sentSlot == slot
            && !owner.slotReceipt.load()
            && !owner.slotReceipt.init(System.nanoTime())
            && owner.slotReceipt.proof() instanceof autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool proof
            && proof.init(owner, MC.player.getEyePosition(), this.eyePosition())) {
            return true;
         }

         if (previous == null && owner.slotReceipt != null && !owner.slotReceipt.isSent()) {
            owner.slotReceipt.markConsumed();
         }

         ServerboundSetCarriedItemPacket packet = new ServerboundSetCarriedItemPacket(slot);
         owner.sentSlot = slot;
         owner.slotReceipt = autismclient.util.leafdupe.LeafDupePacketTracker.init(packet, this.proofFor(owner), previous);
         MC.getConnection().send(packet);
         return true;
      } else {
         return false;
      }
   }

   private void cancelIfReceiptLost() {
      if (this.breakAttempt != null
         && !autismclient.util.leafdupe.LeafDupePacketTracker.init(this.breakAttempt.breakReceipt, this.breakAttempt.pos)) {
         this.cancelBreakAttempt();
      }
   }

   private void cancelBreakAttempt() {
      this.breakStartPending = false;
      LeafDupeModule.BreakAttempt current = this.breakAttempt;
      if (current != null) {
         current.cancelled = true;
      }

      this.breakAttempt = null;
      if (current != null) {
         if (current.breakReceipt != null) {
            current.breakReceipt.markConsumed();
         }

         if (current.slotReceipt != null) {
            current.slotReceipt.markConsumed();
         }

         if (inWorld()
            && current.level == MC.level
            && current.player == MC.player
            && current.connection == MC.getConnection().getConnection()
            && MC.gameMode != null
            && MC.player.getInventory().getSelectedSlot() == current.sentSlot
            && autismclient.util.InventorySlotArbiter.canBeginActionGroup("leaf-dupe")
            && autismclient.util.leafdupe.LeafDupePacketTracker.trackedSlot() >= 0) {
            ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$setCarriedIndex(autismclient.util.leafdupe.LeafDupePacketTracker.trackedSlot());
         }
      }

      if (this.cycleReceipt != null) {
         if (this.cycleReceipt.isSent()) {
            this.cycleCount++;
            this.cycleDelayTicks = 0;
         } else {
            this.cycleReceipt.markConsumed();
         }

         this.cycleReceipt = null;
      }
   }

   private autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool proofFor(LeafDupeModule.BreakAttempt owner) {
      return new autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool(owner, MC.player.getEyePosition(), this.eyePosition());
   }

   public static boolean allowSend(Connection connection, Packet<?> packet) {
      autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil receipt = autismclient.util.leafdupe.LeafDupePacketTracker.receiptFor(packet);
      if (receipt == null) {
         return true;
      }

      LeafDupeModule module = instance;
      autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool proof = receipt.proof() instanceof autismclient.util.leafdupe.LeafDupePacketTracker.Inner_CachePool value ? value : null;
      LeafDupeModule.BreakAttempt owner = proof != null && proof.init() instanceof LeafDupeModule.BreakAttempt value ? value : null;
      boolean valid = module != null
         && owner != null
         && !owner.cancelled
         && owner == module.breakAttempt
         && owner.connection == connection
         && inWorld()
         && owner.level == MC.level
         && owner.player == MC.player
         && owner.pos.equals(module.targetPos)
         && MC.level.isLoaded(owner.pos)
         && MC.level.getBlockState(owner.pos) == owner.state
         && MC.gui.screen() == null
         && MC.mouseHandler.isMouseGrabbed()
         && !aD.init_Z()
         && !MC.options.keyAttack.isDown()
         && !MC.options.keyUse.isDown()
         && !MC.player.isUsingItem()
         && MC.player.isAlive()
         && !MC.player.hasInfiniteMaterials()
         && autismclient.util.InventorySlotArbiter.canBeginActionGroup("leaf-dupe")
         && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(receipt.slot(), "leaf-dupe")
         && proof.init(owner, MC.player.getEyePosition(), module.eyePosition());
      if (valid && packet instanceof ServerboundUseItemOnPacket use) {
         ItemStack stack = use.hand() == InteractionHand.OFF_HAND ? MC.player.getOffhandItem() : MC.player.getInventory().getItem(receipt.slot());
         valid = stack.is(Items.BONE_MEAL)
            && autismclient.util.leafdupe.LeafDupePacketTracker.init(owner.breakReceipt, owner.pos)
            && (use.hand() != InteractionHand.OFF_HAND || !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("leaf-dupe"));
      } else if (valid && packet instanceof ServerboundPlayerActionPacket action) {
         valid = ItemStack.isSameItemSameComponents(owner.heldStack, MC.player.getInventory().getItem(owner.slot));
         if (action.getAction() == Action.START_DESTROY_BLOCK) {
            valid &= MC.player.getInventory().getSelectedSlot() == owner.slot
               && isPartialDamage(owner.state.getDestroyProgress(MC.player, MC.level, owner.pos));
         }

         if (action.getAction() == Action.STOP_DESTROY_BLOCK) {
            valid &= autismclient.util.leafdupe.LeafDupePacketTracker.init(owner.breakReceipt, owner.pos);
         }
      }

      return autismclient.util.leafdupe.LeafDupePacketTracker.init(packet, valid);
   }

   private String computeStatus() {
      if (!inWorld()) {
         return "No world";
      }

      if (MC.player.hasInfiniteMaterials()) {
         return "Survival required";
      }

      String paused = this.pauseReason();
      if (paused != null) {
         return paused;
      }

      if (this.targetBlocks.get().isEmpty()) {
         return "Whitelist empty";
      }

      if (this.leafPos == null) {
         return this.describeSelection(this.leafMode, this.leafPosition, "leaf");
      }

      if (this.targetPos == null) {
         return this.describeSelection(this.targetMode, this.targetPosition, "target");
      }

      if (this.resetPos == null) {
         return this.describeSelection(this.resetMode, this.resetPosition, "reset block");
      }

      if (MC.level.isLoaded(this.leafPos) && MC.level.isLoaded(this.targetPos) && MC.level.isLoaded(this.resetPos)) {
         String leafProblem = this.leafProblem(this.leafPos);
         if (leafProblem != null) {
            return leafProblem.contains("lower half") ? "Remove lower half" : "Leaf invalid";
         }

         String targetProblem = this.targetProblem(this.targetPos);
         if (targetProblem != null) {
            this.debugLog("Target rejected: %s (%s)", targetProblem, MC.level.getBlockState(this.targetPos));
            return "Target invalid";
         }

         if (this.resetPos.equals(this.targetPos)) {
            return "Reset is target";
         }

         if (this.resetProblem(this.resetPos) != null) {
            return "Reset invalid";
         }

         if (this.boneMealSlot() < 0) {
            return "No bone meal";
         }

         String access = autismclient.util.leafdupe.LeafDupeValidator.init(
            this.eyePosition(),
            MC.player.getEyePosition(),
            List.of(this.leafPos, this.targetPos, this.resetPos),
            MC.player.blockInteractionRange() - 0.05,
            this::shapeBounds,
            (pos, eye) -> autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, pos, eye) != null
         );
         return access != null ? access : null;
      } else {
         return "Out of reach";
      }
   }

   private String describeSelection(EnumSetting<LeafDupeModule.SelectionMode> selection, StringSetting setting, String label) {
      if (selection.get() == LeafDupeModule.SelectionMode.Automatic) {
         return "No " + label;
      } else {
         BlockPos custom = parsePosition(setting);
         if (custom == null) {
            return "Set " + label;
         } else if (MC.level != null && MC.level.isLoaded(custom)) {
            return label.equals("leaf") && this.leafProblem(custom) != null
                  || label.equals("target") && this.targetProblem(custom) != null
                  || label.equals("reset block") && this.resetProblem(custom) != null
               ? "Invalid " + label
               : "Out of reach";
         } else {
            return "Out of reach";
         }
      }
   }

   private void refreshPositions(boolean force) {
      if (this.leafMode.get() == LeafDupeModule.SelectionMode.Custom) {
         this.leafPos = parsePosition(this.leafPosition);
      }

      if (this.targetMode.get() == LeafDupeModule.SelectionMode.Custom) {
         this.targetPos = parsePosition(this.targetPosition);
      }

      if (this.resetMode.get() == LeafDupeModule.SelectionMode.Custom) {
         this.resetPos = parsePosition(this.resetPosition);
      }

      if (force || --this.flush <= 0) {
         this.flush = 5;
         Vec3 eye = this.eyePosition();
         boolean leaf = this.leafMode.get() == LeafDupeModule.SelectionMode.Automatic && !this.isUsableLeaf(this.leafPos, eye);
         boolean target = this.targetMode.get() == LeafDupeModule.SelectionMode.Automatic && !this.isUsableTarget(this.targetPos, eye);
         boolean reset = this.resetMode.get() == LeafDupeModule.SelectionMode.Automatic && !this.isUsableReset(this.resetPos, eye);
         if (leaf || target || reset) {
            this.autoSelect(eye, leaf, target, reset);
         }
      }
   }

   private void autoSelect(Vec3 eye, boolean seekLeaf, boolean seekTarget, boolean seekReset) {
      List<LeafDupeModule.RankedPos> leaves = new ArrayList<>();
      List<LeafDupeModule.RankedPos> targets = new ArrayList<>();
      List<LeafDupeModule.RankedPos> resets = new ArrayList<>();
      Set<String> allowed = new HashSet<>(this.targetBlocks.get());
      double reach = MC.player.blockInteractionRange();
      double maxDistance = (reach + 1.0) * (reach + 1.0);

      for (BlockPos pos : BlockPos.withinClippedManhattan(MC.player.blockPosition(), 6, 6, 6)) {
         if (MC.level.isLoaded(pos)) {
            double distance = eye.distanceToSqr(Vec3.atCenterOf(pos));
            if (!(distance > maxDistance)) {
               BlockState state = MC.level.getBlockState(pos);
               if (seekLeaf && state.is(Blocks.SMALL_DRIPLEAF) && this.leafProblem(pos) == null) {
                  leaves.add(new LeafDupeModule.RankedPos(pos.immutable(), distance));
               }

               if (seekTarget && allowed.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())) {
                  targets.add(new LeafDupeModule.RankedPos(pos.immutable(), distance));
               }

               if (seekReset
                  && !pos.equals(this.targetPos)
                  && !state.isAir()
                  && state.getFluidState().isEmpty()
                  && !state.hasBlockEntity()
                  && !(state.getBlock() instanceof BonemealableBlock)
                  && autismclient.util.leafdupe.LeafDupeValidator.targetProblem(state, true, false) == null) {
                  resets.add(new LeafDupeModule.RankedPos(pos.immutable(), distance));
               }
            }
         }
      }

      leaves.sort(Comparator.comparingDouble(LeafDupeModule.RankedPos::distance));
      targets.sort(Comparator.comparingDouble(LeafDupeModule.RankedPos::distance));
      resets.sort(Comparator.comparingDouble(LeafDupeModule.RankedPos::distance));
      this.debugLog("Scan: %d leaf, %d target, %d reset candidates; whitelist=%s", leaves.size(), targets.size(), resets.size(), allowed);
      if (seekLeaf) {
         BlockPos found = this.firstVisible(leaves, eye);
         if (found != null) {
            this.leafPos = found;
         }
      }

      if (seekTarget) {
         BlockPos found = this.firstVisible(targets, eye);
         if (found != null) {
            this.targetPos = found;
         }
      }

      if (seekReset) {
         List<LeafDupeModule.RankedPos> visible = new ArrayList<>();

         for (LeafDupeModule.RankedPos candidate : this.candidatePageOf(resets)) {
            if (this.isUsableReset(candidate.pos(), eye)) {
               visible.add(candidate);
            }
         }

         BlockPos below = MC.player.blockPosition().below();
         if (this.isUsableReset(below, eye)) {
            this.resetPos = below;
         } else if (!visible.isEmpty()) {
            BlockPos found = visible.getFirst().pos();

            for (LeafDupeModule.RankedPos candidate : visible) {
               if (autismclient.util.leafdupe.LeafDupeValidator.init(eye, 0.0F, 0.0F, candidate.pos(), this.leafPos, reach) != null) {
                  found = candidate.pos();
                  break;
               }
            }

            this.resetPos = found;
         }
      }

      this.candidatePage++;
   }

   private BlockPos firstVisible(List<LeafDupeModule.RankedPos> candidates, Vec3 eye) {
      for (LeafDupeModule.RankedPos candidate : this.candidatePageOf(candidates)) {
         if (autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, candidate.pos(), eye) != null) {
            return candidate.pos();
         }
      }

      return null;
   }

   private List<LeafDupeModule.RankedPos> candidatePageOf(List<LeafDupeModule.RankedPos> candidates) {
      return autismclient.util.leafdupe.LeafDupeValidator.page(candidates, this.candidatePage, 6);
   }

   private boolean isUsableLeaf(BlockPos pos, Vec3 eye) {
      return pos != null && this.leafProblem(pos) == null && autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, pos, eye) != null;
   }

   private boolean isUsableTarget(BlockPos pos, Vec3 eye) {
      return pos != null && this.targetProblem(pos) == null && autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, pos, eye) != null;
   }

   private boolean isUsableReset(BlockPos pos, Vec3 eye) {
      return pos != null && !pos.equals(this.targetPos) && this.resetProblem(pos) == null && this.hitFor(pos, eye) != null;
   }

   private float[][] candidateLooks() {
      float lastYaw = this.lastWireYaw();
      float lastPitch = this.lastWirePitch();
      if (!rotationOverrideActive) {
         return new float[][]{{lastYaw, lastPitch}};
      } else {
         return Float.compare(appliedYaw, lastYaw) == 0 && Float.compare(appliedPitch, lastPitch) == 0
            ? new float[][]{{lastYaw, lastPitch}}
            : new float[][]{{appliedYaw, appliedPitch}, {lastYaw, lastPitch}};
      }
   }

   private String blockReason() {
      if (this.targetPos != null && autismclient.util.leafdupe.LeafDupePacketTracker.load(this.targetPos)) {
         return "Restoring target";
      } else {
         long wait = autismclient.util.leafdupe.LeafDupePacketTracker.waitMs();
         if (wait == 9223372036854775807L) {
            return "Use suitable tool";
         } else if (wait > 0L) {
            return "FastBreak timing";
         } else {
            return this.packetWindowMs.get() > 0 && this.packetsAvailable() < 2 ? "Packet window" : null;
         }
      }
   }

   private boolean sendUseSequence(InteractionHand hand, Vec3 eye) {
      LeafDupeModule.BreakAttempt current = this.breakAttempt;
      if (current != null
         && autismclient.util.leafdupe.LeafDupePacketTracker.init(current.breakReceipt, this.targetPos)
         && isFasterTool(MC.player.getInventory().getItem(current.slot), current.state)) {
         BlockHitResult reset = this.hitFor(this.resetPos, eye);
         BlockHitResult leaf = autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, this.leafPos, eye);
         if (reset != null && leaf != null) {
            current.heldStack = MC.player.getInventory().getItem(current.slot).copy();
            int mealSlot = MC.player.getInventory().getSelectedSlot();
            if (!this.selectSlotTracked(mealSlot, current, null)) {
               return false;
            }

            autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil resetWrite = this.sendTrackedUse(hand, reset, mealSlot, current, null);
            autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil leafWrite = this.sendTrackedUse(hand, leaf, mealSlot, current, resetWrite);
            if (!this.selectSlotTracked(current.slot, current, leafWrite)) {
               return false;
            }

            boolean abort = autismclient.util.leafdupe.LeafDupePacketTracker.init(this.targetPos);
            if (abort) {
               MC.getConnection().send(new ServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.targetPos, Direction.DOWN));
            }

            Direction stopFace = this.visibleFace(eye, this.targetPos, Direction.DOWN, false);

            try (BlockStatePredictionHandler prediction = this.predictionHandler().startPredicting()) {
               ServerboundPlayerActionPacket stop = new ServerboundPlayerActionPacket(
                  Action.STOP_DESTROY_BLOCK, this.targetPos, stopFace, prediction.currentSequence()
               );
               this.cycleReceipt = autismclient.util.leafdupe.LeafDupePacketTracker.init(
                  stop, this.proofFor(current), current.slot, leafWrite
               );
               MC.getConnection().send(stop);
            }

            if (this.debug.get()) {
               this.chatInfo(
                  "Cycle queued: reset %s, leaf %s, stop %s%s.", reset.getDirection(), leaf.getDirection(), stopFace, abort ? ", abort first" : ""
               );
            }

            return true;
         } else {
            this.status = reset == null ? "Reset face blocked" : "Leaf obstructed";
            this.flush = 0;
            return false;
         }
      } else {
         return false;
      }
   }

   private BlockHitResult hitFor(BlockPos pos, Vec3 eye) {
      BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(MC, pos, eye);
      return hit != null && MC.level.getFluidState(pos.relative(hit.getDirection())).isEmpty() ? hit : null;
   }

   private void debugLog(String message, Object... args) {
      if (this.debug.get()) {
         long now = System.currentTimeMillis();
         if (now - this.lastDebugMillis >= 1000L) {
            this.lastDebugMillis = now;
            this.chatInfo(message, args);
         }
      }
   }

   private Vec3 eyePosition() {
      AutismLocalPlayerAccessor player = (AutismLocalPlayerAccessor)MC.player;
      double eyeHeight = MC.player.getEyeY() - MC.player.getY();
      Vec3 written = ServerRotationTracker.getSnapshot().position();
      return written != null
         ? written.add(0.0, eyeHeight, 0.0)
         : new Vec3(player.autism$getLastSentX(), player.autism$getLastSentY() + eyeHeight, player.autism$getLastSentZ());
   }

   private float lastWireYaw() {
      float written = ServerRotationTracker.getLastWireYaw();
      return Float.isFinite(written) ? written : ((AutismLocalPlayerAccessor)MC.player).autism$getLastSentYaw();
   }

   private float lastWirePitch() {
      ServerRotationTracker.RotationSnapshot written = ServerRotationTracker.getSnapshot();
      return written.initialized() ? written.currentPitch() : ((AutismLocalPlayerAccessor)MC.player).autism$getLastSentPitch();
   }

   private static boolean inWorld() {
      return MC != null && MC.player != null && MC.level != null && MC.getConnection() != null;
   }

   private String pauseReason() {
      if (aD.init_Z()) {
         return "Paused";
      } else if (MC.gui.screen() != null || OverlayManager.getInstance().hasFocusedTextInput()) {
         return "GUI open";
      } else if (!MC.mouseHandler.isMouseGrabbed()) {
         return "Cursor released";
      } else if (!MC.player.isAlive() || MC.player.isSpectator() || MC.player.isPassenger() || MC.getCameraEntity() != MC.player) {
         return "Player unavailable";
      } else if (!MC.player.isUsingItem() && !MC.options.keyAttack.isDown() && !MC.options.keyUse.isDown()) {
         return !autismclient.util.InventorySlotArbiter.canBeginActionGroup("leaf-dupe") ? "Hand busy" : null;
      } else {
         return "Manual input";
      }
   }

   private static String visibleStatus(String reason) {
      return reason != null && !reason.equals("Packet window") && !reason.equals("FastBreak timing") && !reason.equals("Restoring target") ? reason : "";
   }

   private float[] aimForResetAndLeaf(Vec3 eye, float lastYaw, float lastPitch) {
      return autismclient.util.leafdupe.LeafDupeValidator.init(eye, lastYaw, lastPitch, this.resetPos, this.leafPos, MC.player.blockInteractionRange());
   }

   private Direction visibleFace(Vec3 eye, BlockPos pos, Direction preferred, boolean avoidFluids) {
      return autismclient.util.leafdupe.LeafDupeValidator.init(eye, pos, preferred, avoidFluids, neighbor -> !MC.level.getFluidState(neighbor).isEmpty());
   }

   private int packetsAvailable() {
      return this.rateLimiter.remaining(this.packetWindowMs.get(), System.currentTimeMillis());
   }

   private void sendUse(InteractionHand hand, BlockPos pos, Vec3 point, Direction face) {
      this.rateLimiter.recordUse(this.packetWindowMs.get(), System.currentTimeMillis());
      BlockHitResult hit = new BlockHitResult(point, face, pos, false);

      try (BlockStatePredictionHandler prediction = this.predictionHandler().startPredicting()) {
         MC.getConnection().send(new ServerboundUseItemOnPacket(hand, hit, prediction.currentSequence()));
      }
   }

   private autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil sendTrackedUse(
      InteractionHand hand, BlockHitResult hit, int slot, LeafDupeModule.BreakAttempt owner, autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil previous
   ) {
      this.rateLimiter.recordUse(this.packetWindowMs.get(), System.currentTimeMillis());

      try (BlockStatePredictionHandler prediction = this.predictionHandler().startPredicting()) {
         ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(hand, hit, prediction.currentSequence());
         autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil receipt = autismclient.util.leafdupe.LeafDupePacketTracker.init(
            packet, this.proofFor(owner), slot, previous
         );
         MC.getConnection().send(packet);
         return receipt;
      }
   }

   private BlockStatePredictionHandler predictionHandler() {
      return ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler();
   }

   private int boneMealSlot() {
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

   private boolean isSlotSelected(int slot) {
      return slot >= 0 && MC.player != null ? slot == 40 || slot == MC.player.getInventory().getSelectedSlot() : false;
   }

   private InteractionHand handForSlot(int slot) {
      return slot == 40 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
   }

   private boolean selectSlot(int slot) {
      if (slot >= 0 && slot <= 8 && MC.player != null) {
         autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
         return MC.player.getInventory().getSelectedSlot() == slot;
      } else {
         return false;
      }
   }

   private String leafProblem(BlockPos pos) {
      if (MC.level == null) {
         return "no world.";
      } else {
         return !MC.level.isLoaded(pos)
            ? "chunk is not loaded."
            : autismclient.util.leafdupe.LeafDupeValidator.dripleafProblem(MC.level.getBlockState(pos), MC.level.getBlockState(pos.below()), true);
      }
   }

   private String resetProblem(BlockPos pos) {
      if (MC.level == null) {
         return "no world.";
      }

      if (!MC.level.isLoaded(pos)) {
         return "chunk is not loaded.";
      }

      BlockState state = MC.level.getBlockState(pos);
      return autismclient.util.leafdupe.LeafDupeValidator.targetProblem(state, true, state.getMenuProvider(MC.level, pos) != null);
   }

   private String targetProblem(BlockPos pos) {
      if (MC.level == null) {
         return "no world.";
      } else if (!MC.level.isLoaded(pos)) {
         return "chunk is not loaded.";
      } else {
         BlockState state = MC.level.getBlockState(pos);
         if (state.isAir()) {
            return "that is air, there is nothing to break.";
         } else {
            return !this.targetBlocks.get().contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString())
               ? state.getBlock().getName().getString() + " is not in the target whitelist."
               : null;
         }
      }
   }

   private BlockPos lookedAtBlock() {
      if (MC.player != null && MC.level != null) {
         if (MC.hitResult instanceof BlockHitResult block && block.getType() == Type.BLOCK) {
            return block.getBlockPos().immutable();
         } else {
            this.chatError("You are not looking at a block.");
            return null;
         }
      } else {
         this.chatError("You are not in a world.");
         return null;
      }
   }

   private AABB shapeBounds(BlockPos pos) {
      VoxelShape shape = MC.level.getBlockState(pos).getShape(MC.level, pos);
      return shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
   }

   private static BlockPos parsePosition(StringSetting setting) {
      return autismclient.util.leafdupe.LeafDupeValidator.init(setting.get());
   }

   private void chatInfo(String message, Object... args) {
      this.chatColored(ChatFormatting.GRAY, message, args);
   }

   private void chatWarn(String message, Object... args) {
      this.chatColored(ChatFormatting.YELLOW, message, args);
   }

   private void chatError(String message, Object... args) {
      this.chatColored(ChatFormatting.RED, message, args);
   }

   private void chatColored(ChatFormatting color, String message, Object... args) {
      String text = args.length == 0 ? message : String.format(Locale.ROOT, message, args);
      AutismClientMessaging.send(Component.empty().append(AutismClientMessaging.themedTag(this.name())).append(Component.literal(text).withStyle(color)));
   }

   private static void requestRotation(float yaw, float pitch, int priority) {
      if (!rotationRequested || priority >= rotationPriority) {
         requestedYaw = yaw;
         requestedPitch = pitch;
         rotationPriority = priority;
         rotationRequested = true;
      }
   }

   private static void releaseRotation() {
      rotationRequested = false;
      rotationOverrideActive = false;
      rotationPriority = -2147483648;
   }

   public static void beforeSendPosition(LocalPlayer player) {
      LeafDupeModule module = instance;
      if (module != null && player == MC.player) {
         if (!rotationRequested && !module.boneMealPending && !module.breakStartPending && module.pendingResetClick == null) {
            rotationOverrideActive = false;
         } else if (inWorld() && MC.level == module.sessionLevel && MC.player == module.sessionPlayer && module.pauseReason() == null) {
            if (module.pendingResetClick != null) {
               module.pendingResetClick.tickRetry();
            }

            rotationOverrideActive = rotationRequested;
            if (rotationRequested) {
               appliedYaw = requestedYaw;
               appliedPitch = requestedPitch;
            }

            rotationRequested = false;
            rotationPriority = -2147483648;
            if (module.pendingResetClick != null) {
               module.pendingResetClick.trySend();
            }

            if (module.breakStartPending) {
               module.sendBreakStart();
            } else {
               module.sendBoneMealCycle();
            }
         } else {
            releaseRotation();
            module.boneMealPending = false;
            module.cancelBreakAttempt();
            if (module.pendingResetClick != null) {
               module.pendingResetClick.detach();
            }
         }
      }
   }

   public static float overrideYaw(LocalPlayer player, float original) {
      return player == MC.player && rotationOverrideActive ? appliedYaw : original;
   }

   public static float overridePitch(LocalPlayer player, float original) {
      return player == MC.player && rotationOverrideActive ? appliedPitch : original;
   }

   public static void afterSendPosition(LocalPlayer player) {
      rotationOverrideActive = false;
      LeafDupeModule module = instance;
      if (module != null && player == MC.player && module.cycleLimitReached) {
         module.cycleLimitReached = false;
         module.chatInfo("Finished %d cycle(s).", module.cycleCount);
         module.setEnabled(false);
      }
   }

   public static void onLogin() {
      releaseRotation();
      if (instance != null) {
         instance.endSession();
      }

      autismclient.util.leafdupe.LeafDupePacketTracker.reset();
      autismclient.util.leafdupe.LeafDupePacketTracker.init(0);
   }

   public static void onDisconnect() {
      releaseRotation();
      if (instance != null) {
         instance.endSession();
      }

      autismclient.util.leafdupe.LeafDupePacketTracker.reset();
   }

   private void endSession() {
      this.cancelBreakAttempt();
      if (this.pendingResetClick != null) {
         this.pendingResetClick.detach();
      }

      this.boneMealPending = false;
      this.cycleLimitReached = false;
      this.sessionLevel = null;
      this.sessionPlayer = null;
      this.leafPos = this.targetPos = this.resetPos = null;
      this.flush = 0;
      this.candidatePage = 0L;
      this.cycleCount = 0;
      this.rateLimiter.init();
   }

   private enum SelectionMode {
      Automatic,
      Custom;
   }

   private final class PendingResetClick {
      private final InteractionHand hand;
      private final int mealSlot;
      private final ClientLevel level = Module.MC.level;
      private final LocalPlayer player = Module.MC.player;
      private final ClientPacketListener connection = Module.MC.getConnection();
      private final BlockPos resetPos = LeafDupeModule.this.resetPos;
      private final int originalSlot = Module.MC.player.getInventory().getSelectedSlot();
      private int retriesLeft = 20;

      private PendingResetClick(int mealSlot) {
         this.mealSlot = mealSlot;
         this.hand = LeafDupeModule.this.handForSlot(mealSlot);
      }

      private void detach() {
         if (LeafDupeModule.this.pendingResetClick == this) {
            LeafDupeModule.this.pendingResetClick = null;
         }
      }

      private void tickRetry() {
         if (!this.isSameSession()) {
            this.detach();
         } else if (this.retriesLeft-- <= 0) {
            LeafDupeModule.this.chatWarn("Grim: could not send the last reset click (rotation or packet window), skipped it.");
            this.detach();
         } else if (this.hand != InteractionHand.MAIN_HAND || LeafDupeModule.this.isSlotSelected(this.mealSlot)) {
            Vec3 eye = LeafDupeModule.this.eyePosition();
            float lastYaw = LeafDupeModule.this.lastWireYaw();
            float[] aim = autismclient.util.leafdupe.LeafDupeValidator.aimAt(eye, autismclient.util.leafdupe.LeafDupeValidator.init(eye, this.resetPos), lastYaw);
            LeafDupeModule.requestRotation(aim[0], aim[1], 100);
         } else if (autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
            try {
               if (!LeafDupeModule.this.selectSlot(this.mealSlot)) {
                  this.detach();
               }
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
            }
         }
      }

      private void trySend() {
         if (LeafDupeModule.this.pendingResetClick == this) {
            if (!this.isSameSession()) {
               this.detach();
            } else {
               Vec3 eye = LeafDupeModule.this.eyePosition();
               if (autismclient.util.leafdupe.LeafDupeValidator.init(eye, LeafDupeModule.this.candidateLooks(), this.resetPos, Module.MC.player.blockInteractionRange())
                  && LeafDupeModule.this.packetsAvailable() >= 1) {
                  if (!Module.MC.player.getItemInHand(this.hand).is(Items.BONE_MEAL)) {
                     this.detach();
                  } else if (autismclient.util.InventorySlotArbiter.beginActionGroup("leaf-dupe")) {
                     try {
                        BlockHitResult hit = LeafDupeModule.this.hitFor(this.resetPos, eye);
                        if (hit == null) {
                           this.detach();
                           return;
                        }

                        LeafDupeModule.this.sendUse(this.hand, this.resetPos, hit.getLocation(), hit.getDirection());
                     } finally {
                        autismclient.util.InventorySlotArbiter.endActionGroup("leaf-dupe");
                     }

                     if (LeafDupeModule.this.debug.get()) {
                        LeafDupeModule.this.chatInfo("Last reset click sent.");
                     }

                     this.detach();
                     return;
                  }
               }
            }
         }
      }

      private boolean isSameSession() {
         return LeafDupeModule.inWorld()
               && Module.MC.level == this.level
               && Module.MC.player == this.player
               && Module.MC.getConnection() == this.connection
               && LeafDupeModule.this.pauseReason() == null
               && this.resetPos != null
               && LeafDupeModule.this.resetProblem(this.resetPos) == null
               && Module.MC.player.getInventory().getItem(this.mealSlot).is(Items.BONE_MEAL)
               && (
                  this.hand != InteractionHand.MAIN_HAND
                     || Module.MC.player.getInventory().getSelectedSlot() == this.originalSlot
                     || Module.MC.player.getInventory().getSelectedSlot() == this.mealSlot
               )
               && (this.hand != InteractionHand.OFF_HAND || !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("leaf-dupe"))
               && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(
                  this.hand == InteractionHand.OFF_HAND ? 40 : Module.MC.player.getInventory().getSelectedSlot(), "leaf-dupe"
               )
            ? autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(Module.MC, this.resetPos, LeafDupeModule.this.eyePosition()) != null
               && autismclient.ai.engine.AiBlockInteraction.findReachableHitFrom(Module.MC, this.resetPos, Module.MC.player.getEyePosition()) != null
            : false;
      }
   }

   private final class BreakAttempt {
      final ClientLevel level = Module.MC.level;
      final LocalPlayer player = Module.MC.player;
      final Connection connection = Module.MC.getConnection().getConnection();
      final BlockPos pos;
      final BlockState state;
      final int slot;
      ItemStack heldStack;
      final long createdNanos = System.nanoTime();
      volatile autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil breakReceipt;
      volatile autismclient.util.leafdupe.LeafDupePacketTracker.Inner_StreamUtil slotReceipt;
      int sentSlot = -1;
      volatile boolean cancelled;

      BreakAttempt(BlockPos target, BlockState state, int slot) {
         this.pos = target.immutable();
         this.state = state;
         this.slot = slot;
         this.heldStack = Module.MC.player.getInventory().getItem(slot).copy();
      }
   }

   private record RankedPos(BlockPos pos, double distance) {
   }
}
