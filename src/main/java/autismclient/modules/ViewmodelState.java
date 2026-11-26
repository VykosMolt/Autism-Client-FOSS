package autismclient.modules;

import autismclient.util.AutismLiteVariant;

public final class ViewmodelState {
   private static volatile boolean close_Z;
   static volatile boolean init_Z;
   static volatile float init_F;
   static volatile float load_F;
   static volatile float read_F;
   static volatile float write_F;
   static volatile float update_F;
   static volatile float sync_F;
   static volatile boolean load_Z;
   static volatile float flush_F;
   static volatile float close_F;
   static volatile float open;
   static volatile float poll;
   static volatile float push;
   static volatile float pop;
   static volatile int init_I = 6;
   static volatile int load_I;
   static volatile float tick = 0.1F;
   static volatile float run = 0.9F;
   static volatile boolean read_Z = true;
   static volatile boolean write_Z = true;
   static volatile boolean update_Z = true;
   static volatile boolean sync_Z;
   static volatile boolean flush_Z;

   private ViewmodelState() {
   }

   static void init_V() {
      close_Z = true;
   }

   static void load_V() {
      close_Z = false;
   }

   private static boolean open_Z() {
      return AutismLiteVariant.enabled() || autismclient.e.HashUtil.init();
   }

   public static boolean init_Z() {
      return close_Z && open_Z();
   }

   public static boolean load_Z() {
      return close_Z && open_Z() && init_Z;
   }

   public static float init_F() {
      return init_F;
   }

   public static float load_F() {
      return load_F;
   }

   public static float read_F() {
      return read_F;
   }

   public static float write_F() {
      return write_F;
   }

   public static float update_F() {
      return update_F;
   }

   public static float sync_F() {
      return sync_F;
   }

   public static boolean read_Z() {
      return close_Z && open_Z() && load_Z;
   }

   public static float flush_F() {
      return flush_F;
   }

   public static float close_F() {
      return close_F;
   }

   public static float open_F() {
      return open;
   }

   public static float poll() {
      return poll;
   }

   public static float push() {
      return push;
   }

   public static float pop() {
      return pop;
   }

   public static int init_I() {
      return init_I;
   }

   public static int load_I() {
      return load_I;
   }

   public static float tick() {
      return tick;
   }

   public static float run() {
      return run;
   }

   public static boolean write_Z() {
      return close_Z && open_Z() && read_Z;
   }

   public static boolean update_Z() {
      return write_Z;
   }

   public static boolean sync_Z() {
      return update_Z;
   }

   public static boolean flush_Z() {
      return sync_Z;
   }

   public static boolean close_Z() {
      return close_Z && open_Z() && flush_Z;
   }
}
