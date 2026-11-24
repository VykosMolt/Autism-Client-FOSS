package autismclient.util.g;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

final class StateCache {
   private static final int init_I = 512;
   private static final double init_D = 2.44140625E-4;
   private final Map<Integer, StateCache.Inner_DataUtil> init_Map = new ConcurrentHashMap<>();
   private final Map<UUID, Integer> load = new ConcurrentHashMap<>();

   void init_V_I(int id, String type, double x, double y, double z) {
      this.init(id, null, type, x, y, z, Vec3.ZERO, 0.0F, 0.0F, 0.0F, false, -1);
   }

   void init(int id, UUID uuid, String type, double x, double y, double z, Vec3 movement, float yRot, float xRot, float headYRot, boolean onGround, int ownerId) {
      this.init(id, uuid, type, x, y, z, movement, yRot, xRot, headYRot, onGround, ownerId, null);
   }

   void init(
      int id,
      UUID uuid,
      String type,
      double x,
      double y,
      double z,
      Vec3 movement,
      float yRot,
      float xRot,
      float headYRot,
      boolean onGround,
      int ownerId,
      Vec3 focus
   ) {
      String safeType = type == null ? "" : type;
      Vec3 newPosition = new Vec3(x, y, z);
      if (this.init_Map.size() < 512 || this.init_Map.containsKey(id) || this.init(safeType, newPosition, focus)) {
         StateCache.Inner_DataUtil state = new StateCache.Inner_DataUtil(
            id, uuid, safeType, newPosition, movement == null ? Vec3.ZERO : movement, yRot, xRot, headYRot, onGround, ownerId, -1
         );
         StateCache.Inner_DataUtil replaced = this.init_Map.put(id, state);
         if (replaced != null && replaced.init_UUID() != null && !replaced.init_UUID().equals(uuid)) {
            this.load.remove(replaced.init_UUID(), id);
         }

         if (uuid != null) {
            Integer oldId = this.load.put(uuid, id);
            if (oldId != null && oldId != id) {
               StateCache.Inner_DataUtil stale = this.init_Map.get(oldId);
               if (stale != null && uuid.equals(stale.init_UUID())) {
                  this.init_Map.remove(oldId, stale);
               }
            }
         }
      }
   }

   private boolean init(String newType, Vec3 newPosition, Vec3 focus) {
      int newRank = init_I_Str(newType);
      StateCache.Inner_DataUtil victim = null;
      int victimRank = -1;
      double victimDistance = -1.0;
      double newDistance = focus == null ? 1.0 / 0.0 : newPosition.distanceToSqr(focus);

      for (StateCache.Inner_DataUtil value : this.init_Map.values()) {
         int rank = init_I_Str(value.init_Str());
         double distance = focus == null ? 0.0 : value.init_Vec3().distanceToSqr(focus);
         boolean lowerPriority = rank > newRank;
         boolean sameButFarther = rank == newRank && focus != null && distance > newDistance;
         if ((lowerPriority || sameButFarther) && (victim == null || rank > victimRank || rank == victimRank && distance > victimDistance)) {
            victim = value;
            victimRank = rank;
            victimDistance = distance;
         }
      }

      if (victim == null) {
         return false;
      }

      StateCache.Inner_DataUtil removed = this.init_Map.remove(victim.init_I());
      if (removed == null) {
         return false;
      }

      if (removed.init_UUID() != null) {
         this.load.remove(removed.init_UUID(), removed.init_I());
      }

      return true;
   }

   private static int init_I_Str(String type) {
      String normalized = init_Str_Str(type);
      if (!normalized.equals("player") && !normalized.equals("fishing bobber")) {
         return !normalized.equals("item")
               && !normalized.equals("experience orb")
               && !normalized.contains("arrow")
               && !normalized.contains("projectile")
               && !normalized.contains("display")
               && !normalized.equals("falling block")
               && !normalized.equals("area effect cloud")
               && !normalized.equals("firework rocket")
            ? 1
            : 2;
      } else {
         return 0;
      }
   }

   void init(int id, double x, double y, double z) {
      this.init_Map
         .computeIfPresent(
            id, (ignored, old) -> init(old, new Vec3(x, y, z), old.load_Vec3(), old.init_F(), old.load_F(), old.read_F(), old.init_Z(), old.read_I())
         );
   }

   void init(int id, short xa, short ya, short za, boolean hasPosition, float yRot, float xRot, boolean hasRotation, boolean onGround) {
      this.init_Map.computeIfPresent(id, (ignored, old) -> {
         Vec3 pos = old.init_Vec3();
         if (hasPosition) {
            pos = pos.add(xa * 2.44140625E-4, ya * 2.44140625E-4, za * 2.44140625E-4);
         }

         return init(old, pos, old.load_Vec3(), hasRotation ? yRot : old.init_F(), hasRotation ? xRot : old.load_F(), old.read_F(), onGround, old.read_I());
      });
   }

   void init(int id, PositionMoveRotation values, boolean onGround) {
      if (values != null) {
         this.init_Map
            .computeIfPresent(
               id, (ignored, old) -> init(old, values.position(), values.deltaMovement(), values.yRot(), values.xRot(), old.read_F(), onGround, old.read_I())
            );
      }
   }

   void init_V_I_2(int id, Vec3 position, Vec3 movement, float yRot, float xRot) {
      if (position != null) {
         this.init_Map
            .computeIfPresent(
               id, (ignored, old) -> init(old, position, movement == null ? old.load_Vec3() : movement, yRot, xRot, old.read_F(), old.init_Z(), old.read_I())
            );
      }
   }

   void init(int id, PositionMoveRotation change, Set<Relative> relatives, boolean onGround) {
      if (change != null) {
         Set<Relative> relativeSet = relatives == null ? Set.of() : relatives;
         this.init_Map.computeIfPresent(id, (ignored, old) -> {
            PositionMoveRotation absolute = PositionMoveRotation.calculateAbsolute(old.init_PositionMoveRotati(), change, relativeSet);
            return init(old, absolute.position(), absolute.deltaMovement(), absolute.yRot(), absolute.xRot(), old.read_F(), onGround, old.read_I());
         });
      }
   }

   void init_V_I_Vec3(int id, Vec3 movement) {
      if (movement != null) {
         this.init_Map
            .computeIfPresent(id, (ignored, old) -> init(old, old.init_Vec3(), movement, old.init_F(), old.load_F(), old.read_F(), old.init_Z(), old.read_I()));
      }
   }

   void init_V_I_F(int id, float headYRot) {
      this.init_Map
         .computeIfPresent(id, (ignored, old) -> init(old, old.init_Vec3(), old.load_Vec3(), old.init_F(), old.load_F(), headYRot, old.init_Z(), old.read_I()));
   }

   void init_V_I_I(int hookId, int encodedTargetId) {
      int targetId = encodedTargetId > 0 ? encodedTargetId - 1 : -1;
      this.init_Map
         .computeIfPresent(
            hookId, (ignored, old) -> init(old, old.init_Vec3(), old.load_Vec3(), old.init_F(), old.load_F(), old.read_F(), old.init_Z(), targetId)
         );
   }

   StateCache.Inner_DataUtil init_StateCache_DataUti_I(int id) {
      return this.init_Map.get(id);
   }

   StateCache.Inner_DataUtil init_StateCache_DataUti_UUID(UUID uuid) {
      if (uuid == null) {
         return null;
      }

      Integer id = this.load.get(uuid);
      if (id == null) {
         return null;
      }

      StateCache.Inner_DataUtil state = this.init_Map.get(id);
      if (state != null && uuid.equals(state.init_UUID())) {
         return state;
      }

      this.load.remove(uuid, id);
      return null;
   }

   Iterable<StateCache.Inner_DataUtil> init_Iterable() {
      return this.init_Map.values();
   }

   int init_I() {
      return this.init_Map.size();
   }

   private static StateCache.Inner_DataUtil init(
      StateCache.Inner_DataUtil old, Vec3 position, Vec3 movement, float yRot, float xRot, float headYRot, boolean onGround, int hookedId
   ) {
      return new StateCache.Inner_DataUtil(
         old.init_I(), old.init_UUID(), old.init_Str(), position, movement, yRot, xRot, headYRot, onGround, old.load_I(), hookedId
      );
   }

   void init_V_I(int id) {
      StateCache.Inner_DataUtil removed = this.init_Map.remove(id);
      if (removed != null && removed.init_UUID() != null) {
         this.load.remove(removed.init_UUID(), id);
      }
   }

   void init_V() {
      this.init_Map.clear();
      this.load.clear();
   }

   int init_I_Str_Vec3(String typeQuery, Vec3 from) {
      String q = init_Str_Str(typeQuery);
      int best = -1;
      double bestDist = 1.7976931348623157E308;

      for (StateCache.Inner_DataUtil value : this.init_Map.values()) {
         if (q.isEmpty() || init_Str_Str(value.init_Str()).contains(q)) {
            double distance = value.init_Vec3().distanceToSqr(from);
            if (distance < bestDist) {
               bestDist = distance;
               best = value.init_I();
            }
         }
      }

      return best;
   }

   double[] init_DArr_I(int id) {
      StateCache.Inner_DataUtil value = this.init_Map.get(id);
      return value == null ? null : new double[]{value.init_Vec3().x, value.init_Vec3().y, value.init_Vec3().z};
   }

   String init_Str_I(int id) {
      StateCache.Inner_DataUtil value = this.init_Map.get(id);
      return value == null ? null : value.init_Str();
   }

   boolean init(List<String> typeQueries, boolean containerOnly, double cx, double cy, double cz, double radius) {
      double radiusSq = radius * radius;
      ArrayList<String> queries = new ArrayList<>();
      if (typeQueries != null) {
         for (String query : typeQueries) {
            if (query != null && !query.isBlank() && !query.startsWith("~")) {
               queries.add(init_Str_Str(query));
            }
         }
      }

      for (StateCache.Inner_DataUtil value : this.init_Map.values()) {
         Vec3 pos = value.init_Vec3();
         double dx = pos.x - cx;
         double dy = pos.y - cy;
         double dz = pos.z - cz;
         if (!(dx * dx + dy * dy + dz * dz > radiusSq)) {
            String type = init_Str_Str(value.init_Str());
            if (!containerOnly || type.contains("boat") || type.contains("minecart") || type.contains("llama") || type.contains("chest")) {
               if (queries.isEmpty()) {
                  return true;
               }

               for (String query : queries) {
                  if (type.contains(query)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private static String init_Str_Str(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      int colon = normalized.indexOf(58);
      if (colon >= 0) {
         normalized = normalized.substring(colon + 1);
      }

      return normalized.replace('_', ' ');
   }

   record Inner_DataUtil(
      int init_I,
      UUID init_UUID,
      String init_Str,
      Vec3 init_Vec3,
      Vec3 load_Vec3,
      float init_F,
      float load_F,
      float read_F,
      boolean init_Z,
      int load_I,
      int read_I
   ) {
      PositionMoveRotation init_PositionMoveRotati() {
         return new PositionMoveRotation(this.init_Vec3, this.load_Vec3, this.init_F, this.load_F);
      }
   }
}
