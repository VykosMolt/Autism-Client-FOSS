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
   private static final Minecraft MC = Minecraft.getInstance();
   private static final Object MISSING = new Object();
   private static final Pattern INTERMEDIARY_SIMPLE_NAME = Pattern.compile("(?<![\\w$.])(class_\\d+)(?=[\\[{(:\\s]|$)");
   private static final Pattern INTERMEDIARY_QUALIFIED_NAME = Pattern.compile("(net\\.minecraft(?:\\.[\\w$]+)*\\.class_\\d+)(?=[\\[{(:\\s]|$)");
   private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile("(?i)\\b[a-z0-9_.-]+:[a-z0-9_./-]+\\b");
   private static final Pattern PRINTABLE_RUN_PATTERN = Pattern.compile("[\\x20-\\x7E]{4,}");
   private static final Pattern BASE64_PATTERN = Pattern.compile("(?<![A-Za-z0-9+/=])(?:[A-Za-z0-9+/]{16,}={0,2})(?![A-Za-z0-9+/=])");
   private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(?<![0-9a-f])(?:0x)?[0-9a-f]{16,}(?![0-9a-f])");
   private static final Pattern UUID_PATTERN = Pattern.compile("(?i)\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b");
   private static final Pattern HOST_PATTERN = Pattern.compile(
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
   private static final Set<String> IGNORED_METHOD_NAMES = Set.of(
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
   private static final Set<String> IGNORED_METHOD_KEYS = Set.of(
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
   private static final Set<String> EXTRA_GETTER_NAMES = Set.of(
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

   public static PacketInspectorEngine.InspectionReport inspect(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return inspect(entry, null, true);
   }

   public static PacketInspectorEngine.InspectionReport init_bu_JsonUtil_AutismPacketLogger_bu_SyncManager(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      return inspect(entry, payloadAnalysis, true);
   }

   public static PacketInspectorEngine.InspectionReport inspect(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis, boolean includePayloadAnalyzer
   ) {
      try {
         return buildInspectionReport(entry, payloadAnalysis, includePayloadAnalyzer);
      } catch (Throwable t) {
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder(entry.name + " [" + entry.direction + "]");
         builder.section("Meta", UiColorPalette.warmWhite());
         builder.line("Name: " + entry.name, UiColorPalette.softWhite());
         builder.line("Direction: " + entry.direction, directionColor(entry.direction));
         builder.line("Class: " + entry.packetClass.getName(), UiColorPalette.textBody());
         builder.line("Tick: " + entry.tick, UiColorPalette.textBody());
         builder.line("Time: " + Instant.ofEpochMilli(entry.timestampMs), UiColorPalette.textBody());
         builder.blankLine();
         builder.section("Inspect Error", UiColorPalette.errorText());
         builder.line("The detailed inspector failed on this packet, so this fallback view was opened instead.", UiColorPalette.errorText());
         builder.line("Reason: " + t.getClass().getSimpleName() + (t.getMessage() == null ? "" : " - " + sanitize(t.getMessage())), UiColorPalette.errorText());
         if (entry.packet != null) {
            builder.blankLine();
            builder.section("Fallback", UiColorPalette.warningYellow());
            builder.line("Packet Type: " + describeValue(callGetter(entry.packet, "getPacketType")), UiColorPalette.textBody());
            builder.line("toString(): " + sanitize(String.valueOf(entry.packet)), UiColorPalette.textMuted());
         }

         return builder.build();
      }
   }

   public static PacketInspectorEngine.InspectionReport load_bu_JsonUtil_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      return buildInspectionReport(entry, null, true);
   }

   public static PacketInspectorEngine.InspectionReport load_bu_JsonUtil_AutismPacketLogger_bu_SyncManager(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      return buildInspectionReport(entry, payloadAnalysis, true);
   }

   public static PacketInspectorEngine.InspectionReport buildInspectionReport(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis, boolean includePayloadAnalyzer
   ) {
      String title = entry.name + " [" + entry.direction + "]";
      PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder(title);
      AutismPayloadSupport.PayloadEntry payloadSnapshot = entry == null ? null : AutismPayloadSupport.resolveEntryPayload(entry);
      builder.section("Meta", UiColorPalette.warmWhite());
      builder.line("Name: " + entry.name, UiColorPalette.softWhite());
      builder.line("Direction: " + entry.direction, directionColor(entry.direction));
      builder.line("Class: " + entry.packetClass.getName(), UiColorPalette.textBody());
      if (payloadSnapshot != null) {
         builder.line("Payload Channel: " + payloadSnapshot.channel(), UiColorPalette.successText());
         builder.line("Payload Size: " + payloadSnapshot.byteLength() + " bytes", UiColorPalette.textBright());
         PayloadChannelFilterManager.MatchedRule filter = matchingFilterRule(payloadSnapshot);
         if (filter != null) {
            builder.line("Matched Filter: " + filter.load() + " (" + filter.pattern() + ")", UiColorPalette.mint());
         }

         String source = describeChannelSource(payloadSnapshot.channel());
         if (source != null && !source.isBlank()) {
            builder.line("Likely Source: " + source, UiColorPalette.textBody());
         }
      }

      Object packetType = callGetter(entry.packet, "getPacketType");
      if (packetType != null) {
         builder.line("Packet Type: " + describeValue(packetType), UiColorPalette.textBody());
      }

      builder.line("Tick: " + entry.tick, UiColorPalette.textBody());
      builder.line("Time: " + Instant.ofEpochMilli(entry.timestampMs), UiColorPalette.textBody());
      builder.line("Inventory: " + entry.inventoryPacket + " | Movement: " + entry.movementPacket, UiColorPalette.textMuted());
      PacketDetailFormatter.Inner_DataUtil normalAnalysis = PacketDetailFormatter.analyze(entry);
      if (entry.packet == null) {
         builder.blankLine();
         builder.section("Reality", UiColorPalette.cyan());
         if (payloadSnapshot != null) {
            appendPayloadHeader(payloadSnapshot, builder);
            builder.line("Packet Object: missing; showing preserved payload snapshot.", UiColorPalette.textMuted());
         } else {
            builder.line("Packet instance is missing, so only metadata is available.", UiColorPalette.errorText());
         }

         if (normalAnalysis.hasMeaning()) {
            builder.blankLine();
            builder.section("Meaning", UiColorPalette.warningYellow());
            appendLines(builder, normalAnalysis.meaningLines());
         }

         if (includePayloadAnalyzer && payloadSnapshot != null) {
            PacketInspectorEngine.ReportBuilder analyzerBuilder = new PacketInspectorEngine.ReportBuilder(title);
            if (appendPayloadAnalysis(payloadSnapshot, analyzerBuilder, payloadAnalysis)) {
               builder.blankLine();
               builder.section("Analyzer", UiColorPalette.mint());
               builder.append(analyzerBuilder);
            }
         }

         if (normalAnalysis.hasWire()) {
            builder.blankLine();
            builder.section("Wire", UiColorPalette.blue());
            appendLines(builder, normalAnalysis.wireLines());
         }

         return builder.build();
      } else {
         PacketInspectorEngine.PacketSummaryFlags summary = PacketInspectorEngine.PacketSummaryFlags.NONE;
         if (normalAnalysis.init_Z()) {
            builder.blankLine();
            builder.section("Reality", UiColorPalette.cyan());
            appendLines(builder, normalAnalysis.lines());
         } else if (payloadSnapshot != null) {
            builder.blankLine();
            builder.section("Reality", UiColorPalette.cyan());
            appendPayloadHeader(payloadSnapshot, builder);
         } else {
            PacketInspectorEngine.ReportBuilder summaryBuilder = new PacketInspectorEngine.ReportBuilder(title);
            summary = describePacket(entry.packet, entry.name, entry, summaryBuilder);
            if (summary.init_Z()) {
               builder.blankLine();
               builder.section("Reality", UiColorPalette.cyan());
               builder.append(summaryBuilder);
            }
         }

         if (normalAnalysis.hasMeaning()) {
            builder.blankLine();
            builder.section("Meaning", UiColorPalette.warningYellow());
            appendLines(builder, normalAnalysis.meaningLines());
         }

         if (includePayloadAnalyzer && payloadSnapshot != null) {
            PacketInspectorEngine.ReportBuilder analyzerBuilder = new PacketInspectorEngine.ReportBuilder(title);
            if (appendPayloadAnalysis(payloadSnapshot, analyzerBuilder, payloadAnalysis)) {
               builder.blankLine();
               builder.section("Analyzer", UiColorPalette.mint());
               builder.append(analyzerBuilder);
            }
         }

         if (normalAnalysis.hasWire()) {
            builder.blankLine();
            builder.section("Wire", UiColorPalette.blue());
            appendLines(builder, normalAnalysis.wireLines());
         }

         boolean semanticComplete = normalAnalysis.init_Z() ? normalAnalysis.write() : summary.load();
         if (!semanticComplete) {
            builder.blankLine();
            builder.section("Details", UiColorPalette.blue());
            dumpObject(entry.packet, builder, new IdentityHashMap<>());
         }

         return builder.build();
      }
   }

   public static PacketInspectorEngine.InspectionReport read_bu_JsonUtil_AutismPacketLogger_bu_SyncManager(
      AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      try {
         String title = entry == null ? "Payload Analyzer" : entry.name + " Analyzer";
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder(title);
         AutismPayloadSupport.PayloadEntry snapshot = entry == null ? null : AutismPayloadSupport.resolveEntryPayload(entry);
         builder.section("Analyzer", UiColorPalette.mint());
         if (snapshot == null) {
            builder.line("No payload snapshot is available for this packet.", UiColorPalette.errorText());
            return builder.build();
         }

         if (payloadAnalysis == null) {
            builder.line("Channel: " + snapshot.channel(), UiColorPalette.successText());
            builder.line(
               "Direction: " + snapshot.direction() + " | Protocol: " + orFallback(snapshot.protocolPhase(), "unknown"), directionColor(snapshot.direction())
            );
            builder.line("Body Size: " + snapshot.rawBytes().length + " bytes", UiColorPalette.textBright());
            String source = describeChannelSource(snapshot.channel());
            if (source != null && !source.isBlank()) {
               builder.line("What It Looks Like: " + source, UiColorPalette.textBright());
            }

            builder.blankLine();
            builder.line("Decoder Status: waiting for automatic payload analysis.", UiColorPalette.warningYellow());
            return builder.build();
         } else {
            appendPayloadAnalysis(snapshot, builder, payloadAnalysis);
            return builder.build();
         }
      } catch (Throwable t) {
         PacketInspectorEngine.ReportBuilder builder = new PacketInspectorEngine.ReportBuilder("Payload Analyzer");
         builder.section("Analyzer Error", UiColorPalette.errorText());
         builder.line("Reason: " + t.getClass().getSimpleName() + (t.getMessage() == null ? "" : " - " + sanitize(t.getMessage())), UiColorPalette.errorText());
         return builder.build();
      }
   }

   public static PacketInspectorEngine.PayloadAnalysis analyzePayload(AutismPayloadSupport.PayloadEntry snapshot) {
      String channel = snapshot == null ? "payload" : orFallback(snapshot.channel(), "payload");
      return new PacketInspectorEngine.PayloadAnalysis(
         false, false, 0.02, "Preparing " + channel, List.of(new PacketInspectorEngine.ColoredLine("Decoder Status: preparing automatic payload analysis", UiColorPalette.textBody()))
      );
   }

   public static PacketInspectorEngine.PayloadAnalysis failedAnalysis(String message) {
      String reason = message != null && !message.isBlank() ? message : "unknown error";
      return new PacketInspectorEngine.PayloadAnalysis(
         true,
         true,
         1.0,
         "Decoder failed",
         List.of(new PacketInspectorEngine.ColoredLine("Decoder Status: failed", UiColorPalette.errorText()), new PacketInspectorEngine.ColoredLine("Reason: " + sanitize(reason), UiColorPalette.errorText()))
      );
   }

   public static PacketInspectorEngine.PayloadAnalysis analyzePayloadWithProgress(
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
            runPayloadAnalysis(snapshot, builder, progressSink);
            PacketInspectorEngine.PayloadAnalysis done = new PacketInspectorEngine.PayloadAnalysis(true, false, 1.0, analysisVerdict(builder.lines), builder.lines);
            if (progressSink != null) {
               progressSink.accept(done);
            }

            return done;
         } catch (Throwable t) {
            PacketInspectorEngine.PayloadAnalysis failed = failedAnalysis(t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage()));
            if (progressSink != null) {
               progressSink.accept(failed);
            }

            return failed;
         }
      }
   }

   private static String analysisVerdict(List<PacketInspectorEngine.ColoredLine> lines) {
      if (lines != null) {
         for (PacketInspectorEngine.ColoredLine line : lines) {
            String text = line == null ? "" : line.text();
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

   private static PacketInspectorEngine.PacketSummaryFlags describePacket(
      Packet<?> packet, String packetNameHint, AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.ReportBuilder builder
   ) {
      PacketInspectorEngine.PacketSummaryFlags packetSpecific = describeKnownPacket(packet, packetNameHint, entry, builder);
      return packetSpecific.init_Z() ? packetSpecific : describeProperties(packet, builder);
   }

   private static boolean appendPayloadHeader(AutismPayloadSupport.PayloadEntry snapshot, PacketInspectorEngine.ReportBuilder builder) {
      if (snapshot == null) {
         return false;
      }

      byte[] raw = snapshot.rawBytes();
      String channel = orFallback(snapshot.channel(), "unknown");
      builder.line("Channel: " + channel, UiColorPalette.successText());
      PayloadChannelFilterManager.MatchedRule filter = matchingFilterRule(snapshot);
      if (filter != null) {
         builder.line("Matched Filter: " + filter.load() + " (" + filter.pattern() + ")", UiColorPalette.mint());
      }

      String source = describeChannelSource(channel);
      if (source != null && !source.isBlank()) {
         builder.line("Likely Source: " + source, UiColorPalette.textBright());
      }

      builder.line(
         "Payload Direction: " + snapshot.direction() + " | Protocol: " + orFallback(snapshot.protocolPhase(), "unknown"), directionColor(snapshot.direction())
      );
      if (snapshot.packetId() >= 0) {
         builder.line("Payload Packet ID: " + snapshot.packetId(), UiColorPalette.textBody());
      }

      builder.line("Payload Class: " + orFallback(snapshot.payloadClassName(), "unknown"), UiColorPalette.textBody());
      builder.line("Payload Size: " + raw.length + " bytes", UiColorPalette.textBright());
      if (snapshot.commandApiValue() != null) {
         builder.line("CommandApi Value: " + snapshot.commandApiValue(), UiColorPalette.successText());
      }

      PayloadFieldCodec.PayloadPreview preview = PayloadFieldCodec.buildPreview(raw);
      if (isRegisterChannel(channel)) {
         List<String> channels = identifierPreview(raw, 10);
         if (!channels.isEmpty()) {
            int total = countChannelIds(raw);
            builder.line((isUnregisterChannel(channel) ? "Unregistered" : "Registered") + " Channels: " + total, UiColorPalette.successText());

            for (String value : channels) {
               builder.line("  - " + value, UiColorPalette.textBright());
            }

            if (total > channels.size()) {
               builder.line("  ... +" + (total - channels.size()) + " more", UiColorPalette.textMuted());
            }
         }
      }

      if (preview.mcStringPreview() != null && !preview.mcStringPreview().startsWith("<")) {
         builder.line("Minecraft String: " + quote(sanitize(preview.mcStringPreview())), UiColorPalette.successText());
      }

      if (preview.javaUtfPreview() != null && !preview.javaUtfPreview().startsWith("<")) {
         builder.line("Java writeUTF: " + quote(sanitize(preview.javaUtfPreview())), UiColorPalette.successText());
      }

      if (preview.utf8Preview() != null && !preview.utf8Preview().isBlank() && !"<binary>".equals(preview.utf8Preview())) {
         builder.line("UTF-8 Guess: " + quote(sanitize(collapseControlChars(preview.utf8Preview()))), UiColorPalette.successText());
      }

      List<String> identifiers = identifierPreview(raw, 6);
      if (!identifiers.isEmpty() && !isRegisterChannel(channel)) {
         builder.line("Identifier Tokens: " + identifiers.size(), UiColorPalette.textBright());

         for (String identifier : identifiers) {
            builder.line("  - " + identifier, UiColorPalette.textBody());
         }
      }

      builder.line("Payload Hex: " + preview.hexPreview(), UiColorPalette.textMuted());
      builder.line("Analyzer: automatic payload decoding runs when this packet is inspected.", UiColorPalette.textBody());
      return true;
   }

   private static void appendLines(PacketInspectorEngine.ReportBuilder builder, List<PacketInspectorEngine.ColoredLine> lines) {
      if (builder != null && lines != null) {
         for (PacketInspectorEngine.ColoredLine line : lines) {
            if (line != null) {
               builder.line(line.text(), line.color());
            }
         }
      }
   }

   private static PayloadChannelFilterManager.MatchedRule matchingFilterRule(AutismPayloadSupport.PayloadEntry snapshot) {
      if (snapshot == null) {
         return null;
      }

      try {
         return new PayloadChannelFilterManager().findMatchingRuleForEntry(snapshot, snapshot.direction());
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static PacketInspectorEngine.PacketSummaryFlags describeKnownPacket(
      Packet<?> packet, String packetNameHint, AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.ReportBuilder builder
   ) {
      boolean wrote = false;
      boolean complete = false;
      if (packet instanceof ServerboundPlayerActionPacket playerAction) {
         wrote = true;
         complete = true;
         builder.line("Action: " + playerAction.getAction(), UiColorPalette.textBright());
         builder.line("Meaning: " + describePlayerAction(playerAction.getAction()), UiColorPalette.textBright());
         builder.line("Block Pos: " + formatPos(playerAction.getPos()), UiColorPalette.textBright());
         builder.line("Face: " + playerAction.getDirection(), UiColorPalette.textBright());
         builder.line("Sequence: " + playerAction.getSequence(), UiColorPalette.textBody());
         String capturedBlockState = entry == null ? null : entry.capturedBlockState;
         if (capturedBlockState != null && !capturedBlockState.isBlank()) {
            builder.line("Block: " + capturedBlockState, UiColorPalette.successText());
         } else {
            BlockState state = blockStateAt(playerAction.getPos());
            if (state != null) {
               builder.line("Block: " + describeBlockState(state), UiColorPalette.successText());
            }
         }

         if (MC.player != null
            && (
               playerAction.getAction() == Action.DROP_ITEM
                  || playerAction.getAction() == Action.DROP_ALL_ITEMS
                  || playerAction.getAction() == Action.RELEASE_USE_ITEM
            )) {
            builder.line("Held Item: " + describeStack(MC.player.getMainHandItem()), UiColorPalette.successText());
         }
      }

      if (packet instanceof ServerboundUseItemOnPacket interactBlock) {
         BlockHitResult hit = interactBlock.hitResult();
         wrote = true;
         complete = true;
         builder.line("Interaction: Use Block (right click targeted block)", UiColorPalette.textBright());
         builder.line("Hand: " + interactBlock.hand(), UiColorPalette.textBright());
         builder.line("Block Pos: " + formatPos(hit.getBlockPos()), UiColorPalette.textBright());
         builder.line("Side: " + hit.getDirection(), UiColorPalette.textBright());
         builder.line("Hit Pos: " + formatVec(hit.getLocation()), UiColorPalette.textBright());
         builder.line("Inside Block: " + hit.isInside(), UiColorPalette.textBright());
         builder.line("Sequence: " + interactBlock.sequence(), UiColorPalette.textBody());
         if (MC.player != null) {
            builder.line("Held Item: " + describeStack(MC.player.getItemInHand(interactBlock.hand())), UiColorPalette.successText());
         }

         String capturedBlockState = entry == null ? null : entry.capturedBlockState;
         if (capturedBlockState != null && !capturedBlockState.isBlank()) {
            builder.line("Block: " + capturedBlockState, UiColorPalette.successText());
         } else {
            BlockState state = blockStateAt(hit.getBlockPos());
            if (state != null) {
               builder.line("Block: " + describeBlockState(state), UiColorPalette.successText());
            }
         }
      }

      if (packet instanceof ServerboundUseItemPacket interactItem) {
         wrote = true;
         complete = true;
         builder.line("Interaction: Use Item (right click without a targeted block)", UiColorPalette.textBright());
         builder.line("Hand: " + interactItem.hand(), UiColorPalette.textBright());
         builder.line("Sequence: " + interactItem.sequence(), UiColorPalette.textBody());
         builder.line("Yaw: " + interactItem.yRot(), UiColorPalette.textBody());
         builder.line("Pitch: " + interactItem.xRot(), UiColorPalette.textBody());
         if (MC.player != null) {
            if (firstGetterValue(MC.player.getInventory(), "getSelectedSlot", "selectedSlot") instanceof Number number) {
               builder.line("Selected Slot: " + describeHotbarSlot(number.intValue()), UiColorPalette.textBright());
            }

            builder.line("Held Item: " + describeStack(MC.player.getItemInHand(interactItem.hand())), UiColorPalette.successText());
         }
      }

      if (packet instanceof ServerboundInteractPacket interactEntity) {
         wrote = true;
         complete = true;
         int entityId = interactEntity.entityId();
         appendEntity(builder, "Entity Id", entityId);
         builder.line("Player Sneaking: " + interactEntity.usingSecondaryAction(), UiColorPalette.textBright());
         PacketInspectorEngine.InteractEntityCapture capture = captureInteract(interactEntity);
         if (capture.action != null) {
            builder.line("Interaction: " + capture.action, UiColorPalette.textBright());
         }

         if (capture.hand != null) {
            builder.line("Hand: " + capture.hand, UiColorPalette.textBright());
            if (MC.player != null) {
               builder.line("Held Item: " + describeStack(MC.player.getItemInHand(capture.hand)), UiColorPalette.successText());
            }
         }

         if (capture.hitPos != null) {
            builder.line("Hit Pos: " + formatVec(capture.hitPos), UiColorPalette.textBright());
         }
      }

      if (packet instanceof ServerboundAttackPacket attack) {
         wrote = true;
         complete = true;
         appendEntity(builder, "Entity Id", attack.entityId());
         builder.line("Interaction: Attack", UiColorPalette.textBright());
         if (MC.player != null) {
            builder.line("Held Item: " + describeStack(MC.player.getMainHandItem()), UiColorPalette.successText());
         }
      }

      if (packet instanceof ClientboundOpenScreenPacket openScreen) {
         wrote = true;
         complete = true;
         builder.line("Sync Id: " + openScreen.getContainerId(), UiColorPalette.textBright());
         builder.line("Screen Type: " + describeValue(openScreen.getType()), UiColorPalette.textBright());
         builder.line("Title: " + formatValue(openScreen.getTitle()), UiColorPalette.successText());
      }

      if (packet instanceof ClientboundContainerSetSlotPacket slotUpdate) {
         wrote = true;
         complete = true;
         builder.line("Sync Id: " + slotUpdate.getContainerId(), UiColorPalette.textBright());
         builder.line("Revision: " + slotUpdate.getStateId(), UiColorPalette.textBody());
         builder.line("Slot: " + slotUpdate.getSlot(), UiColorPalette.textBright());
         builder.line("Stack: " + describeStack(slotUpdate.getItem()), UiColorPalette.successText());
      }

      if (packet instanceof ClientboundContainerSetContentPacket inventory) {
         wrote = true;
         builder.line("Sync Id: " + inventory.containerId(), UiColorPalette.textBright());
         builder.line("Revision: " + inventory.stateId(), UiColorPalette.textBody());
         builder.line("Items: " + inventory.items().size() + " slots (" + countNonEmpty(inventory.items()) + " non-empty)", UiColorPalette.textBright());
         builder.line("Cursor: " + describeStack(inventory.carriedItem()), UiColorPalette.successText());
      }

      if (packet instanceof ClientboundLevelChunkWithLightPacket chunkData) {
         wrote = true;
         builder.line("Chunk: " + chunkData.x() + ", " + chunkData.z(), UiColorPalette.textBright());
         builder.line("Chunk Data: " + describeValue(chunkData.chunkData()), UiColorPalette.textBody());
         builder.line("Light Data: " + describeValue(chunkData.lightData()), UiColorPalette.textBody());
      }

      if (packet instanceof ClientboundEntityPositionSyncPacket positionSync) {
         wrote = true;
         appendEntity(builder, "Entity Id", positionSync.id());
         builder.line("Values: " + describeValue(Compat263.entityPosSyncValues(positionSync)), UiColorPalette.textBright());
         builder.line("On Ground: " + positionSync.onGround(), UiColorPalette.textBright());
      }

      if (packet instanceof ClientboundSetEntityDataPacket trackerUpdate) {
         wrote = true;
         appendEntity(builder, "Entity Id", trackerUpdate.id());
         List<?> trackedValues = trackerUpdate.packedItems();
         builder.line("Tracked Values: " + (trackedValues == null ? 0 : trackedValues.size()), UiColorPalette.textBright());
      }

      if (packet instanceof ClientboundAddEntityPacket addEntity) {
         wrote = true;
         complete = true;
         appendEntity(builder, "Entity Id", addEntity.getId());
         builder.line("Entity Type: " + entityTypeId(addEntity.getType()), UiColorPalette.successText());
         builder.line("Uuid: " + addEntity.getUUID(), UiColorPalette.textBody());
         builder.line("Pos: " + formatVec(new Vec3(addEntity.getX(), addEntity.getY(), addEntity.getZ())), UiColorPalette.textBright());
         builder.line("Movement: " + formatVec(addEntity.getMovement()), UiColorPalette.textBright());
         builder.line("Rot: yaw=" + addEntity.getYRot() + ", pitch=" + addEntity.getXRot() + ", head=" + addEntity.getYHeadRot(), UiColorPalette.textBright());
         builder.line("Data: " + addEntity.getData(), UiColorPalette.textBody());
      }

      if (packet instanceof ClientboundSoundPacket soundPacket) {
         wrote = true;
         complete = true;
         builder.line("Sound: " + describeHolder(soundPacket.getSound()), UiColorPalette.successText());
         builder.line("Category: " + soundPacket.getSource(), UiColorPalette.textBright());
         builder.line("Pos: " + formatVec(new Vec3(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ())), UiColorPalette.textBright());
         builder.line("Volume: " + soundPacket.getVolume(), UiColorPalette.textBody());
         builder.line("Pitch: " + soundPacket.getPitch(), UiColorPalette.textBody());
         builder.line("Seed: " + soundPacket.getSeed(), UiColorPalette.textMuted());
      }

      if (packet instanceof ClientboundGameEventPacket gameState) {
         wrote = true;
         complete = true;
         builder.line("Reason: " + describeValue(gameState.getEvent()), UiColorPalette.textBright());
         builder.line("Value: " + gameState.getParam(), UiColorPalette.textBright());
      }

      PacketInspectorEngine.PacketSummaryFlags broadKnown = describeBroadKnownPacket(packet, builder);
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
         builder.line("Channel: " + payloadSnapshot.channel(), UiColorPalette.textBright());
         if (payloadSnapshot.protocolPhase() != null && !payloadSnapshot.protocolPhase().isBlank()) {
            builder.line("Payload Protocol: " + payloadSnapshot.protocolPhase(), UiColorPalette.textBody());
         }

         builder.line("Payload Class: " + payloadSnapshot.payloadClassName(), UiColorPalette.textBody());
         builder.line("Payload Size: " + payloadSnapshot.byteLength() + " bytes", UiColorPalette.textBright());
         if (payloadSnapshot.commandApiValue() != null) {
            builder.line("CommandApi Value: " + payloadSnapshot.commandApiValue(), UiColorPalette.successText());
         }

         PayloadFieldCodec.PayloadPreview preview = PayloadFieldCodec.buildPreview(payloadSnapshot.rawBytes());
         if (preview.mcStringPreview() != null && !preview.mcStringPreview().startsWith("<")) {
            builder.line("Minecraft String: " + preview.mcStringPreview(), UiColorPalette.successText());
         }

         if (preview.javaUtfPreview() != null && !preview.javaUtfPreview().startsWith("<")) {
            builder.line("Java writeUTF: " + preview.javaUtfPreview(), UiColorPalette.successText());
         }

         if (preview.utf8Preview() != null && !preview.utf8Preview().isBlank() && !"<binary>".equals(preview.utf8Preview())) {
            builder.line("UTF-8 Guess: " + preview.utf8Preview(), UiColorPalette.successText());
         }

         builder.line("Payload Hex: " + preview.hexPreview(), UiColorPalette.textMuted());
         builder.line("Payload Fields: raw bytes preserved; use Edit Payload for typed builder.", UiColorPalette.successText());
         complete = false;
      }

      PacketInspectorEngine.PacketSummaryFlags reflective = describeReflectivePacket(packet, packetNameHint, entry, builder);
      return !wrote && reflective.init_Z() ? reflective : new PacketInspectorEngine.PacketSummaryFlags(wrote, complete);
   }

   private static boolean appendPayloadAnalysis(
      AutismPayloadSupport.PayloadEntry snapshot, PacketInspectorEngine.ReportBuilder builder, PacketInspectorEngine.PayloadAnalysis payloadAnalysis
   ) {
      if (payloadAnalysis == null) {
         return runPayloadAnalysis(snapshot, builder, (Consumer<PacketInspectorEngine.PayloadAnalysis>)null);
      }

      for (PacketInspectorEngine.ColoredLine line : payloadAnalysis.lines()) {
         builder.line(line.text(), line.color());
      }

      return true;
   }

   private static boolean runPayloadAnalysis(
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
      reportProgress(builder, progressSink, 0.06, "Reading payload metadata", false, false);
      builder.line("Channel: " + snapshot.channel(), UiColorPalette.successText());
      builder.line("Direction: " + snapshot.direction() + " | Protocol: " + orFallback(snapshot.protocolPhase(), "unknown"), directionColor(snapshot.direction()));
      builder.line("Body Size: " + raw.length + " bytes", UiColorPalette.textBright());
      PayloadChannelFilterManager.MatchedRule filter = matchingFilterRule(snapshot);
      if (filter != null) {
         builder.line("Matched Filter: " + filter.load() + " (" + filter.pattern() + ")", UiColorPalette.mint());
      }

      String source = describeChannelSource(snapshot.channel());
      if (source != null && !source.isBlank()) {
         builder.line("What It Looks Like: " + source, UiColorPalette.textBright());
      }

      if (snapshot.commandApiValue() != null) {
         builder.line("CommandApi Int: " + snapshot.commandApiValue(), UiColorPalette.successText());
         decodedAnything = true;
      }

      appendCompression(raw, builder);
      reportProgress(builder, progressSink, 0.2, "Checking plugin-channel structure", false, false);
      PacketInspectorEngine.PayloadDecodeResult knownChannel = decodeKnownChannel(snapshot, raw, builder);
      decodedAnything |= knownChannel.init_Z();
      decodedStrongly |= knownChannel.load_Z();
      textualPayload |= knownChannel.read();
      completeKnownSchema |= knownChannel.write();
      if (knownChannel.init_Z()) {
         bestInterpretation = interpretationFor(snapshot.channel(), completeKnownSchema);
      }

      PacketInspectorEngine.PayloadDecodeResult decompressed = PacketInspectorEngine.PayloadDecodeResult.NONE;
      if (!completeKnownSchema) {
         decompressed = decodeCompressed(snapshot, raw, builder);
         decodedAnything |= decompressed.init_Z();
         decodedStrongly |= decompressed.load_Z();
         textualPayload |= decompressed.read();
         completeKnownSchema |= decompressed.write();
         if (decompressed.init_Z()) {
            bestInterpretation = "Compressed payload with nested decoded content";
         }
      }

      reportProgress(builder, progressSink, 0.36, "Trying JSON decoders", false, false);
      if (!completeKnownSchema) {
         JsonElement json = parseJsonPayload(raw);
         if (json != null) {
            describeJson(json, builder);
            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            completeKnownSchema = true;
            bestInterpretation = "JSON payload";
         } else {
            JsonElement embeddedJson = findEmbeddedJson(raw);
            if (embeddedJson != null) {
               builder.line("Decoded As: embedded JSON", UiColorPalette.successText());
               describeJson(embeddedJson, builder);
               decodedStrongly = true;
               decodedAnything = true;
               textualPayload = true;
               bestInterpretation = "Payload containing embedded JSON";
            }
         }
      }

      reportProgress(builder, progressSink, 0.52, "Trying string codecs", false, false);
      String minecraftString = completeKnownSchema ? null : AutismPayloadSupport.decodeMcString(raw);
      if (!completeKnownSchema) {
         if (minecraftString != null && !minecraftString.isBlank()) {
            builder.line("Decoded As: Minecraft String", UiColorPalette.successText());
            builder.line("Value: " + quote(sanitize(minecraftString)), UiColorPalette.textBright());
            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Single Minecraft ByteBuf string";
            }
         }

         List<String> minecraftStrings = readMcStrings(raw, 12);
         if (minecraftStrings.size() > 1 || minecraftStrings.size() == 1 && !Objects.equals(minecraftStrings.get(0), minecraftString)) {
            builder.line("Minecraft String Sequence: " + minecraftStrings.size() + " value" + pluralSuffix(minecraftStrings.size()), UiColorPalette.successText());

            for (String value : minecraftStrings) {
               builder.line("  - " + quote(sanitize(value)), UiColorPalette.textBright());
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
            builder.line("Decoded As: Java writeUTF", UiColorPalette.successText());
            builder.line("Value: " + quote(sanitize(javaUtf)), UiColorPalette.textBright());
            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Single Java DataOutput writeUTF string";
            }
         }

         List<String> javaUtfStrings = readJavaUtfStrings(raw, 12);
         if (javaUtfStrings.size() > 1 || javaUtfStrings.size() == 1 && !Objects.equals(javaUtfStrings.get(0), javaUtf)) {
            builder.line("Java writeUTF Sequence: " + javaUtfStrings.size() + " value" + pluralSuffix(javaUtfStrings.size()), UiColorPalette.successText());

            for (String value : javaUtfStrings) {
               builder.line("  - " + quote(sanitize(value)), UiColorPalette.textBright());
            }

            decodedStrongly = true;
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Sequence of Java DataOutput writeUTF strings";
            }
         }

         if (appendNullSeparated(raw, builder)) {
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Null-separated text fields";
            }
         }

         String utf8 = AutismPayloadSupport.decodePrintableUtf8(raw);
         if (utf8 != null && !utf8.isBlank() && !Objects.equals(utf8, minecraftString) && !Objects.equals(utf8, javaUtf)) {
            builder.line("Readable UTF-8: " + quote(sanitize(collapseControlChars(utf8))), UiColorPalette.successText());
            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Readable UTF-8 text payload";
            }
         }
      }

      reportProgress(builder, progressSink, 0.7, "Extracting identifiers and fragments", false, false);
      if (!completeKnownSchema) {
         PacketInspectorEngine.PayloadDecodeResult byteBufScan = scanByteBufStrings(raw, builder);
         decodedAnything |= byteBufScan.init_Z();
         decodedStrongly |= byteBufScan.load_Z();
         textualPayload |= byteBufScan.read();
         if (byteBufScan.init_Z() && bestInterpretation.isBlank()) {
            bestInterpretation = "Minecraft ByteBuf-style payload with embedded string fields";
         }

         List<String> identifiers = identifierPreview(raw, 16);
         if (!identifiers.isEmpty() && !isRegisterChannel(snapshot.channel())) {
            builder.line("Identifier Tokens: " + identifiers.size(), UiColorPalette.textBright());

            for (String identifier : identifiers) {
               builder.line("  - " + identifier, UiColorPalette.textBody());
            }

            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Identifier-bearing custom payload";
            }
         }

         List<String> strings = stringPreview(raw, 8);
         if (!strings.isEmpty()) {
            builder.line("Readable Fragments:", UiColorPalette.textBright());

            for (String string : strings) {
               builder.line("  - " + quote(sanitize(string)), UiColorPalette.textBody());
            }

            decodedAnything = true;
            textualPayload = true;
            if (bestInterpretation.isBlank()) {
               bestInterpretation = "Payload with readable text fragments";
            }
         }
      }

      reportProgress(builder, progressSink, 0.78, "Running deep nested probes", false, false);
      if (!completeKnownSchema) {
         PacketInspectorEngine.PayloadDecodeResult deep = decodeDeep(raw, builder);
         decodedAnything |= deep.init_Z();
         decodedStrongly |= deep.load_Z();
         textualPayload |= deep.read();
         completeKnownSchema |= deep.write();
         if (deep.init_Z() && bestInterpretation.isBlank()) {
            bestInterpretation = deep.load_Z() ? "Nested or encoded payload container" : "Payload with recoverable embedded clues";
         }
      }

      reportProgress(builder, progressSink, 0.84, textualPayload ? "Checking confidence" : "Reading numeric and binary hints", false, false);
      if (!textualPayload) {
         appendRawHints(raw, builder);
      }

      if (!completeKnownSchema) {
         appendByteStats(raw, builder);
      }

      if (raw.length == 0) {
         builder.line("Body is empty.", UiColorPalette.textMuted());
      } else if (!bestInterpretation.isBlank()) {
         builder.line("Best Interpretation: " + bestInterpretation, !completeKnownSchema && !decodedStrongly ? UiColorPalette.textBright() : UiColorPalette.successText());
      }

      if (raw.length != 0) {
         if (completeKnownSchema) {
            builder.line("Confidence: complete known-schema decode. Payload body is accounted for.", UiColorPalette.successText());
         } else if (decodedStrongly) {
            builder.line("Confidence: high. A full structured/text decoder matched this payload.", UiColorPalette.successText());
         } else if (!decodedAnything) {
            builder.line("Binary Payload: no safe schema/text decoder matched. Exact bytes are preserved.", UiColorPalette.textMuted());
         } else if (!decodedStrongly) {
            builder.line("Confidence: partial. Analyzer found readable hints, but no complete schema matched.", UiColorPalette.textMuted());
         }
      }

      String finalStatus = completeKnownSchema
         ? "Known schema decoded"
         : (decodedStrongly ? "High-confidence analysis" : (decodedAnything ? "Partial analysis complete" : "Binary analysis complete"));
      reportProgress(builder, progressSink, 1.0, finalStatus, true, false);
      return true;
   }

   private static void reportProgress(
      PacketInspectorEngine.ReportBuilder builder, Consumer<PacketInspectorEngine.PayloadAnalysis> sink, double progress, String status, boolean done, boolean failed
   ) {
      if (sink != null) {
         sink.accept(new PacketInspectorEngine.PayloadAnalysis(done, failed, progress, status, builder.lines));
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeKnownChannel(
      AutismPayloadSupport.PayloadEntry snapshot, byte[] raw, PacketInspectorEngine.ReportBuilder builder
   ) {
      if (snapshot == null) {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }

      String channel = normalizeChannel(snapshot.channel());
      if (isRegisterChannel(channel)) {
         boolean decodedRegister = decodeRegisterChannels(snapshot, raw, builder);
         return decodedRegister ? PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE : PacketInspectorEngine.PayloadDecodeResult.NONE;
      }

      if ("minecraft:brand".equals(channel)) {
         String brand = AutismPayloadSupport.decodeMcString(raw);
         String mode = "Minecraft String";
         if (brand == null || brand.isBlank()) {
            brand = AutismPayloadSupport.decodePrintableUtf8(raw);
            mode = "raw UTF-8";
         }

         if (brand != null && !brand.isBlank()) {
            builder.line("Decoded As: Brand payload (" + mode + ")", UiColorPalette.successText());
            builder.line("Brand: " + quote(sanitize(collapseControlChars(brand))), UiColorPalette.textBright());
            return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
         } else {
            return PacketInspectorEngine.PayloadDecodeResult.NONE;
         }
      } else {
         if ("vv:server_details".equals(channel)) {
            JsonElement json = parseJsonPayload(raw);
            if (json != null) {
               builder.line("Decoded As: ViaVersion server details JSON", UiColorPalette.successText());
               describeJson(json, builder);
               return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
            }
         }

         if (!"bungeecord".equals(channel) && !"bungeecord:main".equals(channel)) {
            if (channel.startsWith("fabric:")) {
               List<String> values = readMcStrings(raw, 16);
               if (values.isEmpty()) {
                  values = readJavaUtfStrings(raw, 16);
               }

               if (!values.isEmpty()) {
                  builder.line("Decoded As: Fabric readable payload fields", UiColorPalette.successText());

                  for (String value : values) {
                     builder.line("  - " + quote(sanitize(collapseControlChars(value))), UiColorPalette.textBright());
                  }

                  return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
               }

               String utf8 = AutismPayloadSupport.decodePrintableUtf8(raw);
               if (utf8 != null && !utf8.isBlank()) {
                  builder.line("Decoded As: Fabric readable text payload", UiColorPalette.successText());
                  builder.line("Text: " + quote(sanitize(collapseControlChars(utf8))), UiColorPalette.textBright());
                  return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
               }
            }

            if (channel.startsWith("worldedit:")) {
               String utf8 = AutismPayloadSupport.decodePrintableUtf8(raw);
               if (utf8 != null && !utf8.isBlank()) {
                  builder.line("Decoded As: WorldEdit/CUI text payload", UiColorPalette.successText());
                  builder.line("Message: " + quote(sanitize(collapseControlChars(utf8))), UiColorPalette.textBright());
                  return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
               }
            }

            return PacketInspectorEngine.PayloadDecodeResult.NONE;
         } else {
            return decodeBungeeCordMessage(raw, builder);
         }
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeBungeeCordMessage(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      PacketInspectorEngine.ByteCursor reader = new PacketInspectorEngine.ByteCursor(raw);

      String subchannel;
      try {
         subchannel = reader.readJavaUtf();
      } catch (IOException ignored) {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }

      if (subchannel != null && !subchannel.isBlank()) {
         builder.line("Decoded As: BungeeCord/DataOutput plugin message", UiColorPalette.successText());
         builder.line("Subchannel: " + subchannel, UiColorPalette.textBright());
         String normalized = subchannel.toLowerCase(Locale.ROOT);
         boolean known = true;

         try {
            switch (normalized) {
               case "connect":
                  builder.line("Target Server: " + reader.readJavaUtf(), UiColorPalette.textBright());
                  break;
               case "connectother":
                  builder.line("Player: " + reader.readJavaUtf(), UiColorPalette.textBright());
                  builder.line("Target Server: " + reader.readJavaUtf(), UiColorPalette.textBright());
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
                  builder.line("Argument: " + reader.readJavaUtf(), UiColorPalette.textBright());
                  break;
               case "message":
               case "messageraw":
               case "kickplayer":
                  builder.line("Player: " + reader.readJavaUtf(), UiColorPalette.textBright());
                  builder.line("Message: " + quote(sanitize(reader.readJavaUtf())), UiColorPalette.textBright());
                  break;
               case "forward": {
                  builder.line("Target Server: " + reader.readJavaUtf(), UiColorPalette.textBright());
                  String nestedChannel = reader.readJavaUtf();
                  builder.line("Nested Channel: " + nestedChannel, UiColorPalette.successText());
                  byte[] nested = reader.readShortPrefixedBytes();
                  decodeNestedBytes("Nested Forward Payload", nestedChannel, nested, builder);
                  break;
               }
               case "forwardtoplayer": {
                  builder.line("Target Player: " + reader.readJavaUtf(), UiColorPalette.textBright());
                  String nestedChannel = reader.readJavaUtf();
                  builder.line("Nested Channel: " + nestedChannel, UiColorPalette.successText());
                  byte[] nested = reader.readShortPrefixedBytes();
                  decodeNestedBytes("Nested Forward Payload", nestedChannel, nested, builder);
                  break;
               }
               default:
                  known = false;
                  List<String> values = new ArrayList<>();

                  while (reader.remaining() > 0 && values.size() < 48) {
                     int before = reader.position();

                     try {
                        String value = reader.readJavaUtf();
                        if (!isMostlyPrintableText(value)) {
                           reader.seek(before);
                           break;
                        }

                        values.add(value);
                     } catch (IOException ex) {
                        reader.seek(before);
                        break;
                     }
                  }

                  if (!values.isEmpty()) {
                     builder.line("Additional UTF Fields: " + values.size(), UiColorPalette.textBright());

                     for (String value : values) {
                        builder.line("  - " + quote(sanitize(value)), UiColorPalette.textBody());
                     }
                  }
            }
         } catch (IOException ex) {
            builder.line("BungeeCord Parse Stopped: " + sanitize(ex.getMessage()), UiColorPalette.warningYellow());
            return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, false);
         }

         if (reader.remaining() > 0) {
            builder.line("Remaining Bytes: " + reader.remaining() + "B " + AutismPayloadSupport.toHexPreview(reader.remainingBytes(), 48), UiColorPalette.textMuted());
         }

         return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, known && reader.remaining() == 0);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeCompressed(
      AutismPayloadSupport.PayloadEntry snapshot, byte[] raw, PacketInspectorEngine.ReportBuilder builder
   ) {
      PacketInspectorEngine.CompressionKind kind = detectCompression(raw);
      if (kind == PacketInspectorEngine.CompressionKind.NONE) {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      } else {
         byte[] decoded = decompress(raw, kind);
         if (decoded != null && decoded.length != 0) {
            builder.line("Decoded As: " + kind.label() + " compressed payload", UiColorPalette.successText());
            builder.line("Uncompressed Size: " + decoded.length + " bytes", UiColorPalette.textBright());
            PacketInspectorEngine.PayloadDecodeResult nested = decodeNestedBytes("Uncompressed Payload", snapshot == null ? "" : snapshot.channel(), decoded, builder);
            return nested.init_Z() ? new PacketInspectorEngine.PayloadDecodeResult(true, true, nested.read(), nested.write()) : new PacketInspectorEngine.PayloadDecodeResult(true, true, false, false);
         } else {
            builder.line("Compression Decode: " + kind.label() + " header found, but decompression failed.", UiColorPalette.warningYellow());
            return new PacketInspectorEngine.PayloadDecodeResult(true, false, false, false);
         }
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeNestedBytes(String title, String channel, byte[] bytes, PacketInspectorEngine.ReportBuilder builder) {
      if (bytes == null) {
         bytes = new byte[0];
      }

      builder.line(title + ": " + bytes.length + " bytes", UiColorPalette.textBright());
      if (bytes.length == 0) {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }

      boolean anything = false;
      boolean strong = false;
      boolean textual = false;
      boolean complete = false;
      JsonElement json = parseJsonPayload(bytes);
      if (json != null) {
         builder.line("  Nested Decoded As: JSON", UiColorPalette.successText());
         describeJson(json, builder);
         return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
      }

      if (isRegisterChannel(channel)) {
         AutismPayloadSupport.PayloadEntry nestedSnapshot = new AutismPayloadSupport.PayloadEntry(
            "", channel, bytes, bytes.length, null, "", false, false, "", "", -1, ""
         );
         boolean register = decodeRegisterChannels(nestedSnapshot, bytes, builder);
         if (register) {
            return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
         }
      }

      String mcString = AutismPayloadSupport.decodeMcString(bytes);
      if (mcString != null && !mcString.isBlank()) {
         builder.line("  Nested Minecraft String: " + quote(sanitize(mcString)), UiColorPalette.successText());
         anything = true;
         strong = true;
         textual = true;
         complete = true;
      }

      List<String> javaUtf = readJavaUtfStrings(bytes, 8);
      if (!javaUtf.isEmpty()) {
         builder.line("  Nested Java UTF Fields: " + javaUtf.size(), UiColorPalette.successText());

         for (String value : javaUtf) {
            builder.line("    - " + quote(sanitize(value)), UiColorPalette.textBody());
         }

         anything = true;
         strong = true;
         textual = true;
         complete = true;
      }

      List<String> ids = identifierPreview(bytes, 8);
      if (!ids.isEmpty()) {
         builder.line("  Nested Identifiers: " + ids.size(), UiColorPalette.textBright());

         for (String id : ids) {
            builder.line("    - " + id, UiColorPalette.textBody());
         }

         anything = true;
         textual = true;
      }

      if (!anything) {
         String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
         if (utf8 != null && !utf8.isBlank()) {
            builder.line("  Nested UTF-8: " + quote(sanitize(collapseControlChars(utf8))), UiColorPalette.textBright());
            anything = true;
            textual = true;
         } else {
            builder.line("  Nested Hex: " + AutismPayloadSupport.toHexPreview(bytes, 64), UiColorPalette.textMuted());
         }
      }

      return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, complete);
   }

   private static PacketInspectorEngine.PayloadDecodeResult scanByteBufStrings(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length >= 2) {
         List<PacketInspectorEngine.WireFieldSlice> fields = scanWireFields(raw, 16);
         if (fields.isEmpty()) {
            return PacketInspectorEngine.PayloadDecodeResult.NONE;
         }

         builder.line("Minecraft ByteBuf String Fields: " + fields.size(), UiColorPalette.textBright());

         for (PacketInspectorEngine.WireFieldSlice field : fields) {
            builder.line("  @" + field.init_I() + " len=" + field.load() + ": " + quote(sanitize(field.init_Str())), UiColorPalette.textBody());
         }

         return new PacketInspectorEngine.PayloadDecodeResult(true, false, true, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeDeep(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length != 0) {
         boolean anything = false;
         boolean strong = false;
         boolean textual = false;
         PacketInspectorEngine.PayloadDecodeResult nbt = decodeNbt(raw, builder, "Payload NBT");
         anything |= nbt.init_Z();
         strong |= nbt.load_Z();
         textual |= nbt.read();
         String text = new String(raw, StandardCharsets.UTF_8);
         if (appendIdentifiers(text, builder)) {
            anything = true;
            textual = true;
         }

         PacketInspectorEngine.PayloadDecodeResult embedded = decodeEmbeddedBase64(text, builder);
         anything |= embedded.init_Z();
         strong |= embedded.load_Z();
         textual |= embedded.read();
         PacketInspectorEngine.PayloadDecodeResult lengthPrefixed = scanLengthPrefixed(raw, builder);
         anything |= lengthPrefixed.init_Z();
         strong |= lengthPrefixed.load_Z();
         textual |= lengthPrefixed.read();
         PacketInspectorEngine.PayloadDecodeResult rawUuid = decodeRawUuid(raw, builder);
         anything |= rawUuid.init_Z();
         return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeNbt(byte[] raw, PacketInspectorEngine.ReportBuilder builder, String label) {
      if (raw != null && raw.length >= 3) {
         CompoundTag compressed = readGzipNbt(raw);
         if (compressed != null) {
            builder.line(label + ": compressed NBT compound", UiColorPalette.successText());
            describeNbt(compressed, builder, "  ");
            return new PacketInspectorEngine.PayloadDecodeResult(true, true, true, true);
         } else {
            return PacketInspectorEngine.PayloadDecodeResult.NONE;
         }
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static CompoundTag readGzipNbt(byte[] raw) {
      if (detectCompression(raw) != PacketInspectorEngine.CompressionKind.GZIP) {
         return null;
      }

      try (ByteArrayInputStream input = new ByteArrayInputStream(raw)) {
         CompoundTag tag = NbtIo.readCompressed(input, MacroClipboardCodec.createNbtAccounter());
         return tag != null && !tag.isEmpty() ? tag : null;
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static void describeNbt(CompoundTag tag, PacketInspectorEngine.ReportBuilder builder, String indent) {
      if (tag != null) {
         builder.line(indent + "Keys: " + tag.size(), UiColorPalette.textBright());
         int shown = 0;

         for (String key : tag.keySet()) {
            if (shown >= 16) {
               builder.line(indent + "... +" + (tag.size() - shown) + " more", UiColorPalette.textMuted());
               break;
            }

            Tag value = tag.get(key);
            builder.line(indent + key + ": " + tagSummary(value), UiColorPalette.textBody());
            shown++;
         }
      }
   }

   private static String tagSummary(Tag tag) {
      if (tag == null) {
         return "null";
      }

      String value = tag.toString();
      return tag.getClass().getSimpleName() + " " + sanitize(value);
   }

   private static boolean appendIdentifiers(String text, PacketInspectorEngine.ReportBuilder builder) {
      if (text != null && !text.isBlank()) {
         boolean wrote = false;
         List<String> uuids = findAll(UUID_PATTERN, text, 8);
         if (!uuids.isEmpty()) {
            builder.line("UUID Tokens: " + uuids.size(), UiColorPalette.textBright());

            for (String uuid : uuids) {
               builder.line("  - " + uuid.toLowerCase(Locale.ROOT), UiColorPalette.textBody());
            }

            wrote = true;
         }

         List<String> hosts = findAll(HOST_PATTERN, text, 12);
         if (!hosts.isEmpty()) {
            builder.line("Host/IP Tokens: " + hosts.size(), UiColorPalette.textBright());

            for (String host : hosts) {
               builder.line("  - " + host, UiColorPalette.textBody());
            }

            wrote = true;
         }

         return wrote;
      } else {
         return false;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeEmbeddedBase64(String text, PacketInspectorEngine.ReportBuilder builder) {
      if (text != null && text.length() >= 16) {
         boolean anything = false;
         boolean strong = false;
         boolean textual = false;
         int shown = 0;
         Matcher base64 = BASE64_PATTERN.matcher(text);

         while (base64.find() && shown < 4) {
            String token = base64.group();
            byte[] decoded = decodeBase64(token);
            if (isPlausibleSize(decoded)) {
               PacketInspectorEngine.PayloadDecodeResult outcome = classifyBytes(decoded);
               if (outcome.init_Z()) {
                  builder.line("Embedded Base64 Payload @" + base64.start() + ": " + decoded.length + " bytes", UiColorPalette.successText());
                  PacketInspectorEngine.PayloadDecodeResult nested = decodeNestedBytes("  Base64 decoded", "", decoded, builder);
                  anything = true;
                  strong |= nested.load_Z() || outcome.load_Z();
                  textual |= nested.read() || outcome.read();
                  shown++;
               }
            }
         }

         shown = 0;
         Matcher hex = HEX_PATTERN.matcher(text);

         while (hex.find() && shown < 4) {
            String token = hex.group();
            byte[] decoded = decodeHex(token);
            if (isPlausibleSize(decoded)) {
               PacketInspectorEngine.PayloadDecodeResult outcome = classifyBytes(decoded);
               if (outcome.init_Z()) {
                  builder.line("Embedded Hex Payload @" + hex.start() + ": " + decoded.length + " bytes", UiColorPalette.successText());
                  PacketInspectorEngine.PayloadDecodeResult nested = decodeNestedBytes("  Hex decoded", "", decoded, builder);
                  anything = true;
                  strong |= nested.load_Z() || outcome.load_Z();
                  textual |= nested.read() || outcome.read();
                  shown++;
               }
            }
         }

         return new PacketInspectorEngine.PayloadDecodeResult(anything, strong, textual, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult scanLengthPrefixed(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length >= 5) {
         boolean anything = false;
         boolean strong = false;
         boolean textual = false;
         Set<String> seen = new LinkedHashSet<>();
         int shown = 0;

         for (int offset = 0; offset < raw.length - 2 && shown < 6; offset++) {
            PacketInspectorEngine.VarIntRead varInt = readVarInt(raw, offset);
            if (varInt != null) {
               int length = varInt.init();
               int start = varInt.load();
               if (isPlausibleLength(length, start, raw.length)) {
                  String key = start + ":" + length;
                  if (seen.add(key)) {
                     byte[] slice = Arrays.copyOfRange(raw, start, start + length);
                     PacketInspectorEngine.PayloadDecodeResult outcome = classifyBytes(slice);
                     if (outcome.init_Z()) {
                        builder.line("Nested Candidate: VarInt length @" + offset + " -> " + length + " bytes", UiColorPalette.textBright());
                        PacketInspectorEngine.PayloadDecodeResult nested = decodeNestedBytes("  Candidate Payload", "", slice, builder);
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
            if (isPlausibleLength(shortLength, shortStart, raw.length)) {
               String key = shortStart + ":" + shortLength;
               if (seen.add(key)) {
                  byte[] slice = Arrays.copyOfRange(raw, shortStart, shortStart + shortLength);
                  PacketInspectorEngine.PayloadDecodeResult outcome = classifyBytes(slice);
                  if (outcome.init_Z()) {
                     builder.line("Nested Candidate: unsigned-short length @" + offset + " -> " + shortLength + " bytes", UiColorPalette.textBright());
                     PacketInspectorEngine.PayloadDecodeResult nested = decodeNestedBytes("  Candidate Payload", "", slice, builder);
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
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult decodeRawUuid(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length >= 16) {
         List<String> uuids = new ArrayList<>();

         for (int offset = 0; offset + 16 <= raw.length && uuids.size() < 4; offset++) {
            if (!isMostlyPrintable(raw, offset, 16)) {
               ByteBuffer buffer = ByteBuffer.wrap(raw, offset, 16).order(ByteOrder.BIG_ENDIAN);
               UUID uuid = new UUID(buffer.getLong(), buffer.getLong());
               if ((uuid.getMostSignificantBits() != 0L || uuid.getLeastSignificantBits() != 0L) && isPlausibleUuid(uuid)) {
                  uuids.add("@" + offset + " " + uuid);
               }
            }
         }

         if (uuids.isEmpty()) {
            return PacketInspectorEngine.PayloadDecodeResult.NONE;
         }

         builder.line("Raw UUID Candidates: " + uuids.size(), UiColorPalette.textBright());

         for (String uuid : uuids) {
            builder.line("  - " + uuid, UiColorPalette.textBody());
         }

         return new PacketInspectorEngine.PayloadDecodeResult(true, false, false, false);
      } else {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      }
   }

   private static PacketInspectorEngine.PayloadDecodeResult classifyBytes(byte[] bytes) {
      if (bytes == null || bytes.length == 0) {
         return PacketInspectorEngine.PayloadDecodeResult.NONE;
      } else if (parseJsonPayload(bytes) != null) {
         return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
      } else if (AutismPayloadSupport.decodeMcString(bytes) != null) {
         return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
      } else if (!readJavaUtfStrings(bytes, 4).isEmpty()) {
         return PacketInspectorEngine.PayloadDecodeResult.FULL_DECODE;
      } else if (!identifierPreview(bytes, 4).isEmpty()) {
         return new PacketInspectorEngine.PayloadDecodeResult(true, false, true, false);
      } else {
         String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
         if (utf8 != null && utf8.length() >= 4) {
            return new PacketInspectorEngine.PayloadDecodeResult(true, false, true, false);
         } else {
            return detectCompression(bytes) != PacketInspectorEngine.CompressionKind.NONE
               ? new PacketInspectorEngine.PayloadDecodeResult(true, true, false, false)
               : PacketInspectorEngine.PayloadDecodeResult.NONE;
         }
      }
   }

   private static boolean isPlausibleLength(int length, int start, int totalLength) {
      if (length < 4 || length > 67108864) {
         return false;
      } else if (start >= 0 && start <= totalLength) {
         return start + length > totalLength ? false : length <= totalLength - start;
      } else {
         return false;
      }
   }

   private static boolean isPlausibleSize(byte[] bytes) {
      return bytes != null && bytes.length >= 4 && bytes.length <= 67108864;
   }

   private static List<String> findAll(Pattern pattern, String text, int max) {
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

   private static byte[] decodeBase64(String token) {
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

   private static byte[] decodeHex(String token) {
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

   private static boolean isMostlyPrintable(byte[] raw, int offset, int length) {
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

   private static boolean isPlausibleUuid(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      long most = uuid.getMostSignificantBits();
      int version = (int)(most >>> 12 & 15L);
      return version >= 1 && version <= 7;
   }

   private static boolean decodeRegisterChannels(AutismPayloadSupport.PayloadEntry snapshot, byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      List<String> nullSeparated = splitNullSeparated(raw, 48);
      List<String> identifiers = identifierPreview(raw, 48);
      LinkedHashSet<String> channels = new LinkedHashSet<>();
      channels.addAll(nullSeparated.stream().filter(value -> CHANNEL_ID_PATTERN.matcher(value).matches()).map(value -> value.toLowerCase(Locale.ROOT)).toList());
      channels.addAll(identifiers);
      if (channels.isEmpty()) {
         builder.line("Register Payload: no channel identifiers were readable.", UiColorPalette.textMuted());
         return false;
      }

      int total = Math.max(countChannelIds(raw), channels.size());
      builder.line(
         (isUnregisterChannel(snapshot.channel()) ? "Unregistered" : "Registered") + " Channels: " + channels.size() + (total > channels.size() ? "+ shown" : ""),
         UiColorPalette.successText()
      );
      int shown = 0;

      for (String channel : channels) {
         if (shown >= 48) {
            builder.line("  ... +" + (channels.size() - shown) + " more", UiColorPalette.textMuted());
            break;
         }

         builder.line("  - " + channel, UiColorPalette.textBright());
         shown++;
      }

      return true;
   }

   private static JsonElement findEmbeddedJson(byte[] raw) {
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

   private static boolean appendNullSeparated(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      List<String> parts = splitNullSeparated(raw, 16);
      if (parts.size() <= 1) {
         return false;
      }

      builder.line("Null-Separated Strings: " + parts.size() + " value" + pluralSuffix(parts.size()), UiColorPalette.successText());

      for (String part : parts) {
         builder.line("  - " + quote(sanitize(part)), UiColorPalette.textBright());
      }

      return true;
   }

   private static List<String> splitNullSeparated(byte[] raw, int max) {
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

            String cleaned = collapseControlChars(part);
            if (isMostlyPrintableText(cleaned)) {
               values.add(cleaned);
            }
         }

         return values;
      } else {
         return List.of();
      }
   }

   private static List<String> readMcStrings(byte[] raw, int max) {
      if (raw != null && raw.length != 0) {
         int index = 0;
         List<String> values = new ArrayList<>();

         while (index < raw.length && values.size() < Math.max(1, max)) {
            PacketInspectorEngine.VarIntRead length = readVarInt(raw, index);
            if (length == null || length.init() < 0 || length.init() > 32767) {
               return List.of();
            }

            index = length.load();
            if (index + length.init() > raw.length) {
               return List.of();
            }

            String value = new String(raw, index, length.init(), StandardCharsets.UTF_8);
            if (!isMostlyPrintableText(value)) {
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

   private static List<String> readJavaUtfStrings(byte[] raw, int max) {
      if (raw != null && raw.length >= 2) {
         List<String> values = new ArrayList<>();

         try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(raw))) {
            while (input.available() > 0 && values.size() < Math.max(1, max)) {
               String value = input.readUTF();
               if (!isMostlyPrintableText(value)) {
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

   private static PacketInspectorEngine.VarIntRead readVarInt(byte[] bytes, int startIndex) {
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

   private static List<PacketInspectorEngine.WireFieldSlice> scanWireFields(byte[] raw, int max) {
      if (raw != null && raw.length >= 2) {
         List<PacketInspectorEngine.WireFieldSlice> fields = new ArrayList<>();
         Set<Integer> occupied = new LinkedHashSet<>();

         for (int offset = 0; offset < raw.length && fields.size() < Math.max(1, max); offset++) {
            if (!occupied.contains(offset)) {
               PacketInspectorEngine.VarIntRead length = readVarInt(raw, offset);
               if (length != null) {
                  int valueLength = length.init();
                  if (valueLength >= 1 && valueLength <= 32767) {
                     int start = length.load();
                     if (start + valueLength <= raw.length) {
                        String value = new String(raw, start, valueLength, StandardCharsets.UTF_8);
                        if (isMostlyPrintableText(value)) {
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

   private static PacketInspectorEngine.CompressionKind detectCompression(byte[] raw) {
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

   private static byte[] decompress(byte[] raw, PacketInspectorEngine.CompressionKind kind) {
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

   private static void appendRawHints(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      if (raw != null && raw.length != 0) {
         List<String> varInts = varIntPreview(raw, 5);
         if (!varInts.isEmpty()) {
            builder.line("VarInt Prefix: " + String.join(", ", varInts), UiColorPalette.textBody());
         }

         if (raw.length >= 1) {
            builder.line("First Byte: unsigned=" + (raw[0] & 0xFF) + ", signed=" + raw[0], UiColorPalette.textMuted());
         }

         if (raw.length >= 2) {
            ByteBuffer be = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer le = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            builder.line("First Short: BE=" + be.getShort(0) + ", LE=" + le.getShort(0), UiColorPalette.textMuted());
         }

         if (raw.length >= 4) {
            ByteBuffer be = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer le = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            builder.line("First Int: BE=" + be.getInt(0) + ", LE=" + le.getInt(0), UiColorPalette.textMuted());
            float beFloat = be.getFloat(0);
            float leFloat = le.getFloat(0);
            if (isReasonableFloat(beFloat) || isReasonableFloat(leFloat)) {
               builder.line("First Float: BE=" + formatFloat(beFloat) + ", LE=" + formatFloat(leFloat), UiColorPalette.textMuted());
            }
         }

         if (raw.length >= 8) {
            ByteBuffer be = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer le = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            builder.line("First Long: BE=" + be.getLong(0) + ", LE=" + le.getLong(0), UiColorPalette.textMuted());
            double beDouble = be.getDouble(0);
            double leDouble = le.getDouble(0);
            if (isReasonableDouble(beDouble) || isReasonableDouble(leDouble)) {
               builder.line("First Double: BE=" + formatDouble(beDouble) + ", LE=" + formatDouble(leDouble), UiColorPalette.textMuted());
            }
         }

         if (raw.length >= 16) {
            ByteBuffer uuidBuffer = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN);
            UUID uuid = new UUID(uuidBuffer.getLong(0), uuidBuffer.getLong(8));
            builder.line("First 16 Bytes As UUID: " + uuid, UiColorPalette.textMuted());
         }
      }
   }

   private static List<String> varIntPreview(byte[] raw, int max) {
      if (raw != null && raw.length != 0) {
         List<String> values = new ArrayList<>();
         int index = 0;

         while (index < raw.length && values.size() < Math.max(1, max) && index < 20) {
            PacketInspectorEngine.VarIntRead read = readVarInt(raw, index);
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

   private static void appendCompression(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
      PacketInspectorEngine.CompressionKind kind = detectCompression(raw);
      if (kind == PacketInspectorEngine.CompressionKind.GZIP) {
         builder.line("Compression Hint: gzip stream header", UiColorPalette.warningYellow());
      } else if (kind == PacketInspectorEngine.CompressionKind.ZLIB) {
         builder.line("Compression Hint: zlib/deflate stream header", UiColorPalette.warningYellow());
      }
   }

   private static void appendByteStats(byte[] raw, PacketInspectorEngine.ReportBuilder builder) {
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

         builder.line(
            "Binary Stats: printable="
               + percent(printable, raw.length)
               + ", zero="
               + percent(zero, raw.length)
               + ", high-bit="
               + percent(high, raw.length)
               + ", entropy="
               + formatDouble(entropy(histogram, raw.length))
               + " bits/B",
            UiColorPalette.textMuted()
         );
      }
   }

   private static double entropy(int[] histogram, int total) {
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

   private static String percent(int count, int total) {
      return total <= 0 ? "0%" : Math.round(count * 1000.0 / total) / 10.0 + "%";
   }

   private static String formatFloat(float value) {
      return !Float.isFinite(value) ? String.valueOf(value) : String.format(Locale.ROOT, "%.4f", value).replaceAll("\\.?0+$", "");
   }

   private static String formatDouble(double value) {
      return !Double.isFinite(value) ? String.valueOf(value) : String.format(Locale.ROOT, "%.4f", value).replaceAll("\\.?0+$", "");
   }

   private static boolean isReasonableFloat(float value) {
      if (!Float.isFinite(value)) {
         return false;
      }

      float abs = Math.abs(value);
      return abs == 0.0F || abs >= 1.0E-6F && abs <= 1000000.0F;
   }

   private static boolean isReasonableDouble(double value) {
      if (!Double.isFinite(value)) {
         return false;
      }

      double abs = Math.abs(value);
      return abs == 0.0 || abs >= 1.0E-6 && abs <= 1000000.0;
   }

   private static boolean isMostlyPrintableText(String value) {
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

   private static String pluralSuffix(int count) {
      return count == 1 ? "" : "s";
   }

   private static void describeJson(JsonElement json, PacketInspectorEngine.ReportBuilder builder) {
      if (json != null) {
         if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            builder.line("Decoded As: JSON object (" + object.size() + " keys)", UiColorPalette.successText());

            for (Entry<String, JsonElement> entry : object.entrySet()) {
               builder.line("  " + entry.getKey() + ": " + jsonSummary(entry.getValue()), UiColorPalette.textBright());
            }
         } else if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            builder.line("Decoded As: JSON array (" + array.size() + " items)", UiColorPalette.successText());
            int index = 0;

            for (JsonElement element : array) {
               if (index >= 16) {
                  builder.line("  ... +" + (array.size() - index) + " more", UiColorPalette.textMuted());
                  break;
               }

               builder.line("  [" + index + "]: " + jsonSummary(element), UiColorPalette.textBright());
               index++;
            }
         } else {
            builder.line("Decoded As: JSON value", UiColorPalette.successText());
            builder.line("Value: " + jsonSummary(json), UiColorPalette.textBright());
         }
      }
   }

   private static String jsonSummary(JsonElement element) {
      if (element == null || element.isJsonNull()) {
         return "null";
      } else if (element.isJsonPrimitive()) {
         JsonPrimitive primitive = element.getAsJsonPrimitive();
         return primitive.isString() ? quote(sanitize(primitive.getAsString())) : sanitize(primitive.toString());
      } else if (element.isJsonArray()) {
         return "array[" + element.getAsJsonArray().size() + "] " + sanitize(element.toString());
      } else {
         return element.isJsonObject() ? "object{" + element.getAsJsonObject().size() + "} " + sanitize(element.toString()) : sanitize(element.toString());
      }
   }

   private static JsonElement parseJsonPayload(byte[] raw) {
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

   private static List<String> identifierPreview(byte[] raw, int max) {
      String text = new String(raw == null ? new byte[0] : raw, StandardCharsets.UTF_8);
      Matcher matcher = CHANNEL_ID_PATTERN.matcher(text);
      LinkedHashSet<String> tokens = new LinkedHashSet<>();

      while (matcher.find() && tokens.size() < Math.max(1, max)) {
         tokens.add(matcher.group().toLowerCase(Locale.ROOT));
      }

      return new ArrayList<>(tokens);
   }

   private static int countChannelIds(byte[] raw) {
      String text = new String(raw == null ? new byte[0] : raw, StandardCharsets.UTF_8);
      Matcher matcher = CHANNEL_ID_PATTERN.matcher(text);
      LinkedHashSet<String> tokens = new LinkedHashSet<>();

      while (matcher.find()) {
         tokens.add(matcher.group().toLowerCase(Locale.ROOT));
      }

      return tokens.size();
   }

   private static List<String> stringPreview(byte[] raw, int max) {
      String text = new String(raw == null ? new byte[0] : raw, StandardCharsets.ISO_8859_1);
      Matcher matcher = PRINTABLE_RUN_PATTERN.matcher(text);
      List<String> runs = new ArrayList<>();

      while (matcher.find() && runs.size() < Math.max(1, max)) {
         String run = matcher.group().strip();
         if (run.length() >= 4) {
            runs.add(collapseControlChars(run));
         }
      }

      return runs;
   }

   private static String collapseControlChars(String text) {
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

   private static boolean isRegisterChannel(String channel) {
      String normalized = normalizeChannel(channel);
      return normalized.equals("minecraft:register")
         || normalized.equals("register")
         || normalized.equals("minecraft:unregister")
         || normalized.equals("unregister");
   }

   private static boolean isUnregisterChannel(String channel) {
      String normalized = normalizeChannel(channel);
      return normalized.endsWith("unregister");
   }

   private static String normalizeChannel(String channel) {
      return channel == null ? "" : channel.strip().toLowerCase(Locale.ROOT);
   }

   private static String interpretationFor(String channel, boolean complete) {
      String normalized = normalizeChannel(channel);
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

   private static String describeChannelSource(String channel) {
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

   private static String orFallback(String value, String fallback) {
      return value != null && !value.isBlank() ? value : fallback;
   }

   private static PacketInspectorEngine.PacketSummaryFlags describeBroadKnownPacket(Packet<?> packet, PacketInspectorEngine.ReportBuilder builder) {
      if (packet == null) {
         return PacketInspectorEngine.PacketSummaryFlags.NONE;
      }

      String simpleName = packet.getClass().getSimpleName();
      String className = packet.getClass().getName().replace('$', '.');
      boolean wrote = false;
      boolean complete = true;
      switch (simpleName) {
         case "ClientboundBlockUpdatePacket":
            wrote |= appendMeaning(builder, "Server changed one block");
            wrote |= appendField(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= appendField(builder, packet, "Block", "getBlockState", "blockState", "state");
            break;
         case "ClientboundBlockEntityDataPacket":
            wrote |= appendMeaning(builder, "Server updated block entity data");
            wrote |= appendField(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= appendField(builder, packet, "Block Entity Type", "getType", "type");
            wrote |= appendField(builder, packet, "Nbt", "getTag", "tag");
            break;
         case "ClientboundBlockEventPacket":
            wrote |= appendMeaning(builder, "Server fired a block event");
            wrote |= appendField(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= appendField(builder, packet, "Block", "getBlock", "block");
            wrote |= appendField(builder, packet, "Event Type", "getB0", "b0", "eventType");
            wrote |= appendField(builder, packet, "Event Data", "getB1", "b1", "eventData");
            break;
         case "ClientboundSectionBlocksUpdatePacket":
            wrote |= appendMeaning(builder, "Server changed multiple blocks in a chunk section");
            wrote |= appendField(builder, packet, "Section", "sectionPos", "getSectionPos");
            Object positions = firstPresent(firstGetterValue(packet, "positions", "getPositions"), propertyByLabel(packet, "Positions"));
            Object states = firstPresent(firstGetterValue(packet, "states", "getStates"), propertyByLabel(packet, "States"));
            if (positions != null) {
               wrote |= appendPlainValue(builder, "Positions", positions);
            }

            if (states != null) {
               wrote |= appendPlainValue(builder, "States", states);
            }

            complete = false;
            break;
         case "ClientboundLevelEventPacket":
            wrote |= appendMeaning(builder, "World event such as particles, sound, or block effect");
            wrote |= appendField(builder, packet, "Event", "getType", "type");
            wrote |= appendField(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= appendField(builder, packet, "Data", "getData", "data");
            wrote |= appendField(builder, packet, "Global", "isGlobalEvent", "globalEvent");
            break;
         case "ClientboundLevelParticlesPacket":
            wrote |= appendMeaning(builder, "Particle spawn");
            wrote |= appendField(builder, packet, "Particle", "getParticle", "getParticleType", "particle");
            wrote |= appendVecField(builder, packet, "Pos", "getX", "x", "getY", "y", "getZ", "z");
            wrote |= appendVecField(builder, packet, "Offset", "getXDist", "xDist", "getYDist", "yDist", "getZDist", "zDist");
            wrote |= appendField(builder, packet, "Speed", "getMaxSpeed", "maxSpeed");
            wrote |= appendField(builder, packet, "Count", "getCount", "count");
            wrote |= appendField(builder, packet, "Override Limiter", "isOverrideLimiter", "overrideLimiter");
            break;
         case "ClientboundForgetLevelChunkPacket":
            wrote |= appendMeaning(builder, "Client should unload a chunk");
            wrote |= appendField(builder, packet, "Chunk", "pos", "getPos");
            wrote |= appendField(builder, packet, "Chunk X", "getX", "x");
            wrote |= appendField(builder, packet, "Chunk Z", "getZ", "z");
            break;
         case "ClientboundSetChunkCacheCenterPacket":
            wrote |= appendMeaning(builder, "Server changed chunk cache center");
            wrote |= appendField(builder, packet, "Chunk X", "getX", "x");
            wrote |= appendField(builder, packet, "Chunk Z", "getZ", "z");
            break;
         case "ClientboundSetChunkCacheRadiusPacket":
         case "ClientboundSetSimulationDistancePacket":
            wrote |= appendMeaning(
               builder, simpleName.contains("Simulation") ? "Server changed simulation distance" : "Server changed chunk render distance"
            );
            wrote |= appendField(builder, packet, "Radius", "getRadius", "radius");
            wrote |= appendField(builder, packet, "Distance", "simulationDistance", "distance");
            break;
         case "ClientboundMoveEntityPacket.Pos":
         case "ClientboundMoveEntityPacket.Rot":
         case "ClientboundMoveEntityPacket.PosRot":
         case "ClientboundTeleportEntityPacket":
         case "ClientboundRotateHeadPacket":
         case "ClientboundHurtAnimationPacket":
            wrote |= appendMeaning(builder, "Server moved or rotated an entity");
            wrote |= appendEntityField(builder, packet, "Entity Id", "getEntityId", "entityId", "id");
            wrote |= appendField(builder, packet, "Delta X", "getXa", "xa");
            wrote |= appendField(builder, packet, "Delta Y", "getYa", "ya");
            wrote |= appendField(builder, packet, "Delta Z", "getZa", "za");
            wrote |= appendField(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
            wrote |= appendField(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
            wrote |= appendField(builder, packet, "Head Yaw", "getYHeadRot", "yHeadRot");
            wrote |= appendField(builder, packet, "Change", "change");
            wrote |= appendField(builder, packet, "Relatives", "relatives");
            wrote |= appendField(builder, packet, "On Ground", "isOnGround", "onGround");
            break;
         case "ClientboundMoveVehiclePacket":
         case "ServerboundMoveVehiclePacket":
            wrote |= appendMeaning(
               builder, simpleName.startsWith("Serverbound") ? "Client moved the ridden vehicle" : "Server moved the ridden vehicle"
            );
            wrote |= appendVecField(builder, packet, "Pos", "getX", "x", "getY", "y", "getZ", "z");
            wrote |= appendField(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
            wrote |= appendField(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
            wrote |= appendField(builder, packet, "On Ground", "isOnGround", "onGround");
            break;
         case "ClientboundRemoveEntitiesPacket":
            wrote |= appendMeaning(builder, "Server removed entities");
            Object ids = firstPresent(firstGetterValue(packet, "getEntityIds", "entityIds"), recordComponentAt(packet, 0));
            if (ids != null) {
               builder.line("Entities: " + entityIdList(ids), UiColorPalette.cyan());
               wrote = true;
            }
            break;
         case "ClientboundTakeItemEntityPacket":
            wrote |= appendMeaning(builder, "Item/entity pickup animation");
            wrote |= appendEntityField(builder, packet, "Item Entity", "getItemId", "itemId");
            wrote |= appendEntityField(builder, packet, "Collector", "getPlayerId", "playerId");
            wrote |= appendField(builder, packet, "Amount", "getAmount", "amount");
            break;
         case "ClientboundSetPassengersPacket":
            wrote |= appendMeaning(builder, "Server changed vehicle passengers");
            wrote |= appendEntityField(builder, packet, "Vehicle", "getVehicle", "vehicle", "vehicleId");
            Object passengers = firstPresent(firstGetterValue(packet, "getPassengers", "passengers"), propertyByLabel(packet, "Passengers"));
            if (passengers != null) {
               builder.line("Passengers: " + entityIdList(passengers), UiColorPalette.cyan());
               wrote = true;
            }
            break;
         case "ClientboundSetEntityLinkPacket":
            wrote |= appendMeaning(builder, "Server linked one entity to another");
            wrote |= appendEntityField(builder, packet, "Source", "getSourceId", "sourceId", "source");
            wrote |= appendEntityField(builder, packet, "Destination", "getDestId", "destId", "destination");
            break;
         case "ClientboundSetHealthPacket":
            wrote |= appendMeaning(builder, "Player health update");
            wrote |= appendField(builder, packet, "Health", "getHealth", "health");
            wrote |= appendField(builder, packet, "Food", "getFood", "food");
            wrote |= appendField(builder, packet, "Saturation", "getSaturation", "saturation");
            break;
         case "ClientboundSetExperiencePacket":
            wrote |= appendMeaning(builder, "Player experience update");
            wrote |= appendField(builder, packet, "Progress", "getExperienceProgress", "experienceProgress", "progress");
            wrote |= appendField(builder, packet, "Level", "getExperienceLevel", "experienceLevel", "level");
            wrote |= appendField(builder, packet, "Total", "getTotalExperience", "totalExperience", "total");
            break;
         case "ClientboundSetHeldSlotPacket":
         case "ServerboundSetCarriedItemPacket":
            wrote |= appendMeaning(
               builder, simpleName.startsWith("Serverbound") ? "Client selected hotbar slot" : "Server selected held hotbar slot"
            );
            Object slot = firstPresent(firstGetterValue(packet, "getSlot", "slot", "getSelectedSlot", "selectedSlot"), recordComponentAt(packet, 0));
            if (slot instanceof Number number) {
               builder.line("Slot: " + describeHotbarSlot(number.intValue()), UiColorPalette.textBright());
               wrote = true;
            } else if (slot != null) {
               builder.line("Slot: " + describeValue(slot), UiColorPalette.textBright());
               wrote = true;
            }
            break;
         case "ClientboundSetPlayerInventoryPacket":
         case "ClientboundSetCursorItemPacket":
            wrote |= appendMeaning(builder, simpleName.contains("Cursor") ? "Server updated cursor item" : "Server updated player inventory slot");
            wrote |= appendField(builder, packet, "Slot", "slot", "getSlot");
            wrote |= appendField(builder, packet, "Stack", "contents", "getContents", "item", "getItem");
            break;
         case "ClientboundContainerClosePacket":
            wrote |= appendMeaning(builder, "Server closed a handled screen");
            wrote |= appendField(builder, packet, "Sync Id", "getContainerId", "containerId");
            break;
         case "ClientboundCooldownPacket":
            wrote |= appendMeaning(builder, "Item cooldown update");
            wrote |= appendField(builder, packet, "Item", "getItem", "item");
            wrote |= appendField(builder, packet, "Ticks", "getDuration", "duration", "ticks");
            break;
         case "ClientboundOpenBookPacket":
            wrote |= appendMeaning(builder, "Open book animation/screen");
            wrote |= appendField(builder, packet, "Hand", "getHand", "hand");
            break;
         case "ClientboundOpenSignEditorPacket":
            wrote |= appendMeaning(builder, "Open sign editor");
            wrote |= appendField(builder, packet, "Block Pos", "getPos", "pos");
            wrote |= appendField(builder, packet, "Front", "isFrontText", "frontText", "front");
            break;
         case "ClientboundSetActionBarTextPacket":
         case "ClientboundSetTitleTextPacket":
         case "ClientboundSetSubtitleTextPacket":
            wrote |= appendMeaning(builder, "Server displayed text on the HUD");
            wrote |= appendTextField(builder, packet, "Text", "getText", "text");
            break;
         case "ClientboundSetTitlesAnimationPacket":
            wrote |= appendMeaning(builder, "Title timing update");
            wrote |= appendField(builder, packet, "Fade In", "getFadeIn", "fadeIn");
            wrote |= appendField(builder, packet, "Stay", "getStay", "stay");
            wrote |= appendField(builder, packet, "Fade Out", "getFadeOut", "fadeOut");
            break;
         case "ClientboundClearTitlesPacket":
            wrote |= appendMeaning(builder, "Clear title/subtitle text");
            wrote |= appendField(builder, packet, "Reset Times", "shouldResetTimes", "resetTimes");
            break;
         case "ClientboundTabListPacket":
            wrote |= appendMeaning(builder, "Player list header/footer update");
            wrote |= appendTextField(builder, packet, "Header", "getHeader", "header");
            wrote |= appendTextField(builder, packet, "Footer", "getFooter", "footer");
            break;
         case "ClientboundSetTimePacket":
            wrote |= appendMeaning(builder, "World time update");
            wrote |= appendField(builder, packet, "Game Time", "getGameTime", "gameTime");
            wrote |= appendField(builder, packet, "Day Time", "getDayTime", "dayTime");
            wrote |= appendField(builder, packet, "Tick Day Time", "isTickDayTime", "tickDayTime");
            break;
         case "ClientboundPlayerAbilitiesPacket":
         case "ServerboundPlayerAbilitiesPacket":
            wrote |= appendMeaning(builder, "Player ability flags");
            wrote |= appendField(builder, packet, "Invulnerable", "isInvulnerable", "invulnerable");
            wrote |= appendField(builder, packet, "Flying", "isFlying", "flying");
            wrote |= appendField(builder, packet, "Can Fly", "canFly", "mayfly", "mayFly");
            wrote |= appendField(builder, packet, "Instabuild", "canInstabuild", "instabuild");
            wrote |= appendField(builder, packet, "Fly Speed", "getFlyingSpeed", "flyingSpeed");
            wrote |= appendField(builder, packet, "Walk Speed", "getWalkingSpeed", "walkingSpeed");
            break;
         case "ClientboundChangeDifficultyPacket":
         case "ServerboundChangeDifficultyPacket":
         case "ServerboundLockDifficultyPacket":
            wrote |= appendMeaning(builder, "Difficulty setting update");
            wrote |= appendField(builder, packet, "Difficulty", "getDifficulty", "difficulty");
            wrote |= appendField(builder, packet, "Locked", "isLocked", "locked");
            break;
         case "ClientboundSoundEntityPacket":
            wrote |= appendMeaning(builder, "Sound played from an entity");
            wrote |= appendField(builder, packet, "Sound", "getSound", "sound");
            wrote |= appendField(builder, packet, "Category", "getSource", "source", "category");
            wrote |= appendEntityField(builder, packet, "Entity", "getId", "id", "entityId");
            wrote |= appendField(builder, packet, "Volume", "getVolume", "volume");
            wrote |= appendField(builder, packet, "Pitch", "getPitch", "pitch");
            wrote |= appendField(builder, packet, "Seed", "getSeed", "seed");
            break;
         case "ClientboundStopSoundPacket":
            wrote |= appendMeaning(builder, "Stop one or more sounds");
            wrote |= appendField(builder, packet, "Source", "getSource", "source");
            wrote |= appendField(builder, packet, "Name", "getName", "name");
            break;
         case "ClientboundBossEventPacket":
            wrote |= appendMeaning(builder, "Boss bar update");
            wrote |= appendField(builder, packet, "Boss Bar Id", "getId", "id");
            wrote |= appendField(builder, packet, "Operation", "getOperation", "operation");
            complete = false;
            break;
         case "ClientboundPlayerInfoUpdatePacket":
            wrote |= appendMeaning(builder, "Player list entries updated");
            wrote |= appendField(builder, packet, "Actions", "actions");
            wrote |= appendCollectionField(builder, packet, "Entries", "entries");
            complete = false;
            break;
         case "ClientboundPlayerInfoRemovePacket":
            wrote |= appendMeaning(builder, "Player list entries removed");
            wrote |= appendCollectionField(builder, packet, "Profiles", "profileIds", "getProfileIds");
            break;
         case "ClientboundPlayerCombatEndPacket":
         case "ClientboundPlayerCombatEnterPacket":
         case "ClientboundPlayerCombatKillPacket":
            wrote |= appendMeaning(builder, "Player combat state update");
            wrote |= appendEntityField(builder, packet, "Player", "playerId", "getPlayerId");
            wrote |= appendEntityField(builder, packet, "Killer", "killerId", "getKillerId");
            wrote |= appendField(builder, packet, "Duration", "duration", "getDuration");
            wrote |= appendTextField(builder, packet, "Message", "message", "getMessage");
            break;
         case "ClientboundSetScorePacket":
         case "ClientboundResetScorePacket":
            wrote |= appendMeaning(builder, "Scoreboard score update");
            wrote |= appendField(builder, packet, "Owner", "owner", "getOwner");
            wrote |= appendField(builder, packet, "Objective", "objectiveName", "getObjectiveName");
            wrote |= appendField(builder, packet, "Score", "score", "getScore");
            wrote |= appendField(builder, packet, "Display", "display", "numberFormat", "getDisplay");
            break;
         case "ClientboundSetObjectivePacket":
         case "ClientboundSetDisplayObjectivePacket":
            wrote |= appendMeaning(builder, "Scoreboard objective update");
            wrote |= appendField(builder, packet, "Objective", "getObjectiveName", "objectiveName");
            wrote |= appendTextField(builder, packet, "Display Name", "getDisplayName", "displayName");
            wrote |= appendField(builder, packet, "Render Type", "getRenderType", "renderType");
            wrote |= appendField(builder, packet, "Slot", "getSlot", "slot");
            wrote |= appendField(builder, packet, "Method", "getMethod", "method");
            break;
         case "ClientboundSetPlayerTeamPacket":
            wrote |= appendMeaning(builder, "Scoreboard team update");
            wrote |= appendField(builder, packet, "Team", "getName", "name");
            wrote |= appendField(builder, packet, "Method", "getMethod", "method");
            wrote |= appendCollectionField(builder, packet, "Players", "getPlayers", "players");
            complete = false;
            break;
         case "ClientboundSetBorderCenterPacket":
         case "ClientboundSetBorderSizePacket":
         case "ClientboundSetBorderLerpSizePacket":
         case "ClientboundSetBorderWarningDelayPacket":
         case "ClientboundSetBorderWarningDistancePacket":
         case "ClientboundInitializeBorderPacket":
            wrote |= appendMeaning(builder, "World border update");
            wrote |= appendField(builder, packet, "Center X", "getNewCenterX", "newCenterX", "centerX");
            wrote |= appendField(builder, packet, "Center Z", "getNewCenterZ", "newCenterZ", "centerZ");
            wrote |= appendField(builder, packet, "Size", "getSize", "size");
            wrote |= appendField(builder, packet, "Old Size", "getOldSize", "oldSize");
            wrote |= appendField(builder, packet, "New Size", "getNewSize", "newSize");
            wrote |= appendField(builder, packet, "Lerp Time", "getLerpTime", "lerpTime");
            wrote |= appendField(builder, packet, "Warning Blocks", "getWarningBlocks", "warningBlocks");
            wrote |= appendField(builder, packet, "Warning Time", "getWarningTime", "warningTime");
            break;
         case "ClientboundResourcePackPushPacket":
         case "ClientboundResourcePackPopPacket":
            wrote |= appendMeaning(builder, "Resource pack request/update");
            wrote |= appendField(builder, packet, "Id", "id", "getId");
            wrote |= appendField(builder, packet, "Url", "url", "getUrl");
            wrote |= appendField(builder, packet, "Hash", "hash", "getHash");
            wrote |= appendField(builder, packet, "Required", "required", "isRequired");
            wrote |= appendTextField(builder, packet, "Prompt", "prompt", "getPrompt");
            break;
         case "ClientboundUpdateRecipesPacket":
         case "ClientboundRecipeBookAddPacket":
         case "ClientboundRecipeBookRemovePacket":
         case "ClientboundRecipeBookSettingsPacket":
            wrote |= appendMeaning(builder, "Recipe book/recipe registry update");
            wrote |= appendCollectionField(builder, packet, "Recipes", "recipes", "getRecipes", "entries");
            wrote |= appendField(builder, packet, "Settings", "settings", "getSettings");
            complete = false;
            break;
         case "ClientboundUpdateTagsPacket":
            wrote |= appendMeaning(builder, "Registry tag update");
            wrote |= appendField(builder, packet, "Tags", "tags", "getTags");
            complete = false;
            break;
         case "ServerboundPlayerCommandPacket":
            wrote |= appendMeaning(builder, "Client sent player/entity command");
            wrote |= appendEntityField(builder, packet, "Entity", "getId", "id", "entityId");
            wrote |= appendField(builder, packet, "Action", "getAction", "action");
            wrote |= appendValue(builder, "Data", firstPresent(firstGetterValue(packet, "getData", "data"), recordComponentAt(packet, 2)));
            break;
         case "ServerboundPlayerInputPacket":
            wrote |= appendMeaning(builder, "Client movement input state");
            wrote |= appendField(builder, packet, "Input", "input");
            wrote |= appendField(builder, packet, "Forward", "xxa", "forward");
            wrote |= appendField(builder, packet, "Sideways", "zza", "sideways");
            wrote |= appendField(builder, packet, "Jumping", "isJumping", "jumping");
            wrote |= appendField(builder, packet, "Sneaking", "isShiftKeyDown", "shiftKeyDown", "sneaking");
            break;
         case "ServerboundPaddleBoatPacket":
            wrote |= appendMeaning(builder, "Client boat paddle state");
            wrote |= appendField(builder, packet, "Left", "getLeft", "left");
            wrote |= appendField(builder, packet, "Right", "getRight", "right");
            break;
         case "ServerboundCommandSuggestionPacket":
            wrote |= appendMeaning(builder, "Client requested command completions");
            wrote |= appendField(builder, packet, "Transaction Id", "getId", "id");
            wrote |= appendField(builder, packet, "Command", "getCommand", "command");
            break;
         case "ServerboundChatCommandPacket":
         case "ServerboundChatCommandSignedPacket":
            wrote |= appendMeaning(builder, "Outgoing command");
            wrote |= appendField(builder, packet, "Command", "command", "getCommand");
            break;
         case "ServerboundPickItemFromBlockPacket":
            wrote |= appendMeaning(builder, "Client pick-blocked a block");
            wrote |= appendField(builder, packet, "Block Pos", "pos", "getPos");
            wrote |= appendField(builder, packet, "Include Data", "includeData", "isIncludeData");
            break;
         case "ServerboundPickItemFromEntityPacket":
            wrote |= appendMeaning(builder, "Client pick-blocked an entity");
            wrote |= appendEntityField(builder, packet, "Entity", "id", "getId", "entityId");
            wrote |= appendField(builder, packet, "Include Data", "includeData", "isIncludeData");
            break;
         default:
            if (className.contains("ServerboundMovePlayerPacket")) {
               wrote |= appendMeaning(builder, "Client player movement");
               wrote |= appendVecField(builder, packet, "Pos", "getX", "x", "getY", "y", "getZ", "z");
               wrote |= appendField(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
               wrote |= appendField(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
               wrote |= appendField(builder, packet, "On Ground", "isOnGround", "onGround");
               wrote |= appendField(builder, packet, "Horizontal Collision", "horizontalCollision");
            } else if (className.contains("ClientboundMoveEntityPacket")) {
               wrote |= appendMeaning(builder, "Server moved or rotated an entity");
               wrote |= appendEntityField(builder, packet, "Entity Id", "getEntityId", "entityId", "id");
               wrote |= appendField(builder, packet, "Delta X", "getXa", "xa");
               wrote |= appendField(builder, packet, "Delta Y", "getYa", "ya");
               wrote |= appendField(builder, packet, "Delta Z", "getZa", "za");
               wrote |= appendField(builder, packet, "Yaw", "getYRot", "yRot", "yaw");
               wrote |= appendField(builder, packet, "Pitch", "getXRot", "xRot", "pitch");
               wrote |= appendField(builder, packet, "On Ground", "isOnGround", "onGround");
            }
      }

      return new PacketInspectorEngine.PacketSummaryFlags(wrote, complete);
   }

   private static boolean appendMeaning(PacketInspectorEngine.ReportBuilder builder, String meaning) {
      if (meaning != null && !meaning.isBlank()) {
         builder.line("Meaning: " + meaning, UiColorPalette.textBright());
         return true;
      } else {
         return false;
      }
   }

   private static boolean appendField(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = firstPresent(firstGetterValue(packet, accessors), propertyByLabel(packet, label));
      return appendValue(builder, label, value);
   }

   private static boolean appendValue(PacketInspectorEngine.ReportBuilder builder, String label, Object value) {
      if (value != null && value != MISSING) {
         builder.line(label + ": " + describeLabeledValue(label, value), labeledValueColor(label, value));
         return true;
      } else {
         return false;
      }
   }

   private static boolean appendPlainValue(PacketInspectorEngine.ReportBuilder builder, String label, Object value) {
      if (value != null && value != MISSING) {
         builder.line(label + ": " + valueText(value), labeledValueColor(label, value));
         return true;
      } else {
         return false;
      }
   }

   private static boolean appendEntityField(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = firstPresent(firstGetterValue(packet, accessors), propertyByLabel(packet, label));
      if (value instanceof Number number) {
         appendEntity(builder, label, number.intValue());
         return true;
      } else if (value instanceof Entity entity) {
         builder.line(label + ": " + describeEntity(entity), UiColorPalette.cyan());
         return true;
      } else {
         return appendValue(builder, label, value);
      }
   }

   private static boolean appendTextField(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = firstPresent(firstGetterValue(packet, accessors), propertyByLabel(packet, label));
      if (value != null && value != MISSING) {
         String text = componentText(value);
         builder.line(label + ": " + (text != null && !text.isBlank() ? quote(sanitize(text)) : describeLabeledValue(label, value)), scalarValueColor(label, value));
         return true;
      } else {
         return false;
      }
   }

   private static boolean appendCollectionField(PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String... accessors) {
      Object value = firstPresent(firstGetterValue(packet, accessors), propertyByLabel(packet, label));
      if (value != null && value != MISSING) {
         if (value instanceof Collection<?> collection) {
            builder.line(label + ": " + describeCollection(collection), UiColorPalette.textBody());
         } else {
            builder.line(label + ": " + describeLabeledValue(label, value), labeledValueColor(label, value));
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean appendVecField(
      PacketInspectorEngine.ReportBuilder builder, Packet<?> packet, String label, String xGetter, String xField, String yGetter, String yField, String zGetter, String zField
   ) {
      Object x = firstPresent(firstGetterValue(packet, xGetter, xField), propertyByLabel(packet, xField));
      Object y = firstPresent(firstGetterValue(packet, yGetter, yField), propertyByLabel(packet, yField));
      Object z = firstPresent(firstGetterValue(packet, zGetter, zField), propertyByLabel(packet, zField));
      if (x instanceof Number xn && y instanceof Number yn && z instanceof Number zn) {
         builder.line(label + ": " + formatVec(new Vec3(xn.doubleValue(), yn.doubleValue(), zn.doubleValue())), UiColorPalette.warningYellow());
         return true;
      } else {
         return false;
      }
   }

   private static PacketInspectorEngine.PacketSummaryFlags describeProperties(Packet<?> packet, PacketInspectorEngine.ReportBuilder builder) {
      List<PacketInspectorEngine.PropertyEntry> properties = properties(packet);
      if (properties.isEmpty()) {
         String rawFallback = rawToString(packet);
         if (rawFallback != null) {
            builder.line("Raw: " + rawFallback, UiColorPalette.textMuted());
            return new PacketInspectorEngine.PacketSummaryFlags(true, false);
         } else {
            return PacketInspectorEngine.PacketSummaryFlags.NONE;
         }
      } else {
         int lines = 0;

         for (PacketInspectorEngine.PropertyEntry property : properties) {
            if (lines >= 8) {
               break;
            }

            Object value = property.value();
            if (value != null && value != MISSING) {
               String summary = describeLabeledValue(property.label(), value);
               if (summary != null && !summary.isBlank()) {
                  builder.line(property.label() + ": " + summary, labeledValueColor(property.label(), value));
                  lines++;
               }
            }
         }

         return new PacketInspectorEngine.PacketSummaryFlags(lines > 0, false);
      }
   }

   private static PacketInspectorEngine.PacketSummaryFlags describeReflectivePacket(
      Packet<?> packet, String packetNameHint, AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketInspectorEngine.ReportBuilder builder
   ) {
      if (packet instanceof ServerboundPunchPacket) {
         builder.line("Interaction: Punch / Swing (attack / use animation)", UiColorPalette.textBright());
         if (MC.player != null) {
            builder.line("Held Item: " + describeStack(MC.player.getMainHandItem()), UiColorPalette.successText());
         }

         return new PacketInspectorEngine.PacketSummaryFlags(true, true);
      }

      if (packet instanceof CompatSwingPacket || isPacketNamed(packet, packetNameHint, "HandSwingC2S", "HandSwingC2SPacket")) {
         Object hand = firstPresent(firstGetterValue(packet, "getHand", "hand"), recordComponentAt(packet, 0), propertyByLabel(packet, "Hand"));
         if (hand != null) {
            builder.line("Interaction: Swing InteractionHand (attack / use animation)", UiColorPalette.textBright());
            builder.line("Hand: " + describeValue(hand), UiColorPalette.textBright());
            if (MC.player != null && hand instanceof InteractionHand playerHand) {
               builder.line("Held Item: " + describeStack(MC.player.getItemInHand(playerHand)), UiColorPalette.successText());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSetCarriedItemPacket updateSelectedSlotPacket) {
         Object slot = firstGetterValue(updateSelectedSlotPacket, "getSelectedSlot", "getSlot", "selectedSlot", "slot");
         if (slot != null) {
            if (slot instanceof Number number) {
               builder.line("Selected Slot: " + describeHotbarSlot(number.intValue()), UiColorPalette.textBright());
            } else {
               builder.line("Selected Slot: " + describeValue(slot), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundChatPacket chatMessagePacket || isPacketNamed(packet, packetNameHint, "ChatMessageC2S", "ChatMessageC2SPacket")) {
         Object message = firstPresent(firstGetterValue(packet, "chatMessage", "getChatMessage"), recordComponentAt(packet, 0));
         Object timestamp = firstPresent(firstGetterValue(packet, "timestamp", "getTimestamp"), recordComponentAt(packet, 1));
         Object salt = firstPresent(firstGetterValue(packet, "salt", "getSalt"), recordComponentAt(packet, 2));
         Object signature = firstPresent(firstGetterValue(packet, "signature", "getSignature"), recordComponentAt(packet, 3));
         Object acknowledgment = firstPresent(firstGetterValue(packet, "acknowledgment", "getAcknowledgment"), recordComponentAt(packet, 4));
         if (message != null || timestamp != null || salt != null) {
            builder.line("Kind: Outgoing Signed Chat Message", UiColorPalette.textBright());
            if (MC.player != null) {
               String senderName = MC.player.getName() == null ? null : MC.player.getName().getString();
               if (senderName != null && !senderName.isBlank()) {
                  builder.line("Sender: " + senderName, UiColorPalette.textBright());
               }
            }

            String text = componentText(message);
            if (text != null && !text.isBlank()) {
               builder.line("Message: " + quote(sanitize(text)), UiColorPalette.successText());
            }

            if (timestamp != null) {
               builder.line("Timestamp: " + describeValue(timestamp), UiColorPalette.textBody());
            }

            if (salt != null) {
               builder.line("Salt: " + describeValue(salt), UiColorPalette.textBody());
            }

            if (signature != null) {
               builder.line("Signature: Present", UiColorPalette.textBody());
            }

            if (acknowledgment != null) {
               builder.line("Acknowledgment: " + valueText(acknowledgment), plainValueColor(acknowledgment));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundBlockChangedAckPacket
         || isPacketNamed(packet, packetNameHint, "PlayerActionResponseS2C", "PlayerActionResponseS2CPacket")) {
         Object sequence = firstPresent(
            firstGetterValue(packet, "sequence", "getSequence"),
            recordComponentAt(packet, 0),
            fieldValue(packet, "comp_633"),
            propertyByLabel(packet, "Sequence")
         );
         if (sequence != null) {
            builder.line("Meaning: Server acknowledged a sequence-based player action", UiColorPalette.textBright());
            builder.line("Sequence: " + describeValue(sequence), UiColorPalette.textBright());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundContainerClosePacket
         || isPacketNamed(packet, packetNameHint, "CloseAbstractContainerScreenC2S", "ServerboundContainerClosePacket")) {
         Object syncId = firstPresent(
            firstGetterValue(packet, "getSyncId", "syncId"),
            recordComponentAt(packet, 0),
            fieldValue(packet, "field_12827"),
            propertyByLabel(packet, "Sync Id", "SyncId")
         );
         if (syncId != null) {
            builder.line("Interaction: Close Handled Screen", UiColorPalette.textBright());
            builder.line("Sync Id: " + describeValue(syncId), UiColorPalette.textBright());
            if (entry != null && entry.capturedScreen != null && !entry.capturedScreen.isBlank()) {
               builder.line("Closed Screen: " + entry.capturedScreen, UiColorPalette.successText());
            }

            if (syncId instanceof Number number && number.intValue() == 0) {
               builder.line("Likely Screen: Player Inventory / Survival Crafting", UiColorPalette.successText());
            }

            builder.line("Note: This packet only sends the sync id, not the closed screen title/type", UiColorPalette.textBody());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundSetEquipmentPacket
         || isPacketNamed(packet, packetNameHint, "EntityEquipmentUpdateS2C", "EntityEquipmentUpdateS2CPacket")) {
         Object entityIdValue = firstPresent(
            firstGetterValue(packet, "getEntityId", "entityId", "id"),
            recordComponentAt(packet, 0),
            fieldValue(packet, "field_12565"),
            propertyByLabel(packet, "Entity Id", "EntityId")
         );
         Object equipmentListValue = firstPresent(
            firstGetterValue(packet, "getEquipmentList", "equipmentList"),
            recordComponentAt(packet, 1),
            fieldValue(packet, "field_25721"),
            propertyByLabel(packet, "Equipment List", "EquipmentList")
         );
         if (entityIdValue != null || equipmentListValue != null) {
            builder.line("Meaning: Server updated an entity's visible equipment", UiColorPalette.textBright());
            if (entityIdValue instanceof Number entityIdNumber) {
               appendEntity(builder, "Entity Id", entityIdNumber.intValue());
            } else if (entityIdValue != null) {
               builder.line("Entity Id: " + describeValue(entityIdValue), UiColorPalette.textBright());
            }

            if (equipmentListValue instanceof Collection<?> equipmentEntries) {
               builder.line("Changed Slots: " + equipmentEntries.size(), UiColorPalette.textBright());
               String equipmentSummary = describeEquipment(equipmentEntries);
               if (equipmentSummary != null && !equipmentSummary.isBlank()) {
                  builder.line("Equipment: " + equipmentSummary, UiColorPalette.successText());
               }
            } else if (equipmentListValue != null) {
               builder.line("Equipment: " + valueText(equipmentListValue), plainValueColor(equipmentListValue));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundContainerButtonClickPacket buttonClickPacket) {
         Object syncId = firstGetterValue(buttonClickPacket, "getSyncId", "syncId");
         Object buttonId = firstGetterValue(buttonClickPacket, "getButtonId", "buttonId");
         if (syncId != null || buttonId != null) {
            builder.line("Interaction: Click handled-screen button", UiColorPalette.textBright());
            if (syncId != null) {
               builder.line("Sync Id: " + describeValue(syncId), UiColorPalette.textBright());
            }

            if (buttonId != null) {
               builder.line("Button Id: " + describeValue(buttonId), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeInventoryActionPacket) {
         Object slot = firstGetterValue(creativeInventoryActionPacket, "getSlot", "slot");
         Object stack = firstGetterValue(creativeInventoryActionPacket, "getStack", "stack");
         if (slot != null || stack != null) {
            if (slot != null) {
               builder.line("Slot: " + describeValue(slot), UiColorPalette.textBright());
            }

            if (stack != null) {
               builder.line("Stack: " + valueText(stack), plainValueColor(stack));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundContainerClickPacket clickSlotPacket) {
         Object syncId = firstGetterValue(clickSlotPacket, "getSyncId", "syncId");
         Object revision = firstGetterValue(clickSlotPacket, "getRevision", "revision");
         Object slot = firstGetterValue(clickSlotPacket, "getSlot", "slot");
         Object button = firstGetterValue(clickSlotPacket, "getButton", "button");
         Object actionType = firstGetterValue(clickSlotPacket, "getActionType", "actionType");
         Object modifiedStacks = firstGetterValue(clickSlotPacket, "getModifiedStacks", "modifiedStacks");
         Object cursorStack = firstGetterValue(clickSlotPacket, "cursor", "getStack", "getCursorStack", "cursorStack");
         if (syncId != null || slot != null || actionType != null) {
            builder.line("Interaction: " + describeClickType(actionType), UiColorPalette.textBright());
            if (syncId != null) {
               builder.line("Sync Id: " + describeValue(syncId), UiColorPalette.textBright());
            }

            if (revision != null) {
               builder.line("Revision: " + describeValue(revision), UiColorPalette.textBody());
            }

            if (slot != null) {
               builder.line("Slot: " + describeValue(slot), UiColorPalette.textBright());
            }

            if (button != null) {
               builder.line("Button: " + describeValue(button), UiColorPalette.textBright());
            }

            if (actionType != null) {
               builder.line("Action Type: " + describeValue(actionType), UiColorPalette.textBright());
            }

            if (cursorStack != null) {
               builder.line("Cursor: " + valueText(cursorStack), plainValueColor(cursorStack));
            }

            if (modifiedStacks instanceof Map<?, ?> map) {
               builder.line("Changed Slots: " + map.size(), UiColorPalette.textBody());
            } else if (modifiedStacks != null) {
               builder.line("Changed Slots: " + valueText(modifiedStacks), plainValueColor(modifiedStacks));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, false);
         }
      }

      if (packet instanceof ServerboundEditBookPacket bookUpdatePacket) {
         Object slot = firstGetterValue(bookUpdatePacket, "slot", "getSlot");
         Object pages = firstGetterValue(bookUpdatePacket, "pages", "getPages");
         Object title = firstGetterValue(bookUpdatePacket, "title", "getTitle");
         if (slot != null || pages != null || title != null) {
            boolean signing = title instanceof Optional<?> optional && optional.isPresent();
            builder.line("Interaction: " + (signing ? "Sign Book" : "Edit Book"), UiColorPalette.textBright());
            if (slot instanceof Number number) {
               builder.line("Slot: " + describeHotbarSlot(number.intValue()), UiColorPalette.textBright());
            } else if (slot != null) {
               builder.line("Slot: " + describeValue(slot), UiColorPalette.textBright());
            }

            if (title != null) {
               String titleComponent = componentText(title);
               if (titleComponent != null && !titleComponent.isBlank()) {
                  builder.line("Title: " + quote(sanitize(titleComponent)), UiColorPalette.successText());
               }
            }

            if (pages instanceof Collection<?> collection) {
               builder.line("Pages: " + collection.size(), UiColorPalette.textBright());
               String preview = bookPagesPreview(collection);
               if (preview != null) {
                  builder.line("Preview: " + preview, UiColorPalette.textBody());
               }
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSignUpdatePacket updateSignPacket) {
         Object pos = firstGetterValue(updateSignPacket, "getPos", "pos");
         Object lines = firstGetterValue(updateSignPacket, "getText", "text");
         Object front = firstGetterValue(updateSignPacket, "isFront", "front");
         if (pos != null || lines != null || front != null) {
            builder.line("Interaction: Update Sign Component", UiColorPalette.textBright());
            if (pos != null) {
               builder.line("Block Pos: " + valueText(pos), plainValueColor(pos));
            }

            if (front != null) {
               builder.line("Side: " + (Boolean.TRUE.equals(front) ? "Front" : "Back"), UiColorPalette.textBright());
            }

            if (lines instanceof String[] stringLines) {
               builder.line("Component: " + signLinesPreview(stringLines), UiColorPalette.successText());
            } else if (lines != null) {
               builder.line("Component: " + valueText(lines), plainValueColor(lines));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundRenameItemPacket renameItemPacket) {
         Object name = firstGetterValue(renameItemPacket, "getName", "name");
         if (name != null) {
            builder.line("Interaction: Rename Item in Anvil", UiColorPalette.textBright());
            builder.line("Name: " + quote(sanitize(String.valueOf(name))), UiColorPalette.successText());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundPlaceRecipePacket craftRequestPacket) {
         Object syncId = firstGetterValue(craftRequestPacket, "syncId", "getSyncId");
         Object recipeId = firstGetterValue(craftRequestPacket, "recipeId", "getRecipeId");
         Object craftAll = firstGetterValue(craftRequestPacket, "craftAll", "isCraftAll", "getCraftAll");
         if (syncId != null || recipeId != null || craftAll != null) {
            builder.line("Interaction: Craft Recipe", UiColorPalette.textBright());
            if (syncId != null) {
               builder.line("Sync Id: " + describeValue(syncId), UiColorPalette.textBright());
            }

            if (recipeId != null) {
               builder.line("Recipe: " + describeValue(recipeId), UiColorPalette.successText());
            }

            if (craftAll != null) {
               builder.line("Craft All: " + describeValue(craftAll), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundSelectTradePacket selectMerchantTradePacket) {
         Object tradeId = firstGetterValue(selectMerchantTradePacket, "getTradeId", "tradeId");
         if (tradeId != null) {
            builder.line("Interaction: Select Merchant Trade", UiColorPalette.textBright());
            builder.line("Trade Index: " + describeValue(tradeId), UiColorPalette.textBright());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundContainerSetDataPacket screenHandlerPropertyUpdatePacket) {
         Object syncId = firstGetterValue(screenHandlerPropertyUpdatePacket, "getSyncId", "syncId");
         Object property = firstGetterValue(screenHandlerPropertyUpdatePacket, "getPropertyId", "propertyId", "getProperty", "property");
         Object value = firstGetterValue(screenHandlerPropertyUpdatePacket, "getValue", "value");
         if (syncId != null || property != null || value != null) {
            if (syncId != null) {
               builder.line("Sync Id: " + describeValue(syncId), UiColorPalette.textBright());
            }

            if (property != null) {
               builder.line("Property: " + describeValue(property), UiColorPalette.textBright());
            }

            if (value != null) {
               builder.line("Value: " + describeValue(value), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundBlockDestructionPacket blockBreakingProgressPacket) {
         Object entityId = firstGetterValue(blockBreakingProgressPacket, "getEntityId", "entityId");
         Object pos = firstGetterValue(blockBreakingProgressPacket, "getPos", "pos");
         Object progress = firstGetterValue(blockBreakingProgressPacket, "getProgress", "progress");
         if (entityId != null || pos != null || progress != null) {
            if (entityId instanceof Number number) {
               appendEntity(builder, "Entity Id", number.intValue());
            } else if (entityId != null) {
               builder.line("Entity Id: " + describeValue(entityId), UiColorPalette.textBright());
            }

            if (pos != null) {
               builder.line("Block Pos: " + valueText(pos), plainValueColor(pos));
            }

            if (progress != null) {
               builder.line("Progress: " + describeValue(progress), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundEntityEventPacket entityStatusPacket) {
         Object entityId = firstGetterValue(entityStatusPacket, "getEntityId", "entityId");
         if (entityId == null && MC.level != null) {
            Entity entity = entityStatusPacket.getEntity(MC.level);
            if (entity != null) {
               entityId = entity.getId();
            }
         }

         Object status = firstGetterValue(entityStatusPacket, "getStatus", "status");
         if (status == null) {
            status = firstGetterValue(entityStatusPacket, "getEventId", "eventId");
         }

         if (entityId != null || status != null) {
            if (entityId instanceof Number number) {
               appendEntity(builder, "Entity Id", number.intValue());
            } else if (entityId != null) {
               builder.line("Entity Id: " + describeValue(entityId), UiColorPalette.textBright());
            }

            if (status != null) {
               builder.line("Status: " + describeValue(status), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundSetEntityMotionPacket entityVelocityUpdatePacket) {
         Object entityId = firstGetterValue(entityVelocityUpdatePacket, "getEntityId", "entityId", "id");
         Object velocity = firstGetterValue(entityVelocityUpdatePacket, "getVelocity", "velocity");
         if (velocity == null) {
            velocity = firstGetterValue(entityVelocityUpdatePacket, "movement");
         }

         if (entityId != null || velocity != null) {
            if (entityId instanceof Number number) {
               appendEntity(builder, "Entity Id", number.intValue());
            } else if (entityId != null) {
               builder.line("Entity Id: " + describeValue(entityId), UiColorPalette.textBright());
            }

            if (velocity != null) {
               builder.line("Velocity: " + valueText(velocity), plainValueColor(velocity));
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundSystemChatPacket gameMessagePacket) {
         Object content = firstGetterValue(gameMessagePacket, "content", "getContent");
         Object overlay = firstGetterValue(gameMessagePacket, "overlay", "isOverlay", "getOverlay");
         if (content != null || overlay != null) {
            String message = componentText(content);
            builder.line("Kind: " + (Boolean.TRUE.equals(overlay) ? "Overlay / HUD message" : "Game / system message"), UiColorPalette.textBright());
            PacketInspectorEngine.ParsedMessagePair parsed = parseChatLine(message);
            if (parsed != null) {
               builder.line("Possible Sender: " + parsed.init(), UiColorPalette.textBright());
               builder.line("Message: " + quote(sanitize(parsed.load())), UiColorPalette.successText());
            } else if (message != null && !message.isBlank()) {
               builder.line("Message: " + quote(sanitize(message)), UiColorPalette.successText());
            } else if (content != null) {
               builder.line("Message: " + valueText(content), plainValueColor(content));
            }

            if (overlay != null) {
               builder.line("Overlay: " + describeValue(overlay), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundDisguisedChatPacket profilelessChatMessagePacket) {
         Object message = firstGetterValue(profilelessChatMessagePacket, "message", "getMessage");
         Object chatType = firstGetterValue(profilelessChatMessagePacket, "chatType", "getChatType");
         PacketInspectorEngine.DecodeCheckPair chatSummary = appendChatSummary(message, chatType, null, null, null, "Incoming Chat Message", builder);
         if (chatSummary.init()) {
            return new PacketInspectorEngine.PacketSummaryFlags(true, chatSummary.load());
         }
      }

      if (packet instanceof ClientboundPlayerChatPacket chatMessageS2CPacket) {
         Object body = firstPresent(
            firstGetterValue(chatMessageS2CPacket, "body", "getBody"),
            recordComponentAt(chatMessageS2CPacket, 4),
            recordComponentByNames(chatMessageS2CPacket, 2, "content", "timestamp", "salt")
         );
         Object serializedParameters = firstPresent(
            firstGetterValue(chatMessageS2CPacket, "serializedParameters", "getSerializedParameters"),
            recordComponentAt(chatMessageS2CPacket, 7),
            recordComponentByNames(chatMessageS2CPacket, 2, "name", "targetName", "type")
         );
         Object sender = firstPresent(
            firstGetterValue(chatMessageS2CPacket, "sender", "getSender"),
            recordComponentAt(chatMessageS2CPacket, 1),
            recordComponentOfType(chatMessageS2CPacket, UUID.class)
         );
         Object filterMask = firstPresent(
            firstGetterValue(chatMessageS2CPacket, "filterMask", "getFilterMask"),
            recordComponentAt(chatMessageS2CPacket, 6),
            recordComponentByNames(chatMessageS2CPacket, 1, "isFullyFiltered", "isPassThrough")
         );
         Object unsignedContent = firstPresent(
            firstGetterValue(chatMessageS2CPacket, "unsignedContent", "getUnsignedContent"),
            recordComponentAt(chatMessageS2CPacket, 5),
            firstOtherRecordComponent(chatMessageS2CPacket, body, serializedParameters, sender, filterMask)
         );
         Object message = unsignedContent;
         Object timestamp = null;
         if (body != null) {
            Object bodyContent = firstPresent(firstGetterValue(body, "content", "getContent"), recordComponentAt(body, 0), firstOtherRecordComponent(body));
            if (bodyContent != null && (message == null || componentText(message) == null || componentText(message).isBlank())) {
               message = bodyContent;
            }

            timestamp = firstPresent(
               firstGetterValue(body, "timestamp", "getTimestamp"), recordComponentAt(body, 1), recordComponentOfType(body, Instant.class)
            );
         }

         PacketInspectorEngine.DecodeCheckPair chatSummary = appendChatSummary(
            message, serializedParameters, sender instanceof UUID uuid ? uuid : null, timestamp, filterMask, "Incoming Signed Chat Message", builder
         );
         if (chatSummary.init()) {
            return new PacketInspectorEngine.PacketSummaryFlags(true, chatSummary.load());
         }
      }

      if (packet instanceof ClientboundSetActionBarTextPacket overlayMessagePacket) {
         Object text = firstGetterValue(overlayMessagePacket, "text", "getText");
         String message = componentText(text);
         if (message != null || text != null) {
            builder.line("Kind: Overlay / HUD message", UiColorPalette.textBright());
            builder.line("Message: " + (message == null ? valueText(text) : quote(sanitize(message))), message == null ? plainValueColor(text) : UiColorPalette.successText());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundOpenBookPacket openWrittenBookPacket) {
         Object hand = firstGetterValue(openWrittenBookPacket, "getHand", "hand");
         if (hand != null) {
            builder.line("Interaction: Open Written Book", UiColorPalette.textBright());
            builder.line("Hand: " + describeValue(hand), UiColorPalette.textBright());
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ClientboundDisconnectPacket disconnectPacket) {
         Object reason = firstGetterValue(disconnectPacket, "reason", "getReason");
         if (reason != null) {
            builder.line("Reason: " + valueText(reason), plainValueColor(reason));
            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      if (packet instanceof ServerboundMovePlayerPacket playerMovePacket) {
         Object x = firstGetterValue(playerMovePacket, "getX", "x");
         Object y = firstGetterValue(playerMovePacket, "getY", "y");
         Object z = firstGetterValue(playerMovePacket, "getZ", "z");
         Object yaw = firstGetterValue(playerMovePacket, "getYaw", "yaw");
         Object pitch = firstGetterValue(playerMovePacket, "getPitch", "pitch");
         Object onGround = firstGetterValue(playerMovePacket, "isOnGround", "onGround");
         if (x != null || y != null || z != null || yaw != null || pitch != null || onGround != null) {
            if (x != null || y != null || z != null) {
               builder.line("Pos: x=" + describeValue(x) + ", y=" + describeValue(y) + ", z=" + describeValue(z), UiColorPalette.textBright());
            }

            if (yaw != null || pitch != null) {
               builder.line("Rot: yaw=" + describeValue(yaw) + ", pitch=" + describeValue(pitch), UiColorPalette.textBright());
            }

            if (onGround != null) {
               builder.line("On Ground: " + describeValue(onGround), UiColorPalette.textBright());
            }

            return new PacketInspectorEngine.PacketSummaryFlags(true, true);
         }
      }

      return PacketInspectorEngine.PacketSummaryFlags.NONE;
   }

   private static void dumpObject(Object root, PacketInspectorEngine.ReportBuilder builder, IdentityHashMap<Object, Boolean> visited) {
      visited.put(root, Boolean.TRUE);
      List<PacketInspectorEngine.PropertyEntry> properties = properties(root);
      List<Field> fields = instanceFields(root.getClass());
      Set<String> consumedFieldNames = new LinkedHashSet<>();

      for (PacketInspectorEngine.PropertyEntry property : properties) {
         dumpProperty(property.label(), property.value(), 0, builder, visited);
         consumedFieldNames.add(decapitalize(normalizeLabel(property.label())));
      }

      boolean wroteAny = !properties.isEmpty();

      for (Field field : fields) {
         String fieldLabel = fieldLabel(field);
         if (fieldLabel != null && !fieldLabel.isBlank() && !consumedFieldNames.contains(decapitalize(normalizeLabel(fieldLabel)))) {
            dumpProperty(fieldLabel, readFieldQuietly(field, root), 0, builder, visited);
            wroteAny = true;
         }
      }

      if (!wroteAny) {
         String rawFallback = rawToString(root);
         if (rawFallback != null) {
            builder.line("Raw: " + rawFallback, UiColorPalette.textMuted());
         } else {
            builder.line("<No readable fields>", UiColorPalette.textMuted());
         }
      }
   }

   private static void dumpProperty(String label, Object value, int depth, PacketInspectorEngine.ReportBuilder builder, IdentityHashMap<Object, Boolean> visited) {
      String indent = "  ".repeat(Math.max(0, depth));

      try {
         dumpProperty(label, value, depth, indent, builder, visited);
      } catch (Throwable t) {
         builder.line(indent + label + ": <error reading value: " + t.getClass().getSimpleName() + ">", UiColorPalette.errorText());
      }
   }

   private static void dumpProperty(String label, Object value, int depth, String indent, PacketInspectorEngine.ReportBuilder builder, IdentityHashMap<Object, Boolean> visited) {
      if (!builder.isTruncated()) {
         if (value == MISSING) {
            builder.line(indent + label + ": <inaccessible>", UiColorPalette.textMuted());
         } else if (value == null) {
            builder.line(indent + label + ": null", UiColorPalette.textMuted());
         } else if (depth >= 7) {
            builder.line(indent + label + ": <max depth reached>", UiColorPalette.textMuted());
         } else {
            Class<?> valueClass = value.getClass();
            if (isSimpleValue(value)) {
               builder.line(indent + label + ": " + formatLabeledValue(label, value), scalarValueColor(label, value));
            } else if (value instanceof Optional<?> optional) {
               if (optional.isEmpty()) {
                  builder.line(indent + label + ": Optional.empty", UiColorPalette.textMuted());
               } else {
                  builder.line(indent + label + ": Optional", UiColorPalette.textBody());
                  dumpProperty("value", optional.get(), depth + 1, builder, visited);
               }
            } else if (value instanceof BlockHitResult hit) {
               builder.line(indent + label + ": BlockHitResult", UiColorPalette.textBody());
               dumpProperty("blockPos", hit.getBlockPos(), depth + 1, builder, visited);
               dumpProperty("hitPos", hit.getLocation(), depth + 1, builder, visited);
               dumpProperty("side", hit.getDirection(), depth + 1, builder, visited);
               dumpProperty("type", hit.getType(), depth + 1, builder, visited);
               dumpProperty("insideBlock", hit.isInside(), depth + 1, builder, visited);
            } else if (value instanceof Map<?, ?> map) {
               builder.line(indent + label + ": Map[" + map.size() + "]", UiColorPalette.textBody());
               int index = 0;

               for (Entry<?, ?> entry : map.entrySet()) {
                  if (index >= 64) {
                     builder.line(indent + "  ... " + (map.size() - index) + " more entries", UiColorPalette.textMuted());
                     return;
                  }

                  if (isSimpleValue(entry.getKey()) && isSimpleValue(entry.getValue())) {
                     builder.line(indent + "  [" + index + "] " + formatValue(entry.getKey()) + " = " + formatValue(entry.getValue()), UiColorPalette.textBright());
                  } else {
                     builder.line(indent + "  [" + index + "]", UiColorPalette.textBody());
                     dumpProperty("key", entry.getKey(), depth + 2, builder, visited);
                     dumpProperty("value", entry.getValue(), depth + 2, builder, visited);
                  }

                  index++;
               }
            } else if (value instanceof Collection<?> collection) {
               builder.line(indent + label + ": List[" + collection.size() + "]", UiColorPalette.textBody());
               int index = 0;

               for (Object item : collection) {
                  if (index >= 64) {
                     builder.line(indent + "  ... " + (collection.size() - index) + " more entries", UiColorPalette.textMuted());
                     return;
                  }

                  dumpProperty("[" + index + "]", item, depth + 1, builder, visited);
                  index++;
               }
            } else if (valueClass.isArray()) {
               int length = Array.getLength(value);
               if (valueClass.getComponentType().isPrimitive()) {
                  builder.line(indent + label + ": " + primitiveArrayText(value), UiColorPalette.textBright());
               } else {
                  builder.line(indent + label + ": " + classLabel(valueClass) + "[" + length + "]", UiColorPalette.textBody());

                  for (int i = 0; i < Math.min(length, 64); i++) {
                     dumpProperty("[" + i + "]", Array.get(value, i), depth + 1, builder, visited);
                  }

                  if (length > 64) {
                     builder.line(indent + "  ... " + (length - 64) + " more entries", UiColorPalette.textMuted());
                  }
               }
            } else if (visited.containsKey(value)) {
               builder.line(indent + label + ": <already shown " + classLabel(valueClass) + ">", UiColorPalette.textMuted());
            } else if (isOpaqueType(valueClass)) {
               builder.line(indent + label + ": " + describeValue(value), scalarValueColor(label, value));
            } else {
               visited.put(value, Boolean.TRUE);
               List<PacketInspectorEngine.PropertyEntry> properties = properties(value);
               List<Field> fields = instanceFields(valueClass);
               if (properties.isEmpty() && fields.isEmpty()) {
                  builder.line(indent + label + ": " + describeValue(value), scalarValueColor(label, value));
               } else {
                  builder.line(indent + label + ": " + classLabel(valueClass), UiColorPalette.textBody());
                  Set<String> consumedFieldNames = new LinkedHashSet<>();

                  for (PacketInspectorEngine.PropertyEntry property : properties) {
                     dumpProperty(property.label(), property.value(), depth + 1, builder, visited);
                     consumedFieldNames.add(decapitalize(normalizeLabel(property.label())));
                  }

                  for (Field field : fields) {
                     String fieldLabel = fieldLabel(field);
                     if (fieldLabel != null && !fieldLabel.isBlank() && !consumedFieldNames.contains(decapitalize(normalizeLabel(fieldLabel)))) {
                        dumpProperty(fieldLabel, readFieldQuietly(field, value), depth + 1, builder, visited);
                     }
                  }
               }
            }
         }
      }
   }

   private static List<PacketInspectorEngine.PropertyEntry> properties(Object owner) {
      if (owner == null) {
         return List.of();
      }

      Map<String, PacketInspectorEngine.PropertyEntry> properties = new LinkedHashMap<>();
      Class<?> type = owner.getClass();
      if (type.isRecord()) {
         for (RecordComponent component : type.getRecordComponents()) {
            Object value = invokeQuietly(component.getAccessor(), owner);
            putProperty(properties, componentLabel(type, component), value, PacketInspectorEngine.PropertySource.RECORD);
         }
      }

      Arrays.stream(type.getMethods())
         .filter(method -> isInspectableAccessor(method))
         .sorted(Comparator.comparing(Method::getName, String.CASE_INSENSITIVE_ORDER))
         .forEach(method -> putProperty(properties, accessorLabel(method), invokeQuietly(method, owner), PacketInspectorEngine.PropertySource.METHOD));
      if (properties.isEmpty()) {
         collectProperties(properties, owner, type);
      }

      return new ArrayList<>(properties.values());
   }

   private static void collectProperties(Map<String, PacketInspectorEngine.PropertyEntry> properties, Object owner, Class<?> type) {
      if (type.isRecord()) {
         Map<String, Integer> labelCounts = new LinkedHashMap<>();

         for (RecordComponent component : type.getRecordComponents()) {
            Object value = invokeQuietly(component.getAccessor(), owner);
            String alias = firstNonBlank(
               componentLabel(type, component), uniqueTypeLabel(component.getAccessor().getReturnType(), labelCounts)
            );
            putProperty(properties, alias, value, PacketInspectorEngine.PropertySource.RECORD);
         }
      }
   }

   private static String uniqueTypeLabel(Class<?> type, Map<String, Integer> labelCounts) {
      String base = typeLabel(type);
      int count = labelCounts.getOrDefault(base, 0) + 1;
      labelCounts.put(base, count);
      return count == 1 ? base : base + " " + count;
   }

   private static String typeLabel(Class<?> type) {
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
         return type.isArray() ? typeLabel(type.getComponentType()) + " List" : classLabel(type);
      }
   }

   private static void putProperty(Map<String, PacketInspectorEngine.PropertyEntry> properties, String label, Object value, PacketInspectorEngine.PropertySource source) {
      if (label != null && !label.isBlank() && !isObfuscatedLabel(label) && !isIgnoredLabel(label)) {
         String key = normalizeLabel(label);
         if (!properties.containsKey(key)) {
            properties.put(key, new PacketInspectorEngine.PropertyEntry(humanizeLabel(label), value, source));
         }
      }
   }

   private static boolean isInspectableAccessor(Method method) {
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

      if (isIgnoredLabel(method.getName())) {
         return false;
      }

      String mappedLabel = propertyLabel(InspectorMappingsIndex.methodAlias(method), method.getReturnType());
      return isIgnoredLabel(mappedLabel)
         ? false
         : isGetterName(method)
            || EXTRA_GETTER_NAMES.contains(method.getName())
            || mappedLabel != null && EXTRA_GETTER_NAMES.contains(mappedLabel)
            || mappedLabel != null && !isObfuscatedLabel(mappedLabel);
   }

   private static boolean isGetterName(Method method) {
      String name = method.getName();
      return name.startsWith("get") && name.length() > 3
         ? true
         : name.startsWith("is") && name.length() > 2 && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class);
   }

   private static String accessorLabel(Method method) {
      String mappedLabel = propertyLabel(InspectorMappingsIndex.methodAlias(method), method.getReturnType());
      return mappedLabel != null && !mappedLabel.isBlank() ? mappedLabel : propertyLabel(method.getName(), method.getReturnType());
   }

   private static Object invokeQuietly(Method method, Object owner) {
      try {
         if (!method.canAccess(owner)) {
            method.setAccessible(true);
         }

         return method.invoke(owner);
      } catch (Throwable ignored) {
         return MISSING;
      }
   }

   private static List<Field> instanceFields(Class<?> type) {
      List<Field> fields = new ArrayList<>();

      for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
         fields.addAll(
            Arrays.stream(cursor.getDeclaredFields())
               .filter(field -> !Modifier.isStatic(field.getModifiers()))
               .filter(field -> !Modifier.isTransient(field.getModifiers()))
               .filter(field -> !field.isSynthetic())
               .filter(field -> {
                  String fieldLabel = fieldLabel(field);
                  return fieldLabel != null && !fieldLabel.isBlank();
               })
               .sorted((a, b) -> fieldLabel(a).compareToIgnoreCase(fieldLabel(b)))
               .collect(Collectors.toList())
         );
      }

      return fields;
   }

   private static Object readFieldQuietly(Field field, Object owner) {
      try {
         if (!field.canAccess(owner)) {
            field.setAccessible(true);
         }

         return field.get(owner);
      } catch (Throwable ignored) {
         return MISSING;
      }
   }

   private static Object fieldValue(Object owner, String fieldName) {
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

   private static Object firstFieldValue(Object owner, String... fieldNames) {
      for (String fieldName : fieldNames) {
         Object value = fieldValue(owner, fieldName);
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   private static Object firstPresent(Object... values) {
      if (values == null) {
         return null;
      }

      for (Object value : values) {
         if (value != null && value != MISSING) {
            return value;
         }
      }

      return null;
   }

   private static String firstNonBlank(String... values) {
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

   private static String componentLabel(Class<?> ownerType, RecordComponent component) {
      return component == null ? null : firstNonBlank(InspectorMappingsIndex.methodAlias(component.getAccessor()), component.getName());
   }

   private static String fieldLabel(Field field) {
      return field == null ? null : firstNonBlank(InspectorMappingsIndex.fieldAlias(field), isObfuscatedLabel(field.getName()) ? null : field.getName());
   }

   private static Object recordComponentAt(Object owner, int index) {
      if (owner != null && index >= 0) {
         try {
            Class<?> type = owner.getClass();
            if (!type.isRecord()) {
               return null;
            }

            RecordComponent[] components = type.getRecordComponents();
            return components != null && index < components.length ? invokeQuietly(components[index].getAccessor(), owner) : null;
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Object recordComponentOfType(Object owner, Class<?> expectedType) {
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
               Object value = invokeQuietly(component.getAccessor(), owner);
               if (value != null && value != MISSING && expectedType.isInstance(value)) {
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

   private static Object recordComponentByNames(Object owner, int requiredMatches, String... methodNames) {
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
               Object value = invokeQuietly(component.getAccessor(), owner);
               if (value != null && value != MISSING) {
                  int matches = 0;

                  for (String methodName : methodNames) {
                     if (hasMethod(value.getClass(), methodName)) {
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

   private static Object firstOtherRecordComponent(Object owner, Object... excludedValues) {
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
            Object value = invokeQuietly(component.getAccessor(), owner);
            if (value != null && value != MISSING && !isExcluded(value, excludedValues)) {
               String text = componentText(value);
               if (text != null && !text.isBlank()) {
                  return value;
               }
            }
         }
      } catch (Throwable var10) {
      }

      return null;
   }

   private static Integer intField(Object owner, String fieldName) {
      return fieldValue(owner, fieldName) instanceof Number number ? number.intValue() : null;
   }

   private static Boolean boolField(Object owner, String fieldName) {
      return fieldValue(owner, fieldName) instanceof Boolean bool ? bool : null;
   }

   private static Object callGetter(Object owner, String methodName) {
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

   private static Object firstGetterValue(Object owner, String... methodNames) {
      for (String methodName : methodNames) {
         Object value = callGetter(owner, methodName);
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   private static Object propertyByLabel(Object owner, String... labels) {
      if (owner != null && labels != null && labels.length != 0) {
         List<PacketInspectorEngine.PropertyEntry> properties = properties(owner);

         for (String label : labels) {
            String normalizedTarget = normalizeLabel(label);

            for (PacketInspectorEngine.PropertyEntry property : properties) {
               if (normalizedTarget.equals(normalizeLabel(property.label()))) {
                  Object value = property.value();
                  if (value != null && value != MISSING) {
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

   private static boolean hasMethod(Class<?> type, String methodName) {
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

   private static boolean isExcluded(Object value, Object... excludedValues) {
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

   private static boolean isPacketNamed(Packet<?> packet, String packetNameHint, String... names) {
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

   private static boolean isSimpleValue(Object value) {
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

   private static boolean isOpaqueType(Class<?> type) {
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

   private static int valueColor(Object value) {
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

   private static int directionColor(String direction) {
      return "C2S".equalsIgnoreCase(direction) ? UiColorPalette.cyan() : UiColorPalette.amber();
   }

   private static String formatValue(Object value) {
      return formatLabeledValue("", value);
   }

   private static String formatLabeledValue(String label, Object value) {
      if (value == null) {
         return "null";
      } else if (value instanceof Number number && isEntityIdLabel(label)) {
         return describeEntityId(number.intValue());
      } else if (value instanceof String string) {
         return quote(sanitize(string));
      } else if (value instanceof Character c) {
         return quote(String.valueOf(c));
      } else if (value instanceof Component text) {
         return quote(sanitize(text.getString()));
      } else if (value instanceof Identifier id) {
         return id.toString();
      } else if (value instanceof UUID uuid) {
         return uuid.toString();
      } else if (value instanceof Instant instant) {
         return instant.toString();
      } else if (value instanceof Enum<?> enumValue) {
         return enumValue.name();
      } else if (value instanceof BlockPos pos) {
         return formatPos(pos);
      } else if (value instanceof Vec3 vec) {
         return formatVec(vec);
      } else if (value instanceof Entity entity) {
         return describeEntity(entity);
      } else if (value instanceof ItemStack stack) {
         return describeStack(stack);
      } else if (value instanceof Tag nbt) {
         return sanitize(nbt.asString().orElse(String.valueOf(nbt)));
      } else if (value instanceof BlockState state) {
         return describeBlockState(state);
      } else if (value instanceof Holder<?> entry) {
         return describeHolder(entry);
      } else if (value instanceof BitSet bitSet) {
         return formatBitSet(bitSet);
      } else {
         return value instanceof Class<?> clazz ? classLabel(clazz) : rewriteNames(sanitize(String.valueOf(value)));
      }
   }

   private static String describeValue(Object value) {
      if (value == null) {
         return "null";
      }

      if (isSimpleValue(value)) {
         return formatValue(value);
      }

      try {
         return rewriteNames(sanitize(String.valueOf(value)));
      } catch (Throwable ignored) {
         return "<unprintable " + classLabel(value.getClass()) + ">";
      }
   }

   private static String rawToString(Object value) {
      if (value == null) {
         return null;
      }

      try {
         String raw = rewriteNames(sanitize(String.valueOf(value)));
         if (raw != null && !raw.isBlank()) {
            return isDefaultObjectToString(raw) ? null : raw;
         } else {
            return null;
         }
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static boolean isDefaultObjectToString(String raw) {
      return raw != null && raw.matches("[\\w.$]+@[0-9a-fA-F]+");
   }

   private static String describeHotbarSlot(int slot) {
      return slot >= 0 && slot <= 8 ? slot + 1 + " (hotbar index " + slot + ")" : String.valueOf(slot);
   }

   private static String describePlayerAction(Action action) {
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
            var10000 = friendlyClassLabel(action.name());
      }

      return var10000;
   }

   private static String describeClickType(Object actionType) {
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
         return actionType == null ? "Click Slot" : "Click Slot (" + describeValue(actionType) + ")";
      }
   }

   private static String componentText(Object value) {
      if (value == null || value == MISSING) {
         return null;
      } else if (value instanceof Component text) {
         return text.getString();
      } else if (value instanceof String string) {
         return string;
      } else if (value instanceof Optional<?> optional) {
         return optional.<String>map(PacketInspectorEngine::componentText).orElse(null);
      } else if (value instanceof String[] array) {
         return String.join(" | ", array);
      } else {
         Object stringLike = firstGetterValue(value, "getString", "string", "content", "raw", "name");
         if (stringLike instanceof Component text) {
            return text.getString();
         } else {
            return stringLike instanceof String string ? string : null;
         }
      }
   }

   private static String bookPagesPreview(Collection<?> pages) {
      List<String> previews = new ArrayList<>();
      int index = 0;

      for (Object page : pages) {
         String text = componentText(page);
         if ((text == null || text.isBlank()) && page != null) {
            text = describeValue(page);
         }

         if (text != null && !text.isBlank()) {
            previews.add("p" + (index + 1) + "=" + quote(sanitize(text)));
         }

         index++;
         if (previews.size() >= 2) {
            break;
         }
      }

      return previews.isEmpty() ? null : String.join(" | ", previews);
   }

   private static String signLinesPreview(String[] lines) {
      List<String> parts = new ArrayList<>();

      for (int i = 0; i < lines.length; i++) {
         String line = lines[i] == null ? "" : lines[i].trim();
         if (!line.isEmpty()) {
            parts.add("L" + (i + 1) + "=" + quote(sanitize(line)));
         }
      }

      return parts.isEmpty() ? "<empty sign>" : String.join(" | ", parts);
   }

   private static PacketInspectorEngine.DecodeCheckPair appendChatSummary(
      Object messageValue, Object parameters, UUID senderUuid, Object timestampValue, Object filterMask, String kind, PacketInspectorEngine.ReportBuilder builder
   ) {
      boolean wrote = false;
      boolean informative = false;
      if (kind != null && !kind.isBlank()) {
         builder.line("Kind: " + kind, scalarValueColor("Kind", kind));
         wrote = true;
      }

      String chatType = chatTypeName(parameters);
      if (chatType != null && !chatType.isBlank()) {
         builder.line("Chat Type: " + chatType, scalarValueColor("Chat Type", chatType));
         wrote = true;
         informative = true;
      }

      String sender = senderName(parameters, senderUuid);
      if (sender != null && !sender.isBlank()) {
         builder.line("Sender: " + sender, scalarValueColor("Sender", sender));
         wrote = true;
         informative = true;
      }

      String target = chatTargetName(parameters);
      if (target != null && !target.isBlank()) {
         builder.line("Target: " + target, scalarValueColor("Target", target));
         wrote = true;
         informative = true;
      }

      String message = componentText(messageValue);
      if (message != null && !message.isBlank()) {
         builder.line("Message: " + quote(sanitize(message)), scalarValueColor("Message", message));
         wrote = true;
         informative = true;
      } else if (messageValue != null) {
         builder.line("Message: " + valueText(messageValue), labeledValueColor("Message", messageValue));
         wrote = true;
         informative = true;
      }

      if (timestampValue != null) {
         builder.line("Timestamp: " + describeValue(timestampValue), scalarValueColor("Timestamp", timestampValue));
         wrote = true;
         informative = true;
      }

      String filter = filterMaskText(filterMask);
      if (filter != null) {
         builder.line("Filter: " + filter, scalarValueColor("Filter", filter));
         wrote = true;
         informative = true;
      }

      return new PacketInspectorEngine.DecodeCheckPair(wrote, informative);
   }

   private static String senderName(Object parameters, UUID senderUuid) {
      String senderName = componentText(
         firstPresent(
            callGetter(parameters, "name"),
            recordComponentAt(parameters, 1),
            firstOtherRecordComponent(parameters, recordComponentAt(parameters, 0), recordComponentAt(parameters, 2))
         )
      );
      if (senderName == null || senderName.isBlank()) {
         senderName = playerNameFor(senderUuid);
      }

      if (senderName != null && !senderName.isBlank()) {
         return senderUuid == null ? sanitize(senderName) : sanitize(senderName) + " (" + senderUuid + ")";
      } else {
         return senderUuid == null ? null : senderUuid.toString();
      }
   }

   private static String chatTargetName(Object parameters) {
      String targetName = componentText(firstPresent(callGetter(parameters, "targetName"), recordComponentAt(parameters, 2)));
      return targetName != null && !targetName.isBlank() ? sanitize(targetName) : null;
   }

   private static String chatTypeName(Object parameters) {
      Object type = firstPresent(callGetter(parameters, "type"), recordComponentAt(parameters, 0));
      return type == null ? null : describeValue(type);
   }

   private static String filterMaskText(Object filterMask) {
      if (filterMask == null) {
         return null;
      }

      Object fullyFiltered = callGetter(filterMask, "isFullyFiltered");
      if (Boolean.TRUE.equals(fullyFiltered)) {
         return "Fully filtered";
      }

      Object passThrough = callGetter(filterMask, "isPassThrough");
      return Boolean.TRUE.equals(passThrough) ? "Pass-through" : "Partially filtered";
   }

   private static String playerNameFor(UUID uuid) {
      if (uuid == null) {
         return null;
      }

      try {
         if (MC.getConnection() != null) {
            PlayerInfo entry = MC.getConnection().getPlayerInfo(uuid);
            if (entry != null && entry.getProfile() != null) {
               String profileName = componentText(entry.getProfile());
               if (profileName != null && !profileName.isBlank()) {
                  return profileName;
               }
            }
         }
      } catch (Throwable var4) {
      }

      try {
         if (MC.level != null) {
            for (AbstractClientPlayer player : MC.level.players()) {
               if (uuid.equals(player.getUUID()) && player.getGameProfile() != null) {
                  String profileName = componentText(player.getGameProfile());
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

   private static PacketInspectorEngine.ParsedMessagePair parseChatLine(String raw) {
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

   private static String describeEntity(Entity entity) {
      String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      String name = entity.getDisplayName() == null ? "" : entity.getDisplayName().getString();
      return typeId + " #" + entity.getId() + " [" + sanitize(name) + "] @" + formatVec(new Vec3(entity.getX(), entity.getY(), entity.getZ()));
   }

   private static void appendEntity(PacketInspectorEngine.ReportBuilder builder, String label, int entityId) {
      builder.line(label + ": " + describeEntityId(entityId), UiColorPalette.cyan());
   }

   private static String describeEntityId(int entityId) {
      Entity entity = entityById(entityId);
      return entity != null ? entityId + " -> " + describeEntity(entity) : entityId + " (entity not loaded in client world)";
   }

   private static String entityIdList(Object ids) {
      if (ids != null && ids != MISSING) {
         List<String> parts = new ArrayList<>();
         int total = 0;
         if (ids instanceof int[] array) {
            total = array.length;

            for (int i = 0; i < Math.min(array.length, 8); i++) {
               parts.add(describeEntityId(array[i]));
            }
         } else if (ids instanceof Collection<?> collection) {
            total = collection.size();
            int shown = 0;

            for (Object id : collection) {
               if (shown >= 8) {
                  break;
               }

               if (id instanceof Number number) {
                  parts.add(describeEntityId(number.intValue()));
               } else {
                  parts.add(valueText(id));
               }

               shown++;
            }
         } else {
            if (!ids.getClass().isArray()) {
               return valueText(ids);
            }

            int length = Array.getLength(ids);
            total = length;

            for (int i = 0; i < Math.min(length, 8); i++) {
               Object id = Array.get(ids, i);
               parts.add(id instanceof Number number ? describeEntityId(number.intValue()) : valueText(id));
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

   private static String describeCollection(Collection<?> collection) {
      if (collection == null) {
         return "null";
      }

      List<String> parts = new ArrayList<>();
      int shown = 0;

      for (Object item : collection) {
         if (shown >= 6) {
            break;
         }

         parts.add(valueText(item));
         shown++;
      }

      if (collection.size() > shown) {
         parts.add("... +" + (collection.size() - shown) + " more");
      }

      return "[" + collection.size() + "] " + String.join(", ", parts);
   }

   private static Entity entityById(int entityId) {
      try {
         return MC.level == null ? null : MC.level.getEntity(entityId);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private static boolean isEntityIdLabel(String label) {
      String normalized = normalizeLabel(label);
      return normalized.equals("entityid")
         || normalized.equals("entity")
         || normalized.equals("entityindex")
         || normalized.equals("targetentityid")
         || normalized.equals("sourceentityid")
         || normalized.endsWith("entityid");
   }

   private static String entityTypeId(EntityType<?> type) {
      if (type == null) {
         return "Unknown";
      }

      try {
         return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
      } catch (Throwable ignored) {
         return describeValue(type);
      }
   }

   private static String describeStack(ItemStack stack) {
      if (stack.isEmpty()) {
         return "ItemStack.EMPTY";
      }

      String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      String name = stack.getHoverName() == null ? "" : stack.getHoverName().getString();
      String display = itemId;
      if (!name.isEmpty() && !Objects.equals(name, stack.getItem().getName(stack).getString())) {
         display = display + " [" + sanitize(name) + "]";
      }

      return stack.getCount() + "x " + display;
   }

   private static String describeBlockState(BlockState state) {
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " " + sanitize(state.toString());
   }

   private static String formatPos(BlockPos pos) {
      return "x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ();
   }

   private static String formatVec(Vec3 vec) {
      return String.format(Locale.ROOT, "x=%.3f, y=%.3f, z=%.3f", vec.x, vec.y, vec.z);
   }

   private static String formatBitSet(BitSet bitSet) {
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

   private static String describeHolder(Holder<?> entry) {
      try {
         Optional<?> key = entry.unwrapKey();
         if (key.isPresent()) {
            return sanitize(String.valueOf(key.get()));
         }
      } catch (Throwable var3) {
      }

      try {
         Object value = callGetter(entry, "value");
         if (value != null) {
            return sanitize(String.valueOf(value));
         }
      } catch (Throwable var2) {
      }

      return sanitize(String.valueOf(entry));
   }

   private static String valueText(Object value) {
      return describeLabeledValue("", value);
   }

   private static String describeLabeledValue(String label, Object value) {
      if (value == null || value == MISSING) {
         return null;
      } else if (isSimpleValue(value)) {
         return formatLabeledValue(label, value);
      } else if (value instanceof Optional<?> optional) {
         return optional.<String>map(item -> describeLabeledValue(label, item)).orElse("Optional.empty");
      } else if (value instanceof Collection<?> collection) {
         return classLabel(value.getClass()) + "[" + collection.size() + "]";
      } else if (value instanceof Map<?, ?> map) {
         return "Map[" + map.size() + "]";
      } else {
         Class<?> type = value.getClass();
         return type.isArray() ? type.getComponentType().getSimpleName() + "[" + Array.getLength(value) + "]" : describeValue(value);
      }
   }

   private static int labeledValueColor(String label, Object value) {
      if (value != null && value != MISSING) {
         return !(value instanceof Collection) && !(value instanceof Map) && !value.getClass().isArray() ? scalarValueColor(label, value) : UiColorPalette.textBody();
      } else {
         return UiColorPalette.textMuted();
      }
   }

   private static int plainValueColor(Object value) {
      return labeledValueColor("", value);
   }

   private static int scalarValueColor(String label, Object value) {
      String normalized = normalizeLabel(label);
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
            return valueColor(value);
         }
      }
   }

   private static String humanizeLabel(String label) {
      return friendlyClassLabel(label).replace('.', ' ').trim();
   }

   private static String decapitalize(String value) {
      return value != null && !value.isEmpty() ? Character.toLowerCase(value.charAt(0)) + value.substring(1) : "";
   }

   private static String normalizeLabel(String value) {
      return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
   }

   private static boolean isObfuscatedLabel(String label) {
      if (label != null && !label.isBlank()) {
         String normalized = label.trim().toLowerCase(Locale.ROOT);
         return normalized.matches("(comp|field|method|class)_?\\d+");
      } else {
         return false;
      }
   }

   private static boolean isIgnoredLabel(String label) {
      return label != null && !label.isBlank() ? IGNORED_METHOD_NAMES.contains(label) || IGNORED_METHOD_KEYS.contains(normalizeLabel(label)) : false;
   }

   private static int countNonEmpty(List<ItemStack> stacks) {
      int count = 0;

      for (ItemStack stack : stacks) {
         if (stack != null && !stack.isEmpty()) {
            count++;
         }
      }

      return count;
   }

   private static PacketInspectorEngine.InteractEntityCapture captureInteract(ServerboundInteractPacket packet) {
      PacketInspectorEngine.InteractEntityCapture capture = new PacketInspectorEngine.InteractEntityCapture();

      try {
         capture.action = "Interact At";
         capture.hand = packet.hand();
         capture.hitPos = packet.location();
      } catch (Throwable ignored) {
         Object interactionType = fieldValue(packet, "type");
         if (interactionType != null && interactionType != MISSING) {
            capture.action = classLabel(interactionType.getClass());
            if (firstFieldValue(interactionType, "hand", "interactionHand") instanceof InteractionHand actualHand) {
               capture.hand = actualHand;
            }

            if (firstFieldValue(interactionType, "pos", "hitPos", "location") instanceof Vec3 actualHitPos) {
               capture.hitPos = actualHitPos;
            }
         }
      }

      return capture;
   }

   private static String primitiveArrayText(Object array) {
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

   private static String classLabel(Class<?> type) {
      if (type == null) {
         return "Unknown";
      }

      String alias = InspectorMappingsIndex.classAlias(type);
      return alias != null && !alias.isBlank() ? friendlyClassLabel(alias) : friendlyClassLabel(type.getSimpleName().isEmpty() ? type.getName() : type.getSimpleName());
   }

   private static String friendlyClassLabel(String value) {
      if (value != null && !value.isEmpty()) {
         String alias = firstNonBlank(InspectorMappingsIndex.aliasForClass(value.replace('/', '.')), InspectorMappingsIndex.aliasForSimpleName(value.substring(value.lastIndexOf(46) + 1)));
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

   private static String propertyLabel(String label, Class<?> returnType) {
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

   private static String describeEquipment(Collection<?> equipmentEntries) {
      if (equipmentEntries != null && !equipmentEntries.isEmpty()) {
         List<String> parts = new ArrayList<>();
         int shown = 0;

         for (Object entry : equipmentEntries) {
            if (shown >= 6) {
               parts.add("... +" + (equipmentEntries.size() - shown) + " more");
               break;
            }

            String summary = slotEntrySummary(entry);
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

   private static String slotEntrySummary(Object entry) {
      if (entry == null) {
         return null;
      }

      Object slot = firstPresent(
         firstGetterValue(entry, "getFirst", "first", "slot", "getSlot"), recordComponentAt(entry, 0), fieldValue(entry, "first")
      );
      Object stack = firstPresent(
         firstGetterValue(entry, "getSecond", "second", "stack", "getStack"), recordComponentAt(entry, 1), fieldValue(entry, "second")
      );
      if (slot == null && stack == null) {
         return valueText(entry);
      }

      String slotComponent = slot == null ? "Unknown Slot" : describeValue(slot);
      String stackComponent = stack instanceof ItemStack itemStack ? describeStack(itemStack) : valueText(stack);
      return slotComponent + "=" + stackComponent;
   }

   private static String rewriteNames(String raw) {
      if (raw != null && !raw.isBlank()) {
         String rewritten = rewriteIntermediary(raw, INTERMEDIARY_QUALIFIED_NAME, true);
         return rewriteIntermediary(rewritten, INTERMEDIARY_SIMPLE_NAME, false);
      } else {
         return raw;
      }
   }

   private static String rewriteIntermediary(String raw, Pattern pattern, boolean qualified) {
      Matcher matcher = pattern.matcher(raw);
      StringBuffer buffer = new StringBuffer();
      boolean changed = false;

      while (matcher.find()) {
         String original = matcher.group(1);
         String alias = qualified ? InspectorMappingsIndex.aliasForClass(original) : InspectorMappingsIndex.aliasForSimpleName(original);
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

   private static String quote(String value) {
      return "\"" + value + "\"";
   }

   private static String sanitize(String value) {
      if (value == null) {
         return "";
      }

      String sanitized = value.replace('\n', ' ').replace('\r', ' ').trim();
      return sanitized.length() <= 320 ? sanitized : sanitized.substring(0, 317) + "...";
   }

   private static BlockState blockStateAt(BlockPos pos) {
      try {
         return MC.level != null ? MC.level.getBlockState(pos) : null;
      } catch (Throwable ignored) {
         return null;
      }
   }

   private record PayloadDecodeResult(boolean init_Z, boolean load_Z, boolean read, boolean write) {
      private static final PacketInspectorEngine.PayloadDecodeResult NONE = new PacketInspectorEngine.PayloadDecodeResult(false, false, false, false);
      private static final PacketInspectorEngine.PayloadDecodeResult FULL_DECODE = new PacketInspectorEngine.PayloadDecodeResult(true, true, true, true);
   }

   private enum CompressionKind {
      NONE("none"),
      GZIP("gzip"),
      ZLIB("zlib/deflate");

      private final String init_Str;

      CompressionKind(String label) {
         this.init_Str = label;
      }

      String label() {
         return this.init_Str;
      }
   }

   private static final class ReportBuilder {
      private final String title;
      private final List<PacketInspectorEngine.ColoredLine> lines = new ArrayList<>();
      private final StringBuilder text = new StringBuilder();
      private boolean truncated;

      private ReportBuilder(String title) {
         this.title = title;
      }

      void section(String title, int color) {
         this.line("[" + title + "]", color);
      }

      void line(String text, int color) {
         if (!this.truncated) {
            if (this.lines.size() >= 900) {
               this.truncated = true;
               this.lines.add(new PacketInspectorEngine.ColoredLine("... output truncated ...", UiColorPalette.errorText()));
               this.text.append("... output truncated ...\n");
            } else {
               this.lines.add(new PacketInspectorEngine.ColoredLine(text, color));
               this.text.append(text).append('\n');
            }
         }
      }

      void blankLine() {
         this.line("", UiColorPalette.textBright());
      }

      void append(PacketInspectorEngine.ReportBuilder other) {
         if (other != null) {
            for (PacketInspectorEngine.ColoredLine line : other.lines) {
               this.line(line.text(), line.color());
            }
         }
      }

      boolean isTruncated() {
         return this.truncated;
      }

      PacketInspectorEngine.InspectionReport build() {
         return new PacketInspectorEngine.InspectionReport(this.title, this.lines, this.text.toString());
      }
   }

   private record WireFieldSlice(int init_I, int load, String init_Str) {
   }

   private record PropertyEntry(String label, Object value, PacketInspectorEngine.PropertySource source) {
   }

   private record PacketSummaryFlags(boolean init_Z, boolean load) {
      private static final PacketInspectorEngine.PacketSummaryFlags NONE = new PacketInspectorEngine.PacketSummaryFlags(false, false);
   }

   public static final class InspectionReport {
      private final String title;
      private final List<PacketInspectorEngine.ColoredLine> lines;
      private final String copyText;

      private InspectionReport(String title, List<PacketInspectorEngine.ColoredLine> lines, String copyText) {
         this.title = title;
         this.lines = Collections.unmodifiableList(lines);
         this.copyText = copyText;
      }

      public String title() {
         return this.title;
      }

      public List<PacketInspectorEngine.ColoredLine> lines() {
         return this.lines;
      }

      public String copyText() {
         return this.copyText;
      }
   }

   private static final class InteractEntityCapture {
      private String action;
      private InteractionHand hand;
      private Vec3 hitPos;
   }

   private enum PropertySource {
      RECORD,
      METHOD;
   }

   private record ParsedMessagePair(String init, String load) {
   }

   private record DecodeCheckPair(boolean init, boolean load) {
   }

   public record PayloadAnalysis(boolean complete, boolean load, double progress, String status, List<PacketInspectorEngine.ColoredLine> lines) {
      public PayloadAnalysis {
         progress = Math.max(0.0, Math.min(1.0, progress));
         status = status == null ? "" : status;
         lines = lines == null ? List.of() : List.copyOf(lines);
      }
   }

   public static final class ColoredLine {
      private final String text;
      private final int color;

      public ColoredLine(String text, int color) {
         this.text = text;
         this.color = color;
      }

      public String text() {
         return this.text;
      }

      public int color() {
         return this.color;
      }
   }

   private record VarIntRead(int init, int load) {
   }

   private static final class ByteCursor {
      private final byte[] bytes;
      private int position;

      private ByteCursor(byte[] bytes) {
         this.bytes = bytes == null ? new byte[0] : bytes;
      }

      int position() {
         return this.position;
      }

      void seek(int position) {
         this.position = Math.max(0, Math.min(this.bytes.length, position));
      }

      int remaining() {
         return Math.max(0, this.bytes.length - this.position);
      }

      byte[] remainingBytes() {
         return this.remaining() <= 0 ? new byte[0] : Arrays.copyOfRange(this.bytes, this.position, this.bytes.length);
      }

      String readJavaUtf() throws IOException {
         if (this.remaining() < 2) {
            throw new IOException("expected Java UTF length");
         }

         int length = (this.bytes[this.position] & 255) << 8 | this.bytes[this.position + 1] & 255;
         if (length >= 0 && this.position + 2 + length <= this.bytes.length) {
            int start = this.position;

            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(this.bytes, start, length + 2))) {
               String value = in.readUTF();
               this.position = start + 2 + length;
               return value;
            }
         } else {
            throw new IOException("Java UTF length " + length + " exceeds remaining " + this.remaining());
         }
      }

      byte[] readShortPrefixedBytes() throws IOException {
         if (this.remaining() < 2) {
            throw new IOException("expected unsigned short byte length");
         } else {
            int length = (this.bytes[this.position] & 255) << 8 | this.bytes[this.position + 1] & 255;
            this.position += 2;
            if (length >= 0 && this.position + length <= this.bytes.length) {
               byte[] out = Arrays.copyOfRange(this.bytes, this.position, this.position + length);
               this.position += length;
               return out;
            } else {
               throw new IOException("byte array length " + length + " exceeds remaining " + this.remaining());
            }
         }
      }
   }
}
