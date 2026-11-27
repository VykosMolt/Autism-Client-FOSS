package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class AiProspect {
   private static final int init_I = 16;
   private static final int load_I = 24;
   private static final double init_D = 3.5;
   private static final int read = 90;
   private static final Map<String, String> init_Map = new LinkedHashMap<>();
   private static final Map<String, String> load_Map = new LinkedHashMap<>();

   private AiProspect() {
   }

   static boolean init_Z(AiItems.ItemSpec target) {
      return init_Str(target) != null;
   }

   static String init_Str(AiItems.ItemSpec target) {
      for (String id : target.itemIds()) {
         String kind = init_Map.get(id);
         if (kind != null) {
            return kind;
         }
      }

      for (String id : target.blockIds()) {
         String kind = load_Map.get(id);
         if (kind != null) {
            return kind;
         }
      }

      return null;
   }

   static List<String> init_List(String kind) {
      List<String> blocks = new ArrayList<>();
      load_Map.forEach((block, family) -> {
         if (family.equals(kind)) {
            blocks.add(block);
         }
      });
      return blocks;
   }

   static String init_Str(String kind) {
      return kind.equals("ancient_debris") ? "ancient_debris" : kind + "_ore";
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = AiJson.getString(args, "item", "");
         final AiItems.ItemSpec target = AiItems.resolve(spec);
         final String kind = init_Str(target);
         if (kind == null) {
            throw new IllegalArgumentException(
               "'" + spec + "' is not a seed-simulated ore; prospectable families: " + String.join(", ", new LinkedHashSet<>(init_Map.values()))
            );
         }

         int requested = Math.max(1, Math.min(2304, AiJson.getInt(args, "count", 1)));
         boolean additional = AiJson.getBoolean(args, "additional", false);
         final int timeoutSeconds = Math.max(30, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 900)));
         final int maxDistance = Math.max(16, Math.min(512, AiJson.getInt(args, "maxDistance", 256)));
         final List<String> oreBlocks = init_List(kind);
         final int held = AiItems.countInInventory(target);
         final int goal = additional ? held + requested : requested;
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final List<BlockPos> init_List = new ArrayList<>();
            final Set<Long> init_Set = new LinkedHashSet<>();
            final JsonArray init_JsonArray = new JsonArray();
            final AiGuard.GuardPolicy init_X_CachePool = AiGuard.GuardPolicy.standard();
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiOperation read;
            String init_Str = "";
            int init_I;
            Inner_DataUtil init_bz_1_DataUtil = Inner_DataUtil.LOCATE;
            BlockPos init_BlockPos;
            String load_Str = "";
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        AiItems.countInInventory(target) > held ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT,
                        "Prospect deadline reached with " + AiItems.countInInventory(target) + "/" + goal
                     );
                  }

                  if (AiItems.countInInventory(target) >= goal) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + spec);
                  }

                  if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.init(AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("prospecting", mc.player.getHealth(), mc.player.getMaxHealth()));
                  }

                  AiOperation.Result tier = this.load();
                  if (tier != null) {
                     return tier;
                  }

                  AiOperation.Result guarded = this.init_AiOperation_Result_29();
                  if (guarded != null) {
                     return guarded;
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result result = this.init_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.init(result.message());
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (result.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init(AiOperation.Status.FAILED, "Could not predict " + kind + " positions: " + result.message());
                     }

                     this.init(AiJson.parseObject(result.dataJson()).getAsJsonObject());
                     if (this.init_List.isEmpty()) {
                        return this.init(AiOperation.Status.FAILED, "No predicted " + kind + " within " + maxDistance + " blocks of the player");
                     }

                     this.init_bz_1_DataUtil = Inner_DataUtil.TRAVEL;
                     return this.init("Located " + this.init_List.size() + " predicted " + kind + " site(s)");
                  } else if (this.load_AiOperation != null) {
                     AiOperation.Result result = this.load_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.init(result.message());
                     }

                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                     this.init_JsonArray
                        .add(
                           AiJson.objectOf(
                              "site",
                              this.init_BlockPos == null ? "" : this.init_BlockPos.toShortString(),
                              "phase",
                              this.init_bz_1_DataUtil.name(),
                              "status",
                              result.status().name(),
                              "message",
                              result.message()
                           )
                        );
                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.init(AiOperation.Status.CANCELLED, "Prospect cancelled");
                     }

                     if (AiItems.countInInventory(target) >= goal) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Obtained " + goal + " " + spec);
                     }

                     if (this.init_bz_1_DataUtil == Inner_DataUtil.TRAVEL) {
                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           this.load_Str = "travel: " + result.message();
                           this.init_BlockPos = null;
                           this.init_bz_1_DataUtil = Inner_DataUtil.TRAVEL;
                           return this.init("Site unreachable; trying the next prediction");
                        } else {
                           this.init_bz_1_DataUtil = Inner_DataUtil.EXTRACT;
                           return this.init("Arrived; extracting " + kind);
                        }
                     } else {
                        this.load_Str = "extract: " + result.message();
                        this.init_BlockPos = null;
                        this.init_bz_1_DataUtil = Inner_DataUtil.TRAVEL;
                        return this.init("Site exhausted; trying the next prediction");
                     }
                  } else if (this.init_bz_1_DataUtil == Inner_DataUtil.LOCATE) {
                     this.init_AiOperation = AiOracle.init_AiOperation_JsonObject(AiJson.objectOf("query", kind, "cap", 16));
                     return this.init("Predicting " + kind + " positions from the world seed");
                  } else if (this.init_bz_1_DataUtil == Inner_DataUtil.EXTRACT) {
                     this.load_AiOperation = autismclient.ai.engine.AiMine.init(
                        AiJson.objectOf(
                           "block",
                           AiProspect.init_Str(kind),
                           "count",
                           Math.max(1, goal - AiItems.countInInventory(target)),
                           "radius",
                           24,
                           "timeoutSeconds",
                           Math.min(90, this.init_I_30())
                        )
                     );
                     return this.init("Extracting " + kind + " around " + (this.init_BlockPos == null ? "the area" : this.init_BlockPos.toShortString()));
                  } else {
                     this.init_BlockPos = this.init_BlockPos_10();
                     if (this.init_BlockPos != null) {
                        this.init_Set.add(this.init_BlockPos.asLong());
                        this.load_AiOperation = AiGoto.travelToCompat(this.init_BlockPos, 3.5, true, true, true, Math.min(240, this.init_I_30()));
                        return this.init("Travelling to predicted " + kind + " at " + this.init_BlockPos.toShortString());
                     } else if (this.init_List.isEmpty()) {
                        this.init_bz_1_DataUtil = Inner_DataUtil.LOCATE;
                        return this.init("Re-predicting from the new position");
                     } else {
                        return this.init(
                           AiOperation.Status.FAILED,
                           "Every predicted " + kind + " site was tried" + (this.load_Str.isEmpty() ? "" : "; last failure " + this.load_Str)
                        );
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Prospect cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_29() {
               if (this.read != null) {
                  AiOperation.Result result = this.read.tick();
                  if (!result.terminal()) {
                     return this.init("Interrupted: " + this.init_Str + " - " + result.message());
                  }

                  this.read.cancel();
                  this.read = null;
                  return this.init("Resuming after " + this.init_Str);
               } else {
                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiOperation action = AiGuard.createResponseOperation(this.init_X_CachePool, vitals, this.init_I_30());
                  if (action == null) {
                     return null;
                  }

                  this.init_Str = AiGuard.decisionReason(this.init_X_CachePool, vitals);
                  this.init_I++;
                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  this.read = action;
                  return this.init("Interrupted: " + this.init_Str);
               }
            }

            AiOperation.Result load() {
               if (oreBlocks.isEmpty()) {
                  return null;
               }

               Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(oreBlocks.get(0))).orElse(null);
               if (block == null) {
                  return null;
               }

               BlockState state = block.defaultBlockState();
               if (AiItems.hasUsableToolFor(state)) {
                  return null;
               }

               Item needed = AiItems.cheapestRequiredTool(state);
               JsonObject missing = new JsonObject();
               missing.addProperty(needed == null ? "minecraft:iron_pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString(), 1);
               return this.init(
                  AiOperation.Status.FAILED,
                  "No tool in the hotbar can harvest "
                     + oreBlocks.get(0)
                     + "; craft "
                     + (needed == null ? "a better pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString())
                     + " first",
                  AiJson.objectOf("missing", missing)
               );
            }

            void init(JsonObject data) {
               this.init_List.clear();
               if (data.has("positions") && data.get("positions").isJsonArray()) {
                  for (JsonElement element : data.getAsJsonArray("positions")) {
                     if (element.isJsonObject()) {
                        JsonObject entry = element.getAsJsonObject();
                        BlockPos pos = new BlockPos(
                           AiJson.getInt(entry, "x", 0), AiJson.getInt(entry, "y", 0), AiJson.getInt(entry, "z", 0)
                        );
                        if (!this.init_Set.contains(pos.asLong()) && !(mc.player.blockPosition().distSqr(pos) > (double)maxDistance * maxDistance)) {
                           this.init_List.add(pos);
                        }
                     }
                  }
               }
            }

            BlockPos init_BlockPos_10() {
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;

               for (BlockPos candidate : this.init_List) {
                  if (!this.init_Set.contains(candidate.asLong())) {
                     double distance = mc.player.blockPosition().distSqr(candidate);
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = candidate;
                     }
                  }
               }

               if (best != null) {
                  this.init_List.remove(best);
               }

               return best;
            }

            int init_I_30() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            JsonObject init_JsonObject_46() {
               JsonObject out = AiJson.objectOf(
                  "item",
                  spec,
                  "ore",
                  kind,
                  "goal",
                  goal,
                  "held",
                  AiItems.countInInventory(target),
                  "startedWith",
                  held,
                  "phase",
                  this.init_bz_1_DataUtil.name().toLowerCase(Locale.ROOT),
                  "sitesRemaining",
                  this.init_List.size(),
                  "sitesTried",
                  this.init_Set.size()
               );
               if (this.init_BlockPos != null) {
                  out.addProperty("site", this.init_BlockPos.toShortString());
               }

               out.add("attempts", this.init_JsonArray);
               out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.init_I, this.init_Str));
               out.addProperty("provenance", "Oracle positions are seed predictions, confirmed only by the extraction phase");
               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_46().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_46();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  if (this.read != null) {
                     this.read.cancel();
                     this.read = null;
                  }

                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }
               }
            }

            enum Inner_DataUtil {
               LOCATE,
               TRAVEL,
               EXTRACT;
            }
         };
      } else {
         throw new IllegalArgumentException("Prospecting requires a connected living player");
      }
   }

   static {
      init_Map.put("minecraft:coal", "coal");
      init_Map.put("minecraft:raw_iron", "iron");
      init_Map.put("minecraft:iron_ingot", "iron");
      init_Map.put("minecraft:raw_copper", "copper");
      init_Map.put("minecraft:copper_ingot", "copper");
      init_Map.put("minecraft:raw_gold", "gold");
      init_Map.put("minecraft:gold_ingot", "gold");
      init_Map.put("minecraft:redstone", "redstone");
      init_Map.put("minecraft:lapis_lazuli", "lapis");
      init_Map.put("minecraft:diamond", "diamond");
      init_Map.put("minecraft:emerald", "emerald");
      init_Map.put("minecraft:quartz", "quartz");
      init_Map.put("minecraft:ancient_debris", "ancient_debris");
      init_Map.put("minecraft:netherite_scrap", "ancient_debris");
      load_Map.put("minecraft:coal_ore", "coal");
      load_Map.put("minecraft:deepslate_coal_ore", "coal");
      load_Map.put("minecraft:iron_ore", "iron");
      load_Map.put("minecraft:deepslate_iron_ore", "iron");
      load_Map.put("minecraft:copper_ore", "copper");
      load_Map.put("minecraft:deepslate_copper_ore", "copper");
      load_Map.put("minecraft:gold_ore", "gold");
      load_Map.put("minecraft:deepslate_gold_ore", "gold");
      load_Map.put("minecraft:nether_gold_ore", "gold");
      load_Map.put("minecraft:redstone_ore", "redstone");
      load_Map.put("minecraft:deepslate_redstone_ore", "redstone");
      load_Map.put("minecraft:lapis_ore", "lapis");
      load_Map.put("minecraft:deepslate_lapis_ore", "lapis");
      load_Map.put("minecraft:diamond_ore", "diamond");
      load_Map.put("minecraft:deepslate_diamond_ore", "diamond");
      load_Map.put("minecraft:emerald_ore", "emerald");
      load_Map.put("minecraft:deepslate_emerald_ore", "emerald");
      load_Map.put("minecraft:nether_quartz_ore", "quartz");
      load_Map.put("minecraft:ancient_debris", "ancient_debris");
   }
}
