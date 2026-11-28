package autismclient.ai;

import java.util.HashSet;
import java.util.Set;

final class AiPlacementInput {
   private boolean init_Z;
   private final Set<Integer> pressedKeys = new HashSet<>();

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
      return this.init_Z || !this.pressedKeys.isEmpty();
   }

   boolean init(int key) {
      return this.pressedKeys.contains(key);
   }

   Set<Integer> heldKeys() {
      return Set.copyOf(this.pressedKeys);
   }

   boolean init(int key, boolean down) {
      boolean owned = this.init_Z || this.pressedKeys.contains(key);
      if (down && this.init_Z) {
         this.pressedKeys.add(key);
      } else if (!down) {
         this.pressedKeys.remove(key);
      }

      return owned;
   }
}
