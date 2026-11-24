# AUTISM Client 5.8 (Minecraft 26.3)

A FOSS fork of **AUTISM Client** for **Minecraft 26.3** (Fabric, Java 25).

This is still a work in progress. Right now the codebase is a ~750k-line slopfest consolidating 5.3, the 5.7.11 updates (including the AI/parkour stack), Aggressive PvP, and the MC 26.3 fixes into one working tree. The goal is to end up with something much shorter and cleaner: cutting out the slop and bloat (like the matchmaking system), rewriting functions and classes properly, and replacing subsystems such as swapping `multi` out for an [Azalea](https://github.com/azalea-rs/azalea)-based Rust network protocol.

> Original integrity and authorization checks are kept untouched and excluded from this repo via `.gitignore`. Prebuilt JARs are under **Releases**.

## Branches

- `master`: **AUTISM Client 5.8** (Minecraft 26.3, active development)
- `5.3-patched`: Legacy **AUTISM Client 5.3** (Minecraft 26.3 patched)

## Building & Verifying Locally

Requires **Java 25**, Minecraft `26.3` libraries (listed in a local `cp26.3.txt` file, gitignored), and your local `precompiled/integrity-classes.jar` + `stubs/integrity-stubs.jar`.

```bash
# Build the Fabric mod JAR -> build/libs/AUTISM-Client-5.8-26.3.jar
./gradle.sh assemble

# Run the MC 26.3 Mixin audits (Sodium, Sodium+Iris, and vanilla Indigo)
SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh

# Run the regression checks
bash tools/checks/run.sh
```

## Contributing & Agentic Code Policy

Contributions (including **agentic / AI-authored code**) are welcome.

We **only accept agentic contributions** from the following models:
- **Mythos / Fable 5.2**
- **Opus 5.5**
- **Gemini 4 Neon**
- **GPT 6.2 Sol / Astra**

We kindly ask agents to **properly review their own code** before submitting:
- Audit your diffs for edge cases, MC 26.3 API mismatches, and decompiler artifacts.
- Run `./gradle.sh assemble`, `tools/verify-26.3.sh`, and `tools/checks/run.sh` and confirm everything passes.
- Do not modify or bypass integrity/authorization files.
