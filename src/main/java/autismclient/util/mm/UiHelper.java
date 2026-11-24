package autismclient.util.mm;

public enum UiHelper {
   init_UiHelper(1),
   load(2),
   read(3),
   write(4),
   update(5),
   sync(6),
   flush(7),
   close(9),
   open(10),
   poll(16);

   public final int init_I;

   UiHelper(int id) {
      this.init_I = id;
   }

   public static UiHelper init(int id) {
      for (UiHelper t : values()) {
         if (t.init_I == id) {
            return t;
         }
      }

      return null;
   }

   public boolean init() {
      boolean var10000;
      switch (this) {
         case init_UiHelper:
         case flush:
         case open:
            var10000 = true;
            break;
         case load:
         case read:
         case write:
         case update:
         case sync:
         case close:
         case poll:
            var10000 = false;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public boolean load() {
      boolean var10000;
      switch (this) {
         case init_UiHelper:
         case flush:
         case open:
         case poll:
            var10000 = true;
            break;
         case load:
         case read:
         case write:
         case update:
         case sync:
         case close:
            var10000 = false;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public boolean read() {
      return !this.load();
   }

   public boolean write() {
      boolean var10000;
      switch (this) {
         case init_UiHelper:
         case sync:
         case poll:
            var10000 = false;
            break;
         case load:
         case read:
         case write:
         case update:
         case flush:
         case close:
         case open:
            var10000 = true;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }
}
