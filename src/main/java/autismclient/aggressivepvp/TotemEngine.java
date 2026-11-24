package autismclient.aggressivepvp;

import autismclient.aggressivepvp.InvUtils.FindItemResult;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.ValueRange;
import autismclient.modules.Module;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.Random;
import java.util.Set;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/**
 * The Donut Meteor fork's Auto Totem, integrated into AUTISM 5.3-26.3's auto-totem module when its Version is Aggressive.
 */
public final class TotemEngine extends Engine {
    /** AUTISM's own settings that stay visible under both versions: the pop animation is a visual, not the totem logic. */
    private static final Set<String> SHARED = Set.of("no-render");

    private static final String GENERAL = "General", SWAP = "Swap", HEALTH = "Health";

    private final EnumSetting<Mode> mode = add(new EnumSetting<>("ag-mode", "Mode", Mode.Strict, Mode.values()).group(GENERAL)
        .description("When to hold a totem."));

    private final IntSetting delay = add(new IntSetting("ag-delay", "Delay", 0, 0, 1000, 1).sliderRange(0, 20).unit("ticks").group(GENERAL)
        .description("Ticks between slot movements."));

    private final IntSetting health = add(new IntSetting("ag-health", "Health Threshold", 10, 0, 36, 1).sliderRange(0, 20).group(HEALTH)
        .description("Totem below this health."), () -> mode.get() == Mode.Smart);

    private final BoolSetting elytra = add(new BoolSetting("ag-elytra", "While Gliding", true).group(HEALTH)
        .description("Totem while gliding."), () -> mode.get() == Mode.Smart);

    private final BoolSetting fall = add(new BoolSetting("ag-fall", "Predict Fall Damage", true).group(HEALTH)
        .description("Count incoming fall damage."), () -> mode.get() == Mode.Smart);

    private final BoolSetting explosion = add(new BoolSetting("ag-explosion", "Predict Explosion Damage", true).group(HEALTH)
        .description("Count crystals, beds, swords."), () -> mode.get() == Mode.Smart);

    // Swap

    private final BoolSetting stage = add(new BoolSetting("ag-stage", "Hotbar Totem", true).group(SWAP)
        .description("Keep a totem in hotbar."));

    private final BoolSetting stageAll = add(new BoolSetting("ag-stage-all", "Fill Hotbar", true).group(SWAP)
        .description("Fill empty hotbar slots."), () -> stage.get());

    private final BoolSetting stageWhileMoving = add(new BoolSetting("ag-stage-while-moving", "Refill While Moving", true).group(SWAP)
        .description("Refill hotbar while moving."), () -> stage.get());

    private final IntSetting stageSlot = add(new IntSetting("ag-stage-slot", "Hotbar Slot", 9, 1, 9, 1).group(SWAP)
        .description("Hotbar slot for totem."), () -> stage.get());

    private final EnumSetting<TotemLogic.ClickPolicy> clicksWhileMoving = add(new EnumSetting<>("ag-clicks-while-moving", "Clicks While Moving", TotemLogic.ClickPolicy.AfterPop, TotemLogic.ClickPolicy.values(),
        p -> p == TotemLogic.ClickPolicy.AfterPop ? "After Pop" : EnumSetting.titleCase(p)).group(SWAP)
        .description("Inventory clicks while moving."));

    private final BoolSetting instant = add(new BoolSetting("ag-instant", "Instant Re-equip", true).group(SWAP)
        .description("Re-equip the next tick."));

    private final RangeSetting popDelay = add(new RangeSetting("ag-pop-delay", "Pop Delay", new ValueRange(3, 6), 0, 40, 1).sliderRange(0, 20)
        .unit("ticks").group(SWAP).description("Random ticks after pop."), () -> !instant.get());

    private final BoolSetting explain = add(new BoolSetting("ag-explain", "Explain", false).group(GENERAL)
        .description("Say why in chat."));

    private final IntSetting reselectDelay = add(new IntSetting("ag-reselect-delay", "Reselect Delay", 0, 0, 10, 1).unit("ticks").group(SWAP)
        .description("Ticks before reselecting slot."));

    private final BoolSetting backoff = add(new BoolSetting("ag-backoff", "Backoff", true).group(SWAP)
        .description("Wait longer after refusals."));

    // Damage

    private final BoolSetting tnt = add(new BoolSetting("ag-tnt", "Predict TNT", true).group(HEALTH)
        .description("Count primed TNT."), () -> mode.get() == Mode.Smart);

    private final BoolSetting anchors = add(new BoolSetting("ag-anchors", "Predict Anchors", true).group(HEALTH)
        .description("Count charged respawn anchors."), () -> mode.get() == Mode.Smart);

    private final BoolSetting mace = add(new BoolSetting("ag-mace", "Predict Mace", true).group(HEALTH)
        .description("Count mace smashes."), () -> mode.get() == Mode.Smart);

    private final BoolSetting burning = add(new BoolSetting("ag-burning", "Predict Burning", true).group(HEALTH)
        .description("Count fire and lava."), () -> mode.get() == Mode.Smart);

    public boolean locked;
    private int totems, ticks;

    private final TotemLogic.Tracker tracker = new TotemLogic.Tracker();
    private java.lang.ref.WeakReference<LocalPlayer> trackedPlayer = new java.lang.ref.WeakReference<>(null);
    private boolean warnedBackoff, warnedWaiting;
    private boolean reselectPending;

    /** The server is on the totem's hotbar slot, not the one the client shows, until the reselect goes out. */
    public boolean reselectPending() {
        return reselectPending;
    }

    private long reselectAt;
    private boolean swapReported;
    private int swapMirrorSlot = -1;
    private ItemStack swapMirrorOld;
    private final Random random = new Random();

    private int failedSwapSlot = -1;
    private long distrustUntil;
    private static final int DISTRUST_TICKS = 60;

    private int sourceAvoiding(int avoid, int from, int to) {
        boolean[] totemAt = new boolean[36];
        for (int slot = 0; slot < 36; slot++) totemAt[slot] = mc.player.getInventory().getItem(slot).is(Items.TOTEM_OF_UNDYING);
        return TotemLogic.pick(totemAt, from, to, avoid);
    }

    private int holdsInARow;
    private long holdOffUntil;

    private int stagedSlot = -1;
    private volatile int unconfirmedSlot = -1;
    private long containerClosedAt = -1000;
    private boolean containerWasOpen;
    private static final int AFTER_CONTAINER_TICKS = 10;
    private long unconfirmedUntil;
    private long stageNoteAt = -1000;

    public TotemEngine(Module module) {
        super(module, SHARED);
        arrange(GENERAL, SWAP, HEALTH);
    }

    @Override
    protected boolean explain() {
        return explain.get();
    }

    @Override
    public void onActivate() {
        warnedBackoff = warnedWaiting = false;
        reselectPending = false;
        swapMirrorSlot = -1;
        failedSwapSlot = -1;
        distrustUntil = 0;
    }

    @Override
    public void onDeactivate() {
        if (reselectPending && mc.player != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
        }
        reselectPending = false;
    }

    @Override
    public void onPreTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) {
            reselectPending = false;
            return;
        }
        tracker.tick();
        if (mc.player != trackedPlayer.get()) {
            trackedPlayer = new java.lang.ref.WeakReference<>(mc.player);
            tracker.forget();
        }
        boolean containerOpen = mc.player.containerMenu != mc.player.inventoryMenu;
        if (containerWasOpen && !containerOpen) containerClosedAt = tracker.now();
        containerWasOpen = containerOpen;
        if (reselectPending && tracker.now() >= reselectAt) {
            reselectPending = false;
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
        }

        FindItemResult result = InvUtils.find(Items.TOTEM_OF_UNDYING);
        totems = result.count();
        if (totems <= 0) {
            locked = false;
            return;
        }

        boolean holding = mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
        if (holding) warnedWaiting = false;
        FindItemResult hotbar = InvUtils.find(stack -> stack.is(Items.TOTEM_OF_UNDYING), 0, 8);

        if (mode.get() == Mode.Strict) locked = true;
        else {
            boolean low = mc.player.getHealth() + mc.player.getAbsorptionAmount() - predictedDamage() <= health.get();
            boolean ely = elytra.get() && mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA) && mc.player.isFallFlying();
            locked = low || ely;
        }

        boolean quiet = ActionWindow.mayClickInventory();
        boolean still = still() || quiet, containerOk = containerOk();
        boolean inventoryMenu = mc.player.containerMenu == mc.player.inventoryMenu;
        boolean[] emptyHotbar = new boolean[9];
        for (int slot = 0; slot < 9; slot++) emptyHotbar[slot] = mc.player.getInventory().getItem(slot).isEmpty();
        int stageTarget = stage.get() ? TotemLogic.stageTarget(emptyHotbar, mc.player.getInventory().getSelectedSlot(), hotbar.found(), stageSlot.get() - 1, stageAll.get()) : -1;

        TotemLogic.Kind pending = tracker.pending();
        if (pending != null && tracker.verdictDue()) {
            Boolean achieved = null;
            if (pending == TotemLogic.Kind.SWAP) {
                boolean rejected = !holding && swapMirrorOld != null && ItemStack.matches(mc.player.getOffhandItem(), swapMirrorOld);
                switch (TotemLogic.swapOutcome(swapReported || rejected, holding, tracker.extended())) {
                    case SUCCESS -> achieved = true;
                    case FAILURE -> achieved = false;
                    case UNREPORTED_WAIT -> tracker.extend();
                    case UNREPORTED_REVERT -> {
                        revertMirror();
                        holding = mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
                        hotbar = InvUtils.find(stack -> stack.is(Items.TOTEM_OF_UNDYING), 0, 8);
                        achieved = false;
                    }
                }
                if (achieved != null) {
                    failedSwapSlot = achieved ? -1 : swapMirrorSlot;
                    swapMirrorSlot = -1;
                    swapMirrorOld = null;
                }
            } else if (pending == TotemLogic.Kind.STAGE) {
                achieved = stagedSlot >= 0 ? mc.player.getInventory().getItem(stagedSlot).is(Items.TOTEM_OF_UNDYING) : hotbar.found();
                stagedSlot = -1;
            }
            else achieved = holding;

            boolean stillWanted = pending == TotemLogic.Kind.STAGE ? stageTarget >= 0 : locked;
            boolean retry = pending == TotemLogic.Kind.SWAP && failedSwapSlot >= 0 && tracker.failures() == 0 && sourceAvoiding(failedSwapSlot, 0, 35) >= 0;
            TotemLogic.Verdict verdict = achieved == null ? TotemLogic.Verdict.NONE : tracker.verdict(achieved, stillWanted, backoff.get() && !retry);
            if (verdict == TotemLogic.Verdict.SUCCESS) warnedBackoff = false;
            else if (verdict == TotemLogic.Verdict.FAILURE) {
                if (pending == TotemLogic.Kind.SWAP && failedSwapSlot >= 0) distrustUntil = tracker.now() + DISTRUST_TICKS;
                Autism.LOG.info("[AutoTotem] t={} {} undone by the server ({} in a row), backing off {} ticks", tracker.now(), pending, tracker.failures(), tracker.lastBackoff());
                if (!warnedBackoff && backoff.get()) {
                    warnedBackoff = true;
                    if (pending == TotemLogic.Kind.SWAP) warning("Server undid the swap (%d in a row); waiting %d ticks.", tracker.failures(), tracker.lastBackoff());
                    else warning("Server undid the click (%d in a row); waiting %d ticks. Stand still to refill.", tracker.failures(), tracker.lastBackoff());
                }
            }
        }

        if (ticks < delay.get()) {
            ticks++;
            return;
        }
        if (tracker.pending() != null) {
            if (!(locked && !holding && tracker.abandonStage())) return;
            stagedSlot = -1;
        }

        if (locked && !holding) {
            int window = TotemLogic.windowFor(latencyMs());
            int avoid = tracker.now() < distrustUntil ? failedSwapSlot : -1;
            int swapSlot = avoid < 0 ? hotbar.slot() : sourceAvoiding(avoid, 0, 8);
            if (swapSlot >= 0 && swapSlot == unconfirmedSlot && tracker.now() < unconfirmedUntil) {
                int confirmed = sourceAvoiding(unconfirmedSlot, 0, 8);
                if (confirmed >= 0) {
                    Autism.LOG.info("[AutoTotem] t={} hotbar slot {} was filled moments ago and the server has not shown it yet: swapping from slot {}", tracker.now(), swapSlot + 1, confirmed + 1);
                    swapSlot = confirmed;
                }
            }
            int main = sourceAvoiding(avoid, 9, 35);
            int clickSlot = avoid < 0 ? result.slot() : main >= 0 ? main : swapSlot >= 0 ? swapSlot : result.slot();
            TotemLogic.Path path = TotemLogic.path(swapSlot >= 0 && hotbar.found(), still, containerOk, clicksWhileMoving.get(), tracker.recentPop(), mc.player.isUsingItem(), tracker.canSwap() && inventoryMenu, tracker.canClick() || still && tracker.popWaitOver());
            if (avoid >= 0 && main >= 0 && path == TotemLogic.Path.SWAP
                && TotemLogic.path(false, still, containerOk, clicksWhileMoving.get(), tracker.recentPop(), false, false, tracker.canClick()) == TotemLogic.Path.CLICK) path = TotemLogic.Path.CLICK;
            switch (path) {
                case SWAP -> {
                    swapFromHotbar(swapSlot);
                    tracker.attemptedSwap(TotemLogic.swapWindowFor(latencyMs()));
                }
                case CLICK -> {
                    Autism.LOG.info("[AutoTotem] t={} click move to offhand from inventory slot {} (still {}, pop {} ticks ago)", tracker.now(), clickSlot, still, tracker.ticksSincePop());
                    serverSwap(clickSlot, 40);
                    closeInventory();
                    tracker.attempted(TotemLogic.Kind.CLICK, window);
                }
                case WAIT -> {
                    if (!warnedWaiting && tracker.canClick() && tracker.canSwap()) {
                        warnedWaiting = true;
                        if (!inventoryMenu) warning("Totem needed, but a container is open.");
                        else if (!containerOk) warning("Totem needed, but a screen is open.");
                        else warning("Totem needed: none in hotbar while moving.");
                    }
                    return;
                }
            }
            ticks = 0;
            return;
        }

        boolean hold = !quiet && stageWhileMoving.get() && tracker.now() >= holdOffUntil && holdable();
        boolean afterContainer = tracker.now() - containerClosedAt < AFTER_CONTAINER_TICKS;
        if (stageTarget >= 0 && (still || hold) && containerOk && !afterContainer && (tracker.canClick() || tracker.popWaitOver())) {
            FindItemResult main = InvUtils.find(stack -> stack.is(Items.TOTEM_OF_UNDYING), 9, 35);
            if (main.found() && hold) {
                if (++holdsInARow > 2) {
                    holdsInARow = 0;
                    holdOffUntil = tracker.now() + 100;
                    Autism.LOG.info("[AutoTotem] t={} keys held twice for staging, still not released on the server; not holding for 100 ticks", tracker.now());
                }
                else ActionWindow.holdInput();
            } else if (main.found()) {
                holdsInARow = 0;
                Autism.LOG.info("[AutoTotem] t={} staging a totem from inventory slot {} into hotbar slot {}", tracker.now(), main.slot(), stageTarget + 1);
                serverSwap(main.slot(), stageTarget);
                closeInventory();
                stagedSlot = stageTarget;
                unconfirmedSlot = stageTarget;
                unconfirmedUntil = tracker.now() + TotemLogic.windowFor(latencyMs());
                tracker.attempted(TotemLogic.Kind.STAGE, TotemLogic.windowFor(latencyMs()));
                ticks = 0;
            }
        } else if (stage.get() && tracker.now() - stageNoteAt >= 100) {
            boolean anyEmpty = false;
            for (boolean empty : emptyHotbar) anyEmpty |= empty;
            if (anyEmpty && InvUtils.find(stack -> stack.is(Items.TOTEM_OF_UNDYING), 9, 35).found()) {
                stageNoteAt = tracker.now();
                Autism.LOG.info("[AutoTotem] not staging: {}", stageTarget < 0 ? "no slot to stage into" : !still ? "you are moving" : !containerOk ? "a screen or another container is open" : afterContainer ? "a container was just closed" : "backing off after the server undid moves");
            }
        }
    }

    private void closeInventory() {
        if (mc.gui.screen() instanceof InventoryScreen) return;
        mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
    }

    private void serverSwap(int index, int button) {
        InventoryMenu menu = mc.player.inventoryMenu;
        mc.getConnection().send(new ServerboundContainerClickPacket(menu.containerId, menu.getStateId(), (short) InvUtils.indexToId(index), (byte) button, ContainerInput.SWAP, Int2ObjectMaps.emptyMap(), HashedStack.EMPTY));
    }

    private void swapFromHotbar(int slot) {
        Autism.syncSelected();
        Inventory inv = mc.player.getInventory();
        int selected = inv.getSelectedSlot();
        var connection = mc.getConnection();
        if (slot != selected) connection.send(new ServerboundSetCarriedItemPacket(slot));
        connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
        if (slot != selected) {
            if (reselectDelay.get() == 0) connection.send(new ServerboundSetCarriedItemPacket(selected));
            else {
                reselectPending = true;
                reselectAt = tracker.now() + reselectDelay.get();
            }
        }
        swapReported = false;
        swapMirrorSlot = slot;
        ItemStack offhand = inv.getItem(Inventory.SLOT_OFFHAND);
        swapMirrorOld = offhand.copy();
        inv.setItem(Inventory.SLOT_OFFHAND, inv.getItem(slot));
        inv.setItem(slot, offhand);
        Autism.LOG.info("[AutoTotem] t={} offhand swap from hotbar slot {} (selected {}, reselect delay {})", tracker.now(), slot + 1, selected + 1, reselectDelay.get());
    }

    private void revertMirror() {
        if (swapMirrorSlot == -1 || swapMirrorOld == null) return;
        Inventory inv = mc.player.getInventory();
        if (inv.getItem(Inventory.SLOT_OFFHAND).is(Items.TOTEM_OF_UNDYING) && ItemStack.matches(inv.getItem(swapMirrorSlot), swapMirrorOld)) {
            ItemStack totem = inv.getItem(Inventory.SLOT_OFFHAND);
            inv.setItem(Inventory.SLOT_OFFHAND, inv.getItem(swapMirrorSlot));
            inv.setItem(swapMirrorSlot, totem);
            Autism.LOG.info("[AutoTotem] t={} swap never reported by the server, mirror reverted (slot {})", tracker.now(), swapMirrorSlot + 1);
        }
    }

    private int latencyMs() {
        try {
            var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null && info.getLatency() > 0) return info.getLatency();
        } catch (Exception ignored) {}
        return 100;
    }

    private boolean still() {
        LocalPlayer p = mc.player;
        if (p.isFallFlying() || p.isSwimming()) return false;
        ClientInput in = p.input;
        if (in != null && in.keyPresses != null && (in.keyPresses.forward() || in.keyPresses.backward() || in.keyPresses.left() || in.keyPresses.right() || in.keyPresses.jump())) return false;
        if (!p.onGround() && !p.isPassenger()) return false;
        Vec3 v = p.isPassenger() ? p.getVehicle().getDeltaMovement() : p.getDeltaMovement();
        return v.horizontalDistanceSqr() < 0.0004;
    }

    private boolean holdable() {
        LocalPlayer p = mc.player;
        return !p.isFallFlying() && !p.isSwimming() && !p.isInWater() && !p.isPassenger();
    }

    private boolean containerOk() {
        if (mc.player.containerMenu != mc.player.inventoryMenu) return false;
        return mc.gui.screen() == null || mc.gui.screen() instanceof InventoryScreen;
    }

    private float predictedDamage() {
        float damage = PlayerUtils.possibleHealthReductions(explosion.get(), fall.get());
        if (tnt.get() || mace.get()) {
            Vec3 pos = mc.player.position();
            for (Entity e : mc.level.entitiesForRendering()) {
                if (tnt.get() && e instanceof PrimedTnt && e.distanceToSqr(mc.player) < 100) {
                    damage = Math.max(damage, DamageUtils.explosionDamage(mc.player, pos, mc.player.getBoundingBox(), e.position(), 8f, DamageUtils.HIT_FACTORY));
                } else if (mace.get() && e instanceof Player other && other != mc.player && !Autism.friend(other)
                    && other.getMainHandItem().is(Items.MACE) && !other.onGround() && other.getY() > mc.player.getY()
                    && other.distanceToSqr(mc.player) < 64) {
                    double fell = Math.max(other.fallDistance, other.getY() - mc.player.getY());
                    float bonus = DamageUtils.calculateReductions(TotemLogic.maceBonus(fell), mc.player, mc.level.damageSources().playerAttack(other));
                    damage = Math.max(damage, DamageUtils.getAttackDamage(other, mc.player) + bonus);
                }
            }
        }
        if (anchors.get() && !mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS)) {
            BlockPos origin = mc.player.blockPosition();
            for (BlockPos bp : BlockPos.betweenClosed(origin.offset(-6, -6, -6), origin.offset(6, 6, 6))) {
                BlockState state = mc.level.getBlockState(bp);
                if (state.is(Blocks.RESPAWN_ANCHOR) && state.getValue(BlockStateProperties.RESPAWN_ANCHOR_CHARGES) > 0) {
                    damage = Math.max(damage, DamageUtils.anchorDamage(mc.player, Vec3.atCenterOf(bp)));
                }
            }
        }
        if (burning.get()) {
            if (mc.player.isInLava()) damage += 4;
            else if (mc.player.isOnFire()) damage += 1;
        }
        return damage;
    }

    /** Network thread. */
    public void onPacketReceive(Packet<?> packet) {
        if (packet instanceof ClientboundContainerSetSlotPacket p) {
            if (p.getContainerId() == 0 && p.getSlot() == InventoryMenu.SHIELD_SLOT && p.getItem().is(Items.TOTEM_OF_UNDYING)) later(() -> swapReported = true);
            int staged = unconfirmedSlot;
            if (p.getContainerId() == 0 && staged >= 0 && p.getSlot() == InventoryMenu.USE_ROW_SLOT_START + staged && p.getItem().is(Items.TOTEM_OF_UNDYING)) unconfirmedSlot = -1;
            return;
        }
        if (packet instanceof ClientboundContainerSetContentPacket p) {
            if (p.containerId() == 0 && p.items().size() > InventoryMenu.SHIELD_SLOT && p.items().get(InventoryMenu.SHIELD_SLOT).is(Items.TOTEM_OF_UNDYING)) later(() -> swapReported = true);
            int staged = unconfirmedSlot;
            if (p.containerId() == 0 && staged >= 0 && p.items().size() > InventoryMenu.USE_ROW_SLOT_START + staged
                && p.items().get(InventoryMenu.USE_ROW_SLOT_START + staged).is(Items.TOTEM_OF_UNDYING)) unconfirmedSlot = -1;
            return;
        }
        if (!(packet instanceof ClientboundEntityEventPacket p)) return;
        if (p.getEventId() != EntityEvent.PROTECTED_FROM_DEATH) return;
        later(() -> {
            if (mc.level == null || mc.player == null || p.getEntity(mc.level) != mc.player) return;
            Autism.LOG.info("[AutoTotem] t={} totem popped", tracker.now());
            if (mc.player != null && !mc.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) && mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                mc.player.getOffhandItem().shrink(1);
            }
            tracker.popped(instant.get() ? 0 : TotemLogic.popDelay(random, (int) popDelay.get().min(), (int) popDelay.get().max()));
            ticks = delay.get();
        });
    }

    public enum Mode {
        Smart,
        Strict
    }
}
