package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.protocol.Packet;

public class PacketPresetManager {
   private static final File PRESET_DIR = new File(AutismClientAddon.FOLDER, "presets");
   private static final PacketPresetManager INSTANCE = new PacketPresetManager();
   private static final String USER_PRESET_PREFIX = "User Preset ";
   private static final Map<String, String> PACKET_NAME_ALIASES = buildAliases();
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private List<PacketPresetManager.PresetEntry> cachedBuiltInPresets = null;
   private List<PacketPresetManager.PresetEntry> cachedUserPresets = null;
   private String signature = "";

   private PacketPresetManager() {
      if (!PRESET_DIR.exists()) {
         PRESET_DIR.mkdirs();
      }
   }

   public static PacketPresetManager getInstance() {
      return INSTANCE;
   }

   public boolean savePreset(String name) {
      String sanitized = this.sanitizeName(name);
      if (sanitized == null) {
         AutismClientMessaging.sendPrefixed("§ePreset name cannot be empty.");
         return false;
      } else if (this.update(sanitized)) {
         AutismClientMessaging.sendPrefixed("§cThat preset name is reserved for developer templates.");
         return false;
      } else {
         this.writePreset(this.presetFromSession(sanitized), true);
         this.invalidateUserPresets();
         return true;
      }
   }

   public String saveCurrentAsNew() {
      String name = this.nextUserPresetName();
      this.writePreset(this.presetFromSession(name), true);
      this.invalidateUserPresets();
      return name;
   }

   public String saveNew(Set<String> c2sPackets, Set<String> s2cPackets) {
      String name = this.nextUserPresetName();
      this.writePreset(
         new AutismPacketPreset(
            name,
            c2sPackets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(c2sPackets),
            s2cPackets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(s2cPackets)
         ),
         true
      );
      this.invalidateUserPresets();
      return name;
   }

   public boolean overwritePreset(String name) {
      String sanitized = this.sanitizeName(name);
      if (sanitized == null) {
         AutismClientMessaging.sendPrefixed("§eSelect a user preset first.");
         return false;
      } else if (this.update(sanitized)) {
         AutismClientMessaging.sendPrefixed("§cDeveloper presets cannot be overwritten.");
         return false;
      } else {
         File file = new File(PRESET_DIR, sanitized + ".json");
         if (!file.exists()) {
            AutismClientMessaging.sendPrefixed("§cUser preset not found: " + sanitized);
            return false;
         } else {
            this.writePreset(this.presetFromSession(sanitized), true);
            this.invalidateUserPresets();
            return true;
         }
      }
   }

   public boolean loadPreset(String name) {
      PacketPresetManager.PresetEntry entry = this.findPreset(name);
      if (entry == null) {
         AutismClientMessaging.sendPrefixed("§cPreset not found: " + name);
         return false;
      } else {
         this.applyPreset(entry.preset());
         AutismClientMessaging.sendPrefixed("§aLoaded preset: " + entry.name());
         return true;
      }
   }

   public boolean deletePreset(String name) {
      String sanitized = this.sanitizeName(name);
      if (sanitized == null) {
         AutismClientMessaging.sendPrefixed("§eSelect a user preset first.");
         return false;
      } else if (this.update(sanitized)) {
         AutismClientMessaging.sendPrefixed("§cDeveloper presets cannot be deleted.");
         return false;
      } else {
         File file = new File(PRESET_DIR, sanitized + ".json");
         if (!file.exists()) {
            AutismClientMessaging.sendPrefixed("§cUser preset not found: " + sanitized);
            return false;
         } else if (!file.delete()) {
            AutismClientMessaging.sendPrefixed("§cFailed to delete preset: " + sanitized);
            return false;
         } else {
            this.invalidateUserPresets();
            AutismClientMessaging.sendPrefixed("§aDeleted preset: " + sanitized);
            return true;
         }
      }
   }

   public boolean importShared(String presetName, String presetJson, String sender) {
      String sanitized = this.sanitizeName(presetName);
      if (sanitized != null && presetJson != null && !presetJson.isBlank()) {
         if (this.update(sanitized)) {
            sanitized = sanitized + " (LAN)";
         }

         try {
            AutismPacketPreset preset = this.gson.fromJson(presetJson, AutismPacketPreset.class);
            if (preset == null) {
               return false;
            }

            preset.name = this.uniqueName(sanitized);
            this.writePreset(preset, false);
            this.invalidateUserPresets();
            AutismClientMessaging.sendPrefixed("§aReceived preset from " + sender + ": " + preset.name);
            return true;
         } catch (Exception e) {
            AutismClientAddon.LOG.error("Failed to import shared preset", e);
            AutismClientMessaging.sendPrefixed("§cFailed to import preset: " + e.getMessage());
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean update(String name) {
      String normalized = this.normalizeKey(name);
      if (normalized.isEmpty()) {
         return false;
      }

      for (PacketPresetManager.PresetEntry entry : this.builtInPresets()) {
         if (this.normalizeKey(entry.name()).equals(normalized)) {
            return true;
         }
      }

      return false;
   }

   public PacketPresetManager.PresetEntry findPreset(String name) {
      String normalized = this.normalizeKey(name);
      if (normalized.isEmpty()) {
         return null;
      }

      for (PacketPresetManager.PresetEntry entry : this.builtInPresets()) {
         if (this.normalizeKey(entry.name()).equals(normalized)) {
            return entry;
         }
      }

      for (PacketPresetManager.PresetEntry entry : this.userPresets()) {
         if (this.normalizeKey(entry.name()).equals(normalized)) {
            return entry;
         }
      }

      return null;
   }

   public List<PacketPresetManager.PresetEntry> builtInPresets() {
      if (this.cachedBuiltInPresets != null) {
         return this.cachedBuiltInPresets;
      }

      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      List<PacketPresetManager.PresetEntry> entries = new ArrayList<>();
      entries.add(
         new PacketPresetManager.PresetEntry(
            this.presetFromClasses("Default", (Collection<Class<? extends Packet<?>>>)module.getDefaultC2sPackets(), (Collection<Class<? extends Packet<?>>>)module.getDefaultS2cPackets()), true
         )
      );
      entries.add(
         new PacketPresetManager.PresetEntry(
            this.presetFromNames(
               "Movement & World",
               Set.of(
                  "ServerboundPunchPacket",
                  "ServerboundAttackPacket",
                  "ServerboundPlayerActionPacket",
                  "ServerboundUseItemOnPacket",
                  "ServerboundInteractPacket",
                  "ServerboundUseItemPacket",
                  "ServerboundMovePlayerPacket.Pos",
                  "ServerboundMovePlayerPacket.PosRot",
                  "ServerboundMovePlayerPacket.Rot",
                  "ServerboundMovePlayerPacket.StatusOnly",
                  "ServerboundAcceptTeleportationPacket",
                  "ServerboundMoveVehiclePacket"
               ),
               Set.of(
                  "ClientboundBlockEventPacket",
                  "ClientboundBlockUpdatePacket",
                  "ClientboundEntityPositionSyncPacket",
                  "ClientboundSetEntityMotionPacket",
                  "ClientboundPlayerPositionPacket",
                  "ClientboundPlayerRotationPacket",
                  "ClientboundMoveVehiclePacket",
                  "ClientboundLevelEventPacket"
               )
            ),
            true
         )
      );
      entries.add(
         new PacketPresetManager.PresetEntry(
            this.presetFromNames(
               "Inventory, Chat & Commands",
               Set.of(
                  "ServerboundContainerButtonClickPacket",
                  "ServerboundChatPacket",
                  "ServerboundChatCommandPacket",
                  "ServerboundPlaceRecipePacket",
                  "ServerboundContainerClickPacket",
                  "ServerboundSetCreativeModeSlotPacket",
                  "ServerboundPickItemFromBlockPacket",
                  "ServerboundPickItemFromEntityPacket",
                  "ServerboundPlayerActionPacket",
                  "ServerboundRecipeBookChangeSettingsPacket",
                  "ServerboundRecipeBookSeenRecipePacket",
                  "ServerboundRenameItemPacket",
                  "ServerboundCommandSuggestionPacket",
                  "ServerboundChatCommandSignedPacket",
                  "ServerboundSignUpdatePacket"
               ),
               Set.of(
                  "ClientboundPlayerChatPacket",
                  "ClientboundContainerClosePacket",
                  "ClientboundCommandSuggestionsPacket",
                  "ClientboundCommandsPacket",
                  "ClientboundSystemChatPacket",
                  "ClientboundContainerSetContentPacket",
                  "ClientboundMountScreenOpenPacket",
                  "ClientboundOpenScreenPacket",
                  "ClientboundContainerSetDataPacket",
                  "ClientboundContainerSetSlotPacket",
                  "ClientboundSetCursorItemPacket",
                  "ClientboundMerchantOffersPacket"
               )
            ),
            true
         )
      );
      entries.add(
         new PacketPresetManager.PresetEntry(
            this.presetFromNames(
               "Chunk Freeze & Blocks",
               Set.of(
                  "ServerboundChunkBatchReceivedPacket",
                  "ServerboundClientCommandPacket",
                  "ServerboundPunchPacket",
                  "ServerboundPlayerActionPacket",
                  "ServerboundPlayerInputPacket",
                  "ServerboundUseItemOnPacket",
                  "ServerboundUseItemPacket",
                  "ServerboundMovePlayerPacket.PosRot"
               ),
               Set.of(
                  "ClientboundBlockEventPacket",
                  "ClientboundBlockUpdatePacket",
                  "ClientboundLevelChunkWithLightPacket",
                  "ClientboundSectionBlocksUpdatePacket",
                  "ClientboundSetChunkCacheRadiusPacket",
                  "ClientboundSetChunkCacheCenterPacket",
                  "ClientboundGameEventPacket",
                  "ClientboundLightUpdatePacket",
                  "ClientboundChunkBatchStartPacket",
                  "ClientboundForgetLevelChunkPacket",
                  "ClientboundLevelEventPacket"
               )
            ),
            true
         )
      );
      this.cachedBuiltInPresets = List.copyOf(entries);
      return this.cachedBuiltInPresets;
   }

   public List<PacketPresetManager.PresetEntry> userPresets() {
      String signature = this.directorySignature();
      if (this.cachedUserPresets != null && this.signature.equals(signature)) {
         return this.cachedUserPresets;
      }

      List<PacketPresetManager.PresetEntry> entries = new ArrayList<>();
      File[] files = PRESET_DIR.listFiles((dir, fileName) -> fileName.endsWith(".json"));
      if (files == null) {
         this.signature = signature;
         this.cachedUserPresets = List.of();
         return this.cachedUserPresets;
      }

      for (File file : files) {
         try (FileReader reader = new FileReader(file)) {
            AutismPacketPreset preset = this.gson.fromJson(reader, AutismPacketPreset.class);
            if (preset != null) {
               if (preset.name == null || preset.name.isBlank()) {
                  preset.name = file.getName().replace(".json", "");
               }

               if (this.update(preset.name)) {
                  AutismClientAddon.LOG.warn("Ignoring user preset with reserved built-in name: {}", preset.name);
               } else {
                  if (preset.c2sPackets == null) {
                     preset.c2sPackets = new LinkedHashSet<>();
                  }

                  if (preset.s2cPackets == null) {
                     preset.s2cPackets = new LinkedHashSet<>();
                  }

                  entries.add(new PacketPresetManager.PresetEntry(preset, false));
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.error("Failed to read preset file {}", file.getName(), e);
         }
      }

      entries.sort(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)));
      this.signature = signature;
      this.cachedUserPresets = List.copyOf(entries);
      return this.cachedUserPresets;
   }

   public List<String> presetNames() {
      List<String> names = new ArrayList<>();

      for (PacketPresetManager.PresetEntry entry : this.builtInPresets()) {
         names.add(entry.name());
      }

      for (PacketPresetManager.PresetEntry entry : this.userPresets()) {
         names.add(entry.name());
      }

      return names;
   }

   private AutismPacketPreset presetFromSession(String name) {
      AutismSessionState shared = AutismSessionState.getInstance();
      Set<String> c2s = this.packetNames(shared.getCustomC2sPackets());
      Set<String> s2c = this.packetNames(shared.getCustomS2cPackets());
      return new AutismPacketPreset(name, c2s, s2c);
   }

   private AutismPacketPreset presetFromClasses(String name, Collection<Class<? extends Packet<?>>> c2sPackets, Collection<Class<? extends Packet<?>>> s2cPackets) {
      return new AutismPacketPreset(name, this.packetNames(c2sPackets), this.packetNames(s2cPackets));
   }

   private AutismPacketPreset presetFromNames(String name, Set<String> c2sNames, Set<String> s2cNames) {
      return new AutismPacketPreset(
         name,
         this.resolvePacketClasses(c2sNames, true).stream().map(this::packetName).collect(Collectors.toCollection(LinkedHashSet::new)),
         this.resolvePacketClasses(s2cNames, false).stream().map(this::packetName).collect(Collectors.toCollection(LinkedHashSet::new))
      );
   }

   private Set<String> packetNames(Collection<Class<? extends Packet<?>>> packets) {
      return packets.stream()
         .map(this::packetName)
         .filter(packetName -> packetName != null && !packetName.isEmpty())
         .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   private String packetName(Class<? extends Packet<?>> packetClass) {
      String name = PacketTypeRegistry.getPacketName(packetClass);
      return name != null ? name : packetClass.getName();
   }

   private Set<Class<? extends Packet<?>>> resolvePacketClasses(Collection<String> names, boolean c2s) {
      Set<Class<? extends Packet<?>>> resolved = new LinkedHashSet<>();
      Set<Class<? extends Packet<?>>> pool = c2s ? PacketTypeRegistry.getC2sPackets() : PacketTypeRegistry.getS2cPackets();

      for (String packetName : names) {
         if (packetName != null && !packetName.isBlank()) {
            String resolvedName = this.resolvePacketAlias(packetName);
            Class<? extends Packet<?>> packetClass = PacketTypeRegistry.resolvePacketClass(resolvedName);
            if (packetClass == null) {
               packetClass = PacketTypeRegistry.resolvePacketClass(PacketNameResolver.friendlyName(resolvedName));
            }

            if (packetClass != null && pool.contains(packetClass)) {
               resolved.add(packetClass);
            } else {
               AutismClientAddon.LOG.warn("Unknown packet name in preset: {}", packetName);
            }
         }
      }

      return resolved;
   }

   private void writePreset(AutismPacketPreset preset, boolean notify) {
      String safeName = this.sanitizeName(preset.name);
      if (safeName == null) {
         AutismClientMessaging.sendPrefixed("§cInvalid preset name.");
      } else {
         File file = new File(PRESET_DIR, safeName + ".json");

         try (FileWriter writer = new FileWriter(file)) {
            this.gson.toJson(preset, writer);
            if (notify) {
               AutismClientMessaging.sendPrefixed("§aSaved preset: " + preset.name);
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.error("Failed to save preset", e);
            AutismClientMessaging.sendPrefixed("§cFailed to save preset: " + e.getMessage());
         }
      }
   }

   private void applyPreset(AutismPacketPreset preset) {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.setCustomC2sPackets(this.resolvePacketClasses(preset.c2sPackets, true));
      module.setCustomS2cPackets(this.resolvePacketClasses(preset.s2cPackets, false));
   }

   private String nextUserPresetName() {
      int index = 1;

      while (true) {
         String candidate = "User Preset " + index;
         if (this.findPreset(candidate) == null) {
            return candidate;
         }

         index++;
      }
   }

   private String uniqueName(String preferredName) {
      String baseName = this.sanitizeName(preferredName);
      if (baseName == null) {
         baseName = "User Preset LAN";
      }

      String candidate = baseName;
      int suffix = 1;

      while (this.findPreset(candidate) != null || this.update(candidate)) {
         candidate = baseName + " (" + suffix++ + ")";
      }

      return candidate;
   }

   private String sanitizeName(String name) {
      if (name == null) {
         return null;
      }

      String cleaned = name.trim().replace('\\', ' ').replace('/', ' ').replaceAll("[\\x00-\\x1f]", "").replace("..", "").replaceAll("[:*?\"<>|]", "").trim();

      while (cleaned.startsWith(".")) {
         cleaned = cleaned.substring(1).trim();
      }

      return cleaned.isEmpty() ? null : cleaned;
   }

   private String normalizeKey(String name) {
      return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
   }

   private String resolvePacketAlias(String packetName) {
      if (packetName == null) {
         return "";
      }

      String trimmed = packetName.trim();
      String aliased = PACKET_NAME_ALIASES.get(trimmed);
      return aliased != null ? aliased : trimmed;
   }

   private void invalidateUserPresets() {
      this.cachedUserPresets = null;
      this.signature = "";
   }

   private String directorySignature() {
      File[] files = PRESET_DIR.listFiles((dir, fileName) -> fileName.endsWith(".json"));
      if (files != null && files.length != 0) {
         List<File> orderedFiles = new ArrayList<>(List.of(files));
         orderedFiles.sort(Comparator.comparing(File::getName));
         StringBuilder signature = new StringBuilder();

         for (File file : orderedFiles) {
            signature.append(file.getName()).append(':').append(file.lastModified()).append(':').append(file.length()).append(';');
         }

         return signature.toString();
      } else {
         return "empty";
      }
   }

   private static Map<String, String> buildAliases() {
      return Map.ofEntries(
         Map.entry("HandSwingC2SPacket", "ServerboundPunchPacket"),
         Map.entry("CompatSwingPacket", "ServerboundPunchPacket"),
         Map.entry("PlayerMoveC2SPacket", "ServerboundMovePlayerPacket.PosRot"),
         Map.entry("PlayerMoveC2SPacket.Full", "ServerboundMovePlayerPacket.PosRot"),
         Map.entry("PlayerMoveC2SPacket.LookAndOnGround", "ServerboundMovePlayerPacket.Rot"),
         Map.entry("PlayerMoveC2SPacket.OnGroundOnly", "ServerboundMovePlayerPacket.StatusOnly"),
         Map.entry("PlayerMoveC2SPacket.PositionAndOnGround", "ServerboundMovePlayerPacket.Pos"),
         Map.entry("TeleportConfirmC2SPacket", "ServerboundAcceptTeleportationPacket"),
         Map.entry("VehicleMoveC2SPacket", "ServerboundMoveVehiclePacket"),
         Map.entry("AcknowledgeChunksC2SPacket", "ServerboundChunkBatchReceivedPacket"),
         Map.entry("PlayerInputC2SPacket", "ServerboundPlayerInputPacket"),
         Map.entry("ChatMessageC2SPacket", "ServerboundChatPacket"),
         Map.entry("CommandExecutionC2SPacket", "ServerboundChatCommandPacket"),
         Map.entry("PickItemFromBlockC2SPacket", "ServerboundPickItemFromBlockPacket"),
         Map.entry("PickItemFromEntityC2SPacket", "ServerboundPickItemFromEntityPacket"),
         Map.entry("RecipeBookDataC2SPacket", "ServerboundRecipeBookChangeSettingsPacket"),
         Map.entry("RenameItemC2SPacket", "ServerboundRenameItemPacket"),
         Map.entry("RequestCommandCompletionsC2SPacket", "ServerboundCommandSuggestionPacket"),
         Map.entry("BlockEventS2CPacket", "ClientboundBlockEventPacket"),
         Map.entry("BlockUpdateS2CPacket", "ClientboundBlockUpdatePacket"),
         Map.entry("PlayerRotationS2CPacket", "ClientboundPlayerRotationPacket"),
         Map.entry("VehicleMoveS2CPacket", "ClientboundMoveVehiclePacket"),
         Map.entry("WorldEventS2CPacket", "ClientboundLevelEventPacket"),
         Map.entry("ChatMessageS2CPacket", "ClientboundPlayerChatPacket"),
         Map.entry("CloseScreenS2CPacket", "ClientboundContainerClosePacket"),
         Map.entry("CommandTreeS2CPacket", "ClientboundCommandsPacket"),
         Map.entry("GameMessageS2CPacket", "ClientboundSystemChatPacket"),
         Map.entry("OpenMountScreenS2CPacket", "ClientboundMountScreenOpenPacket"),
         Map.entry("AbstractContainerMenuPropertyUpdateS2CPacket", "ClientboundContainerSetDataPacket"),
         Map.entry("SetCursorItemS2CPacket", "ClientboundSetCursorItemPacket"),
         Map.entry("SetClientboundContainerSetContentPacket", "ClientboundContainerSetContentPacket"),
         Map.entry("SetTradeOffersS2CPacket", "ClientboundMerchantOffersPacket"),
         Map.entry("ChunkDeltaUpdateS2CPacket", "ClientboundSectionBlocksUpdatePacket"),
         Map.entry("ChunkLoadDistanceS2CPacket", "ClientboundSetChunkCacheRadiusPacket"),
         Map.entry("ChunkRenderDistanceCenterS2CPacket", "ClientboundSetChunkCacheCenterPacket"),
         Map.entry("ChunkSentS2CPacket", "ClientboundLevelChunkWithLightPacket"),
         Map.entry("LightUpdateS2CPacket", "ClientboundLightUpdatePacket"),
         Map.entry("StartChunkSendS2CPacket", "ClientboundChunkBatchStartPacket"),
         Map.entry("UnloadChunkS2CPacket", "ClientboundForgetLevelChunkPacket")
      );
   }

   public record PresetEntry(AutismPacketPreset preset, boolean init_Z) {
      public String name() {
         return this.preset != null && this.preset.name != null ? this.preset.name : "";
      }

      public int c2sCount() {
         return this.preset != null && this.preset.c2sPackets != null ? this.preset.c2sPackets.size() : 0;
      }

      public int s2cCount() {
         return this.preset != null && this.preset.s2cPackets != null ? this.preset.s2cPackets.size() : 0;
      }

      public boolean init_Z() {
         return !this.init_Z;
      }

      public boolean isBuiltIn() {
         return this.init_Z;
      }
   }
}
