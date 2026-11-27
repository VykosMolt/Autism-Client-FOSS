package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiSmelt {
   private static final int write = 80;
   private static final int update = 100;
   private static final int sync = 40;
   static final int init_I = 2;
   private static BlockPos lastStationPos;
   private static Object lastStationLevel;
   private static Object lastStationConnection;
   private static long lastStationTime;
   private static final Set<Item> init_Set = Set.of(
      Items.CRAFTING_TABLE,
      Items.CHEST,
      Items.TRAPPED_CHEST,
      Items.BARREL,
      Items.BOOKSHELF,
      Items.LADDER,
      Items.BOWL,
      Items.BOW,
      Items.CRAFTER,
      Items.LECTERN,
      Items.CARTOGRAPHY_TABLE,
      Items.FLETCHING_TABLE,
      Items.SMITHING_TABLE,
      Items.LOOM,
      Items.COMPOSTER,
      Items.NOTE_BLOCK,
      Items.JUKEBOX,
      Items.BEEHIVE
   );
   static final int load_I = 2;
   static final int read = 4;

   private AiSmelt() {
   }

   static boolean isFuelProblem(String message) {
      return message != null && (message.startsWith("Missing compatible fuel") || message.startsWith("Fuel is reserved"));
   }

   public static void init(BlockPos pos) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.getBlockState(pos).getBlock() instanceof AbstractFurnaceBlock) {
         lastStationPos = pos.immutable();
         lastStationLevel = mc.level;
         lastStationConnection = mc.getConnection();
         lastStationTime = System.nanoTime();
      }
   }

   static AiOperation init(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
         JsonObject copy = args.deepCopy();
         copy.addProperty("collect", true);
         String station = mc.player.containerMenu instanceof SmokerMenu
            ? "minecraft:smoker"
            : (mc.player.containerMenu instanceof BlastFurnaceMenu ? "minecraft:blast_furnace" : "minecraft:furnace");
         copy.addProperty("station", station);
         if (lastStationPos != null
            && lastStationLevel == mc.level
            && lastStationConnection == mc.getConnection()
            && System.nanoTime() - lastStationTime < 300000000000L
            && mc.level.getBlockState(lastStationPos).getBlock() == AiCraft.requireBlock(station)
            && mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(lastStationPos)) < mc.player.blockInteractionRange()) {
            copy.addProperty("x", lastStationPos.getX());
            copy.addProperty("y", lastStationPos.getY());
            copy.addProperty("z", lastStationPos.getZ());
         } else if (mc.hitResult instanceof BlockHitResult hit && mc.level.getBlockState(hit.getBlockPos()).getBlock() == AiCraft.requireBlock(station)) {
            copy.addProperty("x", hit.getBlockPos().getX());
            copy.addProperty("y", hit.getBlockPos().getY());
            copy.addProperty("z", hit.getBlockPos().getZ());
         }

         return load(copy);
      } else {
         throw new IllegalArgumentException("Open a furnace, smoker or blast furnace first");
      }
   }

   static AiOperation load(JsonObject args) {
      return init(args, null);
   }

   static AiOperation init(final JsonObject args, final AiFurnaceWork.Inner_DataUtil workPlan) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String outputSpec = AiJson.getString(args, "item", "");
         int requested = AiJson.getInt(args, "count", 1);
         if (requested >= 1 && requested <= 2304) {
            AiItems.ItemSpec outputTarget = AiItems.resolveItem(outputSpec);
            if (!AiJson.getBoolean(args, "additional", false) && AiItems.countInInventory(outputTarget) >= requested) {
               return AiGameOperations.immediate(
                  AiOperation.Status.SUCCEEDED,
                  "Inventory already holds the requested " + outputSpec,
                  AiJson.objectOf("item", outputSpec, "target", requested, "held", AiItems.countInInventory(outputTarget))
               );
            }

            final boolean collect = AiJson.getBoolean(args, "collect", false);
            final boolean loadOnly = AiJson.getBoolean(args, "loadOnly", false);

            final AiSmelt.Inner_CachePool resolved;
            try {
               resolved = collect
                  ? new AiSmelt.Inner_CachePool(
                     load(AiJson.getString(args, "item", "")),
                     load(AiJson.getString(args, "input", "")),
                     fuelOrCoal(AiJson.getString(args, "fuel", "")),
                     AiJson.getString(args, "station", "minecraft:furnace")
                  )
                  : init(mc, outputTarget, AiJson.getString(args, "input", ""), AiJson.getString(args, "fuel", ""), requested);
            } catch (IllegalArgumentException noFuel) {
               if (!collect
                  && AiJson.getString(args, "fuel", "").isBlank()
                  && !AiJson.getBoolean(args, "fuelFetched", false)
                  && noFuel.getMessage() != null
                  && noFuel.getMessage().startsWith("No usable fuel")) {
                  return init(args, workPlan, Math.max(1, requested - (AiJson.getBoolean(args, "additional", false) ? 0 : AiItems.countInInventory(outputTarget))));
               }

               throw noFuel;
            }

            final BlockPos knownStation = args.has("x")
               ? new BlockPos(AiJson.getInt(args, "x", 0), AiJson.getInt(args, "y", 0), AiJson.getInt(args, "z", 0))
               : null;
            boolean additional = AiJson.getBoolean(args, "additional", false);
            final int timeoutSeconds = Math.max(10, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600)));
            final String outputId = BuiltInRegistries.ITEM.getKey(resolved.output()).toString();
            final String statusTitle = "Smelting " + resolved.output().getDefaultInstance().getHoverName().getString();
            final int cookTicksPerItem = autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, resolved.output())
               .stream()
               .filter(p -> p.station().equals(resolved.stationId()) && p.inputs().contains(resolved.input()))
               .mapToInt(p -> Math.max(1, p.cookTimeTicks()))
               .min()
               .orElse(200);
            int held = AiGameState.countInventoryItem(outputId);
            final int target = additional ? held + requested : requested;
            if (!collect && !loadOnly && !args.has("x") && !AiJson.getBoolean(args, "singleFurnace", false) && AiParallelSmelt.furnaceCountFor(target - held) > 1) {
               return new AiParallelSmelt(resolved, target, cookTicksPerItem, timeoutSeconds);
            }

            if (knownStation != null && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
               mc.player.closeContainer();
            }

            return new AiOperation() {
               final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object startLevel = mc.level;
               final Object startConnection = mc.getConnection();
               final LocalPlayer startPlayer = mc.player;
               final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
               final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
               final AiGuardLayer guard = new AiGuardLayer(AiGuard.GuardPolicy.standard(), "smelt");
               AiStationPlacer stationPlacer;
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               AiFurnaceWork furnaceWork;
               JsonObject init_JsonObject = new JsonObject();
               boolean init_Z;
               int init_I;
               BlockPos init_BlockPos;
               boolean load_Z;
               JsonObject load_JsonObject = new JsonObject();
               BlockPos station = knownStation;
               int load_I;
               int read_I;
               int write_I;
               int update_I;
               boolean read_Z;
               boolean write_Z;
               boolean update_Z;
               boolean sync_Z;
               boolean flush;
               AiOperation read_AiOperation;
               final boolean close = AiJson.getString(args, "fuel", "").isBlank();
               AiOperation write_AiOperation;
               int sync_I;

               AiOperation init(int itemsLeft) {
                  int divisor = AiSmelt.burnDivisor(resolved.stationId());
                  long heat = (long)itemsLeft * cookTicksPerItem;
                  int seconds = Math.max(30, Math.min(300, this.secondsLeft() - 20));
                  if (AiObtain.nearestSourceDistance("minecraft:coal") != 1.7976931348623157E308 && AiItems.hasUsableToolFor(Blocks.COAL_ORE.defaultBlockState())) {
                     return AiObtain.init(
                        AiJson.objectOf(
                           "item", "minecraft:coal", "count", (int)Math.ceil(heat / (1600.0 / divisor)), "additional", true, "timeoutSeconds", seconds
                        )
                     );
                  }

                  int planks = (int)Math.ceil(heat / (300.0 / divisor)) + autismclient.ai.engine.AiRestock.reservedWood();
                  return AiObtain.init(AiJson.objectOf("item", "planks", "count", planks, "timeoutSeconds", seconds));
               }

               @Override
               public AiOperation.Result tick() {
                  if (this.write_Z
                     || mc.player == null
                     || mc.level != this.startLevel
                     || mc.getConnection() != this.startConnection
                     || mc.player != this.startPlayer) {
                     return this.init(AiOperation.Status.CANCELLED, "Smelting cancelled or world/player changed; committed contents keep cooking");
                  }

                  if (!mc.player.isAlive()) {
                     return this.init(AiOperation.Status.FAILED, "Player died while smelting");
                  }

                  if (System.nanoTime() > this.deadline) {
                     return this.init(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        "Smelting target not reached before the deadline; pending transfers require reconciliation"
                     );
                  }

                  AiOperation.Result guarded = this.guard.tick(this, this::cancelWork, this::secondsLeft);
                  if (guarded != null) {
                     if (!guarded.terminal()) {
                        return this.init(guarded.message());
                     }

                     JsonObject receipt = this.receipt();
                     receipt.add("survival", AiRunResources.parseEvidenceJson(guarded.dataJson()));
                     this.cancel();
                     return new AiOperation.Result(guarded.status(), guarded.message(), receipt.toString(), guarded.evidence(), guarded.failure());
                  } else {
                     if (AiGameState.countInventoryItem(outputId) >= target && this.load_AiOperation == null) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Inventory already holds the requested " + outputId);
                     }

                     if (this.sync_Z && AiGameState.countInventoryItem(resolved.stationId()) <= 0) {
                        this.sync_Z = false;
                        this.station = null;
                        this.load_I = 0;
                        this.read_AiOperation = AiCraft.craft(
                           AiJson.objectOf("item", resolved.stationId(), "count", 1, "timeoutSeconds", Math.max(20, Math.min(90, this.secondsLeft())))
                        );
                        if (this.read_AiOperation != null) {
                           return this.init("Making a " + resolved.stationId() + " of our own");
                        }
                     }

                     if (this.write_AiOperation != null) {
                        AiOperation.Result got = this.write_AiOperation.tick();
                        if (!got.terminal()) {
                           return this.init("Fetching fuel: " + got.message());
                        }

                        this.write_AiOperation.cancel();
                        this.write_AiOperation = null;
                        return got.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(AiOperation.Status.FAILED, "Out of fuel, and fetching more failed: " + got.message())
                           : this.init("Fuel in hand; back to the " + resolved.stationId());
                     } else if (this.read_AiOperation != null) {
                        AiOperation.Result made = this.read_AiOperation.tick();
                        if (!made.terminal()) {
                           return this.init("Making a " + resolved.stationId() + " of our own: " + made.message());
                        }

                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                        return made.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(AiOperation.Status.FAILED, "Making a " + resolved.stationId() + " failed: " + made.message())
                           : this.init("Our own " + resolved.stationId() + " is ready to place");
                     } else if (this.furnaceWork != null) {
                        AiOperation.Result r = this.furnaceWork.tick();
                        this.init_JsonObject = AiRunResources.parseEvidenceJson(r.dataJson());
                        if (!r.terminal()) {
                           return this.init(r.message());
                        }

                        this.furnaceWork.cancel();
                        this.furnaceWork = null;
                        return r.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(r.status(), "Productive smelt work: " + r.message())
                           : this.init("Back at the furnace to collect or load the next batch");
                     } else {
                        if (this.load_Z && this.init_BlockPos != null) {
                           this.load_Z = false;
                           this.furnaceWork = new AiFurnaceWork(this.station, this.init_BlockPos, BuiltInRegistries.ITEM.getKey(resolved.input()).toString(), 0);
                           return this.init("Survival interruption settled; replan the return to the committed furnace");
                        }

                        if (this.load_AiOperation != null && !(mc.player.containerMenu instanceof AbstractFurnaceMenu)) {
                           if (this.load_AiOperation.requiresReconciliationAfterEmergency()) {
                              return this.init(AiOperation.Status.UNCERTAIN, "Furnace closed with an unconfirmed transfer; reconcile before reopening");
                           }

                           this.load_AiOperation.cancel();
                           this.load_AiOperation = null;
                        }

                        if (this.load_AiOperation != null) {
                           AiOperation.Result result = this.load_AiOperation.tick();
                           this.load_JsonObject = AiRunResources.parseEvidenceJson(result.dataJson());
                           if (!result.terminal()) {
                              if (!loadOnly
                                 && workPlan != null
                                 && !this.init_Z
                                 && this.station != null
                                 && result.message().startsWith("Waiting for furnace output")
                                 && this.load_AiOperation.canInterruptNow()
                                 && mc.player.containerMenu instanceof AbstractFurnaceMenu menu
                                 && menu.isLit()
                                 && !menu.getSlot(0).getItem().isEmpty()
                                 && mc.player.onGround()
                                 && AiFurnaceWork.init_Z_Q_DataUtil(workPlan)) {
                                 int remaining = menu.getSlot(0).getItem().getCount();
                                 int window = Math.max(0, remaining * cookTicksPerItem - (int)(menu.getBurnProgress() * cookTicksPerItem));
                                 this.init_Z = true;
                                 this.init_I++;
                                 this.load_AiOperation.cancel();
                                 this.load_AiOperation = null;
                                 mc.player.closeContainer();
                                 this.load_I = 0;
                                 this.init_BlockPos = mc.player.blockPosition();
                                 String workInput = BuiltInRegistries.ITEM.getKey(resolved.input()).toString();
                                 this.furnaceWork = new AiFurnaceWork(this.station, this.init_BlockPos, workInput, window, workPlan);
                                 return this.init("Batch is cooking; start useful work with a clear walking return route");
                              }

                              if (result.message().startsWith("Loading furnace input")) {
                                 this.init_Z = false;
                              }

                              return this.init(result.message());
                           } else {
                              this.load_AiOperation = null;
                              if (result.status() == AiOperation.Status.SUCCEEDED) {
                                 return this.init(AiOperation.Status.SUCCEEDED, loadOnly ? result.message() : "Smelted " + outputId + " to target");
                              }

                              if (this.close && !collect && !loadOnly && this.sync_I < 2 && AiSmelt.isFuelProblem(result.message())) {
                                 this.sync_I++;
                                 if (mc.player.containerMenu instanceof AbstractFurnaceMenu) {
                                    mc.player.closeContainer();
                                 }

                                 this.load_I = 0;
                                 this.write_AiOperation = this.init(Math.max(1, target - AiGameState.countInventoryItem(outputId)));
                                 if (this.write_AiOperation != null) {
                                    return this.init("Out of spare fuel; fetching more");
                                 }
                              }

                              return this.init(result.status(), result.message());
                           }
                        } else {
                           if (!(mc.player.containerMenu instanceof AbstractFurnaceMenu)) {
                              AiOperation.Result open = this.init_AiOperation_Result_32();
                              if (open != null) {
                                 return open;
                              }
                           }

                           this.load_AiOperation = AiGameplay.update(
                              AiJson.objectOf(
                                 "input",
                                 BuiltInRegistries.ITEM.getKey(resolved.input()).toString(),
                                 "fuel",
                                 BuiltInRegistries.ITEM.getKey(resolved.fuel()).toString(),
                                 "fuelFlexible",
                                 AiJson.getString(args, "fuel", "").isBlank(),
                                 "item",
                                 outputId,
                                 "count",
                                 target,
                                 "batchSize",
                                 AiJson.getInt(args, "batchSize", 64),
                                 "cookTicksPerItem",
                                 cookTicksPerItem,
                                 "fuelBurnDivisor",
                                 AiSmelt.burnDivisor(resolved.stationId()),
                                 "collect",
                                 collect,
                                 "loadOnly",
                                 loadOnly,
                                 "timeoutSeconds",
                                 Math.max(10, this.secondsLeft())
                              )
                           );
                           return this.init("Loading the " + resolved.stationId());
                        }
                     }
                  }
               }

               AiOperation.Result init_AiOperation_Result_32() {
                  if (this.station == null && this.update_I-- <= 0) {
                     this.station = this.findStation();
                     this.update_I = 40;
                  }

                  if (this.station == null && this.update_I < 40 && !this.read_Z) {
                     return this.init("Looking for a " + resolved.stationId());
                  }

                  if (this.station == null) {
                     if (!this.read_Z) {
                        if (AiGameState.countInventoryItem(resolved.stationId()) <= 0 && !this.flush) {
                           this.flush = true;
                           this.read_AiOperation = AiObtain.init(
                              AiJson.objectOf(
                                 "item", resolved.stationId(), "count", 1, "timeoutSeconds", Math.max(30, Math.min(300, this.secondsLeft()))
                              )
                           );
                           return this.init("No " + resolved.stationId() + " near; making one");
                        }

                        if (AiGameState.countInventoryItem(resolved.stationId()) <= 0) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty(resolved.stationId(), 1);
                           return this.init(
                              AiOperation.Status.FAILED,
                              "No " + resolved.stationId() + " in reach or in the bag",
                              AiJson.objectOf("missing", missing)
                           );
                        }

                        this.stationPlacer = new AiStationPlacer(
                           BuiltInRegistries.ITEM.getOptional(Identifier.parse(resolved.stationId())).orElseThrow(), AiProgression.displayName(resolved.stationId())
                        );
                        this.read_Z = true;
                     }

                     AiStationPlacer.Inner_DataUtil status = this.stationPlacer.tick();
                     if (status == AiStationPlacer.Inner_DataUtil.PLACED) {
                        this.station = this.stationPlacer.getPlaced();
                        this.read_Z = false;
                        return this.init(resolved.stationId() + " placed");
                     } else if (status == AiStationPlacer.Inner_DataUtil.FAILED) {
                        this.read_Z = false;
                        return this.init(AiOperation.Status.FAILED, "Could not place a " + resolved.stationId() + ": " + this.stationPlacer.getFailureReason());
                     } else {
                        return this.init(this.stationPlacer.getStatus());
                     }
                  } else {
                     BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, this.station);
                     if (interactionHit == null) {
                        if (this.init_AiOperation == null) {
                           this.init_AiOperation = AiGoto.approachBlockCompat(this.station, Math.min(120, this.secondsLeft()));
                        }

                        AiOperation.Result legResult = this.init_AiOperation.tick();
                        if (!legResult.terminal()) {
                           return this.init("Travelling to the " + resolved.stationId());
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                           if (++this.read_I >= 2) {
                              return this.init(
                                 AiOperation.Status.FAILED, "No route to the " + resolved.stationId() + " at " + this.station.toShortString()
                              );
                           }

                           if (knownStation != null
                              && this.station.equals(knownStation)
                              && mc.level.getBlockState(knownStation).getBlock() == AiCraft.requireBlock(resolved.stationId())) {
                              return this.init("No route to the furnace yet; trying again");
                           }

                           this.station = null;
                           return this.init("No route to that station; looking for another");
                        } else {
                           return this.init("Arrived at the " + resolved.stationId());
                        }
                     } else {
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                        }

                        this.aim.aimAt(interactionHit.getLocation());
                        this.aim.tick();
                        if (++this.load_I > 80) {
                           if (!this.update_Z) {
                              this.update_Z = true;
                              this.sync_Z = true;
                              this.load_I = 0;
                              AutismClientAddon.LOG
                                 .info(
                                    "[AI] smelt: the {} at {} would not open; making one of our own",
                                    resolved.stationId(),
                                    interactionHit.getBlockPos().toShortString()
                                 );
                              return this.init("The " + resolved.stationId() + " will not open; making one");
                           } else {
                              return this.init(
                                 AiOperation.Status.FAILED, "Could not open the " + resolved.stationId() + " at " + this.station.toShortString()
                              );
                           }
                        } else {
                           if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.station)) {
                              return this.init("Turning toward the " + resolved.stationId());
                           }

                           BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                           if (hit == null) {
                              return this.init("Turning toward the " + resolved.stationId());
                           }

                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                           return mc.player.containerMenu instanceof AbstractFurnaceMenu ? null : this.init("Opening the " + resolved.stationId());
                        }
                     }
                  }
               }

               BlockPos findStation() {
                  Block wanted = AiCraft.requireBlock(resolved.stationId());
                  BlockPos origin = mc.player.blockPosition();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  int radius = 16;

                  for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
                     if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wanted) {
                        double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = pos.immutable();
                        }
                     }
                  }

                  return best;
               }

               int secondsLeft() {
                  return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
               }

               JsonObject receipt() {
                  return AiJson.objectOf(
                     "item",
                     outputId,
                     "target",
                     target,
                     "held",
                     AiGameState.countInventoryItem(outputId),
                     "furnace",
                     this.load_JsonObject,
                     "productiveWork",
                     this.init_JsonObject,
                     "productiveBatches",
                     this.init_I,
                     "cookTicksPerItem",
                     cookTicksPerItem,
                     "input",
                     BuiltInRegistries.ITEM.getKey(resolved.input()).toString(),
                     "fuel",
                     BuiltInRegistries.ITEM.getKey(resolved.fuel()).toString(),
                     "station",
                     resolved.stationId(),
                     "stationAt",
                     this.station == null ? null : this.station.toShortString(),
                     "inputHeld",
                     AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(resolved.input()).toString())
                  );
               }

               AiOperation.Result init(String message) {
                  AiVisualization.init(this, statusTitle, AiTaskStatus.init(message, "Preparing the furnace"), AiGameState.countInventoryItem(outputId), target);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.receipt().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  return this.init(status, message, null);
               }

               AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
                  JsonObject data = this.receipt();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               void cancelWork() {
                  if (this.furnaceWork != null) {
                     this.furnaceWork.cancel();
                     this.furnaceWork = null;
                     this.load_Z = true;
                  }

                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                  }

                  if (this.stationPlacer != null) {
                     this.stationPlacer.cancel();
                     this.stationPlacer = null;
                  }

                  this.read_Z = false;
                  this.aim.clear();
                  this.placer.reset();
                  this.load_I = 0;
                  if (mc.player == this.startPlayer && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
                     mc.player.closeContainer();
                  }
               }

               @Override
               public void cancel() {
                  if (!this.write_Z) {
                     this.write_Z = true;
                     AiVisualization.clearOwner(this);
                     this.guard.cancel();
                     if (this.furnaceWork != null) {
                        this.furnaceWork.cancel();
                        this.furnaceWork = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                     }

                     if (this.write_AiOperation != null) {
                        this.write_AiOperation.cancel();
                        this.write_AiOperation = null;
                     }

                     this.aim.clear();
                     this.placer.reset();
                     if (mc.player == this.startPlayer && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
                        mc.player.closeContainer();
                     }

                     if (this.stationPlacer != null) {
                        this.stationPlacer.cancel();
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

               @Override
               public boolean canInterruptNow() {
                  return this.guard.canInterruptNow()
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                     && (this.furnaceWork == null || this.furnaceWork.canInterruptNow())
                     && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.stationPlacer == null || this.stationPlacer.canInterrupt());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.guard.requiresReconciliation()
                     || this.furnaceWork != null && this.furnaceWork.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                     || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.stationPlacer != null && this.stationPlacer.needsReconciliation();
               }
            };
         } else {
            throw new IllegalArgumentException("count must be between 1 and 2304");
         }
      } else {
         throw new IllegalArgumentException("Smelting requires a connected living player");
      }
   }

   static AiOperation init(JsonObject args, final AiFurnaceWork.Inner_DataUtil workPlan, int items) {
      int seconds = Math.max(30, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 600) / 2));
      final AiOperation fuel = AiObtain.nearestSourceDistance("minecraft:coal") != 1.7976931348623157E308 && AiItems.hasUsableToolFor(Blocks.COAL_ORE.defaultBlockState())
         ? AiObtain.init(AiJson.objectOf("item", "minecraft:coal", "count", (int)Math.ceil(items / 8.0), "additional", true, "timeoutSeconds", seconds))
         : AiObtain.init(
            AiJson.objectOf(
               "item", "planks", "count", (int)Math.ceil(items * 200 / 300.0) + autismclient.ai.engine.AiRestock.reservedWood(), "timeoutSeconds", seconds
            )
         );
      final JsonObject retry = args.deepCopy();
      retry.addProperty("fuelFetched", true);
      return new AiOperation() {
         AiOperation fuelOperation = fuel;
         AiOperation load;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z) {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "Smelting cancelled", new JsonObject());
            }

            if (this.load != null) {
               return this.load.tick();
            }

            AiOperation.Result got = this.fuelOperation.tick();
            if (!got.terminal()) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, "Fetching fuel first: " + got.message(), got.dataJson(), List.of());
            }

            this.fuelOperation.cancel();
            if (got.status() != AiOperation.Status.SUCCEEDED) {
               return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "No spare fuel to smelt with, and fetching some failed: " + got.message(), got.dataJson());
            }

            try {
               this.load = AiSmelt.init(retry, workPlan);
            } catch (IllegalArgumentException e) {
               return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, e.getMessage(), "{}");
            }

            return new AiOperation.Result(AiOperation.Status.RUNNING, "Fuel in hand; smelting", "{}", List.of());
         }

         @Override
         public boolean canInterruptNow() {
            return this.load == null ? this.fuelOperation.canInterruptNow() : this.load.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.load == null ? this.fuelOperation.requiresReconciliationAfterEmergency() : this.load.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            this.fuelOperation.cancel();
            if (this.load != null) {
               this.load.cancel();
            }
         }
      };
   }

   static int burnDivisor(String station) {
      return !station.equals("minecraft:smoker") && !station.equals("minecraft:blast_furnace") ? 1 : 2;
   }

   static Item fuelOrCoal(String recorded) {
      return recorded.isBlank() ? Items.COAL : load(recorded);
   }

   private static Item load(String id) {
      return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElseThrow(() -> new IllegalArgumentException("Unknown item " + id));
   }

   static AiSmelt.Inner_CachePool init(Minecraft mc, AiItems.ItemSpec outputTarget, String inputSpec, String fuelSpec, int items) {
      List<String> tried = new ArrayList<>();

      record Inner_DataUtil(Item output, autismclient.util.AutoCraftingEngine.SmeltRecipe plan, Item input, int stationTier) {
      }

      List<Inner_DataUtil> viable = new ArrayList<>();

      for (Item output : outputTarget.items()) {
         for (autismclient.util.AutoCraftingEngine.SmeltRecipe plan : autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, output)) {
            tried.add(plan.recipeKey());
            Item input = init(plan, inputSpec);
            if (input != null) {
               viable.add(new Inner_DataUtil(output, plan, input, stationTier(mc, plan.station())));
            }
         }
      }

      viable.sort(Comparator.comparingInt(Inner_DataUtil::stationTier));

      for (Inner_DataUtil candidate : viable) {
         Item fuel = init(fuelSpec, candidate.input(), items);
         if (fuel != null) {
            return new AiSmelt.Inner_CachePool(candidate.output(), candidate.input(), fuel, candidate.plan().station());
         }
      }

      if (tried.isEmpty()) {
         throw new IllegalArgumentException("No known smelting recipe produces '" + outputTarget.spec() + "'");
      } else if (viable.isEmpty()) {
         throw new IllegalArgumentException("No smelting input for '" + outputTarget.spec() + "' is in the inventory; obtain one of the recipe inputs first");
      } else {
         throw new IllegalArgumentException("No usable fuel in inventory for smelting; obtain coal, charcoal or another fuel first");
      }
   }

   private static int stationTier(Minecraft mc, String stationId) {
      if (hasStationNearby(mc, stationId)) {
         return 0;
      } else {
         return AiGameState.countInventoryItem(stationId) > 0 ? 1 : 2;
      }
   }

   static boolean hasStationNearby(Minecraft mc, String stationId) {
      if (mc.player != null && mc.level != null) {
         Block wanted = AiCraft.requireBlock(stationId);
         BlockPos origin = mc.player.blockPosition();
         int radius = 16;

         for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
            if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wanted) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean init(Minecraft mc, String output, String input) {
      Item wanted = load(output);
      Item source = load(input);
      return autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, wanted)
         .stream()
         .filter(p -> p.inputs().contains(source))
         .anyMatch(p -> AiGameState.countInventoryItem(p.station()) > 0 || hasStationNearby(mc, p.station()));
   }

   private static Item init(autismclient.util.AutoCraftingEngine.SmeltRecipe plan, String inputSpec) {
      if (!inputSpec.isBlank()) {
         AiItems.ItemSpec requested = AiItems.resolveItem(inputSpec);

         for (Item candidate : plan.inputs()) {
            if (requested.items().contains(candidate)) {
               return candidate;
            }
         }

         return null;
      } else {
         Item best = null;
         int bestCount = 0;

         for (Item candidate : plan.inputs()) {
            int count = AiGameState.countMainInventoryItem(BuiltInRegistries.ITEM.getKey(candidate).toString());
            if (count > bestCount) {
               bestCount = count;
               best = candidate;
            }
         }

         return best;
      }
   }

   static Item init(String fuelSpec, Item input, int items) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return null;
      }

      record Inner_StreamUtil(Item item, int burn, int count, ItemStack stack) {
      }

      if (!fuelSpec.isBlank()) {
         AiItems.ItemSpec requested = AiItems.resolveItem(fuelSpec);
         return init(
            requested.items(),
            candidate -> Math.max(0, AiGameState.countMainInventoryItem(BuiltInRegistries.ITEM.getKey(candidate).toString()) - AiRunResources.mandatoryReservedCount(candidate)),
            candidate -> AiItems.fuelBurnTime(new ItemStack(candidate)),
            items
         );
      }

      List<Inner_StreamUtil> fuels = new ArrayList<>();
      Inventory inventory = mc.player.getInventory();
      int spareWood = autismclient.ai.engine.AiRestock.availableSpareWood(inventory);

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty()
            && stack.getItem() != input
            && !stack.isDamageableItem()
            && !init_Set.contains(stack.getItem())
            && AiGameState.countMainInventoryItem(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) > AiRunResources.mandatoryReservedCount(stack.getItem())) {
            int burn = AiItems.fuelBurnTime(stack);
            if (burn > 0) {
               int count = Math.max(0, AiGameState.countMainInventoryItem(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) - AiRunResources.mandatoryReservedCount(stack.getItem()));
               if (stack.is(ItemTags.PLANKS)) {
                  count = Math.min(count, spareWood);
               } else if (stack.is(ItemTags.LOGS)) {
                  count = Math.min(count, spareWood / 4);
               } else if (stack.is(Items.STICK)) {
                  count = Math.max(0, count - 2);
               }

               if (count > 0) {
                  fuels.add(new Inner_StreamUtil(stack.getItem(), burn, count, stack));
               }
            }
         }
      }

      long needed = Math.max(1L, items) * 200L;
      Inner_StreamUtil fallback = null;

      for (int tier = 0; tier < 4; tier++) {
         Inner_StreamUtil cover = null;
         Inner_StreamUtil richest = null;

         for (Inner_StreamUtil held : fuels) {
            if (init_I(held.stack()) == tier) {
               if ((long)held.burn() * held.count() >= needed && (cover == null || held.burn() < cover.burn())) {
                  cover = held;
               }

               if (richest == null || (long)held.burn() * held.count() > (long)richest.burn() * richest.count()) {
                  richest = held;
               }
            }
         }

         if (cover != null) {
            return cover.item();
         }

         if (fallback == null) {
            fallback = richest;
         }
      }

      return fallback == null ? null : fallback.item();
   }

   static int init(Item fuel) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && fuel != null) {
         int count = Math.max(0, AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(fuel).toString()) - AiRunResources.mandatoryReservedCount(fuel));
         ItemStack stack = new ItemStack(fuel);
         int spareWood = autismclient.ai.engine.AiRestock.availableSpareWood(mc.player.getInventory());
         if (stack.is(ItemTags.PLANKS)) {
            return Math.min(count, spareWood);
         } else if (stack.is(ItemTags.LOGS)) {
            return Math.min(count, spareWood / 4);
         } else {
            return stack.is(Items.STICK) ? Math.max(0, count - 2) : count;
         }
      } else {
         return 0;
      }
   }

   static int init_I(ItemStack stack) {
      if (stack.is(ItemTags.LOGS)) {
         return 3;
      } else if (stack.is(ItemTags.PLANKS)) {
         return 2;
      } else {
         return isSmallFuel(stack) ? 0 : 1;
      }
   }

   static <T> T init(Iterable<T> candidates, ToIntFunction<T> count, ToIntFunction<T> burn, int items) {
      long needed = Math.max(1L, items) * 200L;
      long most = 0L;
      T richest = null;

      for (T candidate : candidates) {
         long heat = (long)count.applyAsInt(candidate) * burn.applyAsInt(candidate);
         if (heat >= needed) {
            return candidate;
         }

         if (heat > most) {
            most = heat;
            richest = candidate;
         }
      }

      return richest;
   }

   private static boolean isSmallFuel(ItemStack stack) {
      return stack.is(Items.STICK) || stack.is(ItemTags.SAPLINGS) || stack.is(Items.BAMBOO);
   }

   static JsonArray init(Minecraft mc, Item output) {
      JsonArray out = new JsonArray();

      for (autismclient.util.AutoCraftingEngine.SmeltRecipe plan : autismclient.util.AutoCraftingEngine.getSmeltRecipesFor(mc, output)) {
         JsonArray inputs = new JsonArray();

         for (Item input : plan.inputs()) {
            inputs.add(BuiltInRegistries.ITEM.getKey(input).toString());
         }

         out.add(AiJson.objectOf("recipe", plan.recipeKey(), "station", plan.station(), "outputCount", plan.outputCount(), "inputs", inputs));
      }

      return out;
   }

   record Inner_CachePool(Item output, Item input, Item fuel, String stationId) {
   }
}
