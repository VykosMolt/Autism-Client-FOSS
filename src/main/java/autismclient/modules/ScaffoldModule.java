package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.ValueRange;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PlacementTickStamp;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.PlacedBlockTrailRenderer;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.AsyncTraceLogger;
import autismclient.util.SmoothRotationIntegrator;
import autismclient.util.RegistryIdValidator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Fail;
import net.minecraft.world.InteractionResult.Pass;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ScaffoldModule extends Module {
   static final int init_I = 5;
   static final int load_I = 5;
   static final float init_F = 2.0F;
   static final float load_F = 180.0F;
   static final double init_D = 0.0;
   static final float read_F = 1.0F;
   static final boolean init_Z = true;
   static final String SWITCH_BACK_DESCRIPTION = "Restore previous hotbar slot.";
   static final String REMOVE_LIMITS_DESCRIPTION = "Uncapped rotation and placement rates; safety stays.";
   private static final double buffer_D = 0.15;
   private static final double entry_D = 1.0E-9;
   static final float write_F = 20.0F;
   static final float update_F = 10.0F;
   private static final int state_I = 2;
   private static final int queue_I = 4;
   private static final double node_D = 2.0;
   private static final double task_D = 0.05;
   private static final double pool_D = 2.0;
   private static final double index_D = 0.5;
   private static final double count_D = 0.05;
   private static final double flags_D = 1.1;
   private static final double level_D = 4.0;
   static final float sync_F = 38.0F;
   private static final double frame_D = 0.22;
   static final double load_D = 0.15;
   static final double read_D = 0.06;
   static final int read_I = 2;
   static final int write_I = 3;
   static final double write_D = 2.2;
   static final double update_D = 0.23;
   private static final float queue_F = 37.5F;
   private static final int buffer_I = 2;
   static final int update_I = -1;
   static final int sync_I = 4;
   static final float flush_F = 30.0F;
   static final float close_F = 36.0F;
   static final float open_F = 22.5F;
   private static final double block_D = 0.62;
   private static final double chunk_D = 0.38;
   static final float poll_F = 12.0F;
   static final float push_F = 2.0F;
   private static final int entry_I = 8;
   static final int flush_I = 2;
   private static final int node_I = 6;
   static final double sync_D = 0.3;
   private static final int task_I = 10;
   private static final VoxelShape CENTRE_SUPPORT_SHAPE = Block.column(2.0, 0.0, 10.0);
   private static final double table_D = 0.45;
   private static final double store_D = 60.0;
   private static final double drain_D = 0.35;
   private static final double fill_D = 0.3;
   private static final int pool_I = 3;
   private static final double mark_D = 0.15;
   private static final double scan_D = 0.8;
   private static final int index_I = 3;
   private static final int count_I = 20;
   private static final int flags_I = 10;
   private static final int level_I = 30;
   private static final int frame_I = 6;
   private static final double parse_D = 0.2;
   static final float pop_F = 18.0F;
   static final float tick_F = 0.2F;
   private static final int block_I = 64;
   static final float run_F = 89.0F;
   static final float get_F = 89.5F;
   private static final int chunk_I = 4;
   private static final int table_I = 8;
   private static final int store_I = 27;
   static final float set_F = 37.0F;
   static final float put_F = 37.0F;
   static final float add_F = 20.0F;
   private static final int drain_I = 12;
   private static final double bind_D = 0.02;
   static final double flush_D = 0.003;
   private static final double f_a_D = 0.29;
   private static final double f_b_D = 0.15;
   private static final int fill_I = 20;
   static final double close_D = 0.25;
   private static final double f_c_D = 0.75;
   private static final double f_d_D = 0.03;
   private static final double f_e_D = 0.35;
   private static final double f_f_D = 0.01;
   private static final int mark_I = 24;
   private static final double f_g_D = 0.42;
   private static final double f_h_D = 1.7;
   private static final int scan_I = 3;
   private static final int parse_I = 6;
   private static final double f_i_D = 2.5;
   private static final int bind_I = 4;
   static final int close_I = 2;
   private static final int f_a_I = 64;
   private static final int f_b_I = 150;
   private static final int f_c_I = 8;
   private static final long poll_J = 1000L;
   static final long init_J = 400L;
   static final long load_J = 1000L;
   static final float clear_F = 85.0F;
   static final float reset_F = 3.0F;
   static final float start_F = 20.0F;
   static final float stop_F = 70.0F;
   static final long read_J = 800L;
   static final int open_I = 5;
   static final long write_J = 5000L;
   static final float next_F = 0.5F;
   static final float prev_F = 84.5F;
   static final int poll_I = 2;
   static final int push_I = 3;
   static final int pop_I = 5;
   static final int tick_I = 6;
   private static final long push_J = 750L;
   private static final double f_j_D = 1.08;
   static final int run_I = 25;
   static final double open_D = 1.0;
   static final double poll_D = 0.3;
   static final double push_D = 0.8;
   static final double pop_D = 0.12;
   static final double tick_D = 0.3;
   private static final int f_d_I = 90;
   private static final int f_e_I = 2;
   static final int get_I = 3;
   static final int set_I = 5;
   private static final int f_f_I = 40;
   private static final float buffer_F = 68.0F;
   private static final float entry_F = 12.0F;
   private static final double f_k_D = 0.03;
   private static final double[] LOOKAHEAD_DISTANCES = new double[]{1.5, 2.0, 2.5};
   static final int put_I = 20;
   private static final double[] SUPPORT_SAMPLE_OFFSETS = new double[]{0.301, 0.0, -0.301};
   private static final int f_g_I = 4;
   private static final int f_h_I = 4;
   private static final double f_l_D = 0.001;
   private static final double f_m_D = 0.02;
   private static final double f_n_D = 0.2;
   private static final double f_o_D = 0.05;
   private static final double f_p_D = 3.0;
   private static final double f_q_D = 1.0E-4;
   static final double run_D = 0.25;
   static final double get_D = 0.5;
   static final double set_D = 0.1;
   static final double put_D = 0.02;
   static final double add_D = 0.04;
   static final double clear_D = -0.12;
   static final double reset_D = -0.14;
   static final double start_D = 0.05;
   static final int add_I = 14;
   static final double stop_D = -0.12;
   static final int clear_I = 2;
   static final int reset_I = 2;
   static final double next_D = 0.25;
   private static final double f_r_D = 0.91;
   private static final double f_s_D = 0.546;
   private static final double f_t_D = 0.096;
   private static final double f_u_D = 0.999;
   private static final int f_i_I = 2;
   private static final List<BlockPos> NEIGHBOUR_OFFSETS = neighbourOffsets();
   static final int start_I = 5;
   static final int stop_I = 0;
   static final int[] TURN_DELAY_TICKS = new int[]{0, 1, 3};
   static final int MAX_TURN_DELAY_INDEX = TURN_DELAY_TICKS.length - 1;
   static final int prev_I = 2;
   static final int valid_I = 4;
   private static final boolean load_Z = true;
   private static final boolean read_Z = false;
   private int switchBackSlot = -1;
   private int lastRequestedSlot = -1;
   private int scaffoldSlot = -1;
   private int switchBackDelay;
   private boolean slotSwitched;
   private RotationMath.Rotation sentRotation;
   private ScaffoldModule.MotionVectorPair courseLine;
   private final ArrayDeque<BlockPos> recentPlaced = new ArrayDeque<>(4);
   private final ArrayDeque<Vec3> recentOffsets = new ArrayDeque<>(5);
   private BlockPos sampledSupport;
   private ScaffoldModule.SupportHeightSample supportSample;
   private int f_n_I;
   private int supportSampleTick = -2147483648;
   private ScaffoldModule.MotionVectorPair lockedLine;
   private int courseOctant = -1;
   private int candidateOctant = -1;
   private int candidateDwell;
   private int inputOctant;
   private float heldLaneYaw = 0.0F / 0.0F;
   private float candidateLaneYaw = 0.0F / 0.0F;
   private int candidateYawStreak;
   private int laneYawTick = -2147483648;
   private String blockListRaw = "";
   private Set<Block> blockListCache = Set.of();
   private RotationMath.Rotation activeRotation;
   private int rotationHoldTicks;
   private int settleTicks;
   private final SmoothRotationIntegrator rotationIntegrator = new SmoothRotationIntegrator();
   private RotationMath.Rotation previousGoal;
   private float directionChange;
   private final autismclient.util.RotationStreamPlanner.StreamState rotationStream = new autismclient.util.RotationStreamPlanner.StreamState();
   private final RangeSetting rotationSmoothness;
   private ScaffoldRotationSmoother rotationSmoother;
   private ScaffoldRotationSmoother.Inner_DataUtil cachedSmootherBand;
   private boolean update_Z;
   private float index_F = 0.0F / 0.0F;
   private int f_x_I = -2147483648;
   private int rotatedTick = -2147483648;
   private int lastPlaceTick = -2147483648;
   private final Random random = new Random();
   private boolean rotationWindingDown;
   private boolean edgeGateActive;
   private float basePitch = 74.0F;
   private ScaffoldModule.TellyPhase tellyPhase = ScaffoldModule.TellyPhase.IDLE;
   private ScaffoldModule.TurnCyclePhase turnPhase = ScaffoldModule.TurnCyclePhase.FORWARD;
   private ScaffoldModule.QuantizedPlacement quantizedPlacement;
   private boolean close_Z;
   private boolean imperfectionArmed;
   private final ScaffoldPlaceRateTracker placeRateTracker = new ScaffoldPlaceRateTracker();
   private int imperfectionCount;
   private boolean imperfectionRequested;
   private BlockPos imperfectionCell;
   private List<BlockPos> imperfectionSupports = List.of();
   private final Item[] slotItems = new Item[9];
   private ClientLevel slotCacheLevel;
   private int blockAckSequence = -2147483648;
   private final int[] slotRemaining = new int[9];
   private final int[] slotUseSequence = new int[9];
   private final int[] slotUseTick = new int[9];
   private int useSlot = -1;
   private ScaffoldModule.ForwardKeyState forwardKeyState = ScaffoldModule.ForwardKeyState.RELEASED;
   private boolean ownsInput;
   private BlockPos read_BlockPos;
   private boolean pop_Z;
   private boolean dangerStop;
   private ScaffoldModule.SneakHoldFlags sneakHold;
   private Input airborneInput;
   private Input stableInput;
   private Input lastLiveInput;
   private int inputStableTicks;
   static final int size_I = 3;
   private boolean run_Z;
   private boolean spaceWasDown;
   private boolean set_Z;
   private boolean spaceHeld;
   private boolean add_Z;
   private boolean clear_Z;
   private BlockPos write_BlockPos;
   private int batchPower;
   private boolean reset_Z;
   private boolean start_Z;
   private boolean stop_Z;
   private boolean next_Z;
   private int walkOffGraceTicks;
   private int f_G_I;
   private int tellyAirTicks;
   private int f_I_I;
   private int f_J_I;
   private int f_K_I;
   private int supportY;
   private double launchY;
   private double launchProgress;
   private float anchorYaw;
   private float anchorPitch;
   private double laneOffset;
   private int f_M_I;
   private boolean prev_Z;
   private boolean valid_Z;
   private float steerOffset;
   private float block_F;
   private ScaffoldModule.StrafeSide strafeSide;
   private ScaffoldModule.StrafeSide lastStrafePulse;
   private int strafeCooldown;
   private int strafePulsesUsed;
   private int f_P_I;
   private int f_Q_I;
   private int anchorDeviationStreak;
   private int f_S_I;
   private boolean size_Z;
   private int aimTick;
   private int f_U_I;
   private int selectedOffset;
   private boolean data_Z;
   private int f_W_I;
   private boolean farFromEdge;
   private boolean cache_Z;
   private boolean diagonalBridge;
   private boolean queue_Z;
   private boolean buffer_Z;
   private boolean braking;
   private boolean node_Z;
   private int f_X_I;
   private ScaffoldModule.PushTarget pushTarget;
   private int pushTargetTick;
   private BlockPos pushFooting;
   private int f_Z_I;
   private RotationMath.Rotation plannedAim;
   private boolean task_Z;
   private int f_aa_I;
   private int f_ab_I;
   private BlockPos laneSupport;
   private BlockPos groundedSupport;
   private int groundedSupportTick;
   private BlockPos close_BlockPos;
   private BlockPos open_BlockPos;
   private BlockPos entrySupport;
   private BlockPos entryCell;
   private boolean pool_Z;
   private Vec3 laneCentrePoint;
   private ScaffoldModule.LivePlacement livePlacement;
   private long pop_J;
   private static final String[] OCTANT_NAMES = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
   private int leadMotionTick;
   private Vec3 leadImpulse;
   private int airTicks;
   private int f_af_I;
   private Vec3 takeoffPosition;
   private BlockPos trackedSupport;
   private Direction trackedFace;
   private int f_ag_I;
   private int f_ah_I;
   private int f_ai_I;
   private int paceJitterMs;
   static final long update_J = 3L;
   static final long sync_J = 50L;
   static final long flush_J = 100L;
   static final long close_J = 350L;
   static final long open_J = 250L;
   private BlockPos tick_BlockPos;
   private int f_ak_I;
   private static final int f_al_I = 8;
   private final ArrayDeque<ScaffoldModule.PlacedBlockStamp> placedStamps;
   private final ArrayDeque<Long> placeTimes;
   private final ArrayDeque<Long> placeGaps;
   private final ArrayDeque<BlockPos> sync_ArrayDeque;
   private long lastGapNanos;
   private float lastWirePitch;
   private long lastPlaceNanos;
   private long jumpStartNanos;
   private long writtenClickNanos;
   private boolean wasOnGround;
   private BlockPos heldFaceSupport;
   private Direction heldFace;
   private int heldFaceTicks;
   private int heldFaceTick;
   private int lastSneakTick;
   private int sneakLatchTicks;
   private int sneakHeldTicks;
   private int f_ar_I;
   private final Map<BlockPos, Integer> recentCells;
   private static final int f_as_I = 10;
   private static final int f_at_I = 32;
   static final double prev_D = 0.05;
   private int liveCaptureTick;
   private int f_av_I;
   private int tellyCaptureTick;
   private int tellyTraceTick;
   private ScaffoldModule.PlannedPlacement heldPlacement;
   private BlockPos upFaceSwapCell;
   private double f_y_D;
   private int f_ay_I;
   private boolean count_Z;
   private double f_z_D;
   private int f_az_I;
   private boolean flags_Z;
   private boolean level_Z;
   private int f_aA_I;
   private Input lastOriginalInput;
   private int originalInputTick;
   private Direction waitFace;
   private int waitFaceTick;
   private double starvedTravel;
   private int starveTick;
   private int heldPlacementTick;
   private boolean frame_Z;
   private int heldServedTick;
   private ScaffoldModule.PlannedPlacement stickyPlacement;
   private int stickyTick;
   private int f_aH_I;
   private int f_aI_I;
   private static final ConcurrentLinkedQueue<ScaffoldModule.QueuedPlacementClick> writtenClicks = new ConcurrentLinkedQueue<>();
   private static final AtomicReference<ScaffoldModule.InputFlags> writtenInputFlags = new AtomicReference<>();
   private static volatile Vec3 lastWrittenPosition;
   private static volatile Vec3 lastPublishedEye;
   private static volatile boolean writtenSneak;
   private static final ReferenceQueue<Packet<?>> packetKeyQueue = new ReferenceQueue<>();
   private static final Map<ScaffoldModule.WeakPacketKey, ScaffoldModule.QueuedPacketContext> queuedPackets = new HashMap<>();
   private ScaffoldModule.PlannedPlacement pendingPlacement;
   private ScaffoldModule.MotionVectorPair pendingLine;
   private Vec3 pendingFallOff;
   private int pendingTick;
   private ScaffoldModule.PlacementState placementState;
   private long activeGeneration;
   private long generationCounter;
   private InteractionHand pendingHand;
   private boolean buildsPlannedCell;
   private int submittedCount;
   private boolean duplicateSubmitted;
   private int f_aL_I;
   private RotationMath.Rotation clickRotation;
   private RotationMath.Rotation submitRotation;
   private int pendingSequence;
   private boolean resultSeen;
   private boolean resultConsumed;
   private boolean clickWritten;
   private String pendingStatus;
   private final ArrayDeque<ScaffoldModule.PendingPlacementSeq> predictions;
   private final ScaffoldFailureRecorder failureRecorder;
   private final AtomicBoolean capturingFailure;
   private final Map<BlockPos, Integer> predictedCells;
   private ClientLevel trackedLevel;
   private volatile int ackedSequence;
   private int reconciledSequence;
   private boolean inputFlagsSeen;
   private boolean wireOnGround;
   private boolean wireHorizontalCollision;
   private boolean wireHasPosition;
   private int f_aP_I;
   private BlockPos set_BlockPos;
   private Vec3 lastPosition;
   private Vec3 lastPositionDelta;
   private float appliedSteerBias;
   private int emittedForward;
   private int emittedSideways;
   private int f_aS_I;
   private boolean f_a_Z;
   private float lastRawForward;
   private float lastRawSideways;
   private float fill_F;
   private int f_aT_I;
   private BlockPos reservedSupport;
   private BlockPos reservedConnector;
   private BlockPos reservedRiser;
   private BlockPos reservedStep;
   private boolean reservationActive;
   private String reservationReason;
   private boolean physicalClimbIntent;
   private int f_aU_I;
   private int f_aV_I;
   private boolean edgeDanger;
   private boolean fallDanger;
   private boolean lateralBrink;
   private String brakeMode;
   private String carryLabel;
   private double carryDistance;
   private String pushStatus;
   private Vec3 carryStart;
   private boolean footingOwed;
   private int footingOwedTicks;
   private boolean f_h_Z;
   private boolean f_i_Z;
   private String open_Str;
   private BlockPos riseTakeoffCell;
   private boolean rising;
   private boolean predictionReady;
   private boolean clickLanded;
   private String steerMode;
   private String push_Str;
   private String reservationLedger;
   private final StringBuilder stripTrace;
   private String tick_Str;
   private int f_aX_I;
   private int f_aY_I;
   private Vec3 traceLastPosition;
   private String reservationSnapshot;
   private int placeCount;
   private int settleOkCount;
   private int aimHoldCount;
   private int paceHoldCount;
   private int f_bd_I;
   private int f_be_I;
   private String get_Str;
   private BlockPos traceSupport;
   private BlockPos traceRiser;
   private int f_bf_I;
   private int f_bg_I;
   private int f_bh_I;
   private int f_bi_I;
   private int f_bj_I;
   private String set_Str;
   private String put_Str;
   private int supportChanges;
   private String placeStatus;
   private String dropReason;
   private long paceElapsedMs;
   private long paceWaitMs;
   private boolean paceUsedMean;
   private boolean f_n_Z;
   private boolean f_o_Z;
   private int f_bl_I;
   private int f_bm_I;
   private int f_bn_I;
   private int f_bo_I;
   private int f_bp_I;
   private int f_bq_I;
   private int f_br_I;
   private int f_bs_I;
   private int f_bt_I;
   private int f_bu_I;
   private int f_bv_I;
   private int f_bw_I;
   private String planFailure;
   private final StringBuilder planDetail;
   private String aimDecision;
   private String targetSource;
   private BlockPos pinSupport;
   private Direction pinFace;
   private BlockPos missTargetSupport;
   private Direction missTargetFace;
   private int missStreakCount;
   private float lastDebugYaw;
   private float lastDebugPitch;
   private double debugYawTravel;
   private double debugPitchTravel;
   private int debugSamples;
   private int lastDebugTick;
   private float appliedYaw;
   private BlockPos lastAcknowledgedCell;
   private int driftTick;
   private boolean driftGrounded;
   private int driftAirTicks;
   private int driftGroundTicks;
   private int f_bD_I;
   private double driftResidualSum;
   private double driftLateralSum;
   private double f_G_D;
   private double lastLaneError;
   private double lastCourseProgress;
   private int kinematicsTick;
   private boolean kinematicsGrounded;
   private boolean f_r_Z;
   private float kinematicsAnchorYaw;
   private float unusedPitchDelta;
   private float lastPlaceYaw;
   private RotationMath.Rotation rawRotation;
   static final float valid_F = 0.35F;
   private int lastDitherSign;
   private BlockHitResult aimRay;
   private float placePitch;
   private int rotationStepCount;
   private static final double f_J_D = 0.08;
   private static final double f_K_D = 0.035;
   private static final double f_L_D = 0.012;
   private static final double f_M_D = 3.0;
   private static final double f_N_D = 0.1;
   private static final double f_O_D = 0.18;
   private static final double f_P_D = 0.055;
   private static final float f_d_F = 8.0F;
   private static final float f_e_F = 15.0F;
   private static final float f_f_F = 5.0F;
   private static final float f_g_F = 7.0F;
   private static final double f_Q_D = 0.1;
   private static final double f_R_D = 0.0125;
   static final double valid_D = 0.0196;
   static final double size_D = 0.026;
   static final int data_I = 4;
   static final double data_D = 0.05;
   static final double value_D = 0.25;
   private static final double f_S_D = 0.91;
   private static final double f_T_D = 0.16;
   private static final double f_U_D = 0.1;
   private static final double f_V_D = 0.48;
   private static final int f_bH_I = 2;
   private static final int f_bI_I = 2;
   private static final int f_bJ_I = 3;
   private static final double f_W_D = 0.12;
   static final double cache_D = 0.03;
   static final double state_D = 0.05;
   private static final int f_bK_I = 0;
   private static final double[] FACE_AIM_OFFSETS = new double[]{0.0, -0.16, 0.16, -0.28, 0.28};
   static final double queue_D = 0.026;
   static final float size_F = 15.0F;
   static final float data_F = 3.0F;
   static final float value_F = 95.0F;
   private static final float f_h_F = 179.5F;
   private static final float f_i_F = 1.0F;
   private static final float f_j_F = 2.0F;
   private static final double f_X_D = 0.08;
   private static final float f_k_F = 25.0F;
   private static final double f_Y_D = 0.45;
   private static final int f_bL_I = 1;
   private static final int f_bM_I = 30;
   static final float cache_F = 18.0F;
   private int lastTickedClientTick;
   private static int inputOverrideTick = -2147483648;
   private final Vec3[] recentPositions;
   private final Vec3[] recentVelocities;
   private final boolean[] recentGrounded;
   private boolean lastShift;
   private int lastInputTick;
   private String next_Str;
   private BlockPos data_BlockPos;
   private int f_bQ_I;
   private BlockPos lastRiseCell;
   private int riseCellGraceTicks;
   private Input recoveryInput;
   private static float lastSentYaw = 0.0F / 0.0F;
   private static int appliedRotationTick = -2147483648;
   private int paceWaitTicks;
   static final int value_I = 4;
   private int correctionSide;
   private int correctionHeldTicks;
   private int correctionLockTicks;
   private final autismclient.util.MovementInputFixer movementInputFixer;
   private boolean sweeping;
   private int sweepTicks;
   private static final double[] INPUT_BOUNDARY_ANGLES = new double[]{30.0, 150.0, -150.0, -30.0};
   private int f_bY_I;
   private int f_bZ_I;
   private float f_m_F;
   private int outOfReachTicks;
   private int groundedFootingRow;
   private int footingRiseTick;
   private int airborneBuiltRow;
   private double f_Z_D;
   private static BlockPos cachedSupportPos;
   private static BlockState cachedSupportState;
   private static AABB cachedSupportBox;
   static final int cache_I = 20;
   static final float state_F = 89.3F;
   private static final double f_aa_D = 0.06;

   ScaffoldModule() {
      super("scaffold", "Scaffold", ae.load_ae, "Places blocks beneath you.");
      this.sneakHold = ScaffoldModule.SneakHoldFlags.NONE;
      this.strafeSide = ScaffoldModule.StrafeSide.NONE;
      this.lastStrafePulse = ScaffoldModule.StrafeSide.NONE;
      this.f_P_I = -1;
      this.aimTick = -2147483648;
      this.f_U_I = -2147483648;
      this.selectedOffset = -1;
      this.data_Z = true;
      this.f_X_I = 1;
      this.pushTargetTick = -2147483648;
      this.f_Z_I = -2147483648;
      this.groundedSupportTick = -2147483648;
      this.leadMotionTick = -2147483648;
      this.leadImpulse = Vec3.ZERO;
      this.tick_BlockPos = null;
      this.f_ak_I = -2147483648;
      this.placedStamps = new ArrayDeque<>();
      this.placeTimes = new ArrayDeque<>();
      this.placeGaps = new ArrayDeque<>();
      this.sync_ArrayDeque = new ArrayDeque<>();
      this.lastGapNanos = -9223372036854775808L;
      this.lastWirePitch = 0.0F / 0.0F;
      this.lastPlaceNanos = -9223372036854775808L;
      this.jumpStartNanos = -9223372036854775808L;
      this.writtenClickNanos = -9223372036854775808L;
      this.heldFaceTick = -2147483648;
      this.lastSneakTick = -2147483648;
      this.recentCells = new HashMap<>();
      this.liveCaptureTick = -1;
      this.f_av_I = -2147483648;
      this.tellyCaptureTick = -1;
      this.tellyTraceTick = -2147483648;
      this.f_y_D = 0.0 / 0.0;
      this.f_ay_I = -2147483648;
      this.f_z_D = 0.0 / 0.0;
      this.f_az_I = -2147483648;
      this.originalInputTick = -2147483648;
      this.waitFaceTick = -2147483648;
      this.starveTick = -2147483648;
      this.heldPlacementTick = -1;
      this.heldServedTick = -2147483648;
      this.stickyTick = -1;
      this.f_aI_I = -2147483648;
      this.pendingTick = -2147483648;
      this.placementState = ScaffoldModule.PlacementState.IDLE;
      this.pendingSequence = -1;
      this.pendingStatus = "--";
      this.predictions = new ArrayDeque<>();
      this.failureRecorder = new ScaffoldFailureRecorder();
      this.capturingFailure = new AtomicBoolean();
      this.predictedCells = new HashMap<>();
      this.ackedSequence = -2147483648;
      this.reconciledSequence = -2147483648;
      this.f_aP_I = -2147483648;
      this.lastPositionDelta = Vec3.ZERO;
      this.fill_F = 0.0F / 0.0F;
      this.reservationReason = "--";
      this.f_aU_I = -2147483648;
      this.f_aV_I = -2147483648;
      this.brakeMode = "--";
      this.carryLabel = "--";
      this.pushStatus = "--";
      this.open_Str = "-";
      this.steerMode = "cam";
      this.push_Str = "--";
      this.reservationLedger = "--";
      this.stripTrace = new StringBuilder();
      this.tick_Str = "--";
      this.f_aX_I = -1;
      this.f_aY_I = -1;
      this.reservationSnapshot = "--";
      this.get_Str = "--";
      this.f_bf_I = -1;
      this.f_bg_I = -1;
      this.f_bh_I = -1;
      this.f_bi_I = -1;
      this.f_bj_I = -1;
      this.set_Str = "--";
      this.put_Str = "--";
      this.placeStatus = "--";
      this.dropReason = "--";
      this.paceElapsedMs = -1L;
      this.paceWaitMs = -1L;
      this.f_n_Z = true;
      this.f_bv_I = -2147483648;
      this.f_bw_I = -2147483648;
      this.planFailure = "?";
      this.planDetail = new StringBuilder();
      this.aimDecision = "?";
      this.targetSource = "?";
      this.lastDebugYaw = 0.0F / 0.0F;
      this.lastDebugPitch = 0.0F / 0.0F;
      this.lastDebugTick = -2147483648;
      this.appliedYaw = 0.0F / 0.0F;
      this.driftTick = -2147483648;
      this.driftGrounded = true;
      this.lastLaneError = 0.0 / 0.0;
      this.lastCourseProgress = 0.0 / 0.0;
      this.kinematicsTick = -2147483648;
      this.kinematicsAnchorYaw = 0.0F / 0.0F;
      this.lastPlaceYaw = 0.0F / 0.0F;
      this.placePitch = 0.0F / 0.0F;
      this.lastTickedClientTick = -2147483648;
      this.recentPositions = new Vec3[2];
      this.recentVelocities = new Vec3[2];
      this.recentGrounded = new boolean[2];
      this.lastInputTick = -2147483648;
      this.next_Str = "air";
      this.f_bQ_I = -2147483648;
      this.movementInputFixer = new autismclient.util.MovementInputFixer();
      this.f_m_F = 0.0F / 0.0F;
      this.groundedFootingRow = -2147483648;
      this.footingRiseTick = -2147483648;
      this.airborneBuiltRow = -2147483648;
      this.f_Z_D = 0.0 / 0.0;
      this.add(new ChoiceSetting("mode", "Mode", "Legit", "Legit", "Rage", "Telly", "Eagle").description("Choose scaffold mode.").build());
      this.add(
         new BoolSetting("remove-limits", "Remove Limits", false)
            .visibleWhen(() -> this.node_Z() && !this.isTellyOrEagleMode())
            .description("Uncapped rotation and placement rates; safety stays.")
            .build()
      );
      this.add(
         new BoolSetting("allow-diagonal", "Allow Diagonal", false)
            .visibleWhen(this::isTellyOrEagleMode)
            .description("Use Legit bridging for diagonal movement, retaining placement and landing checks.")
            .build()
      );
      if (!AutismLiteVariant.enabled()) {
         this.add(
            new BoolSetting("eagle-jump", "Jump", true)
               .visibleWhen(this::isEagleMode)
               .description("Jump while bridging. Off walks backward without jumping or sneaking.")
               .build()
         );
      }

      this.add(
         new IntSetting("imperfection", "Imperfection", 80, 0, 200, 1)
            .unit("%")
            .group("Humanization")
            .visibleWhen(() -> this.isTellyOrEagleMode() && !this.isEagleMode())
            .description("Extra tops per 100 flat jumps. 100 aims for one each jump. 120 aims for a second on 20%.")
            .build()
      );
      this.rotationSmoothness = this.add(
         new RangeSetting("rotation-smoothness", "Rotation Smoothness", 20.0, 100.0, 0.0, 100.0, 1.0)
            .unit("%")
            .group("Humanization")
            .visibleWhen(this::isTellyOrEagleMode)
            .description("Random smoothness per sweep. Turns stay bounded at every setting; higher values ease them over more time.")
            .build()
      );
      this.add(
         new IntSetting("turn-delay", "Turn Delay", 2, 0, MAX_TURN_DELAY_INDEX, 1)
            .group("Humanization")
            .visibleWhen(() -> this.isTellyOrEagleMode() && !this.isEagleMode())
            .description("Preferred delay. Each jump turns earlier only when its placement budget requires it.")
            .build()
      );
      this.add(new BoolSetting("switch-back", "Switch Back", true).description("Restore previous hotbar slot.").build());
      this.add(new ChoiceSetting("filter-mode", "Filter", "Off", "Off", "Whitelist", "Blacklist").description("Choose filter mode.").build());
      this.add(new IntSetting("rage-blocks", "Blocks Ahead", 0, 0, 5, 1).visibleWhen(this::isRageOrFastMode).description("Blocks built ahead.").build());
      this.add(
         RegistryListSetting.placeableBlocks("blocks", "Blocks")
            .visibleWhen(() -> !"Off".equals(this.choice("filter-mode")))
            .description("Choose filtered blocks.")
            .build()
      );
      this.add(new BoolSetting("place-animation", "Place Animation", true).description("Placement ripple effect").group("Animation").build());
      this.add(
         new BoolSetting("animation-custom", "Custom Color", false)
            .description("Custom ripple color")
            .group("Animation")
            .visibleWhen(() -> this.bool("place-animation"))
            .build()
      );
      this.add(
         new ColorSetting("animation-color", "Color", -50373)
            .description("Ripple color")
            .group("Animation")
            .visibleWhen(() -> this.bool("place-animation") && this.bool("animation-custom"))
            .build()
      );
   }

   @Override
   public void onEnable() {
      this.releaseControls(true);
      this.failureRecorder.init();
      this.rerollAimPitch();
      this.liveCaptureTick = -1;
      this.tellyCaptureTick = this.init_I_16();
      this.tellyTraceTick = -2147483648;
      this.lastDebugYaw = 0.0F / 0.0F;
      this.lastDebugPitch = 0.0F / 0.0F;
      this.debugYawTravel = 0.0;
      this.debugPitchTravel = 0.0;
      this.debugSamples = 0;
      this.syncPlaceAnimation();
   }

   @Override
   public String info() {
      return this.choice("mode");
   }

   @Override
   public void onDisable() {
      RotationMath.Rotation live = this.activeRotation;
      RotationMath.Rotation liveRaw = this.rawRotation;
      float liveSent = lastSentYaw;
      boolean tellyLive = autismclient.util.RotationStreamPlanner.isActive(this.rotationStream);
      this.releaseControls(false);
      if (live != null && MC != null && MC.player != null) {
         if (tellyLive) {
            autismclient.util.RotationStreamPlanner.begin(this.rotationStream, live);
         }

         this.activeRotation = live;
         this.rawRotation = liveRaw != null ? liveRaw : live;
         lastSentYaw = liveSent;
         this.rotationHoldTicks = 5;
         this.rotationWindingDown = true;
      }

      PlacedBlockTrailRenderer.recordScaffoldPlacement();
   }

   @Override
   public void onGameLeft() {
      this.releaseControls(false);
      this.clearSlotCache();
      PlacedBlockTrailRenderer.load(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD);
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if ("blocks".equals(settingId) || "filter-mode".equals(settingId)) {
         this.blockListRaw = null;
      }

      if ("switch-back".equals(settingId) && !this.bool("switch-back")) {
         this.switchBackSlot = -1;
         this.switchBackDelay = 0;
      }

      if ("mode".equals(settingId)) {
         this.releaseControls(true);
         this.liveCaptureTick = -1;
         this.tellyCaptureTick = this.init_I_16();
         this.tellyTraceTick = -2147483648;
      }

      this.syncPlaceAnimation();
   }

   private int init_I_16() {
      if (this.pool_Z()) {
      }

      return -1;
   }

   private void syncPlaceAnimation() {
      PlacedBlockTrailRenderer.init(this.isEnabled() && this.bool("place-animation"), this.bool("animation-custom"), am.init_I_Module(this, "animation-color", -50373));
   }

   @Override
   public void preMovementTick() {
      if (AutismLiteVariant.enabled() || !ClutchModule.read_Z_20()) {
         boolean tellyTickRan = false;
         if (this.isTellyOrEagleMode()) {
            if (!this.cache_Z && this.diagonalBridgeActive() && this.canOperate() && MC.player.onGround()) {
               BlockPos support = this.supportBlockUnder(MC.player);
               if (support != null) {
                  this.beginTelly(MC.player, support, true);
               }
            }

            if (this.cache_Z) {
               this.updateDiagonalMode();
            }

            if (this.pool_Z()) {
               this.trackPositionDelta();
               tellyTickRan = true;
               this.size_V();
               if (!this.cache_Z) {
                  return;
               }
            }
         }

         if (this.flags_Z()) {
            this.resetOnLevelChange();
            this.lastAcknowledgedCell = null;
            this.trackAirTicks();
            this.reconcilePending();
            if (!tellyTickRan) {
               this.trackPositionDelta();
            }

            this.tickPlacement();
         } else {
            this.refreshPlaceHand();
         }
      } else if (this.placementState == ScaffoldModule.PlacementState.ARMED) {
         this.cancelPendingPlacement();
      }
   }

   static int yawToOctant(float yaw) {
      int step = Math.round(Mth.wrapDegrees(yaw) / 45.0F);
      return (step % 8 + 8) % 8;
   }

   static float octantToYaw(int step) {
      return Mth.wrapDegrees(step * 45.0F);
   }

   static boolean isDiagonalOctant(int step) {
      return step >= 0 && (step & 1) == 1;
   }

   static int[] debounceOctant(int held, int candidate, int dwell, float cameraYaw, boolean frozen) {
      int want = yawToOctant(cameraYaw);
      if (held == -1) {
         return new int[]{want, -1, 0};
      }

      if (frozen) {
         return new int[]{held, candidate, dwell};
      }

      if (want == held) {
         return new int[]{held, -1, 0};
      }

      if (Math.abs(Mth.wrapDegrees(cameraYaw - octantToYaw(held))) <= 37.5F) {
         return new int[]{held, -1, 0};
      }

      int nextDwell = want == candidate ? dwell + 1 : 1;
      return nextDwell >= 2 ? new int[]{want, -1, 0} : new int[]{held, want, nextDwell};
   }

   static float[] debounceYaw(float held, float candidate, int streak, float requested) {
      if (Float.isNaN(held)) {
         return new float[]{requested, 0.0F / 0.0F, 0.0F};
      }

      if (requested == held) {
         return new float[]{held, 0.0F / 0.0F, 0.0F};
      }

      int nextStreak = requested == candidate ? streak + 1 : 1;
      return nextStreak >= 2 ? new float[]{requested, 0.0F / 0.0F, 0.0F} : new float[]{held, requested, nextStreak};
   }

   private void updateCourseOctant() {
      if (MC.player != null) {
         int[] next = debounceOctant(this.courseOctant, this.candidateOctant, this.candidateDwell, MC.player.getYRot(), this.k());
         this.courseOctant = next[0];
         this.candidateOctant = next[1];
         this.candidateDwell = next[2];
      }
   }

   private float courseYaw() {
      return this.courseOctant == -1 ? (MC.player == null ? 0.0F : MC.player.getYRot()) : octantToYaw(this.courseOctant);
   }

   static float inputYawOffset(Input input) {
      float yaw = 0.0F;
      float forwardMultiplier;
      if (input.backward() && !input.forward()) {
         yaw += 180.0F;
         forwardMultiplier = -0.5F;
      } else if (input.forward() && !input.backward()) {
         forwardMultiplier = 0.5F;
      } else {
         forwardMultiplier = 1.0F;
      }

      if (input.left() && !input.right()) {
         yaw -= 90.0F * forwardMultiplier;
      }

      if (input.right() && !input.left()) {
         yaw += 90.0F * forwardMultiplier;
      }

      return yaw;
   }

   static int inputOctantOffset(Input input) {
      if (!hasMovementInput(input)) {
         return 0;
      }

      int steps = Math.round(inputYawOffset(input) / 45.0F);
      return (steps % 8 + 8) % 8;
   }

   static int laneOctantFor(int courseStep, Input input) {
      return offsetOctant(courseStep, inputOctantOffset(input));
   }

   static int offsetOctant(int courseStep, int octantSteps) {
      return courseStep == -1 ? -1 : ((courseStep + octantSteps) % 8 + 8) % 8;
   }

   private void captureInputOctant(Input input) {
      if (hasMovementInput(input) && !this.k()) {
         this.inputOctant = inputOctantOffset(input);
      }
   }

   private int laneOctant() {
      return offsetOctant(this.courseOctant, this.inputOctant);
   }

   private float laneYaw() {
      int lane = this.laneOctant();
      return lane == -1 ? this.courseYaw() : octantToYaw(lane);
   }

   private Vec3 laneDirection() {
      return Vec3.directionFromRotation(0.0F, this.laneYaw());
   }

   private float debouncedLaneYaw() {
      if (this.c_Z() && this.laneOctant() == -1) {
         return this.activeRotation != null ? this.activeRotation.yaw() : MC.player.getYRot();
      }

      float requested = oppositeYaw(this.laneYaw());
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.laneYawTick) {
         float[] next = debounceYaw(this.heldLaneYaw, this.candidateLaneYaw, this.candidateYawStreak, requested);
         this.heldLaneYaw = next[0];
         this.candidateLaneYaw = next[1];
         this.candidateYawStreak = (int)next[2];
         this.laneYawTick = tick;
      }

      return this.heldLaneYaw;
   }

   static Vec3 positionDelta(Vec3 previous, Vec3 current) {
      return previous == null ? Vec3.ZERO : current.subtract(previous);
   }

   private void trackPositionDelta() {
      if (MC.player != null) {
         Vec3 pos = MC.player.position();
         this.lastPositionDelta = positionDelta(this.lastPosition, pos);
         this.lastPosition = pos;
         this.rising = false;
      }
   }

   private Vec3 leadMotion() {
      if (MC.player == null) {
         return Vec3.ZERO;
      }

      if (MC.player.onGround()) {
         return new Vec3(this.lastPositionDelta.x, 0.0, this.lastPositionDelta.z);
      }

      Vec3 delta = MC.player.getDeltaMovement();
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.leadMotionTick) {
         this.leadMotionTick = tick;
         Input keys = MC.player.input == null ? null : MC.player.input.keyPresses;
         boolean directional = hasMovementInput(keys) || this.originalInputTick == tick - 1 && hasMovementInput(this.lastOriginalInput);
         boolean geometric = directional && this.poll(true, false);
         boolean cadence = directional && this.open(true, false);
         boolean coast = geometric && this.level_Z && !cadence;
         this.leadImpulse = coast ? Vec3.ZERO : applyMoveImpulse(delta, directional, geometric || cadence, keys != null && keys.shift());
      }

      return delta.add(this.leadImpulse);
   }

   static Vec3 applyMoveImpulse(Vec3 velocity, boolean directional, boolean braking, boolean sneaking) {
      double speed = Math.hypot(velocity.x, velocity.z);
      if (directional && !(speed < 0.003)) {
         double impulse = 0.02 * (sneaking ? 0.3 : 1.0) / speed;
         if (braking) {
            impulse = -impulse;
         }

         return new Vec3(velocity.x * impulse, 0.0, velocity.z * impulse);
      } else {
         return Vec3.ZERO;
      }
   }

   private void refreshPlaceHand() {
      if (!this.canOperate()) {
         this.courseLine = null;
         this.tickSwitchBack();
      } else if (!BlinkPacketManager.isHoldingActionsOnly()) {
         this.slotSwitched = false;
         InteractionHand hand = this.placeHand();
         if (hand == null) {
            if (this.slotSwitched) {
               this.requestSwitchBack();
            } else {
               this.tickSwitchBack();
            }
         } else {
            this.requestSwitchBack();
            ItemStack stack = MC.player.getItemInHand(hand);
            if (this.isPlaceableBlock(stack)) {
               BlockPos base = this.predictedSupportCell();
               if (base != null) {
                  BlockPos step = this.inputStep();
                  int ahead = step == null ? 0 : Math.max(0, Math.min(5, this.integer("rage-blocks")));
                  if (PerTickPlacementLock.init(this.id())) {
                     for (BlockPos cell : cellsAhead(base, step, ahead, this::open)) {
                        this.canPlaceInCell(cell, hand, stack);
                     }
                  }
               }
            }
         }
      }
   }

   static List<BlockPos> cellsAhead(BlockPos base, BlockPos step, int ahead, Predicate<BlockPos> solid) {
      List<BlockPos> cells = new ArrayList<>();
      if (base == null) {
         return cells;
      }

      if (step == null) {
         cells.add(base);
         return cells;
      }

      boolean diagonal = step.getX() != 0 && step.getZ() != 0;

      for (int i = 0; i <= Math.max(0, ahead); i++) {
         BlockPos lane = base.offset(step.getX() * i, 0, step.getZ() * i);
         cells.add(lane);
         if (diagonal && i != 0) {
            BlockPos alongX = lane.offset(-step.getX(), 0, 0);
            BlockPos alongZ = lane.offset(0, 0, -step.getZ());
            if (!solid.test(alongX) && !solid.test(alongZ)) {
               cells.add(alongX);
            }
         }
      }

      return cells;
   }

   private BlockPos predictedSupportCell() {
      Vec3 vec = MC.player.position().add(MC.player.getDeltaMovement()).add(0.0, -0.75, 0.0);
      BlockPos cell = BlockPos.containing(vec.x, vec.y, vec.z);
      int footY = MC.player.blockPosition().getY();
      if (cell.getY() >= footY) {
         cell = new BlockPos(cell.getX(), footY - 1, cell.getZ());
      }

      return MC.level.isOutsideBuildHeight(cell) ? null : cell;
   }

   private BlockPos inputStep() {
      if (MC.options == null) {
         return null;
      }

      Vec3 look = Vec3.directionFromRotation(0.0F, MC.player.getYRot());
      double x = 0.0;
      double z = 0.0;
      if (isKeyDown(MC.options.keyUp)) {
         x += look.x;
         z += look.z;
      }

      if (isKeyDown(MC.options.keyDown)) {
         x -= look.x;
         z -= look.z;
      }

      if (isKeyDown(MC.options.keyLeft)) {
         x += look.z;
         z -= look.x;
      }

      if (isKeyDown(MC.options.keyRight)) {
         x -= look.z;
         z += look.x;
      }

      return snapStepToOctant(x, z);
   }

   static BlockPos snapStepToOctant(double x, double z) {
      if (x * x + z * z <= 1.0E-8) {
         return null;
      }

      double yaw = Math.toRadians(octantToYaw(yawToOctant((float)Math.toDegrees(Math.atan2(-x, z)))));
      int stepX = (int)Math.round(-Math.sin(yaw));
      int stepZ = (int)Math.round(Math.cos(yaw));
      return stepX == 0 && stepZ == 0 ? null : new BlockPos(stepX, 0, stepZ);
   }

   private boolean canPlaceInCell(BlockPos cell, InteractionHand hand, ItemStack stack) {
      if (cell != null && !MC.level.isOutsideBuildHeight(cell)) {
         BlockState state = MC.level.getBlockState(cell);
         if (state.canBeReplaced() && !this.hasCentreSupport(state, cell)) {
            if (!canCatchOn(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), cell)) {
               return false;
            }

            Direction side = this.bestPlaceFace(cell);
            if (side == null) {
               return false;
            }

            BlockPos neighbour = cell.relative(side);
            Vec3 hitPos = faceCentre(supportBox(neighbour), side.getOpposite());
            Vec3 eye = MC.player.getEyePosition();
            double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
            if (eye.distanceToSqr(hitPos) > reach * reach) {
               return false;
            }

            BlockHitResult hit = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
            if (al.init_Z_HitResult(hit, hand, this.id())) {
               return false;
            }

            ScaffoldModule.PlannedPlacement target = new ScaffoldModule.PlannedPlacement(neighbour, cell, side.getOpposite(), hit, RotationMath.lookAt(hitPos, eye), cell.getY());
            return this.placeTarget(target, hand, stack);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private Direction bestPlaceFace(BlockPos cell) {
      Vec3 look = Vec3.atCenterOf(cell).subtract(MC.player.getEyePosition());
      double bestRelevancy = -1.7976931348623157E308;
      Direction best = null;

      for (Direction side : Direction.values()) {
         BlockPos neighbour = cell.relative(side);
         if (!MC.level.isOutsideBuildHeight(neighbour)) {
            BlockState state = MC.level.getBlockState(neighbour);
            if (!state.canBeReplaced() && state.getFluidState().isEmpty()) {
               double relevancy = side.getAxis().choose(look.x, look.y, look.z) * side.getAxisDirection().getStep();
               if (relevancy > bestRelevancy) {
                  bestRelevancy = relevancy;
                  best = side;
               }
            }
         }
      }

      return best;
   }

   private void tickPlacement() {
      if (!this.canOperate()) {
         this.courseLine = null;
         this.edgeGateActive = false;
         this.lockedLine = null;
         this.updateIdleAim(false);
         this.tickSwitchBack();
      } else {
         this.clickLanded = false;
         this.f_h_Z = false;
         this.f_i_Z = false;
         this.paceUsedMean = false;
         if (this.isPlacementInFlight()) {
            this.placeStatus = "attempt-" + this.placementState.name().toLowerCase(Locale.ROOT);
            this.captureLiveTrace("real-wait", this.pendingPlacement);
            if (this.pendingPlacement != null) {
               this.rotateTowards(this.init_ce_DataUtil_aI_TableIndex(this.pendingPlacement));
            } else {
               this.preAim();
            }
         } else if (BlinkPacketManager.isHoldingActionsOnly()) {
            this.placeStatus = "blink-action-only";
            this.captureLiveTrace("real-wait", this.pendingPlacement);
            this.tickSwitchBack();
         } else {
            this.close_BlockPos();
            ItemStack planningStack = this.placeableStack();
            ScaffoldModule.PlannedPlacement target = this.init_aI_TableIndex_ItemStack(planningStack);
            if (target == null) {
               this.f_ai_I++;
               this.write_V_aI_TableIndex(null);
               this.placeStatus = "plan";
               this.captureLiveTrace("no-target " + this.planFailure, null);
               this.preAim();
               this.tickSwitchBack();
            } else {
               this.slotSwitched = false;
               InteractionHand hand = this.placeHand();
               if (hand == null) {
                  if (this.slotSwitched) {
                     this.requestSwitchBack();
                  } else {
                     this.tickSwitchBack();
                  }

                  this.write_V_aI_TableIndex(target);
                  this.placeStatus = "hand";
                  this.captureLiveTrace("hand-switch", target);
                  this.rotateTowards(target.rotation());
               } else {
                  this.requestSwitchBack();
                  ItemStack stack = MC.player.getItemInHand(hand);
                  target = this.init_aI_TableIndex_ItemStack(stack);
                  if (target == null) {
                     this.f_ai_I++;
                     this.write_V_aI_TableIndex(null);
                     this.placeStatus = "plan";
                     this.captureLiveTrace("no-target-2 " + this.planFailure, null);
                     this.preAim();
                  } else {
                     this.f_ai_I = 0;
                     if (this.load_Z_aI_TableIndex(target)) {
                        this.stickyPlacement = null;
                        this.write_V_aI_TableIndex(null);
                        boolean droppedTopHasASide = false;
                        if (target.face() == Direction.UP) {
                           ScaffoldModule.PlannedPlacement viaSide = this.init_aI_TableIndex_BlockPos(
                              target.load(),
                              target.support(),
                              this.predictedPosition(this.courseLine),
                              stack,
                              this.courseLine == null ? null : this.courseLine.load()
                           );
                           if (viaSide != null && !this.load_Z_aI_TableIndex(viaSide)) {
                              target = viaSide;
                              this.f_bt_I++;
                              droppedTopHasASide = true;
                           }
                        }

                        if (!droppedTopHasASide) {
                           if (!isGroundJump(MC.player.onGround(), this.wantsJump())) {
                              this.placeStatus = "drop-" + this.dropReason;
                              this.captureLiveTrace("rise-drop", target);
                              this.rotateTowards(this.placeAimRotation());
                              return;
                           }

                           this.rememberRecentCell(target.load());
                           ScaffoldModule.PlannedPlacement replanned = this.init_aI_TableIndex_ItemStack(stack);
                           if (replanned == null || this.load_Z_aI_TableIndex(replanned)) {
                              this.placeStatus = "drop-" + this.dropReason;
                              this.captureLiveTrace("rise-drop", target);
                              this.rotateTowards(this.placeAimRotation());
                              return;
                           }

                           target = replanned;
                           this.f_bt_I++;
                        }
                     }

                     this.write_V_aI_TableIndex(target);
                     this.placePitch = target.rotation().pitch();
                     ScaffoldModule.WireClickRotation wireClick = this.wireClickRotation();
                     RotationMath.Rotation clickRotation = wireClick == null ? null : wireClick.load();
                     boolean rayLands = clickRotation != null && this.load_Z_aI_TableIndex_ce_DataUtil(target, clickRotation);
                     ScaffoldModule.PlannedPlacement laneRay = null;
                     if (clickRotation != null && !rayLands) {
                        laneRay = this.init_aI_TableIndex_aI_TableIndex_BlockHitResult(target, this.aimRay);
                        if (laneRay != null && this.load_Z_aI_TableIndex(laneRay)) {
                           laneRay = null;
                        }
                     }

                     ScaffoldModule.PlannedPlacement clickable = rayLands ? target : laneRay;
                     boolean paceFreesPitch = false;
                     if (clickable != null && this.checkPace(clickable, clickRotation)) {
                        paceFreesPitch = true;
                        this.f_aV_I = AutismSessionState.getInstance().getClientTick();
                     }

                     this.rotateTowards(this.init_ce_DataUtil_aI_TableIndex(target), load_Z_Z_Z(rayLands || laneRay != null, paceFreesPitch));
                     if (wireClick == null) {
                        this.placeStatus = "wire-wait";
                        this.captureLiveTrace("aim-hold miss=no-wire", target);
                     } else {
                        if (!rayLands) {
                           if (laneRay == null) {
                              if (this.pop(target)) {
                                 this.f_ai_I = Math.max(this.f_ai_I, 1);
                              }

                              this.trackTargetStreak(target, clickRotation);
                              String waitCode = this.waitCode(target);
                              this.trackWaitFace(target, waitCode);
                              this.placeStatus = "aim-" + waitCode;
                              this.captureLiveTrace("aim-hold" + this.clickDebug(target), target);
                              return;
                           }

                           target = laneRay;
                           this.write_V_aI_TableIndex(target);
                        }

                        if (clickRotation != null) {
                           float emittedPitch = clickRotation.pitch();
                           if (!isBelowPitchLimit(emittedPitch)) {
                              this.placeStatus = "pveto-" + (int)emittedPitch;
                              this.captureLiveTrace("pitch-veto", target);
                              return;
                           }

                           if (this.init_Z_ce_DataUtil(clickRotation)) {
                              if (++this.f_bY_I > 2) {
                                 this.rememberRecentCell(target.load());
                                 this.stickyPlacement = null;
                                 this.f_bY_I = 0;
                              }

                              float yawResidual = this.laneOctant() == -1 ? 0.0F / 0.0F : octantResidual(this.laneYaw(), clickRotation.yaw());
                              this.placeStatus = "yawoff-" + (Float.isNaN(yawResidual) ? "?" : String.format(Locale.ROOT, "%.1f", yawResidual));
                              this.captureLiveTrace("yaw-veto", target);
                              return;
                           }

                           this.f_bY_I = 0;
                           float goalErr = Math.abs(Mth.wrapDegrees(target.rotation().yaw() - clickRotation.yaw()));
                           if (target.face().getAxis().isHorizontal() && isYawFarFromEmitted(target.rotation().yaw(), clickRotation.yaw())) {
                              boolean closing = isErrorImproving(goalErr, this.f_m_F);
                              this.f_m_F = goalErr;
                              this.f_bZ_I = closing ? 0 : this.f_bZ_I + 1;
                              if (this.f_bZ_I > 2) {
                                 this.rememberRecentCell(target.load());
                                 this.stickyPlacement = null;
                                 this.f_bZ_I = 0;
                                 this.f_m_F = 0.0F / 0.0F;
                              }

                              this.placeStatus = "goaloff-" + (int)goalErr;
                              this.captureLiveTrace("yaw-veto", target);
                              return;
                           }

                           this.f_bZ_I = 0;
                           this.f_m_F = 0.0F / 0.0F;
                           float clickPitchStep = wireClick.init_ce_DataUtil() == null
                              ? 0.0F
                              : Math.abs(clickRotation.pitch() - wireClick.init_ce_DataUtil().pitch());
                           boolean descending = !MC.player.onGround() && MC.player.getDeltaMovement().y < 0.0;
                           if (!this.block_Z() && !descending && clickPitchStep > 10.0F) {
                              this.placeStatus = "pstep-" + (int)clickPitchStep;
                              this.captureLiveTrace("pitch-step", target);
                              return;
                           }
                        }

                        this.f_ag_I = 0;
                        this.f_ah_I = 0;
                        this.f_ar_I = 0;
                        int tick = AutismSessionState.getInstance().getClientTick();
                        if (!this.block_Z() && tick == this.lastPlaceTick) {
                           this.placeStatus = "sametick";
                           this.captureLiveTrace("pace-hold", target);
                        } else {
                           boolean pitchFreed = this.f_aV_I == tick;
                           if (paceFreesPitch || pitchFreed || this.checkPace(target, clickRotation)) {
                              if (target.load().getY() > target.support().getY() || this.isReservedCell(target.load())) {
                                 this.tick_BlockPos = target.load().immutable();
                                 this.f_ak_I = AutismSessionState.getInstance().getClientTick();
                              }

                              this.placeStatus = !paceFreesPitch && pitchFreed
                                 ? "pace-pfree"
                                 : "pace-" + this.paceElapsedMs + (this.paceUsedMean ? "/imean" : "/" + this.paceWaitMs + "ms");
                              this.captureLiveTrace("pace-hold", target);
                           } else if (BlinkPacketManager.isHoldingActionsOnly()) {
                              this.placeStatus = "blink-action-only";
                              this.captureLiveTrace("real-wait", target);
                           } else {
                              Vec3 publishedEye = lastPublishedEye;
                              double publishedReach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
                              BlockHitResult publishedRay = publishedEye == null
                                 ? null
                                 : raycast(publishedEye, clickRotation, publishedReach, MC.level, MC.player);
                              if (publishedRay != null
                                 && this.aimRay != null
                                 && publishedRay.getBlockPos().equals(this.aimRay.getBlockPos())
                                 && publishedRay.getDirection() == this.aimRay.getDirection()) {
                                 this.pendingPlacement = target;
                                 this.pendingLine = this.courseLine;
                                 this.pendingFallOff = this.fallOffPoint(this.courseLine);
                                 this.pendingTick = tick;
                                 this.placementState = ScaffoldModule.PlacementState.ARMED;
                                 this.generationCounter = incrementNonNegative(this.generationCounter);
                                 this.activeGeneration = this.generationCounter;
                                 this.pendingHand = hand;
                                 if (this.isTellyOrEagleMode()) {
                                    this.useSlot = MC.player.getInventory().getSelectedSlot();
                                    this.trackedSlotCount(this.useSlot);
                                 }

                                 this.buildsPlannedCell = this.hitPlacesPending(this.aimRay, target);
                                 this.submittedCount = 0;
                                 this.duplicateSubmitted = false;
                                 this.f_aL_I = 0;
                                 this.clickRotation = clickRotation;
                                 this.submitRotation = wireClick.init_ce_DataUtil();
                                 this.pendingSequence = -1;
                                 this.resultSeen = false;
                                 this.resultConsumed = false;
                                 this.clickWritten = false;
                                 this.pendingStatus = "queued";
                                 this.lastPlaceTick = this.pendingTick;
                                 this.writtenClickNanos = -9223372036854775808L;
                                 this.paceJitterMs = this.random.nextInt(25);
                                 this.clickLanded = true;
                                 this.placeStatus = "ok";
                                 autismclient.util.VirtualInputDispatcher.requestScaffold(this.activeGeneration);
                                 ((AutismMinecraftAccessor)MC).autism$setRightClickDelay(0);
                                 this.captureLiveTrace("PLACE", target);
                              } else {
                                 this.placeStatus = "wire-pose";
                                 this.captureLiveTrace("real-wait", target);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void reconcilePending() {
      this.drainWrittenClicks();
      this.reconcileAcknowledgedPredictions();
      ScaffoldModule.PlannedPlacement pending = this.pendingPlacement;
      if (pending != null && MC.level != null && MC.player != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         int age = Math.max(0, tick - this.pendingTick);
         BlockPos cell = pending.load();
         boolean solid = this.hasCentreSupport(MC.level.getBlockState(cell), cell);
         ScaffoldModule.StateDecision decision = decidePlacementState(
            this.placementState, age, this.resultSeen, this.resultConsumed, solid, isAcknowledged(this.pendingSequence, this.ackedSequence)
         );
         if (decision.state() == ScaffoldModule.PlacementState.FAILED) {
            String reason = "use".equals(decision.reason()) ? "use-" + this.pendingStatus : decision.reason();
            this.init_V_Str_3(reason);
         } else {
            this.placementState = decision.state();
         }

         if (this.placementState == ScaffoldModule.PlacementState.RECONCILING) {
            this.placeStatus = "reconcile";
            this.captureLiveTrace("real-wait", pending);
         } else if (this.placementState == ScaffoldModule.PlacementState.PREDICTED && solid) {
            this.predictedCells.remove(cell);
            this.notePlaced(cell, this.pendingLine, this.pendingFallOff);
            this.lastAcknowledgedCell = cell;
            if (!MC.player.onGround()) {
               this.f_af_I++;
               if (!cell.equals(this.reservedStep)) {
                  this.airborneBuiltRow = Math.max(this.airborneBuiltRow, cell.getY());
               }
            }

            this.recordPrediction(this.pendingSequence, cell);
            PlacedBlockTrailRenderer.init(cell);
            this.placeStatus = "ok";
            this.captureLiveTrace("settle-ok", pending);
            this.resetPlacementState();
         } else if (this.placementState == ScaffoldModule.PlacementState.FAILED) {
            this.placeStatus = this.pendingStatus;
            this.captureLiveTrace("real-miss", pending);
            this.resetPlacementState();
         }
      } else if (pending == null && this.placementState != ScaffoldModule.PlacementState.IDLE) {
         this.resetPlacementState();
      }
   }

   private boolean isPlacementInFlight() {
      return isInFlightState(this.placementState);
   }

   private void init_V_Str_3(String reason) {
      if (this.pendingPlacement != null && MC != null && MC.level != null) {
         BlockPos cell = this.pendingPlacement.load();
         boolean predicted = this.level_Z() ? isSolid(MC.level.getBlockState(cell), MC.level, cell, CollisionContext.of(MC.player)) : this.open(cell);
         if (predicted) {
            this.trackHighestSequence(cell, this.pendingSequence);
         }
      }

      this.placementState = ScaffoldModule.PlacementState.FAILED;
      this.pendingStatus = reason == null ? "failed" : reason;
      if (this.stickyPlacement != null && this.pendingPlacement != null && this.stickyPlacement.load().equals(this.pendingPlacement.load())) {
         this.stickyPlacement = null;
      }
   }

   private void resetPlacementState() {
      this.pendingPlacement = null;
      this.pendingLine = null;
      this.pendingFallOff = null;
      this.pendingTick = -2147483648;
      this.placementState = ScaffoldModule.PlacementState.IDLE;
      this.activeGeneration = 0L;
      this.pendingHand = null;
      this.buildsPlannedCell = false;
      this.submittedCount = 0;
      this.duplicateSubmitted = false;
      this.f_aL_I = 0;
      this.clickRotation = null;
      this.submitRotation = null;
      this.pendingSequence = -1;
      this.resultSeen = false;
      this.resultConsumed = false;
      this.clickWritten = false;
      this.pendingStatus = "--";
   }

   private void recordPrediction(int sequence, BlockPos cell) {
      this.recordPrediction(sequence, cell, null);
   }

   private void recordPrediction(int sequence, BlockPos cell, BlockPos supportingCell) {
      if (sequence >= 0 && cell != null && !isAcknowledged(sequence, this.ackedSequence)) {
         this.predictions.addLast(new ScaffoldModule.PendingPlacementSeq(sequence, cell.immutable(), supportingCell == null ? null : supportingCell.immutable()));

         while (this.predictions.size() > 64) {
            this.predictions.removeFirst();
         }
      }
   }

   private void trackHighestSequence(BlockPos cell, int sequence) {
      if (cell != null) {
         int highest = this.predictedCells.merge(cell.immutable(), sequence, (oldSequence, newSequence) -> Math.max(oldSequence, newSequence));
         if (isSequenceAcknowledged(highest, this.ackedSequence)) {
            this.predictedCells.remove(cell);
         }
      }
   }

   private void resetOnLevelChange() {
      ClientLevel level = MC == null ? null : MC.level;
      if (levelChanged(this.trackedLevel, level)) {
         this.trackedLevel = level;
         this.predictions.clear();
         this.predictedCells.clear();
         this.ackedSequence = -2147483648;
         this.reconciledSequence = -2147483648;
         writtenClicks.clear();
         if (this.pendingPlacement != null || this.placementState != ScaffoldModule.PlacementState.IDLE) {
            this.resetPlacementState();
         }

         this.clearReservedCells();
         this.footingRiseTick = -2147483648;
      }
   }

   static boolean levelChanged(Object previous, Object current) {
      return previous != current;
   }

   private void reconcileAcknowledgedPredictions() {
      int acknowledged = this.ackedSequence;
      if (acknowledged != -2147483648 && acknowledged > this.reconciledSequence && MC.level != null) {
         this.reconciledSequence = acknowledged;
         Set<BlockPos> rejected = new HashSet<>();
         Set<BlockPos> confirmed = new HashSet<>();

         for (ScaffoldModule.PendingPlacementSeq predicted : this.predictions) {
            if (isAcknowledged(predicted.sequence(), acknowledged)) {
               BlockState state = MC.level.getBlockState(predicted.cell());
               boolean solid = predicted.load() == null
                  ? this.hasCentreSupport(state, predicted.cell())
                  : isSolid(state, MC.level, predicted.cell(), CollisionContext.of(MC.player));
               if (!solid) {
                  rejected.add(predicted.cell());
                  if (predicted.load() != null) {
                     this.captureFailure("ack-reconciled-nonsupport", predicted.cell(), predicted.sequence());
                  }
               } else {
                  confirmed.add(predicted.cell());
               }
            }
         }

         Set<BlockPos> brokenChain = brokenPredictions(new ArrayList<>(this.predictions), rejected, confirmed);
         Iterator<ScaffoldModule.PendingPlacementSeq> iterator = this.predictions.iterator();

         while (iterator.hasNext()) {
            ScaffoldModule.PendingPlacementSeq predicted = iterator.next();
            if (brokenChain.contains(predicted.cell()) && !isAcknowledged(predicted.sequence(), acknowledged)) {
               this.trackHighestSequence(predicted.cell(), predicted.sequence());
               this.forgetCell(predicted.cell());
            }

            if (isAcknowledged(predicted.sequence(), acknowledged)) {
               iterator.remove();
               this.predictedCells.remove(predicted.cell());
               if (brokenChain.contains(predicted.cell())) {
                  this.load_V_BlockPos(predicted.cell());
                  this.forgetCell(predicted.cell());
               }
            }
         }

         if (this.pendingPlacement != null && brokenChain.contains(this.pendingPlacement.support())) {
            autismclient.util.VirtualInputDispatcher.update_V();
            this.init_V_Str_3("ack-support-air");
         }
      }
   }

   static Set<BlockPos> brokenPredictions(List<ScaffoldModule.PendingPlacementSeq> predictions, Set<BlockPos> rejected) {
      return brokenPredictions(predictions, rejected, Set.of());
   }

   static Set<BlockPos> brokenPredictions(List<ScaffoldModule.PendingPlacementSeq> predictions, Set<BlockPos> rejected, Set<BlockPos> confirmed) {
      Set<BlockPos> broken = new HashSet<>(rejected);

      boolean changed;
      do {
         changed = false;

         for (ScaffoldModule.PendingPlacementSeq predicted : predictions) {
            if (predicted.load() != null && broken.contains(predicted.load()) && !confirmed.contains(predicted.cell())) {
               changed |= broken.add(predicted.cell());
            }
         }
      } while (changed);

      return broken;
   }

   private void forgetCell(BlockPos cell) {
      if (this.livePlacement != null
         && (cell.equals(this.livePlacement.plan().support()) || cell.equals(this.livePlacement.plan().load()))) {
         this.livePlacement = null;
      }

      if (cell.equals(this.close_BlockPos)) {
         this.close_BlockPos = null;
         this.start_Z = false;
      }

      if (cell.equals(this.entryCell)) {
         this.clearEntryCells();
      }

      this.data_Z = false;
   }

   private void pruneAcknowledgedSequences(int acknowledged) {
      Iterator<Entry<BlockPos, Integer>> iterator = this.predictedCells.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<BlockPos, Integer> entry = iterator.next();
         int highestSequence = entry.getValue();
         if (isSequenceAcknowledged(highestSequence, acknowledged)) {
            iterator.remove();
         }
      }
   }

   static boolean isSequenceAcknowledged(int highestSequence, int acknowledged) {
      return highestSequence >= 0 && isAcknowledged(highestSequence, acknowledged);
   }

   private void load_V_BlockPos(BlockPos cell) {
      if (cell != null) {
         this.recentPlaced.removeIf(cell::equals);
         this.recentOffsets.clear();
         if (this.stickyPlacement != null && cell.equals(this.stickyPlacement.load())) {
            this.stickyPlacement = null;
         }

         this.lockedLine = null;
         this.set_BlockPos = null;
         this.f_aU_I = -2147483648;
         this.placeStatus = "ack-air";
      }
   }

   private void trackAirTicks() {
      if (MC.player != null && !MC.player.onGround()) {
         this.airTicks++;
      } else {
         this.airTicks = 0;
         this.f_af_I = 0;
         this.takeoffPosition = MC.player == null ? null : MC.player.position();
      }
   }

   private String carryDebug() {
      if (MC.player != null && !MC.player.onGround() && this.takeoffPosition != null) {
         Vec3 now = MC.player.position();
         double dx = now.x - this.takeoffPosition.x;
         double dz = now.z - this.takeoffPosition.z;
         return String.format(Locale.ROOT, "carry=%.2f/%.2f", Math.sqrt(dx * dx + dz * dz), 0.8);
      } else {
         return "carry=--";
      }
   }

   private RotationMath.Rotation init_ce_DataUtil_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (MC.player != null
         && target.face().getAxis().isHorizontal()
         && this.f_ag_I >= 3
         && this.f_ar_I == 0
         && target.support().equals(this.trackedSupport)
         && target.face() == this.trackedFace) {
         this.aimDecision = "miss-direct";
         RotationMath.Rotation direct = RotationMath.lookAt(target.hit().getLocation(), MC.player.getEyePosition());
         return new RotationMath.Rotation(direct.yaw(), capPitch89(direct.pitch()));
      } else {
         return target.rotation();
      }
   }

   static boolean isHitBetweenEyeAndFace(Vec3 eye, Vec3 hit, BlockPos support, Direction face) {
      double eyePast = distancePastSupportFace(eye, support, face);
      double hitPast = distancePastSupportFace(hit, support, face);
      return eyePast > 0.0 ? hitPast > 0.01 && hitPast < eyePast : hitPast < -0.01 && hitPast > eyePast;
   }

   private void trackTargetStreak(ScaffoldModule.PlannedPlacement target, RotationMath.Rotation clickRotation) {
      if (!target.support().equals(this.trackedSupport) || target.face() != this.trackedFace) {
         this.trackedSupport = target.support();
         this.trackedFace = target.face();
         this.f_ag_I = 0;
         this.f_ar_I = 0;
         this.f_ah_I = 0;
      }

      boolean occluded = this.aimRay != null
         && !this.aimRay.getBlockPos().equals(target.support())
         && this.open(this.aimRay.getBlockPos())
         && MC.player != null
         && isHitBetweenEyeAndFace(MC.player.getEyePosition(), this.aimRay.getLocation(), target.support(), target.face());
      boolean blank = this.aimRay == null;
      boolean aimArrived = clickRotation == null || !isYawFarFromEmitted(target.rotation().yaw(), clickRotation.yaw());
      boolean pastAndBlank = blank && MC.player != null && this.sync(target) && aimArrived;
      if (occluded || pastAndBlank) {
         if (++this.f_ar_I >= 2) {
            this.rememberRecentCell(target.load());
            this.stickyPlacement = null;
            if (target.support().equals(this.pinSupport) && target.face() == this.pinFace) {
               this.pinSupport = null;
               this.pinFace = null;
            }

            this.f_ar_I = 0;
         }
      } else if (!blank) {
         this.f_ar_I = 0;
      }

      if (MC.player == null || !this.sync(target)) {
         this.f_ag_I = 0;
         this.f_ah_I = 0;
      } else if (this.init_Z_aI_TableIndex(target) && this.f_ah_I < 6) {
         this.f_ag_I = 0;
         this.f_ah_I++;
      } else {
         this.f_ag_I++;
         this.f_ah_I = 0;
      }
   }

   private boolean init_Z_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (MC.player != null && !target.face().getAxis().isVertical()) {
         float yaw = target.rotation().yaw();
         Vec3 lead = this.leadMotion();
         if (lead.horizontalDistanceSqr() < 1.0E-4 && this.courseLine != null) {
            lead = new Vec3(this.courseLine.load().x, 0.0, this.courseLine.load().z).normalize().scale(0.05);
         }

         Vec3 eye = MC.player.getEyePosition();

         for (int step = 1; step <= 2; step++) {
            Vec3 projected = eye.add(lead.scale(step));
            if (load_Z_Vec3(projected, target.support(), target.face(), yaw, true)) {
               float solve = requiredPitchAt(projected, target.support(), target.face(), yaw);
               if (Float.isFinite(solve) && solve <= 89.5F) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean isYawFarFromEmitted(float goalYaw, float emittedYaw) {
      return Math.abs(Mth.wrapDegrees(goalYaw - emittedYaw)) > 20.0F;
   }

   private boolean staysBeyondPitchLimit(Vec3 eye, BlockPos support, Direction face, float yaw) {
      Vec3 lead = this.leadMotion();

      for (int step = 0; step <= 2; step++) {
         if (!isPitchBeyondLimit(requiredPitchAt(eye.add(lead.scale(step)), support, face, yaw))) {
            return false;
         }
      }

      return true;
   }

   static boolean init_Z_Vec3_4(Vec3 eye, BlockPos support, Direction face, Predicate<BlockPos> fullCubeAt) {
      if (eye == null || support == null || face == null || fullCubeAt == null) {
         return false;
      } else if (!face.getAxis().isHorizontal()) {
         return false;
      } else if (Mth.floor(eye.x) == support.getX() && Mth.floor(eye.z) == support.getZ()) {
         BlockPos lid = support.above();
         return eye.y >= lid.getY() + 1.0 && fullCubeAt.test(lid);
      } else {
         return false;
      }
   }

   private boolean init_Z_BlockPos_Direction(BlockPos support, Direction face) {
      if (MC.player != null && MC.level != null) {
         Vec3 eye = MC.player.getEyePosition();
         Vec3 lead = this.leadMotion();
         int horizon = Math.max(2, leadTicks(this.paceWaitTicks));

         for (int step = 0; step <= horizon; step++) {
            if (!init_Z_Vec3_4(eye.add(lead.scale(step)), support, face, this::isFullCube)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isFullCube(BlockPos pos) {
      return MC.level != null && pos != null ? MC.level.getBlockState(pos).isCollisionShapeFullBlock(MC.level, pos) : false;
   }

   static float requiredPitchAt(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return requiredPitch(eye, supportBox(support), face, yaw);
   }

   static float requiredPitch(Vec3 eye, AABB support, Direction face, float yaw) {
      if (support == null) {
         return 0.0F / 0.0F;
      }

      double past = distancePastFace(eye, support, face);
      if (past <= 0.0) {
         return 0.0F / 0.0F;
      }

      double yawRad = Math.toRadians(yaw);
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      double toward = -(lookX * face.getStepX() + lookZ * face.getStepZ());
      if (toward <= 0.1) {
         return 0.0F / 0.0F;
      }

      double run = past / toward;
      double drop = eye.y - supportMidY(support);
      return (float)Math.toDegrees(Math.atan2(drop, run));
   }

   static boolean rayMatchesPlan(BlockPos plannedCell, Direction plannedFace, BlockPos rayCell, Direction rayFace) {
      return rayMatchesPlan(plannedCell, plannedFace, null, rayCell, rayFace, null);
   }

   static boolean rayMatchesPlan(
      BlockPos plannedCell, Direction plannedFace, BlockPos plannedSupport, BlockPos rayCell, Direction rayFace, BlockPos raySupport
   ) {
      return rayCell != null && rayCell.equals(plannedCell) && rayFace == plannedFace
         ? plannedSupport == null || raySupport == null || plannedSupport.equals(raySupport)
         : false;
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_aI_TableIndex_BlockHitResult(ScaffoldModule.PlannedPlacement planned, BlockHitResult ray) {
      if (MC.player != null && MC.level != null && this.activeRotation != null && ray != null) {
         BlockPos support = ray.getBlockPos().immutable();
         Direction face = ray.getDirection();
         BlockPos cell = this.clickedPlacePos(ray, this.placeableStack());
         if (cell == null) {
            return null;
         } else if (rayMatchesPlan(planned.load(), planned.face(), planned.support(), cell, face, support)) {
            return null;
         } else if (!MC.level.getBlockState(cell).canBeReplaced()) {
            return null;
         } else if (!canCatchOn(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), cell, this.push(cell))) {
            return null;
         } else if (!this.read_Z_BlockPos_2(cell)) {
            return null;
         } else if (!init_Z_BlockPos_4(cell, this.footingRow(), this.reservedSupport, this.courseLine == null ? this.footingRowCell() : null)) {
            return null;
         } else {
            return this.isDirectlyBelowFoot(planned.load()) && !this.isDirectlyBelowFoot(cell)
               ? null
               : new ScaffoldModule.PlannedPlacement(support, cell, face, ray, this.activeRotation, cell.getY());
         }
      } else {
         return null;
      }
   }

   static boolean init_Z_BlockPos_4(BlockPos cell, int oracleRow, BlockPos reservedSupport, BlockPos ownSupport) {
      return cell.getY() <= oracleRow ? true : sameColumn(cell, reservedSupport) || sameColumn(cell, ownSupport);
   }

   private boolean isDirectlyBelowFoot(BlockPos cell) {
      return MC.player != null && isDirectlyBelow(cell, this.footCell());
   }

   private BlockPos footCell() {
      int row = this.footingRow();
      return row == -2147483648 ? this.close_BlockPos() : this.footCellAtRow(row);
   }

   private String waitCode(ScaffoldModule.PlannedPlacement target) {
      this.init_V_aI_TableIndex(target);
      if (this.aimRay == null) {
         return this.sync(target) ? "blank" : "plane";
      } else if (!this.aimRay.getBlockPos().equals(target.support())) {
         return "occ";
      } else if (this.aimRay.getDirection() != target.face()) {
         return this.aimRay.getDirection() == Direction.UP ? "u" : "side";
      } else {
         return this.pop(target) ? "reach" : "near";
      }
   }

   private void trackWaitFace(ScaffoldModule.PlannedPlacement target, String waitCode) {
      this.waitFace = null;
      if (MC.player != null
         && target != null
         && ("plane".equals(waitCode) || "u".equals(waitCode))
         && target.face().getAxis().isHorizontal()
         && !this.sync(target)) {
         this.waitFace = target.face();
         this.waitFaceTick = AutismSessionState.getInstance().getClientTick();
      }
   }

   static boolean isStarvedByWaitFace(Direction waitFace, int waitAge, boolean rising, Vec3 lane) {
      return waitFace != null && waitAge == 0 && rising && lane != null ? waitFace.getStepX() * lane.x + waitFace.getStepZ() * lane.z > 0.0 : false;
   }

   static boolean isStillStarved(boolean starves, double spentBlocks) {
      return starves && spentBlocks <= 0.35;
   }

   private boolean isStarved(boolean rising, Vec3 lane) {
      int tick = AutismSessionState.getInstance().getClientTick();
      boolean starves = isStarvedByWaitFace(this.waitFace, ticksSince(tick, this.waitFaceTick), rising, lane);
      if (tick != this.starveTick) {
         this.starveTick = tick;
         this.starvedTravel = starves ? this.starvedTravel + this.laneSpeed(lane) : 0.0;
      }

      return isStillStarved(starves, this.starvedTravel);
   }

   private double laneSpeed(Vec3 lane) {
      if (MC.player != null && lane != null) {
         Vec3 velocity = MC.player.getDeltaMovement();
         return Math.abs(velocity.x * lane.x + velocity.z * lane.z);
      } else {
         return 0.0;
      }
   }

   static boolean isDirectlyBelow(BlockPos cell, BlockPos foot) {
      return cell != null && foot != null && cell.getY() == foot.getY() - 1 && cell.getX() == foot.getX() && cell.getZ() == foot.getZ();
   }

   private boolean read_Z_BlockPos_2(BlockPos cell) {
      if (this.courseLine == null) {
         return false;
      }

      Vec3 direction = this.courseLine.load();
      return init_Z_BlockPos_5(cell, this.footCell(), axisSign(direction.x), axisSign(direction.z));
   }

   static boolean init_Z_BlockPos_5(BlockPos cell, BlockPos foot, int dx, int dz) {
      if (dx == 0 && dz == 0) {
         return false;
      }

      if (cell.getY() != foot.getY() - 1 && cell.getY() != foot.getY()) {
         return false;
      }

      int rx = foot.getX() - cell.getX();
      int rz = foot.getZ() - cell.getZ();
      return (rx == 0 || rx == dx) && (rz == 0 || rz == dz);
   }

   private boolean load_Z_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (target != null && MC.player != null && target.face().getAxis().isVertical()) {
         boolean stairRiser = target.face() == Direction.UP && !MC.player.onGround() && target.load().getY() > this.footingRow();
         Vec3 dropCarry = MC.player.onGround() ? Vec3.ZERO : MC.player.getDeltaMovement();
         boolean own = stairRiser && sameColumn(target.load(), this.footingRowCell());
         boolean drop = stairRiser
            ? (
               own
                  ? !overlapsColumn(MC.player.position(), target.load())
                  : !this.landsOnCellOnLane(MC.player.position(), MC.player.getDeltaMovement(), target.load())
            )
            : !write(MC.player.position(), dropCarry, target.load());
         if (drop) {
            this.dropReason = stairRiser ? (own ? "own" : "arc") : "foot";
         }

         return drop;
      } else {
         return false;
      }
   }

   static boolean isGroundJump(boolean onGround, boolean jumpHeld) {
      return onGround && jumpHeld;
   }

   static int ticksSince(int tick, int lastTick) {
      if (lastTick == -2147483648) {
         return -1;
      }

      int since = tick - lastTick;
      return since < 0 ? -1 : since;
   }

   private boolean checkPace(ScaffoldModule.PlannedPlacement target, RotationMath.Rotation clickRotation) {
      if (MC.player == null) {
         return false;
      }

      long nowNanos = System.nanoTime();
      long elapsed = this.msSinceLastPlace(nowNanos);
      this.paceElapsedMs = elapsed;
      if (this.block_Z()) {
         this.paceWaitTicks = 0;
         this.paceWaitMs = 0L;
         return false;
      }

      if (MC.player.onGround() && !this.f_Z()) {
         long burstWait = burstWaitMs(nowNanos, this.placeTimes.stream().mapToLong(Long::longValue).toArray());
         if (burstWait > 0L) {
            this.paceWaitTicks = (int)((burstWait + 49L) / 50L);
            this.paceWaitMs = elapsed + burstWait;
            return true;
         }
      }

      boolean airborne = !MC.player.onGround();
      boolean descending = airborne && MC.player.getDeltaMovement().y < 0.0;
      boolean chainCell = airborne && this.isReservedCell(target.load());
      boolean footCatch = this.push(target.load());
      boolean fallingCatch = footCatch && MC.player.fallDistance >= 1.0;
      if (!this.write_Z_aI_TableIndex(target) && (!chainCell || !descending) && (!chainCell || !this.update_Z_aI_TableIndex(target)) && !fallingCatch) {
         if (init_Z_BlockPos_3(target.load(), target.support(), this.upFaceSwapCell)) {
            boolean landingRiser = !MC.player.onGround() && MC.player.getDeltaMovement().y < 0.0 && target.load().getY() > target.support().getY();
            long floor = landingRiser ? 50L : 100L;
            this.paceWaitMs = floor;
            return isBelowFloor(elapsed, floor);
         }

         if (chainCell || footCatch) {
            this.f_h_Z = true;
            this.paceWaitMs = 100L;
            return isBelowFloor(elapsed, 100L);
         }

         if (this.flush(target.load())) {
            this.f_h_Z = true;
            this.paceWaitMs = 100L;
            return isBelowFloor(elapsed, 100L);
         }

         if (!this.read_Z_aI_TableIndex(target)) {
            List<BlockPos> booked = new ArrayList<>(this.placedStamps.size() + 1);

            for (ScaffoldModule.PlacedBlockStamp sample : this.placedStamps) {
               booked.add(sample.cell());
            }

            booked.add(target.load());
            long limit = init_J_Z(
               isSingleAxisRow(booked), isNearCardinalYaw(clickRotation == null ? MC.player.getYRot() : clickRotation.yaw()), this.sneakedRecently(), this.recentlyJumped()
            );
            long floor = withMargin(limit) + this.paceJitterMs;
            this.paceWaitTicks = ticksUntilFloor(elapsed, floor);
            this.paceWaitMs = floor;
            if (isBelowFloor(elapsed, floor)) {
               return true;
            }

            if (!this.placedStamps.isEmpty() && this.lastPlaceNanos != -9223372036854775808L) {
               long candidate = elapsed;
               long[] recent = new long[this.placedStamps.size()];
               int index = 0;

               for (ScaffoldModule.PlacedBlockStamp sample : this.placedStamps) {
                  recent[index++] = sample.nanos();
               }

               double mean = rollingMeanWithCandidate(recent, candidate, 8);
               this.paceWaitMs = 0L;
               return mean < limit * 1.08 ? true : this.init_Z_aI_TableIndex_2(target, clickRotation, nowNanos);
            } else {
               return this.init_Z_aI_TableIndex_2(target, clickRotation, nowNanos);
            }
         } else if (this.hasNoFooting()) {
            this.f_i_Z = true;
            this.paceWaitMs = 50L;
            return isBelowFloor(elapsed, 50L);
         } else {
            this.f_h_Z = true;
            this.paceWaitMs = 100L;
            return isBelowFloor(elapsed, 100L);
         }
      } else {
         this.f_i_Z = true;
         this.paceWaitMs = 50L;
         return isBelowFloor(elapsed, 50L);
      }
   }

   private boolean init_Z_aI_TableIndex_2(ScaffoldModule.PlannedPlacement target, RotationMath.Rotation clickRotation, long nowNanos) {
      if (MC.player == null || !MC.player.onGround()) {
         return false;
      }

      if (!isHorizontalFace(target.face())) {
         return false;
      }

      if (!this.placeGaps.isEmpty() && this.lastGapNanos != -9223372036854775808L) {
         long candidate = this.msSinceLastGap(nowNanos);
         if (candidate < 0L) {
            return false;
         }

         if (candidate >= 1000L) {
            return false;
         }

         float pitch = clickRotation == null ? MC.player.getXRot() : clickRotation.pitch();
         List<BlockPos> cells = new ArrayList<>(this.sync_ArrayDeque.size() + 1);
         if (candidate < 5000L) {
            cells.addAll(this.sync_ArrayDeque);
         }

         while (cells.size() > 4) {
            cells.remove(0);
         }

         cells.add(target.load());
         if (!init_Z_F_2(pitch, this.lastWirePitch, candidate, isStraightLine(cells))) {
            return false;
         }

         long[] recent = new long[this.placeGaps.size()];
         int index = 0;

         for (long gap : this.placeGaps) {
            recent[index++] = gap;
         }

         if (rollingMeanWithCandidate(recent, candidate, 8) >= 432.0) {
            return false;
         }

         this.paceElapsedMs = candidate;
         this.paceUsedMean = true;
         return true;
      } else {
         return false;
      }
   }

   static long withMargin(long limitMs) {
      return Math.round(limitMs * 1.08);
   }

   private long msSinceLastPlace(long nowNanos) {
      return elapsedMs(nowNanos, this.lastPlaceNanos);
   }

   static long elapsedMs(long nowNanos, long sinceNanos) {
      if (sinceNanos == -9223372036854775808L) {
         return -1L;
      } else {
         return nowNanos <= sinceNanos ? 0L : (nowNanos - sinceNanos) / 1000000L;
      }
   }

   static long monotonicNanos(long previousNanos, long observedNanos) {
      return previousNanos == -9223372036854775808L ? observedNanos : Math.max(previousNanos, observedNanos);
   }

   static boolean isBelowFloor(long elapsedMs, long floorMs) {
      return elapsedMs >= 0L && elapsedMs < floorMs - 3L;
   }

   static boolean load_Z_Z_Z(boolean rayClickable, boolean paceRefused) {
      return rayClickable && !paceRefused;
   }

   static long init_J_Z(boolean diagonal) {
      return diagonal ? 250L : 350L;
   }

   static long init_J_J_Z(long exemptionMs, boolean diagonal) {
      return Math.max(exemptionMs, init_J_Z(diagonal));
   }

   static int ticksUntilFloor(long elapsedMs, long floorMs) {
      return elapsedMs >= 0L && elapsedMs < floorMs ? (int)Math.ceil((floorMs - elapsedMs) / 50.0) : 0;
   }

   private int supportRow() {
      if (MC.player == null) {
         return -2147483648;
      } else {
         return MC.player.onGround() ? this.footingRow() : this.airborneFloorRow();
      }
   }

   private boolean read_Z_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (MC.player != null && MC.level != null) {
         int row = this.supportRow();
         if (row == -2147483648) {
            return false;
         } else {
            return !isDirectlyBelow(target.load(), this.footCellAtRow(row))
               ? false
               : this.footingOverlapAt(MC.player.position(), row) <= init_D_Z(MC.player.onGround());
         }
      } else {
         return false;
      }
   }

   static double init_D_Z(boolean grounded) {
      return grounded ? 0.3 : 0.12;
   }

   private BlockPos footCellAtRow(int row) {
      Vec3 position = MC.player.position();
      return new BlockPos(Mth.floor(position.x), row + 1, Mth.floor(position.z));
   }

   private boolean write_Z_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (MC.player == null || MC.level == null) {
         return false;
      }

      if (!MC.player.onGround() && !(MC.player.getDeltaMovement().y >= 0.0)) {
         int row = this.supportRow();
         if (row == -2147483648) {
            return false;
         } else {
            Vec3 landing = projectLandingPoint(MC.player.position(), MC.player.getDeltaMovement(), row + 1.0, 12);
            if (landing == null) {
               return false;
            } else {
               return !target.load().equals(BlockPos.containing(landing.x, row + 0.5, landing.z))
                  ? false
                  : this.footingOverlapAt(MC.player.position(), row) <= 0.12;
            }
         }
      } else {
         return false;
      }
   }

   private boolean isReservedCell(BlockPos placed) {
      return this.flush(placed)
         || placed.equals(this.reservedSupport)
         || placed.equals(this.reservedConnector)
         || placed.equals(this.reservedStep)
         || placed.equals(this.reservedRiser);
   }

   private boolean update_Z_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (target.face().getAxis().isVertical()) {
         return false;
      }

      Vec3 eye = MC.player.getEyePosition();
      Vec3 velocity = MC.player.getDeltaMovement();
      float yaw = target.rotation().yaw();

      for (int step = 1; step <= 2; step++) {
         Vec3 projected = eye.add(velocity.scale(step));
         if (!(distancePastSupportFace(projected, target.support(), target.face()) <= 0.0)
            && load_Z_Vec3(projected, target.support(), target.face(), yaw, false)) {
            float solve = requiredPitchAt(projected, target.support(), target.face(), yaw);
            if (Float.isFinite(solve) && solve <= 89.5F) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean init_Z_Z(
      boolean grounded, boolean descending, boolean stampFresh, boolean stampedCell, boolean faceHorizontal, boolean eyePastPlane, boolean windowAlive
   ) {
      return !grounded && descending && stampFresh && !stampedCell && faceHorizontal && eyePastPlane && !windowAlive;
   }

   private void recordPlacement(BlockPos placed, BlockPos against, Direction face, float wirePitch) {
      long observed = this.writtenClickNanos == -9223372036854775808L ? System.nanoTime() : this.writtenClickNanos;
      long now = monotonicNanos(this.lastPlaceNanos, observed);
      this.placeTimes.addLast(now);

      while (this.placeTimes.size() > 4) {
         this.placeTimes.removeFirst();
      }

      long interval = this.lastPlaceNanos == -9223372036854775808L ? 1000L : elapsedMs(now, this.lastPlaceNanos);
      if (placed.getY() != against.getY()) {
         interval += 1000L;
      }

      this.lastPlaceNanos = now;
      this.placedStamps.addLast(new ScaffoldModule.PlacedBlockStamp(interval, placed));

      while (this.placedStamps.size() > 8) {
         this.placedStamps.removeFirst();
      }

      if (isHorizontalFace(face)) {
         this.recordPlaceGap(now, placed, wirePitch);
      }

      this.tick_BlockPos = null;
      this.f_ak_I = -2147483648;
      this.clearHeldFace();
   }

   static boolean isHorizontalFace(Direction face) {
      return face != null && face.getAxis().isHorizontal();
   }

   static long burstWaitMs(long nowNanos, long[] writtenNanos) {
      if (writtenNanos != null && writtenNanos.length >= 4) {
         long oldest = writtenNanos[writtenNanos.length - 4];
         return Math.max(0L, 1050L - elapsedMs(nowNanos, oldest));
      } else {
         return 0L;
      }
   }

   private void recordPlaceGap(long now, BlockPos placed, float wirePitch) {
      long gap = this.msSinceLastGap(now);
      if (gap < 0L) {
         gap = 1000L;
      }

      if (gap >= 5000L) {
         this.sync_ArrayDeque.clear();
      }

      this.lastGapNanos = now;
      this.lastWirePitch = wirePitch;
      this.placeGaps.addLast(gap);

      while (this.placeGaps.size() > 8) {
         this.placeGaps.removeFirst();
      }

      this.sync_ArrayDeque.addLast(placed.immutable());

      while (this.sync_ArrayDeque.size() > 5) {
         this.sync_ArrayDeque.removeFirst();
      }
   }

   private long msSinceLastGap(long nowNanos) {
      long elapsed = elapsedMs(nowNanos, this.lastGapNanos);
      return elapsed < 0L ? -1L : Math.min(1000L, elapsed);
   }

   private boolean recentlyJumped() {
      long elapsed = elapsedMs(System.nanoTime(), this.jumpStartNanos);
      return elapsed >= 0L && elapsed < 750L;
   }

   private void trackJumpStart() {
      if (MC.player != null) {
         boolean onGround = MC.player.onGround();
         if (this.wasOnGround && !onGround && MC.player.getDeltaMovement().y > 0.0) {
            this.jumpStartNanos = System.nanoTime();
            this.recentCells.clear();
         }

         this.wasOnGround = onGround;
      }
   }

   static boolean isStraightLine(List<BlockPos> blocks) {
      int lastX = 0;
      int lastY = 0;
      int lastZ = 0;
      boolean lockedOnX = false;
      boolean lockedOnZ = false;
      boolean first = true;
      int yTolerance = 1;

      for (BlockPos block : blocks) {
         if (!first) {
            if (lastY != block.getY()) {
               if (yTolerance-- <= 0) {
                  return false;
               }
            } else {
               if (lastX == block.getX()) {
                  lockedOnX = true;
               } else if (lockedOnX) {
                  return false;
               }

               if (lastZ == block.getZ()) {
                  lockedOnZ = true;
               } else if (lockedOnZ) {
                  return false;
               }
            }
         }

         lastX = block.getX();
         lastY = block.getY();
         lastZ = block.getZ();
         first = false;
      }

      return lockedOnX || lockedOnZ;
   }

   static boolean init_Z_F_2(float pitch, float lastPitch, long gapMs, boolean oneLine) {
      if (pitch > 85.0F) {
         return true;
      }

      if (oneLine || gapMs >= 800L) {
         return false;
      }

      if (!(pitch > 70.0F)) {
         return false;
      }

      float diff = Math.abs(pitch - lastPitch);
      return diff > 3.0F && diff < 20.0F;
   }

   static boolean load_Z_F_F(float pitch, float lastPitch) {
      float diff = Math.abs(pitch - lastPitch);
      return !(diff > 3.0F) || !(diff < 20.0F);
   }

   static float init_F_F(float goal, float lastPitch, double windowLow, double windowHigh) {
      if (load_Z_F_F(goal, lastPitch)) {
         return goal;
      }

      float bound = Double.isFinite(windowLow) && Double.isFinite(windowHigh) && windowLow <= windowHigh && windowLow > 89.0 ? 89.3F : 89.0F;
      double lo = Double.isFinite(windowLow) ? windowLow : -1.0 / 0.0;
      double hi = Math.min(Double.isFinite(windowHigh) ? windowHigh : 1.0 / 0.0, bound);
      if (lo > hi) {
         return goal;
      }

      float sign = Math.signum(goal - lastPitch);
      float below = (float)Mth.clamp(lastPitch + sign * 2.5F, lo, hi);
      float above = (float)Mth.clamp(lastPitch + sign * 20.5F, lo, hi);
      Float chosen = null;
      if (load_Z_F_F(below, lastPitch)) {
         chosen = below;
      }

      if (load_Z_F_F(above, lastPitch) && (chosen == null || Math.abs(above - goal) < Math.abs(chosen - goal))) {
         chosen = above;
      }

      return chosen == null ? goal : chosen;
   }

   private float init_F_F(float goal, double windowLow, double windowHigh, Direction face, BlockPos placedBlock) {
      if (!this.flags_Z() || !isHorizontalFace(face)) {
         return goal;
      }

      if (!Float.isNaN(this.lastWirePitch) && goal > 70.0F) {
         long gap = this.msSinceLastGap(System.nanoTime());
         if (gap < 0L || gap >= 800L) {
            return goal;
         }

         if (load_Z_F_F(goal, this.lastWirePitch)) {
            return goal;
         }

         List<BlockPos> cells = new ArrayList<>(this.sync_ArrayDeque.size() + 1);
         if (gap < 5000L) {
            cells.addAll(this.sync_ArrayDeque);
         }

         while (cells.size() > 4) {
            cells.remove(0);
         }

         cells.add(placedBlock);
         return !isStraightLine(cells) && !this.pop_Z() ? goal : init_F_F(goal, this.lastWirePitch, windowLow, windowHigh);
      } else {
         return goal;
      }
   }

   static float init_F_F_J(float lastPitch, long gapMs) {
      float park = 84.5F;
      if (!Float.isNaN(lastPitch) && gapMs >= 0L && gapMs < 800L) {
         float bandTop = lastPitch + 2.5F;
         return !(bandTop >= park) && bandTop > 70.0F ? bandTop : park;
      } else {
         return park;
      }
   }

   static float init_F_F(float goal, double windowLow, boolean armable, boolean shallowAhead, boolean parkAllowed, float safePitch) {
      if (!armable || !(goal > safePitch)) {
         return goal;
      } else if (Double.isFinite(windowLow) && windowLow <= safePitch) {
         return safePitch;
      } else {
         return shallowAhead && parkAllowed ? safePitch : goal;
      }
   }

   static boolean init_Z_Vec3(Vec3 eye, Vec3 step, BlockPos support, Direction face, float yaw, float safePitch, int lookaheadTicks) {
      if (step.lengthSqr() < 1.0E-6) {
         return false;
      }

      Vec3 travelled = Vec3.ZERO;
      Vec3 perTick = step;

      for (int k = 1; k <= lookaheadTicks; k++) {
         travelled = travelled.add(perTick);
         perTick = perTick.scale(0.91);
         double[] win = init_DArr_Vec3_2(eye.add(travelled), support, face, yaw);
         if (win != null && win[0] <= safePitch) {
            return true;
         }
      }

      return false;
   }

   private boolean pop_Z() {
      if (this.placeGaps.size() < 7) {
         return true;
      }

      long candidate = this.msSinceLastGap(System.nanoTime());
      if (candidate < 0L) {
         return true;
      }

      long[] recent = new long[this.placeGaps.size()];
      int index = 0;

      for (long gap : this.placeGaps) {
         recent[index++] = gap;
      }

      return rollingMeanWithCandidate(recent, candidate, 8) < 432.0;
   }

   private boolean holdFaceWithin(BlockPos support, Direction face, int maxTicks) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (support.equals(this.heldFaceSupport) && face == this.heldFace) {
         if (tick != this.heldFaceTick) {
            this.heldFaceTicks++;
            this.heldFaceTick = tick;
         }

         return this.heldFaceTicks <= maxTicks;
      } else {
         this.heldFaceSupport = support.immutable();
         this.heldFace = face;
         this.heldFaceTicks = 1;
         this.heldFaceTick = tick;
         return true;
      }
   }

   private boolean isAirborneReservedCell(BlockPos placed) {
      return MC.player != null && !MC.player.onGround() && this.isReservedCell(placed);
   }

   private void clearHeldFace() {
      this.heldFaceSupport = null;
      this.heldFace = null;
      this.heldFaceTicks = 0;
      this.heldFaceTick = -2147483648;
   }

   private float init_F_F(float goal, double windowLow, Direction face, Vec3 probeEye, Vec3 leadStep, float yaw, BlockPos support) {
      if (this.flags_Z() && isHorizontalFace(face)) {
         float safe = init_F_F_J(this.lastWirePitch, this.msSinceLastGap(System.nanoTime()));
         if (goal > safe && this.pop_Z()) {
            boolean grounded = MC.player != null && MC.player.onGround();
            boolean immediate = Double.isFinite(windowLow) && windowLow <= safe;
            if (immediate && support.equals(this.heldFaceSupport) && face == this.heldFace) {
               this.clearHeldFace();
            }

            boolean shallowAhead = !immediate
               && !this.k()
               && !this.isAirborneReservedCell(support.relative(face))
               && init_Z_Vec3(probeEye, leadStep, support, face, yaw, safe, grounded ? 5 : 2);
            boolean parkAllowed = shallowAhead && this.holdFaceWithin(support, face, grounded ? 6 : 3);
            return init_F_F(goal, windowLow, true, shallowAhead, parkAllowed, safe);
         } else {
            return goal;
         }
      } else {
         return goal;
      }
   }

   static boolean isSingleAxisRow(List<BlockPos> booked) {
      if (booked.size() < 2) {
         return true;
      }

      BlockPos first = booked.get(0);
      boolean sameX = true;
      boolean sameZ = true;

      for (BlockPos pos : booked) {
         if (pos.getY() != first.getY()) {
            return false;
         }

         if (pos.getX() != first.getX()) {
            sameX = false;
         }

         if (pos.getZ() != first.getZ()) {
            sameZ = false;
         }
      }

      return sameX || sameZ;
   }

   static boolean isNearCardinalYaw(float emittedYaw) {
      double band = Math.abs(emittedYaw) % 90.0;
      return band < 10.0 || band > 80.0;
   }

   static long init_J_Z(boolean oneLine, boolean banded, boolean sneakedRecently, boolean recentJump) {
      if (oneLine) {
         if (recentJump) {
            return 300L;
         } else if (banded) {
            return sneakedRecently ? 350L : 500L;
         } else {
            return sneakedRecently ? 200L : 350L;
         }
      } else {
         return !banded && sneakedRecently ? 150L : 300L;
      }
   }

   static double rollingMeanWithCandidate(long[] recent, long candidateMs, int capacity) {
      int keep = Math.min(recent.length, capacity - 1);
      double sum = candidateMs;

      for (int i = recent.length - keep; i < recent.length; i++) {
         sum += recent[i];
      }

      return sum / (keep + 1.0);
   }

   private boolean sneakedRecently() {
      int since = ticksSince(AutismSessionState.getInstance().getClientTick(), this.lastSneakTick);
      return since >= 0 && since <= 150;
   }

   private boolean updateSneakLatch(boolean wanted) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.sneakLatchTicks > 0) {
         if (!wanted && this.sneakHeldTicks >= (this.diagonalBridge ? 3 : 2) && this.run_Z()) {
            this.sneakLatchTicks = 0;
            this.sneakHeldTicks = 0;
            return false;
         } else {
            this.sneakLatchTicks--;
            this.sneakHeldTicks++;
            this.lastSneakTick = tick;
            return true;
         }
      } else if (wanted || this.placedRecently(tick) && load_Z_I_2(ticksSince(tick, this.lastSneakTick))) {
         if (ticksSince(tick, this.lastSneakTick) != 1) {
            this.sneakLatchTicks = init_I_Z_I(this.diagonalBridge, ThreadLocalRandom.current().nextInt(3)) - 1;
            this.sneakHeldTicks = 1;
         } else {
            this.sneakHeldTicks++;
         }

         this.lastSneakTick = tick;
         return true;
      } else {
         this.sneakHeldTicks = 0;
         return false;
      }
   }

   static int init_I_Z_I(boolean diagonalTelly, int roll) {
      return !diagonalTelly ? 2 : Math.max(3, Math.min(5, 3 + Math.max(0, roll)));
   }

   private boolean run_Z() {
      return MC.player != null && (!MC.player.onGround() || this.wantsJump());
   }

   static boolean load_Z_I_2(int sinceSneak) {
      return sinceSneak < 0 || sinceSneak >= 90;
   }

   private void rememberRecentCell(BlockPos cell) {
      if (cell != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         this.recentCells.entrySet().removeIf(entry -> ticksSince(tick, entry.getValue()) >= 10);
         if (this.recentCells.size() < 32) {
            this.recentCells.put(cell.immutable(), tick);
         }
      }
   }

   private boolean sync(BlockPos cell) {
      if (this.recentCells.isEmpty()) {
         return false;
      } else {
         Integer marked = this.recentCells.get(cell);
         if (marked == null) {
            return false;
         } else if (ticksSince(AutismSessionState.getInstance().getClientTick(), marked) >= 10) {
            this.recentCells.remove(cell);
            return false;
         } else {
            return MC.player == null || !overlapsColumn(MC.player.position(), cell);
         }
      }
   }

   private boolean sync(ScaffoldModule.PlannedPlacement target) {
      return distancePastSupportFace(MC.player.getEyePosition(), target.support(), target.face()) > 0.05;
   }

   public static void init_V_22() {
   }

   private boolean placedRecently(int tick) {
      return this.lastPlaceTick != -2147483648 && tick - this.lastPlaceTick <= 40;
   }

   static boolean isInFlightState(ScaffoldModule.PlacementState state) {
      return state == ScaffoldModule.PlacementState.ARMED || state == ScaffoldModule.PlacementState.SENT || state == ScaffoldModule.PlacementState.RECONCILING;
   }

   static long incrementNonNegative(long previous) {
      return Math.incrementExact(Math.max(0L, previous));
   }

   static boolean isAcknowledged(int sequence, int acknowledged) {
      return sequence >= 0 && acknowledged >= sequence;
   }

   static ScaffoldModule.StateDecision decidePlacementState(
      ScaffoldModule.PlacementState state, int age, boolean resultSeen, boolean resultConsumed, boolean solid, boolean ackCovered
   ) {
      if (state == null) {
         state = ScaffoldModule.PlacementState.IDLE;
      }

      if (state == ScaffoldModule.PlacementState.IDLE || state == ScaffoldModule.PlacementState.FAILED) {
         return new ScaffoldModule.StateDecision(state, null);
      }

      if (resultSeen && !resultConsumed) {
         return new ScaffoldModule.StateDecision(ScaffoldModule.PlacementState.FAILED, "use");
      }

      if (state == ScaffoldModule.PlacementState.ARMED) {
         return age >= 2 ? new ScaffoldModule.StateDecision(ScaffoldModule.PlacementState.FAILED, "not-sent") : new ScaffoldModule.StateDecision(state, null);
      }

      if (resultSeen && resultConsumed && solid) {
         return new ScaffoldModule.StateDecision(ScaffoldModule.PlacementState.PREDICTED, null);
      }

      if (state == ScaffoldModule.PlacementState.PREDICTED && !solid) {
         state = ScaffoldModule.PlacementState.RECONCILING;
      } else if (state == ScaffoldModule.PlacementState.SENT && resultSeen && resultConsumed) {
         state = ScaffoldModule.PlacementState.RECONCILING;
      }

      if (state == ScaffoldModule.PlacementState.RECONCILING) {
         if (ackCovered) {
            return new ScaffoldModule.StateDecision(ScaffoldModule.PlacementState.FAILED, "ack-air");
         }

         if (age >= 2) {
            return new ScaffoldModule.StateDecision(ScaffoldModule.PlacementState.FAILED, "timeout-air");
         }
      } else if (state == ScaffoldModule.PlacementState.SENT && age >= 2) {
         return new ScaffoldModule.StateDecision(ScaffoldModule.PlacementState.FAILED, "result-timeout");
      }

      return new ScaffoldModule.StateDecision(state, null);
   }

   private static void expungeCollectedPacketKeys() {
      ScaffoldModule.WeakPacketKey collected;
      while ((collected = (ScaffoldModule.WeakPacketKey)packetKeyQueue.poll()) != null) {
         queuedPackets.remove(collected);
      }
   }

   static ScaffoldModule.BridgeStage bridgeStageFor(boolean supportSolid, int supportDeficit, boolean riserSolid) {
      if (!supportSolid) {
         return supportDeficit <= 1 ? ScaffoldModule.BridgeStage.SUPPORT : ScaffoldModule.BridgeStage.CONNECTOR;
      } else {
         return riserSolid ? ScaffoldModule.BridgeStage.READY : ScaffoldModule.BridgeStage.RISER;
      }
   }

   private boolean isLiveCaptureActive() {
      return this.liveCaptureTick >= 0;
   }

   private void captureLiveTrace(String outcome, ScaffoldModule.PlannedPlacement target) {
      if (this.isLiveCaptureActive() && MC.player != null) {
         if (this.liveCaptureTick == 0) {
            AsyncTraceLogger.init("[scaffold-live] capture start");
            AsyncTraceLogger.init(
               "[scaffold-why] legend lr=the reservation ledger, advisory only - it decides no key since the 12:52 ruling (known=landing solved, deficit=blocks still owed, pend=click already on the wire, landsolid=is the landing column's support already a block - winners take off T, losers F); strip=<stage>:<-removed+added> per stage, F/B/L/R/J/S/P keys, net=physical vs emitted - the jump and travel keys are ALWAYS the player's own; the only writers left are wire-sneak/lip-stop/reconcile/empty-stack (each sneak-only), lane (grounded steering) and the sprint drop for the non-sprint predictor; clk=<why code> past=<eye past face plane>/<margin needed> demand=<pitch the crossing needs>/<hard cap> drop=<eye height over the face>; rsv=<reserved chain cell>:<its own verdict> - oob/cooldown/behind/solid/no-repl/rowlock/ok or plan[<per-face codes>], where ip:<box|side|strict> is the body-clearance refusal and strict means the CATCH PREDICATE was shut; pick=<selection tier> plan=<last plan reject>; arcend=one line per landing, dy=+0.00 placed=0 is a jump that achieved nothing; arcchain=the same arc's two-click window - the landing support (planned/became a block) against every tick the riser above it was body-legal. clear ending BEFORE set means the level was never placeable, not merely missed"
            );
         }

         Vec3 p = MC.player.position();
         Vec3 v = MC.player.getDeltaMovement();
         if (this.f_n_Z && !MC.player.onGround() && this.wantsJump()) {
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-live] t%03d takeoff     vel=%.3f,%.3f,%.3f arc=%s foot=%s",
                  this.liveCaptureTick,
                  v.x,
                  v.y,
                  v.z,
                  this.landingDebug(),
                  this.footSupportDebug()
               )
            );
            this.f_aX_I = this.liveCaptureTick;
            this.f_aY_I = AutismSessionState.getInstance().getClientTick();
            this.traceLastPosition = p;
            this.reservationSnapshot = (this.reservedSupport == null ? "--" : this.reservedSupport.toShortString()) + ":" + this.reservationReason;
            this.placeCount = 0;
            this.settleOkCount = 0;
            this.aimHoldCount = 0;
            this.paceHoldCount = 0;
            this.f_bd_I = 0;
            this.f_be_I = 0;
         }

         if (!MC.player.onGround()) {
            this.tallyOutcome(outcome);
         }

         this.trackSupportTrace();
         if (!this.f_n_Z && MC.player.onGround() && this.f_aX_I >= 0) {
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-why] t%03d arcend     from=t%03d ticks=%d lines=%d dy=%+.2f dxz=%.2f placed=%d set=%d aim=%d pace=%d notgt=%d drop=%d goal=%s end=%s",
                  this.liveCaptureTick,
                  this.f_aX_I,
                  this.f_aY_I < 0 ? -1 : AutismSessionState.getInstance().getClientTick() - this.f_aY_I,
                  this.liveCaptureTick - this.f_aX_I,
                  this.traceLastPosition == null ? 0.0 / 0.0 : p.y - this.traceLastPosition.y,
                  this.traceLastPosition == null ? 0.0 / 0.0 : Math.hypot(p.x - this.traceLastPosition.x, p.z - this.traceLastPosition.z),
                  this.placeCount,
                  this.settleOkCount,
                  this.aimHoldCount,
                  this.paceHoldCount,
                  this.f_bd_I,
                  this.f_be_I,
                  this.reservationSnapshot,
                  this.placeStatus
               )
            );
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-why] t%03d arcchain   sup=%s plan=%s set=%s | ris=%s clear=%s..%s set=%s clk=%s rfail=%s relatch=%d land=%s",
                  this.liveCaptureTick,
                  formatCell(this.traceSupport),
                  formatTick(this.f_bf_I),
                  formatTick(this.f_bg_I),
                  formatCell(this.traceRiser),
                  formatTick(this.f_bh_I),
                  formatTick(this.f_bi_I),
                  formatTick(this.f_bj_I),
                  this.set_Str,
                  this.put_Str,
                  this.supportChanges,
                  BlockPos.containing(p.x, p.y - 0.05, p.z).toShortString()
               )
            );
            this.f_aX_I = -1;
            this.f_aY_I = -1;
            this.traceLastPosition = null;
            this.supportChanges = 0;
            this.resetSupportTracking(null);
         }

         if (!this.f_o_Z && MC.player != null && !MC.player.onGround() && MC.player.fallDistance >= 0.5 && MC.player.getDeltaMovement().y < 0.0) {
            this.f_o_Z = true;
            int placeAge = this.f_av_I == -2147483648 ? -1 : AutismSessionState.getInstance().getClientTick() - this.f_av_I;
            ScaffoldModule.PlannedPlacement held = this.pendingPlacement != null ? this.pendingPlacement : this.stickyPlacement;
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-live] t%03d fall-start pos=%.2f,%.2f,%.2f vel=%.3f,%.3f,%.3f fall=%.1f placeage=%d arc=%s tgt=%s",
                  this.liveCaptureTick,
                  p.x,
                  p.y,
                  p.z,
                  v.x,
                  v.y,
                  v.z,
                  MC.player.fallDistance,
                  placeAge,
                  this.landingDebug(),
                  formatTarget(held)
               )
            );
         }

         if (MC.player.onGround()) {
            this.f_o_Z = false;
         }

         this.f_n_Z = MC.player.onGround();
         AsyncTraceLogger.init(
            String.format(
               Locale.ROOT,
               "[scaffold-live] t%03d %-11s pos=%.2f,%.2f,%.2f vel=%.3f,%.3f,%.3f step=%.3f,%.3f gnd=%s fall=%.1f sneak=%s twr=%s %s %s %s %s %s %s emitted=%s click=%s goal=%s %s %s src=%s eye=%s eff=%s target=%s %s",
               this.liveCaptureTick,
               outcome,
               p.x,
               p.y,
               p.z,
               v.x,
               v.y,
               v.z,
               this.lastPositionDelta.x,
               this.lastPositionDelta.z,
               MC.player.onGround() ? "T" : "F",
               MC.player.fallDistance,
               this.edgeGateActive ? "T" : "F",
               this.c_Z() ? "T" : "F",
               this.keysDebug(),
               this.courseDebug(),
               this.lineErrorDebug(),
               this.rowLockDebug(),
               this.gateDebug(),
               this.carryDebug(),
               formatRotation(this.activeRotation),
               formatRotation(this.clickRotation),
               formatRotation(this.previousGoal),
               this.rotationDeltaDebug(),
               this.useDebug(),
               this.targetSource,
               this.aimDecision,
               this.set_BlockPos == null ? "null" : this.set_BlockPos.toShortString(),
               formatTarget(target),
               !"place".equals(outcome) && !outcome.startsWith("PLACE") ? "" : this.load_Str_aI_TableIndex(target)
            )
         );
         this.logWhyTrace(target);
         this.stripTrace.setLength(0);
         this.tick_Str = "--";
         this.liveCaptureTick++;
         this.read_V_Str(outcome);
      }
   }

   private void tallyOutcome(String outcome) {
      if (outcome.startsWith("PLACE")) {
         this.placeCount++;
      } else if (outcome.startsWith("settle-ok")) {
         this.settleOkCount++;
      } else if (outcome.startsWith("aim-hold")) {
         this.aimHoldCount++;
      } else if (outcome.startsWith("pace-hold")) {
         this.paceHoldCount++;
      } else if (outcome.startsWith("no-target")) {
         this.f_bd_I++;
      } else if (outcome.startsWith("rise-drop")) {
         this.f_be_I++;
      }
   }

   private void read_V_Str(String outcome) {
      this.f_bl_I++;
      if (outcome.startsWith("PLACE")) {
         this.f_bm_I++;
      } else if (outcome.startsWith("settle-ok")) {
         this.f_bn_I++;
      } else if (outcome.startsWith("real-miss")) {
         this.f_bo_I++;
      } else if (outcome.startsWith("aim-hold")) {
         this.f_bp_I++;
      } else if (outcome.startsWith("pace-hold")) {
         this.f_bq_I++;
      } else if (outcome.startsWith("no-target")) {
         this.f_br_I++;
      } else if (outcome.startsWith("rise-drop")) {
         this.f_bs_I++;
      } else if (outcome.startsWith("yaw-veto") || outcome.startsWith("pitch")) {
         this.f_bu_I++;
      }

      int row = this.footingRow();
      if (row != -2147483648) {
         if (this.f_bv_I == -2147483648) {
            this.f_bv_I = row;
         } else if (row != this.f_bv_I) {
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-live] rollup y=%d>%d ticks=%d place=%d set=%d miss=%d aim=%d pace=%d notgt=%d drop=%d repl=%d veto=%d",
                  this.f_bv_I,
                  row,
                  this.f_bl_I,
                  this.f_bm_I,
                  this.f_bn_I,
                  this.f_bo_I,
                  this.f_bp_I,
                  this.f_bq_I,
                  this.f_br_I,
                  this.f_bs_I,
                  this.f_bt_I,
                  this.f_bu_I
               )
            );
            this.f_bv_I = row;
            this.f_bl_I = 0;
            this.f_bm_I = 0;
            this.f_bn_I = 0;
            this.f_bo_I = 0;
            this.f_bp_I = 0;
            this.f_bq_I = 0;
            this.f_br_I = 0;
            this.f_bs_I = 0;
            this.f_bt_I = 0;
            this.f_bu_I = 0;
         }
      }
   }

   private String load_Str_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (target != null && target.hit() != null) {
         Vec3 loc = target.hit().getLocation();
         BlockPos block = target.hit().getBlockPos();
         int tick = AutismSessionState.getInstance().getClientTick();
         int gap = this.f_av_I == -2147483648 ? -1 : tick - this.f_av_I;
         this.f_av_I = tick;
         RotationMath.Rotation click = this.clickRotation;
         float yawOff = click == null ? 0.0F / 0.0F : Mth.wrapDegrees(click.yaw() - this.courseYaw());
         float residual = click != null && this.laneOctant() != -1 ? octantResidual(this.laneYaw(), click.yaw()) : 0.0F / 0.0F;
         return String.format(
            Locale.ROOT,
            "hit=%.3f,%.3f,%.3f face=%s gap=%d yawoff=%.1f res=%.1f",
            loc.x - block.getX(),
            loc.y - block.getY(),
            loc.z - block.getZ(),
            target.face(),
            gap,
            yawOff,
            residual
         );
      } else {
         return "hit=--";
      }
   }

   private String rotationDeltaDebug() {
      if (this.activeRotation == null) {
         return "d=--,-- sum=--";
      }

      float yaw = this.activeRotation.yaw();
      float pitch = this.activeRotation.pitch();
      float dYaw = Float.isNaN(this.lastDebugYaw) ? 0.0F : Mth.wrapDegrees(yaw - this.lastDebugYaw);
      float dPitch = Float.isNaN(this.lastDebugPitch) ? 0.0F : pitch - this.lastDebugPitch;
      this.lastDebugYaw = yaw;
      this.lastDebugPitch = pitch;
      float sent = lastSentYaw;
      float rawDelta = !Float.isNaN(sent) && !Float.isNaN(this.appliedYaw) ? Math.abs(sent - this.appliedYaw) : 0.0F;
      this.appliedYaw = sent;
      this.debugYawTravel += rawDelta;
      this.debugPitchTravel = this.debugPitchTravel + Math.abs(dPitch);
      if (rawDelta > 1.0E-4F || Math.abs(dPitch) > 1.0E-4F) {
         this.debugSamples++;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      int dt = this.lastDebugTick == -2147483648 ? 1 : tick - this.lastDebugTick;
      this.lastDebugTick = tick;
      return String.format(
         Locale.ROOT,
         "d=%+.2f,%+.2f sum=%.0f,%.0f mv=%d dt=%d sent=%.2f/%.1f rd=%.2f",
         dYaw,
         dPitch,
         this.debugYawTravel,
         this.debugPitchTravel,
         this.debugSamples,
         dt,
         sent,
         this.activeRotation == null ? 0.0F / 0.0F : this.activeRotation.pitch(),
         rawDelta
      );
   }

   private String overlapDebug() {
      return MC.player == null
         ? " ovl=? set=--"
         : String.format(
            Locale.ROOT,
            " ovl=%.2f/%s set=%s",
            this.footingOverlapAt(MC.player.position(), this.footingRow()),
            MC.player.onGround() ? "T" : "F",
            this.lastAcknowledgedCell == null ? "--" : this.lastAcknowledgedCell.toShortString()
         );
   }

   private ScaffoldModule.KinematicsSnapshot captureKinematics() {
      Input emitted = MC.player.input == null ? Input.EMPTY : MC.player.input.keyPresses;
      int forwardImpulse = (emitted.forward() ? 1 : 0) - (emitted.backward() ? 1 : 0);
      int sidewaysImpulse = (emitted.left() ? 1 : 0) - (emitted.right() ? 1 : 0);
      double octant = impulseAngle(forwardImpulse, sidewaysImpulse);
      double flip = distanceToBoundary(this.fill_F);
      boolean paired = this.kinematicsTick == AutismSessionState.getInstance().getClientTick() - 1;
      if (paired && !Double.isNaN(octant) && !Float.isNaN(lastSentYaw) && !Float.isNaN(this.kinematicsAnchorYaw)) {
         double residual = Mth.wrapDegrees(lastSentYaw + octant - this.kinematicsAnchorYaw);
         double speed = moveAcceleration(this.kinematicsGrounded, emitted.sprint());
         double radians = residual * 0.01745329238474369;
         double forwardAccel = speed * Math.cos(radians);
         double lateralAccel = -speed * Math.sin(radians);
         if (this.f_r_Z && emitted.sprint()) {
            double impulse = (residual - octant) * 0.01745329238474369;
            forwardAccel += 0.2 * Math.cos(impulse);
            lateralAccel += -0.2 * Math.sin(impulse);
         }

         return new ScaffoldModule.KinematicsSnapshot(octant, residual, forwardAccel, lateralAccel, flip, emitted.sprint(), this.kinematicsGrounded);
      } else {
         return new ScaffoldModule.KinematicsSnapshot(octant, 0.0 / 0.0, 0.0 / 0.0, 0.0 / 0.0, flip, emitted.sprint(), this.kinematicsGrounded);
      }
   }

   static double moveAcceleration(boolean groundedWhenEmitted, boolean sprintWhenEmitted) {
      return (groundedWhenEmitted ? 0.1 : 0.0196) * (sprintWhenEmitted ? 1.3 : 1.0);
   }

   static double nextLateralVelocity(double perp, double lateralAccel, boolean groundedWhenEmitted) {
      return (perp + lateralAccel) * (groundedWhenEmitted ? 0.546 : 0.91);
   }

   private String kinematicsDebug(ScaffoldModule.KinematicsSnapshot drift) {
      return "oct="
         + (Double.isNaN(drift.octant()) ? "--" : formatSigned1(drift.octant()))
         + " res="
         + formatSigned1(drift.residual())
         + " afwd="
         + formatSigned4(drift.read())
         + " alat="
         + formatSigned4(drift.write())
         + " dlat="
         + formatSigned4(this.f_G_D)
         + " drg="
         + String.format(Locale.ROOT, "%.3f", drift.grounded() ? 0.546 : 0.91)
         + " spr="
         + (drift.sprinting() ? "T" : "F")
         + " flip="
         + (Double.isNaN(drift.update()) ? "--" : String.format(Locale.ROOT, "%.0f/%.0f", drift.update(), 15.0F));
   }

   private static String formatSigned1(double value) {
      return Double.isNaN(value) ? "--" : String.format(Locale.ROOT, "%+.1f", value);
   }

   private static String formatSigned4(double value) {
      return Double.isNaN(value) ? "--" : String.format(Locale.ROOT, "%+.4f", value);
   }

   private double forwardProgress(Vec3 pos) {
      Vec3 forward = this.anchorForward();
      return forward.horizontalDistanceSqr() <= 1.0E-8 ? 0.0 / 0.0 : pos.dot(forward.normalize());
   }

   private void accumulateDrift(ScaffoldModule.KinematicsSnapshot drift, double laneError, Vec3 pos) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.driftTick) {
         this.driftTick = tick;
         if (!Double.isNaN(drift.write())) {
            this.f_G_D = this.f_G_D + drift.write();
         }

         boolean grounded = MC.player.onGround();
         double course = this.forwardProgress(pos);
         if (!grounded) {
            if (this.driftGrounded) {
               this.driftAirTicks = 0;
               this.f_bD_I = 0;
               this.driftResidualSum = 0.0;
               this.driftLateralSum = 0.0;
            }

            this.driftAirTicks++;
            if (drift.sprinting()) {
               this.f_bD_I++;
            }

            if (!Double.isNaN(drift.residual())) {
               this.driftResidualSum = this.driftResidualSum + Math.abs(drift.residual());
            }

            if (!Double.isNaN(drift.write())) {
               this.driftLateralSum = this.driftLateralSum + drift.write();
            }
         } else {
            if (!this.driftGrounded && this.driftAirTicks > 0) {
               this.logDriftRate(laneError, course);
               this.lastLaneError = laneError;
               this.lastCourseProgress = course;
               this.driftGroundTicks = 0;
            }

            this.driftGroundTicks++;
         }

         this.driftGrounded = grounded;
      }
   }

   static double blocksPerSecond(double travel, int groundTicks, int airTicks) {
      int ticks = groundTicks + airTicks;
      return ticks > 0 && !Double.isNaN(travel) ? travel * 20.0 / ticks : 0.0 / 0.0;
   }

   private void logDriftRate(double laneError, double course) {
      double laneMoved = Double.isNaN(this.lastLaneError) ? 0.0 / 0.0 : laneError - this.lastLaneError;
      double travel = Double.isNaN(this.lastCourseProgress) ? 0.0 / 0.0 : Math.abs(course - this.lastCourseProgress);
      double bps = blocksPerSecond(travel, this.driftGroundTicks, this.driftAirTicks);
      AsyncTraceLogger.init(
         "[telly-cycle] air="
            + this.driftAirTicks
            + " ground="
            + this.driftGroundTicks
            + " sprint="
            + this.f_bD_I
            + "/"
            + this.driftAirTicks
            + " res="
            + formatSigned1(this.driftAirTicks == 0 ? 0.0 / 0.0 : this.driftResidualSum / this.driftAirTicks)
            + " dv="
            + formatSigned4(this.driftLateralSum)
            + " dlane="
            + formatSigned4(laneMoved)
            + " travel="
            + formatSigned4(travel)
            + " bps="
            + formatSigned4(bps)
      );
   }

   private void captureTellyTrace(String outcome) {
      if (MC.player != null && this.tellyCaptureTick >= 0) {
         if (this.tellyCaptureTick == 0) {
            AsyncTraceLogger.init("[telly-live] capture start");
         }

         Vec3 pos = MC.player.position();
         Vec3 vel = MC.player.getDeltaMovement();
         double laneError = this.laneSupport == null ? 0.0 / 0.0 : this.laneOffset - lateralOffset(pos, this.anchorYaw);
         double perp = vel.dot(this.anchorLeft());
         ScaffoldModule.KinematicsSnapshot drift = this.captureKinematics();
         this.accumulateDrift(drift, laneError, pos);
         AsyncTraceLogger.init(
            String.format(
               Locale.ROOT,
               "[telly-live] t%03d %-9s pos=%.2f,%.2f,%.2f vel=%.3f,%.3f,%.3f step=%.3f,%.3f gnd=%s fall=%.1f sneak=%s %s anchor=%.0f emitted=%s %s lane=%+.3f/perp%+.3f/bias%+.1f/steer%+.1f%s off=%s air=%s/cd%d/p%d hold=%s phase=%s motion=%s bridgeY=%d bridge=%s tgt=%s raised=%s queued=%s cd=%d fail=%d %s",
               this.tellyCaptureTick,
               outcome,
               pos.x,
               pos.y,
               pos.z,
               vel.x,
               vel.y,
               vel.z,
               this.lastPositionDelta.x,
               this.lastPositionDelta.z,
               MC.player.onGround() ? "T" : "F",
               MC.player.fallDistance,
               isKeyDown(MC.options.keyShift) ? "T" : "F",
               this.keysDebug(),
               this.anchorYaw,
               formatRotation(this.activeRotation),
               this.rotationDeltaDebug(),
               laneError,
               perp,
               this.appliedSteerBias,
               this.block_F,
               this.valid_Z ? "*" : "",
               this.set_Z() ? "T" : "F",
               this.strafeSide,
               this.strafeCooldown,
               this.strafePulsesUsed,
               this.recoveryInput,
               this.tellyPhase,
               this.forwardKeyState,
               this.supportY,
               this.laneSupport == null ? "--" : this.laneSupport.toShortString(),
               this.livePlacement == null ? "--" : this.livePlacement.plan().load().toShortString(),
               this.livePlacement != null && this.livePlacement.init_Z() ? "T" : "F",
               this.stop_Z ? "T" : "F",
               this.f_G_I,
               this.f_J_I,
               this.kinematicsDebug(drift)
            )
         );
         this.tellyCaptureTick++;
      }
   }

   private boolean set_Z() {
      try {
         return MC.player != null && this.laneSupport != null && this.update_Z_LocalPlayer(MC.player);
      } catch (RuntimeException ignored) {
         return false;
      }
   }

   private void captureTickTraceOnce() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.tellyTraceTick) {
         this.tellyTraceTick = tick;
         this.captureTellyTrace(this.ownsInput ? "tick" : "idle");
         this.kinematicsTick = tick;
         this.kinematicsGrounded = MC.player != null && MC.player.onGround();
         this.f_r_Z = this.run_Z;
         this.kinematicsAnchorYaw = this.anchorYaw;
      }
   }

   private String keysDebug() {
      Input emitted = MC.player.input == null ? Input.EMPTY : MC.player.input.keyPresses;
      return "keys="
         + (isKeyDown(MC.options.keyUp) ? "W" : "-")
         + (isKeyDown(MC.options.keyLeft) ? "A" : "-")
         + (isKeyDown(MC.options.keyDown) ? "S" : "-")
         + (isKeyDown(MC.options.keyRight) ? "D" : "-")
         + (this.wantsJump() ? "J" : "-")
         + (isKeyDown(MC.options.keyShift) ? "N" : "-")
         + (isKeyDown(MC.options.keySprint) ? "R" : "-")
         + " inPrev="
         + (emitted.forward() ? "W" : "-")
         + (emitted.left() ? "A" : "-")
         + (emitted.backward() ? "S" : "-")
         + (emitted.right() ? "D" : "-")
         + (emitted.jump() ? "J" : "-")
         + (emitted.shift() ? "N" : "-")
         + (emitted.sprint() ? "R" : "-");
   }

   private String courseDebug() {
      String cand = this.candidateOctant == -1 ? "--" : OCTANT_NAMES[this.candidateOctant] + ":" + this.candidateDwell;
      return String.format(
         Locale.ROOT,
         "crs=%s/%+.0f go=%s/%+.0f cam=%+.0f cand=%s dia=%s frz=%s lin=%+.0f",
         this.courseOctant == -1 ? "--" : OCTANT_NAMES[this.courseOctant],
         this.courseYaw(),
         this.laneOctant() == -1 ? "--" : OCTANT_NAMES[this.laneOctant()],
         this.laneYaw(),
         MC.player.getYRot(),
         cand,
         this.courseLine != null && isDiagonal(this.courseLine.load()) ? "T" : "F",
         this.k() ? "T" : "F",
         this.debouncedLaneYaw()
      );
   }

   private String lineErrorDebug() {
      ScaffoldModule.MotionVectorPair line = this.courseLine;
      if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 d = line.load().normalize();
         Vec3 left = new Vec3(d.z, 0.0, -d.x);
         Vec3 position = MC.player.position();
         double error = lineLateralError(line, position);
         Vec3 velocity = MC.player.getDeltaMovement();
         double perp = new Vec3(velocity.x, 0.0, velocity.z).dot(left);
         return String.format(Locale.ROOT, "lane=%+.3f/perp%+.3f", error, perp);
      } else {
         return "lane=--";
      }
   }

   private String rowLockDebug() {
      ScaffoldModule.RowLockBounds lock = this.flags_Z() ? this.rowLockBounds() : null;
      int age = this.stickyTick < 0 ? -1 : AutismSessionState.getInstance().getClientTick() - this.stickyTick;
      String sticky = "stk=" + (this.stickyPlacement == null ? "none" : age + "/" + this.f_aH_I);
      return lock == null
         ? "lock=none " + sticky
         : String.format(Locale.ROOT, "lock=%s/y%d/p%d/f[%d,%d] %s", lock.xAxis() ? "x" : "z", lock.load(), lock.perpendicular(), lock.write(), lock.update(), sticky);
   }

   private void logInputChange(String owner, Input before, Input after) {
      if (this.isLiveCaptureActive() && owner != null && before != null && after != null) {
         StringBuilder diff = new StringBuilder();
         appendKeyChange(diff, 'F', before.forward(), after.forward());
         appendKeyChange(diff, 'B', before.backward(), after.backward());
         appendKeyChange(diff, 'L', before.left(), after.left());
         appendKeyChange(diff, 'R', before.right(), after.right());
         appendKeyChange(diff, 'J', before.jump(), after.jump());
         appendKeyChange(diff, 'S', before.shift(), after.shift());
         appendKeyChange(diff, 'P', before.sprint(), after.sprint());
         if (diff.length() != 0) {
            if (this.stripTrace.length() > 0) {
               this.stripTrace.append(',');
            }

            this.stripTrace.append(owner).append(':').append(diff);
         }
      }
   }

   private static void appendKeyChange(StringBuilder out, char name, boolean before, boolean after) {
      if (before != after) {
         out.append((char)(after ? '+' : '-')).append(name);
      }
   }

   private void init_V_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (MC.player != null && target != null) {
         Vec3 eye = MC.player.getEyePosition();
         float yaw = this.activeRotation != null ? this.activeRotation.yaw() : MC.player.getYRot();
         float demand = requiredPitchAt(eye, target.support(), target.face(), yaw);
         double xing = init_D_Vec3_3(eye, target.support(), target.face(), yaw);
         this.tick_Str = String.format(
            Locale.ROOT,
            "past=%+.3f/%.2f demand=%s/%.1f drop=%.2f xing=%s",
            distancePastSupportFace(eye, target.support(), target.face()),
            0.05,
            Float.isNaN(demand) ? "nan" : String.format(Locale.ROOT, "%.1f", demand),
            89.5F,
            eye.y - (target.support().getY() + 1.0 - 0.5),
            Double.isNaN(xing) ? "--" : String.format(Locale.ROOT, "%+.2f", xing)
         );
      } else {
         this.tick_Str = "--";
      }
   }

   private void logWhyTrace(ScaffoldModule.PlannedPlacement target) {
      AsyncTraceLogger.init(
         String.format(
            Locale.ROOT,
            "[scaffold-why] t%03d lr=%s | strip=%s | clk=%s %s | rsv=%s | pick=%s plan=%s | tgt=%s",
            this.liveCaptureTick,
            this.reservationLedger,
            this.stripTrace.length() == 0 ? "none" : this.stripTrace.toString(),
            this.placeStatus,
            this.tick_Str,
            this.get_Str,
            this.targetSource,
            this.planFailure.isEmpty() ? "ok" : this.planFailure,
            formatTarget(target)
         )
      );
   }

   private String gateDebug() {
      return "gate=edge"
         + (this.edgeDanger ? "1" : "0")
         + ".fall"
         + (this.fallDanger ? "1" : "0")
         + ".brink"
         + (this.lateralBrink ? "1" : "0")
         + ".owed"
         + (this.footingOwed ? "1" : "0")
         + ".pbrink"
         + (this.f_h_Z ? "1" : "0")
         + ".last"
         + (this.f_i_Z ? "1" : "0")
         + ".nofoot"
         + (this.hasNoFooting() ? "1" : "0")
         + ".resc"
         + (this.bind_Z() ? "1" : "0")
         + ".div"
         + (this.a_Z() ? "1" : "0")
         + ".rise"
         + (this.rising ? "1" : "0")
         + " brake="
         + this.brakeMode
         + (Double.isNaN(this.f_y_D) ? "" : String.format(Locale.ROOT, " dclimb=%.0f/%d", this.f_y_D, 400L))
         + " lovl="
         + this.carryLabel
         + " stand="
         + this.pushStatus
         + String.format(Locale.ROOT, " trv=%.2f", this.carryDistance)
         + " jmp="
         + this.open_Str
         + " arc="
         + this.landingDebug()
         + " "
         + this.supportDebug()
         + " tkoff="
         + (this.riseTakeoffCell == null ? "--" : this.riseTakeoffCell.toShortString())
         + ":"
         + this.next_Str
         + " riser="
         + this.push_Str
         + " click="
         + (this.clickLanded ? "1" : "0")
         + " free="
         + (this.predictionReady ? "1" : "0")
         + " anch="
         + this.steerMode
         + this.overlapDebug()
         + " pin="
         + (
            this.pinFace == null
               ? "--"
               : this.pinFace.getName().charAt(0) + "@" + (this.pinSupport == null ? "?" : this.pinSupport.toShortString())
         )
         + (Double.isNaN(this.f_Z_D) ? " cross=--" : String.format(Locale.ROOT, " cross=%.2f", this.f_Z_D))
         + " foot="
         + this.footSupportDebug()
         + " held="
         + this.placeableStack().getCount()
         + " why="
         + this.placeStatus;
   }

   private String landingDebug() {
      if (MC.player != null && MC.level != null && !MC.player.onGround()) {
         String budget = this.airTicks + "/" + this.f_af_I;
         int row = this.footingRow();
         Vec3 landing = projectLandingPoint(MC.player.position(), MC.player.getDeltaMovement(), row + 1.0, 12, 0.02);
         if (landing == null) {
            return budget + "@--";
         }

         BlockPos cell = BlockPos.containing(landing.x, row + 0.5, landing.z);
         if (MC.level.isOutsideBuildHeight(cell)) {
            return budget + "@oob";
         }

         String at = "@" + cell.getX() + "," + cell.getZ();
         int deficit = this.supportDeficit(cell);
         return budget + at + (deficit == 0 ? ":ok" : (deficit == 1 ? ":n1" : ":n0"));
      } else {
         return "--";
      }
   }

   private void trackSupportTrace() {
      if (MC.player != null && MC.level != null && !MC.player.onGround()) {
         BlockPos support = this.reservedSupport;
         if (support != null) {
            if (!support.equals(this.traceSupport)) {
               if (this.traceSupport != null) {
                  this.supportChanges++;
               }

               this.resetSupportTracking(support);
            }

            if (this.f_bf_I < 0 && "support".equals(this.reservationReason)) {
               this.f_bf_I = this.liveCaptureTick;
            }

            if (this.f_bg_I < 0 && this.open(this.traceSupport)) {
               this.f_bg_I = this.liveCaptureTick;
            }

            BlockPos riser = this.traceRiser;
            if (riser != null && !MC.level.isOutsideBuildHeight(riser)) {
               if (this.f_bj_I < 0 && this.open(riser)) {
                  this.f_bj_I = this.liveCaptureTick;
               }

               String clear = catchDebug(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), riser, this.push(riser));
               if ("ok".equals(clear)) {
                  if (this.f_bh_I < 0) {
                     this.f_bh_I = this.liveCaptureTick;
                  }

                  this.f_bi_I = this.liveCaptureTick;
                  this.put_Str = this.push_Str;
               } else {
                  this.set_Str = clear;
               }
            }
         }
      }
   }

   private void resetSupportTracking(BlockPos support) {
      this.traceSupport = support == null ? null : support.immutable();
      this.traceRiser = support == null ? null : support.above().immutable();
      this.f_bf_I = -1;
      this.f_bg_I = -1;
      this.f_bh_I = -1;
      this.f_bi_I = -1;
      this.f_bj_I = -1;
      this.set_Str = "--";
      this.put_Str = "--";
   }

   private static String formatTick(int tick) {
      return tick < 0 ? "--" : String.format(Locale.ROOT, "t%03d", tick);
   }

   private String supportDebug() {
      String support = this.reservedSupport == null ? "--" : this.reservedSupport.toShortString();
      String connector = this.reservedConnector == null ? "--" : this.reservedConnector.toShortString();
      String deficit = "?";
      if (this.reservedSupport != null && MC.level != null && !MC.level.isOutsideBuildHeight(this.reservedSupport)) {
         int count = this.supportDeficit(this.reservedSupport);
         deficit = count == 0 ? "ok" : (count == 1 ? "n1" : "n0");
      }

      return "reserve=" + this.reservationReason + "@" + support + ":" + deficit + "/" + connector;
   }

   private String footSupportDebug() {
      if (MC.level == null) {
         return "?";
      } else {
         BlockPos support = BlockPos.containing(MC.player.position()).below();
         if (MC.level.isOutsideBuildHeight(support)) {
            return "oob";
         } else {
            return this.hasCentreSupport(MC.level.getBlockState(support), support) ? "solid" : "AIR";
         }
      }
   }

   private static String formatRotation(RotationMath.Rotation rotation) {
      return rotation == null ? "null" : String.format(Locale.ROOT, "%.1f,%.1f", rotation.yaw(), rotation.pitch());
   }

   private static String formatTarget(ScaffoldModule.PlannedPlacement target) {
      if (target == null) {
         return "none";
      }

      Vec3 p = target.hit().getLocation();
      return target.face()
         + "@"
         + target.support().toShortString()
         + "->"
         + target.load().toShortString()
         + String.format(Locale.ROOT, " pt=%.2f,%.2f,%.2f", p.x, p.y, p.z);
   }

   private void preAim() {
      this.updateIdleAim(true);
   }

   private RotationMath.Rotation rotateTowards(RotationMath.Rotation goal) {
      return this.rotateTowards(goal, false);
   }

   private RotationMath.Rotation rotateTowards(RotationMath.Rotation goal, boolean holdPitch) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.rotatedTick == tick && this.activeRotation != null) {
         return this.activeRotation;
      }

      if (MC.player == null) {
         return this.activeRotation;
      }

      this.rotationWindingDown = false;
      this.rotationStepCount = 0;
      if (this.rawRotation == null) {
         this.rawRotation = RotationMath.ofPlayer(MC.player);
         this.rotationIntegrator.reseed(this.random.nextLong());
         this.previousGoal = null;
         this.directionChange = 0.0F;
      }

      double gcd = RotationMath.gcdStep();
      this.rawRotation = smoothStep(
         this.rotationIntegrator,
         this.rawRotation,
         RotationMath.angleDelta(goal.yaw(), this.rawRotation.yaw()),
         holdPitch ? 0.0F : Mth.clamp(goal.pitch(), -90.0F, 90.0F) - this.rawRotation.pitch(),
         this.directionChangeEma(goal),
         gcd,
         rotationLimit(this.block_Z(), 18.0F),
         rotationLimit(this.block_Z(), 20.0F)
      );
      float previousPitch = this.activeRotation == null ? 0.0F / 0.0F : this.activeRotation.pitch();
      this.activeRotation = ditherRotation(this.rawRotation, this.activeRotation, gcd, this.nextDitherSign(), this.lastPlaceYaw);
      this.unusedPitchDelta = Float.isNaN(previousPitch) ? 0.0F : Math.abs(this.activeRotation.pitch() - previousPitch);
      this.rotationHoldTicks = 5;
      this.rotatedTick = tick;
      return this.activeRotation;
   }

   static RotationMath.Rotation smoothStep(SmoothRotationIntegrator smoother, RotationMath.Rotation current, RotationMath.Rotation goal, float directionChange, double gcd) {
      return smoothStep(
         smoother, current, RotationMath.angleDelta(goal.yaw(), current.yaw()), Mth.clamp(goal.pitch(), -90.0F, 90.0F) - current.pitch(), directionChange, gcd
      );
   }

   static RotationMath.Rotation smoothStep(SmoothRotationIntegrator smoother, RotationMath.Rotation current, float yawError, float pitchError, float directionChange, double gcd) {
      return smoothStep(smoother, current, yawError, pitchError, directionChange, gcd, 18.0F, 20.0F);
   }

   static RotationMath.Rotation smoothStep(
      SmoothRotationIntegrator smoother, RotationMath.Rotation current, float yawError, float pitchError, float directionChange, double gcd, float maxYawStep, float maxPitchStep
   ) {
      SmoothRotationIntegrator.CountStep step = smoother.step(yawError, pitchError, gcd, maxYawStep, maxPitchStep, 180.0F, directionChange, 0.35F, true, true);
      RotationMath.Rotation delta = new SmoothRotationIntegrator.CountStep(
            deadbandCounts(clampCounts(step.init(), maxYawStep, gcd), yawError, gcd), deadbandCounts(clampCounts(step.load(), maxPitchStep, gcd), pitchError, gcd)
         )
         .init(gcd);
      return new RotationMath.Rotation(Mth.wrapDegrees(current.yaw() + delta.yaw()), Mth.clamp(current.pitch() + delta.pitch(), -90.0F, 90.0F));
   }

   static long deadbandCounts(long counts, float error, double gcd) {
      if (Double.isFinite(gcd) && !(gcd <= 1.0E-9)) {
         return Math.abs(error) < gcd * 0.5 ? 0L : counts;
      } else {
         return counts;
      }
   }

   static long clampCounts(long counts, float maxDegrees, double gcd) {
      if (Double.isFinite(gcd) && !(gcd <= 1.0E-9)) {
         long limit = Math.max(1L, (long)Math.floor(maxDegrees / gcd));
         return Math.max(-limit, Math.min(limit, counts));
      } else {
         return counts;
      }
   }

   private float directionChangeEma(RotationMath.Rotation goal) {
      float change = this.previousGoal == null ? 0.0F : Math.min(1.0F, RotationMath.vectorAngleDegrees(this.previousGoal, goal) / 45.0F);
      this.previousGoal = goal;
      this.directionChange = this.directionChange * 0.65F + change * 0.35F;
      return this.directionChange;
   }

   static float nudgeOffIntegerAngle(float angle, double gcd) {
      if (angle % 1.0F == 0.0F && angle != 0.0F && angle != 90.0F && angle != -90.0F) {
         float step = (float)(gcd > 1.0E-6 ? gcd : 0.0225);
         float nudged = angle + step;
         return nudged != 90.0F && nudged != -90.0F ? nudged : angle - step;
      } else {
         return angle;
      }
   }

   static float yawOffsetFrom45(float yaw) {
      return (float)(yaw - Math.round(yaw / 45.0) * 45.0);
   }

   static boolean load_Z_F(float yaw) {
      return Math.abs(yawOffsetFrom45(yaw)) >= 0.2F && yaw % 1.0F != 0.0F;
   }

   static float init_F_F_2(float candidate, double gcd, int preferSign, float forbidden, float lastPlaceYaw) {
      float step = (float)(gcd > 1.0E-6 ? gcd : 0.0225);
      int direction = preferSign != 0 ? preferSign : (yawOffsetFrom45(candidate) >= 0.0F ? 1 : -1);
      if (load_Z_F(candidate) && candidate != forbidden && candidate != lastPlaceYaw) {
         return candidate;
      }

      for (int ring = 1; ring <= 64; ring++) {
         float near = candidate + direction * ring * step;
         if (load_Z_F(near) && near != forbidden && near != lastPlaceYaw) {
            return near;
         }

         float far = candidate - direction * ring * step;
         if (load_Z_F(far) && far != forbidden && far != lastPlaceYaw) {
            return far;
         }
      }

      return candidate + step;
   }

   static RotationMath.Rotation ditherRotation(
      RotationMath.Rotation raw, RotationMath.Rotation previousEmitted, double gcd, int ditherCounts, float lastPlaceYaw
   ) {
      if (raw == null) {
         return null;
      }

      float previousYaw = previousEmitted == null ? 0.0F / 0.0F : previousEmitted.yaw();
      float candidate = Mth.wrapDegrees((float)(raw.yaw() + ditherCounts * gcd));
      int prefer = previousEmitted == null ? 0 : (int)Math.signum(RotationMath.angleDelta(raw.yaw(), previousYaw));
      return new RotationMath.Rotation(init_F_F_2(candidate, gcd, prefer, previousYaw, lastPlaceYaw), nudgeOffIntegerAngle(Mth.clamp(raw.pitch(), -90.0F, 90.0F), gcd));
   }

   private int nextDitherSign() {
      int next = this.lastDitherSign;

      while (next == this.lastDitherSign) {
         next = this.random.nextInt(3) - 1;
      }

      this.lastDitherSign = next;
      return next;
   }

   private ScaffoldModule.WireClickRotation wireClickRotation() {
      return wireClickRotation(ServerRotationTracker.getSnapshot());
   }

   static ScaffoldModule.WireClickRotation wireClickRotation(ServerRotationTracker.RotationSnapshot snapshot) {
      if (snapshot != null && snapshot.initialized() && Float.isFinite(snapshot.currentYaw()) && Float.isFinite(snapshot.currentPitch())) {
         RotationMath.Rotation current = new RotationMath.Rotation(snapshot.currentYaw(), snapshot.currentPitch());
         RotationMath.Rotation previous = Float.isFinite(snapshot.previousYaw()) && Float.isFinite(snapshot.previousPitch())
            ? new RotationMath.Rotation(snapshot.previousYaw(), snapshot.previousPitch())
            : current;
         return new ScaffoldModule.WireClickRotation(previous, current, snapshot.tick());
      } else {
         return null;
      }
   }

   static float wirePitchGap(ServerRotationTracker.RotationSnapshot snapshot) {
      ScaffoldModule.WireClickRotation wire = wireClickRotation(snapshot);
      return wire == null ? 0.0F / 0.0F : Math.abs(wire.load().pitch() - wire.init_ce_DataUtil().pitch());
   }

   private String useDebug() {
      this.drainInputFlags();
      String wire = !this.inputFlagsSeen ? "--" : (this.wireOnGround ? "T" : "F") + (this.wireHorizontalCollision ? "H" : "-") + (this.wireHasPosition ? "P" : "S");
      return String.format(
            Locale.ROOT,
            "use=%s/g%d/o%d:%d/s%d/%s%s ack=%d/%d q=%d+%d wire=%s",
            this.placementState.name().toLowerCase(Locale.ROOT),
            this.activeGeneration,
            this.submittedCount,
            this.f_aL_I,
            this.pendingSequence,
            this.pendingStatus,
            this.resultConsumed ? "C" : "-",
            this.reconciledSequence,
            this.ackedSequence,
            this.predictions.size(),
            this.predictedCells.size(),
            wire
         )
         + (this.hasPredictedFooting() ? " pend=T" : "");
   }

   private boolean hasPredictedFooting() {
      if (MC.player != null && !this.predictions.isEmpty()) {
         AABB box = MC.player.getBoundingBox();
         int below = Mth.floor(box.minY - 0.001);

         for (ScaffoldModule.PendingPlacementSeq placement : this.predictions) {
            BlockPos cell = placement.cell();
            if (cell.getY() == below && cell.getX() + 1 > box.minX && cell.getX() < box.maxX && cell.getZ() + 1 > box.minZ && cell.getZ() < box.maxZ) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean load_Z_aI_TableIndex_ce_DataUtil(ScaffoldModule.PlannedPlacement target, RotationMath.Rotation clickRotation) {
      if (MC.player != null && MC.level != null && clickRotation != null) {
         double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
         BlockHitResult ray = raycast(MC.player.getEyePosition(), clickRotation, reach, MC.level, MC.player);
         this.aimRay = ray;
         return hitMatchesPending(ray, target, this.hitPlacesPending(ray, target));
      } else {
         return false;
      }
   }

   private String clickDebug(ScaffoldModule.PlannedPlacement target) {
      if (!this.isLiveCaptureActive()) {
         return "";
      }

      RotationMath.Rotation rotation = this.clickRotation;
      if (rotation == null) {
         ScaffoldModule.WireClickRotation wire = this.wireClickRotation();
         rotation = wire == null ? null : wire.load();
      }

      if (MC.player != null && MC.level != null && rotation != null) {
         double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
         BlockHitResult ray = raycast(MC.player.getEyePosition(), rotation, reach, MC.level, MC.player);
         return ray == null
            ? " miss=no-hit"
            : " miss=hit:"
               + ray.getBlockPos().toShortString()
               + "/"
               + ray.getDirection().getName().charAt(0)
               + String.format(Locale.ROOT, "@%.2f", ray.getLocation().y)
               + " want:"
               + target.support().toShortString()
               + "/"
               + target.face().getName().charAt(0)
               + String.format(Locale.ROOT, ">=%.2f", target.minHitY());
      } else {
         return " miss=no-state";
      }
   }

   private void updateIdleAim(boolean allowPreAim) {
      if (MC != null && MC.player != null) {
         if (allowPreAim && this.c_Z()) {
            this.rotateTowards(this.open_ce_DataUtil());
         } else if (!allowPreAim || !this.add_Z() && !this.isAirborneOnCourse()) {
            if (allowPreAim && this.reset_Z()) {
               this.rotateTowards(this.placeAimRotation());
            } else {
               if (this.activeRotation != null) {
                  this.rotationWindingDown = true;
               }

               this.tickRotationWindDown();
            }
         } else {
            this.rotateTowards(this.placeAimRotation());
         }
      }
   }

   private boolean add_Z() {
      LocalPlayer player = MC.player;
      if (!player.onGround()) {
         return false;
      }

      Vec3 motion = player.getDeltaMovement();
      if (!this.isPlaceableBlock(this.placeableStack())) {
         return false;
      }

      Vec3 direction;
      if (this.courseLine != null && this.courseLine.load().horizontalDistanceSqr() > 1.0E-8) {
         direction = this.courseLine.load();
      } else {
         if (!(motion.horizontalDistance() > 0.03)) {
            return false;
         }

         direction = new Vec3(motion.x, 0.0, motion.z).normalize();
      }

      Vec3 position = player.position();

      for (double distance : LOOKAHEAD_DISTANCES) {
         if (MC.level.getBlockState(blockBelow(position.add(direction.scale(distance)))).isAir()) {
            return true;
         }
      }

      return false;
   }

   private boolean isAirborneOnCourse() {
      boolean hasCourse = this.courseLine != null && this.courseLine.load().horizontalDistanceSqr() > 1.0E-8;
      return isAirborneOnCourseWithBlock(MC.player.onGround(), hasCourse, this.isPlaceableBlock(this.placeableStack()));
   }

   static boolean isAirborneOnCourseWithBlock(boolean onGround, boolean hasCourse, boolean hasPlaceableBlock) {
      return !onGround && hasCourse && hasPlaceableBlock;
   }

   private boolean reset_Z() {
      return !this.isPlaceableBlock(this.placeableStack())
         ? false
         : init_Z_Z(
            isKeyDown(MC.options.keyUp)
               || isKeyDown(MC.options.keyDown)
               || isKeyDown(MC.options.keyLeft)
               || isKeyDown(MC.options.keyRight),
            this.courseLine == null ? null : this.courseLine.load(),
            this.fallOffPoint(this.courseLine),
            AutismSessionState.getInstance().getClientTick(),
            this.lastPlaceTick
         );
   }

   static boolean init_Z_Z(boolean directionalKeyHeld, Vec3 courseDirection, Vec3 fallOff, int clientTick, int lastPlacementTick) {
      int sincePlacement = clientTick - lastPlacementTick;
      return sincePlacement >= 0 && sincePlacement <= 20
         ? true
         : directionalKeyHeld && courseDirection != null && courseDirection.horizontalDistanceSqr() > 1.0E-8 && fallOff != null;
   }

   private RotationMath.Rotation placeAimRotation() {
      float pitch = this.basePitch;
      if (Float.isFinite(this.placePitch) && this.placedRecently(AutismSessionState.getInstance().getClientTick())) {
         pitch = this.placePitch;
      }

      float yaw = this.debouncedLaneYaw();
      if (this.pendingTick != AutismSessionState.getInstance().getClientTick() && this.candidateOctant != -1 && this.candidateDwell >= 2) {
         int candidateLane = offsetOctant(this.candidateOctant, this.inputOctant);
         if (candidateLane != -1) {
            yaw = oppositeYaw(octantToYaw(candidateLane));
         }
      }

      return new RotationMath.Rotation(yaw, pitch);
   }

   static float capPitch89(float solved) {
      return Math.min(solved, 89.0F);
   }

   static float init_F_F(float solved, double windowLow, double windowHigh) {
      float goal = solved;
      float bound = 89.0F;
      if (Double.isFinite(windowLow) && Double.isFinite(windowHigh) && windowLow <= windowHigh) {
         goal = (float)Mth.clamp(goal, windowLow, windowHigh);
         if (windowLow > 89.0) {
            bound = 89.3F;
         }

         if (windowLow > 89.30000305175781) {
            bound = 89.5F;
         }
      }

      return Math.min(goal, bound);
   }

   static boolean isBelowPitchLimit(float emitted) {
      return emitted <= 89.5F;
   }

   static float oppositeYaw(float movementYaw) {
      return Mth.wrapDegrees(movementYaw + 180.0F);
   }

   static float yawOffset(float sentYaw, float vanillaYaw) {
      return Float.isNaN(sentYaw) ? 0.0F : sentYaw - vanillaYaw;
   }

   static float yawWrapOffset(float sentYaw, float cameraYaw) {
      return Float.isFinite(sentYaw) && Float.isFinite(cameraYaw) ? 360.0F * Math.round((sentYaw - cameraYaw) / 360.0F) : 0.0F;
   }

   private void applyYawWrapOffset() {
      if (MC != null && MC.player != null && autismclient.util.RotationStreamPlanner.isActive(this.rotationStream)) {
         LocalPlayer player = MC.player;
         float offset = yawWrapOffset(lastSentYaw, player.getYRot());
         if (offset != 0.0F && Float.isFinite(offset)) {
            player.setYRot(player.getYRot() + offset);
            player.yRotO += offset;
            player.setYHeadRot(player.getYHeadRot() + offset);
            player.yHeadRotO += offset;
            player.yBodyRot += offset;
            player.yBodyRotO += offset;
            player.yBob += offset;
            player.yBobO += offset;
         }
      }
   }

   static int settleTickBudget(float yawDebt, float pitchDelta) {
      int yawTicks = (int)Math.ceil(Math.abs(yawDebt) / 37.0F);
      int pitchTicks = (int)Math.ceil(Math.abs(pitchDelta) / 37.0F);
      return Mth.clamp(Math.max(yawTicks, pitchTicks) + 4, 8, 27);
   }

   static boolean isRotationSettled(float yawDebt, float pitchDelta) {
      return Math.abs(yawDebt) <= 2.0F && Math.abs(pitchDelta) <= 2.0F;
   }

   private void tickRotationWindDown() {
      if (this.rotationWindingDown) {
         if (MC != null && MC.player != null && this.activeRotation != null) {
            this.applyYawWrapOffset();
            if (this.rawRotation == null) {
               this.rawRotation = this.activeRotation;
            }

            RotationMath.Rotation camera = RotationMath.ofPlayer(MC.player);
            double gcd = RotationMath.gcdStep();
            float debt = yawOffset(lastSentYaw, MC.player.getYRot());
            float pitchDelta = camera.pitch() - this.rawRotation.pitch();
            float yawCap = this.rotationStepCount == 0 ? 20.0F : 37.0F;
            float pitchCap = this.rotationStepCount == 0 ? 20.0F : 37.0F;
            if (autismclient.util.RotationStreamPlanner.isActive(this.rotationStream)) {
               this.activeRotation = RotationMath.stepQuantized(this.activeRotation, camera, 95.0F, 95.0F, gcd);
               autismclient.util.RotationStreamPlanner.begin(this.rotationStream, this.activeRotation);
               this.rawRotation = this.activeRotation;
            } else {
               this.rawRotation = smoothStep(
                  this.rotationIntegrator, this.rawRotation, -debt, pitchDelta, this.directionChangeEma(camera), gcd, yawCap, pitchCap
               );
               this.activeRotation = ditherRotation(this.rawRotation, this.activeRotation, gcd, this.nextDitherSign(), this.lastPlaceYaw);
            }

            this.rotationStepCount++;
            this.settleTicks = Math.max(this.settleTicks, Math.min(settleTickBudget(debt, pitchDelta), 27 - this.rotationStepCount));
            if (this.settleTicks > 0) {
               this.settleTicks--;
            }

            this.rotationHoldTicks = Math.max(this.rotationHoldTicks, 1);
            if (isRotationSettled(debt, pitchDelta)) {
               this.resetRotationState();
            } else if (this.rotationStepCount >= 27) {
               this.captureLiveTrace("wind-down-forced d=" + String.format(Locale.ROOT, "%.2f", debt), null);
               this.resetRotationState();
            }
         } else {
            this.resetRotationState();
         }
      }
   }

   void resetRotationState() {
      this.applyYawWrapOffset();
      this.activeRotation = null;
      this.rawRotation = null;
      this.rotationHoldTicks = 0;
      this.rotatedTick = -2147483648;
      this.rotationWindingDown = false;
      this.settleTicks = 0;
      this.rotationStepCount = 0;
      this.lastDitherSign = 0;
      this.rotationIntegrator.reset();
      this.previousGoal = null;
      this.directionChange = 0.0F;
      autismclient.util.RotationStreamPlanner.reset(this.rotationStream);
   }

   void rerollAimPitch() {
      this.basePitch = 68.0F + this.random.nextFloat() * 12.0F;
   }

   private void size_V() {
      this.run_Z = false;
      this.braking = false;
      this.node_Z = false;
      this.f_X_I = 1;
      this.pushTarget = null;
      this.pushTargetTick = -2147483648;
      this.farFromEdge = false;
      this.strafeSide = ScaffoldModule.StrafeSide.NONE;
      this.recoveryInput = null;
      this.tickOncePerClientTick();
      if (!this.canOperate()) {
         this.block_V();
         this.tickRotationWindDown();
         this.tickSwitchBack();
      } else {
         LocalPlayer player = MC.player;
         this.anchorPitch = Mth.clamp(player.getXRot(), -90.0F, 90.0F);
         Input liveKeys = new Input(
            isKeyDown(MC.options.keyUp),
            isKeyDown(MC.options.keyDown),
            isKeyDown(MC.options.keyLeft),
            isKeyDown(MC.options.keyRight),
            false,
            false,
            false
         );
         this.inputStableTicks = liveKeys.equals(this.lastLiveInput) ? this.inputStableTicks + 1 : 1;
         this.lastLiveInput = liveKeys;
         this.stableInput = stabilizedInput(this.stableInput, liveKeys, this.inputStableTicks, this.ownsInput);
         boolean physicalForward = this.stableInput.forward();
         boolean physicalSpace = (!this.isEagleMode() || this.bool("eagle-jump")) && isKeyDown(MC.options.keyJump);
         boolean physicalMoving = anyMovementKey(physicalForward, this.stableInput.backward(), this.stableInput.left(), this.stableInput.right());
         this.spaceHeld = physicalSpace;
         if (!physicalSpace) {
            this.reset_Z = false;
         }

         if (player.onGround() || !this.ownsInput) {
            this.airborneInput = this.stableInput;
         }

         this.dangerStop = false;
         if (!AutismLiteVariant.enabled()) {
            this.dangerStop = DangerModule.poll_Z_6();
         }

         boolean rawStop = !physicalMoving || this.dangerStop;
         boolean airborneOwned = this.ownsInput && !player.onGround();
         boolean stopSafeHere = !rawStop || airborneOwned || !this.ownsInput || this.sneakHold.init_Z() || this.init_Z_LocalPlayer(player);
         this.sneakHold = nextSneakHold(this.sneakHold, rawStop, airborneOwned, stopSafeHere);
         this.pop_Z = this.sneakHold.init_Z();
         if (isFreshJumpPress(physicalForward, physicalSpace, this.spaceWasDown, this.ownsInput)) {
            this.set_Z = true;
         }

         this.spaceWasDown = physicalSpace;
         if (player.onGround()) {
            BlockPos groundedSupport = this.supportBlockUnder(player);
            if (groundedSupport != null) {
               this.groundedSupport = groundedSupport.immutable();
               this.groundedSupportTick = AutismSessionState.getInstance().getClientTick();
            }
         }

         if (!this.ownsInput) {
            if (!player.onGround() && this.hasRecentGroundedSupport(player)) {
               this.update_V_LocalPlayer(player);
            } else {
               if (!physicalMoving || !player.onGround()) {
                  if (!physicalMoving) {
                     this.prev_Z = false;
                  }

                  this.updateIdleAim(false);
                  this.tickSwitchBack();
                  return;
               }

               BlockPos under = this.supportBlockUnder(player);
               if (under == null) {
                  this.updateIdleAim(false);
                  this.tickSwitchBack();
                  return;
               }

               if (!this.isPlaceableBlock(this.placeableStack())) {
                  this.updateIdleAim(false);
                  this.tickSwitchBack();
                  return;
               }

               this.init_V_LocalPlayer_BlockPos(player, under);
            }
         }

         this.pop_Z = this.sneakHold.init_Z();
         if (this.pop_Z && !player.onGround()) {
            this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
         }

         if (this.pop_Z && !this.start_Z) {
            this.set_Z = false;
            this.clear_Z = false;
            if (!this.stop_Z && this.livePlacement != null && this.livePlacement.init_Z()) {
               this.livePlacement = null;
            }
         }

         this.open_V(player);
         if (this.prev_Z) {
            this.trackAnchorDeviation(player);
         }

         this.braking = this.push(player);
         if (!this.isEagleMode()
            && !player.onGround()
            && this.livePlacement == null
            && !this.stop_Z
            && (this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING)
            && (!this.open_Z(player) || this.load_BlockPos_LocalPlayer(player) == null)) {
            this.livePlacement = this.planLanePlacement(player, this.placeableStack());
         }

         this.close_V(player);
         this.activeRotation = this.chooseAim(player);
         this.rotationHoldTicks = 5;
         if (player.onGround()) {
            this.load_V_LocalPlayer(player);
         } else if (this.tellyPhase == ScaffoldModule.TellyPhase.RECOVERING) {
            this.sync_V(player);
            this.tickTellyAirborne(player);
         } else {
            this.tickTellyAirborne(player);
         }

         if (!this.cache_Z) {
            this.poll_V(player);
            if (this.ownsInput && player.onGround() && this.forwardKeyState == ScaffoldModule.ForwardKeyState.HOLD && !this.pop_Z && !this.add_Z) {
               if (++this.f_W_I >= 30) {
                  this.f_W_I = 0;
                  this.anchorDeviationStreak = 0;
                  this.task_Z = false;
                  this.f_aa_I = 0;
                  this.f_ab_I = 0;
                  this.f_S_I = 0;
                  this.clearEntryCells();
               }
            } else {
               this.f_W_I = 0;
            }

            if (this.ownsInput) {
               if (this.turnPhase != ScaffoldModule.TurnCyclePhase.PLACING) {
                  this.placeHand();
               }

               this.requestSwitchBack();
            }

            this.tickSwitchBack();
         }
      }
   }

   private void init_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      this.write_BlockPos = null;
      this.reset_Z = false;
      this.turnPhase = ScaffoldModule.TurnCyclePhase.FORWARD;
      this.quantizedPlacement = null;
      this.close_Z = false;
      this.ownsInput = true;
      this.pop_Z = false;
      this.add_Z = false;
      this.tellyPhase = ScaffoldModule.TellyPhase.FORWARD_DWELL;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
      this.rotationWindingDown = false;
      this.rotationStepCount = 0;
      if (!autismclient.util.RotationStreamPlanner.isActive(this.rotationStream)) {
         autismclient.util.RotationStreamPlanner.begin(this.rotationStream, this.lastSentRotation());
      }

      this.plannedAim = autismclient.util.RotationStreamPlanner.currentRotation(this.rotationStream);
      float lookYaw = this.movementYaw(player);
      boolean hadCourse = this.prev_Z && Float.isFinite(this.anchorYaw);
      float previousAnchor = this.anchorYaw;
      this.clearEntryCells();
      if (hadCourse) {
         float lookDeviation = Math.abs(RotationMath.angleDelta(this.anchorYaw, lookYaw));
         if (lookDeviation > 45.0F) {
            float snapped = snapYawToCardinal(lookYaw);
            this.anchorYaw = Mth.wrapDegrees(snapped);
         } else {
            this.anchorYaw = Mth.wrapDegrees(this.anchorYaw);
         }
      } else {
         this.anchorYaw = Mth.wrapDegrees(snapYawToCardinal(lookYaw));
      }

      this.prev_Z = true;
      if (hadCourse && Float.compare(snapYawToCardinal(previousAnchor), snapYawToCardinal(this.anchorYaw)) != 0) {
         this.setEntryCells(under, cardinalDirection(previousAnchor));
         this.value_V();
      }

      this.anchorPitch = Mth.clamp(player.getXRot(), -90.0F, 90.0F);
      this.laneCentrePoint = this.lanePointFor(under, player.position(), this.anchorYaw);
      this.laneOffset = lateralOffset(this.laneCentrePoint, this.anchorYaw);
      this.anchorDeviationStreak = 0;
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.resetStrafeState();
      this.laneSupport = under.immutable();
      this.supportY = under.getY();
      this.f_K_I = 0;
      this.tellyAirTicks = 0;
      this.buffer_Z = false;
      this.f_S_I = 0;
      this.livePlacement = null;
      this.open_BlockPos = null;
      this.stop_Z = false;
      this.selectedOffset = -1;
      this.data_Z = true;
   }

   private void trackAnchorDeviation(LocalPlayer player) {
      float deviation = Math.abs(RotationMath.angleDelta(this.anchorYaw, this.movementYaw(player)));
      this.anchorDeviationStreak = deviationStreak(this.anchorDeviationStreak, deviation);
   }

   static int deviationStreak(int current, float deviation) {
      if (deviation > 45.0F) {
         return current + 1;
      } else {
         return deviation < 35.0F ? 0 : Math.max(0, current - 1);
      }
   }

   static Input stabilizedInput(Input current, Input live, int stableTicks, boolean owned) {
      return current != null && owned && stableTicks < 3 ? current : live;
   }

   static boolean anyMovementKey(boolean forward, boolean backward, boolean left, boolean right) {
      return forward || backward || left || right;
   }

   static ScaffoldModule.SneakHoldFlags nextSneakHold(ScaffoldModule.SneakHoldFlags previous, boolean rawStop, boolean airborneOwned, boolean stopSafeHere) {
      ScaffoldModule.SneakHoldFlags last = previous == null ? ScaffoldModule.SneakHoldFlags.NONE : previous;
      if (airborneOwned) {
         return new ScaffoldModule.SneakHoldFlags(last.init_Z(), last.load() || rawStop && !last.init_Z());
      }

      boolean stop = rawStop && (last.init_Z() || stopSafeHere);
      return new ScaffoldModule.SneakHoldFlags(stop, rawStop && !stop);
   }

   private void resnapAnchor(LocalPlayer player, BlockPos laneSupport) {
      if (laneSupport != null && this.anchorDeviationStreak >= 3) {
         this.anchorDeviationStreak = 0;
         float previousAnchor = this.anchorYaw;
         Direction previousDirection = cardinalDirection(previousAnchor);
         float snapped = snapYawToCardinal(this.movementYaw(player));
         this.anchorYaw = Mth.wrapDegrees(snapped);
         this.prev_Z = true;
         this.laneCentrePoint = this.lanePointFor(laneSupport, player.position(), this.anchorYaw);
         this.laneOffset = lateralOffset(this.laneCentrePoint, this.anchorYaw);
         this.resetSteering();
         this.resetStrafeState();
         if (Float.compare(snapYawToCardinal(previousAnchor), snapYawToCardinal(this.anchorYaw)) != 0) {
            this.setEntryCells(laneSupport, previousDirection);
            this.value_V();
         }
      }
   }

   private void setEntryCells(BlockPos support, Direction entryDirection) {
      this.clearEntryCells();
      if (support != null && entryDirection != null && entryDirection.getAxis().isHorizontal()) {
         this.entrySupport = support.immutable();
         this.entryCell = support.relative(entryDirection).immutable();
      }
   }

   private void clearEntryCells() {
      if (this.livePlacement != null && this.entryCell != null && this.entryCell.equals(this.livePlacement.plan().load())) {
         this.livePlacement = null;
         this.selectedOffset = -1;
      }

      this.entrySupport = null;
      this.entryCell = null;
      this.pool_Z = false;
   }

   private boolean isAnchorDeviating() {
      return this.anchorDeviationStreak >= 2;
   }

   private void value_V() {
      this.livePlacement = null;
      this.selectedOffset = -1;
      if (!this.task_Z) {
         this.task_Z = true;
         this.f_aa_I = 0;
         this.f_ab_I = 1;
         this.resetSteering();
      }
   }

   private void load_V_LocalPlayer(LocalPlayer player) {
      if (!this.isEagleMode() || this.bool("eagle-jump") && this.isAimAligned()) {
         this.cache_Z = false;
         this.diagonalBridge = false;
         BlockPos under = this.supportBlockUnder(player);
         if (!this.pop_Z && !this.add_Z) {
            if (under != null && !isKeyDown(MC.options.keyShift) && autismclient.util.BlockPlacementPlanner.requiresSneakToPlace(MC.level.getBlockState(under), under)) {
               this.beginStraightTelly(player, under);
            } else if (this.tellyPhase == ScaffoldModule.TellyPhase.RECOVERING) {
               this.sync_V_LocalPlayer_BlockPos(player, under != null ? under : this.laneSupport);
            } else {
               if (under != null) {
                  float anchorBeforeTurn = this.anchorYaw;
                  this.resnapAnchor(player, under);
                  double laneError = this.laneOffset - lateralOffset(player.position(), this.anchorYaw);
                  if (anchorBeforeTurn != this.anchorYaw && Math.abs(laneError) > 0.55) {
                     this.laneCentrePoint = this.lanePointFor(under, player.position(), this.anchorYaw);
                     this.laneOffset = lateralOffset(this.laneCentrePoint, this.anchorYaw);
                     this.resetSteering();
                  }
               }

               if (under == null) {
                  if (this.laneSupport == null) {
                     this.chunk_V();
                  } else if (this.tellyAirTicks > 0 && this.flush_Z(player)) {
                     this.holdAtEdge(player, this.laneSupport);
                  } else {
                     if (this.tellyAirTicks > 0
                        || this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH
                        || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING
                        || this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING) {
                        this.update_V_BlockPos(this.laneSupport);
                     }

                     this.resnapAnchor(player, this.laneSupport);
                     if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, this.laneSupport)) {
                        if (this.livePlacement == null) {
                           ItemStack lipStack = this.placeableStack();
                           if (this.isPlaceableBlock(lipStack)) {
                              this.livePlacement = this.planLanePlacement(player, lipStack);
                           }
                        }

                        if (this.sync_Z_LocalPlayer_BlockPos(player, this.laneSupport)) {
                           this.beginStraightTelly(player, this.laneSupport);
                        } else if (this.livePlacement != null && !this.isAnchorDeviating() && this.flush_Z_LocalPlayer_BlockPos(player, this.laneSupport)) {
                           if (this.ensureAimAligned()) {
                              this.init_V_LocalPlayer_aI_DataPipe(player, this.livePlacement);
                           } else {
                              this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
                           }
                        } else {
                           if (this.isAnchorDeviating()) {
                              this.f_S_I++;
                              if (this.f_S_I < 20) {
                                 this.holdAtEdge(player, this.laneSupport);
                                 return;
                              }

                              this.anchorDeviationStreak = 0;
                              this.f_S_I = 0;
                           }

                           if (!this.isPlaceableBlock(this.placeableStack())) {
                              this.holdAtEdge(player, under);
                           } else {
                              this.beginStraightTelly(player, this.laneSupport);
                           }
                        }
                     }
                  }
               } else if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, under)) {
                  if (this.tellyAirTicks > 0
                     || this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH
                     || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING
                     || this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING) {
                     boolean interruptedLanding = this.queue_Z || this.next_Z;
                     this.update_V_BlockPos(under);
                     this.close_ce_DataUtil();
                     if (interruptedLanding) {
                        this.beginStraightTelly(player, under);
                        return;
                     }

                     this.tellyPhase = ScaffoldModule.TellyPhase.RUNNING;
                     this.f_K_I = 0;
                  }

                  if (this.rotationSmoother != null && this.rotationSmoother.init_Z() && !this.isAimAligned()) {
                     this.close_ce_DataUtil();
                     this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
                     Input carry = this.init_Input_LocalPlayer_BlockPos(player, under);
                     this.recoveryInput = carry != null
                        ? carry
                        : this.stayOnSupportInput(player, under, new Input(false, false, false, false, false, false, false));
                  } else if (this.tellyPhase != ScaffoldModule.TellyPhase.FORWARD_DWELL || !this.load_Z_LocalPlayer_BlockPos(player, under)) {
                     boolean nearEdge = this.isNearEdge(player, under);
                     this.farFromEdge = !nearEdge && this.supportRemaining(player, under) > 4.5;
                     if (this.pop_Z) {
                        this.read_V_LocalPlayer_BlockPos(player, under);
                     } else {
                        this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
                        this.updateLateralCorrection(player, under);
                        this.slotSwitched = false;
                        InteractionHand hand = this.placeHand();
                        if (hand != null) {
                           this.requestSwitchBack();
                           ItemStack stack = player.getItemInHand(hand);
                           if (this.isPlaceableBlock(stack)) {
                              this.laneSupport = under.immutable();
                              this.supportY = under.getY();
                              ScaffoldModule.LivePlacement first = this.planLanePlacement(player, stack);
                              this.livePlacement = first;
                              if (this.sync_Z_LocalPlayer_BlockPos(player, under)) {
                                 this.beginStraightTelly(player, under);
                              } else if (this.farFromEdge && this.spaceHeld && !this.isAnchorDeviating()) {
                                 this.init_V_LocalPlayer_aI_DataPipe(player, first);
                              } else if (!nearEdge) {
                                 this.f_S_I = 0;
                                 if (this.init_Z_LocalPlayer_3(player, under, first)) {
                                    this.init_V_LocalPlayer_aI_DataPipe(player, first);
                                 }
                              } else if (first == null) {
                                 this.f_S_I++;
                                 if (this.f_S_I >= 40) {
                                    this.beginStraightTelly(player, under);
                                 } else {
                                    this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
                                 }
                              } else {
                                 if (this.isAnchorDeviating()) {
                                    this.f_S_I++;
                                    if (this.f_S_I < 20) {
                                       this.holdAtEdge(player, under);
                                       return;
                                    }

                                    this.anchorDeviationStreak = 0;
                                    this.f_S_I = 0;
                                 }

                                 this.f_S_I = 0;
                                 boolean launchCatchable = !this.set_Z && !this.spaceHeld
                                    ? this.flush_Z_LocalPlayer_BlockPos(player, under)
                                    : this.close_Z_LocalPlayer_BlockPos(player, under);
                                 if (init_Z_Z_2(launchCatchable)) {
                                    this.enterTellyRecovery(player, under);
                                 } else if (!this.ensureAimAligned()) {
                                    this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
                                 } else {
                                    this.init_V_LocalPlayer_aI_DataPipe(player, first);
                                 }
                              }
                           } else if (!this.slotSwitched) {
                              if (nearEdge) {
                                 this.holdAtEdge(player, under);
                              }
                           } else {
                              this.forwardKeyState = nearEdge ? ScaffoldModule.ForwardKeyState.HOLD : ScaffoldModule.ForwardKeyState.FORWARD;
                           }
                        } else if (!this.slotSwitched) {
                           if (nearEdge) {
                              this.holdAtEdge(player, under);
                           }
                        } else {
                           this.forwardKeyState = nearEdge ? ScaffoldModule.ForwardKeyState.HOLD : ScaffoldModule.ForwardKeyState.FORWARD;
                           if (this.slotSwitched) {
                              this.requestSwitchBack();
                           }
                        }
                     }
                  }
               }
            }
         } else {
            if (under != null && this.tellyAirTicks > 0) {
               this.update_V_BlockPos(under);
            }

            this.read_V_LocalPlayer_BlockPos(player, under);
         }
      } else {
         this.beginEagleBridge(player, this.supportBlockUnder(player));
      }
   }

   private boolean init_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      if (this.pop_Z) {
         this.task_Z = false;
         return false;
      }

      if (this.tellyAirTicks > 0
         || this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH
         || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING
         || this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING) {
         this.update_V_BlockPos(under);
      }

      Vec3 velocity = player.getDeltaMovement();
      Vec3 forward = this.anchorForward();
      Vec3 left = this.anchorLeft();
      Input recovery = this.stayOnSupportInput(player, under, centringInput(player.getBoundingBox(), under, this.anchorYaw));
      double laneError = this.laneOffset - lateralOffset(player.position(), this.anchorYaw);
      if (Math.abs(laneError) > 0.55) {
         this.laneCentrePoint = this.lanePointFor(under, player.position(), this.anchorYaw);
         this.laneOffset = lateralOffset(this.laneCentrePoint, this.anchorYaw);
         laneError = this.laneOffset - lateralOffset(player.position(), this.anchorYaw);
      }

      boolean settled = !hasMovementInput(recovery)
         && this.plannedAim != null
         && isLaneSettled(
            this.plannedAim.yaw(),
            this.anchorFacingYaw(),
            velocity.x * forward.x + velocity.z * forward.z,
            velocity.x * left.x + velocity.z * left.z,
            laneError,
            player.onGround()
         );
      if (settled && this.f_ab_I <= 0) {
         this.task_Z = false;
         this.clearEntryCells();
         this.tellyPhase = ScaffoldModule.TellyPhase.FORWARD_DWELL;
         this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
         this.f_M_I = 0;
         this.f_K_I = 0;
         return true;
      }

      if (settled) {
         this.f_ab_I--;
      }

      this.resetSteering();
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
      Direction overhang = this.overhangDirection(player);
      this.recoveryInput = recovery;
      if (!isOverhangCell(this.entrySupport, this.entryCell, overhang)) {
         this.clearEntryCells();
      }

      if (overhang != null && isOverhangCell(this.entrySupport, this.entryCell, overhang) && this.sync_Z(player)) {
         this.f_aa_I++;
         if (this.f_aa_I >= 30) {
            this.clearEntryCells();
            this.task_Z = false;
            this.enterTellyRecovery(player, under);
         }

         return true;
      } else {
         this.f_aa_I++;
         if (this.f_aa_I >= 30) {
            this.task_Z = false;
            this.enterTellyRecovery(player, under);
         }

         return true;
      }
   }

   private void read_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      this.holdAtEdge(player, under);
      boolean atEdge = under == null || this.isNearEdge(player, under);
      Direction overhang = this.overhangDirection(player);
      if (atEdge || overhang != null) {
         this.f_S_I++;
         if (overhang != null && isOverhangCell(this.entrySupport, this.entryCell, overhang) && this.sync_Z(player)) {
            return;
         }

         if (atEdge && this.flush_Z(player)) {
            return;
         }
      }

      if (!this.dangerStop && this.load_Z_LocalPlayer(player)) {
         this.chunk_V();
      }
   }

   private void holdAtEdge(LocalPlayer player, BlockPos support) {
      this.resetSteering();
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
      this.run_Z = false;
      this.recoveryInput = support == null
         ? null
         : this.stayOnSupportInput(player, support, centringInput(player.getBoundingBox(), support, this.anchorYaw));
   }

   static double coastDistance(double velocity, double friction) {
      return Double.isFinite(velocity) && Double.isFinite(friction) ? velocity / (1.0 - Mth.clamp(friction, 0.0, 0.99)) : 0.0;
   }

   static double groundAcceleration(double movementSpeed, double blockFriction) {
      double f = Mth.clamp(blockFriction, 0.1, 1.0);
      double scale = f > 0.6 ? 0.21600002 / (f * f * f) : 1.0;
      return Math.max(0.0, movementSpeed) * 0.98 * scale;
   }

   static double brakingDistance(double velocity, double friction, double brakeAcceleration) {
      if (Double.isFinite(velocity) && !(Math.abs(velocity) < 1.0E-6)) {
         double f = Mth.clamp(friction, 0.0, 0.99);
         double sign = Math.signum(velocity);
         double motion = velocity;
         double travelled = 0.0;
         double furthest = 0.0;

         for (int tick = 0; tick < 200 && Math.abs(motion) > 1.0E-5; tick++) {
            if (motion * sign >= 0.03) {
               motion -= sign * Math.max(0.0, brakeAcceleration);
            }

            travelled += motion;
            if (travelled * sign > furthest * sign) {
               furthest = travelled;
            }

            motion *= f;
         }

         return furthest;
      } else {
         return 0.0;
      }
   }

   static Input driftRecoveryInput(Input recovery, ScaffoldModule.AxisDriftMetrics along, ScaffoldModule.AxisDriftMetrics across) {
      boolean forward = recovery != null && recovery.forward();
      boolean backward = recovery != null && recovery.backward();
      boolean left = recovery != null && recovery.left();
      boolean right = recovery != null && recovery.right();
      boolean brakeAlong = along != null && along.init_Z_2();
      boolean brakeAcross = across != null && across.init_Z_2();
      if (brakeAlong) {
         forward = along.init_D() < 0.0;
         backward = along.init_D() > 0.0;
         if (!brakeAcross) {
            right = false;
            left = false;
         }
      }

      if (brakeAcross) {
         left = across.init_D() < 0.0;
         right = across.init_D() > 0.0;
         if (!brakeAlong) {
            backward = false;
            forward = false;
         }
      }

      return new Input(forward, backward, left, right, false, false, false);
   }

   private double blockFriction(BlockPos support) {
      return support != null && MC.level != null ? MC.level.getBlockState(support).getBlock().getFriction() : 0.6;
   }

   private double groundDrag(BlockPos support) {
      return Mth.clamp(this.blockFriction(support) * 0.91, 0.0, 0.99);
   }

   private Input stayOnSupportInput(LocalPlayer player, BlockPos support, Input recovery) {
      if (player != null
         && support != null
         && MC.level != null
         && player.onGround()
         && Float.isFinite(this.anchorYaw)
         && overlapsBlock(player.getBoundingBox(), support, 1.0E-9)) {
         double friction = this.groundDrag(support);
         Vec3 velocity = player.getDeltaMovement();
         Vec3 forward = this.anchorForward();
         Vec3 left = this.anchorLeft();
         AABB box = player.getBoundingBox();
         Vec3 toSupport = Vec3.atCenterOf(support).subtract(box.getCenter());
         ScaffoldModule.AxisDriftMetrics along = this.measureAxisDrift(
            player, box, forward, friction, velocity.x * forward.x + velocity.z * forward.z, toSupport.x * forward.x + toSupport.z * forward.z
         );
         ScaffoldModule.AxisDriftMetrics across = this.measureAxisDrift(
            player, box, left, friction, velocity.x * left.x + velocity.z * left.z, toSupport.x * left.x + toSupport.z * left.z
         );
         return driftRecoveryInput(recovery, along, across);
      } else {
         return recovery;
      }
   }

   private Input init_Input_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (!this.isEagleMode()
         && support != null
         && this.activeRotation != null
         && !this.spaceHeld
         && !this.set_Z
         && !this.clear_Z
         && !this.start_Z
         && this.flush_I() <= 1
         && !this.pop_Z
         && !this.sneakHold.load()
         && !this.isAnchorDeviating()
         && !this.task_Z) {
         Input forward = new Input(true, false, false, false, false, false, false);
         Input keys = init_Input_Input_2(forward, this.anchorYaw, this.activeRotation.yaw());
         Vec3 local = new Vec3(axisValue(keys.left(), keys.right()), 0.0, axisValue(keys.forward(), keys.backward())).normalize();
         double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (player.isSprinting() ? 1.3 : 1.0);
         double acceleration = groundAcceleration(speed, this.blockFriction(support));
         float yaw = this.activeRotation.yaw() * 0.017453292F;
         Vec3 force = new Vec3(local.x * Mth.cos(yaw) - local.z * Mth.sin(yaw), 0.0, local.z * Mth.cos(yaw) + local.x * Mth.sin(yaw)).scale(acceleration);
         Vec3 velocity = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
         Vec3 step = velocity.add(force);
         if (!(force.dot(this.anchorForward()) <= 0.0) && !(this.collideMovement(player, player.getBoundingBox(), step).distanceToSqr(step) > 1.0E-8)) {
            boolean safe = brakeStaysOnFloor(
               player.getBoundingBox(), velocity, force, this.groundDrag(support), acceleration * 0.6, box -> this.hasFloorUnderFootprint(box, player.getY(), 0.12)
            );
            return safe ? forward : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean brakeStaysOnFloor(AABB body, Vec3 velocity, Vec3 acceleration, double drag, double brake, Predicate<AABB> floor) {
      Vec3 step = velocity.add(acceleration);
      Vec3 next = step.scale(Mth.clamp(drag, 0.0, 0.99));
      Vec3 reach = step.add(brakingDistance(next.x, drag, brake), 0.0, brakingDistance(next.z, drag, brake));
      if (reach.horizontalDistance() > 1.5) {
         return false;
      }

      double minX = Math.min(0.0, reach.x) - 0.015;
      double maxX = Math.max(0.0, reach.x) + 0.015;
      double minZ = Math.min(0.0, reach.z) - 0.015;
      double maxZ = Math.max(0.0, reach.z) + 0.015;
      int xs = Math.max(1, (int)Math.ceil((maxX - minX) / 0.04));
      int zs = Math.max(1, (int)Math.ceil((maxZ - minZ) / 0.04));

      for (int x = 0; x <= xs; x++) {
         for (int z = 0; z <= zs; z++) {
            if (!floor.test(body.move(Mth.lerp((double)x / xs, minX, maxX), 0.0, Mth.lerp((double)z / zs, minZ, maxZ)))) {
               return false;
            }
         }
      }

      return true;
   }

   private ScaffoldModule.AxisDriftMetrics measureAxisDrift(LocalPlayer player, AABB box, Vec3 axis, double friction, double velocity, double toSupport) {
      double coast = coastDistance(velocity, friction);
      boolean leaves = !this.hasFloorUnderFootprint(box.move(axis.scale(coast)), player.getY(), 0.12);
      return new ScaffoldModule.AxisDriftMetrics(velocity, coast, toSupport, leaves);
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player) {
      if (player != null && MC.level != null && player.onGround() && Float.isFinite(this.anchorYaw)) {
         BlockPos support = this.supportBlockUnder(player);
         if (support == null) {
            return true;
         }

         double friction = this.groundDrag(support);
         Vec3 velocity = player.getDeltaMovement();
         Vec3 forward = this.anchorForward();
         Vec3 left = this.anchorLeft();
         double alongVelocity = velocity.x * forward.x + velocity.z * forward.z;
         double acrossVelocity = velocity.x * left.x + velocity.z * left.z;
         double brake = strafeAcceleration(Math.min(0.1, player.getSpeed()), this.blockFriction(support), alongVelocity, acrossVelocity);
         Vec3 reach = forward.scale(brakingDistance(alongVelocity, friction, brake)).add(left.scale(brakingDistance(acrossVelocity, friction, brake)));
         AABB box = player.getBoundingBox();
         return reach.horizontalDistanceSqr() < 1.0E-6 ? true : this.hasFloorUnderFootprint(box.move(reach), player.getY(), 0.05);
      } else {
         return true;
      }
   }

   static double strafeAcceleration(double movementSpeed, double blockFriction, double alongVelocity, double acrossVelocity) {
      boolean bothAxes = Math.abs(alongVelocity) >= 0.03 && Math.abs(acrossVelocity) >= 0.03;
      return groundAcceleration(movementSpeed, blockFriction) * 0.92 * (bothAxes ? Mth.SQRT_OF_TWO / 2.0 : 1.0);
   }

   private boolean load_Z_LocalPlayer(LocalPlayer player) {
      if (player.onGround() && !this.stop_Z && !this.isPlacementInFlight() && !(player.getDeltaMovement().horizontalDistance() >= 0.06)) {
         BlockPos support = this.supportBlockUnder(player);
         if (support == null) {
            return false;
         }

         double drag = Mth.clamp(Math.max(0.91, MC.level.getBlockState(support).getBlock().getFriction() * 0.91), 0.0, 0.99);
         Vec3 coast = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
         AABB box = player.getBoundingBox();
         if (!this.hasFloorUnderFootprint(box, player.getY(), 0.12)) {
            return false;
         }

         Vec3 remaining = coast.scale(1.0 / (1.0 - drag));
         int samples = Math.max(1, (int)Math.ceil(remaining.horizontalDistance() / 0.05));

         for (int sample = 0; sample < samples; sample++) {
            Vec3 step = this.collideMovement(player, box, remaining.scale(1.0 / samples));
            box = box.move(step);
            if (!this.hasFloorUnderFootprint(box, player.getY(), 0.12)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean load_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      this.resetSteering();
      boolean nearEdge = this.isNearEdge(player, under);
      if (this.pop_Z) {
         this.read_V_LocalPlayer_BlockPos(player, under);
         return true;
      } else {
         this.f_K_I = init_I_I_Z(this.f_K_I, true);
         if (!write_Z_I(this.f_K_I)) {
            this.forwardKeyState = nearEdge ? ScaffoldModule.ForwardKeyState.HOLD : ScaffoldModule.ForwardKeyState.FORWARD;
            return true;
         } else {
            this.tellyPhase = ScaffoldModule.TellyPhase.RUNNING;
            this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
            this.f_K_I = 0;
            return false;
         }
      }
   }

   private void enterTellyRecovery(LocalPlayer player, BlockPos support) {
      if (this.tellyPhase != ScaffoldModule.TellyPhase.RECOVERING && player != null && support != null) {
         this.tellyPhase = ScaffoldModule.TellyPhase.RECOVERING;
         this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
         this.f_M_I = 0;
         this.livePlacement = null;
         this.stop_Z = false;
         this.open_BlockPos = null;
         this.laneSupport = support.immutable();
         this.resetSteering();
         this.resetStrafeState();
      }
   }

   private void sync_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player == null || support == null) {
         this.chunk_V();
      } else if (this.pop_Z) {
         this.read_V_LocalPlayer_BlockPos(player, support);
      } else {
         this.resnapAnchor(player, support);
         if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, support)) {
            if (this.sync_Z_LocalPlayer_BlockPos(player, support)) {
               this.tellyPhase = ScaffoldModule.TellyPhase.RUNNING;
               this.f_M_I = 0;
               this.beginStraightTelly(player, support);
            } else {
               this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
               this.laneCentrePoint = this.lanePointFor(support, player.position(), this.anchorYaw);
               this.laneOffset = lateralOffset(this.laneCentrePoint, this.anchorYaw);
               this.recoveryInput = this.stayOnSupportInput(player, support, centringInput(player.getBoundingBox(), support, this.anchorYaw));
               this.f_M_I++;
               if (this.f_M_I >= 60) {
                  this.tellyPhase = ScaffoldModule.TellyPhase.RUNNING;
                  this.beginStraightTelly(player, support);
               } else if (this.flush_Z_LocalPlayer_BlockPos(player, support)) {
                  this.recoveryInput = null;
                  this.tellyPhase = ScaffoldModule.TellyPhase.FORWARD_DWELL;
                  this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
                  this.laneCentrePoint = this.lanePointFor(support, player.position(), this.anchorYaw);
                  this.laneOffset = lateralOffset(this.laneCentrePoint, this.anchorYaw);
                  this.laneSupport = support.immutable();
                  this.supportY = support.getY();
                  this.livePlacement = null;
                  this.f_M_I = 0;
                  this.f_K_I = 0;
               }
            }
         }
      }
   }

   private void beginStraightTelly(LocalPlayer player, BlockPos support) {
      this.beginTelly(player, support, false);
   }

   static boolean isFacingAgainstCourse(float course, float yaw) {
      return Float.isFinite(course) && Float.isFinite(yaw) && Math.abs(RotationMath.angleDelta(course + 180.0F, yaw)) <= 1.0F;
   }

   private void beginEagleBridge(LocalPlayer player, BlockPos support) {
      if (!AutismLiteVariant.enabled()) {
         this.cache_Z = false;
         this.diagonalBridge = false;
         this.farFromEdge = false;
         this.run_Z = false;
         this.set_Z = false;
         this.clear_Z = false;
         this.resetImperfection();
         if (this.tellyAirTicks > 0 && support != null) {
            this.update_V_BlockPos(support);
         }

         this.tellyPhase = ScaffoldModule.TellyPhase.RUNNING;
         this.turnPhase = ScaffoldModule.TurnCyclePhase.PLACING;
         if (support == null) {
            support = this.laneSupport;
         }

         if (support == null) {
            this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
         } else {
            float previousCourse = this.anchorYaw;
            this.resnapAnchor(player, support);
            if (previousCourse != this.anchorYaw) {
               this.read_BlockPos = null;
               this.quantizedPlacement = null;
               this.close_Z = false;
            }

            if (this.pop_Z || this.add_Z) {
               this.read_V_LocalPlayer_BlockPos(player, support);
            } else if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, support)) {
               this.laneSupport = support.immutable();
               this.supportY = support.getY();
               if (this.read_BlockPos != null
                  && this.isSettledSolid(MC.level.getBlockState(this.read_BlockPos), this.read_BlockPos)
                  && isUnacknowledgedPrediction(this.read_BlockPos, this.ackedSequence, this.predictions)) {
                  this.read_BlockPos = null;
               }

               if (!this.stop_Z) {
                  this.livePlacement = this.planLanePlacement(player, this.placeableStack());
               }

               if (this.read_BlockPos == null && this.livePlacement != null) {
                  ScaffoldModule.PlannedPlacement next = this.livePlacement.plan();
                  Vec3 plane = Vec3.atCenterOf(next.support()).add(this.anchorForward().scale(0.5));
                  if (player.getEyePosition().subtract(plane).dot(this.anchorForward()) > 1.0E-9) {
                     this.read_BlockPos = next.load().immutable();
                  }
               }

               this.flush_Z(player);
               this.updateLateralCorrection(player, support);
               Vec3 forward = this.anchorForward();
               double friction = this.groundDrag(support);
               double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
               if (player.isSprinting()) {
                  speed /= 1.3;
               }

               Vec3 step = player.getDeltaMovement().multiply(1.0, 0.0, 1.0).add(forward.scale(speed * 0.98));
               double brake = strafeAcceleration(speed, this.blockFriction(support), step.dot(forward), 0.0);
               Vec3 stop = step.add(forward.scale(brakingDistance(step.dot(forward) * friction, friction, brake)));
               boolean safe = this.read_BlockPos == null
                  && this.isAimAligned()
                  && this.isPlaceableBlock(this.placeableStack())
                  && this.hasFloorUnderFootprint(player.getBoundingBox().move(stop), player.getY(), 0.08);
               this.forwardKeyState = safe ? ScaffoldModule.ForwardKeyState.FORWARD : ScaffoldModule.ForwardKeyState.HOLD;
               if (!safe) {
                  this.resetSteering();
                  this.recoveryInput = this.init_Input_LocalPlayer(player, support, speed);
               }
            }
         }
      }
   }

   private Input init_Input_LocalPlayer(LocalPlayer player, BlockPos support, double speed) {
      Input straight = this.stayOnSupportInput(player, support, new Input(false, false, false, false, false, false, false));
      if (AutismLiteVariant.enabled()) {
         return straight;
      }

      if (this.read_BlockPos != null && this.isAimAligned() && !straight.left() && !straight.right()) {
         Vec3 forward = this.anchorForward();
         Vec3 left = this.anchorLeft();
         Vec3 plane = Vec3.atCenterOf(this.read_BlockPos).subtract(forward.scale(0.5));
         boolean approach = !hasMovementInput(straight)
            && player.getDeltaMovement().horizontalDistanceSqr() < 2.25E-4
            && player.position().subtract(plane).dot(forward) < 0.04;
         if (!approach && straight.forward() == straight.backward()) {
            return straight;
         }

         double acceleration = groundAcceleration(speed, this.blockFriction(support));
         double friction = this.groundDrag(support);
         Vec3 along = forward.scale(!approach && !straight.forward() ? -1.0 : 1.0);
         Vec3 normalStop = coastEndPosition(player.position(), player.getDeltaMovement(), along.scale(acceleration), friction);
         if (!approach && normalStop.subtract(plane).dot(forward) > 0.025) {
            return straight;
         }

         Input best = straight;
         double cost = 1.0 / 0.0;
         double faceLimit = (Math.abs(forward.x) > 0.5 ? player.getBoundingBox().getXsize() : player.getBoundingBox().getZsize()) * 0.5 - 0.08;

         for (int side = -1; side <= 1; side++) {
            Vec3 impulse = along.add(left.scale(side)).normalize().scale(acceleration);
            Vec3 stop = coastEndPosition(player.position(), player.getDeltaMovement(), impulse, friction);
            double exposed = stop.subtract(plane).dot(forward);
            double error = Math.abs(this.laneOffset - lateralOffset(stop, this.anchorYaw));
            if (!(exposed < 0.04) && !(exposed > faceLimit) && !(error > 0.35) && this.init_Z_LocalPlayer_6(player, impulse, friction)) {
               double score = error + exposed * 0.1;
               if (score < cost) {
                  cost = score;
                  best = new Input(approach || straight.forward(), !approach && straight.backward(), side > 0, side < 0, false, false, false);
               }
            }
         }

         return best;
      } else {
         return straight;
      }
   }

   static Vec3 coastEndPosition(Vec3 position, Vec3 velocity, Vec3 impulse, double friction) {
      return position.add(velocity.add(impulse).multiply(1.0, 0.0, 1.0).scale(1.0 / (1.0 - Mth.clamp(friction, 0.0, 0.99))));
   }

   private boolean init_Z_LocalPlayer_6(LocalPlayer player, Vec3 impulse, double friction) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      AABB body = player.getBoundingBox();
      Vec3 velocity = player.getDeltaMovement().add(impulse).multiply(1.0, 0.0, 1.0);

      for (int tick = 0; tick < 14; tick++) {
         Vec3 step = this.collideMovement(player, body, velocity);
         if (step.subtract(velocity).horizontalDistanceSqr() > 1.0E-8) {
            return false;
         }

         body = body.move(step);
         if (!this.hasFloorUnderFootprint(body, player.getY(), 0.08)) {
            return false;
         }

         velocity = velocity.scale(friction);
      }

      return true;
   }

   private void beginTelly(LocalPlayer player, BlockPos support, boolean diagonal) {
      if (this.isEagleMode() && !diagonal) {
         this.beginEagleBridge(player, support);
      } else {
         this.cache_Z = true;
         this.f_ay_I = -2147483648;
         this.f_az_I = -2147483648;
         this.diagonalBridge = diagonal;
         this.ownsInput = false;
         this.farFromEdge = false;
         this.run_Z = false;
         this.forwardKeyState = ScaffoldModule.ForwardKeyState.RELEASED;
         this.tellyPhase = ScaffoldModule.TellyPhase.RUNNING;
         this.resetSteering();
         this.resetStrafeState();
         this.clearEntryCells();
         this.recoveryInput = null;
         this.livePlacement = null;
         this.open_BlockPos = null;
         this.stop_Z = false;
         if (support != null) {
            this.laneSupport = support.immutable();
            this.supportY = support.getY();
         }

         this.rawRotation = this.activeRotation != null ? this.activeRotation : this.lastSentRotation();
         this.activeRotation = this.rawRotation;
         this.rotationIntegrator.reseed(this.random.nextLong());
         this.previousGoal = null;
         this.directionChange = 0.0F;
         autismclient.util.RotationStreamPlanner.reset(this.rotationStream);
         this.rotationWindingDown = false;
         this.rotationStepCount = 0;
         this.rotationHoldTicks = 5;
         int tick = AutismSessionState.getInstance().getClientTick();
         this.rotatedTick = this.aimTick == tick ? tick : -2147483648;
         this.f_aV_I = tick;
         this.emittedForward = 0;
         this.fill_F = 0.0F / 0.0F;
         this.state_V();
         this.clearReservedCells();
         this.stickyPlacement = null;
         this.lockedLine = null;
         this.updateCourseOctant();
         this.trackFootingRow();
         Input intent = filteredPhysicalInput(
            new Input(
               isKeyDown(MC.options.keyUp),
               isKeyDown(MC.options.keyDown),
               isKeyDown(MC.options.keyLeft),
               isKeyDown(MC.options.keyRight),
               isKeyDown(MC.options.keyJump),
               isKeyDown(MC.options.keyShift),
               isKeyDown(MC.options.keySprint)
            ),
            this.diagonalBridge,
            !this.isEagleMode() || this.bool("eagle-jump")
         );
         this.captureInputOctant(intent);
         this.courseLine = hasMovementInput(intent) ? this.courseLineFor(intent) : null;
      }
   }

   private void updateDiagonalMode() {
      if (!this.bool("allow-diagonal")) {
         this.diagonalBridge = false;
      }

      if (this.diagonalBridgeActive()) {
         this.diagonalBridge = true;
      } else if (this.canOperate() && MC.player.onGround()) {
         LocalPlayer player = MC.player;
         BlockPos support = this.supportBlockUnder(player);
         if (support != null
            && isStandingUnsneaked(
               player.onGround(), player.isCrouching(), isKeyDown(MC.options.keyShift), player.input == null ? null : player.input.keyPresses
            )) {
            this.trackAnchorDeviation(player);
            this.resnapAnchor(player, support);
            if ((
                  isKeyDown(MC.options.keyUp)
                     || isKeyDown(MC.options.keyDown)
                     || isKeyDown(MC.options.keyLeft)
                     || isKeyDown(MC.options.keyRight)
               )
               && this.placementState == ScaffoldModule.PlacementState.IDLE
               && !this.prunePredictedCells()
               && !this.sync_Z_LocalPlayer_BlockPos(player, support)
               && this.flush_Z_LocalPlayer_BlockPos(player, support)
               && this.init_Z_LocalPlayer(player)) {
               RotationMath.Rotation live = this.activeRotation != null ? this.activeRotation : this.lastSentRotation();
               this.block_V();
               autismclient.util.RotationStreamPlanner.begin(this.rotationStream, live);
               this.plannedAim = live;
               this.activeRotation = live;
               this.stickyPlacement = null;
               this.edgeGateActive = false;
               this.lockedLine = null;
               this.courseLine = null;
               this.clearReservedCells();
               this.emittedForward = 0;
               this.fill_F = 0.0F / 0.0F;
               this.state_V();
            }
         }
      }
   }

   static boolean isStandingUnsneaked(boolean grounded, boolean crouched, boolean physicalSneak, Input sampled) {
      return grounded && !crouched && !physicalSneak && sampled != null && !sampled.shift();
   }

   private boolean diagonalBridgeActive() {
      return MC.player != null
         && isDiagonalBridge(
            this.choice("mode"),
            this.bool("allow-diagonal"),
            MC.player.getYRot(),
            new Input(
               isKeyDown(MC.options.keyUp),
               isKeyDown(MC.options.keyDown),
               isKeyDown(MC.options.keyLeft),
               isKeyDown(MC.options.keyRight),
               false,
               false,
               false
            )
         );
   }

   static boolean isDiagonalBridge(String mode, boolean allowed, float cameraYaw, Input physical) {
      return ("Telly".equals(mode) || "Eagle".equals(mode)) && allowed && hasMovementInput(physical) && isDiagonalOctant(yawToOctant(cameraYaw + inputYawOffset(physical)));
   }

   private void init_V_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.LivePlacement first) {
      int jumpDelay = ((AutismLivingEntityAccessor)player).autism$getNoJumpDelay();
      if (!canJumpNow(player.onGround(), jumpDelay)) {
         this.holdAtEdge(player, this.supportBlockUnder(player));
      } else if (this.sync_Z_LocalPlayer_BlockPos(player, this.laneSupport)) {
         this.beginStraightTelly(player, this.laneSupport);
      } else {
         BlockPos footing = this.supportBlockUnder(player);
         if (footing != null
            && this.isAimAligned()
            && (this.set_Z || this.spaceHeld || this.flush_Z_LocalPlayer_BlockPos(player, footing) && this.load_Z_LocalPlayer(player, footing, first))) {
            int batchPower = !this.isEagleMode() && this.spaceHeld
               ? this.init_I_LocalPlayer(player, footing, player.position(), player.getDeltaMovement(), player.getBoundingBox(), this.canSprint(player))
               : -1;
            if (this.isEagleMode() || !this.spaceHeld || batchPower >= 0 || !this.poll(player, footing)) {
               boolean flatEntry = !this.isEagleMode()
                  && this.spaceHeld
                  && batchPower < 0
                  && !this.reset_Z
                  && this.flush_Z_LocalPlayer_BlockPos(player, footing)
                  && this.load_Z_LocalPlayer(player, footing, first);
               if ((this.set_Z || this.spaceHeld) && !flatEntry && !this.open_Z(player, footing)) {
                  if (this.isEagleMode()
                     || this.spaceHeld
                     || !this.init_Z_LocalPlayer(player, footing, player.position(), player.getDeltaMovement(), player.getBoundingBox(), false)) {
                     this.holdAtEdge(player, footing);
                     this.recoveryInput = this.stayOnSupportInput(player, footing, centringInput(player.getBoundingBox(), footing, this.anchorYaw, 0.0));
                     return;
                  }

                  this.f_Z_I = AutismSessionState.getInstance().getClientTick();
               }

               if (!this.init_Z_LocalPlayer_2(player, first, flatEntry)) {
                  this.holdAtEdge(player, this.supportBlockUnder(player));
               } else {
                  this.clearEntryCells();
                  this.turnPhase = this.isEagleMode() ? ScaffoldModule.TurnCyclePhase.PLACING : ScaffoldModule.TurnCyclePhase.FORWARD;
                  this.quantizedPlacement = null;
                  this.close_Z = false;
                  this.pop_J++;
                  this.clear_Z = (this.set_Z || this.spaceHeld) && !flatEntry;
                  this.write_BlockPos = this.clear_Z && !this.isEagleMode() && this.spaceHeld ? footing.relative(this.anchorDirection(), 2).immutable() : null;
                  this.batchPower = Math.max(0, batchPower);
                  if (this.spaceHeld) {
                     this.reset_Z = true;
                  }

                  this.smoother().init(this.smootherBand(), !this.isEagleMode());
                  this.cachedSmootherBand = null;
                  this.update_Z = false;
                  this.imperfectionArmed = this.placeRateTracker.init(!this.isEagleMode() && !flatEntry ? this.integer("imperfection") : 0, this.clear_Z, this.random);
                  this.imperfectionCount = this.placeRateTracker.init_I();
                  this.imperfectionRequested = false;
                  this.imperfectionCell = null;
                  this.imperfectionSupports = init_List_BlockPos_2(footing, this.anchorDirection(), this.placeRateTracker.init_I());
                  this.set_Z = false;
                  this.add_Z = false;
                  this.cache_Z = false;
                  this.diagonalBridge = false;
                  this.queue_Z = false;
                  this.start_Z = false;
                  this.close_BlockPos = null;
                  this.f_I_I = 0;
                  this.next_Z = false;
                  this.walkOffGraceTicks = 0;
                  this.launchY = player.getY();
                  this.launchProgress = player.position().dot(this.anchorForward());
                  this.pushFooting = null;
                  this.f_J_I = 0;
                  this.tellyAirTicks = 0;
                  this.buffer_Z = this.clear_Z && (this.write_BlockPos == null || this.batchPower == 0) || this.placeRateTracker.init_I() > 1;
                  this.f_S_I = 0;
                  this.livePlacement = first;
                  this.data_Z = true;
                  this.tellyPhase = ScaffoldModule.TellyPhase.LAUNCH;
                  this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
                  this.resetSteering();
                  this.resetStrafeState();
                  this.state_V();
                  this.run_Z = true;
               }
            }
         } else if (footing != null) {
            this.enterTellyRecovery(player, footing);
         } else {
            this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
         }
      }
   }

   static boolean canJumpNow(boolean grounded, int jumpDelay) {
      return grounded && jumpDelay <= 1;
   }

   private void state_V() {
      this.emittedSideways = 0;
      this.f_aS_I = 0;
      this.f_a_Z = false;
   }

   private void tickTellyAirborne(LocalPlayer player) {
      this.cache_Z = false;
      this.diagonalBridge = false;
      this.tellyAirTicks++;
      if (this.laneSupport == null) {
         this.chunk_V();
      } else {
         if (this.tellyPhase == ScaffoldModule.TellyPhase.RUNNING || this.tellyPhase == ScaffoldModule.TellyPhase.FORWARD_DWELL) {
            if (!this.next_Z() && this.init_Z_LocalPlayer_I(player, this.supportY)) {
               this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
               return;
            }

            this.sync_V(player);
         }

         this.forwardKeyState = !this.pop_Z && !this.add_Z && !this.buffer_Z ? ScaffoldModule.ForwardKeyState.FORWARD : ScaffoldModule.ForwardKeyState.HOLD;
         ItemStack stack = this.placeableStack();
         if (player.horizontalCollision
            || player.verticalCollision && player.getDeltaMovement().y <= 0.0
            || this.projectLedge(player, this.supportY + (this.start_Z ? 2.0 : 1.0)).init_Z()) {
            this.queue_Z = true;
            if (!this.start_Z) {
               this.clear_Z = false;
               if (!this.stop_Z && this.livePlacement != null && this.livePlacement.init_Z()) {
                  this.livePlacement = null;
               }
            }
         }

         if (this.start_Z && this.close_BlockPos != null && !this.isSettledSolid(MC.level.getBlockState(this.close_BlockPos), this.close_BlockPos)) {
            this.start_Z = false;
            if (this.laneSupport != null && this.laneSupport.getY() > this.supportY) {
               this.laneSupport = new BlockPos(this.laneSupport.getX(), this.supportY, this.laneSupport.getZ());
            }

            this.close_BlockPos = null;
         }

         if (this.start_Z
            && this.close_BlockPos != null
            && !this.stop_Z
            && player.getDeltaMovement().y < -0.035
            && !this.init_Z_LocalPlayer_I(player, this.supportY + 1)
            && !init_Z_Vec3_7(player.position(), player.getDeltaMovement(), player.getBoundingBox(), this.close_BlockPos, this.horizontalHeading(player))
            )
          {
            BlockPos lowerSupport = this.close_BlockPos.below();
            this.start_Z = false;
            this.close_BlockPos = null;
            this.clear_Z = false;
            this.livePlacement = null;
            this.tellyPhase = ScaffoldModule.TellyPhase.AIMING;
            if (this.isSettledSolid(MC.level.getBlockState(lowerSupport), lowerSupport)) {
               this.laneSupport = lowerSupport;
            }
         }

         if (this.next_Z()) {
            this.write_V_LocalPlayer(player);
         } else {
            int landingBlockY = this.start_Z ? this.supportY + 1 : this.supportY;
            boolean descending = player.getDeltaMovement().y < -0.035;
            boolean landingSecured = this.init_Z_LocalPlayer_I(player, landingBlockY);
            if (!landingSecured && !this.node_Z && this.write_Z_LocalPlayer_I(player, landingBlockY)) {
               this.buffer_Z = true;
               this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
               landingSecured = this.init_Z_LocalPlayer_I(player, landingBlockY);
            }

            if (!landingSecured) {
               this.updateStrafeCorrection(player, landingBlockY + 1.0);
            } else {
               this.strafeCooldown = Math.max(0, this.strafeCooldown - 1);
            }

            if (!landingSecured && this.start_Z && this.close_Z(player)) {
               this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
            }

            boolean turnPending = this.isAnchorDeviating();
            boolean reservePending = turnPending || this.sneakHold.load();
            boolean turnReserveCovered = !reservePending || landingSecured && this.init_Z_LocalPlayer(player, landingBlockY, init_I_Z_2(turnPending));
            boolean chainCovered = isLandingCovered(landingSecured, reservePending, turnReserveCovered);
            if (chainCovered && this.tellyPhase != ScaffoldModule.TellyPhase.RETURNING) {
               chainCovered = this.load_Z_LocalPlayer_I(player, landingBlockY);
            }

            boolean walkOffGrace = this.next_Z && this.walkOffGraceTicks > 0;
            if (this.walkOffGraceTicks > 0) {
               this.walkOffGraceTicks--;
            }

            if (!landingSecured
               && !walkOffGrace
               && !this.stop_Z
               && this.f_G_I <= 0
               && (this.update_Z_LocalPlayer(player) || this.isWithinCatchHeight(player, descending))) {
               this.init_Z_LocalPlayer_ItemStack(player, stack);
            }

            if (!landingSecured && this.f_J_I >= 3) {
               this.beginTellyReturn(player);
            } else if (!landingSecured && !walkOffGrace && this.hasFallenTooFar(player, descending)) {
               if (!this.stop_Z && this.f_G_I <= 0 && !this.init_Z_LocalPlayer_ItemStack(player, stack)) {
                  this.beginTellyReturn(player);
               }
            } else if (!this.isPlaceableBlock(stack)) {
               this.beginTellyReturn(player);
            } else {
               ScaffoldModule.TellyPhase coveredPhase = nextTellyPhase(
                  this.tellyPhase, chainCovered, this.clear_Z && !this.start_Z || this.tick(player)
               );
               if (coveredPhase == ScaffoldModule.TellyPhase.RETURNING && this.tellyPhase != ScaffoldModule.TellyPhase.RETURNING) {
                  this.livePlacement = null;
                  this.tellyPhase = coveredPhase;
               } else {
                  if (landingSecured && this.clear_Z && !this.start_Z && player.getDeltaMovement().y < 0.0 && player.getY() < this.supportY + 2.0) {
                     this.clear_Z = false;
                     this.livePlacement = null;
                     if (chainCovered) {
                        this.tellyPhase = ScaffoldModule.TellyPhase.RETURNING;
                        return;
                     }
                  }

                  if (this.tellyPhase != ScaffoldModule.TellyPhase.RETURNING) {
                     if (this.livePlacement == null) {
                        if (chainCovered) {
                           if ((!this.clear_Z || this.start_Z) && !this.tick(player)) {
                              this.tellyPhase = nextTellyPhase(this.tellyPhase, true, false);
                           }

                           return;
                        }

                        if (this.open_Z(player) && this.load_BlockPos_LocalPlayer(player) != null) {
                           return;
                        }

                        this.livePlacement = this.planLanePlacement(player, stack);
                     }

                     if (this.livePlacement != null) {
                        this.tellyPhase = ScaffoldModule.TellyPhase.AIMING;
                        this.slotSwitched = false;
                        InteractionHand hand = this.placeHand();
                        if (hand == null) {
                           if (this.slotSwitched) {
                              this.requestSwitchBack();
                           }
                        } else {
                           this.requestSwitchBack();
                           ItemStack held = player.getItemInHand(hand);
                           if (this.isPlaceableBlock(held)) {
                              landingBlockY = this.start_Z ? this.supportY + 1 : this.supportY;
                              landingSecured = this.init_Z_LocalPlayer_I(player, landingBlockY);
                              turnPending = this.isAnchorDeviating();
                              reservePending = turnPending || this.sneakHold.load();
                              turnReserveCovered = !reservePending || landingSecured && this.init_Z_LocalPlayer(player, landingBlockY, init_I_Z_2(turnPending));
                              chainCovered = isLandingCovered(landingSecured, reservePending, turnReserveCovered);
                              if (chainCovered && this.tellyPhase != ScaffoldModule.TellyPhase.RETURNING) {
                                 chainCovered = this.load_Z_LocalPlayer_I(player, landingBlockY);
                              }

                              if (chainCovered && (!this.clear_Z || this.start_Z) && !this.tick(player)) {
                                 this.tellyPhase = nextTellyPhase(this.tellyPhase, true, false);
                                 this.livePlacement = null;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean next_Z() {
      return this.pop_Z || this.queue_Z || this.next_Z || this.add_Z;
   }

   private void write_V_LocalPlayer(LocalPlayer player) {
      this.forwardKeyState = !this.pop_Z && !this.queue_Z && !this.add_Z ? ScaffoldModule.ForwardKeyState.FORWARD : ScaffoldModule.ForwardKeyState.HOLD;
      if (!this.start_Z) {
         this.clear_Z = false;
         if (!this.stop_Z && this.livePlacement != null && this.livePlacement.init_Z()) {
            this.livePlacement = null;
         }
      }

      int blockY = this.targetBlockY(player);
      if (this.init_Z_LocalPlayer_I(player, blockY)) {
         this.tellyPhase = ScaffoldModule.TellyPhase.RETURNING;
         if (!this.stop_Z) {
            this.livePlacement = null;
         }
      } else {
         this.updateStrafeCorrection(player, blockY + 1.0);
         if (!this.stop_Z && this.f_G_I <= 0) {
            this.read_Z_LocalPlayer(player);
         }
      }
   }

   private int targetBlockY(LocalPlayer player) {
      int intended = this.supportY + (this.start_Z ? 1 : 0);
      return Math.min(intended, Mth.floor(player.getY() + 1.0E-9) - 1);
   }

   private boolean read_Z_LocalPlayer(LocalPlayer player) {
      if (!this.stop_Z && this.f_G_I <= 0) {
         int blockY = this.targetBlockY(player);
         if (this.init_Z_LocalPlayer_I(player, blockY)) {
            return false;
         }

         this.slotSwitched = false;
         InteractionHand hand = this.placeHand();
         if (hand == null) {
            if (this.slotSwitched) {
               this.requestSwitchBack();
            }

            return this.slotSwitched;
         } else {
            this.requestSwitchBack();
            ItemStack held = player.getItemInHand(hand);
            if (!this.isPlaceableBlock(held)) {
               return false;
            }

            Vec3 landing = this.projectLedge(player, blockY + 1.0).point();
            AABB footprint = player.getBoundingBox().move(landing.x - player.getX(), blockY + 1.0 - player.getY(), landing.z - player.getZ());
            ScaffoldModule.LivePlacement best = null;
            double bestCost = 1.0 / 0.0;

            for (int x = Mth.floor(footprint.minX + 1.0E-9); x <= Mth.floor(footprint.maxX - 1.0E-9); x++) {
               for (int z = Mth.floor(footprint.minZ + 1.0E-9); z <= Mth.floor(footprint.maxZ - 1.0E-9); z++) {
                  BlockPos cell = new BlockPos(x, blockY, z);
                  if (overlapsBlock(footprint, cell)
                     && !MC.level.isOutsideBuildHeight(cell)
                     && this.canReplaceWith(MC.level.getBlockState(cell), cell, held)
                     && !intersectsCell(cell, player.getBoundingBox())) {
                     for (Direction face : Direction.values()) {
                        BlockPos support = cell.relative(face.getOpposite());
                        if (this.isSettledSolid(MC.level.getBlockState(support), support)) {
                           Vec3 aim = faceCentre(supportBox(support), face);
                           ScaffoldModule.LivePlacement candidate = new ScaffoldModule.LivePlacement(
                              new ScaffoldModule.PlannedPlacement(
                                 support.immutable(),
                                 cell.immutable(),
                                 face,
                                 new BlockHitResult(aim, face, support, false),
                                 RotationMath.lookAt(aim, player.getEyePosition()),
                                 blockY
                              ),
                              false
                           );
                           ScaffoldModule.LivePlacement live = this.refineLivePlacement(player, candidate);
                           if (live != null
                              && this.hitPlacesPending(live.plan().hit(), live.plan())
                              && !al.init_Z_HitResult(live.plan().hit(), hand, this.id())) {
                              double cost = Vec3.atBottomCenterOf(cell.above()).distanceToSqr(landing)
                                 + angleBetween(this.lastSentRotation(), live.plan().rotation()) * 0.001;
                              if (autismclient.util.BlockPlacementPlanner.wouldInteractWithoutSneak(MC.level.getBlockState(support), support, player.isShiftKeyDown())) {
                                 cost++;
                              }

                              if (cost < bestCost) {
                                 bestCost = cost;
                                 best = live;
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (best != null) {
               this.tellyPhase = ScaffoldModule.TellyPhase.AIMING;
               this.livePlacement = best;
               return this.tryPlaceLive(best, hand, held, null, true);
            }

            if (this.livePlacement != null && this.livePlacement.init_Z()) {
               return false;
            }

            this.livePlacement = this.planLanePlacement(player, held);
            boolean pending = this.init_Z_LocalPlayer_ItemStack(player, held);
            if (this.stop_Z) {
               this.tellyPhase = ScaffoldModule.TellyPhase.AIMING;
            }

            return pending;
         }
      } else {
         return true;
      }
   }

   private boolean hasRecentGroundedSupport(LocalPlayer player) {
      if (!this.isPlaceableBlock(this.placeableStack())) {
         return false;
      }

      if (this.groundedSupport == null) {
         return false;
      }

      int sinceGrounded = AutismSessionState.getInstance().getClientTick() - this.groundedSupportTick;
      return sinceGrounded >= 0 && sinceGrounded <= 6 ? !this.hasSupportWithin(player.getX(), player.getY(), player.getZ(), 2) : false;
   }

   private void update_V_LocalPlayer(LocalPlayer player) {
      this.init_V_LocalPlayer_BlockPos(player, this.groundedSupport);
      this.sync_V(player);
   }

   private void sync_V(LocalPlayer player) {
      this.turnPhase = ScaffoldModule.TurnCyclePhase.FORWARD;
      this.quantizedPlacement = null;
      this.close_Z = false;
      this.pop_J++;
      this.clear_Z = false;
      this.start_Z = false;
      this.close_BlockPos = null;
      this.f_I_I = 0;
      this.next_Z = true;
      this.walkOffGraceTicks = 3;
      this.stop_Z = false;
      this.open_BlockPos = null;
      this.data_Z = true;
      this.f_J_I = 0;
      this.tellyAirTicks = Math.max(this.tellyAirTicks, 3);
      this.supportY = this.laneSupport.getY();
      this.launchY = this.supportY + 1.0;
      this.launchProgress = player.position().dot(this.anchorForward()) - 1.45;
      this.tellyPhase = ScaffoldModule.TellyPhase.LAUNCH;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
      this.resetSteering();
      this.resetStrafeState();
   }

   public static void write_V_12() {
      if ((AutismLiteVariant.enabled() || !DangerModule.read_Z_22() && !ClutchModule.read_Z_20())
         && MC != null
         && MC.player != null
         && MC.level != null
         && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()) {
         if (scaffold.flags_Z()) {
            scaffold.buffer_V();
         } else if (scaffold.pool_Z() && scaffold.ownsInput && scaffold.canOperate()) {
            scaffold.count_V();
            if (!MC.player.onGround() || scaffold.isEagleMode()) {
               scaffold.entry_V();
            }
         }
      }
   }

   private static void syncLastWireYaw() {
      float written = ServerRotationTracker.getLastWireYaw();
      if (Float.isFinite(written)) {
         lastSentYaw = written;
      }

      appliedRotationTick = -2147483648;
   }

   public static RotationMath.Rotation queuedClickRotation(Packet<?> packet) {
      if (!(packet instanceof ServerboundUseItemOnPacket)) {
         return null;
      }

      synchronized (queuedPackets) {
         ScaffoldModule.QueuedPacketContext queued = queuedPackets.get(new ScaffoldModule.WeakPacketKey(packet));
         return queued == null ? null : queued.rotation();
      }
   }

   public static boolean init_Z_26() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()
         && scaffold.frame_Z()
         && scaffold.placementState == ScaffoldModule.PlacementState.ARMED
         && scaffold.pendingTick == AutismSessionState.getInstance().getClientTick();
   }

   public static boolean load_Z_11() {
      if (AutismLiteVariant.enabled() || !DangerModule.read_Z_22() && !ClutchModule.read_Z_20()) {
         if (init_Z_26() && !BlinkPacketManager.isHoldingActionsOnly()) {
            Module module = al.init_Module_Str("scaffold");
            ScaffoldModule scaffold = (ScaffoldModule)module;
            return autismclient.util.VirtualInputDispatcher.consumeScaffold(scaffold.activeGeneration);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void init_V_Packet(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket use) {
         synchronized (queuedPackets) {
            expungeCollectedPacketKeys();
            if (queuedPackets.containsKey(new ScaffoldModule.WeakPacketKey(packet))) {
               return;
            }
         }

         long edgeGeneration = autismclient.util.VirtualInputDispatcher.init_J();
         if (edgeGeneration > 0L && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.frame_Z()) {
            ScaffoldModule.PlannedPlacement pending = scaffold.pendingPlacement;
            BlockHitResult hit = use.hitResult();
            if (pending != null
               && scaffold.placementState == ScaffoldModule.PlacementState.ARMED
               && edgeGeneration == scaffold.activeGeneration
               && use.hand() == scaffold.pendingHand
               && hitMatchesPending(hit, pending, scaffold.buildsPlannedCell)) {
               synchronized (queuedPackets) {
                  expungeCollectedPacketKeys();
                  ScaffoldModule.WeakPacketKey lookup = new ScaffoldModule.WeakPacketKey(packet);
                  if (!queuedPackets.containsKey(lookup)) {
                     int ordinal = scaffold.submittedCount++;
                     ScaffoldModule.QueuedPacketContext queued = new ScaffoldModule.QueuedPacketContext(
                        scaffold.activeGeneration,
                        ordinal,
                        use.hand(),
                        pending.load().immutable(),
                        pending.support().immutable(),
                        pending.face(),
                        scaffold.clickRotation
                     );
                     queuedPackets.put(new ScaffoldModule.WeakPacketKey(packet, packetKeyQueue), queued);
                     if (ordinal > 0) {
                        scaffold.duplicateSubmitted = true;
                     }
                  }
               }
            }
         }
      }
   }

   public static void load_V_Packet(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket) {
         synchronized (queuedPackets) {
            expungeCollectedPacketKeys();
            queuedPackets.remove(new ScaffoldModule.WeakPacketKey(packet));
         }
      }
   }

   private void buffer_V() {
      if (this.pendingTick == AutismSessionState.getInstance().getClientTick()
         && this.pendingPlacement != null
         && this.clickRotation != null
         && this.placementState == ScaffoldModule.PlacementState.ARMED) {
         double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
         BlockHitResult ray = raycast(MC.player.getEyePosition(), this.clickRotation, reach, MC.level, MC.player);
         if (ray != null) {
            MC.hitResult = ray;
         }
      }
   }

   private void entry_V() {
      this.tickOncePerClientTick();
      if (isKeyDown(MC.options.keyJump) || this.set_Z) {
         this.level_V();
      }

      if (!this.stop_Z && this.f_G_I <= 0 && !this.run_Z) {
         if (this.quantizedPlacement != null && !this.quantizedPlacement.placement().init_Z() && this.open_Z(MC.player)) {
            ScaffoldModule.LivePlacement rise = this.planRiseFromLanding(MC.player, this.placeableStack());
            if (rise != null && this.refineLivePlacement(MC.player, rise) != null) {
               this.quantizedPlacement = null;
               this.close_Z = false;
            }
         }

         if (this.quantizedPlacement != null) {
            ScaffoldModule.LivePlacement prepared = this.quantizedPlacement.placement();
            boolean optional = prepared.plan().load().equals(this.imperfectionCell);
            if (optional && this.quantizedPlacement.tick() == AutismSessionState.getInstance().getClientTick()) {
               return;
            }

            if (!optional || this.push(MC.player, prepared.plan().support())) {
               InteractionHand preparedHand = this.placeHand();
               if (preparedHand != null && !al.init_Z_HitResult(prepared.plan().hit(), preparedHand, this.id())) {
                  this.tryPlaceLive(prepared, preparedHand, MC.player.getItemInHand(preparedHand), null);
               }

               return;
            }

            this.quantizedPlacement = null;
            this.imperfectionCell = null;
         }

         if (this.next_Z()) {
            this.read_Z_LocalPlayer(MC.player);
         } else {
            ScaffoldModule.LivePlacement optional = this.init_aI_DataPipe_LocalPlayer(MC.player);
            if (optional != null) {
               InteractionHand optionalHand = this.placeHand();
               if (optionalHand != null && !al.init_Z_HitResult(optional.plan().hit(), optionalHand, this.id())) {
                  this.tryPlaceLive(optional, optionalHand, MC.player.getItemInHand(optionalHand), null);
                  return;
               }
            }

            if (!this.init_Z_LocalPlayer_I(MC.player, this.supportY) || !this.tick(MC.player)) {
               this.slotSwitched = false;
               InteractionHand hand = this.placeHand();
               if (hand == null) {
                  if (this.slotSwitched) {
                     this.requestSwitchBack();
                  }
               } else {
                  this.requestSwitchBack();
                  ItemStack held = MC.player.getItemInHand(hand);
                  if (this.isPlaceableBlock(held)) {
                     if (this.open_Z(MC.player) && this.laneSupport != null) {
                        ScaffoldModule.LivePlacement rise = this.planRiseFromLanding(MC.player, held);
                        if (rise != null) {
                           ScaffoldModule.LivePlacement live = this.refineLivePlacement(MC.player, rise);
                           if (live != null && !al.init_Z_HitResult(live.plan().hit(), hand, this.id())) {
                              if (this.tryPlaceLive(live, hand, held, null)
                                 && this.write_Z_LocalPlayer_BlockPos(MC.player, live.plan().load())) {
                                 this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
                              }

                              return;
                           }
                        }
                     }

                     if (this.livePlacement == null
                        && (this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING)
                        && (!this.open_Z(MC.player) || this.load_BlockPos_LocalPlayer(MC.player) == null)) {
                        this.livePlacement = this.planLanePlacement(MC.player, held);
                     }

                     if (this.livePlacement != null) {
                        ScaffoldModule.LivePlacement live = this.refineLivePlacement(MC.player, this.livePlacement);
                        if (live != null && !al.init_Z_HitResult(live.plan().hit(), hand, this.id())) {
                           this.tryPlaceLive(live, hand, held, null);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean hasFallenTooFar(LocalPlayer player, boolean descending) {
      if (!descending) {
         return false;
      }

      double targetTop = this.supportY + 1.0;
      return player.getY() < targetTop + 0.05 || player.getY() < this.launchY - 1.15 || this.tellyAirTicks > 14;
   }

   private boolean isWithinCatchHeight(LocalPlayer player, boolean descending) {
      if (descending && player != null) {
         double catchTop = this.start_Z ? this.supportY + 2.0 : this.supportY + 1.0;
         double fall = Math.max(0.0, -player.getDeltaMovement().y);
         return player.getY() <= catchTop + Math.max(0.35, fall * 2.0);
      } else {
         return false;
      }
   }

   private boolean update_Z_LocalPlayer(LocalPlayer player) {
      double catchTop = this.start_Z ? this.supportY + 2.0 : this.supportY + 1.0;
      double feetY = Math.min(catchTop, player.getY());
      Vec3 landing = projectAirControlLanding(player.position(), player.getDeltaMovement(), feetY, this.horizontalHeading(player));
      double lane = lateralOffset(new Vec3(landing.x, player.getY(), landing.z), this.anchorYaw);
      return Math.abs(lane - this.laneOffset) > 0.4;
   }

   private boolean init_Z_LocalPlayer_ItemStack(LocalPlayer player, ItemStack stack) {
      if (this.stop_Z) {
         return true;
      }

      if (stack == null) {
         return false;
      }

      if (this.livePlacement != null && this.livePlacement.init_Z()) {
         return true;
      }

      this.slotSwitched = false;
      InteractionHand hand = this.placeHand();
      if (hand == null) {
         if (this.slotSwitched) {
            this.requestSwitchBack();
            return true;
         } else {
            return false;
         }
      } else {
         this.requestSwitchBack();
         ItemStack held = player.getItemInHand(hand);
         if (!this.isPlaceableBlock(held)) {
            return false;
         }

         ScaffoldModule.LivePlacement urgent = this.livePlacement;
         if (urgent == null
            || !isForwardExtension(
               urgent.plan().support(),
               urgent.plan().load(),
               urgent.plan().face(),
               this.anchorDirection()
            )) {
            urgent = this.planLanePlacement(player, held);
         }

         if (urgent != null
            && isForwardExtension(
               urgent.plan().support(),
               urgent.plan().load(),
               urgent.plan().face(),
               this.anchorDirection()
            )) {
            this.livePlacement = urgent;
            ScaffoldModule.LivePlacement live = this.refineLivePlacement(player, urgent);
            if (live == null) {
               return true;
            }

            if (al.init_Z_HitResult(live.plan().hit(), hand, this.id())) {
               return false;
            }

            this.tryPlaceLive(live, hand, held, null, true);
            return true;
         } else {
            return false;
         }
      }
   }

   static boolean isForwardExtension(BlockPos support, BlockPos placed, Direction face, Direction forward) {
      return support != null
         && placed != null
         && face != null
         && forward != null
         && face == forward
         && placed.equals(support.relative(forward))
         && placed.getY() == support.getY();
   }

   private void beginTellyReturn(LocalPlayer player) {
      this.add_Z = true;
      if (!this.stop_Z) {
         this.livePlacement = null;
      }

      this.tellyPhase = ScaffoldModule.TellyPhase.RETURNING;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
      this.write_V_LocalPlayer(player);
   }

   private void tickOncePerClientTick() {
      if (MC != null && MC.player != null && MC.level != null) {
         this.resetOnLevelChange();
         int tick = AutismSessionState.getInstance().getClientTick();
         if (tick != this.lastTickedClientTick) {
            this.lastTickedClientTick = tick;
            this.tickPendingPlacement();
            if (this.f_G_I > 0) {
               this.f_G_I--;
            }
         }
      }
   }

   private void tickPendingPlacement() {
      if (this.isTellyOrEagleMode()) {
         this.drainWrittenClicks();
         if (this.level_Z()) {
            this.drain_V();
            int age = Math.max(0, AutismSessionState.getInstance().getClientTick() - this.pendingTick);
            if (isArmedWithoutSubmit(this.placementState, age, this.resultSeen, this.submittedCount)) {
               autismclient.util.VirtualInputDispatcher.update_V();
               this.init_V_Str_3("input-not-consumed");
            }

            if (this.placementState != ScaffoldModule.PlacementState.IDLE && this.placementState != ScaffoldModule.PlacementState.FAILED) {
               boolean supported = this.pendingPlacement != null
                  && this.isSettledSolid(MC.level.getBlockState(this.pendingPlacement.load()), this.pendingPlacement.load());
               ScaffoldModule.StateDecision decision = decidePlacementState(
                  this.placementState, age, this.resultSeen, this.resultConsumed, supported, isAcknowledged(this.pendingSequence, this.ackedSequence)
               );
               if (decision.state() == ScaffoldModule.PlacementState.FAILED) {
                  this.init_V_Str_3(decision.reason());
               } else {
                  this.placementState = decision.state();
               }
            }

            this.pool_V();
            if (this.placementState != ScaffoldModule.PlacementState.IDLE
               && (this.placementState == ScaffoldModule.PlacementState.PREDICTED || this.placementState == ScaffoldModule.PlacementState.FAILED || age >= 2)) {
               autismclient.util.VirtualInputDispatcher.update_V();
               this.resetPlacementState();
            }
         }
      }
   }

   static boolean isArmedWithoutSubmit(ScaffoldModule.PlacementState state, int age, boolean resultSeen, int submitted) {
      return state == ScaffoldModule.PlacementState.ARMED && age > 0 && !resultSeen && submitted == 0;
   }

   static boolean init_Z_Z_2(boolean turnReserve, boolean resultSeen, int submitted) {
      return !turnReserve && (resultSeen || submitted > 0);
   }

   static ScaffoldModule.PlacementAckStatus ackStatusFor(ScaffoldModule.PlacementState state, int age, boolean resultSeen, boolean consumed, boolean supported) {
      if (state != null && state != ScaffoldModule.PlacementState.IDLE && state != ScaffoldModule.PlacementState.FAILED && (!resultSeen || consumed)) {
         if (resultSeen && consumed && supported && state != ScaffoldModule.PlacementState.ARMED) {
            return ScaffoldModule.PlacementAckStatus.PLACED;
         } else {
            return age >= 2 ? ScaffoldModule.PlacementAckStatus.FAILED : ScaffoldModule.PlacementAckStatus.WAITING;
         }
      } else {
         return ScaffoldModule.PlacementAckStatus.FAILED;
      }
   }

   private void pool_V() {
      if (this.stop_Z && this.open_BlockPos != null) {
         int age = Math.max(0, AutismSessionState.getInstance().getClientTick() - this.pendingTick);
         ScaffoldModule.PlacementAckStatus status = ackStatusFor(
            this.placementState,
            age,
            this.resultSeen,
            this.resultConsumed,
            this.isSettledSolid(MC.level.getBlockState(this.open_BlockPos), this.open_BlockPos)
         );
         if (status != ScaffoldModule.PlacementAckStatus.WAITING) {
            this.stop_Z = false;
            BlockPos placed = this.open_BlockPos;
            this.open_BlockPos = null;
            boolean turnReserve = this.pool_Z && this.entryCell != null && this.entryCell.equals(placed);
            this.pool_Z = false;
            boolean imperfection = this.imperfectionRequested;
            this.imperfectionRequested = false;
            if (status == ScaffoldModule.PlacementAckStatus.FAILED) {
               if (!imperfection && init_Z_Z_2(turnReserve, this.resultSeen, this.submittedCount)) {
                  this.f_J_I++;
               }

               this.livePlacement = null;
            } else {
               if (this.pendingPlacement != null) {
                  this.recordPrediction(this.pendingSequence, placed, this.pendingPlacement.support());
               }

               PlacedBlockTrailRenderer.init(placed);
               if (imperfection) {
                  this.placeRateTracker.init_V();
                  this.imperfectionArmed = this.placeRateTracker.init_Z();
                  this.imperfectionCell = null;
                  this.livePlacement = null;
               } else if (turnReserve) {
                  this.f_J_I = 0;
                  this.livePlacement = null;
                  this.clearEntryCells();
               } else {
                  boolean raised = this.livePlacement != null
                     && this.livePlacement.plan().load().equals(placed)
                     && this.livePlacement.init_Z();
                  this.laneSupport = placed.immutable();
                  if (raised) {
                     this.start_Z = true;
                     this.close_BlockPos = placed.immutable();
                  } else {
                     this.f_I_I++;
                     this.supportY = Math.min(this.supportY, placed.getY());
                  }

                  this.f_J_I = 0;
                  this.livePlacement = null;
               }
            }
         }
      }
   }

   private void update_V_BlockPos(BlockPos under) {
      this.write_BlockPos = null;
      this.cachedSmootherBand = null;
      this.resetImperfection();
      this.turnPhase = ScaffoldModule.TurnCyclePhase.FORWARD;
      this.quantizedPlacement = null;
      this.close_Z = false;
      this.tellyPhase = ScaffoldModule.TellyPhase.FORWARD_DWELL;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
      this.resetStrafeState();
      this.tellyAirTicks = 0;
      this.buffer_Z = false;
      this.f_I_I = 0;
      this.f_J_I = 0;
      this.f_K_I = 0;
      this.clear_Z = false;
      this.start_Z = false;
      this.close_BlockPos = null;
      this.stop_Z = false;
      this.next_Z = false;
      this.walkOffGraceTicks = 0;
      this.queue_Z = false;
      this.open_BlockPos = null;
      this.livePlacement = null;
      this.data_Z = true;
      this.laneSupport = under.immutable();
      this.supportY = under.getY();
   }

   private boolean isNearEdge(LocalPlayer player, BlockPos under) {
      Direction direction = this.anchorDirection();
      BlockPos ahead = under.relative(direction);
      if (this.isSettledSolid(MC.level.getBlockState(ahead), ahead)) {
         return false;
      }

      double var10000;
      switch (direction) {
         case EAST:
            var10000 = under.getX() + 1.0 - player.getX();
            break;
         case WEST:
            var10000 = player.getX() - under.getX();
            break;
         case SOUTH:
            var10000 = under.getZ() + 1.0 - player.getZ();
            break;
         case NORTH:
            var10000 = player.getZ() - under.getZ();
            break;
         default:
            var10000 = 1.0 / 0.0;
      }

      double remaining = var10000;
      Vec3 forward = this.anchorForward();
      Vec3 velocity = player.getDeltaMovement();
      double forwardSpeed = Math.max(0.0, velocity.x * forward.x + velocity.z * forward.z);
      if (!this.isEagleMode() && !this.spaceHeld && !this.set_Z && this.flush_I() <= 1) {
         double acceleration = adjustedMoveSpeed(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), this.canSprint(player));
         return remaining <= read_D_D_D(forwardSpeed, acceleration);
      } else {
         return isAtEdgeMargin(remaining, forwardSpeed);
      }
   }

   static double read_D_D_D(double speed, double acceleration) {
      return Math.max(Math.max(0.2, 0.18 + speed * 0.5), speed + acceleration - 0.14);
   }

   static boolean isAtEdgeMargin(double supportRemaining, double forwardSpeed) {
      return supportRemaining <= edgeMargin(forwardSpeed);
   }

   private static double edgeMargin(double forwardSpeed) {
      return Mth.clamp(0.52 + Math.max(0.0, forwardSpeed) * 0.12, 0.52, 0.64);
   }

   private BlockPos supportBlockUnder(LocalPlayer player) {
      AABB box = player.getBoundingBox();
      int y = Mth.floor(player.getY() - 0.08);
      BlockPos best = null;
      double bestDistance = 1.0 / 0.0;
      int minX = Mth.floor(box.minX + 1.0E-9);
      int maxX = Mth.floor(box.maxX - 1.0E-9);
      int minZ = Mth.floor(box.minZ + 1.0E-9);
      int maxZ = Mth.floor(box.maxZ - 1.0E-9);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (this.isSettledSolid(MC.level.getBlockState(pos), pos)) {
               double distance = Vec3.atCenterOf(pos).distanceToSqr(player.position());
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = pos.immutable();
               }
            }
         }
      }

      return best;
   }

   private ScaffoldModule.LivePlacement planLanePlacement(LocalPlayer player, ItemStack stack) {
      if (this.laneSupport != null && this.laneCentrePoint != null && this.isPlaceableBlock(stack)) {
         BlockPos support = this.effectiveLaneSupport();
         return this.planForwardPlacement(player, stack, support);
      } else {
         return null;
      }
   }

   private ScaffoldModule.LivePlacement planForwardPlacement(LocalPlayer player, ItemStack stack, BlockPos support) {
      if (!this.isPlaceableBlock(stack)) {
         return null;
      }

      if (support == null) {
         return null;
      }

      Direction direction = this.anchorDirection();

      for (int i = 0; i < 8; i++) {
         BlockPos next = support.relative(direction);
         if (MC.level.isOutsideBuildHeight(next)) {
            return null;
         }

         BlockState state = MC.level.getBlockState(next);
         if (!this.isSettledSolid(state, next)) {
            if (!state.isAir() && !state.canBeReplaced()) {
               return null;
            }

            Vec3 aim = this.faceAimPoint(support, direction);
            ScaffoldModule.PlannedPlacement target = new ScaffoldModule.PlannedPlacement(
               support.immutable(),
               next.immutable(),
               direction,
               new BlockHitResult(aim, direction, support, false),
               RotationMath.lookAt(aim, player.getEyePosition()),
               support.getY()
            );
            return new ScaffoldModule.LivePlacement(target, false);
         }

         support = next;
      }

      return null;
   }

   private ScaffoldModule.LivePlacement planRisePlacement(LocalPlayer player, ItemStack stack, BlockPos support) {
      if (support != null && this.isPlaceableBlock(stack)) {
         BlockPos raised = support.above();
         if (MC.level.isOutsideBuildHeight(raised)) {
            return null;
         }

         BlockState state = MC.level.getBlockState(raised);
         if (!state.isAir() && !state.canBeReplaced()) {
            return null;
         }

         AABB box = supportBox(support);
         Vec3 aim = faceCentre(box, Direction.UP);
         ScaffoldModule.PlannedPlacement target = new ScaffoldModule.PlannedPlacement(
            support.immutable(),
            raised.immutable(),
            Direction.UP,
            new BlockHitResult(aim, Direction.UP, support, false),
            RotationMath.lookAt(aim, player.getEyePosition()),
            box.maxY - 0.1 * box.getYsize()
         );
         return new ScaffoldModule.LivePlacement(target, true);
      } else {
         return null;
      }
   }

   private Vec3 faceAimPoint(BlockPos support, Direction direction) {
      double faceY = Mth.clamp(0.78 - this.f_I_I * 0.11, 0.28, 0.78);
      AABB box = supportBox(support);
      Vec3 centre = faceCentre(box, direction);
      return new Vec3(centre.x, box.minY + faceY * box.getYsize(), centre.z);
   }

   private boolean sync_Z(LocalPlayer player) {
      BlockPos support = this.entrySupport;
      BlockPos cell = this.entryCell;
      Direction direction = horizontalDirectionTo(support, cell);
      if (support != null && cell != null && direction != null) {
         if (this.isSettledSolid(MC.level.getBlockState(cell), cell)) {
            if (this.livePlacement != null && cell.equals(this.livePlacement.plan().load())) {
               this.livePlacement = null;
            }

            this.clearEntryCells();
            return false;
         } else {
            if (!this.isSettledSolid(MC.level.getBlockState(support), support)) {
               this.clearEntryCells();
               return false;
            }

            BlockState targetState = MC.level.getBlockState(cell);
            if (!targetState.isAir() && !targetState.canBeReplaced()) {
               this.clearEntryCells();
               return false;
            }

            if (!this.stop_Z && this.f_G_I <= 0) {
               this.slotSwitched = false;
               InteractionHand hand = this.placeHand();
               if (hand != null) {
                  this.requestSwitchBack();
                  ItemStack held = player.getItemInHand(hand);
                  if (!this.isPlaceableBlock(held)) {
                     return false;
                  }

                  Vec3 aim = this.faceAimPoint(support, direction);
                  ScaffoldModule.PlannedPlacement target = new ScaffoldModule.PlannedPlacement(
                     support, cell, direction, new BlockHitResult(aim, direction, support, false), RotationMath.lookAt(aim, player.getEyePosition()), support.getY()
                  );
                  ScaffoldModule.LivePlacement placement = new ScaffoldModule.LivePlacement(target, false);
                  this.livePlacement = placement;
                  ScaffoldModule.LivePlacement live = this.refineLivePlacement(player, placement);
                  if (live == null) {
                     return true;
                  }

                  if (al.init_Z_HitResult(live.plan().hit(), hand, this.id())) {
                     return false;
                  }

                  if (this.tryPlaceLive(live, hand, held, cell)) {
                     this.pool_Z = true;
                  }

                  return true;
               } else if (this.slotSwitched) {
                  this.requestSwitchBack();
                  return true;
               } else {
                  return false;
               }
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   static Direction horizontalDirectionTo(BlockPos support, BlockPos cell) {
      if (support != null && cell != null && support.getY() == cell.getY()) {
         for (Direction direction : Plane.HORIZONTAL) {
            if (support.relative(direction).equals(cell)) {
               return direction;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static boolean isOverhangCell(BlockPos support, BlockPos cell, Direction overhang) {
      return overhang != null && overhang == horizontalDirectionTo(support, cell);
   }

   static boolean intersectsCell(BlockPos cell, AABB playerBox) {
      return cell != null && playerBox != null && new AABB(cell).intersects(playerBox);
   }

   private boolean tryPlaceLive(ScaffoldModule.LivePlacement live, InteractionHand hand, ItemStack held, BlockPos queuedCell) {
      return this.run_Z ? false : this.tryPlaceLive(live, hand, held, queuedCell, false);
   }

   private boolean tryPlaceLive(ScaffoldModule.LivePlacement live, InteractionHand hand, ItemStack held, BlockPos queuedCell, boolean lastChance) {
      this.close_Z = false;
      if (!MC.player.onGround() && !this.next_Z()) {
         if (this.turnPhase == ScaffoldModule.TurnCyclePhase.RETURNED && !lastChance) {
            return false;
         }

         if (this.turnPhase == ScaffoldModule.TurnCyclePhase.FORWARD && !this.pop(MC.player)) {
            return false;
         }
      }

      if (this.turnPhase == ScaffoldModule.TurnCyclePhase.RETURNED && (lastChance || this.next_Z())) {
         this.turnPhase = ScaffoldModule.TurnCyclePhase.PLACING;
         this.quantizedPlacement = null;
         this.close_Z = false;
         this.tellyPhase = ScaffoldModule.TellyPhase.AIMING;
      }

      BlockState supportState = MC.level.getBlockState(live.plan().support());
      if (!autismclient.util.BlockPlacementPlanner.requiresSneakToPlace(supportState, live.plan().support())
         || isKeyDown(MC.options.keyShift) && MC.player.isShiftKeyDown()) {
         if (intersectsCell(live.plan().load(), MC.player.getBoundingBox())) {
            return false;
         }

         boolean prepared = this.close(live.plan());
         boolean imperfection = live.plan().load().equals(this.imperfectionCell);
         float cap = this.rotationSpeedCap();
         ScaffoldModule.PlannedPlacement aimed = this.init_aI_TableIndex_aI_TableIndex(live.plan());
         if (aimed == null) {
            aimed = this.smoothAim(live.plan(), MC.player.getEyePosition());
         }

         if (aimed != null) {
            aimed = this.init_aI_TableIndex_aI_TableIndex_F(aimed, cap);
         }

         if (aimed == null || !prepared) {
            ScaffoldModule.AimGoal preview = this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(MC.player, live);
            if (preview != null) {
               syncLastWireYaw();
               this.aimTowards(preview.rotation());
            }

            return false;
         } else {
            if (!this.flush(aimed)) {
               this.close_Z = false;
               return false;
            }

            if (!MC.player.onGround()) {
               this.turnPhase = ScaffoldModule.TurnCyclePhase.PLACING;
            }

            syncLastWireYaw();
            this.commitAim(aimed.rotation());
            if (!this.submitPlacement(aimed, hand)) {
               this.close_Z = false;
               return false;
            }

            this.quantizedPlacement = null;
            this.livePlacement = live;
            this.stop_Z = true;
            this.open_BlockPos = queuedCell != null ? queuedCell : aimed.load().immutable();
            this.imperfectionRequested = imperfection;
            if (imperfection) {
               this.imperfectionArmed = false;
            }

            this.f_G_I = 1;
            this.captureTellyTrace("PLACE");
            return true;
         }
      } else {
         return false;
      }
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_aI_TableIndex_F(ScaffoldModule.PlannedPlacement target, float cap) {
      if (AutismLiteVariant.enabled()) {
         return target;
      }

      if (!MC.player.onGround() && target.face().getAxis() != Axis.Y && target.support().getY() == target.load().getY()) {
         Vec3 eye = MC.player.getEyePosition();
         Vec3 motion = MC.player.getDeltaMovement().add(this.airControlVector().scale(0.0196));
         Vec3 next = eye.add(this.collideMovement(MC.player, MC.player.getBoundingBox(), motion));
         double reach = MC.player.blockInteractionRange() - 0.035;
         if (ClutchBlockPlanner.init_Z_Vec3_2(
            next, Vec3.directionFromRotation(target.rotation().pitch(), target.rotation().yaw()), target.load(), target.face(), reach
         )) {
            return target;
         }

         RotationMath.Rotation from = this.lastSentRotation();

         for (ClutchBlockPlanner.AimAngles counts : ClutchBlockPlanner.init_List_Vec3_2(
            eye,
            next,
            ClutchBlockPlanner.init(target.support(), target.face()),
            target.support(),
            target.face(),
            target.load(),
            target.face(),
            reach,
            from.yaw(),
            from.pitch(),
            RotationMath.gcdStep(),
            0.02
         )) {
            RotationMath.Rotation rotation = new RotationMath.Rotation(from.yaw() + Mth.wrapDegrees(counts.init() - from.yaw()), counts.load());
            if (!(Math.abs(Mth.wrapDegrees(rotation.yaw() - from.yaw())) > cap)
               && !(Math.abs(rotation.pitch() - from.pitch()) > cap)
               && !(Math.abs(rotation.pitch()) > 89.5)) {
               BlockHitResult ray = raycast(eye, rotation, reach, MC.level, MC.player);
               if (ray != null
                  && ray.getBlockPos().equals(target.support())
                  && ray.getDirection() == target.face()
                  && this.isHitOnSupportFace(ray)) {
                  return new ScaffoldModule.PlannedPlacement(target.support(), target.load(), target.face(), ray, rotation, target.minHitY());
               }
            }
         }

         return target;
      } else {
         return target;
      }
   }

   private boolean submitPlacement(ScaffoldModule.PlannedPlacement aimed, InteractionHand hand) {
      if (MC.player == null || aimed == null || hand == null) {
         return false;
      }

      if (!this.flush(aimed)) {
         return false;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.lastPlaceTick || this.isPlacementInFlight()) {
         return false;
      }

      if (BlinkPacketManager.isHoldingActionsOnly()) {
         return false;
      }

      this.pendingPlacement = aimed;
      this.pendingLine = this.courseLine;
      this.pendingFallOff = null;
      this.pendingTick = tick;
      this.placementState = ScaffoldModule.PlacementState.ARMED;
      this.generationCounter = incrementNonNegative(this.generationCounter);
      this.activeGeneration = this.generationCounter;
      this.pendingHand = hand;
      this.useSlot = hand == InteractionHand.MAIN_HAND ? MC.player.getInventory().getSelectedSlot() : -1;
      if (this.useSlot >= 0) {
         this.trackedSlotCount(this.useSlot);
      }

      this.buildsPlannedCell = this.hitPlacesPending(aimed.hit(), aimed);
      this.submittedCount = 0;
      this.duplicateSubmitted = false;
      this.f_aL_I = 0;
      this.clickRotation = aimed.rotation();
      this.submitRotation = this.activeRotation;
      this.pendingSequence = -1;
      this.resultSeen = false;
      this.resultConsumed = false;
      this.clickWritten = false;
      this.pendingStatus = "queued";
      this.lastPlaceTick = tick;
      this.writtenClickNanos = -9223372036854775808L;
      if (this.close_Z) {
         PlacementTickStamp.stamp();
      }

      MC.hitResult = aimed.hit();
      autismclient.util.VirtualInputDispatcher.requestScaffold(this.activeGeneration);
      return true;
   }

   private void cancelPendingPlacement() {
      if (this.placementState != ScaffoldModule.PlacementState.IDLE || this.pendingPlacement != null) {
         autismclient.util.VirtualInputDispatcher.update_V();
         this.resetPlacementState();
      }
   }

   private void count_V() {
      if (this.pendingTick == AutismSessionState.getInstance().getClientTick() && this.pendingPlacement != null && this.placementState == ScaffoldModule.PlacementState.ARMED) {
         if (this.flush(this.pendingPlacement)) {
            MC.hitResult = this.pendingPlacement.hit();
         } else {
            boolean unused = !this.resultSeen && this.submittedCount == 0;
            this.cancelPendingPlacement();
            this.stop_Z = false;
            this.open_BlockPos = null;
            this.pool_Z = false;
            this.livePlacement = null;
            if (unused) {
               this.lastPlaceTick = -2147483648;
            }
         }
      }
   }

   private boolean flush(ScaffoldModule.PlannedPlacement target) {
      Vec3 writtenPosition = lastWrittenPosition;
      if (MC.player != null && MC.level != null && writtenPosition != null && target != null) {
         Vec3 eye = MC.player.getEyePosition();
         Vec3 wireEye = writtenPosition.add(0.0, MC.player.getEyeHeight(), 0.0);
         double reach = MC.player.blockInteractionRange();
         if (!isWithinReach(eye, wireEye, target.hit().getLocation(), reach)) {
            return false;
         } else if (this.close(target) && this.rotationReachesFace(wireEye, target.rotation(), target.support(), target.face(), reach)) {
            BlockHitResult liveRay = this.raycastRotation(target.rotation(), reach);
            return liveRay != null
               && liveRay.getBlockPos().equals(target.support())
               && liveRay.getDirection() == target.face()
               && this.isHitOnSupportFace(liveRay)
               && cellClearOfBody(MC.player.getBoundingBox(), writtenPosition.subtract(MC.player.position()), target.load());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean close(ScaffoldModule.PlannedPlacement target) {
      ScaffoldModule.WireClickRotation written = this.wireClickRotation();
      Vec3 position = lastWrittenPosition;
      return target != null
         && MC.player != null
         && written != null
         && position != null
         && this.rotationReachesFace(
            position.add(0.0, MC.player.getEyeHeight(), 0.0),
            written.load(),
            target.support(),
            target.face(),
            MC.player.blockInteractionRange()
         );
   }

   private boolean rotationReachesFace(Vec3 eye, RotationMath.Rotation rotation, BlockPos support, Direction face, double reach) {
      if (eye != null
         && rotation != null
         && support != null
         && face != null
         && Double.isFinite(reach)
         && !(reach <= 0.035)
         && Float.isFinite(rotation.yaw())
         && Float.isFinite(rotation.pitch())) {
         BlockHitResult ray = raycast(eye, rotation, reach - 0.035, MC.level, MC.player);
         return ray != null && ray.getBlockPos().equals(support) && ray.getDirection() == face && this.isHitOnSupportFace(ray);
      } else {
         return false;
      }
   }

   static boolean init_Z_Vec3(Vec3 eye, RotationMath.Rotation rotation, BlockPos support, double reach) {
      if (eye != null
         && rotation != null
         && support != null
         && Double.isFinite(reach)
         && !(reach <= 0.0)
         && Float.isFinite(rotation.yaw())
         && Float.isFinite(rotation.pitch())) {
         AABB box = new AABB(support);
         if (box.contains(eye)) {
            return true;
         }

         Vec3 end = eye.add(Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).scale(reach));
         return box.clip(eye, end).isPresent();
      } else {
         return false;
      }
   }

   private boolean isHitOnSupportFace(BlockHitResult hit) {
      BlockPos support = hit.getBlockPos();
      VoxelShape shape = MC.level.getBlockState(support).getShape(MC.level, support, CollisionContext.of(MC.player));

      for (AABB box : shape.toAabbs()) {
         if (isHitInsetOnFace(hit.getLocation(), box.move(support), hit.getDirection())) {
            return true;
         }
      }

      return false;
   }

   static boolean isHitInsetOnFace(Vec3 hit, AABB box, Direction side) {
      if (hit != null && box != null && side != null) {
         ScaffoldModule.FaceRect face = ScaffoldModule.FaceRect.init(box, side).load(0.08);
         double epsilon = 1.0E-5;
         return hit.x >= face.read().x - epsilon
            && hit.x <= face.write().x + epsilon
            && hit.y >= face.read().y - epsilon
            && hit.y <= face.write().y + epsilon
            && hit.z >= face.read().z - epsilon
            && hit.z <= face.write().z + epsilon;
      } else {
         return false;
      }
   }

   static double aimHeight(int supportY, int flatPlacements, double takeoffY, double playerY) {
      return Mth.clamp(supportY + 0.78 - Math.max(0, flatPlacements) * 0.11 - Math.max(0.0, takeoffY - playerY) * 0.18, supportY + 0.22, supportY + 0.8);
   }

   static boolean isWithinReach(Vec3 eye, Vec3 wireEye, Vec3 hit, double reach) {
      if (eye != null && wireEye != null && hit != null && Double.isFinite(reach) && !(reach <= 0.035)) {
         double limit = (reach - 0.035) * (reach - 0.035);
         return eye.distanceToSqr(hit) <= limit && wireEye.distanceToSqr(hit) <= limit;
      } else {
         return false;
      }
   }

   static boolean isUnacknowledgedPrediction(BlockPos support, int acknowledged, Iterable<ScaffoldModule.PendingPlacementSeq> predictions) {
      for (ScaffoldModule.PendingPlacementSeq prediction : predictions) {
         if (support.equals(prediction.cell()) && !isAcknowledged(prediction.sequence(), acknowledged)) {
            return false;
         }
      }

      return true;
   }

   private ScaffoldModule.LivePlacement refineLivePlacement(LocalPlayer player, ScaffoldModule.LivePlacement placement) {
      return this.refineLivePlacement(player, placement, player.getEyePosition(1.0F), player.getBoundingBox());
   }

   private ScaffoldModule.LivePlacement refineLivePlacement(LocalPlayer player, ScaffoldModule.LivePlacement placement, Vec3 eye, AABB body) {
      ScaffoldModule.PlannedPlacement target = placement.plan();
      if (!this.isSettledSolid(MC.level.getBlockState(target.support()), target.support())) {
         if (placement == this.livePlacement) {
            this.livePlacement = null;
         }

         return null;
      } else {
         if (!this.isEagleMode() && !intersectsCell(target.load(), body)) {
            RotationMath.Rotation current = this.lastSentRotation();
            BlockHitResult carried = raycast(eye, current, player.blockInteractionRange(), MC.level, player);
            if (carried != null
               && carried.getBlockPos().equals(target.support())
               && carried.getDirection() == target.face()
               && this.isHitOnSupportFace(carried)
               && (!this.size_Z() || Math.abs(RotationMath.angleDelta(current.yaw(), this.anchorYaw)) >= this.init_D_8() - 1.0)) {
               return new ScaffoldModule.LivePlacement(
                  new ScaffoldModule.PlannedPlacement(target.support(), target.load(), target.face(), carried, current, target.minHitY()),
                  placement.init_Z()
               );
            }
         }

         if (target.face().getAxis().isVertical()) {
            if (intersectsCell(target.load(), body)) {
               return null;
            }

            Vec3 point = target.hit().getLocation();
            if (placement.init_Z() && (!this.next_Z() || this.isEagleMode())) {
               point = supportAimPoint(supportBox(target.support()), eye, this.anchorForward(), this.isEagleMode() ? null : this.snapEyeToLane(eye));
               if (point == null) {
                  return null;
               }
            }

            RotationMath.Rotation rotation = RotationMath.lookAt(point, eye);
            if (placement.init_Z() && this.size_Z()) {
               rotation = this.init_ce_DataUtil_BlockPos(target.support(), target.face(), eye, this.lastSentRotation(), rotation);
               if (rotation == null) {
                  return null;
               }
            }

            BlockHitResult ray = raycast(eye, rotation, player.blockInteractionRange(), MC.level, player);
            return ray != null && ray.getBlockPos().equals(target.support()) && ray.getDirection() == target.face()
               ? new ScaffoldModule.LivePlacement(
                  new ScaffoldModule.PlannedPlacement(target.support(), target.load(), target.face(), ray, rotation, target.minHitY()), placement.init_Z()
               )
               : null;
         } else {
            BlockState supportState = MC.level.getBlockState(target.support());
            VoxelShape shape = supportState.getShape(MC.level, target.support(), CollisionContext.of(player));
            if (shape.isEmpty()) {
               return null;
            }

            Vec3 normal = new Vec3(target.face().getStepX(), target.face().getStepY(), target.face().getStepZ());
            double desiredY = aimHeight(target.support().getY(), this.f_I_I, this.launchY, eye.y - player.getEyeHeight());
            BlockPos above = target.support().above();
            if (MC.level.getBlockState(above).isCollisionShapeFullBlock(MC.level, above)) {
               desiredY = Math.min(desiredY, target.support().getY() + 0.2);
            }

            double reach = player.blockInteractionRange();
            RotationMath.Rotation current = this.lastSentRotation();
            if (this.size_Z() && target.face() == this.anchorDirection()) {
               Vec3 top = supportAimPoint(supportBox(target.support()), eye, this.anchorForward(), this.snapEyeToLane(eye));
               if (top == null) {
                  return null;
               }

               RotationMath.Rotation fixed = this.init_ce_DataUtil_BlockPos(target.support(), target.face(), eye, current, RotationMath.lookAt(top, eye));
               if (fixed == null) {
                  return null;
               }

               BlockHitResult ray = raycast(eye, fixed, reach, MC.level, player);
               return new ScaffoldModule.LivePlacement(
                  new ScaffoldModule.PlannedPlacement(target.support(), target.load(), target.face(), ray, fixed, target.minHitY()), placement.init_Z()
               );
            } else {
               ScaffoldModule.FaceAimSample[] samples = new ScaffoldModule.FaceAimSample[FACE_AIM_OFFSETS.length];
               double[] costs = new double[FACE_AIM_OFFSETS.length];
               Arrays.fill(costs, 1.0 / 0.0);
               Vec3 blockOffset = new Vec3(target.support().getX(), target.support().getY(), target.support().getZ());

               for (AABB localBox : shape.toAabbs()) {
                  ScaffoldModule.FaceRect face = ScaffoldModule.FaceRect.init(localBox, target.face()).load(0.12).init(blockOffset);
                  if (!(face.area() <= 1.0E-9) && !(eye.subtract(face.centre()).dot(normal) <= 0.0125)) {
                     for (int offsetIndex = 0; offsetIndex < FACE_AIM_OFFSETS.length; offsetIndex++) {
                        double offset = FACE_AIM_OFFSETS[offsetIndex];
                        Vec3 point = init_Vec3_Vec3(
                           face.read(),
                           face.write(),
                           eye,
                           target.face(),
                           desiredY,
                           (this.isEagleMode() ? 0.0 : eyePastEdge(face.read(), face.write(), eye, target.face(), this.snapEyeToLane(eye))) + offset,
                           0.0F
                        );
                        if (!(eye.distanceToSqr(point) > reach * reach + 1.0E-7)) {
                           RotationMath.Rotation rotation = RotationMath.lookAt(point, eye);
                           BlockHitResult ray = raycast(eye, rotation, reach, MC.level, player);
                           if (ray != null && ray.getBlockPos().equals(target.support()) && ray.getDirection() == target.face()) {
                              double cost = angleBetween(current, rotation) + Math.abs(offset) * 4.0 + eye.distanceTo(point) * 0.015;
                              if (cost < costs[offsetIndex]) {
                                 costs[offsetIndex] = cost;
                                 samples[offsetIndex] = new ScaffoldModule.FaceAimSample(face, point, rotation, ray, offsetIndex);
                              }
                           }
                        }
                     }
                  }
               }

               int selectedOffset = Double.isFinite(costs[0]) ? 0 : cheapestIndex(this.selectedOffset, costs);
               ScaffoldModule.FaceAimSample best = selectedOffset < 0 ? null : samples[selectedOffset];
               if (best == null) {
                  return null;
               }

               this.selectedOffset = selectedOffset;
               ScaffoldModule.PlannedPlacement live = new ScaffoldModule.PlannedPlacement(
                  target.support(),
                  target.load(),
                  target.face(),
                  best.hit(),
                  best.rotation(),
                  best.face().read().y
               );
               return new ScaffoldModule.LivePlacement(live, placement.init_Z());
            }
         }
      }
   }

   static int cheapestIndex(int lockedIndex, double[] costs) {
      if (costs != null && costs.length != 0) {
         if (lockedIndex >= 0 && lockedIndex < costs.length && Double.isFinite(costs[lockedIndex])) {
            return lockedIndex;
         }

         int best = -1;
         double bestCost = 1.0 / 0.0;

         for (int index = 0; index < costs.length; index++) {
            double cost = costs[index];
            if (Double.isFinite(cost) && cost < bestCost) {
               best = index;
               bestCost = cost;
            }
         }

         return best;
      } else {
         return -1;
      }
   }

   static Vec3 init_Vec3_Vec3(Vec3 from, Vec3 to, Vec3 eye, Direction side, double height, double offset, float lookOffset) {
      double distance = side.getAxis() == Axis.X ? Math.abs(eye.x - from.x) : Math.abs(eye.z - from.z);
      double sideways = Math.tan(lookOffset * 0.017453292F) * distance + offset;
      Vec3 center = side.getAxis() == Axis.X
         ? new Vec3(from.x, height, eye.z + side.getStepX() * sideways)
         : new Vec3(eye.x - side.getStepZ() * sideways, height, from.z);
      return new Vec3(Mth.clamp(center.x, from.x, to.x), Mth.clamp(height, from.y, to.y), Mth.clamp(center.z, from.z, to.z));
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (this.plannedAim == null) {
         return null;
      }

      BlockHitResult ray = this.raycastRotation(this.plannedAim, MC.player.blockInteractionRange());
      return ray != null
            && ray.getBlockPos().equals(target.support())
            && ray.getDirection() == target.face()
            && this.isHitOnSupportFace(ray)
         ? new ScaffoldModule.PlannedPlacement(target.support(), target.load(), target.face(), ray, this.plannedAim, target.minHitY())
         : null;
   }

   private ScaffoldModule.PlannedPlacement stepAim(ScaffoldModule.PlannedPlacement live) {
      return this.stepAim(live, this.rotationSpeedCap());
   }

   private ScaffoldModule.PlannedPlacement smoothAim(ScaffoldModule.PlannedPlacement live, Vec3 eye) {
      this.update_Z = false;
      RotationMath.Rotation proposed = this.smoother()
         .init(this.lastSentRotation(), live.rotation(), false, this.rotationSmoothness.get(), RotationMath.gcdStep(), AutismSessionState.getInstance().getClientTick());
      ScaffoldModule.PlannedPlacement endpoint = new ScaffoldModule.PlannedPlacement(
         live.support(), live.load(), live.face(), live.hit(), proposed, live.minHitY()
      );
      return this.stepAim(endpoint, this.rotationSpeedCap(), eye, true);
   }

   private ScaffoldModule.PlannedPlacement stepAim(ScaffoldModule.PlannedPlacement live, float cap) {
      return this.stepAim(live, cap, MC.player.getEyePosition());
   }

   private ScaffoldModule.PlannedPlacement stepAim(ScaffoldModule.PlannedPlacement live, float cap, Vec3 eye) {
      return this.stepAim(live, cap, eye, false);
   }

   private ScaffoldModule.PlannedPlacement stepAim(ScaffoldModule.PlannedPlacement live, float cap, Vec3 eye, boolean continuous) {
      RotationMath.Rotation from = this.lastSentRotation();
      RotationMath.Rotation stepped = continuous
         ? RotationMath.stepQuantizedUnwrapped(from, live.rotation(), cap, cap, RotationMath.gcdStep())
         : stepRotationQuantized(from, live.rotation(), cap, cap, RotationMath.gcdStep());
      BlockHitResult ray = raycast(eye, stepped, MC.player.blockInteractionRange(), MC.level, MC.player);
      if (ray != null && ray.getBlockPos().equals(live.support()) && ray.getDirection() == live.face() && this.isHitOnSupportFace(ray)) {
         return new ScaffoldModule.PlannedPlacement(live.support(), live.load(), live.face(), ray, stepped, live.minHitY());
      }

      double gcd = gcdOrDefault(RotationMath.gcdStep());

      for (int radius = 1; radius <= 3; radius++) {
         for (int yawCounts = -radius; yawCounts <= radius; yawCounts++) {
            int pitchCounts = radius - Math.abs(yawCounts);
            ScaffoldModule.PlannedPlacement adjusted = this.stepAim(live, from, stepped, yawCounts, pitchCounts, gcd, cap, eye);
            if (adjusted != null) {
               return adjusted;
            }

            if (pitchCounts != 0) {
               adjusted = this.stepAim(live, from, stepped, yawCounts, -pitchCounts, gcd, cap, eye);
               if (adjusted != null) {
                  return adjusted;
               }
            }
         }
      }

      return null;
   }

   private ScaffoldModule.PlannedPlacement stepAim(
      ScaffoldModule.PlannedPlacement live, RotationMath.Rotation from, RotationMath.Rotation base, int yawCounts, int pitchCounts, double gcd, float cap, Vec3 eye
   ) {
      RotationMath.Rotation candidate = new RotationMath.Rotation(
         base.yaw() + (float)(yawCounts * gcd), Mth.clamp(base.pitch() + (float)(pitchCounts * gcd), -90.0F, 90.0F)
      );
      float yawDelta = Math.abs(candidate.yaw() - from.yaw());
      float pitchDelta = Math.abs(candidate.pitch() - from.pitch());
      double countSlack = gcd * 0.501;
      if (!(yawDelta > cap + countSlack) && !(pitchDelta > cap + countSlack)) {
         BlockHitResult ray = raycast(eye, candidate, MC.player.blockInteractionRange(), MC.level, MC.player);
         return ray != null
               && ray.getBlockPos().equals(live.support())
               && ray.getDirection() == live.face()
               && this.isHitOnSupportFace(ray)
            ? new ScaffoldModule.PlannedPlacement(live.support(), live.load(), live.face(), ray, candidate, live.minHitY())
            : null;
      } else {
         return null;
      }
   }

   private boolean flush_Z(LocalPlayer player) {
      if (this.stop_Z) {
         return true;
      }

      if (this.f_G_I > 0) {
         return true;
      }

      this.slotSwitched = false;
      InteractionHand hand = this.placeHand();
      if (hand == null) {
         if (this.slotSwitched) {
            this.requestSwitchBack();
            return true;
         } else {
            return false;
         }
      } else {
         this.requestSwitchBack();
         ItemStack held = player.getItemInHand(hand);
         if (!this.isPlaceableBlock(held)) {
            return false;
         }

         if (this.livePlacement == null) {
            this.livePlacement = this.planLanePlacement(player, held);
         }

         if (this.livePlacement == null) {
            return false;
         }

         ScaffoldModule.LivePlacement live = this.refineLivePlacement(player, this.livePlacement);
         if (live == null) {
            return false;
         }

         if (al.init_Z_HitResult(live.plan().hit(), hand, this.id())) {
            return false;
         }

         this.tryPlaceLive(live, hand, held, null);
         return true;
      }
   }

   private boolean close_Z(LocalPlayer player) {
      return this.close_BlockPos == null ? false : this.write_Z_LocalPlayer_BlockPos(player, this.close_BlockPos);
   }

   private boolean write_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos raised) {
      double feetY = raised.getY() + 1.0;
      Vec3 projected = projectLanding(player.position(), player.getDeltaMovement(), feetY, this.horizontalHeading(player));
      return !overlapsBlock(player.getBoundingBox().move(projected.x - player.getX(), feetY - player.getY(), projected.z - player.getZ()), raised);
   }

   private boolean open_Z(LocalPlayer player) {
      return this.clear_Z && !this.start_Z ? player.getDeltaMovement().y > 0.0 || player.getY() > this.supportY + 2.0 : false;
   }

   private BlockPos load_BlockPos_LocalPlayer(LocalPlayer player) {
      if (this.laneSupport == null) {
         return null;
      }

      if (this.write_BlockPos != null && this.clear_Z) {
         BlockPos support = this.write_BlockPos;
         if (!this.isSettledSolid(MC.level.getBlockState(support), support)) {
            return null;
         }

         Vec3 landing = projectLanding(
            player.position(), player.getDeltaMovement(), this.supportY + 2.0, this.anchorForward(), Math.max(0, this.batchPower - this.tellyAirTicks)
         );
         if (!overlapsBlock(player.getBoundingBox().move(landing.subtract(player.position())), support.above(), 0.12)) {
            return null;
         }

         BlockState above = MC.level.getBlockState(support.above());
         return !above.isAir() && !above.canBeReplaced() ? null : support;
      } else {
         double feetY = this.supportY + 2.0;
         Vec3 projected = projectLanding(player.position(), player.getDeltaMovement(), feetY, this.airControlVector(), this.braking ? 1 : 14);
         BlockPos cell = this.alignToLane(BlockPos.containing(projected.x, this.supportY, projected.z));
         Direction back = this.anchorDirection().getOpposite();

         for (int step = 0; step <= 1; step++) {
            BlockPos candidate = step == 0 ? cell : cell.relative(back, step);
            if (!MC.level.isOutsideBuildHeight(candidate)
               && this.isSettledSolid(MC.level.getBlockState(candidate), candidate)
               && init_Z_Vec3_7(player.position(), player.getDeltaMovement(), player.getBoundingBox(), candidate.above(), this.horizontalHeading(player))
               )
             {
               BlockState above = MC.level.getBlockState(candidate.above());
               if (above.isAir() || above.canBeReplaced()) {
                  return candidate;
               }
            }
         }

         return null;
      }
   }

   static boolean init_Z_Vec3_7(Vec3 position, Vec3 velocity, AABB footprint, BlockPos raised, Vec3 forward) {
      double feetY = raised.getY() + 1.0;
      if (velocity.y <= 0.0 && position.y < feetY - 1.0E-9) {
         return false;
      }

      Vec3 powered = projectLanding(position, velocity, feetY, forward);
      if (overlapsBlock(footprint.move(powered.x - position.x, feetY - position.y, powered.z - position.z), raised)) {
         return true;
      }

      Vec3 coast = projectLanding(position, velocity, feetY, Vec3.ZERO);
      return overlapsBlock(footprint.move(coast.x - position.x, feetY - position.y, coast.z - position.z), raised);
   }

   private static Vec3 projectLanding(Vec3 position, Vec3 velocity, double feetY, Vec3 forward) {
      return projectLanding(position, velocity, feetY, forward, 14);
   }

   private static Vec3 projectLanding(Vec3 position, Vec3 velocity, double feetY, Vec3 forward, int controlTicks) {
      Vec3 projected = position;
      Vec3 motion = velocity;
      Vec3 control = forward != null && !(forward.horizontalDistanceSqr() <= 1.0E-10)
         ? new Vec3(forward.x, 0.0, forward.z).normalize().scale(0.0196)
         : Vec3.ZERO;

      for (int tick = 0; tick < 14; tick++) {
         if (tick < controlTicks) {
            motion = motion.add(control);
         }

         projected = projected.add(motion);
         if (motion.y <= 0.0 && projected.y <= feetY + 1.0E-9) {
            return new Vec3(projected.x, feetY, projected.z);
         }

         motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
      }

      return new Vec3(projected.x, feetY, projected.z);
   }

   private Vec3 horizontalHeading(LocalPlayer player) {
      Vec3 velocity = player.getDeltaMovement();
      double hx = velocity.x;
      double hz = velocity.z;
      double length = Math.sqrt(hx * hx + hz * hz);
      return length < 0.05 ? this.anchorForward() : new Vec3(hx / length, 0.0, hz / length);
   }

   private boolean init_Z_LocalPlayer_I(LocalPlayer player, int blockY) {
      double feetY = blockY + 1.0;
      Vec3 projected = this.projectLedge(player, feetY).point();
      return this.init_Z_LocalPlayer(player, blockY, projected, feetY);
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, int blockY, int distance) {
      double feetY = blockY + 1.0;
      Vec3 projected = this.projectLedge(player, feetY).point();
      BlockPos runway = this.alignToLane(BlockPos.containing(projected.x, blockY, projected.z))
         .relative(this.anchorDirection(), Math.max(1, distance));
      return this.isSettledSolid(MC.level.getBlockState(runway), runway);
   }

   static boolean isLandingCovered(boolean landingSecured, boolean turnPending, boolean turnReserveCovered) {
      return landingSecured && (!turnPending || turnReserveCovered);
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, int blockY, Vec3 projected, double feetY) {
      AABB moved = player.getBoundingBox().move(projected.x - player.getX(), feetY - player.getY(), projected.z - player.getZ());
      return this.hasFloorUnderFootprint(moved, feetY, 0.12);
   }

   private boolean load_Z_LocalPlayer_I(LocalPlayer player, int blockY) {
      if (!this.isEagleMode() && !this.clear_Z && !this.spaceHeld && !this.next_Z()) {
         double feetY = blockY + 1.0;
         Vec3 selected = this.projectLedge(player, feetY).point();
         Vec3 coast = projectLedge(
               player.position(),
               player.getDeltaMovement(),
               feetY,
               Vec3.ZERO,
               0.91,
               player.getBoundingBox(),
               false,
               (box, movement) -> this.collideMovement(player, box, movement)
            )
            .point();
         AABB body = player.getBoundingBox();
         AABB selectedBox = body.move(selected.subtract(player.position()));
         AABB coastBox = body.move(coast.subtract(player.position()));
         return init_Z_AABB(selectedBox, coastBox, this.anchorLeft(), this.laneOffset, box -> this.hasFloorUnderFootprint(box, feetY, 0.125));
      } else {
         return true;
      }
   }

   static boolean init_Z_AABB(AABB selected, AABB coast, Vec3 left, double lane, Predicate<AABB> floor) {
      Vec3 delta = coast.getCenter().subtract(selected.getCenter());
      int samples = Math.max(1, (int)Math.ceil(delta.horizontalDistance() / 0.01));

      for (int tick = 0; tick <= samples; tick++) {
         AABB box = selected.move(delta.scale((double)tick / samples));
         if (!floor.test(box)) {
            return false;
         }

         double correction = lane - box.getCenter().dot(left);
         if (!floor.test(box.move(left.scale(correction)))) {
            return false;
         }
      }

      return true;
   }

   private boolean hasFloorUnderFootprint(AABB footprint, double feetY, double overlap) {
      int minX = Mth.floor(footprint.minX + 1.0E-9);
      int maxX = Mth.floor(footprint.maxX - 1.0E-9);
      int minZ = Mth.floor(footprint.minZ + 1.0E-9);
      int maxZ = Mth.floor(footprint.maxZ - 1.0E-9);
      int minY = Mth.floor(feetY - 1.0);
      int maxY = Mth.floor(feetY - 1.0E-9);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockState state = MC.level.getBlockState(pos);
               if (this.isSettledSolid(state, pos)) {
                  for (AABB local : state.getCollisionShape(MC.level, pos, CollisionContext.of(MC.player)).toAabbs()) {
                     AABB floor = local.move(x, y, z);
                     if (!(floor.maxY > feetY + 1.0E-9) && !(feetY - floor.maxY > 0.08)) {
                        double overlapX = Math.min(footprint.maxX, floor.maxX) - Math.max(footprint.minX, floor.minX);
                        double overlapZ = Math.min(footprint.maxZ, floor.maxZ) - Math.max(footprint.minZ, floor.minZ);
                        if (overlapX >= overlap && overlapZ >= overlap) {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean update_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos block) {
      if (block == null) {
         return false;
      }

      double feetY = block.getY() + 1.0;
      Vec3 projected = projectAirControlLanding(player.position(), player.getDeltaMovement(), feetY, this.anchorForward());
      AABB moved = player.getBoundingBox().move(projected.x - player.getX(), feetY - player.getY(), projected.z - player.getZ());
      return overlapsBlock(moved, block);
   }

   static boolean overlapsBlock(AABB footprint, BlockPos block) {
      return overlapsBlock(footprint, block, 0.12);
   }

   static boolean overlapsBlock(AABB footprint, BlockPos block, double requiredOverlap) {
      if (footprint != null && block != null) {
         double overlapX = Math.min(footprint.maxX, block.getX() + 1.0) - Math.max(footprint.minX, block.getX());
         double overlapZ = Math.min(footprint.maxZ, block.getZ() + 1.0) - Math.max(footprint.minZ, block.getZ());
         return overlapX >= requiredOverlap && overlapZ >= requiredOverlap;
      } else {
         return false;
      }
   }

   static Vec3 projectBallisticLanding(Vec3 position, Vec3 velocity, double feetY) {
      Vec3 projected = position;
      Vec3 motion = velocity;

      for (int tick = 0; tick < 14; tick++) {
         projected = projected.add(motion);
         if (motion.y <= 0.0 && projected.y <= feetY + 0.08) {
            return new Vec3(projected.x, feetY, projected.z);
         }

         motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
      }

      return new Vec3(projected.x, feetY, projected.z);
   }

   static int ticksToLanding(Vec3 position, Vec3 velocity, double feetY) {
      Vec3 projected = position;
      Vec3 motion = velocity;

      for (int tick = 1; tick <= 14; tick++) {
         projected = projected.add(motion);
         if (motion.y <= 0.0 && projected.y <= feetY + 0.08) {
            return tick;
         }

         motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
      }

      return 14;
   }

   static Vec3 projectAirControlLanding(Vec3 position, Vec3 velocity, double feetY, Vec3 forward) {
      Vec3 projected = position;
      Vec3 motion = velocity;
      Vec3 airControl = forward != null && !(forward.horizontalDistanceSqr() <= 1.0E-10)
         ? new Vec3(forward.x, 0.0, forward.z).normalize().scale(0.0196)
         : Vec3.ZERO;

      for (int tick = 0; tick < 14; tick++) {
         motion = motion.add(airControl);
         projected = projected.add(motion);
         if (motion.y <= 0.0 && projected.y <= feetY + 0.08) {
            return new Vec3(projected.x, feetY, projected.z);
         }

         motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
      }

      return new Vec3(projected.x, feetY, projected.z);
   }

   private Vec3 launchVelocity(LocalPlayer player) {
      boolean sprinting = this.canSprint(player);
      double groundAcceleration = adjustedMoveSpeed(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), sprinting);
      return jumpLaunchVelocity(player.getDeltaMovement(), this.anchorYaw, groundAcceleration, sprinting);
   }

   static double adjustedMoveSpeed(double movementSpeed, boolean wasSprinting, boolean sprinting) {
      double speed = Math.max(0.0, movementSpeed);
      if (sprinting != wasSprinting) {
         speed *= sprinting ? 1.3 : 0.7692307692307692;
      }

      return speed * 0.98;
   }

   private boolean canSprint(LocalPlayer player) {
      if (!this.isEagleMode() && this.f_Z_I != AutismSessionState.getInstance().getClientTick() && !isKeyDown(MC.options.keyShift)) {
         AutismLocalPlayerAccessor vanilla = (AutismLocalPlayerAccessor)player;
         return vanilla.autism$isSprintingPossible(player.getAbilities().flying)
            && !player.isMovingSlowly()
            && !vanilla.autism$isSlowDueToUsingItem()
            && !player.isFallFlying();
      } else {
         return false;
      }
   }

   static Vec3 jumpLaunchVelocity(Vec3 velocity, float visibleYawDegrees, double groundAcceleration, boolean sprinting) {
      if (velocity == null) {
         velocity = Vec3.ZERO;
      }

      float visibleYaw = visibleYawDegrees * 0.017453292F;
      Vec3 jumpDirection = new Vec3(-Mth.sin(visibleYaw), 0.0, Mth.cos(visibleYaw));
      double sprintJumpBoost = sprinting ? 0.2 : 0.0;
      double visibleForwardImpulse = sprintJumpBoost + groundAcceleration;
      return new Vec3(velocity.x + jumpDirection.x * visibleForwardImpulse, Math.max(velocity.y, 0.42), velocity.z + jumpDirection.z * visibleForwardImpulse);
   }

   private boolean sync_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && MC.level != null) {
         Vec3 launch = this.isEagleMode()
            ? jumpLaunchVelocity(player.getDeltaMovement(), this.anchorYaw, Math.max(0.1, player.getSpeed()) * 0.98, true)
            : this.launchVelocity(player);
         double drag = MC.level.getBlockState(support).getBlock().getFriction() * 0.91;
         AABB body = player.getDimensions(Pose.STANDING).makeBoundingBox(player.position());
         if (!this.isEagleMode()) {
            body = body.inflate(0.08, 0.0, 0.08);
         }

         return projectLedge(
               player.position(),
               launch,
               player.getY(),
               this.anchorForward(),
               drag,
               body,
               true,
               (box, movement) -> this.collideMovement(player, box, movement)
            )
            .init_Z();
      } else {
         return true;
      }
   }

   private Vec3 collideMovement(LocalPlayer player, AABB box, Vec3 movement) {
      return Entity.collideBoundingBox(player, movement, box, MC.level, MC.level.getEntityCollisions(player, box.expandTowards(movement)));
   }

   private ScaffoldModule.LedgePoint projectLedge(LocalPlayer player, double feetY) {
      return projectLedge(
         player.position(),
         player.getDeltaMovement(),
         feetY,
         this.airControlVector(),
         0.91,
         player.getBoundingBox(),
         false,
         (box, movement) -> this.collideMovement(player, box, movement),
         this.braking ? 1 : (this.node_Z ? this.f_X_I : 14)
      );
   }

   private Vec3 airControlVector() {
      if (this.braking) {
         return this.anchorForward().scale(-1.0);
      }

      Vec3 forward = this.forwardKeyState != ScaffoldModule.ForwardKeyState.HOLD && !this.pop_Z ? this.anchorForward() : Vec3.ZERO;
      double side = this.forwardKeyState == ScaffoldModule.ForwardKeyState.FORWARD
         ? 0.0
         : (this.strafeSide == ScaffoldModule.StrafeSide.LEFT ? 1.0 : (this.strafeSide == ScaffoldModule.StrafeSide.RIGHT ? -1.0 : 0.0));
      Vec3 input = forward.add(this.anchorLeft().scale(side));
      return input.normalize().scale(MC.player != null && MC.player.isCrouching() ? 0.3 : 1.0);
   }

   static ScaffoldModule.LedgePoint projectLedge(
      Vec3 position,
      Vec3 velocity,
      double feetY,
      Vec3 forward,
      double launchDrag,
      AABB footprint,
      boolean launch,
      BiFunction<AABB, Vec3, Vec3> collisionResolver
   ) {
      return projectLedge(position, velocity, feetY, forward, launchDrag, footprint, launch, collisionResolver, 14);
   }

   static ScaffoldModule.LedgePoint projectLedge(
      Vec3 position,
      Vec3 velocity,
      double feetY,
      Vec3 forward,
      double launchDrag,
      AABB footprint,
      boolean launch,
      BiFunction<AABB, Vec3, Vec3> collisionResolver,
      int controlTicks
   ) {
      Vec3 projected = position;
      Vec3 motion = velocity;
      AABB box = footprint;
      Vec3 control = forward != null && !(forward.horizontalDistanceSqr() <= 1.0E-10) ? new Vec3(forward.x, 0.0, forward.z) : Vec3.ZERO;
      if (control.lengthSqr() > 1.0) {
         control = control.normalize();
      }

      control = control.scale(0.0196);
      boolean obstructed = false;

      for (int tick = 0; tick < 14; tick++) {
         if ((!launch || tick > 0) && tick < controlTicks) {
            motion = motion.add(control);
         }

         double dy = motion.y <= 0.0 && projected.y >= feetY ? Math.max(motion.y, feetY - projected.y) : motion.y;
         Vec3 requested = new Vec3(motion.x, dy, motion.z);
         Vec3 moved = collisionResolver.apply(box, requested);
         boolean blockedX = Math.abs(moved.x - requested.x) > 1.0E-9;
         boolean blockedY = Math.abs(moved.y - requested.y) > 1.0E-9;
         boolean blockedZ = Math.abs(moved.z - requested.z) > 1.0E-9;
         obstructed |= blockedX || blockedY || blockedZ;
         projected = projected.add(moved);
         box = box.move(moved);
         if (motion.y <= 0.0 && projected.y <= feetY + 0.08) {
            return new ScaffoldModule.LedgePoint(new Vec3(projected.x, feetY, projected.z), obstructed);
         }

         double drag = launch && tick == 0 ? Mth.clamp(launchDrag, 0.0, 1.2) : 0.91;
         motion = new Vec3(blockedX ? 0.0 : motion.x * drag, ((blockedY ? 0.0 : motion.y) - 0.08) * 0.98, blockedZ ? 0.0 : motion.z * drag);
      }

      return new ScaffoldModule.LedgePoint(new Vec3(projected.x, feetY, projected.z), obstructed);
   }

   private boolean flush_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && MC.level != null) {
         Vec3 launchVelocity = this.launchVelocity(player);
         double launchDrag = MC.level.getBlockState(support).getBlock().getFriction() * 0.91;
         Vec3 forward = this.anchorForward();
         Vec3 landing = projectLaunchLanding(player.position(), launchVelocity, player.getY(), forward, launchDrag);
         Vec3 coastLanding = projectLaunchLanding(player.position(), launchVelocity, player.getY(), forward, launchDrag, 0.0);
         Vec3 fastLanding = projectLaunchLanding(player.position(), launchVelocity, player.getY(), forward, launchDrag, this.canSprint(player) ? 0.026 : 0.0196);
         return this.init_Z_LocalPlayer_4(player, support, coastLanding)
            && this.init_Z_LocalPlayer_4(player, support, landing)
            && this.init_Z_LocalPlayer_4(player, support, fastLanding);
      } else {
         return false;
      }
   }

   private boolean init_Z_LocalPlayer_4(LocalPlayer player, BlockPos support, Vec3 landing) {
      Direction direction = this.anchorDirection();
      BlockPos projectedCell = BlockPos.containing(landing.x, support.getY(), landing.z);
      if (this.isSettledSolid(MC.level.getBlockState(projectedCell), projectedCell)) {
         return true;
      }

      BlockPos catchBlock = alignToLane(projectedCell, support, direction);
      int steps = (catchBlock.getX() - support.getX()) * direction.getStepX() + (catchBlock.getZ() - support.getZ()) * direction.getStepZ();
      if (steps >= 1 && steps <= 6) {
         for (int step = 1; step <= steps; step++) {
            BlockPos cell = support.relative(direction, step);
            if (MC.level.isOutsideBuildHeight(cell)) {
               return false;
            }

            BlockState state = MC.level.getBlockState(cell);
            if (!this.isSettledSolid(state, cell) && !state.isAir() && !state.canBeReplaced()) {
               return false;
            }
         }

         double feetY = support.getY() + 1.0;
         AABB landingBox = player.getBoundingBox().move(landing.x - player.getX(), feetY - player.getY(), landing.z - player.getZ());
         return overlapsBlock(landingBox, catchBlock, 0.12);
      } else {
         return false;
      }
   }

   private boolean init_Z_LocalPlayer_3(LocalPlayer player, BlockPos under, ScaffoldModule.LivePlacement first) {
      if (player == null || under == null || first == null) {
         return false;
      } else if (this.pop_Z || this.isAnchorDeviating() || this.task_Z) {
         return false;
      } else if (!this.isAimAligned()) {
         return false;
      } else {
         boolean catchable = !this.set_Z && !this.spaceHeld ? this.flush_Z_LocalPlayer_BlockPos(player, under) : this.close_Z_LocalPlayer_BlockPos(player, under);
         if (!catchable) {
            return false;
         } else {
            return !isLaneErrorSmall(this.laneError(player))
               ? false
               : read_Z_I(this.init_I_LocalPlayer_BlockPos(player, under)) && this.load_Z_LocalPlayer(player, under, first);
         }
      }
   }

   private boolean load_Z_LocalPlayer(LocalPlayer player, BlockPos under, ScaffoldModule.LivePlacement first) {
      if (first != null && !first.init_Z()) {
         Vec3 forward = this.anchorForward();
         ScaffoldModule.PlannedPlacement target = first.plan();
         if (target.face() != null && target.face() == this.anchorDirection()) {
            Vec3 launch = this.launchVelocity(player);
            double drag = MC.level.getBlockState(under).getBlock().getFriction() * 0.91;
            double feetY = under.getY() + 1.0;
            double air = this.canSprint(player) ? 0.026 : 0.0196;
            Vec3 shortest = projectLaunchLanding(player.position(), launch, feetY, forward, drag, 0.0196);
            Vec3 longest = projectLaunchLanding(player.position(), launch, feetY, forward, drag, air).add(forward.scale(0.25));
            if (this.init_Z_LocalPlayer(player, under.getY(), shortest, feetY) && this.init_Z_LocalPlayer(player, under.getY(), longest, feetY)) {
               return true;
            }

            double face = Vec3.atCenterOf(target.support()).dot(forward) + 0.5;
            return init_I_Vec3(player.position(), launch, feetY, forward, drag, air, face) >= Math.max(4, this.smootherBand().init_I() + 2);
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   private boolean init_Z_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.LivePlacement first) {
      return this.init_Z_LocalPlayer_2(player, first, false);
   }

   private boolean init_Z_LocalPlayer_2(LocalPlayer player, ScaffoldModule.LivePlacement first, boolean flatEntry) {
      if (first != null && !first.init_Z() && (flatEntry || !this.set_Z && !this.spaceHeld)) {
         BlockPos root = first.plan().support();
         BlockPos under = this.supportBlockUnder(player);
         if (under == null) {
            return true;
         }

         Vec3 launch = this.launchVelocity(player);
         double drag = this.groundDrag(under);
         double feetY = under.getY() + 1.0;
         Vec3 landing = projectLaunchLanding(player.position(), launch, feetY, this.anchorForward(), drag, 0.0);
         BlockPos cell = this.alignToLane(BlockPos.containing(landing.x, under.getY(), landing.z));
         Direction direction = this.anchorDirection();
         int distance = Math.max(0, (cell.getX() - root.getX()) * direction.getStepX() + (cell.getZ() - root.getZ()) * direction.getStepZ());
         int missing = 0;

         for (int step = 1; step <= Math.min(8, distance); step++) {
            BlockPos needed = root.relative(direction, step);
            if (!this.isSettledSolid(MC.level.getBlockState(needed), needed)) {
               missing++;
            }
         }

         Vec3 afterLaunch = new Vec3(launch.x * drag, (launch.y - 0.08) * 0.98, launch.z * drag);
         int flight = 1 + ticksToLanding(player.position().add(launch), afterLaunch, feetY);
         int extras = flatEntry ? 0 : this.flush_I();
         int turn = this.smootherBand().init_I();
         if (canFillInFlight(flight, missing, turn, extras)) {
            return true;
         } else {
            return extras != 1
               ? false
               : init(
                     player.position().add(launch),
                     afterLaunch,
                     direction,
                     root,
                     feetY,
                     0.0,
                     turn + 1,
                     extras,
                     0,
                     false,
                     needed -> this.isSettledSolid(MC.level.getBlockState(needed), needed)
                  )
                  .init_Z();
         }
      } else {
         return true;
      }
   }

   static boolean canFillInFlight(int flightTicks, int missing, int rotationTicks) {
      return canFillInFlight(flightTicks, missing, rotationTicks, 0);
   }

   static boolean canFillInFlight(int flightTicks, int missing, int rotationTicks, int extraTops) {
      return missing == 0 || rotationTicks + missing * 3 - 1 + Math.clamp(extraTops, 0, 2) * 2 <= flightTicks;
   }

   static int init_I_Vec3(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward, double launchDrag, double airControl, double faceAlong) {
      if (position != null && launchVelocity != null && forward != null && !(forward.horizontalDistanceSqr() <= 1.0E-10)) {
         Vec3 axis = new Vec3(forward.x, 0.0, forward.z).normalize();
         Vec3 projected = position.add(launchVelocity);
         double drag = Mth.clamp(launchDrag, 0.0, 1.2);
         Vec3 motion = new Vec3(launchVelocity.x * drag, (launchVelocity.y - 0.08) * 0.98, launchVelocity.z * drag);
         Vec3 control = axis.scale(airControl);
         int crossed = projected.dot(axis) >= faceAlong + 0.05 ? 0 : -1;

         for (int tick = 1; tick < 14; tick++) {
            motion = motion.add(control);
            projected = projected.add(motion);
            if (crossed < 0 && projected.dot(axis) >= faceAlong + 0.05) {
               crossed = tick;
            }

            if (motion.y <= 0.0 && projected.y <= feetY + 0.08) {
               return crossed < 0 ? -1 : tick - crossed;
            }

            motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
         }

         return crossed < 0 ? -1 : 13 - crossed;
      } else {
         return -1;
      }
   }

   private double laneError(LocalPlayer player) {
      return player == null ? 0.0 : this.laneOffset - lateralOffset(player.position(), this.anchorYaw);
   }

   static boolean read_Z_I(int steps) {
      return steps >= 2 && steps <= 4;
   }

   static boolean isLaneErrorSmall(double laneError) {
      return Double.isFinite(laneError) && Math.abs(laneError) <= 0.3;
   }

   private int init_I_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && MC.level != null) {
         Vec3 launchVelocity = this.launchVelocity(player);
         double launchDrag = MC.level.getBlockState(support).getBlock().getFriction() * 0.91;
         Vec3 landing = projectLaunchLanding(player.position(), launchVelocity, player.getY(), this.anchorForward(), launchDrag);
         Direction direction = this.anchorDirection();
         BlockPos catchBlock = BlockPos.containing(landing.x, support.getY(), landing.z);
         return (catchBlock.getX() - support.getX()) * direction.getStepX() + (catchBlock.getZ() - support.getZ()) * direction.getStepZ();
      } else {
         return -1;
      }
   }

   private boolean close_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      return !this.flush_Z_LocalPlayer_BlockPos(player, support)
         ? false
         : canReachHeight(player.position(), this.launchVelocity(player), support.getY() + 2.0);
   }

   private boolean open_Z(LocalPlayer player, BlockPos footing) {
      return this.init_Z_LocalPlayer_2(player, footing, player.position(), player.getDeltaMovement(), player.getBoundingBox());
   }

   private boolean init_Z_LocalPlayer_2(LocalPlayer player, BlockPos footing, Vec3 position, Vec3 velocity, AABB body) {
      return this.init_Z_LocalPlayer(player, footing, position, velocity, body, this.canSprint(player));
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, BlockPos footing, Vec3 position, Vec3 velocity, AABB body, boolean sprinting) {
      if (!this.isEagleMode() && this.spaceHeld) {
         return this.init_I_LocalPlayer(player, footing, position, velocity, body, sprinting) >= 0;
      }

      double acceleration = adjustedMoveSpeed(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), sprinting);
      Vec3 launch = jumpLaunchVelocity(velocity, this.anchorYaw, acceleration, sprinting);
      return init_Z_Vec3(
         position,
         launch,
         body,
         footing,
         this.anchorDirection(),
         this.groundDrag(footing),
         this.smootherBand().init_I(),
         cell -> this.isSettledSolid(MC.level.getBlockState(cell), cell),
         cell -> MC.level.getBlockState(cell).isAir() || MC.level.getBlockState(cell).canBeReplaced(),
         cell -> !MC.level.isOutsideBuildHeight(cell)
      );
   }

   static boolean init_Z_Vec3(
      Vec3 position,
      Vec3 launch,
      AABB body,
      BlockPos footing,
      Direction direction,
      double drag,
      int turn,
      Predicate<BlockPos> solid,
      Predicate<BlockPos> fillable,
      Predicate<BlockPos> inBounds
   ) {
      Vec3 forward = Vec3.atLowerCornerOf(direction.getUnitVec3i());
      double feetY = footing.getY() + 2.0;
      Vec3 after = new Vec3(launch.x * drag, (launch.y - 0.08) * 0.98, launch.z * drag);
      Vec3 landing = projectLanding(position.add(launch), after, feetY, Vec3.ZERO);
      AABB landed = body.move(landing.subtract(position));
      BlockPos projected = alignToLane(BlockPos.containing(landing.x, footing.getY(), landing.z), footing, direction);
      int flight = 1 + ticksToLanding(position.add(launch), after, feetY);

      for (int back = 0; back <= 1; back++) {
         BlockPos candidate = projected.relative(direction.getOpposite(), back);
         if (overlapsBlock(landed, candidate.above())) {
            int steps = (candidate.getX() - footing.getX()) * direction.getStepX() + (candidate.getZ() - footing.getZ()) * direction.getStepZ();
            if (steps >= 0 && steps <= 6 && inBounds.test(candidate.above())) {
               int missing = 0;
               int lastMissing = 0;
               boolean blocked = false;

               for (int step = 1; step <= steps; step++) {
                  BlockPos cell = footing.relative(direction, step);
                  if (!solid.test(cell)) {
                     if (!fillable.test(cell)) {
                        blocked = true;
                        break;
                     }

                     missing++;
                     lastMissing = step;
                  }
               }

               if (!blocked) {
                  double face = Vec3.atCenterOf(footing.relative(direction, Math.max(0, lastMissing - 1))).dot(forward) + 0.5;
                  int lead = missing == 0 ? flight : init_I_Vec3(position, launch, feetY, forward, drag, 0.0, face);
                  if (load_Z_I(flight, missing, turn, lead)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private int init_I_LocalPlayer(LocalPlayer player, BlockPos footing, Vec3 position, Vec3 velocity, AABB body, boolean sprinting) {
      if (AutismLiteVariant.enabled()) {
         return -1;
      }

      double acceleration = adjustedMoveSpeed(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), sprinting);
      Vec3 launch = jumpLaunchVelocity(velocity, this.anchorYaw, acceleration, sprinting);
      return init_I_Vec3(
         position,
         launch,
         body,
         footing,
         this.anchorDirection(),
         this.groundDrag(footing),
         cell -> this.isSettledSolid(MC.level.getBlockState(cell), cell),
         cell -> MC.level.getBlockState(cell).isAir() || MC.level.getBlockState(cell).canBeReplaced(),
         cell -> !MC.level.isOutsideBuildHeight(cell)
      );
   }

   private boolean poll(LocalPlayer player, BlockPos footing) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (!this.pop_Z && !this.sneakHold.load() && !this.isAnchorDeviating() && !this.task_Z && this.isAimAligned()) {
         double acceleration = adjustedMoveSpeed(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), this.canSprint(player));
         Vec3 velocity = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
         Vec3 force = this.anchorForward().scale(acceleration);
         Vec3 step = velocity.add(force);
         if (this.collideMovement(player, player.getBoundingBox(), step).distanceToSqr(step) > 1.0E-8) {
            return false;
         }

         if (!brakeStaysOnFloor(
            player.getBoundingBox(), velocity, force, this.groundDrag(footing), acceleration * 0.6, box -> this.hasFloorUnderFootprint(box, player.getY(), 0.125)
         )) {
            return false;
         }

         this.forwardKeyState = ScaffoldModule.ForwardKeyState.FORWARD;
         this.recoveryInput = null;
         this.run_Z = false;
         return true;
      } else {
         return false;
      }
   }

   static int init_I_Vec3(
      Vec3 position,
      Vec3 launch,
      AABB body,
      BlockPos footing,
      Direction course,
      double drag,
      Predicate<BlockPos> solid,
      Predicate<BlockPos> fillable,
      Predicate<BlockPos> inBounds
   ) {
      if (AutismLiteVariant.enabled()) {
         return -1;
      }

      BlockPos target = footing.relative(course, 2);
      if (inBounds.test(target.above()) && fillable.test(target.above())) {
         for (int step = 1; step <= 2; step++) {
            if (!solid.test(footing.relative(course, step)) && !fillable.test(footing.relative(course, step))) {
               return -1;
            }
         }

         Vec3 forward = Vec3.atLowerCornerOf(course.getUnitVec3i());
         double feetY = footing.getY() + 2.0;

         for (int power = 8; power >= 0; power--) {
            Vec3 at = position.add(launch);
            Vec3 motion = new Vec3(launch.x * drag, (launch.y - 0.08) * 0.98, launch.z * drag);
            Vec3 fastAt = at;
            Vec3 fastMotion = motion;
            Vec3[] path = new Vec3[14];
            path[0] = at;
            int flight = 1;

            for (int tick = 1; tick < path.length; tick++) {
               if (tick <= power) {
                  motion = motion.add(forward.scale(0.0196));
                  fastMotion = fastMotion.add(forward.scale(0.026));
               }

               at = at.add(motion);
               path[tick] = at;
               flight = tick + 1;
               fastAt = fastAt.add(fastMotion);
               motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
               fastMotion = new Vec3(fastMotion.x * 0.91, (fastMotion.y - 0.08) * 0.98, fastMotion.z * 0.91);
               if (at.y <= feetY && motion.y < 0.0) {
                  break;
               }
            }

            Vec3 landing = new Vec3(at.x, feetY, at.z);
            if (overlapsBlock(body.move(landing.subtract(position)), target.above(), 0.125)) {
               Vec3 fastLanding = new Vec3(fastAt.x, feetY, fastAt.z);
               if (overlapsBlock(body.move(fastLanding.subtract(position)), target.above(), 0.125)) {
                  int ready = 2;
                  int missing = 0;

                  for (int step = 1; step <= 2; step++) {
                     if (!solid.test(footing.relative(course, step))) {
                        missing++;
                        double plane = Vec3.atCenterOf(footing.relative(course, step - 1)).dot(forward) + 0.5;
                        int visible = 1;

                        while (visible < flight && path[visible].dot(forward) < plane + 0.35) {
                           visible++;
                        }

                        ready = Math.max(ready, visible) + 2;
                     }
                  }

                  double nearTop = Vec3.atCenterOf(target).dot(forward) - 0.5 + 0.17;
                  int topVisible = 1;

                  while (topVisible < flight && (path[topVisible].dot(forward) < nearTop || path[topVisible].y < feetY + 0.001)) {
                     topVisible++;
                  }

                  int topLook = topVisible + (missing == 0 ? 1 : 0);
                  if (Math.max(ready, Math.max(3, topLook)) + 1 < flight) {
                     return power;
                  }
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private void close_V(LocalPlayer player) {
      if (!this.isEagleMode()
         && this.ownsInput
         && !player.onGround()
         && !this.clear_Z
         && !this.start_Z
         && !this.next_Z()
         && !this.isAnchorDeviating()
         && !this.sneakHold.load()
         && (
            this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH
               || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING
               || this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING
         )) {
         BlockPos root = this.effectiveLaneSupport();
         if (root != null) {
            if (this.imperfectionCount == 2 && (this.imperfectionArmed || this.imperfectionRequested) && this.placeRateTracker.init_Z()) {
               this.node_Z = true;
               this.buffer_Z = true;
               this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
            } else {
               boolean returning = this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING || this.turnPhase == ScaffoldModule.TurnCyclePhase.RETURNED;
               if (returning && this.flush_I() > 1) {
                  this.node_Z = true;
                  this.f_X_I = 0;
                  this.buffer_Z = true;
                  this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
               } else {
                  int turn = returning ? 0 : this.smoother().write(this.rotationSmoothness.get());
                  int optional = this.sync_I() / 2;
                  int back = returning && this.isAimAligned()
                     ? 0
                     : (
                        returning && this.smoother().init_Z()
                           ? this.smoother().init_I()
                           : (
                              this.turnPhase == ScaffoldModule.TurnCyclePhase.FORWARD
                                 ? this.smoother().read(this.rotationSmoothness.get())
                                 : this.smoother().init(this.rotationSmoothness.get(), this.lastSentRotation(), this.anchorRotation(), RotationMath.gcdStep())
                           )
                     );
                  Predicate<BlockPos> solid = cell -> this.isSettledSolid(MC.level.getBlockState(cell), cell);
                  double groundDrag = this.groundDrag(root);
                  if (this.placeableStack().getItem() instanceof BlockItem item) {
                     groundDrag = Math.max(groundDrag, item.getBlock().getFriction() * 0.91);
                  }

                  double powered = player.isSprinting() ? 0.026 : 0.0196;
                  double walkSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (player.isSprinting() ? 1.3 : 1.0);
                  double groundBrake = groundAcceleration(walkSpeed, groundDrag / 0.91) * 0.7;
                  BlockPos published = this.init_BlockPos_LocalPlayer(player, root, optional);
                  ScaffoldModule.PushTarget push = init(
                     player.position(),
                     player.getDeltaMovement(),
                     this.anchorDirection(),
                     root,
                     this.supportY + 1.0,
                     powered,
                     turn,
                     optional,
                     back,
                     returning,
                     solid,
                     1,
                     groundDrag,
                     published,
                     groundBrake
                  );
                  if (this.imperfectionCount <= 1 && !returning) {
                     for (int ticks = 12; ticks > 1; ticks--) {
                        ScaffoldModule.PushTarget sustained = init(
                           player.position(),
                           player.getDeltaMovement(),
                           this.anchorDirection(),
                           root,
                           this.supportY + 1.0,
                           powered,
                           turn,
                           optional,
                           back,
                           false,
                           solid,
                           ticks,
                           groundDrag,
                           published,
                           groundBrake
                        );
                        if (sustained.init_Z()) {
                           push = sustained;
                           this.f_X_I = ticks;
                           break;
                        }
                     }
                  }

                  this.node_Z = true;
                  this.pushTargetTick = AutismSessionState.getInstance().getClientTick();
                  this.pushTarget = push;
                  this.buffer_Z = !push.init_Z();
                  this.forwardKeyState = push.init_Z() ? ScaffoldModule.ForwardKeyState.FORWARD : ScaffoldModule.ForwardKeyState.HOLD;
                  if (!push.init_Z()) {
                     ScaffoldModule.PushTarget coast = init(
                        player.position(),
                        player.getDeltaMovement(),
                        this.anchorDirection(),
                        root,
                        this.supportY + 1.0,
                        0.0,
                        turn,
                        optional,
                        back,
                        returning,
                        solid,
                        1,
                        groundDrag,
                        published,
                        groundBrake
                     );
                     this.pushTarget = coast;
                     if (!coast.init_Z() && !(player.getDeltaMovement().dot(this.anchorForward()) <= 0.0236)) {
                        ScaffoldModule.PushTarget brake = init(
                           player.position(),
                           player.getDeltaMovement(),
                           this.anchorDirection(),
                           root,
                           this.supportY + 1.0,
                           -0.0196,
                           turn,
                           optional,
                           back,
                           returning,
                           solid,
                           1,
                           groundDrag,
                           published,
                           groundBrake
                        );
                        this.braking = brake.init_Z();
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos init_BlockPos_LocalPlayer(LocalPlayer player, BlockPos root, int optional) {
      if (!this.stop_Z && this.f_G_I <= 0) {
         ScaffoldModule.WireClickRotation wire = this.wireClickRotation();
         if (wire != null && wire.tick() < AutismSessionState.getInstance().getClientTick()) {
            BlockHitResult ray = raycast(player.getEyePosition(), wire.load(), player.blockInteractionRange() - 0.035, MC.level, player);
            if (ray != null && this.isHitOnSupportFace(ray)) {
               BlockPos placed = ray.getBlockPos().relative(ray.getDirection());
               boolean top = optional > 0 && ray.getDirection() == Direction.UP && placed.equals(this.imperfectionCell);
               boolean floor = optional == 0 && ray.getDirection() == this.anchorDirection() && ray.getBlockPos().equals(root);
               if ((top || floor) && MC.level.getBlockState(placed).canBeReplaced()) {
                  ScaffoldModule.PlannedPlacement target = new ScaffoldModule.PlannedPlacement(ray.getBlockPos(), placed, ray.getDirection(), ray, wire.load(), placed.getY());
                  return this.flush(target) ? placed : null;
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static ScaffoldModule.PushTarget init(
      Vec3 position,
      Vec3 velocity,
      Direction course,
      BlockPos root,
      double feetY,
      double firstControl,
      int turn,
      int optionalTops,
      int returnTicks,
      boolean returning,
      Predicate<BlockPos> solid
   ) {
      return init(position, velocity, course, root, feetY, firstControl, turn, optionalTops, returnTicks, returning, solid, 1);
   }

   static ScaffoldModule.PushTarget init(
      Vec3 position,
      Vec3 velocity,
      Direction course,
      BlockPos root,
      double feetY,
      double firstControl,
      int turn,
      int optionalTops,
      int returnTicks,
      boolean returning,
      Predicate<BlockPos> solid,
      int powerTicks
   ) {
      return init(position, velocity, course, root, feetY, firstControl, turn, optionalTops, returnTicks, returning, solid, powerTicks, 0.546);
   }

   static ScaffoldModule.PushTarget init(
      Vec3 position,
      Vec3 velocity,
      Direction course,
      BlockPos root,
      double feetY,
      double firstControl,
      int turn,
      int optionalTops,
      int returnTicks,
      boolean returning,
      Predicate<BlockPos> solid,
      int powerTicks,
      double groundDrag
   ) {
      return init(position, velocity, course, root, feetY, firstControl, turn, optionalTops, returnTicks, returning, solid, powerTicks, groundDrag, null);
   }

   static ScaffoldModule.PushTarget init(
      Vec3 position,
      Vec3 velocity,
      Direction course,
      BlockPos root,
      double feetY,
      double firstControl,
      int turn,
      int optionalTops,
      int returnTicks,
      boolean returning,
      Predicate<BlockPos> solid,
      int powerTicks,
      double groundDrag,
      BlockPos publishedCell
   ) {
      return init(
         position,
         velocity,
         course,
         root,
         feetY,
         firstControl,
         turn,
         optionalTops,
         returnTicks,
         returning,
         solid,
         powerTicks,
         groundDrag,
         publishedCell,
         groundAcceleration(0.1, groundDrag / 0.91) * 0.7
      );
   }

   static ScaffoldModule.PushTarget init(
      Vec3 position,
      Vec3 velocity,
      Direction course,
      BlockPos root,
      double feetY,
      double firstControl,
      int turn,
      int optionalTops,
      int returnTicks,
      boolean returning,
      Predicate<BlockPos> solid,
      int powerTicks,
      double groundDrag,
      BlockPos publishedCell,
      double groundBrake
   ) {
      Vec3 forward = Vec3.atLowerCornerOf(course.getUnitVec3i());
      Vec3 at = position;
      Vec3 motion = velocity;
      Vec3[] path = new Vec3[14];
      int flight = 0;

      for (int tick = 0; tick < path.length; tick++) {
         if (tick == 0) {
            motion = motion.add(forward.scale(firstControl));
         } else if (tick < powerTicks && firstControl > 0.0) {
            motion = motion.add(forward.scale(0.0196));
         }

         at = at.add(motion);
         path[tick] = at;
         flight = tick + 1;
         motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
         if (at.y <= feetY + 1.0E-9 && motion.y < 0.0) {
            break;
         }
      }

      Vec3 landing = new Vec3(at.x, feetY, at.z);
      double rootAlong = Vec3.atCenterOf(root).dot(forward);
      int groundWait = 0;
      int missing = 0;
      int lastUse = -1;

      for (int pass = 0; pass < 4; pass++) {
         Vec3 held = landing;
         if (groundWait > 0) {
            held = held.add(forward.scale(coastTravel(motion.dot(forward), groundDrag, groundWait, groundBrake)));
         }

         int distance = Math.max(0, (int)Math.ceil(held.dot(forward) - rootAlong - 0.68));
         if (distance > 6) {
            return new ScaffoldModule.PushTarget(false, distance, flight, flight, landing);
         }

         boolean publishedTop = optionalTops > 0 && publishedCell != null && publishedCell.getY() > root.getY();
         int ready = Math.max(0, turn - 1) + Math.max(0, optionalTops) * 2 - (publishedTop ? 1 : 0);
         missing = 0;
         lastUse = optionalTops > 0 ? ready - 1 : -1;

         for (int step = 1; step <= distance; step++) {
            BlockPos cell = root.relative(course, step);
            if (!solid.test(cell)) {
               missing++;
               double face = rootAlong + step - 0.5;
               int visible = 0;

               while (visible < flight && path[visible].dot(forward) < face + 0.05) {
                  visible++;
               }

               boolean publishedFloor = optionalTops == 0 && missing == 1 && cell.equals(publishedCell);
               int look = Math.max(ready, visible);
               lastUse = publishedFloor ? 0 : look + 1;
               ready = lastUse + 1;
            }
         }

         if (returning && missing > 0 || lastUse >= flight) {
            return new ScaffoldModule.PushTarget(false, missing, flight, lastUse, landing);
         }

         int wait = Math.max(0, returnTicks - Math.max(0, flight - lastUse));
         if (wait <= groundWait) {
            return new ScaffoldModule.PushTarget(true, missing, flight, lastUse, landing);
         }

         groundWait = wait;
      }

      return new ScaffoldModule.PushTarget(false, missing, flight, lastUse, landing);
   }

   static double coastTravel(double velocity, double drag, int wait, double brake) {
      if (wait <= 0) {
         return 0.0;
      }

      double motion = Math.max(0.0, velocity);
      double travel = 0.0;
      double friction = Mth.clamp(drag, 0.0, 0.99);

      for (int tick = 0; tick < wait; tick++) {
         travel += motion;
         motion *= friction;
      }

      return travel + brakingDistance(motion, friction, Math.max(0.0, brake));
   }

   static ScaffoldModule.PredictedTickStep init_aI_ArrayUtil_Vec3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward, boolean brake) {
      Vec3 at = position;
      Vec3 motion = velocity;

      for (int tick = 0; tick < 14; tick++) {
         if (brake && tick == 0) {
            motion = motion.subtract(forward.scale(0.0196));
         }

         at = at.add(motion);
         Vec3 next = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
         if (motion.y <= 0.0 && at.y <= feetY + 1.0E-9) {
            return new ScaffoldModule.PredictedTickStep(new Vec3(at.x, feetY, at.z), new Vec3(next.x, 0.0, next.z), tick + 1);
         }

         motion = next;
      }

      return new ScaffoldModule.PredictedTickStep(new Vec3(at.x, feetY, at.z), motion, 14);
   }

   private boolean push(LocalPlayer player) {
      if (this.isEagleMode()
         || !this.ownsInput
         || player.onGround()
         || !this.spaceHeld
         || this.pop_Z
         || this.sneakHold.load()
         || this.isAnchorDeviating()
         || this.next_Z()
         || this.queue_Z) {
         return false;
      }

      if (!this.clear_Z) {
         return false;
      }

      if (this.write_BlockPos != null && this.clear_Z && !this.start_Z) {
         this.buffer_Z = this.tellyAirTicks >= this.batchPower;
         this.forwardKeyState = this.buffer_Z ? ScaffoldModule.ForwardKeyState.HOLD : ScaffoldModule.ForwardKeyState.FORWARD;
         this.node_Z = true;
         this.f_X_I = Math.max(0, this.batchPower - this.tellyAirTicks);
         return false;
      }

      this.buffer_Z = true;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
      Vec3 forward = this.anchorForward();
      Vec3 velocity = player.getDeltaMovement();
      if (velocity.dot(forward) <= 0.0196) {
         return false;
      }

      int rise = this.clear_Z ? 1 : 0;
      double feetY = this.supportY + 1.0 + rise;
      ScaffoldModule.PredictedTickStep coast = init_aI_ArrayUtil_Vec3(player.position(), velocity, feetY, forward, false);
      if (coast.ticks() < 2) {
         return false;
      }

      ScaffoldModule.PredictedTickStep eased = init_aI_ArrayUtil_Vec3(player.position(), velocity, feetY, forward, true);
      AABB easedBody = player.getBoundingBox().move(eased.position().subtract(player.position()));
      BlockPos projected = this.alignToLane(BlockPos.containing(coast.position().x, this.supportY, coast.position().z)).above(rise);
      BlockPos base = projected.below(rise);
      BlockPos footing = this.close_BlockPos != null
         ? this.close_BlockPos
         : (
            this.pushFooting != null && this.pushFooting.getY() == projected.getY()
               ? this.pushFooting
               : init_BlockPos_BlockPos_2(projected, easedBody, this.anchorDirection(), this.isSettledSolid(MC.level.getBlockState(base), base))
         );
      AABB landed = player.getBoundingBox().move(coast.position().subtract(player.position()));
      if (this.init_Z_LocalPlayer_2(player, footing, coast.position(), coast.load(), landed)) {
         return false;
      }

      boolean safe = eased.load().dot(forward) > 0.004 && overlapsBlock(easedBody, footing, 0.12);
      if (safe) {
         this.pushFooting = footing.immutable();
      }

      return safe;
   }

   static BlockPos init_BlockPos_BlockPos_2(BlockPos projected, AABB easedBody, Direction course, boolean projectedFloorSolid) {
      BlockPos back = projected.relative(course.getOpposite());
      return !projectedFloorSolid && overlapsBlock(easedBody, back, 0.12) ? back : projected;
   }

   static boolean load_Z_I(int flight, int missing, int turn, int finalFaceLead) {
      return missing >= 0 && missing <= 6 && flight >= Math.max(1, turn) + (missing + 1) * 2 - 1 && (missing == 0 || finalFaceLead >= 4);
   }

   private static boolean canReachHeight(Vec3 position, Vec3 launchVelocity, double targetFeetY) {
      double y = position.y + launchVelocity.y;
      if (y >= targetFeetY) {
         return true;
      }

      double motionY = (launchVelocity.y - 0.08) * 0.98;

      for (int tick = 1; tick < 14; tick++) {
         y += motionY;
         if (y >= targetFeetY) {
            return true;
         }

         if (motionY <= 0.0) {
            return false;
         }

         motionY = (motionY - 0.08) * 0.98;
      }

      return false;
   }

   static boolean init_Z_Z_2(boolean projectedLandingCatchable) {
      return !projectedLandingCatchable;
   }

   static ScaffoldModule.TellyPhase nextTellyPhase(ScaffoldModule.TellyPhase current, boolean chainCovered, boolean risePending) {
      if (current == ScaffoldModule.TellyPhase.RETURNING) {
         return ScaffoldModule.TellyPhase.RETURNING;
      } else {
         return chainCovered && !risePending ? ScaffoldModule.TellyPhase.RETURNING : current;
      }
   }

   private static int init_I_Vec3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward, BlockPos support, double launchDrag) {
      if (position != null && velocity != null && support != null) {
         Vec3 landing = projectLaunchLanding(position, velocity, feetY, forward, launchDrag);
         return init_I_Vec3_2(landing, forward, support);
      } else {
         return 1;
      }
   }

   static int init_I_Vec3_2(Vec3 landing, Vec3 forward, BlockPos support) {
      if (landing != null && forward != null && support != null) {
         int landingX = Mth.floor(landing.x);
         int landingZ = Mth.floor(landing.z);
         int gridDistance;
         if (Math.abs(forward.x) >= Math.abs(forward.z)) {
            gridDistance = (int)Math.round((landingX - support.getX()) * Math.signum(forward.x));
         } else {
            gridDistance = (int)Math.round((landingZ - support.getZ()) * Math.signum(forward.z));
         }

         return Mth.clamp(gridDistance, 1, 6);
      } else {
         return 1;
      }
   }

   static Vec3 projectLaunchLanding(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward) {
      return projectLaunchLanding(position, launchVelocity, feetY, forward, 0.546);
   }

   static Vec3 projectLaunchLanding(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward, double launchDrag) {
      return projectLaunchLanding(position, launchVelocity, feetY, forward, launchDrag, 0.0196);
   }

   static Vec3 projectLaunchLanding(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward, double launchDrag, double airControlPerTick) {
      if (position != null && launchVelocity != null) {
         Vec3 projected = position.add(launchVelocity);
         double horizontalDrag = Mth.clamp(launchDrag, 0.0, 1.2);
         Vec3 motion = new Vec3(launchVelocity.x * horizontalDrag, (launchVelocity.y - 0.08) * 0.98, launchVelocity.z * horizontalDrag);
         Vec3 airControl = forward != null && !(forward.horizontalDistanceSqr() <= 1.0E-10)
            ? new Vec3(forward.x, 0.0, forward.z).normalize().scale(airControlPerTick)
            : Vec3.ZERO;

         for (int tick = 1; tick < 14; tick++) {
            motion = motion.add(airControl);
            projected = projected.add(motion);
            if (motion.y <= 0.0 && projected.y <= feetY + 0.08) {
               return new Vec3(projected.x, feetY, projected.z);
            }

            motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
         }

         return new Vec3(projected.x, feetY, projected.z);
      } else {
         return position;
      }
   }

   static float snapYawToCardinal(float yaw) {
      return Mth.wrapDegrees(Math.round(Mth.wrapDegrees(yaw) / 90.0F) * 90.0F);
   }

   static Direction overhangSide(double fracX, double fracZ, boolean northVoid, boolean southVoid, boolean westVoid, boolean eastVoid) {
      double half = 0.3;
      double best = 0.05;
      Direction result = null;
      double north = half - fracZ;
      if (northVoid && north > best) {
         best = north;
         result = Direction.NORTH;
      }

      double south = fracZ + half - 1.0;
      if (southVoid && south > best) {
         best = south;
         result = Direction.SOUTH;
      }

      double west = half - fracX;
      if (westVoid && west > best) {
         best = west;
         result = Direction.WEST;
      }

      double east = fracX + half - 1.0;
      if (eastVoid && east > best) {
         result = Direction.EAST;
      }

      return result;
   }

   private Direction overhangDirection(LocalPlayer player) {
      Vec3 pos = player.position();
      BlockPos cell = BlockPos.containing(pos.x, pos.y - 0.5, pos.z);
      if (!this.isSettledSolid(MC.level.getBlockState(cell), cell)) {
         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos neighbor = cell.relative(direction);
            if (this.isSettledSolid(MC.level.getBlockState(neighbor), neighbor)) {
               return direction.getOpposite();
            }
         }

         return null;
      } else {
         double fracX = pos.x - Math.floor(pos.x);
         double fracZ = pos.z - Math.floor(pos.z);
         return overhangSide(
            fracX,
            fracZ,
            !this.isSettledSolid(MC.level.getBlockState(cell.north()), cell.north()),
            !this.isSettledSolid(MC.level.getBlockState(cell.south()), cell.south()),
            !this.isSettledSolid(MC.level.getBlockState(cell.west()), cell.west()),
            !this.isSettledSolid(MC.level.getBlockState(cell.east()), cell.east())
         );
      }
   }

   private BlockPos alignToLane(BlockPos raw) {
      return this.laneSupport == null ? raw : alignToLane(raw, this.laneSupport, this.anchorDirection());
   }

   static BlockPos alignToLane(BlockPos raw, BlockPos support, Direction direction) {
      return direction.getAxis() == Axis.Z ? new BlockPos(support.getX(), raw.getY(), raw.getZ()) : new BlockPos(raw.getX(), raw.getY(), support.getZ());
   }

   private BlockPos effectiveLaneSupport() {
      if (this.laneSupport == null) {
         return null;
      }

      if (this.isSettledSolid(MC.level.getBlockState(this.laneSupport), this.laneSupport)) {
         return this.laneSupport;
      }

      Direction back = this.anchorDirection().getOpposite();

      for (int step = 1; step <= 3; step++) {
         BlockPos candidate = this.laneSupport.relative(back, step);
         if (this.isSettledSolid(MC.level.getBlockState(candidate), candidate)) {
            this.laneSupport = candidate.immutable();
            return this.laneSupport;
         }
      }

      return null;
   }

   static boolean cellClearOfBody(AABB currentBox, Vec3 writtenOffset, BlockPos cell) {
      return currentBox != null && writtenOffset != null && cell != null
         ? !intersectsCell(cell, currentBox) && !intersectsCell(cell, currentBox.move(writtenOffset))
         : false;
   }

   static boolean canCatchOn(AABB box, Vec3 velocity, BlockPos cell) {
      return canCatchOn(box, velocity, cell, false);
   }

   static boolean canCatchOn(AABB box, Vec3 velocity, BlockPos cell, boolean fallingCatch) {
      if (box != null && cell != null) {
         AABB cellBox = new AABB(cell);
         if (box.intersects(cellBox)) {
            return false;
         }

         if (velocity != null && !(velocity.y >= 0.0)) {
            double feet = box.minY;
            double top = cellBox.maxY;
            if (feet <= top) {
               return true;
            }

            double x = 0.0;
            double z = 0.0;
            double vy = velocity.y;
            boolean crossed = false;

            for (int tick = 0; tick < 2; tick++) {
               x += velocity.x;
               feet += vy;
               z += velocity.z;
               vy = (vy - 0.08) * 0.98;
               if (!(feet >= top)) {
                  boolean firstCross = !crossed;
                  crossed = true;
                  AABB swept = box.move(x, 0.0, z);
                  boolean over = swept.maxX > cellBox.minX && swept.minX < cellBox.maxX && swept.maxZ > cellBox.minZ && swept.minZ < cellBox.maxZ;
                  if (fallingCatch && firstCross && over) {
                     return true;
                  }

                  if (over) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   static String catchDebug(AABB box, Vec3 velocity, BlockPos cell, boolean fallingCatch) {
      if (box != null && cell != null) {
         AABB cellBox = new AABB(cell);
         if (box.intersects(cellBox)) {
            return "box";
         }

         if (velocity != null && !(velocity.y >= 0.0)) {
            double feet = box.minY;
            double top = cellBox.maxY;
            if (feet <= top) {
               return "ok";
            }

            double x = 0.0;
            double z = 0.0;
            double vy = velocity.y;
            boolean crossed = false;

            for (int tick = 0; tick < 2; tick++) {
               x += velocity.x;
               feet += vy;
               z += velocity.z;
               vy = (vy - 0.08) * 0.98;
               if (!(feet >= top)) {
                  boolean firstCross = !crossed;
                  crossed = true;
                  AABB swept = box.move(x, 0.0, z);
                  boolean over = swept.maxX > cellBox.minX && swept.minX < cellBox.maxX && swept.maxZ > cellBox.minZ && swept.minZ < cellBox.maxZ;
                  if (fallingCatch && firstCross && over) {
                     return "ok";
                  }

                  if (over) {
                     return !fallingCatch ? "strict" : "side";
                  }
               }
            }

            return "ok";
         } else {
            return "ok";
         }
      } else {
         return "ok";
      }
   }

   static RotationMath.Rotation stepRotation(RotationMath.Rotation current, RotationMath.Rotation goal, float yawCap, float pitchCap, double gcd) {
      if (current == null) {
         return goal;
      }

      if (goal == null) {
         return current;
      }

      RotationMath.Rotation stepped = RotationMath.stepToward(current, goal, yawCap, pitchCap);
      if (gcd <= 0.0) {
         return stepped;
      }

      float yawDiff = RotationMath.angleDelta(stepped.yaw(), current.yaw());
      float pitchDiff = RotationMath.angleDelta(stepped.pitch(), current.pitch());
      float yaw = current.yaw() + (float)(Math.round(yawDiff / gcd) * gcd);
      float pitch = current.pitch() + (float)(Math.round(pitchDiff / gcd) * gcd);
      return new RotationMath.Rotation(yaw, Mth.clamp(pitch, -90.0F, 90.0F));
   }

   static RotationMath.Rotation stepRotation(RotationMath.Rotation current, RotationMath.Rotation goal, float stepCap, double gcd) {
      return stepRotation(current, goal, stepCap, stepCap, gcd);
   }

   static RotationMath.Rotation stepRotationQuantized(RotationMath.Rotation current, RotationMath.Rotation goal, float yawCap, float pitchCap, double gcd) {
      if (current == null) {
         return goal;
      } else {
         return goal == null ? current : RotationMath.stepQuantized(current, goal, yawCap, pitchCap, gcd);
      }
   }

   private static double gcdOrDefault(double gcd) {
      return Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
   }

   static boolean isLaneSettled(float smoothedYaw, float anchorYaw, double velAlongCourse, double velCrossCourse, double laneError, boolean onGround) {
      if (!onGround) {
         return false;
      }

      if (Math.abs(RotationMath.angleDelta(anchorYaw, smoothedYaw)) > 1.0F) {
         return false;
      }

      if (Math.abs(laneError) > 0.45) {
         return false;
      }

      double speed = Math.sqrt(velAlongCourse * velAlongCourse + velCrossCourse * velCrossCourse);
      if (speed < 0.08) {
         return true;
      }

      if (velAlongCourse <= 0.0) {
         return false;
      }

      double angle = Math.toDegrees(Math.atan2(Math.abs(velCrossCourse), velAlongCourse));
      return angle <= 25.0;
   }

   static float courseAnchorYaw(boolean latched, float retainedYaw, float measuredTravelYaw) {
      return latched && Float.isFinite(retainedYaw) ? Mth.wrapDegrees(retainedYaw) : snapYawToCardinal(measuredTravelYaw);
   }

   static boolean isFreshJumpPress(boolean physicalForward, boolean physicalSpace, boolean physicalSpaceWasDown, boolean ownsInput) {
      return physicalForward && physicalSpace ? !physicalSpaceWasDown || !ownsInput : false;
   }

   static boolean isTellyActive(ScaffoldModule.TellyPhase phase) {
      return phase != null && phase != ScaffoldModule.TellyPhase.IDLE;
   }

   private Vec3 anchorForward() {
      return forwardFromYaw(this.anchorYaw);
   }

   static Vec3 forwardFromYaw(float anchorYaw) {
      float radians = anchorYaw * 0.017453292F;
      return new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
   }

   private Vec3 anchorLeft() {
      Vec3 forward = this.anchorForward();
      return new Vec3(forward.z, 0.0, -forward.x);
   }

   private Direction anchorDirection() {
      return cardinalDirection(this.anchorYaw);
   }

   static Direction cardinalDirection(float yaw) {
      int quadrant = Math.floorMod(Math.round(yaw / 90.0F), 4);
      Direction var10000;
      switch (quadrant) {
         case 0:
            var10000 = Direction.SOUTH;
            break;
         case 1:
            var10000 = Direction.WEST;
            break;
         case 2:
            var10000 = Direction.NORTH;
            break;
         default:
            var10000 = Direction.EAST;
      }

      return var10000;
   }

   static Vec3 laneCentre(BlockPos support, Vec3 playerPosition, float yaw) {
      boolean alongZ = Math.floorMod(Math.round(yaw / 90.0F), 2) == 0;
      return alongZ ? new Vec3(support.getX() + 0.5, playerPosition.y, playerPosition.z) : new Vec3(playerPosition.x, playerPosition.y, support.getZ() + 0.5);
   }

   private static double lateralOffset(Vec3 position, float yaw) {
      float radians = yaw * 0.017453292F;
      Vec3 left = new Vec3(Mth.cos(radians), 0.0, Mth.sin(radians));
      return position.dot(left);
   }

   private RotationMath.Rotation anchorRotation() {
      return new RotationMath.Rotation(this.anchorFacingYaw(), this.anchorPitch);
   }

   private RotationMath.Rotation offsetAnchorRotation() {
      return new RotationMath.Rotation(Mth.wrapDegrees(this.anchorYaw + this.block_F), this.anchorPitch);
   }

   private float anchorFacingYaw() {
      return Mth.wrapDegrees(this.anchorYaw + (this.isEagleMode() ? 180.0F : 0.0F));
   }

   private RotationMath.Rotation chooseAim(LocalPlayer player) {
      if (this.size_Z) {
         if (this.aimedThisTick() && this.plannedAim != null) {
            return this.plannedAim;
         }

         this.size_Z = false;
      }

      if (this.aimedThisTick() && this.plannedAim != null) {
         return this.plannedAim;
      }

      ScaffoldModule.AimGoal goal = this.aimGoal(player);
      if (goal.mode() == ScaffoldModule.AimMode.RETURN) {
         return this.close_ce_DataUtil();
      }

      if (isPlacementAim(goal.mode())) {
         this.update_Z = false;
         this.data_Z = false;
         return this.aimTowards(goal.rotation());
      }

      if (goal.mode() == ScaffoldModule.AimMode.HOLD) {
         return this.holdAim();
      }

      if (goal.mode() == ScaffoldModule.AimMode.FORWARD && !player.onGround()) {
         return this.close_ce_DataUtil();
      }

      this.update_Z = true;
      return this.aimTowards(goal.rotation());
   }

   static boolean isPlacementAim(ScaffoldModule.AimMode intent) {
      return intent == ScaffoldModule.AimMode.PLACEMENT;
   }

   private boolean aimedThisTick() {
      return this.aimTick == AutismSessionState.getInstance().getClientTick();
   }

   private RotationMath.Rotation holdAim() {
      if (this.plannedAim == null) {
         this.plannedAim = this.lastSentRotation();
      }

      this.aimTick = AutismSessionState.getInstance().getClientTick();
      this.activeRotation = this.plannedAim;
      this.rotationHoldTicks = 5;
      return this.plannedAim;
   }

   private RotationMath.Rotation aimTowards(RotationMath.Rotation goal) {
      return this.aimTowards(goal, this.rotationSpeedCap());
   }

   private RotationMath.Rotation aimTowards(RotationMath.Rotation goal, float cap) {
      RotationMath.Rotation planned = this.quantizedPlacement != null && this.quantizedPlacement.tick() == AutismSessionState.getInstance().getClientTick()
         ? this.quantizedPlacement.placement().plan().rotation()
         : this.smoother().init(this.lastSentRotation(), goal, this.update_Z, this.rotationSmoothness.get(), RotationMath.gcdStep(), AutismSessionState.getInstance().getClientTick());
      this.plannedAim = RotationMath.stepQuantized(this.lastSentRotation(), planned, cap, cap, RotationMath.gcdStep());
      this.smoother().init(this.plannedAim, AutismSessionState.getInstance().getClientTick());
      this.f_U_I = AutismSessionState.getInstance().getClientTick();
      autismclient.util.RotationStreamPlanner.begin(this.rotationStream, this.plannedAim);
      this.aimTick = AutismSessionState.getInstance().getClientTick();
      this.activeRotation = this.plannedAim;
      this.rotationHoldTicks = 5;
      return this.plannedAim;
   }

   private void commitAim(RotationMath.Rotation rotation) {
      this.plannedAim = rotation;
      this.smoother().init(rotation, AutismSessionState.getInstance().getClientTick());
      autismclient.util.RotationStreamPlanner.begin(this.rotationStream, rotation);
      this.activeRotation = rotation;
      this.rotationHoldTicks = 5;
      this.aimTick = AutismSessionState.getInstance().getClientTick();
      this.size_Z = true;
      this.f_U_I = AutismSessionState.getInstance().getClientTick();
      this.data_Z = false;
   }

   private ScaffoldModule.AimGoal aimGoal(LocalPlayer player) {
      if (this.isEagleMode()) {
         return this.eagleAimGoal(player);
      }

      if (player.onGround()) {
         if (this.tellyAirTicks > 0 && this.livePlacement != null && this.supportBlockUnder(player) == null) {
            ScaffoldModule.LivePlacement catchTarget = this.refineLivePlacement(player, this.livePlacement);
            return new ScaffoldModule.AimGoal(
               catchTarget != null ? catchTarget.plan().rotation() : this.lastSentRotation(),
               catchTarget != null ? ScaffoldModule.AimMode.PLACEMENT : ScaffoldModule.AimMode.HOLD
            );
         }

         if (this.forwardKeyState == ScaffoldModule.ForwardKeyState.HOLD) {
            boolean turnReserveTarget = this.livePlacement != null
               && this.entryCell != null
               && this.entryCell.equals(this.livePlacement.plan().load());
            ScaffoldModule.LivePlacement heldTarget = !read_Z_Z_Z(this.pop_Z, this.add_Z) && !turnReserveTarget ? null : this.livePlacement;
            if (heldTarget != null) {
               return new ScaffoldModule.AimGoal(this.liveAimRotation(player, heldTarget), ScaffoldModule.AimMode.PLACEMENT);
            }
         }

         return new ScaffoldModule.AimGoal(this.valid_Z && !this.smoother().init_Z() ? this.offsetAnchorRotation() : this.anchorRotation(), ScaffoldModule.AimMode.FORWARD);
      } else {
         if (this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING || this.add_Z) {
            return new ScaffoldModule.AimGoal(this.anchorRotation(), aimModeFor(this.tellyPhase, false, this.add_Z, this.turnPhase));
         }

         if (this.turnPhase == ScaffoldModule.TurnCyclePhase.RETURNED) {
            return new ScaffoldModule.AimGoal(this.anchorRotation(), ScaffoldModule.AimMode.FORWARD);
         }

         boolean aimingPhase = !this.add_Z && (this.tellyPhase == ScaffoldModule.TellyPhase.LAUNCH || this.tellyPhase == ScaffoldModule.TellyPhase.AIMING);
         if (aimingPhase && this.clear_Z && !this.start_Z && (this.quantizedPlacement == null || !this.quantizedPlacement.placement().init_Z())) {
            ScaffoldModule.LivePlacement rise = this.planRiseFromLanding(player, this.placeableStack());
            ScaffoldModule.AimGoal preview = rise == null ? null : this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, rise);
            if (preview != null) {
               return preview;
            }
         }

         if (this.quantizedPlacement != null && this.quantizedPlacement.tick() < AutismSessionState.getInstance().getClientTick()) {
            ScaffoldModule.WireClickRotation wire = this.wireClickRotation();
            if (wire != null
               && init_Z_I_2(
                  this.quantizedPlacement.tick(),
                  AutismSessionState.getInstance().getClientTick(),
                  wire.tick(),
                  this.quantizedPlacement.placement().plan().rotation(),
                  wire.load()
               )) {
               return new ScaffoldModule.AimGoal(wire.load(), ScaffoldModule.AimMode.HOLD);
            }
         }

         ScaffoldModule.LivePlacement optional = aimingPhase && this.imperfectionArmed ? this.init_aI_DataPipe_LocalPlayer(player) : null;
         boolean windowDue = this.turnPhase == ScaffoldModule.TurnCyclePhase.PLACING || this.read_Z_LocalPlayer_I(player, this.sync_I());
         if (aimingPhase && windowDue && optional != null) {
            if (!this.init_Z_LocalPlayer_2(player, optional.plan().support(), true, false)) {
               this.buffer_Z = true;
               this.forwardKeyState = ScaffoldModule.ForwardKeyState.HOLD;
               optional = this.init_aI_DataPipe_LocalPlayer(player);
            }

            ScaffoldModule.AimGoal preview = optional == null ? null : this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, optional);
            if (preview != null) {
               return preview;
            }
         }

         if (aimingPhase && windowDue && this.livePlacement != null) {
            ScaffoldModule.AimGoal preview = this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, this.livePlacement);
            if (preview != null) {
               return preview;
            }
         }

         ScaffoldModule.AimMode gap = aimModeFor(this.tellyPhase, false, this.add_Z, this.turnPhase);
         return new ScaffoldModule.AimGoal(gap == ScaffoldModule.AimMode.HOLD && this.plannedAim != null ? this.plannedAim : this.anchorRotation(), gap);
      }
   }

   private ScaffoldModule.AimGoal eagleAimGoal(LocalPlayer player) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      if (this.livePlacement != null
         && this.livePlacement.plan().face().getAxis().isHorizontal()
         && this.livePlacement.plan().face() != this.anchorDirection()
         && !this.livePlacement.plan().load().equals(this.entryCell)
         && !this.next_Z()) {
         this.livePlacement = null;
      }

      if (this.quantizedPlacement != null) {
         ScaffoldModule.LivePlacement prepared = this.quantizedPlacement.placement();
         boolean current = this.livePlacement != null && prepared.plan().load().equals(this.livePlacement.plan().load());
         boolean stillVisible = this.quantizedPlacement.tick() == AutismSessionState.getInstance().getClientTick()
            || this.init_aI_TableIndex_aI_TableIndex(prepared.plan()) != null;
         if (!stillVisible || !current && (!prepared.init_Z() || !this.open_Z(player))) {
            this.quantizedPlacement = null;
            this.close_Z = false;
         }
      }

      if (this.quantizedPlacement != null && this.quantizedPlacement.tick() < AutismSessionState.getInstance().getClientTick()) {
         ScaffoldModule.WireClickRotation wire = this.wireClickRotation();
         if (wire != null
            && init_Z_I_2(
               this.quantizedPlacement.tick(),
               AutismSessionState.getInstance().getClientTick(),
               wire.tick(),
               this.quantizedPlacement.placement().plan().rotation(),
               wire.load()
            )) {
            return new ScaffoldModule.AimGoal(wire.load(), ScaffoldModule.AimMode.HOLD);
         }
      }

      ScaffoldModule.LivePlacement target = !player.onGround() && this.open_Z(player)
         ? this.planRiseFromLanding(player, this.placeableStack())
         : null;
      if (target == null) {
         target = this.livePlacement;
      }

      if (target != null) {
         ScaffoldModule.AimGoal prepared = this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, target);
         if (prepared != null) {
            return prepared;
         }
      }

      return this.isAimAligned()
         ? new ScaffoldModule.AimGoal(this.plannedAim, ScaffoldModule.AimMode.HOLD)
         : new ScaffoldModule.AimGoal(new RotationMath.Rotation(this.anchorYaw + 180.0F, 82.0F), ScaffoldModule.AimMode.PLACEMENT);
   }

   static ScaffoldModule.AimMode aimModeFor(ScaffoldModule.TellyPhase phase, boolean placementGoal, boolean finishing, ScaffoldModule.TurnCyclePhase lookPhase) {
      if (finishing || phase == ScaffoldModule.TellyPhase.RETURNING) {
         return ScaffoldModule.AimMode.RETURN;
      } else if (lookPhase == ScaffoldModule.TurnCyclePhase.RETURNED) {
         return ScaffoldModule.AimMode.FORWARD;
      } else if (placementGoal) {
         return ScaffoldModule.AimMode.PLACEMENT;
      } else {
         return lookPhase == ScaffoldModule.TurnCyclePhase.PLACING ? ScaffoldModule.AimMode.HOLD : ScaffoldModule.AimMode.FORWARD;
      }
   }

   private boolean pop(LocalPlayer player) {
      return this.read_Z_LocalPlayer_I(player, this.sync_I());
   }

   private boolean read_Z_LocalPlayer_I(LocalPlayer player, int optionalIntervals) {
      if (this.next_Z()) {
         return true;
      }

      BlockPos root = this.effectiveLaneSupport();
      if (root == null) {
         return true;
      }

      boolean rise = this.clear_Z && !this.start_Z && this.open_Z(player);
      int blockY = this.start_Z ? this.supportY + 1 : this.supportY;
      double feetY = blockY + (rise ? 2.0 : 1.0);
      Vec3 landing = projectAirControlLanding(player.position(), player.getDeltaMovement(), feetY, this.anchorForward());
      BlockPos cell = this.alignToLane(BlockPos.containing(landing.x, blockY, landing.z));
      Direction direction = this.anchorDirection();
      int distance = Math.max(0, (cell.getX() - root.getX()) * direction.getStepX() + (cell.getZ() - root.getZ()) * direction.getStepZ());
      int missing = 0;

      for (int step = 1; step <= Math.min(8, distance); step++) {
         BlockPos needed = root.relative(direction, step);
         if (!this.isSettledSolid(MC.level.getBlockState(needed), needed)) {
            missing++;
         }
      }

      return ticksToLanding(player.position(), player.getDeltaMovement(), feetY)
         <= init_I_I_2(missing, rise, this.smoother().load(this.rotationSmoothness.get()), optionalIntervals, this.turnDelayTicks());
   }

   private int turnDelayTicks() {
      return TURN_DELAY_TICKS[Mth.clamp(this.integer("turn-delay"), 0, MAX_TURN_DELAY_INDEX)];
   }

   static int init_I_I_2(int missing, boolean rise, int turnTicks, int optionalIntervals, int delay) {
      int links = Math.max(1, missing + (rise ? 1 : 0));
      int minimum = links * 2 + Math.max(1, turnTicks) + optionalIntervals;
      int preferred = links * 2 + 6 - delay + Math.max(1, turnTicks) - 2 + optionalIntervals;
      return Math.max(minimum, preferred);
   }

   private float rotationSpeedCap() {
      float cap = this.smoother().init(this.rotationSmoothness.get(), this.update_Z);
      return this.isEagleMode() ? Math.min(18.0F, cap) : cap;
   }

   private ScaffoldRotationSmoother smoother() {
      if (this.rotationSmoother == null) {
         this.rotationSmoother = ScaffoldRotationSmoother.create();
      }

      return this.rotationSmoother;
   }

   private ScaffoldRotationSmoother.Inner_DataUtil smootherBand() {
      ValueRange band = this.rotationSmoothness.get();
      if (this.cachedSmootherBand == null
         || this.cachedSmootherBand.init_D() < band.min()
         || this.cachedSmootherBand.init_D() > band.max()
         || this.cachedSmootherBand.load_D() < band.min()
         || this.cachedSmootherBand.load_D() > band.max()) {
         this.cachedSmootherBand = this.smoother().init_aM_DataUtil_ValueRange(band);
      }

      return this.cachedSmootherBand;
   }

   private int sync_I() {
      return this.imperfectionArmed && !this.clear_Z && !this.spaceHeld && !this.next_Z() ? this.placeRateTracker.init_I() * 2 : 0;
   }

   private int flush_I() {
      return !this.isEagleMode() && !this.spaceHeld && !this.set_Z ? this.placeRateTracker.init(this.integer("imperfection"), this.random) : 0;
   }

   private boolean write_Z_LocalPlayer_I(LocalPlayer player, int blockY) {
      if (this.forwardKeyState == ScaffoldModule.ForwardKeyState.FORWARD && !this.start_Z) {
         BlockPos root = this.effectiveLaneSupport();
         if (root == null) {
            return true;
         }

         double feetY = blockY + 1.0;
         Vec3 landing = this.projectLedge(player, feetY).point();
         BlockPos cell = this.alignToLane(BlockPos.containing(landing.x, blockY, landing.z));
         Direction direction = this.anchorDirection();
         int distance = Math.max(0, (cell.getX() - root.getX()) * direction.getStepX() + (cell.getZ() - root.getZ()) * direction.getStepZ());
         int missing = 0;

         for (int step = 1; step <= Math.min(8, distance); step++) {
            BlockPos needed = root.relative(direction, step);
            if (!this.isSettledSolid(MC.level.getBlockState(needed), needed)) {
               missing++;
            }
         }

         int turnTicks = this.turnPhase == ScaffoldModule.TurnCyclePhase.FORWARD
            ? this.smoother().load(this.rotationSmoothness.get())
            : (this.turnPhase == ScaffoldModule.TurnCyclePhase.PLACING ? this.smoother().write(this.rotationSmoothness.get()) : 0);
         int remaining = ticksToLanding(player.position(), player.getDeltaMovement(), feetY);
         int needed = requiredFlightTicks(missing, this.turnPhase == ScaffoldModule.TurnCyclePhase.FORWARD, turnTicks, this.turnDelayTicks(), !this.isEagleMode());
         return missing > 0 && remaining <= needed + this.sync_I();
      } else {
         return false;
      }
   }

   static int requiredFlightTicks(int missing, boolean forward, int turnRemaining, int delay) {
      return requiredFlightTicks(missing, forward, turnRemaining, delay, false);
   }

   static int requiredFlightTicks(int missing, boolean forward, int turnRemaining, int delay, boolean reserveFaceVisibility) {
      int look = forward ? Math.max(1, turnRemaining) : Math.max(0, turnRemaining - (reserveFaceVisibility ? 0 : 2));
      int required = Math.max(0, missing) * (reserveFaceVisibility ? 3 : 2) + look;
      return Math.max(required, required + 1 - delay);
   }

   static boolean init_Z_I_5(int remainingFlightTicks, int missingBlocks, boolean rise, int delayTicks) {
      return remainingFlightTicks <= Math.max(1, missingBlocks + (rise ? 1 : 0)) * 2 + 6 - delayTicks;
   }

   private ScaffoldModule.AimGoal init_aI_UtilCore_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.LivePlacement placement) {
      if (!this.run_Z && this.turnPhase != ScaffoldModule.TurnCyclePhase.RETURNED) {
         Vec3 motion = player.getDeltaMovement().add(this.airControlVector().scale(this.isEagleMode() && player.onGround() && this.read_BlockPos != null ? 0.0 : 0.0196));
         Vec3 moved = this.collideMovement(player, player.getBoundingBox(), motion);
         Vec3 eye = player.getEyePosition().add(moved);
         ScaffoldModule.LivePlacement future = this.refineLivePlacement(player, placement, eye, player.getBoundingBox().move(moved));
         if (future == null) {
            return null;
         }

         if (!AutismLiteVariant.enabled()
            && !this.isEagleMode()
            && !this.clear_Z
            && !this.spaceHeld
            && !this.valid_Z()
            && future.plan().face().getAxis().isHorizontal()
            && ticksToLanding(player.position(), player.getDeltaMovement(), this.supportY + 1.0) <= 2) {
            Vec3 nextEye = new Vec3(
               eye.x + motion.x * 0.91, Math.max(this.supportY + 1.0 + player.getEyeHeight(), eye.y + (motion.y - 0.08) * 0.98), eye.z + motion.z * 0.91
            );
            ScaffoldModule.PlannedPlacement target = future.plan();
            RotationMath.Rotation carry = init_ce_DataUtil_BlockPos_2(
               target.support(),
               target.face(),
               eye,
               nextEye,
               this.lastSentRotation(),
               target.rotation(),
               this.anchorYaw,
               RotationMath.gcdStep(),
               player.blockInteractionRange() - 0.035
            );
            if (carry != null && this.rotationReachesFace(eye, carry, target.support(), target.face(), player.blockInteractionRange())) {
               BlockHitResult hit = raycast(eye, carry, player.blockInteractionRange(), MC.level, player);
               future = new ScaffoldModule.LivePlacement(
                  new ScaffoldModule.PlannedPlacement(target.support(), target.load(), target.face(), hit, carry, target.minHitY()), future.init_Z()
               );
            }
         }

         ScaffoldModule.PlannedPlacement quantized = this.smoothAim(future.plan(), eye);
         if (quantized == null) {
            this.quantizedPlacement = null;
            this.close_Z = false;
            this.turnPhase = ScaffoldModule.TurnCyclePhase.PLACING;
            return new ScaffoldModule.AimGoal(future.plan().rotation(), ScaffoldModule.AimMode.PLACEMENT);
         }

         if (!this.isEagleMode()
            && !player.onGround()
            && !this.clear_Z
            && !this.spaceHeld
            && !this.pop_Z
            && !this.sneakHold.load()
            && !this.isAnchorDeviating()
            && this.forwardKeyState == ScaffoldModule.ForwardKeyState.FORWARD
            && !this.braking
            && quantized.face().getAxis().isHorizontal()) {
            boolean stable = init_Z_F(
               this.anchorYaw,
               quantized.rotation().yaw(),
               0.0196 * (player.isCrouching() ? 0.3 : 1.0),
               force -> {
                  Vec3 actual = this.collideMovement(player, player.getBoundingBox(), player.getDeltaMovement().add(force));
                  return this.rotationReachesFace(
                     player.getEyePosition().add(actual),
                     quantized.rotation(),
                     quantized.support(),
                     quantized.face(),
                     player.blockInteractionRange()
                  );
               }
            );
            if (!stable) {
               this.quantizedPlacement = null;
               this.close_Z = false;
               return new ScaffoldModule.AimGoal(this.plannedAim != null ? this.plannedAim : this.lastSentRotation(), ScaffoldModule.AimMode.HOLD);
            }
         }

         this.quantizedPlacement = new ScaffoldModule.QuantizedPlacement(new ScaffoldModule.LivePlacement(quantized, placement.init_Z()), AutismSessionState.getInstance().getClientTick());
         this.turnPhase = ScaffoldModule.TurnCyclePhase.PLACING;
         return new ScaffoldModule.AimGoal(quantized.rotation(), ScaffoldModule.AimMode.PLACEMENT);
      } else {
         return null;
      }
   }

   static RotationMath.Rotation init_ce_DataUtil_BlockPos_2(
      BlockPos support, Direction face, Vec3 eye, Vec3 nextEye, RotationMath.Rotation from, RotationMath.Rotation normal, float course, double gcd, double reach
   ) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      AABB box = new AABB(support);
      Vec3 ray = Vec3.directionFromRotation(normal.pitch(), normal.yaw()).scale(reach);
      if (isHitInsetOnFace(box.clip(eye, eye.add(ray)).orElse(null), box, face) && isHitInsetOnFace(box.clip(nextEye, nextEye.add(ray)).orElse(null), box, face)) {
         return null;
      }

      double side = RotationMath.angleDelta(normal.yaw(), course);
      RotationMath.Rotation best = null;
      double cost = 1.0 / 0.0;

      for (ClutchBlockPlanner.AimAngles counts : ClutchBlockPlanner.init_List_Vec3_2(
         eye, nextEye, ClutchBlockPlanner.init(support, face), support, face, support, face, reach, from.yaw(), from.pitch(), gcd, 0.02
      )) {
         RotationMath.Rotation look = new RotationMath.Rotation(from.yaw() + RotationMath.angleDelta(counts.init(), from.yaw()), counts.load());
         if (!(side * RotationMath.angleDelta(look.yaw(), course) <= 0.0)) {
            double travel = Math.hypot(RotationMath.angleDelta(look.yaw(), from.yaw()), look.pitch() - from.pitch());
            if (travel < cost) {
               best = look;
               cost = travel;
            }
         }
      }

      return best;
   }

   static boolean init_Z_F(float course, float yaw, double acceleration, Predicate<Vec3> valid) {
      float angle = yaw * 0.017453292F;

      for (int octant = 0; octant < 8; octant++) {
         double residual = Mth.wrapDegrees(yaw + octant * 45.0F - course) * 0.017453292F;
         if (!(Math.cos(residual) < 0.7)) {
            Input keys = inputForOctant(octant, new Input(true, false, false, false, false, false, true));
            Vec3 local = new Vec3(axisValue(keys.left(), keys.right()), 0.0, axisValue(keys.forward(), keys.backward())).normalize();
            Vec3 direction = new Vec3(local.x * Mth.cos(angle) - local.z * Mth.sin(angle), 0.0, local.z * Mth.cos(angle) + local.x * Mth.sin(angle));
            if (!valid.test(direction.scale(acceleration))) {
               return false;
            }

            if (keys.forward() && !valid.test(direction.scale(acceleration * 1.3))) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean init_Z_I_2(int preparedTick, int currentTick, int writtenTick, RotationMath.Rotation prepared, RotationMath.Rotation written) {
      return prepared != null
         && written != null
         && preparedTick < currentTick
         && writtenTick < currentTick
         && Math.abs(RotationMath.angleDelta(prepared.yaw(), written.yaw())) <= 0.002F
         && Math.abs(prepared.pitch() - written.pitch()) <= 0.002F;
   }

   static double eyePastEdge(Vec3 from, Vec3 to, Vec3 eye, Direction side) {
      return eyePastEdge(from, to, eye, side, eye);
   }

   static double eyePastEdge(Vec3 from, Vec3 to, Vec3 eye, Direction side, Vec3 laneReference) {
      if (side.getAxis() == Axis.Z) {
         double edge = laneReference.x >= (from.x + to.x) * 0.5 ? from.x : to.x;
         return (eye.x - edge) / side.getStepZ();
      } else {
         double edge = laneReference.z >= (from.z + to.z) * 0.5 ? from.z : to.z;
         return (edge - eye.z) / side.getStepX();
      }
   }

   private Vec3 snapEyeToLane(Vec3 eye) {
      Vec3 left = this.anchorLeft();
      return eye.add(left.scale(this.laneOffset - eye.dot(left)));
   }

   static Vec3 offsetLanePoint(BlockPos support, Vec3 position, float yaw) {
      Direction course = cardinalDirection(yaw);
      return course.getAxis() == Axis.Z
         ? new Vec3(support.getX() + (position.x - support.getX() < 0.5 ? 0.25 : 0.75), position.y, position.z)
         : new Vec3(position.x, position.y, support.getZ() + (position.z - support.getZ() < 0.5 ? 0.25 : 0.75));
   }

   private Vec3 lanePointFor(BlockPos support, Vec3 position, float yaw) {
      return this.isEagleMode() ? laneCentre(support, position, yaw) : offsetLanePoint(support, position, yaw);
   }

   static Vec3 supportAimPoint(AABB support, Vec3 eye, Vec3 forward) {
      return supportAimPoint(support, eye, forward, eye);
   }

   static Vec3 supportAimPoint(AABB support, Vec3 eye, Vec3 forward, Vec3 laneReference) {
      double marginX = Math.min(0.12, support.getXsize() * 0.2);
      double marginZ = Math.min(0.12, support.getZsize() * 0.2);
      Vec3 point = new Vec3(
         Mth.clamp(eye.x - forward.x * 0.16, support.minX + marginX, support.maxX - marginX),
         support.maxY,
         Mth.clamp(eye.z - forward.z * 0.16, support.minZ + marginZ, support.maxZ - marginZ)
      );
      if (laneReference != null && Math.abs(forward.z) > 0.5) {
         point = new Vec3(laneReference.x >= support.getCenter().x ? support.minX + marginX : support.maxX - marginX, point.y, point.z);
      } else if (laneReference != null && Math.abs(forward.x) > 0.5) {
         point = new Vec3(point.x, point.y, laneReference.z >= support.getCenter().z ? support.minZ + marginZ : support.maxZ - marginZ);
      }

      return point.subtract(eye).dot(forward) < -0.0125 ? point : null;
   }

   private void resetImperfection() {
      this.imperfectionCount = 0;
      this.imperfectionArmed = false;
      this.imperfectionRequested = false;
      this.imperfectionCell = null;
      this.imperfectionSupports = List.of();
   }

   private void level_V() {
      this.imperfectionArmed = false;
      if (this.quantizedPlacement != null
         && this.imperfectionCell != null
         && this.imperfectionCell.equals(this.quantizedPlacement.placement().plan().load())) {
         this.quantizedPlacement = null;
         this.close_Z = false;
      }

      if (!this.imperfectionRequested) {
         this.imperfectionCell = null;
      }
   }

   private void open_V(LocalPlayer player) {
      if (this.spaceHeld || this.set_Z) {
         this.level_V();
      }
   }

   static boolean init_Z_aI_ValueSink(ScaffoldModule.TellyPhase phase, ScaffoldModule.TurnCyclePhase look, int remaining, int turning) {
      return (phase == ScaffoldModule.TellyPhase.LAUNCH || phase == ScaffoldModule.TellyPhase.AIMING)
         && look != ScaffoldModule.TurnCyclePhase.RETURNED
         && remaining >= Math.max(0, turning) + 2;
   }

   static List<BlockPos> init_List_BlockPos_2(BlockPos root, Direction forward, int count) {
      if (root != null && forward != null && !forward.getAxis().isVertical()) {
         int tops = Math.clamp(count, 0, 2);
         List<BlockPos> supports = new ArrayList<>(tops + 3);

         for (int back = tops - 1; back >= 0; back--) {
            supports.add(root.relative(forward.getOpposite(), back).immutable());
         }

         if (tops > 1) {
            for (int back = 2; back <= 3; back++) {
               supports.add(root.relative(forward.getOpposite(), back).above().immutable());
            }

            supports.add(root.above().immutable());
         } else if (tops == 1) {
            supports.add(root.relative(forward.getOpposite()).immutable());
         }

         return List.copyOf(supports);
      } else {
         return List.of();
      }
   }

   private boolean tick(LocalPlayer player) {
      return (this.imperfectionArmed || this.imperfectionRequested)
         && this.placeRateTracker.init_Z()
         && !this.clear_Z
         && !this.spaceHeld
         && !this.next_Z()
         && !this.isAnchorDeviating()
         && player != null
         && !player.onGround()
         && this.turnPhase != ScaffoldModule.TurnCyclePhase.RETURNED
         && ticksToLanding(player.position(), player.getDeltaMovement(), this.supportY + 1.0) >= this.placeRateTracker.init_I() * 2;
   }

   private ScaffoldModule.LivePlacement init_aI_DataPipe_LocalPlayer(LocalPlayer player) {
      if (this.imperfectionArmed && this.tick(player)) {
         boolean deferredPastArc = false;
         Vec3 motion = player.getDeltaMovement().add(this.airControlVector().scale(0.0196));
         Vec3 moved = this.collideMovement(player, player.getBoundingBox(), motion);
         Vec3 eye = player.getEyePosition().add(moved);
         Vec3 following = eye.add(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91).add(this.airControlVector().scale(0.0196));

         for (BlockPos support : this.imperfectionSupports) {
            if (this.init_Z_LocalPlayer_5(player, support, true)) {
               RotationMath.Rotation current = this.lastSentRotation();
               BlockHitResult carried = raycast(eye, current, player.blockInteractionRange() - 0.035, MC.level, player);
               Vec3 point = supportAimPoint(new AABB(support), eye, this.anchorForward(), this.snapEyeToLane(eye));
               boolean holds = carried != null
                  && carried.getBlockPos().equals(support)
                  && carried.getDirection() == Direction.UP
                  && this.isHitOnSupportFace(carried)
                  && (!this.valid_Z() || Math.abs(RotationMath.angleDelta(current.yaw(), this.anchorYaw)) >= 171.0F);
               RotationMath.Rotation rotation = holds
                  ? current
                  : (
                     point == null
                        ? null
                        : init_ce_DataUtil_BlockPos(
                           support,
                           this.anchorDirection(),
                           eye,
                           following,
                           RotationMath.lookAt(point, eye),
                           this.lastSentRotation(),
                           RotationMath.gcdStep(),
                           player.blockInteractionRange() - 0.035,
                           this.livePlacement != null
                              && support.equals(this.livePlacement.plan().support())
                              && this.livePlacement.plan().face() == this.anchorDirection()
                        )
                  );
               if (!holds && point != null && this.valid_Z()) {
                  rotation = this.init_ce_DataUtil_BlockPos(support, Direction.UP, eye, current, RotationMath.lookAt(point, eye));
               }

               if (rotation != null) {
                  if (this.imperfectionCount <= 1 && !init_Z_F(this.anchorYaw, current.yaw(), rotation.yaw(), this.f_I_I, RotationMath.gcdStep())) {
                     deferredPastArc = true;
                  } else {
                     BlockHitResult ray = raycast(eye, rotation, player.blockInteractionRange() - 0.035, MC.level, player);
                     if (ray != null && ray.getDirection() == Direction.UP && ray.getBlockPos().equals(support) && this.isHitOnSupportFace(ray)) {
                        this.imperfectionCell = support.above().immutable();
                        return new ScaffoldModule.LivePlacement(
                           new ScaffoldModule.PlannedPlacement(support.immutable(), this.imperfectionCell, Direction.UP, ray, rotation, support.getY()), false
                        );
                     }
                  }
               }
            }
         }

         if (deferredPastArc) {
            this.level_V();
         }

         return null;
      } else {
         return null;
      }
   }

   static boolean init_Z_F(float course, float current, float target, int floors, double gcd) {
      if (floors <= 0) {
         return true;
      }

      double tolerance = Double.isFinite(gcd) && gcd > 0.0 ? gcd * 1.01 : 0.1515;
      return Math.abs(RotationMath.angleDelta(target, course)) <= Math.abs(RotationMath.angleDelta(current, course)) + tolerance;
   }

   private boolean valid_Z() {
      return !this.isEagleMode() && this.imperfectionCount == 2 && this.imperfectionArmed && this.placeRateTracker.init_Z() && !this.clear_Z && !this.spaceHeld && !this.next_Z();
   }

   private boolean size_Z() {
      return this.valid_Z() || !this.isEagleMode() && this.write_BlockPos != null && this.clear_Z && !this.start_Z && !this.next_Z();
   }

   private double init_D_8() {
      return this.write_BlockPos != null && this.clear_Z ? 150.0 : 172.0;
   }

   private RotationMath.Rotation init_ce_DataUtil_BlockPos(BlockPos support, Direction face, Vec3 eye, RotationMath.Rotation from, RotationMath.Rotation normal) {
      double side = RotationMath.angleDelta(normal.yaw(), this.anchorYaw);
      double prior = RotationMath.angleDelta(from.yaw(), this.anchorYaw);
      double rear = Math.max(this.init_D_8(), prior * side > 0.0 ? Math.abs(prior) : 0.0);
      if (this.write_BlockPos != null && this.clear_Z) {
         return init_ce_DataUtil_AABB(
            supportBox(support),
            face,
            eye,
            from,
            this.anchorYaw,
            side,
            rear,
            RotationMath.gcdStep(),
            MC.player.blockInteractionRange() - 0.035,
            look -> this.rotationReachesFace(eye, look, support, face, MC.player.blockInteractionRange())
         );
      }

      float yaw = this.anchorYaw + (float)Math.copySign(rear, side);
      return init_ce_DataUtil_AABB(
         supportBox(support),
         face,
         eye,
         from,
         yaw,
         RotationMath.gcdStep(),
         MC.player.blockInteractionRange() - 0.035,
         look -> this.rotationReachesFace(eye, look, support, face, MC.player.blockInteractionRange())
      );
   }

   static RotationMath.Rotation init_ce_DataUtil_AABB(
      AABB box,
      Direction face,
      Vec3 eye,
      RotationMath.Rotation from,
      float course,
      double side,
      double minimumRear,
      double gcd,
      double reach,
      Predicate<RotationMath.Rotation> valid
   ) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      double rear = minimumRear;
      if (face.getAxis().isHorizontal()) {
         Vec3 forward = Vec3.directionFromRotation(0.0F, course);
         Vec3 left = new Vec3(-forward.z, 0.0, forward.x);
         double min = Math.min(box.minX * left.x, box.maxX * left.x) + Math.min(box.minZ * left.z, box.maxZ * left.z);
         double max = Math.max(box.minX * left.x, box.maxX * left.x) + Math.max(box.minZ * left.z, box.maxZ * left.z);
         double room = (side > 0.0 ? max - eye.dot(left) : eye.dot(left) - min) - 0.12;
         double plane = face == Direction.EAST ? box.maxX : (face == Direction.WEST ? box.minX : (face == Direction.SOUTH ? box.maxZ : box.minZ));
         double distance = Math.abs((face.getAxis() == Axis.X ? eye.x : eye.z) - plane);
         if (room < 0.0) {
            return null;
         }

         rear = Math.max(rear, 180.0 - Math.toDegrees(Math.atan2(room, Math.max(0.001, distance))) + 0.02);
      }

      for (double step = Math.max(0.3, gcdOrDefault(gcd) * 1.01); rear <= 178.5; rear += step) {
         float yaw = course + (float)Math.copySign(rear, side);
         RotationMath.Rotation look = init_ce_DataUtil_AABB(box, face, eye, from, yaw, gcd, reach, valid);
         if (look != null) {
            return look;
         }
      }

      return null;
   }

   static RotationMath.Rotation init_ce_DataUtil_AABB(
      AABB box, Direction face, Vec3 eye, RotationMath.Rotation from, float yaw, double gcd, double reach, Predicate<RotationMath.Rotation> valid
   ) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      RotationMath.Rotation bearing = stepRotationQuantized(from, new RotationMath.Rotation(yaw, from.pitch()), 180.0F, 180.0F, gcd);
      Vec3 horizontal = Vec3.directionFromRotation(0.0F, bearing.yaw());
      List<Vec3> points = new ArrayList<>();
      if (face.getAxis().isHorizontal()) {
         Vec3 normal = Vec3.atLowerCornerOf(face.getUnitVec3i());
         double plane = face == Direction.EAST ? box.maxX : (face == Direction.WEST ? box.minX : (face == Direction.SOUTH ? box.maxZ : box.minZ));
         double origin = face.getAxis() == Axis.X ? eye.x : eye.z;
         double direction = face.getAxis() == Axis.X ? horizontal.x : horizontal.z;
         if (horizontal.dot(normal) >= -0.001) {
            return null;
         }

         double distance = (plane - origin) / direction;
         if (distance <= 0.0 || distance > reach) {
            return null;
         }

         Vec3 at = eye.add(horizontal.scale(distance));
         if (at.x < box.minX + (face.getAxis() == Axis.Z ? 0.12 : -0.001)
            || at.x > box.maxX - (face.getAxis() == Axis.Z ? 0.12 : -0.001)
            || at.z < box.minZ + (face.getAxis() == Axis.X ? 0.12 : -0.001)
            || at.z > box.maxZ - (face.getAxis() == Axis.X ? 0.12 : -0.001)) {
            return null;
         }

         for (int step = 0; step <= 12; step++) {
            points.add(new Vec3(at.x, Mth.lerp(step / 12.0, box.minY + 0.12, box.maxY - 0.12), at.z));
         }
      } else {
         for (int step = 1; step < 96; step++) {
            Vec3 at = eye.add(horizontal.scale(reach * step / 96.0));
            if (at.x >= box.minX + 0.12 && at.x <= box.maxX - 0.12 && at.z >= box.minZ + 0.12 && at.z <= box.maxZ - 0.12) {
               points.add(new Vec3(at.x, face == Direction.UP ? box.maxY : box.minY, at.z));
            }
         }
      }

      RotationMath.Rotation best = null;
      double cost = 1.0 / 0.0;

      for (Vec3 point : points) {
         RotationMath.Rotation desired = new RotationMath.Rotation(bearing.yaw(), RotationMath.lookAt(point, eye).pitch());
         RotationMath.Rotation look = stepRotationQuantized(from, desired, 180.0F, 180.0F, gcd);
         Vec3 hit = box.clip(eye, eye.add(Vec3.directionFromRotation(look.pitch(), look.yaw()).scale(reach))).orElse(null);
         if (isHitInsetOnFace(hit, box, face) && valid.test(look)) {
            double travel = Math.hypot(RotationMath.angleDelta(look.yaw(), from.yaw()), look.pitch() - from.pitch());
            if (travel < cost) {
               cost = travel;
               best = look;
            }
         }
      }

      return best;
   }

   static RotationMath.Rotation init_ce_DataUtil_BlockPos(
      BlockPos support, Direction forward, Vec3 eye, Vec3 nextEye, RotationMath.Rotation from, double gcd, double reach
   ) {
      if (support != null && forward != null && !forward.getAxis().isVertical() && eye != null) {
         Vec3 point = supportAimPoint(new AABB(support), eye, Vec3.atLowerCornerOf(forward.getUnitVec3i()));
         return point == null ? null : init_ce_DataUtil_BlockPos(support, forward, eye, nextEye, RotationMath.lookAt(point, eye), from, gcd, reach);
      } else {
         return null;
      }
   }

   private boolean push(LocalPlayer player, BlockPos support) {
      return this.init_Z_LocalPlayer_5(player, support, false);
   }

   private boolean init_Z_LocalPlayer_5(LocalPlayer player, BlockPos support, boolean preparing) {
      if (this.imperfectionArmed
         && this.integer("imperfection") > 0
         && !player.onGround()
         && !this.clear_Z
         && !this.spaceHeld
         && !this.pop_Z
         && !this.sneakHold.load()
         && !this.add_Z
         && !this.cache_Z
         && !this.queue_Z
         && !this.next_Z()
         && !this.isAnchorDeviating()
         && support != null
         && support.getY() >= this.supportY
         && support.getY() <= this.supportY + 1
         && this.turnPhase != ScaffoldModule.TurnCyclePhase.RETURNED
         && !(player.getDeltaMovement().dot(this.anchorForward()) <= 1.0E-4)) {
         BlockPos extra = support.above();
         BlockState state = MC.level.getBlockState(support);
         if (!MC.level.isOutsideBuildHeight(extra)
            && MC.level.getBlockState(extra).isAir()
            && this.isSettledSolid(state, support)
            && state.isCollisionShapeFullBlock(MC.level, support)
            && !autismclient.util.BlockPlacementPlanner.requiresSneakToPlace(state, support)
            && isUnacknowledgedPrediction(support, this.ackedSequence, this.predictions)) {
            Vec3 forward = this.anchorForward();
            AABB body = player.getBoundingBox();
            Vec3 velocity = player.getDeltaMovement();
            if (!init_Z_AABB_6(body, velocity, forward, extra)) {
               return false;
            }

            if (preparing) {
               Vec3 move = velocity.add(this.airControlVector().scale(0.0196));
               body = body.move(this.collideMovement(player, body, move));
               velocity = new Vec3(move.x * 0.91, (move.y - 0.08) * 0.98, move.z * 0.91);
            }

            return !init_Z_AABB_6(body, velocity, forward, extra)
               ? false
               : this.init_Z_LocalPlayer_2(player, support, preparing, false) || preparing && this.init_Z_LocalPlayer_2(player, support, true, true);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_LocalPlayer_2(LocalPlayer player, BlockPos support, boolean preparing, boolean coast) {
      BlockPos root = this.effectiveLaneSupport();
      if (root == null) {
         return false;
      }

      if (!this.isEagleMode()
         && this.imperfectionCount <= 1
         && !coast
         && this.pushTargetTick == AutismSessionState.getInstance().getClientTick()
         && this.pushTarget != null
         && this.pushTarget.init_Z()) {
         return this.trackedSlotCount(player.getInventory().getSelectedSlot()) >= this.pushTarget.init_I() + this.placeRateTracker.init_I() + 1;
      }

      Vec3 landing = coast
         ? projectLedge(
               player.position(),
               player.getDeltaMovement(),
               this.supportY + 1.0,
               Vec3.ZERO,
               0.91,
               player.getBoundingBox(),
               false,
               (box, move) -> this.collideMovement(player, box, move)
            )
            .point()
         : this.projectLedge(player, this.supportY + 1.0).point();
      BlockPos cell = this.alignToLane(BlockPos.containing(landing.x, this.supportY, landing.z));
      Direction direction = this.anchorDirection();
      int missing = init_I_BlockPos(root, cell, direction, at -> this.isSettledSolid(MC.level.getBlockState(at), at));
      int remaining = ticksToLanding(player.position(), player.getDeltaMovement(), this.supportY + 1.0);
      int turning = !preparing
         ? 0
         : (
            this.turnPhase == ScaffoldModule.TurnCyclePhase.PLACING
               ? this.smoother().write(this.rotationSmoothness.get())
               : this.smoother().load(this.rotationSmoothness.get())
         );
      boolean carried = false;
      if (!preparing && this.quantizedPlacement != null) {
         Vec3 motion = player.getDeltaMovement().add(this.airControlVector().scale(0.0196));
         Vec3 eye = player.getEyePosition().add(this.collideMovement(player, player.getBoundingBox(), motion));
         BlockHitResult side = raycast(
            eye, this.quantizedPlacement.placement().plan().rotation(), player.blockInteractionRange() - 0.035, MC.level, player
         );
         carried = side != null && side.getBlockPos().equals(support) && side.getDirection() == this.anchorDirection() && this.isHitOnSupportFace(side);
      }

      return init_Z_I(
         remaining + (carried ? 2 : 0), missing, turning, this.trackedSlotCount(player.getInventory().getSelectedSlot()), this.placeRateTracker.init_I(), !preparing
      );
   }

   static boolean read_Z_I(int remaining, int missing, int turning, int blocks) {
      return missing > 0 && init_Z_I(remaining, missing, turning, blocks, 1);
   }

   static int init_I_BlockPos(BlockPos root, BlockPos landing, Direction forward, Predicate<BlockPos> solid) {
      int distance = Math.max(0, (landing.getX() - root.getX()) * forward.getStepX() + (landing.getZ() - root.getZ()) * forward.getStepZ());
      if (distance > 8) {
         return 9;
      }

      int missing = 0;

      for (int step = 1; step <= distance; step++) {
         if (!solid.test(root.relative(forward, step))) {
            missing++;
         }
      }

      return missing;
   }

   static boolean init_Z_I(int remaining, int missing, int turning, int blocks, int tops) {
      return init_Z_I(remaining, missing, turning, blocks, tops, false);
   }

   static boolean init_Z_I(int remaining, int missing, int turning, int blocks, int tops, boolean prepared) {
      int extras = Math.clamp(tops, 0, 2);
      return extras > 0
         && missing >= 0
         && missing <= 6
         && blocks >= missing + extras + 1
         && remaining >= missing * 2 + Math.max(0, turning - 2) + extras * 2 - (prepared ? 1 : 0);
   }

   static boolean init_Z_AABB_6(AABB body, Vec3 velocity, Vec3 forward, BlockPos extra) {
      if (body != null && velocity != null && forward != null && extra != null) {
         AABB obstacle = new AABB(extra).inflate(0.08);
         if (!body.intersects(obstacle) && !(velocity.dot(forward) <= 1.0E-4)) {
            double behind = forward.x > 0.5
               ? body.minX - obstacle.maxX
               : (forward.x < -0.5 ? obstacle.minX - body.maxX : (forward.z > 0.5 ? body.minZ - obstacle.maxZ : obstacle.minZ - body.maxZ));
            if (behind < 0.02) {
               return false;
            }

            AABB projected = body;
            Vec3 motion = velocity;

            for (int tick = 0; tick < 14; tick++) {
               motion = motion.add(forward.scale(0.0196));
               if (projected.expandTowards(motion).intersects(obstacle)) {
                  return false;
               }

               projected = projected.move(motion);
               if (motion.y <= 0.0 && projected.minY <= extra.getY()) {
                  return true;
               }

               motion = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static RotationMath.Rotation init_ce_DataUtil_BlockPos(
      BlockPos support, Direction side, Vec3 eye, Vec3 nextEye, RotationMath.Rotation normal, RotationMath.Rotation from, double gcd, double reach
   ) {
      return init_ce_DataUtil_BlockPos(support, side, eye, nextEye, normal, from, gcd, reach, true);
   }

   static RotationMath.Rotation init_ce_DataUtil_BlockPos(
      BlockPos support,
      Direction side,
      Vec3 eye,
      Vec3 nextEye,
      RotationMath.Rotation normal,
      RotationMath.Rotation from,
      double gcd,
      double reach,
      boolean nextSideRequired
   ) {
      if (support != null
         && side != null
         && !side.getAxis().isVertical()
         && eye != null
         && nextEye != null
         && normal != null
         && from != null
         && Double.isFinite(reach)
         && !(reach <= 0.0)) {
         AABB box = new AABB(support);
         RotationMath.Rotation best = null;
         double cost = 1.0 / 0.0;
         boolean bestCarries = false;

         for (int along = 0; along < 7; along++) {
            for (int across = 0; across < 7; across++) {
               double lane = 0.12 + across * 0.12666666666666668;
               double axis = 0.12 + along * 0.12666666666666668;
               Vec3 point = new Vec3(
                  support.getX() + (side.getAxis() == Axis.X ? axis : lane), box.maxY, support.getZ() + (side.getAxis() == Axis.Z ? axis : lane)
               );
               RotationMath.Rotation rotation = stepRotationQuantized(from, RotationMath.lookAt(point, eye), 180.0F, 180.0F, gcdOrDefault(gcd));
               float courseYaw = side.toYRot();
               double normalSide = RotationMath.angleDelta(normal.yaw(), courseYaw);
               double candidateSide = RotationMath.angleDelta(rotation.yaw(), courseYaw);
               if (!(normalSide * candidateSide <= 0.0)) {
                  double yaw = Math.abs(RotationMath.angleDelta(rotation.yaw(), normal.yaw()));
                  double pitch = Math.abs(rotation.pitch() - normal.pitch());
                  if (!(yaw > 18.0) && !(pitch > 24.0)) {
                     Vec3 look = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).scale(reach);
                     Vec3 topHit = box.clip(eye, eye.add(look)).orElse(null);
                     Vec3 sideHit = box.clip(nextEye, nextEye.add(look)).orElse(null);
                     if (isHitInsetOnFace(topHit, box, Direction.UP)) {
                        boolean carries = nextSideRequired && isHitInsetOnFace(sideHit, box, side);
                        double candidate = Math.hypot(RotationMath.angleDelta(rotation.yaw(), from.yaw()), rotation.pitch() - from.pitch());
                        if (best == null || carries && !bestCarries || carries == bestCarries && candidate < cost) {
                           best = rotation;
                           cost = candidate;
                           bestCarries = carries;
                        }
                     }
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private ScaffoldModule.LivePlacement planRiseFromLanding(LocalPlayer player, ItemStack stack) {
      if (player != null && this.laneSupport != null && this.open_Z(player) && this.isPlaceableBlock(stack)) {
         BlockPos riseCell = this.load_BlockPos_LocalPlayer(player);
         return riseCell == null ? null : this.planRisePlacement(player, stack, riseCell);
      } else {
         return null;
      }
   }

   static boolean read_Z_Z_Z(boolean stopRequested, boolean finishing) {
      return stopRequested || finishing;
   }

   private void poll_V(LocalPlayer player) {
      if (this.ownsInput && player != null && !player.onGround() && this.tellyPhase == ScaffoldModule.TellyPhase.RETURNING && (!this.size_Z || !this.aimedThisTick())) {
         if (read_Z_Z(this.aimedThisTick(), this.f_U_I == AutismSessionState.getInstance().getClientTick(), this.size_Z)) {
            this.aimTick = -2147483648;
         }

         this.close_ce_DataUtil();
      }
   }

   static boolean read_Z_Z(boolean visited, boolean moved, boolean placementPinned) {
      return visited && !moved && !placementPinned;
   }

   private RotationMath.Rotation close_ce_DataUtil() {
      return this.init_ce_DataUtil_F(180.0F);
   }

   private RotationMath.Rotation init_ce_DataUtil_F(float cap) {
      if (this.isEagleMode()) {
         this.data_Z = true;
         this.update_Z = false;
         return this.holdAim();
      }

      if (write_Z_Z_Z(this.size_Z, this.aimedThisTick())) {
         return this.holdAim();
      }

      if (this.turnPhase == ScaffoldModule.TurnCyclePhase.PLACING) {
         this.turnPhase = ScaffoldModule.TurnCyclePhase.RETURNED;
      }

      this.quantizedPlacement = null;
      this.close_Z = false;
      if (this.data_Z && this.isAimAligned()) {
         return this.holdAim();
      }

      if (this.aimedThisTick()) {
         return this.holdAim();
      }

      this.update_Z = true;
      RotationMath.Rotation returned = this.aimTowards(this.anchorRotation(), Math.min(cap, this.rotationSpeedCap()));
      this.data_Z = true;
      return returned;
   }

   static boolean write_Z_Z_Z(boolean heldForPlacement, boolean sameTick) {
      return heldForPlacement && sameTick;
   }

   private RotationMath.Rotation liveAimRotation(LocalPlayer player, ScaffoldModule.LivePlacement placement) {
      ScaffoldModule.LivePlacement live = this.refineLivePlacement(player, placement);
      return live != null ? live.plan().rotation() : this.anchorRotation();
   }

   private boolean isAimAligned() {
      return this.isEagleMode()
         ? this.plannedAim != null && isFacingAgainstCourse(this.anchorYaw, this.plannedAim.yaw())
         : this.plannedAim != null && Math.abs(RotationMath.angleDelta(this.anchorFacingYaw(), this.plannedAim.yaw())) <= 2.0F;
   }

   static int init_I_Z_2(boolean turnPending) {
      return turnPending ? 2 : 1;
   }

   private boolean ensureAimAligned() {
      if (this.isAimAligned()) {
         return true;
      }

      this.close_ce_DataUtil();
      return this.isAimAligned();
   }

   static int init_I_I_Z(int currentTicks, boolean forwardAligned) {
      return !forwardAligned ? 0 : Math.min(0, Math.max(0, currentTicks) + 1);
   }

   static boolean write_Z_I(int alignedTicks) {
      return alignedTicks >= 0;
   }

   private static boolean isKeyDown(KeyMapping mapping) {
      return mapping != null && autismclient.util.KeyMappingControl.of(mapping).autism$isActuallyDown();
   }

   private void frame_V() {
      this.plannedAim = null;
      if (!this.rotationWindingDown) {
         if (this.activeRotation != null && autismclient.util.RotationStreamPlanner.isActive(this.rotationStream)) {
            this.rotationWindingDown = true;
         } else if (this.activeRotation == null) {
            this.rotationHoldTicks = 0;
            autismclient.util.RotationStreamPlanner.reset(this.rotationStream);
         }
      }
   }

   private void block_V() {
      this.read_BlockPos = null;
      this.placeRateTracker.load();
      if (this.rotationSmoother != null) {
         this.rotationSmoother.reset();
      }

      this.cachedSmootherBand = null;
      this.index_F = 0.0F / 0.0F;
      this.f_x_I = -2147483648;
      this.resetImperfection();
      this.turnPhase = ScaffoldModule.TurnCyclePhase.FORWARD;
      this.quantizedPlacement = null;
      this.close_Z = false;
      this.cache_Z = false;
      this.diagonalBridge = false;
      this.queue_Z = false;
      this.frame_V();
      this.resetStrafeState();
      this.clearSteerOffsets();
      this.cancelPendingPlacement();
      this.recoveryInput = null;
      this.tellyPhase = ScaffoldModule.TellyPhase.IDLE;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.RELEASED;
      this.ownsInput = false;
      this.pop_Z = false;
      this.sneakHold = ScaffoldModule.SneakHoldFlags.NONE;
      this.run_Z = false;
      this.spaceWasDown = false;
      this.set_Z = false;
      this.spaceHeld = false;
      this.add_Z = false;
      this.clear_Z = false;
      this.start_Z = false;
      this.close_BlockPos = null;
      this.stop_Z = false;
      this.next_Z = false;
      this.walkOffGraceTicks = 0;
      this.f_G_I = 0;
      this.tellyAirTicks = 0;
      this.buffer_Z = false;
      this.f_I_I = 0;
      this.f_J_I = 0;
      this.f_K_I = 0;
      this.supportY = 0;
      this.launchY = 0.0;
      this.launchProgress = 0.0;
      this.anchorYaw = 0.0F;
      this.aimTick = -2147483648;
      this.lastTickedClientTick = -2147483648;
      this.anchorPitch = 0.0F;
      this.laneOffset = 0.0;
      this.f_M_I = 0;
      this.prev_Z = false;
      this.anchorDeviationStreak = 0;
      this.f_S_I = 0;
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.task_Z = false;
      this.f_aa_I = 0;
      this.f_ab_I = 0;
      this.size_Z = false;
      this.selectedOffset = -1;
      this.data_Z = true;
      this.f_W_I = 0;
      this.laneSupport = null;
      this.open_BlockPos = null;
      this.clearEntryCells();
      this.laneCentrePoint = null;
      this.livePlacement = null;
   }

   private void chunk_V() {
      this.read_BlockPos = null;
      if (this.rotationSmoother != null) {
         this.rotationSmoother.reset();
      }

      this.cachedSmootherBand = null;
      this.index_F = 0.0F / 0.0F;
      this.f_x_I = -2147483648;
      this.resetImperfection();
      this.turnPhase = ScaffoldModule.TurnCyclePhase.FORWARD;
      this.quantizedPlacement = null;
      this.close_Z = false;
      this.cache_Z = false;
      this.diagonalBridge = false;
      this.queue_Z = false;
      this.frame_V();
      this.resetStrafeState();
      this.clearSteerOffsets();
      this.cancelPendingPlacement();
      this.recoveryInput = null;
      this.tellyPhase = ScaffoldModule.TellyPhase.IDLE;
      this.forwardKeyState = ScaffoldModule.ForwardKeyState.RELEASED;
      this.ownsInput = false;
      this.pop_Z = false;
      this.run_Z = false;
      this.tellyAirTicks = 0;
      this.buffer_Z = false;
      this.f_K_I = 0;
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.next_Z = false;
      this.walkOffGraceTicks = 0;
      this.f_M_I = 0;
      this.set_Z = false;
      this.add_Z = false;
      this.anchorDeviationStreak = 0;
      this.f_S_I = 0;
      this.task_Z = false;
      this.f_aa_I = 0;
      this.f_ab_I = 0;
      this.size_Z = false;
      this.selectedOffset = -1;
      this.data_Z = true;
      this.aimTick = -2147483648;
      this.lastTickedClientTick = -2147483648;
      this.f_W_I = 0;
      this.livePlacement = null;
      this.open_BlockPos = null;
      this.stop_Z = false;
      this.clearEntryCells();
   }

   @Override
   public void tick() {
      if (!this.isEnabled()) {
         this.tickRotationWindDown();
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.rotationWindingDown && this.activeRotation != null;
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundMovePlayerPacket movement && movement.hasRotation()) {
         RotationMath.Rotation base = this.lastSentRotation();
         this.sentRotation = new RotationMath.Rotation(movement.getYRot(base.yaw()), movement.getXRot(base.pitch()));
      }

      return false;
   }

   public static void onPacketWritten(Packet<?> packet) {
      if (packet instanceof ServerboundPlayerInputPacket input) {
         writtenSneak = input.input().shift();
      }

      if (packet instanceof ServerboundMovePlayerPacket movement) {
         if (movement.hasPosition()) {
            lastWrittenPosition = new Vec3(movement.getX(0.0), movement.getY(0.0), movement.getZ(0.0));
         }

         Vec3 position = lastWrittenPosition;
         if (position != null) {
            lastPublishedEye = position.add(0.0, writtenSneak ? 1.27 : 1.62, 0.0);
         }
      }

      ScaffoldModule.QueuedPacketContext queued = null;
      if (packet instanceof ServerboundUseItemOnPacket) {
         synchronized (queuedPackets) {
            expungeCollectedPacketKeys();
            queued = queuedPackets.remove(new ScaffoldModule.WeakPacketKey(packet));
         }
      }

      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && (scaffold.frame_Z() || scaffold.isTellyOrEagleMode())) {
         if (scaffold.isTellyOrEagleMode()) {
            scaffold.init_V_Packet_aI_RenderState(packet, queued);
         }

         if (packet instanceof ServerboundMovePlayerPacket movement) {
            writtenInputFlags.set(new ScaffoldModule.InputFlags(movement.isOnGround(), movement.horizontalCollision(), movement.hasPosition()));
         } else if (packet instanceof ServerboundUseItemOnPacket use && queued != null) {
            BlockHitResult hit = use.hitResult();
            writtenClicks.offer(
               new ScaffoldModule.QueuedPlacementClick(
                  use.sequence(),
                  hit.getBlockPos().immutable(),
                  hit.getDirection(),
                  hit.getLocation(),
                  System.nanoTime(),
                  use.hand(),
                  queued,
                  ServerRotationTracker.getSnapshot()
               )
            );
         }
      }
   }

   public static void onDisconnect() {
      lastWrittenPosition = null;
      lastPublishedEye = null;
      writtenSneak = false;
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold) {
         scaffold.failureRecorder.init();
         scaffold.clearSlotCache();
      }

      synchronized (queuedPackets) {
         queuedPackets.clear();

         while (packetKeyQueue.poll() != null) {
         }
      }

      writtenClicks.clear();
      writtenInputFlags.set(null);
   }

   private void init_V_Packet_aI_RenderState(Packet<?> packet, ScaffoldModule.QueuedPacketContext queued) {
      ServerRotationTracker.RotationSnapshot snapshot = ServerRotationTracker.getSnapshot();
      RotationMath.Rotation wire = snapshot.initialized() ? new RotationMath.Rotation(ServerRotationTracker.getLastWireYaw(), snapshot.currentPitch()) : null;
      int tick = AutismSessionState.getInstance().getClientTick();
      if (packet instanceof ServerboundUseItemOnPacket use && queued != null) {
         BlockHitResult hit = use.hitResult();
         this.failureRecorder
            .init(
               new ScaffoldFailureRecorder.PlacementFact(
                  "use",
                  tick,
                  use.sequence(),
                  queued.placed(),
                  hit.getBlockPos().immutable(),
                  hit.getDirection(),
                  lastWrittenPosition,
                  hit.getLocation(),
                  queued.rotation(),
                  wire,
                  null,
                  "generation=" + queued.generation() + ",ordinal=" + queued.ordinal() + ",hand=" + use.hand()
               )
            );
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         this.failureRecorder
            .init(
               new ScaffoldFailureRecorder.PlacementFact(
                  "move",
                  tick,
                  -1,
                  null,
                  null,
                  null,
                  lastWrittenPosition,
                  null,
                  null,
                  wire,
                  null,
                  "ground=" + move.isOnGround() + ",collision=" + move.horizontalCollision()
               )
            );
      } else if (packet instanceof ServerboundPlayerInputPacket input) {
         this.failureRecorder.init(new ScaffoldFailureRecorder.PlacementFact("input", tick, -1, null, null, null, lastWrittenPosition, null, null, wire, input.input(), null));
      } else if (packet instanceof ServerboundPlayerCommandPacket command) {
         this.failureRecorder.init(new ScaffoldFailureRecorder.PlacementFact("command", tick, -1, null, null, null, lastWrittenPosition, null, null, wire, null, command.getAction().name()));
      }
   }

   public static void onBlockUpdate(BlockPos cell, BlockState state) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.isTellyOrEagleMode() && scaffold.failureRecorder.init(cell)) {
         BlockPos immutable = cell.immutable();
         scaffold.failureRecorder
            .init(new ScaffoldFailureRecorder.PlacementFact("server-block", AutismSessionState.getInstance().getClientTick(), -1, immutable, null, null, null, null, null, null, null, state.toString()));
         if (state.isAir()) {
            scaffold.captureFailure("server-sent-air-for-recent-use", immutable, -1);
         }
      }
   }

   private void captureFailure(String reason, BlockPos cell, int sequence) {
      if (MC != null && MC.level != null && MC.player != null && this.capturingFailure.compareAndSet(false, true)) {
         ScaffoldFailureRecorder.FailureCapture capture = this.failureRecorder.init(reason, cell, sequence, AutismSessionState.getInstance().getClientTick(), System.nanoTime());
         if (capture == null) {
            this.capturingFailure.set(false);
         } else {
            Path directory = MC.gameDirectory.toPath().resolve("logs");
            autismclient.util.BackgroundTaskExecutor.submit("Telly-placement-diagnostics", () -> {
               try {
                  Files.createDirectories(directory);
                  String json = capture.toJson();
                  if (capture.init_Z()) {
                     Files.writeString(directory.resolve("telly-rejection-first.json"), json);
                  }

                  Files.writeString(directory.resolve("telly-rejection-latest.json"), json);
               } catch (IOException | RuntimeException error) {
                  AutismClientAddon.LOG.debug("Could not save Telly placement diagnostics", error);
               } finally {
                  this.capturingFailure.set(false);
               }
            });
         }
      }
   }

   private void drainWrittenClicks() {
      ScaffoldModule.QueuedPlacementClick written;
      while ((written = writtenClicks.poll()) != null) {
         this.handleWrittenClick(written);
      }
   }

   private void drainInputFlags() {
      ScaffoldModule.InputFlags written = writtenInputFlags.getAndSet(null);
      if (written != null) {
         this.inputFlagsSeen = true;
         this.wireOnGround = written.init();
         this.wireHorizontalCollision = written.load();
         this.wireHasPosition = written.read();
      }
   }

   private void handleWrittenClick(ScaffoldModule.QueuedPlacementClick written) {
      ScaffoldModule.QueuedPacketContext queued = written == null ? null : written.context();
      if (queued != null) {
         ScaffoldModule.WireClickRotation writtenWire = wireClickRotation(written.rotationSnapshot());
         RotationMath.Rotation actualClick = writtenWire == null ? null : writtenWire.load();
         if (this.flags_Z()) {
            this.writtenClickNanos = written.writtenNanos();
            this.recordPlacement(queued.placed(), queued.load(), written.face(), actualClick == null ? 0.0F / 0.0F : actualClick.pitch());
         }

         ScaffoldModule.PlannedPlacement pending = this.pendingPlacement;
         boolean matches = clickMatchesPending(pending, written, this.buildsPlannedCell);
         if (isClickOfGeneration(written, this.activeGeneration)) {
            this.f_aL_I++;
            this.pendingSequence = Math.max(this.pendingSequence, written.sequence());
            if (this.isTellyOrEagleMode() && this.useSlot >= 0 && this.useSlot < 9) {
               this.slotUseSequence[this.useSlot] = written.sequence();
            }

            if (pending == null
               || !matches
               || written.hand() != queued.hand()
               || written.face() != queued.face()) {
               this.init_V_Str_3("packet-mismatch");
            } else if (isUntrustedWrite(queued, this.duplicateSubmitted, this.placementState, this.f_aL_I)) {
               this.init_V_Str_3("duplicate-write");
            } else if (!this.level_Z() && !wireMatchesQueued(queued, written.rotationSnapshot())) {
               this.init_V_Str_3("wire-rotation");
            } else {
               this.placementState = ScaffoldModule.PlacementState.SENT;
               this.pendingStatus = "sent";
               this.clickWritten = true;
               if (actualClick != null) {
                  this.lastPlaceYaw = actualClick.yaw();
               }

               this.drain_V();
            }
         } else if (queued.ordinal() > 0 || this.predictedCells.containsKey(queued.placed())) {
            this.trackHighestSequence(queued.placed(), written.sequence());
         }
      }
   }

   static boolean clickMatchesPending(ScaffoldModule.PlannedPlacement pending, ScaffoldModule.QueuedPlacementClick written) {
      return clickMatchesPending(pending, written, false);
   }

   static boolean clickMatchesPending(ScaffoldModule.PlannedPlacement pending, ScaffoldModule.QueuedPlacementClick written, boolean buildsPlannedCell) {
      if (pending != null && written != null) {
         if (!written.cell().equals(pending.support())) {
            return false;
         } else {
            return buildsPlannedCell ? true : written.face() == pending.face() && written.hitLocation().y >= pending.minHitY();
         }
      } else {
         return false;
      }
   }

   static boolean isClickOfGeneration(ScaffoldModule.QueuedPlacementClick written, long generation) {
      return written != null && written.context() != null && written.context().generation() == generation;
   }

   static boolean isSingleArmedWrite(ScaffoldModule.PlacementState state, int writeCount) {
      return state == ScaffoldModule.PlacementState.ARMED && writeCount == 1;
   }

   static boolean isUntrustedWrite(ScaffoldModule.QueuedPacketContext queued, boolean duplicateSubmitted, ScaffoldModule.PlacementState state, int writeCount) {
      return queued == null || queued.ordinal() != 0 || duplicateSubmitted || !isSingleArmedWrite(state, writeCount);
   }

   static boolean wireMatchesQueued(ScaffoldModule.QueuedPacketContext queued, ServerRotationTracker.RotationSnapshot snapshot) {
      if (queued != null && queued.rotation() != null && snapshot != null) {
         ScaffoldModule.WireClickRotation wire = wireClickRotation(snapshot);
         return wire != null && sameRotation(queued.rotation(), wire.load());
      } else {
         return false;
      }
   }

   public static void onUseItemOnResult(InteractionHand hand, BlockHitResult hit, InteractionResult result) {
      long generation = autismclient.util.VirtualInputDispatcher.init_J();
      if (generation > 0L && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.frame_Z()) {
         scaffold.handleUseResult(generation, hand, hit, result);
      }
   }

   private void handleUseResult(long generation, InteractionHand hand, BlockHitResult hit, InteractionResult result) {
      ScaffoldModule.PlannedPlacement pending = this.pendingPlacement;
      if (this.placementState != ScaffoldModule.PlacementState.IDLE
         && isMatchingResult(generation, this.activeGeneration, hand, this.pendingHand, this.resultSeen, hit, pending, this.buildsPlannedCell)) {
         this.resultSeen = true;
         this.resultConsumed = result != null && result.consumesAction();
         if (this.isTellyOrEagleMode() && this.resultConsumed && this.useSlot >= 0 && !MC.player.hasInfiniteMaterials()) {
            this.slotRemaining[this.useSlot] = Math.max(0, this.slotRemaining[this.useSlot] - 1);
            this.slotUseTick[this.useSlot] = AutismSessionState.getInstance().getClientTick();
         }

         this.pendingStatus = result == null ? "null" : result.getClass().getSimpleName().toLowerCase(Locale.ROOT);
         this.drainWrittenClicks();
         this.drain_V();
      }
   }

   static boolean isMatchingResult(
      long generation,
      long activeGeneration,
      InteractionHand hand,
      InteractionHand expectedHand,
      boolean resultSeen,
      BlockHitResult hit,
      ScaffoldModule.PlannedPlacement pending
   ) {
      return isMatchingResult(generation, activeGeneration, hand, expectedHand, resultSeen, hit, pending, false);
   }

   static boolean isMatchingResult(
      long generation,
      long activeGeneration,
      InteractionHand hand,
      InteractionHand expectedHand,
      boolean resultSeen,
      BlockHitResult hit,
      ScaffoldModule.PlannedPlacement pending,
      boolean buildsPlannedCell
   ) {
      return !resultSeen
         && generation > 0L
         && generation == activeGeneration
         && hand != null
         && hand == expectedHand
         && pending != null
         && hit != null
         && hitMatchesPending(hit, pending, buildsPlannedCell);
   }

   private void drain_V() {
      ScaffoldModule.PlannedPlacement pending = this.pendingPlacement;
      if (pending != null && this.resultSeen && this.placementState == ScaffoldModule.PlacementState.SENT && MC.level != null) {
         BlockPos cell = pending.load();
         int age = Math.max(0, AutismSessionState.getInstance().getClientTick() - this.pendingTick);
         ScaffoldModule.StateDecision decision = decidePlacementState(
            this.placementState,
            age,
            true,
            this.resultConsumed,
            this.level_Z()
               ? this.isSettledSolid(MC.level.getBlockState(cell), cell)
               : this.hasCentreSupport(MC.level.getBlockState(cell), cell),
            isAcknowledged(this.pendingSequence, this.ackedSequence)
         );
         if (decision.state() == ScaffoldModule.PlacementState.FAILED) {
            this.init_V_Str_3("use".equals(decision.reason()) ? "use-" + this.pendingStatus : decision.reason());
         } else {
            this.placementState = decision.state();
         }
      }
   }

   public static void onBlockChangedAck(int sequence) {
      if (MC != null && MC.level != null && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold) {
         scaffold.syncSlotCacheLevel();
         scaffold.blockAckSequence = Math.max(scaffold.blockAckSequence, sequence);
         if (scaffold.isEnabled() && (scaffold.frame_Z() || scaffold.isTellyOrEagleMode())) {
            scaffold.resetOnLevelChange();
            if (sequence > scaffold.ackedSequence) {
               scaffold.ackedSequence = sequence;
            }

            scaffold.reconcileAcknowledgedPredictions();
            scaffold.pruneAcknowledgedSequences(sequence);
            ScaffoldModule.PlannedPlacement active = scaffold.pendingPlacement;
            if (active != null && isAcknowledged(scaffold.pendingSequence, sequence)) {
               boolean solid = scaffold.level_Z()
                  ? scaffold.isSettledSolid(MC.level.getBlockState(active.load()), active.load())
                  : scaffold.open(active.load());
               if (scaffold.placementState == ScaffoldModule.PlacementState.FAILED) {
                  return;
               }

               int age = Math.max(0, AutismSessionState.getInstance().getClientTick() - scaffold.pendingTick);
               ScaffoldModule.StateDecision decision = decidePlacementState(
                  scaffold.placementState, age, scaffold.resultSeen, scaffold.resultConsumed, solid, true
               );
               if (decision.state() == ScaffoldModule.PlacementState.FAILED) {
                  scaffold.init_V_Str_3(decision.reason());
               } else {
                  scaffold.placementState = decision.state();
               }
            }
         }
      }
   }

   private boolean canOperate() {
      return !AutismLiteVariant.enabled() && ClutchModule.read_Z_20()
         ? false
         : MC != null
            && MC.player != null
            && MC.level != null
            && MC.gameMode != null
            && MC.getConnection() != null
            && MC.gui.screen() == null
            && MC.gui.overlay() == null
            && !MC.player.isSpectator()
            && !MC.player.isHandsBusy()
            && !aD.init_Z()
            && !FreecamController.isActive()
            && !RemoteViewController.isActive()
            && !f.load()
            && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
            && !MacroExecutor.isRunning()
            && !AutoTotemModule.init_Z_6()
            && !AutoArmorModule.init_Z_20();
   }

   public static boolean read_Z_12() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.pool_Z() && scaffold.ownsInput;
   }

   public static boolean write_Z_5() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()
         && scaffold.pool_Z()
         && (
            scaffold.ownsInput
               || MC != null
                  && MC.options != null
                  && (
                     isKeyDown(MC.options.keyUp)
                        || isKeyDown(MC.options.keyDown)
                        || isKeyDown(MC.options.keyLeft)
                        || isKeyDown(MC.options.keyRight)
                  )
         );
   }

   public static boolean update_Z_5() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.isRageOrFastMode();
   }

   public static boolean sync_Z_6() {
      return inputOverrideTick == AutismSessionState.getInstance().getClientTick();
   }

   public static Input overrideInput(ClientInput source, Input original) {
      if (!AutismLiteVariant.enabled() && ClutchModule.read_Z_20()) {
         return original;
      }

      if (AutismLiteVariant.enabled()) {
         return load_Input_ClientInput_Input(source, original);
      }

      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && (DangerModule.read_Z_22() || ClutchModule.read_Z_20())
         && MC != null
         && MC.player != null
         && MC.player.input == source
         && (scaffold.isEnabled() && scaffold.canOperate() && scaffold.chunk_Z() || scaffold.rotationWindingDown)) {
         RotationMath.Rotation saved = scaffold.activeRotation;
         int resets = scaffold.rotationHoldTicks;

         try {
            scaffold.activeRotation = autismclient.util.SilentRotationManager.getCurrentRotation();
            scaffold.rotationHoldTicks = Math.max(1, resets);
            Input result = load_Input_ClientInput_Input(source, original);
            inputOverrideTick = AutismSessionState.getInstance().getClientTick();
            return result;
         } finally {
            scaffold.activeRotation = saved;
            scaffold.rotationHoldTicks = resets;
         }
      } else {
         return load_Input_ClientInput_Input(source, original);
      }
   }

   private static Input load_Input_ClientInput_Input(ClientInput source, Input original) {
      Input result = read_Input_ClientInput_Input(source, original);
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && MC != null && MC.player != null && MC.player.input == source
         ? scaffold.init_Input_ClientInput(source, original, result)
         : result;
   }

   private Input init_Input_ClientInput(ClientInput source, Input original, Input result) {
      LocalPlayer player = MC.player;
      int tick = AutismSessionState.getInstance().getClientTick();
      boolean continuous = this.lastInputTick == tick - 1;
      Vec3 pos = player.position();
      Vec3 vel = player.getDeltaMovement();
      Vec3 pos1 = continuous ? this.recentPositions[0] : null;
      Vec3 vel1 = continuous ? this.recentVelocities[0] : null;
      Vec3 pos2 = continuous ? this.recentPositions[1] : null;
      Vec3 vel2 = continuous ? this.recentVelocities[1] : null;
      boolean ground1 = continuous && this.recentGrounded[0];
      boolean ground2 = continuous && this.recentGrounded[1];
      boolean shiftBefore = !continuous || this.lastShift;
      Input last = source.keyPresses;
      this.recentPositions[1] = this.recentPositions[0];
      this.recentVelocities[1] = this.recentVelocities[0];
      this.recentGrounded[1] = this.recentGrounded[0];
      this.recentPositions[0] = pos;
      this.recentVelocities[0] = vel;
      this.recentGrounded[0] = player.onGround();
      this.lastShift = last.shift();
      this.lastInputTick = tick;
      if (result != null
         && result.shift()
         && !original.shift()
         && !last.shift()
         && !shiftBefore
         && !result.jump()
         && this.flags_Z()
         && !this.ownsInput
         && player.onGround()
         && ground1
         && ground2
         && pos1 != null
         && pos2 != null
         && vel1 != null
         && vel2 != null
         && !player.horizontalCollision
         && result.forward() == last.forward()
         && result.backward() == last.backward()
         && result.left() == last.left()
         && result.right() == last.right()) {
         double ax = pos.x - pos1.x - vel1.x;
         double az = pos.z - pos1.z - vel1.z;
         double bx = pos1.x - pos2.x - vel2.x;
         double bz = pos1.z - pos2.z - vel2.z;
         if (!(Math.abs(ax - bx) > 0.002) && !(Math.abs(az - bz) > 0.002)) {
            double mx = vel.x + ax;
            double mz = vel.z + az;
            double sx = Math.abs(mx) > 1.0E-4 ? Math.signum(mx) : 0.0;
            double sz = Math.abs(mz) > 1.0E-4 ? Math.signum(mz) : 0.0;
            if (sx == 0.0 && sz == 0.0) {
               return result;
            } else if (init_Z_LocalPlayer(player, 0.0, 0.0, mx + sx * 0.002, mz + sz * 0.002)) {
               return result;
            } else {
               double lastStep = Math.hypot(pos.x - pos1.x, pos.z - pos1.z);
               if (lastStep < 0.001) {
                  return result;
               } else {
                  double friction = Math.hypot(vel.x, vel.z) / lastStep;
                  double sneak = Mth.clamp(player.getAttributeValue(Attributes.SNEAKING_SPEED), 0.0, 1.0);
                  double nx = mx * friction;
                  double nz = mz * friction;
                  boolean deadNow = load_Z_LocalPlayer(player, mx, mz, nx + ax * sneak, nz + az * sneak);
                  boolean deadLater = load_Z_LocalPlayer(player, mx, mz, nx + ax, nz + az);
                  if (deadNow && !deadLater) {
                     Input deferred = new Input(result.forward(), result.backward(), result.left(), result.right(), result.jump(), false, result.sprint());
                     this.logInputChange("dead-zone-defer", result, deferred);
                     return deferred;
                  } else {
                     return result;
                  }
               }
            }
         } else {
            return result;
         }
      } else {
         return result;
      }
   }

   private static boolean init_Z_LocalPlayer(LocalPlayer player, double ox, double oz, double dx, double dz) {
      AABB box = player.getBoundingBox().move(ox, 0.0, oz);
      double down = player.maxUpStep();
      return player.level()
         .noCollision(
            player,
            new AABB(box.minX + 1.0E-7 + dx, box.minY - down - 1.0E-7, box.minZ + 1.0E-7 + dz, box.maxX - 1.0E-7 + dx, box.minY, box.maxZ - 1.0E-7 + dz)
         );
   }

   private static boolean load_Z_LocalPlayer(LocalPlayer player, double ox, double oz, double dx, double dz) {
      double attempted = Math.hypot(dx, dz);
      if (attempted < 0.05) {
         return false;
      }

      double sx = Math.signum(dx) * 0.05;
      double sz = Math.signum(dz) * 0.05;

      while (dx != 0.0 && init_Z_LocalPlayer(player, ox, oz, dx, 0.0)) {
         if (Math.abs(dx) <= 0.05) {
            dx = 0.0;
            break;
         }

         dx -= sx;
      }

      while (dz != 0.0 && init_Z_LocalPlayer(player, ox, oz, 0.0, dz)) {
         if (Math.abs(dz) <= 0.05) {
            dz = 0.0;
            break;
         }

         dz -= sz;
      }

      while (dx != 0.0 && dz != 0.0 && init_Z_LocalPlayer(player, ox, oz, dx, dz)) {
         dx = Math.abs(dx) <= 0.05 ? 0.0 : dx - sx;
         dz = Math.abs(dz) <= 0.05 ? 0.0 : dz - sz;
      }

      return Math.hypot(dx, dz) < 0.005;
   }

   private static Input read_Input_ClientInput_Input(ClientInput source, Input original) {
      if (original != null && MC != null && MC.player != null && MC.player.input == source) {
         if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold) {
            if (!isRunning(scaffold.isEnabled(), scaffold.canOperate())) {
               scaffold.clearReservedCells();
               return scaffold.rotationWindingDown ? scaffold.flush(original) : original;
            }

            if (scaffold.pool_Z()) {
               scaffold.clearReservedCells();
               Input authored = scaffold.sync(original);
               Input result = scaffold.ownsInput ? scaffold.poll(authored) : scaffold.flush(authored);
               if (scaffold.ownsInput) {
                  int inputTick = AutismSessionState.getInstance().getClientTick();
                  if (scaffold.isEagleMode() || scaffold.f_Z_I == inputTick || init_Z_Z_Input(scaffold.forwardKeyState == ScaffoldModule.ForwardKeyState.FORWARD, result)) {
                     scaffold.f_aP_I = inputTick;
                  } else if (scaffold.f_aP_I == inputTick) {
                     scaffold.f_aP_I = -2147483648;
                  }
               }

               scaffold.captureTickTraceOnce();
               return result;
            } else {
               if (scaffold.cache_Z) {
                  original = filteredPhysicalInput(original, scaffold.diagonalBridge, !scaffold.isEagleMode() || scaffold.bool("eagle-jump"));
               }

               scaffold.lastOriginalInput = original;
               if (scaffold.flags_Z()) {
                  scaffold.updateCourseOctant();
                  scaffold.trackFootingRow();
                  scaffold.trackJumpStart();
                  scaffold.captureInputOctant(original);
               }

               boolean directionalInput = hasMovementInput(original);
               ScaffoldModule.MotionVectorPair previousLine = scaffold.courseLine;
               ScaffoldModule.MotionVectorPair requestedLine = directionalInput ? scaffold.courseLineFor(original) : null;
               boolean courseChanged = courseChanged(previousLine, requestedLine);
               scaffold.courseLine = requestedLine;
               if (!scaffold.flags_Z()) {
                  scaffold.clearReservedCells();
                  return original;
               }

               if (scaffold.duplicateSubmitted || scaffold.prunePredictedCells()) {
                  scaffold.f_aP_I = AutismSessionState.getInstance().getClientTick();
                  scaffold.open_Str = "reconcile";
                  scaffold.updateSneakLatch(true);
                  Input reconciled = new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), true, false);
                  reconciled = scaffold.steerInput(reconciled, original);
                  scaffold.logInputChange("reconcile", original, reconciled);
                  scaffold.logInputChange("net", original, reconciled);
                  scaffold.reservationLedger = "reconcile dup=" + (scaffold.duplicateSubmitted ? "T" : "F") + " untrusted=" + (scaffold.prunePredictedCells() ? "T" : "F");
                  return reconciled;
               }

               if (!scaffold.isPlaceableBlock(scaffold.placeableStack()) && scaffold.j()) {
                  scaffold.clearReservedCells();
                  scaffold.edgeGateActive = true;
                  scaffold.edgeDanger = false;
                  scaffold.fallDanger = false;
                  scaffold.lateralBrink = false;
                  scaffold.footingOwed = false;
                  scaffold.footingOwedTicks = 0;
                  scaffold.open_Str = "empty";
                  scaffold.updateSneakLatch(true);
                  Input stopped = new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), true, false);
                  stopped = scaffold.steerInput(stopped, original);
                  scaffold.logInputChange("empty-stack", original, stopped);
                  scaffold.logInputChange("net", original, stopped);
                  scaffold.reservationLedger = "empty-stack";
                  return stopped;
               }

               boolean carryArc = scaffold.isJumpingAirborne();
               boolean atEdge = scaffold.j() && !carryArc;
               boolean fallAhead = scaffold.edgeDropWarning() == ScaffoldModule.EdgeDropWarning.IMMINENT && !carryArc;
               boolean clickArmed = scaffold.state_Z();
               boolean clickLands = (atEdge || fallAhead) && clickArmed;
               boolean edgeDanger = atEdge && !clickLands;
               boolean fallDanger = fallAhead && !clickLands;
               boolean lateralBrink = scaffold.store_Z() || scaffold.drain_Z() || scaffold.load_Z_Input_Z(original, courseChanged);
               boolean footingOwedRaw = !clickArmed && scaffold.parse_Z();
               scaffold.footingOwedTicks = footingOwedRaw ? scaffold.footingOwedTicks + 1 : 0;
               boolean footingOwed = footingOwedRaw && scaffold.footingOwedTicks <= 10;
               scaffold.edgeGateActive = edgeDanger || fallDanger || lateralBrink || footingOwed;
               scaffold.edgeDanger = edgeDanger;
               scaffold.fallDanger = fallDanger;
               scaffold.lateralBrink = lateralBrink;
               scaffold.footingOwed = footingOwed;
               scaffold.courseLine = scaffold.retainLine(requestedLine, scaffold.edgeGateActive);
               if (courseChanged) {
                  if (MC.player == null || !MC.player.onGround()) {
                     scaffold.stickyPlacement = null;
                  }

                  scaffold.resetInputCorrection();
               }

               Input adjusted = original;
               BlockPos riseTakeoffCell = scaffold.heldRiseCell();
               scaffold.riseTakeoffCell = riseTakeoffCell;
               boolean physicalClimbIntent = directionalInput && scaffold.wantsJump();
               scaffold.physicalClimbIntent = physicalClimbIntent;
               scaffold.buffer_Z();
               if (scaffold.data_BlockPos != null) {
                  scaffold.next_Str = "floor";
               }

               scaffold.updateReservation(physicalClimbIntent, courseChanged);
               boolean lipStop = init_Z_Z_3(MC.player.onGround(), clickArmed, footingOwedRaw, scaffold.footingOwedTicks, 10);
               boolean wireSneak = scaffold.updateSneakLatch(adjusted.shift() || scaffold.edgeGateActive || lipStop);
               if (!adjusted.shift() && wireSneak) {
                  Input beforeSneak = adjusted;
                  adjusted = new Input(adjusted.forward(), adjusted.backward(), adjusted.left(), adjusted.right(), adjusted.jump(), true, adjusted.sprint());
                  scaffold.logInputChange("wire-sneak", beforeSneak, adjusted);
               }

               if (lipStop && !adjusted.shift()) {
                  Input beforeLip = adjusted;
                  adjusted = new Input(adjusted.forward(), adjusted.backward(), adjusted.left(), adjusted.right(), adjusted.jump(), true, false);
                  scaffold.logInputChange("lip-stop", beforeLip, adjusted);
               }

               scaffold.open_Str = "-";
               if (write(physicalClimbIntent, MC.player.onGround(), scaffold.reservationActive)) {
                  Input beforeSprint = adjusted;
                  adjusted = withoutSprint(adjusted);
                  scaffold.logInputChange("sprint-drop", beforeSprint, adjusted);
                  scaffold.f_aP_I = AutismSessionState.getInstance().getClientTick();
               }

               Input emitted = scaffold.steerInput(adjusted, original);
               scaffold.logInputChange("lane", adjusted, emitted);
               if (MC.player.onGround()) {
                  scaffold.carryStart = MC.player.position();
               }

               boolean arcBrake = scaffold.poll(directionalInput, true);
               boolean climbBrake = scaffold.open(directionalInput, true);
               boolean coast = arcBrake && scaffold.level_Z && !climbBrake;
               scaffold.brakeMode = arcBrake ? (climbBrake ? "arc+dclimb" : (coast ? "coast" : "arc")) : (climbBrake ? "dclimb" : "--");
               boolean geometricBrake = arcBrake;
               arcBrake |= climbBrake;
               if (arcBrake) {
                  Input beforeBrake = emitted;
                  Vec3 brakeLane = scaffold.courseLine == null ? null : scaffold.courseLine.load();
                  emitted = coast
                     ? jumpShiftOnly(emitted)
                     : (
                        geometricBrake && isDriftingOffLane(MC.player.getDeltaMovement(), brakeLane)
                           ? counterMovementInput(
                              emitted, MC.player.getDeltaMovement(), scaffold.activeRotation == null ? MC.player.getYRot() : scaffold.activeRotation.yaw()
                           )
                           : mirroredInput(emitted)
                     );
                  if (!hasMovementInput(emitted)) {
                     scaffold.originalInputTick = AutismSessionState.getInstance().getClientTick();
                  }

                  scaffold.logInputChange("brake", beforeBrake, emitted);
               }

               if (MC.player.onGround() && !emitted.jump() && !emitted.shift()) {
                  float yaw = scaffold.activeRotation == null ? MC.player.getYRot() : scaffold.activeRotation.yaw();
                  BlockPos frictionCell = BlockPos.containing(MC.player.getX(), MC.player.getY() - 0.5, MC.player.getZ());
                  double friction = MC.level.getBlockState(frictionCell).getBlock().getFriction();
                  Vec3 nextStep = predictVelocity(
                     MC.player.getDeltaMovement(), emitted, yaw, MC.player.getAttributeValue(Attributes.MOVEMENT_SPEED), friction, MC.player.isCrouching()
                  );
                  if (init_Z_LocalPlayer(MC.player, 0.0, 0.0, nextStep.x, nextStep.z)) {
                     Input beforeFooting = emitted;
                     boolean hold = scaffold.updateSneakLatch(true);
                     emitted = new Input(emitted.forward(), emitted.backward(), emitted.left(), emitted.right(), emitted.jump(), hold, emitted.sprint());
                     scaffold.logInputChange("step-floor", beforeFooting, emitted);
                  }
               }

               scaffold.logInputChange("net", original, emitted);
               return emitted;
            }
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   static boolean write(boolean physicalIntent, boolean grounded, boolean reservationAirborne) {
      return physicalIntent && (grounded || reservationAirborne);
   }

   static boolean init_Z_Z_Input(boolean forwardCarry, Input emitted) {
      return !forwardCarry || emitted == null || !emitted.forward() || !emitted.sprint();
   }

   static Input withoutSprint(Input input) {
      return input != null && input.sprint()
         ? new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), input.shift(), false)
         : input;
   }

   static Input jumpShiftOnly(Input input) {
      return new Input(false, false, false, false, input.jump(), input.shift(), false);
   }

   static Input counterMovementInput(Input input, Vec3 velocity, float writtenYaw) {
      if (velocity.horizontalDistance() < 0.03) {
         return jumpShiftOnly(input);
      }

      double counterYaw = Math.toDegrees(Math.atan2(velocity.z, velocity.x)) + 90.0;
      int octant = (int)Math.round(Mth.wrapDegrees(counterYaw - writtenYaw) / 45.0);
      return withoutSprint(inputForOctant(octant, input));
   }

   static boolean isDriftingOffLane(Vec3 velocity, Vec3 lane) {
      double speed = velocity.horizontalDistance();
      if (speed < 0.03) {
         return false;
      } else {
         return lane != null && !(lane.horizontalDistanceSqr() < 1.0E-8)
            ? (velocity.x * lane.x + velocity.z * lane.z) / lane.horizontalDistance() < 0.85 * speed
            : true;
      }
   }

   static Vec3 predictVelocity(Vec3 velocity, Input input, float yaw, double movementSpeed, double blockFriction, boolean crouched) {
      Vec3 local = new Vec3((input.left() ? 1 : 0) - (input.right() ? 1 : 0), 0.0, (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0));
      if (local.horizontalDistanceSqr() < 1.0E-8) {
         return velocity.multiply(1.0, 0.0, 1.0);
      }

      local = local.normalize();
      double magnitude = init_D_Vec3_Z(local, crouched);
      double acceleration = groundAcceleration(movementSpeed, blockFriction) / 0.98 * magnitude;
      Vec3 direction = Vec3.directionFromRotation(0.0F, yaw + inputYawOffset(input));
      return velocity.multiply(1.0, 0.0, 1.0).add(direction.scale(acceleration));
   }

   private boolean state_Z() {
      this.drainWrittenClicks();
      this.drain_V();
      boolean ready = this.pendingTick == AutismSessionState.getInstance().getClientTick()
         && this.placementState == ScaffoldModule.PlacementState.PREDICTED
         && !this.duplicateSubmitted
         && this.pendingPlacement != null
         && this.open(this.pendingPlacement.load());
      this.predictionReady = ready;
      return ready;
   }

   static boolean hitMatchesPending(BlockHitResult hit, ScaffoldModule.PlannedPlacement pending) {
      return hitMatchesPending(hit, pending, false);
   }

   static boolean hitMatchesPending(BlockHitResult hit, ScaffoldModule.PlannedPlacement pending, boolean buildsPlannedCell) {
      if (hit != null && pending != null) {
         if (!hit.getBlockPos().equals(pending.support())) {
            return false;
         } else {
            return buildsPlannedCell ? true : hit.getDirection() == pending.face() && hit.getLocation().y >= pending.minHitY();
         }
      } else {
         return false;
      }
   }

   static boolean isCellAhead(Vec3 from, Vec3 lane, BlockPos cell) {
      if (lane == null) {
         return false;
      }

      double dx = cell.getX() + 0.5 - from.x;
      double dz = cell.getZ() + 0.5 - from.z;
      return dx * lane.x + dz * lane.z > 0.0;
   }

   private int footingRow() {
      if (MC.player == null) {
         return -2147483648;
      } else {
         return !MC.player.onGround() && this.groundedFootingRow != -2147483648 ? this.groundedFootingRow : this.measureFootingRow();
      }
   }

   private int measureFootingRow() {
      double feetY = MC.player.getY();
      return findFootingRow(feetY, row -> MC.level != null && this.rowReachesFeet(row, feetY));
   }

   static int findFootingRow(double feetY, IntPredicate reachesFeet) {
      int row = rowBelowFeet(feetY);

      for (int drop = 0; drop <= 2; drop++) {
         if (reachesFeet.test(row - drop)) {
            return row - drop;
         }
      }

      return row;
   }

   static int rowBelowFeet(double feetY) {
      return Mth.floor(feetY - 0.001);
   }

   private boolean rowReachesFeet(int row, double feetY) {
      AABB box = MC.player.getBoundingBox();

      for (int x = Mth.floor(box.minX + 0.001); x <= Mth.floor(box.maxX - 0.001); x++) {
         for (int z = Mth.floor(box.minZ + 0.001); z <= Mth.floor(box.maxZ - 0.001); z++) {
            BlockPos cell = new BlockPos(x, row, z);
            VoxelShape shape = MC.level.getBlockState(cell).getCollisionShape(MC.level, cell);
            if (!shape.isEmpty() && row + shape.max(Axis.Y) >= feetY - 0.001) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean wantsJump() {
      return MC.player != null
         && (!this.isEagleMode() || this.bool("eagle-jump"))
         && isJumpRequested(this.cache_Z, this.diagonalBridge, isKeyDown(MC.options.keyJump), MC.player.input == null ? null : MC.player.input.keyPresses);
   }

   static boolean isJumpRequested(boolean walkingFallback, boolean diagonal, boolean physicalJump, Input sampled) {
      return (!walkingFallback || diagonal) && (physicalJump || sampled != null && sampled.jump());
   }

   private void updateReservation(boolean physicalClimbIntent, boolean courseChanged) {
      if (courseChanged) {
         this.clearReservedCells();
      }

      if (MC.player != null && MC.level != null) {
         if (MC.player.onGround() || !this.reservationActive && this.reservedSupport == null && !physicalClimbIntent) {
            if (physicalClimbIntent && this.courseLine != null) {
               if (this.reservationActive) {
                  this.clearReservedCells();
               }

               int footingRow = this.footingRow();
               BlockPos landingSupport = this.read_BlockPos_I(footingRow);
               boolean landingKnown = landingSupport != null && !MC.level.isOutsideBuildHeight(landingSupport);
               BlockPos beforeGrounded = this.reservedSupport;
               this.reservedSupport = chooseReservedSupport(this.reservedSupport, landingKnown ? landingSupport.immutable() : null, true, true, false);
               if (beforeGrounded == null || !beforeGrounded.equals(this.reservedSupport)) {
                  this.reservedRiser = null;
                  this.reservedStep = null;
               }

               this.reservedConnector = null;
               int deficit = landingKnown ? this.supportDeficit(landingSupport) : 2;
               if (landingKnown
                  && deficit <= 0
                  && (!this.poll(landingSupport) || this.duplicateSubmitted || !init_Z_aI_SyncManager(this.placementState, this.pendingPlacement, landingSupport))) {
                  deficit = 1;
               }

               this.reservationLedger = "grounded known="
                  + (landingKnown ? "T" : "F")
                  + " deficit="
                  + deficit
                  + " pend="
                  + (this.pendingPlacement != null ? "T" : "F")
                  + " land="
                  + (landingSupport == null ? "--" : landingSupport.toShortString())
                  + " landsolid="
                  + (landingSupport == null ? "--" : (this.hasCentreSupport(MC.level.getBlockState(landingSupport), landingSupport) ? "T" : "F"))
                  + " riser="
                  + (landingKnown && this.d_Z() ? (this.open(landingSupport.above()) ? "T" : "F") : "--");
            } else {
               this.clearReservedCells();
               this.reservationLedger = "not-asked intent=" + (physicalClimbIntent ? "T" : "F") + " line=" + (this.courseLine == null ? "F" : "T");
            }
         } else {
            BlockPos before = this.reservedSupport;
            BlockPos advanced = this.read_BlockPos_I(this.footingRow());
            boolean stillLands = before != null
               && (
                  this.landsOnCellOnLane(MC.player.position(), MC.player.getDeltaMovement(), before)
                     || this.landsOnCellOnLane(MC.player.position(), MC.player.getDeltaMovement(), before.above())
               );
            this.reservedSupport = chooseReservedSupport(
               before,
               advanced == null ? null : advanced.immutable(),
               false,
               true,
               false,
               this.courseLine == null ? null : this.courseLine.load(),
               stillLands
            );
            this.reservationActive = true;
            boolean moved = before != null && !before.equals(this.reservedSupport);
            if (moved) {
               this.reservedRiser = null;
               this.reservedConnector = null;
               this.reservedStep = null;
            }

            this.reservationLedger = "airborne reserved="
               + (this.reservedSupport == null ? "--" : this.reservedSupport.toShortString())
               + (moved ? "<" + before.toShortString() : "");
         }
      } else {
         this.clearReservedCells();
         this.reservationLedger = "not-asked";
      }
   }

   static boolean init_Z_aI_SyncManager(ScaffoldModule.PlacementState state, ScaffoldModule.PlannedPlacement pending, BlockPos support) {
      return pending == null || support == null || !pending.load().equals(support) || state == ScaffoldModule.PlacementState.PREDICTED;
   }

   private void clearReservedCells() {
      this.reservedSupport = null;
      this.reservedConnector = null;
      this.reservedRiser = null;
      this.reservedStep = null;
      this.reservationActive = false;
      this.reservationReason = "--";
   }

   static BlockPos chooseReservedSupport(BlockPos reserved, BlockPos projected, boolean grounded, boolean physicalIntent, boolean courseChanged) {
      return chooseReservedSupport(reserved, projected, grounded, physicalIntent, courseChanged, null, true);
   }

   static BlockPos chooseReservedSupport(
      BlockPos reserved, BlockPos projected, boolean grounded, boolean physicalIntent, boolean courseChanged, Vec3 lane, boolean reservedStillLands
   ) {
      if (!physicalIntent || courseChanged) {
         return null;
      }

      if (grounded) {
         return projected;
      }

      if (reserved == null) {
         return projected;
      }

      if (projected != null && !reservedStillLands && lane != null) {
         if (projected.getY() != reserved.getY()) {
            return reserved;
         }

         double ahead = (projected.getX() - reserved.getX()) * lane.x + (projected.getZ() - reserved.getZ()) * lane.z;
         return ahead > 0.0 ? projected : reserved;
      } else {
         return reserved;
      }
   }

   private BlockPos footingRowCell() {
      if (MC.player == null) {
         return null;
      }

      BlockPos at = BlockPos.containing(MC.player.position());
      return new BlockPos(at.getX(), this.footingRow(), at.getZ());
   }

   static boolean sameColumn(BlockPos a, BlockPos b) {
      return a != null && b != null && a.getX() == b.getX() && a.getZ() == b.getZ();
   }

   private int airborneFloorRow() {
      ScaffoldModule.RowLockBounds lock = this.rowLockBounds();
      return floorRow(this.footingRow(), lock == null ? -2147483648 : lock.load(), this.airborneBuiltRow);
   }

   static int floorRow(int oracleRow, int lockRow, int airborneBuiltRow) {
      int floor = oracleRow;
      if (lockRow != -2147483648) {
         floor = floor == -2147483648 ? lockRow : Math.max(floor, lockRow);
      }

      if (airborneBuiltRow != -2147483648) {
         floor = floor == -2147483648 ? airborneBuiltRow : Math.max(floor, airborneBuiltRow);
      }

      return floor;
   }

   static boolean isBelowFooting(int candidateY, int footingRow, boolean descending) {
      return candidateY < footingRow && !descending;
   }

   static boolean init_Z_I_6(int placedY, int builtFloorRow, boolean fallingBelowFooting, boolean arcLandsOnColumn) {
      return isBelowFooting(placedY, builtFloorRow, fallingBelowFooting) && !arcLandsOnColumn;
   }

   private boolean open(ScaffoldModule.PlannedPlacement target) {
      if (target != null && MC.player != null) {
         BlockPos placed = target.load();
         return init_Z_I_6(placed.getY(), this.airborneFloorRow(), this.h_Z(), fallLandsOnCell(MC.player.position(), MC.player.getDeltaMovement(), placed));
      } else {
         return false;
      }
   }

   static boolean init_Z_Z_3(boolean fallingBelowFooting, int placedY, double feetY, double feetVelY) {
      return fallingBelowFooting && placedY + 1.0 > feetY + Math.min(feetVelY, 0.0) + 0.001;
   }

   private boolean poll(ScaffoldModule.PlannedPlacement target) {
      return target != null && MC.player != null ? init_Z_Z_3(this.h_Z(), target.load().getY(), MC.player.position().y, MC.player.getDeltaMovement().y) : false;
   }

   static boolean isErrorImproving(float error, float lastError) {
      return Float.isNaN(lastError) || error < lastError - 0.5F;
   }

   private BlockPos groundRiseCell() {
      if (MC.player != null && MC.level != null && MC.player.onGround() && this.wantsJump()) {
         if (this.courseLine != null && this.courseLine.load() != null) {
            int footingRow = this.footingRow();
            Vec3 lane = this.courseLine.load();
            Vec3 position = MC.player.position();
            int riseY = footingRow + 1;
            ScaffoldModule.RowLockBounds rowLock = this.rowLockBounds();
            BlockPos last = null;
            BlockPos far = null;

            for (double step = 0.5; step <= 1.7; step += 0.3) {
               BlockPos cell = BlockPos.containing(position.x + lane.x * step, riseY + 0.5, position.z + lane.z * step);
               if (!cell.equals(last)) {
                  last = cell;
                  if (!init_Z_Vec3_6(position, lane, cell, 0.0)) {
                     BlockPos below = cell.below();
                     if ((!this.open(below) || !overlapsColumn(position, below)) && !MC.level.isOutsideBuildHeight(cell)) {
                        BlockState state = MC.level.getBlockState(cell);
                        if (!this.hasCentreSupport(state, cell) && (state.isAir() || state.canBeReplaced())) {
                           boolean belowReady = this.poll(below);
                           if (belowReady && (rowLock == null || rowLock.init(cell, true))) {
                              double[] landing = simulateLanding(position, MC.player.getDeltaMovement(), position.y);
                              if (landing != null && this.open(new BlockPos((int)Math.floor(landing[0]), riseY - 1, (int)Math.floor(landing[1])))) {
                                 this.next_Str = "lands";
                                 return null;
                              }

                              if (far == null) {
                                 far = cell;
                              }

                              if (jumpLandsOnCell(position, MC.player.getDeltaMovement(), cell)) {
                                 this.next_Str = "cell";
                                 return cell;
                              }
                           }
                        }
                     }
                  }
               }
            }

            BlockPos own = this.init_BlockPos_Vec3_2(position, riseY, rowLock);
            if (own != null) {
               this.next_Str = "own";
               return own;
            } else {
               this.next_Str = far != null ? "far" : "none";
               return far;
            }
         } else {
            this.next_Str = "nolane";
            return null;
         }
      } else {
         this.next_Str = "air";
         return null;
      }
   }

   private BlockPos init_BlockPos_Vec3_2(Vec3 position, int riseY, ScaffoldModule.RowLockBounds rowLock) {
      BlockPos cell = this.load_BlockPos_Vec3(position, riseY, rowLock);
      return cell != null && this.open(cell.below()) ? cell : null;
   }

   private BlockPos load_BlockPos_Vec3(Vec3 position, int riseY, ScaffoldModule.RowLockBounds rowLock) {
      if (!this.placedRecently(AutismSessionState.getInstance().getClientTick())) {
         return null;
      }

      BlockPos foot = this.close_BlockPos();
      BlockPos cell = new BlockPos(foot.getX(), riseY, foot.getZ());
      if (MC.level.isOutsideBuildHeight(cell)) {
         return null;
      }

      BlockState state = MC.level.getBlockState(cell);
      if (!this.hasCentreSupport(state, cell) && (state.isAir() || state.canBeReplaced())) {
         if (!overlapsColumn(position, cell.below())) {
            return null;
         } else {
            return rowLock != null && !rowLock.init(cell, true) ? null : cell;
         }
      } else {
         return null;
      }
   }

   private boolean buffer_Z() {
      this.data_BlockPos = null;
      if (MC.player == null || MC.level == null) {
         return false;
      }

      if (!this.wantsJump() && !this.reservationActive) {
         return false;
      }

      BlockPos support = this.read_BlockPos_I(this.footingRow());
      if (support != null && !this.open(support)) {
         if (this.supportDeficit(support) >= 2) {
            return false;
         }

         this.data_BlockPos = support;
         this.f_bQ_I = AutismSessionState.getInstance().getClientTick();
         return true;
      } else {
         return false;
      }
   }

   private int supportDeficit(BlockPos cell) {
      if (MC.level != null && cell != null && !MC.level.isOutsideBuildHeight(cell)) {
         if (this.hasCentreSupport(MC.level.getBlockState(cell), cell)) {
            return 0;
         }

         for (Direction face : Direction.values()) {
            BlockPos neighbour = cell.relative(face);
            if (!MC.level.isOutsideBuildHeight(neighbour) && this.hasCentreSupport(MC.level.getBlockState(neighbour), neighbour)) {
               return 1;
            }
         }

         return 2;
      } else {
         return 0;
      }
   }

   private boolean flush(BlockPos cell) {
      return this.data_BlockPos != null && this.data_BlockPos.equals(cell) && this.f_bQ_I == AutismSessionState.getInstance().getClientTick();
   }

   private BlockPos heldRiseCell() {
      BlockPos now = this.groundRiseCell();
      if (now != null) {
         this.lastRiseCell = now;
         this.riseCellGraceTicks = 4;
         return now;
      } else if (this.lastRiseCell == null) {
         return null;
      } else {
         boolean climbing = MC.player != null && MC.player.onGround() && this.wantsJump();
         if (climbing && this.riseCellGraceTicks > 0 && !this.open(this.lastRiseCell.below())) {
            this.riseCellGraceTicks--;
            this.next_Str = "latch";
            return this.lastRiseCell;
         } else {
            this.lastRiseCell = null;
            this.riseCellGraceTicks = 0;
            return null;
         }
      }
   }

   static boolean init_Z_Z_3(boolean onGround, boolean clickArmed, boolean footingOwed, int owedTicks, int maxTicks) {
      return onGround && !clickArmed && footingOwed && owedTicks > maxTicks;
   }

   static double[] simulateLanding(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded) {
      return simulateLanding(position, velocity, initialVy, surfaceY, grounded, 0);
   }

   static double[] simulateLanding(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded, int sneakedTicks) {
      return simulateLanding(position, velocity, initialVy, surfaceY, grounded, sneakedTicks, null, null, null);
   }

   static double[] simulateLanding(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded, int sneakedTicks, Vec3 inputDirection) {
      return simulateLanding(position, velocity, initialVy, surfaceY, grounded, sneakedTicks, inputDirection, null, null);
   }

   static double init_D_Vec3_Z(Vec3 normalizedDirection, boolean sneaking) {
      if (normalizedDirection == null) {
         return 0.0;
      }

      double maxAxis = Math.max(Math.abs(normalizedDirection.x), Math.abs(normalizedDirection.z));
      if (maxAxis <= 1.0E-9) {
         return 0.0;
      }

      double scaled = 0.98 * (sneaking ? 0.3 : 1.0);
      return Math.min(1.0, scaled / maxAxis);
   }

   private static double[] simulateLanding(
      Vec3 position,
      Vec3 velocity,
      double initialVy,
      double surfaceY,
      boolean grounded,
      int sneakedTicks,
      Vec3 inputDirection,
      AABB initialBox,
      ScaffoldModule.FacePointResolver collisionResolver
   ) {
      if (position != null && velocity != null) {
         double x = position.x;
         double y = position.y;
         double z = position.z;
         double vx = velocity.x;
         double vy = initialVy;
         double vz = velocity.z;
         Vec3 heldDirection = null;
         if (inputDirection != null && inputDirection.horizontalDistanceSqr() > 1.0E-12) {
            double length = Math.sqrt(inputDirection.x * inputDirection.x + inputDirection.z * inputDirection.z);
            heldDirection = new Vec3(inputDirection.x / length, 0.0, inputDirection.z / length);
         }

         AABB box = initialBox;
         boolean first = grounded;
         boolean descending = vy < 0.0;
         if (descending && y < surfaceY) {
            return null;
         }

         for (int tick = 0; tick < 20; tick++) {
            if (vx * vx + vz * vz < 9.0E-6) {
               vx = 0.0;
               vz = 0.0;
            }

            if (Math.abs(vy) < 0.003) {
               vy = 0.0;
            }

            double speed = Math.sqrt(vx * vx + vz * vz);
            double scale = tick < sneakedTicks ? 0.3 : 1.0;
            double movedVx = vx;
            double movedVz = vz;
            double directionX = heldDirection == null ? (speed <= 1.0E-6 ? 0.0 : vx / speed) : heldDirection.x;
            double directionZ = heldDirection == null ? (speed <= 1.0E-6 ? 0.0 : vz / speed) : heldDirection.z;
            double controlMagnitude = heldDirection == null ? scale : init_D_Vec3_Z(heldDirection, tick < sneakedTicks);
            if (first) {
               double acceleration = heldDirection == null ? 0.096 * scale : 0.1 * controlMagnitude;
               movedVx += directionX * acceleration;
               movedVz += directionZ * acceleration;
            } else {
               double acceleration = heldDirection == null ? 0.02 * scale : 0.02 * controlMagnitude;
               movedVx += directionX * acceleration;
               movedVz += directionZ * acceleration;
            }

            double previousX = x;
            double previousY = y;
            double previousZ = z;
            Vec3 intended = new Vec3(movedVx, vy, movedVz);
            Vec3 movement = collisionResolver != null && box != null ? collisionResolver.resolve(box, intended) : intended;
            if (movement == null) {
               movement = intended;
            }

            x += movement.x;
            y += movement.y;
            z += movement.z;
            if (box != null) {
               box = box.move(movement);
            }

            boolean xCollision = !Mth.equal(intended.x, movement.x);
            boolean yCollision = !Mth.equal(intended.y, movement.y);
            boolean zCollision = !Mth.equal(intended.z, movement.z);
            if (xCollision) {
               movedVx = 0.0;
            }

            if (zCollision) {
               movedVz = 0.0;
            }

            if (yCollision) {
               if (intended.y < 0.0) {
                  return new double[]{x, z};
               }

               vy = 0.0;
            }

            if (vy < 0.0) {
               descending = true;
            }

            if (descending && previousY >= surfaceY && y <= surfaceY) {
               double drop = previousY - y;
               double t = drop <= 1.0E-12 ? 1.0 : Mth.clamp((previousY - surfaceY) / drop, 0.0, 1.0);
               return new double[]{previousX + movement.x * t, previousZ + movement.z * t};
            }

            if (first) {
               vx = movedVx * 0.546;
               vz = movedVz * 0.546;
               first = false;
            } else {
               vx = movedVx * 0.91;
               vz = movedVz * 0.91;
            }

            vy = (vy - 0.08) * 0.98;
         }

         return null;
      } else {
         return null;
      }
   }

   private double[] simulateLandingOnLane(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded, int sneakedTicks) {
      Vec3 lane = this.courseLine == null ? null : this.courseLine.load();
      return MC.player != null && MC.level != null
         ? simulateLanding(
            position,
            velocity,
            initialVy,
            surfaceY,
            grounded,
            sneakedTicks,
            lane,
            MC.player.getBoundingBox(),
            (box, movement) -> Entity.collideBoundingBox(
               MC.player, movement, box, MC.level, MC.level.getEntityCollisions(MC.player, box.expandTowards(movement))
            )
         )
         : simulateLanding(position, velocity, initialVy, surfaceY, grounded, sneakedTicks, lane);
   }

   static boolean landsOnCell(Vec3 position, Vec3 velocity, double initialVy, BlockPos cell, boolean grounded) {
      if (cell == null) {
         return false;
      }

      double[] landing = simulateLanding(position, velocity, initialVy, cell.getY() + 1.0, grounded);
      return landing != null && landingOverlapsCell(landing, cell);
   }

   static boolean fallLandsOnCell(Vec3 position, Vec3 velocity, BlockPos cell) {
      return landsOnCell(position, velocity, velocity == null ? 0.0 : velocity.y, cell, false);
   }

   private boolean landsOnCellOnLane(Vec3 position, Vec3 velocity, BlockPos cell) {
      if (cell == null) {
         return false;
      }

      double[] landing = this.simulateLandingOnLane(
         position, velocity, velocity == null ? 0.0 : velocity.y, cell.getY() + 1.0, MC.player != null && MC.player.onGround(), this.sneakTicks()
      );
      return landing != null && landingOverlapsCell(landing, cell);
   }

   private int sneakTicks() {
      int latchedCurrent = MC.player != null && MC.player.isCrouching() ? 1 : 0;
      return Math.max(latchedCurrent, Math.max(0, this.sneakLatchTicks));
   }

   static boolean jumpLandsOnCell(Vec3 position, Vec3 velocity, BlockPos cell) {
      return landsOnCell(position, velocity, 0.42, cell, true);
   }

   static double[] simulateLanding(Vec3 position, Vec3 velocity, double surfaceY) {
      return simulateLanding(position, velocity, 0.42, surfaceY, true);
   }

   static boolean landingOverlapsCell(double[] landing, BlockPos cell) {
      if (landing != null && cell != null) {
         double overlapX = Math.min(landing[0] + 0.29, cell.getX() + 1.0) - Math.max(landing[0] - 0.29, cell.getX());
         double overlapZ = Math.min(landing[1] + 0.29, cell.getZ() + 1.0) - Math.max(landing[1] - 0.29, cell.getZ());
         return overlapX >= 0.15 && overlapZ >= 0.15;
      } else {
         return false;
      }
   }

   private boolean isJumpingAirborne() {
      return MC.player != null && isAirborneJump(MC.player.onGround(), this.wantsJump());
   }

   static boolean isAirborneJump(boolean onGround, boolean jumpHeld) {
      return jumpHeld && !onGround;
   }

   static boolean courseChanged(ScaffoldModule.MotionVectorPair previous, ScaffoldModule.MotionVectorPair requested) {
      return previous != null && requested != null && !isSameHeading(previous.load(), requested.load());
   }

   private Input sync(Input original) {
      if (this.ownsInput && this.forwardKeyState != ScaffoldModule.ForwardKeyState.RELEASED) {
         Input authored = this.controlledInput();
         if (this.f_Z_I == AutismSessionState.getInstance().getClientTick()) {
            authored = new Input(authored.forward(), authored.backward(), authored.left(), authored.right(), authored.jump(), authored.shift(), false);
         }

         return withManualSneak(authored, isKeyDown(MC.options.keyShift));
      } else {
         return original;
      }
   }

   static Input withManualSneak(Input authored, boolean manualSneak) {
      return new Input(
         authored.forward(), authored.backward(), authored.left(), authored.right(), authored.jump(), manualSneak, authored.sprint() && !manualSneak
      );
   }

   private Input controlledInput() {
      if (this.braking && MC.player != null && !MC.player.onGround()) {
         return new Input(false, true, false, false, false, false, false);
      }

      if (this.forwardKeyState == ScaffoldModule.ForwardKeyState.HOLD) {
         if (this.recoveryInput != null) {
            Input recovery = this.recoveryInput;
            this.recoveryInput = null;
            return new Input(recovery.forward(), recovery.backward(), recovery.left(), recovery.right(), this.run_Z, false, false);
         }

         if (MC.player != null && MC.player.onGround()) {
            BlockPos support = this.supportBlockUnder(MC.player);
            if (support == null) {
               support = this.laneSupport;
            }

            Input brake = support == null
               ? null
               : this.stayOnSupportInput(MC.player, support, new Input(false, false, false, false, false, false, false));
            if (brake != null && hasMovementInput(brake)) {
               return new Input(brake.forward(), brake.backward(), brake.left(), brake.right(), this.run_Z, false, false);
            }
         }

         ScaffoldModule.StrafeSide strafe = this.strafeSide;
         return new Input(false, false, strafe == ScaffoldModule.StrafeSide.LEFT, strafe == ScaffoldModule.StrafeSide.RIGHT, this.run_Z, false, false);
      } else {
         return MC.player != null && MC.player.onGround() && isTellyRunning(this.tellyPhase)
            ? forwardSprintInput(this.run_Z, false)
            : forwardStrafeInput(this.strafeSide, this.run_Z, false);
      }
   }

   static Input centringInput(AABB footprint, BlockPos support, float courseYaw) {
      return centringInput(footprint, support, courseYaw, 0.45);
   }

   static Input centringInput(AABB footprint, BlockPos support, float courseYaw, double alongDeadZone) {
      if (overlapsBlock(footprint, support, 1.0E-9) && Float.isFinite(courseYaw)) {
         Vec3 forward = forwardFromYaw(courseYaw);
         Vec3 left = new Vec3(forward.z, 0.0, -forward.x);
         Vec3 inward = Vec3.atCenterOf(support).subtract(footprint.getCenter());
         double along = inward.dot(forward);
         double across = inward.dot(left);
         return new Input(along > alongDeadZone, along < -alongDeadZone, across > 0.45, across < -0.45, false, false, false);
      } else {
         return new Input(false, false, false, false, false, false, false);
      }
   }

   static Input forwardStrafeInput(ScaffoldModule.StrafeSide strafe, boolean jump, boolean sneak) {
      ScaffoldModule.StrafeSide safe = strafe == null ? ScaffoldModule.StrafeSide.NONE : strafe;
      return new Input(true, false, safe == ScaffoldModule.StrafeSide.LEFT, safe == ScaffoldModule.StrafeSide.RIGHT, jump, sneak, true);
   }

   static ScaffoldModule.StrafeSwitchState nextStrafeSwitch(
      int cooldown, ScaffoldModule.StrafeSide lastPulse, int pulsesUsed, double laneError, double lateralVelocity, int ticksUntilLanding
   ) {
      int cooled = Math.max(0, cooldown - 1);
      int used = Math.max(0, pulsesUsed);
      ScaffoldModule.StrafeSide previous = lastPulse == null ? ScaffoldModule.StrafeSide.NONE : lastPulse;
      if (Double.isFinite(laneError) && Double.isFinite(lateralVelocity)) {
         double predictedError = predictedLaneError(laneError, lateralVelocity, ticksUntilLanding);
         double projectedDistance = Math.abs(predictedError);
         if (projectedDistance <= 0.1 && Math.abs(laneError) <= 0.16) {
            previous = ScaffoldModule.StrafeSide.NONE;
         }

         if (!(projectedDistance <= 0.16) && cooldown <= 0) {
            double desired = Mth.clamp(laneError * 0.18, -0.055, 0.055);
            ScaffoldModule.StrafeSide requested = strafeSideFor(lateralVelocity, desired);
            if (requested == ScaffoldModule.StrafeSide.NONE) {
               return new ScaffoldModule.StrafeSwitchState(ScaffoldModule.StrafeSide.NONE, cooled, previous, used);
            }

            boolean reversing = previous != ScaffoldModule.StrafeSide.NONE && requested != previous;
            if (reversing && projectedDistance < 0.48) {
               return new ScaffoldModule.StrafeSwitchState(ScaffoldModule.StrafeSide.NONE, cooled, previous, used);
            }

            int pulseLimit = projectedDistance >= 0.48 ? 3 : 2;
            return used >= pulseLimit
               ? new ScaffoldModule.StrafeSwitchState(ScaffoldModule.StrafeSide.NONE, cooled, previous, used)
               : new ScaffoldModule.StrafeSwitchState(requested, 2, requested, used + 1);
         } else {
            return new ScaffoldModule.StrafeSwitchState(ScaffoldModule.StrafeSide.NONE, cooled, previous, used);
         }
      } else {
         return new ScaffoldModule.StrafeSwitchState(ScaffoldModule.StrafeSide.NONE, cooled, previous, used);
      }
   }

   static ScaffoldModule.StrafeSide strafeSideFor(double lateralVelocity, double desiredVelocity) {
      if (!Double.isFinite(lateralVelocity) || !Double.isFinite(desiredVelocity)) {
         return ScaffoldModule.StrafeSide.NONE;
      } else if (lateralVelocity < desiredVelocity - 0.026) {
         return ScaffoldModule.StrafeSide.LEFT;
      } else {
         return lateralVelocity > desiredVelocity + 0.026 ? ScaffoldModule.StrafeSide.RIGHT : ScaffoldModule.StrafeSide.NONE;
      }
   }

   static double predictedLaneError(double laneError, double lateralVelocity, int ticksUntilLanding) {
      int ticks = Mth.clamp(ticksUntilLanding, 1, 14);
      double travel = 0.0;
      double motion = lateralVelocity;

      for (int tick = 0; tick < ticks; tick++) {
         travel += motion;
         motion *= 0.91;
      }

      return laneError - travel;
   }

   private void updateStrafeCorrection(LocalPlayer player, double catchFeetY) {
      if (player != null && this.laneSupport != null) {
         Vec3 left = this.anchorLeft();
         double laneError = this.laneOffset - lateralOffset(player.position(), this.anchorYaw);
         double lateralVelocity = player.getDeltaMovement().dot(left);
         int ticksUntilLanding = ticksToLanding(player.position(), player.getDeltaMovement(), catchFeetY);
         ScaffoldModule.StrafeSwitchState next = nextStrafeSwitch(this.strafeCooldown, this.lastStrafePulse, this.strafePulsesUsed, laneError, lateralVelocity, ticksUntilLanding);
         this.strafeSide = next.side();
         this.strafeCooldown = next.cooldown();
         this.lastStrafePulse = next.lastPulse();
         this.strafePulsesUsed = next.pulsesUsed();
      }
   }

   private void resetStrafeState() {
      this.strafeSide = ScaffoldModule.StrafeSide.NONE;
      this.lastStrafePulse = ScaffoldModule.StrafeSide.NONE;
      this.strafeCooldown = 0;
      this.strafePulsesUsed = 0;
      this.f_P_I = -1;
      this.f_Q_I = 0;
      this.index_F = 0.0F / 0.0F;
      this.f_x_I = -2147483648;
   }

   static Input forwardSprintInput(boolean jump, boolean sneak) {
      return new Input(true, false, false, false, jump, sneak, true);
   }

   static Input filteredPhysicalInput(Input physical) {
      return filteredPhysicalInput(physical, false);
   }

   static Input filteredPhysicalInput(Input physical, boolean diagonal) {
      return filteredPhysicalInput(physical, diagonal, true);
   }

   static Input filteredPhysicalInput(Input physical, boolean diagonal, boolean jumpAllowed) {
      return diagonal && jumpAllowed
         ? physical
         : new Input(physical.forward(), physical.backward(), physical.left(), physical.right(), false, physical.shift(), diagonal && physical.sprint());
   }

   static boolean isTellyRunning(ScaffoldModule.TellyPhase phase) {
      return phase == ScaffoldModule.TellyPhase.RUNNING;
   }

   private void updateLateralCorrection(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && player.onGround() && this.tellyPhase == ScaffoldModule.TellyPhase.RUNNING) {
         Vec3 left = this.anchorLeft();
         double error = this.laneOffset - lateralOffset(player.position(), this.anchorYaw);
         double lateralVelocity = player.getDeltaMovement().dot(left);
         BlockState supportState = MC.level.getBlockState(support);
         double drag = supportState.getBlock().getFriction() * 0.91;
         double groundAcceleration = Math.max(0.025, player.getSpeed() * 0.98);
         double forwardSpeed = Math.max(0.0, player.getDeltaMovement().dot(this.anchorForward()));
         double runwayRemaining = this.supportRemaining(player, support);
         double launchPoint = edgeMargin(forwardSpeed);
         boolean returnToCourse = runwayRemaining <= init_D_D_2(launchPoint, forwardSpeed, this.block_F);
         ScaffoldModule.LateralCorrection next = lateralCorrection(this.valid_Z, this.block_F, error, lateralVelocity, groundAcceleration, drag, returnToCourse);
         this.valid_Z = next.active();
         this.block_F = next.offset();
      } else {
         this.resetSteering();
      }
   }

   private double supportRemaining(LocalPlayer player, BlockPos support) {
      Direction direction = this.anchorDirection();
      double var10000;
      switch (direction) {
         case EAST:
            var10000 = support.getX() + 1.0 - player.getX();
            break;
         case WEST:
            var10000 = player.getX() - support.getX();
            break;
         case SOUTH:
            var10000 = support.getZ() + 1.0 - player.getZ();
            break;
         case NORTH:
            var10000 = player.getZ() - support.getZ();
            break;
         default:
            var10000 = 1.0 / 0.0;
      }

      double remaining = var10000;

      for (int step = 1; step <= 8; step++) {
         BlockPos ahead = support.relative(direction, step);
         if (!this.isSettledSolid(MC.level.getBlockState(ahead), ahead)) {
            return remaining + step - 1.0;
         }
      }

      return 1.0 / 0.0;
   }

   static ScaffoldModule.LateralCorrection lateralCorrection(
      boolean active, float currentOffset, double laneError, double lateralVelocity, double groundAcceleration, double drag, boolean returnToCourse
   ) {
      boolean nextActive = active;
      double predictedError = laneError - lateralVelocity * 3.0;
      if (Math.abs(laneError) > 0.55) {
         nextActive = false;
      } else if (returnToCourse) {
         nextActive = false;
      } else if (nextActive) {
         if (Math.abs(laneError) <= 0.035 && Math.abs(lateralVelocity) <= 0.012) {
            nextActive = false;
         }
      } else if (Math.abs(laneError) > 0.08 || Math.abs(predictedError) > 0.1) {
         nextActive = true;
      }

      float targetOffset = 0.0F;
      if (nextActive) {
         double safeDrag = Mth.clamp(drag, 0.2, 0.99);
         double safeAcceleration = Math.max(0.025, Math.abs(groundAcceleration));
         double desiredLateralVelocity = Mth.clamp(laneError * 0.18, -0.055, 0.055);
         double requiredLateralAcceleration = desiredLateralVelocity / safeDrag - lateralVelocity;
         double steerProgress = Mth.clamp((Math.abs(laneError) - 0.08) / 0.15999999999999998, 0.0, 1.0);
         double maxSteer = Mth.lerp(steerProgress, 8.0, 15.0);
         double maximumRatio = Math.sin(maxSteer * 0.01745329238474369);
         double steeringRatio = Mth.clamp(requiredLateralAcceleration / safeAcceleration, -maximumRatio, maximumRatio);
         targetOffset = (float)(-Math.toDegrees(Math.asin(steeringRatio)));
      }

      boolean movingOutward = targetOffset != 0.0F
         && (currentOffset == 0.0F || Math.signum(targetOffset) == Math.signum(currentOffset) && Math.abs(targetOffset) > Math.abs(currentOffset));
      float maximumStep = movingOutward ? 5.0F : 7.0F;
      float nextOffset = approach(currentOffset, targetOffset, maximumStep);
      if (!nextActive && Math.abs(nextOffset) < 0.05F) {
         nextOffset = 0.0F;
      }

      return new ScaffoldModule.LateralCorrection(nextActive, nextOffset);
   }

   static double init_D_D_2(double launchPoint, double forwardSpeed, float steeringOffset) {
      int returnTicks = Math.max(1, Mth.ceil(Math.abs(steeringOffset) / 7.0F));
      return launchPoint + Math.max(0.0, forwardSpeed) * (returnTicks + 1) + 0.1;
   }

   private static float approach(float current, float target, float maximumStep) {
      float difference = target - current;
      return Math.abs(difference) <= maximumStep ? target : current + Math.copySign(maximumStep, difference);
   }

   private void resetSteering() {
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.clearSteerOffsets();
   }

   private void clearSteerOffsets() {
      this.steerOffset = 0.0F;
      this.appliedSteerBias = 0.0F;
      this.fill_F = 0.0F / 0.0F;
   }

   public static float velocityYaw(Entity entity, float vanillaYaw) {
      if (entity == null || MC == null || entity != MC.player) {
         return vanillaYaw;
      } else if (f.load()) {
         return vanillaYaw;
      } else if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isSilentRotationActive()) {
         RotationMath.Rotation rotation = scaffold.activeRotation;
         return rotation != null && scaffold.rotationHoldTicks > 0 ? overrideYaw(MC.player, vanillaYaw) : vanillaYaw;
      } else {
         return vanillaYaw;
      }
   }

   public static float overrideYaw(LocalPlayer player, float vanillaYaw) {
      RotationMath.Rotation rotation = load_ce_DataUtil_LocalPlayer(player);
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != appliedRotationTick) {
         appliedRotationTick = tick;
         float anchor = Float.isNaN(lastSentYaw) ? vanillaYaw : lastSentYaw;
         lastSentYaw = rotation == null ? vanillaYaw : unwrapYaw(anchor, rotation.yaw());
      }

      return lastSentYaw;
   }

   static float unwrapYaw(float previousSentYaw, float silentYaw) {
      return previousSentYaw + Mth.wrapDegrees(silentYaw - previousSentYaw);
   }

   public static float overridePitch(LocalPlayer player, float vanillaPitch) {
      RotationMath.Rotation rotation = load_ce_DataUtil_LocalPlayer(player);
      return rotation == null ? vanillaPitch : rotation.pitch();
   }

   public static Vec3 init_Vec3_LivingEntity_Vec3_2(LivingEntity entity, Vec3 vanillaImpulse) {
      RotationMath.Rotation rotation = init_ce_DataUtil_LivingEntity(entity);
      if (rotation == null) {
         return vanillaImpulse;
      }

      float sent = MC != null && entity == MC.player ? overrideYaw(MC.player, rotation.yaw()) : rotation.yaw();
      float yaw = sent * 0.017453292F;
      return new Vec3(-Mth.sin(yaw) * 0.2F, vanillaImpulse.y, Mth.cos(yaw) * 0.2F);
   }

   public static float init_F_LivingEntity_F(LivingEntity entity, float vanillaPitch) {
      RotationMath.Rotation rotation = init_ce_DataUtil_LivingEntity(entity);
      return rotation == null ? vanillaPitch : rotation.pitch();
   }

   public static Vec3 load_Vec3_LivingEntity_Vec3(LivingEntity entity, Vec3 vanillaLook) {
      RotationMath.Rotation rotation = init_ce_DataUtil_LivingEntity(entity);
      return rotation == null ? vanillaLook : Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
   }

   private static RotationMath.Rotation init_ce_DataUtil_LivingEntity(LivingEntity entity) {
      if (entity == null || MC == null || entity != MC.player) {
         return null;
      } else if (f.load()) {
         return null;
      } else if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isSilentRotationActive()) {
         RotationMath.Rotation rotation = scaffold.activeRotation;
         return rotation != null && scaffold.rotationHoldTicks > 0 ? rotation : null;
      } else {
         return null;
      }
   }

   private static RotationMath.Rotation load_ce_DataUtil_LocalPlayer(LocalPlayer player) {
      if (player == null || MC == null || player != MC.player) {
         return null;
      } else if (f.load()) {
         return null;
      } else {
         return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isSilentRotationActive() && scaffold.rotationHoldTicks > 0 ? scaffold.activeRotation : null;
      }
   }

   private boolean isRising(boolean jumping) {
      boolean rising = isJumpingOrAirborne(jumping, MC.player == null || MC.player.onGround()) && this.d_Z();
      this.rising = rising;
      return rising;
   }

   static boolean isJumpingOrAirborne(boolean jumpKeyHeld, boolean onGround) {
      return jumpKeyHeld || !onGround;
   }

   public static boolean flush_Z_5() {
      return MC != null && load_ce_DataUtil_LocalPlayer(MC.player) != null;
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && flush_Z_5();
   }

   public static RotationMath.Rotation activeRotation() {
      return MC == null ? null : load_ce_DataUtil_LocalPlayer(MC.player);
   }

   public static RotationMath.Rotation load_ce_DataUtil_3() {
      RotationMath.Rotation active = activeRotation();
      if (active == null) {
         return null;
      }

      float yaw = Float.isNaN(lastSentYaw) ? active.yaw() : unwrapYaw(lastSentYaw, active.yaw());
      return new RotationMath.Rotation(yaw, active.pitch());
   }

   public static Vec3 init_Vec3_LocalPlayer_Vec3(LocalPlayer player, Vec3 original) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && !scaffold.pool_Z()) {
         RotationMath.Rotation rotation = load_ce_DataUtil_LocalPlayer(player);
         return rotation == null ? original : Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
      } else {
         return original;
      }
   }

   private boolean node_Z() {
      return !this.isRageOrFastMode() && !this.pool_Z();
   }

   private boolean isRageOrFastMode() {
      String mode = this.choice("mode");
      return "Rage".equals(mode) || "Fast".equals(mode);
   }

   private boolean pool_Z() {
      return this.isTellyOrEagleMode() && !this.cache_Z;
   }

   private boolean isTellyOrEagleMode() {
      return "Telly".equals(this.choice("mode")) || this.isEagleMode();
   }

   private boolean isEagleMode() {
      return AutismLiteVariant.enabled() ? false : "Eagle".equals(this.choice("mode"));
   }

   private boolean flags_Z() {
      return this.node_Z();
   }

   private boolean level_Z() {
      return this.pool_Z() && this.ownsInput;
   }

   private boolean frame_Z() {
      return this.flags_Z() || this.level_Z();
   }

   private boolean block_Z() {
      return this.flags_Z() && (this.isTellyOrEagleMode() ? this.diagonalBridge && this.bool("allow-diagonal") : this.bool("remove-limits"));
   }

   static float rotationLimit(boolean unlimited, float bounded) {
      return unlimited ? 180.0F : bounded;
   }

   private boolean chunk_Z() {
      return this.flags_Z() || this.pool_Z() && this.ownsInput;
   }

   private boolean isSilentRotationActive() {
      return !AutismLiteVariant.enabled() && (DangerModule.read_Z_22() || ClutchModule.read_Z_20()) ? false : isSilentActive(this.rotationWindingDown, this.isEnabled(), this.chunk_Z(), this.canOperate());
   }

   public static RotationMath.Rotation read_ce_DataUtil() {
      boolean clutchHandoff = false;
      if (!AutismLiteVariant.enabled()) {
         clutchHandoff = ClutchModule.read_Z_20();
      }

      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && (scaffold.canOperate() || clutchHandoff) && scaffold.rotationHoldTicks > 0
         ? (scaffold.pool_Z() ? scaffold.anchorRotation() : scaffold.activeRotation)
         : null;
   }

   public static void init_V_ce_DataUtil_3(RotationMath.Rotation rotation) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && rotation != null) {
         scaffold.activeRotation = rotation;
         lastSentYaw = rotation.yaw();
         appliedRotationTick = -2147483648;
         scaffold.rotationHoldTicks = 5;
         scaffold.rawRotation = rotation;
         scaffold.previousGoal = null;
         scaffold.rotationIntegrator.reseed(scaffold.random.nextLong());
         scaffold.emittedForward = 0;
         scaffold.fill_F = 0.0F / 0.0F;
         if (scaffold.pool_Z()) {
            scaffold.plannedAim = rotation;
            autismclient.util.RotationStreamPlanner.begin(scaffold.rotationStream, rotation);
         }
      }
   }

   public static void load_V_ce_DataUtil(RotationMath.Rotation rotation) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.pool_Z() && MC.player != null && MC.level != null) {
         BlockPos support = MC.player.onGround() ? scaffold.supportBlockUnder(MC.player) : null;
         scaffold.block_V();
         scaffold.groundedSupport = support;
         scaffold.groundedSupportTick = support == null ? -2147483648 : AutismSessionState.getInstance().getClientTick();
         if (support != null
            && (
               isKeyDown(MC.options.keyUp)
                  || isKeyDown(MC.options.keyDown)
                  || isKeyDown(MC.options.keyLeft)
                  || isKeyDown(MC.options.keyRight)
            )) {
            scaffold.init_V_LocalPlayer_BlockPos(MC.player, support);
         }
      }

      init_V_ce_DataUtil_3(rotation);
   }

   static boolean isSilentActive(boolean windingDown, boolean enabled, boolean silentPath, boolean canRun) {
      return windingDown || enabled && silentPath && canRun;
   }

   static boolean isRunning(boolean enabled, boolean canRun) {
      return enabled && canRun;
   }

   static boolean isSameHeading(Vec3 previous, Vec3 current) {
      if (previous != null && current != null && !(previous.horizontalDistanceSqr() <= 1.0E-12) && !(current.horizontalDistanceSqr() <= 1.0E-12)) {
         Vec3 first = new Vec3(previous.x, 0.0, previous.z).normalize();
         Vec3 second = new Vec3(current.x, 0.0, current.z).normalize();
         return first.dot(second) >= 0.999;
      } else {
         return false;
      }
   }

   private static double signedAngleDegrees(Vec3 from, Vec3 to) {
      double cross = from.x * to.z - from.z * to.x;
      double dot = from.x * to.x + from.z * to.z;
      return Math.toDegrees(Math.atan2(cross, dot));
   }

   private boolean init_Z_Vec3_aI_HashUtil(Vec3 position, ScaffoldModule.MotionVectorPair line) {
      if (this.rowLockBounds() != null) {
         return true;
      }

      int since = AutismSessionState.getInstance().getClientTick() - this.lastPlaceTick;
      if (since >= 0 && since <= 20) {
         return true;
      }

      Vec3 heading = this.travelHeading(line);
      return heading != null && this.hasSupportAhead(position, heading, 1.0);
   }

   private Vec3 travelHeading(ScaffoldModule.MotionVectorPair line) {
      Vec3 velocity = MC.player.getDeltaMovement();
      Vec3 horizontal = new Vec3(velocity.x, 0.0, velocity.z);
      if (horizontal.length() >= 0.05) {
         return horizontal.normalize();
      } else if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 direction = line.load();
         return new Vec3(direction.x, 0.0, direction.z).normalize();
      } else {
         return null;
      }
   }

   private boolean store_Z() {
      if (MC.player == null || MC.level == null || !MC.player.onGround()) {
         return false;
      }

      if (this.hasNoFooting()) {
         return true;
      }

      if (this.willLoseFooting()) {
         return true;
      }

      if (this.a_Z()) {
         return true;
      }

      ScaffoldModule.MotionVectorPair line = this.courseLine;
      if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 position = MC.player.position();
         if (Math.abs(lineLateralError(line, position)) <= 0.45) {
            return false;
         }

         Vec3 probe = init_Vec3_aI_HashUtil(line, position, 0.35);
         return !this.hasSupportWithin(probe.x, position.y, probe.z, 1);
      } else {
         return false;
      }
   }

   private boolean load_Z_Input_Z(Input input, boolean courseChanged) {
      if (this.diagonalBridge && this.isTellyOrEagleMode() && MC.player.onGround() && (courseChanged || this.a_Z()) && hasMovementInput(input)) {
         LocalPlayer player = MC.player;
         BlockPos support = this.supportBlockUnder(player);
         if (support == null) {
            return true;
         }

         double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
         double side = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
         double yaw = Math.toRadians(player.getYRot());
         Vec3 direction = new Vec3(side * Math.cos(yaw) - forward * Math.sin(yaw), 0.0, forward * Math.cos(yaw) + side * Math.sin(yaw)).normalize();
         double acceleration = groundAcceleration(player.getAttributeValue(Attributes.MOVEMENT_SPEED), this.blockFriction(support));
         Vec3 step = player.getDeltaMovement().multiply(1.0, 0.0, 1.0).add(direction.scale(acceleration));
         return !this.hasFloorUnderFootprint(player.getBoundingBox().move(step), player.getY(), 0.12);
      } else {
         return false;
      }
   }

   private boolean drain_Z() {
      if (this.isTellyOrEagleMode() && this.diagonalBridge && MC.player != null && MC.level != null && !MC.player.onGround() && !(MC.player.getDeltaMovement().y >= 0.0)) {
         LocalPlayer player = MC.player;
         Vec3 requested = this.leadMotion();
         AABB body = player.getBoundingBox();
         Vec3 moved = this.collideMovement(player, body, requested);
         if (moved.y <= requested.y + 1.0E-9) {
            return false;
         }

         AABB landed = body.move(moved);
         double feetY = landed.minY;
         if (!this.hasFloorUnderFootprint(landed, feetY, 1.0E-9)) {
            return false;
         }

         Vec3 lane = this.courseLine == null ? Vec3.ZERO : this.courseLine.load();
         if (lane.horizontalDistanceSqr() > 1.0E-8) {
            lane = new Vec3(lane.x, 0.0, lane.z).normalize();
         }

         BlockPos frictionCell = BlockPos.containing(landed.getCenter().x, feetY - 0.5, landed.getCenter().z);
         double friction = MC.level.getBlockState(frictionCell).getBlock().getFriction();
         double acceleration = groundAcceleration(player.getAttributeValue(Attributes.MOVEMENT_SPEED), friction);
         Vec3 next = new Vec3(moved.x * 0.91, 0.0, moved.z * 0.91).add(lane.scale(acceleration));
         return !this.hasFloorUnderFootprint(landed.move(next), feetY, 0.12);
      } else {
         return false;
      }
   }

   private boolean willLoseFooting() {
      if (MC.player != null && MC.level != null && MC.player.onGround()) {
         Vec3 velocity = MC.player.getDeltaMovement();
         if (velocity.horizontalDistanceSqr() <= 1.0E-8) {
            return false;
         }

         Vec3 next = nextHorizontalPosition(MC.player.position(), velocity);
         return isNoOverlap(this.footingOverlap(next));
      } else {
         return false;
      }
   }

   static Vec3 nextHorizontalPosition(Vec3 position, Vec3 velocity) {
      return position != null && velocity != null ? position.add(velocity.x, 0.0, velocity.z) : position;
   }

   static Input mirroredInput(Input input) {
      return input == null ? null : new Input(input.backward(), input.forward(), input.right(), input.left(), input.jump(), input.shift(), false);
   }

   static double meanGapWithPending(long[] gaps, long sinceLastMs, int window) {
      if (gaps != null && gaps.length != 0 && window > 0 && sinceLastMs >= 0L) {
         long[] clamped = new long[gaps.length];

         for (int i = 0; i < gaps.length; i++) {
            clamped[i] = Math.min(1000L, Math.max(0L, gaps[i]));
         }

         return rollingMeanWithCandidate(clamped, Math.min(1000L, sinceLastMs), window);
      } else {
         return 0.0 / 0.0;
      }
   }

   static boolean init_Z_Z(
      boolean grimFamily,
      boolean diagonalLane,
      boolean ascending,
      boolean directionalInput,
      boolean airborne,
      boolean rising,
      double laneSpeed,
      double scoredMeanMs,
      long sinceLastMs
   ) {
      if (!grimFamily || !diagonalLane || !ascending || !directionalInput || !airborne) {
         return false;
      } else if (!rising) {
         return false;
      } else if (laneSpeed < 0.03) {
         return false;
      } else if (sinceLastMs >= 0L && sinceLastMs < 1000L) {
         return Double.isNaN(scoredMeanMs) ? false : scoredMeanMs < 432.0;
      } else {
         return false;
      }
   }

   private boolean open(boolean directionalInput, boolean traced) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.f_ay_I != tick) {
         this.f_ay_I = tick;
         this.f_z_D = 0.0 / 0.0;
         this.count_Z = this.write_Z_Z(directionalInput);
      }

      if (traced) {
         this.f_y_D = this.f_z_D;
      }

      return this.count_Z;
   }

   private boolean write_Z_Z(boolean directionalInput) {
      if (this.flags_Z() && MC.player != null) {
         Vec3 lane = this.courseLine == null ? null : this.courseLine.load();
         if (!isDiagonal(lane)) {
            return false;
         }

         long[] gaps = new long[this.placeGaps.size()];
         int index = 0;

         for (long gap : this.placeGaps) {
            gaps[index++] = gap;
         }

         long sinceLast = this.msSinceLastGap(System.nanoTime());
         double mean = meanGapWithPending(gaps, sinceLast, 8);
         this.f_z_D = mean;
         if (this.block_Z()) {
            return false;
         }

         ScaffoldModule.PlannedPlacement next = this.pendingPlacement != null ? this.pendingPlacement : this.stickyPlacement;
         if (next != null
            && this.activeRotation != null
            && !init_Z_Direction(next.face(), this.activeRotation.pitch(), next.rotation().pitch(), this.lastWirePitch, sinceLast)) {
            return false;
         }

         Vec3 velocity = MC.player.getDeltaMovement();
         double length = Math.sqrt(lane.x * lane.x + lane.z * lane.z);
         double laneSpeed = length < 1.0E-6 ? 0.0 : (velocity.x * lane.x + velocity.z * lane.z) / length;
         return init_Z_Z(true, true, this.d_Z(), directionalInput, !MC.player.onGround(), velocity.y > 0.0, laneSpeed, mean, sinceLast);
      } else {
         return false;
      }
   }

   static boolean init_Z_Direction(Direction face, float writtenPitch, float goalPitch, float previousPitch, long gapMs) {
      return isHorizontalFace(face) && (init_Z_F_2(writtenPitch, previousPitch, gapMs, false) || init_Z_F_2(goalPitch, previousPitch, gapMs, false));
   }

   private double landingOverlap(Vec3 landing, int row) {
      if (landing != null && MC.level != null) {
         double[] point = new double[]{landing.x, landing.z};
         double best = 0.0 / 0.0;
         int minX = Mth.floor(landing.x - 0.29);
         int maxX = Mth.floor(landing.x + 0.29);
         int minZ = Mth.floor(landing.z - 0.29);
         int maxZ = Mth.floor(landing.z + 0.29);

         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos cell = new BlockPos(x, row, z);
               if (!MC.level.isOutsideBuildHeight(cell) && this.close(cell)) {
                  double overlap = landingOverlapArea(point, cell);
                  if (Double.isNaN(best) || overlap > best) {
                     best = overlap;
                  }
               }
            }
         }

         return best;
      } else {
         return 0.0 / 0.0;
      }
   }

   private boolean close(BlockPos cell) {
      BlockState state = MC.level.getBlockState(cell);
      return this.hasCentreSupport(state, cell);
   }

   private boolean poll(boolean directionalInput, boolean traced) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.f_az_I != tick) {
         this.f_az_I = tick;
         this.level_Z = false;
         this.flags_Z = this.push(directionalInput, true);
         if (this.flags_Z && MC.player != null) {
            Vec3 coast = projectLandingPoint(MC.player.position(), MC.player.getDeltaMovement(), this.f_aA_I + 1.0, 24, 0.0);
            this.level_Z = coast != null && this.landingOverlap(coast, this.f_aA_I) >= 0.2;
         }
      }

      return this.flags_Z;
   }

   private boolean push(boolean directionalInput, boolean traced) {
      if (traced) {
         this.carryDistance = 0.0;
         this.carryLabel = "--";
         this.pushStatus = "--";
      }

      if (this.mark_Z()) {
         if (traced) {
            this.pushStatus = "off";
         }

         return false;
      } else if (this.flags_Z() && MC.player != null && !MC.player.onGround()) {
         if (directionalInput && this.d_Z()) {
            BlockPos support = this.reservedSupport;
            if (support == null) {
               if (traced) {
                  this.pushStatus = "nosup";
               }

               return false;
            } else {
               Vec3 velocity = MC.player.getDeltaMovement();
               ScaffoldModule.MotionVectorPair line = this.courseLine;
               Vec3 lane = line == null ? null : line.load();
               double brakeSpeed = velocity.horizontalDistance();
               if (!isDriftingOffLane(velocity, lane) && lane != null && lane.horizontalDistanceSqr() > 1.0E-8) {
                  brakeSpeed = (velocity.x * lane.x + velocity.z * lane.z) / lane.horizontalDistance();
               }

               if (brakeSpeed < 0.03) {
                  if (traced) {
                     this.pushStatus = "speed";
                  }

                  return false;
               } else {
                  boolean flatTellyCatch = isFlatDiagonalTelly(this.choice("mode"), this.diagonalBridge, this.d_Z()) && !this.open(support.above());
                  double catchFeet = support.getY() + (flatTellyCatch ? 1.0 : 2.0);
                  Vec3 landing = projectLandingPoint(MC.player.position(), velocity, catchFeet, 24, 0.02);
                  this.f_aA_I = support.getY() + (flatTellyCatch ? 0 : 1);
                  if (landing == null && !flatTellyCatch) {
                     this.f_aA_I = support.getY();
                     landing = projectLandingPoint(MC.player.position(), velocity, this.f_aA_I + 1.0, 24, 0.02);
                  }

                  if (landing == null) {
                     if (traced) {
                        this.pushStatus = "noland";
                     }

                     return false;
                  } else {
                     Vec3 start = this.carryStart;
                     if (traced) {
                        this.carryDistance = start == null ? 0.0 : Math.hypot(landing.x - start.x, landing.z - start.z);
                     }

                     double carry = this.landingOverlap(landing, this.f_aA_I);
                     if (traced) {
                        this.carryLabel = Double.isNaN(carry) ? "bare" : String.format(Locale.ROOT, "%+.2f", carry);
                     }

                     if (carry >= 0.15) {
                        return false;
                     }

                     if (this.isStarved(velocity.y > 0.0, lane)) {
                        if (traced) {
                           this.pushStatus = "xing";
                        }

                        return false;
                     } else {
                        return true;
                     }
                  }
               }
            }
         } else {
            if (traced) {
               this.pushStatus = "noasc";
            }

            return false;
         }
      } else {
         if (traced) {
            this.pushStatus = "nogrim";
         }

         return false;
      }
   }

   private boolean mark_Z() {
      return false;
   }

   private boolean hasNoFooting() {
      return MC.player != null && MC.level != null && MC.player.onGround() ? isNoOverlap(this.footingOverlap(MC.player.position())) : false;
   }

   static boolean isNoOverlap(double overlap) {
      return overlap <= 0.0;
   }

   private double footingOverlap(Vec3 point) {
      return this.footingOverlapAt(point, Mth.floor(point.y) - 1);
   }

   private double footingOverlapAt(Vec3 point, int y) {
      double half = MC.player.getBbWidth() * 0.5;
      double minX = point.x - half;
      double maxX = point.x + half;
      double minZ = point.z - half;
      double maxZ = point.z + half;
      double best = 0.0;

      for (int cx = Mth.floor(minX); cx <= Mth.floor(maxX); cx++) {
         for (int cz = Mth.floor(minZ); cz <= Mth.floor(maxZ); cz++) {
            BlockPos pos = new BlockPos(cx, y, cz);
            if (!MC.level.isOutsideBuildHeight(pos) && this.hasCentreSupport(MC.level.getBlockState(pos), pos)) {
               double overlapX = Math.min(maxX, cx + 1.0) - Math.max(minX, cx);
               double overlapZ = Math.min(maxZ, cz + 1.0) - Math.max(minZ, cz);
               best = Math.max(best, Math.min(overlapX, overlapZ));
            }
         }
      }

      return best;
   }

   private boolean parse_Z() {
      if (MC.player != null && MC.level != null && MC.player.onGround()) {
         BlockPos footing = this.close_BlockPos().below();
         if (!MC.level.getBlockState(footing).canBeReplaced()) {
            return false;
         }

         Vec3 position = MC.player.position();
         Vec3 lead = this.leadMotion().scale(leadTicks(this.paceWaitTicks));
         return load_Z_D_D(this.footingOverlap(position), this.footingOverlap(position.add(lead)));
      } else {
         return false;
      }
   }

   static boolean load_Z_D_D(double overlap, double leadOverlap) {
      return overlap <= 0.3 && leadOverlap <= overlap;
   }

   static int leadTicks(int paceWaitTicks) {
      return Mth.clamp(1 + paceWaitTicks, 1, 4);
   }

   private boolean bind_Z() {
      int since = AutismSessionState.getInstance().getClientTick() - this.f_aU_I;
      return since >= 0 && since <= 6;
   }

   private boolean a_Z() {
      ScaffoldModule.MotionVectorPair line = this.courseLine;
      if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 velocity = MC.player.getDeltaMovement();
         Vec3 horizontal = new Vec3(velocity.x, 0.0, velocity.z);
         if (horizontal.length() < 0.05) {
            return false;
         } else {
            return Math.abs(signedAngleDegrees(horizontal, line.load())) <= 60.0 ? false : this.init_Z_Vec3_aI_HashUtil(MC.player.position(), line);
         }
      } else {
         return false;
      }
   }

   static double lineLateralError(ScaffoldModule.MotionVectorPair line, Vec3 position) {
      Vec3 direction = line.load().normalize();
      Vec3 left = new Vec3(direction.z, 0.0, -direction.x);
      return nearestPointOnLine(line, position).subtract(position).dot(left);
   }

   static Vec3 init_Vec3_aI_HashUtil(ScaffoldModule.MotionVectorPair line, Vec3 position, double distance) {
      Vec3 direction = line.load().normalize();
      Vec3 left = new Vec3(direction.z, 0.0, -direction.x);
      double error = lineLateralError(line, position);
      return position.add(left.scale(error > 0.0 ? -distance : distance));
   }

   private boolean hasSupportAhead(Vec3 position, Vec3 horizontal, double speed) {
      Vec3 step = horizontal.scale(1.0 / speed);

      for (double distance = 0.5; distance <= 2.5; distance += 0.5) {
         BlockPos feet = BlockPos.containing(position.add(step.scale(distance)));
         boolean supported = this.hasCentreSupport(MC.level.getBlockState(feet), feet)
            || this.hasCentreSupport(MC.level.getBlockState(feet.below()), feet.below())
            || this.hasCentreSupport(MC.level.getBlockState(feet.below(2)), feet.below(2));
         if (!supported) {
            return true;
         }
      }

      return false;
   }

   private Input flush(Input input) {
      boolean tellyWindDown = this.rotationWindingDown && autismclient.util.RotationStreamPlanner.isActive(this.rotationStream);
      if ((this.chunk_Z() || tellyWindDown) && this.activeRotation != null && this.rotationHoldTicks > 0) {
         if (this.rotationWindingDown) {
            return this.close(input);
         }

         if (this.pool_Z()) {
            return this.remapInput(input, MC.player.getYRot(), this.activeRotation.yaw());
         }

         Input result = init_Input_Input_2(input, MC.player.getYRot(), this.activeRotation.yaw());
         if (result != null && !result.forward() && hasMovementInput(input)) {
            this.f_aP_I = AutismSessionState.getInstance().getClientTick();
         }

         return result;
      } else {
         return input;
      }
   }

   static float inputYaw(float forward, float sideways, float referenceYaw) {
      return Mth.wrapDegrees(referenceYaw - (float)Math.toDegrees(Math.atan2(sideways, forward)));
   }

   static boolean isStrafing(Input physical) {
      return physical != null && physical.left() != physical.right();
   }

   static float snapToLaneOctant(float laneYaw, float freeYaw) {
      float off = Mth.wrapDegrees(freeYaw - laneYaw);
      return Mth.wrapDegrees(laneYaw + Math.round(off / 45.0F) * 45.0F);
   }

   static float octantResidual(float laneYaw, float emittedYaw) {
      float off = Mth.wrapDegrees(emittedYaw - laneYaw);
      return Mth.wrapDegrees(off - Math.round(off / 45.0F) * 45.0F);
   }

   static boolean isStrongBias(float bias) {
      return Math.abs(bias) > 37.5F;
   }

   private float sync_F() {
      ScaffoldModule.MotionVectorPair line = this.courseLine;
      if (line != null && MC.player != null) {
         Vec3 direction = line.load();
         if (direction.horizontalDistanceSqr() <= 1.0E-8) {
            this.decayCorrection();
            return 0.0F;
         }

         Vec3 unit = direction.normalize();
         Vec3 left = new Vec3(unit.z, 0.0, -unit.x);
         double offset = lineLateralError(line, MC.player.position());
         int previous = this.correctionSide;
         this.correctionSide = nextCorrectionSide(previous, this.correctionHeldTicks, this.correctionLockTicks, offset, MC.player.getDeltaMovement().dot(left));
         if (this.correctionSide == 0) {
            if (previous != 0) {
               this.correctionHeldTicks = 0;
               this.correctionLockTicks = 3;
            } else {
               this.decayCorrection();
            }

            return 0.0F;
         } else {
            this.correctionHeldTicks++;
            this.correctionLockTicks = 0;
            double demand = Math.max(Math.abs(offset), 0.06) * this.correctionSide;
            return laneSteerOffset(unit, left.scale(demand), Vec3.ZERO, 0.0, 0.23);
         }
      } else {
         this.decayCorrection();
         return 0.0F;
      }
   }

   private void decayCorrection() {
      this.correctionSide = 0;
      this.correctionHeldTicks = 0;
      if (this.correctionLockTicks > 0) {
         this.correctionLockTicks--;
      }
   }

   static int nextCorrectionSide(int side, int heldTicks, int lockTicks, double offset, double perpendicularVelocity) {
      double predicted = offset - perpendicularVelocity * 4.0;
      if (side != 0) {
         if (Math.abs(offset) <= 0.06) {
            return 0;
         }

         if (heldTicks >= 2) {
            return 0;
         }

         double settles = offset - perpendicularVelocity * 2.2;
         return side * settles <= 0.06 ? 0 : side;
      } else if (lockTicks > 0) {
         return 0;
      } else if (Math.abs(predicted) < 0.15) {
         return 0;
      } else if (Math.abs(offset) <= 0.06) {
         return 0;
      } else {
         return offset > 0.0 ? 1 : -1;
      }
   }

   private Input close(Input input) {
      int tick = AutismSessionState.getInstance().getClientTick();
      Input result = this.movementInputFixer.load(input, MC.player.getYRot(), this.activeRotation.yaw(), tick);
      if (result == null) {
         return null;
      }

      this.emittedForward = result.forward() == result.backward() ? 0 : (result.forward() ? 1 : -1);
      this.emittedSideways = result.left() == result.right() ? 0 : (result.left() ? 1 : -1);
      this.f_aS_I = 0;
      this.f_a_Z = false;
      this.fill_F = 0.0F / 0.0F;
      if (!result.forward() && hasMovementInput(input)) {
         this.f_aP_I = tick;
      }

      return result;
   }

   private Input steerInput(Input input, Input physical) {
      if (this.chunk_Z() && this.activeRotation != null && this.rotationHoldTicks > 0) {
         float forward = axisValue(input.forward(), input.backward());
         float sideways = axisValue(input.left(), input.right());
         float emitted = this.activeRotation.yaw();
         this.steerMode = "cam";
         this.appliedSteerBias = 0.0F;
         this.sweeping = false;
         if (isStrafing(physical)) {
            this.steerMode = "steer";
            this.f_aT_I = 0;
            this.decayCorrection();
         } else if (forward == 0.0F && sideways == 0.0F) {
            this.f_aT_I = 0;
            this.decayCorrection();
         } else if (this.laneOctant() != -1) {
            float laneYaw = this.laneYaw();
            float bias = Mth.wrapDegrees(laneYaw - inputYaw(forward, sideways, MC.player.getYRot()));
            if (isStrongBias(bias)) {
               this.appliedSteerBias = Mth.clamp(this.sync_F(), -38.0F, 38.0F);
               this.f_aT_I = 0;
            } else {
               this.appliedSteerBias = bias + Mth.clamp(this.sync_F(), -38.0F, 38.0F);
               this.steerMode = "lane";
               float residual = Math.abs(octantResidual(laneYaw, emitted));
               if (residual > 2.0F) {
                  this.sweeping = ++this.sweepTicks <= 8;
               } else {
                  this.sweepTicks = 0;
               }

               if (residual > 12.0F) {
                  if (++this.f_aT_I <= 6) {
                     Input coast = withoutStrafe(this.open(input));
                     if (coast != null) {
                        this.steerMode = this.sweeping ? "sweep" : "coast";
                        if (!coast.forward()) {
                           this.f_aP_I = AutismSessionState.getInstance().getClientTick();
                        }

                        return coast;
                     }
                  }
               } else {
                  this.f_aT_I = 0;
               }

               if (this.sweeping) {
                  this.steerMode = "sweep";
               }
            }
         }

         if (this.rotationWindingDown && this.appliedSteerBias == 0.0F && !this.sweeping) {
            return this.close(input);
         }

         Input result = this.remapInput(input, MC.player.getYRot(), emitted);
         if (this.sweeping) {
            result = withoutStrafe(result);
         }

         if (result != null && !result.forward() && hasMovementInput(input)) {
            this.f_aP_I = AutismSessionState.getInstance().getClientTick();
         }

         return result;
      } else {
         return input;
      }
   }

   static Input withoutStrafe(Input input) {
      return input != null && input.left() != input.right()
         ? new Input(input.forward(), input.backward(), false, false, input.jump(), input.shift(), keepsSprint(input, input.forward()))
         : input;
   }

   static boolean keepsSprint(Input input, boolean emittedForward) {
      return input != null && input.sprint() && emittedForward;
   }

   private Input open(Input input) {
      return this.emittedForward == 0 && this.emittedSideways == 0
         ? null
         : new Input(this.emittedForward > 0, this.emittedForward < 0, this.emittedSideways > 0, this.emittedSideways < 0, input.jump(), input.shift(), keepsSprint(input, this.emittedForward > 0));
   }

   public static boolean close_Z_4() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.f_aP_I == AutismSessionState.getInstance().getClientTick();
   }

   public static boolean open_Z_3() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()
         && scaffold.pool_Z()
         && scaffold.ownsInput
         && scaffold.forwardKeyState != ScaffoldModule.ForwardKeyState.RELEASED;
   }

   private void updateSteerOffset(float courseYaw) {
      this.steerOffset = stepSteerOffset(this.steerOffset, this.desiredSteerOffset(courseYaw));
      this.appliedSteerBias = this.steerOffset;
   }

   private float desiredSteerOffset(float courseYaw) {
      if (MC.player != null && this.laneSupport != null && !Float.isNaN(this.anchorYaw)) {
         double error = this.laneOffset - lateralOffset(MC.player.position(), this.anchorYaw);
         if (Double.isFinite(error) && !(Math.abs(error) > 0.55)) {
            Vec3 forward = this.anchorForward();
            if (forward.horizontalDistanceSqr() <= 1.0E-8) {
               return 0.0F;
            }

            Vec3 left = this.anchorLeft();
            return laneSteerOffset(forward.normalize(), left.scale(error), MC.player.getDeltaMovement());
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   static float stepSteerOffset(float current, float target) {
      float wanted = Mth.clamp(target, -15.0F, 15.0F);
      float step = Mth.clamp(wanted - current, -3.0F, 3.0F);
      return Mth.clamp(current + step, -15.0F, 15.0F);
   }

   private Input poll(Input input) {
      if (this.chunk_Z() && this.activeRotation != null && this.rotationHoldTicks > 0) {
         boolean easing = this.braking && input.backward() && !input.forward();
         if (MC.player != null
            && !MC.player.onGround()
            && this.laneSupport != null
            && (easing || this.forwardKeyState == ScaffoldModule.ForwardKeyState.FORWARD && input.forward() && !input.backward())) {
            double error = this.laneOffset - lateralOffset(MC.player.position(), this.anchorYaw);
            double velocity = MC.player.getDeltaMovement().dot(this.anchorLeft());
            float controlCourse = easing ? this.anchorYaw + 180.0F : this.anchorYaw;
            if (easing) {
               error = -error;
               velocity = -velocity;
            }

            int tick = AutismSessionState.getInstance().getClientTick();
            if (this.f_x_I != tick || !Float.isFinite(this.index_F) || Math.abs(Mth.wrapDegrees(this.index_F - this.activeRotation.yaw())) > 0.001F) {
               float nextYaw = this.smoother().init(this.activeRotation, this.rotationSmoothness.get(), RotationMath.gcdStep());
               boolean turning = Math.abs(Mth.wrapDegrees(nextYaw - this.activeRotation.yaw())) > 4.0F
                  || Float.isFinite(this.index_F) && Math.abs(Mth.wrapDegrees(this.index_F - this.activeRotation.yaw())) > 4.0F;
               ScaffoldModule.StrafeTickBudget next = strafeTickBudget(
                  controlCourse,
                  this.activeRotation.yaw(),
                  nextYaw,
                  error,
                  velocity,
                  0.0196 * (MC.player.isSprinting() ? 1.3 : 1.0),
                  this.f_P_I,
                  this.f_Q_I,
                  turning
               );
               this.f_P_I = next.init();
               this.f_Q_I = next.load();
               this.index_F = this.activeRotation.yaw();
               this.f_x_I = tick;
            }

            return inputForOctant(this.f_P_I, input);
         } else {
            float courseYaw = this.valid_Z ? Mth.wrapDegrees(this.anchorYaw + this.block_F) : this.anchorYaw;
            if (this.forwardKeyState == ScaffoldModule.ForwardKeyState.HOLD && MC.player != null && MC.player.onGround()) {
               this.clearSteerOffsets();
               this.state_V();
               this.emittedForward = 0;
               return init_Input_Input_2(input, courseYaw, this.activeRotation.yaw());
            } else {
               this.updateSteerOffset(courseYaw);
               return this.remapInput(input, courseYaw, this.activeRotation.yaw());
            }
         }
      } else {
         return input;
      }
   }

   static ScaffoldModule.StrafeTickBudget strafeTickBudget(
      float courseYaw, float emittedYaw, double error, double velocity, double acceleration, int previous, int dwell
   ) {
      return strafeTickBudget(courseYaw, emittedYaw, emittedYaw, error, velocity, acceleration, previous, dwell, false);
   }

   static ScaffoldModule.StrafeTickBudget strafeTickBudget(
      float courseYaw, float emittedYaw, float nextYaw, double error, double velocity, double acceleration, int previous, int dwell, boolean turning
   ) {
      int nearest = Math.floorMod(Math.round(Mth.wrapDegrees(courseYaw - emittedYaw) / 45.0F), 8);
      if (Float.isFinite(courseYaw)
         && Float.isFinite(emittedYaw)
         && Double.isFinite(error)
         && Double.isFinite(velocity)
         && Double.isFinite(acceleration)
         && !(acceleration <= 0.0)) {
         int nextNearest = Math.floorMod(Math.round(Mth.wrapDegrees(courseYaw - nextYaw) / 45.0F), 8);
         double carryResidual = Mth.wrapDegrees(nextYaw + nextNearest * 45.0F - courseYaw) * 0.017453292F;
         double carry = -Math.sin(carryResidual) * acceleration;
         int best = nearest;
         double bestCost = 1.0 / 0.0;
         double priorCost = 1.0 / 0.0;

         for (int octant = 0; octant < 8; octant++) {
            double residual = Mth.wrapDegrees(emittedYaw + octant * 45.0F - courseYaw) * 0.017453292F;
            double forward = Math.cos(residual);
            if (!(forward < 0.7)) {
               double force = -Math.sin(residual) * acceleration;
               double remaining = error;
               double motion = velocity;

               for (int tick = 0; tick < 6; tick++) {
                  motion += tick < (turning ? 1 : 2) ? force : carry;
                  remaining -= motion;
                  motion *= 0.91;
               }

               double correction = Math.max(0.0, Math.abs(remaining) - 0.04);
               double cost = correction * correction + motion * motion * 16.0 + (1.0 - forward) * (turning ? 0.01 : 0.008);
               if (octant == previous) {
                  priorCost = cost;
               }

               if (cost < bestCost) {
                  bestCost = cost;
                  best = octant;
               }
            }
         }

         if (Double.isFinite(priorCost) && (turning ? priorCost <= bestCost + 1.0E-4 : dwell < 2 || priorCost <= bestCost + 4.0E-4)) {
            best = previous;
         }

         return new ScaffoldModule.StrafeTickBudget(best, best == previous ? Math.min(20, dwell + 1) : 1);
      } else {
         return new ScaffoldModule.StrafeTickBudget(nearest, 1);
      }
   }

   static Input inputForOctant(int octant, Input original) {
      int index = Math.floorMod(octant, 8);
      boolean forward = index == 0 || index == 1 || index == 7;
      boolean back = index >= 3 && index <= 5;
      boolean left = index >= 5 && index <= 7;
      boolean right = index >= 1 && index <= 3;
      return new Input(forward, back, left, right, original.jump(), original.shift(), keepsSprint(original, forward));
   }

   public static Input init_Input_Input_2(Input input, float playerYaw, float silentYaw) {
      if (input == null) {
         return null;
      }

      float forward = axisValue(input.forward(), input.backward());
      float sideways = axisValue(input.left(), input.right());
      float deltaYaw = (playerYaw - silentYaw) * 0.017453292F;
      float cosine = Mth.cos(deltaYaw);
      float sine = Mth.sin(deltaYaw);
      float transformedSideways = sideways * cosine - forward * sine;
      float transformedForward = forward * cosine + sideways * sine;
      int roundedSideways = Math.round(transformedSideways);
      int roundedForward = Math.round(transformedForward);
      return new Input(
         roundedForward > 0,
         roundedForward < 0,
         roundedSideways > 0,
         roundedSideways < 0,
         input.jump(),
         input.shift(),
         keepsSprint(input, roundedForward > 0)
      );
   }

   private Input remapInput(Input input, float playerYaw, float silentYaw) {
      if (input == null) {
         return null;
      }

      float forward = axisValue(input.forward(), input.backward());
      float sideways = axisValue(input.left(), input.right());
      if (forward == 0.0F && sideways == 0.0F) {
         this.emittedForward = 0;
         this.emittedSideways = 0;
         this.f_aS_I = 0;
         this.f_a_Z = false;
         this.lastRawForward = 0.0F;
         this.lastRawSideways = 0.0F;
         this.fill_F = 0.0F / 0.0F;
         return input;
      }

      float deltaDegrees = Mth.wrapDegrees(playerYaw + this.appliedSteerBias - silentYaw);
      boolean referenceMoved = Float.isFinite(this.fill_F) && Math.abs(Mth.wrapDegrees(deltaDegrees - this.fill_F)) >= 22.5F;
      this.fill_F = deltaDegrees;
      float deltaYaw = deltaDegrees * 0.017453292F;
      float cosine = Mth.cos(deltaYaw);
      float sine = Mth.sin(deltaYaw);
      float transformedSideways = sideways * cosine - forward * sine;
      float transformedForward = forward * cosine + sideways * sine;
      int steadyForward = hysteresisSign(transformedForward, this.emittedForward);
      int steadySideways = this.init_I_I(Math.round(transformedSideways), forward, sideways, referenceMoved);
      if (steadyForward == 0 && steadySideways == 0) {
         steadyForward = Math.round(transformedForward);
         steadySideways = Math.round(transformedSideways);
      }

      this.emittedForward = steadyForward;
      this.emittedSideways = steadySideways;
      return new Input(
         steadyForward > 0, steadyForward < 0, steadySideways > 0, steadySideways < 0, input.jump(), input.shift(), keepsSprint(input, steadyForward > 0)
      );
   }

   private int init_I_I(int requested, float rawForward, float rawSideways, boolean referenceMoved) {
      boolean playerChangedKeys = rawForward != this.lastRawForward || rawSideways != this.lastRawSideways;
      this.lastRawForward = rawForward;
      this.lastRawSideways = rawSideways;
      if (this.rotationWindingDown) {
         this.f_aS_I = 0;
         this.f_a_Z = false;
         return requested;
      }

      if (requested == this.emittedSideways) {
         this.f_aS_I++;
         if (this.correctionSide != 0 && requested != 0) {
            this.f_a_Z = true;
         }

         return requested;
      } else if (!shouldReleaseCorrection(playerChangedKeys, this.f_a_Z && this.correctionSide == 0, this.f_aS_I, this.appliedSteerBias, referenceMoved)) {
         this.f_aS_I++;
         return this.emittedSideways;
      } else {
         this.f_aS_I = 0;
         this.f_a_Z = this.correctionSide != 0 && requested != 0;
         return requested;
      }
   }

   static boolean shouldReleaseCorrection(boolean playerChangedKeys, boolean correctionEnded, int hold, float bias, boolean referenceMoved) {
      return playerChangedKeys || correctionEnded || referenceMoved || hold >= 4 || Math.abs(bias) >= 36.0F;
   }

   static int hysteresisSign(double component, int previous) {
      double magnitude = Math.abs(component);
      double threshold = previous == 0 ? 0.62 : 0.38;
      if (magnitude < threshold) {
         return 0;
      } else {
         return component < 0.0 ? -1 : 1;
      }
   }

   static double impulseAngle(int forwardImpulse, int sidewaysImpulse) {
      return forwardImpulse == 0 && sidewaysImpulse == 0 ? 0.0 / 0.0 : Math.toDegrees(Math.atan2(-sidewaysImpulse, forwardImpulse));
   }

   static double distanceToBoundary(double deltaDegrees) {
      if (!Double.isFinite(deltaDegrees)) {
         return 0.0 / 0.0;
      }

      double best = 1.7976931348623157E308;

      for (double boundary : INPUT_BOUNDARY_ANGLES) {
         best = Math.min(best, Math.abs(Mth.wrapDegrees(deltaDegrees - boundary)));
      }

      return best;
   }

   private void resetInputCorrection() {
      this.emittedForward = 0;
      this.emittedSideways = 0;
      this.f_aS_I = 0;
      this.f_a_Z = false;
      this.lastRawForward = 0.0F;
      this.lastRawSideways = 0.0F;
      this.appliedSteerBias = 0.0F;
      this.f_aT_I = 0;
      this.sweepTicks = 0;
      this.decayCorrection();
      this.correctionLockTicks = 0;
   }

   static float laneSteerOffset(Vec3 laneDirection, Vec3 laneOffset, Vec3 horizontalVel) {
      return laneSteerOffset(laneDirection, laneOffset, horizontalVel, 0.22, 1.1);
   }

   static float laneSteerOffset(Vec3 laneDirection, Vec3 laneOffset, Vec3 horizontalVel, double deadband, double lookahead) {
      Vec3 left = new Vec3(laneDirection.z, 0.0, -laneDirection.x);
      double error = laneOffset.dot(left) - horizontalVel.dot(left) * 4.0;
      if (Math.abs(error) < deadband) {
         return 0.0F;
      }

      Vec3 desired = laneDirection.scale(lookahead).add(left.scale(error));
      return desired.lengthSqr() <= 1.0E-12 ? 0.0F : (float)Mth.clamp(signedAngleDegrees(laneDirection, desired), -38.0, 38.0);
   }

   private static float axisValue(boolean positive, boolean negative) {
      if (positive == negative) {
         return 0.0F;
      } else {
         return positive ? 1.0F : -1.0F;
      }
   }

   private ScaffoldModule.RowLockBounds rowLockBounds() {
      if (this.b_Z() && MC.player != null && this.courseLine != null) {
         Vec3 direction = this.courseLine.load();
         if (isDiagonal(direction)) {
            return null;
         }

         int stepX = axisSign(direction.x);
         int stepZ = axisSign(direction.z);
         if (stepX == 0 && stepZ == 0) {
            return null;
         }

         int since = AutismSessionState.getInstance().getClientTick() - this.lastPlaceTick;
         if (since >= 0 && since <= 10) {
            BlockPos lastPlaced = this.recentPlaced.peekLast();
            if (lastPlaced == null) {
               return null;
            }

            if (!this.hasCentreSupport(MC.level.getBlockState(lastPlaced), lastPlaced)) {
               return null;
            }

            int rowY = read_I_I_I(lastPlaced.getY(), this.footingRow());
            if (BlockPos.containing(MC.player.position()).getY() - 1 < rowY) {
               return null;
            }

            boolean xAxis = stepX != 0;
            BlockPos playerCell = BlockPos.containing(MC.player.position());
            return new ScaffoldModule.RowLockBounds(
               xAxis,
               xAxis ? lastPlaced.getZ() : lastPlaced.getX(),
               rowY,
               xAxis ? lastPlaced.getX() : lastPlaced.getZ(),
               this.init_I_BlockPos(lastPlaced, xAxis, -1),
               this.init_I_BlockPos(lastPlaced, xAxis, 1),
               xAxis ? playerCell.getZ() : playerCell.getX()
            );
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private boolean b_Z() {
      return !this.isTellyOrEagleMode() || !this.diagonalBridge;
   }

   static int read_I_I_I(int placedRow, int footingRow) {
      return footingRow == placedRow + 1 ? footingRow : placedRow;
   }

   private int init_I_BlockPos(BlockPos lastPlaced, boolean xAxis, int sign) {
      for (int step = 1; step <= 12; step++) {
         BlockPos pos = xAxis ? lastPlaced.offset(sign * step, 0, 0) : lastPlaced.offset(0, 0, sign * step);
         if (MC.level.isOutsideBuildHeight(pos)) {
            return -2147483648;
         }

         if (!this.hasCentreSupport(MC.level.getBlockState(pos), pos)) {
            return xAxis ? pos.getX() : pos.getZ();
         }
      }

      return -2147483648;
   }

   static Vec3 projectLandingPoint(Vec3 position, Vec3 velocity, double feetY, int maxTicks) {
      return projectLandingPoint(position, velocity, feetY, maxTicks, 0.0);
   }

   static Vec3 projectLandingPoint(Vec3 position, Vec3 velocity, double feetY, int maxTicks, double impulse) {
      double x = position.x;
      double y = position.y;
      double z = position.z;
      double vx = velocity.x;
      double vy = velocity.y;
      double vz = velocity.z;

      for (int tick = 0; tick < maxTicks; tick++) {
         double previousY = y;
         x += vx;
         y += vy;
         z += vz;
         if (vy < 0.0 && previousY >= feetY && y < feetY) {
            double t = (previousY - feetY) / (previousY - y);
            return new Vec3(x - vx + vx * t, feetY, z - vz + vz * t);
         }

         double speed = Math.sqrt(vx * vx + vz * vz);
         double push = speed <= 1.0E-6 ? 0.0 : impulse / speed;
         vx = vx * 0.91 + vx * push;
         vy = (vy - 0.08) * 0.98;
         vz = vz * 0.91 + vz * push;
      }

      return null;
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_Vec3_ItemStack(Vec3 predicted, ItemStack stack) {
      if (MC.player != null && MC.level != null) {
         Vec3 velocity = MC.player.getDeltaMovement();
         if (!MC.player.onGround() && velocity.y > 0.0) {
            return null;
         }

         Vec3 lane = this.courseLine == null ? null : this.courseLine.load();
         boolean jumping = this.wantsJump();
         if (!MC.player.onGround()) {
            int row = this.airborneFloorRow();
            Vec3 landing = projectLandingPoint(MC.player.position(), velocity, row + 1.0, 12);
            if (landing != null) {
               List<BlockPos> columns = footprintColumns(new Vec3(landing.x, row + 1.5, landing.z));
               BlockPos centered = columns.get(0);
               if (MC.level.isOutsideBuildHeight(centered)) {
                  return null;
               }

               if (this.hasCentreSupport(MC.level.getBlockState(centered), centered)) {
                  return null;
               }

               for (BlockPos cell : columns) {
                  if (!MC.level.isOutsideBuildHeight(cell)) {
                     BlockState state = MC.level.getBlockState(cell);
                     if (state.canBeReplaced() && !this.hasCentreSupport(state, cell)) {
                        ScaffoldModule.PlannedPlacement target = this.planPlacement(cell, predicted, false, lane, jumping);
                        if (!this.push(target) && target != null) {
                           return target;
                        }
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return this.init_aI_TableIndex_Vec3(predicted, lane, jumping);
         }
      } else {
         return null;
      }
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_Vec3(Vec3 predicted, Vec3 lane, boolean jumping) {
      Vec3 position = MC.player.position();
      List<BlockPos> columns = footprintColumns(position);
      BlockPos centered = columns.get(0);
      if (MC.level.isOutsideBuildHeight(centered)) {
         return null;
      }

      if (this.hasCentreSupport(MC.level.getBlockState(centered), centered)) {
         return null;
      }

      for (BlockPos support : columns) {
         if (!MC.level.isOutsideBuildHeight(support)) {
            BlockState state = MC.level.getBlockState(support);
            if (state.canBeReplaced() && !this.hasCentreSupport(state, support)) {
               ScaffoldModule.PlannedPlacement target = this.planPlacement(support, predicted, false, lane, jumping);
               if (!this.push(target) && target != null) {
                  return target;
               }
            }
         }
      }

      return null;
   }

   private boolean push(ScaffoldModule.PlannedPlacement target) {
      return target != null && MC.player != null && !isLegSighted(target, MC.player.getEyePosition(), this.leadMotion());
   }

   static List<BlockPos> footprintColumns(Vec3 position) {
      BlockPos centered = BlockPos.containing(position).below();
      List<BlockPos> columns = new ArrayList<>(4);
      columns.add(centered);
      double fx = position.x - Math.floor(position.x);
      double fz = position.z - Math.floor(position.z);
      int sx = fx < 0.3 ? -1 : (fx > 0.7 ? 1 : 0);
      int sz = fz < 0.3 ? -1 : (fz > 0.7 ? 1 : 0);
      double overlapX = sx == 0 ? 0.0 : (sx < 0 ? 0.3 - fx : fx - 0.7);
      double overlapZ = sz == 0 ? 0.0 : (sz < 0 ? 0.3 - fz : fz - 0.7);
      BlockPos xSide = sx == 0 ? null : centered.offset(sx, 0, 0);
      BlockPos zSide = sz == 0 ? null : centered.offset(0, 0, sz);
      BlockPos first = overlapX >= overlapZ ? xSide : zSide;
      BlockPos second = overlapX >= overlapZ ? zSide : xSide;
      if (first != null) {
         columns.add(first);
      }

      if (second != null) {
         columns.add(second);
      }

      if (sx != 0 && sz != 0) {
         columns.add(centered.offset(sx, 0, sz));
      }

      return columns;
   }

   private boolean c_Z() {
      if (MC == null || MC.player == null || MC.options == null) {
         return false;
      }

      if (!this.flags_Z() || !this.wantsJump()) {
         return false;
      }

      if (!this.isPlaceableBlock(this.placeableStack())) {
         return false;
      }

      boolean directional = isKeyDown(MC.options.keyUp)
         || isKeyDown(MC.options.keyDown)
         || isKeyDown(MC.options.keyLeft)
         || isKeyDown(MC.options.keyRight);
      return !directional || MC.player.horizontalCollision;
   }

   private boolean d_Z() {
      if (this.c_Z()) {
         return true;
      } else {
         Vec3 lane = this.courseLine == null ? null : this.courseLine.load();
         if (lane == null || isDiagonal(lane)) {
            return true;
         } else if (!this.physicalClimbIntent && !this.reservationActive) {
            int since = ticksSince(AutismSessionState.getInstance().getClientTick(), this.footingRiseTick);
            return since >= 0 && since <= 30;
         } else {
            return true;
         }
      }
   }

   private RotationMath.Rotation open_ce_DataUtil() {
      BlockPos support = BlockPos.containing(MC.player.position()).below();
      float yaw = this.debouncedLaneYaw();
      float pitch = init_F_Vec3(MC.player.getEyePosition(), support, yaw);
      if (Float.isNaN(pitch)) {
         BlockPos cell = BlockPos.containing(MC.player.position());
         Vec3 point = new Vec3(cell.getX() + 0.5, cell.getY(), cell.getZ() + 0.5);
         pitch = RotationMath.lookAt(point, MC.player.getEyePosition()).pitch();
      }

      return new RotationMath.Rotation(yaw, capPitch89(pitch));
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex() {
      if (this.c_Z() && !MC.player.onGround() && MC.level != null) {
         BlockPos cell = BlockPos.containing(MC.player.position());

         for (int depth = 0; depth < 3; depth++) {
            if (MC.level.isOutsideBuildHeight(cell)) {
               return null;
            }

            if (!MC.level.getBlockState(cell).canBeReplaced()) {
               return null;
            }

            BlockPos below = cell.below();
            if (MC.level.isOutsideBuildHeight(below)) {
               return null;
            }

            if (!MC.level.getBlockState(below).canBeReplaced()) {
               return this.planPlacement(cell, MC.player.position(), false, null, true, Direction.UP);
            }

            cell = below;
         }

         return null;
      } else {
         return null;
      }
   }

   private ScaffoldModule.PlannedPlacement load_aI_TableIndex() {
      this.push_Str = "--";
      if (MC.player != null && MC.level != null) {
         if (!MC.player.onGround() && this.wantsJump()) {
            if (!this.d_Z()) {
               this.push_Str = "flat";
               return null;
            }

            Vec3 position = MC.player.position();
            Vec3 velocity = MC.player.getDeltaMovement();
            Vec3 lane = this.courseLine == null ? null : this.courseLine.load();
            StringBuilder fails = new StringBuilder();
            ScaffoldModule.PlannedPlacement footing = null;
            BlockPos arcLanding = this.load_BlockPos_I(this.footingRow());
            BlockPos ownSupport = this.footingRowCell();

            for (BlockPos support : this.load_List_3()) {
               BlockPos riser = support.above();
               if (!MC.level.isOutsideBuildHeight(riser)) {
                  boolean own = sameColumn(support, ownSupport) && lane == null;
                  String fail;
                  if (this.sync(riser)) {
                     fail = "dead";
                  } else if (!MC.level.getBlockState(riser).canBeReplaced()) {
                     fail = "occupied";
                  } else if (own ? !overlapsColumn(position, support) : !this.landsOnCellOnLane(position, velocity, riser)) {
                     fail = "arc";
                     if (this.isLiveCaptureActive() && !own) {
                        double[] landing = this.simulateLandingOnLane(position, velocity, velocity.y, riser.getY() + 1.0, false, this.sneakTicks());
                        fail = landing == null ? "arc(null)" : String.format(Locale.ROOT, "arc(ovl%.2f)", landingOverlapArea(landing, riser));
                     }
                  } else if (!this.hasCentreSupport(MC.level.getBlockState(support), support)) {
                     ScaffoldModule.PlannedPlacement sideReach = this.planPlacement(riser, position, false, lane, true, null);
                     if (sideReach != null) {
                        this.push_Str = "--";
                        return sideReach;
                     }

                     if (footing == null && support.equals(arcLanding) && !this.sync(support)) {
                        footing = this.planPlacement(support, position, false, lane, true, null);
                     }

                     fail = footing == null ? "no-support" : "footing";
                  } else {
                     ScaffoldModule.PlannedPlacement plan = this.planPlacement(riser, position, false, lane, true, Direction.UP);
                     if (plan != null) {
                        this.push_Str = "--";
                        return plan;
                     }

                     fail = "plan";
                  }

                  if (fails.length() > 0) {
                     fails.append(',');
                  }

                  fails.append(support.getX()).append('/').append(support.getZ()).append(':').append(fail);
               }
            }

            if (this.isLiveCaptureActive() && fails.length() > 0) {
               fails.append(";own=").append(ownSupport == null ? "--" : ownSupport.getX() + "/" + ownSupport.getZ());
               fails.append(";land=").append(arcLanding == null ? "--" : arcLanding.getX() + "/" + arcLanding.getZ());
            }

            this.push_Str = fails.length() == 0 ? "none" : fails.toString();
            return footing;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static double landingOverlapArea(double[] landing, BlockPos cell) {
      double overlapX = Math.min(landing[0] + 0.29, cell.getX() + 1.0) - Math.max(landing[0] - 0.29, cell.getX());
      double overlapZ = Math.min(landing[1] + 0.29, cell.getZ() + 1.0) - Math.max(landing[1] - 0.29, cell.getZ());
      return Math.min(overlapX, overlapZ);
   }

   private BlockPos load_BlockPos_I(int footingRow) {
      if (MC.player == null) {
         return null;
      }

      Vec3 position = MC.player.position();
      Vec3 velocity = MC.player.getDeltaMovement();
      boolean grounded = MC.player.onGround();
      double[] landing = this.simulateLandingOnLane(position, velocity, grounded ? 0.42 : velocity.y, footingRow + 2.0, grounded, this.sneakTicks());
      return landing == null ? null : new BlockPos(Mth.floor(landing[0]), footingRow, Mth.floor(landing[1]));
   }

   private BlockPos read_BlockPos_I(int footingRow) {
      if (MC.player == null) {
         return null;
      }

      Vec3 position = MC.player.position();
      Vec3 velocity = MC.player.getDeltaMovement();
      boolean grounded = MC.player.onGround();
      double takeoffVy = grounded ? 0.42 : velocity.y;
      double[] landing = null;
      if (this.d_Z()) {
         landing = this.simulateLandingOnLane(position, velocity, takeoffVy, footingRow + 2.0, grounded, this.sneakTicks());
      }

      if (landing == null) {
         landing = this.simulateLandingOnLane(position, velocity, takeoffVy, footingRow + 1.0, grounded, this.sneakTicks());
      }

      return landing == null ? null : new BlockPos(Mth.floor(landing[0]), footingRow, Mth.floor(landing[1]));
   }

   private List<BlockPos> load_List_3() {
      List<BlockPos> candidates = new ArrayList<>(6);
      int footingRow = this.footingRow();
      BlockPos foot = this.close_BlockPos();
      BlockPos own = new BlockPos(foot.getX(), footingRow, foot.getZ());
      BlockPos landing = this.load_BlockPos_I(footingRow);
      candidates.addAll(init_List_BlockPos(own, landing, isDiagonal(this.laneDirection())));
      Vec3 step = this.laneDirection();
      BlockPos ahead = new BlockPos(own.getX() + axisSign(step.x), footingRow, own.getZ() + axisSign(step.z));
      if (!ahead.equals(own) && !candidates.contains(ahead)) {
         candidates.add(ahead);
      }

      int tried = 0;

      for (Iterator<BlockPos> recent = this.recentPlaced.descendingIterator(); recent.hasNext() && tried < 3; tried++) {
         BlockPos support = recent.next();
         if (!candidates.contains(support)) {
            candidates.add(support);
         }
      }

      return candidates;
   }

   static List<BlockPos> init_List_BlockPos(BlockPos own, BlockPos landing, boolean diagonal) {
      List<BlockPos> ordered = new ArrayList<>(2);
      BlockPos first = diagonal ? landing : own;
      BlockPos second = diagonal ? own : landing;
      if (first != null) {
         ordered.add(first);
      }

      if (second != null && !ordered.contains(second)) {
         ordered.add(second);
      }

      return ordered;
   }

   static List<BlockPos> connectorCells(BlockPos landing, Vec3 laneDirection) {
      List<BlockPos> connectors = new ArrayList<>(2);
      if (landing != null && laneDirection != null) {
         int stepX = axisSign(laneDirection.x);
         int stepZ = axisSign(laneDirection.z);
         if (stepX != 0) {
            connectors.add(landing.offset(-stepX, 0, 0));
         }

         if (stepZ != 0) {
            BlockPos zLeg = landing.offset(0, 0, -stepZ);
            if (!connectors.contains(zLeg)) {
               connectors.add(zLeg);
            }
         }

         return connectors;
      } else {
         return connectors;
      }
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_BlockPos(BlockPos riser, BlockPos landing, Vec3 predicted, ItemStack stack, Vec3 lane) {
      if (riser != null && landing != null) {
         for (BlockPos connector : connectorCells(landing, lane)) {
            Direction face = axisDirectionBetween(landing, connector);
            if (face != null) {
               ScaffoldModule.PlannedPlacement viaStep = this.init_aI_TableIndex_BlockPos_2(riser, predicted, stack, lane, face);
               if (viaStep != null) {
                  return viaStep;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static Direction axisDirectionBetween(BlockPos landing, BlockPos connector) {
      if (landing != null && connector != null) {
         int dx = landing.getX() - connector.getX();
         int dz = landing.getZ() - connector.getZ();
         if (dx != 0 && dz != 0) {
            return null;
         } else if (dx != 0) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
         } else if (dz != 0) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private ScaffoldModule.PlannedPlacement load_aI_TableIndex_Vec3_ItemStack(Vec3 predicted, ItemStack stack) {
      if (MC.player != null && MC.level != null && this.f_Z() && this.courseLine != null) {
         Vec3 lane = this.courseLine.load();
         BlockPos landing = this.reservedSupport;
         if (landing == null) {
            landing = this.read_BlockPos_I(this.footingRow());
            if (landing == null || MC.level.isOutsideBuildHeight(landing)) {
               this.reservationReason = "unknown";
               return null;
            }

            this.reservedSupport = landing.immutable();
         }

         boolean supportSolid = this.open(landing);
         int deficit = supportSolid ? 0 : this.supportDeficit(landing);
         BlockPos riser = landing.above();
         boolean riserSolid = !MC.level.isOutsideBuildHeight(riser) && this.open(riser);
         ScaffoldModule.BridgeStage need = bridgeStageFor(supportSolid, deficit, riserSolid);
         if (need == ScaffoldModule.BridgeStage.READY || MC.level.isOutsideBuildHeight(riser)) {
            this.reservedConnector = null;
            this.reservationReason = "ready";
            return null;
         }

         if (need == ScaffoldModule.BridgeStage.RISER) {
            this.reservedConnector = null;
            if (!this.d_Z()) {
               this.reservedRiser = null;
               this.reservationReason = "ready";
               return null;
            }

            this.reservedRiser = riser.immutable();
            ScaffoldModule.PlannedPlacement rise = this.init_aI_TableIndex_BlockPos_2(riser, predicted, stack, lane, Direction.UP);
            if (rise == null) {
               rise = this.init_aI_TableIndex_BlockPos(riser, landing, predicted, stack, lane);
            }

            this.reservationReason = rise == null ? "riser-wait" : "riser";
            return rise;
         } else if (need == ScaffoldModule.BridgeStage.SUPPORT) {
            this.reservedConnector = null;
            ScaffoldModule.PlannedPlacement step = this.init_aI_TableIndex_BlockPos(landing, predicted, stack, lane);
            if (step != null) {
               this.reservationReason = "step";
               return step;
            } else {
               ScaffoldModule.PlannedPlacement support = this.init_aI_TableIndex_BlockPos_2(landing, predicted, stack, lane, null);
               this.reservationReason = support == null ? "support-wait" : "support";
               return support;
            }
         } else {
            List<ScaffoldModule.PlannedPlacement> legs = new ArrayList<>(2);

            for (BlockPos connector : connectorCells(landing, lane)) {
               if (!MC.level.isOutsideBuildHeight(connector)
                  && !this.open(connector)
                  && this.supportDeficit(connector) <= 1
                  && !init_Z_Vec3_6(MC.player.position(), lane, connector, 0.8)) {
                  ScaffoldModule.PlannedPlacement leg = this.init_aI_TableIndex_BlockPos_2(connector, predicted, stack, lane, null);
                  if (leg != null) {
                     legs.add(leg);
                  }
               }
            }

            if (legs.isEmpty()) {
               this.reservedConnector = null;
               this.reservationReason = "leg-wait";
               return null;
            } else {
               legs.sort(planOrder(predicted, this.placeAimRotation()));
               ScaffoldModule.PlannedPlacement leg = pickLeg(legs, MC.player.getEyePosition(), this.leadMotion(), landing, this.reservedConnector);
               this.reservedConnector = leg.load().immutable();
               this.reservationReason = leg == legs.get(0) ? "leg" : "leg-plane";
               return leg;
            }
         }
      } else {
         this.reservationReason = "--";
         return null;
      }
   }

   static ScaffoldModule.PlannedPlacement pickLeg(List<ScaffoldModule.PlannedPlacement> legs, Vec3 eye, BlockPos held) {
      ScaffoldModule.PlannedPlacement best = legs.get(0);
      if (legs.size() >= 2 && best.face().getAxis().isHorizontal()) {
         double bestPast = distancePastSupportFace(eye, best.support(), best.face());

         for (int i = 1; i < legs.size(); i++) {
            ScaffoldModule.PlannedPlacement leg = legs.get(i);
            if (leg.face().getAxis().isHorizontal()) {
               double past = distancePastSupportFace(eye, leg.support(), leg.face());
               if (past > bestPast) {
                  best = leg;
                  bestPast = past;
               }
            }
         }

         if (held != null && !best.load().equals(held)) {
            for (ScaffoldModule.PlannedPlacement leg : legs) {
               if (leg.load().equals(held) && leg.face().getAxis().isHorizontal()) {
                  double heldPast = distancePastSupportFace(eye, leg.support(), leg.face());
                  if (!read_Z_D_D(bestPast, heldPast) && bestPast - heldPast <= 0.25) {
                     return leg;
                  }
                  break;
               }
            }

            return best;
         } else {
            return best;
         }
      } else {
         return best;
      }
   }

   static boolean read_Z_D_D(double bestPast, double heldPast) {
      return bestPast >= 0.05 && heldPast < 0.05;
   }

   static ScaffoldModule.PlannedPlacement pickLeg(List<ScaffoldModule.PlannedPlacement> legs, Vec3 eye, Vec3 lead, BlockPos held) {
      return pickLeg(legs, eye, lead, null, held);
   }

   static ScaffoldModule.PlannedPlacement pickLeg(List<ScaffoldModule.PlannedPlacement> legs, Vec3 eye, Vec3 lead, BlockPos landing, BlockPos held) {
      List<ScaffoldModule.PlannedPlacement> live = sightedLegs(legs, eye, lead);
      if (held == null) {
         ScaffoldModule.PlannedPlacement successor = successorLeg(live, eye, lead, landing);
         if (successor != null) {
            return successor;
         }
      }

      return pickLeg(live, eye, held);
   }

   static ScaffoldModule.PlannedPlacement successorLeg(List<ScaffoldModule.PlannedPlacement> legs, Vec3 eye, Vec3 lead, BlockPos landing) {
      if (legs != null && legs.size() >= 2 && landing != null && eye != null) {
         ScaffoldModule.PlannedPlacement only = null;
         double bestPast = -1.7976931348623157E308;

         for (ScaffoldModule.PlannedPlacement leg : legs) {
            bestPast = Math.max(bestPast, distancePastSupportFace(eye, leg.support(), leg.face()));
            if (init_Z_aI_TableIndex(leg, landing, eye, lead)) {
               if (only != null) {
                  return null;
               }

               only = leg;
            }
         }

         if (only == null) {
            return null;
         }

         double past = distancePastSupportFace(eye, only.support(), only.face());
         return bestPast - past <= 0.25 ? only : null;
      } else {
         return null;
      }
   }

   static boolean init_Z_aI_TableIndex(ScaffoldModule.PlannedPlacement leg, BlockPos landing, Vec3 eye, Vec3 lead) {
      if (leg != null && landing != null) {
         Direction face = axisDirectionBetween(landing, leg.load());
         if (face == null) {
            return false;
         }

         Vec3 at = lead == null ? eye : eye.add(lead.scale(2.0));
         return read_Z_Vec3(at, leg.load(), face, leg.rotation().yaw());
      } else {
         return false;
      }
   }

   static List<ScaffoldModule.PlannedPlacement> sightedLegs(List<ScaffoldModule.PlannedPlacement> legs, Vec3 eye, Vec3 lead) {
      if (legs != null && legs.size() >= 2) {
         List<ScaffoldModule.PlannedPlacement> sighted = new ArrayList<>(legs.size());

         for (ScaffoldModule.PlannedPlacement leg : legs) {
            if (isLegSighted(leg, eye, lead)) {
               sighted.add(leg);
            }
         }

         return !sighted.isEmpty() && sighted.size() != legs.size() ? sighted : legs;
      } else {
         return legs;
      }
   }

   static boolean isLegSighted(ScaffoldModule.PlannedPlacement leg, Vec3 eye, Vec3 lead) {
      if (leg != null && eye != null && leg.face().getAxis().isHorizontal()) {
         float yaw = leg.rotation().yaw();

         for (int step = 0; step <= 2; step++) {
            Vec3 at = lead == null ? eye : eye.add(lead.scale(step));
            if (distancePastSupportFace(at, leg.support(), leg.face()) <= 0.0) {
               return true;
            }

            if (read_Z_Vec3(at, leg.support(), leg.face(), yaw)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_BlockPos_2(BlockPos candidate, Vec3 predicted, ItemStack stack, Vec3 lane, Direction preferredFace) {
      if (candidate == null || MC.level.isOutsideBuildHeight(candidate)) {
         return this.tracePlan(candidate, "oob", null);
      }

      if (this.sync(candidate)) {
         return this.tracePlan(candidate, "cooldown", null);
      }

      if (init_Z_Vec3_6(MC.player.position(), lane, candidate, 0.8)) {
         return this.tracePlan(candidate, "behind", null);
      }

      BlockState state = MC.level.getBlockState(candidate);
      if (!this.hasCentreSupport(state, candidate) && state.canBeReplaced()) {
         boolean replaceExisting = !state.isAir() && state.getFluidState().isEmpty();
         if (replaceExisting && !this.canReplaceWith(state, candidate, stack)) {
            return this.tracePlan(candidate, "no-repl", null);
         }

         ScaffoldModule.RowLockBounds rowLock = this.rowLockBounds();
         boolean rising = candidate.getY() > this.footingRow();
         if (rowLock != null && !rowLock.init(candidate, rising)) {
            return this.tracePlan(candidate, "rowlock", null);
         }

         Direction onlyFace = preferredFace != null ? preferredFace : (rowLock == null ? null : rowLock.init(candidate));
         ScaffoldModule.PlannedPlacement plan = this.planPlacement(candidate, predicted, replaceExisting, lane, true, onlyFace, true);
         return plan != null && this.push(plan)
            ? this.tracePlan(candidate, "blind", null)
            : this.tracePlan(candidate, plan == null ? "plan" : "ok", plan);
      } else {
         return this.tracePlan(candidate, "solid", null);
      }
   }

   static List<BlockPos> backCells(BlockPos landing, Vec3 lane) {
      List<BlockPos> candidates = new ArrayList<>(2);
      if (landing != null && lane != null) {
         int backX = -axisSign(lane.x);
         int backZ = -axisSign(lane.z);
         if (backX != 0) {
            candidates.add(landing.offset(backX, 1, 0));
         }

         if (backZ != 0) {
            candidates.add(landing.offset(0, 1, backZ));
         }

         return candidates;
      } else {
         return candidates;
      }
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_BlockPos(BlockPos landing, Vec3 predicted, ItemStack stack, Vec3 lane) {
      if (landing != null && lane != null && MC.level != null && this.d_Z()) {
         BlockPos rise = landing.above();
         if (!MC.level.isOutsideBuildHeight(rise) && !this.open(rise)) {
            for (BlockPos step : backCells(landing, lane)) {
               if (!MC.level.isOutsideBuildHeight(step) && !this.open(step) && this.open(step.below())) {
                  ScaffoldModule.PlannedPlacement plan = this.init_aI_TableIndex_BlockPos_2(step, predicted, stack, lane, Direction.UP);
                  if (plan != null) {
                     this.reservedStep = step.immutable();
                     return plan;
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private ScaffoldModule.PlannedPlacement tracePlan(BlockPos cell, String why, ScaffoldModule.PlannedPlacement plan) {
      if (this.isLiveCaptureActive() && this.get_Str.length() <= 220) {
         String detail = "plan".equals(why) && this.planDetail.length() > 0 ? "plan[" + this.planDetail.toString().trim() + "]" : why;
         if (plan != null && MC.player != null && plan.face().getAxis().isHorizontal()) {
            detail = detail + String.format(Locale.ROOT, "/past%+.2f", distancePastSupportFace(MC.player.getEyePosition(), plan.support(), plan.face()));
         }

         this.get_Str = ("--".equals(this.get_Str) ? "" : this.get_Str + ",") + formatCell(cell) + ":" + detail;
         return plan;
      } else {
         return plan;
      }
   }

   private static String formatCell(BlockPos cell) {
      return cell == null ? "--" : cell.getX() + "," + cell.getY() + "," + cell.getZ();
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_ItemStack(ItemStack stack) {
      this.frame_Z = false;
      this.get_Str = "--";
      ScaffoldModule.PlannedPlacement target = this.load_aI_TableIndex_ItemStack(stack);
      if (this.flags_Z() && target != null && MC.player != null) {
         int stampAge = ticksSince(AutismSessionState.getInstance().getClientTick(), this.f_ak_I);
         boolean stampFresh = this.tick_BlockPos != null && stampAge >= 0 && stampAge <= 8;
         boolean windowAlive = this.init_Z_aI_TableIndex(target)
            || load_Z_Vec3(MC.player.getEyePosition(), target.support(), target.face(), target.rotation().yaw(), true);
         if (init_Z_Z(
            MC.player.onGround(),
            MC.player.getDeltaMovement().y < 0.0,
            stampFresh,
            target.load().equals(this.tick_BlockPos),
            target.face().getAxis().isHorizontal(),
            this.sync(target),
            windowAlive
         )) {
            this.targetSource = "steal-veto:" + target.load().toShortString();
            this.stickyPlacement = null;
            return null;
         }
      }

      target = this.read_aI_TableIndex_aI_TableIndex(target);
      if (this.flags_Z() && !this.frame_Z) {
         this.holdPlacement(target);
      }

      return target;
   }

   private ScaffoldModule.PlannedPlacement read_aI_TableIndex_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      if (target != null && this.flags_Z() && MC.player != null && MC.level != null) {
         BlockPos placed = target.load();
         BlockState state = MC.level.getBlockState(placed);
         boolean replaceExisting = !state.isAir() && state.getFluidState().isEmpty();
         if (!init_Z_Direction(target.face(), replaceExisting, this.isFullCube(placed.below()))) {
            return target;
         } else {
            String planFail = this.planFailure;
            ScaffoldModule.PlannedPlacement up = this.planPlacement(
               placed,
               this.predictedPosition(this.courseLine),
               replaceExisting,
               this.courseLine == null ? null : this.courseLine.load(),
               this.f_Z(),
               Direction.UP
            );
            this.planFailure = planFail;
            if (up != null && up.load().equals(placed) && up.face() == Direction.UP) {
               this.upFaceSwapCell = placed.immutable();
               return up;
            } else {
               return target;
            }
         }
      } else {
         return target;
      }
   }

   static List<ScaffoldModule.BridgeStepNode> bridgeSteps(BlockPos landing, int maxMissing, Predicate<BlockPos> supported, Predicate<BlockPos> open) {
      if (landing != null && maxMissing > 0 && !supported.test(landing) && open.test(landing)) {
         int limit = Math.min(6, maxMissing);
         ArrayDeque<ScaffoldModule.BridgeStepNode> queue = new ArrayDeque<>();
         Set<BlockPos> seen = new HashSet<>();
         List<ScaffoldModule.BridgeStepNode> frontier = new ArrayList<>();
         queue.add(new ScaffoldModule.BridgeStepNode(landing.immutable(), 1));
         seen.add(landing);

         while (!queue.isEmpty()) {
            ScaffoldModule.BridgeStepNode link = queue.removeFirst();
            boolean anchored = false;

            for (Direction side : Direction.values()) {
               BlockPos adjacent = link.cell().relative(side);
               if (supported.test(adjacent)) {
                  anchored = true;
               } else if (side.getAxis().isHorizontal() && link.depth() < limit && seen.add(adjacent) && open.test(adjacent)) {
                  queue.addLast(new ScaffoldModule.BridgeStepNode(adjacent, link.depth() + 1));
               }
            }

            if (anchored) {
               frontier.add(link);
            }
         }

         frontier.sort(
            Comparator.comparingInt(ScaffoldModule.BridgeStepNode::depth)
               .thenComparingInt(linkx -> linkx.cell().getX())
               .thenComparingInt(linkx -> linkx.cell().getZ())
         );
         return frontier;
      } else {
         return List.of();
      }
   }

   static boolean isFlatDiagonalTelly(String mode, boolean diagonal, boolean ascending) {
      return "Telly".equals(mode) && diagonal && !ascending;
   }

   private ScaffoldModule.PlannedPlacement read_aI_TableIndex_Vec3_ItemStack(Vec3 predicted, ItemStack stack) {
      if (isFlatDiagonalTelly(this.choice("mode"), this.diagonalBridge, this.d_Z())
         && MC.player != null
         && MC.level != null
         && this.courseLine != null
         && (!MC.player.onGround() || this.wantsJump())) {
         int row = this.footingRow();
         if (row != -2147483648 && !(MC.player.getY() < row + 1.0)) {
            boolean grounded = MC.player.onGround();
            double[] landing = this.simulateLandingOnLane(
               MC.player.position(), MC.player.getDeltaMovement(), grounded ? 0.42 : MC.player.getDeltaMovement().y, row + 1.0, grounded, this.sneakTicks()
            );
            if (landing == null) {
               return null;
            }

            BlockPos goal = new BlockPos(Mth.floor(landing[0]), row, Mth.floor(landing[1]));
            List<ScaffoldModule.BridgeStepNode> links = bridgeSteps(
               goal, 6, this::poll, cell -> !MC.level.isOutsideBuildHeight(cell) && MC.level.hasChunkAt(cell) && MC.level.getBlockState(cell).canBeReplaced()
            );
            if (links.isEmpty()) {
               return null;
            }

            this.reservedSupport = goal.immutable();
            Vec3 lane = this.courseLine.load();
            List<ScaffoldModule.PlannedPlacement> plans = new ArrayList<>();
            int bestMissing = 2147483647;

            for (ScaffoldModule.BridgeStepNode link : links) {
               if (link.depth() > bestMissing) {
                  break;
               }

               if (!this.sync(link.cell())) {
                  ScaffoldModule.PlannedPlacement plan = this.init_aI_TableIndex_BlockPos_2(link.cell(), predicted, stack, lane, null);
                  if (plan != null && !this.poll(plan)) {
                     bestMissing = link.depth();
                     plans.add(plan);
                  }
               }
            }

            if (plans.isEmpty()) {
               return null;
            }

            plans.sort(planOrder(predicted, this.placeAimRotation()));
            BlockPos previous = this.stickyPlacement == null ? null : this.stickyPlacement.load();
            BlockPos held = previous != null && plans.stream().anyMatch(planx -> planx.load().equals(previous)) ? previous : null;
            return pickLeg(plans, MC.player.getEyePosition(), this.leadMotion(), goal, held);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private ScaffoldModule.PlannedPlacement load_aI_TableIndex_ItemStack(ItemStack stack) {
      if (!this.isPlaceableBlock(stack)) {
         return null;
      }

      Vec3 predicted = this.predictedPosition(this.courseLine);
      BlockPos predictedBase = blockBelow(predicted);
      if (this.flags_Z()) {
         ScaffoldModule.PlannedPlacement tower = this.init_aI_TableIndex();
         if (tower != null) {
            this.stickyPlacement = tower;
            this.stickyTick = AutismSessionState.getInstance().getClientTick();
            this.f_aH_I = 0;
            this.targetSource = "tower:" + tower.load().toShortString();
            return tower;
         }

         ScaffoldModule.PlannedPlacement tellyLanding = this.read_aI_TableIndex_Vec3_ItemStack(predicted, stack);
         if (tellyLanding != null) {
            this.stickyPlacement = tellyLanding;
            this.stickyTick = AutismSessionState.getInstance().getClientTick();
            this.f_aH_I = 0;
            this.targetSource = "telly-floor:" + tellyLanding.load().toShortString();
            return tellyLanding;
         }

         ScaffoldModule.PlannedPlacement rescue = this.init_aI_TableIndex_Vec3_ItemStack(predicted, stack);
         if (rescue != null) {
            this.f_aU_I = AutismSessionState.getInstance().getClientTick();
            this.stickyPlacement = rescue;
            this.stickyTick = AutismSessionState.getInstance().getClientTick();
            this.f_aH_I = 0;
            this.targetSource = "rescue:" + rescue.load().toShortString();
            return rescue;
         }

         ScaffoldModule.PlannedPlacement riser = this.load_aI_TableIndex();
         if (riser != null) {
            this.stickyPlacement = riser;
            this.stickyTick = AutismSessionState.getInstance().getClientTick();
            this.f_aH_I = 0;
            this.targetSource = "riser:" + riser.load().toShortString();
            return riser;
         }

         ScaffoldModule.PlannedPlacement reservation = this.load_aI_TableIndex_Vec3_ItemStack(predicted, stack);
         if (reservation != null && !this.open(reservation) && !this.poll(reservation)) {
            this.stickyPlacement = reservation;
            this.stickyTick = AutismSessionState.getInstance().getClientTick();
            this.f_aH_I = 0;
            this.targetSource = "reserve-" + this.reservationReason + ":" + reservation.load().toShortString();
            return reservation;
         }

         if (MC.player != null && !MC.player.onGround() && MC.player.getDeltaMovement().y < 0.0 && this.i()) {
            ScaffoldModule.PlannedPlacement catchBelow = this.init_aI_TableIndex_Vec3(
               predicted, this.courseLine == null ? null : this.courseLine.load(), this.wantsJump()
            );
            if (catchBelow != null) {
               this.f_aU_I = AutismSessionState.getInstance().getClientTick();
               this.stickyPlacement = catchBelow;
               this.stickyTick = AutismSessionState.getInstance().getClientTick();
               this.f_aH_I = 0;
               this.targetSource = "catch:" + catchBelow.load().toShortString();
               return catchBelow;
            }
         }

         boolean rowLocked = this.rowLockBounds() != null;
         if (!rowLocked) {
            predictedBase = this.load_BlockPos_BlockPos(predictedBase);
            ScaffoldModule.PlannedPlacement drift = this.init_aI_TableIndex_BlockPos(predictedBase);
            if (drift != null) {
               this.stickyPlacement = drift;
               this.stickyTick = AutismSessionState.getInstance().getClientTick();
               this.f_aH_I = 0;
               return drift;
            }
         }

         ScaffoldModule.PlannedPlacement held = this.read_aI_TableIndex();
         if (held != null && !this.poll(held)) {
            this.targetSource = "hold:" + held.load().toShortString();
            return held;
         }

         ScaffoldModule.PlannedPlacement sticky = this.init_aI_TableIndex_Vec3(predicted);
         if (sticky != null && !this.open(sticky) && !this.poll(sticky)) {
            this.targetSource = "sticky";
            return sticky;
         }

         ScaffoldModule.PlannedPlacement fresh = this.init_aI_TableIndex_BlockPos_2(predictedBase, predicted, stack);
         String freshTier = rowLocked ? "row" : "lane";
         if (fresh != null && this.poll(fresh)) {
            fresh = null;
            this.targetSource = freshTier + "-uncatch";
         } else {
            this.targetSource = fresh != null ? freshTier + ":" + fresh.load().toShortString() : freshTier + "-null";
         }

         this.stickyPlacement = fresh;
         this.stickyTick = AutismSessionState.getInstance().getClientTick();
         this.f_aH_I = 0;
         return fresh;
      } else {
         return this.init_aI_TableIndex_BlockPos_2(predictedBase, predicted, stack);
      }
   }

   private BlockPos load_BlockPos_BlockPos(BlockPos base) {
      if (base != null && this.set_BlockPos != null && this.courseLine != null) {
         Vec3 direction = this.courseLine.load();
         if (isDiagonal(direction)) {
            return base;
         } else {
            boolean northSouth = Math.abs(direction.z) > Math.abs(direction.x);
            int row = northSouth ? this.set_BlockPos.getX() : this.set_BlockPos.getZ();
            if (northSouth) {
               return base.getX() == row ? base : new BlockPos(row, base.getY(), base.getZ());
            } else {
               return base.getZ() == row ? base : new BlockPos(base.getX(), base.getY(), row);
            }
         }
      } else {
         return base;
      }
   }

   private BlockPos close_BlockPos() {
      Vec3 velocity = MC.player.getDeltaMovement();
      Vec3 position = MC.player.position();
      Vec3 future = position.add(velocity.x * 2.0, 0.0, velocity.z * 2.0);
      BlockPos foot = BlockPos.containing(future);
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.f_bw_I == tick && this.set_BlockPos != null) {
         return new BlockPos(this.set_BlockPos.getX(), foot.getY(), this.set_BlockPos.getZ());
      }

      this.f_bw_I = tick;
      BlockPos committed = this.set_BlockPos;
      if (committed != null && (foot.getX() != committed.getX() || foot.getZ() != committed.getZ())) {
         boolean projectionFirm = true;
         if (foot.getX() != committed.getX()) {
            projectionFirm &= load_Z_D_Z(future.x, foot.getX() > committed.getX());
         }

         if (foot.getZ() != committed.getZ()) {
            projectionFirm &= load_Z_D_Z(future.z, foot.getZ() > committed.getZ());
         }

         if (projectionFirm) {
            this.set_BlockPos = foot;
         } else {
            BlockPos currentCell = BlockPos.containing(position);
            if (currentCell.getX() != committed.getX() || currentCell.getZ() != committed.getZ()) {
               boolean positionFirm = true;
               if (currentCell.getX() != committed.getX()) {
                  positionFirm &= init_Z_D_Z(position.x, currentCell.getX() > committed.getX());
               }

               if (currentCell.getZ() != committed.getZ()) {
                  positionFirm &= init_Z_D_Z(position.z, currentCell.getZ() > committed.getZ());
               }

               if (positionFirm) {
                  this.set_BlockPos = currentCell;
               }
            }
         }

         return new BlockPos(this.set_BlockPos.getX(), foot.getY(), this.set_BlockPos.getZ());
      } else {
         this.set_BlockPos = foot;
         return foot;
      }
   }

   private static boolean init_Z_D_Z(double coordinate, boolean positive) {
      double fraction = coordinate - Math.floor(coordinate);
      return positive ? fraction > 0.1 : fraction < 0.9;
   }

   private static boolean load_Z_D_Z(double coordinate, boolean positive) {
      double fraction = coordinate - Math.floor(coordinate);
      return positive ? fraction > 0.15 : fraction < 0.85;
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_Vec3(Vec3 predicted) {
      ScaffoldModule.PlannedPlacement sticky = this.stickyPlacement;
      if (sticky == null) {
         return null;
      }

      this.stickyPlacement = null;
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.stickyTick >= 0 && tick - this.stickyTick <= 6) {
         BlockPos placed = sticky.load();
         if (!MC.level.isOutsideBuildHeight(placed) && !this.hasCentreSupport(MC.level.getBlockState(placed), placed)) {
            BlockPos support = sticky.support();
            if (!MC.level.isOutsideBuildHeight(support) && !MC.level.getBlockState(support).canBeReplaced()) {
               if (MC.player != null && MC.player.onGround() && placed.getY() < BlockPos.containing(MC.player.position()).getY() - 1) {
                  return null;
               }

               Vec3 laneDirection = this.courseLine == null ? null : this.courseLine.load();
               if (MC.player != null && !this.h_Z() && init_Z_Vec3_6(MC.player.position(), laneDirection, placed, 0.8)) {
                  return null;
               }

               if (sticky.face().getAxis().isVertical() && MC.player != null && !write(MC.player.position(), MC.player.getDeltaMovement(), placed)) {
                  return null;
               }

               if (MC.player != null && isFartherThan(MC.player.position(), support, 2.5)) {
                  return null;
               }

               boolean jumping = this.f_Z();
               ScaffoldModule.RowLockBounds rowLock = this.rowLockBounds();
               if (rowLock != null && !rowLock.init(placed, this.isRising(jumping))) {
                  return null;
               }

               ScaffoldModule.PlannedPlacement replanned = this.planPlacement(placed, predicted, false, laneDirection, jumping, sticky.face());
               if (replanned == null || !replanned.support().equals(support) || replanned.face() != sticky.face()) {
                  return null;
               }

               if (MC.player != null && !isLegSighted(replanned, MC.player.getEyePosition(), this.leadMotion())) {
                  return null;
               }

               if (replanned.face().getAxis().isHorizontal() && MC.player != null) {
                  boolean bandExit = init_Z_Vec3_2(MC.player.getEyePosition(), replanned.support(), replanned.face());
                  boolean outOfReach = isPitchBeyondLimit(
                     requiredPitchAt(MC.player.getEyePosition(), replanned.support(), replanned.face(), replanned.rotation().yaw())
                  );
                  if (this.f_aI_I != tick) {
                     this.f_aI_I = tick;
                     this.f_aH_I = bandExit ? this.f_aH_I + 1 : 0;
                     this.outOfReachTicks = outOfReach ? this.outOfReachTicks + 1 : 0;
                  }

                  if (this.f_aH_I >= 2) {
                     this.f_aH_I = 0;
                     return null;
                  }

                  if (this.outOfReachTicks >= 2) {
                     this.outOfReachTicks = 0;
                     return null;
                  }
               }

               this.stickyPlacement = replanned;
               return replanned;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean isPitchBeyondLimit(float requiredPitch) {
      return Float.isFinite(requiredPitch) && requiredPitch > 89.5F;
   }

   private float sync(float freeYaw) {
      if (this.laneOctant() == -1) {
         return freeYaw;
      } else {
         return this.f_ai_I < 1 && !this.h_Z() ? snapToLaneOctant(this.laneYaw(), freeYaw) : freeYaw;
      }
   }

   private boolean e_Z() {
      return this.init_Z_ce_DataUtil(this.activeRotation);
   }

   private boolean init_Z_ce_DataUtil(RotationMath.Rotation rotation) {
      if (rotation != null && this.laneOctant() != -1) {
         return this.f_ai_I < 1 && !this.h_Z() ? Math.abs(octantResidual(this.laneYaw(), rotation.yaw())) > 12.0F : false;
      } else {
         return false;
      }
   }

   private boolean pop(ScaffoldModule.PlannedPlacement target) {
      return target != null && MC.player != null && target.face().getAxis().isHorizontal()
         ? isPitchBeyondLimit(requiredPitchAt(MC.player.getEyePosition(), target.support(), target.face(), target.rotation().yaw()))
         : false;
   }

   static boolean init_Z_Vec3_2(Vec3 eye, BlockPos support, Direction face) {
      boolean xAxisFace = face.getStepX() != 0;
      double lateral = xAxisFace ? eye.z : eye.x;
      double edge = xAxisFace ? support.getZ() : support.getX();
      return lateral < edge || lateral > edge + 1.0;
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_BlockPos(BlockPos laneBase) {
      if (MC.player != null && laneBase != null) {
         Vec3 velocity = MC.player.getDeltaMovement();
         double horizontalSpeed = Math.hypot(velocity.x, velocity.z);
         if (horizontalSpeed < 0.05) {
            return null;
         }

         BlockPos footCell = this.close_BlockPos();
         if (footCell.getY() - 1 != laneBase.getY()) {
            return null;
         }

         BlockPos driftBase = new BlockPos(footCell.getX(), laneBase.getY(), footCell.getZ());
         if (driftBase.equals(laneBase)) {
            return null;
         }

         if (this.k() && MC.player.position().y < driftBase.getY() + 1.5) {
            return null;
         }

         if (Vec3.atCenterOf(driftBase).subtract(MC.player.position()).horizontalDistance() > 2.0) {
            return null;
         }

         if (MC.level.isOutsideBuildHeight(driftBase)) {
            return null;
         }

         if (this.hasCentreSupport(MC.level.getBlockState(driftBase), driftBase)) {
            return null;
         }

         Vec3 future = MC.player.position().add(velocity.x * 2.0, 0.0, velocity.z * 2.0);
         Vec3 laneDirection = this.courseLine == null ? null : this.courseLine.load();
         boolean jumping = this.f_Z();
         if (!this.h_Z() && init_Z_Vec3_6(MC.player.position(), laneDirection, driftBase, 0.8)) {
            return null;
         }

         ScaffoldModule.PlannedPlacement direct = this.planPlacement(driftBase, future, false, laneDirection, jumping);
         if (direct != null) {
            this.targetSource = "guard:" + driftBase.toShortString();
            return direct;
         }

         List<ScaffoldModule.PlannedPlacement> seedPlans = new ArrayList<>(4);

         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos seed = driftBase.relative(direction);
            if (!MC.level.isOutsideBuildHeight(seed) && !(Vec3.atCenterOf(seed).subtract(MC.player.position()).horizontalDistance() > 2.0)) {
               BlockState seedState = MC.level.getBlockState(seed);
               if (seedState.isAir()) {
                  ScaffoldModule.PlannedPlacement seeded = this.planPlacement(seed, future, false, laneDirection, jumping);
                  if (seeded != null) {
                     seedPlans.add(seeded);
                  }
               }
            }
         }

         Vec3 ahead = new Vec3(velocity.x, 0.0, velocity.z);
         seedPlans.removeIf(plan -> Vec3.atCenterOf(plan.load()).subtract(MC.player.position()).dot(ahead) < 0.0);
         if (seedPlans.isEmpty()) {
            return null;
         }

         seedPlans.sort(planOrder(future, this.placeAimRotation()));
         this.targetSource = "gseed:" + seedPlans.get(0).load().toShortString();
         return seedPlans.get(0);
      } else {
         return null;
      }
   }

   static Comparator<ScaffoldModule.PlannedPlacement> planOrder(Vec3 future, RotationMath.Rotation posture) {
      return Comparator.<ScaffoldModule.PlannedPlacement>comparingDouble(plan -> Math.ceil(Vec3.atCenterOf(plan.load()).distanceTo(future) / 0.5))
         .thenComparingDouble(plan -> angleBetween(posture, plan.rotation()));
   }

   static BlockPos blockBelow(Vec3 position) {
      return BlockPos.containing(position).below();
   }

   private BlockPos read_BlockPos_BlockPos(BlockPos realBase) {
      if (MC.player == null || !MC.player.onGround()) {
         return null;
      } else if (this.courseLine == null) {
         return null;
      } else {
         Vec3 direction = this.courseLine.load();
         if (direction == null) {
            return null;
         } else {
            int stepX = axisSign(direction.x);
            int stepZ = axisSign(direction.z);
            if (stepX == 0 && stepZ == 0) {
               return null;
            } else {
               BlockPos ahead = realBase.offset(stepX, 0, stepZ);
               if (MC.level.isOutsideBuildHeight(ahead)) {
                  return null;
               } else {
                  return this.hasCentreSupport(MC.level.getBlockState(ahead), ahead) ? null : ahead;
               }
            }
         }
      }
   }

   private ScaffoldModule.PlannedPlacement init_aI_TableIndex_BlockPos_2(BlockPos base, Vec3 plannedPosition, ItemStack stack) {
      if (base == null) {
         this.planFailure = "base-null";
         return null;
      }

      if (MC.level.isOutsideBuildHeight(base)) {
         this.planFailure = "base-outside-height " + base.toShortString();
         return null;
      }

      BlockState baseState = MC.level.getBlockState(base);
      if (this.hasCentreSupport(baseState, base)) {
         BlockPos realBase = blockBelow(MC.player.position());
         if (this.flags_Z()
            && !realBase.equals(base)
            && !MC.level.isOutsideBuildHeight(realBase)
            && !this.hasCentreSupport(MC.level.getBlockState(realBase), realBase)) {
            base = realBase;
         } else {
            BlockPos ahead = this.read_BlockPos_BlockPos(realBase);
            if (ahead == null) {
               this.planFailure = "base-solid " + base.toShortString();
               return null;
            }

            base = ahead;
         }
      }

      Vec3 laneDirection = this.courseLine == null ? null : this.courseLine.load();
      boolean jumping = this.f_Z();
      boolean diagonal = isDiagonal(laneDirection);
      List<BlockPos> offsets = this.flags_Z()
         ? this.orderCandidates(cachedNeighbourOffsets(), base, plannedPosition, this.courseLine)
         : this.orderCandidates(NEIGHBOUR_OFFSETS, base, plannedPosition, this.courseLine);
      int solidSkips = 0;
      int replaceSkips = 0;
      int planNulls = 0;
      int gateAbove = 0;
      int gateFloor = 0;
      int gateBehind = 0;
      int gateColumnHold = 0;
      int gateRiseVeto = 0;
      int gateRowLock = 0;
      int gateHeight = 0;
      int gateDead = 0;
      int gateBelow = 0;
      String firstPlanDetail = null;
      this.planFailure = "";
      ScaffoldModule.RowLockBounds rowLock = this.flags_Z() ? this.rowLockBounds() : null;
      boolean gateRise = this.flags_Z() && !this.h_Z();
      Vec3 riseFrom = MC.player.position();
      Vec3 riseCarry = MC.player.getDeltaMovement();
      boolean gateColumn = this.flags_Z() && this.groundedFootingRow != -2147483648;
      boolean riseAllowed = this.isRising(jumping);
      boolean preferHeight = gateRise && riseAllowed;
      int footingRow = this.footingRow();
      int planTick = AutismSessionState.getInstance().getClientTick();
      ScaffoldModule.PlannedPlacement firstPlan = null;

      for (BlockPos offset : offsets) {
         BlockPos candidate = base.offset(offset);
         if (offset.getY() > 0 && MC.player.onGround() && !jumping) {
            gateAbove++;
         } else if (gateRise
            && offset.getY() > 0
            && overlapsColumn(riseFrom, candidate)
            && !hasLaneSupportAhead(this.laneOctant(), candidate.below(), isCellAhead(riseFrom, laneDirection, candidate), this::open)) {
            gateFloor++;
         } else if (gateRise && init_Z_Vec3_6(riseFrom, laneDirection, candidate, 0.8)) {
            gateBehind++;
         } else if (gateColumn
            && candidate.getY() > this.groundedFootingRow
            && overlapsColumn(riseFrom, candidate)
            && !init_Z_Vec3_5(riseFrom, riseCarry, candidate, 3, 0.15)) {
            gateColumnHold++;
         } else if (init_Z_Z(preferHeight, candidate.getY(), footingRow)
            && !fallLandsOnCell(riseFrom, riseCarry, candidate)
            && !write(riseFrom, riseCarry, candidate)) {
            gateRiseVeto++;
         } else if (this.flags_Z() && isBelowFooting(candidate.getY(), this.airborneFloorRow(), this.h_Z())) {
            gateBelow++;
         } else {
            Direction pinnedFace = null;
            if (rowLock != null) {
               if (!rowLock.init(candidate, riseAllowed)) {
                  gateRowLock++;
                  continue;
               }

               pinnedFace = rowLock.init(candidate);
            }

            if (MC.level.isOutsideBuildHeight(candidate)) {
               gateHeight++;
            } else if (this.sync(candidate)) {
               gateDead++;
            } else {
               BlockState candidateState = MC.level.getBlockState(candidate);
               if (this.hasCentreSupport(candidateState, candidate)) {
                  solidSkips++;
               } else {
                  boolean replaceExisting = !candidateState.isAir() && candidateState.getFluidState().isEmpty();
                  if (replaceExisting && !this.canReplaceWith(candidateState, candidate, stack)) {
                     replaceSkips++;
                  } else {
                     ScaffoldModule.PlannedPlacement target = this.planPlacement(
                        candidate, plannedPosition, replaceExisting, laneDirection, jumping, pinnedFace
                     );
                     if (target != null) {
                        if (!preferHeight) {
                           return target;
                        }

                        if (target.load().getY() > footingRow) {
                           return target;
                        }

                        if (firstPlan == null) {
                           firstPlan = target;
                        }
                     }

                     if (firstPlanDetail == null) {
                        firstPlanDetail = candidate.toShortString() + "{" + this.planDetail.toString().trim() + "}";
                     }

                     planNulls++;
                  }
               }
            }
         }
      }

      if (firstPlan != null) {
         return firstPlan;
      }

      this.planFailure = "base="
         + base.toShortString()
         + " solid:"
         + solidSkips
         + " repl:"
         + replaceSkips
         + " gate[abv:"
         + gateAbove
         + " flr:"
         + gateFloor
         + " bhd:"
         + gateBehind
         + " col:"
         + gateColumnHold
         + " veto:"
         + gateRiseVeto
         + " lock:"
         + gateRowLock
         + " hgt:"
         + gateHeight
         + " dead:"
         + gateDead
         + " blw:"
         + gateBelow
         + "] null:"
         + planNulls
         + (this.planFailure.isEmpty() ? "" : " [" + this.planFailure + "]")
         + (firstPlanDetail == null ? "" : " first=" + firstPlanDetail);
      return null;
   }

   static List<BlockPos> cachedNeighbourOffsets() {
      return NEIGHBOUR_OFFSETS;
   }

   static double faceAlignment(Direction face, Vec3 lane) {
      if (face != null && lane != null) {
         double length = Math.sqrt(lane.x * lane.x + lane.z * lane.z);
         return length < 1.0E-6 ? 0.0 : (face.getStepX() * lane.x + face.getStepZ() * lane.z) / length;
      } else {
         return 0.0;
      }
   }

   private static boolean isDiagonal(Vec3 direction) {
      return direction != null && axisSign(direction.x) != 0 && axisSign(direction.z) != 0;
   }

   private static int axisSign(double component) {
      if (component > 1.0E-6) {
         return 1;
      } else {
         return component < -1.0E-6 ? -1 : 0;
      }
   }

   private List<BlockPos> orderCandidates(List<BlockPos> source, BlockPos base, Vec3 predictedPosition, ScaffoldModule.MotionVectorPair optimalLine) {
      List<BlockPos> ordered = new ArrayList<>(source);
      ordered.sort(
         Comparator.<BlockPos>comparingInt(ScaffoldModule::nonZeroRank)
            .thenComparingDouble(offset -> this.candidateDistance(base.offset(offset), predictedPosition, optimalLine))
            .thenComparingDouble(offset -> offset.distSqr(BlockPos.ZERO))
            .thenComparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getX)
            .thenComparingInt(Vec3i::getZ)
      );
      return ordered;
   }

   static int nonZeroRank(BlockPos offset) {
      return BlockPos.ZERO.equals(offset) ? 0 : 1;
   }

   private double candidateDistance(BlockPos pos, Vec3 predictedPosition, ScaffoldModule.MotionVectorPair optimalLine) {
      VoxelShape shape = MC.level.getBlockState(pos).getShape(MC.level, pos, CollisionContext.of(MC.player));
      if (shape.isEmpty()) {
         return optimalLine == null ? Vec3.atCenterOf(pos).distanceToSqr(predictedPosition) : distanceSqToLine(optimalLine, Vec3.atCenterOf(pos));
      }

      double best = 1.0 / 0.0;

      for (AABB local : shape.toAabbs()) {
         AABB box = local.move(pos);
         double distance = optimalLine == null ? distanceSqToBox(predictedPosition, box) : lineBoxDistance(optimalLine, box);
         best = Math.min(best, distance);
      }

      return best;
   }

   private ScaffoldModule.PlannedPlacement planPlacement(
      BlockPos candidate, Vec3 plannedPosition, boolean replaceExisting, Vec3 laneDirection, boolean jumping
   ) {
      return this.planPlacement(candidate, plannedPosition, replaceExisting, laneDirection, jumping, null);
   }

   private ScaffoldModule.PlannedPlacement planPlacement(
      BlockPos candidate, Vec3 plannedPosition, boolean replaceExisting, Vec3 laneDirection, boolean jumping, Direction onlyFace
   ) {
      return this.planPlacement(candidate, plannedPosition, replaceExisting, laneDirection, jumping, onlyFace, false);
   }

   private ScaffoldModule.PlannedPlacement planPlacement(
      BlockPos candidate, Vec3 plannedPosition, boolean replaceExisting, Vec3 laneDirection, boolean jumping, Direction onlyFace, boolean planeLead
   ) {
      ScaffoldModule.SupportCandidate bestPlan = null;
      double bestFaceAngle = 1.0 / 0.0;
      this.planDetail.setLength(0);
      if (!replaceExisting && MC.player != null) {
         boolean catchMode = this.push(candidate);
         if (!canCatchOn(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), candidate, catchMode)) {
            this.planFailure = "inside-player";
            this.planDetail.append("ip:").append(catchDebug(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), candidate, catchMode));
            return null;
         }
      }

      Vec3 aimPosition = aimOrigin(plannedPosition, MC.player.position(), this.flags_Z());
      Vec3 aimEye = aimPosition.add(0.0, MC.player.getEyeHeight(), 0.0);
      Vec3 selectionPosition = predictedOrAim(plannedPosition, aimPosition);
      Vec3 selectionEye = selectionPosition.add(0.0, MC.player.getEyeHeight(), 0.0);
      Vec3 planeLeadEye = null;
      if (planeLead && this.flags_Z() && !MC.player.onGround()) {
         Vec3 travel = MC.player.getDeltaMovement();
         planeLeadEye = aimEye.add(travel.x * 4.0, 0.0, travel.z * 4.0);
      }

      RotationMath.Rotation from = this.flags_Z() ? this.placeAimRotation() : this.lastSentRotation();
      boolean pinnable = this.flags_Z() && MC.player != null;
      Vec3 pinEye = pinnable ? MC.player.getEyePosition().add(this.leadMotion()) : null;
      Vec3 pinLead = pinnable ? this.leadMotion() : Vec3.ZERO;
      float pinYaw = pinnable ? this.debouncedLaneYaw() : 0.0F;
      boolean heldPinStillPins = pinnable
         && this.pinFace != null
         && isPinnedCell(this.pinSupport, this.pinFace, candidate)
         && init_Z_Vec3_2(pinEye, pinLead, this.pinSupport, this.pinFace, pinYaw, true, 2);
      boolean bestFacePins = false;
      ScaffoldModule.FaceHitTarget bestSample = null;
      ScaffoldModule.RowLockBounds visibilityLock = this.flags_Z() ? this.rowLockBounds() : null;
      boolean lockPinnedFace = visibilityLock != null && onlyFace != null && onlyFace == visibilityLock.init(candidate);
      boolean requireExactVisibility = !lockPinnedFace && write(this.flags_Z(), this.edgeGateActive, MC.player.onGround(), jumping);
      double visibilityReach = requireExactVisibility ? Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange()) : 0.0;

      for (Direction face : Direction.values()) {
         if (onlyFace == null || face == onlyFace) {
            char faceCode = face.getName().charAt(0);
            BlockPos supportPos = replaceExisting ? candidate : candidate.relative(face.getOpposite());
            if (!MC.level.isOutsideBuildHeight(supportPos)) {
               BlockState supportState = MC.level.getBlockState(supportPos);
               if (!replaceExisting && supportState.canBeReplaced()) {
                  this.planFailure = "no-support(" + face + ")";
                  this.planDetail.append(faceCode).append(":ns ");
               } else if (this.flags_Z()
                  && this.b_Z()
                  && face.getAxis().isHorizontal()
                  && laneDirection != null
                  && !isDiagonal(laneDirection)
                  && faceAlignment(face, laneDirection) <= -0.5) {
                  this.planFailure = "reach-around(" + face + ")";
                  this.planDetail.append(faceCode).append(":ra ");
               } else {
                  Vec3 faceCenter = Vec3.atCenterOf(supportPos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
                  Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
                  boolean leadPasses = planeLeadEye != null && planeLeadEye.subtract(faceCenter).dot(normal) >= 0.0;
                  if (!leadPasses && selectionEye.subtract(faceCenter).dot(normal) < 0.0 && aimEye.subtract(faceCenter).dot(normal) < 0.0) {
                     this.planFailure = "normal-side(" + face + ")";
                     this.planDetail.append(faceCode).append(":sd ");
                  } else if (this.flags_Z() && this.init_Z_BlockPos_Direction(supportPos, face)) {
                     this.planFailure = "self-occ(" + face + ")";
                     this.planDetail.append(faceCode).append(":so ");
                  } else {
                     boolean facePins = pinnable
                        && init_Z_Vec3_2(pinEye, pinLead, supportPos, face, pinYaw, this.isPinned(supportPos, face), 2);
                     if (facePins && close_Z_Z_Z(heldPinStillPins, this.isPinned(supportPos, face))) {
                        facePins = false;
                     }

                     RotationMath.Rotation faceRotation = RotationMath.lookAt(faceCenter, aimEye);
                     double faceAngle = angleBetween(from, faceRotation);
                     if (this.flags_Z()
                        && face.getAxis().isHorizontal()
                        && aimPosition.y > supportPos.getY() + 1.0 + 1.0
                        && this.staysBeyondPitchLimit(aimEye, supportPos, face, faceRotation.yaw())) {
                        this.planFailure = "pitch-cap(" + face + ")";
                        this.planDetail.append(faceCode).append(":pc ");
                     } else if (isBetterPin(facePins, faceAngle, bestFacePins, bestFaceAngle)) {
                        VoxelShape shape = supportState.getShape(MC.level, supportPos, CollisionContext.of(MC.player));
                        ScaffoldModule.FaceHitTarget planSample = null;

                        for (AABB localBox : shape.toAabbs()) {
                           ScaffoldModule.FaceRect sampledFace = ScaffoldModule.FaceRect.init(localBox, face);
                           ScaffoldModule.FaceRect searchFace = sampledFace;
                           if (searchFace.write().y >= 0.9) {
                              ScaffoldModule.FaceRect truncated = searchFace.init(0.6);
                              if (truncated.area() > 1.0E-9) {
                                 searchFace = truncated;
                              }
                           }

                           Vec3 point = this.init_Vec3_aI_DataUtil(searchFace, sampledFace, supportPos, aimEye, from, this.courseLine);
                           if (point == null) {
                              this.planFailure = "no-point(" + face + ")";
                              this.planDetail.append(faceCode).append(":np ");
                           } else {
                              if (requireExactVisibility) {
                                 Vec3 candidatePoint = point.add(supportPos.getX(), supportPos.getY(), supportPos.getZ());
                                 RotationMath.Rotation visibleRotation = RotationMath.lookAt(candidatePoint, aimEye);
                                 BlockHitResult visibleHit = this.raycastRotation(visibleRotation, visibilityReach);
                                 if (visibleHit == null || !visibleHit.getBlockPos().equals(supportPos) || visibleHit.getDirection() != face) {
                                    this.planFailure = "visibility(" + face + ")";
                                    this.planDetail.append(faceCode).append(":vs ");
                                    continue;
                                 }
                              }

                              ScaffoldModule.FaceHitTarget sample = new ScaffoldModule.FaceHitTarget(sampledFace, point, face);
                              if (planSample == null || compareFaceHits(sample, planSample) > 0) {
                                 planSample = sample;
                              }
                           }
                        }

                        if (planSample != null) {
                           bestFaceAngle = faceAngle;
                           bestFacePins = facePins;
                           bestPlan = new ScaffoldModule.SupportCandidate(supportPos.immutable(), face);
                           bestSample = planSample;
                        }
                     }
                  }
               }
            }
         }
      }

      if (bestPlan != null && bestSample != null) {
         Vec3 worldPoint = bestSample.point().add(bestPlan.support().getX(), bestPlan.support().getY(), bestPlan.support().getZ());
         ScaffoldModule.RowLockBounds goalLock = this.flags_Z() ? this.rowLockBounds() : null;
         Direction goalPinnedFace = goalLock == null ? null : goalLock.init(candidate);
         RotationMath.Rotation rotation;
         if (this.flags_Z() && MC.player != null && !MC.player.onGround()) {
            Vec3 airEye = MC.player.getEyePosition().add(this.leadMotion());
            float airPitch = 0.0F / 0.0F;
            float airYaw = pinYaw;
            if (bestFacePins && bestPlan.face().getAxis().isHorizontal()) {
               this.aimDecision = "air-pin";
               if (this.laneOctant() != -1) {
                  float landing = load_F_Vec3(
                     MC.player.getEyePosition(), this.leadMotion(), bestPlan.support(), bestPlan.face(), airYaw, this.laneYaw()
                  );
                  if (!Float.isNaN(landing)) {
                     airYaw = landing;
                     this.aimDecision = "air-land";
                  }

                  float flip = init_F_Vec3_3(MC.player.getEyePosition(), this.leadMotion(), bestPlan.support(), bestPlan.face(), airYaw);
                  if (!Float.isNaN(flip)) {
                     airYaw = flip;
                     this.aimDecision = "air-flip";
                  }
               }

               airPitch = init_F_Vec3(airEye, MC.player.getEyePosition(), bestPlan.support(), bestPlan.face(), airYaw, 89.5F);
            } else if (bestPlan.face() == Direction.UP) {
               airPitch = init_F_Vec3(airEye, bestPlan.support(), pinYaw);
               if (Float.isNaN(airPitch)) {
                  airPitch = init_F_Vec3(MC.player.getEyePosition(), bestPlan.support(), pinYaw);
               }

               if (!Float.isNaN(airPitch)) {
                  this.aimDecision = "air-top";
               } else if (this.laneOctant() != -1) {
                  float topFlip = init_F_Vec3_3(MC.player.getEyePosition(), this.leadMotion(), bestPlan.support(), pinYaw);
                  if (!Float.isNaN(topFlip)) {
                     float flipPitch = init_F_Vec3(airEye, bestPlan.support(), topFlip);
                     if (Float.isNaN(flipPitch)) {
                        flipPitch = init_F_Vec3(MC.player.getEyePosition(), bestPlan.support(), topFlip);
                     }

                     if (!Float.isNaN(flipPitch)) {
                        airYaw = topFlip;
                        airPitch = flipPitch;
                        this.aimDecision = "air-flip";
                     }
                  }
               }
            }

            if (!Float.isNaN(airPitch)) {
               rotation = new RotationMath.Rotation(airYaw, airPitch);
            } else {
               if (bestPlan.face().getAxis().isHorizontal()) {
                  double lowY = bestPlan.support().getY() + bestSample.face().read().y + 0.15;
                  if (lowY < worldPoint.y) {
                     worldPoint = new Vec3(worldPoint.x, lowY, worldPoint.z);
                  }
               }

               this.aimDecision = "air-direct";
               rotation = this.init_ce_DataUtil_ce_DataUtil_Vec3(RotationMath.lookAt(worldPoint, MC.player.getEyePosition()), worldPoint);
               double bearingRun = Math.hypot(worldPoint.x - MC.player.getEyePosition().x, worldPoint.z - MC.player.getEyePosition().z);
               float chosen = bestPlan.face().getAxis().isHorizontal() && bearingRun >= 0.75 ? this.sync(rotation.yaw()) : pinYaw;
               if (this.laneOctant() != -1) {
                  float landing = load_F_Vec3(
                     MC.player.getEyePosition(), this.leadMotion(), bestPlan.support(), bestPlan.face(), chosen, this.laneYaw()
                  );
                  if (!Float.isNaN(landing)) {
                     chosen = landing;
                     this.aimDecision = "air-land";
                  }

                  float flip = init_F_Vec3_3(MC.player.getEyePosition(), this.leadMotion(), bestPlan.support(), bestPlan.face(), chosen);
                  if (!Float.isNaN(flip)) {
                     chosen = flip;
                     this.aimDecision = "air-flip";
                     float flipPitch = init_F_Vec3(airEye, MC.player.getEyePosition(), bestPlan.support(), bestPlan.face(), chosen, 89.5F);
                     if (!Float.isNaN(flipPitch)) {
                        rotation = new RotationMath.Rotation(rotation.yaw(), flipPitch);
                     }
                  }
               }

               rotation = new RotationMath.Rotation(chosen, rotation.pitch());
            }
         } else if (this.flags_Z() && MC.player != null && bestPlan.face().getAxis().isHorizontal()) {
            Vec3 leadEye = pinEye == null ? MC.player.getEyePosition().add(this.leadMotion()) : pinEye;
            boolean postured = false;
            float goalYaw = 0.0F;
            if (bestFacePins) {
               this.aimDecision = MC.player.onGround() ? "lead-pin" : "air-pin";
               goalYaw = pinYaw;
               postured = true;
            }

            if (!postured) {
               Vec3 goalEye = this.init_Vec3_BlockPos(bestPlan.support(), bestPlan.face(), selectionEye, laneDirection);
               RotationMath.Rotation pointGoal = RotationMath.lookAt(worldPoint, goalEye);
               goalYaw = this.sync(this.init_ce_DataUtil_ce_DataUtil_Vec3(pointGoal, worldPoint).yaw());
            }

            rotation = new RotationMath.Rotation(
               goalYaw, init_F_Vec3(MC.player.getEyePosition(), leadEye, bestPlan.support(), bestPlan.face(), goalYaw, 89.3F)
            );
         } else if (bestFacePins
            && bestPlan.face() == Direction.UP
            && !Float.isNaN(init_F_Vec3(pinEye == null ? MC.player.getEyePosition().add(this.leadMotion()) : pinEye, bestPlan.support(), pinYaw))) {
            this.aimDecision = MC.player.onGround() ? "lead-top" : "air-top";
            rotation = new RotationMath.Rotation(
               pinYaw, init_F_Vec3(pinEye == null ? MC.player.getEyePosition().add(this.leadMotion()) : pinEye, bestPlan.support(), pinYaw)
            );
         } else {
            Vec3 goalEye = this.init_Vec3_BlockPos(bestPlan.support(), bestPlan.face(), selectionEye, laneDirection);
            rotation = RotationMath.lookAt(worldPoint, goalEye);
            rotation = this.init_ce_DataUtil_ce_DataUtil_Vec3(rotation, worldPoint);
            if (goalPinnedFace != null) {
               float postureYaw = this.debouncedLaneYaw();
               if (bestPlan.face() == Direction.UP && MC.player != null) {
                  Vec3 topEye = pinEye == null ? MC.player.getEyePosition().add(this.leadMotion()) : pinEye;
                  float topPitch = init_F_Vec3(topEye, bestPlan.support(), postureYaw);
                  if (Float.isNaN(topPitch)) {
                     this.planDetail.append("u:tx ");
                     return null;
                  }

                  this.aimDecision = MC.player.onGround() ? "lead-top" : "air-top";
                  rotation = new RotationMath.Rotation(postureYaw, topPitch);
               } else {
                  rotation = new RotationMath.Rotation(postureYaw, rotation.pitch());
               }
            } else {
               rotation = new RotationMath.Rotation(this.sync(rotation.yaw()), rotation.pitch());
            }
         }

         if (this.flags_Z()) {
            Vec3 clampEye = MC.player.getEyePosition();
            Vec3 clampLead = clampEye.add(this.leadMotion());
            double[] window = MC.player.onGround()
               ? init_DArr_Vec3_2(clampEye, clampLead, bestPlan.support(), bestPlan.face(), rotation.yaw())
               : init_DArr_Vec3_2(clampLead, clampEye, bestPlan.support(), bestPlan.face(), rotation.yaw());
            Vec3 publishedEye = lastPublishedEye;
            if (window != null && publishedEye != null) {
               double[] published = init_DArr_Vec3_2(publishedEye, bestPlan.support(), bestPlan.face(), rotation.yaw());
               if (published != null && Math.max(window[0], published[0]) <= Math.min(window[1], published[1])) {
                  window = new double[]{Math.max(window[0], published[0]), Math.min(window[1], published[1]), window[2]};
               }
            }

            double windowLow = window == null ? 0.0 / 0.0 : window[0];
            double windowHigh = window == null ? 0.0 / 0.0 : window[1];
            float goalPitch = init_F_F(rotation.pitch(), windowLow, windowHigh);
            float dodged = this.init_F_F(
               goalPitch,
               windowLow,
               bestPlan.face(),
               MC.player.onGround() ? clampEye : clampLead,
               this.leadMotion(),
               rotation.yaw(),
               bestPlan.support()
            );
            double flickHigh = dodged < goalPitch ? (Double.isNaN(windowHigh) ? dodged : Math.min(windowHigh, dodged)) : windowHigh;
            goalPitch = this.init_F_F(dodged, windowLow, flickHigh, bestPlan.face(), candidate.immutable());
            rotation = new RotationMath.Rotation(rotation.yaw(), goalPitch);
         }

         BlockHitResult plannedHit = new BlockHitResult(worldPoint, bestPlan.face(), bestPlan.support(), false);
         return new ScaffoldModule.PlannedPlacement(
            bestPlan.support(),
            candidate.immutable(),
            bestPlan.face(),
            plannedHit,
            rotation,
            bestPlan.support().getY() + bestSample.face().read().y
         );
      } else {
         return null;
      }
   }

   private boolean f_Z() {
      return this.wantsJump() || this.reservationActive && MC.player != null && !MC.player.onGround();
   }

   private boolean open(BlockPos pos) {
      return MC.level != null && !MC.level.isOutsideBuildHeight(pos) ? this.hasCentreSupport(MC.level.getBlockState(pos), pos) : false;
   }

   private boolean poll(BlockPos pos) {
      if (!this.open(pos)) {
         this.predictedCells.remove(pos);
         return false;
      } else {
         return !this.predictedCells.containsKey(pos);
      }
   }

   private boolean prunePredictedCells() {
      Iterator<BlockPos> iterator = this.predictedCells.keySet().iterator();

      while (iterator.hasNext()) {
         BlockPos cell = iterator.next();
         if (!this.open(cell)) {
            iterator.remove();
         }
      }

      return !this.predictedCells.isEmpty();
   }

   static boolean hasLaneSupportAhead(int laneStep, BlockPos below, Predicate<BlockPos> solid) {
      return hasLaneSupportAhead(laneStep, below, false, solid);
   }

   static boolean hasLaneSupportAhead(int laneStep, BlockPos below, boolean aheadRise, Predicate<BlockPos> solid) {
      if (laneStep != -1 && below != null) {
         double yaw = Math.toRadians(octantToYaw(laneStep));
         int dx = (int)Math.round(-Math.sin(yaw));
         int dz = (int)Math.round(Math.cos(yaw));
         if (dx == 0 && dz == 0) {
            return true;
         } else if (!solid.test(below.offset(dx, 0, dz))) {
            return false;
         } else if (dx != 0 && dz != 0) {
            return aheadRise ? true : solid.test(below.offset(dx, 0, 0)) || solid.test(below.offset(0, 0, dz));
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   static boolean init_Z_Z(boolean preferHeight, int candidateY, int footingRow) {
      return preferHeight && candidateY > footingRow;
   }

   private void trackFootingRow() {
      if (MC.player != null && MC.player.onGround()) {
         int row = this.measureFootingRow();
         if (this.groundedFootingRow != -2147483648 && row > this.groundedFootingRow) {
            this.footingRiseTick = AutismSessionState.getInstance().getClientTick();
         } else if (this.groundedFootingRow != -2147483648 && row < this.groundedFootingRow) {
            this.footingRiseTick = -2147483648;
         }

         this.groundedFootingRow = row;
         this.airborneBuiltRow = -2147483648;
      }
   }

   private boolean h_Z() {
      return !this.k() ? false : this.groundedFootingRow == -2147483648 || MC.player.getY() < this.groundedFootingRow + 1.0;
   }

   static boolean fallsBelowRow(double feet, double vy, int footingRow, int lookahead) {
      double plane = footingRow + 1.0;
      if (feet < plane) {
         return true;
      }

      for (int tick = 0; tick < lookahead; tick++) {
         feet += vy;
         vy = (vy - 0.08) * 0.98;
         if (feet < plane) {
            return true;
         }
      }

      return false;
   }

   private boolean i() {
      if (this.h_Z()) {
         return true;
      } else {
         return !this.k() ? false : fallsBelowRow(MC.player.getY(), MC.player.getDeltaMovement().y, this.groundedFootingRow, 2);
      }
   }

   private boolean push(BlockPos candidate) {
      if (!this.flags_Z() || MC.player == null || MC.player.onGround()) {
         return false;
      }

      if (MC.player.getDeltaMovement().y >= 0.0) {
         return false;
      }

      if (candidate.getY() + 1.0 > MC.player.getBoundingBox().minY) {
         return false;
      }

      if (this.h_Z()) {
         return true;
      }

      Vec3 landing = projectLandingPoint(MC.player.position(), MC.player.getDeltaMovement(), candidate.getY() + 1.0, 2);
      return landing != null && Mth.floor(landing.x) == candidate.getX() && Mth.floor(landing.z) == candidate.getZ();
   }

   private static double columnOverlap(double centre, int cell) {
      double low = Math.max(centre - 0.3, cell);
      double high = Math.min(centre + 0.3, cell + 1.0);
      return Math.max(0.0, high - low);
   }

   static boolean isServedThisTick(int servedTick, int now, BlockPos held, BlockPos noted) {
      return servedTick == now && held != null && held.equals(noted);
   }

   private void holdPlacement(ScaffoldModule.PlannedPlacement target) {
      if (target != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (!isServedThisTick(this.heldServedTick, tick, this.heldPlacement == null ? null : this.heldPlacement.load(), target.load())) {
            this.heldPlacement = target;
            this.heldPlacementTick = tick;
         }
      }
   }

   static boolean isHeldPlacementValid(int heldTick, int now, int maxTicks, boolean cellOpen, boolean supported) {
      if (heldTick < 0) {
         return false;
      }

      int age = now - heldTick;
      return age >= 0 && age <= maxTicks && cellOpen && supported;
   }

   static boolean read_Z_I_I(int heldRow, int footingRow) {
      return footingRow == -2147483648 || heldRow >= footingRow;
   }

   static int highestRow(int footingRow, int lastPlacedRow) {
      if (footingRow == -2147483648) {
         return lastPlacedRow;
      } else {
         return lastPlacedRow == -2147483648 ? footingRow : Math.max(footingRow, lastPlacedRow);
      }
   }

   static boolean init_Z_Vec3_8(Vec3 position, Vec3 laneDirection, BlockPos placed, boolean descending) {
      return !descending && init_Z_Vec3_6(position, laneDirection, placed, 0.8);
   }

   private ScaffoldModule.PlannedPlacement read_aI_TableIndex() {
      ScaffoldModule.PlannedPlacement held = this.heldPlacement;
      if (held != null && MC.level != null) {
         BlockPos placed = held.load();
         BlockPos support = held.support();
         if (!MC.level.isOutsideBuildHeight(placed) && !MC.level.isOutsideBuildHeight(support)) {
            BlockPos lastPlaced = this.recentPlaced.peekLast();
            if (!read_Z_I_I(placed.getY(), highestRow(this.footingRow(), lastPlaced == null ? -2147483648 : lastPlaced.getY()))) {
               this.heldPlacement = null;
               return null;
            } else if (MC.player != null
               && init_Z_Vec3_8(MC.player.position(), this.courseLine == null ? null : this.courseLine.load(), placed, this.h_Z())) {
               this.heldPlacement = null;
               return null;
            } else if (MC.player != null && !canCatchOn(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), placed, this.push(placed))) {
               this.heldPlacement = null;
               return null;
            } else if (this.sync(placed)) {
               this.heldPlacement = null;
               return null;
            } else if (this.load_Z_aI_TableIndex(held)) {
               this.heldPlacement = null;
               return null;
            } else if (MC.player != null && !isLegSighted(held, MC.player.getEyePosition(), this.leadMotion())) {
               this.heldPlacement = null;
               return null;
            } else {
               boolean cellOpen = MC.level.getBlockState(placed).canBeReplaced();
               boolean supported = this.hasCentreSupport(MC.level.getBlockState(support), support);
               if (!isHeldPlacementValid(this.heldPlacementTick, AutismSessionState.getInstance().getClientTick(), 3, cellOpen, supported)) {
                  this.heldPlacement = null;
                  return null;
               } else {
                  this.frame_Z = true;
                  this.heldServedTick = AutismSessionState.getInstance().getClientTick();
                  ScaffoldModule.PlannedPlacement resolved = this.write_aI_TableIndex_aI_TableIndex(held);
                  return resolved == null ? held : resolved;
               }
            }
         } else {
            this.heldPlacement = null;
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean init_Z_BlockPos_3(BlockPos placed, BlockPos support, BlockPos upFaceSwapCell) {
      return placed.getY() != support.getY() && !placed.equals(upFaceSwapCell);
   }

   static boolean init_Z_Direction(Direction heldFace, boolean replaceExisting, boolean fullCubeBelow) {
      return heldFace != null && heldFace.getAxis().isHorizontal() ? replaceExisting || fullCubeBelow : false;
   }

   private ScaffoldModule.PlannedPlacement write_aI_TableIndex_aI_TableIndex(ScaffoldModule.PlannedPlacement held) {
      if (MC.player != null && MC.level != null) {
         BlockPos placed = held.load();
         BlockState state = MC.level.getBlockState(placed);
         boolean replaceExisting = !state.isAir() && state.getFluidState().isEmpty();
         String planFail = this.planFailure;
         ScaffoldModule.PlannedPlacement upgraded = this.read_aI_TableIndex_aI_TableIndex(held);
         if (upgraded != held) {
            this.heldPlacement = upgraded;
            return upgraded;
         } else {
            ScaffoldModule.PlannedPlacement fresh = this.planPlacement(
               placed,
               this.predictedPosition(this.courseLine),
               replaceExisting,
               this.courseLine == null ? null : this.courseLine.load(),
               this.f_Z(),
               held.face()
            );
            this.planFailure = planFail;
            return fresh != null && fresh.load().equals(placed) && fresh.face() == held.face() ? fresh : null;
         }
      } else {
         return null;
      }
   }

   static boolean init_Z_Vec3_6(Vec3 position, Vec3 laneDirection, BlockPos cell, double margin) {
      if (position != null && laneDirection != null && cell != null) {
         double lx = laneDirection.x;
         double lz = laneDirection.z;
         double length = Math.sqrt(lx * lx + lz * lz);
         if (length <= 1.0E-6) {
            return false;
         }

         double along = (cell.getX() + 0.5 - position.x) * (lx / length) + (cell.getZ() + 0.5 - position.z) * (lz / length);
         return along < -margin;
      } else {
         return false;
      }
   }

   static boolean overlapsColumn(Vec3 position, BlockPos column) {
      return position != null && column != null && columnOverlap(position.x, column.getX()) > 0.0 && columnOverlap(position.z, column.getZ()) > 0.0;
   }

   static boolean init_Z_Vec3_5(Vec3 position, Vec3 velocity, BlockPos column, int leadTicks, double minOverlap) {
      if (position != null && column != null) {
         double x = position.x + (velocity == null ? 0.0 : velocity.x) * leadTicks;
         double z = position.z + (velocity == null ? 0.0 : velocity.z) * leadTicks;
         return columnOverlap(x, column.getX()) >= minOverlap && columnOverlap(z, column.getZ()) >= minOverlap;
      } else {
         return true;
      }
   }

   private boolean isPinned(BlockPos support, Direction face) {
      return this.pinFace == face && support != null && support.equals(this.pinSupport);
   }

   static boolean isPinnedCell(BlockPos pinSupport, Direction pinFace, BlockPos candidate) {
      return pinSupport != null && pinFace != null && pinSupport.relative(pinFace).equals(candidate);
   }

   static boolean close_Z_Z_Z(boolean heldPinStillPins, boolean heldForThisFace) {
      return heldPinStillPins && !heldForThisFace;
   }

   static boolean init_Z_Vec3_2(Vec3 pinEye, BlockPos support, Direction face, float pinYaw, boolean holding) {
      return face.getAxis().isHorizontal()
         ? load_Z_Vec3(pinEye, support, face, pinYaw, holding)
         : face == Direction.UP && init_Z_Vec3_2(pinEye, support, pinYaw, holding);
   }

   static boolean isBetterPin(boolean pins, double angle, boolean bestPins, double bestAngle) {
      return pins != bestPins ? pins : angle < bestAngle;
   }

   private void write_V_aI_TableIndex(ScaffoldModule.PlannedPlacement target) {
      this.f_Z_D = target != null && MC.player != null
         ? init_D_Vec3_3(MC.player.getEyePosition().add(this.leadMotion()), target.support(), target.face(), this.debouncedLaneYaw())
         : 0.0 / 0.0;
      if (target != null && MC.player != null && this.flags_Z()) {
         if (!this.tick(target)
            && init_Z_Vec3_2(
               MC.player.getEyePosition().add(this.leadMotion()),
               this.leadMotion(),
               target.support(),
               target.face(),
               this.debouncedLaneYaw(),
               this.isPinned(target.support(), target.face()),
               2
            )) {
            this.pinSupport = target.support();
            this.pinFace = target.face();
         } else {
            this.pinSupport = null;
            this.pinFace = null;
         }
      } else {
         this.pinSupport = null;
         this.pinFace = null;
      }
   }

   private boolean tick(ScaffoldModule.PlannedPlacement target) {
      if (target == null) {
         this.missTargetSupport = null;
         this.missTargetFace = null;
         this.missStreakCount = 0;
         return false;
      } else {
         boolean same = this.missTargetFace == target.face() && target.support().equals(this.missTargetSupport);
         this.missTargetSupport = target.support();
         this.missTargetFace = target.face();
         this.missStreakCount = missStreak(this.missStreakCount, same, this.clickLanded);
         return this.missStreakCount > 14;
      }
   }

   static int missStreak(int previous, boolean sameTarget, boolean clickLanded) {
      if (clickLanded) {
         return 0;
      } else {
         return sameTarget ? previous + 1 : 1;
      }
   }

   private RotationMath.Rotation init_ce_DataUtil_ce_DataUtil_Vec3(RotationMath.Rotation pushGoal, Vec3 worldPoint) {
      if (this.flags_Z() && MC.player != null && !MC.player.onGround()) {
         Vec3 eye = MC.player.getEyePosition();
         RotationMath.Rotation direct = RotationMath.lookAt(worldPoint, eye);
         RotationMath.Rotation predicted = RotationMath.lookAt(worldPoint, eye.add(MC.player.getDeltaMovement()));
         return init_ce_DataUtil_ce_DataUtil(pushGoal, direct, predicted);
      } else {
         return pushGoal;
      }
   }

   static RotationMath.Rotation init_ce_DataUtil_ce_DataUtil(RotationMath.Rotation pushGoal, RotationMath.Rotation direct, RotationMath.Rotation predicted) {
      return Math.abs(RotationMath.angleDelta(predicted.yaw(), pushGoal.yaw())) > 100.0F
         ? pushGoal
         : new RotationMath.Rotation(predicted.yaw(), Math.min(predicted.pitch(), 90.0F));
   }

   private boolean placeTarget(ScaffoldModule.PlannedPlacement target, InteractionHand hand, ItemStack stack) {
      return this.placeTarget(target, hand, stack, true, true);
   }

   private boolean placeTarget(
      ScaffoldModule.PlannedPlacement target, InteractionHand hand, ItemStack stack, boolean restoreClientRotation, boolean sendPlacementRotation
   ) {
      if (AutismLiteVariant.enabled() || !DangerModule.read_Z_22() && !ClutchModule.read_Z_20()) {
         RotationMath.Rotation rotation = target.rotation();
         ScaffoldModule.MotionVectorPair placementLine = this.courseLine;
         Vec3 previousFallOff = this.fallOffPoint(placementLine);
         float clientYaw = MC.player.getYRot();
         float clientPitch = MC.player.getXRot();
         if (sendPlacementRotation && !sameRotation(rotation, this.lastSentRotation())) {
            this.sendRotationPacket(rotation);
         }

         int oldCount = stack.getCount();

         try {
            InteractionResult result = MC.gameMode.useItemOn(MC.player, hand, target.hit());
            if (result instanceof Fail) {
               return false;
            }

            if (result instanceof Pass) {
               if (stack.isEmpty()) {
                  return false;
               }

               if (MC.gameMode.useItem(MC.player, hand) instanceof Success success) {
                  if (success.swingSource() == SwingSource.PREDICTED) {
                     Compat263.swing(MC.player, hand);
                  }

                  Compat263.getItemInHandRenderer(MC.gameRenderer).itemUsed(hand);
               }

               return false;
            } else if (!result.consumesAction()) {
               return false;
            } else if (result instanceof Success success && success.swingSource() != SwingSource.PREDICTED) {
               return true;
            } else {
               Compat263.swing(MC.player, hand);
               autismclient.util.ClickRateTracker.recordRightClick();
               PlacedBlockTrailRenderer.init(target.load());
               this.notePlaced(target.load(), placementLine, previousFallOff);
               boolean wasStackUsed = !stack.isEmpty() && (stack.getCount() != oldCount || MC.player.hasInfiniteMaterials());
               if (wasStackUsed) {
                  Compat263.getItemInHandRenderer(MC.gameRenderer).itemUsed(hand);
               }

               return true;
            }
         } finally {
            if (restoreClientRotation) {
               RotationMath.Rotation clientRotation = new RotationMath.Rotation(clientYaw, clientPitch);
               if (!sameRotation(this.lastSentRotation(), clientRotation)) {
                  this.sendRotationPacket(clientRotation);
               }
            }
         }
      } else {
         return false;
      }
   }

   static Vec3 aimOrigin(Vec3 predictedPosition, Vec3 actualPosition, boolean edgeHoldActive) {
      if (predictedPosition == null) {
         return actualPosition;
      } else if (actualPosition == null) {
         return predictedPosition;
      } else {
         return edgeHoldActive ? actualPosition : predictedPosition;
      }
   }

   static Vec3 predictedOrAim(Vec3 predictedPosition, Vec3 aimPosition) {
      return predictedPosition == null ? aimPosition : predictedPosition;
   }

   private void notePlaced(BlockPos placed, ScaffoldModule.MotionVectorPair line, Vec3 previousFallOff) {
      this.lockedLine = null;
      BlockPos immutable = placed.immutable();
      if (!immutable.equals(this.recentPlaced.peekLast())) {
         while (this.recentPlaced.size() >= 4) {
            this.recentPlaced.removeFirst();
         }

         this.recentPlaced.addLast(immutable);
      }

      if (line != null && previousFallOff != null) {
         float angle = (float)Math.atan2(line.load().z, line.load().x);
         Vec3 unrotatedOffset = MC.player.position().subtract(previousFallOff).yRot(angle);
         this.recentOffsets.addLast(unrotatedOffset);

         while (this.recentOffsets.size() > 4) {
            this.recentOffsets.removeFirst();
         }
      }
   }

   private void sendRotationPacket(RotationMath.Rotation rotation) {
      if (rotation != null && MC.getConnection() != null && MC.player != null) {
         this.sentRotation = rotation;
         MC.getConnection()
            .send(
               new PosRot(
                  MC.player.getX(), MC.player.getY(), MC.player.getZ(), rotation.yaw(), rotation.pitch(), MC.player.onGround(), MC.player.horizontalCollision
               )
            );
      }
   }

   private static boolean sameRotation(RotationMath.Rotation first, RotationMath.Rotation second) {
      return first != null && second != null && Float.compare(first.yaw(), second.yaw()) == 0 && Float.compare(first.pitch(), second.pitch()) == 0;
   }

   private BlockHitResult raycastRotation(RotationMath.Rotation rotation, double reach) {
      return rotation == null ? null : raycast(MC.player.getEyePosition(), rotation, reach, MC.level, MC.player);
   }

   static BlockHitResult raycast(Vec3 eye, RotationMath.Rotation rotation, double reach, BlockGetter world, Entity entity) {
      Vec3 look = lookVector(rotation);
      Vec3 end = eye.add(look.scale(reach));
      HitResult result = world.clip(
         new ClipContext(
            eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, entity == null ? CollisionContext.empty() : CollisionContext.of(entity)
         )
      );
      return result instanceof BlockHitResult blockHit && result.getType() == Type.BLOCK ? blockHit : null;
   }

   private ItemStack placeableStack() {
      ItemStack main = MC.player.getMainHandItem();
      if (this.isPlaceableBlock(main)) {
         return main;
      }

      int hotbar = this.bestBlockSlot();
      return hotbar >= 0 ? MC.player.getInventory().getItem(hotbar) : ItemStack.EMPTY;
   }

   private BlockPos clickedPlacePos(BlockHitResult hit, ItemStack stack) {
      if (hit == null) {
         return null;
      } else {
         return MC.player != null && stack != null && !stack.isEmpty()
            ? new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, hit).getClickedPos().immutable()
            : hit.getBlockPos().relative(hit.getDirection());
      }
   }

   private boolean hitPlacesPending(BlockHitResult hit, ScaffoldModule.PlannedPlacement pending) {
      return hit != null && pending != null ? pending.load().equals(this.clickedPlacePos(hit, this.placeableStack())) : false;
   }

   static AABB supportBox(BlockPos support) {
      if (support == null) {
         return null;
      }

      if (MC != null && MC.level != null) {
         BlockState state = MC.level.getBlockState(support);
         if (support.equals(cachedSupportPos) && state == cachedSupportState) {
            return cachedSupportBox;
         }

         AABB box = unitBox(support);
         VoxelShape shape = state.getShape(MC.level, support, MC.player == null ? CollisionContext.empty() : CollisionContext.of(MC.player));
         if (!shape.isEmpty()) {
            box = shape.bounds().move(support.getX(), support.getY(), support.getZ());
         }

         cachedSupportPos = support.immutable();
         cachedSupportState = state;
         cachedSupportBox = box;
         return box;
      } else {
         return unitBox(support);
      }
   }

   static Vec3 faceCentre(AABB support, Direction face) {
      Vec3 c = support.getCenter();
      Vec3 var10000;
      switch (face) {
         case EAST:
            var10000 = new Vec3(support.maxX, c.y, c.z);
            break;
         case WEST:
            var10000 = new Vec3(support.minX, c.y, c.z);
            break;
         case SOUTH:
            var10000 = new Vec3(c.x, c.y, support.maxZ);
            break;
         case NORTH:
            var10000 = new Vec3(c.x, c.y, support.minZ);
            break;
         case DOWN:
            var10000 = new Vec3(c.x, support.minY, c.z);
            break;
         case UP:
            var10000 = new Vec3(c.x, support.maxY, c.z);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static AABB unitBox(BlockPos support) {
      return new AABB(support.getX(), support.getY(), support.getZ(), support.getX() + 1.0, support.getY() + 1.0, support.getZ() + 1.0);
   }

   private boolean canReplaceWith(BlockState state, BlockPos pos, ItemStack stack) {
      BlockPlaceContext context = new BlockPlaceContext(
         MC.player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atLowerCornerOf(pos), Direction.UP, pos, false)
      );
      return state.canBeReplaced(context);
   }

   private InteractionHand placeHand() {
      ItemStack main = MC.player.getMainHandItem();
      int selected = MC.player.getInventory().getSelectedSlot();
      int remaining = this.isTellyOrEagleMode() ? this.trackedSlotCount(selected) : main.getCount();
      int freshSlot = -1;
      if (this.isTellyOrEagleMode() && this.turnPhase != ScaffoldModule.TurnCyclePhase.PLACING && remaining < 8) {
         for (int candidate = 0; candidate < 9; candidate++) {
            if (candidate != selected && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(candidate, this.id())) {
               ItemStack stack = MC.player.getInventory().getItem(candidate);
               if (this.isPlaceableBlock(stack)
                  && this.trackedSlotCount(candidate) >= 8
                  && (freshSlot < 0 || this.compareBlockStacks(stack, MC.player.getInventory().getItem(freshSlot)) > 0)) {
                  freshSlot = candidate;
               }
            }
         }
      }

      if (this.isPlaceableBlock(main) && remaining > 0 && freshSlot < 0) {
         if (this.lastRequestedSlot == MC.player.getInventory().getSelectedSlot()) {
            this.lastRequestedSlot = -1;
         }

         return InteractionHand.MAIN_HAND;
      } else {
         int slot = freshSlot >= 0 ? freshSlot : this.bestBlockSlot();
         if (slot < 0) {
            return null;
         }

         if (selected == slot) {
            return InteractionHand.MAIN_HAND;
         }

         if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
            return null;
         }

         try {
            if (this.bool("switch-back") && this.switchBackSlot < 0) {
               this.switchBackSlot = selected;
            }

            this.lastRequestedSlot = slot;
            this.scaffoldSlot = slot;
            this.slotSwitched = true;
            autismclient.util.VirtualInputDispatcher.setSlot(slot);
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }

         return null;
      }
   }

   public static int load_I_I(int slot) {
      if (MC.player != null && MC.level != null && slot >= 0 && slot < 9) {
         return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold ? scaffold.trackedSlotCount(slot) : MC.player.getInventory().getItem(slot).getCount();
      } else {
         return 0;
      }
   }

   public static void init_V_I_I(int slot, int sequence) {
      if (MC.player != null
         && MC.level != null
         && slot >= 0
         && slot < 9
         && !MC.player.hasInfiniteMaterials()
         && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold) {
         scaffold.syncSlotCacheLevel();
         scaffold.slotRemaining[slot] = Math.max(0, scaffold.slotRemaining[slot] - 1);
         scaffold.slotUseTick[slot] = AutismSessionState.getInstance().getClientTick();
         scaffold.slotUseSequence[slot] = sequence;
      }
   }

   private int trackedSlotCount(int slot) {
      if (slot >= 0 && slot < 9) {
         this.syncSlotCacheLevel();
         ItemStack stack = MC.player.getInventory().getItem(slot);
         int visible = stack.isEmpty() ? 0 : stack.getCount();
         if (MC.player.hasInfiniteMaterials()) {
            this.slotItems[slot] = null;
            return availableCount(0, visible, false, true);
         }

         if (!stack.isEmpty() && this.slotItems[slot] != stack.getItem()) {
            this.slotItems[slot] = stack.getItem();
            this.slotRemaining[slot] = visible;
            this.slotUseSequence[slot] = -1;
            this.slotUseTick[slot] = -2147483648;
         }

         int tick = AutismSessionState.getInstance().getClientTick();
         boolean settled = isSlotUseSettled(this.slotUseSequence[slot], this.blockAckSequence, this.slotUseTick[slot], tick, autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs())
            && (this.useSlot != slot || !this.isPlacementInFlight());
         this.slotRemaining[slot] = availableCount(this.slotRemaining[slot], visible, settled);
         return this.slotRemaining[slot];
      } else {
         return 0;
      }
   }

   private void syncSlotCacheLevel() {
      if (this.slotCacheLevel != MC.level) {
         this.clearSlotCache();
         this.slotCacheLevel = MC.level;
      }
   }

   private void clearSlotCache() {
      this.slotCacheLevel = null;
      this.blockAckSequence = -2147483648;
      Arrays.fill(this.slotItems, null);
   }

   static boolean isSlotUseSettled(int sequence, int acknowledged, int useTick, int tick, int pingMs) {
      if (sequence == 0) {
         int echoTicks = Math.max(20, 4 + (int)Math.ceil(Math.max(0, pingMs) / 25.0));
         return useTick == -2147483648 || tick - useTick > echoTicks;
      } else {
         return useTick < tick - 1 && isAcknowledged(sequence, acknowledged);
      }
   }

   static int availableCount(int remaining, int visible, boolean serverCaughtUp) {
      return availableCount(remaining, visible, serverCaughtUp, false);
   }

   static int availableCount(int remaining, int visible, boolean serverCaughtUp, boolean infiniteMaterials) {
      if (infiniteMaterials) {
         return visible > 0 ? 2147483647 : 0;
      } else {
         return Math.max(0, serverCaughtUp ? visible : Math.min(remaining, visible));
      }
   }

   private int bestBlockSlot() {
      int best = this.bestBlockSlot(true);
      return best >= 0 ? best : this.bestBlockSlot(false);
   }

   private int bestBlockSlot(boolean requireReserve) {
      int best = -1;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            int available = this.isTellyOrEagleMode() ? this.trackedSlotCount(slot) : stack.getCount();
            if (this.isPlaceableBlock(stack)
               && available > 0
               && (!requireReserve || available > 1)
               && (best < 0 || this.compareBlockStacks(stack, MC.player.getInventory().getItem(best)) > 0)) {
               best = slot;
            }
         }
      }

      return best;
   }

   private int compareBlockStacks(ItemStack first, ItemStack second) {
      Block firstBlock = ((BlockItem)first.getItem()).getBlock();
      Block secondBlock = ((BlockItem)second.getItem()).getBlock();
      BlockState firstState = firstBlock.defaultBlockState();
      BlockState secondState = secondBlock.defaultBlockState();
      int result = Boolean.compare(!this.isPoorScaffoldBlock(firstBlock, firstState), !this.isPoorScaffoldBlock(secondBlock, secondState));
      if (result != 0) {
         return result;
      }

      result = Boolean.compare(firstState.isRedstoneConductor(MC.level, BlockPos.ZERO), secondState.isRedstoneConductor(MC.level, BlockPos.ZERO));
      if (result != 0) {
         return result;
      }

      result = Boolean.compare(firstState.isCollisionShapeFullBlock(MC.level, BlockPos.ZERO), secondState.isCollisionShapeFullBlock(MC.level, BlockPos.ZERO));
      if (result != 0) {
         return result;
      }

      result = Float.compare(firstBlock.getFriction(), secondBlock.getFriction());
      if (result != 0) {
         return result;
      }

      result = Float.compare(Math.abs(firstBlock.getJumpFactor() - 1.0F), Math.abs(secondBlock.getJumpFactor() - 1.0F));
      if (result != 0) {
         return result;
      }

      result = Float.compare(Math.abs(firstBlock.getSpeedFactor() - 1.0F), Math.abs(secondBlock.getSpeedFactor() - 1.0F));
      if (result != 0) {
         return result;
      }

      result = Double.compare(this.hardnessPenalty(secondState, true), this.hardnessPenalty(firstState, true));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(second.getCount(), first.getCount());
      return result != 0 ? result : Double.compare(this.hardnessPenalty(secondState, false), this.hardnessPenalty(firstState, false));
   }

   private boolean isPoorScaffoldBlock(Block block, BlockState state) {
      return block.getFriction() > 0.6F
         || block.getSpeedFactor() < 1.0F
         || block.getJumpFactor() < 1.0F
         || block instanceof BaseEntityBlock
         || !state.isCollisionShapeFullBlock(MC.level, BlockPos.ZERO)
         || block == Blocks.CRAFTING_TABLE
         || block == Blocks.JIGSAW
         || block == Blocks.SMITHING_TABLE
         || block == Blocks.FLETCHING_TABLE
         || block == Blocks.ENCHANTING_TABLE
         || block == Blocks.CAULDRON
         || block == Blocks.MAGMA_BLOCK;
   }

   private double hardnessPenalty(BlockState state, boolean neutralRange) {
      double hardness = state.getDestroySpeed(MC.level, BlockPos.ZERO);
      return neutralRange && hardness >= 0.8 && hardness <= 2.0 ? 0.0 : Math.abs(1.7 - hardness);
   }

   private boolean isPlaceableBlock(ItemStack stack) {
      if (!(stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem)) {
         return false;
      } else if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      } else {
         Block block = blockItem.getBlock();
         if (isScaffoldBlock(block) && this.passesBlockFilter(block)) {
            BlockState state = block.defaultBlockState();
            return state.entityCanStandOnFace(MC.level, BlockPos.ZERO, MC.player, Direction.UP);
         } else {
            return false;
         }
      }
   }

   public static boolean isScaffoldBlock(Block block) {
      if (block == null || !(block.asItem() instanceof BlockItem blockItem && blockItem.getBlock() == block)) {
         return false;
      }

      if (!(block instanceof FallingBlock) && block != Blocks.TNT && block != Blocks.COBWEB && block != Blocks.NETHER_PORTAL) {
         try {
            VoxelShape collision = block.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());
            return Block.isFaceFull(collision, Direction.UP);
         } catch (Throwable ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean passesBlockFilter(Block block) {
      String mode = this.choice("filter-mode");
      if ("Off".equals(mode)) {
         return true;
      }

      boolean listed = this.filteredBlocks().contains(block);
      return "Whitelist".equals(mode) ? listed : !listed;
   }

   private Set<Block> filteredBlocks() {
      String raw = this.value("blocks");
      if (raw.equals(this.blockListRaw)) {
         return this.blockListCache;
      }

      Set<Block> blocks = new HashSet<>();

      for (String entry : this.list("blocks")) {
         Identifier id = Identifier.tryParse(RegistryIdValidator.init(entry));
         if (id != null) {
            BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> {
               if (isScaffoldBlock(block)) {
                  blocks.add(block);
               }
            });
         }
      }

      this.blockListRaw = raw;
      this.blockListCache = Set.copyOf(blocks);
      return this.blockListCache;
   }

   private void scheduleSwitchBack() {
      if (this.bool("switch-back") && this.switchBackSlot >= 0) {
         this.switchBackDelay = 5;
      }
   }

   private void requestSwitchBack() {
      this.scheduleSwitchBack();
   }

   private void tickSwitchBack() {
      if (!this.bool("switch-back")) {
         this.switchBackSlot = -1;
         this.scaffoldSlot = -1;
         this.switchBackDelay = 0;
      } else if (this.switchBackSlot >= 0 && MC != null && MC.player != null) {
         if (this.scaffoldSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.scaffoldSlot) {
            this.switchBackSlot = -1;
            this.lastRequestedSlot = -1;
            this.scaffoldSlot = -1;
            this.switchBackDelay = 0;
         } else if (this.switchBackDelay > 0) {
            this.switchBackDelay--;
         } else {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (selected == this.switchBackSlot) {
               this.switchBackSlot = -1;
               this.lastRequestedSlot = -1;
               this.scaffoldSlot = -1;
            } else if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
               this.switchBackDelay = 1;
            } else {
               try {
                  this.lastRequestedSlot = this.switchBackSlot;
                  autismclient.util.VirtualInputDispatcher.setSlot(this.switchBackSlot);
               } finally {
                  autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
               }
            }
         }
      }
   }

   private RotationMath.Rotation lastSentRotation() {
      if (this.sentRotation != null) {
         return this.sentRotation;
      } else {
         return MC != null && MC.player != null ? RotationMath.ofPlayer(MC.player) : new RotationMath.Rotation(0.0F, 0.0F);
      }
   }

   private static double angleBetween(RotationMath.Rotation first, RotationMath.Rotation second) {
      double cosine = Mth.clamp(lookVector(first).dot(lookVector(second)), -1.0, 1.0);
      return Math.toDegrees(Math.acos(cosine));
   }

   private static Vec3 lookVector(RotationMath.Rotation rotation) {
      return Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
   }

   private ScaffoldModule.MotionVectorPair courseLineFor(Input input) {
      Vec3 direction = this.laneDirection();
      ScaffoldModule.SupportHeightSample support = this.sampleSupportHeight();
      if (support == null) {
         if (!this.flags_Z()) {
            return null;
         }

         Vec3 position = MC.player.position();
         ScaffoldModule.MotionVectorPair previous = this.courseLine;
         Vec3 anchor = lineOrigin(
            position,
            previous == null ? null : previous.init(),
            previous == null ? null : previous.load(),
            direction,
            this.f_n_I <= 2 ? this.sampledSupport : null
         );
         return new ScaffoldModule.MotionVectorPair(new Vec3(anchor.x, position.y, anchor.z), direction);
      } else {
         this.supportSample = support;
         ScaffoldModule.MotionVectorPair placedLine = this.lineFromRecentPlacements();
         Vec3 anchor;
         if (placedLine != null && placedLine.load().dot(direction) >= 0.5) {
            anchor = nearestPointOnLine(placedLine, MC.player.position());
         } else {
            anchor = new Vec3(support.support().getX() + 0.5, MC.player.getY(), support.support().getZ() + 0.5);
         }

         return new ScaffoldModule.MotionVectorPair(new Vec3(anchor.x, MC.player.getY(), anchor.z), direction);
      }
   }

   private ScaffoldModule.MotionVectorPair retainLine(ScaffoldModule.MotionVectorPair requested, boolean edgeActive) {
      ScaffoldModule.MotionVectorPair retained = selectLine(requested, this.lockedLine, edgeActive, MC.player.getY());
      if (edgeActive && requested != null) {
         if (this.lockedLine == null) {
            this.lockedLine = requested;
         }

         return retained;
      } else {
         this.lockedLine = null;
         return retained;
      }
   }

   static ScaffoldModule.MotionVectorPair selectLine(ScaffoldModule.MotionVectorPair requested, ScaffoldModule.MotionVectorPair locked, boolean edgeActive, double playerY) {
      if (edgeActive && requested != null) {
         ScaffoldModule.MotionVectorPair selected = locked == null ? requested : locked;
         Vec3 origin = selected.init();
         return new ScaffoldModule.MotionVectorPair(new Vec3(origin.x, playerY, origin.z), selected.load());
      } else {
         return requested;
      }
   }

   static Vec3 lineOrigin(Vec3 position, Vec3 previousOrigin, Vec3 previousDirection, Vec3 requestedDirection, BlockPos lastSupport) {
      if (position == null) {
         return Vec3.ZERO;
      } else if (previousOrigin != null
         && previousDirection != null
         && requestedDirection != null
         && previousDirection.dot(requestedDirection) >= 0.5
         && previousDirection.lengthSqr() > 1.0E-12) {
         double parameter = position.subtract(previousOrigin).dot(previousDirection) / previousDirection.lengthSqr();
         return previousOrigin.add(previousDirection.scale(parameter));
      } else {
         return lastSupport != null ? new Vec3(lastSupport.getX() + 0.5, position.y, lastSupport.getZ() + 0.5) : position;
      }
   }

   private ScaffoldModule.MotionVectorPair lineFromRecentPlacements() {
      if (this.recentPlaced.size() < 2) {
         return null;
      }

      Iterator<BlockPos> iterator = this.recentPlaced.descendingIterator();
      BlockPos last = iterator.next();
      Vec3 lastCenter = new Vec3(last.getX() + 0.5, last.getY(), last.getZ() + 0.5);

      while (iterator.hasNext()) {
         BlockPos previous = iterator.next();
         Vec3 previousCenter = new Vec3(previous.getX() + 0.5, previous.getY(), previous.getZ() + 0.5);
         Vec3 direction = new Vec3(lastCenter.x - previousCenter.x, 0.0, lastCenter.z - previousCenter.z);
         if (!(direction.lengthSqr() <= 1.0E-8)) {
            return new ScaffoldModule.MotionVectorPair(previousCenter.add(lastCenter).scale(0.5), direction.normalize());
         }
      }

      return null;
   }

   private ScaffoldModule.SupportHeightSample sampleSupportHeight() {
      List<ScaffoldModule.SupportDistanceCandidate> candidates = new ArrayList<>(SUPPORT_SAMPLE_OFFSETS.length * SUPPORT_SAMPLE_OFFSETS.length);
      Set<BlockPos> visited = new HashSet<>();
      Vec3 playerPosition = MC.player.position();

      for (double xOffset : SUPPORT_SAMPLE_OFFSETS) {
         for (double zOffset : SUPPORT_SAMPLE_OFFSETS) {
            BlockPos pos = BlockPos.containing(playerPosition.x + xOffset, playerPosition.y - 1.0, playerPosition.z + zOffset);
            if (visited.add(pos)) {
               ScaffoldModule.SupportDistanceCandidate candidate = this.supportCandidate(pos);
               if (candidate != null) {
                  candidates.add(candidate);
               }
            }
         }
      }

      if (!candidates.isEmpty()) {
         ScaffoldModule.SupportDistanceCandidate best = candidates.stream().min(ScaffoldModule::compareSupportCandidates).orElse(null);
         if (best == null) {
            return null;
         }

         this.f_n_I = 0;
         this.supportSampleTick = -2147483648;
         ScaffoldModule.SupportDistanceCandidate chosen = this.init_aI_RingBuffer_List_aI_RingBuffer(candidates, best);
         this.sampledSupport = chosen.pos();
         return new ScaffoldModule.SupportHeightSample(
            chosen.pos(), playerPosition.x - (chosen.pos().getX() + 0.5), playerPosition.z - (chosen.pos().getZ() + 0.5)
         );
      } else {
         if (!this.flags_Z()) {
            this.sampledSupport = null;
            this.supportSample = null;
            this.f_n_I = 0;
            this.supportSampleTick = -2147483648;
            return null;
         }

         int clientTick = AutismSessionState.getInstance().getClientTick();
         if (this.supportSampleTick != clientTick) {
            this.supportSampleTick = clientTick;
            this.f_n_I++;
         }

         if (this.f_n_I > 2) {
            this.sampledSupport = null;
            this.supportSample = null;
         }

         return null;
      }
   }

   private ScaffoldModule.SupportDistanceCandidate supportCandidate(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      VoxelShape shape = state.getCollisionShape(MC.level, pos, CollisionContext.of(MC.player));
      if (shape.isEmpty()) {
         return null;
      }

      AABB playerBox = MC.player.getBoundingBox();
      double bestSurfaceDelta = 1.0 / 0.0;
      double overlapAtBestSurface = 0.0;

      for (AABB local : shape.toAabbs()) {
         double minX = pos.getX() + local.minX;
         double maxX = pos.getX() + local.maxX;
         double maxY = pos.getY() + local.maxY;
         double minZ = pos.getZ() + local.minZ;
         double maxZ = pos.getZ() + local.maxZ;
         double overlapX = Math.min(playerBox.maxX, maxX) - Math.max(playerBox.minX, minX);
         double overlapZ = Math.min(playerBox.maxZ, maxZ) - Math.max(playerBox.minZ, minZ);
         if (!(overlapX <= 0.0) && !(overlapZ <= 0.0)) {
            double surfaceDelta = Math.abs(playerBox.minY - maxY);
            double overlap = overlapX * overlapZ;
            if (surfaceDelta + 0.001 < bestSurfaceDelta) {
               bestSurfaceDelta = surfaceDelta;
               overlapAtBestSurface = overlap;
            } else if (Math.abs(surfaceDelta - bestSurfaceDelta) <= 0.001) {
               overlapAtBestSurface += overlap;
            }
         }
      }

      if (!Double.isFinite(bestSurfaceDelta)) {
         return null;
      }

      double dx = MC.player.getX() - (pos.getX() + 0.5);
      double dz = MC.player.getZ() - (pos.getZ() + 0.5);
      return new ScaffoldModule.SupportDistanceCandidate(pos.immutable(), overlapAtBestSurface, bestSurfaceDelta, dx * dx + dz * dz);
   }

   private ScaffoldModule.SupportDistanceCandidate init_aI_RingBuffer_List_aI_RingBuffer(List<ScaffoldModule.SupportDistanceCandidate> candidates, ScaffoldModule.SupportDistanceCandidate best) {
      BlockPos lastPlaced = this.recentPlaced.peekLast();
      ScaffoldModule.SupportDistanceCandidate preferred = candidateAt(candidates, lastPlaced);
      if (preferred != null && isNearlyAsGood(preferred, best)) {
         return preferred;
      }

      preferred = candidateAt(candidates, this.sampledSupport);
      return preferred != null && isNearlyAsGood(preferred, best) ? preferred : best;
   }

   private static ScaffoldModule.SupportDistanceCandidate candidateAt(List<ScaffoldModule.SupportDistanceCandidate> candidates, BlockPos position) {
      if (position == null) {
         return null;
      }

      for (ScaffoldModule.SupportDistanceCandidate candidate : candidates) {
         if (candidate.pos().equals(position)) {
            return candidate;
         }
      }

      return null;
   }

   private static boolean isNearlyAsGood(ScaffoldModule.SupportDistanceCandidate candidate, ScaffoldModule.SupportDistanceCandidate best) {
      return candidate.load() <= best.load() + 0.001 && candidate.init_D() + 0.02 >= best.init_D();
   }

   private static int compareSupportCandidates(ScaffoldModule.SupportDistanceCandidate first, ScaffoldModule.SupportDistanceCandidate second) {
      if (first.load() + 0.001 < second.load()) {
         return -1;
      } else if (second.load() + 0.001 < first.load()) {
         return 1;
      } else if (first.init_D() > second.init_D() + 0.02) {
         return -1;
      } else {
         return first.init_D() + 0.02 < second.init_D() ? 1 : Double.compare(first.read(), second.read());
      }
   }

   private Vec3 predictedPosition(ScaffoldModule.MotionVectorPair line) {
      Vec3 playerPosition = MC.player.position();
      if (line == null) {
         return playerPosition;
      } else {
         boolean nearEdge = this.isNearEdgeWithin(0.05, playerPosition);
         Vec3 fallOff = this.fallOffPoint(line);
         if (fallOff == null) {
            return playerPosition;
         } else if (shouldEdgeSneak(this.flags_Z(), nearEdge, this.edgeGateActive)) {
            return write_Vec3_Vec3_Vec3(fallOff, line.load());
         } else if (nearEdge) {
            return playerPosition;
         } else {
            Vec3 delta = fallOff.subtract(playerPosition);
            double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            Vec3 bootstrap = horizontalDistance <= 1.0E-8 ? fallOff : fallOff.subtract(new Vec3(delta.x, 0.0, delta.z).scale(0.2 / horizontalDistance));
            Vec3 average = this.meanOffset();
            if (average == null) {
               ScaffoldModule.SupportHeightSample support = this.supportSample;
               return support == null ? bootstrap : bootstrap.add(support.offsetX(), 0.0, support.load());
            } else {
               float angle = (float)Math.atan2(line.load().z, line.load().x);
               Vec3 historyPosition = fallOff.add(average.yRot(-angle));
               double blend = Math.min(1.0, this.recentOffsets.size() / 2.0);
               return bootstrap.lerp(historyPosition, blend);
            }
         }
      }
   }

   static boolean write(Vec3 position, Vec3 velocity, BlockPos cell) {
      if (position != null && velocity != null && cell != null) {
         double surfaceY = cell.getY() + 1.0;
         double x = position.x;
         double y = position.y;
         double z = position.z;
         double vx = velocity.x;
         double vy = velocity.y;
         double vz = velocity.z;

         for (int tick = 0; tick < 20 && y > surfaceY; tick++) {
            x += vx;
            y += vy;
            z += vz;
            double speed = Math.sqrt(vx * vx + vz * vz);
            double push = speed <= 1.0E-6 ? 0.0 : 0.02 / speed;
            vx = vx * 0.91 + vx * push;
            vz = vz * 0.91 + vz * push;
            vy = (vy - 0.08) * 0.98;
         }

         double overlapX = Math.min(x + 0.29, cell.getX() + 1.0) - Math.max(x - 0.29, cell.getX());
         double overlapZ = Math.min(z + 0.29, cell.getZ() + 1.0) - Math.max(z - 0.29, cell.getZ());
         return overlapX >= 0.15 && overlapZ >= 0.15;
      } else {
         return false;
      }
   }

   static boolean isFartherThan(Vec3 position, BlockPos support, double maxDistance) {
      if (position != null && support != null) {
         double dx = support.getX() + 0.5 - position.x;
         double dz = support.getZ() + 0.5 - position.z;
         return dx * dx + dz * dz > maxDistance * maxDistance;
      } else {
         return false;
      }
   }

   static Vec3 write_Vec3_Vec3_Vec3(Vec3 fallOff, Vec3 direction) {
      if (fallOff == null) {
         return null;
      }

      if (direction == null) {
         return fallOff;
      }

      Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
      return horizontal.lengthSqr() <= 1.0E-12 ? fallOff : fallOff.add(horizontal.normalize().scale(1.0E-4));
   }

   static Vec3 eyePastFace(Vec3 playerEye, Vec3 faceCenter, Vec3 normal, double lead) {
      double playerPast = playerEye.subtract(faceCenter).dot(normal);
      return playerPast >= lead ? playerEye : playerEye.add(normal.scale(lead - playerPast));
   }

   private Vec3 init_Vec3_BlockPos(BlockPos support, Direction face, Vec3 selectionEye, Vec3 laneDirection) {
      if (this.flags_Z() && MC.player != null) {
         if (laneDirection != null && !(laneDirection.horizontalDistanceSqr() <= 1.0E-12)) {
            Vec3 course = new Vec3(laneDirection.x, 0.0, laneDirection.z).normalize();
            Vec3 leadEye = MC.player.getEyePosition().add(MC.player.getDeltaMovement());
            Vec3 goalEye = init_Vec3_Vec3_3(leadEye, selectionEye, support, face, course, 0.25);
            this.aimDecision = goalEye == leadEye ? "lead" : "push";
            return goalEye;
         } else {
            return selectionEye;
         }
      } else {
         return selectionEye;
      }
   }

   static Vec3 init_Vec3_Vec3_3(Vec3 playerEye, Vec3 selectionEye, BlockPos support, Direction face, Vec3 course, double lead) {
      if (course == null) {
         return selectionEye;
      } else if (face == Direction.UP) {
         Vec3 farEdge = Vec3.atCenterOf(support).add(course.scale(0.5));
         farEdge = new Vec3(farEdge.x, playerEye.y, farEdge.z);
         return eyePastFace(playerEye, farEdge, course, lead);
      } else {
         Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
         Vec3 faceCenter = Vec3.atCenterOf(support).add(normal.scale(0.5));
         return eyePastFace(playerEye, faceCenter, normal, lead);
      }
   }

   static float load_F_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return init_F_Vec3(eye, support, face, yaw, 89.3F);
   }

   private static double[] init_DArr_Vec3_2(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return init_DArr_Vec3_4(eye, supportBox(support), face, yaw);
   }

   static double[] init_DArr_Vec3_4(Vec3 eye, AABB support, Direction face, float yaw) {
      if (support == null) {
         return null;
      }

      double past = distancePastFace(eye, support, face);
      if (past <= 0.0) {
         return null;
      }

      double yawRad = Math.toRadians(yaw);
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      double toward = -(lookX * face.getStepX() + lookZ * face.getStepZ());
      if (toward <= 0.1) {
         return null;
      }

      double run = past / toward;
      double margin = 0.06 * support.getYsize();
      double shallowest = Math.toDegrees(Math.atan2(eye.y - (support.maxY - margin), run));
      double steepest = Math.toDegrees(Math.atan2(eye.y - (support.minY + margin), run));
      return new double[]{shallowest, steepest, run};
   }

   static float init_F_Vec3(Vec3 primaryEye, Vec3 secondaryEye, BlockPos support, Direction face, float yaw, float maxPitch) {
      return init_F_Vec3_2(primaryEye, secondaryEye, supportBox(support), face, yaw, maxPitch);
   }

   static float init_F_Vec3_2(Vec3 primaryEye, Vec3 secondaryEye, AABB support, Direction face, float yaw, float maxPitch) {
      double[] window = init_DArr_Vec3_3(primaryEye, secondaryEye, support, face, yaw);
      if (window == null) {
         return Math.min(maxPitch, 89.3F);
      }

      double preferred = Math.toDegrees(Math.atan2(primaryEye.y - supportMidY(support), window[2]));
      return (float)Mth.clamp(Mth.clamp(preferred, window[0], window[1]), -maxPitch, maxPitch);
   }

   static double supportMidY(AABB support) {
      return support.maxY - 0.5 * support.getYsize();
   }

   static double[] init_DArr_Vec3_2(Vec3 primaryEye, Vec3 secondaryEye, BlockPos support, Direction face, float yaw) {
      return init_DArr_Vec3_3(primaryEye, secondaryEye, supportBox(support), face, yaw);
   }

   static double[] init_DArr_Vec3_3(Vec3 primaryEye, Vec3 secondaryEye, AABB support, Direction face, float yaw) {
      double[] primary = init_DArr_Vec3_4(primaryEye, support, face, yaw);
      if (primary == null) {
         return null;
      }

      double low = primary[0];
      double high = primary[1];
      double[] secondary = init_DArr_Vec3_4(secondaryEye, support, face, yaw);
      if (secondary != null) {
         double bothLow = Math.max(low, secondary[0]);
         double bothHigh = Math.min(high, secondary[1]);
         if (bothLow <= bothHigh) {
            low = bothLow;
            high = bothHigh;
         }
      }

      return new double[]{low, high, primary[2]};
   }

   static float init_F_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw, float maxPitch) {
      return init_F_Vec3_2(eye, supportBox(support), face, yaw, maxPitch);
   }

   static float init_F_Vec3_2(Vec3 eye, AABB support, Direction face, float yaw, float maxPitch) {
      float park = Math.min(maxPitch, 89.3F);
      double past = distancePastFace(eye, support, face);
      if (past <= 0.0) {
         return park;
      }

      double yawRad = Math.toRadians(yaw);
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      double toward = -(lookX * face.getStepX() + lookZ * face.getStepZ());
      if (toward <= 0.1) {
         return park;
      }

      double run = past / toward;
      double drop = eye.y - supportMidY(support);
      return (float)Mth.clamp(Math.toDegrees(Math.atan2(drop, run)), -maxPitch, maxPitch);
   }

   static double distancePastSupportFace(Vec3 eye, BlockPos support, Direction face) {
      return distancePastFace(eye, supportBox(support), face);
   }

   static double distancePastFace(Vec3 eye, AABB support, Direction face) {
      if (support == null) {
         return 0.0;
      }

      double var10000;
      switch (face) {
         case EAST:
            var10000 = support.maxX;
            break;
         case WEST:
            var10000 = support.minX;
            break;
         case SOUTH:
            var10000 = support.maxZ;
            break;
         case NORTH:
            var10000 = support.minZ;
            break;
         case DOWN:
            var10000 = support.minY;
            break;
         case UP:
            var10000 = support.maxY;
            break;
         default:
            throw new MatchException(null, null);
      }

      double plane = var10000;
      switch (face.getAxis()) {
         case X:
            var10000 = eye.x;
            break;
         case Y:
            var10000 = eye.y;
            break;
         case Z:
            var10000 = eye.z;
            break;
         default:
            throw new MatchException(null, null);
      }

      double along = var10000;
      return (along - plane) * (face.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 : -1.0);
   }

   static boolean init_Z_Vec3_3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return load_Z_Vec3(eye, support, face, yaw, false);
   }

   static double init_D_Vec3_2(Vec3 eye, BlockPos support, float yaw, double margin) {
      return init_D_Vec3_4(eye, supportBox(support), yaw, margin);
   }

   static double init_D_Vec3_4(Vec3 eye, AABB support, float yaw, double margin) {
      double[] span = init_DArr_Vec3_3(eye, support, yaw, margin);
      return span == null ? 0.0 / 0.0 : (span[0] + span[1]) * 0.5;
   }

   static double[] init_DArr_Vec3(Vec3 eye, BlockPos support, float yaw, double margin) {
      return init_DArr_Vec3_3(eye, supportBox(support), yaw, margin);
   }

   static double[] init_DArr_Vec3_3(Vec3 eye, AABB support, float yaw, double margin) {
      if (support == null) {
         return null;
      }

      double drop = eye.y - support.maxY;
      if (drop <= 1.0E-4) {
         return null;
      }

      double yawRad = Math.toRadians(yaw);
      double[] origin = new double[]{eye.x, eye.z};
      double[] look = new double[]{-Math.sin(yawRad), Math.cos(yawRad)};
      double[] low = new double[]{support.minX + margin, support.minZ + margin};
      double[] high = new double[]{support.maxX - margin, support.maxZ - margin};
      double enter = 0.0;
      double exit = 1.7976931348623157E308;

      for (int axis = 0; axis < 2; axis++) {
         if (Math.abs(look[axis]) < 1.0E-6) {
            if (origin[axis] < low[axis] || origin[axis] > high[axis]) {
               return null;
            }
         } else {
            double first = (low[axis] - origin[axis]) / look[axis];
            double second = (high[axis] - origin[axis]) / look[axis];
            enter = Math.max(enter, Math.min(first, second));
            exit = Math.min(exit, Math.max(first, second));
         }
      }

      return exit <= enter ? null : new double[]{enter, exit};
   }

   static boolean init_Z_Vec3_2(Vec3 eye, BlockPos support, float yaw, boolean holding) {
      return init_Z_Vec3_5(eye, supportBox(support), yaw, holding);
   }

   static boolean init_Z_Vec3_5(Vec3 eye, AABB support, float yaw, boolean holding) {
      return !Double.isNaN(init_D_Vec3_4(eye, support, yaw, holding ? 0.02 : 0.1));
   }

   static float init_F_Vec3(Vec3 eye, BlockPos support, float yaw) {
      return init_F_Vec3_2(eye, supportBox(support), yaw);
   }

   static float init_F_Vec3_2(Vec3 eye, AABB support, float yaw) {
      double[] span = init_DArr_Vec3_3(eye, support, yaw, 0.1);
      if (span == null) {
         return 0.0F / 0.0F;
      }

      double drop = eye.y - support.maxY;
      double middle = (span[0] + span[1]) * 0.5;
      return (float)Math.toDegrees(Math.atan2(drop, Math.max(middle, 1.0E-4)));
   }

   static boolean load_Z_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw, boolean holding) {
      return init_Z_Vec3_3(eye, supportBox(support), face, yaw, holding);
   }

   static boolean init_Z_Vec3_3(Vec3 eye, AABB support, Direction face, float yaw, boolean holding) {
      if (support == null) {
         return false;
      }

      double past = distancePastFace(eye, support, face);
      if (past <= 0.0) {
         return true;
      }

      double yawRad = Math.toRadians(yaw);
      double lookX = -Math.sin(yawRad);
      double lookZ = Math.cos(yawRad);
      double toward = -(lookX * face.getStepX() + lookZ * face.getStepZ());
      if (toward <= 0.1) {
         return false;
      }

      double run = past / toward;
      boolean xAxisFace = face.getStepX() != 0;
      double crossing = xAxisFace ? eye.z + lookZ * run : eye.x + lookX * run;
      double edge = xAxisFace ? support.minZ : support.minX;
      double far = xAxisFace ? support.maxZ : support.maxX;
      boolean corner = Math.abs(Math.abs(lookX) - Math.abs(lookZ)) <= 0.05;
      double margin = corner ? (holding ? -0.14 : -0.12) : (holding ? -0.12 : 0.04);
      return crossing >= edge + margin && crossing <= far - margin;
   }

   static double init_D_Vec3_3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return init_D_Vec3_5(eye, supportBox(support), face, yaw);
   }

   static double init_D_Vec3_5(Vec3 eye, AABB support, Direction face, float yaw) {
      if (support != null && face.getAxis().isHorizontal()) {
         double past = distancePastFace(eye, support, face);
         if (past <= 0.0) {
            return 0.0 / 0.0;
         }

         double yawRad = Math.toRadians(yaw);
         double lookX = -Math.sin(yawRad);
         double lookZ = Math.cos(yawRad);
         double toward = -(lookX * face.getStepX() + lookZ * face.getStepZ());
         if (toward <= 0.1) {
            return 0.0 / 0.0;
         }

         double run = past / toward;
         boolean xAxisFace = face.getStepX() != 0;
         double crossing = xAxisFace ? eye.z + lookZ * run : eye.x + lookX * run;
         double low = xAxisFace ? support.minZ : support.minX;
         double span = xAxisFace ? support.getZsize() : support.getXsize();
         return span <= 1.0E-9 ? 0.0 / 0.0 : (crossing - low) / span;
      } else {
         return 0.0 / 0.0;
      }
   }

   private static boolean read_Z_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      double frac = init_D_Vec3_3(eye, support, face, yaw);
      return !Double.isNaN(frac) && frac >= 0.04 && frac <= 0.96;
   }

   private static float load_F_Vec3(Vec3 eye, Vec3 lead, BlockPos support, Direction face, float fromYaw, float laneYaw) {
      if (!face.getAxis().isHorizontal()) {
         return 0.0F / 0.0F;
      }

      for (int step = 0; step <= 2; step++) {
         if (read_Z_Vec3(eye.add(lead.scale(step)), support, face, fromYaw)) {
            return 0.0F / 0.0F;
         }
      }

      for (float off = 1.0F; off <= 18.0F; off++) {
         for (int sign = 1; sign >= -1; sign -= 2) {
            float yaw = Mth.wrapDegrees(fromYaw + sign * off);
            if (read_Z_Vec3(eye, support, face, yaw) && !(Math.abs(octantResidual(laneYaw, yaw)) > 12.0F)) {
               return yaw;
            }
         }
      }

      return 0.0F / 0.0F;
   }

   static float init_F_Vec3_3(Vec3 eye, Vec3 lead, BlockPos support, Direction face, float fromYaw) {
      if (!face.getAxis().isHorizontal()) {
         return 0.0F / 0.0F;
      }

      if (!init_Z_Vec3_6(eye, lead, support, face, fromYaw)) {
         for (int step = 0; step <= 2; step++) {
            if (read_Z_Vec3(eye.add(lead.scale(step)), support, face, fromYaw)) {
               return 0.0F / 0.0F;
            }
         }
      }

      for (int gridStep = 1; gridStep <= 2; gridStep++) {
         for (int sign = 1; sign >= -1; sign -= 2) {
            float yaw = Mth.wrapDegrees(fromYaw + sign * gridStep * 45.0F);
            if (!init_Z_Vec3_6(eye, lead, support, face, yaw)) {
               boolean lands = true;

               for (int step = 0; step <= 2 && lands; step++) {
                  lands = read_Z_Vec3(eye.add(lead.scale(step)), support, face, yaw);
               }

               if (lands) {
                  return yaw;
               }
            }
         }
      }

      return 0.0F / 0.0F;
   }

   static float init_F_Vec3_3(Vec3 eye, Vec3 lead, BlockPos support, float fromYaw) {
      for (int step = 0; step <= 2; step++) {
         if (!Float.isNaN(init_F_Vec3(eye.add(lead.scale(step)), support, fromYaw))) {
            return 0.0F / 0.0F;
         }
      }

      for (int gridStep = 1; gridStep <= 2; gridStep++) {
         for (int sign = 1; sign >= -1; sign -= 2) {
            float yaw = Mth.wrapDegrees(fromYaw + sign * gridStep * 45.0F);
            boolean lands = true;

            for (int step = 0; step <= 2 && lands; step++) {
               lands = !Float.isNaN(init_F_Vec3(eye.add(lead.scale(step)), support, yaw));
            }

            if (lands) {
               return yaw;
            }
         }
      }

      return 0.0F / 0.0F;
   }

   static boolean init_Z_Vec3_6(Vec3 eye, Vec3 lead, BlockPos support, Direction face, float yaw) {
      if (!face.getAxis().isHorizontal()) {
         return false;
      }

      double yawRad = Math.toRadians(yaw);
      double toward = -(-Math.sin(yawRad) * face.getStepX() + Math.cos(yawRad) * face.getStepZ());
      if (toward >= 0.25) {
         return false;
      }

      double[] real = init_DArr_Vec3_2(eye, support, face, yaw);
      if (real == null) {
         return false;
      }

      double[] ahead = init_DArr_Vec3_2(eye.add(lead), support, face, yaw);
      return ahead == null ? false : ahead[1] < real[0] || real[1] < ahead[0];
   }

   static boolean init_Z_Vec3_2(Vec3 pinEye, Vec3 leadStep, BlockPos support, Direction face, float pinYaw, boolean holding, int lookahead) {
      for (int step = 0; step <= lookahead; step++) {
         Vec3 eye = step == 0 ? pinEye : pinEye.add(leadStep.scale(step));
         if (init_Z_Vec3_2(eye, support, face, pinYaw, holding)) {
            return true;
         }
      }

      return false;
   }

   static boolean shouldEdgeSneak(boolean grimFamily, boolean nearEdge, boolean edgeSneakActive) {
      return grimFamily && (nearEdge || edgeSneakActive);
   }

   static boolean write(boolean grimFamily, boolean edgeSneakActive, boolean onGround, boolean jumping) {
      return grimFamily && edgeSneakActive && onGround && !jumping;
   }

   private Vec3 fallOffPoint(ScaffoldModule.MotionVectorPair line) {
      if (line == null) {
         return null;
      }

      Vec3 nearest = nearestPointOnLine(line, MC.player.position());
      Vec3 from = nearest.add(0.0, -0.1, 0.0);
      Vec3 to = from.add(line.load().scale(3.0));
      Vec3 collision = this.firstCollision(from, to);
      return collision == null ? null : new Vec3(collision.x, MC.player.getY(), collision.z);
   }

   private Vec3 moveDirection() {
      Input input = MC.player.input == null ? Input.EMPTY : MC.player.input.keyPresses;
      Vec3 nextVelocity = MC.player.getDeltaMovement();
      if (nextVelocity.horizontalDistanceSqr() > 9.0E-6) {
         return new Vec3(nextVelocity.x, 0.0, nextVelocity.z).normalize();
      } else {
         return hasMovementInput(input) ? Vec3.directionFromRotation(0.0F, this.inputMoveYaw(input)) : Vec3.directionFromRotation(0.0F, MC.player.getYRot());
      }
   }

   private boolean isNearEdgeWithin(double distance, Vec3 position) {
      Vec3 nextVelocity = MC.player.getDeltaMovement();
      Vec3 direction = this.moveDirection();
      Vec3 from = position.add(0.0, -0.1, 0.0);
      if (this.firstCollision(from, from.add(direction.scale(distance))) != null) {
         return true;
      }

      Vec3 nextPosition = position.add(nextVelocity.x, nextVelocity.y, nextVelocity.z);
      Vec3 positionInTwoTicks = nextPosition.add(nextVelocity.x, 0.0, nextVelocity.z);
      return this.wouldFall(position) || this.wouldFall(positionInTwoTicks);
   }

   private boolean j() {
      if (MC.player.onGround()) {
         double lookahead = Mth.clamp(0.12 + MC.player.getDeltaMovement().horizontalDistance() * 1.0, 0.12, 0.35);
         return this.isNearEdgeWithin(lookahead, MC.player.position());
      } else {
         return MC.player.fallDistance > 0.0 && this.isNearEdgeWithin(0.1, MC.player.position());
      }
   }

   private ScaffoldModule.EdgeDropWarning edgeDropWarning() {
      if (MC.player.onGround() && !this.isNearEdgeWithin(0.35, MC.player.position())) {
         return ScaffoldModule.EdgeDropWarning.NONE;
      }

      Vec3 position = MC.player.position();
      Vec3 velocity = MC.player.getDeltaMovement();
      double x = position.x;
      double y = position.y;
      double z = position.z;
      double vx = velocity.x;
      double vy = velocity.y;
      double vz = velocity.z;
      double startY = y;

      for (int tick = 0; tick < 12; tick++) {
         vx *= 0.91;
         vy = (vy - 0.08) * 0.98;
         vz *= 0.91;
         x += vx;
         y += vy;
         z += vz;
         if (this.hasSupportWithin(x, y, z, 2)) {
            return ScaffoldModule.EdgeDropWarning.NONE;
         }

         double drop = startY - y;
         if (drop > 2.0) {
            return ScaffoldModule.EdgeDropWarning.IMMINENT;
         }

         if (drop > 0.6 && tick < 4) {
            return ScaffoldModule.EdgeDropWarning.IMMINENT;
         }
      }

      return ScaffoldModule.EdgeDropWarning.NONE;
   }

   private boolean hasSupportWithin(double x, double y, double z, int blocks) {
      MutableBlockPos pos = new MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));

      for (int step = 0; step <= blocks; step++) {
         if (this.hasCentreSupport(MC.level.getBlockState(pos), pos)) {
            return true;
         }

         pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
      }

      return false;
   }

   private boolean wouldFall(Vec3 position) {
      AABB hitbox = MC.player
         .getDimensions(MC.player.getPose())
         .makeBoundingBox(position)
         .inflate(-0.05, 0.0, -0.05)
         .move(0.0, MC.player.fallDistance - MC.player.maxUpStep(), 0.0);
      return MC.level.noCollision(MC.player, hitbox);
   }

   private Vec3 firstCollision(Vec3 from, Vec3 to) {
      Vec3 line = to.subtract(from);
      if (line.lengthSqr() <= 1.0E-12) {
         return null;
      }

      List<AABB> boxes = this.collisionBoxesAlong(from, to);
      Vec3 current = from;
      Vec3 extendedFrom = from.add(line.scale(-1000.0));
      Vec3 extendedTo = to.add(line.scale(1000.0));

      while (true) {
         List<AABB> containing = new ArrayList<>();

         for (AABB box : boxes) {
            if (box.contains(current)) {
               containing.add(box);
            }
         }

         if (containing.isEmpty()) {
            return current;
         }

         for (AABB box : containing) {
            if (box.contains(to)) {
               return null;
            }
         }

         Vec3 next = null;
         double nearestToDestination = 1.0 / 0.0;

         for (AABB box : containing) {
            Vec3 clipped = box.clip(extendedTo, extendedFrom).orElse(null);
            if (clipped != null) {
               double distance = clipped.distanceToSqr(to);
               if (distance < nearestToDestination) {
                  nearestToDestination = distance;
                  next = clipped;
               }
            }
         }

         if (next == null) {
            return current;
         }

         current = next;
         boxes.removeAll(containing);
      }
   }

   private List<AABB> collisionBoxesAlong(Vec3 from, Vec3 to) {
      AABB fromBox = MC.player.getDimensions(Pose.STANDING).makeBoundingBox(from);
      AABB toBox = MC.player.getDimensions(Pose.STANDING).makeBoundingBox(to);
      AABB union = fromBox.minmax(toBox);
      int minX = Mth.floor(union.minX - 0.3 - 1.0E-7);
      int maxX = Mth.floor(union.maxX + 0.3 + 1.0E-7);
      int minY = Mth.floor(union.minY - 0.5 - 1.0E-7);
      int maxY = Mth.floor(union.minY + 1.0E-7);
      int minZ = Mth.floor(union.minZ - 0.3 - 1.0E-7);
      int maxZ = Mth.floor(union.maxZ + 0.3 + 1.0E-7);
      Vec3 line = to.subtract(from);
      Vec3 extendedFrom = from.add(line.scale(-1000.0));
      Vec3 extendedTo = to.add(line.scale(1000.0));
      List<AABB> boxes = new ArrayList<>();

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos pos = new BlockPos(x, y, z);
               VoxelShape shape = MC.level.getBlockState(pos).getCollisionShape(MC.level, pos);

               for (AABB local : shape.toAabbs()) {
                  AABB adjusted = new AABB(
                     x + local.minX - 0.3, y + local.minY - 1.0, z + local.minZ - 0.3, x + local.maxX + 0.3, y + local.maxY + 0.55, z + local.maxZ + 0.3
                  );
                  if (adjusted.clip(extendedFrom, extendedTo).isPresent()) {
                     boxes.add(adjusted);
                  }
               }
            }
         }
      }

      return boxes;
   }

   private Vec3 meanOffset() {
      if (this.recentOffsets.isEmpty()) {
         return null;
      }

      Vec3 sum = Vec3.ZERO;

      for (Vec3 offset : this.recentOffsets) {
         sum = sum.add(offset);
      }

      return sum.scale(1.0 / this.recentOffsets.size());
   }

   private static Vec3 nearestPointOnLine(ScaffoldModule.MotionVectorPair line, Vec3 point) {
      Vec3 delta = point.subtract(line.init());
      double projection = delta.dot(line.load());
      return line.init().add(line.load().scale(projection));
   }

   private static double distanceSqToLine(ScaffoldModule.MotionVectorPair line, Vec3 point) {
      return nearestPointOnLine(line, point).distanceToSqr(point);
   }

   private static double distanceSqToBox(Vec3 point, AABB box) {
      double x = Mth.clamp(point.x, box.minX, box.maxX);
      double y = Mth.clamp(point.y, box.minY, box.maxY);
      double z = Mth.clamp(point.z, box.minZ, box.maxZ);
      return point.distanceToSqr(new Vec3(x, y, z));
   }

   private static double lineBoxDistance(ScaffoldModule.MotionVectorPair movementLine, AABB box) {
      ScaffoldModule.RayLine line = new ScaffoldModule.RayLine(movementLine.init(), movementLine.load());
      if (rayIntersectsBox(line, box)) {
         return 0.0;
      }

      Vec3 p000 = new Vec3(box.minX, box.minY, box.minZ);
      Vec3 p001 = new Vec3(box.minX, box.minY, box.maxZ);
      Vec3 p010 = new Vec3(box.minX, box.maxY, box.minZ);
      Vec3 p011 = new Vec3(box.minX, box.maxY, box.maxZ);
      Vec3 p100 = new Vec3(box.maxX, box.minY, box.minZ);
      Vec3 p101 = new Vec3(box.maxX, box.minY, box.maxZ);
      Vec3 p110 = new Vec3(box.maxX, box.maxY, box.minZ);
      Vec3 p111 = new Vec3(box.maxX, box.maxY, box.maxZ);
      ScaffoldModule.EdgeSegment[] edges = new ScaffoldModule.EdgeSegment[]{
         new ScaffoldModule.EdgeSegment(p000, p001),
         new ScaffoldModule.EdgeSegment(p000, p010),
         new ScaffoldModule.EdgeSegment(p000, p100),
         new ScaffoldModule.EdgeSegment(p111, p110),
         new ScaffoldModule.EdgeSegment(p111, p101),
         new ScaffoldModule.EdgeSegment(p111, p011),
         new ScaffoldModule.EdgeSegment(p001, p011),
         new ScaffoldModule.EdgeSegment(p001, p101),
         new ScaffoldModule.EdgeSegment(p010, p011),
         new ScaffoldModule.EdgeSegment(p010, p110),
         new ScaffoldModule.EdgeSegment(p100, p101),
         new ScaffoldModule.EdgeSegment(p100, p110)
      };
      double best = 1.0 / 0.0;

      for (ScaffoldModule.EdgeSegment edge : edges) {
         ScaffoldModule.SegmentIntersection pair = segmentIntersection(edge, line);
         if (pair != null) {
            best = Math.min(best, pair.init().distanceToSqr(pair.load()));
         }
      }

      return best;
   }

   private static boolean rayIntersectsBox(ScaffoldModule.RayLine line, AABB box) {
      double enter = -1.0 / 0.0;
      double exit = 1.0 / 0.0;
      double[] anchors = new double[]{line.init().x, line.init().y, line.init().z};
      double[] directions = new double[]{line.load().x, line.load().y, line.load().z};
      double[] minimums = new double[]{box.minX, box.minY, box.minZ};
      double[] maximums = new double[]{box.maxX, box.maxY, box.maxZ};

      for (int axis = 0; axis < 3; axis++) {
         if (Mth.equal(directions[axis], 0.0)) {
            if (anchors[axis] < minimums[axis] || anchors[axis] > maximums[axis]) {
               return false;
            }
         } else {
            double first = (minimums[axis] - anchors[axis]) / directions[axis];
            double second = (maximums[axis] - anchors[axis]) / directions[axis];
            enter = Math.max(enter, Math.min(first, second));
            exit = Math.min(exit, Math.max(first, second));
            if (enter > exit + 1.0E-9) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean hasMovementInput(Input input) {
      return input != null && (input.forward() != input.backward() || input.left() != input.right());
   }

   private float inputMoveYaw(Input input) {
      return MC.player.getYRot() + inputYawOffset(input);
   }

   private float movementYaw(LocalPlayer player) {
      Input move = this.ownsInput && !player.onGround() && this.airborneInput != null
         ? this.airborneInput
         : (
            this.ownsInput && this.stableInput != null
               ? this.stableInput
               : new Input(
                  isKeyDown(MC.options.keyUp),
                  isKeyDown(MC.options.keyDown),
                  isKeyDown(MC.options.keyLeft),
                  isKeyDown(MC.options.keyRight),
                  false,
                  false,
                  false
               )
         );
      if (!hasMovementInput(move)) {
         return Float.isFinite(this.anchorYaw) ? this.anchorYaw : player.getYRot();
      } else {
         return this.inputMoveYaw(move);
      }
   }

   private boolean k() {
      return MC.player != null && !MC.player.onGround() && MC.player.getDeltaMovement().y < -0.08;
   }

   private Vec3 init_Vec3_aI_DataUtil(
      ScaffoldModule.FaceRect face, ScaffoldModule.FaceRect fullFace, BlockPos targetPos, Vec3 eye, RotationMath.Rotation currentRotation, ScaffoldModule.MotionVectorPair optimalLine
   ) {
      Vec3 offset = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ());
      ScaffoldModule.FaceRect trimmed = face.load(0.15).init(offset);
      ScaffoldModule.FaceRect targetFace = this.init_aI_DataUtil_aI_DataUtil(trimmed, eye, optimalLine);
      Vec3 point = init_Vec3_aI_DataUtil_aI_ByteReader(targetFace, new ScaffoldModule.RayLine(eye, lookVector(currentRotation)));
      if (this.flags_Z()) {
         point = init_Vec3_Vec3(point, fullFace, offset);
      }

      return point.subtract(offset);
   }

   private static Vec3 init_Vec3_Vec3(Vec3 point, ScaffoldModule.FaceRect face, Vec3 offset) {
      double cx = offset.x + (face.read().x + face.write().x) * 0.5;
      double cy = offset.y + (face.read().y + face.write().y) * 0.5;
      double cz = offset.z + (face.read().z + face.write().z) * 0.5;
      double hx = write_D_D(face.write().x - face.read().x);
      double hy = write_D_D(face.write().y - face.read().y);
      double hz = write_D_D(face.write().z - face.read().z);
      return new Vec3(Mth.clamp(point.x, cx - hx, cx + hx), Mth.clamp(point.y, cy - hy, cy + hy), Mth.clamp(point.z, cz - hz, cz + hz));
   }

   private static double write_D_D(double span) {
      return Math.max(0.0, Math.min(0.2, span * 0.5 - 0.15));
   }

   private ScaffoldModule.FaceRect init_aI_DataUtil_aI_DataUtil(ScaffoldModule.FaceRect trimmedFace, Vec3 eye, ScaffoldModule.MotionVectorPair optimalLine) {
      if (optimalLine == null) {
         return trimmedFace;
      }

      Vec3 nearest = nearestPointOnLine(optimalLine, MC.player.position());
      Vec3 directionToLine = MC.player.position().subtract(nearest).normalize();
      Vec3 collision = facePlaneIntersection(trimmedFace, new ScaffoldModule.RayLine(eye, optimalLine.load()));
      if (collision == null) {
         return trimmedFace;
      }

      Vec3 b = MC.player.position().add(directionToLine.scale(2.0));
      AABB crop = new AABB(collision.x, MC.player.getY() - 2.0, collision.z, b.x, MC.player.getY() + 1.0, b.z);
      ScaffoldModule.FaceRect clamped = trimmedFace.init(crop);
      return clamped.area() < 1.0E-4 ? trimmedFace : clamped;
   }

   private static int compareFaceHits(ScaffoldModule.FaceHitTarget first, ScaffoldModule.FaceHitTarget second) {
      int normal = Double.compare(init_D_aI_StreamUtil(first), init_D_aI_StreamUtil(second));
      return normal != 0 ? normal : Double.compare(first.point().y, second.point().y);
   }

   private static double init_D_aI_StreamUtil(ScaffoldModule.FaceHitTarget sample) {
      Vec3 centered = sample.point().subtract(0.5, 0.5, 0.5);
      double x = centered.x * sample.side().getStepX();
      double y = centered.y * sample.side().getStepY();
      double z = centered.z * sample.side().getStepZ();
      return x * x + y * y + z * z;
   }

   private static Vec3 init_Vec3_aI_DataUtil_aI_ByteReader(ScaffoldModule.FaceRect face, ScaffoldModule.RayLine line) {
      Vec3 intersection = facePlaneIntersection(face, line);
      List<ScaffoldModule.EdgeSegment> edges = face.edges();
      Vec3 center = face.centre();
      if (intersection != null) {
         boolean inside = true;
         Iterator bestDistance = edges.iterator();

         while (true) {
            if (bestDistance.hasNext()) {
               ScaffoldModule.EdgeSegment edge = (ScaffoldModule.EdgeSegment)bestDistance.next();
               Vec3 edgeCenter = edge.init(0.5);
               if (!(edgeCenter.subtract(intersection).dot(edgeCenter.subtract(center)) <= 0.0)) {
                  continue;
               }

               inside = false;
            }

            if (edges.isEmpty() || inside) {
               return intersection;
            }
            break;
         }
      }

      Vec3 bestPoint = null;
      double bestDistance = 1.0 / 0.0;

      for (ScaffoldModule.EdgeSegment edge : edges) {
         ScaffoldModule.SegmentIntersection pair = segmentIntersection(edge, line);
         if (pair != null) {
            double distance = pair.init().distanceToSqr(pair.load());
            if (distance < bestDistance) {
               bestDistance = distance;
               bestPoint = pair.init();
            }
         }
      }

      return bestPoint != null ? bestPoint : (intersection != null ? intersection : center);
   }

   private static Vec3 facePlaneIntersection(ScaffoldModule.FaceRect face, ScaffoldModule.RayLine line) {
      Vec3 dimensions = face.size();
      double plane;
      double anchor;
      double direction;
      if (Mth.equal(dimensions.x, 0.0)) {
         plane = face.read().x;
         anchor = line.init().x;
         direction = line.load().x;
      } else if (Mth.equal(dimensions.y, 0.0)) {
         plane = face.read().y;
         anchor = line.init().y;
         direction = line.load().y;
      } else {
         if (!Mth.equal(dimensions.z, 0.0)) {
            return null;
         }

         plane = face.read().z;
         anchor = line.init().z;
         direction = line.load().z;
      }

      if (Mth.equal(direction, 0.0)) {
         return null;
      }

      double parameter = (plane - anchor) / direction;
      return Double.isFinite(parameter) ? line.init(parameter) : null;
   }

   private static ScaffoldModule.SegmentIntersection segmentIntersection(ScaffoldModule.EdgeSegment segment, ScaffoldModule.RayLine line) {
      Vec3 firstDirection = segment.direction();
      Vec3 secondDirection = line.load();
      Vec3 delta = segment.load().subtract(line.init());
      double a = firstDirection.dot(firstDirection);
      double b = firstDirection.dot(secondDirection);
      double c = secondDirection.dot(secondDirection);
      double d = firstDirection.dot(delta);
      double e = secondDirection.dot(delta);
      double determinant = a * c - b * b;
      ScaffoldModule.BestAimHit best = null;
      if (Math.abs(determinant) > 1.0E-9) {
         best = init_aI_IoWorker_aI_IoWorker(best, segment, line, (b * e - c * d) / determinant, (a * e - b * d) / determinant);
      }

      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, 0.0, e / c);
      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, 1.0, (b + e) / c);
      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, Mth.clamp(-d / a, 0.0, 1.0), 0.0);
      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, 0.0, e / c);
      return best == null ? null : new ScaffoldModule.SegmentIntersection(best.firstPoint(), best.load());
   }

   private static ScaffoldModule.BestAimHit init_aI_IoWorker_aI_IoWorker(
      ScaffoldModule.BestAimHit best, ScaffoldModule.EdgeSegment segment, ScaffoldModule.RayLine line, double firstParameter, double secondParameter
   ) {
      if (Double.isFinite(firstParameter) && Double.isFinite(secondParameter) && !(firstParameter < -1.0E-9) && !(firstParameter > 1.000000001)) {
         double first = Mth.clamp(firstParameter, 0.0, 1.0);
         Vec3 firstPoint = segment.init(first);
         Vec3 secondPoint = line.init(secondParameter);
         double distance = firstPoint.distanceToSqr(secondPoint);
         return best != null && !(distance < best.distance() - 1.0E-9) ? best : new ScaffoldModule.BestAimHit(firstPoint, secondPoint, distance);
      } else {
         return best;
      }
   }

   private boolean hasCentreSupport(BlockState state, BlockPos pos) {
      return init_Z_BlockState_3(state, MC.level, pos, CollisionContext.of(MC.player));
   }

   static boolean init_Z_BlockState_3(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (state == null || state.isAir()) {
         return false;
      }

      if (state.isFaceSturdy(level, pos, Direction.UP, SupportType.CENTER)) {
         return true;
      }

      VoxelShape collision = state.getCollisionShape(level, pos, context);
      return collision.isEmpty() ? false : !Shapes.joinIsNotEmpty(collision.getFaceShape(Direction.UP), CENTRE_SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
   }

   private boolean isSettledSolid(BlockState state, BlockPos pos) {
      boolean supported = isSolid(state, MC.level, pos, CollisionContext.of(MC.player));
      if (!supported) {
         this.predictedCells.remove(pos);
      }

      return supported && !this.predictedCells.containsKey(pos);
   }

   static boolean isSolid(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return !state.isAir() && !state.canBeReplaced() && !state.getCollisionShape(level, pos, context).isEmpty();
   }

   public static void onPositionCorrection() {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled()) {
         scaffold.h_V();
      }
   }

   public static void recordAppliedRotation(float appliedYaw, float appliedPitch) {
      lastSentYaw = appliedYaw;
      appliedRotationTick = AutismSessionState.getInstance().getClientTick();
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled()) {
         scaffold.sentRotation = new RotationMath.Rotation(appliedYaw, appliedPitch);
         scaffold.appliedYaw = appliedYaw;
      }
   }

   private void h_V() {
      if (this.isTellyOrEagleMode()) {
         if (this.pendingPlacement != null && !isAcknowledged(this.pendingSequence, this.ackedSequence)) {
            BlockPos pendingCell = this.pendingPlacement.load();
            if (isSolid(MC.level.getBlockState(pendingCell), MC.level, pendingCell, CollisionContext.of(MC.player))) {
               this.trackHighestSequence(pendingCell, this.pendingSequence);
            }
         }

         BlockPos nearbyFooting = MC.player == null ? null : this.supportBlockUnder(MC.player);
         BlockPos previousFooting = this.groundedSupport;
         if (nearbyFooting == null && MC.player != null && previousFooting != null && load_Z_Vec3_BlockPos(MC.player.position(), previousFooting)) {
            nearbyFooting = previousFooting;
         }

         if (nearbyFooting != null
            && (
               !this.isSettledSolid(MC.level.getBlockState(nearbyFooting), nearbyFooting)
                  || !isUnacknowledgedPrediction(nearbyFooting, this.ackedSequence, this.predictions)
            )) {
            nearbyFooting = null;
         }

         this.block_V();
         this.lastPlaceTick = AutismSessionState.getInstance().getClientTick();
         this.groundedSupport = nearbyFooting;
         this.groundedSupportTick = nearbyFooting == null ? -2147483648 : AutismSessionState.getInstance().getClientTick();
      }

      this.clearReservedCells();
      this.stickyPlacement = null;
      this.stickyTick = -1;
      this.f_aH_I = 0;
      this.outOfReachTicks = 0;
      this.pinSupport = null;
      this.pinFace = null;
      this.lastRiseCell = null;
      this.riseCellGraceTicks = 0;
      this.data_BlockPos = null;
      this.f_bQ_I = -2147483648;
      this.trackedSupport = null;
      this.trackedFace = null;
      this.f_ag_I = 0;
      this.f_ah_I = 0;
      this.f_ar_I = 0;
      this.missTargetSupport = null;
      this.missTargetFace = null;
      this.missStreakCount = 0;
      this.set_BlockPos = null;
      this.f_bw_I = -2147483648;
      this.lastPosition = null;
      this.lastPositionDelta = Vec3.ZERO;
      this.airTicks = 0;
      this.f_af_I = 0;
      this.airborneBuiltRow = -2147483648;
      if (!this.isTellyOrEagleMode()) {
         this.predictions.clear();
      }

      if (this.placementState != ScaffoldModule.PlacementState.IDLE) {
         this.init_V_Str_3("position-correction");
      }

      if (this.isLiveCaptureActive() && MC != null && MC.player != null) {
         Vec3 p = MC.player.position();
         AsyncTraceLogger.init(String.format(Locale.ROOT, "[scaffold-live] t%03d setback     pos=%.2f,%.2f,%.2f - position state cleared", this.liveCaptureTick, p.x, p.y, p.z));
      }
   }

   static boolean load_Z_Vec3_BlockPos(Vec3 position, BlockPos footing) {
      if (position != null && footing != null) {
         double x = position.x - (footing.getX() + 0.5);
         double z = position.z - (footing.getZ() + 0.5);
         double height = position.y - (footing.getY() + 1);
         return x * x + z * z <= 2.25 && height >= -0.2 && height <= 2.0;
      } else {
         return false;
      }
   }

   private void releaseControls(boolean restoreSlot) {
      autismclient.util.VirtualInputDispatcher.update_V();
      if (restoreSlot
         && this.bool("switch-back")
         && this.switchBackSlot >= 0
         && MC != null
         && MC.player != null
         && !MacroExecutor.isRunning()
         && MC.player.getInventory().getSelectedSlot() != this.switchBackSlot
         && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         try {
            autismclient.util.VirtualInputDispatcher.setSlot(this.switchBackSlot);
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }
      }

      this.switchBackSlot = -1;
      this.lastRequestedSlot = -1;
      this.scaffoldSlot = -1;
      this.switchBackDelay = 0;
      this.slotSwitched = false;
      this.sentRotation = MC != null && MC.player != null ? RotationMath.ofPlayer(MC.player) : null;
      this.resetRotationState();
      this.rerollAimPitch();
      this.stickyPlacement = null;
      this.stickyTick = -1;
      this.pendingPlacement = null;
      this.pendingLine = null;
      this.pendingFallOff = null;
      this.pendingTick = -2147483648;
      this.placementState = ScaffoldModule.PlacementState.IDLE;
      this.activeGeneration = 0L;
      this.pendingHand = null;
      this.buildsPlannedCell = false;
      this.submittedCount = 0;
      this.duplicateSubmitted = false;
      this.f_aL_I = 0;
      this.clickRotation = null;
      this.submitRotation = null;
      this.pendingSequence = -1;
      this.resultSeen = false;
      this.resultConsumed = false;
      this.clickWritten = false;
      this.pendingStatus = "--";
      this.predictions.clear();
      this.predictedCells.clear();
      this.trackedLevel = null;
      this.ackedSequence = -2147483648;
      this.reconciledSequence = -2147483648;
      writtenClicks.clear();
      writtenInputFlags.set(null);
      this.inputFlagsSeen = false;
      this.wireOnGround = false;
      this.wireHorizontalCollision = false;
      this.wireHasPosition = false;
      this.f_aP_I = -2147483648;
      this.paceWaitTicks = 0;
      this.f_aV_I = -2147483648;
      this.f_aH_I = 0;
      this.outOfReachTicks = 0;
      this.f_aI_I = -2147483648;
      this.set_BlockPos = null;
      this.f_bw_I = -2147483648;
      this.lastPosition = null;
      this.lastPositionDelta = Vec3.ZERO;
      this.edgeDanger = false;
      this.fallDanger = false;
      this.lateralBrink = false;
      this.footingOwed = false;
      this.footingOwedTicks = 0;
      this.brakeMode = "--";
      this.f_y_D = 0.0 / 0.0;
      this.f_ay_I = -2147483648;
      this.count_Z = false;
      this.f_z_D = 0.0 / 0.0;
      this.f_az_I = -2147483648;
      this.flags_Z = false;
      this.level_Z = false;
      this.lastOriginalInput = null;
      this.originalInputTick = -2147483648;
      this.carryLabel = "--";
      this.pushStatus = "--";
      this.carryDistance = 0.0;
      this.waitFace = null;
      this.waitFaceTick = -2147483648;
      this.starvedTravel = 0.0;
      this.starveTick = -2147483648;
      this.f_h_Z = false;
      this.f_i_Z = false;
      this.lastPlaceYaw = 0.0F / 0.0F;
      this.open_Str = "-";
      this.airTicks = 0;
      this.f_af_I = 0;
      this.correctionHeldTicks = 0;
      this.correctionLockTicks = 0;
      this.f_aU_I = -2147483648;
      this.riseTakeoffCell = null;
      this.next_Str = "air";
      this.rising = false;
      this.heldPlacement = null;
      this.upFaceSwapCell = null;
      this.heldPlacementTick = -1;
      this.heldServedTick = -2147483648;
      this.predictionReady = false;
      this.clickLanded = false;
      this.lastDebugTick = -2147483648;
      this.appliedYaw = 0.0F / 0.0F;
      this.placeStatus = "--";
      this.dropReason = "--";
      this.paceElapsedMs = -1L;
      this.paceWaitMs = -1L;
      this.paceUsedMean = false;
      this.f_n_Z = true;
      this.f_o_Z = false;
      this.takeoffPosition = null;
      this.reservationLedger = "--";
      this.stripTrace.setLength(0);
      this.tick_Str = "--";
      this.f_aX_I = -1;
      this.f_aY_I = -1;
      this.traceLastPosition = null;
      this.reservationSnapshot = "--";
      this.placeCount = 0;
      this.settleOkCount = 0;
      this.aimHoldCount = 0;
      this.paceHoldCount = 0;
      this.f_bd_I = 0;
      this.f_be_I = 0;
      this.supportChanges = 0;
      this.get_Str = "--";
      this.resetSupportTracking(null);
      this.f_bl_I = 0;
      this.f_bm_I = 0;
      this.f_bn_I = 0;
      this.f_bo_I = 0;
      this.f_bp_I = 0;
      this.f_bq_I = 0;
      this.f_br_I = 0;
      this.f_bs_I = 0;
      this.f_bt_I = 0;
      this.f_bu_I = 0;
      this.f_bv_I = -2147483648;
      lastSentYaw = 0.0F / 0.0F;
      appliedRotationTick = -2147483648;
      this.pinSupport = null;
      this.pinFace = null;
      this.clearReservedCells();
      this.physicalClimbIntent = false;
      this.groundedFootingRow = -2147483648;
      this.footingRiseTick = -2147483648;
      this.airborneBuiltRow = -2147483648;
      this.resetInputCorrection();
      this.missTargetSupport = null;
      this.missTargetFace = null;
      this.missStreakCount = 0;
      this.placePitch = 0.0F / 0.0F;
      this.f_Z_D = 0.0 / 0.0;
      this.block_V();
      this.lastPlaceTick = -2147483648;
      this.groundedSupport = null;
      this.groundedSupportTick = -2147483648;
      this.courseLine = null;
      this.edgeGateActive = false;
      this.data_BlockPos = null;
      this.f_bQ_I = -2147483648;
      this.trackedSupport = null;
      this.trackedFace = null;
      this.f_ag_I = 0;
      this.f_ah_I = 0;
      this.f_ar_I = 0;
      this.clearHeldFace();
      this.aimRay = null;
      this.f_ai_I = 0;
      this.paceJitterMs = 0;
      this.placedStamps.clear();
      this.placeTimes.clear();
      this.lastPlaceNanos = -9223372036854775808L;
      this.placeGaps.clear();
      this.sync_ArrayDeque.clear();
      this.lastGapNanos = -9223372036854775808L;
      this.lastWirePitch = 0.0F / 0.0F;
      this.jumpStartNanos = -9223372036854775808L;
      this.writtenClickNanos = -9223372036854775808L;
      this.tick_BlockPos = null;
      this.f_ak_I = -2147483648;
      this.lastAcknowledgedCell = null;
      this.f_bY_I = 0;
      this.f_bZ_I = 0;
      this.f_m_F = 0.0F / 0.0F;
      this.recentCells.clear();
      this.wasOnGround = false;
      this.lastSneakTick = -2147483648;
      this.sneakLatchTicks = 0;
      this.sneakHeldTicks = 0;
      this.lastRiseCell = null;
      this.riseCellGraceTicks = 0;
      this.recentPlaced.clear();
      this.recentOffsets.clear();
      this.sampledSupport = null;
      this.supportSample = null;
      this.f_n_I = 0;
      this.supportSampleTick = -2147483648;
      this.lockedLine = null;
      this.courseOctant = -1;
      this.candidateOctant = -1;
      this.candidateDwell = 0;
      this.inputOctant = 0;
      this.heldLaneYaw = 0.0F / 0.0F;
      this.candidateLaneYaw = 0.0F / 0.0F;
      this.candidateYawStreak = 0;
      this.laneYawTick = -2147483648;
   }

   private static List<BlockPos> neighbourOffsets() {
      List<BlockPos> offsets = new ArrayList<>(27);

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            offsets.add(new BlockPos(x, 0, z));
            offsets.add(new BlockPos(x, -1, z));
            offsets.add(new BlockPos(x, 1, z));
         }
      }

      offsets.sort(
         Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(BlockPos.ZERO))
            .thenComparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getX)
            .thenComparingInt(Vec3i::getZ)
      );
      return List.copyOf(offsets);
   }

   record PredictedTickStep(Vec3 position, Vec3 load, int ticks) {
   }

   private record SupportHeightSample(BlockPos support, double offsetX, double load) {
   }

   @FunctionalInterface
   private interface FacePointResolver {
      Vec3 resolve(AABB var1, Vec3 var2);
   }

   private record RayLine(Vec3 init, Vec3 load) {
      Vec3 init(double parameter) {
         return this.init.add(this.load.scale(parameter));
      }
   }

   private record QuantizedPlacement(ScaffoldModule.LivePlacement placement, int tick) {
   }

   private enum EdgeDropWarning {
      NONE,
      IMMINENT;
   }

   record SupportCandidate(BlockPos support, Direction face) {
   }

   record StrafeTickBudget(int init, int load) {
   }

   record StateDecision(ScaffoldModule.PlacementState state, String reason) {
   }

   private record LivePlacement(ScaffoldModule.PlannedPlacement plan, boolean init_Z) {
   }

   private record FaceRect(Vec3 init, Vec3 load) {
      private FaceRect {
         Vec3 minimum = new Vec3(Math.min(init.x, load.x), Math.min(init.y, load.y), Math.min(init.z, load.z));
         Vec3 maximum = new Vec3(Math.max(init.x, load.x), Math.max(init.y, load.y), Math.max(init.z, load.z));
         init = minimum;
         load = maximum;
      }

      static ScaffoldModule.FaceRect init(AABB box, Direction side) {
         ScaffoldModule.FaceRect var10000;
         switch (side) {
            case EAST:
               var10000 = new ScaffoldModule.FaceRect(new Vec3(box.maxX, box.minY, box.minZ), new Vec3(box.maxX, box.maxY, box.maxZ));
               break;
            case WEST:
               var10000 = new ScaffoldModule.FaceRect(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.minX, box.maxY, box.maxZ));
               break;
            case SOUTH:
               var10000 = new ScaffoldModule.FaceRect(new Vec3(box.minX, box.minY, box.maxZ), new Vec3(box.maxX, box.maxY, box.maxZ));
               break;
            case NORTH:
               var10000 = new ScaffoldModule.FaceRect(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.maxX, box.maxY, box.minZ));
               break;
            case DOWN:
               var10000 = new ScaffoldModule.FaceRect(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.maxX, box.minY, box.maxZ));
               break;
            case UP:
               var10000 = new ScaffoldModule.FaceRect(new Vec3(box.minX, box.maxY, box.minZ), new Vec3(box.maxX, box.maxY, box.maxZ));
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      Vec3 size() {
         return this.load.subtract(this.init);
      }

      Vec3 centre() {
         return this.init.lerp(this.load, 0.5);
      }

      double area() {
         Vec3 dimensions = this.size();
         return dimensions.x * dimensions.y + dimensions.y * dimensions.z + dimensions.x * dimensions.z;
      }

      ScaffoldModule.FaceRect init(double minimumY) {
         return new ScaffoldModule.FaceRect(
            new Vec3(this.init.x, Math.max(this.init.y, minimumY), this.init.z), new Vec3(this.load.x, Math.max(this.load.y, minimumY), this.load.z)
         );
      }

      ScaffoldModule.FaceRect load(double amount) {
         Vec3 inset = this.size().scale(amount);
         return new ScaffoldModule.FaceRect(this.init.add(inset), this.load.subtract(inset));
      }

      ScaffoldModule.FaceRect init(Vec3 offset) {
         return new ScaffoldModule.FaceRect(this.init.add(offset), this.load.add(offset));
      }

      ScaffoldModule.FaceRect init(AABB box) {
         return new ScaffoldModule.FaceRect(init(this.init, box), init(this.load, box));
      }

      List<ScaffoldModule.EdgeSegment> edges() {
         Vec3 dimensions = this.size();
         Vec3 first;
         Vec3 second;
         if (Mth.equal(dimensions.x, 0.0)) {
            first = new Vec3(0.0, dimensions.y, 0.0);
            second = new Vec3(0.0, 0.0, dimensions.z);
         } else if (Mth.equal(dimensions.y, 0.0)) {
            first = new Vec3(dimensions.x, 0.0, 0.0);
            second = new Vec3(0.0, 0.0, dimensions.z);
         } else {
            if (!Mth.equal(dimensions.z, 0.0)) {
               return List.of();
            }

            first = new Vec3(0.0, dimensions.y, 0.0);
            second = new Vec3(dimensions.x, 0.0, 0.0);
         }

         List<ScaffoldModule.EdgeSegment> edges = new ArrayList<>(4);
         if (first.lengthSqr() > 1.0E-9) {
            edges.add(new ScaffoldModule.EdgeSegment(this.init, this.init.add(first)));
            edges.add(new ScaffoldModule.EdgeSegment(this.load, this.load.subtract(first)));
         }

         if (second.lengthSqr() > 1.0E-9) {
            edges.add(new ScaffoldModule.EdgeSegment(this.init, this.init.add(second)));
            edges.add(new ScaffoldModule.EdgeSegment(this.load, this.load.subtract(second)));
         }

         return edges;
      }

      private static Vec3 init(Vec3 point, AABB box) {
         return new Vec3(Mth.clamp(point.x, box.minX, box.maxX), Mth.clamp(point.y, box.minY, box.maxY), Mth.clamp(point.z, box.minZ, box.maxZ));
      }

      public Vec3 read() {
         return this.init;
      }

      public Vec3 write() {
         return this.load;
      }
   }

   enum BridgeStage {
      CONNECTOR,
      SUPPORT,
      RISER,
      READY;
   }

   record LedgePoint(Vec3 point, boolean init_Z) {
   }

   record RowLockBounds(boolean xAxis, int perpendicular, int load, int read, int write, int update, int sync) {
      boolean init(BlockPos pos, boolean rising) {
         int perp = this.xAxis ? pos.getZ() : pos.getX();
         int index = this.xAxis ? pos.getX() : pos.getZ();
         boolean onRow = perp == this.perpendicular;
         boolean ownColumn = perp == this.sync;
         if (!onRow && !ownColumn) {
            return false;
         } else {
            return pos.getY() == this.load
               ? ownColumn || index == this.write || index == this.update
               : rising && pos.getY() == this.load + 1 && (ownColumn || index == this.read || index == this.write || index == this.update);
         }
      }

      Direction init(BlockPos pos) {
         if (pos.getY() != this.load) {
            return null;
         } else {
            int perp = this.xAxis ? pos.getZ() : pos.getX();
            if (perp != this.perpendicular) {
               return null;
            } else {
               int index = this.xAxis ? pos.getX() : pos.getZ();
               if (this.xAxis) {
                  return index > this.read ? Direction.EAST : Direction.WEST;
               } else {
                  return index > this.read ? Direction.SOUTH : Direction.NORTH;
               }
            }
         }
      }
   }

   record MotionVectorPair(Vec3 init, Vec3 load) {
   }

   private record BestAimHit(Vec3 firstPoint, Vec3 load, double distance) {
   }

   private record PlacedBlockStamp(long nanos, BlockPos cell) {
   }

   private enum ForwardKeyState {
      RELEASED,
      FORWARD,
      HOLD;
   }

   record BridgeStepNode(BlockPos cell, int depth) {
   }

   record LateralCorrection(boolean active, float offset) {
   }

   record AxisDriftMetrics(double init_D, double load, double read, boolean init_Z) {
      boolean init_Z_2() {
         return this.init_Z
            && Double.isFinite(this.init_D)
            && Math.abs(this.init_D) >= 0.03
            && (this.init_D * this.read <= 0.0 || Math.abs(this.load) > Math.abs(this.read));
      }

      public double load_D() {
         return this.load;
      }

      public boolean load_Z() {
         return this.init_Z;
      }
   }

   record QueuedPlacementClick(
      int sequence,
      BlockPos cell,
      Direction face,
      Vec3 hitLocation,
      long writtenNanos,
      InteractionHand hand,
      ScaffoldModule.QueuedPacketContext context,
      ServerRotationTracker.RotationSnapshot rotationSnapshot
   ) {
      QueuedPlacementClick(int sequence, BlockPos support, Direction face, Vec3 location, long nanos) {
         this(sequence, support, face, location, nanos, InteractionHand.MAIN_HAND, null, null);
      }
   }

   enum TurnCyclePhase {
      FORWARD,
      PLACING,
      RETURNED;
   }

   private record EdgeSegment(Vec3 init, Vec3 load) {
      Vec3 direction() {
         return this.load.subtract(this.init);
      }

      Vec3 init(double parameter) {
         return this.init.add(this.direction().scale(parameter));
      }

      public Vec3 load() {
         return this.init;
      }

      public Vec3 read() {
         return this.load;
      }
   }

   record PushTarget(boolean init_Z, int init_I, int load, int read, Vec3 init_Vec3) {
   }

   private record SegmentIntersection(Vec3 init, Vec3 load) {
   }

   private record FaceAimSample(
      ScaffoldModule.FaceRect face, Vec3 point, RotationMath.Rotation rotation, BlockHitResult hit, int init_I
   ) {
   }

   record QueuedPacketContext(
      long generation,
      int ordinal,
      InteractionHand hand,
      BlockPos placed,
      BlockPos load,
      Direction face,
      RotationMath.Rotation rotation
   ) {
   }

   private record SupportDistanceCandidate(BlockPos pos, double init_D, double load, double read) {
   }

   private static final class WeakPacketKey extends WeakReference<Packet<?>> {
      private final int init;

      WeakPacketKey(Packet<?> packet) {
         super(packet);
         this.init = System.identityHashCode(packet);
      }

      WeakPacketKey(Packet<?> packet, ReferenceQueue<Packet<?>> queue) {
         super(packet, queue);
         this.init = System.identityHashCode(packet);
      }

      @Override
      public int hashCode() {
         return this.init;
      }

      @Override
      public boolean equals(Object other) {
         if (this == other) {
            return true;
         } else if (!(other instanceof ScaffoldModule.WeakPacketKey identity)) {
            return false;
         } else {
            Packet<?> packet = this.get();
            return packet != null && packet == identity.get();
         }
      }
   }

   enum PlacementAckStatus {
      WAITING,
      PLACED,
      FAILED;
   }

   private record FaceHitTarget(ScaffoldModule.FaceRect face, Vec3 point, Direction side) {
   }

   enum PlacementState {
      IDLE,
      ARMED,
      SENT,
      PREDICTED,
      RECONCILING,
      FAILED;
   }

   record PlannedPlacement(
      BlockPos support, BlockPos load, Direction face, BlockHitResult hit, RotationMath.Rotation rotation, double minHitY
   ) {
   }

   private record InputFlags(boolean init, boolean load, boolean read) {
   }

   private record KinematicsSnapshot(double octant, double residual, double read, double write, double update, boolean sprinting, boolean grounded) {
   }

   record WireClickRotation(RotationMath.Rotation init_ce_DataUtil, RotationMath.Rotation load, int tick) {
   }

   record StrafeSwitchState(ScaffoldModule.StrafeSide side, int cooldown, ScaffoldModule.StrafeSide lastPulse, int pulsesUsed) {
   }

   record PendingPlacementSeq(int sequence, BlockPos cell, BlockPos load) {
   }

   private record AimGoal(RotationMath.Rotation rotation, ScaffoldModule.AimMode mode) {
   }

   enum TellyPhase {
      IDLE,
      RUNNING,
      FORWARD_DWELL,
      RECOVERING,
      LAUNCH,
      AIMING,
      RETURNING;
   }

   enum AimMode {
      FORWARD,
      PLACEMENT,
      HOLD,
      RETURN;
   }

   record SneakHoldFlags(boolean init_Z, boolean load) {
      static final ScaffoldModule.SneakHoldFlags NONE = new ScaffoldModule.SneakHoldFlags(false, false);
   }

   enum StrafeSide {
      NONE,
      LEFT,
      RIGHT;
   }
}
