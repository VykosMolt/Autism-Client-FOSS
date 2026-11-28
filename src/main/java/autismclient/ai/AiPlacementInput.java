package autismclient.ai;

import java.util.HashSet;
import java.util.Set;

final class AiPlacementInput {
   private boolean active;
   private final Set<Integer> pressedKeys = new HashSet<>();

   void init_V() {
      this.active = true;
   }

   void load_V() {
      this.active = false;
   }

   boolean isActive() {
      return this.active;
   }

   boolean load_Z() {
      return this.active || !this.pressedKeys.isEmpty();
   }

   boolean isHeld(int key) {
      return this.pressedKeys.contains(key);
   }

   Set<Integer> heldKeys() {
      return Set.copyOf(this.pressedKeys);
   }

   boolean init(int key, boolean down) {
      boolean owned = this.active || this.pressedKeys.contains(key);
      if (down && this.active) {
         this.pressedKeys.add(key);
      } else if (!down) {
         this.pressedKeys.remove(key);
      }

      return owned;
   }
}
