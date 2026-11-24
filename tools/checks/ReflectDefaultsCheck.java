package autismclient.util;

import java.lang.reflect.Method;

public class ReflectDefaultsCheck {
   public static void main(String[] args) throws Exception {
      Method defaults = R.class.getDeclaredMethod("init_Obj_Class", Class.class);
      defaults.setAccessible(true);
      Class<?>[] types = {boolean.class, char.class, byte.class, short.class, int.class, long.class, float.class, double.class};
      Object[] expected = {false, '\u0000', (byte) 0, (short) 0, 0, 0L, 0.0F, 0.0};
      for (int i = 0; i < types.length; i++) {
         Object got = defaults.invoke(null, types[i]);
         if (!expected[i].equals(got)) {
            System.out.println("FAIL: default for " + types[i] + " was " + got + " (" + (got == null ? "null" : got.getClass().getSimpleName()) + ")");
            System.exit(1);
         }
      }
      if (defaults.invoke(null, String.class) != null) {
         System.out.println("FAIL: non-primitive default not null");
         System.exit(1);
      }
      System.out.println("PASS ReflectDefaultsCheck");
   }
}
