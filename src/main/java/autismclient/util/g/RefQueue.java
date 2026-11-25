package autismclient.util.g;

import autismclient.util.ProxyEntry;
import autismclient.util.ProxyType;
import autismclient.util.ca;

public final class RefQueue {
   private RefQueue() {
   }

   public static RefQueue.Inner_DataUtil init(ConfigStore.Inner_DataUtil proxy, String destHost, int destPort, int timeoutMs) {
      return init(proxy == null ? null : proxy.init_bW(), destHost, destPort, timeoutMs);
   }

   public static RefQueue.Inner_DataUtil init(ProxyEntry proxy, String destHost, int destPort, int timeoutMs) {
      ca.Inner_DataUtil verified = ca.init(proxy, destHost, destPort, timeoutMs);
      return new RefQueue.Inner_DataUtil(verified.init_Z(), verified.init_J(), verified.init_bZ(), verified.init_ca_StreamUtil());
   }

   public record Inner_DataUtil(boolean init_Z, long init_J, ProxyType init_bZ, ca.Inner_StreamUtil init_ca_StreamUtil) {
      public Inner_DataUtil(boolean ok, long latencyMs, ProxyType workingType) {
         this(ok, latencyMs, workingType, ok ? ca.Inner_StreamUtil.update : ca.Inner_StreamUtil.load);
      }

      public Inner_DataUtil(boolean ok, long latencyMs) {
         this(ok, latencyMs, null);
      }
   }
}
