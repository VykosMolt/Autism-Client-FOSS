package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

final class AiPortalLinks {
   private static String init_Str = "";
   private static Object init_Obj;
   private static boolean init_Z;
   private static long init_J;
   private static final List<AiPortalLinks.Inner_DataUtil> init_List = new ArrayList<>();
   private static final Map<String, Double> init_Map = new HashMap<>();

   private AiPortalLinks() {
   }

   private static void init_V() {
      String current = AiWorldIdentity.init(Minecraft.getInstance());
      if (!init_Str.equals(current) || init_Obj != Minecraft.getInstance().getConnection()) {
         init_Str = current;
         init_Obj = Minecraft.getInstance().getConnection();
         init_List.clear();
         init_Map.clear();
         init_Z = false;
         init_J++;
      }

      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         init_Map.put(AiWorldIdentity.load(Minecraft.getInstance()), level.dimensionType().coordinateScale());
      }

      AiService service = AiService.load_cc();
      if (!init_Z && service != null && init_Obj != null) {
         String requestedWorld = init_Str;
         Object requestedConnection = init_Obj;
         long requestedGeneration = init_J;
         init_Z = service.init_Z_Str_Consumer(
            init_Str,
            stored -> {
               if (init_J == requestedGeneration
                  && init_Str.equals(requestedWorld)
                  && init_Obj == requestedConnection
                  && Minecraft.getInstance().getConnection() == requestedConnection
                  && AiWorldIdentity.init(Minecraft.getInstance()).equals(requestedWorld)) {
                  for (JsonElement entry : stored) {
                     try {
                        JsonObject saved = entry.getAsJsonObject();
                        String from = AiJson.getString(saved, "fromDimension", "");
                        String to = AiJson.getString(saved, "toDimension", "");
                        BlockPos entrance = init_BlockPos(AiJson.getString(saved, "entrance", ""));
                        BlockPos arrival = init_BlockPos(AiJson.getString(saved, "arrival", ""));
                        if (entrance != null
                           && arrival != null
                           && !from.isEmpty()
                           && !to.isEmpty()
                           && !from.equals(to)
                           && !init_List.stream()
                              .anyMatch(link -> link.fromDimension.equals(from) && link.toDimension.equals(to) && link.entrance.distSqr(entrance) < 16.0)) {
                           if (init_List.size() >= 64) {
                              break;
                           }

                           JsonObject before = saved.has("entranceEvidence") && saved.get("entranceEvidence").isJsonObject()
                              ? saved.getAsJsonObject("entranceEvidence")
                              : new JsonObject();
                           JsonObject after = saved.has("arrivalEvidence") && saved.get("arrivalEvidence").isJsonObject()
                              ? saved.getAsJsonObject("arrivalEvidence")
                              : new JsonObject();
                           init_List.add(
                              new AiPortalLinks.Inner_DataUtil(
                                 from,
                                 entrance,
                                 to,
                                 arrival,
                                 -1L,
                                 saved.has("observedAtMs") ? saved.get("observedAtMs").getAsLong() : 0L,
                                 true,
                                 AiJson.getBoolean(saved, "safeAtObservation", false),
                                 before.deepCopy(),
                                 after.deepCopy()
                              )
                           );
                        }
                     } catch (RuntimeException var15) {
                     }
                  }

                  init_List.sort(Comparator.comparingLong(AiPortalLinks.Inner_DataUtil::observedAtMillis));
                  service.init_V_Str_JsonArray(init_Str, load());
               }
            }
         );
      }
   }

   private static BlockPos init_BlockPos(String text) {
      try {
         String[] xyz = text.split(",");
         if (xyz.length != 3) {
            return null;
         }

         int x = Integer.parseInt(xyz[0].trim());
         int y = Integer.parseInt(xyz[1].trim());
         int z = Integer.parseInt(xyz[2].trim());
         return Math.abs((long)x) <= 30000000L && Math.abs((long)z) <= 30000000L && y >= -2048 && y <= 2048 ? new BlockPos(x, y, z) : null;
      } catch (RuntimeException e) {
         return null;
      }
   }

   static void init(String from, BlockPos entrance, String to, BlockPos arrival, boolean safe, JsonObject entranceEvidence) {
      init_V();
      if (entrance != null && arrival != null && from != null && to != null && !from.equals(to)) {
         init_List.removeIf(link -> link.fromDimension.equals(from) && link.entrance.distSqr(entrance) < 16.0 && link.toDimension.equals(to));
         init_List.add(
            new AiPortalLinks.Inner_DataUtil(
               from,
               entrance.immutable(),
               to,
               arrival.immutable(),
               AiThreats.init_J(),
               System.currentTimeMillis(),
               false,
               safe,
               entranceEvidence == null ? new JsonObject() : entranceEvidence.deepCopy(),
               init(arrival)
            )
         );
         AiRunTrace.init_V_Str_JsonObject(
            "portal_link",
            AiJson.objectOf("from", from, "entrance", entrance.toShortString(), "to", to, "arrival", arrival.toShortString(), "safe", safe)
         );
         init_List.sort(Comparator.comparingLong(AiPortalLinks.Inner_DataUtil::observedAtMillis));

         while (init_List.size() > 64) {
            init_List.remove(0);
         }

         AiService service = AiService.load_cc();
         if (service != null) {
            service.init_V_Str_JsonArray(init_Str, load());
         }
      }
   }

   static JsonObject init(BlockPos near) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && near != null) {
         BlockPos seed = null;

         for (BlockPos p : BlockPos.betweenClosed(near.offset(-3, -3, -3), near.offset(3, 3, 3))) {
            if (AiTerrain.isLoaded(mc.level, p) && mc.level.getBlockState(p).is(Blocks.NETHER_PORTAL)) {
               seed = p.immutable();
               break;
            }
         }

         if (seed == null) {
            BlockPos endPortal = null;
            int active = 0;
            boolean partial = false;

            for (BlockPos p : BlockPos.betweenClosed(near.offset(-3, -3, -3), near.offset(3, 3, 3))) {
               if (!AiTerrain.isLoaded(mc.level, p)) {
                  partial = true;
               } else if (mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                  active++;
                  if (endPortal == null) {
                     endPortal = p.immutable();
                  }
               }
            }

            return AiJson.objectOf(
               "observed",
               endPortal != null,
               "kind",
               endPortal == null ? "arrival_position" : "end_portal",
               "dimension",
               AiWorldIdentity.load(mc),
               "arrival",
               near.toShortString(),
               "activeCellsInLocalScan",
               active,
               "partialCoverage",
               partial,
               "observedTick",
               AiThreats.init_J(),
               "geometryVerified",
               false
            );
         } else {
            Axis axis = mc.level.getBlockState(seed).getValue(BlockStateProperties.HORIZONTAL_AXIS);
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            HashSet<BlockPos> cells = new HashSet<>();
            queue.add(seed);
            int minX = seed.getX();
            int maxX = minX;
            int minY = seed.getY();
            int maxY = minY;
            int minZ = seed.getZ();
            int maxZ = minZ;
            boolean partial = false;

            while (!queue.isEmpty() && cells.size() < 441) {
               BlockPos cell = queue.removeFirst();
               if (!AiTerrain.isLoaded(mc.level, cell)) {
                  partial = true;
               } else {
                  BlockState state = mc.level.getBlockState(cell);
                  if (state.is(Blocks.NETHER_PORTAL) && state.getValue(NetherPortalBlock.AXIS) == axis && cells.add(cell)) {
                     minX = Math.min(minX, cell.getX());
                     maxX = Math.max(maxX, cell.getX());
                     minY = Math.min(minY, cell.getY());
                     maxY = Math.max(maxY, cell.getY());
                     minZ = Math.min(minZ, cell.getZ());
                     maxZ = Math.max(maxZ, cell.getZ());

                     for (Direction direction : Direction.values()) {
                        if (direction.getAxis() == axis || direction.getAxis() == Axis.Y) {
                           queue.add(cell.relative(direction));
                        }
                     }
                  }
               }
            }

            int width = axis == Axis.X ? maxX - minX + 1 : maxZ - minZ + 1;
            int height = maxY - minY + 1;
            boolean rectangle = width >= 2 && width <= 21 && height >= 3 && height <= 21 && cells.size() == width * height;
            boolean frameComplete = rectangle && !partial && queue.isEmpty();
            if (rectangle) {
               for (int horizontal = -1; horizontal <= width; horizontal++) {
                  for (int vertical = -1; vertical <= height; vertical++) {
                     boolean side = horizontal == -1 || horizontal == width;
                     boolean end = vertical == -1 || vertical == height;
                     if ((side || end) && (!side || !end)) {
                        BlockPos border = axis == Axis.X
                           ? new BlockPos(minX + horizontal, minY + vertical, minZ)
                           : new BlockPos(minX, minY + vertical, minZ + horizontal);
                        if (!AiTerrain.isLoaded(mc.level, border)) {
                           frameComplete = false;
                           partial = true;
                        } else if (!mc.level.getBlockState(border).is(Blocks.OBSIDIAN)) {
                           frameComplete = false;
                        }
                     }
                  }
               }
            }

            BlockPos frameMin = axis == Axis.X ? new BlockPos(minX - 1, minY - 1, minZ) : new BlockPos(minX, minY - 1, minZ - 1);
            BlockPos frameMax = axis == Axis.X ? new BlockPos(maxX + 1, maxY + 1, maxZ) : new BlockPos(maxX, maxY + 1, maxZ + 1);
            return AiJson.objectOf(
               "observed",
               true,
               "dimension",
               AiWorldIdentity.load(mc),
               "axis",
               axis.getName(),
               "activeCells",
               cells.size(),
               "interiorMin",
               new BlockPos(minX, minY, minZ).toShortString(),
               "interiorMax",
               new BlockPos(maxX, maxY, maxZ).toShortString(),
               "partialCoverage",
               partial || !queue.isEmpty(),
               "interiorRectangle",
               rectangle,
               "frameMin",
               frameMin.toShortString(),
               "frameMax",
               frameMax.toShortString(),
               "normalObsidianFrameConfirmed",
               frameComplete,
               "observedTick",
               AiThreats.init_J()
            );
         }
      } else {
         return AiJson.objectOf("observed", false);
      }
   }

   static void init_V(String resetWorld) {
      if (resetWorld == null || resetWorld.equals(init_Str)) {
         init_List.clear();
         init_Z = true;
         init_J++;
      }
   }

   static String init_Str() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null) {
         return "arrival player or terrain is unavailable";
      } else if (mc.player.isInLava()) {
         return "there is lava at the player's feet";
      } else if (mc.player.isInWall()) {
         return "the player is suffocating inside a block";
      } else if (!AiTerrain.isLoaded(mc.level, mc.player.blockPosition())) {
         return "arrival terrain is unavailable";
      } else if (AiEndRun.init_Z(mc) || mc.player.isOnFire()) {
         return "the arrival is burning";
      } else if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
         return "arrival has no confirmed supported stance";
      } else if (mc.player.getY() < mc.level.getMinY()) {
         return "the arrival is below the world's build height";
      } else {
         return autismclient.ai.engine.AiEnvironmentHazards.init(mc.level, mc.player.blockPosition()) ? "arrival intersects an unmodelled sulfur launch column" : null;
      }
   }

   static JsonObject init(Vec3 position, String from, String to) {
      init_V();
      Double sourceScale = init_Map.get(from);
      Double targetScale = init_Map.get(to);
      if (sourceScale != null && targetScale != null && Double.isFinite(sourceScale) && Double.isFinite(targetScale) && !(targetScale <= 0.0)) {
         double factor = sourceScale / targetScale;
         return AiJson.objectOf(
            "available",
            true,
            "x",
            position.x * factor,
            "y",
            position.y,
            "z",
            position.z * factor,
            "verified",
            false,
            "sourceScale",
            sourceScale,
            "targetScale",
            targetScale,
            "source",
            "observed client dimension types",
            "requiresBorderAndPortalSearch",
            true
         );
      } else {
         return AiJson.objectOf(
            "available",
            false,
            "verified",
            false,
            "reason",
            "both dimension coordinate scales have not been observed",
            "sourceDimension",
            from,
            "targetDimension",
            to
         );
      }
   }

   static JsonArray init_JsonArray() {
      init_V();
      return load();
   }

   static BlockPos init(int radius) {
      init_V();
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null
         ? init(load(), AiWorldIdentity.load(mc), mc.player.blockPosition(), radius, at -> !AiTerrain.isLoaded(mc.level, at) || mc.level.getBlockState(at).is(Blocks.NETHER_PORTAL))
         : null;
   }

   static BlockPos init(JsonArray records, String dimension, BlockPos origin, int radius) {
      return init(records, dimension, origin, radius, at -> true);
   }

   private static BlockPos init(JsonArray records, String dimension, BlockPos origin, int radius, Predicate<BlockPos> accepted) {
      BlockPos best = null;
      double distance = (double)radius * radius;

      for (JsonElement entry : records) {
         try {
            JsonObject record = entry.getAsJsonObject();

            for (String side : List.of("entrance", "arrival")) {
               if (dimension.equals(AiJson.getString(record, side.equals("entrance") ? "fromDimension" : "toDimension", ""))
                  && record.has(side + "Evidence")
                  && record.get(side + "Evidence").isJsonObject()) {
                  JsonObject evidence = record.getAsJsonObject(side + "Evidence");
                  if (AiJson.getBoolean(evidence, "observed", false)
                     && AiJson.getBoolean(evidence, "interiorRectangle", false)
                     && AiJson.getInt(evidence, "activeCells", 0) >= 6
                     && !AiJson.getBoolean(evidence, "partialCoverage", true)) {
                     BlockPos at = init_BlockPos(AiJson.getString(evidence, "interiorMin", ""));
                     if (at != null && at.distSqr(origin) <= distance && accepted.test(at)) {
                        best = at;
                        distance = at.distSqr(origin);
                     }
                  }
               }
            }
         } catch (RuntimeException var15) {
         }
      }

      return best;
   }

   private static JsonArray load() {
      JsonArray out = new JsonArray();

      for (AiPortalLinks.Inner_DataUtil link : init_List) {
         out.add(
            AiJson.objectOf(
               "fromDimension",
               link.fromDimension,
               "entrance",
               link.entrance.toShortString(),
               "toDimension",
               link.toDimension,
               "arrival",
               link.arrival.toShortString(),
               "observedTick",
               link.observedTick,
               "safeAtObservation",
               link.safe,
               "observedAtMs",
               link.observedAtMillis,
               "knowledge",
               link.historical ? "STALE" : "OBSERVED_TRANSITION",
               "verifyBeforeTravel",
               true,
               "entranceEvidence",
               link.entranceEvidence.deepCopy(),
               "arrivalEvidence",
               link.arrivalEvidence.deepCopy()
            )
         );
      }

      return out;
   }

   record Inner_DataUtil(
      String fromDimension,
      BlockPos entrance,
      String toDimension,
      BlockPos arrival,
      long observedTick,
      long observedAtMillis,
      boolean historical,
      boolean safe,
      JsonObject entranceEvidence,
      JsonObject arrivalEvidence
   ) {
   }
}
