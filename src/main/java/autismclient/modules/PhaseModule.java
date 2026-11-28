package autismclient.modules;

import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.util.AuditClientFixes;
import autismclient.util.AutismLiteVariant;
import java.util.Locale;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PhaseModule extends Module {
   private static final int init_I = 0;
   private static final int load = 1;
   private static final int read = 2;
   private static final PhaseModule.Inner_StreamUtil CLIP_MODE = new PhaseModule.Inner_StreamUtil();
   private static final PhaseModule.Inner_CachePool INTAVE_MODE = new PhaseModule.Inner_CachePool();
   private static final PhaseModule.Inner_DataUtil BLINK_MODE = new PhaseModule.Inner_DataUtil();
   private static final PhaseModule.Inner_ConfigStore SPIDER_MODE = new PhaseModule.Inner_ConfigStore();
   private static volatile PhaseModule INSTANCE;
   private static volatile int activeModeKind;
   private volatile PhaseModule.Inner_BufferHelper mode;

   public PhaseModule() {
      super("phase", "Phase", ae.load_ae, "Phase through blocks.");
      this.add(new ChoiceSetting("mode", "Mode", "Clip", "Clip", "Intave", "Blink", "Spider").description("Phase method.").build());
      this.add(
         new IntSetting("maximum", "Maximum", 120, 1, 300, 1)
            .unit("ticks")
            .visibleWhen(() -> "Blink".equals(this.choice("mode")))
            .description("Blink tick budget.")
            .build()
      );
      INSTANCE = this;
   }

   @Override
   public String info() {
      String mode = this.choice("mode");
      PhaseModule.Inner_BufferHelper current = this.mode;
      if (current == null) {
         return mode;
      }

      String state = current.modeStatus();
      return state.isEmpty() ? mode : mode + " " + state;
   }

   @Override
   public void onEnable() {
      this.activateMode(modeByName(this.choice("mode")));
   }

   @Override
   public void onDisable() {
      this.deactivateMode();
   }

   @Override
   public void onGameJoin() {
      if (this.isEnabled() && this.mode == null) {
         this.activateMode(modeByName(this.choice("mode")));
      }
   }

   @Override
   public void onGameLeft() {
      this.deactivateMode();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if ("mode".equals(settingId) && this.isEnabled()) {
         this.refreshMode();
      }
   }

   @Override
   protected void onSettingsReset() {
      if (this.isEnabled()) {
         this.refreshMode();
      }
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         PhaseModule.Inner_BufferHelper mode = this.mode;
         if (mode != null) {
            mode.modePreMovementTick();
         }
      }
   }

   @Override
   public void onNetworkMovementTickPre() {
      PhaseModule.Inner_BufferHelper mode = this.mode;
      if (mode != null) {
         mode.modeNetworkTickPre();
      }
   }

   @Override
   public void onPacketProcessFrame() {
      PhaseModule.Inner_BufferHelper mode = this.mode;
      if (mode != null) {
         mode.modePacketFrame();
      }
   }

   @Override
   public boolean shouldCancelPlayerTick() {
      PhaseModule.Inner_BufferHelper mode = this.mode;
      return mode != null && mode.modeCancelsPlayerTick();
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      PhaseModule.Inner_BufferHelper mode = this.mode;
      if (mode != null && packet instanceof ClientboundPlayerPositionPacket) {
         mode.modeOnSetback();
      }

      return false;
   }

   public static VoxelShape init(VoxelShape original, BlockState state, BlockPos pos) {
      int mode = activeModeKind;
      if (mode == 0 || MC == null || !MC.isSameThread()) {
         return original;
      } else {
         LocalPlayer player = MC.player;
         if (player == null) {
            return original;
         } else {
            return mode == 2 ? BLINK_MODE.collisionShape(original, player, pos) : SPIDER_MODE.collisionShape(original, state, player, pos);
         }
      }
   }

   private void refreshMode() {
      PhaseModule.Inner_BufferHelper next = modeByName(this.choice("mode"));
      if (next != this.mode) {
         this.deactivateMode();
         this.activateMode(next);
      }
   }

   private void activateMode(PhaseModule.Inner_BufferHelper mode) {
      this.mode = mode;
      this.updateActiveModeKind();
      mode.onModeStart();
   }

   private void deactivateMode() {
      PhaseModule.Inner_BufferHelper mode = this.mode;
      this.mode = null;
      this.updateActiveModeKind();
      if (mode != null) {
         mode.onModeStop();
      }
   }

   private void updateActiveModeKind() {
      PhaseModule.Inner_BufferHelper mode = this.mode;
      if (mode == SPIDER_MODE) {
         activeModeKind = 1;
      } else if (mode == BLINK_MODE) {
         activeModeKind = 2;
      } else {
         activeModeKind = 0;
      }
   }

   private static PhaseModule.Inner_BufferHelper modeByName(String name) {
      PhaseModule.Inner_BufferHelper var10000;
      switch (name) {
         case "Intave":
            var10000 = INTAVE_MODE;
            break;
         case "Blink":
            var10000 = BLINK_MODE;
            break;
         case "Spider":
            var10000 = SPIDER_MODE;
            break;
         default:
            var10000 = CLIP_MODE;
      }

      return var10000;
   }

   private static boolean collidesAt(LocalPlayer player, Vec3 pos) {
      AABB box = player.getBoundingBox().move(pos.subtract(player.position()));

      for (VoxelShape shape : player.level().getBlockCollisions(player, box)) {
         if (!shape.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private static void setVerticalVelocity(LocalPlayer player, double y) {
      Vec3 velocity = player.getDeltaMovement();
      player.setDeltaMovement(velocity.x, y, velocity.z);
   }

   public static boolean audit$isSpiderActive() {
      if (activeModeKind == 1 && !aD.read_Z()) {
         PhaseModule var0 = INSTANCE;
         if (var0 != null) {
            return var0.isEnabled();
         }
      }

      return false;
   }

   private interface Inner_BufferHelper {
      default void onModeStart() {
      }

      default void onModeStop() {
      }

      default void modePreMovementTick() {
      }

      default void modeNetworkTickPre() {
      }

      default void modePacketFrame() {
      }

      default boolean modeCancelsPlayerTick() {
         return false;
      }

      default void modeOnSetback() {
      }

      default String modeStatus() {
         return "";
      }
   }

   private static final class Inner_CachePool implements PhaseModule.Inner_BufferHelper {
      private boolean sinking;

      @Override
      public void modePreMovementTick() {
         if (!AutismLiteVariant.enabled()) {
            LocalPlayer player = Module.MC.player;
            ClientPacketListener connection = Module.MC.getConnection();
            if (player != null && connection != null) {
               boolean check = Module.MC.options.keyAttack.isDown() && player.getXRot() > 80.0F;
               BlockPos below = player.blockPosition().offset(0, -1, 0);
               if (check) {
                  connection.send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, below, Direction.UP));
                  this.sinking = true;
               } else if (this.sinking) {
                  this.sinking = false;
               }

               if (this.sinking) {
                  ((AutismEntityAccessor)player).autism$setPosition(new Vec3(player.getX(), player.getY() - 0.0052, player.getZ()));
               }

               if (player.isShiftKeyDown()) {
                  float distance = 0.005F;
                  double rotation = Math.toRadians(player.getYRot());
                  if (Module.MC.options.keyUp.isDown()) {
                     nudgeHorizontal(player, rotation, distance, 1, 1);
                  } else if (Module.MC.options.keyDown.isDown()) {
                     nudgeHorizontal(player, rotation, -distance, 1, -1);
                  } else if (Module.MC.options.keyLeft.isDown()) {
                     nudgeHorizontal(player, rotation, distance, -1, 1);
                  } else if (Module.MC.options.keyRight.isDown()) {
                     nudgeHorizontal(player, rotation, -distance, -1, -1);
                  }
               }
            }
         }
      }

      @Override
      public String modeStatus() {
         if (Module.MC.player == null) {
            return "";
         } else if (this.sinking) {
            return "sinking";
         } else {
            return Module.MC.options.keyAttack.isDown() ? "look down" : "hold attack";
         }
      }

      private static void nudgeHorizontal(LocalPlayer player, double rotation, float distance, int xMultiplier, int zMultiplier) {
         double xx = Math.cos(rotation) * distance * xMultiplier;
         double zz = Math.sin(rotation) * distance * zMultiplier;
         player.setPos(player.getX() + xx, player.getY(), player.getZ() + zz);
      }
   }

   private static final class Inner_ConfigStore implements PhaseModule.Inner_BufferHelper {
      private int init = 1;

      @Override
      public void onModeStart() {
         this.init = 1;
         LocalPlayer player = Module.MC.player;
         if (player != null) {
            PhaseModule.setVerticalVelocity(player, 0.0);
         }
      }

      @Override
      public void onModeStop() {
         AuditClientFixes.clearNoPhysicsIfUnused();
      }

      @Override
      public void modePreMovementTick() {
         if (!AutismLiteVariant.enabled()) {
            LocalPlayer player = Module.MC.player;
            if (player != null && Module.MC.level != null) {
               switch (this.init) {
                  case 1:
                     if (Module.MC.options.keyJump.isDown() && !Module.MC.level.getBlockState(player.blockPosition()).isAir()) {
                        PhaseModule.setVerticalVelocity(player, 0.42);
                        this.init++;
                     }

                     player.setOnGround(true);
                     break;
                  case 2:
                     PhaseModule.setVerticalVelocity(player, 0.33);
                     this.init++;
                     break;
                  case 3:
                     PhaseModule.setVerticalVelocity(player, 0.25);
                     this.init++;
               }

               if (this.init > 3) {
                  this.init = 1;
               }

               player.noPhysics = true;
               if (player.isShiftKeyDown()) {
                  player.setDeltaMovement(MovementModuleHooks.init(player, player.getDeltaMovement(), 0.179));
               }
            }
         }
      }

      VoxelShape collisionShape(VoxelShape original, BlockState state, LocalPlayer player, BlockPos pos) {
         if (state.getBlock() instanceof LiquidBlock) {
            return original;
         } else {
            return pos.getY() >= player.getY() ? Shapes.empty() : original;
         }
      }
   }

   private static final class Inner_DataUtil implements PhaseModule.Inner_BufferHelper {
      private static final int init_I = 3;
      private final BlinkPacketManager.Inner_StreamUtil blinkPolicy = this::blinkDecision;
      private volatile PhaseModule.Inner_DataUtil.Inner_DataUtil_2 state;
      private volatile boolean probingCollision;
      private volatile String pendingDisableReason;
      private volatile boolean exceededMaximum;
      private int heldTicks;
      private boolean walking;
      private int failedClearCount;

      private Inner_DataUtil() {
         this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
      }

      @Override
      public void onModeStart() {
         this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         this.heldTicks = 0;
         this.pendingDisableReason = null;
         this.exceededMaximum = false;
         this.walking = false;
         this.failedClearCount = 0;
         BlinkPacketManager.registerPolicy(this.blinkPolicy);
      }

      @Override
      public void onModeStop() {
         this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.FLUSHING;
         BlinkPacketManager.flushIncoming();
         this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         BlinkPacketManager.unregisterPolicy(this.blinkPolicy);
         BlinkPacketManager.requestPolicyFlush(true, true);
         this.heldTicks = 0;
         this.pendingDisableReason = null;
         this.exceededMaximum = false;
         this.walking = false;
         this.failedClearCount = 0;
      }

      @Override
      public void modeNetworkTickPre() {
         LocalPlayer player = Module.MC.player;
         if (player != null) {
            this.probingCollision = true;

            try {
               if (this.state == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING) {
                  if (PhaseModule.collidesAt(player, player.position())) {
                     this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING;
                  }
               } else if (this.state == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING && !PhaseModule.collidesAt(player, player.position())) {
                  this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING;
                  this.walking = true;
               }
            } finally {
               this.probingCollision = false;
            }
         }
      }

      @Override
      public void modePacketFrame() {
         String reason = this.pendingDisableReason;
         if (reason != null) {
            this.pendingDisableReason = null;
            PhaseModule.INSTANCE.disableWithToggleMessage(reason);
         } else if (this.exceededMaximum) {
            this.exceededMaximum = false;
            this.flushHeldPackets();
         } else if (this.state == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING) {
            LocalPlayer player = Module.MC.player;
            if (player != null) {
               boolean collides = false;
               this.probingCollision = true;

               try {
                  for (Vec3 pos : BlinkPacketManager.getQueuedMovementPositions()) {
                     if (PhaseModule.collidesAt(player, pos)) {
                        collides = true;
                        break;
                     }
                  }
               } finally {
                  this.probingCollision = false;
               }

               if (!collides) {
                  PhaseModule.INSTANCE.setEnabled(false);
               }
            }
         }
      }

      @Override
      public boolean modeCancelsPlayerTick() {
         if (this.heldTicks > PhaseModule.INSTANCE.integer("maximum")) {
            this.exceededMaximum = true;
            return true;
         }

         if (this.state == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING || this.state == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING
            )
          {
            this.heldTicks++;
         }

         return false;
      }

      private void flushHeldPackets() {
         this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.FLUSHING;
         BlinkPacketManager.flushIncoming();
         this.state = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         BlinkPacketManager.requestPolicyFlush(false, true);
         this.heldTicks = 0;
         if (this.walking) {
            this.failedClearCount = 0;
         } else if (++this.failedClearCount >= 3) {
            this.pendingDisableReason = "Phase disabled: block did not clear.";
         }

         this.walking = false;
      }

      @Override
      public void modeOnSetback() {
         this.pendingDisableReason = "Phase disabled: server set you back.";
      }

      private BlinkPacketManager.Inner_DataUtil blinkDecision(Packet<?> packet, boolean incoming) {
         if (aD.read_Z()) {
            return BlinkPacketManager.Inner_DataUtil.FLUSH;
         } else {
            PhaseModule.Inner_DataUtil.Inner_DataUtil_2 current = this.state;
            if (current == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING) {
               return BlinkPacketManager.Inner_DataUtil.FLUSH;
            } else if (!(packet instanceof ClientboundBlockUpdatePacket)
               && !(packet instanceof ClientboundBlockEventPacket)
               && !(packet instanceof ClientboundSectionBlocksUpdatePacket)
               && !(packet instanceof ClientboundLevelChunkWithLightPacket)
               && !(packet instanceof ClientboundChunkBatchFinishedPacket)
               && !(packet instanceof ClientboundSetTitleTextPacket)) {
               return current != PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING && current != PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING
                  ? BlinkPacketManager.Inner_DataUtil.PASS
                  : BlinkPacketManager.Inner_DataUtil.QUEUE;
            } else {
               return BlinkPacketManager.Inner_DataUtil.PASS;
            }
         }
      }

      VoxelShape collisionShape(VoxelShape original, LocalPlayer player, BlockPos pos) {
         if (!this.probingCollision && !this.state.init_Z) {
            return pos.getY() >= player.position().y || player.isShiftKeyDown() && player.onGround() ? Shapes.empty() : original;
         } else {
            return original;
         }
      }

      @Override
      public String modeStatus() {
         PhaseModule.Inner_DataUtil.Inner_DataUtil_2 current = this.state;
         return current == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING ? "" : current.name().toLowerCase(Locale.ROOT);
      }

      private enum Inner_DataUtil_2 {
         WAITING(false),
         PHASING(false),
         WALKING(true),
         FLUSHING(true);

         final boolean init_Z;

         Inner_DataUtil_2(boolean boxCollisions) {
            this.init_Z = boxCollisions;
         }
      }
   }

   private static final class Inner_StreamUtil implements PhaseModule.Inner_BufferHelper {
      private static final double init = 0.07840000152;

      @Override
      public void modePreMovementTick() {
         if (!AutismLiteVariant.enabled()) {
            LocalPlayer player = Module.MC.player;
            ClientPacketListener connection = Module.MC.getConnection();
            if (player != null && connection != null) {
               Vec3 center = Vec3.atCenterOf(player.blockPosition());
               connection.send(new PosRot(center.x, player.getY() - 0.07840000152, center.z, player.getYRot(), player.getXRot(), false, false));
               PhaseModule.INSTANCE.disableWithToggleMessage("Phase: clip packet sent.");
            }
         }
      }
   }
}
