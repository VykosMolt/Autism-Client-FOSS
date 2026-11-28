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
   private static String loadedWorldKey = "";
   private static Object loadedConnection;
   private static boolean memoryLoadStarted;
   private static long generation;
   private static final List<AiPortalLinks.PortalLink> links = new ArrayList<>();
   private static final Map<String, Double> coordinateScales = new HashMap<>();

   private AiPortalLinks() {
   }

   private static void ensureLoaded() {
      String current = AiWorldIdentity.currentWorldKey(Minecraft.getInstance());
      if (!loadedWorldKey.equals(current) || loadedConnection != Minecraft.getInstance().getConnection()) {
         loadedWorldKey = current;
         loadedConnection = Minecraft.getInstance().getConnection();
         links.clear();
         coordinateScales.clear();
         memoryLoadStarted = false;
         generation++;
      }

      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         coordinateScales.put(AiWorldIdentity.currentDimensionId(Minecraft.getInstance()), level.dimensionType().coordinateScale());
      }

      AiService service = AiService.getExistingInstance();
      if (!memoryLoadStarted && service != null && loadedConnection != null) {
         String requestedWorld = loadedWorldKey;
         Object requestedConnection = loadedConnection;
         long requestedGeneration = generation;
         memoryLoadStarted = service.loadPortalMemory(
            loadedWorldKey,
            stored -> {
               if (generation == requestedGeneration
                  && loadedWorldKey.equals(requestedWorld)
                  && loadedConnection == requestedConnection
                  && Minecraft.getInstance().getConnection() == requestedConnection
                  && AiWorldIdentity.currentWorldKey(Minecraft.getInstance()).equals(requestedWorld)) {
                  for (JsonElement entry : stored) {
                     try {
                        JsonObject saved = entry.getAsJsonObject();
                        String from = AiJson.getString(saved, "fromDimension", "");
                        String to = AiJson.getString(saved, "toDimension", "");
                        BlockPos entrance = parsePos(AiJson.getString(saved, "entrance", ""));
                        BlockPos arrival = parsePos(AiJson.getString(saved, "arrival", ""));
                        if (entrance != null
                           && arrival != null
                           && !from.isEmpty()
                           && !to.isEmpty()
                           && !from.equals(to)
                           && !links.stream()
                              .anyMatch(link -> link.fromDimension.equals(from) && link.toDimension.equals(to) && link.entrance.distSqr(entrance) < 16.0)) {
                           if (links.size() >= 64) {
                              break;
                           }

                           JsonObject before = saved.has("entranceEvidence") && saved.get("entranceEvidence").isJsonObject()
                              ? saved.getAsJsonObject("entranceEvidence")
                              : new JsonObject();
                           JsonObject after = saved.has("arrivalEvidence") && saved.get("arrivalEvidence").isJsonObject()
                              ? saved.getAsJsonObject("arrivalEvidence")
                              : new JsonObject();
                           links.add(
                              new AiPortalLinks.PortalLink(
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

                  links.sort(Comparator.comparingLong(AiPortalLinks.PortalLink::observedAtMillis));
                  service.savePortalMemory(loadedWorldKey, toJson());
               }
            }
         );
      }
   }

   private static BlockPos parsePos(String text) {
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

   static void recordLink(String from, BlockPos entrance, String to, BlockPos arrival, boolean safe, JsonObject entranceEvidence) {
      ensureLoaded();
      if (entrance != null && arrival != null && from != null && to != null && !from.equals(to)) {
         links.removeIf(link -> link.fromDimension.equals(from) && link.entrance.distSqr(entrance) < 16.0 && link.toDimension.equals(to));
         links.add(
            new AiPortalLinks.PortalLink(
               from,
               entrance.immutable(),
               to,
               arrival.immutable(),
               AiThreats.currentClientTick(),
               System.currentTimeMillis(),
               false,
               safe,
               entranceEvidence == null ? new JsonObject() : entranceEvidence.deepCopy(),
               entranceEvidence(arrival)
            )
         );
         AiRunTrace.emitServiceEvent(
            "portal_link",
            AiJson.objectOf("from", from, "entrance", entrance.toShortString(), "to", to, "arrival", arrival.toShortString(), "safe", safe)
         );
         links.sort(Comparator.comparingLong(AiPortalLinks.PortalLink::observedAtMillis));

         while (links.size() > 64) {
            links.remove(0);
         }

         AiService service = AiService.getExistingInstance();
         if (service != null) {
            service.savePortalMemory(loadedWorldKey, toJson());
         }
      }
   }

   static JsonObject entranceEvidence(BlockPos near) {
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
               AiWorldIdentity.currentDimensionId(mc),
               "arrival",
               near.toShortString(),
               "activeCellsInLocalScan",
               active,
               "partialCoverage",
               partial,
               "observedTick",
               AiThreats.currentClientTick(),
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
               AiWorldIdentity.currentDimensionId(mc),
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
               AiThreats.currentClientTick()
            );
         }
      } else {
         return AiJson.objectOf("observed", false);
      }
   }

   static void reset(String resetWorld) {
      if (resetWorld == null || resetWorld.equals(loadedWorldKey)) {
         links.clear();
         memoryLoadStarted = true;
         generation++;
      }
   }

   static String arrivalHazard() {
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
         return autismclient.ai.engine.AiEnvironmentHazards.isHazardous(mc.level, mc.player.blockPosition()) ? "arrival intersects an unmodelled sulfur launch column" : null;
      }
   }

   static JsonObject predictArrival(Vec3 position, String from, String to) {
      ensureLoaded();
      Double sourceScale = coordinateScales.get(from);
      Double targetScale = coordinateScales.get(to);
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

   static JsonArray snapshotJson() {
      ensureLoaded();
      return toJson();
   }

   static BlockPos nearestKnownPortal(int radius) {
      ensureLoaded();
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null
         ? nearestLinkMatching(toJson(), AiWorldIdentity.currentDimensionId(mc), mc.player.blockPosition(), radius, at -> !AiTerrain.isLoaded(mc.level, at) || mc.level.getBlockState(at).is(Blocks.NETHER_PORTAL))
         : null;
   }

   static BlockPos nearestLink(JsonArray records, String dimension, BlockPos origin, int radius) {
      return nearestLinkMatching(records, dimension, origin, radius, at -> true);
   }

   private static BlockPos nearestLinkMatching(JsonArray records, String dimension, BlockPos origin, int radius, Predicate<BlockPos> accepted) {
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
                     BlockPos at = parsePos(AiJson.getString(evidence, "interiorMin", ""));
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

   private static JsonArray toJson() {
      JsonArray out = new JsonArray();

      for (AiPortalLinks.PortalLink link : links) {
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

   record PortalLink(
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
