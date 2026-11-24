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

public final class J {
   private static final Logger init_Logger = LogUtils.getLogger();
   private static Field init_Field;
   private static final ThreadLocal<Executor> init_ThreadLocal = new ThreadLocal<>();
   private static volatile String init_Str = "";
   private static final Map<Integer, AtomicLong> init_Map = new ConcurrentHashMap<>();
   private static final ArrayDeque<Long> init_ArrayDeque = new ArrayDeque<>();
   private static final int init_I = 280;
   private static final long init_J = 7000000000L;
   private static final Object init_Obj = new Object();
   private static WeakReference<Object> init_WeakReference = new WeakReference<>(null);
   private static int load_I = -1;
   private static final Map<Long, J.Inner_TextFormat> load_Map = new HashMap<>();
   private static List<aK.Inner_StreamUtil> init_List;
   private static List<J.Inner_EventBus> load_List = List.of();
   private static Map<Item, List<J.Inner_EventBus>> read_Map = Map.of();
   private static List<aK.Inner_StreamUtil> read_List;
   private static int read_I = -1;
   private static List<aK.Inner_DataUtil> write_List;
   private static List<J.Inner_StreamUtil> update_List = List.of();
   private static Map<Item, List<J.Inner_StreamUtil>> write_Map = Map.of();
   private static List<J.Inner_FileCache> sync = List.of();
   private static Map<Item, List<J.Inner_FileCache>> update_Map = Map.of();
   private static Object load_Obj;

   private J() {
   }

   public static J.Inner_BufferHelper init_J_BufferHelper(Minecraft mc) {
      return init(mc, false);
   }

   public static J.Inner_BufferHelper init(Minecraft mc, boolean forExecution) {
      return mc != null && mc.player != null && mc.level != null ? init(mc, forExecution ? aK.init_List(mc) : aK.read(mc)) : null;
   }

   public static void init_V(Minecraft mc) {
      aK.init_V(mc);
   }

   private static J.Inner_BufferHelper init(Minecraft mc, List<aK.Inner_StreamUtil> recipes) {
      if (mc != null && mc.player != null && mc.level != null) {
         AbstractCraftingMenu activeCrafting = mc.player.containerMenu instanceof AbstractCraftingMenu handler ? handler : null;
         Inventory playerInventory = mc.player.getInventory();
         List<ItemStack> inventoryStacks = new ArrayList<>(playerInventory.getContainerSize());

         for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            inventoryStacks.add(stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY);
         }

         boolean hasActive = activeCrafting != null;
         boolean activeIsPlayer = hasActive && init_Z(mc, activeCrafting);
         return new J.Inner_BufferHelper(
            inventoryStacks,
            hasActive,
            activeIsPlayer,
            hasActive ? activeCrafting.getGridWidth() : 0,
            hasActive ? activeCrafting.getGridHeight() : 0,
            hasActive ? init(activeCrafting) : List.of(),
            new ArrayList<>(init_Collection(mc)),
            recipes,
            SlotDisplayContext.fromLevel(mc.level),
            init_FeatureFlagSet(mc),
            mc.getResourceManager(),
            mc.getConnection() != null ? mc.getConnection().registryAccess() : mc.level.registryAccess(),
            FabricLoader.getInstance().getAllMods().size(),
            mc.getConnection()
         );
      } else {
         return null;
      }
   }

   public static List<J.Inner_TaskQueue> init_List(Minecraft mc) {
      return init(load_J_BufferHelper(mc));
   }

   public static J.Inner_SyncManager init(String recipeKey, int recipeId) {
      String key = recipeKey == null ? "" : recipeKey;
      int syncedIndex = init(key);
      return (recipe, synced) -> !key.isBlank() && key.equalsIgnoreCase(recipe.init_Str)
         || syncedIndex >= 0 && synced != null && synced.id().index() == syncedIndex
         || recipeId >= 0 && (recipe.init_Str.hashCode() == recipeId || synced != null && synced.id().index() == recipeId);
   }

   private static int init(String key) {
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

   public static List<J.Inner_TaskQueue> init(J.Inner_BufferHelper snap) {
      return init(snap, null);
   }

   public static List<J.Inner_TaskQueue> init(J.Inner_BufferHelper snap, J.Inner_SyncManager filter) {
      if (snap == null) {
         return List.of();
      }

      long perfStart = bK.init_J();
      List<J.Inner_RenderState> contexts = new ArrayList<>();
      if (snap.init_Z) {
         contexts.add(init(snap, snap.load_Z ? J.Inner_ConfigStore.init : J.Inner_ConfigStore.load, snap.init_I, snap.load_I, snap.load_List));
         if (snap.load_Z) {
            contexts.add(init(snap, J.Inner_ConfigStore.load, 3, 3, List.of()));
         }
      } else {
         contexts.add(init(snap, J.Inner_ConfigStore.load, 3, 3, List.of()));
      }

      List<ItemStack> bag = init(snap.init_List);
      Map<String, J.Inner_TaskQueue> optionsByKey = new HashMap<>();
      List<aK.Inner_StreamUtil> localRecipes = snap.write != null
         ? snap.write
         : aK.init_List(snap.init_RegistryAccess, snap.init_FeatureFlagSet, snap.init_ContextMap);

      for (J.Inner_RenderState context : contexts) {
         List<aK.Inner_StreamUtil> candidates = init(localRecipes, context.load_List);
         J.Inner_NetHelper materials = new J.Inner_NetHelper(bag, context.init_List);

         for (aK.Inner_StreamUtil localRecipe : candidates) {
            if (localRecipe.init(context.init_I, context.load_I) && !localRecipe.init_ItemStack.isEmpty()) {
               RecipeDisplayEntry syncedEntry = context.init_Map.get(localRecipe.load_Str);
               if (filter == null || filter.test(localRecipe, syncedEntry)) {
                  int maxCraftsByMaterials = materials.init(localRecipe);
                  int maxCraftsByStorage = init(localRecipe, context.init_List, bag, maxCraftsByMaterials);
                  J.Inner_TaskQueue option = new J.Inner_TaskQueue(
                     localRecipe, syncedEntry, maxCraftsByMaterials, maxCraftsByStorage, context.init_J_ConfigStore
                  );
                  init(optionsByKey, option);
               }
            }
         }
      }

      List<J.Inner_TaskQueue> options = new ArrayList<>(optionsByKey.values());
      options.sort(
         Comparator.<J.Inner_TaskQueue, Boolean>comparing(optionx -> !optionx.read_Z)
            .thenComparing(optionx -> optionx.init_J_ConfigStore != J.Inner_ConfigStore.init)
            .thenComparing(optionx -> -optionx.update)
            .thenComparing(optionx -> optionx.load_Str, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(optionx -> optionx.init_Str, String.CASE_INSENSITIVE_ORDER)
      );
      bK.load(filter == null ? "craft.scan" : "craft.resolve", perfStart, 250000000L);
      return options;
   }

   private static List<ItemStack> init(List<ItemStack> inventoryStacks) {
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

   private static J.Inner_RenderState init(J.Inner_BufferHelper snap, J.Inner_ConfigStore source, int gridWidth, int gridHeight, List<ItemStack> inputStacks) {
      J.Inner_TextFormat resolution = init(snap, gridWidth, gridHeight);
      return new J.Inner_RenderState(source, gridWidth, gridHeight, inputStacks, resolution.init_Map, resolution.init_List);
   }

   private static boolean init_Z(Minecraft mc, AbstractCraftingMenu craftingHandler) {
      return mc != null && mc.player != null && craftingHandler == mc.player.inventoryMenu;
   }

   private static FeatureFlagSet init_FeatureFlagSet(Minecraft mc) {
      if (mc == null) {
         return FeatureFlagSet.of();
      } else if (mc.getConnection() != null) {
         return mc.getConnection().enabledFeatures();
      } else {
         return mc.level != null ? mc.level.enabledFeatures() : FeatureFlagSet.of();
      }
   }

   private static List<ItemStack> init(AbstractCraftingMenu craftingHandler) {
      List<ItemStack> inputStacks = new ArrayList<>();

      for (Slot slot : craftingHandler.getInputGridSlots()) {
         if (slot != null && !slot.getItem().isEmpty()) {
            inputStacks.add(slot.getItem().copy());
         }
      }

      return inputStacks;
   }

   private static void init(Map<String, J.Inner_TaskQueue> optionsByKey, J.Inner_TaskQueue candidate) {
      if (candidate != null && candidate.init_Str != null && !candidate.init_Str.isBlank()) {
         String mergeKey = candidate.init_Str;
         J.Inner_TaskQueue existing = optionsByKey.get(mergeKey);
         if (existing == null) {
            optionsByKey.put(mergeKey, candidate);
         } else {
            boolean preferCandidate = false;
            if (candidate.init_ItemStack.getCount() != existing.init_ItemStack.getCount()) {
               preferCandidate = candidate.init_ItemStack.getCount() > existing.init_ItemStack.getCount();
            } else if (candidate.read_Z != existing.read_Z) {
               preferCandidate = candidate.read_Z;
            } else if (candidate.init_J_ConfigStore != existing.init_J_ConfigStore) {
               preferCandidate = candidate.init_J_ConfigStore == J.Inner_ConfigStore.init;
            } else if (candidate.update != existing.update) {
               preferCandidate = candidate.update > existing.update;
            } else if (!existing.init() && candidate.init()) {
               preferCandidate = true;
            }

            if (preferCandidate) {
               optionsByKey.put(mergeKey, candidate);
            }
         }
      }
   }

   private static List<aK.Inner_StreamUtil> init(List<aK.Inner_StreamUtil> localRecipes, List<aK.Inner_StreamUtil> syncedRecipes) {
      List<aK.Inner_StreamUtil> merged = new ArrayList<>();
      Set<String> seenSignatures = new HashSet<>();
      if (localRecipes != null) {
         for (aK.Inner_StreamUtil recipe : localRecipes) {
            if (recipe != null) {
               merged.add(recipe);
               if (recipe.load_Str != null && !recipe.load_Str.isBlank()) {
                  seenSignatures.add(recipe.load_Str);
               }
            }
         }
      }

      if (syncedRecipes != null) {
         for (aK.Inner_StreamUtil recipe : syncedRecipes) {
            if (recipe != null && (recipe.load_Str == null || !seenSignatures.contains(recipe.load_Str))) {
               merged.add(recipe);
               if (recipe.load_Str != null && !recipe.load_Str.isBlank()) {
                  seenSignatures.add(recipe.load_Str);
               }
            }
         }
      }

      return merged;
   }

   private static J.Inner_BufferHelper load_J_BufferHelper(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         return init(mc, () -> init(mc, aK.init_List(mc)));
      } else {
         return null;
      }
   }

   public static J.Inner_TaskQueue init(Minecraft mc, int recipeId) {
      return init(init(load_J_BufferHelper(mc), init("", recipeId)), "", recipeId);
   }

   public static J.Inner_TaskQueue init(Minecraft mc, String recipeKey) {
      return recipeKey != null && !recipeKey.isBlank() ? init(init(load_J_BufferHelper(mc), init(recipeKey, -1)), recipeKey, -1) : null;
   }

   public static J.Inner_TaskQueue init_J_TaskQueue(Minecraft mc, ItemStack output) {
      if (output != null && !output.isEmpty()) {
         J.Inner_TaskQueue best = null;

         for (J.Inner_TaskQueue option : init_List(mc)) {
            if (init(option.init_ItemStack, output)) {
               if (best == null) {
                  best = option;
               } else if (option.read_Z && !best.read_Z) {
                  best = option;
               } else if (option.read_Z == best.read_Z && option.update > best.update) {
                  best = option;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static ItemStack init_ItemStack(Minecraft mc) {
      if (mc != null && mc.player != null) {
         return mc.player.containerMenu instanceof AbstractCraftingMenu craftingHandler ? craftingHandler.getResultSlot().getItem().copy() : ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static int init(int outputPerCraft, int desiredAmount) {
      int safeOutput = Math.max(1, outputPerCraft);
      int safeDesired = Math.max(1, desiredAmount);
      return Math.max(1, (int)Math.ceil((double)safeDesired / safeOutput));
   }

   public static int load(int outputPerCraft, int desiredAmount) {
      int safeOutput = Math.max(1, outputPerCraft);
      return init(safeOutput, desiredAmount) * safeOutput;
   }

   public static int init(J.Inner_TaskQueue option, int desiredAmount) {
      if (option == null) {
         return load(1, desiredAmount);
      }

      int desiredCrafts = Math.min(option.update, init(option.init_ItemStack.getCount(), desiredAmount));
      return Math.max(0, desiredCrafts * Math.max(1, option.init_ItemStack.getCount()));
   }

   public static int init(J.Inner_TaskQueue option, int desiredAmount, boolean useMaxAmount) {
      if (option == null) {
         return useMaxAmount ? 0 : Math.max(1, desiredAmount);
      } else {
         return useMaxAmount ? option.sync : Math.max(1, desiredAmount);
      }
   }

   public static String init_Str(J.Inner_TaskQueue option) {
      if (option == null) {
         return "No recipe";
      } else if (option.read_Z) {
         return "Crafts x" + option.update;
      } else {
         return option.init_Z && !option.load_Z ? "No Room" : "Need Mats";
      }
   }

   public static List<Packet<?>> init_List(Minecraft mc, int recipeId, int desiredAmount) {
      return init_List(mc, null, recipeId, desiredAmount);
   }

   public static List<Packet<?>> init_List(Minecraft mc, String recipeKey, int recipeId, int desiredAmount) {
      if (mc != null && mc.player != null && mc.level != null) {
         if (!(mc.player.containerMenu instanceof AbstractCraftingMenu craftingHandler)) {
            return List.of();
         } else {
            J.Inner_TaskQueue option = init(mc, recipeKey, recipeId);
            if (option != null && option.update > 0) {
               int wantCrafts = Math.min(option.update, init(option.init_ItemStack.getCount(), desiredAmount));
               if (wantCrafts <= 0) {
                  return List.of();
               }

               I.Inner_CachePool layout = init_I_CachePool(mc, craftingHandler);
               if (layout != null) {
                  List<I.Inner_ConfigStore> phases = I.init_List(
                     layout,
                     init((AbstractContainerMenu)craftingHandler),
                     craftingHandler.getCarried(),
                     I.Inner_NetHelper.init_I_NetHelper_aK_StreamUtil(option.init_aK_StreamUtil),
                     wantCrafts,
                     mc.player.getInventory().getSelectedSlot()
                  );
                  if (!phases.isEmpty()) {
                     List<Packet<?>> packets = new ArrayList<>();

                     for (I.Inner_ConfigStore phase : phases) {
                        for (I.Inner_StreamUtil click : phase.init_List()) {
                           packets.add(
                              new ServerboundContainerClickPacket(
                                 craftingHandler.containerId,
                                 craftingHandler.getStateId(),
                                 (short)click.init_I(),
                                 (byte)click.load_I(),
                                 click.init_ContainerInput(),
                                 new Int2ObjectArrayMap<>(),
                                 HashedStack.EMPTY
                              )
                           );
                        }
                     }

                     return packets;
                  }
               }

               if (!option.init()) {
                  return List.of();
               }

               boolean craftAll = wantCrafts >= option.update;
               List<Packet<?>> packets = new ArrayList<>();
               int fills = craftAll ? 1 : wantCrafts;

               for (int i = 0; i < fills; i++) {
                  packets.add(new ServerboundPlaceRecipePacket(craftingHandler.containerId, option.init_RecipeDisplayEntry.id(), craftAll));
               }

               packets.add(
                  de.init(
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

   public static J.Inner_CachePool init_J_CachePool(Minecraft mc, int recipeId, int desiredAmount) {
      return init_J_CachePool(mc, null, recipeId, desiredAmount);
   }

   public static J.Inner_CachePool init(Minecraft mc, String recipeKey, int recipeId, int desiredAmount, Executor clientDispatch) {
      Executor previous = init_ThreadLocal.get();
      init_ThreadLocal.set(Objects.requireNonNull(clientDispatch));

      try {
         return init_J_CachePool(mc, recipeKey, recipeId, desiredAmount);
      } finally {
         if (previous == null) {
            init_ThreadLocal.remove();
         } else {
            init_ThreadLocal.set(previous);
         }
      }
   }

   public static J.Inner_CachePool init_J_CachePool(Minecraft mc, String recipeKey, int recipeId, int desiredAmount) {
      if (mc == null || mc.player == null || mc.getConnection() == null || mc.gameMode == null) {
         return J.Inner_CachePool.init("No network connection.");
      }

      if (autismclient.modules.aD.read_Z()) {
         return J.Inner_CachePool.init("Crafting is unavailable right now.");
      }

      boolean onClient = mc.isSameThread();
      J.Inner_TaskQueue requestedOption = init(mc, recipeKey, recipeId);
      J.Inner_CachePool rejected = init_J_CachePool(requestedOption);
      if (rejected != null) {
         return rejected;
      }

      J.Inner_StateCache preparedContext = init(mc, requestedOption, onClient);
      if (preparedContext != null && preparedContext.init_AbstractCraftingMe != null) {
         try {
            return init(mc, preparedContext.init_AbstractCraftingMe, requestedOption, desiredAmount, onClient);
         } finally {
            init(mc, preparedContext);
         }
      } else {
         return requestedOption.init_J_ConfigStore != J.Inner_ConfigStore.load && requestedOption.init_aK_StreamUtil.init(2, 2)
            ? J.Inner_CachePool.init("Open your inventory or a crafting screen first.")
            : J.Inner_CachePool.init("No reachable crafting table is available.");
      }
   }

   public static J.Inner_CachePool init_J_CachePool(J.Inner_TaskQueue option) {
      if (option == null) {
         return J.Inner_CachePool.init("That recipe is not available right now.");
      } else if (option.read_Z && option.update > 0) {
         return null;
      } else {
         return option.init_Z && !option.load_Z
            ? J.Inner_CachePool.load("No room in the inventory for " + option.load_Str + ".")
            : J.Inner_CachePool.init("Missing materials for " + option.load_Str + ".");
      }
   }

   private static J.Inner_CachePool init(Minecraft mc, AbstractCraftingMenu menu, J.Inner_TaskQueue option, int desiredAmount, boolean onClient) {
      long started = System.nanoTime();
      int perCraft = Math.max(1, option.init_ItemStack.getCount());
      int wantCrafts = Math.min(option.update, init(perCraft, desiredAmount));
      int roundedTarget = wantCrafts * perCraft;
      I.Inner_NetHelper spec = I.Inner_NetHelper.init_I_NetHelper_aK_StreamUtil(option.init_aK_StreamUtil);
      Integer before = init(mc, () -> init_I(mc, option.init_ItemStack));
      if (before == null) {
         return J.Inner_CachePool.init("The client did not respond.");
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
            init_V();
         }

         if (Thread.currentThread().isInterrupted()) {
            failure = "Craft interrupted before the next batch.";
            uncertain = phases > 0;
            break;
         }

         int left = wantCrafts - crafts;
         J.Inner_JsonUtil run = init(mc, () -> init(mc, menu, spec, left));
         if (run == null) {
            failure = "The client did not respond; dispatched work may have changed inventory.";
            uncertain = true;
            break;
         }

         if (run.init_Str() != null) {
            failure = run.init_Str();
            break;
         }

         phases++;
         if (onClient) {
            crafts += run.init_I_ConfigStore().load_I();
         } else {
            J.Inner_DataUtil confirm = init(mc, menu, run);
            if (confirm == J.Inner_DataUtil.init) {
               crafts += run.init_I_ConfigStore().load_I();
            } else {
               if (confirm != J.Inner_DataUtil.write && confirm != J.Inner_DataUtil.update && confirm != J.Inner_DataUtil.read) {
                  Integer now = init(mc, () -> init_I(mc, option.init_ItemStack));
                  int expected = (crafts + run.init_I_ConfigStore().load_I()) * perCraft;
                  if (confirm == J.Inner_DataUtil.load && now != null && now - before >= expected) {
                     crafts += run.init_I_ConfigStore().load_I();
                     continue;
                  }

                  failure = confirm == J.Inner_DataUtil.read ? "The crafting screen closed." : "The server did not accept the craft.";
                  if (confirm == J.Inner_DataUtil.load) {
                     init_V(mc, menu);
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
         Integer after = init(mc, () -> init_I(mc, option.init_ItemStack));
         crafted = !uncertain && after != null ? Math.max(0, after - before) : crafts * perCraft;
      }

      init_Logger.info(
         "[Autism] craft {}: {} of {} in {} burst(s), {} ms{}{}",
         option.init_Str,
         crafted,
         roundedTarget,
         phases,
         (System.nanoTime() - started) / 1000000L,
         failure == null ? "" : " (" + failure + ")",
         init_Str.isEmpty() ? "" : ", last confirmed on " + init_Str
      );
      if (uncertain) {
         return J.Inner_CachePool.load(failure, crafts * perCraft);
      } else if (crafted <= 0) {
         return J.Inner_CachePool.init(failure != null ? failure : "Craft failed.");
      } else {
         return crafted < roundedTarget
            ? J.Inner_CachePool.init("Crafted " + crafted + "/" + roundedTarget + " " + option.load_Str + "." + (failure == null ? "" : " " + failure), crafted)
            : J.Inner_CachePool.init("Crafted " + crafted + " " + option.load_Str + ".", crafted);
      }
   }

   private static J.Inner_JsonUtil init(Minecraft mc, AbstractCraftingMenu menu, I.Inner_NetHelper spec, int crafts) {
      if (mc.player == null || mc.gameMode == null) {
         return J.Inner_JsonUtil.init("No player.");
      }

      if (autismclient.modules.aD.read_Z()) {
         return J.Inner_JsonUtil.init("Crafting is unavailable right now.");
      }

      if (mc.player.containerMenu != menu) {
         return J.Inner_JsonUtil.init("The crafting screen closed.");
      }

      I.Inner_CachePool layout = init_I_CachePool(mc, menu);
      if (layout == null) {
         return J.Inner_JsonUtil.init("This crafting screen is not supported.");
      }

      I.Inner_BufferHelper outcome = I.init_I_BufferHelper(
         layout, init((AbstractContainerMenu)menu), menu.getCarried(), spec, crafts, mc.player.getInventory().getSelectedSlot()
      );
      if (outcome.init_I_ConfigStore() == null) {
         return J.Inner_JsonUtil.init(outcome.init_Str());
      }

      ItemStack[] initial = init((AbstractContainerMenu)menu);
      int stateBefore = menu.getStateId();
      long contentBefore = init_J(menu.containerId);
      init(mc, menu, layout, outcome.init_I_ConfigStore());
      return new J.Inner_JsonUtil(outcome.init_I_ConfigStore(), layout, initial, stateBefore, contentBefore, null);
   }

   private static void init(Minecraft mc, AbstractCraftingMenu menu, I.Inner_CachePool layout, I.Inner_ConfigStore phase) {
      for (int i = 0; i < phase.init_List().size(); i++) {
         I.Inner_StreamUtil click = phase.init_List().get(i);
         if (click.init_I() == layout.init_I()) {
            menu.getSlot(layout.init_I()).set(phase.write().get(i).copy());
         }

         ClientPacketListener connection = mc.getConnection();
         if (connection == null || mc.player == null || mc.player.containerMenu != menu) {
            throw new IllegalStateException("Crafting menu changed before click dispatch");
         }

         ItemStack[] before = init((AbstractContainerMenu)menu);
         menu.clicked(click.init_I(), click.load_I(), click.init_ContainerInput(), mc.player);
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
               (short)click.init_I(),
               (byte)click.load_I(),
               click.init_ContainerInput(),
               changed,
               HashedStack.create(menu.getCarried(), connection.decoratedHashOpsGenenerator())
            )
         );
         init(menu, phase.load_List().get(i), phase.read_List().get(i));
      }

      load(phase.init_List().size());
   }

   private static void init(AbstractContainerMenu menu, ItemStack[] slots, ItemStack carried) {
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

   private static J.Inner_DataUtil init(Minecraft mc, AbstractCraftingMenu menu, J.Inner_JsonUtil run) {
      long deadline = System.nanoTime() + init_J(mc) * 1000000L;
      int stale = run.init_I_ConfigStore().init_List().size();
      long[] last = new long[]{-1L, -9223372036854775808L, System.nanoTime()};
      long started = System.nanoTime();

      while (System.nanoTime() < deadline) {
         if (Thread.currentThread().isInterrupted()) {
            return J.Inner_DataUtil.write;
         }

         J.Inner_DataUtil state = init(mc, () -> {
            if (mc.player != null && mc.player.containerMenu == menu) {
               long packets = init_J(menu.containerId) - run.init_J();
               int stateId = menu.getStateId();
               long now = System.nanoTime();
               if (packets != last[0] || stateId != last[1]) {
                  last[0] = packets;
                  last[1] = stateId;
                  last[2] = now;
               }

               if (stateId == run.init_I() && packets == 0L) {
                  return null;
               } else {
                  boolean allAnswers = stale == 0 || packets >= stale;
                  boolean matches = init(menu, run);
                  if (matches && allAnswers) {
                     init_Str = "answers " + packets + "/" + stale + " in " + (now - started) / 1000000L + " ms";
                     return J.Inner_DataUtil.init;
                  } else {
                     return !matches && allAnswers && now - last[2] > 300000000L ? J.Inner_DataUtil.load : null;
                  }
               }
            } else {
               return J.Inner_DataUtil.read;
            }
         });
         if (state != null) {
            return state;
         }
      }

      return J.Inner_DataUtil.update;
   }

   private static boolean init(AbstractCraftingMenu menu, J.Inner_JsonUtil run) {
      ItemStack[] planned = run.init_I_ConfigStore().init_ItemStackArr();
      ItemStack[] initial = run.init_ItemStackArr();
      I.Inner_CachePool layout = run.init_I_CachePool();

      for (int i = 0; i < Math.min(planned.length, menu.slots.size()); i++) {
         if (i != layout.init_I()) {
            boolean planChanges = layout.init(i) || initial == null || i >= initial.length || !ItemStack.matches(initial[i], planned[i]);
            if (planChanges && !ItemStack.matches(menu.slots.get(i).getItem(), planned[i])) {
               return false;
            }
         }
      }

      return ItemStack.matches(menu.getCarried(), run.init_I_ConfigStore().init_ItemStack());
   }

   private static void init_V(Minecraft mc, AbstractCraftingMenu menu) {
      J.Inner_JsonUtil run = init(mc, () -> {
         if (mc.player != null && mc.gameMode != null && mc.player.containerMenu == menu) {
            I.Inner_CachePool layout = init_I_CachePool(mc, menu);
            if (layout == null) {
               return null;
            }

            I.Inner_ConfigStore phase = I.init(layout, init((AbstractContainerMenu)menu), menu.getCarried());
            if (phase == null) {
               return null;
            }

            ItemStack[] initial = init((AbstractContainerMenu)menu);
            int stateBefore = menu.getStateId();
            long contentBefore = init_J(menu.containerId);
            init(mc, menu, layout, phase);
            return new J.Inner_JsonUtil(phase, layout, initial, stateBefore, contentBefore, null);
         } else {
            return null;
         }
      });
      if (run != null && run.init_I_ConfigStore() != null && !mc.isSameThread()) {
         init(mc, menu, run);
      }
   }

   private static long init_J(Minecraft mc) {
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

   static I.Inner_CachePool init_I_CachePool(Minecraft mc, AbstractCraftingMenu menu) {
      if (mc != null && mc.player != null && menu != null) {
         if (menu == mc.player.inventoryMenu && menu.slots.size() == 46) {
            return I.Inner_CachePool.load_I_CachePool();
         } else {
            return menu instanceof CraftingMenu && menu.slots.size() == 46 && menu.getGridWidth() == 3 && menu.getGridHeight() == 3
               ? I.Inner_CachePool.init_I_CachePool()
               : null;
         }
      } else {
         return null;
      }
   }

   private static ItemStack[] init(AbstractContainerMenu menu) {
      ItemStack[] slots = new ItemStack[menu.slots.size()];

      for (int i = 0; i < slots.length; i++) {
         ItemStack stack = menu.slots.get(i).getItem();
         slots[i] = stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
      }

      return slots;
   }

   public static void init_V(int containerId) {
      init_Map.computeIfAbsent(containerId, id -> new AtomicLong()).incrementAndGet();
   }

   private static long init_J(int containerId) {
      AtomicLong count = init_Map.get(containerId);
      return count == null ? 0L : count.get();
   }

   private static synchronized void load(int clicks) {
      long now = System.nanoTime();

      for (int i = 0; i < clicks; i++) {
         init_ArrayDeque.addLast(now);
      }
   }

   private static synchronized long init_J() {
      long now = System.nanoTime();

      while (!init_ArrayDeque.isEmpty() && now - init_ArrayDeque.peekFirst() > 7000000000L) {
         init_ArrayDeque.pollFirst();
      }

      if (init_ArrayDeque.size() + 96 <= 280) {
         return 0L;
      }

      int over = init_ArrayDeque.size() + 96 - 280;
      Long oldest = null;
      int index = 0;

      for (Long time : init_ArrayDeque) {
         if (++index >= over) {
            oldest = time;
            break;
         }
      }

      return oldest == null ? 0L : Math.max(0L, 7000000000L - (now - oldest));
   }

   private static void init_V() {
      long wait = init_J();
      if (wait > 0L) {
         try {
            Thread.sleep(Math.min(7000L, wait / 1000000L + 1L));
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
   }

   private static J.Inner_TaskQueue init(Minecraft mc, String recipeKey, int recipeId) {
      String key = recipeKey == null ? "" : recipeKey;
      return init(init(load_J_BufferHelper(mc), init(key, recipeId)), key, recipeId);
   }

   private static J.Inner_StateCache init(Minecraft mc, J.Inner_TaskQueue option, boolean onClient) {
      if (mc != null && mc.player != null && option != null) {
         AbstractContainerMenu current = init(mc, () -> mc.player == null ? null : mc.player.containerMenu);
         if (current instanceof CraftingMenu tableMenu && option.init_aK_StreamUtil.init(tableMenu.getGridWidth(), tableMenu.getGridHeight())) {
            return new J.Inner_StateCache(tableMenu, false, null);
         } else if (option.init_aK_StreamUtil.init(2, 2)) {
            if (current == mc.player.inventoryMenu) {
               return new J.Inner_StateCache(mc.player.inventoryMenu, false, null);
            }

            Boolean closed = init(mc, () -> {
               if (mc.player == null) {
                  return false;
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu) {
                  mc.player.closeContainer();
               }

               return mc.player.containerMenu == mc.player.inventoryMenu;
            });
            return Boolean.TRUE.equals(closed) ? new J.Inner_StateCache(mc.player.inventoryMenu, false, null) : null;
         } else {
            if (onClient) {
               return null;
            }

            Screen restoreScreen = init(mc, () -> mc.player != null && mc.player.containerMenu == mc.player.inventoryMenu ? mc.gui.screen() : null);
            AbstractContainerMenu previous = init(mc, () -> mc.player == null ? null : mc.player.containerMenu);
            Boolean opened = init(mc, () -> {
               BlockPos tablePos = init_BlockPos(mc);
               if (tablePos != null && mc.gameMode != null && mc.player != null) {
                  BlockHitResult hitResult = init(mc, tablePos);
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

            long deadline = System.nanoTime() + init_J(mc) * 1000000L;

            while (System.nanoTime() < deadline) {
               if (Thread.currentThread().isInterrupted()) {
                  return null;
               }

               CraftingMenu table = init(
                  mc,
                  () -> mc.player != null && mc.player.containerMenu instanceof CraftingMenu menu && menu != previous && menu.getStateId() > 0 ? menu : null
               );
               if (table != null) {
                  return new J.Inner_StateCache(table, true, restoreScreen);
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private static void init(Minecraft mc, J.Inner_StateCache context) {
      if (mc != null && mc.player != null && context != null && context.init_Z) {
         init(mc, () -> {
            if (mc.player == null) {
               return null;
            }

            if (mc.player.containerMenu != mc.player.inventoryMenu) {
               mc.player.closeContainer();
            }

            if (context.init_Screen != null) {
               mc.gui.setScreen(context.init_Screen);
               mc.player.containerMenu = mc.player.inventoryMenu;
            }

            return null;
         });
      }
   }

   private static BlockPos init_BlockPos(Minecraft mc) {
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
                  BlockHitResult hitResult = init(mc, pos);
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

   private static BlockHitResult init(Minecraft mc, BlockPos tablePos) {
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

   private static <T> T init(Minecraft mc, Supplier<T> task) {
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
      Executor dispatch = init_ThreadLocal.get();
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

   private static J.Inner_TextFormat init(J.Inner_BufferHelper snap, int gridWidth, int gridHeight) {
      long gridKey = (long)gridWidth << 16 | gridHeight & 65535L;
      synchronized (init_Obj) {
         if (init_WeakReference.get() != snap.init_Obj || load_I != snap.read_List.size()) {
            load_Map.clear();
            init_WeakReference = new WeakReference<>(snap.init_Obj);
            load_I = snap.read_List.size();
         }

         J.Inner_TextFormat cached = load_Map.get(gridKey);
         if (cached != null) {
            return cached;
         }

         long perfStart = bK.init_J();
         List<RecipeDisplayEntry> supported = new ArrayList<>();

         for (RecipeDisplayEntry entry : snap.read_List) {
            if (init(entry, gridWidth, gridHeight)) {
               supported.add(entry);
            }
         }

         J.Inner_TextFormat fresh = init(supported, snap.init_ContextMap, snap.init_FeatureFlagSet);
         bK.load("craft.syncedResolve", perfStart, 100000000L);
         load_Map.put(gridKey, fresh);
         return fresh;
      }
   }

   static J.Inner_TextFormat init(List<RecipeDisplayEntry> entries, ContextMap params, FeatureFlagSet enabledFeatures) {
      List<aK.Inner_StreamUtil> recipes = new ArrayList<>();
      Map<String, RecipeDisplayEntry> bySignature = new HashMap<>();
      if (entries != null && params != null) {
         Set<String> seenKeys = new HashSet<>();

         for (RecipeDisplayEntry entry : entries) {
            if (entry != null && entry.display() != null) {
               aK.Inner_StreamUtil recipe = aK.init("synced:" + entry.id().index(), entry.display(), entry.craftingRequirements(), enabledFeatures, params);
               if (recipe != null) {
                  if (recipe.load_Str != null) {
                     bySignature.putIfAbsent(recipe.load_Str, entry);
                  }

                  if (seenKeys.add(recipe.init_Str)) {
                     recipes.add(recipe);
                  }
               }
            }
         }

         return new J.Inner_TextFormat(recipes, bySignature);
      } else {
         return new J.Inner_TextFormat(recipes, bySignature);
      }
   }

   private static int init(aK.Inner_StreamUtil localRecipe, List<ItemStack> inputStacks, List<ItemStack> inventoryStacks, int maxCraftsByMaterials) {
      if (localRecipe != null && !localRecipe.init_ItemStack.isEmpty() && inventoryStacks != null) {
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

         while (crafted < craftLimit && init(localRecipe, inventorySlots, gridPools) && init(localRecipe.init_ItemStack, inventorySlots)) {
            crafted++;
         }

         return crafted;
      } else {
         return 0;
      }
   }

   private static boolean init(aK.Inner_StreamUtil localRecipe, List<ItemStack> inventorySlots, List<ItemStack> gridPools) {
      if (localRecipe != null && !localRecipe.load_List.isEmpty()) {
         for (Ingredient ingredient : localRecipe.load_List) {
            if (ingredient != null && !ingredient.isEmpty() && !init(gridPools, ingredient, false) && !init(inventorySlots, ingredient, true)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean init(List<ItemStack> stacks, Ingredient ingredient, boolean preferSmallestStack) {
      if (stacks != null && !stacks.isEmpty() && ingredient != null && !ingredient.isEmpty()) {
         int bestIndex = -1;
         int bestCount = 2147483647;

         for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (I.init_Z(stack) && ingredient.test(stack)) {
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

   private static boolean init(ItemStack resultStack, List<ItemStack> inventorySlots) {
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

   private static Collection<RecipeDisplayEntry> init_Collection(Minecraft mc) {
      if (mc != null && mc.player != null) {
         if (mc.player.getRecipeBook() instanceof ClientRecipeBook recipeBook) {
            try {
               if (init_Field == null) {
                  init_Field = ClientRecipeBook.class.getDeclaredField("known");
                  init_Field.setAccessible(true);
               }

               if (init_Field.get(recipeBook) instanceof Map<?, ?> recipeMap) {
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

   private static boolean init(RecipeDisplayEntry entry, int gridWidth, int gridHeight) {
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

   private static boolean init(ItemStack left, ItemStack right) {
      return left != null
         && right != null
         && !left.isEmpty()
         && !right.isEmpty()
         && left.getCount() == right.getCount()
         && ItemStack.isSameItemSameComponents(left, right);
   }

   private static int init_I(Minecraft mc, ItemStack template) {
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

   public static List<J.Inner_TaskQueue> init(List<J.Inner_TaskQueue> all, String query) {
      if (query != null && !query.isBlank()) {
         String lower = query.toLowerCase();
         return all.stream().filter(o -> o.write_Str.contains(lower)).toList();
      } else {
         return new ArrayList<>(all);
      }
   }

   public static J.Inner_TaskQueue init(List<J.Inner_TaskQueue> recipes, String recipeKey, int recipeId) {
      if (recipes == null) {
         return null;
      }

      String key = recipeKey == null ? "" : recipeKey;
      int syncedIndex = init(key);

      for (J.Inner_TaskQueue option : recipes) {
         if (!key.isBlank() && key.equalsIgnoreCase(option.init_Str)) {
            return option;
         }
      }

      for (J.Inner_TaskQueue option : recipes) {
         if (syncedIndex >= 0 && option.load_I == syncedIndex) {
            return option;
         }

         if (recipeId >= 0 && (option.init_I == recipeId || option.load_I == recipeId)) {
            return option;
         }
      }

      return null;
   }

   private static List<aK.Inner_StreamUtil> update(Minecraft mc) {
      List<aK.Inner_StreamUtil> local = aK.init_List(mc);
      if (!local.isEmpty()) {
         return local;
      }

      List<RecipeDisplayEntry> entries = new ArrayList<>(init_Collection(mc));
      if (read_List != null && entries.size() == read_I) {
         return read_List;
      }

      FeatureFlagSet features = mc.getConnection() != null ? mc.getConnection().enabledFeatures() : mc.level.enabledFeatures();
      read_List = List.copyOf(init(entries, SlotDisplayContext.fromLevel(mc.level), features).init_List);
      read_I = entries.size();
      return read_List;
   }

   public static synchronized List<J.Inner_EventBus> load_List(Minecraft mc) {
      if (mc != null && mc.level != null) {
         List<aK.Inner_StreamUtil> source = update(mc);
         if (source == init_List) {
            return load_List;
         }

         ContextMap slotContext = SlotDisplayContext.fromLevel(mc.level);
         List<J.Inner_EventBus> plans = new ArrayList<>();
         Map<Item, List<J.Inner_EventBus>> byOutput = new HashMap<>();

         for (aK.Inner_StreamUtil recipe : source) {
            J.Inner_EventBus plan = init(recipe, slotContext);
            if (plan != null) {
               plans.add(plan);
               byOutput.computeIfAbsent(plan.init_Item(), item -> new ArrayList<>()).add(plan);
            }
         }

         for (List<J.Inner_EventBus> group : byOutput.values()) {
            group.sort(Comparator.comparingInt(planx -> planx.init_List().size()));
         }

         load_List = List.copyOf(plans);
         read_Map = Map.copyOf(byOutput);
         init_List = source;
         return load_List;
      } else {
         return List.of();
      }
   }

   public static synchronized JsonObject init_JsonObject(Minecraft mc) {
      JsonObject out = new JsonObject();
      if (mc != null && mc.level != null) {
         aK.Inner_CachePool snapshot = aK.init_aK_CachePool(mc);
         List<J.Inner_EventBus> plans = load_List(mc);
         List<RecipeDisplayEntry> synced = new ArrayList<>(init_Collection(mc));
         FeatureFlagSet features = mc.getConnection() != null ? mc.getConnection().enabledFeatures() : mc.level.enabledFeatures();
         int syncedDerived = init(synced, SlotDisplayContext.fromLevel(mc.level), features).init_List.size();
         out.addProperty("source", snapshot.init_Str());
         out.addProperty("filesRead", snapshot.init_I());
         out.addProperty("filesSkipped", snapshot.load_I());
         out.addProperty("craftingRecipes", snapshot.init_List().size());
         out.addProperty("cookingRecipes", snapshot.load_List().size());
         out.addProperty("plansBuilt", plans.size());
         out.addProperty("syncedBookEntries", synced.size());
         out.addProperty("syncedDerivedRecipes", syncedDerived);
         return out;
      } else {
         out.addProperty("state", "no level");
         return out;
      }
   }

   public static List<J.Inner_EventBus> init(Minecraft mc, Item output) {
      if (output == null) {
         return List.of();
      }

      load_List(mc);
      return read_Map.getOrDefault(output, List.of());
   }

   public static synchronized List<J.Inner_StreamUtil> read(Minecraft mc) {
      List<aK.Inner_DataUtil> source = aK.load_List(mc);
      if (source == write_List) {
         return update_List;
      }

      List<J.Inner_StreamUtil> plans = new ArrayList<>();
      Map<Item, List<J.Inner_StreamUtil>> byOutput = new HashMap<>();

      for (aK.Inner_DataUtil recipe : source) {
         J.Inner_StreamUtil plan = new J.Inner_StreamUtil(
            recipe.init_Str(),
            recipe.init_List(),
            recipe.init_ItemStack().getItem(),
            Math.max(1, recipe.init_ItemStack().getCount()),
            recipe.load(),
            recipe.init_I()
         );
         plans.add(plan);
         byOutput.computeIfAbsent(plan.init_Item(), item -> new ArrayList<>()).add(plan);
      }

      for (List<J.Inner_StreamUtil> group : byOutput.values()) {
         group.sort(Comparator.comparingInt(planx -> "minecraft:furnace".equals(planx.load_Str()) ? 0 : 1));
      }

      update_List = List.copyOf(plans);
      write_Map = Map.copyOf(byOutput);
      write_List = source;
      return update_List;
   }

   public static List<J.Inner_StreamUtil> load(Minecraft mc, Item output) {
      if (output == null) {
         return List.of();
      }

      read(mc);
      return write_Map.getOrDefault(output, List.of());
   }

   public static synchronized List<J.Inner_FileCache> write(Minecraft mc) {
      if (mc != null && mc.level != null) {
         RecipeAccess access = mc.level.recipeAccess();
         if (access == load_Obj) {
            return sync;
         }

         ContextMap slotContext = SlotDisplayContext.fromLevel(mc.level);
         List<J.Inner_FileCache> plans = new ArrayList<>();
         Map<Item, List<J.Inner_FileCache>> byOutput = new HashMap<>();

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

                  J.Inner_FileCache plan = new J.Inner_FileCache(key, inputs, result.getItem(), Math.max(1, result.getCount()), "minecraft:stonecutter");
                  plans.add(plan);
                  byOutput.computeIfAbsent(plan.init_Item(), item -> new ArrayList<>()).add(plan);
               }
            }
         }

         sync = List.copyOf(plans);
         update_Map = Map.copyOf(byOutput);
         load_Obj = access;
         return sync;
      } else {
         return List.of();
      }
   }

   public static List<J.Inner_FileCache> read(Minecraft mc, Item output) {
      if (output == null) {
         return List.of();
      }

      write(mc);
      return update_Map.getOrDefault(output, List.of());
   }

   static J.Inner_EventBus init(aK.Inner_StreamUtil recipe, ContextMap slotContext) {
      if (recipe != null && !recipe.init_ItemStack.isEmpty() && !recipe.load_List.isEmpty()) {
         List<J.Inner_UiHelper> inputs = new ArrayList<>(recipe.load_List.size());

         for (Ingredient ingredient : recipe.load_List) {
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

            inputs.add(new J.Inner_UiHelper(accepts));
         }

         return new J.Inner_EventBus(
            recipe.init_Str, recipe.init_ItemStack.getItem(), Math.max(1, recipe.init_ItemStack.getCount()), !recipe.init(2, 2), inputs
         );
      } else {
         return null;
      }
   }

   public static final class Inner_BufferHelper {
      final List<ItemStack> init_List;
      final boolean init_Z;
      final boolean load_Z;
      final int init_I;
      final int load_I;
      final List<ItemStack> load_List;
      final List<RecipeDisplayEntry> read_List;
      final List<aK.Inner_StreamUtil> write;
      final ContextMap init_ContextMap;
      final FeatureFlagSet init_FeatureFlagSet;
      final ResourceManager init_ResourceManager;
      final RegistryAccess init_RegistryAccess;
      final int read_I;
      public final Object init_Obj;

      Inner_BufferHelper(
         List<ItemStack> inventoryStacks,
         boolean hasActiveCraftingMenu,
         boolean activeIsPlayerMenu,
         int activeGridWidth,
         int activeGridHeight,
         List<ItemStack> activeGridStacks,
         List<RecipeDisplayEntry> rawSyncedEntries,
         List<aK.Inner_StreamUtil> localRecipes,
         ContextMap slotContext,
         FeatureFlagSet enabledFeatures,
         ResourceManager resourceManager,
         RegistryAccess registryAccess,
         int modCount,
         Object connection
      ) {
         this.init_List = inventoryStacks;
         this.init_Z = hasActiveCraftingMenu;
         this.load_Z = activeIsPlayerMenu;
         this.init_I = activeGridWidth;
         this.load_I = activeGridHeight;
         this.load_List = activeGridStacks;
         this.read_List = rawSyncedEntries;
         this.write = localRecipes;
         this.init_ContextMap = slotContext;
         this.init_FeatureFlagSet = enabledFeatures;
         this.init_ResourceManager = resourceManager;
         this.init_RegistryAccess = registryAccess;
         this.read_I = modCount;
         this.init_Obj = connection;
      }
   }

   public static final class Inner_CachePool {
      public final boolean init_Z;
      public final boolean load;
      public final String init_Str;
      public final int init_I;
      public final J.Inner_CachePool.Inner_DataUtil init_J_CachePool_DataUt;

      private Inner_CachePool(boolean success, String message, int craftedAmount) {
         this(success, false, message, craftedAmount);
      }

      private Inner_CachePool(boolean success, boolean uncertain, String message, int craftedAmount) {
         this(success, uncertain, message, craftedAmount, J.Inner_CachePool.Inner_DataUtil.init);
      }

      private Inner_CachePool(boolean success, boolean uncertain, String message, int craftedAmount, J.Inner_CachePool.Inner_DataUtil rejection) {
         this.init_Z = success;
         this.load = uncertain;
         this.init_Str = message;
         this.init_I = craftedAmount;
         this.init_J_CachePool_DataUt = rejection;
      }

      public static J.Inner_CachePool init(String message, int craftedAmount) {
         return new J.Inner_CachePool(true, message, craftedAmount);
      }

      public static J.Inner_CachePool init(String message) {
         return new J.Inner_CachePool(false, message, 0);
      }

      public static J.Inner_CachePool load(String message, int confirmedAmount) {
         return new J.Inner_CachePool(false, true, message, confirmedAmount);
      }

      public static J.Inner_CachePool load(String message) {
         return new J.Inner_CachePool(false, false, message, 0, J.Inner_CachePool.Inner_DataUtil.load);
      }

      public enum Inner_DataUtil {
         init,
         load;
      }
   }

   public enum Inner_ConfigStore {
      init,
      load;
   }

   private enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update;
   }

   public record Inner_EventBus(String init_Str, Item init_Item, int init_I, boolean init_Z, List<J.Inner_UiHelper> init_List) {
      public Inner_EventBus {
         init_List = List.copyOf(init_List);
      }
   }

   public record Inner_FileCache(String init_Str, List<Item> init_List, Item init_Item, int init_I, String load) {
      public Inner_FileCache {
         init_List = List.copyOf(init_List);
      }
   }

   private record Inner_JsonUtil(
      I.Inner_ConfigStore init_I_ConfigStore, I.Inner_CachePool init_I_CachePool, ItemStack[] init_ItemStackArr, int init_I, long init_J, String init_Str
   ) {
      static J.Inner_JsonUtil init(String failure) {
         return new J.Inner_JsonUtil(null, null, null, 0, 0L, failure);
      }
   }

   private static final class Inner_NetHelper {
      private final Map<Holder<Item>, Integer> init = new HashMap<>();

      Inner_NetHelper(List<ItemStack> bag, List<ItemStack> grid) {
         this.init(bag);
         this.init(grid);
      }

      private void init(List<ItemStack> stacks) {
         if (stacks != null) {
            for (ItemStack stack : stacks) {
               if (I.init_Z(stack)) {
                  this.init.merge(stack.typeHolder(), stack.getCount(), Integer::sum);
               }
            }
         }
      }

      int init(aK.Inner_StreamUtil recipe) {
         if (recipe != null && !recipe.load_List.isEmpty() && !this.init.isEmpty()) {
            int bound = 2147483647;

            for (Ingredient ingredient : recipe.load_List) {
               if (ingredient == null || ingredient.isEmpty()) {
                  return 0;
               }

               int held = 0;

               for (Entry<Holder<Item>, Integer> entry : this.init.entrySet()) {
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
            this.init.forEach(contents::account);
            return contents.tryPickAll(recipe.load_List, Math.max(1, bound), null);
         } else {
            return 0;
         }
      }
   }

   private static final class Inner_RenderState {
      private final J.Inner_ConfigStore init_J_ConfigStore;
      private final int init_I;
      private final int load_I;
      private final List<ItemStack> init_List;
      private final Map<String, RecipeDisplayEntry> init_Map;
      private final List<aK.Inner_StreamUtil> load_List;

      private Inner_RenderState(
         J.Inner_ConfigStore source,
         int gridWidth,
         int gridHeight,
         List<ItemStack> inputStacks,
         Map<String, RecipeDisplayEntry> syncedBySignature,
         List<aK.Inner_StreamUtil> syncedRecipes
      ) {
         this.init_J_ConfigStore = source;
         this.init_I = gridWidth;
         this.load_I = gridHeight;
         this.init_List = inputStacks == null ? List.of() : List.copyOf(inputStacks);
         this.init_Map = syncedBySignature == null ? Map.of() : Map.copyOf(syncedBySignature);
         this.load_List = syncedRecipes == null ? List.of() : List.copyOf(syncedRecipes);
      }
   }

   private static final class Inner_StateCache {
      private final AbstractCraftingMenu init_AbstractCraftingMe;
      private final boolean init_Z;
      private final Screen init_Screen;

      private Inner_StateCache(AbstractCraftingMenu handler, boolean closeAfter, Screen restoreScreen) {
         this.init_AbstractCraftingMe = handler;
         this.init_Z = closeAfter;
         this.init_Screen = restoreScreen;
      }
   }

   public record Inner_StreamUtil(String init_Str, List<Item> init_List, Item init_Item, int init_I, String load_Str, int load_I) {
      public Inner_StreamUtil(String recipeKey, List<Item> inputs, Item output, int outputCount, String station) {
         this(recipeKey, inputs, output, outputCount, station, "minecraft:furnace".equals(station) ? 200 : 100);
      }

      public Inner_StreamUtil {
         init_List = List.copyOf(init_List);
      }
   }

   public interface Inner_SyncManager {
      boolean test(aK.Inner_StreamUtil var1, RecipeDisplayEntry var2);
   }

   public static final class Inner_TaskQueue {
      public final String init_Str;
      public final int init_I;
      public final int load_I;
      public final RecipeDisplayEntry init_RecipeDisplayEntry;
      public final aK.Inner_StreamUtil init_aK_StreamUtil;
      public final ItemStack init_ItemStack;
      public final Component init_Component;
      public final String load_Str;
      public final String read_Str;
      public final String write_Str;
      public final boolean init_Z;
      public final boolean load_Z;
      public final int read_I;
      public final int write_I;
      public final boolean read_Z;
      public final int update;
      public final int sync;
      public final J.Inner_ConfigStore init_J_ConfigStore;

      public Inner_TaskQueue(
         aK.Inner_StreamUtil localRecipe, RecipeDisplayEntry syncedEntry, int maxCraftsByMaterials, int maxCraftsByStorage, J.Inner_ConfigStore craftSource
      ) {
         this.init_aK_StreamUtil = localRecipe;
         this.init_RecipeDisplayEntry = syncedEntry;
         this.init_Str = localRecipe.init_Str;
         this.init_I = localRecipe.init_Str.hashCode();
         this.load_I = syncedEntry != null ? syncedEntry.id().index() : -1;
         this.init_ItemStack = localRecipe.init_ItemStack.copy();
         this.init_Component = this.init_ItemStack.getHoverName().copy();
         this.load_Str = this.init_Component.getString();
         this.read_I = Math.max(0, maxCraftsByMaterials);
         this.write_I = Math.max(0, maxCraftsByStorage);
         this.init_Z = this.read_I > 0;
         this.load_Z = this.write_I > 0;
         this.update = Math.max(0, Math.min(this.read_I, this.write_I));
         this.read_Z = this.update > 0;
         this.sync = this.update * Math.max(1, this.init_ItemStack.getCount());
         this.init_J_ConfigStore = craftSource == null ? J.Inner_ConfigStore.init : craftSource;
         Identifier id = this.init_ItemStack.getItem() == null ? null : BuiltInRegistries.ITEM.getKey(this.init_ItemStack.getItem());
         this.read_Str = id == null ? "" : id.toString();
         this.write_Str = (this.load_Str + " " + this.read_Str + " " + this.init_Str).toLowerCase();
      }

      public boolean init() {
         return this.init_RecipeDisplayEntry != null && this.load_I >= 0;
      }
   }

   static final class Inner_TextFormat {
      final List<aK.Inner_StreamUtil> init_List;
      final Map<String, RecipeDisplayEntry> init_Map;

      Inner_TextFormat(List<aK.Inner_StreamUtil> recipes, Map<String, RecipeDisplayEntry> bySignature) {
         this.init_List = recipes;
         this.init_Map = bySignature;
      }
   }

   public record Inner_UiHelper(List<Item> init) {
      public Inner_UiHelper {
         init = List.copyOf(init);
      }

      public boolean init(Item item) {
         return this.init.contains(item);
      }
   }
}
