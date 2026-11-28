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
   private static volatile List<String> blockIdsCache;
   private static volatile List<String> placeableBlockIdsCache;
   private static volatile List<String> entityIdsCache;
   private static volatile String[][] itemIdIndex;
   private static volatile String[][] blockIdIndex;

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

   public static <S> CompletableFuture<Suggestions> suggestDelayState(
      CommandContext<S> ignored, SuggestionsBuilder builder
   ) {
      return suggest(builder, "on", "off", "toggle", "enable", "disable");
   }

   public static <S> CompletableFuture<Suggestions> suggestVclipBlocks(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "50", "100", "-1", "-5", "-10", "-50", "-100");
   }

   public static <S> CompletableFuture<Suggestions> suggestVclipSegments(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "20", "50");
   }

   public static <S> CompletableFuture<Suggestions> suggestMaxPackets(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "20", "40", "100");
   }

   public static <S> CompletableFuture<Suggestions> suggestDamageAmount(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "2", "4", "8", "20");
   }

   public static <S> CompletableFuture<Suggestions> suggestCount(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "16", "32", "64", "1000", "10000", "100000");
   }

   public static <S> CompletableFuture<Suggestions> suggestBoolean(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "true", "false");
   }

   public static <S> CompletableFuture<Suggestions> suggestTimes(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "1", "5", "10", "50", "100", "1000");
   }

   public static <S> CompletableFuture<Suggestions> suggestDelayTicks(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "0", "5", "10", "20", "40", "100", "200");
   }

   public static <S> CompletableFuture<Suggestions> suggestMinutes(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "15", "30", "45", "60", "90", "120");
   }

   public static <S> CompletableFuture<Suggestions> suggestCounts(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "5", "10", "20", "50");
   }

   public static <S> CompletableFuture<Suggestions> suggestCoordinate(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "~");
   }

   public static <S> CompletableFuture<Suggestions> suggestWaypointName(CommandContext<S> ignored, SuggestionsBuilder builder) {
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

   public static <S> CompletableFuture<Suggestions> suggestCommand(CommandContext<S> ignored, SuggestionsBuilder builder) {
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
      List<String> cached = blockIdsCache;
      if (cached != null) {
         return cached;
      }

      TreeSet<String> ids = new TreeSet<>();

      for (Block block : BuiltInRegistries.BLOCK) {
         if (isPlaceableBlock(block)) {
            ids.add(RegistrySuggestions.shortId(BuiltInRegistries.BLOCK.getKey(block)));
         }
      }

      return blockIdsCache = List.copyOf(ids);
   }

   public static List<String> placeableBlockIds() {
      List<String> cached = placeableBlockIdsCache;
      if (cached != null) {
         return cached;
      }

      TreeSet<String> ids = new TreeSet<>();

      for (Block block : BuiltInRegistries.BLOCK) {
         BlockState state = block.defaultBlockState();
         if (!state.isAir() && !(block instanceof LiquidBlock) && block.asItem() != Items.AIR) {
            ids.add(RegistrySuggestions.shortId(BuiltInRegistries.BLOCK.getKey(block)));
         }
      }

      return placeableBlockIdsCache = List.copyOf(ids);
   }

   public static List<String> entityIds() {
      List<String> cached = entityIdsCache;
      if (cached != null) {
         return cached;
      }

      TreeSet<String> ids = new TreeSet<>();

      for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
         if (type.getCategory() != MobCategory.MISC) {
            ids.add(RegistrySuggestions.shortId(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
         }
      }

      return entityIdsCache = List.copyOf(ids);
   }

   public static <S> CompletableFuture<Suggestions> suggestBlockId(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String id : placeableBlockIds()) {
         if (id.startsWith(remaining)) {
            builder.suggest(id);
         }
      }

      return builder.buildFuture();
   }

   public static <S> CompletableFuture<Suggestions> suggestFakeGamemode(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "survival", "creative", "adventure", "spectator", "reset", "0", "1", "2", "3");
   }

   public static <S> CompletableFuture<Suggestions> suggestGamemode(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, "survival", "creative", "adventure", "spectator", "0", "1", "2", "3");
   }

   public static <S> CompletableFuture<Suggestions> suggestPrefix(CommandContext<S> ignored, SuggestionsBuilder builder) {
      return suggest(builder, ".", "%", "-", "_", "*", "#", "@", "&", "=");
   }

   public static <S> CompletableFuture<Suggestions> suggestItemId(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      String[][] cache = itemIdIndex;
      if (cache == null) {
         cache = itemIdIndex = buildIdIndex(BuiltInRegistries.ITEM.keySet());
      }

      for (String[] entry : cache) {
         if (entry[1].startsWith(remaining)) {
            builder.suggest(entry[0]);
         }
      }

      return builder.buildFuture();
   }

   public static <S> CompletableFuture<Suggestions> suggestBlocks(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      String[][] cache = blockIdIndex;
      if (cache == null) {
         cache = blockIdIndex = buildIdIndex(BuiltInRegistries.BLOCK.keySet());
      }

      for (String[] entry : cache) {
         if (entry[1].startsWith(remaining)) {
            builder.suggest(entry[0]);
         }
      }

      return builder.buildFuture();
   }

   private static String[][] buildIdIndex(Set<Identifier> ids) {
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
