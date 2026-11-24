package autismclient.gui.b;

public final class TaskQueue {
   public static final int init = 54;
   public static final int load = 120;
   public static final int read = 22;
   public static final int write = 0;
   public static final int update = 86;
   public static final int sync = 8;
   public static final int flush = 20;

   private TaskQueue() {
   }

   public static TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil_Str(String typeId, int containerCount, int auxCount) {
      if (containerCount < 0) {
         containerCount = 0;
      }

      TaskQueue.Inner_DataUtil typed = load(init_Str_Str(typeId), containerCount, Math.max(0, auxCount));
      return typed != null ? typed : init_TaskQueue_DataUtil_I(containerCount);
   }

   private static TaskQueue.Inner_DataUtil load(String key, int n, int aux) {
      TaskQueue.Inner_DataUtil var10000;
      switch (key) {
         case "furnace":
         case "blast_furnace":
         case "smoker":
            var10000 = n == 3 ? init_TaskQueue_DataUtil_I_IArr(62, 0, 0, 0, 36, 72, 18) : null;
            break;
         case "anvil":
            var10000 = n == 3 ? init_TaskQueue_DataUtil_I_IArr(52, 0, 18, 42, 18, 96, 18) : null;
            break;
         case "enchantment":
            var10000 = n == 2 ? init_TaskQueue_DataUtil_I_IArr(68, 0, 18, 18, 18) : null;
            break;
         case "brewing_stand":
            var10000 = n == 5 ? init_TaskQueue_DataUtil_I_IArr(86, 24, 54, 48, 60, 72, 54, 48, 0, 0, 54) : null;
            break;
         case "beacon":
            var10000 = n == 1 ? init_TaskQueue_DataUtil_I_IArr(108, 0, 0) : null;
            break;
         case "merchant":
            var10000 = n == 3 ? init_TaskQueue_DataUtil_I_IArr(Math.max(60, 8 + aux * 20 + 6), 90, 0, 108, 0, 156, 0) : null;
            break;
         case "grindstone":
            var10000 = n == 3 ? init_TaskQueue_DataUtil_I_IArr(62, 0, 0, 0, 36, 72, 18) : null;
            break;
         case "stonecutter":
            var10000 = n == 2 ? init_TaskQueue_DataUtil_I_IArr(80, 0, 18, 126, 18) : null;
            break;
         case "loom":
            var10000 = n == 4 ? init_TaskQueue_DataUtil_I_IArr(80, 0, 0, 18, 0, 0, 18, 126, 36) : null;
            break;
         case "cartography_table":
            var10000 = n == 3 ? init_TaskQueue_DataUtil_I_IArr(62, 0, 0, 0, 36, 126, 18) : null;
            break;
         case "smithing":
            var10000 = n == 4 ? init_TaskQueue_DataUtil_I_IArr(44, 0, 0, 18, 0, 36, 0, 90, 0) : null;
            break;
         case "lectern":
            var10000 = n == 1 ? init_TaskQueue_DataUtil_I_IArr(44, 0, 0) : null;
            break;
         case "crafting":
            var10000 = n == 10 ? init_TaskQueue_DataUtil() : null;
            break;
         case "hopper":
            var10000 = n == 5 ? init_TaskQueue_DataUtil_I_IArr(26, 0, 0, 18, 0, 36, 0, 54, 0, 72, 0) : null;
            break;
         case "generic_3x3":
         case "crafter_3x3":
            var10000 = n == 9 ? load() : null;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   private static TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil() {
      int[] x = new int[10];
      int[] y = new int[10];
      x[0] = 108;
      y[0] = 18;

      for (int i = 1; i <= 9; i++) {
         x[i] = (i - 1) % 3 * 18;
         y[i] = (i - 1) / 3 * 18;
      }

      return new TaskQueue.Inner_DataUtil(x, y, 62);
   }

   private static TaskQueue.Inner_DataUtil load() {
      int[] x = new int[9];
      int[] y = new int[9];

      for (int i = 0; i < 9; i++) {
         x[i] = i % 3 * 18;
         y[i] = i / 3 * 18;
      }

      return new TaskQueue.Inner_DataUtil(x, y, 62);
   }

   private static TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil_I(int n) {
      int[] x = new int[n];
      int[] y = new int[n];

      for (int i = 0; i < n; i++) {
         x[i] = i % 9 * 18;
         y[i] = i / 9 * 18;
      }

      int invTop = (n + 8) / 9 * 18 + 8;
      return new TaskQueue.Inner_DataUtil(x, y, invTop);
   }

   private static TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil_I_IArr(int invTop, int... xy) {
      int n = xy.length / 2;
      int[] x = new int[n];
      int[] y = new int[n];

      for (int i = 0; i < n; i++) {
         x[i] = xy[i * 2];
         y[i] = xy[i * 2 + 1];
      }

      return new TaskQueue.Inner_DataUtil(x, y, invTop);
   }

   private static String init_Str_Str(String typeId) {
      if (typeId == null) {
         return "";
      }

      int c = typeId.indexOf(58);
      return c >= 0 ? typeId.substring(c + 1) : typeId;
   }

   public record Inner_DataUtil(int[] init_IArr, int[] load, int init_I) {
   }
}
