package autismclient.ai;

import java.io.IOException;
import java.io.Reader;

final class AiBoundedIo {
   private AiBoundedIo() {
   }

   static String readLine(Reader reader, int limit) throws IOException {
      StringBuilder line = new StringBuilder();

      int c;
      while ((c = reader.read()) != -1) {
         if (c == 10) {
            return line.toString();
         }

         if (c != 13) {
            line.append((char)c);
         }

         if (line.length() > limit) {
            throw new IOException("Protocol line exceeds " + limit + " characters");
         }
      }

      return line.isEmpty() ? null : line.toString();
   }
}
