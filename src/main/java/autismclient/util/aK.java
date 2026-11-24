package autismclient.util;

import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.slf4j.Logger;

final class aK {
   private static final Logger init_Logger = LogUtils.getLogger();
   private static final Object init_Obj = new Object();
   private static final FileToIdConverter init_FileToIdConverter = FileToIdConverter.registry(Registries.RECIPE);
   private static RegistryAccess init_RegistryAccess;
   private static FeatureFlagSet init_FeatureFlagSet;
   private static aK.Inner_CachePool init_aK_CachePool;
   private static CompletableFuture<aK.Inner_CachePool> init_CompletableFuture;
   private static Object load_Obj;
   private static aK.Inner_CachePool load_aK_CachePool;

   private aK() {
   }

   static List<aK.Inner_StreamUtil> init_List(Minecraft mc) {
      return init_aK_CachePool(mc).init_List();
   }

   static List<aK.Inner_DataUtil> load_List(Minecraft mc) {
      return init_aK_CachePool(mc).load_List();
   }

   static aK.Inner_CachePool init_aK_CachePool(Minecraft mc) {
      if (mc != null && mc.level != null) {
         aK.Inner_CachePool server = load_aK_CachePool(mc);
         return server != null && !server.init_List().isEmpty()
            ? server
            : init(init_RegistryAccess(mc), init_FeatureFlagSet(mc), SlotDisplayContext.fromLevel(mc.level), mc);
      } else {
         return aK.Inner_CachePool.init_aK_CachePool;
      }
   }

   static void init_V(Minecraft mc) {
      if (mc != null && mc.level != null) {
         if (mc.getSingleplayerServer() != null) {
            Thread thread = new Thread(() -> {
               try {
                  load_aK_CachePool(mc);
               } catch (RuntimeException var2x) {
               }
            }, "Autism-Recipes");
            thread.setDaemon(true);
            thread.start();
         } else {
            RegistryAccess access = init_RegistryAccess(mc);
            FeatureFlagSet features = init_FeatureFlagSet(mc);
            ContextMap slotContext = SlotDisplayContext.fromLevel(mc.level);
            synchronized (init_Obj) {
               if (!init(access, features) || init_aK_CachePool == null && init_CompletableFuture == null) {
                  init_CompletableFuture(access, features, slotContext);
               }
            }
         }
      }
   }

   static List<aK.Inner_StreamUtil> read(Minecraft mc) {
      if (mc != null && mc.level != null) {
         aK.Inner_CachePool server = load_aK_CachePool(mc);
         if (server != null && !server.init_List().isEmpty()) {
            return server.init_List();
         }

         RegistryAccess access = init_RegistryAccess(mc);
         FeatureFlagSet features = init_FeatureFlagSet(mc);
         synchronized (init_Obj) {
            if (init(access, features)) {
               if (init_aK_CachePool != null && !init_aK_CachePool.init_List().isEmpty()) {
                  return init_aK_CachePool.init_List();
               }

               if (init_CompletableFuture != null) {
                  return null;
               }
            }

            init_CompletableFuture(access, features, SlotDisplayContext.fromLevel(mc.level));
            return null;
         }
      } else {
         return null;
      }
   }

   static List<aK.Inner_StreamUtil> init_List(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      return init(access, features, slotContext, null).init_List();
   }

   static int init_I(Minecraft mc) {
      return mc == null ? -1 : init_FileToIdConverter.listMatchingResources(mc.getResourceManager()).size();
   }

   private static RegistryAccess init_RegistryAccess(Minecraft mc) {
      return mc.getConnection() != null ? mc.getConnection().registryAccess() : mc.level.registryAccess();
   }

   private static FeatureFlagSet init_FeatureFlagSet(Minecraft mc) {
      return mc.getConnection() != null ? mc.getConnection().enabledFeatures() : mc.level.enabledFeatures();
   }

   private static boolean init(RegistryAccess access, FeatureFlagSet features) {
      return init_RegistryAccess == access && Objects.equals(init_FeatureFlagSet, features);
   }

   private static CompletableFuture<aK.Inner_CachePool> init_CompletableFuture(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      init_RegistryAccess = access;
      init_FeatureFlagSet = features;
      init_aK_CachePool = null;
      CompletableFuture<aK.Inner_CachePool> load = CompletableFuture.supplyAsync(() -> init_aK_CachePool(access, features, slotContext), runnable -> {
         Thread thread = new Thread(runnable, "Autism-Recipes");
         thread.setDaemon(true);
         thread.start();
      });
      init_CompletableFuture = load;
      load.whenComplete((snapshot, error) -> {
         synchronized (init_Obj) {
            if (init_CompletableFuture == load) {
               init_CompletableFuture = null;
               if (snapshot != null) {
                  init_aK_CachePool = snapshot;
               }
            }
         }
      });
      return load;
   }

   private static aK.Inner_CachePool init(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext, Minecraft mc) {
      if (access == null) {
         return aK.Inner_CachePool.init_aK_CachePool;
      }

      CompletableFuture<aK.Inner_CachePool> pending;
      synchronized (init_Obj) {
         if (init(access, features) && init_aK_CachePool != null) {
            return init_aK_CachePool;
         }

         pending = init(access, features) && init_CompletableFuture != null ? init_CompletableFuture : init_CompletableFuture(access, features, slotContext);
      }

      aK.Inner_CachePool loaded;
      try {
         loaded = pending.join();
      } catch (RuntimeException e) {
         loaded = null;
      }

      if (loaded != null && !loaded.init_List().isEmpty()) {
         return loaded;
      } else {
         return mc == null ? aK.Inner_CachePool.init_aK_CachePool : init(mc.getResourceManager(), access, features, slotContext);
      }
   }

   static aK.Inner_CachePool init_aK_CachePool(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      long started = System.nanoTime();
      Map<Identifier, IoSupplier<InputStream>> files = new TreeMap<>(Comparator.comparing(Identifier::toString));
      VanillaPackResources pack = ServerPacksSource.createVanillaPackSource();

      try {
         Compat263.vanillaListResources(pack, PackType.SERVER_DATA, "minecraft", "recipe", files::putIfAbsent);
         aK.Inner_CachePool parsed = init(files, access, features, slotContext, "client jar");
         init_Logger.info(
            "[Autism] recipes: {} crafting + {} cooking from the client jar ({} files, {} skipped) in {} ms",
            parsed.init_List().size(),
            parsed.load_List().size(),
            parsed.init_I(),
            parsed.load_I(),
            (System.nanoTime() - started) / 1000000L
         );
         Compat263.vanillaClose(pack);
         return parsed;
      } catch (RuntimeException e) {
         init_Logger.warn("[Autism] recipes: the client jar's data pack could not be read: {}", e.toString());
         return aK.Inner_CachePool.init_aK_CachePool;
      }
   }

   private static aK.Inner_CachePool init(
      Map<Identifier, IoSupplier<InputStream>> files, RegistryAccess access, FeatureFlagSet features, ContextMap slotContext, String source
   ) {
      DynamicOps<JsonElement> ops = access.createSerializationContext(JsonOps.INSTANCE);
      LinkedHashMap<String, aK.Inner_StreamUtil> crafting = new LinkedHashMap<>();
      List<aK.Inner_DataUtil> cooking = new ArrayList<>();
      int failed = 0;

      for (Entry<Identifier, IoSupplier<InputStream>> entry : files.entrySet()) {
         Identifier recipeId = init_FileToIdConverter.fileToId(entry.getKey());

         try (Reader reader = new BufferedReader(new InputStreamReader(entry.getValue().get(), StandardCharsets.UTF_8))) {
            JsonElement json = JsonParser.parseReader(reader);
            Recipe<?> decoded = Recipe.CODEC.parse(ops, json).getOrThrow(JsonParseException::new).value();
            if (decoded instanceof CraftingRecipe craftingRecipe) {
               for (aK.Inner_StreamUtil recipe : init(recipeId, craftingRecipe, features, slotContext)) {
                  crafting.put(recipe.init_Str, recipe);
               }
            } else if (decoded instanceof AbstractCookingRecipe cookingRecipe) {
               aK.Inner_DataUtil recipe = init(recipeId, cookingRecipe, decoded, slotContext);
               if (recipe != null) {
                  cooking.add(recipe);
               }
            }
         } catch (IOException | RuntimeException e) {
            failed++;
         }
      }

      List<aK.Inner_StreamUtil> craftingList = new ArrayList<>(crafting.values());
      craftingList.sort(Comparator.comparing(recipex -> recipex.init_Str, String.CASE_INSENSITIVE_ORDER));
      cooking.sort(Comparator.comparing(aK.Inner_DataUtil::init_Str, String.CASE_INSENSITIVE_ORDER));
      return new aK.Inner_CachePool(List.copyOf(craftingList), List.copyOf(cooking), source, files.size(), failed);
   }

   private static aK.Inner_CachePool init(ResourceManager manager, RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      if (manager == null) {
         return aK.Inner_CachePool.init_aK_CachePool;
      }

      Map<Identifier, IoSupplier<InputStream>> files = new TreeMap<>(Comparator.comparing(Identifier::toString));

      for (Entry<Identifier, Resource> entry : init_FileToIdConverter.listMatchingResources(manager).entrySet()) {
         Resource resource = entry.getValue();
         files.put(entry.getKey(), resource::open);
      }

      return files.isEmpty() ? aK.Inner_CachePool.init_aK_CachePool : init(files, access, features, slotContext, "client resources");
   }

   private static aK.Inner_CachePool load_aK_CachePool(Minecraft mc) {
      IntegratedServer server = mc.getSingleplayerServer();
      if (server == null) {
         return null;
      }

      RecipeManager manager;
      try {
         manager = server.getRecipeManager();
      } catch (RuntimeException e) {
         return null;
      }

      if (manager == null) {
         return null;
      }

      synchronized (init_Obj) {
         if (load_Obj == manager && load_aK_CachePool != null) {
            return load_aK_CachePool;
         }

         try {
            load_aK_CachePool = init(manager.getRecipes(), init_FeatureFlagSet(mc), SlotDisplayContext.fromLevel(mc.level));
            load_Obj = manager;
         } catch (RuntimeException e) {
            return null;
         }

         return load_aK_CachePool;
      }
   }

   static aK.Inner_CachePool init(Collection<RecipeHolder<?>> holders, FeatureFlagSet enabledFeatures, ContextMap slotContext) {
      LinkedHashMap<String, aK.Inner_StreamUtil> craftingByKey = new LinkedHashMap<>();
      List<aK.Inner_DataUtil> cooking = new ArrayList<>();

      for (RecipeHolder<?> holder : holders) {
         if (holder != null && holder.id() != null && holder.value() != null) {
            Identifier recipeId = holder.id().identifier();
            if (recipeId != null) {
               Recipe<?> decoded = holder.value();
               if (decoded instanceof CraftingRecipe crafting) {
                  for (aK.Inner_StreamUtil recipe : init(recipeId, crafting, enabledFeatures, slotContext)) {
                     craftingByKey.put(recipe.init_Str, recipe);
                  }
               } else if (decoded instanceof AbstractCookingRecipe cookingRecipe) {
                  aK.Inner_DataUtil recipe = init(recipeId, cookingRecipe, decoded, slotContext);
                  if (recipe != null) {
                     cooking.add(recipe);
                  }
               }
            }
         }
      }

      List<aK.Inner_StreamUtil> crafting = new ArrayList<>(craftingByKey.values());
      crafting.sort(Comparator.comparing(recipex -> recipex.init_Str, String.CASE_INSENSITIVE_ORDER));
      cooking.sort(Comparator.comparing(aK.Inner_DataUtil::init_Str, String.CASE_INSENSITIVE_ORDER));
      return new aK.Inner_CachePool(List.copyOf(crafting), List.copyOf(cooking));
   }

   private static aK.Inner_DataUtil init(Identifier recipeId, AbstractCookingRecipe cooking, Recipe<?> decoded, ContextMap slotContext) {
      List<ItemStack> inputStacks = cooking.input().display().resolveForStacks(slotContext);
      LinkedHashMap<Identifier, Item> inputs = new LinkedHashMap<>();

      for (ItemStack stack : inputStacks) {
         if (stack != null && !stack.isEmpty()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null) {
               inputs.put(id, stack.getItem());
            }
         }
      }

      ItemStack result = ItemStack.EMPTY;

      for (RecipeDisplay display : cooking.display()) {
         ItemStack candidate = display.result().resolveForFirstStack(slotContext);
         if (candidate != null && !candidate.isEmpty()) {
            result = candidate;
            break;
         }
      }

      return !inputs.isEmpty() && !result.isEmpty()
         ? new aK.Inner_DataUtil(recipeId.toString(), new ArrayList<>(inputs.values()), result, init(decoded), cooking.experience(), cooking.cookingTime())
         : null;
   }

   private static String init(Recipe<?> recipe) {
      String name = recipe.getClass().getSimpleName().toLowerCase(Locale.ROOT);
      if (name.contains("blast")) {
         return "minecraft:blast_furnace";
      } else if (name.contains("smoking")) {
         return "minecraft:smoker";
      } else {
         return name.contains("campfire") ? "minecraft:campfire" : "minecraft:furnace";
      }
   }

   private static <T> List<T> init_List(List<T> values) {
      return values != null && !values.isEmpty() ? Collections.unmodifiableList(new ArrayList<>(values)) : List.of();
   }

   static aK.Inner_StreamUtil init(
      String recipeKey, RecipeDisplay display, Optional<List<Ingredient>> craftingRequirements, FeatureFlagSet enabledFeatures, ContextMap slotContext
   ) {
      if (recipeKey == null || recipeKey.isBlank() || display == null || slotContext == null) {
         return null;
      }

      if (!display.isEnabled(enabledFeatures)) {
         return null;
      }

      ItemStack result = display.result().resolveForFirstStack(slotContext);
      if (result != null && !result.isEmpty() && result.isItemEnabled(enabledFeatures)) {
         boolean shaped = display instanceof ShapedCraftingRecipeDisplay;
         if (!shaped && !(display instanceof ShapelessCraftingRecipeDisplay)) {
            return null;
         }

         List<Ingredient> explicitRequirements = craftingRequirements != null && craftingRequirements.isPresent()
            ? load(craftingRequirements.get())
            : List.of();
         int width;
         int height;
         List<Ingredient> gridIngredients;
         List<Ingredient> requirements;
         if (display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
            width = shapedDisplay.width();
            height = shapedDisplay.height();
            gridIngredients = init(shapedDisplay.ingredients(), slotContext);
            requirements = explicitRequirements.isEmpty() ? load(gridIngredients) : explicitRequirements;
         } else {
            List<SlotDisplay> displays = init(display);
            width = Math.max(1, displays.size());
            height = 1;
            requirements = explicitRequirements.isEmpty() ? load(init(displays, slotContext)) : explicitRequirements;
            gridIngredients = new ArrayList<>(requirements);
         }

         if (width > 0 && height > 0 && !requirements.isEmpty()) {
            if (requirements.stream().anyMatch(ingredient -> ingredient == null || ingredient.isEmpty())) {
               return null;
            }

            String signature = init(result, shaped, width, height, init(shaped, gridIngredients, requirements, slotContext));
            return new aK.Inner_StreamUtil(recipeKey, result, width, height, shaped, gridIngredients, requirements, signature);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static List<aK.Inner_StreamUtil> init(Identifier recipeId, CraftingRecipe recipe, FeatureFlagSet enabledFeatures, ContextMap slotContext) {
      if (recipe != null && !recipe.isSpecial() && !recipe.placementInfo().isImpossibleToPlace()) {
         List<aK.Inner_StreamUtil> result = new ArrayList<>();
         List<RecipeDisplay> displays = recipe.display();

         for (int displayIndex = 0; displayIndex < displays.size(); displayIndex++) {
            RecipeDisplay display = displays.get(displayIndex);
            aK.Inner_StreamUtil localRecipe = init(recipeId, recipe, display, displayIndex, enabledFeatures, slotContext);
            if (localRecipe != null) {
               result.add(localRecipe);
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   private static aK.Inner_StreamUtil init(
      Identifier recipeId, CraftingRecipe recipe, RecipeDisplay display, int displayIndex, FeatureFlagSet enabledFeatures, ContextMap slotContext
   ) {
      if (display != null && display.isEnabled(enabledFeatures)) {
         ItemStack result = display.result().resolveForFirstStack(slotContext);
         if (result != null && !result.isEmpty() && result.isItemEnabled(enabledFeatures)) {
            boolean shaped = display instanceof ShapedCraftingRecipeDisplay;
            if (!shaped && !(display instanceof ShapelessCraftingRecipeDisplay)) {
               return null;
            }

            int width;
            int height;
            List<Ingredient> gridIngredients;
            List<Ingredient> requirements;
            if (recipe instanceof ShapedRecipe shapedRecipe && display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
               width = shapedRecipe.getWidth();
               height = shapedRecipe.getHeight();
               gridIngredients = init(shapedRecipe.getIngredients(), width * height);
               requirements = load(gridIngredients);
               if (width != shapedDisplay.width() || height != shapedDisplay.height()) {
                  width = shapedDisplay.width();
                  height = shapedDisplay.height();
                  gridIngredients = init(shapedDisplay.ingredients(), slotContext);
                  requirements = load(gridIngredients);
               }
            } else if (display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
               width = shapedDisplay.width();
               height = shapedDisplay.height();
               gridIngredients = init(shapedDisplay.ingredients(), slotContext);
               requirements = load(gridIngredients);
            } else {
               height = 1;
               requirements = init(recipe, display, slotContext);
               width = requirements.size();
               gridIngredients = new ArrayList<>(requirements);
            }

            if (width > 0 && height > 0 && !requirements.isEmpty()) {
               if (requirements.stream().anyMatch(ingredient -> ingredient == null || ingredient.isEmpty())) {
                  return null;
               }

               String recipeKey = recipeId.toString();
               if (displayIndex > 0) {
                  recipeKey = recipeKey + "#" + displayIndex;
               }

               String signature = init(result, shaped, width, height, init(shaped, gridIngredients, requirements, slotContext));
               return new aK.Inner_StreamUtil(recipeKey, result, width, height, shaped, gridIngredients, requirements, signature);
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static List<Ingredient> init(List<Optional<Ingredient>> ingredients, int size) {
      List<Ingredient> grid = new ArrayList<>(Math.max(size, ingredients == null ? 0 : ingredients.size()));
      if (ingredients != null) {
         for (Optional<Ingredient> ingredient : ingredients) {
            grid.add(ingredient.orElse(null));
         }
      }

      while (grid.size() < size) {
         grid.add(null);
      }

      return grid;
   }

   private static List<Ingredient> init(List<SlotDisplay> displays, ContextMap slotContext) {
      List<Ingredient> ingredients = new ArrayList<>(displays == null ? 0 : displays.size());
      if (displays == null) {
         return ingredients;
      }

      for (SlotDisplay display : displays) {
         ingredients.add(init(display, slotContext));
      }

      return ingredients;
   }

   private static Ingredient init(SlotDisplay display, ContextMap slotContext) {
      if (display == null) {
         return null;
      }

      List<ItemStack> stacks = display.resolveForStacks(slotContext);
      if (stacks != null && !stacks.isEmpty()) {
         LinkedHashMap<Identifier, Item> items = new LinkedHashMap<>();

         for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty() && stack.getItem() != null) {
               Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
               if (id != null) {
                  items.put(id, stack.getItem());
               }
            }
         }

         if (items.isEmpty()) {
            return null;
         }

         Ingredient ingredient = Ingredient.of(items.values().stream());
         return ingredient.isEmpty() ? null : ingredient;
      } else {
         return null;
      }
   }

   private static List<Ingredient> init(CraftingRecipe recipe, RecipeDisplay display, ContextMap slotContext) {
      PlacementInfo placement = recipe.placementInfo();
      List<SlotDisplay> slots = init(display);
      return placement != null && !placement.ingredients().isEmpty() && (slots.isEmpty() || slots.size() == placement.ingredients().size())
         ? new ArrayList<>(placement.ingredients())
         : load(init(slots, slotContext));
   }

   private static List<SlotDisplay> init(RecipeDisplay display) {
      if (display instanceof ShapedCraftingRecipeDisplay shaped) {
         return shaped.ingredients();
      } else {
         return display instanceof ShapelessCraftingRecipeDisplay shapeless ? shapeless.ingredients() : List.of();
      }
   }

   private static List<Ingredient> load(List<Ingredient> ingredients) {
      List<Ingredient> present = new ArrayList<>();
      if (ingredients == null) {
         return present;
      }

      for (Ingredient ingredient : ingredients) {
         if (ingredient != null && !ingredient.isEmpty()) {
            present.add(ingredient);
         }
      }

      return present;
   }

   private static List<String> init(boolean shaped, List<Ingredient> gridIngredients, List<Ingredient> requirements, ContextMap slotContext) {
      List<String> parts = new ArrayList<>();
      if (shaped) {
         if (gridIngredients != null) {
            for (Ingredient ingredient : gridIngredients) {
               parts.add(init(ingredient, slotContext));
            }
         }
      } else if (requirements != null) {
         for (Ingredient ingredient : requirements) {
            parts.add(init(ingredient, slotContext));
         }

         parts.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return parts;
   }

   private static String init(Ingredient ingredient, ContextMap slotContext) {
      return ingredient != null && !ingredient.isEmpty() ? init_Str(ingredient.display().resolveForStacks(slotContext)) : "_";
   }

   private static String init_Str(List<ItemStack> stacks) {
      if (stacks != null && !stacks.isEmpty()) {
         Set<String> ids = new HashSet<>();

         for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty() && stack.getItem() != null) {
               Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
               if (id != null) {
                  ids.add(id.toString());
               }
            }
         }

         if (ids.isEmpty()) {
            return "_";
         }

         List<String> ordered = new ArrayList<>(ids);
         ordered.sort(String.CASE_INSENSITIVE_ORDER);
         return String.join(",", ordered);
      } else {
         return "_";
      }
   }

   private static String init(ItemStack result, boolean shaped, int width, int height, Collection<String> parts) {
      Identifier resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
      String resultKey = resultId == null ? result.getHoverName().getString() : resultId.toString();
      return resultKey + "|" + result.getCount() + "|" + (shaped ? "shaped" : "shapeless") + "|" + width + "x" + height + "|" + String.join(";", parts);
   }

   record Inner_CachePool(List<aK.Inner_StreamUtil> init_List, List<aK.Inner_DataUtil> load_List, String init_Str, int init_I, int load_I) {
      static final aK.Inner_CachePool init_aK_CachePool = new aK.Inner_CachePool(List.of(), List.of(), "none", 0, 0);

      Inner_CachePool(List<aK.Inner_StreamUtil> crafting, List<aK.Inner_DataUtil> cooking) {
         this(crafting, cooking, "server", crafting.size() + cooking.size(), 0);
      }
   }

   record Inner_DataUtil(String init_Str, List<Item> init_List, ItemStack init_ItemStack, String load, float init_F, int init_I) {
      Inner_DataUtil {
         init_List = List.copyOf(init_List);
         init_ItemStack = init_ItemStack.copy();
      }
   }

   static final class Inner_StreamUtil {
      final String init_Str;
      final ItemStack init_ItemStack;
      final int init_I;
      final int load_I;
      final boolean init_Z;
      final List<Ingredient> init_List;
      final List<Ingredient> load_List;
      final String load_Str;

      Inner_StreamUtil(
         String recipeKey,
         ItemStack result,
         int width,
         int height,
         boolean shaped,
         List<Ingredient> gridIngredients,
         List<Ingredient> requirements,
         String signature
      ) {
         this.init_Str = recipeKey;
         this.init_ItemStack = result.copy();
         this.init_I = width;
         this.load_I = height;
         this.init_Z = shaped;
         this.init_List = aK.init_List(gridIngredients);
         this.load_List = aK.init_List(requirements);
         this.load_Str = signature;
      }

      boolean init(int gridWidth, int gridHeight) {
         return !this.init_Z ? this.load_List.size() <= gridWidth * gridHeight : this.init_I <= gridWidth && this.load_I <= gridHeight;
      }
   }
}
