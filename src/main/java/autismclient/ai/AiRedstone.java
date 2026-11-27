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
   private static final List<Property<?>> init_List = List.of(
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
   private static final List<Property<Boolean>> load_List = List.of(BlockStateProperties.OPEN, BlockStateProperties.POWERED, BlockStateProperties.LIT);

   private AiRedstone() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiRedstone.Inner_DataUtil action = init(AiJson.getString(args, "action", "inspect"));
         if (action == AiRedstone.Inner_DataUtil.INSPECT) {
            int radius = Math.max(1, Math.min(24, AiJson.getInt(args, "radius", 8)));
            return AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "Read redstone components within " + radius + " blocks", init(mc, radius));
         } else {
            final BlockPos pos = AiGameOperations.requireBlockPos(args);
            final boolean desired = AiJson.getBoolean(args, "state", true);
            final boolean navigate = AiJson.getBoolean(args, "navigate", true);
            final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)));
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
               BlockState init_BlockState;
               AiOperation init_AiOperation;
               int init_I;
               int load_I;
               boolean init_Z;
               boolean load_Z;

               @Override
               public AiOperation.Result tick() {
                  if (!this.load_Z && mc.player != null && mc.level != null) {
                     if (System.nanoTime() > this.init_J) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Redstone action did not complete before the deadline");
                     }

                     if (!AiTerrain.isLoaded(mc.level, pos)) {
                        return this.init(AiOperation.Status.FAILED, "Target is in unloaded terrain");
                     }

                     BlockState state = mc.level.getBlockState(pos);
                     Property<Boolean> property = AiRedstone.init_Property(state);
                     if (property == null) {
                        return this.init(
                           AiOperation.Status.FAILED,
                           BuiltInRegistries.BLOCK.getKey(state.getBlock())
                              + " has no open/powered/lit state to drive; use game:interact for a plain right-click"
                        );
                     }

                     if (action == AiRedstone.Inner_DataUtil.SET && state.getValue(property) == desired && !this.init_Z) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Already " + property.getName() + "=" + desired + "; nothing to do");
                     }

                     if (!this.init_Z) {
                        BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, pos);
                        if (interactionHit == null) {
                           if (!navigate) {
                              return this.init(AiOperation.Status.FAILED, "Component is out of reach and navigate is disabled");
                           }

                           if (this.init_AiOperation == null) {
                              this.init_AiOperation = AiGoto.approachBlockCompat(pos, Math.min(120, this.init_I_25()));
                           }

                           AiOperation.Result legResult = this.init_AiOperation.tick();
                           if (!legResult.terminal()) {
                              return this.init("Walking to the component");
                           }

                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                           return legResult.status() != AiOperation.Status.SUCCEEDED
                              ? this.init(AiOperation.Status.FAILED, "No route to the component: " + legResult.message())
                              : this.init("Arrived at the component");
                        } else {
                           if (this.init_AiOperation != null) {
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                           }

                           this.init_DataUtil.aimAt(interactionHit.getLocation());
                           this.init_DataUtil.tick();
                           if (++this.init_I > 60) {
                              return this.init(AiOperation.Status.FAILED, "Could not aim at " + pos.toShortString());
                           }

                           if (!autismclient.ai.engine.AiAim.isLookingAtBlock(pos)) {
                              return this.init("Turning toward the component");
                           }

                           BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                           if (hit == null) {
                              return this.init("Turning toward the component");
                           }

                           this.init_BlockState = state;
                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                           this.init_Z = true;
                           return this.init("Activating the component");
                        }
                     } else if (this.init_BlockState != null && !state.equals(this.init_BlockState)) {
                        boolean now = state.getValue(property);
                        return action == AiRedstone.Inner_DataUtil.SET && now != desired
                           ? this.init(
                              AiOperation.Status.UNCERTAIN,
                              "The component changed to " + property.getName() + "=" + now + " rather than the requested " + desired
                           )
                           : this.init(AiOperation.Status.SUCCEEDED, property.getName() + " is now " + now);
                     } else {
                        return ++this.load_I > 30
                           ? this.init(AiOperation.Status.UNCERTAIN, "The click went out but the block state did not change; the server may have rejected it")
                           : this.init("Waiting for the component to change");
                     }
                  } else {
                     return this.init(AiOperation.Status.CANCELLED, "Redstone action cancelled");
                  }
               }

               int init_I_25() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_40() {
                  JsonObject out = AiJson.objectOf(
                     "action", action.name().toLowerCase(Locale.ROOT), "x", pos.getX(), "y", pos.getY(), "z", pos.getZ()
                  );
                  if (mc.level != null && AiTerrain.isLoaded(mc.level, pos)) {
                     out.add("component", AiRedstone.init_JsonObject(mc, pos));
                  }

                  if (action == AiRedstone.Inner_DataUtil.SET) {
                     out.addProperty("requested", desired);
                  }

                  return out;
               }

               AiOperation.Result init(String message) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_40().toString(), List.of());
               }

               AiOperation.Result init(AiOperation.Status status, String message) {
                  JsonObject data = this.init_JsonObject_40();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               @Override
               public void cancel() {
                  if (!this.load_Z) {
                     this.load_Z = true;
                     this.init_DataUtil.clear();
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }
                  }
               }
            };
         }
      } else {
         throw new IllegalArgumentException("Redstone operations require a connected living player");
      }
   }

   static JsonObject init(Minecraft mc, int radius) {
      JsonArray components = new JsonArray();
      BlockPos origin = mc.player.blockPosition();
      int vertical = Math.min(radius, 8);

      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -vertical, -radius), origin.offset(radius, vertical, radius))) {
         if (components.size() >= 128) {
            break;
         }

         if (AiTerrain.isLoaded(mc.level, pos)) {
            BlockState state = mc.level.getBlockState(pos);
            if (init_Z(state)) {
               components.add(init_JsonObject(mc, pos.immutable()));
            }
         }
      }

      return AiJson.objectOf(
         "radius", radius, "components", components, "limits", "Loaded terrain only, at most 128 components, vertical radius capped at 8."
      );
   }

   static JsonObject init_JsonObject(Minecraft mc, BlockPos pos) {
      BlockState state = mc.level.getBlockState(pos);
      JsonObject properties = new JsonObject();

      for (Property<?> property : init_List) {
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
         init_I(mc, pos),
         "toggleable",
         init_Property(state) != null,
         "properties",
         properties
      );
   }

   static int init_I(Minecraft mc, BlockPos pos) {
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

   static Property<Boolean> init_Property(BlockState state) {
      for (Property<Boolean> property : load_List) {
         if (state.hasProperty(property)) {
            return property;
         }
      }

      return null;
   }

   static boolean init_Z(BlockState state) {
      if (state.isAir()) {
         return false;
      }

      if (state.isSignalSource()) {
         return true;
      }

      for (Property<?> property : init_List) {
         if (state.hasProperty(property)) {
            return true;
         }
      }

      return false;
   }

   private static AiRedstone.Inner_DataUtil init(String text) {
      try {
         return AiRedstone.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect, toggle or set");
      }
   }

   enum Inner_DataUtil {
      INSPECT,
      TOGGLE,
      SET;
   }
}
