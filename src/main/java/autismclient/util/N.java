package autismclient.util;

import net.minecraft.world.inventory.ContainerInput;

public enum N {
   init_N("Pickup", "Pick", false),
   load_N("Quick Move", "QMove", false),
   read("Swap", "Swap", false),
   write("Clone", "Clone", false),
   update("Throw (2x)", "Throw", false),
   sync("Quick Craft", "QCraft", false),
   flush("Pick Up All", "All", false),
   close("Drop Item (1x)", "Drop1", false),
   open("Drop Stack", "DropStk", false);

   public final String init_Str;
   public final String load_Str;
   public final boolean init_Z;

   N(String displayName, String shortName, boolean isPlayerAction) {
      this.init_Str = displayName;
      this.load_Str = shortName;
      this.init_Z = isPlayerAction;
   }

   public boolean init_Z() {
      return !this.init_Z;
   }

   public boolean load() {
      return this.init_I() >= 0;
   }

   public int init_I() {
      byte var10000;
      switch (this) {
         case load_N:
            var10000 = 0;
            break;
         case read:
         case update:
         case sync:
         default:
            var10000 = -1;
            break;
         case write:
            var10000 = 2;
            break;
         case flush:
            var10000 = 0;
            break;
         case close:
            var10000 = 0;
            break;
         case open:
            var10000 = 1;
      }

      return var10000;
   }

   public ContainerInput init_ContainerInput() {
      if (this.init_Z) {
         throw new IllegalStateException("Cannot convert PlayerAction to ContainerInput: " + this);
      }

      ContainerInput var10000;
      switch (this) {
         case init_N:
            var10000 = ContainerInput.PICKUP;
            break;
         case load_N:
            var10000 = ContainerInput.QUICK_MOVE;
            break;
         case read:
            var10000 = ContainerInput.SWAP;
            break;
         case write:
            var10000 = ContainerInput.CLONE;
            break;
         case update:
         case close:
         case open:
            var10000 = ContainerInput.THROW;
            break;
         case sync:
            var10000 = ContainerInput.QUICK_CRAFT;
            break;
         case flush:
            var10000 = ContainerInput.PICKUP_ALL;
            break;
         default:
            throw new IllegalStateException("Unknown action: " + this);
      }

      return var10000;
   }
}
