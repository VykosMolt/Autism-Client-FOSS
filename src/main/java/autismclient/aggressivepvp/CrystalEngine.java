package autismclient.aggressivepvp;

import autismclient.aggressivepvp.BlockUtils.SwingMode;
import autismclient.aggressivepvp.InvUtils.FindItemResult;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.modules.Module;
import autismclient.util.ValueSink;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/**
 * The Donut Meteor fork's Crystal Aura, integrated into AUTISM 5.3-26.3's crystal-aura module when its Version is Aggressive.
 */
public final class CrystalEngine extends Engine {
    /** AUTISM's own settings that stay visible under both versions: visuals, not the aura. */
    private static final Set<String> SHARED = Set.of("crystal-view", "view-size", "view-y", "view-spin", "view-bounce");

    private static final double MIN_DAMAGE = 1.5;

    private static final String GENERAL = "General", TIMING = "Timing", DAMAGE = "Damage", SWITCH = "Switch", PLACE = "Place", FACE_PLACE = "Face Place",
        BREAK = "Destroy", PAUSE = "Pause";

    // General

    private final BoolSetting doPlace = add(new BoolSetting("ag-place", "Place", true).group(GENERAL)
        .description("Place crystals."));

    private final BoolSetting doBreak = add(new BoolSetting("ag-break", "Destroy", true).group(GENERAL)
        .description("Attack crystals in range."));

    private final DoubleSetting targetRange = add(new DoubleSetting("ag-target-range", "Target Range", 10, 1, 16, 0.5).group(GENERAL)
        .description("Enemy search radius."));

    private final BoolSetting predictMovement = add(new BoolSetting("ag-predict-movement", "Predict Movement", true).group(GENERAL)
        .description("Aim at predicted positions."));

    private final DoubleSetting minDamage = add(new DoubleSetting("ag-min-damage", "Min Target Damage", MIN_DAMAGE, 0, 36, 0.5).sliderRange(0, 10).group(DAMAGE)
        .description("Minimum blast damage to target."));

    private final BoolSetting assumeProtection = add(new BoolSetting("ag-assume-protection", "Assume Protection", true).group(DAMAGE)
        .description("Treat hidden armor as protected."));

    private final DoubleSetting maxDamage = add(new DoubleSetting("ag-max-damage", "Max Self Damage", 16, 0, 36, 0.5).sliderRange(0, 20).group(DAMAGE)
        .description("Maximum blast damage to self."));

    private final DoubleSetting damageRatio = add(new DoubleSetting("ag-damage-ratio", "Damage Ratio", 0.85, 0, 5, 0.05).sliderRange(0, 3).group(DAMAGE)
        .description("Target damage per self damage."));

    private final DoubleSetting lethalMultiplier = add(new DoubleSetting("ag-lethal-multiplier", "Lethal Multiplier", 1, 0, 4, 0.05).sliderRange(0, 2).group(DAMAGE)
        .description("Health divisor for kill checks."));

    private final BoolSetting antiSuicide = add(new BoolSetting("ag-anti-suicide", "Anti Suicide", true).group(DAMAGE)
        .description("Never risk lethal self damage."));

    private final BoolSetting tradeTotem = add(new BoolSetting("ag-trade-totem", "Trade Totem", true).group(DAMAGE)
        .description("Trade your totem for theirs."));

    private final IntSetting popMargin = add(new IntSetting("ag-pop-margin", "Pop Margin", 20, 0, 50, 1).unit("%").group(DAMAGE)
        .description("Self damage counted as pop."));

    private final IntSetting keepTotems = add(new IntSetting("ag-keep-totems", "Keep Totems", 3, 0, 20, 1).sliderRange(0, 10).group(DAMAGE)
        .description("Stop trading at this many."), () -> tradeTotem.get());

    private final BoolSetting ignoreNakeds = add(new BoolSetting("ag-ignore-nakeds", "Ignore Nakeds", false).group(GENERAL)
        .description("Ignore players with no items."));

    private final BoolSetting crosshair = add(new BoolSetting("ag-crosshair-only", "Crosshair Only", false).group(GENERAL)
        .description("Only act on crosshair."));

    private final RegistryListSetting entities = add(RegistryListSetting.entityTypes("ag-entities", "Entities", "minecraft:player|minecraft:warden|minecraft:wither").group(GENERAL)
        .description("Entities to attack."));

    private final EnumSetting<SwingMode> swingMode = add(new EnumSetting<>("ag-swing-mode", "Swing Mode", SwingMode.Both, SwingMode.values()).group(GENERAL)
        .description("How the swing is shown."));

    private final BoolSetting explain = add(new BoolSetting("ag-explain", "Explain", false).group(GENERAL)
        .description("Say why in chat."));

    // Switch

    private final EnumSetting<AutoSwitchMode> autoSwitch = add(new EnumSetting<>("ag-auto-switch", "Auto Switch", AutoSwitchMode.Normal, AutoSwitchMode.values()).group(SWITCH)
        .description("Switch to crystals automatically."));

    private final BoolSetting switchBack = add(new BoolSetting("ag-switch-back", "Switch Back", true).group(SWITCH)
        .description("Restore previous slot after placing."), () -> autoSwitch.get() == AutoSwitchMode.Normal);

    private final BoolSetting noGapSwitch = add(new BoolSetting("ag-no-gap-switch", "No Gap Switch", true).group(SWITCH)
        .description("No switch while holding gapples."), () -> autoSwitch.get() == AutoSwitchMode.Normal);

    private final BoolSetting noBowSwitch = add(new BoolSetting("ag-no-bow-switch", "No Bow Switch", true).group(SWITCH)
        .description("No switch while holding bows."));

    private final BoolSetting antiWeakness = add(new BoolSetting("ag-anti-weakness", "Anti Weakness", true).group(DAMAGE)
        .description("Borrow a weapon under weakness."));

    // Place

    private final IntSetting placeDelay = add(new IntSetting("ag-place-delay", "Place Delay", 0, 0, 20, 1).unit("ticks").group(TIMING)
        .description("Wait after explosion before placing."));

    private final DoubleSetting placeRange = add(new DoubleSetting("ag-place-range", "Place Range", 4.5, 0, 6, 0.05).group(PLACE)
        .description("Crystal placement reach."));

    private final DoubleSetting placeWallsRange = add(new DoubleSetting("ag-place-walls-range", "Place Walls Range", 4.5, 0, 6, 0.05).group(PLACE)
        .description("Placement reach through walls."));

    private final BoolSetting instantPlace = add(new BoolSetting("ag-instant-place", "Instant Place", true).group(PLACE)
        .description("Place on new obsidian instantly."));

    private final BoolSetting turnToPlace = add(new BoolSetting("ag-turn-to-place", "Turn To Place", true).group(PLACE)
        .description("Silently turn to place."));

    private final BoolSetting placement112 = add(new BoolSetting("ag-1.12-placement", "1.12 Placement", false).group(PLACE)
        .description("Use 1.12 crystal placement."));

    private final EnumSetting<SupportMode> support = add(new EnumSetting<>("ag-support", "Support", SupportMode.OnClick, SupportMode.values(), m -> m == SupportMode.OnClick ? "On Click" : EnumSetting.titleCase(m)).group(PLACE)
        .description("Build obsidian bases."));

    private final IntSetting supportDelay = add(new IntSetting("ag-support-delay", "Support Delay", 1, 0, 20, 1).unit("ticks").group(TIMING)
        .description("Wait after placing support."), () -> support.get() != SupportMode.Disabled);

    // Face Place

    private final BoolSetting facePlace = add(new BoolSetting("ag-face-place", "Face Place", true).group(FACE_PLACE)
        .description("Face-place weak targets."));

    private final DoubleSetting facePlaceHealth = add(new DoubleSetting("ag-face-place-health", "Face Place Health", 8, 1, 36, 0.5).sliderRange(1, 20).group(FACE_PLACE)
        .description("Face-place below this health."), () -> facePlace.get());

    private final DoubleSetting facePlaceDurability = add(new DoubleSetting("ag-face-place-durability", "Face Place Durability", 2, 1, 100, 1).unit("%").group(FACE_PLACE)
        .description("Face-place below this durability."), () -> facePlace.get());

    private final BoolSetting facePlaceArmor = add(new BoolSetting("ag-face-place-missing-armor", "Face Place Missing Armor", false).group(FACE_PLACE)
        .description("Face-place targets missing armor."), () -> facePlace.get());

    private final KeybindSetting forceFacePlace = add(new KeybindSetting("ag-force-face-place", "Force Face Place", -1).group(FACE_PLACE)
        .description("Hold to force face-place."));

    // Break

    private final IntSetting breakDelay = add(new IntSetting("ag-break-delay", "Destroy Delay", 0, 0, 20, 1).unit("ticks").group(TIMING)
        .description("Wait after placing before destroying."));

    private final IntSetting switchDelay = add(new IntSetting("ag-switch-delay", "Switch Delay", 0, 0, 20, 1).unit("ticks").group(TIMING)
        .description("Wait after switching before destroying."));

    private final BoolSetting smartDelay = add(new BoolSetting("ag-smart-delay", "Smart Delay", true).group(TIMING)
        .description("Time crystals to damage windows."));

    private final IntSetting placeAhead = add(new IntSetting("ag-place-ahead", "Place Ahead", 150, 0, 500, 10).unit("ms").group(TIMING)
        .description("Place this early before hit."), () -> smartDelay.get());

    private final BoolSetting dtap = add(new BoolSetting("ag-dtap", "Dtap", false).group(TIMING)
        .description("Pop late for follow-up kill."), () -> smartDelay.get());
    private int dtapNotedAt = -1000;
    private boolean dtapHeld, dtapRelease;

    private final BoolSetting deny = add(new BoolSetting("ag-deny-crystals", "Deny Crystals", true).group(BREAK)
        .description("Pop enemy crystals near you."), () -> smartDelay.get());

    private final DoubleSetting breakRange = add(new DoubleSetting("ag-break-range", "Destroy Range", 4.75, 0, 6, 0.05).group(BREAK)
        .description("Crystal destroy reach."));

    private final DoubleSetting breakWallsRange = add(new DoubleSetting("ag-break-walls-range", "Destroy Walls Range", 4.5, 0, 6, 0.05).group(BREAK)
        .description("Destroy reach through walls."));

    private final BoolSetting onlyBreakOwn = add(new BoolSetting("ag-only-own", "Only Own", false).group(BREAK)
        .description("Destroy only own crystals."));

    private final IntSetting breakAttempts = add(new IntSetting("ag-break-attempts", "Destroy Attempts", 2, 1, 5, 1).group(BREAK)
        .description("Hits before giving up."));

    private final IntSetting ticksExisted = add(new IntSetting("ag-ticks-existed", "Ticks Existed", 0, 0, 20, 1).unit("ticks").group(TIMING)
        .description("Minimum crystal age."));

    private final IntSetting attackFrequency = add(new IntSetting("ag-attack-frequency", "Attack Frequency", 25, 1, 30, 1).unit("/s").group(TIMING)
        .description("Maximum hits per second."));

    // Pause

    private final EnumSetting<PauseMode> pauseOnUse = add(new EnumSetting<>("ag-pause-on-use", "Pause On Use", PauseMode.Place, PauseMode.values()).group(PAUSE)
        .description("Pause while using items."));

    private final EnumSetting<PauseMode> pauseOnMine = add(new EnumSetting<>("ag-pause-on-mine", "Pause On Mine", PauseMode.None, PauseMode.values()).group(PAUSE)
        .description("Pause while mining."));

    private final BoolSetting pauseOnLag = add(new BoolSetting("ag-pause-on-lag", "Pause On Lag", true).group(PAUSE)
        .description("Pause while the server lags."));

    private final DoubleSetting pauseHealth = add(new DoubleSetting("ag-pause-health", "Pause Health", 0, 0, 36, 0.5).sliderRange(0, 20).group(PAUSE)
        .description("Pause below this health."));

    public CrystalEngine(Module module) {
        super(module, SHARED);
        arrange(GENERAL, TIMING, DAMAGE, SWITCH, PLACE, FACE_PLACE, BREAK, PAUSE);
    }

    @Override
    protected boolean explain() {
        return explain.get();
    }

    private List<String> entitiesRaw;
    private final Set<EntityType<?>> entityTypes = new HashSet<>();

    private Set<EntityType<?>> entityTypes() {
        List<String> raw = entities.get();
        if (!raw.equals(entitiesRaw)) {
            entitiesRaw = raw;
            entityTypes.clear();
            for (String id : raw) {
                net.minecraft.resources.Identifier key = net.minecraft.resources.Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
                if (key != null) BuiltInRegistries.ENTITY_TYPE.getOptional(key).ifPresent(entityTypes::add);
            }
        }
        return entityTypes;
    }

    /** AUTISM's ValueSink handles both mouse binds (-1000 - button) and keyboard keys via CompatGLFW on MC 26.3 SDL3. */
    private boolean forceFacePlacePressed() {
        int key = forceFacePlace.get();
        return key != -1 && mc.gui.screen() == null && ValueSink.init(mc, key);
    }

    // Fields

    private static final int PATIENCE = 30;
    private static final int BASE_COOLDOWN = 12;
    private int buildAskedAge = -1000, saidNoSpotAge = -1000;
    private static final int BUILD_ASK_TICKS = 10;

    private Item mainItem, offItem;

    private int breakTimer, placeTimer, switchTimer, ticksPassed;
    private int switchBackSlot = -1, switchedTo = -1;
    private final List<LivingEntity> targets = new ArrayList<>();

    private Vec3 vec3d = Vec3.ZERO;
    private Vec3 playerEyePos = Vec3.ZERO;
    private final BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
    private AABB box = new AABB(0, 0, 0, 0, 0, 0);

    private Vec3 vec3dRayTraceEnd = Vec3.ZERO;
    private ClipContext clipContext;

    private final IntSet placedCrystals = new IntOpenHashSet();
    private boolean placing;
    private int placingTimer;
    public int kaTimer;
    private final BlockPos.MutableBlockPos placingCrystalBlockPos = new BlockPos.MutableBlockPos();

    private final IntSet removed = new IntOpenHashSet();
    private final Int2IntMap attemptedBreaks = new Int2IntOpenHashMap();
    private final Int2IntMap waitingToExplode = new Int2IntOpenHashMap();
    private int attacks;

    private LivingEntity bestTarget;
    private double bestTargetDamage;
    private int bestTargetTimer;

    private CrystalLogic.Rules rules;
    private float scoreDamage;
    private LivingEntity scoreTarget;
    private double worthScore;
    private float worthDamage;
    private float baseWorth;

    private CrystalLogic.Window selfWindow = new CrystalLogic.Window();
    private float selfSeenHealth = -1;
    private int selfSeenHurt;
    private int explosionAge = -1000, shieldStart = -2000;

    private record Hit(LivingEntity target, float damage, long sentAt) {}
    private final Int2ObjectMap<CrystalLogic.Window> windows = new Int2ObjectOpenHashMap<>();
    private final Int2FloatMap seenHealth = new Int2FloatOpenHashMap();
    private final Int2IntMap seenHurt = new Int2IntOpenHashMap();
    private final IntSet targetIds = new IntOpenHashSet();
    private final Int2ObjectMap<Hit> hits = new Int2ObjectOpenHashMap<>();
    private final CrystalLogic.RoundTrip roundTrip = new CrystalLogic.RoundTrip();
    private final Int2ObjectMap<Vec3> breakLeads = new Int2ObjectOpenHashMap<>(), placeLeads = new Int2ObjectOpenHashMap<>();
    private long placeSentAt;
    private boolean placeAnswered = true;
    private final CrystalLogic.Aim aim = new CrystalLogic.Aim();
    private double heldDueAt = Double.MAX_VALUE;
    private boolean ignoreWindow;
    private final IntOpenHashSet describedTargets = new IntOpenHashSet();
    private final IntOpenHashSet seenTotem = new IntOpenHashSet();
    private int selfRefused;
    private int ratioRefused;
    private boolean limitLifted, saidRatioRefusal;
    private float leastSelf;
    private boolean saidSelfRefusal;
    private final BlockPos.MutableBlockPos instantBase = new BlockPos.MutableBlockPos();
    private int instantAge = -1000;
    private static final int INSTANT_TICKS = 20;
    private float hitDamage;
    private LivingEntity hitTarget;
    private int lastActionAge;
    private int baseTimer;

    private float evalDamage, evalSelf;
    private double placeSelf = -1;
    private boolean offhandSpent;
    private int spentAge;
    private final Int2IntOpenHashMap poppedAt = new Int2IntOpenHashMap();
    private final List<Vec3> threats = new ArrayList<>();
    private int threatAge = -1;
    private int totemsLeft;
    private boolean saidLowTotems;
    private static final int SPENT_TICKS = 40;
    private double evalSelfCost;
    private LivingEntity evalTarget;
    private int age, lagTicks, leftAloneAt, ignoredAt;
    private boolean saidNoTarget, saidLag;
    private int statNoTarget, statLag, statPlaced, statHits, statExploded, statGaveUp;
    private int seenBases, usableBases, buildSpots, statBlocked, unseenBases;
    private String refusal, crystalWhy;
    private final java.util.Map<String, Integer> refusals = new java.util.TreeMap<>();
    private String instantSaid;
    private String pauseWhy = "nothing";

    private Entity wantHit;
    private float wantHitDamage;
    private LivingEntity wantHitTarget;
    private BlockHitResult wantClick;
    private double wantClickDamage;
    private BlockPos wantSupport;
    private boolean turnable;
    private boolean lookRefused;
    private int turnedAge = -1000;
    private int handAge = -1000;
    private static final int HAND_TICKS = 10;
    private boolean turnedPlacePending;
    private int turnedHitId = Integer.MIN_VALUE, turnFails;
    private boolean turnOff;
    private int statLookWait, statOrderWait, statTurned;
    private static final double REACH_MARGIN = 0.05;
    private static final double AIM_INSET = 0.25, FACE_INSET = 0.4;
    private static final double TURN_MARGIN = 0.25, FACE_TURN_MARGIN = 0.55;
    private static final int TURN_GIVE_UP = 4;

    @Override
    public void onActivate() {
        switchBackSlot = -1;
        breakTimer = 0;
        placeTimer = 0;
        ticksPassed = 0;

        clipContext = mc.player != null
            ? new ClipContext(new Vec3(0, 0, 0), new Vec3(0, 0, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player)
            : null;

        placing = false;
        placingTimer = 0;
        kaTimer = 0;

        age = lagTicks = lastActionAge = 0;
        selfWindow = new CrystalLogic.Window();
        selfSeenHealth = -1;
        selfSeenHurt = mc.player != null ? mc.player.hurtTime : 0;
        explosionAge = -1000;
        shieldStart = -2000;
        leftAloneAt = ignoredAt = -1000;
        saidNoTarget = saidLag = false;
        statNoTarget = statLag = statPlaced = statHits = statExploded = statGaveUp = 0;

        attacks = 0;

        rules = new CrystalLogic.Rules(minDamage.get(), maxDamage.get(), antiSuicide.get(), lethalMultiplier.get(), damageRatio.get());

        bestTargetDamage = 0;
        bestTargetTimer = 0;

        heldDueAt = Double.MAX_VALUE;
        instantAge = -1000;
        offhandSpent = false;
        threatAge = -1;
        poppedAt.clear();
        describedTargets.clear();

        FightLog.start(String.format(java.util.Locale.ROOT, "version aggressive (AUTISM 5.8-26.3); crosshair-only %s; place-delay %d; break-delay %d; instant-place %s; attack-frequency %d; ticks-existed %d; place-range %.1f; break-range %.1f; min-damage %.1f; max-damage %.1f; switch %s; ping %d; smart-delay %s; place-ahead %d; predict-movement %s; support %s; deny-crystals %s; damage-ratio %.1f; aim %.0f; turn-to-place %s; trade-totem %s; pop-margin %d; keep-totems %d",
            crosshair.get(), placeDelay.get(), breakDelay.get(), instantPlace.get(), attackFrequency.get(), ticksExisted.get(), placeRange.get(), breakRange.get(), minDamage.get(), maxDamage.get(), autoSwitch.get(), PlayerUtils.getPing(),
            smartDelay.get(), placeAhead.get(), predictMovement.get(), support.get(), deny.get(), damageRatio.get(), aim.ms(), turnToPlace.get(), tradeTotem.get(), popMargin.get(), keepTotems.get()));
    }

    @Override
    public void onDeactivate() {
        Rotations.reset();
        FightLog.stop();

        targets.clear();

        placedCrystals.clear();

        attemptedBreaks.clear();
        waitingToExplode.clear();

        removed.clear();
        windows.clear();
        seenHealth.clear();
        seenHurt.clear();
        selfSeenHealth = -1;
        hits.clear();
        baseTimer = 0;

        bestTarget = null;
    }

    @Override
    public void onPreTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;
        if (clipContext == null) {
            clipContext = new ClipContext(new Vec3(0, 0, 0), new Vec3(0, 0, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
        }
        DamageUtils.assumeHiddenProtection = assumeProtection.get();
        totemsLeft = InvUtils.find(Items.TOTEM_OF_UNDYING).count();
        boolean lowTotems = tradeTotem.get() && totemsLeft <= keepTotems.get();
        if (lowTotems && !saidLowTotems) info("%d %s left: Trade Totem paused.", totemsLeft, totemsLeft == 1 ? "totem" : "totems");
        saidLowTotems = lowTotems;
        if (placing) {
            if (placingTimer > 0) placingTimer--;
            else {
                placing = false;
                if (turnedPlacePending) {
                    turnedPlacePending = false;
                    turnFailed();
                }
            }
        }

        if (kaTimer > 0) kaTimer--;

        if (ticksPassed < 20) ticksPassed++;
        else {
            ticksPassed = 0;
            attacks = 0;
        }

        if (bestTargetTimer > 0) bestTargetTimer--;
        bestTargetDamage = 0;

        if (breakTimer > 0) breakTimer--;
        if (placeTimer > 0) placeTimer--;
        if (switchTimer > 0) switchTimer--;

        if (baseTimer > 0) baseTimer--;

        mainItem = mc.player.getMainHandItem().getItem();
        offItem = mc.player.getOffhandItem().getItem();

        int answerTicks = CrystalLogic.answerTicks(latencyMs());
        for (IntIterator it = waitingToExplode.keySet().iterator(); it.hasNext(); ) {
            int id = it.nextInt();
            int ticks = waitingToExplode.get(id);

            if (ticks >= answerTicks) {
                it.remove();
                removed.remove(id);

                FightLog.unanswered(id, attemptedBreaks.get(id));
                if (id == turnedHitId) {
                    turnedHitId = Integer.MIN_VALUE;
                    turnFailed();
                }
                if (attemptedBreaks.get(id) > breakAttempts.get()) {
                    statGaveUp++;
                    if (age - ignoredAt >= 200) {
                        ignoredAt = age;
                        warning("Server ignored %d hits; try Crosshair Only.", attemptedBreaks.get(id));
                    }
                }
            } else {
                waitingToExplode.put(id, ticks + 1);
            }
        }

        playerEyePos = new Vec3(mc.player.position().x, mc.player.position().y + mc.player.getEyeHeight(mc.player.getPose()), mc.player.position().z);

        report();
        FightLog.tick();

        rules = new CrystalLogic.Rules(minDamage.get(), maxDamage.get(), antiSuicide.get(), lethalMultiplier.get(), damageRatio.get());
        findTargets();
        Rotations.correctMovement = true;
        if (targets.isEmpty()) {
            describedTargets.clear();
            seenTotem.clear();
            saidSelfRefusal = false;
            saidRatioRefusal = false;
            turnOff = false;
            turnFails = 0;
        }
        for (LivingEntity target : targets) if (describedTargets.add(target.getId())) FightLog.target(target);
        watchHealth();
        breakLeads.clear();
        placeLeads.clear();

        wantHit = null;
        wantClick = null;
        wantSupport = null;
        if (support.get() == SupportMode.OnClick && useAtNothing() && mc.player.getMainHandItem().is(Items.OBSIDIAN)) buildAskedAge = age;
        turnable = mayTurn();

        if (!targets.isEmpty()) {
            doBreak();
            doPlace();
        }
    }

    // Sending

    public void onPlayerTickMovement() {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;
        runActions();
    }

    private void runActions() {
        Entity hit = wantHit;
        BlockHitResult click = wantClick;
        BlockPos supportAt = wantSupport;
        wantHit = null;
        wantClick = null;
        wantSupport = null;
        if (ActionWindow.foreign()) handAge = age;

        if (switchBackSlot >= 0) {
            if (!switchBack.get() || mc.player.getInventory().getSelectedSlot() != switchedTo) switchBackSlot = -1;
            else if (!(click != null && supportAt == null) && !mc.player.isUsingItem() && ActionWindow.maySwitchSlot()) {
                InvUtils.swap(switchBackSlot, false);
                switchBackSlot = -1;
            }
        }

        if (hit == null && click == null) return;
        if (mc.player.isUsingItem()) return;

        Vec3 eye = mc.player.getEyePosition();
        float yaw = ActionWindow.yaw(), pitch = ActionWindow.pitch();
        boolean turned = age - turnedAge == 1;
        double hitReach = mc.player.entityInteractionRange() - REACH_MARGIN, clickReach = mc.player.blockInteractionRange() - REACH_MARGIN;

        double[] next = null, nextAim = null;
        double nextReach = 0;
        boolean sent = false;

        if (hit != null && hit.isAlive() && !removed.contains(hit.getId())) {
            double[] box = crystalBox(hit.getX(), hit.getY(), hit.getZ());
            if (!ActionWindow.mayAttack(hit.getId())) statOrderWait++;
            else if (LookMath.reach(eye.x, eye.y, eye.z, yaw, pitch, box) <= hitReach) {
                FightLog.turned = turned;
                attackCrystal(hit, wantHitTarget, wantHitDamage);
                FightLog.turned = false;
                hitSent(hit, wantHitTarget, wantHitDamage);
                if (turned) {
                    turnedHitId = hit.getId();
                    statTurned++;
                }
                sent = true;
                if (!cannotPlace()) {
                    BlockPos base = hit.blockPosition().below();
                    next = blockBox(base);
                    nextAim = aimBox(base, -1);
                    nextReach = clickReach;
                }
            } else {
                statLookWait++;
                next = nextAim = box;
                nextReach = hitReach;
            }
        }

        if (!sent && click != null) {
            BlockPos pos = click.getBlockPos();
            int face = -1;
            if (supportAt != null) {
                Direction side = BlockUtils.getPlaceSide(supportAt);
                if (side == null) return;
                pos = supportAt.relative(side);
                face = side.getOpposite().get3DDataValue();
            }

            double[] box = blockBox(pos);
            LookMath.Entry entry = LookMath.enter(eye.x, eye.y, eye.z, yaw, pitch, box);
            boolean lookOn = entry != null && entry.face() >= 0 && entry.distance() <= clickReach && (face < 0 || entry.face() == face);

            if (!ActionWindow.mayPlace()) statOrderWait++;
            else if (lookOn) {
                BlockHitResult real = new BlockHitResult(new Vec3(entry.x(), entry.y(), entry.z()), Direction.from3DDataValue(entry.face()), pos, false);
                FightLog.turned = turned;
                boolean went = placeCrystal(real, wantClickDamage, supportAt);
                FightLog.turned = false;
                if (went) {
                    placeTimer += placeDelay.get();
                    sent = true;
                    if (supportAt == null) {
                        if (turned) {
                            turnedPlacePending = true;
                            statTurned++;
                        }
                        if (doBreak.get()) {
                            next = nextAim = crystalBox(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                            nextReach = hitReach;
                        }
                    } else {
                        next = blockBox(supportAt);
                        nextAim = aimBox(supportAt, -1);
                        nextReach = clickReach;
                    }
                }
            } else if (next == null) {
                statLookWait++;
                nextAim = aimBox(pos, face);
                next = face < 0 ? box : nextAim;
                nextReach = clickReach;
            }
        }

        if (next != null) turnTo(next, nextAim, nextReach, nextReach == hitReach ? AIM_INSET : FACE_INSET);
    }

    private boolean mayTurn() {
        if (!turnToPlace.get() || turnOff || crosshair.get() || mc.player == null || mc.getCameraEntity() != mc.player) return false;
        if (Autism.aimOwner() != null) return false;
        if ((mc.options.keyUse.isDown() || mc.options.keyAttack.isDown() || age - handAge < HAND_TICKS) && !(useAtNothing() && !mc.options.keyAttack.isDown())) return false;
        return !mc.player.isFallFlying() && !mc.player.isSwimming() && !mc.player.isPassenger();
    }

    private boolean useAtNothing() {
        return mc.options.keyUse.isDown() && mc.gui.screen() == null && (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS);
    }

    private void turnTo(double[] box, double[] aimAt, double reach, double inset) {
        if (!mayTurn() || ActionWindow.foreign()) return;

        Vec3 eye = mc.player.getEyePosition(), speed = mc.player.getDeltaMovement();
        double ex = eye.x + speed.x, ey = eye.y + (mc.player.onGround() ? 0 : speed.y), ez = eye.z + speed.z;
        if (LookMath.reach(ex, ey, ez, mc.player.getYRot(), mc.player.getXRot(), box) <= reach) return;

        float[] look = LookMath.aim(ex, ey, ez, aimAt, inset, ActionWindow.yaw());
        if (look == null || LookMath.reach(ex, ey, ez, look[0], look[1], box) > reach) return;

        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        float yaw = look[0] + (random.nextFloat() - 0.5f) * 1.5f, pitch = Mth.clamp(look[1] + (random.nextFloat() - 0.5f), -90, 90);
        if (LookMath.reach(ex, ey, ez, yaw, pitch, box) > reach) {
            yaw = look[0];
            pitch = look[1];
        }

        Rotations.rotateOnce(yaw, pitch, 50);
        turnedAge = age;
    }

    private void turnFailed() {
        if (++turnFails < TURN_GIVE_UP || turnOff) return;
        turnOff = true;
        FightLog.note("TURN", "%d turned actions in a row unanswered: no more turning in this fight", turnFails);
        warning("Turned look ignored %d times; crosshair only this fight.", turnFails);
    }

    private static double[] crystalBox(double x, double y, double z) {
        return new double[]{x - 1, y, z - 1, x + 1, y + 2, z + 1};
    }

    private static double[] blockBox(BlockPos pos) {
        return new double[]{pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1};
    }

    private double[] aimBox(BlockPos pos, int face) {
        if (face < 0) {
            BlockHitResult seen = getVisibleFace(pos);
            if (seen == null) return blockBox(pos);
            face = seen.getDirection().get3DDataValue();
        }
        return faceBox(pos, face);
    }

    private static double[] faceBox(BlockPos pos, int face) {
        double[] box = blockBox(pos);
        int axis = face < 2 ? 1 : face < 4 ? 2 : 0;
        if ((face & 1) == 0) box[axis + 3] = box[axis] + 0.1;
        else box[axis] = box[axis + 3] - 0.1;
        return box;
    }

    public void onEntityAdded(Entity entity) {
        if (!(entity instanceof EndCrystal)) return;
        FightLog.spawn(entity);

        if (placing && entity.blockPosition().equals(placingCrystalBlockPos)) {
            placing = false;
            placingTimer = 0;
            placedCrystals.add(entity.getId());
            roundTrip.add((System.nanoTime() - placeSentAt) / 1e6);
            placeAnswered = true;
            if (turnedPlacePending) {
                turnedPlacePending = false;
                turnFails = 0;
            }
        }

        explain(entity);
    }

    private void explain(Entity crystal) {
        if (!explain.get() || !doBreak.get() || crosshair.get() || removed.contains(crystal.getId())) return;
        if (isOutOfRange(crystal.position(), crystal.blockPosition(), false)) return;

        if (targets.isEmpty()) {
            if (!saidNoTarget) {
                saidNoTarget = true;
                info("No target within %.0f blocks.", targetRange.get());
            }
            return;
        }

        if (age - leftAloneAt < 100) return;
        blockPos.set(crystal.blockPosition()).move(0, -1, 0);
        double worth;
        boolean was = ignoreWindow;
        ignoreWindow = true;
        try {
            worth = scoreCrystal(crystal.position(), blockPos, false, false);
        } finally {
            ignoreWindow = was;
        }
        if (worth != CrystalLogic.NONE) return;

        leftAloneAt = age;
        info("Skipped: %.1f to %s, %.1f to you.", evalDamage, evalTarget == null ? "nobody" : EntityUtils.getName(evalTarget), evalSelf);
    }

    private void report() {
        age++;

        boolean lag = pauseOnLag.get() && ActionWindow.timeSinceLastTick() >= 1.0f;
        lagTicks = lag ? lagTicks + 1 : 0;
        if (lag) statLag++;
        if (targets.isEmpty()) statNoTarget++;

        if (lagTicks == 60 && !saidLag) {
            saidLag = true;
            warning("Server lag: paused 3 s (Pause On Lag).");
        }

        if (age % 100 != 0) return;
        if (statNoTarget < 100 || statPlaced + statHits > 0) {
            Autism.LOG.info("[CrystalAura] 5 s: {} targets now, {} ticks without one, {} ticks paused for lag, placed {}, hit {}, exploded {}, gave up on {}; last crystal looked at: {} to {}, {} to self; crosshair-only {}, ping {}",
                targets.size(), statNoTarget, statLag, statPlaced, statHits, statExploded, statGaveUp,
                evalDamage, evalTarget == null ? "nobody" : EntityUtils.getName(evalTarget), evalSelf, crosshair.get(), PlayerUtils.getPing());
        }
        if (!targets.isEmpty() && statPlaced == 0) {
            String idle = String.format(java.util.Locale.ROOT, "no click in 5 s: last search saw %d obsidian or bedrock in range, %d of them usable, %d not under your look, %d refused for the damage to you (least %.1f; max-damage %.1f), %d for hurting you more than the target, %d spots to build on; refused by reason: %s; search held back %d ticks (last pause: %s); sends waited %d ticks for the look and %d for the tick's order, %d turned, turning %s; support %s, obsidian in hotbar %s, crystals in hotbar %s, health %.1f, trade-totem %s",
                seenBases, usableBases, unseenBases, selfRefused, selfRefused > 0 ? leastSelf : 0, maxDamage.get(), ratioRefused, buildSpots, refusals.isEmpty() ? "none" : refusals.toString(), statBlocked, pauseWhy, statLookWait, statOrderWait, statTurned, turnOff ? "given up" : turnToPlace.get() ? "on" : "off", support.get(), InvUtils.testInHotbar(Items.OBSIDIAN), InvUtils.testInHotbar(Items.END_CRYSTAL), EntityUtils.getTotalHealth(mc.player), tradeTotem.get());
            FightLog.note("IDLE", "%s", idle);
            Autism.LOG.info("[CrystalAura] {}", idle);
            if (!saidSelfRefusal && usableBases == 0 && selfRefused > 0) {
                saidSelfRefusal = true;
                warning("Every base costs you %.0f+; %s", leastSelf, !tradeTotem.get() ? "raise Max Self Damage." : Versions.aggressiveTotemOn() ? "Trade Totem needs a hotbar totem." : "Trade Totem needs Aggressive Auto Totem.");
            }
            if (!saidRatioRefusal && usableBases == 0 && ratioRefused > 0) {
                saidRatioRefusal = true;
                warning("Every base hurts you more; lower Damage Ratio.");
            }
        }
        statNoTarget = statLag = statPlaced = statHits = statExploded = statGaveUp = statBlocked = statLookWait = statOrderWait = statTurned = 0;
        pauseWhy = "nothing";
    }

    public void onEntityRemoved(Entity entity) {
        if (entity instanceof EndCrystal) {
            FightLog.gone(entity);
            placedCrystals.remove(entity.getId());
            removed.remove(entity.getId());
            Hit hit = hits.remove(entity.getId());
            if (waitingToExplode.containsKey(entity.getId())) {
                statExploded++;
                if (entity.getId() == turnedHitId) {
                    turnedHitId = Integer.MIN_VALUE;
                    turnFails = 0;
                }
                if (hit != null) {
                    if (attemptedBreaks.get(entity.getId()) <= 1) roundTrip.add((System.nanoTime() - hit.sentAt()) / 1e6);
                    if (hit.target() != null) {
                        window(hit.target()).exploded(age, hit.damage());
                        FightLog.note("WINDOW", "%s; our crystal went off, predicted %.1f; round trip %.0f ms; aim %.0f ms", EntityUtils.getName(hit.target()), hit.damage(), roundTrip.ms(), aim.ms());
                    }
                }
            }
            waitingToExplode.remove(entity.getId());
            attemptedBreaks.remove(entity.getId());
        }
    }

    private int latencyMs() {
        return (int) Math.max(PlayerUtils.getPing(), roundTrip.ms());
    }

    private Vec3 lead(LivingEntity target, boolean breaking) {
        Int2ObjectMap<Vec3> cache = breaking ? breakLeads : placeLeads;
        Vec3 cached = cache.get(target.getId());
        if (cached != null) return cached;

        double ticks = Math.min(8, (breaking ? 1 : 2) * roundTrip.ticks() + 1);
        double[] o = CrystalLogic.lead(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo, target.onGround(), ticks);
        Vec3 offset = Entity.collideBoundingBox(target, new Vec3(o[0], o[1], o[2]), target.getBoundingBox(), mc.level, List.of());

        cache.put(target.getId(), offset);
        return offset;
    }

    private static double nowMs() {
        return System.nanoTime() / 1e6;
    }

    private CrystalLogic.Window window(LivingEntity target) {
        return windows.computeIfAbsent(target.getId(), unused -> new CrystalLogic.Window());
    }

    private void watchHealth() {
        int ownHurt = mc.player.hurtTime;
        if (ownHurt > selfSeenHurt) selfWindow.flinched(age);
        selfSeenHurt = ownHurt;
        float ownHealth = EntityUtils.getTotalHealth(mc.player);
        if (selfSeenHealth >= 0 && ownHealth < selfSeenHealth - 0.05f && ownHurt > 0) {
            selfWindow.damaged(age, selfSeenHealth - ownHealth);
            FightLog.note("WINDOW", "SELF hit for %.1f: shielded for the rest of the window", selfSeenHealth - ownHealth);
        }
        selfSeenHealth = ownHealth;
        if (Math.abs(selfWindow.startedAt() - explosionAge) <= 2) shieldStart = selfWindow.startedAt();

        targetIds.clear();
        for (LivingEntity target : targets) targetIds.add(target.getId());
        seenHealth.keySet().retainAll(targetIds);
        poppedAt.int2IntEntrySet().removeIf(entry -> age - entry.getIntValue() > CrystalLogic.POP_ABSORPTION_TICKS);
        seenHurt.keySet().retainAll(targetIds);
        windows.keySet().retainAll(targetIds);

        for (LivingEntity target : targets) {
            int id = target.getId();
            CrystalLogic.Window window = window(target);

            int hurt = target.hurtTime;
            if (hurt > seenHurt.getOrDefault(id, hurt)) {
                if (window.flinched(age)) aim.counted();
                FightLog.note("WINDOW", "%s flinched", EntityUtils.getName(target));
            }
            seenHurt.put(id, hurt);

            float health = EntityUtils.getTotalHealth(target);
            float before = seenHealth.getOrDefault(id, health);
            seenHealth.put(id, health);
            if (health < before - 0.05f && poppedAt.containsKey(id) && !(age - poppedAt.get(id) <= 5 && health <= 1.05f)) poppedAt.remove(id);
            if (health < before - 0.05f && hurt > 0 && window.damaged(age, before - health)) aim.counted();

            if (window.expire(age)) {
                aim.missed();
                FightLog.note("WINDOW", "%s; our hit did not count, aim now %.0f ms", EntityUtils.getName(target), aim.ms());
            }
        }
    }

    /** Network thread. */
    public void onPacketReceive(Packet<?> packet) {
        if (packet instanceof ClientboundExplodePacket explosion) {
            Vec3 center = explosion.center();
            later(() -> {
                if (running && mc.player != null && center.distanceToSqr(mc.player.position()) <= 144) explosionAge = age;
            });
            return;
        }
        if (packet instanceof ClientboundContainerSetSlotPacket slot) {
            if (slot.getContainerId() == 0 && slot.getSlot() == InventoryMenu.SHIELD_SLOT && slot.getItem().is(Items.TOTEM_OF_UNDYING)) later(() -> offhandSpent = false);
            return;
        }
        if (packet instanceof ClientboundContainerSetContentPacket content) {
            if (content.containerId() == 0 && content.items().size() > InventoryMenu.SHIELD_SLOT && content.items().get(InventoryMenu.SHIELD_SLOT).is(Items.TOTEM_OF_UNDYING)) later(() -> offhandSpent = false);
            return;
        }
        if (!(packet instanceof ClientboundEntityEventPacket entityEvent) || entityEvent.getEventId() != 35) return;
        later(() -> {
            if (!running || mc.level == null) return;
            if (!(entityEvent.getEntity(mc.level) instanceof LivingEntity popped)) return;
            if (popped == mc.player) {
                selfWindow.popped(age);
                if (!mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
                    offhandSpent = true;
                    spentAge = age;
                }
            }
            else {
                window(popped).popped(age);
                poppedAt.put(popped.getId(), age);
            }
        });
    }

    // Break

    private void doBreak() {
        if (!doBreak.get() || breakTimer > 0 || switchTimer > 0 || attacks >= attackFrequency.get()) return;
        if (shouldPause(PauseMode.Break)) return;

        double bestScore = CrystalLogic.NONE;
        Entity crystal = null;
        heldDueAt = Double.MAX_VALUE;

        List<Entity> held = new ArrayList<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            dtapHeld = false;
            double score = getBreakScore(entity, true);
            if (score == CrystalLogic.NONE && dtapHeld) held.add(entity);

            if (score > bestScore) {
                bestScore = score;
                crystal = entity;
                hitDamage = scoreDamage;
                hitTarget = scoreTarget;
            }
        }

        for (Entity h : held) {
            if (crystal == null || h.distanceToSqr(crystal) > 12 * 12) continue;
            dtapRelease = true;
            try {
                crystal = getBreakScore(h, true) > CrystalLogic.NONE ? h : null;
            } finally {
                dtapRelease = false;
            }
            hitDamage = scoreDamage;
            hitTarget = scoreTarget;
            if (crystal != null) FightLog.note("DTAP", "id %d let go: another crystal within 12 blocks was to be hit", h.getId());
            break;
        }

        if (crystal != null) doBreak(crystal);
    }

    private double getBreakScore(Entity entity, boolean checkCrystalAge) {
        if (!(entity instanceof EndCrystal)) return CrystalLogic.NONE;
        if (crosshair.get() && !underCrosshair(entity)) return CrystalLogic.NONE;
        if (onlyBreakOwn.get() && !placedCrystals.contains(entity.getId())) return CrystalLogic.NONE;
        if (removed.contains(entity.getId())) return CrystalLogic.NONE;
        if (attemptedBreaks.get(entity.getId()) > breakAttempts.get()) return CrystalLogic.NONE;
        if (checkCrystalAge && entity.tickCount < ticksExisted.get()) return CrystalLogic.NONE;
        if (isOutOfRange(entity.position(), entity.blockPosition(), false)) return CrystalLogic.NONE;

        Vec3 eye = mc.player.getEyePosition();
        double[] crystal = crystalBox(entity.getX(), entity.getY(), entity.getZ());
        double reach = mc.player.entityInteractionRange() - REACH_MARGIN;
        boolean lookOn = LookMath.reach(eye.x, eye.y, eye.z, ActionWindow.yaw(), ActionWindow.pitch(), crystal) <= reach;
        if (!lookOn && !(turnable && LookMath.nearest(eye.x, eye.y, eye.z, crystal) <= reach - TURN_MARGIN)) return CrystalLogic.NONE;

        blockPos.set(entity.blockPosition()).move(0, -1, 0);
        boolean instant = age - instantAge < INSTANT_TICKS && blockPos.equals(instantBase);
        double score;
        boolean was = ignoreWindow;
        if (instant) ignoreWindow = true;
        try {
            score = scoreCrystal(entity.position(), blockPos, true, false);
        } finally {
            ignoreWindow = was;
        }

        if (score == CrystalLogic.NONE && worthScore == CrystalLogic.NONE && !targets.isEmpty() && !ignoreWindow && smartDelay.get() && deny.get() && !placedCrystals.contains(entity.getId())
            && CrystalLogic.deny(evalSelf, evalSelfCost, CrystalLogic.popBar(EntityUtils.getTotalHealth(mc.player), popMargin.get() / 100.0))) {
            scoreDamage = 0;
            scoreTarget = null;
            return CrystalLogic.DENY_SCORE;
        }

        return score;
    }

    private boolean underCrosshair(Entity entity) {
        HitResult hit = mc.hitResult;
        if (hit instanceof EntityHitResult entityHit) return entityHit.getEntity() == entity;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(mc.player.entityInteractionRange()));
        Vec3 point = entity.getBoundingBox().clip(eye, end).orElse(null);
        if (point == null) return false;
        return hit == null || hit.getType() == HitResult.Type.MISS || eye.distanceToSqr(point) <= eye.distanceToSqr(hit.getLocation());
    }

    private void doBreak(Entity crystal) {
        if (antiWeakness.get()) {
            MobEffectInstance weakness = mc.player.getEffect(MobEffects.WEAKNESS);
            MobEffectInstance strength = mc.player.getEffect(MobEffects.STRENGTH);

            if (weakness != null && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())) {
                if (!isValidWeaknessItem(mc.player.getMainHandItem(), crystal)) {
                    if (!InvUtils.swap(InvUtils.findInHotbar(stack -> isValidWeaknessItem(stack, crystal)).slot(), false))
                        return;

                    switchTimer = 1;
                    return;
                }
            }
        }

        wantHit = crystal;
        wantHitDamage = hitDamage;
        wantHitTarget = hitTarget;
    }

    private void hitSent(Entity crystal, LivingEntity target, float damage) {
        breakTimer = breakDelay.get();
        if (crystal.blockPosition().below().equals(instantBase)) instantAge = -1000;
        removed.add(crystal.getId());
        attemptedBreaks.put(crystal.getId(), attemptedBreaks.get(crystal.getId()) + 1);
        waitingToExplode.put(crystal.getId(), 0);
        hits.put(crystal.getId(), new Hit(target, damage, System.nanoTime()));
        if (target != null) window(target).sent(damage, roundTrip.ms(), CrystalLogic.answerTicks(latencyMs()) * 50);
        if (target == null) FightLog.note("DENY", "id %d set off inside our own damage window", crystal.getId());
    }

    private boolean isValidWeaknessItem(ItemStack itemStack, Entity crystal) {
        return DamageUtils.getAttackDamage(mc.player, crystal, itemStack) > 0;
    }

    private void attackCrystal(Entity entity, LivingEntity target, float damage) {
        // MC 26.3 follows every attack with ServerboundPunchPacket.INSTANCE, whatever the arm is shown doing
        ActionWindow.own(() -> {
            mc.player.connection.send(new ServerboundAttackPacket(entity.getId()));
            mc.player.connection.send(ServerboundPunchPacket.INSTANCE);
        });
        swingMode.get().attackAnimation(InteractionHand.MAIN_HAND);

        attacks++;
        statHits++;
        lastActionAge = age;
        float self = DamageUtils.crystalDamage(mc.player, entity.position(), predictMovement.get(), entity.blockPosition().below());
        FightLog.attack(entity, attemptedBreaks.get(entity.getId()), String.format(java.util.Locale.ROOT, "predicted %.1f to %s (health %.1f), %.1f to you (health %.1f)",
            damage, target == null ? "nobody (deny)" : EntityUtils.getName(target), target == null ? 0 : targetHealth(target), self, EntityUtils.getTotalHealth(mc.player)));
    }

    public void onPacketSend(Packet<?> packet) {
        onUseSend(packet);
        if (packet instanceof ServerboundSetCarriedItemPacket) {
            switchTimer = switchDelay.get();
        }
    }

    // Place

    private boolean cannotPlace() {
        if (!doPlace.get() || placeTimer > 0) return true;
        if (shouldPause(PauseMode.Place)) return true;
        if (placing) return true;
        if (Versions.totemReselectPending()) return true;
        if (!InvUtils.testInHotbar(Items.END_CRYSTAL)) return true;

        if (autoSwitch.get() != AutoSwitchMode.None) {
            if (noGapSwitch.get() && autoSwitch.get() == AutoSwitchMode.Normal && offItem != Items.END_CRYSTAL) {
                if (mainItem == Items.ENCHANTED_GOLDEN_APPLE
                    || offItem == Items.ENCHANTED_GOLDEN_APPLE
                    || mainItem == Items.GOLDEN_APPLE
                    || offItem == Items.GOLDEN_APPLE) return true;
            }
            if (noBowSwitch.get() && (mainItem == Items.BOW || offItem == Items.BOW)) return true;
        } else if (mainItem != Items.END_CRYSTAL && offItem != Items.END_CRYSTAL) return true;

        boolean was = ignoreWindow;
        ignoreWindow = true;
        try {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (getBreakScore(entity, false) > CrystalLogic.NONE) return true;
            }
        } finally {
            ignoreWindow = was;
        }

        return false;
    }

    private double scoreBase(BlockPos bp, boolean hasBlock, boolean aimed) {
        baseWorth = 0;

        refusal = "no air above it";
        blockPos.set(bp.getX(), bp.getY() + 1, bp.getZ());
        if (!mc.level.getBlockState(blockPos).isAir()) return CrystalLogic.NONE;

        if (placement112.get()) {
            blockPos.move(0, 1, 0);
            if (!mc.level.getBlockState(blockPos).isAir()) return CrystalLogic.NONE;
        }
        refusal = "beyond place-range";

        vec3d = new Vec3(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
        if (!aimed && !PlayerUtils.isWithin(vec3d, Math.max(placeRange.get(), placeWallsRange.get()))) return CrystalLogic.NONE;

        refusal = "beyond the server's block reach";
        if (!mc.player.isWithinBlockInteractionRange(bp, 1.0)) return CrystalLogic.NONE;

        Vec3 eye = mc.player.getEyePosition();
        double clickReach = mc.player.blockInteractionRange() - REACH_MARGIN, hitReach = mc.player.entityInteractionRange() - REACH_MARGIN;
        double[] block = blockBox(bp), crystal = crystalBox(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
        double blockNear = LookMath.nearest(eye.x, eye.y, eye.z, block), crystalNear = LookMath.nearest(eye.x, eye.y, eye.z, crystal);
        refusal = blockNear > clickReach ? String.format(java.util.Locale.ROOT, "block %.1f from your eyes, over the click reach %.1f", blockNear, clickReach)
            : String.format(java.util.Locale.ROOT, "crystal %.1f from your eyes, over the hit reach %.1f: it could not be set off from here", crystalNear, hitReach);
        if (blockNear > clickReach || crystalNear > hitReach) return CrystalLogic.NONE;

        if (hasBlock && !aimed) {
            boolean lookOn = LookMath.reach(eye.x, eye.y, eye.z, ActionWindow.yaw(), ActionWindow.pitch(), block) <= clickReach;
            if (!lookOn && !(turnable && blockNear <= clickReach - FACE_TURN_MARGIN && crystalNear <= hitReach - TURN_MARGIN)) {
                lookRefused = !turnable;
                refusal = turnable ? "too near the edge of reach to turn to" : "not under your look (turn-to-place off or not possible now)";
                return CrystalLogic.NONE;
            }
        }

        double score = scoreCrystal(vec3d, bp, false, !hasBlock && support.get() == SupportMode.Fast);
        float worth = worthScore != CrystalLogic.NONE ? worthDamage : 0;
        refusal = crystalWhy;
        if (score == CrystalLogic.NONE && worth == 0) return CrystalLogic.NONE;

        double x = bp.getX();
        double y = bp.getY() + 1;
        double z = bp.getZ();
        box = new AABB(x, y, z, x + 1, y + (placement112.get() ? 1 : 2), z + 1);

        refusal = "an entity in the way";
        if (intersectsWithEntities(box)) return CrystalLogic.NONE;

        if (predictMovement.get()) {
            for (LivingEntity target : targets) {
                if (box.intersects(target.getBoundingBox().move(lead(target, false)))) {
                    if (score != CrystalLogic.NONE) score -= CrystalLogic.BLOCKED;
                    break;
                }
            }
        }

        refusal = "behind a wall, beyond place-walls-range";
        if (!aimed) {
            if (hasBlock) {
                if (!PlayerUtils.isWithin(vec3d, (getVisibleFace(bp) != null ? placeRange : placeWallsRange).get())) return CrystalLogic.NONE;
            } else {
                blockPos.set(bp).move(0, 1, 0);
                if (isOutOfRange(vec3d, blockPos, true)) return CrystalLogic.NONE;
            }
        }

        baseWorth = worth;
        refusal = score == CrystalLogic.NONE ? crystalWhy : null;
        return score;
    }

    private void placeOn(BlockHitResult result, double damage, BlockPos supportBlock) {
        wantClick = result;
        wantClickDamage = damage;
        wantSupport = supportBlock;
    }

    private void doPlace() {
        if (cannotPlace()) {
            statBlocked++;
            return;
        }
        seenBases = usableBases = buildSpots = selfRefused = unseenBases = ratioRefused = 0;
        refusals.clear();
        leastSelf = Float.MAX_VALUE;

        HitResult hit = crosshair.get() ? mc.hitResult : null;
        if (hit instanceof EntityHitResult entityHit && removed.contains(entityHit.getEntity().getId())) hit = mc.player.pick(mc.player.blockInteractionRange(), 1, false);
        BlockHitResult aimed = hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK ? blockHit : null;
        if (crosshair.get() && aimed == null) return;

        AtomicDouble bestScore = new AtomicDouble(CrystalLogic.NONE), bestDamage = new AtomicDouble(0), bestWorth = new AtomicDouble(0), bestSelf = new AtomicDouble(-1);
        AtomicDouble buildScore = new AtomicDouble(CrystalLogic.NONE), buildDamage = new AtomicDouble(0);
        BlockPos.MutableBlockPos bestBlockPos = new BlockPos.MutableBlockPos(), buildBlockPos = new BlockPos.MutableBlockPos();
        boolean asked = support.get() == SupportMode.OnClick && age - buildAskedAge <= BUILD_ASK_TICKS;
        boolean build = (support.get() == SupportMode.OnClick ? asked : support.get() != SupportMode.Disabled) && aimed == null && InvUtils.testInHotbar(Items.OBSIDIAN);

        forEachBlock((int) Math.ceil(placeRange.get()), (int) Math.ceil(placeRange.get()), (bp, blockState) -> {
            if (aimed != null && !bp.equals(aimed.getBlockPos())) return;

            if (blockState.is(Blocks.BEDROCK) || blockState.is(Blocks.OBSIDIAN)) {
                evalSelf = -1;
                lookRefused = false;
                boolean fresh = instantPlace.get() && age - instantAge < INSTANT_TICKS && bp.equals(instantBase);
                boolean wasIgnoring = ignoreWindow;
                if (fresh) ignoreWindow = true;
                double timed;
                try {
                    timed = scoreBase(bp, true, aimed != null);
                } finally {
                    ignoreWindow = wasIgnoring;
                }
                seenBases++;
                if (lookRefused) unseenBases++;
                if (timed == CrystalLogic.NONE && refusal != null) refusals.merge(refusal.split(":")[0], 1, Integer::sum);
                if (fresh) {
                    String verdict = timed != CrystalLogic.NONE ? "usable" : "refused: " + (refusal == null ? "unknown" : refusal);
                    if (!verdict.equals(instantSaid)) {
                        instantSaid = verdict;
                        FightLog.note("OBSIDIAN", "at %d %d %d, %.1f blocks from your eyes, %d ticks after it appeared: %s", bp.getX(), bp.getY(), bp.getZ(),
                            Math.sqrt(mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(bp))), age - instantAge, verdict);
                    }
                }
                if (baseWorth > bestWorth.get()) bestWorth.set(baseWorth);
                if (timed == CrystalLogic.NONE) {
                    if (evalSelf >= 0 && limitLifted) {
                        if (evalDamage < evalSelf * damageRatio.get()) ratioRefused++;
                    }
                    else if (evalSelf > maxDamage.get() || (evalSelf >= 0 && evalSelf >= EntityUtils.getTotalHealth(mc.player) - CrystalLogic.SUICIDE_MARGIN)) {
                        selfRefused++;
                        leastSelf = Math.min(leastSelf, (float) evalSelf);
                    }
                    return;
                }
                usableBases++;

                if (age - lastActionAge < 20 && bp.getX() == placingCrystalBlockPos.getX() && bp.getY() + 1 == placingCrystalBlockPos.getY() && bp.getZ() == placingCrystalBlockPos.getZ()) timed += CrystalLogic.STICKY;

                if (timed > bestScore.get()) {
                    bestScore.set(timed);
                    bestDamage.set(scoreDamage);
                    bestSelf.set(evalSelf);
                    bestBlockPos.set(bp);
                }
            }
            else if (build && blockState.canBeReplaced()) {
                if (!baseSpot(bp) || BlockUtils.getPlaceSide(bp) == null || !BlockUtils.canPlace(bp, true)) return;

                double score;
                boolean was = ignoreWindow;
                ignoreWindow = true;
                try {
                    score = scoreBase(bp, false, false);
                } finally {
                    ignoreWindow = was;
                }
                if (score != CrystalLogic.NONE) buildSpots++;
                if (score > buildScore.get()) {
                    buildScore.set(score);
                    buildDamage.set(scoreDamage);
                    buildBlockPos.set(bp);
                }
            }
        });

        {
            if (baseTimer == 0 && buildScore.get() != CrystalLogic.NONE && CrystalLogic.worthBase(bestWorth.get(), buildDamage.get())) {
                BlockPos base = buildBlockPos.immutable();
                placeOn(getPlaceInfo(base), buildDamage.get(), base);
            }
            else if (asked && buildScore.get() == CrystalLogic.NONE && bestScore.get() == CrystalLogic.NONE && age - saidNoSpotAge >= 40) {
                saidNoSpotAge = age;
                info("No spot hurts the target enough.");
            }
            else if (bestScore.get() != CrystalLogic.NONE) {
                placeSelf = bestSelf.get();
                placeOn(aimed != null ? aimed : getPlaceInfo(bestBlockPos.immutable()), bestDamage.get(), null);
            }
        }
    }

    private static void forEachBlock(int hRadius, int vRadius, java.util.function.BiConsumer<BlockPos, BlockState> visit) {
        int px = mc.player.getBlockX(), py = mc.player.getBlockY(), pz = mc.player.getBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = px - hRadius; x <= px + hRadius; x++) {
            for (int z = pz - hRadius; z <= pz + hRadius; z++) {
                for (int y = Math.max(mc.level.getMinY(), py - vRadius); y <= py + vRadius; y++) {
                    if (y > mc.level.getHeight()) break;
                    pos.set(x, y, z);
                    visit.accept(pos, mc.level.getBlockState(pos));
                }
            }
        }
    }

    private boolean baseSpot(BlockPos bp) {
        double x = bp.getX() + 0.5, y = bp.getY() + 0.5, z = bp.getZ() + 0.5;
        double mine = mc.player.distanceToSqr(x, y, z);

        for (LivingEntity target : targets) {
            if (bp.getY() > Mth.floor(target.getY())) continue;
            double dx = x - target.getX(), dz = z - target.getZ();
            if (dx * dx + dz * dz > 16.5) continue;
            if (mine < mc.player.distanceToSqr(target)) return true;
        }
        return false;
    }

    public void onBlockUpdate(BlockPos pos, BlockState newState) {
        if (!instantPlace.get() || crosshair.get() || mc.player == null) return;
        if (!newState.is(Blocks.OBSIDIAN) && !newState.is(Blocks.BEDROCK)) return;
        if (targets.isEmpty() || !PlayerUtils.isWithin(pos, Math.max(placeRange.get(), placeWallsRange.get()) + 2)) return;

        instantBase.set(pos);
        instantAge = age;
        instantSaid = null;
    }

    private BlockHitResult getVisibleFace(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            vec3dRayTraceEnd = new Vec3(
                blockPos.getX() + 0.5 + side.getUnitVec3i().getX() * 0.5,
                blockPos.getY() + 0.5 + side.getUnitVec3i().getY() * 0.5,
                blockPos.getZ() + 0.5 + side.getUnitVec3i().getZ() * 0.5
            );

            clipContext = new ClipContext(playerEyePos, vec3dRayTraceEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
            BlockHitResult result = mc.level.clip(clipContext);

            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                return result;
            }
        }

        return null;
    }

    private BlockHitResult getPlaceInfo(BlockPos blockPos) {
        BlockHitResult result = getVisibleFace(blockPos);
        if (result != null) return result;

        Direction side = blockPos.getY() > playerEyePos.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(new Vec3(blockPos.getX() + 0.5, blockPos.getY() + (side == Direction.UP ? 1 : 0), blockPos.getZ() + 0.5), side, blockPos, false);
    }

    private boolean placeCrystal(BlockHitResult result, double damage, BlockPos supportBlock) {
        Item targetItem = supportBlock == null ? Items.END_CRYSTAL : Items.OBSIDIAN;

        FindItemResult item = InvUtils.findInHotbar(targetItem);
        if (!item.found()) return false;

        int prevSlot = mc.player.getInventory().getSelectedSlot();

        if (autoSwitch.get() != AutoSwitchMode.None && !item.isOffhand()) InvUtils.swap(item.slot(), false);
        if (autoSwitch.get() == AutoSwitchMode.Normal && supportBlock == null && !item.isOffhand() && item.slot() != prevSlot) {
            if (switchBackSlot < 0) switchBackSlot = prevSlot;
            switchedTo = item.slot();
        }

        InteractionHand hand = item.getHand();
        if (hand == null) return false;

        if (supportBlock == null) {
            ActionWindow.own(() -> ((AutismMultiPlayerGameModeAccessor) mc.gameMode).autism$startPrediction(mc.level, sequence -> new ServerboundUseItemOnPacket(hand, result, sequence)));

            swingMode.get().interactSwing(hand);

            placing = true;
            statPlaced++;
            lastActionAge = age;
            FightLog.place(result, hand, damage, placeSelf, evalTarget);
            placingTimer = CrystalLogic.answerTicks(latencyMs());
            boolean retry = !placeAnswered && result.getBlockPos().getX() == placingCrystalBlockPos.getX() && result.getBlockPos().getY() + 1 == placingCrystalBlockPos.getY() && result.getBlockPos().getZ() == placingCrystalBlockPos.getZ();
            if (!retry) placeSentAt = System.nanoTime();
            placeAnswered = false;
            kaTimer = 8;
            placingCrystalBlockPos.set(result.getBlockPos()).move(0, 1, 0);
        } else {
            ActionWindow.own(() -> BlockUtils.interact(result, hand, swingMode.get()));
            buildAskedAge = -1000;
            baseTimer = BASE_COOLDOWN;
            placeTimer += supportDelay.get();

            FightLog.note("BASE", "obsidian at %d %d %d for a crystal predicted to deal %.1f", supportBlock.getX(), supportBlock.getY(), supportBlock.getZ(), damage);
        }

        if (autoSwitch.get() == AutoSwitchMode.Silent) InvUtils.swap(prevSlot, false);
        return true;
    }

    private void onUseSend(Packet<?> packet) {
        if (packet instanceof ServerboundUseItemPacket use && use.hand() == InteractionHand.MAIN_HAND && support.get() == SupportMode.OnClick
            && mc.player != null && mc.player.getMainHandItem().is(Items.OBSIDIAN) && (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS)) {
            buildAskedAge = age;
        }
    }

    // Face place

    private boolean shouldFacePlace(LivingEntity target) {
        if (!facePlace.get()) return false;
        if (forceFacePlacePressed()) return true;
        if (targetHealth(target) <= facePlaceHealth.get()) return true;

        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
            ItemStack itemStack = target.getItemBySlot(slot);

            if (itemStack == null || itemStack.isEmpty()) {
                if (facePlaceArmor.get()) return true;
            } else {
                if ((double) (itemStack.getMaxDamage() - itemStack.getDamageValue()) / itemStack.getMaxDamage() * 100 <= facePlaceDurability.get())
                    return true;
            }
        }

        return false;
    }

    // Others

    private boolean shouldPause(PauseMode process) {
        if (crosshair.get() && mc.getCameraEntity() != mc.player) return true;

        if (mc.player.isUsingItem() || (mc.options.keyUse.isDown() && (mc.player.getMainHandItem().getUseAnimation() != ItemUseAnimation.NONE || mc.player.getOffhandItem().getUseAnimation() != ItemUseAnimation.NONE))) {
            if (pauseOnUse.get().matches(process)) return paused("an item in use");
        }

        if (pauseOnLag.get() && ActionWindow.timeSinceLastTick() >= 1.0f) return paused("lag");
        if (pauseOnMine.get().matches(process) && mc.gameMode.isDestroying()) return paused("mining");
        return EntityUtils.getTotalHealth(mc.player) <= pauseHealth.get() && paused("health at or under pause-health");
    }

    private boolean paused(String why) {
        pauseWhy = why;
        return true;
    }

    private boolean isOutOfRange(Vec3 vec3d, BlockPos blockPos, boolean place) {
        clipContext = new ClipContext(playerEyePos, vec3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);

        BlockHitResult result = mc.level.clip(clipContext);

        if (result == null || !result.getBlockPos().equals(blockPos))
            return !PlayerUtils.isWithin(vec3d, (place ? placeWallsRange : breakWallsRange).get());
        return !PlayerUtils.isWithin(vec3d, (place ? placeRange : breakRange).get());
    }

    private LivingEntity getNearestTarget() {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity target : targets) {
            double distance = PlayerUtils.squaredDistanceTo(target);

            if (distance < nearestDistance) {
                nearestTarget = target;
                nearestDistance = distance;
            }
        }

        return nearestTarget;
    }

    private double scoreCrystal(Vec3 vec3d, BlockPos obsidianPos, boolean breaking, boolean fast) {
        float selfDamage = DamageUtils.crystalDamage(mc.player, vec3d, predictMovement.get(), obsidianPos);
        float selfHealth = EntityUtils.getTotalHealth(mc.player);
        evalSelf = selfDamage;
        evalDamage = 0;
        evalTarget = null;
        crystalWhy = "no target near enough to hurt";
        worthScore = CrystalLogic.NONE;
        if (offhandSpent && age - spentAge > SPENT_TICKS && mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) offhandSpent = false;
        String wait = offhandSpent ? "your next totem is not in your offhand on the server yet"
            : popPending(vec3d, (float) CrystalLogic.popBar(selfHealth, popMargin.get() / 100.0)) ? "a pop of yours is already on its way (one totem at a time)" : null;
        selfHealth = (float) CrystalLogic.popBar(CrystalLogic.judgedHealth(selfHealth, wait != null), popMargin.get() / 100.0);
        boolean suicide = antiSuicide.get() && selfDamage >= selfHealth - CrystalLogic.SUICIDE_MARGIN;
        boolean spare = wait == null && tradeTotem.get() && totemsLeft > keepTotems.get() && mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
            && Versions.aggressiveTotemOn() && InvUtils.find(stack -> stack.is(Items.TOTEM_OF_UNDYING), 0, 8).found();
        limitLifted = spare;
        if (suicide && !spare) {
            crystalWhy = String.format(java.util.Locale.ROOT, "would kill you (%s): %.1f to you", wait != null ? wait
                : tradeTotem.get() && totemsLeft <= keepTotems.get() ? totemsLeft + " totems left, no trades (keep-totems)" : "anti-suicide", selfDamage);
            return CrystalLogic.NONE;
        }
        CrystalLogic.Rules trade = spare ? CrystalLogic.traded(rules) : null;

        boolean shield = smartDelay.get() && shieldStart == selfWindow.startedAt();
        double selfCost = shield ? selfWindow.shielded(selfDamage, age, (breaking ? 1 : 2) * roundTrip.slowestTicks()) : selfDamage;
        evalSelfCost = selfCost;

        LivingEntity nearest = fast ? getNearestTarget() : null;
        double best = CrystalLogic.NONE;

        for (LivingEntity target : targets) {
            if (fast && target != nearest) continue;

            float damage = predictMovement.get()
                ? DamageUtils.crystalDamage(target, vec3d, lead(target, breaking), obsidianPos)
                : DamageUtils.crystalDamage(target, vec3d, false, obsidianPos);

            if (damage > evalDamage) {
                evalDamage = damage;
                evalTarget = target;
            }

            if (damage > bestTargetDamage) {
                bestTarget = target;
                bestTargetDamage = damage;
                bestTargetTimer = 10;
            }

            if (target.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || target.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) seenTotem.add(target.getId());
            boolean totem = seenTotem.contains(target.getId());
            double health = targetHealth(target);
            boolean face = shouldFacePlace(target);

            boolean kill = spare;
            double worth = kill ? CrystalLogic.score(trade, damage, selfDamage, health, selfHealth, face, false)
                : suicide ? CrystalLogic.NONE : CrystalLogic.score(rules, damage, selfDamage, health, selfHealth, face, totem);
            if (worth > worthScore) {
                worthScore = worth;
                worthDamage = damage;
            }

            double timed = worth;
            if (smartDelay.get()) {
                double counted = damage;
                boolean hold = false;
                CrystalLogic.Window window = windows.get(target.getId());
                if (window != null && age - lastActionAge < PATIENCE) {
                    double leadMs = roundTrip.ms() + (breaking ? 0 : roundTrip.ms() + placeAhead.get());
                    counted = window.countedAt(damage, leadMs, aim.ms());
                    if (breaking && !ignoreWindow && counted != damage && worth != CrystalLogic.NONE) heldDueAt = Math.min(heldDueAt, nowMs() + window.dueIn(leadMs, aim.ms()));
                    if (breaking && !ignoreWindow && !dtapRelease && dtap.get() && counted > 0 && counted < damage && CrystalLogic.lethal(rules, counted, health)) {
                        double dtapLead = CrystalLogic.dtapLeadMs(roundTrip.ms(), roundTrip.slowestMs(), aim.ms());
                        int handTotems = (target.getOffhandItem().is(Items.TOTEM_OF_UNDYING) ? 1 : 0) + (target.getMainHandItem().is(Items.TOTEM_OF_UNDYING) ? 1 : 0);
                        net.minecraft.world.damagesource.DamageSource blast = mc.level.damageSources().explosion(null);
                        double excess = handTotems != 1 ? 0 : CrystalLogic.rawExcess(raw -> DamageUtils.calculateReductions((float) raw, target, blast), damage, damage - counted);
                        hold = CrystalLogic.dtapHold(rules, excess, damage, health, handTotems, window.dueIn(leadMs, aim.ms()), dtapLead, selfCost, selfDamage);
                        dtapHeld |= hold;
                        if (hold && dtapNotedAt != window.startedAt()) {
                            dtapNotedAt = window.startedAt();
                            FightLog.note("DTAP", "%s; holding a pop of %.1f (%.1f after armour, full %.1f) to land %.0f ms before the window ends", EntityUtils.getName(target), excess, counted, damage, dtapLead);
                        }
                    }
                }
                if (hold) timed = CrystalLogic.NONE;
                else if (counted != damage || selfCost != selfDamage) {
                    timed = spare ? worth == CrystalLogic.NONE ? CrystalLogic.NONE : CrystalLogic.score(trade, counted, selfCost, health, selfHealth, face, false)
                        : suicide ? CrystalLogic.NONE : CrystalLogic.score(rules, counted, selfCost, health, selfHealth, face, totem);
                }
            }

            double score = ignoreWindow && !(kill && selfDamage >= selfHealth - CrystalLogic.SUICIDE_MARGIN) ? worth : timed;
            if (target == evalTarget) {
                crystalWhy = worth == CrystalLogic.NONE
                    ? (suicide && !kill ? "would kill you (anti-suicide)" : CrystalLogic.why(kill ? trade : rules, damage, selfDamage, health, selfHealth, face, !kill && totem))
                    : score == CrystalLogic.NONE ? "waiting for the target's damage window (smart-delay)" : null;
                if (crystalWhy != null && !crystalWhy.startsWith("waiting")) crystalWhy += String.format(java.util.Locale.ROOT, ": %.1f to %s, %.1f to you", damage, EntityUtils.getName(target), selfDamage);
            }
            if (score > best) {
                best = score;
                scoreDamage = damage;
                scoreTarget = target;
            }
        }

        return best;
    }

    private boolean popPending(Vec3 except, float selfHealth) {
        double pops = selfHealth - CrystalLogic.SUICIDE_MARGIN;
        if (placing && placeSelf >= pops && !placingCrystalBlockPos.equals(BlockPos.containing(except))) return true;
        if (threatAge != age) {
            threatAge = age;
            threats.clear();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof EndCrystal) || entity.distanceToSqr(mc.player) > 144) continue;
                if (DamageUtils.crystalDamage(mc.player, entity.position(), predictMovement.get(), entity.blockPosition().below()) >= pops) threats.add(entity.position());
            }
        }
        for (Vec3 threat : threats) {
            if (!threat.equals(except)) return true;
        }
        return false;
    }

    private double targetHealth(LivingEntity target) {
        int popped = poppedAt.getOrDefault(target.getId(), Integer.MIN_VALUE);
        return CrystalLogic.targetHealth(EntityUtils.getTotalHealth(target), target.getAbsorptionAmount(), popped == Integer.MIN_VALUE ? -1 : age - popped);
    }

    @Override
    public String info() {
        if (bestTarget != null && bestTargetTimer > 0) return EntityUtils.getName(bestTarget);
        return targets.isEmpty() ? "" : EntityUtils.getName(targets.get(0));
    }

    private void findTargets() {
        targets.clear();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;

            if (livingEntity instanceof Player player) {
                if (player.getAbilities().instabuild || livingEntity == mc.player) continue;
                if (!player.isAlive() || Autism.excluded(player)) continue;

                if (ignoreNakeds.get()) {
                    if (player.getOffhandItem().isEmpty()
                        && player.getMainHandItem().isEmpty()
                        && player.getItemBySlot(EquipmentSlot.FEET).isEmpty()
                        && player.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                        && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                        && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                    ) continue;
                }
            }

            if (!(entityTypes().contains(livingEntity.getType()))) continue;
            if (livingEntity.distanceToSqr(mc.player) > targetRange.get() * targetRange.get()) continue;

            targets.add(livingEntity);
        }
    }

    private boolean intersectsWithEntities(AABB box) {
        return EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !removed.contains(entity.getId()));
    }

    public enum AutoSwitchMode {
        Normal,
        Silent,
        None
    }

    public enum SupportMode {
        Disabled,
        OnClick,
        Accurate,
        Fast
    }

    public enum PauseMode {
        Both,
        Place,
        Break,
        None;

        public boolean matches(PauseMode process) {
            return this == process || this == PauseMode.Both;
        }
    }
}
