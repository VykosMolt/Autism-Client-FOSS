package autismclient.util.mm.guardian.impl;

import autismclient.e.DataUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class AutismJarAttest {
   private AutismJarAttest() {
   }

   private static Path selfJar() {
      try {
         Optional<ModContainer> opt = FabricLoader.getInstance()
            .getModContainer(DataUtil.init(new int[]{962453623, 962453526, -228708833, 1499963052, -745784254, -1644476086, 560848257}));
         if (opt.isEmpty()) {
            return null;
         }

         Path only = null;

         for (Path p : opt.get().getOrigin().getPaths()) {
            if (p != null && Files.isRegularFile(p)) {
               if (only != null) {
                  return null;
               }

               only = p;
            }
         }

         return only;
      } catch (Throwable t) {
         return null;
      }
   }

   public static String answer(JsonObject challenge) {
      if (challenge != null
         && challenge.has(DataUtil.init(new int[]{962453625, 962453527, -228708665, 1499964980, -745724790, -1642629026}))
         && challenge.has(DataUtil.init(new int[]{962453625, 962453515, -228708663, 1499964980, -745724786, -1642629026, 618106713}))) {
         Path jar = selfJar();
         if (jar == null) {
            return null;
         }

         try (FileChannel ch = FileChannel.open(jar, StandardOpenOption.READ)) {
            long size = ch.size();
            MessageDigest md = MessageDigest.getInstance(
               DataUtil.init(new int[]{962453627, 962453544, -228708690, 1499966877, -745665142, -1640782065, 675364957, -538522456})
            );
            md.update(
               Base64.getDecoder()
                  .decode(challenge.get(DataUtil.init(new int[]{962453627, 962453525, -228708727, 1499966898, -745665084, -1640781992})).getAsString())
            );

            for (JsonElement el : challenge.getAsJsonArray(
               DataUtil.init(new int[]{962453629, 962453519, -228708539, 1499968816, -745605630, -1638934950, 732623317})
            )) {
               JsonArray r = el.getAsJsonArray();
               long off = r.get(0).getAsLong();
               int len = r.get(1).getAsInt();
               if (off < 0L || len < 0 || off + len > size) {
                  return null;
               }

               ByteBuffer buf = ByteBuffer.allocate(Math.min(len, 65536));
               long remaining = len;
               long pos = off;

               while (remaining > 0L) {
                  buf.clear();
                  if (buf.capacity() > remaining) {
                     buf.limit((int)remaining);
                  }

                  int n = ch.read(buf, pos);
                  if (n <= 0) {
                     return null;
                  }

                  buf.flip();
                  md.update(buf);
                  pos += n;
                  remaining -= n;
               }
            }

            return hex(md.digest());
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String hex(byte[] b) {
      StringBuilder sb = new StringBuilder(b.length * 2);

      for (byte x : b) {
         sb.append(Character.forDigit(x >> 4 & 15, 16)).append(Character.forDigit(x & 15, 16));
      }

      return sb.toString();
   }
}
