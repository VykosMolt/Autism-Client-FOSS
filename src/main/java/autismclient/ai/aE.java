package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import autismclient.util.BlockBreakSpeedHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class aE {
   private static final int sync = 5;
   static final int init_I = 8;
   private static final int flush = 64;
   static final double init_D = 160.0;
   static final double load_D = 40.0;
   static final int load_I = 8;
   private static final Set<String> init_Set = Set.of(
      "minecraft:stone",
      "minecraft:dirt",
      "minecraft:gravel",
      "minecraft:sand",
      "minecraft:deepslate",
      "minecraft:netherrack",
      "minecraft:tuff",
      "minecraft:andesite",
      "minecraft:diorite",
      "minecraft:granite"
   );
   static final int read = 8;
   static final int write = 32;
   static final int update = 32;

   private aE() {
   }

   static AiOperation init(JsonObject args) {
      return init(args, 0, new LinkedHashSet<>());
   }

   static AiOperation init(JsonObject args, final int depth, final Set<String> visiting) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = ag.init_Str_JsonObject(args, "item", "");
         final af.Inner_DataUtil target = af.load_af_DataUtil(spec);
         if (depth == 0 && !ag.init_Z_JsonObject(args, "staged", false) && target.items().size() == 1) {
            String only = BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString();
            int want = ag.init_Z_JsonObject(args, "additional", false)
               ? af.init_I(target) + ag.init_I_JsonObject(args, "count", 1)
               : ag.init_I_JsonObject(args, "count", 1);
            boolean equippedObsidian = only.equals("minecraft:obsidian") && bx.init_bx_CachePool(mc).ordinal() >= bx.Inner_CachePool.update.ordinal();
            if (equippedObsidian && want >= 1 && want <= 2304 && af.init_I(target) < want) {
               return aD.load(want, ag.init_I_JsonObject(args, "timeoutSeconds", 600));
            }

            if (!equippedObsidian && bx.init_Z(only, want)) {
               return bx.init_AiOperation_JsonObject(args, only, want);
            }

            if (only.equals("minecraft:water_bucket") && want >= 1 && want <= 16 && af.init_I(target) < want) {
               return aD.init(want, ag.init_I_JsonObject(args, "timeoutSeconds", 300));
            }
         }

         int requested = ag.init_I_JsonObject(args, "count", 1);
         if (requested >= 1 && requested <= 2304) {
            boolean additional = ag.init_Z_JsonObject(args, "additional", false);
            final boolean allowCombat = ag.init_Z_JsonObject(args, "allowCombat", true);
            final boolean allowMining = ag.init_Z_JsonObject(args, "allowMining", true);
            final int radius = Math.max(8, Math.min(128, ag.init_I_JsonObject(args, "radius", 64)));
            final int timeoutSeconds = Math.max(10, Math.min(3600, ag.init_I_JsonObject(args, "timeoutSeconds", 600)));
            final int held = af.init_I(target);
            final int goal = additional ? held + requested : requested;
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object init_Obj = mc.level;
               final Object load_Obj = mc.getConnection();
               final LocalPlayer init_LocalPlayer = mc.player;
               final List<aE.Inner_DataUtil> init_List = new ArrayList<>();
               final JsonArray init_JsonArray = new JsonArray();
               final Set<String> init_Set = new LinkedHashSet<>(target.itemIds());
               final X.Inner_CachePool init_X_CachePool = X.Inner_CachePool.standard().keeping(this.init_Set);
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               String init_Str = "";
               int init_I;
               aE.Inner_DataUtil init_aE_DataUtil;
               String load_Str = "";
               Set<Item> load_Set;
               Set<Item> read_Set;
               List<Block> load_List;
               List<EntityType<?>> read_List;
               boolean init_Z;
               boolean load_Z;
               int load_I;
               boolean read_Z;
               boolean write_Z;
               Boolean init_Boolean;
               af.Inner_DataUtil init_af_DataUtil = target;
               af.Inner_DataUtil load_af_DataUtil;
               String read_Str;
               boolean update;
               String write_Str;
               boolean sync;
               int read_I;
               final EnumMap<aE.Inner_DataUtil, Integer> init_EnumMap = new EnumMap<>(aE.Inner_DataUtil.class);

               @Override
               public AiOperation.Result tick() {
                  if (this.sync || mc.player == null || mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.init_LocalPlayer) {
                     return this.init(AiOperation.Status.CANCELLED, "Obtain cancelled or world/player changed");
                  }

                  if (!mc.player.isAlive()) {
                     return this.init(AiOperation.Status.FAILED, "Player died while obtaining supplies");
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        "Obtain deadline reached with " + af.init_I(this.init_af_DataUtil) + "/" + goal
                     );
                  }

                  if (af.init_I(this.init_af_DataUtil) >= goal && this.init_AiOperation == null) {
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.init_aE_DataUtil = null;
                     }

                     return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + this.write_Str_2());
                  } else {
                     AiOperation.Result guarded = this.init_AiOperation_Result_17();
                     if (guarded != null) {
                        return guarded;
                     }

                     if (this.init_AiOperation == null) {
                        aE.Inner_DataUtil next = this.init_aE_DataUtil();
                        if (next == null) {
                           String why = this.init_List.isEmpty()
                              ? aE.init(
                                 !this.init_Set_6().isEmpty(),
                                 !this.load_Set_4().isEmpty(),
                                 !this.init_List_6().isEmpty(),
                                 bz.init_Z(target),
                                 !this.load_List_3().isEmpty()
                              )
                              : "every strategy was tried";
                           return this.init(
                              AiOperation.Status.FAILED, "No remaining route to " + spec + " (" + af.init_I(this.init_af_DataUtil) + "/" + goal + "); " + why
                           );
                        } else {
                           this.init_AiOperation = bJ.init_AiOperation_Str(
                              "obtain", next.name() + " " + spec + " " + af.init_I(this.init_af_DataUtil) + "/" + goal, this.init_AiOperation(next)
                           );
                           if (this.init_AiOperation == null) {
                              this.init_List.add(next);
                              return this.init_AiOperation_Result_9("Strategy " + next + " unavailable; trying the next");
                           } else {
                              this.init_aE_DataUtil = next;
                              this.read_I = af.init_I(this.init_af_DataUtil);
                              return this.init_AiOperation_Result_9("Starting " + next.name().toLowerCase(Locale.ROOT) + " for " + spec);
                           }
                        }
                     } else {
                        AiOperation.Result result = this.init_AiOperation.tick();
                        if (!result.terminal()) {
                           return this.init_AiOperation_Result_9(result.message());
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.init_JsonArray.add(aE.init(this.init_aE_DataUtil.name(), this.load_Str, result));
                        if (result.status() == AiOperation.Status.CANCELLED) {
                           return this.init(AiOperation.Status.CANCELLED, "Obtain cancelled");
                        }

                        if (result.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(AiOperation.Status.UNCERTAIN, result.message());
                        }

                        if (af.init_I(this.init_af_DataUtil) >= goal) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + this.write_Str_2());
                        }

                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           AiOperation detour = this.init(result);
                           if (detour != null) {
                              this.init_AiOperation = bJ.init_AiOperation_Str("obtain", "station detour for " + spec, detour);
                              return this.init_AiOperation_Result_9(
                                 this.load_Z && this.init_aE_DataUtil == aE.Inner_DataUtil.write
                                    ? "Smelting needs a furnace; making one first"
                                    : "Craft needs a crafting table; obtaining one first"
                              );
                           }

                           if ((this.init_aE_DataUtil == aE.Inner_DataUtil.sync || this.init_aE_DataUtil == aE.Inner_DataUtil.update)
                              && !this.read_Z
                              && aE.init_Str(result.dataJson()) != null) {
                              this.read_Z = true;
                              this.init_aE_DataUtil = null;
                              return this.init_AiOperation_Result_9("Mining tool broke; re-tooling before one more attempt");
                           }

                           if (this.init_aE_DataUtil == aE.Inner_DataUtil.update && !this.update && aE.init(result)) {
                              String family = this.read_Str_3();
                              if (family != null && !this.write_Z_6()) {
                                 this.update = true;
                                 this.write_Str = family;
                                 this.init_af_DataUtil = af.load_af_DataUtil(family);
                                 this.load_Set = null;
                                 this.read_Set = null;
                                 this.load_List = null;
                                 this.read_List = null;
                                 this.init_AiOperation = bJ.init_AiOperation_Str(
                                    "obtain", "family fallback " + family, this.init(Math.max(15, this.init_I_19()))
                                 );
                                 if (this.init_AiOperation == null) {
                                    return this.init_AiOperation_Result_9("No harvestable source for the " + family + " family");
                                 }

                                 this.load_Str = "gather " + family + " (family fallback from " + spec + ")";
                                 return this.init_AiOperation_Result_9("No " + spec + " in range; gathering the " + family + " family instead");
                              }
                           }

                           if (this.init_aE_DataUtil == aE.Inner_DataUtil.read && this.load_I < 2 && result.dataJson().contains("\"missing\"")) {
                              this.load_I++;
                              this.init_aE_DataUtil = null;
                              return this.init_AiOperation_Result_9("Craft still short on materials; fetching the rest");
                           }

                           if (af.init_I(this.init_af_DataUtil) > this.read_I && this.init_EnumMap.merge(this.init_aE_DataUtil, 1, Integer::sum) <= 8) {
                              this.init_aE_DataUtil = null;
                              return this.init_AiOperation_Result_9("Strategy " + result.status() + " after progress; another pass");
                           }

                           this.init_List.add(this.init_aE_DataUtil);
                        }

                        this.init_aE_DataUtil = null;
                        return this.init_AiOperation_Result_9("Strategy " + result.status() + "; re-evaluating");
                     }
                  }
               }

               AiOperation.Result init_AiOperation_Result_17() {
                  if (this.load_AiOperation != null) {
                     AiOperation.Result result = this.load_AiOperation.tick();
                     if (result.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, result.message());
                     }

                     if (!result.terminal()) {
                        return this.init_AiOperation_Result_9("Interrupted: " + this.init_Str + " - " + result.message());
                     }

                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                     return this.init_AiOperation_Result_9("Resuming after " + this.init_Str);
                  } else if (autismclient.ai.a.AiMotionSafety.fill_Z() && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())) {
                     X.Inner_BufferHelper vitals = X.init_X_BufferHelper();
                     AiOperation action = X.init_AiOperation_X_CachePool(this.init_X_CachePool, vitals, this.init_I_19());
                     if (action == null) {
                        return null;
                     }

                     this.init_Str = X.init_Str(this.init_X_CachePool, vitals);
                     this.init_I++;
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.init_aE_DataUtil = null;
                     }

                     this.load_AiOperation = bJ.init_AiOperation_Str("survival", "obtain: " + this.init_Str, action);
                     return this.init_AiOperation_Result_9("Interrupted: " + this.init_Str);
                  } else {
                     return null;
                  }
               }

               aE.Inner_DataUtil init_aE_DataUtil() {
                  if (!this.init_List.contains(aE.Inner_DataUtil.read) && !this.init_Set_6().isEmpty()) {
                     g.Inner_StreamUtil graph = e.init_g_StreamUtil();
                     if (this.init_Set_6().stream().anyMatch(item -> {
                        g.Inner_CachePool plan = g.init(graph, BuiltInRegistries.ITEM.getKey(item).toString(), goal);
                        return plan.complete() && !plan.steps().isEmpty();
                     })) {
                        return aE.Inner_DataUtil.read;
                     }
                  }

                  if (!this.init_List.contains(aE.Inner_DataUtil.write)
                     && this.load_Set_4()
                        .stream()
                        .anyMatch(
                           output -> autismclient.util.AutoCraftingEngine.load(mc, output)
                              .stream()
                              .anyMatch(
                                 p -> p.init_List().stream().mapToInt(i -> S.init_I_Str(BuiltInRegistries.ITEM.getKey(i).toString())).sum()
                                    >= goal - af.init_I(this.init_af_DataUtil)
                              )
                        )) {
                     return aE.Inner_DataUtil.write;
                  }

                  if (allowMining
                     && !this.init_List.contains(aE.Inner_DataUtil.update)
                     && this.init_af_DataUtil
                        .items()
                        .stream()
                        .anyMatch(
                           item -> item instanceof BlockItem form
                              && !aE.init().contains(BuiltInRegistries.ITEM.getKey(item).toString())
                              && aE.init_Z_Item_Block(item, form.getBlock())
                              && this.init(form.getBlock().defaultBlockState())
                              && cu.init(mc, List.of(form.getBlock()), 8) <= 64.0
                        )) {
                     return aE.Inner_DataUtil.update;
                  }

                  if (!allowCombat
                     || this.init_List.contains(aE.Inner_DataUtil.close)
                     || this.load_List_3().isEmpty()
                     || this.write_Z_6()
                     || this.init(this.load_List_3(), 160.0) == null && !aE.init_Z(this.load_List_3())) {
                     for (aE.Inner_DataUtil strategy : aE.Inner_DataUtil.values()) {
                        if (!this.init_List.contains(strategy)
                           && (strategy != aE.Inner_DataUtil.close || allowCombat)
                           && (strategy != aE.Inner_DataUtil.update && strategy != aE.Inner_DataUtil.sync || allowMining)
                           && this.init_Z(strategy)) {
                           return strategy;
                        }
                     }

                     return null;
                  } else {
                     return aE.Inner_DataUtil.close;
                  }
               }

               boolean init_Z(aE.Inner_DataUtil strategy) {
                  boolean var10000;
                  switch (strategy) {
                     case init:
                        var10000 = false;
                        break;
                     case load:
                        var10000 = depth < 5 && this.init_Str_11() != null;
                        break;
                     case read:
                        var10000 = !this.init_Set_6().isEmpty();
                        break;
                     case write:
                        var10000 = this.load_Z_12();
                        break;
                     case update:
                        var10000 = !this.init_List_6().isEmpty() && !this.read_Z_8();
                        break;
                     case sync:
                        var10000 = this.load_Str_3() != null;
                        break;
                     case flush:
                        var10000 = bz.init_Z(target) && !aG.init_Z();
                        break;
                     case close:
                        var10000 = !this.load_List_3().isEmpty();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               }

               String init_Str_11() {
                  for (Block block : this.init_List_6()) {
                     BlockState state = block.defaultBlockState();
                     if (!this.init(state)) {
                        Item tool = af.init_Item(state);
                        if (tool != null) {
                           String toolId = BuiltInRegistries.ITEM.getKey(tool).toString();
                           if (!visiting.contains(toolId) && !this.init_af_DataUtil.items().contains(tool)) {
                              return toolId;
                           }
                        }
                     }
                  }

                  return null;
               }

               boolean init(BlockState state) {
                  return autismclient.ai.a.r.init_I_Minecraft_2(mc, state, true, (block, tool) -> af.init(mc, block, tool, this.init_af_DataUtil.items())) >= 0;
               }

               AiOperation init_AiOperation(aE.Inner_DataUtil strategy) {
                  int missing = goal - af.init_I(this.init_af_DataUtil);
                  int legSeconds = Math.max(15, this.init_I_19());
                  switch (strategy) {
                     case load:
                        String toolId = this.init_Str_11();
                        if (toolId == null) {
                           return null;
                        }

                        this.load_Str = "obtain " + toolId + " to harvest " + spec;
                        Set<String> nested = new LinkedHashSet<>(visiting);

                        for (Item item : this.init_af_DataUtil.items()) {
                           nested.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }

                        return aE.init(
                           ag.init_JsonObject_ObjArr(
                              "item",
                              toolId,
                              "count",
                              e.init_I(toolId) + 1,
                              "radius",
                              radius,
                              "allowCombat",
                              allowCombat,
                              "allowMining",
                              allowMining,
                              "timeoutSeconds",
                              legSeconds
                           ),
                           depth + 1,
                           nested
                        );
                     case read:
                        g.Inner_StreamUtil graph = e.init_g_StreamUtil();
                        Item output = aE.init(this.init_Set_6(), goal, graph, aE::init_D);
                        if (output == null) {
                           return null;
                        } else {
                           String outputId = BuiltInRegistries.ITEM.getKey(output).toString();
                           g.Inner_CachePool plan = g.init(graph, outputId, goal);
                           if (!plan.complete() && depth < 5) {
                              Entry<String, Integer> shortfall = this.init(plan);
                              if (shortfall != null) {
                                 String want = shortfall.getKey();
                                 if (want.endsWith("_log") && aE.init_D(want) == 1.7976931348623157E308 && !visiting.contains("log")) {
                                    want = "log";
                                 }

                                 this.load_Str = "obtain " + want + " for " + outputId;
                                 Set<String> nestedCraft = new LinkedHashSet<>(visiting);
                                 nestedCraft.add(outputId);
                                 int count = shortfall.getValue() + aE.init_I_Z_Str(aE.init(graph, outputId, plan), shortfall.getKey());
                                 return aE.init(
                                    ag.init_JsonObject_ObjArr(
                                       "item",
                                       want,
                                       "count",
                                       count,
                                       "additional",
                                       true,
                                       "radius",
                                       radius,
                                       "allowCombat",
                                       allowCombat,
                                       "allowMining",
                                       allowMining,
                                       "timeoutSeconds",
                                       legSeconds
                                    ),
                                    depth + 1,
                                    nestedCraft
                                 );
                              }
                           }

                           if (plan.steps().isEmpty()) {
                              return null;
                           }

                           this.load_Str = "craft " + outputId;
                           return e.init(ag.init_JsonObject_ObjArr("item", outputId, "count", goal, "timeoutSeconds", legSeconds));
                        }
                     case write:
                        Item outputx = this.load_Set_4()
                           .stream()
                           .filter(this::init_Z)
                           .max(
                              Comparator.comparingInt(
                                 item -> autismclient.util.AutoCraftingEngine.load(mc, item)
                                    .stream()
                                    .flatMap(p -> p.init_List().stream())
                                    .mapToInt(i -> S.load(BuiltInRegistries.ITEM.getKey(i).toString()))
                                    .max()
                                    .orElse(0)
                              )
                           )
                           .orElse(null);
                        if (outputx == null) {
                           return null;
                        } else {
                           String outputId = BuiltInRegistries.ITEM.getKey(outputx).toString();
                           Entry<String, Integer> inputNeed = this.init_Map_Entry(outputx);
                           Set<String> nestedSmelt = new LinkedHashSet<>(visiting);
                           nestedSmelt.add(outputId);
                           if (inputNeed != null && depth < 5) {
                              String inputId = inputNeed.getKey();
                              this.load_Str = "obtain " + inputId + " to smelt into " + outputId;
                              return aE.init(
                                 ag.init_JsonObject_ObjArr(
                                    "item",
                                    inputId,
                                    "count",
                                    inputNeed.getValue(),
                                    "additional",
                                    false,
                                    "radius",
                                    radius,
                                    "allowCombat",
                                    allowCombat,
                                    "allowMining",
                                    allowMining,
                                    "timeoutSeconds",
                                    legSeconds
                                 ),
                                 depth + 1,
                                 nestedSmelt
                              );
                           } else {
                              autismclient.util.AutoCraftingEngine.Inner_StreamUtil cooking = autismclient.util.AutoCraftingEngine.load(mc, outputx)
                                 .stream()
                                 .filter(p -> p.init_List().stream().anyMatch(i -> S.init_I_Str(BuiltInRegistries.ITEM.getKey(i).toString()) > 0))
                                 .min(Comparator.comparingInt(p -> ci.init_Z(mc, p.load_Str()) ? 0 : (S.init_I_Str(p.load_Str()) > 0 ? 1 : 2)))
                                 .orElse(null);
                              if (cooking != null && depth < 5) {
                                 if (!ci.init_Z(mc, cooking.load_Str()) && S.init_I_Str(cooking.load_Str()) == 0 && !visiting.contains(cooking.load_Str())) {
                                    this.load_Str = "obtain " + cooking.load_Str() + " for " + outputId;
                                    return aE.init(
                                       ag.init_JsonObject_ObjArr(
                                          "item",
                                          cooking.load_Str(),
                                          "count",
                                          1,
                                          "staged",
                                          true,
                                          "radius",
                                          radius,
                                          "allowMining",
                                          allowMining,
                                          "allowCombat",
                                          allowCombat,
                                          "timeoutSeconds",
                                          legSeconds
                                       ),
                                       depth + 1,
                                       nestedSmelt
                                    );
                                 }

                                 Item source = cooking.init_List()
                                    .stream()
                                    .filter(i -> S.init_I_Str(BuiltInRegistries.ITEM.getKey(i).toString()) > 0)
                                    .findFirst()
                                    .orElseThrow();
                                 Item fuel = ci.init("", source, missing);
                                 int burnDivisor = ci.init_I(cooking.load_Str());
                                 int burn = fuel == null ? 0 : af.init_I(new ItemStack(fuel)) / burnDivisor;
                                 int fuelCount = fuel == null ? 0 : ci.init(fuel);
                                 int furnaceCount = aK.init_I_I_3(missing);
                                 if (fuelCount < aK.init_I(missing, furnaceCount, cooking.load_I(), burn)) {
                                    ItemStack fuelStack = fuel == null ? ItemStack.EMPTY : new ItemStack(fuel);
                                    boolean coalNearby = aE.init_D("minecraft:coal") != 1.7976931348623157E308
                                       && af.init_Z(Blocks.COAL_ORE.defaultBlockState());
                                    String fuelId;
                                    int needed;
                                    if (fuel == null || ci.init_I(fuelStack) != 1 || fuel != Items.COAL && fuel != Items.CHARCOAL) {
                                       if (coalNearby && allowMining && !visiting.contains("minecraft:coal")) {
                                          fuelId = "minecraft:coal";
                                          needed = aK.init_I(missing, furnaceCount, cooking.load_I(), 1600 / burnDivisor) + bK.load(Items.COAL);
                                       } else if (af.init_I(af.load_af_DataUtil("log")) <= 0
                                          && af.init_I(af.load_af_DataUtil("planks")) <= 0
                                          && aE.init_D("minecraft:coal") != 1.7976931348623157E308) {
                                          fuelId = "minecraft:coal";
                                          needed = aK.init_I(missing, furnaceCount, cooking.load_I(), 1600 / burnDivisor);
                                       } else {
                                          fuelId = "planks";
                                          needed = aK.init_I(missing, furnaceCount, cooking.load_I(), 300 / burnDivisor) + autismclient.ai.a.UtilCore.init_I();
                                       }
                                    } else {
                                       fuelId = BuiltInRegistries.ITEM.getKey(fuel).toString();
                                       needed = aK.init_I(missing, furnaceCount, cooking.load_I(), burn) + bK.load(fuel);
                                    }

                                    if (!visiting.contains(fuelId)) {
                                       this.load_Str = "obtain fuel " + fuelId + " for " + outputId;
                                       return aE.init(
                                          ag.init_JsonObject_ObjArr(
                                             "item",
                                             fuelId,
                                             "count",
                                             needed,
                                             "radius",
                                             radius,
                                             "allowMining",
                                             allowMining,
                                             "allowCombat",
                                             allowCombat,
                                             "timeoutSeconds",
                                             legSeconds
                                          ),
                                          depth + 1,
                                          nestedSmelt
                                       );
                                    }
                                 }
                              }

                              this.load_Str = "smelt " + outputId;

                              try {
                                 int largestInput = autismclient.util.AutoCraftingEngine.load(mc, outputx)
                                    .stream()
                                    .flatMap(p -> p.init_List().stream())
                                    .mapToInt(i -> S.init_I_Str(BuiltInRegistries.ITEM.getKey(i).toString()))
                                    .max()
                                    .orElse(0);
                                 int batchGoal = Math.min(goal, af.init_I(this.init_af_DataUtil) + largestInput);
                                 return ci.load(ag.init_JsonObject_ObjArr("item", outputId, "count", batchGoal, "timeoutSeconds", legSeconds));
                              } catch (IllegalArgumentException e) {
                                 this.load_Str = "smelt " + outputId + " (" + e.getMessage() + ")";
                                 return null;
                              }
                           }
                        }
                     case update:
                        this.load_Str = "gather " + spec;
                        return this.init(legSeconds);
                     case sync:
                        String mineId = this.load_Str_3();
                        if (mineId == null) {
                           return null;
                        }

                        this.load_Str = "mine " + mineId + " below ground for " + spec;
                        return this.init(mineId, missing, legSeconds);
                     case flush:
                        this.load_Str = "prospect " + spec;
                        return bz.init(ag.init_JsonObject_ObjArr("item", spec, "count", goal, "timeoutSeconds", legSeconds));
                     case close:
                        List<EntityType<?>> mobs = this.load_List_3();
                        if (mobs.isEmpty()) {
                           return null;
                        }

                        List<String> names = new ArrayList<>();

                        for (EntityType<?> type : mobs) {
                           names.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
                        }

                        this.load_Str = "hunt " + String.join(",", names);
                        JsonObject fightArgs = ag.init_JsonObject_ObjArr(
                           "target", String.join(",", names), "count", Math.max(1, Math.min(32, missing)), "radius", radius, "timeoutSeconds", legSeconds
                        );
                        String colors = aE.init(this.init_af_DataUtil.items());
                        if (!colors.isEmpty()) {
                           fightArgs.addProperty("sheepColors", colors);
                        }

                        if (aE.init_Z(mobs)) {
                           fightArgs.addProperty("retreatHealth", 0);
                        }

                        if (aE.init_Z(mobs)) {
                           fightArgs.addProperty("radius", Math.max(radius, 64));
                        }

                        return this.init(mobs, fightArgs, names, legSeconds);
                     default:
                        return null;
                  }
               }

               AiOperation init(List<EntityType<?>> mobs, JsonObject fightArgs, List<String> names, int legSeconds) {
                  if (this.init(mobs, 160.0) == null && aE.init_Z(mobs)) {
                     this.load_Str = "look for " + String.join(",", names);
                     List<Item> items = List.copyOf(this.init_af_DataUtil.items());
                     return aB.load(
                        aE.init(e -> mobs.contains(e.getType()) && aE.init_Z_Entity_Collection(e, items), String.join(",", names), legSeconds),
                        () -> this.init(mobs, fightArgs, this.init_I_19())
                     );
                  } else {
                     return this.init(mobs, fightArgs, legSeconds);
                  }
               }

               boolean init_Z_18() {
                  return af.init_I(af.load_af_DataUtil("log")) > 0 || af.init_I(af.load_af_DataUtil("planks")) >= 2 || aE.init_D("log") <= 1024.0;
               }

               AiOperation init(List<EntityType<?>> mobs, JsonObject fightArgs, int legSeconds) {
                  Entity far = this.init(mobs, 160.0);
                  if (far != null && mc.player.distanceTo(far) > 40.0) {
                     this.load_Str = "walk to the " + BuiltInRegistries.ENTITY_TYPE.getKey(far.getType()).getPath() + " and hunt";
                     AiOperation walk = HashUtil.init_AiOperation_BlockPos(far.blockPosition(), 8.0, true, true, true, legSeconds);
                     return this.init(aB.init_AiOperation_AiOperation_Supplier(walk, () -> this.init(mobs, fightArgs)));
                  } else {
                     return this.init(this.init(mobs, fightArgs));
                  }
               }

               AiOperation init(List<EntityType<?>> mobs, JsonObject fightArgs) {
                  if (aE.init_Z(mobs) && af.init(36) < 0 && depth < 5 && !visiting.contains("minecraft:wooden_sword") && this.init_Z_18()) {
                     this.load_Str = "a wooden sword before the hunt";
                     Set<String> nested = new LinkedHashSet<>(visiting);
                     nested.add("minecraft:wooden_sword");
                     AiOperation sword = aE.init(
                        ag.init_JsonObject_ObjArr(
                           "item",
                           "minecraft:wooden_sword",
                           "count",
                           1,
                           "radius",
                           radius,
                           "allowCombat",
                           allowCombat,
                           "allowMining",
                           allowMining,
                           "timeoutSeconds",
                           Math.min(120, this.init_I_19())
                        ),
                        depth + 1,
                        nested
                     );
                     return aB.init_AiOperation_AiOperation_Supplier(sword, () -> ValueSink.init(fightArgs));
                  } else {
                     return ValueSink.init(fightArgs);
                  }
               }

               Entry<String, Integer> init(g.Inner_CachePool plan) {
                  return aE.init(plan.missing(), visiting, this.init_Set);
               }

               Entity init(List<EntityType<?>> types, double range) {
                  Entity best = null;
                  double bestSq = range * range;

                  for (Entity e : mc.level.entitiesForRendering()) {
                     if (e instanceof LivingEntity living
                        && living.isAlive()
                        && types.contains(e.getType())
                        && aE.init_Z_Entity_Collection(e, this.init_af_DataUtil.items())) {
                        double d = mc.player.distanceToSqr(e);
                        if (d < bestSq) {
                           bestSq = d;
                           best = e;
                        }
                     }
                  }

                  return best;
               }

               AiOperation init(final AiOperation fight) {
                  return new AiOperation() {
                     final List<Item> init_List = new ArrayList<>(init_af_DataUtil.items());
                     AiOperation init_AiOperation = fight;
                     AiOperation.Result init_AiOperation_Result;
                     int init_I = -1;
                     boolean init_Z;

                     @Override
                     public AiOperation.Result tick() {
                        if (!this.init_Z && mc.player != null && mc.level != null) {
                           if (this.init_AiOperation != null) {
                              AiOperation.Result result = this.init_AiOperation.tick();
                              if (!result.terminal()) {
                                 return result;
                              }

                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                              if (this.init_I < 0) {
                                 this.init_AiOperation_Result = result;
                                 if (result.status() == AiOperation.Status.CANCELLED) {
                                    return result;
                                 }

                                 this.init_I = 0;
                              } else if (result.status() == AiOperation.Status.CANCELLED) {
                                 return result;
                              }
                           }

                           if (af.init_I(init_af_DataUtil) >= goal) {
                              return R.init_AiOperation_Result(
                                 AiOperation.Status.SUCCEEDED, "Hunt and collect brought " + spec + " to target", new JsonObject()
                              );
                           }

                           int missing = goal - af.init_I(init_af_DataUtil);
                           if (this.init_I == 0 && !this.init_List.isEmpty()) {
                              this.init_I++;
                              List<String> ids = new ArrayList<>();

                              for (Item variant : this.init_List) {
                                 ids.add(BuiltInRegistries.ITEM.getKey(variant).toString());
                              }

                              String dropIds = String.join(",", ids);
                              this.init_AiOperation = T.write(
                                 ag.init_JsonObject_ObjArr(
                                    "item", dropIds, "count", missing, "additional", true, "timeoutSeconds", Math.max(1, Math.min(60, init_I_19()))
                                 )
                              );
                              return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Collecting the dropped " + spec, new JsonObject());
                           } else {
                              return R.init_AiOperation_Result(this.init_AiOperation_Result.status(), this.init_AiOperation_Result.message(), new JsonObject());
                           }
                        } else {
                           return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Hunt cancelled", new JsonObject());
                        }
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        this.init_Z = true;
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                        }
                     }
                  };
               }

               AiOperation init(AiOperation.Result result) {
                  if (this.init_aE_DataUtil == aE.Inner_DataUtil.write
                     && !this.load_Z
                     && depth < 5
                     && result.dataJson().contains("\"minecraft:furnace\"")
                     && !visiting.contains("minecraft:furnace")
                     && !this.init_Set.contains("minecraft:furnace")) {
                     this.load_Z = true;
                     this.load_Str = "obtain minecraft:furnace to smelt";
                     Set<String> nested = new LinkedHashSet<>(visiting);
                     nested.addAll(this.init_Set);
                     return aE.init(
                        ag.init_JsonObject_ObjArr(
                           "item",
                           "minecraft:furnace",
                           "count",
                           1,
                           "radius",
                           radius,
                           "allowCombat",
                           allowCombat,
                           "allowMining",
                           allowMining,
                           "staged",
                           true,
                           "timeoutSeconds",
                           Math.max(15, this.init_I_19())
                        ),
                        depth + 1,
                        nested
                     );
                  }

                  if (this.init_aE_DataUtil != aE.Inner_DataUtil.read || this.init_Z || depth >= 5) {
                     return null;
                  }

                  if (!aE.load(result.dataJson())) {
                     return null;
                  }

                  if (!visiting.contains("minecraft:crafting_table") && !this.init_Set.contains("minecraft:crafting_table")) {
                     this.init_Z = true;
                     if (af.init_I(af.load_af_DataUtil("planks")) < 4 && af.init_I(af.load_af_DataUtil("log")) <= 0) {
                        this.load_Str = "obtain minecraft:crafting_table for the 3x3 grid";
                        Set<String> nested = new LinkedHashSet<>(visiting);
                        nested.addAll(this.init_Set);
                        return aE.init(
                           ag.init_JsonObject_ObjArr(
                              "item",
                              "minecraft:crafting_table",
                              "count",
                              1,
                              "radius",
                              radius,
                              "allowCombat",
                              allowCombat,
                              "allowMining",
                              allowMining,
                              "timeoutSeconds",
                              Math.max(15, this.init_I_19())
                           ),
                           depth + 1,
                           nested
                        );
                     } else {
                        this.load_Str = "craft a crafting table for the 3x3 grid";
                        return e.init(
                           ag.init_JsonObject_ObjArr(
                              "item",
                              "minecraft:crafting_table",
                              "count",
                              e.init_I("minecraft:crafting_table") + 1,
                              "timeoutSeconds",
                              Math.max(15, Math.min(30, this.init_I_19()))
                           )
                        );
                     }
                  } else {
                     return null;
                  }
               }

               String load_Str_3() {
                  List<String> ids = new ArrayList<>(this.init_List_6().size());

                  for (Block block : this.init_List_6()) {
                     ids.add(BuiltInRegistries.BLOCK.getKey(block).toString());
                  }

                  return aE.init_Str(ids);
               }

               AiOperation init(int timeoutSecondsx) {
                  List<String> blocks = new ArrayList<>();

                  for (Block block : this.init_List_6()) {
                     if (this.init(block.defaultBlockState())) {
                        blocks.add(BuiltInRegistries.BLOCK.getKey(block).toString());
                     }
                  }

                  if (blocks.isEmpty()) {
                     return null;
                  }

                  boolean trees = blocks.stream().anyMatch(id -> id.endsWith("_log"));
                  return autismclient.ai.a.BufferHelper.init_AiOperation(
                     ag.init_JsonObject_ObjArr(
                        "blocks",
                        autismclient.ai.a.BufferHelper.init(blocks),
                        "drops",
                        autismclient.ai.a.BufferHelper.init(this.init_af_DataUtil.itemIds()),
                        "held",
                        goal,
                        "trees",
                        trees,
                        "radius",
                        Math.max(48, radius),
                        "timeoutSeconds",
                        timeoutSeconds
                     )
                  );
               }

               AiOperation init(final String blockId, final int count, final int timeoutSecondsx) {
                  return !aH.init_Z
                     ? this.init(timeoutSeconds)
                     : new AiOperation() {
                        final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
                        final BlockPos init_BlockPos = mc.player.blockPosition();
                        final int init_I = this.init_BlockPos.getY();
                        final Block init_Block = e.init_Block(blockId);
                        AiOperation init_AiOperation;
                        boolean init_Z;
                        int load_I;
                        boolean load_Z;

                        @Override
                        public AiOperation.Result tick() {
                           if (!this.load_Z && mc.player != null && mc.level != null) {
                              if (System.nanoTime() > this.init_J) {
                                 return R.init_AiOperation_Result(AiOperation.Status.TIMED_OUT, "Mine deadline reached", new JsonObject());
                              }

                              if (af.init_I(init_af_DataUtil) >= goal) {
                                 return R.init_AiOperation_Result(AiOperation.Status.SUCCEEDED, "Mined " + blockId + " to target", new JsonObject());
                              }

                              if (this.init_AiOperation != null) {
                                 AiOperation.Result result = this.init_AiOperation.tick();
                                 if (!result.terminal()) {
                                    return result;
                                 }

                                 this.init_AiOperation.cancel();
                                 this.init_AiOperation = null;
                                 if (result.status() == AiOperation.Status.CANCELLED) {
                                    return result;
                                 }

                                 if (this.init_Z) {
                                    return result;
                                 }

                                 if (result.status() != AiOperation.Status.SUCCEEDED && ++this.load_I >= 2) {
                                    this.init_Z = true;
                                 }
                              }

                              int currentY = mc.player.blockPosition().getY();
                              boolean found = this.init_Z || this.init();
                              Integer nextY = aE.init(this.init_I, currentY, mc.level.getMinY(), found);
                              if (found) {
                                 this.init_Z = true;
                                 this.init_AiOperation = autismclient.ai.a.TokenStore.init(
                                    ag.init_JsonObject_ObjArr("block", blockId, "count", count, "radius", radius, "timeoutSeconds", init_I_19())
                                 );
                                 return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Mining " + blockId, new JsonObject());
                              }

                              if (nextY == null) {
                                 return R.init_AiOperation_Result(
                                    AiOperation.Status.FAILED,
                                    "No " + blockId + " in the scan band after descending " + (this.init_I - currentY) + " to Y=" + currentY,
                                    new JsonObject()
                                 );
                              }

                              this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(
                                 new BlockPos(this.init_BlockPos.getX(), nextY, this.init_BlockPos.getZ()), 2.5, true, true, true, aE.init(init_I_19())
                              );
                              return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Descending to Y=" + nextY + " toward " + blockId, new JsonObject());
                           } else {
                              return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Mine cancelled", new JsonObject());
                           }
                        }

                        boolean init() {
                           return cu.init(mc, List.of(this.init_Block), Math.min(radius, 96)) != 1.7976931348623157E308;
                        }

                        @Override
                        public boolean canInterruptNow() {
                           return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                        }

                        @Override
                        public boolean requiresReconciliationAfterEmergency() {
                           return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
                        }

                        @Override
                        public void cancel() {
                           this.load_Z = true;
                           if (this.init_AiOperation != null) {
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                           }
                        }
                     };
               }

               Set<Item> init_Set_6() {
                  if (this.load_Set == null) {
                     this.load_Set = this.init(true);
                  }

                  return this.load_Set;
               }

               Set<Item> load_Set_4() {
                  if (this.read_Set == null) {
                     this.read_Set = this.init(false);
                  }

                  return this.read_Set;
               }

               Set<Item> init(boolean crafting) {
                  Set<Item> out = new LinkedHashSet<>();

                  for (Item item : this.init_af_DataUtil.items()) {
                     if (!visiting.contains(BuiltInRegistries.ITEM.getKey(item).toString())) {
                        boolean known = crafting ? !autismclient.util.AutoCraftingEngine.init(mc, item).isEmpty() : !autismclient.util.AutoCraftingEngine.load(mc, item).isEmpty();
                        if (known) {
                           out.add(item);
                        }
                     }
                  }

                  return out;
               }

               boolean init_Z_4(String itemId) {
                  return itemId.startsWith("minecraft:raw_") || !itemId.endsWith("_ore");
               }

               boolean load_Z_12() {
                  return this.load_Set_4().stream().anyMatch(this::init_Z);
               }

               boolean init_Z(Item output) {
                  Entry<String, Integer> need = this.init_Map_Entry(output);
                  return need == null || this.init_Z_4(need.getKey());
               }

               Entry<String, Integer> init_Map_Entry(Item output) {
                  String best = null;
                  int bestCount = -1;

                  for (autismclient.util.AutoCraftingEngine.Inner_StreamUtil plan : autismclient.util.AutoCraftingEngine.load(mc, output)) {
                     if (plan.init_List().stream().mapToInt(i -> S.init_I_Str(BuiltInRegistries.ITEM.getKey(i).toString())).sum()
                        >= goal - af.init_I(this.init_af_DataUtil)) {
                        return null;
                     }

                     for (Item input : plan.init_List()) {
                        String inputId = BuiltInRegistries.ITEM.getKey(input).toString();
                        int count = S.init_I_Str(inputId);
                        if (count >= goal - af.init_I(this.init_af_DataUtil)) {
                           return null;
                        }

                        if (!visiting.contains(inputId)
                           && (count > bestCount || count == bestCount && best != null && !this.init_Z_4(best) && this.init_Z_4(inputId))) {
                           bestCount = count;
                           best = inputId;
                        }
                     }
                  }

                  return best == null ? null : Map.entry(best, goal - af.init_I(this.init_af_DataUtil));
               }

               boolean read_Z_8() {
                  if (!aH.init_Z) {
                     return false;
                  }

                  if (this.write_Z || this.init_List.contains(aE.Inner_DataUtil.sync) || this.load_Str_3() == null) {
                     return false;
                  }

                  if (!aE.init(false, false, true, this.write_Z_6())) {
                     return false;
                  }

                  this.write_Z = true;
                  this.init_JsonArray
                     .add(
                        ag.init_JsonObject_ObjArr(
                           "strategy",
                           "gather",
                           "status",
                           "SKIPPED",
                           "detail",
                           "gather " + spec,
                           "message",
                           "no source blocks within 32 in loaded terrain; trying buried mining first"
                        )
                     );
                  return true;
               }

               boolean write_Z_6() {
                  if (this.init_Boolean == null) {
                     this.init_Boolean = cu.init(mc, this.init_List_6(), 32) != 1.7976931348623157E308;
                  }

                  return this.init_Boolean;
               }

               String read_Str_3() {
                  return target.items().size() != 1
                     ? null
                     : aE.init_Str_Str_Function(BuiltInRegistries.ITEM.getKey(target.items().iterator().next()).toString(), aE::init_Set);
               }

               String write_Str_2() {
                  if (this.write_Str == null) {
                     return spec;
                  }

                  String bestId = spec;
                  int bestCount = -1;

                  for (Item item : this.init_af_DataUtil.items()) {
                     String id = BuiltInRegistries.ITEM.getKey(item).toString();
                     int count = S.init_I_Str(id);
                     if (count > bestCount) {
                        bestCount = count;
                        bestId = id;
                     }
                  }

                  return aE.init(spec, this.write_Str, bestId);
               }

               List<Block> init_List_6() {
                  if (this.load_List != null) {
                     return this.load_List;
                  }

                  List<Block> blocks = new ArrayList<>(this.init_af_DataUtil.blocks());

                  for (Item item : this.init_af_DataUtil.items()) {
                     for (Block block : af.init(item)) {
                        if (!blocks.contains(block)) {
                           blocks.add(block);
                        }
                     }
                  }

                  blocks.removeIf(
                     blockx -> this.init_af_DataUtil
                        .items()
                        .stream()
                        .noneMatch(itemx -> af.init(itemx).contains(blockx) && aE.init_Z_Item_Block(itemx, blockx))
                  );
                  blocks.removeIf(aE::init);
                  this.load_List = List.copyOf(blocks);
                  return this.load_List;
               }

               List<EntityType<?>> load_List_3() {
                  if (this.read_List != null) {
                     return this.read_List;
                  }

                  List<EntityType<?>> mobs = new ArrayList<>();

                  for (Item item : this.init_af_DataUtil.items()) {
                     for (EntityType<?> type : af.load(item)) {
                        if (!mobs.contains(type)) {
                           mobs.add(type);
                        }
                     }
                  }

                  this.read_List = List.copyOf(mobs);
                  return this.read_List;
               }

               int init_I_19() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_32() {
                  JsonObject out = ag.init_JsonObject_ObjArr(
                     "item", spec, "goal", goal, "held", af.init_I(this.init_af_DataUtil), "startedWith", held, "depth", depth
                  );
                  out.add("matches", ag.init.toJsonTree(this.init_af_DataUtil.itemIds()));
                  if (this.write_Str != null) {
                     out.addProperty("familyFallbackFrom", spec);
                  }

                  out.add("attempts", this.init_JsonArray);
                  if (this.init_aE_DataUtil != null) {
                     out.addProperty("strategy", this.init_aE_DataUtil.name().toLowerCase(Locale.ROOT));
                     out.addProperty("strategyDetail", this.load_Str);
                  }

                  JsonArray spent = new JsonArray();

                  for (aE.Inner_DataUtil strategy : this.init_List) {
                     spent.add(strategy.name().toLowerCase(Locale.ROOT));
                  }

                  out.add("exhausted", spent);
                  out.add("survival", X.init_JsonObject_X_BufferHelper(X.init_X_BufferHelper(), this.init_I, this.init_Str));
                  return out;
               }

               AiOperation.Result init_AiOperation_Result_9(String message) {
                  if (this.load_af_DataUtil != this.init_af_DataUtil) {
                     this.load_af_DataUtil = this.init_af_DataUtil;
                     String label = this.init_af_DataUtil.items().size() == 1
                        ? this.init_af_DataUtil.items().iterator().next().getDefaultInstance().getHoverName().getString()
                        : this.write_Str_2().replace("minecraft:", "").replace('_', ' ');
                     this.read_Str = "Getting " + label;
                  }

                  cG.init(this, this.read_Str, cs.init(message, "Preparing materials"), af.init_I(this.init_af_DataUtil), goal);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_32().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  JsonObject data = this.init_JsonObject_32();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               @Override
               public boolean canInterruptNow() {
                  return (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency();
               }

               @Override
               public void cancel() {
                  if (!this.sync) {
                     this.sync = true;
                     cG.init_V(this);
                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }
                  }
               }
            };
         } else {
            throw new IllegalArgumentException("count must be between 1 and 2304");
         }
      } else {
         throw new IllegalArgumentException("Obtaining items requires a connected living player");
      }
   }

   static boolean init(Block block) {
      BlockState state = block.defaultBlockState();
      String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
      return !path.endsWith("_wool") && (!path.endsWith("_carpet") || path.equals("moss_carpet") || path.equals("pale_moss_carpet")) && !path.endsWith("_bed")
         ? state.is(BlockTags.PLANKS)
            || state.is(BlockTags.WOODEN_SLABS)
            || state.is(BlockTags.WOODEN_STAIRS)
            || state.is(BlockTags.WOODEN_FENCES)
            || state.is(BlockTags.WOODEN_DOORS)
            || state.is(BlockTags.WOODEN_TRAPDOORS)
         : true;
   }

   static boolean init_Z_Item_Block(Item item, Block block) {
      if (item instanceof BlockItem form && form.getBlock() == block && !init().contains(BuiltInRegistries.ITEM.getKey(item).toString())) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.player != null) {
            for (int slot = 0; slot < 36; slot++) {
               ItemStack stack = mc.player.getInventory().getItem(slot);
               if (stack.isCorrectToolForDrops(block.defaultBlockState()) && af.init(stack, 2) && BlockBreakSpeedHelper.init_Z(mc, stack)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   static Item init(Set<Item> craftable, int goal, g.Inner_StreamUtil graph, ToDoubleFunction<String> missingSourceDistance) {
      Item best = null;
      String bestId = null;
      int bestMissing = 2147483647;
      int bestHeldInputs = -1;
      double bestDistance = -1.0;
      g.Inner_CachePool bestPlan = null;

      for (Item item : craftable) {
         if (item != null) {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            g.Inner_CachePool plan = g.init(graph, id, goal);
            int missing = 0;

            for (int amount : plan.missing().values()) {
               missing += amount;
            }

            int heldInputs = init(plan, id, graph);
            double distance = -1.0;
            boolean replace;
            if (best == null) {
               replace = true;
            } else if (missing != bestMissing) {
               replace = missing < bestMissing;
            } else if (heldInputs != bestHeldInputs) {
               replace = heldInputs > bestHeldInputs;
            } else {
               distance = init_D_g_CachePool_ToDoubleFunction(plan, missingSourceDistance);
               if (bestDistance < 0.0) {
                  bestDistance = init_D_g_CachePool_ToDoubleFunction(bestPlan, missingSourceDistance);
               }

               replace = distance < bestDistance || distance == bestDistance && (init_Z(id) != init_Z(bestId) ? init_Z(id) : id.compareTo(bestId) < 0);
            }

            if (replace) {
               best = item;
               bestId = id;
               bestMissing = missing;
               bestHeldInputs = heldInputs;
               bestPlan = plan;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   static boolean init_Z(String id) {
      String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
      return path.startsWith("white_") || path.startsWith("oak_");
   }

   static String init(Collection<Item> items) {
      List<String> colors = new ArrayList<>();

      for (Item item : items) {
         String path = BuiltInRegistries.ITEM.getKey(item).getPath();
         if (path.endsWith("_wool")) {
            colors.add(path.substring(0, path.length() - "_wool".length()));
         }
      }

      return String.join(",", colors);
   }

   static boolean init_Z_Entity_Collection(Entity entity, Collection<Item> items) {
      if (!(entity instanceof Sheep sheep)) {
         return true;
      } else {
         String colors = init(items);
         return colors.isEmpty() ? true : !sheep.isSheared() && List.of(colors.split(",")).contains(sheep.getColor().getSerializedName());
      }
   }

   static boolean init_Z(List<EntityType<?>> mobs) {
      if (mobs.isEmpty()) {
         return false;
      }

      for (EntityType<?> type : mobs) {
         if (type.getCategory() != MobCategory.CREATURE) {
            return false;
         }
      }

      return true;
   }

   static AiOperation init(final Predicate<Entity> wanted, final String what, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final long init_J = System.nanoTime() + Math.max(15, seconds) * 1000000000L;
         final BlockPos init_BlockPos = mc.player.blockPosition();
         final Object init_Obj = mc.level;
         AiOperation init_AiOperation;
         int init_I;
         int load;
         boolean init_Z;

         boolean init() {
            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof LivingEntity living && living.isAlive() && wanted.test(e) && mc.player.distanceToSqr(e) <= 25600.0) {
                  return true;
               }
            }

            return false;
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return R.init_AiOperation_Result(status, message, ag.init_JsonObject_ObjArr("looking", what, "legs", this.init_I));
         }

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z || mc.player == null || mc.level != this.init_Obj) {
               return this.init(AiOperation.Status.CANCELLED, "Search cancelled or world changed");
            }

            if (!mc.player.isAlive()) {
               return this.init(AiOperation.Status.FAILED, "Player died while looking for " + what);
            }

            if (this.init_AiOperation != null) {
               if (this.init_AiOperation.canInterruptNow() && !this.init_AiOperation.requiresReconciliationAfterEmergency() && this.init()) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  return this.init(AiOperation.Status.SUCCEEDED, "Found " + what);
               }

               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init(AiOperation.Status.RUNNING, "Looking for " + what + ": " + r.message());
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            if (this.init()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Found " + what);
            }

            long left = (this.init_J - System.nanoTime()) / 1000000000L;
            if (left <= 0L || this.init_I >= 8) {
               return this.init(AiOperation.Status.FAILED, "No " + what + " found in " + this.init_I + " legs");
            }

            if (!cl.init_Z_Minecraft_I(mc, 5) && this.load < 2) {
               this.load++;
               this.init_AiOperation = autismclient.ai.a.d.init(ag.init_JsonObject_ObjArr("timeoutSeconds", (int)Math.min(120L, left)));
               return this.init(AiOperation.Status.RUNNING, "Up to open sky to look for " + what);
            }

            double a = this.init_I++ * 2.399;
            int x = this.init_BlockPos.getX() + (int)Math.round(Math.cos(a) * 64.0);
            int z = this.init_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 64.0);
            BlockPos surface = cl.init_BlockPos_BlockPos(mc.player.blockPosition(), x, z, column -> cl.init_BlockPos_Minecraft_BlockPos(mc, column));
            if (surface == null) {
               return this.init(AiOperation.Status.RUNNING, "No safe ground that way; trying another bearing");
            }

            this.init_AiOperation = HashUtil.init(
               surface,
               2.0,
               false,
               false,
               false,
               (int)Math.min(60L, left),
               autismclient.ai.a.ClassCache.Inner_IoWorker.load,
               true,
               2.0,
               cl.init_ClassCache_JsonUti_BlockPos(surface),
               HashUtil.Inner_BufferHelper.DEFAULT
            );
            return this.init(AiOperation.Status.RUNNING, "Looking for " + what);
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }
         }
      };
   }

   private static double init_D_g_CachePool_ToDoubleFunction(g.Inner_CachePool plan, ToDoubleFunction<String> missingSourceDistance) {
      double best = 1.7976931348623157E308;

      for (String missingId : plan.missing().keySet()) {
         best = Math.min(best, missingSourceDistance.applyAsDouble(missingId));
      }

      return best;
   }

   static double init_D(String itemId) {
      Minecraft mc = Minecraft.getInstance();
      af.Inner_DataUtil target = af.load_af_DataUtil(itemId);
      List<Block> sources = new ArrayList<>(target.blocks());

      for (Item item : target.items()) {
         for (Block block : af.init(item)) {
            if (!sources.contains(block)) {
               sources.add(block);
            }
         }
      }

      sources.removeIf(aE::init);
      double best = sources.isEmpty() ? 1.7976931348623157E308 : cu.init(mc, sources, 32);
      List<EntityType<?>> mobs = new ArrayList<>();

      for (Item item : target.items()) {
         for (EntityType<?> type : af.load(item)) {
            if (!mobs.contains(type)) {
               mobs.add(type);
            }
         }
      }

      if (!mobs.isEmpty() && mc.player != null && mc.level != null) {
         for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof LivingEntity living && living.isAlive() && mobs.contains(e.getType()) && init_Z_Entity_Collection(e, target.items())) {
               double d = mc.player.distanceToSqr(e);
               if (d <= 25600.0) {
                  best = Math.min(best, d);
               }
            }
         }
      }

      return best;
   }

   private static int init(g.Inner_CachePool plan, String outputId, g.Inner_StreamUtil graph) {
      int held = 0;

      for (g.Inner_TaskQueue step : plan.steps()) {
         if (step.recipe().output().equals(outputId)) {
            for (List<String> slot : step.recipe().inputs()) {
               int slotHeld = 0;

               for (String accepted : slot) {
                  slotHeld = Math.max(slotHeld, Math.max(0, graph.init_I(accepted)));
               }

               held += Math.min(slotHeld, step.crafts());
            }
         }
      }

      return held;
   }

   static Entry<String, Integer> init(Map<String, Integer> missing, Set<String> visiting, Set<String> ownItemIds) {
      for (Entry<String, Integer> entry : missing.entrySet()) {
         if (!visiting.contains(entry.getKey()) && !ownItemIds.contains(entry.getKey())) {
            return entry;
         }
      }

      return null;
   }

   static String init(boolean craftKnown, boolean smeltKnown, boolean hasSourceBlocks, boolean prospectable, boolean hasMobDrops) {
      List<String> reasons = new ArrayList<>();
      if (!craftKnown) {
         reasons.add("no known recipe");
      }

      if (!smeltKnown) {
         reasons.add("no known smelting recipe");
      }

      if (!hasSourceBlocks) {
         reasons.add("no source blocks");
      }

      if (!prospectable) {
         reasons.add("not prospectable");
      }

      if (!hasMobDrops) {
         reasons.add("no mob drops");
      }

      return "no strategy applies (" + String.join("; ", reasons) + ")";
   }

   static String init_Str(List<String> sourceBlockIds) {
      for (String id : sourceBlockIds) {
         if (init_Set.contains(id)) {
            return id;
         }
      }

      return null;
   }

   static Integer init(int startY, int currentY, int minY, boolean sourceInBand) {
      if (sourceInBand) {
         return null;
      }

      if (startY - currentY >= 32) {
         return null;
      }

      int next = Math.max(minY + 4, Math.max(startY - 32, currentY - 8));
      return next < currentY ? next : null;
   }

   static int init(int remainingSeconds) {
      return Math.max(10, Math.min(45, remainingSeconds / 4));
   }

   static String init_Str(String dataJson) {
      if (dataJson != null && !dataJson.isBlank()) {
         try {
            JsonObject data = ag.init_JsonObject_Str(dataJson);
            if (data.has("missing") && data.get("missing").isJsonObject()) {
               for (Entry<String, JsonElement> entry : data.getAsJsonObject("missing").entrySet()) {
                  if (read(entry.getKey())) {
                     return entry.getKey();
                  }
               }

               return null;
            } else {
               return null;
            }
         } catch (RuntimeException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean read(String id) {
      return id != null && (id.endsWith("_pickaxe") || id.endsWith("_shovel") || id.endsWith("_axe") || id.endsWith("_hoe") || id.endsWith("shears"));
   }

   static int init_I_Z_Str(boolean needsTable, String item) {
      if (needsTable && !e.load() && e.init_I("minecraft:crafting_table") <= 0) {
         String path = item.substring(item.indexOf(58) + 1);
         if (!path.endsWith("_planks") && !path.equals("planks")) {
            return !path.endsWith("_log") && !path.endsWith("_wood") && !path.endsWith("_stem") && !path.endsWith("_hyphae") && !path.equals("log") ? 0 : 1;
         } else {
            return 4;
         }
      } else {
         return 0;
      }
   }

   static boolean init(g.Inner_StreamUtil graph, String outputId, g.Inner_CachePool plan) {
      if (plan.requiresTable()) {
         return true;
      }

      List<g.Inner_BufferHelper> recipes = graph.init_List(outputId);
      return !recipes.isEmpty() && recipes.get(0).requiresTable();
   }

   static boolean load(String dataJson) {
      if (dataJson != null && !dataJson.isBlank()) {
         try {
            JsonObject data = ag.init_JsonObject_Str(dataJson);
            return data.has("missing") && data.get("missing").isJsonObject() && data.getAsJsonObject("missing").has("minecraft:crafting_table");
         } catch (RuntimeException e) {
            return false;
         }
      } else {
         return false;
      }
   }

   static boolean init(boolean alreadyDeferred, boolean mineTried, boolean hasMineRoute, boolean sourceNearby) {
      return !alreadyDeferred && !mineTried && hasMineRoute && !sourceNearby;
   }

   static int init(String itemId, Set<String> directlyMineable, boolean craftable, boolean smeltable) {
      if (directlyMineable.contains(itemId)) {
         return 0;
      } else {
         return !craftable && !smeltable ? 2 : 1;
      }
   }

   static Set<String> init() {
      return af.init_Set();
   }

   static g.Inner_StreamUtil init(final g.Inner_StreamUtil inner) {
      final Minecraft mc = Minecraft.getInstance();
      final Set<String> mineable = init();
      final Map<String, Double> distances = new HashMap<>();
      return new g.Inner_StreamUtil() {
         @Override
         public List<g.Inner_BufferHelper> init_List(String itemId) {
            return inner.init_List(itemId);
         }

         @Override
         public int init_I(String itemId) {
            return inner.init_I(itemId);
         }

         @Override
         public int load_I(String itemId) {
            boolean craftable = !inner.init_List(itemId).isEmpty();
            boolean smeltable = BuiltInRegistries.ITEM
               .getOptional(Identifier.parse(itemId))
               .map(item -> !autismclient.util.AutoCraftingEngine.load(mc, item).isEmpty())
               .orElse(false);
            return aE.init(itemId, mineable, craftable, smeltable);
         }

         @Override
         public String init(Set<String> variantIds) {
            return aE.init_Str_Collection_Function(variantIds, aE::init_Set);
         }

         @Override
         public boolean init_Z(String itemId) {
            return BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId)).map(item -> aE.init_Z(af.load(item))).orElse(false);
         }

         @Override
         public double init_D(String itemId) {
            return mc != null && mc.player != null && mc.level != null ? distances.computeIfAbsent(itemId, id -> {
               double distance = aE.init_D(id);
               return distance == 1.7976931348623157E308 ? 256.0 : 1.0 + Math.sqrt(distance);
            }) : inner.init_D(itemId);
         }
      };
   }

   static String init_Str_Collection_Function(Collection<String> variantIds, Function<String, Set<String>> resolver) {
      if (variantIds != null && variantIds.size() >= 2) {
         Set<String> wanted = new HashSet<>(variantIds);
         Set<String> candidates = new LinkedHashSet<>();

         for (String id : variantIds) {
            String path = id.contains(":") ? id.substring(id.indexOf(58) + 1) : id;
            candidates.add(path);
            int underscore = path.indexOf(95);
            if (underscore >= 0 && underscore + 1 < path.length()) {
               candidates.add(path.substring(underscore + 1));
            }
         }

         List<String> ordered = new ArrayList<>(candidates);
         ordered.sort(String.CASE_INSENSITIVE_ORDER);

         for (String candidate : ordered) {
            Set<String> resolved = resolver.apply(candidate);
            if (resolved != null && resolved.equals(wanted)) {
               return candidate;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static Set<String> init_Set(String spec) {
      try {
         Set<String> ids = new HashSet<>();

         for (Item item : af.init_af_DataUtil(spec).items()) {
            ids.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }

         return ids.isEmpty() ? null : ids;
      } catch (RuntimeException e) {
         return null;
      }
   }

   static String init_Str_Str_Function(String itemId, Function<String, Set<String>> resolver) {
      if (itemId != null && resolver != null) {
         String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(58) + 1) : itemId;
         List<String> candidates = new ArrayList<>();
         if (path.endsWith("_ore")) {
            candidates.add(path.substring(0, path.length() - 4));
         }

         int underscore = path.indexOf(95);
         if (underscore >= 0 && underscore + 1 < path.length()) {
            String tail = path.substring(underscore + 1);
            candidates.add(tail);
            if (tail.endsWith("_ore")) {
               candidates.add(tail.substring(0, tail.length() - 4));
            }
         }

         candidates.add(path);

         for (String candidate : candidates) {
            if (!candidate.isEmpty() && !"ore".equals(candidate)) {
               Set<String> resolved = resolver.apply(candidate);
               if (resolved != null && resolved.size() > 1 && resolved.contains(itemId)) {
                  return candidate;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static boolean init(AiOperation.Result result) {
      if (result != null && result.status() == AiOperation.Status.FAILED) {
         String message = result.message();
         return message != null && message.startsWith("No more reachable sources") && message.contains("(collected 0 of");
      } else {
         return false;
      }
   }

   static String init(String spec, String widenedFamily, String obtainedId) {
      return widenedFamily == null ? spec : obtainedId + " (family fallback from " + spec + ")";
   }

   static JsonObject init(String strategy, String detail, AiOperation.Result result) {
      JsonObject entry = ag.init_JsonObject_ObjArr(
         "strategy", strategy.toLowerCase(Locale.ROOT), "detail", detail, "status", result.status().name(), "message", result.message()
      );

      try {
         JsonElement parsed = JsonParser.parseString(result.dataJson());
         if (parsed.isJsonObject() && !parsed.getAsJsonObject().keySet().isEmpty()) {
            entry.add("data", parsed);
         }
      } catch (RuntimeException var5) {
      }

      return entry;
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close;
   }
}
