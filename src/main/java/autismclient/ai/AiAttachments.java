package autismclient.ai;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

final class AiAttachments {
   private static final Pattern init = Pattern.compile(
      "(?i)^(?:please\\s+)?(?:build|place|construct)(?:\\s+(?:the\\s+attached|the|this|attached))?(?:\\s+(?:schematic|file))?(?:\\s+(here|at\\s+-?\\d+\\s+-?\\d+\\s+-?\\d+))?[.!]*$"
   );

   private AiAttachments() {
   }

   static String init() {
      PointerBuffer filters = BufferUtils.createPointerBuffer(AiSchematic.init_StrArr.length);
      ArrayList<ByteBuffer> buffers = new ArrayList<>();

      try {
         for (String extension : AiSchematic.init_StrArr) {
            ByteBuffer buffer = MemoryUtil.memASCII("*" + extension);
            buffers.add(buffer);
            filters.put(buffer);
         }

         filters.flip();
         return TinyFileDialogs.tinyfd_openFileDialog("Attach schematic", null, filters, "Schematic files (.schem, .schematic, .litematic)", false);
      } finally {
         buffers.forEach(MemoryUtil::memFree);
      }
   }

   static AiAttachments.Inner_DataUtil init(Path source, Path directory) throws IOException, NoSuchAlgorithmException {
      String name = AiSchematic.init_Str(source.getFileName().toString());
      if (name.chars().anyMatch(c -> c < 32 || c == 34)) {
         throw new IllegalArgumentException("Invalid file name");
      }

      String lower = name.toLowerCase(Locale.ROOT);
      String extension = Arrays.stream(AiSchematic.init_StrArr)
         .filter(lower::endsWith)
         .findFirst()
         .orElseThrow(() -> new IllegalArgumentException("Choose a .schem, .schematic or .litematic file"));
      long bytes = Files.size(source);
      if (bytes > 67108864L) {
         throw new IllegalArgumentException("Schematic exceeds 64 MB");
      }

      Files.createDirectories(directory);
      Path temporary = Files.createTempFile(directory, ".import-", extension);

      try {
         Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
         if (Files.size(temporary) > 67108864L) {
            throw new IllegalArgumentException("Schematic exceeds 64 MB");
         }

         AiSchematic.Inner_DataUtil decoded = AiSchematic.init_bP_DataUtil_Path_Z(temporary, false);
         AiSchematic.init(AiBuilder.init_List_List(decoded.cells(), 2097152, 1023));
         Path target = directory.resolve(name);
         if (Files.exists(target) && Files.mismatch(temporary, target) != -1L) {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(temporary))).substring(0, 12);
            String stem = name.substring(0, name.length() - extension.length());
            name = stem.substring(0, Math.min(stem.length(), 100)) + "-" + hash + extension;
            target = directory.resolve(name);
            if (Files.exists(target) && Files.mismatch(temporary, target) != -1L) {
               throw new IllegalArgumentException("A different file already uses the imported name " + name);
            }
         }

         if (!Files.exists(target)) {
            Files.move(temporary, target);
         }

         return new AiAttachments.Inner_DataUtil(name, decoded.format(), decoded.cells().size(), Files.size(target));
      } finally {
         Files.deleteIfExists(temporary);
      }
   }

   static String init(String text, List<AiAttachments.Inner_DataUtil> files) {
      if (files.isEmpty()) {
         return text;
      }

      Matcher build = init.matcher(text.trim());
      return files.size() == 1 && (text.isBlank() || build.matches())
         ? "build \"" + files.getFirst().name() + "\"" + (!text.isBlank() && build.group(1) != null ? " " + build.group(1) : "")
         : text + "\nAttached schematic files: " + AiJson.init.toJson(files.stream().map(AiAttachments.Inner_DataUtil::name).toList());
   }

   record Inner_DataUtil(String name, String format, int cells, long bytes) {
   }
}
