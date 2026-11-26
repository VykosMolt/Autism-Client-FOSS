package autismclient.ai;

import java.util.HashSet;
import java.util.Set;

final class AiPlacementInput {
   private boolean init_Z;
   private final Set<Integer> init_Set = new HashSet<>();

   void init_V() {
      this.init_Z = true;
   }

   void load_V() {
      this.init_Z = false;
   }

   boolean init_Z() {
      return this.init_Z;
   }

   boolean load_Z() {
      return this.init_Z || !this.init_Set.isEmpty();
   }

   boolean init(int key) {
      return this.init_Set.contains(key);
   }

   Set<Integer> init_Set() {
      return Set.copyOf(this.init_Set);
   }

   boolean init(int key, boolean down) {
      boolean owned = this.init_Z || this.init_Set.contains(key);
      if (down && this.init_Z) {
         this.init_Set.add(key);
      } else if (!down) {
         this.init_Set.remove(key);
      }

      return owned;
   }
}
