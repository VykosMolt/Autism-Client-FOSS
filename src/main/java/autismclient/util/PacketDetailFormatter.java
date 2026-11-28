package autismclient.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class PacketDetailFormatter {
   private static final int init = 16;
   private static final int load = 12;

   private PacketDetailFormatter() {
   }

   public static PacketDetailFormatter.PacketAnalysis analyze(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      PacketDetailFormatter.AnalysisBuilder out = new PacketDetailFormatter.AnalysisBuilder();
      if (entry == null) {
         return out.build(false, "fallback");
      }

      init_V_AutismPacketLogger_bh_StreamUtil(entry, out);
      PacketDetailFormatter.WireDecode wireDecode = init_bh_CachePool_AutismPacketLogger(entry);
      Packet<?> packet = wireDecode.packet != null ? wireDecode.packet : entry.packet;
      if (packet == null) {
         if (wireDecode.init_Str != null && !wireDecode.init_Str.isBlank()) {
            out.addLine("Wire Decode: " + wireDecode.init_Str, wireDecode.decoded ? UiColorPalette.mint() : UiColorPalette.warningYellow());
         }

         return out.build(false, "fallback");
      } else {
         boolean complete = init_Z_Packet(packet, out, wireDecode);
         return out.build(complete, complete ? "complete" : (out.hasLines() ? wireDecode.sourceLabel() : "fallback"));
      }
   }

   public static String describe(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      PacketDetailFormatter.PacketAnalysis analysis = analyze(entry);
      if (!analysis.init_Z() && !analysis.hasMeaning()) {
         return "";
      }

      StringBuilder out = new StringBuilder();
      if (analysis.init_Z()) {
         out.append("[Reality]\n");
         appendLines(out, analysis.lines());
      }

      if (analysis.hasMeaning()) {
         out.append("\n[Meaning]\n");
         appendLines(out, analysis.meaningLines());
      }

      return out.toString().stripTrailing();
   }

   private static void appendLines(StringBuilder out, List<PacketInspectorEngine.ColoredLine> lines) {
      for (PacketInspectorEngine.ColoredLine line : lines) {
         if (line != null) {
            out.append(line.text()).append('\n');
         }
      }
   }

   private static PacketDetailFormatter.WireDecode init_bh_CachePool_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry == null || entry.packet == null || entry.packetClass == null) {
         return PacketDetailFormatter.WireDecode.none();
      }

      if (!Packet.class.isAssignableFrom(entry.packetClass)) {
         return PacketDetailFormatter.WireDecode.none();
      }

      PacketWireCaptureStore.PacketCapture snapshot = PacketWireCaptureStore.captureFor(entry.packet);
      if (snapshot != null && snapshot.plaintext().length != 0) {
         try {
            Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)entry.packetClass;
            PacketStreamCodecDecoder.DecodedFrame decoded = PacketStreamCodecDecoder.decodeFrame(packetClass, snapshot.plaintext(), snapshot.protocol());
            return PacketDetailFormatter.WireDecode.ofDecoded(decoded.packet(), "decoded from captured plaintext with Minecraft STREAM_CODEC");
         } catch (Throwable t) {
            return PacketDetailFormatter.WireDecode.ofFailure("captured plaintext was kept, but STREAM_CODEC decode failed: " + describeValue(t));
         }
      } else {
         return PacketDetailFormatter.WireDecode.none();
      }
   }

   private static boolean init_Z_Packet(Packet<?> packet, PacketDetailFormatter.AnalysisBuilder out, PacketDetailFormatter.WireDecode wireDecode) {
      if (wireDecode != null && wireDecode.init_Str != null && !wireDecode.init_Str.isBlank()) {
         out.addLine("Wire Decode: " + wireDecode.init_Str, wireDecode.decoded ? UiColorPalette.mint() : UiColorPalette.warningYellow());
      }

      PacketSchemaSnapshot view = PacketSchemaSnapshot.capture(packet);
      init_V_br_bh_StreamUtil(view, out);
      init_V_Packet(packet, view, out);
      return view.read();
   }

   private static void init_V_br_bh_StreamUtil(PacketSchemaSnapshot view, PacketDetailFormatter.AnalysisBuilder out) {
      if (view == null) {
         out.addLine("Status: fallback", UiColorPalette.warningYellow());
         out.addLine("No packet object was available.", UiColorPalette.errorText());
      } else {
         PacketSchemaRegistry.PacketSchema schema = view.schema();
         out.addLine("Status: " + view.status(), view.read() ? UiColorPalette.mint() : UiColorPalette.warningYellow());
         if (schema != null) {
            out.addLine("Schema: " + (view.load_Z() ? "Minecraft source-backed schema" : "reflection fallback"), UiColorPalette.textBright());
            out.addLine("Protocol: " + schema.read() + " | Direction: " + schema.write(), UiColorPalette.textBody());
            out.addLine("Codec: " + schema.update() + " | Source: " + schema.flush(), UiColorPalette.textBody());
            if (!schema.sync().isBlank()) {
               out.addLine("Packet Type: " + schema.sync(), UiColorPalette.textBody());
            }

            if (schema.inherited()) {
               out.addLine("Field Note: using parent packet schema for nested packet variant", UiColorPalette.warningYellow());
            }
         } else {
            out.addLine("Schema: reflection fallback", UiColorPalette.warningYellow());
         }

         if (!view.fallbackNote().isBlank()) {
            out.addLine("Fallback Note: " + view.fallbackNote(), UiColorPalette.textMuted());
         }

         if (view.fields().isEmpty()) {
            out.addLine("Fields: none", UiColorPalette.textMuted());
         } else {
            out.addLine("Fields: " + view.fields().size(), UiColorPalette.textBright());

            for (PacketFieldSnapshot field : view.fields()) {
               int color = field.readable() ? UiColorPalette.softWhite() : UiColorPalette.errorText();
               out.addLine(field.name() + " (" + field.typeName() + "): " + field.write(), color);

               for (String detail : field.init_List()) {
                  out.addLine("  " + detail, UiColorPalette.textBody());
               }
            }
         }
      }
   }

   private static void init_V_Packet(Packet<?> packet, PacketSchemaSnapshot view, PacketDetailFormatter.AnalysisBuilder out) {
      if (packet instanceof ClientboundOpenScreenPacket open) {
         out.addSemantic("Server opens a handled screen.", UiColorPalette.textBright());
         out.addSemantic("Container: #" + open.getContainerId() + " type=" + describeValue(open.getType()), UiColorPalette.blue());
         out.addSemantic("Title: " + quote(open.getTitle() == null ? "" : open.getTitle().getString()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         out.addSemantic("Server replaces the full contents of a container.", UiColorPalette.textBright());
         out.addSemantic("Container: #" + content.containerId() + " state=" + content.stateId(), UiColorPalette.blue());
         out.addSemantic("Slots: " + content.items().size() + " total, " + countNonEmpty(content.items()) + " non-empty", UiColorPalette.textBright());
         out.addSemantic("Carried Item: " + PacketStateDeltaTracker.describeStack(content.carriedItem()), UiColorPalette.successText());
         init_V_List_bh_StreamUtil(content.items(), out);
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         out.addSemantic("Server updates one container slot.", UiColorPalette.textBright());
         out.addSemantic("Container: #" + slot.getContainerId() + " state=" + slot.getStateId(), UiColorPalette.blue());
         out.addSemantic("Slot: " + slot.getSlot(), UiColorPalette.textBright());
         out.addSemantic("Item: " + PacketStateDeltaTracker.describeStack(slot.getItem()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         out.addSemantic("Server sets the carried cursor item.", UiColorPalette.textBright());
         out.addSemantic("Cursor: " + PacketStateDeltaTracker.describeStack(cursor.contents()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventory) {
         out.addSemantic("Server updates a player inventory slot.", UiColorPalette.textBright());
         out.addSemantic("Inventory Slot: " + inventory.slot(), UiColorPalette.blue());
         out.addSemantic("Item: " + PacketStateDeltaTracker.describeStack(inventory.contents()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         out.addSemantic("Server changes the selected hotbar slot.", UiColorPalette.textBright());
         out.addSemantic("Selected Slot: " + held.slot(), UiColorPalette.successText());
      } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         out.addSemantic("Client changes the selected hotbar slot.", UiColorPalette.textBright());
         out.addSemantic("Selected Slot: " + carried.getSlot(), UiColorPalette.successText());
      } else if (packet instanceof ServerboundContainerClickPacket click) {
         out.addSemantic("Client clicks inside a handled screen.", UiColorPalette.textBright());
         out.addSemantic("Container: #" + click.containerId() + " state=" + click.stateId(), UiColorPalette.blue());
         out.addSemantic("Input: " + describeInput(click.containerInput()) + " (" + click.containerInput() + ")", UiColorPalette.successText());
         out.addSemantic("Slot: " + click.slotNum() + " | Button: " + describeButton(click.containerInput(), click.buttonNum()), UiColorPalette.textBright());
         out.addSemantic("Changed Slots: " + click.changedSlots().size(), UiColorPalette.textBright());
         int shown = 0;

         for (Entry<HashedStack> slot : click.changedSlots().int2ObjectEntrySet()) {
            if (shown >= 16) {
               out.addSemantic("  ... +" + (click.changedSlots().size() - shown) + " more", UiColorPalette.textMuted());
               break;
            }

            out.addSemantic("  #" + slot.getIntKey() + " -> " + PacketStateDeltaTracker.describeHashedStack(slot.getValue()), UiColorPalette.textBody());
            shown++;
         }

         out.addSemantic("Carried Item: " + PacketStateDeltaTracker.describeHashedStack(click.carriedItem()), UiColorPalette.successText());
      } else if (packet instanceof ServerboundContainerButtonClickPacket button) {
         out.addSemantic("Client presses a handled-screen button.", UiColorPalette.textBright());
         out.addSemantic("Container: #" + describeValue(callFirst(button, "containerId", "getContainerId", "getSyncId")), UiColorPalette.blue());
         out.addSemantic("Button Id: " + describeValue(callFirst(button, "buttonId", "getButtonId")), UiColorPalette.textBright());
      } else if (packet instanceof ServerboundContainerClosePacket || packet instanceof ClientboundContainerClosePacket) {
         out.addSemantic(packet instanceof ServerboundContainerClosePacket ? "Client closes a handled screen." : "Server closes a handled screen.", UiColorPalette.textBright());
         Object id = packet instanceof ClientboundContainerClosePacket close
            ? close.getContainerId()
            : callFirst(packet, "containerId", "getContainerId");
         out.addSemantic("Container: #" + describeValue(id), UiColorPalette.blue());
      } else if (packet instanceof ClientboundContainerSetDataPacket data) {
         out.addSemantic("Server updates a container property/data slot.", UiColorPalette.textBright());
         out.addSemantic("Container: #" + describeValue(callFirst(data, "containerId", "getContainerId", "getSyncId")), UiColorPalette.blue());
         out.addSemantic(
            "Property: "
               + describeValue(callFirst(data, "id", "getId", "propertyId", "getPropertyId"))
               + " = "
               + describeValue(callFirst(data, "value", "getValue")),
            UiColorPalette.successText()
         );
      } else if (packet instanceof ServerboundSetCreativeModeSlotPacket creative) {
         out.addSemantic("Client sets a creative inventory slot.", UiColorPalette.textBright());
         out.addSemantic("Slot: " + describeValue(callFirst(creative, "slot", "getSlot")), UiColorPalette.blue());
         out.addSemantic("Stack: " + formatValue(callFirst(creative, "itemStack", "stack", "getItem", "getStack")), UiColorPalette.successText());
      } else if (packet instanceof ServerboundSelectBundleItemPacket bundle) {
         out.addSemantic("Client selects an item inside a bundle.", UiColorPalette.textBright());
         out.addSemantic(
            "Slot: "
               + describeValue(callFirst(bundle, "slot", "getSlot"))
               + " | Index: "
               + describeValue(callFirst(bundle, "selectedItemIndex", "index", "getIndex")),
            UiColorPalette.blue()
         );
      } else if (packet instanceof ClientboundPlayerPositionPacket position) {
         PositionMoveRotation change = position.change();
         out.addSemantic("Server sends an authoritative player position/rotation correction.", UiColorPalette.textBright());
         out.addSemantic("Teleport Id: " + position.id(), UiColorPalette.blue());
         out.addSemantic("Relative Flags: " + formatRelatives(position.relatives()), UiColorPalette.textBright());
         out.addSemantic("Encoded Position: " + formatVec(change.position()), UiColorPalette.textBright());
         out.addSemantic("Encoded Velocity Delta: " + formatVec(change.deltaMovement()), UiColorPalette.textBright());
         out.addSemantic("Encoded Rotation: yaw=" + formatFloat(change.yRot()) + ", pitch=" + formatFloat(change.xRot()), UiColorPalette.textBright());
         out.addSemantic("Expected Reply: ServerboundAcceptTeleportation #" + position.id(), UiColorPalette.amber());
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         out.addSemantic("Client sends player movement state.", UiColorPalette.textBright());
         out.addSemantic("Variant: " + move.getClass().getSimpleName().replace("ServerboundMovePlayerPacket$", ""), UiColorPalette.textBright());
         out.addSemantic("Has Position: " + move.hasPosition() + " | Has Rotation: " + move.hasRotation(), UiColorPalette.textBright());
         out.addSemantic("On Ground: " + move.isOnGround() + " | Horizontal Collision: " + move.horizontalCollision(), UiColorPalette.textBright());
         out.addSemantic("Encoded Position: x=" + move.getX(0.0) + ", y=" + move.getY(0.0) + ", z=" + move.getZ(0.0), UiColorPalette.successText());
         out.addSemantic("Encoded Rotation: yaw=" + formatFloat(move.getYRot(0.0F)) + ", pitch=" + formatFloat(move.getXRot(0.0F)), UiColorPalette.successText());
      } else if (packet instanceof ServerboundAcceptTeleportationPacket ack) {
         out.addSemantic("Client acknowledges a server teleport/correction.", UiColorPalette.textBright());
         out.addSemantic("Teleport Id: " + ack.id(), UiColorPalette.blue());
      } else if (packet instanceof ClientboundLoginPacket login) {
         out.addSemantic("Server enters the play world.", UiColorPalette.textBright());
         out.addSemantic("Player Entity Id: " + login.playerId(), UiColorPalette.blue());
         out.addSemantic("Dimension: " + login.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
         out.addSemantic("Game Mode: " + login.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
         out.addSemantic("Chunk Radius: " + login.chunkRadius() + " | Simulation Distance: " + login.simulationDistance(), UiColorPalette.textBright());
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         out.addSemantic("Server respawns player or changes dimension/world.", UiColorPalette.textBright());
         out.addSemantic("Dimension: " + respawn.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
         out.addSemantic("Game Mode: " + respawn.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
         out.addSemantic("Keep Data Mask: " + respawn.dataToKeep(), UiColorPalette.textBright());
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         out.addSemantic("Server requests client transfer.", UiColorPalette.textBright());
         out.addSemantic("Target: " + transfer.host() + ":" + transfer.port(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
         out.addSemantic("Server starts configuration phase.", UiColorPalette.textBright());
      } else if (packet instanceof ClientboundFinishConfigurationPacket) {
         out.addSemantic("Server finishes configuration phase.", UiColorPalette.textBright());
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         out.addSemantic("Server changes chunk cache center.", UiColorPalette.textBright());
         out.addSemantic("Chunk Center: " + center.getX() + ", " + center.getZ(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         out.addSemantic("Server changes chunk render/cache radius.", UiColorPalette.textBright());
         out.addSemantic("Radius: " + radius.getRadius(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetSimulationDistancePacket simulation) {
         out.addSemantic("Server changes simulation distance.", UiColorPalette.textBright());
         out.addSemantic("Simulation Distance: " + simulation.simulationDistance(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
         out.addSemantic("Client should unload a chunk.", UiColorPalette.textBright());
         out.addSemantic("Chunk: " + forget.pos().x() + ", " + forget.pos().z(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         out.addSemantic("Server sends chunk data with light data.", UiColorPalette.textBright());
         out.addSemantic("Chunk: " + chunk.x() + ", " + chunk.z(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundGameEventPacket gameEvent) {
         out.addSemantic("Server sends a game/world event.", UiColorPalette.textBright());
         out.addSemantic("Event: " + describeValue(gameEvent.getEvent()) + " | Value: " + gameEvent.getParam(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundResourcePackPushPacket pack) {
         out.addSemantic("Server requests a resource pack.", UiColorPalette.textBright());
         out.addSemantic("Id: " + pack.id(), UiColorPalette.blue());
         out.addSemantic("Url: " + pack.url(), UiColorPalette.successText());
         out.addSemantic("Required: " + pack.required() + " | Hash: " + pack.hash(), UiColorPalette.textBright());
      } else if (packet instanceof ClientboundResourcePackPopPacket pack) {
         out.addSemantic("Server removes a resource pack.", UiColorPalette.textBright());
         out.addSemantic("Id: " + pack.id().map(Object::toString).orElse("latest / top"), UiColorPalette.blue());
      } else if (packet instanceof ClientboundDisconnectPacket disconnect) {
         out.addSemantic("Server disconnects the client.", UiColorPalette.textBright());
         out.addSemantic("Reason: " + describeValue(disconnect.reason()), UiColorPalette.errorText());
      } else if (view != null && view.read()) {
         out.addSemantic("No specialist meaning layer yet; Reality shows the exact source-backed packet fields.", UiColorPalette.textMuted());
      }
   }

   private static void init_V_List_bh_StreamUtil(List<ItemStack> items, PacketDetailFormatter.AnalysisBuilder out) {
      int shown = 0;

      for (int i = 0; i < items.size(); i++) {
         ItemStack stack = items.get(i);
         if (stack != null && !stack.isEmpty()) {
            if (shown == 0) {
               out.addSemantic("Non-empty Preview:", UiColorPalette.textBright());
            }

            if (shown >= 12) {
               out.addSemantic("  ... more non-empty slots hidden", UiColorPalette.textMuted());
               return;
            }

            out.addSemantic("  #" + i + " " + PacketStateDeltaTracker.describeStack(stack), UiColorPalette.textBody());
            shown++;
         }
      }
   }

   private static boolean init_Z_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry, Packet<?> packet, PacketDetailFormatter.AnalysisBuilder out) {
      PacketStateDeltaTracker.DeltaCapture capture = entry.packetContext;
      PacketStateDeltaTracker.StateSnapshot before = capture == null ? PacketStateDeltaTracker.StateSnapshot.EMPTY : capture.before();
      PacketStateDeltaTracker.StateSnapshot after = capture == null ? PacketStateDeltaTracker.StateSnapshot.EMPTY : capture.load();
      if (packet instanceof ClientboundOpenScreenPacket open) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server opened a handled screen", UiColorPalette.textBright());
         out.addLine("Container Id: " + open.getContainerId(), UiColorPalette.blue());
         out.addLine("Screen Type: " + describeValue(open.getType()), UiColorPalette.textBright());
         out.addLine("Title: " + quote(open.getTitle() == null ? "" : open.getTitle().getString()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: server replaced full container contents", UiColorPalette.textBright());
         out.addLine("Container Id: " + content.containerId(), UiColorPalette.blue());
         out.addLine("State Id: " + content.stateId(), UiColorPalette.textBody());
         out.addLine("Slots: " + content.items().size() + " total, " + countNonEmpty(content.items()) + " non-empty", UiColorPalette.textBright());
         out.addLine("Container Area: " + init_Str_bq_CachePool(after), UiColorPalette.textBright());
         out.addLine("Cursor: " + PacketStateDeltaTracker.describeStack(content.carriedItem()), UiColorPalette.successText());
         appendItemPreview(content.items(), out);
         return true;
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: server updated one container slot", UiColorPalette.textBright());
         out.addLine("Container Id: " + slot.getContainerId(), UiColorPalette.blue());
         out.addLine("State Id: " + slot.getStateId(), UiColorPalette.textBody());
         out.addLine("Slot: " + slot.getSlot() + " (" + PacketStateDeltaTracker.describeSlot(after, slot.getSlot()) + ")", UiColorPalette.textBright());
         out.addLine("Before: " + before.itemAt(slot.getSlot()), UiColorPalette.textMuted());
         out.addLine("After: " + PacketStateDeltaTracker.describeStack(slot.getItem()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: server updated carried cursor item", UiColorPalette.textBright());
         out.addLine("Before: " + before.carriedItem(), UiColorPalette.textMuted());
         out.addLine("After: " + PacketStateDeltaTracker.describeStack(cursor.contents()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventory) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: server updated a player inventory slot", UiColorPalette.textBright());
         out.addLine("Inventory Slot: " + inventory.slot(), UiColorPalette.blue());
         out.addLine("Before: " + before.inventoryItems().getOrDefault(inventory.slot(), "unknown"), UiColorPalette.textMuted());
         out.addLine("After: " + PacketStateDeltaTracker.describeStack(inventory.contents()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: server selected held hotbar slot", UiColorPalette.textBright());
         out.addLine("Before: " + before.selectedSlot(), UiColorPalette.textMuted());
         out.addLine("After: " + held.slot(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: client selected hotbar slot", UiColorPalette.textBright());
         out.addLine("Before: " + before.selectedSlot(), UiColorPalette.textMuted());
         out.addLine("After: " + carried.getSlot(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundContainerClickPacket click) {
         init_V_ServerboundContain(click, before, after, out);
         return false;
      } else if (packet instanceof ServerboundContainerButtonClickPacket button) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: client clicked a handled-screen button", UiColorPalette.textBright());
         out.addLine("Container Id: " + callFirst(button, "containerId", "getContainerId", "getSyncId"), UiColorPalette.blue());
         out.addLine("Button Id: " + callFirst(button, "buttonId", "getButtonId"), UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ServerboundContainerClosePacket || packet instanceof ClientboundContainerClosePacket) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine(
            "Meaning: " + (packet instanceof ServerboundContainerClosePacket ? "client closed a handled screen" : "server closed a handled screen"), UiColorPalette.textBright()
         );
         Object id = packet instanceof ClientboundContainerClosePacket close
            ? close.getContainerId()
            : callFirst(packet, "containerId", "getContainerId");
         out.addLine("Container Id: " + describeValue(id), UiColorPalette.blue());
         if (!before.screenTitle().isBlank()) {
            out.addLine("Closed Screen: " + quote(before.screenTitle()), UiColorPalette.successText());
         }

         return true;
      } else if (packet instanceof ClientboundContainerSetDataPacket data) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server updated container property/data", UiColorPalette.textBright());
         out.addLine("Container Id: " + callFirst(data, "containerId", "getContainerId", "getSyncId"), UiColorPalette.blue());
         out.addLine("Property: " + callFirst(data, "id", "getId", "propertyId", "getPropertyId"), UiColorPalette.textBright());
         out.addLine("Value: " + callFirst(data, "value", "getValue"), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundSetCreativeModeSlotPacket creative) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: client set a creative inventory slot", UiColorPalette.textBright());
         out.addLine("Slot: " + callFirst(creative, "slot", "getSlot"), UiColorPalette.blue());
         out.addLine("Stack: " + formatValue(callFirst(creative, "itemStack", "stack", "getItem", "getStack")), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundSelectBundleItemPacket bundle) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: client selected an item inside a bundle", UiColorPalette.textBright());
         out.addLine("Slot: " + callFirst(bundle, "slot", "getSlot"), UiColorPalette.blue());
         out.addLine("Selected Item Index: " + callFirst(bundle, "selectedItemIndex", "index", "getIndex"), UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundPlayerPositionPacket position) {
         init_V_ClientboundPlayerP(position, before, after, out);
         return true;
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         init_V_ServerboundMovePla(move, before, after, out);
         return true;
      } else if (packet instanceof ServerboundAcceptTeleportationPacket ack) {
         out.addLine("Status: context-aware", UiColorPalette.mint());
         out.addLine("Meaning: client acknowledged server teleport/correction", UiColorPalette.textBright());
         out.addLine("Teleport Id: " + ack.id(), UiColorPalette.blue());
         if (before.sync() >= 0) {
            out.addLine("Pending Before: " + before.sync() + (before.sync() == ack.id() ? " (matched)" : " (different)"), UiColorPalette.textBright());
         }

         return true;
      } else if (packet instanceof ClientboundLoginPacket login) {
         init_V_ClientboundLoginPa_bh_StreamUtil(login, out);
         return true;
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: respawn or dimension/world change", UiColorPalette.textBright());
         out.addLine("Dimension: " + respawn.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
         out.addLine("Game Mode: " + respawn.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
         out.addLine("Previous Game Mode: " + respawn.commonPlayerSpawnInfo().previousGameType(), UiColorPalette.textBody());
         out.addLine("Keep Data Mask: " + respawn.dataToKeep(), UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server requested client transfer", UiColorPalette.textBright());
         out.addLine("Target: " + transfer.host() + ":" + transfer.port(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: play connection is switching into configuration phase", UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundFinishConfigurationPacket) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: configuration phase finished", UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server changed chunk cache center", UiColorPalette.textBright());
         out.addLine("Chunk Center: " + center.getX() + ", " + center.getZ(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server changed chunk render/cache radius", UiColorPalette.textBright());
         out.addLine("Radius: " + radius.getRadius(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetSimulationDistancePacket simulation) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server changed simulation distance", UiColorPalette.textBright());
         out.addLine("Simulation Distance: " + simulation.simulationDistance(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: client should unload a chunk", UiColorPalette.textBright());
         out.addLine("Chunk: " + forget.pos().x() + ", " + forget.pos().z(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         out.addLine("Status: fallback", UiColorPalette.warningYellow());
         out.addLine("Meaning: server sent chunk data with light data", UiColorPalette.textBright());
         out.addLine("Chunk: " + chunk.x() + ", " + chunk.z(), UiColorPalette.successText());
         out.addLine("Chunk Data: " + describeValue(chunk.chunkData()), UiColorPalette.textBody());
         out.addLine("Light Data: " + describeValue(chunk.lightData()), UiColorPalette.textBody());
         return false;
      } else if (packet instanceof ClientboundGameEventPacket gameEvent) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: game/world event", UiColorPalette.textBright());
         out.addLine("Event: " + describeValue(gameEvent.getEvent()), UiColorPalette.textBright());
         out.addLine("Value: " + gameEvent.getParam(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundResourcePackPushPacket pack) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server requested resource pack", UiColorPalette.textBright());
         out.addLine("Id: " + pack.id(), UiColorPalette.blue());
         out.addLine("Url: " + pack.url(), UiColorPalette.successText());
         out.addLine("Hash: " + pack.hash(), UiColorPalette.textBody());
         out.addLine("Required: " + pack.required(), UiColorPalette.textBright());
         pack.prompt().ifPresent(component -> out.addLine("Prompt: " + quote(component.getString()), UiColorPalette.textBright()));
         return true;
      } else if (packet instanceof ClientboundResourcePackPopPacket pack) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server removed resource pack", UiColorPalette.textBright());
         out.addLine("Id: " + pack.id().map(Object::toString).orElse("latest / top"), UiColorPalette.blue());
         return true;
      } else if (packet instanceof ClientboundDisconnectPacket disconnect) {
         out.addLine("Status: complete", UiColorPalette.mint());
         out.addLine("Meaning: server disconnected the client", UiColorPalette.textBright());
         out.addLine("Reason: " + describeValue(disconnect.reason()), UiColorPalette.errorText());
         return true;
      } else {
         return false;
      }
   }

   private static void init_V_ServerboundContain(
      ServerboundContainerClickPacket click, PacketStateDeltaTracker.StateSnapshot before, PacketStateDeltaTracker.StateSnapshot after, PacketDetailFormatter.AnalysisBuilder out
   ) {
      out.addLine("Status: context-aware", UiColorPalette.mint());
      out.addLine("Meaning: client clicked inside a handled screen", UiColorPalette.textBright());
      out.addLine("Container Id: " + click.containerId(), UiColorPalette.blue());
      out.addLine("State Id: " + click.stateId(), UiColorPalette.textBody());
      out.addLine("Input: " + describeInput(click.containerInput()) + " (" + click.containerInput() + ")", UiColorPalette.successText());
      out.addLine("Slot: " + click.slotNum() + " (" + PacketStateDeltaTracker.describeSlot(before, click.slotNum()) + ")", UiColorPalette.textBright());
      out.addLine("Button: " + describeButton(click.containerInput(), click.buttonNum()), UiColorPalette.textBright());
      out.addLine("Slot Before: " + before.itemAt(click.slotNum()), UiColorPalette.textMuted());
      out.addLine("Slot After: " + after.itemAt(click.slotNum()), UiColorPalette.successText());
      out.addLine("Cursor Before: " + before.carriedItem(), UiColorPalette.textMuted());
      out.addLine("Cursor After: " + PacketStateDeltaTracker.describeHashedStack(click.carriedItem()), UiColorPalette.successText());
      out.addLine("Changed Slots: " + click.changedSlots().size(), UiColorPalette.textBright());
      int shown = 0;

      for (Entry<HashedStack> slot : click.changedSlots().int2ObjectEntrySet()) {
         if (shown >= 16) {
            out.addLine("  ... +" + (click.changedSlots().size() - shown) + " more", UiColorPalette.textMuted());
            break;
         }

         out.addLine(
            "  #" + slot.getIntKey() + " " + PacketStateDeltaTracker.describeSlot(before, slot.getIntKey()) + " -> " + PacketStateDeltaTracker.describeHashedStack(slot.getValue()),
            UiColorPalette.textBody()
         );
         shown++;
      }
   }

   private static void init_V_ClientboundPlayerP(
      ClientboundPlayerPositionPacket position, PacketStateDeltaTracker.StateSnapshot before, PacketStateDeltaTracker.StateSnapshot after, PacketDetailFormatter.AnalysisBuilder out
   ) {
      PositionMoveRotation change = position.change();
      out.addLine("Status: context-aware", UiColorPalette.mint());
      out.addLine("Meaning: server corrected/teleported local player", UiColorPalette.textBright());
      out.addLine("Teleport Id: " + position.id(), UiColorPalette.blue());
      out.addLine("Relative Flags: " + formatRelatives(position.relatives()), UiColorPalette.textBright());
      out.addLine("Raw Position Change: " + formatVec(change.position()), UiColorPalette.textBright());
      out.addLine("Raw Delta Movement: " + formatVec(change.deltaMovement()), UiColorPalette.textBright());
      out.addLine("Raw Rotation: yaw=" + formatFloat(change.yRot()) + ", pitch=" + formatFloat(change.xRot()), UiColorPalette.textBright());
      if (before.serverPosition() != null) {
         out.addLine("Before Server Pos: " + PacketStateDeltaTracker.formatPosition(before.serverPosition()), UiColorPalette.textMuted());
      }

      if (after.serverPosition() != null) {
         out.addLine("Computed After: " + PacketStateDeltaTracker.formatPosition(after.serverPosition()), UiColorPalette.successText());
      }

      out.addLine("Ack Expected: ServerboundAcceptTeleportation #" + position.id(), UiColorPalette.amber());
   }

   private static void init_V_ServerboundMovePla(ServerboundMovePlayerPacket move, PacketStateDeltaTracker.StateSnapshot before, PacketStateDeltaTracker.StateSnapshot after, PacketDetailFormatter.AnalysisBuilder out) {
      out.addLine("Status: context-aware", UiColorPalette.mint());
      out.addLine("Meaning: client player movement update", UiColorPalette.textBright());
      out.addLine("Variant: " + move.getClass().getSimpleName().replace("ServerboundMovePlayerPacket$", ""), UiColorPalette.textBright());
      out.addLine("Has Position: " + move.hasPosition(), UiColorPalette.textBright());
      out.addLine("Has Rotation: " + move.hasRotation(), UiColorPalette.textBright());
      out.addLine("On Ground: " + move.isOnGround(), UiColorPalette.textBright());
      out.addLine("Horizontal Collision: " + move.horizontalCollision(), UiColorPalette.textBright());
      if (before.clientPosition() != null) {
         out.addLine("Before: " + PacketStateDeltaTracker.formatPosition(before.clientPosition()), UiColorPalette.textMuted());
      }

      if (after.clientPosition() != null) {
         out.addLine("After: " + PacketStateDeltaTracker.formatPosition(after.clientPosition()), UiColorPalette.successText());
         if (before.clientPosition() != null) {
            out.addLine("Distance Delta: " + String.format(Locale.ROOT, "%.4f blocks", before.clientPosition().init(after.clientPosition())), UiColorPalette.warningYellow());
         }
      }
   }

   private static void init_V_ClientboundLoginPa_bh_StreamUtil(ClientboundLoginPacket login, PacketDetailFormatter.AnalysisBuilder out) {
      out.addLine("Status: complete", UiColorPalette.mint());
      out.addLine("Meaning: entered play world", UiColorPalette.textBright());
      out.addLine("Player Entity Id: " + login.playerId(), UiColorPalette.blue());
      out.addLine("Dimension: " + login.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
      out.addLine("Game Mode: " + login.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
      out.addLine("Hardcore: " + login.hardcore(), UiColorPalette.textBright());
      out.addLine("Known Levels: " + login.levels().size(), UiColorPalette.textBright());
      out.addLine("Chunk Radius: " + login.chunkRadius(), UiColorPalette.textBright());
      out.addLine("Simulation Distance: " + login.simulationDistance(), UiColorPalette.textBright());
      out.addLine("Reduced Debug: " + login.reducedDebugInfo(), UiColorPalette.textBright());
      out.addLine("Secure Chat Enforced: " + login.enforcesSecureChat(), UiColorPalette.textBright());
   }

   private static void init_V_bq_DataUtil_bh_StreamUtil(PacketStateDeltaTracker.DeltaCapture capture, PacketDetailFormatter.AnalysisBuilder out) {
      if (capture != null && capture.captured()) {
         PacketStateDeltaTracker.StateSnapshot before = capture.before();
         PacketStateDeltaTracker.StateSnapshot after = capture.load();
         out.addSemantic("Context Status: " + (before.tracked() ? "tracked" : "started mid-stream / partial"), before.tracked() ? UiColorPalette.mint() : UiColorPalette.warningYellow());
         if (!after.protocolState().isBlank()) {
            out.addSemantic("Protocol State: " + after.protocolState(), UiColorPalette.textBright());
         }

         if (after.containerId() >= 0 || !after.screenTitle().isBlank()) {
            out.addSemantic("Screen: #" + after.containerId() + " " + quote(after.screenTitle()) + " " + after.menuType(), UiColorPalette.successText());
         }

         if (!after.dimension().isBlank()) {
            out.addSemantic("Dimension: " + after.dimension(), UiColorPalette.successText());
         }

         if (after.chunkCenterX() != null && after.chunkCenterZ() != null) {
            out.addSemantic("Chunk Center: " + after.chunkCenterX() + ", " + after.chunkCenterZ(), UiColorPalette.textBright());
         }

         if (after.chunkRadius() != null) {
            out.addSemantic("Chunk Radius: " + after.chunkRadius(), UiColorPalette.textBright());
         }

         if (after.simulationDistance() != null) {
            out.addSemantic("Simulation Distance: " + after.simulationDistance(), UiColorPalette.textBright());
         }

         if (after.sync() >= 0) {
            out.addSemantic("Pending Teleport Ack: " + after.sync(), UiColorPalette.amber());
         }

         if (!capture.changes().isEmpty()) {
            out.addSemantic("Changes:", UiColorPalette.textBright());

            for (String change : capture.changes()) {
               out.addSemantic("  - " + change, UiColorPalette.textBody());
            }
         }
      }
   }

   private static void init_V_AutismPacketLogger_bh_StreamUtil(AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketDetailFormatter.AnalysisBuilder out) {
      if (entry != null && entry.packet != null) {
         PacketWireCaptureStore.PacketCapture snapshot = PacketWireCaptureStore.captureFor(entry.packet);
         if (snapshot != null) {
            out.addWire("Protocol: " + snapshot.protocol(), UiColorPalette.textBody());
            out.addWire("Direction: " + snapshot.direction(), "C2S".equalsIgnoreCase(snapshot.direction()) ? UiColorPalette.amber() : UiColorPalette.cyan());
            if (snapshot.packetId() >= 0) {
               out.addWire("Packet ID: " + snapshot.packetId(), UiColorPalette.textBody());
            }

            if (snapshot.packetType() != null && !snapshot.packetType().isBlank()) {
               out.addWire("Packet Type: " + snapshot.packetType(), UiColorPalette.textBody());
            }

            byte[] bytes = snapshot.plaintext();
            out.addWire("Plaintext: " + bytes.length + " bytes " + PacketWireCaptureStore.hexPreview(bytes, 48), UiColorPalette.textMuted());
         }
      }
   }

   private static void appendItemPreview(List<ItemStack> items, PacketDetailFormatter.AnalysisBuilder out) {
      int shown = 0;

      for (int i = 0; i < items.size(); i++) {
         ItemStack stack = items.get(i);
         if (stack != null && !stack.isEmpty()) {
            if (shown == 0) {
               out.addLine("Non-empty Preview:", UiColorPalette.textBright());
            }

            if (shown >= 12) {
               out.addLine("  ... more non-empty slots hidden", UiColorPalette.textMuted());
               return;
            }

            out.addLine("  #" + i + " " + PacketStateDeltaTracker.describeStack(stack), UiColorPalette.textBody());
            shown++;
         }
      }
   }

   private static String init_Str_bq_CachePool(PacketStateDeltaTracker.StateSnapshot snapshot) {
      if (snapshot != null && snapshot.totalSlots() >= 0) {
         int containerSlots = Math.max(0, snapshot.containerSlots());
         int playerSlots = Math.max(0, snapshot.totalSlots() - containerSlots);
         return containerSlots + " container slots + " + playerSlots + " player slots";
      } else {
         return "unknown";
      }
   }

   private static int countNonEmpty(Collection<ItemStack> items) {
      int count = 0;
      if (items != null) {
         for (ItemStack item : items) {
            if (item != null && !item.isEmpty()) {
               count++;
            }
         }
      }

      return count;
   }

   private static String describeInput(ContainerInput input) {
      if (input == null) {
         return "unknown";
      }

      String var10000;
      switch (input) {
         case PICKUP:
            var10000 = "PICKUP left/right click pickup/place";
            break;
         case QUICK_MOVE:
            var10000 = "QUICK_MOVE shift-click transfer";
            break;
         case SWAP:
            var10000 = "SWAP hotbar/offhand swap";
            break;
         case CLONE:
            var10000 = "CLONE creative middle-click";
            break;
         case THROW:
            var10000 = "THROW drop from slot";
            break;
         case QUICK_CRAFT:
            var10000 = "QUICK_CRAFT drag distribute";
            break;
         case PICKUP_ALL:
            var10000 = "PICKUP_ALL gather matching carried stack";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String describeButton(ContainerInput input, int button) {
      if (input == ContainerInput.PICKUP) {
         return button == 0 ? "0 left click" : (button == 1 ? "1 right click" : String.valueOf(button));
      } else if (input == ContainerInput.QUICK_MOVE) {
         return button == 0 ? "0 shift-left" : (button == 1 ? "1 shift-right" : String.valueOf(button));
      } else if (input == ContainerInput.SWAP) {
         return button == 40 ? "40 offhand" : button + " hotbar slot";
      } else if (input == ContainerInput.THROW) {
         return button == 0 ? "0 drop one" : (button == 1 ? "1 drop stack" : String.valueOf(button));
      } else {
         return String.valueOf(button);
      }
   }

   private static String formatValue(Object value) {
      return value instanceof ItemStack stack ? PacketStateDeltaTracker.describeStack(stack) : describeValue(value);
   }

   private static String formatRelatives(Set<Relative> relatives) {
      return relatives != null && !relatives.isEmpty() ? relatives.toString() : "none / absolute";
   }

   private static String formatVec(Vec3 vec) {
      return vec == null ? "unknown" : String.format(Locale.ROOT, "x=%.3f, y=%.3f, z=%.3f", vec.x, vec.y, vec.z);
   }

   private static String formatFloat(float value) {
      return String.format(Locale.ROOT, "%.2f", value);
   }

   private static Object callFirst(Object target, String... methodNames) {
      if (target != null && methodNames != null) {
         for (String methodName : methodNames) {
            if (methodName != null && !methodName.isBlank()) {
               try {
                  Method method = target.getClass().getMethod(methodName);
                  if (method.getParameterCount() == 0) {
                     return method.invoke(target);
                  }
               } catch (Throwable var7) {
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String describeValue(Object value) {
      if (value == null) {
         return "unknown";
      } else if (value instanceof ItemStack stack) {
         return PacketStateDeltaTracker.describeStack(stack);
      } else {
         try {
            if (value instanceof Optional<?> optional) {
               return optional.<String>map(PacketDetailFormatter::describeValue).orElse("empty");
            } else if (value instanceof Holder<?> holder) {
               return holder.unwrapKey().map(key -> key.identifier().toString()).orElse(String.valueOf(holder.value()));
            } else {
               return value instanceof Item item ? BuiltInRegistries.ITEM.getKey(item).toString() : String.valueOf(value);
            }
         } catch (Throwable ignored) {
            return value.getClass().getSimpleName();
         }
      }
   }

   private static String quote(String value) {
      if (value == null) {
         return "\"\"";
      }

      String trimmed = value.length() > 96 ? value.substring(0, 93) + "..." : value;
      return "\"" + trimmed + "\"";
   }

   private record WireDecode(Packet<?> packet, boolean decoded, String init_Str) {
      static PacketDetailFormatter.WireDecode none() {
         return new PacketDetailFormatter.WireDecode(null, false, "");
      }

      static PacketDetailFormatter.WireDecode ofDecoded(Packet<?> packet, String message) {
         return new PacketDetailFormatter.WireDecode(packet, true, message);
      }

      static PacketDetailFormatter.WireDecode ofFailure(String message) {
         return new PacketDetailFormatter.WireDecode(null, false, message);
      }

      String sourceLabel() {
         return this.decoded ? "wire-codec" : "fallback";
      }

      public String message() {
         return this.init_Str;
      }
   }

   public record PacketAnalysis(
      List<PacketInspectorEngine.ColoredLine> lines, List<PacketInspectorEngine.ColoredLine> load, List<PacketInspectorEngine.ColoredLine> read, boolean init_Z, String init_Str
   ) {
      public PacketAnalysis {
         lines = lines == null ? List.of() : List.copyOf(lines);
         load = load == null ? List.of() : List.copyOf(load);
         read = read == null ? List.of() : List.copyOf(read);
         init_Str = init_Str == null ? "fallback" : init_Str;
      }

      public boolean init_Z() {
         return !this.lines.isEmpty();
      }

      public boolean hasMeaning() {
         return !this.load.isEmpty();
      }

      public boolean hasWire() {
         return !this.read.isEmpty();
      }

      public List<PacketInspectorEngine.ColoredLine> meaningLines() {
         return this.load;
      }

      public List<PacketInspectorEngine.ColoredLine> wireLines() {
         return this.read;
      }

      public boolean write() {
         return this.init_Z;
      }
   }

   private static final class AnalysisBuilder {
      private final List<PacketInspectorEngine.ColoredLine> lines = new ArrayList<>();
      private final List<PacketInspectorEngine.ColoredLine> semanticLines = new ArrayList<>();
      private final List<PacketInspectorEngine.ColoredLine> wireLines = new ArrayList<>();

      void addLine(String text, int color) {
         this.lines.add(new PacketInspectorEngine.ColoredLine(text, color));
      }

      void addSemantic(String text, int color) {
         this.semanticLines.add(new PacketInspectorEngine.ColoredLine(text, color));
      }

      void addWire(String text, int color) {
         this.wireLines.add(new PacketInspectorEngine.ColoredLine(text, color));
      }

      boolean hasLines() {
         return !this.lines.isEmpty();
      }

      PacketDetailFormatter.PacketAnalysis build(boolean complete, String status) {
         return new PacketDetailFormatter.PacketAnalysis(this.lines, this.semanticLines, this.wireLines, complete, status);
      }
   }
}
