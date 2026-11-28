# Rewrite plan: the worst monoliths first (draft, 2026-11-28)

Status: proposal. Nothing here has started. Numbers are measured on `dev` after Phase 11D, except where a section names `deecfd9`,
unless marked *inferred*. Tools: `wc -l` on `src/main/java` (excluded paths pruned) and an ASM pass over
`build/classes/java/main` (`autism-deobf-plans` scratch tool `Mono.java`) that folds nested classes into their
top-level class and counts bytecode instructions in methods that touch Minecraft, Mojang, Fabric, LWJGL, Mixin,
Sodium, Iris or Via types directly ("game %").

## 1. What changes when rewriting starts

Today's safety net only works for renames. The build maps names back to the jar's original names, and
`VerifyBytecodeEquiv` proves the jar is unchanged. A rewrite changes bytecode on purpose, so it needs a different net:

1. **Gate 2 stays for every class that is not being rewritten.** Rewritten classes would have to be listed
   explicitly (one file, printed by the gate), and everything else must stay equivalent. That is a change to a gate,
   so it is the owner's decision, not the agents'.
2. **Characterization tests before each rewrite.** Capture what the current code does through an existing seam
   (golden inputs to outputs), then run the same cases against the new code. Section 3 names the seam per target.
3. The 44 `tools/checks` and the 3 mixin audit profiles keep running unchanged.
4. In-game smoke tests are the owner's; agents never launch the game.

Constraints that do not move:
- Integrity classes and everything in AGENTS.md §1 stay untouched until bright's removal.
- ABI-pinned classes stay frozen: `modules/f` (8,595 lines), `as`, `aD`, `ae`, `am`, `ar`, `util/L`, `gui/f/*`,
  and the rest of the FROZEN list. The same goes for `autismclient.api.*`, Gson DTO fields, public `MacroAction`
  fields (reflection binding and addon API), and members that `tools/checks` reflects on.
- *Unknown, and it cannot be inspected under the rules:* whether the integrity layer hashes jar contents. That
  matters before shipping native libraries (section 4).

## 2. The monoliths, measured

The source tree is 625,298 lines in 1,520 files (excluded paths not counted). Of all bytecode, 64% sits in
methods that touch game or platform types directly.

| File | Lines | Methods | Nested | Game % | Fan-in | What it is |
|---|---:|---:|---:|---:|---:|---|
| `ai/print/PrintCore` | 34,465 | 1,450 | 145 | 93 | 10 | schematic printer core loop |
| `modules/ScaffoldModule` | 18,595 | 1,319 | 45 | 89 | 31 | scaffold (placement + aim + movement) |
| `gui/macro/editor/ActionEditorOverlay` | 16,349 | 963 | 11 | 66 | 9 | per-action macro editor UI |
| `util/macro/MacroExecutor` | 11,435 | 899 | 32 | 76 | 65 | macro runtime |
| `ai/AiBuilder` | 8,777 | 399 | 19 | 88 | 26 | blueprint builder |
| `ai/engine/AiPathfinder` | 8,675 | 711 | 40 | 76 | 62 | A* pathfinding |
| `modules/f` (frozen) | 8,595 | 671 | 43 | 52 | 17 | ABI-pinned, not touchable |
| `util/AutismServerInfoOverlay` | 7,415 | 433 | 20 | 35 | 9 | server info / scan UI |
| `util/AutismMacroEditorOverlay` | 6,869 | 536 | 15 | 41 | 10 | macro list/editor UI |
| `ai/AiService` | 5,851 | 450 | 17 | 44 | 40 | AI agent runtime |
| `util/AutismPacketLoggerOverlay` | 5,445 | 361 | 16 | 43 | 13 | packet logger UI |
| `util/PacketInspectorEngine` | 5,301 | 281 | 16 | 54 | 3 | packet inspection |
| `ai/engine/AiPaceScript` | 5,175 | 351 | 22 | 62 | 10 | movement pacing scripts |
| `ai/engine/AiGoto` | 4,318 | 361 | 15 | 92 | 57 | goal-driven travel |
| `util/AutismHudManager` | 4,269 | 340 | 13 | 54 | 8 | HUD layout |
| `modules/KillAuraModule` | 4,112 | 312 | 8 | 79 | 15 | kill aura |

Fan-in counts the other top-level classes that reference this one. Smells that were measured:
- `MacroExecutor` has 304 `instanceof <X>Action` checks on 287 lines (`grep -o 'instanceof [A-Za-z.]*Action\b'`).
  Action behaviour lives in the executor, not in the action classes of `util.macro` (105 `*Action.java` files,
  102 of them classes; `MacroAction`, `AimTargetAction` and `CaptureOutputAction` are interfaces).
- `ActionEditorOverlay` has 88 `instanceof <X>Action` checks (same grep) and 117 `MacroActionType.` references (on 109 lines), though an
  editor schema already exists (`ActionFieldRegistry`, `ActionFieldSchema`).
- `PrintCore` reaches the world through `PrintEnv`, a 198-line interface with one implementation
  (`LivePrintEnv`). That is a ready-made seam for a fake world.
- `AiPathfinder` already searches through a grid abstraction (`AiPathfinder$GridView`, used by `isInGoalWithGrid`).

### Where the size comes from (measured at `deecfd9`)
An ASM pass over all 4,024 non-excluded classes (`autism-deobf-plans` scratch tool `Bloat.java`):
- **Not copy-paste:** exact duplicate method bodies (8+ instructions) account for 434 lines. Structural clones
  (40+ instructions, same shape with names and constants ignored) add 864 more.
- **Not dead private code:** 208 private methods that nothing references, 1,603 lines in all.
- **Size:** 341,924 of the 625,298 source lines carry bytecode. The rest is declarations, braces and decompiler
  formatting.
- **Breadth and concentration:** `ai` is 255k lines (41%), `util` 175k (28%), `modules` 94k, `gui` 60k. The 46 files
  over 2,000 lines hold 236,614 lines (38%); the 100 largest hold 51%.
- **God methods:** 233 methods over 100 lines and 48 over 200. The largest are `ActionFieldRegistry.<clinit>` (1,465
  lines of registry table), `AiBuilder$2.tickBuild` (821), `MacroExecutor.executeRun` (721),
  `AiPathfinder$MoveGenerator.generateMovesCached` (553), `AiTools.capabilities` (551) and `AiSchematic.legacyBlock`
  (551).

So deduplication and dead-code removal buy little. The levers, largest first:
1. **Feature scope.** The AI automation stack alone is bigger than most whole clients. Cutting features the owner does
   not want removes more code than any refactor, and it is cheaper before a rewrite than after one. This needs a
   feature inventory (lines, fan-in, entry points per feature) and the owner's keep/cut list.
2. **God-class splits** (R1–R6 below).
3. **Tables as data.** Registries written as code (`ActionFieldRegistry`, `AiTools.capabilities`,
  `AiSchematic.legacyBlock`) become resource files read at startup.

## 3. Order and approach

### R1. Macro system: `MacroExecutor`, `ActionEditorOverlay`, `AutismMacroEditorOverlay` (~34.6k lines)
First, because the fix is clear and testable, and fan-in 65 means everything else leans on it.
- Move execution into the actions (`execute(ctx)` per action, or a handler registry keyed by `MacroActionType`).
  Start with the type switches and remove the `instanceof` chain one action family at a time.
- Drive the editor from `ActionFieldSchema` instead of per-type code.
- **Contract:** the macro NBT format (`util/AutismMacro`). Golden test: every stored and sample macro decodes and
  re-encodes byte-identically, and a scripted run on a fake context produces the same action trace. The
  `LegacyMulti`, `DynamicBindingClamp` and `PacketGate` checks already cover parts of it.
- Language: Java. It is all game calls and UI.

### R2. Schematic printer: `PrintCore` (34.5k lines) and `PrintAccess` (3.4k lines)
- Build a fake `PrintEnv` (in-memory blocks, scripted interaction receipts). Record what the current
  `PrintCore` asks for per tick on fixed schematics, and use that as the oracle.
- Split it into a planner (what to place next, support and reachability, build order: pure given a world
  snapshot), an executor (click and interaction protocol, inventory, receipts) and recovery.
- Language: Java for the executor. The planner is a Rust candidate once it runs on a plain voxel snapshot
  (*inferred*: needs a read of how much planner state reaches into `BlockState`).

### R3. Pathfinding: `AiPathfinder`, `AiGoto`, `AiJumpSolver`, `AiJumpManeuver` (~18k lines)
- This is the strongest Rust case: CPU-bound search, already behind `GridView`, and fan-in 62 means one interface
  serves many callers.
- Design: a Rust-side voxel cache fed by chunk-load, unload and block-update deltas, and one native call per
  search (not per node) returning the path as an int array. The Java implementation stays as the fallback and as
  the parity oracle (same inputs give the same path, or the same cost when ties differ).
- `~/Games/Horizon/horizon-autism` has Rust code ported from AUTISM 5.3 (`src/ai`, about 3k lines). Use it as
  reference only. It is older and written against Horizon's API.

### R4. `ScaffoldModule` (18.6k lines)
- Split by concern: placement planning, aim and rotation, timing and packets, telemetry (`FaceRect`,
  `KinematicsSnapshot`, `AxisDriftMetrics` and the like). `ScaffoldFailureRecorder` already records failures;
  its records can seed regression fixtures.
- Language: Java. It works on player physics and packets every tick.

### R5. Overlays: `AutismServerInfoOverlay`, `AutismPacketLoggerOverlay`, `AutismHudManager`, `ModulesMenuController`
- Move the hand-rolled layout onto the existing `gui.ui` toolkit (97 classes). Java.

### R6. AI task monoliths: `AiBuilder` (8,777 lines), `AiService` (5,851 lines)
- `AiBuilder` (fan-in 26, 88% game) runs a build in one 821-line `tickBuild` inside the anonymous `AiBuilder$2`.
  Split the build planner from the per-tick executor, the same way as R2. Its planner shares the schematic work
  with R2, so do it after R2.
- `AiService` (fan-in 40) is the agent loop: `tickClientState` (436 lines) and `runAgentLoop` (212). Split the
  provider and transport from the tool dispatch and from client-state polling.
- Language: Java.
- Both are readable now: 14 and 17 obfuscated members remain.

Not planned yet: `modules/f` (8,595 lines). The ABI pin fixes its name and the symbols that untouched frozen
files use, not its method bodies. So its internals can be rewritten behind the same signatures once the
rewritten-class gate policy (section 1) exists. Also not planned: `PacketInspectorEngine` (fan-in 3, low payoff).

## 4. Rust: where it makes sense

Rust gets self-contained compute with plain data in and out. Anything that touches game objects every tick stays
Java: mixins, event-driven modules, rendering, UI and packets. Measured, that is most of the code (64% game %
overall, 76-93% in the AI and module monoliths).

| Candidate | Verdict | Why |
|---|---|---|
| Path search and jump/movement simulation | yes | CPU-bound, already abstracted (R3) |
| Schematic decoding and build-order planning | yes | pure data. Parity: `SchematicDecodeCheck`. Horizon has NBT (853 lines) and schematics (2.9k lines) Rust as reference |
| Ore simulation (`util/oresim`, 2,948 lines) | no | it runs vanilla worldgen on a synthetic level (`VanillaWorldGenBootstrap`, `SyntheticWorldGenLevel`). A Rust port would reimplement vanilla worldgen and need re-verifying every MC update |
| Packet inspection | no | crossing the boundary per packet costs more than it saves |
| UI, modules, macros | no | game calls throughout |

Integration, if adopted:
- **Java 25 FFM API** (`java.lang.foreign`, final since JDK 22): no JNI C glue. A Rust `cdylib` exposes
  `extern "C"` functions. Bulk data goes through `MemorySegment` as flat arrays allocated in an arena.
- **Native libraries per platform** packed in the jar (at least linux-x86_64, windows-x86_64, macos-aarch64),
  extracted with a hash check. cargo runs from a Gradle task; cross-compile with cargo-zigbuild or a CI matrix.
- **Failure handling:** `catch_unwind` at every exported function so a panic becomes an error code. Rust never
  calls back into the JVM from its own threads. If the library fails to load, the Java path runs.
- **Native-access warning** (*inferred*): JDK 24+ warns on restricted native access unless the JVM gets
  `--enable-native-access` (JEP 472). Launchers rarely pass it, so check what users would see.
- **Open risk:** whether adding resources to the jar trips the integrity layer (section 1).

## 5. Before R1 starts (owner decisions)

1. The gate policy for rewritten classes (section 1, item 1).
2. ~~Deobfuscation of rewrite targets~~ Decided (owner, 2026-11-28): rewrite targets that are readable are not
   deobfuscated further; their remaining names get settled during the rewrite. At `deecfd9` most targets have 2-9
   obfuscated members per 1,000 lines (`PrintCore` 177 of 3,212 members). The exceptions are `ScaffoldModule` (531
   non-constant of 1,973, 27%) and `AiGoto` (120 of 461, 26%).
3. The target platforms for native libraries, and whether the native-access warning is acceptable.
4. Whether rewrites wait for bright's integrity removal. Only the frozen classes strictly have to.
5. Feature scope: which features to keep (section 2, "Where the size comes from"). This is the largest lever on size.
