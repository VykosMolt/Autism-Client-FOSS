import autismclient.aggressivepvp.CrystalLogic;
import autismclient.aggressivepvp.CrystalLogic.Rules;

public class AggressiveCrystalLogicCheck {
    static int failures;

    static void check(String what, boolean ok) {
        if (!ok) {
            System.out.println("FAIL: " + what);
            failures++;
        }
    }

    static final double NONE = CrystalLogic.NONE;
    static final Rules R = new Rules(6, 6, true, 1);

    static double score(double damage, double self) {
        return CrystalLogic.score(R, damage, self, 36, 20, false, false);
    }

    public static void main(String[] args) {
        check("10 damage for 2 scores", score(10, 2) > NONE);
        check("score = damage - 0.3 * self", Math.abs(score(10, 2) - 9.4) < 1e-9);
        check("under min damage -> NONE", score(5.9, 0) == NONE);
        check("at min damage scores", score(6, 0) > NONE);
        check("over the self cap -> NONE", score(30, 6.1) == NONE);
        check("at the self cap scores", score(30, 6) > NONE);
        check("more damage wins", score(12, 2) > score(10, 2));
        check("less self damage wins", score(10, 1) > score(10, 4));

        check("face place: 2 damage scores", CrystalLogic.score(R, 2, 0, 36, 20, true, false) > NONE);
        check("face place: 1.4 damage -> NONE", CrystalLogic.score(R, 1.4, 0, 36, 20, true, false) == NONE);
        check("face place keeps the self cap", CrystalLogic.score(R, 2, 7, 36, 20, true, false) == NONE);
        check("face place with min damage 1 keeps 1", CrystalLogic.score(new Rules(1, 6, true, 1), 1.2, 0, 36, 20, true, false) > NONE);

        check("kill under min damage scores", CrystalLogic.score(R, 3, 0, 3, 20, false, false) > NONE);
        check("kill over the self cap scores", CrystalLogic.score(R, 9, 12, 9, 20, false, false) > NONE);
        check("kill that kills us -> NONE", CrystalLogic.score(R, 9, 19.6, 9, 20, false, false) == NONE);
        check("kill leaving exactly the margin -> NONE", CrystalLogic.score(R, 9, 19.5, 9, 20, false, false) == NONE);
        check("kill leaving more than the margin scores", CrystalLogic.score(R, 9, 19.4, 9, 20, false, false) > NONE);
        check("kill beats any other crystal", CrystalLogic.score(R, 8, 2, 8, 20, false, false) > score(35, 0));
        check("just short of a kill is not one", CrystalLogic.score(R, 5, 0, 5.1, 20, false, false) == NONE);
        check("kill below 0.5 damage -> NONE", CrystalLogic.score(R, 0.4, 0, 0.3, 20, false, false) == NONE);

        check("pop under min damage scores", CrystalLogic.score(R, 3, 0, 3, 20, false, true) > NONE);
        check("pop over the self cap -> NONE", CrystalLogic.score(R, 9, 12, 9, 20, false, true) == NONE);

        Rules slack = new Rules(6, 6, true, 1.2);
        check("multiplier 1.2: 5 on 6 is a kill", CrystalLogic.lethal(slack, 5, 6) && CrystalLogic.score(slack, 5, 0, 6, 20, false, false) > NONE);
        check("multiplier 1.2: 4.9 on 6 is not", !CrystalLogic.lethal(slack, 4.9, 6));
        Rules off = new Rules(6, 6, true, 0);
        check("multiplier 0: no kills", !CrystalLogic.lethal(off, 100, 1) && CrystalLogic.score(off, 3, 0, 3, 20, false, false) == NONE && CrystalLogic.score(off, 9, 12, 9, 20, false, false) == NONE);

        Rules reckless = new Rules(6, 36, false, 1);
        check("anti-suicide off scores", CrystalLogic.score(reckless, 10, 25, 36, 20, false, false) > NONE);
        check("anti-suicide on refuses", CrystalLogic.score(new Rules(6, 36, true, 1), 10, 25, 36, 20, false, false) == NONE);

        check("3.5 damage -> NONE", score(3.5, 0) == NONE);
        check("answer ticks <= 100", CrystalLogic.answerTicks(0) == 4 && CrystalLogic.answerTicks(-5) == 4 && CrystalLogic.answerTicks(100) == 4);
        check("answer ticks scaled", CrystalLogic.answerTicks(101) == 5 && CrystalLogic.answerTicks(150) == 5 && CrystalLogic.answerTicks(151) == 6 && CrystalLogic.answerTicks(400) == 10);

        check("window 11 ticks", CrystalLogic.counted(12, 10, 11) == 12);
        check("window 10 ticks", CrystalLogic.counted(12, 10, 10) == 2);
        check("window equal/smaller", CrystalLogic.counted(10, 10, 4) == 0 && CrystalLogic.counted(6, 10, 4) == 0);
        check("window excess", CrystalLogic.counted(15, 10, 4) == 5);

        CrystalLogic.Window w = new CrystalLogic.Window();
        check("window fresh", w.counted(9, 100, 0) == 9 && w.open(100));
        w.damaged(100, 8);
        check("window right after 8", w.counted(8, 101, 0) == 0 && !w.open(101));
        check("window 9+2", w.counted(8, 109, 2) == 8);
        check("window 8+2", w.counted(8, 108, 2) == 0);
        check("window 7+4", w.counted(8, 107, 4) == 8);
        w.damaged(104, 3);
        check("window excess raises last", w.counted(11, 106, 0) == 0 && w.counted(12, 106, 0) == 1 && w.counted(8, 109, 2) == 8);
        w.damaged(115, 5);
        check("window new after expiry", w.counted(5, 116, 0) == 0 && w.counted(5, 124, 2) == 5);

        CrystalLogic.Window hidden = new CrystalLogic.Window();
        hidden.exploded(200, 9);
        hidden.expire(210);
        check("window hidden", hidden.counted(9, 205, 0) == 0 && hidden.counted(9, 209, 2) == 9);
        hidden.exploded(204, 20);
        check("window hidden no restart", hidden.counted(9, 209, 2) == 9);
        hidden.exploded(230, 0.5);
        check("window sub-1 no window", hidden.counted(9, 231, 0) == 9);

        CrystalLogic.Window seen = new CrystalLogic.Window();
        seen.damaged(300, 6);
        seen.exploded(320, 9);
        seen.expire(322);
        check("window holds 3 ticks", seen.counted(9, 322, 0) == 0);
        seen.expire(324);
        check("window expires unproven", seen.counted(9, 324, 0) == 9);
        seen.exploded(340, 9);
        seen.damaged(341, 7);
        seen.expire(350);
        check("window drop replaces prediction", seen.counted(7, 345, 0) == 0 && seen.counted(8, 345, 0) == 1);

        CrystalLogic.Window pop = new CrystalLogic.Window();
        pop.popped(400);
        check("window after pop", pop.counted(1000, 405, 0) == 0 && pop.counted(6, 409, 2) == 6);

        CrystalLogic.Window flinch = new CrystalLogic.Window();
        flinch.flinched(500);
        check("window flinch", flinch.counted(1000, 504, 0) == 0 && flinch.counted(6, 509, 2) == 6);
        flinch.flinched(520);
        flinch.damaged(521, 7);
        check("window flinch + drop", flinch.counted(7, 523, 0) == 0 && flinch.counted(9, 523, 0) == 2);
        flinch.exploded(540, 9);
        flinch.flinched(541);
        flinch.expire(550);
        check("window flinch proves explosion", flinch.counted(9, 545, 0) == 0 && flinch.counted(10, 545, 0) == 1);
        flinch.flinched(560);
        flinch.exploded(561, 9);
        check("window explosion sizes flinch", flinch.counted(9, 563, 0) == 0 && flinch.counted(10, 563, 0) == 1);
        flinch.exploded(580, 9);
        flinch.expire(584);
        check("window unproven expires", flinch.counted(9, 584, 0) == 9);
        flinch.damaged(600, 5);
        flinch.popped(604);
        flinch.damaged(605, 3);
        check("window pop inside running locks", flinch.counted(1000, 606, 0) == 0 && flinch.counted(6, 609, 2) == 6);

        CrystalLogic.Window late = new CrystalLogic.Window();
        late.damaged(700, 2);
        late.exploded(720, 9);
        late.damaged(724, 9);
        late.expire(724);
        check("window late drop", late.counted(9, 725, 0) == 0 && !late.open(725));
        check("window open agrees", !late.open(730) && late.open(731) && late.counted(9, 730, 0) == 0 && late.counted(9, 731, 0) == 9);

        CrystalLogic.Window small = new CrystalLogic.Window();
        small.exploded(800, 8);
        small.damaged(802, 0.5);
        check("window small unrelated drop", small.counted(8, 803, 0) == 0 && small.counted(9, 803, 0) == 0.5);

        CrystalLogic.Window follow = new CrystalLogic.Window();
        follow.exploded(1000, 6);
        follow.popped(1000);
        follow.damaged(1001, 3);
        check("pop by our own crystal of 6", follow.counted(16, 1001, 0) == 7 && follow.counted(9, 1001, 0) == 0);
        CrystalLogic.Window follow2 = new CrystalLogic.Window();
        follow2.exploded(1000, 6);
        follow2.popped(1001);
        check("pop by our own crystal of 6 alone", follow2.counted(16, 1002, 0) == 10);
        CrystalLogic.Window other = new CrystalLogic.Window();
        other.popped(1100);
        other.exploded(1100, 6);
        check("pop seen before our crystal locked", other.counted(1000, 1101, 0) == 0);

        CrystalLogic.Window own = new CrystalLogic.Window();
        check("shield fresh", own.shielded(7, 900, 2) == 7);
        own.flinched(900);
        own.damaged(900, 6);
        check("shield <= 6", own.shielded(6, 902, 2) == 0 && own.shielded(4, 902, 2) == 0);
        check("shield excess", own.shielded(9, 902, 2) == 3);
        check("shield tick 8", own.shielded(6, 904, 4) == 0);
        check("shield tick 9", own.shielded(6, 905, 4) == 6);
        own.popped(920);
        check("shield after pop", own.shielded(40, 923, 2) == 40 && own.shielded(4, 923, 2) == 4);
        CrystalLogic.Window blind = new CrystalLogic.Window();
        blind.flinched(950);
        check("shield blind flinch", blind.shielded(9, 952, 1) == 9 && blind.counted(9, 952, 1) == 0);
        CrystalLogic.Window stale = new CrystalLogic.Window();
        stale.flinched(12000);
        stale.damaged(12000, 5);
        check("stale clock reset", stale.shielded(9, 0, 1) == 9 && stale.counted(9, 0, 1) == 9);
        stale.flinched(3);
        stale.damaged(3, 6);
        check("fresh hit after reset", stale.shielded(6, 5, 1) == 0 && stale.startedAt() == 3);
        CrystalLogic.Window stolen = new CrystalLogic.Window();
        stolen.exploded(1200, 6);
        stolen.damaged(1200, 6);
        stolen.damaged(1202, 9);
        stolen.popped(1202);
        check("stolen pop locked", stolen.counted(40, 1203, 0) == 0);
        check("deny yes", CrystalLogic.deny(7, 0, 20));
        check("deny costs us", !CrystalLogic.deny(7, 3, 20));
        check("deny too small", !CrystalLogic.deny(2, 0, 20));
        check("deny lethal", !CrystalLogic.deny(19.6, 0, 20));
        check("deny score ordering", CrystalLogic.DENY_SCORE > CrystalLogic.NONE && CrystalLogic.DENY_SCORE < CrystalLogic.score(new Rules(0, 100, false, 0), 0.6, 100, 20, 20, false, false));

        CrystalLogic.RoundTrip rt = new CrystalLogic.RoundTrip();
        check("rt default", rt.ms() == 100 && rt.ticks() == 2);
        rt.add(140); rt.add(90); rt.add(120);
        check("rt min", rt.ms() == 90);
        rt.add(-5); rt.add(5000);
        check("rt drops nonsense", rt.ms() == 90);
        check("rt slowest", rt.slowestMs() == 140 && rt.slowestTicks() == 2.8 && new CrystalLogic.RoundTrip().slowestMs() == 100);
        for (int i = 0; i < 8; i++) rt.add(150);
        check("rt ages out", rt.ms() == 150);
        check("answerTicks rt", CrystalLogic.answerTicks((int) rt.ms()) == 5);

        double[] lead = CrystalLogic.lead(0.2, -0.08, 0, true, 4);
        check("lead ground", Math.abs(lead[0] - 0.8) < 1e-9 && lead[1] == 0 && lead[2] == 0);
        lead = CrystalLogic.lead(0, -0.5, 0.1, false, 4);
        check("lead fall capped", lead[1] == -1.5 && Math.abs(lead[2] - 0.4) < 1e-9);
        lead = CrystalLogic.lead(0.6, 0, 0.8, true, 4);
        check("lead flat capped", Math.abs(Math.hypot(lead[0], lead[2]) - 3) < 1e-9 && Math.abs(lead[0] / lead[2] - 0.75) < 1e-9);
        lead = CrystalLogic.lead(0.2, 0, 0, true, 0);
        check("lead zero", lead[0] == 0 && lead[1] == 0 && lead[2] == 0);
        lead = CrystalLogic.lead(30, 0, 4, true, 4);
        check("lead teleport", lead[0] == 0 && lead[1] == 0 && lead[2] == 0);
        lead = CrystalLogic.lead(0, 0.42, 0, false, 4);
        check("lead jump", Math.abs(lead[1] - 0.8292) < 1e-3);

        Rules trade = new Rules(4, 10, true, 1, 1);
        check("ratio 1 refuse", CrystalLogic.score(trade, 6, 9, 20, 20, false, false) == NONE);
        check("ratio 1 accept", CrystalLogic.score(trade, 9, 6, 20, 20, false, false) > NONE);
        check("ratio 1 kill", CrystalLogic.score(trade, 6, 9, 5, 20, false, false) > NONE);
        check("ratio 2", CrystalLogic.score(new Rules(4, 10, true, 1, 2), 9, 6, 20, 20, false, false) == NONE
            && CrystalLogic.score(new Rules(4, 10, true, 1, 2), 12, 6, 20, 20, false, false) > NONE);
        check("ratio off", CrystalLogic.score(new Rules(4, 10, true, 1), 6, 9, 20, 20, false, false) > NONE);

        check("base 0/5", CrystalLogic.worthBase(0, 5));
        check("base 0/4", !CrystalLogic.worthBase(0, 4));
        check("base 8/30", !CrystalLogic.worthBase(8, 30));
        check("base 4/9", CrystalLogic.worthBase(4, 9));
        check("base 6/11", !CrystalLogic.worthBase(6, 11));
        check("base 3/7", !CrystalLogic.worthBase(3, 7));

        double[] clock = {1_000_000};
        CrystalLogic.Window ms = new CrystalLogic.Window(() -> clock[0]);
        check("ms fresh", ms.dueIn(60, -20) <= 0 && ms.countedAt(12, 60, -20) == 12);
        ms.exploded(100, 12);
        check("ms after hit", ms.countedAt(12, 60, -20) == 0 && ms.countedAt(20, 60, -20) == 8);
        check("ms dueIn 420", Math.abs(ms.dueIn(60, -20) - 420) < 1e-9);
        check("ms dueIn place 210", Math.abs(ms.dueIn(60 + 60 + 150, -20) - 210) < 1e-9);
        clock[0] += 419;
        check("ms 1ms early", ms.dueIn(60, -20) > 0 && ms.countedAt(12, 60, -20) == 0);
        clock[0] += 1;
        check("ms due", ms.dueIn(60, -20) <= 0 && ms.countedAt(12, 60, -20) == 12);
        check("ms aim shift", Math.abs(ms.dueIn(60, -5) - 15) < 1e-9);

        ms.sent(14, 60, 200);
        check("ms in flight", ms.dueIn(60, -20) > 0 && ms.countedAt(14, 60, -20) == 0);
        check("ms in flight due 480", Math.abs(ms.dueIn(60, -20) - 480) < 1e-9);
        check("ms in flight excess", ms.countedAt(20, 60, -20) == 6);
        clock[0] += 201;
        check("ms unanswered forgotten", ms.dueIn(60, -20) <= 0);
        ms.sent(14, 60, 200);
        clock[0] += 75;
        ms.exploded(110, 14);
        check("ms exploded anchors", Math.abs(ms.dueIn(60, -20) - 420) < 1e-9);

        double[] clock2 = {5_000};
        CrystalLogic.Window timed = new CrystalLogic.Window(() -> clock2[0]);
        timed.sent(12, 60, 200);
        clock2[0] += 70;
        timed.exploded(100, 12);
        check("ms first hit", !timed.flinched(101));
        clock2[0] += 420;
        timed.sent(13, 60, 200);
        clock2[0] += 55;
        timed.exploded(109, 13);
        check("ms 9th tick new window", timed.startedAt() == 109 && Math.abs(timed.dueIn(60, -20) - 420) < 1e-9);
        check("ms drop proves once", timed.damaged(110, 13) && !timed.damaged(111, 1));
        clock2[0] += 200;
        timed.exploded(113, 13);
        check("ms 200ms in no restart", timed.startedAt() == 109);
        clock2[0] += 220;
        timed.sent(13, 60, 200);
        clock2[0] += 60;
        timed.exploded(119, 13);
        check("ms timed expire", !timed.expire(122) && timed.expire(123) && timed.dueIn(60, -20) <= 0);
        timed.sent(13, 60, 200);
        clock2[0] += 60;
        timed.exploded(130, 13);
        check("ms untimed expire", !timed.expire(134) && timed.dueIn(60, -20) <= 0);

        CrystalLogic.Aim aim = new CrystalLogic.Aim();
        check("aim start", aim.ms() == -20);
        aim.counted();
        check("aim floor", aim.ms() == -20);
        aim.missed();
        check("aim miss +15", aim.ms() == -5);
        aim.counted();
        check("aim hit -0.25", aim.ms() == -5.25);
        for (int i = 0; i < 20; i++) aim.missed();
        check("aim cap", aim.ms() == 100);

        check("dtap lead 100", CrystalLogic.dtapLeadMs(100, 100, -20) == 200);
        check("dtap lead aim 100", CrystalLogic.dtapLeadMs(30, 30, 100) == 270);
        check("dtap lead spread", CrystalLogic.dtapLeadMs(100, 250, -20) == 300);
        check("dtap hold", CrystalLogic.dtapHold(R, 10, 14, 8, 1, 300, 200, 3, 3));
        check("dtap 0 totems", !CrystalLogic.dtapHold(R, 10, 14, 8, 0, 300, 200, 3, 3));
        check("dtap 2 totems", !CrystalLogic.dtapHold(R, 10, 14, 8, 2, 300, 200, 3, 3));
        check("dtap within lead", !CrystalLogic.dtapHold(R, 10, 14, 8, 1, 200, 200, 3, 3));
        check("dtap over lead", CrystalLogic.dtapHold(R, 10, 14, 8, 1, 201, 200, 3, 3));
        check("dtap non-pop", !CrystalLogic.dtapHold(R, 7.9, 14, 8, 1, 300, 200, 3, 3));
        check("dtap short follow-up", !CrystalLogic.dtapHold(R, 10, 8.9, 8, 1, 300, 200, 3, 3));
        check("dtap exact 9", CrystalLogic.dtapHold(R, 10, 9, 8, 1, 300, 200, 3, 3));
        check("dtap lethal 0", !CrystalLogic.dtapHold(new Rules(6, 6, true, 0), 10, 14, 8, 1, 300, 200, 3, 3));
        check("dtap self shielded", !CrystalLogic.dtapHold(R, 10, 14, 8, 1, 300, 200, 2.9, 3));

        {
            CrystalLogic.Window self = new CrystalLogic.Window(() -> 0);
            self.damaged(0, 10);
            double now = self.shielded(10, 1, 0.6), later = self.shielded(10, 8, 0.6);
            check("dtap self shielded now", now == 0 && later == 10);
            check("dtap self not held", !CrystalLogic.dtapHold(R, 10, 14, 8, 1, 300, 200, now, 10));
        }

        {
            java.util.function.DoubleUnaryOperator netherite = raw -> raw * (1 - Math.clamp(20 - raw / (2 + 12 / 4.0), 20 * 0.2, 20) / 25) * (1 - 16 / 25.0);
            double a = netherite.applyAsDouble(80), o = netherite.applyAsDouble(20), real = netherite.applyAsDouble(60);
            double ex = CrystalLogic.rawExcess(netherite, a, o);
            check("dtap raw excess", Math.abs(ex - real) < 1e-6 && real < a - o - 5);
            check("dtap raw plain", Math.abs(CrystalLogic.rawExcess(raw -> raw, 14, 4) - 10) < 1e-9);
            check("dtap raw only after armour", CrystalLogic.dtapHold(R, a - o, 20, 16, 1, 300, 200, 3, 3) && !CrystalLogic.dtapHold(R, ex, 20, 16, 1, 300, 200, 3, 3));
        }

        {
            int cases = 0, newWindows = 0, held = 0, total = 0;
            for (double rtt : new double[]{20, 30, 50, 100, 150, 250})
                for (double spread : new double[]{0, 50, 150})
                    for (double aimMs : new double[]{-20, 0, 50, 100})
                        for (double phase = 0; phase < 50; phase += 5) {
                            double slowest = rtt + spread, gLead = CrystalLogic.dtapLeadMs(rtt, slowest, aimMs);
                            double[] now = {phase};
                            CrystalLogic.Window gw = new CrystalLogic.Window(() -> now[0]);
                            gw.damaged(0, 4);
                            double sentAt = -1;
                            for (now[0] = 50; now[0] < 1000 && sentAt < 0; now[0] += 50)
                                if (!CrystalLogic.dtapHold(R, gw.countedAt(14, rtt, aimMs), 14, 8, 1, gw.dueIn(rtt, aimMs), gLead, 3, 3)) sentAt = now[0];
                            total++;
                            if (sentAt == 50) continue;
                            held++;
                            now[0] = sentAt;
                            gw.sent(14, rtt, CrystalLogic.answerTicks((int) rtt) * 50);
                            now[0] = Math.ceil((sentAt + slowest) / 50 + 1e-9) * 50;
                            int tick = (int) (now[0] / 50);
                            gw.exploded(tick, 14);
                            gw.popped(tick);
                            cases++;
                            if (gw.startedAt() != 0) newWindows++;
                        }
            check("dtap grid no new window", newWindows == 0);
            check("dtap grid held majority", held > total / 2);
        }

        {
            double[] now = {0};
            CrystalLogic.Window dw = new CrystalLogic.Window(() -> now[0]);
            dw.damaged(0, 4);
            double rtt = 100, aimMs = -20, dLead = CrystalLogic.dtapLeadMs(rtt, rtt, aimMs), sentAt = -1;
            for (now[0] = 50; now[0] < 500 && sentAt < 0; now[0] += 50) {
                double counted = dw.countedAt(14, rtt, aimMs);
                if (!CrystalLogic.dtapHold(R, counted, 14, 8, 1, dw.dueIn(rtt, aimMs), dLead, 3, 3)) sentAt = now[0];
            }
            double popLands = sentAt + rtt, followLands = sentAt + 50 + rtt + 50 + rtt;
            check("dtap window held", sentAt > 50);
            check("dtap window pop lands inside", popLands <= CrystalLogic.WINDOW_MS + aimMs - 100);
            check("dtap window follow-up after", followLands >= CrystalLogic.WINDOW_MS + aimMs);
            check("dtap window gap", followLands - popLands <= rtt + 150);
        }

        java.util.Random rnd = new java.util.Random(7);
        int disagree = 0, named = 0;
        for (int i = 0; i < 200000; i++) {
            Rules r = new Rules(rnd.nextDouble() * 8, rnd.nextDouble() * 20, rnd.nextBoolean(), 0.5 + rnd.nextDouble(), rnd.nextDouble() * 2, rnd.nextBoolean());
            double d = rnd.nextDouble() * 40, self = rnd.nextDouble() * 40, th = 1 + rnd.nextDouble() * 35, sh = 1 + rnd.nextDouble() * 35;
            boolean face = rnd.nextBoolean(), totem = rnd.nextBoolean();
            boolean refused = CrystalLogic.score(r, d, self, th, sh, face, totem) == NONE;
            String why = CrystalLogic.why(r, d, self, th, sh, face, totem);
            if (refused != (why != null)) disagree++;
            if (why != null) named++;
        }
        check("why agrees with score", disagree == 0 && named > 0);
        check("why ratio named", "hurts you more than the target (damage-ratio)".equals(CrystalLogic.why(new Rules(2, 20, true, 1, 1), 10, 15, 36, 20, false, false)));

        if (failures != 0) {
            System.out.println("FAIL: AggressiveCrystalLogicCheck (" + failures + " failures)");
            System.exit(1);
        }
        System.out.println("PASS: AggressiveCrystalLogicCheck");
    }
}
