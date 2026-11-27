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
   private static final File init_File = new File(AutismClientAddon.FOLDER, "presets");
   private static final PacketPresetManager init_bT = new PacketPresetManager();
   private static final String init_Str = "User Preset ";
   private static final Map<String, String> init_Map = init_Map();
   private final Gson init_Gson = new GsonBuilder().setPrettyPrinting().create();
   private List<PacketPresetManager.Inner_DataUtil> init_List = null;
   private List<PacketPresetManager.Inner_DataUtil> load_List = null;
   private String signature = "";

   private PacketPresetManager() {
      if (!init_File.exists()) {
         init_File.mkdirs();
      }
   }

   public static PacketPresetManager getInstance() {
      return init_bT;
   }

   public boolean savePreset(String name) {
      String sanitized = this.load_Str(name);
      if (sanitized == null) {
         AutismClientMessaging.sendPrefixed("§ePreset name cannot be empty.");
         return false;
      } else if (this.update(sanitized)) {
         AutismClientMessaging.sendPrefixed("§cThat preset name is reserved for developer templates.");
         return false;
      } else {
         this.init(this.init_AutismPacketPreset(sanitized), true);
         this.init_V();
         return true;
      }
   }

   public String init_Str() {
      String name = this.load_Str();
      this.init(this.init_AutismPacketPreset(name), true);
      this.init_V();
      return name;
   }

   public String init(Set<String> c2sPackets, Set<String> s2cPackets) {
      String name = this.load_Str();
      this.init(
         new AutismPacketPreset(
            name,
            c2sPackets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(c2sPackets),
            s2cPackets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(s2cPackets)
         ),
         true
      );
      this.init_V();
      return name;
   }

   public boolean overwritePreset(String name) {
      String sanitized = this.load_Str(name);
      if (sanitized == null) {
         AutismClientMessaging.sendPrefixed("§eSelect a user preset first.");
         return false;
      } else if (this.update(sanitized)) {
         AutismClientMessaging.sendPrefixed("§cDeveloper presets cannot be overwritten.");
         return false;
      } else {
         File file = new File(init_File, sanitized + ".json");
         if (!file.exists()) {
            AutismClientMessaging.sendPrefixed("§cUser preset not found: " + sanitized);
            return false;
         } else {
            this.init(this.init_AutismPacketPreset(sanitized), true);
            this.init_V();
            return true;
         }
      }
   }

   public boolean loadPreset(String name) {
      PacketPresetManager.Inner_DataUtil entry = this.findPreset(name);
      if (entry == null) {
         AutismClientMessaging.sendPrefixed("§cPreset not found: " + name);
         return false;
      } else {
         this.init(entry.preset());
         AutismClientMessaging.sendPrefixed("§aLoaded preset: " + entry.name());
         return true;
      }
   }

   public boolean deletePreset(String name) {
      String sanitized = this.load_Str(name);
      if (sanitized == null) {
         AutismClientMessaging.sendPrefixed("§eSelect a user preset first.");
         return false;
      } else if (this.update(sanitized)) {
         AutismClientMessaging.sendPrefixed("§cDeveloper presets cannot be deleted.");
         return false;
      } else {
         File file = new File(init_File, sanitized + ".json");
         if (!file.exists()) {
            AutismClientMessaging.sendPrefixed("§cUser preset not found: " + sanitized);
            return false;
         } else if (!file.delete()) {
            AutismClientMessaging.sendPrefixed("§cFailed to delete preset: " + sanitized);
            return false;
         } else {
            this.init_V();
            AutismClientMessaging.sendPrefixed("§aDeleted preset: " + sanitized);
            return true;
         }
      }
   }

   public boolean init(String presetName, String presetJson, String sender) {
      String sanitized = this.load_Str(presetName);
      if (sanitized != null && presetJson != null && !presetJson.isBlank()) {
         if (this.update(sanitized)) {
            sanitized = sanitized + " (LAN)";
         }

         try {
            AutismPacketPreset preset = this.init_Gson.fromJson(presetJson, AutismPacketPreset.class);
            if (preset == null) {
               return false;
            }

            preset.name = this.init_Str(sanitized);
            this.init(preset, false);
            this.init_V();
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
      String normalized = this.read_Str(name);
      if (normalized.isEmpty()) {
         return false;
      }

      for (PacketPresetManager.Inner_DataUtil entry : this.builtInPresets()) {
         if (this.read_Str(entry.name()).equals(normalized)) {
            return true;
         }
      }

      return false;
   }

   public PacketPresetManager.Inner_DataUtil findPreset(String name) {
      String normalized = this.read_Str(name);
      if (normalized.isEmpty()) {
         return null;
      }

      for (PacketPresetManager.Inner_DataUtil entry : this.builtInPresets()) {
         if (this.read_Str(entry.name()).equals(normalized)) {
            return entry;
         }
      }

      for (PacketPresetManager.Inner_DataUtil entry : this.userPresets()) {
         if (this.read_Str(entry.name()).equals(normalized)) {
            return entry;
         }
      }

      return null;
   }

   public List<PacketPresetManager.Inner_DataUtil> builtInPresets() {
      if (this.init_List != null) {
         return this.init_List;
      }

      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      List<PacketPresetManager.Inner_DataUtil> entries = new ArrayList<>();
      entries.add(
         new PacketPresetManager.Inner_DataUtil(
            this.init("Default", (Collection<Class<? extends Packet<?>>>)module.getDefaultC2sPackets(), (Collection<Class<? extends Packet<?>>>)module.getDefaultS2cPackets()), true
         )
      );
      entries.add(
         new PacketPresetManager.Inner_DataUtil(
            this.init(
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
         new PacketPresetManager.Inner_DataUtil(
            this.init(
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
         new PacketPresetManager.Inner_DataUtil(
            this.init(
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
      this.init_List = List.copyOf(entries);
      return this.init_List;
   }

   public List<PacketPresetManager.Inner_DataUtil> userPresets() {
      String signature = this.read_Str();
      if (this.load_List != null && this.signature.equals(signature)) {
         return this.load_List;
      }

      List<PacketPresetManager.Inner_DataUtil> entries = new ArrayList<>();
      File[] files = init_File.listFiles((dir, fileName) -> fileName.endsWith(".json"));
      if (files == null) {
         this.signature = signature;
         this.load_List = List.of();
         return this.load_List;
      }

      for (File file : files) {
         try (FileReader reader = new FileReader(file)) {
            AutismPacketPreset preset = this.init_Gson.fromJson(reader, AutismPacketPreset.class);
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

                  entries.add(new PacketPresetManager.Inner_DataUtil(preset, false));
               }
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.error("Failed to read preset file {}", file.getName(), e);
         }
      }

      entries.sort(Comparator.comparing(entry -> entry.name().toLowerCase(Locale.ROOT)));
      this.signature = signature;
      this.load_List = List.copyOf(entries);
      return this.load_List;
   }

   public List<String> read_List() {
      List<String> names = new ArrayList<>();

      for (PacketPresetManager.Inner_DataUtil entry : this.builtInPresets()) {
         names.add(entry.name());
      }

      for (PacketPresetManager.Inner_DataUtil entry : this.userPresets()) {
         names.add(entry.name());
      }

      return names;
   }

   private AutismPacketPreset init_AutismPacketPreset(String name) {
      AutismSessionState shared = AutismSessionState.getInstance();
      Set<String> c2s = this.init(shared.getCustomC2sPackets());
      Set<String> s2c = this.init(shared.getCustomS2cPackets());
      return new AutismPacketPreset(name, c2s, s2c);
   }

   private AutismPacketPreset init(String name, Collection<Class<? extends Packet<?>>> c2sPackets, Collection<Class<? extends Packet<?>>> s2cPackets) {
      return new AutismPacketPreset(name, this.init(c2sPackets), this.init(s2cPackets));
   }

   private AutismPacketPreset init(String name, Set<String> c2sNames, Set<String> s2cNames) {
      return new AutismPacketPreset(
         name,
         this.init(c2sNames, true).stream().map(this::init).collect(Collectors.toCollection(LinkedHashSet::new)),
         this.init(s2cNames, false).stream().map(this::init).collect(Collectors.toCollection(LinkedHashSet::new))
      );
   }

   private Set<String> init(Collection<Class<? extends Packet<?>>> packets) {
      return packets.stream()
         .map(this::init)
         .filter(packetName -> packetName != null && !packetName.isEmpty())
         .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   private String init(Class<? extends Packet<?>> packetClass) {
      String name = PacketTypeRegistry.getPacketName(packetClass);
      return name != null ? name : packetClass.getName();
   }

   private Set<Class<? extends Packet<?>>> init(Collection<String> names, boolean c2s) {
      Set<Class<? extends Packet<?>>> resolved = new LinkedHashSet<>();
      Set<Class<? extends Packet<?>>> pool = c2s ? PacketTypeRegistry.getC2sPackets() : PacketTypeRegistry.getS2cPackets();

      for (String packetName : names) {
         if (packetName != null && !packetName.isBlank()) {
            String resolvedName = this.write_Str(packetName);
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

   private void init(AutismPacketPreset preset, boolean notify) {
      String safeName = this.load_Str(preset.name);
      if (safeName == null) {
         AutismClientMessaging.sendPrefixed("§cInvalid preset name.");
      } else {
         File file = new File(init_File, safeName + ".json");

         try (FileWriter writer = new FileWriter(file)) {
            this.init_Gson.toJson(preset, writer);
            if (notify) {
               AutismClientMessaging.sendPrefixed("§aSaved preset: " + preset.name);
            }
         } catch (IOException e) {
            AutismClientAddon.LOG.error("Failed to save preset", e);
            AutismClientMessaging.sendPrefixed("§cFailed to save preset: " + e.getMessage());
         }
      }
   }

   private void init(AutismPacketPreset preset) {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      module.setCustomC2sPackets(this.init(preset.c2sPackets, true));
      module.setCustomS2cPackets(this.init(preset.s2cPackets, false));
   }

   private String load_Str() {
      int index = 1;

      while (true) {
         String candidate = "User Preset " + index;
         if (this.findPreset(candidate) == null) {
            return candidate;
         }

         index++;
      }
   }

   private String init_Str(String preferredName) {
      String baseName = this.load_Str(preferredName);
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

   private String load_Str(String name) {
      if (name == null) {
         return null;
      }

      String cleaned = name.trim().replace('\\', ' ').replace('/', ' ').replaceAll("[\\x00-\\x1f]", "").replace("..", "").replaceAll("[:*?\"<>|]", "").trim();

      while (cleaned.startsWith(".")) {
         cleaned = cleaned.substring(1).trim();
      }

      return cleaned.isEmpty() ? null : cleaned;
   }

   private String read_Str(String name) {
      return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
   }

   private String write_Str(String packetName) {
      if (packetName == null) {
         return "";
      }

      String trimmed = packetName.trim();
      String aliased = init_Map.get(trimmed);
      return aliased != null ? aliased : trimmed;
   }

   private void init_V() {
      this.load_List = null;
      this.signature = "";
   }

   private String read_Str() {
      File[] files = init_File.listFiles((dir, fileName) -> fileName.endsWith(".json"));
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

   private static Map<String, String> init_Map() {
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

   public record Inner_DataUtil(AutismPacketPreset preset, boolean init_Z) {
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
