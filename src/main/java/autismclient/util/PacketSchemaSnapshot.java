package autismclient.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.HashedStack;
import net.minecraft.network.HashedStack.ActualItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public record PacketSchemaSnapshot(
   Packet<?> packet, PacketSchemaRegistry.PacketSchema schema, List<PacketFieldSnapshot> fields, boolean init_Z, boolean load_Z, String status, String fallbackNote
) {
   private static final int init_I = 12;
   private static final int MAX_VALUE_LENGTH = 220;
   private static final Object UNREADABLE = new Object();

   public PacketSchemaSnapshot {
      fields = fields == null ? List.of() : List.copyOf(fields);
      status = status == null ? "fallback" : status;
      fallbackNote = fallbackNote == null ? "" : fallbackNote;
   }

   public static PacketSchemaSnapshot capture(Packet<?> packet) {
      if (packet == null) {
         return new PacketSchemaSnapshot(null, null, List.of(), false, false, "fallback", "packet object missing");
      }

      PacketSchemaRegistry.PacketSchema schema = PacketSchemaRegistry.schemaFor(packet.getClass());
      if (schema == null) {
         List<PacketFieldSnapshot> reflected = reflectFields(packet, (PacketSchemaRegistry.PacketSchema)null);
         return new PacketSchemaSnapshot(packet, null, reflected, false, false, "fallback", "no generated schema for packet class");
      }

      if (schema.fields().isEmpty()) {
         if ("UNIT".equalsIgnoreCase(schema.update())) {
            return new PacketSchemaSnapshot(packet, schema, List.of(), true, true, "complete", "");
         }

         List<PacketFieldSnapshot> reflected = reflectFields(packet, schema);
         return new PacketSchemaSnapshot(packet, schema, reflected, true, false, "fallback", "schema has no source field list");
      } else {
         List<PacketFieldSnapshot> values = new ArrayList<>();
         boolean allReadable = true;

         for (PacketSchemaRegistry.FieldSchema field : schema.fields()) {
            Object value = readMember(packet, field.name());
            boolean readable = value != UNREADABLE;
            if (!readable) {
               allReadable = false;
            }

            values.add(
               new PacketFieldSnapshot(field, readable ? value : null, describeValue(readable ? value : null, field, new IdentityHashMap<>(), 0), readable, readable && field.editableCandidate())
            );
         }

         boolean complete = schema.complete() && allReadable && !schema.inherited();
         return new PacketSchemaSnapshot(
            packet,
            schema,
            values,
            true,
            complete,
            complete ? "complete" : "fallback",
            complete ? "" : (schema.inherited() ? "schema inherited from parent packet class" : "one or more schema fields were not readable")
         );
      }
   }

   public Optional<PacketFieldSnapshot> field(String name) {
      if (name != null && !name.isBlank()) {
         for (PacketFieldSnapshot field : this.fields) {
            if (field.name().equals(name)) {
               return Optional.of(field);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   private static Object readMember(Object target, String name) {
      if (target != null && name != null && !name.isBlank()) {
         for (String methodName : accessorNames(name)) {
            try {
               Method method = target.getClass().getMethod(methodName);
               if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
                  method.setAccessible(true);
                  return method.invoke(target);
               }
            } catch (Throwable var6) {
            }
         }

         for (Class<?> current = target.getClass(); current != null && current != Object.class; current = current.getSuperclass()) {
            try {
               Field field = current.getDeclaredField(name);
               if (!Modifier.isStatic(field.getModifiers())) {
                  field.setAccessible(true);
                  return field.get(target);
               }
            } catch (Throwable var5) {
            }
         }

         return UNREADABLE;
      } else {
         return UNREADABLE;
      }
   }

   private static List<String> accessorNames(String name) {
      String capitalized = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
      return List.of(name, "get" + capitalized, "is" + capitalized);
   }

   private static List<PacketFieldSnapshot> reflectFields(Object packet, PacketSchemaRegistry.PacketSchema schema) {
      List<PacketSchemaRegistry.FieldSchema> fieldSchemas = new ArrayList<>();
      if (packet.getClass().isRecord()) {
         for (RecordComponent component : packet.getClass().getRecordComponents()) {
            String type = component.getGenericType().getTypeName();
            fieldSchemas.add(new PacketSchemaRegistry.FieldSchema(component.getName(), type, schemaKind(type), isScalarKind(schemaKind(type))));
         }
      } else {
         List<Field> fields = new ArrayList<>();

         for (Class<?> current = packet.getClass(); current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
               if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                  fields.add(field);
               }
            }
         }

         fields.sort(Comparator.comparing(Field::getName));
         Set<String> seen = new LinkedHashSet<>();

         for (Field field : fields) {
            if (seen.add(field.getName())) {
               String type = field.getGenericType().getTypeName();
               fieldSchemas.add(new PacketSchemaRegistry.FieldSchema(field.getName(), type, schemaKind(type), isScalarKind(schemaKind(type))));
            }
         }
      }

      List<PacketFieldSnapshot> values = new ArrayList<>();

      for (PacketSchemaRegistry.FieldSchema field : fieldSchemas) {
         Object value = readMember(packet, field.name());
         boolean readable = value != UNREADABLE;
         values.add(
            new PacketFieldSnapshot(field, readable ? value : null, describeValue(readable ? value : null, field, new IdentityHashMap<>(), 0), readable, readable && field.editableCandidate())
         );
      }

      return values;
   }

   private static String schemaKind(String type) {
      if (type == null) {
         return "object";
      } else {
         String lower = type.toLowerCase(Locale.ROOT);
         if (Set.of("byte", "short", "int", "long", "float", "double").contains(lower)) {
            return "number";
         } else if ("boolean".equals(lower)) {
            return "boolean";
         } else if ("java.lang.string".equals(lower) || "string".equals(lower)) {
            return "string";
         } else if (lower.contains("itemstack") || lower.contains("hashedstack")) {
            return "item";
         } else if (lower.contains("component")) {
            return "component";
         } else if (lower.contains("identifier") || lower.contains("resourcekey")) {
            return "identifier";
         } else if (lower.contains("holder")) {
            return "holder";
         } else if (lower.contains("blockpos") || lower.contains("chunkpos")) {
            return "position";
         } else if (lower.contains("vec3") || lower.contains("positionmoverotation")) {
            return "vector";
         } else if (lower.contains("uuid")) {
            return "uuid";
         } else if (lower.contains("optional")) {
            return "optional";
         } else if (lower.contains("list") || lower.contains("set")) {
            return "list";
         } else if (lower.contains("map") || lower.contains("int2objectmap")) {
            return "map";
         } else if (lower.contains("bitset")) {
            return "bitset";
         } else {
            return !lower.contains("containerinput") && !lower.contains("relative") ? "object" : "enum";
         }
      }
   }

   private static boolean isScalarKind(String kind) {
      return Set.of("number", "boolean", "string", "identifier", "enum", "uuid").contains(kind);
   }

   private static List<String> describeValue(Object value, PacketSchemaRegistry.FieldSchema field, IdentityHashMap<Object, Boolean> seen, int depth) {
      List<String> lines = new ArrayList<>();
      appendValue(lines, value, seen, depth);
      if (lines.isEmpty()) {
         lines.add("null");
      }

      return lines;
   }

   private static void appendValue(List<String> lines, Object value, IdentityHashMap<Object, Boolean> seen, int depth) {
      if (value == null) {
         lines.add("null");
      } else if (depth > 3) {
         lines.add(truncateText(String.valueOf(value)));
      } else if (value instanceof String string) {
         lines.add(quote(string));
      } else if (value instanceof Number || value instanceof Boolean || value instanceof UUID || value instanceof Enum) {
         lines.add(String.valueOf(value));
      } else if (value instanceof Identifier identifier) {
         lines.add(identifier.toString());
      } else if (value instanceof ResourceKey<?> key) {
         lines.add(key.identifier().toString());
      } else if (value instanceof Component component) {
         lines.add(quote(component.getString()));
      } else if (value instanceof ItemStack stack) {
         lines.add(describeStack(stack));
      } else if (value instanceof HashedStack hashedStack) {
         lines.add(describeHashedStack(hashedStack));
      } else if (value instanceof Holder<?> holder) {
         lines.add(describeHolder(holder));
      } else if (value instanceof BlockPos pos) {
         lines.add("x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ());
      } else if (value instanceof ChunkPos pos) {
         lines.add("x=" + pos.x() + ", z=" + pos.z());
      } else if (value instanceof Vec3 vec) {
         lines.add(String.format(Locale.ROOT, "x=%.5f, y=%.5f, z=%.5f", vec.x, vec.y, vec.z));
      } else if (value instanceof PositionMoveRotation movement) {
         lines.add(
            "position="
               + formatVec3(movement.position())
               + ", delta="
               + formatVec3(movement.deltaMovement())
               + ", yaw="
               + formatFloat(movement.yRot())
               + ", pitch="
               + formatFloat(movement.xRot())
         );
      } else if (value instanceof Optional<?> optional) {
         if (optional.isEmpty()) {
            lines.add("empty");
         } else {
            lines.add("present");
            List<String> nested = new ArrayList<>();
            appendValue(nested, optional.get(), seen, depth + 1);

            for (String line : nested) {
               lines.add("  " + line);
            }
         }
      } else if (value instanceof Int2ObjectMap<?> fastMap) {
         lines.add("map entries=" + fastMap.size());
         int shown = 0;

         for (Entry<?> entry : fastMap.int2ObjectEntrySet()) {
            if (shown >= 12) {
               lines.add("  ... +" + (fastMap.size() - shown) + " more");
               break;
            }

            lines.add("  " + entry.getIntKey() + " -> " + inline(entry.getValue(), seen, depth + 1));
            shown++;
         }
      } else if (value instanceof Map<?, ?> map) {
         lines.add("map entries=" + map.size());
         int shown = 0;

         for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
            if (shown >= 12) {
               lines.add("  ... +" + (map.size() - shown) + " more");
               break;
            }

            lines.add("  " + inline(entry.getKey(), seen, depth + 1) + " -> " + inline(entry.getValue(), seen, depth + 1));
            shown++;
         }
      } else if (value instanceof Collection<?> collection) {
         lines.add("items=" + collection.size());
         int shown = 0;

         for (Object item : collection) {
            if (shown >= 12) {
               lines.add("  ... +" + (collection.size() - shown) + " more");
               break;
            }

            lines.add("  #" + shown + " " + inline(item, seen, depth + 1));
            shown++;
         }
      } else if (value.getClass().isArray()) {
         int length = Array.getLength(value);
         lines.add("array length=" + length);

         for (int i = 0; i < Math.min(length, 12); i++) {
            lines.add("  #" + i + " " + inline(Array.get(value, i), seen, depth + 1));
         }

         if (length > 12) {
            lines.add("  ... +" + (length - 12) + " more");
         }
      } else if (value instanceof Tag tag) {
         lines.add(truncateText(tag.toString()));
      } else if (seen.put(value, Boolean.TRUE) != null) {
         lines.add("<cycle " + value.getClass().getSimpleName() + ">");
      } else {
         lines.add(truncateText(String.valueOf(value)));
         seen.remove(value);
      }
   }

   private static String inline(Object value, IdentityHashMap<Object, Boolean> seen, int depth) {
      List<String> lines = new ArrayList<>();
      appendValue(lines, value, seen, depth);
      return lines.isEmpty() ? "null" : lines.getFirst();
   }

   private static String describeStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String itemId;
         try {
            itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         } catch (Throwable ignored) {
            itemId = String.valueOf(stack.getItem());
         }

         StringBuilder out = new StringBuilder(itemId).append(" x").append(stack.getCount());

         try {
            if (stack.isDamageableItem()) {
               out.append(" damage=").append(stack.getDamageValue()).append('/').append(stack.getMaxDamage());
            }
         } catch (Throwable var6) {
         }

         try {
            Component customName = stack.getCustomName();
            if (customName != null) {
               out.append(" name=").append(quote(customName.getString()));
            }
         } catch (Throwable var5) {
         }

         try {
            String components = String.valueOf(stack.getComponentsPatch());
            if (!components.isBlank() && !"{}".equals(components)) {
               out.append(" components=").append(truncateText(components));
            }
         } catch (Throwable var4) {
         }

         return out.toString();
      } else {
         return "empty";
      }
   }

   private static String describeHashedStack(HashedStack stack) {
      if (stack != null && stack != HashedStack.EMPTY) {
         return stack instanceof ActualItem actual
            ? describeHolder(actual.item()) + " x" + actual.count() + " hashedComponents=" + truncateText(String.valueOf(actual.components()))
            : truncateText(String.valueOf(stack));
      } else {
         return "empty";
      }
   }

   private static String describeHolder(Holder<?> holder) {
      if (holder == null) {
         return "null";
      }

      try {
         Optional<? extends ResourceKey<?>> key = holder.unwrapKey();
         if (key.isPresent()) {
            return key.get().identifier().toString();
         }
      } catch (Throwable var6) {
      }

      Object value;
      try {
         value = holder.value();
      } catch (Throwable ignored) {
         value = holder;
      }

      if (value instanceof Item item) {
         return BuiltInRegistries.ITEM.getKey(item).toString();
      } else {
         try {
            if (value instanceof Registry<?> registry) {
               return String.valueOf(registry.key().identifier());
            }
         } catch (Throwable var4) {
         }

         return truncateText(String.valueOf(value));
      }
   }

   private static String formatVec3(Vec3 vec) {
      return vec == null ? "null" : String.format(Locale.ROOT, "(%.5f, %.5f, %.5f)", vec.x, vec.y, vec.z);
   }

   private static String formatFloat(float value) {
      return String.format(Locale.ROOT, "%.4f", value);
   }

   private static String quote(String value) {
      return value == null ? "\"\"" : "\"" + truncateText(value.replace("\n", "\\n").replace("\r", "\\r")) + "\"";
   }

   private static String truncateText(String value) {
      if (value == null) {
         return "null";
      }

      String trimmed = value.strip();
      return trimmed.length() <= 220 ? trimmed : trimmed.substring(0, 217) + "...";
   }

   public boolean init_Z() {
      return this.packet instanceof ServerboundMovePlayerPacket;
   }

   public boolean load_Z() {
      return this.init_Z;
   }

   public boolean read() {
      return this.load_Z;
   }
}
