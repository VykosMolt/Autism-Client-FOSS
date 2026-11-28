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

public final class BookBotFileReader {
   private static final int init_I = 4194304;
   private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
   private static final Charset UTF_32LE = Charset.forName("UTF-32LE");
   private static final Charset UTF_32BE = Charset.forName("UTF-32BE");

   private BookBotFileReader() {
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
         return decode(bytes);
      }
   }

   static String decode(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         BookBotFileReader.BomEncoding encoding = detectBom(bytes);
         String decoded;
         if (encoding != null) {
            decoded = new String(bytes, encoding.bomLength(), bytes.length - encoding.bomLength(), encoding.charset());
         } else {
            decoded = decodeStrictUtf8(bytes);
         }

         return normalize(decoded);
      } else {
         return "";
      }
   }

   public static String normalize(String text) {
      if (text != null && !text.isEmpty()) {
         int start = text.charAt(0) == '\ufeff' ? 1 : 0;
         String value = start == 0 ? text : text.substring(start);
         return value.replace("\r\n", "\n").replace('\r', '\n').replace("\u0000", "");
      } else {
         return "";
      }
   }

   private static String decodeStrictUtf8(byte[] bytes) {
      try {
         return StandardCharsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
      } catch (CharacterCodingException ignored) {
         return new String(bytes, WINDOWS_1252);
      }
   }

   private static BookBotFileReader.BomEncoding detectBom(byte[] bytes) {
      int length = bytes.length;
      if (length >= 4 && unsigned(bytes[0]) == 0 && unsigned(bytes[1]) == 0 && unsigned(bytes[2]) == 254 && unsigned(bytes[3]) == 255) {
         return new BookBotFileReader.BomEncoding(UTF_32BE, 4);
      }

      if (length >= 4 && unsigned(bytes[0]) == 255 && unsigned(bytes[1]) == 254 && unsigned(bytes[2]) == 0 && unsigned(bytes[3]) == 0) {
         return new BookBotFileReader.BomEncoding(UTF_32LE, 4);
      }

      if (length >= 3 && unsigned(bytes[0]) == 239 && unsigned(bytes[1]) == 187 && unsigned(bytes[2]) == 191) {
         return new BookBotFileReader.BomEncoding(StandardCharsets.UTF_8, 3);
      }

      if (length >= 2 && unsigned(bytes[0]) == 254 && unsigned(bytes[1]) == 255) {
         return new BookBotFileReader.BomEncoding(StandardCharsets.UTF_16BE, 2);
      }

      if (length >= 2 && unsigned(bytes[0]) == 255 && unsigned(bytes[1]) == 254) {
         return new BookBotFileReader.BomEncoding(StandardCharsets.UTF_16LE, 2);
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
            return new BookBotFileReader.BomEncoding(StandardCharsets.UTF_16LE, 0);
         }

         if (evenNuls >= threshold && evenNuls >= oddNuls * 3) {
            return new BookBotFileReader.BomEncoding(StandardCharsets.UTF_16BE, 0);
         }
      }

      return null;
   }

   private static int unsigned(byte value) {
      return value & 0xFF;
   }

   private record BomEncoding(Charset charset, int bomLength) {
   }
}
