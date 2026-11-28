package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

final class AiRedstone {
   private static final int init_I = 8;
   private static final int load_I = 24;
   private static final int read = 128;
   private static final int write = 60;
   private static final int update = 30;
   private static final List<Property<?>> INSPECTED_PROPERTIES = List.of(
      BlockStateProperties.POWERED,
      BlockStateProperties.OPEN,
      BlockStateProperties.LIT,
      BlockStateProperties.POWER,
      BlockStateProperties.DELAY,
      BlockStateProperties.LOCKED,
      BlockStateProperties.TRIGGERED,
      BlockStateProperties.EXTENDED,
      BlockStateProperties.ENABLED,
      BlockStateProperties.ATTACHED,
      BlockStateProperties.INVERTED
   );
   private static final List<Property<Boolean>> TOGGLE_PROPERTIES = List.of(BlockStateProperties.OPEN, BlockStateProperties.POWERED, BlockStateProperties.LIT);

   private AiRedstone() {
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiRedstone.RedstoneAction action = parseAction(AiJson.getString(args, "action", "inspect"));
         if (action == AiRedstone.RedstoneAction.INSPECT) {
            int radius = Math.max(1, Math.min(24, AiJson.getInt(args, "radius", 8)));
            return AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "Read redstone components within " + radius + " blocks", scanComponents(mc, radius));
         } else {
            final BlockPos pos = AiGameOperations.requireBlockPos(args);
            final boolean desired = AiJson.getBoolean(args, "state", true);
            final boolean navigate = AiJson.getBoolean(args, "navigate", true);
            final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)));
            return new AiOperation() {
               final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
               final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
               BlockState observedState;
               AiOperation approachOp;
               int aimTicks;
               int settleTicks;
               boolean toggled;
               boolean cancelled;

               @Override
               public AiOperation.Result tick() {
                  if (!this.cancelled && mc.player != null && mc.level != null) {
                     if (System.nanoTime() > this.deadline) {
                        return this.finish(AiOperation.Status.TIMED_OUT, "Redstone action did not complete before the deadline");
                     }

                     if (!AiTerrain.isLoaded(mc.level, pos)) {
                        return this.finish(AiOperation.Status.FAILED, "Target is in unloaded terrain");
                     }

                     BlockState state = mc.level.getBlockState(pos);
                     Property<Boolean> property = AiRedstone.toggleProperty(state);
                     if (property == null) {
                        return this.finish(
                           AiOperation.Status.FAILED,
                           BuiltInRegistries.BLOCK.getKey(state.getBlock())
                              + " has no open/powered/lit state to drive; use game:interact for a plain right-click"
                        );
                     }

                     if (action == AiRedstone.RedstoneAction.SET && state.getValue(property) == desired && !this.toggled) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Already " + property.getName() + "=" + desired + "; nothing to do");
                     }

                     if (!this.toggled) {
                        BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, pos);
                        if (interactionHit == null) {
                           if (!navigate) {
                              return this.finish(AiOperation.Status.FAILED, "Component is out of reach and navigate is disabled");
                           }

                           if (this.approachOp == null) {
                              this.approachOp = AiGoto.approachBlockCompat(pos, Math.min(120, this.secondsLeft()));
                           }

                           AiOperation.Result legResult = this.approachOp.tick();
                           if (!legResult.terminal()) {
                              return this.running("Walking to the component");
                           }

                           this.approachOp.cancel();
                           this.approachOp = null;
                           return legResult.status() != AiOperation.Status.SUCCEEDED
                              ? this.finish(AiOperation.Status.FAILED, "No route to the component: " + legResult.message())
                              : this.running("Arrived at the component");
                        } else {
                           if (this.approachOp != null) {
                              this.approachOp.cancel();
                              this.approachOp = null;
                           }

                           this.aim.aimAt(interactionHit.getLocation());
                           this.aim.tick();
                           if (++this.aimTicks > 60) {
                              return this.finish(AiOperation.Status.FAILED, "Could not aim at " + pos.toShortString());
                           }

                           if (!autismclient.ai.engine.AiAim.isLookingAtBlock(pos)) {
                              return this.running("Turning toward the component");
                           }

                           BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                           if (hit == null) {
                              return this.running("Turning toward the component");
                           }

                           this.observedState = state;
                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                           this.toggled = true;
                           return this.running("Activating the component");
                        }
                     } else if (this.observedState != null && !state.equals(this.observedState)) {
                        boolean now = state.getValue(property);
                        return action == AiRedstone.RedstoneAction.SET && now != desired
                           ? this.finish(
                              AiOperation.Status.UNCERTAIN,
                              "The component changed to " + property.getName() + "=" + now + " rather than the requested " + desired
                           )
                           : this.finish(AiOperation.Status.SUCCEEDED, property.getName() + " is now " + now);
                     } else {
                        return ++this.settleTicks > 30
                           ? this.finish(AiOperation.Status.UNCERTAIN, "The click went out but the block state did not change; the server may have rejected it")
                           : this.running("Waiting for the component to change");
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Redstone action cancelled");
                  }
               }

               int secondsLeft() {
                  return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
               }

               JsonObject progressData() {
                  JsonObject out = AiJson.objectOf(
                     "action", action.name().toLowerCase(Locale.ROOT), "x", pos.getX(), "y", pos.getY(), "z", pos.getZ()
                  );
                  if (mc.level != null && AiTerrain.isLoaded(mc.level, pos)) {
                     out.add("component", AiRedstone.componentJson(mc, pos));
                  }

                  if (action == AiRedstone.RedstoneAction.SET) {
                     out.addProperty("requested", desired);
                  }

                  return out;
               }

               AiOperation.Result running(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressData().toString(), List.of());
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  JsonObject data = this.progressData();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               @Override
               public void cancel() {
                  if (!this.cancelled) {
                     this.cancelled = true;
                     this.aim.clear();
                     if (this.approachOp != null) {
                        this.approachOp.cancel();
                        this.approachOp = null;
                     }
                  }
               }
            };
         }
      } else {
         throw new IllegalArgumentException("Redstone operations require a connected living player");
      }
   }

   static JsonObject scanComponents(Minecraft mc, int radius) {
      JsonArray components = new JsonArray();
      BlockPos origin = mc.player.blockPosition();
      int vertical = Math.min(radius, 8);

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -vertical, -radius), origin.offset(radius, vertical, radius))) {
         if (components.size() >= 128) {
            break;
         }

         if (AiTerrain.isLoaded(mc.level, pos)) {
            BlockState state = mc.level.getBlockState(pos);
            if (isRedstoneComponent(state)) {
               components.add(componentJson(mc, pos.immutable()));
            }
         }
      }

      return AiJson.objectOf(
         "radius", radius, "components", components, "limits", "Loaded terrain only, at most 128 components, vertical radius capped at 8."
      );
   }

   static JsonObject componentJson(Minecraft mc, BlockPos pos) {
      BlockState state = mc.level.getBlockState(pos);
      JsonObject properties = new JsonObject();

      for (Property<?> property : INSPECTED_PROPERTIES) {
         if (state.hasProperty(property)) {
            properties.addProperty(property.getName(), String.valueOf(state.getValue(property)));
         }
      }

      return AiJson.objectOf(
         "x",
         pos.getX(),
         "y",
         pos.getY(),
         "z",
         pos.getZ(),
         "block",
         BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),
         "signal",
         signalStrength(mc, pos),
         "toggleable",
         toggleProperty(state) != null,
         "properties",
         properties
      );
   }

   static int signalStrength(Minecraft mc, BlockPos pos) {
      BlockState state = mc.level.getBlockState(pos);
      if (!state.isSignalSource()) {
         return -1;
      }

      int best = 0;

      for (Direction side : Direction.values()) {
         best = Math.max(best, state.getSignal(mc.level, pos, side));
      }

      return best;
   }

   static Property<Boolean> toggleProperty(BlockState state) {
      for (Property<Boolean> property : TOGGLE_PROPERTIES) {
         if (state.hasProperty(property)) {
            return property;
         }
      }

      return null;
   }

   static boolean isRedstoneComponent(BlockState state) {
      if (state.isAir()) {
         return false;
      }

      if (state.isSignalSource()) {
         return true;
      }

      for (Property<?> property : INSPECTED_PROPERTIES) {
         if (state.hasProperty(property)) {
            return true;
         }
      }

      return false;
   }

   private static AiRedstone.RedstoneAction parseAction(String text) {
      try {
         return AiRedstone.RedstoneAction.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect, toggle or set");
      }
   }

   enum RedstoneAction {
      INSPECT,
      TOGGLE,
      SET;
   }
}
