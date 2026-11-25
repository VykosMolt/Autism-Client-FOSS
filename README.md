# AUTISM Client 5.8 (Minecraft 26.3)

[![Build & Verify (MC 26.3)](https://github.com/VykosMolt/Autism-Client-FOSS/actions/workflows/ci.yml/badge.svg)](https://github.com/VykosMolt/Autism-Client-FOSS/actions/workflows/ci.yml)

A FOSS fork of **AUTISM Client** for **Minecraft 26.3** (Fabric, Java 25).

This is still a work in progress. Right now the codebase is a ~730k-line consolidation of my own patched version of 5.3, a version of 5.3 made for 26.3, the 5.7.11 updates (including the AI/parkour stack), Aggressive PvP addon, and the further MC 26.3 fixes in one tree. (small note: upstream itself is ~750k lines, which fun fact; is 120k lines more than Minecraft Java)

The goal, obviously, is to end up with something much shorter, cleaner and actually manageable by cutting down vibecoded slop like the countless monolithic classes of over 9k lines of code ( ~20k lines for Scaffolding btw, and it didnt even work before I fixed it), removing unrelated extras that belong as their own mods (the closed-backend matchmaking/MQTT/Guardian stack and donation/auth gating have already been stripped), and replacing subsystems such as swapping `multi` out for an [Azalea](https://github.com/azalea-rs/azalea)-based Rust network protocol.


## Branches

- `master`: **AUTISM Client 5.8** (Minecraft 26.3, active development)
- `5.3-patched`: Legacy **AUTISM Client 5.3** (Minecraft 26.3 patched)

## Building & Verifying Locally

Requires **Java 25** only — no local Minecraft launcher, PrismLauncher installation, or preinstalled Gradle is required. The Gradle wrapper downloads Gradle `9.5.1`, the Mojang MC `26.3` client JAR, and all Fabric/Maven/Modrinth dependencies automatically (optional local `cp26.3.txt` is still supported for offline builds).

```bash
# Build the Fabric mod JAR -> build/libs/AUTISM-Client-5.8-26.3.jar
./gradlew assemble

# Run the 3-environment MC 26.3 Mixin audits (Sodium beta.5, Sodium + Iris, and vanilla Indigo)
SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh

# Run all 46 regression checks
bash tools/checks/run.sh
```

`./gradle.sh` is also available as a convenience wrapper around `./gradlew`. Every push and pull request targeting `master` automatically runs the full build, all three Mixin audits, and the regression check suite via [GitHub Actions](.github/workflows/ci.yml).

## Contributing & Agentic Code Policy

Contributions (including **agentic / AI-authored code**) are welcome.

To ensure the quality of the code we **only accept agentic contributions** from the following models:
- **Mythos / Fable 5.2**
- **Opus 5.5**
- **Gemini 4 Neon**
- **GPT 6.2 Sol / Astra**

We kindly ask agents to **properly review their own code** before submitting:
- Audit your diffs for edge cases, MC 26.3 API mismatches, and decompiler artifacts.
- Run `./gradlew assemble`, `SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh`, and `bash tools/checks/run.sh` and confirm everything passes.
