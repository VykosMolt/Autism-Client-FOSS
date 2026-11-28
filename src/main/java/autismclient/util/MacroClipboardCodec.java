package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.util.macro.MacroAction;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MacroClipboardCodec {
   private static final int init_I = 1;
   private static final String MACRO_TYPE = "autism_macro";
   private static final String load = "autism_macro_steps";
   private static final String MACRO_LIST_TYPE = "autism_macro_list";
   private static String lastClipboard;
   private static List<AutismMacro> clipboardMacros = List.of();
   private static boolean clipboardIsCut;
   private static final long init_J = 8388608L;
   private static volatile Set<String> packetClassNames;

   public static void copyQueue(List<AutismSessionState.DelayedPacket> queue) {
      try {
         CompoundTag rootTag = new CompoundTag();
         ListTag packetList = new ListTag();

         for (AutismSessionState.DelayedPacket qp : queue) {
            CompoundTag packetTag = encode(qp);
            if (packetTag != null) {
               packetList.add(packetTag);
            }
         }

         rootTag.put("packets", packetList);
         rootTag.putInt("version", 1);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         NbtIo.writeCompressed(rootTag, outputStream);
         String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
         Minecraft.getInstance().keyboardHandler.setClipboard(base64);
         AutismClientAddon.LOG.info("[Autism] Copied {} packets to clipboard", queue.size());
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to copy packets to clipboard", e);
      }
   }

   public static boolean copyMacro(AutismMacro macro) {
      if (macro == null) {
         return false;
      }

      try {
         CompoundTag rootTag = new CompoundTag();
         rootTag.putInt("version", 1);
         rootTag.putString("type", "autism_macro");
         rootTag.put("macro", macro.toSharedTag());
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         NbtIo.writeCompressed(rootTag, outputStream);
         String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
         Minecraft.getInstance().keyboardHandler.setClipboard(base64);
         AutismClientAddon.LOG.info("[Autism] Copied macro '{}' to clipboard", macro.name);
         return true;
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to copy macro to clipboard", e);
         return false;
      }
   }

   public static boolean copyMacros(List<AutismMacro> macros, boolean cut) {
      if (macros != null && !macros.isEmpty()) {
         List<AutismMacro> live = new ArrayList<>();

         for (AutismMacro macro : macros) {
            if (macro != null) {
               live.add(macro);
            }
         }

         if (live.isEmpty()) {
            return false;
         }

         if (live.size() == 1) {
            if (!copyMacro(live.get(0))) {
               return false;
            }
         } else {
            try {
               CompoundTag rootTag = new CompoundTag();
               rootTag.putInt("version", 1);
               rootTag.putString("type", "autism_macro_list");
               ListTag macroList = new ListTag();

               for (AutismMacro macro : live) {
                  macroList.add(macro.toSharedTag());
               }

               rootTag.put("macros", macroList);
               ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
               NbtIo.writeCompressed(rootTag, outputStream);
               Minecraft.getInstance().keyboardHandler.setClipboard(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
               AutismClientAddon.LOG.info("[Autism] Copied {} macros to clipboard", macroList.size());
            } catch (Exception e) {
               AutismClientAddon.LOG.error("[Autism] Failed to copy macros to clipboard", e);
               return false;
            }
         }

         rememberClipboard(live, cut);
         return true;
      } else {
         return false;
      }
   }

   private static void rememberClipboard(List<AutismMacro> macros, boolean cut) {
      List<AutismMacro> copies = new ArrayList<>(macros.size());

      for (AutismMacro macro : macros) {
         copies.add(macro.copy());
      }

      clipboardMacros = List.copyOf(copies);
      clipboardIsCut = cut;

      try {
         lastClipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
      } catch (Exception e) {
         lastClipboard = null;
      }
   }

   public static MacroClipboardCodec.PastedMacros pasteMacros() {
      String clipboard;
      try {
         clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
      } catch (Exception e) {
         clipboard = null;
      }

      if (clipboard != null && lastClipboard != null && clipboard.equals(lastClipboard) && !clipboardMacros.isEmpty()) {
         List<AutismMacro> out = new ArrayList<>(clipboardMacros.size());

         for (AutismMacro macro : clipboardMacros) {
            AutismMacro copy = macro.copy();
            if (!clipboardIsCut) {
               copy.keyCode = -1;
            }

            out.add(copy);
         }

         clipboardIsCut = false;
         return new MacroClipboardCodec.PastedMacros(out, true);
      } else {
         List<AutismMacro> list = readClipboardMacros();
         if (!list.isEmpty()) {
            return new MacroClipboardCodec.PastedMacros(list, false);
         }

         AutismMacro single = pasteMacro();
         return new MacroClipboardCodec.PastedMacros(single == null ? List.of() : List.of(single), false);
      }
   }

   public static MacroClipboardCodec.ClipboardKind clipboardKind() {
      CompoundTag rootTag = readClipboardTag();
      if (rootTag == null) {
         return MacroClipboardCodec.ClipboardKind.NONE;
      } else {
         String type = rootTag.getStringOr("type", "");
         if (!"autism_macro".equals(type) && !"autism_macro_list".equals(type)) {
            return "autism_macro_steps".equals(type) ? MacroClipboardCodec.ClipboardKind.STEPS : MacroClipboardCodec.ClipboardKind.NONE;
         } else {
            return MacroClipboardCodec.ClipboardKind.MACROS;
         }
      }
   }

   private static CompoundTag readClipboardTag() {
      try {
         String base64 = Minecraft.getInstance().keyboardHandler.getClipboard();
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            CompoundTag rootTag = NbtIo.readCompressed(new ByteArrayInputStream(data), createNbtAccounter());
            int version = rootTag.getIntOr("version", 0);
            if (version != 1) {
               AutismClientAddon.LOG.warn("[Autism] Unsupported clipboard version: {}", version);
               return null;
            } else {
               return rootTag;
            }
         } else {
            return null;
         }
      } catch (Exception e) {
         return null;
      }
   }

   private static List<MacroAction> decodeActions(ListTag actionList) {
      List<MacroAction> actions = new ArrayList<>();
      if (actionList == null) {
         return actions;
      }

      int dropped = 0;

      for (int i = 0; i < actionList.size(); i++) {
         if (actionList.get(i) instanceof CompoundTag actionTag) {
            if (!AutismMacro.isBuiltInActionType(actionTag.getStringOr("type", ""))) {
               dropped++;
            } else {
               MacroAction action = AutismMacro.deserializeAction(actionTag);
               if (action != null) {
                  action.sanitizeForSharing();
                  actions.add(action);
               }
            }
         }
      }

      warnDropped(dropped);
      return actions;
   }

   private static AutismMacro decodeMacro(CompoundTag macroTag) {
      int dropped = AutismMacro.pruneUnknownActions(macroTag);
      AutismMacro macro = new AutismMacro().fromTag(macroTag).sanitizeForSharing();
      macro.keyCode = -1;
      warnDropped(dropped);
      return macro;
   }

   private static void warnDropped(int dropped) {
      if (dropped > 0) {
         bi.init("Removed " + dropped + " addon action" + (dropped == 1 ? "" : "s") + " from imported macro for safety", -2054854);
      }
   }

   public static AutismMacro pasteMacro() {
      CompoundTag rootTag = readClipboardTag();
      if (rootTag == null) {
         return null;
      }

      String type = rootTag.getStringOr("type", "");
      if ("autism_macro".equals(type)) {
         CompoundTag macroTag = rootTag.getCompound("macro").orElse(new CompoundTag());
         if (macroTag.isEmpty()) {
            AutismClientAddon.LOG.warn("[Autism] Macro clipboard payload was empty");
            return null;
         } else {
            return decodeMacro(macroTag);
         }
      } else if ("autism_macro_steps".equals(type)) {
         List<MacroAction> actions = decodeActions(rootTag.getList("actions").orElse(new ListTag()));
         if (actions.isEmpty()) {
            return null;
         }

         AutismMacro macro = new AutismMacro();
         macro.actions = actions;
         return macro;
      } else {
         AutismClientAddon.LOG.warn("[Autism] Clipboard data is not a Autism macro payload");
         return null;
      }
   }

   public static List<AutismMacro> readClipboardMacros() {
      List<AutismMacro> macros = new ArrayList<>();
      CompoundTag rootTag = readClipboardTag();
      if (rootTag == null) {
         return macros;
      }

      String type = rootTag.getStringOr("type", "");
      if (!"autism_macro_list".equals(type)) {
         return macros;
      }

      ListTag macroList = rootTag.getList("macros").orElse(new ListTag());

      for (int i = 0; i < macroList.size(); i++) {
         if (macroList.get(i) instanceof CompoundTag macroTag && !macroTag.isEmpty()) {
            macros.add(decodeMacro(macroTag));
         }
      }

      return macros;
   }

   private static CompoundTag decodeTag(String base64) {
      try {
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            CompoundTag rootTag = NbtIo.readCompressed(new ByteArrayInputStream(data), createNbtAccounter());
            return rootTag.getIntOr("version", 0) == 1 ? rootTag : null;
         } else {
            return null;
         }
      } catch (Exception e) {
         return null;
      }
   }

   public static String encodeMacro(AutismMacro macro) {
      if (macro == null) {
         return null;
      }

      try {
         CompoundTag rootTag = new CompoundTag();
         rootTag.putInt("version", 1);
         rootTag.putString("type", "autism_macro");
         rootTag.put("macro", macro.toSharedTag());
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         NbtIo.writeCompressed(rootTag, out);
         return Base64.getEncoder().encodeToString(out.toByteArray());
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to serialize macro to Base64", e);
         return null;
      }
   }

   public static AutismMacro init_aM(String base64) {
      CompoundTag rootTag = decodeTag(base64);
      if (rootTag == null) {
         return null;
      }

      String type = rootTag.getStringOr("type", "");
      if ("autism_macro".equals(type)) {
         CompoundTag macroTag = rootTag.getCompound("macro").orElse(new CompoundTag());
         return macroTag.isEmpty() ? null : decodeMacro(macroTag);
      }

      if ("autism_macro_steps".equals(type)) {
         List<MacroAction> actions = decodeActions(rootTag.getList("actions").orElse(new ListTag()));
         if (actions.isEmpty()) {
            return null;
         }

         AutismMacro macro = new AutismMacro();
         macro.actions = actions;
         return macro;
      } else {
         return null;
      }
   }

   public static String init_Str(String base64) {
      CompoundTag rootTag = decodeTag(base64);
      if (rootTag == null) {
         return null;
      } else {
         String type = rootTag.getStringOr("type", "");
         if (!"autism_macro".equals(type) && !"autism_macro_steps".equals(type)) {
            return !rootTag.getList("packets").isPresent() && !rootTag.contains("packet") ? null : "packets";
         } else {
            return type;
         }
      }
   }

   public static boolean copySteps(List<MacroAction> actions) {
      if (actions != null && !actions.isEmpty()) {
         try {
            CompoundTag rootTag = new CompoundTag();
            rootTag.putInt("version", 1);
            rootTag.putString("type", "autism_macro_steps");
            ListTag actionList = new ListTag();

            for (MacroAction action : actions) {
               if (action != null) {
                  MacroAction copy = AutismMacro.deserializeAction(action.toTag());
                  if (copy != null) {
                     copy.sanitizeForSharing();
                     actionList.add(copy.toTag());
                  }
               }
            }

            if (actionList.isEmpty()) {
               return false;
            }

            rootTag.put("actions", actionList);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed(rootTag, outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            Minecraft.getInstance().keyboardHandler.setClipboard(base64);
            AutismClientAddon.LOG.info("[Autism] Copied {} macro steps to clipboard", actionList.size());
            return true;
         } catch (Exception e) {
            AutismClientAddon.LOG.error("[Autism] Failed to copy macro steps to clipboard", e);
            return false;
         }
      } else {
         return false;
      }
   }

   public static List<MacroAction> pasteSteps() {
      CompoundTag rootTag = readClipboardTag();
      if (rootTag == null) {
         return null;
      }

      String type = rootTag.getStringOr("type", "");
      if ("autism_macro_steps".equals(type)) {
         List<MacroAction> actions = decodeActions(rootTag.getList("actions").orElse(new ListTag()));
         return actions.isEmpty() ? null : actions;
      }

      if ("autism_macro".equals(type)) {
         CompoundTag macroTag = rootTag.getCompound("macro").orElse(new CompoundTag());
         List<MacroAction> actions = decodeActions(macroTag.getList("actions").orElse(new ListTag()));
         return actions.isEmpty() ? null : actions;
      }

      if ("autism_macro_list".equals(type)) {
         List<MacroAction> actions = new ArrayList<>();
         ListTag macroList = rootTag.getList("macros").orElse(new ListTag());

         for (int i = 0; i < macroList.size(); i++) {
            if (macroList.get(i) instanceof CompoundTag macroTag) {
               actions.addAll(decodeActions(macroTag.getList("actions").orElse(new ListTag())));
            }
         }

         return actions.isEmpty() ? null : actions;
      } else {
         return null;
      }
   }

   public static List<AutismSessionState.DelayedPacket> readPackets() {
      try {
         String base64 = Minecraft.getInstance().keyboardHandler.getClipboard();
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            CompoundTag rootTag = NbtIo.readCompressed(inputStream, createNbtAccounter());
            int version = rootTag.getIntOr("version", 0);
            if (version != 1) {
               AutismClientAddon.LOG.error("[Autism] Unsupported clipboard format version: {}", version);
               return null;
            }

            ListTag packetList = (ListTag)rootTag.get("packets");
            List<AutismSessionState.DelayedPacket> queue = new ArrayList<>();

            for (int i = 0; i < packetList.size(); i++) {
               CompoundTag packetTag = (CompoundTag)packetList.get(i);
               AutismSessionState.DelayedPacket qp = readDelayedPacket(packetTag);
               if (qp != null) {
                  queue.add(qp);
               }
            }

            AutismClientAddon.LOG.info("[Autism] Pasted {} packets from clipboard", queue.size());
            return queue.isEmpty() ? null : queue;
         } else {
            AutismClientAddon.LOG.warn("[Autism] Clipboard is empty");
            return null;
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to paste packets from clipboard", e);
         return null;
      }
   }

   public static Packet<?> decodePacket(String base64) {
      try {
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            CompoundTag tag = NbtIo.readCompressed(inputStream, createNbtAccounter());
            if (tag.contains("packets")) {
               ListTag list = (ListTag)tag.get("packets");
               if (list != null && !list.isEmpty()) {
                  CompoundTag packetTag = (CompoundTag)list.get(0);
                  AutismSessionState.DelayedPacket qp = readDelayedPacket(packetTag);
                  return qp != null ? qp.packet : null;
               } else {
                  return null;
               }
            } else if (tag.contains("packet")) {
               AutismSessionState.DelayedPacket qp = readDelayedPacket(tag);
               return qp != null ? qp.packet : null;
            } else {
               return decodePacketFromTag(tag);
            }
         } else {
            return null;
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to deserialize packet from Base64", e);
         return null;
      }
   }

   public static CompoundTag encode(AutismSessionState.DelayedPacket qp) {
      try {
         CompoundTag tag = new CompoundTag();
         tag.putInt("delay", qp.getDelay());
         tag.putInt("id", qp.getId());
         tag.putString("replayMode", qp.getReplayMode().name());
         CompoundTag packetData = encodePacket(qp.packet);
         if (packetData != null) {
            tag.put("packet", packetData);
            return tag;
         } else {
            return null;
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to serialize queued packet", e);
         return null;
      }
   }

   public static AutismSessionState.DelayedPacket readDelayedPacket(CompoundTag tag) {
      try {
         int delay = tag.getIntOr("delay", 0);
         int id = tag.getIntOr("id", 0);
         AutismSessionState.ReplayMode replayMode = AutismSessionState.ReplayMode.REGENERATE;
         if (tag.contains("replayMode")) {
            try {
               replayMode = AutismSessionState.ReplayMode.valueOf(tag.getStringOr("replayMode", "REGENERATE"));
            } catch (IllegalArgumentException ignored) {
               replayMode = AutismSessionState.ReplayMode.REGENERATE;
            }
         }

         CompoundTag packetData = tag.getCompound("packet").orElse(new CompoundTag());
         Packet<?> packet = decodePacketFromTag(packetData);
         if (packet != null) {
            return id > 0 ? new AutismSessionState.DelayedPacket(packet, delay, id, replayMode) : new AutismSessionState.DelayedPacket(packet, delay, replayMode);
         } else {
            return null;
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to deserialize queued packet", e);
         return null;
      }
   }

   private static CompoundTag encodePacket(Packet<?> packet) {
      CompoundTag tag = new CompoundTag();
      String className = packet.getClass().getName();
      tag.putString("class", className);
      String friendlyName = PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packet.getClass());
      if (friendlyName != null) {
         tag.putString("friendlyName", friendlyName);
      }

      if (packet instanceof ServerboundCustomPayloadPacket) {
         tag.putBoolean("customPayloadPacket", true);
         AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.inspectPacket(packet, "C2S");
         if (snapshot != null) {
            tag.putString("payloadChannel", snapshot.channel());
            tag.putByteArray("payloadBytes", snapshot.rawBytes());
            return tag;
         }
      }

      CompoundTag fieldsTag = new CompoundTag();
      writeFields(packet, fieldsTag);
      tag.put("fields", fieldsTag);
      return tag;
   }

   private static Packet<?> decodePacketFromTag(CompoundTag tag) {
      try {
         if (tag.getBooleanOr("customPayloadPacket", false)) {
            String channel = tag.getStringOr("payloadChannel", "");
            byte[] bytes = tag.getByteArray("payloadBytes").orElse(new byte[0]);
            if (!channel.isBlank()) {
               return AutismPayloadSupport.buildServerboundPacket(channel, bytes);
            }
         }

         String className = tag.getStringOr("class", "");
         Class<?> packetClass = null;
         if (isKnownPacketClass(className)) {
            try {
               packetClass = Class.forName(className);
            } catch (ClassNotFoundException var5) {
            }
         }

         if (packetClass == null && tag.contains("friendlyName")) {
            packetClass = PacketTypeRegistry.resolvePacketClass(tag.getStringOr("friendlyName", ""));
         }

         if (packetClass == null) {
            AutismClientAddon.LOG.warn("[Autism] Rejected unknown/untrusted packet class: {}", className);
            return null;
         }

         CompoundTag fieldsTag = tag.getCompound("fields").orElse(new CompoundTag());
         if (packetClass.isRecord()) {
            return (Packet<?>)readRecord(packetClass, fieldsTag);
         }

         Packet<?> packet = (Packet<?>)unsafe().allocateInstance(packetClass);
         readFields(packet, fieldsTag);
         if (!PacketTypeRegistry.getC2sPackets().contains(packetClass)) {
            AutismClientAddon.LOG.warn("[Autism] Deserialized packet {} is NOT a registered C2S packet! It may be skipped when sending.", className);
         }

         return packet;
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to deserialize packet", e);
         return null;
      }
   }

   private static void writeFields(Object obj, CompoundTag fieldsTag) {
      for (Class<?> clazz = obj.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
         for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
               field.setAccessible(true);

               try {
                  Object value = field.get(obj);
                  writeValue(fieldsTag, field.getName(), value, field.getType());
               } catch (Exception e) {
                  AutismClientAddon.LOG.warn("[Autism] Failed to serialize field: {}", field.getName(), e);
               }
            }
         }
      }
   }

   private static void writeValue(CompoundTag tag, String name, Object value, Class<?> type) {
      if (value == null) {
         tag.putBoolean(name + "_null", true);
      } else {
         tag.putString(name + "_type", value.getClass().getName());
         if (value instanceof ItemStack stack) {
            if (!stack.isEmpty()) {
               CompoundTag stackNbt = new CompoundTag();

               try {
                  Tag encoded = ItemStack.CODEC.encodeStart(registries().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
                  tag.put(name, encoded);
               } catch (Throwable e) {
                  stackNbt.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                  stackNbt.putInt("count", stack.getCount());
                  tag.put(name, stackNbt);
               }
            } else {
               tag.putBoolean(name + "_empty", true);
            }
         } else if (value instanceof CompoundTag) {
            tag.put(name, (CompoundTag)value);
         } else if (value instanceof Tag) {
            tag.put(name, (Tag)value);
         } else if (value instanceof BlockPos) {
            tag.putLong(name, ((BlockPos)value).asLong());
         } else if (value instanceof Identifier) {
            tag.putString(name, value.toString());
         } else if (value instanceof Component) {
            tag.putString(name, ((Component)value).getString());
         } else if (value instanceof String) {
            tag.putString(name, (String)value);
         } else if (value instanceof Integer) {
            tag.putInt(name, (Integer)value);
         } else if (value instanceof Boolean) {
            tag.putBoolean(name, (Boolean)value);
         } else if (value instanceof Long) {
            tag.putLong(name, (Long)value);
         } else if (value instanceof Float) {
            tag.putFloat(name, (Float)value);
         } else if (value instanceof Double) {
            tag.putDouble(name, (Double)value);
         } else if (value instanceof Byte) {
            tag.putByte(name, (Byte)value);
         } else if (value instanceof Short) {
            tag.putShort(name, (Short)value);
         } else if (value instanceof byte[]) {
            tag.putByteArray(name, (byte[])value);
         } else if (value instanceof int[]) {
            tag.putIntArray(name, (int[])value);
         } else if (value instanceof long[]) {
            tag.putLongArray(name, (long[])value);
         } else if (value instanceof Enum) {
            tag.putString(name, ((Enum)value).name());
            tag.putString(name + "_enumClass", value.getClass().getName());
         } else if (value instanceof Instant) {
            tag.putLong(name, ((Instant)value).toEpochMilli());
         } else if (value instanceof UUID uuid) {
            tag.putLong(name + "_mostSig", uuid.getMostSignificantBits());
            tag.putLong(name + "_leastSig", uuid.getLeastSignificantBits());
         } else if (value instanceof Optional<?> opt) {
            if (opt.isPresent()) {
               writeValue(tag, name + "_value", opt.get(), opt.get().getClass());
               tag.putBoolean(name + "_present", true);
            } else {
               tag.putBoolean(name + "_present", false);
            }
         } else if (value instanceof BitSet bitSet) {
            tag.putLongArray(name, bitSet.toLongArray());
         } else if (value instanceof Vec3 vec) {
            tag.putDouble(name + "_x", vec.x);
            tag.putDouble(name + "_y", vec.y);
            tag.putDouble(name + "_z", vec.z);
         } else if (value instanceof BlockHitResult hit) {
            writeValue(tag, name + "_pos", hit.getLocation(), Vec3.class);
            tag.putString(name + "_side", hit.getDirection().name());
            writeValue(tag, name + "_blockPos", hit.getBlockPos(), BlockPos.class);
            tag.putBoolean(name + "_insideBlock", hit.isInside());
         } else if (value instanceof Int2ObjectMap) {
            writeIntMap(tag, name, (Int2ObjectMap<?>)value);
         } else if (value instanceof Map) {
            writeMap(tag, name, (Map<?, ?>)value);
         } else if (value instanceof List) {
            writeList(tag, name, (List<?>)value);
         } else {
            try {
               tag.putString(name + "_toString", value.toString());
            } catch (Exception var12) {
            }
         }
      }
   }

   private static void writeIntMap(CompoundTag tag, String name, Int2ObjectMap<?> map) {
      CompoundTag mapTag = new CompoundTag();
      int index = 0;

      for (Entry<?> entry : map.int2ObjectEntrySet()) {
         String entryName = "entry_" + index++;
         CompoundTag entryTag = new CompoundTag();
         int key = entry.getIntKey();
         Object val = entry.getValue();
         entryTag.putInt("key", key);
         if (val != null) {
            writeValue(entryTag, "value", val, val.getClass());
         }

         mapTag.put(entryName, entryTag);
      }

      mapTag.putInt("size", map.size());
      tag.put(name, mapTag);
   }

   private static void writeMap(CompoundTag tag, String name, Map<?, ?> map) {
      CompoundTag mapTag = new CompoundTag();
      int index = 0;

      for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
         String entryName = "entry_" + index++;
         CompoundTag entryTag = new CompoundTag();
         Object key = entry.getKey();
         Object val = entry.getValue();
         if (key != null) {
            writeValue(entryTag, "key", key, key.getClass());
         }

         if (val != null) {
            writeValue(entryTag, "value", val, val.getClass());
         }

         mapTag.put(entryName, entryTag);
      }

      mapTag.putInt("size", map.size());
      tag.put(name, mapTag);
   }

   private static void writeList(CompoundTag tag, String name, List<?> list) {
      ListTag listTag = new ListTag();

      for (Object item : list) {
         if (item != null) {
            CompoundTag itemTag = new CompoundTag();
            writeValue(itemTag, "value", item, item.getClass());
            listTag.add(itemTag);
         }
      }

      tag.put(name, listTag);
      tag.putInt(name + "_size", list.size());
   }

   private static void readFields(Object obj, CompoundTag fieldsTag) {
      for (Class<?> clazz = obj.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
         for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
               field.setAccessible(true);
               String fieldName = field.getName();
               if (!fieldsTag.contains(fieldName + "_null")) {
                  try {
                     Object value = readValue(fieldsTag, fieldName, field.getType());
                     if (value != null) {
                        long offset = unsafe().objectFieldOffset(field);
                        Class<?> type = field.getType();
                        if (type == int.class) {
                           unsafe().putInt(obj, offset, (Integer)value);
                        } else if (type == boolean.class) {
                           unsafe().putBoolean(obj, offset, (Boolean)value);
                        } else if (type == byte.class) {
                           unsafe().putByte(obj, offset, (Byte)value);
                        } else if (type == short.class) {
                           unsafe().putShort(obj, offset, (Short)value);
                        } else if (type == long.class) {
                           unsafe().putLong(obj, offset, (Long)value);
                        } else if (type == float.class) {
                           unsafe().putFloat(obj, offset, (Float)value);
                        } else if (type == double.class) {
                           unsafe().putDouble(obj, offset, (Double)value);
                        } else if (type == char.class) {
                           unsafe().putChar(obj, offset, (Character)value);
                        } else {
                           unsafe().putObject(obj, offset, value);
                        }
                     } else {
                        Object defaultValue = defaultFieldValue(field.getType(), fieldName);
                        if (defaultValue != null) {
                           long offset = unsafe().objectFieldOffset(field);
                           unsafe().putObject(obj, offset, defaultValue);
                        }
                     }
                  } catch (Exception e) {
                     AutismClientAddon.LOG.warn("[Autism] Failed to deserialize field: {}", fieldName, e);
                  }
               }
            }
         }
      }
   }

   private static Object readValue(CompoundTag tag, String name, Class<?> targetType) {
      if (!tag.contains(name + "_type") && !tag.contains(name)) {
         return null;
      }

      if (tag.contains(name + "_empty") && tag.getBooleanOr(name + "_empty", false)) {
         return ItemStack.EMPTY;
      }

      String typeName = tag.contains(name + "_type") ? tag.getStringOr(name + "_type", "") : null;
      if (targetType == ItemStack.class || typeName != null && typeName.contains("ItemStack")) {
         if (tag.contains(name)) {
            try {
               Tag stackNbt = tag.get(name);
               return stackNbt == null
                  ? ItemStack.EMPTY
                  : ItemStack.CODEC.parse(registries().createSerializationContext(NbtOps.INSTANCE), stackNbt).result().orElse(ItemStack.EMPTY);
            } catch (Exception e) {
               return ItemStack.EMPTY;
            }
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         if (targetType == CompoundTag.class) {
            return tag.getCompound(name).orElse(new CompoundTag());
         }

         if (targetType == Tag.class) {
            return tag.get(name);
         }

         if (targetType == BlockPos.class) {
            return BlockPos.of(tag.getLongOr(name, 0L));
         }

         if (targetType == Identifier.class) {
            return Identifier.parse(tag.getStringOr(name, ""));
         }

         if (targetType == Component.class) {
            return Component.literal(tag.getStringOr(name, ""));
         }

         if (targetType == String.class) {
            return tag.getStringOr(name, "");
         }

         if (targetType == Integer.class || targetType == int.class) {
            return tag.getIntOr(name, 0);
         }

         if (targetType == Boolean.class || targetType == boolean.class) {
            return tag.getBooleanOr(name, false);
         }

         if (targetType == Long.class || targetType == long.class) {
            return tag.getLongOr(name, 0L);
         }

         if (targetType == Float.class || targetType == float.class) {
            return tag.getFloatOr(name, 0.0F);
         }

         if (targetType == Double.class || targetType == double.class) {
            return tag.getDoubleOr(name, 0.0);
         }

         if (targetType == Byte.class || targetType == byte.class) {
            return tag.getByteOr(name, (byte)0);
         }

         if (targetType == Short.class || targetType == short.class) {
            return tag.getShortOr(name, (short)0);
         }

         if (targetType == byte[].class) {
            return tag.getByteArray(name).orElse(new byte[0]);
         }

         if (targetType == int[].class) {
            return tag.getIntArray(name).orElse(new int[0]);
         }

         if (targetType == long[].class) {
            return tag.getLongArray(name).orElse(new long[0]);
         }

         if (targetType.isEnum()) {
            String enumName = tag.getStringOr(name, "");

            try {
               return Enum.valueOf((Class)targetType, enumName);
            } catch (Exception e) {
               return null;
            }
         } else {
            if (targetType == Instant.class) {
               long epochMilli = tag.getLongOr(name, System.currentTimeMillis());
               return Instant.ofEpochMilli(epochMilli);
            }

            if (targetType == UUID.class) {
               if (tag.contains(name + "_mostSig")) {
                  long mostSig = tag.getLongOr(name + "_mostSig", 0L);
                  long leastSig = tag.getLongOr(name + "_leastSig", 0L);
                  return new UUID(mostSig, leastSig);
               } else {
                  return new UUID(0L, 0L);
               }
            } else if (targetType == Optional.class || Optional.class.isAssignableFrom(targetType)) {
               if (tag.getBooleanOr(name + "_present", false)) {
                  Object value = readValue(tag, name + "_value", Object.class);
                  return Optional.ofNullable(value);
               } else {
                  return Optional.empty();
               }
            } else {
               if (targetType == BitSet.class) {
                  long[] longs = tag.getLongArray(name).orElse(new long[0]);
                  return BitSet.valueOf(longs);
               }

               if (targetType == Vec3.class) {
                  double x = tag.getDoubleOr(name + "_x", 0.0);
                  double y = tag.getDoubleOr(name + "_y", 0.0);
                  double z = tag.getDoubleOr(name + "_z", 0.0);
                  return new Vec3(x, y, z);
               }

               if (targetType == BlockHitResult.class) {
                  Vec3 pos = (Vec3)readValue(tag, name + "_pos", Vec3.class);
                  if (pos == null) {
                     pos = Vec3.ZERO;
                  }

                  String sideName = tag.getStringOr(name + "_side", "UP");

                  Direction side;
                  try {
                     side = Direction.valueOf(sideName);
                  } catch (IllegalArgumentException e) {
                     side = Direction.UP;
                  }

                  BlockPos blockPos = (BlockPos)readValue(tag, name + "_blockPos", BlockPos.class);
                  if (blockPos == null) {
                     blockPos = BlockPos.ZERO;
                  }

                  boolean insideBlock = tag.getBooleanOr(name + "_insideBlock", false);
                  return new BlockHitResult(pos, side, blockPos, insideBlock);
               } else if (targetType == HashedStack.class) {
                  return HashedStack.EMPTY;
               } else if (Int2ObjectMap.class.isAssignableFrom(targetType)) {
                  return decodeIntMap(tag.getCompound(name).orElse(new CompoundTag()));
               } else if (Map.class.isAssignableFrom(targetType)) {
                  return decodeMap(tag.getCompound(name).orElse(new CompoundTag()));
               } else if (List.class.isAssignableFrom(targetType)) {
                  return readList((ListTag)tag.get(name));
               } else {
                  return tag.contains(name + "_toString") ? null : null;
               }
            }
         }
      }
   }

   private static Int2ObjectMap<Object> decodeIntMap(CompoundTag mapTag) {
      Int2ObjectMap<Object> map = new Int2ObjectArrayMap<>();
      int size = mapTag.getIntOr("size", 0);

      for (int i = 0; i < size; i++) {
         String entryName = "entry_" + i;
         if (mapTag.contains(entryName)) {
            CompoundTag entryTag = mapTag.getCompound(entryName).orElse(new CompoundTag());
            int key = entryTag.getIntOr("key", 0);
            Object value = readValue(entryTag, "value", Object.class);
            if (value != null) {
               map.put(key, value);
            }
         }
      }

      return map;
   }

   private static Map<Object, Object> decodeMap(CompoundTag mapTag) {
      Map<Object, Object> map = new HashMap<>();
      int size = mapTag.getIntOr("size", 0);

      for (int i = 0; i < size; i++) {
         String entryName = "entry_" + i;
         if (mapTag.contains(entryName)) {
            CompoundTag entryTag = mapTag.getCompound(entryName).orElse(new CompoundTag());
            Object key = readValue(entryTag, "key", Object.class);
            Object value = readValue(entryTag, "value", Object.class);
            if (key != null) {
               map.put(key, value);
            }
         }
      }

      return map;
   }

   private static List<Object> readList(ListTag listTag) {
      List<Object> list = new ArrayList<>();

      for (int i = 0; i < listTag.size(); i++) {
         CompoundTag itemTag = (CompoundTag)listTag.get(i);
         Object value = readValue(itemTag, "value", Object.class);
         if (value != null) {
            list.add(value);
         }
      }

      return list;
   }

   private static Provider registries() {
      if (Minecraft.getInstance().level != null) {
         return Minecraft.getInstance().level.registryAccess();
      } else {
         return Minecraft.getInstance().getConnection() != null
            ? Minecraft.getInstance().getConnection().registryAccess()
            : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
      }
   }

   public static Provider init_HolderLookup_Provi() {
      return registries();
   }

   public static NbtAccounter createNbtAccounter() {
      return NbtAccounter.create(8388608L);
   }

   private static boolean isKnownPacketClass(String className) {
      if (className != null && !className.isBlank()) {
         Set<String> set = packetClassNames;
         if (set == null) {
            Set<String> built = new HashSet<>();

            try {
               for (Class<?> c : PacketTypeRegistry.getC2sPackets()) {
                  built.add(c.getName());
               }

               for (Class<?> c : PacketTypeRegistry.getS2cPackets()) {
                  built.add(c.getName());
               }
            } catch (Throwable var5) {
            }

            set = built;
            packetClassNames = built;
         }

         return set.contains(className);
      } else {
         return false;
      }
   }

   private static MacroClipboardCodec.UnsafeAccess unsafe() {
      return MacroClipboardCodec.UnsafeAccess.INSTANCE;
   }

   private static Object readRecord(Class<?> recordClass, CompoundTag fieldsTag) throws InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      RecordComponent[] components = recordClass.getRecordComponents();
      Class<?>[] paramTypes = new Class[components.length];
      Object[] args = new Object[components.length];

      for (int i = 0; i < components.length; i++) {
         RecordComponent component = components[i];
         paramTypes[i] = component.getType();
         String name = component.getName();
         Object value = readValue(fieldsTag, name, component.getType());
         if (value == null) {
            if (component.getType() == int.class) {
               value = 0;
            } else if (component.getType() == boolean.class) {
               value = false;
            } else if (component.getType() == byte.class) {
               value = (byte)0;
            } else if (component.getType() == short.class) {
               value = (short)0;
            } else if (component.getType() == long.class) {
               value = 0L;
            } else if (component.getType() == float.class) {
               value = 0.0F;
            } else if (component.getType() == double.class) {
               value = 0.0;
            } else if (component.getType() == char.class) {
               value = '\u0000';
            } else {
               value = defaultFieldValue(component.getType(), name);
            }
         }

         args[i] = value;
      }

      Constructor<?> constructor = recordClass.getDeclaredConstructor(paramTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
   }

   private static Object defaultFieldValue(Class<?> type, String fieldName) {
      if (type != CustomPacketPayload.class && !CustomPacketPayload.class.isAssignableFrom(type)) {
         try {
            try {
               Field emptyField = type.getDeclaredField("EMPTY");
               if (Modifier.isStatic(emptyField.getModifiers())) {
                  emptyField.setAccessible(true);
                  Object empty = emptyField.get(null);
                  if (empty != null) {
                     AutismClientAddon.LOG.warn("[Autism] Using {}.EMPTY for unsupported field: {}", type.getSimpleName(), fieldName);
                     return empty;
                  }
               }
            } catch (NoSuchFieldException var9) {
            }

            try {
               Field defaultField = type.getDeclaredField("DEFAULT");
               if (Modifier.isStatic(defaultField.getModifiers())) {
                  defaultField.setAccessible(true);
                  Object defaultValue = defaultField.get(null);
                  if (defaultValue != null) {
                     AutismClientAddon.LOG.warn("[Autism] Using {}.DEFAULT for unsupported field: {}", type.getSimpleName(), fieldName);
                     return defaultValue;
                  }
               }
            } catch (NoSuchFieldException var8) {
            }

            if (type.isRecord()) {
               RecordComponent[] components = type.getRecordComponents();
               Class<?>[] paramTypes = new Class[components.length];
               Object[] args = new Object[components.length];

               for (int i = 0; i < components.length; i++) {
                  paramTypes[i] = components[i].getType();
                  if (paramTypes[i] == byte[].class) {
                     args[i] = new byte[0];
                  } else if (paramTypes[i] == BitSet.class) {
                     args[i] = new BitSet();
                  } else if (paramTypes[i] == Vec3.class) {
                     args[i] = Vec3.ZERO;
                  } else if (paramTypes[i] == BlockHitResult.class) {
                     args[i] = BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
                  } else if (paramTypes[i].isPrimitive()) {
                     args[i] = primitiveDefault(paramTypes[i]);
                  } else if (paramTypes[i].isRecord()) {
                     try {
                        args[i] = defaultFieldValue(paramTypes[i], components[i].getName());
                     } catch (Exception e) {
                        AutismClientAddon.LOG.warn("[Autism] Failed to create nested Record {}: {}", paramTypes[i].getSimpleName(), e.getMessage());
                        args[i] = null;
                     }
                  } else {
                     args[i] = null;
                  }
               }

               Constructor<?> constructor = type.getDeclaredConstructor(paramTypes);
               constructor.setAccessible(true);
               Object instance = constructor.newInstance(args);
               AutismClientAddon.LOG.warn("[Autism] Created default {} for unsupported field: {}", type.getSimpleName(), fieldName);
               return instance;
            }
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("[Autism] Could not create safe default for {}: {}", type.getSimpleName(), e.getMessage());
         }

         AutismClientAddon.LOG.error("[Autism] No safe default found for unsupported type: {} field: {}", type.getSimpleName(), fieldName);
         return null;
      } else {
         return new AutismPayloadSupport.RawCustomPacketPayload(Identifier.fromNamespaceAndPath("minecraft", "empty"), new byte[0]);
      }
   }

   private static Object primitiveDefault(Class<?> type) {
      if (type == int.class) {
         return 0;
      } else if (type == boolean.class) {
         return false;
      } else if (type == byte.class) {
         return (byte)0;
      } else if (type == short.class) {
         return (short)0;
      } else if (type == long.class) {
         return 0L;
      } else if (type == float.class) {
         return 0.0F;
      } else if (type == double.class) {
         return 0.0;
      } else {
         return type == char.class ? '\u0000' : null;
      }
   }

   public static String encodeQueue(List<AutismSessionState.DelayedPacket> queue) {
      try {
         CompoundTag rootTag = new CompoundTag();
         ListTag packetList = new ListTag();

         for (AutismSessionState.DelayedPacket qp : queue) {
            CompoundTag packetTag = encode(qp);
            if (packetTag != null) {
               packetList.add(packetTag);
            }
         }

         rootTag.put("packets", packetList);
         rootTag.putInt("version", 1);
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         NbtIo.writeCompressed(rootTag, outputStream);
         return Base64.getEncoder().encodeToString(outputStream.toByteArray());
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to serialize queue to Base64", e);
         return null;
      }
   }

   public static List<AutismSessionState.DelayedPacket> decodeQueue(String base64) {
      try {
         byte[] compressed = Base64.getDecoder().decode(base64);
         ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
         CompoundTag rootTag = NbtIo.readCompressed(inputStream, createNbtAccounter());
         ListTag packetList = rootTag.getList("packets").orElse(new ListTag());
         List<AutismSessionState.DelayedPacket> queue = new ArrayList<>();

         for (int i = 0; i < packetList.size(); i++) {
            CompoundTag packetTag = (CompoundTag)packetList.get(i);
            AutismSessionState.DelayedPacket qp = readDelayedPacket(packetTag);
            if (qp != null) {
               queue.add(qp);
            }
         }

         AutismClientAddon.LOG.info("[Autism] Deserialized {} packets from Base64", queue.size());
         return queue.isEmpty() ? null : queue;
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to deserialize queue from Base64", e);
         return null;
      }
   }

   private static final class UnsafeAccess {
      private static final MacroClipboardCodec.UnsafeAccess INSTANCE = create();
      private final Object unsafe;
      private final Method objectFieldOffset;
      private final Method putIntMethod;
      private final Method putBooleanMethod;
      private final Method putByteMethod;
      private final Method putShortMethod;
      private final Method putLongMethod;
      private final Method putFloatMethod;
      private final Method putDoubleMethod;
      private final Method putCharMethod;
      private final Method putObjectMethod;
      private final Method allocateInstanceMethod;

      private UnsafeAccess(Object unsafe, Class<?> type) throws NoSuchMethodException {
         this.unsafe = unsafe;
         this.objectFieldOffset = type.getMethod("objectFieldOffset", Field.class);
         this.putIntMethod = type.getMethod("putInt", Object.class, long.class, int.class);
         this.putBooleanMethod = type.getMethod("putBoolean", Object.class, long.class, boolean.class);
         this.putByteMethod = type.getMethod("putByte", Object.class, long.class, byte.class);
         this.putShortMethod = type.getMethod("putShort", Object.class, long.class, short.class);
         this.putLongMethod = type.getMethod("putLong", Object.class, long.class, long.class);
         this.putFloatMethod = type.getMethod("putFloat", Object.class, long.class, float.class);
         this.putDoubleMethod = type.getMethod("putDouble", Object.class, long.class, double.class);
         this.putCharMethod = type.getMethod("putChar", Object.class, long.class, char.class);
         this.putObjectMethod = type.getMethod("putObject", Object.class, long.class, Object.class);
         this.allocateInstanceMethod = type.getMethod("allocateInstance", Class.class);
      }

      private static MacroClipboardCodec.UnsafeAccess create() {
         try {
            Class<?> type = Class.forName("sun.misc.Unsafe");
            Field f = type.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return new MacroClipboardCodec.UnsafeAccess(f.get(null), type);
         } catch (Exception e) {
            throw new RuntimeException("Cannot access Unsafe", e);
         }
      }

      long objectFieldOffset(Field field) {
         return this.invokeLong(this.objectFieldOffset, field);
      }

      void putInt(Object object, long offset, int value) {
         this.invokeVoid(this.putIntMethod, object, offset, value);
      }

      void putBoolean(Object object, long offset, boolean value) {
         this.invokeVoid(this.putBooleanMethod, object, offset, value);
      }

      void putByte(Object object, long offset, byte value) {
         this.invokeVoid(this.putByteMethod, object, offset, value);
      }

      void putShort(Object object, long offset, short value) {
         this.invokeVoid(this.putShortMethod, object, offset, value);
      }

      void putLong(Object object, long offset, long value) {
         this.invokeVoid(this.putLongMethod, object, offset, value);
      }

      void putFloat(Object object, long offset, float value) {
         this.invokeVoid(this.putFloatMethod, object, offset, value);
      }

      void putDouble(Object object, long offset, double value) {
         this.invokeVoid(this.putDoubleMethod, object, offset, value);
      }

      void putChar(Object object, long offset, char value) {
         this.invokeVoid(this.putCharMethod, object, offset, value);
      }

      void putObject(Object object, long offset, Object value) {
         this.invokeVoid(this.putObjectMethod, object, offset, value);
      }

      Object allocateInstance(Class<?> type) {
         return this.invokeObject(this.allocateInstanceMethod, type);
      }

      private long invokeLong(Method method, Object... args) {
         try {
            return (Long)method.invoke(this.unsafe, args);
         } catch (Exception e) {
            throw new RuntimeException("Unsafe call failed", e);
         }
      }

      private void invokeVoid(Method method, Object... args) {
         try {
            method.invoke(this.unsafe, args);
         } catch (Exception e) {
            throw new RuntimeException("Unsafe call failed", e);
         }
      }

      private Object invokeObject(Method method, Object... args) {
         try {
            return method.invoke(this.unsafe, args);
         } catch (Exception e) {
            throw new RuntimeException("Unsafe call failed", e);
         }
      }
   }

   public enum ClipboardKind {
      NONE,
      MACROS,
      STEPS;
   }

   public record PastedMacros(List<AutismMacro> macros, boolean fromInternalCopy) {
   }
}
