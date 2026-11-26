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
import autismclient.util.bL;
import autismclient.util.bM;
import autismclient.util.cd;
import autismclient.util.ce;
import autismclient.util.cg;
import autismclient.util.ch;
import autismclient.util.ck;
import autismclient.util.AsyncTraceLogger;
import autismclient.util.dg;
import autismclient.util.di;
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
   static final String init_Str = "Restore previous hotbar slot.";
   static final String load_Str = "Uncapped rotation and placement rates; safety stays.";
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
   private static final VoxelShape init_VoxelShape = Block.column(2.0, 0.0, 10.0);
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
   private static final double[] init_DArr = new double[]{1.5, 2.0, 2.5};
   static final int put_I = 20;
   private static final double[] load_DArr = new double[]{0.301, 0.0, -0.301};
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
   private static final List<BlockPos> init_List = read_List();
   static final int start_I = 5;
   static final int stop_I = 0;
   static final int[] init_IArr = new int[]{0, 1, 3};
   static final int next_I = init_IArr.length - 1;
   static final int prev_I = 2;
   static final int valid_I = 4;
   private static final boolean load_Z = true;
   private static final boolean read_Z = false;
   private int f_j_I = -1;
   private int f_k_I = -1;
   private int f_l_I = -1;
   private int f_m_I;
   private boolean write_Z;
   private ce.Inner_DataUtil init_ce_DataUtil;
   private ScaffoldModule.Inner_HashUtil init_aI_HashUtil;
   private final ArrayDeque<BlockPos> init_ArrayDeque = new ArrayDeque<>(4);
   private final ArrayDeque<Vec3> load_ArrayDeque = new ArrayDeque<>(5);
   private BlockPos init_BlockPos;
   private ScaffoldModule.Inner_BitSetUtil init_aI_BitSetUtil;
   private int f_n_I;
   private int f_o_I = -2147483648;
   private ScaffoldModule.Inner_HashUtil load_aI_HashUtil;
   private int f_p_I = -1;
   private int f_q_I = -1;
   private int f_r_I;
   private int f_s_I;
   private float node_F = 0.0F / 0.0F;
   private float task_F = 0.0F / 0.0F;
   private int f_t_I;
   private int f_u_I = -2147483648;
   private String read_Str = "";
   private Set<Block> init_Set = Set.of();
   private ce.Inner_DataUtil load_ce_DataUtil;
   private int f_v_I;
   private int f_w_I;
   private final dg init_dg = new dg();
   private ce.Inner_DataUtil read_ce_DataUtil;
   private float pool_F;
   private final autismclient.util.ap.Inner_TaskQueue init_ap_TaskQueue = new autismclient.util.ap.Inner_TaskQueue();
   private final RangeSetting init_RangeSetting;
   private ScaffoldRotationSmoother init_aM;
   private ScaffoldRotationSmoother.Inner_DataUtil init_aM_DataUtil;
   private boolean update_Z;
   private float index_F = 0.0F / 0.0F;
   private int f_x_I = -2147483648;
   private int f_y_I = -2147483648;
   private int f_z_I = -2147483648;
   private final Random init_Random = new Random();
   private boolean sync_Z;
   private boolean flush_Z;
   private float count_F = 74.0F;
   private ScaffoldModule.Inner_ValueSink init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.IDLE;
   private ScaffoldModule.Inner_NodeWalker init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.FORWARD;
   private ScaffoldModule.Inner_CacheEntry init_aI_CacheEntry;
   private boolean close_Z;
   private boolean open_Z;
   private final ScaffoldPlaceRateTracker init_aN = new ScaffoldPlaceRateTracker();
   private int f_A_I;
   private boolean poll_Z;
   private BlockPos load_BlockPos;
   private List<BlockPos> load_List = List.of();
   private final Item[] init_ItemArr = new Item[9];
   private ClientLevel init_ClientLevel;
   private int f_B_I = -2147483648;
   private final int[] load_IArr = new int[9];
   private final int[] read_IArr = new int[9];
   private final int[] write_IArr = new int[9];
   private int f_C_I = -1;
   private ScaffoldModule.Inner_KeyHolder init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.RELEASED;
   private boolean push_Z;
   private BlockPos read_BlockPos;
   private boolean pop_Z;
   private boolean tick_Z;
   private ScaffoldModule.Inner_b init_aI_b;
   private Input init_Input;
   private Input load_Input;
   private Input read_Input;
   private int f_D_I;
   static final int size_I = 3;
   private boolean run_Z;
   private boolean get_Z;
   private boolean set_Z;
   private boolean put_Z;
   private boolean add_Z;
   private boolean clear_Z;
   private BlockPos write_BlockPos;
   private int f_E_I;
   private boolean reset_Z;
   private boolean start_Z;
   private boolean stop_Z;
   private boolean next_Z;
   private int f_F_I;
   private int f_G_I;
   private int f_H_I;
   private int f_I_I;
   private int f_J_I;
   private int f_K_I;
   private int f_L_I;
   private double f_v_D;
   private double f_w_D;
   private float flags_F;
   private float level_F;
   private double f_x_D;
   private int f_M_I;
   private boolean prev_Z;
   private boolean valid_Z;
   private float frame_F;
   private float block_F;
   private ScaffoldModule.Inner_c init_aI_c;
   private ScaffoldModule.Inner_c load_aI_c;
   private int f_N_I;
   private int f_O_I;
   private int f_P_I;
   private int f_Q_I;
   private int f_R_I;
   private int f_S_I;
   private boolean size_Z;
   private int f_T_I;
   private int f_U_I;
   private int f_V_I;
   private boolean data_Z;
   private int f_W_I;
   private boolean value_Z;
   private boolean cache_Z;
   private boolean state_Z;
   private boolean queue_Z;
   private boolean buffer_Z;
   private boolean entry_Z;
   private boolean node_Z;
   private int f_X_I;
   private ScaffoldModule.Inner_PathHelper init_aI_PathHelper;
   private int f_Y_I;
   private BlockPos update_BlockPos;
   private int f_Z_I;
   private ce.Inner_DataUtil write_ce_DataUtil;
   private boolean task_Z;
   private int f_aa_I;
   private int f_ab_I;
   private BlockPos sync_BlockPos;
   private BlockPos flush_BlockPos;
   private int f_ac_I;
   private BlockPos close_BlockPos;
   private BlockPos open_BlockPos;
   private BlockPos poll_BlockPos;
   private BlockPos push_BlockPos;
   private boolean pool_Z;
   private Vec3 init_Vec3;
   private ScaffoldModule.Inner_DataPipe init_aI_DataPipe;
   private long pop_J;
   private static final String[] init_StrArr = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
   private int f_ad_I;
   private Vec3 load_Vec3;
   private int f_ae_I;
   private int f_af_I;
   private Vec3 read_Vec3;
   private BlockPos pop_BlockPos;
   private Direction init_Direction;
   private int f_ag_I;
   private int f_ah_I;
   private int f_ai_I;
   private int f_aj_I;
   static final long update_J = 3L;
   static final long sync_J = 50L;
   static final long flush_J = 100L;
   static final long close_J = 350L;
   static final long open_J = 250L;
   private BlockPos tick_BlockPos;
   private int f_ak_I;
   private static final int f_al_I = 8;
   private final ArrayDeque<ScaffoldModule.Inner_JsonUtil> read_ArrayDeque;
   private final ArrayDeque<Long> write_ArrayDeque;
   private final ArrayDeque<Long> update_ArrayDeque;
   private final ArrayDeque<BlockPos> sync_ArrayDeque;
   private long tick_J;
   private float chunk_F;
   private long run_J;
   private long get_J;
   private long set_J;
   private boolean index_Z;
   private BlockPos run_BlockPos;
   private Direction load_Direction;
   private int f_am_I;
   private int f_an_I;
   private int f_ao_I;
   private int f_ap_I;
   private int f_aq_I;
   private int f_ar_I;
   private final Map<BlockPos, Integer> init_Map;
   private static final int f_as_I = 10;
   private static final int f_at_I = 32;
   static final double prev_D = 0.05;
   private int f_au_I;
   private int f_av_I;
   private int f_aw_I;
   private int f_ax_I;
   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex;
   private BlockPos get_BlockPos;
   private double f_y_D;
   private int f_ay_I;
   private boolean count_Z;
   private double f_z_D;
   private int f_az_I;
   private boolean flags_Z;
   private boolean level_Z;
   private int f_aA_I;
   private Input write_Input;
   private int f_aB_I;
   private Direction read_Direction;
   private int f_aC_I;
   private double f_A_D;
   private int f_aD_I;
   private int f_aE_I;
   private boolean frame_Z;
   private int f_aF_I;
   private ScaffoldModule.Inner_TableIndex load_aI_TableIndex;
   private int f_aG_I;
   private int f_aH_I;
   private int f_aI_I;
   private static final ConcurrentLinkedQueue<ScaffoldModule.Inner_NetHelper> init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private static final AtomicReference<ScaffoldModule.Inner_TaskQueue> init_AtomicReference = new AtomicReference<>();
   private static volatile Vec3 write_Vec3;
   private static volatile Vec3 update_Vec3;
   private static volatile boolean block_Z;
   private static final ReferenceQueue<Packet<?>> init_ReferenceQueue = new ReferenceQueue<>();
   private static final Map<ScaffoldModule.Inner_StateCache, ScaffoldModule.Inner_RenderState> load_Map = new HashMap<>();
   private ScaffoldModule.Inner_TableIndex read_aI_TableIndex;
   private ScaffoldModule.Inner_HashUtil read_aI_HashUtil;
   private Vec3 sync_Vec3;
   private int f_aJ_I;
   private ScaffoldModule.Inner_SyncManager init_aI_SyncManager;
   private long put_J;
   private long add_J;
   private InteractionHand init_InteractionHand;
   private boolean chunk_Z;
   private int f_aK_I;
   private boolean table_Z;
   private int f_aL_I;
   private ce.Inner_DataUtil update_ce_DataUtil;
   private ce.Inner_DataUtil sync_ce_DataUtil;
   private int f_aM_I;
   private boolean store_Z;
   private boolean drain_Z;
   private boolean fill_Z;
   private String write_Str;
   private final ArrayDeque<ScaffoldModule.Inner_UiHelper> flush_ArrayDeque;
   private final ScaffoldFailureRecorder init_aO;
   private final AtomicBoolean init_AtomicBoolean;
   private final Map<BlockPos, Integer> read_Map;
   private ClientLevel load_ClientLevel;
   private volatile int f_aN_I;
   private int f_aO_I;
   private boolean mark_Z;
   private boolean scan_Z;
   private boolean parse_Z;
   private boolean bind_Z;
   private int f_aP_I;
   private BlockPos set_BlockPos;
   private Vec3 flush_Vec3;
   private Vec3 close_Vec3;
   private float table_F;
   private int f_aQ_I;
   private int f_aR_I;
   private int f_aS_I;
   private boolean f_a_Z;
   private float store_F;
   private float drain_F;
   private float fill_F;
   private int f_aT_I;
   private BlockPos put_BlockPos;
   private BlockPos add_BlockPos;
   private BlockPos clear_BlockPos;
   private BlockPos reset_BlockPos;
   private boolean f_b_Z;
   private String update_Str;
   private boolean f_c_Z;
   private int f_aU_I;
   private int f_aV_I;
   private boolean f_d_Z;
   private boolean f_e_Z;
   private boolean f_f_Z;
   private String sync_Str;
   private String flush_Str;
   private double f_B_D;
   private String close_Str;
   private Vec3 open_Vec3;
   private boolean f_g_Z;
   private int f_aW_I;
   private boolean f_h_Z;
   private boolean f_i_Z;
   private String open_Str;
   private BlockPos start_BlockPos;
   private boolean f_j_Z;
   private boolean f_k_Z;
   private boolean f_l_Z;
   private String poll_Str;
   private String push_Str;
   private String pop_Str;
   private final StringBuilder init_StringBuilder;
   private String tick_Str;
   private int f_aX_I;
   private int f_aY_I;
   private Vec3 poll_Vec3;
   private String run_Str;
   private int f_aZ_I;
   private int f_ba_I;
   private int f_bb_I;
   private int f_bc_I;
   private int f_bd_I;
   private int f_be_I;
   private String get_Str;
   private BlockPos stop_BlockPos;
   private BlockPos next_BlockPos;
   private int f_bf_I;
   private int f_bg_I;
   private int f_bh_I;
   private int f_bi_I;
   private int f_bj_I;
   private String set_Str;
   private String put_Str;
   private int f_bk_I;
   private String add_Str;
   private String clear_Str;
   private long clear_J;
   private long reset_J;
   private boolean f_m_Z;
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
   private String reset_Str;
   private final StringBuilder load_StringBuilder;
   private String start_Str;
   private String stop_Str;
   private BlockPos prev_BlockPos;
   private Direction write_Direction;
   private BlockPos valid_BlockPos;
   private Direction update_Direction;
   private int f_bx_I;
   private float mark_F;
   private float scan_F;
   private double f_C_D;
   private double f_D_D;
   private int f_by_I;
   private int f_bz_I;
   private float parse_F;
   private BlockPos size_BlockPos;
   private int f_bA_I;
   private boolean f_p_Z;
   private int f_bB_I;
   private int f_bC_I;
   private int f_bD_I;
   private double f_E_D;
   private double f_F_D;
   private double f_G_D;
   private double f_H_D;
   private double f_I_D;
   private int f_bE_I;
   private boolean f_q_Z;
   private boolean f_r_Z;
   private float bind_F;
   private float f_a_F;
   private float f_b_F;
   private ce.Inner_DataUtil flush_ce_DataUtil;
   static final float valid_F = 0.35F;
   private int f_bF_I;
   private BlockHitResult init_BlockHitResult;
   private float f_c_F;
   private int f_bG_I;
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
   private static final double[] read_DArr = new double[]{0.0, -0.16, 0.16, -0.28, 0.28};
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
   private int f_bN_I;
   private static int f_bO_I = -2147483648;
   private final Vec3[] init_Vec3Arr;
   private final Vec3[] load_Vec3Arr;
   private final boolean[] init_ZArr;
   private boolean f_s_Z;
   private int f_bP_I;
   private String next_Str;
   private BlockPos data_BlockPos;
   private int f_bQ_I;
   private BlockPos value_BlockPos;
   private int f_bR_I;
   private Input update_Input;
   private static float f_l_F = 0.0F / 0.0F;
   private static int f_bS_I = -2147483648;
   private int f_bT_I;
   static final int value_I = 4;
   private int f_bU_I;
   private int f_bV_I;
   private int f_bW_I;
   private final autismclient.util.bb init_bb;
   private boolean f_t_Z;
   private int f_bX_I;
   private static final double[] write_DArr = new double[]{30.0, 150.0, -150.0, -30.0};
   private int f_bY_I;
   private int f_bZ_I;
   private float f_m_F;
   private int f_ca_I;
   private int f_cb_I;
   private int f_cc_I;
   private int f_cd_I;
   private double f_Z_D;
   private static BlockPos cache_BlockPos;
   private static BlockState init_BlockState;
   private static AABB init_AABB;
   static final int cache_I = 20;
   static final float state_F = 89.3F;
   private static final double f_aa_D = 0.06;

   ScaffoldModule() {
      super("scaffold", "Scaffold", ae.load_ae, "Places blocks beneath you.");
      this.init_aI_b = ScaffoldModule.Inner_b.init_aI_b;
      this.init_aI_c = ScaffoldModule.Inner_c.NONE;
      this.load_aI_c = ScaffoldModule.Inner_c.NONE;
      this.f_P_I = -1;
      this.f_T_I = -2147483648;
      this.f_U_I = -2147483648;
      this.f_V_I = -1;
      this.data_Z = true;
      this.f_X_I = 1;
      this.f_Y_I = -2147483648;
      this.f_Z_I = -2147483648;
      this.f_ac_I = -2147483648;
      this.f_ad_I = -2147483648;
      this.load_Vec3 = Vec3.ZERO;
      this.tick_BlockPos = null;
      this.f_ak_I = -2147483648;
      this.read_ArrayDeque = new ArrayDeque<>();
      this.write_ArrayDeque = new ArrayDeque<>();
      this.update_ArrayDeque = new ArrayDeque<>();
      this.sync_ArrayDeque = new ArrayDeque<>();
      this.tick_J = -9223372036854775808L;
      this.chunk_F = 0.0F / 0.0F;
      this.run_J = -9223372036854775808L;
      this.get_J = -9223372036854775808L;
      this.set_J = -9223372036854775808L;
      this.f_an_I = -2147483648;
      this.f_ao_I = -2147483648;
      this.init_Map = new HashMap<>();
      this.f_au_I = -1;
      this.f_av_I = -2147483648;
      this.f_aw_I = -1;
      this.f_ax_I = -2147483648;
      this.f_y_D = 0.0 / 0.0;
      this.f_ay_I = -2147483648;
      this.f_z_D = 0.0 / 0.0;
      this.f_az_I = -2147483648;
      this.f_aB_I = -2147483648;
      this.f_aC_I = -2147483648;
      this.f_aD_I = -2147483648;
      this.f_aE_I = -1;
      this.f_aF_I = -2147483648;
      this.f_aG_I = -1;
      this.f_aI_I = -2147483648;
      this.f_aJ_I = -2147483648;
      this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.IDLE;
      this.f_aM_I = -1;
      this.write_Str = "--";
      this.flush_ArrayDeque = new ArrayDeque<>();
      this.init_aO = new ScaffoldFailureRecorder();
      this.init_AtomicBoolean = new AtomicBoolean();
      this.read_Map = new HashMap<>();
      this.f_aN_I = -2147483648;
      this.f_aO_I = -2147483648;
      this.f_aP_I = -2147483648;
      this.close_Vec3 = Vec3.ZERO;
      this.fill_F = 0.0F / 0.0F;
      this.update_Str = "--";
      this.f_aU_I = -2147483648;
      this.f_aV_I = -2147483648;
      this.sync_Str = "--";
      this.flush_Str = "--";
      this.close_Str = "--";
      this.open_Str = "-";
      this.poll_Str = "cam";
      this.push_Str = "--";
      this.pop_Str = "--";
      this.init_StringBuilder = new StringBuilder();
      this.tick_Str = "--";
      this.f_aX_I = -1;
      this.f_aY_I = -1;
      this.run_Str = "--";
      this.get_Str = "--";
      this.f_bf_I = -1;
      this.f_bg_I = -1;
      this.f_bh_I = -1;
      this.f_bi_I = -1;
      this.f_bj_I = -1;
      this.set_Str = "--";
      this.put_Str = "--";
      this.add_Str = "--";
      this.clear_Str = "--";
      this.clear_J = -1L;
      this.reset_J = -1L;
      this.f_n_Z = true;
      this.f_bv_I = -2147483648;
      this.f_bw_I = -2147483648;
      this.reset_Str = "?";
      this.load_StringBuilder = new StringBuilder();
      this.start_Str = "?";
      this.stop_Str = "?";
      this.mark_F = 0.0F / 0.0F;
      this.scan_F = 0.0F / 0.0F;
      this.f_bz_I = -2147483648;
      this.parse_F = 0.0F / 0.0F;
      this.f_bA_I = -2147483648;
      this.f_p_Z = true;
      this.f_H_D = 0.0 / 0.0;
      this.f_I_D = 0.0 / 0.0;
      this.f_bE_I = -2147483648;
      this.bind_F = 0.0F / 0.0F;
      this.f_b_F = 0.0F / 0.0F;
      this.f_c_F = 0.0F / 0.0F;
      this.f_bN_I = -2147483648;
      this.init_Vec3Arr = new Vec3[2];
      this.load_Vec3Arr = new Vec3[2];
      this.init_ZArr = new boolean[2];
      this.f_bP_I = -2147483648;
      this.next_Str = "air";
      this.f_bQ_I = -2147483648;
      this.init_bb = new autismclient.util.bb();
      this.f_m_F = 0.0F / 0.0F;
      this.f_cb_I = -2147483648;
      this.f_cc_I = -2147483648;
      this.f_cd_I = -2147483648;
      this.f_Z_D = 0.0 / 0.0;
      this.add(new ChoiceSetting("mode", "Mode", "Legit", "Legit", "Rage", "Telly", "Eagle").description("Choose scaffold mode.").build());
      this.add(
         new BoolSetting("remove-limits", "Remove Limits", false)
            .visibleWhen(() -> this.node_Z() && !this.index_Z())
            .description("Uncapped rotation and placement rates; safety stays.")
            .build()
      );
      this.add(
         new BoolSetting("allow-diagonal", "Allow Diagonal", false)
            .visibleWhen(this::index_Z)
            .description("Use Legit bridging for diagonal movement, retaining placement and landing checks.")
            .build()
      );
      if (!AutismLiteVariant.enabled()) {
         this.add(
            new BoolSetting("eagle-jump", "Jump", true)
               .visibleWhen(this::count_Z)
               .description("Jump while bridging. Off walks backward without jumping or sneaking.")
               .build()
         );
      }

      this.add(
         new IntSetting("imperfection", "Imperfection", 80, 0, 200, 1)
            .unit("%")
            .group("Humanization")
            .visibleWhen(() -> this.index_Z() && !this.count_Z())
            .description("Extra tops per 100 flat jumps. 100 aims for one each jump. 120 aims for a second on 20%.")
            .build()
      );
      this.init_RangeSetting = this.add(
         new RangeSetting("rotation-smoothness", "Rotation Smoothness", 20.0, 100.0, 0.0, 100.0, 1.0)
            .unit("%")
            .group("Humanization")
            .visibleWhen(this::index_Z)
            .description("Random smoothness per sweep. Turns stay bounded at every setting; higher values ease them over more time.")
            .build()
      );
      this.add(
         new IntSetting("turn-delay", "Turn Delay", 2, 0, next_I, 1)
            .group("Humanization")
            .visibleWhen(() -> this.index_Z() && !this.count_Z())
            .description("Preferred delay. Each jump turns earlier only when its placement budget requires it.")
            .build()
      );
      this.add(new BoolSetting("switch-back", "Switch Back", true).description("Restore previous hotbar slot.").build());
      this.add(new ChoiceSetting("filter-mode", "Filter", "Off", "Off", "Whitelist", "Blacklist").description("Choose filter mode.").build());
      this.add(new IntSetting("rage-blocks", "Blocks Ahead", 0, 0, 5, 1).visibleWhen(this::task_Z).description("Blocks built ahead.").build());
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
      this.load_V_Z(true);
      this.init_aO.init();
      this.read_V_12();
      this.f_au_I = -1;
      this.f_aw_I = this.init_I_16();
      this.f_ax_I = -2147483648;
      this.mark_F = 0.0F / 0.0F;
      this.scan_F = 0.0F / 0.0F;
      this.f_C_D = 0.0;
      this.f_D_D = 0.0;
      this.f_by_I = 0;
      this.flush_V_5();
   }

   @Override
   public String info() {
      return this.choice("mode");
   }

   @Override
   public void onDisable() {
      ce.Inner_DataUtil live = this.load_ce_DataUtil;
      ce.Inner_DataUtil liveRaw = this.flush_ce_DataUtil;
      float liveSent = f_l_F;
      boolean tellyLive = autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue);
      this.load_V_Z(false);
      if (live != null && MC != null && MC.player != null) {
         if (tellyLive) {
            autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, live);
         }

         this.load_ce_DataUtil = live;
         this.flush_ce_DataUtil = liveRaw != null ? liveRaw : live;
         f_l_F = liveSent;
         this.f_v_I = 5;
         this.sync_Z = true;
      }

      cg.init_V();
   }

   @Override
   public void onGameLeft() {
      this.load_V_Z(false);
      this.d_V();
      cg.load(cg.Inner_DataUtil.SCAFFOLD);
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if ("blocks".equals(settingId) || "filter-mode".equals(settingId)) {
         this.read_Str = null;
      }

      if ("switch-back".equals(settingId) && !this.bool("switch-back")) {
         this.f_j_I = -1;
         this.f_m_I = 0;
      }

      if ("mode".equals(settingId)) {
         this.load_V_Z(true);
         this.f_au_I = -1;
         this.f_aw_I = this.init_I_16();
         this.f_ax_I = -2147483648;
      }

      this.flush_V_5();
   }

   private int init_I_16() {
      if (this.pool_Z()) {
      }

      return -1;
   }

   private void flush_V_5() {
      cg.init(this.isEnabled() && this.bool("place-animation"), this.bool("animation-custom"), am.init_I_Module(this, "animation-color", -50373));
   }

   @Override
   public void preMovementTick() {
      if (AutismLiteVariant.enabled() || !ClutchModule.read_Z_20()) {
         boolean tellyTickRan = false;
         if (this.index_Z()) {
            if (!this.cache_Z && this.stop_Z() && this.cache_Z() && MC.player.onGround()) {
               BlockPos support = this.init_BlockPos_LocalPlayer(MC.player);
               if (support != null) {
                  this.init_V_LocalPlayer(MC.player, support, true);
               }
            }

            if (this.cache_Z) {
               this.cache_V();
            }

            if (this.pool_Z()) {
               this.open_V_3();
               tellyTickRan = true;
               this.size_V();
               if (!this.cache_Z) {
                  return;
               }
            }
         }

         if (this.flags_Z()) {
            this.get_V();
            this.size_BlockPos = null;
            this.put_V();
            this.pop_V();
            if (!tellyTickRan) {
               this.open_V_3();
            }

            this.push_V_2();
         } else {
            this.poll_V_3();
         }
      } else if (this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.ARMED) {
         this.index_V();
      }
   }

   static int init_I_F(float yaw) {
      int step = Math.round(Mth.wrapDegrees(yaw) / 45.0F);
      return (step % 8 + 8) % 8;
   }

   static float init_F_I(int step) {
      return Mth.wrapDegrees(step * 45.0F);
   }

   static boolean init_Z_I_6(int step) {
      return step >= 0 && (step & 1) == 1;
   }

   static int[] init_IArr_I(int held, int candidate, int dwell, float cameraYaw, boolean frozen) {
      int want = init_I_F(cameraYaw);
      if (held == -1) {
         return new int[]{want, -1, 0};
      }

      if (frozen) {
         return new int[]{held, candidate, dwell};
      }

      if (want == held) {
         return new int[]{held, -1, 0};
      }

      if (Math.abs(Mth.wrapDegrees(cameraYaw - init_F_I(held))) <= 37.5F) {
         return new int[]{held, -1, 0};
      }

      int nextDwell = want == candidate ? dwell + 1 : 1;
      return nextDwell >= 2 ? new int[]{want, -1, 0} : new int[]{held, want, nextDwell};
   }

   static float[] init_FArr_F(float held, float candidate, int streak, float requested) {
      if (Float.isNaN(held)) {
         return new float[]{requested, 0.0F / 0.0F, 0.0F};
      }

      if (requested == held) {
         return new float[]{held, 0.0F / 0.0F, 0.0F};
      }

      int nextStreak = requested == candidate ? streak + 1 : 1;
      return nextStreak >= 2 ? new float[]{requested, 0.0F / 0.0F, 0.0F} : new float[]{held, requested, nextStreak};
   }

   private void close_V_4() {
      if (MC.player != null) {
         int[] next = init_IArr_I(this.f_p_I, this.f_q_I, this.f_r_I, MC.player.getYRot(), this.k());
         this.f_p_I = next[0];
         this.f_q_I = next[1];
         this.f_r_I = next[2];
      }
   }

   private float init_F_4() {
      return this.f_p_I == -1 ? (MC.player == null ? 0.0F : MC.player.getYRot()) : init_F_I(this.f_p_I);
   }

   static float init_F_Input(Input input) {
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

   static int init_I_Input(Input input) {
      if (!load_Z_Input(input)) {
         return 0;
      }

      int steps = Math.round(init_F_Input(input) / 45.0F);
      return (steps % 8 + 8) % 8;
   }

   static int init_I_I_Input(int courseStep, Input input) {
      return init_I_I_I(courseStep, init_I_Input(input));
   }

   static int init_I_I_I(int courseStep, int octantSteps) {
      return courseStep == -1 ? -1 : ((courseStep + octantSteps) % 8 + 8) % 8;
   }

   private void init_V_Input(Input input) {
      if (load_Z_Input(input) && !this.k()) {
         this.f_s_I = init_I_Input(input);
      }
   }

   private int load_I_8() {
      return init_I_I_I(this.f_p_I, this.f_s_I);
   }

   private float load_F_2() {
      int lane = this.load_I_8();
      return lane == -1 ? this.init_F_4() : init_F_I(lane);
   }

   private Vec3 init_Vec3_3() {
      return Vec3.directionFromRotation(0.0F, this.load_F_2());
   }

   private float read_F() {
      if (this.c_Z() && this.load_I_8() == -1) {
         return this.load_ce_DataUtil != null ? this.load_ce_DataUtil.init() : MC.player.getYRot();
      }

      float requested = read_F_F(this.load_F_2());
      int tick = ck.init_ck().state_I();
      if (tick != this.f_u_I) {
         float[] next = init_FArr_F(this.node_F, this.task_F, this.f_t_I, requested);
         this.node_F = next[0];
         this.task_F = next[1];
         this.f_t_I = (int)next[2];
         this.f_u_I = tick;
      }

      return this.node_F;
   }

   static Vec3 init_Vec3_Vec3_Vec3(Vec3 previous, Vec3 current) {
      return previous == null ? Vec3.ZERO : current.subtract(previous);
   }

   private void open_V_3() {
      if (MC.player != null) {
         Vec3 pos = MC.player.position();
         this.close_Vec3 = init_Vec3_Vec3_Vec3(this.flush_Vec3, pos);
         this.flush_Vec3 = pos;
         this.f_j_Z = false;
      }
   }

   private Vec3 load_Vec3() {
      if (MC.player == null) {
         return Vec3.ZERO;
      }

      if (MC.player.onGround()) {
         return new Vec3(this.close_Vec3.x, 0.0, this.close_Vec3.z);
      }

      Vec3 delta = MC.player.getDeltaMovement();
      int tick = ck.init_ck().state_I();
      if (tick != this.f_ad_I) {
         this.f_ad_I = tick;
         Input keys = MC.player.input == null ? null : MC.player.input.keyPresses;
         boolean directional = load_Z_Input(keys) || this.f_aB_I == tick - 1 && load_Z_Input(this.write_Input);
         boolean geometric = directional && this.poll(true, false);
         boolean cadence = directional && this.open(true, false);
         boolean coast = geometric && this.level_Z && !cadence;
         this.load_Vec3 = coast ? Vec3.ZERO : init_Vec3_Vec3_5(delta, directional, geometric || cadence, keys != null && keys.shift());
      }

      return delta.add(this.load_Vec3);
   }

   static Vec3 init_Vec3_Vec3_5(Vec3 velocity, boolean directional, boolean braking, boolean sneaking) {
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

   private void poll_V_3() {
      if (!this.cache_Z()) {
         this.init_aI_HashUtil = null;
         this.g_V();
      } else if (!BlinkPacketManager.load_Z()) {
         this.write_Z = false;
         InteractionHand hand = this.init_InteractionHand();
         if (hand == null) {
            if (this.write_Z) {
               this.f_V();
            } else {
               this.g_V();
            }
         } else {
            this.f_V();
            ItemStack stack = MC.player.getItemInHand(hand);
            if (this.init_Z_ItemStack_4(stack)) {
               BlockPos base = this.init_BlockPos_5();
               if (base != null) {
                  BlockPos step = this.load_BlockPos();
                  int ahead = step == null ? 0 : Math.max(0, Math.min(5, this.integer("rage-blocks")));
                  if (bM.init(this.id())) {
                     for (BlockPos cell : init_List_BlockPos_2(base, step, ahead, this::open)) {
                        this.init_Z_BlockPos_7(cell, hand, stack);
                     }
                  }
               }
            }
         }
      }
   }

   static List<BlockPos> init_List_BlockPos_2(BlockPos base, BlockPos step, int ahead, Predicate<BlockPos> solid) {
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

   private BlockPos init_BlockPos_5() {
      Vec3 vec = MC.player.position().add(MC.player.getDeltaMovement()).add(0.0, -0.75, 0.0);
      BlockPos cell = BlockPos.containing(vec.x, vec.y, vec.z);
      int footY = MC.player.blockPosition().getY();
      if (cell.getY() >= footY) {
         cell = new BlockPos(cell.getX(), footY - 1, cell.getZ());
      }

      return MC.level.isOutsideBuildHeight(cell) ? null : cell;
   }

   private BlockPos load_BlockPos() {
      if (MC.options == null) {
         return null;
      }

      Vec3 look = Vec3.directionFromRotation(0.0F, MC.player.getYRot());
      double x = 0.0;
      double z = 0.0;
      if (init_Z_KeyMapping(MC.options.keyUp)) {
         x += look.x;
         z += look.z;
      }

      if (init_Z_KeyMapping(MC.options.keyDown)) {
         x -= look.x;
         z -= look.z;
      }

      if (init_Z_KeyMapping(MC.options.keyLeft)) {
         x += look.z;
         z -= look.x;
      }

      if (init_Z_KeyMapping(MC.options.keyRight)) {
         x -= look.z;
         z += look.x;
      }

      return init_BlockPos_D_D(x, z);
   }

   static BlockPos init_BlockPos_D_D(double x, double z) {
      if (x * x + z * z <= 1.0E-8) {
         return null;
      }

      double yaw = Math.toRadians(init_F_I(init_I_F((float)Math.toDegrees(Math.atan2(-x, z)))));
      int stepX = (int)Math.round(-Math.sin(yaw));
      int stepZ = (int)Math.round(Math.cos(yaw));
      return stepX == 0 && stepZ == 0 ? null : new BlockPos(stepX, 0, stepZ);
   }

   private boolean init_Z_BlockPos_7(BlockPos cell, InteractionHand hand, ItemStack stack) {
      if (cell != null && !MC.level.isOutsideBuildHeight(cell)) {
         BlockState state = MC.level.getBlockState(cell);
         if (state.canBeReplaced() && !this.init_Z_BlockState_BlockPos(state, cell)) {
            if (!load_Z_AABB(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), cell)) {
               return false;
            }

            Direction side = this.init_Direction_BlockPos(cell);
            if (side == null) {
               return false;
            }

            BlockPos neighbour = cell.relative(side);
            Vec3 hitPos = init_Vec3_AABB_Direction(init_AABB_BlockPos(neighbour), side.getOpposite());
            Vec3 eye = MC.player.getEyePosition();
            double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
            if (eye.distanceToSqr(hitPos) > reach * reach) {
               return false;
            }

            BlockHitResult hit = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);
            if (al.init_Z_HitResult(hit, hand, this.id())) {
               return false;
            }

            ScaffoldModule.Inner_TableIndex target = new ScaffoldModule.Inner_TableIndex(neighbour, cell, side.getOpposite(), hit, ce.init(hitPos, eye), cell.getY());
            return this.init_Z_aI_TableIndex_3(target, hand, stack);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private Direction init_Direction_BlockPos(BlockPos cell) {
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

   private void push_V_2() {
      if (!this.cache_Z()) {
         this.init_aI_HashUtil = null;
         this.flush_Z = false;
         this.load_aI_HashUtil = null;
         this.init_V_Z_6(false);
         this.g_V();
      } else {
         this.f_l_Z = false;
         this.f_h_Z = false;
         this.f_i_Z = false;
         this.f_m_Z = false;
         if (this.poll_Z_3()) {
            this.add_Str = "attempt-" + this.init_aI_SyncManager.name().toLowerCase(Locale.ROOT);
            this.init_V_Str_aI_TableIndex("real-wait", this.read_aI_TableIndex);
            if (this.read_aI_TableIndex != null) {
               this.init_ce_DataUtil_ce_DataUtil(this.init_ce_DataUtil_aI_TableIndex(this.read_aI_TableIndex));
            } else {
               this.next_V();
            }
         } else if (BlinkPacketManager.load_Z()) {
            this.add_Str = "blink-action-only";
            this.init_V_Str_aI_TableIndex("real-wait", this.read_aI_TableIndex);
            this.g_V();
         } else {
            this.close_BlockPos();
            ItemStack planningStack = this.init_ItemStack_2();
            ScaffoldModule.Inner_TableIndex target = this.init_aI_TableIndex_ItemStack(planningStack);
            if (target == null) {
               this.f_ai_I++;
               this.write_V_aI_TableIndex(null);
               this.add_Str = "plan";
               this.init_V_Str_aI_TableIndex("no-target " + this.reset_Str, null);
               this.next_V();
               this.g_V();
            } else {
               this.write_Z = false;
               InteractionHand hand = this.init_InteractionHand();
               if (hand == null) {
                  if (this.write_Z) {
                     this.f_V();
                  } else {
                     this.g_V();
                  }

                  this.write_V_aI_TableIndex(target);
                  this.add_Str = "hand";
                  this.init_V_Str_aI_TableIndex("hand-switch", target);
                  this.init_ce_DataUtil_ce_DataUtil(target.init_ce_DataUtil());
               } else {
                  this.f_V();
                  ItemStack stack = MC.player.getItemInHand(hand);
                  target = this.init_aI_TableIndex_ItemStack(stack);
                  if (target == null) {
                     this.f_ai_I++;
                     this.write_V_aI_TableIndex(null);
                     this.add_Str = "plan";
                     this.init_V_Str_aI_TableIndex("no-target-2 " + this.reset_Str, null);
                     this.next_V();
                  } else {
                     this.f_ai_I = 0;
                     if (this.load_Z_aI_TableIndex(target)) {
                        this.load_aI_TableIndex = null;
                        this.write_V_aI_TableIndex(null);
                        boolean droppedTopHasASide = false;
                        if (target.init_Direction() == Direction.UP) {
                           ScaffoldModule.Inner_TableIndex viaSide = this.init_aI_TableIndex_BlockPos(
                              target.load(),
                              target.init_BlockPos(),
                              this.load_Vec3_aI_HashUtil(this.init_aI_HashUtil),
                              stack,
                              this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load()
                           );
                           if (viaSide != null && !this.load_Z_aI_TableIndex(viaSide)) {
                              target = viaSide;
                              this.f_bt_I++;
                              droppedTopHasASide = true;
                           }
                        }

                        if (!droppedTopHasASide) {
                           if (!init_Z_Z_Z(MC.player.onGround(), this.queue_Z())) {
                              this.add_Str = "drop-" + this.clear_Str;
                              this.init_V_Str_aI_TableIndex("rise-drop", target);
                              this.init_ce_DataUtil_ce_DataUtil(this.write_ce_DataUtil());
                              return;
                           }

                           this.read_V_BlockPos(target.load());
                           ScaffoldModule.Inner_TableIndex replanned = this.init_aI_TableIndex_ItemStack(stack);
                           if (replanned == null || this.load_Z_aI_TableIndex(replanned)) {
                              this.add_Str = "drop-" + this.clear_Str;
                              this.init_V_Str_aI_TableIndex("rise-drop", target);
                              this.init_ce_DataUtil_ce_DataUtil(this.write_ce_DataUtil());
                              return;
                           }

                           target = replanned;
                           this.f_bt_I++;
                        }
                     }

                     this.write_V_aI_TableIndex(target);
                     this.f_c_F = target.init_ce_DataUtil().load();
                     ScaffoldModule.Inner_TextFormat wireClick = this.init_aI_TextFormat();
                     ce.Inner_DataUtil clickRotation = wireClick == null ? null : wireClick.load();
                     boolean rayLands = clickRotation != null && this.load_Z_aI_TableIndex_ce_DataUtil(target, clickRotation);
                     ScaffoldModule.Inner_TableIndex laneRay = null;
                     if (clickRotation != null && !rayLands) {
                        laneRay = this.init_aI_TableIndex_aI_TableIndex_BlockHitResult(target, this.init_BlockHitResult);
                        if (laneRay != null && this.load_Z_aI_TableIndex(laneRay)) {
                           laneRay = null;
                        }
                     }

                     ScaffoldModule.Inner_TableIndex clickable = rayLands ? target : laneRay;
                     boolean paceFreesPitch = false;
                     if (clickable != null && this.init_Z_aI_TableIndex_ce_DataUtil(clickable, clickRotation)) {
                        paceFreesPitch = true;
                        this.f_aV_I = ck.init_ck().state_I();
                     }

                     this.init_ce_DataUtil_ce_DataUtil_Z(this.init_ce_DataUtil_aI_TableIndex(target), load_Z_Z_Z(rayLands || laneRay != null, paceFreesPitch));
                     if (wireClick == null) {
                        this.add_Str = "wire-wait";
                        this.init_V_Str_aI_TableIndex("aim-hold miss=no-wire", target);
                     } else {
                        if (!rayLands) {
                           if (laneRay == null) {
                              if (this.pop(target)) {
                                 this.f_ai_I = Math.max(this.f_ai_I, 1);
                              }

                              this.init_V_aI_TableIndex_ce_DataUtil(target, clickRotation);
                              String waitCode = this.init_Str_aI_TableIndex(target);
                              this.init_V_aI_TableIndex_Str(target, waitCode);
                              this.add_Str = "aim-" + waitCode;
                              this.init_V_Str_aI_TableIndex("aim-hold" + this.write_Str_aI_TableIndex(target), target);
                              return;
                           }

                           target = laneRay;
                           this.write_V_aI_TableIndex(target);
                        }

                        if (clickRotation != null) {
                           float emittedPitch = clickRotation.load();
                           if (!read_Z_F(emittedPitch)) {
                              this.add_Str = "pveto-" + (int)emittedPitch;
                              this.init_V_Str_aI_TableIndex("pitch-veto", target);
                              return;
                           }

                           if (this.init_Z_ce_DataUtil(clickRotation)) {
                              if (++this.f_bY_I > 2) {
                                 this.read_V_BlockPos(target.load());
                                 this.load_aI_TableIndex = null;
                                 this.f_bY_I = 0;
                              }

                              float yawResidual = this.load_I_8() == -1 ? 0.0F / 0.0F : update_F_F_F(this.load_F_2(), clickRotation.init());
                              this.add_Str = "yawoff-" + (Float.isNaN(yawResidual) ? "?" : String.format(Locale.ROOT, "%.1f", yawResidual));
                              this.init_V_Str_aI_TableIndex("yaw-veto", target);
                              return;
                           }

                           this.f_bY_I = 0;
                           float goalErr = Math.abs(Mth.wrapDegrees(target.init_ce_DataUtil().init() - clickRotation.init()));
                           if (target.init_Direction().getAxis().isHorizontal() && init_Z_F_F(target.init_ce_DataUtil().init(), clickRotation.init())) {
                              boolean closing = update_Z_F_F(goalErr, this.f_m_F);
                              this.f_m_F = goalErr;
                              this.f_bZ_I = closing ? 0 : this.f_bZ_I + 1;
                              if (this.f_bZ_I > 2) {
                                 this.read_V_BlockPos(target.load());
                                 this.load_aI_TableIndex = null;
                                 this.f_bZ_I = 0;
                                 this.f_m_F = 0.0F / 0.0F;
                              }

                              this.add_Str = "goaloff-" + (int)goalErr;
                              this.init_V_Str_aI_TableIndex("yaw-veto", target);
                              return;
                           }

                           this.f_bZ_I = 0;
                           this.f_m_F = 0.0F / 0.0F;
                           float clickPitchStep = wireClick.init_ce_DataUtil() == null
                              ? 0.0F
                              : Math.abs(clickRotation.load() - wireClick.init_ce_DataUtil().load());
                           boolean descending = !MC.player.onGround() && MC.player.getDeltaMovement().y < 0.0;
                           if (!this.block_Z() && !descending && clickPitchStep > 10.0F) {
                              this.add_Str = "pstep-" + (int)clickPitchStep;
                              this.init_V_Str_aI_TableIndex("pitch-step", target);
                              return;
                           }
                        }

                        this.f_ag_I = 0;
                        this.f_ah_I = 0;
                        this.f_ar_I = 0;
                        int tick = ck.init_ck().state_I();
                        if (!this.block_Z() && tick == this.f_z_I) {
                           this.add_Str = "sametick";
                           this.init_V_Str_aI_TableIndex("pace-hold", target);
                        } else {
                           boolean pitchFreed = this.f_aV_I == tick;
                           if (paceFreesPitch || pitchFreed || this.init_Z_aI_TableIndex_ce_DataUtil(target, clickRotation)) {
                              if (target.load().getY() > target.init_BlockPos().getY() || this.write_Z_BlockPos(target.load())) {
                                 this.tick_BlockPos = target.load().immutable();
                                 this.f_ak_I = ck.init_ck().state_I();
                              }

                              this.add_Str = !paceFreesPitch && pitchFreed
                                 ? "pace-pfree"
                                 : "pace-" + this.clear_J + (this.f_m_Z ? "/imean" : "/" + this.reset_J + "ms");
                              this.init_V_Str_aI_TableIndex("pace-hold", target);
                           } else if (BlinkPacketManager.load_Z()) {
                              this.add_Str = "blink-action-only";
                              this.init_V_Str_aI_TableIndex("real-wait", target);
                           } else {
                              Vec3 publishedEye = update_Vec3;
                              double publishedReach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
                              BlockHitResult publishedRay = publishedEye == null
                                 ? null
                                 : init_BlockHitResult_Vec3(publishedEye, clickRotation, publishedReach, MC.level, MC.player);
                              if (publishedRay != null
                                 && this.init_BlockHitResult != null
                                 && publishedRay.getBlockPos().equals(this.init_BlockHitResult.getBlockPos())
                                 && publishedRay.getDirection() == this.init_BlockHitResult.getDirection()) {
                                 this.read_aI_TableIndex = target;
                                 this.read_aI_HashUtil = this.init_aI_HashUtil;
                                 this.sync_Vec3 = this.read_Vec3_aI_HashUtil(this.init_aI_HashUtil);
                                 this.f_aJ_I = tick;
                                 this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.ARMED;
                                 this.add_J = load_J_J(this.add_J);
                                 this.put_J = this.add_J;
                                 this.init_InteractionHand = hand;
                                 if (this.index_Z()) {
                                    this.f_C_I = MC.player.getInventory().getSelectedSlot();
                                    this.read_I_I(this.f_C_I);
                                 }

                                 this.chunk_Z = this.load_Z_BlockHitResult_aI_TableIndex(this.init_BlockHitResult, target);
                                 this.f_aK_I = 0;
                                 this.table_Z = false;
                                 this.f_aL_I = 0;
                                 this.update_ce_DataUtil = clickRotation;
                                 this.sync_ce_DataUtil = wireClick.init_ce_DataUtil();
                                 this.f_aM_I = -1;
                                 this.store_Z = false;
                                 this.drain_Z = false;
                                 this.fill_Z = false;
                                 this.write_Str = "queued";
                                 this.f_z_I = this.f_aJ_I;
                                 this.set_J = -9223372036854775808L;
                                 this.f_aj_I = this.init_Random.nextInt(25);
                                 this.f_l_Z = true;
                                 this.add_Str = "ok";
                                 autismclient.util.as.init_V_J(this.put_J);
                                 ((AutismMinecraftAccessor)MC).autism$setRightClickDelay(0);
                                 this.init_V_Str_aI_TableIndex("PLACE", target);
                              } else {
                                 this.add_Str = "wire-pose";
                                 this.init_V_Str_aI_TableIndex("real-wait", target);
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

   private void pop_V() {
      this.table_V();
      this.set_V();
      ScaffoldModule.Inner_TableIndex pending = this.read_aI_TableIndex;
      if (pending != null && MC.level != null && MC.player != null) {
         int tick = ck.init_ck().state_I();
         int age = Math.max(0, tick - this.f_aJ_I);
         BlockPos cell = pending.load();
         boolean solid = this.init_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell);
         ScaffoldModule.Inner_ConfigStore decision = init_aI_ConfigStore_aI_SyncManager(
            this.init_aI_SyncManager, age, this.store_Z, this.drain_Z, solid, load_Z_I_I(this.f_aM_I, this.f_aN_I)
         );
         if (decision.init_aI_SyncManager() == ScaffoldModule.Inner_SyncManager.FAILED) {
            String reason = "use".equals(decision.init_Str()) ? "use-" + this.write_Str : decision.init_Str();
            this.init_V_Str_3(reason);
         } else {
            this.init_aI_SyncManager = decision.init_aI_SyncManager();
         }

         if (this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.RECONCILING) {
            this.add_Str = "reconcile";
            this.init_V_Str_aI_TableIndex("real-wait", pending);
         } else if (this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.PREDICTED && solid) {
            this.read_Map.remove(cell);
            this.init_V_BlockPos_3(cell, this.read_aI_HashUtil, this.sync_Vec3);
            this.size_BlockPos = cell;
            if (!MC.player.onGround()) {
               this.f_af_I++;
               if (!cell.equals(this.reset_BlockPos)) {
                  this.f_cd_I = Math.max(this.f_cd_I, cell.getY());
               }
            }

            this.init_V_I_BlockPos(this.f_aM_I, cell);
            cg.init(cell);
            this.add_Str = "ok";
            this.init_V_Str_aI_TableIndex("settle-ok", pending);
            this.run_V();
         } else if (this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.FAILED) {
            this.add_Str = this.write_Str;
            this.init_V_Str_aI_TableIndex("real-miss", pending);
            this.run_V();
         }
      } else if (pending == null && this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE) {
         this.run_V();
      }
   }

   private boolean poll_Z_3() {
      return init_Z_aI_SyncManager(this.init_aI_SyncManager);
   }

   private void init_V_Str_3(String reason) {
      if (this.read_aI_TableIndex != null && MC != null && MC.level != null) {
         BlockPos cell = this.read_aI_TableIndex.load();
         boolean predicted = this.level_Z() ? load_Z_BlockState(MC.level.getBlockState(cell), MC.level, cell, CollisionContext.of(MC.player)) : this.open(cell);
         if (predicted) {
            this.init_V_BlockPos_I_2(cell, this.f_aM_I);
         }
      }

      this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.FAILED;
      this.write_Str = reason == null ? "failed" : reason;
      if (this.load_aI_TableIndex != null && this.read_aI_TableIndex != null && this.load_aI_TableIndex.load().equals(this.read_aI_TableIndex.load())) {
         this.load_aI_TableIndex = null;
      }
   }

   private void run_V() {
      this.read_aI_TableIndex = null;
      this.read_aI_HashUtil = null;
      this.sync_Vec3 = null;
      this.f_aJ_I = -2147483648;
      this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.IDLE;
      this.put_J = 0L;
      this.init_InteractionHand = null;
      this.chunk_Z = false;
      this.f_aK_I = 0;
      this.table_Z = false;
      this.f_aL_I = 0;
      this.update_ce_DataUtil = null;
      this.sync_ce_DataUtil = null;
      this.f_aM_I = -1;
      this.store_Z = false;
      this.drain_Z = false;
      this.fill_Z = false;
      this.write_Str = "--";
   }

   private void init_V_I_BlockPos(int sequence, BlockPos cell) {
      this.init_V_I_3(sequence, cell, null);
   }

   private void init_V_I_3(int sequence, BlockPos cell, BlockPos supportingCell) {
      if (sequence >= 0 && cell != null && !load_Z_I_I(sequence, this.f_aN_I)) {
         this.flush_ArrayDeque.addLast(new ScaffoldModule.Inner_UiHelper(sequence, cell.immutable(), supportingCell == null ? null : supportingCell.immutable()));

         while (this.flush_ArrayDeque.size() > 64) {
            this.flush_ArrayDeque.removeFirst();
         }
      }
   }

   private void init_V_BlockPos_I_2(BlockPos cell, int sequence) {
      if (cell != null) {
         int highest = this.read_Map.merge(cell.immutable(), sequence, (oldSequence, newSequence) -> Math.max(oldSequence, newSequence));
         if (init_Z_I_I_3(highest, this.f_aN_I)) {
            this.read_Map.remove(cell);
         }
      }
   }

   private void get_V() {
      ClientLevel level = MC == null ? null : MC.level;
      if (init_Z_Obj_Obj(this.load_ClientLevel, level)) {
         this.load_ClientLevel = level;
         this.flush_ArrayDeque.clear();
         this.read_Map.clear();
         this.f_aN_I = -2147483648;
         this.f_aO_I = -2147483648;
         init_ConcurrentLinkedQu.clear();
         if (this.read_aI_TableIndex != null || this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE) {
            this.run_V();
         }

         this.fill_V();
         this.f_cc_I = -2147483648;
      }
   }

   static boolean init_Z_Obj_Obj(Object previous, Object current) {
      return previous != current;
   }

   private void set_V() {
      int acknowledged = this.f_aN_I;
      if (acknowledged != -2147483648 && acknowledged > this.f_aO_I && MC.level != null) {
         this.f_aO_I = acknowledged;
         Set<BlockPos> rejected = new HashSet<>();
         Set<BlockPos> confirmed = new HashSet<>();

         for (ScaffoldModule.Inner_UiHelper predicted : this.flush_ArrayDeque) {
            if (load_Z_I_I(predicted.init_I(), acknowledged)) {
               BlockState state = MC.level.getBlockState(predicted.init_BlockPos());
               boolean solid = predicted.load() == null
                  ? this.init_Z_BlockState_BlockPos(state, predicted.init_BlockPos())
                  : load_Z_BlockState(state, MC.level, predicted.init_BlockPos(), CollisionContext.of(MC.player));
               if (!solid) {
                  rejected.add(predicted.init_BlockPos());
                  if (predicted.load() != null) {
                     this.init_V_Str("ack-reconciled-nonsupport", predicted.init_BlockPos(), predicted.init_I());
                  }
               } else {
                  confirmed.add(predicted.init_BlockPos());
               }
            }
         }

         Set<BlockPos> brokenChain = init_Set_List(new ArrayList<>(this.flush_ArrayDeque), rejected, confirmed);
         Iterator<ScaffoldModule.Inner_UiHelper> iterator = this.flush_ArrayDeque.iterator();

         while (iterator.hasNext()) {
            ScaffoldModule.Inner_UiHelper predicted = iterator.next();
            if (brokenChain.contains(predicted.init_BlockPos()) && !load_Z_I_I(predicted.init_I(), acknowledged)) {
               this.init_V_BlockPos_I_2(predicted.init_BlockPos(), predicted.init_I());
               this.init_V_BlockPos_4(predicted.init_BlockPos());
            }

            if (load_Z_I_I(predicted.init_I(), acknowledged)) {
               iterator.remove();
               this.read_Map.remove(predicted.init_BlockPos());
               if (brokenChain.contains(predicted.init_BlockPos())) {
                  this.load_V_BlockPos(predicted.init_BlockPos());
                  this.init_V_BlockPos_4(predicted.init_BlockPos());
               }
            }
         }

         if (this.read_aI_TableIndex != null && brokenChain.contains(this.read_aI_TableIndex.init_BlockPos())) {
            autismclient.util.as.update_V();
            this.init_V_Str_3("ack-support-air");
         }
      }
   }

   static Set<BlockPos> init_Set_List_Set(List<ScaffoldModule.Inner_UiHelper> predictions, Set<BlockPos> rejected) {
      return init_Set_List(predictions, rejected, Set.of());
   }

   static Set<BlockPos> init_Set_List(List<ScaffoldModule.Inner_UiHelper> predictions, Set<BlockPos> rejected, Set<BlockPos> confirmed) {
      Set<BlockPos> broken = new HashSet<>(rejected);

      boolean changed;
      do {
         changed = false;

         for (ScaffoldModule.Inner_UiHelper predicted : predictions) {
            if (predicted.load() != null && broken.contains(predicted.load()) && !confirmed.contains(predicted.init_BlockPos())) {
               changed |= broken.add(predicted.init_BlockPos());
            }
         }
      } while (changed);

      return broken;
   }

   private void init_V_BlockPos_4(BlockPos cell) {
      if (this.init_aI_DataPipe != null
         && (cell.equals(this.init_aI_DataPipe.init_aI_TableIndex().init_BlockPos()) || cell.equals(this.init_aI_DataPipe.init_aI_TableIndex().load()))) {
         this.init_aI_DataPipe = null;
      }

      if (cell.equals(this.close_BlockPos)) {
         this.close_BlockPos = null;
         this.start_Z = false;
      }

      if (cell.equals(this.push_BlockPos)) {
         this.data_V();
      }

      this.data_Z = false;
   }

   private void load_V_I_2(int acknowledged) {
      Iterator<Entry<BlockPos, Integer>> iterator = this.read_Map.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<BlockPos, Integer> entry = iterator.next();
         int highestSequence = entry.getValue();
         if (init_Z_I_I_3(highestSequence, acknowledged)) {
            iterator.remove();
         }
      }
   }

   static boolean init_Z_I_I_3(int highestSequence, int acknowledged) {
      return highestSequence >= 0 && load_Z_I_I(highestSequence, acknowledged);
   }

   private void load_V_BlockPos(BlockPos cell) {
      if (cell != null) {
         this.init_ArrayDeque.removeIf(cell::equals);
         this.load_ArrayDeque.clear();
         if (this.load_aI_TableIndex != null && cell.equals(this.load_aI_TableIndex.load())) {
            this.load_aI_TableIndex = null;
         }

         this.load_aI_HashUtil = null;
         this.set_BlockPos = null;
         this.f_aU_I = -2147483648;
         this.add_Str = "ack-air";
      }
   }

   private void put_V() {
      if (MC.player != null && !MC.player.onGround()) {
         this.f_ae_I++;
      } else {
         this.f_ae_I = 0;
         this.f_af_I = 0;
         this.read_Vec3 = MC.player == null ? null : MC.player.position();
      }
   }

   private String init_Str_8() {
      if (MC.player != null && !MC.player.onGround() && this.read_Vec3 != null) {
         Vec3 now = MC.player.position();
         double dx = now.x - this.read_Vec3.x;
         double dz = now.z - this.read_Vec3.z;
         return String.format(Locale.ROOT, "carry=%.2f/%.2f", Math.sqrt(dx * dx + dz * dz), 0.8);
      } else {
         return "carry=--";
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (MC.player != null
         && target.init_Direction().getAxis().isHorizontal()
         && this.f_ag_I >= 3
         && this.f_ar_I == 0
         && target.init_BlockPos().equals(this.pop_BlockPos)
         && target.init_Direction() == this.init_Direction) {
         this.start_Str = "miss-direct";
         ce.Inner_DataUtil direct = ce.init(target.init_BlockHitResult().getLocation(), MC.player.getEyePosition());
         return new ce.Inner_DataUtil(direct.init(), load_F_F_2(direct.load()));
      } else {
         return target.init_ce_DataUtil();
      }
   }

   static boolean init_Z_Vec3_7(Vec3 eye, Vec3 hit, BlockPos support, Direction face) {
      double eyePast = init_D_Vec3(eye, support, face);
      double hitPast = init_D_Vec3(hit, support, face);
      return eyePast > 0.0 ? hitPast > 0.01 && hitPast < eyePast : hitPast < -0.01 && hitPast > eyePast;
   }

   private void init_V_aI_TableIndex_ce_DataUtil(ScaffoldModule.Inner_TableIndex target, ce.Inner_DataUtil clickRotation) {
      if (!target.init_BlockPos().equals(this.pop_BlockPos) || target.init_Direction() != this.init_Direction) {
         this.pop_BlockPos = target.init_BlockPos();
         this.init_Direction = target.init_Direction();
         this.f_ag_I = 0;
         this.f_ar_I = 0;
         this.f_ah_I = 0;
      }

      boolean occluded = this.init_BlockHitResult != null
         && !this.init_BlockHitResult.getBlockPos().equals(target.init_BlockPos())
         && this.open(this.init_BlockHitResult.getBlockPos())
         && MC.player != null
         && init_Z_Vec3_7(MC.player.getEyePosition(), this.init_BlockHitResult.getLocation(), target.init_BlockPos(), target.init_Direction());
      boolean blank = this.init_BlockHitResult == null;
      boolean aimArrived = clickRotation == null || !init_Z_F_F(target.init_ce_DataUtil().init(), clickRotation.init());
      boolean pastAndBlank = blank && MC.player != null && this.sync(target) && aimArrived;
      if (occluded || pastAndBlank) {
         if (++this.f_ar_I >= 2) {
            this.read_V_BlockPos(target.load());
            this.load_aI_TableIndex = null;
            if (target.init_BlockPos().equals(this.prev_BlockPos) && target.init_Direction() == this.write_Direction) {
               this.prev_BlockPos = null;
               this.write_Direction = null;
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

   private boolean init_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (MC.player != null && !target.init_Direction().getAxis().isVertical()) {
         float yaw = target.init_ce_DataUtil().init();
         Vec3 lead = this.load_Vec3();
         if (lead.horizontalDistanceSqr() < 1.0E-4 && this.init_aI_HashUtil != null) {
            lead = new Vec3(this.init_aI_HashUtil.load().x, 0.0, this.init_aI_HashUtil.load().z).normalize().scale(0.05);
         }

         Vec3 eye = MC.player.getEyePosition();

         for (int step = 1; step <= 2; step++) {
            Vec3 projected = eye.add(lead.scale(step));
            if (load_Z_Vec3(projected, target.init_BlockPos(), target.init_Direction(), yaw, true)) {
               float solve = init_F_Vec3(projected, target.init_BlockPos(), target.init_Direction(), yaw);
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

   static boolean init_Z_F_F(float goalYaw, float emittedYaw) {
      return Math.abs(Mth.wrapDegrees(goalYaw - emittedYaw)) > 20.0F;
   }

   private boolean load_Z_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      Vec3 lead = this.load_Vec3();

      for (int step = 0; step <= 2; step++) {
         if (!update_Z_F(init_F_Vec3(eye.add(lead.scale(step)), support, face, yaw))) {
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
         Vec3 lead = this.load_Vec3();
         int horizon = Math.max(2, init_I_I_3(this.f_bT_I));

         for (int step = 0; step <= horizon; step++) {
            if (!init_Z_Vec3_4(eye.add(lead.scale(step)), support, face, this::init_Z_BlockPos_4)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_BlockPos_4(BlockPos pos) {
      return MC.level != null && pos != null ? MC.level.getBlockState(pos).isCollisionShapeFullBlock(MC.level, pos) : false;
   }

   static float init_F_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return init_F_Vec3_2(eye, init_AABB_BlockPos(support), face, yaw);
   }

   static float init_F_Vec3_2(Vec3 eye, AABB support, Direction face, float yaw) {
      if (support == null) {
         return 0.0F / 0.0F;
      }

      double past = init_D_Vec3_2(eye, support, face);
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
      double drop = eye.y - init_D_AABB(support);
      return (float)Math.toDegrees(Math.atan2(drop, run));
   }

   static boolean init_Z_BlockPos_7(BlockPos plannedCell, Direction plannedFace, BlockPos rayCell, Direction rayFace) {
      return init_Z_BlockPos(plannedCell, plannedFace, null, rayCell, rayFace, null);
   }

   static boolean init_Z_BlockPos(
      BlockPos plannedCell, Direction plannedFace, BlockPos plannedSupport, BlockPos rayCell, Direction rayFace, BlockPos raySupport
   ) {
      return rayCell != null && rayCell.equals(plannedCell) && rayFace == plannedFace
         ? plannedSupport == null || raySupport == null || plannedSupport.equals(raySupport)
         : false;
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex_BlockHitResult(ScaffoldModule.Inner_TableIndex planned, BlockHitResult ray) {
      if (MC.player != null && MC.level != null && this.load_ce_DataUtil != null && ray != null) {
         BlockPos support = ray.getBlockPos().immutable();
         Direction face = ray.getDirection();
         BlockPos cell = this.init_BlockPos_BlockHitResult_ItemStack_2(ray, this.init_ItemStack_2());
         if (cell == null) {
            return null;
         } else if (init_Z_BlockPos(planned.load(), planned.init_Direction(), planned.init_BlockPos(), cell, face, support)) {
            return null;
         } else if (!MC.level.getBlockState(cell).canBeReplaced()) {
            return null;
         } else if (!init_Z_AABB_5(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), cell, this.push(cell))) {
            return null;
         } else if (!this.read_Z_BlockPos_2(cell)) {
            return null;
         } else if (!init_Z_BlockPos_4(cell, this.close_I(), this.put_BlockPos, this.init_aI_HashUtil == null ? this.update_BlockPos() : null)) {
            return null;
         } else {
            return this.load_Z_BlockPos_2(planned.load()) && !this.load_Z_BlockPos_2(cell)
               ? null
               : new ScaffoldModule.Inner_TableIndex(support, cell, face, ray, this.load_ce_DataUtil, cell.getY());
         }
      } else {
         return null;
      }
   }

   static boolean init_Z_BlockPos_4(BlockPos cell, int oracleRow, BlockPos reservedSupport, BlockPos ownSupport) {
      return cell.getY() <= oracleRow ? true : load_Z_BlockPos_BlockPos(cell, reservedSupport) || load_Z_BlockPos_BlockPos(cell, ownSupport);
   }

   private boolean load_Z_BlockPos_2(BlockPos cell) {
      return MC.player != null && init_Z_BlockPos_BlockPos_2(cell, this.read_BlockPos());
   }

   private BlockPos read_BlockPos() {
      int row = this.close_I();
      return row == -2147483648 ? this.close_BlockPos() : this.init_BlockPos_I_2(row);
   }

   private String init_Str_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      this.init_V_aI_TableIndex(target);
      if (this.init_BlockHitResult == null) {
         return this.sync(target) ? "blank" : "plane";
      } else if (!this.init_BlockHitResult.getBlockPos().equals(target.init_BlockPos())) {
         return "occ";
      } else if (this.init_BlockHitResult.getDirection() != target.init_Direction()) {
         return this.init_BlockHitResult.getDirection() == Direction.UP ? "u" : "side";
      } else {
         return this.pop(target) ? "reach" : "near";
      }
   }

   private void init_V_aI_TableIndex_Str(ScaffoldModule.Inner_TableIndex target, String waitCode) {
      this.read_Direction = null;
      if (MC.player != null
         && target != null
         && ("plane".equals(waitCode) || "u".equals(waitCode))
         && target.init_Direction().getAxis().isHorizontal()
         && !this.sync(target)) {
         this.read_Direction = target.init_Direction();
         this.f_aC_I = ck.init_ck().state_I();
      }
   }

   static boolean init_Z_Direction(Direction waitFace, int waitAge, boolean rising, Vec3 lane) {
      return waitFace != null && waitAge == 0 && rising && lane != null ? waitFace.getStepX() * lane.x + waitFace.getStepZ() * lane.z > 0.0 : false;
   }

   static boolean init_Z_Z_D(boolean starves, double spentBlocks) {
      return starves && spentBlocks <= 0.35;
   }

   private boolean init_Z_Z_Vec3(boolean rising, Vec3 lane) {
      int tick = ck.init_ck().state_I();
      boolean starves = init_Z_Direction(this.read_Direction, load_I_I_I(tick, this.f_aC_I), rising, lane);
      if (tick != this.f_aD_I) {
         this.f_aD_I = tick;
         this.f_A_D = starves ? this.f_A_D + this.init_D_Vec3(lane) : 0.0;
      }

      return init_Z_Z_D(starves, this.f_A_D);
   }

   private double init_D_Vec3(Vec3 lane) {
      if (MC.player != null && lane != null) {
         Vec3 velocity = MC.player.getDeltaMovement();
         return Math.abs(velocity.x * lane.x + velocity.z * lane.z);
      } else {
         return 0.0;
      }
   }

   static boolean init_Z_BlockPos_BlockPos_2(BlockPos cell, BlockPos foot) {
      return cell != null && foot != null && cell.getY() == foot.getY() - 1 && cell.getX() == foot.getX() && cell.getZ() == foot.getZ();
   }

   private boolean read_Z_BlockPos_2(BlockPos cell) {
      if (this.init_aI_HashUtil == null) {
         return false;
      }

      Vec3 direction = this.init_aI_HashUtil.load();
      return init_Z_BlockPos_5(cell, this.read_BlockPos(), load_I_D(direction.x), load_I_D(direction.z));
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

   private boolean load_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (target != null && MC.player != null && target.init_Direction().getAxis().isVertical()) {
         boolean stairRiser = target.init_Direction() == Direction.UP && !MC.player.onGround() && target.load().getY() > this.close_I();
         Vec3 dropCarry = MC.player.onGround() ? Vec3.ZERO : MC.player.getDeltaMovement();
         boolean own = stairRiser && load_Z_BlockPos_BlockPos(target.load(), this.update_BlockPos());
         boolean drop = stairRiser
            ? (
               own
                  ? !init_Z_Vec3_BlockPos(MC.player.position(), target.load())
                  : !this.update_Z_Vec3(MC.player.position(), MC.player.getDeltaMovement(), target.load())
            )
            : !write(MC.player.position(), dropCarry, target.load());
         if (drop) {
            this.clear_Str = stairRiser ? (own ? "own" : "arc") : "foot";
         }

         return drop;
      } else {
         return false;
      }
   }

   static boolean init_Z_Z_Z(boolean onGround, boolean jumpHeld) {
      return onGround && jumpHeld;
   }

   static int load_I_I_I(int tick, int lastTick) {
      if (lastTick == -2147483648) {
         return -1;
      }

      int since = tick - lastTick;
      return since < 0 ? -1 : since;
   }

   private boolean init_Z_aI_TableIndex_ce_DataUtil(ScaffoldModule.Inner_TableIndex target, ce.Inner_DataUtil clickRotation) {
      if (MC.player == null) {
         return false;
      }

      long nowNanos = System.nanoTime();
      long elapsed = this.read_J_J(nowNanos);
      this.clear_J = elapsed;
      if (this.block_Z()) {
         this.f_bT_I = 0;
         this.reset_J = 0L;
         return false;
      }

      if (MC.player.onGround() && !this.f_Z()) {
         long burstWait = init_J_J_JArr(nowNanos, this.write_ArrayDeque.stream().mapToLong(Long::longValue).toArray());
         if (burstWait > 0L) {
            this.f_bT_I = (int)((burstWait + 49L) / 50L);
            this.reset_J = elapsed + burstWait;
            return true;
         }
      }

      boolean airborne = !MC.player.onGround();
      boolean descending = airborne && MC.player.getDeltaMovement().y < 0.0;
      boolean chainCell = airborne && this.write_Z_BlockPos(target.load());
      boolean footCatch = this.push(target.load());
      boolean fallingCatch = footCatch && MC.player.fallDistance >= 1.0;
      if (!this.write_Z_aI_TableIndex(target) && (!chainCell || !descending) && (!chainCell || !this.update_Z_aI_TableIndex(target)) && !fallingCatch) {
         if (init_Z_BlockPos_3(target.load(), target.init_BlockPos(), this.get_BlockPos)) {
            boolean landingRiser = !MC.player.onGround() && MC.player.getDeltaMovement().y < 0.0 && target.load().getY() > target.init_BlockPos().getY();
            long floor = landingRiser ? 50L : 100L;
            this.reset_J = floor;
            return init_Z_J_J(elapsed, floor);
         }

         if (chainCell || footCatch) {
            this.f_h_Z = true;
            this.reset_J = 100L;
            return init_Z_J_J(elapsed, 100L);
         }

         if (this.flush(target.load())) {
            this.f_h_Z = true;
            this.reset_J = 100L;
            return init_Z_J_J(elapsed, 100L);
         }

         if (!this.read_Z_aI_TableIndex(target)) {
            List<BlockPos> booked = new ArrayList<>(this.read_ArrayDeque.size() + 1);

            for (ScaffoldModule.Inner_JsonUtil sample : this.read_ArrayDeque) {
               booked.add(sample.init_BlockPos());
            }

            booked.add(target.load());
            long limit = init_J_Z(
               load_Z_List(booked), init_Z_F_3(clickRotation == null ? MC.player.getYRot() : clickRotation.init()), this.tick_Z_4(), this.push_Z_2()
            );
            long floor = init_J_J(limit) + this.f_aj_I;
            this.f_bT_I = init_I_J_J(elapsed, floor);
            this.reset_J = floor;
            if (init_Z_J_J(elapsed, floor)) {
               return true;
            }

            if (!this.read_ArrayDeque.isEmpty() && this.run_J != -9223372036854775808L) {
               long candidate = elapsed;
               long[] recent = new long[this.read_ArrayDeque.size()];
               int index = 0;

               for (ScaffoldModule.Inner_JsonUtil sample : this.read_ArrayDeque) {
                  recent[index++] = sample.init_J();
               }

               double mean = init_D_JArr(recent, candidate, 8);
               this.reset_J = 0L;
               return mean < limit * 1.08 ? true : this.init_Z_aI_TableIndex_2(target, clickRotation, nowNanos);
            } else {
               return this.init_Z_aI_TableIndex_2(target, clickRotation, nowNanos);
            }
         } else if (this.scan_Z()) {
            this.f_i_Z = true;
            this.reset_J = 50L;
            return init_Z_J_J(elapsed, 50L);
         } else {
            this.f_h_Z = true;
            this.reset_J = 100L;
            return init_Z_J_J(elapsed, 100L);
         }
      } else {
         this.f_i_Z = true;
         this.reset_J = 50L;
         return init_Z_J_J(elapsed, 50L);
      }
   }

   private boolean init_Z_aI_TableIndex_2(ScaffoldModule.Inner_TableIndex target, ce.Inner_DataUtil clickRotation, long nowNanos) {
      if (MC.player == null || !MC.player.onGround()) {
         return false;
      }

      if (!init_Z_Direction(target.init_Direction())) {
         return false;
      }

      if (!this.update_ArrayDeque.isEmpty() && this.tick_J != -9223372036854775808L) {
         long candidate = this.write_J_J(nowNanos);
         if (candidate < 0L) {
            return false;
         }

         if (candidate >= 1000L) {
            return false;
         }

         float pitch = clickRotation == null ? MC.player.getXRot() : clickRotation.load();
         List<BlockPos> cells = new ArrayList<>(this.sync_ArrayDeque.size() + 1);
         if (candidate < 5000L) {
            cells.addAll(this.sync_ArrayDeque);
         }

         while (cells.size() > 4) {
            cells.remove(0);
         }

         cells.add(target.load());
         if (!init_Z_F_2(pitch, this.chunk_F, candidate, init_Z_List(cells))) {
            return false;
         }

         long[] recent = new long[this.update_ArrayDeque.size()];
         int index = 0;

         for (long gap : this.update_ArrayDeque) {
            recent[index++] = gap;
         }

         if (init_D_JArr(recent, candidate, 8) >= 432.0) {
            return false;
         }

         this.clear_J = candidate;
         this.f_m_Z = true;
         return true;
      } else {
         return false;
      }
   }

   static long init_J_J(long limitMs) {
      return Math.round(limitMs * 1.08);
   }

   private long read_J_J(long nowNanos) {
      return init_J_J_J(nowNanos, this.run_J);
   }

   static long init_J_J_J(long nowNanos, long sinceNanos) {
      if (sinceNanos == -9223372036854775808L) {
         return -1L;
      } else {
         return nowNanos <= sinceNanos ? 0L : (nowNanos - sinceNanos) / 1000000L;
      }
   }

   static long load_J_J_J(long previousNanos, long observedNanos) {
      return previousNanos == -9223372036854775808L ? observedNanos : Math.max(previousNanos, observedNanos);
   }

   static boolean init_Z_J_J(long elapsedMs, long floorMs) {
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

   static int init_I_J_J(long elapsedMs, long floorMs) {
      return elapsedMs >= 0L && elapsedMs < floorMs ? (int)Math.ceil((floorMs - elapsedMs) / 50.0) : 0;
   }

   private int read_I_5() {
      if (MC.player == null) {
         return -2147483648;
      } else {
         return MC.player.onGround() ? this.close_I() : this.poll_I();
      }
   }

   private boolean read_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (MC.player != null && MC.level != null) {
         int row = this.read_I_5();
         if (row == -2147483648) {
            return false;
         } else {
            return !init_Z_BlockPos_BlockPos_2(target.load(), this.init_BlockPos_I_2(row))
               ? false
               : this.load_D_Vec3_I(MC.player.position(), row) <= init_D_Z(MC.player.onGround());
         }
      } else {
         return false;
      }
   }

   static double init_D_Z(boolean grounded) {
      return grounded ? 0.3 : 0.12;
   }

   private BlockPos init_BlockPos_I_2(int row) {
      Vec3 position = MC.player.position();
      return new BlockPos(Mth.floor(position.x), row + 1, Mth.floor(position.z));
   }

   private boolean write_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (MC.player == null || MC.level == null) {
         return false;
      }

      if (!MC.player.onGround() && !(MC.player.getDeltaMovement().y >= 0.0)) {
         int row = this.read_I_5();
         if (row == -2147483648) {
            return false;
         } else {
            Vec3 landing = init_Vec3_Vec3_2(MC.player.position(), MC.player.getDeltaMovement(), row + 1.0, 12);
            if (landing == null) {
               return false;
            } else {
               return !target.load().equals(BlockPos.containing(landing.x, row + 0.5, landing.z))
                  ? false
                  : this.load_D_Vec3_I(MC.player.position(), row) <= 0.12;
            }
         }
      } else {
         return false;
      }
   }

   private boolean write_Z_BlockPos(BlockPos placed) {
      return this.flush(placed)
         || placed.equals(this.put_BlockPos)
         || placed.equals(this.add_BlockPos)
         || placed.equals(this.reset_BlockPos)
         || placed.equals(this.clear_BlockPos);
   }

   private boolean update_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (target.init_Direction().getAxis().isVertical()) {
         return false;
      }

      Vec3 eye = MC.player.getEyePosition();
      Vec3 velocity = MC.player.getDeltaMovement();
      float yaw = target.init_ce_DataUtil().init();

      for (int step = 1; step <= 2; step++) {
         Vec3 projected = eye.add(velocity.scale(step));
         if (!(init_D_Vec3(projected, target.init_BlockPos(), target.init_Direction()) <= 0.0)
            && load_Z_Vec3(projected, target.init_BlockPos(), target.init_Direction(), yaw, false)) {
            float solve = init_F_Vec3(projected, target.init_BlockPos(), target.init_Direction(), yaw);
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

   private void init_V_BlockPos_2(BlockPos placed, BlockPos against, Direction face, float wirePitch) {
      long observed = this.set_J == -9223372036854775808L ? System.nanoTime() : this.set_J;
      long now = load_J_J_J(this.run_J, observed);
      this.write_ArrayDeque.addLast(now);

      while (this.write_ArrayDeque.size() > 4) {
         this.write_ArrayDeque.removeFirst();
      }

      long interval = this.run_J == -9223372036854775808L ? 1000L : init_J_J_J(now, this.run_J);
      if (placed.getY() != against.getY()) {
         interval += 1000L;
      }

      this.run_J = now;
      this.read_ArrayDeque.addLast(new ScaffoldModule.Inner_JsonUtil(interval, placed));

      while (this.read_ArrayDeque.size() > 8) {
         this.read_ArrayDeque.removeFirst();
      }

      if (init_Z_Direction(face)) {
         this.init_V_J(now, placed, wirePitch);
      }

      this.tick_BlockPos = null;
      this.f_ak_I = -2147483648;
      this.clear_V();
   }

   static boolean init_Z_Direction(Direction face) {
      return face != null && face.getAxis().isHorizontal();
   }

   static long init_J_J_JArr(long nowNanos, long[] writtenNanos) {
      if (writtenNanos != null && writtenNanos.length >= 4) {
         long oldest = writtenNanos[writtenNanos.length - 4];
         return Math.max(0L, 1050L - init_J_J_J(nowNanos, oldest));
      } else {
         return 0L;
      }
   }

   private void init_V_J(long now, BlockPos placed, float wirePitch) {
      long gap = this.write_J_J(now);
      if (gap < 0L) {
         gap = 1000L;
      }

      if (gap >= 5000L) {
         this.sync_ArrayDeque.clear();
      }

      this.tick_J = now;
      this.chunk_F = wirePitch;
      this.update_ArrayDeque.addLast(gap);

      while (this.update_ArrayDeque.size() > 8) {
         this.update_ArrayDeque.removeFirst();
      }

      this.sync_ArrayDeque.addLast(placed.immutable());

      while (this.sync_ArrayDeque.size() > 5) {
         this.sync_ArrayDeque.removeFirst();
      }
   }

   private long write_J_J(long nowNanos) {
      long elapsed = init_J_J_J(nowNanos, this.tick_J);
      return elapsed < 0L ? -1L : Math.min(1000L, elapsed);
   }

   private boolean push_Z_2() {
      long elapsed = init_J_J_J(System.nanoTime(), this.get_J);
      return elapsed >= 0L && elapsed < 750L;
   }

   private void add_V() {
      if (MC.player != null) {
         boolean onGround = MC.player.onGround();
         if (this.index_Z && !onGround && MC.player.getDeltaMovement().y > 0.0) {
            this.get_J = System.nanoTime();
            this.init_Map.clear();
         }

         this.index_Z = onGround;
      }
   }

   static boolean init_Z_List(List<BlockPos> blocks) {
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
      if (!this.flags_Z() || !init_Z_Direction(face)) {
         return goal;
      }

      if (!Float.isNaN(this.chunk_F) && goal > 70.0F) {
         long gap = this.write_J_J(System.nanoTime());
         if (gap < 0L || gap >= 800L) {
            return goal;
         }

         if (load_Z_F_F(goal, this.chunk_F)) {
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
         return !init_Z_List(cells) && !this.pop_Z() ? goal : init_F_F(goal, this.chunk_F, windowLow, windowHigh);
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
      if (this.update_ArrayDeque.size() < 7) {
         return true;
      }

      long candidate = this.write_J_J(System.nanoTime());
      if (candidate < 0L) {
         return true;
      }

      long[] recent = new long[this.update_ArrayDeque.size()];
      int index = 0;

      for (long gap : this.update_ArrayDeque) {
         recent[index++] = gap;
      }

      return init_D_JArr(recent, candidate, 8) < 432.0;
   }

   private boolean init_Z_BlockPos_5(BlockPos support, Direction face, int maxTicks) {
      int tick = ck.init_ck().state_I();
      if (support.equals(this.run_BlockPos) && face == this.load_Direction) {
         if (tick != this.f_an_I) {
            this.f_am_I++;
            this.f_an_I = tick;
         }

         return this.f_am_I <= maxTicks;
      } else {
         this.run_BlockPos = support.immutable();
         this.load_Direction = face;
         this.f_am_I = 1;
         this.f_an_I = tick;
         return true;
      }
   }

   private boolean update_Z_BlockPos(BlockPos placed) {
      return MC.player != null && !MC.player.onGround() && this.write_Z_BlockPos(placed);
   }

   private void clear_V() {
      this.run_BlockPos = null;
      this.load_Direction = null;
      this.f_am_I = 0;
      this.f_an_I = -2147483648;
   }

   private float init_F_F(float goal, double windowLow, Direction face, Vec3 probeEye, Vec3 leadStep, float yaw, BlockPos support) {
      if (this.flags_Z() && init_Z_Direction(face)) {
         float safe = init_F_F_J(this.chunk_F, this.write_J_J(System.nanoTime()));
         if (goal > safe && this.pop_Z()) {
            boolean grounded = MC.player != null && MC.player.onGround();
            boolean immediate = Double.isFinite(windowLow) && windowLow <= safe;
            if (immediate && support.equals(this.run_BlockPos) && face == this.load_Direction) {
               this.clear_V();
            }

            boolean shallowAhead = !immediate
               && !this.k()
               && !this.update_Z_BlockPos(support.relative(face))
               && init_Z_Vec3(probeEye, leadStep, support, face, yaw, safe, grounded ? 5 : 2);
            boolean parkAllowed = shallowAhead && this.init_Z_BlockPos_5(support, face, grounded ? 6 : 3);
            return init_F_F(goal, windowLow, true, shallowAhead, parkAllowed, safe);
         } else {
            return goal;
         }
      } else {
         return goal;
      }
   }

   static boolean load_Z_List(List<BlockPos> booked) {
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

   static boolean init_Z_F_3(float emittedYaw) {
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

   static double init_D_JArr(long[] recent, long candidateMs, int capacity) {
      int keep = Math.min(recent.length, capacity - 1);
      double sum = candidateMs;

      for (int i = recent.length - keep; i < recent.length; i++) {
         sum += recent[i];
      }

      return sum / (keep + 1.0);
   }

   private boolean tick_Z_4() {
      int since = load_I_I_I(ck.init_ck().state_I(), this.f_ao_I);
      return since >= 0 && since <= 150;
   }

   private boolean load_Z_Z(boolean wanted) {
      int tick = ck.init_ck().state_I();
      if (this.f_ap_I > 0) {
         if (!wanted && this.f_aq_I >= (this.state_Z ? 3 : 2) && this.run_Z()) {
            this.f_ap_I = 0;
            this.f_aq_I = 0;
            return false;
         } else {
            this.f_ap_I--;
            this.f_aq_I++;
            this.f_ao_I = tick;
            return true;
         }
      } else if (wanted || this.update_Z_I(tick) && load_Z_I_2(load_I_I_I(tick, this.f_ao_I))) {
         if (load_I_I_I(tick, this.f_ao_I) != 1) {
            this.f_ap_I = init_I_Z_I(this.state_Z, ThreadLocalRandom.current().nextInt(3)) - 1;
            this.f_aq_I = 1;
         } else {
            this.f_aq_I++;
         }

         this.f_ao_I = tick;
         return true;
      } else {
         this.f_aq_I = 0;
         return false;
      }
   }

   static int init_I_Z_I(boolean diagonalTelly, int roll) {
      return !diagonalTelly ? 2 : Math.max(3, Math.min(5, 3 + Math.max(0, roll)));
   }

   private boolean run_Z() {
      return MC.player != null && (!MC.player.onGround() || this.queue_Z());
   }

   static boolean load_Z_I_2(int sinceSneak) {
      return sinceSneak < 0 || sinceSneak >= 90;
   }

   private void read_V_BlockPos(BlockPos cell) {
      if (cell != null) {
         int tick = ck.init_ck().state_I();
         this.init_Map.entrySet().removeIf(entry -> load_I_I_I(tick, entry.getValue()) >= 10);
         if (this.init_Map.size() < 32) {
            this.init_Map.put(cell.immutable(), tick);
         }
      }
   }

   private boolean sync(BlockPos cell) {
      if (this.init_Map.isEmpty()) {
         return false;
      } else {
         Integer marked = this.init_Map.get(cell);
         if (marked == null) {
            return false;
         } else if (load_I_I_I(ck.init_ck().state_I(), marked) >= 10) {
            this.init_Map.remove(cell);
            return false;
         } else {
            return MC.player == null || !init_Z_Vec3_BlockPos(MC.player.position(), cell);
         }
      }
   }

   private boolean sync(ScaffoldModule.Inner_TableIndex target) {
      return init_D_Vec3(MC.player.getEyePosition(), target.init_BlockPos(), target.init_Direction()) > 0.05;
   }

   public static void init_V_22() {
   }

   private boolean update_Z_I(int tick) {
      return this.f_z_I != -2147483648 && tick - this.f_z_I <= 40;
   }

   static boolean init_Z_aI_SyncManager(ScaffoldModule.Inner_SyncManager state) {
      return state == ScaffoldModule.Inner_SyncManager.ARMED || state == ScaffoldModule.Inner_SyncManager.SENT || state == ScaffoldModule.Inner_SyncManager.RECONCILING;
   }

   static long load_J_J(long previous) {
      return Math.incrementExact(Math.max(0L, previous));
   }

   static boolean load_Z_I_I(int sequence, int acknowledged) {
      return sequence >= 0 && acknowledged >= sequence;
   }

   static ScaffoldModule.Inner_ConfigStore init_aI_ConfigStore_aI_SyncManager(
      ScaffoldModule.Inner_SyncManager state, int age, boolean resultSeen, boolean resultConsumed, boolean solid, boolean ackCovered
   ) {
      if (state == null) {
         state = ScaffoldModule.Inner_SyncManager.IDLE;
      }

      if (state == ScaffoldModule.Inner_SyncManager.IDLE || state == ScaffoldModule.Inner_SyncManager.FAILED) {
         return new ScaffoldModule.Inner_ConfigStore(state, null);
      }

      if (resultSeen && !resultConsumed) {
         return new ScaffoldModule.Inner_ConfigStore(ScaffoldModule.Inner_SyncManager.FAILED, "use");
      }

      if (state == ScaffoldModule.Inner_SyncManager.ARMED) {
         return age >= 2 ? new ScaffoldModule.Inner_ConfigStore(ScaffoldModule.Inner_SyncManager.FAILED, "not-sent") : new ScaffoldModule.Inner_ConfigStore(state, null);
      }

      if (resultSeen && resultConsumed && solid) {
         return new ScaffoldModule.Inner_ConfigStore(ScaffoldModule.Inner_SyncManager.PREDICTED, null);
      }

      if (state == ScaffoldModule.Inner_SyncManager.PREDICTED && !solid) {
         state = ScaffoldModule.Inner_SyncManager.RECONCILING;
      } else if (state == ScaffoldModule.Inner_SyncManager.SENT && resultSeen && resultConsumed) {
         state = ScaffoldModule.Inner_SyncManager.RECONCILING;
      }

      if (state == ScaffoldModule.Inner_SyncManager.RECONCILING) {
         if (ackCovered) {
            return new ScaffoldModule.Inner_ConfigStore(ScaffoldModule.Inner_SyncManager.FAILED, "ack-air");
         }

         if (age >= 2) {
            return new ScaffoldModule.Inner_ConfigStore(ScaffoldModule.Inner_SyncManager.FAILED, "timeout-air");
         }
      } else if (state == ScaffoldModule.Inner_SyncManager.SENT && age >= 2) {
         return new ScaffoldModule.Inner_ConfigStore(ScaffoldModule.Inner_SyncManager.FAILED, "result-timeout");
      }

      return new ScaffoldModule.Inner_ConfigStore(state, null);
   }

   private static void reset_V() {
      ScaffoldModule.Inner_StateCache collected;
      while ((collected = (ScaffoldModule.Inner_StateCache)init_ReferenceQueue.poll()) != null) {
         load_Map.remove(collected);
      }
   }

   static ScaffoldModule.Inner_EventBus init_aI_EventBus_Z(boolean supportSolid, int supportDeficit, boolean riserSolid) {
      if (!supportSolid) {
         return supportDeficit <= 1 ? ScaffoldModule.Inner_EventBus.SUPPORT : ScaffoldModule.Inner_EventBus.CONNECTOR;
      } else {
         return riserSolid ? ScaffoldModule.Inner_EventBus.READY : ScaffoldModule.Inner_EventBus.RISER;
      }
   }

   private boolean get_Z() {
      return this.f_au_I >= 0;
   }

   private void init_V_Str_aI_TableIndex(String outcome, ScaffoldModule.Inner_TableIndex target) {
      if (this.get_Z() && MC.player != null) {
         if (this.f_au_I == 0) {
            AsyncTraceLogger.init("[scaffold-live] capture start");
            AsyncTraceLogger.init(
               "[scaffold-why] legend lr=the reservation ledger, advisory only - it decides no key since the 12:52 ruling (known=landing solved, deficit=blocks still owed, pend=click already on the wire, landsolid=is the landing column's support already a block - winners take off T, losers F); strip=<stage>:<-removed+added> per stage, F/B/L/R/J/S/P keys, net=physical vs emitted - the jump and travel keys are ALWAYS the player's own; the only writers left are wire-sneak/lip-stop/reconcile/empty-stack (each sneak-only), lane (grounded steering) and the sprint drop for the non-sprint predictor; clk=<why code> past=<eye past face plane>/<margin needed> demand=<pitch the crossing needs>/<hard cap> drop=<eye height over the face>; rsv=<reserved chain cell>:<its own verdict> - oob/cooldown/behind/solid/no-repl/rowlock/ok or plan[<per-face codes>], where ip:<box|side|strict> is the body-clearance refusal and strict means the CATCH PREDICATE was shut; pick=<selection tier> plan=<last plan reject>; arcend=one line per landing, dy=+0.00 placed=0 is a jump that achieved nothing; arcchain=the same arc's two-click window - the landing support (planned/became a block) against every tick the riser above it was body-legal. clear ending BEFORE set means the level was never placeable, not merely missed"
            );
         }

         Vec3 p = MC.player.position();
         Vec3 v = MC.player.getDeltaMovement();
         if (this.f_n_Z && !MC.player.onGround() && this.queue_Z()) {
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-live] t%03d takeoff     vel=%.3f,%.3f,%.3f arc=%s foot=%s",
                  this.f_au_I,
                  v.x,
                  v.y,
                  v.z,
                  this.open_Str(),
                  this.push_Str()
               )
            );
            this.f_aX_I = this.f_au_I;
            this.f_aY_I = ck.init_ck().state_I();
            this.poll_Vec3 = p;
            this.run_Str = (this.put_BlockPos == null ? "--" : this.put_BlockPos.toShortString()) + ":" + this.update_Str;
            this.f_aZ_I = 0;
            this.f_ba_I = 0;
            this.f_bb_I = 0;
            this.f_bc_I = 0;
            this.f_bd_I = 0;
            this.f_be_I = 0;
         }

         if (!MC.player.onGround()) {
            this.load_V_Str(outcome);
         }

         this.stop_V();
         if (!this.f_n_Z && MC.player.onGround() && this.f_aX_I >= 0) {
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-why] t%03d arcend     from=t%03d ticks=%d lines=%d dy=%+.2f dxz=%.2f placed=%d set=%d aim=%d pace=%d notgt=%d drop=%d goal=%s end=%s",
                  this.f_au_I,
                  this.f_aX_I,
                  this.f_aY_I < 0 ? -1 : ck.init_ck().state_I() - this.f_aY_I,
                  this.f_au_I - this.f_aX_I,
                  this.poll_Vec3 == null ? 0.0 / 0.0 : p.y - this.poll_Vec3.y,
                  this.poll_Vec3 == null ? 0.0 / 0.0 : Math.hypot(p.x - this.poll_Vec3.x, p.z - this.poll_Vec3.z),
                  this.f_aZ_I,
                  this.f_ba_I,
                  this.f_bb_I,
                  this.f_bc_I,
                  this.f_bd_I,
                  this.f_be_I,
                  this.run_Str,
                  this.add_Str
               )
            );
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-why] t%03d arcchain   sup=%s plan=%s set=%s | ris=%s clear=%s..%s set=%s clk=%s rfail=%s relatch=%d land=%s",
                  this.f_au_I,
                  init_Str_BlockPos_2(this.stop_BlockPos),
                  init_Str_I(this.f_bf_I),
                  init_Str_I(this.f_bg_I),
                  init_Str_BlockPos_2(this.next_BlockPos),
                  init_Str_I(this.f_bh_I),
                  init_Str_I(this.f_bi_I),
                  init_Str_I(this.f_bj_I),
                  this.set_Str,
                  this.put_Str,
                  this.f_bk_I,
                  BlockPos.containing(p.x, p.y - 0.05, p.z).toShortString()
               )
            );
            this.f_aX_I = -1;
            this.f_aY_I = -1;
            this.poll_Vec3 = null;
            this.f_bk_I = 0;
            this.write_V_BlockPos(null);
         }

         if (!this.f_o_Z && MC.player != null && !MC.player.onGround() && MC.player.fallDistance >= 0.5 && MC.player.getDeltaMovement().y < 0.0) {
            this.f_o_Z = true;
            int placeAge = this.f_av_I == -2147483648 ? -1 : ck.init_ck().state_I() - this.f_av_I;
            ScaffoldModule.Inner_TableIndex held = this.read_aI_TableIndex != null ? this.read_aI_TableIndex : this.load_aI_TableIndex;
            AsyncTraceLogger.init(
               String.format(
                  Locale.ROOT,
                  "[scaffold-live] t%03d fall-start pos=%.2f,%.2f,%.2f vel=%.3f,%.3f,%.3f fall=%.1f placeage=%d arc=%s tgt=%s",
                  this.f_au_I,
                  p.x,
                  p.y,
                  p.z,
                  v.x,
                  v.y,
                  v.z,
                  MC.player.fallDistance,
                  placeAge,
                  this.open_Str(),
                  read_Str_aI_TableIndex(held)
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
               this.f_au_I,
               outcome,
               p.x,
               p.y,
               p.z,
               v.x,
               v.y,
               v.z,
               this.close_Vec3.x,
               this.close_Vec3.z,
               MC.player.onGround() ? "T" : "F",
               MC.player.fallDistance,
               this.flush_Z ? "T" : "F",
               this.c_Z() ? "T" : "F",
               this.write_Str(),
               this.update_Str(),
               this.sync_Str(),
               this.flush_Str(),
               this.close_Str(),
               this.init_Str_8(),
               init_Str_ce_DataUtil(this.load_ce_DataUtil),
               init_Str_ce_DataUtil(this.update_ce_DataUtil),
               init_Str_ce_DataUtil(this.read_ce_DataUtil),
               this.load_Str_3(),
               this.pop_Str(),
               this.stop_Str,
               this.start_Str,
               this.set_BlockPos == null ? "null" : this.set_BlockPos.toShortString(),
               read_Str_aI_TableIndex(target),
               !"place".equals(outcome) && !outcome.startsWith("PLACE") ? "" : this.load_Str_aI_TableIndex(target)
            )
         );
         this.load_V_aI_TableIndex(target);
         this.init_StringBuilder.setLength(0);
         this.tick_Str = "--";
         this.f_au_I++;
         this.read_V_Str(outcome);
      }
   }

   private void load_V_Str(String outcome) {
      if (outcome.startsWith("PLACE")) {
         this.f_aZ_I++;
      } else if (outcome.startsWith("settle-ok")) {
         this.f_ba_I++;
      } else if (outcome.startsWith("aim-hold")) {
         this.f_bb_I++;
      } else if (outcome.startsWith("pace-hold")) {
         this.f_bc_I++;
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

      int row = this.close_I();
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

   private String load_Str_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (target != null && target.init_BlockHitResult() != null) {
         Vec3 loc = target.init_BlockHitResult().getLocation();
         BlockPos block = target.init_BlockHitResult().getBlockPos();
         int tick = ck.init_ck().state_I();
         int gap = this.f_av_I == -2147483648 ? -1 : tick - this.f_av_I;
         this.f_av_I = tick;
         ce.Inner_DataUtil click = this.update_ce_DataUtil;
         float yawOff = click == null ? 0.0F / 0.0F : Mth.wrapDegrees(click.init() - this.init_F_4());
         float residual = click != null && this.load_I_8() != -1 ? update_F_F_F(this.load_F_2(), click.init()) : 0.0F / 0.0F;
         return String.format(
            Locale.ROOT,
            "hit=%.3f,%.3f,%.3f face=%s gap=%d yawoff=%.1f res=%.1f",
            loc.x - block.getX(),
            loc.y - block.getY(),
            loc.z - block.getZ(),
            target.init_Direction(),
            gap,
            yawOff,
            residual
         );
      } else {
         return "hit=--";
      }
   }

   private String load_Str_3() {
      if (this.load_ce_DataUtil == null) {
         return "d=--,-- sum=--";
      }

      float yaw = this.load_ce_DataUtil.init();
      float pitch = this.load_ce_DataUtil.load();
      float dYaw = Float.isNaN(this.mark_F) ? 0.0F : Mth.wrapDegrees(yaw - this.mark_F);
      float dPitch = Float.isNaN(this.scan_F) ? 0.0F : pitch - this.scan_F;
      this.mark_F = yaw;
      this.scan_F = pitch;
      float sent = f_l_F;
      float rawDelta = !Float.isNaN(sent) && !Float.isNaN(this.parse_F) ? Math.abs(sent - this.parse_F) : 0.0F;
      this.parse_F = sent;
      this.f_C_D += rawDelta;
      this.f_D_D = this.f_D_D + Math.abs(dPitch);
      if (rawDelta > 1.0E-4F || Math.abs(dPitch) > 1.0E-4F) {
         this.f_by_I++;
      }

      int tick = ck.init_ck().state_I();
      int dt = this.f_bz_I == -2147483648 ? 1 : tick - this.f_bz_I;
      this.f_bz_I = tick;
      return String.format(
         Locale.ROOT,
         "d=%+.2f,%+.2f sum=%.0f,%.0f mv=%d dt=%d sent=%.2f/%.1f rd=%.2f",
         dYaw,
         dPitch,
         this.f_C_D,
         this.f_D_D,
         this.f_by_I,
         dt,
         sent,
         this.load_ce_DataUtil == null ? 0.0F / 0.0F : this.load_ce_DataUtil.load(),
         rawDelta
      );
   }

   private String read_Str() {
      return MC.player == null
         ? " ovl=? set=--"
         : String.format(
            Locale.ROOT,
            " ovl=%.2f/%s set=%s",
            this.load_D_Vec3_I(MC.player.position(), this.close_I()),
            MC.player.onGround() ? "T" : "F",
            this.size_BlockPos == null ? "--" : this.size_BlockPos.toShortString()
         );
   }

   private ScaffoldModule.Inner_TaskTimer init_aI_TaskTimer() {
      Input emitted = MC.player.input == null ? Input.EMPTY : MC.player.input.keyPresses;
      int forwardImpulse = (emitted.forward() ? 1 : 0) - (emitted.backward() ? 1 : 0);
      int sidewaysImpulse = (emitted.left() ? 1 : 0) - (emitted.right() ? 1 : 0);
      double octant = init_D_I_I(forwardImpulse, sidewaysImpulse);
      double flip = init_D_D_3(this.fill_F);
      boolean paired = this.f_bE_I == ck.init_ck().state_I() - 1;
      if (paired && !Double.isNaN(octant) && !Float.isNaN(f_l_F) && !Float.isNaN(this.bind_F)) {
         double residual = Mth.wrapDegrees(f_l_F + octant - this.bind_F);
         double speed = init_D_Z_Z(this.f_q_Z, emitted.sprint());
         double radians = residual * 0.01745329238474369;
         double forwardAccel = speed * Math.cos(radians);
         double lateralAccel = -speed * Math.sin(radians);
         if (this.f_r_Z && emitted.sprint()) {
            double impulse = (residual - octant) * 0.01745329238474369;
            forwardAccel += 0.2 * Math.cos(impulse);
            lateralAccel += -0.2 * Math.sin(impulse);
         }

         return new ScaffoldModule.Inner_TaskTimer(octant, residual, forwardAccel, lateralAccel, flip, emitted.sprint(), this.f_q_Z);
      } else {
         return new ScaffoldModule.Inner_TaskTimer(octant, 0.0 / 0.0, 0.0 / 0.0, 0.0 / 0.0, flip, emitted.sprint(), this.f_q_Z);
      }
   }

   static double init_D_Z_Z(boolean groundedWhenEmitted, boolean sprintWhenEmitted) {
      return (groundedWhenEmitted ? 0.1 : 0.0196) * (sprintWhenEmitted ? 1.3 : 1.0);
   }

   static double init_D_D_4(double perp, double lateralAccel, boolean groundedWhenEmitted) {
      return (perp + lateralAccel) * (groundedWhenEmitted ? 0.546 : 0.91);
   }

   private String init_Str_aI_TaskTimer(ScaffoldModule.Inner_TaskTimer drift) {
      return "oct="
         + (Double.isNaN(drift.init_D()) ? "--" : init_Str_D(drift.init_D()))
         + " res="
         + init_Str_D(drift.load_D())
         + " afwd="
         + load_Str_D(drift.read())
         + " alat="
         + load_Str_D(drift.write())
         + " dlat="
         + load_Str_D(this.f_G_D)
         + " drg="
         + String.format(Locale.ROOT, "%.3f", drift.load_Z() ? 0.546 : 0.91)
         + " spr="
         + (drift.init_Z() ? "T" : "F")
         + " flip="
         + (Double.isNaN(drift.update()) ? "--" : String.format(Locale.ROOT, "%.0f/%.0f", drift.update(), 15.0F));
   }

   private static String init_Str_D(double value) {
      return Double.isNaN(value) ? "--" : String.format(Locale.ROOT, "%+.1f", value);
   }

   private static String load_Str_D(double value) {
      return Double.isNaN(value) ? "--" : String.format(Locale.ROOT, "%+.4f", value);
   }

   private double load_D_Vec3(Vec3 pos) {
      Vec3 forward = this.write_Vec3();
      return forward.horizontalDistanceSqr() <= 1.0E-8 ? 0.0 / 0.0 : pos.dot(forward.normalize());
   }

   private void init_V_aI_TaskTimer(ScaffoldModule.Inner_TaskTimer drift, double laneError, Vec3 pos) {
      int tick = ck.init_ck().state_I();
      if (tick != this.f_bA_I) {
         this.f_bA_I = tick;
         if (!Double.isNaN(drift.write())) {
            this.f_G_D = this.f_G_D + drift.write();
         }

         boolean grounded = MC.player.onGround();
         double course = this.load_D_Vec3(pos);
         if (!grounded) {
            if (this.f_p_Z) {
               this.f_bB_I = 0;
               this.f_bD_I = 0;
               this.f_E_D = 0.0;
               this.f_F_D = 0.0;
            }

            this.f_bB_I++;
            if (drift.init_Z()) {
               this.f_bD_I++;
            }

            if (!Double.isNaN(drift.load_D())) {
               this.f_E_D = this.f_E_D + Math.abs(drift.load_D());
            }

            if (!Double.isNaN(drift.write())) {
               this.f_F_D = this.f_F_D + drift.write();
            }
         } else {
            if (!this.f_p_Z && this.f_bB_I > 0) {
               this.init_V_D_D(laneError, course);
               this.f_H_D = laneError;
               this.f_I_D = course;
               this.f_bC_I = 0;
            }

            this.f_bC_I++;
         }

         this.f_p_Z = grounded;
      }
   }

   static double init_D_D_5(double travel, int groundTicks, int airTicks) {
      int ticks = groundTicks + airTicks;
      return ticks > 0 && !Double.isNaN(travel) ? travel * 20.0 / ticks : 0.0 / 0.0;
   }

   private void init_V_D_D(double laneError, double course) {
      double laneMoved = Double.isNaN(this.f_H_D) ? 0.0 / 0.0 : laneError - this.f_H_D;
      double travel = Double.isNaN(this.f_I_D) ? 0.0 / 0.0 : Math.abs(course - this.f_I_D);
      double bps = init_D_D_5(travel, this.f_bC_I, this.f_bB_I);
      AsyncTraceLogger.init(
         "[telly-cycle] air="
            + this.f_bB_I
            + " ground="
            + this.f_bC_I
            + " sprint="
            + this.f_bD_I
            + "/"
            + this.f_bB_I
            + " res="
            + init_Str_D(this.f_bB_I == 0 ? 0.0 / 0.0 : this.f_E_D / this.f_bB_I)
            + " dv="
            + load_Str_D(this.f_F_D)
            + " dlane="
            + load_Str_D(laneMoved)
            + " travel="
            + load_Str_D(travel)
            + " bps="
            + load_Str_D(bps)
      );
   }

   private void write_V_Str(String outcome) {
      if (MC.player != null && this.f_aw_I >= 0) {
         if (this.f_aw_I == 0) {
            AsyncTraceLogger.init("[telly-live] capture start");
         }

         Vec3 pos = MC.player.position();
         Vec3 vel = MC.player.getDeltaMovement();
         double laneError = this.sync_BlockPos == null ? 0.0 / 0.0 : this.f_x_D - init_D_Vec3_F(pos, this.flags_F);
         double perp = vel.dot(this.update_Vec3());
         ScaffoldModule.Inner_TaskTimer drift = this.init_aI_TaskTimer();
         this.init_V_aI_TaskTimer(drift, laneError, pos);
         AsyncTraceLogger.init(
            String.format(
               Locale.ROOT,
               "[telly-live] t%03d %-9s pos=%.2f,%.2f,%.2f vel=%.3f,%.3f,%.3f step=%.3f,%.3f gnd=%s fall=%.1f sneak=%s %s anchor=%.0f emitted=%s %s lane=%+.3f/perp%+.3f/bias%+.1f/steer%+.1f%s off=%s air=%s/cd%d/p%d hold=%s phase=%s motion=%s bridgeY=%d bridge=%s tgt=%s raised=%s queued=%s cd=%d fail=%d %s",
               this.f_aw_I,
               outcome,
               pos.x,
               pos.y,
               pos.z,
               vel.x,
               vel.y,
               vel.z,
               this.close_Vec3.x,
               this.close_Vec3.z,
               MC.player.onGround() ? "T" : "F",
               MC.player.fallDistance,
               init_Z_KeyMapping(MC.options.keyShift) ? "T" : "F",
               this.write_Str(),
               this.flags_F,
               init_Str_ce_DataUtil(this.load_ce_DataUtil),
               this.load_Str_3(),
               laneError,
               perp,
               this.table_F,
               this.block_F,
               this.valid_Z ? "*" : "",
               this.set_Z() ? "T" : "F",
               this.init_aI_c,
               this.f_N_I,
               this.f_O_I,
               this.update_Input,
               this.init_aI_ValueSink,
               this.init_aI_KeyHolder,
               this.f_L_I,
               this.sync_BlockPos == null ? "--" : this.sync_BlockPos.toShortString(),
               this.init_aI_DataPipe == null ? "--" : this.init_aI_DataPipe.init_aI_TableIndex().load().toShortString(),
               this.init_aI_DataPipe != null && this.init_aI_DataPipe.init_Z() ? "T" : "F",
               this.stop_Z ? "T" : "F",
               this.f_G_I,
               this.f_J_I,
               this.init_Str_aI_TaskTimer(drift)
            )
         );
         this.f_aw_I++;
      }
   }

   private boolean set_Z() {
      try {
         return MC.player != null && this.sync_BlockPos != null && this.update_Z_LocalPlayer(MC.player);
      } catch (RuntimeException ignored) {
         return false;
      }
   }

   private void start_V() {
      int tick = ck.init_ck().state_I();
      if (tick != this.f_ax_I) {
         this.f_ax_I = tick;
         this.write_V_Str(this.push_Z ? "tick" : "idle");
         this.f_bE_I = tick;
         this.f_q_Z = MC.player != null && MC.player.onGround();
         this.f_r_Z = this.run_Z;
         this.bind_F = this.flags_F;
      }
   }

   private String write_Str() {
      Input emitted = MC.player.input == null ? Input.EMPTY : MC.player.input.keyPresses;
      return "keys="
         + (init_Z_KeyMapping(MC.options.keyUp) ? "W" : "-")
         + (init_Z_KeyMapping(MC.options.keyLeft) ? "A" : "-")
         + (init_Z_KeyMapping(MC.options.keyDown) ? "S" : "-")
         + (init_Z_KeyMapping(MC.options.keyRight) ? "D" : "-")
         + (this.queue_Z() ? "J" : "-")
         + (init_Z_KeyMapping(MC.options.keyShift) ? "N" : "-")
         + (init_Z_KeyMapping(MC.options.keySprint) ? "R" : "-")
         + " inPrev="
         + (emitted.forward() ? "W" : "-")
         + (emitted.left() ? "A" : "-")
         + (emitted.backward() ? "S" : "-")
         + (emitted.right() ? "D" : "-")
         + (emitted.jump() ? "J" : "-")
         + (emitted.shift() ? "N" : "-")
         + (emitted.sprint() ? "R" : "-");
   }

   private String update_Str() {
      String cand = this.f_q_I == -1 ? "--" : init_StrArr[this.f_q_I] + ":" + this.f_r_I;
      return String.format(
         Locale.ROOT,
         "crs=%s/%+.0f go=%s/%+.0f cam=%+.0f cand=%s dia=%s frz=%s lin=%+.0f",
         this.f_p_I == -1 ? "--" : init_StrArr[this.f_p_I],
         this.init_F_4(),
         this.load_I_8() == -1 ? "--" : init_StrArr[this.load_I_8()],
         this.load_F_2(),
         MC.player.getYRot(),
         cand,
         this.init_aI_HashUtil != null && init_Z_Vec3_2(this.init_aI_HashUtil.load()) ? "T" : "F",
         this.k() ? "T" : "F",
         this.read_F()
      );
   }

   private String sync_Str() {
      ScaffoldModule.Inner_HashUtil line = this.init_aI_HashUtil;
      if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 d = line.load().normalize();
         Vec3 left = new Vec3(d.z, 0.0, -d.x);
         Vec3 position = MC.player.position();
         double error = init_D_aI_HashUtil_Vec3(line, position);
         Vec3 velocity = MC.player.getDeltaMovement();
         double perp = new Vec3(velocity.x, 0.0, velocity.z).dot(left);
         return String.format(Locale.ROOT, "lane=%+.3f/perp%+.3f", error, perp);
      } else {
         return "lane=--";
      }
   }

   private String flush_Str() {
      ScaffoldModule.Inner_FileCache lock = this.flags_Z() ? this.init_aI_FileCache() : null;
      int age = this.f_aG_I < 0 ? -1 : ck.init_ck().state_I() - this.f_aG_I;
      String sticky = "stk=" + (this.load_aI_TableIndex == null ? "none" : age + "/" + this.f_aH_I);
      return lock == null
         ? "lock=none " + sticky
         : String.format(Locale.ROOT, "lock=%s/y%d/p%d/f[%d,%d] %s", lock.init_Z() ? "x" : "z", lock.load(), lock.init_I(), lock.write(), lock.update(), sticky);
   }

   private void init_V_Str_2(String owner, Input before, Input after) {
      if (this.get_Z() && owner != null && before != null && after != null) {
         StringBuilder diff = new StringBuilder();
         init_V_StringBuilder(diff, 'F', before.forward(), after.forward());
         init_V_StringBuilder(diff, 'B', before.backward(), after.backward());
         init_V_StringBuilder(diff, 'L', before.left(), after.left());
         init_V_StringBuilder(diff, 'R', before.right(), after.right());
         init_V_StringBuilder(diff, 'J', before.jump(), after.jump());
         init_V_StringBuilder(diff, 'S', before.shift(), after.shift());
         init_V_StringBuilder(diff, 'P', before.sprint(), after.sprint());
         if (diff.length() != 0) {
            if (this.init_StringBuilder.length() > 0) {
               this.init_StringBuilder.append(',');
            }

            this.init_StringBuilder.append(owner).append(':').append(diff);
         }
      }
   }

   private static void init_V_StringBuilder(StringBuilder out, char name, boolean before, boolean after) {
      if (before != after) {
         out.append((char)(after ? '+' : '-')).append(name);
      }
   }

   private void init_V_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (MC.player != null && target != null) {
         Vec3 eye = MC.player.getEyePosition();
         float yaw = this.load_ce_DataUtil != null ? this.load_ce_DataUtil.init() : MC.player.getYRot();
         float demand = init_F_Vec3(eye, target.init_BlockPos(), target.init_Direction(), yaw);
         double xing = init_D_Vec3_3(eye, target.init_BlockPos(), target.init_Direction(), yaw);
         this.tick_Str = String.format(
            Locale.ROOT,
            "past=%+.3f/%.2f demand=%s/%.1f drop=%.2f xing=%s",
            init_D_Vec3(eye, target.init_BlockPos(), target.init_Direction()),
            0.05,
            Float.isNaN(demand) ? "nan" : String.format(Locale.ROOT, "%.1f", demand),
            89.5F,
            eye.y - (target.init_BlockPos().getY() + 1.0 - 0.5),
            Double.isNaN(xing) ? "--" : String.format(Locale.ROOT, "%+.2f", xing)
         );
      } else {
         this.tick_Str = "--";
      }
   }

   private void load_V_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      AsyncTraceLogger.init(
         String.format(
            Locale.ROOT,
            "[scaffold-why] t%03d lr=%s | strip=%s | clk=%s %s | rsv=%s | pick=%s plan=%s | tgt=%s",
            this.f_au_I,
            this.pop_Str,
            this.init_StringBuilder.length() == 0 ? "none" : this.init_StringBuilder.toString(),
            this.add_Str,
            this.tick_Str,
            this.get_Str,
            this.stop_Str,
            this.reset_Str.isEmpty() ? "ok" : this.reset_Str,
            read_Str_aI_TableIndex(target)
         )
      );
   }

   private String close_Str() {
      return "gate=edge"
         + (this.f_d_Z ? "1" : "0")
         + ".fall"
         + (this.f_e_Z ? "1" : "0")
         + ".brink"
         + (this.f_f_Z ? "1" : "0")
         + ".owed"
         + (this.f_g_Z ? "1" : "0")
         + ".pbrink"
         + (this.f_h_Z ? "1" : "0")
         + ".last"
         + (this.f_i_Z ? "1" : "0")
         + ".nofoot"
         + (this.scan_Z() ? "1" : "0")
         + ".resc"
         + (this.bind_Z() ? "1" : "0")
         + ".div"
         + (this.a_Z() ? "1" : "0")
         + ".rise"
         + (this.f_j_Z ? "1" : "0")
         + " brake="
         + this.sync_Str
         + (Double.isNaN(this.f_y_D) ? "" : String.format(Locale.ROOT, " dclimb=%.0f/%d", this.f_y_D, 400L))
         + " lovl="
         + this.flush_Str
         + " stand="
         + this.close_Str
         + String.format(Locale.ROOT, " trv=%.2f", this.f_B_D)
         + " jmp="
         + this.open_Str
         + " arc="
         + this.open_Str()
         + " "
         + this.poll_Str()
         + " tkoff="
         + (this.start_BlockPos == null ? "--" : this.start_BlockPos.toShortString())
         + ":"
         + this.next_Str
         + " riser="
         + this.push_Str
         + " click="
         + (this.f_l_Z ? "1" : "0")
         + " free="
         + (this.f_k_Z ? "1" : "0")
         + " anch="
         + this.poll_Str
         + this.read_Str()
         + " pin="
         + (
            this.write_Direction == null
               ? "--"
               : this.write_Direction.getName().charAt(0) + "@" + (this.prev_BlockPos == null ? "?" : this.prev_BlockPos.toShortString())
         )
         + (Double.isNaN(this.f_Z_D) ? " cross=--" : String.format(Locale.ROOT, " cross=%.2f", this.f_Z_D))
         + " foot="
         + this.push_Str()
         + " held="
         + this.init_ItemStack_2().getCount()
         + " why="
         + this.add_Str;
   }

   private String open_Str() {
      if (MC.player != null && MC.level != null && !MC.player.onGround()) {
         String budget = this.f_ae_I + "/" + this.f_af_I;
         int row = this.close_I();
         Vec3 landing = init_Vec3_Vec3(MC.player.position(), MC.player.getDeltaMovement(), row + 1.0, 12, 0.02);
         if (landing == null) {
            return budget + "@--";
         }

         BlockPos cell = BlockPos.containing(landing.x, row + 0.5, landing.z);
         if (MC.level.isOutsideBuildHeight(cell)) {
            return budget + "@oob";
         }

         String at = "@" + cell.getX() + "," + cell.getZ();
         int deficit = this.load_I_BlockPos(cell);
         return budget + at + (deficit == 0 ? ":ok" : (deficit == 1 ? ":n1" : ":n0"));
      } else {
         return "--";
      }
   }

   private void stop_V() {
      if (MC.player != null && MC.level != null && !MC.player.onGround()) {
         BlockPos support = this.put_BlockPos;
         if (support != null) {
            if (!support.equals(this.stop_BlockPos)) {
               if (this.stop_BlockPos != null) {
                  this.f_bk_I++;
               }

               this.write_V_BlockPos(support);
            }

            if (this.f_bf_I < 0 && "support".equals(this.update_Str)) {
               this.f_bf_I = this.f_au_I;
            }

            if (this.f_bg_I < 0 && this.open(this.stop_BlockPos)) {
               this.f_bg_I = this.f_au_I;
            }

            BlockPos riser = this.next_BlockPos;
            if (riser != null && !MC.level.isOutsideBuildHeight(riser)) {
               if (this.f_bj_I < 0 && this.open(riser)) {
                  this.f_bj_I = this.f_au_I;
               }

               String clear = init_Str_AABB(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), riser, this.push(riser));
               if ("ok".equals(clear)) {
                  if (this.f_bh_I < 0) {
                     this.f_bh_I = this.f_au_I;
                  }

                  this.f_bi_I = this.f_au_I;
                  this.put_Str = this.push_Str;
               } else {
                  this.set_Str = clear;
               }
            }
         }
      }
   }

   private void write_V_BlockPos(BlockPos support) {
      this.stop_BlockPos = support == null ? null : support.immutable();
      this.next_BlockPos = support == null ? null : support.above().immutable();
      this.f_bf_I = -1;
      this.f_bg_I = -1;
      this.f_bh_I = -1;
      this.f_bi_I = -1;
      this.f_bj_I = -1;
      this.set_Str = "--";
      this.put_Str = "--";
   }

   private static String init_Str_I(int tick) {
      return tick < 0 ? "--" : String.format(Locale.ROOT, "t%03d", tick);
   }

   private String poll_Str() {
      String support = this.put_BlockPos == null ? "--" : this.put_BlockPos.toShortString();
      String connector = this.add_BlockPos == null ? "--" : this.add_BlockPos.toShortString();
      String deficit = "?";
      if (this.put_BlockPos != null && MC.level != null && !MC.level.isOutsideBuildHeight(this.put_BlockPos)) {
         int count = this.load_I_BlockPos(this.put_BlockPos);
         deficit = count == 0 ? "ok" : (count == 1 ? "n1" : "n0");
      }

      return "reserve=" + this.update_Str + "@" + support + ":" + deficit + "/" + connector;
   }

   private String push_Str() {
      if (MC.level == null) {
         return "?";
      } else {
         BlockPos support = BlockPos.containing(MC.player.position()).below();
         if (MC.level.isOutsideBuildHeight(support)) {
            return "oob";
         } else {
            return this.init_Z_BlockState_BlockPos(MC.level.getBlockState(support), support) ? "solid" : "AIR";
         }
      }
   }

   private static String init_Str_ce_DataUtil(ce.Inner_DataUtil rotation) {
      return rotation == null ? "null" : String.format(Locale.ROOT, "%.1f,%.1f", rotation.init(), rotation.load());
   }

   private static String read_Str_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (target == null) {
         return "none";
      }

      Vec3 p = target.init_BlockHitResult().getLocation();
      return target.init_Direction()
         + "@"
         + target.init_BlockPos().toShortString()
         + "->"
         + target.load().toShortString()
         + String.format(Locale.ROOT, " pt=%.2f,%.2f,%.2f", p.x, p.y, p.z);
   }

   private void next_V() {
      this.init_V_Z_6(true);
   }

   private ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil goal) {
      return this.init_ce_DataUtil_ce_DataUtil_Z(goal, false);
   }

   private ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil_Z(ce.Inner_DataUtil goal, boolean holdPitch) {
      int tick = ck.init_ck().state_I();
      if (this.f_y_I == tick && this.load_ce_DataUtil != null) {
         return this.load_ce_DataUtil;
      }

      if (MC.player == null) {
         return this.load_ce_DataUtil;
      }

      this.sync_Z = false;
      this.f_bG_I = 0;
      if (this.flush_ce_DataUtil == null) {
         this.flush_ce_DataUtil = ce.init(MC.player);
         this.init_dg.init_V_J(this.init_Random.nextLong());
         this.read_ce_DataUtil = null;
         this.pool_F = 0.0F;
      }

      double gcd = ce.init();
      this.flush_ce_DataUtil = init_ce_DataUtil_dg(
         this.init_dg,
         this.flush_ce_DataUtil,
         ce.init(goal.init(), this.flush_ce_DataUtil.init()),
         holdPitch ? 0.0F : Mth.clamp(goal.load(), -90.0F, 90.0F) - this.flush_ce_DataUtil.load(),
         this.init_F_ce_DataUtil(goal),
         gcd,
         init_F_Z_F(this.block_Z(), 18.0F),
         init_F_Z_F(this.block_Z(), 20.0F)
      );
      float previousPitch = this.load_ce_DataUtil == null ? 0.0F / 0.0F : this.load_ce_DataUtil.load();
      this.load_ce_DataUtil = init_ce_DataUtil_ce_DataUtil(this.flush_ce_DataUtil, this.load_ce_DataUtil, gcd, this.write_I_5(), this.f_b_F);
      this.f_a_F = Float.isNaN(previousPitch) ? 0.0F : Math.abs(this.load_ce_DataUtil.load() - previousPitch);
      this.f_v_I = 5;
      this.f_y_I = tick;
      return this.load_ce_DataUtil;
   }

   static ce.Inner_DataUtil init_ce_DataUtil_dg(dg smoother, ce.Inner_DataUtil current, ce.Inner_DataUtil goal, float directionChange, double gcd) {
      return init_ce_DataUtil_dg(
         smoother, current, ce.init(goal.init(), current.init()), Mth.clamp(goal.load(), -90.0F, 90.0F) - current.load(), directionChange, gcd
      );
   }

   static ce.Inner_DataUtil init_ce_DataUtil_dg(dg smoother, ce.Inner_DataUtil current, float yawError, float pitchError, float directionChange, double gcd) {
      return init_ce_DataUtil_dg(smoother, current, yawError, pitchError, directionChange, gcd, 18.0F, 20.0F);
   }

   static ce.Inner_DataUtil init_ce_DataUtil_dg(
      dg smoother, ce.Inner_DataUtil current, float yawError, float pitchError, float directionChange, double gcd, float maxYawStep, float maxPitchStep
   ) {
      dg.Inner_DataUtil step = smoother.init_dg_DataUtil_F_2(yawError, pitchError, gcd, maxYawStep, maxPitchStep, 180.0F, directionChange, 0.35F, true, true);
      ce.Inner_DataUtil delta = new dg.Inner_DataUtil(
            init_J_J(load_J_J(step.init(), maxYawStep, gcd), yawError, gcd), init_J_J(load_J_J(step.load(), maxPitchStep, gcd), pitchError, gcd)
         )
         .init(gcd);
      return new ce.Inner_DataUtil(Mth.wrapDegrees(current.init() + delta.init()), Mth.clamp(current.load() + delta.load(), -90.0F, 90.0F));
   }

   static long init_J_J(long counts, float error, double gcd) {
      if (Double.isFinite(gcd) && !(gcd <= 1.0E-9)) {
         return Math.abs(error) < gcd * 0.5 ? 0L : counts;
      } else {
         return counts;
      }
   }

   static long load_J_J(long counts, float maxDegrees, double gcd) {
      if (Double.isFinite(gcd) && !(gcd <= 1.0E-9)) {
         long limit = Math.max(1L, (long)Math.floor(maxDegrees / gcd));
         return Math.max(-limit, Math.min(limit, counts));
      } else {
         return counts;
      }
   }

   private float init_F_ce_DataUtil(ce.Inner_DataUtil goal) {
      float change = this.read_ce_DataUtil == null ? 0.0F : Math.min(1.0F, ce.load(this.read_ce_DataUtil, goal) / 45.0F);
      this.read_ce_DataUtil = goal;
      this.pool_F = this.pool_F * 0.65F + change * 0.35F;
      return this.pool_F;
   }

   static float init_F_F_D(float angle, double gcd) {
      if (angle % 1.0F == 0.0F && angle != 0.0F && angle != 90.0F && angle != -90.0F) {
         float step = (float)(gcd > 1.0E-6 ? gcd : 0.0225);
         float nudged = angle + step;
         return nudged != 90.0F && nudged != -90.0F ? nudged : angle - step;
      } else {
         return angle;
      }
   }

   static float init_F_F_3(float yaw) {
      return (float)(yaw - Math.round(yaw / 45.0) * 45.0);
   }

   static boolean load_Z_F(float yaw) {
      return Math.abs(init_F_F_3(yaw)) >= 0.2F && yaw % 1.0F != 0.0F;
   }

   static float init_F_F_2(float candidate, double gcd, int preferSign, float forbidden, float lastPlaceYaw) {
      float step = (float)(gcd > 1.0E-6 ? gcd : 0.0225);
      int direction = preferSign != 0 ? preferSign : (init_F_F_3(candidate) >= 0.0F ? 1 : -1);
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

   static ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil(
      ce.Inner_DataUtil raw, ce.Inner_DataUtil previousEmitted, double gcd, int ditherCounts, float lastPlaceYaw
   ) {
      if (raw == null) {
         return null;
      }

      float previousYaw = previousEmitted == null ? 0.0F / 0.0F : previousEmitted.init();
      float candidate = Mth.wrapDegrees((float)(raw.init() + ditherCounts * gcd));
      int prefer = previousEmitted == null ? 0 : (int)Math.signum(ce.init(raw.init(), previousYaw));
      return new ce.Inner_DataUtil(init_F_F_2(candidate, gcd, prefer, previousYaw, lastPlaceYaw), init_F_F_D(Mth.clamp(raw.load(), -90.0F, 90.0F), gcd));
   }

   private int write_I_5() {
      int next = this.f_bF_I;

      while (next == this.f_bF_I) {
         next = this.init_Random.nextInt(3) - 1;
      }

      this.f_bF_I = next;
      return next;
   }

   private ScaffoldModule.Inner_TextFormat init_aI_TextFormat() {
      return init_aI_TextFormat_ch_CachePool(ch.init_ch_CachePool());
   }

   static ScaffoldModule.Inner_TextFormat init_aI_TextFormat_ch_CachePool(ch.Inner_CachePool snapshot) {
      if (snapshot != null && snapshot.init_Z() && Float.isFinite(snapshot.read()) && Float.isFinite(snapshot.write())) {
         ce.Inner_DataUtil current = new ce.Inner_DataUtil(snapshot.read(), snapshot.write());
         ce.Inner_DataUtil previous = Float.isFinite(snapshot.init_F()) && Float.isFinite(snapshot.load())
            ? new ce.Inner_DataUtil(snapshot.init_F(), snapshot.load())
            : current;
         return new ScaffoldModule.Inner_TextFormat(previous, current, snapshot.init_I());
      } else {
         return null;
      }
   }

   static float init_F_ch_CachePool(ch.Inner_CachePool snapshot) {
      ScaffoldModule.Inner_TextFormat wire = init_aI_TextFormat_ch_CachePool(snapshot);
      return wire == null ? 0.0F / 0.0F : Math.abs(wire.load().load() - wire.init_ce_DataUtil().load());
   }

   private String pop_Str() {
      this.store_V();
      String wire = !this.mark_Z ? "--" : (this.scan_Z ? "T" : "F") + (this.parse_Z ? "H" : "-") + (this.bind_Z ? "P" : "S");
      return String.format(
            Locale.ROOT,
            "use=%s/g%d/o%d:%d/s%d/%s%s ack=%d/%d q=%d+%d wire=%s",
            this.init_aI_SyncManager.name().toLowerCase(Locale.ROOT),
            this.put_J,
            this.f_aK_I,
            this.f_aL_I,
            this.f_aM_I,
            this.write_Str,
            this.drain_Z ? "C" : "-",
            this.f_aO_I,
            this.f_aN_I,
            this.flush_ArrayDeque.size(),
            this.read_Map.size(),
            wire
         )
         + (this.put_Z() ? " pend=T" : "");
   }

   private boolean put_Z() {
      if (MC.player != null && !this.flush_ArrayDeque.isEmpty()) {
         AABB box = MC.player.getBoundingBox();
         int below = Mth.floor(box.minY - 0.001);

         for (ScaffoldModule.Inner_UiHelper placement : this.flush_ArrayDeque) {
            BlockPos cell = placement.init_BlockPos();
            if (cell.getY() == below && cell.getX() + 1 > box.minX && cell.getX() < box.maxX && cell.getZ() + 1 > box.minZ && cell.getZ() < box.maxZ) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean load_Z_aI_TableIndex_ce_DataUtil(ScaffoldModule.Inner_TableIndex target, ce.Inner_DataUtil clickRotation) {
      if (MC.player != null && MC.level != null && clickRotation != null) {
         double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
         BlockHitResult ray = init_BlockHitResult_Vec3(MC.player.getEyePosition(), clickRotation, reach, MC.level, MC.player);
         this.init_BlockHitResult = ray;
         return init_Z_BlockHitResult(ray, target, this.load_Z_BlockHitResult_aI_TableIndex(ray, target));
      } else {
         return false;
      }
   }

   private String write_Str_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (!this.get_Z()) {
         return "";
      }

      ce.Inner_DataUtil rotation = this.update_ce_DataUtil;
      if (rotation == null) {
         ScaffoldModule.Inner_TextFormat wire = this.init_aI_TextFormat();
         rotation = wire == null ? null : wire.load();
      }

      if (MC.player != null && MC.level != null && rotation != null) {
         double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
         BlockHitResult ray = init_BlockHitResult_Vec3(MC.player.getEyePosition(), rotation, reach, MC.level, MC.player);
         return ray == null
            ? " miss=no-hit"
            : " miss=hit:"
               + ray.getBlockPos().toShortString()
               + "/"
               + ray.getDirection().getName().charAt(0)
               + String.format(Locale.ROOT, "@%.2f", ray.getLocation().y)
               + " want:"
               + target.init_BlockPos().toShortString()
               + "/"
               + target.init_Direction().getName().charAt(0)
               + String.format(Locale.ROOT, ">=%.2f", target.init_D());
      } else {
         return " miss=no-state";
      }
   }

   private void init_V_Z_6(boolean allowPreAim) {
      if (MC != null && MC.player != null) {
         if (allowPreAim && this.c_Z()) {
            this.init_ce_DataUtil_ce_DataUtil(this.open_ce_DataUtil());
         } else if (!allowPreAim || !this.add_Z() && !this.clear_Z()) {
            if (allowPreAim && this.reset_Z()) {
               this.init_ce_DataUtil_ce_DataUtil(this.write_ce_DataUtil());
            } else {
               if (this.load_ce_DataUtil != null) {
                  this.sync_Z = true;
               }

               this.valid_V();
            }
         } else {
            this.init_ce_DataUtil_ce_DataUtil(this.write_ce_DataUtil());
         }
      }
   }

   private boolean add_Z() {
      LocalPlayer player = MC.player;
      if (!player.onGround()) {
         return false;
      }

      Vec3 motion = player.getDeltaMovement();
      if (!this.init_Z_ItemStack_4(this.init_ItemStack_2())) {
         return false;
      }

      Vec3 direction;
      if (this.init_aI_HashUtil != null && this.init_aI_HashUtil.load().horizontalDistanceSqr() > 1.0E-8) {
         direction = this.init_aI_HashUtil.load();
      } else {
         if (!(motion.horizontalDistance() > 0.03)) {
            return false;
         }

         direction = new Vec3(motion.x, 0.0, motion.z).normalize();
      }

      Vec3 position = player.position();

      for (double distance : init_DArr) {
         if (MC.level.getBlockState(init_BlockPos_Vec3(position.add(direction.scale(distance)))).isAir()) {
            return true;
         }
      }

      return false;
   }

   private boolean clear_Z() {
      boolean hasCourse = this.init_aI_HashUtil != null && this.init_aI_HashUtil.load().horizontalDistanceSqr() > 1.0E-8;
      return init_Z_Z_3(MC.player.onGround(), hasCourse, this.init_Z_ItemStack_4(this.init_ItemStack_2()));
   }

   static boolean init_Z_Z_3(boolean onGround, boolean hasCourse, boolean hasPlaceableBlock) {
      return !onGround && hasCourse && hasPlaceableBlock;
   }

   private boolean reset_Z() {
      return !this.init_Z_ItemStack_4(this.init_ItemStack_2())
         ? false
         : init_Z_Z(
            init_Z_KeyMapping(MC.options.keyUp)
               || init_Z_KeyMapping(MC.options.keyDown)
               || init_Z_KeyMapping(MC.options.keyLeft)
               || init_Z_KeyMapping(MC.options.keyRight),
            this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load(),
            this.read_Vec3_aI_HashUtil(this.init_aI_HashUtil),
            ck.init_ck().state_I(),
            this.f_z_I
         );
   }

   static boolean init_Z_Z(boolean directionalKeyHeld, Vec3 courseDirection, Vec3 fallOff, int clientTick, int lastPlacementTick) {
      int sincePlacement = clientTick - lastPlacementTick;
      return sincePlacement >= 0 && sincePlacement <= 20
         ? true
         : directionalKeyHeld && courseDirection != null && courseDirection.horizontalDistanceSqr() > 1.0E-8 && fallOff != null;
   }

   private ce.Inner_DataUtil write_ce_DataUtil() {
      float pitch = this.count_F;
      if (Float.isFinite(this.f_c_F) && this.update_Z_I(ck.init_ck().state_I())) {
         pitch = this.f_c_F;
      }

      float yaw = this.read_F();
      if (this.f_aJ_I != ck.init_ck().state_I() && this.f_q_I != -1 && this.f_r_I >= 2) {
         int candidateLane = init_I_I_I(this.f_q_I, this.f_s_I);
         if (candidateLane != -1) {
            yaw = read_F_F(init_F_I(candidateLane));
         }
      }

      return new ce.Inner_DataUtil(yaw, pitch);
   }

   static float load_F_F_2(float solved) {
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

   static boolean read_Z_F(float emitted) {
      return emitted <= 89.5F;
   }

   static float read_F_F(float movementYaw) {
      return Mth.wrapDegrees(movementYaw + 180.0F);
   }

   static float init_F_F_F(float sentYaw, float vanillaYaw) {
      return Float.isNaN(sentYaw) ? 0.0F : sentYaw - vanillaYaw;
   }

   static float load_F_F_F(float sentYaw, float cameraYaw) {
      return Float.isFinite(sentYaw) && Float.isFinite(cameraYaw) ? 360.0F * Math.round((sentYaw - cameraYaw) / 360.0F) : 0.0F;
   }

   private void prev_V() {
      if (MC != null && MC.player != null && autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue)) {
         LocalPlayer player = MC.player;
         float offset = load_F_F_F(f_l_F, player.getYRot());
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

   static int init_I_F_F(float yawDebt, float pitchDelta) {
      int yawTicks = (int)Math.ceil(Math.abs(yawDebt) / 37.0F);
      int pitchTicks = (int)Math.ceil(Math.abs(pitchDelta) / 37.0F);
      return Mth.clamp(Math.max(yawTicks, pitchTicks) + 4, 8, 27);
   }

   static boolean read_Z_F_F(float yawDebt, float pitchDelta) {
      return Math.abs(yawDebt) <= 2.0F && Math.abs(pitchDelta) <= 2.0F;
   }

   private void valid_V() {
      if (this.sync_Z) {
         if (MC != null && MC.player != null && this.load_ce_DataUtil != null) {
            this.prev_V();
            if (this.flush_ce_DataUtil == null) {
               this.flush_ce_DataUtil = this.load_ce_DataUtil;
            }

            ce.Inner_DataUtil camera = ce.init(MC.player);
            double gcd = ce.init();
            float debt = init_F_F_F(f_l_F, MC.player.getYRot());
            float pitchDelta = camera.load() - this.flush_ce_DataUtil.load();
            float yawCap = this.f_bG_I == 0 ? 20.0F : 37.0F;
            float pitchCap = this.f_bG_I == 0 ? 20.0F : 37.0F;
            if (autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue)) {
               this.load_ce_DataUtil = ce.init(this.load_ce_DataUtil, camera, 95.0F, 95.0F, gcd);
               autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, this.load_ce_DataUtil);
               this.flush_ce_DataUtil = this.load_ce_DataUtil;
            } else {
               this.flush_ce_DataUtil = init_ce_DataUtil_dg(
                  this.init_dg, this.flush_ce_DataUtil, -debt, pitchDelta, this.init_F_ce_DataUtil(camera), gcd, yawCap, pitchCap
               );
               this.load_ce_DataUtil = init_ce_DataUtil_ce_DataUtil(this.flush_ce_DataUtil, this.load_ce_DataUtil, gcd, this.write_I_5(), this.f_b_F);
            }

            this.f_bG_I++;
            this.f_w_I = Math.max(this.f_w_I, Math.min(init_I_F_F(debt, pitchDelta), 27 - this.f_bG_I));
            if (this.f_w_I > 0) {
               this.f_w_I--;
            }

            this.f_v_I = Math.max(this.f_v_I, 1);
            if (read_Z_F_F(debt, pitchDelta)) {
               this.load_V_18();
            } else if (this.f_bG_I >= 27) {
               this.init_V_Str_aI_TableIndex("wind-down-forced d=" + String.format(Locale.ROOT, "%.2f", debt), null);
               this.load_V_18();
            }
         } else {
            this.load_V_18();
         }
      }
   }

   void load_V_18() {
      this.prev_V();
      this.load_ce_DataUtil = null;
      this.flush_ce_DataUtil = null;
      this.f_v_I = 0;
      this.f_y_I = -2147483648;
      this.sync_Z = false;
      this.f_w_I = 0;
      this.f_bG_I = 0;
      this.f_bF_I = 0;
      this.init_dg.load_V();
      this.read_ce_DataUtil = null;
      this.pool_F = 0.0F;
      autismclient.util.ap.init_V_ap_TaskQueue(this.init_ap_TaskQueue);
   }

   void read_V_12() {
      this.count_F = 68.0F + this.init_Random.nextFloat() * 12.0F;
   }

   private void size_V() {
      this.run_Z = false;
      this.entry_Z = false;
      this.node_Z = false;
      this.f_X_I = 1;
      this.init_aI_PathHelper = null;
      this.f_Y_I = -2147483648;
      this.value_Z = false;
      this.init_aI_c = ScaffoldModule.Inner_c.NONE;
      this.update_Input = null;
      this.node_V();
      if (!this.cache_Z()) {
         this.block_V();
         this.valid_V();
         this.g_V();
      } else {
         LocalPlayer player = MC.player;
         this.level_F = Mth.clamp(player.getXRot(), -90.0F, 90.0F);
         Input liveKeys = new Input(
            init_Z_KeyMapping(MC.options.keyUp),
            init_Z_KeyMapping(MC.options.keyDown),
            init_Z_KeyMapping(MC.options.keyLeft),
            init_Z_KeyMapping(MC.options.keyRight),
            false,
            false,
            false
         );
         this.f_D_I = liveKeys.equals(this.read_Input) ? this.f_D_I + 1 : 1;
         this.read_Input = liveKeys;
         this.load_Input = init_Input_Input(this.load_Input, liveKeys, this.f_D_I, this.push_Z);
         boolean physicalForward = this.load_Input.forward();
         boolean physicalSpace = (!this.count_Z() || this.bool("eagle-jump")) && init_Z_KeyMapping(MC.options.keyJump);
         boolean physicalMoving = init_Z_Z_5(physicalForward, this.load_Input.backward(), this.load_Input.left(), this.load_Input.right());
         this.put_Z = physicalSpace;
         if (!physicalSpace) {
            this.reset_Z = false;
         }

         if (player.onGround() || !this.push_Z) {
            this.init_Input = this.load_Input;
         }

         this.tick_Z = false;
         if (!AutismLiteVariant.enabled()) {
            this.tick_Z = DangerModule.poll_Z_6();
         }

         boolean rawStop = !physicalMoving || this.tick_Z;
         boolean airborneOwned = this.push_Z && !player.onGround();
         boolean stopSafeHere = !rawStop || airborneOwned || !this.push_Z || this.init_aI_b.init_Z() || this.init_Z_LocalPlayer(player);
         this.init_aI_b = init_aI_b_aI_b(this.init_aI_b, rawStop, airborneOwned, stopSafeHere);
         this.pop_Z = this.init_aI_b.init_Z();
         if (load_Z_Z_2(physicalForward, physicalSpace, this.get_Z, this.push_Z)) {
            this.set_Z = true;
         }

         this.get_Z = physicalSpace;
         if (player.onGround()) {
            BlockPos groundedSupport = this.init_BlockPos_LocalPlayer(player);
            if (groundedSupport != null) {
               this.flush_BlockPos = groundedSupport.immutable();
               this.f_ac_I = ck.init_ck().state_I();
            }
         }

         if (!this.push_Z) {
            if (!player.onGround() && this.write_Z_LocalPlayer(player)) {
               this.update_V_LocalPlayer(player);
            } else {
               if (!physicalMoving || !player.onGround()) {
                  if (!physicalMoving) {
                     this.prev_Z = false;
                  }

                  this.init_V_Z_6(false);
                  this.g_V();
                  return;
               }

               BlockPos under = this.init_BlockPos_LocalPlayer(player);
               if (under == null) {
                  this.init_V_Z_6(false);
                  this.g_V();
                  return;
               }

               if (!this.init_Z_ItemStack_4(this.init_ItemStack_2())) {
                  this.init_V_Z_6(false);
                  this.g_V();
                  return;
               }

               this.init_V_LocalPlayer_BlockPos(player, under);
            }
         }

         this.pop_Z = this.init_aI_b.init_Z();
         if (this.pop_Z && !player.onGround()) {
            this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
         }

         if (this.pop_Z && !this.start_Z) {
            this.set_Z = false;
            this.clear_Z = false;
            if (!this.stop_Z && this.init_aI_DataPipe != null && this.init_aI_DataPipe.init_Z()) {
               this.init_aI_DataPipe = null;
            }
         }

         this.open_V(player);
         if (this.prev_Z) {
            this.init_V_LocalPlayer(player);
         }

         this.entry_Z = this.push(player);
         if (!this.count_Z()
            && !player.onGround()
            && this.init_aI_DataPipe == null
            && !this.stop_Z
            && (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING)
            && (!this.open_Z(player) || this.load_BlockPos_LocalPlayer(player) == null)) {
            this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, this.init_ItemStack_2());
         }

         this.close_V(player);
         this.load_ce_DataUtil = this.init_ce_DataUtil_LocalPlayer(player);
         this.f_v_I = 5;
         if (player.onGround()) {
            this.load_V_LocalPlayer(player);
         } else if (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RECOVERING) {
            this.sync_V(player);
            this.read_V_LocalPlayer(player);
         } else {
            this.read_V_LocalPlayer(player);
         }

         if (!this.cache_Z) {
            this.poll_V(player);
            if (this.push_Z && player.onGround() && this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.HOLD && !this.pop_Z && !this.add_Z) {
               if (++this.f_W_I >= 30) {
                  this.f_W_I = 0;
                  this.f_R_I = 0;
                  this.task_Z = false;
                  this.f_aa_I = 0;
                  this.f_ab_I = 0;
                  this.f_S_I = 0;
                  this.data_V();
               }
            } else {
               this.f_W_I = 0;
            }

            if (this.push_Z) {
               if (this.init_aI_NodeWalker != ScaffoldModule.Inner_NodeWalker.PLACING) {
                  this.init_InteractionHand();
               }

               this.f_V();
            }

            this.g_V();
         }
      }
   }

   private void init_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      this.write_BlockPos = null;
      this.reset_Z = false;
      this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.FORWARD;
      this.init_aI_CacheEntry = null;
      this.close_Z = false;
      this.push_Z = true;
      this.pop_Z = false;
      this.add_Z = false;
      this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.FORWARD_DWELL;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
      this.sync_Z = false;
      this.f_bG_I = 0;
      if (!autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue)) {
         autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, this.poll_ce_DataUtil());
      }

      this.write_ce_DataUtil = autismclient.util.ap.init_ce_DataUtil_ap_TaskQueue(this.init_ap_TaskQueue);
      float lookYaw = this.init_F_LocalPlayer(player);
      boolean hadCourse = this.prev_Z && Float.isFinite(this.flags_F);
      float previousAnchor = this.flags_F;
      this.data_V();
      if (hadCourse) {
         float lookDeviation = Math.abs(ce.init(this.flags_F, lookYaw));
         if (lookDeviation > 45.0F) {
            float snapped = write_F_F(lookYaw);
            this.flags_F = Mth.wrapDegrees(snapped);
         } else {
            this.flags_F = Mth.wrapDegrees(this.flags_F);
         }
      } else {
         this.flags_F = Mth.wrapDegrees(write_F_F(lookYaw));
      }

      this.prev_Z = true;
      if (hadCourse && Float.compare(write_F_F(previousAnchor), write_F_F(this.flags_F)) != 0) {
         this.init_V_BlockPos_Direction(under, init_Direction_F(previousAnchor));
         this.value_V();
      }

      this.level_F = Mth.clamp(player.getXRot(), -90.0F, 90.0F);
      this.init_Vec3 = this.read_Vec3_BlockPos(under, player.position(), this.flags_F);
      this.f_x_D = init_D_Vec3_F(this.init_Vec3, this.flags_F);
      this.f_R_I = 0;
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.mark_V();
      this.sync_BlockPos = under.immutable();
      this.f_L_I = under.getY();
      this.f_K_I = 0;
      this.f_H_I = 0;
      this.buffer_Z = false;
      this.f_S_I = 0;
      this.init_aI_DataPipe = null;
      this.open_BlockPos = null;
      this.stop_Z = false;
      this.f_V_I = -1;
      this.data_Z = true;
   }

   private void init_V_LocalPlayer(LocalPlayer player) {
      float deviation = Math.abs(ce.init(this.flags_F, this.init_F_LocalPlayer(player)));
      this.f_R_I = init_I_I_F(this.f_R_I, deviation);
   }

   static int init_I_I_F(int current, float deviation) {
      if (deviation > 45.0F) {
         return current + 1;
      } else {
         return deviation < 35.0F ? 0 : Math.max(0, current - 1);
      }
   }

   static Input init_Input_Input(Input current, Input live, int stableTicks, boolean owned) {
      return current != null && owned && stableTicks < 3 ? current : live;
   }

   static boolean init_Z_Z_5(boolean forward, boolean backward, boolean left, boolean right) {
      return forward || backward || left || right;
   }

   static ScaffoldModule.Inner_b init_aI_b_aI_b(ScaffoldModule.Inner_b previous, boolean rawStop, boolean airborneOwned, boolean stopSafeHere) {
      ScaffoldModule.Inner_b last = previous == null ? ScaffoldModule.Inner_b.init_aI_b : previous;
      if (airborneOwned) {
         return new ScaffoldModule.Inner_b(last.init_Z(), last.load() || rawStop && !last.init_Z());
      }

      boolean stop = rawStop && (last.init_Z() || stopSafeHere);
      return new ScaffoldModule.Inner_b(stop, rawStop && !stop);
   }

   private void load_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos laneSupport) {
      if (laneSupport != null && this.f_R_I >= 3) {
         this.f_R_I = 0;
         float previousAnchor = this.flags_F;
         Direction previousDirection = init_Direction_F(previousAnchor);
         float snapped = write_F_F(this.init_F_LocalPlayer(player));
         this.flags_F = Mth.wrapDegrees(snapped);
         this.prev_Z = true;
         this.init_Vec3 = this.read_Vec3_BlockPos(laneSupport, player.position(), this.flags_F);
         this.f_x_D = init_D_Vec3_F(this.init_Vec3, this.flags_F);
         this.scan_V();
         this.mark_V();
         if (Float.compare(write_F_F(previousAnchor), write_F_F(this.flags_F)) != 0) {
            this.init_V_BlockPos_Direction(laneSupport, previousDirection);
            this.value_V();
         }
      }
   }

   private void init_V_BlockPos_Direction(BlockPos support, Direction entryDirection) {
      this.data_V();
      if (support != null && entryDirection != null && entryDirection.getAxis().isHorizontal()) {
         this.poll_BlockPos = support.immutable();
         this.push_BlockPos = support.relative(entryDirection).immutable();
      }
   }

   private void data_V() {
      if (this.init_aI_DataPipe != null && this.push_BlockPos != null && this.push_BlockPos.equals(this.init_aI_DataPipe.init_aI_TableIndex().load())) {
         this.init_aI_DataPipe = null;
         this.f_V_I = -1;
      }

      this.poll_BlockPos = null;
      this.push_BlockPos = null;
      this.pool_Z = false;
   }

   private boolean start_Z() {
      return this.f_R_I >= 2;
   }

   private void value_V() {
      this.init_aI_DataPipe = null;
      this.f_V_I = -1;
      if (!this.task_Z) {
         this.task_Z = true;
         this.f_aa_I = 0;
         this.f_ab_I = 1;
         this.scan_V();
      }
   }

   private void load_V_LocalPlayer(LocalPlayer player) {
      if (!this.count_Z() || this.bool("eagle-jump") && this.data_Z()) {
         this.cache_Z = false;
         this.state_Z = false;
         BlockPos under = this.init_BlockPos_LocalPlayer(player);
         if (!this.pop_Z && !this.add_Z) {
            if (under != null && !init_Z_KeyMapping(MC.options.keyShift) && autismclient.util.U.init_Z(MC.level.getBlockState(under), under)) {
               this.flush_V_LocalPlayer_BlockPos(player, under);
            } else if (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RECOVERING) {
               this.sync_V_LocalPlayer_BlockPos(player, under != null ? under : this.sync_BlockPos);
            } else {
               if (under != null) {
                  float anchorBeforeTurn = this.flags_F;
                  this.load_V_LocalPlayer_BlockPos(player, under);
                  double laneError = this.f_x_D - init_D_Vec3_F(player.position(), this.flags_F);
                  if (anchorBeforeTurn != this.flags_F && Math.abs(laneError) > 0.55) {
                     this.init_Vec3 = this.read_Vec3_BlockPos(under, player.position(), this.flags_F);
                     this.f_x_D = init_D_Vec3_F(this.init_Vec3, this.flags_F);
                     this.scan_V();
                  }
               }

               if (under == null) {
                  if (this.sync_BlockPos == null) {
                     this.chunk_V();
                  } else if (this.f_H_I > 0 && this.flush_Z(player)) {
                     this.write_V_LocalPlayer_BlockPos(player, this.sync_BlockPos);
                  } else {
                     if (this.f_H_I > 0
                        || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH
                        || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING
                        || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING) {
                        this.update_V_BlockPos(this.sync_BlockPos);
                     }

                     this.load_V_LocalPlayer_BlockPos(player, this.sync_BlockPos);
                     if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, this.sync_BlockPos)) {
                        if (this.init_aI_DataPipe == null) {
                           ItemStack lipStack = this.init_ItemStack_2();
                           if (this.init_Z_ItemStack_4(lipStack)) {
                              this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, lipStack);
                           }
                        }

                        if (this.sync_Z_LocalPlayer_BlockPos(player, this.sync_BlockPos)) {
                           this.flush_V_LocalPlayer_BlockPos(player, this.sync_BlockPos);
                        } else if (this.init_aI_DataPipe != null && !this.start_Z() && this.flush_Z_LocalPlayer_BlockPos(player, this.sync_BlockPos)) {
                           if (this.value_Z()) {
                              this.init_V_LocalPlayer_aI_DataPipe(player, this.init_aI_DataPipe);
                           } else {
                              this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
                           }
                        } else {
                           if (this.start_Z()) {
                              this.f_S_I++;
                              if (this.f_S_I < 20) {
                                 this.write_V_LocalPlayer_BlockPos(player, this.sync_BlockPos);
                                 return;
                              }

                              this.f_R_I = 0;
                              this.f_S_I = 0;
                           }

                           if (!this.init_Z_ItemStack_4(this.init_ItemStack_2())) {
                              this.write_V_LocalPlayer_BlockPos(player, under);
                           } else {
                              this.flush_V_LocalPlayer_BlockPos(player, this.sync_BlockPos);
                           }
                        }
                     }
                  }
               } else if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, under)) {
                  if (this.f_H_I > 0
                     || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH
                     || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING
                     || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING) {
                     boolean interruptedLanding = this.queue_Z || this.next_Z;
                     this.update_V_BlockPos(under);
                     this.close_ce_DataUtil();
                     if (interruptedLanding) {
                        this.flush_V_LocalPlayer_BlockPos(player, under);
                        return;
                     }

                     this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RUNNING;
                     this.f_K_I = 0;
                  }

                  if (this.init_aM != null && this.init_aM.init_Z() && !this.data_Z()) {
                     this.close_ce_DataUtil();
                     this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
                     Input carry = this.init_Input_LocalPlayer_BlockPos(player, under);
                     this.update_Input = carry != null
                        ? carry
                        : this.init_Input_LocalPlayer_2(player, under, new Input(false, false, false, false, false, false, false));
                  } else if (this.init_aI_ValueSink != ScaffoldModule.Inner_ValueSink.FORWARD_DWELL || !this.load_Z_LocalPlayer_BlockPos(player, under)) {
                     boolean nearEdge = this.read_Z_LocalPlayer_BlockPos(player, under);
                     this.value_Z = !nearEdge && this.init_D_LocalPlayer_BlockPos(player, under) > 4.5;
                     if (this.pop_Z) {
                        this.read_V_LocalPlayer_BlockPos(player, under);
                     } else {
                        this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
                        this.open_V(player, under);
                        this.write_Z = false;
                        InteractionHand hand = this.init_InteractionHand();
                        if (hand != null) {
                           this.f_V();
                           ItemStack stack = player.getItemInHand(hand);
                           if (this.init_Z_ItemStack_4(stack)) {
                              this.sync_BlockPos = under.immutable();
                              this.f_L_I = under.getY();
                              ScaffoldModule.Inner_DataPipe first = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, stack);
                              this.init_aI_DataPipe = first;
                              if (this.sync_Z_LocalPlayer_BlockPos(player, under)) {
                                 this.flush_V_LocalPlayer_BlockPos(player, under);
                              } else if (this.value_Z && this.put_Z && !this.start_Z()) {
                                 this.init_V_LocalPlayer_aI_DataPipe(player, first);
                              } else if (!nearEdge) {
                                 this.f_S_I = 0;
                                 if (this.init_Z_LocalPlayer_3(player, under, first)) {
                                    this.init_V_LocalPlayer_aI_DataPipe(player, first);
                                 }
                              } else if (first == null) {
                                 this.f_S_I++;
                                 if (this.f_S_I >= 40) {
                                    this.flush_V_LocalPlayer_BlockPos(player, under);
                                 } else {
                                    this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
                                 }
                              } else {
                                 if (this.start_Z()) {
                                    this.f_S_I++;
                                    if (this.f_S_I < 20) {
                                       this.write_V_LocalPlayer_BlockPos(player, under);
                                       return;
                                    }

                                    this.f_R_I = 0;
                                    this.f_S_I = 0;
                                 }

                                 this.f_S_I = 0;
                                 boolean launchCatchable = !this.set_Z && !this.put_Z
                                    ? this.flush_Z_LocalPlayer_BlockPos(player, under)
                                    : this.close_Z_LocalPlayer_BlockPos(player, under);
                                 if (init_Z_Z_2(launchCatchable)) {
                                    this.update_V_LocalPlayer_BlockPos(player, under);
                                 } else if (!this.value_Z()) {
                                    this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
                                 } else {
                                    this.init_V_LocalPlayer_aI_DataPipe(player, first);
                                 }
                              }
                           } else if (!this.write_Z) {
                              if (nearEdge) {
                                 this.write_V_LocalPlayer_BlockPos(player, under);
                              }
                           } else {
                              this.init_aI_KeyHolder = nearEdge ? ScaffoldModule.Inner_KeyHolder.HOLD : ScaffoldModule.Inner_KeyHolder.FORWARD;
                           }
                        } else if (!this.write_Z) {
                           if (nearEdge) {
                              this.write_V_LocalPlayer_BlockPos(player, under);
                           }
                        } else {
                           this.init_aI_KeyHolder = nearEdge ? ScaffoldModule.Inner_KeyHolder.HOLD : ScaffoldModule.Inner_KeyHolder.FORWARD;
                           if (this.write_Z) {
                              this.f_V();
                           }
                        }
                     }
                  }
               }
            }
         } else {
            if (under != null && this.f_H_I > 0) {
               this.update_V_BlockPos(under);
            }

            this.read_V_LocalPlayer_BlockPos(player, under);
         }
      } else {
         this.close_V_LocalPlayer_BlockPos(player, this.init_BlockPos_LocalPlayer(player));
      }
   }

   private boolean init_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      if (this.pop_Z) {
         this.task_Z = false;
         return false;
      }

      if (this.f_H_I > 0
         || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH
         || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING
         || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING) {
         this.update_V_BlockPos(under);
      }

      Vec3 velocity = player.getDeltaMovement();
      Vec3 forward = this.write_Vec3();
      Vec3 left = this.update_Vec3();
      Input recovery = this.init_Input_LocalPlayer_2(player, under, init_Input_AABB(player.getBoundingBox(), under, this.flags_F));
      double laneError = this.f_x_D - init_D_Vec3_F(player.position(), this.flags_F);
      if (Math.abs(laneError) > 0.55) {
         this.init_Vec3 = this.read_Vec3_BlockPos(under, player.position(), this.flags_F);
         this.f_x_D = init_D_Vec3_F(this.init_Vec3, this.flags_F);
         laneError = this.f_x_D - init_D_Vec3_F(player.position(), this.flags_F);
      }

      boolean settled = !load_Z_Input(recovery)
         && this.write_ce_DataUtil != null
         && init_Z_F(
            this.write_ce_DataUtil.init(),
            this.write_F(),
            velocity.x * forward.x + velocity.z * forward.z,
            velocity.x * left.x + velocity.z * left.z,
            laneError,
            player.onGround()
         );
      if (settled && this.f_ab_I <= 0) {
         this.task_Z = false;
         this.data_V();
         this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.FORWARD_DWELL;
         this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
         this.f_M_I = 0;
         this.f_K_I = 0;
         return true;
      }

      if (settled) {
         this.f_ab_I--;
      }

      this.scan_V();
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
      Direction overhang = this.init_Direction_LocalPlayer(player);
      this.update_Input = recovery;
      if (!init_Z_BlockPos_4(this.poll_BlockPos, this.push_BlockPos, overhang)) {
         this.data_V();
      }

      if (overhang != null && init_Z_BlockPos_4(this.poll_BlockPos, this.push_BlockPos, overhang) && this.sync_Z(player)) {
         this.f_aa_I++;
         if (this.f_aa_I >= 30) {
            this.data_V();
            this.task_Z = false;
            this.update_V_LocalPlayer_BlockPos(player, under);
         }

         return true;
      } else {
         this.f_aa_I++;
         if (this.f_aa_I >= 30) {
            this.task_Z = false;
            this.update_V_LocalPlayer_BlockPos(player, under);
         }

         return true;
      }
   }

   private void read_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      this.write_V_LocalPlayer_BlockPos(player, under);
      boolean atEdge = under == null || this.read_Z_LocalPlayer_BlockPos(player, under);
      Direction overhang = this.init_Direction_LocalPlayer(player);
      if (atEdge || overhang != null) {
         this.f_S_I++;
         if (overhang != null && init_Z_BlockPos_4(this.poll_BlockPos, this.push_BlockPos, overhang) && this.sync_Z(player)) {
            return;
         }

         if (atEdge && this.flush_Z(player)) {
            return;
         }
      }

      if (!this.tick_Z && this.load_Z_LocalPlayer(player)) {
         this.chunk_V();
      }
   }

   private void write_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      this.scan_V();
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
      this.run_Z = false;
      this.update_Input = support == null
         ? null
         : this.init_Input_LocalPlayer_2(player, support, init_Input_AABB(player.getBoundingBox(), support, this.flags_F));
   }

   static double init_D_D_D(double velocity, double friction) {
      return Double.isFinite(velocity) && Double.isFinite(friction) ? velocity / (1.0 - Mth.clamp(friction, 0.0, 0.99)) : 0.0;
   }

   static double load_D_D_D(double movementSpeed, double blockFriction) {
      double f = Mth.clamp(blockFriction, 0.1, 1.0);
      double scale = f > 0.6 ? 0.21600002 / (f * f * f) : 1.0;
      return Math.max(0.0, movementSpeed) * 0.98 * scale;
   }

   static double init_D_D(double velocity, double friction, double brakeAcceleration) {
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

   static Input init_Input_Input_3(Input recovery, ScaffoldModule.Inner_MethodUtil along, ScaffoldModule.Inner_MethodUtil across) {
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

   private double init_D_BlockPos_2(BlockPos support) {
      return support != null && MC.level != null ? MC.level.getBlockState(support).getBlock().getFriction() : 0.6;
   }

   private double load_D_BlockPos(BlockPos support) {
      return Mth.clamp(this.init_D_BlockPos_2(support) * 0.91, 0.0, 0.99);
   }

   private Input init_Input_LocalPlayer_2(LocalPlayer player, BlockPos support, Input recovery) {
      if (player != null
         && support != null
         && MC.level != null
         && player.onGround()
         && Float.isFinite(this.flags_F)
         && init_Z_AABB_2(player.getBoundingBox(), support, 1.0E-9)) {
         double friction = this.load_D_BlockPos(support);
         Vec3 velocity = player.getDeltaMovement();
         Vec3 forward = this.write_Vec3();
         Vec3 left = this.update_Vec3();
         AABB box = player.getBoundingBox();
         Vec3 toSupport = Vec3.atCenterOf(support).subtract(box.getCenter());
         ScaffoldModule.Inner_MethodUtil along = this.init_aI_MethodUtil_LocalPlayer(
            player, box, forward, friction, velocity.x * forward.x + velocity.z * forward.z, toSupport.x * forward.x + toSupport.z * forward.z
         );
         ScaffoldModule.Inner_MethodUtil across = this.init_aI_MethodUtil_LocalPlayer(
            player, box, left, friction, velocity.x * left.x + velocity.z * left.z, toSupport.x * left.x + toSupport.z * left.z
         );
         return init_Input_Input_3(recovery, along, across);
      } else {
         return recovery;
      }
   }

   private Input init_Input_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (!this.count_Z()
         && support != null
         && this.load_ce_DataUtil != null
         && !this.put_Z
         && !this.set_Z
         && !this.clear_Z
         && !this.start_Z
         && this.flush_I() <= 1
         && !this.pop_Z
         && !this.init_aI_b.load()
         && !this.start_Z()
         && !this.task_Z) {
         Input forward = new Input(true, false, false, false, false, false, false);
         Input keys = init_Input_Input_2(forward, this.flags_F, this.load_ce_DataUtil.init());
         Vec3 local = new Vec3(init_F_Z_Z(keys.left(), keys.right()), 0.0, init_F_Z_Z(keys.forward(), keys.backward())).normalize();
         double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (player.isSprinting() ? 1.3 : 1.0);
         double acceleration = load_D_D_D(speed, this.init_D_BlockPos_2(support));
         float yaw = this.load_ce_DataUtil.init() * 0.017453292F;
         Vec3 force = new Vec3(local.x * Mth.cos(yaw) - local.z * Mth.sin(yaw), 0.0, local.z * Mth.cos(yaw) + local.x * Mth.sin(yaw)).scale(acceleration);
         Vec3 velocity = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
         Vec3 step = velocity.add(force);
         if (!(force.dot(this.write_Vec3()) <= 0.0) && !(this.init_Vec3_LocalPlayer(player, player.getBoundingBox(), step).distanceToSqr(step) > 1.0E-8)) {
            boolean safe = init_Z_AABB(
               player.getBoundingBox(), velocity, force, this.load_D_BlockPos(support), acceleration * 0.6, box -> this.init_Z_AABB(box, player.getY(), 0.12)
            );
            return safe ? forward : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean init_Z_AABB(AABB body, Vec3 velocity, Vec3 acceleration, double drag, double brake, Predicate<AABB> floor) {
      Vec3 step = velocity.add(acceleration);
      Vec3 next = step.scale(Mth.clamp(drag, 0.0, 0.99));
      Vec3 reach = step.add(init_D_D(next.x, drag, brake), 0.0, init_D_D(next.z, drag, brake));
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

   private ScaffoldModule.Inner_MethodUtil init_aI_MethodUtil_LocalPlayer(LocalPlayer player, AABB box, Vec3 axis, double friction, double velocity, double toSupport) {
      double coast = init_D_D_D(velocity, friction);
      boolean leaves = !this.init_Z_AABB(box.move(axis.scale(coast)), player.getY(), 0.12);
      return new ScaffoldModule.Inner_MethodUtil(velocity, coast, toSupport, leaves);
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player) {
      if (player != null && MC.level != null && player.onGround() && Float.isFinite(this.flags_F)) {
         BlockPos support = this.init_BlockPos_LocalPlayer(player);
         if (support == null) {
            return true;
         }

         double friction = this.load_D_BlockPos(support);
         Vec3 velocity = player.getDeltaMovement();
         Vec3 forward = this.write_Vec3();
         Vec3 left = this.update_Vec3();
         double alongVelocity = velocity.x * forward.x + velocity.z * forward.z;
         double acrossVelocity = velocity.x * left.x + velocity.z * left.z;
         double brake = init_D_D(Math.min(0.1, player.getSpeed()), this.init_D_BlockPos_2(support), alongVelocity, acrossVelocity);
         Vec3 reach = forward.scale(init_D_D(alongVelocity, friction, brake)).add(left.scale(init_D_D(acrossVelocity, friction, brake)));
         AABB box = player.getBoundingBox();
         return reach.horizontalDistanceSqr() < 1.0E-6 ? true : this.init_Z_AABB(box.move(reach), player.getY(), 0.05);
      } else {
         return true;
      }
   }

   static double init_D_D(double movementSpeed, double blockFriction, double alongVelocity, double acrossVelocity) {
      boolean bothAxes = Math.abs(alongVelocity) >= 0.03 && Math.abs(acrossVelocity) >= 0.03;
      return load_D_D_D(movementSpeed, blockFriction) * 0.92 * (bothAxes ? Mth.SQRT_OF_TWO / 2.0 : 1.0);
   }

   private boolean load_Z_LocalPlayer(LocalPlayer player) {
      if (player.onGround() && !this.stop_Z && !this.poll_Z_3() && !(player.getDeltaMovement().horizontalDistance() >= 0.06)) {
         BlockPos support = this.init_BlockPos_LocalPlayer(player);
         if (support == null) {
            return false;
         }

         double drag = Mth.clamp(Math.max(0.91, MC.level.getBlockState(support).getBlock().getFriction() * 0.91), 0.0, 0.99);
         Vec3 coast = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
         AABB box = player.getBoundingBox();
         if (!this.init_Z_AABB(box, player.getY(), 0.12)) {
            return false;
         }

         Vec3 remaining = coast.scale(1.0 / (1.0 - drag));
         int samples = Math.max(1, (int)Math.ceil(remaining.horizontalDistance() / 0.05));

         for (int sample = 0; sample < samples; sample++) {
            Vec3 step = this.init_Vec3_LocalPlayer(player, box, remaining.scale(1.0 / samples));
            box = box.move(step);
            if (!this.init_Z_AABB(box, player.getY(), 0.12)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean load_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      this.scan_V();
      boolean nearEdge = this.read_Z_LocalPlayer_BlockPos(player, under);
      if (this.pop_Z) {
         this.read_V_LocalPlayer_BlockPos(player, under);
         return true;
      } else {
         this.f_K_I = init_I_I_Z(this.f_K_I, true);
         if (!write_Z_I(this.f_K_I)) {
            this.init_aI_KeyHolder = nearEdge ? ScaffoldModule.Inner_KeyHolder.HOLD : ScaffoldModule.Inner_KeyHolder.FORWARD;
            return true;
         } else {
            this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RUNNING;
            this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
            this.f_K_I = 0;
            return false;
         }
      }
   }

   private void update_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (this.init_aI_ValueSink != ScaffoldModule.Inner_ValueSink.RECOVERING && player != null && support != null) {
         this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RECOVERING;
         this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
         this.f_M_I = 0;
         this.init_aI_DataPipe = null;
         this.stop_Z = false;
         this.open_BlockPos = null;
         this.sync_BlockPos = support.immutable();
         this.scan_V();
         this.mark_V();
      }
   }

   private void sync_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player == null || support == null) {
         this.chunk_V();
      } else if (this.pop_Z) {
         this.read_V_LocalPlayer_BlockPos(player, support);
      } else {
         this.load_V_LocalPlayer_BlockPos(player, support);
         if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, support)) {
            if (this.sync_Z_LocalPlayer_BlockPos(player, support)) {
               this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RUNNING;
               this.f_M_I = 0;
               this.flush_V_LocalPlayer_BlockPos(player, support);
            } else {
               this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
               this.init_Vec3 = this.read_Vec3_BlockPos(support, player.position(), this.flags_F);
               this.f_x_D = init_D_Vec3_F(this.init_Vec3, this.flags_F);
               this.update_Input = this.init_Input_LocalPlayer_2(player, support, init_Input_AABB(player.getBoundingBox(), support, this.flags_F));
               this.f_M_I++;
               if (this.f_M_I >= 60) {
                  this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RUNNING;
                  this.flush_V_LocalPlayer_BlockPos(player, support);
               } else if (this.flush_Z_LocalPlayer_BlockPos(player, support)) {
                  this.update_Input = null;
                  this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.FORWARD_DWELL;
                  this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
                  this.init_Vec3 = this.read_Vec3_BlockPos(support, player.position(), this.flags_F);
                  this.f_x_D = init_D_Vec3_F(this.init_Vec3, this.flags_F);
                  this.sync_BlockPos = support.immutable();
                  this.f_L_I = support.getY();
                  this.init_aI_DataPipe = null;
                  this.f_M_I = 0;
                  this.f_K_I = 0;
               }
            }
         }
      }
   }

   private void flush_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      this.init_V_LocalPlayer(player, support, false);
   }

   static boolean write_Z_F_F(float course, float yaw) {
      return Float.isFinite(course) && Float.isFinite(yaw) && Math.abs(ce.init(course + 180.0F, yaw)) <= 1.0F;
   }

   private void close_V_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (!AutismLiteVariant.enabled()) {
         this.cache_Z = false;
         this.state_Z = false;
         this.value_Z = false;
         this.run_Z = false;
         this.set_Z = false;
         this.clear_Z = false;
         this.flags_V();
         if (this.f_H_I > 0 && support != null) {
            this.update_V_BlockPos(support);
         }

         this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RUNNING;
         this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.PLACING;
         if (support == null) {
            support = this.sync_BlockPos;
         }

         if (support == null) {
            this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
         } else {
            float previousCourse = this.flags_F;
            this.load_V_LocalPlayer_BlockPos(player, support);
            if (previousCourse != this.flags_F) {
               this.read_BlockPos = null;
               this.init_aI_CacheEntry = null;
               this.close_Z = false;
            }

            if (this.pop_Z || this.add_Z) {
               this.read_V_LocalPlayer_BlockPos(player, support);
            } else if (!this.task_Z || !this.init_Z_LocalPlayer_BlockPos(player, support)) {
               this.sync_BlockPos = support.immutable();
               this.f_L_I = support.getY();
               if (this.read_BlockPos != null
                  && this.load_Z_BlockState_BlockPos(MC.level.getBlockState(this.read_BlockPos), this.read_BlockPos)
                  && init_Z_BlockPos_2(this.read_BlockPos, this.f_aN_I, this.flush_ArrayDeque)) {
                  this.read_BlockPos = null;
               }

               if (!this.stop_Z) {
                  this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, this.init_ItemStack_2());
               }

               if (this.read_BlockPos == null && this.init_aI_DataPipe != null) {
                  ScaffoldModule.Inner_TableIndex next = this.init_aI_DataPipe.init_aI_TableIndex();
                  Vec3 plane = Vec3.atCenterOf(next.init_BlockPos()).add(this.write_Vec3().scale(0.5));
                  if (player.getEyePosition().subtract(plane).dot(this.write_Vec3()) > 1.0E-9) {
                     this.read_BlockPos = next.load().immutable();
                  }
               }

               this.flush_Z(player);
               this.open_V(player, support);
               Vec3 forward = this.write_Vec3();
               double friction = this.load_D_BlockPos(support);
               double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
               if (player.isSprinting()) {
                  speed /= 1.3;
               }

               Vec3 step = player.getDeltaMovement().multiply(1.0, 0.0, 1.0).add(forward.scale(speed * 0.98));
               double brake = init_D_D(speed, this.init_D_BlockPos_2(support), step.dot(forward), 0.0);
               Vec3 stop = step.add(forward.scale(init_D_D(step.dot(forward) * friction, friction, brake)));
               boolean safe = this.read_BlockPos == null
                  && this.data_Z()
                  && this.init_Z_ItemStack_4(this.init_ItemStack_2())
                  && this.init_Z_AABB(player.getBoundingBox().move(stop), player.getY(), 0.08);
               this.init_aI_KeyHolder = safe ? ScaffoldModule.Inner_KeyHolder.FORWARD : ScaffoldModule.Inner_KeyHolder.HOLD;
               if (!safe) {
                  this.scan_V();
                  this.update_Input = this.init_Input_LocalPlayer(player, support, speed);
               }
            }
         }
      }
   }

   private Input init_Input_LocalPlayer(LocalPlayer player, BlockPos support, double speed) {
      Input straight = this.init_Input_LocalPlayer_2(player, support, new Input(false, false, false, false, false, false, false));
      if (AutismLiteVariant.enabled()) {
         return straight;
      }

      if (this.read_BlockPos != null && this.data_Z() && !straight.left() && !straight.right()) {
         Vec3 forward = this.write_Vec3();
         Vec3 left = this.update_Vec3();
         Vec3 plane = Vec3.atCenterOf(this.read_BlockPos).subtract(forward.scale(0.5));
         boolean approach = !load_Z_Input(straight)
            && player.getDeltaMovement().horizontalDistanceSqr() < 2.25E-4
            && player.position().subtract(plane).dot(forward) < 0.04;
         if (!approach && straight.forward() == straight.backward()) {
            return straight;
         }

         double acceleration = load_D_D_D(speed, this.init_D_BlockPos_2(support));
         double friction = this.load_D_BlockPos(support);
         Vec3 along = forward.scale(!approach && !straight.forward() ? -1.0 : 1.0);
         Vec3 normalStop = init_Vec3_Vec3_4(player.position(), player.getDeltaMovement(), along.scale(acceleration), friction);
         if (!approach && normalStop.subtract(plane).dot(forward) > 0.025) {
            return straight;
         }

         Input best = straight;
         double cost = 1.0 / 0.0;
         double faceLimit = (Math.abs(forward.x) > 0.5 ? player.getBoundingBox().getXsize() : player.getBoundingBox().getZsize()) * 0.5 - 0.08;

         for (int side = -1; side <= 1; side++) {
            Vec3 impulse = along.add(left.scale(side)).normalize().scale(acceleration);
            Vec3 stop = init_Vec3_Vec3_4(player.position(), player.getDeltaMovement(), impulse, friction);
            double exposed = stop.subtract(plane).dot(forward);
            double error = Math.abs(this.f_x_D - init_D_Vec3_F(stop, this.flags_F));
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

   static Vec3 init_Vec3_Vec3_4(Vec3 position, Vec3 velocity, Vec3 impulse, double friction) {
      return position.add(velocity.add(impulse).multiply(1.0, 0.0, 1.0).scale(1.0 / (1.0 - Mth.clamp(friction, 0.0, 0.99))));
   }

   private boolean init_Z_LocalPlayer_6(LocalPlayer player, Vec3 impulse, double friction) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      AABB body = player.getBoundingBox();
      Vec3 velocity = player.getDeltaMovement().add(impulse).multiply(1.0, 0.0, 1.0);

      for (int tick = 0; tick < 14; tick++) {
         Vec3 step = this.init_Vec3_LocalPlayer(player, body, velocity);
         if (step.subtract(velocity).horizontalDistanceSqr() > 1.0E-8) {
            return false;
         }

         body = body.move(step);
         if (!this.init_Z_AABB(body, player.getY(), 0.08)) {
            return false;
         }

         velocity = velocity.scale(friction);
      }

      return true;
   }

   private void init_V_LocalPlayer(LocalPlayer player, BlockPos support, boolean diagonal) {
      if (this.count_Z() && !diagonal) {
         this.close_V_LocalPlayer_BlockPos(player, support);
      } else {
         this.cache_Z = true;
         this.f_ay_I = -2147483648;
         this.f_az_I = -2147483648;
         this.state_Z = diagonal;
         this.push_Z = false;
         this.value_Z = false;
         this.run_Z = false;
         this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.RELEASED;
         this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RUNNING;
         this.scan_V();
         this.mark_V();
         this.data_V();
         this.update_Input = null;
         this.init_aI_DataPipe = null;
         this.open_BlockPos = null;
         this.stop_Z = false;
         if (support != null) {
            this.sync_BlockPos = support.immutable();
            this.f_L_I = support.getY();
         }

         this.flush_ce_DataUtil = this.load_ce_DataUtil != null ? this.load_ce_DataUtil : this.poll_ce_DataUtil();
         this.load_ce_DataUtil = this.flush_ce_DataUtil;
         this.init_dg.init_V_J(this.init_Random.nextLong());
         this.read_ce_DataUtil = null;
         this.pool_F = 0.0F;
         autismclient.util.ap.init_V_ap_TaskQueue(this.init_ap_TaskQueue);
         this.sync_Z = false;
         this.f_bG_I = 0;
         this.f_v_I = 5;
         int tick = ck.init_ck().state_I();
         this.f_y_I = this.f_T_I == tick ? tick : -2147483648;
         this.f_aV_I = tick;
         this.f_aQ_I = 0;
         this.fill_F = 0.0F / 0.0F;
         this.state_V();
         this.fill_V();
         this.load_aI_TableIndex = null;
         this.load_aI_HashUtil = null;
         this.close_V_4();
         this.b_V();
         Input intent = init_Input_Input_5(
            new Input(
               init_Z_KeyMapping(MC.options.keyUp),
               init_Z_KeyMapping(MC.options.keyDown),
               init_Z_KeyMapping(MC.options.keyLeft),
               init_Z_KeyMapping(MC.options.keyRight),
               init_Z_KeyMapping(MC.options.keyJump),
               init_Z_KeyMapping(MC.options.keyShift),
               init_Z_KeyMapping(MC.options.keySprint)
            ),
            this.state_Z,
            !this.count_Z() || this.bool("eagle-jump")
         );
         this.init_V_Input(intent);
         this.init_aI_HashUtil = load_Z_Input(intent) ? this.init_aI_HashUtil_Input(intent) : null;
      }
   }

   private void cache_V() {
      if (!this.bool("allow-diagonal")) {
         this.state_Z = false;
      }

      if (this.stop_Z()) {
         this.state_Z = true;
      } else if (this.cache_Z() && MC.player.onGround()) {
         LocalPlayer player = MC.player;
         BlockPos support = this.init_BlockPos_LocalPlayer(player);
         if (support != null
            && init_Z_Z_4(
               player.onGround(), player.isCrouching(), init_Z_KeyMapping(MC.options.keyShift), player.input == null ? null : player.input.keyPresses
            )) {
            this.init_V_LocalPlayer(player);
            this.load_V_LocalPlayer_BlockPos(player, support);
            if ((
                  init_Z_KeyMapping(MC.options.keyUp)
                     || init_Z_KeyMapping(MC.options.keyDown)
                     || init_Z_KeyMapping(MC.options.keyLeft)
                     || init_Z_KeyMapping(MC.options.keyRight)
               )
               && this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.IDLE
               && !this.g_Z()
               && !this.sync_Z_LocalPlayer_BlockPos(player, support)
               && this.flush_Z_LocalPlayer_BlockPos(player, support)
               && this.init_Z_LocalPlayer(player)) {
               ce.Inner_DataUtil live = this.load_ce_DataUtil != null ? this.load_ce_DataUtil : this.poll_ce_DataUtil();
               this.block_V();
               autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, live);
               this.write_ce_DataUtil = live;
               this.load_ce_DataUtil = live;
               this.load_aI_TableIndex = null;
               this.flush_Z = false;
               this.load_aI_HashUtil = null;
               this.init_aI_HashUtil = null;
               this.fill_V();
               this.f_aQ_I = 0;
               this.fill_F = 0.0F / 0.0F;
               this.state_V();
            }
         }
      }
   }

   static boolean init_Z_Z_4(boolean grounded, boolean crouched, boolean physicalSneak, Input sampled) {
      return grounded && !crouched && !physicalSneak && sampled != null && !sampled.shift();
   }

   private boolean stop_Z() {
      return MC.player != null
         && init_Z_Str(
            this.choice("mode"),
            this.bool("allow-diagonal"),
            MC.player.getYRot(),
            new Input(
               init_Z_KeyMapping(MC.options.keyUp),
               init_Z_KeyMapping(MC.options.keyDown),
               init_Z_KeyMapping(MC.options.keyLeft),
               init_Z_KeyMapping(MC.options.keyRight),
               false,
               false,
               false
            )
         );
   }

   static boolean init_Z_Str(String mode, boolean allowed, float cameraYaw, Input physical) {
      return ("Telly".equals(mode) || "Eagle".equals(mode)) && allowed && load_Z_Input(physical) && init_Z_I_6(init_I_F(cameraYaw + init_F_Input(physical)));
   }

   private void init_V_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.Inner_DataPipe first) {
      int jumpDelay = ((AutismLivingEntityAccessor)player).autism$getNoJumpDelay();
      if (!init_Z_Z_I(player.onGround(), jumpDelay)) {
         this.write_V_LocalPlayer_BlockPos(player, this.init_BlockPos_LocalPlayer(player));
      } else if (this.sync_Z_LocalPlayer_BlockPos(player, this.sync_BlockPos)) {
         this.flush_V_LocalPlayer_BlockPos(player, this.sync_BlockPos);
      } else {
         BlockPos footing = this.init_BlockPos_LocalPlayer(player);
         if (footing != null
            && this.data_Z()
            && (this.set_Z || this.put_Z || this.flush_Z_LocalPlayer_BlockPos(player, footing) && this.load_Z_LocalPlayer(player, footing, first))) {
            int batchPower = !this.count_Z() && this.put_Z
               ? this.init_I_LocalPlayer(player, footing, player.position(), player.getDeltaMovement(), player.getBoundingBox(), this.poll_Z(player))
               : -1;
            if (this.count_Z() || !this.put_Z || batchPower >= 0 || !this.poll(player, footing)) {
               boolean flatEntry = !this.count_Z()
                  && this.put_Z
                  && batchPower < 0
                  && !this.reset_Z
                  && this.flush_Z_LocalPlayer_BlockPos(player, footing)
                  && this.load_Z_LocalPlayer(player, footing, first);
               if ((this.set_Z || this.put_Z) && !flatEntry && !this.open_Z(player, footing)) {
                  if (this.count_Z()
                     || this.put_Z
                     || !this.init_Z_LocalPlayer(player, footing, player.position(), player.getDeltaMovement(), player.getBoundingBox(), false)) {
                     this.write_V_LocalPlayer_BlockPos(player, footing);
                     this.update_Input = this.init_Input_LocalPlayer_2(player, footing, init_Input_AABB(player.getBoundingBox(), footing, this.flags_F, 0.0));
                     return;
                  }

                  this.f_Z_I = ck.init_ck().state_I();
               }

               if (!this.init_Z_LocalPlayer_2(player, first, flatEntry)) {
                  this.write_V_LocalPlayer_BlockPos(player, this.init_BlockPos_LocalPlayer(player));
               } else {
                  this.data_V();
                  this.init_aI_NodeWalker = this.count_Z() ? ScaffoldModule.Inner_NodeWalker.PLACING : ScaffoldModule.Inner_NodeWalker.FORWARD;
                  this.init_aI_CacheEntry = null;
                  this.close_Z = false;
                  this.pop_J++;
                  this.clear_Z = (this.set_Z || this.put_Z) && !flatEntry;
                  this.write_BlockPos = this.clear_Z && !this.count_Z() && this.put_Z ? footing.relative(this.init_Direction(), 2).immutable() : null;
                  this.f_E_I = Math.max(0, batchPower);
                  if (this.put_Z) {
                     this.reset_Z = true;
                  }

                  this.init_aM().init(this.init_aM_DataUtil(), !this.count_Z());
                  this.init_aM_DataUtil = null;
                  this.update_Z = false;
                  this.open_Z = this.init_aN.init(!this.count_Z() && !flatEntry ? this.integer("imperfection") : 0, this.clear_Z, this.init_Random);
                  this.f_A_I = this.init_aN.init_I();
                  this.poll_Z = false;
                  this.load_BlockPos = null;
                  this.load_List = init_List_BlockPos_2(footing, this.init_Direction(), this.init_aN.init_I());
                  this.set_Z = false;
                  this.add_Z = false;
                  this.cache_Z = false;
                  this.state_Z = false;
                  this.queue_Z = false;
                  this.start_Z = false;
                  this.close_BlockPos = null;
                  this.f_I_I = 0;
                  this.next_Z = false;
                  this.f_F_I = 0;
                  this.f_v_D = player.getY();
                  this.f_w_D = player.position().dot(this.write_Vec3());
                  this.update_BlockPos = null;
                  this.f_J_I = 0;
                  this.f_H_I = 0;
                  this.buffer_Z = this.clear_Z && (this.write_BlockPos == null || this.f_E_I == 0) || this.init_aN.init_I() > 1;
                  this.f_S_I = 0;
                  this.init_aI_DataPipe = first;
                  this.data_Z = true;
                  this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.LAUNCH;
                  this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
                  this.scan_V();
                  this.mark_V();
                  this.state_V();
                  this.run_Z = true;
               }
            }
         } else if (footing != null) {
            this.update_V_LocalPlayer_BlockPos(player, footing);
         } else {
            this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
         }
      }
   }

   static boolean init_Z_Z_I(boolean grounded, int jumpDelay) {
      return grounded && jumpDelay <= 1;
   }

   private void state_V() {
      this.f_aR_I = 0;
      this.f_aS_I = 0;
      this.f_a_Z = false;
   }

   private void read_V_LocalPlayer(LocalPlayer player) {
      this.cache_Z = false;
      this.state_Z = false;
      this.f_H_I++;
      if (this.sync_BlockPos == null) {
         this.chunk_V();
      } else {
         if (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RUNNING || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.FORWARD_DWELL) {
            if (!this.next_Z() && this.init_Z_LocalPlayer_I(player, this.f_L_I)) {
               this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
               return;
            }

            this.sync_V(player);
         }

         this.init_aI_KeyHolder = !this.pop_Z && !this.add_Z && !this.buffer_Z ? ScaffoldModule.Inner_KeyHolder.FORWARD : ScaffoldModule.Inner_KeyHolder.HOLD;
         ItemStack stack = this.init_ItemStack_2();
         if (player.horizontalCollision
            || player.verticalCollision && player.getDeltaMovement().y <= 0.0
            || this.init_aI_FieldCache_LocalPlayer_D(player, this.f_L_I + (this.start_Z ? 2.0 : 1.0)).init_Z()) {
            this.queue_Z = true;
            if (!this.start_Z) {
               this.clear_Z = false;
               if (!this.stop_Z && this.init_aI_DataPipe != null && this.init_aI_DataPipe.init_Z()) {
                  this.init_aI_DataPipe = null;
               }
            }
         }

         if (this.start_Z && this.close_BlockPos != null && !this.load_Z_BlockState_BlockPos(MC.level.getBlockState(this.close_BlockPos), this.close_BlockPos)) {
            this.start_Z = false;
            if (this.sync_BlockPos != null && this.sync_BlockPos.getY() > this.f_L_I) {
               this.sync_BlockPos = new BlockPos(this.sync_BlockPos.getX(), this.f_L_I, this.sync_BlockPos.getZ());
            }

            this.close_BlockPos = null;
         }

         if (this.start_Z
            && this.close_BlockPos != null
            && !this.stop_Z
            && player.getDeltaMovement().y < -0.035
            && !this.init_Z_LocalPlayer_I(player, this.f_L_I + 1)
            && !init_Z_Vec3_7(player.position(), player.getDeltaMovement(), player.getBoundingBox(), this.close_BlockPos, this.init_Vec3_LocalPlayer_2(player))
            )
          {
            BlockPos lowerSupport = this.close_BlockPos.below();
            this.start_Z = false;
            this.close_BlockPos = null;
            this.clear_Z = false;
            this.init_aI_DataPipe = null;
            this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.AIMING;
            if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(lowerSupport), lowerSupport)) {
               this.sync_BlockPos = lowerSupport;
            }
         }

         if (this.next_Z()) {
            this.write_V_LocalPlayer(player);
         } else {
            int landingBlockY = this.start_Z ? this.f_L_I + 1 : this.f_L_I;
            boolean descending = player.getDeltaMovement().y < -0.035;
            boolean landingSecured = this.init_Z_LocalPlayer_I(player, landingBlockY);
            if (!landingSecured && !this.node_Z && this.write_Z_LocalPlayer_I(player, landingBlockY)) {
               this.buffer_Z = true;
               this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
               landingSecured = this.init_Z_LocalPlayer_I(player, landingBlockY);
            }

            if (!landingSecured) {
               this.init_V_LocalPlayer_D(player, landingBlockY + 1.0);
            } else {
               this.f_N_I = Math.max(0, this.f_N_I - 1);
            }

            if (!landingSecured && this.start_Z && this.close_Z(player)) {
               this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
            }

            boolean turnPending = this.start_Z();
            boolean reservePending = turnPending || this.init_aI_b.load();
            boolean turnReserveCovered = !reservePending || landingSecured && this.init_Z_LocalPlayer(player, landingBlockY, init_I_Z_2(turnPending));
            boolean chainCovered = load_Z_Z(landingSecured, reservePending, turnReserveCovered);
            if (chainCovered && this.init_aI_ValueSink != ScaffoldModule.Inner_ValueSink.RETURNING) {
               chainCovered = this.load_Z_LocalPlayer_I(player, landingBlockY);
            }

            boolean walkOffGrace = this.next_Z && this.f_F_I > 0;
            if (this.f_F_I > 0) {
               this.f_F_I--;
            }

            if (!landingSecured
               && !walkOffGrace
               && !this.stop_Z
               && this.f_G_I <= 0
               && (this.update_Z_LocalPlayer(player) || this.load_Z_LocalPlayer_Z(player, descending))) {
               this.init_Z_LocalPlayer_ItemStack(player, stack);
            }

            if (!landingSecured && this.f_J_I >= 3) {
               this.flush_V(player);
            } else if (!landingSecured && !walkOffGrace && this.init_Z_LocalPlayer_Z(player, descending)) {
               if (!this.stop_Z && this.f_G_I <= 0 && !this.init_Z_LocalPlayer_ItemStack(player, stack)) {
                  this.flush_V(player);
               }
            } else if (!this.init_Z_ItemStack_4(stack)) {
               this.flush_V(player);
            } else {
               ScaffoldModule.Inner_ValueSink coveredPhase = init_aI_ValueSink_aI_ValueSink(
                  this.init_aI_ValueSink, chainCovered, this.clear_Z && !this.start_Z || this.tick(player)
               );
               if (coveredPhase == ScaffoldModule.Inner_ValueSink.RETURNING && this.init_aI_ValueSink != ScaffoldModule.Inner_ValueSink.RETURNING) {
                  this.init_aI_DataPipe = null;
                  this.init_aI_ValueSink = coveredPhase;
               } else {
                  if (landingSecured && this.clear_Z && !this.start_Z && player.getDeltaMovement().y < 0.0 && player.getY() < this.f_L_I + 2.0) {
                     this.clear_Z = false;
                     this.init_aI_DataPipe = null;
                     if (chainCovered) {
                        this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RETURNING;
                        return;
                     }
                  }

                  if (this.init_aI_ValueSink != ScaffoldModule.Inner_ValueSink.RETURNING) {
                     if (this.init_aI_DataPipe == null) {
                        if (chainCovered) {
                           if ((!this.clear_Z || this.start_Z) && !this.tick(player)) {
                              this.init_aI_ValueSink = init_aI_ValueSink_aI_ValueSink(this.init_aI_ValueSink, true, false);
                           }

                           return;
                        }

                        if (this.open_Z(player) && this.load_BlockPos_LocalPlayer(player) != null) {
                           return;
                        }

                        this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, stack);
                     }

                     if (this.init_aI_DataPipe != null) {
                        this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.AIMING;
                        this.write_Z = false;
                        InteractionHand hand = this.init_InteractionHand();
                        if (hand == null) {
                           if (this.write_Z) {
                              this.f_V();
                           }
                        } else {
                           this.f_V();
                           ItemStack held = player.getItemInHand(hand);
                           if (this.init_Z_ItemStack_4(held)) {
                              landingBlockY = this.start_Z ? this.f_L_I + 1 : this.f_L_I;
                              landingSecured = this.init_Z_LocalPlayer_I(player, landingBlockY);
                              turnPending = this.start_Z();
                              reservePending = turnPending || this.init_aI_b.load();
                              turnReserveCovered = !reservePending || landingSecured && this.init_Z_LocalPlayer(player, landingBlockY, init_I_Z_2(turnPending));
                              chainCovered = load_Z_Z(landingSecured, reservePending, turnReserveCovered);
                              if (chainCovered && this.init_aI_ValueSink != ScaffoldModule.Inner_ValueSink.RETURNING) {
                                 chainCovered = this.load_Z_LocalPlayer_I(player, landingBlockY);
                              }

                              if (chainCovered && (!this.clear_Z || this.start_Z) && !this.tick(player)) {
                                 this.init_aI_ValueSink = init_aI_ValueSink_aI_ValueSink(this.init_aI_ValueSink, true, false);
                                 this.init_aI_DataPipe = null;
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
      this.init_aI_KeyHolder = !this.pop_Z && !this.queue_Z && !this.add_Z ? ScaffoldModule.Inner_KeyHolder.FORWARD : ScaffoldModule.Inner_KeyHolder.HOLD;
      if (!this.start_Z) {
         this.clear_Z = false;
         if (!this.stop_Z && this.init_aI_DataPipe != null && this.init_aI_DataPipe.init_Z()) {
            this.init_aI_DataPipe = null;
         }
      }

      int blockY = this.init_I_LocalPlayer(player);
      if (this.init_Z_LocalPlayer_I(player, blockY)) {
         this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RETURNING;
         if (!this.stop_Z) {
            this.init_aI_DataPipe = null;
         }
      } else {
         this.init_V_LocalPlayer_D(player, blockY + 1.0);
         if (!this.stop_Z && this.f_G_I <= 0) {
            this.read_Z_LocalPlayer(player);
         }
      }
   }

   private int init_I_LocalPlayer(LocalPlayer player) {
      int intended = this.f_L_I + (this.start_Z ? 1 : 0);
      return Math.min(intended, Mth.floor(player.getY() + 1.0E-9) - 1);
   }

   private boolean read_Z_LocalPlayer(LocalPlayer player) {
      if (!this.stop_Z && this.f_G_I <= 0) {
         int blockY = this.init_I_LocalPlayer(player);
         if (this.init_Z_LocalPlayer_I(player, blockY)) {
            return false;
         }

         this.write_Z = false;
         InteractionHand hand = this.init_InteractionHand();
         if (hand == null) {
            if (this.write_Z) {
               this.f_V();
            }

            return this.write_Z;
         } else {
            this.f_V();
            ItemStack held = player.getItemInHand(hand);
            if (!this.init_Z_ItemStack_4(held)) {
               return false;
            }

            Vec3 landing = this.init_aI_FieldCache_LocalPlayer_D(player, blockY + 1.0).init_Vec3();
            AABB footprint = player.getBoundingBox().move(landing.x - player.getX(), blockY + 1.0 - player.getY(), landing.z - player.getZ());
            ScaffoldModule.Inner_DataPipe best = null;
            double bestCost = 1.0 / 0.0;

            for (int x = Mth.floor(footprint.minX + 1.0E-9); x <= Mth.floor(footprint.maxX - 1.0E-9); x++) {
               for (int z = Mth.floor(footprint.minZ + 1.0E-9); z <= Mth.floor(footprint.maxZ - 1.0E-9); z++) {
                  BlockPos cell = new BlockPos(x, blockY, z);
                  if (init_Z_AABB_BlockPos(footprint, cell)
                     && !MC.level.isOutsideBuildHeight(cell)
                     && this.init_Z_BlockState_3(MC.level.getBlockState(cell), cell, held)
                     && !init_Z_BlockPos_AABB_2(cell, player.getBoundingBox())) {
                     for (Direction face : Direction.values()) {
                        BlockPos support = cell.relative(face.getOpposite());
                        if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(support), support)) {
                           Vec3 aim = init_Vec3_AABB_Direction(init_AABB_BlockPos(support), face);
                           ScaffoldModule.Inner_DataPipe candidate = new ScaffoldModule.Inner_DataPipe(
                              new ScaffoldModule.Inner_TableIndex(
                                 support.immutable(),
                                 cell.immutable(),
                                 face,
                                 new BlockHitResult(aim, face, support, false),
                                 ce.init(aim, player.getEyePosition()),
                                 blockY
                              ),
                              false
                           );
                           ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(player, candidate);
                           if (live != null
                              && this.load_Z_BlockHitResult_aI_TableIndex(live.init_aI_TableIndex().init_BlockHitResult(), live.init_aI_TableIndex())
                              && !al.init_Z_HitResult(live.init_aI_TableIndex().init_BlockHitResult(), hand, this.id())) {
                              double cost = Vec3.atBottomCenterOf(cell.above()).distanceToSqr(landing)
                                 + init_D_ce_DataUtil_ce_DataUtil(this.poll_ce_DataUtil(), live.init_aI_TableIndex().init_ce_DataUtil()) * 0.001;
                              if (autismclient.util.U.init(MC.level.getBlockState(support), support, player.isShiftKeyDown())) {
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
               this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.AIMING;
               this.init_aI_DataPipe = best;
               return this.init_Z_aI_DataPipe(best, hand, held, null, true);
            }

            if (this.init_aI_DataPipe != null && this.init_aI_DataPipe.init_Z()) {
               return false;
            }

            this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, held);
            boolean pending = this.init_Z_LocalPlayer_ItemStack(player, held);
            if (this.stop_Z) {
               this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.AIMING;
            }

            return pending;
         }
      } else {
         return true;
      }
   }

   private boolean write_Z_LocalPlayer(LocalPlayer player) {
      if (!this.init_Z_ItemStack_4(this.init_ItemStack_2())) {
         return false;
      }

      if (this.flush_BlockPos == null) {
         return false;
      }

      int sinceGrounded = ck.init_ck().state_I() - this.f_ac_I;
      return sinceGrounded >= 0 && sinceGrounded <= 6 ? !this.init_Z_D(player.getX(), player.getY(), player.getZ(), 2) : false;
   }

   private void update_V_LocalPlayer(LocalPlayer player) {
      this.init_V_LocalPlayer_BlockPos(player, this.flush_BlockPos);
      this.sync_V(player);
   }

   private void sync_V(LocalPlayer player) {
      this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.FORWARD;
      this.init_aI_CacheEntry = null;
      this.close_Z = false;
      this.pop_J++;
      this.clear_Z = false;
      this.start_Z = false;
      this.close_BlockPos = null;
      this.f_I_I = 0;
      this.next_Z = true;
      this.f_F_I = 3;
      this.stop_Z = false;
      this.open_BlockPos = null;
      this.data_Z = true;
      this.f_J_I = 0;
      this.f_H_I = Math.max(this.f_H_I, 3);
      this.f_L_I = this.sync_BlockPos.getY();
      this.f_v_D = this.f_L_I + 1.0;
      this.f_w_D = player.position().dot(this.write_Vec3()) - 1.45;
      this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.LAUNCH;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
      this.scan_V();
      this.mark_V();
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
         } else if (scaffold.pool_Z() && scaffold.push_Z && scaffold.cache_Z()) {
            scaffold.count_V();
            if (!MC.player.onGround() || scaffold.count_Z()) {
               scaffold.entry_V();
            }
         }
      }
   }

   private static void queue_V() {
      float written = ch.init_F();
      if (Float.isFinite(written)) {
         f_l_F = written;
      }

      f_bS_I = -2147483648;
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_Packet(Packet<?> packet) {
      if (!(packet instanceof ServerboundUseItemOnPacket)) {
         return null;
      }

      synchronized (load_Map) {
         ScaffoldModule.Inner_RenderState queued = load_Map.get(new ScaffoldModule.Inner_StateCache(packet));
         return queued == null ? null : queued.init_ce_DataUtil();
      }
   }

   public static boolean init_Z_26() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()
         && scaffold.frame_Z()
         && scaffold.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.ARMED
         && scaffold.f_aJ_I == ck.init_ck().state_I();
   }

   public static boolean load_Z_11() {
      if (AutismLiteVariant.enabled() || !DangerModule.read_Z_22() && !ClutchModule.read_Z_20()) {
         if (init_Z_26() && !BlinkPacketManager.load_Z()) {
            Module module = al.init_Module_Str("scaffold");
            ScaffoldModule scaffold = (ScaffoldModule)module;
            return autismclient.util.as.init_Z_J(scaffold.put_J);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static void init_V_Packet(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket use) {
         synchronized (load_Map) {
            reset_V();
            if (load_Map.containsKey(new ScaffoldModule.Inner_StateCache(packet))) {
               return;
            }
         }

         long edgeGeneration = autismclient.util.as.init_J();
         if (edgeGeneration > 0L && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.frame_Z()) {
            ScaffoldModule.Inner_TableIndex pending = scaffold.read_aI_TableIndex;
            BlockHitResult hit = use.hitResult();
            if (pending != null
               && scaffold.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.ARMED
               && edgeGeneration == scaffold.put_J
               && use.hand() == scaffold.init_InteractionHand
               && init_Z_BlockHitResult(hit, pending, scaffold.chunk_Z)) {
               synchronized (load_Map) {
                  reset_V();
                  ScaffoldModule.Inner_StateCache lookup = new ScaffoldModule.Inner_StateCache(packet);
                  if (!load_Map.containsKey(lookup)) {
                     int ordinal = scaffold.f_aK_I++;
                     ScaffoldModule.Inner_RenderState queued = new ScaffoldModule.Inner_RenderState(
                        scaffold.put_J,
                        ordinal,
                        use.hand(),
                        pending.load().immutable(),
                        pending.init_BlockPos().immutable(),
                        pending.init_Direction(),
                        scaffold.update_ce_DataUtil
                     );
                     load_Map.put(new ScaffoldModule.Inner_StateCache(packet, init_ReferenceQueue), queued);
                     if (ordinal > 0) {
                        scaffold.table_Z = true;
                     }
                  }
               }
            }
         }
      }
   }

   public static void load_V_Packet(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket) {
         synchronized (load_Map) {
            reset_V();
            load_Map.remove(new ScaffoldModule.Inner_StateCache(packet));
         }
      }
   }

   private void buffer_V() {
      if (this.f_aJ_I == ck.init_ck().state_I()
         && this.read_aI_TableIndex != null
         && this.update_ce_DataUtil != null
         && this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.ARMED) {
         double reach = Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange());
         BlockHitResult ray = init_BlockHitResult_Vec3(MC.player.getEyePosition(), this.update_ce_DataUtil, reach, MC.level, MC.player);
         if (ray != null) {
            MC.hitResult = ray;
         }
      }
   }

   private void entry_V() {
      this.node_V();
      if (init_Z_KeyMapping(MC.options.keyJump) || this.set_Z) {
         this.level_V();
      }

      if (!this.stop_Z && this.f_G_I <= 0 && !this.run_Z) {
         if (this.init_aI_CacheEntry != null && !this.init_aI_CacheEntry.init_aI_DataPipe().init_Z() && this.open_Z(MC.player)) {
            ScaffoldModule.Inner_DataPipe rise = this.load_aI_DataPipe_LocalPlayer_ItemStack(MC.player, this.init_ItemStack_2());
            if (rise != null && this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(MC.player, rise) != null) {
               this.init_aI_CacheEntry = null;
               this.close_Z = false;
            }
         }

         if (this.init_aI_CacheEntry != null) {
            ScaffoldModule.Inner_DataPipe prepared = this.init_aI_CacheEntry.init_aI_DataPipe();
            boolean optional = prepared.init_aI_TableIndex().load().equals(this.load_BlockPos);
            if (optional && this.init_aI_CacheEntry.init_I() == ck.init_ck().state_I()) {
               return;
            }

            if (!optional || this.push(MC.player, prepared.init_aI_TableIndex().init_BlockPos())) {
               InteractionHand preparedHand = this.init_InteractionHand();
               if (preparedHand != null && !al.init_Z_HitResult(prepared.init_aI_TableIndex().init_BlockHitResult(), preparedHand, this.id())) {
                  this.init_Z_aI_DataPipe(prepared, preparedHand, MC.player.getItemInHand(preparedHand), null);
               }

               return;
            }

            this.init_aI_CacheEntry = null;
            this.load_BlockPos = null;
         }

         if (this.next_Z()) {
            this.read_Z_LocalPlayer(MC.player);
         } else {
            ScaffoldModule.Inner_DataPipe optional = this.init_aI_DataPipe_LocalPlayer(MC.player);
            if (optional != null) {
               InteractionHand optionalHand = this.init_InteractionHand();
               if (optionalHand != null && !al.init_Z_HitResult(optional.init_aI_TableIndex().init_BlockHitResult(), optionalHand, this.id())) {
                  this.init_Z_aI_DataPipe(optional, optionalHand, MC.player.getItemInHand(optionalHand), null);
                  return;
               }
            }

            if (!this.init_Z_LocalPlayer_I(MC.player, this.f_L_I) || !this.tick(MC.player)) {
               this.write_Z = false;
               InteractionHand hand = this.init_InteractionHand();
               if (hand == null) {
                  if (this.write_Z) {
                     this.f_V();
                  }
               } else {
                  this.f_V();
                  ItemStack held = MC.player.getItemInHand(hand);
                  if (this.init_Z_ItemStack_4(held)) {
                     if (this.open_Z(MC.player) && this.sync_BlockPos != null) {
                        ScaffoldModule.Inner_DataPipe rise = this.load_aI_DataPipe_LocalPlayer_ItemStack(MC.player, held);
                        if (rise != null) {
                           ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(MC.player, rise);
                           if (live != null && !al.init_Z_HitResult(live.init_aI_TableIndex().init_BlockHitResult(), hand, this.id())) {
                              if (this.init_Z_aI_DataPipe(live, hand, held, null)
                                 && this.write_Z_LocalPlayer_BlockPos(MC.player, live.init_aI_TableIndex().load())) {
                                 this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
                              }

                              return;
                           }
                        }
                     }

                     if (this.init_aI_DataPipe == null
                        && (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING)
                        && (!this.open_Z(MC.player) || this.load_BlockPos_LocalPlayer(MC.player) == null)) {
                        this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(MC.player, held);
                     }

                     if (this.init_aI_DataPipe != null) {
                        ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(MC.player, this.init_aI_DataPipe);
                        if (live != null && !al.init_Z_HitResult(live.init_aI_TableIndex().init_BlockHitResult(), hand, this.id())) {
                           this.init_Z_aI_DataPipe(live, hand, held, null);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean init_Z_LocalPlayer_Z(LocalPlayer player, boolean descending) {
      if (!descending) {
         return false;
      }

      double targetTop = this.f_L_I + 1.0;
      return player.getY() < targetTop + 0.05 || player.getY() < this.f_v_D - 1.15 || this.f_H_I > 14;
   }

   private boolean load_Z_LocalPlayer_Z(LocalPlayer player, boolean descending) {
      if (descending && player != null) {
         double catchTop = this.start_Z ? this.f_L_I + 2.0 : this.f_L_I + 1.0;
         double fall = Math.max(0.0, -player.getDeltaMovement().y);
         return player.getY() <= catchTop + Math.max(0.35, fall * 2.0);
      } else {
         return false;
      }
   }

   private boolean update_Z_LocalPlayer(LocalPlayer player) {
      double catchTop = this.start_Z ? this.f_L_I + 2.0 : this.f_L_I + 1.0;
      double feetY = Math.min(catchTop, player.getY());
      Vec3 landing = init_Vec3_Vec3_3(player.position(), player.getDeltaMovement(), feetY, this.init_Vec3_LocalPlayer_2(player));
      double lane = init_D_Vec3_F(new Vec3(landing.x, player.getY(), landing.z), this.flags_F);
      return Math.abs(lane - this.f_x_D) > 0.4;
   }

   private boolean init_Z_LocalPlayer_ItemStack(LocalPlayer player, ItemStack stack) {
      if (this.stop_Z) {
         return true;
      }

      if (stack == null) {
         return false;
      }

      if (this.init_aI_DataPipe != null && this.init_aI_DataPipe.init_Z()) {
         return true;
      }

      this.write_Z = false;
      InteractionHand hand = this.init_InteractionHand();
      if (hand == null) {
         if (this.write_Z) {
            this.f_V();
            return true;
         } else {
            return false;
         }
      } else {
         this.f_V();
         ItemStack held = player.getItemInHand(hand);
         if (!this.init_Z_ItemStack_4(held)) {
            return false;
         }

         ScaffoldModule.Inner_DataPipe urgent = this.init_aI_DataPipe;
         if (urgent == null
            || !init_Z_BlockPos_6(
               urgent.init_aI_TableIndex().init_BlockPos(),
               urgent.init_aI_TableIndex().load(),
               urgent.init_aI_TableIndex().init_Direction(),
               this.init_Direction()
            )) {
            urgent = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, held);
         }

         if (urgent != null
            && init_Z_BlockPos_6(
               urgent.init_aI_TableIndex().init_BlockPos(),
               urgent.init_aI_TableIndex().load(),
               urgent.init_aI_TableIndex().init_Direction(),
               this.init_Direction()
            )) {
            this.init_aI_DataPipe = urgent;
            ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(player, urgent);
            if (live == null) {
               return true;
            }

            if (al.init_Z_HitResult(live.init_aI_TableIndex().init_BlockHitResult(), hand, this.id())) {
               return false;
            }

            this.init_Z_aI_DataPipe(live, hand, held, null, true);
            return true;
         } else {
            return false;
         }
      }
   }

   static boolean init_Z_BlockPos_6(BlockPos support, BlockPos placed, Direction face, Direction forward) {
      return support != null
         && placed != null
         && face != null
         && forward != null
         && face == forward
         && placed.equals(support.relative(forward))
         && placed.getY() == support.getY();
   }

   private void flush_V(LocalPlayer player) {
      this.add_Z = true;
      if (!this.stop_Z) {
         this.init_aI_DataPipe = null;
      }

      this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.RETURNING;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
      this.write_V_LocalPlayer(player);
   }

   private void node_V() {
      if (MC != null && MC.player != null && MC.level != null) {
         this.get_V();
         int tick = ck.init_ck().state_I();
         if (tick != this.f_bN_I) {
            this.f_bN_I = tick;
            this.task_V();
            if (this.f_G_I > 0) {
               this.f_G_I--;
            }
         }
      }
   }

   private void task_V() {
      if (this.index_Z()) {
         this.table_V();
         if (this.level_Z()) {
            this.drain_V();
            int age = Math.max(0, ck.init_ck().state_I() - this.f_aJ_I);
            if (init_Z_aI_SyncManager(this.init_aI_SyncManager, age, this.store_Z, this.f_aK_I)) {
               autismclient.util.as.update_V();
               this.init_V_Str_3("input-not-consumed");
            }

            if (this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE && this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.FAILED) {
               boolean supported = this.read_aI_TableIndex != null
                  && this.load_Z_BlockState_BlockPos(MC.level.getBlockState(this.read_aI_TableIndex.load()), this.read_aI_TableIndex.load());
               ScaffoldModule.Inner_ConfigStore decision = init_aI_ConfigStore_aI_SyncManager(
                  this.init_aI_SyncManager, age, this.store_Z, this.drain_Z, supported, load_Z_I_I(this.f_aM_I, this.f_aN_I)
               );
               if (decision.init_aI_SyncManager() == ScaffoldModule.Inner_SyncManager.FAILED) {
                  this.init_V_Str_3(decision.init_Str());
               } else {
                  this.init_aI_SyncManager = decision.init_aI_SyncManager();
               }
            }

            this.pool_V();
            if (this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE
               && (this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.PREDICTED || this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.FAILED || age >= 2)) {
               autismclient.util.as.update_V();
               this.run_V();
            }
         }
      }
   }

   static boolean init_Z_aI_SyncManager(ScaffoldModule.Inner_SyncManager state, int age, boolean resultSeen, int submitted) {
      return state == ScaffoldModule.Inner_SyncManager.ARMED && age > 0 && !resultSeen && submitted == 0;
   }

   static boolean init_Z_Z_2(boolean turnReserve, boolean resultSeen, int submitted) {
      return !turnReserve && (resultSeen || submitted > 0);
   }

   static ScaffoldModule.Inner_StreamGate init_aI_StreamGate_aI_SyncManager(ScaffoldModule.Inner_SyncManager state, int age, boolean resultSeen, boolean consumed, boolean supported) {
      if (state != null && state != ScaffoldModule.Inner_SyncManager.IDLE && state != ScaffoldModule.Inner_SyncManager.FAILED && (!resultSeen || consumed)) {
         if (resultSeen && consumed && supported && state != ScaffoldModule.Inner_SyncManager.ARMED) {
            return ScaffoldModule.Inner_StreamGate.PLACED;
         } else {
            return age >= 2 ? ScaffoldModule.Inner_StreamGate.FAILED : ScaffoldModule.Inner_StreamGate.WAITING;
         }
      } else {
         return ScaffoldModule.Inner_StreamGate.FAILED;
      }
   }

   private void pool_V() {
      if (this.stop_Z && this.open_BlockPos != null) {
         int age = Math.max(0, ck.init_ck().state_I() - this.f_aJ_I);
         ScaffoldModule.Inner_StreamGate status = init_aI_StreamGate_aI_SyncManager(
            this.init_aI_SyncManager,
            age,
            this.store_Z,
            this.drain_Z,
            this.load_Z_BlockState_BlockPos(MC.level.getBlockState(this.open_BlockPos), this.open_BlockPos)
         );
         if (status != ScaffoldModule.Inner_StreamGate.WAITING) {
            this.stop_Z = false;
            BlockPos placed = this.open_BlockPos;
            this.open_BlockPos = null;
            boolean turnReserve = this.pool_Z && this.push_BlockPos != null && this.push_BlockPos.equals(placed);
            this.pool_Z = false;
            boolean imperfection = this.poll_Z;
            this.poll_Z = false;
            if (status == ScaffoldModule.Inner_StreamGate.FAILED) {
               if (!imperfection && init_Z_Z_2(turnReserve, this.store_Z, this.f_aK_I)) {
                  this.f_J_I++;
               }

               this.init_aI_DataPipe = null;
            } else {
               if (this.read_aI_TableIndex != null) {
                  this.init_V_I_3(this.f_aM_I, placed, this.read_aI_TableIndex.init_BlockPos());
               }

               cg.init(placed);
               if (imperfection) {
                  this.init_aN.init_V();
                  this.open_Z = this.init_aN.init_Z();
                  this.load_BlockPos = null;
                  this.init_aI_DataPipe = null;
               } else if (turnReserve) {
                  this.f_J_I = 0;
                  this.init_aI_DataPipe = null;
                  this.data_V();
               } else {
                  boolean raised = this.init_aI_DataPipe != null
                     && this.init_aI_DataPipe.init_aI_TableIndex().load().equals(placed)
                     && this.init_aI_DataPipe.init_Z();
                  this.sync_BlockPos = placed.immutable();
                  if (raised) {
                     this.start_Z = true;
                     this.close_BlockPos = placed.immutable();
                  } else {
                     this.f_I_I++;
                     this.f_L_I = Math.min(this.f_L_I, placed.getY());
                  }

                  this.f_J_I = 0;
                  this.init_aI_DataPipe = null;
               }
            }
         }
      }
   }

   private void update_V_BlockPos(BlockPos under) {
      this.write_BlockPos = null;
      this.init_aM_DataUtil = null;
      this.flags_V();
      this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.FORWARD;
      this.init_aI_CacheEntry = null;
      this.close_Z = false;
      this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.FORWARD_DWELL;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
      this.mark_V();
      this.f_H_I = 0;
      this.buffer_Z = false;
      this.f_I_I = 0;
      this.f_J_I = 0;
      this.f_K_I = 0;
      this.clear_Z = false;
      this.start_Z = false;
      this.close_BlockPos = null;
      this.stop_Z = false;
      this.next_Z = false;
      this.f_F_I = 0;
      this.queue_Z = false;
      this.open_BlockPos = null;
      this.init_aI_DataPipe = null;
      this.data_Z = true;
      this.sync_BlockPos = under.immutable();
      this.f_L_I = under.getY();
   }

   private boolean read_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos under) {
      Direction direction = this.init_Direction();
      BlockPos ahead = under.relative(direction);
      if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(ahead), ahead)) {
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
      Vec3 forward = this.write_Vec3();
      Vec3 velocity = player.getDeltaMovement();
      double forwardSpeed = Math.max(0.0, velocity.x * forward.x + velocity.z * forward.z);
      if (!this.count_Z() && !this.put_Z && !this.set_Z && this.flush_I() <= 1) {
         double acceleration = init_D_D_6(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), this.poll_Z(player));
         return remaining <= read_D_D_D(forwardSpeed, acceleration);
      } else {
         return init_Z_D_D(remaining, forwardSpeed);
      }
   }

   static double read_D_D_D(double speed, double acceleration) {
      return Math.max(Math.max(0.2, 0.18 + speed * 0.5), speed + acceleration - 0.14);
   }

   static boolean init_Z_D_D(double supportRemaining, double forwardSpeed) {
      return supportRemaining <= load_D_D(forwardSpeed);
   }

   private static double load_D_D(double forwardSpeed) {
      return Mth.clamp(0.52 + Math.max(0.0, forwardSpeed) * 0.12, 0.52, 0.64);
   }

   private BlockPos init_BlockPos_LocalPlayer(LocalPlayer player) {
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
            if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(pos), pos)) {
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

   private ScaffoldModule.Inner_DataPipe init_aI_DataPipe_LocalPlayer_ItemStack(LocalPlayer player, ItemStack stack) {
      if (this.sync_BlockPos != null && this.init_Vec3 != null && this.init_Z_ItemStack_4(stack)) {
         BlockPos support = this.write_BlockPos();
         return this.init_aI_DataPipe_LocalPlayer(player, stack, support);
      } else {
         return null;
      }
   }

   private ScaffoldModule.Inner_DataPipe init_aI_DataPipe_LocalPlayer(LocalPlayer player, ItemStack stack, BlockPos support) {
      if (!this.init_Z_ItemStack_4(stack)) {
         return null;
      }

      if (support == null) {
         return null;
      }

      Direction direction = this.init_Direction();

      for (int i = 0; i < 8; i++) {
         BlockPos next = support.relative(direction);
         if (MC.level.isOutsideBuildHeight(next)) {
            return null;
         }

         BlockState state = MC.level.getBlockState(next);
         if (!this.load_Z_BlockState_BlockPos(state, next)) {
            if (!state.isAir() && !state.canBeReplaced()) {
               return null;
            }

            Vec3 aim = this.init_Vec3_BlockPos_Direction(support, direction);
            ScaffoldModule.Inner_TableIndex target = new ScaffoldModule.Inner_TableIndex(
               support.immutable(),
               next.immutable(),
               direction,
               new BlockHitResult(aim, direction, support, false),
               ce.init(aim, player.getEyePosition()),
               support.getY()
            );
            return new ScaffoldModule.Inner_DataPipe(target, false);
         }

         support = next;
      }

      return null;
   }

   private ScaffoldModule.Inner_DataPipe load_aI_DataPipe_LocalPlayer(LocalPlayer player, ItemStack stack, BlockPos support) {
      if (support != null && this.init_Z_ItemStack_4(stack)) {
         BlockPos raised = support.above();
         if (MC.level.isOutsideBuildHeight(raised)) {
            return null;
         }

         BlockState state = MC.level.getBlockState(raised);
         if (!state.isAir() && !state.canBeReplaced()) {
            return null;
         }

         AABB box = init_AABB_BlockPos(support);
         Vec3 aim = init_Vec3_AABB_Direction(box, Direction.UP);
         ScaffoldModule.Inner_TableIndex target = new ScaffoldModule.Inner_TableIndex(
            support.immutable(),
            raised.immutable(),
            Direction.UP,
            new BlockHitResult(aim, Direction.UP, support, false),
            ce.init(aim, player.getEyePosition()),
            box.maxY - 0.1 * box.getYsize()
         );
         return new ScaffoldModule.Inner_DataPipe(target, true);
      } else {
         return null;
      }
   }

   private Vec3 init_Vec3_BlockPos_Direction(BlockPos support, Direction direction) {
      double faceY = Mth.clamp(0.78 - this.f_I_I * 0.11, 0.28, 0.78);
      AABB box = init_AABB_BlockPos(support);
      Vec3 centre = init_Vec3_AABB_Direction(box, direction);
      return new Vec3(centre.x, box.minY + faceY * box.getYsize(), centre.z);
   }

   private boolean sync_Z(LocalPlayer player) {
      BlockPos support = this.poll_BlockPos;
      BlockPos cell = this.push_BlockPos;
      Direction direction = init_Direction_BlockPos_BlockPos(support, cell);
      if (support != null && cell != null && direction != null) {
         if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell)) {
            if (this.init_aI_DataPipe != null && cell.equals(this.init_aI_DataPipe.init_aI_TableIndex().load())) {
               this.init_aI_DataPipe = null;
            }

            this.data_V();
            return false;
         } else {
            if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(support), support)) {
               this.data_V();
               return false;
            }

            BlockState targetState = MC.level.getBlockState(cell);
            if (!targetState.isAir() && !targetState.canBeReplaced()) {
               this.data_V();
               return false;
            }

            if (!this.stop_Z && this.f_G_I <= 0) {
               this.write_Z = false;
               InteractionHand hand = this.init_InteractionHand();
               if (hand != null) {
                  this.f_V();
                  ItemStack held = player.getItemInHand(hand);
                  if (!this.init_Z_ItemStack_4(held)) {
                     return false;
                  }

                  Vec3 aim = this.init_Vec3_BlockPos_Direction(support, direction);
                  ScaffoldModule.Inner_TableIndex target = new ScaffoldModule.Inner_TableIndex(
                     support, cell, direction, new BlockHitResult(aim, direction, support, false), ce.init(aim, player.getEyePosition()), support.getY()
                  );
                  ScaffoldModule.Inner_DataPipe placement = new ScaffoldModule.Inner_DataPipe(target, false);
                  this.init_aI_DataPipe = placement;
                  ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(player, placement);
                  if (live == null) {
                     return true;
                  }

                  if (al.init_Z_HitResult(live.init_aI_TableIndex().init_BlockHitResult(), hand, this.id())) {
                     return false;
                  }

                  if (this.init_Z_aI_DataPipe(live, hand, held, cell)) {
                     this.pool_Z = true;
                  }

                  return true;
               } else if (this.write_Z) {
                  this.f_V();
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

   static Direction init_Direction_BlockPos_BlockPos(BlockPos support, BlockPos cell) {
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

   static boolean init_Z_BlockPos_4(BlockPos support, BlockPos cell, Direction overhang) {
      return overhang != null && overhang == init_Direction_BlockPos_BlockPos(support, cell);
   }

   static boolean init_Z_BlockPos_AABB_2(BlockPos cell, AABB playerBox) {
      return cell != null && playerBox != null && new AABB(cell).intersects(playerBox);
   }

   private boolean init_Z_aI_DataPipe(ScaffoldModule.Inner_DataPipe live, InteractionHand hand, ItemStack held, BlockPos queuedCell) {
      return this.run_Z ? false : this.init_Z_aI_DataPipe(live, hand, held, queuedCell, false);
   }

   private boolean init_Z_aI_DataPipe(ScaffoldModule.Inner_DataPipe live, InteractionHand hand, ItemStack held, BlockPos queuedCell, boolean lastChance) {
      this.close_Z = false;
      if (!MC.player.onGround() && !this.next_Z()) {
         if (this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.RETURNED && !lastChance) {
            return false;
         }

         if (this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.FORWARD && !this.pop(MC.player)) {
            return false;
         }
      }

      if (this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.RETURNED && (lastChance || this.next_Z())) {
         this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.PLACING;
         this.init_aI_CacheEntry = null;
         this.close_Z = false;
         this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.AIMING;
      }

      BlockState supportState = MC.level.getBlockState(live.init_aI_TableIndex().init_BlockPos());
      if (!autismclient.util.U.init_Z(supportState, live.init_aI_TableIndex().init_BlockPos())
         || init_Z_KeyMapping(MC.options.keyShift) && MC.player.isShiftKeyDown()) {
         if (init_Z_BlockPos_AABB_2(live.init_aI_TableIndex().load(), MC.player.getBoundingBox())) {
            return false;
         }

         boolean prepared = this.close(live.init_aI_TableIndex());
         boolean imperfection = live.init_aI_TableIndex().load().equals(this.load_BlockPos);
         float cap = this.update_F();
         ScaffoldModule.Inner_TableIndex aimed = this.init_aI_TableIndex_aI_TableIndex(live.init_aI_TableIndex());
         if (aimed == null) {
            aimed = this.init_aI_TableIndex_aI_TableIndex_Vec3(live.init_aI_TableIndex(), MC.player.getEyePosition());
         }

         if (aimed != null) {
            aimed = this.init_aI_TableIndex_aI_TableIndex_F(aimed, cap);
         }

         if (aimed == null || !prepared) {
            ScaffoldModule.Inner_UtilCore preview = this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(MC.player, live);
            if (preview != null) {
               queue_V();
               this.load_ce_DataUtil_ce_DataUtil(preview.init_ce_DataUtil());
            }

            return false;
         } else {
            if (!this.flush(aimed)) {
               this.close_Z = false;
               return false;
            }

            if (!MC.player.onGround()) {
               this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.PLACING;
            }

            queue_V();
            this.read_V_ce_DataUtil(aimed.init_ce_DataUtil());
            if (!this.init_Z_aI_TableIndex_InteractionHand(aimed, hand)) {
               this.close_Z = false;
               return false;
            }

            this.init_aI_CacheEntry = null;
            this.init_aI_DataPipe = live;
            this.stop_Z = true;
            this.open_BlockPos = queuedCell != null ? queuedCell : aimed.load().immutable();
            this.poll_Z = imperfection;
            if (imperfection) {
               this.open_Z = false;
            }

            this.f_G_I = 1;
            this.write_V_Str("PLACE");
            return true;
         }
      } else {
         return false;
      }
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex_F(ScaffoldModule.Inner_TableIndex target, float cap) {
      if (AutismLiteVariant.enabled()) {
         return target;
      }

      if (!MC.player.onGround() && target.init_Direction().getAxis() != Axis.Y && target.init_BlockPos().getY() == target.load().getY()) {
         Vec3 eye = MC.player.getEyePosition();
         Vec3 motion = MC.player.getDeltaMovement().add(this.read_Vec3().scale(0.0196));
         Vec3 next = eye.add(this.init_Vec3_LocalPlayer(MC.player, MC.player.getBoundingBox(), motion));
         double reach = MC.player.blockInteractionRange() - 0.035;
         if (ClutchBlockPlanner.init_Z_Vec3_2(
            next, Vec3.directionFromRotation(target.init_ce_DataUtil().load(), target.init_ce_DataUtil().init()), target.load(), target.init_Direction(), reach
         )) {
            return target;
         }

         ce.Inner_DataUtil from = this.poll_ce_DataUtil();

         for (ClutchBlockPlanner.Inner_StreamUtil counts : ClutchBlockPlanner.init_List_Vec3_2(
            eye,
            next,
            ClutchBlockPlanner.init(target.init_BlockPos(), target.init_Direction()),
            target.init_BlockPos(),
            target.init_Direction(),
            target.load(),
            target.init_Direction(),
            reach,
            from.init(),
            from.load(),
            ce.init(),
            0.02
         )) {
            ce.Inner_DataUtil rotation = new ce.Inner_DataUtil(from.init() + Mth.wrapDegrees(counts.init() - from.init()), counts.load());
            if (!(Math.abs(Mth.wrapDegrees(rotation.init() - from.init())) > cap)
               && !(Math.abs(rotation.load() - from.load()) > cap)
               && !(Math.abs(rotation.load()) > 89.5)) {
               BlockHitResult ray = init_BlockHitResult_Vec3(eye, rotation, reach, MC.level, MC.player);
               if (ray != null
                  && ray.getBlockPos().equals(target.init_BlockPos())
                  && ray.getDirection() == target.init_Direction()
                  && this.init_Z_BlockHitResult_3(ray)) {
                  return new ScaffoldModule.Inner_TableIndex(target.init_BlockPos(), target.load(), target.init_Direction(), ray, rotation, target.init_D());
               }
            }
         }

         return target;
      } else {
         return target;
      }
   }

   private boolean init_Z_aI_TableIndex_InteractionHand(ScaffoldModule.Inner_TableIndex aimed, InteractionHand hand) {
      if (MC.player == null || aimed == null || hand == null) {
         return false;
      }

      if (!this.flush(aimed)) {
         return false;
      }

      int tick = ck.init_ck().state_I();
      if (tick == this.f_z_I || this.poll_Z_3()) {
         return false;
      }

      if (BlinkPacketManager.load_Z()) {
         return false;
      }

      this.read_aI_TableIndex = aimed;
      this.read_aI_HashUtil = this.init_aI_HashUtil;
      this.sync_Vec3 = null;
      this.f_aJ_I = tick;
      this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.ARMED;
      this.add_J = load_J_J(this.add_J);
      this.put_J = this.add_J;
      this.init_InteractionHand = hand;
      this.f_C_I = hand == InteractionHand.MAIN_HAND ? MC.player.getInventory().getSelectedSlot() : -1;
      if (this.f_C_I >= 0) {
         this.read_I_I(this.f_C_I);
      }

      this.chunk_Z = this.load_Z_BlockHitResult_aI_TableIndex(aimed.init_BlockHitResult(), aimed);
      this.f_aK_I = 0;
      this.table_Z = false;
      this.f_aL_I = 0;
      this.update_ce_DataUtil = aimed.init_ce_DataUtil();
      this.sync_ce_DataUtil = this.load_ce_DataUtil;
      this.f_aM_I = -1;
      this.store_Z = false;
      this.drain_Z = false;
      this.fill_Z = false;
      this.write_Str = "queued";
      this.f_z_I = tick;
      this.set_J = -9223372036854775808L;
      if (this.close_Z) {
         bL.init_V();
      }

      MC.hitResult = aimed.init_BlockHitResult();
      autismclient.util.as.init_V_J(this.put_J);
      return true;
   }

   private void index_V() {
      if (this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE || this.read_aI_TableIndex != null) {
         autismclient.util.as.update_V();
         this.run_V();
      }
   }

   private void count_V() {
      if (this.f_aJ_I == ck.init_ck().state_I() && this.read_aI_TableIndex != null && this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.ARMED) {
         if (this.flush(this.read_aI_TableIndex)) {
            MC.hitResult = this.read_aI_TableIndex.init_BlockHitResult();
         } else {
            boolean unused = !this.store_Z && this.f_aK_I == 0;
            this.index_V();
            this.stop_Z = false;
            this.open_BlockPos = null;
            this.pool_Z = false;
            this.init_aI_DataPipe = null;
            if (unused) {
               this.f_z_I = -2147483648;
            }
         }
      }
   }

   private boolean flush(ScaffoldModule.Inner_TableIndex target) {
      Vec3 writtenPosition = write_Vec3;
      if (MC.player != null && MC.level != null && writtenPosition != null && target != null) {
         Vec3 eye = MC.player.getEyePosition();
         Vec3 wireEye = writtenPosition.add(0.0, MC.player.getEyeHeight(), 0.0);
         double reach = MC.player.blockInteractionRange();
         if (!init_Z_Vec3_9(eye, wireEye, target.init_BlockHitResult().getLocation(), reach)) {
            return false;
         } else if (this.close(target) && this.init_Z_Vec3(wireEye, target.init_ce_DataUtil(), target.init_BlockPos(), target.init_Direction(), reach)) {
            BlockHitResult liveRay = this.init_BlockHitResult_ce_DataUtil_D(target.init_ce_DataUtil(), reach);
            return liveRay != null
               && liveRay.getBlockPos().equals(target.init_BlockPos())
               && liveRay.getDirection() == target.init_Direction()
               && this.init_Z_BlockHitResult_3(liveRay)
               && init_Z_AABB_3(MC.player.getBoundingBox(), writtenPosition.subtract(MC.player.position()), target.load());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean close(ScaffoldModule.Inner_TableIndex target) {
      ScaffoldModule.Inner_TextFormat written = this.init_aI_TextFormat();
      Vec3 position = write_Vec3;
      return target != null
         && MC.player != null
         && written != null
         && position != null
         && this.init_Z_Vec3(
            position.add(0.0, MC.player.getEyeHeight(), 0.0),
            written.load(),
            target.init_BlockPos(),
            target.init_Direction(),
            MC.player.blockInteractionRange()
         );
   }

   private boolean init_Z_Vec3(Vec3 eye, ce.Inner_DataUtil rotation, BlockPos support, Direction face, double reach) {
      if (eye != null
         && rotation != null
         && support != null
         && face != null
         && Double.isFinite(reach)
         && !(reach <= 0.035)
         && Float.isFinite(rotation.init())
         && Float.isFinite(rotation.load())) {
         BlockHitResult ray = init_BlockHitResult_Vec3(eye, rotation, reach - 0.035, MC.level, MC.player);
         return ray != null && ray.getBlockPos().equals(support) && ray.getDirection() == face && this.init_Z_BlockHitResult_3(ray);
      } else {
         return false;
      }
   }

   static boolean init_Z_Vec3(Vec3 eye, ce.Inner_DataUtil rotation, BlockPos support, double reach) {
      if (eye != null
         && rotation != null
         && support != null
         && Double.isFinite(reach)
         && !(reach <= 0.0)
         && Float.isFinite(rotation.init())
         && Float.isFinite(rotation.load())) {
         AABB box = new AABB(support);
         if (box.contains(eye)) {
            return true;
         }

         Vec3 end = eye.add(Vec3.directionFromRotation(rotation.load(), rotation.init()).scale(reach));
         return box.clip(eye, end).isPresent();
      } else {
         return false;
      }
   }

   private boolean init_Z_BlockHitResult_3(BlockHitResult hit) {
      BlockPos support = hit.getBlockPos();
      VoxelShape shape = MC.level.getBlockState(support).getShape(MC.level, support, CollisionContext.of(MC.player));

      for (AABB box : shape.toAabbs()) {
         if (init_Z_Vec3_3(hit.getLocation(), box.move(support), hit.getDirection())) {
            return true;
         }
      }

      return false;
   }

   static boolean init_Z_Vec3_3(Vec3 hit, AABB box, Direction side) {
      if (hit != null && box != null && side != null) {
         ScaffoldModule.Inner_DataUtil face = ScaffoldModule.Inner_DataUtil.init(box, side).load(0.08);
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

   static double init_D_I(int supportY, int flatPlacements, double takeoffY, double playerY) {
      return Mth.clamp(supportY + 0.78 - Math.max(0, flatPlacements) * 0.11 - Math.max(0.0, takeoffY - playerY) * 0.18, supportY + 0.22, supportY + 0.8);
   }

   static boolean init_Z_Vec3_9(Vec3 eye, Vec3 wireEye, Vec3 hit, double reach) {
      if (eye != null && wireEye != null && hit != null && Double.isFinite(reach) && !(reach <= 0.035)) {
         double limit = (reach - 0.035) * (reach - 0.035);
         return eye.distanceToSqr(hit) <= limit && wireEye.distanceToSqr(hit) <= limit;
      } else {
         return false;
      }
   }

   static boolean init_Z_BlockPos_2(BlockPos support, int acknowledged, Iterable<ScaffoldModule.Inner_UiHelper> predictions) {
      for (ScaffoldModule.Inner_UiHelper prediction : predictions) {
         if (support.equals(prediction.init_BlockPos()) && !load_Z_I_I(prediction.init_I(), acknowledged)) {
            return false;
         }
      }

      return true;
   }

   private ScaffoldModule.Inner_DataPipe init_aI_DataPipe_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.Inner_DataPipe placement) {
      return this.init_aI_DataPipe_LocalPlayer(player, placement, player.getEyePosition(1.0F), player.getBoundingBox());
   }

   private ScaffoldModule.Inner_DataPipe init_aI_DataPipe_LocalPlayer(LocalPlayer player, ScaffoldModule.Inner_DataPipe placement, Vec3 eye, AABB body) {
      ScaffoldModule.Inner_TableIndex target = placement.init_aI_TableIndex();
      if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(target.init_BlockPos()), target.init_BlockPos())) {
         if (placement == this.init_aI_DataPipe) {
            this.init_aI_DataPipe = null;
         }

         return null;
      } else {
         if (!this.count_Z() && !init_Z_BlockPos_AABB_2(target.load(), body)) {
            ce.Inner_DataUtil current = this.poll_ce_DataUtil();
            BlockHitResult carried = init_BlockHitResult_Vec3(eye, current, player.blockInteractionRange(), MC.level, player);
            if (carried != null
               && carried.getBlockPos().equals(target.init_BlockPos())
               && carried.getDirection() == target.init_Direction()
               && this.init_Z_BlockHitResult_3(carried)
               && (!this.size_Z() || Math.abs(ce.init(current.init(), this.flags_F)) >= this.init_D_8() - 1.0)) {
               return new ScaffoldModule.Inner_DataPipe(
                  new ScaffoldModule.Inner_TableIndex(target.init_BlockPos(), target.load(), target.init_Direction(), carried, current, target.init_D()),
                  placement.init_Z()
               );
            }
         }

         if (target.init_Direction().getAxis().isVertical()) {
            if (init_Z_BlockPos_AABB_2(target.load(), body)) {
               return null;
            }

            Vec3 point = target.init_BlockHitResult().getLocation();
            if (placement.init_Z() && (!this.next_Z() || this.count_Z())) {
               point = init_Vec3_AABB(init_AABB_BlockPos(target.init_BlockPos()), eye, this.write_Vec3(), this.count_Z() ? null : this.init_Vec3_Vec3_2(eye));
               if (point == null) {
                  return null;
               }
            }

            ce.Inner_DataUtil rotation = ce.init(point, eye);
            if (placement.init_Z() && this.size_Z()) {
               rotation = this.init_ce_DataUtil_BlockPos(target.init_BlockPos(), target.init_Direction(), eye, this.poll_ce_DataUtil(), rotation);
               if (rotation == null) {
                  return null;
               }
            }

            BlockHitResult ray = init_BlockHitResult_Vec3(eye, rotation, player.blockInteractionRange(), MC.level, player);
            return ray != null && ray.getBlockPos().equals(target.init_BlockPos()) && ray.getDirection() == target.init_Direction()
               ? new ScaffoldModule.Inner_DataPipe(
                  new ScaffoldModule.Inner_TableIndex(target.init_BlockPos(), target.load(), target.init_Direction(), ray, rotation, target.init_D()), placement.init_Z()
               )
               : null;
         } else {
            BlockState supportState = MC.level.getBlockState(target.init_BlockPos());
            VoxelShape shape = supportState.getShape(MC.level, target.init_BlockPos(), CollisionContext.of(player));
            if (shape.isEmpty()) {
               return null;
            }

            Vec3 normal = new Vec3(target.init_Direction().getStepX(), target.init_Direction().getStepY(), target.init_Direction().getStepZ());
            double desiredY = init_D_I(target.init_BlockPos().getY(), this.f_I_I, this.f_v_D, eye.y - player.getEyeHeight());
            BlockPos above = target.init_BlockPos().above();
            if (MC.level.getBlockState(above).isCollisionShapeFullBlock(MC.level, above)) {
               desiredY = Math.min(desiredY, target.init_BlockPos().getY() + 0.2);
            }

            double reach = player.blockInteractionRange();
            ce.Inner_DataUtil current = this.poll_ce_DataUtil();
            if (this.size_Z() && target.init_Direction() == this.init_Direction()) {
               Vec3 top = init_Vec3_AABB(init_AABB_BlockPos(target.init_BlockPos()), eye, this.write_Vec3(), this.init_Vec3_Vec3_2(eye));
               if (top == null) {
                  return null;
               }

               ce.Inner_DataUtil fixed = this.init_ce_DataUtil_BlockPos(target.init_BlockPos(), target.init_Direction(), eye, current, ce.init(top, eye));
               if (fixed == null) {
                  return null;
               }

               BlockHitResult ray = init_BlockHitResult_Vec3(eye, fixed, reach, MC.level, player);
               return new ScaffoldModule.Inner_DataPipe(
                  new ScaffoldModule.Inner_TableIndex(target.init_BlockPos(), target.load(), target.init_Direction(), ray, fixed, target.init_D()), placement.init_Z()
               );
            } else {
               ScaffoldModule.Inner_RefQueue[] samples = new ScaffoldModule.Inner_RefQueue[read_DArr.length];
               double[] costs = new double[read_DArr.length];
               Arrays.fill(costs, 1.0 / 0.0);
               Vec3 blockOffset = new Vec3(target.init_BlockPos().getX(), target.init_BlockPos().getY(), target.init_BlockPos().getZ());

               for (AABB localBox : shape.toAabbs()) {
                  ScaffoldModule.Inner_DataUtil face = ScaffoldModule.Inner_DataUtil.init(localBox, target.init_Direction()).load(0.12).init(blockOffset);
                  if (!(face.init_D() <= 1.0E-9) && !(eye.subtract(face.load_Vec3()).dot(normal) <= 0.0125)) {
                     for (int offsetIndex = 0; offsetIndex < read_DArr.length; offsetIndex++) {
                        double offset = read_DArr[offsetIndex];
                        Vec3 point = init_Vec3_Vec3(
                           face.read(),
                           face.write(),
                           eye,
                           target.init_Direction(),
                           desiredY,
                           (this.count_Z() ? 0.0 : init_D_Vec3(face.read(), face.write(), eye, target.init_Direction(), this.init_Vec3_Vec3_2(eye))) + offset,
                           0.0F
                        );
                        if (!(eye.distanceToSqr(point) > reach * reach + 1.0E-7)) {
                           ce.Inner_DataUtil rotation = ce.init(point, eye);
                           BlockHitResult ray = init_BlockHitResult_Vec3(eye, rotation, reach, MC.level, player);
                           if (ray != null && ray.getBlockPos().equals(target.init_BlockPos()) && ray.getDirection() == target.init_Direction()) {
                              double cost = init_D_ce_DataUtil_ce_DataUtil(current, rotation) + Math.abs(offset) * 4.0 + eye.distanceTo(point) * 0.015;
                              if (cost < costs[offsetIndex]) {
                                 costs[offsetIndex] = cost;
                                 samples[offsetIndex] = new ScaffoldModule.Inner_RefQueue(face, point, rotation, ray, offsetIndex);
                              }
                           }
                        }
                     }
                  }
               }

               int selectedOffset = Double.isFinite(costs[0]) ? 0 : init_I_I_DArr(this.f_V_I, costs);
               ScaffoldModule.Inner_RefQueue best = selectedOffset < 0 ? null : samples[selectedOffset];
               if (best == null) {
                  return null;
               }

               this.f_V_I = selectedOffset;
               ScaffoldModule.Inner_TableIndex live = new ScaffoldModule.Inner_TableIndex(
                  target.init_BlockPos(),
                  target.load(),
                  target.init_Direction(),
                  best.init_BlockHitResult(),
                  best.init_ce_DataUtil(),
                  best.init_aI_DataUtil().read().y
               );
               return new ScaffoldModule.Inner_DataPipe(live, placement.init_Z());
            }
         }
      }
   }

   static int init_I_I_DArr(int lockedIndex, double[] costs) {
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (this.write_ce_DataUtil == null) {
         return null;
      }

      BlockHitResult ray = this.init_BlockHitResult_ce_DataUtil_D(this.write_ce_DataUtil, MC.player.blockInteractionRange());
      return ray != null
            && ray.getBlockPos().equals(target.init_BlockPos())
            && ray.getDirection() == target.init_Direction()
            && this.init_Z_BlockHitResult_3(ray)
         ? new ScaffoldModule.Inner_TableIndex(target.init_BlockPos(), target.load(), target.init_Direction(), ray, this.write_ce_DataUtil, target.init_D())
         : null;
   }

   private ScaffoldModule.Inner_TableIndex load_aI_TableIndex_aI_TableIndex(ScaffoldModule.Inner_TableIndex live) {
      return this.load_aI_TableIndex_aI_TableIndex_F(live, this.update_F());
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex_Vec3(ScaffoldModule.Inner_TableIndex live, Vec3 eye) {
      this.update_Z = false;
      ce.Inner_DataUtil proposed = this.init_aM()
         .init(this.poll_ce_DataUtil(), live.init_ce_DataUtil(), false, this.init_RangeSetting.get(), ce.init(), ck.init_ck().state_I());
      ScaffoldModule.Inner_TableIndex endpoint = new ScaffoldModule.Inner_TableIndex(
         live.init_BlockPos(), live.load(), live.init_Direction(), live.init_BlockHitResult(), proposed, live.init_D()
      );
      return this.init_aI_TableIndex_aI_TableIndex(endpoint, this.update_F(), eye, true);
   }

   private ScaffoldModule.Inner_TableIndex load_aI_TableIndex_aI_TableIndex_F(ScaffoldModule.Inner_TableIndex live, float cap) {
      return this.init_aI_TableIndex_aI_TableIndex(live, cap, MC.player.getEyePosition());
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex(ScaffoldModule.Inner_TableIndex live, float cap, Vec3 eye) {
      return this.init_aI_TableIndex_aI_TableIndex(live, cap, eye, false);
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex(ScaffoldModule.Inner_TableIndex live, float cap, Vec3 eye, boolean continuous) {
      ce.Inner_DataUtil from = this.poll_ce_DataUtil();
      ce.Inner_DataUtil stepped = continuous
         ? ce.load(from, live.init_ce_DataUtil(), cap, cap, ce.init())
         : load_ce_DataUtil_ce_DataUtil(from, live.init_ce_DataUtil(), cap, cap, ce.init());
      BlockHitResult ray = init_BlockHitResult_Vec3(eye, stepped, MC.player.blockInteractionRange(), MC.level, MC.player);
      if (ray != null && ray.getBlockPos().equals(live.init_BlockPos()) && ray.getDirection() == live.init_Direction() && this.init_Z_BlockHitResult_3(ray)) {
         return new ScaffoldModule.Inner_TableIndex(live.init_BlockPos(), live.load(), live.init_Direction(), ray, stepped, live.init_D());
      }

      double gcd = read_D_D(ce.init());

      for (int radius = 1; radius <= 3; radius++) {
         for (int yawCounts = -radius; yawCounts <= radius; yawCounts++) {
            int pitchCounts = radius - Math.abs(yawCounts);
            ScaffoldModule.Inner_TableIndex adjusted = this.init_aI_TableIndex_aI_TableIndex(live, from, stepped, yawCounts, pitchCounts, gcd, cap, eye);
            if (adjusted != null) {
               return adjusted;
            }

            if (pitchCounts != 0) {
               adjusted = this.init_aI_TableIndex_aI_TableIndex(live, from, stepped, yawCounts, -pitchCounts, gcd, cap, eye);
               if (adjusted != null) {
                  return adjusted;
               }
            }
         }
      }

      return null;
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_aI_TableIndex(
      ScaffoldModule.Inner_TableIndex live, ce.Inner_DataUtil from, ce.Inner_DataUtil base, int yawCounts, int pitchCounts, double gcd, float cap, Vec3 eye
   ) {
      ce.Inner_DataUtil candidate = new ce.Inner_DataUtil(
         base.init() + (float)(yawCounts * gcd), Mth.clamp(base.load() + (float)(pitchCounts * gcd), -90.0F, 90.0F)
      );
      float yawDelta = Math.abs(candidate.init() - from.init());
      float pitchDelta = Math.abs(candidate.load() - from.load());
      double countSlack = gcd * 0.501;
      if (!(yawDelta > cap + countSlack) && !(pitchDelta > cap + countSlack)) {
         BlockHitResult ray = init_BlockHitResult_Vec3(eye, candidate, MC.player.blockInteractionRange(), MC.level, MC.player);
         return ray != null
               && ray.getBlockPos().equals(live.init_BlockPos())
               && ray.getDirection() == live.init_Direction()
               && this.init_Z_BlockHitResult_3(ray)
            ? new ScaffoldModule.Inner_TableIndex(live.init_BlockPos(), live.load(), live.init_Direction(), ray, candidate, live.init_D())
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

      this.write_Z = false;
      InteractionHand hand = this.init_InteractionHand();
      if (hand == null) {
         if (this.write_Z) {
            this.f_V();
            return true;
         } else {
            return false;
         }
      } else {
         this.f_V();
         ItemStack held = player.getItemInHand(hand);
         if (!this.init_Z_ItemStack_4(held)) {
            return false;
         }

         if (this.init_aI_DataPipe == null) {
            this.init_aI_DataPipe = this.init_aI_DataPipe_LocalPlayer_ItemStack(player, held);
         }

         if (this.init_aI_DataPipe == null) {
            return false;
         }

         ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(player, this.init_aI_DataPipe);
         if (live == null) {
            return false;
         }

         if (al.init_Z_HitResult(live.init_aI_TableIndex().init_BlockHitResult(), hand, this.id())) {
            return false;
         }

         this.init_Z_aI_DataPipe(live, hand, held, null);
         return true;
      }
   }

   private boolean close_Z(LocalPlayer player) {
      return this.close_BlockPos == null ? false : this.write_Z_LocalPlayer_BlockPos(player, this.close_BlockPos);
   }

   private boolean write_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos raised) {
      double feetY = raised.getY() + 1.0;
      Vec3 projected = read_Vec3_Vec3(player.position(), player.getDeltaMovement(), feetY, this.init_Vec3_LocalPlayer_2(player));
      return !init_Z_AABB_BlockPos(player.getBoundingBox().move(projected.x - player.getX(), feetY - player.getY(), projected.z - player.getZ()), raised);
   }

   private boolean open_Z(LocalPlayer player) {
      return this.clear_Z && !this.start_Z ? player.getDeltaMovement().y > 0.0 || player.getY() > this.f_L_I + 2.0 : false;
   }

   private BlockPos load_BlockPos_LocalPlayer(LocalPlayer player) {
      if (this.sync_BlockPos == null) {
         return null;
      }

      if (this.write_BlockPos != null && this.clear_Z) {
         BlockPos support = this.write_BlockPos;
         if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(support), support)) {
            return null;
         }

         Vec3 landing = init_Vec3_Vec3_3(
            player.position(), player.getDeltaMovement(), this.f_L_I + 2.0, this.write_Vec3(), Math.max(0, this.f_E_I - this.f_H_I)
         );
         if (!init_Z_AABB_2(player.getBoundingBox().move(landing.subtract(player.position())), support.above(), 0.12)) {
            return null;
         }

         BlockState above = MC.level.getBlockState(support.above());
         return !above.isAir() && !above.canBeReplaced() ? null : support;
      } else {
         double feetY = this.f_L_I + 2.0;
         Vec3 projected = init_Vec3_Vec3_3(player.position(), player.getDeltaMovement(), feetY, this.read_Vec3(), this.entry_Z ? 1 : 14);
         BlockPos cell = this.init_BlockPos_BlockPos(BlockPos.containing(projected.x, this.f_L_I, projected.z));
         Direction back = this.init_Direction().getOpposite();

         for (int step = 0; step <= 1; step++) {
            BlockPos candidate = step == 0 ? cell : cell.relative(back, step);
            if (!MC.level.isOutsideBuildHeight(candidate)
               && this.load_Z_BlockState_BlockPos(MC.level.getBlockState(candidate), candidate)
               && init_Z_Vec3_7(player.position(), player.getDeltaMovement(), player.getBoundingBox(), candidate.above(), this.init_Vec3_LocalPlayer_2(player))
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

      Vec3 powered = read_Vec3_Vec3(position, velocity, feetY, forward);
      if (init_Z_AABB_BlockPos(footprint.move(powered.x - position.x, feetY - position.y, powered.z - position.z), raised)) {
         return true;
      }

      Vec3 coast = read_Vec3_Vec3(position, velocity, feetY, Vec3.ZERO);
      return init_Z_AABB_BlockPos(footprint.move(coast.x - position.x, feetY - position.y, coast.z - position.z), raised);
   }

   private static Vec3 read_Vec3_Vec3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward) {
      return init_Vec3_Vec3_3(position, velocity, feetY, forward, 14);
   }

   private static Vec3 init_Vec3_Vec3_3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward, int controlTicks) {
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

   private Vec3 init_Vec3_LocalPlayer_2(LocalPlayer player) {
      Vec3 velocity = player.getDeltaMovement();
      double hx = velocity.x;
      double hz = velocity.z;
      double length = Math.sqrt(hx * hx + hz * hz);
      return length < 0.05 ? this.write_Vec3() : new Vec3(hx / length, 0.0, hz / length);
   }

   private boolean init_Z_LocalPlayer_I(LocalPlayer player, int blockY) {
      double feetY = blockY + 1.0;
      Vec3 projected = this.init_aI_FieldCache_LocalPlayer_D(player, feetY).init_Vec3();
      return this.init_Z_LocalPlayer(player, blockY, projected, feetY);
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, int blockY, int distance) {
      double feetY = blockY + 1.0;
      Vec3 projected = this.init_aI_FieldCache_LocalPlayer_D(player, feetY).init_Vec3();
      BlockPos runway = this.init_BlockPos_BlockPos(BlockPos.containing(projected.x, blockY, projected.z))
         .relative(this.init_Direction(), Math.max(1, distance));
      return this.load_Z_BlockState_BlockPos(MC.level.getBlockState(runway), runway);
   }

   static boolean load_Z_Z(boolean landingSecured, boolean turnPending, boolean turnReserveCovered) {
      return landingSecured && (!turnPending || turnReserveCovered);
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, int blockY, Vec3 projected, double feetY) {
      AABB moved = player.getBoundingBox().move(projected.x - player.getX(), feetY - player.getY(), projected.z - player.getZ());
      return this.init_Z_AABB(moved, feetY, 0.12);
   }

   private boolean load_Z_LocalPlayer_I(LocalPlayer player, int blockY) {
      if (!this.count_Z() && !this.clear_Z && !this.put_Z && !this.next_Z()) {
         double feetY = blockY + 1.0;
         Vec3 selected = this.init_aI_FieldCache_LocalPlayer_D(player, feetY).init_Vec3();
         Vec3 coast = init_aI_FieldCache_Vec3(
               player.position(),
               player.getDeltaMovement(),
               feetY,
               Vec3.ZERO,
               0.91,
               player.getBoundingBox(),
               false,
               (box, movement) -> this.init_Vec3_LocalPlayer(player, box, movement)
            )
            .init_Vec3();
         AABB body = player.getBoundingBox();
         AABB selectedBox = body.move(selected.subtract(player.position()));
         AABB coastBox = body.move(coast.subtract(player.position()));
         return init_Z_AABB(selectedBox, coastBox, this.update_Vec3(), this.f_x_D, box -> this.init_Z_AABB(box, feetY, 0.125));
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

   private boolean init_Z_AABB(AABB footprint, double feetY, double overlap) {
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
               if (this.load_Z_BlockState_BlockPos(state, pos)) {
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
      Vec3 projected = init_Vec3_Vec3_3(player.position(), player.getDeltaMovement(), feetY, this.write_Vec3());
      AABB moved = player.getBoundingBox().move(projected.x - player.getX(), feetY - player.getY(), projected.z - player.getZ());
      return init_Z_AABB_BlockPos(moved, block);
   }

   static boolean init_Z_AABB_BlockPos(AABB footprint, BlockPos block) {
      return init_Z_AABB_2(footprint, block, 0.12);
   }

   static boolean init_Z_AABB_2(AABB footprint, BlockPos block, double requiredOverlap) {
      if (footprint != null && block != null) {
         double overlapX = Math.min(footprint.maxX, block.getX() + 1.0) - Math.max(footprint.minX, block.getX());
         double overlapZ = Math.min(footprint.maxZ, block.getZ() + 1.0) - Math.max(footprint.minZ, block.getZ());
         return overlapX >= requiredOverlap && overlapZ >= requiredOverlap;
      } else {
         return false;
      }
   }

   static Vec3 init_Vec3_Vec3_2(Vec3 position, Vec3 velocity, double feetY) {
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

   static int init_I_Vec3(Vec3 position, Vec3 velocity, double feetY) {
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

   static Vec3 init_Vec3_Vec3_3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward) {
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

   private Vec3 load_Vec3_LocalPlayer(LocalPlayer player) {
      boolean sprinting = this.poll_Z(player);
      double groundAcceleration = init_D_D_6(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), sprinting);
      return init_Vec3_Vec3(player.getDeltaMovement(), this.flags_F, groundAcceleration, sprinting);
   }

   static double init_D_D_6(double movementSpeed, boolean wasSprinting, boolean sprinting) {
      double speed = Math.max(0.0, movementSpeed);
      if (sprinting != wasSprinting) {
         speed *= sprinting ? 1.3 : 0.7692307692307692;
      }

      return speed * 0.98;
   }

   private boolean poll_Z(LocalPlayer player) {
      if (!this.count_Z() && this.f_Z_I != ck.init_ck().state_I() && !init_Z_KeyMapping(MC.options.keyShift)) {
         AutismLocalPlayerAccessor vanilla = (AutismLocalPlayerAccessor)player;
         return vanilla.autism$isSprintingPossible(player.getAbilities().flying)
            && !player.isMovingSlowly()
            && !vanilla.autism$isSlowDueToUsingItem()
            && !player.isFallFlying();
      } else {
         return false;
      }
   }

   static Vec3 init_Vec3_Vec3(Vec3 velocity, float visibleYawDegrees, double groundAcceleration, boolean sprinting) {
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
         Vec3 launch = this.count_Z()
            ? init_Vec3_Vec3(player.getDeltaMovement(), this.flags_F, Math.max(0.1, player.getSpeed()) * 0.98, true)
            : this.load_Vec3_LocalPlayer(player);
         double drag = MC.level.getBlockState(support).getBlock().getFriction() * 0.91;
         AABB body = player.getDimensions(Pose.STANDING).makeBoundingBox(player.position());
         if (!this.count_Z()) {
            body = body.inflate(0.08, 0.0, 0.08);
         }

         return init_aI_FieldCache_Vec3(
               player.position(),
               launch,
               player.getY(),
               this.write_Vec3(),
               drag,
               body,
               true,
               (box, movement) -> this.init_Vec3_LocalPlayer(player, box, movement)
            )
            .init_Z();
      } else {
         return true;
      }
   }

   private Vec3 init_Vec3_LocalPlayer(LocalPlayer player, AABB box, Vec3 movement) {
      return Entity.collideBoundingBox(player, movement, box, MC.level, MC.level.getEntityCollisions(player, box.expandTowards(movement)));
   }

   private ScaffoldModule.Inner_FieldCache init_aI_FieldCache_LocalPlayer_D(LocalPlayer player, double feetY) {
      return init_aI_FieldCache_Vec3(
         player.position(),
         player.getDeltaMovement(),
         feetY,
         this.read_Vec3(),
         0.91,
         player.getBoundingBox(),
         false,
         (box, movement) -> this.init_Vec3_LocalPlayer(player, box, movement),
         this.entry_Z ? 1 : (this.node_Z ? this.f_X_I : 14)
      );
   }

   private Vec3 read_Vec3() {
      if (this.entry_Z) {
         return this.write_Vec3().scale(-1.0);
      }

      Vec3 forward = this.init_aI_KeyHolder != ScaffoldModule.Inner_KeyHolder.HOLD && !this.pop_Z ? this.write_Vec3() : Vec3.ZERO;
      double side = this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.FORWARD
         ? 0.0
         : (this.init_aI_c == ScaffoldModule.Inner_c.LEFT ? 1.0 : (this.init_aI_c == ScaffoldModule.Inner_c.RIGHT ? -1.0 : 0.0));
      Vec3 input = forward.add(this.update_Vec3().scale(side));
      return input.normalize().scale(MC.player != null && MC.player.isCrouching() ? 0.3 : 1.0);
   }

   static ScaffoldModule.Inner_FieldCache init_aI_FieldCache_Vec3(
      Vec3 position,
      Vec3 velocity,
      double feetY,
      Vec3 forward,
      double launchDrag,
      AABB footprint,
      boolean launch,
      BiFunction<AABB, Vec3, Vec3> collisionResolver
   ) {
      return init_aI_FieldCache_Vec3(position, velocity, feetY, forward, launchDrag, footprint, launch, collisionResolver, 14);
   }

   static ScaffoldModule.Inner_FieldCache init_aI_FieldCache_Vec3(
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
            return new ScaffoldModule.Inner_FieldCache(new Vec3(projected.x, feetY, projected.z), obstructed);
         }

         double drag = launch && tick == 0 ? Mth.clamp(launchDrag, 0.0, 1.2) : 0.91;
         motion = new Vec3(blockedX ? 0.0 : motion.x * drag, ((blockedY ? 0.0 : motion.y) - 0.08) * 0.98, blockedZ ? 0.0 : motion.z * drag);
      }

      return new ScaffoldModule.Inner_FieldCache(new Vec3(projected.x, feetY, projected.z), obstructed);
   }

   private boolean flush_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && MC.level != null) {
         Vec3 launchVelocity = this.load_Vec3_LocalPlayer(player);
         double launchDrag = MC.level.getBlockState(support).getBlock().getFriction() * 0.91;
         Vec3 forward = this.write_Vec3();
         Vec3 landing = init_Vec3_Vec3_2(player.position(), launchVelocity, player.getY(), forward, launchDrag);
         Vec3 coastLanding = init_Vec3_Vec3_2(player.position(), launchVelocity, player.getY(), forward, launchDrag, 0.0);
         Vec3 fastLanding = init_Vec3_Vec3_2(player.position(), launchVelocity, player.getY(), forward, launchDrag, this.poll_Z(player) ? 0.026 : 0.0196);
         return this.init_Z_LocalPlayer_4(player, support, coastLanding)
            && this.init_Z_LocalPlayer_4(player, support, landing)
            && this.init_Z_LocalPlayer_4(player, support, fastLanding);
      } else {
         return false;
      }
   }

   private boolean init_Z_LocalPlayer_4(LocalPlayer player, BlockPos support, Vec3 landing) {
      Direction direction = this.init_Direction();
      BlockPos projectedCell = BlockPos.containing(landing.x, support.getY(), landing.z);
      if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(projectedCell), projectedCell)) {
         return true;
      }

      BlockPos catchBlock = init_BlockPos_BlockPos(projectedCell, support, direction);
      int steps = (catchBlock.getX() - support.getX()) * direction.getStepX() + (catchBlock.getZ() - support.getZ()) * direction.getStepZ();
      if (steps >= 1 && steps <= 6) {
         for (int step = 1; step <= steps; step++) {
            BlockPos cell = support.relative(direction, step);
            if (MC.level.isOutsideBuildHeight(cell)) {
               return false;
            }

            BlockState state = MC.level.getBlockState(cell);
            if (!this.load_Z_BlockState_BlockPos(state, cell) && !state.isAir() && !state.canBeReplaced()) {
               return false;
            }
         }

         double feetY = support.getY() + 1.0;
         AABB landingBox = player.getBoundingBox().move(landing.x - player.getX(), feetY - player.getY(), landing.z - player.getZ());
         return init_Z_AABB_2(landingBox, catchBlock, 0.12);
      } else {
         return false;
      }
   }

   private boolean init_Z_LocalPlayer_3(LocalPlayer player, BlockPos under, ScaffoldModule.Inner_DataPipe first) {
      if (player == null || under == null || first == null) {
         return false;
      } else if (this.pop_Z || this.start_Z() || this.task_Z) {
         return false;
      } else if (!this.data_Z()) {
         return false;
      } else {
         boolean catchable = !this.set_Z && !this.put_Z ? this.flush_Z_LocalPlayer_BlockPos(player, under) : this.close_Z_LocalPlayer_BlockPos(player, under);
         if (!catchable) {
            return false;
         } else {
            return !init_Z_D_3(this.init_D_LocalPlayer(player))
               ? false
               : read_Z_I(this.init_I_LocalPlayer_BlockPos(player, under)) && this.load_Z_LocalPlayer(player, under, first);
         }
      }
   }

   private boolean load_Z_LocalPlayer(LocalPlayer player, BlockPos under, ScaffoldModule.Inner_DataPipe first) {
      if (first != null && !first.init_Z()) {
         Vec3 forward = this.write_Vec3();
         ScaffoldModule.Inner_TableIndex target = first.init_aI_TableIndex();
         if (target.init_Direction() != null && target.init_Direction() == this.init_Direction()) {
            Vec3 launch = this.load_Vec3_LocalPlayer(player);
            double drag = MC.level.getBlockState(under).getBlock().getFriction() * 0.91;
            double feetY = under.getY() + 1.0;
            double air = this.poll_Z(player) ? 0.026 : 0.0196;
            Vec3 shortest = init_Vec3_Vec3_2(player.position(), launch, feetY, forward, drag, 0.0196);
            Vec3 longest = init_Vec3_Vec3_2(player.position(), launch, feetY, forward, drag, air).add(forward.scale(0.25));
            if (this.init_Z_LocalPlayer(player, under.getY(), shortest, feetY) && this.init_Z_LocalPlayer(player, under.getY(), longest, feetY)) {
               return true;
            }

            double face = Vec3.atCenterOf(target.init_BlockPos()).dot(forward) + 0.5;
            return init_I_Vec3(player.position(), launch, feetY, forward, drag, air, face) >= Math.max(4, this.init_aM_DataUtil().init_I() + 2);
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   private boolean init_Z_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.Inner_DataPipe first) {
      return this.init_Z_LocalPlayer_2(player, first, false);
   }

   private boolean init_Z_LocalPlayer_2(LocalPlayer player, ScaffoldModule.Inner_DataPipe first, boolean flatEntry) {
      if (first != null && !first.init_Z() && (flatEntry || !this.set_Z && !this.put_Z)) {
         BlockPos root = first.init_aI_TableIndex().init_BlockPos();
         BlockPos under = this.init_BlockPos_LocalPlayer(player);
         if (under == null) {
            return true;
         }

         Vec3 launch = this.load_Vec3_LocalPlayer(player);
         double drag = this.load_D_BlockPos(under);
         double feetY = under.getY() + 1.0;
         Vec3 landing = init_Vec3_Vec3_2(player.position(), launch, feetY, this.write_Vec3(), drag, 0.0);
         BlockPos cell = this.init_BlockPos_BlockPos(BlockPos.containing(landing.x, under.getY(), landing.z));
         Direction direction = this.init_Direction();
         int distance = Math.max(0, (cell.getX() - root.getX()) * direction.getStepX() + (cell.getZ() - root.getZ()) * direction.getStepZ());
         int missing = 0;

         for (int step = 1; step <= Math.min(8, distance); step++) {
            BlockPos needed = root.relative(direction, step);
            if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(needed), needed)) {
               missing++;
            }
         }

         Vec3 afterLaunch = new Vec3(launch.x * drag, (launch.y - 0.08) * 0.98, launch.z * drag);
         int flight = 1 + init_I_Vec3(player.position().add(launch), afterLaunch, feetY);
         int extras = flatEntry ? 0 : this.flush_I();
         int turn = this.init_aM_DataUtil().init_I();
         if (init_Z_I_3(flight, missing, turn, extras)) {
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
                     needed -> this.load_Z_BlockState_BlockPos(MC.level.getBlockState(needed), needed)
                  )
                  .init_Z();
         }
      } else {
         return true;
      }
   }

   static boolean init_Z_I_2(int flightTicks, int missing, int rotationTicks) {
      return init_Z_I_3(flightTicks, missing, rotationTicks, 0);
   }

   static boolean init_Z_I_3(int flightTicks, int missing, int rotationTicks, int extraTops) {
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

   private double init_D_LocalPlayer(LocalPlayer player) {
      return player == null ? 0.0 : this.f_x_D - init_D_Vec3_F(player.position(), this.flags_F);
   }

   static boolean read_Z_I(int steps) {
      return steps >= 2 && steps <= 4;
   }

   static boolean init_Z_D_3(double laneError) {
      return Double.isFinite(laneError) && Math.abs(laneError) <= 0.3;
   }

   private int init_I_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && MC.level != null) {
         Vec3 launchVelocity = this.load_Vec3_LocalPlayer(player);
         double launchDrag = MC.level.getBlockState(support).getBlock().getFriction() * 0.91;
         Vec3 landing = init_Vec3_Vec3_2(player.position(), launchVelocity, player.getY(), this.write_Vec3(), launchDrag);
         Direction direction = this.init_Direction();
         BlockPos catchBlock = BlockPos.containing(landing.x, support.getY(), landing.z);
         return (catchBlock.getX() - support.getX()) * direction.getStepX() + (catchBlock.getZ() - support.getZ()) * direction.getStepZ();
      } else {
         return -1;
      }
   }

   private boolean close_Z_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      return !this.flush_Z_LocalPlayer_BlockPos(player, support)
         ? false
         : init_Z_Vec3_4(player.position(), this.load_Vec3_LocalPlayer(player), support.getY() + 2.0);
   }

   private boolean open_Z(LocalPlayer player, BlockPos footing) {
      return this.init_Z_LocalPlayer_2(player, footing, player.position(), player.getDeltaMovement(), player.getBoundingBox());
   }

   private boolean init_Z_LocalPlayer_2(LocalPlayer player, BlockPos footing, Vec3 position, Vec3 velocity, AABB body) {
      return this.init_Z_LocalPlayer(player, footing, position, velocity, body, this.poll_Z(player));
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, BlockPos footing, Vec3 position, Vec3 velocity, AABB body, boolean sprinting) {
      if (!this.count_Z() && this.put_Z) {
         return this.init_I_LocalPlayer(player, footing, position, velocity, body, sprinting) >= 0;
      }

      double acceleration = init_D_D_6(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), sprinting);
      Vec3 launch = init_Vec3_Vec3(velocity, this.flags_F, acceleration, sprinting);
      return init_Z_Vec3(
         position,
         launch,
         body,
         footing,
         this.init_Direction(),
         this.load_D_BlockPos(footing),
         this.init_aM_DataUtil().init_I(),
         cell -> this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell),
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
      Vec3 landing = read_Vec3_Vec3(position.add(launch), after, feetY, Vec3.ZERO);
      AABB landed = body.move(landing.subtract(position));
      BlockPos projected = init_BlockPos_BlockPos(BlockPos.containing(landing.x, footing.getY(), landing.z), footing, direction);
      int flight = 1 + init_I_Vec3(position.add(launch), after, feetY);

      for (int back = 0; back <= 1; back++) {
         BlockPos candidate = projected.relative(direction.getOpposite(), back);
         if (init_Z_AABB_BlockPos(landed, candidate.above())) {
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

      double acceleration = init_D_D_6(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), sprinting);
      Vec3 launch = init_Vec3_Vec3(velocity, this.flags_F, acceleration, sprinting);
      return init_I_Vec3(
         position,
         launch,
         body,
         footing,
         this.init_Direction(),
         this.load_D_BlockPos(footing),
         cell -> this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell),
         cell -> MC.level.getBlockState(cell).isAir() || MC.level.getBlockState(cell).canBeReplaced(),
         cell -> !MC.level.isOutsideBuildHeight(cell)
      );
   }

   private boolean poll(LocalPlayer player, BlockPos footing) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (!this.pop_Z && !this.init_aI_b.load() && !this.start_Z() && !this.task_Z && this.data_Z()) {
         double acceleration = init_D_D_6(player.getAttributeValue(Attributes.MOVEMENT_SPEED), player.isSprinting(), this.poll_Z(player));
         Vec3 velocity = new Vec3(player.getDeltaMovement().x, 0.0, player.getDeltaMovement().z);
         Vec3 force = this.write_Vec3().scale(acceleration);
         Vec3 step = velocity.add(force);
         if (this.init_Vec3_LocalPlayer(player, player.getBoundingBox(), step).distanceToSqr(step) > 1.0E-8) {
            return false;
         }

         if (!init_Z_AABB(
            player.getBoundingBox(), velocity, force, this.load_D_BlockPos(footing), acceleration * 0.6, box -> this.init_Z_AABB(box, player.getY(), 0.125)
         )) {
            return false;
         }

         this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.FORWARD;
         this.update_Input = null;
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
            if (init_Z_AABB_2(body.move(landing.subtract(position)), target.above(), 0.125)) {
               Vec3 fastLanding = new Vec3(fastAt.x, feetY, fastAt.z);
               if (init_Z_AABB_2(body.move(fastLanding.subtract(position)), target.above(), 0.125)) {
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
      if (!this.count_Z()
         && this.push_Z
         && !player.onGround()
         && !this.clear_Z
         && !this.start_Z
         && !this.next_Z()
         && !this.start_Z()
         && !this.init_aI_b.load()
         && (
            this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH
               || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING
               || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING
         )) {
         BlockPos root = this.write_BlockPos();
         if (root != null) {
            if (this.f_A_I == 2 && (this.open_Z || this.poll_Z) && this.init_aN.init_Z()) {
               this.node_Z = true;
               this.buffer_Z = true;
               this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
            } else {
               boolean returning = this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING || this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.RETURNED;
               if (returning && this.flush_I() > 1) {
                  this.node_Z = true;
                  this.f_X_I = 0;
                  this.buffer_Z = true;
                  this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
               } else {
                  int turn = returning ? 0 : this.init_aM().write(this.init_RangeSetting.get());
                  int optional = this.sync_I() / 2;
                  int back = returning && this.data_Z()
                     ? 0
                     : (
                        returning && this.init_aM().init_Z()
                           ? this.init_aM().init_I()
                           : (
                              this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.FORWARD
                                 ? this.init_aM().read(this.init_RangeSetting.get())
                                 : this.init_aM().init(this.init_RangeSetting.get(), this.poll_ce_DataUtil(), this.update_ce_DataUtil(), ce.init())
                           )
                     );
                  Predicate<BlockPos> solid = cell -> this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell);
                  double groundDrag = this.load_D_BlockPos(root);
                  if (this.init_ItemStack_2().getItem() instanceof BlockItem item) {
                     groundDrag = Math.max(groundDrag, item.getBlock().getFriction() * 0.91);
                  }

                  double powered = player.isSprinting() ? 0.026 : 0.0196;
                  double walkSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (player.isSprinting() ? 1.3 : 1.0);
                  double groundBrake = load_D_D_D(walkSpeed, groundDrag / 0.91) * 0.7;
                  BlockPos published = this.init_BlockPos_LocalPlayer(player, root, optional);
                  ScaffoldModule.Inner_PathHelper push = init(
                     player.position(),
                     player.getDeltaMovement(),
                     this.init_Direction(),
                     root,
                     this.f_L_I + 1.0,
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
                  if (this.f_A_I <= 1 && !returning) {
                     for (int ticks = 12; ticks > 1; ticks--) {
                        ScaffoldModule.Inner_PathHelper sustained = init(
                           player.position(),
                           player.getDeltaMovement(),
                           this.init_Direction(),
                           root,
                           this.f_L_I + 1.0,
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
                  this.f_Y_I = ck.init_ck().state_I();
                  this.init_aI_PathHelper = push;
                  this.buffer_Z = !push.init_Z();
                  this.init_aI_KeyHolder = push.init_Z() ? ScaffoldModule.Inner_KeyHolder.FORWARD : ScaffoldModule.Inner_KeyHolder.HOLD;
                  if (!push.init_Z()) {
                     ScaffoldModule.Inner_PathHelper coast = init(
                        player.position(),
                        player.getDeltaMovement(),
                        this.init_Direction(),
                        root,
                        this.f_L_I + 1.0,
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
                     this.init_aI_PathHelper = coast;
                     if (!coast.init_Z() && !(player.getDeltaMovement().dot(this.write_Vec3()) <= 0.0236)) {
                        ScaffoldModule.Inner_PathHelper brake = init(
                           player.position(),
                           player.getDeltaMovement(),
                           this.init_Direction(),
                           root,
                           this.f_L_I + 1.0,
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
                        this.entry_Z = brake.init_Z();
                     }
                  }
               }
            }
         }
      }
   }

   private BlockPos init_BlockPos_LocalPlayer(LocalPlayer player, BlockPos root, int optional) {
      if (!this.stop_Z && this.f_G_I <= 0) {
         ScaffoldModule.Inner_TextFormat wire = this.init_aI_TextFormat();
         if (wire != null && wire.init_I() < ck.init_ck().state_I()) {
            BlockHitResult ray = init_BlockHitResult_Vec3(player.getEyePosition(), wire.load(), player.blockInteractionRange() - 0.035, MC.level, player);
            if (ray != null && this.init_Z_BlockHitResult_3(ray)) {
               BlockPos placed = ray.getBlockPos().relative(ray.getDirection());
               boolean top = optional > 0 && ray.getDirection() == Direction.UP && placed.equals(this.load_BlockPos);
               boolean floor = optional == 0 && ray.getDirection() == this.init_Direction() && ray.getBlockPos().equals(root);
               if ((top || floor) && MC.level.getBlockState(placed).canBeReplaced()) {
                  ScaffoldModule.Inner_TableIndex target = new ScaffoldModule.Inner_TableIndex(ray.getBlockPos(), placed, ray.getDirection(), ray, wire.load(), placed.getY());
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

   static ScaffoldModule.Inner_PathHelper init(
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

   static ScaffoldModule.Inner_PathHelper init(
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

   static ScaffoldModule.Inner_PathHelper init(
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

   static ScaffoldModule.Inner_PathHelper init(
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
         load_D_D_D(0.1, groundDrag / 0.91) * 0.7
      );
   }

   static ScaffoldModule.Inner_PathHelper init(
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
            held = held.add(forward.scale(init_D_D_2(motion.dot(forward), groundDrag, groundWait, groundBrake)));
         }

         int distance = Math.max(0, (int)Math.ceil(held.dot(forward) - rootAlong - 0.68));
         if (distance > 6) {
            return new ScaffoldModule.Inner_PathHelper(false, distance, flight, flight, landing);
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
            return new ScaffoldModule.Inner_PathHelper(false, missing, flight, lastUse, landing);
         }

         int wait = Math.max(0, returnTicks - Math.max(0, flight - lastUse));
         if (wait <= groundWait) {
            return new ScaffoldModule.Inner_PathHelper(true, missing, flight, lastUse, landing);
         }

         groundWait = wait;
      }

      return new ScaffoldModule.Inner_PathHelper(false, missing, flight, lastUse, landing);
   }

   static double init_D_D_2(double velocity, double drag, int wait, double brake) {
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

      return travel + init_D_D(motion, friction, Math.max(0.0, brake));
   }

   static ScaffoldModule.Inner_ArrayUtil init_aI_ArrayUtil_Vec3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward, boolean brake) {
      Vec3 at = position;
      Vec3 motion = velocity;

      for (int tick = 0; tick < 14; tick++) {
         if (brake && tick == 0) {
            motion = motion.subtract(forward.scale(0.0196));
         }

         at = at.add(motion);
         Vec3 next = new Vec3(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91);
         if (motion.y <= 0.0 && at.y <= feetY + 1.0E-9) {
            return new ScaffoldModule.Inner_ArrayUtil(new Vec3(at.x, feetY, at.z), new Vec3(next.x, 0.0, next.z), tick + 1);
         }

         motion = next;
      }

      return new ScaffoldModule.Inner_ArrayUtil(new Vec3(at.x, feetY, at.z), motion, 14);
   }

   private boolean push(LocalPlayer player) {
      if (this.count_Z()
         || !this.push_Z
         || player.onGround()
         || !this.put_Z
         || this.pop_Z
         || this.init_aI_b.load()
         || this.start_Z()
         || this.next_Z()
         || this.queue_Z) {
         return false;
      }

      if (!this.clear_Z) {
         return false;
      }

      if (this.write_BlockPos != null && this.clear_Z && !this.start_Z) {
         this.buffer_Z = this.f_H_I >= this.f_E_I;
         this.init_aI_KeyHolder = this.buffer_Z ? ScaffoldModule.Inner_KeyHolder.HOLD : ScaffoldModule.Inner_KeyHolder.FORWARD;
         this.node_Z = true;
         this.f_X_I = Math.max(0, this.f_E_I - this.f_H_I);
         return false;
      }

      this.buffer_Z = true;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
      Vec3 forward = this.write_Vec3();
      Vec3 velocity = player.getDeltaMovement();
      if (velocity.dot(forward) <= 0.0196) {
         return false;
      }

      int rise = this.clear_Z ? 1 : 0;
      double feetY = this.f_L_I + 1.0 + rise;
      ScaffoldModule.Inner_ArrayUtil coast = init_aI_ArrayUtil_Vec3(player.position(), velocity, feetY, forward, false);
      if (coast.init_I() < 2) {
         return false;
      }

      ScaffoldModule.Inner_ArrayUtil eased = init_aI_ArrayUtil_Vec3(player.position(), velocity, feetY, forward, true);
      AABB easedBody = player.getBoundingBox().move(eased.init_Vec3().subtract(player.position()));
      BlockPos projected = this.init_BlockPos_BlockPos(BlockPos.containing(coast.init_Vec3().x, this.f_L_I, coast.init_Vec3().z)).above(rise);
      BlockPos base = projected.below(rise);
      BlockPos footing = this.close_BlockPos != null
         ? this.close_BlockPos
         : (
            this.update_BlockPos != null && this.update_BlockPos.getY() == projected.getY()
               ? this.update_BlockPos
               : init_BlockPos_BlockPos_2(projected, easedBody, this.init_Direction(), this.load_Z_BlockState_BlockPos(MC.level.getBlockState(base), base))
         );
      AABB landed = player.getBoundingBox().move(coast.init_Vec3().subtract(player.position()));
      if (this.init_Z_LocalPlayer_2(player, footing, coast.init_Vec3(), coast.load(), landed)) {
         return false;
      }

      boolean safe = eased.load().dot(forward) > 0.004 && init_Z_AABB_2(easedBody, footing, 0.12);
      if (safe) {
         this.update_BlockPos = footing.immutable();
      }

      return safe;
   }

   static BlockPos init_BlockPos_BlockPos_2(BlockPos projected, AABB easedBody, Direction course, boolean projectedFloorSolid) {
      BlockPos back = projected.relative(course.getOpposite());
      return !projectedFloorSolid && init_Z_AABB_2(easedBody, back, 0.12) ? back : projected;
   }

   static boolean load_Z_I(int flight, int missing, int turn, int finalFaceLead) {
      return missing >= 0 && missing <= 6 && flight >= Math.max(1, turn) + (missing + 1) * 2 - 1 && (missing == 0 || finalFaceLead >= 4);
   }

   private static boolean init_Z_Vec3_4(Vec3 position, Vec3 launchVelocity, double targetFeetY) {
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

   static ScaffoldModule.Inner_ValueSink init_aI_ValueSink_aI_ValueSink(ScaffoldModule.Inner_ValueSink current, boolean chainCovered, boolean risePending) {
      if (current == ScaffoldModule.Inner_ValueSink.RETURNING) {
         return ScaffoldModule.Inner_ValueSink.RETURNING;
      } else {
         return chainCovered && !risePending ? ScaffoldModule.Inner_ValueSink.RETURNING : current;
      }
   }

   private static int init_I_Vec3(Vec3 position, Vec3 velocity, double feetY, Vec3 forward, BlockPos support, double launchDrag) {
      if (position != null && velocity != null && support != null) {
         Vec3 landing = init_Vec3_Vec3_2(position, velocity, feetY, forward, launchDrag);
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

   static Vec3 load_Vec3_Vec3(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward) {
      return init_Vec3_Vec3_2(position, launchVelocity, feetY, forward, 0.546);
   }

   static Vec3 init_Vec3_Vec3_2(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward, double launchDrag) {
      return init_Vec3_Vec3_2(position, launchVelocity, feetY, forward, launchDrag, 0.0196);
   }

   static Vec3 init_Vec3_Vec3_2(Vec3 position, Vec3 launchVelocity, double feetY, Vec3 forward, double launchDrag, double airControlPerTick) {
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

   static float write_F_F(float yaw) {
      return Mth.wrapDegrees(Math.round(Mth.wrapDegrees(yaw) / 90.0F) * 90.0F);
   }

   static Direction init_Direction_D(double fracX, double fracZ, boolean northVoid, boolean southVoid, boolean westVoid, boolean eastVoid) {
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

   private Direction init_Direction_LocalPlayer(LocalPlayer player) {
      Vec3 pos = player.position();
      BlockPos cell = BlockPos.containing(pos.x, pos.y - 0.5, pos.z);
      if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell)) {
         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos neighbor = cell.relative(direction);
            if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(neighbor), neighbor)) {
               return direction.getOpposite();
            }
         }

         return null;
      } else {
         double fracX = pos.x - Math.floor(pos.x);
         double fracZ = pos.z - Math.floor(pos.z);
         return init_Direction_D(
            fracX,
            fracZ,
            !this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell.north()), cell.north()),
            !this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell.south()), cell.south()),
            !this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell.west()), cell.west()),
            !this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell.east()), cell.east())
         );
      }
   }

   private BlockPos init_BlockPos_BlockPos(BlockPos raw) {
      return this.sync_BlockPos == null ? raw : init_BlockPos_BlockPos(raw, this.sync_BlockPos, this.init_Direction());
   }

   static BlockPos init_BlockPos_BlockPos(BlockPos raw, BlockPos support, Direction direction) {
      return direction.getAxis() == Axis.Z ? new BlockPos(support.getX(), raw.getY(), raw.getZ()) : new BlockPos(raw.getX(), raw.getY(), support.getZ());
   }

   private BlockPos write_BlockPos() {
      if (this.sync_BlockPos == null) {
         return null;
      }

      if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(this.sync_BlockPos), this.sync_BlockPos)) {
         return this.sync_BlockPos;
      }

      Direction back = this.init_Direction().getOpposite();

      for (int step = 1; step <= 3; step++) {
         BlockPos candidate = this.sync_BlockPos.relative(back, step);
         if (this.load_Z_BlockState_BlockPos(MC.level.getBlockState(candidate), candidate)) {
            this.sync_BlockPos = candidate.immutable();
            return this.sync_BlockPos;
         }
      }

      return null;
   }

   static boolean init_Z_AABB_3(AABB currentBox, Vec3 writtenOffset, BlockPos cell) {
      return currentBox != null && writtenOffset != null && cell != null
         ? !init_Z_BlockPos_AABB_2(cell, currentBox) && !init_Z_BlockPos_AABB_2(cell, currentBox.move(writtenOffset))
         : false;
   }

   static boolean load_Z_AABB(AABB box, Vec3 velocity, BlockPos cell) {
      return init_Z_AABB_5(box, velocity, cell, false);
   }

   static boolean init_Z_AABB_5(AABB box, Vec3 velocity, BlockPos cell, boolean fallingCatch) {
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

   static String init_Str_AABB(AABB box, Vec3 velocity, BlockPos cell, boolean fallingCatch) {
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

   static ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil_2(ce.Inner_DataUtil current, ce.Inner_DataUtil goal, float yawCap, float pitchCap, double gcd) {
      if (current == null) {
         return goal;
      }

      if (goal == null) {
         return current;
      }

      ce.Inner_DataUtil stepped = ce.init(current, goal, yawCap, pitchCap);
      if (gcd <= 0.0) {
         return stepped;
      }

      float yawDiff = ce.init(stepped.init(), current.init());
      float pitchDiff = ce.init(stepped.load(), current.load());
      float yaw = current.init() + (float)(Math.round(yawDiff / gcd) * gcd);
      float pitch = current.load() + (float)(Math.round(pitchDiff / gcd) * gcd);
      return new ce.Inner_DataUtil(yaw, Mth.clamp(pitch, -90.0F, 90.0F));
   }

   static ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil current, ce.Inner_DataUtil goal, float stepCap, double gcd) {
      return init_ce_DataUtil_ce_DataUtil_2(current, goal, stepCap, stepCap, gcd);
   }

   static ce.Inner_DataUtil load_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil current, ce.Inner_DataUtil goal, float yawCap, float pitchCap, double gcd) {
      if (current == null) {
         return goal;
      } else {
         return goal == null ? current : ce.init(current, goal, yawCap, pitchCap, gcd);
      }
   }

   private static double read_D_D(double gcd) {
      return Double.isFinite(gcd) && gcd > 0.0 ? gcd : 0.15;
   }

   static boolean init_Z_F(float smoothedYaw, float anchorYaw, double velAlongCourse, double velCrossCourse, double laneError, boolean onGround) {
      if (!onGround) {
         return false;
      }

      if (Math.abs(ce.init(anchorYaw, smoothedYaw)) > 1.0F) {
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

   static float init_F_Z(boolean latched, float retainedYaw, float measuredTravelYaw) {
      return latched && Float.isFinite(retainedYaw) ? Mth.wrapDegrees(retainedYaw) : write_F_F(measuredTravelYaw);
   }

   static boolean load_Z_Z_2(boolean physicalForward, boolean physicalSpace, boolean physicalSpaceWasDown, boolean ownsInput) {
      return physicalForward && physicalSpace ? !physicalSpaceWasDown || !ownsInput : false;
   }

   static boolean init_Z_aI_ValueSink(ScaffoldModule.Inner_ValueSink phase) {
      return phase != null && phase != ScaffoldModule.Inner_ValueSink.IDLE;
   }

   private Vec3 write_Vec3() {
      return init_Vec3_F(this.flags_F);
   }

   static Vec3 init_Vec3_F(float anchorYaw) {
      float radians = anchorYaw * 0.017453292F;
      return new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
   }

   private Vec3 update_Vec3() {
      Vec3 forward = this.write_Vec3();
      return new Vec3(forward.z, 0.0, -forward.x);
   }

   private Direction init_Direction() {
      return init_Direction_F(this.flags_F);
   }

   static Direction init_Direction_F(float yaw) {
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

   static Vec3 init_Vec3_BlockPos(BlockPos support, Vec3 playerPosition, float yaw) {
      boolean alongZ = Math.floorMod(Math.round(yaw / 90.0F), 2) == 0;
      return alongZ ? new Vec3(support.getX() + 0.5, playerPosition.y, playerPosition.z) : new Vec3(playerPosition.x, playerPosition.y, support.getZ() + 0.5);
   }

   private static double init_D_Vec3_F(Vec3 position, float yaw) {
      float radians = yaw * 0.017453292F;
      Vec3 left = new Vec3(Mth.cos(radians), 0.0, Mth.sin(radians));
      return position.dot(left);
   }

   private ce.Inner_DataUtil update_ce_DataUtil() {
      return new ce.Inner_DataUtil(this.write_F(), this.level_F);
   }

   private ce.Inner_DataUtil sync_ce_DataUtil() {
      return new ce.Inner_DataUtil(Mth.wrapDegrees(this.flags_F + this.block_F), this.level_F);
   }

   private float write_F() {
      return Mth.wrapDegrees(this.flags_F + (this.count_Z() ? 180.0F : 0.0F));
   }

   private ce.Inner_DataUtil init_ce_DataUtil_LocalPlayer(LocalPlayer player) {
      if (this.size_Z) {
         if (this.prev_Z() && this.write_ce_DataUtil != null) {
            return this.write_ce_DataUtil;
         }

         this.size_Z = false;
      }

      if (this.prev_Z() && this.write_ce_DataUtil != null) {
         return this.write_ce_DataUtil;
      }

      ScaffoldModule.Inner_UtilCore goal = this.init_aI_UtilCore_LocalPlayer(player);
      if (goal.init_aI_a() == ScaffoldModule.Inner_a.RETURN) {
         return this.close_ce_DataUtil();
      }

      if (init_Z_aI_a(goal.init_aI_a())) {
         this.update_Z = false;
         this.data_Z = false;
         return this.load_ce_DataUtil_ce_DataUtil(goal.init_ce_DataUtil());
      }

      if (goal.init_aI_a() == ScaffoldModule.Inner_a.HOLD) {
         return this.flush_ce_DataUtil();
      }

      if (goal.init_aI_a() == ScaffoldModule.Inner_a.FORWARD && !player.onGround()) {
         return this.close_ce_DataUtil();
      }

      this.update_Z = true;
      return this.load_ce_DataUtil_ce_DataUtil(goal.init_ce_DataUtil());
   }

   static boolean init_Z_aI_a(ScaffoldModule.Inner_a intent) {
      return intent == ScaffoldModule.Inner_a.PLACEMENT;
   }

   private boolean prev_Z() {
      return this.f_T_I == ck.init_ck().state_I();
   }

   private ce.Inner_DataUtil flush_ce_DataUtil() {
      if (this.write_ce_DataUtil == null) {
         this.write_ce_DataUtil = this.poll_ce_DataUtil();
      }

      this.f_T_I = ck.init_ck().state_I();
      this.load_ce_DataUtil = this.write_ce_DataUtil;
      this.f_v_I = 5;
      return this.write_ce_DataUtil;
   }

   private ce.Inner_DataUtil load_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil goal) {
      return this.init_ce_DataUtil_ce_DataUtil_F(goal, this.update_F());
   }

   private ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil_F(ce.Inner_DataUtil goal, float cap) {
      ce.Inner_DataUtil planned = this.init_aI_CacheEntry != null && this.init_aI_CacheEntry.init_I() == ck.init_ck().state_I()
         ? this.init_aI_CacheEntry.init_aI_DataPipe().init_aI_TableIndex().init_ce_DataUtil()
         : this.init_aM().init(this.poll_ce_DataUtil(), goal, this.update_Z, this.init_RangeSetting.get(), ce.init(), ck.init_ck().state_I());
      this.write_ce_DataUtil = ce.init(this.poll_ce_DataUtil(), planned, cap, cap, ce.init());
      this.init_aM().init(this.write_ce_DataUtil, ck.init_ck().state_I());
      this.f_U_I = ck.init_ck().state_I();
      autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, this.write_ce_DataUtil);
      this.f_T_I = ck.init_ck().state_I();
      this.load_ce_DataUtil = this.write_ce_DataUtil;
      this.f_v_I = 5;
      return this.write_ce_DataUtil;
   }

   private void read_V_ce_DataUtil(ce.Inner_DataUtil rotation) {
      this.write_ce_DataUtil = rotation;
      this.init_aM().init(rotation, ck.init_ck().state_I());
      autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(this.init_ap_TaskQueue, rotation);
      this.load_ce_DataUtil = rotation;
      this.f_v_I = 5;
      this.f_T_I = ck.init_ck().state_I();
      this.size_Z = true;
      this.f_U_I = ck.init_ck().state_I();
      this.data_Z = false;
   }

   private ScaffoldModule.Inner_UtilCore init_aI_UtilCore_LocalPlayer(LocalPlayer player) {
      if (this.count_Z()) {
         return this.load_aI_UtilCore_LocalPlayer(player);
      }

      if (player.onGround()) {
         if (this.f_H_I > 0 && this.init_aI_DataPipe != null && this.init_BlockPos_LocalPlayer(player) == null) {
            ScaffoldModule.Inner_DataPipe catchTarget = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(player, this.init_aI_DataPipe);
            return new ScaffoldModule.Inner_UtilCore(
               catchTarget != null ? catchTarget.init_aI_TableIndex().init_ce_DataUtil() : this.poll_ce_DataUtil(),
               catchTarget != null ? ScaffoldModule.Inner_a.PLACEMENT : ScaffoldModule.Inner_a.HOLD
            );
         }

         if (this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.HOLD) {
            boolean turnReserveTarget = this.init_aI_DataPipe != null
               && this.push_BlockPos != null
               && this.push_BlockPos.equals(this.init_aI_DataPipe.init_aI_TableIndex().load());
            ScaffoldModule.Inner_DataPipe heldTarget = !read_Z_Z_Z(this.pop_Z, this.add_Z) && !turnReserveTarget ? null : this.init_aI_DataPipe;
            if (heldTarget != null) {
               return new ScaffoldModule.Inner_UtilCore(this.init_ce_DataUtil_LocalPlayer_aI_DataPipe(player, heldTarget), ScaffoldModule.Inner_a.PLACEMENT);
            }
         }

         return new ScaffoldModule.Inner_UtilCore(this.valid_Z && !this.init_aM().init_Z() ? this.sync_ce_DataUtil() : this.update_ce_DataUtil(), ScaffoldModule.Inner_a.FORWARD);
      } else {
         if (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING || this.add_Z) {
            return new ScaffoldModule.Inner_UtilCore(this.update_ce_DataUtil(), init_aI_a_aI_ValueSink(this.init_aI_ValueSink, false, this.add_Z, this.init_aI_NodeWalker));
         }

         if (this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.RETURNED) {
            return new ScaffoldModule.Inner_UtilCore(this.update_ce_DataUtil(), ScaffoldModule.Inner_a.FORWARD);
         }

         boolean aimingPhase = !this.add_Z && (this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.LAUNCH || this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.AIMING);
         if (aimingPhase && this.clear_Z && !this.start_Z && (this.init_aI_CacheEntry == null || !this.init_aI_CacheEntry.init_aI_DataPipe().init_Z())) {
            ScaffoldModule.Inner_DataPipe rise = this.load_aI_DataPipe_LocalPlayer_ItemStack(player, this.init_ItemStack_2());
            ScaffoldModule.Inner_UtilCore preview = rise == null ? null : this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, rise);
            if (preview != null) {
               return preview;
            }
         }

         if (this.init_aI_CacheEntry != null && this.init_aI_CacheEntry.init_I() < ck.init_ck().state_I()) {
            ScaffoldModule.Inner_TextFormat wire = this.init_aI_TextFormat();
            if (wire != null
               && init_Z_I_2(
                  this.init_aI_CacheEntry.init_I(),
                  ck.init_ck().state_I(),
                  wire.init_I(),
                  this.init_aI_CacheEntry.init_aI_DataPipe().init_aI_TableIndex().init_ce_DataUtil(),
                  wire.load()
               )) {
               return new ScaffoldModule.Inner_UtilCore(wire.load(), ScaffoldModule.Inner_a.HOLD);
            }
         }

         ScaffoldModule.Inner_DataPipe optional = aimingPhase && this.open_Z ? this.init_aI_DataPipe_LocalPlayer(player) : null;
         boolean windowDue = this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.PLACING || this.read_Z_LocalPlayer_I(player, this.sync_I());
         if (aimingPhase && windowDue && optional != null) {
            if (!this.init_Z_LocalPlayer_2(player, optional.init_aI_TableIndex().init_BlockPos(), true, false)) {
               this.buffer_Z = true;
               this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.HOLD;
               optional = this.init_aI_DataPipe_LocalPlayer(player);
            }

            ScaffoldModule.Inner_UtilCore preview = optional == null ? null : this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, optional);
            if (preview != null) {
               return preview;
            }
         }

         if (aimingPhase && windowDue && this.init_aI_DataPipe != null) {
            ScaffoldModule.Inner_UtilCore preview = this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, this.init_aI_DataPipe);
            if (preview != null) {
               return preview;
            }
         }

         ScaffoldModule.Inner_a gap = init_aI_a_aI_ValueSink(this.init_aI_ValueSink, false, this.add_Z, this.init_aI_NodeWalker);
         return new ScaffoldModule.Inner_UtilCore(gap == ScaffoldModule.Inner_a.HOLD && this.write_ce_DataUtil != null ? this.write_ce_DataUtil : this.update_ce_DataUtil(), gap);
      }
   }

   private ScaffoldModule.Inner_UtilCore load_aI_UtilCore_LocalPlayer(LocalPlayer player) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      if (this.init_aI_DataPipe != null
         && this.init_aI_DataPipe.init_aI_TableIndex().init_Direction().getAxis().isHorizontal()
         && this.init_aI_DataPipe.init_aI_TableIndex().init_Direction() != this.init_Direction()
         && !this.init_aI_DataPipe.init_aI_TableIndex().load().equals(this.push_BlockPos)
         && !this.next_Z()) {
         this.init_aI_DataPipe = null;
      }

      if (this.init_aI_CacheEntry != null) {
         ScaffoldModule.Inner_DataPipe prepared = this.init_aI_CacheEntry.init_aI_DataPipe();
         boolean current = this.init_aI_DataPipe != null && prepared.init_aI_TableIndex().load().equals(this.init_aI_DataPipe.init_aI_TableIndex().load());
         boolean stillVisible = this.init_aI_CacheEntry.init_I() == ck.init_ck().state_I()
            || this.init_aI_TableIndex_aI_TableIndex(prepared.init_aI_TableIndex()) != null;
         if (!stillVisible || !current && (!prepared.init_Z() || !this.open_Z(player))) {
            this.init_aI_CacheEntry = null;
            this.close_Z = false;
         }
      }

      if (this.init_aI_CacheEntry != null && this.init_aI_CacheEntry.init_I() < ck.init_ck().state_I()) {
         ScaffoldModule.Inner_TextFormat wire = this.init_aI_TextFormat();
         if (wire != null
            && init_Z_I_2(
               this.init_aI_CacheEntry.init_I(),
               ck.init_ck().state_I(),
               wire.init_I(),
               this.init_aI_CacheEntry.init_aI_DataPipe().init_aI_TableIndex().init_ce_DataUtil(),
               wire.load()
            )) {
            return new ScaffoldModule.Inner_UtilCore(wire.load(), ScaffoldModule.Inner_a.HOLD);
         }
      }

      ScaffoldModule.Inner_DataPipe target = !player.onGround() && this.open_Z(player)
         ? this.load_aI_DataPipe_LocalPlayer_ItemStack(player, this.init_ItemStack_2())
         : null;
      if (target == null) {
         target = this.init_aI_DataPipe;
      }

      if (target != null) {
         ScaffoldModule.Inner_UtilCore prepared = this.init_aI_UtilCore_LocalPlayer_aI_DataPipe(player, target);
         if (prepared != null) {
            return prepared;
         }
      }

      return this.data_Z()
         ? new ScaffoldModule.Inner_UtilCore(this.write_ce_DataUtil, ScaffoldModule.Inner_a.HOLD)
         : new ScaffoldModule.Inner_UtilCore(new ce.Inner_DataUtil(this.flags_F + 180.0F, 82.0F), ScaffoldModule.Inner_a.PLACEMENT);
   }

   static ScaffoldModule.Inner_a init_aI_a_aI_ValueSink(ScaffoldModule.Inner_ValueSink phase, boolean placementGoal, boolean finishing, ScaffoldModule.Inner_NodeWalker lookPhase) {
      if (finishing || phase == ScaffoldModule.Inner_ValueSink.RETURNING) {
         return ScaffoldModule.Inner_a.RETURN;
      } else if (lookPhase == ScaffoldModule.Inner_NodeWalker.RETURNED) {
         return ScaffoldModule.Inner_a.FORWARD;
      } else if (placementGoal) {
         return ScaffoldModule.Inner_a.PLACEMENT;
      } else {
         return lookPhase == ScaffoldModule.Inner_NodeWalker.PLACING ? ScaffoldModule.Inner_a.HOLD : ScaffoldModule.Inner_a.FORWARD;
      }
   }

   private boolean pop(LocalPlayer player) {
      return this.read_Z_LocalPlayer_I(player, this.sync_I());
   }

   private boolean read_Z_LocalPlayer_I(LocalPlayer player, int optionalIntervals) {
      if (this.next_Z()) {
         return true;
      }

      BlockPos root = this.write_BlockPos();
      if (root == null) {
         return true;
      }

      boolean rise = this.clear_Z && !this.start_Z && this.open_Z(player);
      int blockY = this.start_Z ? this.f_L_I + 1 : this.f_L_I;
      double feetY = blockY + (rise ? 2.0 : 1.0);
      Vec3 landing = init_Vec3_Vec3_3(player.position(), player.getDeltaMovement(), feetY, this.write_Vec3());
      BlockPos cell = this.init_BlockPos_BlockPos(BlockPos.containing(landing.x, blockY, landing.z));
      Direction direction = this.init_Direction();
      int distance = Math.max(0, (cell.getX() - root.getX()) * direction.getStepX() + (cell.getZ() - root.getZ()) * direction.getStepZ());
      int missing = 0;

      for (int step = 1; step <= Math.min(8, distance); step++) {
         BlockPos needed = root.relative(direction, step);
         if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(needed), needed)) {
            missing++;
         }
      }

      return init_I_Vec3(player.position(), player.getDeltaMovement(), feetY)
         <= init_I_I_2(missing, rise, this.init_aM().load(this.init_RangeSetting.get()), optionalIntervals, this.update_I());
   }

   private int update_I() {
      return init_IArr[Mth.clamp(this.integer("turn-delay"), 0, next_I)];
   }

   static int init_I_I_2(int missing, boolean rise, int turnTicks, int optionalIntervals, int delay) {
      int links = Math.max(1, missing + (rise ? 1 : 0));
      int minimum = links * 2 + Math.max(1, turnTicks) + optionalIntervals;
      int preferred = links * 2 + 6 - delay + Math.max(1, turnTicks) - 2 + optionalIntervals;
      return Math.max(minimum, preferred);
   }

   private float update_F() {
      float cap = this.init_aM().init(this.init_RangeSetting.get(), this.update_Z);
      return this.count_Z() ? Math.min(18.0F, cap) : cap;
   }

   private ScaffoldRotationSmoother init_aM() {
      if (this.init_aM == null) {
         this.init_aM = ScaffoldRotationSmoother.init_aM();
      }

      return this.init_aM;
   }

   private ScaffoldRotationSmoother.Inner_DataUtil init_aM_DataUtil() {
      ValueRange band = this.init_RangeSetting.get();
      if (this.init_aM_DataUtil == null
         || this.init_aM_DataUtil.init_D() < band.min()
         || this.init_aM_DataUtil.init_D() > band.max()
         || this.init_aM_DataUtil.load_D() < band.min()
         || this.init_aM_DataUtil.load_D() > band.max()) {
         this.init_aM_DataUtil = this.init_aM().init_aM_DataUtil_ValueRange(band);
      }

      return this.init_aM_DataUtil;
   }

   private int sync_I() {
      return this.open_Z && !this.clear_Z && !this.put_Z && !this.next_Z() ? this.init_aN.init_I() * 2 : 0;
   }

   private int flush_I() {
      return !this.count_Z() && !this.put_Z && !this.set_Z ? this.init_aN.init(this.integer("imperfection"), this.init_Random) : 0;
   }

   private boolean write_Z_LocalPlayer_I(LocalPlayer player, int blockY) {
      if (this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.FORWARD && !this.start_Z) {
         BlockPos root = this.write_BlockPos();
         if (root == null) {
            return true;
         }

         double feetY = blockY + 1.0;
         Vec3 landing = this.init_aI_FieldCache_LocalPlayer_D(player, feetY).init_Vec3();
         BlockPos cell = this.init_BlockPos_BlockPos(BlockPos.containing(landing.x, blockY, landing.z));
         Direction direction = this.init_Direction();
         int distance = Math.max(0, (cell.getX() - root.getX()) * direction.getStepX() + (cell.getZ() - root.getZ()) * direction.getStepZ());
         int missing = 0;

         for (int step = 1; step <= Math.min(8, distance); step++) {
            BlockPos needed = root.relative(direction, step);
            if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(needed), needed)) {
               missing++;
            }
         }

         int turnTicks = this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.FORWARD
            ? this.init_aM().load(this.init_RangeSetting.get())
            : (this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.PLACING ? this.init_aM().write(this.init_RangeSetting.get()) : 0);
         int remaining = init_I_Vec3(player.position(), player.getDeltaMovement(), feetY);
         int needed = init_I_I_3(missing, this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.FORWARD, turnTicks, this.update_I(), !this.count_Z());
         return missing > 0 && remaining <= needed + this.sync_I();
      } else {
         return false;
      }
   }

   static int init_I_I_3(int missing, boolean forward, int turnRemaining, int delay) {
      return init_I_I_3(missing, forward, turnRemaining, delay, false);
   }

   static int init_I_I_3(int missing, boolean forward, int turnRemaining, int delay, boolean reserveFaceVisibility) {
      int look = forward ? Math.max(1, turnRemaining) : Math.max(0, turnRemaining - (reserveFaceVisibility ? 0 : 2));
      int required = Math.max(0, missing) * (reserveFaceVisibility ? 3 : 2) + look;
      return Math.max(required, required + 1 - delay);
   }

   static boolean init_Z_I_5(int remainingFlightTicks, int missingBlocks, boolean rise, int delayTicks) {
      return remainingFlightTicks <= Math.max(1, missingBlocks + (rise ? 1 : 0)) * 2 + 6 - delayTicks;
   }

   private ScaffoldModule.Inner_UtilCore init_aI_UtilCore_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.Inner_DataPipe placement) {
      if (!this.run_Z && this.init_aI_NodeWalker != ScaffoldModule.Inner_NodeWalker.RETURNED) {
         Vec3 motion = player.getDeltaMovement().add(this.read_Vec3().scale(this.count_Z() && player.onGround() && this.read_BlockPos != null ? 0.0 : 0.0196));
         Vec3 moved = this.init_Vec3_LocalPlayer(player, player.getBoundingBox(), motion);
         Vec3 eye = player.getEyePosition().add(moved);
         ScaffoldModule.Inner_DataPipe future = this.init_aI_DataPipe_LocalPlayer(player, placement, eye, player.getBoundingBox().move(moved));
         if (future == null) {
            return null;
         }

         if (!AutismLiteVariant.enabled()
            && !this.count_Z()
            && !this.clear_Z
            && !this.put_Z
            && !this.valid_Z()
            && future.init_aI_TableIndex().init_Direction().getAxis().isHorizontal()
            && init_I_Vec3(player.position(), player.getDeltaMovement(), this.f_L_I + 1.0) <= 2) {
            Vec3 nextEye = new Vec3(
               eye.x + motion.x * 0.91, Math.max(this.f_L_I + 1.0 + player.getEyeHeight(), eye.y + (motion.y - 0.08) * 0.98), eye.z + motion.z * 0.91
            );
            ScaffoldModule.Inner_TableIndex target = future.init_aI_TableIndex();
            ce.Inner_DataUtil carry = init_ce_DataUtil_BlockPos_2(
               target.init_BlockPos(),
               target.init_Direction(),
               eye,
               nextEye,
               this.poll_ce_DataUtil(),
               target.init_ce_DataUtil(),
               this.flags_F,
               ce.init(),
               player.blockInteractionRange() - 0.035
            );
            if (carry != null && this.init_Z_Vec3(eye, carry, target.init_BlockPos(), target.init_Direction(), player.blockInteractionRange())) {
               BlockHitResult hit = init_BlockHitResult_Vec3(eye, carry, player.blockInteractionRange(), MC.level, player);
               future = new ScaffoldModule.Inner_DataPipe(
                  new ScaffoldModule.Inner_TableIndex(target.init_BlockPos(), target.load(), target.init_Direction(), hit, carry, target.init_D()), future.init_Z()
               );
            }
         }

         ScaffoldModule.Inner_TableIndex quantized = this.init_aI_TableIndex_aI_TableIndex_Vec3(future.init_aI_TableIndex(), eye);
         if (quantized == null) {
            this.init_aI_CacheEntry = null;
            this.close_Z = false;
            this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.PLACING;
            return new ScaffoldModule.Inner_UtilCore(future.init_aI_TableIndex().init_ce_DataUtil(), ScaffoldModule.Inner_a.PLACEMENT);
         }

         if (!this.count_Z()
            && !player.onGround()
            && !this.clear_Z
            && !this.put_Z
            && !this.pop_Z
            && !this.init_aI_b.load()
            && !this.start_Z()
            && this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.FORWARD
            && !this.entry_Z
            && quantized.init_Direction().getAxis().isHorizontal()) {
            boolean stable = init_Z_F(
               this.flags_F,
               quantized.init_ce_DataUtil().init(),
               0.0196 * (player.isCrouching() ? 0.3 : 1.0),
               force -> {
                  Vec3 actual = this.init_Vec3_LocalPlayer(player, player.getBoundingBox(), player.getDeltaMovement().add(force));
                  return this.init_Z_Vec3(
                     player.getEyePosition().add(actual),
                     quantized.init_ce_DataUtil(),
                     quantized.init_BlockPos(),
                     quantized.init_Direction(),
                     player.blockInteractionRange()
                  );
               }
            );
            if (!stable) {
               this.init_aI_CacheEntry = null;
               this.close_Z = false;
               return new ScaffoldModule.Inner_UtilCore(this.write_ce_DataUtil != null ? this.write_ce_DataUtil : this.poll_ce_DataUtil(), ScaffoldModule.Inner_a.HOLD);
            }
         }

         this.init_aI_CacheEntry = new ScaffoldModule.Inner_CacheEntry(new ScaffoldModule.Inner_DataPipe(quantized, placement.init_Z()), ck.init_ck().state_I());
         this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.PLACING;
         return new ScaffoldModule.Inner_UtilCore(quantized.init_ce_DataUtil(), ScaffoldModule.Inner_a.PLACEMENT);
      } else {
         return null;
      }
   }

   static ce.Inner_DataUtil init_ce_DataUtil_BlockPos_2(
      BlockPos support, Direction face, Vec3 eye, Vec3 nextEye, ce.Inner_DataUtil from, ce.Inner_DataUtil normal, float course, double gcd, double reach
   ) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      AABB box = new AABB(support);
      Vec3 ray = Vec3.directionFromRotation(normal.load(), normal.init()).scale(reach);
      if (init_Z_Vec3_3(box.clip(eye, eye.add(ray)).orElse(null), box, face) && init_Z_Vec3_3(box.clip(nextEye, nextEye.add(ray)).orElse(null), box, face)) {
         return null;
      }

      double side = ce.init(normal.init(), course);
      ce.Inner_DataUtil best = null;
      double cost = 1.0 / 0.0;

      for (ClutchBlockPlanner.Inner_StreamUtil counts : ClutchBlockPlanner.init_List_Vec3_2(
         eye, nextEye, ClutchBlockPlanner.init(support, face), support, face, support, face, reach, from.init(), from.load(), gcd, 0.02
      )) {
         ce.Inner_DataUtil look = new ce.Inner_DataUtil(from.init() + ce.init(counts.init(), from.init()), counts.load());
         if (!(side * ce.init(look.init(), course) <= 0.0)) {
            double travel = Math.hypot(ce.init(look.init(), from.init()), look.load() - from.load());
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
            Input keys = init_Input_I_Input(octant, new Input(true, false, false, false, false, false, true));
            Vec3 local = new Vec3(init_F_Z_Z(keys.left(), keys.right()), 0.0, init_F_Z_Z(keys.forward(), keys.backward())).normalize();
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

   static boolean init_Z_I_2(int preparedTick, int currentTick, int writtenTick, ce.Inner_DataUtil prepared, ce.Inner_DataUtil written) {
      return prepared != null
         && written != null
         && preparedTick < currentTick
         && writtenTick < currentTick
         && Math.abs(ce.init(prepared.init(), written.init())) <= 0.002F
         && Math.abs(prepared.load() - written.load()) <= 0.002F;
   }

   static double init_D_Vec3_6(Vec3 from, Vec3 to, Vec3 eye, Direction side) {
      return init_D_Vec3(from, to, eye, side, eye);
   }

   static double init_D_Vec3(Vec3 from, Vec3 to, Vec3 eye, Direction side, Vec3 laneReference) {
      if (side.getAxis() == Axis.Z) {
         double edge = laneReference.x >= (from.x + to.x) * 0.5 ? from.x : to.x;
         return (eye.x - edge) / side.getStepZ();
      } else {
         double edge = laneReference.z >= (from.z + to.z) * 0.5 ? from.z : to.z;
         return (edge - eye.z) / side.getStepX();
      }
   }

   private Vec3 init_Vec3_Vec3_2(Vec3 eye) {
      Vec3 left = this.update_Vec3();
      return eye.add(left.scale(this.f_x_D - eye.dot(left)));
   }

   static Vec3 load_Vec3_BlockPos(BlockPos support, Vec3 position, float yaw) {
      Direction course = init_Direction_F(yaw);
      return course.getAxis() == Axis.Z
         ? new Vec3(support.getX() + (position.x - support.getX() < 0.5 ? 0.25 : 0.75), position.y, position.z)
         : new Vec3(position.x, position.y, support.getZ() + (position.z - support.getZ() < 0.5 ? 0.25 : 0.75));
   }

   private Vec3 read_Vec3_BlockPos(BlockPos support, Vec3 position, float yaw) {
      return this.count_Z() ? init_Vec3_BlockPos(support, position, yaw) : load_Vec3_BlockPos(support, position, yaw);
   }

   static Vec3 init_Vec3_AABB(AABB support, Vec3 eye, Vec3 forward) {
      return init_Vec3_AABB(support, eye, forward, eye);
   }

   static Vec3 init_Vec3_AABB(AABB support, Vec3 eye, Vec3 forward, Vec3 laneReference) {
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

   private void flags_V() {
      this.f_A_I = 0;
      this.open_Z = false;
      this.poll_Z = false;
      this.load_BlockPos = null;
      this.load_List = List.of();
   }

   private void level_V() {
      this.open_Z = false;
      if (this.init_aI_CacheEntry != null
         && this.load_BlockPos != null
         && this.load_BlockPos.equals(this.init_aI_CacheEntry.init_aI_DataPipe().init_aI_TableIndex().load())) {
         this.init_aI_CacheEntry = null;
         this.close_Z = false;
      }

      if (!this.poll_Z) {
         this.load_BlockPos = null;
      }
   }

   private void open_V(LocalPlayer player) {
      if (this.put_Z || this.set_Z) {
         this.level_V();
      }
   }

   static boolean init_Z_aI_ValueSink(ScaffoldModule.Inner_ValueSink phase, ScaffoldModule.Inner_NodeWalker look, int remaining, int turning) {
      return (phase == ScaffoldModule.Inner_ValueSink.LAUNCH || phase == ScaffoldModule.Inner_ValueSink.AIMING)
         && look != ScaffoldModule.Inner_NodeWalker.RETURNED
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
      return (this.open_Z || this.poll_Z)
         && this.init_aN.init_Z()
         && !this.clear_Z
         && !this.put_Z
         && !this.next_Z()
         && !this.start_Z()
         && player != null
         && !player.onGround()
         && this.init_aI_NodeWalker != ScaffoldModule.Inner_NodeWalker.RETURNED
         && init_I_Vec3(player.position(), player.getDeltaMovement(), this.f_L_I + 1.0) >= this.init_aN.init_I() * 2;
   }

   private ScaffoldModule.Inner_DataPipe init_aI_DataPipe_LocalPlayer(LocalPlayer player) {
      if (this.open_Z && this.tick(player)) {
         boolean deferredPastArc = false;
         Vec3 motion = player.getDeltaMovement().add(this.read_Vec3().scale(0.0196));
         Vec3 moved = this.init_Vec3_LocalPlayer(player, player.getBoundingBox(), motion);
         Vec3 eye = player.getEyePosition().add(moved);
         Vec3 following = eye.add(motion.x * 0.91, (motion.y - 0.08) * 0.98, motion.z * 0.91).add(this.read_Vec3().scale(0.0196));

         for (BlockPos support : this.load_List) {
            if (this.init_Z_LocalPlayer_5(player, support, true)) {
               ce.Inner_DataUtil current = this.poll_ce_DataUtil();
               BlockHitResult carried = init_BlockHitResult_Vec3(eye, current, player.blockInteractionRange() - 0.035, MC.level, player);
               Vec3 point = init_Vec3_AABB(new AABB(support), eye, this.write_Vec3(), this.init_Vec3_Vec3_2(eye));
               boolean holds = carried != null
                  && carried.getBlockPos().equals(support)
                  && carried.getDirection() == Direction.UP
                  && this.init_Z_BlockHitResult_3(carried)
                  && (!this.valid_Z() || Math.abs(ce.init(current.init(), this.flags_F)) >= 171.0F);
               ce.Inner_DataUtil rotation = holds
                  ? current
                  : (
                     point == null
                        ? null
                        : init_ce_DataUtil_BlockPos(
                           support,
                           this.init_Direction(),
                           eye,
                           following,
                           ce.init(point, eye),
                           this.poll_ce_DataUtil(),
                           ce.init(),
                           player.blockInteractionRange() - 0.035,
                           this.init_aI_DataPipe != null
                              && support.equals(this.init_aI_DataPipe.init_aI_TableIndex().init_BlockPos())
                              && this.init_aI_DataPipe.init_aI_TableIndex().init_Direction() == this.init_Direction()
                        )
                  );
               if (!holds && point != null && this.valid_Z()) {
                  rotation = this.init_ce_DataUtil_BlockPos(support, Direction.UP, eye, current, ce.init(point, eye));
               }

               if (rotation != null) {
                  if (this.f_A_I <= 1 && !init_Z_F(this.flags_F, current.init(), rotation.init(), this.f_I_I, ce.init())) {
                     deferredPastArc = true;
                  } else {
                     BlockHitResult ray = init_BlockHitResult_Vec3(eye, rotation, player.blockInteractionRange() - 0.035, MC.level, player);
                     if (ray != null && ray.getDirection() == Direction.UP && ray.getBlockPos().equals(support) && this.init_Z_BlockHitResult_3(ray)) {
                        this.load_BlockPos = support.above().immutable();
                        return new ScaffoldModule.Inner_DataPipe(
                           new ScaffoldModule.Inner_TableIndex(support.immutable(), this.load_BlockPos, Direction.UP, ray, rotation, support.getY()), false
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
      return Math.abs(ce.init(target, course)) <= Math.abs(ce.init(current, course)) + tolerance;
   }

   private boolean valid_Z() {
      return !this.count_Z() && this.f_A_I == 2 && this.open_Z && this.init_aN.init_Z() && !this.clear_Z && !this.put_Z && !this.next_Z();
   }

   private boolean size_Z() {
      return this.valid_Z() || !this.count_Z() && this.write_BlockPos != null && this.clear_Z && !this.start_Z && !this.next_Z();
   }

   private double init_D_8() {
      return this.write_BlockPos != null && this.clear_Z ? 150.0 : 172.0;
   }

   private ce.Inner_DataUtil init_ce_DataUtil_BlockPos(BlockPos support, Direction face, Vec3 eye, ce.Inner_DataUtil from, ce.Inner_DataUtil normal) {
      double side = ce.init(normal.init(), this.flags_F);
      double prior = ce.init(from.init(), this.flags_F);
      double rear = Math.max(this.init_D_8(), prior * side > 0.0 ? Math.abs(prior) : 0.0);
      if (this.write_BlockPos != null && this.clear_Z) {
         return init_ce_DataUtil_AABB(
            init_AABB_BlockPos(support),
            face,
            eye,
            from,
            this.flags_F,
            side,
            rear,
            ce.init(),
            MC.player.blockInteractionRange() - 0.035,
            look -> this.init_Z_Vec3(eye, look, support, face, MC.player.blockInteractionRange())
         );
      }

      float yaw = this.flags_F + (float)Math.copySign(rear, side);
      return init_ce_DataUtil_AABB(
         init_AABB_BlockPos(support),
         face,
         eye,
         from,
         yaw,
         ce.init(),
         MC.player.blockInteractionRange() - 0.035,
         look -> this.init_Z_Vec3(eye, look, support, face, MC.player.blockInteractionRange())
      );
   }

   static ce.Inner_DataUtil init_ce_DataUtil_AABB(
      AABB box,
      Direction face,
      Vec3 eye,
      ce.Inner_DataUtil from,
      float course,
      double side,
      double minimumRear,
      double gcd,
      double reach,
      Predicate<ce.Inner_DataUtil> valid
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

      for (double step = Math.max(0.3, read_D_D(gcd) * 1.01); rear <= 178.5; rear += step) {
         float yaw = course + (float)Math.copySign(rear, side);
         ce.Inner_DataUtil look = init_ce_DataUtil_AABB(box, face, eye, from, yaw, gcd, reach, valid);
         if (look != null) {
            return look;
         }
      }

      return null;
   }

   static ce.Inner_DataUtil init_ce_DataUtil_AABB(
      AABB box, Direction face, Vec3 eye, ce.Inner_DataUtil from, float yaw, double gcd, double reach, Predicate<ce.Inner_DataUtil> valid
   ) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      ce.Inner_DataUtil bearing = load_ce_DataUtil_ce_DataUtil(from, new ce.Inner_DataUtil(yaw, from.load()), 180.0F, 180.0F, gcd);
      Vec3 horizontal = Vec3.directionFromRotation(0.0F, bearing.init());
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

      ce.Inner_DataUtil best = null;
      double cost = 1.0 / 0.0;

      for (Vec3 point : points) {
         ce.Inner_DataUtil desired = new ce.Inner_DataUtil(bearing.init(), ce.init(point, eye).load());
         ce.Inner_DataUtil look = load_ce_DataUtil_ce_DataUtil(from, desired, 180.0F, 180.0F, gcd);
         Vec3 hit = box.clip(eye, eye.add(Vec3.directionFromRotation(look.load(), look.init()).scale(reach))).orElse(null);
         if (init_Z_Vec3_3(hit, box, face) && valid.test(look)) {
            double travel = Math.hypot(ce.init(look.init(), from.init()), look.load() - from.load());
            if (travel < cost) {
               cost = travel;
               best = look;
            }
         }
      }

      return best;
   }

   static ce.Inner_DataUtil init_ce_DataUtil_BlockPos(
      BlockPos support, Direction forward, Vec3 eye, Vec3 nextEye, ce.Inner_DataUtil from, double gcd, double reach
   ) {
      if (support != null && forward != null && !forward.getAxis().isVertical() && eye != null) {
         Vec3 point = init_Vec3_AABB(new AABB(support), eye, Vec3.atLowerCornerOf(forward.getUnitVec3i()));
         return point == null ? null : init_ce_DataUtil_BlockPos(support, forward, eye, nextEye, ce.init(point, eye), from, gcd, reach);
      } else {
         return null;
      }
   }

   private boolean push(LocalPlayer player, BlockPos support) {
      return this.init_Z_LocalPlayer_5(player, support, false);
   }

   private boolean init_Z_LocalPlayer_5(LocalPlayer player, BlockPos support, boolean preparing) {
      if (this.open_Z
         && this.integer("imperfection") > 0
         && !player.onGround()
         && !this.clear_Z
         && !this.put_Z
         && !this.pop_Z
         && !this.init_aI_b.load()
         && !this.add_Z
         && !this.cache_Z
         && !this.queue_Z
         && !this.next_Z()
         && !this.start_Z()
         && support != null
         && support.getY() >= this.f_L_I
         && support.getY() <= this.f_L_I + 1
         && this.init_aI_NodeWalker != ScaffoldModule.Inner_NodeWalker.RETURNED
         && !(player.getDeltaMovement().dot(this.write_Vec3()) <= 1.0E-4)) {
         BlockPos extra = support.above();
         BlockState state = MC.level.getBlockState(support);
         if (!MC.level.isOutsideBuildHeight(extra)
            && MC.level.getBlockState(extra).isAir()
            && this.load_Z_BlockState_BlockPos(state, support)
            && state.isCollisionShapeFullBlock(MC.level, support)
            && !autismclient.util.U.init_Z(state, support)
            && init_Z_BlockPos_2(support, this.f_aN_I, this.flush_ArrayDeque)) {
            Vec3 forward = this.write_Vec3();
            AABB body = player.getBoundingBox();
            Vec3 velocity = player.getDeltaMovement();
            if (!init_Z_AABB_6(body, velocity, forward, extra)) {
               return false;
            }

            if (preparing) {
               Vec3 move = velocity.add(this.read_Vec3().scale(0.0196));
               body = body.move(this.init_Vec3_LocalPlayer(player, body, move));
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
      BlockPos root = this.write_BlockPos();
      if (root == null) {
         return false;
      }

      if (!this.count_Z()
         && this.f_A_I <= 1
         && !coast
         && this.f_Y_I == ck.init_ck().state_I()
         && this.init_aI_PathHelper != null
         && this.init_aI_PathHelper.init_Z()) {
         return this.read_I_I(player.getInventory().getSelectedSlot()) >= this.init_aI_PathHelper.init_I() + this.init_aN.init_I() + 1;
      }

      Vec3 landing = coast
         ? init_aI_FieldCache_Vec3(
               player.position(),
               player.getDeltaMovement(),
               this.f_L_I + 1.0,
               Vec3.ZERO,
               0.91,
               player.getBoundingBox(),
               false,
               (box, move) -> this.init_Vec3_LocalPlayer(player, box, move)
            )
            .init_Vec3()
         : this.init_aI_FieldCache_LocalPlayer_D(player, this.f_L_I + 1.0).init_Vec3();
      BlockPos cell = this.init_BlockPos_BlockPos(BlockPos.containing(landing.x, this.f_L_I, landing.z));
      Direction direction = this.init_Direction();
      int missing = init_I_BlockPos(root, cell, direction, at -> this.load_Z_BlockState_BlockPos(MC.level.getBlockState(at), at));
      int remaining = init_I_Vec3(player.position(), player.getDeltaMovement(), this.f_L_I + 1.0);
      int turning = !preparing
         ? 0
         : (
            this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.PLACING
               ? this.init_aM().write(this.init_RangeSetting.get())
               : this.init_aM().load(this.init_RangeSetting.get())
         );
      boolean carried = false;
      if (!preparing && this.init_aI_CacheEntry != null) {
         Vec3 motion = player.getDeltaMovement().add(this.read_Vec3().scale(0.0196));
         Vec3 eye = player.getEyePosition().add(this.init_Vec3_LocalPlayer(player, player.getBoundingBox(), motion));
         BlockHitResult side = init_BlockHitResult_Vec3(
            eye, this.init_aI_CacheEntry.init_aI_DataPipe().init_aI_TableIndex().init_ce_DataUtil(), player.blockInteractionRange() - 0.035, MC.level, player
         );
         carried = side != null && side.getBlockPos().equals(support) && side.getDirection() == this.init_Direction() && this.init_Z_BlockHitResult_3(side);
      }

      return init_Z_I(
         remaining + (carried ? 2 : 0), missing, turning, this.read_I_I(player.getInventory().getSelectedSlot()), this.init_aN.init_I(), !preparing
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

   static ce.Inner_DataUtil init_ce_DataUtil_BlockPos(
      BlockPos support, Direction side, Vec3 eye, Vec3 nextEye, ce.Inner_DataUtil normal, ce.Inner_DataUtil from, double gcd, double reach
   ) {
      return init_ce_DataUtil_BlockPos(support, side, eye, nextEye, normal, from, gcd, reach, true);
   }

   static ce.Inner_DataUtil init_ce_DataUtil_BlockPos(
      BlockPos support,
      Direction side,
      Vec3 eye,
      Vec3 nextEye,
      ce.Inner_DataUtil normal,
      ce.Inner_DataUtil from,
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
         ce.Inner_DataUtil best = null;
         double cost = 1.0 / 0.0;
         boolean bestCarries = false;

         for (int along = 0; along < 7; along++) {
            for (int across = 0; across < 7; across++) {
               double lane = 0.12 + across * 0.12666666666666668;
               double axis = 0.12 + along * 0.12666666666666668;
               Vec3 point = new Vec3(
                  support.getX() + (side.getAxis() == Axis.X ? axis : lane), box.maxY, support.getZ() + (side.getAxis() == Axis.Z ? axis : lane)
               );
               ce.Inner_DataUtil rotation = load_ce_DataUtil_ce_DataUtil(from, ce.init(point, eye), 180.0F, 180.0F, read_D_D(gcd));
               float courseYaw = side.toYRot();
               double normalSide = ce.init(normal.init(), courseYaw);
               double candidateSide = ce.init(rotation.init(), courseYaw);
               if (!(normalSide * candidateSide <= 0.0)) {
                  double yaw = Math.abs(ce.init(rotation.init(), normal.init()));
                  double pitch = Math.abs(rotation.load() - normal.load());
                  if (!(yaw > 18.0) && !(pitch > 24.0)) {
                     Vec3 look = Vec3.directionFromRotation(rotation.load(), rotation.init()).scale(reach);
                     Vec3 topHit = box.clip(eye, eye.add(look)).orElse(null);
                     Vec3 sideHit = box.clip(nextEye, nextEye.add(look)).orElse(null);
                     if (init_Z_Vec3_3(topHit, box, Direction.UP)) {
                        boolean carries = nextSideRequired && init_Z_Vec3_3(sideHit, box, side);
                        double candidate = Math.hypot(ce.init(rotation.init(), from.init()), rotation.load() - from.load());
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

   private ScaffoldModule.Inner_DataPipe load_aI_DataPipe_LocalPlayer_ItemStack(LocalPlayer player, ItemStack stack) {
      if (player != null && this.sync_BlockPos != null && this.open_Z(player) && this.init_Z_ItemStack_4(stack)) {
         BlockPos riseCell = this.load_BlockPos_LocalPlayer(player);
         return riseCell == null ? null : this.load_aI_DataPipe_LocalPlayer(player, stack, riseCell);
      } else {
         return null;
      }
   }

   static boolean read_Z_Z_Z(boolean stopRequested, boolean finishing) {
      return stopRequested || finishing;
   }

   private void poll_V(LocalPlayer player) {
      if (this.push_Z && player != null && !player.onGround() && this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RETURNING && (!this.size_Z || !this.prev_Z())) {
         if (read_Z_Z(this.prev_Z(), this.f_U_I == ck.init_ck().state_I(), this.size_Z)) {
            this.f_T_I = -2147483648;
         }

         this.close_ce_DataUtil();
      }
   }

   static boolean read_Z_Z(boolean visited, boolean moved, boolean placementPinned) {
      return visited && !moved && !placementPinned;
   }

   private ce.Inner_DataUtil close_ce_DataUtil() {
      return this.init_ce_DataUtil_F(180.0F);
   }

   private ce.Inner_DataUtil init_ce_DataUtil_F(float cap) {
      if (this.count_Z()) {
         this.data_Z = true;
         this.update_Z = false;
         return this.flush_ce_DataUtil();
      }

      if (write_Z_Z_Z(this.size_Z, this.prev_Z())) {
         return this.flush_ce_DataUtil();
      }

      if (this.init_aI_NodeWalker == ScaffoldModule.Inner_NodeWalker.PLACING) {
         this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.RETURNED;
      }

      this.init_aI_CacheEntry = null;
      this.close_Z = false;
      if (this.data_Z && this.data_Z()) {
         return this.flush_ce_DataUtil();
      }

      if (this.prev_Z()) {
         return this.flush_ce_DataUtil();
      }

      this.update_Z = true;
      ce.Inner_DataUtil returned = this.init_ce_DataUtil_ce_DataUtil_F(this.update_ce_DataUtil(), Math.min(cap, this.update_F()));
      this.data_Z = true;
      return returned;
   }

   static boolean write_Z_Z_Z(boolean heldForPlacement, boolean sameTick) {
      return heldForPlacement && sameTick;
   }

   private ce.Inner_DataUtil init_ce_DataUtil_LocalPlayer_aI_DataPipe(LocalPlayer player, ScaffoldModule.Inner_DataPipe placement) {
      ScaffoldModule.Inner_DataPipe live = this.init_aI_DataPipe_LocalPlayer_aI_DataPipe(player, placement);
      return live != null ? live.init_aI_TableIndex().init_ce_DataUtil() : this.update_ce_DataUtil();
   }

   private boolean data_Z() {
      return this.count_Z()
         ? this.write_ce_DataUtil != null && write_Z_F_F(this.flags_F, this.write_ce_DataUtil.init())
         : this.write_ce_DataUtil != null && Math.abs(ce.init(this.write_F(), this.write_ce_DataUtil.init())) <= 2.0F;
   }

   static int init_I_Z_2(boolean turnPending) {
      return turnPending ? 2 : 1;
   }

   private boolean value_Z() {
      if (this.data_Z()) {
         return true;
      }

      this.close_ce_DataUtil();
      return this.data_Z();
   }

   static int init_I_I_Z(int currentTicks, boolean forwardAligned) {
      return !forwardAligned ? 0 : Math.min(0, Math.max(0, currentTicks) + 1);
   }

   static boolean write_Z_I(int alignedTicks) {
      return alignedTicks >= 0;
   }

   private static boolean init_Z_KeyMapping(KeyMapping mapping) {
      return mapping != null && autismclient.util.aB.init(mapping).autism$isActuallyDown();
   }

   private void frame_V() {
      this.write_ce_DataUtil = null;
      if (!this.sync_Z) {
         if (this.load_ce_DataUtil != null && autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue)) {
            this.sync_Z = true;
         } else if (this.load_ce_DataUtil == null) {
            this.f_v_I = 0;
            autismclient.util.ap.init_V_ap_TaskQueue(this.init_ap_TaskQueue);
         }
      }
   }

   private void block_V() {
      this.read_BlockPos = null;
      this.init_aN.load();
      if (this.init_aM != null) {
         this.init_aM.init_V();
      }

      this.init_aM_DataUtil = null;
      this.index_F = 0.0F / 0.0F;
      this.f_x_I = -2147483648;
      this.flags_V();
      this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.FORWARD;
      this.init_aI_CacheEntry = null;
      this.close_Z = false;
      this.cache_Z = false;
      this.state_Z = false;
      this.queue_Z = false;
      this.frame_V();
      this.mark_V();
      this.parse_V();
      this.index_V();
      this.update_Input = null;
      this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.IDLE;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.RELEASED;
      this.push_Z = false;
      this.pop_Z = false;
      this.init_aI_b = ScaffoldModule.Inner_b.init_aI_b;
      this.run_Z = false;
      this.get_Z = false;
      this.set_Z = false;
      this.put_Z = false;
      this.add_Z = false;
      this.clear_Z = false;
      this.start_Z = false;
      this.close_BlockPos = null;
      this.stop_Z = false;
      this.next_Z = false;
      this.f_F_I = 0;
      this.f_G_I = 0;
      this.f_H_I = 0;
      this.buffer_Z = false;
      this.f_I_I = 0;
      this.f_J_I = 0;
      this.f_K_I = 0;
      this.f_L_I = 0;
      this.f_v_D = 0.0;
      this.f_w_D = 0.0;
      this.flags_F = 0.0F;
      this.f_T_I = -2147483648;
      this.f_bN_I = -2147483648;
      this.level_F = 0.0F;
      this.f_x_D = 0.0;
      this.f_M_I = 0;
      this.prev_Z = false;
      this.f_R_I = 0;
      this.f_S_I = 0;
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.task_Z = false;
      this.f_aa_I = 0;
      this.f_ab_I = 0;
      this.size_Z = false;
      this.f_V_I = -1;
      this.data_Z = true;
      this.f_W_I = 0;
      this.sync_BlockPos = null;
      this.open_BlockPos = null;
      this.data_V();
      this.init_Vec3 = null;
      this.init_aI_DataPipe = null;
   }

   private void chunk_V() {
      this.read_BlockPos = null;
      if (this.init_aM != null) {
         this.init_aM.init_V();
      }

      this.init_aM_DataUtil = null;
      this.index_F = 0.0F / 0.0F;
      this.f_x_I = -2147483648;
      this.flags_V();
      this.init_aI_NodeWalker = ScaffoldModule.Inner_NodeWalker.FORWARD;
      this.init_aI_CacheEntry = null;
      this.close_Z = false;
      this.cache_Z = false;
      this.state_Z = false;
      this.queue_Z = false;
      this.frame_V();
      this.mark_V();
      this.parse_V();
      this.index_V();
      this.update_Input = null;
      this.init_aI_ValueSink = ScaffoldModule.Inner_ValueSink.IDLE;
      this.init_aI_KeyHolder = ScaffoldModule.Inner_KeyHolder.RELEASED;
      this.push_Z = false;
      this.pop_Z = false;
      this.run_Z = false;
      this.f_H_I = 0;
      this.buffer_Z = false;
      this.f_K_I = 0;
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.next_Z = false;
      this.f_F_I = 0;
      this.f_M_I = 0;
      this.set_Z = false;
      this.add_Z = false;
      this.f_R_I = 0;
      this.f_S_I = 0;
      this.task_Z = false;
      this.f_aa_I = 0;
      this.f_ab_I = 0;
      this.size_Z = false;
      this.f_V_I = -1;
      this.data_Z = true;
      this.f_T_I = -2147483648;
      this.f_bN_I = -2147483648;
      this.f_W_I = 0;
      this.init_aI_DataPipe = null;
      this.open_BlockPos = null;
      this.stop_Z = false;
      this.data_V();
   }

   @Override
   public void tick() {
      if (!this.isEnabled()) {
         this.valid_V();
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.sync_Z && this.load_ce_DataUtil != null;
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundMovePlayerPacket movement && movement.hasRotation()) {
         ce.Inner_DataUtil base = this.poll_ce_DataUtil();
         this.init_ce_DataUtil = new ce.Inner_DataUtil(movement.getYRot(base.init()), movement.getXRot(base.load()));
      }

      return false;
   }

   public static void read_V_Packet(Packet<?> packet) {
      if (packet instanceof ServerboundPlayerInputPacket input) {
         block_Z = input.input().shift();
      }

      if (packet instanceof ServerboundMovePlayerPacket movement) {
         if (movement.hasPosition()) {
            write_Vec3 = new Vec3(movement.getX(0.0), movement.getY(0.0), movement.getZ(0.0));
         }

         Vec3 position = write_Vec3;
         if (position != null) {
            update_Vec3 = position.add(0.0, block_Z ? 1.27 : 1.62, 0.0);
         }
      }

      ScaffoldModule.Inner_RenderState queued = null;
      if (packet instanceof ServerboundUseItemOnPacket) {
         synchronized (load_Map) {
            reset_V();
            queued = load_Map.remove(new ScaffoldModule.Inner_StateCache(packet));
         }
      }

      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && (scaffold.frame_Z() || scaffold.index_Z())) {
         if (scaffold.index_Z()) {
            scaffold.init_V_Packet_aI_RenderState(packet, queued);
         }

         if (packet instanceof ServerboundMovePlayerPacket movement) {
            init_AtomicReference.set(new ScaffoldModule.Inner_TaskQueue(movement.isOnGround(), movement.horizontalCollision(), movement.hasPosition()));
         } else if (packet instanceof ServerboundUseItemOnPacket use && queued != null) {
            BlockHitResult hit = use.hitResult();
            init_ConcurrentLinkedQu.offer(
               new ScaffoldModule.Inner_NetHelper(
                  use.sequence(),
                  hit.getBlockPos().immutable(),
                  hit.getDirection(),
                  hit.getLocation(),
                  System.nanoTime(),
                  use.hand(),
                  queued,
                  ch.init_ch_CachePool()
               )
            );
         }
      }
   }

   public static void update_V_10() {
      write_Vec3 = null;
      update_Vec3 = null;
      block_Z = false;
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold) {
         scaffold.init_aO.init();
         scaffold.d_V();
      }

      synchronized (load_Map) {
         load_Map.clear();

         while (init_ReferenceQueue.poll() != null) {
         }
      }

      init_ConcurrentLinkedQu.clear();
      init_AtomicReference.set(null);
   }

   private void init_V_Packet_aI_RenderState(Packet<?> packet, ScaffoldModule.Inner_RenderState queued) {
      ch.Inner_CachePool snapshot = ch.init_ch_CachePool();
      ce.Inner_DataUtil wire = snapshot.init_Z() ? new ce.Inner_DataUtil(ch.init_F(), snapshot.write()) : null;
      int tick = ck.init_ck().state_I();
      if (packet instanceof ServerboundUseItemOnPacket use && queued != null) {
         BlockHitResult hit = use.hitResult();
         this.init_aO
            .init(
               new ScaffoldFailureRecorder.Inner_StreamUtil(
                  "use",
                  tick,
                  use.sequence(),
                  queued.init_BlockPos(),
                  hit.getBlockPos().immutable(),
                  hit.getDirection(),
                  write_Vec3,
                  hit.getLocation(),
                  queued.init_ce_DataUtil(),
                  wire,
                  null,
                  "generation=" + queued.init_J() + ",ordinal=" + queued.init_I() + ",hand=" + use.hand()
               )
            );
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         this.init_aO
            .init(
               new ScaffoldFailureRecorder.Inner_StreamUtil(
                  "move",
                  tick,
                  -1,
                  null,
                  null,
                  null,
                  write_Vec3,
                  null,
                  null,
                  wire,
                  null,
                  "ground=" + move.isOnGround() + ",collision=" + move.horizontalCollision()
               )
            );
      } else if (packet instanceof ServerboundPlayerInputPacket input) {
         this.init_aO.init(new ScaffoldFailureRecorder.Inner_StreamUtil("input", tick, -1, null, null, null, write_Vec3, null, null, wire, input.input(), null));
      } else if (packet instanceof ServerboundPlayerCommandPacket command) {
         this.init_aO.init(new ScaffoldFailureRecorder.Inner_StreamUtil("command", tick, -1, null, null, null, write_Vec3, null, null, wire, null, command.getAction().name()));
      }
   }

   public static void init_V_BlockPos_BlockState_2(BlockPos cell, BlockState state) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.index_Z() && scaffold.init_aO.init(cell)) {
         BlockPos immutable = cell.immutable();
         scaffold.init_aO
            .init(new ScaffoldFailureRecorder.Inner_StreamUtil("server-block", ck.init_ck().state_I(), -1, immutable, null, null, null, null, null, null, null, state.toString()));
         if (state.isAir()) {
            scaffold.init_V_Str("server-sent-air-for-recent-use", immutable, -1);
         }
      }
   }

   private void init_V_Str(String reason, BlockPos cell, int sequence) {
      if (MC != null && MC.level != null && MC.player != null && this.init_AtomicBoolean.compareAndSet(false, true)) {
         ScaffoldFailureRecorder.Inner_DataUtil capture = this.init_aO.init(reason, cell, sequence, ck.init_ck().state_I(), System.nanoTime());
         if (capture == null) {
            this.init_AtomicBoolean.set(false);
         } else {
            Path directory = MC.gameDirectory.toPath().resolve("logs");
            autismclient.util.BackgroundTaskExecutor.init_Future_Str_Runnable("Telly-placement-diagnostics", () -> {
               try {
                  Files.createDirectories(directory);
                  String json = capture.init_Str_2();
                  if (capture.init_Z()) {
                     Files.writeString(directory.resolve("telly-rejection-first.json"), json);
                  }

                  Files.writeString(directory.resolve("telly-rejection-latest.json"), json);
               } catch (IOException | RuntimeException error) {
                  AutismClientAddon.LOG.debug("Could not save Telly placement diagnostics", error);
               } finally {
                  this.init_AtomicBoolean.set(false);
               }
            });
         }
      }
   }

   private void table_V() {
      ScaffoldModule.Inner_NetHelper written;
      while ((written = init_ConcurrentLinkedQu.poll()) != null) {
         this.init_V_aI_NetHelper(written);
      }
   }

   private void store_V() {
      ScaffoldModule.Inner_TaskQueue written = init_AtomicReference.getAndSet(null);
      if (written != null) {
         this.mark_Z = true;
         this.scan_Z = written.init();
         this.parse_Z = written.load();
         this.bind_Z = written.read();
      }
   }

   private void init_V_aI_NetHelper(ScaffoldModule.Inner_NetHelper written) {
      ScaffoldModule.Inner_RenderState queued = written == null ? null : written.init_aI_RenderState();
      if (queued != null) {
         ScaffoldModule.Inner_TextFormat writtenWire = init_aI_TextFormat_ch_CachePool(written.init_ch_CachePool());
         ce.Inner_DataUtil actualClick = writtenWire == null ? null : writtenWire.load();
         if (this.flags_Z()) {
            this.set_J = written.init_J();
            this.init_V_BlockPos_2(queued.init_BlockPos(), queued.load(), written.init_Direction(), actualClick == null ? 0.0F / 0.0F : actualClick.load());
         }

         ScaffoldModule.Inner_TableIndex pending = this.read_aI_TableIndex;
         boolean matches = init_Z_aI_TableIndex(pending, written, this.chunk_Z);
         if (init_Z_aI_NetHelper_J(written, this.put_J)) {
            this.f_aL_I++;
            this.f_aM_I = Math.max(this.f_aM_I, written.init_I());
            if (this.index_Z() && this.f_C_I >= 0 && this.f_C_I < 9) {
               this.read_IArr[this.f_C_I] = written.init_I();
            }

            if (pending == null
               || !matches
               || written.init_InteractionHand() != queued.init_InteractionHand()
               || written.init_Direction() != queued.init_Direction()) {
               this.init_V_Str_3("packet-mismatch");
            } else if (init_Z_aI_RenderState(queued, this.table_Z, this.init_aI_SyncManager, this.f_aL_I)) {
               this.init_V_Str_3("duplicate-write");
            } else if (!this.level_Z() && !init_Z_aI_RenderState_ch_CachePool(queued, written.init_ch_CachePool())) {
               this.init_V_Str_3("wire-rotation");
            } else {
               this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.SENT;
               this.write_Str = "sent";
               this.fill_Z = true;
               if (actualClick != null) {
                  this.f_b_F = actualClick.init();
               }

               this.drain_V();
            }
         } else if (queued.init_I() > 0 || this.read_Map.containsKey(queued.init_BlockPos())) {
            this.init_V_BlockPos_I_2(queued.init_BlockPos(), written.init_I());
         }
      }
   }

   static boolean init_Z_aI_TableIndex_aI_NetHelper(ScaffoldModule.Inner_TableIndex pending, ScaffoldModule.Inner_NetHelper written) {
      return init_Z_aI_TableIndex(pending, written, false);
   }

   static boolean init_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex pending, ScaffoldModule.Inner_NetHelper written, boolean buildsPlannedCell) {
      if (pending != null && written != null) {
         if (!written.init_BlockPos().equals(pending.init_BlockPos())) {
            return false;
         } else {
            return buildsPlannedCell ? true : written.init_Direction() == pending.init_Direction() && written.init_Vec3().y >= pending.init_D();
         }
      } else {
         return false;
      }
   }

   static boolean init_Z_aI_NetHelper_J(ScaffoldModule.Inner_NetHelper written, long generation) {
      return written != null && written.init_aI_RenderState() != null && written.init_aI_RenderState().init_J() == generation;
   }

   static boolean init_Z_aI_SyncManager_I(ScaffoldModule.Inner_SyncManager state, int writeCount) {
      return state == ScaffoldModule.Inner_SyncManager.ARMED && writeCount == 1;
   }

   static boolean init_Z_aI_RenderState(ScaffoldModule.Inner_RenderState queued, boolean duplicateSubmitted, ScaffoldModule.Inner_SyncManager state, int writeCount) {
      return queued == null || queued.init_I() != 0 || duplicateSubmitted || !init_Z_aI_SyncManager_I(state, writeCount);
   }

   static boolean init_Z_aI_RenderState_ch_CachePool(ScaffoldModule.Inner_RenderState queued, ch.Inner_CachePool snapshot) {
      if (queued != null && queued.init_ce_DataUtil() != null && snapshot != null) {
         ScaffoldModule.Inner_TextFormat wire = init_aI_TextFormat_ch_CachePool(snapshot);
         return wire != null && init_Z_ce_DataUtil_ce_DataUtil_4(queued.init_ce_DataUtil(), wire.load());
      } else {
         return false;
      }
   }

   public static void init_V_InteractionHand(InteractionHand hand, BlockHitResult hit, InteractionResult result) {
      long generation = autismclient.util.as.init_J();
      if (generation > 0L && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.frame_Z()) {
         scaffold.init_V_J(generation, hand, hit, result);
      }
   }

   private void init_V_J(long generation, InteractionHand hand, BlockHitResult hit, InteractionResult result) {
      ScaffoldModule.Inner_TableIndex pending = this.read_aI_TableIndex;
      if (this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE
         && init_Z_J(generation, this.put_J, hand, this.init_InteractionHand, this.store_Z, hit, pending, this.chunk_Z)) {
         this.store_Z = true;
         this.drain_Z = result != null && result.consumesAction();
         if (this.index_Z() && this.drain_Z && this.f_C_I >= 0 && !MC.player.hasInfiniteMaterials()) {
            this.load_IArr[this.f_C_I] = Math.max(0, this.load_IArr[this.f_C_I] - 1);
            this.write_IArr[this.f_C_I] = ck.init_ck().state_I();
         }

         this.write_Str = result == null ? "null" : result.getClass().getSimpleName().toLowerCase(Locale.ROOT);
         this.table_V();
         this.drain_V();
      }
   }

   static boolean init_Z_J(
      long generation,
      long activeGeneration,
      InteractionHand hand,
      InteractionHand expectedHand,
      boolean resultSeen,
      BlockHitResult hit,
      ScaffoldModule.Inner_TableIndex pending
   ) {
      return init_Z_J(generation, activeGeneration, hand, expectedHand, resultSeen, hit, pending, false);
   }

   static boolean init_Z_J(
      long generation,
      long activeGeneration,
      InteractionHand hand,
      InteractionHand expectedHand,
      boolean resultSeen,
      BlockHitResult hit,
      ScaffoldModule.Inner_TableIndex pending,
      boolean buildsPlannedCell
   ) {
      return !resultSeen
         && generation > 0L
         && generation == activeGeneration
         && hand != null
         && hand == expectedHand
         && pending != null
         && hit != null
         && init_Z_BlockHitResult(hit, pending, buildsPlannedCell);
   }

   private void drain_V() {
      ScaffoldModule.Inner_TableIndex pending = this.read_aI_TableIndex;
      if (pending != null && this.store_Z && this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.SENT && MC.level != null) {
         BlockPos cell = pending.load();
         int age = Math.max(0, ck.init_ck().state_I() - this.f_aJ_I);
         ScaffoldModule.Inner_ConfigStore decision = init_aI_ConfigStore_aI_SyncManager(
            this.init_aI_SyncManager,
            age,
            true,
            this.drain_Z,
            this.level_Z()
               ? this.load_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell)
               : this.init_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell),
            load_Z_I_I(this.f_aM_I, this.f_aN_I)
         );
         if (decision.init_aI_SyncManager() == ScaffoldModule.Inner_SyncManager.FAILED) {
            this.init_V_Str_3("use".equals(decision.init_Str()) ? "use-" + this.write_Str : decision.init_Str());
         } else {
            this.init_aI_SyncManager = decision.init_aI_SyncManager();
         }
      }
   }

   public static void init_V_I_8(int sequence) {
      if (MC != null && MC.level != null && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold) {
         scaffold.c_V();
         scaffold.f_B_I = Math.max(scaffold.f_B_I, sequence);
         if (scaffold.isEnabled() && (scaffold.frame_Z() || scaffold.index_Z())) {
            scaffold.get_V();
            if (sequence > scaffold.f_aN_I) {
               scaffold.f_aN_I = sequence;
            }

            scaffold.set_V();
            scaffold.load_V_I_2(sequence);
            ScaffoldModule.Inner_TableIndex active = scaffold.read_aI_TableIndex;
            if (active != null && load_Z_I_I(scaffold.f_aM_I, sequence)) {
               boolean solid = scaffold.level_Z()
                  ? scaffold.load_Z_BlockState_BlockPos(MC.level.getBlockState(active.load()), active.load())
                  : scaffold.open(active.load());
               if (scaffold.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.FAILED) {
                  return;
               }

               int age = Math.max(0, ck.init_ck().state_I() - scaffold.f_aJ_I);
               ScaffoldModule.Inner_ConfigStore decision = init_aI_ConfigStore_aI_SyncManager(
                  scaffold.init_aI_SyncManager, age, scaffold.store_Z, scaffold.drain_Z, solid, true
               );
               if (decision.init_aI_SyncManager() == ScaffoldModule.Inner_SyncManager.FAILED) {
                  scaffold.init_V_Str_3(decision.init_Str());
               } else {
                  scaffold.init_aI_SyncManager = decision.init_aI_SyncManager();
               }
            }
         }
      }
   }

   private boolean cache_Z() {
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
            && !FreecamController.init_Z()
            && !cd.init_Z()
            && !f.load()
            && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
            && !MacroExecutor.write_Z()
            && !AutoTotemModule.init_Z_6()
            && !AutoArmorModule.init_Z_20();
   }

   public static boolean read_Z_12() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.pool_Z() && scaffold.push_Z;
   }

   public static boolean write_Z_5() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()
         && scaffold.pool_Z()
         && (
            scaffold.push_Z
               || MC != null
                  && MC.options != null
                  && (
                     init_Z_KeyMapping(MC.options.keyUp)
                        || init_Z_KeyMapping(MC.options.keyDown)
                        || init_Z_KeyMapping(MC.options.keyLeft)
                        || init_Z_KeyMapping(MC.options.keyRight)
                  )
         );
   }

   public static boolean update_Z_5() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.task_Z();
   }

   public static boolean sync_Z_6() {
      return f_bO_I == ck.init_ck().state_I();
   }

   public static Input init_Input_ClientInput_Input_4(ClientInput source, Input original) {
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
         && (scaffold.isEnabled() && scaffold.cache_Z() && scaffold.chunk_Z() || scaffold.sync_Z)) {
         ce.Inner_DataUtil saved = scaffold.load_ce_DataUtil;
         int resets = scaffold.f_v_I;

         try {
            scaffold.load_ce_DataUtil = autismclient.util.aD.init_ce_DataUtil();
            scaffold.f_v_I = Math.max(1, resets);
            Input result = load_Input_ClientInput_Input(source, original);
            f_bO_I = ck.init_ck().state_I();
            return result;
         } finally {
            scaffold.load_ce_DataUtil = saved;
            scaffold.f_v_I = resets;
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
      int tick = ck.init_ck().state_I();
      boolean continuous = this.f_bP_I == tick - 1;
      Vec3 pos = player.position();
      Vec3 vel = player.getDeltaMovement();
      Vec3 pos1 = continuous ? this.init_Vec3Arr[0] : null;
      Vec3 vel1 = continuous ? this.load_Vec3Arr[0] : null;
      Vec3 pos2 = continuous ? this.init_Vec3Arr[1] : null;
      Vec3 vel2 = continuous ? this.load_Vec3Arr[1] : null;
      boolean ground1 = continuous && this.init_ZArr[0];
      boolean ground2 = continuous && this.init_ZArr[1];
      boolean shiftBefore = !continuous || this.f_s_Z;
      Input last = source.keyPresses;
      this.init_Vec3Arr[1] = this.init_Vec3Arr[0];
      this.load_Vec3Arr[1] = this.load_Vec3Arr[0];
      this.init_ZArr[1] = this.init_ZArr[0];
      this.init_Vec3Arr[0] = pos;
      this.load_Vec3Arr[0] = vel;
      this.init_ZArr[0] = player.onGround();
      this.f_s_Z = last.shift();
      this.f_bP_I = tick;
      if (result != null
         && result.shift()
         && !original.shift()
         && !last.shift()
         && !shiftBefore
         && !result.jump()
         && this.flags_Z()
         && !this.push_Z
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
                     this.init_V_Str_2("dead-zone-defer", result, deferred);
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
            if (!flush_Z_Z_Z(scaffold.isEnabled(), scaffold.cache_Z())) {
               scaffold.fill_V();
               return scaffold.sync_Z ? scaffold.flush(original) : original;
            }

            if (scaffold.pool_Z()) {
               scaffold.fill_V();
               Input authored = scaffold.sync(original);
               Input result = scaffold.push_Z ? scaffold.poll(authored) : scaffold.flush(authored);
               if (scaffold.push_Z) {
                  int inputTick = ck.init_ck().state_I();
                  if (scaffold.count_Z() || scaffold.f_Z_I == inputTick || init_Z_Z_Input(scaffold.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.FORWARD, result)) {
                     scaffold.f_aP_I = inputTick;
                  } else if (scaffold.f_aP_I == inputTick) {
                     scaffold.f_aP_I = -2147483648;
                  }
               }

               scaffold.start_V();
               return result;
            } else {
               if (scaffold.cache_Z) {
                  original = init_Input_Input_5(original, scaffold.state_Z, !scaffold.count_Z() || scaffold.bool("eagle-jump"));
               }

               scaffold.write_Input = original;
               if (scaffold.flags_Z()) {
                  scaffold.close_V_4();
                  scaffold.b_V();
                  scaffold.add_V();
                  scaffold.init_V_Input(original);
               }

               boolean directionalInput = load_Z_Input(original);
               ScaffoldModule.Inner_HashUtil previousLine = scaffold.init_aI_HashUtil;
               ScaffoldModule.Inner_HashUtil requestedLine = directionalInput ? scaffold.init_aI_HashUtil_Input(original) : null;
               boolean courseChanged = init_Z_aI_HashUtil_aI_HashUtil(previousLine, requestedLine);
               scaffold.init_aI_HashUtil = requestedLine;
               if (!scaffold.flags_Z()) {
                  scaffold.fill_V();
                  return original;
               }

               if (scaffold.table_Z || scaffold.g_Z()) {
                  scaffold.f_aP_I = ck.init_ck().state_I();
                  scaffold.open_Str = "reconcile";
                  scaffold.load_Z_Z(true);
                  Input reconciled = new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), true, false);
                  reconciled = scaffold.init_Input_Input_Input(reconciled, original);
                  scaffold.init_V_Str_2("reconcile", original, reconciled);
                  scaffold.init_V_Str_2("net", original, reconciled);
                  scaffold.pop_Str = "reconcile dup=" + (scaffold.table_Z ? "T" : "F") + " untrusted=" + (scaffold.g_Z() ? "T" : "F");
                  return reconciled;
               }

               if (!scaffold.init_Z_ItemStack_4(scaffold.init_ItemStack_2()) && scaffold.j()) {
                  scaffold.fill_V();
                  scaffold.flush_Z = true;
                  scaffold.f_d_Z = false;
                  scaffold.f_e_Z = false;
                  scaffold.f_f_Z = false;
                  scaffold.f_g_Z = false;
                  scaffold.f_aW_I = 0;
                  scaffold.open_Str = "empty";
                  scaffold.load_Z_Z(true);
                  Input stopped = new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), true, false);
                  stopped = scaffold.init_Input_Input_Input(stopped, original);
                  scaffold.init_V_Str_2("empty-stack", original, stopped);
                  scaffold.init_V_Str_2("net", original, stopped);
                  scaffold.pop_Str = "empty-stack";
                  return stopped;
               }

               boolean carryArc = scaffold.entry_Z();
               boolean atEdge = scaffold.j() && !carryArc;
               boolean fallAhead = scaffold.init_aI_CachePool() == ScaffoldModule.Inner_CachePool.IMMINENT && !carryArc;
               boolean clickArmed = scaffold.state_Z();
               boolean clickLands = (atEdge || fallAhead) && clickArmed;
               boolean edgeDanger = atEdge && !clickLands;
               boolean fallDanger = fallAhead && !clickLands;
               boolean lateralBrink = scaffold.store_Z() || scaffold.drain_Z() || scaffold.load_Z_Input_Z(original, courseChanged);
               boolean footingOwedRaw = !clickArmed && scaffold.parse_Z();
               scaffold.f_aW_I = footingOwedRaw ? scaffold.f_aW_I + 1 : 0;
               boolean footingOwed = footingOwedRaw && scaffold.f_aW_I <= 10;
               scaffold.flush_Z = edgeDanger || fallDanger || lateralBrink || footingOwed;
               scaffold.f_d_Z = edgeDanger;
               scaffold.f_e_Z = fallDanger;
               scaffold.f_f_Z = lateralBrink;
               scaffold.f_g_Z = footingOwed;
               scaffold.init_aI_HashUtil = scaffold.init_aI_HashUtil_aI_HashUtil_Z(requestedLine, scaffold.flush_Z);
               if (courseChanged) {
                  if (MC.player == null || !MC.player.onGround()) {
                     scaffold.load_aI_TableIndex = null;
                  }

                  scaffold.a_V();
               }

               Input adjusted = original;
               BlockPos riseTakeoffCell = scaffold.flush_BlockPos();
               scaffold.start_BlockPos = riseTakeoffCell;
               boolean physicalClimbIntent = directionalInput && scaffold.queue_Z();
               scaffold.f_c_Z = physicalClimbIntent;
               scaffold.buffer_Z();
               if (scaffold.data_BlockPos != null) {
                  scaffold.next_Str = "floor";
               }

               scaffold.init_V_Z_Z_2(physicalClimbIntent, courseChanged);
               boolean lipStop = init_Z_Z_3(MC.player.onGround(), clickArmed, footingOwedRaw, scaffold.f_aW_I, 10);
               boolean wireSneak = scaffold.load_Z_Z(adjusted.shift() || scaffold.flush_Z || lipStop);
               if (!adjusted.shift() && wireSneak) {
                  Input beforeSneak = adjusted;
                  adjusted = new Input(adjusted.forward(), adjusted.backward(), adjusted.left(), adjusted.right(), adjusted.jump(), true, adjusted.sprint());
                  scaffold.init_V_Str_2("wire-sneak", beforeSneak, adjusted);
               }

               if (lipStop && !adjusted.shift()) {
                  Input beforeLip = adjusted;
                  adjusted = new Input(adjusted.forward(), adjusted.backward(), adjusted.left(), adjusted.right(), adjusted.jump(), true, false);
                  scaffold.init_V_Str_2("lip-stop", beforeLip, adjusted);
               }

               scaffold.open_Str = "-";
               if (write(physicalClimbIntent, MC.player.onGround(), scaffold.f_b_Z)) {
                  Input beforeSprint = adjusted;
                  adjusted = init_Input_Input_2(adjusted);
                  scaffold.init_V_Str_2("sprint-drop", beforeSprint, adjusted);
                  scaffold.f_aP_I = ck.init_ck().state_I();
               }

               Input emitted = scaffold.init_Input_Input_Input(adjusted, original);
               scaffold.init_V_Str_2("lane", adjusted, emitted);
               if (MC.player.onGround()) {
                  scaffold.open_Vec3 = MC.player.position();
               }

               boolean arcBrake = scaffold.poll(directionalInput, true);
               boolean climbBrake = scaffold.open(directionalInput, true);
               boolean coast = arcBrake && scaffold.level_Z && !climbBrake;
               scaffold.sync_Str = arcBrake ? (climbBrake ? "arc+dclimb" : (coast ? "coast" : "arc")) : (climbBrake ? "dclimb" : "--");
               boolean geometricBrake = arcBrake;
               arcBrake |= climbBrake;
               if (arcBrake) {
                  Input beforeBrake = emitted;
                  Vec3 brakeLane = scaffold.init_aI_HashUtil == null ? null : scaffold.init_aI_HashUtil.load();
                  emitted = coast
                     ? load_Input_Input(emitted)
                     : (
                        geometricBrake && init_Z_Vec3_Vec3(MC.player.getDeltaMovement(), brakeLane)
                           ? init_Input_Input_4(
                              emitted, MC.player.getDeltaMovement(), scaffold.load_ce_DataUtil == null ? MC.player.getYRot() : scaffold.load_ce_DataUtil.init()
                           )
                           : write_Input_Input(emitted)
                     );
                  if (!load_Z_Input(emitted)) {
                     scaffold.f_aB_I = ck.init_ck().state_I();
                  }

                  scaffold.init_V_Str_2("brake", beforeBrake, emitted);
               }

               if (MC.player.onGround() && !emitted.jump() && !emitted.shift()) {
                  float yaw = scaffold.load_ce_DataUtil == null ? MC.player.getYRot() : scaffold.load_ce_DataUtil.init();
                  BlockPos frictionCell = BlockPos.containing(MC.player.getX(), MC.player.getY() - 0.5, MC.player.getZ());
                  double friction = MC.level.getBlockState(frictionCell).getBlock().getFriction();
                  Vec3 nextStep = init_Vec3_Vec3(
                     MC.player.getDeltaMovement(), emitted, yaw, MC.player.getAttributeValue(Attributes.MOVEMENT_SPEED), friction, MC.player.isCrouching()
                  );
                  if (init_Z_LocalPlayer(MC.player, 0.0, 0.0, nextStep.x, nextStep.z)) {
                     Input beforeFooting = emitted;
                     boolean hold = scaffold.load_Z_Z(true);
                     emitted = new Input(emitted.forward(), emitted.backward(), emitted.left(), emitted.right(), emitted.jump(), hold, emitted.sprint());
                     scaffold.init_V_Str_2("step-floor", beforeFooting, emitted);
                  }
               }

               scaffold.init_V_Str_2("net", original, emitted);
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

   static Input init_Input_Input_2(Input input) {
      return input != null && input.sprint()
         ? new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), input.shift(), false)
         : input;
   }

   static Input load_Input_Input(Input input) {
      return new Input(false, false, false, false, input.jump(), input.shift(), false);
   }

   static Input init_Input_Input_4(Input input, Vec3 velocity, float writtenYaw) {
      if (velocity.horizontalDistance() < 0.03) {
         return load_Input_Input(input);
      }

      double counterYaw = Math.toDegrees(Math.atan2(velocity.z, velocity.x)) + 90.0;
      int octant = (int)Math.round(Mth.wrapDegrees(counterYaw - writtenYaw) / 45.0);
      return init_Input_Input_2(init_Input_I_Input(octant, input));
   }

   static boolean init_Z_Vec3_Vec3(Vec3 velocity, Vec3 lane) {
      double speed = velocity.horizontalDistance();
      if (speed < 0.03) {
         return false;
      } else {
         return lane != null && !(lane.horizontalDistanceSqr() < 1.0E-8)
            ? (velocity.x * lane.x + velocity.z * lane.z) / lane.horizontalDistance() < 0.85 * speed
            : true;
      }
   }

   static Vec3 init_Vec3_Vec3(Vec3 velocity, Input input, float yaw, double movementSpeed, double blockFriction, boolean crouched) {
      Vec3 local = new Vec3((input.left() ? 1 : 0) - (input.right() ? 1 : 0), 0.0, (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0));
      if (local.horizontalDistanceSqr() < 1.0E-8) {
         return velocity.multiply(1.0, 0.0, 1.0);
      }

      local = local.normalize();
      double magnitude = init_D_Vec3_Z(local, crouched);
      double acceleration = load_D_D_D(movementSpeed, blockFriction) / 0.98 * magnitude;
      Vec3 direction = Vec3.directionFromRotation(0.0F, yaw + init_F_Input(input));
      return velocity.multiply(1.0, 0.0, 1.0).add(direction.scale(acceleration));
   }

   private boolean state_Z() {
      this.table_V();
      this.drain_V();
      boolean ready = this.f_aJ_I == ck.init_ck().state_I()
         && this.init_aI_SyncManager == ScaffoldModule.Inner_SyncManager.PREDICTED
         && !this.table_Z
         && this.read_aI_TableIndex != null
         && this.open(this.read_aI_TableIndex.load());
      this.f_k_Z = ready;
      return ready;
   }

   static boolean init_Z_BlockHitResult_aI_TableIndex(BlockHitResult hit, ScaffoldModule.Inner_TableIndex pending) {
      return init_Z_BlockHitResult(hit, pending, false);
   }

   static boolean init_Z_BlockHitResult(BlockHitResult hit, ScaffoldModule.Inner_TableIndex pending, boolean buildsPlannedCell) {
      if (hit != null && pending != null) {
         if (!hit.getBlockPos().equals(pending.init_BlockPos())) {
            return false;
         } else {
            return buildsPlannedCell ? true : hit.getDirection() == pending.init_Direction() && hit.getLocation().y >= pending.init_D();
         }
      } else {
         return false;
      }
   }

   static boolean init_Z_Vec3_5(Vec3 from, Vec3 lane, BlockPos cell) {
      if (lane == null) {
         return false;
      }

      double dx = cell.getX() + 0.5 - from.x;
      double dz = cell.getZ() + 0.5 - from.z;
      return dx * lane.x + dz * lane.z > 0.0;
   }

   private int close_I() {
      if (MC.player == null) {
         return -2147483648;
      } else {
         return !MC.player.onGround() && this.f_cb_I != -2147483648 ? this.f_cb_I : this.open_I();
      }
   }

   private int open_I() {
      double feetY = MC.player.getY();
      return init_I_D_IntPredicate(feetY, row -> MC.level != null && this.init_Z_I_D(row, feetY));
   }

   static int init_I_D_IntPredicate(double feetY, IntPredicate reachesFeet) {
      int row = init_I_D(feetY);

      for (int drop = 0; drop <= 2; drop++) {
         if (reachesFeet.test(row - drop)) {
            return row - drop;
         }
      }

      return row;
   }

   static int init_I_D(double feetY) {
      return Mth.floor(feetY - 0.001);
   }

   private boolean init_Z_I_D(int row, double feetY) {
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

   private boolean queue_Z() {
      return MC.player != null
         && (!this.count_Z() || this.bool("eagle-jump"))
         && load_Z_Z(this.cache_Z, this.state_Z, init_Z_KeyMapping(MC.options.keyJump), MC.player.input == null ? null : MC.player.input.keyPresses);
   }

   static boolean load_Z_Z(boolean walkingFallback, boolean diagonal, boolean physicalJump, Input sampled) {
      return (!walkingFallback || diagonal) && (physicalJump || sampled != null && sampled.jump());
   }

   private void init_V_Z_Z_2(boolean physicalClimbIntent, boolean courseChanged) {
      if (courseChanged) {
         this.fill_V();
      }

      if (MC.player != null && MC.level != null) {
         if (MC.player.onGround() || !this.f_b_Z && this.put_BlockPos == null && !physicalClimbIntent) {
            if (physicalClimbIntent && this.init_aI_HashUtil != null) {
               if (this.f_b_Z) {
                  this.fill_V();
               }

               int footingRow = this.close_I();
               BlockPos landingSupport = this.read_BlockPos_I(footingRow);
               boolean landingKnown = landingSupport != null && !MC.level.isOutsideBuildHeight(landingSupport);
               BlockPos beforeGrounded = this.put_BlockPos;
               this.put_BlockPos = init_BlockPos_BlockPos(this.put_BlockPos, landingKnown ? landingSupport.immutable() : null, true, true, false);
               if (beforeGrounded == null || !beforeGrounded.equals(this.put_BlockPos)) {
                  this.clear_BlockPos = null;
                  this.reset_BlockPos = null;
               }

               this.add_BlockPos = null;
               int deficit = landingKnown ? this.load_I_BlockPos(landingSupport) : 2;
               if (landingKnown
                  && deficit <= 0
                  && (!this.poll(landingSupport) || this.table_Z || !init_Z_aI_SyncManager(this.init_aI_SyncManager, this.read_aI_TableIndex, landingSupport))) {
                  deficit = 1;
               }

               this.pop_Str = "grounded known="
                  + (landingKnown ? "T" : "F")
                  + " deficit="
                  + deficit
                  + " pend="
                  + (this.read_aI_TableIndex != null ? "T" : "F")
                  + " land="
                  + (landingSupport == null ? "--" : landingSupport.toShortString())
                  + " landsolid="
                  + (landingSupport == null ? "--" : (this.init_Z_BlockState_BlockPos(MC.level.getBlockState(landingSupport), landingSupport) ? "T" : "F"))
                  + " riser="
                  + (landingKnown && this.d_Z() ? (this.open(landingSupport.above()) ? "T" : "F") : "--");
            } else {
               this.fill_V();
               this.pop_Str = "not-asked intent=" + (physicalClimbIntent ? "T" : "F") + " line=" + (this.init_aI_HashUtil == null ? "F" : "T");
            }
         } else {
            BlockPos before = this.put_BlockPos;
            BlockPos advanced = this.read_BlockPos_I(this.close_I());
            boolean stillLands = before != null
               && (
                  this.update_Z_Vec3(MC.player.position(), MC.player.getDeltaMovement(), before)
                     || this.update_Z_Vec3(MC.player.position(), MC.player.getDeltaMovement(), before.above())
               );
            this.put_BlockPos = init_BlockPos_BlockPos(
               before,
               advanced == null ? null : advanced.immutable(),
               false,
               true,
               false,
               this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load(),
               stillLands
            );
            this.f_b_Z = true;
            boolean moved = before != null && !before.equals(this.put_BlockPos);
            if (moved) {
               this.clear_BlockPos = null;
               this.add_BlockPos = null;
               this.reset_BlockPos = null;
            }

            this.pop_Str = "airborne reserved="
               + (this.put_BlockPos == null ? "--" : this.put_BlockPos.toShortString())
               + (moved ? "<" + before.toShortString() : "");
         }
      } else {
         this.fill_V();
         this.pop_Str = "not-asked";
      }
   }

   static boolean init_Z_aI_SyncManager(ScaffoldModule.Inner_SyncManager state, ScaffoldModule.Inner_TableIndex pending, BlockPos support) {
      return pending == null || support == null || !pending.load().equals(support) || state == ScaffoldModule.Inner_SyncManager.PREDICTED;
   }

   private void fill_V() {
      this.put_BlockPos = null;
      this.add_BlockPos = null;
      this.clear_BlockPos = null;
      this.reset_BlockPos = null;
      this.f_b_Z = false;
      this.update_Str = "--";
   }

   static BlockPos init_BlockPos_BlockPos(BlockPos reserved, BlockPos projected, boolean grounded, boolean physicalIntent, boolean courseChanged) {
      return init_BlockPos_BlockPos(reserved, projected, grounded, physicalIntent, courseChanged, null, true);
   }

   static BlockPos init_BlockPos_BlockPos(
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

   private BlockPos update_BlockPos() {
      if (MC.player == null) {
         return null;
      }

      BlockPos at = BlockPos.containing(MC.player.position());
      return new BlockPos(at.getX(), this.close_I(), at.getZ());
   }

   static boolean load_Z_BlockPos_BlockPos(BlockPos a, BlockPos b) {
      return a != null && b != null && a.getX() == b.getX() && a.getZ() == b.getZ();
   }

   private int poll_I() {
      ScaffoldModule.Inner_FileCache lock = this.init_aI_FileCache();
      return init_I_I(this.close_I(), lock == null ? -2147483648 : lock.load(), this.f_cd_I);
   }

   static int init_I_I(int oracleRow, int lockRow, int airborneBuiltRow) {
      int floor = oracleRow;
      if (lockRow != -2147483648) {
         floor = floor == -2147483648 ? lockRow : Math.max(floor, lockRow);
      }

      if (airborneBuiltRow != -2147483648) {
         floor = floor == -2147483648 ? airborneBuiltRow : Math.max(floor, airborneBuiltRow);
      }

      return floor;
   }

   static boolean init_Z_I_3(int candidateY, int footingRow, boolean descending) {
      return candidateY < footingRow && !descending;
   }

   static boolean init_Z_I_6(int placedY, int builtFloorRow, boolean fallingBelowFooting, boolean arcLandsOnColumn) {
      return init_Z_I_3(placedY, builtFloorRow, fallingBelowFooting) && !arcLandsOnColumn;
   }

   private boolean open(ScaffoldModule.Inner_TableIndex target) {
      if (target != null && MC.player != null) {
         BlockPos placed = target.load();
         return init_Z_I_6(placed.getY(), this.poll_I(), this.h_Z(), load_Z_Vec3_2(MC.player.position(), MC.player.getDeltaMovement(), placed));
      } else {
         return false;
      }
   }

   static boolean init_Z_Z_3(boolean fallingBelowFooting, int placedY, double feetY, double feetVelY) {
      return fallingBelowFooting && placedY + 1.0 > feetY + Math.min(feetVelY, 0.0) + 0.001;
   }

   private boolean poll(ScaffoldModule.Inner_TableIndex target) {
      return target != null && MC.player != null ? init_Z_Z_3(this.h_Z(), target.load().getY(), MC.player.position().y, MC.player.getDeltaMovement().y) : false;
   }

   static boolean update_Z_F_F(float error, float lastError) {
      return Float.isNaN(lastError) || error < lastError - 0.5F;
   }

   private BlockPos sync_BlockPos() {
      if (MC.player != null && MC.level != null && MC.player.onGround() && this.queue_Z()) {
         if (this.init_aI_HashUtil != null && this.init_aI_HashUtil.load() != null) {
            int footingRow = this.close_I();
            Vec3 lane = this.init_aI_HashUtil.load();
            Vec3 position = MC.player.position();
            int riseY = footingRow + 1;
            ScaffoldModule.Inner_FileCache rowLock = this.init_aI_FileCache();
            BlockPos last = null;
            BlockPos far = null;

            for (double step = 0.5; step <= 1.7; step += 0.3) {
               BlockPos cell = BlockPos.containing(position.x + lane.x * step, riseY + 0.5, position.z + lane.z * step);
               if (!cell.equals(last)) {
                  last = cell;
                  if (!init_Z_Vec3_6(position, lane, cell, 0.0)) {
                     BlockPos below = cell.below();
                     if ((!this.open(below) || !init_Z_Vec3_BlockPos(position, below)) && !MC.level.isOutsideBuildHeight(cell)) {
                        BlockState state = MC.level.getBlockState(cell);
                        if (!this.init_Z_BlockState_BlockPos(state, cell) && (state.isAir() || state.canBeReplaced())) {
                           boolean belowReady = this.poll(below);
                           if (belowReady && (rowLock == null || rowLock.init(cell, true))) {
                              double[] landing = init_DArr_Vec3(position, MC.player.getDeltaMovement(), position.y);
                              if (landing != null && this.open(new BlockPos((int)Math.floor(landing[0]), riseY - 1, (int)Math.floor(landing[1])))) {
                                 this.next_Str = "lands";
                                 return null;
                              }

                              if (far == null) {
                                 far = cell;
                              }

                              if (read_Z_Vec3(position, MC.player.getDeltaMovement(), cell)) {
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

   private BlockPos init_BlockPos_Vec3_2(Vec3 position, int riseY, ScaffoldModule.Inner_FileCache rowLock) {
      BlockPos cell = this.load_BlockPos_Vec3(position, riseY, rowLock);
      return cell != null && this.open(cell.below()) ? cell : null;
   }

   private BlockPos load_BlockPos_Vec3(Vec3 position, int riseY, ScaffoldModule.Inner_FileCache rowLock) {
      if (!this.update_Z_I(ck.init_ck().state_I())) {
         return null;
      }

      BlockPos foot = this.close_BlockPos();
      BlockPos cell = new BlockPos(foot.getX(), riseY, foot.getZ());
      if (MC.level.isOutsideBuildHeight(cell)) {
         return null;
      }

      BlockState state = MC.level.getBlockState(cell);
      if (!this.init_Z_BlockState_BlockPos(state, cell) && (state.isAir() || state.canBeReplaced())) {
         if (!init_Z_Vec3_BlockPos(position, cell.below())) {
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

      if (!this.queue_Z() && !this.f_b_Z) {
         return false;
      }

      BlockPos support = this.read_BlockPos_I(this.close_I());
      if (support != null && !this.open(support)) {
         if (this.load_I_BlockPos(support) >= 2) {
            return false;
         }

         this.data_BlockPos = support;
         this.f_bQ_I = ck.init_ck().state_I();
         return true;
      } else {
         return false;
      }
   }

   private int load_I_BlockPos(BlockPos cell) {
      if (MC.level != null && cell != null && !MC.level.isOutsideBuildHeight(cell)) {
         if (this.init_Z_BlockState_BlockPos(MC.level.getBlockState(cell), cell)) {
            return 0;
         }

         for (Direction face : Direction.values()) {
            BlockPos neighbour = cell.relative(face);
            if (!MC.level.isOutsideBuildHeight(neighbour) && this.init_Z_BlockState_BlockPos(MC.level.getBlockState(neighbour), neighbour)) {
               return 1;
            }
         }

         return 2;
      } else {
         return 0;
      }
   }

   private boolean flush(BlockPos cell) {
      return this.data_BlockPos != null && this.data_BlockPos.equals(cell) && this.f_bQ_I == ck.init_ck().state_I();
   }

   private BlockPos flush_BlockPos() {
      BlockPos now = this.sync_BlockPos();
      if (now != null) {
         this.value_BlockPos = now;
         this.f_bR_I = 4;
         return now;
      } else if (this.value_BlockPos == null) {
         return null;
      } else {
         boolean climbing = MC.player != null && MC.player.onGround() && this.queue_Z();
         if (climbing && this.f_bR_I > 0 && !this.open(this.value_BlockPos.below())) {
            this.f_bR_I--;
            this.next_Str = "latch";
            return this.value_BlockPos;
         } else {
            this.value_BlockPos = null;
            this.f_bR_I = 0;
            return null;
         }
      }
   }

   static boolean init_Z_Z_3(boolean onGround, boolean clickArmed, boolean footingOwed, int owedTicks, int maxTicks) {
      return onGround && !clickArmed && footingOwed && owedTicks > maxTicks;
   }

   static double[] init_DArr_Vec3(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded) {
      return init_DArr_Vec3(position, velocity, initialVy, surfaceY, grounded, 0);
   }

   static double[] init_DArr_Vec3(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded, int sneakedTicks) {
      return init_DArr_Vec3(position, velocity, initialVy, surfaceY, grounded, sneakedTicks, null, null, null);
   }

   static double[] init_DArr_Vec3(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded, int sneakedTicks, Vec3 inputDirection) {
      return init_DArr_Vec3(position, velocity, initialVy, surfaceY, grounded, sneakedTicks, inputDirection, null, null);
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

   private static double[] init_DArr_Vec3(
      Vec3 position,
      Vec3 velocity,
      double initialVy,
      double surfaceY,
      boolean grounded,
      int sneakedTicks,
      Vec3 inputDirection,
      AABB initialBox,
      ScaffoldModule.Inner_BufferHelper collisionResolver
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

   private double[] load_DArr_Vec3(Vec3 position, Vec3 velocity, double initialVy, double surfaceY, boolean grounded, int sneakedTicks) {
      Vec3 lane = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
      return MC.player != null && MC.level != null
         ? init_DArr_Vec3(
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
         : init_DArr_Vec3(position, velocity, initialVy, surfaceY, grounded, sneakedTicks, lane);
   }

   static boolean init_Z_Vec3_4(Vec3 position, Vec3 velocity, double initialVy, BlockPos cell, boolean grounded) {
      if (cell == null) {
         return false;
      }

      double[] landing = init_DArr_Vec3(position, velocity, initialVy, cell.getY() + 1.0, grounded);
      return landing != null && init_Z_DArr_BlockPos(landing, cell);
   }

   static boolean load_Z_Vec3_2(Vec3 position, Vec3 velocity, BlockPos cell) {
      return init_Z_Vec3_4(position, velocity, velocity == null ? 0.0 : velocity.y, cell, false);
   }

   private boolean update_Z_Vec3(Vec3 position, Vec3 velocity, BlockPos cell) {
      if (cell == null) {
         return false;
      }

      double[] landing = this.load_DArr_Vec3(
         position, velocity, velocity == null ? 0.0 : velocity.y, cell.getY() + 1.0, MC.player != null && MC.player.onGround(), this.push_I()
      );
      return landing != null && init_Z_DArr_BlockPos(landing, cell);
   }

   private int push_I() {
      int latchedCurrent = MC.player != null && MC.player.isCrouching() ? 1 : 0;
      return Math.max(latchedCurrent, Math.max(0, this.f_ap_I));
   }

   static boolean read_Z_Vec3(Vec3 position, Vec3 velocity, BlockPos cell) {
      return init_Z_Vec3_4(position, velocity, 0.42, cell, true);
   }

   static double[] init_DArr_Vec3(Vec3 position, Vec3 velocity, double surfaceY) {
      return init_DArr_Vec3(position, velocity, 0.42, surfaceY, true);
   }

   static boolean init_Z_DArr_BlockPos(double[] landing, BlockPos cell) {
      if (landing != null && cell != null) {
         double overlapX = Math.min(landing[0] + 0.29, cell.getX() + 1.0) - Math.max(landing[0] - 0.29, cell.getX());
         double overlapZ = Math.min(landing[1] + 0.29, cell.getZ() + 1.0) - Math.max(landing[1] - 0.29, cell.getZ());
         return overlapX >= 0.15 && overlapZ >= 0.15;
      } else {
         return false;
      }
   }

   private boolean entry_Z() {
      return MC.player != null && update_Z_Z_Z(MC.player.onGround(), this.queue_Z());
   }

   static boolean update_Z_Z_Z(boolean onGround, boolean jumpHeld) {
      return jumpHeld && !onGround;
   }

   static boolean init_Z_aI_HashUtil_aI_HashUtil(ScaffoldModule.Inner_HashUtil previous, ScaffoldModule.Inner_HashUtil requested) {
      return previous != null && requested != null && !load_Z_Vec3_Vec3(previous.load(), requested.load());
   }

   private Input sync(Input original) {
      if (this.push_Z && this.init_aI_KeyHolder != ScaffoldModule.Inner_KeyHolder.RELEASED) {
         Input authored = this.init_Input();
         if (this.f_Z_I == ck.init_ck().state_I()) {
            authored = new Input(authored.forward(), authored.backward(), authored.left(), authored.right(), authored.jump(), authored.shift(), false);
         }

         return init_Input_Input_Z(authored, init_Z_KeyMapping(MC.options.keyShift));
      } else {
         return original;
      }
   }

   static Input init_Input_Input_Z(Input authored, boolean manualSneak) {
      return new Input(
         authored.forward(), authored.backward(), authored.left(), authored.right(), authored.jump(), manualSneak, authored.sprint() && !manualSneak
      );
   }

   private Input init_Input() {
      if (this.entry_Z && MC.player != null && !MC.player.onGround()) {
         return new Input(false, true, false, false, false, false, false);
      }

      if (this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.HOLD) {
         if (this.update_Input != null) {
            Input recovery = this.update_Input;
            this.update_Input = null;
            return new Input(recovery.forward(), recovery.backward(), recovery.left(), recovery.right(), this.run_Z, false, false);
         }

         if (MC.player != null && MC.player.onGround()) {
            BlockPos support = this.init_BlockPos_LocalPlayer(MC.player);
            if (support == null) {
               support = this.sync_BlockPos;
            }

            Input brake = support == null
               ? null
               : this.init_Input_LocalPlayer_2(MC.player, support, new Input(false, false, false, false, false, false, false));
            if (brake != null && load_Z_Input(brake)) {
               return new Input(brake.forward(), brake.backward(), brake.left(), brake.right(), this.run_Z, false, false);
            }
         }

         ScaffoldModule.Inner_c strafe = this.init_aI_c;
         return new Input(false, false, strafe == ScaffoldModule.Inner_c.LEFT, strafe == ScaffoldModule.Inner_c.RIGHT, this.run_Z, false, false);
      } else {
         return MC.player != null && MC.player.onGround() && load_Z_aI_ValueSink(this.init_aI_ValueSink)
            ? init_Input_Z_Z(this.run_Z, false)
            : init_Input_aI_c(this.init_aI_c, this.run_Z, false);
      }
   }

   static Input init_Input_AABB(AABB footprint, BlockPos support, float courseYaw) {
      return init_Input_AABB(footprint, support, courseYaw, 0.45);
   }

   static Input init_Input_AABB(AABB footprint, BlockPos support, float courseYaw, double alongDeadZone) {
      if (init_Z_AABB_2(footprint, support, 1.0E-9) && Float.isFinite(courseYaw)) {
         Vec3 forward = init_Vec3_F(courseYaw);
         Vec3 left = new Vec3(forward.z, 0.0, -forward.x);
         Vec3 inward = Vec3.atCenterOf(support).subtract(footprint.getCenter());
         double along = inward.dot(forward);
         double across = inward.dot(left);
         return new Input(along > alongDeadZone, along < -alongDeadZone, across > 0.45, across < -0.45, false, false, false);
      } else {
         return new Input(false, false, false, false, false, false, false);
      }
   }

   static Input init_Input_aI_c(ScaffoldModule.Inner_c strafe, boolean jump, boolean sneak) {
      ScaffoldModule.Inner_c safe = strafe == null ? ScaffoldModule.Inner_c.NONE : strafe;
      return new Input(true, false, safe == ScaffoldModule.Inner_c.LEFT, safe == ScaffoldModule.Inner_c.RIGHT, jump, sneak, true);
   }

   static ScaffoldModule.Inner_TokenStore init_aI_TokenStore_I(
      int cooldown, ScaffoldModule.Inner_c lastPulse, int pulsesUsed, double laneError, double lateralVelocity, int ticksUntilLanding
   ) {
      int cooled = Math.max(0, cooldown - 1);
      int used = Math.max(0, pulsesUsed);
      ScaffoldModule.Inner_c previous = lastPulse == null ? ScaffoldModule.Inner_c.NONE : lastPulse;
      if (Double.isFinite(laneError) && Double.isFinite(lateralVelocity)) {
         double predictedError = init_D_D_3(laneError, lateralVelocity, ticksUntilLanding);
         double projectedDistance = Math.abs(predictedError);
         if (projectedDistance <= 0.1 && Math.abs(laneError) <= 0.16) {
            previous = ScaffoldModule.Inner_c.NONE;
         }

         if (!(projectedDistance <= 0.16) && cooldown <= 0) {
            double desired = Mth.clamp(laneError * 0.18, -0.055, 0.055);
            ScaffoldModule.Inner_c requested = init_aI_c_D_D(lateralVelocity, desired);
            if (requested == ScaffoldModule.Inner_c.NONE) {
               return new ScaffoldModule.Inner_TokenStore(ScaffoldModule.Inner_c.NONE, cooled, previous, used);
            }

            boolean reversing = previous != ScaffoldModule.Inner_c.NONE && requested != previous;
            if (reversing && projectedDistance < 0.48) {
               return new ScaffoldModule.Inner_TokenStore(ScaffoldModule.Inner_c.NONE, cooled, previous, used);
            }

            int pulseLimit = projectedDistance >= 0.48 ? 3 : 2;
            return used >= pulseLimit
               ? new ScaffoldModule.Inner_TokenStore(ScaffoldModule.Inner_c.NONE, cooled, previous, used)
               : new ScaffoldModule.Inner_TokenStore(requested, 2, requested, used + 1);
         } else {
            return new ScaffoldModule.Inner_TokenStore(ScaffoldModule.Inner_c.NONE, cooled, previous, used);
         }
      } else {
         return new ScaffoldModule.Inner_TokenStore(ScaffoldModule.Inner_c.NONE, cooled, previous, used);
      }
   }

   static ScaffoldModule.Inner_c init_aI_c_D_D(double lateralVelocity, double desiredVelocity) {
      if (!Double.isFinite(lateralVelocity) || !Double.isFinite(desiredVelocity)) {
         return ScaffoldModule.Inner_c.NONE;
      } else if (lateralVelocity < desiredVelocity - 0.026) {
         return ScaffoldModule.Inner_c.LEFT;
      } else {
         return lateralVelocity > desiredVelocity + 0.026 ? ScaffoldModule.Inner_c.RIGHT : ScaffoldModule.Inner_c.NONE;
      }
   }

   static double init_D_D_3(double laneError, double lateralVelocity, int ticksUntilLanding) {
      int ticks = Mth.clamp(ticksUntilLanding, 1, 14);
      double travel = 0.0;
      double motion = lateralVelocity;

      for (int tick = 0; tick < ticks; tick++) {
         travel += motion;
         motion *= 0.91;
      }

      return laneError - travel;
   }

   private void init_V_LocalPlayer_D(LocalPlayer player, double catchFeetY) {
      if (player != null && this.sync_BlockPos != null) {
         Vec3 left = this.update_Vec3();
         double laneError = this.f_x_D - init_D_Vec3_F(player.position(), this.flags_F);
         double lateralVelocity = player.getDeltaMovement().dot(left);
         int ticksUntilLanding = init_I_Vec3(player.position(), player.getDeltaMovement(), catchFeetY);
         ScaffoldModule.Inner_TokenStore next = init_aI_TokenStore_I(this.f_N_I, this.load_aI_c, this.f_O_I, laneError, lateralVelocity, ticksUntilLanding);
         this.init_aI_c = next.init_aI_c();
         this.f_N_I = next.init_I();
         this.load_aI_c = next.load_aI_c();
         this.f_O_I = next.load_I();
      }
   }

   private void mark_V() {
      this.init_aI_c = ScaffoldModule.Inner_c.NONE;
      this.load_aI_c = ScaffoldModule.Inner_c.NONE;
      this.f_N_I = 0;
      this.f_O_I = 0;
      this.f_P_I = -1;
      this.f_Q_I = 0;
      this.index_F = 0.0F / 0.0F;
      this.f_x_I = -2147483648;
   }

   static Input init_Input_Z_Z(boolean jump, boolean sneak) {
      return new Input(true, false, false, false, jump, sneak, true);
   }

   static Input read_Input_Input(Input physical) {
      return load_Input_Input_Z(physical, false);
   }

   static Input load_Input_Input_Z(Input physical, boolean diagonal) {
      return init_Input_Input_5(physical, diagonal, true);
   }

   static Input init_Input_Input_5(Input physical, boolean diagonal, boolean jumpAllowed) {
      return diagonal && jumpAllowed
         ? physical
         : new Input(physical.forward(), physical.backward(), physical.left(), physical.right(), false, physical.shift(), diagonal && physical.sprint());
   }

   static boolean load_Z_aI_ValueSink(ScaffoldModule.Inner_ValueSink phase) {
      return phase == ScaffoldModule.Inner_ValueSink.RUNNING;
   }

   private void open_V(LocalPlayer player, BlockPos support) {
      if (player != null && support != null && player.onGround() && this.init_aI_ValueSink == ScaffoldModule.Inner_ValueSink.RUNNING) {
         Vec3 left = this.update_Vec3();
         double error = this.f_x_D - init_D_Vec3_F(player.position(), this.flags_F);
         double lateralVelocity = player.getDeltaMovement().dot(left);
         BlockState supportState = MC.level.getBlockState(support);
         double drag = supportState.getBlock().getFriction() * 0.91;
         double groundAcceleration = Math.max(0.025, player.getSpeed() * 0.98);
         double forwardSpeed = Math.max(0.0, player.getDeltaMovement().dot(this.write_Vec3()));
         double runwayRemaining = this.init_D_LocalPlayer_BlockPos(player, support);
         double launchPoint = load_D_D(forwardSpeed);
         boolean returnToCourse = runwayRemaining <= init_D_D_2(launchPoint, forwardSpeed, this.block_F);
         ScaffoldModule.Inner_MapStore next = init_aI_MapStore_Z(this.valid_Z, this.block_F, error, lateralVelocity, groundAcceleration, drag, returnToCourse);
         this.valid_Z = next.init_Z();
         this.block_F = next.init_F();
      } else {
         this.scan_V();
      }
   }

   private double init_D_LocalPlayer_BlockPos(LocalPlayer player, BlockPos support) {
      Direction direction = this.init_Direction();
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
         if (!this.load_Z_BlockState_BlockPos(MC.level.getBlockState(ahead), ahead)) {
            return remaining + step - 1.0;
         }
      }

      return 1.0 / 0.0;
   }

   static ScaffoldModule.Inner_MapStore init_aI_MapStore_Z(
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
      float nextOffset = load_F_F(currentOffset, targetOffset, maximumStep);
      if (!nextActive && Math.abs(nextOffset) < 0.05F) {
         nextOffset = 0.0F;
      }

      return new ScaffoldModule.Inner_MapStore(nextActive, nextOffset);
   }

   static double init_D_D_2(double launchPoint, double forwardSpeed, float steeringOffset) {
      int returnTicks = Math.max(1, Mth.ceil(Math.abs(steeringOffset) / 7.0F));
      return launchPoint + Math.max(0.0, forwardSpeed) * (returnTicks + 1) + 0.1;
   }

   private static float load_F_F(float current, float target, float maximumStep) {
      float difference = target - current;
      return Math.abs(difference) <= maximumStep ? target : current + Math.copySign(maximumStep, difference);
   }

   private void scan_V() {
      this.valid_Z = false;
      this.block_F = 0.0F;
      this.parse_V();
   }

   private void parse_V() {
      this.frame_F = 0.0F;
      this.table_F = 0.0F;
      this.fill_F = 0.0F / 0.0F;
   }

   public static float init_F_Entity_F(Entity entity, float vanillaYaw) {
      if (entity == null || MC == null || entity != MC.player) {
         return vanillaYaw;
      } else if (f.load()) {
         return vanillaYaw;
      } else if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.table_Z()) {
         ce.Inner_DataUtil rotation = scaffold.load_ce_DataUtil;
         return rotation != null && scaffold.f_v_I > 0 ? init_F_LocalPlayer_F(MC.player, vanillaYaw) : vanillaYaw;
      } else {
         return vanillaYaw;
      }
   }

   public static float init_F_LocalPlayer_F(LocalPlayer player, float vanillaYaw) {
      ce.Inner_DataUtil rotation = load_ce_DataUtil_LocalPlayer(player);
      int tick = ck.init_ck().state_I();
      if (tick != f_bS_I) {
         f_bS_I = tick;
         float anchor = Float.isNaN(f_l_F) ? vanillaYaw : f_l_F;
         f_l_F = rotation == null ? vanillaYaw : read_F_F_F(anchor, rotation.init());
      }

      return f_l_F;
   }

   static float read_F_F_F(float previousSentYaw, float silentYaw) {
      return previousSentYaw + Mth.wrapDegrees(silentYaw - previousSentYaw);
   }

   public static float load_F_LocalPlayer_F(LocalPlayer player, float vanillaPitch) {
      ce.Inner_DataUtil rotation = load_ce_DataUtil_LocalPlayer(player);
      return rotation == null ? vanillaPitch : rotation.load();
   }

   public static Vec3 init_Vec3_LivingEntity_Vec3_2(LivingEntity entity, Vec3 vanillaImpulse) {
      ce.Inner_DataUtil rotation = init_ce_DataUtil_LivingEntity(entity);
      if (rotation == null) {
         return vanillaImpulse;
      }

      float sent = MC != null && entity == MC.player ? init_F_LocalPlayer_F(MC.player, rotation.init()) : rotation.init();
      float yaw = sent * 0.017453292F;
      return new Vec3(-Mth.sin(yaw) * 0.2F, vanillaImpulse.y, Mth.cos(yaw) * 0.2F);
   }

   public static float init_F_LivingEntity_F(LivingEntity entity, float vanillaPitch) {
      ce.Inner_DataUtil rotation = init_ce_DataUtil_LivingEntity(entity);
      return rotation == null ? vanillaPitch : rotation.load();
   }

   public static Vec3 load_Vec3_LivingEntity_Vec3(LivingEntity entity, Vec3 vanillaLook) {
      ce.Inner_DataUtil rotation = init_ce_DataUtil_LivingEntity(entity);
      return rotation == null ? vanillaLook : Vec3.directionFromRotation(rotation.load(), rotation.init());
   }

   private static ce.Inner_DataUtil init_ce_DataUtil_LivingEntity(LivingEntity entity) {
      if (entity == null || MC == null || entity != MC.player) {
         return null;
      } else if (f.load()) {
         return null;
      } else if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.table_Z()) {
         ce.Inner_DataUtil rotation = scaffold.load_ce_DataUtil;
         return rotation != null && scaffold.f_v_I > 0 ? rotation : null;
      } else {
         return null;
      }
   }

   private static ce.Inner_DataUtil load_ce_DataUtil_LocalPlayer(LocalPlayer player) {
      if (player == null || MC == null || player != MC.player) {
         return null;
      } else if (f.load()) {
         return null;
      } else {
         return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.table_Z() && scaffold.f_v_I > 0 ? scaffold.load_ce_DataUtil : null;
      }
   }

   private boolean read_Z_Z(boolean jumping) {
      boolean rising = sync_Z_Z_Z(jumping, MC.player == null || MC.player.onGround()) && this.d_Z();
      this.f_j_Z = rising;
      return rising;
   }

   static boolean sync_Z_Z_Z(boolean jumpKeyHeld, boolean onGround) {
      return jumpKeyHeld || !onGround;
   }

   public static boolean flush_Z_5() {
      return MC != null && load_ce_DataUtil_LocalPlayer(MC.player) != null;
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && flush_Z_5();
   }

   public static ce.Inner_DataUtil init_ce_DataUtil_3() {
      return MC == null ? null : load_ce_DataUtil_LocalPlayer(MC.player);
   }

   public static ce.Inner_DataUtil load_ce_DataUtil_3() {
      ce.Inner_DataUtil active = init_ce_DataUtil_3();
      if (active == null) {
         return null;
      }

      float yaw = Float.isNaN(f_l_F) ? active.init() : read_F_F_F(f_l_F, active.init());
      return new ce.Inner_DataUtil(yaw, active.load());
   }

   public static Vec3 init_Vec3_LocalPlayer_Vec3(LocalPlayer player, Vec3 original) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && !scaffold.pool_Z()) {
         ce.Inner_DataUtil rotation = load_ce_DataUtil_LocalPlayer(player);
         return rotation == null ? original : Vec3.directionFromRotation(rotation.load(), rotation.init());
      } else {
         return original;
      }
   }

   private boolean node_Z() {
      return !this.task_Z() && !this.pool_Z();
   }

   private boolean task_Z() {
      String mode = this.choice("mode");
      return "Rage".equals(mode) || "Fast".equals(mode);
   }

   private boolean pool_Z() {
      return this.index_Z() && !this.cache_Z;
   }

   private boolean index_Z() {
      return "Telly".equals(this.choice("mode")) || this.count_Z();
   }

   private boolean count_Z() {
      return AutismLiteVariant.enabled() ? false : "Eagle".equals(this.choice("mode"));
   }

   private boolean flags_Z() {
      return this.node_Z();
   }

   private boolean level_Z() {
      return this.pool_Z() && this.push_Z;
   }

   private boolean frame_Z() {
      return this.flags_Z() || this.level_Z();
   }

   private boolean block_Z() {
      return this.flags_Z() && (this.index_Z() ? this.state_Z && this.bool("allow-diagonal") : this.bool("remove-limits"));
   }

   static float init_F_Z_F(boolean unlimited, float bounded) {
      return unlimited ? 180.0F : bounded;
   }

   private boolean chunk_Z() {
      return this.flags_Z() || this.pool_Z() && this.push_Z;
   }

   private boolean table_Z() {
      return !AutismLiteVariant.enabled() && (DangerModule.read_Z_22() || ClutchModule.read_Z_20()) ? false : read_Z_Z(this.sync_Z, this.isEnabled(), this.chunk_Z(), this.cache_Z());
   }

   public static ce.Inner_DataUtil read_ce_DataUtil() {
      boolean clutchHandoff = false;
      if (!AutismLiteVariant.enabled()) {
         clutchHandoff = ClutchModule.read_Z_20();
      }

      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && (scaffold.cache_Z() || clutchHandoff) && scaffold.f_v_I > 0
         ? (scaffold.pool_Z() ? scaffold.update_ce_DataUtil() : scaffold.load_ce_DataUtil)
         : null;
   }

   public static void init_V_ce_DataUtil_3(ce.Inner_DataUtil rotation) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && rotation != null) {
         scaffold.load_ce_DataUtil = rotation;
         f_l_F = rotation.init();
         f_bS_I = -2147483648;
         scaffold.f_v_I = 5;
         scaffold.flush_ce_DataUtil = rotation;
         scaffold.read_ce_DataUtil = null;
         scaffold.init_dg.init_V_J(scaffold.init_Random.nextLong());
         scaffold.f_aQ_I = 0;
         scaffold.fill_F = 0.0F / 0.0F;
         if (scaffold.pool_Z()) {
            scaffold.write_ce_DataUtil = rotation;
            autismclient.util.ap.init_V_ap_TaskQueue_ce_DataUtil(scaffold.init_ap_TaskQueue, rotation);
         }
      }
   }

   public static void load_V_ce_DataUtil(ce.Inner_DataUtil rotation) {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled() && scaffold.pool_Z() && MC.player != null && MC.level != null) {
         BlockPos support = MC.player.onGround() ? scaffold.init_BlockPos_LocalPlayer(MC.player) : null;
         scaffold.block_V();
         scaffold.flush_BlockPos = support;
         scaffold.f_ac_I = support == null ? -2147483648 : ck.init_ck().state_I();
         if (support != null
            && (
               init_Z_KeyMapping(MC.options.keyUp)
                  || init_Z_KeyMapping(MC.options.keyDown)
                  || init_Z_KeyMapping(MC.options.keyLeft)
                  || init_Z_KeyMapping(MC.options.keyRight)
            )) {
            scaffold.init_V_LocalPlayer_BlockPos(MC.player, support);
         }
      }

      init_V_ce_DataUtil_3(rotation);
   }

   static boolean read_Z_Z(boolean windingDown, boolean enabled, boolean silentPath, boolean canRun) {
      return windingDown || enabled && silentPath && canRun;
   }

   static boolean flush_Z_Z_Z(boolean enabled, boolean canRun) {
      return enabled && canRun;
   }

   static boolean load_Z_Vec3_Vec3(Vec3 previous, Vec3 current) {
      if (previous != null && current != null && !(previous.horizontalDistanceSqr() <= 1.0E-12) && !(current.horizontalDistanceSqr() <= 1.0E-12)) {
         Vec3 first = new Vec3(previous.x, 0.0, previous.z).normalize();
         Vec3 second = new Vec3(current.x, 0.0, current.z).normalize();
         return first.dot(second) >= 0.999;
      } else {
         return false;
      }
   }

   private static double init_D_Vec3_Vec3(Vec3 from, Vec3 to) {
      double cross = from.x * to.z - from.z * to.x;
      double dot = from.x * to.x + from.z * to.z;
      return Math.toDegrees(Math.atan2(cross, dot));
   }

   private boolean init_Z_Vec3_aI_HashUtil(Vec3 position, ScaffoldModule.Inner_HashUtil line) {
      if (this.init_aI_FileCache() != null) {
         return true;
      }

      int since = ck.init_ck().state_I() - this.f_z_I;
      if (since >= 0 && since <= 20) {
         return true;
      }

      Vec3 heading = this.init_Vec3_aI_HashUtil(line);
      return heading != null && this.load_Z_Vec3(position, heading, 1.0);
   }

   private Vec3 init_Vec3_aI_HashUtil(ScaffoldModule.Inner_HashUtil line) {
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

      if (this.scan_Z()) {
         return true;
      }

      if (this.fill_Z()) {
         return true;
      }

      if (this.a_Z()) {
         return true;
      }

      ScaffoldModule.Inner_HashUtil line = this.init_aI_HashUtil;
      if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 position = MC.player.position();
         if (Math.abs(init_D_aI_HashUtil_Vec3(line, position)) <= 0.45) {
            return false;
         }

         Vec3 probe = init_Vec3_aI_HashUtil(line, position, 0.35);
         return !this.init_Z_D(probe.x, position.y, probe.z, 1);
      } else {
         return false;
      }
   }

   private boolean load_Z_Input_Z(Input input, boolean courseChanged) {
      if (this.state_Z && this.index_Z() && MC.player.onGround() && (courseChanged || this.a_Z()) && load_Z_Input(input)) {
         LocalPlayer player = MC.player;
         BlockPos support = this.init_BlockPos_LocalPlayer(player);
         if (support == null) {
            return true;
         }

         double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
         double side = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
         double yaw = Math.toRadians(player.getYRot());
         Vec3 direction = new Vec3(side * Math.cos(yaw) - forward * Math.sin(yaw), 0.0, forward * Math.cos(yaw) + side * Math.sin(yaw)).normalize();
         double acceleration = load_D_D_D(player.getAttributeValue(Attributes.MOVEMENT_SPEED), this.init_D_BlockPos_2(support));
         Vec3 step = player.getDeltaMovement().multiply(1.0, 0.0, 1.0).add(direction.scale(acceleration));
         return !this.init_Z_AABB(player.getBoundingBox().move(step), player.getY(), 0.12);
      } else {
         return false;
      }
   }

   private boolean drain_Z() {
      if (this.index_Z() && this.state_Z && MC.player != null && MC.level != null && !MC.player.onGround() && !(MC.player.getDeltaMovement().y >= 0.0)) {
         LocalPlayer player = MC.player;
         Vec3 requested = this.load_Vec3();
         AABB body = player.getBoundingBox();
         Vec3 moved = this.init_Vec3_LocalPlayer(player, body, requested);
         if (moved.y <= requested.y + 1.0E-9) {
            return false;
         }

         AABB landed = body.move(moved);
         double feetY = landed.minY;
         if (!this.init_Z_AABB(landed, feetY, 1.0E-9)) {
            return false;
         }

         Vec3 lane = this.init_aI_HashUtil == null ? Vec3.ZERO : this.init_aI_HashUtil.load();
         if (lane.horizontalDistanceSqr() > 1.0E-8) {
            lane = new Vec3(lane.x, 0.0, lane.z).normalize();
         }

         BlockPos frictionCell = BlockPos.containing(landed.getCenter().x, feetY - 0.5, landed.getCenter().z);
         double friction = MC.level.getBlockState(frictionCell).getBlock().getFriction();
         double acceleration = load_D_D_D(player.getAttributeValue(Attributes.MOVEMENT_SPEED), friction);
         Vec3 next = new Vec3(moved.x * 0.91, 0.0, moved.z * 0.91).add(lane.scale(acceleration));
         return !this.init_Z_AABB(landed.move(next), feetY, 0.12);
      } else {
         return false;
      }
   }

   private boolean fill_Z() {
      if (MC.player != null && MC.level != null && MC.player.onGround()) {
         Vec3 velocity = MC.player.getDeltaMovement();
         if (velocity.horizontalDistanceSqr() <= 1.0E-8) {
            return false;
         }

         Vec3 next = load_Vec3_Vec3_Vec3(MC.player.position(), velocity);
         return load_Z_D(this.read_D_Vec3(next));
      } else {
         return false;
      }
   }

   static Vec3 load_Vec3_Vec3_Vec3(Vec3 position, Vec3 velocity) {
      return position != null && velocity != null ? position.add(velocity.x, 0.0, velocity.z) : position;
   }

   static Input write_Input_Input(Input input) {
      return input == null ? null : new Input(input.backward(), input.forward(), input.right(), input.left(), input.jump(), input.shift(), false);
   }

   static double load_D_JArr(long[] gaps, long sinceLastMs, int window) {
      if (gaps != null && gaps.length != 0 && window > 0 && sinceLastMs >= 0L) {
         long[] clamped = new long[gaps.length];

         for (int i = 0; i < gaps.length; i++) {
            clamped[i] = Math.min(1000L, Math.max(0L, gaps[i]));
         }

         return init_D_JArr(clamped, Math.min(1000L, sinceLastMs), window);
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
      int tick = ck.init_ck().state_I();
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
         Vec3 lane = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
         if (!init_Z_Vec3_2(lane)) {
            return false;
         }

         long[] gaps = new long[this.update_ArrayDeque.size()];
         int index = 0;

         for (long gap : this.update_ArrayDeque) {
            gaps[index++] = gap;
         }

         long sinceLast = this.write_J_J(System.nanoTime());
         double mean = load_D_JArr(gaps, sinceLast, 8);
         this.f_z_D = mean;
         if (this.block_Z()) {
            return false;
         }

         ScaffoldModule.Inner_TableIndex next = this.read_aI_TableIndex != null ? this.read_aI_TableIndex : this.load_aI_TableIndex;
         if (next != null
            && this.load_ce_DataUtil != null
            && !init_Z_Direction(next.init_Direction(), this.load_ce_DataUtil.load(), next.init_ce_DataUtil().load(), this.chunk_F, sinceLast)) {
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
      return init_Z_Direction(face) && (init_Z_F_2(writtenPitch, previousPitch, gapMs, false) || init_Z_F_2(goalPitch, previousPitch, gapMs, false));
   }

   private double init_D_Vec3_I(Vec3 landing, int row) {
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
                  double overlap = init_D_DArr_BlockPos(point, cell);
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
      return this.init_Z_BlockState_BlockPos(state, cell);
   }

   private boolean poll(boolean directionalInput, boolean traced) {
      int tick = ck.init_ck().state_I();
      if (this.f_az_I != tick) {
         this.f_az_I = tick;
         this.level_Z = false;
         this.flags_Z = this.push(directionalInput, true);
         if (this.flags_Z && MC.player != null) {
            Vec3 coast = init_Vec3_Vec3(MC.player.position(), MC.player.getDeltaMovement(), this.f_aA_I + 1.0, 24, 0.0);
            this.level_Z = coast != null && this.init_D_Vec3_I(coast, this.f_aA_I) >= 0.2;
         }
      }

      return this.flags_Z;
   }

   private boolean push(boolean directionalInput, boolean traced) {
      if (traced) {
         this.f_B_D = 0.0;
         this.flush_Str = "--";
         this.close_Str = "--";
      }

      if (this.mark_Z()) {
         if (traced) {
            this.close_Str = "off";
         }

         return false;
      } else if (this.flags_Z() && MC.player != null && !MC.player.onGround()) {
         if (directionalInput && this.d_Z()) {
            BlockPos support = this.put_BlockPos;
            if (support == null) {
               if (traced) {
                  this.close_Str = "nosup";
               }

               return false;
            } else {
               Vec3 velocity = MC.player.getDeltaMovement();
               ScaffoldModule.Inner_HashUtil line = this.init_aI_HashUtil;
               Vec3 lane = line == null ? null : line.load();
               double brakeSpeed = velocity.horizontalDistance();
               if (!init_Z_Vec3_Vec3(velocity, lane) && lane != null && lane.horizontalDistanceSqr() > 1.0E-8) {
                  brakeSpeed = (velocity.x * lane.x + velocity.z * lane.z) / lane.horizontalDistance();
               }

               if (brakeSpeed < 0.03) {
                  if (traced) {
                     this.close_Str = "speed";
                  }

                  return false;
               } else {
                  boolean flatTellyCatch = init_Z_Str(this.choice("mode"), this.state_Z, this.d_Z()) && !this.open(support.above());
                  double catchFeet = support.getY() + (flatTellyCatch ? 1.0 : 2.0);
                  Vec3 landing = init_Vec3_Vec3(MC.player.position(), velocity, catchFeet, 24, 0.02);
                  this.f_aA_I = support.getY() + (flatTellyCatch ? 0 : 1);
                  if (landing == null && !flatTellyCatch) {
                     this.f_aA_I = support.getY();
                     landing = init_Vec3_Vec3(MC.player.position(), velocity, this.f_aA_I + 1.0, 24, 0.02);
                  }

                  if (landing == null) {
                     if (traced) {
                        this.close_Str = "noland";
                     }

                     return false;
                  } else {
                     Vec3 start = this.open_Vec3;
                     if (traced) {
                        this.f_B_D = start == null ? 0.0 : Math.hypot(landing.x - start.x, landing.z - start.z);
                     }

                     double carry = this.init_D_Vec3_I(landing, this.f_aA_I);
                     if (traced) {
                        this.flush_Str = Double.isNaN(carry) ? "bare" : String.format(Locale.ROOT, "%+.2f", carry);
                     }

                     if (carry >= 0.15) {
                        return false;
                     }

                     if (this.init_Z_Z_Vec3(velocity.y > 0.0, lane)) {
                        if (traced) {
                           this.close_Str = "xing";
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
               this.close_Str = "noasc";
            }

            return false;
         }
      } else {
         if (traced) {
            this.close_Str = "nogrim";
         }

         return false;
      }
   }

   private boolean mark_Z() {
      return false;
   }

   private boolean scan_Z() {
      return MC.player != null && MC.level != null && MC.player.onGround() ? load_Z_D(this.read_D_Vec3(MC.player.position())) : false;
   }

   static boolean load_Z_D(double overlap) {
      return overlap <= 0.0;
   }

   private double read_D_Vec3(Vec3 point) {
      return this.load_D_Vec3_I(point, Mth.floor(point.y) - 1);
   }

   private double load_D_Vec3_I(Vec3 point, int y) {
      double half = MC.player.getBbWidth() * 0.5;
      double minX = point.x - half;
      double maxX = point.x + half;
      double minZ = point.z - half;
      double maxZ = point.z + half;
      double best = 0.0;

      for (int cx = Mth.floor(minX); cx <= Mth.floor(maxX); cx++) {
         for (int cz = Mth.floor(minZ); cz <= Mth.floor(maxZ); cz++) {
            BlockPos pos = new BlockPos(cx, y, cz);
            if (!MC.level.isOutsideBuildHeight(pos) && this.init_Z_BlockState_BlockPos(MC.level.getBlockState(pos), pos)) {
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
         Vec3 lead = this.load_Vec3().scale(init_I_I_3(this.f_bT_I));
         return load_Z_D_D(this.read_D_Vec3(position), this.read_D_Vec3(position.add(lead)));
      } else {
         return false;
      }
   }

   static boolean load_Z_D_D(double overlap, double leadOverlap) {
      return overlap <= 0.3 && leadOverlap <= overlap;
   }

   static int init_I_I_3(int paceWaitTicks) {
      return Mth.clamp(1 + paceWaitTicks, 1, 4);
   }

   private boolean bind_Z() {
      int since = ck.init_ck().state_I() - this.f_aU_I;
      return since >= 0 && since <= 6;
   }

   private boolean a_Z() {
      ScaffoldModule.Inner_HashUtil line = this.init_aI_HashUtil;
      if (line != null && !(line.load().horizontalDistanceSqr() <= 1.0E-8)) {
         Vec3 velocity = MC.player.getDeltaMovement();
         Vec3 horizontal = new Vec3(velocity.x, 0.0, velocity.z);
         if (horizontal.length() < 0.05) {
            return false;
         } else {
            return Math.abs(init_D_Vec3_Vec3(horizontal, line.load())) <= 60.0 ? false : this.init_Z_Vec3_aI_HashUtil(MC.player.position(), line);
         }
      } else {
         return false;
      }
   }

   static double init_D_aI_HashUtil_Vec3(ScaffoldModule.Inner_HashUtil line, Vec3 position) {
      Vec3 direction = line.load().normalize();
      Vec3 left = new Vec3(direction.z, 0.0, -direction.x);
      return init_Vec3_aI_HashUtil_Vec3(line, position).subtract(position).dot(left);
   }

   static Vec3 init_Vec3_aI_HashUtil(ScaffoldModule.Inner_HashUtil line, Vec3 position, double distance) {
      Vec3 direction = line.load().normalize();
      Vec3 left = new Vec3(direction.z, 0.0, -direction.x);
      double error = init_D_aI_HashUtil_Vec3(line, position);
      return position.add(left.scale(error > 0.0 ? -distance : distance));
   }

   private boolean load_Z_Vec3(Vec3 position, Vec3 horizontal, double speed) {
      Vec3 step = horizontal.scale(1.0 / speed);

      for (double distance = 0.5; distance <= 2.5; distance += 0.5) {
         BlockPos feet = BlockPos.containing(position.add(step.scale(distance)));
         boolean supported = this.init_Z_BlockState_BlockPos(MC.level.getBlockState(feet), feet)
            || this.init_Z_BlockState_BlockPos(MC.level.getBlockState(feet.below()), feet.below())
            || this.init_Z_BlockState_BlockPos(MC.level.getBlockState(feet.below(2)), feet.below(2));
         if (!supported) {
            return true;
         }
      }

      return false;
   }

   private Input flush(Input input) {
      boolean tellyWindDown = this.sync_Z && autismclient.util.ap.init_Z_ap_TaskQueue(this.init_ap_TaskQueue);
      if ((this.chunk_Z() || tellyWindDown) && this.load_ce_DataUtil != null && this.f_v_I > 0) {
         if (this.sync_Z) {
            return this.close(input);
         }

         if (this.pool_Z()) {
            return this.load_Input_Input(input, MC.player.getYRot(), this.load_ce_DataUtil.init());
         }

         Input result = init_Input_Input_2(input, MC.player.getYRot(), this.load_ce_DataUtil.init());
         if (result != null && !result.forward() && load_Z_Input(input)) {
            this.f_aP_I = ck.init_ck().state_I();
         }

         return result;
      } else {
         return input;
      }
   }

   static float init_F_F_2(float forward, float sideways, float referenceYaw) {
      return Mth.wrapDegrees(referenceYaw - (float)Math.toDegrees(Math.atan2(sideways, forward)));
   }

   static boolean init_Z_Input(Input physical) {
      return physical != null && physical.left() != physical.right();
   }

   static float write_F_F_F(float laneYaw, float freeYaw) {
      float off = Mth.wrapDegrees(freeYaw - laneYaw);
      return Mth.wrapDegrees(laneYaw + Math.round(off / 45.0F) * 45.0F);
   }

   static float update_F_F_F(float laneYaw, float emittedYaw) {
      float off = Mth.wrapDegrees(emittedYaw - laneYaw);
      return Mth.wrapDegrees(off - Math.round(off / 45.0F) * 45.0F);
   }

   static boolean write_Z_F(float bias) {
      return Math.abs(bias) > 37.5F;
   }

   private float sync_F() {
      ScaffoldModule.Inner_HashUtil line = this.init_aI_HashUtil;
      if (line != null && MC.player != null) {
         Vec3 direction = line.load();
         if (direction.horizontalDistanceSqr() <= 1.0E-8) {
            this.bind_V();
            return 0.0F;
         }

         Vec3 unit = direction.normalize();
         Vec3 left = new Vec3(unit.z, 0.0, -unit.x);
         double offset = init_D_aI_HashUtil_Vec3(line, MC.player.position());
         int previous = this.f_bU_I;
         this.f_bU_I = init_I_I(previous, this.f_bV_I, this.f_bW_I, offset, MC.player.getDeltaMovement().dot(left));
         if (this.f_bU_I == 0) {
            if (previous != 0) {
               this.f_bV_I = 0;
               this.f_bW_I = 3;
            } else {
               this.bind_V();
            }

            return 0.0F;
         } else {
            this.f_bV_I++;
            this.f_bW_I = 0;
            double demand = Math.max(Math.abs(offset), 0.06) * this.f_bU_I;
            return init_F_Vec3_4(unit, left.scale(demand), Vec3.ZERO, 0.0, 0.23);
         }
      } else {
         this.bind_V();
         return 0.0F;
      }
   }

   private void bind_V() {
      this.f_bU_I = 0;
      this.f_bV_I = 0;
      if (this.f_bW_I > 0) {
         this.f_bW_I--;
      }
   }

   static int init_I_I(int side, int heldTicks, int lockTicks, double offset, double perpendicularVelocity) {
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
      int tick = ck.init_ck().state_I();
      Input result = this.init_bb.load(input, MC.player.getYRot(), this.load_ce_DataUtil.init(), tick);
      if (result == null) {
         return null;
      }

      this.f_aQ_I = result.forward() == result.backward() ? 0 : (result.forward() ? 1 : -1);
      this.f_aR_I = result.left() == result.right() ? 0 : (result.left() ? 1 : -1);
      this.f_aS_I = 0;
      this.f_a_Z = false;
      this.fill_F = 0.0F / 0.0F;
      if (!result.forward() && load_Z_Input(input)) {
         this.f_aP_I = tick;
      }

      return result;
   }

   private Input init_Input_Input_Input(Input input, Input physical) {
      if (this.chunk_Z() && this.load_ce_DataUtil != null && this.f_v_I > 0) {
         float forward = init_F_Z_Z(input.forward(), input.backward());
         float sideways = init_F_Z_Z(input.left(), input.right());
         float emitted = this.load_ce_DataUtil.init();
         this.poll_Str = "cam";
         this.table_F = 0.0F;
         this.f_t_Z = false;
         if (init_Z_Input(physical)) {
            this.poll_Str = "steer";
            this.f_aT_I = 0;
            this.bind_V();
         } else if (forward == 0.0F && sideways == 0.0F) {
            this.f_aT_I = 0;
            this.bind_V();
         } else if (this.load_I_8() != -1) {
            float laneYaw = this.load_F_2();
            float bias = Mth.wrapDegrees(laneYaw - init_F_F_2(forward, sideways, MC.player.getYRot()));
            if (write_Z_F(bias)) {
               this.table_F = Mth.clamp(this.sync_F(), -38.0F, 38.0F);
               this.f_aT_I = 0;
            } else {
               this.table_F = bias + Mth.clamp(this.sync_F(), -38.0F, 38.0F);
               this.poll_Str = "lane";
               float residual = Math.abs(update_F_F_F(laneYaw, emitted));
               if (residual > 2.0F) {
                  this.f_t_Z = ++this.f_bX_I <= 8;
               } else {
                  this.f_bX_I = 0;
               }

               if (residual > 12.0F) {
                  if (++this.f_aT_I <= 6) {
                     Input coast = update_Input_Input(this.open(input));
                     if (coast != null) {
                        this.poll_Str = this.f_t_Z ? "sweep" : "coast";
                        if (!coast.forward()) {
                           this.f_aP_I = ck.init_ck().state_I();
                        }

                        return coast;
                     }
                  }
               } else {
                  this.f_aT_I = 0;
               }

               if (this.f_t_Z) {
                  this.poll_Str = "sweep";
               }
            }
         }

         if (this.sync_Z && this.table_F == 0.0F && !this.f_t_Z) {
            return this.close(input);
         }

         Input result = this.load_Input_Input(input, MC.player.getYRot(), emitted);
         if (this.f_t_Z) {
            result = update_Input_Input(result);
         }

         if (result != null && !result.forward() && load_Z_Input(input)) {
            this.f_aP_I = ck.init_ck().state_I();
         }

         return result;
      } else {
         return input;
      }
   }

   static Input update_Input_Input(Input input) {
      return input != null && input.left() != input.right()
         ? new Input(input.forward(), input.backward(), false, false, input.jump(), input.shift(), init_Z_Input_Z(input, input.forward()))
         : input;
   }

   static boolean init_Z_Input_Z(Input input, boolean emittedForward) {
      return input != null && input.sprint() && emittedForward;
   }

   private Input open(Input input) {
      return this.f_aQ_I == 0 && this.f_aR_I == 0
         ? null
         : new Input(this.f_aQ_I > 0, this.f_aQ_I < 0, this.f_aR_I > 0, this.f_aR_I < 0, input.jump(), input.shift(), init_Z_Input_Z(input, this.f_aQ_I > 0));
   }

   public static boolean close_Z_4() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.f_aP_I == ck.init_ck().state_I();
   }

   public static boolean open_Z_3() {
      return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()
         && scaffold.pool_Z()
         && scaffold.push_Z
         && scaffold.init_aI_KeyHolder != ScaffoldModule.Inner_KeyHolder.RELEASED;
   }

   private void init_V_F(float courseYaw) {
      this.frame_F = sync_F_F_F(this.frame_F, this.update_F_F(courseYaw));
      this.table_F = this.frame_F;
   }

   private float update_F_F(float courseYaw) {
      if (MC.player != null && this.sync_BlockPos != null && !Float.isNaN(this.flags_F)) {
         double error = this.f_x_D - init_D_Vec3_F(MC.player.position(), this.flags_F);
         if (Double.isFinite(error) && !(Math.abs(error) > 0.55)) {
            Vec3 forward = this.write_Vec3();
            if (forward.horizontalDistanceSqr() <= 1.0E-8) {
               return 0.0F;
            }

            Vec3 left = this.update_Vec3();
            return init_F_Vec3_3(forward.normalize(), left.scale(error), MC.player.getDeltaMovement());
         } else {
            return 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   static float sync_F_F_F(float current, float target) {
      float wanted = Mth.clamp(target, -15.0F, 15.0F);
      float step = Mth.clamp(wanted - current, -3.0F, 3.0F);
      return Mth.clamp(current + step, -15.0F, 15.0F);
   }

   private Input poll(Input input) {
      if (this.chunk_Z() && this.load_ce_DataUtil != null && this.f_v_I > 0) {
         boolean easing = this.entry_Z && input.backward() && !input.forward();
         if (MC.player != null
            && !MC.player.onGround()
            && this.sync_BlockPos != null
            && (easing || this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.FORWARD && input.forward() && !input.backward())) {
            double error = this.f_x_D - init_D_Vec3_F(MC.player.position(), this.flags_F);
            double velocity = MC.player.getDeltaMovement().dot(this.update_Vec3());
            float controlCourse = easing ? this.flags_F + 180.0F : this.flags_F;
            if (easing) {
               error = -error;
               velocity = -velocity;
            }

            int tick = ck.init_ck().state_I();
            if (this.f_x_I != tick || !Float.isFinite(this.index_F) || Math.abs(Mth.wrapDegrees(this.index_F - this.load_ce_DataUtil.init())) > 0.001F) {
               float nextYaw = this.init_aM().init(this.load_ce_DataUtil, this.init_RangeSetting.get(), ce.init());
               boolean turning = Math.abs(Mth.wrapDegrees(nextYaw - this.load_ce_DataUtil.init())) > 4.0F
                  || Float.isFinite(this.index_F) && Math.abs(Mth.wrapDegrees(this.index_F - this.load_ce_DataUtil.init())) > 4.0F;
               ScaffoldModule.Inner_ClassCache next = init_aI_ClassCache_F(
                  controlCourse,
                  this.load_ce_DataUtil.init(),
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
               this.index_F = this.load_ce_DataUtil.init();
               this.f_x_I = tick;
            }

            return init_Input_I_Input(this.f_P_I, input);
         } else {
            float courseYaw = this.valid_Z ? Mth.wrapDegrees(this.flags_F + this.block_F) : this.flags_F;
            if (this.init_aI_KeyHolder == ScaffoldModule.Inner_KeyHolder.HOLD && MC.player != null && MC.player.onGround()) {
               this.parse_V();
               this.state_V();
               this.f_aQ_I = 0;
               return init_Input_Input_2(input, courseYaw, this.load_ce_DataUtil.init());
            } else {
               this.init_V_F(courseYaw);
               return this.load_Input_Input(input, courseYaw, this.load_ce_DataUtil.init());
            }
         }
      } else {
         return input;
      }
   }

   static ScaffoldModule.Inner_ClassCache init_aI_ClassCache_F(
      float courseYaw, float emittedYaw, double error, double velocity, double acceleration, int previous, int dwell
   ) {
      return init_aI_ClassCache_F(courseYaw, emittedYaw, emittedYaw, error, velocity, acceleration, previous, dwell, false);
   }

   static ScaffoldModule.Inner_ClassCache init_aI_ClassCache_F(
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

         return new ScaffoldModule.Inner_ClassCache(best, best == previous ? Math.min(20, dwell + 1) : 1);
      } else {
         return new ScaffoldModule.Inner_ClassCache(nearest, 1);
      }
   }

   static Input init_Input_I_Input(int octant, Input original) {
      int index = Math.floorMod(octant, 8);
      boolean forward = index == 0 || index == 1 || index == 7;
      boolean back = index >= 3 && index <= 5;
      boolean left = index >= 5 && index <= 7;
      boolean right = index >= 1 && index <= 3;
      return new Input(forward, back, left, right, original.jump(), original.shift(), init_Z_Input_Z(original, forward));
   }

   public static Input init_Input_Input_2(Input input, float playerYaw, float silentYaw) {
      if (input == null) {
         return null;
      }

      float forward = init_F_Z_Z(input.forward(), input.backward());
      float sideways = init_F_Z_Z(input.left(), input.right());
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
         init_Z_Input_Z(input, roundedForward > 0)
      );
   }

   private Input load_Input_Input(Input input, float playerYaw, float silentYaw) {
      if (input == null) {
         return null;
      }

      float forward = init_F_Z_Z(input.forward(), input.backward());
      float sideways = init_F_Z_Z(input.left(), input.right());
      if (forward == 0.0F && sideways == 0.0F) {
         this.f_aQ_I = 0;
         this.f_aR_I = 0;
         this.f_aS_I = 0;
         this.f_a_Z = false;
         this.store_F = 0.0F;
         this.drain_F = 0.0F;
         this.fill_F = 0.0F / 0.0F;
         return input;
      }

      float deltaDegrees = Mth.wrapDegrees(playerYaw + this.table_F - silentYaw);
      boolean referenceMoved = Float.isFinite(this.fill_F) && Math.abs(Mth.wrapDegrees(deltaDegrees - this.fill_F)) >= 22.5F;
      this.fill_F = deltaDegrees;
      float deltaYaw = deltaDegrees * 0.017453292F;
      float cosine = Mth.cos(deltaYaw);
      float sine = Mth.sin(deltaYaw);
      float transformedSideways = sideways * cosine - forward * sine;
      float transformedForward = forward * cosine + sideways * sine;
      int steadyForward = init_I_D_I(transformedForward, this.f_aQ_I);
      int steadySideways = this.init_I_I(Math.round(transformedSideways), forward, sideways, referenceMoved);
      if (steadyForward == 0 && steadySideways == 0) {
         steadyForward = Math.round(transformedForward);
         steadySideways = Math.round(transformedSideways);
      }

      this.f_aQ_I = steadyForward;
      this.f_aR_I = steadySideways;
      return new Input(
         steadyForward > 0, steadyForward < 0, steadySideways > 0, steadySideways < 0, input.jump(), input.shift(), init_Z_Input_Z(input, steadyForward > 0)
      );
   }

   private int init_I_I(int requested, float rawForward, float rawSideways, boolean referenceMoved) {
      boolean playerChangedKeys = rawForward != this.store_F || rawSideways != this.drain_F;
      this.store_F = rawForward;
      this.drain_F = rawSideways;
      if (this.sync_Z) {
         this.f_aS_I = 0;
         this.f_a_Z = false;
         return requested;
      }

      if (requested == this.f_aR_I) {
         this.f_aS_I++;
         if (this.f_bU_I != 0 && requested != 0) {
            this.f_a_Z = true;
         }

         return requested;
      } else if (!init_Z_Z_2(playerChangedKeys, this.f_a_Z && this.f_bU_I == 0, this.f_aS_I, this.table_F, referenceMoved)) {
         this.f_aS_I++;
         return this.f_aR_I;
      } else {
         this.f_aS_I = 0;
         this.f_a_Z = this.f_bU_I != 0 && requested != 0;
         return requested;
      }
   }

   static boolean init_Z_Z_2(boolean playerChangedKeys, boolean correctionEnded, int hold, float bias, boolean referenceMoved) {
      return playerChangedKeys || correctionEnded || referenceMoved || hold >= 4 || Math.abs(bias) >= 36.0F;
   }

   static int init_I_D_I(double component, int previous) {
      double magnitude = Math.abs(component);
      double threshold = previous == 0 ? 0.62 : 0.38;
      if (magnitude < threshold) {
         return 0;
      } else {
         return component < 0.0 ? -1 : 1;
      }
   }

   static double init_D_I_I(int forwardImpulse, int sidewaysImpulse) {
      return forwardImpulse == 0 && sidewaysImpulse == 0 ? 0.0 / 0.0 : Math.toDegrees(Math.atan2(-sidewaysImpulse, forwardImpulse));
   }

   static double init_D_D_3(double deltaDegrees) {
      if (!Double.isFinite(deltaDegrees)) {
         return 0.0 / 0.0;
      }

      double best = 1.7976931348623157E308;

      for (double boundary : write_DArr) {
         best = Math.min(best, Math.abs(Mth.wrapDegrees(deltaDegrees - boundary)));
      }

      return best;
   }

   private void a_V() {
      this.f_aQ_I = 0;
      this.f_aR_I = 0;
      this.f_aS_I = 0;
      this.f_a_Z = false;
      this.store_F = 0.0F;
      this.drain_F = 0.0F;
      this.table_F = 0.0F;
      this.f_aT_I = 0;
      this.f_bX_I = 0;
      this.bind_V();
      this.f_bW_I = 0;
   }

   static float init_F_Vec3_3(Vec3 laneDirection, Vec3 laneOffset, Vec3 horizontalVel) {
      return init_F_Vec3_4(laneDirection, laneOffset, horizontalVel, 0.22, 1.1);
   }

   static float init_F_Vec3_4(Vec3 laneDirection, Vec3 laneOffset, Vec3 horizontalVel, double deadband, double lookahead) {
      Vec3 left = new Vec3(laneDirection.z, 0.0, -laneDirection.x);
      double error = laneOffset.dot(left) - horizontalVel.dot(left) * 4.0;
      if (Math.abs(error) < deadband) {
         return 0.0F;
      }

      Vec3 desired = laneDirection.scale(lookahead).add(left.scale(error));
      return desired.lengthSqr() <= 1.0E-12 ? 0.0F : (float)Mth.clamp(init_D_Vec3_Vec3(laneDirection, desired), -38.0, 38.0);
   }

   private static float init_F_Z_Z(boolean positive, boolean negative) {
      if (positive == negative) {
         return 0.0F;
      } else {
         return positive ? 1.0F : -1.0F;
      }
   }

   private ScaffoldModule.Inner_FileCache init_aI_FileCache() {
      if (this.b_Z() && MC.player != null && this.init_aI_HashUtil != null) {
         Vec3 direction = this.init_aI_HashUtil.load();
         if (init_Z_Vec3_2(direction)) {
            return null;
         }

         int stepX = load_I_D(direction.x);
         int stepZ = load_I_D(direction.z);
         if (stepX == 0 && stepZ == 0) {
            return null;
         }

         int since = ck.init_ck().state_I() - this.f_z_I;
         if (since >= 0 && since <= 10) {
            BlockPos lastPlaced = this.init_ArrayDeque.peekLast();
            if (lastPlaced == null) {
               return null;
            }

            if (!this.init_Z_BlockState_BlockPos(MC.level.getBlockState(lastPlaced), lastPlaced)) {
               return null;
            }

            int rowY = read_I_I_I(lastPlaced.getY(), this.close_I());
            if (BlockPos.containing(MC.player.position()).getY() - 1 < rowY) {
               return null;
            }

            boolean xAxis = stepX != 0;
            BlockPos playerCell = BlockPos.containing(MC.player.position());
            return new ScaffoldModule.Inner_FileCache(
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
      return !this.index_Z() || !this.state_Z;
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

         if (!this.init_Z_BlockState_BlockPos(MC.level.getBlockState(pos), pos)) {
            return xAxis ? pos.getX() : pos.getZ();
         }
      }

      return -2147483648;
   }

   static Vec3 init_Vec3_Vec3_2(Vec3 position, Vec3 velocity, double feetY, int maxTicks) {
      return init_Vec3_Vec3(position, velocity, feetY, maxTicks, 0.0);
   }

   static Vec3 init_Vec3_Vec3(Vec3 position, Vec3 velocity, double feetY, int maxTicks, double impulse) {
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_Vec3_ItemStack(Vec3 predicted, ItemStack stack) {
      if (MC.player != null && MC.level != null) {
         Vec3 velocity = MC.player.getDeltaMovement();
         if (!MC.player.onGround() && velocity.y > 0.0) {
            return null;
         }

         Vec3 lane = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
         boolean jumping = this.queue_Z();
         if (!MC.player.onGround()) {
            int row = this.poll_I();
            Vec3 landing = init_Vec3_Vec3_2(MC.player.position(), velocity, row + 1.0, 12);
            if (landing != null) {
               List<BlockPos> columns = init_List_Vec3(new Vec3(landing.x, row + 1.5, landing.z));
               BlockPos centered = columns.get(0);
               if (MC.level.isOutsideBuildHeight(centered)) {
                  return null;
               }

               if (this.init_Z_BlockState_BlockPos(MC.level.getBlockState(centered), centered)) {
                  return null;
               }

               for (BlockPos cell : columns) {
                  if (!MC.level.isOutsideBuildHeight(cell)) {
                     BlockState state = MC.level.getBlockState(cell);
                     if (state.canBeReplaced() && !this.init_Z_BlockState_BlockPos(state, cell)) {
                        ScaffoldModule.Inner_TableIndex target = this.init_aI_TableIndex_BlockPos_3(cell, predicted, false, lane, jumping);
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_Vec3(Vec3 predicted, Vec3 lane, boolean jumping) {
      Vec3 position = MC.player.position();
      List<BlockPos> columns = init_List_Vec3(position);
      BlockPos centered = columns.get(0);
      if (MC.level.isOutsideBuildHeight(centered)) {
         return null;
      }

      if (this.init_Z_BlockState_BlockPos(MC.level.getBlockState(centered), centered)) {
         return null;
      }

      for (BlockPos support : columns) {
         if (!MC.level.isOutsideBuildHeight(support)) {
            BlockState state = MC.level.getBlockState(support);
            if (state.canBeReplaced() && !this.init_Z_BlockState_BlockPos(state, support)) {
               ScaffoldModule.Inner_TableIndex target = this.init_aI_TableIndex_BlockPos_3(support, predicted, false, lane, jumping);
               if (!this.push(target) && target != null) {
                  return target;
               }
            }
         }
      }

      return null;
   }

   private boolean push(ScaffoldModule.Inner_TableIndex target) {
      return target != null && MC.player != null && !init_Z_aI_TableIndex_4(target, MC.player.getEyePosition(), this.load_Vec3());
   }

   static List<BlockPos> init_List_Vec3(Vec3 position) {
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

      if (!this.flags_Z() || !this.queue_Z()) {
         return false;
      }

      if (!this.init_Z_ItemStack_4(this.init_ItemStack_2())) {
         return false;
      }

      boolean directional = init_Z_KeyMapping(MC.options.keyUp)
         || init_Z_KeyMapping(MC.options.keyDown)
         || init_Z_KeyMapping(MC.options.keyLeft)
         || init_Z_KeyMapping(MC.options.keyRight);
      return !directional || MC.player.horizontalCollision;
   }

   private boolean d_Z() {
      if (this.c_Z()) {
         return true;
      } else {
         Vec3 lane = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
         if (lane == null || init_Z_Vec3_2(lane)) {
            return true;
         } else if (!this.f_c_Z && !this.f_b_Z) {
            int since = load_I_I_I(ck.init_ck().state_I(), this.f_cc_I);
            return since >= 0 && since <= 30;
         } else {
            return true;
         }
      }
   }

   private ce.Inner_DataUtil open_ce_DataUtil() {
      BlockPos support = BlockPos.containing(MC.player.position()).below();
      float yaw = this.read_F();
      float pitch = init_F_Vec3(MC.player.getEyePosition(), support, yaw);
      if (Float.isNaN(pitch)) {
         BlockPos cell = BlockPos.containing(MC.player.position());
         Vec3 point = new Vec3(cell.getX() + 0.5, cell.getY(), cell.getZ() + 0.5);
         pitch = ce.init(point, MC.player.getEyePosition()).load();
      }

      return new ce.Inner_DataUtil(yaw, load_F_F_2(pitch));
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex() {
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
               return this.init_aI_TableIndex_BlockPos(cell, MC.player.position(), false, null, true, Direction.UP);
            }

            cell = below;
         }

         return null;
      } else {
         return null;
      }
   }

   private ScaffoldModule.Inner_TableIndex load_aI_TableIndex() {
      this.push_Str = "--";
      if (MC.player != null && MC.level != null) {
         if (!MC.player.onGround() && this.queue_Z()) {
            if (!this.d_Z()) {
               this.push_Str = "flat";
               return null;
            }

            Vec3 position = MC.player.position();
            Vec3 velocity = MC.player.getDeltaMovement();
            Vec3 lane = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
            StringBuilder fails = new StringBuilder();
            ScaffoldModule.Inner_TableIndex footing = null;
            BlockPos arcLanding = this.load_BlockPos_I(this.close_I());
            BlockPos ownSupport = this.update_BlockPos();

            for (BlockPos support : this.load_List_3()) {
               BlockPos riser = support.above();
               if (!MC.level.isOutsideBuildHeight(riser)) {
                  boolean own = load_Z_BlockPos_BlockPos(support, ownSupport) && lane == null;
                  String fail;
                  if (this.sync(riser)) {
                     fail = "dead";
                  } else if (!MC.level.getBlockState(riser).canBeReplaced()) {
                     fail = "occupied";
                  } else if (own ? !init_Z_Vec3_BlockPos(position, support) : !this.update_Z_Vec3(position, velocity, riser)) {
                     fail = "arc";
                     if (this.get_Z() && !own) {
                        double[] landing = this.load_DArr_Vec3(position, velocity, velocity.y, riser.getY() + 1.0, false, this.push_I());
                        fail = landing == null ? "arc(null)" : String.format(Locale.ROOT, "arc(ovl%.2f)", init_D_DArr_BlockPos(landing, riser));
                     }
                  } else if (!this.init_Z_BlockState_BlockPos(MC.level.getBlockState(support), support)) {
                     ScaffoldModule.Inner_TableIndex sideReach = this.init_aI_TableIndex_BlockPos(riser, position, false, lane, true, null);
                     if (sideReach != null) {
                        this.push_Str = "--";
                        return sideReach;
                     }

                     if (footing == null && support.equals(arcLanding) && !this.sync(support)) {
                        footing = this.init_aI_TableIndex_BlockPos(support, position, false, lane, true, null);
                     }

                     fail = footing == null ? "no-support" : "footing";
                  } else {
                     ScaffoldModule.Inner_TableIndex plan = this.init_aI_TableIndex_BlockPos(riser, position, false, lane, true, Direction.UP);
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

            if (this.get_Z() && fails.length() > 0) {
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

   private static double init_D_DArr_BlockPos(double[] landing, BlockPos cell) {
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
      double[] landing = this.load_DArr_Vec3(position, velocity, grounded ? 0.42 : velocity.y, footingRow + 2.0, grounded, this.push_I());
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
         landing = this.load_DArr_Vec3(position, velocity, takeoffVy, footingRow + 2.0, grounded, this.push_I());
      }

      if (landing == null) {
         landing = this.load_DArr_Vec3(position, velocity, takeoffVy, footingRow + 1.0, grounded, this.push_I());
      }

      return landing == null ? null : new BlockPos(Mth.floor(landing[0]), footingRow, Mth.floor(landing[1]));
   }

   private List<BlockPos> load_List_3() {
      List<BlockPos> candidates = new ArrayList<>(6);
      int footingRow = this.close_I();
      BlockPos foot = this.close_BlockPos();
      BlockPos own = new BlockPos(foot.getX(), footingRow, foot.getZ());
      BlockPos landing = this.load_BlockPos_I(footingRow);
      candidates.addAll(init_List_BlockPos(own, landing, init_Z_Vec3_2(this.init_Vec3_3())));
      Vec3 step = this.init_Vec3_3();
      BlockPos ahead = new BlockPos(own.getX() + load_I_D(step.x), footingRow, own.getZ() + load_I_D(step.z));
      if (!ahead.equals(own) && !candidates.contains(ahead)) {
         candidates.add(ahead);
      }

      int tried = 0;

      for (Iterator<BlockPos> recent = this.init_ArrayDeque.descendingIterator(); recent.hasNext() && tried < 3; tried++) {
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

   static List<BlockPos> init_List_BlockPos_Vec3(BlockPos landing, Vec3 laneDirection) {
      List<BlockPos> connectors = new ArrayList<>(2);
      if (landing != null && laneDirection != null) {
         int stepX = load_I_D(laneDirection.x);
         int stepZ = load_I_D(laneDirection.z);
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos(BlockPos riser, BlockPos landing, Vec3 predicted, ItemStack stack, Vec3 lane) {
      if (riser != null && landing != null) {
         for (BlockPos connector : init_List_BlockPos_Vec3(landing, lane)) {
            Direction face = load_Direction_BlockPos_BlockPos(landing, connector);
            if (face != null) {
               ScaffoldModule.Inner_TableIndex viaStep = this.init_aI_TableIndex_BlockPos_2(riser, predicted, stack, lane, face);
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

   static Direction load_Direction_BlockPos_BlockPos(BlockPos landing, BlockPos connector) {
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

   private ScaffoldModule.Inner_TableIndex load_aI_TableIndex_Vec3_ItemStack(Vec3 predicted, ItemStack stack) {
      if (MC.player != null && MC.level != null && this.f_Z() && this.init_aI_HashUtil != null) {
         Vec3 lane = this.init_aI_HashUtil.load();
         BlockPos landing = this.put_BlockPos;
         if (landing == null) {
            landing = this.read_BlockPos_I(this.close_I());
            if (landing == null || MC.level.isOutsideBuildHeight(landing)) {
               this.update_Str = "unknown";
               return null;
            }

            this.put_BlockPos = landing.immutable();
         }

         boolean supportSolid = this.open(landing);
         int deficit = supportSolid ? 0 : this.load_I_BlockPos(landing);
         BlockPos riser = landing.above();
         boolean riserSolid = !MC.level.isOutsideBuildHeight(riser) && this.open(riser);
         ScaffoldModule.Inner_EventBus need = init_aI_EventBus_Z(supportSolid, deficit, riserSolid);
         if (need == ScaffoldModule.Inner_EventBus.READY || MC.level.isOutsideBuildHeight(riser)) {
            this.add_BlockPos = null;
            this.update_Str = "ready";
            return null;
         }

         if (need == ScaffoldModule.Inner_EventBus.RISER) {
            this.add_BlockPos = null;
            if (!this.d_Z()) {
               this.clear_BlockPos = null;
               this.update_Str = "ready";
               return null;
            }

            this.clear_BlockPos = riser.immutable();
            ScaffoldModule.Inner_TableIndex rise = this.init_aI_TableIndex_BlockPos_2(riser, predicted, stack, lane, Direction.UP);
            if (rise == null) {
               rise = this.init_aI_TableIndex_BlockPos(riser, landing, predicted, stack, lane);
            }

            this.update_Str = rise == null ? "riser-wait" : "riser";
            return rise;
         } else if (need == ScaffoldModule.Inner_EventBus.SUPPORT) {
            this.add_BlockPos = null;
            ScaffoldModule.Inner_TableIndex step = this.init_aI_TableIndex_BlockPos(landing, predicted, stack, lane);
            if (step != null) {
               this.update_Str = "step";
               return step;
            } else {
               ScaffoldModule.Inner_TableIndex support = this.init_aI_TableIndex_BlockPos_2(landing, predicted, stack, lane, null);
               this.update_Str = support == null ? "support-wait" : "support";
               return support;
            }
         } else {
            List<ScaffoldModule.Inner_TableIndex> legs = new ArrayList<>(2);

            for (BlockPos connector : init_List_BlockPos_Vec3(landing, lane)) {
               if (!MC.level.isOutsideBuildHeight(connector)
                  && !this.open(connector)
                  && this.load_I_BlockPos(connector) <= 1
                  && !init_Z_Vec3_6(MC.player.position(), lane, connector, 0.8)) {
                  ScaffoldModule.Inner_TableIndex leg = this.init_aI_TableIndex_BlockPos_2(connector, predicted, stack, lane, null);
                  if (leg != null) {
                     legs.add(leg);
                  }
               }
            }

            if (legs.isEmpty()) {
               this.add_BlockPos = null;
               this.update_Str = "leg-wait";
               return null;
            } else {
               legs.sort(init_Comparator_Vec3_ce_DataUtil(predicted, this.write_ce_DataUtil()));
               ScaffoldModule.Inner_TableIndex leg = init_aI_TableIndex_List(legs, MC.player.getEyePosition(), this.load_Vec3(), landing, this.add_BlockPos);
               this.add_BlockPos = leg.load().immutable();
               this.update_Str = leg == legs.get(0) ? "leg" : "leg-plane";
               return leg;
            }
         }
      } else {
         this.update_Str = "--";
         return null;
      }
   }

   static ScaffoldModule.Inner_TableIndex init_aI_TableIndex_List(List<ScaffoldModule.Inner_TableIndex> legs, Vec3 eye, BlockPos held) {
      ScaffoldModule.Inner_TableIndex best = legs.get(0);
      if (legs.size() >= 2 && best.init_Direction().getAxis().isHorizontal()) {
         double bestPast = init_D_Vec3(eye, best.init_BlockPos(), best.init_Direction());

         for (int i = 1; i < legs.size(); i++) {
            ScaffoldModule.Inner_TableIndex leg = legs.get(i);
            if (leg.init_Direction().getAxis().isHorizontal()) {
               double past = init_D_Vec3(eye, leg.init_BlockPos(), leg.init_Direction());
               if (past > bestPast) {
                  best = leg;
                  bestPast = past;
               }
            }
         }

         if (held != null && !best.load().equals(held)) {
            for (ScaffoldModule.Inner_TableIndex leg : legs) {
               if (leg.load().equals(held) && leg.init_Direction().getAxis().isHorizontal()) {
                  double heldPast = init_D_Vec3(eye, leg.init_BlockPos(), leg.init_Direction());
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

   static ScaffoldModule.Inner_TableIndex init_aI_TableIndex_List(List<ScaffoldModule.Inner_TableIndex> legs, Vec3 eye, Vec3 lead, BlockPos held) {
      return init_aI_TableIndex_List(legs, eye, lead, null, held);
   }

   static ScaffoldModule.Inner_TableIndex init_aI_TableIndex_List(List<ScaffoldModule.Inner_TableIndex> legs, Vec3 eye, Vec3 lead, BlockPos landing, BlockPos held) {
      List<ScaffoldModule.Inner_TableIndex> live = init_List_List(legs, eye, lead);
      if (held == null) {
         ScaffoldModule.Inner_TableIndex successor = load_aI_TableIndex_List(live, eye, lead, landing);
         if (successor != null) {
            return successor;
         }
      }

      return init_aI_TableIndex_List(live, eye, held);
   }

   static ScaffoldModule.Inner_TableIndex load_aI_TableIndex_List(List<ScaffoldModule.Inner_TableIndex> legs, Vec3 eye, Vec3 lead, BlockPos landing) {
      if (legs != null && legs.size() >= 2 && landing != null && eye != null) {
         ScaffoldModule.Inner_TableIndex only = null;
         double bestPast = -1.7976931348623157E308;

         for (ScaffoldModule.Inner_TableIndex leg : legs) {
            bestPast = Math.max(bestPast, init_D_Vec3(eye, leg.init_BlockPos(), leg.init_Direction()));
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

         double past = init_D_Vec3(eye, only.init_BlockPos(), only.init_Direction());
         return bestPast - past <= 0.25 ? only : null;
      } else {
         return null;
      }
   }

   static boolean init_Z_aI_TableIndex(ScaffoldModule.Inner_TableIndex leg, BlockPos landing, Vec3 eye, Vec3 lead) {
      if (leg != null && landing != null) {
         Direction face = load_Direction_BlockPos_BlockPos(landing, leg.load());
         if (face == null) {
            return false;
         }

         Vec3 at = lead == null ? eye : eye.add(lead.scale(2.0));
         return read_Z_Vec3(at, leg.load(), face, leg.init_ce_DataUtil().init());
      } else {
         return false;
      }
   }

   static List<ScaffoldModule.Inner_TableIndex> init_List_List(List<ScaffoldModule.Inner_TableIndex> legs, Vec3 eye, Vec3 lead) {
      if (legs != null && legs.size() >= 2) {
         List<ScaffoldModule.Inner_TableIndex> sighted = new ArrayList<>(legs.size());

         for (ScaffoldModule.Inner_TableIndex leg : legs) {
            if (init_Z_aI_TableIndex_4(leg, eye, lead)) {
               sighted.add(leg);
            }
         }

         return !sighted.isEmpty() && sighted.size() != legs.size() ? sighted : legs;
      } else {
         return legs;
      }
   }

   static boolean init_Z_aI_TableIndex_4(ScaffoldModule.Inner_TableIndex leg, Vec3 eye, Vec3 lead) {
      if (leg != null && eye != null && leg.init_Direction().getAxis().isHorizontal()) {
         float yaw = leg.init_ce_DataUtil().init();

         for (int step = 0; step <= 2; step++) {
            Vec3 at = lead == null ? eye : eye.add(lead.scale(step));
            if (init_D_Vec3(at, leg.init_BlockPos(), leg.init_Direction()) <= 0.0) {
               return true;
            }

            if (read_Z_Vec3(at, leg.init_BlockPos(), leg.init_Direction(), yaw)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos_2(BlockPos candidate, Vec3 predicted, ItemStack stack, Vec3 lane, Direction preferredFace) {
      if (candidate == null || MC.level.isOutsideBuildHeight(candidate)) {
         return this.init_aI_TableIndex_BlockPos(candidate, "oob", null);
      }

      if (this.sync(candidate)) {
         return this.init_aI_TableIndex_BlockPos(candidate, "cooldown", null);
      }

      if (init_Z_Vec3_6(MC.player.position(), lane, candidate, 0.8)) {
         return this.init_aI_TableIndex_BlockPos(candidate, "behind", null);
      }

      BlockState state = MC.level.getBlockState(candidate);
      if (!this.init_Z_BlockState_BlockPos(state, candidate) && state.canBeReplaced()) {
         boolean replaceExisting = !state.isAir() && state.getFluidState().isEmpty();
         if (replaceExisting && !this.init_Z_BlockState_3(state, candidate, stack)) {
            return this.init_aI_TableIndex_BlockPos(candidate, "no-repl", null);
         }

         ScaffoldModule.Inner_FileCache rowLock = this.init_aI_FileCache();
         boolean rising = candidate.getY() > this.close_I();
         if (rowLock != null && !rowLock.init(candidate, rising)) {
            return this.init_aI_TableIndex_BlockPos(candidate, "rowlock", null);
         }

         Direction onlyFace = preferredFace != null ? preferredFace : (rowLock == null ? null : rowLock.init(candidate));
         ScaffoldModule.Inner_TableIndex plan = this.init_aI_TableIndex_BlockPos(candidate, predicted, replaceExisting, lane, true, onlyFace, true);
         return plan != null && this.push(plan)
            ? this.init_aI_TableIndex_BlockPos(candidate, "blind", null)
            : this.init_aI_TableIndex_BlockPos(candidate, plan == null ? "plan" : "ok", plan);
      } else {
         return this.init_aI_TableIndex_BlockPos(candidate, "solid", null);
      }
   }

   static List<BlockPos> load_List_BlockPos_Vec3(BlockPos landing, Vec3 lane) {
      List<BlockPos> candidates = new ArrayList<>(2);
      if (landing != null && lane != null) {
         int backX = -load_I_D(lane.x);
         int backZ = -load_I_D(lane.z);
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos(BlockPos landing, Vec3 predicted, ItemStack stack, Vec3 lane) {
      if (landing != null && lane != null && MC.level != null && this.d_Z()) {
         BlockPos rise = landing.above();
         if (!MC.level.isOutsideBuildHeight(rise) && !this.open(rise)) {
            for (BlockPos step : load_List_BlockPos_Vec3(landing, lane)) {
               if (!MC.level.isOutsideBuildHeight(step) && !this.open(step) && this.open(step.below())) {
                  ScaffoldModule.Inner_TableIndex plan = this.init_aI_TableIndex_BlockPos_2(step, predicted, stack, lane, Direction.UP);
                  if (plan != null) {
                     this.reset_BlockPos = step.immutable();
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos(BlockPos cell, String why, ScaffoldModule.Inner_TableIndex plan) {
      if (this.get_Z() && this.get_Str.length() <= 220) {
         String detail = "plan".equals(why) && this.load_StringBuilder.length() > 0 ? "plan[" + this.load_StringBuilder.toString().trim() + "]" : why;
         if (plan != null && MC.player != null && plan.init_Direction().getAxis().isHorizontal()) {
            detail = detail + String.format(Locale.ROOT, "/past%+.2f", init_D_Vec3(MC.player.getEyePosition(), plan.init_BlockPos(), plan.init_Direction()));
         }

         this.get_Str = ("--".equals(this.get_Str) ? "" : this.get_Str + ",") + init_Str_BlockPos_2(cell) + ":" + detail;
         return plan;
      } else {
         return plan;
      }
   }

   private static String init_Str_BlockPos_2(BlockPos cell) {
      return cell == null ? "--" : cell.getX() + "," + cell.getY() + "," + cell.getZ();
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_ItemStack(ItemStack stack) {
      this.frame_Z = false;
      this.get_Str = "--";
      ScaffoldModule.Inner_TableIndex target = this.load_aI_TableIndex_ItemStack(stack);
      if (this.flags_Z() && target != null && MC.player != null) {
         int stampAge = load_I_I_I(ck.init_ck().state_I(), this.f_ak_I);
         boolean stampFresh = this.tick_BlockPos != null && stampAge >= 0 && stampAge <= 8;
         boolean windowAlive = this.init_Z_aI_TableIndex(target)
            || load_Z_Vec3(MC.player.getEyePosition(), target.init_BlockPos(), target.init_Direction(), target.init_ce_DataUtil().init(), true);
         if (init_Z_Z(
            MC.player.onGround(),
            MC.player.getDeltaMovement().y < 0.0,
            stampFresh,
            target.load().equals(this.tick_BlockPos),
            target.init_Direction().getAxis().isHorizontal(),
            this.sync(target),
            windowAlive
         )) {
            this.stop_Str = "steal-veto:" + target.load().toShortString();
            this.load_aI_TableIndex = null;
            return null;
         }
      }

      target = this.read_aI_TableIndex_aI_TableIndex(target);
      if (this.flags_Z() && !this.frame_Z) {
         this.read_V_aI_TableIndex(target);
      }

      return target;
   }

   private ScaffoldModule.Inner_TableIndex read_aI_TableIndex_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (target != null && this.flags_Z() && MC.player != null && MC.level != null) {
         BlockPos placed = target.load();
         BlockState state = MC.level.getBlockState(placed);
         boolean replaceExisting = !state.isAir() && state.getFluidState().isEmpty();
         if (!init_Z_Direction(target.init_Direction(), replaceExisting, this.init_Z_BlockPos_4(placed.below()))) {
            return target;
         } else {
            String planFail = this.reset_Str;
            ScaffoldModule.Inner_TableIndex up = this.init_aI_TableIndex_BlockPos(
               placed,
               this.load_Vec3_aI_HashUtil(this.init_aI_HashUtil),
               replaceExisting,
               this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load(),
               this.f_Z(),
               Direction.UP
            );
            this.reset_Str = planFail;
            if (up != null && up.load().equals(placed) && up.init_Direction() == Direction.UP) {
               this.get_BlockPos = placed.immutable();
               return up;
            } else {
               return target;
            }
         }
      } else {
         return target;
      }
   }

   static List<ScaffoldModule.Inner_ListPool> init_List_BlockPos(BlockPos landing, int maxMissing, Predicate<BlockPos> supported, Predicate<BlockPos> open) {
      if (landing != null && maxMissing > 0 && !supported.test(landing) && open.test(landing)) {
         int limit = Math.min(6, maxMissing);
         ArrayDeque<ScaffoldModule.Inner_ListPool> queue = new ArrayDeque<>();
         Set<BlockPos> seen = new HashSet<>();
         List<ScaffoldModule.Inner_ListPool> frontier = new ArrayList<>();
         queue.add(new ScaffoldModule.Inner_ListPool(landing.immutable(), 1));
         seen.add(landing);

         while (!queue.isEmpty()) {
            ScaffoldModule.Inner_ListPool link = queue.removeFirst();
            boolean anchored = false;

            for (Direction side : Direction.values()) {
               BlockPos adjacent = link.init_BlockPos().relative(side);
               if (supported.test(adjacent)) {
                  anchored = true;
               } else if (side.getAxis().isHorizontal() && link.init_I() < limit && seen.add(adjacent) && open.test(adjacent)) {
                  queue.addLast(new ScaffoldModule.Inner_ListPool(adjacent, link.init_I() + 1));
               }
            }

            if (anchored) {
               frontier.add(link);
            }
         }

         frontier.sort(
            Comparator.comparingInt(ScaffoldModule.Inner_ListPool::init_I)
               .thenComparingInt(linkx -> linkx.init_BlockPos().getX())
               .thenComparingInt(linkx -> linkx.init_BlockPos().getZ())
         );
         return frontier;
      } else {
         return List.of();
      }
   }

   static boolean init_Z_Str(String mode, boolean diagonal, boolean ascending) {
      return "Telly".equals(mode) && diagonal && !ascending;
   }

   private ScaffoldModule.Inner_TableIndex read_aI_TableIndex_Vec3_ItemStack(Vec3 predicted, ItemStack stack) {
      if (init_Z_Str(this.choice("mode"), this.state_Z, this.d_Z())
         && MC.player != null
         && MC.level != null
         && this.init_aI_HashUtil != null
         && (!MC.player.onGround() || this.queue_Z())) {
         int row = this.close_I();
         if (row != -2147483648 && !(MC.player.getY() < row + 1.0)) {
            boolean grounded = MC.player.onGround();
            double[] landing = this.load_DArr_Vec3(
               MC.player.position(), MC.player.getDeltaMovement(), grounded ? 0.42 : MC.player.getDeltaMovement().y, row + 1.0, grounded, this.push_I()
            );
            if (landing == null) {
               return null;
            }

            BlockPos goal = new BlockPos(Mth.floor(landing[0]), row, Mth.floor(landing[1]));
            List<ScaffoldModule.Inner_ListPool> links = init_List_BlockPos(
               goal, 6, this::poll, cell -> !MC.level.isOutsideBuildHeight(cell) && MC.level.hasChunkAt(cell) && MC.level.getBlockState(cell).canBeReplaced()
            );
            if (links.isEmpty()) {
               return null;
            }

            this.put_BlockPos = goal.immutable();
            Vec3 lane = this.init_aI_HashUtil.load();
            List<ScaffoldModule.Inner_TableIndex> plans = new ArrayList<>();
            int bestMissing = 2147483647;

            for (ScaffoldModule.Inner_ListPool link : links) {
               if (link.init_I() > bestMissing) {
                  break;
               }

               if (!this.sync(link.init_BlockPos())) {
                  ScaffoldModule.Inner_TableIndex plan = this.init_aI_TableIndex_BlockPos_2(link.init_BlockPos(), predicted, stack, lane, null);
                  if (plan != null && !this.poll(plan)) {
                     bestMissing = link.init_I();
                     plans.add(plan);
                  }
               }
            }

            if (plans.isEmpty()) {
               return null;
            }

            plans.sort(init_Comparator_Vec3_ce_DataUtil(predicted, this.write_ce_DataUtil()));
            BlockPos previous = this.load_aI_TableIndex == null ? null : this.load_aI_TableIndex.load();
            BlockPos held = previous != null && plans.stream().anyMatch(planx -> planx.load().equals(previous)) ? previous : null;
            return init_aI_TableIndex_List(plans, MC.player.getEyePosition(), this.load_Vec3(), goal, held);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private ScaffoldModule.Inner_TableIndex load_aI_TableIndex_ItemStack(ItemStack stack) {
      if (!this.init_Z_ItemStack_4(stack)) {
         return null;
      }

      Vec3 predicted = this.load_Vec3_aI_HashUtil(this.init_aI_HashUtil);
      BlockPos predictedBase = init_BlockPos_Vec3(predicted);
      if (this.flags_Z()) {
         ScaffoldModule.Inner_TableIndex tower = this.init_aI_TableIndex();
         if (tower != null) {
            this.load_aI_TableIndex = tower;
            this.f_aG_I = ck.init_ck().state_I();
            this.f_aH_I = 0;
            this.stop_Str = "tower:" + tower.load().toShortString();
            return tower;
         }

         ScaffoldModule.Inner_TableIndex tellyLanding = this.read_aI_TableIndex_Vec3_ItemStack(predicted, stack);
         if (tellyLanding != null) {
            this.load_aI_TableIndex = tellyLanding;
            this.f_aG_I = ck.init_ck().state_I();
            this.f_aH_I = 0;
            this.stop_Str = "telly-floor:" + tellyLanding.load().toShortString();
            return tellyLanding;
         }

         ScaffoldModule.Inner_TableIndex rescue = this.init_aI_TableIndex_Vec3_ItemStack(predicted, stack);
         if (rescue != null) {
            this.f_aU_I = ck.init_ck().state_I();
            this.load_aI_TableIndex = rescue;
            this.f_aG_I = ck.init_ck().state_I();
            this.f_aH_I = 0;
            this.stop_Str = "rescue:" + rescue.load().toShortString();
            return rescue;
         }

         ScaffoldModule.Inner_TableIndex riser = this.load_aI_TableIndex();
         if (riser != null) {
            this.load_aI_TableIndex = riser;
            this.f_aG_I = ck.init_ck().state_I();
            this.f_aH_I = 0;
            this.stop_Str = "riser:" + riser.load().toShortString();
            return riser;
         }

         ScaffoldModule.Inner_TableIndex reservation = this.load_aI_TableIndex_Vec3_ItemStack(predicted, stack);
         if (reservation != null && !this.open(reservation) && !this.poll(reservation)) {
            this.load_aI_TableIndex = reservation;
            this.f_aG_I = ck.init_ck().state_I();
            this.f_aH_I = 0;
            this.stop_Str = "reserve-" + this.update_Str + ":" + reservation.load().toShortString();
            return reservation;
         }

         if (MC.player != null && !MC.player.onGround() && MC.player.getDeltaMovement().y < 0.0 && this.i()) {
            ScaffoldModule.Inner_TableIndex catchBelow = this.init_aI_TableIndex_Vec3(
               predicted, this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load(), this.queue_Z()
            );
            if (catchBelow != null) {
               this.f_aU_I = ck.init_ck().state_I();
               this.load_aI_TableIndex = catchBelow;
               this.f_aG_I = ck.init_ck().state_I();
               this.f_aH_I = 0;
               this.stop_Str = "catch:" + catchBelow.load().toShortString();
               return catchBelow;
            }
         }

         boolean rowLocked = this.init_aI_FileCache() != null;
         if (!rowLocked) {
            predictedBase = this.load_BlockPos_BlockPos(predictedBase);
            ScaffoldModule.Inner_TableIndex drift = this.init_aI_TableIndex_BlockPos(predictedBase);
            if (drift != null) {
               this.load_aI_TableIndex = drift;
               this.f_aG_I = ck.init_ck().state_I();
               this.f_aH_I = 0;
               return drift;
            }
         }

         ScaffoldModule.Inner_TableIndex held = this.read_aI_TableIndex();
         if (held != null && !this.poll(held)) {
            this.stop_Str = "hold:" + held.load().toShortString();
            return held;
         }

         ScaffoldModule.Inner_TableIndex sticky = this.init_aI_TableIndex_Vec3(predicted);
         if (sticky != null && !this.open(sticky) && !this.poll(sticky)) {
            this.stop_Str = "sticky";
            return sticky;
         }

         ScaffoldModule.Inner_TableIndex fresh = this.init_aI_TableIndex_BlockPos_2(predictedBase, predicted, stack);
         String freshTier = rowLocked ? "row" : "lane";
         if (fresh != null && this.poll(fresh)) {
            fresh = null;
            this.stop_Str = freshTier + "-uncatch";
         } else {
            this.stop_Str = fresh != null ? freshTier + ":" + fresh.load().toShortString() : freshTier + "-null";
         }

         this.load_aI_TableIndex = fresh;
         this.f_aG_I = ck.init_ck().state_I();
         this.f_aH_I = 0;
         return fresh;
      } else {
         return this.init_aI_TableIndex_BlockPos_2(predictedBase, predicted, stack);
      }
   }

   private BlockPos load_BlockPos_BlockPos(BlockPos base) {
      if (base != null && this.set_BlockPos != null && this.init_aI_HashUtil != null) {
         Vec3 direction = this.init_aI_HashUtil.load();
         if (init_Z_Vec3_2(direction)) {
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
      int tick = ck.init_ck().state_I();
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

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_Vec3(Vec3 predicted) {
      ScaffoldModule.Inner_TableIndex sticky = this.load_aI_TableIndex;
      if (sticky == null) {
         return null;
      }

      this.load_aI_TableIndex = null;
      int tick = ck.init_ck().state_I();
      if (this.f_aG_I >= 0 && tick - this.f_aG_I <= 6) {
         BlockPos placed = sticky.load();
         if (!MC.level.isOutsideBuildHeight(placed) && !this.init_Z_BlockState_BlockPos(MC.level.getBlockState(placed), placed)) {
            BlockPos support = sticky.init_BlockPos();
            if (!MC.level.isOutsideBuildHeight(support) && !MC.level.getBlockState(support).canBeReplaced()) {
               if (MC.player != null && MC.player.onGround() && placed.getY() < BlockPos.containing(MC.player.position()).getY() - 1) {
                  return null;
               }

               Vec3 laneDirection = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
               if (MC.player != null && !this.h_Z() && init_Z_Vec3_6(MC.player.position(), laneDirection, placed, 0.8)) {
                  return null;
               }

               if (sticky.init_Direction().getAxis().isVertical() && MC.player != null && !write(MC.player.position(), MC.player.getDeltaMovement(), placed)) {
                  return null;
               }

               if (MC.player != null && init_Z_Vec3(MC.player.position(), support, 2.5)) {
                  return null;
               }

               boolean jumping = this.f_Z();
               ScaffoldModule.Inner_FileCache rowLock = this.init_aI_FileCache();
               if (rowLock != null && !rowLock.init(placed, this.read_Z_Z(jumping))) {
                  return null;
               }

               ScaffoldModule.Inner_TableIndex replanned = this.init_aI_TableIndex_BlockPos(placed, predicted, false, laneDirection, jumping, sticky.init_Direction());
               if (replanned == null || !replanned.init_BlockPos().equals(support) || replanned.init_Direction() != sticky.init_Direction()) {
                  return null;
               }

               if (MC.player != null && !init_Z_aI_TableIndex_4(replanned, MC.player.getEyePosition(), this.load_Vec3())) {
                  return null;
               }

               if (replanned.init_Direction().getAxis().isHorizontal() && MC.player != null) {
                  boolean bandExit = init_Z_Vec3_2(MC.player.getEyePosition(), replanned.init_BlockPos(), replanned.init_Direction());
                  boolean outOfReach = update_Z_F(
                     init_F_Vec3(MC.player.getEyePosition(), replanned.init_BlockPos(), replanned.init_Direction(), replanned.init_ce_DataUtil().init())
                  );
                  if (this.f_aI_I != tick) {
                     this.f_aI_I = tick;
                     this.f_aH_I = bandExit ? this.f_aH_I + 1 : 0;
                     this.f_ca_I = outOfReach ? this.f_ca_I + 1 : 0;
                  }

                  if (this.f_aH_I >= 2) {
                     this.f_aH_I = 0;
                     return null;
                  }

                  if (this.f_ca_I >= 2) {
                     this.f_ca_I = 0;
                     return null;
                  }
               }

               this.load_aI_TableIndex = replanned;
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

   static boolean update_Z_F(float requiredPitch) {
      return Float.isFinite(requiredPitch) && requiredPitch > 89.5F;
   }

   private float sync(float freeYaw) {
      if (this.load_I_8() == -1) {
         return freeYaw;
      } else {
         return this.f_ai_I < 1 && !this.h_Z() ? write_F_F_F(this.load_F_2(), freeYaw) : freeYaw;
      }
   }

   private boolean e_Z() {
      return this.init_Z_ce_DataUtil(this.load_ce_DataUtil);
   }

   private boolean init_Z_ce_DataUtil(ce.Inner_DataUtil rotation) {
      if (rotation != null && this.load_I_8() != -1) {
         return this.f_ai_I < 1 && !this.h_Z() ? Math.abs(update_F_F_F(this.load_F_2(), rotation.init())) > 12.0F : false;
      } else {
         return false;
      }
   }

   private boolean pop(ScaffoldModule.Inner_TableIndex target) {
      return target != null && MC.player != null && target.init_Direction().getAxis().isHorizontal()
         ? update_Z_F(init_F_Vec3(MC.player.getEyePosition(), target.init_BlockPos(), target.init_Direction(), target.init_ce_DataUtil().init()))
         : false;
   }

   static boolean init_Z_Vec3_2(Vec3 eye, BlockPos support, Direction face) {
      boolean xAxisFace = face.getStepX() != 0;
      double lateral = xAxisFace ? eye.z : eye.x;
      double edge = xAxisFace ? support.getZ() : support.getX();
      return lateral < edge || lateral > edge + 1.0;
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos(BlockPos laneBase) {
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

         if (this.init_Z_BlockState_BlockPos(MC.level.getBlockState(driftBase), driftBase)) {
            return null;
         }

         Vec3 future = MC.player.position().add(velocity.x * 2.0, 0.0, velocity.z * 2.0);
         Vec3 laneDirection = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
         boolean jumping = this.f_Z();
         if (!this.h_Z() && init_Z_Vec3_6(MC.player.position(), laneDirection, driftBase, 0.8)) {
            return null;
         }

         ScaffoldModule.Inner_TableIndex direct = this.init_aI_TableIndex_BlockPos_3(driftBase, future, false, laneDirection, jumping);
         if (direct != null) {
            this.stop_Str = "guard:" + driftBase.toShortString();
            return direct;
         }

         List<ScaffoldModule.Inner_TableIndex> seedPlans = new ArrayList<>(4);

         for (Direction direction : Plane.HORIZONTAL) {
            BlockPos seed = driftBase.relative(direction);
            if (!MC.level.isOutsideBuildHeight(seed) && !(Vec3.atCenterOf(seed).subtract(MC.player.position()).horizontalDistance() > 2.0)) {
               BlockState seedState = MC.level.getBlockState(seed);
               if (seedState.isAir()) {
                  ScaffoldModule.Inner_TableIndex seeded = this.init_aI_TableIndex_BlockPos_3(seed, future, false, laneDirection, jumping);
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

         seedPlans.sort(init_Comparator_Vec3_ce_DataUtil(future, this.write_ce_DataUtil()));
         this.stop_Str = "gseed:" + seedPlans.get(0).load().toShortString();
         return seedPlans.get(0);
      } else {
         return null;
      }
   }

   static Comparator<ScaffoldModule.Inner_TableIndex> init_Comparator_Vec3_ce_DataUtil(Vec3 future, ce.Inner_DataUtil posture) {
      return Comparator.<ScaffoldModule.Inner_TableIndex>comparingDouble(plan -> Math.ceil(Vec3.atCenterOf(plan.load()).distanceTo(future) / 0.5))
         .thenComparingDouble(plan -> init_D_ce_DataUtil_ce_DataUtil(posture, plan.init_ce_DataUtil()));
   }

   static BlockPos init_BlockPos_Vec3(Vec3 position) {
      return BlockPos.containing(position).below();
   }

   private BlockPos read_BlockPos_BlockPos(BlockPos realBase) {
      if (MC.player == null || !MC.player.onGround()) {
         return null;
      } else if (this.init_aI_HashUtil == null) {
         return null;
      } else {
         Vec3 direction = this.init_aI_HashUtil.load();
         if (direction == null) {
            return null;
         } else {
            int stepX = load_I_D(direction.x);
            int stepZ = load_I_D(direction.z);
            if (stepX == 0 && stepZ == 0) {
               return null;
            } else {
               BlockPos ahead = realBase.offset(stepX, 0, stepZ);
               if (MC.level.isOutsideBuildHeight(ahead)) {
                  return null;
               } else {
                  return this.init_Z_BlockState_BlockPos(MC.level.getBlockState(ahead), ahead) ? null : ahead;
               }
            }
         }
      }
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos_2(BlockPos base, Vec3 plannedPosition, ItemStack stack) {
      if (base == null) {
         this.reset_Str = "base-null";
         return null;
      }

      if (MC.level.isOutsideBuildHeight(base)) {
         this.reset_Str = "base-outside-height " + base.toShortString();
         return null;
      }

      BlockState baseState = MC.level.getBlockState(base);
      if (this.init_Z_BlockState_BlockPos(baseState, base)) {
         BlockPos realBase = init_BlockPos_Vec3(MC.player.position());
         if (this.flags_Z()
            && !realBase.equals(base)
            && !MC.level.isOutsideBuildHeight(realBase)
            && !this.init_Z_BlockState_BlockPos(MC.level.getBlockState(realBase), realBase)) {
            base = realBase;
         } else {
            BlockPos ahead = this.read_BlockPos_BlockPos(realBase);
            if (ahead == null) {
               this.reset_Str = "base-solid " + base.toShortString();
               return null;
            }

            base = ahead;
         }
      }

      Vec3 laneDirection = this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load();
      boolean jumping = this.f_Z();
      boolean diagonal = init_Z_Vec3_2(laneDirection);
      List<BlockPos> offsets = this.flags_Z()
         ? this.init_List_List(init_List_10(), base, plannedPosition, this.init_aI_HashUtil)
         : this.init_List_List(init_List, base, plannedPosition, this.init_aI_HashUtil);
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
      this.reset_Str = "";
      ScaffoldModule.Inner_FileCache rowLock = this.flags_Z() ? this.init_aI_FileCache() : null;
      boolean gateRise = this.flags_Z() && !this.h_Z();
      Vec3 riseFrom = MC.player.position();
      Vec3 riseCarry = MC.player.getDeltaMovement();
      boolean gateColumn = this.flags_Z() && this.f_cb_I != -2147483648;
      boolean riseAllowed = this.read_Z_Z(jumping);
      boolean preferHeight = gateRise && riseAllowed;
      int footingRow = this.close_I();
      int planTick = ck.init_ck().state_I();
      ScaffoldModule.Inner_TableIndex firstPlan = null;

      for (BlockPos offset : offsets) {
         BlockPos candidate = base.offset(offset);
         if (offset.getY() > 0 && MC.player.onGround() && !jumping) {
            gateAbove++;
         } else if (gateRise
            && offset.getY() > 0
            && init_Z_Vec3_BlockPos(riseFrom, candidate)
            && !init_Z_I_7(this.load_I_8(), candidate.below(), init_Z_Vec3_5(riseFrom, laneDirection, candidate), this::open)) {
            gateFloor++;
         } else if (gateRise && init_Z_Vec3_6(riseFrom, laneDirection, candidate, 0.8)) {
            gateBehind++;
         } else if (gateColumn
            && candidate.getY() > this.f_cb_I
            && init_Z_Vec3_BlockPos(riseFrom, candidate)
            && !init_Z_Vec3_5(riseFrom, riseCarry, candidate, 3, 0.15)) {
            gateColumnHold++;
         } else if (init_Z_Z(preferHeight, candidate.getY(), footingRow)
            && !load_Z_Vec3_2(riseFrom, riseCarry, candidate)
            && !write(riseFrom, riseCarry, candidate)) {
            gateRiseVeto++;
         } else if (this.flags_Z() && init_Z_I_3(candidate.getY(), this.poll_I(), this.h_Z())) {
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
               if (this.init_Z_BlockState_BlockPos(candidateState, candidate)) {
                  solidSkips++;
               } else {
                  boolean replaceExisting = !candidateState.isAir() && candidateState.getFluidState().isEmpty();
                  if (replaceExisting && !this.init_Z_BlockState_3(candidateState, candidate, stack)) {
                     replaceSkips++;
                  } else {
                     ScaffoldModule.Inner_TableIndex target = this.init_aI_TableIndex_BlockPos(
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
                        firstPlanDetail = candidate.toShortString() + "{" + this.load_StringBuilder.toString().trim() + "}";
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

      this.reset_Str = "base="
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
         + (this.reset_Str.isEmpty() ? "" : " [" + this.reset_Str + "]")
         + (firstPlanDetail == null ? "" : " first=" + firstPlanDetail);
      return null;
   }

   static List<BlockPos> init_List_10() {
      return init_List;
   }

   static double init_D_Direction_Vec3(Direction face, Vec3 lane) {
      if (face != null && lane != null) {
         double length = Math.sqrt(lane.x * lane.x + lane.z * lane.z);
         return length < 1.0E-6 ? 0.0 : (face.getStepX() * lane.x + face.getStepZ() * lane.z) / length;
      } else {
         return 0.0;
      }
   }

   private static boolean init_Z_Vec3_2(Vec3 direction) {
      return direction != null && load_I_D(direction.x) != 0 && load_I_D(direction.z) != 0;
   }

   private static int load_I_D(double component) {
      if (component > 1.0E-6) {
         return 1;
      } else {
         return component < -1.0E-6 ? -1 : 0;
      }
   }

   private List<BlockPos> init_List_List(List<BlockPos> source, BlockPos base, Vec3 predictedPosition, ScaffoldModule.Inner_HashUtil optimalLine) {
      List<BlockPos> ordered = new ArrayList<>(source);
      ordered.sort(
         Comparator.<BlockPos>comparingInt(ScaffoldModule::init_I_BlockPos)
            .thenComparingDouble(offset -> this.init_D_BlockPos(base.offset(offset), predictedPosition, optimalLine))
            .thenComparingDouble(offset -> offset.distSqr(BlockPos.ZERO))
            .thenComparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getX)
            .thenComparingInt(Vec3i::getZ)
      );
      return ordered;
   }

   static int init_I_BlockPos(BlockPos offset) {
      return BlockPos.ZERO.equals(offset) ? 0 : 1;
   }

   private double init_D_BlockPos(BlockPos pos, Vec3 predictedPosition, ScaffoldModule.Inner_HashUtil optimalLine) {
      VoxelShape shape = MC.level.getBlockState(pos).getShape(MC.level, pos, CollisionContext.of(MC.player));
      if (shape.isEmpty()) {
         return optimalLine == null ? Vec3.atCenterOf(pos).distanceToSqr(predictedPosition) : load_D_aI_HashUtil_Vec3(optimalLine, Vec3.atCenterOf(pos));
      }

      double best = 1.0 / 0.0;

      for (AABB local : shape.toAabbs()) {
         AABB box = local.move(pos);
         double distance = optimalLine == null ? init_D_Vec3_AABB(predictedPosition, box) : init_D_aI_HashUtil_AABB(optimalLine, box);
         best = Math.min(best, distance);
      }

      return best;
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos_3(
      BlockPos candidate, Vec3 plannedPosition, boolean replaceExisting, Vec3 laneDirection, boolean jumping
   ) {
      return this.init_aI_TableIndex_BlockPos(candidate, plannedPosition, replaceExisting, laneDirection, jumping, null);
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos(
      BlockPos candidate, Vec3 plannedPosition, boolean replaceExisting, Vec3 laneDirection, boolean jumping, Direction onlyFace
   ) {
      return this.init_aI_TableIndex_BlockPos(candidate, plannedPosition, replaceExisting, laneDirection, jumping, onlyFace, false);
   }

   private ScaffoldModule.Inner_TableIndex init_aI_TableIndex_BlockPos(
      BlockPos candidate, Vec3 plannedPosition, boolean replaceExisting, Vec3 laneDirection, boolean jumping, Direction onlyFace, boolean planeLead
   ) {
      ScaffoldModule.Inner_CharEncoder bestPlan = null;
      double bestFaceAngle = 1.0 / 0.0;
      this.load_StringBuilder.setLength(0);
      if (!replaceExisting && MC.player != null) {
         boolean catchMode = this.push(candidate);
         if (!init_Z_AABB_5(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), candidate, catchMode)) {
            this.reset_Str = "inside-player";
            this.load_StringBuilder.append("ip:").append(init_Str_AABB(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), candidate, catchMode));
            return null;
         }
      }

      Vec3 aimPosition = init_Vec3_Vec3_3(plannedPosition, MC.player.position(), this.flags_Z());
      Vec3 aimEye = aimPosition.add(0.0, MC.player.getEyeHeight(), 0.0);
      Vec3 selectionPosition = read_Vec3_Vec3_Vec3(plannedPosition, aimPosition);
      Vec3 selectionEye = selectionPosition.add(0.0, MC.player.getEyeHeight(), 0.0);
      Vec3 planeLeadEye = null;
      if (planeLead && this.flags_Z() && !MC.player.onGround()) {
         Vec3 travel = MC.player.getDeltaMovement();
         planeLeadEye = aimEye.add(travel.x * 4.0, 0.0, travel.z * 4.0);
      }

      ce.Inner_DataUtil from = this.flags_Z() ? this.write_ce_DataUtil() : this.poll_ce_DataUtil();
      boolean pinnable = this.flags_Z() && MC.player != null;
      Vec3 pinEye = pinnable ? MC.player.getEyePosition().add(this.load_Vec3()) : null;
      Vec3 pinLead = pinnable ? this.load_Vec3() : Vec3.ZERO;
      float pinYaw = pinnable ? this.read_F() : 0.0F;
      boolean heldPinStillPins = pinnable
         && this.write_Direction != null
         && init_Z_BlockPos_6(this.prev_BlockPos, this.write_Direction, candidate)
         && init_Z_Vec3_2(pinEye, pinLead, this.prev_BlockPos, this.write_Direction, pinYaw, true, 2);
      boolean bestFacePins = false;
      ScaffoldModule.Inner_StreamUtil bestSample = null;
      ScaffoldModule.Inner_FileCache visibilityLock = this.flags_Z() ? this.init_aI_FileCache() : null;
      boolean lockPinnedFace = visibilityLock != null && onlyFace != null && onlyFace == visibilityLock.init(candidate);
      boolean requireExactVisibility = !lockPinnedFace && write(this.flags_Z(), this.flush_Z, MC.player.onGround(), jumping);
      double visibilityReach = requireExactVisibility ? Math.max(MC.player.blockInteractionRange(), MC.player.entityInteractionRange()) : 0.0;

      for (Direction face : Direction.values()) {
         if (onlyFace == null || face == onlyFace) {
            char faceCode = face.getName().charAt(0);
            BlockPos supportPos = replaceExisting ? candidate : candidate.relative(face.getOpposite());
            if (!MC.level.isOutsideBuildHeight(supportPos)) {
               BlockState supportState = MC.level.getBlockState(supportPos);
               if (!replaceExisting && supportState.canBeReplaced()) {
                  this.reset_Str = "no-support(" + face + ")";
                  this.load_StringBuilder.append(faceCode).append(":ns ");
               } else if (this.flags_Z()
                  && this.b_Z()
                  && face.getAxis().isHorizontal()
                  && laneDirection != null
                  && !init_Z_Vec3_2(laneDirection)
                  && init_D_Direction_Vec3(face, laneDirection) <= -0.5) {
                  this.reset_Str = "reach-around(" + face + ")";
                  this.load_StringBuilder.append(faceCode).append(":ra ");
               } else {
                  Vec3 faceCenter = Vec3.atCenterOf(supportPos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
                  Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
                  boolean leadPasses = planeLeadEye != null && planeLeadEye.subtract(faceCenter).dot(normal) >= 0.0;
                  if (!leadPasses && selectionEye.subtract(faceCenter).dot(normal) < 0.0 && aimEye.subtract(faceCenter).dot(normal) < 0.0) {
                     this.reset_Str = "normal-side(" + face + ")";
                     this.load_StringBuilder.append(faceCode).append(":sd ");
                  } else if (this.flags_Z() && this.init_Z_BlockPos_Direction(supportPos, face)) {
                     this.reset_Str = "self-occ(" + face + ")";
                     this.load_StringBuilder.append(faceCode).append(":so ");
                  } else {
                     boolean facePins = pinnable
                        && init_Z_Vec3_2(pinEye, pinLead, supportPos, face, pinYaw, this.load_Z_BlockPos_Direction(supportPos, face), 2);
                     if (facePins && close_Z_Z_Z(heldPinStillPins, this.load_Z_BlockPos_Direction(supportPos, face))) {
                        facePins = false;
                     }

                     ce.Inner_DataUtil faceRotation = ce.init(faceCenter, aimEye);
                     double faceAngle = init_D_ce_DataUtil_ce_DataUtil(from, faceRotation);
                     if (this.flags_Z()
                        && face.getAxis().isHorizontal()
                        && aimPosition.y > supportPos.getY() + 1.0 + 1.0
                        && this.load_Z_Vec3(aimEye, supportPos, face, faceRotation.init())) {
                        this.reset_Str = "pitch-cap(" + face + ")";
                        this.load_StringBuilder.append(faceCode).append(":pc ");
                     } else if (init_Z_Z_2(facePins, faceAngle, bestFacePins, bestFaceAngle)) {
                        VoxelShape shape = supportState.getShape(MC.level, supportPos, CollisionContext.of(MC.player));
                        ScaffoldModule.Inner_StreamUtil planSample = null;

                        for (AABB localBox : shape.toAabbs()) {
                           ScaffoldModule.Inner_DataUtil sampledFace = ScaffoldModule.Inner_DataUtil.init(localBox, face);
                           ScaffoldModule.Inner_DataUtil searchFace = sampledFace;
                           if (searchFace.write().y >= 0.9) {
                              ScaffoldModule.Inner_DataUtil truncated = searchFace.init(0.6);
                              if (truncated.init_D() > 1.0E-9) {
                                 searchFace = truncated;
                              }
                           }

                           Vec3 point = this.init_Vec3_aI_DataUtil(searchFace, sampledFace, supportPos, aimEye, from, this.init_aI_HashUtil);
                           if (point == null) {
                              this.reset_Str = "no-point(" + face + ")";
                              this.load_StringBuilder.append(faceCode).append(":np ");
                           } else {
                              if (requireExactVisibility) {
                                 Vec3 candidatePoint = point.add(supportPos.getX(), supportPos.getY(), supportPos.getZ());
                                 ce.Inner_DataUtil visibleRotation = ce.init(candidatePoint, aimEye);
                                 BlockHitResult visibleHit = this.init_BlockHitResult_ce_DataUtil_D(visibleRotation, visibilityReach);
                                 if (visibleHit == null || !visibleHit.getBlockPos().equals(supportPos) || visibleHit.getDirection() != face) {
                                    this.reset_Str = "visibility(" + face + ")";
                                    this.load_StringBuilder.append(faceCode).append(":vs ");
                                    continue;
                                 }
                              }

                              ScaffoldModule.Inner_StreamUtil sample = new ScaffoldModule.Inner_StreamUtil(sampledFace, point, face);
                              if (planSample == null || init_I_aI_StreamUtil_aI_StreamUtil(sample, planSample) > 0) {
                                 planSample = sample;
                              }
                           }
                        }

                        if (planSample != null) {
                           bestFaceAngle = faceAngle;
                           bestFacePins = facePins;
                           bestPlan = new ScaffoldModule.Inner_CharEncoder(supportPos.immutable(), face);
                           bestSample = planSample;
                        }
                     }
                  }
               }
            }
         }
      }

      if (bestPlan != null && bestSample != null) {
         Vec3 worldPoint = bestSample.init_Vec3().add(bestPlan.init_BlockPos().getX(), bestPlan.init_BlockPos().getY(), bestPlan.init_BlockPos().getZ());
         ScaffoldModule.Inner_FileCache goalLock = this.flags_Z() ? this.init_aI_FileCache() : null;
         Direction goalPinnedFace = goalLock == null ? null : goalLock.init(candidate);
         ce.Inner_DataUtil rotation;
         if (this.flags_Z() && MC.player != null && !MC.player.onGround()) {
            Vec3 airEye = MC.player.getEyePosition().add(this.load_Vec3());
            float airPitch = 0.0F / 0.0F;
            float airYaw = pinYaw;
            if (bestFacePins && bestPlan.init_Direction().getAxis().isHorizontal()) {
               this.start_Str = "air-pin";
               if (this.load_I_8() != -1) {
                  float landing = load_F_Vec3(
                     MC.player.getEyePosition(), this.load_Vec3(), bestPlan.init_BlockPos(), bestPlan.init_Direction(), airYaw, this.load_F_2()
                  );
                  if (!Float.isNaN(landing)) {
                     airYaw = landing;
                     this.start_Str = "air-land";
                  }

                  float flip = init_F_Vec3_3(MC.player.getEyePosition(), this.load_Vec3(), bestPlan.init_BlockPos(), bestPlan.init_Direction(), airYaw);
                  if (!Float.isNaN(flip)) {
                     airYaw = flip;
                     this.start_Str = "air-flip";
                  }
               }

               airPitch = init_F_Vec3(airEye, MC.player.getEyePosition(), bestPlan.init_BlockPos(), bestPlan.init_Direction(), airYaw, 89.5F);
            } else if (bestPlan.init_Direction() == Direction.UP) {
               airPitch = init_F_Vec3(airEye, bestPlan.init_BlockPos(), pinYaw);
               if (Float.isNaN(airPitch)) {
                  airPitch = init_F_Vec3(MC.player.getEyePosition(), bestPlan.init_BlockPos(), pinYaw);
               }

               if (!Float.isNaN(airPitch)) {
                  this.start_Str = "air-top";
               } else if (this.load_I_8() != -1) {
                  float topFlip = init_F_Vec3_3(MC.player.getEyePosition(), this.load_Vec3(), bestPlan.init_BlockPos(), pinYaw);
                  if (!Float.isNaN(topFlip)) {
                     float flipPitch = init_F_Vec3(airEye, bestPlan.init_BlockPos(), topFlip);
                     if (Float.isNaN(flipPitch)) {
                        flipPitch = init_F_Vec3(MC.player.getEyePosition(), bestPlan.init_BlockPos(), topFlip);
                     }

                     if (!Float.isNaN(flipPitch)) {
                        airYaw = topFlip;
                        airPitch = flipPitch;
                        this.start_Str = "air-flip";
                     }
                  }
               }
            }

            if (!Float.isNaN(airPitch)) {
               rotation = new ce.Inner_DataUtil(airYaw, airPitch);
            } else {
               if (bestPlan.init_Direction().getAxis().isHorizontal()) {
                  double lowY = bestPlan.init_BlockPos().getY() + bestSample.init_aI_DataUtil().read().y + 0.15;
                  if (lowY < worldPoint.y) {
                     worldPoint = new Vec3(worldPoint.x, lowY, worldPoint.z);
                  }
               }

               this.start_Str = "air-direct";
               rotation = this.init_ce_DataUtil_ce_DataUtil_Vec3(ce.init(worldPoint, MC.player.getEyePosition()), worldPoint);
               double bearingRun = Math.hypot(worldPoint.x - MC.player.getEyePosition().x, worldPoint.z - MC.player.getEyePosition().z);
               float chosen = bestPlan.init_Direction().getAxis().isHorizontal() && bearingRun >= 0.75 ? this.sync(rotation.init()) : pinYaw;
               if (this.load_I_8() != -1) {
                  float landing = load_F_Vec3(
                     MC.player.getEyePosition(), this.load_Vec3(), bestPlan.init_BlockPos(), bestPlan.init_Direction(), chosen, this.load_F_2()
                  );
                  if (!Float.isNaN(landing)) {
                     chosen = landing;
                     this.start_Str = "air-land";
                  }

                  float flip = init_F_Vec3_3(MC.player.getEyePosition(), this.load_Vec3(), bestPlan.init_BlockPos(), bestPlan.init_Direction(), chosen);
                  if (!Float.isNaN(flip)) {
                     chosen = flip;
                     this.start_Str = "air-flip";
                     float flipPitch = init_F_Vec3(airEye, MC.player.getEyePosition(), bestPlan.init_BlockPos(), bestPlan.init_Direction(), chosen, 89.5F);
                     if (!Float.isNaN(flipPitch)) {
                        rotation = new ce.Inner_DataUtil(rotation.init(), flipPitch);
                     }
                  }
               }

               rotation = new ce.Inner_DataUtil(chosen, rotation.load());
            }
         } else if (this.flags_Z() && MC.player != null && bestPlan.init_Direction().getAxis().isHorizontal()) {
            Vec3 leadEye = pinEye == null ? MC.player.getEyePosition().add(this.load_Vec3()) : pinEye;
            boolean postured = false;
            float goalYaw = 0.0F;
            if (bestFacePins) {
               this.start_Str = MC.player.onGround() ? "lead-pin" : "air-pin";
               goalYaw = pinYaw;
               postured = true;
            }

            if (!postured) {
               Vec3 goalEye = this.init_Vec3_BlockPos(bestPlan.init_BlockPos(), bestPlan.init_Direction(), selectionEye, laneDirection);
               ce.Inner_DataUtil pointGoal = ce.init(worldPoint, goalEye);
               goalYaw = this.sync(this.init_ce_DataUtil_ce_DataUtil_Vec3(pointGoal, worldPoint).init());
            }

            rotation = new ce.Inner_DataUtil(
               goalYaw, init_F_Vec3(MC.player.getEyePosition(), leadEye, bestPlan.init_BlockPos(), bestPlan.init_Direction(), goalYaw, 89.3F)
            );
         } else if (bestFacePins
            && bestPlan.init_Direction() == Direction.UP
            && !Float.isNaN(init_F_Vec3(pinEye == null ? MC.player.getEyePosition().add(this.load_Vec3()) : pinEye, bestPlan.init_BlockPos(), pinYaw))) {
            this.start_Str = MC.player.onGround() ? "lead-top" : "air-top";
            rotation = new ce.Inner_DataUtil(
               pinYaw, init_F_Vec3(pinEye == null ? MC.player.getEyePosition().add(this.load_Vec3()) : pinEye, bestPlan.init_BlockPos(), pinYaw)
            );
         } else {
            Vec3 goalEye = this.init_Vec3_BlockPos(bestPlan.init_BlockPos(), bestPlan.init_Direction(), selectionEye, laneDirection);
            rotation = ce.init(worldPoint, goalEye);
            rotation = this.init_ce_DataUtil_ce_DataUtil_Vec3(rotation, worldPoint);
            if (goalPinnedFace != null) {
               float postureYaw = this.read_F();
               if (bestPlan.init_Direction() == Direction.UP && MC.player != null) {
                  Vec3 topEye = pinEye == null ? MC.player.getEyePosition().add(this.load_Vec3()) : pinEye;
                  float topPitch = init_F_Vec3(topEye, bestPlan.init_BlockPos(), postureYaw);
                  if (Float.isNaN(topPitch)) {
                     this.load_StringBuilder.append("u:tx ");
                     return null;
                  }

                  this.start_Str = MC.player.onGround() ? "lead-top" : "air-top";
                  rotation = new ce.Inner_DataUtil(postureYaw, topPitch);
               } else {
                  rotation = new ce.Inner_DataUtil(postureYaw, rotation.load());
               }
            } else {
               rotation = new ce.Inner_DataUtil(this.sync(rotation.init()), rotation.load());
            }
         }

         if (this.flags_Z()) {
            Vec3 clampEye = MC.player.getEyePosition();
            Vec3 clampLead = clampEye.add(this.load_Vec3());
            double[] window = MC.player.onGround()
               ? init_DArr_Vec3_2(clampEye, clampLead, bestPlan.init_BlockPos(), bestPlan.init_Direction(), rotation.init())
               : init_DArr_Vec3_2(clampLead, clampEye, bestPlan.init_BlockPos(), bestPlan.init_Direction(), rotation.init());
            Vec3 publishedEye = update_Vec3;
            if (window != null && publishedEye != null) {
               double[] published = init_DArr_Vec3_2(publishedEye, bestPlan.init_BlockPos(), bestPlan.init_Direction(), rotation.init());
               if (published != null && Math.max(window[0], published[0]) <= Math.min(window[1], published[1])) {
                  window = new double[]{Math.max(window[0], published[0]), Math.min(window[1], published[1]), window[2]};
               }
            }

            double windowLow = window == null ? 0.0 / 0.0 : window[0];
            double windowHigh = window == null ? 0.0 / 0.0 : window[1];
            float goalPitch = init_F_F(rotation.load(), windowLow, windowHigh);
            float dodged = this.init_F_F(
               goalPitch,
               windowLow,
               bestPlan.init_Direction(),
               MC.player.onGround() ? clampEye : clampLead,
               this.load_Vec3(),
               rotation.init(),
               bestPlan.init_BlockPos()
            );
            double flickHigh = dodged < goalPitch ? (Double.isNaN(windowHigh) ? dodged : Math.min(windowHigh, dodged)) : windowHigh;
            goalPitch = this.init_F_F(dodged, windowLow, flickHigh, bestPlan.init_Direction(), candidate.immutable());
            rotation = new ce.Inner_DataUtil(rotation.init(), goalPitch);
         }

         BlockHitResult plannedHit = new BlockHitResult(worldPoint, bestPlan.init_Direction(), bestPlan.init_BlockPos(), false);
         return new ScaffoldModule.Inner_TableIndex(
            bestPlan.init_BlockPos(),
            candidate.immutable(),
            bestPlan.init_Direction(),
            plannedHit,
            rotation,
            bestPlan.init_BlockPos().getY() + bestSample.init_aI_DataUtil().read().y
         );
      } else {
         return null;
      }
   }

   private boolean f_Z() {
      return this.queue_Z() || this.f_b_Z && MC.player != null && !MC.player.onGround();
   }

   private boolean open(BlockPos pos) {
      return MC.level != null && !MC.level.isOutsideBuildHeight(pos) ? this.init_Z_BlockState_BlockPos(MC.level.getBlockState(pos), pos) : false;
   }

   private boolean poll(BlockPos pos) {
      if (!this.open(pos)) {
         this.read_Map.remove(pos);
         return false;
      } else {
         return !this.read_Map.containsKey(pos);
      }
   }

   private boolean g_Z() {
      Iterator<BlockPos> iterator = this.read_Map.keySet().iterator();

      while (iterator.hasNext()) {
         BlockPos cell = iterator.next();
         if (!this.open(cell)) {
            iterator.remove();
         }
      }

      return !this.read_Map.isEmpty();
   }

   static boolean init_Z_I_4(int laneStep, BlockPos below, Predicate<BlockPos> solid) {
      return init_Z_I_7(laneStep, below, false, solid);
   }

   static boolean init_Z_I_7(int laneStep, BlockPos below, boolean aheadRise, Predicate<BlockPos> solid) {
      if (laneStep != -1 && below != null) {
         double yaw = Math.toRadians(init_F_I(laneStep));
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

   private void b_V() {
      if (MC.player != null && MC.player.onGround()) {
         int row = this.open_I();
         if (this.f_cb_I != -2147483648 && row > this.f_cb_I) {
            this.f_cc_I = ck.init_ck().state_I();
         } else if (this.f_cb_I != -2147483648 && row < this.f_cb_I) {
            this.f_cc_I = -2147483648;
         }

         this.f_cb_I = row;
         this.f_cd_I = -2147483648;
      }
   }

   private boolean h_Z() {
      return !this.k() ? false : this.f_cb_I == -2147483648 || MC.player.getY() < this.f_cb_I + 1.0;
   }

   static boolean init_Z_D_2(double feet, double vy, int footingRow, int lookahead) {
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
         return !this.k() ? false : init_Z_D_2(MC.player.getY(), MC.player.getDeltaMovement().y, this.f_cb_I, 2);
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

      Vec3 landing = init_Vec3_Vec3_2(MC.player.position(), MC.player.getDeltaMovement(), candidate.getY() + 1.0, 2);
      return landing != null && Mth.floor(landing.x) == candidate.getX() && Mth.floor(landing.z) == candidate.getZ();
   }

   private static double init_D_D_I(double centre, int cell) {
      double low = Math.max(centre - 0.3, cell);
      double high = Math.min(centre + 0.3, cell + 1.0);
      return Math.max(0.0, high - low);
   }

   static boolean init_Z_I_4(int servedTick, int now, BlockPos held, BlockPos noted) {
      return servedTick == now && held != null && held.equals(noted);
   }

   private void read_V_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      if (target != null) {
         int tick = ck.init_ck().state_I();
         if (!init_Z_I_4(this.f_aF_I, tick, this.init_aI_TableIndex == null ? null : this.init_aI_TableIndex.load(), target.load())) {
            this.init_aI_TableIndex = target;
            this.f_aE_I = tick;
         }
      }
   }

   static boolean init_Z_I_3(int heldTick, int now, int maxTicks, boolean cellOpen, boolean supported) {
      if (heldTick < 0) {
         return false;
      }

      int age = now - heldTick;
      return age >= 0 && age <= maxTicks && cellOpen && supported;
   }

   static boolean read_Z_I_I(int heldRow, int footingRow) {
      return footingRow == -2147483648 || heldRow >= footingRow;
   }

   static int write_I_I_I(int footingRow, int lastPlacedRow) {
      if (footingRow == -2147483648) {
         return lastPlacedRow;
      } else {
         return lastPlacedRow == -2147483648 ? footingRow : Math.max(footingRow, lastPlacedRow);
      }
   }

   static boolean init_Z_Vec3_8(Vec3 position, Vec3 laneDirection, BlockPos placed, boolean descending) {
      return !descending && init_Z_Vec3_6(position, laneDirection, placed, 0.8);
   }

   private ScaffoldModule.Inner_TableIndex read_aI_TableIndex() {
      ScaffoldModule.Inner_TableIndex held = this.init_aI_TableIndex;
      if (held != null && MC.level != null) {
         BlockPos placed = held.load();
         BlockPos support = held.init_BlockPos();
         if (!MC.level.isOutsideBuildHeight(placed) && !MC.level.isOutsideBuildHeight(support)) {
            BlockPos lastPlaced = this.init_ArrayDeque.peekLast();
            if (!read_Z_I_I(placed.getY(), write_I_I_I(this.close_I(), lastPlaced == null ? -2147483648 : lastPlaced.getY()))) {
               this.init_aI_TableIndex = null;
               return null;
            } else if (MC.player != null
               && init_Z_Vec3_8(MC.player.position(), this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load(), placed, this.h_Z())) {
               this.init_aI_TableIndex = null;
               return null;
            } else if (MC.player != null && !init_Z_AABB_5(MC.player.getBoundingBox(), MC.player.getDeltaMovement(), placed, this.push(placed))) {
               this.init_aI_TableIndex = null;
               return null;
            } else if (this.sync(placed)) {
               this.init_aI_TableIndex = null;
               return null;
            } else if (this.load_Z_aI_TableIndex(held)) {
               this.init_aI_TableIndex = null;
               return null;
            } else if (MC.player != null && !init_Z_aI_TableIndex_4(held, MC.player.getEyePosition(), this.load_Vec3())) {
               this.init_aI_TableIndex = null;
               return null;
            } else {
               boolean cellOpen = MC.level.getBlockState(placed).canBeReplaced();
               boolean supported = this.init_Z_BlockState_BlockPos(MC.level.getBlockState(support), support);
               if (!init_Z_I_3(this.f_aE_I, ck.init_ck().state_I(), 3, cellOpen, supported)) {
                  this.init_aI_TableIndex = null;
                  return null;
               } else {
                  this.frame_Z = true;
                  this.f_aF_I = ck.init_ck().state_I();
                  ScaffoldModule.Inner_TableIndex resolved = this.write_aI_TableIndex_aI_TableIndex(held);
                  return resolved == null ? held : resolved;
               }
            }
         } else {
            this.init_aI_TableIndex = null;
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

   private ScaffoldModule.Inner_TableIndex write_aI_TableIndex_aI_TableIndex(ScaffoldModule.Inner_TableIndex held) {
      if (MC.player != null && MC.level != null) {
         BlockPos placed = held.load();
         BlockState state = MC.level.getBlockState(placed);
         boolean replaceExisting = !state.isAir() && state.getFluidState().isEmpty();
         String planFail = this.reset_Str;
         ScaffoldModule.Inner_TableIndex upgraded = this.read_aI_TableIndex_aI_TableIndex(held);
         if (upgraded != held) {
            this.init_aI_TableIndex = upgraded;
            return upgraded;
         } else {
            ScaffoldModule.Inner_TableIndex fresh = this.init_aI_TableIndex_BlockPos(
               placed,
               this.load_Vec3_aI_HashUtil(this.init_aI_HashUtil),
               replaceExisting,
               this.init_aI_HashUtil == null ? null : this.init_aI_HashUtil.load(),
               this.f_Z(),
               held.init_Direction()
            );
            this.reset_Str = planFail;
            return fresh != null && fresh.load().equals(placed) && fresh.init_Direction() == held.init_Direction() ? fresh : null;
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

   static boolean init_Z_Vec3_BlockPos(Vec3 position, BlockPos column) {
      return position != null && column != null && init_D_D_I(position.x, column.getX()) > 0.0 && init_D_D_I(position.z, column.getZ()) > 0.0;
   }

   static boolean init_Z_Vec3_5(Vec3 position, Vec3 velocity, BlockPos column, int leadTicks, double minOverlap) {
      if (position != null && column != null) {
         double x = position.x + (velocity == null ? 0.0 : velocity.x) * leadTicks;
         double z = position.z + (velocity == null ? 0.0 : velocity.z) * leadTicks;
         return init_D_D_I(x, column.getX()) >= minOverlap && init_D_D_I(z, column.getZ()) >= minOverlap;
      } else {
         return true;
      }
   }

   private boolean load_Z_BlockPos_Direction(BlockPos support, Direction face) {
      return this.write_Direction == face && support != null && support.equals(this.prev_BlockPos);
   }

   static boolean init_Z_BlockPos_6(BlockPos pinSupport, Direction pinFace, BlockPos candidate) {
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

   static boolean init_Z_Z_2(boolean pins, double angle, boolean bestPins, double bestAngle) {
      return pins != bestPins ? pins : angle < bestAngle;
   }

   private void write_V_aI_TableIndex(ScaffoldModule.Inner_TableIndex target) {
      this.f_Z_D = target != null && MC.player != null
         ? init_D_Vec3_3(MC.player.getEyePosition().add(this.load_Vec3()), target.init_BlockPos(), target.init_Direction(), this.read_F())
         : 0.0 / 0.0;
      if (target != null && MC.player != null && this.flags_Z()) {
         if (!this.tick(target)
            && init_Z_Vec3_2(
               MC.player.getEyePosition().add(this.load_Vec3()),
               this.load_Vec3(),
               target.init_BlockPos(),
               target.init_Direction(),
               this.read_F(),
               this.load_Z_BlockPos_Direction(target.init_BlockPos(), target.init_Direction()),
               2
            )) {
            this.prev_BlockPos = target.init_BlockPos();
            this.write_Direction = target.init_Direction();
         } else {
            this.prev_BlockPos = null;
            this.write_Direction = null;
         }
      } else {
         this.prev_BlockPos = null;
         this.write_Direction = null;
      }
   }

   private boolean tick(ScaffoldModule.Inner_TableIndex target) {
      if (target == null) {
         this.valid_BlockPos = null;
         this.update_Direction = null;
         this.f_bx_I = 0;
         return false;
      } else {
         boolean same = this.update_Direction == target.init_Direction() && target.init_BlockPos().equals(this.valid_BlockPos);
         this.valid_BlockPos = target.init_BlockPos();
         this.update_Direction = target.init_Direction();
         this.f_bx_I = init_I_I_3(this.f_bx_I, same, this.f_l_Z);
         return this.f_bx_I > 14;
      }
   }

   static int init_I_I_3(int previous, boolean sameTarget, boolean clickLanded) {
      if (clickLanded) {
         return 0;
      } else {
         return sameTarget ? previous + 1 : 1;
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil_Vec3(ce.Inner_DataUtil pushGoal, Vec3 worldPoint) {
      if (this.flags_Z() && MC.player != null && !MC.player.onGround()) {
         Vec3 eye = MC.player.getEyePosition();
         ce.Inner_DataUtil direct = ce.init(worldPoint, eye);
         ce.Inner_DataUtil predicted = ce.init(worldPoint, eye.add(MC.player.getDeltaMovement()));
         return init_ce_DataUtil_ce_DataUtil(pushGoal, direct, predicted);
      } else {
         return pushGoal;
      }
   }

   static ce.Inner_DataUtil init_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil pushGoal, ce.Inner_DataUtil direct, ce.Inner_DataUtil predicted) {
      return Math.abs(ce.init(predicted.init(), pushGoal.init())) > 100.0F
         ? pushGoal
         : new ce.Inner_DataUtil(predicted.init(), Math.min(predicted.load(), 90.0F));
   }

   private boolean init_Z_aI_TableIndex_3(ScaffoldModule.Inner_TableIndex target, InteractionHand hand, ItemStack stack) {
      return this.init_Z_aI_TableIndex(target, hand, stack, true, true);
   }

   private boolean init_Z_aI_TableIndex(
      ScaffoldModule.Inner_TableIndex target, InteractionHand hand, ItemStack stack, boolean restoreClientRotation, boolean sendPlacementRotation
   ) {
      if (AutismLiteVariant.enabled() || !DangerModule.read_Z_22() && !ClutchModule.read_Z_20()) {
         ce.Inner_DataUtil rotation = target.init_ce_DataUtil();
         ScaffoldModule.Inner_HashUtil placementLine = this.init_aI_HashUtil;
         Vec3 previousFallOff = this.read_Vec3_aI_HashUtil(placementLine);
         float clientYaw = MC.player.getYRot();
         float clientPitch = MC.player.getXRot();
         if (sendPlacementRotation && !init_Z_ce_DataUtil_ce_DataUtil_4(rotation, this.poll_ce_DataUtil())) {
            this.write_V_ce_DataUtil(rotation);
         }

         int oldCount = stack.getCount();

         try {
            InteractionResult result = MC.gameMode.useItemOn(MC.player, hand, target.init_BlockHitResult());
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
               autismclient.util.G.load_V();
               cg.init(target.load());
               this.init_V_BlockPos_3(target.load(), placementLine, previousFallOff);
               boolean wasStackUsed = !stack.isEmpty() && (stack.getCount() != oldCount || MC.player.hasInfiniteMaterials());
               if (wasStackUsed) {
                  Compat263.getItemInHandRenderer(MC.gameRenderer).itemUsed(hand);
               }

               return true;
            }
         } finally {
            if (restoreClientRotation) {
               ce.Inner_DataUtil clientRotation = new ce.Inner_DataUtil(clientYaw, clientPitch);
               if (!init_Z_ce_DataUtil_ce_DataUtil_4(this.poll_ce_DataUtil(), clientRotation)) {
                  this.write_V_ce_DataUtil(clientRotation);
               }
            }
         }
      } else {
         return false;
      }
   }

   static Vec3 init_Vec3_Vec3_3(Vec3 predictedPosition, Vec3 actualPosition, boolean edgeHoldActive) {
      if (predictedPosition == null) {
         return actualPosition;
      } else if (actualPosition == null) {
         return predictedPosition;
      } else {
         return edgeHoldActive ? actualPosition : predictedPosition;
      }
   }

   static Vec3 read_Vec3_Vec3_Vec3(Vec3 predictedPosition, Vec3 aimPosition) {
      return predictedPosition == null ? aimPosition : predictedPosition;
   }

   private void init_V_BlockPos_3(BlockPos placed, ScaffoldModule.Inner_HashUtil line, Vec3 previousFallOff) {
      this.load_aI_HashUtil = null;
      BlockPos immutable = placed.immutable();
      if (!immutable.equals(this.init_ArrayDeque.peekLast())) {
         while (this.init_ArrayDeque.size() >= 4) {
            this.init_ArrayDeque.removeFirst();
         }

         this.init_ArrayDeque.addLast(immutable);
      }

      if (line != null && previousFallOff != null) {
         float angle = (float)Math.atan2(line.load().z, line.load().x);
         Vec3 unrotatedOffset = MC.player.position().subtract(previousFallOff).yRot(angle);
         this.load_ArrayDeque.addLast(unrotatedOffset);

         while (this.load_ArrayDeque.size() > 4) {
            this.load_ArrayDeque.removeFirst();
         }
      }
   }

   private void write_V_ce_DataUtil(ce.Inner_DataUtil rotation) {
      if (rotation != null && MC.getConnection() != null && MC.player != null) {
         this.init_ce_DataUtil = rotation;
         MC.getConnection()
            .send(
               new PosRot(
                  MC.player.getX(), MC.player.getY(), MC.player.getZ(), rotation.init(), rotation.load(), MC.player.onGround(), MC.player.horizontalCollision
               )
            );
      }
   }

   private static boolean init_Z_ce_DataUtil_ce_DataUtil_4(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
      return first != null && second != null && Float.compare(first.init(), second.init()) == 0 && Float.compare(first.load(), second.load()) == 0;
   }

   private BlockHitResult init_BlockHitResult_ce_DataUtil_D(ce.Inner_DataUtil rotation, double reach) {
      return rotation == null ? null : init_BlockHitResult_Vec3(MC.player.getEyePosition(), rotation, reach, MC.level, MC.player);
   }

   static BlockHitResult init_BlockHitResult_Vec3(Vec3 eye, ce.Inner_DataUtil rotation, double reach, BlockGetter world, Entity entity) {
      Vec3 look = init_Vec3_ce_DataUtil(rotation);
      Vec3 end = eye.add(look.scale(reach));
      HitResult result = world.clip(
         new ClipContext(
            eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, entity == null ? CollisionContext.empty() : CollisionContext.of(entity)
         )
      );
      return result instanceof BlockHitResult blockHit && result.getType() == Type.BLOCK ? blockHit : null;
   }

   private ItemStack init_ItemStack_2() {
      ItemStack main = MC.player.getMainHandItem();
      if (this.init_Z_ItemStack_4(main)) {
         return main;
      }

      int hotbar = this.pop_I();
      return hotbar >= 0 ? MC.player.getInventory().getItem(hotbar) : ItemStack.EMPTY;
   }

   private BlockPos init_BlockPos_BlockHitResult_ItemStack_2(BlockHitResult hit, ItemStack stack) {
      if (hit == null) {
         return null;
      } else {
         return MC.player != null && stack != null && !stack.isEmpty()
            ? new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, hit).getClickedPos().immutable()
            : hit.getBlockPos().relative(hit.getDirection());
      }
   }

   private boolean load_Z_BlockHitResult_aI_TableIndex(BlockHitResult hit, ScaffoldModule.Inner_TableIndex pending) {
      return hit != null && pending != null ? pending.load().equals(this.init_BlockPos_BlockHitResult_ItemStack_2(hit, this.init_ItemStack_2())) : false;
   }

   static AABB init_AABB_BlockPos(BlockPos support) {
      if (support == null) {
         return null;
      }

      if (MC != null && MC.level != null) {
         BlockState state = MC.level.getBlockState(support);
         if (support.equals(cache_BlockPos) && state == init_BlockState) {
            return init_AABB;
         }

         AABB box = load_AABB_BlockPos(support);
         VoxelShape shape = state.getShape(MC.level, support, MC.player == null ? CollisionContext.empty() : CollisionContext.of(MC.player));
         if (!shape.isEmpty()) {
            box = shape.bounds().move(support.getX(), support.getY(), support.getZ());
         }

         cache_BlockPos = support.immutable();
         init_BlockState = state;
         init_AABB = box;
         return box;
      } else {
         return load_AABB_BlockPos(support);
      }
   }

   static Vec3 init_Vec3_AABB_Direction(AABB support, Direction face) {
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

   static AABB load_AABB_BlockPos(BlockPos support) {
      return new AABB(support.getX(), support.getY(), support.getZ(), support.getX() + 1.0, support.getY() + 1.0, support.getZ() + 1.0);
   }

   private boolean init_Z_BlockState_3(BlockState state, BlockPos pos, ItemStack stack) {
      BlockPlaceContext context = new BlockPlaceContext(
         MC.player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atLowerCornerOf(pos), Direction.UP, pos, false)
      );
      return state.canBeReplaced(context);
   }

   private InteractionHand init_InteractionHand() {
      ItemStack main = MC.player.getMainHandItem();
      int selected = MC.player.getInventory().getSelectedSlot();
      int remaining = this.index_Z() ? this.read_I_I(selected) : main.getCount();
      int freshSlot = -1;
      if (this.index_Z() && this.init_aI_NodeWalker != ScaffoldModule.Inner_NodeWalker.PLACING && remaining < 8) {
         for (int candidate = 0; candidate < 9; candidate++) {
            if (candidate != selected && !autismclient.util.ah.init(candidate, this.id())) {
               ItemStack stack = MC.player.getInventory().getItem(candidate);
               if (this.init_Z_ItemStack_4(stack)
                  && this.read_I_I(candidate) >= 8
                  && (freshSlot < 0 || this.init_I_ItemStack_ItemStack(stack, MC.player.getInventory().getItem(freshSlot)) > 0)) {
                  freshSlot = candidate;
               }
            }
         }
      }

      if (this.init_Z_ItemStack_4(main) && remaining > 0 && freshSlot < 0) {
         if (this.f_k_I == MC.player.getInventory().getSelectedSlot()) {
            this.f_k_I = -1;
         }

         return InteractionHand.MAIN_HAND;
      } else {
         int slot = freshSlot >= 0 ? freshSlot : this.pop_I();
         if (slot < 0) {
            return null;
         }

         if (selected == slot) {
            return InteractionHand.MAIN_HAND;
         }

         if (!autismclient.util.ah.write_Z(this.id())) {
            return null;
         }

         try {
            if (this.bool("switch-back") && this.f_j_I < 0) {
               this.f_j_I = selected;
            }

            this.f_k_I = slot;
            this.f_l_I = slot;
            this.write_Z = true;
            autismclient.util.as.init_V_I(slot);
         } finally {
            autismclient.util.ah.read_V(this.id());
         }

         return null;
      }
   }

   public static int load_I_I(int slot) {
      if (MC.player != null && MC.level != null && slot >= 0 && slot < 9) {
         return al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold ? scaffold.read_I_I(slot) : MC.player.getInventory().getItem(slot).getCount();
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
         scaffold.c_V();
         scaffold.load_IArr[slot] = Math.max(0, scaffold.load_IArr[slot] - 1);
         scaffold.write_IArr[slot] = ck.init_ck().state_I();
         scaffold.read_IArr[slot] = sequence;
      }
   }

   private int read_I_I(int slot) {
      if (slot >= 0 && slot < 9) {
         this.c_V();
         ItemStack stack = MC.player.getInventory().getItem(slot);
         int visible = stack.isEmpty() ? 0 : stack.getCount();
         if (MC.player.hasInfiniteMaterials()) {
            this.init_ItemArr[slot] = null;
            return init_I_I_2(0, visible, false, true);
         }

         if (!stack.isEmpty() && this.init_ItemArr[slot] != stack.getItem()) {
            this.init_ItemArr[slot] = stack.getItem();
            this.load_IArr[slot] = visible;
            this.read_IArr[slot] = -1;
            this.write_IArr[slot] = -2147483648;
         }

         int tick = ck.init_ck().state_I();
         boolean settled = load_Z_I(this.read_IArr[slot], this.f_B_I, this.write_IArr[slot], tick, autismclient.util.macro.ServerTickPhaseTracker.init_I())
            && (this.f_C_I != slot || !this.poll_Z_3());
         this.load_IArr[slot] = init_I_I_2(this.load_IArr[slot], visible, settled);
         return this.load_IArr[slot];
      } else {
         return 0;
      }
   }

   private void c_V() {
      if (this.init_ClientLevel != MC.level) {
         this.d_V();
         this.init_ClientLevel = MC.level;
      }
   }

   private void d_V() {
      this.init_ClientLevel = null;
      this.f_B_I = -2147483648;
      Arrays.fill(this.init_ItemArr, null);
   }

   static boolean load_Z_I(int sequence, int acknowledged, int useTick, int tick, int pingMs) {
      if (sequence == 0) {
         int echoTicks = Math.max(20, 4 + (int)Math.ceil(Math.max(0, pingMs) / 25.0));
         return useTick == -2147483648 || tick - useTick > echoTicks;
      } else {
         return useTick < tick - 1 && load_Z_I_I(sequence, acknowledged);
      }
   }

   static int init_I_I_2(int remaining, int visible, boolean serverCaughtUp) {
      return init_I_I_2(remaining, visible, serverCaughtUp, false);
   }

   static int init_I_I_2(int remaining, int visible, boolean serverCaughtUp, boolean infiniteMaterials) {
      if (infiniteMaterials) {
         return visible > 0 ? 2147483647 : 0;
      } else {
         return Math.max(0, serverCaughtUp ? visible : Math.min(remaining, visible));
      }
   }

   private int pop_I() {
      int best = this.load_I_Z(true);
      return best >= 0 ? best : this.load_I_Z(false);
   }

   private int load_I_Z(boolean requireReserve) {
      int best = -1;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.ah.init(slot, this.id())) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            int available = this.index_Z() ? this.read_I_I(slot) : stack.getCount();
            if (this.init_Z_ItemStack_4(stack)
               && available > 0
               && (!requireReserve || available > 1)
               && (best < 0 || this.init_I_ItemStack_ItemStack(stack, MC.player.getInventory().getItem(best)) > 0)) {
               best = slot;
            }
         }
      }

      return best;
   }

   private int init_I_ItemStack_ItemStack(ItemStack first, ItemStack second) {
      Block firstBlock = ((BlockItem)first.getItem()).getBlock();
      Block secondBlock = ((BlockItem)second.getItem()).getBlock();
      BlockState firstState = firstBlock.defaultBlockState();
      BlockState secondState = secondBlock.defaultBlockState();
      int result = Boolean.compare(!this.init_Z_Block_BlockState(firstBlock, firstState), !this.init_Z_Block_BlockState(secondBlock, secondState));
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

      result = Double.compare(this.init_D_BlockState_Z(secondState, true), this.init_D_BlockState_Z(firstState, true));
      if (result != 0) {
         return result;
      }

      result = Integer.compare(second.getCount(), first.getCount());
      return result != 0 ? result : Double.compare(this.init_D_BlockState_Z(secondState, false), this.init_D_BlockState_Z(firstState, false));
   }

   private boolean init_Z_Block_BlockState(Block block, BlockState state) {
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

   private double init_D_BlockState_Z(BlockState state, boolean neutralRange) {
      double hardness = state.getDestroySpeed(MC.level, BlockPos.ZERO);
      return neutralRange && hardness >= 0.8 && hardness <= 2.0 ? 0.0 : Math.abs(1.7 - hardness);
   }

   private boolean init_Z_ItemStack_4(ItemStack stack) {
      if (!(stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem)) {
         return false;
      } else if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      } else {
         Block block = blockItem.getBlock();
         if (init_Z_Block_3(block) && this.load_Z_Block(block)) {
            BlockState state = block.defaultBlockState();
            return state.entityCanStandOnFace(MC.level, BlockPos.ZERO, MC.player, Direction.UP);
         } else {
            return false;
         }
      }
   }

   public static boolean init_Z_Block_3(Block block) {
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

   private boolean load_Z_Block(Block block) {
      String mode = this.choice("filter-mode");
      if ("Off".equals(mode)) {
         return true;
      }

      boolean listed = this.init_Set_6().contains(block);
      return "Whitelist".equals(mode) ? listed : !listed;
   }

   private Set<Block> init_Set_6() {
      String raw = this.value("blocks");
      if (raw.equals(this.read_Str)) {
         return this.init_Set;
      }

      Set<Block> blocks = new HashSet<>();

      for (String entry : this.list("blocks")) {
         Identifier id = Identifier.tryParse(di.init(entry));
         if (id != null) {
            BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> {
               if (init_Z_Block_3(block)) {
                  blocks.add(block);
               }
            });
         }
      }

      this.read_Str = raw;
      this.init_Set = Set.copyOf(blocks);
      return this.init_Set;
   }

   private void e_V() {
      if (this.bool("switch-back") && this.f_j_I >= 0) {
         this.f_m_I = 5;
      }
   }

   private void f_V() {
      this.e_V();
   }

   private void g_V() {
      if (!this.bool("switch-back")) {
         this.f_j_I = -1;
         this.f_l_I = -1;
         this.f_m_I = 0;
      } else if (this.f_j_I >= 0 && MC != null && MC.player != null) {
         if (this.f_l_I >= 0 && MC.player.getInventory().getSelectedSlot() != this.f_l_I) {
            this.f_j_I = -1;
            this.f_k_I = -1;
            this.f_l_I = -1;
            this.f_m_I = 0;
         } else if (this.f_m_I > 0) {
            this.f_m_I--;
         } else {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (selected == this.f_j_I) {
               this.f_j_I = -1;
               this.f_k_I = -1;
               this.f_l_I = -1;
            } else if (!autismclient.util.ah.write_Z(this.id())) {
               this.f_m_I = 1;
            } else {
               try {
                  this.f_k_I = this.f_j_I;
                  autismclient.util.as.init_V_I(this.f_j_I);
               } finally {
                  autismclient.util.ah.read_V(this.id());
               }
            }
         }
      }
   }

   private ce.Inner_DataUtil poll_ce_DataUtil() {
      if (this.init_ce_DataUtil != null) {
         return this.init_ce_DataUtil;
      } else {
         return MC != null && MC.player != null ? ce.init(MC.player) : new ce.Inner_DataUtil(0.0F, 0.0F);
      }
   }

   private static double init_D_ce_DataUtil_ce_DataUtil(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
      double cosine = Mth.clamp(init_Vec3_ce_DataUtil(first).dot(init_Vec3_ce_DataUtil(second)), -1.0, 1.0);
      return Math.toDegrees(Math.acos(cosine));
   }

   private static Vec3 init_Vec3_ce_DataUtil(ce.Inner_DataUtil rotation) {
      return Vec3.directionFromRotation(rotation.load(), rotation.init());
   }

   private ScaffoldModule.Inner_HashUtil init_aI_HashUtil_Input(Input input) {
      Vec3 direction = this.init_Vec3_3();
      ScaffoldModule.Inner_BitSetUtil support = this.init_aI_BitSetUtil();
      if (support == null) {
         if (!this.flags_Z()) {
            return null;
         }

         Vec3 position = MC.player.position();
         ScaffoldModule.Inner_HashUtil previous = this.init_aI_HashUtil;
         Vec3 anchor = init_Vec3_Vec3_4(
            position,
            previous == null ? null : previous.init(),
            previous == null ? null : previous.load(),
            direction,
            this.f_n_I <= 2 ? this.init_BlockPos : null
         );
         return new ScaffoldModule.Inner_HashUtil(new Vec3(anchor.x, position.y, anchor.z), direction);
      } else {
         this.init_aI_BitSetUtil = support;
         ScaffoldModule.Inner_HashUtil placedLine = this.init_aI_HashUtil();
         Vec3 anchor;
         if (placedLine != null && placedLine.load().dot(direction) >= 0.5) {
            anchor = init_Vec3_aI_HashUtil_Vec3(placedLine, MC.player.position());
         } else {
            anchor = new Vec3(support.init_BlockPos().getX() + 0.5, MC.player.getY(), support.init_BlockPos().getZ() + 0.5);
         }

         return new ScaffoldModule.Inner_HashUtil(new Vec3(anchor.x, MC.player.getY(), anchor.z), direction);
      }
   }

   private ScaffoldModule.Inner_HashUtil init_aI_HashUtil_aI_HashUtil_Z(ScaffoldModule.Inner_HashUtil requested, boolean edgeActive) {
      ScaffoldModule.Inner_HashUtil retained = init_aI_HashUtil_aI_HashUtil(requested, this.load_aI_HashUtil, edgeActive, MC.player.getY());
      if (edgeActive && requested != null) {
         if (this.load_aI_HashUtil == null) {
            this.load_aI_HashUtil = requested;
         }

         return retained;
      } else {
         this.load_aI_HashUtil = null;
         return retained;
      }
   }

   static ScaffoldModule.Inner_HashUtil init_aI_HashUtil_aI_HashUtil(ScaffoldModule.Inner_HashUtil requested, ScaffoldModule.Inner_HashUtil locked, boolean edgeActive, double playerY) {
      if (edgeActive && requested != null) {
         ScaffoldModule.Inner_HashUtil selected = locked == null ? requested : locked;
         Vec3 origin = selected.init();
         return new ScaffoldModule.Inner_HashUtil(new Vec3(origin.x, playerY, origin.z), selected.load());
      } else {
         return requested;
      }
   }

   static Vec3 init_Vec3_Vec3_4(Vec3 position, Vec3 previousOrigin, Vec3 previousDirection, Vec3 requestedDirection, BlockPos lastSupport) {
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

   private ScaffoldModule.Inner_HashUtil init_aI_HashUtil() {
      if (this.init_ArrayDeque.size() < 2) {
         return null;
      }

      Iterator<BlockPos> iterator = this.init_ArrayDeque.descendingIterator();
      BlockPos last = iterator.next();
      Vec3 lastCenter = new Vec3(last.getX() + 0.5, last.getY(), last.getZ() + 0.5);

      while (iterator.hasNext()) {
         BlockPos previous = iterator.next();
         Vec3 previousCenter = new Vec3(previous.getX() + 0.5, previous.getY(), previous.getZ() + 0.5);
         Vec3 direction = new Vec3(lastCenter.x - previousCenter.x, 0.0, lastCenter.z - previousCenter.z);
         if (!(direction.lengthSqr() <= 1.0E-8)) {
            return new ScaffoldModule.Inner_HashUtil(previousCenter.add(lastCenter).scale(0.5), direction.normalize());
         }
      }

      return null;
   }

   private ScaffoldModule.Inner_BitSetUtil init_aI_BitSetUtil() {
      List<ScaffoldModule.Inner_RingBuffer> candidates = new ArrayList<>(load_DArr.length * load_DArr.length);
      Set<BlockPos> visited = new HashSet<>();
      Vec3 playerPosition = MC.player.position();

      for (double xOffset : load_DArr) {
         for (double zOffset : load_DArr) {
            BlockPos pos = BlockPos.containing(playerPosition.x + xOffset, playerPosition.y - 1.0, playerPosition.z + zOffset);
            if (visited.add(pos)) {
               ScaffoldModule.Inner_RingBuffer candidate = this.init_aI_RingBuffer_BlockPos(pos);
               if (candidate != null) {
                  candidates.add(candidate);
               }
            }
         }
      }

      if (!candidates.isEmpty()) {
         ScaffoldModule.Inner_RingBuffer best = candidates.stream().min(ScaffoldModule::init_I_aI_RingBuffer_aI_RingBuffer).orElse(null);
         if (best == null) {
            return null;
         }

         this.f_n_I = 0;
         this.f_o_I = -2147483648;
         ScaffoldModule.Inner_RingBuffer chosen = this.init_aI_RingBuffer_List_aI_RingBuffer(candidates, best);
         this.init_BlockPos = chosen.init_BlockPos();
         return new ScaffoldModule.Inner_BitSetUtil(
            chosen.init_BlockPos(), playerPosition.x - (chosen.init_BlockPos().getX() + 0.5), playerPosition.z - (chosen.init_BlockPos().getZ() + 0.5)
         );
      } else {
         if (!this.flags_Z()) {
            this.init_BlockPos = null;
            this.init_aI_BitSetUtil = null;
            this.f_n_I = 0;
            this.f_o_I = -2147483648;
            return null;
         }

         int clientTick = ck.init_ck().state_I();
         if (this.f_o_I != clientTick) {
            this.f_o_I = clientTick;
            this.f_n_I++;
         }

         if (this.f_n_I > 2) {
            this.init_BlockPos = null;
            this.init_aI_BitSetUtil = null;
         }

         return null;
      }
   }

   private ScaffoldModule.Inner_RingBuffer init_aI_RingBuffer_BlockPos(BlockPos pos) {
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
      return new ScaffoldModule.Inner_RingBuffer(pos.immutable(), overlapAtBestSurface, bestSurfaceDelta, dx * dx + dz * dz);
   }

   private ScaffoldModule.Inner_RingBuffer init_aI_RingBuffer_List_aI_RingBuffer(List<ScaffoldModule.Inner_RingBuffer> candidates, ScaffoldModule.Inner_RingBuffer best) {
      BlockPos lastPlaced = this.init_ArrayDeque.peekLast();
      ScaffoldModule.Inner_RingBuffer preferred = init_aI_RingBuffer_List_BlockPos(candidates, lastPlaced);
      if (preferred != null && init_Z_aI_RingBuffer_aI_RingBuffer(preferred, best)) {
         return preferred;
      }

      preferred = init_aI_RingBuffer_List_BlockPos(candidates, this.init_BlockPos);
      return preferred != null && init_Z_aI_RingBuffer_aI_RingBuffer(preferred, best) ? preferred : best;
   }

   private static ScaffoldModule.Inner_RingBuffer init_aI_RingBuffer_List_BlockPos(List<ScaffoldModule.Inner_RingBuffer> candidates, BlockPos position) {
      if (position == null) {
         return null;
      }

      for (ScaffoldModule.Inner_RingBuffer candidate : candidates) {
         if (candidate.init_BlockPos().equals(position)) {
            return candidate;
         }
      }

      return null;
   }

   private static boolean init_Z_aI_RingBuffer_aI_RingBuffer(ScaffoldModule.Inner_RingBuffer candidate, ScaffoldModule.Inner_RingBuffer best) {
      return candidate.load() <= best.load() + 0.001 && candidate.init_D() + 0.02 >= best.init_D();
   }

   private static int init_I_aI_RingBuffer_aI_RingBuffer(ScaffoldModule.Inner_RingBuffer first, ScaffoldModule.Inner_RingBuffer second) {
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

   private Vec3 load_Vec3_aI_HashUtil(ScaffoldModule.Inner_HashUtil line) {
      Vec3 playerPosition = MC.player.position();
      if (line == null) {
         return playerPosition;
      } else {
         boolean nearEdge = this.init_Z_D_Vec3(0.05, playerPosition);
         Vec3 fallOff = this.read_Vec3_aI_HashUtil(line);
         if (fallOff == null) {
            return playerPosition;
         } else if (update_Z_Z(this.flags_Z(), nearEdge, this.flush_Z)) {
            return write_Vec3_Vec3_Vec3(fallOff, line.load());
         } else if (nearEdge) {
            return playerPosition;
         } else {
            Vec3 delta = fallOff.subtract(playerPosition);
            double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            Vec3 bootstrap = horizontalDistance <= 1.0E-8 ? fallOff : fallOff.subtract(new Vec3(delta.x, 0.0, delta.z).scale(0.2 / horizontalDistance));
            Vec3 average = this.flush_Vec3();
            if (average == null) {
               ScaffoldModule.Inner_BitSetUtil support = this.init_aI_BitSetUtil;
               return support == null ? bootstrap : bootstrap.add(support.init_D(), 0.0, support.load());
            } else {
               float angle = (float)Math.atan2(line.load().z, line.load().x);
               Vec3 historyPosition = fallOff.add(average.yRot(-angle));
               double blend = Math.min(1.0, this.load_ArrayDeque.size() / 2.0);
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

   static boolean init_Z_Vec3(Vec3 position, BlockPos support, double maxDistance) {
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

   static Vec3 load_Vec3_Vec3_2(Vec3 playerEye, Vec3 faceCenter, Vec3 normal, double lead) {
      double playerPast = playerEye.subtract(faceCenter).dot(normal);
      return playerPast >= lead ? playerEye : playerEye.add(normal.scale(lead - playerPast));
   }

   private Vec3 init_Vec3_BlockPos(BlockPos support, Direction face, Vec3 selectionEye, Vec3 laneDirection) {
      if (this.flags_Z() && MC.player != null) {
         if (laneDirection != null && !(laneDirection.horizontalDistanceSqr() <= 1.0E-12)) {
            Vec3 course = new Vec3(laneDirection.x, 0.0, laneDirection.z).normalize();
            Vec3 leadEye = MC.player.getEyePosition().add(MC.player.getDeltaMovement());
            Vec3 goalEye = init_Vec3_Vec3_3(leadEye, selectionEye, support, face, course, 0.25);
            this.start_Str = goalEye == leadEye ? "lead" : "push";
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
         return load_Vec3_Vec3_2(playerEye, farEdge, course, lead);
      } else {
         Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
         Vec3 faceCenter = Vec3.atCenterOf(support).add(normal.scale(0.5));
         return load_Vec3_Vec3_2(playerEye, faceCenter, normal, lead);
      }
   }

   static float load_F_Vec3(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return init_F_Vec3(eye, support, face, yaw, 89.3F);
   }

   private static double[] init_DArr_Vec3_2(Vec3 eye, BlockPos support, Direction face, float yaw) {
      return init_DArr_Vec3_4(eye, init_AABB_BlockPos(support), face, yaw);
   }

   static double[] init_DArr_Vec3_4(Vec3 eye, AABB support, Direction face, float yaw) {
      if (support == null) {
         return null;
      }

      double past = init_D_Vec3_2(eye, support, face);
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
      return init_F_Vec3_2(primaryEye, secondaryEye, init_AABB_BlockPos(support), face, yaw, maxPitch);
   }

   static float init_F_Vec3_2(Vec3 primaryEye, Vec3 secondaryEye, AABB support, Direction face, float yaw, float maxPitch) {
      double[] window = init_DArr_Vec3_3(primaryEye, secondaryEye, support, face, yaw);
      if (window == null) {
         return Math.min(maxPitch, 89.3F);
      }

      double preferred = Math.toDegrees(Math.atan2(primaryEye.y - init_D_AABB(support), window[2]));
      return (float)Mth.clamp(Mth.clamp(preferred, window[0], window[1]), -maxPitch, maxPitch);
   }

   static double init_D_AABB(AABB support) {
      return support.maxY - 0.5 * support.getYsize();
   }

   static double[] init_DArr_Vec3_2(Vec3 primaryEye, Vec3 secondaryEye, BlockPos support, Direction face, float yaw) {
      return init_DArr_Vec3_3(primaryEye, secondaryEye, init_AABB_BlockPos(support), face, yaw);
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
      return init_F_Vec3_2(eye, init_AABB_BlockPos(support), face, yaw, maxPitch);
   }

   static float init_F_Vec3_2(Vec3 eye, AABB support, Direction face, float yaw, float maxPitch) {
      float park = Math.min(maxPitch, 89.3F);
      double past = init_D_Vec3_2(eye, support, face);
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
      double drop = eye.y - init_D_AABB(support);
      return (float)Mth.clamp(Math.toDegrees(Math.atan2(drop, run)), -maxPitch, maxPitch);
   }

   static double init_D_Vec3(Vec3 eye, BlockPos support, Direction face) {
      return init_D_Vec3_2(eye, init_AABB_BlockPos(support), face);
   }

   static double init_D_Vec3_2(Vec3 eye, AABB support, Direction face) {
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
      return init_D_Vec3_4(eye, init_AABB_BlockPos(support), yaw, margin);
   }

   static double init_D_Vec3_4(Vec3 eye, AABB support, float yaw, double margin) {
      double[] span = init_DArr_Vec3_3(eye, support, yaw, margin);
      return span == null ? 0.0 / 0.0 : (span[0] + span[1]) * 0.5;
   }

   static double[] init_DArr_Vec3(Vec3 eye, BlockPos support, float yaw, double margin) {
      return init_DArr_Vec3_3(eye, init_AABB_BlockPos(support), yaw, margin);
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
      return init_Z_Vec3_5(eye, init_AABB_BlockPos(support), yaw, holding);
   }

   static boolean init_Z_Vec3_5(Vec3 eye, AABB support, float yaw, boolean holding) {
      return !Double.isNaN(init_D_Vec3_4(eye, support, yaw, holding ? 0.02 : 0.1));
   }

   static float init_F_Vec3(Vec3 eye, BlockPos support, float yaw) {
      return init_F_Vec3_2(eye, init_AABB_BlockPos(support), yaw);
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
      return init_Z_Vec3_3(eye, init_AABB_BlockPos(support), face, yaw, holding);
   }

   static boolean init_Z_Vec3_3(Vec3 eye, AABB support, Direction face, float yaw, boolean holding) {
      if (support == null) {
         return false;
      }

      double past = init_D_Vec3_2(eye, support, face);
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
      return init_D_Vec3_5(eye, init_AABB_BlockPos(support), face, yaw);
   }

   static double init_D_Vec3_5(Vec3 eye, AABB support, Direction face, float yaw) {
      if (support != null && face.getAxis().isHorizontal()) {
         double past = init_D_Vec3_2(eye, support, face);
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
            if (read_Z_Vec3(eye, support, face, yaw) && !(Math.abs(update_F_F_F(laneYaw, yaw)) > 12.0F)) {
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

   static boolean update_Z_Z(boolean grimFamily, boolean nearEdge, boolean edgeSneakActive) {
      return grimFamily && (nearEdge || edgeSneakActive);
   }

   static boolean write(boolean grimFamily, boolean edgeSneakActive, boolean onGround, boolean jumping) {
      return grimFamily && edgeSneakActive && onGround && !jumping;
   }

   private Vec3 read_Vec3_aI_HashUtil(ScaffoldModule.Inner_HashUtil line) {
      if (line == null) {
         return null;
      }

      Vec3 nearest = init_Vec3_aI_HashUtil_Vec3(line, MC.player.position());
      Vec3 from = nearest.add(0.0, -0.1, 0.0);
      Vec3 to = from.add(line.load().scale(3.0));
      Vec3 collision = this.update_Vec3_Vec3_Vec3(from, to);
      return collision == null ? null : new Vec3(collision.x, MC.player.getY(), collision.z);
   }

   private Vec3 sync_Vec3() {
      Input input = MC.player.input == null ? Input.EMPTY : MC.player.input.keyPresses;
      Vec3 nextVelocity = MC.player.getDeltaMovement();
      if (nextVelocity.horizontalDistanceSqr() > 9.0E-6) {
         return new Vec3(nextVelocity.x, 0.0, nextVelocity.z).normalize();
      } else {
         return load_Z_Input(input) ? Vec3.directionFromRotation(0.0F, this.load_F_Input(input)) : Vec3.directionFromRotation(0.0F, MC.player.getYRot());
      }
   }

   private boolean init_Z_D_Vec3(double distance, Vec3 position) {
      Vec3 nextVelocity = MC.player.getDeltaMovement();
      Vec3 direction = this.sync_Vec3();
      Vec3 from = position.add(0.0, -0.1, 0.0);
      if (this.update_Vec3_Vec3_Vec3(from, from.add(direction.scale(distance))) != null) {
         return true;
      }

      Vec3 nextPosition = position.add(nextVelocity.x, nextVelocity.y, nextVelocity.z);
      Vec3 positionInTwoTicks = nextPosition.add(nextVelocity.x, 0.0, nextVelocity.z);
      return this.load_Z_Vec3(position) || this.load_Z_Vec3(positionInTwoTicks);
   }

   private boolean j() {
      if (MC.player.onGround()) {
         double lookahead = Mth.clamp(0.12 + MC.player.getDeltaMovement().horizontalDistance() * 1.0, 0.12, 0.35);
         return this.init_Z_D_Vec3(lookahead, MC.player.position());
      } else {
         return MC.player.fallDistance > 0.0 && this.init_Z_D_Vec3(0.1, MC.player.position());
      }
   }

   private ScaffoldModule.Inner_CachePool init_aI_CachePool() {
      if (MC.player.onGround() && !this.init_Z_D_Vec3(0.35, MC.player.position())) {
         return ScaffoldModule.Inner_CachePool.NONE;
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
         if (this.init_Z_D(x, y, z, 2)) {
            return ScaffoldModule.Inner_CachePool.NONE;
         }

         double drop = startY - y;
         if (drop > 2.0) {
            return ScaffoldModule.Inner_CachePool.IMMINENT;
         }

         if (drop > 0.6 && tick < 4) {
            return ScaffoldModule.Inner_CachePool.IMMINENT;
         }
      }

      return ScaffoldModule.Inner_CachePool.NONE;
   }

   private boolean init_Z_D(double x, double y, double z, int blocks) {
      MutableBlockPos pos = new MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));

      for (int step = 0; step <= blocks; step++) {
         if (this.init_Z_BlockState_BlockPos(MC.level.getBlockState(pos), pos)) {
            return true;
         }

         pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
      }

      return false;
   }

   private boolean load_Z_Vec3(Vec3 position) {
      AABB hitbox = MC.player
         .getDimensions(MC.player.getPose())
         .makeBoundingBox(position)
         .inflate(-0.05, 0.0, -0.05)
         .move(0.0, MC.player.fallDistance - MC.player.maxUpStep(), 0.0);
      return MC.level.noCollision(MC.player, hitbox);
   }

   private Vec3 update_Vec3_Vec3_Vec3(Vec3 from, Vec3 to) {
      Vec3 line = to.subtract(from);
      if (line.lengthSqr() <= 1.0E-12) {
         return null;
      }

      List<AABB> boxes = this.init_List_Vec3_Vec3(from, to);
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

   private List<AABB> init_List_Vec3_Vec3(Vec3 from, Vec3 to) {
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

   private Vec3 flush_Vec3() {
      if (this.load_ArrayDeque.isEmpty()) {
         return null;
      }

      Vec3 sum = Vec3.ZERO;

      for (Vec3 offset : this.load_ArrayDeque) {
         sum = sum.add(offset);
      }

      return sum.scale(1.0 / this.load_ArrayDeque.size());
   }

   private static Vec3 init_Vec3_aI_HashUtil_Vec3(ScaffoldModule.Inner_HashUtil line, Vec3 point) {
      Vec3 delta = point.subtract(line.init());
      double projection = delta.dot(line.load());
      return line.init().add(line.load().scale(projection));
   }

   private static double load_D_aI_HashUtil_Vec3(ScaffoldModule.Inner_HashUtil line, Vec3 point) {
      return init_Vec3_aI_HashUtil_Vec3(line, point).distanceToSqr(point);
   }

   private static double init_D_Vec3_AABB(Vec3 point, AABB box) {
      double x = Mth.clamp(point.x, box.minX, box.maxX);
      double y = Mth.clamp(point.y, box.minY, box.maxY);
      double z = Mth.clamp(point.z, box.minZ, box.maxZ);
      return point.distanceToSqr(new Vec3(x, y, z));
   }

   private static double init_D_aI_HashUtil_AABB(ScaffoldModule.Inner_HashUtil movementLine, AABB box) {
      ScaffoldModule.Inner_ByteReader line = new ScaffoldModule.Inner_ByteReader(movementLine.init(), movementLine.load());
      if (init_Z_aI_ByteReader_AABB(line, box)) {
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
      ScaffoldModule.Inner_PacketUtil[] edges = new ScaffoldModule.Inner_PacketUtil[]{
         new ScaffoldModule.Inner_PacketUtil(p000, p001),
         new ScaffoldModule.Inner_PacketUtil(p000, p010),
         new ScaffoldModule.Inner_PacketUtil(p000, p100),
         new ScaffoldModule.Inner_PacketUtil(p111, p110),
         new ScaffoldModule.Inner_PacketUtil(p111, p101),
         new ScaffoldModule.Inner_PacketUtil(p111, p011),
         new ScaffoldModule.Inner_PacketUtil(p001, p011),
         new ScaffoldModule.Inner_PacketUtil(p001, p101),
         new ScaffoldModule.Inner_PacketUtil(p010, p011),
         new ScaffoldModule.Inner_PacketUtil(p010, p110),
         new ScaffoldModule.Inner_PacketUtil(p100, p101),
         new ScaffoldModule.Inner_PacketUtil(p100, p110)
      };
      double best = 1.0 / 0.0;

      for (ScaffoldModule.Inner_PacketUtil edge : edges) {
         ScaffoldModule.Inner_PoolEntry pair = init_aI_PoolEntry_aI_PacketUtil_aI_ByteReader(edge, line);
         if (pair != null) {
            best = Math.min(best, pair.init().distanceToSqr(pair.load()));
         }
      }

      return best;
   }

   private static boolean init_Z_aI_ByteReader_AABB(ScaffoldModule.Inner_ByteReader line, AABB box) {
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

   private static boolean load_Z_Input(Input input) {
      return input != null && (input.forward() != input.backward() || input.left() != input.right());
   }

   private float load_F_Input(Input input) {
      return MC.player.getYRot() + init_F_Input(input);
   }

   private float init_F_LocalPlayer(LocalPlayer player) {
      Input move = this.push_Z && !player.onGround() && this.init_Input != null
         ? this.init_Input
         : (
            this.push_Z && this.load_Input != null
               ? this.load_Input
               : new Input(
                  init_Z_KeyMapping(MC.options.keyUp),
                  init_Z_KeyMapping(MC.options.keyDown),
                  init_Z_KeyMapping(MC.options.keyLeft),
                  init_Z_KeyMapping(MC.options.keyRight),
                  false,
                  false,
                  false
               )
         );
      if (!load_Z_Input(move)) {
         return Float.isFinite(this.flags_F) ? this.flags_F : player.getYRot();
      } else {
         return this.load_F_Input(move);
      }
   }

   private boolean k() {
      return MC.player != null && !MC.player.onGround() && MC.player.getDeltaMovement().y < -0.08;
   }

   private Vec3 init_Vec3_aI_DataUtil(
      ScaffoldModule.Inner_DataUtil face, ScaffoldModule.Inner_DataUtil fullFace, BlockPos targetPos, Vec3 eye, ce.Inner_DataUtil currentRotation, ScaffoldModule.Inner_HashUtil optimalLine
   ) {
      Vec3 offset = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ());
      ScaffoldModule.Inner_DataUtil trimmed = face.load(0.15).init(offset);
      ScaffoldModule.Inner_DataUtil targetFace = this.init_aI_DataUtil_aI_DataUtil(trimmed, eye, optimalLine);
      Vec3 point = init_Vec3_aI_DataUtil_aI_ByteReader(targetFace, new ScaffoldModule.Inner_ByteReader(eye, init_Vec3_ce_DataUtil(currentRotation)));
      if (this.flags_Z()) {
         point = init_Vec3_Vec3(point, fullFace, offset);
      }

      return point.subtract(offset);
   }

   private static Vec3 init_Vec3_Vec3(Vec3 point, ScaffoldModule.Inner_DataUtil face, Vec3 offset) {
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

   private ScaffoldModule.Inner_DataUtil init_aI_DataUtil_aI_DataUtil(ScaffoldModule.Inner_DataUtil trimmedFace, Vec3 eye, ScaffoldModule.Inner_HashUtil optimalLine) {
      if (optimalLine == null) {
         return trimmedFace;
      }

      Vec3 nearest = init_Vec3_aI_HashUtil_Vec3(optimalLine, MC.player.position());
      Vec3 directionToLine = MC.player.position().subtract(nearest).normalize();
      Vec3 collision = load_Vec3_aI_DataUtil_aI_ByteReader(trimmedFace, new ScaffoldModule.Inner_ByteReader(eye, optimalLine.load()));
      if (collision == null) {
         return trimmedFace;
      }

      Vec3 b = MC.player.position().add(directionToLine.scale(2.0));
      AABB crop = new AABB(collision.x, MC.player.getY() - 2.0, collision.z, b.x, MC.player.getY() + 1.0, b.z);
      ScaffoldModule.Inner_DataUtil clamped = trimmedFace.init(crop);
      return clamped.init_D() < 1.0E-4 ? trimmedFace : clamped;
   }

   private static int init_I_aI_StreamUtil_aI_StreamUtil(ScaffoldModule.Inner_StreamUtil first, ScaffoldModule.Inner_StreamUtil second) {
      int normal = Double.compare(init_D_aI_StreamUtil(first), init_D_aI_StreamUtil(second));
      return normal != 0 ? normal : Double.compare(first.init_Vec3().y, second.init_Vec3().y);
   }

   private static double init_D_aI_StreamUtil(ScaffoldModule.Inner_StreamUtil sample) {
      Vec3 centered = sample.init_Vec3().subtract(0.5, 0.5, 0.5);
      double x = centered.x * sample.init_Direction().getStepX();
      double y = centered.y * sample.init_Direction().getStepY();
      double z = centered.z * sample.init_Direction().getStepZ();
      return x * x + y * y + z * z;
   }

   private static Vec3 init_Vec3_aI_DataUtil_aI_ByteReader(ScaffoldModule.Inner_DataUtil face, ScaffoldModule.Inner_ByteReader line) {
      Vec3 intersection = load_Vec3_aI_DataUtil_aI_ByteReader(face, line);
      List<ScaffoldModule.Inner_PacketUtil> edges = face.init_List();
      Vec3 center = face.load_Vec3();
      if (intersection != null) {
         boolean inside = true;
         Iterator bestDistance = edges.iterator();

         while (true) {
            if (bestDistance.hasNext()) {
               ScaffoldModule.Inner_PacketUtil edge = (ScaffoldModule.Inner_PacketUtil)bestDistance.next();
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

      for (ScaffoldModule.Inner_PacketUtil edge : edges) {
         ScaffoldModule.Inner_PoolEntry pair = init_aI_PoolEntry_aI_PacketUtil_aI_ByteReader(edge, line);
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

   private static Vec3 load_Vec3_aI_DataUtil_aI_ByteReader(ScaffoldModule.Inner_DataUtil face, ScaffoldModule.Inner_ByteReader line) {
      Vec3 dimensions = face.init_Vec3();
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

   private static ScaffoldModule.Inner_PoolEntry init_aI_PoolEntry_aI_PacketUtil_aI_ByteReader(ScaffoldModule.Inner_PacketUtil segment, ScaffoldModule.Inner_ByteReader line) {
      Vec3 firstDirection = segment.init_Vec3();
      Vec3 secondDirection = line.load();
      Vec3 delta = segment.load().subtract(line.init());
      double a = firstDirection.dot(firstDirection);
      double b = firstDirection.dot(secondDirection);
      double c = secondDirection.dot(secondDirection);
      double d = firstDirection.dot(delta);
      double e = secondDirection.dot(delta);
      double determinant = a * c - b * b;
      ScaffoldModule.Inner_IoWorker best = null;
      if (Math.abs(determinant) > 1.0E-9) {
         best = init_aI_IoWorker_aI_IoWorker(best, segment, line, (b * e - c * d) / determinant, (a * e - b * d) / determinant);
      }

      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, 0.0, e / c);
      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, 1.0, (b + e) / c);
      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, Mth.clamp(-d / a, 0.0, 1.0), 0.0);
      best = init_aI_IoWorker_aI_IoWorker(best, segment, line, 0.0, e / c);
      return best == null ? null : new ScaffoldModule.Inner_PoolEntry(best.init_Vec3(), best.load());
   }

   private static ScaffoldModule.Inner_IoWorker init_aI_IoWorker_aI_IoWorker(
      ScaffoldModule.Inner_IoWorker best, ScaffoldModule.Inner_PacketUtil segment, ScaffoldModule.Inner_ByteReader line, double firstParameter, double secondParameter
   ) {
      if (Double.isFinite(firstParameter) && Double.isFinite(secondParameter) && !(firstParameter < -1.0E-9) && !(firstParameter > 1.000000001)) {
         double first = Mth.clamp(firstParameter, 0.0, 1.0);
         Vec3 firstPoint = segment.init(first);
         Vec3 secondPoint = line.init(secondParameter);
         double distance = firstPoint.distanceToSqr(secondPoint);
         return best != null && !(distance < best.init_D() - 1.0E-9) ? best : new ScaffoldModule.Inner_IoWorker(firstPoint, secondPoint, distance);
      } else {
         return best;
      }
   }

   private boolean init_Z_BlockState_BlockPos(BlockState state, BlockPos pos) {
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
      return collision.isEmpty() ? false : !Shapes.joinIsNotEmpty(collision.getFaceShape(Direction.UP), init_VoxelShape, BooleanOp.ONLY_SECOND);
   }

   private boolean load_Z_BlockState_BlockPos(BlockState state, BlockPos pos) {
      boolean supported = load_Z_BlockState(state, MC.level, pos, CollisionContext.of(MC.player));
      if (!supported) {
         this.read_Map.remove(pos);
      }

      return supported && !this.read_Map.containsKey(pos);
   }

   static boolean load_Z_BlockState(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return !state.isAir() && !state.canBeReplaced() && !state.getCollisionShape(level, pos, context).isEmpty();
   }

   public static void sync_V_6() {
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled()) {
         scaffold.h_V();
      }
   }

   public static void init_V_F_F(float appliedYaw, float appliedPitch) {
      f_l_F = appliedYaw;
      f_bS_I = ck.init_ck().state_I();
      if (al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold && scaffold.isEnabled()) {
         scaffold.init_ce_DataUtil = new ce.Inner_DataUtil(appliedYaw, appliedPitch);
         scaffold.parse_F = appliedYaw;
      }
   }

   private void h_V() {
      if (this.index_Z()) {
         if (this.read_aI_TableIndex != null && !load_Z_I_I(this.f_aM_I, this.f_aN_I)) {
            BlockPos pendingCell = this.read_aI_TableIndex.load();
            if (load_Z_BlockState(MC.level.getBlockState(pendingCell), MC.level, pendingCell, CollisionContext.of(MC.player))) {
               this.init_V_BlockPos_I_2(pendingCell, this.f_aM_I);
            }
         }

         BlockPos nearbyFooting = MC.player == null ? null : this.init_BlockPos_LocalPlayer(MC.player);
         BlockPos previousFooting = this.flush_BlockPos;
         if (nearbyFooting == null && MC.player != null && previousFooting != null && load_Z_Vec3_BlockPos(MC.player.position(), previousFooting)) {
            nearbyFooting = previousFooting;
         }

         if (nearbyFooting != null
            && (
               !this.load_Z_BlockState_BlockPos(MC.level.getBlockState(nearbyFooting), nearbyFooting)
                  || !init_Z_BlockPos_2(nearbyFooting, this.f_aN_I, this.flush_ArrayDeque)
            )) {
            nearbyFooting = null;
         }

         this.block_V();
         this.f_z_I = ck.init_ck().state_I();
         this.flush_BlockPos = nearbyFooting;
         this.f_ac_I = nearbyFooting == null ? -2147483648 : ck.init_ck().state_I();
      }

      this.fill_V();
      this.load_aI_TableIndex = null;
      this.f_aG_I = -1;
      this.f_aH_I = 0;
      this.f_ca_I = 0;
      this.prev_BlockPos = null;
      this.write_Direction = null;
      this.value_BlockPos = null;
      this.f_bR_I = 0;
      this.data_BlockPos = null;
      this.f_bQ_I = -2147483648;
      this.pop_BlockPos = null;
      this.init_Direction = null;
      this.f_ag_I = 0;
      this.f_ah_I = 0;
      this.f_ar_I = 0;
      this.valid_BlockPos = null;
      this.update_Direction = null;
      this.f_bx_I = 0;
      this.set_BlockPos = null;
      this.f_bw_I = -2147483648;
      this.flush_Vec3 = null;
      this.close_Vec3 = Vec3.ZERO;
      this.f_ae_I = 0;
      this.f_af_I = 0;
      this.f_cd_I = -2147483648;
      if (!this.index_Z()) {
         this.flush_ArrayDeque.clear();
      }

      if (this.init_aI_SyncManager != ScaffoldModule.Inner_SyncManager.IDLE) {
         this.init_V_Str_3("position-correction");
      }

      if (this.get_Z() && MC != null && MC.player != null) {
         Vec3 p = MC.player.position();
         AsyncTraceLogger.init(String.format(Locale.ROOT, "[scaffold-live] t%03d setback     pos=%.2f,%.2f,%.2f - position state cleared", this.f_au_I, p.x, p.y, p.z));
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

   private void load_V_Z(boolean restoreSlot) {
      autismclient.util.as.update_V();
      if (restoreSlot
         && this.bool("switch-back")
         && this.f_j_I >= 0
         && MC != null
         && MC.player != null
         && !MacroExecutor.write_Z()
         && MC.player.getInventory().getSelectedSlot() != this.f_j_I
         && autismclient.util.ah.write_Z(this.id())) {
         try {
            autismclient.util.as.init_V_I(this.f_j_I);
         } finally {
            autismclient.util.ah.read_V(this.id());
         }
      }

      this.f_j_I = -1;
      this.f_k_I = -1;
      this.f_l_I = -1;
      this.f_m_I = 0;
      this.write_Z = false;
      this.init_ce_DataUtil = MC != null && MC.player != null ? ce.init(MC.player) : null;
      this.load_V_18();
      this.read_V_12();
      this.load_aI_TableIndex = null;
      this.f_aG_I = -1;
      this.read_aI_TableIndex = null;
      this.read_aI_HashUtil = null;
      this.sync_Vec3 = null;
      this.f_aJ_I = -2147483648;
      this.init_aI_SyncManager = ScaffoldModule.Inner_SyncManager.IDLE;
      this.put_J = 0L;
      this.init_InteractionHand = null;
      this.chunk_Z = false;
      this.f_aK_I = 0;
      this.table_Z = false;
      this.f_aL_I = 0;
      this.update_ce_DataUtil = null;
      this.sync_ce_DataUtil = null;
      this.f_aM_I = -1;
      this.store_Z = false;
      this.drain_Z = false;
      this.fill_Z = false;
      this.write_Str = "--";
      this.flush_ArrayDeque.clear();
      this.read_Map.clear();
      this.load_ClientLevel = null;
      this.f_aN_I = -2147483648;
      this.f_aO_I = -2147483648;
      init_ConcurrentLinkedQu.clear();
      init_AtomicReference.set(null);
      this.mark_Z = false;
      this.scan_Z = false;
      this.parse_Z = false;
      this.bind_Z = false;
      this.f_aP_I = -2147483648;
      this.f_bT_I = 0;
      this.f_aV_I = -2147483648;
      this.f_aH_I = 0;
      this.f_ca_I = 0;
      this.f_aI_I = -2147483648;
      this.set_BlockPos = null;
      this.f_bw_I = -2147483648;
      this.flush_Vec3 = null;
      this.close_Vec3 = Vec3.ZERO;
      this.f_d_Z = false;
      this.f_e_Z = false;
      this.f_f_Z = false;
      this.f_g_Z = false;
      this.f_aW_I = 0;
      this.sync_Str = "--";
      this.f_y_D = 0.0 / 0.0;
      this.f_ay_I = -2147483648;
      this.count_Z = false;
      this.f_z_D = 0.0 / 0.0;
      this.f_az_I = -2147483648;
      this.flags_Z = false;
      this.level_Z = false;
      this.write_Input = null;
      this.f_aB_I = -2147483648;
      this.flush_Str = "--";
      this.close_Str = "--";
      this.f_B_D = 0.0;
      this.read_Direction = null;
      this.f_aC_I = -2147483648;
      this.f_A_D = 0.0;
      this.f_aD_I = -2147483648;
      this.f_h_Z = false;
      this.f_i_Z = false;
      this.f_b_F = 0.0F / 0.0F;
      this.open_Str = "-";
      this.f_ae_I = 0;
      this.f_af_I = 0;
      this.f_bV_I = 0;
      this.f_bW_I = 0;
      this.f_aU_I = -2147483648;
      this.start_BlockPos = null;
      this.next_Str = "air";
      this.f_j_Z = false;
      this.init_aI_TableIndex = null;
      this.get_BlockPos = null;
      this.f_aE_I = -1;
      this.f_aF_I = -2147483648;
      this.f_k_Z = false;
      this.f_l_Z = false;
      this.f_bz_I = -2147483648;
      this.parse_F = 0.0F / 0.0F;
      this.add_Str = "--";
      this.clear_Str = "--";
      this.clear_J = -1L;
      this.reset_J = -1L;
      this.f_m_Z = false;
      this.f_n_Z = true;
      this.f_o_Z = false;
      this.read_Vec3 = null;
      this.pop_Str = "--";
      this.init_StringBuilder.setLength(0);
      this.tick_Str = "--";
      this.f_aX_I = -1;
      this.f_aY_I = -1;
      this.poll_Vec3 = null;
      this.run_Str = "--";
      this.f_aZ_I = 0;
      this.f_ba_I = 0;
      this.f_bb_I = 0;
      this.f_bc_I = 0;
      this.f_bd_I = 0;
      this.f_be_I = 0;
      this.f_bk_I = 0;
      this.get_Str = "--";
      this.write_V_BlockPos(null);
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
      f_l_F = 0.0F / 0.0F;
      f_bS_I = -2147483648;
      this.prev_BlockPos = null;
      this.write_Direction = null;
      this.fill_V();
      this.f_c_Z = false;
      this.f_cb_I = -2147483648;
      this.f_cc_I = -2147483648;
      this.f_cd_I = -2147483648;
      this.a_V();
      this.valid_BlockPos = null;
      this.update_Direction = null;
      this.f_bx_I = 0;
      this.f_c_F = 0.0F / 0.0F;
      this.f_Z_D = 0.0 / 0.0;
      this.block_V();
      this.f_z_I = -2147483648;
      this.flush_BlockPos = null;
      this.f_ac_I = -2147483648;
      this.init_aI_HashUtil = null;
      this.flush_Z = false;
      this.data_BlockPos = null;
      this.f_bQ_I = -2147483648;
      this.pop_BlockPos = null;
      this.init_Direction = null;
      this.f_ag_I = 0;
      this.f_ah_I = 0;
      this.f_ar_I = 0;
      this.clear_V();
      this.init_BlockHitResult = null;
      this.f_ai_I = 0;
      this.f_aj_I = 0;
      this.read_ArrayDeque.clear();
      this.write_ArrayDeque.clear();
      this.run_J = -9223372036854775808L;
      this.update_ArrayDeque.clear();
      this.sync_ArrayDeque.clear();
      this.tick_J = -9223372036854775808L;
      this.chunk_F = 0.0F / 0.0F;
      this.get_J = -9223372036854775808L;
      this.set_J = -9223372036854775808L;
      this.tick_BlockPos = null;
      this.f_ak_I = -2147483648;
      this.size_BlockPos = null;
      this.f_bY_I = 0;
      this.f_bZ_I = 0;
      this.f_m_F = 0.0F / 0.0F;
      this.init_Map.clear();
      this.index_Z = false;
      this.f_ao_I = -2147483648;
      this.f_ap_I = 0;
      this.f_aq_I = 0;
      this.value_BlockPos = null;
      this.f_bR_I = 0;
      this.init_ArrayDeque.clear();
      this.load_ArrayDeque.clear();
      this.init_BlockPos = null;
      this.init_aI_BitSetUtil = null;
      this.f_n_I = 0;
      this.f_o_I = -2147483648;
      this.load_aI_HashUtil = null;
      this.f_p_I = -1;
      this.f_q_I = -1;
      this.f_r_I = 0;
      this.f_s_I = 0;
      this.node_F = 0.0F / 0.0F;
      this.task_F = 0.0F / 0.0F;
      this.f_t_I = 0;
      this.f_u_I = -2147483648;
   }

   private static List<BlockPos> read_List() {
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

   record Inner_ArrayUtil(Vec3 init_Vec3, Vec3 load, int init_I) {
   }

   private record Inner_BitSetUtil(BlockPos init_BlockPos, double init_D, double load) {
   }

   @FunctionalInterface
   private interface Inner_BufferHelper {
      Vec3 resolve(AABB var1, Vec3 var2);
   }

   private record Inner_ByteReader(Vec3 init, Vec3 load) {
      Vec3 init(double parameter) {
         return this.init.add(this.load.scale(parameter));
      }
   }

   private record Inner_CacheEntry(ScaffoldModule.Inner_DataPipe init_aI_DataPipe, int init_I) {
   }

   private enum Inner_CachePool {
      NONE,
      IMMINENT;
   }

   record Inner_CharEncoder(BlockPos init_BlockPos, Direction init_Direction) {
   }

   record Inner_ClassCache(int init, int load) {
   }

   record Inner_ConfigStore(ScaffoldModule.Inner_SyncManager init_aI_SyncManager, String init_Str) {
   }

   private record Inner_DataPipe(ScaffoldModule.Inner_TableIndex init_aI_TableIndex, boolean init_Z) {
   }

   private record Inner_DataUtil(Vec3 init, Vec3 load) {
      private Inner_DataUtil {
         Vec3 minimum = new Vec3(Math.min(init.x, load.x), Math.min(init.y, load.y), Math.min(init.z, load.z));
         Vec3 maximum = new Vec3(Math.max(init.x, load.x), Math.max(init.y, load.y), Math.max(init.z, load.z));
         init = minimum;
         load = maximum;
      }

      static ScaffoldModule.Inner_DataUtil init(AABB box, Direction side) {
         ScaffoldModule.Inner_DataUtil var10000;
         switch (side) {
            case EAST:
               var10000 = new ScaffoldModule.Inner_DataUtil(new Vec3(box.maxX, box.minY, box.minZ), new Vec3(box.maxX, box.maxY, box.maxZ));
               break;
            case WEST:
               var10000 = new ScaffoldModule.Inner_DataUtil(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.minX, box.maxY, box.maxZ));
               break;
            case SOUTH:
               var10000 = new ScaffoldModule.Inner_DataUtil(new Vec3(box.minX, box.minY, box.maxZ), new Vec3(box.maxX, box.maxY, box.maxZ));
               break;
            case NORTH:
               var10000 = new ScaffoldModule.Inner_DataUtil(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.maxX, box.maxY, box.minZ));
               break;
            case DOWN:
               var10000 = new ScaffoldModule.Inner_DataUtil(new Vec3(box.minX, box.minY, box.minZ), new Vec3(box.maxX, box.minY, box.maxZ));
               break;
            case UP:
               var10000 = new ScaffoldModule.Inner_DataUtil(new Vec3(box.minX, box.maxY, box.minZ), new Vec3(box.maxX, box.maxY, box.maxZ));
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      Vec3 init_Vec3() {
         return this.load.subtract(this.init);
      }

      Vec3 load_Vec3() {
         return this.init.lerp(this.load, 0.5);
      }

      double init_D() {
         Vec3 dimensions = this.init_Vec3();
         return dimensions.x * dimensions.y + dimensions.y * dimensions.z + dimensions.x * dimensions.z;
      }

      ScaffoldModule.Inner_DataUtil init(double minimumY) {
         return new ScaffoldModule.Inner_DataUtil(
            new Vec3(this.init.x, Math.max(this.init.y, minimumY), this.init.z), new Vec3(this.load.x, Math.max(this.load.y, minimumY), this.load.z)
         );
      }

      ScaffoldModule.Inner_DataUtil load(double amount) {
         Vec3 inset = this.init_Vec3().scale(amount);
         return new ScaffoldModule.Inner_DataUtil(this.init.add(inset), this.load.subtract(inset));
      }

      ScaffoldModule.Inner_DataUtil init(Vec3 offset) {
         return new ScaffoldModule.Inner_DataUtil(this.init.add(offset), this.load.add(offset));
      }

      ScaffoldModule.Inner_DataUtil init(AABB box) {
         return new ScaffoldModule.Inner_DataUtil(init(this.init, box), init(this.load, box));
      }

      List<ScaffoldModule.Inner_PacketUtil> init_List() {
         Vec3 dimensions = this.init_Vec3();
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

         List<ScaffoldModule.Inner_PacketUtil> edges = new ArrayList<>(4);
         if (first.lengthSqr() > 1.0E-9) {
            edges.add(new ScaffoldModule.Inner_PacketUtil(this.init, this.init.add(first)));
            edges.add(new ScaffoldModule.Inner_PacketUtil(this.load, this.load.subtract(first)));
         }

         if (second.lengthSqr() > 1.0E-9) {
            edges.add(new ScaffoldModule.Inner_PacketUtil(this.init, this.init.add(second)));
            edges.add(new ScaffoldModule.Inner_PacketUtil(this.load, this.load.subtract(second)));
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

   enum Inner_EventBus {
      CONNECTOR,
      SUPPORT,
      RISER,
      READY;
   }

   record Inner_FieldCache(Vec3 init_Vec3, boolean init_Z) {
   }

   record Inner_FileCache(boolean init_Z, int init_I, int load, int read, int write, int update, int sync) {
      boolean init(BlockPos pos, boolean rising) {
         int perp = this.init_Z ? pos.getZ() : pos.getX();
         int index = this.init_Z ? pos.getX() : pos.getZ();
         boolean onRow = perp == this.init_I;
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
            int perp = this.init_Z ? pos.getZ() : pos.getX();
            if (perp != this.init_I) {
               return null;
            } else {
               int index = this.init_Z ? pos.getX() : pos.getZ();
               if (this.init_Z) {
                  return index > this.read ? Direction.EAST : Direction.WEST;
               } else {
                  return index > this.read ? Direction.SOUTH : Direction.NORTH;
               }
            }
         }
      }
   }

   record Inner_HashUtil(Vec3 init, Vec3 load) {
   }

   private record Inner_IoWorker(Vec3 init_Vec3, Vec3 load, double init_D) {
   }

   private record Inner_JsonUtil(long init_J, BlockPos init_BlockPos) {
   }

   private enum Inner_KeyHolder {
      RELEASED,
      FORWARD,
      HOLD;
   }

   record Inner_ListPool(BlockPos init_BlockPos, int init_I) {
   }

   record Inner_MapStore(boolean init_Z, float init_F) {
   }

   record Inner_MethodUtil(double init_D, double load, double read, boolean init_Z) {
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

   record Inner_NetHelper(
      int init_I,
      BlockPos init_BlockPos,
      Direction init_Direction,
      Vec3 init_Vec3,
      long init_J,
      InteractionHand init_InteractionHand,
      ScaffoldModule.Inner_RenderState init_aI_RenderState,
      ch.Inner_CachePool init_ch_CachePool
   ) {
      Inner_NetHelper(int sequence, BlockPos support, Direction face, Vec3 location, long nanos) {
         this(sequence, support, face, location, nanos, InteractionHand.MAIN_HAND, null, null);
      }
   }

   enum Inner_NodeWalker {
      FORWARD,
      PLACING,
      RETURNED;
   }

   private record Inner_PacketUtil(Vec3 init, Vec3 load) {
      Vec3 init_Vec3() {
         return this.load.subtract(this.init);
      }

      Vec3 init(double parameter) {
         return this.init.add(this.init_Vec3().scale(parameter));
      }

      public Vec3 load() {
         return this.init;
      }

      public Vec3 read() {
         return this.load;
      }
   }

   record Inner_PathHelper(boolean init_Z, int init_I, int load, int read, Vec3 init_Vec3) {
   }

   private record Inner_PoolEntry(Vec3 init, Vec3 load) {
   }

   private record Inner_RefQueue(
      ScaffoldModule.Inner_DataUtil init_aI_DataUtil, Vec3 init_Vec3, ce.Inner_DataUtil init_ce_DataUtil, BlockHitResult init_BlockHitResult, int init_I
   ) {
   }

   record Inner_RenderState(
      long init_J,
      int init_I,
      InteractionHand init_InteractionHand,
      BlockPos init_BlockPos,
      BlockPos load,
      Direction init_Direction,
      ce.Inner_DataUtil init_ce_DataUtil
   ) {
   }

   private record Inner_RingBuffer(BlockPos init_BlockPos, double init_D, double load, double read) {
   }

   private static final class Inner_StateCache extends WeakReference<Packet<?>> {
      private final int init;

      Inner_StateCache(Packet<?> packet) {
         super(packet);
         this.init = System.identityHashCode(packet);
      }

      Inner_StateCache(Packet<?> packet, ReferenceQueue<Packet<?>> queue) {
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
         } else if (!(other instanceof ScaffoldModule.Inner_StateCache identity)) {
            return false;
         } else {
            Packet<?> packet = this.get();
            return packet != null && packet == identity.get();
         }
      }
   }

   enum Inner_StreamGate {
      WAITING,
      PLACED,
      FAILED;
   }

   private record Inner_StreamUtil(ScaffoldModule.Inner_DataUtil init_aI_DataUtil, Vec3 init_Vec3, Direction init_Direction) {
   }

   enum Inner_SyncManager {
      IDLE,
      ARMED,
      SENT,
      PREDICTED,
      RECONCILING,
      FAILED;
   }

   record Inner_TableIndex(
      BlockPos init_BlockPos, BlockPos load, Direction init_Direction, BlockHitResult init_BlockHitResult, ce.Inner_DataUtil init_ce_DataUtil, double init_D
   ) {
   }

   private record Inner_TaskQueue(boolean init, boolean load, boolean read) {
   }

   private record Inner_TaskTimer(double init_D, double load_D, double read, double write, double update, boolean init_Z, boolean load_Z) {
   }

   record Inner_TextFormat(ce.Inner_DataUtil init_ce_DataUtil, ce.Inner_DataUtil load, int init_I) {
   }

   record Inner_TokenStore(ScaffoldModule.Inner_c init_aI_c, int init_I, ScaffoldModule.Inner_c load_aI_c, int load_I) {
   }

   record Inner_UiHelper(int init_I, BlockPos init_BlockPos, BlockPos load) {
   }

   private record Inner_UtilCore(ce.Inner_DataUtil init_ce_DataUtil, ScaffoldModule.Inner_a init_aI_a) {
   }

   enum Inner_ValueSink {
      IDLE,
      RUNNING,
      FORWARD_DWELL,
      RECOVERING,
      LAUNCH,
      AIMING,
      RETURNING;
   }

   enum Inner_a {
      FORWARD,
      PLACEMENT,
      HOLD,
      RETURN;
   }

   record Inner_b(boolean init_Z, boolean load) {
      static final ScaffoldModule.Inner_b init_aI_b = new ScaffoldModule.Inner_b(false, false);
   }

   enum Inner_c {
      NONE,
      LEFT,
      RIGHT;
   }
}
