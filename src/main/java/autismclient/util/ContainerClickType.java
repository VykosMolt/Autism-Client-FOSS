package autismclient.util;

import net.minecraft.world.inventory.ContainerInput;

public enum ContainerClickType {
   PICKUP("Pickup", "Pick", false),
   QUICK_MOVE("Quick Move", "QMove", false),
   SWAP("Swap", "Swap", false),
   CLONE("Clone", "Clone", false),
   THROW("Throw (2x)", "Throw", false),
   QUICK_CRAFT("Quick Craft", "QCraft", false),
   PICKUP_ALL("Pick Up All", "All", false),
   DROP_ITEM("Drop Item (1x)", "Drop1", false),
   DROP_STACK("Drop Stack", "DropStk", false);

   public final String init_Str;
   public final String load_Str;
   public final boolean init_Z;

   ContainerClickType(String displayName, String shortName, boolean isPlayerAction) {
      this.init_Str = displayName;
      this.load_Str = shortName;
      this.init_Z = isPlayerAction;
   }

   public boolean init_Z() {
      return !this.init_Z;
   }

   public boolean hasDefaultButton() {
      return this.defaultButton() >= 0;
   }

   public int defaultButton() {
      byte var10000;
      switch (this) {
         case QUICK_MOVE:
            var10000 = 0;
            break;
         case SWAP:
         case THROW:
         case QUICK_CRAFT:
         default:
            var10000 = -1;
            break;
         case CLONE:
            var10000 = 2;
            break;
         case PICKUP_ALL:
            var10000 = 0;
            break;
         case DROP_ITEM:
            var10000 = 0;
            break;
         case DROP_STACK:
            var10000 = 1;
      }

      return var10000;
   }

   public ContainerInput toContainerInput() {
      if (this.init_Z) {
         throw new IllegalStateException("Cannot convert PlayerAction to ContainerInput: " + this);
      }

      ContainerInput var10000;
      switch (this) {
         case PICKUP:
            var10000 = ContainerInput.PICKUP;
            break;
         case QUICK_MOVE:
            var10000 = ContainerInput.QUICK_MOVE;
            break;
         case SWAP:
            var10000 = ContainerInput.SWAP;
            break;
         case CLONE:
            var10000 = ContainerInput.CLONE;
            break;
         case THROW:
         case DROP_ITEM:
         case DROP_STACK:
            var10000 = ContainerInput.THROW;
            break;
         case QUICK_CRAFT:
            var10000 = ContainerInput.QUICK_CRAFT;
            break;
         case PICKUP_ALL:
            var10000 = ContainerInput.PICKUP_ALL;
            break;
         default:
            throw new IllegalStateException("Unknown action: " + this);
      }

      return var10000;
   }
}
