package autismclient.util.d;

public enum StreamUtil {
   init_StreamUtil(1),
   load(2),
   read(3),
   write(4),
   update(5),
   sync(6),
   flush(16),
   close(17),
   open(23),
   poll(18),
   push(19),
   pop(20),
   tick(21),
   run(28),
   get(30),
   set(31),
   put(33),
   add(34),
   clear(35),
   reset(40),
   start(42),
   stop(43),
   next(44),
   prev(45),
   valid(46),
   size(50),
   data(51);

   private final int init_I;

   StreamUtil(int id) {
      this.init_I = id;
   }

   public int init() {
      return this.init_I;
   }

   public static StreamUtil init(int id) {
      for (StreamUtil type : values()) {
         if (type.init_I == id) {
            return type;
         }
      }

      return null;
   }
}
