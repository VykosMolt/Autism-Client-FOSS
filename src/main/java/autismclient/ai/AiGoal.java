package autismclient.ai;

import autismclient.AutismClientAddon;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AiGoal {
   static final long init_J = 1500000000L;
   static final long LABEL_STABLE_NANOS = 3000000000L;
   private static final Map<Object, AiGoal.GoalEntry> goals = new ConcurrentHashMap<>();
   private static final Object VIS_OWNER = new Object();
   private static final AiGoal.StableLabel LABEL = new AiGoal.StableLabel();
   private static volatile AiTaskStatus.Inner_StreamUtil status = AiTaskStatus.Inner_StreamUtil.EMPTY;
   private static volatile long statusAt;
   private static String detail = "";

   private AiGoal() {
   }

   static void set(Object owner, int depth, String objective, String goal, String detail) {
      long now = System.nanoTime();
      AiGoal.GoalEntry old = goals.get(owner);
      String g = oneLine(goal);
      if (g.isEmpty()) {
         g = old == null ? "" : old.goal;
      }

      if (!g.isEmpty()) {
         goals.put(owner, new AiGoal.GoalEntry(depth, oneLine(objective), g, oneLine(detail), now));
         refresh(now);
      }
   }

   static void clear(Object owner) {
      if (goals.remove(owner) != null) {
         refresh(System.nanoTime());
      }
   }

   static AiTaskStatus.Inner_StreamUtil currentStatus() {
      return System.nanoTime() - statusAt > 1500000000L ? AiTaskStatus.Inner_StreamUtil.EMPTY : status;
   }

   static String statusLine(String fallback) {
      AiTaskStatus.Inner_StreamUtil shown = currentStatus();
      return !shown.visible() ? fallback : shown.activity() + (shown.detail().isEmpty() ? "" : " (" + shown.detail() + ")");
   }

   private static synchronized void refresh(long now) {
      goals.values().removeIf(e -> now - e.touched > 1500000000L);
      AiGoal.GoalEntry deepest = null;
      AiGoal.GoalEntry shallowest = null;

      for (AiGoal.GoalEntry e : goals.values()) {
         if (deepest == null || e.depth > deepest.depth || e.depth == deepest.depth && e.touched > deepest.touched) {
            deepest = e;
         }

         if (shallowest == null || e.depth < shallowest.depth) {
            shallowest = e;
         }
      }

      if (deepest == null) {
         LABEL.reset();
         detail = "";
         status = AiTaskStatus.Inner_StreamUtil.EMPTY;
         AiVisualization.clearOwner(VIS_OWNER);
      } else {
         String goal = LABEL.stabilize(deepest.goal, now, 3000000000L);
         if (!goal.equals(status.activity())) {
            AutismClientAddon.LOG.info("[AI] goal: {} - {}", shallowest.objective.isEmpty() ? deepest.objective : shallowest.objective, goal);
         }

         if (goal.equals(deepest.goal)) {
            detail = deepest.detail;
         }

         String title = shallowest.objective.isEmpty() ? deepest.objective : shallowest.objective;
         status = new AiTaskStatus.Inner_StreamUtil(title.isEmpty() ? goal : title, goal, detail);
         statusAt = now;
         AiVisualization.showContent(VIS_OWNER, status);
      }
   }

   static String phaseLabel(String phase) {
      String var10000;
      switch (phase == null ? "" : phase) {
         case "WOOD":
            var10000 = "Gathering wood";
            break;
         case "FOOD":
            var10000 = "Hunting for food";
            break;
         case "KIT":
            var10000 = "Making the diamond kit";
            break;
         case "EQUIP":
            var10000 = "Putting on the kit";
            break;
         case "COOK":
            var10000 = "Cooking food";
            break;
         case "HEAL":
            var10000 = "Recovering health";
            break;
         case "NETHER_ENTRY":
            var10000 = "Getting to the Nether";
            break;
         case "NETHER":
            var10000 = "Getting blaze rods and pearls";
            break;
         case "STRONGHOLD":
            var10000 = "Finding the stronghold";
            break;
         case "END":
            var10000 = "Fighting the dragon";
            break;
         default:
            var10000 = "Starting the run";
      }

      return var10000;
   }

   static String stepLabel(String key) {
      if (key != null && !key.isEmpty()) {
         int colon = key.indexOf(58);
         String kind = colon < 0 ? key : key.substring(0, colon);
         String id = colon < 0 ? "" : key.substring(colon + 1);
         String var10000;
         switch (kind) {
            case "wood":
            case "stock-wood":
            case "fuel-wood":
            case "fuel-planks":
               var10000 = "Gathering wood";
               break;
            case "ore":
               var10000 = "Getting " + write(id);
               break;
            case "fuel-coal":
            case "pack-coal":
               var10000 = "Getting coal";
               break;
            case "smelt":
            case "smelt-load":
            case "collect":
            case "prework":
               var10000 = id.isEmpty() ? "Smelting" : "Smelting " + write(id);
               break;
            case "pick":
            case "axe":
            case "spare-pick":
            case "final":
            case "diamond-pick-early":
            case "shield-early":
            case "table":
            case "furnace":
            case "pack-furnace":
            case "pack-table":
            case "pack-table-completion":
            case "craft-table-completion":
            case "smith":
            case "smith-base":
            case "smith-ingot":
            case "smith-table":
               var10000 = "Making tools and gear";
               break;
            case "food":
               var10000 = "Hunting for food";
               break;
            case "water-early":
            case "expedition-water":
            case "obsidian-water":
               var10000 = "Filling water buckets";
               break;
            case "armour-now":
            case "shield-offhand":
               var10000 = "Putting on gear";
               break;
            case "nether":
               var10000 = "Getting to the Nether";
               break;
            default:
               var10000 = "";
         }

         return var10000;
      } else {
         return "";
      }
   }

   static String write(String id) {
      String s = (id == null ? "" : id).toLowerCase(Locale.ROOT);
      int colon = s.indexOf(58);
      if (colon >= 0) {
         s = s.substring(colon + 1);
      }

      s = s.replace("raw_", "").replace("_ingot", "").replace("_ore", "").replace("deepslate_", "").replace("_nugget", "");
      String var10000;
      switch (s) {
         case "diamond":
            var10000 = "diamonds";
            break;
         case "cobblestone":
         case "cobbled_deepslate":
         case "stone":
         case "blackstone":
            var10000 = "stone";
            break;
         case "lapis_lazuli":
            var10000 = "lapis";
            break;
         case "emerald":
            var10000 = "emeralds";
            break;
         case "":
            var10000 = "materials";
            break;
         default:
            var10000 = s.replace('_', ' ');
      }

      return var10000;
   }

   static String goalLabel(String label, String step) {
      String l = label == null ? "" : label.toLowerCase(Locale.ROOT);
      if (l.contains("blaze") || l.contains("drink fire resistance") || l.contains("lava round the spawner") || l.contains("away from the lava")) {
         return "Getting blaze rods";
      }

      if (!l.contains("nugget") && (!l.contains("gold") || !l.startsWith("mine") && !l.startsWith("extract"))) {
         if (l.contains("barter") || l.contains("piglin")) {
            return "Bartering for pearls";
         }

         if (l.contains("enderman") || l.contains("endermen") || l.contains("warped") || l.contains("pearl") || l.contains("fighting hole")) {
            return "Hunting endermen for pearls";
         }

         if (l.contains("fortress")) {
            return "Finding a fortress";
         }

         if (l.startsWith("walk back") || l.contains("portal") || l.contains("home")) {
            return "Returning to the Overworld";
         }

         if (l.contains("heal") || l.startsWith("eat")) {
            return "Recovering health";
         }

         if (!l.isEmpty()) {
            return "";
         }

         String var10000;
         switch (step == null ? "" : step) {
            case "FORTRESS":
               var10000 = "Finding a fortress";
               break;
            case "RODS":
               var10000 = "Getting blaze rods";
               break;
            case "PEARLS":
               var10000 = "Getting ender pearls";
               break;
            case "RETURN":
               var10000 = "Returning to the Overworld";
               break;
            default:
               var10000 = "";
         }

         return var10000;
      } else {
         return "Gathering gold";
      }
   }

   private static String oneLine(String value) {
      return value == null ? "" : value.strip().replaceAll("[\\r\\n\\t]+", " ");
   }

   private record GoalEntry(int depth, String objective, String goal, String detail, long touched) {
   }

   static final class StableLabel {
      private String shown = "";
      private String pending = "";
      private long pendingSince;

      String stabilize(String next, long now, long hold) {
         if (!this.shown.isEmpty() && !next.equals(this.shown)) {
            if (!next.equals(this.pending)) {
               this.pending = next;
               this.pendingSince = now;
            } else if (now - this.pendingSince >= hold) {
               this.shown = next;
               this.pending = "";
            }

            return this.shown;
         } else {
            this.shown = next;
            this.pending = "";
            return this.shown;
         }
      }

      void reset() {
         this.shown = "";
         this.pending = "";
      }
   }
}
