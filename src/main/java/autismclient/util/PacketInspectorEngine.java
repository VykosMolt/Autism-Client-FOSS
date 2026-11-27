package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatSwingPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PacketInspectorEngine {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Object init_Obj = new Object();
   private static final Pattern init_Pattern = Pattern.compile("(?<![\\w$.])(class_\\d+)(?=[\\[{(:\\s]|$)");
   private static final Pattern load_Pattern = Pattern.compile("(net\\.minecraft(?:\\.[\\w$]+)*\\.class_\\d+)(?=[\\[{(:\\s]|$)");
   private static final Pattern read_Pattern = Pattern.compile("(?i)\\b[a-z0-9_.-]+:[a-z0-9_./-]+\\b");
   private static final Pattern write_Pattern = Pattern.compile("[\\x20-\\x7E]{4,}");
   private static final Pattern update_Pattern = Pattern.compile("(?<![A-Za-z0-9+/=])(?:[A-Za-z0-9+/]{16,}={0,2})(?![A-Za-z0-9+/=])");
   private static final Pattern sync_Pattern = Pattern.compile("(?i)(?<![0-9a-f])(?:0x)?[0-9a-f]{16,}(?![0-9a-f])");
   private static final Pattern flush_Pattern = Pattern.compile("(?i)\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b");
   private static final Pattern close_Pattern = Pattern.compile(
      "(?i)\\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}(?::\\d{1,5})?\\b|\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d{1,5})?\\b"
   );
   private static final int init_I = 7;
   private static final int load_I = 900;
   private static final int read_I = 64;
   private static final int write_I = 64;
   private static final int update_I = 320;
   private static final int sync_I = 8;
   private static final int flush_I = 67108864;
   private static final int close_I = 48;
   private static final Set<String> init_Set = Set.of(
      "getClass",
      "getPacketType",
      "toString",
      "hashCode",
      "copy",
      "apply",
      "write",
      "read",
      "streamCodec",
      "codec",
      "isWritingErrorSkippable",
      "writingErrorSkippable",
      "WritingErrorSkippable"
   );
   private static final Set<String> load_Set = Set.of(
      "getclass",
      "getpackettype",
      "tostring",
      "hashcode",
      "copy",
      "apply",
      "write",
      "read",
      "streamcodec",
      "codec",
      "iswritingerrorskippable",
      "writingerrorskippable"
   );
   private static final Set<String> read_Set = Set.of(
      "id",
      "syncId",
      "revision",
      "slot",
      "contents",
      "cursorStack",
      "trackedValues",
      "values",
      "reason",
      "value",
      "hand",
      "sequence",
      "yaw",
      "pitch",
      "chunkX",
      "chunkZ",
      "chunkData",
      "lightData",
      "sound",
      "category",
      "x",
      "y",
      "z",
      "onGround",
      "name",
      "screenHandlerType",
      "type",
      "state",
      "mode",
      "seed"
   );

   private PacketInspectorEngine() {
   }

   public static PacketInspectorEngine.InspectionReport init_bu_JsonUtil_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return init_bu_JsonUtil_AutismPacketLogger(entry, null, true);
   }

   public static PacketInspectorEngine.InspectionReport init_bu_JsonUtil_AutismPacketLogger_bu_SyncManager(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      return init_bu_JsonUtil_AutismPacketLogger(entry, payloadAnalysis, true);
   }

   public static PacketInspectorEngine.InspectionReport init_bu_JsonUtil_AutismPacketLogger(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis, boolean includePayloadAnalyzer
   ) {
      try {
         return load_bu_JsonUtil_AutismPacketLogger(entry, payloadAnalysis, includePayloadAnalyzer);
      } catch (Throwable t) {
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder(entry.load_Str + " [" + entry.init_Str + "]");
         builder.init("Meta", UiColorPalette.warmWhite());
         builder.load("Name: " + entry.load_Str, UiColorPalette.softWhite());
         builder.load("Direction: " + entry.init_Str, init_I_Str(entry.init_Str));
         builder.load("Class: " + entry.init_Class.getName(), UiColorPalette.textBody());
         builder.load("Tick: " + entry.init_I, UiColorPalette.textBody());
         builder.load("Time: " + Instant.ofEpochMilli(entry.load_J), UiColorPalette.textBody());
         builder.init_V();
         builder.init("Inspect Error", UiColorPalette.errorText());
         builder.load("The detailed inspector failed on this packet, so this fallback view was opened instead.", UiColorPalette.errorText());
         builder.load("Reason: " + t.getClass().getSimpleName() + (t.getMessage() == null ? "" : " - " + poll(t.getMessage())), UiColorPalette.errorText());
         if (entry.init_Packet != null) {
            builder.init_V();
            builder.init("Fallback", UiColorPalette.warningYellow());
            builder.load("Packet Type: " + load_Str_Obj(load_Obj_Obj_Str(entry.init_Packet, "getPacketType")), UiColorPalette.textBody());
            builder.load("toString(): " + poll(String.valueOf(entry.init_Packet)), UiColorPalette.textMuted());
         }

         return builder.init_bu_JsonUtil();
      }
   }

   public static PacketInspectorEngine.InspectionReport load_bu_JsonUtil_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return load_bu_JsonUtil_AutismPacketLogger(entry, null, true);
   }

   public static PacketInspectorEngine.InspectionReport load_bu_JsonUtil_AutismPacketLogger_bu_SyncManager(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      return load_bu_JsonUtil_AutismPacketLogger(entry, payloadAnalysis, true);
   }

   public static PacketInspectorEngine.InspectionReport load_bu_JsonUtil_AutismPacketLogger(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis, boolean includePayloadAnalyzer
   ) {
      String title = entry.load_Str + " [" + entry.init_Str + "]";
      PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder(title);
      AutismPayloadSupport.PayloadEntry payloadSnapshot = entry == null ? null : AutismPayloadSupport.resolveEntryPayload(entry);
      builder.init("Meta", UiColorPalette.warmWhite());
      builder.load("Name: " + entry.load_Str, UiColorPalette.softWhite());
      builder.load("Direction: " + entry.init_Str, init_I_Str(entry.init_Str));
      builder.load("Class: " + entry.init_Class.getName(), UiColorPalette.textBody());
      if (payloadSnapshot != null) {
         builder.load("Payload Channel: " + payloadSnapshot.channel(), UiColorPalette.successText());
         builder.load("Payload Size: " + payloadSnapshot.byteLength() + " bytes", UiColorPalette.textBright());
         PayloadChannelFilterManager.Inner_StreamUtil filter = init_bB_StreamUtil_AutismPayloadSuppo(payloadSnapshot);
         if (filter != null) {
            builder.load("Matched Filter: " + filter.load() + " (" + filter.pattern() + ")", UiColorPalette.mint());
         }

         String source = read_Str_Str(payloadSnapshot.channel());
         if (source != null && !source.isBlank()) {
            builder.load("Likely Source: " + source, UiColorPalette.textBody());
         }
      }

      Object packetType = load_Obj_Obj_Str(entry.init_Packet, "getPacketType");
      if (packetType != null) {
         builder.load("Packet Type: " + load_Str_Obj(packetType), UiColorPalette.textBody());
      }

      builder.load("Tick: " + entry.init_I, UiColorPalette.textBody());
      builder.load("Time: " + Instant.ofEpochMilli(entry.load_J), UiColorPalette.textBody());
      builder.load("Inventory: " + entry.init_Z + " | Movement: " + entry.load_Z, UiColorPalette.textMuted());
      PacketDetailFormatter.Inner_DataUtil normalAnalysis = PacketDetailFormatter.init_bh_DataUtil_AutismPacketLogger(entry);
      if (entry.init_Packet == null) {
         builder.init_V();
         builder.init("Reality", UiColorPalette.cyan());
         if (payloadSnapshot != null) {
            init_Z_AutismPayloadSuppo_bu_ConfigStore(payloadSnapshot, builder);
            builder.load("Packet Object: missing; showing preserved payload snapshot.", UiColorPalette.textMuted());
         } else {
            builder.load("Packet instance is missing, so only metadata is available.", UiColorPalette.errorText());
         }

         if (normalAnalysis.load_Z()) {
            builder.init_V();
            builder.init("Meaning", UiColorPalette.warningYellow());
            init_V_bu_ConfigStore_List(builder, normalAnalysis.load_List());
         }

         if (includePayloadAnalyzer && payloadSnapshot != null) {
            PacketInspectorEngine.ReportBuilder analyzerBuilder = new PacketInspectorEngine.ReportBuilder(title);
            if (init_Z_AutismPayloadSuppo(payloadSnapshot, analyzerBuilder, payloadAnalysis)) {
               builder.init_V();
               builder.init("Analyzer", UiColorPalette.mint());
               builder.init(analyzerBuilder);
            }
         }

         if (normalAnalysis.read_Z()) {
            builder.init_V();
            builder.init("Wire", UiColorPalette.blue());
            init_V_bu_ConfigStore_List(builder, normalAnalysis.read_List());
         }

         return builder.init_bu_JsonUtil();
      } else {
         PacketInspectorEngine.PacketSummaryFlags summary = PacketInspectorEngine.PacketSummaryFlags.init_bu_FileCache;
         if (normalAnalysis.init_Z()) {
            builder.init_V();
            builder.init("Reality", UiColorPalette.cyan());
            init_V_bu_ConfigStore_List(builder, normalAnalysis.init_List());
         } else if (payloadSnapshot != null) {
            builder.init_V();
            builder.init("Reality", UiColorPalette.cyan());
            init_Z_AutismPayloadSuppo_bu_ConfigStore(payloadSnapshot, builder);
         } else {
            PacketInspectorEngine.ReportBuilder summaryBuilder = new PacketInspectorEngine.ReportBuilder(title);
            summary = init_bu_FileCache_Packet(entry.init_Packet, entry.load_Str, entry, summaryBuilder);
            if (summary.init_Z()) {
               builder.init_V();
               builder.init("Reality", UiColorPalette.cyan());
               builder.init(summaryBuilder);
            }
         }

         if (normalAnalysis.load_Z()) {
            builder.init_V();
            builder.init("Meaning", UiColorPalette.warningYellow());
            init_V_bu_ConfigStore_List(builder, normalAnalysis.load_List());
         }

         if (includePayloadAnalyzer && payloadSnapshot != null) {
            PacketInspectorEngine.ReportBuilder analyzerBuilder = new PacketInspectorEngine.ReportBuilder(title);
            if (init_Z_AutismPayloadSuppo(payloadSnapshot, analyzerBuilder, payloadAnalysis)) {
               builder.init_V();
               builder.init("Analyzer", UiColorPalette.mint());
               builder.init(analyzerBuilder);
            }
         }

         if (normalAnalysis.read_Z()) {
            builder.init_V();
            builder.init("Wire", UiColorPalette.blue());
            init_V_bu_ConfigStore_List(builder, normalAnalysis.read_List());
         }

         boolean semanticComplete = normalAnalysis.init_Z() ? normalAnalysis.write() : summary.load();
         if (!semanticComplete) {
            builder.init_V();
            builder.init("Details", UiColorPalette.blue());
            init_V_Obj(entry.init_Packet, builder, new IdentityHashMap<>());
         }

         return builder.init_bu_JsonUtil();
      }
   }

   public static PacketInspectorEngine.InspectionReport read_bu_JsonUtil_AutismPacketLogger_bu_SyncManager(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      try {
         String title = entry == null ? "Payload Analyzer" : entry.load_Str + " Analyzer";
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder(title);
         AutismPayloadSupport.PayloadEntry snapshot = entry == null ? null : AutismPayloadSupport.resolveEntryPayload(entry);
         builder.init("Analyzer", UiColorPalette.mint());
         if (snapshot == null) {
            builder.load("No payload snapshot is available for this packet.", UiColorPalette.errorText());
            return builder.init_bu_JsonUtil();
         }

         if (payloadAnalysis == null) {
            builder.load("Channel: " + snapshot.channel(), UiColorPalette.successText());
            builder.load(
               "Direction: " + snapshot.direction() + " | Protocol: " + init_Str_Str_Str(snapshot.protocolPhase(), "unknown"), init_I_Str(snapshot.direction())
            );
            builder.load("Body Size: " + snapshot.rawBytes().length + " bytes", UiColorPalette.textBright());
            String source = read_Str_Str(snapshot.channel());
            if (source != null && !source.isBlank()) {
               builder.load("What It Looks Like: " + source, UiColorPalette.textBright());
            }

            builder.init_V();
            builder.load("Decoder Status: waiting for automatic payload analysis.", UiColorPalette.warningYellow());
            return builder.init_bu_JsonUtil();
         } else {
            init_Z_AutismPayloadSuppo(snapshot, builder, payloadAnalysis);
            return builder.init_bu_JsonUtil();
         }
      } catch (Throwable t) {
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder("Payload Analyzer");
         builder.init("Analyzer Error", UiColorPalette.errorText());
         builder.load("Reason: " + t.getClass().getSimpleName() + (t.getMessage() == null ? "" : " - " + poll(t.getMessage())), UiColorPalette.errorText());
         return builder.init_bu_JsonUtil();
      }
   }

   public static PacketInspectorEngine.PayloadAnalysis init_bu_SyncManager_AutismPayloadSuppo(AutismPayloadSupport.PayloadEntry snapshot) {
      String channel = snapshot == null ? "payload" : init_Str_Str_Str(snapshot.channel(), "payload");
      return new PacketInspectorEngine.PayloadAnalysis(
         false, false, 0.02, "Preparing " + channel, List.of(new PacketInspectorEngine.ColoredLine("Decoder Status: preparing automatic payload analysis", UiColorPalette.textBody()))
      );
   }

   public static PacketInspectorEngine.PayloadAnalysis init_bu_SyncManager_Str(String message) {
      String reason = message != null && !message.isBlank() ? message : "unknown error";
      return new PacketInspectorEngine.PayloadAnalysis(
         true,
         true,
         1.0,
         "Decoder failed",
         List.of(new PacketInspectorEngine.ColoredLine("Decoder Status: failed", UiColorPalette.errorText()), new PacketInspectorEngine.ColoredLine("Reason: " + poll(reason), UiColorPalette.errorText()))
      );
   }

   public static PacketInspectorEngine.PayloadAnalysis init_bu_SyncManager_AutismPayloadSuppo_Consumer(
      AutismPayloadSupport.PayloadEntry snapshot, Consumer<PacketInspectorEngine.PayloadAnalysis> progressSink
   ) {
      if (snapshot == null) {
         PacketInspectorEngine.PayloadAnalysis empty = new PacketInspectorEngine.PayloadAnalysis(
            true, false, 1.0, "No payload body", List.of(new PacketInspectorEngine.ColoredLine("No payload snapshot is available.", UiColorPalette.textMuted()))
         );
         if (progressSink != null) {
            progressSink.accept(empty);
         }

         return empty;
      } else {
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder("Payload Analyzer");

         try {
            init_Z_AutismPayloadSuppo_2(snapshot, builder, progressSink);
            PacketInspectorEngine.PayloadAnalysis done = new PacketInspectorEngine.PayloadAnalysis(true, false, 1.0, init_Str_List(builder.init_List), builder.init_List);
            if (progressSink != null) {
               progressSink.accept(done);
            }

            return done;
         } catch (Throwable t) {
            PacketInspectorEngine.PayloadAnalysis failed = init_bu_SyncManager_Str(t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage()));
            if (progressSink != null) {
               progressSink.accept(failed);
            }

            return failed;
         }
      }
   }

   private static String init_Str_List(List<PacketInspectorEngine.ColoredLine> lines) {
      if (lines != null) {
         for (PacketInspectorEngine.ColoredLine line : lines) {
            String text = line == null ? "" : line.init_Str();
            if (text != null) {
               String lower = text.toLowerCase(Locale.ROOT);
               if (lower.contains("complete known-schema decode")) {
                  return "Known schema decoded";
               }

               if (lower.contains("confidence: high")) {
                  return "High-confidence analysis";
               }

               if (lower.contains("confidence: partial")) {
                  return "Partial analysis complete";
               }

               if (lower.contains("binary payload")) {
                  return "Binary analysis complete";
               }
            }
         }
      }

      return "Analysis finished";
   }

   private static PacketInspectorEngine.PacketSummaryFlags init_bu_FileCache_Packet(
      Packet<?> packet, String packetNameHint, AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.ReportBuilder builder
   ) {
      PacketInspectorEngine.PacketSummaryFlags packetSpecific = load_bu_FileCache_Packet(packet, packetNameHint, entry, builder);
      return packetSpecific.init_Z() ? packetSpecific : load_bu_FileCache_Packet_bu_ConfigStore(packet, builder);
   }

   private static boolean init_Z_AutismPayloadSuppo_bu_ConfigStore(AutismPayloadSupport.PayloadEntry snapshot, PacketInspectorEngine.ReportBuilder builder) {
      if (snapshot == null) {
         return false;
      }

      byte[] raw = snapshot.rawBytes();
      String channel = init_Str_Str_Str(snapshot.channel(), "unknown");
      builder.load("Channel: " + channel, UiColorPalette.successText());
      PayloadChannelFilterManager.Inner_StreamUtil filter = init_bB_StreamUtil_AutismPayloadSuppo(snapshot);
      if (filter != null) {
         builder.load("Matched Filter: " + filter.load() + " (" + filter.pattern() + ")", UiColorPalette.mint());
      }

      String source = read_Str_Str(channel);
      if (source != null && !source.isBlank()) {
         builder.load("Likely Source: " + source, UiColorPalette.textBright());
      }

      builder.load(
         "Payload Direction: " + snapshot.direction() + " | Protocol: " + init_Str_Str_Str(snapshot.protocolPhase(), "unknown"), init_I_Str(snapshot.direction())
      );
      if (snapshot.packetId() >= 0) {
         builder.load("Payload Packet ID: " + snapshot.packetId(), UiColorPalette.textBody());
      }

      builder.load("Payload Class: " + init_Str_Str_Str(snapshot.payloadClassName(), "unknown"), UiColorPalette.textBody());
      builder.load("Payload Size: " + raw.length + " bytes", UiColorPalette.textBright());
      if (snapshot.commandApiValue() != null) {
         builder.load("CommandApi Value: " + snapshot.commandApiValue(), UiColorPalette.successText());
      }

      PayloadFieldCodec.Inner_NetHelper preview = PayloadFieldCodec.buildPreview(raw);
      if (load_Z_Str(channel)) {
         List<String> channels = sync(raw, 10);
         if (!channels.isEmpty()) {
            int total = init_I_BArr(raw);
            builder.load((read_Z_Str(channel) ? "Unregistered" : "Registered") + " Channels: " + total, UiColorPalette.successText());

            for (String value : channels) {
               builder.load("  - " + value, UiColorPalette.textBright());
            }

            if (total > channels.size()) {
               builder.load("  ... +" + (total - channels.size()) + " more", UiColorPalette.textMuted());
            }
         }
      }

      if (preview.mcStringPreview() != null && !preview.mcStringPreview().startsWith("<")) {
         builder.load("Minecraft String: " + open(poll(preview.mcStringPreview())), UiColorPalette.successText());
      }

      if (preview.javaUtfPreview() != null && !preview.javaUtfPreview().startsWith("<")) {
         builder.load("Java writeUTF: " + open(poll(preview.javaUtfPreview())), UiColorPalette.successText());
      }

      if (preview.utf8Preview() != null && !preview.utf8Preview().isBlank() && !"<binary>".equals(preview.utf8Preview())) {
         builder.load("UTF-8 Guess: " + open(poll(init_Str_Str(preview.utf8Preview()))), UiColorPalette.successText());
      }

      List<String> identifiers = sync(raw, 6);
      if (!identifiers.isEmpty() && !load_Z_Str(channel)) {
         builder.load("Identifier Tokens: " + identifiers.size(), UiColorPalette.textBright());

         for (String identifier : identifiers) {
            builder.load("  - " + identifier, UiColorPalette.textBody());
         }
      }

      builder.load("Payload Hex: " + preview.hexPreview(), UiColorPalette.textMuted());
      builder.load("Analyzer: automatic payload decoding runs when this packet is inspected.", UiColorPalette.textBody());
      return true;
   }

   private static void init_V_bu_ConfigStore_List(PacketInspectorEngine.ReportBuilder builder, List<PacketInspectorEngine.ColoredLine> lines) {
      if (builder != null && lines != null) {
         for (PacketInspectorEngine.ColoredLine line : lines) {
            if (line != null) {
               builder.load(line.init_Str(), line.init_I());
            }
         }
      }
   }

   private static PayloadChannelFilterManager.Inner_StreamUtil init_bB_StreamUtil_AutismPayloadSuppo(AutismPayloadSupport.PayloadEntry snapshot) {
      if (snapshot == null) {
         return null;
      }

      try {
         return new PayloadChannelFilterManager().findMatchingRuleForEntry(snapshot, snapshot.direction());
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static PacketInspectorEngine.PacketSummaryFlags load_bu_FileCache_Packet(
      Packet<?> packet, String packetNameHint, AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.ReportBuilder builder
   ) {
      boolean wrote = false;
      boolean complete = false;
      if (packet instanceof ServerboundPlayerActionPacket playerAction) {
         wrote = true;
         complete = true;
         builder.load("Action: " + playerAction.getAction(), UiColorPalette.textBright());
         builder.load("Meaning: " + init_Str_ServerboundPlayerA(playerAction.getAction()), UiColorPalette.textBright());
         builder.load("Block Pos: " + init_Str_BlockPos(playerAction.getPos()), UiColorPalette.textBright());
         builder.load("Face: " + playerAction.getDirection(), UiColorPalette.textBright());
         builder.load("Sequence: " + playerAction.getSequence(), UiColorPalette.textBody());
         String capturedBlockState = entry == null ? null : entry.write;
         if (capturedBlockState != null && !capturedBlockState.isBlank()) {
            builder.load("Block: " + capturedBlockState, UiColorPalette.successText());
         } else {
            BlockState state = init_BlockState_BlockPos(playerAction.getPos());
            if (state != null) {
               builder.load("Block: " + init_Str_BlockState(state), UiColorPalette.successText());
            }
         }

         if (init_Minecraft.player != null
            && (
               playerAction.getAction() == Action.DROP_ITEM
                  || playerAction.getAction() == Action.DROP_ALL_ITEMS
                  || playerAction.getAction() == Action.RELEASE_USE_ITEM
            )) {
            builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getMainHandItem()), UiColorPalette.successText());
         }
      }

      if (packet instanceof ServerboundUseItemOnPacket interactBlock) {
         BlockHitResult hit = interactBlock.hitResult();
         wrote = true;
         complete = true;
         builder.load("Interaction: Use Block (right click targeted block)", UiColorPalette.textBright());
         builder.load("Hand: " + interactBlock.hand(), UiColorPalette.textBright());
         builder.load("Block Pos: " + init_Str_BlockPos(hit.getBlockPos()), UiColorPalette.textBright());
         builder.load("Side: " + hit.getDirection(), UiColorPalette.textBright());
         builder.load("Hit Pos: " + init_Str_Vec3(hit.getLocation()), UiColorPalette.textBright());
         builder.load("Inside Block: " + hit.isInside(), UiColorPalette.textBright());
         builder.load("Sequence: " + interactBlock.sequence(), UiColorPalette.textBody());
         if (init_Minecraft.player != null) {
            builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getItemInHand(interactBlock.hand())), UiColorPalette.successText());
         }

         String capturedBlockState = entry == null ? null : entry.write;
         if (capturedBlockState != null && !capturedBlockState.isBlank()) {
            builder.load("Block: " + capturedBlockState, UiColorPalette.successText());
         } else {
            BlockState state = init_BlockState_BlockPos(hit.getBlockPos());
            if (state != null) {
               builder.load("Block: " + init_Str_BlockState(state), UiColorPalette.successText());
            }
         }
      }

      if (packet instanceof ServerboundUseItemPacket interactItem) {
         wrote = true;
         complete = true;
         builder.load("Interaction: Use Item (right click without a targeted block)", UiColorPalette.textBright());
         builder.load("Hand: " + interactItem.hand(), UiColorPalette.textBright());
         builder.load("Sequence: " + interactItem.sequence(), UiColorPalette.textBody());
         builder.load("Yaw: " + interactItem.yRot(), UiColorPalette.textBody());
         builder.load("Pitch: " + interactItem.xRot(), UiColorPalette.textBody());
         if (init_Minecraft.player != null) {
            if (load_Obj_Obj_StrArr(init_Minecraft.player.getInventory(), "getSelectedSlot", "selectedSlot") instanceof Number number) {
               builder.load("Selected Slot: " + load_Str_I(number.intValue()), UiColorPalette.textBright());
            }

            builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getItemInHand(interactItem.hand())), UiColorPalette.successText());
         }
      }

      if (packet instanceof ServerboundInteractPacket interactEntity) {
         wrote = true;
         complete = true;
         int entityId = interactEntity.entityId();
         init_V_bu_ConfigStore(builder, "Entity Id", entityId);
         builder.load("Player Sneaking: " + interactEntity.usingSecondaryAction(), UiColorPalette.textBright());
         PacketInspectorEngine.InteractEntityCapture capture = init_bu_NetHelper_ServerboundInterac(interactEntity);
         if (capture.init_Str != null) {
            builder.load("Interaction: " + capture.init_Str, UiColorPalette.textBright());
         }

         if (capture.init_InteractionHand != null) {
            builder.load("Hand: " + capture.init_InteractionHand, UiColorPalette.textBright());
            if (init_Minecraft.player != null) {
               builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getItemInHand(capture.init_InteractionHand)), UiColorPalette.successText());
            }
         }

         if (capture.init_Vec3 != null) {
            builder.load("Hit Pos: " + init_Str_Vec3(capture.init_Vec3), UiColorPalette.textBright());
         }
      }

      if (packet instanceof ServerboundAttackPacket attack) {
         wrote = true;
         complete = true;
         init_V_bu_ConfigStore(builder, "Entity Id", attack.entityId());
         builder.load("Interaction: Attack", UiColorPalette.textBright());
         if (init_Minecraft.player != null) {
            builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getMainHandItem()), UiColorPalette.successText());
         }
      }

      if (packet instanceof ClientboundOpenScreenPacket openScreen) {
         wrote = true;
         complete = true;
         builder.load("Sync Id: " + openScreen.getContainerId(), UiColorPalette.textBright());
         builder.load("Screen Type: " + load_Str_Obj(openScreen.getType()), UiColorPalette.textBright());
         builder.load("Title: " + init_Str_Obj(openScreen.getTitle()), UiColorPalette.successText());
      }

      if (packet instanceof ClientboundContainerSetSlotPacket slotUpdate) {
         wrote = true;
         complete = true;
         builder.load("Sync Id: " + slotUpdate.getContainerId(), UiColorPalette.textBright());
         builder.load("Revision: " + slotUpdate.getStateId(), UiColorPalette.textBody());
         builder.load("Slot: " + slotUpdate.getSlot(), UiColorPalette.textBright());
         builder.load("Stack: " + init_Str_ItemStack(slotUpdate.getItem()), UiColorPalette.successText());
      }

      if (packet instanceof ClientboundContainerSetContentPacket inventory) {
         wrote = true;
         builder.load("Sync Id: " + inventory.containerId(), UiColorPalette.textBright());
         builder.load("Revision: " + inventory.stateId(), UiColorPalette.textBody());
         builder.load("Items: " + inventory.items().size() + " slots (" + init_I_List(inventory.items()) + " non-empty)", UiColorPalette.textBright());
         builder.load("Cursor: " + init_Str_ItemStack(inventory.carriedItem()), UiColorPalette.successText());
      }

      if (packet instanceof ClientboundLevelChunkWithLightPacket chunkData) {
         wrote = true;
         builder.load("Chunk: " + chunkData.x() + ", " + chunkData.z(), UiColorPalette.textBright());
         builder.load("Chunk Data: " + load_Str_Obj(chunkData.chunkData()), UiColorPalette.textBody());
         builder.load("Light Data: " + load_Str_Obj(chunkData.lightData()), UiColorPalette.textBody());
      }

      if (packet instanceof ClientboundEntityPositionSyncPacket positionSync) {
         wrote = true;
         init_V_bu_ConfigStore(builder, "Entity Id", positionSync.id());
         builder.load("Values: " + load_Str_Obj(Compat263.entityPosSyncValues(positionSync)), UiColorPalette.textBright());
         builder.load("On Ground: " + positionSync.onGround(), UiColorPalette.textBright());
      }

      if (packet instanceof ClientboundSetEntityDataPacket trackerUpdate) {
         wrote = true;
         init_V_bu_ConfigStore(builder, "Entity Id", trackerUpdate.id());
         List<?> trackedValues = trackerUpdate.packedItems();
         builder.load("Tracked Values: " + (trackedValues == null ? 0 : trackedValues.size()), UiColorPalette.textBright());
      }

      if (packet instanceof ClientboundAddEntityPacket addEntity) {
         wrote = true;
         complete = true;
         init_V_bu_ConfigStore(builder, "Entity Id", addEntity.getId());
         builder.load("Entity Type: " + init_Str_EntityType(addEntity.getType()), UiColorPalette.successText());
         builder.load("Uuid: " + addEntity.getUUID(), UiColorPalette.textBody());
         builder.load("Pos: " + init_Str_Vec3(new Vec3(addEntity.getX(), addEntity.getY(), addEntity.getZ())), UiColorPalette.textBright());
         builder.load("Movement: " + init_Str_Vec3(addEntity.getMovement()), UiColorPalette.textBright());
         builder.load("Rot: yaw=" + addEntity.getYRot() + ", pitch=" + addEntity.getXRot() + ", head=" + addEntity.getYHeadRot(), UiColorPalette.textBright());
         builder.load("Data: " + addEntity.getData(), UiColorPalette.textBody());
      }

      if (packet instanceof ClientboundSoundPacket soundPacket) {
         wrote = true;
         complete = true;
         builder.load("Sound: " + init_Str_Holder(soundPacket.getSound()), UiColorPalette.successText());
         builder.load("Category: " + soundPacket.getSource(), UiColorPalette.textBright());
         builder.load("Pos: " + init_Str_Vec3(new Vec3(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ())), UiColorPalette.textBright());
         builder.load("Volume: " + soundPacket.getVolume(), UiColorPalette.textBody());
         builder.load("Pitch: " + soundPacket.getPitch(), UiColorPalette.textBody());
         builder.load("Seed: " + soundPacket.getSeed(), UiColorPalette.textMuted());
      }

      if (packet instanceof ClientboundGameEventPacket gameState) {
         wrote = true;
         complete = true;
         builder.load("Reason: " + load_Str_Obj(gameState.getEvent()), UiColorPalette.textBright());
         builder.load("Value: " + gameState.getParam(), UiColorPalette.textBright());
      }

      PacketInspectorEngine.PacketSummaryFlags broadKnown = init_bu_FileCache_Packet_bu_ConfigStore(packet, builder);
      if (!wrote && broadKnown.init_Z()) {
         return broadKnown;
      }

      if (broadKnown.init_Z()) {
         wrote = true;
         complete = complete && broadKnown.load();
      }

      AutismPayloadSupport.PayloadEntry payloadSnapshot = entry == null ? null : AutismPayloadSupport.resolveEntryPayload(entry);
      if (payloadSnapshot != null) {
         wrote = true;
         builder.load("Channel: " + payloadSnapshot.channel(), UiColorPalette.textBright());
         if (payloadSnapshot.protocolPhase() != null && !payloadSnapshot.protocolPhase().isBlank()) {
            builder.load("Payload Protocol: " + payloadSnapshot.protocolPhase(), UiColorPalette.textBody());
         }

         builder.load("Payload Class: " + payloadSnapshot.payloadClassName(), UiColorPalette.textBody());
         builder.load("Payload Size: " + payloadSnapshot.byteLength() + " bytes", UiColorPalette.textBright());
         if (payloadSnapshot.commandApiValue() != null) {
            builder.load("CommandApi Value: " + payloadSnapshot.commandApiValue(), UiColorPalette.successText());
         }

         PayloadFieldCodec.Inner_NetHelper preview = PayloadFieldCodec.buildPreview(payloadSnapshot.rawBytes());
         if (preview.mcStringPreview() != null && !preview.mcStringPreview().startsWith("<")) {
            builder.load("Minecraft String: " + preview.mcStringPreview(), UiColorPalette.successText());
         }

         if (preview.javaUtfPreview() != null && !preview.javaUtfPreview().startsWith("<")) {
            builder.load("Java writeUTF: " + preview.javaUtfPreview(), UiColorPalette.successText());
         }

         if (preview.utf8Preview() != null && !preview.utf8Preview().isBlank() && !"<binary>".equals(preview.utf8Preview())) {
            builder.load("UTF-8 Guess: " + preview.utf8Preview(), UiColorPalette.successText());
         }

         builder.load("Payload Hex: " + preview.hexPreview(), UiColorPalette.textMuted());
         builder.load("Payload Fields: raw bytes preserved; use Edit Payload for typed builder.", UiColorPalette.successText());
         complete = false;
      }

      PacketInspectorEngine.PacketSummaryFlags reflective = read_bu_FileCache_Packet(packet, packetNameHint, entry, builder);
      return !wrote && reflective.init_Z() ? reflective : new PacketInspectorEngine.PacketSummaryFlags(wrote, complete);
   }

   private static boolean init_Z_AutismPayloadSuppo(
      AutismPayloadSupport.PayloadEntry snapshot, PacketInspectorEngine.ReportBuilder builder, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      if (payloadAnalysis == null) {
         return init_Z_AutismPayloadSuppo_2(snapshot, builder, (Consumer<PacketInspectorEngine.PayloadAnalysis>)null);
      }

      for (PacketInspectorEngine.ColoredLine line : payloadAnalysis.init_List()) {
         builder.load(line.init_Str(), line.init_I());
      }

      return true;
   }

   private static boolean init_Z_AutismPayloadSuppo_2(
      AutismPayloadSupport.PayloadEntry snapshot, PacketInspectorEngine.ReportBuilder builder, Consumer<PacketInspectorEngine.PayloadAnalysis> progressSink
   ) {
      if (snapshot == null) {
         return false;
      }

      byte[] raw = snapshot.rawBytes();
      boolean decodedStrongly = false;
      boolean decodedAnything = false;
      boolean textualPayload = false;
      boolean completeKnownSchema = false;
      String bestInterpretation = "";
      init_V_bu_ConfigStore(builder, progressSink, 0.06, "Reading payload metadata", false, false);
      builder.load("Channel: " + snapshot.channel(), UiColorPalette.successText());
      builder.load("Direction: " + snapshot.direction() + " | Protocol: " + init_Str_Str_Str(snapshot.protocolPhase(), "unknown"), init_I_Str(snapshot.direction()));
      builder.load("Body Size: " + raw.length + " bytes", UiColorPalette.textBright());
      PayloadChannelFilterManager.Inner_StreamUtil filter = init_bB_StreamUtil_AutismPayloadSuppo(snapshot);
      if (filter != null) {
         builder.load("Matched Filter: " + filter.load() + " (" + filter.pattern() + ")", UiColorPalette.mint());
      }

      String source = read_Str_Str(snapshot.channel());
      if (source != null && !source.isBlank()) {
         builder.load("What It Looks Like: " + source, UiColorPalette.textBright());
      }

      if (snapshot.commandApiValue() != null) {
         builder.load("CommandApi Int: " + snapshot.commandApiValue(), UiColorPalette.successText());
         decodedAnything = true;
      }

      load_V_BArr_bu_ConfigStore(raw, builder);
      init_V_bu_ConfigStore(builder, progressSink, 0.2, "Checking plugin-channel structure", false, false);
      PacketInspectorEngine.PayloadDecodeResult knownChannel = init_bu_BufferHelper_AutismPayloadSuppo(snapshot, raw, builder);
      decodedAnything |= knownChannel.init_Z();
      decodedStrongly |= knownChannel.load_Z();
      textualPayload |= knownChannel.read();
      completeKnownSchema |= knownChannel.write();
      if (knownChannel.init_Z()) {
         bestInterpretation = init_Str_Str_Z(snapshot.channel(), completeKnownSchema);
      }

      PacketInspectorEngine.PayloadDecodeResult decompressed = PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      if (!completeKnownSchema) {
         decompressed = load_bu_BufferHelper_AutismPayloadSuppo(snapshot, raw, builder);
         decodedAnything |= decompressed.init_Z();
         decodedStrongly |= decompressed.load_Z();
         textualPayload |= decompressed.read();
         completeKnownSchema |= decompressed.write();
         if (decompressed.init_Z()) {
            bestInterpretation = "Compressed payload with nested decoded content";
         }
      }

      init_V_bu_ConfigStore(builder, progressSink, 0.36, "Trying JSON decoders", false, false);
      if (!completeKnownSchema) {
         JsonElement json = load_JsonElement_BArr(raw);
         if (json != null) {
            init_V_JsonElement_bu_ConfigStore(json, builder);
            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            completeKnownSchema = true;
            bestInterpretation = "JSON payload";
         } else {
            JsonElement embeddedJson = init_JsonElement_BArr(raw);
            if (embeddedJson != null) {
               builder.load("Decoded As: embedded JSON", UiColorPalette.successText());
               init_V_JsonElement_bu_ConfigStore(embeddedJson, builder);
               decodedStrongly = true;
               decodedAnything = true;
               textualPayload = true;
               bestInterpretation = "Payload containing embedded JSON";
            }
         }
      }

      init_V_bu_ConfigStore(builder, progressSink, 0.52, "Trying string codecs", false, false);
      String minecraftString = completeKnownSchema ? null : AutismPayloadSupport.decodeMcString(raw);
      if (!completeKnownSchema) {
         if (minecraftString != null && !minecraftString.isBlank()) {
            builder.load("Decoded As: Minecraft String", UiColorPalette.successText());
            builder.load("Value: " + open(poll(minecraftString)), UiColorPalette.textBright());
            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Single Minecraft ByteBuf string";
            }
         }

         List<String> minecraftStrings = load_List_BArr_I(raw, 12);
         if (minecraftStrings.size() > 1 || minecraftStrings.size() == 1 && !Objects.equals(minecraftStrings.get(0), minecraftString)) {
            builder.load("Minecraft String Sequence: " + minecraftStrings.size() + " value" + init_Str_I(minecraftStrings.size()), UiColorPalette.successText());

            for (String value : minecraftStrings) {
               builder.load("  - " + open(poll(value)), UiColorPalette.textBright());
            }

            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Sequence of Minecraft ByteBuf strings";
            }
         }

         String javaUtf = AutismPayloadSupport.decodeJavaUtf(raw);
         if (javaUtf != null && !javaUtf.isBlank()) {
            builder.load("Decoded As: Java writeUTF", UiColorPalette.successText());
            builder.load("Value: " + open(poll(javaUtf)), UiColorPalette.textBright());
            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Single Java DataOutput writeUTF string";
            }
         }

         List<String> javaUtfStrings = read_List_BArr_I(raw, 12);
         if (javaUtfStrings.size() > 1 || javaUtfStrings.size() == 1 && !Objects.equals(javaUtfStrings.get(0), javaUtf)) {
            builder.load("Java writeUTF Sequence: " + javaUtfStrings.size() + " value" + init_Str_I(javaUtfStrings.size()), UiColorPalette.successText());

            for (String value : javaUtfStrings) {
               builder.load("  - " + open(poll(value)), UiColorPalette.textBright());
            }

            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Sequence of Java DataOutput writeUTF strings";
            }
         }

         if (init_Z_BArr_bu_ConfigStore(raw, builder)) {
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Null-separated text fields";
            }
         }

         String utf8 = AutismPayloadSupport.decodePrintableUtf8(raw);
         if (utf8 != null && !utf8.isBlank() && !Objects.equals(utf8, minecraftString) && !Objects.equals(utf8, javaUtf)) {
            builder.load("Readable UTF-8: " + open(poll(init_Str_Str(utf8))), UiColorPalette.successText());
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Readable UTF-8 text payload";
            }
         }
      }

      init_V_bu_ConfigStore(builder, progressSink, 0.7, "Extracting identifiers and fragments", false, false);
      if (!completeKnownSchema) {
         PacketInspectorEngine.PayloadDecodeResult byteBufScan = load_bu_BufferHelper_BArr_bu_ConfigStore(raw, builder);
         decodedAnything |= byteBufScan.init_Z();
         decodedStrongly |= byteBufScan.load_Z();
         textualPayload |= byteBufScan.read();
         if (byteBufScan.init_Z() && bestInterpretation.isBlank()) {
            bestInterpretation = "Minecraft ByteBuf-style payload with embedded string fields";
         }

         List<String> identifiers = sync(raw, 16);
         if (!identifiers.isEmpty() && !load_Z_Str(snapshot.channel())) {
            builder.load("Identifier Tokens: " + identifiers.size(), UiColorPalette.textBright());

            for (String identifier : identifiers) {
               builder.load("  - " + identifier, UiColorPalette.textBody());
            }

            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Identifier-bearing custom payload";
            }
         }

         List<String> strings = flush(raw, 8);
         if (!strings.isEmpty()) {
            builder.load("Readable Fragments:", UiColorPalette.textBright());

            for (String string : strings) {
               builder.load("  - " + open(poll(string)), UiColorPalette.textBody());
            }

            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Payload with readable text fragments";
            }
         }
      }

      init_V_bu_ConfigStore(builder, progressSink, 0.78, "Running deep nested probes", false, false);
      if (!completeKnownSchema) {
         PacketInspectorEngine.PayloadDecodeResult deep = read_bu_BufferHelper_BArr_bu_ConfigStore(raw, builder);
         decodedAnything |= deep.init_Z();
         decodedStrongly |= deep.load_Z();
         textualPayload |= deep.read();
         completeKnownSchema |= deep.write();
         if (deep.init_Z() && bestInterpretation.isBlank()) {
            bestInterpretation = deep.load_Z() ? "Nested or encoded payload container" : "Payload with recoverable embedded clues";
         }
      }

      init_V_bu_ConfigStore(builder, progressSink, 0.84, textualPayload ? "Checking confidence" : "Reading numeric and binary hints", false, false);
      if (!textualPayload) {
         init_V_BArr_bu_ConfigStore(raw, builder);
      }

      if (!completeKnownSchema) {
         read_V_BArr_bu_ConfigStore(raw, builder);
      }

      if (raw.length == 0) {
         builder.load("Body is empty.", UiColorPalette.textMuted());
      } else if (!bestInterpretation.isBlank()) {
         builder.load("Best Interpretation: " + bestInterpretation, !completeKnownSchema && !decodedStrongly ? UiColorPalette.textBright() : UiColorPalette.successText());
      }

      if (raw.length != 0) {
         if (completeKnownSchema) {
            builder.load("Confidence: complete known-schema decode. Payload body is accounted for.", UiColorPalette.successText());
         } else if (decodedStrongly) {
            builder.load("Confidence: high. A full structured/text decoder matched this payload.", UiColorPalette.successText());
         } else if (!decodedAnything) {
            builder.load("Binary Payload: no safe schema/text decoder matched. Exact bytes are preserved.", UiColorPalette.textMuted());
         } else if (!decodedStrongly) {
            builder.load("Confidence: partial. Analyzer found readable hints, but no complete schema matched.", UiColorPalette.textMuted());
         }
      }

      String finalStatus = completeKnownSchema
         ? "Known schema decoded"
         : (decodedStrongly ? "High-confidence analysis" : (decodedAnything ? "Partial analysis complete" : "Binary analysis complete"));
      init_V_bu_ConfigStore(builder, progressSink, 1.0, finalStatus, true, false);
      return true;
   }

   private static void init_V_bu_ConfigStore(
      PacketInspectorEngine.ReportBuilder builder, Consumer<PacketInspectorEngine.PayloadAnalysis> sink, double progress, String status, boolean done, boolean failed
   ) {
      if (sink != null) {
         sink.accept(new PacketInspectorEngine.PayloadAnalysis(done, failed, progress, status, builder.init_List));
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper_AutismPayloadSuppo(
      AutismPayloadSupport.PayloadEntry snapshot, byte[] raw, PacketInspectorEngine.ReportBuilder builder
   ) {
      if (snapshot == null) {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }

      String channel = load_Str_Str(snapshot.channel());
      if (load_Z_Str(channel)) {
         boolean decodedRegister = init_Z_AutismPayloadSuppo_3(snapshot, raw, builder);
         return decodedRegister ? PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper : PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }

      if ("minecraft:brand".equals(channel)) {
         String brand = AutismPayloadSupport.decodeMcString(raw);
         String mode = "Minecraft String";
         if (brand == null || brand.isBlank()) {
            brand = AutismPayloadSupport.decodePrintableUtf8(raw);
            mode = "raw UTF-8";
         }

         if (brand != null && !brand.isBlank()) {
            builder.load("Decoded As: Brand payload (" + mode + ")", UiColorPalette.successText());
            builder.load("Brand: " + open(poll(init_Str_Str(brand))), UiColorPalette.textBright());
            return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
         } else {
            return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
         }
      } else {
         if ("vv:server_details".equals(channel)) {
            JsonElement json = load_JsonElement_BArr(raw);
            if (json != null) {
               builder.load("Decoded As: ViaVersion server details JSON", UiColorPalette.successText());
               init_V_JsonElement_bu_ConfigStore(json, builder);
               return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
            }
         }

         if (!"bungeecord".equals(channel) && !"bungeecord:main".equals(channel)) {
            if (channel.startsWith("fabric:")) {
               List<String> values = load_List_BArr_I(raw, 16);
               if (values.isEmpty()) {
                  values = read_List_BArr_I(raw, 16);
               }

               if (!values.isEmpty()) {
                  builder.load("Decoded As: Fabric readable payload fields", UiColorPalette.successText());

                  for (String value : values) {
                     builder.load("  - " + open(poll(init_Str_Str(value))), UiColorPalette.textBright());
                  }

                  return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
               }

               String utf8 = AutismPayloadSupport.decodePrintableUtf8(raw);
               if (utf8 != null && !utf8.isBlank()) {
                  builder.load("Decoded As: Fabric readable text payload", UiColorPalette.successText());
                  builder.load("Text: " + open(poll(init_Str_Str(utf8))), UiColorPalette.textBright());
                  return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
               }
            }

            if (channel.startsWith("worldedit:")) {
               String utf8 = AutismPayloadSupport.decodePrintableUtf8(raw);
               if (utf8 != null && !utf8.isBlank()) {
                  builder.load("Decoded As: WorldEdit/CUI text payload", UiColorPalette.successText());
                  builder.load("Message: " + open(poll(init_Str_Str(utf8))), UiColorPalette.textBright());
                  return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
               }
            }

            return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
         } else {
            return init_bu_BufferHelper_BArr_bu_ConfigStore(raw, builder);
         }
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      PacketInspectorEngine.ByteCursor reader = new PacketInspectorEngine.ByteCursor(raw);

      String subchannel;
      try {
         subchannel = reader.init_Str();
      } catch (IOException ignored) {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }

      if (subchannel != null && !subchannel.isBlank()) {
         builder.load("Decoded As: BungeeCord/DataOutput plugin message", UiColorPalette.successText());
         builder.load("Subchannel: " + subchannel, UiColorPalette.textBright());
         String normalized = subchannel.toLowerCase(Locale.ROOT);
         boolean known = true;

         try {
            switch (normalized) {
               case "connect":
                  builder.load("Target Server: " + reader.init_Str(), UiColorPalette.textBright());
                  break;
               case "connectother":
                  builder.load("Player: " + reader.init_Str(), UiColorPalette.textBright());
                  builder.load("Target Server: " + reader.init_Str(), UiColorPalette.textBright());
               case "ip":
               case "getserver":
               case "getservers":
               case "servername":
                  break;
               case "ipother":
               case "playercount":
               case "playerlist":
               case "uuidother":
               case "serverip":
                  builder.load("Argument: " + reader.init_Str(), UiColorPalette.textBright());
                  break;
               case "message":
               case "messageraw":
               case "kickplayer":
                  builder.load("Player: " + reader.init_Str(), UiColorPalette.textBright());
                  builder.load("Message: " + open(poll(reader.init_Str())), UiColorPalette.textBright());
                  break;
               case "forward": {
                  builder.load("Target Server: " + reader.init_Str(), UiColorPalette.textBright());
                  String nestedChannel = reader.init_Str();
                  builder.load("Nested Channel: " + nestedChannel, UiColorPalette.successText());
                  byte[] nested = reader.load_BArr();
                  init_bu_BufferHelper_Str("Nested Forward Payload", nestedChannel, nested, builder);
                  break;
               }
               case "forwardtoplayer": {
                  builder.load("Target Player: " + reader.init_Str(), UiColorPalette.textBright());
                  String nestedChannel = reader.init_Str();
                  builder.load("Nested Channel: " + nestedChannel, UiColorPalette.successText());
                  byte[] nested = reader.load_BArr();
                  init_bu_BufferHelper_Str("Nested Forward Payload", nestedChannel, nested, builder);
                  break;
               }
               default:
                  known = false;
                  List<String> values = new ArrayList<>();

                  while (reader.load_I() > 0 && values.size() < 48) {
                     int before = reader.init_I();

                     try {
                        String value = reader.init_Str();
                        if (!init_Z_Str(value)) {
                           reader.init(before);
                           break;
                        }

                        values.add(value);
                     } catch (IOException ex) {
                        reader.init(before);
                        break;
                     }
                  }

                  if (!values.isEmpty()) {
                     builder.load("Additional UTF Fields: " + values.size(), UiColorPalette.textBright());

                     for (String value : values) {
                        builder.load("  - " + open(poll(value)), UiColorPalette.textBody());
                     }
                  }
            }
         } catch (IOException ex) {
            builder.load("BungeeCord Parse Stopped: " + poll(ex.getMessage()), UiColorPalette.warningYellow());
            return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
         }

         if (reader.load_I() > 0) {
            builder.load("Remaining Bytes: " + reader.load_I() + "B " + AutismPayloadSupport.toHexPreview(reader.init_BArr(), 48), UiColorPalette.textMuted());
         }

         return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, known && reader.load_I() == 0);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult load_bu_BufferHelper_AutismPayloadSuppo(
      AutismPayloadSupport.PayloadEntry snapshot, byte[] raw, PacketInspectorEngine.ReportBuilder builder
   ) {
      PacketInspectorEngine.CompressionKind kind = init_bu_CachePool_BArr(raw);
      if (kind == PacketInspectorEngine.CompressionKind.NONE) {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      } else {
         byte[] decoded = init_BArr_BArr_bu_CachePool(raw, kind);
         if (decoded != null && decoded.length != 0) {
            builder.load("Decoded As: " + kind.init() + " compressed payload", UiColorPalette.successText());
            builder.load("Uncompressed Size: " + decoded.length + " bytes", UiColorPalette.textBright());
            PacketInspectorEngine.PayloadDecodeResult nested = init_bu_BufferHelper_Str("Uncompressed Payload", snapshot == null ? "" : snapshot.channel(), decoded, builder);
            return nested.init_Z() ? new PacketInspectorEngine.PayloadDecodeResult(true, true, nested.read(), nested.write()) : new PacketInspectorEngine.PayloadDecodeResult(true, true, false, false);
         } else {
            builder.load("Compression Decode: " + kind.init() + " header found, but decompression failed.", UiColorPalette.warningYellow());
            return new PacketInspectorEngine.PayloadDecodeResult(true, false, false, false);
         }
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper_Str(String title, String channel, byte[] bytes, PacketInspectorEngine.ReportBuilder builder) {
      if (bytes == null) {
         bytes = new byte[0];
      }

      builder.load(title + ": " + bytes.length + " bytes", UiColorPalette.textBright());
      if (bytes.length == 0) {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }

      boolean anything = false;
      boolean strong = false;
      boolean textual = false;
      boolean complete = false;
      JsonElement json = load_JsonElement_BArr(bytes);
      if (json != null) {
         builder.load("  Nested Decoded As: JSON", UiColorPalette.successText());
         init_V_JsonElement_bu_ConfigStore(json, builder);
         return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
      }

      if (load_Z_Str(channel)) {
         AutismPayloadSupport.PayloadEntry nestedSnapshot = new AutismPayloadSupport.PayloadEntry(
            "", channel, bytes, bytes.length, null, "", false, false, "", "", -1, ""
         );
         boolean register = init_Z_AutismPayloadSuppo_3(nestedSnapshot, bytes, builder);
         if (register) {
            return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
         }
      }

      String mcString = AutismPayloadSupport.decodeMcString(bytes);
      if (mcString != null && !mcString.isBlank()) {
         builder.load("  Nested Minecraft String: " + open(poll(mcString)), UiColorPalette.successText());
         anything = true;
         strong = true;
         textual = true;
         complete = true;
      }

      List<String> javaUtf = read_List_BArr_I(bytes, 8);
      if (!javaUtf.isEmpty()) {
         builder.load("  Nested Java UTF Fields: " + javaUtf.size(), UiColorPalette.successText());

         for (String value : javaUtf) {
            builder.load("    - " + open(poll(value)), UiColorPalette.textBody());
         }

         anything = true;
         strong = true;
         textual = true;
         complete = true;
      }

      List<String> ids = sync(bytes, 8);
      if (!ids.isEmpty()) {
         builder.load("  Nested Identifiers: " + ids.size(), UiColorPalette.textBright());

         for (String id : ids) {
            builder.load("    - " + id, UiColorPalette.textBody());
         }

         anything = true;
         textual = true;
      }

      if (!anything) {
         String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
         if (utf8 != null && !utf8.isBlank()) {
            builder.load("  Nested UTF-8: " + open(poll(init_Str_Str(utf8))), UiColorPalette.textBright());
            anything = true;
            textual = true;
         } else {
            builder.load("  Nested Hex: " + AutismPayloadSupport.toHexPreview(bytes, 64), UiColorPalette.textMuted());
         }
      }

      return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, complete);
   }

   private static PacketInspectorEngine.PayloadDecodeResult load_bu_BufferHelper_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length >= 2) {
         List<PacketInspectorEngine.WireFieldSlice> fields = write_List_BArr_I(raw, 16);
         if (fields.isEmpty()) {
            return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
         }

         builder.load("Minecraft ByteBuf String Fields: " + fields.size(), UiColorPalette.textBright());

         for (PacketInspectorEngine.WireFieldSlice field : fields) {
            builder.load("  @" + field.init_I() + " len=" + field.load() + ": " + open(poll(field.init_Str())), UiColorPalette.textBody());
         }

         return new PacketInspectorEngine.PayloadDecodeResult(true, false, true, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult read_bu_BufferHelper_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length != 0) {
         boolean anything = false;
         boolean strong = false;
         boolean textual = false;
         PacketInspectorEngine.PayloadDecodeResult nbt = init_bu_BufferHelper_BArr(raw, builder, "Payload NBT");
         anything |= nbt.init_Z();
         strong |= nbt.load_Z();
         textual |= nbt.read();
         String text = new String(raw, StandardCharsets.UTF_8);
         if (init_Z_Str_bu_ConfigStore(text, builder)) {
            anything = true;
            textual = true;
         }

         PacketInspectorEngine.PayloadDecodeResult embedded = init_bu_BufferHelper_Str_bu_ConfigStore(text, builder);
         anything |= embedded.init_Z();
         strong |= embedded.load_Z();
         textual |= embedded.read();
         PacketInspectorEngine.PayloadDecodeResult lengthPrefixed = write_bu_BufferHelper_BArr_bu_ConfigStore(raw, builder);
         anything |= lengthPrefixed.init_Z();
         strong |= lengthPrefixed.load_Z();
         textual |= lengthPrefixed.read();
         PacketInspectorEngine.PayloadDecodeResult rawUuid = update(raw, builder);
         anything |= rawUuid.init_Z();
         return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper_BArr(byte[] raw, PacketInspectorEngine.ReportBuilder builder, String label) {
      if (raw != null && raw.length >= 3) {
         CompoundTag compressed = init_CompoundTag_BArr(raw);
         if (compressed != null) {
            builder.load(label + ": compressed NBT compound", UiColorPalette.successText());
            init_V_CompoundTag(compressed, builder, "  ");
            return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, true);
         } else {
            return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
         }
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static CompoundTag init_CompoundTag_BArr(byte[] raw) {
      if (init_bu_CachePool_BArr(raw) != PacketInspectorEngine.CompressionKind.GZIP) {
         return null;
      }

      try (ByteArrayInputStream input = new ByteArrayInputStream(raw)) {
         CompoundTag tag = NbtIo.readCompressed(input, MacroClipboardCodec.init_NbtAccounter());
         return tag != null && !tag.isEmpty() ? tag : null;
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static void init_V_CompoundTag(CompoundTag tag, PacketInspectorEngine.ReportBuilder builder, String indent) {
      if (tag != null) {
         builder.load(indent + "Keys: " + tag.size(), UiColorPalette.textBright());
         int shown = 0;

         for (String key : tag.keySet()) {
            if (shown >= 16) {
               builder.load(indent + "... +" + (tag.size() - shown) + " more", UiColorPalette.textMuted());
               break;
            }

            Tag value = tag.get(key);
            builder.load(indent + key + ": " + init_Str_Tag(value), UiColorPalette.textBody());
            shown++;
         }
      }
   }

   private static String init_Str_Tag(Tag tag) {
      if (tag == null) {
         return "null";
      }

      String value = tag.toString();
      return tag.getClass().getSimpleName() + " " + poll(value);
   }

   private static boolean init_Z_Str_bu_ConfigStore(String text, PacketInspectorEngine.ReportBuilder builder) {
      if (text != null && !text.isBlank()) {
         boolean wrote = false;
         List<String> uuids = init_List_Pattern(flush_Pattern, text, 8);
         if (!uuids.isEmpty()) {
            builder.load("UUID Tokens: " + uuids.size(), UiColorPalette.textBright());

            for (String uuid : uuids) {
               builder.load("  - " + uuid.toLowerCase(Locale.ROOT), UiColorPalette.textBody());
            }

            wrote = true;
         }

         List<String> hosts = init_List_Pattern(close_Pattern, text, 12);
         if (!hosts.isEmpty()) {
            builder.load("Host/IP Tokens: " + hosts.size(), UiColorPalette.textBright());

            for (String host : hosts) {
               builder.load("  - " + host, UiColorPalette.textBody());
            }

            wrote = true;
         }

         return wrote;
      } else {
         return false;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper_Str_bu_ConfigStore(String text, PacketInspectorEngine.ReportBuilder builder) {
      if (text != null && text.length() >= 16) {
         boolean anything = false;
         boolean strong = false;
         boolean textual = false;
         int shown = 0;
         Matcher base64 = update_Pattern.matcher(text);

         while (base64.find() && shown < 4) {
            String token = base64.group();
            byte[] decoded = init_BArr_Str(token);
            if (init_Z_BArr(decoded)) {
               PacketInspectorEngine.PayloadDecodeResult outcome = init_bu_BufferHelper_BArr(decoded);
               if (outcome.init_Z()) {
                  builder.load("Embedded Base64 Payload @" + base64.start() + ": " + decoded.length + " bytes", UiColorPalette.successText());
                  PacketInspectorEngine.PayloadDecodeResult nested = init_bu_BufferHelper_Str("  Base64 decoded", "", decoded, builder);
                  anything = true;
                  strong |= nested.load_Z() || outcome.load_Z();
                  textual |= nested.read() || outcome.read();
                  shown++;
               }
            }
         }

         shown = 0;
         Matcher hex = sync_Pattern.matcher(text);

         while (hex.find() && shown < 4) {
            String token = hex.group();
            byte[] decoded = load_BArr_Str(token);
            if (init_Z_BArr(decoded)) {
               PacketInspectorEngine.PayloadDecodeResult outcome = init_bu_BufferHelper_BArr(decoded);
               if (outcome.init_Z()) {
                  builder.load("Embedded Hex Payload @" + hex.start() + ": " + decoded.length + " bytes", UiColorPalette.successText());
                  PacketInspectorEngine.PayloadDecodeResult nested = init_bu_BufferHelper_Str("  Hex decoded", "", decoded, builder);
                  anything = true;
                  strong |= nested.load_Z() || outcome.load_Z();
                  textual |= nested.read() || outcome.read();
                  shown++;
               }
            }
         }

         return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult write_bu_BufferHelper_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length >= 5) {
         boolean anything = false;
         boolean strong = false;
         boolean textual = false;
         Set<String> seen = new LinkedHashSet<>();
         int shown = 0;

         for (int offset = 0; offset < raw.length - 2 && shown < 6; offset++) {
            PacketInspectorEngine.VarIntRead varInt = init_bu_TextFormat_BArr_I(raw, offset);
            if (varInt != null) {
               int length = varInt.init();
               int start = varInt.load();
               if (init_Z_I(length, start, raw.length)) {
                  String key = start + ":" + length;
                  if (seen.add(key)) {
                     byte[] slice = Arrays.copyOfRange(raw, start, start + length);
                     PacketInspectorEngine.PayloadDecodeResult outcome = init_bu_BufferHelper_BArr(slice);
                     if (outcome.init_Z()) {
                        builder.load("Nested Candidate: VarInt length @" + offset + " -> " + length + " bytes", UiColorPalette.textBright());
                        PacketInspectorEngine.PayloadDecodeResult nested = init_bu_BufferHelper_Str("  Candidate Payload", "", slice, builder);
                        anything = true;
                        strong |= nested.load_Z() || outcome.load_Z();
                        textual |= nested.read() || outcome.read();
                        shown++;
                     }
                  }
               }
            }

            int shortLength = (raw[offset] & 255) << 8 | raw[offset + 1] & 255;
            int shortStart = offset + 2;
            if (init_Z_I(shortLength, shortStart, raw.length)) {
               String key = shortStart + ":" + shortLength;
               if (seen.add(key)) {
                  byte[] slice = Arrays.copyOfRange(raw, shortStart, shortStart + shortLength);
                  PacketInspectorEngine.PayloadDecodeResult outcome = init_bu_BufferHelper_BArr(slice);
                  if (outcome.init_Z()) {
                     builder.load("Nested Candidate: unsigned-short length @" + offset + " -> " + shortLength + " bytes", UiColorPalette.textBright());
                     PacketInspectorEngine.PayloadDecodeResult nested = init_bu_BufferHelper_Str("  Candidate Payload", "", slice, builder);
                     anything = true;
                     strong |= nested.load_Z() || outcome.load_Z();
                     textual |= nested.read() || outcome.read();
                     shown++;
                  }
               }
            }
         }

         return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult update(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length >= 16) {
         List<String> uuids = new ArrayList<>();

         for (int offset = 0; offset + 16 <= raw.length && uuids.size() < 4; offset++) {
            if (!init_Z_BArr(raw, offset, 16)) {
               ByteBuffer buffer = ByteBuffer.wrap(raw, offset, 16).order(ByteOrder.BIG_ENDIAN);
               UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
               if ((uuid.getMostSignificantBits() != 0L || uuid.getLeastSignificantBits() != 0L) && init_Z_UUID(uuid)) {
                  uuids.add("@" + offset + " " + uuid);
               }
            }
         }

         if (uuids.isEmpty()) {
            return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
         }

         builder.load("Raw UUID Candidates: " + uuids.size(), UiColorPalette.textBright());

         for (String uuid : uuids) {
            builder.load("  - " + uuid, UiColorPalette.textBody());
         }

         return new PacketInspectorEngine.PayloadDecodeResult(true, false, false, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper_BArr(byte[] bytes) {
      if (bytes == null || bytes.length == 0) {
         return PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
      } else if (load_JsonElement_BArr(bytes) != null) {
         return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
      } else if (AutismPayloadSupport.decodeMcString(bytes) != null) {
         return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
      } else if (!read_List_BArr_I(bytes, 4).isEmpty()) {
         return PacketInspectorEngine.PayloadDecodeResult.load_bu_BufferHelper;
      } else if (!sync(bytes, 4).isEmpty()) {
         return new PacketInspectorEngine.PayloadDecodeResult(true, false, true, false);
      } else {
         String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
         if (utf8 != null && utf8.length() >= 4) {
            return new PacketInspectorEngine.PayloadDecodeResult(true, false, true, false);
         } else {
            return init_bu_CachePool_BArr(bytes) != PacketInspectorEngine.CompressionKind.NONE
               ? new PacketInspectorEngine.PayloadDecodeResult(true, true, false, false)
               : PacketInspectorEngine.PayloadDecodeResult.init_bu_BufferHelper;
         }
      }
   }

   private static boolean init_Z_I(int length, int start, int totalLength) {
      if (length < 4 || length > 67108864) {
         return false;
      } else if (start >= 0 && start <= totalLength) {
         return start + length > totalLength ? false : length <= totalLength - start;
      } else {
         return false;
      }
   }

   private static boolean init_Z_BArr(byte[] bytes) {
      return bytes != null && bytes.length >= 4 && bytes.length <= 67108864;
   }

   private static List<String> init_List_Pattern(Pattern pattern, String text, int max) {
      if (pattern != null && text != null && !text.isBlank()) {
         LinkedHashSet<String> tokens = new LinkedHashSet<>();
         Matcher matcher = pattern.matcher(text);

         while (matcher.find() && tokens.size() < Math.max(1, max)) {
            tokens.add(matcher.group());
         }

         return new ArrayList<>(tokens);
      } else {
         return List.of();
      }
   }

   private static byte[] init_BArr_Str(String token) {
      if (token != null && !token.isBlank()) {
         String normalized = token.strip();
         int remainder = normalized.length() % 4;
         if (remainder != 0) {
            normalized = normalized + "=".repeat(4 - remainder);
         }

         try {
            return Base64.getDecoder().decode(normalized);
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static byte[] load_BArr_Str(String token) {
      if (token != null && !token.isBlank()) {
         String normalized = token.strip().replaceFirst("(?i)^0x", "");
         if ((normalized.length() & 1) == 0 && normalized.length() >= 8 && normalized.matches("(?i)[0-9a-f]+")) {
            byte[] out = new byte[normalized.length() / 2];

            try {
               for (int i = 0; i < normalized.length(); i += 2) {
                  out[i / 2] = (byte)Integer.parseInt(normalized.substring(i, i + 2), 16);
               }

               return out;
            } catch (Throwable ignored) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean init_Z_BArr(byte[] raw, int offset, int length) {
      if (raw != null && offset >= 0 && length > 0 && offset + length <= raw.length) {
         int printable = 0;

         for (int i = offset; i < offset + length; i++) {
            int value = raw[i] & 255;
            if (value >= 32 && value <= 126) {
               printable++;
            }
         }

         return printable >= length * 3 / 4;
      } else {
         return false;
      }
   }

   private static boolean init_Z_UUID(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      long most = uuid.getMostSignificantBits();
      int version = (int)(most >>> 12 & 15L);
      return version >= 1 && version <= 7;
   }

   private static boolean init_Z_AutismPayloadSuppo_3(AutismPayloadSupport.PayloadEntry snapshot, byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      List<String> nullSeparated = init_List_BArr_I(raw, 48);
      List<String> identifiers = sync(raw, 48);
      LinkedHashSet<String> channels = new LinkedHashSet<>();
      channels.addAll(nullSeparated.stream().filter(value -> read_Pattern.matcher(value).matches()).map(value -> value.toLowerCase(Locale.ROOT)).toList());
      channels.addAll(identifiers);
      if (channels.isEmpty()) {
         builder.load("Register Payload: no channel identifiers were readable.", UiColorPalette.textMuted());
         return false;
      }

      int total = Math.max(init_I_BArr(raw), channels.size());
      builder.load(
         (read_Z_Str(snapshot.channel()) ? "Unregistered" : "Registered") + " Channels: " + channels.size() + (total > channels.size() ? "+ shown" : ""),
         UiColorPalette.successText()
      );
      int shown = 0;

      for (String channel : channels) {
         if (shown >= 48) {
            builder.load("  ... +" + (channels.size() - shown) + " more", UiColorPalette.textMuted());
            break;
         }

         builder.load("  - " + channel, UiColorPalette.textBright());
         shown++;
      }

      return true;
   }

   private static JsonElement init_JsonElement_BArr(byte[] raw) {
      if (raw != null && raw.length >= 2) {
         String text = new String(raw, StandardCharsets.UTF_8);
         int attempts = 0;

         for (int start = 0; start < text.length() && attempts < 20; start++) {
            char open = text.charAt(start);
            if (open == '{' || open == '[') {
               char close = (char)(open == '{' ? 125 : 93);

               for (int end = text.lastIndexOf(close); end > start && attempts < 20; end = text.lastIndexOf(close, end - 1)) {
                  attempts++;
                  String candidate = text.substring(start, end + 1).strip();
                  if (candidate.length() >= 2) {
                     try {
                        JsonElement parsed = JsonParser.parseString(candidate);
                        if (parsed != null && (parsed.isJsonObject() || parsed.isJsonArray())) {
                           return parsed;
                        }
                     } catch (Throwable var9) {
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean init_Z_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      List<String> parts = init_List_BArr_I(raw, 16);
      if (parts.size() <= 1) {
         return false;
      }

      builder.load("Null-Separated Strings: " + parts.size() + " value" + init_Str_I(parts.size()), UiColorPalette.successText());

      for (String part : parts) {
         builder.load("  - " + open(poll(part)), UiColorPalette.textBright());
      }

      return true;
   }

   private static List<String> init_List_BArr_I(byte[] raw, int max) {
      if (raw != null && raw.length != 0) {
         boolean hasNull = false;

         for (byte value : raw) {
            if (value == 0) {
               hasNull = true;
               break;
            }
         }

         if (!hasNull) {
            return List.of();
         }

         String text = new String(raw, StandardCharsets.UTF_8);
         List<String> values = new ArrayList<>();

         for (String part : text.split("\\u0000")) {
            if (values.size() >= Math.max(1, max)) {
               break;
            }

            String cleaned = init_Str_Str(part);
            if (init_Z_Str(cleaned)) {
               values.add(cleaned);
            }
         }

         return values;
      } else {
         return List.of();
      }
   }

   private static List<String> load_List_BArr_I(byte[] raw, int max) {
      if (raw != null && raw.length != 0) {
         int index = 0;
         List<String> values = new ArrayList<>();

         while (index < raw.length && values.size() < Math.max(1, max)) {
            PacketInspectorEngine.VarIntRead length = init_bu_TextFormat_BArr_I(raw, index);
            if (length == null || length.init() < 0 || length.init() > 32767) {
               return List.of();
            }

            index = length.load();
            if (index + length.init() > raw.length) {
               return List.of();
            }

            String value = new String(raw, index, length.init(), StandardCharsets.UTF_8);
            if (!init_Z_Str(value)) {
               return List.of();
            }

            values.add(value);
            index += length.init();
         }

         return index == raw.length ? values : List.of();
      } else {
         return List.of();
      }
   }

   private static List<String> read_List_BArr_I(byte[] raw, int max) {
      if (raw != null && raw.length >= 2) {
         List<String> values = new ArrayList<>();

         try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(raw))) {
            while (input.available() > 0 && values.size() < Math.max(1, max)) {
               String value = input.readUTF();
               if (!init_Z_Str(value)) {
                  return List.of();
               }

               values.add(value);
            }

            return input.available() == 0 ? values : List.of();
         } catch (IOException ignored) {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static PacketInspectorEngine.VarIntRead init_bu_TextFormat_BArr_I(byte[] bytes, int startIndex) {
      if (bytes != null && startIndex >= 0 && startIndex < bytes.length) {
         int value = 0;
         int shift = 0;

         for (int index = startIndex; index < bytes.length && shift < 35; shift += 7) {
            int current = bytes[index++] & 255;
            value |= (current & 127) << shift;
            if ((current & 128) == 0) {
               return new PacketInspectorEngine.VarIntRead(value, index);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static List<PacketInspectorEngine.WireFieldSlice> write_List_BArr_I(byte[] raw, int max) {
      if (raw != null && raw.length >= 2) {
         List<PacketInspectorEngine.WireFieldSlice> fields = new ArrayList<>();
         Set<Integer> occupied = new LinkedHashSet<>();

         for (int offset = 0; offset < raw.length && fields.size() < Math.max(1, max); offset++) {
            if (!occupied.contains(offset)) {
               PacketInspectorEngine.VarIntRead length = init_bu_TextFormat_BArr_I(raw, offset);
               if (length != null) {
                  int valueLength = length.init();
                  if (valueLength >= 1 && valueLength <= 32767) {
                     int start = length.load();
                     if (start + valueLength <= raw.length) {
                        String value = new String(raw, start, valueLength, StandardCharsets.UTF_8);
                        if (init_Z_Str(value)) {
                           int fieldBytes = start + valueLength - offset;
                           if (fieldBytes > 1) {
                              fields.add(new PacketInspectorEngine.WireFieldSlice(offset, valueLength, value));

                              for (int i = offset; i < start + valueLength; i++) {
                                 occupied.add(i);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return fields;
      } else {
         return List.of();
      }
   }

   private static PacketInspectorEngine.CompressionKind init_bu_CachePool_BArr(byte[] raw) {
      if (raw != null && raw.length >= 2) {
         int b0 = raw[0] & 255;
         int b1 = raw[1] & 255;
         if (b0 == 31 && b1 == 139) {
            return PacketInspectorEngine.CompressionKind.GZIP;
         } else {
            return b0 == 120 && (b1 == 1 || b1 == 94 || b1 == 156 || b1 == 218) ? PacketInspectorEngine.CompressionKind.ZLIB : PacketInspectorEngine.CompressionKind.NONE;
         }
      } else {
         return PacketInspectorEngine.CompressionKind.NONE;
      }
   }

   private static byte[] init_BArr_BArr_bu_CachePool(byte[] raw, PacketInspectorEngine.CompressionKind kind) {
      if (raw != null && raw.length != 0 && kind != null && kind != PacketInspectorEngine.CompressionKind.NONE) {
         try {
            ByteArrayInputStream source = new ByteArrayInputStream(raw);
            InputStream input = kind == PacketInspectorEngine.CompressionKind.GZIP ? new GZIPInputStream(source) : new InflaterInputStream(source);

            try (
               input;
               ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(raw.length * 2, 8192));
            ) {
               byte[] buffer = new byte[4096];
               int total = 0;

               int read;
               while ((read = input.read(buffer)) >= 0) {
                  if (read != 0) {
                     total += read;
                     if (total > 67108864) {
                        return null;
                     }

                     out.write(buffer, 0, read);
                  }
               }

               return out.toByteArray();
            }
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static void init_V_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length != 0) {
         List<String> varInts = update(raw, 5);
         if (!varInts.isEmpty()) {
            builder.load("VarInt Prefix: " + String.join(", ", varInts), UiColorPalette.textBody());
         }

         if (raw.length >= 1) {
            builder.load("First Byte: unsigned=" + (raw[0] & 0xFF) + ", signed=" + raw[0], UiColorPalette.textMuted());
         }

         if (raw.length >= 2) {
            ByteBuffer be = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer le = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            builder.load("First Short: BE=" + be.getShort(0) + ", LE=" + le.getShort(0), UiColorPalette.textMuted());
         }

         if (raw.length >= 4) {
            ByteBuffer be = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer le = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            builder.load("First Int: BE=" + be.getInt(0) + ", LE=" + le.getInt(0), UiColorPalette.textMuted());
            float beFloat = be.getFloat(0);
            float leFloat = le.getFloat(0);
            if (init_Z_F(beFloat) || init_Z_F(leFloat)) {
               builder.load("First Float: BE=" + init_Str_F(beFloat) + ", LE=" + init_Str_F(leFloat), UiColorPalette.textMuted());
            }
         }

         if (raw.length >= 8) {
            ByteBuffer be = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer le = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            builder.load("First Long: BE=" + be.getLong(0) + ", LE=" + le.getLong(0), UiColorPalette.textMuted());
            double beDouble = be.getDouble(0);
            double leDouble = le.getDouble(0);
            if (init_Z_D(beDouble) || init_Z_D(leDouble)) {
               builder.load("First Double: BE=" + init_Str_D(beDouble) + ", LE=" + init_Str_D(leDouble), UiColorPalette.textMuted());
            }
         }

         if (raw.length >= 16) {
            ByteBuffer uuidBuffer = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            UUID uuid = new UUID(uuidBuffer.getLong(0), uuidBuffer.getLong(8));
            builder.load("First 16 Bytes As UUID: " + uuid, UiColorPalette.textMuted());
         }
      }
   }

   private static List<String> update(byte[] raw, int max) {
      if (raw != null && raw.length != 0) {
         List<String> values = new ArrayList<>();
         int index = 0;

         while (index < raw.length && values.size() < Math.max(1, max) && index < 20) {
            PacketInspectorEngine.VarIntRead read = init_bu_TextFormat_BArr_I(raw, index);
            if (read == null || read.load() <= index) {
               break;
            }

            values.add(String.valueOf(read.init()));
            index = read.load();
         }

         return values;
      } else {
         return List.of();
      }
   }

   private static void load_V_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      PacketInspectorEngine.CompressionKind kind = init_bu_CachePool_BArr(raw);
      if (kind == PacketInspectorEngine.CompressionKind.GZIP) {
         builder.load("Compression Hint: gzip stream header", UiColorPalette.warningYellow());
      } else if (kind == PacketInspectorEngine.CompressionKind.ZLIB) {
         builder.load("Compression Hint: zlib/deflate stream header", UiColorPalette.warningYellow());
      }
   }

   private static void read_V_BArr_bu_ConfigStore(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length != 0) {
         int printable = 0;
         int zero = 0;
         int high = 0;
         int[] histogram = new int[256];

         for (byte value : raw) {
            int unsigned = value & 255;
            histogram[unsigned]++;
            if (unsigned == 0) {
               zero++;
            }

            if (unsigned >= 128) {
               high++;
            }

            if (unsigned >= 32 && unsigned <= 126) {
               printable++;
            }
         }

         builder.load(
            "Binary Stats: printable="
               + init_Str_I_I(printable, raw.length)
               + ", zero="
               + init_Str_I_I(zero, raw.length)
               + ", high-bit="
               + init_Str_I_I(high, raw.length)
               + ", entropy="
               + init_Str_D(init_D_IArr_I(histogram, raw.length))
               + " bits/B",
            UiColorPalette.textMuted()
         );
      }
   }

   private static double init_D_IArr_I(int[] histogram, int total) {
      if (histogram != null && total > 0) {
         double entropy = 0.0;

         for (int count : histogram) {
            if (count > 0) {
               double p = (double)count / total;
               entropy -= p * (Math.log(p) / Math.log(2.0));
            }
         }

         return entropy;
      } else {
         return 0.0;
      }
   }

   private static String init_Str_I_I(int count, int total) {
      return total <= 0 ? "0%" : Math.round(count * 1000.0 / total) / 10.0 + "%";
   }

   private static String init_Str_F(float value) {
      return !Float.isFinite(value) ? String.valueOf(value) : String.format(Locale.ROOT, "%.4f", value).replaceAll("\\.?0+$", "");
   }

   private static String init_Str_D(double value) {
      return !Double.isFinite(value) ? String.valueOf(value) : String.format(Locale.ROOT, "%.4f", value).replaceAll("\\.?0+$", "");
   }

   private static boolean init_Z_F(float value) {
      if (!Float.isFinite(value)) {
         return false;
      }

      float abs = Math.abs(value);
      return abs == 0.0F || abs >= 1.0E-6F && abs <= 1000000.0F;
   }

   private static boolean init_Z_D(double value) {
      if (!Double.isFinite(value)) {
         return false;
      }

      double abs = Math.abs(value);
      return abs == 0.0 || abs >= 1.0E-6 && abs <= 1000000.0;
   }

   private static boolean init_Z_Str(String value) {
      if (value != null && !value.isBlank()) {
         int printable = 0;
         int control = 0;

         for (int i = 0; i < value.length(); i++) {
            char chr = value.charAt(i);
            if (Character.isISOControl(chr) && chr != '\n' && chr != '\r' && chr != '\t') {
               control++;
            } else {
               printable++;
            }
         }

         return printable > 0 && control * 8 <= Math.max(1, value.length());
      } else {
         return false;
      }
   }

   private static String init_Str_I(int count) {
      return count == 1 ? "" : "s";
   }

   private static void init_V_JsonElement_bu_ConfigStore(JsonElement json, PacketInspectorEngine.ReportBuilder builder) {
      if (json != null) {
         if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            builder.load("Decoded As: JSON object (" + object.size() + " keys)", UiColorPalette.successText());

            for (Entry<String, JsonElement> entry : object.entrySet()) {
               builder.load("  " + entry.getKey() + ": " + init_Str_JsonElement(entry.getValue()), UiColorPalette.textBright());
            }
         } else if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            builder.load("Decoded As: JSON array (" + array.size() + " items)", UiColorPalette.successText());
            int index = 0;

            for (JsonElement element : array) {
               if (index >= 16) {
                  builder.load("  ... +" + (array.size() - index) + " more", UiColorPalette.textMuted());
                  break;
               }

               builder.load("  [" + index + "]: " + init_Str_JsonElement(element), UiColorPalette.textBright());
               index++;
            }
         } else {
            builder.load("Decoded As: JSON value", UiColorPalette.successText());
            builder.load("Value: " + init_Str_JsonElement(json), UiColorPalette.textBright());
         }
      }
   }

   private static String init_Str_JsonElement(JsonElement element) {
      if (element == null || element.isJsonNull()) {
         return "null";
      } else if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         return primitive.isString() ? open(poll(primitive.getAsString())) : poll(primitive.toString());
      } else if (element.isJsonArray()) {
         return "array[" + element.getAsJsonArray().size() + "] " + poll(element.toString());
      } else {
         return element.isJsonObject() ? "object{" + element.getAsJsonObject().size() + "} " + poll(element.toString()) : poll(element.toString());
      }
   }

   private static JsonElement load_JsonElement_BArr(byte[] raw) {
      String text = AutismPayloadSupport.decodePrintableUtf8(raw);
      if (text != null && !text.isBlank()) {
         String stripped = text.strip();
         if (!stripped.startsWith("{") && !stripped.startsWith("[")) {
            return null;
         }

         try {
            return JsonParser.parseString(stripped);
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static List<String> sync(byte[] raw, int max) {
      String text = new String(raw == null ? new byte[0] : raw, StandardCharsets.UTF_8);
      Matcher matcher = read_Pattern.matcher(text);
      LinkedHashSet<String> tokens = new LinkedHashSet<>();

      while (matcher.find() && tokens.size() < Math.max(1, max)) {
         tokens.add(matcher.group().toLowerCase(Locale.ROOT));
      }

      return new ArrayList<>(tokens);
   }

   private static int init_I_BArr(byte[] raw) {
      String text = new String(raw == null ? new byte[0] : raw, StandardCharsets.UTF_8);
      Matcher matcher = read_Pattern.matcher(text);
      LinkedHashSet<String> tokens = new LinkedHashSet<>();

      while (matcher.find()) {
         tokens.add(matcher.group().toLowerCase(Locale.ROOT));
      }

      return tokens.size();
   }

   private static List<String> flush(byte[] raw, int max) {
      String text = new String(raw == null ? new byte[0] : raw, StandardCharsets.ISO_8859_1);
      Matcher matcher = write_Pattern.matcher(text);
      List<String> runs = new ArrayList<>();

      while (matcher.find() && runs.size() < Math.max(1, max)) {
         String run = matcher.group().strip();
         if (run.length() >= 4) {
            runs.add(init_Str_Str(run));
         }
      }

      return runs;
   }

   private static String init_Str_Str(String text) {
      if (text != null && !text.isBlank()) {
         StringBuilder sb = new StringBuilder(text.length());
         boolean spaced = false;

         for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (Character.isISOControl(chr)) {
               if (!spaced) {
                  sb.append(' ');
                  spaced = true;
               }
            } else {
               sb.append(chr);
               spaced = Character.isWhitespace(chr);
            }
         }

         return sb.toString().strip();
      } else {
         return "";
      }
   }

   private static boolean load_Z_Str(String channel) {
      String normalized = load_Str_Str(channel);
      return normalized.equals("minecraft:register")
         || normalized.equals("register")
         || normalized.equals("minecraft:unregister")
         || normalized.equals("unregister");
   }

   private static boolean read_Z_Str(String channel) {
      String normalized = load_Str_Str(channel);
      return normalized.endsWith("unregister");
   }

   private static String load_Str_Str(String channel) {
      return channel == null ? "" : channel.strip().toLowerCase(Locale.ROOT);
   }

   private static String init_Str_Str_Z(String channel, boolean complete) {
      String normalized = load_Str_Str(channel);
      if (normalized.equals("minecraft:register") || normalized.equals("register")) {
         return "Legacy plugin channel registration list";
      } else if (normalized.equals("minecraft:unregister") || normalized.equals("unregister")) {
         return "Legacy plugin channel unregister list";
      } else if (normalized.equals("minecraft:brand")) {
         return "Minecraft brand payload";
      } else if (normalized.equals("bungeecord") || normalized.equals("bungeecord:main")) {
         return complete ? "BungeeCord/DataOutput plugin message" : "BungeeCord/DataOutput-like plugin message";
      } else if (normalized.startsWith("worldedit:")) {
         return "WorldEdit/CUI text payload";
      } else {
         return complete ? "Known channel payload" : "Channel-specific payload";
      }
   }

   private static String read_Str_Str(String channel) {
      if (channel != null && !channel.isBlank()) {
         String normalized = channel.strip().toLowerCase(Locale.ROOT);
         if (normalized.equals("minecraft:register") || normalized.equals("register")) {
            return "legacy plugin-channel registration list";
         } else if (normalized.equals("minecraft:unregister") || normalized.equals("unregister")) {
            return "legacy plugin-channel unregister list";
         } else if (normalized.equals("minecraft:brand")) {
            return "client/server brand payload";
         } else if (normalized.equals("bungeecord:main") || normalized.equals("bungeecord")) {
            return "BungeeCord/proxy plugin messaging";
         } else if (normalized.startsWith("vv:") || normalized.startsWith("viaversion:") || normalized.startsWith("viabackwards:")) {
            return "ViaVersion/ViaFabricPlus compatibility payload";
         } else if (normalized.startsWith("fabric:")) {
            return "Fabric networking payload";
         } else if (normalized.startsWith("worldedit:")) {
            return "WorldEdit/CUI payload";
         } else if (normalized.startsWith("oraxen:")) {
            return "Oraxen resource/item plugin payload";
         } else if (normalized.startsWith("itemsadder:")) {
            return "ItemsAdder resource/item plugin payload";
         } else if (normalized.startsWith("nexo:")) {
            return "Nexo resource/item plugin payload";
         } else if (normalized.startsWith("velocity:")) {
            return "Velocity/proxy plugin messaging";
         } else if (normalized.startsWith("geyser:") || normalized.startsWith("floodgate:")) {
            return "Bedrock bridge plugin payload";
         } else if (!normalized.startsWith("voicechat:") && !normalized.startsWith("plasmovoice:")) {
            int colon = normalized.indexOf(58);
            return colon > 0 ? "custom payload namespace: " + normalized.substring(0, colon) : "custom payload channel";
         } else {
            return "voice chat plugin payload";
         }
      } else {
         return "";
      }
   }

   private static String init_Str_Str_Str(String value, String fallback) {
      return value != null && !value.isBlank() ? value : fallback;
   }

   private static PacketInspectorEngine.PacketSummaryFlags init_bu_FileCache_Packet_bu_ConfigStore(Packet<?> packet, PacketInspectorEngine.ReportBuilder builder) {
      if (packet == null) {
         return PacketInspectorEngine.PacketSummaryFlags.init_bu_FileCache;
      }

      String simpleName = packet.getClass().getSimpleName();
      String className = packet.getClass().getName().replace('$', '.');
      boolean wrote = false;
      boolean complete = true;
      switch (simpleName) {
         case "ClientboundBlockUpdatePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server changed one block");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block", "getBlockState", "blockState", "state");
            break;
         case "ClientboundBlockEntityDataPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server updated block entity data");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Entity Type", "getType", "type");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Nbt", "getTag", "tag");
            break;
         case "ClientboundBlockEventPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server fired a block event");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block", "getBlock", "block");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Event Type", "getB0", "b0", "eventType");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Event Data", "getB1", "b1", "eventData");
            break;
         case "ClientboundSectionBlocksUpdatePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server changed multiple blocks in a chunk section");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Section", "sectionPos", "getSectionPos");
            Object positions = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "positions", "getPositions"), read_Obj_Obj_StrArr(packet, "Positions"));
            Object states = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "states", "getStates"), read_Obj_Obj_StrArr(packet, "States"));
            if (positions != null) {
               wrote |= load_Z_bu_ConfigStore(builder, "Positions", positions);
            }

            if (states != null) {
               wrote |= load_Z_bu_ConfigStore(builder, "States", states);
            }

            complete = false;
            break;
         case "ClientboundLevelEventPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "World event such as particles, sound, or block effect");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Event", "getType", "type");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Data", "getData", "data");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Global", "isGlobalEvent", "globalEvent");
            break;
         case "ClientboundLevelParticlesPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Particle spawn");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Particle", "getParticle", "getParticleType", "particle");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Pos", "getX", "x", "getY", "y", "getZ", "z");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Offset", "getXDist", "xDist", "getYDist", "yDist", "getZDist", "zDist");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Speed", "getMaxSpeed", "maxSpeed");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Count", "getCount", "count");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Override Limiter", "isOverrideLimiter", "overrideLimiter");
            break;
         case "ClientboundForgetLevelChunkPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client should unload a chunk");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Chunk", "pos", "getPos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Chunk X", "getX", "x");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Chunk Z", "getZ", "z");
            break;
         case "ClientboundSetChunkCacheCenterPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server changed chunk cache center");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Chunk X", "getX", "x");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Chunk Z", "getZ", "z");
            break;
         case "ClientboundSetChunkCacheRadiusPacket":
         case "ClientboundSetSimulationDistancePacket":
            wrote |= init_Z_bu_ConfigStore_Str(
               builder, simpleName.contains("Simulation") ? "Server changed simulation distance" : "Server changed chunk render distance"
            );
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Radius", "getRadius", "radius");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Distance", "simulationDistance", "distance");
            break;
         case "ClientboundMoveEntityPacket.Pos":
         case "ClientboundMoveEntityPacket.Rot":
         case "ClientboundMoveEntityPacket.PosRot":
         case "ClientboundTeleportEntityPacket":
         case "ClientboundRotateHeadPacket":
         case "ClientboundHurtAnimationPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server moved or rotated an entity");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Entity Id", "getEntityId", "entityId", "id");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Delta X", "getXa", "xa");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Delta Y", "getYa", "ya");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Delta Z", "getZa", "za");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Head Yaw", "getYHeadRot", "yHeadRot");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Change", "change");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Relatives", "relatives");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "On Ground", "isOnGround", "onGround");
            break;
         case "ClientboundMoveVehiclePacket":
         case "ServerboundMoveVehiclePacket":
            wrote |= init_Z_bu_ConfigStore_Str(
               builder, simpleName.startsWith("Serverbound") ? "Client moved the ridden vehicle" : "Server moved the ridden vehicle"
            );
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Pos", "getX", "x", "getY", "y", "getZ", "z");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "On Ground", "isOnGround", "onGround");
            break;
         case "ClientboundRemoveEntitiesPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server removed entities");
            Object ids = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "getEntityIds", "entityIds"), init_Obj_Obj_I(packet, 0));
            if (ids != null) {
               builder.load("Entities: " + open(ids), UiColorPalette.cyan());
               wrote = true;
            }
            break;
         case "ClientboundTakeItemEntityPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Item/entity pickup animation");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Item Entity", "getItemId", "itemId");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Collector", "getPlayerId", "playerId");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Amount", "getAmount", "amount");
            break;
         case "ClientboundSetPassengersPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server changed vehicle passengers");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Vehicle", "getVehicle", "vehicle", "vehicleId");
            Object passengers = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "getPassengers", "passengers"), read_Obj_Obj_StrArr(packet, "Passengers"));
            if (passengers != null) {
               builder.load("Passengers: " + open(passengers), UiColorPalette.cyan());
               wrote = true;
            }
            break;
         case "ClientboundSetEntityLinkPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server linked one entity to another");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Source", "getSourceId", "sourceId", "source");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Destination", "getDestId", "destId", "destination");
            break;
         case "ClientboundSetHealthPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player health update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Health", "getHealth", "health");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Food", "getFood", "food");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Saturation", "getSaturation", "saturation");
            break;
         case "ClientboundSetExperiencePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player experience update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Progress", "getExperienceProgress", "experienceProgress", "progress");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Level", "getExperienceLevel", "experienceLevel", "level");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Total", "getTotalExperience", "totalExperience", "total");
            break;
         case "ClientboundSetHeldSlotPacket":
         case "ServerboundSetCarriedItemPacket":
            wrote |= init_Z_bu_ConfigStore_Str(
               builder, simpleName.startsWith("Serverbound") ? "Client selected hotbar slot" : "Server selected held hotbar slot"
            );
            Object slot = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "getSlot", "slot", "getSelectedSlot", "selectedSlot"), init_Obj_Obj_I(packet, 0));
            if (slot instanceof Number number) {
               builder.load("Slot: " + load_Str_I(number.intValue()), UiColorPalette.textBright());
               wrote = true;
            } else if (slot != null) {
               builder.load("Slot: " + load_Str_Obj(slot), UiColorPalette.textBright());
               wrote = true;
            }
            break;
         case "ClientboundSetPlayerInventoryPacket":
         case "ClientboundSetCursorItemPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, simpleName.contains("Cursor") ? "Server updated cursor item" : "Server updated player inventory slot");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Slot", "slot", "getSlot");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Stack", "contents", "getContents", "item", "getItem");
            break;
         case "ClientboundContainerClosePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server closed a handled screen");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Sync Id", "getContainerId", "containerId");
            break;
         case "ClientboundCooldownPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Item cooldown update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Item", "getItem", "item");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Ticks", "getDuration", "duration", "ticks");
            break;
         case "ClientboundOpenBookPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Open book animation/screen");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Hand", "getHand", "hand");
            break;
         case "ClientboundOpenSignEditorPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Open sign editor");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Front", "isFrontText", "frontText", "front");
            break;
         case "ClientboundSetActionBarTextPacket":
         case "ClientboundSetTitleTextPacket":
         case "ClientboundSetSubtitleTextPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Server displayed text on the HUD");
            wrote |= read_Z_bu_ConfigStore(builder, packet, "Text", "getText", "text");
            break;
         case "ClientboundSetTitlesAnimationPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Title timing update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Fade In", "getFadeIn", "fadeIn");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Stay", "getStay", "stay");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Fade Out", "getFadeOut", "fadeOut");
            break;
         case "ClientboundClearTitlesPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Clear title/subtitle text");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Reset Times", "shouldResetTimes", "resetTimes");
            break;
         case "ClientboundTabListPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player list header/footer update");
            wrote |= read_Z_bu_ConfigStore(builder, packet, "Header", "getHeader", "header");
            wrote |= read_Z_bu_ConfigStore(builder, packet, "Footer", "getFooter", "footer");
            break;
         case "ClientboundSetTimePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "World time update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Game Time", "getGameTime", "gameTime");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Day Time", "getDayTime", "dayTime");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Tick Day Time", "isTickDayTime", "tickDayTime");
            break;
         case "ClientboundPlayerAbilitiesPacket":
         case "ServerboundPlayerAbilitiesPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player ability flags");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Invulnerable", "isInvulnerable", "invulnerable");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Flying", "isFlying", "flying");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Can Fly", "canFly", "mayfly", "mayFly");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Instabuild", "canInstabuild", "instabuild");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Fly Speed", "getFlyingSpeed", "flyingSpeed");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Walk Speed", "getWalkingSpeed", "walkingSpeed");
            break;
         case "ClientboundChangeDifficultyPacket":
         case "ServerboundChangeDifficultyPacket":
         case "ServerboundLockDifficultyPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Difficulty setting update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Difficulty", "getDifficulty", "difficulty");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Locked", "isLocked", "locked");
            break;
         case "ClientboundSoundEntityPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Sound played from an entity");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Sound", "getSound", "sound");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Category", "getSource", "source", "category");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Entity", "getId", "id", "entityId");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Volume", "getVolume", "volume");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Pitch", "getPitch", "pitch");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Seed", "getSeed", "seed");
            break;
         case "ClientboundStopSoundPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Stop one or more sounds");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Source", "getSource", "source");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Name", "getName", "name");
            break;
         case "ClientboundBossEventPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Boss bar update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Boss Bar Id", "getId", "id");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Operation", "getOperation", "operation");
            complete = false;
            break;
         case "ClientboundPlayerInfoUpdatePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player list entries updated");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Actions", "actions");
            wrote |= write_Z_bu_ConfigStore(builder, packet, "Entries", "entries");
            complete = false;
            break;
         case "ClientboundPlayerInfoRemovePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player list entries removed");
            wrote |= write_Z_bu_ConfigStore(builder, packet, "Profiles", "profileIds", "getProfileIds");
            break;
         case "ClientboundPlayerCombatEndPacket":
         case "ClientboundPlayerCombatEnterPacket":
         case "ClientboundPlayerCombatKillPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Player combat state update");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Player", "playerId", "getPlayerId");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Killer", "killerId", "getKillerId");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Duration", "duration", "getDuration");
            wrote |= read_Z_bu_ConfigStore(builder, packet, "Message", "message", "getMessage");
            break;
         case "ClientboundSetScorePacket":
         case "ClientboundResetScorePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Scoreboard score update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Owner", "owner", "getOwner");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Objective", "objectiveName", "getObjectiveName");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Score", "score", "getScore");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Display", "display", "numberFormat", "getDisplay");
            break;
         case "ClientboundSetObjectivePacket":
         case "ClientboundSetDisplayObjectivePacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Scoreboard objective update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Objective", "getObjectiveName", "objectiveName");
            wrote |= read_Z_bu_ConfigStore(builder, packet, "Display Name", "getDisplayName", "displayName");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Render Type", "getRenderType", "renderType");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Slot", "getSlot", "slot");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Method", "getMethod", "method");
            break;
         case "ClientboundSetPlayerTeamPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Scoreboard team update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Team", "getName", "name");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Method", "getMethod", "method");
            wrote |= write_Z_bu_ConfigStore(builder, packet, "Players", "getPlayers", "players");
            complete = false;
            break;
         case "ClientboundSetBorderCenterPacket":
         case "ClientboundSetBorderSizePacket":
         case "ClientboundSetBorderLerpSizePacket":
         case "ClientboundSetBorderWarningDelayPacket":
         case "ClientboundSetBorderWarningDistancePacket":
         case "ClientboundInitializeBorderPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "World border update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Center X", "getNewCenterX", "newCenterX", "centerX");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Center Z", "getNewCenterZ", "newCenterZ", "centerZ");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Size", "getSize", "size");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Old Size", "getOldSize", "oldSize");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "New Size", "getNewSize", "newSize");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Lerp Time", "getLerpTime", "lerpTime");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Warning Blocks", "getWarningBlocks", "warningBlocks");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Warning Time", "getWarningTime", "warningTime");
            break;
         case "ClientboundResourcePackPushPacket":
         case "ClientboundResourcePackPopPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Resource pack request/update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Id", "id", "getId");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Url", "url", "getUrl");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Hash", "hash", "getHash");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Required", "required", "isRequired");
            wrote |= read_Z_bu_ConfigStore(builder, packet, "Prompt", "prompt", "getPrompt");
            break;
         case "ClientboundUpdateRecipesPacket":
         case "ClientboundRecipeBookAddPacket":
         case "ClientboundRecipeBookRemovePacket":
         case "ClientboundRecipeBookSettingsPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Recipe book/recipe registry update");
            wrote |= write_Z_bu_ConfigStore(builder, packet, "Recipes", "recipes", "getRecipes", "entries");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Settings", "settings", "getSettings");
            complete = false;
            break;
         case "ClientboundUpdateTagsPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Registry tag update");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Tags", "tags", "getTags");
            complete = false;
            break;
         case "ServerboundPlayerCommandPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client sent player/entity command");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Entity", "getId", "id", "entityId");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Action", "getAction", "action");
            wrote |= init_Z_bu_ConfigStore(builder, "Data", init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "getData", "data"), init_Obj_Obj_I(packet, 2)));
            break;
         case "ServerboundPlayerInputPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client movement input state");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Input", "input");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Forward", "xxa", "forward");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Sideways", "zza", "sideways");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Jumping", "isJumping", "jumping");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Sneaking", "isShiftKeyDown", "shiftKeyDown", "sneaking");
            break;
         case "ServerboundPaddleBoatPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client boat paddle state");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Left", "getLeft", "left");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Right", "getRight", "right");
            break;
         case "ServerboundCommandSuggestionPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client requested command completions");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Transaction Id", "getId", "id");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Command", "getCommand", "command");
            break;
         case "ServerboundChatCommandPacket":
         case "ServerboundChatCommandSignedPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Outgoing command");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Command", "command", "getCommand");
            break;
         case "ServerboundPickItemFromBlockPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client pick-blocked a block");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Block Pos", "pos", "getPos");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Include Data", "includeData", "isIncludeData");
            break;
         case "ServerboundPickItemFromEntityPacket":
            wrote |= init_Z_bu_ConfigStore_Str(builder, "Client pick-blocked an entity");
            wrote |= load_Z_bu_ConfigStore(builder, packet, "Entity", "id", "getId", "entityId");
            wrote |= init_Z_bu_ConfigStore(builder, packet, "Include Data", "includeData", "isIncludeData");
            break;
         default:
            if (className.contains("ServerboundMovePlayerPacket")) {
               wrote |= init_Z_bu_ConfigStore_Str(builder, "Client player movement");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Pos", "getX", "x", "getY", "y", "getZ", "z");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "On Ground", "isOnGround", "onGround");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Horizontal Collision", "horizontalCollision");
            } else if (className.contains("ClientboundMoveEntityPacket")) {
               wrote |= init_Z_bu_ConfigStore_Str(builder, "Server moved or rotated an entity");
               wrote |= load_Z_bu_ConfigStore(builder, packet, "Entity Id", "getEntityId", "entityId", "id");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Delta X", "getXa", "xa");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Delta Y", "getYa", "ya");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Delta Z", "getZa", "za");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
               wrote |= init_Z_bu_ConfigStore(builder, packet, "On Ground", "isOnGround", "onGround");
            }
      }

      return new PacketInspectorEngine.PacketSummaryFlags(wrote, complete);
   }

   private static boolean init_Z_bu_ConfigStore_Str(PacketInspectorEngine.ReportBuilder builder, String meaning) {
      if (meaning != null && !meaning.isBlank()) {
         builder.load("Meaning: " + meaning, UiColorPalette.textBright());
         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, accessors), read_Obj_Obj_StrArr(packet, label));
      return init_Z_bu_ConfigStore(builder, label, value);
   }

   private static boolean init_Z_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, String label, Object value) {
      if (value != null && value != init_Obj) {
         builder.load(label + ": " + load_Str_Str_Obj(label, value), init_I_Str_Obj(label, value));
         return true;
      } else {
         return false;
      }
   }

   private static boolean load_Z_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, String label, Object value) {
      if (value != null && value != init_Obj) {
         builder.load(label + ": " + poll(value), init_I_Str_Obj(label, value));
         return true;
      } else {
         return false;
      }
   }

   private static boolean load_Z_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, accessors), read_Obj_Obj_StrArr(packet, label));
      if (value instanceof Number number) {
         init_V_bu_ConfigStore(builder, label, number.intValue());
         return true;
      } else if (value instanceof Entity entity) {
         builder.load(label + ": " + init_Str_Entity(entity), UiColorPalette.cyan());
         return true;
      } else {
         return init_Z_bu_ConfigStore(builder, label, value);
      }
   }

   private static boolean read_Z_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, accessors), read_Obj_Obj_StrArr(packet, label));
      if (value != null && value != init_Obj) {
         String text = update(value);
         builder.load(label + ": " + (text != null && !text.isBlank() ? open(poll(text)) : load_Str_Str_Obj(label, value)), load_I_Str_Obj(label, value));
         return true;
      } else {
         return false;
      }
   }

   private static boolean write_Z_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, accessors), read_Obj_Obj_StrArr(packet, label));
      if (value != null && value != init_Obj) {
         if (value instanceof Collection<?> collection) {
            builder.load(label + ": " + load_Str_Collection(collection), UiColorPalette.textBody());
         } else {
            builder.load(label + ": " + load_Str_Str_Obj(label, value), init_I_Str_Obj(label, value));
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_bu_ConfigStore(
      PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String xGetter, String xField, String yGetter, String yField, String zGetter, String zField
   ) {
      Object x = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, xGetter, xField), read_Obj_Obj_StrArr(packet, xField));
      Object y = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, yGetter, yField), read_Obj_Obj_StrArr(packet, yField));
      Object z = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, zGetter, zField), read_Obj_Obj_StrArr(packet, zField));
      if (x instanceof Number xn && y instanceof Number yn && z instanceof Number zn) {
         builder.load(label + ": " + init_Str_Vec3(new Vec3(xn.doubleValue(), yn.doubleValue(), zn.doubleValue())), UiColorPalette.warningYellow());
         return true;
      } else {
         return false;
      }
   }

   private static PacketInspectorEngine.PacketSummaryFlags load_bu_FileCache_Packet_bu_ConfigStore(Packet<?> packet, PacketInspectorEngine.ReportBuilder builder) {
      List<PacketInspectorEngine.PropertyEntry> properties = init_List_Obj(packet);
      if (properties.isEmpty()) {
         String rawFallback = read_Str_Obj(packet);
         if (rawFallback != null) {
            builder.load("Raw: " + rawFallback, UiColorPalette.textMuted());
            return new PacketInspectorEngine.PacketSummaryFlags(true, false);
         } else {
            return PacketInspectorEngine.PacketSummaryFlags.init_bu_FileCache;
         }
      } else {
         int lines = 0;

         for (PacketInspectorEngine.PropertyEntry property : properties) {
            if (lines >= 8) {
               break;
            }

            Object value = property.init_Obj();
            if (value != null && value != init_Obj) {
               String summary = load_Str_Str_Obj(property.init_Str(), value);
               if (summary != null && !summary.isBlank()) {
                  builder.load(property.init_Str() + ": " + summary, init_I_Str_Obj(property.init_Str(), value));
                  lines++;
               }
            }
         }

         return new PacketInspectorEngine.PacketSummaryFlags(lines > 0, false);
      }
   }

   private static PacketInspectorEngine.PacketSummaryFlags read_bu_FileCache_Packet(
      Packet<?> packet, String packetNameHint, AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.ReportBuilder builder
   ) {
      if (packet instanceof ServerboundPunchPacket) {
         builder.load("Interaction: Punch / Swing (attack / use animation)", UiColorPalette.textBright());
         if (init_Minecraft.player != null) {
            builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getMainHandItem()), UiColorPalette.successText());
         }

         return new PacketInspectorEngine.PacketSummaryFlags(true, true);
      }

      if (packet instanceof CompatSwingPacket || init_Z_Packet(packet, packetNameHint, "HandSwingC2S", "HandSwingC2SPacket")) {
         Object hand = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "getHand", "hand"), init_Obj_Obj_I(packet, 0), read_Obj_Obj_StrArr(packet, "Hand"));
         if (hand != null) {
            builder.load("Interaction: Swing InteractionHand (attack / use animation)", UiColorPalette.textBright());
            builder.load("Hand: " + load_Str_Obj(hand), UiColorPalette.textBright());
            if (init_Minecraft.player != null && hand instanceof InteractionHand playerHand) {
               builder.load("Held Item: " + init_Str_ItemStack(init_Minecraft.player.getItemInHand(playerHand)), UiColorPalette.successText());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSetCarriedItemPacket updateSelectedSlotPacket) {
         Object slot = load_Obj_Obj_StrArr(updateSelectedSlotPacket, "getSelectedSlot", "getSlot", "selectedSlot", "slot");
         if (slot != null) {
            if (slot instanceof Number number) {
               builder.load("Selected Slot: " + load_Str_I(number.intValue()), UiColorPalette.textBright());
            } else {
               builder.load("Selected Slot: " + load_Str_Obj(slot), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundChatPacket chatMessagePacket || init_Z_Packet(packet, packetNameHint, "ChatMessageC2S", "ChatMessageC2SPacket")) {
         Object message = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "chatMessage", "getChatMessage"), init_Obj_Obj_I(packet, 0));
         Object timestamp = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "timestamp", "getTimestamp"), init_Obj_Obj_I(packet, 1));
         Object salt = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "salt", "getSalt"), init_Obj_Obj_I(packet, 2));
         Object signature = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "signature", "getSignature"), init_Obj_Obj_I(packet, 3));
         Object acknowledgment = init_Obj_ObjArr(load_Obj_Obj_StrArr(packet, "acknowledgment", "getAcknowledgment"), init_Obj_Obj_I(packet, 4));
         if (message != null || timestamp != null || salt != null) {
            builder.load("Kind: Outgoing Signed Chat Message", UiColorPalette.textBright());
            if (init_Minecraft.player != null) {
               String senderName = init_Minecraft.player.getName() == null ? null : init_Minecraft.player.getName().getString();
               if (senderName != null && !senderName.isBlank()) {
                  builder.load("Sender: " + senderName, UiColorPalette.textBright());
               }
            }

            String text = update(message);
            if (text != null && !text.isBlank()) {
               builder.load("Message: " + open(poll(text)), UiColorPalette.successText());
            }

            if (timestamp != null) {
               builder.load("Timestamp: " + load_Str_Obj(timestamp), UiColorPalette.textBody());
            }

            if (salt != null) {
               builder.load("Salt: " + load_Str_Obj(salt), UiColorPalette.textBody());
            }

            if (signature != null) {
               builder.load("Signature: Present", UiColorPalette.textBody());
            }

            if (acknowledgment != null) {
               builder.load("Acknowledgment: " + poll(acknowledgment), load_I_Obj(acknowledgment));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundBlockChangedAckPacket
         || init_Z_Packet(packet, packetNameHint, "PlayerActionResponseS2C", "PlayerActionResponseS2CPacket")) {
         Object sequence = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(packet, "sequence", "getSequence"),
            init_Obj_Obj_I(packet, 0),
            init_Obj_Obj_Str(packet, "comp_633"),
            read_Obj_Obj_StrArr(packet, "Sequence")
         );
         if (sequence != null) {
            builder.load("Meaning: Server acknowledged a sequence-based player action", UiColorPalette.textBright());
            builder.load("Sequence: " + load_Str_Obj(sequence), UiColorPalette.textBright());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundContainerClosePacket
         || init_Z_Packet(packet, packetNameHint, "CloseAbstractContainerScreenC2S", "ServerboundContainerClosePacket")) {
         Object syncId = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(packet, "getSyncId", "syncId"),
            init_Obj_Obj_I(packet, 0),
            init_Obj_Obj_Str(packet, "field_12827"),
            read_Obj_Obj_StrArr(packet, "Sync Id", "SyncId")
         );
         if (syncId != null) {
            builder.load("Interaction: Close Handled Screen", UiColorPalette.textBright());
            builder.load("Sync Id: " + load_Str_Obj(syncId), UiColorPalette.textBright());
            if (entry != null && entry.update != null && !entry.update.isBlank()) {
               builder.load("Closed Screen: " + entry.update, UiColorPalette.successText());
            }

            if (syncId instanceof Number number && number.intValue() == 0) {
               builder.load("Likely Screen: Player Inventory / Survival Crafting", UiColorPalette.successText());
            }

            builder.load("Note: This packet only sends the sync id, not the closed screen title/type", UiColorPalette.textBody());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundSetEquipmentPacket
         || init_Z_Packet(packet, packetNameHint, "EntityEquipmentUpdateS2C", "EntityEquipmentUpdateS2CPacket")) {
         Object entityIdValue = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(packet, "getEntityId", "entityId", "id"),
            init_Obj_Obj_I(packet, 0),
            init_Obj_Obj_Str(packet, "field_12565"),
            read_Obj_Obj_StrArr(packet, "Entity Id", "EntityId")
         );
         Object equipmentListValue = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(packet, "getEquipmentList", "equipmentList"),
            init_Obj_Obj_I(packet, 1),
            init_Obj_Obj_Str(packet, "field_25721"),
            read_Obj_Obj_StrArr(packet, "Equipment List", "EquipmentList")
         );
         if (entityIdValue != null || equipmentListValue != null) {
            builder.load("Meaning: Server updated an entity's visible equipment", UiColorPalette.textBright());
            if (entityIdValue instanceof Number entityIdNumber) {
               init_V_bu_ConfigStore(builder, "Entity Id", entityIdNumber.intValue());
            } else if (entityIdValue != null) {
               builder.load("Entity Id: " + load_Str_Obj(entityIdValue), UiColorPalette.textBright());
            }

            if (equipmentListValue instanceof Collection<?> equipmentEntries) {
               builder.load("Changed Slots: " + equipmentEntries.size(), UiColorPalette.textBright());
               String equipmentSummary = read_Str_Collection(equipmentEntries);
               if (equipmentSummary != null && !equipmentSummary.isBlank()) {
                  builder.load("Equipment: " + equipmentSummary, UiColorPalette.successText());
               }
            } else if (equipmentListValue != null) {
               builder.load("Equipment: " + poll(equipmentListValue), load_I_Obj(equipmentListValue));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundContainerButtonClickPacket buttonClickPacket) {
         Object syncId = load_Obj_Obj_StrArr(buttonClickPacket, "getSyncId", "syncId");
         Object buttonId = load_Obj_Obj_StrArr(buttonClickPacket, "getButtonId", "buttonId");
         if (syncId != null || buttonId != null) {
            builder.load("Interaction: Click handled-screen button", UiColorPalette.textBright());
            if (syncId != null) {
               builder.load("Sync Id: " + load_Str_Obj(syncId), UiColorPalette.textBright());
            }

            if (buttonId != null) {
               builder.load("Button Id: " + load_Str_Obj(buttonId), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeInventoryActionPacket) {
         Object slot = load_Obj_Obj_StrArr(creativeInventoryActionPacket, "getSlot", "slot");
         Object stack = load_Obj_Obj_StrArr(creativeInventoryActionPacket, "getStack", "stack");
         if (slot != null || stack != null) {
            if (slot != null) {
               builder.load("Slot: " + load_Str_Obj(slot), UiColorPalette.textBright());
            }

            if (stack != null) {
               builder.load("Stack: " + poll(stack), load_I_Obj(stack));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundContainerClickPacket clickSlotPacket) {
         Object syncId = load_Obj_Obj_StrArr(clickSlotPacket, "getSyncId", "syncId");
         Object revision = load_Obj_Obj_StrArr(clickSlotPacket, "getRevision", "revision");
         Object slot = load_Obj_Obj_StrArr(clickSlotPacket, "getSlot", "slot");
         Object button = load_Obj_Obj_StrArr(clickSlotPacket, "getButton", "button");
         Object actionType = load_Obj_Obj_StrArr(clickSlotPacket, "getActionType", "actionType");
         Object modifiedStacks = load_Obj_Obj_StrArr(clickSlotPacket, "getModifiedStacks", "modifiedStacks");
         Object cursorStack = load_Obj_Obj_StrArr(clickSlotPacket, "cursor", "getStack", "getCursorStack", "cursorStack");
         if (syncId != null || slot != null || actionType != null) {
            builder.load("Interaction: " + write_Str_Obj(actionType), UiColorPalette.textBright());
            if (syncId != null) {
               builder.load("Sync Id: " + load_Str_Obj(syncId), UiColorPalette.textBright());
            }

            if (revision != null) {
               builder.load("Revision: " + load_Str_Obj(revision), UiColorPalette.textBody());
            }

            if (slot != null) {
               builder.load("Slot: " + load_Str_Obj(slot), UiColorPalette.textBright());
            }

            if (button != null) {
               builder.load("Button: " + load_Str_Obj(button), UiColorPalette.textBright());
            }

            if (actionType != null) {
               builder.load("Action Type: " + load_Str_Obj(actionType), UiColorPalette.textBright());
            }

            if (cursorStack != null) {
               builder.load("Cursor: " + poll(cursorStack), load_I_Obj(cursorStack));
            }

            if (modifiedStacks instanceof Map<?, ?> map) {
               builder.load("Changed Slots: " + map.size(), UiColorPalette.textBody());
            } else if (modifiedStacks != null) {
               builder.load("Changed Slots: " + poll(modifiedStacks), load_I_Obj(modifiedStacks));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, false);
         }
      }

      if (packet instanceof ServerboundEditBookPacket bookUpdatePacket) {
         Object slot = load_Obj_Obj_StrArr(bookUpdatePacket, "slot", "getSlot");
         Object pages = load_Obj_Obj_StrArr(bookUpdatePacket, "pages", "getPages");
         Object title = load_Obj_Obj_StrArr(bookUpdatePacket, "title", "getTitle");
         if (slot != null || pages != null || title != null) {
            boolean signing = title instanceof Optional<?> optional && optional.isPresent();
            builder.load("Interaction: " + (signing ? "Sign Book" : "Edit Book"), UiColorPalette.textBright());
            if (slot instanceof Number number) {
               builder.load("Slot: " + load_Str_I(number.intValue()), UiColorPalette.textBright());
            } else if (slot != null) {
               builder.load("Slot: " + load_Str_Obj(slot), UiColorPalette.textBright());
            }

            if (title != null) {
               String titleComponent = update(title);
               if (titleComponent != null && !titleComponent.isBlank()) {
                  builder.load("Title: " + open(poll(titleComponent)), UiColorPalette.successText());
               }
            }

            if (pages instanceof Collection<?> collection) {
               builder.load("Pages: " + collection.size(), UiColorPalette.textBright());
               String preview = init_Str_Collection(collection);
               if (preview != null) {
                  builder.load("Preview: " + preview, UiColorPalette.textBody());
               }
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSignUpdatePacket updateSignPacket) {
         Object pos = load_Obj_Obj_StrArr(updateSignPacket, "getPos", "pos");
         Object lines = load_Obj_Obj_StrArr(updateSignPacket, "getText", "text");
         Object front = load_Obj_Obj_StrArr(updateSignPacket, "isFront", "front");
         if (pos != null || lines != null || front != null) {
            builder.load("Interaction: Update Sign Component", UiColorPalette.textBright());
            if (pos != null) {
               builder.load("Block Pos: " + poll(pos), load_I_Obj(pos));
            }

            if (front != null) {
               builder.load("Side: " + (Boolean.TRUE.equals(front) ? "Front" : "Back"), UiColorPalette.textBright());
            }

            if (lines instanceof String[] stringLines) {
               builder.load("Component: " + load_Str_StrArr(stringLines), UiColorPalette.successText());
            } else if (lines != null) {
               builder.load("Component: " + poll(lines), load_I_Obj(lines));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundRenameItemPacket renameItemPacket) {
         Object name = load_Obj_Obj_StrArr(renameItemPacket, "getName", "name");
         if (name != null) {
            builder.load("Interaction: Rename Item in Anvil", UiColorPalette.textBright());
            builder.load("Name: " + open(poll(String.valueOf(name))), UiColorPalette.successText());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundPlaceRecipePacket craftRequestPacket) {
         Object syncId = load_Obj_Obj_StrArr(craftRequestPacket, "syncId", "getSyncId");
         Object recipeId = load_Obj_Obj_StrArr(craftRequestPacket, "recipeId", "getRecipeId");
         Object craftAll = load_Obj_Obj_StrArr(craftRequestPacket, "craftAll", "isCraftAll", "getCraftAll");
         if (syncId != null || recipeId != null || craftAll != null) {
            builder.load("Interaction: Craft Recipe", UiColorPalette.textBright());
            if (syncId != null) {
               builder.load("Sync Id: " + load_Str_Obj(syncId), UiColorPalette.textBright());
            }

            if (recipeId != null) {
               builder.load("Recipe: " + load_Str_Obj(recipeId), UiColorPalette.successText());
            }

            if (craftAll != null) {
               builder.load("Craft All: " + load_Str_Obj(craftAll), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSelectTradePacket selectMerchantTradePacket) {
         Object tradeId = load_Obj_Obj_StrArr(selectMerchantTradePacket, "getTradeId", "tradeId");
         if (tradeId != null) {
            builder.load("Interaction: Select Merchant Trade", UiColorPalette.textBright());
            builder.load("Trade Index: " + load_Str_Obj(tradeId), UiColorPalette.textBright());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundContainerSetDataPacket screenHandlerPropertyUpdatePacket) {
         Object syncId = load_Obj_Obj_StrArr(screenHandlerPropertyUpdatePacket, "getSyncId", "syncId");
         Object property = load_Obj_Obj_StrArr(screenHandlerPropertyUpdatePacket, "getPropertyId", "propertyId", "getProperty", "property");
         Object value = load_Obj_Obj_StrArr(screenHandlerPropertyUpdatePacket, "getValue", "value");
         if (syncId != null || property != null || value != null) {
            if (syncId != null) {
               builder.load("Sync Id: " + load_Str_Obj(syncId), UiColorPalette.textBright());
            }

            if (property != null) {
               builder.load("Property: " + load_Str_Obj(property), UiColorPalette.textBright());
            }

            if (value != null) {
               builder.load("Value: " + load_Str_Obj(value), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundBlockDestructionPacket blockBreakingProgressPacket) {
         Object entityId = load_Obj_Obj_StrArr(blockBreakingProgressPacket, "getEntityId", "entityId");
         Object pos = load_Obj_Obj_StrArr(blockBreakingProgressPacket, "getPos", "pos");
         Object progress = load_Obj_Obj_StrArr(blockBreakingProgressPacket, "getProgress", "progress");
         if (entityId != null || pos != null || progress != null) {
            if (entityId instanceof Number number) {
               init_V_bu_ConfigStore(builder, "Entity Id", number.intValue());
            } else if (entityId != null) {
               builder.load("Entity Id: " + load_Str_Obj(entityId), UiColorPalette.textBright());
            }

            if (pos != null) {
               builder.load("Block Pos: " + poll(pos), load_I_Obj(pos));
            }

            if (progress != null) {
               builder.load("Progress: " + load_Str_Obj(progress), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundEntityEventPacket entityStatusPacket) {
         Object entityId = load_Obj_Obj_StrArr(entityStatusPacket, "getEntityId", "entityId");
         if (entityId == null && init_Minecraft.level != null) {
            Entity entity = entityStatusPacket.getEntity(init_Minecraft.level);
            if (entity != null) {
               entityId = entity.getId();
            }
         }

         Object status = load_Obj_Obj_StrArr(entityStatusPacket, "getStatus", "status");
         if (status == null) {
            status = load_Obj_Obj_StrArr(entityStatusPacket, "getEventId", "eventId");
         }

         if (entityId != null || status != null) {
            if (entityId instanceof Number number) {
               init_V_bu_ConfigStore(builder, "Entity Id", number.intValue());
            } else if (entityId != null) {
               builder.load("Entity Id: " + load_Str_Obj(entityId), UiColorPalette.textBright());
            }

            if (status != null) {
               builder.load("Status: " + load_Str_Obj(status), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundSetEntityMotionPacket entityVelocityUpdatePacket) {
         Object entityId = load_Obj_Obj_StrArr(entityVelocityUpdatePacket, "getEntityId", "entityId", "id");
         Object velocity = load_Obj_Obj_StrArr(entityVelocityUpdatePacket, "getVelocity", "velocity");
         if (velocity == null) {
            velocity = load_Obj_Obj_StrArr(entityVelocityUpdatePacket, "movement");
         }

         if (entityId != null || velocity != null) {
            if (entityId instanceof Number number) {
               init_V_bu_ConfigStore(builder, "Entity Id", number.intValue());
            } else if (entityId != null) {
               builder.load("Entity Id: " + load_Str_Obj(entityId), UiColorPalette.textBright());
            }

            if (velocity != null) {
               builder.load("Velocity: " + poll(velocity), load_I_Obj(velocity));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundSystemChatPacket gameMessagePacket) {
         Object content = load_Obj_Obj_StrArr(gameMessagePacket, "content", "getContent");
         Object overlay = load_Obj_Obj_StrArr(gameMessagePacket, "overlay", "isOverlay", "getOverlay");
         if (content != null || overlay != null) {
            String message = update(content);
            builder.load("Kind: " + (Boolean.TRUE.equals(overlay) ? "Overlay / HUD message" : "Game / system message"), UiColorPalette.textBright());
            PacketInspectorEngine.ParsedMessagePair parsed = init_bu_StateCache_Str(message);
            if (parsed != null) {
               builder.load("Possible Sender: " + parsed.init(), UiColorPalette.textBright());
               builder.load("Message: " + open(poll(parsed.load())), UiColorPalette.successText());
            } else if (message != null && !message.isBlank()) {
               builder.load("Message: " + open(poll(message)), UiColorPalette.successText());
            } else if (content != null) {
               builder.load("Message: " + poll(content), load_I_Obj(content));
            }

            if (overlay != null) {
               builder.load("Overlay: " + load_Str_Obj(overlay), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundDisguisedChatPacket profilelessChatMessagePacket) {
         Object message = load_Obj_Obj_StrArr(profilelessChatMessagePacket, "message", "getMessage");
         Object chatType = load_Obj_Obj_StrArr(profilelessChatMessagePacket, "chatType", "getChatType");
         PacketInspectorEngine.DecodeCheckPair chatSummary = init_bu_StreamUtil_Obj(message, chatType, null, null, null, "Incoming Chat Message", builder);
         if (chatSummary.init()) {
            return new PacketInspectorEngine.PacketSummaryFlags(true, chatSummary.load());
         }
      }

      if (packet instanceof ClientboundPlayerChatPacket chatMessageS2CPacket) {
         Object body = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(chatMessageS2CPacket, "body", "getBody"),
            init_Obj_Obj_I(chatMessageS2CPacket, 4),
            init_Obj_Obj(chatMessageS2CPacket, 2, "content", "timestamp", "salt")
         );
         Object serializedParameters = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(chatMessageS2CPacket, "serializedParameters", "getSerializedParameters"),
            init_Obj_Obj_I(chatMessageS2CPacket, 7),
            init_Obj_Obj(chatMessageS2CPacket, 2, "name", "targetName", "type")
         );
         Object sender = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(chatMessageS2CPacket, "sender", "getSender"),
            init_Obj_Obj_I(chatMessageS2CPacket, 1),
            init_Obj_Obj_Class(chatMessageS2CPacket, UUID.class)
         );
         Object filterMask = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(chatMessageS2CPacket, "filterMask", "getFilterMask"),
            init_Obj_Obj_I(chatMessageS2CPacket, 6),
            init_Obj_Obj(chatMessageS2CPacket, 1, "isFullyFiltered", "isPassThrough")
         );
         Object unsignedContent = init_Obj_ObjArr(
            load_Obj_Obj_StrArr(chatMessageS2CPacket, "unsignedContent", "getUnsignedContent"),
            init_Obj_Obj_I(chatMessageS2CPacket, 5),
            init_Obj_Obj_ObjArr(chatMessageS2CPacket, body, serializedParameters, sender, filterMask)
         );
         Object message = unsignedContent;
         Object timestamp = null;
         if (body != null) {
            Object bodyContent = init_Obj_ObjArr(load_Obj_Obj_StrArr(body, "content", "getContent"), init_Obj_Obj_I(body, 0), init_Obj_Obj_ObjArr(body));
            if (bodyContent != null && (message == null || update(message) == null || update(message).isBlank())) {
               message = bodyContent;
            }

            timestamp = init_Obj_ObjArr(
               load_Obj_Obj_StrArr(body, "timestamp", "getTimestamp"), init_Obj_Obj_I(body, 1), init_Obj_Obj_Class(body, Instant.class)
            );
         }

         PacketInspectorEngine.DecodeCheckPair chatSummary = init_bu_StreamUtil_Obj(
            message, serializedParameters, sender instanceof UUID uuid ? uuid : null, timestamp, filterMask, "Incoming Signed Chat Message", builder
         );
         if (chatSummary.init()) {
            return new PacketInspectorEngine.PacketSummaryFlags(true, chatSummary.load());
         }
      }

      if (packet instanceof ClientboundSetActionBarTextPacket overlayMessagePacket) {
         Object text = load_Obj_Obj_StrArr(overlayMessagePacket, "text", "getText");
         String message = update(text);
         if (message != null || text != null) {
            builder.load("Kind: Overlay / HUD message", UiColorPalette.textBright());
            builder.load("Message: " + (message == null ? poll(text) : open(poll(message))), message == null ? load_I_Obj(text) : UiColorPalette.successText());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundOpenBookPacket openWrittenBookPacket) {
         Object hand = load_Obj_Obj_StrArr(openWrittenBookPacket, "getHand", "hand");
         if (hand != null) {
            builder.load("Interaction: Open Written Book", UiColorPalette.textBright());
            builder.load("Hand: " + load_Str_Obj(hand), UiColorPalette.textBright());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundDisconnectPacket disconnectPacket) {
         Object reason = load_Obj_Obj_StrArr(disconnectPacket, "reason", "getReason");
         if (reason != null) {
            builder.load("Reason: " + poll(reason), load_I_Obj(reason));
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundMovePlayerPacket playerMovePacket) {
         Object x = load_Obj_Obj_StrArr(playerMovePacket, "getX", "x");
         Object y = load_Obj_Obj_StrArr(playerMovePacket, "getY", "y");
         Object z = load_Obj_Obj_StrArr(playerMovePacket, "getZ", "z");
         Object yaw = load_Obj_Obj_StrArr(playerMovePacket, "getYaw", "yaw");
         Object pitch = load_Obj_Obj_StrArr(playerMovePacket, "getPitch", "pitch");
         Object onGround = load_Obj_Obj_StrArr(playerMovePacket, "isOnGround", "onGround");
         if (x != null || y != null || z != null || yaw != null || pitch != null || onGround != null) {
            if (x != null || y != null || z != null) {
               builder.load("Pos: x=" + load_Str_Obj(x) + ", y=" + load_Str_Obj(y) + ", z=" + load_Str_Obj(z), UiColorPalette.textBright());
            }

            if (yaw != null || pitch != null) {
               builder.load("Rot: yaw=" + load_Str_Obj(yaw) + ", pitch=" + load_Str_Obj(pitch), UiColorPalette.textBright());
            }

            if (onGround != null) {
               builder.load("On Ground: " + load_Str_Obj(onGround), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      return PacketInspectorEngine.PacketSummaryFlags.init_bu_FileCache;
   }

   private static void init_V_Obj(Object root, PacketInspectorEngine.ReportBuilder builder, IdentityHashMap<Object, Boolean> visited) {
      visited.put(root, Boolean.TRUE);
      List<PacketInspectorEngine.PropertyEntry> properties = init_List_Obj(root);
      List<Field> fields = init_List_Class(root.getClass());
      Set<String> consumedFieldNames = new LinkedHashSet<>();

      for (PacketInspectorEngine.PropertyEntry property : properties) {
         init_V_Str(property.init_Str(), property.init_Obj(), 0, builder, visited);
         consumedFieldNames.add(update_Str(sync_Str(property.init_Str())));
      }

      boolean wroteAny = !properties.isEmpty();

      for (Field field : fields) {
         String fieldLabel = init_Str_Field(field);
         if (fieldLabel != null && !fieldLabel.isBlank() && !consumedFieldNames.contains(update_Str(sync_Str(fieldLabel)))) {
            init_V_Str(fieldLabel, init_Obj_Field_Obj(field, root), 0, builder, visited);
            wroteAny = true;
         }
      }

      if (!wroteAny) {
         String rawFallback = read_Str_Obj(root);
         if (rawFallback != null) {
            builder.load("Raw: " + rawFallback, UiColorPalette.textMuted());
         } else {
            builder.load("<No readable fields>", UiColorPalette.textMuted());
         }
      }
   }

   private static void init_V_Str(String label, Object value, int depth, PacketInspectorEngine.ReportBuilder builder, IdentityHashMap<Object, Boolean> visited) {
      String indent = "  ".repeat(Math.max(0, depth));

      try {
         init_V_Str(label, value, depth, indent, builder, visited);
      } catch (Throwable t) {
         builder.load(indent + label + ": <error reading value: " + t.getClass().getSimpleName() + ">", UiColorPalette.errorText());
      }
   }

   private static void init_V_Str(String label, Object value, int depth, String indent, PacketInspectorEngine.ReportBuilder builder, IdentityHashMap<Object, Boolean> visited) {
      if (!builder.init_Z()) {
         if (value == init_Obj) {
            builder.load(indent + label + ": <inaccessible>", UiColorPalette.textMuted());
         } else if (value == null) {
            builder.load(indent + label + ": null", UiColorPalette.textMuted());
         } else if (depth >= 7) {
            builder.load(indent + label + ": <max depth reached>", UiColorPalette.textMuted());
         } else {
            Class<?> valueClass = value.getClass();
            if (init_Z_Obj(value)) {
               builder.load(indent + label + ": " + init_Str_Str_Obj(label, value), load_I_Str_Obj(label, value));
            } else if (value instanceof Optional<?> optional) {
               if (optional.isEmpty()) {
                  builder.load(indent + label + ": Optional.empty", UiColorPalette.textMuted());
               } else {
                  builder.load(indent + label + ": Optional", UiColorPalette.textBody());
                  init_V_Str("value", optional.get(), depth + 1, builder, visited);
               }
            } else if (value instanceof BlockHitResult hit) {
               builder.load(indent + label + ": BlockHitResult", UiColorPalette.textBody());
               init_V_Str("blockPos", hit.getBlockPos(), depth + 1, builder, visited);
               init_V_Str("hitPos", hit.getLocation(), depth + 1, builder, visited);
               init_V_Str("side", hit.getDirection(), depth + 1, builder, visited);
               init_V_Str("type", hit.getType(), depth + 1, builder, visited);
               init_V_Str("insideBlock", hit.isInside(), depth + 1, builder, visited);
            } else if (value instanceof Map<?, ?> map) {
               builder.load(indent + label + ": Map[" + map.size() + "]", UiColorPalette.textBody());
               int index = 0;

               for (Entry<?, ?> entry : map.entrySet()) {
                  if (index >= 64) {
                     builder.load(indent + "  ... " + (map.size() - index) + " more entries", UiColorPalette.textMuted());
                     return;
                  }

                  if (init_Z_Obj(entry.getKey()) && init_Z_Obj(entry.getValue())) {
                     builder.load(indent + "  [" + index + "] " + init_Str_Obj(entry.getKey()) + " = " + init_Str_Obj(entry.getValue()), UiColorPalette.textBright());
                  } else {
                     builder.load(indent + "  [" + index + "]", UiColorPalette.textBody());
                     init_V_Str("key", entry.getKey(), depth + 2, builder, visited);
                     init_V_Str("value", entry.getValue(), depth + 2, builder, visited);
                  }

                  index++;
               }
            } else if (value instanceof Collection<?> collection) {
               builder.load(indent + label + ": List[" + collection.size() + "]", UiColorPalette.textBody());
               int index = 0;

               for (Object item : collection) {
                  if (index >= 64) {
                     builder.load(indent + "  ... " + (collection.size() - index) + " more entries", UiColorPalette.textMuted());
                     return;
                  }

                  init_V_Str("[" + index + "]", item, depth + 1, builder, visited);
                  index++;
               }
            } else if (valueClass.isArray()) {
               int length = Array.getLength(value);
               if (valueClass.getComponentType().isPrimitive()) {
                  builder.load(indent + label + ": " + push(value), UiColorPalette.textBright());
               } else {
                  builder.load(indent + label + ": " + load_Str_Class(valueClass) + "[" + length + "]", UiColorPalette.textBody());

                  for (int i = 0; i < Math.min(length, 64); i++) {
                     init_V_Str("[" + i + "]", Array.get(value, i), depth + 1, builder, visited);
                  }

                  if (length > 64) {
                     builder.load(indent + "  ... " + (length - 64) + " more entries", UiColorPalette.textMuted());
                  }
               }
            } else if (visited.containsKey(value)) {
               builder.load(indent + label + ": <already shown " + load_Str_Class(valueClass) + ">", UiColorPalette.textMuted());
            } else if (init_Z_Class(valueClass)) {
               builder.load(indent + label + ": " + load_Str_Obj(value), load_I_Str_Obj(label, value));
            } else {
               visited.put(value, Boolean.TRUE);
               List<PacketInspectorEngine.PropertyEntry> properties = init_List_Obj(value);
               List<Field> fields = init_List_Class(valueClass);
               if (properties.isEmpty() && fields.isEmpty()) {
                  builder.load(indent + label + ": " + load_Str_Obj(value), load_I_Str_Obj(label, value));
               } else {
                  builder.load(indent + label + ": " + load_Str_Class(valueClass), UiColorPalette.textBody());
                  Set<String> consumedFieldNames = new LinkedHashSet<>();

                  for (PacketInspectorEngine.PropertyEntry property : properties) {
                     init_V_Str(property.init_Str(), property.init_Obj(), depth + 1, builder, visited);
                     consumedFieldNames.add(update_Str(sync_Str(property.init_Str())));
                  }

                  for (Field field : fields) {
                     String fieldLabel = init_Str_Field(field);
                     if (fieldLabel != null && !fieldLabel.isBlank() && !consumedFieldNames.contains(update_Str(sync_Str(fieldLabel)))) {
                        init_V_Str(fieldLabel, init_Obj_Field_Obj(field, value), depth + 1, builder, visited);
                     }
                  }
               }
            }
         }
      }
   }

   private static List<PacketInspectorEngine.PropertyEntry> init_List_Obj(Object owner) {
      if (owner == null) {
         return List.of();
      }

      Map<String, PacketInspectorEngine.PropertyEntry> properties = new LinkedHashMap<>();
      Class<?> type = owner.getClass();
      if (type.isRecord()) {
         for (RecordComponent component : type.getRecordComponents()) {
            Object value = init_Obj_Method_Obj(component.getAccessor(), owner);
            init_V_Map(properties, init_Str_Class_RecordComponent(type, component), value, PacketInspectorEngine.PropertySource.RECORD);
         }
      }

      Arrays.stream(type.getMethods())
         .filter(method -> init_Z_Method(method))
         .sorted(Comparator.comparing(Method::getName, String.CASE_INSENSITIVE_ORDER))
         .forEach(method -> init_V_Map(properties, init_Str_Method(method), init_Obj_Method_Obj(method, owner), PacketInspectorEngine.PropertySource.METHOD));
      if (properties.isEmpty()) {
         init_V_Map(properties, owner, type);
      }

      return new ArrayList<>(properties.values());
   }

   private static void init_V_Map(Map<String, PacketInspectorEngine.PropertyEntry> properties, Object owner, Class<?> type) {
      if (type.isRecord()) {
         Map<String, Integer> labelCounts = new LinkedHashMap<>();

         for (RecordComponent component : type.getRecordComponents()) {
            Object value = init_Obj_Method_Obj(component.getAccessor(), owner);
            String alias = init_Str_StrArr(
               init_Str_Class_RecordComponent(type, component), init_Str_Class_Map(component.getAccessor().getReturnType(), labelCounts)
            );
            init_V_Map(properties, alias, value, PacketInspectorEngine.PropertySource.RECORD);
         }
      }
   }

   private static String init_Str_Class_Map(Class<?> type, Map<String, Integer> labelCounts) {
      String base = init_Str_Class(type);
      int count = labelCounts.getOrDefault(base, 0) + 1;
      labelCounts.put(base, count);
      return count == 1 ? base : base + " " + count;
   }

   private static String init_Str_Class(Class<?> type) {
      if (type == null) {
         return "Value";
      }

      if (type == boolean.class || type == Boolean.class) {
         return "Flag";
      }

      if (type == String.class || Component.class.isAssignableFrom(type)) {
         return "Component";
      }

      if (type == UUID.class) {
         return "Uuid";
      }

      if (type == Identifier.class) {
         return "Identifier";
      }

      if (type == BlockPos.class) {
         return "Block Pos";
      }

      if (type == Vec3.class) {
         return "Position";
      }

      if (type == ItemStack.class) {
         return "Item";
      }

      if (type == Tag.class) {
         return "Nbt";
      }

      if (type == Instant.class) {
         return "Timestamp";
      }

      if (type.isEnum()) {
         return "Type";
      }

      if (type.isPrimitive()) {
         if (type == int.class || type == long.class || type == short.class || type == byte.class) {
            return "Number";
         }

         if (type == float.class || type == double.class) {
            return "Decimal";
         }

         if (type == char.class) {
            return "Character";
         }
      }

      if (Number.class.isAssignableFrom(type)) {
         return "Number";
      } else if (Collection.class.isAssignableFrom(type)) {
         return "Entries";
      } else if (Map.class.isAssignableFrom(type)) {
         return "Map";
      } else if (Optional.class.isAssignableFrom(type)) {
         return "Optional";
      } else {
         return type.isArray() ? init_Str_Class(type.getComponentType()) + " List" : load_Str_Class(type);
      }
   }

   private static void init_V_Map(Map<String, PacketInspectorEngine.PropertyEntry> properties, String label, Object value, PacketInspectorEngine.PropertySource source) {
      if (label != null && !label.isBlank() && !sync_Z(label) && !flush_Z(label)) {
         String key = sync_Str(label);
         if (!properties.containsKey(key)) {
            properties.put(key, new PacketInspectorEngine.PropertyEntry(write_Str_Str(label), value, source));
         }
      }
   }

   private static boolean init_Z_Method(Method method) {
      int modifiers = method.getModifiers();
      if (!Modifier.isPublic(modifiers)) {
         return false;
      }

      if (Modifier.isStatic(modifiers)) {
         return false;
      }

      if (method.isSynthetic()) {
         return false;
      }

      if (method.getParameterCount() != 0) {
         return false;
      }

      if (method.getReturnType() == void.class) {
         return false;
      }

      if (flush_Z(method.getName())) {
         return false;
      }

      String mappedLabel = init_Str_Str_Class(InspectorMappingsIndex.init(method), method.getReturnType());
      return flush_Z(mappedLabel)
         ? false
         : load_Z_Method(method)
            || read_Set.contains(method.getName())
            || mappedLabel != null && read_Set.contains(mappedLabel)
            || mappedLabel != null && !sync_Z(mappedLabel);
   }

   private static boolean load_Z_Method(Method method) {
      String name = method.getName();
      return name.startsWith("get") && name.length() > 3
         ? true
         : name.startsWith("is") && name.length() > 2 && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class);
   }

   private static String init_Str_Method(Method method) {
      String mappedLabel = init_Str_Str_Class(InspectorMappingsIndex.init(method), method.getReturnType());
      return mappedLabel != null && !mappedLabel.isBlank() ? mappedLabel : init_Str_Str_Class(method.getName(), method.getReturnType());
   }

   private static Object init_Obj_Method_Obj(Method method, Object owner) {
      try {
         if (!method.canAccess(owner)) {
            method.setAccessible(true);
         }

         return method.invoke(owner);
      } catch (Throwable ignored) {
         return init_Obj;
      }
   }

   private static List<Field> init_List_Class(Class<?> type) {
      List<Field> fields = new ArrayList<>();

      for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
         fields.addAll(
            Arrays.stream(cursor.getDeclaredFields())
               .filter(field -> !Modifier.isStatic(field.getModifiers()))
               .filter(field -> !Modifier.isTransient(field.getModifiers()))
               .filter(field -> !field.isSynthetic())
               .filter(field -> {
                  String fieldLabel = init_Str_Field(field);
                  return fieldLabel != null && !fieldLabel.isBlank();
               })
               .sorted((a, b) -> init_Str_Field(a).compareToIgnoreCase(init_Str_Field(b)))
               .collect(Collectors.toList())
         );
      }

      return fields;
   }

   private static Object init_Obj_Field_Obj(Field field, Object owner) {
      try {
         if (!field.canAccess(owner)) {
            field.setAccessible(true);
         }

         return field.get(owner);
      } catch (Throwable ignored) {
         return init_Obj;
      }
   }

   private static Object init_Obj_Obj_Str(Object owner, String fieldName) {
      if (owner != null && fieldName != null) {
         for (Class<?> cursor = owner.getClass(); cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            try {
               Field field = cursor.getDeclaredField(fieldName);
               if (!field.canAccess(owner)) {
                  field.setAccessible(true);
               }

               return field.get(owner);
            } catch (Throwable var4) {
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static Object init_Obj_Obj_StrArr(Object owner, String... fieldNames) {
      for (String fieldName : fieldNames) {
         Object value = init_Obj_Obj_Str(owner, fieldName);
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   private static Object init_Obj_ObjArr(Object... values) {
      if (values == null) {
         return null;
      }

      for (Object value : values) {
         if (value != null && value != init_Obj) {
            return value;
         }
      }

      return null;
   }

   private static String init_Str_StrArr(String... values) {
      if (values == null) {
         return null;
      }

      for (String value : values) {
         if (value != null && !value.isBlank()) {
            return value;
         }
      }

      return null;
   }

   private static String init_Str_Class_RecordComponent(Class<?> ownerType, RecordComponent component) {
      return component == null ? null : init_Str_StrArr(InspectorMappingsIndex.init(component.getAccessor()), component.getName());
   }

   private static String init_Str_Field(Field field) {
      return field == null ? null : init_Str_StrArr(InspectorMappingsIndex.init(field), sync_Z(field.getName()) ? null : field.getName());
   }

   private static Object init_Obj_Obj_I(Object owner, int index) {
      if (owner != null && index >= 0) {
         try {
            Class<?> type = owner.getClass();
            if (!type.isRecord()) {
               return null;
            }

            RecordComponent[] components = type.getRecordComponents();
            return components != null && index < components.length ? init_Obj_Method_Obj(components[index].getAccessor(), owner) : null;
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Object init_Obj_Obj_Class(Object owner, Class<?> expectedType) {
      if (owner != null && expectedType != null) {
         try {
            Class<?> type = owner.getClass();
            if (!type.isRecord()) {
               return null;
            }

            RecordComponent[] components = type.getRecordComponents();
            if (components == null) {
               return null;
            }

            for (RecordComponent component : components) {
               Object value = init_Obj_Method_Obj(component.getAccessor(), owner);
               if (value != null && value != init_Obj && expectedType.isInstance(value)) {
                  return value;
               }
            }
         } catch (Throwable var9) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static Object init_Obj_Obj(Object owner, int requiredMatches, String... methodNames) {
      if (owner != null && requiredMatches > 0 && methodNames != null && methodNames.length != 0) {
         try {
            Class<?> type = owner.getClass();
            if (!type.isRecord()) {
               return null;
            }

            RecordComponent[] components = type.getRecordComponents();
            if (components == null) {
               return null;
            }

            for (RecordComponent component : components) {
               Object value = init_Obj_Method_Obj(component.getAccessor(), owner);
               if (value != null && value != init_Obj) {
                  int matches = 0;

                  for (String methodName : methodNames) {
                     if (init_Z_Class_Str(value.getClass(), methodName)) {
                        matches++;
                     }
                  }

                  if (matches >= requiredMatches) {
                     return value;
                  }
               }
            }
         } catch (Throwable var15) {
         }

         return null;
      } else {
         return null;
      }
   }

   private static Object init_Obj_Obj_ObjArr(Object owner, Object... excludedValues) {
      if (owner == null) {
         return null;
      }

      try {
         Class<?> type = owner.getClass();
         if (!type.isRecord()) {
            return null;
         }

         RecordComponent[] components = type.getRecordComponents();
         if (components == null) {
            return null;
         }

         for (RecordComponent component : components) {
            Object value = init_Obj_Method_Obj(component.getAccessor(), owner);
            if (value != null && value != init_Obj && !init_Z_Obj_ObjArr(value, excludedValues)) {
               String text = update(value);
               if (text != null && !text.isBlank()) {
                  return value;
               }
            }
         }
      } catch (Throwable var10) {
      }

      return null;
   }

   private static Integer init_Integer_Obj_Str(Object owner, String fieldName) {
      return init_Obj_Obj_Str(owner, fieldName) instanceof Number number ? number.intValue() : null;
   }

   private static Boolean init_Boolean_Obj_Str(Object owner, String fieldName) {
      return init_Obj_Obj_Str(owner, fieldName) instanceof Boolean bool ? bool : null;
   }

   private static Object load_Obj_Obj_Str(Object owner, String methodName) {
      if (owner != null && methodName != null) {
         try {
            Method method = owner.getClass().getMethod(methodName);
            if (!method.canAccess(owner)) {
               method.setAccessible(true);
            }

            return method.invoke(owner);
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Object load_Obj_Obj_StrArr(Object owner, String... methodNames) {
      for (String methodName : methodNames) {
         Object value = load_Obj_Obj_Str(owner, methodName);
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   private static Object read_Obj_Obj_StrArr(Object owner, String... labels) {
      if (owner != null && labels != null && labels.length != 0) {
         List<PacketInspectorEngine.PropertyEntry> properties = init_List_Obj(owner);

         for (String label : labels) {
            String normalizedTarget = sync_Str(label);

            for (PacketInspectorEngine.PropertyEntry property : properties) {
               if (normalizedTarget.equals(sync_Str(property.init_Str()))) {
                  Object value = property.init_Obj();
                  if (value != null && value != init_Obj) {
                     return value;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean init_Z_Class_Str(Class<?> type, String methodName) {
      if (type != null && methodName != null && !methodName.isBlank()) {
         try {
            type.getMethod(methodName);
            return true;
         } catch (Throwable ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean init_Z_Obj_ObjArr(Object value, Object... excludedValues) {
      if (value != null && excludedValues != null) {
         for (Object excluded : excludedValues) {
            if (value == excluded) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init_Z_Packet(Packet<?> packet, String packetNameHint, String... names) {
      if (packetNameHint != null) {
         for (String name : names) {
            if (name.equalsIgnoreCase(packetNameHint)) {
               return true;
            }
         }
      }

      String simpleName = packet != null && packet.getClass() != null ? packet.getClass().getSimpleName() : null;
      if (simpleName != null) {
         for (String name : names) {
            if (name.equalsIgnoreCase(simpleName)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean init_Z_Obj(Object value) {
      return value == null
         ? true
         : value instanceof String
            || value instanceof Number
            || value instanceof Boolean
            || value instanceof Character
            || value instanceof Enum
            || value instanceof UUID
            || value instanceof Instant
            || value instanceof Identifier
            || value instanceof Component
            || value instanceof BlockPos
            || value instanceof Vec3
            || value instanceof Entity
            || value instanceof ItemStack
            || value instanceof Tag
            || value instanceof BlockState
            || value instanceof Holder
            || value instanceof BitSet
            || value instanceof Class;
   }

   private static boolean init_Z_Class(Class<?> type) {
      if (type == null) {
         return true;
      }

      String name = type.getName();
      return Entity.class.isAssignableFrom(type)
         || ItemStack.class.isAssignableFrom(type)
         || Tag.class.isAssignableFrom(type)
         || name.startsWith("net.minecraft.world.")
         || name.startsWith("net.minecraft.client.")
         || name.startsWith("io.netty.")
         || name.contains("StreamCodec")
         || name.contains("PacketType")
         || name.contains("ByteBuf");
   }

   private static int init_I_Obj(Object value) {
      if (value instanceof Boolean bool) {
         return bool ? UiColorPalette.mint() : UiColorPalette.magenta();
      } else if (value instanceof Entity || value instanceof UUID) {
         return UiColorPalette.cyan();
      } else if (value instanceof BlockState || value instanceof BlockPos || value instanceof Vec3) {
         return UiColorPalette.warningYellow();
      } else if (value instanceof ItemStack || value instanceof Component || value instanceof String) {
         return UiColorPalette.mint();
      } else if (value instanceof Enum) {
         return UiColorPalette.amber();
      } else if (value instanceof Identifier) {
         return UiColorPalette.blue();
      } else {
         return !(value instanceof Number) && !(value instanceof Instant) && !(value instanceof BitSet) ? UiColorPalette.softWhite() : UiColorPalette.silver();
      }
   }

   private static int init_I_Str(String direction) {
      return "C2S".equalsIgnoreCase(direction) ? UiColorPalette.cyan() : UiColorPalette.amber();
   }

   private static String init_Str_Obj(Object value) {
      return init_Str_Str_Obj("", value);
   }

   private static String init_Str_Str_Obj(String label, Object value) {
      if (value == null) {
         return "null";
      } else if (value instanceof Number number && update_Z(label)) {
         return read_Str_I(number.intValue());
      } else if (value instanceof String string) {
         return open(poll(string));
      } else if (value instanceof Character c) {
         return open(String.valueOf(c));
      } else if (value instanceof Component text) {
         return open(poll(text.getString()));
      } else if (value instanceof Identifier id) {
         return id.toString();
      } else if (value instanceof UUID uuid) {
         return uuid.toString();
      } else if (value instanceof Instant instant) {
         return instant.toString();
      } else if (value instanceof Enum<?> enumValue) {
         return enumValue.name();
      } else if (value instanceof BlockPos pos) {
         return init_Str_BlockPos(pos);
      } else if (value instanceof Vec3 vec) {
         return init_Str_Vec3(vec);
      } else if (value instanceof Entity entity) {
         return init_Str_Entity(entity);
      } else if (value instanceof ItemStack stack) {
         return init_Str_ItemStack(stack);
      } else if (value instanceof Tag nbt) {
         return poll(nbt.asString().orElse(String.valueOf(nbt)));
      } else if (value instanceof BlockState state) {
         return init_Str_BlockState(state);
      } else if (value instanceof Holder<?> entry) {
         return init_Str_Holder(entry);
      } else if (value instanceof BitSet bitSet) {
         return init_Str_BitSet(bitSet);
      } else {
         return value instanceof Class<?> clazz ? load_Str_Class(clazz) : close(poll(String.valueOf(value)));
      }
   }

   private static String load_Str_Obj(Object value) {
      if (value == null) {
         return "null";
      }

      if (init_Z_Obj(value)) {
         return init_Str_Obj(value);
      }

      try {
         return close(poll(String.valueOf(value)));
      } catch (Throwable ignored) {
         return "<unprintable " + load_Str_Class(value.getClass()) + ">";
      }
   }

   private static String read_Str_Obj(Object value) {
      if (value == null) {
         return null;
      }

      try {
         String raw = close(poll(String.valueOf(value)));
         if (raw != null && !raw.isBlank()) {
            return write_Z_Str(raw) ? null : raw;
         } else {
            return null;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static boolean write_Z_Str(String raw) {
      return raw != null && raw.matches("[\\w.$]+@[0-9a-fA-F]+");
   }

   private static String load_Str_I(int slot) {
      return slot >= 0 && slot <= 8 ? slot + 1 + " (hotbar index " + slot + ")" : String.valueOf(slot);
   }

   private static String init_Str_ServerboundPlayerA(Action action) {
      String var10000;
      switch (action) {
         case START_DESTROY_BLOCK:
            var10000 = "Start breaking targeted block";
            break;
         case ABORT_DESTROY_BLOCK:
            var10000 = "Abort breaking targeted block";
            break;
         case STOP_DESTROY_BLOCK:
            var10000 = "Finish breaking targeted block";
            break;
         case DROP_ALL_ITEMS:
            var10000 = "Drop full held stack";
            break;
         case DROP_ITEM:
            var10000 = "Drop one held item";
            break;
         case RELEASE_USE_ITEM:
            var10000 = "Release item use";
            break;
         case SWAP_ITEM_WITH_OFFHAND:
            var10000 = "Swap main hand and offhand";
            break;
         default:
            var10000 = flush_Str(action.name());
      }

      return var10000;
   }

   private static String write_Str_Obj(Object actionType) {
      if (actionType instanceof Enum<?> enumValue) {
         String var10000;
         switch (enumValue.name()) {
            case "PICKUP":
               var10000 = "Pick up / place stack";
               break;
            case "QUICK_MOVE":
               var10000 = "Quick move / shift-click";
               break;
            case "SWAP":
               var10000 = "Swap with hotbar slot";
               break;
            case "CLONE":
               var10000 = "Clone stack";
               break;
            case "THROW":
               var10000 = "Throw item";
               break;
            case "QUICK_CRAFT":
               var10000 = "Drag split across slots";
               break;
            case "PICKUP_ALL":
               var10000 = "Pick up all matching items";
               break;
            default:
               var10000 = "Click Slot (" + enumValue.name() + ")";
         }

         return var10000;
      } else {
         return actionType == null ? "Click Slot" : "Click Slot (" + load_Str_Obj(actionType) + ")";
      }
   }

   private static String update(Object value) {
      if (value == null || value == init_Obj) {
         return null;
      } else if (value instanceof Component text) {
         return text.getString();
      } else if (value instanceof String string) {
         return string;
      } else if (value instanceof Optional<?> optional) {
         return optional.<String>map(PacketInspectorEngine::update).orElse(null);
      } else if (value instanceof String[] array) {
         return String.join(" | ", array);
      } else {
         Object stringLike = load_Obj_Obj_StrArr(value, "getString", "string", "content", "raw", "name");
         if (stringLike instanceof Component text) {
            return text.getString();
         } else {
            return stringLike instanceof String string ? string : null;
         }
      }
   }

   private static String init_Str_Collection(Collection<?> pages) {
      List<String> previews = new ArrayList<>();
      int index = 0;

      for (Object page : pages) {
         String text = update(page);
         if ((text == null || text.isBlank()) && page != null) {
            text = load_Str_Obj(page);
         }

         if (text != null && !text.isBlank()) {
            previews.add("p" + (index + 1) + "=" + open(poll(text)));
         }

         index++;
         if (previews.size() >= 2) {
            break;
         }
      }

      return previews.isEmpty() ? null : String.join(" | ", previews);
   }

   private static String load_Str_StrArr(String[] lines) {
      List<String> parts = new ArrayList<>();

      for (int i = 0; i < lines.length; i++) {
         String line = lines[i] == null ? "" : lines[i].trim();
         if (!line.isEmpty()) {
            parts.add("L" + (i + 1) + "=" + open(poll(line)));
         }
      }

      return parts.isEmpty() ? "<empty sign>" : String.join(" | ", parts);
   }

   private static PacketInspectorEngine.DecodeCheckPair init_bu_StreamUtil_Obj(
      Object messageValue, Object parameters, UUID senderUuid, Object timestampValue, Object filterMask, String kind, PacketInspectorEngine.ReportBuilder builder
   ) {
      boolean wrote = false;
      boolean informative = false;
      if (kind != null && !kind.isBlank()) {
         builder.load("Kind: " + kind, load_I_Str_Obj("Kind", kind));
         wrote = true;
      }

      String chatType = flush(parameters);
      if (chatType != null && !chatType.isBlank()) {
         builder.load("Chat Type: " + chatType, load_I_Str_Obj("Chat Type", chatType));
         wrote = true;
         informative = true;
      }

      String sender = init_Str_Obj_UUID(parameters, senderUuid);
      if (sender != null && !sender.isBlank()) {
         builder.load("Sender: " + sender, load_I_Str_Obj("Sender", sender));
         wrote = true;
         informative = true;
      }

      String target = sync(parameters);
      if (target != null && !target.isBlank()) {
         builder.load("Target: " + target, load_I_Str_Obj("Target", target));
         wrote = true;
         informative = true;
      }

      String message = update(messageValue);
      if (message != null && !message.isBlank()) {
         builder.load("Message: " + open(poll(message)), load_I_Str_Obj("Message", message));
         wrote = true;
         informative = true;
      } else if (messageValue != null) {
         builder.load("Message: " + poll(messageValue), init_I_Str_Obj("Message", messageValue));
         wrote = true;
         informative = true;
      }

      if (timestampValue != null) {
         builder.load("Timestamp: " + load_Str_Obj(timestampValue), load_I_Str_Obj("Timestamp", timestampValue));
         wrote = true;
         informative = true;
      }

      String filter = close(filterMask);
      if (filter != null) {
         builder.load("Filter: " + filter, load_I_Str_Obj("Filter", filter));
         wrote = true;
         informative = true;
      }

      return new PacketInspectorEngine.DecodeCheckPair(wrote, informative);
   }

   private static String init_Str_Obj_UUID(Object parameters, UUID senderUuid) {
      String senderName = update(
         init_Obj_ObjArr(
            load_Obj_Obj_Str(parameters, "name"),
            init_Obj_Obj_I(parameters, 1),
            init_Obj_Obj_ObjArr(parameters, init_Obj_Obj_I(parameters, 0), init_Obj_Obj_I(parameters, 2))
         )
      );
      if (senderName == null || senderName.isBlank()) {
         senderName = init_Str_UUID(senderUuid);
      }

      if (senderName != null && !senderName.isBlank()) {
         return senderUuid == null ? poll(senderName) : poll(senderName) + " (" + senderUuid + ")";
      } else {
         return senderUuid == null ? null : senderUuid.toString();
      }
   }

   private static String sync(Object parameters) {
      String targetName = update(init_Obj_ObjArr(load_Obj_Obj_Str(parameters, "targetName"), init_Obj_Obj_I(parameters, 2)));
      return targetName != null && !targetName.isBlank() ? poll(targetName) : null;
   }

   private static String flush(Object parameters) {
      Object type = init_Obj_ObjArr(load_Obj_Obj_Str(parameters, "type"), init_Obj_Obj_I(parameters, 0));
      return type == null ? null : load_Str_Obj(type);
   }

   private static String close(Object filterMask) {
      if (filterMask == null) {
         return null;
      }

      Object fullyFiltered = load_Obj_Obj_Str(filterMask, "isFullyFiltered");
      if (Boolean.TRUE.equals(fullyFiltered)) {
         return "Fully filtered";
      }

      Object passThrough = load_Obj_Obj_Str(filterMask, "isPassThrough");
      return Boolean.TRUE.equals(passThrough) ? "Pass-through" : "Partially filtered";
   }

   private static String init_Str_UUID(UUID uuid) {
      if (uuid == null) {
         return null;
      }

      try {
         if (init_Minecraft.getConnection() != null) {
            PlayerInfo entry = init_Minecraft.getConnection().getPlayerInfo(uuid);
            if (entry != null && entry.getProfile() != null) {
               String profileName = update(entry.getProfile());
               if (profileName != null && !profileName.isBlank()) {
                  return profileName;
               }
            }
         }
      } catch (Throwable var4) {
      }

      try {
         if (init_Minecraft.level != null) {
            for (AbstractClientPlayer player : init_Minecraft.level.players()) {
               if (uuid.equals(player.getUUID()) && player.getGameProfile() != null) {
                  String profileName = update(player.getGameProfile());
                  if (profileName != null && !profileName.isBlank()) {
                     return profileName;
                  }
               }
            }
         }
      } catch (Throwable var5) {
      }

      return null;
   }

   private static PacketInspectorEngine.ParsedMessagePair init_bu_StateCache_Str(String raw) {
      if (raw == null) {
         return null;
      }

      String text = raw.trim();
      if (text.isEmpty()) {
         return null;
      }

      if (text.startsWith("<")) {
         int end = text.indexOf("> ");
         if (end > 1 && end + 2 < text.length()) {
            return new PacketInspectorEngine.ParsedMessagePair(text.substring(1, end).trim(), text.substring(end + 2).trim());
         }
      }

      int colon = text.indexOf(": ");
      if (colon > 0 && colon <= 48 && colon + 2 < text.length()) {
         String sender = text.substring(0, colon).trim();
         String message = text.substring(colon + 2).trim();
         if (!sender.isEmpty() && !message.isEmpty()) {
            return new PacketInspectorEngine.ParsedMessagePair(sender, message);
         }
      }

      return null;
   }

   private static String init_Str_Entity(Entity entity) {
      String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      String name = entity.getDisplayName() == null ? "" : entity.getDisplayName().getString();
      return typeId + " #" + entity.getId() + " [" + poll(name) + "] @" + init_Str_Vec3(new Vec3(entity.getX(), entity.getY(), entity.getZ()));
   }

   private static void init_V_bu_ConfigStore(PacketInspectorEngine.ReportBuilder builder, String label, int entityId) {
      builder.load(label + ": " + read_Str_I(entityId), UiColorPalette.cyan());
   }

   private static String read_Str_I(int entityId) {
      Entity entity = init_Entity_I(entityId);
      return entity != null ? entityId + " -> " + init_Str_Entity(entity) : entityId + " (entity not loaded in client world)";
   }

   private static String open(Object ids) {
      if (ids != null && ids != init_Obj) {
         List<String> parts = new ArrayList<>();
         int total = 0;
         if (ids instanceof int[] array) {
            total = array.length;

            for (int i = 0; i < Math.min(array.length, 8); i++) {
               parts.add(read_Str_I(array[i]));
            }
         } else if (ids instanceof Collection<?> collection) {
            total = collection.size();
            int shown = 0;

            for (Object id : collection) {
               if (shown >= 8) {
                  break;
               }

               if (id instanceof Number number) {
                  parts.add(read_Str_I(number.intValue()));
               } else {
                  parts.add(poll(id));
               }

               shown++;
            }
         } else {
            if (!ids.getClass().isArray()) {
               return poll(ids);
            }

            int length = Array.getLength(ids);
            total = length;

            for (int i = 0; i < Math.min(length, 8); i++) {
               Object id = Array.get(ids, i);
               parts.add(id instanceof Number number ? read_Str_I(number.intValue()) : poll(id));
            }
         }

         if (total > parts.size()) {
            parts.add("... +" + (total - parts.size()) + " more");
         }

         return "[" + total + "] " + String.join(", ", parts);
      } else {
         return "null";
      }
   }

   private static String load_Str_Collection(Collection<?> collection) {
      if (collection == null) {
         return "null";
      }

      List<String> parts = new ArrayList<>();
      int shown = 0;

      for (Object item : collection) {
         if (shown >= 6) {
            break;
         }

         parts.add(poll(item));
         shown++;
      }

      if (collection.size() > shown) {
         parts.add("... +" + (collection.size() - shown) + " more");
      }

      return "[" + collection.size() + "] " + String.join(", ", parts);
   }

   private static Entity init_Entity_I(int entityId) {
      try {
         return init_Minecraft.level == null ? null : init_Minecraft.level.getEntity(entityId);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static boolean update_Z(String label) {
      String normalized = sync_Str(label);
      return normalized.equals("entityid")
         || normalized.equals("entity")
         || normalized.equals("entityindex")
         || normalized.equals("targetentityid")
         || normalized.equals("sourceentityid")
         || normalized.endsWith("entityid");
   }

   private static String init_Str_EntityType(EntityType<?> type) {
      if (type == null) {
         return "Unknown";
      }

      try {
         return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
      } catch (Throwable ignored) {
         return load_Str_Obj(type);
      }
   }

   private static String init_Str_ItemStack(ItemStack stack) {
      if (stack.isEmpty()) {
         return "ItemStack.EMPTY";
      }

      String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      String name = stack.getHoverName() == null ? "" : stack.getHoverName().getString();
      String display = itemId;
      if (!name.isEmpty() && !Objects.equals(name, stack.getItem().getName(stack).getString())) {
         display = display + " [" + poll(name) + "]";
      }

      return stack.getCount() + "x " + display;
   }

   private static String init_Str_BlockState(BlockState state) {
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + poll(state.toString());
   }

   private static String init_Str_BlockPos(BlockPos pos) {
      return "x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ();
   }

   private static String init_Str_Vec3(Vec3 vec) {
      return String.format(Locale.ROOT, "x=%.3f, y=%.3f, z=%.3f", vec.x, vec.y, vec.z);
   }

   private static String init_Str_BitSet(BitSet bitSet) {
      int shown = 0;
      StringBuilder builder = new StringBuilder("BitSet{");

      for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
         if (shown >= 16) {
            builder.append("...");
            break;
         }

         if (shown > 0) {
            builder.append(", ");
         }

         builder.append(bit);
         shown++;
      }

      builder.append("}");
      return builder.toString();
   }

   private static String init_Str_Holder(Holder<?> entry) {
      try {
         Optional<?> key = entry.unwrapKey();
         if (key.isPresent()) {
            return poll(String.valueOf(key.get()));
         }
      } catch (Throwable var3) {
      }

      try {
         Object value = load_Obj_Obj_Str(entry, "value");
         if (value != null) {
            return poll(String.valueOf(value));
         }
      } catch (Throwable var2) {
      }

      return poll(String.valueOf(entry));
   }

   private static String poll(Object value) {
      return load_Str_Str_Obj("", value);
   }

   private static String load_Str_Str_Obj(String label, Object value) {
      if (value == null || value == init_Obj) {
         return null;
      } else if (init_Z_Obj(value)) {
         return init_Str_Str_Obj(label, value);
      } else if (value instanceof Optional<?> optional) {
         return optional.<String>map(item -> load_Str_Str_Obj(label, item)).orElse("Optional.empty");
      } else if (value instanceof Collection<?> collection) {
         return load_Str_Class(value.getClass()) + "[" + collection.size() + "]";
      } else if (value instanceof Map<?, ?> map) {
         return "Map[" + map.size() + "]";
      } else {
         Class<?> type = value.getClass();
         return type.isArray() ? type.getComponentType().getSimpleName() + "[" + Array.getLength(value) + "]" : load_Str_Obj(value);
      }
   }

   private static int init_I_Str_Obj(String label, Object value) {
      if (value != null && value != init_Obj) {
         return !(value instanceof Collection) && !(value instanceof Map) && !value.getClass().isArray() ? load_I_Str_Obj(label, value) : UiColorPalette.textBody();
      } else {
         return UiColorPalette.textMuted();
      }
   }

   private static int load_I_Obj(Object value) {
      return init_I_Str_Obj("", value);
   }

   private static int load_I_Str_Obj(String label, Object value) {
      String normalized = sync_Str(label);
      if (value instanceof Boolean bool) {
         return bool ? UiColorPalette.mint() : UiColorPalette.magenta();
      } else {
         if (normalized.contains("sender")
            || normalized.contains("author")
            || normalized.contains("player")
            || normalized.equals("entity")
            || normalized.contains("entityid")
            || normalized.contains("owner")) {
            return UiColorPalette.cyan();
         }

         if (normalized.contains("target") || normalized.contains("recipient") || normalized.contains("team")) {
            return UiColorPalette.magenta();
         }

         if (normalized.contains("message")
            || normalized.contains("content")
            || normalized.equals("text")
            || normalized.contains("title")
            || normalized.contains("book")
            || normalized.contains("page")
            || normalized.contains("sign")
            || normalized.contains("recipe")
            || normalized.contains("item")
            || normalized.contains("stack")) {
            return UiColorPalette.mint();
         }

         if (normalized.equals("kind")
            || normalized.contains("action")
            || normalized.contains("interaction")
            || normalized.equals("type")
            || normalized.contains("mode")
            || normalized.contains("reason")
            || normalized.contains("status")
            || normalized.contains("hand")) {
            return UiColorPalette.amber();
         }

         if (normalized.contains("timestamp") || normalized.contains("time") || normalized.contains("sequence") || normalized.contains("salt")) {
            return UiColorPalette.warmWhite();
         }

         if (normalized.contains("slot")
            || normalized.contains("syncid")
            || normalized.contains("index")
            || normalized.contains("revision")
            || normalized.contains("checksum")
            || normalized.contains("offset")) {
            return UiColorPalette.blue();
         }

         if (normalized.contains("sound") || normalized.contains("channel") || normalized.contains("identifier") || normalized.contains("packettype")) {
            return UiColorPalette.blue();
         }

         if (normalized.contains("block")
            || normalized.contains("pos")
            || normalized.equals("x")
            || normalized.equals("y")
            || normalized.equals("z")
            || normalized.contains("yaw")
            || normalized.contains("pitch")
            || normalized.contains("velocity")) {
            return UiColorPalette.warningYellow();
         }

         if (normalized.contains("filter")) {
            String text = value == null ? "" : String.valueOf(value).toLowerCase(Locale.ROOT);
            if (text.contains("fully filtered")) {
               return UiColorPalette.magenta();
            } else {
               return text.contains("pass-through") ? UiColorPalette.mint() : UiColorPalette.warningYellow();
            }
         } else {
            return init_I_Obj(value);
         }
      }
   }

   private static String write_Str_Str(String label) {
      return flush_Str(label).replace('.', ' ').trim();
   }

   private static String update_Str(String value) {
      return value != null && !value.isEmpty() ? Character.toLowerCase(value.charAt(0)) + value.substring(1) : "";
   }

   private static String sync_Str(String value) {
      return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
   }

   private static boolean sync_Z(String label) {
      if (label != null && !label.isBlank()) {
         String normalized = label.trim().toLowerCase(Locale.ROOT);
         return normalized.matches("(comp|field|method|class)_?\\d+");
      } else {
         return false;
      }
   }

   private static boolean flush_Z(String label) {
      return label != null && !label.isBlank() ? init_Set.contains(label) || load_Set.contains(sync_Str(label)) : false;
   }

   private static int init_I_List(List<ItemStack> stacks) {
      int count = 0;

      for (ItemStack stack : stacks) {
         if (stack != null && !stack.isEmpty()) {
            count++;
         }
      }

      return count;
   }

   private static PacketInspectorEngine.InteractEntityCapture init_bu_NetHelper_ServerboundInterac(ServerboundInteractPacket packet) {
      PacketInspectorEngine.InteractEntityCapture capture = new PacketInspectorEngine.InteractEntityCapture();

      try {
         capture.init_Str = "Interact At";
         capture.init_InteractionHand = packet.hand();
         capture.init_Vec3 = packet.location();
      } catch (Throwable ignored) {
         Object interactionType = init_Obj_Obj_Str(packet, "type");
         if (interactionType != null && interactionType != init_Obj) {
            capture.init_Str = load_Str_Class(interactionType.getClass());
            if (init_Obj_Obj_StrArr(interactionType, "hand", "interactionHand") instanceof InteractionHand actualHand) {
               capture.init_InteractionHand = actualHand;
            }

            if (init_Obj_Obj_StrArr(interactionType, "pos", "hitPos", "location") instanceof Vec3 actualHitPos) {
               capture.init_Vec3 = actualHitPos;
            }
         }
      }

      return capture;
   }

   private static String push(Object array) {
      int length = Array.getLength(array);
      StringBuilder builder = new StringBuilder();
      builder.append(array.getClass().getComponentType().getSimpleName()).append('[').append(length).append("] ");
      builder.append('[');
      int shown = Math.min(length, 64);

      for (int i = 0; i < shown; i++) {
         if (i > 0) {
            builder.append(", ");
         }

         builder.append(Array.get(array, i));
      }

      if (length > shown) {
         builder.append(", ...");
      }

      builder.append(']');
      return builder.toString();
   }

   private static String load_Str_Class(Class<?> type) {
      if (type == null) {
         return "Unknown";
      }

      String alias = InspectorMappingsIndex.init(type);
      return alias != null && !alias.isBlank() ? flush_Str(alias) : flush_Str(type.getSimpleName().isEmpty() ? type.getName() : type.getSimpleName());
   }

   private static String flush_Str(String value) {
      if (value != null && !value.isEmpty()) {
         String alias = init_Str_StrArr(InspectorMappingsIndex.init_Str(value.replace('/', '.')), InspectorMappingsIndex.load(value.substring(value.lastIndexOf(46) + 1)));
         String source = (alias == null ? value : alias).replace('$', '.');
         if (source.endsWith("Packet")) {
            source = source.substring(0, source.length() - 6);
         }

         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (i > 0 && Character.isUpperCase(ch) && Character.isLowerCase(source.charAt(i - 1))) {
               builder.append(' ');
            }

            builder.append(ch);
         }

         return builder.toString();
      } else {
         return "";
      }
   }

   private static String init_Str_Str_Class(String label, Class<?> returnType) {
      if (label != null && !label.isBlank()) {
         if (label.startsWith("get") && label.length() > 3 && Character.isUpperCase(label.charAt(3))) {
            return label.substring(3);
         } else {
            return label.startsWith("is")
                  && label.length() > 2
                  && Character.isUpperCase(label.charAt(2))
                  && (returnType == boolean.class || returnType == Boolean.class)
               ? label.substring(2)
               : label;
         }
      } else {
         return label;
      }
   }

   private static String read_Str_Collection(Collection<?> equipmentEntries) {
      if (equipmentEntries != null && !equipmentEntries.isEmpty()) {
         List<String> parts = new ArrayList<>();
         int shown = 0;

         for (Object entry : equipmentEntries) {
            if (shown >= 6) {
               parts.add("... +" + (equipmentEntries.size() - shown) + " more");
               break;
            }

            String summary = pop(entry);
            if (summary != null && !summary.isBlank()) {
               parts.add(summary);
               shown++;
            }
         }

         return parts.isEmpty() ? null : String.join(", ", parts);
      } else {
         return null;
      }
   }

   private static String pop(Object entry) {
      if (entry == null) {
         return null;
      }

      Object slot = init_Obj_ObjArr(
         load_Obj_Obj_StrArr(entry, "getFirst", "first", "slot", "getSlot"), init_Obj_Obj_I(entry, 0), init_Obj_Obj_Str(entry, "first")
      );
      Object stack = init_Obj_ObjArr(
         load_Obj_Obj_StrArr(entry, "getSecond", "second", "stack", "getStack"), init_Obj_Obj_I(entry, 1), init_Obj_Obj_Str(entry, "second")
      );
      if (slot == null && stack == null) {
         return poll(entry);
      }

      String slotComponent = slot == null ? "Unknown Slot" : load_Str_Obj(slot);
      String stackComponent = stack instanceof ItemStack itemStack ? init_Str_ItemStack(itemStack) : poll(stack);
      return slotComponent + "=" + stackComponent;
   }

   private static String close(String raw) {
      if (raw != null && !raw.isBlank()) {
         String rewritten = init_Str_Str(raw, load_Pattern, true);
         return init_Str_Str(rewritten, init_Pattern, false);
      } else {
         return raw;
      }
   }

   private static String init_Str_Str(String raw, Pattern pattern, boolean qualified) {
      Matcher matcher = pattern.matcher(raw);
      StringBuffer buffer = new StringBuffer();
      boolean changed = false;

      while (matcher.find()) {
         String original = matcher.group(1);
         String alias = qualified ? InspectorMappingsIndex.init_Str(original) : InspectorMappingsIndex.load(original);
         if (alias != null && !alias.isBlank()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(alias));
            changed = true;
         }
      }

      if (!changed) {
         return raw;
      }

      matcher.appendTail(buffer);
      return buffer.toString();
   }

   private static String open(String value) {
      return "\"" + value + "\"";
   }

   private static String poll(String value) {
      if (value == null) {
         return "";
      }

      String sanitized = value.replace('\n', ' ').replace('\r', ' ').trim();
      return sanitized.length() <= 320 ? sanitized : sanitized.substring(0, 317) + "...";
   }

   private static BlockState init_BlockState_BlockPos(BlockPos pos) {
      try {
         return init_Minecraft.level != null ? init_Minecraft.level.getBlockState(pos) : null;
      } catch (Throwable ignored) {
         return null;
      }
   }

   private record PayloadDecodeResult(boolean init_Z, boolean load_Z, boolean read, boolean write) {
      private static final PacketInspectorEngine.PayloadDecodeResult init_bu_BufferHelper = new PacketInspectorEngine.PayloadDecodeResult(false, false, false, false);
      private static final PacketInspectorEngine.PayloadDecodeResult load_bu_BufferHelper = new PacketInspectorEngine.PayloadDecodeResult(true, true, true, true);
   }

   private enum CompressionKind {
      NONE("none"),
      GZIP("gzip"),
      ZLIB("zlib/deflate");

      private final String init_Str;

      CompressionKind(String label) {
         this.init_Str = label;
      }

      String init() {
         return this.init_Str;
      }
   }

   private static final class ReportBuilder {
      private final String init_Str;
      private final List<PacketInspectorEngine.ColoredLine> init_List = new ArrayList<>();
      private final StringBuilder init_StringBuilder = new StringBuilder();
      private boolean init_Z;

      private ReportBuilder(String title) {
         this.init_Str = title;
      }

      void init(String title, int color) {
         this.load("[" + title + "]", color);
      }

      void load(String text, int color) {
         if (!this.init_Z) {
            if (this.init_List.size() >= 900) {
               this.init_Z = true;
               this.init_List.add(new PacketInspectorEngine.ColoredLine("... output truncated ...", UiColorPalette.errorText()));
               this.init_StringBuilder.append("... output truncated ...\n");
            } else {
               this.init_List.add(new PacketInspectorEngine.ColoredLine(text, color));
               this.init_StringBuilder.append(text).append('\n');
            }
         }
      }

      void init_V() {
         this.load("", UiColorPalette.textBright());
      }

      void init(PacketInspectorEngine.ReportBuilder other) {
         if (other != null) {
            for (PacketInspectorEngine.ColoredLine line : other.init_List) {
               this.load(line.init_Str(), line.init_I());
            }
         }
      }

      boolean init_Z() {
         return this.init_Z;
      }

      PacketInspectorEngine.InspectionReport init_bu_JsonUtil() {
         return new PacketInspectorEngine.InspectionReport(this.init_Str, this.init_List, this.init_StringBuilder.toString());
      }
   }

   private record WireFieldSlice(int init_I, int load, String init_Str) {
   }

   private record PropertyEntry(String init_Str, Object init_Obj, PacketInspectorEngine.PropertySource init_bu_RenderState) {
   }

   private record PacketSummaryFlags(boolean init_Z, boolean load) {
      private static final PacketInspectorEngine.PacketSummaryFlags init_bu_FileCache = new PacketInspectorEngine.PacketSummaryFlags(false, false);
   }

   public static final class InspectionReport {
      private final String init_Str;
      private final List<PacketInspectorEngine.ColoredLine> init_List;
      private final String load;

      private InspectionReport(String title, List<PacketInspectorEngine.ColoredLine> lines, String copyText) {
         this.init_Str = title;
         this.init_List = Collections.unmodifiableList(lines);
         this.load = copyText;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public List<PacketInspectorEngine.ColoredLine> init_List() {
         return this.init_List;
      }

      public String load() {
         return this.load;
      }
   }

   private static final class InteractEntityCapture {
      private String init_Str;
      private InteractionHand init_InteractionHand;
      private Vec3 init_Vec3;
   }

   private enum PropertySource {
      RECORD,
      METHOD;
   }

   private record ParsedMessagePair(String init, String load) {
   }

   private record DecodeCheckPair(boolean init, boolean load) {
   }

   public record PayloadAnalysis(boolean init_Z, boolean load, double init_D, String init_Str, List<PacketInspectorEngine.ColoredLine> init_List) {
      public PayloadAnalysis {
         init_D = Math.max(0.0, Math.min(1.0, init_D));
         init_Str = init_Str == null ? "" : init_Str;
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
      }
   }

   public static final class ColoredLine {
      private final String init_Str;
      private final int init_I;

      public ColoredLine(String text, int color) {
         this.init_Str = text;
         this.init_I = color;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public int init_I() {
         return this.init_I;
      }
   }

   private record VarIntRead(int init, int load) {
   }

   private static final class ByteCursor {
      private final byte[] init_BArr;
      private int init_I;

      private ByteCursor(byte[] bytes) {
         this.init_BArr = bytes == null ? new byte[0] : bytes;
      }

      int init_I() {
         return this.init_I;
      }

      void init(int position) {
         this.init_I = Math.max(0, Math.min(this.init_BArr.length, position));
      }

      int load_I() {
         return Math.max(0, this.init_BArr.length - this.init_I);
      }

      byte[] init_BArr() {
         return this.load_I() <= 0 ? new byte[0] : Arrays.copyOfRange(this.init_BArr, this.init_I, this.init_BArr.length);
      }

      String init_Str() throws IOException {
         if (this.load_I() < 2) {
            throw new IOException("expected Java UTF length");
         }

         int length = (this.init_BArr[this.init_I] & 255) << 8 | this.init_BArr[this.init_I + 1] & 255;
         if (length >= 0 && this.init_I + 2 + length <= this.init_BArr.length) {
            int start = this.init_I;

            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(this.init_BArr, start, length + 2))) {
               String value = in.readUTF();
               this.init_I = start + 2 + length;
               return value;
            }
         } else {
            throw new IOException("Java UTF length " + length + " exceeds remaining " + this.load_I());
         }
      }

      byte[] load_BArr() throws IOException {
         if (this.load_I() < 2) {
            throw new IOException("expected unsigned short byte length");
         } else {
            int length = (this.init_BArr[this.init_I] & 255) << 8 | this.init_BArr[this.init_I + 1] & 255;
            this.init_I += 2;
            if (length >= 0 && this.init_I + length <= this.init_BArr.length) {
               byte[] out = Arrays.copyOfRange(this.init_BArr, this.init_I, this.init_I + length);
               this.init_I += length;
               return out;
            } else {
               throw new IOException("byte array length " + length + " exceeds remaining " + this.load_I());
            }
         }
      }
   }
}
