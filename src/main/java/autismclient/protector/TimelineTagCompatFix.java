package autismclient.protector;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryDataLoader.NetworkedRegistryData;
import net.minecraft.tags.TagNetworkSerialization.NetworkPayload;

public final class TimelineTagCompatFix {
   private TimelineTagCompatFix() {
   }

   public static void init(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> entries, boolean translated) {
      NetworkedRegistryData timelines = entries.get(Registries.TIMELINE);
      if (timelines != null && timelines.tags().isEmpty()) {
         Map<Identifier, Integer> ids = new HashMap<>();

         for (int i = 0; i < timelines.elements().size(); i++) {
            ids.put(timelines.elements().get(i).id(), i);
         }

         if (translated
            || timelines.elements().size() == 4 && ids.keySet().equals(Set.of(init("day"), init("moon"), init("early_game"), init("villager_schedule")))) {
            IntList universal = new IntArrayList();
            init(universal, ids, "villager_schedule");
            IntList overworld = new IntArrayList(universal);
            init(overworld, ids, "day");
            init(overworld, ids, "moon");
            init(overworld, ids, "early_game");
            Map<Identifier, IntList> tags = Map.of(
               init("universal"),
               universal,
               init("in_overworld"),
               overworld,
               init("in_nether"),
               new IntArrayList(universal),
               init("in_end"),
               new IntArrayList(universal)
            );
            entries.put(Registries.TIMELINE, new NetworkedRegistryData(timelines.elements(), new NetworkPayload(tags)));
         }
      }
   }

   private static void init(IntList target, Map<Identifier, Integer> ids, String name) {
      Integer index = ids.get(init(name));
      if (index != null) {
         target.add(index.intValue());
      }
   }

   private static Identifier init(String name) {
      return Identifier.withDefaultNamespace(name);
   }
}
