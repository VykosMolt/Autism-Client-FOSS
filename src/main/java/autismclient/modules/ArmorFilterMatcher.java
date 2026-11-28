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
   static final String ANY_OPTION = "Any";
   static final List<String> BASE_OPTIONS = List.of("Any", "Empty", "Armor", "Leather", "Chain", "Copper", "Iron", "Gold", "Diamond", "Netherite");
   static final List<String> HEAD_OPTIONS = withOptions(BASE_OPTIONS, "Turtle", "Pumpkin", "Skull");
   static final List<String> CHEST_OPTIONS = withOptions(BASE_OPTIONS, "Elytra");

   private ArmorFilterMatcher() {
   }

   static boolean isNpcUuid(UUID uuid) {
      return uuid != null && uuid.version() == 2 && uuid.variant() == 2;
   }

   static int groundViolation(int vl, boolean claimsGround, boolean supported) {
      if (claimsGround && !supported) {
         return vl + 1;
      } else {
         return !claimsGround ? vl / 2 : Math.max(0, vl - 1);
      }
   }

   static Map<Integer, Integer> duplicateNameFlags(List<ArmorFilterMatcher.Identity> identities) {
      Map<String, List<ArmorFilterMatcher.Identity>> byName = new HashMap<>();

      for (ArmorFilterMatcher.Identity id : identities) {
         if (id.name() != null && id.uuid() != null) {
            byName.computeIfAbsent(id.name(), k -> new ArrayList<>()).add(id);
         }
      }

      Map<Integer, Integer> out = new HashMap<>();

      for (List<ArmorFilterMatcher.Identity> group : byName.values()) {
         if (group.size() >= 2 && distinctUuidCount(group) >= 2) {
            List<ArmorFilterMatcher.Identity> open = new ArrayList<>();

            for (ArmorFilterMatcher.Identity id : group) {
               if (!id.sync() && !id.isProven()) {
                  open.add(id);
               }
            }

            boolean anyProven = false;

            for (ArmorFilterMatcher.Identity id : group) {
               anyProven |= id.isProven() && !id.sync();
            }

            if (anyProven) {
               for (ArmorFilterMatcher.Identity id : open) {
                  if (id.hasEntity()) {
                     out.put(id.boundEntityId(), 2);
                  }
               }
            } else if (open.size() >= 2) {
               int best = -2147483648;

               for (ArmorFilterMatcher.Identity id : open) {
                  best = Math.max(best, id.evidenceScore());
               }

               List<ArmorFilterMatcher.Identity> top = new ArrayList<>();

               for (ArmorFilterMatcher.Identity id : open) {
                  if (id.evidenceScore() == best) {
                     top.add(id);
                  }
               }

               int topEntities = 0;

               for (ArmorFilterMatcher.Identity id : top) {
                  if (id.hasEntity()) {
                     topEntities++;
                  }
               }

               for (ArmorFilterMatcher.Identity id : open) {
                  if (id.hasEntity()) {
                     boolean isTop = id.evidenceScore() == best;
                     if (!isTop || top.size() > 1 && topEntities > 1) {
                        out.put(id.boundEntityId(), 1);
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   private static int distinctUuidCount(List<ArmorFilterMatcher.Identity> group) {
      Set<UUID> seen = new HashSet<>();

      for (ArmorFilterMatcher.Identity id : group) {
         seen.add(id.uuid());
      }

      return seen.size();
   }

   private static List<String> withOptions(List<String> base, String... more) {
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
               var10000 = !empty && (isArmorPiece(id) || id.equals("elytra"));
               break;
            case "Leather":
               var10000 = isArmorOfTier(id, "leather_");
               break;
            case "Chain":
               var10000 = isArmorOfTier(id, "chainmail_");
               break;
            case "Copper":
               var10000 = isArmorOfTier(id, "copper_");
               break;
            case "Iron":
               var10000 = isArmorOfTier(id, "iron_");
               break;
            case "Gold":
               var10000 = isArmorOfTier(id, "golden_");
               break;
            case "Diamond":
               var10000 = isArmorOfTier(id, "diamond_");
               break;
            case "Netherite":
               var10000 = isArmorOfTier(id, "netherite_");
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

   private static boolean isArmorPiece(String id) {
      return id.endsWith("_helmet") || id.endsWith("_chestplate") || id.endsWith("_leggings") || id.endsWith("_boots");
   }

   private static boolean isArmorOfTier(String id, String prefix) {
      return id.startsWith(prefix) && isArmorPiece(id);
   }

   record Identity(UUID uuid, String name, int init_I, boolean init_Z, boolean load, boolean read, boolean write, boolean update) {
      boolean hasEntity() {
         return this.init_I >= 0;
      }

      int evidenceScore() {
         return (this.load ? 2 : 0) + (this.read ? 1 : 0) + (this.write ? 1 : 0);
      }

      public int boundEntityId() {
         return this.init_I;
      }

      public boolean isProven() {
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
