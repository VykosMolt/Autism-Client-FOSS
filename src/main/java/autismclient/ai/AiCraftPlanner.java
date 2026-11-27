package autismclient.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public final class AiCraftPlanner {
   private static final int init = 8;
   private static final int load = 256;

   private AiCraftPlanner() {
   }

   public static AiCraftPlanner.CraftPlan init(AiCraftPlanner.RecipeGraph graph, String itemId, int quantity) {
      int held = Math.max(0, graph.held(itemId));
      if (quantity > 0 && held < quantity) {
         AiCraftPlanner.StockLedger state = new AiCraftPlanner.StockLedger(graph);
         state.init_Map.put(itemId, held);
         List<AiCraftPlanner.CraftStep> steps = new ArrayList<>();
         Map<String, Integer> missing = new LinkedHashMap<>();
         state.init(itemId, quantity, steps, missing, new HashSet<>(), 0);
         return new AiCraftPlanner.CraftPlan(init_List(steps), missing, held, quantity);
      } else {
         return new AiCraftPlanner.CraftPlan(List.of(), Map.of(), held, quantity);
      }
   }

   static List<AiCraftPlanner.CraftStep> init_List(List<AiCraftPlanner.CraftStep> steps) {
      List<AiCraftPlanner.CraftStep> out = new ArrayList<>();

      for (AiCraftPlanner.CraftStep step : steps) {
         int at = -1;

         for (int j = out.size() - 1; j >= 0; j--) {
            AiCraftPlanner.CraftStep earlier = out.get(j);
            if (earlier.recipe().key().equals(step.recipe().key())) {
               at = j;
               break;
            }

            if (init(step.recipe(), earlier.recipe().output())) {
               break;
            }
         }

         if (at < 0) {
            out.add(step);
         } else {
            out.set(at, new AiCraftPlanner.CraftStep(out.get(at).recipe(), out.get(at).crafts() + step.crafts()));
         }
      }

      return out;
   }

   private static boolean init(AiCraftPlanner.RecipeSpec recipe, String itemId) {
      for (List<String> slot : recipe.inputs()) {
         if (slot.contains(itemId)) {
            return true;
         }
      }

      return false;
   }

   private static int init_I(List<AiCraftPlanner.CraftStep> steps) {
      int cost = 0;

      for (AiCraftPlanner.CraftStep step : init_List(steps)) {
         cost += 10 + step.crafts() + (step.recipe().stonecutting() ? 20 : 0);
      }

      return cost;
   }

   private static Map<List<String>, Integer> init(AiCraftPlanner.RecipeSpec recipe) {
      Map<List<String>, Integer> grouped = new LinkedHashMap<>();

      for (List<String> slot : recipe.inputs()) {
         grouped.merge(List.copyOf(slot), 1, Integer::sum);
      }

      return grouped;
   }

   private static List<AiCraftPlanner.RecipeSpec> load(List<AiCraftPlanner.RecipeSpec> recipes) {
      List<AiCraftPlanner.RecipeSpec> copy = new ArrayList<>(recipes);
      copy.sort(Comparator.<AiCraftPlanner.RecipeSpec>comparingInt(recipe -> init(recipe).size()).thenComparingInt(recipe -> recipe.inputs().size()));
      return copy;
   }

   public record RecipeSpec(String key, String output, int outputCount, boolean requiresTable, List<List<String>> inputs, String station) {
      public RecipeSpec(String key, String output, int outputCount, boolean requiresTable, List<List<String>> inputs) {
         this(key, output, outputCount, requiresTable, inputs, "");
      }

      public RecipeSpec {
         inputs = List.copyOf(inputs);
         station = station == null ? "" : station;
      }

      public boolean stonecutting() {
         return this.station.equals("minecraft:stonecutter");
      }
   }

   public record CraftPlan(List<AiCraftPlanner.CraftStep> steps, Map<String, Integer> missing, int alreadyHeld, int target) {
      public CraftPlan {
         steps = List.copyOf(steps);
         missing = Map.copyOf(missing);
      }

      public boolean complete() {
         return this.missing.isEmpty();
      }

      public boolean satisfied() {
         return this.steps.isEmpty() && this.missing.isEmpty();
      }

      public boolean requiresTable() {
         for (AiCraftPlanner.CraftStep step : this.steps) {
            if (step.recipe().requiresTable()) {
               return true;
            }
         }

         return false;
      }
   }

   private static final class StockLedger {
      private final AiCraftPlanner.RecipeGraph init_g_StreamUtil;
      private final Map<String, Integer> init_Map = new HashMap<>();

      StockLedger(AiCraftPlanner.RecipeGraph graph) {
         this.init_g_StreamUtil = graph;
      }

      int init_I(String itemId) {
         return this.init_Map.computeIfAbsent(itemId, id -> Math.max(0, this.init_g_StreamUtil.held(id)));
      }

      void init(String itemId, int amount) {
         this.init_Map.put(itemId, this.init_I(itemId) - amount);
      }

      void load(String itemId, int amount) {
         this.init_Map.put(itemId, this.init_I(itemId) + amount);
      }

      int init(String itemId, int need, List<AiCraftPlanner.CraftStep> steps, Map<String, Integer> missing, Set<String> active, int depth) {
         if (need <= 0) {
            return 0;
         }

         int fromStock = Math.min(need, this.init_I(itemId));
         if (fromStock > 0) {
            this.init(itemId, fromStock);
            need -= fromStock;
         }

         if (need == 0) {
            return 0;
         }

         if (depth < 8 && steps.size() < 256 && active.add(itemId)) {
            AiCraftPlanner.PlanBranch best = null;
            AiCraftPlanner.PlanBranch complete = null;

            try {
               for (AiCraftPlanner.RecipeSpec recipe : AiCraftPlanner.load(this.init_g_StreamUtil.recipesFor(itemId))) {
                  int perCraft = Math.max(1, recipe.outputCount());
                  int crafts = (need + perCraft - 1) / perCraft;
                  List<AiCraftPlanner.CraftStep> attemptSteps = new ArrayList<>();
                  Map<String, Integer> attemptMissing = new LinkedHashMap<>();
                  Map<String, Integer> snapshot = new HashMap<>(this.init_Map);
                  int shortfall = this.init(recipe, crafts, attemptSteps, attemptMissing, active, depth);
                  if (shortfall == 0) {
                     attemptSteps.add(new AiCraftPlanner.CraftStep(recipe, crafts));
                     this.load(itemId, crafts * perCraft - need);
                     AiCraftPlanner.PlanBranch candidate = new AiCraftPlanner.PlanBranch(0, attemptSteps, Map.of(), new HashMap<>(this.init_Map));
                     if (complete == null || AiCraftPlanner.init_I(candidate.steps()) < AiCraftPlanner.init_I(complete.steps())) {
                        complete = candidate;
                     }

                     this.init_Map.clear();
                     this.init_Map.putAll(snapshot);
                  } else {
                     boolean cutOfAMinedItem = recipe.stonecutting() && this.init_g_StreamUtil.load_I(itemId) == 0;
                     if (this.init_g_StreamUtil.init_Z(itemId)) {
                        cutOfAMinedItem = true;
                     }

                     boolean circularAcquisition = attemptMissing.containsKey(itemId);
                     if (!cutOfAMinedItem
                        && !circularAcquisition
                        && (
                           best == null
                              || shortfall < best.shortfall()
                              || shortfall == best.shortfall() && this.init(attemptMissing) < this.init(best.missing())
                        )) {
                        best = new AiCraftPlanner.PlanBranch(shortfall, attemptSteps, attemptMissing, new HashMap<>(this.init_Map));
                     }

                     this.init_Map.clear();
                     this.init_Map.putAll(snapshot);
                  }
               }
            } finally {
               active.remove(itemId);
            }

            if (complete != null) {
               this.init_Map.clear();
               this.init_Map.putAll(complete.stock());
               steps.addAll(complete.steps());
               return 0;
            } else if (best != null && !best.missing().isEmpty()) {
               this.init_Map.clear();
               this.init_Map.putAll(best.stock());
               steps.addAll(best.steps());
               best.missing().forEach((id, amount) -> missing.merge(id, amount, Integer::sum));
               return need;
            } else {
               missing.merge(itemId, need, Integer::sum);
               return need;
            }
         } else {
            missing.merge(itemId, need, Integer::sum);
            return need;
         }
      }

      private double init(Map<String, Integer> missing) {
         double cost = 0.0;

         for (Entry<String, Integer> entry : missing.entrySet()) {
            cost += entry.getValue().intValue() * this.init_g_StreamUtil.init_D(entry.getKey());
         }

         return cost;
      }

      private int init(AiCraftPlanner.RecipeSpec recipe, int crafts, List<AiCraftPlanner.CraftStep> steps, Map<String, Integer> missing, Set<String> active, int depth) {
         int shortfall = 0;
         Iterator var8 = AiCraftPlanner.init(recipe).entrySet().iterator();

         while (true) {
            Entry<List<String>, Integer> requirement;
            int needed;
            String choice;
            while (true) {
               if (!var8.hasNext()) {
                  return shortfall;
               }

               requirement = (Entry<List<String>, Integer>)var8.next();
               needed = requirement.getValue() * crafts;
               choice = null;
               if (requirement.getKey().size() <= 1) {
                  break;
               }

               List<String> byStock = new ArrayList<>(requirement.getKey());
               byStock.sort((a, b) -> Integer.compare(this.init_I(b), this.init_I(a)));
               String topped = this.init_I(byStock.get(0)) > 0 ? byStock.get(0) : null;

               for (String option : byStock) {
                  int take = Math.min(needed, this.init_I(option));
                  if (take <= 0) {
                     break;
                  }

                  this.init(option, take);
                  needed -= take;
               }

               if (needed > 0) {
                  for (String option : requirement.getKey()) {
                     if (this.init_Z(option)) {
                        choice = option;
                        break;
                     }
                  }

                  if (choice == null) {
                     choice = topped;
                  }
                  break;
               }
            }

            if (choice == null) {
               choice = this.init(requirement.getKey(), needed);
            }

            if (choice == null) {
               shortfall += needed;
               missing.merge(requirement.getKey().get(0), needed, Integer::sum);
            } else {
               shortfall += this.init(choice, needed, steps, missing, active, depth + 1);
            }
         }
      }

      private String init(List<String> accepted, int needed) {
         String best = null;
         int bestStock = -1;

         for (String option : accepted) {
            int have = this.init_I(option);
            if (have > bestStock) {
               bestStock = have;
               best = option;
            }
         }

         if (bestStock >= needed) {
            return best;
         }

         int bestRank = 2147483647;

         for (String option : accepted) {
            bestRank = Math.min(bestRank, this.init_g_StreamUtil.load_I(option));
         }

         List<String> tied = new ArrayList<>();

         for (String option : accepted) {
            if (this.init_g_StreamUtil.load_I(option) == bestRank) {
               tied.add(option);
            }
         }

         if (tied.size() > 1) {
            int tiedStock = 0;

            for (String option : tied) {
               tiedStock = Math.max(tiedStock, this.init_I(option));
            }

            if (tiedStock == 0) {
               for (String option : tied) {
                  if (this.init_Z(option)) {
                     return option;
                  }
               }

               String family = this.init_g_StreamUtil.init(new HashSet<>(tied));
               if (family != null) {
                  return family;
               }
            }
         }

         if (tied.size() == 1) {
            return tied.getFirst();
         }

         String pick = null;
         int pickStock = -1;
         double pickCost = 1.7976931348623157E308;

         for (String option : tied) {
            int have = this.init_I(option);
            double cost = this.init_g_StreamUtil.init_D(option);
            if (have > pickStock || have == pickStock && cost < pickCost) {
               pickStock = have;
               pickCost = cost;
               pick = option;
            }
         }

         return pick != null ? pick : best;
      }

      private boolean init_Z(String itemId) {
         for (AiCraftPlanner.RecipeSpec recipe : this.init_g_StreamUtil.recipesFor(itemId)) {
            boolean covered = !recipe.inputs().isEmpty();

            for (List<String> slot : recipe.inputs()) {
               boolean slotCovered = false;

               for (String option : slot) {
                  if (this.init_I(option) > 0) {
                     slotCovered = true;
                     break;
                  }
               }

               if (!slotCovered) {
                  covered = false;
                  break;
               }
            }

            if (covered) {
               return true;
            }
         }

         return false;
      }
   }

   private record PlanBranch(int shortfall, List<AiCraftPlanner.CraftStep> steps, Map<String, Integer> missing, Map<String, Integer> stock) {
   }

   public interface RecipeGraph {
      List<AiCraftPlanner.RecipeSpec> recipesFor(String var1);

      int held(String var1);

      default int load_I(String itemId) {
         return this.recipesFor(itemId).isEmpty() ? 2 : 1;
      }

      default String init(Set<String> variantIds) {
         return null;
      }

      default double init_D(String itemId) {
         return 1.0;
      }

      default boolean init_Z(String itemId) {
         return false;
      }
   }

   public record CraftStep(AiCraftPlanner.RecipeSpec recipe, int crafts) {
      public int produced() {
         return this.crafts * Math.max(1, this.recipe.outputCount());
      }
   }
}
