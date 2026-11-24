package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.a.HashUtil;
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

final class ci {
   private static final int write = 80;
   private static final int update = 100;
   private static final int sync = 40;
   static final int init_I = 2;
   private static BlockPos init_BlockPos;
   private static Object init_Obj;
   private static Object load_Obj;
   private static long init_J;
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

   private ci() {
   }

   static boolean init_Z(String message) {
      return message != null && (message.startsWith("Missing compatible fuel") || message.startsWith("Fuel is reserved"));
   }

   public static void init(BlockPos pos) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.getBlockState(pos).getBlock() instanceof AbstractFurnaceBlock) {
         init_BlockPos = pos.immutable();
         init_Obj = mc.level;
         load_Obj = mc.getConnection();
         init_J = System.nanoTime();
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
         if (init_BlockPos != null
            && init_Obj == mc.level
            && load_Obj == mc.getConnection()
            && System.nanoTime() - init_J < 300000000000L
            && mc.level.getBlockState(init_BlockPos).getBlock() == e.init_Block(station)
            && mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(init_BlockPos)) < mc.player.blockInteractionRange()) {
            copy.addProperty("x", init_BlockPos.getX());
            copy.addProperty("y", init_BlockPos.getY());
            copy.addProperty("z", init_BlockPos.getZ());
         } else if (mc.hitResult instanceof BlockHitResult hit && mc.level.getBlockState(hit.getBlockPos()).getBlock() == e.init_Block(station)) {
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

   static AiOperation init(final JsonObject args, final Q.Inner_DataUtil workPlan) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         String outputSpec = ag.init_Str_JsonObject(args, "item", "");
         int requested = ag.init_I_JsonObject(args, "count", 1);
         if (requested >= 1 && requested <= 2304) {
            af.Inner_DataUtil outputTarget = af.load_af_DataUtil(outputSpec);
            if (!ag.init_Z_JsonObject(args, "additional", false) && af.init_I(outputTarget) >= requested) {
               return R.init_AiOperation(
                  AiOperation.Status.SUCCEEDED,
                  "Inventory already holds the requested " + outputSpec,
                  ag.init_JsonObject_ObjArr("item", outputSpec, "target", requested, "held", af.init_I(outputTarget))
               );
            }

            final boolean collect = ag.init_Z_JsonObject(args, "collect", false);
            final boolean loadOnly = ag.init_Z_JsonObject(args, "loadOnly", false);

            final ci.Inner_CachePool resolved;
            try {
               resolved = collect
                  ? new ci.Inner_CachePool(
                     load(ag.init_Str_JsonObject(args, "item", "")),
                     load(ag.init_Str_JsonObject(args, "input", "")),
                     init_Item(ag.init_Str_JsonObject(args, "fuel", "")),
                     ag.init_Str_JsonObject(args, "station", "minecraft:furnace")
                  )
                  : init(mc, outputTarget, ag.init_Str_JsonObject(args, "input", ""), ag.init_Str_JsonObject(args, "fuel", ""), requested);
            } catch (IllegalArgumentException noFuel) {
               if (!collect
                  && ag.init_Str_JsonObject(args, "fuel", "").isBlank()
                  && !ag.init_Z_JsonObject(args, "fuelFetched", false)
                  && noFuel.getMessage() != null
                  && noFuel.getMessage().startsWith("No usable fuel")) {
                  return init(args, workPlan, Math.max(1, requested - (ag.init_Z_JsonObject(args, "additional", false) ? 0 : af.init_I(outputTarget))));
               }

               throw noFuel;
            }

            final BlockPos knownStation = args.has("x")
               ? new BlockPos(ag.init_I_JsonObject(args, "x", 0), ag.init_I_JsonObject(args, "y", 0), ag.init_I_JsonObject(args, "z", 0))
               : null;
            boolean additional = ag.init_Z_JsonObject(args, "additional", false);
            final int timeoutSeconds = Math.max(10, Math.min(3600, ag.init_I_JsonObject(args, "timeoutSeconds", 600)));
            final String outputId = BuiltInRegistries.ITEM.getKey(resolved.output()).toString();
            final String statusTitle = "Smelting " + resolved.output().getDefaultInstance().getHoverName().getString();
            final int cookTicksPerItem = autismclient.util.J.load(mc, resolved.output())
               .stream()
               .filter(p -> p.load_Str().equals(resolved.stationId()) && p.init_List().contains(resolved.input()))
               .mapToInt(p -> Math.max(1, p.load_I()))
               .min()
               .orElse(200);
            int held = S.init_I_Str(outputId);
            final int target = additional ? held + requested : requested;
            if (!collect && !loadOnly && !args.has("x") && !ag.init_Z_JsonObject(args, "singleFurnace", false) && aK.init_I_I_3(target - held) > 1) {
               return new aK(resolved, target, cookTicksPerItem, timeoutSeconds);
            }

            if (knownStation != null && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
               mc.player.closeContainer();
            }

            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final Object init_Obj = mc.level;
               final Object load_Obj = mc.getConnection();
               final LocalPlayer init_LocalPlayer = mc.player;
               final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
               final autismclient.ai.a.DataPipe init_DataPipe = new autismclient.ai.a.DataPipe();
               final Y init_Y = new Y(X.Inner_CachePool.standard(), "smelt");
               cn init_cn;
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               Q init_Q;
               JsonObject init_JsonObject = new JsonObject();
               boolean init_Z;
               int init_I;
               BlockPos init_BlockPos;
               boolean load_Z;
               JsonObject load_JsonObject = new JsonObject();
               BlockPos load_BlockPos = knownStation;
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
               final boolean close = ag.init_Str_JsonObject(args, "fuel", "").isBlank();
               AiOperation write_AiOperation;
               int sync_I;

               AiOperation init(int itemsLeft) {
                  int divisor = ci.init_I(resolved.stationId());
                  long heat = (long)itemsLeft * cookTicksPerItem;
                  int seconds = Math.max(30, Math.min(300, this.init_I_33() - 20));
                  if (aE.init_D("minecraft:coal") != 1.7976931348623157E308 && af.init_Z(Blocks.COAL_ORE.defaultBlockState())) {
                     return aE.init(
                        ag.init_JsonObject_ObjArr(
                           "item", "minecraft:coal", "count", (int)Math.ceil(heat / (1600.0 / divisor)), "additional", true, "timeoutSeconds", seconds
                        )
                     );
                  }

                  int planks = (int)Math.ceil(heat / (300.0 / divisor)) + autismclient.ai.a.UtilCore.init_I();
                  return aE.init(ag.init_JsonObject_ObjArr("item", "planks", "count", planks, "timeoutSeconds", seconds));
               }

               @Override
               public AiOperation.Result tick() {
                  if (this.write_Z
                     || mc.player == null
                     || mc.level != this.init_Obj
                     || mc.getConnection() != this.load_Obj
                     || mc.player != this.init_LocalPlayer) {
                     return this.init(AiOperation.Status.CANCELLED, "Smelting cancelled or world/player changed; committed contents keep cooking");
                  }

                  if (!mc.player.isAlive()) {
                     return this.init(AiOperation.Status.FAILED, "Player died while smelting");
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        "Smelting target not reached before the deadline; pending transfers require reconciliation"
                     );
                  }

                  AiOperation.Result guarded = this.init_Y.init_AiOperation_Result_AiOperation(this, this::init_V_25, this::init_I_33);
                  if (guarded != null) {
                     if (!guarded.terminal()) {
                        return this.init(guarded.message());
                     }

                     JsonObject receipt = this.init_JsonObject_49();
                     receipt.add("survival", bK.init_JsonObject(guarded.dataJson()));
                     this.cancel();
                     return new AiOperation.Result(guarded.status(), guarded.message(), receipt.toString(), guarded.evidence(), guarded.failure());
                  } else {
                     if (S.init_I_Str(outputId) >= target && this.load_AiOperation == null) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Inventory already holds the requested " + outputId);
                     }

                     if (this.sync_Z && S.init_I_Str(resolved.stationId()) <= 0) {
                        this.sync_Z = false;
                        this.load_BlockPos = null;
                        this.load_I = 0;
                        this.read_AiOperation = e.init(
                           ag.init_JsonObject_ObjArr("item", resolved.stationId(), "count", 1, "timeoutSeconds", Math.max(20, Math.min(90, this.init_I_33())))
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
                     } else if (this.init_Q != null) {
                        AiOperation.Result r = this.init_Q.tick();
                        this.init_JsonObject = bK.init_JsonObject(r.dataJson());
                        if (!r.terminal()) {
                           return this.init(r.message());
                        }

                        this.init_Q.cancel();
                        this.init_Q = null;
                        return r.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(r.status(), "Productive smelt work: " + r.message())
                           : this.init("Back at the furnace to collect or load the next batch");
                     } else {
                        if (this.load_Z && this.init_BlockPos != null) {
                           this.load_Z = false;
                           this.init_Q = new Q(this.load_BlockPos, this.init_BlockPos, BuiltInRegistries.ITEM.getKey(resolved.input()).toString(), 0);
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
                           this.load_JsonObject = bK.init_JsonObject(result.dataJson());
                           if (!result.terminal()) {
                              if (!loadOnly
                                 && workPlan != null
                                 && !this.init_Z
                                 && this.load_BlockPos != null
                                 && result.message().startsWith("Waiting for furnace output")
                                 && this.load_AiOperation.canInterruptNow()
                                 && mc.player.containerMenu instanceof AbstractFurnaceMenu menu
                                 && menu.isLit()
                                 && !menu.getSlot(0).getItem().isEmpty()
                                 && mc.player.onGround()
                                 && Q.init_Z_Q_DataUtil(workPlan)) {
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
                                 this.init_Q = new Q(this.load_BlockPos, this.init_BlockPos, workInput, window, workPlan);
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

                              if (this.close && !collect && !loadOnly && this.sync_I < 2 && ci.init_Z(result.message())) {
                                 this.sync_I++;
                                 if (mc.player.containerMenu instanceof AbstractFurnaceMenu) {
                                    mc.player.closeContainer();
                                 }

                                 this.load_I = 0;
                                 this.write_AiOperation = this.init(Math.max(1, target - S.init_I_Str(outputId)));
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

                           this.load_AiOperation = T.update(
                              ag.init_JsonObject_ObjArr(
                                 "input",
                                 BuiltInRegistries.ITEM.getKey(resolved.input()).toString(),
                                 "fuel",
                                 BuiltInRegistries.ITEM.getKey(resolved.fuel()).toString(),
                                 "fuelFlexible",
                                 ag.init_Str_JsonObject(args, "fuel", "").isBlank(),
                                 "item",
                                 outputId,
                                 "count",
                                 target,
                                 "batchSize",
                                 ag.init_I_JsonObject(args, "batchSize", 64),
                                 "cookTicksPerItem",
                                 cookTicksPerItem,
                                 "fuelBurnDivisor",
                                 ci.init_I(resolved.stationId()),
                                 "collect",
                                 collect,
                                 "loadOnly",
                                 loadOnly,
                                 "timeoutSeconds",
                                 Math.max(10, this.init_I_33())
                              )
                           );
                           return this.init("Loading the " + resolved.stationId());
                        }
                     }
                  }
               }

               AiOperation.Result init_AiOperation_Result_32() {
                  if (this.load_BlockPos == null && this.update_I-- <= 0) {
                     this.load_BlockPos = this.init_BlockPos_13();
                     this.update_I = 40;
                  }

                  if (this.load_BlockPos == null && this.update_I < 40 && !this.read_Z) {
                     return this.init("Looking for a " + resolved.stationId());
                  }

                  if (this.load_BlockPos == null) {
                     if (!this.read_Z) {
                        if (S.init_I_Str(resolved.stationId()) <= 0 && !this.flush) {
                           this.flush = true;
                           this.read_AiOperation = aE.init(
                              ag.init_JsonObject_ObjArr(
                                 "item", resolved.stationId(), "count", 1, "timeoutSeconds", Math.max(30, Math.min(300, this.init_I_33()))
                              )
                           );
                           return this.init("No " + resolved.stationId() + " near; making one");
                        }

                        if (S.init_I_Str(resolved.stationId()) <= 0) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty(resolved.stationId(), 1);
                           return this.init(
                              AiOperation.Status.FAILED,
                              "No " + resolved.stationId() + " in reach or in the bag",
                              ag.init_JsonObject_ObjArr("missing", missing)
                           );
                        }

                        this.init_cn = new cn(
                           BuiltInRegistries.ITEM.getOptional(Identifier.parse(resolved.stationId())).orElseThrow(), bx.init_Str(resolved.stationId())
                        );
                        this.read_Z = true;
                     }

                     cn.Inner_DataUtil status = this.init_cn.init_cn_DataUtil();
                     if (status == cn.Inner_DataUtil.load) {
                        this.load_BlockPos = this.init_cn.init_BlockPos();
                        this.read_Z = false;
                        return this.init(resolved.stationId() + " placed");
                     } else if (status == cn.Inner_DataUtil.read) {
                        this.read_Z = false;
                        return this.init(AiOperation.Status.FAILED, "Could not place a " + resolved.stationId() + ": " + this.init_cn.load_Str());
                     } else {
                        return this.init(this.init_cn.init_Str());
                     }
                  } else {
                     BlockHitResult interactionHit = autismclient.ai.a.CachePool.init(mc, this.load_BlockPos);
                     if (interactionHit == null) {
                        if (this.init_AiOperation == null) {
                           this.init_AiOperation = HashUtil.init_AiOperation(this.load_BlockPos, Math.min(120, this.init_I_33()));
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
                                 AiOperation.Status.FAILED, "No route to the " + resolved.stationId() + " at " + this.load_BlockPos.toShortString()
                              );
                           }

                           if (knownStation != null
                              && this.load_BlockPos.equals(knownStation)
                              && mc.level.getBlockState(knownStation).getBlock() == e.init_Block(resolved.stationId())) {
                              return this.init("No route to the furnace yet; trying again");
                           }

                           this.load_BlockPos = null;
                           return this.init("No route to that station; looking for another");
                        } else {
                           return this.init("Arrived at the " + resolved.stationId());
                        }
                     } else {
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                        }

                        this.init_DataUtil.init_V_Vec3(interactionHit.getLocation());
                        this.init_DataUtil.load_V();
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
                                 AiOperation.Status.FAILED, "Could not open the " + resolved.stationId() + " at " + this.load_BlockPos.toShortString()
                              );
                           }
                        } else {
                           if (!autismclient.ai.a.DataUtil.init_Z_BlockPos(this.load_BlockPos)) {
                              return this.init("Turning toward the " + resolved.stationId());
                           }

                           BlockHitResult hit = autismclient.ai.a.DataUtil.init_BlockHitResult();
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

               BlockPos init_BlockPos_13() {
                  Block wanted = e.init_Block(resolved.stationId());
                  BlockPos origin = mc.player.blockPosition();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;
                  int radius = 16;

                  for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
                     if (cu.init(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wanted) {
                        double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = pos.immutable();
                        }
                     }
                  }

                  return best;
               }

               int init_I_33() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_49() {
                  return ag.init_JsonObject_ObjArr(
                     "item",
                     outputId,
                     "target",
                     target,
                     "held",
                     S.init_I_Str(outputId),
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
                     this.load_BlockPos == null ? null : this.load_BlockPos.toShortString(),
                     "inputHeld",
                     S.init_I_Str(BuiltInRegistries.ITEM.getKey(resolved.input()).toString())
                  );
               }

               AiOperation.Result init(String message) {
                  cG.init(this, statusTitle, cs.init(message, "Preparing the furnace"), S.init_I_Str(outputId), target);
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_49().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  return this.init(status, message, null);
               }

               AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
                  JsonObject data = this.init_JsonObject_49();
                  if (extra != null) {
                     for (Entry<String, JsonElement> entry : extra.entrySet()) {
                        data.add(entry.getKey(), entry.getValue());
                     }
                  }

                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               void init_V_25() {
                  if (this.init_Q != null) {
                     this.init_Q.cancel();
                     this.init_Q = null;
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

                  if (this.init_cn != null) {
                     this.init_cn.init_V();
                     this.init_cn = null;
                  }

                  this.read_Z = false;
                  this.init_DataUtil.init_V();
                  this.init_DataPipe.init_V();
                  this.load_I = 0;
                  if (mc.player == this.init_LocalPlayer && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
                     mc.player.closeContainer();
                  }
               }

               @Override
               public void cancel() {
                  if (!this.write_Z) {
                     this.write_Z = true;
                     cG.init_V(this);
                     this.init_Y.init_V();
                     if (this.init_Q != null) {
                        this.init_Q.cancel();
                        this.init_Q = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                     }

                     if (this.write_AiOperation != null) {
                        this.write_AiOperation.cancel();
                        this.write_AiOperation = null;
                     }

                     this.init_DataUtil.init_V();
                     this.init_DataPipe.init_V();
                     if (mc.player == this.init_LocalPlayer && mc.player.containerMenu instanceof AbstractFurnaceMenu) {
                        mc.player.closeContainer();
                     }

                     if (this.init_cn != null) {
                        this.init_cn.init_V();
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
                  return this.init_Y.load()
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                     && (this.init_Q == null || this.init_Q.canInterruptNow())
                     && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                     && (this.init_cn == null || this.init_cn.init_Z());
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return this.init_Y.read()
                     || this.init_Q != null && this.init_Q.requiresReconciliationAfterEmergency()
                     || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                     || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                     || this.init_cn != null && this.init_cn.load_Z();
               }
            };
         } else {
            throw new IllegalArgumentException("count must be between 1 and 2304");
         }
      } else {
         throw new IllegalArgumentException("Smelting requires a connected living player");
      }
   }

   static AiOperation init(JsonObject args, final Q.Inner_DataUtil workPlan, int items) {
      int seconds = Math.max(30, Math.min(300, ag.init_I_JsonObject(args, "timeoutSeconds", 600) / 2));
      final AiOperation fuel = aE.init_D("minecraft:coal") != 1.7976931348623157E308 && af.init_Z(Blocks.COAL_ORE.defaultBlockState())
         ? aE.init(ag.init_JsonObject_ObjArr("item", "minecraft:coal", "count", (int)Math.ceil(items / 8.0), "additional", true, "timeoutSeconds", seconds))
         : aE.init(
            ag.init_JsonObject_ObjArr(
               "item", "planks", "count", (int)Math.ceil(items * 200 / 300.0) + autismclient.ai.a.UtilCore.init_I(), "timeoutSeconds", seconds
            )
         );
      final JsonObject retry = args.deepCopy();
      retry.addProperty("fuelFetched", true);
      return new AiOperation() {
         AiOperation init_AiOperation = fuel;
         AiOperation load;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_Z) {
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Smelting cancelled", new JsonObject());
            }

            if (this.load != null) {
               return this.load.tick();
            }

            AiOperation.Result got = this.init_AiOperation.tick();
            if (!got.terminal()) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, "Fetching fuel first: " + got.message(), got.dataJson(), List.of());
            }

            this.init_AiOperation.cancel();
            if (got.status() != AiOperation.Status.SUCCEEDED) {
               return AiOperation.Result.failed(A.pop, "No spare fuel to smelt with, and fetching some failed: " + got.message(), got.dataJson());
            }

            try {
               this.load = ci.init(retry, workPlan);
            } catch (IllegalArgumentException e) {
               return AiOperation.Result.failed(A.pop, e.getMessage(), "{}");
            }

            return new AiOperation.Result(AiOperation.Status.RUNNING, "Fuel in hand; smelting", "{}", List.of());
         }

         @Override
         public boolean canInterruptNow() {
            return this.load == null ? this.init_AiOperation.canInterruptNow() : this.load.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.load == null ? this.init_AiOperation.requiresReconciliationAfterEmergency() : this.load.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            this.init_AiOperation.cancel();
            if (this.load != null) {
               this.load.cancel();
            }
         }
      };
   }

   static int init_I(String station) {
      return !station.equals("minecraft:smoker") && !station.equals("minecraft:blast_furnace") ? 1 : 2;
   }

   static Item init_Item(String recorded) {
      return recorded.isBlank() ? Items.COAL : load(recorded);
   }

   private static Item load(String id) {
      return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElseThrow(() -> new IllegalArgumentException("Unknown item " + id));
   }

   static ci.Inner_CachePool init(Minecraft mc, af.Inner_DataUtil outputTarget, String inputSpec, String fuelSpec, int items) {
      List<String> tried = new ArrayList<>();

      record Inner_DataUtil(Item output, autismclient.util.J.Inner_StreamUtil plan, Item input, int stationTier) {
      }

      List<Inner_DataUtil> viable = new ArrayList<>();

      for (Item output : outputTarget.items()) {
         for (autismclient.util.J.Inner_StreamUtil plan : autismclient.util.J.load(mc, output)) {
            tried.add(plan.init_Str());
            Item input = init(plan, inputSpec);
            if (input != null) {
               viable.add(new Inner_DataUtil(output, plan, input, init_I(mc, plan.load_Str())));
            }
         }
      }

      viable.sort(Comparator.comparingInt(Inner_DataUtil::stationTier));

      for (Inner_DataUtil candidate : viable) {
         Item fuel = init(fuelSpec, candidate.input(), items);
         if (fuel != null) {
            return new ci.Inner_CachePool(candidate.output(), candidate.input(), fuel, candidate.plan().load_Str());
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

   private static int init_I(Minecraft mc, String stationId) {
      if (init_Z(mc, stationId)) {
         return 0;
      } else {
         return S.init_I_Str(stationId) > 0 ? 1 : 2;
      }
   }

   static boolean init_Z(Minecraft mc, String stationId) {
      if (mc.player != null && mc.level != null) {
         Block wanted = e.init_Block(stationId);
         BlockPos origin = mc.player.blockPosition();
         int radius = 16;

         for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 8, radius))) {
            if (cu.init(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == wanted) {
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
      return autismclient.util.J.load(mc, wanted)
         .stream()
         .filter(p -> p.init_List().contains(source))
         .anyMatch(p -> S.init_I_Str(p.load_Str()) > 0 || init_Z(mc, p.load_Str()));
   }

   private static Item init(autismclient.util.J.Inner_StreamUtil plan, String inputSpec) {
      if (!inputSpec.isBlank()) {
         af.Inner_DataUtil requested = af.load_af_DataUtil(inputSpec);

         for (Item candidate : plan.init_List()) {
            if (requested.items().contains(candidate)) {
               return candidate;
            }
         }

         return null;
      } else {
         Item best = null;
         int bestCount = 0;

         for (Item candidate : plan.init_List()) {
            int count = S.load(BuiltInRegistries.ITEM.getKey(candidate).toString());
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
         af.Inner_DataUtil requested = af.load_af_DataUtil(fuelSpec);
         return init(
            requested.items(),
            candidate -> Math.max(0, S.load(BuiltInRegistries.ITEM.getKey(candidate).toString()) - bK.load(candidate)),
            candidate -> af.init_I(new ItemStack(candidate)),
            items
         );
      }

      List<Inner_StreamUtil> fuels = new ArrayList<>();
      Inventory inventory = mc.player.getInventory();
      int spareWood = autismclient.ai.a.UtilCore.init(inventory);

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (!stack.isEmpty()
            && stack.getItem() != input
            && !stack.isDamageableItem()
            && !init_Set.contains(stack.getItem())
            && S.load(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) > bK.load(stack.getItem())) {
            int burn = af.init_I(stack);
            if (burn > 0) {
               int count = Math.max(0, S.load(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) - bK.load(stack.getItem()));
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
         int count = Math.max(0, S.init_I_Str(BuiltInRegistries.ITEM.getKey(fuel).toString()) - bK.load(fuel));
         ItemStack stack = new ItemStack(fuel);
         int spareWood = autismclient.ai.a.UtilCore.init(mc.player.getInventory());
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
         return init_Z(stack) ? 0 : 1;
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

   private static boolean init_Z(ItemStack stack) {
      return stack.is(Items.STICK) || stack.is(ItemTags.SAPLINGS) || stack.is(Items.BAMBOO);
   }

   static JsonArray init(Minecraft mc, Item output) {
      JsonArray out = new JsonArray();

      for (autismclient.util.J.Inner_StreamUtil plan : autismclient.util.J.load(mc, output)) {
         JsonArray inputs = new JsonArray();

         for (Item input : plan.init_List()) {
            inputs.add(BuiltInRegistries.ITEM.getKey(input).toString());
         }

         out.add(ag.init_JsonObject_ObjArr("recipe", plan.init_Str(), "station", plan.load_Str(), "outputCount", plan.init_I(), "inputs", inputs));
      }

      return out;
   }

   record Inner_CachePool(Item output, Item input, Item fuel, String stationId) {
   }
}
