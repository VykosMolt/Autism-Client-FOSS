package autismclient.aggressivepvp;

import autismclient.api.module.Setting;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.Module;
import autismclient.modules.CrystalAuraModule;
import autismclient.util.AutismConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.InteractionHand;

/**
 * Verifies native Aggressive PvP integration inside AUTISM 5.3-26.3:
 * constructor wiring, Version setting placement/defaults/reset, Default vs Aggressive visibility,
 * shared visuals, group contiguity, 5-word descriptions, delay units, lifecycle takeover,
 * preMovementTick switchOver, ActionWindow hold guard, error fallback + settingsHint, and defaults.
 */
public final class AggressiveWiringCheck {
    static int checks, failures;

    static void check(String what, boolean ok) {
        checks++;
        if (!ok) {
            failures++;
            System.out.println("FAILED: " + what);
        }
    }

    static boolean visible(Module m, String id) {
        Setting<?, ?> s = m.setting(id);
        if (s == null) throw new IllegalStateException("no setting " + id);
        return s.isVisible();
    }

    static String def(Module m, String id) {
        return m.setting(id).defaultValue();
    }

    static double num(Module m, String id) {
        return Double.parseDouble(def(m, id));
    }

    static int[] shown(Module m) {
        int own = 0, ours = 0;
        for (Setting<?, ?> s : m.settings()) {
            if (!s.isVisible()) continue;
            if (s.id().startsWith("ag-")) ours++;
            else if (!s.id().equals("version")) own++;
        }
        return new int[]{own, ours};
    }

    static Map<String, Boolean> visuals(Module m) {
        Map<String, Boolean> out = new TreeMap<>();
        for (Setting<?, ?> s : m.settings()) {
            String sid = s.id();
            if (sid.equals("crystal-view") || sid.startsWith("view-") || sid.equals("no-render")) {
                out.put(sid, s.isVisible());
            }
        }
        return out;
    }

    static void lifecycle(Runnable call) {
        try {
            call.run();
        } catch (Throwable e) {
            // Default AUTISM code may fail headless (needs Minecraft instance); Aggressive PvP code must never throw here.
            for (Throwable cur = e; cur != null; cur = cur.getCause()) {
                for (StackTraceElement f : cur.getStackTrace()) {
                    if (f.getClassName().startsWith("autismclient.aggressivepvp.")) {
                        throw new AssertionError("Aggressive PvP code threw during lifecycle", e);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Isolate AutismConfig in memory so this check never reads or writes user config files.
        Field globalField = AutismConfig.class.getDeclaredField("globalInstance");
        globalField.setAccessible(true);
        globalField.set(null, new AutismConfig());

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        CrystalAuraModule crystalModule = new CrystalAuraModule();
        Constructor<AutoTotemModule> fcCtor = AutoTotemModule.class.getDeclaredConstructor();
        fcCtor.setAccessible(true);
        AutoTotemModule totemModule = fcCtor.newInstance();

        check("Autism.verified() is true", Autism.verified());

        Object[][] cases = {
            {crystalModule, Versions.crystal, "place", "crystal-view", "ag-keep-totems", "ag-trade-totem", "ag-min-damage", "4.5"},
            {totemModule, Versions.totem, "switch-mode", "no-render", "ag-health", "ag-mode", "ag-health", "3"}
        };

        for (Object[] c : cases) {
            Module module = (Module) c[0];
            Engine engine = (Engine) c[1];
            String ownSetting = (String) c[2];
            String sharedSetting = (String) c[3];
            String condTarget = (String) c[4];
            String condSource = (String) c[5];
            String resetKey = (String) c[6];
            String resetVal = (String) c[7];
            boolean isCrystal = module == crystalModule;

            String name = module.id();
            List<Setting<?, ?>> all = module.settings();
            int ours = 0;
            for (Setting<?, ?> s : all) if (s.id().startsWith("ag-")) ours++;

            check(name + ": engine built by module constructor", engine != null && engine.module == module);
            check(name + ": Version is the first setting", all.get(0).id().equals("version"));
            check(name + ": Version defaults to Default", module.value("version").equals("Default"));

            int[] defShown = shown(module);
            check(name + ": under Default no engine setting shows", defShown[1] == 0);
            check(name + ": under Default AUTISM's own " + ownSetting + " shows", visible(module, ownSetting));
            check(name + ": shared " + sharedSetting + " shows under Default", visible(module, sharedSetting));

            lifecycle(module::onEnable);
            check(name + ": enabled under Default -> AUTISM runs", !engine.running);
            lifecycle(module::onDisable);

            if (isCrystal) module.setValueTransient("crystal-view", "true");
            Map<String, Boolean> visualsDefault = visuals(module);
            module.setValueTransient("version", "Aggressive");
            check(name + ": shared visuals show under Aggressive as under Default", visuals(module).equals(visualsDefault) && visualsDefault.containsValue(true));
            if (isCrystal) module.setValueTransient("crystal-view", "false");

            int[] aggShown = shown(module);
            check(name + ": under Aggressive none of AUTISM's non-shared settings show", aggShown[0] <= 1 && !visible(module, ownSetting));
            check(name + ": shared " + sharedSetting + " still shows under Aggressive", visible(module, sharedSetting));
            check(name + ": under Aggressive engine settings show", aggShown[1] > 0);

            List<String> longText = new ArrayList<>(), noUnit = new ArrayList<>(), runs = new ArrayList<>();
            Setting<?, ?> lastOfShared = null;
            String sharedGroup = module.setting(sharedSetting).group();
            for (Setting<?, ?> s : all) {
                String sid = s.id(), text = s.description();
                if ((sid.startsWith("ag-") || sid.equals("version")) && (text == null || text.trim().split("\\s+").length > 5)) {
                    longText.add(sid);
                }
                if (sid.startsWith("ag-") && !s.getClass().getSimpleName().equals("BoolSetting") && s.label().contains("Delay") && s.unit().isEmpty()) {
                    noUnit.add(sid);
                }
                if (!s.isVisible()) continue;
                String group = s.group();
                if (runs.isEmpty() || !runs.get(runs.size() - 1).equals(group)) runs.add(group);
                if (group.equals(sharedGroup)) lastOfShared = s;
            }
            check(name + ": descriptions <= 5 words " + longText, longText.isEmpty());
            check(name + ": every delay names its unit " + noUnit, noUnit.isEmpty());
            check(name + ": each Aggressive group is one contiguous block " + runs, runs.size() == new HashSet<>(runs).size());
            check(name + ": shared " + sharedSetting + " closes group " + sharedGroup, lastOfShared != null && lastOfShared.id().equals(sharedSetting));

            lifecycle(module::onEnable);
            check(name + ": enabled under Aggressive -> engine runs", engine.running);
            check(name + ": shouldCancelUse false while Aggressive running", !module.shouldCancelUse(null, InteractionHand.MAIN_HAND));
            check(name + ": shouldCancelAttack false while Aggressive running", !module.shouldCancelAttack(null));
            if (isCrystal) {
                check("crystal-aura: load_Z_23 false while Aggressive running", !crystalModule.isSilentMovementActive());
                check("crystal-aura: info() safe while Aggressive running", crystalModule.info() != null);
            }
            lifecycle(module::onDisable);
            check(name + ": disabled -> engine stops", !engine.running);

            module.setValueTransient(resetKey, resetVal);
            // Reset settings in-memory (avoiding disk save by checking keepOnReset + resetSettings behavior directly)
            for (Setting<?, ?> s : all) {
                if (!s.isKeptOnReset()) module.setValueTransient(s.id(), s.defaultValue());
            }
            check(name + ": reset keeps Aggressive and resets " + resetKey,
                module.value("version").equals("Aggressive") && module.value(resetKey).equals(def(module, resetKey)));

            boolean before = visible(module, condTarget);
            module.setValueTransient(condSource, isCrystal ? "false" : "Smart");
            boolean after = visible(module, condTarget);
            check(name + ": " + condTarget + " follows " + condSource, isCrystal ? (before && !after) : (!before && after));

            module.setValueTransient("version", "Default");
            check(name + ": back under Default engine settings hide again", shown(module)[1] == 0 && visible(module, ownSetting));
        }

        // Input hold safety when Aggressive AutoTotem is off
        ActionWindow.holdInput();
        check("leftover input hold dropped when Aggressive AutoTotem off", !ActionWindow.inputHeld());

        // Runtime switchOver + error fallback + settingsHint
        var prevEnabled = Versions.enabled;
        boolean[] on = {true};
        Versions.enabled = m -> on[0];
        try {
            crystalModule.setValueTransient("version", "Aggressive");
            lifecycle(crystalModule::preMovementTick);
            check("preMovementTick switches crystal-aura to Aggressive", Versions.crystal.running);
            crystalModule.setValueTransient("version", "Default");
            lifecycle(crystalModule::preMovementTick);
            check("preMovementTick switches crystal-aura back to Default", !Versions.crystal.running);

            crystalModule.setValueTransient("version", "Aggressive");
            lifecycle(totemModule::preMovementTick);
            check("auto-totem preMovementTick also switches crystal-aura to Aggressive", Versions.crystal.running);
            crystalModule.setValueTransient("version", "Default");
            lifecycle(totemModule::preMovementTick);
            check("auto-totem preMovementTick switches crystal-aura back to Default", !Versions.crystal.running);

            Versions.totem.running = true;
            Versions.totem.activated = true;
            check("aggressiveTotemOn true when live", Versions.aggressiveTotemOn());
            ActionWindow.holdInput();
            check("input hold active when Aggressive AutoTotem live", ActionWindow.inputHeld());
            check("AutoTotemModule.init_Z_6 reflects Aggressive input hold", AutoTotemModule.init_Z_6());
            check("AutoTotemModule.isBusyOrPausingMovement reflects Aggressive input hold", AutoTotemModule.isBusyOrPausingMovement());
            ActionWindow.releaseHold();
            check("releaseHold clears hold", !ActionWindow.inputHeld());
            check("AutoTotemModule.init_Z_6 false when Aggressive idle", !AutoTotemModule.init_Z_6());
            Versions.totem.activated = false;
            check("AutoTotemModule.init_Z_6 does not recurse when running but not activated", !AutoTotemModule.init_Z_6());
            Versions.totem.activated = true;
            on[0] = false;
            check("aggressiveTotemOn false when module disabled", !Versions.aggressiveTotemOn());
            on[0] = true;

            // Default -> Aggressive switchOver must clear Default CrystalAura switch-back and Default AutoTotem input hold
            Field tStopField = CrystalAuraModule.class.getDeclaredField("stop");
            tStopField.setAccessible(true);
            tStopField.setInt(crystalModule, 4);
            crystalModule.setValueTransient("version", "Aggressive");
            lifecycle(crystalModule::preMovementTick);
            check("Default -> Aggressive clears crystal-aura switch-back slot", Versions.crystal.running && tStopField.getInt(crystalModule) == -1);
            crystalModule.setValueTransient("version", "Default");
            lifecycle(crystalModule::preMovementTick);

            Field fcStartField = AutoTotemModule.class.getDeclaredField("start");
            fcStartField.setAccessible(true);
            fcStartField.setInt(null, 999999);
            totemModule.setValueTransient("version", "Aggressive");
            Versions.totem.running = false;
            lifecycle(totemModule::preMovementTick);
            check("Default -> Aggressive clears auto-totem pause-movement start", Versions.totem.running && fcStartField.getInt(null) == Integer.MIN_VALUE);

            // Rotations.reset() must clear movingBySentYaw
            Rotations.movingBySentYaw = true;
            Rotations.reset();
            check("Rotations.reset clears movingBySentYaw", !Rotations.movingBySentYaw);

            // Panic Mode stops Aggressive engines cleanly
            Versions.totem.running = true;
            Versions.totem.activated = true;
            AutismConfig panicCfg = AutismConfig.getGlobal();
            AutismConfig.ModuleState hideState = panicCfg.modules.computeIfAbsent("hide", k -> new AutismConfig.ModuleState());
            hideState.enabled = true;
            autismclient.modules.aD.init(panicCfg);
            check("Panic Mode suppresses aggressiveTotemOn", !Versions.aggressiveTotemOn());
            lifecycle(totemModule::preMovementTick);
            check("Panic Mode stops running Aggressive engine on preMovementTick", !Versions.totem.running);
            hideState.enabled = false;
            autismclient.modules.aD.init(panicCfg);

            Method guard = Versions.class.getDeclaredMethod("guard", Engine.class, Runnable.class);
            guard.setAccessible(true);
            totemModule.setValueTransient("version", "Aggressive");
            boolean returned = (Boolean) guard.invoke(null, Versions.totem, (Runnable) () -> { throw new IllegalStateException("planted"); });
            check("guard catches exception and marks engine failed", !returned && Versions.totem.failed);
            check("failed engine not live", !Versions.aggressiveTotemOn());
            check("failed engine shows settingsHint", "Aggressive is off until restart.".equals(totemModule.settingsHint()));
            check("failed engine menu falls back to Default settings", shown(totemModule)[1] == 0 && visible(totemModule, "switch-mode"));

            Versions.totem.failed = false;
            Versions.totem.running = false;
            Versions.totem.activated = false;
            totemModule.setValueTransient("version", "Default");
        } finally {
            Versions.enabled = prevEnabled;
        }

        // Defaults match Donut Aggressive PvP profile
        check("crystal defaults match",
            num(crystalModule, "ag-min-damage") == 1.5
                && num(crystalModule, "ag-max-damage") == 16
                && num(crystalModule, "ag-damage-ratio") == 0.85
                && def(crystalModule, "ag-predict-movement").equals("true")
                && def(crystalModule, "ag-trade-totem").equals("true")
                && num(crystalModule, "ag-break-range") == 4.75);
        check("totem defaults match", def(totemModule, "ag-mode").equals("Strict") && def(totemModule, "ag-instant").equals("true"));
        check("dtap and explain defaults off",
            def(crystalModule, "ag-dtap").equals("false")
                && def(crystalModule, "ag-explain").equals("false")
                && def(totemModule, "ag-explain").equals("false"));

        if (failures > 0) {
            System.out.println("FAIL: AggressiveWiringCheck (" + failures + " of " + checks + " failed)");
            System.exit(1);
        }
        System.out.println("PASS: AggressiveWiringCheck (" + checks + " checks)");
    }
}
