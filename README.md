# AUTISM Client 5.8 (Minecraft 26.3)

A FOSS fork of **AUTISM Client** for **Minecraft 26.3** (Fabric, Java 25).

This is still a work in progress. Right now the codebase is a ~750k-line consolidation of my own patched version of 5.3, a version of 5.3 made for 26.3, the 5.7.11 updates (including the AI/parkour stack), Aggressive PvP addon, and the further MC 26.3 fixes in one tree. (small note: upstream itself is ~750k lines, which fun fact; is 120k lines more than Minecraft Java)

The goal, obviously, is to end up with something much shorter, cleaner and actually manageable by cutting down vibecoded slop like the countless monolithic classes of over 9k lines of code ( ~20k lines for Scaffolding btw, and it didnt even work before I fixed it), removing unrelated extras that belong as their own mods (like the matchmaking system), and replacing subsystems such as swapping `multi` out for an [Azalea](https://github.com/azalea-rs/azalea)-based Rust network protocol.


## Branches

- `master`: **AUTISM Client 5.8** (Minecraft 26.3, release branch)
- `dev`: **AUTISM Client 5.8** (Minecraft 26.3, active development)
- `5.3-patched`: Legacy **AUTISM Client 5.3** (Minecraft 26.3 patched)

## Building & Verifying Locally

Install **Java 25**. The Gradle wrapper downloads the verified Minecraft `26.3` client and build dependencies. GitHub Actions runs compilation and the regression checks on pushes and pull requests targeting `master` or `dev`.

This branch requires Fabric Loader **0.19.5** or later and Fabric API **0.165.0+26.3** or later. Compilation and the Mixin audits use these minimum versions.

```bash
# Compile and run the regression checks
./gradlew compileJava
bash tools/checks/run.sh
```

Packaging requires your local `precompiled/integrity-classes.jar` and `stubs/integrity-stubs.jar`. The packaged Fabric mod is written to `build/libs/AUTISM-Client-5.8-26.3.jar`.

```bash
./gradlew assemble
# Run the MC 26.3 Mixin audits (Sodium, Sodium+Iris, and vanilla Indigo)
SKIP_JAR_CHECK=1 bash tools/verify-26.3.sh
```

## Documentation

Architecture notes, ABI guardrails, package maps, and deobfuscation progress live under `docs/`:

- [Architecture & Build Overview](docs/ARCHITECTURE.md)
- [ABI Boundaries & Silent-Breakage Hazards](docs/ABI_AND_HAZARDS.md)
- [59-Package Directory](docs/PACKAGES.md)
- [Deobfuscation Ledger](docs/DEOBFUSCATION_LEDGER.tsv)
- Subsystem Guides: [Commands](docs/subsystems/COMMANDS.md) · [Modules](docs/subsystems/MODULES.md) · [Macros](docs/subsystems/MACROS.md) · [GUI & Screens](docs/subsystems/GUI.md) · [AutismProtector](docs/subsystems/PROTECTOR.md)

## Contributing & Agentic Code Policy

Contributions (including **agentic / AI-authored code**) are welcome.

To ensure the quality of the code we **only accept agentic contributions** from the following models:
- **Mythos / Fable 5.2**
- **Opus 5.5**
- **Gemini 4 Neon**
- **GPT 6.2 Sol / Astra**

AI agents must **review and verify their own work** before submitting.

- Read the full diff and trace changes through the normal game, mod, or build entry points. Check edge cases, failure paths, MC 26.3 API compatibility, and decompiler artifacts.
- Exercise the changed behavior through those entry points. Run the regression checks, local build, and Mixin audits above. Fix failures introduced by your changes, then rerun the affected checks against the final diff.
- Check the final result against every requested deliverable and finish any remaining work within scope.
- In the pull request, list the checks you ran and their results. Identify any unverified behavior or checks and explain what blocked verification.
- Do not modify or bypass integrity/authorization files.
