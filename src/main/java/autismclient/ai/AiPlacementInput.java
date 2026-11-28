package autismclient.ai;

import java.util.HashSet;
import java.util.Set;

final class AiPlacementInput {
   private boolean active;
   private final Set<Integer> pressedKeys = new HashSet<>();

   void activate() {
      this.active = true;
   }

   void deactivate() {
      this.active = false;
   }

   boolean isActive() {
      return this.active;
   }

   boolean ownsInput() {
      return this.active || !this.pressedKeys.isEmpty();
   }

   boolean isHeld(int key) {
      return this.pressedKeys.contains(key);
   }

   Set<Integer> heldKeys() {
      return Set.copyOf(this.pressedKeys);
   }

   boolean trackKey(int key, boolean down) {
      boolean owned = this.active || this.pressedKeys.contains(key);
      if (down && this.active) {
         this.pressedKeys.add(key);
      } else if (!down) {
         this.pressedKeys.remove(key);
      }

      return owned;
   }
}
