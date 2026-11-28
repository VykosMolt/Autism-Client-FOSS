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

   static boolean isInFire(Minecraft mc) {
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

      if (isInFire(mc) && fireEscapeCell(mc) != null) {
         return null;
      }

      BlockPos feet = mc.player.blockPosition();
      return AiNetherRun.sequence(
         AiBucket.create(
            AiJson.objectOf("action", "empty", "fluid", "water", "x", feet.getX(), "y", feet.getY(), "z", feet.getZ(), "timeoutSeconds", 5)
         ),
         () -> AiBucket.create(AiJson.objectOf("action", "fill", "fluid", "water", "timeoutSeconds", 5))
      );
   }

   static BlockPos fireEscapeCell(Minecraft mc) {
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

   static boolean extinguishFire(Minecraft mc) {
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

   static AiOperation escapeFireOp(Minecraft mc) {
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

   static AiOperation enterPortal(final Minecraft mc, final BlockPos portal) {
      final ResourceKey<Level> from = mc.level == null ? null : mc.level.dimension();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         int ticks;
         boolean cancelled;
         final String worldKey = AiWorldIdentity.currentWorldKey(mc);
         final Object connection = mc.getConnection();

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (!this.worldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || this.connection != mc.getConnection() || !mc.player.isAlive()) {
                  return this.finish(AiOperation.Status.CANCELLED, "Portal traversal player or world changed");
               }

               if (mc.level.dimension() != from || mc.gui.screen() instanceof WinScreen) {
                  boolean expected = from == Level.END
                     ? mc.level.dimension() == Level.OVERWORLD || mc.gui.screen() instanceof WinScreen
                     : mc.level.dimension() == Level.END;
                  return this.finish(
                     expected ? AiOperation.Status.SUCCEEDED : AiOperation.Status.UNCERTAIN,
                     expected ? "through the expected End portal" : "Unexpected dimension transition"
                  );
               }

               if (!mc.level.getBlockState(portal).is(Blocks.END_PORTAL)) {
                  return this.finish(AiOperation.Status.FAILED, "The expected portal block is no longer active");
               }

               if (mc.player.distanceToSqr(Vec3.atCenterOf(portal)) > 64.0) {
                  return this.finish(AiOperation.Status.FAILED, "Direct portal step requires a nearby verified stance");
               }

               if (++this.ticks > 60) {
                  return this.finish(AiOperation.Status.FAILED, "did not reach the portal");
               }

               this.aim.aimAt(new Vec3(portal.getX() + 0.5, portal.getY(), portal.getZ() + 0.5));
               this.aim.tick();
               AiFallGuard.stampTick();
               mc.options.keyUp.setDown(true);
               mc.options.keyJump.setDown(mc.player.onGround() && mc.player.horizontalCollision);
               return AiGameOperations.result(AiOperation.Status.RUNNING, "stepping onto the portal", new JsonObject());
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "cancelled");
            }
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
            this.aim.clear();
            return AiGameOperations.result(status, message, new JsonObject());
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            this.aim.clear();
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
         }
      };
   }

   static AiOperation endRun(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(120, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 1800)));
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final String worldKey = AiWorldIdentity.currentWorldKey(mc);
            final Object connection = mc.getConnection();
            final UUID playerId = mc.player.getUUID();
            final long deathRevisionAtStart = AiServerEvidence.deathRevision();
            final JsonArray steps = new JsonArray();
            final AiGuardLayer guard = new AiGuardLayer(AiGuard.GuardPolicy.standard().withoutFlee(), "end");
            AiOperation child;
            AiOperation.Result lastChildResult;
            JsonObject pendingChildEvidence;
            boolean reconcilePending;
            String label = "";
            boolean dragonDead;
            boolean centralExitAttempted;
            boolean creditsSeen;
            BlockPos exitPortal;
            JsonObject exitLinkEvidence;
            int arrivalTicks;
            int armorAttempts;
            BlockPos fountain;
            int fights;
            long burrowCooldownUntil;
            long healWalkWaitUntil;
            long lowHealthBurrowCooldownUntil;
            BlockPos healSpot;
            final Set<Long> rejectedHealSpots = new HashSet<>();
            int exitAttempts;
            float healThreshold = 16.0F;

            AiOperation stepIntoPortal(BlockPos portal) {
               return AiEndRun.enterPortal(mc, portal);
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null && mc.level != null) {
                  if (!this.worldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || this.connection != mc.getConnection()) {
                     return this.finish(AiOperation.Status.CANCELLED, "End run connection changed");
                  }

                  if (!mc.player.getUUID().equals(this.playerId)) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "Player changed during End traversal");
                  }

                  if (mc.player.isAlive() && this.deathRevisionAtStart == AiServerEvidence.deathRevision()) {
                     if (mc.gui.screen() instanceof WinScreen credits) {
                        if (this.dragonDead && this.centralExitAttempted) {
                           this.creditsSeen = true;
                           credits.onClose();
                           AiSpeedrun.log("[AI] end: credits closed; respawning in the overworld");
                           return this.running("Past the credits");
                        } else {
                           return this.finish(AiOperation.Status.UNCERTAIN, "Credits appeared without a verified central-exit attempt");
                        }
                     } else if (mc.level.dimension() != Level.END) {
                        this.stopChild();
                        if (!this.dragonDead || !this.centralExitAttempted || this.exitPortal == null || mc.level.dimension() != Level.OVERWORLD) {
                           return this.finish(AiOperation.Status.UNCERTAIN, "Left the End without a verified dragon defeat and central exit transition");
                        }

                        if (++this.arrivalTicks < 20) {
                           return this.running("Verifying supported Overworld arrival after the central exit");
                        }

                        String hazard = AiPortalLinks.arrivalHazard();
                        AiPortalLinks.recordLink("minecraft:the_end", this.exitPortal, "minecraft:overworld", mc.player.blockPosition(), hazard == null, this.exitLinkEvidence);
                        return this.finish(
                           hazard == null ? AiOperation.Status.SUCCEEDED : AiOperation.Status.UNCERTAIN,
                           hazard == null
                              ? "Dragon defeated and central exit to the Overworld verified"
                              : "Central exit reached; arrival requires recovery: " + hazard
                        );
                     } else {
                        if (System.nanoTime() > this.deadline) {
                           return this.finish(this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Out of time in the End");
                        }

                        autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(5000000000L);
                        autismclient.ai.engine.AiMotionSafety.setPitchFloor(25.0F, 5000000000L);
                        if (mc.player.onGround() && mc.player.tickCount % 40 == 0) {
                           AiMlg.moveToHotbar(mc.player, Items.WATER_BUCKET);
                        }

                        if (this.fountain == null) {
                           this.fountain = this.findFountain();
                        }

                        boolean liveDragon = !AiWorldScan.findEntitiesByType("minecraft:ender_dragon", 256.0).isEmpty();
                        this.dragonDead = this.dragonDead | (!liveDragon && this.findActiveExitPortal() != null);
                        if (!this.label.startsWith("fight the dragon")) {
                           AiOperation.Result g = this.guard
                              .tick(
                                 this.child, this::stopChild, () -> (int)Math.max(10L, (this.deadline - System.nanoTime()) / 1000000000L)
                              );
                           if (g != null) {
                              return g.terminal() ? this.finishWithChild(g.status(), g.message(), g) : g;
                           }
                        }

                        if (this.child != null) {
                           AiOperation.Result r = this.child.tick();
                           this.lastChildResult = r;
                           if (!r.terminal()) {
                              return this.running(this.label + ": " + r.message());
                           }

                           r = AiNetherRun.finishChild(this.child, r);
                           this.lastChildResult = r;
                           this.child = null;
                           AiSpeedrun.log("[AI] end step {}: {} - {}", r.status(), this.label, AiSpeedrun.clip(r.message()));
                           this.steps
                              .add(AiJson.objectOf("step", this.label, "status", r.status().name(), "message", AiSpeedrun.clip(r.message())));
                           if (r.status() == AiOperation.Status.CANCELLED) {
                              return this.finishWithChild(AiOperation.Status.CANCELLED, "Cancelled at " + this.label, r);
                           }

                           if (r.status() == AiOperation.Status.UNCERTAIN) {
                              return this.finishWithChild(AiOperation.Status.UNCERTAIN, this.label + ": " + r.message(), r);
                           }

                           if (this.label.startsWith("fight the dragon") && r.status() == AiOperation.Status.SUCCEEDED) {
                              this.dragonDead = this.dragonDead | AiJson.getBoolean(AiRunResources.parseEvidenceJson(r.dataJson()), "dragonDead", false);
                           }

                           if (this.label.equals("heal in a hole") && r.status() != AiOperation.Status.SUCCEEDED && this.healSpot != null) {
                              this.rejectedHealSpots.add(this.healSpot.below().asLong());
                              this.healSpot = null;
                              this.burrowCooldownUntil = 0L;
                           }

                           if (this.label.startsWith("fight the dragon") && r.failure() == AiFailure.LOW_HEALTH) {
                              this.healThreshold = Math.min(20.0F, mc.player.getMaxHealth()) - 0.01F;
                           }

                           if (this.label.startsWith("fight the dragon")
                              && r.status() != AiOperation.Status.SUCCEEDED
                              && r.failure() != AiFailure.LOW_HEALTH
                              && ++this.fights >= 3
                              && !this.dragonDead) {
                              return this.finish(AiOperation.Status.FAILED, "The dragon fight failed " + this.fights + " times: " + AiSpeedrun.clip(r.message()));
                           }
                        }

                        if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
                           return this.running("Landing");
                        }

                        if ((mc.player.isOnFire() || AiEndRun.isInFire(mc)) && !mc.player.isInWater()) {
                           AiOperation out = AiEndRun.douseFire(mc);
                           if (out == null && AiEndRun.isInFire(mc)) {
                              out = AiEndRun.escapeFireOp(mc);
                           }

                           if (out != null) {
                              return this.startStep("put the fire out", out);
                           }
                        }

                        return this.planNextStep();
                     }
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "Player died in the End; no completion credited");
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            AiOperation.Result healBeforeFight() {
               if (mc.player.getHealth() + mc.player.getAbsorptionAmount() >= this.healThreshold) {
                  this.healThreshold = 16.0F;
                  this.healSpot = null;
                  return null;
               }

               if (this.healSpot == null) {
                  this.healSpot = this.findHealSpot();
               }

               if (mc.player.getHealth() <= 6.0F && System.nanoTime() > this.burrowCooldownUntil && System.nanoTime() > this.lowHealthBurrowCooldownUntil) {
                  this.lowHealthBurrowCooldownUntil = System.nanoTime() + 15000000000L;
                  return this.startStep(
                     "heal in a hole here",
                     AiBurrow.burrow(AiJson.objectOf("healTo", Math.max(18.0F, this.healThreshold), "stay", false, "towerOk", false, "timeoutSeconds", 120))
                  );
               }

               if (this.healSpot != null
                  && Math.hypot(mc.player.getX() - (this.healSpot.getX() + 0.5), mc.player.getZ() - (this.healSpot.getZ() + 0.5)) > 1.2) {
                  if (System.nanoTime() < this.healWalkWaitUntil) {
                     return this.startStep(
                        "wait to walk to the heal spot (health " + String.format("%.0f", mc.player.getHealth()) + ")",
                        AiNetherRun.delayThen(20, AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "waited", new JsonObject()))
                     );
                  }

                  this.healWalkWaitUntil = System.nanoTime() + 3000000000L;
                  return this.startStep(
                     "to the heal spot at " + this.healSpot.toShortString(),
                     AiGuard.allowDuringLowHealth(AiGoto.travelToCompat(this.healSpot, 0.6, true, true, true, 40, autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST, true))
                  );
               } else if (mc.player.getFoodData().getFoodLevel() < 20 && AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0) {
                  return this.startStep("eat before the fight", AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 20)));
               } else if (System.nanoTime() > this.burrowCooldownUntil) {
                  this.burrowCooldownUntil = System.nanoTime() + 20000000000L;
                  return this.startStep(
                     "heal in a hole",
                     AiBurrow.burrow(AiJson.objectOf("healTo", Math.max(18.0F, this.healThreshold), "stay", false, "towerOk", false, "timeoutSeconds", 120))
                  );
               } else {
                  return this.startStep(
                     "rest to heal (health " + String.format("%.0f", mc.player.getHealth()) + ")",
                     AiNetherRun.delayThen(100, AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "rested", new JsonObject()))
                  );
               }
            }

            BlockPos findHealSpot() {
               double px = mc.player.getX();
               double pz = mc.player.getZ();
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (int r = 17; r <= 23; r += 3) {
                  for (int k = 0; k < 24; k++) {
                     double a = 6.283185307179586 * k / 24.0;
                     int x = (this.fountain == null ? 0 : this.fountain.getX()) + (int)Math.floor(Math.cos(a) * r);
                     int z = (this.fountain == null ? 0 : this.fountain.getZ()) + (int)Math.floor(Math.sin(a) * r);
                     BlockPos top = this.surfaceAt(x, z);
                     if (top != null && !this.rejectedHealSpots.contains(top.asLong()) && mc.level.getBlockState(top).is(Blocks.END_STONE)) {
                        boolean solid = true;

                        for (int d = 1; d <= 4 && solid; d++) {
                           if (mc.level.getBlockState(top.below(d)).getCollisionShape(mc.level, top.below(d)).isEmpty()) {
                              solid = false;
                           }
                        }

                        for (int dx = -3; dx <= 3 && solid; dx++) {
                           for (int dz = -3; dz <= 3 && solid; dz++) {
                              BlockPos t = this.surfaceAt(x + dx, z + dz);
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

            BlockPos surfaceAt(int x, int z) {
               BlockPos probe = new BlockPos(x, 64, z);
               if (!AiTerrain.isLoaded(mc.level, probe)) {
                  return null;
               }

               int y = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
               return y <= mc.level.getMinY() ? null : new BlockPos(x, y, z);
            }

            AiOperation.Result planNextStep() {
               BlockPos portal = this.findActiveExitPortal();
               if (this.dragonDead && portal != null) {
                  this.exitPortal = portal.immutable();
                  this.exitLinkEvidence = AiJson.objectOf(
                     "kind",
                     "central_end_exit",
                     "dimension",
                     "minecraft:the_end",
                     "world",
                     this.worldKey,
                     "entrance",
                     portal.toShortString(),
                     "fountain",
                     this.fountain.toShortString(),
                     "active",
                     true,
                     "observedTick",
                     AiThreats.currentClientTick()
                  );
               }

               if (this.dragonDead && portal != null && this.exitAttempts >= 1) {
                  this.centralExitAttempted = true;
                  this.exitAttempts++;
                  return this.startStep("onto the exit portal at " + portal.toShortString(), this.stepIntoPortal(portal));
               }

               if (this.dragonDead && portal != null) {
                  this.centralExitAttempted = true;
                  this.exitAttempts++;
                  return this.startStep(
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

               if (this.dragonDead) {
                  return this.running("Waiting for the exit portal to open");
               }

               if (AiRunResources.isCampaignActive()) {
                  Map<String, Integer> missing = AiRunResources.missingMandatoryGear();
                  if (!missing.isEmpty()) {
                     return this.finish(AiOperation.Status.FAILED, "Required campaign equipment is unavailable in the End: " + missing);
                  }

                  if (!AiRunResources.isMandatoryArmorEquipped()) {
                     if (this.armorAttempts++ >= 2) {
                        return this.finish(AiOperation.Status.FAILED, "Required End armor could not be equipped; readiness remains unresolved");
                     }

                     return this.startStep("equip the required End armor", AiEquip.create(AiJson.objectOf("what", "armor")));
                  }
               }

               int fx = this.fountain == null ? 0 : this.fountain.getX();
               int fz = this.fountain == null ? 0 : this.fountain.getZ();
               double fromCentre = Math.hypot(mc.player.getX() - fx, mc.player.getZ() - fz);
               if (fromCentre > 32.0) {
                  return this.startStep(
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
                        new AiNetherRun.RadiusGoal(fx, fz, 24),
                        AiGoto.GotoOptions.DEFAULT
                     )
                  );
               }

               AiOperation.Result healed = this.healBeforeFight();
               if (healed != null) {
                  return healed;
               }

               int beds = AiItems.countInInventory(AiItems.resolve("bed"));
               return this.startStep(
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
                        (int)Math.max(60L, Math.min(1500L, (this.deadline - System.nanoTime()) / 1000000000L))
                     )
                  )
               );
            }

            BlockPos findActiveExitPortal() {
               if (this.fountain == null) {
                  return null;
               }

               for (BlockPos p : BlockPos.betweenClosed(this.fountain.offset(-4, -4, -4), this.fountain.offset(4, 3, 4))) {
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

            BlockPos findFountain() {
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

            AiOperation.Result startStep(String label, AiOperation op) {
               this.label = label;
               this.lastChildResult = null;
               AiSpeedrun.log("[AI] end: {}", label);
               this.child = AiRunDiagnostics.trace("end", label, op);
               return this.running(label);
            }

            JsonObject progressJson() {
               return AiJson.objectOf(
                  "dragonDead",
                  this.dragonDead,
                  "centralExitAttempted",
                  this.centralExitAttempted,
                  "creditsSeen",
                  this.creditsSeen,
                  "fountain",
                  this.fountain == null ? "" : this.fountain.toShortString(),
                  "fights",
                  this.fights,
                  "steps",
                  this.steps,
                  "pendingChild",
                  this.pendingChildEvidence == null ? new JsonObject() : this.pendingChildEvidence
               );
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
            }

            void stopChild() {
               if (this.child != null) {
                  boolean pending = this.child.requiresReconciliationAfterEmergency();
                  this.reconcilePending |= pending;
                  if (pending && this.lastChildResult != null) {
                     this.pendingChildEvidence = AiRunResources.compactEvidenceJson(this.lastChildResult.dataJson());
                  }

                  this.child.cancel();
                  this.child = null;
               }
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finishWithChild(status, message, this.lastChildResult);
            }

            AiOperation.Result finishWithChild(AiOperation.Status status, String message, AiOperation.Result child) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               AiOperation.Result result = AiNetherRun.phaseResult(status, message, this.progressJson(), pending, child);
               this.reconcilePending = this.reconcilePending | (pending || AiJson.getBoolean(AiRunResources.parseEvidenceJson(result.dataJson()), "reobserveBeforeResuming", false));
               this.stopChild();
               this.guard.cancel();
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(0L);
               return result;
            }

            @Override
            public boolean canInterruptNow() {
               return !this.reconcilePending && (this.child == null || this.child.canInterruptNow()) && this.guard.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.reconcilePending || this.child != null && this.child.requiresReconciliationAfterEmergency() || this.guard.requiresReconciliation();
            }

            @Override
            public void cancel() {
               this.reconcilePending = this.reconcilePending | this.requiresReconciliationAfterEmergency();
               this.stopChild();
               this.guard.cancel();
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(0L);
            }
         };
      } else {
         throw new IllegalArgumentException("The End run needs a connected living player");
      }
   }
}
