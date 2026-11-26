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
   private static final PhaseModule.Inner_StreamUtil init_aF_StreamUtil = new PhaseModule.Inner_StreamUtil();
   private static final PhaseModule.Inner_CachePool init_aF_CachePool = new PhaseModule.Inner_CachePool();
   private static final PhaseModule.Inner_DataUtil init_aF_DataUtil = new PhaseModule.Inner_DataUtil();
   private static final PhaseModule.Inner_ConfigStore init_aF_ConfigStore = new PhaseModule.Inner_ConfigStore();
   private static volatile PhaseModule init_aF;
   private static volatile int write;
   private volatile PhaseModule.Inner_BufferHelper init_aF_BufferHelper;

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
      init_aF = this;
   }

   @Override
   public String info() {
      String mode = this.choice("mode");
      PhaseModule.Inner_BufferHelper current = this.init_aF_BufferHelper;
      if (current == null) {
         return mode;
      }

      String state = current.init_Str();
      return state.isEmpty() ? mode : mode + " " + state;
   }

   @Override
   public void onEnable() {
      this.init_V_aF_BufferHelper(init_aF_BufferHelper_Str(this.choice("mode")));
   }

   @Override
   public void onDisable() {
      this.load();
   }

   @Override
   public void onGameJoin() {
      if (this.isEnabled() && this.init_aF_BufferHelper == null) {
         this.init_V_aF_BufferHelper(init_aF_BufferHelper_Str(this.choice("mode")));
      }
   }

   @Override
   public void onGameLeft() {
      this.load();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if ("mode".equals(settingId) && this.isEnabled()) {
         this.init();
      }
   }

   @Override
   protected void onSettingsReset() {
      if (this.isEnabled()) {
         this.init();
      }
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
         if (mode != null) {
            mode.sync();
         }
      }
   }

   @Override
   public void onNetworkMovementTickPre() {
      PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
      if (mode != null) {
         mode.read();
      }
   }

   @Override
   public void onPacketProcessFrame() {
      PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
      if (mode != null) {
         mode.write();
      }
   }

   @Override
   public boolean shouldCancelPlayerTick() {
      PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
      return mode != null && mode.init_Z();
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
      if (mode != null && packet instanceof ClientboundPlayerPositionPacket) {
         mode.update();
      }

      return false;
   }

   public static VoxelShape init(VoxelShape original, BlockState state, BlockPos pos) {
      int mode = write;
      if (mode == 0 || MC == null || !MC.isSameThread()) {
         return original;
      } else {
         LocalPlayer player = MC.player;
         if (player == null) {
            return original;
         } else {
            return mode == 2 ? init_aF_DataUtil.init(original, player, pos) : init_aF_ConfigStore.init(original, state, player, pos);
         }
      }
   }

   private void init() {
      PhaseModule.Inner_BufferHelper next = init_aF_BufferHelper_Str(this.choice("mode"));
      if (next != this.init_aF_BufferHelper) {
         this.load();
         this.init_V_aF_BufferHelper(next);
      }
   }

   private void init_V_aF_BufferHelper(PhaseModule.Inner_BufferHelper mode) {
      this.init_aF_BufferHelper = mode;
      this.read();
      mode.init_V();
   }

   private void load() {
      PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
      this.init_aF_BufferHelper = null;
      this.read();
      if (mode != null) {
         mode.load();
      }
   }

   private void read() {
      PhaseModule.Inner_BufferHelper mode = this.init_aF_BufferHelper;
      if (mode == init_aF_ConfigStore) {
         write = 1;
      } else if (mode == init_aF_DataUtil) {
         write = 2;
      } else {
         write = 0;
      }
   }

   private static PhaseModule.Inner_BufferHelper init_aF_BufferHelper_Str(String name) {
      PhaseModule.Inner_BufferHelper var10000;
      switch (name) {
         case "Intave":
            var10000 = init_aF_CachePool;
            break;
         case "Blink":
            var10000 = init_aF_DataUtil;
            break;
         case "Spider":
            var10000 = init_aF_ConfigStore;
            break;
         default:
            var10000 = init_aF_StreamUtil;
      }

      return var10000;
   }

   private static boolean init(LocalPlayer player, Vec3 pos) {
      AABB box = player.getBoundingBox().move(pos.subtract(player.position()));

      for (VoxelShape shape : player.level().getBlockCollisions(player, box)) {
         if (!shape.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private static void init(LocalPlayer player, double y) {
      Vec3 velocity = player.getDeltaMovement();
      player.setDeltaMovement(velocity.x, y, velocity.z);
   }

   public static boolean audit$isSpiderActive() {
      if (write == 1 && !aD.read_Z()) {
         PhaseModule var0 = init_aF;
         if (var0 != null) {
            return var0.isEnabled();
         }
      }

      return false;
   }

   private interface Inner_BufferHelper {
      default void init_V() {
      }

      default void load() {
      }

      default void sync() {
      }

      default void read() {
      }

      default void write() {
      }

      default boolean init_Z() {
         return false;
      }

      default void update() {
      }

      default String init_Str() {
         return "";
      }
   }

   private static final class Inner_CachePool implements PhaseModule.Inner_BufferHelper {
      private boolean init;

      @Override
      public void sync() {
         if (!AutismLiteVariant.enabled()) {
            LocalPlayer player = Module.MC.player;
            ClientPacketListener connection = Module.MC.getConnection();
            if (player != null && connection != null) {
               boolean check = Module.MC.options.keyAttack.isDown() && player.getXRot() > 80.0F;
               BlockPos below = player.blockPosition().offset(0, -1, 0);
               if (check) {
                  connection.send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, below, Direction.UP));
                  this.init = true;
               } else if (this.init) {
                  this.init = false;
               }

               if (this.init) {
                  ((AutismEntityAccessor)player).autism$setPosition(new Vec3(player.getX(), player.getY() - 0.0052, player.getZ()));
               }

               if (player.isShiftKeyDown()) {
                  float distance = 0.005F;
                  double rotation = Math.toRadians(player.getYRot());
                  if (Module.MC.options.keyUp.isDown()) {
                     init(player, rotation, distance, 1, 1);
                  } else if (Module.MC.options.keyDown.isDown()) {
                     init(player, rotation, -distance, 1, -1);
                  } else if (Module.MC.options.keyLeft.isDown()) {
                     init(player, rotation, distance, -1, 1);
                  } else if (Module.MC.options.keyRight.isDown()) {
                     init(player, rotation, -distance, -1, -1);
                  }
               }
            }
         }
      }

      @Override
      public String init_Str() {
         if (Module.MC.player == null) {
            return "";
         } else if (this.init) {
            return "sinking";
         } else {
            return Module.MC.options.keyAttack.isDown() ? "look down" : "hold attack";
         }
      }

      private static void init(LocalPlayer player, double rotation, float distance, int xMultiplier, int zMultiplier) {
         double xx = Math.cos(rotation) * distance * xMultiplier;
         double zz = Math.sin(rotation) * distance * zMultiplier;
         player.setPos(player.getX() + xx, player.getY(), player.getZ() + zz);
      }
   }

   private static final class Inner_ConfigStore implements PhaseModule.Inner_BufferHelper {
      private int init = 1;

      @Override
      public void init_V() {
         this.init = 1;
         LocalPlayer player = Module.MC.player;
         if (player != null) {
            PhaseModule.init(player, 0.0);
         }
      }

      @Override
      public void load() {
         AuditClientFixes.clearNoPhysicsIfUnused();
      }

      @Override
      public void sync() {
         if (!AutismLiteVariant.enabled()) {
            LocalPlayer player = Module.MC.player;
            if (player != null && Module.MC.level != null) {
               switch (this.init) {
                  case 1:
                     if (Module.MC.options.keyJump.isDown() && !Module.MC.level.getBlockState(player.blockPosition()).isAir()) {
                        PhaseModule.init(player, 0.42);
                        this.init++;
                     }

                     player.setOnGround(true);
                     break;
                  case 2:
                     PhaseModule.init(player, 0.33);
                     this.init++;
                     break;
                  case 3:
                     PhaseModule.init(player, 0.25);
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

      VoxelShape init(VoxelShape original, BlockState state, LocalPlayer player, BlockPos pos) {
         if (state.getBlock() instanceof LiquidBlock) {
            return original;
         } else {
            return pos.getY() >= player.getY() ? Shapes.empty() : original;
         }
      }
   }

   private static final class Inner_DataUtil implements PhaseModule.Inner_BufferHelper {
      private static final int init_I = 3;
      private final BlinkPacketManager.Inner_StreamUtil init_HashUtil_StreamUti = this::init;
      private volatile PhaseModule.Inner_DataUtil.Inner_DataUtil_2 init_aF_DataUtil_DataUt;
      private volatile boolean init_Z;
      private volatile String init_Str;
      private volatile boolean load_Z;
      private int load_I;
      private boolean read_Z;
      private int read_I;

      private Inner_DataUtil() {
         this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
      }

      @Override
      public void init_V() {
         this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         this.load_I = 0;
         this.init_Str = null;
         this.load_Z = false;
         this.read_Z = false;
         this.read_I = 0;
         BlinkPacketManager.init(this.init_HashUtil_StreamUti);
      }

      @Override
      public void load() {
         this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.FLUSHING;
         BlinkPacketManager.flush();
         this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         BlinkPacketManager.load(this.init_HashUtil_StreamUti);
         BlinkPacketManager.init(true, true);
         this.load_I = 0;
         this.init_Str = null;
         this.load_Z = false;
         this.read_Z = false;
         this.read_I = 0;
      }

      @Override
      public void read() {
         LocalPlayer player = Module.MC.player;
         if (player != null) {
            this.init_Z = true;

            try {
               if (this.init_aF_DataUtil_DataUt == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING) {
                  if (PhaseModule.init(player, player.position())) {
                     this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING;
                  }
               } else if (this.init_aF_DataUtil_DataUt == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING && !PhaseModule.init(player, player.position())) {
                  this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING;
                  this.read_Z = true;
               }
            } finally {
               this.init_Z = false;
            }
         }
      }

      @Override
      public void write() {
         String reason = this.init_Str;
         if (reason != null) {
            this.init_Str = null;
            PhaseModule.init_aF.disableWithToggleMessage(reason);
         } else if (this.load_Z) {
            this.load_Z = false;
            this.flush();
         } else if (this.init_aF_DataUtil_DataUt == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING) {
            LocalPlayer player = Module.MC.player;
            if (player != null) {
               boolean collides = false;
               this.init_Z = true;

               try {
                  for (Vec3 pos : BlinkPacketManager.init_List()) {
                     if (PhaseModule.init(player, pos)) {
                        collides = true;
                        break;
                     }
                  }
               } finally {
                  this.init_Z = false;
               }

               if (!collides) {
                  PhaseModule.init_aF.setEnabled(false);
               }
            }
         }
      }

      @Override
      public boolean init_Z() {
         if (this.load_I > PhaseModule.init_aF.integer("maximum")) {
            this.load_Z = true;
            return true;
         }

         if (this.init_aF_DataUtil_DataUt == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.PHASING || this.init_aF_DataUtil_DataUt == PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WALKING
            )
          {
            this.load_I++;
         }

         return false;
      }

      private void flush() {
         this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.FLUSHING;
         BlinkPacketManager.flush();
         this.init_aF_DataUtil_DataUt = PhaseModule.Inner_DataUtil.Inner_DataUtil_2.WAITING;
         BlinkPacketManager.init(false, true);
         this.load_I = 0;
         if (this.read_Z) {
            this.read_I = 0;
         } else if (++this.read_I >= 3) {
            this.init_Str = "Phase disabled: block did not clear.";
         }

         this.read_Z = false;
      }

      @Override
      public void update() {
         this.init_Str = "Phase disabled: server set you back.";
      }

      private BlinkPacketManager.Inner_DataUtil init(Packet<?> packet, boolean incoming) {
         if (aD.read_Z()) {
            return BlinkPacketManager.Inner_DataUtil.FLUSH;
         } else {
            PhaseModule.Inner_DataUtil.Inner_DataUtil_2 current = this.init_aF_DataUtil_DataUt;
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

      VoxelShape init(VoxelShape original, LocalPlayer player, BlockPos pos) {
         if (!this.init_Z && !this.init_aF_DataUtil_DataUt.init_Z) {
            return pos.getY() >= player.position().y || player.isShiftKeyDown() && player.onGround() ? Shapes.empty() : original;
         } else {
            return original;
         }
      }

      @Override
      public String init_Str() {
         PhaseModule.Inner_DataUtil.Inner_DataUtil_2 current = this.init_aF_DataUtil_DataUt;
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
      public void sync() {
         if (!AutismLiteVariant.enabled()) {
            LocalPlayer player = Module.MC.player;
            ClientPacketListener connection = Module.MC.getConnection();
            if (player != null && connection != null) {
               Vec3 center = Vec3.atCenterOf(player.blockPosition());
               connection.send(new PosRot(center.x, player.getY() - 0.07840000152, center.z, player.getYRot(), player.getXRot(), false, false));
               PhaseModule.init_aF.disableWithToggleMessage("Phase: clip packet sent.");
            }
         }
      }
   }
}
