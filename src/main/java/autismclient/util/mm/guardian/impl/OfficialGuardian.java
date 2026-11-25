package autismclient.util.mm.guardian.impl;

import autismclient.e.DataUtil;
import autismclient.util.mm.a.StreamUtil;
import autismclient.util.mm.guardian.Guardian;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public final class OfficialGuardian implements Guardian {
   @Override
   public boolean isOfficial() {
      return true;
   }

   @Override
   public String attest(String nonceB64, String version, String pubkeyB64) {
      byte[] kb = null;

      try {
         kb = K.k();
         if (kb != null && kb.length != 0) {
            String ev = InjectionScan.evidence().toString();
            String msg = nz(nonceB64) + "\n" + nz(version) + "\n" + nz(pubkeyB64) + "\n" + ev;
            String sig = StreamUtil.init_Str_BArr(StreamUtil.init_BArr_BArr_BArr(kb, msg.getBytes(StandardCharsets.UTF_8)));
            String evB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(ev.getBytes(StandardCharsets.UTF_8));
            return evB64 + "." + sig;
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      } finally {
         if (kb != null) {
            Arrays.fill(kb, (byte)0);
         }
      }
   }

   @Override
   public byte[] unwrapKey(byte[] wrapped, byte[] info) {
      byte[] kb = null;

      try {
         if (wrapped != null && wrapped.length >= 28 && info != null) {
            kb = K.k();
            if (kb != null && kb.length != 0) {
               byte[] key = StreamUtil.init_BArr_BArr(
                  StreamUtil.init_BArr_Str(
                     DataUtil.init(
                        new int[]{
                           1417999569,
                           1417999536,
                           1008313637,
                           1192953030,
                           -1673162248,
                           -328425248,
                           -1591250497,
                           -2084124581,
                           -183351692,
                           -1388938208,
                           -107409867,
                           965262404,
                           -141636879,
                           -95778690,
                           1325824695,
                           -1849106538,
                           -1487724744,
                           1125172146,
                           520599356,
                           -1041288299,
                           2079802351,
                           49360810
                        }
                     )
                  ),
                  kb,
                  new String(info, StandardCharsets.UTF_8),
                  32
               );
               byte[] nonce = Arrays.copyOfRange(wrapped, 0, 12);
               byte[] ct = Arrays.copyOfRange(wrapped, 12, wrapped.length);
               return StreamUtil.load(key, nonce, ct, null);
            } else {
               return null;
            }
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      } finally {
         if (kb != null) {
            Arrays.fill(kb, (byte)0);
         }
      }
   }

   private static String nz(String s) {
      return s == null ? DataUtil.init(new int[]{1417999569}) : s;
   }
}
