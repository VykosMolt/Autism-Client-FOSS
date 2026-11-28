package autismclient.util.macro;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;

public class WaitPacketMatchAction implements CaptureOutputAction, MacroAction {
   public final List<WaitPacketMatchAction.Inner_CachePool> rules = new ArrayList<>();
   public WaitPacketMatchAction.Inner_DataUtil direction = WaitPacketMatchAction.Inner_DataUtil.C2S;
   public String packetName = "";
   public String fieldName = "";
   public WaitPacketMatchAction.Inner_StreamUtil operator = WaitPacketMatchAction.Inner_StreamUtil.EXISTS;
   public String value = "";
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   public transient volatile Packet<?> matchedPacket;
   public transient volatile String matchedDirection = "";

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_PACKET_MATCH;
   }

   public List<WaitPacketMatchAction.Inner_CachePool> effectiveRules() {
      if (!this.rules.isEmpty()) {
         return this.rules;
      }

      WaitPacketMatchAction.Inner_CachePool legacy = this.init_aM_CachePool();
      return legacy.packetName.isBlank() && legacy.fieldName.isBlank() ? List.of() : List.of(legacy);
   }

   public WaitPacketMatchAction.Inner_CachePool init_aM_CachePool_aM_DataUtil_Str(WaitPacketMatchAction.Inner_DataUtil direction, String packetName) {
      WaitPacketMatchAction.Inner_CachePool rule = new WaitPacketMatchAction.Inner_CachePool();
      rule.direction = direction == null ? WaitPacketMatchAction.Inner_DataUtil.ANY : direction;
      rule.packetName = packetName == null ? "" : packetName;
      this.rules.add(rule);
      this.init_V_11();
      return rule;
   }

   public void init_V_11() {
      WaitPacketMatchAction.Inner_CachePool first = this.rules.isEmpty() ? null : this.rules.get(0);
      if (first != null) {
         this.direction = first.direction;
         this.packetName = first.packetName == null ? "" : first.packetName;
         this.fieldName = first.fieldName == null ? "" : first.fieldName;
         this.operator = first.operator == null ? WaitPacketMatchAction.Inner_StreamUtil.EXISTS : first.operator;
         this.value = first.value == null ? "" : first.value;
      }
   }

   public boolean matches(Packet<?> packet, String packetDirection) {
      List<WaitPacketMatchAction.Inner_CachePool> activeRules = this.effectiveRules();
      if (activeRules.isEmpty()) {
         return true;
      }

      for (WaitPacketMatchAction.Inner_CachePool rule : activeRules) {
         if (this.init(rule, packet, packetDirection)) {
            return true;
         }
      }

      return false;
   }

   private boolean init(WaitPacketMatchAction.Inner_CachePool rule, Packet<?> packet, String packetDirection) {
      if (rule.direction != WaitPacketMatchAction.Inner_DataUtil.ANY && !rule.direction.name().equalsIgnoreCase(packetDirection)) {
         return false;
      }

      if (!PacketGateManager.matches(rule.packetName, packet, packetDirection)) {
         return false;
      }

      if (rule.fieldName != null && !rule.fieldName.isBlank()) {
         Object field = this.init_Obj_Packet_Str(packet, rule.fieldName);
         WaitPacketMatchAction.Inner_StreamUtil op = rule.operator == null ? WaitPacketMatchAction.Inner_StreamUtil.EXISTS : rule.operator;
         if (op == WaitPacketMatchAction.Inner_StreamUtil.EXISTS) {
            return field != null;
         }

         String actual = field == null ? "" : String.valueOf(field);
         String expected = rule.value == null ? "" : rule.value;
         boolean var10000;
         switch (op) {
            case EXISTS:
               var10000 = field != null;
               break;
            case EQUALS:
               var10000 = actual.equalsIgnoreCase(expected);
               break;
            case CONTAINS:
               var10000 = actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT));
               break;
            case NOT_EQUALS:
               var10000 = !actual.equalsIgnoreCase(expected);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return true;
      }
   }

   private WaitPacketMatchAction.Inner_CachePool init_aM_CachePool() {
      WaitPacketMatchAction.Inner_CachePool rule = new WaitPacketMatchAction.Inner_CachePool();
      rule.direction = this.direction == null ? WaitPacketMatchAction.Inner_DataUtil.C2S : this.direction;
      rule.packetName = this.packetName == null ? "" : this.packetName;
      rule.fieldName = this.fieldName == null ? "" : this.fieldName;
      rule.operator = this.operator == null ? WaitPacketMatchAction.Inner_StreamUtil.EXISTS : this.operator;
      rule.value = this.value == null ? "" : this.value;
      return rule;
   }

   private Object init_Obj_Packet_Str(Packet<?> packet, String name) {
      if (packet != null && name != null && !name.isBlank()) {
         String wanted = name.trim();

         try {
            Method m = packet.getClass().getMethod(wanted);
            if (m.getParameterCount() == 0) {
               return m.invoke(packet);
            }
         } catch (Exception var8) {
         }

         String getter = "get" + Character.toUpperCase(wanted.charAt(0)) + wanted.substring(1);

         try {
            Method m = packet.getClass().getMethod(getter);
            if (m.getParameterCount() == 0) {
               return m.invoke(packet);
            }
         } catch (Exception var7) {
         }

         for (Class<?> type = packet.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            try {
               Field f = type.getDeclaredField(wanted);
               f.setAccessible(true);
               return f.get(packet);
            } catch (Exception var9) {
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static List<String> fieldNames(Class<? extends Packet<?>> packetClass) {
      if (packetClass == null) {
         return List.of();
      }

      Map<String, Class<?>> fields = init_Map(packetClass);
      return fields.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
   }

   public static List<String> valueOptions(Class<? extends Packet<?>> packetClass, String fieldName) {
      Class<?> type = init_Map(packetClass).get(fieldName);
      if (type == null) {
         return List.of();
      }

      if (type == boolean.class || type == Boolean.class) {
         return List.of("true", "false");
      }

      if (!type.isEnum()) {
         return List.of();
      }

      Object[] constants = type.getEnumConstants();
      List<String> out = new ArrayList<>();
      if (constants != null) {
         for (Object constant : constants) {
            out.add(String.valueOf(constant));
         }
      }

      out.sort(String.CASE_INSENSITIVE_ORDER);
      return out;
   }

   public static Map<String, Class<?>> init_Map(Class<? extends Packet<?>> packetClass) {
      Map<String, Class<?>> out = new LinkedHashMap<>();
      if (packetClass == null) {
         return out;
      }

      RecordComponent[] components = packetClass.getRecordComponents();
      if (components != null) {
         for (RecordComponent component : components) {
            out.putIfAbsent(component.getName(), component.getType());
         }
      }

      for (Class<?> type = packetClass; type != null && type != Object.class; type = type.getSuperclass()) {
         for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
               out.putIfAbsent(field.getName(), field.getType());
            }
         }
      }

      for (Method method : packetClass.getMethods()) {
         if (method.getParameterCount() == 0 && method.getReturnType() != void.class && !method.isSynthetic()) {
            String name = method.getName();
            if (!name.equals("getClass")
               && !name.equals("hashCode")
               && !name.equals("toString")
               && !name.equals("packetType")
               && !name.equals("getPacketType")
               && !name.startsWith("lambda$")
               && !name.startsWith("access$")) {
               if (name.startsWith("get") && name.length() > 3) {
                  name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
               }

               out.putIfAbsent(name, method.getReturnType());
            }
         }
      }

      return out.entrySet()
         .stream()
         .sorted(Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
         .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "WAIT_PACKET_MATCH");
      ListTag list = new ListTag();

      for (WaitPacketMatchAction.Inner_CachePool rule : this.effectiveRules()) {
         list.add(rule.toTag());
      }

      tag.put("rules", list);
      tag.putInt("timeoutMs", this.timeoutMs);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.rules.clear();
      if (tag.contains("rules")) {
         for (Tag element : tag.getList("rules").orElse(new ListTag())) {
            if (element instanceof CompoundTag compound) {
               this.rules.add(WaitPacketMatchAction.Inner_CachePool.init(compound));
            }
         }
      }

      this.direction = MacroNbtCodecHelper.parseEnum(WaitPacketMatchAction.Inner_DataUtil.class, tag.getStringOr("direction", "C2S"), WaitPacketMatchAction.Inner_DataUtil.C2S);
      this.packetName = tag.getStringOr("packetName", "");
      this.fieldName = tag.getStringOr("fieldName", "");
      this.operator = MacroNbtCodecHelper.parseEnum(WaitPacketMatchAction.Inner_StreamUtil.class, tag.getStringOr("operator", "EXISTS"), WaitPacketMatchAction.Inner_StreamUtil.EXISTS);
      this.value = tag.getStringOr("value", "");
      if (this.rules.isEmpty() && (tag.contains("packetName") || tag.contains("fieldName"))) {
         this.rules.add(this.init_aM_CachePool());
      }

      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      this.init_V_11();
      ListenDuringPreviousSerializer.readFromTag(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public String getDisplayName() {
      List<WaitPacketMatchAction.Inner_CachePool> activeRules = this.effectiveRules();
      if (activeRules.isEmpty()) {
         return "Wait Packet Match: Any";
      }

      WaitPacketMatchAction.Inner_CachePool first = activeRules.get(0);
      String label = "Wait " + first.direction + " " + first.packetName;
      if (first.fieldName != null && !first.fieldName.isBlank()) {
         label = label + " " + first.fieldName + " " + first.operator + " " + first.value;
      }

      return activeRules.size() == 1 ? label : label + " (+" + (activeRules.size() - 1) + ")";
   }

   @Override
   public String getIcon() {
      return "P";
   }

   @Override
   public String getSaveAs() {
      return this.saveAs;
   }

   @Override
   public void setSaveAs(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public static class Inner_CachePool {
      public WaitPacketMatchAction.Inner_DataUtil direction = WaitPacketMatchAction.Inner_DataUtil.C2S;
      public String packetName = "";
      public String fieldName = "";
      public WaitPacketMatchAction.Inner_StreamUtil operator = WaitPacketMatchAction.Inner_StreamUtil.EXISTS;
      public String value = "";

      public WaitPacketMatchAction.Inner_CachePool copy() {
         WaitPacketMatchAction.Inner_CachePool copy = new WaitPacketMatchAction.Inner_CachePool();
         copy.direction = this.direction;
         copy.packetName = this.packetName == null ? "" : this.packetName;
         copy.fieldName = this.fieldName == null ? "" : this.fieldName;
         copy.operator = this.operator;
         copy.value = this.value == null ? "" : this.value;
         return copy;
      }

      public CompoundTag toTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("direction", this.direction.name());
         tag.putString("packetName", this.packetName == null ? "" : this.packetName);
         tag.putString("fieldName", this.fieldName == null ? "" : this.fieldName);
         tag.putString("operator", this.operator.name());
         tag.putString("value", this.value == null ? "" : this.value);
         return tag;
      }

      static WaitPacketMatchAction.Inner_CachePool init(CompoundTag tag) {
         WaitPacketMatchAction.Inner_CachePool rule = new WaitPacketMatchAction.Inner_CachePool();
         rule.direction = MacroNbtCodecHelper.parseEnum(WaitPacketMatchAction.Inner_DataUtil.class, tag.getStringOr("direction", "C2S"), WaitPacketMatchAction.Inner_DataUtil.C2S);
         rule.packetName = tag.getStringOr("packetName", "");
         rule.fieldName = tag.getStringOr("fieldName", "");
         rule.operator = MacroNbtCodecHelper.parseEnum(WaitPacketMatchAction.Inner_StreamUtil.class, tag.getStringOr("operator", "EXISTS"), WaitPacketMatchAction.Inner_StreamUtil.EXISTS);
         rule.value = tag.getStringOr("value", "");
         return rule;
      }
   }

   public enum Inner_DataUtil {
      C2S,
      S2C,
      ANY;
   }

   public enum Inner_StreamUtil {
      EXISTS,
      EQUALS,
      CONTAINS,
      NOT_EQUALS;
   }
}
