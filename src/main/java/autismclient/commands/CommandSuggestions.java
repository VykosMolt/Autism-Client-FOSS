package autismclient.commands;

import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CommandSuggestions {
   private static volatile List<String> init_List;
   private static volatile List<String> load_List;
   private static volatile List<String> read;
   private static volatile String[][] init_StrArr2;
   private static volatile String[][] load_StrArr2;

   private CommandSuggestions() {
   }

   public static <S> CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, String... values) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String value : values) {
         if (value != null && value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(value);
         }
      }

      return builder.buildFuture();
   }

   public static <S> CompletableFuture<Suggestions> init_CompletableFuture_CommandContext_SuggestionsBuilder(
      CommandContext<S> ignored, SuggestionsBuilder builder
   ) {
      return suggest(builder, "on", "off", "toggle", "enable", "disable");
   }

   public static <S> CompletableFuture<Suggestions> load(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "50", "100", "-1", "-5", "-10", "-50", "-100");
   }

   public static <S> CompletableFuture<Suggestions> read(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "20", "50");
   }

   public static <S> CompletableFuture<Suggestions> write(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "20", "40", "100");
   }

   public static <S> CompletableFuture<Suggestions> update(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "2", "4", "8", "20");
   }

   public static <S> CompletableFuture<Suggestions> sync(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "16", "32", "64", "1000", "10000", "100000");
   }

   public static <S> CompletableFuture<Suggestions> flush(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "true", "false");
   }

   public static <S> CompletableFuture<Suggestions> close(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "50", "100", "1000");
   }

   public static <S> CompletableFuture<Suggestions> open(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "0", "5", "10", "20", "40", "100", "200");
   }

   public static <S> CompletableFuture<Suggestions> poll(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "15", "30", "45", "60", "90", "120");
   }

   public static <S> CompletableFuture<Suggestions> push(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "5", "10", "20", "50");
   }

   public static <S> CompletableFuture<Suggestions> pop(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "~");
   }

   public static <S> CompletableFuture<Suggestions> tick(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining();
      String bare = (remaining.startsWith("\"") ? remaining.substring(1) : remaining).toLowerCase(Locale.ROOT);
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         for (AutismWaypoints.Waypoint wp : AutismWaypoints.getInstance().list(AuditWaypointScope.current(mc))) {
            String name = wp.name();
            if (name.toLowerCase(Locale.ROOT).startsWith(bare)) {
               builder.suggest(name.contains(" ") ? "\"" + name + "\"" : name);
            }
         }
      }

      return builder.buildFuture();
   }

   public static <S> CompletableFuture<Suggestions> run(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining();
      if (remaining.indexOf(32) >= 0) {
         return builder.buildFuture();
      }

      String prefix = CommandManager.getPrefix();
      String lower = remaining.toLowerCase(Locale.ROOT);

      for (Command command : CommandManager.getCommands()) {
         String line = prefix + CommandManager.getPrimaryName(command);
         if (line.toLowerCase(Locale.ROOT).startsWith(lower)) {
            builder.suggest(line);
         }
      }

      return builder.buildFuture();
   }

   public static boolean isPlaceableBlock(Block block) {
      BlockState state = block.defaultBlockState();
      return !state.isAir() && !(block instanceof LiquidBlock) && block.asItem() != Items.AIR && block.defaultDestroyTime() >= 0.0F;
   }

   public static List<String> blockIds() {
      List<String> cached = init_List;
      if (cached != null) {
         return cached;
      }

      TreeSet<String> ids = new TreeSet<>();

      for (Block block : BuiltInRegistries.BLOCK) {
         if (isPlaceableBlock(block)) {
            ids.add(RegistrySuggestions.init(BuiltInRegistries.BLOCK.getKey(block)));
         }
      }

      return init_List = List.copyOf(ids);
   }

   public static List<String> load() {
      List<String> cached = load_List;
      if (cached != null) {
         return cached;
      }

      TreeSet<String> ids = new TreeSet<>();

      for (Block block : BuiltInRegistries.BLOCK) {
         BlockState state = block.defaultBlockState();
         if (!state.isAir() && !(block instanceof LiquidBlock) && block.asItem() != Items.AIR) {
            ids.add(RegistrySuggestions.init(BuiltInRegistries.BLOCK.getKey(block)));
         }
      }

      return load_List = List.copyOf(ids);
   }

   public static List<String> read() {
      List<String> cached = read;
      if (cached != null) {
         return cached;
      }

      TreeSet<String> ids = new TreeSet<>();

      for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
         if (type.getCategory() != MobCategory.MISC) {
            ids.add(RegistrySuggestions.init(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
         }
      }

      return read = List.copyOf(ids);
   }

   public static <S> CompletableFuture<Suggestions> get(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String id : load()) {
         if (id.startsWith(remaining)) {
            builder.suggest(id);
         }
      }

      return builder.buildFuture();
   }

   public static <S> CompletableFuture<Suggestions> set(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "survival", "creative", "adventure", "spectator", "reset", "0", "1", "2", "3");
   }

   public static <S> CompletableFuture<Suggestions> put(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "survival", "creative", "adventure", "spectator", "0", "1", "2", "3");
   }

   public static <S> CompletableFuture<Suggestions> add(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, ".", "%", "-", "_", "*", "#", "@", "&", "=");
   }

   public static <S> CompletableFuture<Suggestions> clear(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      String[][] cache = init_StrArr2;
      if (cache == null) {
         cache = init_StrArr2 = init_StrArr2_Set(BuiltInRegistries.ITEM.keySet());
      }

      for (String[] entry : cache) {
         if (entry[1].startsWith(remaining)) {
            builder.suggest(entry[0]);
         }
      }

      return builder.buildFuture();
   }

   public static <S> CompletableFuture<Suggestions> reset(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      String[][] cache = load_StrArr2;
      if (cache == null) {
         cache = load_StrArr2 = init_StrArr2_Set(BuiltInRegistries.BLOCK.keySet());
      }

      for (String[] entry : cache) {
         if (entry[1].startsWith(remaining)) {
            builder.suggest(entry[0]);
         }
      }

      return builder.buildFuture();
   }

   private static String[][] init_StrArr2_Set(Set<Identifier> ids) {
      List<String[]> entries = new ArrayList<>(ids.size() * 2);

      for (Identifier id : ids) {
         String full = id.toString();
         String shortId = id.getNamespace().equals("minecraft") ? id.getPath() : full;
         entries.add(new String[]{full, full.toLowerCase(Locale.ROOT)});
         if (!shortId.equals(full)) {
            entries.add(new String[]{shortId, shortId.toLowerCase(Locale.ROOT)});
         }
      }

      return entries.toArray(new String[0][]);
   }
}
