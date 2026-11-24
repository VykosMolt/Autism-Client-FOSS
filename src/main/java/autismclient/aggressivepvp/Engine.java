package autismclient.aggressivepvp;

import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.Setting;
import autismclient.modules.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * The Aggressive side of one AUTISM module: a "Version" choice at the top of the module's settings, AUTISM's own
 * settings shown only under Default, and this engine's settings shown only under Aggressive. The module hands its
 * lifecycle to the engine while it runs.
 */
public abstract class Engine {
    public static final String DEFAULT = "Default", AGGRESSIVE = "Aggressive";

    public final Module module;
    private final ChoiceSetting version;
    private final Set<String> shared;
    private final List<Setting<?, ?>> own = new ArrayList<>();
    /** Whether the engine runs the module instead of AUTISM's own code (Versions). */
    public boolean running;
    /** Whether onActivate has run for this world: it needs a player, which a module switched on at startup has not. */
    public boolean activated;
    /** The engine threw: AUTISM's own code runs the module until the game restarts (Versions.guard). */
    public boolean failed;
    /** The client tick the engine last acted in (Versions.tickKey). */
    long ranAt;

    protected Engine(Module module, Set<String> shared) {
        this.module = module;
        this.shared = shared;
        for (Setting<?, ?> setting : module.settings()) {
            if (!shared.contains(setting.id())) setting.wrapVisibleWhen(() -> !showing());
        }
        // AUTISM shows a description of five words or fewer, as subtitle and tooltip; a longer one not at all
        version = new ChoiceSetting("version", "Version", DEFAULT, DEFAULT, AGGRESSIVE)
            .description("Which logic runs this module.")
            .keepOnReset();
        module.addSetting(version);
        module.moveSettingToTop(version);
    }

    public boolean aggressive() {
        return AGGRESSIVE.equals(version.get());
    }

    /** The menu shows what runs: an engine that stopped shows AUTISM's own settings. */
    private boolean showing() {
        return aggressive() && !failed;
    }

    /** Settings of this engine: shown only under Aggressive, and only when their own condition holds. */
    protected <T, S extends Setting<T, S>> S add(S setting, BooleanSupplier when) {
        setting.visibleWhen(() -> showing() && when.getAsBoolean());
        module.addSetting(setting);
        own.add(setting);
        return setting;
    }

    /**
     * The module's settings in AUTISM's order, which draws groups by their first setting: Version, AUTISM's own (shown
     * under Default), then this engine's by group. A visual both versions share closes its group, as it does under
     * Default (No Render ends General); one in a group of its own (CrystalView) comes last.
     */
    protected void arrange(String... groups) {
        List<String> order = Arrays.asList(groups);
        List<Setting<?, ?>> visuals = new ArrayList<>();
        for (Setting<?, ?> s : module.settings()) if (shared.contains(s.id())) visuals.add(s);
        List<Setting<?, ?>> tail = new ArrayList<>();
        for (String group : order) {
            for (Setting<?, ?> s : own) if (group.equals(s.group())) tail.add(s);
            for (Setting<?, ?> s : visuals) if (group.equals(s.group())) tail.add(s);
        }
        for (Setting<?, ?> s : own) if (!tail.contains(s)) tail.add(s);
        for (Setting<?, ?> s : visuals) if (!tail.contains(s)) tail.add(s);
        module.moveSettingsToEnd(tail);
    }

    protected <T, S extends Setting<T, S>> S add(S setting) {
        return add(setting, () -> true);
    }

    /** Lifecycle, called through the module while the engine runs. */
    public abstract void onActivate();

    public abstract void onDeactivate();

    public abstract void onPreTick();

    public String info() {
        return "";
    }

    /** Work for the client thread, from the network thread: it runs only while the engine still runs, and guarded. */
    protected void later(Runnable code) {
        Versions.later(this, code);
    }

    /** The engine's Explain setting: hints and warnings in chat only while it is on. */
    protected boolean explain() {
        return true;
    }

    protected void info(String format, Object... args) {
        if (explain()) Autism.chat(module.name() + ": " + String.format(format, args));
    }

    protected void warning(String format, Object... args) {
        if (explain()) Autism.chat("§e" + module.name() + ": " + String.format(format, args));
    }

    protected void error(String format, Object... args) {
        Autism.chat("§c" + module.name() + ": " + String.format(format, args));
    }
}
