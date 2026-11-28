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

final class RecipeCatalogLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object LOCK = new Object();
   private static final FileToIdConverter RECIPE_FILES = FileToIdConverter.registry(Registries.RECIPE);
   private static RegistryAccess cachedAccess;
   private static FeatureFlagSet cachedFeatures;
   private static RecipeCatalogLoader.CatalogSnapshot cachedSnapshot;
   private static CompletableFuture<RecipeCatalogLoader.CatalogSnapshot> pendingLoad;
   private static Object load_Obj;
   private static RecipeCatalogLoader.CatalogSnapshot load_aK_CachePool;

   private RecipeCatalogLoader() {
   }

   static List<RecipeCatalogLoader.CraftingEntry> craftingRecipes(Minecraft mc) {
      return catalog(mc).craftingRecipes();
   }

   static List<RecipeCatalogLoader.CookingEntry> cookingRecipes(Minecraft mc) {
      return catalog(mc).cookingRecipes();
   }

   static RecipeCatalogLoader.CatalogSnapshot catalog(Minecraft mc) {
      if (mc != null && mc.level != null) {
         RecipeCatalogLoader.CatalogSnapshot server = load_aK_CachePool(mc);
         return server != null && !server.craftingRecipes().isEmpty()
            ? server
            : loadForClient(registryAccess(mc), enabledFeatures(mc), SlotDisplayContext.fromLevel(mc.level), mc);
      } else {
         return RecipeCatalogLoader.CatalogSnapshot.EMPTY;
      }
   }

   static void preload(Minecraft mc) {
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
            RegistryAccess access = registryAccess(mc);
            FeatureFlagSet features = enabledFeatures(mc);
            ContextMap slotContext = SlotDisplayContext.fromLevel(mc.level);
            synchronized (LOCK) {
               if (!isCacheValid(access, features) || cachedSnapshot == null && pendingLoad == null) {
                  loadAsync(access, features, slotContext);
               }
            }
         }
      }
   }

   static List<RecipeCatalogLoader.CraftingEntry> read(Minecraft mc) {
      if (mc != null && mc.level != null) {
         RecipeCatalogLoader.CatalogSnapshot server = load_aK_CachePool(mc);
         if (server != null && !server.craftingRecipes().isEmpty()) {
            return server.craftingRecipes();
         }

         RegistryAccess access = registryAccess(mc);
         FeatureFlagSet features = enabledFeatures(mc);
         synchronized (LOCK) {
            if (isCacheValid(access, features)) {
               if (cachedSnapshot != null && !cachedSnapshot.craftingRecipes().isEmpty()) {
                  return cachedSnapshot.craftingRecipes();
               }

               if (pendingLoad != null) {
                  return null;
               }
            }

            loadAsync(access, features, SlotDisplayContext.fromLevel(mc.level));
            return null;
         }
      } else {
         return null;
      }
   }

   static List<RecipeCatalogLoader.CraftingEntry> buildCraftingRecipes(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      return loadForClient(access, features, slotContext, null).craftingRecipes();
   }

   static int recipeFileCount(Minecraft mc) {
      return mc == null ? -1 : RECIPE_FILES.listMatchingResources(mc.getResourceManager()).size();
   }

   private static RegistryAccess registryAccess(Minecraft mc) {
      return mc.getConnection() != null ? mc.getConnection().registryAccess() : mc.level.registryAccess();
   }

   private static FeatureFlagSet enabledFeatures(Minecraft mc) {
      return mc.getConnection() != null ? mc.getConnection().enabledFeatures() : mc.level.enabledFeatures();
   }

   private static boolean isCacheValid(RegistryAccess access, FeatureFlagSet features) {
      return cachedAccess == access && Objects.equals(cachedFeatures, features);
   }

   private static CompletableFuture<RecipeCatalogLoader.CatalogSnapshot> loadAsync(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      cachedAccess = access;
      cachedFeatures = features;
      cachedSnapshot = null;
      CompletableFuture<RecipeCatalogLoader.CatalogSnapshot> load = CompletableFuture.supplyAsync(() -> buildSnapshot(access, features, slotContext), runnable -> {
         Thread thread = new Thread(runnable, "Autism-Recipes");
         thread.setDaemon(true);
         thread.start();
      });
      pendingLoad = load;
      load.whenComplete((snapshot, error) -> {
         synchronized (LOCK) {
            if (pendingLoad == load) {
               pendingLoad = null;
               if (snapshot != null) {
                  cachedSnapshot = snapshot;
               }
            }
         }
      });
      return load;
   }

   private static RecipeCatalogLoader.CatalogSnapshot loadForClient(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext, Minecraft mc) {
      if (access == null) {
         return RecipeCatalogLoader.CatalogSnapshot.EMPTY;
      }

      CompletableFuture<RecipeCatalogLoader.CatalogSnapshot> pending;
      synchronized (LOCK) {
         if (isCacheValid(access, features) && cachedSnapshot != null) {
            return cachedSnapshot;
         }

         pending = isCacheValid(access, features) && pendingLoad != null ? pendingLoad : loadAsync(access, features, slotContext);
      }

      RecipeCatalogLoader.CatalogSnapshot loaded;
      try {
         loaded = pending.join();
      } catch (RuntimeException e) {
         loaded = null;
      }

      if (loaded != null && !loaded.craftingRecipes().isEmpty()) {
         return loaded;
      } else {
         return mc == null ? RecipeCatalogLoader.CatalogSnapshot.EMPTY : loadFromResources(mc.getResourceManager(), access, features, slotContext);
      }
   }

   static RecipeCatalogLoader.CatalogSnapshot buildSnapshot(RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      long started = System.nanoTime();
      Map<Identifier, IoSupplier<InputStream>> files = new TreeMap<>(Comparator.comparing(Identifier::toString));
      VanillaPackResources pack = ServerPacksSource.createVanillaPackSource();

      try {
         Compat263.vanillaListResources(pack, PackType.SERVER_DATA, "minecraft", "recipe", files::putIfAbsent);
         RecipeCatalogLoader.CatalogSnapshot parsed = loadFromFiles(files, access, features, slotContext, "client jar");
         LOGGER.info(
            "[Autism] recipes: {} crafting + {} cooking from the client jar ({} files, {} skipped) in {} ms",
            parsed.craftingRecipes().size(),
            parsed.cookingRecipes().size(),
            parsed.filesRead(),
            parsed.filesSkipped(),
            (System.nanoTime() - started) / 1000000L
         );
         Compat263.vanillaClose(pack);
         return parsed;
      } catch (RuntimeException e) {
         LOGGER.warn("[Autism] recipes: the client jar's data pack could not be read: {}", e.toString());
         return RecipeCatalogLoader.CatalogSnapshot.EMPTY;
      }
   }

   private static RecipeCatalogLoader.CatalogSnapshot loadFromFiles(
      Map<Identifier, IoSupplier<InputStream>> files, RegistryAccess access, FeatureFlagSet features, ContextMap slotContext, String source
   ) {
      DynamicOps<JsonElement> ops = access.createSerializationContext(JsonOps.INSTANCE);
      LinkedHashMap<String, RecipeCatalogLoader.CraftingEntry> crafting = new LinkedHashMap<>();
      List<RecipeCatalogLoader.CookingEntry> cooking = new ArrayList<>();
      int failed = 0;

      for (Entry<Identifier, IoSupplier<InputStream>> entry : files.entrySet()) {
         Identifier recipeId = RECIPE_FILES.fileToId(entry.getKey());

         try (Reader reader = new BufferedReader(new InputStreamReader(entry.getValue().get(), StandardCharsets.UTF_8))) {
            JsonElement json = JsonParser.parseReader(reader);
            Recipe<?> decoded = Recipe.CODEC.parse(ops, json).getOrThrow(JsonParseException::new).value();
            if (decoded instanceof CraftingRecipe craftingRecipe) {
               for (RecipeCatalogLoader.CraftingEntry recipe : craftingEntries(recipeId, craftingRecipe, features, slotContext)) {
                  crafting.put(recipe.recipeKey, recipe);
               }
            } else if (decoded instanceof AbstractCookingRecipe cookingRecipe) {
               RecipeCatalogLoader.CookingEntry recipe = cookingEntry(recipeId, cookingRecipe, decoded, slotContext);
               if (recipe != null) {
                  cooking.add(recipe);
               }
            }
         } catch (IOException | RuntimeException e) {
            failed++;
         }
      }

      List<RecipeCatalogLoader.CraftingEntry> craftingList = new ArrayList<>(crafting.values());
      craftingList.sort(Comparator.comparing(recipex -> recipex.recipeKey, String.CASE_INSENSITIVE_ORDER));
      cooking.sort(Comparator.comparing(RecipeCatalogLoader.CookingEntry::recipeKey, String.CASE_INSENSITIVE_ORDER));
      return new RecipeCatalogLoader.CatalogSnapshot(List.copyOf(craftingList), List.copyOf(cooking), source, files.size(), failed);
   }

   private static RecipeCatalogLoader.CatalogSnapshot loadFromResources(ResourceManager manager, RegistryAccess access, FeatureFlagSet features, ContextMap slotContext) {
      if (manager == null) {
         return RecipeCatalogLoader.CatalogSnapshot.EMPTY;
      }

      Map<Identifier, IoSupplier<InputStream>> files = new TreeMap<>(Comparator.comparing(Identifier::toString));

      for (Entry<Identifier, Resource> entry : RECIPE_FILES.listMatchingResources(manager).entrySet()) {
         Resource resource = entry.getValue();
         files.put(entry.getKey(), resource::open);
      }

      return files.isEmpty() ? RecipeCatalogLoader.CatalogSnapshot.EMPTY : loadFromFiles(files, access, features, slotContext, "client resources");
   }

   private static RecipeCatalogLoader.CatalogSnapshot load_aK_CachePool(Minecraft mc) {
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

      synchronized (LOCK) {
         if (load_Obj == manager && load_aK_CachePool != null) {
            return load_aK_CachePool;
         }

         try {
            load_aK_CachePool = buildCatalog(manager.getRecipes(), enabledFeatures(mc), SlotDisplayContext.fromLevel(mc.level));
            load_Obj = manager;
         } catch (RuntimeException e) {
            return null;
         }

         return load_aK_CachePool;
      }
   }

   static RecipeCatalogLoader.CatalogSnapshot buildCatalog(Collection<RecipeHolder<?>> holders, FeatureFlagSet enabledFeatures, ContextMap slotContext) {
      LinkedHashMap<String, RecipeCatalogLoader.CraftingEntry> craftingByKey = new LinkedHashMap<>();
      List<RecipeCatalogLoader.CookingEntry> cooking = new ArrayList<>();

      for (RecipeHolder<?> holder : holders) {
         if (holder != null && holder.id() != null && holder.value() != null) {
            Identifier recipeId = holder.id().identifier();
            if (recipeId != null) {
               Recipe<?> decoded = holder.value();
               if (decoded instanceof CraftingRecipe crafting) {
                  for (RecipeCatalogLoader.CraftingEntry recipe : craftingEntries(recipeId, crafting, enabledFeatures, slotContext)) {
                     craftingByKey.put(recipe.recipeKey, recipe);
                  }
               } else if (decoded instanceof AbstractCookingRecipe cookingRecipe) {
                  RecipeCatalogLoader.CookingEntry recipe = cookingEntry(recipeId, cookingRecipe, decoded, slotContext);
                  if (recipe != null) {
                     cooking.add(recipe);
                  }
               }
            }
         }
      }

      List<RecipeCatalogLoader.CraftingEntry> crafting = new ArrayList<>(craftingByKey.values());
      crafting.sort(Comparator.comparing(recipex -> recipex.recipeKey, String.CASE_INSENSITIVE_ORDER));
      cooking.sort(Comparator.comparing(RecipeCatalogLoader.CookingEntry::recipeKey, String.CASE_INSENSITIVE_ORDER));
      return new RecipeCatalogLoader.CatalogSnapshot(List.copyOf(crafting), List.copyOf(cooking));
   }

   private static RecipeCatalogLoader.CookingEntry cookingEntry(Identifier recipeId, AbstractCookingRecipe cooking, Recipe<?> decoded, ContextMap slotContext) {
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
         ? new RecipeCatalogLoader.CookingEntry(recipeId.toString(), new ArrayList<>(inputs.values()), result, recipeKind(decoded), cooking.experience(), cooking.cookingTime())
         : null;
   }

   private static String recipeKind(Recipe<?> recipe) {
      String name = recipe.getClass().getSimpleName().toLowerCase(Locale.ROOT);
      if (name.contains("blast")) {
         return "minecraft:blast_furnace";
      } else if (name.contains("smoking")) {
         return "minecraft:smoker";
      } else {
         return name.contains("campfire") ? "minecraft:campfire" : "minecraft:furnace";
      }
   }

   private static <T> List<T> immutableCopy(List<T> values) {
      return values != null && !values.isEmpty() ? Collections.unmodifiableList(new ArrayList<>(values)) : List.of();
   }

   static RecipeCatalogLoader.CraftingEntry init(
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
            ? presentIngredients(craftingRequirements.get())
            : List.of();
         int width;
         int height;
         List<Ingredient> gridIngredients;
         List<Ingredient> requirements;
         if (display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
            width = shapedDisplay.width();
            height = shapedDisplay.height();
            gridIngredients = displayIngredients(shapedDisplay.ingredients(), slotContext);
            requirements = explicitRequirements.isEmpty() ? presentIngredients(gridIngredients) : explicitRequirements;
         } else {
            List<SlotDisplay> displays = slotDisplays(display);
            width = Math.max(1, displays.size());
            height = 1;
            requirements = explicitRequirements.isEmpty() ? presentIngredients(displayIngredients(displays, slotContext)) : explicitRequirements;
            gridIngredients = new ArrayList<>(requirements);
         }

         if (width > 0 && height > 0 && !requirements.isEmpty()) {
            if (requirements.stream().anyMatch(ingredient -> ingredient == null || ingredient.isEmpty())) {
               return null;
            }

            String signature = signature(result, shaped, width, height, signatureParts(shaped, gridIngredients, requirements, slotContext));
            return new RecipeCatalogLoader.CraftingEntry(recipeKey, result, width, height, shaped, gridIngredients, requirements, signature);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static List<RecipeCatalogLoader.CraftingEntry> craftingEntries(Identifier recipeId, CraftingRecipe recipe, FeatureFlagSet enabledFeatures, ContextMap slotContext) {
      if (recipe != null && !recipe.isSpecial() && !recipe.placementInfo().isImpossibleToPlace()) {
         List<RecipeCatalogLoader.CraftingEntry> result = new ArrayList<>();
         List<RecipeDisplay> displays = recipe.display();

         for (int displayIndex = 0; displayIndex < displays.size(); displayIndex++) {
            RecipeDisplay display = displays.get(displayIndex);
            RecipeCatalogLoader.CraftingEntry localRecipe = craftingEntry(recipeId, recipe, display, displayIndex, enabledFeatures, slotContext);
            if (localRecipe != null) {
               result.add(localRecipe);
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   private static RecipeCatalogLoader.CraftingEntry craftingEntry(
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
               gridIngredients = shapedGrid(shapedRecipe.getIngredients(), width * height);
               requirements = presentIngredients(gridIngredients);
               if (width != shapedDisplay.width() || height != shapedDisplay.height()) {
                  width = shapedDisplay.width();
                  height = shapedDisplay.height();
                  gridIngredients = displayIngredients(shapedDisplay.ingredients(), slotContext);
                  requirements = presentIngredients(gridIngredients);
               }
            } else if (display instanceof ShapedCraftingRecipeDisplay shapedDisplay) {
               width = shapedDisplay.width();
               height = shapedDisplay.height();
               gridIngredients = displayIngredients(shapedDisplay.ingredients(), slotContext);
               requirements = presentIngredients(gridIngredients);
            } else {
               height = 1;
               requirements = recipeRequirements(recipe, display, slotContext);
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

               String signature = signature(result, shaped, width, height, signatureParts(shaped, gridIngredients, requirements, slotContext));
               return new RecipeCatalogLoader.CraftingEntry(recipeKey, result, width, height, shaped, gridIngredients, requirements, signature);
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

   private static List<Ingredient> shapedGrid(List<Optional<Ingredient>> ingredients, int size) {
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

   private static List<Ingredient> displayIngredients(List<SlotDisplay> displays, ContextMap slotContext) {
      List<Ingredient> ingredients = new ArrayList<>(displays == null ? 0 : displays.size());
      if (displays == null) {
         return ingredients;
      }

      for (SlotDisplay display : displays) {
         ingredients.add(displayIngredient(display, slotContext));
      }

      return ingredients;
   }

   private static Ingredient displayIngredient(SlotDisplay display, ContextMap slotContext) {
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

   private static List<Ingredient> recipeRequirements(CraftingRecipe recipe, RecipeDisplay display, ContextMap slotContext) {
      PlacementInfo placement = recipe.placementInfo();
      List<SlotDisplay> slots = slotDisplays(display);
      return placement != null && !placement.ingredients().isEmpty() && (slots.isEmpty() || slots.size() == placement.ingredients().size())
         ? new ArrayList<>(placement.ingredients())
         : presentIngredients(displayIngredients(slots, slotContext));
   }

   private static List<SlotDisplay> slotDisplays(RecipeDisplay display) {
      if (display instanceof ShapedCraftingRecipeDisplay shaped) {
         return shaped.ingredients();
      } else {
         return display instanceof ShapelessCraftingRecipeDisplay shapeless ? shapeless.ingredients() : List.of();
      }
   }

   private static List<Ingredient> presentIngredients(List<Ingredient> ingredients) {
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

   private static List<String> signatureParts(boolean shaped, List<Ingredient> gridIngredients, List<Ingredient> requirements, ContextMap slotContext) {
      List<String> parts = new ArrayList<>();
      if (shaped) {
         if (gridIngredients != null) {
            for (Ingredient ingredient : gridIngredients) {
               parts.add(ingredientKey(ingredient, slotContext));
            }
         }
      } else if (requirements != null) {
         for (Ingredient ingredient : requirements) {
            parts.add(ingredientKey(ingredient, slotContext));
         }

         parts.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return parts;
   }

   private static String ingredientKey(Ingredient ingredient, ContextMap slotContext) {
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

   private static String signature(ItemStack result, boolean shaped, int width, int height, Collection<String> parts) {
      Identifier resultId = BuiltInRegistries.ITEM.getKey(result.getItem());
      String resultKey = resultId == null ? result.getHoverName().getString() : resultId.toString();
      return resultKey + "|" + result.getCount() + "|" + (shaped ? "shaped" : "shapeless") + "|" + width + "x" + height + "|" + String.join(";", parts);
   }

   record CatalogSnapshot(List<RecipeCatalogLoader.CraftingEntry> craftingRecipes, List<RecipeCatalogLoader.CookingEntry> cookingRecipes, String source, int filesRead, int filesSkipped) {
      static final RecipeCatalogLoader.CatalogSnapshot EMPTY = new RecipeCatalogLoader.CatalogSnapshot(List.of(), List.of(), "none", 0, 0);

      CatalogSnapshot(List<RecipeCatalogLoader.CraftingEntry> crafting, List<RecipeCatalogLoader.CookingEntry> cooking) {
         this(crafting, cooking, "server", crafting.size() + cooking.size(), 0);
      }
   }

   record CookingEntry(String recipeKey, List<Item> inputs, ItemStack result, String load, float init_F, int cookTimeTicks) {
      CookingEntry {
         inputs = List.copyOf(inputs);
         result = result.copy();
      }
   }

   static final class CraftingEntry {
      final String recipeKey;
      final ItemStack result;
      final int width;
      final int height;
      final boolean shaped;
      final List<Ingredient> gridIngredients;
      final List<Ingredient> requirements;
      final String signature;

      CraftingEntry(
         String recipeKey,
         ItemStack result,
         int width,
         int height,
         boolean shaped,
         List<Ingredient> gridIngredients,
         List<Ingredient> requirements,
         String signature
      ) {
         this.recipeKey = recipeKey;
         this.result = result.copy();
         this.width = width;
         this.height = height;
         this.shaped = shaped;
         this.gridIngredients = RecipeCatalogLoader.immutableCopy(gridIngredients);
         this.requirements = RecipeCatalogLoader.immutableCopy(requirements);
         this.signature = signature;
      }

      boolean init(int gridWidth, int gridHeight) {
         return !this.shaped ? this.requirements.size() <= gridWidth * gridHeight : this.width <= gridWidth && this.height <= gridHeight;
      }
   }
}
