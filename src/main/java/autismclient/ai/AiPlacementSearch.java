package autismclient.ai;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;

final class AiPlacementSearch {
   private final List<AiPlacementSearch.PlacementCandidate> init_List;
   private final AiPlacementSearch.WorldProbe world;
   private final boolean init_Z;
   private final Map<List<AiBuilder.CellSpec>, AiPlacementSearch.CellTemplate> init_Map = new IdentityHashMap<>();
   private int init_I;
   private int load_I;
   private int cursorY;
   private int cursorZ;
   private int sizeX;
   private int sizeY;
   private int sizeZ;
   private int close;
   private int open;
   private int poll;
   private int push;
   private Map<Long, AiBuilder.CellSpec> load_Map;
   private AiPlacementSearch.CandidateScore best;
   private boolean load_Z;
   private long init_J;

   AiPlacementSearch(List<AiPlacementSearch.PlacementCandidate> candidates, AiPlacementSearch.WorldProbe world, boolean clearTerrain) {
      this.init_List = List.copyOf(candidates);
      this.world = world;
      this.init_Z = clearTerrain;
   }

   void step(int budget, long deadline) {
      while (!this.load_Z && budget-- > 0 && System.nanoTime() < deadline) {
         if (this.load_Map == null) {
            if (this.init_I >= this.init_List.size()) {
               this.load_Z = true;
               break;
            }

            AiPlacementSearch.PlacementCandidate c = this.init_List.get(this.init_I);
            AiPlacementSearch.CellTemplate template = this.init_Map.get(c.cells());
            if (template == null) {
               Map<Long, AiBuilder.CellSpec> map = new HashMap<>();
               int w = 0;
               int h = 0;
               int d = 0;

               for (AiBuilder.CellSpec cell : c.cells()) {
                  map.put(packKey(cell.dx(), cell.dy(), cell.dz()), cell);
                  w = Math.max(w, cell.dx() + 1);
                  h = Math.max(h, cell.dy() + 1);
                  d = Math.max(d, cell.dz() + 1);
               }

               template = new AiPlacementSearch.CellTemplate(map, w, h, d);
               this.init_Map.put(c.cells(), template);
            }

            this.load_Map = template.cells();
            this.sizeX = template.width();
            this.sizeY = template.height();
            this.sizeZ = template.depth();
            this.load_I = this.cursorY = this.cursorZ = this.close = this.open = this.poll = this.push = 0;
            if (this.load_Map.isEmpty() || (long)this.sizeX * this.sizeY * this.sizeZ > 1000000L) {
               this.init_V();
               continue;
            }
         }

         AiPlacementSearch.PlacementCandidate candidate = this.init_List.get(this.init_I);
         AiBuilder.CellSpec cell = this.load_Map.get(packKey(this.load_I, this.cursorY, this.cursorZ));
         BlockPos pos = candidate.origin().offset(this.load_I, this.cursorY, this.cursorZ);
         AiPlacementSearch.CellEdit edit = this.world.classify(pos, cell);
         this.init_J++;
         if (edit == AiPlacementSearch.CellEdit.UNSAFE) {
            this.init_V();
         } else {
            if (cell != null) {
               if (edit == AiPlacementSearch.CellEdit.MATCH) {
                  this.push++;
               } else {
                  this.close++;
                  if (edit == AiPlacementSearch.CellEdit.BREAK) {
                     this.open++;
                  }

                  if (!this.load_Map.containsKey(packKey(this.load_I, this.cursorY - 1, this.cursorZ)) && this.world.init_Z(pos, cell)) {
                     this.poll++;
                  }
               }
            } else if (this.init_Z && edit == AiPlacementSearch.CellEdit.BREAK) {
               this.open++;
            }

            if (this.best != null && this.close + 3L * this.open + 2L * this.poll > this.best.work()) {
               this.init_V();
            } else {
               if (++this.load_I == this.sizeX) {
                  this.load_I = 0;
                  if (++this.cursorZ == this.sizeZ) {
                     this.cursorZ = 0;
                     this.cursorY++;
                  }
               }

               if (this.cursorY == this.sizeY) {
                  AiPlacementSearch.CandidateScore score = new AiPlacementSearch.CandidateScore(candidate, this.close, this.open, this.poll, this.push);
                  if (this.best == null || score.compareTo(this.best) < 0) {
                     this.best = score;
                  }

                  this.init_V();
               }
            }
         }
      }
   }

   private void init_V() {
      this.init_I++;
      this.load_Map = null;
   }

   private static long packKey(int x, int y, int z) {
      return (x & 65535L) << 32 | (y & 65535L) << 16 | z & 65535L;
   }

   AiPlacementSearch.CandidateScore getResult() {
      return this.best;
   }

   boolean isDone() {
      return this.load_Z;
   }

   int comparedCount() {
      return Math.min(this.init_I, this.init_List.size());
   }

   int candidateCount() {
      return this.init_List.size();
   }

   long init_J() {
      return this.init_J;
   }

   private record CellTemplate(Map<Long, AiBuilder.CellSpec> cells, int width, int height, int depth) {
   }

   record CandidateScore(AiPlacementSearch.PlacementCandidate candidate, int place, int remove, int support, int matched) implements Comparable<AiPlacementSearch.CandidateScore> {
      long work() {
         return this.place + 3L * this.remove + 2L * this.support;
      }

      public int compareTo(AiPlacementSearch.CandidateScore other) {
         int cost = Long.compare(this.work(), other.work());
         return cost != 0 ? cost : Double.compare(this.candidate.distance(), other.candidate.distance());
      }
   }

   interface WorldProbe {
      AiPlacementSearch.CellEdit classify(BlockPos var1, AiBuilder.CellSpec var2);

      boolean init_Z(BlockPos var1, AiBuilder.CellSpec var2);
   }

   record PlacementCandidate(BlockPos origin, int rotation, List<AiBuilder.CellSpec> cells, double distance) {
   }

   enum CellEdit {
      MATCH,
      PLACE,
      BREAK,
      UNSAFE;
   }
}
