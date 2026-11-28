package autismclient.util;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.SelectableRecipe.SingleInputEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.slf4j.Logger;

public final class AutoCraftingEngine {
   private static final Logger LOG = LogUtils.getLogger();
   private static Field recipeBookKnownField;
   private static final ThreadLocal<Executor> CLIENT_DISPATCH_OVERRIDE = new ThreadLocal<>();
   private static volatile String lastConfirmSummary = "";
   private static final Map<Integer, AtomicLong> CONTAINER_SYNC_COUNTERS = new ConcurrentHashMap<>();
   private static final ArrayDeque<Long> RECENT_CLICK_NANOS = new ArrayDeque<>();
   private static final int MAX_CLICKS_PER_WINDOW = 280;
   private static final long CLICK_RATE_WINDOW_NANOS = 7000000000L;
   private static final Object SYNCED_CACHE_LOCK = new Object();
   private static WeakReference<Object> cachedConnectionRef = new WeakReference<>(null);
   private static int cachedSyncedEntryCount = -1;
   private static final Map<Long, AutoCraftingEngine.CatalogIndex> SYNCED_INDEX_BY_GRID = new HashMap<>();
   private static List<RecipeCatalogLoader.CraftingEntry> cachedCraftingSource;
   private static List<AutoCraftingEngine.ResolvedRecipe> cachedResolvedRecipes = List.of();
   private static Map<Item, List<AutoCraftingEngine.ResolvedRecipe>> cachedResolvedByOutput = Map.of();
   private static List<RecipeCatalogLoader.CraftingEntry> cachedSyncedFallbackRecipes;
   private static int cachedSyncedFallbackCount = -1;
   private static List<RecipeCatalogLoader.CookingEntry> cachedSmeltSource;
   private static List<AutoCraftingEngine.SmeltRecipe> cachedSmeltRecipes = List.of();
   private static Map<Item, List<AutoCraftingEngine.SmeltRecipe>> cachedSmeltByOutput = Map.of();
   private static List<AutoCraftingEngine.StationRecipe> cachedStationRecipes = List.of();
   private static Map<Item, List<AutoCraftingEngine.StationRecipe>> cachedStationByOutput = Map.of();
   private static Object cachedStationRecipeAccess;

   private AutoCraftingEngine() {
   }

   public static AutoCraftingEngine.CraftContext captureContext(Minecraft mc) {
      return captureContext(mc, false);
   }

   public static AutoCraftingEngine.CraftContext captureContext(Minecraft mc, boolean forExecution) {
      return mc != null && mc.player != null && mc.level != null ? captureContextWithRecipes(mc, forExecution ? RecipeCatalogLoader.craftingRecipes(mc) : RecipeCatalogLoader.read(mc)) : null;
   }

   public static void prewarmCatalog(Minecraft mc) {
      RecipeCatalogLoader.preload(mc);
   }

   private static AutoCraftingEngine.CraftContext captureContextWithRecipes(Minecraft mc, List<RecipeCatalogLoader.CraftingEntry> recipes) {
      if (mc != null && mc.player != null && mc.level != null) {
         AbstractCraftingMenu activeCrafting = mc.player.containerMenu instanceof AbstractCraftingMenu handler ? handler : null;
         Inventory playerInventory = mc.player.getInventory();
         List<ItemStack> inventoryStacks = new ArrayList<>(playerInventory.getContainerSize());

         for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            inventoryStacks.add(stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY);
         }

         boolean hasActive = activeCrafting != null;
         boolean activeIsPlayer = hasActive && isPlayerInventoryMenu(mc, activeCrafting);
         return new AutoCraftingEngine.CraftContext(
            inventoryStacks,
            hasActive,
            activeIsPlayer,
            hasActive ? activeCrafting.getGridWidth() : 0,
            hasActive ? activeCrafting.getGridHeight() : 0,
            hasActive ? copyGridInputStacks(activeCrafting) : List.of(),
            new ArrayList<>(getKnownRecipeEntries(mc)),
            recipes,
            SlotDisplayContext.fromLevel(mc.level),
            enabledFeatures(mc),
            mc.getResourceManager(),
            mc.getConnection() != null ? mc.getConnection().registryAccess() : mc.level.registryAccess(),
            FabricLoader.getInstance().getAllMods().size(),
            mc.getConnection()
         );
      } else {
         return null;
      }
   }

   public static List<AutoCraftingEngine.CraftOption> scanOptionsForClient(Minecraft mc) {
      return scanOptions(captureContextOnClient(mc));
   }

   public static AutoCraftingEngine.RecipeMatcher matcherFor(String recipeKey, int recipeId) {
      String key = recipeKey == null ? "" : recipeKey;
      int syncedIndex = parseSyncedIndex(key);
      return (recipe, synced) -> !key.isBlank() && key.equalsIgnoreCase(recipe.recipeKey)
         || syncedIndex >= 0 && synced != null && synced.id().index() == syncedIndex
         || recipeId >= 0 && (recipe.recipeKey.hashCode() == recipeId || synced != null && synced.id().index() == recipeId);
   }

   private static int parseSyncedIndex(String key) {
      if (key != null && key.startsWith("synced:")) {
         try {
            return Integer.parseInt(key.substring("synced:".length()));
         } catch (NumberFormatException e) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static List<AutoCraftingEngine.CraftOption> scanOptions(AutoCraftingEngine.CraftContext snap) {
      return scanOptions(snap, null);
   }

   public static List<AutoCraftingEngine.CraftOption> scanOptions(AutoCraftingEngine.CraftContext snap, AutoCraftingEngine.RecipeMatcher filter) {
      if (snap == null) {
         return List.of();
      }

      long perfStart = bK.init_J();
      List<AutoCraftingEngine.MenuSnapshot> contexts = new ArrayList<>();
      if (snap.hasActiveCraftingMenu) {
         contexts.add(buildMenuSnapshot(snap, snap.activeIsPlayerMenu ? AutoCraftingEngine.GridSource.PLAYER_2X2 : AutoCraftingEngine.GridSource.TABLE_3X3, snap.activeGridWidth, snap.activeGridHeight, snap.activeGridStacks));
         if (snap.activeIsPlayerMenu) {
            contexts.add(buildMenuSnapshot(snap, AutoCraftingEngine.GridSource.TABLE_3X3, 3, 3, List.of()));
         }
      } else {
         contexts.add(buildMenuSnapshot(snap, AutoCraftingEngine.GridSource.TABLE_3X3, 3, 3, List.of()));
      }

      List<ItemStack> bag = copyMainBag(snap.inventoryStacks);
      Map<String, AutoCraftingEngine.CraftOption> optionsByKey = new HashMap<>();
      List<RecipeCatalogLoader.CraftingEntry> localRecipes = snap.localRecipes != null
         ? snap.localRecipes
         : RecipeCatalogLoader.buildCraftingRecipes(snap.registryAccess, snap.enabledFeatures, snap.slotContext);

      for (AutoCraftingEngine.MenuSnapshot context : contexts) {
         List<RecipeCatalogLoader.CraftingEntry> candidates = mergeLocalAndSyncedRecipes(localRecipes, context.syncedRecipes);
         AutoCraftingEngine.ItemInventoryCount materials = new AutoCraftingEngine.ItemInventoryCount(bag, context.inputStacks);

         for (RecipeCatalogLoader.CraftingEntry localRecipe : candidates) {
            if (localRecipe.init(context.gridWidth, context.gridHeight) && !localRecipe.result.isEmpty()) {
               RecipeDisplayEntry syncedEntry = context.syncedBySignature.get(localRecipe.signature);
               if (filter == null || filter.test(localRecipe, syncedEntry)) {
                  int maxCraftsByMaterials = materials.maxCraftsFor(localRecipe);
                  int maxCraftsByStorage = simulateMaxCraftsByStorage(localRecipe, context.inputStacks, bag, maxCraftsByMaterials);
                  AutoCraftingEngine.CraftOption option = new AutoCraftingEngine.CraftOption(
                     localRecipe, syncedEntry, maxCraftsByMaterials, maxCraftsByStorage, context.gridSource
                  );
                  mergeBestOption(optionsByKey, option);
               }
            }
         }
      }

      List<AutoCraftingEngine.CraftOption> options = new ArrayList<>(optionsByKey.values());
      options.sort(
         Comparator.<AutoCraftingEngine.CraftOption, Boolean>comparing(optionx -> !optionx.craftable)
            .thenComparing(optionx -> optionx.gridSource != AutoCraftingEngine.GridSource.PLAYER_2X2)
            .thenComparing(optionx -> -optionx.maxCrafts)
            .thenComparing(optionx -> optionx.outputName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(optionx -> optionx.recipeKey, String.CASE_INSENSITIVE_ORDER)
      );
      bK.load(filter == null ? "craft.scan" : "craft.resolve", perfStart, 250000000L);
      return options;
   }

   private static List<ItemStack> copyMainBag(List<ItemStack> inventoryStacks) {
      List<ItemStack> bag = new ArrayList<>(36);
      if (inventoryStacks != null) {
         for (int i = 0; i < Math.min(36, inventoryStacks.size()); i++) {
            ItemStack stack = inventoryStacks.get(i);
            bag.add(stack != null && !stack.isEmpty() ? stack : ItemStack.EMPTY);
         }
      }

      while (bag.size() < 36) {
         bag.add(ItemStack.EMPTY);
      }

      return bag;
   }

   private static AutoCraftingEngine.MenuSnapshot buildMenuSnapshot(AutoCraftingEngine.CraftContext snap, AutoCraftingEngine.GridSource source, int gridWidth, int gridHeight, List<ItemStack> inputStacks) {
      AutoCraftingEngine.CatalogIndex resolution = getOrBuildSyncedIndex(snap, gridWidth, gridHeight);
      return new AutoCraftingEngine.MenuSnapshot(source, gridWidth, gridHeight, inputStacks, resolution.syncedBySignature, resolution.recipes);
   }

   private static boolean isPlayerInventoryMenu(Minecraft mc, AbstractCraftingMenu craftingHandler) {
      return mc != null && mc.player != null && craftingHandler == mc.player.inventoryMenu;
   }

   private static FeatureFlagSet enabledFeatures(Minecraft mc) {
      if (mc == null) {
         return FeatureFlagSet.of();
      } else if (mc.getConnection() != null) {
         return mc.getConnection().enabledFeatures();
      } else {
         return mc.level != null ? mc.level.enabledFeatures() : FeatureFlagSet.of();
      }
   }

   private static List<ItemStack> copyGridInputStacks(AbstractCraftingMenu craftingHandler) {
      List<ItemStack> inputStacks = new ArrayList<>();

      for (Slot slot : craftingHandler.getInputGridSlots()) {
         if (slot != null && !slot.getItem().isEmpty()) {
            inputStacks.add(slot.getItem().copy());
         }
      }

      return inputStacks;
   }

   private static void mergeBestOption(Map<String, AutoCraftingEngine.CraftOption> optionsByKey, AutoCraftingEngine.CraftOption candidate) {
      if (candidate != null && candidate.recipeKey != null && !candidate.recipeKey.isBlank()) {
         String mergeKey = candidate.recipeKey;
         AutoCraftingEngine.CraftOption existing = optionsByKey.get(mergeKey);
         if (existing == null) {
            optionsByKey.put(mergeKey, candidate);
         } else {
            boolean preferCandidate = false;
            if (candidate.outputStack.getCount() != existing.outputStack.getCount()) {
               preferCandidate = candidate.outputStack.getCount() > existing.outputStack.getCount();
            } else if (candidate.craftable != existing.craftable) {
               preferCandidate = candidate.craftable;
            } else if (candidate.gridSource != existing.gridSource) {
               preferCandidate = candidate.gridSource == AutoCraftingEngine.GridSource.PLAYER_2X2;
            } else if (candidate.maxCrafts != existing.maxCrafts) {
               preferCandidate = candidate.maxCrafts > existing.maxCrafts;
            } else if (!existing.hasSyncedEntry() && candidate.hasSyncedEntry()) {
               preferCandidate = true;
            }

            if (preferCandidate) {
               optionsByKey.put(mergeKey, candidate);
            }
         }
      }
   }

   private static List<RecipeCatalogLoader.CraftingEntry> mergeLocalAndSyncedRecipes(List<RecipeCatalogLoader.CraftingEntry> localRecipes, List<RecipeCatalogLoader.CraftingEntry> syncedRecipes) {
      List<RecipeCatalogLoader.CraftingEntry> merged = new ArrayList<>();
      Set<String> seenSignatures = new HashSet<>();
      if (localRecipes != null) {
         for (RecipeCatalogLoader.CraftingEntry recipe : localRecipes) {
            if (recipe != null) {
               merged.add(recipe);
               if (recipe.signature != null && !recipe.signature.isBlank()) {
                  seenSignatures.add(recipe.signature);
               }
            }
         }
      }

      if (syncedRecipes != null) {
         for (RecipeCatalogLoader.CraftingEntry recipe : syncedRecipes) {
            if (recipe != null && (recipe.signature == null || !seenSignatures.contains(recipe.signature))) {
               merged.add(recipe);
               if (recipe.signature != null && !recipe.signature.isBlank()) {
                  seenSignatures.add(recipe.signature);
               }
            }
         }
      }

      return merged;
   }

   private static AutoCraftingEngine.CraftContext captureContextOnClient(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         return callOnClientThread(mc, () -> captureContextWithRecipes(mc, RecipeCatalogLoader.craftingRecipes(mc)));
      } else {
         return null;
      }
   }

   public static AutoCraftingEngine.CraftOption findOptionById(Minecraft mc, int recipeId) {
      return matchOptionInList(scanOptions(captureContextOnClient(mc), matcherFor("", recipeId)), "", recipeId);
   }

   public static AutoCraftingEngine.CraftOption findOptionByKey(Minecraft mc, String recipeKey) {
      return recipeKey != null && !recipeKey.isBlank() ? matchOptionInList(scanOptions(captureContextOnClient(mc), matcherFor(recipeKey, -1)), recipeKey, -1) : null;
   }

   public static AutoCraftingEngine.CraftOption findOptionByOutput(Minecraft mc, ItemStack output) {
      if (output != null && !output.isEmpty()) {
         AutoCraftingEngine.CraftOption best = null;

         for (AutoCraftingEngine.CraftOption option : scanOptionsForClient(mc)) {
            if (matchesOutputStack(option.outputStack, output)) {
               if (best == null) {
                  best = option;
               } else if (option.craftable && !best.craftable) {
                  best = option;
               } else if (option.craftable == best.craftable && option.maxCrafts > best.maxCrafts) {
                  best = option;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static ItemStack currentResultSlotItem(Minecraft mc) {
      if (mc != null && mc.player != null) {
         return mc.player.containerMenu instanceof AbstractCraftingMenu craftingHandler ? craftingHandler.getResultSlot().getItem().copy() : ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static int craftsNeededForAmount(int outputPerCraft, int desiredAmount) {
      int safeOutput = Math.max(1, outputPerCraft);
      int safeDesired = Math.max(1, desiredAmount);
      return Math.max(1, (int)Math.ceil((double)safeDesired / safeOutput));
   }

   public static int roundUpToOutputBatch(int outputPerCraft, int desiredAmount) {
      int safeOutput = Math.max(1, outputPerCraft);
      return craftsNeededForAmount(safeOutput, desiredAmount) * safeOutput;
   }

   public static int estimateCraftableItems(AutoCraftingEngine.CraftOption option, int desiredAmount) {
      if (option == null) {
         return roundUpToOutputBatch(1, desiredAmount);
      }

      int desiredCrafts = Math.min(option.maxCrafts, craftsNeededForAmount(option.outputStack.getCount(), desiredAmount));
      return Math.max(0, desiredCrafts * Math.max(1, option.outputStack.getCount()));
   }

   public static int resolveTargetAmount(AutoCraftingEngine.CraftOption option, int desiredAmount, boolean useMaxAmount) {
      if (option == null) {
         return useMaxAmount ? 0 : Math.max(1, desiredAmount);
      } else {
         return useMaxAmount ? option.maxOutputItems : Math.max(1, desiredAmount);
      }
   }

   public static String formatStatusLabel(AutoCraftingEngine.CraftOption option) {
      if (option == null) {
         return "No recipe";
      } else if (option.craftable) {
         return "Crafts x" + option.maxCrafts;
      } else {
         return option.hasMaterials && !option.hasStorageRoom ? "No Room" : "Need Mats";
      }
   }

   public static List<Packet<?>> buildCraftPackets(Minecraft mc, int recipeId, int desiredAmount) {
      return buildCraftPackets(mc, null, recipeId, desiredAmount);
   }

   public static List<Packet<?>> buildCraftPackets(Minecraft mc, String recipeKey, int recipeId, int desiredAmount) {
      if (mc != null && mc.player != null && mc.level != null) {
         if (!(mc.player.containerMenu instanceof AbstractCraftingMenu craftingHandler)) {
            return List.of();
         } else {
            AutoCraftingEngine.CraftOption option = resolveOption(mc, recipeKey, recipeId);
            if (option != null && option.maxCrafts > 0) {
               int wantCrafts = Math.min(option.maxCrafts, craftsNeededForAmount(option.outputStack.getCount(), desiredAmount));
               if (wantCrafts <= 0) {
                  return List.of();
               }

               CraftingClickPlanner.GridLayout layout = resolveMenuLayout(mc, craftingHandler);
               if (layout != null) {
                  List<CraftingClickPlanner.ClickPhase> phases = CraftingClickPlanner.planPhases(
                     layout,
                     snapshotMenuSlots((AbstractContainerMenu)craftingHandler),
                     craftingHandler.getCarried(),
                     CraftingClickPlanner.RecipeSpec.fromRecipe(option.localRecipe),
                     wantCrafts,
                     mc.player.getInventory().getSelectedSlot()
                  );
                  if (!phases.isEmpty()) {
                     List<Packet<?>> packets = new ArrayList<>();

                     for (CraftingClickPlanner.ClickPhase phase : phases) {
                        for (CraftingClickPlanner.SlotClick click : phase.clicks()) {
                           packets.add(
                              new ServerboundContainerClickPacket(
                                 craftingHandler.containerId,
                                 craftingHandler.getStateId(),
                                 (short)click.slot(),
                                 (byte)click.button(),
                                 click.input(),
                                 new Int2ObjectArrayMap<>(),
                                 HashedStack.EMPTY
                              )
                           );
                        }
                     }

                     return packets;
                  }
               }

               if (!option.hasSyncedEntry()) {
                  return List.of();
               }

               boolean craftAll = wantCrafts >= option.maxCrafts;
               List<Packet<?>> packets = new ArrayList<>();
               int fills = craftAll ? 1 : wantCrafts;

               for (int i = 0; i < fills; i++) {
                  packets.add(new ServerboundPlaceRecipePacket(craftingHandler.containerId, option.syncedEntry.id(), craftAll));
               }

               packets.add(
                  PacketRegenerator.init(
                     new ServerboundContainerClickPacket(
                        craftingHandler.containerId,
                        craftingHandler.getStateId(),
                        (short)craftingHandler.getResultSlot().index,
                        (byte)0,
                        ContainerInput.QUICK_MOVE,
                        new Int2ObjectArrayMap<>(),
                        HashedStack.EMPTY
                     )
                  )
               );
               return packets;
            } else {
               return List.of();
            }
         }
      } else {
         return List.of();
      }
   }

   public static AutoCraftingEngine.CraftResult craft(Minecraft mc, int recipeId, int desiredAmount) {
      return craft(mc, null, recipeId, desiredAmount);
   }

   public static AutoCraftingEngine.CraftResult craftWithDispatch(Minecraft mc, String recipeKey, int recipeId, int desiredAmount, Executor clientDispatch) {
      Executor previous = CLIENT_DISPATCH_OVERRIDE.get();
      CLIENT_DISPATCH_OVERRIDE.set(Objects.requireNonNull(clientDispatch));

      try {
         return craft(mc, recipeKey, recipeId, desiredAmount);
      } finally {
         if (previous == null) {
            CLIENT_DISPATCH_OVERRIDE.remove();
         } else {
            CLIENT_DISPATCH_OVERRIDE.set(previous);
         }
      }
   }

   public static AutoCraftingEngine.CraftResult craft(Minecraft mc, String recipeKey, int recipeId, int desiredAmount) {
      if (mc == null || mc.player == null || mc.getConnection() == null || mc.gameMode == null) {
         return AutoCraftingEngine.CraftResult.failed("No network connection.");
      }

      if (autismclient.modules.aD.read_Z()) {
         return AutoCraftingEngine.CraftResult.failed("Crafting is unavailable right now.");
      }

      boolean onClient = mc.isSameThread();
      AutoCraftingEngine.CraftOption requestedOption = resolveOption(mc, recipeKey, recipeId);
      AutoCraftingEngine.CraftResult rejected = validateCraftable(requestedOption);
      if (rejected != null) {
         return rejected;
      }

      AutoCraftingEngine.OpenMenuSession preparedContext = openMenuForOption(mc, requestedOption, onClient);
      if (preparedContext != null && preparedContext.menu != null) {
         try {
            return executeCraftLoop(mc, preparedContext.menu, requestedOption, desiredAmount, onClient);
         } finally {
            closeTemporaryMenu(mc, preparedContext);
         }
      } else {
         return requestedOption.gridSource != AutoCraftingEngine.GridSource.TABLE_3X3 && requestedOption.localRecipe.init(2, 2)
            ? AutoCraftingEngine.CraftResult.failed("Open your inventory or a crafting screen first.")
            : AutoCraftingEngine.CraftResult.failed("No reachable crafting table is available.");
      }
   }

   public static AutoCraftingEngine.CraftResult validateCraftable(AutoCraftingEngine.CraftOption option) {
      if (option == null) {
         return AutoCraftingEngine.CraftResult.failed("That recipe is not available right now.");
      } else if (option.craftable && option.maxCrafts > 0) {
         return null;
      } else {
         return option.hasMaterials && !option.hasStorageRoom
            ? AutoCraftingEngine.CraftResult.inventoryFull("No room in the inventory for " + option.outputName + ".")
            : AutoCraftingEngine.CraftResult.failed("Missing materials for " + option.outputName + ".");
      }
   }

   private static AutoCraftingEngine.CraftResult executeCraftLoop(Minecraft mc, AbstractCraftingMenu menu, AutoCraftingEngine.CraftOption option, int desiredAmount, boolean onClient) {
      long started = System.nanoTime();
      int perCraft = Math.max(1, option.outputStack.getCount());
      int wantCrafts = Math.min(option.maxCrafts, craftsNeededForAmount(perCraft, desiredAmount));
      int roundedTarget = wantCrafts * perCraft;
      CraftingClickPlanner.RecipeSpec spec = CraftingClickPlanner.RecipeSpec.fromRecipe(option.localRecipe);
      Integer before = callOnClientThread(mc, () -> countInventoryItem(mc, option.outputStack));
      if (before == null) {
         return AutoCraftingEngine.CraftResult.failed("The client did not respond.");
      }

      int crafts = 0;
      int phases = 0;
      String failure = null;
      boolean uncertain = false;

      while (crafts < wantCrafts) {
         if (Thread.currentThread().isInterrupted()) {
            failure = "Craft interrupted; the inventory may have changed.";
            uncertain = phases > 0;
            break;
         }

         if (!onClient) {
            throttleClicksIfNeeded();
         }

         if (Thread.currentThread().isInterrupted()) {
            failure = "Craft interrupted before the next batch.";
            uncertain = phases > 0;
            break;
         }

         int left = wantCrafts - crafts;
         AutoCraftingEngine.PlannedCraft run = callOnClientThread(mc, () -> dispatchCraftBurst(mc, menu, spec, left));
         if (run == null) {
            failure = "The client did not respond; dispatched work may have changed inventory.";
            uncertain = true;
            break;
         }

         if (run.failure() != null) {
            failure = run.failure();
            break;
         }

         phases++;
         if (onClient) {
            crafts += run.phase().load_I();
         } else {
            AutoCraftingEngine.SyncStatus confirm = awaitServerConfirm(mc, menu, run);
            if (confirm == AutoCraftingEngine.SyncStatus.CONFIRMED) {
               crafts += run.phase().load_I();
            } else {
               if (confirm != AutoCraftingEngine.SyncStatus.INTERRUPTED && confirm != AutoCraftingEngine.SyncStatus.UNCONFIRMED && confirm != AutoCraftingEngine.SyncStatus.CLOSED) {
                  Integer now = callOnClientThread(mc, () -> countInventoryItem(mc, option.outputStack));
                  int expected = (crafts + run.phase().load_I()) * perCraft;
                  if (confirm == AutoCraftingEngine.SyncStatus.MISMATCH && now != null && now - before >= expected) {
                     crafts += run.phase().load_I();
                     continue;
                  }

                  failure = confirm == AutoCraftingEngine.SyncStatus.CLOSED ? "The crafting screen closed." : "The server did not accept the craft.";
                  if (confirm == AutoCraftingEngine.SyncStatus.MISMATCH) {
                     clearCraftingGrid(mc, menu);
                  }
                  break;
               }

               failure = "Craft clicks were dispatched but the server result is unconfirmed; reobserve inventory before further crafting.";
               uncertain = true;
               break;
            }
         }
      }

      int crafted;
      if (onClient) {
         crafted = crafts * perCraft;
      } else {
         Integer after = callOnClientThread(mc, () -> countInventoryItem(mc, option.outputStack));
         crafted = !uncertain && after != null ? Math.max(0, after - before) : crafts * perCraft;
      }

      LOG.info(
         "[Autism] craft {}: {} of {} in {} burst(s), {} ms{}{}",
         option.recipeKey,
         crafted,
         roundedTarget,
         phases,
         (System.nanoTime() - started) / 1000000L,
         failure == null ? "" : " (" + failure + ")",
         lastConfirmSummary.isEmpty() ? "" : ", last confirmed on " + lastConfirmSummary
      );
      if (uncertain) {
         return AutoCraftingEngine.CraftResult.uncertainResult(failure, crafts * perCraft);
      } else if (crafted <= 0) {
         return AutoCraftingEngine.CraftResult.failed(failure != null ? failure : "Craft failed.");
      } else {
         return crafted < roundedTarget
            ? AutoCraftingEngine.CraftResult.succeeded("Crafted " + crafted + "/" + roundedTarget + " " + option.outputName + "." + (failure == null ? "" : " " + failure), crafted)
            : AutoCraftingEngine.CraftResult.succeeded("Crafted " + crafted + " " + option.outputName + ".", crafted);
      }
   }

   private static AutoCraftingEngine.PlannedCraft dispatchCraftBurst(Minecraft mc, AbstractCraftingMenu menu, CraftingClickPlanner.RecipeSpec spec, int crafts) {
      if (mc.player == null || mc.gameMode == null) {
         return AutoCraftingEngine.PlannedCraft.failed("No player.");
      }

      if (autismclient.modules.aD.read_Z()) {
         return AutoCraftingEngine.PlannedCraft.failed("Crafting is unavailable right now.");
      }

      if (mc.player.containerMenu != menu) {
         return AutoCraftingEngine.PlannedCraft.failed("The crafting screen closed.");
      }

      CraftingClickPlanner.GridLayout layout = resolveMenuLayout(mc, menu);
      if (layout == null) {
         return AutoCraftingEngine.PlannedCraft.failed("This crafting screen is not supported.");
      }

      CraftingClickPlanner.PhaseResult outcome = CraftingClickPlanner.planNextPhase(
         layout, snapshotMenuSlots((AbstractContainerMenu)menu), menu.getCarried(), spec, crafts, mc.player.getInventory().getSelectedSlot()
      );
      if (outcome.phase() == null) {
         return AutoCraftingEngine.PlannedCraft.failed(outcome.error());
      }

      ItemStack[] initial = snapshotMenuSlots((AbstractContainerMenu)menu);
      int stateBefore = menu.getStateId();
      long contentBefore = getContainerPacketCount(menu.containerId);
      sendClickPhase(mc, menu, layout, outcome.phase());
      return new AutoCraftingEngine.PlannedCraft(outcome.phase(), layout, initial, stateBefore, contentBefore, null);
   }

   private static void sendClickPhase(Minecraft mc, AbstractCraftingMenu menu, CraftingClickPlanner.GridLayout layout, CraftingClickPlanner.ClickPhase phase) {
      for (int i = 0; i < phase.clicks().size(); i++) {
         CraftingClickPlanner.SlotClick click = phase.clicks().get(i);
         if (click.slot() == layout.resultSlot()) {
            menu.getSlot(layout.resultSlot()).set(phase.write().get(i).copy());
         }

         ClientPacketListener connection = mc.getConnection();
         if (connection == null || mc.player == null || mc.player.containerMenu != menu) {
            throw new IllegalStateException("Crafting menu changed before click dispatch");
         }

         ItemStack[] before = snapshotMenuSlots((AbstractContainerMenu)menu);
         menu.clicked(click.slot(), click.button(), click.input(), mc.player);
         Int2ObjectArrayMap<HashedStack> changed = new Int2ObjectArrayMap<>();

         for (int slot = 0; slot < before.length; slot++) {
            if (!ItemStack.matches(before[slot], menu.getSlot(slot).getItem())) {
               changed.put(slot, HashedStack.create(menu.getSlot(slot).getItem(), connection.decoratedHashOpsGenenerator()));
            }
         }

         connection.send(
            new ServerboundContainerClickPacket(
               menu.containerId,
               -1,
               (short)click.slot(),
               (byte)click.button(),
               click.input(),
               changed,
               HashedStack.create(menu.getCarried(), connection.decoratedHashOpsGenenerator())
            )
         );
         applyPredictedMenuState(menu, phase.predictedSlots().get(i), phase.predictedCarried().get(i));
      }

      recordDispatchedClicks(phase.clicks().size());
   }

   private static void applyPredictedMenuState(AbstractContainerMenu menu, ItemStack[] slots, ItemStack carried) {
      for (int i = 0; i < Math.min(slots.length, menu.slots.size()); i++) {
         Slot slot = menu.slots.get(i);
         if (!ItemStack.matches(slot.getItem(), slots[i])) {
            slot.set(slots[i].copy());
         }
      }

      if (!ItemStack.matches(menu.getCarried(), carried)) {
         menu.setCarried(carried.copy());
      }
   }

   private static AutoCraftingEngine.SyncStatus awaitServerConfirm(Minecraft mc, AbstractCraftingMenu menu, AutoCraftingEngine.PlannedCraft run) {
      long deadline = System.nanoTime() + confirmTimeoutMs(mc) * 1000000L;
      int stale = run.phase().clicks().size();
      long[] last = new long[]{-1L, -9223372036854775808L, System.nanoTime()};
      long started = System.nanoTime();

      while (System.nanoTime() < deadline) {
         if (Thread.currentThread().isInterrupted()) {
            return AutoCraftingEngine.SyncStatus.INTERRUPTED;
         }

         AutoCraftingEngine.SyncStatus state = callOnClientThread(mc, () -> {
            if (mc.player != null && mc.player.containerMenu == menu) {
               long packets = getContainerPacketCount(menu.containerId) - run.contentPacketsBefore();
               int stateId = menu.getStateId();
               long now = System.nanoTime();
               if (packets != last[0] || stateId != last[1]) {
                  last[0] = packets;
                  last[1] = stateId;
                  last[2] = now;
               }

               if (stateId == run.stateIdBefore() && packets == 0L) {
                  return null;
               } else {
                  boolean allAnswers = stale == 0 || packets >= stale;
                  boolean matches = matchesPlannedState(menu, run);
                  if (matches && allAnswers) {
                     lastConfirmSummary = "answers " + packets + "/" + stale + " in " + (now - started) / 1000000L + " ms";
                     return AutoCraftingEngine.SyncStatus.CONFIRMED;
                  } else {
                     return !matches && allAnswers && now - last[2] > 300000000L ? AutoCraftingEngine.SyncStatus.MISMATCH : null;
                  }
               }
            } else {
               return AutoCraftingEngine.SyncStatus.CLOSED;
            }
         });
         if (state != null) {
            return state;
         }
      }

      return AutoCraftingEngine.SyncStatus.UNCONFIRMED;
   }

   private static boolean matchesPlannedState(AbstractCraftingMenu menu, AutoCraftingEngine.PlannedCraft run) {
      ItemStack[] planned = run.phase().finalSlots();
      ItemStack[] initial = run.initialSlots();
      CraftingClickPlanner.GridLayout layout = run.layout();

      for (int i = 0; i < Math.min(planned.length, menu.slots.size()); i++) {
         if (i != layout.resultSlot()) {
            boolean planChanges = layout.init(i) || initial == null || i >= initial.length || !ItemStack.matches(initial[i], planned[i]);
            if (planChanges && !ItemStack.matches(menu.slots.get(i).getItem(), planned[i])) {
               return false;
            }
         }
      }

      return ItemStack.matches(menu.getCarried(), run.phase().finalCarried());
   }

   private static void clearCraftingGrid(Minecraft mc, AbstractCraftingMenu menu) {
      AutoCraftingEngine.PlannedCraft run = callOnClientThread(mc, () -> {
         if (mc.player != null && mc.gameMode != null && mc.player.containerMenu == menu) {
            CraftingClickPlanner.GridLayout layout = resolveMenuLayout(mc, menu);
            if (layout == null) {
               return null;
            }

            CraftingClickPlanner.ClickPhase phase = CraftingClickPlanner.init(layout, snapshotMenuSlots((AbstractContainerMenu)menu), menu.getCarried());
            if (phase == null) {
               return null;
            }

            ItemStack[] initial = snapshotMenuSlots((AbstractContainerMenu)menu);
            int stateBefore = menu.getStateId();
            long contentBefore = getContainerPacketCount(menu.containerId);
            sendClickPhase(mc, menu, layout, phase);
            return new AutoCraftingEngine.PlannedCraft(phase, layout, initial, stateBefore, contentBefore, null);
         } else {
            return null;
         }
      });
      if (run != null && run.phase() != null && !mc.isSameThread()) {
         awaitServerConfirm(mc, menu, run);
      }
   }

   private static long confirmTimeoutMs(Minecraft mc) {
      int ping = 0;

      try {
         if (mc.getConnection() != null && mc.player != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) {
               ping = Math.max(0, info.getLatency());
            }
         }
      } catch (RuntimeException var3) {
      }

      return Math.min(8000L, 1500L + 4L * ping);
   }

   static CraftingClickPlanner.GridLayout resolveMenuLayout(Minecraft mc, AbstractCraftingMenu menu) {
      if (mc != null && mc.player != null && menu != null) {
         if (menu == mc.player.inventoryMenu && menu.slots.size() == 46) {
            return CraftingClickPlanner.GridLayout.inventoryGrid();
         } else {
            return menu instanceof CraftingMenu && menu.slots.size() == 46 && menu.getGridWidth() == 3 && menu.getGridHeight() == 3
               ? CraftingClickPlanner.GridLayout.craftingTable()
               : null;
         }
      } else {
         return null;
      }
   }

   private static ItemStack[] snapshotMenuSlots(AbstractContainerMenu menu) {
      ItemStack[] slots = new ItemStack[menu.slots.size()];

      for (int i = 0; i < slots.length; i++) {
         ItemStack stack = menu.slots.get(i).getItem();
         slots[i] = stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
      }

      return slots;
   }

   public static void onContainerContentPacket(int containerId) {
      CONTAINER_SYNC_COUNTERS.computeIfAbsent(containerId, id -> new AtomicLong()).incrementAndGet();
   }

   private static long getContainerPacketCount(int containerId) {
      AtomicLong count = CONTAINER_SYNC_COUNTERS.get(containerId);
      return count == null ? 0L : count.get();
   }

   private static synchronized void recordDispatchedClicks(int clicks) {
      long now = System.nanoTime();

      for (int i = 0; i < clicks; i++) {
         RECENT_CLICK_NANOS.addLast(now);
      }
   }

   private static synchronized long computeClickThrottleWaitNanos() {
      long now = System.nanoTime();

      while (!RECENT_CLICK_NANOS.isEmpty() && now - RECENT_CLICK_NANOS.peekFirst() > 7000000000L) {
         RECENT_CLICK_NANOS.pollFirst();
      }

      if (RECENT_CLICK_NANOS.size() + 96 <= 280) {
         return 0L;
      }

      int over = RECENT_CLICK_NANOS.size() + 96 - 280;
      Long oldest = null;
      int index = 0;

      for (Long time : RECENT_CLICK_NANOS) {
         if (++index >= over) {
            oldest = time;
            break;
         }
      }

      return oldest == null ? 0L : Math.max(0L, 7000000000L - (now - oldest));
   }

   private static void throttleClicksIfNeeded() {
      long wait = computeClickThrottleWaitNanos();
      if (wait > 0L) {
         try {
            Thread.sleep(Math.min(7000L, wait / 1000000L + 1L));
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }

   private static AutoCraftingEngine.CraftOption resolveOption(Minecraft mc, String recipeKey, int recipeId) {
      String key = recipeKey == null ? "" : recipeKey;
      return matchOptionInList(scanOptions(captureContextOnClient(mc), matcherFor(key, recipeId)), key, recipeId);
   }

   private static AutoCraftingEngine.OpenMenuSession openMenuForOption(Minecraft mc, AutoCraftingEngine.CraftOption option, boolean onClient) {
      if (mc != null && mc.player != null && option != null) {
         AbstractContainerMenu current = callOnClientThread(mc, () -> mc.player == null ? null : mc.player.containerMenu);
         if (current instanceof CraftingMenu tableMenu && option.localRecipe.init(tableMenu.getGridWidth(), tableMenu.getGridHeight())) {
            return new AutoCraftingEngine.OpenMenuSession(tableMenu, false, null);
         } else if (option.localRecipe.init(2, 2)) {
            if (current == mc.player.inventoryMenu) {
               return new AutoCraftingEngine.OpenMenuSession(mc.player.inventoryMenu, false, null);
            }

            Boolean closed = callOnClientThread(mc, () -> {
               if (mc.player == null) {
                  return false;
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu) {
                  mc.player.closeContainer();
               }

               return mc.player.containerMenu == mc.player.inventoryMenu;
            });
            return Boolean.TRUE.equals(closed) ? new AutoCraftingEngine.OpenMenuSession(mc.player.inventoryMenu, false, null) : null;
         } else {
            if (onClient) {
               return null;
            }

            Screen restoreScreen = callOnClientThread(mc, () -> mc.player != null && mc.player.containerMenu == mc.player.inventoryMenu ? mc.gui.screen() : null);
            AbstractContainerMenu previous = callOnClientThread(mc, () -> mc.player == null ? null : mc.player.containerMenu);
            Boolean opened = callOnClientThread(mc, () -> {
               BlockPos tablePos = findNearestCraftingTable(mc);
               if (tablePos != null && mc.gameMode != null && mc.player != null) {
                  BlockHitResult hitResult = raycastCraftingTable(mc, tablePos);
                  if (hitResult == null) {
                     return false;
                  }

                  mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                  return true;
               } else {
                  return false;
               }
            });
            if (!Boolean.TRUE.equals(opened)) {
               return null;
            }

            long deadline = System.nanoTime() + confirmTimeoutMs(mc) * 1000000L;

            while (System.nanoTime() < deadline) {
               if (Thread.currentThread().isInterrupted()) {
                  return null;
               }

               CraftingMenu table = callOnClientThread(
                  mc,
                  () -> mc.player != null && mc.player.containerMenu instanceof CraftingMenu menu && menu != previous && menu.getStateId() > 0 ? menu : null
               );
               if (table != null) {
                  return new AutoCraftingEngine.OpenMenuSession(table, true, restoreScreen);
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static void closeTemporaryMenu(Minecraft mc, AutoCraftingEngine.OpenMenuSession context) {
      if (mc != null && mc.player != null && context != null && context.closeAfter) {
         callOnClientThread(mc, () -> {
            if (mc.player == null) {
               return null;
            }

            if (mc.player.containerMenu != mc.player.inventoryMenu) {
               mc.player.closeContainer();
            }

            if (context.restoreScreen != null) {
               mc.gui.setScreen(context.restoreScreen);
               mc.player.containerMenu = mc.player.inventoryMenu;
            }

            return null;
         });
      }
   }

   private static BlockPos findNearestCraftingTable(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         double reach = Math.max(4.5, mc.player.blockInteractionRange());
         double reachSq = reach * reach;
         int radius = (int)Math.ceil(reach);
         BlockPos origin = mc.player.blockPosition();
         BlockPos bestPos = null;
         double bestDistanceSq = 1.7976931348623157E308;

         for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            if (mc.level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
               Vec3 center = Vec3.atCenterOf(pos);
               double distanceSq = mc.player.distanceToSqr(center);
               if (!(distanceSq > reachSq) && !(distanceSq >= bestDistanceSq)) {
                  BlockHitResult hitResult = raycastCraftingTable(mc, pos);
                  if (hitResult != null) {
                     bestPos = pos.immutable();
                     bestDistanceSq = distanceSq;
                  }
               }
            }
         }

         return bestPos;
      } else {
         return null;
      }
   }

   private static BlockHitResult raycastCraftingTable(Minecraft mc, BlockPos tablePos) {
      if (mc != null && mc.player != null && mc.level != null && tablePos != null) {
         Vec3 center = Vec3.atCenterOf(tablePos);
         double reach = Math.max(4.5, mc.player.blockInteractionRange());
         if (mc.player.distanceToSqr(center) > reach * reach) {
            return null;
         }

         ClipContext context = new ClipContext(mc.player.getEyePosition(), center, Block.OUTLINE, Fluid.NONE, mc.player);
         BlockHitResult result = mc.level.clip(context);
         return result.getType() == Type.BLOCK && result.getBlockPos().equals(tablePos) ? result : new BlockHitResult(center, Direction.UP, tablePos, false);
      } else {
         return null;
      }
   }

   private static <T> T callOnClientThread(Minecraft mc, Supplier<T> task) {
      if (mc.isSameThread()) {
         return task.get();
      }

      CompletableFuture<T> future = new CompletableFuture<>();
      Runnable call = () -> {
         if (!future.isDone()) {
            try {
               future.complete(task.get());
            } catch (Throwable t) {
               future.completeExceptionally(t);
            }
         }
      };
      Executor dispatch = CLIENT_DISPATCH_OVERRIDE.get();
      if (dispatch == null) {
         mc.execute(call);
      } else {
         dispatch.execute(call);
      }

      boolean interrupted = Thread.interrupted();
      long deadline = System.nanoTime() + 5000000000L;

      try {
         while (true) {
            try {
               return future.get(Math.max(1L, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
               interrupted = true;
            } catch (ExecutionException e) {
               if (e.getCause() instanceof RuntimeException runtime) {
                  throw runtime;
               }

               return null;
            } catch (TimeoutException e) {
               return null;
            }
         }
      } finally {
         future.cancel(false);
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   private static AutoCraftingEngine.CatalogIndex getOrBuildSyncedIndex(AutoCraftingEngine.CraftContext snap, int gridWidth, int gridHeight) {
      long gridKey = (long)gridWidth << 16 | gridHeight & 65535L;
      synchronized (SYNCED_CACHE_LOCK) {
         if (cachedConnectionRef.get() != snap.connection || cachedSyncedEntryCount != snap.rawSyncedEntries.size()) {
            SYNCED_INDEX_BY_GRID.clear();
            cachedConnectionRef = new WeakReference<>(snap.connection);
            cachedSyncedEntryCount = snap.rawSyncedEntries.size();
         }

         AutoCraftingEngine.CatalogIndex cached = SYNCED_INDEX_BY_GRID.get(gridKey);
         if (cached != null) {
            return cached;
         }

         long perfStart = bK.init_J();
         List<RecipeDisplayEntry> supported = new ArrayList<>();

         for (RecipeDisplayEntry entry : snap.rawSyncedEntries) {
            if (fitsInGrid(entry, gridWidth, gridHeight)) {
               supported.add(entry);
            }
         }

         AutoCraftingEngine.CatalogIndex fresh = buildSyncedIndex(supported, snap.slotContext, snap.enabledFeatures);
         bK.load("craft.syncedResolve", perfStart, 100000000L);
         SYNCED_INDEX_BY_GRID.put(gridKey, fresh);
         return fresh;
      }
   }

   static AutoCraftingEngine.CatalogIndex buildSyncedIndex(List<RecipeDisplayEntry> entries, ContextMap params, FeatureFlagSet enabledFeatures) {
      List<RecipeCatalogLoader.CraftingEntry> recipes = new ArrayList<>();
      Map<String, RecipeDisplayEntry> bySignature = new HashMap<>();
      if (entries != null && params != null) {
         Set<String> seenKeys = new HashSet<>();

         for (RecipeDisplayEntry entry : entries) {
            if (entry != null && entry.display() != null) {
               RecipeCatalogLoader.CraftingEntry recipe = RecipeCatalogLoader.init("synced:" + entry.id().index(), entry.display(), entry.craftingRequirements(), enabledFeatures, params);
               if (recipe != null) {
                  if (recipe.signature != null) {
                     bySignature.putIfAbsent(recipe.signature, entry);
                  }

                  if (seenKeys.add(recipe.recipeKey)) {
                     recipes.add(recipe);
                  }
               }
            }
         }

         return new AutoCraftingEngine.CatalogIndex(recipes, bySignature);
      } else {
         return new AutoCraftingEngine.CatalogIndex(recipes, bySignature);
      }
   }

   private static int simulateMaxCraftsByStorage(RecipeCatalogLoader.CraftingEntry localRecipe, List<ItemStack> inputStacks, List<ItemStack> inventoryStacks, int maxCraftsByMaterials) {
      if (localRecipe != null && !localRecipe.result.isEmpty() && inventoryStacks != null) {
         int craftLimit = Math.max(0, maxCraftsByMaterials);
         if (craftLimit <= 0) {
            return 0;
         }

         List<ItemStack> inventorySlots = new ArrayList<>(36);
         int playerSlotCount = Math.min(36, inventoryStacks.size());

         for (int i = 0; i < playerSlotCount; i++) {
            ItemStack stack = inventoryStacks.get(i);
            inventorySlots.add(stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY);
         }

         List<ItemStack> gridPools = new ArrayList<>();
         if (inputStacks != null) {
            for (ItemStack stack : inputStacks) {
               if (stack != null && !stack.isEmpty()) {
                  gridPools.add(stack.copy());
               }
            }
         }

         int crafted = 0;

         while (crafted < craftLimit && consumeIngredientsSimulated(localRecipe, inventorySlots, gridPools) && insertOutputSimulated(localRecipe.result, inventorySlots)) {
            crafted++;
         }

         return crafted;
      } else {
         return 0;
      }
   }

   private static boolean consumeIngredientsSimulated(RecipeCatalogLoader.CraftingEntry localRecipe, List<ItemStack> inventorySlots, List<ItemStack> gridPools) {
      if (localRecipe != null && !localRecipe.requirements.isEmpty()) {
         for (Ingredient ingredient : localRecipe.requirements) {
            if (ingredient != null && !ingredient.isEmpty() && !consumeOneIngredientSimulated(gridPools, ingredient, false) && !consumeOneIngredientSimulated(inventorySlots, ingredient, true)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean consumeOneIngredientSimulated(List<ItemStack> stacks, Ingredient ingredient, boolean preferSmallestStack) {
      if (stacks != null && !stacks.isEmpty() && ingredient != null && !ingredient.isEmpty()) {
         int bestIndex = -1;
         int bestCount = 2147483647;

         for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (CraftingClickPlanner.isUsableIngredient(stack) && ingredient.test(stack)) {
               int count = stack.getCount();
               if (bestIndex < 0 || preferSmallestStack && count < bestCount) {
                  bestIndex = i;
                  bestCount = count;
                  if (preferSmallestStack && bestCount <= 1) {
                     break;
                  }
               }
            }
         }

         if (bestIndex < 0) {
            return false;
         }

         ItemStack chosen = stacks.get(bestIndex);
         chosen.shrink(1);
         if (chosen.isEmpty()) {
            stacks.set(bestIndex, ItemStack.EMPTY);
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean insertOutputSimulated(ItemStack resultStack, List<ItemStack> inventorySlots) {
      if (resultStack != null && !resultStack.isEmpty() && inventorySlots != null) {
         int remaining = resultStack.getCount();
         int maxStackSize = Math.max(1, resultStack.getMaxStackSize());

         for (int i = 0; i < inventorySlots.size() && remaining > 0; i++) {
            ItemStack existing = inventorySlots.get(i);
            if (existing != null && !existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, resultStack)) {
               int existingMax = Math.max(1, Math.min(existing.getMaxStackSize(), maxStackSize));
               if (existing.getCount() < existingMax) {
                  int add = Math.min(existingMax - existing.getCount(), remaining);
                  existing.grow(add);
                  remaining -= add;
               }
            }
         }

         for (int i = 0; i < inventorySlots.size() && remaining > 0; i++) {
            ItemStack existing = inventorySlots.get(i);
            if (existing == null || existing.isEmpty()) {
               int add = Math.min(maxStackSize, remaining);
               ItemStack placed = resultStack.copy();
               placed.setCount(add);
               inventorySlots.set(i, placed);
               remaining -= add;
            }
         }

         return remaining <= 0;
      } else {
         return false;
      }
   }

   private static Collection<RecipeDisplayEntry> getKnownRecipeEntries(Minecraft mc) {
      if (mc != null && mc.player != null) {
         if (mc.player.getRecipeBook() instanceof ClientRecipeBook recipeBook) {
            try {
               if (recipeBookKnownField == null) {
                  recipeBookKnownField = ClientRecipeBook.class.getDeclaredField("known");
                  recipeBookKnownField.setAccessible(true);
               }

               if (recipeBookKnownField.get(recipeBook) instanceof Map<?, ?> recipeMap) {
                  List<RecipeDisplayEntry> entries = new ArrayList<>(recipeMap.size());

                  for (Object value : recipeMap.values()) {
                     if (value instanceof RecipeDisplayEntry entry) {
                        entries.add(entry);
                     }
                  }

                  if (!entries.isEmpty()) {
                     return entries;
                  }
               }
            } catch (Exception var7) {
            }

            recipeBook.rebuildCollections();
            List<RecipeDisplayEntry> fallback = new ArrayList<>();

            for (RecipeCollection collection : recipeBook.getCollections()) {
               fallback.addAll(collection.getRecipes());
            }

            return fallback;
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static boolean fitsInGrid(RecipeDisplayEntry entry, int gridWidth, int gridHeight) {
      if (entry != null && entry.display() != null) {
         if (!(entry.display() instanceof ShapedCraftingRecipeDisplay shaped)) {
            return entry.display() instanceof ShapelessCraftingRecipeDisplay shapeless ? shapeless.ingredients().size() <= gridWidth * gridHeight : false;
         } else {
            return shaped.width() <= gridWidth && shaped.height() <= gridHeight;
         }
      } else {
         return false;
      }
   }

   private static boolean matchesOutputStack(ItemStack left, ItemStack right) {
      return left != null
         && right != null
         && !left.isEmpty()
         && !right.isEmpty()
         && left.getCount() == right.getCount()
         && ItemStack.isSameItemSameComponents(left, right);
   }

   private static int countInventoryItem(Minecraft mc, ItemStack template) {
      if (mc.player != null && template != null && !template.isEmpty()) {
         int total = 0;
         Inventory inventory = mc.player.getInventory();

         for (int slot = 0; slot < Math.min(36, inventory.getContainerSize()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.getItem() == template.getItem()) {
               total += stack.getCount();
            }
         }

         return total;
      } else {
         return 0;
      }
   }

   public static List<AutoCraftingEngine.CraftOption> filterOptions(List<AutoCraftingEngine.CraftOption> all, String query) {
      if (query != null && !query.isBlank()) {
         String lower = query.toLowerCase();
         return all.stream().filter(o -> o.searchText.contains(lower)).toList();
      } else {
         return new ArrayList<>(all);
      }
   }

   public static AutoCraftingEngine.CraftOption matchOptionInList(List<AutoCraftingEngine.CraftOption> recipes, String recipeKey, int recipeId) {
      if (recipes == null) {
         return null;
      }

      String key = recipeKey == null ? "" : recipeKey;
      int syncedIndex = parseSyncedIndex(key);

      for (AutoCraftingEngine.CraftOption option : recipes) {
         if (!key.isBlank() && key.equalsIgnoreCase(option.recipeKey)) {
            return option;
         }
      }

      for (AutoCraftingEngine.CraftOption option : recipes) {
         if (syncedIndex >= 0 && option.syncedRecipeIndex == syncedIndex) {
            return option;
         }

         if (recipeId >= 0 && (option.recipeKeyHash == recipeId || option.syncedRecipeIndex == recipeId)) {
            return option;
         }
      }

      return null;
   }

   private static List<RecipeCatalogLoader.CraftingEntry> getCraftingCatalogRecipes(Minecraft mc) {
      List<RecipeCatalogLoader.CraftingEntry> local = RecipeCatalogLoader.craftingRecipes(mc);
      if (!local.isEmpty()) {
         return local;
      }

      List<RecipeDisplayEntry> entries = new ArrayList<>(getKnownRecipeEntries(mc));
      if (cachedSyncedFallbackRecipes != null && entries.size() == cachedSyncedFallbackCount) {
         return cachedSyncedFallbackRecipes;
      }

      FeatureFlagSet features = mc.getConnection() != null ? mc.getConnection().enabledFeatures() : mc.level.enabledFeatures();
      cachedSyncedFallbackRecipes = List.copyOf(buildSyncedIndex(entries, SlotDisplayContext.fromLevel(mc.level), features).recipes);
      cachedSyncedFallbackCount = entries.size();
      return cachedSyncedFallbackRecipes;
   }

   public static synchronized List<AutoCraftingEngine.ResolvedRecipe> getResolvedCraftingRecipes(Minecraft mc) {
      if (mc != null && mc.level != null) {
         List<RecipeCatalogLoader.CraftingEntry> source = getCraftingCatalogRecipes(mc);
         if (source == cachedCraftingSource) {
            return cachedResolvedRecipes;
         }

         ContextMap slotContext = SlotDisplayContext.fromLevel(mc.level);
         List<AutoCraftingEngine.ResolvedRecipe> plans = new ArrayList<>();
         Map<Item, List<AutoCraftingEngine.ResolvedRecipe>> byOutput = new HashMap<>();

         for (RecipeCatalogLoader.CraftingEntry recipe : source) {
            AutoCraftingEngine.ResolvedRecipe plan = resolveRecipe(recipe, slotContext);
            if (plan != null) {
               plans.add(plan);
               byOutput.computeIfAbsent(plan.outputItem(), item -> new ArrayList<>()).add(plan);
            }
         }

         for (List<AutoCraftingEngine.ResolvedRecipe> group : byOutput.values()) {
            group.sort(Comparator.comparingInt(planx -> planx.inputs().size()));
         }

         cachedResolvedRecipes = List.copyOf(plans);
         cachedResolvedByOutput = Map.copyOf(byOutput);
         cachedCraftingSource = source;
         return cachedResolvedRecipes;
      } else {
         return List.of();
      }
   }

   public static synchronized JsonObject describeCatalogDiagnostics(Minecraft mc) {
      JsonObject out = new JsonObject();
      if (mc != null && mc.level != null) {
         RecipeCatalogLoader.CatalogSnapshot snapshot = RecipeCatalogLoader.catalog(mc);
         List<AutoCraftingEngine.ResolvedRecipe> plans = getResolvedCraftingRecipes(mc);
         List<RecipeDisplayEntry> synced = new ArrayList<>(getKnownRecipeEntries(mc));
         FeatureFlagSet features = mc.getConnection() != null ? mc.getConnection().enabledFeatures() : mc.level.enabledFeatures();
         int syncedDerived = buildSyncedIndex(synced, SlotDisplayContext.fromLevel(mc.level), features).recipes.size();
         out.addProperty("source", snapshot.source());
         out.addProperty("filesRead", snapshot.filesRead());
         out.addProperty("filesSkipped", snapshot.filesSkipped());
         out.addProperty("craftingRecipes", snapshot.craftingRecipes().size());
         out.addProperty("cookingRecipes", snapshot.cookingRecipes().size());
         out.addProperty("plansBuilt", plans.size());
         out.addProperty("syncedBookEntries", synced.size());
         out.addProperty("syncedDerivedRecipes", syncedDerived);
         return out;
      } else {
         out.addProperty("state", "no level");
         return out;
      }
   }

   public static List<AutoCraftingEngine.ResolvedRecipe> getCraftingRecipesFor(Minecraft mc, Item output) {
      if (output == null) {
         return List.of();
      }

      getResolvedCraftingRecipes(mc);
      return cachedResolvedByOutput.getOrDefault(output, List.of());
   }

   public static synchronized List<AutoCraftingEngine.SmeltRecipe> getSmeltRecipes(Minecraft mc) {
      List<RecipeCatalogLoader.CookingEntry> source = RecipeCatalogLoader.cookingRecipes(mc);
      if (source == cachedSmeltSource) {
         return cachedSmeltRecipes;
      }

      List<AutoCraftingEngine.SmeltRecipe> plans = new ArrayList<>();
      Map<Item, List<AutoCraftingEngine.SmeltRecipe>> byOutput = new HashMap<>();

      for (RecipeCatalogLoader.CookingEntry recipe : source) {
         AutoCraftingEngine.SmeltRecipe plan = new AutoCraftingEngine.SmeltRecipe(
            recipe.recipeKey(),
            recipe.inputs(),
            recipe.result().getItem(),
            Math.max(1, recipe.result().getCount()),
            recipe.load(),
            recipe.cookTimeTicks()
         );
         plans.add(plan);
         byOutput.computeIfAbsent(plan.outputItem(), item -> new ArrayList<>()).add(plan);
      }

      for (List<AutoCraftingEngine.SmeltRecipe> group : byOutput.values()) {
         group.sort(Comparator.comparingInt(planx -> "minecraft:furnace".equals(planx.station()) ? 0 : 1));
      }

      cachedSmeltRecipes = List.copyOf(plans);
      cachedSmeltByOutput = Map.copyOf(byOutput);
      cachedSmeltSource = source;
      return cachedSmeltRecipes;
   }

   public static List<AutoCraftingEngine.SmeltRecipe> getSmeltRecipesFor(Minecraft mc, Item output) {
      if (output == null) {
         return List.of();
      }

      getSmeltRecipes(mc);
      return cachedSmeltByOutput.getOrDefault(output, List.of());
   }

   public static synchronized List<AutoCraftingEngine.StationRecipe> getStonecutterRecipes(Minecraft mc) {
      if (mc != null && mc.level != null) {
         RecipeAccess access = mc.level.recipeAccess();
         if (access == cachedStationRecipeAccess) {
            return cachedStationRecipes;
         }

         ContextMap slotContext = SlotDisplayContext.fromLevel(mc.level);
         List<AutoCraftingEngine.StationRecipe> plans = new ArrayList<>();
         Map<Item, List<AutoCraftingEngine.StationRecipe>> byOutput = new HashMap<>();

         for (Object raw : access.stonecutterRecipes().entries()) {
            SingleInputEntry entry = (SingleInputEntry)raw;
            ItemStack result = entry.recipe().optionDisplay().resolveForFirstStack(slotContext);
            if (result != null && !result.isEmpty()) {
               List<Item> inputs = new ArrayList<>();

               for (ItemStack stack : entry.input().display().resolveForStacks(slotContext)) {
                  if (stack != null && !stack.isEmpty() && !inputs.contains(stack.getItem())) {
                     inputs.add(stack.getItem());
                  }
               }

               if (!inputs.isEmpty()) {
                  String key = "unknown";
                  Optional used = entry.recipe().recipe();
                  if (used.isPresent()) {
                     key = ((Holder<?>)used.get()).unwrapKey().map(k -> k.identifier().toString()).orElse("unknown");
                  }

                  AutoCraftingEngine.StationRecipe plan = new AutoCraftingEngine.StationRecipe(key, inputs, result.getItem(), Math.max(1, result.getCount()), "minecraft:stonecutter");
                  plans.add(plan);
                  byOutput.computeIfAbsent(plan.outputItem(), item -> new ArrayList<>()).add(plan);
               }
            }
         }

         cachedStationRecipes = List.copyOf(plans);
         cachedStationByOutput = Map.copyOf(byOutput);
         cachedStationRecipeAccess = access;
         return cachedStationRecipes;
      } else {
         return List.of();
      }
   }

   public static List<AutoCraftingEngine.StationRecipe> getStonecutterRecipesFor(Minecraft mc, Item output) {
      if (output == null) {
         return List.of();
      }

      getStonecutterRecipes(mc);
      return cachedStationByOutput.getOrDefault(output, List.of());
   }

   static AutoCraftingEngine.ResolvedRecipe resolveRecipe(RecipeCatalogLoader.CraftingEntry recipe, ContextMap slotContext) {
      if (recipe != null && !recipe.result.isEmpty() && !recipe.requirements.isEmpty()) {
         List<AutoCraftingEngine.IngredientSlot> inputs = new ArrayList<>(recipe.requirements.size());

         for (Ingredient ingredient : recipe.requirements) {
            if (ingredient == null || ingredient.isEmpty()) {
               return null;
            }

            List<Item> accepts = new ArrayList<>();

            for (ItemStack stack : ingredient.display().resolveForStacks(slotContext)) {
               if (stack != null && !stack.isEmpty() && !accepts.contains(stack.getItem())) {
                  accepts.add(stack.getItem());
               }
            }

            if (accepts.isEmpty()) {
               return null;
            }

            inputs.add(new AutoCraftingEngine.IngredientSlot(accepts));
         }

         return new AutoCraftingEngine.ResolvedRecipe(
            recipe.recipeKey, recipe.result.getItem(), Math.max(1, recipe.result.getCount()), !recipe.init(2, 2), inputs
         );
      } else {
         return null;
      }
   }

   public static final class CraftContext {
      final List<ItemStack> inventoryStacks;
      final boolean hasActiveCraftingMenu;
      final boolean activeIsPlayerMenu;
      final int activeGridWidth;
      final int activeGridHeight;
      final List<ItemStack> activeGridStacks;
      final List<RecipeDisplayEntry> rawSyncedEntries;
      final List<RecipeCatalogLoader.CraftingEntry> localRecipes;
      final ContextMap slotContext;
      final FeatureFlagSet enabledFeatures;
      final ResourceManager resourceManager;
      final RegistryAccess registryAccess;
      final int modCount;
      public final Object connection;

      CraftContext(
         List<ItemStack> inventoryStacks,
         boolean hasActiveCraftingMenu,
         boolean activeIsPlayerMenu,
         int activeGridWidth,
         int activeGridHeight,
         List<ItemStack> activeGridStacks,
         List<RecipeDisplayEntry> rawSyncedEntries,
         List<RecipeCatalogLoader.CraftingEntry> localRecipes,
         ContextMap slotContext,
         FeatureFlagSet enabledFeatures,
         ResourceManager resourceManager,
         RegistryAccess registryAccess,
         int modCount,
         Object connection
      ) {
         this.inventoryStacks = inventoryStacks;
         this.hasActiveCraftingMenu = hasActiveCraftingMenu;
         this.activeIsPlayerMenu = activeIsPlayerMenu;
         this.activeGridWidth = activeGridWidth;
         this.activeGridHeight = activeGridHeight;
         this.activeGridStacks = activeGridStacks;
         this.rawSyncedEntries = rawSyncedEntries;
         this.localRecipes = localRecipes;
         this.slotContext = slotContext;
         this.enabledFeatures = enabledFeatures;
         this.resourceManager = resourceManager;
         this.registryAccess = registryAccess;
         this.modCount = modCount;
         this.connection = connection;
      }
   }

   public static final class CraftResult {
      public final boolean success;
      public final boolean uncertain;
      public final String message;
      public final int craftedAmount;
      public final AutoCraftingEngine.CraftResult.FailureReason failureReason;

      private CraftResult(boolean success, String message, int craftedAmount) {
         this(success, false, message, craftedAmount);
      }

      private CraftResult(boolean success, boolean uncertain, String message, int craftedAmount) {
         this(success, uncertain, message, craftedAmount, AutoCraftingEngine.CraftResult.FailureReason.NONE);
      }

      private CraftResult(boolean success, boolean uncertain, String message, int craftedAmount, AutoCraftingEngine.CraftResult.FailureReason rejection) {
         this.success = success;
         this.uncertain = uncertain;
         this.message = message;
         this.craftedAmount = craftedAmount;
         this.failureReason = rejection;
      }

      public static AutoCraftingEngine.CraftResult succeeded(String message, int craftedAmount) {
         return new AutoCraftingEngine.CraftResult(true, message, craftedAmount);
      }

      public static AutoCraftingEngine.CraftResult failed(String message) {
         return new AutoCraftingEngine.CraftResult(false, message, 0);
      }

      public static AutoCraftingEngine.CraftResult uncertainResult(String message, int confirmedAmount) {
         return new AutoCraftingEngine.CraftResult(false, true, message, confirmedAmount);
      }

      public static AutoCraftingEngine.CraftResult inventoryFull(String message) {
         return new AutoCraftingEngine.CraftResult(false, false, message, 0, AutoCraftingEngine.CraftResult.FailureReason.INVENTORY_FULL);
      }

      public enum FailureReason {
         NONE,
         INVENTORY_FULL;
      }
   }

   public enum GridSource {
      PLAYER_2X2,
      TABLE_3X3;
   }

   private enum SyncStatus {
      CONFIRMED,
      MISMATCH,
      CLOSED,
      INTERRUPTED,
      UNCONFIRMED;
   }

   public record ResolvedRecipe(String recipeKey, Item outputItem, int outputCount, boolean requiresTable, List<AutoCraftingEngine.IngredientSlot> inputs) {
      public ResolvedRecipe {
         inputs = List.copyOf(inputs);
      }
   }

   public record StationRecipe(String recipeKey, List<Item> inputs, Item outputItem, int outputCount, String station) {
      public StationRecipe {
         inputs = List.copyOf(inputs);
      }
   }

   private record PlannedCraft(
      CraftingClickPlanner.ClickPhase phase, CraftingClickPlanner.GridLayout layout, ItemStack[] initialSlots, int stateIdBefore, long contentPacketsBefore, String failure
   ) {
      static AutoCraftingEngine.PlannedCraft failed(String failure) {
         return new AutoCraftingEngine.PlannedCraft(null, null, null, 0, 0L, failure);
      }
   }

   private static final class ItemInventoryCount {
      private final Map<Holder<Item>, Integer> itemCounts = new HashMap<>();

      ItemInventoryCount(List<ItemStack> bag, List<ItemStack> grid) {
         this.addStacks(bag);
         this.addStacks(grid);
      }

      private void addStacks(List<ItemStack> stacks) {
         if (stacks != null) {
            for (ItemStack stack : stacks) {
               if (CraftingClickPlanner.isUsableIngredient(stack)) {
                  this.itemCounts.merge(stack.typeHolder(), stack.getCount(), Integer::sum);
               }
            }
         }
      }

      int maxCraftsFor(RecipeCatalogLoader.CraftingEntry recipe) {
         if (recipe != null && !recipe.requirements.isEmpty() && !this.itemCounts.isEmpty()) {
            int bound = 2147483647;

            for (Ingredient ingredient : recipe.requirements) {
               if (ingredient == null || ingredient.isEmpty()) {
                  return 0;
               }

               int held = 0;

               for (Entry<Holder<Item>, Integer> entry : this.itemCounts.entrySet()) {
                  if (ingredient.acceptsItem(entry.getKey())) {
                     held += entry.getValue();
                  }
               }

               if (held <= 0) {
                  return 0;
               }

               bound = Math.min(bound, held);
            }

            StackedContents<Holder<Item>> contents = new StackedContents<>();
            this.itemCounts.forEach(contents::account);
            return contents.tryPickAll(recipe.requirements, Math.max(1, bound), null);
         } else {
            return 0;
         }
      }
   }

   private static final class MenuSnapshot {
      private final AutoCraftingEngine.GridSource gridSource;
      private final int gridWidth;
      private final int gridHeight;
      private final List<ItemStack> inputStacks;
      private final Map<String, RecipeDisplayEntry> syncedBySignature;
      private final List<RecipeCatalogLoader.CraftingEntry> syncedRecipes;

      private MenuSnapshot(
         AutoCraftingEngine.GridSource source,
         int gridWidth,
         int gridHeight,
         List<ItemStack> inputStacks,
         Map<String, RecipeDisplayEntry> syncedBySignature,
         List<RecipeCatalogLoader.CraftingEntry> syncedRecipes
      ) {
         this.gridSource = source;
         this.gridWidth = gridWidth;
         this.gridHeight = gridHeight;
         this.inputStacks = inputStacks == null ? List.of() : List.copyOf(inputStacks);
         this.syncedBySignature = syncedBySignature == null ? Map.of() : Map.copyOf(syncedBySignature);
         this.syncedRecipes = syncedRecipes == null ? List.of() : List.copyOf(syncedRecipes);
      }
   }

   private static final class OpenMenuSession {
      private final AbstractCraftingMenu menu;
      private final boolean closeAfter;
      private final Screen restoreScreen;

      private OpenMenuSession(AbstractCraftingMenu handler, boolean closeAfter, Screen restoreScreen) {
         this.menu = handler;
         this.closeAfter = closeAfter;
         this.restoreScreen = restoreScreen;
      }
   }

   public record SmeltRecipe(String recipeKey, List<Item> inputs, Item outputItem, int outputCount, String station, int cookTimeTicks) {
      public SmeltRecipe(String recipeKey, List<Item> inputs, Item output, int outputCount, String station) {
         this(recipeKey, inputs, output, outputCount, station, "minecraft:furnace".equals(station) ? 200 : 100);
      }

      public SmeltRecipe {
         inputs = List.copyOf(inputs);
      }
   }

   public interface RecipeMatcher {
      boolean test(RecipeCatalogLoader.CraftingEntry var1, RecipeDisplayEntry var2);
   }

   public static final class CraftOption {
      public final String recipeKey;
      public final int recipeKeyHash;
      public final int syncedRecipeIndex;
      public final RecipeDisplayEntry syncedEntry;
      public final RecipeCatalogLoader.CraftingEntry localRecipe;
      public final ItemStack outputStack;
      public final Component outputHoverName;
      public final String outputName;
      public final String outputItemId;
      public final String searchText;
      public final boolean hasMaterials;
      public final boolean hasStorageRoom;
      public final int maxCraftsByMaterials;
      public final int maxCraftsByStorage;
      public final boolean craftable;
      public final int maxCrafts;
      public final int maxOutputItems;
      public final AutoCraftingEngine.GridSource gridSource;

      public CraftOption(
         RecipeCatalogLoader.CraftingEntry localRecipe, RecipeDisplayEntry syncedEntry, int maxCraftsByMaterials, int maxCraftsByStorage, AutoCraftingEngine.GridSource craftSource
      ) {
         this.localRecipe = localRecipe;
         this.syncedEntry = syncedEntry;
         this.recipeKey = localRecipe.recipeKey;
         this.recipeKeyHash = localRecipe.recipeKey.hashCode();
         this.syncedRecipeIndex = syncedEntry != null ? syncedEntry.id().index() : -1;
         this.outputStack = localRecipe.result.copy();
         this.outputHoverName = this.outputStack.getHoverName().copy();
         this.outputName = this.outputHoverName.getString();
         this.maxCraftsByMaterials = Math.max(0, maxCraftsByMaterials);
         this.maxCraftsByStorage = Math.max(0, maxCraftsByStorage);
         this.hasMaterials = this.maxCraftsByMaterials > 0;
         this.hasStorageRoom = this.maxCraftsByStorage > 0;
         this.maxCrafts = Math.max(0, Math.min(this.maxCraftsByMaterials, this.maxCraftsByStorage));
         this.craftable = this.maxCrafts > 0;
         this.maxOutputItems = this.maxCrafts * Math.max(1, this.outputStack.getCount());
         this.gridSource = craftSource == null ? AutoCraftingEngine.GridSource.PLAYER_2X2 : craftSource;
         Identifier id = this.outputStack.getItem() == null ? null : BuiltInRegistries.ITEM.getKey(this.outputStack.getItem());
         this.outputItemId = id == null ? "" : id.toString();
         this.searchText = (this.outputName + " " + this.outputItemId + " " + this.recipeKey).toLowerCase();
      }

      public boolean hasSyncedEntry() {
         return this.syncedEntry != null && this.syncedRecipeIndex >= 0;
      }
   }

   static final class CatalogIndex {
      final List<RecipeCatalogLoader.CraftingEntry> recipes;
      final Map<String, RecipeDisplayEntry> syncedBySignature;

      CatalogIndex(List<RecipeCatalogLoader.CraftingEntry> recipes, Map<String, RecipeDisplayEntry> bySignature) {
         this.recipes = recipes;
         this.syncedBySignature = bySignature;
      }
   }

   public record IngredientSlot(List<Item> acceptedItems) {
      public IngredientSlot {
         acceptedItems = List.copyOf(acceptedItems);
      }

      public boolean accepts(Item item) {
         return this.acceptedItems.contains(item);
      }
   }
}
