package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatSwingPacket;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.LastSeenMessages.Update;
import net.minecraft.network.chat.RemoteChatSession.Data;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket.Entry;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class PacketCommandBuilder {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final List<String> init_List = List.of("raw", "rawHex", "body", "bodyHex", "hex", "bytes");
   private static final List<String> load = List.of("packet", "packetHex", "full", "fullHex", "plaintext", "packetBytes");
   private static final List<String> read = List.of("base64", "b64", "bodyBase64");
   private static final List<String> write;

   private PacketCommandBuilder() {
   }

   public static PacketCommandBuilder.Inner_DataUtil init_bm_DataUtil_Str(String rawArgs) {
      List<String> tokens = init_List_Str(rawArgs == null ? "" : rawArgs);
      boolean dryRun = false;
      List<String> kept = new ArrayList<>();

      for (String token : tokens) {
         String normalized = sync(token);
         int eq = token.indexOf(61);
         String key = eq >= 0 ? sync(token.substring(0, eq)) : normalized;
         if (!key.equals("dry") && !key.equals("dryrun") && !key.equals("preview") && !key.equals("nosend")) {
            kept.add(token);
         } else {
            dryRun = eq < 0 || init_Z_Str_Str(key, token.substring(eq + 1));
         }
      }

      return new PacketCommandBuilder.Inner_DataUtil(init_Str_List(kept), dryRun);
   }

   public static PacketCommandBuilder.Inner_StreamUtil init_bm_StreamUtil_Class_Str(Class<? extends Packet<?>> packetClass, String rawArgs) {
      if (packetClass == null) {
         return PacketCommandBuilder.Inner_StreamUtil.init("Packet class is missing.");
      }

      Map<String, String> args;
      try {
         args = init_Map_Str(rawArgs);
      } catch (IllegalArgumentException e) {
         return PacketCommandBuilder.Inner_StreamUtil.init(e.getMessage());
      }

      if (!args.containsKey("help") && !args.containsKey("?")) {
         PacketCommandBuilder.Inner_StreamUtil rawCodec = init_bm_StreamUtil_Class_Map(packetClass, args);
         if (rawCodec != null) {
            return rawCodec;
         }

         PacketCommandBuilder.Inner_StreamUtil special = load(packetClass, args);
         if (special != null) {
            return special;
         }

         if (args.isEmpty()) {
            Packet<?> noArg = init_Packet_Class(packetClass);
            return noArg != null
               ? PacketCommandBuilder.Inner_StreamUtil.init(noArg, "no-arg")
               : PacketCommandBuilder.Inner_StreamUtil.init("Packet needs arguments. " + init_Str_Class(packetClass));
         }

         if (packetClass.isRecord()) {
            PacketCommandBuilder.Inner_StreamUtil record = read(packetClass, args);
            if (record != null) {
               return record;
            }
         }

         PacketCommandBuilder.Inner_StreamUtil schema = write(packetClass, args);
         return schema != null ? schema : PacketCommandBuilder.Inner_StreamUtil.init("Could not build " + read_Str(packetClass) + ". " + init_Str_Class(packetClass));
      } else {
         return PacketCommandBuilder.Inner_StreamUtil.load(init_Str_Class(packetClass));
      }
   }

   public static String init_Str_Class(Class<? extends Packet<?>> packetClass) {
      if (packetClass == null) {
         return "";
      }

      PacketSchemaRegistry.Inner_StreamUtil schema = PacketSchemaRegistry.init(packetClass);
      List<String> fields = new ArrayList<>();
      if (schema != null) {
         for (PacketSchemaRegistry.Inner_DataUtil field : schema.init_List()) {
            fields.add(field.init_Str() + "=" + init_Str_Str_Str(field.load(), field.read()) + " (" + load(field.load(), field.read()) + ")");
         }
      }

      if (fields.isEmpty()) {
         for (Constructor<?> constructor : packetClass.getDeclaredConstructors()) {
            if (constructor.getParameterCount() != 0) {
               fields.add("ctor(" + constructor.getParameterCount() + " args)");
            }
         }
      }

      String usage = fields.isEmpty() ? ".send " + read_Str(packetClass) : ".send " + read_Str(packetClass) + " " + String.join(" ", read(fields));
      StringBuilder out = new StringBuilder("Usage: ").append(usage);
      if (!fields.isEmpty()) {
         out.append("\nFields: ");
         out.append(String.join(", ", fields));
      }

      List<String> examples = read_List(packetClass);
      if (!examples.isEmpty()) {
         out.append("\nExamples: ");
         out.append(String.join(" | ", examples));
      }

      out.append("\nRaw: rawHex=<packet body>, base64=<packet body>, or packetHex=<packet id + body> decodes through Minecraft's own STREAM_CODEC.");
      out.append("\nTip: Tab completes fields and enum values. Use current for live container/player defaults where offered.");
      return out.toString();
   }

   public static CompletableFuture<Suggestions> init_CompletableFuture_Class_SuggestionsBuilder(
      Class<? extends Packet<?>> packetClass, SuggestionsBuilder builder
   ) {
      if (packetClass == null) {
         return builder.buildFuture();
      }

      String remaining = builder.getRemaining();
      int tokenStart = init_I_Str(remaining);
      String current = remaining.substring(tokenStart);
      SuggestionsBuilder tokenBuilder = builder.createOffset(builder.getStart() + tokenStart);
      int eq = current.indexOf(61);
      if (eq >= 0) {
         String field = current.substring(0, eq);
         String valuePrefix = current.substring(eq + 1);

         for (String value : init_List_Class_Str(packetClass, field)) {
            if (value.toLowerCase(Locale.ROOT).startsWith(valuePrefix.toLowerCase(Locale.ROOT))) {
               tokenBuilder.suggest(field + "=" + read(value));
            }
         }

         return tokenBuilder.buildFuture();
      } else {
         Set<String> used = init_Set_Str_I(remaining, tokenStart);
         String currentLower = current.toLowerCase(Locale.ROOT);

         for (String field : init_List_Class(packetClass)) {
            if (!used.contains(sync(field))) {
               String suggestion = field + "=" + init_Str_Class_Str(packetClass, field);
               if (suggestion.toLowerCase(Locale.ROOT).startsWith(currentLower) || field.toLowerCase(Locale.ROOT).startsWith(currentLower)) {
                  tokenBuilder.suggest(suggestion);
               }
            }
         }

         for (String control : List.of("dry", "preview", "noSend")) {
            if (control.toLowerCase(Locale.ROOT).startsWith(currentLower)) {
               tokenBuilder.suggest(control);
            }
         }

         if ("help".startsWith(currentLower)) {
            tokenBuilder.suggest("help");
         }

         return tokenBuilder.buildFuture();
      }
   }

   private static Packet<?> init_Packet_Class(Class<? extends Packet<?>> packetClass) {
      for (Constructor<?> constructor : packetClass.getDeclaredConstructors()) {
         if (constructor.getParameterCount() == 0) {
            try {
               constructor.setAccessible(true);
               return (Packet<?>)constructor.newInstance();
            } catch (Throwable var6) {
            }
         }
      }

      return null;
   }

   private static PacketCommandBuilder.Inner_StreamUtil init_bm_StreamUtil_Class_Map(Class<? extends Packet<?>> packetClass, Map<String, String> args) {
      String bodyHex = init_Str_Map_StrArr(args, init_List.toArray(new String[0]));
      String fullPacketHex = init_Str_Map_StrArr(args, load.toArray(new String[0]));
      String bodyBase64 = init_Str_Map_StrArr(args, read.toArray(new String[0]));
      int provided = (bodyHex == null ? 0 : 1) + (fullPacketHex == null ? 0 : 1) + (bodyBase64 == null ? 0 : 1);
      if (provided == 0) {
         return null;
      }

      if (provided > 1) {
         return PacketCommandBuilder.Inner_StreamUtil.init("Use only one raw packet source: rawHex, packetHex, or base64.");
      }

      try {
         init_V_Map_StrArr(args, write.toArray(new String[0]));
         boolean fullPacket = fullPacketHex != null;
         byte[] sourceBytes;
         if (bodyBase64 != null) {
            sourceBytes = Base64.getDecoder().decode(bodyBase64.trim());
         } else {
            sourceBytes = init_BArr_Str(fullPacket ? fullPacketHex : bodyHex);
         }

         Packet<?> packet = fullPacket ? PacketStreamCodecDecoder.init_bp_CachePool(packetClass, sourceBytes, null).init_Packet() : PacketStreamCodecDecoder.init_Packet(packetClass, sourceBytes, null);
         return PacketCommandBuilder.Inner_StreamUtil.init(packet, fullPacket ? "minecraft packet STREAM_CODEC" : "minecraft body STREAM_CODEC");
      } catch (IllegalArgumentException e) {
         return PacketCommandBuilder.Inner_StreamUtil.init(e.getMessage());
      } catch (Throwable t) {
         return PacketCommandBuilder.Inner_StreamUtil.init("Minecraft codec decode failed: " + init_Str_Throwable(t));
      }
   }

   private static PacketCommandBuilder.Inner_StreamUtil load(Class<? extends Packet<?>> packetClass, Map<String, String> args) {
      String name = packetClass.getName();

      try {
         if (packetClass == ServerboundContainerClosePacket.class) {
            init_V_Map_StrArr(args, "containerId", "id");
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundContainerClosePacket(init_I_Map_2(args, PacketCommandBuilder::load_I, "containerId", "id")), "typed constructor");
         }

         if (packetClass == ServerboundSetCarriedItemPacket.class) {
            init_V_Map_StrArr(args, "slot");
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundSetCarriedItemPacket(init_I_Map_2(args, PacketCommandBuilder::init_I, "slot")), "typed constructor");
         }

         if (packetClass == ServerboundSelectBundleItemPacket.class) {
            init_V_Map_StrArr(args, "slot", "selectedItemIndex", "index");
            return PacketCommandBuilder.Inner_StreamUtil.init(
               new ServerboundSelectBundleItemPacket(init_I_Map_StrArr(args, "slot"), init_I_Map_StrArr(args, "selectedItemIndex", "index")),
               "typed constructor"
            );
         }

         if (packetClass == CompatSwingPacket.class) {
            init_V_Map_StrArr(args, "hand");
            return PacketCommandBuilder.Inner_StreamUtil.init(
               new CompatSwingPacket(init_Enum_Class(InteractionHand.class, args, InteractionHand.MAIN_HAND, "hand")), "typed constructor"
            );
         }

         if (packetClass == ServerboundPunchPacket.class) {
            init_V_Map_StrArr(args);
            return PacketCommandBuilder.Inner_StreamUtil.init(ServerboundPunchPacket.INSTANCE, "typed constructor");
         }

         if (packetClass == ServerboundAttackPacket.class) {
            init_V_Map_StrArr(args, "entityId", "id", "entity");
            int entityId = init_I_Map_2(args, PacketCommandBuilder::write, "entityId", "id", "entity");
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundAttackPacket(entityId), "typed constructor");
         }

         if (packetClass == ServerboundUseItemPacket.class) {
            init_V_Map_StrArr(args, "hand", "sequence", "yRot", "xRot");
            InteractionHand hand = init_Enum_Class(InteractionHand.class, args, InteractionHand.MAIN_HAND, "hand");
            int sequence = init_I_Map(args, 0, "sequence");
            float yRot = init_F_Map(args, init_F(), "yRot", "yaw");
            float xRot = init_F_Map(args, load_F(), "xRot", "pitch");
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundUseItemPacket(hand, sequence, yRot, xRot), "typed constructor");
         }

         if (packetClass == ServerboundUseItemOnPacket.class) {
            init_V_Map_StrArr(args, "hand", "sequence", "pos", "blockPos", "x", "y", "z", "direction", "dir", "face", "hit", "hitX", "hitY", "hitZ", "inside");
            InteractionHand hand = init_Enum_Class(InteractionHand.class, args, InteractionHand.MAIN_HAND, "hand");
            BlockHitResult hitResult = init_BlockHitResult_Map(args);
            int sequence = init_I_Map(args, 0, "sequence");
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundUseItemOnPacket(hand, hitResult, sequence), "typed constructor");
         }

         if (packetClass == ServerboundClientCommandPacket.class) {
            init_V_Map_StrArr(args, "action");
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundClientCommandPacket(init_Enum_Class(Action.class, args, "action")), "typed constructor");
         }

         if (packetClass == ServerboundChatCommandPacket.class) {
            init_V_Map_StrArr(args, "command", "cmd");
            return PacketCommandBuilder.Inner_StreamUtil.init(
               new ServerboundChatCommandPacket(update(init_Str_Map_Str(args, init_Str_Map_StrArr(args, "command") != null ? "command" : "cmd"))),
               "typed constructor"
            );
         }

         if (packetClass == ServerboundChatPacket.class) {
            init_V_Map_StrArr(args, "message", "timeStamp", "timestamp", "time", "salt", "signature", "lastSeenMessages", "lastSeen");
            String message = init_Str_Map_Str(args, "message");
            Instant time = init_Instant_Map(args, Instant.now(), "timeStamp", "timestamp", "time");
            long salt = init_J_Map(args, 0L, "salt");
            MessageSignature signature = init_MessageSignature_Str(init_Str_Map_StrArr(args, "signature"));
            Update lastSeen = init_LastSeenMessages_U_Str(init_Str_Map_StrArr(args, "lastSeenMessages", "lastSeen"));
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundChatPacket(message, time, salt, Optional.ofNullable(signature), lastSeen), "typed constructor");
         }

         if (packetClass == ServerboundChatCommandSignedPacket.class) {
            init_V_Map_StrArr(args, "command", "timeStamp", "timestamp", "time", "salt", "argumentSignatures", "signatures", "lastSeenMessages", "lastSeen");
            String command = init_Str_Map_Str(args, "command");
            Instant time = init_Instant_Map(args, Instant.now(), "timeStamp", "timestamp", "time");
            long salt = init_J_Map(args, 0L, "salt");
            ArgumentSignatures signatures = init_ArgumentSignatures_Str(init_Str_Map_StrArr(args, "argumentSignatures", "signatures"));
            Update lastSeen = init_LastSeenMessages_U_Str(init_Str_Map_StrArr(args, "lastSeenMessages", "lastSeen"));
            return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundChatCommandSignedPacket(command, time, salt, signatures, lastSeen), "typed constructor");
         }

         if (packetClass == ServerboundChatSessionUpdatePacket.class) {
            init_V_Map_StrArr(args, "current", "default");
            Data data = init_RemoteChatSession_();
            if (data == null) {
               throw new IllegalArgumentException("No current local chat session is available.");
            } else {
               return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundChatSessionUpdatePacket(data), "current chat session");
            }
         } else {
            if (packetClass == ServerboundClientInformationPacket.class) {
               init_V_Map_StrArr(args, "default", "information");
               return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundClientInformationPacket(ClientInformation.createDefault()), "current client information");
            }

            if (packetClass != ServerboundCustomPayloadPacket.class) {
               if (packetClass == ServerboundCustomQueryAnswerPacket.class) {
                  init_V_Map_StrArr(args, "transactionId", "id", "payload");
                  int id = init_I_Map_StrArr(args, "transactionId", "id");
                  String payload = init_Str_Map_StrArr(args, "payload");
                  if (payload != null && !init_Z_Str(payload)) {
                     return sync(payload).equals("discarded")
                        ? PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundCustomQueryAnswerPacket(id, DiscardedQueryAnswerPayload.INSTANCE), "typed constructor")
                        : PacketCommandBuilder.Inner_StreamUtil.init("Login custom query answers support payload=empty/null/discarded only from .send.");
                  } else {
                     return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundCustomQueryAnswerPacket(id, null), "typed constructor");
                  }
               } else if (packetClass == ServerboundEditBookPacket.class) {
                  init_V_Map_StrArr(args, "slot", "pages", "page", "title");
                  int slot = init_I_Map_StrArr(args, "slot");
                  String pagesValue = init_Str_Map_StrArr(args, "pages", "page");
                  if (pagesValue == null) {
                     throw new IllegalArgumentException("Missing required argument: pages");
                  }

                  Optional<String> title = init_Optional_Str(init_Str_Map_StrArr(args, "title"));
                  return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundEditBookPacket(slot, init_List_Str(pagesValue, "\\|", 100), title), "typed constructor");
               } else {
                  if (packetClass == ServerboundSignUpdatePacket.class) {
                     init_V_Map_StrArr(
                        args, "pos", "blockPos", "x", "y", "z", "front", "isFrontText", "line0", "line1", "line2", "line3", "l0", "l1", "l2", "l3", "lines"
                     );
                     BlockPos pos = init_BlockPos_Map(args);
                     boolean front = init_Z_Map(args, true, "front", "isFrontText");
                     String[] lines = init_StrArr_Map(args);
                     return PacketCommandBuilder.Inner_StreamUtil.init(Compat263.newSignUpdatePacket(pos, front, lines[0], lines[1], lines[2], lines[3]), "typed constructor");
                  }

                  if (packetClass == ServerboundInteractPacket.class) {
                     init_V_Map_StrArr(args, "entityId", "id", "entity", "hand", "location", "pos", "x", "y", "z", "secondary", "usingSecondaryAction");
                     int entityId = init_I_Map_2(args, PacketCommandBuilder::write, "entityId", "id", "entity");
                     InteractionHand hand = init_Enum_Class(InteractionHand.class, args, InteractionHand.MAIN_HAND, "hand");
                     Vec3 location = init_Str_Map_StrArr(args, "location", "pos") != null
                        ? init_Vec3_Str(init_Str_Map_StrArr(args, "location", "pos"))
                        : init_Vec3_Vec3(new Vec3(init_D_Map(args, 0.0, "x"), init_D_Map(args, 0.0, "y"), init_D_Map(args, 0.0, "z")));
                     boolean secondary = init_Z_Map(args, false, "secondary", "usingSecondaryAction");
                     return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundInteractPacket(entityId, hand, location, secondary), "typed constructor");
                  }

                  if (packetClass == ServerboundContainerClickPacket.class) {
                     init_V_Map_StrArr(
                        args,
                        "containerId",
                        "id",
                        "stateId",
                        "slotNum",
                        "slot",
                        "buttonNum",
                        "button",
                        "containerInput",
                        "input",
                        "click",
                        "changedSlots",
                        "changed",
                        "carriedItem",
                        "carried"
                     );
                     int containerId = init_I_Map_2(args, PacketCommandBuilder::load_I, "containerId", "id");
                     int stateId = init_I_Map_2(args, PacketCommandBuilder::read_I, "stateId");
                     short slot = (short)init_I_Map_StrArr(args, "slotNum", "slot");
                     byte button = (byte)init_I_Map(args, 0, "buttonNum", "button");
                     ContainerInput input = init_Enum_Class(ContainerInput.class, args, "containerInput", "input", "click");
                     Int2ObjectMap<HashedStack> changed = init_Int2ObjectMap_Str(init_Str_Map_StrArr(args, "changedSlots", "changed"));
                     HashedStack carried = init_HashedStack_Str(init_Str_Map_StrArr(args, "carriedItem", "carried"));
                     return PacketCommandBuilder.Inner_StreamUtil.init(
                        new ServerboundContainerClickPacket(containerId, stateId, slot, button, input, changed, carried), "typed constructor"
                     );
                  }

                  if (packetClass == ServerboundContainerButtonClickPacket.class) {
                     init_V_Map_StrArr(args, "containerId", "id", "buttonId", "button");
                     return PacketCommandBuilder.Inner_StreamUtil.init(
                        new ServerboundContainerButtonClickPacket(
                           init_I_Map_2(args, PacketCommandBuilder::load_I, "containerId", "id"), init_I_Map_StrArr(args, "buttonId", "button")
                        ),
                        "typed constructor"
                     );
                  }

                  if (packetClass == ServerboundContainerSlotStateChangedPacket.class) {
                     init_V_Map_StrArr(args, "slotId", "slot", "containerId", "id", "newState", "state", "enabled");
                     return PacketCommandBuilder.Inner_StreamUtil.init(
                        new ServerboundContainerSlotStateChangedPacket(
                           init_I_Map_StrArr(args, "slotId", "slot"),
                           init_I_Map_2(args, PacketCommandBuilder::load_I, "containerId", "id"),
                           init_Z_Map(args, false, "newState", "state", "enabled")
                        ),
                        "typed constructor"
                     );
                  }

                  if (packetClass == ServerboundSetCreativeModeSlotPacket.class) {
                     init_V_Map_StrArr(args, "slotNum", "slot", "itemStack", "item");
                     int slot = init_I_Map_StrArr(args, "slotNum", "slot");
                     ItemStack stack = init_ItemStack_Str(init_Str_Map_StrArr(args, "itemStack", "item"));
                     return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundSetCreativeModeSlotPacket(slot, stack), "typed constructor");
                  }

                  if (packetClass == ServerboundPlayerCommandPacket.class) {
                     init_V_Map_StrArr(args, "action", "data");
                     if (init_Minecraft.player == null) {
                        throw new IllegalArgumentException("Player is required for this packet.");
                     }

                     net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action action = init_Enum_Class(
                        net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.class, args, "action"
                     );
                     int data = init_I_Map(args, 0, "data");
                     return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundPlayerCommandPacket(init_Minecraft.player, action, data), "current player constructor");
                  } else {
                     if (packetClass == ServerboundPlayerInputPacket.class) {
                        init_V_Map_StrArr(args, "forward", "backward", "left", "right", "jump", "shift", "sneak", "sprint", "empty");
                        return init_Z_Map(args, false, "empty")
                           ? PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundPlayerInputPacket(Input.EMPTY), "typed constructor")
                           : PacketCommandBuilder.Inner_StreamUtil.init(
                              new ServerboundPlayerInputPacket(
                                 new Input(
                                    init_Z_Map(args, false, "forward"),
                                    init_Z_Map(args, false, "backward"),
                                    init_Z_Map(args, false, "left"),
                                    init_Z_Map(args, false, "right"),
                                    init_Z_Map(args, false, "jump"),
                                    init_Z_Map(args, false, "shift", "sneak"),
                                    init_Z_Map(args, false, "sprint")
                                 )
                              ),
                              "typed constructor"
                           );
                     }

                     if (packetClass == ServerboundPaddleBoatPacket.class) {
                        init_V_Map_StrArr(args, "left", "right");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new ServerboundPaddleBoatPacket(init_Z_Map(args, false, "left"), init_Z_Map(args, false, "right")), "typed constructor"
                        );
                     }

                     if (packetClass == ServerboundSetCommandBlockPacket.class) {
                        init_V_Map_StrArr(args, "pos", "blockPos", "x", "y", "z", "command", "mode", "trackOutput", "conditional", "automatic");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new ServerboundSetCommandBlockPacket(
                              init_BlockPos_Map(args),
                              init_Str_Map_Str(args, "command"),
                              init_Enum_Class(Mode.class, args, "mode"),
                              init_Z_Map(args, false, "trackOutput"),
                              init_Z_Map(args, false, "conditional"),
                              init_Z_Map(args, false, "automatic")
                           ),
                           "typed constructor"
                        );
                     }

                     if (packetClass == ServerboundSetGameRulePacket.class) {
                        init_V_Map_StrArr(args, "entries", "rules", "rule", "value");
                        return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundSetGameRulePacket(init_List_Map(args)), "typed constructor");
                     }

                     if (packetClass == ServerboundSeenAdvancementsPacket.class) {
                        init_V_Map_StrArr(args, "action", "tab");
                        net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket.Action action = init_Enum_Class(
                           net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket.Action.class, args, "action"
                        );
                        Identifier tab = action == net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket.Action.OPENED_TAB
                           ? Identifier.parse(init_Str_Map_Str(args, "tab"))
                           : null;
                        return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundSeenAdvancementsPacket(action, tab), "typed constructor");
                     }

                     if (packetClass == ServerboundTestInstanceBlockActionPacket.class) {
                        init_V_Map_StrArr(args, "pos", "blockPos", "x", "y", "z", "action", "size", "rotation", "ignoreEntities", "ignore");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new ServerboundTestInstanceBlockActionPacket(
                              init_BlockPos_Map(args),
                              init_Enum_Class(net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.class, args, "action"),
                              Optional.empty(),
                              init_Vec3i_Map(args, Vec3i.ZERO, "size"),
                              init_Enum_Class(Rotation.class, args, Rotation.NONE, "rotation"),
                              init_Z_Map(args, false, "ignoreEntities", "ignore")
                           ),
                           "typed constructor"
                        );
                     }

                     if (packetClass == ServerboundPlayerActionPacket.class) {
                        init_V_Map_StrArr(args, "action", "pos", "x", "y", "z", "direction", "sequence");
                        net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action action = init_Enum_Class(
                           net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.class, args, "action"
                        );
                        BlockPos pos = init_BlockPos_Map(args);
                        Direction direction = init_Enum_Class(Direction.class, args, "direction", "dir", "face");
                        int sequence = init_I_Map(args, 0, "sequence");
                        return PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundPlayerActionPacket(action, pos, direction, sequence), "typed constructor");
                     }

                     if (packetClass == Pos.class || name.endsWith("ServerboundMovePlayerPacket$Pos")) {
                        init_V_Map_StrArr(args, "x", "y", "z", "onGround", "ground", "horizontalCollision", "collision");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new Pos(
                              init_D_Map(args, init_D(), "x"),
                              init_D_Map(args, load_D(), "y"),
                              init_D_Map(args, read_D(), "z"),
                              init_Z_Map(args, init_Z(), "onGround", "ground"),
                              init_Z_Map(args, load_Z(), "horizontalCollision", "collision")
                           ),
                           "typed constructor"
                        );
                     }

                     if (packetClass == PosRot.class || name.endsWith("ServerboundMovePlayerPacket$PosRot")) {
                        init_V_Map_StrArr(args, "x", "y", "z", "yRot", "yaw", "xRot", "pitch", "onGround", "ground", "horizontalCollision", "collision");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new PosRot(
                              init_D_Map(args, init_D(), "x"),
                              init_D_Map(args, load_D(), "y"),
                              init_D_Map(args, read_D(), "z"),
                              init_F_Map(args, init_F(), "yRot", "yaw"),
                              init_F_Map(args, load_F(), "xRot", "pitch"),
                              init_Z_Map(args, init_Z(), "onGround", "ground"),
                              init_Z_Map(args, load_Z(), "horizontalCollision", "collision")
                           ),
                           "typed constructor"
                        );
                     }

                     if (packetClass == Rot.class || name.endsWith("ServerboundMovePlayerPacket$Rot")) {
                        init_V_Map_StrArr(args, "yRot", "yaw", "xRot", "pitch", "onGround", "ground", "horizontalCollision", "collision");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new Rot(
                              init_F_Map(args, init_F(), "yRot", "yaw"),
                              init_F_Map(args, load_F(), "xRot", "pitch"),
                              init_Z_Map(args, init_Z(), "onGround", "ground"),
                              init_Z_Map(args, load_Z(), "horizontalCollision", "collision")
                           ),
                           "typed constructor"
                        );
                     }

                     if (packetClass == StatusOnly.class || name.endsWith("ServerboundMovePlayerPacket$StatusOnly")) {
                        init_V_Map_StrArr(args, "onGround", "ground", "horizontalCollision", "collision");
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           new StatusOnly(init_Z_Map(args, init_Z(), "onGround", "ground"), init_Z_Map(args, load_Z(), "horizontalCollision", "collision")),
                           "typed constructor"
                        );
                     }

                     if (packetClass == ServerboundMoveVehiclePacket.class) {
                        init_V_Map_StrArr(args, "position", "pos", "x", "y", "z", "yRot", "yaw", "xRot", "pitch", "onGround", "ground");
                        if (args.isEmpty() && init_Minecraft.player != null && init_Minecraft.player.getVehicle() != null) {
                           return PacketCommandBuilder.Inner_StreamUtil.init(ServerboundMoveVehiclePacket.fromEntity(init_Minecraft.player.getVehicle()), "current vehicle");
                        }

                        Vec3 position = init_Str_Map_StrArr(args, "position", "pos") != null
                           ? init_Vec3_Str(init_Str_Map_StrArr(args, "position", "pos"))
                           : new Vec3(init_D_Map(args, init_D(), "x"), init_D_Map(args, load_D(), "y"), init_D_Map(args, read_D(), "z"));
                        return PacketCommandBuilder.Inner_StreamUtil.init(
                           Compat263.newServerboundMoveVehiclePacket(
                              position,
                              init_F_Map(args, init_F(), "yRot", "yaw"),
                              init_F_Map(args, load_F(), "xRot", "pitch"),
                              init_Z_Map(args, init_Z(), "onGround", "ground")
                           ),
                           "typed constructor"
                        );
                     } else {
                        return null;
                     }
                  }
               }
            } else {
               init_V_Map_StrArr(args, "brand", "payload", "channel");
               String channel = init_Str_Map_StrArr(args, "channel");
               String brand = init_Str_Map_StrArr(args, "brand", "payload");
               return brand != null && (channel == null || write(channel).equals("minecraft:brand") || write(channel).equals("brand"))
                  ? PacketCommandBuilder.Inner_StreamUtil.init(new ServerboundCustomPayloadPacket(new BrandPayload(brand)), "brand payload")
                  : PacketCommandBuilder.Inner_StreamUtil.init("Use Packet Logger > Payload Sender for arbitrary plugin payload bytes. .send can build minecraft:brand only.");
            }
         }
      } catch (IllegalArgumentException e) {
         return PacketCommandBuilder.Inner_StreamUtil.init(e.getMessage());
      } catch (Throwable t) {
         return PacketCommandBuilder.Inner_StreamUtil.init("Special builder failed: " + init_Str_Throwable(t));
      }
   }

   private static PacketCommandBuilder.Inner_StreamUtil read(Class<? extends Packet<?>> packetClass, Map<String, String> args) {
      RecordComponent[] components = packetClass.getRecordComponents();
      if (components != null && components.length != 0) {
         Constructor<?> constructor;
         try {
            Class<?>[] types = new Class[components.length];

            for (int i = 0; i < components.length; i++) {
               types[i] = components[i].getType();
            }

            constructor = packetClass.getDeclaredConstructor(types);
         } catch (Throwable ignored) {
            return null;
         }

         try {
            Object[] values = new Object[components.length];

            for (int i = 0; i < components.length; i++) {
               values[i] = init_Obj_Map(args, components[i].getName(), components[i].getType(), components[i].getGenericType());
            }

            init_V_Map_List(args, load(init_List_RecordComponentArr(components)));
            constructor.setAccessible(true);
            return PacketCommandBuilder.Inner_StreamUtil.init((Packet<?>)constructor.newInstance(values), "record constructor");
         } catch (IllegalArgumentException e) {
            return PacketCommandBuilder.Inner_StreamUtil.init(e.getMessage());
         } catch (Throwable t) {
            return PacketCommandBuilder.Inner_StreamUtil.init("Record builder failed: " + init_Str_Throwable(t));
         }
      } else {
         return null;
      }
   }

   private static PacketCommandBuilder.Inner_StreamUtil write(Class<? extends Packet<?>> packetClass, Map<String, String> args) {
      PacketSchemaRegistry.Inner_StreamUtil schema = PacketSchemaRegistry.init(packetClass);
      if (schema != null && !schema.init_List().isEmpty()) {
         String firstError = null;

         for (Constructor<?> constructor : packetClass.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == schema.init_List().size() && !init_Z_Constructor(constructor)) {
               try {
                  Object[] values = new Object[schema.init_List().size()];
                  Class<?>[] parameterTypes = constructor.getParameterTypes();
                  List<String> names = new ArrayList<>();

                  for (int i = 0; i < schema.init_List().size(); i++) {
                     String fieldName = schema.init_List().get(i).init_Str();
                     names.add(fieldName);
                     values[i] = init_Obj_Map(args, fieldName, parameterTypes[i], parameterTypes[i]);
                  }

                  init_V_Map_List(args, load(names));
                  constructor.setAccessible(true);
                  return PacketCommandBuilder.Inner_StreamUtil.init((Packet<?>)constructor.newInstance(values), "schema constructor");
               } catch (IllegalArgumentException e) {
                  if (firstError == null) {
                     firstError = e.getMessage();
                  }
               } catch (Throwable var14) {
               }
            }
         }

         return firstError == null ? null : PacketCommandBuilder.Inner_StreamUtil.init(firstError);
      } else {
         return null;
      }
   }

   private static boolean init_Z_Constructor(Constructor<?> constructor) {
      for (Class<?> type : constructor.getParameterTypes()) {
         String name = type.getName();
         if (name.contains("ByteBuf")) {
            return true;
         }
      }

      return false;
   }

   private static Map<String, String> init_Map_Str(String rawArgs) {
      Map<String, String> out = new LinkedHashMap<>();
      if (rawArgs != null && !rawArgs.isBlank()) {
         for (String token : init_List_Str(rawArgs)) {
            if (!token.isBlank()) {
               int eq = token.indexOf(61);
               if (eq < 0) {
                  String key = sync(token);
                  out.put(key, "true");
               } else {
                  String key = sync(token.substring(0, eq));
                  String value = token.substring(eq + 1);
                  if (key.isBlank()) {
                     throw new IllegalArgumentException("Blank argument name in: " + token);
                  }

                  out.put(key, value);
               }
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private static List<String> init_List_Str(String text) {
      List<String> out = new ArrayList<>();
      StringBuilder token = new StringBuilder();
      boolean quoted = false;
      char quote = 0;
      boolean escaped = false;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (escaped) {
            token.append(c);
            escaped = false;
         } else if (c == '\\') {
            escaped = true;
         } else if (quoted) {
            if (c == quote) {
               quoted = false;
            } else {
               token.append(c);
            }
         } else if (c == '"' || c == '\'') {
            quoted = true;
            quote = c;
         } else if (Character.isWhitespace(c)) {
            if (!token.isEmpty()) {
               out.add(token.toString());
               token.setLength(0);
            }
         } else {
            token.append(c);
         }
      }

      if (quoted) {
         throw new IllegalArgumentException("Unclosed quote in arguments.");
      }

      if (!token.isEmpty()) {
         out.add(token.toString());
      }

      return out;
   }

   private static String init_Str_List(List<String> tokens) {
      List<String> out = new ArrayList<>();

      for (String token : tokens) {
         out.add(init_Str_Str(token));
      }

      return String.join(" ", out);
   }

   private static String init_Str_Str(String token) {
      if (token == null) {
         return "";
      }

      boolean needsQuote = token.isBlank();

      for (int i = 0; i < token.length() && !needsQuote; i++) {
         char c = token.charAt(i);
         needsQuote = Character.isWhitespace(c) || c == '"' || c == '\'';
      }

      return !needsQuote ? token : "\"" + token.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
   }

   private static int init_I_Str(String text) {
      boolean quoted = false;
      char quote = 0;
      boolean escaped = false;
      int start = 0;

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (escaped) {
            escaped = false;
         } else if (c == '\\') {
            escaped = true;
         } else if (quoted) {
            if (c == quote) {
               quoted = false;
            }
         } else if (c == '"' || c == '\'') {
            quoted = true;
            quote = c;
         } else if (Character.isWhitespace(c)) {
            start = i + 1;
         }
      }

      return start;
   }

   private static Set<String> init_Set_Str_I(String text, int tokenStart) {
      Set<String> out = new HashSet<>();
      String prefix = tokenStart <= 0 ? "" : text.substring(0, tokenStart);

      try {
         out.addAll(init_Map_Str(prefix).keySet());
      } catch (IllegalArgumentException var5) {
      }

      return out;
   }

   private static List<String> init_List_Class(Class<? extends Packet<?>> packetClass) {
      List<String> fields = load_List(packetClass);
      if (!fields.isEmpty()) {
         return init_List_List(fields);
      }

      PacketSchemaRegistry.Inner_StreamUtil schema = PacketSchemaRegistry.init(packetClass);
      if (schema != null && !schema.init_List().isEmpty()) {
         List<String> out = new ArrayList<>();

         for (PacketSchemaRegistry.Inner_DataUtil field : schema.init_List()) {
            out.add(field.init_Str());
         }

         return init_List_List(out);
      } else {
         return init_List_List(List.of("help"));
      }
   }

   private static List<String> init_List_List(List<String> fields) {
      List<String> out = new ArrayList<>(fields);
      out.add("rawHex");
      out.add("packetHex");
      out.add("base64");
      return out;
   }

   private static List<String> load_List(Class<? extends Packet<?>> packetClass) {
      String name = packetClass.getName();
      if (packetClass == ServerboundUseItemOnPacket.class) {
         return List.of("hand", "pos", "direction", "sequence", "hit", "inside");
      } else if (packetClass == ServerboundContainerClickPacket.class) {
         return List.of("slot", "input", "button", "containerId", "stateId", "changed", "carried");
      } else if (packetClass == ServerboundSignUpdatePacket.class) {
         return List.of("pos", "front", "lines", "line0", "line1", "line2", "line3");
      } else if (packetClass == ServerboundEditBookPacket.class) {
         return List.of("slot", "pages", "title");
      } else if (packetClass == ServerboundSetGameRulePacket.class) {
         return List.of("rule", "value", "entries");
      } else if (packetClass == ServerboundSetCreativeModeSlotPacket.class) {
         return List.of("slot", "item");
      } else if (packetClass == ServerboundPlayerInputPacket.class) {
         return List.of("forward", "backward", "left", "right", "jump", "shift", "sprint");
      } else if (packetClass == ServerboundInteractPacket.class) {
         return List.of("entityId", "hand", "location", "secondary");
      } else if (packetClass == ServerboundAttackPacket.class) {
         return List.of("entityId");
      } else if (packetClass == ServerboundTestInstanceBlockActionPacket.class) {
         return List.of("pos", "action", "size", "rotation", "ignore");
      } else if (packetClass == ServerboundChatCommandPacket.class) {
         return List.of("command");
      } else if (packetClass == ServerboundChatPacket.class) {
         return List.of("message", "time", "salt", "signature", "lastSeen");
      } else if (packetClass == ServerboundChatCommandSignedPacket.class) {
         return List.of("command", "time", "salt", "signatures", "lastSeen");
      } else if (packetClass == ServerboundCustomPayloadPacket.class) {
         return List.of("brand", "channel");
      } else if (packetClass == ServerboundCustomQueryAnswerPacket.class) {
         return List.of("transactionId", "payload");
      } else if (packetClass == Pos.class || name.endsWith("ServerboundMovePlayerPacket$Pos")) {
         return List.of("x", "y", "z", "onGround", "collision");
      } else if (packetClass == PosRot.class || name.endsWith("ServerboundMovePlayerPacket$PosRot")) {
         return List.of("x", "y", "z", "yaw", "pitch", "onGround", "collision");
      } else if (packetClass != Rot.class && !name.endsWith("ServerboundMovePlayerPacket$Rot")) {
         return packetClass != StatusOnly.class && !name.endsWith("ServerboundMovePlayerPacket$StatusOnly") ? List.of() : List.of("onGround", "collision");
      } else {
         return List.of("yaw", "pitch", "onGround", "collision");
      }
   }

   private static List<String> init_List_Class_Str(Class<? extends Packet<?>> packetClass, String rawField) {
      String field = sync(rawField);
      if (field.equals("rawhex") || field.equals("raw") || field.equals("body") || field.equals("bodyhex") || field.equals("hex") || field.equals("bytes")) {
         return List.of("hex:010203", "utf8:text");
      }

      if (field.equals("packethex")
         || field.equals("packet")
         || field.equals("full")
         || field.equals("fullhex")
         || field.equals("plaintext")
         || field.equals("packetbytes")) {
         return List.of("hex:010203");
      }

      if (!field.equals("base64") && !field.equals("b64") && !field.equals("bodybase64")) {
         Class<? extends Enum<?>> enumClass = init_Class_Class_Str(packetClass, field);
         if (enumClass != null) {
            List<String> values = new ArrayList<>();

            for (Enum<?> constant : enumClass.getEnumConstants()) {
               values.add(constant.name());
            }

            return values;
         } else if (field.equals("hand")) {
            return List.of("MAIN_HAND", "OFF_HAND");
         } else if (field.equals("direction") || field.equals("dir") || field.equals("face")) {
            return List.of("UP", "DOWN", "NORTH", "SOUTH", "WEST", "EAST");
         } else if (field.equals("input") || field.equals("click") || field.equals("containerinput")) {
            return List.of("PICKUP", "QUICK_MOVE", "SWAP", "THROW", "QUICK_CRAFT", "PICKUP_ALL");
         } else if (field.equals("containerid") || field.equals("id") || field.equals("stateid")) {
            return List.of("current", "0");
         } else if (field.equals("slot") || field.equals("slotnum") || field.equals("slotid")) {
            return List.of("current", "0");
         } else if (field.equals("x")) {
            return List.of(init_Str_D(init_D()));
         } else if (field.equals("y")) {
            return List.of(init_Str_D(load_D()));
         } else if (field.equals("z")) {
            return List.of(init_Str_D(read_D()));
         } else if (field.equals("yaw") || field.equals("yrot")) {
            return List.of(init_Str_F(init_F()));
         } else if (field.equals("pitch") || field.equals("xrot")) {
            return List.of(init_Str_F(load_F()));
         } else if (field.equals("pos") || field.equals("blockpos") || field.equals("hit") || field.equals("size")) {
            return List.of("0,64,0");
         } else if (field.equals("item") || field.equals("itemstack") || field.equals("carried")) {
            return List.of("empty", "held", "offhand", "cursor");
         } else if (field.equals("payload")) {
            return List.of("empty", "discarded");
         } else if (field.equals("time") || field.equals("timestamp") || field.equals("timestamp")) {
            return List.of("now");
         } else if (field.equals("signature") || field.equals("signatures") || field.equals("lastseen")) {
            return List.of("empty");
         } else if (field.equals("brand")) {
            return List.of("AUTISM");
         } else if (field.equals("channel")) {
            return List.of("minecraft:brand");
         } else {
            return !field.equals("command") && !field.equals("cmd") ? List.of("true", "false", "0", "empty") : List.of("help", "shop");
         }
      } else {
         return List.of("AAECAw==");
      }
   }

   private static Class<? extends Enum<?>> init_Class_Class_Str(Class<? extends Packet<?>> packetClass, String field) {
      if (field.equals("action")) {
         if (packetClass == ServerboundResourcePackPacket.class) {
            return net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.class;
         }

         if (packetClass == ServerboundClientCommandPacket.class) {
            return Action.class;
         }

         if (packetClass == ServerboundPlayerActionPacket.class) {
            return net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.class;
         }

         if (packetClass == ServerboundPlayerCommandPacket.class) {
            return net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.class;
         }

         if (packetClass == ServerboundSeenAdvancementsPacket.class) {
            return net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket.Action.class;
         }

         if (packetClass == ServerboundTestInstanceBlockActionPacket.class) {
            return net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.class;
         }
      }

      if (field.equals("mode") && packetClass == ServerboundSetCommandBlockPacket.class) {
         return Mode.class;
      }

      if (field.equals("rotation")) {
         return Rotation.class;
      }

      if (field.equals("hand")) {
         return InteractionHand.class;
      }

      if (!field.equals("direction") && !field.equals("dir") && !field.equals("face")) {
         if (!field.equals("input") && !field.equals("click") && !field.equals("containerinput")) {
            PacketSchemaRegistry.Inner_StreamUtil schema = PacketSchemaRegistry.init(packetClass);
            if (schema == null) {
               return null;
            }

            for (PacketSchemaRegistry.Inner_DataUtil schemaField : schema.init_List()) {
               if (sync(schemaField.init_Str()).equals(field)) {
                  try {
                     Class<?> cls = Class.forName(load_Str(schemaField.load()));
                     return (Class<? extends Enum<?>>)(cls.isEnum() ? cls : null);
                  } catch (Throwable ignored) {
                     return null;
                  }
               }
            }

            return null;
         } else {
            return ContainerInput.class;
         }
      } else {
         return Direction.class;
      }
   }

   private static String load_Str(String javaType) {
      if (javaType == null) {
         return "";
      }

      if (javaType.indexOf(46) >= 0) {
         return javaType;
      }

      String var10000;
      switch (javaType) {
         case "Difficulty":
            var10000 = "net.minecraft.world.Difficulty";
            break;
         case "GameType":
            var10000 = "net.minecraft.world.level.GameType";
            break;
         case "RecipeBookType":
            var10000 = "net.minecraft.world.inventory.RecipeBookType";
            break;
         case "ContainerInput":
            var10000 = "net.minecraft.world.inventory.ContainerInput";
            break;
         case "InteractionHand":
            var10000 = "net.minecraft.world.InteractionHand";
            break;
         case "Direction":
            var10000 = "net.minecraft.core.Direction";
            break;
         case "Mirror":
            var10000 = "net.minecraft.world.level.block.Mirror";
            break;
         case "Rotation":
            var10000 = "net.minecraft.world.level.block.Rotation";
            break;
         case "StructureMode":
            var10000 = "net.minecraft.world.level.block.state.properties.StructureMode";
            break;
         case "StructureBlockEntity.UpdateType":
            var10000 = "net.minecraft.world.level.block.entity.StructureBlockEntity$UpdateType";
            break;
         case "CommandBlockEntity.Mode":
            var10000 = "net.minecraft.world.level.block.entity.CommandBlockEntity$Mode";
            break;
         case "JigsawBlockEntity.JointType":
            var10000 = "net.minecraft.world.level.block.entity.JigsawBlockEntity$JointType";
            break;
         case "TestBlockMode":
            var10000 = "net.minecraft.world.level.block.state.properties.TestBlockMode";
            break;
         case "ServerboundResourcePackPacket.Action":
            var10000 = "net.minecraft.network.protocol.common.ServerboundResourcePackPacket$Action";
            break;
         case "ServerboundClientCommandPacket.Action":
            var10000 = "net.minecraft.network.protocol.game.ServerboundClientCommandPacket$Action";
            break;
         case "ServerboundPlayerActionPacket.Action":
            var10000 = "net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action";
            break;
         case "ServerboundPlayerCommandPacket.Action":
            var10000 = "net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket$Action";
            break;
         case "ServerboundSeenAdvancementsPacket.Action":
            var10000 = "net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket$Action";
            break;
         case "ServerboundTestInstanceBlockActionPacket.Action":
            var10000 = "net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket$Action";
            break;
         default:
            var10000 = javaType.replace('.', '$');
      }

      return var10000;
   }

   private static String init_Str_Class_Str(Class<? extends Packet<?>> packetClass, String field) {
      List<String> values = init_List_Class_Str(packetClass, field);
      return !values.isEmpty() ? read(values.get(0)) : "0";
   }

   private static String read(String value) {
      if (value == null) {
         return "";
      } else {
         return value.indexOf(32) >= 0 ? "\"" + value.replace("\"", "\\\"") + "\"" : value;
      }
   }

   private static List<String> read_List(Class<? extends Packet<?>> packetClass) {
      String prefix = ".send " + read_Str(packetClass) + " ";
      String name = packetClass.getName();
      if (packetClass == ServerboundUseItemPacket.class) {
         return List.of(prefix + "hand=MAIN_HAND");
      } else if (packetClass == ServerboundUseItemOnPacket.class) {
         return List.of(prefix + "hand=MAIN_HAND", prefix + "pos=0,64,0 direction=UP");
      } else if (packetClass == CompatSwingPacket.class) {
         return List.of(prefix + "hand=MAIN_HAND");
      } else if (packetClass == ServerboundPunchPacket.class) {
         return List.of(prefix.trim());
      } else if (packetClass == ServerboundAttackPacket.class) {
         return List.of(prefix + "entityId=target");
      } else if (packetClass == ServerboundContainerClickPacket.class) {
         return List.of(prefix + "slot=0 input=PICKUP", prefix + "slot=0 input=QUICK_MOVE");
      } else if (packetClass == ServerboundContainerClosePacket.class) {
         return List.of(prefix + "containerId=current");
      } else if (packetClass == ServerboundContainerButtonClickPacket.class) {
         return List.of(prefix + "button=0 containerId=current");
      } else if (packetClass == ServerboundSetCarriedItemPacket.class) {
         return List.of(prefix + "slot=current", prefix + "slot=0");
      } else if (packetClass == Pos.class || name.endsWith("ServerboundMovePlayerPacket$Pos")) {
         return List.of(prefix + "x=current y=current z=current");
      } else if (packetClass == PosRot.class || name.endsWith("ServerboundMovePlayerPacket$PosRot")) {
         return List.of(prefix + "x=current y=current z=current yaw=current pitch=current");
      } else if (packetClass == Rot.class || name.endsWith("ServerboundMovePlayerPacket$Rot")) {
         return List.of(prefix + "yaw=current pitch=current");
      } else if (packetClass == ServerboundChatCommandPacket.class) {
         return List.of(prefix + "command=shop", prefix + "command=\"/warp spawn\"");
      } else if (packetClass == ServerboundChatPacket.class) {
         return List.of(prefix + "message=\"hello\" dry");
      } else if (packetClass == ServerboundEditBookPacket.class) {
         return List.of(prefix + "slot=0 pages=\"page one|page two\" title=\"title\"");
      } else if (packetClass == ServerboundSignUpdatePacket.class) {
         return List.of(prefix + "pos=0,64,0 lines=\"one|two|three|four\"");
      } else if (packetClass == ServerboundSetCreativeModeSlotPacket.class) {
         return List.of(prefix + "slot=0 item=held", prefix + "slot=0 item=empty");
      } else if (packetClass == ServerboundSetGameRulePacket.class) {
         return List.of(prefix + "rule=doDaylightCycle value=false");
      } else if (packetClass == ServerboundCustomPayloadPacket.class) {
         return List.of(prefix + "brand=vanilla");
      } else {
         return packetClass == ServerboundMoveVehiclePacket.class
            ? List.of(prefix + "dry", prefix + "pos=current yaw=current pitch=current")
            : List.of(prefix + "dry", prefix + "rawHex=hex:00 dry", prefix + "packetHex=hex:0000 dry");
      }
   }

   private static Object init_Obj_Map(Map<String, String> args, String fieldName, Class<?> targetType, Type genericType) {
      String value = init_Str_Map_StrArr(args, init_StrArr_Str(fieldName));
      if (value == null) {
         throw new IllegalArgumentException("Missing required argument: " + fieldName);
      } else {
         return init_Obj_Str(fieldName, value, targetType, genericType);
      }
   }

   private static Object init_Obj_Str(String fieldName, String value, Class<?> targetType, Type genericType) {
      if (load_Z(value)) {
         return init_Obj_Str_Class(fieldName, targetType);
      } else if (targetType == String.class) {
         return value;
      } else if (targetType == int.class || targetType == Integer.class) {
         return init_I_Str_Str(fieldName, value);
      } else if (targetType == short.class || targetType == Short.class) {
         return (short)init_I_Str_Str(fieldName, value);
      } else if (targetType == byte.class || targetType == Byte.class) {
         return (byte)init_I_Str_Str(fieldName, value);
      } else if (targetType == long.class || targetType == Long.class) {
         return init_J_Str_Str(fieldName, value);
      } else if (targetType == float.class || targetType == Float.class) {
         return Float.parseFloat(value);
      } else if (targetType == double.class || targetType == Double.class) {
         return Double.parseDouble(value);
      } else if (targetType == boolean.class || targetType == Boolean.class) {
         return init_Z_Str_Str(fieldName, value);
      } else if (targetType.isEnum()) {
         return init_Enum_Class_Str((Class)targetType, value);
      } else if (targetType == Identifier.class) {
         return Identifier.parse(value);
      } else if (targetType == BlockPos.class) {
         return init_BlockPos_Str(value);
      } else if (targetType == Vec3i.class) {
         return init_Vec3i_Str(value);
      } else if (targetType == Vec3.class) {
         return init_Vec3_Str(value);
      } else if (targetType == UUID.class) {
         return UUID.fromString(value);
      } else if (targetType == Instant.class) {
         return init_Instant_Str(value);
      } else if (targetType == byte[].class) {
         return init_BArr_Str(value);
      } else if (targetType == String[].class) {
         return init_List_Str(value, "\\|", 64).toArray(new String[0]);
      } else if (targetType == Optional.class) {
         return init_Obj_Str(fieldName, value, genericType);
      } else if (targetType == List.class) {
         return load(fieldName, value, genericType);
      } else if (targetType == Set.class) {
         return init_Set_Str_Str(fieldName, value);
      } else if (targetType == Map.class) {
         return init_Map_Str_Str(fieldName, value);
      } else if (targetType == Int2ObjectMap.class) {
         return init_Int2ObjectMap_Str(value);
      } else if (targetType == HashedStack.class) {
         return init_HashedStack_Str(value);
      } else if (targetType == ItemStack.class) {
         return init_ItemStack_Str(value);
      } else if (targetType == Input.class) {
         return init_Input_Str(value);
      } else if (targetType == ClientInformation.class) {
         return ClientInformation.createDefault();
      } else if (targetType == RecipeDisplayId.class) {
         return new RecipeDisplayId(init_I_Str_Str(fieldName, value));
      } else if (targetType == ArgumentSignatures.class) {
         return init_ArgumentSignatures_Str(value);
      } else if (targetType == Update.class) {
         return init_LastSeenMessages_U_Str(value);
      } else if (targetType == MessageSignature.class) {
         return init_MessageSignature_Str(value);
      } else {
         throw new IllegalArgumentException(
            "Argument '"
               + fieldName
               + "' uses unsupported type "
               + targetType.getSimpleName()
               + ". Capture/resend or packet macros are still needed for complex packet objects."
         );
      }
   }

   private static Object init_Obj_Str_Class(String fieldName, Class<?> targetType) {
      String normalized = sync(fieldName);
      if (targetType == int.class || targetType == Integer.class) {
         if (normalized.equals("containerid")) {
            return load_I();
         }

         if (normalized.equals("stateid")) {
            return read_I();
         }

         if (normalized.equals("slot") || normalized.equals("slotid") || normalized.equals("slotnum")) {
            return init_I();
         }

         if ((normalized.equals("id") || normalized.equals("entityid")) && init_Minecraft != null && init_Minecraft.player != null) {
            return init_Minecraft.player.getId();
         }
      }

      if ((targetType == short.class || targetType == Short.class)
         && (normalized.equals("slot") || normalized.equals("slotid") || normalized.equals("slotnum"))) {
         return (short)init_I();
      }

      if (targetType != byte.class && targetType != Byte.class || !normalized.equals("buttonnum") && !normalized.equals("button")) {
         if (targetType == double.class || targetType == Double.class) {
            if (normalized.equals("x")) {
               return init_D();
            }

            if (normalized.equals("y")) {
               return load_D();
            }

            if (normalized.equals("z")) {
               return read_D();
            }
         }

         if (targetType == float.class || targetType == Float.class) {
            if (normalized.equals("yrot") || normalized.equals("yaw")) {
               return init_F();
            }

            if (normalized.equals("xrot") || normalized.equals("pitch")) {
               return load_F();
            }
         }

         if (targetType == boolean.class || targetType == Boolean.class) {
            if (normalized.equals("onground") || normalized.equals("ground")) {
               return init_Z();
            }

            if (normalized.equals("horizontalcollision") || normalized.equals("collision")) {
               return load_Z();
            }
         }

         if (targetType == BlockPos.class) {
            BlockHitResult hit = init_BlockHitResult();
            if (hit != null) {
               return hit.getBlockPos();
            }
         }

         if (targetType == Vec3.class) {
            if (normalized.equals("position") || normalized.equals("pos")) {
               return new Vec3(init_D(), load_D(), read_D());
            }

            BlockHitResult hit = init_BlockHitResult();
            if (hit != null) {
               return hit.getLocation();
            }
         }

         if (targetType == ItemStack.class) {
            return init_ItemStack_Str("held");
         } else if (targetType == ClientInformation.class) {
            return ClientInformation.createDefault();
         } else if (targetType == Instant.class) {
            return Instant.now();
         } else {
            throw new IllegalArgumentException("current is not available for " + fieldName + " (" + targetType.getSimpleName() + ").");
         }
      } else {
         return (byte)0;
      }
   }

   private static int init_I_Map_StrArr(Map<String, String> args, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      if (value == null) {
         throw new IllegalArgumentException("Missing required argument: " + names[0]);
      } else {
         return init_I_Str_Str(names[0], value);
      }
   }

   private static int init_I_Map(Map<String, String> args, int fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value != null && !load_Z(value) ? init_I_Str_Str(names[0], value) : fallback;
   }

   private static int init_I_Map_2(Map<String, String> args, IntSupplier fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value != null && !load_Z(value) ? init_I_Str_Str(names[0], value) : fallback.getAsInt();
   }

   private static long init_J_Map(Map<String, String> args, long fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value == null ? fallback : init_J_Str_Str(names[0], value);
   }

   private static double init_D_Map_Str(Map<String, String> args, String name) {
      String value = init_Str_Map_StrArr(args, name);
      if (value == null) {
         throw new IllegalArgumentException("Missing required argument: " + name);
      } else {
         return Double.parseDouble(value);
      }
   }

   private static double init_D_Map(Map<String, String> args, double fallback, String name) {
      String value = init_Str_Map_StrArr(args, name);
      return value != null && !load_Z(value) ? Double.parseDouble(value) : fallback;
   }

   private static float init_F_Map_StrArr(Map<String, String> args, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      if (value == null) {
         throw new IllegalArgumentException("Missing required argument: " + names[0]);
      } else {
         return Float.parseFloat(value);
      }
   }

   private static float init_F_Map(Map<String, String> args, float fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value != null && !load_Z(value) ? Float.parseFloat(value) : fallback;
   }

   private static boolean init_Z_Map(Map<String, String> args, boolean fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value != null && !load_Z(value) ? init_Z_Str_Str(names[0], value) : fallback;
   }

   private static <E extends Enum<E>> E init_Enum_Class(Class<E> enumClass, Map<String, String> args, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      if (value == null) {
         throw new IllegalArgumentException("Missing required argument: " + names[0]);
      } else {
         return init_Enum_Class_Str(enumClass, value);
      }
   }

   private static <E extends Enum<E>> E init_Enum_Class(Class<E> enumClass, Map<String, String> args, E fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value == null ? fallback : init_Enum_Class_Str(enumClass, value);
   }

   private static BlockPos init_BlockPos_Map(Map<String, String> args) {
      String packed = init_Str_Map_StrArr(args, "pos", "blockPos");
      return packed != null
         ? init_BlockPos_Str(packed)
         : new BlockPos(init_I_Map_StrArr(args, "x"), init_I_Map_StrArr(args, "y"), init_I_Map_StrArr(args, "z"));
   }

   private static BlockHitResult init_BlockHitResult_Map(Map<String, String> args) {
      BlockHitResult current = init_BlockHitResult();
      boolean hasExplicitPos = init_Str_Map_StrArr(args, "pos", "blockPos") != null
         || init_Str_Map_StrArr(args, "x") != null
         || init_Str_Map_StrArr(args, "y") != null
         || init_Str_Map_StrArr(args, "z") != null;
      boolean hasExplicitDirection = init_Str_Map_StrArr(args, "direction", "dir", "face") != null;
      boolean hasExplicitHit = init_Str_Map_StrArr(args, "hit") != null
         || init_Str_Map_StrArr(args, "hitX") != null
         || init_Str_Map_StrArr(args, "hitY") != null
         || init_Str_Map_StrArr(args, "hitZ") != null;
      if (!hasExplicitPos && !hasExplicitDirection && !hasExplicitHit && current != null) {
         return current;
      }

      BlockPos pos = hasExplicitPos ? init_BlockPos_Map(args) : (current != null ? current.getBlockPos() : null);
      if (pos == null) {
         throw new IllegalArgumentException("Missing required block position. Use pos=x,y,z or look at a block.");
      }

      Direction direction = hasExplicitDirection
         ? init_Enum_Class(Direction.class, args, "direction", "dir", "face")
         : (current != null ? current.getDirection() : Direction.UP);
      Vec3 hit = init_Vec3_Map(args, current != null ? current.getLocation() : init_Vec3_BlockPos(pos), "hit");
      boolean inside = init_Z_Map(args, current != null && current.isInside(), "inside");
      return new BlockHitResult(hit, direction, pos, inside);
   }

   private static Vec3i init_Vec3i_Map(Map<String, String> args, Vec3i fallback, String packedName) {
      String packed = init_Str_Map_StrArr(args, packedName);
      return packed == null ? fallback : init_Vec3i_Str(packed);
   }

   private static BlockPos init_BlockPos_Str(String value) {
      if (load_Z(value)) {
         BlockHitResult hit = init_BlockHitResult();
         if (hit != null) {
            return hit.getBlockPos();
         } else if (init_Minecraft != null && init_Minecraft.player != null) {
            return init_Minecraft.player.blockPosition();
         } else {
            throw new IllegalArgumentException("current BlockPos needs a player or block hit.");
         }
      } else {
         String[] parts = value.trim().split("[,;]");
         if (parts.length != 3) {
            throw new IllegalArgumentException("BlockPos must be x,y,z.");
         } else {
            return new BlockPos(init_I_Str_Str("x", parts[0].trim()), init_I_Str_Str("y", parts[1].trim()), init_I_Str_Str("z", parts[2].trim()));
         }
      }
   }

   private static Vec3 init_Vec3_Map(Map<String, String> args, Vec3 fallback, String packedName) {
      String packed = init_Str_Map_StrArr(args, packedName);
      if (packed != null) {
         return init_Vec3_Str(packed);
      } else {
         String hx = init_Str_Map_StrArr(args, packedName + "X");
         String hy = init_Str_Map_StrArr(args, packedName + "Y");
         String hz = init_Str_Map_StrArr(args, packedName + "Z");
         if (hx == null && hy == null && hz == null) {
            return fallback;
         } else if (hx != null && hy != null && hz != null) {
            return new Vec3(Double.parseDouble(hx), Double.parseDouble(hy), Double.parseDouble(hz));
         } else {
            throw new IllegalArgumentException(packedName + "X/Y/Z must all be provided.");
         }
      }
   }

   private static Vec3 init_Vec3_BlockPos(BlockPos pos) {
      return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
   }

   private static Vec3 init_Vec3_Str(String value) {
      if (load_Z(value)) {
         return new Vec3(init_D(), load_D(), read_D());
      } else {
         String[] parts = value.trim().split("[,;]");
         if (parts.length != 3) {
            throw new IllegalArgumentException("Vec3 must be x,y,z.");
         } else {
            return new Vec3(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()), Double.parseDouble(parts[2].trim()));
         }
      }
   }

   private static Vec3i init_Vec3i_Str(String value) {
      if (!load_Z(value)) {
         String[] parts = value.trim().split("[,;]");
         if (parts.length != 3) {
            throw new IllegalArgumentException("Vec3i must be x,y,z.");
         } else {
            return new Vec3i(init_I_Str_Str("x", parts[0].trim()), init_I_Str_Str("y", parts[1].trim()), init_I_Str_Str("z", parts[2].trim()));
         }
      } else {
         return init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.blockPosition() : Vec3i.ZERO;
      }
   }

   private static byte[] init_BArr_Str(String value) {
      String trimmed = value == null ? "" : value.trim();
      if (trimmed.isEmpty() || init_Z_Str(trimmed)) {
         return new byte[0];
      } else if (trimmed.regionMatches(true, 0, "base64:", 0, 7)) {
         return Base64.getDecoder().decode(trimmed.substring(7).trim());
      } else if (trimmed.regionMatches(true, 0, "hex:", 0, 4)) {
         return load_BArr(trimmed.substring(4));
      } else {
         String compactHex = trimmed.replace(" ", "").replace("_", "");
         if (compactHex.length() % 2 == 0 && compactHex.matches("(?i)[0-9a-f]+")) {
            return load_BArr(compactHex);
         } else {
            return trimmed.regionMatches(true, 0, "utf8:", 0, 5)
               ? trimmed.substring(5).getBytes(StandardCharsets.UTF_8)
               : trimmed.getBytes(StandardCharsets.UTF_8);
         }
      }
   }

   private static byte[] load_BArr(String value) {
      String compact = value.replaceAll("[^0-9A-Fa-f]", "");
      if (compact.length() % 2 != 0) {
         throw new IllegalArgumentException("Hex byte string must have an even length.");
      }

      byte[] out = new byte[compact.length() / 2];

      for (int i = 0; i < compact.length(); i += 2) {
         out[i / 2] = (byte)Integer.parseInt(compact.substring(i, i + 2), 16);
      }

      return out;
   }

   private static Object init_Obj_Str(String fieldName, String value, Type genericType) {
      if (value != null && !init_Z_Str(value)) {
         if (init_Type_Type(genericType) instanceof Class<?> innerClass) {
            return Optional.of(init_Obj_Str(fieldName, value, innerClass, innerClass));
         } else {
            throw new IllegalArgumentException("Optional argument '" + fieldName + "' only supports empty/null here.");
         }
      } else {
         return Optional.empty();
      }
   }

   private static Object load(String fieldName, String value, Type genericType) {
      if (!(init_Type_Type(genericType) instanceof Class<?> innerClass)) {
         if (init_Z_Str(value)) {
            return List.of();
         } else {
            throw new IllegalArgumentException("List argument '" + fieldName + "' needs a supported element type.");
         }
      } else {
         List<String> var8 = init_List_Str(value, "\\|", 256);
         ArrayList values = new ArrayList(var8.size());

         for (String part : var8) {
            values.add(init_Obj_Str(fieldName, part, innerClass, innerClass));
         }

         return List.copyOf(values);
      }
   }

   private static Set<?> init_Set_Str_Str(String fieldName, String value) {
      if (init_Z_Str(value)) {
         return Set.of();
      } else {
         throw new IllegalArgumentException("Set argument '" + fieldName + "' only supports empty/null from .send.");
      }
   }

   private static Map<?, ?> init_Map_Str_Str(String fieldName, String value) {
      if (init_Z_Str(value)) {
         return Map.of();
      } else {
         throw new IllegalArgumentException("Map argument '" + fieldName + "' only supports empty/null from .send.");
      }
   }

   private static Type init_Type_Type(Type genericType) {
      return genericType instanceof ParameterizedType parameterized && parameterized.getActualTypeArguments().length > 0
         ? parameterized.getActualTypeArguments()[0]
         : Object.class;
   }

   private static Int2ObjectMap<HashedStack> init_Int2ObjectMap_Str(String value) {
      Int2ObjectOpenHashMap<HashedStack> out = new Int2ObjectOpenHashMap<>();
      if (value != null && !init_Z_Str(value)) {
         for (String entry : init_List_Str(value, "\\|", 128)) {
            if (!entry.isBlank()) {
               int eq = entry.indexOf(61);
               if (eq < 0) {
                  throw new IllegalArgumentException("changedSlots entries must be slot=empty.");
               }

               int slot = init_I_Str_Str("changedSlots", entry.substring(0, eq).trim());
               HashedStack stack = init_HashedStack_Str(entry.substring(eq + 1).trim());
               out.put(slot, stack);
            }
         }

         return out;
      } else {
         return out;
      }
   }

   private static HashedStack init_HashedStack_Str(String value) {
      if (value != null && !init_Z_Str(value)) {
         throw new IllegalArgumentException("HashedStack from .send currently supports only empty/null. Capture/resend for real hashed item data.");
      } else {
         return HashedStack.EMPTY;
      }
   }

   private static ItemStack init_ItemStack_Str(String value) {
      if (value != null && !init_Z_Str(value)) {
         String normalized = sync(value);
         if (init_Minecraft.player != null) {
            if ("held".equals(normalized) || "mainhand".equals(normalized)) {
               return init_Minecraft.player.getMainHandItem().copy();
            }

            if ("offhand".equals(normalized)) {
               return init_Minecraft.player.getOffhandItem().copy();
            }

            if ("cursor".equals(normalized) && init_Minecraft.player.containerMenu != null) {
               return init_Minecraft.player.containerMenu.getCarried().copy();
            }
         }

         throw new IllegalArgumentException("ItemStack supports empty, held/mainhand, offhand, or cursor.");
      } else {
         return ItemStack.EMPTY;
      }
   }

   private static Input init_Input_Str(String value) {
      if (value != null && !init_Z_Str(value)) {
         boolean forward = false;
         boolean backward = false;
         boolean left = false;
         boolean right = false;
         boolean jump = false;
         boolean shift = false;
         boolean sprint = false;

         for (String part : value.split("[,;|+]")) {
            switch (sync(part)) {
               case "":
               case "empty":
                  break;
               case "forward":
               case "w":
                  forward = true;
                  break;
               case "backward":
               case "back":
               case "s":
                  backward = true;
                  break;
               case "left":
               case "a":
                  left = true;
                  break;
               case "right":
               case "d":
                  right = true;
                  break;
               case "jump":
               case "space":
                  jump = true;
                  break;
               case "shift":
               case "sneak":
                  shift = true;
                  break;
               case "sprint":
                  sprint = true;
                  break;
               default:
                  throw new IllegalArgumentException("Unknown input flag: " + part);
            }
         }

         return new Input(forward, backward, left, right, jump, shift, sprint);
      } else {
         return Input.EMPTY;
      }
   }

   private static String[] init_StrArr_Map(Map<String, String> args) {
      String packed = init_Str_Map_StrArr(args, "lines");
      String[] lines = new String[]{"", "", "", ""};
      if (packed != null) {
         List<String> parts = init_List_Str(packed, "\\|", 4);

         for (int i = 0; i < Math.min(4, parts.size()); i++) {
            lines[i] = parts.get(i);
         }
      }

      for (int i = 0; i < 4; i++) {
         String value = init_Str_Map_StrArr(args, "line" + i, "l" + i);
         if (value != null) {
            lines[i] = value;
         }
      }

      return lines;
   }

   private static Optional<String> init_Optional_Str(String value) {
      return value != null && !init_Z_Str(value) ? Optional.of(value) : Optional.empty();
   }

   private static List<Entry> init_List_Map(Map<String, String> args) {
      String packed = init_Str_Map_StrArr(args, "entries", "rules");
      if (packed == null) {
         String rule = init_Str_Map_StrArr(args, "rule");
         String value = init_Str_Map_StrArr(args, "value");
         if (rule == null || value == null) {
            throw new IllegalArgumentException("Provide entries=rule=value|rule2=value2 or rule=... value=...");
         }

         packed = rule + "=" + value;
      }

      List<Entry> entries = new ArrayList<>();

      for (String entry : init_List_Str(packed, "\\|", 256)) {
         int eq = entry.indexOf(61);
         if (eq < 0) {
            throw new IllegalArgumentException("Game rule entries must be rule=value.");
         }

         ResourceKey<GameRule<?>> key = ResourceKey.create(Registries.GAME_RULE, init_Identifier_Str(entry.substring(0, eq).trim()));
         entries.add(new Entry(key, entry.substring(eq + 1)));
      }

      return List.copyOf(entries);
   }

   private static Identifier init_Identifier_Str(String value) {
      return value.contains(":") ? Identifier.parse(value) : Identifier.withDefaultNamespace(value);
   }

   private static String write(String value) {
      return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
   }

   private static String init_Str_Map_Str(Map<String, String> args, String name) {
      String value = init_Str_Map_StrArr(args, name);
      if (value == null) {
         throw new IllegalArgumentException("Missing required argument: " + name);
      } else {
         return value;
      }
   }

   private static String update(String command) {
      String trimmed = command == null ? "" : command.trim();

      while (trimmed.startsWith("/")) {
         trimmed = trimmed.substring(1);
      }

      if (trimmed.isBlank()) {
         throw new IllegalArgumentException("Command cannot be blank.");
      } else {
         return trimmed;
      }
   }

   private static List<String> init_List_Str(String value, String delimiterRegex, int max) {
      if (value != null && !init_Z_Str(value)) {
         String[] parts = value.split(delimiterRegex, -1);
         if (parts.length > max) {
            throw new IllegalArgumentException("Too many list values; max " + max + ".");
         }

         List<String> out = new ArrayList<>(parts.length);

         for (String part : parts) {
            out.add(part);
         }

         return out;
      } else {
         return List.of();
      }
   }

   private static boolean init_Z_Str(String value) {
      if (value == null) {
         return true;
      }

      String normalized = value.trim().toLowerCase(Locale.ROOT);
      return normalized.isEmpty()
         || normalized.equals("empty")
         || normalized.equals("null")
         || normalized.equals("none")
         || normalized.equals("{}")
         || normalized.equals("[]");
   }

   private static boolean load_Z(String value) {
      return value != null && value.trim().equalsIgnoreCase("current");
   }

   private static int init_I_Str_Str(String name, String value) {
      try {
         return Integer.decode(value.trim());
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Invalid integer for " + name + ": " + value);
      }
   }

   private static long init_J_Str_Str(String name, String value) {
      try {
         return Long.decode(value.trim());
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Invalid long for " + name + ": " + value);
      }
   }

   private static Instant init_Instant_Map(Map<String, String> args, Instant fallback, String... names) {
      String value = init_Str_Map_StrArr(args, names);
      return value == null ? fallback : init_Instant_Str(value);
   }

   private static Instant init_Instant_Str(String value) {
      if (value != null && !value.isBlank() && !"now".equalsIgnoreCase(value.trim())) {
         String trimmed = value.trim();
         return trimmed.matches("-?\\d+") ? Instant.ofEpochMilli(Long.parseLong(trimmed)) : Instant.parse(trimmed);
      } else {
         return Instant.now();
      }
   }

   private static ArgumentSignatures init_ArgumentSignatures_Str(String value) {
      if (value != null && !init_Z_Str(value)) {
         throw new IllegalArgumentException("ArgumentSignatures supports only empty/null from .send.");
      } else {
         return ArgumentSignatures.EMPTY;
      }
   }

   private static Update init_LastSeenMessages_U_Str(String value) {
      if (value != null && !init_Z_Str(value)) {
         String[] parts = value.split("[,;]");
         if (parts.length == 1) {
            return new Update(init_I_Str_Str("lastSeenOffset", parts[0].trim()), new BitSet(20), (byte)0);
         } else {
            throw new IllegalArgumentException("lastSeen supports empty/null or an offset integer.");
         }
      } else {
         return new Update(0, new BitSet(20), (byte)0);
      }
   }

   private static MessageSignature init_MessageSignature_Str(String value) {
      if (value != null && !init_Z_Str(value)) {
         byte[] bytes = init_BArr_Str(value);
         if (bytes.length != 256) {
            throw new IllegalArgumentException("MessageSignature must be exactly 256 bytes.");
         } else {
            return new MessageSignature(bytes);
         }
      } else {
         return null;
      }
   }

   private static Data init_RemoteChatSession_() {
      if (init_Minecraft != null && init_Minecraft.getConnection() != null) {
         try {
            Field field = init_Minecraft.getConnection().getClass().getDeclaredField("chatSession");
            field.setAccessible(true);
            if (field.get(init_Minecraft.getConnection()) instanceof LocalChatSession localChatSession) {
               return localChatSession.asRemote().asData();
            }
         } catch (Throwable var7) {
         }

         for (Field field : init_Minecraft.getConnection().getClass().getDeclaredFields()) {
            try {
               if (field.getType() == LocalChatSession.class) {
                  field.setAccessible(true);
                  if (field.get(init_Minecraft.getConnection()) instanceof LocalChatSession localChatSession) {
                     return localChatSession.asRemote().asData();
                  }
               }
            } catch (Throwable var6) {
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean init_Z_Str_Str(String name, String value) {
      String normalized = value.trim().toLowerCase(Locale.ROOT);
      boolean var10000;
      switch (normalized) {
         case "true":
         case "1":
         case "yes":
         case "y":
         case "on":
            var10000 = true;
            break;
         case "false":
         case "0":
         case "no":
         case "n":
         case "off":
            var10000 = false;
            break;
         default:
            throw new IllegalArgumentException("Invalid boolean for " + name + ": " + value);
      }

      return var10000;
   }

   private static <E extends Enum<E>> E init_Enum_Class_Str(Class<E> enumClass, String value) {
      String wanted = sync(value);

      for (E constant : enumClass.getEnumConstants()) {
         if (sync(constant.name()).equals(wanted)) {
            return constant;
         }
      }

      throw new IllegalArgumentException("Invalid " + enumClass.getSimpleName() + ": " + value + ". Options: " + load_Str(enumClass));
   }

   private static String load_Str(Class<? extends Enum<?>> enumClass) {
      List<String> names = new ArrayList<>();

      for (Enum<?> constant : enumClass.getEnumConstants()) {
         names.add(constant.name());
      }

      return String.join(", ", names);
   }

   private static void init_V_Map_StrArr(Map<String, String> args, String... allowed) {
      Set<String> normalized = new HashSet<>();

      for (String name : allowed) {
         normalized.add(sync(name));
      }

      for (String key : args.keySet()) {
         if (!normalized.contains(key)) {
            throw new IllegalArgumentException("Unknown argument: " + key);
         }
      }
   }

   private static void init_V_Map_List(Map<String, String> args, List<String> allowed) {
      Set<String> normalized = new HashSet<>();

      for (String name : allowed) {
         normalized.add(sync(name));
      }

      for (String key : args.keySet()) {
         if (!normalized.contains(key)) {
            throw new IllegalArgumentException("Unknown argument: " + key);
         }
      }
   }

   private static List<String> init_List_RecordComponentArr(RecordComponent[] components) {
      List<String> names = new ArrayList<>();

      for (RecordComponent component : components) {
         names.add(component.getName());
      }

      return names;
   }

   private static List<String> load(List<String> names) {
      List<String> out = new ArrayList<>();

      for (String name : names) {
         for (String alias : init_StrArr_Str(name)) {
            out.add(alias);
         }
      }

      return out;
   }

   private static String[] init_StrArr_Str(String name) {
      String normalized = sync(name);
      String[] var10000;
      switch (normalized) {
         case "slotnum":
         case "slotid":
            var10000 = new String[]{name, "slot"};
            break;
         case "buttonnum":
         case "buttonid":
            var10000 = new String[]{name, "button"};
            break;
         case "selecteditemindex":
            var10000 = new String[]{name, "index"};
            break;
         case "containerid":
            var10000 = new String[]{name, "id"};
            break;
         case "containerinput":
            var10000 = new String[]{name, "input", "click"};
            break;
         case "transactionid":
            var10000 = new String[]{name, "id", "tx"};
            break;
         case "entityid":
            var10000 = new String[]{name, "id", "entity"};
            break;
         case "yrot":
            var10000 = new String[]{name, "yaw"};
            break;
         case "xrot":
            var10000 = new String[]{name, "pitch"};
            break;
         case "position":
         case "blockpos":
            var10000 = new String[]{name, "pos"};
            break;
         case "uuid":
            var10000 = new String[]{name, "id"};
            break;
         case "profileid":
            var10000 = new String[]{name, "uuid", "id"};
            break;
         case "isopen":
            var10000 = new String[]{name, "open"};
            break;
         case "isfiltering":
            var10000 = new String[]{name, "filtering"};
            break;
         case "isflying":
            var10000 = new String[]{name, "flying"};
            break;
         case "includedata":
            var10000 = new String[]{name, "data"};
            break;
         case "usemaxitems":
            var10000 = new String[]{name, "max"};
            break;
         case "isfronttext":
            var10000 = new String[]{name, "front"};
            break;
         case "itemstack":
            var10000 = new String[]{name, "item"};
            break;
         case "carrieditem":
            var10000 = new String[]{name, "carried"};
            break;
         case "changedslots":
            var10000 = new String[]{name, "changed"};
            break;
         case "keybytes":
            var10000 = new String[]{name, "key"};
            break;
         case "encryptedchallenge":
            var10000 = new String[]{name, "challenge"};
            break;
         default:
            var10000 = new String[]{name};
      }

      return var10000;
   }

   private static String load(Map<String, String> args, String name) {
      return args.get(sync(name));
   }

   private static String init_Str_Map_StrArr(Map<String, String> args, String... names) {
      for (String name : names) {
         String value = load(args, name);
         if (value != null) {
            return value;
         }
      }

      return null;
   }

   private static String sync(String value) {
      return value == null ? "" : value.trim().replace("-", "").replace("_", "").replace(".", "").toLowerCase(Locale.ROOT);
   }

   private static String init_Str_Str_Str(String javaType, String kind) {
      String lower = (javaType == null ? "" : javaType).toLowerCase(Locale.ROOT);
      if ("boolean".equals(kind)) {
         return "true";
      } else if ("enum".equals(kind)) {
         return "VALUE";
      } else if ("identifier".equals(kind)) {
         return "minecraft:stone";
      } else if ("string".equals(kind)) {
         return "\"text\"";
      } else if (lower.contains("blockpos")) {
         return "0,64,0";
      } else if (!lower.contains("float") && !lower.contains("double")) {
         return "number".equals(kind) ? "0" : "<" + (javaType != null && !javaType.isBlank() ? javaType : "value") + ">";
      } else {
         return "0.0";
      }
   }

   private static List<String> read(List<String> fields) {
      List<String> out = new ArrayList<>();

      for (String field : fields) {
         int typeStart = field.indexOf(" (");
         out.add(typeStart >= 0 ? field.substring(0, typeStart) : field);
      }

      return out;
   }

   private static String load(String javaType, String kind) {
      if (kind != null && !kind.isBlank() && !"object".equals(kind)) {
         return kind;
      } else if (javaType != null && !javaType.isBlank()) {
         int generic = javaType.indexOf(60);
         String base = generic >= 0 ? javaType.substring(0, generic) : javaType;
         int dot = Math.max(base.lastIndexOf(46), base.lastIndexOf(36));
         return dot >= 0 ? base.substring(dot + 1) : base;
      } else {
         return "value";
      }
   }

   private static String read_Str(Class<?> packetClass) {
      String known = PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packetClass);
      return known != null ? known : packetClass.getSimpleName().replace('$', '.');
   }

   private static float init_F() {
      return init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.getYRot() : 0.0F;
   }

   private static float load_F() {
      return init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.getXRot() : 0.0F;
   }

   private static double init_D() {
      return init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.getX() : 0.0;
   }

   private static double load_D() {
      return init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.getY() : 0.0;
   }

   private static double read_D() {
      return init_Minecraft != null && init_Minecraft.player != null ? init_Minecraft.player.getZ() : 0.0;
   }

   private static boolean init_Z() {
      return init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.player.onGround();
   }

   private static boolean load_Z() {
      return init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.player.horizontalCollision;
   }

   private static int init_I() {
      if (init_Minecraft != null && init_Minecraft.player != null) {
         return init_Minecraft.player.getInventory().getSelectedSlot();
      } else {
         throw new IllegalArgumentException("Player is required for current selected slot.");
      }
   }

   private static int load_I() {
      if (init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.player.containerMenu != null) {
         return init_Minecraft.player.containerMenu.containerId;
      } else {
         throw new IllegalArgumentException("Open/current container is required.");
      }
   }

   private static int read_I() {
      return init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.player.containerMenu != null
         ? init_Minecraft.player.containerMenu.getStateId()
         : 0;
   }

   private static int write() {
      if (init_Minecraft != null
         && init_Minecraft.hitResult instanceof EntityHitResult entityHit
         && entityHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
         return entityHit.getEntity().getId();
      } else {
         throw new IllegalArgumentException("Look at an entity or provide entityId=...");
      }
   }

   private static Vec3 init_Vec3_Vec3(Vec3 fallback) {
      return init_Minecraft != null
            && init_Minecraft.hitResult instanceof EntityHitResult entityHit
            && entityHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS
         ? entityHit.getLocation()
         : fallback;
   }

   private static BlockHitResult init_BlockHitResult() {
      return init_Minecraft != null
            && init_Minecraft.hitResult instanceof BlockHitResult blockHit
            && blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS
         ? blockHit
         : null;
   }

   private static String init_Str_D(double value) {
      return String.format(Locale.ROOT, "%.3f", value);
   }

   private static String init_Str_F(float value) {
      return String.format(Locale.ROOT, "%.2f", value);
   }

   private static String init_Str_Throwable(Throwable t) {
      if (t == null) {
         return "unknown";
      }

      String message = t.getMessage();
      return message != null && !message.isBlank() ? message : t.getClass().getSimpleName();
   }

   static {
      List<String> names = new ArrayList<>();
      names.addAll(init_List);
      names.addAll(load);
      names.addAll(read);
      write = List.copyOf(names);
   }

   public record Inner_DataUtil(String init_Str, boolean init_Z) {
   }

   public record Inner_StreamUtil(Packet<?> init_Packet, boolean init_Z, boolean load_Z, String init_Str, String load_Str) {
      static PacketCommandBuilder.Inner_StreamUtil init(Packet<?> packet, String source) {
         return new PacketCommandBuilder.Inner_StreamUtil(packet, true, false, "", source == null ? "builder" : source);
      }

      static PacketCommandBuilder.Inner_StreamUtil init(String message) {
         return new PacketCommandBuilder.Inner_StreamUtil(null, false, false, message == null ? "Packet build failed." : message, "");
      }

      static PacketCommandBuilder.Inner_StreamUtil load(String message) {
         return new PacketCommandBuilder.Inner_StreamUtil(null, false, true, message == null ? "" : message, "");
      }
   }
}
