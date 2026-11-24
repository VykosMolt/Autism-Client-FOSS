# AUTISM Client 5.8 (Minecraft 26.3)

A FOSS fork of **AUTISM Client** for **Minecraft 26.3** (Fabric, Java 25).

This is still a work in progress. Right now the codebase is a ~750k-line consolidation of my own patched version of 5.3, a version of 5.3 made for 26.3, the 5.7.11 updates (including the AI/parkour stack), Aggressive PvP addon, and the further MC 26.3 fixes in one tree. The goal is to end up with something much shorter, cleaner and actually manageable by cutting down vibecoded slop like the countless monolithic classes of over 9k lines of code ( ~20k lines for Scaffolding btw, and it didnt work before I fixed it), removing unrelated extras that belong as their own mods (like the matchmaking system), and replacing subsystems such as swapping `multi` out for an [Azalea](https://github.com/azalea-rs/azalea)-based Rust network protocol.


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
