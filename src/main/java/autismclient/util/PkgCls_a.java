package autismclient.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PkgCls_a {
   private static final int init_I = 4194304;
   private static final Charset init_Charset = Charset.forName("windows-1252");
   private static final Charset load = Charset.forName("UTF-32LE");
   private static final Charset read = Charset.forName("UTF-32BE");

   private PkgCls_a() {
   }

   public static String init(Path path) throws IOException {
      if (path == null) {
         return "";
      }

      byte[] bytes;
      try (InputStream input = Files.newInputStream(path)) {
         bytes = input.readNBytes(4194305);
      }

      if (bytes.length > 4194304) {
         throw new IOException("file exceeds the 4 MiB BookBot limit");
      } else {
         return init_Str(bytes);
      }
   }

   static String init_Str(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         PkgCls_a.Inner_DataUtil encoding = init_a_DataUtil(bytes);
         String decoded;
         if (encoding != null) {
            decoded = new String(bytes, encoding.init_I(), bytes.length - encoding.init_I(), encoding.init_Charset());
         } else {
            decoded = load(bytes);
         }

         return init(decoded);
      } else {
         return "";
      }
   }

   public static String init(String text) {
      if (text != null && !text.isEmpty()) {
         int start = text.charAt(0) == '\ufeff' ? 1 : 0;
         String value = start == 0 ? text : text.substring(start);
         return value.replace("\r\n", "\n").replace('\r', '\n').replace("\u0000", "");
      } else {
         return "";
      }
   }

   private static String load(byte[] bytes) {
      try {
         return StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
      } catch (CharacterCodingException ignored) {
         return new String(bytes, init_Charset);
      }
   }

   private static PkgCls_a.Inner_DataUtil init_a_DataUtil(byte[] bytes) {
      int length = bytes.length;
      if (length >= 4 && init(bytes[0]) == 0 && init(bytes[1]) == 0 && init(bytes[2]) == 254 && init(bytes[3]) == 255) {
         return new PkgCls_a.Inner_DataUtil(read, 4);
      }

      if (length >= 4 && init(bytes[0]) == 255 && init(bytes[1]) == 254 && init(bytes[2]) == 0 && init(bytes[3]) == 0) {
         return new PkgCls_a.Inner_DataUtil(load, 4);
      }

      if (length >= 3 && init(bytes[0]) == 239 && init(bytes[1]) == 187 && init(bytes[2]) == 191) {
         return new PkgCls_a.Inner_DataUtil(StandardCharsets.UTF_8, 3);
      }

      if (length >= 2 && init(bytes[0]) == 254 && init(bytes[1]) == 255) {
         return new PkgCls_a.Inner_DataUtil(StandardCharsets.UTF_16BE, 2);
      }

      if (length >= 2 && init(bytes[0]) == 255 && init(bytes[1]) == 254) {
         return new PkgCls_a.Inner_DataUtil(StandardCharsets.UTF_16LE, 2);
      }

      int sample = Math.min(length & -2, 8192);
      int pairs = sample / 2;
      if (pairs >= 4) {
         int evenNuls = 0;
         int oddNuls = 0;

         for (int i = 0; i < sample; i += 2) {
            if (bytes[i] == 0) {
               evenNuls++;
            }

            if (bytes[i + 1] == 0) {
               oddNuls++;
            }
         }

         int threshold = Math.max(3, pairs / 3);
         if (oddNuls >= threshold && oddNuls >= evenNuls * 3) {
            return new PkgCls_a.Inner_DataUtil(StandardCharsets.UTF_16LE, 0);
         }

         if (evenNuls >= threshold && evenNuls >= oddNuls * 3) {
            return new PkgCls_a.Inner_DataUtil(StandardCharsets.UTF_16BE, 0);
         }
      }

      return null;
   }

   private static int init(byte value) {
      return value & 0xFF;
   }

   private record Inner_DataUtil(Charset init_Charset, int init_I) {
   }
}
