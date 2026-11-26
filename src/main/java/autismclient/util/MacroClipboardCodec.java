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
   private static final String init_Str = "autism_macro";
   private static final String load = "autism_macro_steps";
   private static final String read = "autism_macro_list";
   private static String write;
   private static List<AutismMacro> init_List = List.of();
   private static boolean init_Z;
   private static final long init_J = 8388608L;
   private static volatile Set<String> init_Set;

   public static void init_V(List<AutismSessionState.DelayedPacket> queue) {
      try {
         CompoundTag rootTag = new CompoundTag();
         ListTag packetList = new ListTag();

         for (AutismSessionState.DelayedPacket qp : queue) {
            CompoundTag packetTag = init(qp);
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

   public static boolean init_Z(AutismMacro macro) {
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

   public static boolean init_Z(List<AutismMacro> macros, boolean cut) {
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
            if (!init_Z(live.get(0))) {
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

         init_V(live, cut);
         return true;
      } else {
         return false;
      }
   }

   private static void init_V(List<AutismMacro> macros, boolean cut) {
      List<AutismMacro> copies = new ArrayList<>(macros.size());

      for (AutismMacro macro : macros) {
         copies.add(macro.copy());
      }

      init_List = List.copyOf(copies);
      init_Z = cut;

      try {
         write = Minecraft.getInstance().keyboardHandler.getClipboard();
      } catch (Exception e) {
         write = null;
      }
   }

   public static MacroClipboardCodec.Inner_StreamUtil init_o_StreamUtil() {
      String clipboard;
      try {
         clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
      } catch (Exception e) {
         clipboard = null;
      }

      if (clipboard != null && write != null && clipboard.equals(write) && !init_List.isEmpty()) {
         List<AutismMacro> out = new ArrayList<>(init_List.size());

         for (AutismMacro macro : init_List) {
            AutismMacro copy = macro.copy();
            if (!init_Z) {
               copy.keyCode = -1;
            }

            out.add(copy);
         }

         init_Z = false;
         return new MacroClipboardCodec.Inner_StreamUtil(out, true);
      } else {
         List<AutismMacro> list = init_List();
         if (!list.isEmpty()) {
            return new MacroClipboardCodec.Inner_StreamUtil(list, false);
         }

         AutismMacro single = init_aM();
         return new MacroClipboardCodec.Inner_StreamUtil(single == null ? List.of() : List.of(single), false);
      }
   }

   public static MacroClipboardCodec.Inner_DataUtil init_o_DataUtil() {
      CompoundTag rootTag = init_CompoundTag();
      if (rootTag == null) {
         return MacroClipboardCodec.Inner_DataUtil.NONE;
      } else {
         String type = rootTag.getStringOr("type", "");
         if (!"autism_macro".equals(type) && !"autism_macro_list".equals(type)) {
            return "autism_macro_steps".equals(type) ? MacroClipboardCodec.Inner_DataUtil.STEPS : MacroClipboardCodec.Inner_DataUtil.NONE;
         } else {
            return MacroClipboardCodec.Inner_DataUtil.MACROS;
         }
      }
   }

   private static CompoundTag init_CompoundTag() {
      try {
         String base64 = Minecraft.getInstance().keyboardHandler.getClipboard();
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            CompoundTag rootTag = NbtIo.readCompressed(new ByteArrayInputStream(data), init_NbtAccounter());
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

   private static List<MacroAction> init(ListTag actionList) {
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

      init(dropped);
      return actions;
   }

   private static AutismMacro init_aM(CompoundTag macroTag) {
      int dropped = AutismMacro.pruneUnknownActions(macroTag);
      AutismMacro macro = new AutismMacro().fromTag(macroTag).sanitizeForSharing();
      macro.keyCode = -1;
      init(dropped);
      return macro;
   }

   private static void init(int dropped) {
      if (dropped > 0) {
         bi.init("Removed " + dropped + " addon action" + (dropped == 1 ? "" : "s") + " from imported macro for safety", -2054854);
      }
   }

   public static AutismMacro init_aM() {
      CompoundTag rootTag = init_CompoundTag();
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
            return init_aM(macroTag);
         }
      } else if ("autism_macro_steps".equals(type)) {
         List<MacroAction> actions = init(rootTag.getList("actions").orElse(new ListTag()));
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

   public static List<AutismMacro> init_List() {
      List<AutismMacro> macros = new ArrayList<>();
      CompoundTag rootTag = init_CompoundTag();
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
            macros.add(init_aM(macroTag));
         }
      }

      return macros;
   }

   private static CompoundTag init_CompoundTag(String base64) {
      try {
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            CompoundTag rootTag = NbtIo.readCompressed(new ByteArrayInputStream(data), init_NbtAccounter());
            return rootTag.getIntOr("version", 0) == 1 ? rootTag : null;
         } else {
            return null;
         }
      } catch (Exception e) {
         return null;
      }
   }

   public static String init_Str(AutismMacro macro) {
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
      CompoundTag rootTag = init_CompoundTag(base64);
      if (rootTag == null) {
         return null;
      }

      String type = rootTag.getStringOr("type", "");
      if ("autism_macro".equals(type)) {
         CompoundTag macroTag = rootTag.getCompound("macro").orElse(new CompoundTag());
         return macroTag.isEmpty() ? null : init_aM(macroTag);
      }

      if ("autism_macro_steps".equals(type)) {
         List<MacroAction> actions = init(rootTag.getList("actions").orElse(new ListTag()));
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
      CompoundTag rootTag = init_CompoundTag(base64);
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

   public static boolean init_Z(List<MacroAction> actions) {
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

   public static List<MacroAction> load_List() {
      CompoundTag rootTag = init_CompoundTag();
      if (rootTag == null) {
         return null;
      }

      String type = rootTag.getStringOr("type", "");
      if ("autism_macro_steps".equals(type)) {
         List<MacroAction> actions = init(rootTag.getList("actions").orElse(new ListTag()));
         return actions.isEmpty() ? null : actions;
      }

      if ("autism_macro".equals(type)) {
         CompoundTag macroTag = rootTag.getCompound("macro").orElse(new CompoundTag());
         List<MacroAction> actions = init(macroTag.getList("actions").orElse(new ListTag()));
         return actions.isEmpty() ? null : actions;
      }

      if ("autism_macro_list".equals(type)) {
         List<MacroAction> actions = new ArrayList<>();
         ListTag macroList = rootTag.getList("macros").orElse(new ListTag());

         for (int i = 0; i < macroList.size(); i++) {
            if (macroList.get(i) instanceof CompoundTag macroTag) {
               actions.addAll(init(macroTag.getList("actions").orElse(new ListTag())));
            }
         }

         return actions.isEmpty() ? null : actions;
      } else {
         return null;
      }
   }

   public static List<AutismSessionState.DelayedPacket> read() {
      try {
         String base64 = Minecraft.getInstance().keyboardHandler.getClipboard();
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            CompoundTag rootTag = NbtIo.readCompressed(inputStream, init_NbtAccounter());
            int version = rootTag.getIntOr("version", 0);
            if (version != 1) {
               AutismClientAddon.LOG.error("[Autism] Unsupported clipboard format version: {}", version);
               return null;
            }

            ListTag packetList = (ListTag)rootTag.get("packets");
            List<AutismSessionState.DelayedPacket> queue = new ArrayList<>();

            for (int i = 0; i < packetList.size(); i++) {
               CompoundTag packetTag = (CompoundTag)packetList.get(i);
               AutismSessionState.DelayedPacket qp = init_ck_CachePool(packetTag);
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

   public static Packet<?> init_Packet(String base64) {
      try {
         if (base64 != null && !base64.trim().isEmpty()) {
            byte[] data = Base64.getDecoder().decode(base64.trim());
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            CompoundTag tag = NbtIo.readCompressed(inputStream, init_NbtAccounter());
            if (tag.contains("packets")) {
               ListTag list = (ListTag)tag.get("packets");
               if (list != null && !list.isEmpty()) {
                  CompoundTag packetTag = (CompoundTag)list.get(0);
                  AutismSessionState.DelayedPacket qp = init_ck_CachePool(packetTag);
                  return qp != null ? qp.packet : null;
               } else {
                  return null;
               }
            } else if (tag.contains("packet")) {
               AutismSessionState.DelayedPacket qp = init_ck_CachePool(tag);
               return qp != null ? qp.packet : null;
            } else {
               return init_Packet(tag);
            }
         } else {
            return null;
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to deserialize packet from Base64", e);
         return null;
      }
   }

   public static CompoundTag init(AutismSessionState.DelayedPacket qp) {
      try {
         CompoundTag tag = new CompoundTag();
         tag.putInt("delay", qp.getDelay());
         tag.putInt("id", qp.getId());
         tag.putString("replayMode", qp.getReplayMode().name());
         CompoundTag packetData = init(qp.packet);
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

   public static AutismSessionState.DelayedPacket init_ck_CachePool(CompoundTag tag) {
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
         Packet<?> packet = init_Packet(packetData);
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

   private static CompoundTag init(Packet<?> packet) {
      CompoundTag tag = new CompoundTag();
      String className = packet.getClass().getName();
      tag.putString("class", className);
      String friendlyName = PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packet.getClass());
      if (friendlyName != null) {
         tag.putString("friendlyName", friendlyName);
      }

      if (packet instanceof ServerboundCustomPayloadPacket) {
         tag.putBoolean("customPayloadPacket", true);
         AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.init_AutismPayloadSuppo(packet, "C2S");
         if (snapshot != null) {
            tag.putString("payloadChannel", snapshot.load_Str());
            tag.putByteArray("payloadBytes", snapshot.init_BArr());
            return tag;
         }
      }

      CompoundTag fieldsTag = new CompoundTag();
      init(packet, fieldsTag);
      tag.put("fields", fieldsTag);
      return tag;
   }

   private static Packet<?> init_Packet(CompoundTag tag) {
      try {
         if (tag.getBooleanOr("customPayloadPacket", false)) {
            String channel = tag.getStringOr("payloadChannel", "");
            byte[] bytes = tag.getByteArray("payloadBytes").orElse(new byte[0]);
            if (!channel.isBlank()) {
               return AutismPayloadSupport.init_ServerboundCustomP(channel, bytes);
            }
         }

         String className = tag.getStringOr("class", "");
         Class<?> packetClass = null;
         if (init_Z(className)) {
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
            return (Packet<?>)init(packetClass, fieldsTag);
         }

         Packet<?> packet = (Packet<?>)init_o_CachePool().init_Obj_Class(packetClass);
         load(packet, fieldsTag);
         if (!PacketTypeRegistry.getC2sPackets().contains(packetClass)) {
            AutismClientAddon.LOG.warn("[Autism] Deserialized packet {} is NOT a registered C2S packet! It may be skipped when sending.", className);
         }

         return packet;
      } catch (Exception e) {
         AutismClientAddon.LOG.error("[Autism] Failed to deserialize packet", e);
         return null;
      }
   }

   private static void init(Object obj, CompoundTag fieldsTag) {
      for (Class<?> clazz = obj.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
         for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
               field.setAccessible(true);

               try {
                  Object value = field.get(obj);
                  init(fieldsTag, field.getName(), value, field.getType());
               } catch (Exception e) {
                  AutismClientAddon.LOG.warn("[Autism] Failed to serialize field: {}", field.getName(), e);
               }
            }
         }
      }
   }

   private static void init(CompoundTag tag, String name, Object value, Class<?> type) {
      if (value == null) {
         tag.putBoolean(name + "_null", true);
      } else {
         tag.putString(name + "_type", value.getClass().getName());
         if (value instanceof ItemStack stack) {
            if (!stack.isEmpty()) {
               CompoundTag stackNbt = new CompoundTag();

               try {
                  Tag encoded = ItemStack.CODEC.encodeStart(load_HolderLookup_Provi().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
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
               init(tag, name + "_value", opt.get(), opt.get().getClass());
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
            init(tag, name + "_pos", hit.getLocation(), Vec3.class);
            tag.putString(name + "_side", hit.getDirection().name());
            init(tag, name + "_blockPos", hit.getBlockPos(), BlockPos.class);
            tag.putBoolean(name + "_insideBlock", hit.isInside());
         } else if (value instanceof Int2ObjectMap) {
            init(tag, name, (Int2ObjectMap<?>)value);
         } else if (value instanceof Map) {
            init(tag, name, (Map<?, ?>)value);
         } else if (value instanceof List) {
            init(tag, name, (List<?>)value);
         } else {
            try {
               tag.putString(name + "_toString", value.toString());
            } catch (Exception var12) {
            }
         }
      }
   }

   private static void init(CompoundTag tag, String name, Int2ObjectMap<?> map) {
      CompoundTag mapTag = new CompoundTag();
      int index = 0;

      for (Entry<?> entry : map.int2ObjectEntrySet()) {
         String entryName = "entry_" + index++;
         CompoundTag entryTag = new CompoundTag();
         int key = entry.getIntKey();
         Object val = entry.getValue();
         entryTag.putInt("key", key);
         if (val != null) {
            init(entryTag, "value", val, val.getClass());
         }

         mapTag.put(entryName, entryTag);
      }

      mapTag.putInt("size", map.size());
      tag.put(name, mapTag);
   }

   private static void init(CompoundTag tag, String name, Map<?, ?> map) {
      CompoundTag mapTag = new CompoundTag();
      int index = 0;

      for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
         String entryName = "entry_" + index++;
         CompoundTag entryTag = new CompoundTag();
         Object key = entry.getKey();
         Object val = entry.getValue();
         if (key != null) {
            init(entryTag, "key", key, key.getClass());
         }

         if (val != null) {
            init(entryTag, "value", val, val.getClass());
         }

         mapTag.put(entryName, entryTag);
      }

      mapTag.putInt("size", map.size());
      tag.put(name, mapTag);
   }

   private static void init(CompoundTag tag, String name, List<?> list) {
      ListTag listTag = new ListTag();

      for (Object item : list) {
         if (item != null) {
            CompoundTag itemTag = new CompoundTag();
            init(itemTag, "value", item, item.getClass());
            listTag.add(itemTag);
         }
      }

      tag.put(name, listTag);
      tag.putInt(name + "_size", list.size());
   }

   private static void load(Object obj, CompoundTag fieldsTag) {
      for (Class<?> clazz = obj.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
         for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
               field.setAccessible(true);
               String fieldName = field.getName();
               if (!fieldsTag.contains(fieldName + "_null")) {
                  try {
                     Object value = init(fieldsTag, fieldName, field.getType());
                     if (value != null) {
                        long offset = init_o_CachePool().init_J_Field(field);
                        Class<?> type = field.getType();
                        if (type == int.class) {
                           init_o_CachePool().init_V_Obj_5(obj, offset, (Integer)value);
                        } else if (type == boolean.class) {
                           init_o_CachePool().init_V_Obj_9(obj, offset, (Boolean)value);
                        } else if (type == byte.class) {
                           init_o_CachePool().init_V_Obj(obj, offset, (Byte)value);
                        } else if (type == short.class) {
                           init_o_CachePool().init_V_Obj_8(obj, offset, (Short)value);
                        } else if (type == long.class) {
                           init_o_CachePool().init_V_Obj_6(obj, offset, (Long)value);
                        } else if (type == float.class) {
                           init_o_CachePool().init_V_Obj_4(obj, offset, (Float)value);
                        } else if (type == double.class) {
                           init_o_CachePool().init_V_Obj_3(obj, offset, (Double)value);
                        } else if (type == char.class) {
                           init_o_CachePool().init_V_Obj_2(obj, offset, (Character)value);
                        } else {
                           init_o_CachePool().init_V_Obj_7(obj, offset, value);
                        }
                     } else {
                        Object defaultValue = init(field.getType(), fieldName);
                        if (defaultValue != null) {
                           long offset = init_o_CachePool().init_J_Field(field);
                           init_o_CachePool().init_V_Obj_7(obj, offset, defaultValue);
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

   private static Object init(CompoundTag tag, String name, Class<?> targetType) {
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
                  : ItemStack.CODEC.parse(load_HolderLookup_Provi().createSerializationContext(NbtOps.INSTANCE), stackNbt).result().orElse(ItemStack.EMPTY);
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
                  Object value = init(tag, name + "_value", Object.class);
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
                  Vec3 pos = (Vec3)init(tag, name + "_pos", Vec3.class);
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

                  BlockPos blockPos = (BlockPos)init(tag, name + "_blockPos", BlockPos.class);
                  if (blockPos == null) {
                     blockPos = BlockPos.ZERO;
                  }

                  boolean insideBlock = tag.getBooleanOr(name + "_insideBlock", false);
                  return new BlockHitResult(pos, side, blockPos, insideBlock);
               } else if (targetType == HashedStack.class) {
                  return HashedStack.EMPTY;
               } else if (Int2ObjectMap.class.isAssignableFrom(targetType)) {
                  return init_Int2ObjectMap(tag.getCompound(name).orElse(new CompoundTag()));
               } else if (Map.class.isAssignableFrom(targetType)) {
                  return init_Map(tag.getCompound(name).orElse(new CompoundTag()));
               } else if (List.class.isAssignableFrom(targetType)) {
                  return load((ListTag)tag.get(name));
               } else {
                  return tag.contains(name + "_toString") ? null : null;
               }
            }
         }
      }
   }

   private static Int2ObjectMap<Object> init_Int2ObjectMap(CompoundTag mapTag) {
      Int2ObjectMap<Object> map = new Int2ObjectArrayMap<>();
      int size = mapTag.getIntOr("size", 0);

      for (int i = 0; i < size; i++) {
         String entryName = "entry_" + i;
         if (mapTag.contains(entryName)) {
            CompoundTag entryTag = mapTag.getCompound(entryName).orElse(new CompoundTag());
            int key = entryTag.getIntOr("key", 0);
            Object value = init(entryTag, "value", Object.class);
            if (value != null) {
               map.put(key, value);
            }
         }
      }

      return map;
   }

   private static Map<Object, Object> init_Map(CompoundTag mapTag) {
      Map<Object, Object> map = new HashMap<>();
      int size = mapTag.getIntOr("size", 0);

      for (int i = 0; i < size; i++) {
         String entryName = "entry_" + i;
         if (mapTag.contains(entryName)) {
            CompoundTag entryTag = mapTag.getCompound(entryName).orElse(new CompoundTag());
            Object key = init(entryTag, "key", Object.class);
            Object value = init(entryTag, "value", Object.class);
            if (key != null) {
               map.put(key, value);
            }
         }
      }

      return map;
   }

   private static List<Object> load(ListTag listTag) {
      List<Object> list = new ArrayList<>();

      for (int i = 0; i < listTag.size(); i++) {
         CompoundTag itemTag = (CompoundTag)listTag.get(i);
         Object value = init(itemTag, "value", Object.class);
         if (value != null) {
            list.add(value);
         }
      }

      return list;
   }

   private static Provider load_HolderLookup_Provi() {
      if (Minecraft.getInstance().level != null) {
         return Minecraft.getInstance().level.registryAccess();
      } else {
         return Minecraft.getInstance().getConnection() != null
            ? Minecraft.getInstance().getConnection().registryAccess()
            : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
      }
   }

   public static Provider init_HolderLookup_Provi() {
      return load_HolderLookup_Provi();
   }

   public static NbtAccounter init_NbtAccounter() {
      return NbtAccounter.create(8388608L);
   }

   private static boolean init_Z(String className) {
      if (className != null && !className.isBlank()) {
         Set<String> set = init_Set;
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
            init_Set = built;
         }

         return set.contains(className);
      } else {
         return false;
      }
   }

   private static MacroClipboardCodec.Inner_CachePool init_o_CachePool() {
      return MacroClipboardCodec.Inner_CachePool.init_o_CachePool;
   }

   private static Object init(Class<?> recordClass, CompoundTag fieldsTag) throws InstantiationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      RecordComponent[] components = recordClass.getRecordComponents();
      Class<?>[] paramTypes = new Class[components.length];
      Object[] args = new Object[components.length];

      for (int i = 0; i < components.length; i++) {
         RecordComponent component = components[i];
         paramTypes[i] = component.getType();
         String name = component.getName();
         Object value = init(fieldsTag, name, component.getType());
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
               value = init(component.getType(), name);
            }
         }

         args[i] = value;
      }

      Constructor<?> constructor = recordClass.getDeclaredConstructor(paramTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
   }

   private static Object init(Class<?> type, String fieldName) {
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
                     args[i] = init(paramTypes[i]);
                  } else if (paramTypes[i].isRecord()) {
                     try {
                        args[i] = init(paramTypes[i], components[i].getName());
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

   private static Object init(Class<?> type) {
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

   public static String init_Str(List<AutismSessionState.DelayedPacket> queue) {
      try {
         CompoundTag rootTag = new CompoundTag();
         ListTag packetList = new ListTag();

         for (AutismSessionState.DelayedPacket qp : queue) {
            CompoundTag packetTag = init(qp);
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

   public static List<AutismSessionState.DelayedPacket> init_List(String base64) {
      try {
         byte[] compressed = Base64.getDecoder().decode(base64);
         ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
         CompoundTag rootTag = NbtIo.readCompressed(inputStream, init_NbtAccounter());
         ListTag packetList = rootTag.getList("packets").orElse(new ListTag());
         List<AutismSessionState.DelayedPacket> queue = new ArrayList<>();

         for (int i = 0; i < packetList.size(); i++) {
            CompoundTag packetTag = (CompoundTag)packetList.get(i);
            AutismSessionState.DelayedPacket qp = init_ck_CachePool(packetTag);
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

   private static final class Inner_CachePool {
      private static final MacroClipboardCodec.Inner_CachePool init_o_CachePool = init_o_CachePool();
      private final Object init_Obj;
      private final Method init_Method;
      private final Method load;
      private final Method read;
      private final Method write;
      private final Method update;
      private final Method sync;
      private final Method flush;
      private final Method close;
      private final Method open;
      private final Method poll;
      private final Method push;

      private Inner_CachePool(Object unsafe, Class<?> type) throws NoSuchMethodException {
         this.init_Obj = unsafe;
         this.init_Method = type.getMethod("objectFieldOffset", Field.class);
         this.load = type.getMethod("putInt", Object.class, long.class, int.class);
         this.read = type.getMethod("putBoolean", Object.class, long.class, boolean.class);
         this.write = type.getMethod("putByte", Object.class, long.class, byte.class);
         this.update = type.getMethod("putShort", Object.class, long.class, short.class);
         this.sync = type.getMethod("putLong", Object.class, long.class, long.class);
         this.flush = type.getMethod("putFloat", Object.class, long.class, float.class);
         this.close = type.getMethod("putDouble", Object.class, long.class, double.class);
         this.open = type.getMethod("putChar", Object.class, long.class, char.class);
         this.poll = type.getMethod("putObject", Object.class, long.class, Object.class);
         this.push = type.getMethod("allocateInstance", Class.class);
      }

      private static MacroClipboardCodec.Inner_CachePool init_o_CachePool() {
         try {
            Class<?> type = Class.forName("sun.misc.Unsafe");
            Field f = type.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return new MacroClipboardCodec.Inner_CachePool(f.get(null), type);
         } catch (Exception e) {
            throw new RuntimeException("Cannot access Unsafe", e);
         }
      }

      long init_J_Field(Field field) {
         return this.init_J_Method_ObjArr(this.init_Method, field);
      }

      void init_V_Obj_5(Object object, long offset, int value) {
         this.init_V_Method_ObjArr(this.load, object, offset, value);
      }

      void init_V_Obj_9(Object object, long offset, boolean value) {
         this.init_V_Method_ObjArr(this.read, object, offset, value);
      }

      void init_V_Obj(Object object, long offset, byte value) {
         this.init_V_Method_ObjArr(this.write, object, offset, value);
      }

      void init_V_Obj_8(Object object, long offset, short value) {
         this.init_V_Method_ObjArr(this.update, object, offset, value);
      }

      void init_V_Obj_6(Object object, long offset, long value) {
         this.init_V_Method_ObjArr(this.sync, object, offset, value);
      }

      void init_V_Obj_4(Object object, long offset, float value) {
         this.init_V_Method_ObjArr(this.flush, object, offset, value);
      }

      void init_V_Obj_3(Object object, long offset, double value) {
         this.init_V_Method_ObjArr(this.close, object, offset, value);
      }

      void init_V_Obj_2(Object object, long offset, char value) {
         this.init_V_Method_ObjArr(this.open, object, offset, value);
      }

      void init_V_Obj_7(Object object, long offset, Object value) {
         this.init_V_Method_ObjArr(this.poll, object, offset, value);
      }

      Object init_Obj_Class(Class<?> type) {
         return this.init_Obj_Method_ObjArr(this.push, type);
      }

      private long init_J_Method_ObjArr(Method method, Object... args) {
         try {
            return (Long)method.invoke(this.init_Obj, args);
         } catch (Exception e) {
            throw new RuntimeException("Unsafe call failed", e);
         }
      }

      private void init_V_Method_ObjArr(Method method, Object... args) {
         try {
            method.invoke(this.init_Obj, args);
         } catch (Exception e) {
            throw new RuntimeException("Unsafe call failed", e);
         }
      }

      private Object init_Obj_Method_ObjArr(Method method, Object... args) {
         try {
            return method.invoke(this.init_Obj, args);
         } catch (Exception e) {
            throw new RuntimeException("Unsafe call failed", e);
         }
      }
   }

   public enum Inner_DataUtil {
      NONE,
      MACROS,
      STEPS;
   }

   public record Inner_StreamUtil(List<AutismMacro> init_List, boolean init_Z) {
   }
}
