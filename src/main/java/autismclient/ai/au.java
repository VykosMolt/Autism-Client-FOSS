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

final class au {
   static final int init_I = 3;
   static final int load = 4;
   static final double init_D = 2.0;
   static final int read = 90;
   static final int write = 5;
   static final int update = 3;
   static final int sync = 20;
   static final int flush = 60;
   static final float init_F = 0.5F;
   private static final Set<String> init_Set = Set.of("water", "cobweb", "powder_snow");

   private au() {
   }

   static String init_Str_Str(String requested, List<String> hotbarItemPaths, boolean redirectAvailable) {
      String var10000;
      switch (requested) {
         case "water":
            var10000 = init_I(hotbarItemPaths, "water_bucket") >= 0 ? "water" : null;
            break;
         case "cobweb":
            var10000 = init_I(hotbarItemPaths, "cobweb") >= 0 ? "cobweb" : null;
            break;
         default:
            var10000 = init_I(hotbarItemPaths, "water_bucket") >= 0
               ? "water"
               : (init_I(hotbarItemPaths, "cobweb") >= 0 ? "cobweb" : (redirectAvailable ? "redirect" : null));
      }

      return var10000;
   }

   static int init_I(List<String> hotbarItemPaths, String itemPath) {
      for (int i = 0; i < hotbarItemPaths.size(); i++) {
         if (itemPath.equals(hotbarItemPaths.get(i))) {
            return i;
         }
      }

      return -1;
   }

   static int init_I_Str(String method) {
      return init_I(method, 0);
   }

   static int init_I(String method, int pingMillis) {
      int var10000;
      switch (method) {
         case "water":
            var10000 = init_I_I(pingMillis);
            break;
         case "cobweb":
            var10000 = 4;
            break;
         default:
            var10000 = 0;
      }

      return var10000;
   }

   static int init_I_I(int pingMillis) {
      return 3 + Math.min(5, Math.max(0, pingMillis) / 100);
   }

   static boolean init_Z(String method, int ticksToLand) {
      return init_Z_Str(method, ticksToLand, 0);
   }

   static boolean init_Z_Str(String method, int ticksToLand, int pingMillis) {
      int window = init_I(method, pingMillis);
      return window > 0 && ticksToLand >= 1 && ticksToLand <= window;
   }

   static float init(String landingBlockId, boolean sneaking) {
      return i.init_F_Str_Z(landingBlockId, sneaking);
   }

   static boolean init_Z_F(float multiplier) {
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

   static boolean init(boolean eyeSubmerged, int airSupply) {
      return !eyeSubmerged || airSupply >= 90;
   }

   static AiOperation init_AiOperation_JsonObject(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final String requested = ag.init_Str_JsonObject(args, "method", "auto").toLowerCase(Locale.ROOT);
         if (!requested.equals("water") && !requested.equals("cobweb") && !requested.equals("auto")) {
            throw new IllegalArgumentException("method must be water, cobweb or auto");
         }

         final boolean collect = ag.init_Z_JsonObject(args, "collect", true);
         final long deadline = System.nanoTime() + Math.max(1, Math.min(30, ag.init_I_JsonObject(args, "timeoutSeconds", 10))) * 1000000000L;
         final boolean waterUsable = !client.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES);
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final Object init_Obj = this.init_Minecraft.level;
            final Object load_Obj = this.init_Minecraft.getConnection();
            final UUID init_UUID = this.init_Minecraft.player.getUUID();
            final int init_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
            final int load_I = au.init();
            Inner_DataUtil init_au_1_DataUtil = Inner_DataUtil.init;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            String init_Str;
            int read_I;
            int write_I;
            int update_I;
            int sync;
            int flush = -1;
            double init_D;
            float init_F;
            float load_F;
            float read_F;
            float write_F;
            BlockPos init_BlockPos;
            BlockPos load_BlockPos;
            BlockPos read_BlockPos;
            au.Inner_DataUtil init_au_DataUtil;
            int close;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (this.init_Minecraft.level != this.init_Obj
                     || this.init_Minecraft.getConnection() != this.load_Obj
                     || !this.init_UUID.equals(this.init_Minecraft.player.getUUID())
                     || !this.init_Minecraft.player.isAlive()) {
                     return this.init(AiOperation.Status.CANCELLED, "Clutch player, world or connection changed");
                  }

                  if ("water".equals(this.init_Str)
                     && this.init_Minecraft.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                     return this.init(AiOperation.Status.FAILED, "Water clutch prohibited by the current dimension attributes");
                  }

                  if (System.nanoTime() > deadline) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Clutch window passed before a safe landing");
                  }

                  LocalPlayer player = this.init_Minecraft.player;
                  ClientLevel level = this.init_Minecraft.level;
                  AiOperation.Result var10000;
                  switch (this.init_au_1_DataUtil) {
                     case init:
                        var10000 = this.init(player);
                        break;
                     case load:
                        var10000 = this.init_AiOperation_Result(player, level);
                        break;
                     case read:
                        var10000 = this.load(player, level);
                        break;
                     case write:
                        var10000 = this.read(player, level);
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "MLG clutch cancelled");
               }
            }

            AiOperation.Result init(LocalPlayer player) {
               Module noFall = autismclient.modules.al.init_Module_Str("no-fall");
               if (noFall != null && noFall.isEnabled()) {
                  return this.init(AiOperation.Status.SUCCEEDED, "No-fall module is active; no clutch needed");
               } else {
                  boolean airborne = !player.onGround() || player.fallDistance > 0.5 || player.getDeltaMovement().y < -0.08;
                  if (airborne) {
                     this.init_au_1_DataUtil = Inner_DataUtil.load;
                     return this.init("Fall detected; tracking the landing");
                  } else {
                     return ++this.read_I > 20 ? this.init(AiOperation.Status.FAILED, "No fall in progress") : this.init("Waiting for the fall to begin");
                  }
               }
            }

            AiOperation.Result init_AiOperation_Result(LocalPlayer player, ClientLevel level) {
               this.init_D = Math.max(this.init_D, player.fallDistance);
               this.init_F = player.getHealth();
               if (this.init_Z(player, level)) {
                  this.init_au_1_DataUtil = Inner_DataUtil.read;
                  this.write_I = 0;
                  this.load_F = player.getHealth();
                  this.read_BlockPos = player.blockPosition();
                  return this.init("Landed; measuring the applied fall damage");
               }

               au.Inner_DataUtil forecast = au.init(player, level);
               this.init_au_DataUtil = forecast;
               if (forecast.ticksToLand() >= 0) {
                  if (au.init_Z_F(forecast.multiplier())) {
                     this.read_F = this.write_F = forecast.damage();
                     this.read_BlockPos = forecast.support();
                     return this.init(AiOperation.Status.SUCCEEDED, "Natural soft landing");
                  }

                  if (forecast.damage() <= 0.5F) {
                     this.read_F = this.write_F = forecast.damage();
                     this.read_BlockPos = forecast.support();
                     return this.init(AiOperation.Status.SUCCEEDED, "Fall predicted harmless; no clutch needed");
                  }

                  if (this.init_Str == null) {
                     AiOperation.Result failure = this.init(player, level, forecast);
                     if (failure != null) {
                        return failure;
                     }
                  }

                  if ("redirect".equals(this.init_Str)) {
                     BlockPos target = au.init(player, level, forecast.support().above(), player.isSuppressingBounce());
                     if (target != null) {
                        this.load_BlockPos = target;
                     }

                     if (this.load_BlockPos != null) {
                        Vec3 d = Vec3.atBottomCenterOf(this.load_BlockPos).subtract(player.position());
                        player.setYRot((float)(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0));
                        this.init_Minecraft.options.keyUp.setDown(true);
                     }

                     return this.init("Steering toward a soft landing");
                  } else {
                     if (!this.load_Z) {
                        this.load_Z = this.init(level);
                        if (!this.load_Z && au.init_Z_Str(this.init_Str, forecast.ticksToLand(), this.load_I)) {
                           AiOperation.Result failure = this.load(player, level, forecast);
                           if (failure != null) {
                              return failure;
                           }
                        }
                     }

                     return this.init(
                        this.load_Z
                           ? "Clutch placed; riding the fall in"
                           : "Falling; the placement window opens at " + au.init_I(this.init_Str, this.load_I) + " ticks before touchdown"
                     );
                  }
               } else {
                  return !forecast.voidFall() && !(player.getY() < level.getMinY())
                     ? this.init("Falling; the landing is not inside the prediction horizon yet")
                     : this.init(AiOperation.Status.FAILED, "The fall crosses below the void floor (y=" + level.getMinY() + "); there is no landing to clutch");
               }
            }

            AiOperation.Result init(LocalPlayer player, ClientLevel level, au.Inner_DataUtil forecast) {
               List<String> hotbar = au.init_List_LocalPlayer(player);
               if (waterUsable
                  && !"cobweb".equals(requested)
                  && au.init_I(hotbar, "water_bucket") < 0
                  && this.close < 3
                  && this.init(player, Items.WATER_BUCKET)) {
                  this.close++;
                  return this.init("Moving the water bucket to the hotbar");
               }

               if ("water".equals(requested)) {
                  if (!waterUsable) {
                     return this.init(AiOperation.Status.FAILED, "Water evaporates in this dimension; use a cobweb");
                  }

                  if (au.init_I(hotbar, "water_bucket") < 0) {
                     return this.init(AiOperation.Status.FAILED, "No water bucket in hotbar");
                  }

                  this.init_Str = "water";
                  return null;
               } else if ("cobweb".equals(requested)) {
                  if (au.init_I(hotbar, "cobweb") < 0) {
                     return this.init(AiOperation.Status.FAILED, "No cobweb in hotbar");
                  }

                  this.init_Str = "cobweb";
                  return null;
               } else {
                  this.load_BlockPos = au.init(player, level, forecast.support().above(), player.isSuppressingBounce());
                  this.init_Str = au.init_Str_Str("auto", waterUsable ? hotbar : au.init_List(hotbar, "water_bucket"), this.load_BlockPos != null);
                  return this.init_Str == null ? this.init(AiOperation.Status.FAILED, "No clutch item in hotbar") : null;
               }
            }

            AiOperation.Result load(LocalPlayer player, ClientLevel level, au.Inner_DataUtil forecast) {
               List<String> hotbar = au.init_List_LocalPlayer(player);
               String item = "water".equals(this.init_Str) ? "water_bucket" : "cobweb";
               int slot = au.init_I(hotbar, item);
               if (slot < 0) {
                  return this.init(AiOperation.Status.FAILED, "Clutch item disappeared from the hotbar");
               }

               this.init(player, slot);
               BlockPos cell = forecast.support().above();
               this.init_BlockPos = cell;
               if ("water".equals(this.init_Str)) {
                  R.init(Vec3.atCenterOf(cell.below()).add(0.0, 0.5, 0.0));
                  this.init_Minecraft.gameMode.useItem(player, InteractionHand.MAIN_HAND);
               } else {
                  ItemStack stack = player.getInventory().getItem(slot);
                  BlockPlacementPlanner.Inner_StateCache request = new BlockPlacementPlanner.Inner_StateCache(
                        cell, player.getEyePosition(), Math.max(4.5, player.blockInteractionRange()), BlockPlacementPlanner.init(stack, player, InteractionHand.MAIN_HAND)
                     )
                     .init_U_StateCache_Z(player.isShiftKeyDown())
                     .init_U_StateCache_U_StreamUtil(new BlockPlacementPlanner.Inner_StreamUtil(6));
                  BlockPlacementPlanner.Inner_CachePool candidate = BlockPlacementPlanner.init_U_CachePool(request);
                  if (candidate != null) {
                     player.setYRot(candidate.init_U_DataUtil().load().init());
                     player.setXRot(candidate.init_U_DataUtil().load().load());
                     this.init_Minecraft.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, candidate.init_BlockHitResult());
                  } else {
                     Vec3 hitPos = Vec3.atCenterOf(forecast.support()).add(0.0, 0.5, 0.0);
                     R.init(hitPos);
                     this.init_Minecraft
                        .gameMode
                        .useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(hitPos, Direction.UP, forecast.support(), false));
                  }
               }

               return null;
            }

            AiOperation.Result load(LocalPlayer player, ClientLevel level) {
               this.load_F = Math.min(this.load_F, player.getHealth());
               if (++this.write_I < 5) {
                  return this.init("Landed; measuring the applied fall damage");
               }

               this.write_F = Math.max(0.0F, this.init_F - this.load_F);
               this.read_F = this.init_au_DataUtil == null ? 0.0F : this.init_au_DataUtil.damage();
               boolean softActual = player.isInWater()
                  || "cobweb".equals(au.init(level, player.blockPosition()))
                  || au.init(au.init(level, player.blockPosition().below()), player.isSuppressingBounce()) <= 0.2F;
               if (!(this.write_F <= 0.5F) || !this.load_Z && !softActual && !"redirect".equals(this.init_Str)) {
                  if ("water".equals(this.init_Str) && this.load_Z && collect && player.isAlive()) {
                     this.update_Z = true;
                     this.init_au_1_DataUtil = Inner_DataUtil.write;
                     return this.init("Clutch landed late; collecting the water");
                  } else {
                     return !this.load_Z && !"redirect".equals(this.init_Str)
                        ? this.init(AiOperation.Status.FAILED, "Clutch missed")
                        : this.init(
                           AiOperation.Status.FAILED,
                           "redirect".equals(this.init_Str) ? "Redirect missed the soft landing" : "Clutch placed too late; fall damage was still applied"
                        );
                  }
               } else if ("water".equals(this.init_Str) && this.load_Z && collect) {
                  this.init_au_1_DataUtil = Inner_DataUtil.write;
                  return this.init("Clutch landed; collecting the water");
               } else {
                  return this.init(AiOperation.Status.SUCCEEDED, this.init_Str_14());
               }
            }

            String init_Str_14() {
               if (this.load_Z) {
                  return "water".equals(this.init_Str) ? "Water clutch landed" : "Cobweb clutch landed";
               } else {
                  return "redirect".equals(this.init_Str) ? "Redirected to a soft landing" : "Landed on a naturally soft block";
               }
            }

            AiOperation.Result read(LocalPlayer player, ClientLevel level) {
               BlockPos cell = this.init_BlockPos(player, level);
               boolean sourceGone = cell != null && !level.getFluidState(cell).is(FluidTags.WATER);
               boolean bucketFilled = "water_bucket"
                  .equals(BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(player.getInventory().getSelectedSlot()).getItem()).getPath());
               if (this.update_I <= 0 || !sourceGone && !bucketFilled) {
                  if (!au.init(player.isUnderWater(), player.getAirSupply())) {
                     if (++this.sync > 60) {
                        this.write_Z = true;
                        return this.init(this.update_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED, "Water clutch landed; water left in place");
                     } else {
                        this.init_Minecraft.options.keyJump.setDown(true);
                        return this.init("Surfacing before collecting the water");
                     }
                  } else if (cell != null && this.update_I < 3) {
                     int slot = au.init_I(au.init_List_LocalPlayer(player), "bucket");
                     if (slot < 0) {
                        return this.init(
                           this.update_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED, "Water clutch landed; no empty bucket for collection"
                        );
                     }

                     this.init(player, slot);
                     R.init(Vec3.atCenterOf(cell));
                     this.init_Minecraft.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                     this.update_I++;
                     return this.init("Collecting the placed water");
                  } else {
                     return this.init(this.update_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED, "Water clutch landed; water left in place");
                  }
               } else {
                  this.read_Z = true;
                  return this.update_Z
                     ? this.init(AiOperation.Status.FAILED, "Clutch placed too late; fall damage was still applied (water collected)")
                     : this.init(AiOperation.Status.SUCCEEDED, "Water clutch landed; water collected");
               }
            }

            BlockPos init_BlockPos(LocalPlayer player, ClientLevel level) {
               if (this.init_BlockPos != null && level.getFluidState(this.init_BlockPos).is(FluidTags.WATER)) {
                  return this.init_BlockPos;
               }

               BlockPos feet = player.blockPosition();
               return level.getFluidState(feet).is(FluidTags.WATER) ? feet : this.init_BlockPos;
            }

            boolean init_Z(LocalPlayer player, ClientLevel level) {
               return player.onGround()
                  || player.isInWater()
                  || "cobweb".equals(au.init(level, player.blockPosition()))
                  || this.init_D >= 1.0 && player.fallDistance <= 0.5;
            }

            boolean init(ClientLevel level) {
               if (this.init_BlockPos == null) {
                  return false;
               } else {
                  return "water".equals(this.init_Str)
                     ? level.getFluidState(this.init_BlockPos).is(FluidTags.WATER)
                     : "cobweb".equals(au.init(level, this.init_BlockPos));
               }
            }

            void init(LocalPlayer player, int slot) {
               if (player.getInventory().getSelectedSlot() != slot) {
                  player.getInventory().setSelectedSlot(slot);
                  this.flush = slot;
               }
            }

            JsonObject init_JsonObject_37() {
               JsonObject out = ag.init_JsonObject_ObjArr(
                  "method", this.init_Str == null ? requested : this.init_Str, "placed", this.load_Z, "damageBefore", this.read_F, "damageAfter", this.write_F
               );
               if (this.read_BlockPos != null) {
                  out.add("landing", ag.init_JsonObject_ObjArr("x", this.read_BlockPos.getX(), "y", this.read_BlockPos.getY(), "z", this.read_BlockPos.getZ()));
               }

               if ("water".equals(this.init_Str) && this.load_Z) {
                  out.addProperty("collected", this.read_Z);
               }

               if (this.write_Z) {
                  out.addProperty("unableToCollectSafely", true);
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_37());
            }

            boolean init(LocalPlayer player, Item item) {
               return au.init(player, item);
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               if (status != AiOperation.Status.SUCCEEDED && status != AiOperation.Status.CANCELLED) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] clutch {}: {} (water buckets {}, hotbar water {})",
                        status,
                        message,
                        S.init_I_Str("minecraft:water_bucket"),
                        this.init_Minecraft.player == null ? -1 : au.init_I(au.init_List_LocalPlayer(this.init_Minecraft.player), "water_bucket")
                     );
               }

               return R.init_AiOperation_Result(status, message, this.init_JsonObject_37());
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  autismclient.ai.a.AiMotionSafety.putAt_V();
                  if (this.init_Minecraft.player != null && this.flush >= 0 && this.init_Minecraft.player.getInventory().getSelectedSlot() == this.flush) {
                     this.init_Minecraft.player.getInventory().setSelectedSlot(this.init_I);
                  }
               }
            }

            enum Inner_DataUtil {
               init,
               load,
               read,
               write;
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

         PkgCls_b.init(player.containerMenu.containerId, from, to, ContainerInput.SWAP);
         return true;
      } else {
         return false;
      }
   }

   static au.Inner_DataUtil init(LocalPlayer player, ClientLevel level) {
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
         my = i.init_D_D(my, gravity, slowFalling);
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
            String id = init(level, support.get());
            float multiplier = init(id, player.isSuppressingBounce());
            float damage = multiplier <= 0.0F ? 0.0F : ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(fall, multiplier);
            return new au.Inner_DataUtil(support.get().immutable(), id, multiplier, damage, tick, false);
         }
      }

      float damage = ((AutismLivingEntityAccessor)player).autism$calculateFallDamage(fall, 1.0F);
      return new au.Inner_DataUtil(null, "", 1.0F, damage, -1, y < level.getMinY());
   }

   private static int init() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.getConnection() != null && mc.player != null) {
         PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
         return info == null ? 0 : Math.max(0, info.getLatency());
      } else {
         return 0;
      }
   }

   static BlockPos init(LocalPlayer player, ClientLevel level, BlockPos naturalFeet, boolean sneaking) {
      List<double[]> targets = new ArrayList<>();
      List<BlockPos> feetCells = new ArrayList<>();

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               BlockPos cell = naturalFeet.offset(dx, dy, dz);
               String id = init(level, cell);
               if (!(init(id, sneaking) > 0.2F)) {
                  BlockPos feet = init_Set.contains(id) ? cell : cell.above();
                  targets.add(new double[]{feet.getX() + 0.5, feet.getZ() + 0.5});
                  feetCells.add(feet);
               }
            }
         }
      }

      int pick = init_I_D(naturalFeet.getX() + 0.5, naturalFeet.getZ() + 0.5, targets);
      return pick < 0 ? null : feetCells.get(pick);
   }

   static String init(ClientLevel level, BlockPos pos) {
      return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
   }

   static List<String> init_List_LocalPlayer(LocalPlayer player) {
      List<String> out = new ArrayList<>(9);

      for (int i = 0; i < 9; i++) {
         out.add(BuiltInRegistries.ITEM.getKey(player.getInventory().getItem(i).getItem()).getPath());
      }

      return out;
   }

   private static List<String> init_List(List<String> paths, String itemPath) {
      List<String> out = new ArrayList<>(paths);

      while (out.remove(itemPath)) {
      }

      return out;
   }

   record Inner_DataUtil(BlockPos support, String landingId, float multiplier, float damage, int ticksToLand, boolean voidFall) {
   }
}
