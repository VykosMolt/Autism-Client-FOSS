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
   private static final Map<String, String> ITEM_KINDS = new LinkedHashMap<>();
   private static final Map<String, String> BLOCK_KINDS = new LinkedHashMap<>();

   private AiProspect() {
   }

   static boolean canProspect(AiItems.ItemSpec target) {
      return oreKindFor(target) != null;
   }

   static String oreKindFor(AiItems.ItemSpec target) {
      for (String id : target.itemIds()) {
         String kind = ITEM_KINDS.get(id);
         if (kind != null) {
            return kind;
         }
      }

      for (String id : target.blockIds()) {
         String kind = BLOCK_KINDS.get(id);
         if (kind != null) {
            return kind;
         }
      }

      return null;
   }

   static List<String> oreBlocksFor(String kind) {
      List<String> blocks = new ArrayList<>();
      BLOCK_KINDS.forEach((block, family) -> {
         if (family.equals(kind)) {
            blocks.add(block);
         }
      });
      return blocks;
   }

   static String oreBlockId(String kind) {
      return kind.equals("ancient_debris") ? "ancient_debris" : kind + "_ore";
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = AiJson.getString(args, "item", "");
         final AiItems.ItemSpec target = AiItems.resolve(spec);
         final String kind = oreKindFor(target);
         if (kind == null) {
            throw new IllegalArgumentException(
               "'" + spec + "' is not a seed-simulated ore; prospectable families: " + String.join(", ", new LinkedHashSet<>(ITEM_KINDS.values()))
            );
         }

         int requested = Math.max(1, Math.min(2304, AiJson.getInt(args, "count", 1)));
         boolean additional = AiJson.getBoolean(args, "additional", false);
         final int timeoutSeconds = Math.max(30, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 900)));
         final int maxDistance = Math.max(16, Math.min(512, AiJson.getInt(args, "maxDistance", 256)));
         final List<String> oreBlocks = oreBlocksFor(kind);
         final int held = AiItems.countInInventory(target);
         final int goal = additional ? held + requested : requested;
         return new AiOperation() {
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final List<BlockPos> candidates = new ArrayList<>();
            final Set<Long> visited = new LinkedHashSet<>();
            final JsonArray init_JsonArray = new JsonArray();
            final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard();
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiOperation read;
            String init_Str = "";
            int init_I;
            Inner_DataUtil phase = Inner_DataUtil.LOCATE;
            BlockPos init_BlockPos;
            String load_Str = "";
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadlineNanos) {
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
                     if (this.candidates.isEmpty()) {
                        return this.init(AiOperation.Status.FAILED, "No predicted " + kind + " within " + maxDistance + " blocks of the player");
                     }

                     this.phase = Inner_DataUtil.TRAVEL;
                     return this.init("Located " + this.candidates.size() + " predicted " + kind + " site(s)");
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
                              this.phase.name(),
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

                     if (this.phase == Inner_DataUtil.TRAVEL) {
                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           this.load_Str = "travel: " + result.message();
                           this.init_BlockPos = null;
                           this.phase = Inner_DataUtil.TRAVEL;
                           return this.init("Site unreachable; trying the next prediction");
                        } else {
                           this.phase = Inner_DataUtil.EXTRACT;
                           return this.init("Arrived; extracting " + kind);
                        }
                     } else {
                        this.load_Str = "extract: " + result.message();
                        this.init_BlockPos = null;
                        this.phase = Inner_DataUtil.TRAVEL;
                        return this.init("Site exhausted; trying the next prediction");
                     }
                  } else if (this.phase == Inner_DataUtil.LOCATE) {
                     this.init_AiOperation = AiOracle.create(AiJson.objectOf("query", kind, "cap", 16));
                     return this.init("Predicting " + kind + " positions from the world seed");
                  } else if (this.phase == Inner_DataUtil.EXTRACT) {
                     this.load_AiOperation = autismclient.ai.engine.AiMine.init(
                        AiJson.objectOf(
                           "block",
                           AiProspect.oreBlockId(kind),
                           "count",
                           Math.max(1, goal - AiItems.countInInventory(target)),
                           "radius",
                           24,
                           "timeoutSeconds",
                           Math.min(90, this.secondsLeft())
                        )
                     );
                     return this.init("Extracting " + kind + " around " + (this.init_BlockPos == null ? "the area" : this.init_BlockPos.toShortString()));
                  } else {
                     this.init_BlockPos = this.nearestCandidate();
                     if (this.init_BlockPos != null) {
                        this.visited.add(this.init_BlockPos.asLong());
                        this.load_AiOperation = AiGoto.travelToCompat(this.init_BlockPos, 3.5, true, true, true, Math.min(240, this.secondsLeft()));
                        return this.init("Travelling to predicted " + kind + " at " + this.init_BlockPos.toShortString());
                     } else if (this.candidates.isEmpty()) {
                        this.phase = Inner_DataUtil.LOCATE;
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
                  AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
                  if (action == null) {
                     return null;
                  }

                  this.init_Str = AiGuard.decisionReason(this.guardPolicy, vitals);
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
               this.candidates.clear();
               if (data.has("positions") && data.get("positions").isJsonArray()) {
                  for (JsonElement element : data.getAsJsonArray("positions")) {
                     if (element.isJsonObject()) {
                        JsonObject entry = element.getAsJsonObject();
                        BlockPos pos = new BlockPos(
                           AiJson.getInt(entry, "x", 0), AiJson.getInt(entry, "y", 0), AiJson.getInt(entry, "z", 0)
                        );
                        if (!this.visited.contains(pos.asLong()) && !(mc.player.blockPosition().distSqr(pos) > (double)maxDistance * maxDistance)) {
                           this.candidates.add(pos);
                        }
                     }
                  }
               }
            }

            BlockPos nearestCandidate() {
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;

               for (BlockPos candidate : this.candidates) {
                  if (!this.visited.contains(candidate.asLong())) {
                     double distance = mc.player.blockPosition().distSqr(candidate);
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = candidate;
                     }
                  }
               }

               if (best != null) {
                  this.candidates.remove(best);
               }

               return best;
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
            }

            JsonObject evidence() {
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
                  this.phase.name().toLowerCase(Locale.ROOT),
                  "sitesRemaining",
                  this.candidates.size(),
                  "sitesTried",
                  this.visited.size()
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
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.evidence().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.evidence();
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
      ITEM_KINDS.put("minecraft:coal", "coal");
      ITEM_KINDS.put("minecraft:raw_iron", "iron");
      ITEM_KINDS.put("minecraft:iron_ingot", "iron");
      ITEM_KINDS.put("minecraft:raw_copper", "copper");
      ITEM_KINDS.put("minecraft:copper_ingot", "copper");
      ITEM_KINDS.put("minecraft:raw_gold", "gold");
      ITEM_KINDS.put("minecraft:gold_ingot", "gold");
      ITEM_KINDS.put("minecraft:redstone", "redstone");
      ITEM_KINDS.put("minecraft:lapis_lazuli", "lapis");
      ITEM_KINDS.put("minecraft:diamond", "diamond");
      ITEM_KINDS.put("minecraft:emerald", "emerald");
      ITEM_KINDS.put("minecraft:quartz", "quartz");
      ITEM_KINDS.put("minecraft:ancient_debris", "ancient_debris");
      ITEM_KINDS.put("minecraft:netherite_scrap", "ancient_debris");
      BLOCK_KINDS.put("minecraft:coal_ore", "coal");
      BLOCK_KINDS.put("minecraft:deepslate_coal_ore", "coal");
      BLOCK_KINDS.put("minecraft:iron_ore", "iron");
      BLOCK_KINDS.put("minecraft:deepslate_iron_ore", "iron");
      BLOCK_KINDS.put("minecraft:copper_ore", "copper");
      BLOCK_KINDS.put("minecraft:deepslate_copper_ore", "copper");
      BLOCK_KINDS.put("minecraft:gold_ore", "gold");
      BLOCK_KINDS.put("minecraft:deepslate_gold_ore", "gold");
      BLOCK_KINDS.put("minecraft:nether_gold_ore", "gold");
      BLOCK_KINDS.put("minecraft:redstone_ore", "redstone");
      BLOCK_KINDS.put("minecraft:deepslate_redstone_ore", "redstone");
      BLOCK_KINDS.put("minecraft:lapis_ore", "lapis");
      BLOCK_KINDS.put("minecraft:deepslate_lapis_ore", "lapis");
      BLOCK_KINDS.put("minecraft:diamond_ore", "diamond");
      BLOCK_KINDS.put("minecraft:deepslate_diamond_ore", "diamond");
      BLOCK_KINDS.put("minecraft:emerald_ore", "emerald");
      BLOCK_KINDS.put("minecraft:deepslate_emerald_ore", "emerald");
      BLOCK_KINDS.put("minecraft:nether_quartz_ore", "quartz");
      BLOCK_KINDS.put("minecraft:ancient_debris", "ancient_debris");
   }
}
