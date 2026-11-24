package autismclient.ai;

import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class cA {
   private static final List<String> init = List.of("1", "4", "8", "16", "32", "64");
   private static final List<String> load = List.of(
      "ore",
      "coal",
      "iron",
      "copper",
      "gold",
      "redstone",
      "lapis",
      "diamond",
      "emerald",
      "quartz",
      "ancient_debris",
      "stone",
      "cobblestone",
      "dirt",
      "sand",
      "gravel",
      "clay",
      "log",
      "wood"
   );
   private static final List<String> read = List.of("log", "logs", "wood");
   private static final List<String> write = List.of("north", "south", "east", "west");
   private static List<autismclient.util.J.Inner_EventBus> update;
   private static List<String> sync = List.of();
   private static List<autismclient.util.J.Inner_StreamUtil> flush;
   private static List<String> close = List.of();
   private static volatile List<String> open;
   private static volatile List<String> poll;
   private static volatile List<String> push;
   private static final List<String> pop = List.of("north", "south", "east", "west", "forward", "back", "left", "right");

   private cA() {
   }

   public static cA.Inner_DataUtil init(String remaining) {
      int cut = 0;

      for (int i = remaining.length() - 1; i >= 0; i--) {
         if (Character.isWhitespace(remaining.charAt(i))) {
            cut = i + 1;
            break;
         }
      }

      String before = remaining.substring(0, cut).trim();
      int position = before.isEmpty() ? 0 : before.split("\\s+").length;
      return new cA.Inner_DataUtil(position, remaining.substring(cut), cut);
   }

   public static CompletableFuture<Suggestions> init(String sub, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining();
      cA.Inner_DataUtil cursor = sub.equals("mine") ? load(remaining) : init(remaining);
      int pos = cursor.position();
      SuggestionsBuilder at = builder.createOffset(builder.getStart() + cursor.offset());

      try {
         if (sub.equals("parkour")) {
            List<String> completed = cy.init_List(remaining.substring(0, cursor.offset()));
            List<String> destination = completed.stream().filter(token -> !cy.init_Z(token)).toList();
            if (destination.size() == completed.size()) {
               init(at, cursor.current(), List.of("--no-fall-damage"));
            }

            pos = destination.size();
            if (!destination.isEmpty() && destination.getFirst().equalsIgnoreCase("to")) {
               pos--;
            }
         } else if (sub.equals("goto") && pos >= 1) {
            String trimmed = remaining.trim();
            if (trimmed.regionMatches(true, 0, "to", 0, 2) && (trimmed.length() == 2 || Character.isWhitespace(trimmed.charAt(2)))) {
               pos--;
            }
         }

         init(at, cursor.current(), init(sub, pos));
      } catch (Throwable var8) {
      }

      return at.buildFuture();
   }

   static cA.Inner_DataUtil load(String remaining) {
      int comma = remaining.lastIndexOf(44);
      if (comma < 0 && !remaining.contains("=")) {
         return init(remaining);
      }

      int start = comma + 1;

      while (start < remaining.length() && Character.isWhitespace(remaining.charAt(start))) {
         start++;
      }

      String tail = remaining.substring(start);
      int equals = tail.indexOf(61);
      if (equals >= 0 && tail.substring(equals + 1).chars().noneMatch(Character::isWhitespace)) {
         return new cA.Inner_DataUtil(1, tail.substring(equals + 1), start + equals + 1);
      }

      cA.Inner_DataUtil token = init(tail);
      return new cA.Inner_DataUtil(token.position() == 0 ? 0 : 1, token.current(), start + token.offset());
   }

   public static CompletableFuture<Suggestions> init(SuggestionsBuilder builder) {
      cA.Inner_DataUtil cursor = init(builder.getRemaining());
      SuggestionsBuilder at = builder.createOffset(builder.getStart() + cursor.offset());

      try {
         if (cursor.position() == 0) {
            init(at, cursor.current(), poll());
         } else {
            init(at, cursor.current(), init(builder.getRemaining().trim().split("\\s+")[0], cursor.current()));
         }
      } catch (Throwable var4) {
      }

      return at.buildFuture();
   }

   public static CompletableFuture<Suggestions> load(SuggestionsBuilder builder) {
      try {
         Set<String> families = new TreeSet<>();

         for (AiCatalog.Entry entry : o.init_AiCatalog_Snapshot().entries()) {
            if (o.load_Z(entry.contract().id())) {
               families.add(entry.contract().family());
            }
         }

         init(builder, builder.getRemaining(), families);
      } catch (Throwable var4) {
      }

      return builder.buildFuture();
   }

   static List<String> init(String sub, int position) {
      List var10000;
      switch (sub) {
         case "craft":
            var10000 = position == 0 ? init() : (position == 1 ? init : List.of());
            break;
         case "obtain":
         case "collect":
            var10000 = position == 0 ? read() : (position == 1 ? init : List.of());
            break;
         case "smelt":
            var10000 = position == 0 ? load() : (position == 1 ? init : List.of());
            break;
         case "mine":
            var10000 = position == 0 ? write() : (position == 1 ? init : List.of());
            break;
         case "chop":
            var10000 = position == 0 ? update() : (position == 1 ? init : List.of());
            break;
         case "farm":
            var10000 = position == 0 ? List.of("wheat", "carrots", "potatoes", "beetroots") : (position == 1 ? List.of("24", "48", "80", "160") : List.of());
            break;
         case "fight":
            var10000 = position == 0 ? sync() : (position == 1 ? init : List.of());
            break;
         case "follow":
            var10000 = position == 0 ? sync() : List.of();
            break;
         case "goto":
         case "parkour":
            var10000 = position == 0
               ? init(sub.equals("goto"))
               : (
                  position == 1
                     ? close()
                     : (position == 2 ? List.of("~") : (position == 3 ? List.of("1", "2", "3", "4") : (position == 4 ? open() : List.of())))
               );
            break;
         case "fill":
            var10000 = position < 6 ? List.of("~") : (position == 6 ? flush() : (position == 7 ? List.of("hollow", "solid") : List.of()));
            break;
         case "dig":
            var10000 = position == 0 ? List.of("tunnel") : (position == 1 ? write : (position == 2 ? List.of("8", "16", "32", "64") : List.of()));
            break;
         case "trade":
            var10000 = position == 0 ? List.of("1", "2", "3", "4", "5", "6") : List.of();
            break;
         case "events":
            var10000 = position == 0 ? List.of("0") : List.of();
            break;
         case "build":
            var10000 = position == 0 ? push() : List.of();
            break;
         default:
            var10000 = List.of();
      }

      return var10000;
   }

   static void init(SuggestionsBuilder builder, String current, Collection<String> values) {
      String needle = current.toLowerCase(Locale.ROOT);

      for (String value : values) {
         String lower = value.toLowerCase(Locale.ROOT);
         if (lower.startsWith(needle)) {
            builder.suggest(value);
         } else {
            int colon = lower.indexOf(58);
            if (colon >= 0 && needle.indexOf(58) < 0 && lower.substring(colon + 1).startsWith(needle)) {
               builder.suggest(value);
            }
         }
      }
   }

   static String init(Identifier id) {
      return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
   }

   static synchronized List<String> init() {
      Minecraft mc = Minecraft.getInstance();
      List<autismclient.util.J.Inner_EventBus> plans = autismclient.util.J.load_List(mc);
      if (plans == update) {
         return sync;
      }

      Set<String> ids = new TreeSet<>();

      for (autismclient.util.J.Inner_EventBus plan : plans) {
         ids.add(init(BuiltInRegistries.ITEM.getKey(plan.init_Item())));
      }

      for (autismclient.util.J.Inner_FileCache plan : autismclient.util.J.write(mc)) {
         ids.add(init(BuiltInRegistries.ITEM.getKey(plan.init_Item())));
      }

      sync = List.copyOf(ids);
      update = plans;
      return sync;
   }

   static synchronized List<String> load() {
      List<autismclient.util.J.Inner_StreamUtil> plans = autismclient.util.J.read(Minecraft.getInstance());
      if (plans == flush) {
         return close;
      }

      Set<String> ids = new TreeSet<>();

      for (autismclient.util.J.Inner_StreamUtil plan : plans) {
         ids.add(init(BuiltInRegistries.ITEM.getKey(plan.init_Item())));
      }

      close = List.copyOf(ids);
      flush = plans;
      return close;
   }

   static List<String> read() {
      List<String> cached = push;
      if (cached != null) {
         return cached;
      }

      Set<String> ids = new TreeSet<>(load);
      ids.addAll(read);

      for (Item item : BuiltInRegistries.ITEM) {
         if (item != Items.AIR) {
            ids.add(init(BuiltInRegistries.ITEM.getKey(item)));
         }
      }

      return push = List.copyOf(ids);
   }

   static List<String> write() {
      List<String> cached = open;
      if (cached != null) {
         return cached;
      }

      Set<String> ids = new TreeSet<>(load);
      ids.addAll(autismclient.commands.StreamUtil.init_List());
      return open = List.copyOf(ids);
   }

   static boolean init(Block block) {
      return autismclient.commands.StreamUtil.init_Z_Block(block);
   }

   static List<String> update() {
      List<String> cached = poll;
      if (cached != null) {
         return cached;
      }

      Set<String> ids = new TreeSet<>(read);

      for (Block block : BuiltInRegistries.BLOCK) {
         Identifier key = BuiltInRegistries.BLOCK.getKey(block);
         String path = key.getPath();
         if (path.endsWith("_log") || path.endsWith("_wood") || path.endsWith("_stem") || path.endsWith("_hyphae")) {
            ids.add(init(key));
         }
      }

      return poll = List.copyOf(ids);
   }

   static List<String> sync() {
      List<String> ids = new ArrayList<>(autismclient.commands.StreamUtil.read().size() + 1);
      ids.add("hostiles");
      ids.addAll(autismclient.commands.StreamUtil.read());
      return ids;
   }

   static List<String> flush() {
      return autismclient.commands.StreamUtil.load();
   }

   private static List<String> init(boolean suggestTo) {
      Set<String> out = new TreeSet<>(pop);
      out.add("~");
      if (suggestTo) {
         out.add("to");
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.player != null) {
            for (AutismWaypoints.Waypoint wp : AutismWaypoints.init_AutismWaypoints().init_List_Str(AuditWaypointScope.current(mc))) {
               if (!wp.name().contains(" ")) {
                  out.add(wp.name());
               }
            }
         }
      } catch (Throwable var5) {
      }

      return new ArrayList<>(out);
   }

   static List<String> close() {
      return List.of("~", "5", "10", "20", "50", "100");
   }

   static List<String> open() {
      return new ArrayList<>(new TreeSet<>(AiToolSettings.STRATEGIES));
   }

   static List<String> poll() {
      List<String> ids = new ArrayList<>();

      for (AiCatalog.Entry entry : o.init_AiCatalog_Snapshot().entries()) {
         if (o.load_Z(entry.contract().id())) {
            ids.add(entry.contract().id());
         }
      }

      return ids;
   }

   static List<String> init(String rawId, String current) {
      AiCatalog.Entry entry = o.init_AiCatalog_Entry(o.init_Str(rawId));
      if (entry == null) {
         return List.of();
      }

      JsonObject schema;
      try {
         schema = ag.init_JsonObject_Str(entry.contract().inputSchema());
      } catch (Exception e) {
         return List.of();
      }

      if (!schema.has("properties")) {
         return List.of();
      }

      JsonObject properties = schema.getAsJsonObject("properties");
      List<String> out = new ArrayList<>();
      int eq = current.indexOf(61);
      if (eq >= 0) {
         String key = current.substring(0, eq);
         if (properties.has(key) && properties.get(key).isJsonObject()) {
            JsonObject property = properties.getAsJsonObject(key);
            if (property.has("enum")) {
               for (JsonElement value : property.getAsJsonArray("enum")) {
                  out.add(key + "=" + value.getAsString());
               }
            } else if ("boolean".equals(ag.init_Str_JsonObject(property, "type", ""))) {
               out.add(key + "=true");
               out.add(key + "=false");
            }

            return out;
         } else {
            return out;
         }
      } else {
         for (Entry<String, JsonElement> property : properties.entrySet()) {
            out.add(property.getKey() + "=");
         }

         return out;
      }
   }

   static List<String> push() {
      Set<String> names = new TreeSet<>();
      init(o.load_Path(), names, ".json", true);
      init(o.read_Path(), names, ".schem", false);
      init(o.read_Path(), names, ".schematic", false);
      init(o.read_Path(), names, ".litematic", false);
      return List.copyOf(names);
   }

   private static void init(Path directory, Set<String> out, String extension, boolean stripExtension) {
      try {
         if (directory == null || !Files.isDirectory(directory)) {
            return;
         }

         try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(x$0 -> Files.isRegularFile(x$0))
               .map(f -> f.getFileName().toString())
               .filter(f -> f.endsWith(extension))
               .map(f -> stripExtension ? f.substring(0, f.length() - extension.length()) : f)
               .forEach(out::add);
         }
      } catch (Exception var9) {
      }
   }

   public record Inner_DataUtil(int position, String current, int offset) {
   }
}
