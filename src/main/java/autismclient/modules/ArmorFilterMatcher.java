package autismclient.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class ArmorFilterMatcher {
   static final int init_I = 0;
   static final int load_I = 1;
   static final int read_I = 2;
   static final String init_Str = "Any";
   static final List<String> BASE_OPTIONS = List.of("Any", "Empty", "Armor", "Leather", "Chain", "Copper", "Iron", "Gold", "Diamond", "Netherite");
   static final List<String> HEAD_OPTIONS = init_List_List_StrArr(BASE_OPTIONS, "Turtle", "Pumpkin", "Skull");
   static final List<String> CHEST_OPTIONS = init_List_List_StrArr(BASE_OPTIONS, "Elytra");

   private ArmorFilterMatcher() {
   }

   static boolean init_Z_UUID(UUID uuid) {
      return uuid != null && uuid.version() == 2 && uuid.variant() == 2;
   }

   static int init_I_I(int vl, boolean claimsGround, boolean supported) {
      if (claimsGround && !supported) {
         return vl + 1;
      } else {
         return !claimsGround ? vl / 2 : Math.max(0, vl - 1);
      }
   }

   static Map<Integer, Integer> init_Map_List(List<ArmorFilterMatcher.Inner_DataUtil> identities) {
      Map<String, List<ArmorFilterMatcher.Inner_DataUtil>> byName = new HashMap<>();

      for (ArmorFilterMatcher.Inner_DataUtil id : identities) {
         if (id.init_Str() != null && id.init_UUID() != null) {
            byName.computeIfAbsent(id.init_Str(), k -> new ArrayList<>()).add(id);
         }
      }

      Map<Integer, Integer> out = new HashMap<>();

      for (List<ArmorFilterMatcher.Inner_DataUtil> group : byName.values()) {
         if (group.size() >= 2 && init_I_List(group) >= 2) {
            List<ArmorFilterMatcher.Inner_DataUtil> open = new ArrayList<>();

            for (ArmorFilterMatcher.Inner_DataUtil id : group) {
               if (!id.sync() && !id.load_Z()) {
                  open.add(id);
               }
            }

            boolean anyProven = false;

            for (ArmorFilterMatcher.Inner_DataUtil id : group) {
               anyProven |= id.load_Z() && !id.sync();
            }

            if (anyProven) {
               for (ArmorFilterMatcher.Inner_DataUtil id : open) {
                  if (id.init_Z_2()) {
                     out.put(id.load_I(), 2);
                  }
               }
            } else if (open.size() >= 2) {
               int best = -2147483648;

               for (ArmorFilterMatcher.Inner_DataUtil id : open) {
                  best = Math.max(best, id.init_I_2());
               }

               List<ArmorFilterMatcher.Inner_DataUtil> top = new ArrayList<>();

               for (ArmorFilterMatcher.Inner_DataUtil id : open) {
                  if (id.init_I_2() == best) {
                     top.add(id);
                  }
               }

               int topEntities = 0;

               for (ArmorFilterMatcher.Inner_DataUtil id : top) {
                  if (id.init_Z_2()) {
                     topEntities++;
                  }
               }

               for (ArmorFilterMatcher.Inner_DataUtil id : open) {
                  if (id.init_Z_2()) {
                     boolean isTop = id.init_I_2() == best;
                     if (!isTop || top.size() > 1 && topEntities > 1) {
                        out.put(id.load_I(), 1);
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   private static int init_I_List(List<ArmorFilterMatcher.Inner_DataUtil> group) {
      Set<UUID> seen = new HashSet<>();

      for (ArmorFilterMatcher.Inner_DataUtil id : group) {
         seen.add(id.init_UUID());
      }

      return seen.size();
   }

   private static List<String> init_List_List_StrArr(List<String> base, String... more) {
      List<String> out = new ArrayList<>(base);
      out.addAll(List.of(more));
      return List.copyOf(out);
   }

   static boolean matches(String rule, String itemPath) {
      if (rule != null && !rule.equals("Any")) {
         String id = itemPath == null ? "" : itemPath;
         boolean empty = id.isEmpty() || id.equals("air");
         boolean var10000;
         switch (rule) {
            case "Empty":
               var10000 = empty;
               break;
            case "Armor":
               var10000 = !empty && (init_Z_Str(id) || id.equals("elytra"));
               break;
            case "Leather":
               var10000 = load(id, "leather_");
               break;
            case "Chain":
               var10000 = load(id, "chainmail_");
               break;
            case "Copper":
               var10000 = load(id, "copper_");
               break;
            case "Iron":
               var10000 = load(id, "iron_");
               break;
            case "Gold":
               var10000 = load(id, "golden_");
               break;
            case "Diamond":
               var10000 = load(id, "diamond_");
               break;
            case "Netherite":
               var10000 = load(id, "netherite_");
               break;
            case "Turtle":
               var10000 = id.equals("turtle_helmet");
               break;
            case "Pumpkin":
               var10000 = id.equals("carved_pumpkin");
               break;
            case "Skull":
               var10000 = id.endsWith("_skull") || id.endsWith("_head");
               break;
            case "Elytra":
               var10000 = id.equals("elytra");
               break;
            default:
               var10000 = true;
         }

         return var10000;
      } else {
         return true;
      }
   }

   private static boolean init_Z_Str(String id) {
      return id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots");
   }

   private static boolean load(String id, String prefix) {
      return id.startsWith(prefix) && init_Z_Str(id);
   }

   record Inner_DataUtil(UUID init_UUID, String init_Str, int init_I, boolean init_Z, boolean load, boolean read, boolean write, boolean update) {
      boolean init_Z_2() {
         return this.init_I >= 0;
      }

      int init_I_2() {
         return (this.load ? 2 : 0) + (this.read ? 1 : 0) + (this.write ? 1 : 0);
      }

      public int load_I() {
         return this.init_I;
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      public boolean read() {
         return this.load;
      }

      public boolean write() {
         return this.read;
      }

      public boolean update() {
         return this.write;
      }

      public boolean sync() {
         return this.update;
      }
   }
}
