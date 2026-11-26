# AutismProtector Privacy & Compat Layer (`autismclient.g`, `autismclient.mixin.security`)

AUTISM Client's **AutismProtector** subsystem spans `33` files (`8,651` LOC) in `autismclient.g` (planned target package: `autismclient.protector`) and `34` Mixins (`1,885` LOC) in `autismclient.mixin.security`. Phase 3B renamed all `33/33` classes in `autismclient.g` in place (`0/33` obfuscated remaining).

## Architecture Overview

AutismProtector isolates client fingerprinting surfaces and hardens server-supplied packets/resources per network `Connection`:

1. **Per-Connection Lifecycle & Feature Flags** — `ConnectionPrivacyRegistry` binds each `Connection` / `PacketListener` / `ServerPackManager` / `ServerData.Type` to a privacy mode (`VANILLA` vs `MODDED`) and endpoint lifecycle. `ProtectorFeatureFlags` compiles `AutismConfig` toggles into a bitmask and detects overlapping external privacy mods (`opsec`, `exploitpreventer`).
2. **Brand, Channels & Netty Outbound Filtering** — `OutboundPacketFilterHandler` (a Netty `ChannelOutboundHandlerAdapter`) enforces `OutboundChannelPolicy` verdicts (`PASS` / `DROP` / `REPLACE`) on `ServerboundCustomPayloadPacket` and `ServerboundCustomQueryAnswerPacket`, delegating modded-mode channel filtering to `CustomPayloadFilter` + `ModNamespaceCatalog` and synthesizing late `minecraft:register` frames via `LateRegisterSynthesizer`.
3. **Resource-Pack & HTTP Isolation** — `ResourcePackSessionTracker`, `ResourcePackDownloadScope`, `IsolatedPackDownloader`, `IsolatedResourcePack`, `ScopedHttpUrlConnection`, `PackUrlValidator`, `ResourcePackPushValidator`, `ServerPackSelectionRegistry`, `PackFailureGuard`, and `VanillaKnownPacksManager` enforce HTTP/HTTPS URL/IP/proxy validation, isolated bypass reloads, and post-failure pack suppression.
4. **Translation, Keybind & Component Origin Isolation** — `TranslationSnapshot` (+ `TranslationSnapshotDuck`), `PackTranslationPolicyResolver`, `VanillaTranslationKeyLoader`, `PacketOriginDuck`, `PacketOriginComponentCodec`, `PacketComponentOriginTracker`, and `PacketDecodeOriginStack` prevent remote packets/server packs from probing installed mod translation keys or mod keybinds.
5. **Payload, NBT, Particle & Registry Guards** — `ComponentSafetyValidator`, `TooltipNbtGuard`, `MovementBoundsGuard`, `ModdedParticleGuard`, `RegistryCompatGuard`, and `TimelineTagCompatFix` protect the client against oversized components/tooltips, NaN/extreme coordinates, unknown modded particles, and missing server registry/timeline data.

## Protector Classes (`autismclient.g`, `33` Classes)

### Connection, Mode & Channel Isolation (`10`)
| Class | LOC | Role |
|---|---:|---|
| `ConnectionPrivacyRegistry` | 574 | Bounded (`1024`) per-connection privacy registry binding `Connection`, `PacketListener`, mode (`VANILLA`/`MODDED`), endpoint, and `ServerPackManager` |
| `ProtectorFeatureFlags` | 222 | Compiles `AutismConfig` protector flags into a bitmask, detects `opsec`/`exploitpreventer` conflicts, tracks exempted packets |
| `ProtectorModeDescriptions` | 22 | UI mode labels and help descriptions (`Vanilla`/`Modded`, `Packs: Normal`/`Packs: Bypass`) |
| `OutboundChannelPolicy` | 201 | Evaluates per-connection `PASS`/`DROP`/`REPLACE` verdicts for outbound custom payload & login query answer packets |
| `OutboundPacketFilterHandler` | 298 | Netty `ChannelOutboundHandlerAdapter` and channel send helpers enforcing `OutboundChannelPolicy` |
| `CustomPayloadFilter` | 210 | Filters `minecraft:register`/`unregister`/`mco` and custom channels against `ModNamespaceCatalog`, rewriting `RegistrationPayload` |
| `LateRegisterSynthesizer` | 271 | Synthesizes a late `minecraft:register` payload (`fabric-menu-api-v1`, `voicechat`) in Modded mode if no register arrived within 5 ticks |
| `ModNamespaceCatalog` | 333 | Bootstraps installed mod IDs & Fabric API namespaces; maintains allow/block caches for channels, translation keys, and keybinds |
| `CallerModInspector` | 188 | Walks call stacks, `ProtectionDomain` JARs/`fabric.mod.json`, and FabricLoader dependency graphs to attribute mod ownership |
| `VanillaKnownPacksManager` | 56 | `KnownPacksManager` subclass restricting known-pack negotiation to `ServerPacksSource.createVanillaTrustedRepository()` |

### Resource-Pack & HTTP Download Isolation (`9`)
| Class | LOC | Role |
|---|---:|---|
| `ResourcePackSessionTracker` | 1,062 | Tracks per-connection pack sessions, generations, isolation policies, wait futures, and `autism-pack-response-drain` feedback |
| `ResourcePackDownloadScope` | 656 | Enforces connection-scoped download endpoints, integrated-server guards, and per-manager download cancellation |
| `IsolatedPackDownloader` | 436 | Wraps `PackDownloader`, `PackLoadFeedback`, and `FileResourcesSupplier`; refuses unscoped downloads |
| `IsolatedResourcePack` | 254 | `CompatAbstractPackResources` snapshot exposing only `pack.mcmeta` + `assets/*/lang/*` within strict zip size/entry caps |
| `ScopedHttpUrlConnection` | 1,377 | `HttpURLConnection` implementation enforcing connection-scoped proxy, DNS, socket, and TLS/SNI controls for pack downloads |
| `PackUrlValidator` | 539 | Strict HTTP/HTTPS URI, ASCII/IDN host, IPv4/IPv6 literal, non-local/unicast address, and proxy validator |
| `ResourcePackPushValidator` | 89 | Validates `ClientboundResourcePackPushPacket` schemes (`http`/`https`), auto-deny, and bypass compatibility verdicts |
| `ServerPackSelectionRegistry` | 310 | Tracks `server/<hex>/<uuid>` pack selection provenance per active `ResourcePackSessionTracker` session |
| `PackFailureGuard` | 20 | 15-second post-failure suppression timer checked by `AutismProtectorDownloadedPackSourceMixin` (`createRepositorySource`) and cleared on config/leave (`AutismClientMod`) |

### Translation, Keybind & Component Origin Isolation (`8`)
| Class | LOC | Role |
|---|---:|---|
| `TranslationSnapshot` | 199 | Immutable per-`Language` translation snapshot and nested thread-local builder tracking `VANILLA` / `MOD` / `OTHER` origins |
| `TranslationSnapshotDuck` | 7 | Mixin duck interface on `ClientLanguage` (`autism$setTranslationSnapshot` / `autism$getTranslationSnapshot`) |
| `PackTranslationPolicyResolver` | 335 | Captures `ResourceManager` + connection/policy identity scopes to gate non-vanilla translation pack access |
| `VanillaTranslationKeyLoader` | 80 | Background daemon (`AutismProtector-Vanilla-Keys`) indexing vanilla keys from `/assets/minecraft/lang/en_us.json` |
| `PacketOriginDuck` | 20 | Mixin duck interface on `ComponentContents` (`autism$setFromPacket`, `autism$packetOrigin`, `autism$isFromPacket`, `autism$setSilent`) |
| `PacketOriginComponentCodec` | 26 | `Codec<Component>` wrapper record stamping decoded components with active packet-origin metadata |
| `PacketComponentOriginTracker` | 276 | Graph traversal stamping, propagating, and querying packet-origin provenance across `Component` trees and hover events |
| `PacketDecodeOriginStack` | 54 | Thread-local stack tracking connection origin while decoding custom payload, block entity, and chunk packets |

### Safety, Particle & Registry Compatibility Guards (`6`)
| Class | LOC | Role |
|---|---:|---|
| `ComponentSafetyValidator` | 133 | Enforces recursion depth (`≤ 64`) and character budgets (`≤ 65,536` total, `≤ 16,384` per string) on `Component` and `NumberFormat` |
| `TooltipNbtGuard` | 63 | Clamps tooltips (`≤ 512` lines), replaces unsafe component lines, and measures serialized `ItemStack` NBT size |
| `MovementBoundsGuard` | 32 | Rejects NaN/Infinite/`> 1e9` positions and `> 1e4` velocities in `Vec3` and `PositionMoveRotation` |
| `ModdedParticleGuard` | 105 | Substitutes a no-op sentinel for unknown modded particle IDs and blocks non-vanilla `ParticleProvider`s in Vanilla mode |
| `RegistryCompatGuard` | 142 | Thread-local configuration scope omitting missing `Registries.TRIM_MATERIAL` / delayed item component tags & elements |
| `TimelineTagCompatFix` | 61 | Synthesizes default `Registries.TIMELINE` tags (`universal`, `in_overworld`, `in_nether`, `in_end`) when servers omit them |

## Security & Privacy Mixins (`autismclient.mixin.security`, `34` Mixins)

- **Connection, Handshake & Networking (`11`):** `AutismProtectorConnectScreenMixin`, `AutismProtectorConnectionTrackingMixin`, `AutismProtectorClientHandshakePacketListenerMixin`, `AutismProtectorClientCommonNetworkHandlerMixin`, `AutismProtectorPacketDecoderMixin`, `AutismProtectorPacketProcessorMixin`, `AutismFabricChanneledNetworkAddonMixin`, `AutismFabricClientLoginNetworkAddonMixin`, `AutismFabricPayloadCodecMixin`, `AutismProtectorPayloadTypeRegistryImplMixin`, `AuditFabricPayloadGateMixin`.
- **Resource Packs & HTTP (`5`):** `AutismProtectorDownloadedPackSourceMixin`, `AutismProtectorDownloadQueueMixin`, `AutismProtectorHttpUtilMixin`, `AutismProtectorResourceLoaderImplMixin`, `AutismProtectorServerPackManagerMixin`.
- **Translations, Keybinds & Components (`11`):** `AutismProtectorClientLanguageMixin`, `AutismProtectorDeprecatedTranslationsInfoMixin`, `AutismProtectorTranslatableContentsMixin`, `AutismProtectorKeybindContentsMixin`, `AutismProtectorKeyMappingRegistryImplMixin`, `AutismProtectorOptionsMixin`, `AutismProtectorComponentSerializationMixin`, `AutismComponentUtilsMixin`, `AutismEmptyPlainTextContentsMixin`, `AutismPacketContentsMixin`, `AutismResolvedContentsMixin`.
- **Chat, Telemetry, Particles & Registry Compat (`7`):** `AutismProtectorChatSigningMixin`, `AutismProtectorYggdrasilMixin`, `AutismProtectorParticleEngineMixin`, `AutismClientConfigurationRegistryMixin`, `AutismRegistryDataCollectorMixin`, `AutismItemPropertiesMixin`, `AutismDataComponentInitializerMixin`.
