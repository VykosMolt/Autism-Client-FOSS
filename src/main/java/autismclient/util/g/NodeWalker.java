package autismclient.util.g;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class NodeWalker {
   private NodeWalker() {
   }

   public static List<NodeWalker.Inner_DataUtil> init() {
      Map<String, List<String>> byKey = new LinkedHashMap<>();
      Map<String, String> names = new LinkedHashMap<>();

      for (ClassCache.Inner_FieldCache snapshot : PacketUtil.init_PacketUtil().load_List()) {
         if (snapshot.load_Z()) {
            String key;
            String name;
            if (snapshot.read_Z()) {
               key = "custom";
               name = "CustomScreen";
            } else if (snapshot.sync() != null && !snapshot.sync().isBlank()) {
               key = "s:" + snapshot.sync();
               name = snapshot.sync();
            } else {
               key = "inv";
               name = "Inventory";
            }

            byKey.computeIfAbsent(key, ignored -> new ArrayList<>()).add(snapshot.init_Str());
            names.putIfAbsent(key, name);
         }
      }

      List<NodeWalker.Inner_DataUtil> out = new ArrayList<>(byKey.size());

      for (Entry<String, List<String>> entry : byKey.entrySet()) {
         String name = PacketUtil.update(names.get(entry.getKey()), 40);
         out.add(new NodeWalker.Inner_DataUtil(entry.getKey(), name + " (" + entry.getValue().size() + ")", List.copyOf(entry.getValue())));
      }

      out.sort((a, b) -> Integer.compare(b.init_I(), a.init_I()));
      return out;
   }

   public static NodeWalker.Inner_DataUtil init(List<NodeWalker.Inner_DataUtil> groups, String preferredKey) {
      if (groups != null && !groups.isEmpty()) {
         if (preferredKey != null && !preferredKey.isEmpty()) {
            for (NodeWalker.Inner_DataUtil group : groups) {
               if (group.load().equals(preferredKey)) {
                  return group;
               }
            }
         }

         return groups.getFirst();
      } else {
         return null;
      }
   }

   public record Inner_DataUtil(String init_Str, String load, List<String> init_List) {
      public String init_Str() {
         return this.init_List.getFirst();
      }

      public int init_I() {
         return this.init_List.size();
      }

      public String load() {
         return this.init_Str;
      }

      public String read() {
         return this.load;
      }
   }
}
