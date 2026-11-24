# AUTISM Addon Authoring (AI runtime path)

This guide is the complete contract for addons created through the `client:addon` capability
(scaffold → write → build → load → verify → install). Addons authored here are compiled inside
the running game and loaded at runtime — a stricter path than a normal Gradle-built addon jar.
Follow the rules below exactly and the addon loads first try.

## The addon contract

Every addon is ONE class extending `autismclient.api.SimpleAddon`:

- public **no-arg constructor** calling `super(ApiVersion.CURRENT, "<your root package>")`.
- `protected void initialize()` — all registration happens here, and only here.
- Package is always `aiaddon.<id>` (dashes in the id become underscores).
- The host instantiates the class once, checks the API version, runs `initialize()`, and rolls
  back everything it registered if anything throws — so a broken addon never half-registers.
- Optional `onUnload()` override releases background threads/handles when the addon is unloaded.

## Complete minimal example (compilable as-is)

```java
package aiaddon.example;

import autismclient.api.ApiVersion;
import autismclient.api.SimpleAddon;
import autismclient.api.module.SimpleModule;
import autismclient.util.AutismClientMessaging;

public final class ExampleAddon extends SimpleAddon {
    public ExampleAddon() {
        super(ApiVersion.CURRENT, "aiaddon.example");
    }

    @Override
    protected void initialize() {
        SimpleModule module = new SimpleModule("hello-module", "HelloModule", "Tiny example addon module.")
            .addBool("greet", "Greet On Enable", true)
            .addText("message", "Message", "Hello from my addon")
            .onEnabled(self -> {
                if (self.getBool("greet")) {
                    AutismClientMessaging.sendPrefixed("§a" + self.getText("message"));
                }
            });
        registerModule(module);

        registerSimpleAction("say-hello", "Say Hello", "Send a short addon message.", "S",
            action(ignored -> AutismClientMessaging.sendPrefixed("§aHello from the example addon!")));

        registerSimpleCondition("always-ready", "Always Ready", "Passes immediately.", "Waiting", "P",
            condition(ignored -> true));
    }

    // Raw functional-interface targets keep net.minecraft out of this class's bytecode — see below.
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static java.util.function.Consumer action(java.util.function.Consumer<Object> body) {
        return (java.util.function.Consumer) body;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static java.util.function.Predicate condition(java.util.function.Predicate<Object> body) {
        return (java.util.function.Predicate) body;
    }
}
```

## Hard rules (violations fail at load, sometimes with confusing errors)

1. **No mixins.** Runtime addons are plain classes; the mixin pipeline does not exist for them.
2. **No `net.minecraft.*` references — of ANY kind.** The production runtime is
   intermediary-mapped: named classes like `net.minecraft.client.Minecraft` do not exist there
   (only `net.minecraft.class_310`-style names do). Mod-owned classes (`autismclient.api.*`,
   `autismclient.util.*`, `autismclient.modules.*`, `autismclient.commands.*`) keep stable names.
3. **Hidden references count too.** A lambda written against the inferred type, e.g.
   `registerSimpleAction("x", ..., mc -> ...)` where the parameter type is
   `Consumer<Minecraft>`, compiles a synthetic method whose descriptor names
   `net/minecraft/client/Minecraft` — the constant pool reference fails to link in production
   even when the lambda never uses the parameter. The same applies to method references and to
   subclassing `AddonAction`/`AddonContextAction` (their `run(Minecraft)` descriptor would not
   match the intermediary descriptor at runtime). The safe pattern is the raw-target helper from
   the example: `action(obj -> ...)` / `condition(obj -> ...)`; the lambda then targets
   `Consumer<Object>`/`Predicate<Object>` and no Minecraft type appears anywhere. Never "clean
   up" those casts, and never call Minecraft methods on the lambda parameter.
4. **Ids are namespaced.** Everything you register gets scoped to `<addonId>:<localId>`
   automatically (`AutismAddons.id(local)`); pass only the LOCAL id ("hello-module") to
   registration helpers. Never hard-code the prefix yourself except where a literal full id is
   required (AI capability descriptors — there the id must literally start with `<addonId>:`).
5. **Register inside `initialize()` only.** Registration before/after the load pass is rejected.
6. **No-arg constructor, no static registration.** The host constructs the class reflectively.

## API cheat-sheet (all mod-owned, all runtime-safe)

- `SimpleAddon` helpers: `registerModule(module)`, `registerSimpleAction(localId, label, tip, icon, Consumer)`,
  `registerSimpleCondition(localId, label, tip, status, icon, Predicate)`,
  `registerPreset(label, tip, Supplier<List<MacroAction>>)`, `id(localId)` → scoped id.
- `SimpleModule` (`autismclient.api.module`): fluent module.
  `new SimpleModule(localId, name, description)` → `.addBool(id, label, def)`,
  `.addInt(id, label, def, min, max, step)`, `.addDouble(...)`, `.addText(id, label, def)`,
  `.addChoice(id, label, def, choices...)`, `.addColor(id, label, argb)`,
  `.onEnabled(m -> ...)`, `.onDisabled(m -> ...)`, `.onTick(m -> ...)`.
  Read back with `m.getBool(id)`, `getInt`, `getDouble`, `getText`, `getChoice`.
  All lambdas here take `SimpleModule` — a mod-owned class, always safe.
- `MacroActionEntry.local(localId, factory)` builds a scoped entry for
  `AutismAddons.macroActions().register(entry)`; `.picker(label, tip)` for actions,
  `.condition(label, tip)` for wait-conditions. `SimpleAction`/`SimpleCondition` are the
  lambda-based implementations `SimpleAddon` creates for you (prefer the helpers).
- `AutismAddons` facade: `modules()`, `commands()`, `macroActions()`, `presets()`, `hud()`,
  `events()`, `ai()`, `id(localId)`, `apiVersion()`, `list()`.
- Commands: `AutismAddons.commands().register(new Command(name, description) { ... })` —
  override `build(LiteralArgumentBuilder<AutismCommandSource> root)` and use
  `root.executes(ctx -> { ...; return SUCCESS; })`. Brigadier and `AutismCommandSource` are
  stable-named and safe. `AutismClientMessaging.sendPrefixed("§a...")` prints to chat.
- Events: `AutismAddons.events().onTick/onGameJoin/onGameLeft/onPacketSend/onPacketReceive` —
  listeners receive Minecraft types, so wrap them in the raw-target helper like actions.
- AI capabilities: `AutismAddons.ai().register(provider)` where provider implements
  `autismclient.api.ai.AiCapabilityProvider`: `capabilities()` returns
  `AiCapabilityDescriptor(id = "<addonId>:<local>", owner = "<addonId>", version = 1, family,
  purpose, aliases, inputSchema (JSON string), prerequisites, effects, resources,
  successCriteria, failureRecovery, examples, classification)` and
  `start(id, argumentsJson, observation)` returns an `AiOperation` (tick/cancel;
  terminal statuses SUCCEEDED/FAILED/UNCERTAIN/TIMED_OUT). The whole `autismclient.api.ai`
  package is Minecraft-free, so AI capabilities are the richest fully-safe extension point.

## The client:addon workflow

1. `scaffold {id, name, description, features:[module|macro-action|macro-condition|command|ai-capability]}`
   creates `config/autism/addons/<id>/` with `src/aiaddon/<id>/<Name>Addon.java` and addon.json.
   Ids: `[a-z][a-z0-9_-]{1,31}`. Existing non-empty dirs need `force:true`.
2. `write {id, path, content}` replaces one .java file (≤200k chars) — this is how you fix
   compile errors: read the error list, rewrite the file, rebuild.
3. `read {id, path?}` lists files or reads one back.
4. `build {id}` compiles every .java against the client jar and writes `<id>.jar` (classes plus
   a generated fabric.mod.json). Failures return `errors:[{file,line,message}]` (≤20).
5. `load {id}` runtime-loads the compiled classes through AddonManager and reports
   status/detail/registered ids. `verify {id}` re-checks the live state + the load report;
   the AI catalog picks up new AI capabilities within ~20 ticks on its own.
6. `install {id}` copies the jar into mods/ for normal discovery on next restart (the runtime
   instance keeps running until restart or `unload`). `unload {id}` rolls back the runtime load.

## Common pitfalls

- `NoClassDefFoundError`/`NoSuchMethodError` on load → a `net.minecraft` reference slipped in
  (usually an inferred lambda parameter — use the raw-target helpers).
- "missing no-arg constructor" / instantiation failures → constructor takes arguments or the
  class is not public.
- Registration rejected → registering outside `initialize()`, a duplicate id, or a
  hard-coded namespace prefix (pass local ids; scoping is automatic).
- `NEEDS_NEWER_API` on load → you passed something other than `ApiVersion.CURRENT` to super.
- Wrong package → it must match the root package passed to `super(...)` and live under
  `aiaddon.<id>`; the generated scaffold already gets this right — edit the file in place
  instead of moving it.
- Reading module settings outside the module → keep settings on the module and use its
  handlers; settings ids are local strings, scoped automatically.
- Net.minecraft imports from copied tutorial code (the Gradle-built minimal template uses
  `mc.player`) → that code is for jar-built addons that get remapped; rewrite it against the
  cheat-sheet above for the runtime path.
