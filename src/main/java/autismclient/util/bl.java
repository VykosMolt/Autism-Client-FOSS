package autismclient.util;

import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;

public final class bl {
   private static final long init_J = 300000L;
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static final dc init_dc = new dc(System::currentTimeMillis);

   private bl() {
   }

   public static bl.Inner_StreamUtil init(autismclient.util.macro.w action, String owner) {
      long id = init_AtomicLong.incrementAndGet();
      bl.Inner_StreamUtil handle = new bl.Inner_StreamUtil(id, owner == null ? "" : owner);
      init_dc.init(id, handle.init_Str(), 300000L);

      autismclient.util.g.PkgCls_a.Inner_StateCache result;
      try {
         result = autismclient.util.g.PkgCls_a.init_a_StateCache_w_bl_StreamUtil(action, handle);
      } catch (RuntimeException | Error error) {
         init_dc.init(id, dc.Inner_DataUtil.read);
         throw error;
      }

      if (result != null && result.init_Z()) {
         init_dc.init_Z(id);
      } else {
         init_dc.init(id, dc.Inner_DataUtil.read);
      }

      return handle;
   }

   public static bl.Inner_CachePool init_bl_CachePool(long id) {
      dc.Inner_StreamUtil state = init_dc.init_dc_StreamUtil(id);
      return new bl.Inner_CachePool(state.init_J(), state.init_Str(), init(state.init_dc_DataUtil()), state.load_Z(), state.read(), state.load_J());
   }

   public static bl.Inner_DataUtil init_bl_DataUtil(long id) {
      return init_bl_CachePool(id).init_bl_DataUtil();
   }

   public static void init(long id, String reason) {
      if (init_bl_DataUtil(id) == bl.Inner_DataUtil.init) {
         autismclient.util.g.PkgCls_a.init_V_J_Str(id, reason);
         init_dc.init(id, dc.Inner_DataUtil.write);
      }
   }

   public static void init(Minecraft client) {
      for (long id : init_dc.init_List()) {
         autismclient.util.g.PkgCls_a.load(id, "timeout");
         init_dc.init(id, dc.Inner_DataUtil.read);
      }

      autismclient.util.g.PkgCls_a.init_V_Minecraft(client);
   }

   public static void init(String reason) {
      autismclient.util.g.PkgCls_a.init_V_Str(reason);

      for (long id : init_dc.load()) {
         init_dc.init(id, dc.Inner_DataUtil.write);
      }
   }

   public static void init(long id, bl.Inner_DataUtil completion) {
      init_dc.init(id, init(completion));
   }

   private static dc.Inner_DataUtil init(bl.Inner_DataUtil completion) {
      return completion == null ? null : dc.Inner_DataUtil.valueOf(completion.name());
   }

   private static bl.Inner_DataUtil init(dc.Inner_DataUtil completion) {
      return bl.Inner_DataUtil.valueOf(completion.name());
   }

   public record Inner_CachePool(long init_J, String init_Str, bl.Inner_DataUtil init_bl_DataUtil, boolean init_Z, boolean load_Z, long load_J) {
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   public record Inner_StreamUtil(long init_J, String init_Str) {
   }
}
