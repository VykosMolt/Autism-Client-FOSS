package autismclient.ai;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;

final class AiPlacementSearch {
   private final List<AiPlacementSearch.PlacementCandidate> candidates;
   private final AiPlacementSearch.WorldProbe world;
   private final boolean clearTerrain;
   private final Map<List<AiBuilder.CellSpec>, AiPlacementSearch.CellTemplate> templateCache = new IdentityHashMap<>();
   private int candidateIndex;
   private int cursorX;
   private int cursorY;
   private int cursorZ;
   private int sizeX;
   private int sizeY;
   private int sizeZ;
   private int changedCells;
   private int breakCount;
   private int unsupportedCount;
   private int matchedCount;
   private Map<Long, AiBuilder.CellSpec> currentCells;
   private AiPlacementSearch.CandidateScore best;
   private boolean done;
   private long classifiedCells;

   AiPlacementSearch(List<AiPlacementSearch.PlacementCandidate> candidates, AiPlacementSearch.WorldProbe world, boolean clearTerrain) {
      this.candidates = List.copyOf(candidates);
      this.world = world;
      this.clearTerrain = clearTerrain;
   }

   void step(int budget, long deadline) {
      while (!this.done && budget-- > 0 && System.nanoTime() < deadline) {
         if (this.currentCells == null) {
            if (this.candidateIndex >= this.candidates.size()) {
               this.done = true;
               break;
            }

            AiPlacementSearch.PlacementCandidate c = this.candidates.get(this.candidateIndex);
            AiPlacementSearch.CellTemplate template = this.templateCache.get(c.cells());
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
               this.templateCache.put(c.cells(), template);
            }

            this.currentCells = template.cells();
            this.sizeX = template.width();
            this.sizeY = template.height();
            this.sizeZ = template.depth();
            this.cursorX = this.cursorY = this.cursorZ = this.changedCells = this.breakCount = this.unsupportedCount = this.matchedCount = 0;
            if (this.currentCells.isEmpty() || (long)this.sizeX * this.sizeY * this.sizeZ > 1000000L) {
               this.nextCandidate();
               continue;
            }
         }

         AiPlacementSearch.PlacementCandidate candidate = this.candidates.get(this.candidateIndex);
         AiBuilder.CellSpec cell = this.currentCells.get(packKey(this.cursorX, this.cursorY, this.cursorZ));
         BlockPos pos = candidate.origin().offset(this.cursorX, this.cursorY, this.cursorZ);
         AiPlacementSearch.CellEdit edit = this.world.classify(pos, cell);
         this.classifiedCells++;
         if (edit == AiPlacementSearch.CellEdit.UNSAFE) {
            this.nextCandidate();
         } else {
            if (cell != null) {
               if (edit == AiPlacementSearch.CellEdit.MATCH) {
                  this.matchedCount++;
               } else {
                  this.changedCells++;
                  if (edit == AiPlacementSearch.CellEdit.BREAK) {
                     this.breakCount++;
                  }

                  if (!this.currentCells.containsKey(packKey(this.cursorX, this.cursorY - 1, this.cursorZ)) && this.world.init_Z(pos, cell)) {
                     this.unsupportedCount++;
                  }
               }
            } else if (this.clearTerrain && edit == AiPlacementSearch.CellEdit.BREAK) {
               this.breakCount++;
            }

            if (this.best != null && this.changedCells + 3L * this.breakCount + 2L * this.unsupportedCount > this.best.work()) {
               this.nextCandidate();
            } else {
               if (++this.cursorX == this.sizeX) {
                  this.cursorX = 0;
                  if (++this.cursorZ == this.sizeZ) {
                     this.cursorZ = 0;
                     this.cursorY++;
                  }
               }

               if (this.cursorY == this.sizeY) {
                  AiPlacementSearch.CandidateScore score = new AiPlacementSearch.CandidateScore(candidate, this.changedCells, this.breakCount, this.unsupportedCount, this.matchedCount);
                  if (this.best == null || score.compareTo(this.best) < 0) {
                     this.best = score;
                  }

                  this.nextCandidate();
               }
            }
         }
      }
   }

   private void nextCandidate() {
      this.candidateIndex++;
      this.currentCells = null;
   }

   private static long packKey(int x, int y, int z) {
      return (x & 65535L) << 32 | (y & 65535L) << 16 | z & 65535L;
   }

   AiPlacementSearch.CandidateScore getResult() {
      return this.best;
   }

   boolean isDone() {
      return this.done;
   }

   int comparedCount() {
      return Math.min(this.candidateIndex, this.candidates.size());
   }

   int candidateCount() {
      return this.candidates.size();
   }

   long init_J() {
      return this.classifiedCells;
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
