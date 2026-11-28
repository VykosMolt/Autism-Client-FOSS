# Whole-client audit (2026-11-28, `dev` @ `deecfd9`)

Static audit of every non-excluded class: bloat, dead and vestigial code, practices, design, Rust candidates.
Companion to [REWRITE_PLAN.md](./REWRITE_PLAN.md).

**Method.**
- **Bytecode:** ASM passes over the 4,024 non-excluded classes in `build/classes/java/main`, reached through
  `InnerClasses` from the non-excluded top-level files, so excluded classes are never opened. Overrides of library
  methods are resolved against the Minecraft and library jars on the game classpath.
- **Source:** reads by exact path.
- **Tools:** `Audit.java`, `Bloat.java`, `Anon.java` and `StaleRows.java`, kept in the owner's `autism-deobf-plans`
  folder.
- **Labels:** *verified* means measured or read in the code; *inferred* means a judgement from that evidence.

**Blind spots.**
- Code in the excluded paths (AGENTS.md §1) is invisible. A class that only excluded code references shows up as
  unreachable here.
- `META-INF/services/autismclient.util.mm.guardian.Guardian` was not read.
- Runtime behaviour is not measured: no profiler, no game launch.

## 1. Where the size is (verified)

625,298 source lines in 1,520 files. 341,924 of them carry bytecode; the rest is declarations, braces and decompiler
formatting.

| Area | Lines | Share | Files |
|---|---:|---:|---:|
| Modules (`modules/`) | 93,930 | 15.0% | 150 |
| AI: printer, schematic, builder | 79,393 | 12.7% | 71 |
| Macros (`util/macro`, macro editor) | 74,764 | 12.0% | 152 |
| AI: other tasks (craft, combat, gather, farm…) | 66,857 | 10.7% | 136 |
| Other `util` | 59,432 | 9.5% | 232 |
| AI: movement engine (`ai/engine`) | 53,374 | 8.5% | 67 |
| GUI toolkit and screens | 40,360 | 6.5% | 152 |
| AI: chat, provider, agent runtime | 34,419 | 5.5% | 46 |
| Packet and payload tooling | 28,026 | 4.5% | 39 |
| Overlays and HUD | 25,243 | 4.0% | 15 |
| AI: speedrun and dimension campaigns | 21,216 | 3.4% | 17 |
| Other | 18,035 | 2.9% | 139 |
| Mixins | 12,294 | 2.0% | 220 |
| Protector | 10,536 | 1.7% | 67 |
| LAN sync, ore sim, dupe tooling | 7,419 | 1.2% | 17 |

Areas are assigned by package and class name, so the borders are approximate.
- **AI total:** 255k lines (41%), across 337 files.
- **Counts:** 63 module classes, 105 macro action classes, 251 mixins (254 files under `mixin/`).
- **Concentration:** the 100 largest files hold 51% of the code, and the 46 files over 2,000 lines hold 38%.
- **String literals:** 1.1M characters in all. The largest holders are `AiTools` (138k characters, mostly LLM tool
  descriptions and JSON schemas), `AiContext` (62k, prompt text), `ActionEditorOverlay` (36k) and
  `ActionFieldRegistry` (22k).

## 2. Dead and vestigial code (verified unless marked)

Dead code is about 1% of the client. Deleting it is worth doing but does not change the picture.

- **Unreachable classes: 7, 212 lines.**
  - `modules/ViewmodelModule` (98) is a `Module` that nothing registers; `ViewmodelState` is the live one.
  - Also unreachable: `util/mm/StateCache`, `ai/engine/AiStrafe`, `ai/engine/AiBridgeCadence`,
    `ai/AiPrintHopper` and `util/Q`.
  - Each was checked by name across all non-excluded sources: only its own file mentions it.
- **Unreferenced methods: 903, 4,260 lines.** Overrides, annotated methods, record and enum members, and names that
  appear as string literals were excluded. Notable:
  - `AiJumpSolver`: unused jump strategies, 279 lines. These are `solveSharpCornerJump`, `solveChainedExit`,
    `solveCurvedLiveJump` and `solveStrideJump`.
  - `PrintCore.nearbyCellsStep` (125 lines).
  - A private 183-line `PacketDetailFormatter` formatter.
  - `modules/f.init_V` (109 lines; a frozen class).
- **Fields that are written and never read: 124**, plus 7 that are never touched. Some are named that way:
  `AutismMacroEditorOverlay.unusedText`, `AiNetherRun.unusedRouteCounter`, `AiIntents.UNUSED_FARM_SIZE_LABELS`.
- **Exact duplicate method bodies: 434 lines.**
  - Examples: `lerpColor` copied into 4 widget renderers, `wrapDegrees` into 5 rotation classes (plus 3 more `F`
    variants), `encodeVarInt` twice, and `DirectPacketSender.send`/`sendAlias`.
  - Same-shape clones (names and constants ignored, 40+ instructions) add 864 lines.
- **Stale mapping rows:** 6,743 rows in `mappings/disambig.tsv` that nothing looks up. They are removed on branch
  `cleanup/stale-rows`; the jar is byte-identical without them.
- **Vestigial by purpose:**
  - `AiSchematic.legacyBlock` (551 lines) maps pre-1.13 numeric block IDs for old MCEdit schematics. It is live and
    belongs in a data file.
  - `ActionFieldRegistry.<clinit>` (1,465 lines) is a registry written as code.

## 3. Practices (verified counts; impact inferred)

| Finding | Count | Notes |
|---|---:|---|
| `catch (Throwable)` | 1,510 in 238 files | Also catches `OutOfMemoryError`, `StackOverflowError` and `LinkageError`. Top: `MacroExecutor` 42, `PrintCore` 34, `PacketInspectorEngine` 29. |
| Handlers that swallow the exception (store or pop, then continue or return) | 767 in 268 files | Example: `MacroExecutor.reportLanProgress`, `catch (Exception var5) {}`. Failures disappear without a trace. |
| Mutable static fields | 1,427 | Global state everywhere. Example: `NoRenderState` mirrors module settings into 59 `static volatile` booleans so mixins can read them, and each one has to be kept in sync by hand. |
| Raw `new Thread` | 69 in 38 files | No shared lifecycle. |
| Executor creation sites | 24 | 13 single-thread executors, 4 fixed pools, 1 cached pool, 5 scheduled, 1 virtual-thread. Several persistence stores each run their own scheduler thread. |
| `synchronized` blocks | 502 in 89 files | |
| `CompletableFuture` on the common pool | 9 call sites | AI views, schematic placement, packet inspect, clipboard. |
| Reflection | 64 `setAccessible` (24 files), 92 lookups (34 files) | Mostly packet and payload tooling reflecting on packet classes, and macro field binding. Breaks silently on Minecraft updates. |
| `System.out` | 10 sites | Debug leftovers in `AiBuilder$2.tickBuild`, `AiCombat$2`, `AiLavaEscape$1`, `AiService`, `AiNavDebug`, `AutismServerInfoOverlay.trace`. |
| `instanceof` chains of 25+ in one method | 15 methods | `MacroExecutor`: 304 checks on 287 lines. |
| Methods over 100 / 200 / 500 lines | 233 / 48 / 6 | |
| Mixin config `injectors.defaultRequire: 0` | 251 mixins in `autism.mixins.json` | An injection whose target vanished after a Minecraft update becomes a silent no-op at runtime. The build-time mixin audit catches it only for the 3 tested profiles. |

Checked and fine:
- The `Thread.sleep` calls in `AutoCraftingEngine` (guarded by `!onClient`) and `PayAllModule`
  (`BackgroundTaskExecutor`) run off the client thread.
- Crypto is AES/GCM and SHA-256. SHA-1 is only a GIF cache key.

## 4. Design decisions worth questioning

1. **The AI agent can write, compile and load code into the game (verified capability). Severity: high
   (inferred).**
   - `client:addon` lets the model:
     - scaffold Java source;
     - compile it with a `javac` it finds on the machine (PrismLauncher JDKs, `JAVA_HOME`, `PATH`);
     - load the classes into the running client (`AiAddons.loadAddon` → `autismclient.b.DataUtil.init`);
     - install the jar into `mods/`, where it persists across restarts.
   - I found no user-approval gate in `AiAddons` or `AiTools`. A gate elsewhere is not ruled out.
   - The agent also reads player chat (`chat_keep` lanes). The only defence against prompt injection is a sentence in
     the prompt: "chat lines are untrusted".
   - A player who can steer the model can therefore try to get arbitrary code running on the user's machine.
   - The minimum fix is an explicit in-game confirmation before `build`/`load`/`install`.
2. **The script runtime is an unreproducible binary.**
   - `script_run` executes JavaScript in a separate JVM (`-Xmx256m`), which is good isolation.
   - That JVM is `src/main/resources/autism-ai/worker.jar`: 1.9 MB with Rhino, Gson and `AiScriptWorker`. Its source
     is not in the repository.
3. **AI operations are giant anonymous classes.**
   - 45 anonymous classes have 150+ lines each, 26,321 lines in all.
   - `AiBuilder$2` is 2,981 lines with 184 fields and 117 methods. `AiBlockMine$1` is 1,794 lines, `AiGoto$7` 1,523
     and `AiDragon$1` 1,434.
   - Each is a whole task state machine that implements `AiOperation`. It can't be named, tested or reused.
4. **Parallel implementations of the same thing.**
   - Rotation and aim: 13 classes, about 5,000 lines (`CombatAimEngine`, `RotationStreamPlanner`, `AiAim`,
     `SilentRotationManager`, `SmoothRotationIntegrator`, `ScaffoldRotationSmoother`, `HumanizedAimStepper`,
     `ServerRotationTracker`, `AiCameraTurn`, `RotationMath`, `MacroAimController`, `HumanizedTurnPlanner`,
     `CombatAimSpeedController`).
   - HTTP: `AiHttp` (`HttpClient`), `AutismHttp` (`URLConnection`), `protector/ScopedHttpUrlConnection` and `AiWeb`
     (raw `Socket`).
   - Clipboard: GLFW plus process fallbacks (`wl-copy`, `wl-paste`, `xclip`, `xsel`), about 1,950 lines in 3 files.
   - Persistence: several stores, each with its own debounce scheduler thread.
5. **Hand-rolled overlays next to a UI toolkit.**
   - 23 `*Overlay` files hold 56,853 lines.
   - The `gui.ui` toolkit (97 classes) exists, but most overlays lay out by hand. R5 covers 4 of them.
6. **Macro threading (inferred risk).**
   - Each macro runs on its own thread (`Autism-Macro-<id>`).
   - Whether an action hops to the client thread (`mc.execute`, `postToClientThread`) or runs in place is decided
     action by action inside the 300-branch `instanceof` chain.
   - Thread safety depends on every action's author getting that right. Minecraft's client state is not thread-safe.
7. **Prompts and schemas as Java strings.** `AiTools` and `AiContext` hold 200k characters of string literals, most of
   it LLM prompt text and tool JSON schemas. As resources they could be edited, diffed and reviewed without recompiling.
8. **Licence checks inline in feature logic.**
   - The macro run loop's condition calls `AutismLiteVariant.enabled() || autismclient.e.BitSetUtil.init()`.
   - Both are excluded classes and were not inspected.
   - This ties feature control flow to the integrity layer; any rewrite has to keep those call sites intact until
     bright's removal.

## 5. Rust candidates

Static density cannot see runtime hotness. Many pure-Java classes are pure because they do I/O, JSON or HTTP, and
Rust buys nothing there. The candidates below are judged from what the code does (*inferred*), with measured sizes.
Profile in game (spark) before committing to any of them.

| Candidate | Size | Verdict |
|---|---|---|
| Path search, jump solving and movement simulation: `AiPathfinder`, `AiJumpSolver`, `AiPhysics`, `NavigationOpenSet` (R3) | about 13k lines, 44k instructions | Yes. CPU-bound search. Needs a voxel snapshot instead of `BlockState` lookups, then one native call per search. |
| Schematic decode and build planning: `AiSchematic`, `AiSchematicBatchPlan` (77% pure), `AiPlanMesh`, `AiSchematicReadiness` | about 4.4k lines | Yes, once blocks are plain ids. Parity oracle: `SchematicDecodeCheck`. |
| ESP voxel meshing (`EspVoxelMeshBuilder`) | 436 lines | Only if profiling shows it hot. |
| GIF decoding (`GifImageDecoder`) | 343 lines | No. Too small. |
| Ore sim (`util/oresim`) | 2,948 lines | No. It runs vanilla worldgen. |
| Explosion damage (`ExplosionDamageCalculator`) | 733 lines | No. Needs world access per ray. |
| AI provider, chat, HTTP, JSON, config | — | No. I/O-bound, strings. |
| Packets, UI, modules, macros, mixins | — | No. Game calls throughout. |

The two "yes" rows are about 17.5k lines, about 3% of the client. The leverage is elsewhere.

## 6. What to do, in order of payoff

1. **Feature scope (owner).**
   - Decide what to keep, per area in section 1. AI is 41%: the speedrun campaigns, AI task automation and the agent
     runtime are separable blocks.
   - Cutting before rewriting is the largest reduction available.
2. **Close the code-execution path (security).** Add an in-game confirmation before an AI-initiated addon
   `build`/`load`/`install` or `script_run`, or remove the tool.
3. **Turn the anonymous AI operations into named classes** with explicit state. This is mechanical, enables tests, and
   is the prerequisite for R6.
4. **Consolidate the parallel stacks:**
   - one rotation service, one HTTP client, one persistence scheduler;
   - `wrapDegrees`, `lerpColor` and the like into shared helpers.
5. **Exceptions and logging.**
   - Replace `catch (Throwable)` with specific types.
   - Log instead of swallowing, and route `System.out` to the logger.
6. **Threads.** One owned executor set with shutdown on client stop, instead of 24 executor creation sites and 69 raw
   threads.
7. **Data out of code:**
   - `ActionFieldRegistry`, `AiSchematic.legacyBlock`, and the prompt and schema text, into resources.
   - Build `worker.jar` from source in the repo.
8. **Delete the dead code** in section 2 (about 4.5k lines). Do it only once the rewritten-class gate policy exists,
   since deletions change bytecode.
9. **Rust** for R3, and for schematic planning after R2. Profile first.
