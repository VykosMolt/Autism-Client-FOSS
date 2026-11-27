package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.modules.Module;
import autismclient.util.BlockPlacementPlanner;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiMlg {
   static final int init_I = 3;
   static final int load = 4;
   static final double init_D = 2.0;
   static final int read = 90;
   static final int write = 5;
   static final int update = 3;
   static final int sync = 20;
   static final int flush = 60;
   static final float init_F = 0.5F;
   private static final Set<String> inBlockLandings = Set.of("water", "cobweb", "powder_snow");

   private AiMlg() {
   }

   static String chooseMethod(String requested, List<String> hotbarItemPaths, boolean redirectAvailable) {
      String var10000;
      switch (requested) {
         case "water":
            var10000 = hotbarSlotOf(hotbarItemPaths, "water_bucket") >= 0 ? "water" : null;
            break;
         case "cobweb":
            var10000 = hotbarSlotOf(hotbarItemPaths, "cobweb") >= 0 ? "cobweb" : null;
            break;
         default:
            var10000 = hotbarSlotOf(hotbarItemPaths, "water_bucket") >= 0
               ? "water"
               : (hotbarSlotOf(hotbarItemPaths, "cobweb") >= 0 ? "cobweb" : (redirectAvailable ? "redirect" : null));
      }

      return var10000;
   }

   static int hotbarSlotOf(List<String> hotbarItemPaths, String itemPath) {
      for (int i = 0; i < hotbarItemPaths.size(); i++) {
         if (itemPath.equals(hotbarItemPaths.get(i))) {
            return i;
         }
      }

      return -1;
   }

   static int placementWindowAtZeroPing(String method) {
      return placementWindow(method, 0);
   }

   static int placementWindow(String method, int pingMillis) {
      int var10000;
      switch (method) {
         case "water":
            var10000 = waterWindowTicks(pingMillis);
            break;
         case "cobweb":
            var10000 = 4;
            break;
         default:
            var10000 = 0;
      }

      return var10000;
   }

   static int waterWindowTicks(int pingMillis) {
      return 3 + Math.min(5, Math.max(0, pingMillis) / 100);
   }

   static boolean isInWindowAtZeroPing(String method, int ticksToLand) {
      return isInPlacementWindow(method, ticksToLand, 0);
   }

   static boolean isInPlacementWindow(String method, int ticksToLand, int pingMillis) {
      int window = placementWindow(method, pingMillis);
      return window > 0 && ticksToLand >= 1 && ticksToLand <= window;
   }

   static float landingDamageMultiplier(String landingBlockId, boolean sneaking) {
      return AiDanger.init_F_Str_Z(landingBlockId, sneaking);
   }

   static boolean isSafeMultiplier(float multiplier) {
      return multiplier <= 0.2F;
   }

   static int init_I_D(double naturalX, double naturalZ, List<double[]> targets) {
      int best = -1;
      double bestDistance = 0.0;

      for (int i = 0; i < targets.size(); i++) {
         double[] t = targets.get(i);
         double distance = Math.hypot(t[0] - naturalX, t[1] - naturalZ);
         if (distance <= 2.000000001 && (best < 0 || distance < bestDistance)) {
            best = i;
            bestDistance = distance;
         }
      }

      return best;
   }

   static boolean hasAirToSpare(boolean eyeSubmerged, int airSupply) {
      return !eyeSubmerged || airSupply >= 90;
   }

   static AiOperation create(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final String requested = AiJson.getString(args, "method", "auto").toLowerCase(Locale.ROOT);
         if (!requested.equals("water") && !requested.equals("cobweb") && !requested.equals("auto")) {
            throw new IllegalArgumentException("method must be water, cobweb or auto");
         }

         final boolean collect = AiJson.getBoolean(args, "collect", true);
         final long deadline = System.nanoTime() + Math.max(1, Math.min(30, AiJson.getInt(args, "timeoutSeconds", 10))) * 1000000000L;
         final boolean waterUsable = !client.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES);
         return new AiOperation() {
            final Minecraft client = Minecraft.getInstance();
            final Object startLevel = this.client.level;
            final Object startConnection = this.client.getConnection();
            final UUID startPlayerId = this.client.player.getUUID();
            final int originalSlot = this.client.player.getInventory().getSelectedSlot();
            final int pingMillis = AiMlg.currentPingMillis();
            Inner_DataUtil stage = Inner_DataUtil.GRACE;
            boolean cancelled;
            boolean clutchPlaced;
            boolean waterCollected;
            boolean write_Z;
            boolean update_Z;
            String clutchMethod;
            int graceTicks;
            int landedTicks;
            int update_I;
            int collectTicks;
            int switchedSlot = -1;
            double maxFallDistance;
            float healthBeforeLanding;
            float healthAfterLanding;
            float forecastDamage;
            float damageTaken;
            BlockPos placedCell;
            BlockPos redirectTarget;
            BlockPos forecastSupport;
            AiMlg.Inner_DataUtil forecast;
            int close;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.client.player != null && this.client.level != null) {
                  if (this.client.level != this.startLevel
                     || this.client.getConnection() != this.startConnection
                     || !this.startPlayerId.equals(this.client.player.getUUID())
                     || !this.client.player.isAlive()) {
                     return this.finish(AiOperation.Status.CANCELLED, "Clutch player, world or connection changed");
                  }

                  if ("water".equals(this.clutchMethod)
                     && this.client.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                     return this.finish(AiOperation.Status.FAILED, "Water clutch prohibited by the current dimension attributes");
                  }

                  if (System.nanoTime() > deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Clutch window passed before a safe landing");
                  }

                  LocalPlayer player = this.client.player;
                  ClientLevel level = this.client.level;
                  AiOperation.Result var10000;
                  switch (this.stage) {
                     case GRACE:
                        var10000 = this.tickGrace(player);
                        break;
                     case FALLING:
                        var10000 = this.tickFalling(player, level);
                        break;
                     case SETTLE:
                        var10000 = this.tickSettle(player, level);
                        break;
                     case COLLECT:
                        var10000 = this.tickCollect(player, level);
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "MLG clutch cancelled");
               }
            }

            AiOperation.Result tickGrace(LocalPlayer player) {
               Module noFall = autismclient.modules.al.init_Module_Str("no-fall");
               if (noFall != null && noFall.isEnabled()) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "No-fall module is active; no clutch needed");
               } else {
                  boolean airborne = !player.onGround() || player.fallDistance > 0.5 || player.getDeltaMovement().y < -0.08;
                  if (airborne) {
                     this.stage = Inner_DataUtil.FALLING;
                     return this.running("Fall detected; tracking the landing");
                  } else {
                     return ++this.graceTicks > 20 ? this.finish(AiOperation.Status.FAILED, "No fall in progress") : this.running("Waiting for the fall to begin");
                  }
               }
            }

            AiOperation.Result tickFalling(LocalPlayer player, ClientLevel level) {
               this.maxFallDistance = Math.max(this.maxFallDistance, player.fallDistance);
               this.healthBeforeLanding = player.getHealth();
               if (this.hasLanded(player, level)) {
                  this.stage = Inner_DataUtil.SETTLE;
                  this.landedTicks = 0;
                  this.healthAfterLanding = player.getHealth();
                  this.forecastSupport = player.blockPosition();
                  return this.running("Landed; measuring the applied fall damage");
               }

               AiMlg.Inner_DataUtil forecast = AiMlg.forecastFall(player, level);
               this.forecast = forecast;
               if (forecast.ticksToLand() >= 0) {
                  if (AiMlg.isSafeMultiplier(forecast.multiplier())) {
                     this.forecastDamage = this.damageTaken = forecast.damage();
                     this.forecastSupport = forecast.support();
                     return this.finish(AiOperation.Status.SUCCEEDED, "Natural soft landing");
                  }

                  if (forecast.damage() <= 0.5F) {
                     this.forecastDamage = this.damageTaken = forecast.damage();
                     this.forecastSupport = forecast.support();
                     return this.finish(AiOperation.Status.SUCCEEDED, "Fall predicted harmless; no clutch needed");
                  }

                  if (this.clutchMethod == null) {
                     AiOperation.Result failure = this.init(player, level, forecast);
                     if (failure != null) {
                        return failure;
                     }
                  }

                  if ("redirect".equals(this.clutchMethod)) {
                     BlockPos target = AiMlg.chooseRedirectTarget(player, level, forecast.support().above(), player.isSuppressingBounce());
                     if (target != null) {
                        this.redirectTarget = target;
                     }

                     if (this.redirectTarget != null) {
                        Vec3 d = Vec3.atBottomCenterOf(this.redirectTarget).subtract(player.position());
                        player.setYRot((float)(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0));
                        this.client.options.keyUp.setDown(true);
                     }

                     return this.running("Steering toward a soft landing");
                  } else {
                     if (!this.clutchPlaced) {
                        this.clutchPlaced = this.isClutchInPlace(level);
                        if (!this.clutchPlaced && AiMlg.isInPlacementWindow(this.clutchMethod, forecast.ticksToLand(), this.pingMillis)) {
                           AiOperation.Result failure = this.load(player, level, forecast);
                           if (failure != null) {
                              return failure;
                           }
                        }
                     }

                     return this.running(
                        this.clutchPlaced
                           ? "Clutch placed; riding the fall in"
                           : "Falling; the placement window opens at " + AiMlg.placementWindow(this.clutchMethod, this.pingMillis) + " ticks before touchdown"
                     );
                  }
               } else {
                  return !forecast.voidFall() && !(player.getY() < level.getMinY())
                     ? this.running("Falling; the landing is not inside the prediction horizon yet")
                     : this.finish(AiOperation.Status.FAILED, "The fall crosses below the void floor (y=" + level.getMinY() + "); there is no landing to clutch");
               }
            }

            AiOperation.Result init(LocalPlayer player, ClientLevel level, AiMlg.Inner_DataUtil forecast) {
               List<String> hotbar = AiMlg.hotbarItemPaths(player);
               if (waterUsable
                  && !"cobweb".equals(requested)
                  && AiMlg.hotbarSlotOf(hotbar, "water_bucket") < 0
                  && this.close < 3
                  && this.init(player, Items.WATER_BUCKET)) {
                  this.close++;
                  return this.running("Moving the water bucket to the hotbar");
               }

               if ("water".equals(requested)) {
                  if (!waterUsable) {
                     return this.finish(AiOperation.Status.FAILED, "Water evaporates in this dimension; use a cobweb");
                  }

                  if (AiMlg.hotbarSlotOf(hotbar, "water_bucket") < 0) {
                     return this.finish(AiOperation.Status.FAILED, "No water bucket in hotbar");
                  }

                  this.clutchMethod = "water";
                  return null;
               } else if ("cobweb".equals(requested)) {
                  if (AiMlg.hotbarSlotOf(hotbar, "cobweb") < 0) {
                     return this.finish(AiOperation.Status.FAILED, "No cobweb in hotbar");
                  }

                  this.clutchMethod = "cobweb";
                  return null;
               } else {
                  this.redirectTarget = AiMlg.chooseRedirectTarget(player, level, forecast.support().above(), player.isSuppressingBounce());
                  this.clutchMethod = AiMlg.chooseMethod("auto", waterUsable ? hotbar : AiMlg.withoutItem(hotbar, "water_bucket"), this.redirectTarget != null);
                  return this.clutchMethod == null ? this.finish(AiOperation.Status.FAILED, "No clutch item in hotbar") : null;
               }
            }

            AiOperation.Result load(LocalPlayer player, ClientLevel level, AiMlg.Inner_DataUtil forecast) {
               List<String> hotbar = AiMlg.hotbarItemPaths(player);
               String item = "water".equals(this.clutchMethod) ? "water_bucket" : "cobweb";
               int slot = AiMlg.hotbarSlotOf(hotbar, item);
               if (slot < 0) {
                  return this.finish(AiOperation.Status.FAILED, "Clutch item disappeared from the hotbar");
               }

               this.selectSlot(player, slot);
               BlockPos cell = forecast.support().above();
               this.placedCell = cell;
               if ("water".equals(this.clutchMethod)) {
                  AiGameOperations.snapLookAt(Vec3.atCenterOf(cell.below()).add(0.0, 0.5, 0.0));
                  this.client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
               } else {
                  ItemStack stack = player.getInventory().getItem(slot);
                  BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(
                        cell, player.getEyePosition(), Math.max(4.5, player.blockInteractionRange()), BlockPlacementPlanner.blockItemValidator(stack, player, InteractionHand.MAIN_HAND)
                     )
                     .withSneaking(player.isShiftKeyDown())
                     .withRayBudget(new BlockPlacementPlanner.RayBudget(6));
                  BlockPlacementPlanner.PlacementPlan candidate = BlockPlacementPlanner.plan(request);
                  if (candidate != null) {
                     player.setYRot(candidate.aim().emittedRotation().yaw());
                     player.setXRot(candidate.aim().emittedRotation().pitch());
                     this.client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, candidate.hit());
                  } else {
                     Vec3 hitPos = Vec3.atCenterOf(forecast.support()).add(0.0, 0.5, 0.0);
                     AiGameOperations.snapLookAt(hitPos);
                     this.client
                        .gameMode
                        .useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(hitPos, Direction.UP, forecast.support(), false));
                  }
               }

               return null;
            }

            AiOperation.Result tickSettle(LocalPlayer player, ClientLevel level) {
               this.healthAfterLanding = Math.min(this.healthAfterLanding, player.getHealth());
               if (++this.landedTicks < 5) {
                  return this.running("Landed; measuring the applied fall damage");
               }

               this.damageTaken = Math.max(0.0F, this.healthBeforeLanding - this.healthAfterLanding);
               this.forecastDamage = this.forecast == null ? 0.0F : this.forecast.damage();
               boolean softActual = player.isInWater()
                  || "cobweb".equals(AiMlg.blockPathAt(level, player.blockPosition()))
                  || AiMlg.landingDamageMultiplier(AiMlg.blockPathAt(level, player.blockPosition().below()), player.isSuppressingBounce()) <= 0.2F;
               if (!(this.damageTaken <= 0.5F) || !this.clutchPlaced && !softActual && !"redirect".equals(this.clutchMethod)) {
                  if ("water".equals(this.clutchMethod) && this.clutchPlaced && collect && player.isAlive()) {
                     this.update_Z = true;
                     this.stage = Inner_DataUtil.COLLECT;
                     return this.running("Clutch landed late; collecting the water");
                  } else {
                     return !this.clutchPlaced && !"redirect".equals(this.clutchMethod)
                        ? this.finish(AiOperation.Status.FAILED, "Clutch missed")
                        : this.finish(
                           AiOperation.Status.FAILED,
                           "redirect".equals(this.clutchMethod) ? "Redirect missed the soft landing" : "Clutch placed too late; fall damage was still applied"
                        );
                  }
               } else if ("water".equals(this.clutchMethod) && this.clutchPlaced && collect) {
                  this.stage = Inner_DataUtil.COLLECT;
                  return this.running("Clutch landed; collecting the water");
               } else {
                  return this.finish(AiOperation.Status.SUCCEEDED, this.landedMessage());
               }
            }

            String landedMessage() {
               if (this.clutchPlaced) {
                  return "water".equals(this.clutchMethod) ? "Water clutch landed" : "Cobweb clutch landed";
               } else {
                  return "redirect".equals(this.clutchMethod) ? "Redirected to a soft landing" : "Landed on a naturally soft block";
               }
            }

            AiOperation.Result tickCollect(LocalPlayer player, ClientLevel level) {
               BlockPos cell = this.placedWaterCell(player, level);
               boolean sourceGone = cell != null && !level.getFluidState(cell).is(FluidTags.WATER);
               boolean bucketFilled = "water_bucket"
                  .equals(BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(player.getInventory().getSelectedSlot()).getItem()).getPath());
               if (this.update_I <= 0 || !sourceGone && !bucketFilled) {
                  if (!AiMlg.hasAirToSpare(player.isUnderWater(), player.getAirSupply())) {
                     if (++this.collectTicks > 60) {
                        this.write_Z = true;
                        return this.finish(this.update_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED, "Water clutch landed; water left in place");
                     } else {
                        this.client.options.keyJump.setDown(true);
                        return this.running("Surfacing before collecting the water");
                     }
                  } else if (cell != null && this.update_I < 3) {
                     int slot = AiMlg.hotbarSlotOf(AiMlg.hotbarItemPaths(player), "bucket");
                     if (slot < 0) {
                        return this.finish(
                           this.update_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED, "Water clutch landed; no empty bucket for collection"
                        );
                     }

                     this.selectSlot(player, slot);
                     AiGameOperations.snapLookAt(Vec3.atCenterOf(cell));
                     this.client.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                     this.update_I++;
                     return this.running("Collecting the placed water");
                  } else {
                     return this.finish(this.update_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED, "Water clutch landed; water left in place");
                  }
               } else {
                  this.waterCollected = true;
                  return this.update_Z
                     ? this.finish(AiOperation.Status.FAILED, "Clutch placed too late; fall damage was still applied (water collected)")
                     : this.finish(AiOperation.Status.SUCCEEDED, "Water clutch landed; water collected");
               }
            }

            BlockPos placedWaterCell(LocalPlayer player, ClientLevel level) {
               if (this.placedCell != null && level.getFluidState(this.placedCell).is(FluidTags.WATER)) {
                  return this.placedCell;
               }

               BlockPos feet = player.blockPosition();
               return level.getFluidState(feet).is(FluidTags.WATER) ? feet : this.placedCell;
            }

            boolean hasLanded(LocalPlayer player, ClientLevel level) {
               return player.onGround()
                  || player.isInWater()
                  || "cobweb".equals(AiMlg.blockPathAt(level, player.blockPosition()))
                  || this.maxFallDistance >= 1.0 && player.fallDistance <= 0.5;
            }

            boolean isClutchInPlace(ClientLevel level) {
               if (this.placedCell == null) {
                  return false;
               } else {
                  return "water".equals(this.clutchMethod)
                     ? level.getFluidState(this.placedCell).is(FluidTags.WATER)
                     : "cobweb".equals(AiMlg.blockPathAt(level, this.placedCell));
               }
            }

            void selectSlot(LocalPlayer player, int slot) {
               if (player.getInventory().getSelectedSlot() != slot) {
                  player.getInventory().setSelectedSlot(slot);
                  this.switchedSlot = slot;
               }
            }

            JsonObject statusData() {
               JsonObject out = AiJson.objectOf(
                  "method", this.clutchMethod == null ? requested : this.clutchMethod, "placed", this.clutchPlaced, "damageBefore", this.forecastDamage, "damageAfter", this.damageTaken
               );
               if (this.forecastSupport != null) {
                  out.add("landing", AiJson.objectOf("x", this.forecastSupport.getX(), "y", this.forecastSupport.getY(), "z", this.forecastSupport.getZ()));
               }

               if ("water".equals(this.clutchMethod) && this.clutchPlaced) {
                  out.addProperty("collected", this.waterCollected);
               }

               if (this.write_Z) {
                  out.addProperty("unableToCollectSafely", true);
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.statusData());
            }

            boolean init(LocalPlayer player, Item item) {
               return AiMlg.init(player, item);
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               if (status != AiOperation.Status.SUCCEEDED && status != AiOperation.Status.CANCELLED) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] clutch {}: {} (water buckets {}, hotbar water {})",
                        status,
                        message,
                        AiGameState.countInventoryItem("minecraft:water_bucket"),
                        this.client.player == null ? -1 : AiMlg.hotbarSlotOf(AiMlg.hotbarItemPaths(this.client.player), "water_bucket")
                     );
               }

               return AiGameOperations.result(status, message, this.statusData());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  if (this.client.player != null && this.switchedSlot >= 0 && this.client.player.getInventory().getSelectedSlot() == this.switchedSlot) {
                     this.client.player.getInventory().setSelectedSlot(this.originalSlot);
                  }
               }
            }

            enum Inner_DataUtil {
               GRACE,
               FALLING,
               SETTLE,
               COLLECT;
            }
         };
      } else {
         throw new IllegalArgumentException("MLG clutch requires a connected living player");
      }
   }

   static boolean init(LocalPlayer player, Item item) {
      if (player != null && player.containerMenu == player.inventoryMenu) {
         Inventory inventory = player.getInventory();

         for (int h = 0; h < 9; h++) {
            if (inventory.getItem(h).is(item)) {
               return false;
            }
         }

         int from = -1;

         for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).is(item)) {
               from = i;
               break;
            }
         }

         if (from < 0) {
            return false;
         }

         int to = -1;
         int worst = 2147483647;

         for (int h = 0; h < 9; h++) {
            ItemStack stack = inventory.getItem(h);
            int rank = stack.isEmpty()
               ? 0
               : (
                  !stack.has(DataComponents.WEAPON) && !stack.has(DataComponents.TOOL) && !stack.is(Items.BOW) && !stack.has(DataComponents.FOOD)
                     ? (stack.getItem() instanceof BlockItem ? 1 : 2)
                     : 3
               );
            if (h == inventory.getSelectedSlot()) {
               rank += 4;
            }

            if (rank < worst) {
               worst = rank;
               to = h;
            }
         }

         if (to < 0) {
            return false;
         }

         AiContainerTransactions.sendPredictedClick(player.containerMenu.containerId, from, to, ContainerInput.SWAP);
         return true;
      } else {
         return false;
      }
   }

   static AiMlg.Inner_DataUtil forecastFall(LocalPlayer player, ClientLevel level) {
      Vec3 delta = player.getDeltaMovement();
      double x = player.getX();
      double y = player.getY();
      double z = player.getZ();
      double mx = delta.x;
      double my = delta.y;
      double mz = delta.z;
      double gravity = player.getGravity();
      boolean slowFalling = player.hasEffect(MobEffects.SLOW_FALLING);
      double fall = player.fallDistance;

      for (int tick = 1; tick <= 20; tick++) {
         Vec3 start = new Vec3(x, y, z);
         my = AiDanger.nextMotionY(my, gravity, slowFalling);
         mx *= 0.91;
         mz *= 0.91;
         if (my < 0.0) {
            fall -= my;
         }

         x += mx;
         y += my;
         z += mz;
         AABB swept = player.getDimensions(player.getPose()).makeBoundingBox(start).expandTowards(new Vec3(x, y, z).subtract(start));
         Optional<BlockPos> support = level.findSupportingBlock(player, swept);
         if (support.isPresent()) {
            String id = blockPathAt(level, support.get());
            float multiplier = landingDamageMultiplier(id, player.isSuppressingBounce());
            float damage = multiplier <= 0.0F ? 0.0F : ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(fall, multiplier);
            return new AiMlg.Inner_DataUtil(support.get().immutable(), id, multiplier, damage, tick, false);
         }
      }

      float damage = ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(fall, 1.0F);
      return new AiMlg.Inner_DataUtil(null, "", 1.0F, damage, -1, y < level.getMinY());
   }

   private static int currentPingMillis() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getConnection() != null && mc.player != null) {
         PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
         return info == null ? 0 : Math.max(0, info.getLatency());
      } else {
         return 0;
      }
   }

   static BlockPos chooseRedirectTarget(LocalPlayer player, ClientLevel level, BlockPos naturalFeet, boolean sneaking) {
      List<double[]> targets = new ArrayList<>();
      List<BlockPos> feetCells = new ArrayList<>();

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos cell = naturalFeet.offset(dx, dy, dz);
               String id = blockPathAt(level, cell);
               if (!(landingDamageMultiplier(id, sneaking) > 0.2F)) {
                  BlockPos feet = inBlockLandings.contains(id) ? cell : cell.above();
                  targets.add(new double[]{feet.getX() + 0.5, feet.getZ() + 0.5});
                  feetCells.add(feet);
               }
            }
         }
      }

      int pick = init_I_D(naturalFeet.getX() + 0.5, naturalFeet.getZ() + 0.5, targets);
      return pick < 0 ? null : feetCells.get(pick);
   }

   static String blockPathAt(ClientLevel level, BlockPos pos) {
      return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
   }

   static List<String> hotbarItemPaths(LocalPlayer player) {
      List<String> out = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         out.add(BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(i).getItem()).getPath());
      }

      return out;
   }

   private static List<String> withoutItem(List<String> paths, String itemPath) {
      List<String> out = new ArrayList<>(paths);

      while (out.remove(itemPath)) {
      }

      return out;
   }

   record Inner_DataUtil(BlockPos support, String landingId, float multiplier, float damage, int ticksToLand, boolean voidFall) {
   }
}
