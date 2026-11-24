package autismclient.util.g;

import autismclient.AutismClientAddon;
import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuInput;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.compat263.Compat263;
import autismclient.compat263.CompatSwingPacket;
import autismclient.mixin.accessor.AutismClientConnectionAccessor;
import autismclient.mixin.accessor.AutismFishingHookAccessor;
import autismclient.mixin.accessor.AutismMoveEntityPacketAccessor;
import autismclient.mixin.accessor.AutismPlayerCommandPacketAccessor;
import autismclient.mixin.accessor.AutismRotateHeadPacketAccessor;
import autismclient.util.AutismConfig;
import autismclient.util.AutismPayloadSupport;
import autismclient.util.N;
import autismclient.util.aA;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.bW;
import autismclient.util.bm;
import autismclient.util.bw;
import autismclient.util.ck;
import autismclient.util.macro.A;
import autismclient.util.macro.I;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.aB;
import autismclient.util.macro.aD;
import autismclient.util.macro.aQ;
import autismclient.util.macro.ac;
import autismclient.util.macro.ah;
import autismclient.util.macro.ai;
import autismclient.util.macro.ao;
import autismclient.util.macro.y;
import com.google.common.hash.HashCode;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.timeout.ReadTimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkBatchSizeCalculator;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.HashedStack;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.HashedPatchMap.HashGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.LastSeenMessagesTracker.Update;
import net.minecraft.network.chat.RemoteChatSession.Data;
import net.minecraft.network.chat.SignedMessageChain.Encoder;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundResetChatPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket.NodeBuilder;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot;
import net.minecraft.network.protocol.game.GameProtocols.Context;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.HashOps;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Crypt.SaltSupplier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.ProfilePublicKey.ValidationException;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior.MinecartStep;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.TeamColor;
import org.jspecify.annotations.Nullable;

public final class ClassCache implements EventBus {
   private static final long init_J = 20000L;
   private static final double init_D = 0.08;
   private static final double load_D = 0.98;
   private static final double read_D = -3.92;
   private static final long load_J = 5000L;
   private static final long read_J = 1000L;
   private static final long write_J = 1000L;
   private static final long update_J = 1000L;
   private static final long sync_J = 60000L;
   private static final long flush_J = 500L;
   private static final long close_J = 2000L;
   private static final long open_J = 30000L;
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static final NodeBuilder<Object> init_ClientboundCommand = new NodeBuilder<Object>() {
      @Override
      public ArgumentBuilder<Object, ?> createLiteral(String id) {
         return LiteralArgumentBuilder.literal(id);
      }

      @Override
      public ArgumentBuilder<Object, ?> createArgument(String id, ArgumentType<?> argumentType, @Nullable Identifier suggestionId) {
         return RequiredArgumentBuilder.argument(id, argumentType);
      }

      @Override
      public ArgumentBuilder<Object, ?> configure(ArgumentBuilder<Object, ?> builder, boolean executable, boolean restricted) {
         if (executable) {
            builder.executes(context -> 0);
         }

         return builder;
      }
   };
   private final long poll_J;
   private final FieldCache.Inner_BufferHelper init_FieldCache_BufferH;
   private final bW init_bW;
   private final String init_Str;
   private volatile IoWorker init_IoWorker;
   private final ClassCache.Inner_TokenStore init_ClassCache_TokenSt;
   private final Executor init_Executor;
   private final autismclient.util.g.a.TaskQueue init_TaskQueue_2;
   private final StreamGate.Inner_CachePool init_StreamGate_CachePo;
   private volatile StreamGate.Inner_CachePool load_StreamGate_CachePo;
   private final AtomicReference<ClassCache.Inner_PathHelper> init_AtomicReference = new AtomicReference<>(ClassCache.Inner_PathHelper.init);
   private final TaskQueue init_TaskQueue = new TaskQueue();
   private volatile TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil;
   private volatile String load_Str = "Connection closed";
   private final Map<Identifier, byte[]> init_Map = new LinkedHashMap<>();
   private final Map<UUID, String> load_Map = new ConcurrentHashMap<>();
   private final Set<UUID> init_Set = ConcurrentHashMap.newKeySet();
   private static final int init_I = 8192;
   private final Object init_Obj = new Object();
   private final Map<String, ClassCache.Inner_BitSetUtil> read_Map = new LinkedHashMap<>();
   private final Map<String, Map<String, ClassCache.Inner_RefQueue>> write_Map = new LinkedHashMap<>();
   private final Map<DisplaySlot, String> update_Map = new EnumMap<>(DisplaySlot.class);
   private final Map<String, ClassCache.Inner_CharEncoder> sync_Map = new LinkedHashMap<>();
   private final Map<String, String> flush_Map = new HashMap<>();
   private final Object load_Obj = new Object();
   private final Object read_Obj = new Object();
   private ClassCache.Inner_ConfigStore init_ClassCache_ConfigS;
   private long push_J;
   private volatile LastSeenMessagesTracker init_LastSeenMessagesTr = new LastSeenMessagesTracker(20);
   private volatile MessageSignatureCache init_MessageSignatureCa = MessageSignatureCache.createDefault();
   private final Object write_Obj = new Object();
   private final Object update_Obj = new Object();
   private volatile String read_Str = "Queued";
   private volatile long pop_J = System.currentTimeMillis();
   private volatile int load_I = -1;
   private volatile long tick_J;
   private volatile Connection init_Connection;
   private volatile int read_I;
   private volatile MapStore.Inner_CachePool init_MapStore_CachePool = MapStore.Inner_CachePool.init;
   private volatile boolean init_Z;
   private volatile boolean load_Z;
   private volatile UUID init_UUID;
   private volatile GameProfile init_GameProfile;
   private volatile String write_Str = "";
   private volatile int write_I;
   private volatile SyncManager.Inner_DataUtil init_SyncManager_DataUt;
   private volatile boolean read_Z;
   private volatile FeatureFlagSet init_FeatureFlagSet = FeatureFlags.DEFAULT_FLAGS;
   private volatile Frozen init_RegistryAccess_Fro;
   private volatile PositionMoveRotation init_PositionMoveRotati = new PositionMoveRotation(Vec3.ZERO, Vec3.ZERO, 0.0F, 0.0F);
   private final Object sync_Obj = new Object();
   private volatile boolean write_Z;
   private volatile boolean update_Z;
   private final ChunkBatchSizeCalculator init_ChunkBatchSizeCalc = new ChunkBatchSizeCalculator();
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
   private volatile boolean sync_Z;
   private volatile boolean flush_Z;
   private volatile boolean close_Z;
   private volatile long run_J = 9223372036854775807L;
   private volatile long get_J = 9223372036854775807L;
   private volatile long set_J = 9223372036854775807L;
   private volatile double write_D;
   private volatile boolean open_Z = true;
   private volatile boolean poll_Z;
   private volatile boolean push_Z;
   private volatile int update_I = -1;
   private volatile double update_D;
   private volatile double sync_D;
   private volatile double flush_D;
   private volatile double close_D;
   private volatile boolean pop_Z;
   private volatile long put_J;
   private volatile float init_F;
   private static final double open_D = 0.03999999910593033;
   private volatile long add_J;
   private volatile boolean tick_Z;
   private volatile double poll_D;
   private volatile double push_D;
   private volatile long clear_J;
   private volatile long reset_J;
   private volatile boolean run_Z;
   private volatile long start_J;
   private volatile long stop_J;
   private volatile int sync_I;
   private long next_J;
   private static final long prev_J = 30000L;
   private static final long valid_J = 500L;
   private volatile boolean get_Z;
   private volatile boolean set_Z = true;
   private volatile boolean put_Z;
   private volatile long size_J;
   private volatile long data_J;
   private final AtomicBoolean load_AtomicBoolean = new AtomicBoolean();
   private final MapStore init_MapStore = new MapStore(this::init_MapStore_JsonUtil, this.load_AtomicBoolean::get, this::init_Z_MapStore_JsonUtil);
   private volatile boolean add_Z;
   private volatile long value_J;
   private volatile int flush_I;
   private volatile CommandDispatcher<Object> init_CommandDispatcher = new CommandDispatcher<>();
   private volatile boolean clear_Z;
   private long cache_J;
   private static final long state_J = 750L;
   private final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private final Object flush_Obj = new Object();
   private final Map<Integer, ClassCache.Inner_TaskTimer> close_Map = new LinkedHashMap<>();
   private volatile ClientInformation init_ClientInformation = ClientInformation.createDefault();
   private volatile int close_I = -1;
   private volatile int open_I;
   private volatile int poll_I;
   private volatile String update_Str = "";
   private volatile String sync_Str = "";
   private volatile Component init_Component = Component.empty();
   private volatile long queue_J;
   private volatile long buffer_J;
   private volatile ClassCache.Inner_TextFormat init_ClassCache_TextFor = ClassCache.Inner_TextFormat.init;
   private volatile boolean reset_Z;
   private volatile boolean start_Z;
   private volatile long entry_J;
   private volatile int push_I = -1;
   private final Object close_Obj = new Object();
   private final ArrayDeque<String> init_ArrayDeque = new ArrayDeque<>();
   private volatile long node_J;
   private static final int pop_I = 64;
   private volatile boolean stop_Z;
   private volatile long task_J;
   private final Object open_Obj = new Object();
   private final ArrayDeque<ClassCache.Inner_PoolEntry> load_ArrayDeque = new ArrayDeque<>();
   private static final int tick_I = 128;
   private volatile BlockPos init_BlockPos;
   private volatile boolean next_Z;
   private volatile boolean prev_Z = true;
   private volatile BlockPos load_BlockPos;
   private volatile int run_I = -1;
   private volatile boolean valid_Z;
   private volatile long pool_J;
   private final Object poll_Obj = new Object();
   private final ArrayDeque<ClassCache.Inner_MethodUtil> read_ArrayDeque = new ArrayDeque<>();
   private static final int get_I = 64;
   private final Map<String, Long> open_Map = new ConcurrentHashMap<>();
   private static final int set_I = 256;
   private final Map<Long, BlockState> poll_Map = new ConcurrentHashMap<>();
   private static final int put_I = 512;
   private volatile boolean size_Z;
   private final CacheEntry init_CacheEntry = new CacheEntry();
   private volatile int add_I;
   private volatile int clear_I = -1;
   private volatile int reset_I;
   private volatile boolean data_Z;
   private final Object push_Obj = new Object();
   private final List<ItemStack> init_List = new ArrayList<>();
   private final List<ItemStack> load_List = new ArrayList<>(Collections.nCopies(46, ItemStack.EMPTY));
   private volatile ItemStack init_ItemStack = ItemStack.EMPTY;
   private final int[] init_IArr = new int[16];
   private int start_I;
   private MerchantOffers init_MerchantOffers;
   private int stop_I;
   private int next_I;
   private boolean value_Z;
   private static final int prev_I = 128;
   private static final int valid_I = 100000;
   private static final long index_J = 50L;
   private final ArrayDeque<ClassCache.Inner_TableIndex> write_ArrayDeque = new ArrayDeque<>();
   private ClassCache.Inner_TableIndex init_ClassCache_TableIn;
   private long count_J;
   private long flags_J;
   private long level_J;
   private boolean cache_Z;
   private boolean state_Z;
   private ClassCache.Inner_NetHelper init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
   private volatile ClassCache.Inner_RingBuffer init_ClassCache_RingBuf;
   private volatile ClassCache.Inner_ArrayUtil init_ClassCache_ArrayUt;
   private volatile boolean queue_Z;
   private volatile boolean buffer_Z;
   private volatile HashGenerator init_HashedPatchMap_Has;
   private volatile Frozen load_RegistryAccess_Fro;
   private volatile String flush_Str = "";
   private volatile String close_Str = "";
   private volatile String open_Str = "";
   private volatile int size_I = -1;
   private volatile float load_F = 20.0F;
   private volatile float read_F = 20.0F;
   private volatile double pop_D = 4.5;
   private volatile double tick_D = 3.0;
   private volatile int data_I = 20;
   private volatile long frame_J;
   private volatile long block_J = init_AtomicLong.incrementAndGet() << 32;
   private volatile Encoder init_SignedMessageChain = Encoder.UNSIGNED;
   private volatile LocalChatSession init_LocalChatSession;
   private long chunk;
   private int value_I;
   private final FieldCache.Inner_DataUtil init_FieldCache_DataUti;
   private static final long table = 40000L;
   private volatile boolean entry_Z;
   private long store;
   private final String poll_Str;
   private volatile Map<String, String> push_Map;
   private final autismclient.util.b.StreamUtil init_StreamUtil = new autismclient.util.b.StreamUtil();
   private volatile long drain;
   private volatile boolean node_Z;
   private static final long fill = 10000L;
   private final autismclient.util.f.CachePool init_CachePool = new autismclient.util.f.CachePool(new ClassCache.Inner_PacketUtil());
   private volatile TextFormat init_TextFormat;
   private volatile boolean task_Z;
   private volatile boolean pool_Z;
   private volatile boolean index_Z;
   private volatile String push_Str = "";
   private volatile boolean count_Z;
   private volatile int cache_I;
   private volatile int state_I;
   private volatile aM init_aM;
   private volatile boolean flags_Z;
   private volatile String pop_Str = "";
   private volatile ClassCache.Inner_UiHelper init_ClassCache_UiHelpe = ClassCache.Inner_UiHelper.init_ClassCache_UiHelpe();
   private volatile ClassCache.Inner_RenderState init_ClassCache_RenderS;
   private final TableIndex init_TableIndex;
   private final AtomicReference<String> load_AtomicReference;
   private final StateCache init_StateCache;
   private volatile CachePool init_CachePool_2;
   private volatile String tick_Str;
   private volatile long mark;
   private volatile long scan;
   private volatile String run_Str;
   private volatile String get_Str;
   private final Map<String, Long> pop_Map;
   private volatile ClassCache.Inner_FieldCache init_ClassCache_FieldCa;
   private static final int queue_I = 2048;
   private volatile long parse;
   private static final long bind = 30000L;
   private static final Pattern init_Pattern = Pattern.compile("captcha|verif|anti-?bot|bot ?check|\\bhuman\\b|prove you", 2);
   private volatile int buffer_I;
   private static final int entry_I = 5;
   private long f_a_J;
   private int node_I;
   private static final long f_b_J = 10000L;
   private static final int task_I = 12;
   private static final int pool_I = 256;
   private volatile boolean level_Z;
   private volatile Vec3 init_Vec3;
   private volatile long f_c_J;
   private final Object pop_Obj;
   private final Object tick_Obj;
   private final DataPipe init_DataPipe;
   private final ArrayDeque<ClassCache.Inner_BufferHelper> update_ArrayDeque;
   private final Object run_Obj;
   private long f_d_J;
   private long f_e_J;
   private volatile boolean frame_Z;
   private volatile long f_f_J;
   private volatile long f_g_J;
   private volatile InteractionHand init_InteractionHand;
   private volatile int index_I;
   private volatile long f_h_J;
   private volatile int count_I;
   private volatile long f_i_J;
   private volatile int flags_I;
   private volatile long f_j_J;
   private final AtomicReference<Vec3> read_AtomicReference;
   private volatile double run_D;
   private volatile double get_D;
   private volatile int level_I;
   private static final long f_k_J = 15000L;
   private volatile boolean block_Z;
   private volatile long f_l_J;

   private static boolean m901023() {
      return true;
   }

   ClassCache(
      long generation,
      FieldCache.Inner_BufferHelper spec,
      bW proxy,
      String proxyName,
      IoWorker policy,
      FieldCache.Inner_DataUtil loginMode,
      String profileName,
      Map<String, String> formValues,
      final ClassCache.Inner_TokenStore sink,
      Executor worker,
      StreamGate.Inner_CachePool viaTarget
   ) {
      this.init_ClassCache_RenderS = ClassCache.Inner_RenderState.init_ClassCache_RenderS;
      this.init_TableIndex = new TableIndex();
      this.load_AtomicReference = new AtomicReference<>();
      this.init_StateCache = new StateCache();
      this.init_CachePool_2 = new CachePool();
      this.tick_Str = "";
      this.run_Str = "";
      this.get_Str = "";
      this.pop_Map = new ConcurrentHashMap<>();
      this.buffer_I = 0;
      this.init_Vec3 = Vec3.ZERO;
      this.pop_Obj = new Object();
      this.tick_Obj = new Object();
      this.init_DataPipe = new DataPipe();
      this.update_ArrayDeque = new ArrayDeque<>();
      this.run_Obj = new Object();
      this.init_InteractionHand = InteractionHand.MAIN_HAND;
      this.index_I = -1;
      this.count_I = -1;
      this.flags_I = -1;
      this.read_AtomicReference = new AtomicReference<>();
      this.poll_J = generation;
      this.init_FieldCache_BufferH = spec;
      this.init_bW = proxy;
      this.init_Str = proxyName != null && !proxyName.isBlank() ? proxyName : "Proxy Off";
      this.init_IoWorker = new IoWorker(policy);
      this.init_FieldCache_DataUti = loginMode == null ? FieldCache.Inner_DataUtil.load : loginMode;
      this.poll_Str = profileName == null ? "" : profileName;
      this.push_Map = formValues == null ? Map.of() : Map.copyOf(formValues);
      this.init_ClassCache_TokenSt = sink;
      this.init_Executor = worker;
      this.init_StreamGate_CachePo = viaTarget;
      this.load_StreamGate_CachePo = viaTarget;
      this.init_RegistryAccess_Fro = ClientRegistryLayer.createRegistryAccess().compositeAccess();
      this.init_TaskQueue_2 = new autismclient.util.g.a.TaskQueue(new autismclient.util.g.a.TaskQueue.Inner_DataUtil() {
         @Override
         public void init(String message) {
            ClassCache.this.update_Str_Str(message);
         }

         @Override
         public void load(String command) {
            ClassCache.this.sync_Str_Str(command);
         }

         @Override
         public void read(String note) {
            sink.init_V_ClassCache_Str(ClassCache.this, note);
         }
      }, worker);
   }

   long close_J() {
      return this.poll_J;
   }

   String open_Str() {
      return this.init_FieldCache_BufferH.init_Str();
   }

   boolean tick_Z() {
      Connection current = this.init_Connection;
      return current != null && current.isConnected() && !this.load_AtomicBoolean.get();
   }

   boolean run_Z() {
      return this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && this.tick_Z() && this.write_Z;
   }

   ClassCache.Inner_PathHelper init_ClassCache_PathHel() {
      return this.init_AtomicReference.get();
   }

   String poll_Str() {
      return this.read_Str;
   }

   TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil() {
      return this.init_TaskQueue_DataUtil;
   }

   public ClassCache.Inner_FieldCache init_ClassCache_FieldCa() {
      SyncManager.Inner_DataUtil resolved = this.init_SyncManager_DataUt;
      String name = resolved == null ? this.init_FieldCache_BufferH.init_Str() : resolved.init_User().getName();
      ClassCache.Inner_PathHelper current = this.init_AtomicReference.get();
      PositionMoveRotation p = this.init_PositionMoveRotati;
      Vec3 pos = this.level_Z && System.currentTimeMillis() - this.f_c_J < 1000L ? this.init_Vec3 : p.position();
      boolean isConnected = this.tick_Z();
      String publishedScreen = this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.read && !this.data_Z ? this.update_Str : "";
      StreamGate.Inner_CachePool actualProtocol = this.load_StreamGate_CachePo;
      String publishedProtocol = actualProtocol != null && actualProtocol.init_Z() ? actualProtocol.init_Str() : "Native 26.2";
      boolean publishedReady = current == ClassCache.Inner_PathHelper.flush && isConnected && this.write_Z;
      boolean customMenuOpen = this.init_StreamUtil.init_CustomMenuSnapshot() != null;
      int publishedHotbar = this.add_I + 1;
      ClassCache.Inner_RenderState queue = this.init_ClassCache_RenderS;
      ClassCache.Inner_FieldCache cached = this.init_ClassCache_FieldCa;
      if (cached != null
         && Objects.equals(cached.init_Str(), this.init_FieldCache_BufferH.init_Str())
         && Objects.equals(cached.load_Str(), name)
         && Objects.equals(cached.read_Str(), this.init_Str)
         && Objects.equals(cached.write_Str(), publishedProtocol)
         && cached.init_ClassCache_PathHel() == current
         && Objects.equals(cached.update(), this.read_Str)
         && cached.init_I() == this.load_I
         && cached.init_Z() == isConnected
         && cached.load_Z() == publishedReady
         && cached.init_J() == this.tick_J
         && Objects.equals(cached.sync(), publishedScreen)
         && cached.read_Z() == customMenuOpen
         && Objects.equals(cached.flush(), this.flush_Str)
         && cached.load_I() == publishedHotbar
         && Objects.equals(cached.close(), this.close_Str)
         && cached.write_Z() == this.write_Z
         && Double.doubleToLongBits(cached.init_D()) == Double.doubleToLongBits(pos.x)
         && Double.doubleToLongBits(cached.load_D()) == Double.doubleToLongBits(pos.y)
         && Double.doubleToLongBits(cached.read_D()) == Double.doubleToLongBits(pos.z)
         && Float.floatToIntBits(cached.init_F()) == Float.floatToIntBits(this.load_F)
         && Float.floatToIntBits(cached.load_F()) == Float.floatToIntBits(this.read_F)
         && cached.read_I() == this.data_I
         && cached.load_J() == this.block_J
         && Objects.equals(cached.open(), this.pop_Str)
         && Objects.equals(cached.init_ClassCache_UiHelpe(), this.init_ClassCache_UiHelpe)
         && Objects.equals(cached.init_ClassCache_RenderS(), queue)) {
         return cached;
      }

      ClassCache.Inner_FieldCache next = new ClassCache.Inner_FieldCache(
         this.init_FieldCache_BufferH.init_Str(),
         name,
         this.init_Str,
         publishedProtocol,
         current,
         this.read_Str,
         this.load_I,
         isConnected,
         publishedReady,
         this.tick_J,
         publishedScreen,
         customMenuOpen,
         this.flush_Str,
         publishedHotbar,
         this.close_Str,
         this.write_Z,
         pos.x,
         pos.y,
         pos.z,
         this.load_F,
         this.read_F,
         this.data_I,
         this.block_J,
         this.pop_Str,
         this.init_ClassCache_UiHelpe,
         queue
      );
      this.init_ClassCache_FieldCa = next;
      return next;
   }

   void init_V_InetSocketAddress(InetSocketAddress address, String handshakeHost, int handshakePort) {
      Connection expectedConnection;
      int expectedEpoch;
      synchronized (this.load_Obj) {
         if (this.load_AtomicBoolean.get()) {
            return;
         }

         expectedConnection = this.init_Connection;
         expectedEpoch = this.read_I;
         this.open_Str = init_Str_Str_I(handshakeHost, handshakePort);
      }

      this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.load, "Authenticating");

      try {
         CompletableFuture.<SyncManager.Inner_DataUtil>supplyAsync(
               () -> SyncManager.init_SyncManager_DataUt(this.init_FieldCache_BufferH.init_Str()), this.init_Executor
            )
            .whenComplete((resolved, error) -> {
               try {
                  synchronized (this.load_Obj) {
                     if (!this.load_Z_Connection_I(expectedConnection, expectedEpoch)) {
                        return;
                     }
                  }

                  if (error != null) {
                     this.init_V_Connection_2(expectedConnection, expectedEpoch, error);
                     this.init_V_Connection(expectedConnection, expectedEpoch, "Account authentication failed: " + init_Str_Throwable(error));
                     return;
                  }

                  String identityError = this.init_ClassCache_TokenSt.init_Str_ClassCache_UUID(this, resolved.init_User().getProfileId());
                  if (identityError != null && !identityError.isBlank()) {
                     this.init_V_Connection(expectedConnection, expectedEpoch, identityError);
                     return;
                  }

                  synchronized (this.load_Obj) {
                     if (!this.load_Z_Connection_I(expectedConnection, expectedEpoch)) {
                        return;
                     }

                     this.init_SyncManager_DataUt = resolved;
                  }

                  this.init_V_InetSocketAddress(address, handshakeHost, handshakePort, expectedConnection, expectedEpoch);
               } catch (Throwable failure) {
                  this.init_V_Connection_2(expectedConnection, expectedEpoch, failure);
                  this.init_V_Connection(expectedConnection, expectedEpoch, "Connection startup failed: " + init_Str_Throwable(failure));
               }
            });
      } catch (RuntimeException error) {
         this.init_V_Connection_2(expectedConnection, expectedEpoch, error);
         this.init_V_Connection(expectedConnection, expectedEpoch, "Account authentication could not start: " + init_Str_Throwable(error));
      }
   }

   private void init_V_InetSocketAddress(InetSocketAddress address, String handshakeHost, int handshakePort, @Nullable Connection expected, int epoch) {
      synchronized (this.load_Obj) {
         if (!this.load_Z_Connection_I(expected, epoch)) {
            return;
         }
      }

      if (!StreamGate.init_Z(this.init_StreamGate_CachePo)) {
         this.init_V_InetSocketAddress(address, handshakeHost, handshakePort, this.init_StreamGate_CachePo, expected, epoch);
      } else {
         synchronized (this.load_Obj) {
            if (!this.load_Z_Connection_I(expected, epoch)) {
               return;
            }

            this.load_StreamGate_CachePo = this.init_StreamGate_CachePo;
            this.open_Str = handshakeHost == null ? "" : handshakeHost + ":" + handshakePort;
            this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.read, "Detecting server protocol");
            this.init_TaskQueue.init_V_Str("PROTOCOL_DETECTION");
         }

         try {
            CompletableFuture.<StreamGate.Inner_CachePool>supplyAsync(() -> {
                  try {
                     return StreamGate.init(this.init_StreamGate_CachePo, address, handshakeHost, handshakePort, this.init_bW);
                  } catch (Exception failure) {
                     throw new CompletionException(failure);
                  }
               }, this.init_Executor)
               .orTimeout(10L, TimeUnit.SECONDS)
               .whenComplete(
                  (resolved, failure) -> {
                     synchronized (this.load_Obj) {
                        if (!this.load_Z_Connection_I(expected, epoch)) {
                           return;
                        }
                     }

                     if (failure != null) {
                        this.init_V_Connection_2(expected, epoch, failure);
                        this.init_V_Connection(
                           expected,
                           epoch,
                           "AUTO protocol detection failed: " + init_Str_Throwable(failure) + ". Select the server's protocol explicitly or retry."
                        );
                     } else {
                        try {
                           this.init_V_InetSocketAddress(address, handshakeHost, handshakePort, resolved, expected, epoch);
                        } catch (Throwable startupFailure) {
                           this.init_V_Connection_2(expected, epoch, startupFailure);
                           this.init_V_Connection(expected, epoch, "Connection startup failed: " + init_Str_Throwable(startupFailure));
                        }
                     }
                  }
               );
         } catch (RuntimeException failure) {
            this.init_V_Connection_2(expected, epoch, failure);
            this.init_V_Connection(expected, epoch, "AUTO protocol detection could not start: " + init_Str_Throwable(failure));
         }
      }
   }

   private void init_V_InetSocketAddress(
      InetSocketAddress address, String handshakeHost, int handshakePort, StreamGate.Inner_CachePool resolvedTarget, @Nullable Connection expected, int epoch
   ) {
      Connection created;
      int createdEpoch;
      boolean transferIntent;
      synchronized (this.update_Obj) {
         label198: {
            synchronized (this.load_Obj) {
               if (this.load_Z_Connection_I(expected, epoch)) {
                  this.load_StreamGate_CachePo = resolvedTarget;
                  this.count_Z = false;
                  this.cache_I = this.state_I = 0;
                  this.init_GameProfile = null;
                  this.clear_I = -1;
                  this.write_Str = handshakeHost == null ? "" : handshakeHost;
                  this.write_I = handshakePort;
                  this.entry();
                  this.open_Str = init_Str_Str_I(handshakeHost, handshakePort);
                  transferIntent = this.init_Z;
                  this.init_Z = false;
                  this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.read, transferIntent ? "Transferring" : "Connecting");
                  this.drain = System.currentTimeMillis();
                  this.init_CachePool.init(autismclient.util.f.StreamUtil.init_StreamUtil());
                  this.init_CachePool.init_V(this.drain);
                  created = new Connection(PacketFlow.CLIENTBOUND);
                  this.read_I++;
                  createdEpoch = this.read_I;
                  this.init_TaskQueue.init_V_I(createdEpoch);
                  this.load_Str = "Connection closed";
                  this.init_ClassCache_ConfigS = null;
                  this.init_Connection = created;
                  this.init_MapStore_CachePool = MapStore.Inner_CachePool.init;
                  break label198;
               }
            }

            return;
         }
      }

      try {
         synchronized (this.read_Obj) {
            synchronized (this.load_Obj) {
               if (!this.init_Z_Connection_I(created, createdEpoch)) {
                  return;
               }
            }

            this.init_MapStore.init_V();
            this.init_MapStore.init_V_Connection_SocketAddress(created, address);
            autismclient.g.BufferHelper.init(
               created, this.init_SyncManager_DataUt.init_User().getProfileId(), this.init_SyncManager_DataUt.init_User().getName()
            );
            autismclient.g.BufferHelper.init(created, this.write_Str, this.write_I);
            autismclient.g.BufferHelper.init(created, Type.OTHER);
            synchronized (this.load_Obj) {
               if (!this.init_Z_Connection_I(created, createdEpoch)) {
                  return;
               }
            }
         }
      } catch (RuntimeException error) {
         this.init_V_Connection_2(created, createdEpoch, error);
         this.init_V_Connection(created, createdEpoch, "Multi resource-pack setup failed: " + init_Str_Throwable(error));
         return;
      }

      ConfigStore.Inner_DataUtil connecting = ConfigStore.init_ConfigStore_DataUt(ConfigStore.Inner_DataUtil.init(this.init_bW));

      ChannelFuture future;
      label162: {
         try {
            ConfigStore.init(created, this.init_bW);
            StreamGate.init(created, resolvedTarget);
            future = Connection.connect(address, EventLoopGroupHolder.remote(Minecraft.getInstance().options.useNativeTransport()), created);
            break label162;
         } catch (Throwable error) {
            this.init_V_Connection_2(created, createdEpoch, error);
            String protocol = resolvedTarget != null && resolvedTarget.init_Z() ? "ViaFabricPlus " + resolvedTarget.init_Str() : "native protocol";
            this.init_V_Connection(created, createdEpoch, "Connection setup failed (" + protocol + "): " + init_Str_Throwable(error));
            ConfigStore.init_V(created);
         } finally {
            ConfigStore.init_V(connecting);
         }

         return;
      }

      future.addListener(
         done -> {
            boolean currentAttempt;
            synchronized (this.load_Obj) {
               currentAttempt = this.init_Z_Connection_I(created, createdEpoch);
            }

            if (!currentAttempt) {
               init_V_Connection_Component(created, Component.literal("Cancelled"));
               if (!done.isSuccess()) {
                  ConfigStore.init_V(created);
               }
            } else if (!done.isSuccess()) {
               this.init_V_Connection_2(created, createdEpoch, done.cause());
               this.init_V_Connection(created, createdEpoch, init_Str_Throwable(done.cause()));
               ConfigStore.init_V(created);
            } else {
               try {
                  synchronized (this.load_Obj) {
                     if (!this.init_Z_Connection_I(created, createdEpoch)) {
                        return;
                     }

                     this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.write, "Logging in");
                     created.initiateServerboundPlayConnection(
                        handshakeHost,
                        handshakePort,
                        LoginProtocols.SERVERBOUND,
                        LoginProtocols.CLIENTBOUND,
                        this.init_Obj_ClassCache_IoWorke(
                           ClassCache.Inner_IoWorker.init_ClassCache_IoWorke, ClientLoginPacketListener.class, created, createdEpoch, null
                        ),
                        transferIntent
                     );
                     created.send(
                        new ServerboundHelloPacket(this.init_SyncManager_DataUt.init_User().getName(), this.init_SyncManager_DataUt.init_User().getProfileId())
                     );
                     this.init_TaskQueue.load(ServerboundHelloPacket.class);
                  }
               } catch (Throwable error) {
                  this.init_V_Connection_2(created, createdEpoch, error);
                  this.init_V_Connection(created, createdEpoch, "Login handshake failed: " + init_Str_Throwable(error));
               }
            }
         }
      );
   }

   private <T> T init_Obj_ClassCache_IoWorke(ClassCache.Inner_IoWorker phase, Class<T> listenerType, ClassCache.Inner_ConfigStore attachment) {
      return this.init_Obj_ClassCache_IoWorke(phase, listenerType, attachment.init_Connection, attachment.init_I, attachment);
   }

   private <T> T init_Obj_ClassCache_IoWorke(
      ClassCache.Inner_IoWorker phase, Class<T> listenerType, Connection listenerConnection, int epoch, ClassCache.@Nullable Inner_ConfigStore attachment
   ) {
      InvocationHandler handler = (proxyObject, method, args) -> this.init(phase, listenerConnection, epoch, attachment, proxyObject, method, args);
      T listener = (T)Proxy.newProxyInstance(listenerType.getClassLoader(), new Class[]{listenerType, NetHelper.class}, handler);
      autismclient.g.BufferHelper.init(listenerConnection, (PacketListener)listener);
      return listener;
   }

   private Object init(
      ClassCache.Inner_IoWorker phase,
      Connection listenerConnection,
      int epoch,
      ClassCache.@Nullable Inner_ConfigStore attachment,
      Object proxyObject,
      Method method,
      Object[] args
   ) {
      if (method.getDeclaringClass() == NetHelper.class) {
         if (args != null && args.length > 0 && args[0] instanceof Throwable error) {
            synchronized (this.load_Obj) {
               if (this.init_Z_Connection_I(listenerConnection, epoch)) {
                  this.init_TaskQueue.init_V_Throwable(error);
                  this.load_Str = error instanceof ReadTimeoutException ? "Network read timeout" : "Network failure";
               }
            }
         }

         return null;
      } else {
         Object var10000;
         switch (method.getName()) {
            case "protocol":
               ConnectionProtocol var34 = phase.init_ConnectionProtocol;
               ConnectionProtocol var34x = var34;
               var10000 = var34x;
               break;
            case "flow":
               PacketFlow var33 = PacketFlow.CLIENTBOUND;
               PacketFlow var33x = var33;
               var10000 = var33x;
               break;
            case "isAcceptingMessages":
            case "shouldHandleMessage": {
               Boolean var32;
               synchronized (this.load_Obj) {
                  var32 = this.init_Z_ClassCache_IoWorke(phase, listenerConnection, epoch, attachment) && listenerConnection.isConnected();
               }

               var10000 = var32;
               break;
            }
            case "onDisconnect":
               DisconnectionDetails details = args != null && args.length > 0 && args[0] instanceof DisconnectionDetails value ? value : null;
               this.init_V_DisconnectionDetai(details, listenerConnection, epoch, attachment);
               Object var31 = null;
               Object var31x = var31;
               var10000 = var31x;
               break;
            case "createDisconnectionInfo":
               Throwable error = args != null && args.length > 1 && args[1] instanceof Throwable cause ? cause : null;
               synchronized (this.load_Obj) {
                  if (this.init_Z_Connection_I(listenerConnection, epoch) && error != null) {
                     this.init_TaskQueue.init_V_Throwable(error);
                     this.load_Str = error instanceof ReadTimeoutException ? "Network read timeout" : "Network failure";
                  }
               }

               Component reason = error != null
                  ? Component.literal("Network error: " + init_Str_Throwable(error))
                  : (args != null && args.length > 0 && args[0] instanceof Component value ? value : Component.literal("Connection error"));
               DisconnectionDetails var30 = new DisconnectionDetails(reason);
               DisconnectionDetails var30x = var30;
               var10000 = var30x;
               break;
            case "onPacketError": {
               Throwable error2 = args != null && args.length > 1 && args[1] instanceof Throwable value ? value : null;
               synchronized (this.load_Obj) {
                  if (this.init_Z_ClassCache_IoWorke(phase, listenerConnection, epoch, attachment)) {
                     this.init_TaskQueue.init_V_Throwable(error2);
                     this.load_Str = "Packet processing failed";
                  }
               }

               this.init_V_MapStore_JsonUtil_Str(
                  this.init_MapStore_JsonUtil_ClassCache_IoWorke(phase, listenerConnection, epoch, attachment), "Packet error: " + init_Str_Throwable(error2)
               );
               Object var29 = null;
               Object var29x = var29;
               var10000 = var29x;
               break;
            }
            case "toString":
               String var28 = "Multi" + phase + "Listener[" + this.init_FieldCache_BufferH.init_Str() + "]";
               String var28x = var28;
               var10000 = var28x;
               break;
            case "hashCode":
               Integer var27 = System.identityHashCode(proxyObject);
               Integer var27x = var27;
               var10000 = var27x;
               break;
            case "equals":
               Boolean var26 = args != null && args.length == 1 && proxyObject == args[0];
               Boolean var26x = var26;
               var10000 = var26x;
               break;
            default:
               Object var25;
               label265: {
                  if (args != null && args.length > 0 && args[0] instanceof Packet<?> packet) {
                     MapStore.Inner_JsonUtil target = this.init_MapStore_JsonUtil_ClassCache_IoWorke(phase, listenerConnection, epoch, attachment);
                     synchronized (this.load_Obj) {
                        if (!this.init_Z_ClassCache_IoWorke(phase, listenerConnection, epoch, attachment)) {
                           var25 = init_Obj_Class(method.getReturnType());
                           break label265;
                        }
                     }

                     this.init_V_ClassCache_IoWorke(phase, attachment, target, packet);
                  }

                  return init_Obj_Class(method.getReturnType());
               }

               var10000 = var25;
         }

         return var10000;
      }
   }

   private void init_V_ClassCache_IoWorke(
      ClassCache.Inner_IoWorker phase, ClassCache.@Nullable Inner_ConfigStore attachment, MapStore.Inner_JsonUtil target, Packet<?> packet
   ) {
      this.init_V_ClassCache_IoWorke(phase, attachment, target, packet, 0);
   }

   private void init_V_ClassCache_IoWorke(
      ClassCache.Inner_IoWorker phase, ClassCache.@Nullable Inner_ConfigStore attachment, MapStore.Inner_JsonUtil target, Packet<?> packet, int bundleDepth
   ) {
      if (packet != null && target != null) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_ClassCache_IoWorke(phase, target.init_Connection(), target.init_I(), attachment)) {
               return;
            }
         }

         if (phase == ClassCache.Inner_IoWorker.read && packet instanceof ClientboundBundlePacket bundle) {
            if (bundleDepth >= 8) {
               this.init_V_MapStore_JsonUtil_Str(target, "Protocol error: nested packet bundle limit exceeded");
            } else {
               int count = 0;

               for (Packet<? super ClientGamePacketListener> subPacket : bundle.subPackets()) {
                  if (++count > 4096) {
                     this.init_V_MapStore_JsonUtil_Str(target, "Protocol error: packet bundle size limit exceeded");
                     return;
                  }

                  this.init_V_ClassCache_IoWorke(phase, attachment, target, subPacket, bundleDepth + 1);
                  if (this.load_AtomicBoolean.get()) {
                     return;
                  }
               }
            }
         } else {
            this.init_TaskQueue.init_V_Class(packet.getClass());
            if (phase == ClassCache.Inner_IoWorker.read) {
               this.tick_J = System.currentTimeMillis();
            }

            CustomMenuSnapshot beforeMenu = this.init_StreamUtil.init_CustomMenuSnapshot();
            boolean customMenuPacket = this.init_StreamUtil.init(packet, phase.name());
            if (customMenuPacket) {
               this.init_V_CustomMenuSnapshot_CustomMenuSnapshot(beforeMenu, this.init_StreamUtil.init_CustomMenuSnapshot());
               this.init_V_CustomMenuSnapshot(this.init_StreamUtil.init_CustomMenuSnapshot());
            }

            boolean critical = customMenuPacket || load_Z_Packet(packet);
            boolean allowed = this.init_IoWorker.init_Z_IoWorker_DataUtil(IoWorker.Inner_DataUtil.load, packet.getClass().getName(), critical, false);
            if (allowed || packet instanceof ClientboundPlayerChatPacket) {
               try {
                  this.load_V_MapStore_JsonUtil_Packet(target, packet);
                  switch (phase) {
                     case init_ClassCache_IoWorke:
                        this.init_V_MapStore_JsonUtil_Packet(target, packet);
                        break;
                     case load:
                        this.init_V_ClassCache_ConfigS_2(attachment, target, packet);
                        break;
                     case read:
                        this.init_V_ClassCache_ConfigS_4(attachment, packet, allowed);
                  }
               } catch (Throwable var14) {
                  Throwable error = var14;
                  synchronized (this.load_Obj) {
                     if (this.init_Z_MapStore_JsonUtil(target)) {
                        this.init_TaskQueue.init_V_Throwable(error);
                        this.load_Str = "Protocol processing failed";
                     }
                  }

                  this.init_V_MapStore_JsonUtil_Str(target, "Protocol error: " + init_Str_Throwable(var14));
               }
            }
         }
      }
   }

   private void init_V_MapStore_JsonUtil_Packet(MapStore.Inner_JsonUtil target, Packet<?> packet) throws CryptException {
      if (packet instanceof ClientboundHelloPacket hello) {
         this.init_V_MapStore_JsonUtil_ClientboundHelloPa(target, hello);
      } else if (packet instanceof ClientboundLoginFinishedPacket finished) {
         ClassCache.Inner_ConfigStore attachment;
         synchronized (this.load_Obj) {
            if (!this.init_Z_MapStore_JsonUtil(target)) {
               return;
            }

            this.read_Z = false;
            attachment = new ClassCache.Inner_ConfigStore(target.init_Connection(), target.init_I(), ++this.push_J);
            this.init_ClassCache_ConfigS = attachment;
         }

         if (!this.init_Z_ClassCache_ConfigS_ClientInformation(attachment, Minecraft.getInstance().options.buildPlayerInformation())) {
            return;
         }

         synchronized (this.load_Obj) {
            if (!this.init_Z_ClassCache_ConfigS(attachment) || this.init_MapStore_CachePool != MapStore.Inner_CachePool.load) {
               return;
            }

            this.load_Map.put(finished.gameProfile().id(), finished.gameProfile().name());
            this.init_GameProfile = finished.gameProfile();
            this.init_UUID = finished.gameProfile().id();
         }
      } else if (packet instanceof ClientboundLoginDisconnectPacket disconnected) {
         synchronized (this.load_Obj) {
            if (this.init_Z_MapStore_JsonUtil(target)) {
               this.load_Str = "Server disconnect";
               target.init_Connection().disconnect(disconnected.reason());
            }
         }
      } else if (packet instanceof ClientboundLoginCompressionPacket compression) {
         synchronized (this.load_Obj) {
            if (this.init_Z_MapStore_JsonUtil(target) && !target.init_Connection().isMemoryConnection()) {
               target.init_Connection().setupCompression(compression.getCompressionThreshold(), false);
            }
         }
      } else if (packet instanceof ClientboundCustomQueryPacket query) {
         ServerboundCustomQueryAnswerPacket answer = new ServerboundCustomQueryAnswerPacket(query.transactionId(), null);
         this.init_Z_MapStore_JsonUtil_ServerboundCustomQ(target, answer);
      }
   }

   private boolean init_Z_ClassCache_ConfigS_ClientInformation(ClassCache.Inner_ConfigStore attachment, @Nullable ClientInformation information) {
      MapStore.Inner_JsonUtil loginTarget = this.init_MapStore_JsonUtil_ClassCache_IoWorke(
         ClassCache.Inner_IoWorker.init_ClassCache_IoWorke, attachment.init_Connection, attachment.init_I, null
      );
      MapStore.Inner_JsonUtil configurationTarget = this.init_MapStore_JsonUtil_ClassCache_IoWorke(
         ClassCache.Inner_IoWorker.load, attachment.init_Connection, attachment.init_I, attachment
      );

      try {
         synchronized (this.read_Obj) {
            label128: {
               synchronized (this.load_Obj) {
                  if (!this.init_Z_ClassCache_ConfigS(attachment) || this.init_MapStore_CachePool != MapStore.Inner_CachePool.init) {
                     return false;
                  }
               }

               this.init_MapStore.init_V();
               this.init_MapStore.init_V_Connection(attachment.init_Connection);
               autismclient.g.BufferHelper.init(
                  attachment.init_Connection, this.init_SyncManager_DataUt.init_User().getProfileId(), this.init_SyncManager_DataUt.init_User().getName()
               );
               autismclient.g.BufferHelper.init(attachment.init_Connection, this.write_Str, this.write_I);
               autismclient.g.BufferHelper.init(attachment.init_Connection, Type.OTHER);
               boolean var10000;
               synchronized (this.load_Obj) {
                  if (this.init_Z_ClassCache_ConfigS(attachment) && this.init_MapStore_CachePool == MapStore.Inner_CachePool.init) {
                     this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.update, "Configuring");
                     attachment.init_Connection.setupInboundProtocol(ConfigurationProtocols.CLIENTBOUND, attachment.init_ClientConfiguratio);
                     if (!this.init_Z_MapStore_JsonUtil(loginTarget, attachment, ServerboundLoginAcknowledgedPacket.INSTANCE)) {
                        throw new IllegalStateException("Login acknowledgement was refused");
                     }

                     this.init_MapStore_CachePool = MapStore.Inner_CachePool.load;
                     attachment.init_Connection.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND);
                     this.init_ClientInformation = information;
                     if (!this.init_Z_MapStore_JsonUtil_2(configurationTarget, new ServerboundCustomPayloadPacket(new BrandPayload("vanilla")), true, false)) {
                        throw new IllegalStateException("Brand response was refused");
                     }

                     if (information != null
                        && !this.init_Z_MapStore_JsonUtil_2(configurationTarget, new ServerboundClientInformationPacket(information), true, false)) {
                        throw new IllegalStateException("Client information was refused");
                     }
                     break label128;
                  }

                  var10000 = false;
               }

               return var10000;
            }
         }

         synchronized (this.load_Obj) {
            return this.init_Z_MapStore_JsonUtil(configurationTarget);
         }
      } catch (Throwable var17) {
         Throwable error = var17;
         synchronized (this.load_Obj) {
            if (this.init_Z_ClassCache_ConfigS(attachment)) {
               this.init_TaskQueue.init_V_Throwable(error);
            }
         }

         this.init_V_ClassCache_ConfigS_Str(attachment, "Multi resource-pack setup failed: " + init_Str_Throwable(var17));
         return false;
      }
   }

   private void init_V_MapStore_JsonUtil_ClientboundHelloPa(MapStore.Inner_JsonUtil target, ClientboundHelloPacket hello) throws CryptException {
      SyncManager.Inner_DataUtil authenticatingIdentity;
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return;
         }

         authenticatingIdentity = this.init_SyncManager_DataUt;
      }

      SecretKey secretKey = Crypt.generateSecretKey();
      PublicKey publicKey = hello.getPublicKey();
      String digest = new BigInteger(Crypt.digestData(hello.getServerId(), publicKey, secretKey)).toString(16);
      Cipher decryptCipher = Crypt.getCipher(2, secretKey);
      Cipher encryptCipher = Crypt.getCipher(1, secretKey);
      ServerboundKeyPacket keyPacket = new ServerboundKeyPacket(secretKey, publicKey, hello.getChallenge());
      Runnable enableEncryption = () -> init_V_Connection_Runnable(target.init_Connection(), () -> {
         try {
            synchronized (this.load_Obj) {
               if (!this.init_Z_MapStore_JsonUtil(target) || this.init_SyncManager_DataUt != authenticatingIdentity) {
                  return;
               }

               target.init_Connection().send(keyPacket, future -> {
                  try {
                     if (!future.isSuccess()) {
                        this.init_V_Connection_2(target.init_Connection(), target.init_I(), future.cause());
                        this.init_V_MapStore_JsonUtil_Str(target, "Encryption response failed: " + init_Str_Throwable(future.cause()));
                        return;
                     }

                     synchronized (this.load_Obj) {
                        if (!this.init_Z_MapStore_JsonUtil(target) || this.init_SyncManager_DataUt != authenticatingIdentity) {
                           return;
                        }

                        target.init_Connection().setEncryptionKey(decryptCipher, encryptCipher);
                     }
                  } catch (Throwable error) {
                     this.init_V_Connection_2(target.init_Connection(), target.init_I(), error);
                     this.init_V_MapStore_JsonUtil_Str(target, "Encryption setup failed: " + init_Str_Throwable(error));
                  }
               });
               this.init_TaskQueue.load(ServerboundKeyPacket.class);
            }
         } catch (Throwable error) {
            this.init_V_Connection_2(target.init_Connection(), target.init_I(), error);
            this.init_V_MapStore_JsonUtil_Str(target, "Encryption setup failed: " + init_Str_Throwable(error));
         }
      });
      if (!hello.shouldAuthenticate()) {
         enableEncryption.run();
      } else {
         AtomicBoolean joinCompleted = new AtomicBoolean();
         CompletableFuture.runAsync(
               () -> {
                  try {
                     synchronized (this.load_Obj) {
                        if (!this.init_Z_MapStore_JsonUtil(target) || this.init_SyncManager_DataUt != authenticatingIdentity) {
                           return;
                        }
                     }

                     authenticatingIdentity.init_MinecraftSessionSe()
                        .joinServer(authenticatingIdentity.init_User().getProfileId(), authenticatingIdentity.init_User().getAccessToken(), digest);
                     joinCompleted.set(true);
                     enableEncryption.run();
                  } catch (AuthenticationException error) {
                     joinCompleted.set(true);
                     this.init_V_Connection_2(target.init_Connection(), target.init_I(), error);
                     this.init_V_MapStore_JsonUtil(target, authenticatingIdentity, error);
                  } catch (Throwable error) {
                     joinCompleted.set(true);
                     this.init_V_Connection_2(target.init_Connection(), target.init_I(), error);
                     this.init_V_MapStore_JsonUtil_Str(target, "Session join failed: " + init_Str_Throwable(error));
                  }
               },
               this.init_Executor
            )
            .orTimeout(15L, TimeUnit.SECONDS)
            .whenComplete((ignored, timeout) -> {
               if (timeout != null && !joinCompleted.get() && !this.load_AtomicBoolean.get()) {
                  this.init_V_Connection_2(target.init_Connection(), target.init_I(), timeout);
                  this.init_V_MapStore_JsonUtil_Str(target, "Session authentication timed out after 15s (request or worker queue did not finish)");
               }
            });
      }
   }

   private void init_V_MapStore_JsonUtil(MapStore.Inner_JsonUtil target, SyncManager.Inner_DataUtil authenticatingIdentity, AuthenticationException error) {
      boolean refreshable;
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target) || this.init_SyncManager_DataUt != authenticatingIdentity) {
            return;
         }

         refreshable = !this.read_Z
            && !this.write_Str.isBlank()
            && authenticatingIdentity != null
            && authenticatingIdentity.init_MethodUtil() == autismclient.util.MethodUtil.read;
         if (refreshable) {
            this.read_Z = true;
         }
      }

      if (!refreshable) {
         this.init_V_MapStore_JsonUtil_Str(target, "Session authentication failed: " + init_Str_Throwable(error));
      } else {
         autismclient.util.TokenStore.init_TokenStore().init_V(this.init_FieldCache_BufferH.init_Str());
         this.init_V_MapStore_JsonUtil(target, this.write_Str, this.write_I, false, 1000L, true);
      }
   }

   private void init_V_ClassCache_ConfigS_2(ClassCache.@Nullable Inner_ConfigStore attachment, MapStore.Inner_JsonUtil target, Packet<?> packet) {
      if (attachment != null) {
         ClassCache.Inner_TaskQueue lifecycle = attachment.init_ClassCache_TaskQue;
         CompletableFuture<ClassCache.Inner_SyncManager> selection = null;
         List<KnownPack> requested = null;
         boolean selectKnown = false;
         boolean finish = false;
         synchronized (this.load_Obj) {
            if (!this.init_Z_MapStore_JsonUtil(target) || this.init_ClassCache_ConfigS != attachment) {
               return;
            }

            if (packet instanceof ClientboundRegistryDataPacket registry) {
               lifecycle.init_RegistryDataCollec.appendContents(registry.registry(), registry.entries());
            } else if (packet instanceof ClientboundUpdateTagsPacket tags) {
               lifecycle.init_RegistryDataCollec.appendTags(tags.tags());
            } else if (packet instanceof ClientboundUpdateEnabledFeaturesPacket features) {
               this.init_FeatureFlagSet = FeatureFlags.REGISTRY.fromNames(features.features());
            } else if (packet instanceof ClientboundSelectKnownPacks knownPacks) {
               selection = new CompletableFuture<>();
               lifecycle.init_CompletableFuture = selection;
               requested = List.copyOf(knownPacks.knownPacks());
               selectKnown = true;
            } else if (packet instanceof ClientboundFinishConfigurationPacket) {
               if (lifecycle.init_Z || lifecycle.load) {
                  return;
               }

               selection = lifecycle.init_CompletableFuture;
               lifecycle.init_Z = true;
               finish = true;
            }
         }

         if (selectKnown) {
            this.init_V_ClassCache_ConfigS(attachment, target, selection, requested);
         } else if (finish) {
            this.init_V_ClassCache_ConfigS(attachment, target, selection);
         } else if (packet instanceof ClientboundResetChatPacket) {
            this.entry();
         } else if (packet instanceof ClientboundCodeOfConductPacket) {
            this.init_Z_MapStore_JsonUtil_2(target, ServerboundAcceptCodeOfConductPacket.INSTANCE, true, false);
         }
      }
   }

   private void init_V_ClassCache_ConfigS(
      ClassCache.Inner_ConfigStore attachment,
      MapStore.Inner_JsonUtil target,
      CompletableFuture<ClassCache.Inner_SyncManager> completion,
      List<KnownPack> requested
   ) {
      CompletableFuture.runAsync(
         () -> {
            try {
               ClassCache.Inner_SyncManager selected = attachment.init_ClassCache_TaskQue.init().init(requested);
               completion.complete(selected);
               synchronized (this.load_Obj) {
                  if (this.init_Z_MapStore_JsonUtil(target)
                     && this.init_ClassCache_ConfigS == attachment
                     && attachment.init_ClassCache_TaskQue.init_CompletableFuture == completion) {
                     this.init_Z_MapStore_JsonUtil_2(target, new ServerboundSelectKnownPacks(selected.init()), true, false);
                  }
               }
            } catch (Throwable var11) {
               Throwable error = var11;
               completion.completeExceptionally(var11);
               synchronized (this.load_Obj) {
                  if (this.init_Z_ClassCache_ConfigS(attachment) && attachment.init_ClassCache_TaskQue.init_CompletableFuture == completion) {
                     this.init_TaskQueue.init_V_Throwable(error);
                  }
               }

               this.init_V_ClassCache_ConfigS_3(attachment, completion, "Known-pack selection failed: " + init_Str_Throwable(var11));
            }
         },
         this.init_Executor
      );
   }

   private void init_V_ClassCache_ConfigS(
      ClassCache.Inner_ConfigStore attachment, MapStore.Inner_JsonUtil target, CompletableFuture<ClassCache.Inner_SyncManager> selection
   ) {
      CompletableFuture.runAsync(
         () -> {
            try {
               ClassCache.Inner_SyncManager selected = selection.join();
               synchronized (this.load_Obj) {
                  if (!this.init_Z_MapStore_JsonUtil(target)
                     || this.init_ClassCache_ConfigS != attachment
                     || attachment.init_ClassCache_TaskQue.init_CompletableFuture != selection
                     || !attachment.init_ClassCache_TaskQue.init_Z
                     || attachment.init_ClassCache_TaskQue.load) {
                     return;
                  }
               }

               Frozen base = ClientRegistryLayer.createRegistryAccess().compositeAccess();
               autismclient.g.TaskTimer.init_V(autismclient.g.BufferHelper.init_BufferHelper_Strea(attachment.init_Connection));

               Frozen collected;
               try {
                  if (selected == null) {
                     collected = attachment.init_ClassCache_TaskQue.init_RegistryDataCollec.collectGameRegistries(ResourceProvider.EMPTY, base, false);
                  } else {
                     try (CloseableResourceManager resources = attachment.init_ClassCache_TaskQue.init().init(selected)) {
                        collected = attachment.init_ClassCache_TaskQue.init_RegistryDataCollec.collectGameRegistries(resources, base, false);
                     }
                  }
               } finally {
                  autismclient.g.TaskTimer.init_V();
               }

               init_V_Connection_Runnable(
                  attachment.init_Connection,
                  () -> {
                     try {
                        synchronized (this.load_Obj) {
                           if (!this.init_Z_MapStore_JsonUtil(target)
                              || this.init_ClassCache_ConfigS != attachment
                              || attachment.init_ClassCache_TaskQue.init_CompletableFuture != selection
                              || !attachment.init_ClassCache_TaskQue.init_Z
                              || attachment.init_ClassCache_TaskQue.load) {
                              return;
                           }

                           this.init_RegistryAccess_Fro = collected;
                           attachment.init_Connection
                              .setupInboundProtocol(
                                 GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(collected)),
                                 this.init_Obj_ClassCache_IoWorke(ClassCache.Inner_IoWorker.read, ClientGamePacketListener.class, attachment)
                              );
                           attachment.init_ClassCache_TaskQue.load = true;
                           if (!this.init_Z_MapStore_JsonUtil_2(target, ServerboundFinishConfigurationPacket.INSTANCE, true, false)) {
                              throw new IllegalStateException("Finish acknowledgement was refused");
                           }

                           this.init_MapStore_CachePool = MapStore.Inner_CachePool.read;
                           attachment.init_Connection
                              .setupOutboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(collected), new Context() {
                                 @Override
                                 public boolean hasInfiniteMaterials() {
                                    return true;
                                 }

                                 @Override
                                 public boolean canUseCommandBlocks() {
                                    return true;
                                 }
                              }));
                           this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.sync, "Joining");
                        }
                     } catch (Throwable var10) {
                        Throwable errorx = var10;
                        synchronized (this.load_Obj) {
                           if (this.init_Z_ClassCache_ConfigS(attachment)) {
                              this.init_TaskQueue.init_V_Throwable(errorx);
                           }
                        }

                        this.init_V_ClassCache_ConfigS_3(attachment, selection, "Registry protocol transition failed: " + init_Str_Throwable(var10));
                     }
                  }
               );
            } catch (Throwable var23) {
               Throwable error = var23;
               synchronized (this.load_Obj) {
                  if (this.init_Z_ClassCache_ConfigS(attachment)) {
                     this.init_TaskQueue.init_V_Throwable(error);
                  }
               }

               this.init_V_ClassCache_ConfigS_3(attachment, selection, "Registry setup failed: " + init_Str_Throwable(var23));
            }
         },
         this.init_Executor
      );
   }

   private static void init_V_Connection_Runnable(Connection connection, Runnable action) {
      Channel channel = ((AutismClientConnectionAccessor)connection).getChannel();
      if (channel != null && !channel.eventLoop().inEventLoop()) {
         channel.eventLoop().execute(action);
      } else {
         action.run();
      }
   }

   private void init_V_ClassCache_ConfigS_4(ClassCache.@Nullable Inner_ConfigStore attachment, Packet<?> packet, boolean allowedForDisplay) {
      if (this.stop_Z) {
         this.init_V_Z_Packet(false, packet);
      }

      if (this.size_Z) {
         this.init_CacheEntry.init_V_Packet(packet);
      }

      if (packet instanceof ClientboundLoginPacket login) {
         this.count_Z = true;
         this.cache_I = this.state_I = 0;
         this.add_Z = login.enforcesSecureChat();
         this.size_I = login.playerId();
         this.clear_I = -1;
         this.close_Str = init_Str_CommonPlayerSpawnI(login.commonPlayerSpawnInfo());
         this.push_I = init_I_CommonPlayerSpawnI(login.commonPlayerSpawnInfo());
         this.init_StateCache.init_V();
         this.init_V_Z_Z(true, true);
         long nowJoin = System.currentTimeMillis();
         this.load_V_J(nowJoin);
         this.init_TaskQueue_2.init_V_J(nowJoin);
         this.parse = 0L;
         this.open_Z = true;
         this.poll_Z = false;
         this.write_D = 0.0;
         this.push_Z = false;
         this.update_I = -1;
         this.pop_Z = false;
         this.poll_Map.clear();
         this.init_TableIndex.init_V(this.init_PositionMoveRotati.position(), nowJoin);
         if (this.add_Z) {
            this.put_V();
         }
      } else if (packet instanceof ClientboundGameEventPacket gameEvent) {
         if (gameEvent.getEvent() == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
            this.push_I = (int)gameEvent.getParam();
         } else if (gameEvent.getEvent() == ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START) {
            this.sync_Z = true;
            this.read_V_J(System.currentTimeMillis());
         }
      } else if (packet instanceof ClientboundChunkBatchStartPacket) {
         this.init_ChunkBatchSizeCalc.onBatchStart();
      } else if (packet instanceof ClientboundChunkBatchFinishedPacket batchFinished) {
         this.init_ChunkBatchSizeCalc.onBatchFinished(batchFinished.batchSize());
         this.init_Z_Packet(new ServerboundChunkBatchReceivedPacket(this.init_ChunkBatchSizeCalc.getDesiredChunksPerTick()), true, false);
         this.flush_Z = true;
         this.read_V_J(System.currentTimeMillis());
      } else if (packet instanceof ClientboundOpenSignEditorPacket sign) {
         this.init_BlockPos = sign.pos();
         this.prev_Z = Compat263.openSignIsFrontText(sign);
         this.next_Z = true;
         if (this.level_Z) {
            this.f_f_J++;
         } else {
            this.queue_J++;
         }
      } else if (packet instanceof ClientboundOpenBookPacket book) {
         if (this.level_Z) {
            this.init_InteractionHand = book.hand();
            this.f_g_J++;
         }
      } else if (packet instanceof ClientboundSoundPacket sound) {
         if (this.valid_Z) {
            try {
               this.init_V_Str(sound.getSound().value().location().toString(), sound.getX(), sound.getY(), sound.getZ());
            } catch (RuntimeException var80) {
            }
         }
      } else if (packet instanceof ClientboundCooldownPacket cd) {
         String group = cd.cooldownGroup().toString();
         if (cd.duration() > 0) {
            if (this.open_Map.size() < 256 || this.open_Map.containsKey(group)) {
               this.open_Map.put(group, System.currentTimeMillis() + cd.duration() * 50L);
            }
         } else {
            this.open_Map.remove(group);
         }
      } else if (packet instanceof ClientboundBlockUpdatePacket bu) {
         this.init_V_BlockPos_BlockState(bu.getPos(), bu.getBlockState());
      } else if (packet instanceof ClientboundSectionBlocksUpdatePacket sbu) {
         sbu.runUpdates(this::init_V_BlockPos_BlockState);
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         this.load_V_J(System.currentTimeMillis());
         this.close_Str = init_Str_CommonPlayerSpawnI(respawn.commonPlayerSpawnInfo());
         this.push_I = init_I_CommonPlayerSpawnI(respawn.commonPlayerSpawnInfo());
         this.write_Z = false;
         this.init_V_Z_Z(true, true);
         this.init_StateCache.init_V();
         this.poll_Map.clear();
         this.put_Z = false;
         this.next_Z = false;
         this.f_h_J = 0L;
         this.f_i_J = 0L;
         this.f_j_J = 0L;
         synchronized (this.sync_Obj) {
            this.block_Z = false;
            this.level_I = 0;
            this.write_D = 0.0;
            this.open_Z = true;
            this.poll_Z = false;
            this.clear_J = 0L;
         }

         this.init_ClassCache_RingBuf = null;
         this.init_ClassCache_ArrayUt = null;
         this.queue_Z = false;
         this.buffer_Z = false;
      } else if (packet instanceof ClientboundSetHealthPacket healthPacket) {
         this.load_F = healthPacket.getHealth();
         this.data_I = healthPacket.getFood();
      } else if (packet instanceof ClientboundUpdateMobEffectPacket effectUp) {
         if (effectUp.getEntityId() == this.size_I) {
            long until = effectUp.getEffectDurationTicks() < 0 ? 9223372036854775807L : System.currentTimeMillis() + effectUp.getEffectDurationTicks() * 50L;
            MobEffect effect = effectUp.getEffect().value();
            if (effect == MobEffects.HASTE.value()) {
               this.index_I = effectUp.getEffectAmplifier();
               this.f_h_J = until;
            } else if (effect == MobEffects.CONDUIT_POWER.value()) {
               this.count_I = effectUp.getEffectAmplifier();
               this.f_i_J = until;
            } else if (effect == MobEffects.MINING_FATIGUE.value()) {
               this.flags_I = effectUp.getEffectAmplifier();
               this.f_j_J = until;
            }
         }
      } else if (packet instanceof ClientboundRemoveMobEffectPacket effectRm) {
         if (effectRm.entityId() == this.size_I) {
            MobEffect effect = effectRm.effect().value();
            if (effect == MobEffects.HASTE.value()) {
               this.f_h_J = 0L;
            } else if (effect == MobEffects.CONDUIT_POWER.value()) {
               this.f_i_J = 0L;
            } else if (effect == MobEffects.MINING_FATIGUE.value()) {
               this.f_j_J = 0L;
            }
         }
      } else if (packet instanceof ClientboundUpdateAttributesPacket attributes) {
         this.init_V_ClientboundUpdateA(attributes);
      } else if (packet instanceof ClientboundPlayerPositionPacket move) {
         long correctionAt = System.currentTimeMillis();
         Vec3 beforeCorrection = this.init_PositionMoveRotati.position();
         boolean landed;
         PositionMoveRotation correctedPosition;
         boolean walking;
         synchronized (this.sync_Obj) {
            walking = correctionAt < this.clear_J;
            boolean wasFalling = this.block_Z;
            this.init_PositionMoveRotati = PositionMoveRotation.calculateAbsolute(this.init_PositionMoveRotati, move.change(), move.relatives());
            correctedPosition = this.init_PositionMoveRotati;
            this.f_e_J++;
            this.valid_V();
            this.init_TableIndex.init_V(correctedPosition.position(), correctionAt);
            this.write_D = 0.0;
            landed = wasFalling && this.init_PositionMoveRotati.position().distanceToSqr(beforeCorrection) < 2.25;
            if (walking) {
               this.open_Z = true;
               this.poll_Z = false;
               this.block_Z = false;
            } else if (landed) {
               this.open_Z = true;
               this.poll_Z = false;
               this.block_Z = false;
            } else if (this.update_Z && !this.init_IoWorker.init_Z()) {
               this.open_Z = true;
               this.poll_Z = false;
               this.block_Z = false;
            } else {
               this.open_Z = false;
               this.poll_Z = true;
               this.f_l_J = correctionAt + 15000L;
               this.block_Z = true;
            }
         }

         this.write_Z = true;
         PkgCls_a.init_V_b_Vec3(this, correctedPosition.position());
         this.entry_J++;
         if (walking) {
            this.run_Z = true;
            this.start_J = correctionAt + 500L;
            this.close_V_J(correctionAt);
         } else if (!landed && (!this.update_Z || this.init_IoWorker.init_Z())) {
            this.add_J = Math.max(this.add_J, correctionAt + 5000L);
         }

         this.init_Z_Packet(Compat263.newAcceptTeleportationPacket(move.id()), true, false);
         this.init_V_PositionMoveRotati(correctedPosition);
         this.close_Z = true;
         if (!this.init_AtomicBoolean.get() && this.get_J == 9223372036854775807L) {
            this.get_J = correctionAt + 2000L;
         }

         this.read_V_J(correctionAt);
         this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.flush, "Ready");
         this.buffer_I = 0;
         if (!this.update_Z) {
            this.update_Z = true;
            this.add_J = System.currentTimeMillis() + 5000L;
            this.init_DataPipe.init_V(System.nanoTime());
         }
      } else if (packet instanceof ClientboundPlayerRotationPacket rotate) {
         float yaw;
         float pitch;
         synchronized (this.sync_Obj) {
            yaw = rotate.relativeY() ? this.init_PositionMoveRotati.yRot() + rotate.yRot() : rotate.yRot();
            pitch = rotate.relativeX() ? this.init_PositionMoveRotati.xRot() + rotate.xRot() : rotate.xRot();
            this.init_PositionMoveRotati = this.init_PositionMoveRotati.withRotation(yaw, pitch);
         }

         this.init_Z_Packet(new Rot(yaw, pitch, false, false), true, true);
      } else if (packet instanceof ClientboundSystemChatPacket system) {
         this.load_V_Component(system.content());
         this.init_V_Component(system.content());
         long now = System.currentTimeMillis();
         this.init_V_Component_J(system.content(), now);
         if (this.init_Z_J(now)) {
            this.init_TaskQueue_2.init(system.content(), now);
         }

         if (allowedForDisplay) {
            this.init_ClassCache_TokenSt.init_V_ClassCache_Component(this, system.content());
         }
      } else if (packet instanceof ClientboundDisguisedChatPacket disguised) {
         this.load_V_Component(disguised.message());
         this.init_V_Component(disguised.message());
         long now = System.currentTimeMillis();
         this.init_V_Component_J(disguised.message(), now);
         if (this.init_Z_J(now)) {
            this.init_TaskQueue_2.init(disguised.message(), now);
         }

         if (allowedForDisplay) {
            this.init_ClassCache_TokenSt.init_V_ClassCache_Component(this, disguised.message());
         }
      } else if (packet instanceof ClientboundMapItemDataPacket mapData) {
         long now = System.currentTimeMillis();
         if (this.init_Z_J(now)) {
            mapData.colorPatch().ifPresent(patch -> {
               if (patch.width() == 128 && patch.height() == 128 && patch.startX() == 0 && patch.startY() == 0) {
                  this.init_TaskQueue_2.init(patch.mapColors(), now);
               }
            });
         }
      } else if (packet instanceof ClientboundSetTitleTextPacket title) {
         long now = System.currentTimeMillis();
         this.init_V_Component_J(title.text(), now);
         if (this.init_Z_J(now)) {
            this.init_TaskQueue_2.load(title.text(), now);
         }
      } else if (packet instanceof ClientboundSetSubtitleTextPacket subtitle) {
         long now = System.currentTimeMillis();
         this.init_V_Component_J(subtitle.text(), now);
         if (this.init_Z_J(now)) {
            this.init_TaskQueue_2.load(subtitle.text(), now);
         }
      } else if (packet instanceof ClientboundPlayerChatPacket playerChat) {
         this.init_V_ClientboundPlayerC_Z(playerChat, allowedForDisplay);
      } else if (packet instanceof ClientboundPlayerInfoUpdatePacket playerInfo) {
         boolean addsPlayers = playerInfo.actions().contains(Action.ADD_PLAYER);
         boolean updatesListed = playerInfo.actions().contains(Action.UPDATE_LISTED);

         for (net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry entry : playerInfo.entries()) {
            if (entry.profile() != null) {
               this.load_Map.put(entry.profileId(), entry.profile().name());
            }

            if (entry.profile() != null
               && this.init_SyncManager_DataUt != null
               && (
                  entry.profileId().equals(this.init_UUID)
                     || entry.profileId().equals(this.init_SyncManager_DataUt.init_User().getProfileId())
                     || entry.profile().name().equalsIgnoreCase(this.init_SyncManager_DataUt.init_User().getName())
               )) {
               this.init_GameProfile = entry.profile();
            }

            if (updatesListed) {
               if (entry.listed()) {
                  this.init_V_UUID(entry.profileId());
               } else {
                  this.init_Set.remove(entry.profileId());
               }
            } else if (addsPlayers) {
               this.init_V_UUID(entry.profileId());
            }

            if (this.init_SyncManager_DataUt != null && entry.profileId().equals(this.init_SyncManager_DataUt.init_User().getProfileId())) {
               int updatedPing = Math.max(0, entry.latency());
               if (this.load_I != updatedPing) {
                  this.load_I = updatedPing;
               }
            } else if (addsPlayers
               && this.init_SyncManager_DataUt != null
               && entry.profile() != null
               && entry.profile().name().equalsIgnoreCase(this.init_SyncManager_DataUt.init_User().getName())
               && !entry.profileId().equals(this.init_UUID)) {
               this.init_UUID = entry.profileId();
               this.add_V();
            }
         }

         while (this.load_Map.size() > 2048) {
            UUID oldest = this.load_Map.keySet().iterator().next();
            this.load_Map.remove(oldest);
            this.init_Set.remove(oldest);
         }
      } else if (packet instanceof ClientboundPlayerInfoRemovePacket remove) {
         for (UUID id : remove.profileIds()) {
            this.load_Map.remove(id);
            this.init_Set.remove(id);
         }
      } else if (packet instanceof ClientboundSetPlayerTeamPacket team) {
         this.init_V_ClientboundSetPlay(team);
      } else if (packet instanceof ClientboundSetObjectivePacket objective) {
         this.init_V_ClientboundSetObje(objective);
      } else if (packet instanceof ClientboundSetDisplayObjectivePacket displayObjective) {
         this.init_V_ClientboundSetDisp(displayObjective);
      } else if (packet instanceof ClientboundSetScorePacket score) {
         this.init_V_ClientboundSetScor(score);
      } else if (packet instanceof ClientboundResetScorePacket resetScore) {
         this.init_V_ClientboundResetSc(resetScore);
      } else if (packet instanceof ClientboundCommandsPacket commandPacket) {
         long nowMs = System.currentTimeMillis();
         if (!this.clear_Z || nowMs - this.cache_J >= 750L) {
            this.cache_J = nowMs;
            this.init_CommandDispatcher = new CommandDispatcher<>(
               commandPacket.getRoot(CommandBuildContext.simple(this.init_RegistryAccess_Fro, this.init_FeatureFlagSet), init_ClientboundCommand)
            );
            this.clear_Z = true;
         }
      } else if (packet instanceof ClientboundCommandSuggestionsPacket suggestions) {
         ClassCache.Inner_TaskTimer reply = new ClassCache.Inner_TaskTimer(
            suggestions.id(),
            suggestions.start(),
            suggestions.length(),
            suggestions.suggestions().stream().map(net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket.Entry::text).toList()
         );
         synchronized (this.flush_Obj) {
            this.close_Map.put(reply.init_I(), reply);

            while (this.close_Map.size() > 64) {
               this.close_Map.remove(this.close_Map.keySet().iterator().next());
            }
         }
      } else if (packet instanceof ClientboundOpenScreenPacket open) {
         this.init_V_ClientboundOpenScr(open);
      } else if (packet instanceof ClientboundMountScreenOpenPacket mount) {
         this.init_V_ClientboundMountSc(mount);
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         this.init_V_I_2(content.containerId(), content.stateId(), content.items(), content.carriedItem());
         if (content.containerId() == 0) {
            this.poll_I = content.stateId();
         }

         if (content.containerId() == this.tick_I()) {
            this.open_I = content.stateId();
            synchronized (this.push_Obj) {
               this.init_List.clear();

               for (ItemStack stack : content.items()) {
                  this.init_List.add(init_ItemStack_ItemStack(stack).copy());
               }

               this.init_ItemStack = init_ItemStack_ItemStack(content.carriedItem()).copy();
               if (content.containerId() == 0) {
                  this.reset_Z = true;
                  this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.load;
               } else {
                  this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.read;
               }

               this.start_Z = true;
               this.block_J++;
            }

            this.pop_V();
            this.push_V();
            if (content.containerId() == 0) {
               this.set_V();
            }

            this.init_V_I(content.containerId());
         }
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         this.load_V_I_ItemStack(this.tick_I(), cursor.contents());
         synchronized (this.push_Obj) {
            this.init_ItemStack = init_ItemStack_ItemStack(cursor.contents()).copy();
            this.block_J++;
         }

         this.set_V();
         this.init_V_I(this.tick_I());
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         this.init_V_I(slot.getContainerId(), slot.getStateId(), slot.getSlot(), slot.getItem());
         if (slot.getContainerId() == 0) {
            this.poll_I = slot.getStateId();
         }

         if (slot.getContainerId() == this.tick_I()) {
            this.open_I = slot.getStateId();
            if (this.close_I < 0) {
               int h = slot.getSlot();
               synchronized (this.push_Obj) {
                  if (h >= 0 && h < this.load_List.size()) {
                     ItemStack authoritative = init_ItemStack_ItemStack(slot.getItem()).copy();
                     this.load_List.set(h, authoritative);

                     while (this.init_List.size() < this.load_List.size()) {
                        this.init_List.add(ItemStack.EMPTY);
                     }

                     this.init_List.set(h, authoritative.copy());
                  }

                  this.block_J++;
               }
            } else {
               this.init_V_I_ItemStack(slot.getSlot(), slot.getItem());
               this.pop_V();
            }

            this.push_V();
            if (slot.getContainerId() == 0) {
               this.set_V();
            }

            this.init_V_I(slot.getContainerId());
         }
      } else if (packet instanceof ClientboundContainerSetDataPacket data) {
         if (data.getContainerId() == this.tick_I()) {
            int id = data.getId();
            synchronized (this.push_Obj) {
               if (id >= 0 && id < this.init_IArr.length) {
                  this.init_IArr[id] = data.getValue();
                  this.start_I = Math.max(this.start_I, id + 1);
               }

               this.block_J++;
            }
         }
      } else if (packet instanceof ClientboundMerchantOffersPacket offers) {
         if (offers.getContainerId() == this.tick_I()) {
            synchronized (this.push_Obj) {
               this.init_MerchantOffers = offers.getOffers();
               this.stop_I = offers.getVillagerLevel();
               this.next_I = offers.getVillagerXp();
               this.value_Z = offers.showProgress();
               this.block_J++;
            }
         }
      } else if (packet instanceof ClientboundContainerClosePacket close) {
         if (close.getContainerId() == this.close_I) {
            this.pop_V();
            this.init_V_Z_Z(false, true);
         }
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         int slot = held.slot();
         if (slot >= 0 && slot <= 8) {
            this.add_I = slot;
            this.push_V();
            if (this.init_Z_Packet(new ServerboundSetCarriedItemPacket(slot), false, false)) {
               this.clear_I = slot;
            }
         }
      } else if (packet instanceof ClientboundAnimatePacket animate) {
         if (animate.getId() == this.size_I && animate.getAction() == 0) {
            this.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND), false, false);
         }
      } else if (packet instanceof ClientboundSetEntityMotionPacket motion) {
         this.init_StateCache.init_V_I_Vec3(motion.id(), motion.movement());
         if (motion.id() == this.size_I) {
            if (this.level_Z && !this.put_Z()) {
               this.read_AtomicReference.set(motion.movement());
            } else if (!this.push_Z && !this.pop_Z) {
               long nowKb = System.currentTimeMillis();
               this.run_D = motion.movement().x;
               this.get_D = motion.movement().z;
               this.level_I = 8;
               synchronized (this.sync_Obj) {
                  this.write_D = motion.movement().y;
               }

               this.f_l_J = nowKb + 15000L;
               if (nowKb < this.clear_J) {
                  this.run_Z = false;
                  this.block_Z = true;
               } else {
                  this.block_Z = true;
                  this.open_Z = false;
                  this.poll_Z = false;
               }

               this.add_J = Math.max(this.add_J, nowKb + 800L);
            }
         }
      } else if (packet instanceof ClientboundSetPassengersPacket passengers) {
         this.init_V_ClientboundSetPass(passengers);
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inv) {
         int handler = write_I_I(inv.slot());
         if (handler >= 0) {
            synchronized (this.push_Obj) {
               if (handler < this.load_List.size()) {
                  ItemStack authoritative = inv.contents() == null ? ItemStack.EMPTY : inv.contents().copy();
                  this.load_List.set(handler, authoritative);
                  if (this.close_I >= 0) {
                     int base = this.init_List.size() - 36;
                     int live = handler >= 9 && handler <= 35 ? base + (handler - 9) : (handler >= 36 && handler <= 44 ? base + 27 + (handler - 36) : -1);
                     if (live >= 0 && live < this.init_List.size()) {
                        this.init_List.set(live, authoritative.copy());
                     }
                  }
               }

               this.block_J++;
            }

            if (this.close_I < 0) {
               this.init_V_I_ItemStack(handler, inv.contents());
            }
         }

         this.push_V();
         this.init_V_I(this.tick_I());
      } else if (packet instanceof ClientboundAddEntityPacket add) {
         String type = init_Str_EntityType(add.getType());
         int ownerId = type.contains("fishing_bobber") ? add.getData() : -1;
         this.init_StateCache
            .init(
               add.getId(),
               add.getUUID(),
               type,
               add.getX(),
               add.getY(),
               add.getZ(),
               add.getMovement(),
               add.getYRot(),
               add.getXRot(),
               add.getYHeadRot(),
               false,
               ownerId,
               this.init_PositionMoveRotati.position()
            );
      } else if (packet instanceof ClientboundMoveEntityPacket move) {
         int id = ((AutismMoveEntityPacketAccessor)move).autism$getEntityId();
         this.init_StateCache
            .init(
               id,
               Compat263.moveEntityXa(move),
               Compat263.moveEntityYa(move),
               Compat263.moveEntityZa(move),
               move.hasPosition(),
               move.getYRot(),
               move.getXRot(),
               move.hasRotation(),
               move.isOnGround()
            );
      } else if (packet instanceof ClientboundMoveMinecartPacket minecart && !minecart.lerpSteps().isEmpty()) {
         MinecartStep step = minecart.lerpSteps().getLast();
         this.init_StateCache.init_V_I_2(minecart.entityId(), step.position(), step.movement(), step.yRot(), step.xRot());
      } else if (packet instanceof ClientboundEntityPositionSyncPacket sync) {
         this.init_StateCache.init(sync.id(), Compat263.entityPosSyncValues(sync), sync.onGround());
      } else if (packet instanceof ClientboundTeleportEntityPacket teleport) {
         this.init_StateCache.init(teleport.id(), teleport.change(), teleport.relatives(), teleport.onGround());
      } else if (packet instanceof ClientboundRotateHeadPacket head) {
         int id = ((AutismRotateHeadPacketAccessor)head).autism$getEntityId();
         this.init_StateCache.init_V_I_F(id, head.getYHeadRot());
      } else if (packet instanceof ClientboundSetEntityDataPacket data && load_Z_Str(this.init_StateCache.init_Str_I(data.id()))) {
         int hookedDataId = AutismFishingHookAccessor.autism$getHookedEntityData().id();

         for (DataValue<?> value : data.packedItems()) {
            if (value.id() == hookedDataId && value.value() instanceof Integer encodedTarget) {
               this.init_StateCache.init_V_I_I(data.id(), encodedTarget);
               break;
            }
         }
      } else if (packet instanceof ClientboundRemoveEntitiesPacket rem) {
         for (int i = 0; i < rem.entityIds().size(); i++) {
            int id = rem.entityIds().getInt(i);
            this.init_StateCache.init_V_I(id);
            if (this.push_Z && id == this.update_I) {
               this.state_V();
            }
         }
      } else if (packet instanceof ClientboundStartConfigurationPacket && attachment != null) {
         this.init_V_ClassCache_ConfigS(attachment);
      }
   }

   private static boolean load_Z_Str(String type) {
      return type != null && (type.equals("fishing_bobber") || type.endsWith(":fishing_bobber"));
   }

   private void load_V_J(long now) {
      this.init_AtomicBoolean.set(false);
      this.sync_Z = false;
      this.flush_Z = false;
      this.close_Z = false;
      this.run_J = 9223372036854775807L;
      this.get_J = 9223372036854775807L;
      this.set_J = now + 30000L;
   }

   private void read_V_J(long now) {
      if (!this.init_AtomicBoolean.get() && this.sync_Z && this.flush_Z && this.close_Z && this.run_J == 9223372036854775807L) {
         this.run_J = now + 500L;
      }
   }

   private void write_V_J(long now) {
      if (!this.init_AtomicBoolean.get()) {
         boolean normalReady = now >= this.run_J;
         boolean headlessReady = this.close_Z && now >= this.get_J;
         boolean timedOut = now >= this.set_J;
         if ((normalReady || headlessReady || timedOut)
            && this.init_AtomicBoolean.compareAndSet(false, true)
            && !this.init_Z_Packet(new ServerboundPlayerLoadedPacket(), true, false)) {
            this.init_AtomicBoolean.set(false);
         }
      }
   }

   private int tick_I() {
      return this.close_I >= 0 ? this.close_I : 0;
   }

   private void poll_V() {
      Arrays.fill(this.init_IArr, 0);
      this.start_I = 0;
      this.init_MerchantOffers = null;
      this.stop_I = 0;
      this.next_I = 0;
      this.value_Z = false;
   }

   private void init_V_Z_Z(boolean clearInventory, boolean notifyViewer) {
      synchronized (this.push_Obj) {
         this.buffer_J++;
         this.close_I = -1;
         this.open_I = this.poll_I;
         this.update_Str = "";
         this.sync_Str = "";
         this.init_Component = Component.empty();
         this.data_Z = false;
         this.init_ItemStack = ItemStack.EMPTY;
         this.init_List.clear();
         this.poll_V();
         this.init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.update);
         this.cache_Z = false;
         this.state_Z = false;
         this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
         if (clearInventory) {
            Collections.fill(this.load_List, ItemStack.EMPTY);
            this.reset_Z = false;
         }

         if (this.reset_Z) {
            this.init_List.addAll(init_List_List(this.load_List));
         }

         this.init_ClassCache_TextFor = this.reset_Z ? ClassCache.Inner_TextFormat.load : ClassCache.Inner_TextFormat.init;
         this.start_Z = this.reset_Z;
         this.block_J++;
      }

      if (clearInventory) {
         this.flush_Str = "";
      } else {
         this.push_V();
      }

      if (notifyViewer) {
         this.init_ClassCache_TokenSt.load_V_ClassCache(this);
      }
   }

   private void init_V_ClientboundOpenScr(ClientboundOpenScreenPacket open) {
      synchronized (this.push_Obj) {
         this.buffer_J++;
         this.close_I = open.getContainerId();
         Identifier menuType = BuiltInRegistries.MENU.getKey(open.getType());
         this.sync_Str = menuType == null ? "" : menuType.toString();
         this.init_Component = open.getTitle() == null ? Component.empty() : open.getTitle();
         this.update_Str = this.init_Component.getString();
         this.data_Z = false;
         this.init_ItemStack = ItemStack.EMPTY;
         this.init_List.clear();
         this.poll_V();
         this.init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.update);
         this.cache_Z = false;
         this.state_Z = false;
         this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
         this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.init;
         this.start_Z = false;
         this.block_J++;
      }

      this.queue_J++;
   }

   private void init_V_ClientboundMountSc(ClientboundMountScreenOpenPacket mount) {
      synchronized (this.push_Obj) {
         this.buffer_J++;
         this.close_I = mount.getContainerId();
         this.sync_Str = "autism:mount";
         this.init_Component = Component.literal("Mount");
         this.update_Str = "Mount";
         this.data_Z = false;
         this.init_ItemStack = ItemStack.EMPTY;
         this.init_List.clear();
         this.poll_V();
         this.init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.update);
         this.cache_Z = false;
         this.state_Z = false;
         this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
         this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.init;
         this.start_Z = false;
         this.block_J++;
      }

      this.queue_J++;
   }

   private void init_V_I(int containerId) {
      long now = System.currentTimeMillis();
      synchronized (this.push_Obj) {
         if (containerId == this.tick_I()) {
            this.level_J = now;
            if (this.init_ClassCache_TableIn != null
               && this.init_ClassCache_TableIn.init_J() == this.buffer_J
               && this.init_ClassCache_TableIn.init_I() == containerId) {
               this.cache_Z = true;
               this.init_ClassCache_TableIn.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.read;
            }

            if (this.state_Z) {
               this.state_Z = false;
               this.start_Z = this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.read
                  || this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.load;
               this.block_J++;
            }
         }
      }
   }

   private void init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil inFlightReason) {
      if (this.init_ClassCache_TableIn != null) {
         this.init_ClassCache_TableIn.init_ClassCache_CachePo().init_ClassCache_StreamU = inFlightReason;
      }

      this.init_ClassCache_TableIn = null;

      for (ClassCache.Inner_TableIndex queued : this.write_ArrayDeque) {
         queued.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.update;
      }

      this.write_ArrayDeque.clear();
   }

   private void init_V_I_ItemStack(int index, ItemStack stack) {
      if (index >= 0 && index < 2048) {
         synchronized (this.push_Obj) {
            while (this.init_List.size() <= index) {
               this.init_List.add(ItemStack.EMPTY);
            }

            this.init_List.set(index, stack == null ? ItemStack.EMPTY : stack.copy());
            this.block_J++;
         }
      }
   }

   private void init_V_UUID(UUID id) {
      if (id != null && (this.init_Set.size() < 8192 || this.init_Set.contains(id))) {
         this.init_Set.add(id);
      }
   }

   private void init_V_I_2(int containerId, int stateId, List<ItemStack> items, ItemStack cursor) {
      ClassCache.Inner_RingBuffer saved = this.init_ClassCache_RingBuf;
      if (saved != null && saved.init_I() == containerId) {
         this.init_ClassCache_RingBuf = new ClassCache.Inner_RingBuffer(
            containerId, stateId, saved.init_Component(), saved.init_Str(), init_List_List(items), init_ItemStack_ItemStack(cursor).copy()
         );
      }
   }

   private void init_V_I(int containerId, int stateId, int slot, ItemStack stack) {
      ClassCache.Inner_RingBuffer saved = this.init_ClassCache_RingBuf;
      if (saved != null && saved.init_I() == containerId && slot >= 0 && slot < saved.init_List().size()) {
         List<ItemStack> slots = init_List_List(saved.init_List());
         slots.set(slot, init_ItemStack_ItemStack(stack).copy());
         this.init_ClassCache_RingBuf = new ClassCache.Inner_RingBuffer(
            containerId, stateId, saved.init_Component(), saved.init_Str(), slots, saved.init_ItemStack()
         );
      }
   }

   private void load_V_I_ItemStack(int containerId, ItemStack cursor) {
      ClassCache.Inner_RingBuffer saved = this.init_ClassCache_RingBuf;
      if (saved != null && saved.init_I() == containerId) {
         this.init_ClassCache_RingBuf = new ClassCache.Inner_RingBuffer(
            saved.init_I(), saved.load(), saved.init_Component(), saved.init_Str(), saved.init_List(), init_ItemStack_ItemStack(cursor).copy()
         );
      }
   }

   private static int write_I_I(int playerSlot) {
      if (playerSlot >= 0 && playerSlot <= 8) {
         return 36 + playerSlot;
      } else if (playerSlot >= 9 && playerSlot <= 35) {
         return playerSlot;
      } else if (playerSlot >= 36 && playerSlot <= 39) {
         return 44 - playerSlot;
      } else {
         return playerSlot == 40 ? 45 : -1;
      }
   }

   private static String init_Str_CommonPlayerSpawnI(CommonPlayerSpawnInfo info) {
      try {
         return info == null ? "" : info.dimension().identifier().toString();
      } catch (RuntimeException error) {
         return "";
      }
   }

   private void init_V_ClientboundUpdateA(ClientboundUpdateAttributesPacket packet) {
      if (packet.getEntityId() == this.size_I) {
         try {
            for (AttributeSnapshot snapshot : packet.getValues()) {
               double effective = init_D_ClientboundUpdateA(snapshot);
               if (snapshot.attribute().value() == Attributes.MAX_HEALTH.value()) {
                  this.read_F = (float)Math.max(1.0, effective);
               } else if (snapshot.attribute().value() == Attributes.BLOCK_INTERACTION_RANGE.value()) {
                  this.pop_D = Math.max(0.0, effective);
               } else if (snapshot.attribute().value() == Attributes.ENTITY_INTERACTION_RANGE.value()) {
                  this.tick_D = Math.max(0.0, effective);
               }
            }
         } catch (RuntimeException var6) {
         }
      }
   }

   private static double init_D_ClientboundUpdateA(AttributeSnapshot snapshot) {
      double base = snapshot.base();
      double multipliedBase = 0.0;
      double multipliedTotal = 1.0;

      for (AttributeModifier modifier : snapshot.modifiers()) {
         switch (modifier.operation()) {
            case ADD_VALUE:
               base += modifier.amount();
               break;
            case ADD_MULTIPLIED_BASE:
               multipliedBase += modifier.amount();
               break;
            case ADD_MULTIPLIED_TOTAL:
               multipliedTotal *= 1.0 + modifier.amount();
         }
      }

      return base * (1.0 + multipliedBase) * multipliedTotal;
   }

   private String next_Str() {
      SyncManager.Inner_DataUtil resolved = this.init_SyncManager_DataUt;
      return resolved == null ? this.init_FieldCache_BufferH.init_Str() : resolved.init_User().getName();
   }

   private int run_I() {
      return this.close_I < 0 ? 36 + this.add_I : this.init_List.size() - 9 + this.add_I;
   }

   private void push_V() {
      String name = "";
      synchronized (this.push_Obj) {
         if (this.close_I < 0) {
            int idx = 36 + Math.max(0, Math.min(8, this.add_I));
            if (idx < this.load_List.size()) {
               ItemStack stack = this.load_List.get(idx);
               if (stack != null && !stack.isEmpty()) {
                  name = stack.getHoverName().getString();
               }
            }
         } else {
            int idx = this.run_I();
            if (idx >= 0 && idx < this.init_List.size()) {
               ItemStack stack = this.init_List.get(idx);
               if (stack != null && !stack.isEmpty()) {
                  name = stack.getHoverName().getString();
               }
            }
         }
      }

      this.flush_Str = name;
   }

   public int init_I_I(int handler) {
      synchronized (this.push_Obj) {
         if (this.close_I < 0) {
            return handler >= 36 && handler <= 44 ? handler - 36 : -1;
         }

         int start = this.init_List.size() - 9;
         return handler >= start && handler < this.init_List.size() ? handler - start : -1;
      }
   }

   public int update_I() {
      int idx = Math.max(0, Math.min(8, this.add_I));
      synchronized (this.push_Obj) {
         return this.close_I >= 0 && this.init_ClassCache_TextFor != ClassCache.Inner_TextFormat.write ? this.init_List.size() - 9 + idx : 36 + idx;
      }
   }

   public int sync_I() {
      synchronized (this.push_Obj) {
         return this.close_I < 0 ? 46 : this.init_List.size();
      }
   }

   public String push_Str() {
      return this.close_I < 0 ? "" : this.sync_Str;
   }

   private ClassCache.Inner_FileCache init_ClassCache_FileCac() {
      int[] data = Arrays.copyOf(this.init_IArr, Math.max(0, Math.min(this.start_I, this.init_IArr.length)));
      List<ClassCache.Inner_ListPool> trades = List.of();
      MerchantOffers offers = this.init_MerchantOffers;
      if (offers != null && !offers.isEmpty()) {
         List<ClassCache.Inner_ListPool> list = new ArrayList<>(offers.size());

         for (MerchantOffer o : offers) {
            if (o != null) {
               list.add(
                  new ClassCache.Inner_ListPool(
                     init_ItemStack_ItemStack(o.getCostA()).copy(),
                     init_ItemStack_ItemStack(o.getCostB()).copy(),
                     init_ItemStack_ItemStack(o.getResult()).copy(),
                     o.isOutOfStock(),
                     o.getUses(),
                     o.getMaxUses(),
                     o.getXp(),
                     o.getSpecialPriceDiff()
                  )
               );
            }
         }

         trades = list;
      }

      return new ClassCache.Inner_FileCache(this.sync_Str, data, trades, this.stop_I, this.next_I, this.value_Z);
   }

   public ClassCache.Inner_EventBus init_ClassCache_EventBu() {
      synchronized (this.push_Obj) {
         return new ClassCache.Inner_EventBus(
            this.init_ClassCache_TextFor,
            this.buffer_J,
            this.tick_I(),
            this.sync_Str,
            this.close_I < 0 ? this.poll_I : this.open_I,
            this.init_Component == null ? Component.empty() : this.init_Component.copy(),
            init_List_List(this.init_List),
            init_ItemStack_ItemStack(this.init_ItemStack).copy(),
            this.reset_Z,
            this.start_Z && !this.state_Z,
            this.data_Z,
            this.set_I(),
            this.state_Z
         );
      }
   }

   public ClassCache.Inner_ByteReader init_ClassCache_ByteRea() {
      synchronized (this.push_Obj) {
         if (this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.init) {
            return new ClassCache.Inner_ByteReader(
               Component.literal("Inventory synchronizing..."),
               List.of(),
               ItemStack.EMPTY,
               Math.max(0, this.close_I),
               this.close_I < 0 ? this.poll_I : this.open_I,
               ClassCache.Inner_TextFormat.init,
               false,
               this.buffer_J,
               this.get_I(),
               this.state_Z,
               ClassCache.Inner_FileCache.init_ClassCache_FileCac
            );
         }

         if (this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write) {
            return this.load_ClassCache_ByteRea();
         }

         if (this.close_I >= 0 && !this.data_Z) {
            List<ClassCache.Inner_MapStore> out = new ArrayList<>();
            int size = this.init_List.size();
            int containerCount = Math.max(0, size - 36);
            int auxCount = this.init_MerchantOffers != null ? this.init_MerchantOffers.size() : 0;
            autismclient.gui.b.TaskQueue.Inner_DataUtil layout = autismclient.gui.b.TaskQueue.init_TaskQueue_DataUtil_Str(
               this.sync_Str, containerCount, auxCount
            );

            for (int i = 0; i < containerCount; i++) {
               this.init_V_List(out, i, layout.init_IArr()[i], layout.load()[i]);
            }

            int invTop = layout.init_I();
            int base = containerCount;

            for (int i = 0; i < 27 && base + i < size; i++) {
               this.init_V_List(out, base + i, i % 9 * 18, invTop + i / 9 * 18);
            }

            for (int i = 0; i < 9 && base + 27 + i < size; i++) {
               this.init_V_List(out, base + 27 + i, i * 18, invTop + 54 + 4);
            }

            Component title = this.update_Str.isBlank() ? Component.literal("Container") : this.init_Component;
            return new ClassCache.Inner_ByteReader(
               title,
               out,
               this.init_ItemStack,
               this.close_I,
               this.open_I,
               this.init_ClassCache_TextFor,
               this.start_Z && this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.read,
               this.buffer_J,
               this.get_I(),
               this.state_Z,
               this.init_ClassCache_FileCac()
            );
         } else {
            return this.load_ClassCache_ByteRea();
         }
      }
   }

   public ClassCache.Inner_ByteReader load_ClassCache_ByteRea() {
      synchronized (this.push_Obj) {
         List<ClassCache.Inner_MapStore> out = new ArrayList<>();
         boolean synthetic = this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write;
         int base = this.close_I >= 0 && !synthetic ? this.init_List.size() - 36 : -1;
         this.init_V_List(out, 0, this.init_I_I_I(0, base), 146, 20);

         for (int i = 1; i <= 4; i++) {
            this.init_V_List(out, i, this.init_I_I_I(i, base), 90 + (i - 1) % 2 * 18, 10 + (i - 1) / 2 * 18);
         }

         for (int i = 5; i <= 8; i++) {
            this.init_V_List(out, i, this.init_I_I_I(i, base), 0, (i - 5) * 18);
         }

         for (int i = 9; i <= 35; i++) {
            this.init_V_List(out, i, this.init_I_I_I(i, base), (i - 9) % 9 * 18, 76 + (i - 9) / 9 * 18);
         }

         for (int i = 36; i <= 44; i++) {
            this.init_V_List(out, i, this.init_I_I_I(i, base), (i - 36) * 18, 134);
         }

         this.init_V_List(out, 45, this.init_I_I_I(45, base), 69, 54);
         boolean interactive = this.start_Z
            && !this.state_Z
            && (
               this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.load
                  || this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.read && this.data_Z
            );
         Component title = this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write
            ? Component.literal(this.next_Str() + " Inventory (saved GUI isolated)")
            : Component.literal(this.next_Str() + " Inventory");
         ItemStack cursor = this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write ? ItemStack.EMPTY : this.init_ItemStack;
         return new ClassCache.Inner_ByteReader(
            title,
            out,
            cursor,
            synthetic ? 0 : Math.max(0, this.close_I),
            !synthetic && this.close_I >= 0 ? this.open_I : this.poll_I,
            this.init_ClassCache_TextFor,
            interactive,
            this.buffer_J,
            this.get_I(),
            this.state_Z,
            ClassCache.Inner_FileCache.init_ClassCache_FileCac
         );
      }
   }

   private int get_I() {
      synchronized (this.push_Obj) {
         return this.set_I();
      }
   }

   private int set_I() {
      long pending = this.init_ClassCache_TableIn == null ? 0L : this.init_ClassCache_TableIn.write();

      for (ClassCache.Inner_TableIndex queued : this.write_ArrayDeque) {
         pending += queued.write();
      }

      return (int)Math.min(2147483647L, pending);
   }

   private int init_I_I_I(int h, int base) {
      if (base < 0) {
         return h;
      } else if (h >= 9 && h <= 35) {
         return base + (h - 9);
      } else {
         return h >= 36 && h <= 44 ? base + 27 + (h - 36) : -1;
      }
   }

   private void init_V_List(List<ClassCache.Inner_MapStore> out, int h, int clickHandler, int x, int y) {
      ItemStack stack = h >= 0 && h < this.load_List.size() && this.load_List.get(h) != null ? this.load_List.get(h) : ItemStack.EMPTY;
      out.add(new ClassCache.Inner_MapStore(x, y, clickHandler, stack));
   }

   private void init_V_List(List<ClassCache.Inner_MapStore> out, int handler, int x, int y) {
      ItemStack stack = handler >= 0 && handler < this.init_List.size() && this.init_List.get(handler) != null ? this.init_List.get(handler) : ItemStack.EMPTY;
      out.add(new ClassCache.Inner_MapStore(x, y, handler, stack));
   }

   private void pop_V() {
      synchronized (this.push_Obj) {
         int size = this.init_List.size();
         if (this.close_I < 0) {
            for (int h = 0; h < 46; h++) {
               this.load_List.set(h, h < size ? init_ItemStack_ItemStack(this.init_List.get(h)).copy() : ItemStack.EMPTY);
            }
         } else {
            int base = size - 36;
            if (base < 0) {
               return;
            }

            for (int i = 0; i < 27; i++) {
               this.load_List.set(9 + i, init_ItemStack_ItemStack(this.init_List.get(base + i)).copy());
            }

            for (int i = 0; i < 9; i++) {
               this.load_List.set(36 + i, init_ItemStack_ItemStack(this.init_List.get(base + 27 + i)).copy());
            }
         }
      }
   }

   private static ItemStack init_ItemStack_ItemStack(ItemStack stack) {
      return stack == null ? ItemStack.EMPTY : stack;
   }

   long open_J() {
      return this.block_J;
   }

   public String pop_Str() {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      synchronized (this.push_Obj) {
         if (this.init_ClassCache_TableIn != null || !this.write_ArrayDeque.isEmpty()) {
            if (this.init_ClassCache_NetHelp == ClassCache.Inner_NetHelper.init) {
               this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.load;
            }

            return "Sent";
         }
      }

      this.tick_V();
      return "Sent";
   }

   private void tick_V() {
      synchronized (this.push_Obj) {
         this.data_Z = true;
         this.update_Str = "";
         this.init_Component = Component.empty();
         this.block_J++;
      }
   }

   @Override
   public boolean init_Z_Z_Z(boolean closeAfter, boolean sendClosePacket) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return false;
      }

      synchronized (this.push_Obj) {
         List<ItemStack> source = this.init_List.isEmpty() ? this.load_List : this.init_List;
         this.init_ClassCache_RingBuf = new ClassCache.Inner_RingBuffer(
            this.tick_I(),
            this.open_I,
            this.init_Component == null ? Component.empty() : this.init_Component.copy(),
            this.update_Str,
            init_List_List(source),
            init_ItemStack_ItemStack(this.init_ItemStack).copy()
         );
      }

      if (!closeAfter) {
         return true;
      } else {
         return sendClosePacket ? "Sent".equals(this.run_Str()) : "Sent".equals(this.pop_Str());
      }
   }

   @Override
   public boolean flush_Z() {
      if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && this.close_I >= 0) {
         synchronized (this.push_Obj) {
            if (this.init_ClassCache_TableIn != null || !this.write_ArrayDeque.isEmpty()) {
               this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.write;
               return true;
            }
         }

         boolean sent = this.init_Z_Packet(new ServerboundContainerClosePacket(this.close_I), false, false);
         if (sent) {
            this.run_V();
         }

         return sent;
      } else {
         return false;
      }
   }

   private void run_V() {
      synchronized (this.push_Obj) {
         if (!this.sync_Str.startsWith("synthetic:")) {
            this.sync_Str = "synthetic:" + this.sync_Str;
         }

         this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.write;
         this.start_Z = false;
         this.data_Z = false;
         this.block_J++;
      }
   }

   @Override
   public boolean close_Z() {
      ClassCache.Inner_RingBuffer saved = this.init_ClassCache_RingBuf;
      if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && saved != null) {
         synchronized (this.push_Obj) {
            this.buffer_J++;
            this.close_I = saved.init_I() == 0 ? -1 : saved.init_I();
            this.open_I = saved.load();
            this.init_Component = saved.init_Component() == null ? Component.empty() : saved.init_Component().copy();
            this.update_Str = saved.init_Str() == null ? "" : saved.init_Str();
            this.sync_Str = "synthetic:saved";
            this.data_Z = false;
            this.init_ItemStack = init_ItemStack_ItemStack(saved.init_ItemStack()).copy();
            this.init_List.clear();
            this.init_List.addAll(init_List_List(saved.init_List()));
            this.init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.update);
            this.cache_Z = false;
            this.state_Z = false;
            this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
            this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.write;
            this.start_Z = false;
            this.block_J++;
         }

         this.push_V();
         return true;
      } else {
         return false;
      }
   }

   private static List<ItemStack> init_List_List(List<ItemStack> stacks) {
      List<ItemStack> copy = new ArrayList<>(stacks == null ? 0 : stacks.size());
      if (stacks != null) {
         for (ItemStack stack : stacks) {
            copy.add(init_ItemStack_ItemStack(stack).copy());
         }
      }

      return copy;
   }

   @Override
   public int init_I_aQ_J(aQ action, long now) {
      if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && action != null) {
         if (this.value_Z()) {
            return 0;
         }

         ClassCache.Inner_ArrayUtil job = this.init_ClassCache_ArrayUt;
         if (job == null || job.init_aQ != action) {
            boolean hadContainer = this.close_I >= 0;
            if (hadContainer && this.load_BlockPos == null && this.run_I < 0) {
               return -1;
            }

            job = new ClassCache.Inner_ArrayUtil(action, hadContainer, this.load_BlockPos, this.run_I);
            if (action.mode == aQ.Inner_StreamUtil.init) {
               this.queue_Z = true;
               this.buffer_Z = true;
            }

            if (hadContainer && action.mode == aQ.Inner_StreamUtil.init) {
               synchronized (this.push_Obj) {
                  int ownSlots = Math.max(0, this.init_List.size() - 36);
                  job.init_ArrayDeque.addAll(UtilCore.init(action, this.init_List, ownSlots));
               }
            }

            this.init_ClassCache_ArrayUt = job;
         }

         if (now < job.init_J) {
            return 0;
         }

         int budget = 8;

         while (budget-- > 0) {
            switch (job.init_ClassCache_ClassCa) {
               case init:
                  Integer slot = job.init_ArrayDeque.pollFirst();
                  if (slot != null) {
                     if (!this.init_Z_I(slot, 0, ContainerInput.QUICK_MOVE)) {
                        return this.put_I();
                     }

                     job.init_J = now + 50L;
                     return 0;
                  }

                  job.init_ClassCache_ClassCa = ClassCache.Inner_ClassCache.load;
                  break;
               case load:
                  int closingId = this.close_I;
                  if (closingId >= 0 && !this.init_Z_Packet(new ServerboundContainerClosePacket(closingId), false, false)) {
                     return this.put_I();
                  }

                  this.get_V();
                  job.init_ClassCache_ClassCa = ClassCache.Inner_ClassCache.read;
                  job.init_J = now + 50L;
                  return 0;
               case read:
                  this.get_V();
                  synchronized (this.push_Obj) {
                     job.load.addAll(UtilCore.init(action, this.load_List, this.init_ItemStack));
                  }

                  job.init_ClassCache_ClassCa = ClassCache.Inner_ClassCache.write;
                  break;
               case write:
                  UtilCore.Inner_DataUtil click = job.load.pollFirst();
                  if (click != null) {
                     if (!this.init_Z_I(click.init_I(), click.load(), click.init_ContainerInput())) {
                        return this.put_I();
                     }

                     long delay = Math.max(0L, Math.min(500L, click.init_J()));
                     job.init_J = now + delay;
                     return 0;
                  }

                  this.queue_Z = true;
                  this.set_V();
                  job.init_ClassCache_ClassCa = job.init_Z ? ClassCache.Inner_ClassCache.update : ClassCache.Inner_ClassCache.sync;
                  break;
               case update:
                  boolean sent;
                  if (job.init_BlockPos != null) {
                     BlockPos pos = job.init_BlockPos;
                     sent = "Sent".equals(this.init_Str_I_2(pos.getX(), pos.getY(), pos.getZ(), "UP"));
                  } else {
                     sent = "Sent".equals(this.init_Str_I_Z(job.init_I, false));
                  }

                  if (!sent) {
                     return this.put_I();
                  }

                  job.init_ClassCache_ClassCa = ClassCache.Inner_ClassCache.sync;
                  job.init_J = now + 50L;
                  return 0;
               case sync:
                  this.init_ClassCache_ArrayUt = null;
                  this.set_V();
                  return 1;
            }
         }

         return 0;
      } else {
         return -1;
      }
   }

   private int put_I() {
      this.init_ClassCache_ArrayUt = null;
      this.queue_Z = true;
      this.set_V();
      return -1;
   }

   @Override
   public void read_V() {
      this.init_ClassCache_ArrayUt = null;
      this.set_V();
   }

   private void get_V() {
      synchronized (this.push_Obj) {
         this.buffer_J++;
         this.close_I = -1;
         this.open_I = this.poll_I;
         this.update_Str = "";
         this.sync_Str = "";
         this.init_Component = Component.empty();
         this.data_Z = false;
         this.init_List.clear();
         this.init_List.addAll(init_List_List(this.load_List));
         this.init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.update);
         this.cache_Z = false;
         this.state_Z = false;
         this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
         this.init_ClassCache_TextFor = ClassCache.Inner_TextFormat.load;
         this.start_Z = this.reset_Z;
         this.block_J++;
      }

      this.push_V();
   }

   private void set_V() {
      synchronized (this.push_Obj) {
         if (!this.queue_Z) {
            this.buffer_Z = false;
         } else {
            boolean stored = !init_ItemStack_ItemStack(this.init_ItemStack).isEmpty();

            for (int slot = 0; !stored && slot <= 8 && slot < this.load_List.size(); slot++) {
               stored = !init_ItemStack_ItemStack(this.load_List.get(slot)).isEmpty();
            }

            if (!stored && this.load_List.size() > 45) {
               stored = !init_ItemStack_ItemStack(this.load_List.get(45)).isEmpty();
            }

            if (!stored) {
               this.queue_Z = false;
            }

            this.buffer_Z = stored;
         }
      }
   }

   private void init_V_ClassCache_ConfigS(ClassCache.Inner_ConfigStore previousAttachment) {
      ClassCache.Inner_ConfigStore replacement;
      synchronized (this.load_Obj) {
         if (!this.init_Z_ClassCache_ConfigS(previousAttachment)
            || this.init_MapStore_CachePool != MapStore.Inner_CachePool.read
            || !previousAttachment.init_Connection.isConnected()) {
            return;
         }

         replacement = new ClassCache.Inner_ConfigStore(previousAttachment.init_Connection, previousAttachment.init_I, ++this.push_J);
         this.init_ClassCache_ConfigS = replacement;
      }

      Connection transitioning = replacement.init_Connection;
      MapStore.Inner_JsonUtil playTarget = this.init_MapStore_JsonUtil_ClassCache_IoWorke(
         ClassCache.Inner_IoWorker.read, transitioning, replacement.init_I, replacement
      );

      try {
         synchronized (this.read_Obj) {
            label153: {
               synchronized (this.load_Obj) {
                  if (!this.init_Z_ClassCache_ConfigS(replacement) || this.init_MapStore_CachePool != MapStore.Inner_CachePool.read) {
                     return;
                  }
               }

               this.init_MapStore.init_V();
               this.init_MapStore.init_V_Connection(transitioning);
               autismclient.g.BufferHelper.init(
                  transitioning, this.init_SyncManager_DataUt.init_User().getProfileId(), this.init_SyncManager_DataUt.init_User().getName()
               );
               autismclient.g.BufferHelper.init(transitioning, this.write_Str, this.write_I);
               autismclient.g.BufferHelper.init(transitioning, Type.OTHER);
               synchronized (this.load_Obj) {
                  if (this.init_Z_ClassCache_ConfigS(replacement) && this.init_MapStore_CachePool == MapStore.Inner_CachePool.read) {
                     break label153;
                  }
               }

               return;
            }
         }

         synchronized (this.tick_Obj) {
            this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.update, "Reconfiguring");
            this.buffer();
         }

         this.entry();
         this.clear_V();
         this.init_ClassCache_RingBuf = null;
         this.init_ClassCache_ArrayUt = null;
         synchronized (this.flush_Obj) {
            this.close_Map.clear();
         }

         this.clear_Z = false;
         this.write_Z = false;
         this.update_Z = false;
         this.open_I = 0;
         this.poll_I = 0;
         this.init_V_Z_Z(true, true);
         this.queue_Z = false;
         this.buffer_Z = false;
         this.flush_Str = "";
         this.close_Str = "";
         this.load_F = 20.0F;
         this.read_F = 20.0F;
         this.data_I = 20;
         this.frame_J = 0L;
         this.init_StateCache.init_V();
         Runnable applyProtocolTransition = () -> {
            try {
               synchronized (this.read_Obj) {
                  synchronized (this.load_Obj) {
                     if (this.init_Z_ClassCache_ConfigS(replacement)
                        && this.init_MapStore_CachePool == MapStore.Inner_CachePool.read
                        && transitioning.isConnected()) {
                        transitioning.setupInboundProtocol(ConfigurationProtocols.CLIENTBOUND, replacement.init_ClientConfiguratio);
                        if (!this.init_Z_MapStore_JsonUtil(playTarget, replacement, ServerboundConfigurationAcknowledgedPacket.INSTANCE)) {
                           throw new IllegalStateException("Reconfiguration acknowledgement was refused");
                        }

                        this.init_MapStore_CachePool = MapStore.Inner_CachePool.load;
                        transitioning.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND);
                     }
                  }
               }
            } catch (Throwable var12) {
               Throwable errorx = var12;
               synchronized (this.load_Obj) {
                  if (this.init_Z_ClassCache_ConfigS(replacement)) {
                     this.init_TaskQueue.init_V_Throwable(errorx);
                  }
               }

               this.init_V_ClassCache_ConfigS_Str(replacement, "Reconfiguration failed: " + init_Str_Throwable(var12));
            }
         };
         Channel channel = ((AutismClientConnectionAccessor)transitioning).getChannel();
         if (channel != null) {
            channel.eventLoop().execute(applyProtocolTransition);
         } else {
            applyProtocolTransition.run();
         }
      } catch (Throwable var19) {
         Throwable error = var19;
         synchronized (this.load_Obj) {
            if (this.init_Z_ClassCache_ConfigS(replacement)) {
               this.init_TaskQueue.init_V_Throwable(error);
            }
         }

         this.init_V_ClassCache_ConfigS_Str(replacement, "Reconfiguration failed: " + init_Str_Throwable(var19));
      }
   }

   private void init_V_ClientboundPlayerC_Z(ClientboundPlayerChatPacket packet, boolean show) {
      Optional<SignedMessageBody> unpacked;
      boolean acknowledge;
      synchronized (this.update_Obj) {
         int expected = this.flush_I++;
         if (packet.globalIndex() != expected) {
            this.open("Bad chat index: expected " + expected + ", got " + packet.globalIndex());
            return;
         }

         unpacked = packet.body().unpack(this.init_MessageSignatureCa);
         if (unpacked.isEmpty()) {
            this.open("Unrecognized chat signature");
            return;
         }

         this.init_MessageSignatureCa.push(unpacked.get(), packet.signature().orElse(null));
         acknowledge = packet.signature() != null
            && this.init_LastSeenMessagesTr.addPending(packet.signature().orElse(null), show)
            && this.init_LastSeenMessagesTr.offset() > 64;
      }

      if (acknowledge) {
         this.buffer();
      }

      this.flush_V_Str(unpacked.get().content());
      if (show) {
         String sender = this.load_Map.getOrDefault(packet.sender(), packet.sender().toString().substring(0, 8));
         Component content = packet.unsignedContent().orElse(Component.literal(unpacked.get().content()));
         this.init_ClassCache_TokenSt.init_V_ClassCache_Component(this, Component.literal("<" + sender + "> ").append(content));
      }
   }

   private void init_V_Component_J(Component component, long now) {
      if (component != null && AutismConfig.getGlobal().multiAutoSolveCaptcha) {
         try {
            String s = component.getString();
            if (s != null && init_Pattern.matcher(s).find()) {
               this.parse = now + 30000L;
            }
         } catch (Throwable var5) {
         }
      }
   }

   private boolean init_Z_J(long now) {
      if (this.level_Z) {
         return false;
      } else if (!AutismConfig.getGlobal().multiAutoSolveCaptcha) {
         return false;
      } else if (this.init_TaskQueue_2.init_Z_J(now)) {
         return true;
      } else {
         return now < this.parse ? true : this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush;
      }
   }

   static boolean get_Z() {
      return ck.init_ck().clear_Z() || autismclient.modules.PoolEntry.init_PoolEntry() != null && autismclient.modules.PoolEntry.init_PoolEntry().set_Z();
   }

   private void load_V_MapStore_JsonUtil_Packet(MapStore.Inner_JsonUtil target, Packet<?> packet) {
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return;
         }
      }

      if (packet instanceof ClientboundKeepAlivePacket keepAlive) {
         this.init_Z_MapStore_JsonUtil_2(target, new ServerboundKeepAlivePacket(keepAlive.getId()), true, false);
      } else if (packet instanceof ClientboundPingPacket pingPacket) {
         this.init_Z_MapStore_JsonUtil_2(target, new ServerboundPongPacket(pingPacket.getId()), true, false);
      } else if (packet instanceof ClientboundResourcePackPushPacket pack) {
         this.init_MapStore.init(target, pack, System.currentTimeMillis(), get_Z());
      } else if (packet instanceof ClientboundResourcePackPopPacket pack) {
         this.init_MapStore.init_V_MapStore_JsonUtil_Optional(target, pack.id());
      } else if (packet instanceof ClientboundStoreCookiePacket cookie) {
         synchronized (this.init_Map) {
            this.init_Map.remove(cookie.key());
            while (this.init_Map.size() >= 256) {
               var it = this.init_Map.keySet().iterator();
               it.next();
               it.remove();
            }
            this.init_Map.put(cookie.key(), (byte[])cookie.payload().clone());
         }
      } else if (packet instanceof ClientboundCookieRequestPacket request) {
         byte[] stored;
         synchronized (this.init_Map) {
            stored = this.init_Map.get(request.key());
         }
         this.init_Z_MapStore_JsonUtil_2(target, new ServerboundCookieResponsePacket(request.key(), stored), true, false);
      } else if (packet instanceof ClientboundDisconnectPacket disconnected) {
         if (this.init_Z_MapStore_JsonUtil_Component(target, disconnected.reason())) {
            return;
         }

         if (this.load_Z_MapStore_JsonUtil_Component(target, disconnected.reason())) {
            return;
         }

         if (this.read_Z_MapStore_JsonUtil_Component(target, disconnected.reason())) {
            return;
         }

         synchronized (this.load_Obj) {
            if (!this.init_Z_MapStore_JsonUtil(target)) {
               return;
            }

            this.load_Str = "Server disconnect";
         }

         target.init_Connection().disconnect(disconnected.reason());
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         this.init_V_MapStore_JsonUtil_2(target, transfer.host(), transfer.port());
      }
   }

   private boolean init_Z_MapStore_JsonUtil_Component(MapStore.Inner_JsonUtil target, Component reason) {
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return false;
         }
      }

      if (!this.load_Z && !this.load_AtomicBoolean.get() && !this.write_Str.isBlank()) {
         String text = reason == null ? "" : reason.getString().toLowerCase(Locale.ROOT);
         if (!text.contains("signature")) {
            return false;
         }

         this.load_Z = true;
         this.push("Server rejected signed chat; rejoining without a key in 5s (chat may be limited here).");
         this.init_V_MapStore_JsonUtil(target, this.write_Str, this.write_I, false, 5000L, false);
         return true;
      } else {
         return false;
      }
   }

   private boolean load_Z_MapStore_JsonUtil_Component(MapStore.Inner_JsonUtil target, Component reason) {
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return false;
         }
      }

      if (!this.load_AtomicBoolean.get() && !this.write_Str.isBlank()) {
         String text = reason == null ? "" : reason.getString().toLowerCase(Locale.ROOT);
         boolean verifiedOk = text.contains("successfully passed")
            || text.contains("passed the verification")
            || text.contains("success") && text.contains("verif")
            || text.contains("able to play")
            || text.contains("when you reconnect");
         if (!verifiedOk) {
            return false;
         }

         long delay = 5000L + ThreadLocalRandom.current().nextLong(2000L);
         this.init_V_MapStore_JsonUtil(target, this.write_Str, this.write_I, false, delay, false);
         return true;
      } else {
         return false;
      }
   }

   private boolean size_Z() {
      long nowMs = System.currentTimeMillis();
      if (nowMs - this.f_a_J > 10000L) {
         this.f_a_J = nowMs;
         this.node_I = 0;
      }

      if (++this.node_I > 12) {
         this.open("Too many server transfers; stopping to avoid a redirect loop.");
         return false;
      } else {
         return true;
      }
   }

   private boolean read_Z_MapStore_JsonUtil_Component(MapStore.Inner_JsonUtil target, Component reason) {
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return false;
         }
      }

      if (!this.load_AtomicBoolean.get() && !this.write_Str.isBlank()) {
         if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush) {
            return false;
         } else {
            String text = reason == null ? "" : reason.getString().toLowerCase(Locale.ROOT);
            boolean failure = text.contains("captcha")
               || text.contains("verif")
               || text.contains("incorrect")
               || text.contains("wrong answer")
               || text.contains("try again")
               || text.contains("too many")
               || text.contains("didn't solve")
               || text.contains("did not solve")
               || text.contains("failed the");
            if (!failure) {
               return false;
            } else if (this.buffer_I >= 5) {
               this.push("Verification failed " + this.buffer_I + "x; giving up on " + this.write_Str + ".");
               return false;
            } else {
               this.buffer_I++;
               long delay = 4000L + ThreadLocalRandom.current().nextLong(2000L);
               this.push("Verification failed; retrying (" + this.buffer_I + "/5) in ~4s for a fresh captcha.");
               this.init_V_MapStore_JsonUtil(target, this.write_Str, this.write_I, false, delay, false);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void init_V_MapStore_JsonUtil_2(MapStore.Inner_JsonUtil target, String host, int port) {
      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return;
         }
      }

      if (this.size_Z()) {
         this.load_Z = false;
         this.init_UUID = null;
         this.init_V_MapStore_JsonUtil(target, host, port, true, 0L, false);
      }
   }

   private void init_V_Str_2(String host, int port, boolean transferIntent, long delayMs) {
      this.init_V_MapStore_JsonUtil(null, host, port, transferIntent, delayMs, false);
   }

   private void init_V_Str(String host, int port, boolean transferIntent, long delayMs, boolean reresolve) {
      this.init_V_MapStore_JsonUtil(null, host, port, transferIntent, delayMs, reresolve);
   }

   private void init_V_MapStore_JsonUtil(
      MapStore.@Nullable Inner_JsonUtil expectedTarget, String host, int port, boolean transferIntent, long delayMs, boolean reresolve
   ) {
      if (!this.load_AtomicBoolean.get() && host != null && !host.isBlank()) {
         if (expectedTarget != null) {
            synchronized (this.load_Obj) {
               if (!this.init_Z_MapStore_JsonUtil(expectedTarget)) {
                  return;
               }
            }
         }

         String transferDetail = delayMs > 0L ? "Reconnecting in " + delayMs / 1000L + "s" : "Reconnecting to " + host;
         synchronized (this.tick_Obj) {
            this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.read, transferDetail);
         }

         this.init_V_Z_Z(true, true);
         Connection old;
         int transferEpoch;
         synchronized (this.load_Obj) {
            if (this.load_AtomicBoolean.get() || expectedTarget != null && !this.init_Z_MapStore_JsonUtil(expectedTarget)) {
               return;
            }

            old = this.init_Connection;
            this.read_I++;
            transferEpoch = this.read_I;
            this.init_ClassCache_ConfigS = null;
            this.init_MapStore_CachePool = MapStore.Inner_CachePool.init;
         }

         synchronized (this.read_Obj) {
            label156: {
               synchronized (this.load_Obj) {
                  if (!this.load_Z_Connection_I(old, transferEpoch)) {
                     return;
                  }
               }

               this.init_MapStore.init_V();
               synchronized (this.load_Obj) {
                  if (this.load_Z_Connection_I(old, transferEpoch)) {
                     break label156;
                  }
               }

               return;
            }
         }

         if (old != null) {
            init_V_Connection_Component(old, Component.literal("Reconnecting to " + host + ":" + port));
         }

         String targetHost = host;
         int targetPort = port;
         Executor exec = delayMs > 0L ? CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS, this.init_Executor) : this.init_Executor;
         CompletableFuture.<InetSocketAddress>supplyAsync(() -> {
            synchronized (this.load_Obj) {
               if (!this.load_Z_Connection_I(old, transferEpoch)) {
                  return null;
               }
            }

            return init_InetSocketAddress_Str_I(targetHost, targetPort);
         }, exec).whenComplete((address, error) -> {
            synchronized (this.load_Obj) {
               if (!this.load_Z_Connection_I(old, transferEpoch)) {
                  return;
               }
            }

            if (error == null && address != null) {
               if (reresolve) {
                  try {
                     this.init_SyncManager_DataUt = SyncManager.init_SyncManager_DataUt(this.init_FieldCache_BufferH.init_Str());
                  } catch (Exception resolveError) {
                     this.init_V_Connection_2(old, transferEpoch, resolveError);
                     this.init_V_Connection(old, transferEpoch, "Re-authentication failed: " + init_Str_Throwable(resolveError));
                     return;
                  }
               }

               synchronized (this.load_Obj) {
                  if (!this.load_Z_Connection_I(old, transferEpoch)) {
                     return;
                  }

                  this.init_Z = transferIntent;
               }

               this.init_V_InetSocketAddress(address, targetHost, targetPort, old, transferEpoch);
            } else {
               this.init_V_Connection_2(old, transferEpoch, error);
               this.init_V_Connection(old, transferEpoch, "Reconnect failed: " + targetHost + ":" + targetPort);
            }
         });
      }
   }

   private static InetSocketAddress init_InetSocketAddress_Str_I(String host, int port) {
      ServerAddress server = new ServerAddress(host, port);
      return ServerNameResolver.DEFAULT.resolveAddress(server).map(ResolvedServerAddress::asInetSocketAddress).orElse(null);
   }

   static String init_Str_Str_I(String host, int port) {
      if (host != null && !host.isBlank()) {
         String bracketed = host.indexOf(58) >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;
         return port == 25565 ? bracketed : bracketed + ":" + port;
      } else {
         return "";
      }
   }

   private void init_V_CustomMenuSnapshot_CustomMenuSnapshot(CustomMenuSnapshot before, CustomMenuSnapshot after) {
      if (after != null) {
         this.update_Str = after.title();
         this.queue_J++;
      } else if (before != null) {
         this.update_Str = this.close_I >= 0 ? this.init_Component.getString() : "";
      }
   }

   void init_V_Map(Map<String, String> values) {
      this.push_Map = values == null ? Map.of() : Map.copyOf(values);
      if (values != null && values.containsKey("password")) {
         this.node_Z = true;
      }
   }

   private void init_V_Component(Component content) {
      if (content != null && this.init_FieldCache_DataUti == FieldCache.Inner_DataUtil.load) {
         this.init_CachePool.init(content.getString());
      }
   }

   private boolean data_Z() {
      if (this.init_StreamUtil.init_CustomMenuSnapshot() == null) {
         return false;
      }

      TextFormat run = this.init_TextFormat;
      return run != null && run.write() ? true : !this.prev_Str().isEmpty();
   }

   private String prev_Str() {
      String p = this.push_Map.get("password");
      if (p != null && !p.isBlank()) {
         return p;
      }

      autismclient.util.BitSetUtil account = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(this.init_FieldCache_BufferH.init_Str());
      if (account != null && account.flush != null && !account.flush.isBlank()) {
         return account.flush;
      }

      try {
         String global = aA.init_Map().get("password");
         if (global != null && !global.isBlank()) {
            return global;
         }
      } catch (RuntimeException var4) {
      }

      return "";
   }

   private void init_V_CustomMenuSnapshot(CustomMenuSnapshot snapshot) {
      if (snapshot != null && !this.node_Z) {
         long started = this.drain;
         if (started != 0L && System.currentTimeMillis() - started <= 10000L) {
            boolean hasTextInput = false;

            for (CustomMenuInput input : snapshot.inputs()) {
               if (input.kind() == CustomMenuInput.Kind.TEXT) {
                  hasTextInput = true;
                  break;
               }
            }

            if (hasTextInput && !this.push_Map.containsKey("password")) {
               this.node_Z = true;
               this.init_ClassCache_TokenSt.load(this, snapshot.title());
            }
         }
      }
   }

   private void put_V() {
      SyncManager.Inner_DataUtil id = this.init_SyncManager_DataUt;
      if (id != null && !this.load_Z) {
         SignatureValidator validator = id.init_SignatureValidator();
         if (validator != null) {
            id.init_ProfileKeyPairMana().prepareKeyPair().whenComplete((pair, error) -> {
               if (!this.load_AtomicBoolean.get() && !this.load_Z && error == null && pair != null && !pair.isEmpty()) {
                  LocalChatSession created = LocalChatSession.create(pair.get());
                  Data data = created.asRemote().asData();
                  UUID signAs = this.init_UUID != null ? this.init_UUID : id.init_User().getProfileId();

                  try {
                     data.validate(new GameProfile(signAs, id.init_User().getName()), validator);
                  } catch (ValidationException invalid) {
                     return;
                  }

                  if (!this.load_AtomicBoolean.get() && this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush) {
                     synchronized (this.update_Obj) {
                        this.init_LocalChatSession = created;
                        this.init_SignedMessageChain = created.createMessageEncoder(signAs);
                     }

                     this.init_Z_Packet(new ServerboundChatSessionUpdatePacket(data), true, false);
                  }
               }
            });
         }
      }
   }

   private void add_V() {
      synchronized (this.update_Obj) {
         if (this.init_LocalChatSession != null && this.init_UUID != null) {
            this.init_SignedMessageChain = this.init_LocalChatSession.createMessageEncoder(this.init_UUID);
         }
      }
   }

   int write_I_Str(String command) {
      if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && command != null) {
         int id = this.init_AtomicInteger.updateAndGet(value -> value == 2147483647 ? 1 : value + 1);
         return this.init_Z_Packet(new ServerboundCommandSuggestionPacket(id, command), true, false) ? id : -1;
      } else {
         return -1;
      }
   }

   ClassCache.Inner_TaskTimer init_ClassCache_TaskTim_I(int requestId) {
      synchronized (this.flush_Obj) {
         return this.close_Map.get(requestId);
      }
   }

   private void init_V_ClientboundSetObje(ClientboundSetObjectivePacket packet) {
      String name = packet.getObjectiveName();
      if (name != null && !name.isBlank()) {
         synchronized (this.init_Obj) {
            if (packet.getMethod() == 1) {
               this.read_Map.remove(name);
               this.write_Map.remove(name);
               this.update_Map.values().removeIf(name::equals);
            } else {
               String title = packet.getDisplayName() == null ? name : packet.getDisplayName().getString();
               if (this.read_Map.size() >= 64 && !this.read_Map.containsKey(name)) {
                  String oldest = this.read_Map.keySet().iterator().next();
                  this.read_Map.remove(oldest);
                  this.write_Map.remove(oldest);
                  this.update_Map.values().removeIf(oldest::equals);
               }

               this.read_Map.put(name, new ClassCache.Inner_BitSetUtil(title, packet.getNumberFormat()));
            }
         }
      }
   }

   private void init_V_ClientboundSetDisp(ClientboundSetDisplayObjectivePacket packet) {
      DisplaySlot slot = packet.getSlot();
      if (slot != null) {
         String objective = packet.getObjectiveName();
         synchronized (this.init_Obj) {
            if (objective != null && !objective.isBlank()) {
               this.update_Map.put(slot, objective);
            } else {
               this.update_Map.remove(slot);
            }
         }
      }
   }

   private void init_V_ClientboundSetScor(ClientboundSetScorePacket packet) {
      String objective = packet.objectiveName();
      String owner = packet.owner();
      if (objective != null && !objective.isBlank() && owner != null && !owner.isBlank()) {
         String display = packet.display().map(Component::getString).orElse(owner);
         synchronized (this.init_Obj) {
            if (this.write_Map.size() >= 64 && !this.write_Map.containsKey(objective)) {
               String oldest = this.write_Map.keySet().iterator().next();
               this.write_Map.remove(oldest);
               this.read_Map.remove(oldest);
               this.update_Map.values().removeIf(oldest::equals);
            }

            Map<String, ClassCache.Inner_RefQueue> scores = this.write_Map.computeIfAbsent(objective, ignored -> new LinkedHashMap<>());
            if (scores.size() >= 2048 && !scores.containsKey(owner)) {
               scores.remove(scores.keySet().iterator().next());
            }

            scores.put(owner, new ClassCache.Inner_RefQueue(packet.score(), display, packet.numberFormat()));
         }
      }
   }

   private void init_V_ClientboundSetPlay(ClientboundSetPlayerTeamPacket packet) {
      String teamName = packet.getName();
      if (teamName != null && !teamName.isBlank()) {
         synchronized (this.init_Obj) {
            if (packet.getTeamAction() == net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.REMOVE) {
               this.sync_Map.remove(teamName);
               this.flush_Map.entrySet().removeIf(entry -> teamName.equals(entry.getValue()));
            } else {
               packet.getParameters()
                  .ifPresent(
                     parameters -> {
                        if (this.sync_Map.size() >= 256 && !this.sync_Map.containsKey(teamName)) {
                           String oldest = this.sync_Map.keySet().iterator().next();
                           this.sync_Map.remove(oldest);
                           this.flush_Map.entrySet().removeIf(entry -> oldest.equals(entry.getValue()));
                        }

                        this.sync_Map
                           .put(
                              teamName,
                              new ClassCache.Inner_CharEncoder(
                                 init_Str_Component(parameters.playerPrefix()), init_Str_Component(parameters.playerSuffix()), parameters.color()
                              )
                           );
                     }
                  );
               net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action playerAction = packet.getPlayerAction();
               if (playerAction == net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.ADD) {
                  for (String owner : packet.getPlayers()) {
                     if (owner != null && !owner.isBlank() && (this.flush_Map.size() < 4096 || this.flush_Map.containsKey(owner))) {
                        this.flush_Map.put(owner, teamName);
                     }
                  }
               } else if (playerAction == net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.Action.REMOVE) {
                  for (String owner : packet.getPlayers()) {
                     this.flush_Map.remove(owner, teamName);
                  }
               }
            }
         }
      }
   }

   private static String init_Str_Component(Component component) {
      return component == null ? "" : component.getString();
   }

   private void init_V_ClientboundResetSc(ClientboundResetScorePacket packet) {
      String owner = packet.owner();
      String objective = packet.objectiveName();
      synchronized (this.init_Obj) {
         if (objective != null && !objective.isBlank()) {
            Map<String, ClassCache.Inner_RefQueue> scores = this.write_Map.get(objective);
            if (scores != null) {
               scores.remove(owner);
            }
         } else {
            for (Map<String, ClassCache.Inner_RefQueue> scores : this.write_Map.values()) {
               scores.remove(owner);
            }
         }
      }
   }

   private void clear_V() {
      synchronized (this.init_Obj) {
         this.read_Map.clear();
         this.write_Map.clear();
         this.update_Map.clear();
         this.sync_Map.clear();
         this.flush_Map.clear();
      }
   }

   public String read_Str_Str(String line) {
      String value = line == null ? "" : line.trim();
      if (value.isEmpty()) {
         return "Empty input";
      } else if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      } else if (!this.cache_Z()) {
         return "Rate limited";
      } else {
         return value.startsWith("/") ? this.sync_Str_Str(value.substring(1)) : this.update_Str_Str(value);
      }
   }

   private String update_Str_Str(String content) {
      if (content.length() > 256) {
         return "Chat exceeds 256 characters";
      }

      ServerboundChatPacket packet;
      synchronized (this.update_Obj) {
         Instant now = Instant.now();
         long salt = SaltSupplier.getLong();
         Update update = this.init_LastSeenMessagesTr.generateAndApplyUpdate();
         MessageSignature signature = this.init_SignedMessageChain.pack(new SignedMessageBody(content, now, salt, update.lastSeen()));
         packet = new ServerboundChatPacket(content, now, salt, Optional.ofNullable(signature), update.update());
      }

      return this.init_Z_Packet(packet, false, false) ? "Sent" : "Blocked by packet policy";
   }

   private String sync_Str_Str(String command) {
      if (command.isBlank()) {
         return "Empty command";
      }

      if (!this.clear_Z) {
         return this.init_Z_Packet(new ServerboundChatCommandPacket(command), false, false) ? "Sent" : "Blocked by packet policy";
      }

      SignableCommand<Object> signable = SignableCommand.of(this.init_CommandDispatcher.parse(command, this.write_Obj));
      if (signable.arguments().isEmpty()) {
         return this.init_Z_Packet(new ServerboundChatCommandPacket(command), false, false) ? "Sent" : "Blocked by packet policy";
      }

      ServerboundChatCommandSignedPacket packet;
      synchronized (this.update_Obj) {
         if (this.init_LocalChatSession == null) {
            return this.init_Z_Packet(new ServerboundChatCommandPacket(command), false, false) ? "Sent" : "Blocked by packet policy";
         }

         Instant now = Instant.now();
         long salt = SaltSupplier.getLong();
         Update update = this.init_LastSeenMessagesTr.generateAndApplyUpdate();
         ArgumentSignatures signatures = ArgumentSignatures.signCommand(
            signable, argument -> this.init_SignedMessageChain.pack(new SignedMessageBody(argument, now, salt, update.lastSeen()))
         );
         packet = new ServerboundChatCommandSignedPacket(command, now, salt, signatures, update.update());
      }

      return this.init_Z_Packet(packet, false, false) ? "Sent" : "Blocked by packet policy";
   }

   public String init_Str_Class_Str(Class<? extends Packet<?>> packetClass, String arguments) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      } else if (!HashUtil.init(packetClass)) {
         return "Packet is not headless-safe";
      } else if (!this.cache_Z()) {
         return "Rate limited";
      } else {
         bm.Inner_StreamUtil result = bm.init_bm_StreamUtil_Class_Str(packetClass, arguments);
         if (result.init_Z() && result.init_Packet() != null) {
            return this.init_Z_Packet(result.init_Packet(), false, result.init_Packet() instanceof ServerboundMovePlayerPacket)
               ? "Sent"
               : "Blocked by packet policy";
         } else {
            return result.init_Str();
         }
      }
   }

   private String valid_Str() {
      return this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush ? "Session is not ready" : "";
   }

   public String init_Str_I(int handlerSlot, int button, ContainerInput input) {
      return this.init_Str_I(handlerSlot, button, input, false, 1);
   }

   private String init_Str_I(int handlerSlot, int button, ContainerInput input, boolean allowSynthetic) {
      return this.init_Str_I(handlerSlot, button, input, allowSynthetic, 1);
   }

   private String init_Str_I(int handlerSlot, int button, ContainerInput input, boolean allowSynthetic, int repetitions) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      if (input == null) {
         return "Invalid click";
      }

      if (repetitions >= 1 && repetitions <= 100000) {
         synchronized (this.push_Obj) {
            label113: {
               if (this.start_Z
                     && !this.state_Z
                     && this.init_ClassCache_TextFor != ClassCache.Inner_TextFormat.init
                     && (this.init_ClassCache_TextFor != ClassCache.Inner_TextFormat.write || allowSynthetic)
                  || allowSynthetic && this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write && !this.state_Z) {
                  int limit = this.close_I < 0 ? 46 : this.init_List.size();
                  if (handlerSlot == -999 || handlerSlot >= 0 && handlerSlot < limit) {
                     if (this.write_ArrayDeque.size() + (this.init_ClassCache_TableIn == null ? 0 : 1) >= 128) {
                        return "Inventory click queue is full";
                     }

                     ClassCache.Inner_CachePool ticket = new ClassCache.Inner_CachePool(++this.count_J, this.buffer_J);
                     this.write_ArrayDeque
                        .addLast(
                           new ClassCache.Inner_TableIndex(ticket, this.buffer_J, this.tick_I(), handlerSlot, button, input, allowSynthetic, null, repetitions)
                        );
                     this.block_J++;
                     break label113;
                  }

                  return "Invalid slot";
               }

               return "Inventory is synchronizing";
            }
         }

         this.update_V_J(System.currentTimeMillis());
         return repetitions == 1 ? "Sent" : "Queued " + repetitions + " clicks";
      } else {
         return "Click count must be 1-100000";
      }
   }

   private String init_Str_Packet(Packet<?> packet) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      if (packet == null) {
         return "Invalid packet";
      }

      synchronized (this.push_Obj) {
         if (!this.start_Z
            || this.state_Z
            || this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.init
            || this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write) {
            return "Inventory is synchronizing";
         }

         if (this.write_ArrayDeque.size() + (this.init_ClassCache_TableIn == null ? 0 : 1) >= 128) {
            return "Inventory click queue is full";
         }

         ClassCache.Inner_CachePool ticket = new ClassCache.Inner_CachePool(++this.count_J, this.buffer_J);
         this.write_ArrayDeque.addLast(new ClassCache.Inner_TableIndex(ticket, this.buffer_J, this.tick_I(), -1, 0, ContainerInput.PICKUP, false, packet, 1));
         this.block_J++;
      }

      this.update_V_J(System.currentTimeMillis());
      return "Sent";
   }

   public String init_Str_I(int buttonId) {
      return this.init_Str_Packet(new ServerboundContainerButtonClickPacket(this.tick_I(), buttonId));
   }

   public String load_Str_I(int index) {
      return this.init_Str_Packet(new ServerboundSelectTradePacket(index));
   }

   public String init_Str_I_I(int primaryId, int secondaryId) {
      return this.init_Str_Packet(new ServerboundSetBeaconPacket(init_Optional_I(primaryId), init_Optional_I(secondaryId)));
   }

   private static Optional<Holder<MobEffect>> init_Optional_I(int id) {
      return id < 0 ? Optional.empty() : BuiltInRegistries.MOB_EFFECT.get(id).map(h -> (Holder<MobEffect>)h);
   }

   public String init_Str_RecipeDisplayId_Z(RecipeDisplayId id, boolean placeAll) {
      return this.init_Str_Packet(new ServerboundPlaceRecipePacket(this.tick_I(), id, placeAll));
   }

   public String write_Str_Str(String name) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      String clamped = name == null ? "" : (name.length() > 50 ? name.substring(0, 50) : name);
      return this.init_Z_Packet(new ServerboundRenameItemPacket(clamped), false, false) ? "Sent" : "Blocked by packet policy";
   }

   private boolean init_Z_I(int handlerSlot, int button, ContainerInput input) {
      return "Sent".equals(this.init_Str_I(handlerSlot, button, input, false));
   }

   private boolean load(int handlerSlot, int button, ContainerInput input) {
      return "Sent".equals(this.init_Str_I(handlerSlot, button, input, true));
   }

   private boolean value_Z() {
      synchronized (this.push_Obj) {
         return this.init_ClassCache_TableIn != null || !this.write_ArrayDeque.isEmpty();
      }
   }

   @Override
   public EventBus.Inner_DataUtil init_EventBus_DataUtil() {
      synchronized (this.push_Obj) {
         if (this.state_Z) {
            return EventBus.Inner_DataUtil.read;
         } else {
            return this.init_ClassCache_TextFor != ClassCache.Inner_TextFormat.init && this.init_ClassCache_TableIn == null && this.write_ArrayDeque.isEmpty()
               ? EventBus.Inner_DataUtil.init
               : EventBus.Inner_DataUtil.load;
         }
      }
   }

   private void update_V_J(long now) {
      while (true) {
         Packet<?> packet;
         synchronized (this.push_Obj) {
            if (this.init_ClassCache_TableIn != null) {
               boolean settled = this.cache_Z && now - this.level_J >= 50L;
               boolean noOp = !this.cache_Z && now - this.flags_J >= load_J_I(this.load_I);
               boolean overdue = now - this.flags_J >= init_J_I(this.load_I);
               if (!settled && !noOp && !overdue) {
                  return;
               }

               ClassCache.Inner_TableIndex completed = this.init_ClassCache_TableIn;
               if (completed.write() > 1) {
                  completed.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.init;
                  this.write_ArrayDeque
                     .addFirst(
                        new ClassCache.Inner_TableIndex(
                           completed.init_ClassCache_CachePo(),
                           completed.init_J(),
                           completed.init_I(),
                           completed.load(),
                           completed.read(),
                           completed.init_ContainerInput(),
                           completed.init_Z(),
                           completed.init_Packet(),
                           completed.write() - 1
                        )
                     );
               } else {
                  completed.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.write;
               }

               this.init_ClassCache_TableIn = null;
               this.cache_Z = false;
               this.block_J++;
            }

            ClassCache.Inner_TableIndex sending = this.write_ArrayDeque.pollFirst();
            if (sending == null) {
               return;
            }

            if (sending.init_J() != this.buffer_J
               || sending.init_I() != this.tick_I()
               || this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.init
               || !this.start_Z && (!sending.init_Z() || this.init_ClassCache_TextFor != ClassCache.Inner_TextFormat.write)
               || this.init_ClassCache_TextFor == ClassCache.Inner_TextFormat.write && !sending.init_Z()) {
               sending.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.update;
               continue;
            }

            if (sending.init_Packet() == null && this.init_Z_ClassCache_TableIn(sending)) {
               sending.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.write;
               this.block_J++;
               continue;
            }

            if (sending.init_Packet() != null) {
               packet = sending.init_Packet();
            } else {
               int stateId = this.close_I < 0 ? this.poll_I : this.open_I;
               packet = new ServerboundContainerClickPacket(
                  sending.init_I(),
                  stateId,
                  (short)sending.load(),
                  (byte)sending.read(),
                  sending.init_ContainerInput(),
                  new Int2ObjectOpenHashMap<>(),
                  init_HashedStack_ItemStack_HashedPatchMap_Has(this.init_ItemStack, this.init_HashedPatchMap_Has())
               );
            }

            this.init_ClassCache_TableIn = sending;
            sending.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.load;
            this.flags_J = now;
            this.level_J = now;
            this.cache_Z = sending.init_ContainerInput() == ContainerInput.QUICK_CRAFT;
            if (this.cache_Z) {
               sending.init_ClassCache_CachePo().init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.read;
            }
         }

         if (this.init_Z_Packet(packet, false, false)) {
            return;
         }

         synchronized (this.push_Obj) {
            this.init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.flush);
            this.block_J++;
            return;
         }
      }
   }

   static long init_J_I(int pingMs) {
      return Math.max(1000L, Math.min(5000L, Math.max(0, pingMs) * 4L + 500L));
   }

   static long load_J_I(int pingMs) {
      return Math.max(200L, Math.min(init_J_I(pingMs), Math.max(0, pingMs) * 3L + 200L));
   }

   private void reset_V() {
      ClassCache.Inner_NetHelper close;
      int containerId;
      synchronized (this.push_Obj) {
         if (this.init_ClassCache_NetHelp == ClassCache.Inner_NetHelper.init || this.init_ClassCache_TableIn != null || !this.write_ArrayDeque.isEmpty()) {
            return;
         }

         close = this.init_ClassCache_NetHelp;
         this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init;
         containerId = this.tick_I();
      }

      if (close == ClassCache.Inner_NetHelper.load) {
         this.tick_V();
      } else if (this.init_Z_Packet(new ServerboundContainerClosePacket(containerId), false, false)) {
         if (close == ClassCache.Inner_NetHelper.write) {
            this.run_V();
         } else {
            if (this.close_I >= 0) {
               this.pop_V();
            }

            this.init_V_Z_Z(false, true);
         }
      }
   }

   private boolean init_Z_ClassCache_TableIn(ClassCache.Inner_TableIndex click) {
      List<ItemStack> authoritativeSlots = this.close_I < 0 ? this.load_List : this.init_List;
      ItemStack slot = click.load() >= 0 && click.load() < authoritativeSlots.size()
         ? init_ItemStack_ItemStack(authoritativeSlots.get(click.load()))
         : ItemStack.EMPTY;
      boolean var10000;
      switch (click.init_ContainerInput()) {
         case PICKUP:
            var10000 = slot.isEmpty() && init_ItemStack_ItemStack(this.init_ItemStack).isEmpty();
            break;
         case QUICK_MOVE:
         case THROW:
            var10000 = slot.isEmpty();
            break;
         case CLONE:
            var10000 = slot.isEmpty() || this.push_I != 1;
            break;
         case PICKUP_ALL:
            var10000 = this.load_Z_List(authoritativeSlots);
            break;
         case SWAP:
            var10000 = this.init_Z_ClassCache_TableIn_ItemStack(click, slot);
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private boolean load_Z_List(List<ItemStack> slots) {
      ItemStack cursor = init_ItemStack_ItemStack(this.init_ItemStack);
      if (!cursor.isEmpty() && cursor.getCount() < cursor.getMaxStackSize()) {
         for (ItemStack candidate : slots) {
            if (!init_ItemStack_ItemStack(candidate).isEmpty() && ItemStack.isSameItemSameComponents(cursor, candidate)) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private boolean init_Z_ClassCache_TableIn_ItemStack(ClassCache.Inner_TableIndex click, ItemStack slot) {
      int inventoryHandler = click.read() >= 0 && click.read() <= 8 ? 36 + click.read() : (click.read() == 40 ? 45 : -1);
      if (inventoryHandler >= 0 && inventoryHandler < this.load_List.size()) {
         return this.close_I < 0 && click.load() == inventoryHandler
            ? true
            : slot.isEmpty() && init_ItemStack_ItemStack(this.load_List.get(inventoryHandler)).isEmpty();
      } else {
         return false;
      }
   }

   private static HashedStack init_HashedStack_ItemStack_HashedPatchMap_Has(ItemStack stack, HashGenerator hasher) {
      if (stack == null || stack.isEmpty()) {
         return HashedStack.EMPTY;
      }

      if (hasher == null) {
         return HashedStack.EMPTY;
      }

      try {
         return HashedStack.create(stack, hasher);
      } catch (RuntimeException ignored) {
         return HashedStack.EMPTY;
      }
   }

   private HashGenerator init_HashedPatchMap_Has() {
      Frozen reg = this.init_RegistryAccess_Fro;
      if (reg == null) {
         return null;
      }

      HashGenerator cached = this.init_HashedPatchMap_Has;
      if (cached != null && this.load_RegistryAccess_Fro == reg) {
         return cached;
      }

      RegistryOps<HashCode> ops = reg.createSerializationContext(HashOps.CRC32C_INSTANCE);
      HashGenerator built = component -> component.encodeValue(ops)
         .getOrThrow(msg -> new IllegalArgumentException("Failed to hash " + component + ": " + msg))
         .asInt();
      this.init_HashedPatchMap_Has = built;
      this.load_RegistryAccess_Fro = reg;
      return built;
   }

   public int load_I_I(int visible) {
      if (this.close_I < 0) {
         if (visible >= 100 && visible <= 104) {
            return visible - 100;
         } else if (visible >= 0 && visible <= 8) {
            return 36 + visible;
         } else if (visible >= 9 && visible <= 35) {
            return visible;
         } else if (visible >= 36 && visible <= 39) {
            return 44 - visible;
         } else {
            return visible == 40 ? 45 : -1;
         }
      } else {
         int size;
         synchronized (this.push_Obj) {
            size = this.init_List.size();
         }

         int base = size - 36;
         if (visible >= 100) {
            int gui = visible - 100;
            return gui < base ? gui : -1;
         } else if (visible >= 0 && visible <= 8) {
            return base + 27 + visible;
         } else {
            return visible >= 9 && visible <= 35 ? base + (visible - 9) : -1;
         }
      }
   }

   public String tick_Str() {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      if (!this.cache_Z()) {
         return "Rate limited";
      }

      int dropped = 0;
      synchronized (this.push_Obj) {
         int size = this.init_List.size();
         int from = this.close_I < 0 ? 9 : Math.max(0, size - 36);
         int to = this.close_I < 0 ? Math.min(45, size) : size;

         for (int handler = from; handler < to; handler++) {
            ItemStack stack = this.init_List.get(handler);
            if (stack != null && !stack.isEmpty() && this.load(handler, 1, ContainerInput.THROW)) {
               dropped++;
            }
         }
      }

      return dropped > 0 ? "Sent" : "Nothing to drop";
   }

   public String read_Str_I(int slot0to8) {
      String pre = this.valid_Str();
      if (!pre.isBlank()) {
         return pre;
      }

      int slot = Math.max(0, Math.min(8, slot0to8));
      boolean ok = this.init_Z_Packet(new ServerboundSetCarriedItemPacket(slot), false, false);
      if (ok) {
         this.add_I = slot;
         this.clear_I = slot;
         this.push_V();
      }

      return ok ? "Sent" : "Blocked by packet policy";
   }

   public String init_Str_ItemStack(ItemStack stack) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      } else if (this.push_I != 1) {
         return "Creative mode required";
      } else if (stack != null && !stack.isEmpty()) {
         int handler = 36 + Math.max(0, Math.min(8, this.add_I));
         return this.init_Z_Packet(new ServerboundSetCreativeModeSlotPacket((short)handler, stack.copy()), false, false) ? "Sent" : "Blocked by packet policy";
      } else {
         return "Invalid item";
      }
   }

   public String write_Str_I(int targetHandlerSlot) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      if (this.close_I >= 0) {
         return "Close the bot container first";
      }

      int source = 36 + Math.max(0, Math.min(8, this.add_I));
      synchronized (this.push_Obj) {
         if (!this.reset_Z || !this.start_Z || this.state_Z) {
            return "Inventory is synchronizing";
         }

         if (source >= this.load_List.size() || init_ItemStack_ItemStack(this.load_List.get(source)).isEmpty()) {
            return "Hold an item first";
         }

         if (!init_ItemStack_ItemStack(this.init_ItemStack).isEmpty()) {
            return "Bot cursor is not empty";
         }

         if (targetHandlerSlot != -2147483648) {
            if (targetHandlerSlot < 0 || targetHandlerSlot >= this.load_List.size()) {
               return "Invalid XCarry slot";
            }

            if (!init_ItemStack_ItemStack(this.load_List.get(targetHandlerSlot)).isEmpty()) {
               return "XCarry slot is occupied";
            }
         }

         this.queue_Z = true;
         this.buffer_Z = true;
      }

      String pickup = this.init_Str_I(source, 0, ContainerInput.PICKUP, false);
      if ("Sent".equals(pickup) && targetHandlerSlot != -2147483648) {
         String place = this.init_Str_I(targetHandlerSlot, 0, ContainerInput.PICKUP, false);
         return "Sent".equals(place) ? "Sent" : place;
      } else {
         return pickup;
      }
   }

   public String init_Str_Z(boolean all) {
      String pre = this.valid_Str();
      if (!pre.isBlank()) {
         return pre;
      }

      net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action action = all
         ? net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
         : net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ITEM;
      boolean ok = this.init_Z_Packet(new ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN), false, false);
      return ok ? "Sent" : "Blocked by packet policy";
   }

   public String run_Str() {
      String pre = this.valid_Str();
      if (!pre.isBlank()) {
         return pre;
      }

      synchronized (this.push_Obj) {
         if (this.init_ClassCache_TableIn != null || !this.write_ArrayDeque.isEmpty()) {
            this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.read;
            return "Sent";
         }
      }

      boolean ok = this.init_Z_Packet(new ServerboundContainerClosePacket(this.tick_I()), false, false);
      if (ok) {
         if (this.close_I >= 0) {
            this.pop_V();
         }

         this.init_V_Z_Z(false, true);
      }

      return ok ? "Sent" : "Blocked by packet policy";
   }

   public String get_Str() {
      String pre = this.valid_Str();
      if (!pre.isBlank()) {
         return pre;
      }

      PositionMoveRotation current = this.init_PositionMoveRotati;
      return this.init_Z_Packet(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, ++this.reset_I, current.yRot(), current.xRot()), false, false)
         ? "Sent"
         : "Blocked by packet policy";
   }

   public String set_Str() {
      String pre = this.valid_Str();
      if (!pre.isBlank()) {
         return pre;
      } else {
         return this.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND), false, false) ? "Sent" : "Blocked by packet policy";
      }
   }

   public int update_I_Str(String query) {
      if (query != null && !query.isBlank()) {
         String needle = query.trim().toLowerCase(Locale.ROOT);
         synchronized (this.push_Obj) {
            for (int i = 0; i < this.init_List.size(); i++) {
               ItemStack stack = this.init_List.get(i);
               if (stack != null && !stack.isEmpty()) {
                  String hover = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                  Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                  if (hover.contains(needle)) {
                     return i;
                  }

                  if (id != null && (id.getPath().equals(needle) || id.toString().equals(needle))) {
                     return i;
                  }
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   @Override
   public List<int[]> init_List_MapStore(autismclient.util.macro.MapStore a) {
      List<int[]> plan = new ArrayList<>();
      if (a == null) {
         return plan;
      }

      synchronized (this.push_Obj) {
         int size = this.init_List.size();
         if (size == 0) {
            return plan;
         }

         List<autismclient.util.macro.ArrayUtil> targets = !a.itemTargets.isEmpty() ? a.itemTargets : load_List_List(a.itemNames);
         if (!targets.isEmpty()) {
            for (int ei = 0; ei < targets.size() && plan.size() < 256; ei++) {
               autismclient.util.macro.ArrayUtil target = targets.get(ei);
               if (target != null && (target.update() || target.init_Z())) {
                  N action = a.init_N_I(ei);
                  int button = a.read_I(ei);
                  if (action != N.load_N && action.load()) {
                     button = action.init_I();
                  }

                  int slot;
                  if (action == N.flush && a.useCursorItemForPickupAll) {
                     slot = this.sync_I_I(size);
                  } else {
                     slot = this.init_I_MapStore(a, target, ei, size);
                  }

                  if (slot >= 0) {
                     init_V_List_2(plan, slot, button, action, init_I_I_N(a.init_I_I_3(ei), action));
                  }
               }
            }
         } else if (a.useSlot && a.targetSlot >= 0) {
            N action = a.init_N();
            int button = a.init_I_2();
            if (action != N.load_N && action.load()) {
               button = action.init_I();
            }

            int slot = action == N.flush && a.useCursorItemForPickupAll ? this.sync_I_I(size) : this.load_I_I(a.targetSlot);
            if (slot >= 0 && slot < size) {
               init_V_List_2(plan, slot, button, action, init_I_I_N(a.times, action));
            }
         }

         return plan;
      }
   }

   @Override
   public void init_V_I(int handlerSlot, int button, int containerInputOrdinal) {
      ContainerInput[] all = ContainerInput.values();
      if (containerInputOrdinal >= 0 && containerInputOrdinal < all.length) {
         this.init_Str_I(handlerSlot, button, all[containerInputOrdinal]);
      }
   }

   @Override
   public boolean init_Z_y(y action) {
      if (action != null && action.mode != null) {
         boolean var10000;
         switch (action.mode) {
            case init:
               var10000 = this.load(action.slot, action.button, init_ContainerInput_Str(action.containerInput));
               break;
            case load:
               if (action.entityId < 0) {
                  var10000 = false;
               } else {
                  this.run_I = action.entityId;
                  this.load_BlockPos = null;
                  var10000 = this.init_Z_Packet(
                     new ServerboundInteractPacket(
                        action.entityId, "OFF_HAND".equalsIgnoreCase(action.hand) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, Vec3.ZERO, false
                     ),
                     false,
                     false
                  );
               }
               break;
            case write:
               var10000 = this.init_Z_Packet(
                  new ServerboundSelectBundleItemPacket(action.slot >= 0 ? action.slot : 36 + this.add_I, action.bundleIndex), false, false
               );
               break;
            case update:
               InteractionHand hand = "OFF_HAND".equalsIgnoreCase(action.hand) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
               PositionMoveRotation current = this.init_PositionMoveRotati;
               var10000 = this.init_Z_Packet(new ServerboundUseItemPacket(hand, ++this.reset_I, current.yRot(), current.xRot()), false, false);
               break;
            case sync:
               var10000 = this.init_Z_Packet(
                  new ServerboundPlayerActionPacket(
                     net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
                  ),
                  false,
                  false
               );
               break;
            case flush:
               var10000 = "Sent".equals(this.read_Str_I(Math.max(0, Math.min(8, action.carriedSlot))));
               break;
            case close:
               var10000 = this.init_Z_Packet(new ServerboundClientInformationPacket(this.init_ClientInformation), false, false);
               break;
            case open:
               var10000 = this.update_Z_I(action.containerId);
               break;
            case read:
               var10000 = false;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   private boolean update_Z_I(int containerId) {
      boolean closesTrackedContainer = containerId == this.tick_I();
      if (closesTrackedContainer) {
         synchronized (this.push_Obj) {
            if (this.init_ClassCache_TableIn != null || !this.write_ArrayDeque.isEmpty()) {
               this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.read;
               return true;
            }
         }
      }

      boolean sent = this.init_Z_Packet(new ServerboundContainerClosePacket(containerId), false, false);
      if (sent && closesTrackedContainer) {
         if (this.close_I >= 0 && this.init_ClassCache_TextFor != ClassCache.Inner_TextFormat.write) {
            this.pop_V();
         }

         this.init_V_Z_Z(false, true);
      }

      return sent;
   }

   private static ContainerInput init_ContainerInput_Str(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return ContainerInput.valueOf(value.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return ContainerInput.PICKUP;
         }
      } else {
         return ContainerInput.PICKUP;
      }
   }

   @Override
   public int init_I_List(List<String> pages, String title, boolean sign, boolean requireHeld, int excludedHotbarMask) {
      if (pages != null && !pages.isEmpty()) {
         int hotbar = -1;
         synchronized (this.push_Obj) {
            int selected = Math.max(0, Math.min(8, this.add_I));
            if (requireHeld) {
               int handler = 36 + selected;
               if ((excludedHotbarMask & 1 << selected) != 0 || handler >= this.load_List.size() || !init_Z_ItemStack(this.load_List.get(handler))) {
                  return -1;
               }

               hotbar = selected;
            } else {
               int bookHandler = -1;

               for (int h = 36; h <= 44 && h < this.load_List.size(); h++) {
                  int slot = h - 36;
                  if ((excludedHotbarMask & 1 << slot) == 0 && init_Z_ItemStack(this.load_List.get(h))) {
                     bookHandler = h;
                     break;
                  }
               }

               if (bookHandler >= 0) {
                  hotbar = bookHandler - 36;
               } else {
                  for (int h = 9; h <= 35 && h < this.load_List.size(); h++) {
                     if (init_Z_ItemStack(this.load_List.get(h))) {
                        hotbar = this.update_I_I(excludedHotbarMask);
                        if (hotbar < 0) {
                           return -1;
                        }

                        int liveHandler = this.load_I_I(h);
                        if (liveHandler >= 0 && this.load(liveHandler, hotbar, ContainerInput.SWAP)) {
                           return -2;
                        }

                        return -1;
                     }
                  }

                  if (bookHandler < 0) {
                     return -1;
                  }
               }
            }
         }

         if (hotbar < 0) {
            return -1;
         }

         List<String> bounded = pages.stream().limit(100L).map(page -> page == null ? "" : page).toList();
         Optional<String> signedTitle = sign ? Optional.of(PacketUtil.update(title, 32)) : Optional.empty();
         return this.init_Z_Packet(new ServerboundEditBookPacket(hotbar, bounded, signedTitle), false, false) ? hotbar : -1;
      } else {
         return -1;
      }
   }

   private int update_I_I(int excludedMask) {
      for (int slot = 0; slot < 9; slot++) {
         if ((excludedMask & 1 << slot) == 0 && this.load_List.get(36 + slot).isEmpty()) {
            return slot;
         }
      }

      for (int slot = 0; slot < 9; slot++) {
         if ((excludedMask & 1 << slot) == 0) {
            return slot;
         }
      }

      return -1;
   }

   private static boolean init_Z_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         return id != null && "minecraft:writable_book".equals(id.toString());
      } else {
         return false;
      }
   }

   private int sync_I_I(int size) {
      if (this.init_ItemStack != null && !this.init_ItemStack.isEmpty()) {
         for (int h = 0; h < size; h++) {
            ItemStack stack = this.init_List.get(h);
            if (stack == null || stack.isEmpty()) {
               return h;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static int init_I_I_N(int raw, N action) {
      return action == N.open ? 1 : Math.max(1, Math.min(64, raw));
   }

   private static void init_V_List_2(List<int[]> plan, int slot, int button, N action, int count) {
      ContainerInput input;
      try {
         input = action.init_ContainerInput();
      } catch (RuntimeException ignored) {
         return;
      }

      int ord = input.ordinal();

      for (int i = 0; i < count && plan.size() < 256; i++) {
         plan.add(new int[]{slot, button, ord});
      }
   }

   private static List<autismclient.util.macro.ArrayUtil> load_List_List(List<String> names) {
      List<autismclient.util.macro.ArrayUtil> out = new ArrayList<>();
      if (names == null) {
         return out;
      }

      for (String n : names) {
         autismclient.util.macro.ArrayUtil t = autismclient.util.macro.ArrayUtil.update(n);
         if (t.init_Z() || t.update()) {
            out.add(t);
         }
      }

      return out;
   }

   private int init_I_MapStore(autismclient.util.macro.MapStore a, autismclient.util.macro.ArrayUtil target, int ei, int size) {
      int configuredSlot = target.init_Z() ? target.init_I : -1;
      if (configuredSlot >= 0 && target.update()) {
         return this.init_I_ArrayUtil(target, this.load_I_I(configuredSlot), size);
      }

      if (configuredSlot < 0) {
         return a.useSlot && a.targetSlot >= 0
            ? this.init_I_ArrayUtil(target, this.load_I_I(a.targetSlot), size)
            : this.init_I_ArrayUtil(target, a.init_MapStore_DataUtil_I(ei), a.init_MapStore_CachePool_I(ei), size);
      }

      int h = this.load_I_I(configuredSlot);
      return h >= 0 && h < size ? h : -1;
   }

   private int init_I_ArrayUtil(autismclient.util.macro.ArrayUtil target, int handler, int size) {
      if (handler >= 0 && handler < size) {
         ItemStack stack = this.init_List.get(handler);
         if (stack == null || stack.isEmpty()) {
            return -1;
         } else if (!target.update()) {
            return handler;
         } else {
            return target.init_Z_ItemStack_I(stack, this.load_I_I_I(handler, size)) ? handler : -1;
         }
      } else {
         return -1;
      }
   }

   private int init_I_ArrayUtil(
      autismclient.util.macro.ArrayUtil target,
      autismclient.util.macro.MapStore.Inner_DataUtil scope,
      autismclient.util.macro.MapStore.Inner_CachePool mode,
      int size
   ) {
      if (!target.update() && !target.init_Z()) {
         return -1;
      }

      autismclient.util.macro.MapStore.Inner_CachePool m = mode == null ? autismclient.util.macro.MapStore.Inner_CachePool.init_MapStore_CachePool : mode;
      autismclient.util.macro.MapStore.Inner_DataUtil sc = scope == null ? autismclient.util.macro.MapStore.Inner_DataUtil.init_MapStore_DataUtil : scope;
      int base = this.close_I >= 0 ? size - 36 : -1;
      int bestSlot = -1;
      int bestScore = -1;
      int bestCount = m == autismclient.util.macro.MapStore.Inner_CachePool.load ? 2147483647 : -1;

      for (int h = 0; h < size; h++) {
         ItemStack stack = this.init_List.get(h);
         if (stack != null && !stack.isEmpty()) {
            boolean playerInv = base < 0 || h >= base;
            if ((sc != autismclient.util.macro.MapStore.Inner_DataUtil.load || base >= 0 && !playerInv)
               && (sc != autismclient.util.macro.MapStore.Inner_DataUtil.read || playerInv)) {
               int score = target.init_I_ItemStack_I(stack, this.load_I_I_I(h, size));
               if (score >= 0) {
                  int count = stack.getCount();
                  if (init_Z_MapStore_CachePool(m, score, count, bestScore, bestCount)) {
                     bestScore = score;
                     bestCount = count;
                     bestSlot = h;
                  }
               }
            }
         }
      }

      return bestScore >= 0 ? bestSlot : -1;
   }

   private static boolean init_Z_MapStore_CachePool(autismclient.util.macro.MapStore.Inner_CachePool mode, int score, int count, int bestScore, int bestCount) {
      if (bestScore < 0) {
         return true;
      } else if (mode == autismclient.util.macro.MapStore.Inner_CachePool.load) {
         return count != bestCount ? count < bestCount : score > bestScore;
      } else if (mode == autismclient.util.macro.MapStore.Inner_CachePool.read) {
         return count != bestCount ? count > bestCount : score > bestScore;
      } else {
         return score > bestScore;
      }
   }

   public int read_I_I(int handler) {
      if (handler < 0) {
         return -1;
      }

      int size;
      synchronized (this.push_Obj) {
         size = this.init_List.size();
      }

      return this.load_I_I_I(handler, size);
   }

   private int load_I_I_I(int handler, int size) {
      if (this.close_I < 0) {
         if (handler >= 0 && handler <= 4) {
            return 100 + handler;
         } else if (handler >= 36 && handler <= 44) {
            return handler - 36;
         } else if (handler >= 9 && handler <= 35) {
            return handler;
         } else if (handler >= 5 && handler <= 8) {
            return 44 - handler;
         } else {
            return handler == 45 ? 40 : handler;
         }
      } else {
         int base = size - 36;
         if (handler < base) {
            return 100 + handler;
         } else {
            return handler < base + 27 ? 9 + (handler - base) : handler - (base + 27);
         }
      }
   }

   @Override
   public List<int[]> init_List_ah(ah a) {
      List<int[]> plan = new ArrayList<>();
      if (a == null) {
         return plan;
      }

      synchronized (this.push_Obj) {
         if (this.close_I < 0) {
            return plan;
         }

         int size = this.init_List.size();
         int base = size - 36;
         if (base < 0) {
            return plan;
         }

         int quickMove = ContainerInput.QUICK_MOVE.ordinal();
         boolean loot = a.mode == ah.Inner_DataUtil.init;

         for (int h = 0; h < size && plan.size() < 256; h++) {
            ItemStack stack = this.init_List.get(h);
            if (stack != null && !stack.isEmpty()) {
               boolean playerInv = h >= base;
               if ((!loot || !playerInv) && (loot || playerInv) && init_Z_ah(a, stack, this.load_I_I_I(h, size))) {
                  plan.add(new int[]{h, 0, quickMove});
               }
            }
         }

         return plan;
      }
   }

   private static boolean init_Z_ah(ah a, ItemStack stack, int visibleSlot) {
      if (a.allItems) {
         return true;
      }

      List<autismclient.util.macro.ArrayUtil> targets = !a.itemTargets.isEmpty() ? a.itemTargets : load_List_List(a.targetItems);
      if (targets.isEmpty()) {
         return false;
      }

      for (autismclient.util.macro.ArrayUtil t : targets) {
         if (t != null && (t.init_Z() || t.update()) && t.init_Z_ItemStack_I(stack, visibleSlot)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public List<int[]> init_List_ai(ai a) {
      List<int[]> plan = new ArrayList<>();
      if (a == null) {
         return plan;
      }

      synchronized (this.push_Obj) {
         int size = this.init_List.size();
         if (size == 0) {
            return plan;
         } else if (this.init_ItemStack != null && !this.init_ItemStack.isEmpty()) {
            return plan;
         } else {
            int from = this.init_I_ai(a, true, size);
            int to = this.init_I_ai(a, false, size);
            if (from >= 0 && to >= 0 && from != to) {
               int pickup = ContainerInput.PICKUP.ordinal();
               plan.add(new int[]{from, 0, pickup});
               plan.add(new int[]{to, 0, pickup});
               plan.add(new int[]{from, 0, pickup});
               return plan;
            } else {
               return plan;
            }
         }
      }
   }

   private int init_I_ai(ai a, boolean isFrom, int size) {
      boolean useItem = isFrom ? a.fromUseItemName : a.toUseItemName;
      if (useItem) {
         autismclient.util.macro.ArrayUtil t = isFrom ? a.fromItemTarget : a.toItemTarget;
         if (t != null && (t.update() || t.init_Z())) {
            return t.init_Z() && !t.update()
               ? read_I_I_I(this.load_I_I(t.init_I), size)
               : this.init_I_ArrayUtil(
                  t,
                  autismclient.util.macro.MapStore.Inner_DataUtil.init_MapStore_DataUtil,
                  autismclient.util.macro.MapStore.Inner_CachePool.init_MapStore_CachePool,
                  size
               );
         } else {
            return -1;
         }
      } else {
         int slot = isFrom ? a.fromSlot : a.toSlot;
         return slot >= 0 ? read_I_I_I(this.load_I_I(slot), size) : -1;
      }
   }

   private static int read_I_I_I(int handler, int size) {
      return handler >= 0 && handler < size ? handler : -1;
   }

   @Override
   public List<int[]> init_List_I_2(I a) {
      List<int[]> plan = new ArrayList<>();
      if (a == null) {
         return plan;
      }

      synchronized (this.push_Obj) {
         ItemStack cur = this.init_ItemStack;
         if (cur != null && !cur.isEmpty()) {
            int size = this.init_List.size();
            int trigger = -1;
            boolean hasMatch = false;

            for (int h = 0; h < size; h++) {
               ItemStack stack = this.init_List.get(h);
               if (stack != null && !stack.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, cur)) {
                     hasMatch = true;
                  }
               } else if (trigger < 0) {
                  trigger = h;
               }
            }

            if (trigger >= 0 && hasMatch) {
               int pickupAll = ContainerInput.PICKUP_ALL.ordinal();
               int times = Math.max(1, Math.min(64, a.times));

               for (int i = 0; i < times; i++) {
                  plan.add(new int[]{trigger, 0, pickupAll});
               }

               return plan;
            } else {
               return plan;
            }
         } else {
            return plan;
         }
      }
   }

   @Override
   public List<int[]> init_List_StateCache(autismclient.util.macro.StateCache a) {
      List<int[]> plan = new ArrayList<>();
      if (a == null) {
         return plan;
      }

      if (a.containerSource != autismclient.util.macro.StateCache.Inner_DataUtil.init
         && a.containerSource != autismclient.util.macro.StateCache.Inner_DataUtil.read) {
         return plan;
      }

      int button = 0;

      try {
         button = Integer.parseInt(a.button.split(" ")[0]);
      } catch (RuntimeException var16) {
      }

      ContainerInput input;
      try {
         input = ContainerInput.valueOf(a.containerInput);
      } catch (RuntimeException ignored) {
         input = ContainerInput.PICKUP;
      }

      int inputOrd = input.ordinal();
      List<Integer> visibleSlots = a.init();
      int repeat = Math.max(1, a.repeatCount);
      synchronized (this.push_Obj) {
         int size = this.init_List.size();

         for (int r = 0; r < repeat && plan.size() < 256; r++) {
            for (int visible : visibleSlots) {
               if (plan.size() < 256) {
                  int handler = this.load_I_I(visible);
                  if (handler >= 0 && handler < size) {
                     plan.add(new int[]{handler, button, inputOrd});
                  }
               }
            }
         }

         return plan;
      }
   }

   public String load_Str_Str_Str(String name, String args) {
      if (this.init_AtomicReference.get() != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      }

      String rest = args == null ? "" : args.trim();
      String[] parts = rest.isEmpty() ? new String[0] : rest.split("\\s+");
      switch (name) {
         case "click-slot":
            if (parts.length < 1) {
               return "Usage: click-slot <slot> [mode] [count]";
            } else {
               int visible = sync_I_Str(parts[0]);
               if (visible < 0) {
                  return "Bad slot: " + parts[0];
               } else {
                  int handler = this.load_I_I(visible);
                  if (handler < 0) {
                     return "Slot not available in this menu";
                  } else {
                     BufferHelper.Inner_DataUtil spec = BufferHelper.init_BufferHelper_DataU(parts.length > 1 ? parts[1] : "left");
                     if (spec == null) {
                        return "Bad click mode";
                     }

                     return this.init_Str_I_2(handler, spec, parts.length > 2 ? parts[2] : null);
                  }
               }
            }
         case "click-item":
            if (rest.isEmpty()) {
               return "Usage: click-item <name> [mode] [count]";
            } else {
               String[] split = init_StrArr_Str(rest);
               String[] tail = split[1].isEmpty() ? new String[0] : split[1].split("\\s+");
               BufferHelper.Inner_DataUtil spec = BufferHelper.init_BufferHelper_DataU(tail.length > 0 ? tail[0] : "left");
               if (spec == null) {
                  return "Bad click mode";
               } else {
                  int slot = this.update_I_Str(split[0]);
                  if (slot < 0) {
                     return "Item not found in the open menu";
                  }

                  return this.init_Str_I_2(slot, spec, tail.length > 1 ? tail[1] : null);
               }
            }
         case "change-slot":
            Integer n = parts.length > 0 ? init_Integer_Str(parts[0]) : null;
            if (n != null && n >= 1 && n <= 9) {
               return this.read_Str_I(n - 1);
            }

            return "Usage: change-slot <1-9>";
         case "drop":
            if (parts.length == 0) {
               return "Usage: drop <hand|fullinventory|<amount> [item]|<item>>";
            } else {
               String first = parts[0].toLowerCase(Locale.ROOT);
               if (first.equals("hand")) {
                  return this.init_Str_Z(true);
               } else if (init_Z_Str_Str(first, parts.length > 1 ? parts[1] : "")) {
                  return this.tick_Str();
               } else {
                  Integer amount = init_Integer_Str(parts[0]);
                  if (amount != null) {
                     if (parts.length >= 2) {
                        String query = init_StrArr_Str(rest.substring(parts[0].length()).trim())[0];
                        return this.init_Str_Str(query, amount, false);
                     }

                     return this.load_Str_I_I(this.run_I(), Math.min(amount, this.flush_I_I(this.run_I())));
                  }

                  return this.init_Str_Str(rest, 2147483647, true);
               }
            }
         case "close":
            return this.run_Str();
         case "close-silent":
            return this.pop_Str();
         case "use":
            return this.get_Str();
         case "swing":
            return this.set_Str();
         case "say":
            return rest.isEmpty() ? "Usage: say <message>" : this.read_Str_Str(rest);
         case "damage":
            Double dmgAmount = parts.length > 0 ? init_Double_Str(parts[0]) : null;
            return dmgAmount != null && !(dmgAmount < 0.1) && !(dmgAmount > 1024.0) ? this.read_Str_Str("/damage @s " + dmgAmount) : "Usage: damage <amount>";
         case "vclip":
         case "hclip":
            String precheck = init_Str_ClassCache_PathHel_Z(this.init_AtomicReference.get(), this.write_Z);
            if (!precheck.isBlank()) {
               return precheck;
            } else {
               BufferHelper.Inner_StreamUtil request = BufferHelper.init_BufferHelper_Strea(name, rest);
               if (request.init_BufferHelper_Cache() == null) {
                  return request.init_Str();
               } else {
                  BufferHelper.Inner_CachePool spec = request.init_BufferHelper_Cache();
                  double dx = 0.0;
                  double dz = 0.0;
                  if (name.equals("hclip")) {
                     double yaw = Math.toRadians(this.init_PositionMoveRotati.yRot());
                     dx = -Math.sin(yaw) * spec.init_D();
                     dz = Math.cos(yaw) * spec.init_D();
                  }

                  double dy = name.equals("vclip") ? spec.init_D() : 0.0;
                  int queued = this.init_I_D(dx, dy, dz, spec.init_I(), spec.init_Z());
                  if (queued < 0) {
                     return "Clip refused";
                  }

                  return queued > 0 ? "Sent" : "Nothing to clip";
               }
            }
         case "send":
            if (parts.length < 1) {
               return "Usage: send <PacketClass> [args]";
            } else {
               Class<? extends Packet<?>> cls = bw.init_Class_Str(parts[0]);
               if (cls == null) {
                  return "Unknown packet: " + parts[0];
               }

               return this.init_Str_Class_Str(cls, rest.substring(parts[0].length()).trim());
            }
         default:
            return "unknown or unsupported client command";
      }
   }

   private String init_Str_I_2(int handlerSlot, BufferHelper.Inner_DataUtil spec, String countArg) {
      int count = 1;
      if (countArg != null) {
         Integer parsed = init_Integer_Str(countArg);
         if (parsed == null || parsed < 1 || parsed > 100000) {
            return "Click count must be 1-100000";
         }

         count = parsed;
      }

      return this.init_Str_I(handlerSlot, spec.init_I(), spec.init_ContainerInput(), false, count);
   }

   private static Integer init_Integer_Str(String value) {
      try {
         return Integer.parseInt(value.trim());
      } catch (RuntimeException error) {
         return null;
      }
   }

   private static Double init_Double_Str(String value) {
      try {
         double parsed = Double.parseDouble(value.trim());
         return Double.isFinite(parsed) ? parsed : null;
      } catch (RuntimeException error) {
         return null;
      }
   }

   static boolean init_Z_Str_Str(String first, String second) {
      if (first == null) {
         return false;
      }

      String normalized = first.toLowerCase(Locale.ROOT);
      return normalized.equals("fullinventory")
         || normalized.equals("all")
         || normalized.equals("inv")
         || normalized.equals("inventory")
         || normalized.equals("full") && "inventory".equalsIgnoreCase(second);
   }

   private static int sync_I_Str(String token) {
      if (token == null) {
         return -1;
      }

      String t = token.trim().toLowerCase(Locale.ROOT);
      switch (t) {
         case "boots":
            return 36;
         case "leggings":
            return 37;
         case "chestplate":
            return 38;
         case "helmet":
            return 39;
         case "offhand":
            return 40;
         default:
            int indexed = init_I_Str(t, "hotbar", 1, 9, 0);
            if (indexed >= 0) {
               return indexed;
            } else {
               indexed = init_I_Str(t, "inventory", 1, 27, 9);
               if (indexed >= 0) {
                  return indexed;
               } else {
                  indexed = init_I_Str(t, "gui", 1, 999, 100);
                  if (indexed >= 0) {
                     return indexed;
                  } else {
                     Integer numeric = init_Integer_Str(t);
                     return numeric != null && numeric >= 0 ? numeric : -1;
                  }
               }
            }
      }
   }

   private static int init_I_Str(String token, String prefix, int min, int max, int base) {
      if (!token.startsWith(prefix)) {
         return -1;
      }

      Integer n = init_Integer_Str(token.substring(prefix.length()));
      return n != null && n >= min && n <= max ? base + (n - min) : -1;
   }

   private static String[] init_StrArr_Str(String input) {
      String s = input == null ? "" : input.trim();
      if (s.startsWith("\"")) {
         int end = s.indexOf(34, 1);
         return end > 0 ? new String[]{s.substring(1, end), s.substring(end + 1).trim()} : new String[]{s.substring(1), ""};
      } else {
         int space = s.indexOf(32);
         return space < 0 ? new String[]{s, ""} : new String[]{s.substring(0, space), s.substring(space + 1).trim()};
      }
   }

   private String load_Str_I_I(int handler, int count) {
      if (handler >= 0 && count > 0) {
         if (!this.cache_Z()) {
            return "Rate limited";
         }

         int n = Math.max(1, Math.min(128, count));

         for (int i = 0; i < n; i++) {
            if (!this.load(handler, 0, ContainerInput.THROW)) {
               return "Blocked by packet policy";
            }
         }

         return "Sent";
      } else {
         return "Nothing to drop";
      }
   }

   private int flush_I_I(int handler) {
      synchronized (this.push_Obj) {
         List<ItemStack> source = this.close_I < 0 ? this.load_List : this.init_List;
         if (handler >= 0 && handler < source.size()) {
            ItemStack stack = init_ItemStack_ItemStack(source.get(handler));
            return stack.isEmpty() ? 0 : stack.getCount();
         } else {
            return 0;
         }
      }
   }

   private String init_Str_Str(String query, int requested, boolean wholeFirstStack) {
      String normalized = flush_Str_Str(query);
      if (normalized.isEmpty()) {
         return "Item not found in the bot inventory";
      }

      record Inner_DataUtil(int init, int load) {
      }

      List<Inner_DataUtil> candidates = new ArrayList<>();
      synchronized (this.push_Obj) {
         int selected = Math.max(0, Math.min(8, this.add_I));
         int[] order = new int[36];
         int cursor = 0;
         order[cursor++] = 36 + selected;

         for (int h = 36; h <= 44; h++) {
            if (h != 36 + selected) {
               order[cursor++] = h;
            }
         }

         int h = 9;

         while (h <= 35) {
            order[cursor++] = h++;
         }

         for (int hx : order) {
            if (hx >= 0 && hx < this.load_List.size()) {
               ItemStack stack = init_ItemStack_ItemStack(this.load_List.get(hx));
               if (!stack.isEmpty() && init_Z_ItemStack_Str(stack, normalized)) {
                  int visible = hx >= 36 ? hx - 36 : hx;
                  int live = this.load_I_I(visible);
                  if (live >= 0) {
                     candidates.add(new Inner_DataUtil(live, stack.getCount()));
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return "Item not found in the bot inventory";
      }

      if (!this.cache_Z()) {
         return "Rate limited";
      }

      if (wholeFirstStack) {
         return this.load(candidates.getFirst().init(), 1, ContainerInput.THROW) ? "Sent" : "Blocked by packet policy";
      }

      int remaining = Math.max(1, requested);

      for (Inner_DataUtil candidate : candidates) {
         int count = Math.min(remaining, candidate.load());

         for (int i = 0; i < count; i++) {
            if (!this.load(candidate.init(), 0, ContainerInput.THROW)) {
               return remaining < requested ? "Sent" : "Blocked by packet policy";
            }
         }

         remaining -= count;
         if (remaining <= 0) {
            break;
         }
      }

      return remaining < requested ? "Sent" : "Nothing to drop";
   }

   public String put_Str() {
      String precheck = init_Str_ClassCache_PathHel_Z(this.init_AtomicReference.get(), this.write_Z);
      if (!precheck.isBlank()) {
         return precheck;
      } else if (!this.cache_Z()) {
         return "Rate limited";
      } else {
         return this.load_Z_Z(false) ? "Sent" : "Blocked by packet policy";
      }
   }

   public String add_Str() {
      return this.put_Str();
   }

   static String init_Str_ClassCache_PathHel_Z(ClassCache.Inner_PathHelper status, boolean hasPosition) {
      if (status != ClassCache.Inner_PathHelper.flush) {
         return "Session is not ready";
      } else {
         return !hasPosition ? "Position is not ready" : "";
      }
   }

   private synchronized boolean cache_Z() {
      long now = System.currentTimeMillis();
      if (now - this.chunk >= 1000L) {
         this.chunk = now;
         this.value_I = 0;
      }

      if (this.value_I >= 20) {
         return false;
      }

      this.value_I++;
      return true;
   }

   void init_V_IoWorker(IoWorker updated) {
      if (updated != null) {
         boolean wasGravity = this.init_IoWorker != null && this.init_IoWorker.init_Z();
         this.init_IoWorker = new IoWorker(updated);
         if (this.init_IoWorker.init_Z() && !wasGravity) {
            this.tick_Z = true;
         }
      }
   }

   void init_V_aM(aM macro) {
      this.init_V_aM_J(macro, 0L);
   }

   void init_V_aM_J(aM macro, long startAtMs) {
      if (this.level_Z) {
         PkgCls_a.init_V_b_Str(this, "macro took POV ownership");
      }

      long now = System.currentTimeMillis();
      this.init_ClassCache_RenderS = startAtMs > now ? new ClassCache.Inner_RenderState(now, startAtMs) : ClassCache.Inner_RenderState.init_ClassCache_RenderS;
      this.init_aM = macro;
      this.pool_Z = false;
      this.task_Z = macro != null && autismclient.util.macro.ClassCache.init(macro.init_List, 0, macro.init_Z);
      this.flags_Z = false;
      this.entry_Z = false;
   }

   void load_V_aM(aM macro) {
      if (this.level_Z) {
         PkgCls_a.init_V_b_Str(this, "macro took POV ownership");
      }

      this.init_ClassCache_RenderS = ClassCache.Inner_RenderState.init_ClassCache_RenderS;
      this.init_aM = macro;
      this.pool_Z = false;
      this.task_Z = macro != null && autismclient.util.macro.ClassCache.init(macro.init_List, 0, macro.init_Z);
      this.flags_Z = false;
      this.entry_Z = true;
      this.store = System.currentTimeMillis() + 40000L;
   }

   void sync_V() {
      this.flags_Z = true;
      this.pool_Z = true;
      this.task_Z = false;
      this.load_V();
      this.init_aM = null;
      this.init_ClassCache_RenderS = ClassCache.Inner_RenderState.init_ClassCache_RenderS;
      this.entry_Z = false;
   }

   boolean set_Z() {
      return this.init_TextFormat != null || this.init_aM != null;
   }

   boolean put_Z() {
      long now = System.currentTimeMillis();
      return init_Z_Z_2(this.init_aM != null, this.init_TextFormat != null, this.load_Z_J(now));
   }

   static boolean init_Z_Z_2(boolean startRequested, boolean interpreterRunning, boolean motorActive) {
      return startRequested || interpreterRunning || motorActive;
   }

   private boolean load_Z_J(long now) {
      return now < this.clear_J || this.block_Z && now < this.reset_J;
   }

   public String clear_Str() {
      TextFormat run = this.init_TextFormat;
      return run == null ? "" : run.load_Str();
   }

   private void sync_V_J(long now) {
      if (this.flags_Z) {
         this.flags_Z = false;
         if (this.init_TextFormat != null) {
            this.init_TextFormat.init_V_EventBus(this);
         }

         this.task_Z = false;
         this.init_TextFormat = null;
         this.pop_Str = "";
         this.init_ClassCache_UiHelpe = ClassCache.Inner_UiHelper.init_ClassCache_UiHelpe();
         this.init_ClassCache_RenderS = ClassCache.Inner_RenderState.init_ClassCache_RenderS;
         this.next_V();
         this.start_V();
      }

      aM start = this.init_aM;
      if (start != null && this.init_ClassCache_RenderS.init_Z(now)) {
         start = null;
      }

      if (start != null) {
         this.init_aM = null;
         this.init_ClassCache_RenderS = ClassCache.Inner_RenderState.init_ClassCache_RenderS;
         if (this.init_TextFormat != null) {
            this.init_TextFormat.init_V_EventBus(this);
            this.next_V();
            this.start_V();
         }

         this.stop_V();
         this.init_TextFormat = start.init_List.isEmpty() ? null : new TextFormat(start);
         this.pop_Str = this.init_TextFormat == null ? "" : this.init_TextFormat.init_Str();
         this.init_ClassCache_UiHelpe = this.init_TextFormat == null
            ? ClassCache.Inner_UiHelper.init_ClassCache_UiHelpe()
            : this.init_TextFormat.init_ClassCache_UiHelpe();
      }

      TextFormat run = this.init_TextFormat;
      if (run != null) {
         try {
            run.init_V_J_EventBus(now, this);
         } catch (RuntimeException error) {
            run.init_V_EventBus(this);
            this.task_Z = false;
            this.init_TextFormat = null;
            this.pop_Str = "error";
            this.init_ClassCache_UiHelpe = new ClassCache.Inner_UiHelper(
               run.load_Str(), false, run.init_I(), run.load_I(), run.read_I(), PacketUtil.update("Error: " + init_Str_Throwable(error), 64)
            );
            this.load_AtomicReference.set(run.load_Str() + Character.toString(0) + "error");
            this.entry_Z = false;
            this.next_V();
            this.start_V();
            return;
         }

         if (run.init_Z()) {
            if ("done".equals(run.init_Str()) && this.load_Z_J(now)) {
               this.pop_Str = "finishing movement";
               this.init_ClassCache_UiHelpe = new ClassCache.Inner_UiHelper(
                  run.load_Str(), true, run.load_I(), run.load_I(), run.read_I(), "Finishing movement"
               );
               return;
            }

            this.load_AtomicReference.set(run.load_Str() + "\u0000" + run.init_Str());
            this.init_TextFormat = null;
            this.task_Z = false;
            this.entry_Z = false;
            this.pop_Str = "";
            this.init_ClassCache_UiHelpe = new ClassCache.Inner_UiHelper(
               run.load_Str(), false, run.load_I(), run.load_I(), run.read_I(), PacketUtil.update(run.init_Str(), 64)
            );
            this.start_V();
         } else {
            this.task_Z = run.load_Z();
            this.pop_Str = run.init_Str();
            this.init_ClassCache_UiHelpe = run.init_ClassCache_UiHelpe();
         }
      }
   }

   String reset_Str() {
      return this.load_AtomicReference.getAndSet(null);
   }

   private void start_V() {
      this.prev_V();
      this.read_V();
      if (this.get_Z || !this.set_Z) {
         this.get_Z = false;
         this.set_Z = true;
         this.size_V();
      }

      this.init_Z_Packet(
         new ServerboundPlayerActionPacket(
            net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
         ),
         false,
         false
      );
   }

   private void stop_V() {
      this.get_Z = false;
      this.set_Z = true;
      this.size_V();
      this.put_Z = false;
      this.flush_V_Z(true);
      this.init_Z_Packet(
         new ServerboundPlayerActionPacket(
            net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
         ),
         false,
         false
      );
   }

   private void next_V() {
      this.valid_V();
      this.reset_J = 0L;
      this.clear_J = 0L;
      this.add_J = 0L;
      this.poll_D = 0.0;
      this.push_D = 0.0;
      this.write_D = 0.0;
      this.block_Z = false;
      this.poll_Z = false;
   }

   @Override
   public boolean init_Z() {
      return this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && !this.load_AtomicBoolean.get() && this.load_F > 0.0F && this.write_Z;
   }

   boolean add_Z() {
      return this.task_Z && !this.pool_Z;
   }

   @Override
   public FileCache.Inner_StreamUtil init_FileCache_StreamUt() {
      ClassCache.Inner_PathHelper phase = this.init_AtomicReference.get();
      boolean live = !this.load_AtomicBoolean.get();
      boolean world = live && this.count_Z && (phase == ClassCache.Inner_PathHelper.sync || phase == ClassCache.Inner_PathHelper.flush);
      boolean playable = this.init_Z() && this.init_StreamUtil.init_CustomMenuSnapshot() == null && !this.update_Z();
      String address = this.index_Z && !this.push_Str.isBlank() ? this.push_Str : this.open_Str;
      return new FileCache.Inner_StreamUtil(
         address,
         live && phase != ClassCache.Inner_PathHelper.init && phase != ClassCache.Inner_PathHelper.open && phase != ClassCache.Inner_PathHelper.close,
         world,
         this.cache_I,
         world && this.reset_Z,
         playable,
         this.state_I,
         live && this.init_StreamUtil.init_CustomMenuSnapshot() != null,
         this.pool_Z,
         this.read_Str
      );
   }

   @Override
   public String init_Str_Str(String address) {
      if (address != null && ServerAddress.isValidAddress(address)) {
         ServerAddress target = ServerAddress.parseString(address.trim());
         synchronized (this.load_Obj) {
            if (this.pool_Z || this.flags_Z || !this.task_Z) {
               return "Macro stopped";
            }

            this.read_I++;
            this.load_AtomicBoolean.set(false);
            this.index_Z = true;
            this.push_Str = address.trim();
            this.count_Z = false;
            this.cache_I = this.state_I = 0;
         }

         this.next_V();
         this.start_V();
         this.write_Z = false;
         this.update_Z = false;
         this.put_Z = false;
         this.init_MapStore.load();
         this.init_V_MapStore_JsonUtil(null, target.getHost(), target.getPort(), false, 0L, true);
         return "";
      } else {
         return "Invalid server address";
      }
   }

   @Override
   public void init_V() {
      this.index_Z = false;
      this.push_Str = "";
      if (this.entry_Z) {
         this.store = System.currentTimeMillis() + 40000L;
      }
   }

   @Override
   public void load_V() {
      Connection old;
      synchronized (this.load_Obj) {
         if (!this.index_Z) {
            return;
         }

         this.index_Z = false;
         this.push_Str = "";
         if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush || this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.sync) {
            return;
         }

         this.read_I++;
         this.load_AtomicBoolean.set(true);
         this.init_ClassCache_ConfigS = null;
         old = this.init_Connection;
      }

      this.init_MapStore.read();
      this.init_V_Z_Z(true, true);
      this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.close, "Macro join cancelled");
      init_V_Connection_Component(old, Component.literal("Macro join cancelled"));
   }

   @Override
   public void load_V_Str(String reason) {
      Connection old;
      synchronized (this.load_Obj) {
         if (this.pool_Z || this.flags_Z) {
            return;
         }

         this.read_I++;
         this.load_AtomicBoolean.set(true);
         this.init_ClassCache_ConfigS = null;
         old = this.init_Connection;
         this.index_Z = false;
         this.push_Str = "";
         this.count_Z = false;
         this.cache_I = this.state_I = 0;
      }

      this.next_V();
      this.start_V();
      this.init_MapStore.read();
      this.init_V_Z_Z(true, true);
      this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.close, reason == null ? "Macro" : reason);
      init_V_Connection_Component(old, Component.literal(reason == null ? "Macro" : reason));
   }

   @Override
   public void init_V_Str(String note) {
      if (note != null && !note.isBlank()) {
         this.push(note);
      }
   }

   @Override
   public boolean load_Z() {
      ClassCache.Inner_PathHelper current = this.init_AtomicReference.get();
      if (this.load_AtomicBoolean.get()) {
         return false;
      } else {
         return current == ClassCache.Inner_PathHelper.flush
            ? this.load_F > 0.0F && this.write_Z
            : current == ClassCache.Inner_PathHelper.update || current == ClassCache.Inner_PathHelper.sync;
      }
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   public CustomMenuSnapshot init_CustomMenuSnapshot() {
      return this.init_StreamUtil.init_CustomMenuSnapshot();
   }

   @Override
   public CustomMenuSubmitResult init_CustomMenuSubmitRe_CustomMenuSnapshot_CustomMenuSubmissi(CustomMenuSnapshot snapshot, CustomMenuSubmission submission) {
      CustomMenuSubmitResult result = CustomMenuAdapterRegistry.submit(snapshot, submission);
      if (!result.success()) {
         return result;
      }

      for (Packet<?> packet : result.packets()) {
         if (!this.init_Z_Packet(packet, true, false)) {
            return CustomMenuSubmitResult.failure("Connection closed during submission");
         }
      }

      this.init_StreamUtil.init(snapshot, result.replacement());
      this.init_V_CustomMenuSnapshot_CustomMenuSnapshot(snapshot, this.init_StreamUtil.init_CustomMenuSnapshot());
      return result;
   }

   @Override
   public String init_Str_Str_Map(String template, Map<String, String> macroVariables) {
      String out = template == null ? "" : template;

      for (Entry<String, String> entry : this.push_Map.entrySet()) {
         out = out.replace("{secret." + entry.getKey() + "}", entry.getValue());
      }

      if (out.matches("(?s).*\\{secret\\.[^}]+}.*")) {
         return null;
      }

      String username = this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
         ? this.init_SyncManager_DataUt.init_User().getName()
         : "";
      out = out.replace("{username}", username).replace("{account_id}", this.init_FieldCache_BufferH.init_Str()).replace("{profile_name}", this.poll_Str);
      if (macroVariables != null) {
         for (Entry<String, String> entry : macroVariables.entrySet()) {
            out = out.replace("{" + entry.getKey() + "}", entry.getValue());
         }
      }

      return out;
   }

   @Override
   public String init_Str() {
      return this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null ? this.init_SyncManager_DataUt.init_User().getName() : "";
   }

   @Override
   public String load_Str() {
      return this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
         ? this.init_SyncManager_DataUt.init_User().getProfileId().toString()
         : "";
   }

   @Override
   public String read_Str() {
      return this.open_Str;
   }

   void update_V_Z(boolean value) {
      this.size_Z = value;
      if (!value) {
         this.init_CacheEntry.init_V();
      }
   }

   boolean clear_Z() {
      return this.size_Z;
   }

   boolean reset_Z() {
      return this.size_Z && this.init_CacheEntry.init_Z();
   }

   CacheEntry.Inner_DataUtil init_CacheEntry_DataUti() {
      return this.init_CacheEntry.init_CacheEntry_DataUti();
   }

   Connection init_Connection() {
      return this.init_Connection;
   }

   UUID init_UUID() {
      return this.init_UUID != null
         ? this.init_UUID
         : (
            this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
               ? this.init_SyncManager_DataUt.init_User().getProfileId()
               : null
         );
   }

   PositionMoveRotation init_PositionMoveRotati() {
      return this.init_PositionMoveRotati;
   }

   boolean start_Z() {
      return this.open_Z;
   }

   int flush_I() {
      return this.size_I;
   }

   StateCache.Inner_DataUtil init_StateCache_DataUti_I(int entityId) {
      return this.init_StateCache.init_StateCache_DataUti_I(entityId);
   }

   StateCache.Inner_DataUtil init_StateCache_DataUti_UUID(UUID entityUuid) {
      return this.init_StateCache.init_StateCache_DataUti_UUID(entityUuid);
   }

   Iterable<StateCache.Inner_DataUtil> init_Iterable() {
      return this.init_StateCache.init_Iterable();
   }

   double write_D() {
      return this.pop_D;
   }

   double update_D() {
      return this.tick_D;
   }

   GameProfile init_GameProfile() {
      UUID id = this.init_UUID != null
         ? this.init_UUID
         : (
            this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
               ? this.init_SyncManager_DataUt.init_User().getProfileId()
               : null
         );
      String name = this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
         ? this.init_SyncManager_DataUt.init_User().getName()
         : this.init_FieldCache_BufferH.init_Str();
      return init_GameProfile_GameProfile(this.init_GameProfile, id, name);
   }

   static GameProfile init_GameProfile_GameProfile(GameProfile known, UUID id, String name) {
      if (known != null && known.properties() != null && !known.properties().isEmpty()) {
         return Objects.equals(known.id(), id) && Objects.equals(known.name(), name) ? known : new GameProfile(id, name, known.properties());
      } else {
         return new GameProfile(id, name);
      }
   }

   List<ItemStack> load_List() {
      List<ItemStack> copy = new ArrayList<>(this.load_List.size());
      synchronized (this.push_Obj) {
         for (ItemStack stack : this.load_List) {
            copy.add(stack == null ? ItemStack.EMPTY : stack.copy());
         }

         return copy;
      }
   }

   ItemStack init_ItemStack() {
      synchronized (this.push_Obj) {
         return this.init_ItemStack == null ? ItemStack.EMPTY : this.init_ItemStack.copy();
      }
   }

   int close_I() {
      return this.poll_I;
   }

   int open_I() {
      return Math.max(0, Math.min(8, this.add_I));
   }

   float update_F() {
      return this.load_F;
   }

   int poll_I() {
      return this.data_I;
   }

   int push_I() {
      return this.push_I;
   }

   String start_Str() {
      return this.close_Str;
   }

   void sync_V_Z(boolean value) {
      if (!value) {
         PkgCls_a.init_V_b_Str(this, "POV ownership changed");
      }

      this.level_Z = value;
      if (value && !this.put_Z()) {
         this.clear_J = 0L;
         this.add_J = 0L;
         this.write_D = 0.0;
         this.poll_Z = false;
         this.level_I = 0;
         this.block_Z = false;
         this.init_TaskQueue_2.init_V_J(System.currentTimeMillis());
      }
   }

   boolean stop_Z() {
      return this.level_Z;
   }

   boolean next_Z() {
      return this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && this.write_Z && this.tick_Z();
   }

   void init_V_Vec3(Vec3 pos) {
      if (pos != null) {
         long now = System.currentTimeMillis();
         this.init_Vec3 = pos;
         this.f_c_J = now;
         if (this.level_Z && this.put_Z() && this.init_TableIndex.init_TableIndex_DataUti(pos, now) == TableIndex.Inner_DataUtil.read) {
            synchronized (this.sync_Obj) {
               this.init_PositionMoveRotati = new PositionMoveRotation(
                  pos, this.init_PositionMoveRotati.deltaMovement(), this.init_PositionMoveRotati.yRot(), this.init_PositionMoveRotati.xRot()
               );
            }
         }
      }
   }

   void init_V_Vec3(Vec3 pos, Vec3 deltaMovement, float yRot, float xRot, boolean onGround, boolean horizontalCollision) {
      synchronized (this.tick_Obj) {
         if (this.next_Z() && !this.open_Z()) {
            synchronized (this.sync_Obj) {
               this.init_PositionMoveRotati = new PositionMoveRotation(pos, deltaMovement == null ? Vec3.ZERO : deltaMovement, yRot, xRot);
            }

            this.open_Z = onGround;
            TokenStore.Inner_DataUtil wire = TokenStore.init_TokenStore_DataUti_Vec3(pos, onGround, deltaMovement);
            if (this.init_ClassCache_HashUti_Packet(new PosRot(wire.init_Vec3(), yRot, xRot, wire.init_Z(), horizontalCollision), false, false)
               == ClassCache.Inner_HashUtil.init) {
               this.value_J = System.currentTimeMillis();
               this.init_TableIndex.load(pos, this.value_J);
            }
         }
      }
   }

   boolean init_Z_Vec3_Z(Vec3 pos, boolean onGround) {
      return pos == null ? false : this.init_Z_List(List.of(new A.Inner_DataUtil(pos, onGround)));
   }

   boolean init_Z_List(List<A.Inner_DataUtil> steps) {
      if (steps != null && !steps.isEmpty()) {
         synchronized (this.tick_Obj) {
            if (this.next_Z() && this.level_Z && !this.put_Z() && !this.open_Z()) {
               long generation;
               long epoch;
               synchronized (this.sync_Obj) {
                  epoch = this.f_e_J;
                  synchronized (this.run_Obj) {
                     generation = this.f_d_J;
                  }
               }

               A.Inner_DataUtil accepted = null;
               synchronized (this.pop_Obj) {
                  for (A.Inner_DataUtil step : steps) {
                     if (step == null
                        || step.init_Vec3() == null
                        || this.init_Z_J_J(epoch, generation)
                        || this.init_ClassCache_HashUti_Packet(new Pos(step.init_Vec3(), step.init_Z(), false), false, false) != ClassCache.Inner_HashUtil.init
                        )
                      {
                        break;
                     }

                     accepted = step;
                  }
               }

               if (accepted == null) {
                  return false;
               }

               synchronized (this.sync_Obj) {
                  if (epoch != this.f_e_J) {
                     return false;
                  }

                  synchronized (this.run_Obj) {
                     if (generation != this.f_d_J) {
                        return false;
                     }
                  }

                  this.init_PositionMoveRotati = new PositionMoveRotation(
                     accepted.init_Vec3(), Vec3.ZERO, this.init_PositionMoveRotati.yRot(), this.init_PositionMoveRotati.xRot()
                  );
                  this.open_Z = accepted.init_Z();
                  this.value_J = System.currentTimeMillis();
                  this.init_TableIndex.load(accepted.init_Vec3(), this.value_J);
                  this.write_D = 0.0;
                  this.clear_J = 0L;
                  this.block_Z = false;
                  this.poll_Z = false;
               }

               return accepted == steps.getLast();
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_J_J(long epoch, long generation) {
      synchronized (this.sync_Obj) {
         if (epoch != this.f_e_J) {
            return true;
         }

         boolean var10000;
         synchronized (this.run_Obj) {
            var10000 = generation != this.f_d_J;
         }

         return var10000;
      }
   }

   boolean init_Z_Z(boolean onGround) {
      synchronized (this.tick_Obj) {
         if (this.next_Z() && this.level_Z && !this.put_Z() && !this.open_Z()) {
            if (this.init_ClassCache_HashUti_Packet(new StatusOnly(onGround, false), false, false) != ClassCache.Inner_HashUtil.init) {
               return false;
            }

            this.open_Z = onGround;
            this.value_J = System.currentTimeMillis();
            return true;
         } else {
            return false;
         }
      }
   }

   boolean init_Z_Packet(Packet<?> packet) {
      synchronized (this.tick_Obj) {
         return this.next_Z() && this.init_Z_Packet(packet, false, false);
      }
   }

   boolean init_Z_Entity_ServerboundPlayerC(Entity renderedBot, net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action action) {
      synchronized (this.tick_Obj) {
         if (this.next_Z() && renderedBot != null && action != null && this.size_I >= 0) {
            ServerboundPlayerCommandPacket packet = new ServerboundPlayerCommandPacket(renderedBot, action);
            ((AutismPlayerCommandPacketAccessor)packet).autism$setEntityId(this.size_I);
            return this.init_Z_Packet(packet, false, false);
         } else {
            return false;
         }
      }
   }

   boolean init_Z_Abilities(Abilities abilities) {
      return abilities != null && this.init_Z_Packet(new ServerboundPlayerAbilitiesPacket(abilities));
   }

   boolean load_Z_I(int slot) {
      return "Sent".equals(this.read_Str_I(Math.max(0, Math.min(8, slot))));
   }

   boolean init_Z_I_I(int inventorySlot, int hotbarSlot) {
      if (this.close_I < 0 && inventorySlot >= 0 && inventorySlot <= 35) {
         int handler = inventorySlot <= 8 ? 36 + inventorySlot : inventorySlot;
         return "Sent".equals(this.init_Str_I(handler, Math.max(0, Math.min(8, hotbarSlot)), ContainerInput.SWAP, false));
      } else {
         return false;
      }
   }

   boolean read_Z_I(int inventorySlot) {
      if (this.close_I < 0 && inventorySlot >= 0 && inventorySlot <= 35) {
         int hotbar = Math.max(0, Math.min(8, this.add_I));
         if (inventorySlot <= 8) {
            return "Sent".equals(this.init_Str_I(6, inventorySlot, ContainerInput.SWAP, false));
         }

         int handler = inventorySlot;
         return "Sent".equals(this.init_Str_I(handler, hotbar, ContainerInput.SWAP, false))
            && "Sent".equals(this.init_Str_I(6, hotbar, ContainerInput.SWAP, false))
            && "Sent".equals(this.init_Str_I(handler, hotbar, ContainerInput.SWAP, false));
      } else {
         return false;
      }
   }

   boolean write_Z_I(int entityId) {
      synchronized (this.tick_Obj) {
         if (!this.next_Z() || entityId < 0) {
            return false;
         }

         if (!this.state_Z()) {
            return false;
         }

         if (!this.init_Z_Packet(new ServerboundAttackPacket(entityId), false, false)) {
            return false;
         }

         this.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND), false, false);
         return true;
      }
   }

   boolean init_Z_I(int entityId, InteractionHand hand, Vec3 location, boolean secondary) {
      synchronized (this.tick_Obj) {
         if (entityId < 0 || hand == null || location == null) {
            return false;
         }

         if (!this.next_Z()) {
            return false;
         }

         boolean slotReady = this.state_Z();
         return slotReady && this.init_Z_Packet(new ServerboundInteractPacket(entityId, hand, location, secondary), false, false);
      }
   }

   private boolean state_Z() {
      int slot = Math.max(0, Math.min(8, this.add_I));
      if (this.clear_I != slot) {
         if (!this.init_Z_Packet(new ServerboundSetCarriedItemPacket(slot), false, false)) {
            return false;
         }

         this.clear_I = slot;
      }

      return true;
   }

   int pop_I() {
      return ++this.reset_I;
   }

   void flush_V() {
      synchronized (this.tick_Obj) {
         if (this.next_Z() && this.close_I >= 0) {
            this.run_Str();
         }
      }
   }

   long poll_J() {
      return this.f_f_J;
   }

   long push_J() {
      return this.f_g_J;
   }

   BlockPos init_BlockPos() {
      return this.init_BlockPos;
   }

   boolean prev_Z() {
      return this.prev_Z;
   }

   InteractionHand init_InteractionHand() {
      return this.init_InteractionHand;
   }

   boolean valid_Z() {
      return this.next_Z;
   }

   void close_V() {
      this.next_Z = false;
   }

   float sync_F() {
      long now = System.currentTimeMillis();
      float mult = 1.0F;
      int haste = -1;
      if (this.f_h_J > now && this.index_I >= 0) {
         haste = this.index_I;
      }

      if (this.f_i_J > now && this.count_I > haste) {
         haste = this.count_I;
      }

      if (haste >= 0) {
         mult *= 1.0F + (haste + 1) * 0.2F;
      }

      if (this.f_j_J > now && this.flags_I >= 0) {
         float var10001;
         switch (Math.min(3, this.flags_I)) {
            case 0:
               var10001 = 0.3F;
               break;
            case 1:
               var10001 = 0.09F;
               break;
            case 2:
               var10001 = 0.0027F;
               break;
            default:
               var10001 = 8.1E-4F;
         }

         mult *= var10001;
      }

      return mult;
   }

   Vec3 init_Vec3() {
      return this.read_AtomicReference.getAndSet(null);
   }

   void open_V() {
      long now = System.currentTimeMillis();
      Vec3 delta = this.init_PositionMoveRotati.deltaMovement() == null ? Vec3.ZERO : this.init_PositionMoveRotati.deltaMovement();
      this.poll_Z = false;
      if (this.open_Z) {
         this.write_D = 0.0;
         this.block_Z = false;
      } else {
         this.write_D = Math.max(-3.92, Math.min(1.0, delta.y));
         this.f_l_J = now + 15000L;
         this.block_Z = true;
         this.add_J = Math.max(this.add_J, now + 5000L);
      }

      this.push_Z = false;
      this.update_I = -1;
      this.pop_Z = false;
      this.poll_Map.clear();
   }

   @Override
   public String write_Str() {
      return this.entry_Z ? this.prev_Str() : "";
   }

   @Override
   public float init_F() {
      return this.load_F;
   }

   @Override
   public float load_F() {
      return this.read_F;
   }

   @Override
   public int init_I() {
      return this.data_I;
   }

   @Override
   public boolean write_Z() {
      return this.write_Z;
   }

   @Override
   public double init_D() {
      return this.init_PositionMoveRotati.position().x;
   }

   @Override
   public double load_D() {
      return this.init_PositionMoveRotati.position().y;
   }

   @Override
   public double read_D() {
      return this.init_PositionMoveRotati.position().z;
   }

   @Override
   public String update_Str() {
      return this.close_Str;
   }

   @Override
   public String sync_Str() {
      return this.flush_Str;
   }

   @Override
   public int load_I() {
      return this.add_I;
   }

   @Override
   public String flush_Str() {
      return this.update_Str;
   }

   @Override
   public boolean update_Z() {
      return this.close_I >= 0 && !this.data_Z;
   }

   @Override
   public long init_J() {
      return this.queue_J;
   }

   @Override
   public int init_I_Str(String query) {
      String q = flush_Str_Str(query);
      int total = 0;
      synchronized (this.push_Obj) {
         for (ItemStack stack : this.load_List) {
            if (stack != null && !stack.isEmpty() && (q.isEmpty() || init_Z_ItemStack_Str(stack, q))) {
               total += stack.getCount();
            }
         }

         return total;
      }
   }

   @Override
   public int read_I() {
      int free = 0;
      synchronized (this.push_Obj) {
         for (int h = 9; h <= 44; h++) {
            ItemStack stack = h < this.load_List.size() ? this.load_List.get(h) : ItemStack.EMPTY;
            if (stack == null || stack.isEmpty()) {
               free++;
            }
         }

         return free;
      }
   }

   @Override
   public boolean init_Z_I(int visibleSlot) {
      int handler = this.load_I_I(visibleSlot);
      if (handler < 0) {
         return false;
      }

      synchronized (this.push_Obj) {
         ItemStack stack = handler < this.init_List.size() ? this.init_List.get(handler) : ItemStack.EMPTY;
         return stack != null && !stack.isEmpty();
      }
   }

   @Override
   public boolean sync_Z() {
      ItemStack c = this.init_ItemStack;
      return c == null || c.isEmpty();
   }

   @Override
   public String close_Str() {
      ItemStack c = this.init_ItemStack;
      return c != null && !c.isEmpty() ? c.getHoverName().getString() : "";
   }

   @Override
   public int init_I_ArrayUtil(autismclient.util.macro.ArrayUtil target) {
      if (target != null && (target.update() || target.init_Z())) {
         int total = 0;
         synchronized (this.push_Obj) {
            for (int h = 9; h <= 44 && h < this.load_List.size(); h++) {
               ItemStack stack = this.load_List.get(h);
               if (stack != null && !stack.isEmpty()) {
                  int invIndex = h >= 36 ? h - 36 : h;
                  if (target.init_I_ItemStack_I(stack, invIndex) >= 0) {
                     total += stack.getCount();
                  }
               }
            }

            return total;
         }
      } else {
         return 0;
      }
   }

   @Override
   public boolean init_Z_ArrayUtil(autismclient.util.macro.ArrayUtil target) {
      if (target == null) {
         return false;
      }

      ItemStack c = this.init_ItemStack;
      return c != null && !c.isEmpty() && target.init_I_ItemStack_I(c, -1) >= 0;
   }

   @Override
   public float read_F() {
      PositionMoveRotation p = this.init_PositionMoveRotati;
      return p == null ? 0.0F : p.xRot();
   }

   @Override
   public int[] init_IArr() {
      return this.load_IArr_I(36 + Math.max(0, Math.min(8, this.add_I)));
   }

   @Override
   public int[] init_IArr_I(int inventoryIndex) {
      return this.load_IArr_I(close_I_I(inventoryIndex));
   }

   @Override
   public int[] init_IArr_ArrayUtil(autismclient.util.macro.ArrayUtil target) {
      if (target == null) {
         return null;
      }

      autismclient.util.macro.ArrayUtil itemOnly = target.init_ArrayUtil();
      if (itemOnly.update()) {
         itemOnly.init_I = -1;
      }

      synchronized (this.push_Obj) {
         for (int h = 9; h <= 45 && h < this.load_List.size(); h++) {
            ItemStack stack = this.load_List.get(h);
            if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
               int invIndex = h == 45 ? 40 : (h >= 36 ? h - 36 : h);
               if (itemOnly.init_I_ItemStack_I(stack, invIndex) >= 0) {
                  return new int[]{stack.getDamageValue(), stack.getMaxDamage()};
               }
            }
         }

         return null;
      }
   }

   private int[] load_IArr_I(int handler) {
      synchronized (this.push_Obj) {
         if (handler >= 0 && handler < this.load_List.size()) {
            ItemStack s = this.load_List.get(handler);
            return s != null && !s.isEmpty() && s.isDamageableItem() ? new int[]{s.getDamageValue(), s.getMaxDamage()} : null;
         } else {
            return null;
         }
      }
   }

   private static int close_I_I(int inv) {
      if (inv >= 0 && inv <= 8) {
         return 36 + inv;
      } else if (inv >= 9 && inv <= 35) {
         return inv;
      } else if (inv >= 36 && inv <= 39) {
         return 44 - inv;
      } else {
         return inv == 40 ? 45 : -1;
      }
   }

   @Override
   public long load_J() {
      return this.entry_J;
   }

   @Override
   public long sync_J() {
      return this.close_I < 0 ? this.poll_I : this.open_I;
   }

   private void init_V_BlockPos_BlockState(BlockPos pos, BlockState state) {
      if (pos != null && state != null) {
         long key = pos.asLong();
         if (this.poll_Map.size() >= 512 && !this.poll_Map.containsKey(key)) {
            this.poll_Map.clear();
         }

         this.poll_Map.put(key, state);
      }
   }

   @Override
   public boolean init_Z_I(int x, int y, int z, List<String> ids, boolean anyBlock, boolean wantDestroyed) {
      BlockState st = this.poll_Map.get(BlockPos.asLong(x, y, z));
      if (st == null) {
         return false;
      }

      if (wantDestroyed) {
         return st.isAir();
      }

      if (st.isAir()) {
         return false;
      }

      if (!anyBlock && ids != null && !ids.isEmpty()) {
         Identifier id = BuiltInRegistries.BLOCK.getKey(st.getBlock());
         String cur = id == null ? "" : id.toString();

         for (String want : ids) {
            if (want != null && !want.isBlank() && read_Z_Str_Str(want, cur)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public void init_V_Str_Str(String channel, String rawData) {
      if (channel != null && !channel.isBlank()) {
         try {
            byte[] bytes = AutismPayloadSupport.init_BArr(rawData);
            this.init_Z_Packet(AutismPayloadSupport.init_ServerboundCustomP(channel.trim(), bytes), false, false);
         } catch (RuntimeException var4) {
         }
      }
   }

   @Override
   public String[] init_StrArr_aD(aD action) {
      String[] base = new String[action.entries.size()];
      synchronized (this.push_Obj) {
         int size = this.init_List.size();

         for (int i = 0; i < action.entries.size(); i++) {
            aD.Inner_DataUtil e = action.entries.get(i);
            autismclient.util.macro.ArrayUtil t = e.init_ArrayUtil();
            if (e.init_aD_StreamUtil == aD.Inner_StreamUtil.update && t != null && t.init_Z()) {
               int handlerSlot = this.load_I_I(t.init_I);
               ItemStack s = handlerSlot >= 0 && handlerSlot < size ? this.init_List.get(handlerSlot) : ItemStack.EMPTY;
               base[i] = load_Str_ItemStack(s);
            } else {
               base[i] = "";
            }
         }

         return base;
      }
   }

   @Override
   public boolean init_Z_aD_StrArr(aD action, String[] baseline) {
      if (action.entries.isEmpty()) {
         return true;
      }

      synchronized (this.push_Obj) {
         int size = this.init_List.size();

         for (int i = 0; i < action.entries.size(); i++) {
            if (!this.init_Z_aD_DataUtil(action.entries.get(i), i, baseline, size)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean init_Z_aD_DataUtil(aD.Inner_DataUtil e, int idx, String[] baseline, int size) {
      autismclient.util.macro.ArrayUtil target = e.init_ArrayUtil();
      if (target == null) {
         target = new autismclient.util.macro.ArrayUtil();
      }

      if (target.init_Z()) {
         int handlerSlot = this.load_I_I(target.init_I);
         return handlerSlot >= 0 && handlerSlot < size
            ? this.init_Z_ItemStack(this.init_List.get(handlerSlot), target, target.init_I, e, idx, baseline)
            : e.init_aD_StreamUtil == aD.Inner_StreamUtil.load;
      }

      if (e.init_aD_StreamUtil == aD.Inner_StreamUtil.load) {
         for (int h = 0; h < size; h++) {
            ItemStack s = this.init_List.get(h);
            if (s != null && !s.isEmpty()) {
               int vis = this.load_I_I_I(h, size);
               if (!target.update() || target.init_Z_ItemStack_I(s, vis)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         for (int h = 0; h < size; h++) {
            ItemStack s = this.init_List.get(h);
            if (s != null && !s.isEmpty()) {
               int vis = this.load_I_I_I(h, size);
               if ((!target.update() || target.init_Z_ItemStack_I(s, vis)) && this.init_Z_ItemStack(s, target, vis, e, idx, baseline)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean init_Z_ItemStack(ItemStack stack, autismclient.util.macro.ArrayUtil target, int visibleSlot, aD.Inner_DataUtil e, int idx, String[] baseline) {
      boolean empty = stack == null || stack.isEmpty();
      boolean nameMatches = target == null || !target.update() || !empty && target.init_Z_ItemStack_I(stack, visibleSlot);
      boolean var10000;
      switch (e.init_aD_StreamUtil) {
         case init:
            var10000 = !empty && nameMatches;
            break;
         case load:
            var10000 = empty;
            break;
         case read:
            var10000 = !empty && nameMatches && stack.getCount() >= e.init_I;
            break;
         case write:
            var10000 = empty || nameMatches && stack.getCount() < e.init_I;
            break;
         case update:
            String init = baseline != null && idx < baseline.length && baseline[idx] != null ? baseline[idx] : "";
            var10000 = !load_Str_ItemStack(stack).equals(init);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String load_Str_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         String rich = MacroExecutor.init_Str_Component(stack.getHoverName());
         String components = String.valueOf(stack.getComponents());
         return (id == null ? "" : id.toString()) + "|" + stack.getCount() + "|" + (rich == null ? "" : rich) + "|" + components.hashCode();
      } else {
         return "";
      }
   }

   private static int init_I_CommonPlayerSpawnI(CommonPlayerSpawnInfo info) {
      try {
         return info != null && info.gameType() != null ? info.gameType().getId() : -1;
      } catch (Throwable ignored) {
         return -1;
      }
   }

   private void load_V_Component(Component c) {
      if (c != null) {
         this.flush_V_Str(c.getString());
      }
   }

   private void flush_V_Str(String text) {
      if (text != null && !text.isEmpty()) {
         synchronized (this.close_Obj) {
            this.init_ArrayDeque.addLast(text);

            while (this.init_ArrayDeque.size() > 64) {
               this.init_ArrayDeque.removeFirst();
            }

            this.node_J++;
         }

         this.close_V_Str(text);
      }
   }

   void init_V_CachePool(CachePool config) {
      this.init_CachePool_2 = config == null ? new CachePool() : new CachePool(config);
   }

   void write_V_Str(String kind) {
      CachePool config = this.init_CachePool_2;
      if (config != null && kind != null) {
         boolean trade = "trade".equals(kind);
         boolean enabled = trade ? config.read_Z : config.init_Z;
         if (enabled) {
            this.tick_Str = kind;
            this.mark = System.currentTimeMillis() + (trade ? config.write_I : config.load_I);
         }
      }
   }

   private String size_Str() {
      SyncManager.Inner_DataUtil resolved = this.init_SyncManager_DataUt;
      return resolved == null ? this.init_FieldCache_BufferH.init_Str() : resolved.init_User().getName();
   }

   public String stop_Str() {
      return this.size_Str();
   }

   private static boolean load_Z_Str_Str(String haystack, String needle) {
      return needle != null && !needle.isEmpty() && haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
   }

   private void close_V_Str(String text) {
      if (text != null && !text.isEmpty() && this.scan == 0L) {
         CachePool config = this.init_CachePool_2;
         if (config != null) {
            long now = System.currentTimeMillis();
            String me = PacketUtil.init_Str();
            String self = this.size_Str();
            String kind = this.tick_Str;
            if (!kind.isEmpty() && now < this.mark && !me.isBlank() && load_Z_Str_Str(text, me)) {
               boolean trade = "trade".equals(kind);
               String cmd = CachePool.init(trade ? config.sync : config.write_Str, self, me);
               this.init_V_Z(
                  trade ? config.write_Z : config.load_Z, trade ? config.flush : config.update_Str, cmd, trade ? config.update_I : config.read_I, now
               );
               this.tick_Str = "";
               this.mark = 0L;
            } else {
               for (CachePool.Inner_DataUtil responder : config.init_List) {
                  if (responder.init_Z()) {
                     String trigger = CachePool.init(responder.init_Str(), self, me);
                     if (!trigger.isBlank() && load_Z_Str_Str(text, trigger)) {
                        Long until = this.pop_Map.get(responder.init_Str());
                        if (until == null || now >= until) {
                           this.pop_Map.put(responder.init_Str(), now + responder.init_I() + 3000L);
                           this.init_V_Z(responder.load_Z(), responder.read(), CachePool.init(responder.load_Str(), self, me), responder.init_I(), now);
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void init_V_Z(boolean useMacro, String macro, String command, int delayMs, long now) {
      this.get_Str = useMacro ? (macro == null ? "" : macro) : "";
      this.run_Str = useMacro ? "" : (command == null ? "" : command);
      this.scan = now + Math.max(0, delayMs);
   }

   private void flush_V_J(long now) {
      if (this.scan != 0L && now >= this.scan) {
         this.scan = 0L;
         String macro = this.get_Str;
         String command = this.run_Str;
         this.get_Str = "";
         this.run_Str = "";
         if (this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush) {
            if (!macro.isBlank()) {
               aM loaded = aN.init_aN().init_aM_Str(macro);
               if (loaded != null && !loaded.init_List.isEmpty()) {
                  this.init_V_aM(loaded);
               }
            } else if (!command.isBlank()) {
               this.read_Str_Str(command);
            }
         }
      }
   }

   @Override
   public int write_I() {
      return this.push_I;
   }

   @Override
   public long read_J() {
      return this.node_J;
   }

   @Override
   public List<String> init_List_J(long baselineSeq) {
      synchronized (this.close_Obj) {
         long missed = this.node_J - baselineSeq;
         int take = (int)Math.min(Math.max(0L, missed), this.init_ArrayDeque.size());
         if (take <= 0) {
            return List.of();
         }

         List<String> out = new ArrayList<>(take);
         int skip = this.init_ArrayDeque.size() - take;
         int i = 0;

         for (String line : this.init_ArrayDeque) {
            if (i++ >= skip) {
               out.add(line);
            }
         }

         return out;
      }
   }

   @Override
   public boolean init(List<String> typeRefs, boolean containerOnly, boolean centerOnPlayer, double cx, double cy, double cz, double radius) {
      Vec3 base = this.init_PositionMoveRotati.position();
      double x = centerOnPlayer ? base.x : cx;
      double y = centerOnPlayer ? base.y : cy;
      double z = centerOnPlayer ? base.z : cz;
      return this.init_StateCache.init(typeRefs, containerOnly, x, y, z, radius);
   }

   private void init_V_Z_Packet(boolean c2s, Packet<?> packet) {
      synchronized (this.open_Obj) {
         this.load_ArrayDeque.addLast(new ClassCache.Inner_PoolEntry(++this.task_J, c2s, packet));

         while (this.load_ArrayDeque.size() > 128) {
            this.load_ArrayDeque.removeFirst();
         }
      }
   }

   @Override
   public void init_V_Z(boolean on) {
      this.stop_Z = on;
      if (!on) {
         synchronized (this.open_Obj) {
            this.load_ArrayDeque.clear();
         }
      }
   }

   @Override
   public long write_J() {
      return this.task_J;
   }

   @Override
   public boolean init_Z_J_List(long baselineSeq, List<String> targets) {
      synchronized (this.open_Obj) {
         for (ClassCache.Inner_PoolEntry r : this.load_ArrayDeque) {
            if (r.init_J() > baselineSeq) {
               if (targets == null || targets.isEmpty()) {
                  return true;
               }

               String simple = r.init_Packet().getClass().getSimpleName();

               for (String target : targets) {
                  String dir = aB.init(target);
                  if ((dir.isEmpty() || dir.equalsIgnoreCase("C2S") == r.init_Z()) && aB.load(target).equalsIgnoreCase(simple)) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   @Override
   public boolean init_Z_J_aM(long baselineSeq, autismclient.util.macro.aM action) {
      synchronized (this.open_Obj) {
         for (ClassCache.Inner_PoolEntry r : this.load_ArrayDeque) {
            if (r.init_J() > baselineSeq) {
               try {
                  if (action.init_Z_Packet_Str(r.init_Packet(), r.init_Z() ? "C2S" : "S2C")) {
                     return true;
                  }
               } catch (RuntimeException var9) {
               }
            }
         }

         return false;
      }
   }

   @Override
   public boolean init_Z_ArrayUtil_Z(autismclient.util.macro.ArrayUtil target, boolean mainHand) {
      if (target != null && target.update()) {
         ItemStack stack = this.init_ItemStack_ArrayUtil(target);
         return stack != null && !stack.isEmpty() ? this.load_Z_ItemStack(stack) : true;
      } else {
         ItemStack held = this.init_ItemStack_Z(mainHand);
         return held != null && !held.isEmpty() ? this.load_Z_ItemStack(held) : false;
      }
   }

   private boolean load_Z_ItemStack(ItemStack stack) {
      Long expiry = this.open_Map.get(read_Str_ItemStack(stack));
      return expiry != null && expiry > System.currentTimeMillis();
   }

   private ItemStack init_ItemStack_Z(boolean mainHand) {
      synchronized (this.push_Obj) {
         int h = mainHand ? 36 + Math.max(0, Math.min(8, this.add_I)) : 45;
         return h >= 0 && h < this.load_List.size() ? this.load_List.get(h) : ItemStack.EMPTY;
      }
   }

   private ItemStack init_ItemStack_ArrayUtil(autismclient.util.macro.ArrayUtil target) {
      autismclient.util.macro.ArrayUtil itemOnly = target.init_ArrayUtil();
      if (itemOnly.update()) {
         itemOnly.init_I = -1;
      }

      synchronized (this.push_Obj) {
         for (int h = 9; h <= 45 && h < this.load_List.size(); h++) {
            ItemStack s = this.load_List.get(h);
            if (s != null && !s.isEmpty()) {
               int invIndex = h == 45 ? 40 : (h >= 36 ? h - 36 : h);
               if (itemOnly.init_I_ItemStack_I(s, invIndex) >= 0) {
                  return s;
               }
            }
         }
      }

      return ItemStack.EMPTY;
   }

   private static String read_Str_ItemStack(ItemStack stack) {
      UseCooldown uc = stack.get(DataComponents.USE_COOLDOWN);
      if (uc != null && uc.cooldownGroup().isPresent()) {
         return uc.cooldownGroup().get().toString();
      }

      Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      return id == null ? "" : id.toString();
   }

   @Override
   public String init_Str_TaskQueue_ArrayUtil(autismclient.util.macro.TaskQueue a, autismclient.util.macro.ArrayUtil filter) {
      ItemStack stack = this.init_ItemStack_TaskQueue_ArrayUtil(a, filter);
      if (stack != null && !stack.isEmpty()) {
         String var10000;
         switch (a.itemText == null ? autismclient.util.macro.TaskQueue.Inner_CachePool.init : a.itemText) {
            case load:
               Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
               var10000 = id == null ? "" : id.toString();
               break;
            case read:
               var10000 = write_Str_ItemStack(stack);
               break;
            default:
               var10000 = stack.getHoverName().getString();
         }

         return var10000;
      } else {
         return null;
      }
   }

   private ItemStack init_ItemStack_TaskQueue_ArrayUtil(autismclient.util.macro.TaskQueue a, autismclient.util.macro.ArrayUtil filter) {
      synchronized (this.push_Obj) {
         ItemStack var10000;
         switch (a.source) {
            case sync:
               var10000 = this.init_ItemStack;
               break;
            case flush:
               int h = 36 + Math.max(0, Math.min(8, this.add_I));
               var10000 = h < this.load_List.size() ? this.load_List.get(h) : ItemStack.EMPTY;
               break;
            case write:
               var10000 = this.init_ItemStack_ArrayUtil(filter, a.slot, true);
               break;
            case update:
               var10000 = this.init_ItemStack_ArrayUtil(filter, a.slot, false);
               break;
            default:
               var10000 = null;
         }

         return var10000;
      }
   }

   private ItemStack init_ItemStack_ArrayUtil(autismclient.util.macro.ArrayUtil filter, int slot, boolean guiRegion) {
      int size = this.init_List.size();
      int base = this.close_I >= 0 ? size - 36 : 0;

      for (int h = 0; h < size; h++) {
         ItemStack s = this.init_List.get(h);
         if (s != null && !s.isEmpty()) {
            boolean playerSlot = this.close_I < 0 || h >= base;
            if (guiRegion != playerSlot) {
               int visible = this.load_I_I_I(h, size);
               if ((slot < 0 || slot == visible || slot == h) && (filter == null || !filter.update() || filter.init_I_ItemStack_I(s, visible) >= 0)) {
                  return s;
               }
            }
         }
      }

      return ItemStack.EMPTY;
   }

   private static String write_Str_ItemStack(ItemStack stack) {
      ItemLore lore = stack.get(DataComponents.LORE);
      if (lore == null) {
         return "";
      }

      StringBuilder sb = new StringBuilder();

      for (Component line : lore.lines()) {
         if (sb.length() > 0) {
            sb.append('\n');
         }

         sb.append(line.getString());
      }

      return sb.toString();
   }

   @Override
   public List<String> init_List_Z(boolean excludeSelf) {
      UUID self = this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
         ? this.init_SyncManager_DataUt.init_User().getProfileId()
         : null;
      List<String> names = new ArrayList<>(this.init_Set.size());

      for (UUID profileId : this.init_Set) {
         if (!excludeSelf || self == null || !self.equals(profileId)) {
            String name = this.load_Map.get(profileId);
            if (name != null && !name.isBlank() && !names.contains(name)) {
               names.add(name);
            }
         }
      }

      names.sort(String.CASE_INSENSITIVE_ORDER);
      return List.copyOf(names);
   }

   @Override
   public int load_I_Str(String command) {
      String query = command == null ? "" : command;
      return this.write_I_Str(query);
   }

   @Override
   public List<String> init_List_I(int requestId) {
      ClassCache.Inner_TaskTimer suggest;
      synchronized (this.flush_Obj) {
         suggest = this.close_Map.remove(requestId);
      }

      return suggest == null ? null : List.copyOf(suggest.init_List());
   }

   @Override
   public List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> init_List() {
      synchronized (this.init_Obj) {
         String objective = this.data_Str();
         if (objective != null && !objective.isBlank()) {
            Map<String, ClassCache.Inner_RefQueue> tracked = this.write_Map.get(objective);
            if (tracked != null && !tracked.isEmpty()) {
               ClassCache.Inner_BitSetUtil objectiveData = this.read_Map.getOrDefault(objective, new ClassCache.Inner_BitSetUtil(objective, Optional.empty()));
               List<Entry<String, ClassCache.Inner_RefQueue>> entries = new ArrayList<>(tracked.entrySet());
               entries.removeIf(entryx -> entryx.getKey().startsWith("#"));
               entries.sort((a, b) -> {
                  int scorex = Integer.compare(b.getValue().init_I(), a.getValue().init_I());
                  return scorex != 0 ? scorex : a.getKey().compareToIgnoreCase(b.getKey());
               });
               List<autismclient.util.macro.TaskQueue.Inner_JsonUtil> lines = new ArrayList<>(Math.min(15, entries.size()));

               for (int i = 0; i < entries.size() && i < 15; i++) {
                  Entry<String, ClassCache.Inner_RefQueue> entry = entries.get(i);
                  String owner = entry.getKey();
                  ClassCache.Inner_RefQueue score = entry.getValue();
                  String baseName = score.init_Str() != null && !score.init_Str().isBlank() ? score.init_Str() : owner;
                  ClassCache.Inner_CharEncoder team = this.sync_Map.get(this.flush_Map.get(owner));
                  String name = team == null ? baseName : team.init_Str() + baseName + team.load();
                  NumberFormat format = score.init_Optional().orElseGet(() -> objectiveData.init_Optional().orElse(StyledFormat.SIDEBAR_DEFAULT));
                  String scoreText = init_Str_Component(format.format(score.init_I()));
                  lines.add(
                     new autismclient.util.macro.TaskQueue.Inner_JsonUtil(
                        objective + "\u001f" + owner,
                        i,
                        objective,
                        objectiveData.init_Str(),
                        owner,
                        name,
                        scoreText,
                        scoreText.isEmpty() ? name : name + ": " + scoreText
                     )
                  );
               }

               return List.copyOf(lines);
            } else {
               return List.of();
            }
         } else {
            return List.of();
         }
      }
   }

   private String data_Str() {
      String selfName = this.init_SyncManager_DataUt != null && this.init_SyncManager_DataUt.init_User() != null
         ? this.init_SyncManager_DataUt.init_User().getName()
         : "";
      ClassCache.Inner_CharEncoder team = this.sync_Map.get(this.flush_Map.get(selfName));
      if (team != null && team.init_Optional().isPresent()) {
         String teamObjective = this.update_Map.get(team.init_Optional().get().displaySlot());
         if (teamObjective != null && !teamObjective.isBlank()) {
            return teamObjective;
         }
      }

      return this.update_Map.getOrDefault(DisplaySlot.SIDEBAR, "");
   }

   @Override
   public boolean init_Z_aA(autismclient.util.macro.aA action) {
      return this.init_ClassCache_TokenSt.init_Z_ClassCache_aA(this, action);
   }

   private void prev_V() {
      if (this.stop_Z) {
         this.init_V_Z(false);
      }

      if (this.valid_Z) {
         this.load_V_Z(false);
      }
   }

   @Override
   public boolean init_Z_ac(ac a, String l1, String l2, String l3, String l4) {
      if (a == null) {
         return false;
      }

      BlockPos pos;
      boolean front;
      switch (a.targetMode) {
         case read:
            pos = new BlockPos(a.f_x_I, a.f_y_I, a.f_z_I);
            front = a.frontText;
            break;
         case load:
            pos = this.load_BlockPos;
            front = a.frontText;
            break;
         default:
            pos = this.init_BlockPos;
            front = this.prev_Z;
      }

      if (pos == null) {
         return false;
      }

      boolean sent = this.init_Z_Packet(Compat263.newSignUpdatePacket(pos, front, l1, l2, l3, l4), false, false);
      if (sent) {
         this.next_Z = false;
      }

      ac.Inner_DataUtil mode = a.closeMode == null ? ac.Inner_DataUtil.init : a.closeMode;
      if (mode == ac.Inner_DataUtil.write) {
         this.update_Z_I(a.closePacketContainerId);
      } else if (mode == ac.Inner_DataUtil.read) {
         this.update_Z_I(this.close_I >= 0 ? this.close_I : 0);
      }

      return sent;
   }

   private void init_V_Str(String id, double x, double y, double z) {
      synchronized (this.poll_Obj) {
         this.read_ArrayDeque.addLast(new ClassCache.Inner_MethodUtil(++this.pool_J, id == null ? "" : id, x, y, z));

         while (this.read_ArrayDeque.size() > 64) {
            this.read_ArrayDeque.removeFirst();
         }
      }
   }

   @Override
   public void load_V_Z(boolean on) {
      this.valid_Z = on;
      if (!on) {
         synchronized (this.poll_Obj) {
            this.read_ArrayDeque.clear();
         }
      }
   }

   @Override
   public long update_J() {
      return this.pool_J;
   }

   @Override
   public boolean init_Z_J(long baselineSeq, List<String> ids, boolean checkDistance, double maxDistance) {
      Vec3 self = this.init_PositionMoveRotati.position();
      double maxSq = maxDistance * maxDistance;
      synchronized (this.poll_Obj) {
         Iterator var11 = this.read_ArrayDeque.iterator();

         while (true) {
            ClassCache.Inner_MethodUtil r;
            while (true) {
               if (!var11.hasNext()) {
                  return false;
               }

               r = (ClassCache.Inner_MethodUtil)var11.next();
               if (r.init_J() > baselineSeq) {
                  if (!checkDistance) {
                     break;
                  }

                  double dx = r.init_D() - self.x;
                  double dy = r.load() - self.y;
                  double dz = r.read() - self.z;
                  if (!(dx * dx + dy * dy + dz * dz > maxSq)) {
                     break;
                  }
               }
            }

            if (ids == null || ids.isEmpty()) {
               return true;
            }

            for (String want : ids) {
               if (want != null && !want.isBlank() && read_Z_Str_Str(want, r.init_Str())) {
                  return true;
               }
            }
         }
      }
   }

   private static boolean read_Z_Str_Str(String want, String actual) {
      String w = want.trim().toLowerCase(Locale.ROOT);
      String a = actual == null ? "" : actual.trim().toLowerCase(Locale.ROOT);
      if (w.equals(a)) {
         return true;
      }

      String wp = w.contains(":") ? w.substring(w.indexOf(58) + 1) : w;
      String ap = a.contains(":") ? a.substring(a.indexOf(58) + 1) : a;
      return wp.equals(ap);
   }

   @Override
   public int read_I_Str(String type) {
      return this.init_StateCache.init_I_Str_Vec3(type, this.init_PositionMoveRotati.position());
   }

   @Override
   public double[] init_DArr_I(int entityId) {
      return this.init_StateCache.init_DArr_I(entityId);
   }

   @Override
   public String init_Str_Str_Str(String name, String args) {
      return this.load_Str_Str_Str(name, args);
   }

   @Override
   public void init_V_ao_DataUtil_Z(ao.Inner_DataUtil phase, boolean offhand) {
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      PositionMoveRotation current = this.init_PositionMoveRotati;
      switch (phase == null ? ao.Inner_DataUtil.init : phase) {
         case init:
         case load:
         case write:
            this.init_Z_Packet(new ServerboundUseItemPacket(hand, ++this.reset_I, current.yRot(), current.xRot()), false, false);
            break;
         case read:
            this.init_Z_Packet(
               new ServerboundPlayerActionPacket(
                  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN
               ),
               false,
               false
            );
            break;
         case update:
            this.init_Z_Packet(new CompatSwingPacket(hand), false, false);
      }
   }

   @Override
   public String load_Str_Str(String message) {
      return this.read_Str_Str(message);
   }

   @Override
   public boolean init_Z_Str(String macroName) {
      aM found = aN.init_aN().init_aM_Str(macroName);
      if (found != null && found.init_List != null && !found.init_List.isEmpty()) {
         aM copy = found.init_aM();
         this.init_ClassCache_TokenSt.init_V_ClassCache_aM(this, copy);
         this.init_V_aM(copy);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void write_V() {
      this.sync_V();
   }

   @Override
   public void read_V_Str(String reason) {
      this.init_ClassCache_TokenSt.read_V_ClassCache(this);
      this.sync_V();
      this.update_V_Str(reason == null ? "Macro" : reason);
   }

   @Override
   public float write_F() {
      return this.init_PositionMoveRotati.yRot();
   }

   @Override
   public void init_V_F_F(float yaw, float pitch) {
      synchronized (this.sync_Obj) {
         this.init_PositionMoveRotati = this.init_PositionMoveRotati.withRotation(yaw, pitch);
      }

      this.init_Z_Packet(new Rot(yaw, pitch, true, false), false, true);
   }

   @Override
   public void init_V_D(double worldDx, double worldDz, long durationMs) {
      this.poll_D = worldDx;
      this.push_D = worldDz;
      this.flush_V_Z(this.set_Z);
      long now = System.currentTimeMillis();
      long until = now + Math.max(0L, durationMs);
      this.clear_J = until;
      this.reset_J = Math.max(this.reset_J, until);
      this.run_Z = true;
      this.start_J = now + 500L;
      this.sync_I = 0;
      this.stop_J = 0L;
      this.poll_Z = false;
      this.add_J = Math.max(this.add_J, until);
   }

   private void close_V_J(long now) {
      this.sync_I = now - this.stop_J <= 150L ? this.sync_I + 1 : 1;
      this.stop_J = now;
      if (this.sync_I >= 8) {
         this.clear_J = 0L;
         this.sync_I = 0;
         if (now - this.next_J > 30000L) {
            this.next_J = now;
            this.push("Walk blocked by terrain; move cancelled early. Clear the path or use a clip action.");
         }
      }
   }

   @Override
   public int init_I_D(double dx, double dy, double dz, int segments, boolean onGround) {
      int count = Math.max(1, Math.min(64, segments));
      synchronized (this.tick_Obj) {
         this.frame_Z = false;
         List<A.Inner_DataUtil> steps = new ArrayList<>();
         synchronized (this.sync_Obj) {
            Vec3 origin = this.init_PositionMoveRotati.position();
            Vec3 from = origin;

            for (int i = 1; i <= count; i++) {
               double progress = (double)i / count;
               Vec3 next = new Vec3(origin.x + dx * progress, origin.y + dy * progress, origin.z + dz * progress);

               for (A.Inner_DataUtil step : A.init(from, next, onGround)) {
                  steps.add(step);
                  from = step.init_Vec3();
               }
            }

            if (steps.isEmpty()) {
               return 0;
            }

            synchronized (this.run_Obj) {
               this.update_ArrayDeque.clear();
               long generation = ++this.f_d_J;

               for (A.Inner_DataUtil step : steps) {
                  this.update_ArrayDeque.add(new ClassCache.Inner_BufferHelper(step, generation, this.f_e_J));
               }
            }
         }

         this.write_D = 0.0;
         this.poll_D = 0.0;
         this.push_D = 0.0;
         this.clear_J = 0L;
         this.run_Z = false;
         this.poll_Z = false;
         this.read_Z_J(System.currentTimeMillis());
         return this.frame_Z ? -1 : steps.size();
      }
   }

   @Override
   public boolean open_Z() {
      synchronized (this.run_Obj) {
         return !this.update_ArrayDeque.isEmpty();
      }
   }

   @Override
   public boolean poll_Z() {
      return this.frame_Z;
   }

   @Override
   public long flush_J() {
      int remaining;
      synchronized (this.run_Obj) {
         remaining = this.update_ArrayDeque.size();
      }

      return this.init_DataPipe.init(System.nanoTime(), remaining);
   }

   private void valid_V() {
      synchronized (this.run_Obj) {
         this.update_ArrayDeque.clear();
      }
   }

   private boolean read_Z_J(long now) {
      while (true) {
         ClassCache.Inner_BufferHelper entry;
         synchronized (this.sync_Obj) {
            label118: {
               boolean var10000;
               synchronized (this.run_Obj) {
                  entry = this.update_ArrayDeque.peek();
                  if (entry == null) {
                     return false;
                  }

                  if (entry.init_J() == this.f_d_J && entry.load() == this.f_e_J) {
                     break label118;
                  }

                  var10000 = false;
               }

               return var10000;
            }
         }

         A.Inner_DataUtil step = entry.init_A_DataUtil();
         ClassCache.Inner_HashUtil result = this.init_ClassCache_HashUti_Packet(new Pos(step.init_Vec3(), step.init_Z(), false), false, false);
         if (result == ClassCache.Inner_HashUtil.load) {
            return true;
         }

         synchronized (this.sync_Obj) {
            boolean var16;
            synchronized (this.run_Obj) {
               if (this.update_ArrayDeque.peek() != entry || entry.init_J() != this.f_d_J || entry.load() != this.f_e_J) {
                  return false;
               }

               this.update_ArrayDeque.poll();
               if (result != ClassCache.Inner_HashUtil.read) {
                  this.init_PositionMoveRotati = new PositionMoveRotation(
                     step.init_Vec3(), Vec3.ZERO, this.init_PositionMoveRotati.yRot(), this.init_PositionMoveRotati.xRot()
                  );
                  this.open_Z = step.init_Z();
                  this.value_J = now;
                  this.init_TableIndex.load(step.init_Vec3(), now);
                  continue;
               }

               this.frame_Z = true;
               this.update_ArrayDeque.clear();
               var16 = false;
            }

            return var16;
         }
      }
   }

   @Override
   public void read_V_Z(boolean on) {
      this.get_Z = on;
      this.size_V();
   }

   @Override
   public void write_V_Z(boolean on) {
      this.set_Z = on;
      this.size_V();
      this.flush_V_Z(on);
      synchronized (this.sync_Obj) {
         if (System.currentTimeMillis() < this.clear_J) {
            double mag = Math.sqrt(this.poll_D * this.poll_D + this.push_D * this.push_D);
            if (mag > 1.0E-6) {
               double target = on ? 0.26 : 0.2;
               this.poll_D = this.poll_D / mag * target;
               this.push_D = this.push_D / mag * target;
            }
         }
      }
   }

   @Override
   public boolean push_Z() {
      return this.set_Z;
   }

   private void size_V() {
      this.init_Z_Packet(new ServerboundPlayerInputPacket(new Input(false, false, false, false, false, this.get_Z, this.set_Z)), false, false);
   }

   private void flush_V_Z(boolean on) {
      if (this.size_I >= 0 && this.put_Z != on) {
         this.put_Z = on;
         ByteBuf raw = Unpooled.buffer();

         try {
            FriendlyByteBuf buf = new FriendlyByteBuf(raw);
            buf.writeVarInt(this.size_I);
            buf.writeEnum(
               on
                  ? net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.START_SPRINTING
                  : net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.STOP_SPRINTING
            );
            buf.writeVarInt(0);
            ServerboundPlayerCommandPacket packet = ServerboundPlayerCommandPacket.STREAM_CODEC.decode(buf);
            this.init_Z_Packet(packet, false, false);
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("[Multi] {} sprint announce failed", this.init_FieldCache_BufferH.init_Str(), error);
         } finally {
            raw.release();
         }
      }
   }

   @Override
   public void update_V() {
      long now = System.currentTimeMillis();
      this.write_D = 0.42;
      this.run_Z = false;
      this.open_Z = false;
      this.poll_Z = false;
      this.f_l_J = now + 15000L;
      this.block_Z = true;
      this.add_J = Math.max(this.add_J, now + 1200L);
      this.reset_J = Math.max(this.reset_J, now + 15000L);
   }

   @Override
   public String init_Str_I_Z(int entityId, boolean attack) {
      if (entityId < 0) {
         return "No entity";
      }

      if (!attack) {
         this.run_I = entityId;
         this.load_BlockPos = null;
      }

      if (attack) {
         boolean sent = this.write_Z_I(entityId);
         return sent ? "Sent" : "attack packet blocked or disconnected";
      } else {
         boolean sent = this.init_Z_Packet(new ServerboundInteractPacket(entityId, InteractionHand.MAIN_HAND, Vec3.ZERO, false), false, false);
         return sent ? "Sent" : "interact packet blocked or disconnected";
      }
   }

   @Override
   public String init_Str_I_2(int x, int y, int z, String face) {
      Direction dir = init_Direction_Str(face);
      BlockPos pos = new BlockPos(x, y, z);
      this.load_BlockPos = pos;
      this.run_I = -1;
      Vec3 hitVec = new Vec3(x + 0.5 + dir.getStepX() * 0.5, y + 0.5 + dir.getStepY() * 0.5, z + 0.5 + dir.getStepZ() * 0.5);
      this.init_Z_Packet(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, dir, pos, false), ++this.reset_I), false, false);
      return "Sent";
   }

   @Override
   public String load(int x, int y, int z, String face) {
      Direction dir = init_Direction_Str(face);
      BlockPos pos = new BlockPos(x, y, z);
      this.init_Z_Packet(
         new ServerboundPlayerActionPacket(
            net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, dir, ++this.reset_I
         ),
         false,
         false
      );
      this.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND), false, false);
      this.init_Z_Packet(
         new ServerboundPlayerActionPacket(
            net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, dir, ++this.reset_I
         ),
         false,
         false
      );
      return "Sent";
   }

   private static Direction init_Direction_Str(String name) {
      if (name != null) {
         try {
            return Direction.valueOf(name.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
         }
      }

      return Direction.UP;
   }

   private static String init_Str_EntityType(EntityType<?> type) {
      try {
         return BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
      } catch (RuntimeException ignored) {
         return "";
      }
   }

   private static String flush_Str_Str(String query) {
      if (query == null) {
         return "";
      }

      String q = query.trim().toLowerCase(Locale.ROOT);
      int colon = q.indexOf(58);
      if (colon >= 0) {
         q = q.substring(colon + 1);
      }

      return q.replace('_', ' ').trim();
   }

   private static boolean init_Z_ItemStack_Str(ItemStack stack, String normalizedQuery) {
      if (normalizedQuery.isEmpty()) {
         return true;
      }

      String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT).replace('_', ' ');
      if (name.contains(normalizedQuery)) {
         return true;
      }

      try {
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT).replace('_', ' ');
         return id.contains(normalizedQuery);
      } catch (RuntimeException ignored) {
         return false;
      }
   }

   void init_V_J(long now) {
      ClassCache.Inner_PathHelper currentStatus = this.init_AtomicReference.get();
      if (currentStatus == ClassCache.Inner_PathHelper.init && this.init_Connection == null) {
         if (this.flags_Z) {
            this.sync_V_J(now);
         }
      } else if (currentStatus != ClassCache.Inner_PathHelper.close && currentStatus != ClassCache.Inner_PathHelper.open) {
         this.init_TaskQueue.init();
         Connection current = this.init_Connection;
         if (current != null && !current.isConnecting()) {
            current.tick();
         }

         this.init_MapStore.init_V_J(now);
         this.write_V_J(now);
         if (this.count_Z) {
            this.cache_I = Math.min(2, this.cache_I + 1);
            this.state_I = this.init_Z() && this.init_StreamUtil.init_CustomMenuSnapshot() == null && !this.update_Z() ? Math.min(2, this.state_I + 1) : 0;
         }

         this.sync_V_J(now);
         if (!this.level_Z) {
            this.init_TaskQueue_2.load_V(now);
            if (this.init_FieldCache_DataUti == FieldCache.Inner_DataUtil.load) {
               this.init_CachePool.load(now);
            }

            this.flush_V_J(now);
         }

         if (this.entry_Z && this.init_TextFormat != null && now >= this.store && !this.init_TextFormat.read_Z()) {
            this.sync_V();
         }

         ClassCache.Inner_PathHelper afterNetworkTick = this.init_AtomicReference.get();
         if (afterNetworkTick != ClassCache.Inner_PathHelper.init
            && afterNetworkTick != ClassCache.Inner_PathHelper.flush
            && afterNetworkTick != ClassCache.Inner_PathHelper.close
            && afterNetworkTick != ClassCache.Inner_PathHelper.open
            && now - this.pop_J >= 60000L
            && (this.init_TextFormat == null || !this.init_TextFormat.init_Z_J(now))
            && !this.data_Z()) {
            this.open("Timed out during " + this.read_Str);
         } else if (!this.load_AtomicBoolean.get() && this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush && this.write_Z) {
            this.update_V_J(now);
            this.reset_V();
            if (this.load_F <= 0.0F) {
               this.valid_V();
               if (this.frame_J == 0L) {
                  this.frame_J = now + 1000L;
               } else if (now >= this.frame_J) {
                  this.init_Z_Packet(
                     new ServerboundClientCommandPacket(net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action.PERFORM_RESPAWN),
                     false,
                     false
                  );
                  this.frame_J = now + 1000L;
               }
            } else {
               this.frame_J = 0L;
               boolean clipPending;
               synchronized (this.tick_Obj) {
                  clipPending = this.read_Z_J(now);
               }

               if (!clipPending && (!this.level_Z || this.put_Z())) {
                  IoWorker activePolicy = this.init_IoWorker;
                  if (this.tick_Z) {
                     this.tick_Z = false;
                     if (!this.push_Z && !this.pop_Z && now >= this.clear_J) {
                        synchronized (this.sync_Obj) {
                           if (this.write_D > 0.0) {
                              this.write_D = 0.0;
                           }
                        }

                        this.open_Z = false;
                        this.poll_Z = true;
                        this.f_l_J = now + 15000L;
                        this.block_Z = true;
                        this.add_J = Math.max(this.add_J, now + 5000L);
                     }
                  }

                  boolean walking = now < this.clear_J;
                  if (this.block_Z) {
                     if (now >= this.f_l_J) {
                        this.block_Z = false;
                        this.open_Z = true;
                        synchronized (this.sync_Obj) {
                           this.write_D = 0.0;
                        }
                     } else {
                        this.add_J = Math.max(this.add_J, now + 800L);
                     }
                  }

                  boolean moving = now < this.add_J || walking;
                  if (this.push_Z) {
                     this.queue();
                  } else if (this.pop_Z) {
                     if (now >= this.put_J) {
                        this.pop_Z = false;
                        this.open_Z = true;
                     } else {
                        this.open_Z = false;
                        this.load_Z_Z(false);
                     }
                  } else if (moving) {
                     synchronized (this.sync_Obj) {
                        if (walking) {
                           Vec3 wp = this.init_PositionMoveRotati.position();
                           this.init_PositionMoveRotati = new PositionMoveRotation(
                              new Vec3(wp.x + this.poll_D, wp.y, wp.z + this.push_D),
                              this.init_PositionMoveRotati.deltaMovement(),
                              this.init_PositionMoveRotati.yRot(),
                              this.init_PositionMoveRotati.xRot()
                           );
                           if (this.run_Z && now >= this.start_J) {
                              this.run_Z = false;
                           }
                        }

                        if (this.level_I > 0) {
                           this.level_I--;
                           Vec3 kp = this.init_PositionMoveRotati.position();
                           this.init_PositionMoveRotati = new PositionMoveRotation(
                              new Vec3(kp.x + this.run_D, kp.y, kp.z + this.get_D),
                              this.init_PositionMoveRotati.deltaMovement(),
                              this.init_PositionMoveRotati.yRot(),
                              this.init_PositionMoveRotati.xRot()
                           );
                           this.run_D *= 0.6;
                           this.get_D *= 0.6;
                        }

                        if (walking && this.run_Z && this.write_D <= 0.0) {
                           this.write_D = 0.0;
                           this.open_Z = true;
                        } else if (this.poll_Z) {
                           this.poll_Z = false;
                           this.open_Z = false;
                        } else if (walking) {
                           this.data_V();
                        } else if (this.block_Z) {
                           this.cache_V();
                        } else {
                           this.value_V();
                        }
                     }

                     this.load_Z_Z(false);
                  } else if (init_Z_Z(activePolicy.load_Z(), this.value_J, now)) {
                     this.load_Z_Z(false);
                  }

                  if (activePolicy.read() && !activePolicy.load_Z() && !moving && now - this.size_J >= 1000L) {
                     this.size_J = now;
                     PositionMoveRotation p = this.init_PositionMoveRotati;
                     this.init_Z_Packet(new Rot(p.yRot(), p.xRot(), true, false), false, true);
                  }

                  if (activePolicy.write() && now - this.data_J >= 1000L) {
                     this.data_J = now;
                     this.init_Z_Packet(new CompatSwingPacket(InteractionHand.MAIN_HAND), false, false);
                  }
               }
            }
         }
      } else if (this.flags_Z || this.add_Z()) {
         this.sync_V_J(now);
      }
   }

   private void data_V() {
      double next = (this.write_D - 0.08) * 0.98;
      this.write_D = Math.max(-3.92, next);
      Vec3 p = this.init_PositionMoveRotati.position();
      this.init_PositionMoveRotati = new PositionMoveRotation(
         new Vec3(p.x, p.y + this.write_D, p.z),
         this.init_PositionMoveRotati.deltaMovement(),
         this.init_PositionMoveRotati.yRot(),
         this.init_PositionMoveRotati.xRot()
      );
      this.open_Z = true;
   }

   private void value_V() {
      Vec3 p = this.init_PositionMoveRotati.position();
      double top = this.init_D_D(p.x, p.y, p.z);
      if (Double.isNaN(top)) {
         this.open_Z = true;
         this.write_D = 0.0;
      } else {
         double next = (this.write_D - 0.08) * 0.98;
         this.write_D = Math.max(-3.92, next);
         double newY = p.y + this.write_D;
         if (newY <= top) {
            newY = top;
            this.write_D = 0.0;
            this.open_Z = true;
         } else {
            this.open_Z = false;
         }

         this.init_PositionMoveRotati = new PositionMoveRotation(
            new Vec3(p.x, newY, p.z), this.init_PositionMoveRotati.deltaMovement(), this.init_PositionMoveRotati.yRot(), this.init_PositionMoveRotati.xRot()
         );
      }
   }

   private void cache_V() {
      double next = (this.write_D - 0.08) * 0.98;
      this.write_D = Math.max(-3.92, next);
      Vec3 p = this.init_PositionMoveRotati.position();
      double newY = p.y + this.write_D;
      double top = this.init_D_D(p.x, p.y, p.z);
      if (!Double.isNaN(top) && newY <= top) {
         newY = top;
         this.write_D = 0.0;
         this.open_Z = true;
         this.block_Z = false;
      } else {
         this.open_Z = false;
      }

      this.init_PositionMoveRotati = new PositionMoveRotation(
         new Vec3(p.x, newY, p.z), this.init_PositionMoveRotati.deltaMovement(), this.init_PositionMoveRotati.yRot(), this.init_PositionMoveRotati.xRot()
      );
   }

   private double init_D_D(double x, double y, double z) {
      int bx = (int)Math.floor(x);
      int bz = (int)Math.floor(z);
      double best = 0.0 / 0.0;

      for (Entry<Long, BlockState> e : this.poll_Map.entrySet()) {
         long key = e.getKey();
         if (BlockPos.getX(key) == bx && BlockPos.getZ(key) == bz) {
            BlockState st = e.getValue();
            if (st != null && !st.isAir()) {
               double h = init_D_BlockState(st);
               if (!(h <= 0.0)) {
                  double topY = BlockPos.getY(key) + h;
                  if (topY <= y + 0.5 && (Double.isNaN(best) || topY > best)) {
                     best = topY;
                  }
               }
            }
         }
      }

      return best;
   }

   private static double init_D_BlockState(BlockState st) {
      try {
         VoxelShape shape = st.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         return shape.isEmpty() ? 0.0 : shape.max(Axis.Y);
      } catch (Throwable t) {
         return 1.0;
      }
   }

   private void init_V_ClientboundSetPass(ClientboundSetPassengersPacket passengers) {
      int veh = passengers.getVehicle();
      boolean mountsUs = false;

      for (int p : passengers.getPassengers()) {
         if (p == this.size_I) {
            mountsUs = true;
            break;
         }
      }

      if (mountsUs) {
         double[] vp = this.init_StateCache.init_DArr_I(veh);
         synchronized (this.sync_Obj) {
            this.update_I = veh;
            this.push_Z = true;
            if (vp != null) {
               this.update_D = vp[0];
               this.sync_D = vp[1];
               this.flush_D = vp[2];
            } else {
               Vec3 pp = this.init_PositionMoveRotati.position();
               this.update_D = pp.x;
               this.sync_D = pp.y;
               this.flush_D = pp.z;
            }

            this.close_D = 0.0;
            this.init_F = this.init_PositionMoveRotati.yRot();
            this.open_Z = false;
            this.poll_Z = false;
            this.pop_Z = false;
            this.block_Z = false;
            this.level_I = 0;
         }

         this.add_J = Math.max(this.add_J, System.currentTimeMillis() + 5000L);
      } else if (this.push_Z && veh == this.update_I) {
         this.state_V();
      }
   }

   private void state_V() {
      if (this.push_Z) {
         this.push_Z = false;
         long now = System.currentTimeMillis();
         synchronized (this.sync_Obj) {
            this.open_Z = false;
            this.write_D = 0.0;
            this.poll_Z = false;
            this.pop_Z = true;
            this.block_Z = false;
            this.level_I = 0;
         }

         this.put_J = now + 1000L;
         this.add_J = Math.max(this.add_J, now + 2000L);
      }
   }

   private void queue() {
      if (this.init_DataPipe.init_Z(System.nanoTime())) {
         float yaw;
         float pitch;
         double vx;
         double vy;
         double vz;
         synchronized (this.sync_Obj) {
            this.close_D -= 0.03999999910593033;
            this.sync_D = this.sync_D + this.close_D;
            this.init_F += 3.0F;
            yaw = this.init_F;
            pitch = this.init_PositionMoveRotati.xRot();
            vx = this.update_D;
            vy = this.sync_D;
            vz = this.flush_D;
         }

         this.init_Z_Packet(new ServerboundPaddleBoatPacket(false, false), false, false);
         this.init_Z_Packet(Compat263.newServerboundMoveVehiclePacket(new Vec3(vx, vy, vz), yaw, pitch, false), false, true);
         this.init_Z_Packet(new Rot(yaw, pitch, false, false), false, true);
         this.init_Z_Packet(ServerboundClientTickEndPacket.INSTANCE, true, false);
      }
   }

   static boolean init_Z_Z(boolean enabled, long lastMovementAt, long now) {
      return enabled && now - lastMovementAt >= 1000L;
   }

   private void init_V_PositionMoveRotati(PositionMoveRotation current) {
      if (this.init_Z_Packet(new PosRot(current.position(), current.yRot(), current.xRot(), false, false), true, true)) {
         this.value_J = System.currentTimeMillis();
         this.init_TableIndex.load(current.position(), this.value_J);
      }
   }

   private boolean load_Z_Z(boolean critical) {
      PositionMoveRotation current = this.init_PositionMoveRotati;
      if (this.init_ClassCache_HashUti_Packet(new PosRot(current.position(), current.yRot(), current.xRot(), this.open_Z, false), critical, critical)
         != ClassCache.Inner_HashUtil.init) {
         return false;
      }

      this.value_J = System.currentTimeMillis();
      this.init_TableIndex.load(current.position(), this.value_J);
      return true;
   }

   private ClassCache.Inner_HashUtil init_ClassCache_HashUti_Packet(Packet<?> movement, boolean critical, boolean ungated) {
      synchronized (this.pop_Obj) {
         if (!ungated && !this.init_DataPipe.init_Z(System.nanoTime())) {
            return ClassCache.Inner_HashUtil.load;
         }

         if (!this.init_Z_Packet(movement, critical, true)) {
            return ClassCache.Inner_HashUtil.read;
         }

         this.init_Z_Packet(ServerboundClientTickEndPacket.INSTANCE, true, false);
         return ClassCache.Inner_HashUtil.init;
      }
   }

   private void buffer() {
      int offset;
      synchronized (this.update_Obj) {
         offset = this.init_LastSeenMessagesTr.getAndClearOffset();
      }

      if (offset > 0) {
         this.init_Z_Packet(new ServerboundChatAckPacket(offset), true, false);
      }
   }

   private boolean init_Z_Packet(Packet<?> packet, boolean critical, boolean movementPacket) {
      return this.init_Z_Connection(this.init_Connection, packet, critical, movementPacket);
   }

   private boolean init_Z_Connection(Connection current, Packet<?> packet, boolean critical, boolean movementPacket) {
      return this.init_Z_Connection(current, packet, critical, movementPacket, false);
   }

   private boolean init_Z_Connection(Connection current, Packet<?> packet, boolean critical, boolean movementPacket, boolean loginQueryAnswer) {
      if (packet != null && current != null && !this.load_AtomicBoolean.get()) {
         if (packet instanceof ServerboundContainerClosePacket close && close.getContainerId() == 0 && this.queue_Z) {
            this.set_V();
            if (this.buffer_Z) {
               return true;
            }
         }

         if (!this.init_IoWorker.init_Z_IoWorker_DataUtil(IoWorker.Inner_DataUtil.init, packet.getClass().getName(), critical, movementPacket)) {
            return false;
         }

         if (this.stop_Z) {
            this.init_V_Z_Packet(true, packet);
         }

         if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
            current.send(packet);
            this.init_TaskQueue.load(packet.getClass());
            return true;
         }

         Channel channel = ((AutismClientConnectionAccessor)current).getChannel();
         if (channel == null) {
            return false;
         }

         if (loginQueryAnswer && packet instanceof ServerboundCustomQueryAnswerPacket answer) {
            autismclient.g.ConfigStore.init(current, answer);
         }

         boolean accepted = autismclient.g.ListPool.init(current, channel, packet);
         if (accepted) {
            this.init_TaskQueue.load(packet.getClass());
         }

         return accepted;
      } else {
         return false;
      }
   }

   private MapStore.Inner_JsonUtil init_MapStore_JsonUtil() {
      synchronized (this.load_Obj) {
         Connection current = this.init_Connection;
         if (current == null) {
            return null;
         }

         long generation = this.init_ClassCache_ConfigS == null ? 0L : this.init_ClassCache_ConfigS.init_J;
         return new MapStore.Inner_JsonUtil(current, this.read_I, this.init_MapStore_CachePool, generation);
      }
   }

   private MapStore.Inner_JsonUtil init_MapStore_JsonUtil_ClassCache_IoWorke(
      ClassCache.Inner_IoWorker phase, Connection current, int epoch, ClassCache.@Nullable Inner_ConfigStore attachment
   ) {
      MapStore.Inner_CachePool var10000;
      switch (phase) {
         case init_ClassCache_IoWorke:
            var10000 = MapStore.Inner_CachePool.init;
            break;
         case load:
            var10000 = MapStore.Inner_CachePool.load;
            break;
         case read:
            var10000 = MapStore.Inner_CachePool.read;
            break;
         default:
            throw new MatchException(null, null);
      }

      MapStore.Inner_CachePool resourcePhase = var10000;
      long generation = phase != ClassCache.Inner_IoWorker.init_ClassCache_IoWorke && attachment != null ? attachment.init_J : 0L;
      return new MapStore.Inner_JsonUtil(current, epoch, resourcePhase, generation);
   }

   private boolean init_Z_MapStore_JsonUtil_2(MapStore.Inner_JsonUtil target, Packet<?> packet, boolean critical, boolean movementPacket) {
      if (target == null) {
         return false;
      }

      synchronized (this.load_Obj) {
         if (!this.init_Z_MapStore_JsonUtil(target)) {
            return false;
         }
      }

      return this.init_Z_Connection(target.init_Connection(), packet, critical, movementPacket);
   }

   private boolean init_Z_MapStore_JsonUtil_ServerboundCustomQ(MapStore.Inner_JsonUtil target, ServerboundCustomQueryAnswerPacket answer) {
      if (target != null && answer != null) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_MapStore_JsonUtil(target)) {
               return false;
            }
         }

         return this.init_Z_Connection(target.init_Connection(), answer, true, false, true);
      } else {
         return false;
      }
   }

   private boolean init_Z_MapStore_JsonUtil(MapStore.Inner_JsonUtil captured, ClassCache.Inner_ConfigStore replacement, Packet<?> packet) {
      if (captured != null && replacement != null) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_ClassCache_ConfigS(replacement)
               || captured.init_Connection() != replacement.init_Connection
               || captured.init_I() != replacement.init_I) {
               return false;
            }

            boolean login = captured.init_MapStore_CachePool() == MapStore.Inner_CachePool.init
               && captured.init_J() == 0L
               && this.init_MapStore_CachePool == MapStore.Inner_CachePool.init;
            boolean reconfiguration = captured.init_MapStore_CachePool() == MapStore.Inner_CachePool.read
               && captured.init_J() == replacement.init_J
               && this.init_MapStore_CachePool == MapStore.Inner_CachePool.read;
            if (!login && !reconfiguration) {
               return false;
            }
         }

         return this.init_Z_Connection(captured.init_Connection(), packet, true, false);
      } else {
         return false;
      }
   }

   private boolean init_Z_MapStore_JsonUtil(
      MapStore.Inner_JsonUtil target, UUID id, long generation, net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action action
   ) {
      if (target != null && !this.load_AtomicBoolean.get()) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_Connection_I(target.init_Connection(), target.init_I())) {
               return false;
            }

            long currentGeneration = this.init_ClassCache_ConfigS == null ? 0L : this.init_ClassCache_ConfigS.init_J;
            if (target.init_J() != currentGeneration) {
               return false;
            }

            if (target.init_MapStore_CachePool() != this.init_MapStore_CachePool
               && (target.init_MapStore_CachePool() != MapStore.Inner_CachePool.load || this.init_MapStore_CachePool != MapStore.Inner_CachePool.read)) {
               return false;
            }
         }

         return this.init_Z_Connection(target.init_Connection(), new ServerboundResourcePackPacket(id, action), true, false);
      } else {
         return false;
      }
   }

   private boolean init_Z_Connection_I(Connection current, int epoch) {
      return current != null && this.load_Z_Connection_I(current, epoch);
   }

   private boolean load_Z_Connection_I(@Nullable Connection current, int epoch) {
      return !this.load_AtomicBoolean.get() && this.init_Connection == current && this.read_I == epoch;
   }

   private void init_V_Connection_2(@Nullable Connection current, int epoch, Throwable error) {
      synchronized (this.load_Obj) {
         if (this.load_Z_Connection_I(current, epoch)) {
            this.init_TaskQueue.init_V_Throwable(error);
         }
      }
   }

   private boolean init_Z_ClassCache_ConfigS(ClassCache.@Nullable Inner_ConfigStore attachment) {
      return attachment != null && this.init_Z_Connection_I(attachment.init_Connection, attachment.init_I) && this.init_ClassCache_ConfigS == attachment;
   }

   private boolean init_Z_ClassCache_IoWorke(ClassCache.Inner_IoWorker phase, Connection current, int epoch, ClassCache.@Nullable Inner_ConfigStore attachment) {
      if (!this.init_Z_Connection_I(current, epoch)) {
         return false;
      }

      if (phase != ClassCache.Inner_IoWorker.init_ClassCache_IoWorke) {
         if (!this.init_Z_ClassCache_ConfigS(attachment)) {
            return false;
         }

         boolean var10000;
         switch (phase) {
            case init_ClassCache_IoWorke:
               var10000 = false;
               break;
            case load:
               var10000 = this.init_MapStore_CachePool == MapStore.Inner_CachePool.load;
               break;
            case read:
               var10000 = this.init_MapStore_CachePool == MapStore.Inner_CachePool.read;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return attachment == null && this.init_MapStore_CachePool == MapStore.Inner_CachePool.init;
      }
   }

   private boolean init_Z_MapStore_JsonUtil(MapStore.@Nullable Inner_JsonUtil target) {
      if (target != null && this.init_Z_Connection_I(target.init_Connection(), target.init_I())) {
         long generation = this.init_ClassCache_ConfigS == null ? 0L : this.init_ClassCache_ConfigS.init_J;
         return target.init_MapStore_CachePool() == this.init_MapStore_CachePool && target.init_J() == generation;
      } else {
         return false;
      }
   }

   public void update_V_Str(String reason) {
      this.pool_Z = true;
      this.task_Z = false;
      this.flags_Z = true;
      this.init_aM = null;
      String message = reason == null ? "Disconnected" : reason;
      Connection current;
      synchronized (this.load_Obj) {
         this.index_Z = false;
         this.push_Str = "";
         if (!this.load_AtomicBoolean.compareAndSet(false, true)) {
            this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.close, message);
            return;
         }

         current = this.init_Connection;
      }

      this.init_MapStore.read();
      this.init_V_Z_Z(true, true);
      this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.close, message);
      init_V_Connection_Component(current, Component.literal(message));
   }

   static void init_V_Connection_Component(@Nullable Connection current, Component reason) {
      if (current != null) {
         Channel channel = current instanceof AutismClientConnectionAccessor accessor ? accessor.getChannel() : null;
         if (channel == null) {
            current.disconnect(reason);
            if (!(current instanceof AutismClientConnectionAccessor)) {
               current.handleDisconnection();
               ConfigStore.init_V(current);
            }
         } else {
            Runnable retired = () -> {
               try {
                  current.handleDisconnection();
               } finally {
                  ConfigStore.init_V(current);
               }
            };
            Runnable close = () -> {
               try {
                  current.disconnect(reason);
               } finally {
                  if (!channel.isOpen()) {
                     init_V_Channel_Runnable(channel, retired);
                  } else {
                     channel.close().addListener(ignored -> init_V_Channel_Runnable(channel, retired));
                  }
               }
            };
            if (channel.eventLoop().inEventLoop()) {
               close.run();
            } else {
               try {
                  channel.eventLoop().execute(close);
               } catch (RejectedExecutionException stopped) {
                  channel.close().addListener(ignored -> init_V_Channel_Runnable(channel, retired));
               }
            }
         }
      }
   }

   private static void init_V_Channel_Runnable(Channel channel, Runnable retired) {
      Runnable afterCloseNotification = () -> {
         try {
            channel.eventLoop().execute(retired);
         } catch (RejectedExecutionException terminated) {
            retired.run();
         }
      };

      try {
         channel.eventLoop().execute(afterCloseNotification);
      } catch (RejectedExecutionException terminated) {
         retired.run();
      }
   }

   void sync_V_Str(String reason) {
      this.open(reason == null ? "Connection failed" : reason);
   }

   void init_V_Str_Throwable(String reason, Throwable error) {
      this.init_TaskQueue.init_V_Throwable(error);
      this.sync_V_Str(reason);
   }

   private void init_V_DisconnectionDetai(DisconnectionDetails details, Connection current, int epoch, ClassCache.@Nullable Inner_ConfigStore attachment) {
      synchronized (this.load_Obj) {
         boolean currentGeneration = this.init_Z_Connection_I(current, epoch)
            && (attachment == null ? this.init_ClassCache_ConfigS == null : this.init_ClassCache_ConfigS == attachment);
         if (!currentGeneration || !this.load_AtomicBoolean.compareAndSet(false, true)) {
            return;
         }
      }

      String reason = details == null ? "Disconnected" : details.reason().getString();
      this.poll(reason);
      ConfigStore.init_V(current);
      this.init_MapStore.read();
      this.init_V_Z_Z(true, true);
      this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.close, reason);
   }

   private void open(String message) {
      Connection current;
      int epoch;
      synchronized (this.load_Obj) {
         current = this.init_Connection;
         epoch = this.read_I;
      }

      this.init_V_Connection(current, epoch, message);
   }

   private void init_V_Connection(Connection current, int epoch, String message) {
      synchronized (this.load_Obj) {
         if (!this.load_Z_Connection_I(current, epoch) || !this.load_AtomicBoolean.compareAndSet(false, true)) {
            return;
         }
      }

      this.init_V_Connection_Str(current, message);
   }

   private void init_V_MapStore_JsonUtil_Str(MapStore.Inner_JsonUtil target, String message) {
      if (target != null) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_MapStore_JsonUtil(target) || !this.load_AtomicBoolean.compareAndSet(false, true)) {
               return;
            }
         }

         this.init_V_Connection_Str(target.init_Connection(), message);
      }
   }

   private void init_V_ClassCache_ConfigS_Str(ClassCache.@Nullable Inner_ConfigStore attachment, String message) {
      if (attachment != null) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_ClassCache_ConfigS(attachment) || !this.load_AtomicBoolean.compareAndSet(false, true)) {
               return;
            }
         }

         this.init_V_Connection_Str(attachment.init_Connection, message);
      }
   }

   private void init_V_ClassCache_ConfigS_3(
      ClassCache.@Nullable Inner_ConfigStore attachment, @Nullable CompletableFuture<ClassCache.Inner_SyncManager> selection, String message
   ) {
      if (attachment != null) {
         synchronized (this.load_Obj) {
            if (!this.init_Z_ClassCache_ConfigS(attachment)
               || attachment.init_ClassCache_TaskQue.init_CompletableFuture != selection
               || !this.load_AtomicBoolean.compareAndSet(false, true)) {
               return;
            }
         }

         this.init_V_Connection_Str(attachment.init_Connection, message);
      }
   }

   private void init_V_Connection_Str(Connection current, String message) {
      if ("Connection closed".equals(this.load_Str)) {
         this.load_Str = "Local failure";
      }

      this.poll(message);
      this.init_MapStore.read();
      this.init_V_Z_Z(true, true);
      this.init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper.open, message);
      init_V_Connection_Component(current, Component.literal(message));
   }

   private void poll(String reason) {
      SyncManager.Inner_DataUtil resolved = this.init_SyncManager_DataUt;
      String account = resolved == null ? this.init_FieldCache_BufferH.init_Str() : resolved.init_User().getName();
      StreamGate.Inner_CachePool actualProtocol = this.load_StreamGate_CachePo;
      String protocol = actualProtocol != null && actualProtocol.init_Z() ? actualProtocol.init_Str() : "Native 26.2";
      TaskQueue.Inner_DataUtil report = this.init_TaskQueue
         .init(
            this.load_Str + ": " + reason,
            this.init_bW == null ? "Direct" : (this.init_bW.init_bZ == null ? "Proxy" : this.init_bW.init_bZ.name()),
            protocol,
            this.read_I,
            account,
            this.open_Str
         );
      this.init_TaskQueue_DataUtil = report;

      try {
         this.init_ClassCache_TokenSt.init_V_ClassCache_TaskQueue_DataUtil(this, report);
      } catch (RuntimeException reportingFailure) {
         AutismClientAddon.LOG.warn("[Multi report {}] Reporter failed ({})", report.init(), reportingFailure.getClass().getSimpleName());
      }
   }

   private void init_V_ClassCache_PathHel_Str(ClassCache.Inner_PathHelper value, String text) {
      if (!this.load_AtomicBoolean.get() || value == ClassCache.Inner_PathHelper.close || value == ClassCache.Inner_PathHelper.open) {
         String updatedDetail = text == null ? value.name() : TaskQueue.init_Str_Str_I(text, 4096);
         ClassCache.Inner_PathHelper previous = this.init_AtomicReference.get();
         String previousDetail = this.read_Str;
         this.read_Str = updatedDetail;
         if (value != ClassCache.Inner_PathHelper.close && value != ClassCache.Inner_PathHelper.open) {
            this.init_TaskQueue.init_V_Str(value.name());
         }

         if (previous != value) {
            this.pop_J = System.currentTimeMillis();
         }

         if (value == ClassCache.Inner_PathHelper.close || value == ClassCache.Inner_PathHelper.open) {
            this.load_I = -1;
            this.reset_J = 0L;
            ClassCache.Inner_UiHelper progress = this.init_ClassCache_UiHelpe;
            if (progress != null && progress.init_Z() && !this.add_Z()) {
               this.init_ClassCache_UiHelpe = new ClassCache.Inner_UiHelper(
                  progress.init_Str(), false, progress.init_I(), progress.load_I(), progress.read(), "Disconnected; waiting for retry"
               );
               this.pop_Str = "";
            }
         }

         this.init_AtomicReference.set(value);
         if (previous != value || !Objects.equals(previousDetail, updatedDetail)) {
            this.init_ClassCache_TokenSt.init_V_ClassCache(this);
         }
      }
   }

   private void push(String line) {
      this.init_ClassCache_TokenSt.init_V_ClassCache_Component(this, Component.literal(line));
   }

   private void entry() {
      synchronized (this.update_Obj) {
         this.flush_I = 0;
         this.init_LastSeenMessagesTr = new LastSeenMessagesTracker(20);
         this.init_MessageSignatureCa = MessageSignatureCache.createDefault();
         this.init_SignedMessageChain = Encoder.UNSIGNED;
         this.init_LocalChatSession = null;
         this.init_UUID = null;
      }
   }

   private static boolean load_Z_Packet(Packet<?> packet) {
      return packet instanceof ClientboundKeepAlivePacket
         || packet instanceof ClientboundPingPacket
         || packet instanceof ClientboundDisconnectPacket
         || packet instanceof ClientboundLoginDisconnectPacket
         || packet instanceof ClientboundHelloPacket
         || packet instanceof ClientboundLoginFinishedPacket
         || packet instanceof ClientboundLoginCompressionPacket
         || packet instanceof ClientboundCustomQueryPacket
         || packet instanceof ClientboundFinishConfigurationPacket
         || packet instanceof ClientboundRegistryDataPacket
         || packet instanceof ClientboundUpdateEnabledFeaturesPacket
         || packet instanceof ClientboundSelectKnownPacks
         || packet instanceof ClientboundResetChatPacket
         || packet instanceof ClientboundCodeOfConductPacket
         || packet instanceof ClientboundUpdateTagsPacket
         || packet instanceof ClientboundCookieRequestPacket
         || packet instanceof ClientboundStoreCookiePacket
         || packet instanceof ClientboundResourcePackPushPacket
         || packet instanceof ClientboundResourcePackPopPacket
         || packet instanceof ClientboundTransferPacket
         || packet instanceof ClientboundLoginPacket
         || packet instanceof ClientboundStartConfigurationPacket
         || packet instanceof ClientboundPlayerPositionPacket
         || packet instanceof ClientboundPlayerRotationPacket
         || packet instanceof ClientboundGameEventPacket
         || packet instanceof ClientboundChunkBatchStartPacket
         || packet instanceof ClientboundChunkBatchFinishedPacket
         || packet instanceof ClientboundAddEntityPacket
         || packet instanceof ClientboundMoveEntityPacket
         || packet instanceof ClientboundEntityPositionSyncPacket
         || packet instanceof ClientboundTeleportEntityPacket
         || packet instanceof ClientboundRotateHeadPacket
         || packet instanceof ClientboundSetEntityDataPacket
         || packet instanceof ClientboundSetEntityMotionPacket
         || packet instanceof ClientboundRemoveEntitiesPacket
         || packet instanceof ClientboundSetPassengersPacket
         || packet instanceof ClientboundMoveMinecartPacket
         || packet instanceof ClientboundShowDialogPacket
         || packet instanceof ClientboundClearDialogPacket;
   }

   private static Object init_Obj_Class(Class<?> type) {
      if (!type.isPrimitive()) {
         return null;
      } else if (type == boolean.class) {
         return false;
      } else if (type == char.class) {
         return '\u0000';
      } else if (type == byte.class) {
         return (byte)0;
      } else if (type == short.class) {
         return (short)0;
      } else if (type == int.class) {
         return 0;
      } else if (type == long.class) {
         return 0L;
      } else if (type == float.class) {
         return 0.0F;
      } else {
         return type == double.class ? 0.0 : null;
      }
   }

   private static String init_Str_Throwable(Throwable error) {
      if (error == null) {
         return "Unknown error";
      }

      Throwable cause = error;
      Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());

      while (cause.getCause() != null && seen.size() < 16 && seen.add(cause.getCause())) {
         cause = cause.getCause();
      }

      if (cause instanceof ReadTimeoutException) {
         return "Read timed out";
      } else {
         return cause instanceof SocketTimeoutException ? "Timed out" : cause.getClass().getSimpleName();
      }
   }

   private static final class Inner_ArrayUtil {
      final aQ init_aQ;
      final ArrayDeque<Integer> init_ArrayDeque = new ArrayDeque<>();
      final ArrayDeque<UtilCore.Inner_DataUtil> load = new ArrayDeque<>();
      final boolean init_Z;
      final BlockPos init_BlockPos;
      final int init_I;
      ClassCache.Inner_ClassCache init_ClassCache_ClassCa;
      long init_J;

      Inner_ArrayUtil(aQ action, boolean hadContainer, BlockPos reopenBlock, int reopenEntity) {
         this.init_aQ = action;
         this.init_Z = hadContainer;
         this.init_BlockPos = reopenBlock == null ? null : reopenBlock.immutable();
         this.init_I = reopenEntity;
         this.init_ClassCache_ClassCa = hadContainer ? ClassCache.Inner_ClassCache.init : ClassCache.Inner_ClassCache.read;
      }
   }

   private record Inner_BitSetUtil(String init_Str, Optional<NumberFormat> init_Optional) {
   }

   private record Inner_BufferHelper(A.Inner_DataUtil init_A_DataUtil, long init_J, long load) {
   }

   public record Inner_ByteReader(
      Component init_Component,
      List<ClassCache.Inner_MapStore> init_List,
      ItemStack init_ItemStack,
      int init_I,
      int load_I,
      ClassCache.Inner_TextFormat init_ClassCache_TextFor,
      boolean init_Z,
      long init_J,
      int read,
      boolean load_Z,
      ClassCache.Inner_FileCache init_ClassCache_FileCac
   ) {
   }

   public static final class Inner_CachePool {
      private final long init_J;
      private final long load;
      private volatile ClassCache.Inner_StreamUtil init_ClassCache_StreamU = ClassCache.Inner_StreamUtil.init;

      private Inner_CachePool(long id, long generation) {
         this.init_J = id;
         this.load = generation;
      }

      public long init_J() {
         return this.init_J;
      }

      public long load() {
         return this.load;
      }

      public ClassCache.Inner_StreamUtil init_ClassCache_StreamU() {
         return this.init_ClassCache_StreamU;
      }

      public boolean init_Z() {
         return this.init_ClassCache_StreamU == ClassCache.Inner_StreamUtil.write
            || this.init_ClassCache_StreamU == ClassCache.Inner_StreamUtil.update
            || this.init_ClassCache_StreamU == ClassCache.Inner_StreamUtil.sync
            || this.init_ClassCache_StreamU == ClassCache.Inner_StreamUtil.flush;
      }
   }

   private record Inner_CharEncoder(String init_Str, String load, Optional<TeamColor> init_Optional) {
   }

   private enum Inner_ClassCache {
      init,
      load,
      read,
      write,
      update,
      sync;
   }

   private final class Inner_ConfigStore {
      final Connection init_Connection;
      final int init_I;
      final long init_J;
      final ClassCache.Inner_TaskQueue init_ClassCache_TaskQue = new ClassCache.Inner_TaskQueue();
      final ClientConfigurationPacketListener init_ClientConfiguratio;

      Inner_ConfigStore(Connection connection, int epoch, long configurationGeneration) {
         this.init_Connection = connection;
         this.init_I = epoch;
         this.init_J = configurationGeneration;
         this.init_ClientConfiguratio = ClassCache.this.init_Obj_ClassCache_IoWorke(
            ClassCache.Inner_IoWorker.load, ClientConfigurationPacketListener.class, this
         );
      }
   }

   public record Inner_EventBus(
      ClassCache.Inner_TextFormat init_ClassCache_TextFor,
      long init_J,
      int init_I,
      String init_Str,
      int load_I,
      Component init_Component,
      List<ItemStack> init_List,
      ItemStack init_ItemStack,
      boolean init_Z,
      boolean load_Z,
      boolean read_Z,
      int read_I,
      boolean write
   ) {
   }

   public record Inner_FieldCache(
      String init_Str,
      String load_Str,
      String read_Str,
      String write_Str,
      ClassCache.Inner_PathHelper init_ClassCache_PathHel,
      String update,
      int init_I,
      boolean init_Z,
      boolean load_Z,
      long init_J,
      String sync,
      boolean read_Z,
      String flush,
      int load_I,
      String close,
      boolean write_Z,
      double init_D,
      double load_D,
      double read_D,
      float init_F,
      float load_F,
      int read_I,
      long load_J,
      String open,
      ClassCache.Inner_UiHelper init_ClassCache_UiHelpe,
      ClassCache.Inner_RenderState init_ClassCache_RenderS
   ) {
      public ClassCache.Inner_JsonUtil init_ClassCache_JsonUti(long now) {
         if (this.init_ClassCache_PathHel != ClassCache.Inner_PathHelper.open && this.init_ClassCache_PathHel != ClassCache.Inner_PathHelper.close) {
            boolean needsChannel = this.init_ClassCache_PathHel == ClassCache.Inner_PathHelper.write
               || this.init_ClassCache_PathHel == ClassCache.Inner_PathHelper.update
               || this.init_ClassCache_PathHel == ClassCache.Inner_PathHelper.sync
               || this.init_ClassCache_PathHel == ClassCache.Inner_PathHelper.flush;
            if (needsChannel && !this.init_Z) {
               return ClassCache.Inner_JsonUtil.read;
            } else {
               return this.init_ClassCache_PathHel == ClassCache.Inner_PathHelper.flush && this.load_Z && !this.init_Z(now)
                  ? ClassCache.Inner_JsonUtil.init
                  : ClassCache.Inner_JsonUtil.load;
            }
         } else {
            return ClassCache.Inner_JsonUtil.read;
         }
      }

      public boolean init_Z(long now) {
         return this.init_J > 0L && now - this.init_J >= 20000L;
      }

      public String init_Str(long now) {
         String var10000;
         switch (this.init_ClassCache_JsonUti(now)) {
            case init:
               var10000 = "Ready";
               break;
            case load:
               var10000 = this.init_ClassCache_PathHel == ClassCache.Inner_PathHelper.flush ? (this.load_Z ? "Stalled" : "Syncing") : "Connecting";
               break;
            case read:
               var10000 = "Down";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   public record Inner_FileCache(String init_Str, int[] init_IArr, List<ClassCache.Inner_ListPool> init_List, int init_I, int load, boolean init_Z) {
      public static final ClassCache.Inner_FileCache init_ClassCache_FileCac = new ClassCache.Inner_FileCache("", new int[0], List.of(), 0, 0, false);
   }

   private enum Inner_HashUtil {
      init,
      load,
      read;
   }

   private enum Inner_IoWorker {
      init_ClassCache_IoWorke(ConnectionProtocol.LOGIN),
      load(ConnectionProtocol.CONFIGURATION),
      read(ConnectionProtocol.PLAY);

      final ConnectionProtocol init_ConnectionProtocol;

      Inner_IoWorker(ConnectionProtocol protocol) {
         this.init_ConnectionProtocol = protocol;
      }
   }

   public enum Inner_JsonUtil {
      init,
      load,
      read;
   }

   public record Inner_ListPool(
      ItemStack init_ItemStack, ItemStack load_ItemStack, ItemStack read_ItemStack, boolean init_Z, int init_I, int load_I, int read_I, int write
   ) {
   }

   public record Inner_MapStore(int init_I, int load, int read, ItemStack init_ItemStack) {
   }

   private record Inner_MethodUtil(long init_J, String init_Str, double init_D, double load, double read) {
   }

   private enum Inner_NetHelper {
      init,
      load,
      read,
      write;
   }

   private final class Inner_PacketUtil implements autismclient.util.f.BufferHelper {
      @Override
      public String init_Str() {
         return ClassCache.this.prev_Str();
      }

      @Override
      public boolean init_Z() {
         return ClassCache.this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush;
      }

      @Override
      public boolean load() {
         return ClassCache.this.init_AtomicReference.get() == ClassCache.Inner_PathHelper.flush;
      }

      @Override
      public CustomMenuSnapshot init_CustomMenuSnapshot() {
         return ClassCache.this.init_StreamUtil.init_CustomMenuSnapshot();
      }

      @Override
      public boolean init(CustomMenuSnapshot snapshot, CustomMenuSubmission submission) {
         CustomMenuSubmitResult result = ClassCache.this.init_CustomMenuSubmitRe_CustomMenuSnapshot_CustomMenuSubmissi(snapshot, submission);
         return result != null && result.success();
      }

      @Override
      public boolean init_Z(String line) {
         return "Sent".equals(ClassCache.this.read_Str_Str(line));
      }

      @Override
      public boolean read() {
         TextFormat run = ClassCache.this.init_TextFormat;
         return run != null && run.update();
      }

      @Override
      public void init_V(String message) {
         ClassCache.this.push(message);
      }

      @Override
      public void load(String context) {
         if (!ClassCache.this.node_Z) {
            ClassCache.this.node_Z = true;
            ClassCache.this.init_ClassCache_TokenSt.load(ClassCache.this, context);
         }
      }
   }

   public enum Inner_PathHelper {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open;
   }

   private record Inner_PoolEntry(long init_J, boolean init_Z, Packet<?> init_Packet) {
   }

   private record Inner_RefQueue(int init_I, String init_Str, Optional<NumberFormat> init_Optional) {
   }

   public record Inner_RenderState(long init_J, long load) {
      public static final ClassCache.Inner_RenderState init_ClassCache_RenderS = new ClassCache.Inner_RenderState(0L, 0L);

      public boolean init_Z(long now) {
         return this.load > now;
      }

      public long init_J(long now) {
         return Math.max(0L, this.load - now);
      }

      public double init_D(long now) {
         long span = this.load - this.init_J;
         return span <= 0L ? 1.0 : Math.max(0.0, Math.min(1.0, (double)(now - this.init_J) / span));
      }
   }

   private record Inner_RingBuffer(int init_I, int load, Component init_Component, String init_Str, List<ItemStack> init_List, ItemStack init_ItemStack) {
   }

   private static final class Inner_StateCache {
      private final PackRepository init_PackRepository = ServerPacksSource.createVanillaTrustedRepository();
      private final Map<KnownPack, String> init_Map;

      Inner_StateCache() {
         this.init_PackRepository.reload();
         Map<KnownPack, String> knownPacks = new HashMap<>();

         for (Pack pack : this.init_PackRepository.getAvailablePacks()) {
            PackLocationInfo location = pack.location();
            location.knownPackInfo().ifPresent(knownPack -> knownPacks.put(knownPack, location.id()));
         }

         this.init_Map = Map.copyOf(knownPacks);
      }

      synchronized ClassCache.Inner_SyncManager init(List<KnownPack> requested) {
         List<KnownPack> acknowledged = new ArrayList<>(requested.size());
         List<String> selectedIds = new ArrayList<>(requested.size());

         for (KnownPack knownPack : requested) {
            String packId = this.init_Map.get(knownPack);
            if (packId != null) {
               acknowledged.add(knownPack);
               selectedIds.add(packId);
            }
         }

         return new ClassCache.Inner_SyncManager(acknowledged, selectedIds);
      }

      synchronized CloseableResourceManager init(ClassCache.Inner_SyncManager selection) {
         this.init_PackRepository.setSelected(selection.load());
         return new MultiPackResourceManager(PackType.SERVER_DATA, this.init_PackRepository.openAllSelected());
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   private record Inner_SyncManager(List<KnownPack> init, List<String> load) {
      Inner_SyncManager {
         init = List.copyOf(init);
         load = List.copyOf(load);
      }
   }

   private record Inner_TableIndex(
      ClassCache.Inner_CachePool init_ClassCache_CachePo,
      long init_J,
      int init_I,
      int load,
      int read,
      ContainerInput init_ContainerInput,
      boolean init_Z,
      Packet<?> init_Packet,
      int write
   ) {
   }

   private static final class Inner_TaskQueue {
      final RegistryDataCollector init_RegistryDataCollec = new RegistryDataCollector();
      private ClassCache.Inner_StateCache init_ClassCache_StateCa;
      private CompletableFuture<ClassCache.Inner_SyncManager> init_CompletableFuture = CompletableFuture.completedFuture(null);
      private boolean init_Z;
      private boolean load;

      synchronized ClassCache.Inner_StateCache init() {
         if (this.init_ClassCache_StateCa == null) {
            this.init_ClassCache_StateCa = new ClassCache.Inner_StateCache();
         }

         return this.init_ClassCache_StateCa;
      }
   }

   record Inner_TaskTimer(int init_I, int load, int read, List<String> init_List) {
   }

   public enum Inner_TextFormat {
      init,
      load,
      read,
      write;
   }

   interface Inner_TokenStore {
      String init_Str_ClassCache_UUID(ClassCache var1, UUID var2);

      void init_V_ClassCache(ClassCache var1);

      default void init_V_ClassCache_TaskQueue_DataUtil(ClassCache session, TaskQueue.Inner_DataUtil report) {
      }

      void init_V_ClassCache_Component(ClassCache var1, Component var2);

      default void load_V_ClassCache(ClassCache session) {
      }

      default void init_V_ClassCache_Str(ClassCache session, String text) {
      }

      boolean init_Z_ClassCache_aA(ClassCache var1, autismclient.util.macro.aA var2);

      default void init_V_ClassCache_aM(ClassCache session, aM macro) {
      }

      default void read_V_ClassCache(ClassCache session) {
      }

      default void load(ClassCache session, String title) {
      }
   }

   public record Inner_UiHelper(String init_Str, boolean init_Z, int init_I, int load_I, int read, String load_Str) {
      static ClassCache.Inner_UiHelper init_ClassCache_UiHelpe() {
         return new ClassCache.Inner_UiHelper("", false, 0, 0, 0, "");
      }
   }
}
