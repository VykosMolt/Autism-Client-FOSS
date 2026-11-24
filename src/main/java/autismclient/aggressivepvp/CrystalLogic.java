/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package autismclient.aggressivepvp;

/** How CrystalAura rates one crystal against one target, kept free of Minecraft so a headless check can pin it down. */
public final class CrystalLogic {
    private CrystalLogic() {}

    /** The score of a crystal that breaks a rule. Every real score is greater. */
    public static final double NONE = Double.NEGATIVE_INFINITY;

    /** Health we keep after our own crystal. */
    public static final double SUICIDE_MARGIN = 0.5;
    /** The least damage worth a crystal on a face-place target. */
    public static final double FACE_PLACE_MIN = 1.5;
    /** The least damage worth a crystal that kills (or pops). */
    public static final double LETHAL_MIN = 0.5;
    /** What a point of our own damage costs in points of theirs. */
    public static final double SELF_WEIGHT = 0.3;
    /** Puts every killing crystal above every other one. */
    public static final double LETHAL_BONUS = 100;

    /**
     * The module's settings. A lethal multiplier of 0 turns the kill rules off. damageRatio is the least a crystal must
     * deal to the target per point it deals to us (BlackOut's damage ratio; 0 turns it off): a trade that costs more
     * than it gives is only worth it when it kills. popForPop (trade-totem): a crystal that would pop us goes only when it
     * pops or kills the target as well.
     */
    public record Rules(double minDamage, double maxSelfDamage, boolean antiSuicide, double lethalMultiplier, double damageRatio, boolean popForPop) {
        public Rules(double minDamage, double maxSelfDamage, boolean antiSuicide, double lethalMultiplier, double damageRatio) {
            this(minDamage, maxSelfDamage, antiSuicide, lethalMultiplier, damageRatio, false);
        }

        public Rules(double minDamage, double maxSelfDamage, boolean antiSuicide, double lethalMultiplier) {
            this(minDamage, maxSelfDamage, antiSuicide, lethalMultiplier, 0);
        }
    }

    /**
     * Ticks to wait for the server's answer to a placement or an attack before trying again: the round trip, a server tick
     * and a tick of slack, never less than the 4 that suit a short round trip.
     */
    public static int answerTicks(int latencyMs) {
        return Math.max(4, (Math.max(0, latencyMs) + 49) / 50 + 2);
    }

    public static boolean lethal(Rules r, double damage, double targetHealth) {
        return r.lethalMultiplier > 0 && damage * r.lethalMultiplier >= targetHealth;
    }

    /**
     * Never kill yourself; stay under the self-damage cap unless the crystal kills (a target holding a totem only pops, so
     * the cap stays); do at least the minimum damage, less when face placing, anything when it kills or pops. Among the
     * crystals left, kills first, then damage dealt less a share of the damage taken.
     */
    public static double score(Rules r, double damage, double selfDamage, double targetHealth, double selfHealth, boolean facePlace, boolean targetTotem) {
        if (r.antiSuicide && selfDamage >= selfHealth - SUICIDE_MARGIN) return NONE;

        boolean lethal = lethal(r, damage, targetHealth);
        if (r.popForPop && !lethal && selfDamage >= selfHealth - SUICIDE_MARGIN) return NONE;
        if (selfDamage > r.maxSelfDamage && !(lethal && !targetTotem)) return NONE;

        double min = r.minDamage;
        if (lethal) min = Math.min(min, LETHAL_MIN);
        else if (facePlace) min = Math.min(min, FACE_PLACE_MIN);
        if (damage < min) return NONE;
        if (!lethal && damage < selfDamage * r.damageRatio) return NONE;

        return damage - selfDamage * SELF_WEIGHT + (lethal ? LETHAL_BONUS : 0);
    }

    /**
     * The rules with a totem to trade (Crystal Aura's trade-totem, a spare ready): damage to us that leaves us standing
     * does not count, no max and no ratio; a crystal that would pop us goes only when it pops or kills the target too,
     * totem for totem.
     */
    public static Rules traded(Rules r) {
        return new Rules(r.minDamage, Double.MAX_VALUE, false, r.lethalMultiplier, 0, true);
    }

    /** The health a crystal is judged against for "would pop us": short of our health by a share of it (pop-margin). */
    public static double popBar(double health, double margin) {
        return health * (1 - Math.max(0, Math.min(margin, 0.9)));
    }

    /** Health and absorption a pop leaves: the totem sets health to 1 and gives absorption II, 8 more (26.3 DeathProtection.TOTEM_OF_UNDYING). */
    public static final double POST_POP_HEALTH = 9;

    /**
     * The health a crystal is judged against. While a pop of ours is on its way, that pop comes first, so the crystal is
     * judged against what it leaves. "On its way" means another crystal that would pop us, or a pop the server has not
     * yet answered with the next totem in the offhand. There is then no totem to trade either: one totem at a time.
     */
    public static double judgedHealth(double health, boolean popOnItsWay) {
        return popOnItsWay ? Math.min(health, POST_POP_HEALTH) : health;
    }

    /** Absorption a totem pop gives (absorption II), and how long it lasts: the effect's 100 ticks. */
    public static final double POP_ABSORPTION = 8;
    public static final int POP_ABSORPTION_TICKS = 100;

    /**
     * A target's health when judging whether a crystal kills it. Donut sends no absorption for other players: for the
     * 100 ticks after a pop the target has 8 more than it shows, unless absorption is shown, or its health has been
     * seen to drop since (damage takes absorption first). ticksSincePop is negative when there is no such pop.
     */
    public static double targetHealth(double shown, double shownAbsorption, int ticksSincePop) {
        if (shownAbsorption > 0 || ticksSincePop < 0 || ticksSincePop > POP_ABSORPTION_TICKS) return shown;
        return shown + POP_ABSORPTION;
    }

    /** Why {@link #score} refuses this crystal, in words for the fight log; null when it does not. Same tests, same order. */
    public static String why(Rules r, double damage, double selfDamage, double targetHealth, double selfHealth, boolean facePlace, boolean targetTotem) {
        if (r.antiSuicide && selfDamage >= selfHealth - SUICIDE_MARGIN) return "would kill you (anti-suicide)";
        boolean lethal = lethal(r, damage, targetHealth);
        if (r.popForPop && !lethal && selfDamage >= selfHealth - SUICIDE_MARGIN) return "would pop you without popping the target (trade-totem)";
        if (selfDamage > r.maxSelfDamage && !(lethal && !targetTotem)) return "over max-damage to you";
        double min = r.minDamage;
        if (lethal) min = Math.min(min, LETHAL_MIN);
        else if (facePlace) min = Math.min(min, FACE_PLACE_MIN);
        if (damage < min) return "under min-damage to the target";
        if (!lethal && damage < selfDamage * r.damageRatio) return "hurts you more than the target (damage-ratio)";
        return null;
    }

    /** Server ticks after a counted hit before the next one counts in full. */
    public static final int WINDOW_TICKS = 10;
    /** Ticks of slack on our estimate of when the window opens: a hit a tick late costs 50 ms, one a tick early is wasted. */
    public static final double WINDOW_SLACK = 1;
    /** The least share of a predicted hit a health drop must reach to be taken for that hit. */
    public static final double SAME_HIT = 0.5;
    /** Ticks a hit we only predicted may go without showing in the target's health before the window is called off. */
    public static final int PROOF_TICKS = 3;

    /** The window in milliseconds, at 20 server ticks a second. */
    public static final double WINDOW_MS = WINDOW_TICKS * 50;
    /** A sign of a hit this long before the window's end already belongs to the next hit: ours, sent to land on the tick it ends. */
    public static final double EARLY_SIGN_MS = 60;
    public static final double AIM_FLOOR = -20, AIM_CAP = 100, AIM_BACK = 15, AIM_IN = 0.25;
    public static final double TIMED_MS = 150;

    /** The aim of timed hits, learned from whether they count. */
    public static final class Aim {
        private double ms = AIM_FLOOR;

        public double ms() {
            return ms;
        }

        public void counted() {
            ms = Math.max(AIM_FLOOR, ms - AIM_IN);
        }

        public void missed() {
            ms = Math.min(AIM_CAP, ms + AIM_BACK);
        }
    }

    public static final double DTAP_SEEN_BY = WINDOW_MS - EARLY_SIGN_MS - 50 - 10;

    public static double dtapLeadMs(double roundTripMs, double slowestMs, double aimMs) {
        return Math.max(roundTripMs + 2 * 50, WINDOW_MS + aimMs - roundTripMs + 50 + slowestMs - DTAP_SEEN_BY);
    }

    public static boolean dtapHold(Rules r, double excess, double damage, double targetHealth, int handTotems, double dueInMs, double leadMs, double selfCost, double selfDamage) {
        return handTotems == 1 && dueInMs > leadMs && selfCost >= selfDamage && lethal(r, excess, targetHealth) && lethal(r, damage, POST_POP_HEALTH);
    }

    public static double rawExcess(java.util.function.DoubleUnaryOperator reduce, double damage, double last) {
        return Math.max(0, reduce.applyAsDouble(raw(reduce, damage) - raw(reduce, last)));
    }

    private static double raw(java.util.function.DoubleUnaryOperator reduce, double after) {
        double lo = 0, hi = 1;
        while (reduce.applyAsDouble(hi) < after && hi < 1e6) hi *= 2;
        for (int i = 0; i < 60; i++) {
            double mid = (lo + hi) / 2;
            if (reduce.applyAsDouble(mid) < after) lo = mid;
            else hi = mid;
        }
        return hi;
    }

    /** What a hit adds to a target whose last counted hit, of lastDamage, will be this many server ticks old when it lands. */
    public static double counted(double damage, double lastDamage, double ticksSinceLast) {
        if (ticksSinceLast >= WINDOW_TICKS + WINDOW_SLACK) return damage;
        return Math.max(0, damage - lastDamage);
    }

    public static final class Window {
        private static final double NEVER = -1e12;

        private final java.util.function.DoubleSupplier clock;
        private int start = -1000;
        private double startMs = NEVER;
        private double pendingOpensMs = NEVER, pendingUntilMs = NEVER, pendingLast;
        private boolean pendingTimed, timed;
        private double last;
        private boolean unproven, measured, observable;
        private boolean sizedByUs;

        public Window() {
            this(() -> System.nanoTime() / 1e6);
        }

        public Window(java.util.function.DoubleSupplier clockMs) {
            clock = clockMs;
        }

        public int startedAt() {
            return start;
        }

        private boolean begin(int tick) {
            double now = clock.getAsDouble();
            if (tick >= start && tick - start < WINDOW_TICKS && now - startMs < WINDOW_MS - EARLY_SIGN_MS) return false;
            start = tick;
            startMs = now;
            timed = false;
            last = Double.MAX_VALUE;
            unproven = measured = sizedByUs = false;
            return true;
        }

        public boolean flinched(int tick) {
            observable = true;
            if (begin(tick) || tick - start > PROOF_TICKS || !unproven) return false;
            unproven = false;
            return timed;
        }

        public boolean damaged(int tick, double amount) {
            observable = true;
            begin(tick);
            boolean proved = unproven && timed;
            unproven = false;
            if (!measured && (last == Double.MAX_VALUE || amount >= last * SAME_HIT)) last = amount;
            else {
                last += amount;
                sizedByUs = false;
            }
            measured = true;
            return proved;
        }

        public void sent(double predicted, double roundTripMs, double answerMs) {
            if (predicted < 1) return;
            double now = clock.getAsDouble();
            pendingTimed = Math.abs(now + roundTripMs - (startMs + WINDOW_MS)) <= TIMED_MS;
            pendingOpensMs = now + roundTripMs;
            pendingUntilMs = now + answerMs;
            pendingLast = predicted;
        }

        public void exploded(int tick, double predicted) {
            boolean wasTimed = pendingTimed && clock.getAsDouble() < pendingUntilMs;
            pendingUntilMs = NEVER;
            pendingTimed = false;
            if (predicted < 1) return;
            if (begin(tick)) {
                last = predicted;
                unproven = sizedByUs = true;
                timed = wasTimed;
            }
            else if (!measured && last == Double.MAX_VALUE && tick - start <= PROOF_TICKS) {
                last = predicted;
                sizedByUs = true;
            }
        }

        public void popped(int tick) {
            begin(tick);
            if (!(sizedByUs && tick - start <= PROOF_TICKS)) last = Double.MAX_VALUE;
            measured = true;
            unproven = false;
        }

        public boolean expire(int tick) {
            if (!(unproven && observable && tick - start > PROOF_TICKS)) return false;
            start = -1000;
            startMs = NEVER;
            unproven = false;
            return timed;
        }

        public double dueIn(double leadMs, double aimMs) {
            double now = clock.getAsDouble();
            double from = startMs;
            if (now < pendingUntilMs && pendingOpensMs - startMs >= WINDOW_MS - EARLY_SIGN_MS) from = pendingOpensMs;
            return from + WINDOW_MS + aimMs - leadMs - now;
        }

        public double countedAt(double damage, double leadMs, double aimMs) {
            if (dueIn(leadMs, aimMs) <= 0) return damage;
            double size = last;
            if (clock.getAsDouble() < pendingUntilMs) size = pendingOpensMs - startMs >= WINDOW_MS - EARLY_SIGN_MS ? pendingLast : Math.max(size, pendingLast);
            return Math.max(0, damage - size);
        }

        public double shielded(double damage, int tick, double leadTicks) {
            if (tick < start || tick - start + leadTicks > WINDOW_TICKS - SHIELD_SLACK) return damage;
            if (last == Double.MAX_VALUE) return damage;
            return Math.max(0, damage - last);
        }

        public boolean open(int tick) {
            return tick - start >= WINDOW_TICKS + WINDOW_SLACK;
        }

        public double counted(double damage, int tick, double leadTicks) {
            if (tick < start) return damage;
            return CrystalLogic.counted(damage, last, tick - start + leadTicks);
        }
    }

    public static final class RoundTrip {
        private final double[] samples = new double[8];
        private int count;

        public void add(double ms) {
            if (ms < 0 || ms > 2000) return;
            samples[count++ % samples.length] = ms;
        }

        public double ms() {
            if (count == 0) return 100;
            double min = Double.MAX_VALUE;
            for (int i = 0; i < Math.min(count, samples.length); i++) min = Math.min(min, samples[i]);
            return min;
        }

        public double ticks() {
            return ms() / 50;
        }

        public double slowestMs() {
            if (count == 0) return 100;
            double max = 0;
            for (int i = 0; i < Math.min(count, samples.length); i++) max = Math.max(max, samples[i]);
            return max;
        }

        public double slowestTicks() {
            return slowestMs() / 50;
        }
    }

    public static final double MAX_LEAD = 3, MAX_LEAD_UP = 1.5;
    public static final double MAX_SPEED = 1.5;
    public static final double GRAVITY = 0.08, DRAG = 0.98;

    public static final double STICKY = 0.5;
    public static final double BLOCKED = 8;

    public static double[] lead(double dx, double dy, double dz, boolean onGround, double leadTicks) {
        if (dx * dx + dy * dy + dz * dz > MAX_SPEED * MAX_SPEED) return new double[3];

        double x = dx * leadTicks, z = dz * leadTicks;

        double y = 0;
        if (!onGround) {
            double speed = dy;
            for (double left = leadTicks; left > 0; left--) {
                speed = (speed - GRAVITY) * DRAG;
                y += speed * Math.min(1, left);
            }
            y = Math.clamp(y, -MAX_LEAD_UP, MAX_LEAD_UP);
        }

        double flat = Math.sqrt(x * x + z * z);
        if (flat > MAX_LEAD) {
            x *= MAX_LEAD / flat;
            z *= MAX_LEAD / flat;
        }
        return new double[]{x, y, z};
    }

    public static final double SHIELD_SLACK = 2;
    public static final double DENY_MIN = 4;
    public static final double DENY_COST = 0.5;
    public static final double DENY_SCORE = -1000;

    public static boolean deny(double selfDamage, double shieldedSelfDamage, double selfHealth) {
        return selfDamage >= DENY_MIN && shieldedSelfDamage <= DENY_COST && selfDamage < selfHealth - SUICIDE_MARGIN;
    }

    public static final double BASE_WEAK = 8;
    public static final double BASE_GAIN = 5, BASE_RATIO = 2;

    public static boolean worthBase(double existing, double built) {
        return existing < BASE_WEAK && built >= existing + BASE_GAIN && built >= existing * BASE_RATIO;
    }
}
