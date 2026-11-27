package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.ai.engine.AiFallGuard;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiEndRun {
   static final int init_I = 24;
   static final float init_F = 16.0F;
   static final double init_D = 26.0;
   static final int load = 17;
   static final int read = 23;

   private AiEndRun() {
   }

   static boolean init_Z(Minecraft mc) {
      AABB box = mc.player.getBoundingBox().deflate(0.001);

      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         if (mc.level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
            return true;
         }
      }

      return false;
   }

   static AiOperation douseFire(Minecraft mc) {
      if (mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES) || AiGameState.countInventoryItem("minecraft:water_bucket") <= 0) {
         return null;
      }

      if (init_Z(mc) && init_BlockPos(mc) != null) {
         return null;
      }

      BlockPos feet = mc.player.blockPosition();
      return AiNetherRun.sequence(
         AiBucket.init(
            AiJson.objectOf("action", "empty", "fluid", "water", "x", feet.getX(), "y", feet.getY(), "z", feet.getZ(), "timeoutSeconds", 5)
         ),
         () -> AiBucket.init(AiJson.objectOf("action", "fill", "fluid", "water", "timeoutSeconds", 5))
      );
   }

   static BlockPos init_BlockPos(Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();
      BlockPos best = null;
      double bestD = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-2, -1, -2), feet.offset(2, 1, 2))) {
         if (p.getX() != feet.getX() || p.getZ() != feet.getZ()) {
            BlockPos below = p.below();
            if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
               boolean clear = true;

               for (int dy = 0; dy < 2; dy++) {
                  BlockState s = mc.level.getBlockState(p.above(dy));
                  if (!s.getCollisionShape(mc.level, p.above(dy)).isEmpty() || s.getBlock() instanceof BaseFireBlock || !s.getFluidState().isEmpty()) {
                     clear = false;
                     break;
                  }
               }

               if (clear) {
                  double d = p.distSqr(feet);
                  if (d < bestD) {
                     bestD = d;
                     best = p.immutable();
                  }
               }
            }
         }
      }

      return best;
   }

   static boolean load_Z(Minecraft mc) {
      if (mc.gameMode == null) {
         return false;
      }

      AABB box = mc.player.getBoundingBox().inflate(0.3);

      for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         if (mc.level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
            mc.gameMode.startDestroyBlock(p.immutable(), Direction.UP);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            return true;
         }
      }

      return false;
   }

   static AiOperation load_AiOperation(Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();
      BlockPos best = null;
      double bestD = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-2, -1, -2), feet.offset(2, 1, 2))) {
         if (p.getX() != feet.getX() || p.getZ() != feet.getZ()) {
            BlockPos below = p.below();
            if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
               boolean clear = true;

               for (int dy = 0; dy < 2; dy++) {
                  BlockState s = mc.level.getBlockState(p.above(dy));
                  if (!s.getCollisionShape(mc.level, p.above(dy)).isEmpty() || s.getBlock() instanceof BaseFireBlock || !s.getFluidState().isEmpty()) {
                     clear = false;
                     break;
                  }
               }

               if (clear) {
                  double d = p.distSqr(feet);
                  if (d < bestD) {
                     bestD = d;
                     best = p.immutable();
                  }
               }
            }
         }
      }

      return best == null ? null : AiGuard.allowDuringLowHealth(AiGoto.travelToCompat(best, 0.45, false, false, false, 5, autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true));
   }

   static AiOperation init(final Minecraft mc, final BlockPos portal) {
      final ResourceKey<Level> from = mc.level == null ? null : mc.level.dimension();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
         int init_I;
         boolean init_Z;
         final String init_Str = AiWorldIdentity.currentWorldKey(mc);
         final Object init_Obj = mc.getConnection();

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (!this.init_Str.equals(AiWorldIdentity.currentWorldKey(mc)) || this.init_Obj != mc.getConnection() || !mc.player.isAlive()) {
                  return this.init(AiOperation.Status.CANCELLED, "Portal traversal player or world changed");
               }

               if (mc.level.dimension() != from || mc.gui.screen() instanceof WinScreen) {
                  boolean expected = from == Level.END
                     ? mc.level.dimension() == Level.OVERWORLD || mc.gui.screen() instanceof WinScreen
                     : mc.level.dimension() == Level.END;
                  return this.init(
                     expected ? AiOperation.Status.SUCCEEDED : AiOperation.Status.UNCERTAIN,
                     expected ? "through the expected End portal" : "Unexpected dimension transition"
                  );
               }

               if (!mc.level.getBlockState(portal).is(Blocks.END_PORTAL)) {
                  return this.init(AiOperation.Status.FAILED, "The expected portal block is no longer active");
               }

               if (mc.player.distanceToSqr(Vec3.atCenterOf(portal)) > 64.0) {
                  return this.init(AiOperation.Status.FAILED, "Direct portal step requires a nearby verified stance");
               }

               if (++this.init_I > 60) {
                  return this.init(AiOperation.Status.FAILED, "did not reach the portal");
               }

               this.init_DataUtil.aimAt(new Vec3(portal.getX() + 0.5, portal.getY(), portal.getZ() + 0.5));
               this.init_DataUtil.tick();
               AiFallGuard.load();
               mc.options.keyUp.setDown(true);
               mc.options.keyJump.setDown(mc.player.onGround() && mc.player.horizontalCollision);
               return AiGameOperations.result(AiOperation.Status.RUNNING, "stepping onto the portal", new JsonObject());
            } else {
               return this.init(AiOperation.Status.CANCELLED, "cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
            this.init_DataUtil.clear();
            return AiGameOperations.result(status, message, new JsonObject());
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            this.init_DataUtil.clear();
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
         }
      };
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(120, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 1800)));
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final String init_Str = AiWorldIdentity.currentWorldKey(mc);
            final Object init_Obj = mc.getConnection();
            final UUID init_UUID = mc.player.getUUID();
            final long load_J = AiServerEvidence.deathRevision();
            final JsonArray init_JsonArray = new JsonArray();
            final AiGuardLayer init_Y = new AiGuardLayer(AiGuard.GuardPolicy.standard().withoutFlee(), "end");
            AiOperation init_AiOperation;
            AiOperation.Result init_AiOperation_Result;
            JsonObject init_JsonObject;
            boolean init_Z;
            String label = "";
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            BlockPos init_BlockPos;
            JsonObject load_JsonObject;
            int init_I;
            int load_I;
            BlockPos load_BlockPos;
            int read_I;
            long read_J;
            long write_J;
            long update;
            BlockPos read_BlockPos;
            final Set<Long> init_Set = new HashSet<>();
            int write_I;
            float init_F = 16.0F;

            AiOperation init(BlockPos portal) {
               return AiEndRun.init(mc, portal);
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null && mc.level != null) {
                  if (!this.init_Str.equals(AiWorldIdentity.currentWorldKey(mc)) || this.init_Obj != mc.getConnection()) {
                     return this.init(AiOperation.Status.CANCELLED, "End run connection changed");
                  }

                  if (!mc.player.getUUID().equals(this.init_UUID)) {
                     return this.init(AiOperation.Status.UNCERTAIN, "Player changed during End traversal");
                  }

                  if (mc.player.isAlive() && this.load_J == AiServerEvidence.deathRevision()) {
                     if (mc.gui.screen() instanceof WinScreen credits) {
                        if (this.load_Z && this.read_Z) {
                           this.write_Z = true;
                           credits.onClose();
                           AiSpeedrun.log("[AI] end: credits closed; respawning in the overworld");
                           return this.init("Past the credits");
                        } else {
                           return this.init(AiOperation.Status.UNCERTAIN, "Credits appeared without a verified central-exit attempt");
                        }
                     } else if (mc.level.dimension() != Level.END) {
                        this.init_V_33();
                        if (!this.load_Z || !this.read_Z || this.init_BlockPos == null || mc.level.dimension() != Level.OVERWORLD) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Left the End without a verified dragon defeat and central exit transition");
                        }

                        if (++this.init_I < 20) {
                           return this.init("Verifying supported Overworld arrival after the central exit");
                        }

                        String hazard = AiPortalLinks.arrivalHazard();
                        AiPortalLinks.init("minecraft:the_end", this.init_BlockPos, "minecraft:overworld", mc.player.blockPosition(), hazard == null, this.load_JsonObject);
                        return this.init(
                           hazard == null ? AiOperation.Status.SUCCEEDED : AiOperation.Status.UNCERTAIN,
                           hazard == null
                              ? "Dragon defeated and central exit to the Overworld verified"
                              : "Central exit reached; arrival requires recovery: " + hazard
                        );
                     } else {
                        if (System.nanoTime() > this.init_J) {
                           return this.init(this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Out of time in the End");
                        }

                        autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(5000000000L);
                        autismclient.ai.engine.AiMotionSafety.setPitchFloor(25.0F, 5000000000L);
                        if (mc.player.onGround() && mc.player.tickCount % 40 == 0) {
                           AiMlg.init(mc.player, Items.WATER_BUCKET);
                        }

                        if (this.load_BlockPos == null) {
                           this.load_BlockPos = this.read();
                        }

                        boolean liveDragon = !AiWorldScan.findEntitiesByType("minecraft:ender_dragon", 256.0).isEmpty();
                        this.load_Z = this.load_Z | (!liveDragon && this.load_BlockPos_7() != null);
                        if (!this.label.startsWith("fight the dragon")) {
                           AiOperation.Result g = this.init_Y
                              .tick(
                                 this.init_AiOperation, this::init_V_33, () -> (int)Math.max(10L, (this.init_J - System.nanoTime()) / 1000000000L)
                              );
                           if (g != null) {
                              return g.terminal() ? this.init(g.status(), g.message(), g) : g;
                           }
                        }

                        if (this.init_AiOperation != null) {
                           AiOperation.Result r = this.init_AiOperation.tick();
                           this.init_AiOperation_Result = r;
                           if (!r.terminal()) {
                              return this.init(this.label + ": " + r.message());
                           }

                           r = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.init_AiOperation, r);
                           this.init_AiOperation_Result = r;
                           this.init_AiOperation = null;
                           AiSpeedrun.log("[AI] end step {}: {} - {}", r.status(), this.label, AiSpeedrun.clip(r.message()));
                           this.init_JsonArray
                              .add(AiJson.objectOf("step", this.label, "status", r.status().name(), "message", AiSpeedrun.clip(r.message())));
                           if (r.status() == AiOperation.Status.CANCELLED) {
                              return this.init(AiOperation.Status.CANCELLED, "Cancelled at " + this.label, r);
                           }

                           if (r.status() == AiOperation.Status.UNCERTAIN) {
                              return this.init(AiOperation.Status.UNCERTAIN, this.label + ": " + r.message(), r);
                           }

                           if (this.label.startsWith("fight the dragon") && r.status() == AiOperation.Status.SUCCEEDED) {
                              this.load_Z = this.load_Z | AiJson.getBoolean(AiRunResources.parseEvidenceJson(r.dataJson()), "dragonDead", false);
                           }

                           if (this.label.equals("heal in a hole") && r.status() != AiOperation.Status.SUCCEEDED && this.read_BlockPos != null) {
                              this.init_Set.add(this.read_BlockPos.below().asLong());
                              this.read_BlockPos = null;
                              this.read_J = 0L;
                           }

                           if (this.label.startsWith("fight the dragon") && r.failure() == AiFailure.LOW_HEALTH) {
                              this.init_F = Math.min(20.0F, mc.player.getMaxHealth()) - 0.01F;
                           }

                           if (this.label.startsWith("fight the dragon")
                              && r.status() != AiOperation.Status.SUCCEEDED
                              && r.failure() != AiFailure.LOW_HEALTH
                              && ++this.read_I >= 3
                              && !this.load_Z) {
                              return this.init(AiOperation.Status.FAILED, "The dragon fight failed " + this.read_I + " times: " + AiSpeedrun.clip(r.message()));
                           }
                        }

                        if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
                           return this.init("Landing");
                        }

                        if ((mc.player.isOnFire() || AiEndRun.init_Z(mc)) && !mc.player.isInWater()) {
                           AiOperation out = AiEndRun.douseFire(mc);
                           if (out == null && AiEndRun.init_Z(mc)) {
                              out = AiEndRun.load_AiOperation(mc);
                           }

                           if (out != null) {
                              return this.init("put the fire out", out);
                           }
                        }

                        return this.load_AiOperation_Result_20();
                     }
                  } else {
                     return this.init(AiOperation.Status.FAILED, "Player died in the End; no completion credited");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            AiOperation.Result init_AiOperation_Result_43() {
               if (mc.player.getHealth() + mc.player.getAbsorptionAmount() >= this.init_F) {
                  this.init_F = 16.0F;
                  this.read_BlockPos = null;
                  return null;
               }

               if (this.read_BlockPos == null) {
                  this.read_BlockPos = this.init_BlockPos_20();
               }

               if (mc.player.getHealth() <= 6.0F && System.nanoTime() > this.read_J && System.nanoTime() > this.update) {
                  this.update = System.nanoTime() + 15000000000L;
                  return this.init(
                     "heal in a hole here",
                     AiBurrow.init(AiJson.objectOf("healTo", Math.max(18.0F, this.init_F), "stay", false, "towerOk", false, "timeoutSeconds", 120))
                  );
               }

               if (this.read_BlockPos != null
                  && Math.hypot(mc.player.getX() - (this.read_BlockPos.getX() + 0.5), mc.player.getZ() - (this.read_BlockPos.getZ() + 0.5)) > 1.2) {
                  if (System.nanoTime() < this.write_J) {
                     return this.init(
                        "wait to walk to the heal spot (health " + String.format("%.0f", mc.player.getHealth()) + ")",
                        AiNetherRun.delayThen(20, AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "waited", new JsonObject()))
                     );
                  }

                  this.write_J = System.nanoTime() + 3000000000L;
                  return this.init(
                     "to the heal spot at " + this.read_BlockPos.toShortString(),
                     AiGuard.allowDuringLowHealth(AiGoto.travelToCompat(this.read_BlockPos, 0.6, true, true, true, 40, autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST, true))
                  );
               } else if (mc.player.getFoodData().getFoodLevel() < 20 && AiSurvival.init(mc.player.getInventory()) >= 0) {
                  return this.init("eat before the fight", AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 20)));
               } else if (System.nanoTime() > this.read_J) {
                  this.read_J = System.nanoTime() + 20000000000L;
                  return this.init(
                     "heal in a hole",
                     AiBurrow.init(AiJson.objectOf("healTo", Math.max(18.0F, this.init_F), "stay", false, "towerOk", false, "timeoutSeconds", 120))
                  );
               } else {
                  return this.init(
                     "rest to heal (health " + String.format("%.0f", mc.player.getHealth()) + ")",
                     AiNetherRun.delayThen(100, AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "rested", new JsonObject()))
                  );
               }
            }

            BlockPos init_BlockPos_20() {
               double px = mc.player.getX();
               double pz = mc.player.getZ();
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (int r = 17; r <= 23; r += 3) {
                  for (int k = 0; k < 24; k++) {
                     double a = 6.283185307179586 * k / 24.0;
                     int x = (this.load_BlockPos == null ? 0 : this.load_BlockPos.getX()) + (int)Math.floor(Math.cos(a) * r);
                     int z = (this.load_BlockPos == null ? 0 : this.load_BlockPos.getZ()) + (int)Math.floor(Math.sin(a) * r);
                     BlockPos top = this.init(x, z);
                     if (top != null && !this.init_Set.contains(top.asLong()) && mc.level.getBlockState(top).is(Blocks.END_STONE)) {
                        boolean solid = true;

                        for (int d = 1; d <= 4 && solid; d++) {
                           if (mc.level.getBlockState(top.below(d)).getCollisionShape(mc.level, top.below(d)).isEmpty()) {
                              solid = false;
                           }
                        }

                        for (int dx = -3; dx <= 3 && solid; dx++) {
                           for (int dz = -3; dz <= 3 && solid; dz++) {
                              BlockPos t = this.init(x + dx, z + dz);
                              if (t == null || Math.abs(t.getY() - top.getY()) > 2) {
                                 solid = false;
                              }
                           }
                        }

                        if (solid) {
                           double d = Math.hypot(px - (x + 0.5), pz - (z + 0.5));
                           if (d < bestD) {
                              bestD = d;
                              best = top;
                           }
                        }
                     }
                  }
               }

               return best == null ? null : best.above();
            }

            BlockPos init(int x, int z) {
               BlockPos probe = new BlockPos(x, 64, z);
               if (!AiTerrain.isLoaded(mc.level, probe)) {
                  return null;
               }

               int y = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
               return y <= mc.level.getMinY() ? null : new BlockPos(x, y, z);
            }

            AiOperation.Result load_AiOperation_Result_20() {
               BlockPos portal = this.load_BlockPos_7();
               if (this.load_Z && portal != null) {
                  this.init_BlockPos = portal.immutable();
                  this.load_JsonObject = AiJson.objectOf(
                     "kind",
                     "central_end_exit",
                     "dimension",
                     "minecraft:the_end",
                     "world",
                     this.init_Str,
                     "entrance",
                     portal.toShortString(),
                     "fountain",
                     this.load_BlockPos.toShortString(),
                     "active",
                     true,
                     "observedTick",
                     AiThreats.currentClientTick()
                  );
               }

               if (this.load_Z && portal != null && this.write_I >= 1) {
                  this.read_Z = true;
                  this.write_I++;
                  return this.init("onto the exit portal at " + portal.toShortString(), this.init(portal));
               }

               if (this.load_Z && portal != null) {
                  this.read_Z = true;
                  this.write_I++;
                  return this.init(
                     "into the exit portal at " + portal.toShortString(),
                     AiGoto.travelToCompat(
                        portal,
                        0.6,
                        true,
                        true,
                        true,
                        120,
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                        true,
                        2.0,
                        null,
                        AiGoto.GotoOptions.DEFAULT
                     )
                  );
               }

               if (this.load_Z) {
                  return this.init("Waiting for the exit portal to open");
               }

               if (AiRunResources.isCampaignActive()) {
                  Map<String, Integer> missing = AiRunResources.missingMandatoryGear();
                  if (!missing.isEmpty()) {
                     return this.init(AiOperation.Status.FAILED, "Required campaign equipment is unavailable in the End: " + missing);
                  }

                  if (!AiRunResources.isMandatoryArmorEquipped()) {
                     if (this.load_I++ >= 2) {
                        return this.init(AiOperation.Status.FAILED, "Required End armor could not be equipped; readiness remains unresolved");
                     }

                     return this.init("equip the required End armor", AiEquip.init(AiJson.objectOf("what", "armor")));
                  }
               }

               int fx = this.load_BlockPos == null ? 0 : this.load_BlockPos.getX();
               int fz = this.load_BlockPos == null ? 0 : this.load_BlockPos.getZ();
               double fromCentre = Math.hypot(mc.player.getX() - fx, mc.player.getZ() - fz);
               if (fromCentre > 32.0) {
                  return this.init(
                     "cross to the main island",
                     AiGoto.travelToCompat(
                        new BlockPos(fx, (int)mc.player.getY(), fz),
                        1.0,
                        true,
                        true,
                        true,
                        300,
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                        true,
                        2.0,
                        new AiNetherRun.Inner_NetHelper(fx, fz, 24),
                        AiGoto.GotoOptions.DEFAULT
                     )
                  );
               }

               AiOperation.Result healed = this.init_AiOperation_Result_43();
               if (healed != null) {
                  return healed;
               }

               int beds = AiItems.countInInventory(AiItems.resolve("bed"));
               return this.init(
                  "fight the dragon",
                  AiDragon.create(
                     AiJson.objectOf(
                        "beds",
                        beds,
                        "melee",
                        true,
                        "bow",
                        true,
                        "timeoutSeconds",
                        (int)Math.max(60L, Math.min(1500L, (this.init_J - System.nanoTime()) / 1000000000L))
                     )
                  )
               );
            }

            BlockPos load_BlockPos_7() {
               if (this.load_BlockPos == null) {
                  return null;
               }

               for (BlockPos p : BlockPos.betweenClosed(this.load_BlockPos.offset(-4, -4, -4), this.load_BlockPos.offset(4, 3, 4))) {
                  if (mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                     int active = 0;

                     for (BlockPos cell : BlockPos.betweenClosed(p.offset(-3, 0, -3), p.offset(3, 0, 3))) {
                        if (mc.level.getBlockState(cell).is(Blocks.END_PORTAL)) {
                           active++;
                        }
                     }

                     if (active >= 9) {
                        return p.immutable();
                     }
                  }
               }

               return null;
            }

            BlockPos read() {
               BlockPos rock = AiWorldScan.findNearestBlock(s -> s.is(Blocks.BEDROCK), 12);
               if (rock != null && !(Math.hypot(rock.getX(), rock.getZ()) > 32.0)) {
                  for (BlockPos p : BlockPos.betweenClosed(rock.offset(-4, -5, -4), rock.offset(4, 5, 4))) {
                     if (mc.level.getBlockState(p).is(Blocks.BEDROCK)
                        && mc.level.getBlockState(p.above()).is(Blocks.BEDROCK)
                        && mc.level.getBlockState(p.above(2)).is(Blocks.BEDROCK)) {
                        return p.immutable();
                     }
                  }

                  return null;
               } else {
                  return null;
               }
            }

            AiOperation.Result init(String label, AiOperation op) {
               this.label = label;
               this.init_AiOperation_Result = null;
               AiSpeedrun.log("[AI] end: {}", label);
               this.init_AiOperation = AiRunDiagnostics.trace("end", label, op);
               return this.init(label);
            }

            JsonObject init_JsonObject_61() {
               return AiJson.objectOf(
                  "dragonDead",
                  this.load_Z,
                  "centralExitAttempted",
                  this.read_Z,
                  "creditsSeen",
                  this.write_Z,
                  "fountain",
                  this.load_BlockPos == null ? "" : this.load_BlockPos.toShortString(),
                  "fights",
                  this.read_I,
                  "steps",
                  this.init_JsonArray,
                  "pendingChild",
                  this.init_JsonObject == null ? new JsonObject() : this.init_JsonObject
               );
            }

            AiOperation.Result init(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_61());
            }

            void init_V_33() {
               if (this.init_AiOperation != null) {
                  boolean pending = this.init_AiOperation.requiresReconciliationAfterEmergency();
                  this.init_Z |= pending;
                  if (pending && this.init_AiOperation_Result != null) {
                     this.init_JsonObject = AiRunResources.compactEvidenceJson(this.init_AiOperation_Result.dataJson());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, this.init_AiOperation_Result);
            }

            AiOperation.Result init(AiOperation.Status status, String message, AiOperation.Result child) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, this.init_JsonObject_61(), pending, child);
               this.init_Z = this.init_Z | (pending || AiJson.getBoolean(AiRunResources.parseEvidenceJson(result.dataJson()), "reobserveBeforeResuming", false));
               this.init_V_33();
               this.init_Y.cancel();
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(0L);
               return result;
            }

            @Override
            public boolean canInterruptNow() {
               return !this.init_Z && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow()) && this.init_Y.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_Z || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency() || this.init_Y.requiresReconciliation();
            }

            @Override
            public void cancel() {
               this.init_Z = this.init_Z | this.requiresReconciliationAfterEmergency();
               this.init_V_33();
               this.init_Y.cancel();
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(0L);
            }
         };
      } else {
         throw new IllegalArgumentException("The End run needs a connected living player");
      }
   }
}
