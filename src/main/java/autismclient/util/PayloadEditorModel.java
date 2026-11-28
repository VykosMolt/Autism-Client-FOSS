package autismclient.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PayloadEditorModel {
   private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
   public String direction = "C2S";
   public String phase = "PLAY";
   public String channel = "minecraft:brand";
   public String payloadClassName = "";
   public int packetId = -1;
   public String sourceProtocol = "";
   public String encodingMode = "";
   public List<PayloadFieldCodec.PayloadField> fields = new ArrayList<>();
   public byte[] bodyBytes = new byte[0];
   public byte[] baselineBody = new byte[0];
   public PayloadEditorModel.PayloadProvenance provenance = PayloadEditorModel.PayloadProvenance.USER_EDITED;
   public String error = "";
   public String bodyKind = "Binary";
   public boolean hasCommandApiValue = false;
   public boolean commandApiOverride = false;
   public int commandApiValue = 2147483639;
   private long revision = 1L;
   private long scriptRevision = -9223372036854775808L;
   private String cachedScript = "";
   private long hexDumpRevision = -9223372036854775808L;
   private String cachedHexDump = "";
   private long readCacheRevision = -9223372036854775808L;
   private String cachedRead = "";
   private long writeCacheRevision = -9223372036854775808L;
   private String cachedWrite = "";

   public long revision() {
      return this.revision;
   }

   public void bumpRevision() {
      this.revision++;
   }

   public static PayloadEditorModel fromAction(autismclient.util.macro.PayloadAction action) {
      PayloadEditorModel model = new PayloadEditorModel();
      if (action == null) {
         model.fields = new ArrayList<>(PayloadFieldCodec.defaultBrandTemplate().fields());
         model.rebuildBody(false);
         return model;
      }

      model.direction = normalizeDirection(action.payloadDirection != null && !action.payloadDirection.isBlank() ? action.payloadDirection : action.sourceDirection);
      model.phase = normalizePhase(action.payloadPhase);
      model.channel = action.channel != null && !action.channel.isBlank() ? action.channel.strip() : "minecraft:brand";
      model.payloadClassName = action.payloadClassName == null ? "" : action.payloadClassName;
      model.packetId = action.payloadPacketId;
      model.sourceProtocol = action.sourceProtocol == null ? "" : action.sourceProtocol;
      model.encodingMode = action.payloadEncodingMode == null ? "" : action.payloadEncodingMode;
      model.provenance = PayloadEditorModel.PayloadProvenance.parse(action.payloadProvenance);
      model.hasCommandApiValue = action.commandApiRecognized;
      model.commandApiOverride = action.commandApiOverride;
      model.commandApiValue = action.commandApiValue;
      byte[] raw = new byte[0];
      boolean haveRaw = false;
      if (action.payloadData != null && !action.payloadData.isBlank()) {
         try {
            raw = AutismPayloadSupport.parseHexOrBase64(action.payloadData);
            haveRaw = true;
         } catch (Throwable var5) {
         }
      }

      List<PayloadFieldCodec.PayloadField> fields = PayloadFieldCodec.parseFields(action.payloadFields);
      if (!fields.isEmpty()) {
         model.fields = new ArrayList<>(fields);
         if (!model.rebuildBody(false) && haveRaw) {
            model.setBody(raw, PayloadEditorModel.PayloadProvenance.parse(action.payloadProvenance));
         }
      } else {
         model.setBody(raw, PayloadEditorModel.PayloadProvenance.parse(action.payloadProvenance));
      }

      return model;
   }

   public autismclient.util.macro.PayloadAction toAction(autismclient.util.macro.PayloadAction base) {
      autismclient.util.macro.PayloadAction action = new autismclient.util.macro.PayloadAction();
      if (base != null) {
         action.fromTag(base.toTag());
      }

      action.channel = this.channel != null && !this.channel.isBlank() ? this.channel.strip() : "minecraft:brand";
      action.payloadDirection = normalizeDirection(this.direction);
      action.sourceDirection = action.payloadDirection;
      action.payloadPhase = normalizePhase(this.phase);
      action.sourceProtocol = this.sourceProtocol != null && !this.sourceProtocol.isBlank()
         ? this.sourceProtocol
         : ("CONFIGURATION".equals(action.payloadPhase) ? "CONFIGURATION" : "");
      action.payloadClassName = this.payloadClassName == null ? "" : this.payloadClassName;
      action.payloadPacketId = this.packetId;
      action.payloadProvenance = this.provenance == null ? PayloadEditorModel.PayloadProvenance.USER_EDITED.id() : this.provenance.id();
      action.payloadEncodingMode = this.encodingMode();
      action.payloadFields = PayloadFieldCodec.formatFields(this.fields);
      action.payloadData = AutismPayloadSupport.toHexString(this.bodyBytes);
      action.payloadJson = "";
      action.commandApiRecognized = this.hasCommandApiValue;
      action.commandApiOverride = this.commandApiOverride;
      action.commandApiValue = this.commandApiValue;
      action.payloadScriptEnabled = false;
      action.javaSource = "";
      return action;
   }

   public PayloadFieldCodec.PayloadTemplate toSpec() {
      return new PayloadFieldCodec.PayloadTemplate(
         this.channel,
         PayloadFieldCodec.PayloadDirection.parse(this.direction),
         PayloadFieldCodec.ProtocolPhase.parse(this.phase),
         PayloadFieldCodec.EncodingMode.parse(this.encodingMode(), this.channel),
         this.fields == null ? List.of() : this.fields
      );
   }

   public boolean parseScript(String script) {
      PayloadEditorModel parsed = this.copy();
      List<String> writerLines = new ArrayList<>();
      String[] lines = (script == null ? "" : script).replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (String raw : lines) {
         String line = raw == null ? "" : raw.strip();
         if (line.isEmpty()) {
            writerLines.add(raw == null ? "" : raw);
         } else {
            int split = separatorIndex(line);
            if (split > 0) {
               String key = line.substring(0, split).strip().toLowerCase(Locale.ROOT);
               String value = line.substring(split + 1).strip();
               switch (key) {
                  case "direction":
                     parsed.direction = normalizeDirection(value);
                     break;
                  case "phase":
                     parsed.phase = normalizePhase(value);
                     break;
                  case "channel":
                     parsed.channel = value.isBlank() ? "minecraft:brand" : value;
                     break;
                  case "packetclass":
                  case "packet_class":
                     parsed.payloadClassName = value;
                     break;
                  case "packetid":
                  case "packet_id":
                     parsed.packetId = parseIntOr(value, -1);
                     break;
                  case "sourceprotocol":
                  case "source_protocol":
                     parsed.sourceProtocol = value;
                     break;
                  default:
                     writerLines.add(raw == null ? "" : raw);
               }
            } else {
               writerLines.add(raw == null ? "" : raw);
            }
         }
      }

      parsed.fields = new ArrayList<>(PayloadFieldCodec.parseFields(String.join("\n", writerLines)));
      if (!parsed.rebuildBody(true)) {
         this.error = parsed.error;
         this.bumpRevision();
         return false;
      } else {
         this.copyFrom(parsed);
         this.provenance = PayloadEditorModel.PayloadProvenance.USER_EDITED;
         this.bumpRevision();
         return true;
      }
   }

   public boolean parseHex(String hex) {
      try {
         this.setBody(hexToBytes(hex), PayloadEditorModel.PayloadProvenance.USER_EDITED);
         this.error = "";
         return true;
      } catch (Throwable t) {
         this.error = AutismPayloadSupport.describeError(t);
         this.bumpRevision();
         return false;
      }
   }

   public boolean setUtf8(String text) {
      this.setBody((text == null ? "" : text).getBytes(StandardCharsets.UTF_8), PayloadEditorModel.PayloadProvenance.USER_EDITED);
      return true;
   }

   public boolean setLogical(String text) {
      PayloadEditorModel parsed = this.copy();
      String bodyHex = null;
      String logicalHex = null;
      String[] lines = (text == null ? "" : text).replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (String raw : lines) {
         String line = raw == null ? "" : raw.strip();
         if (!line.isEmpty() && !line.startsWith("#")) {
            int split = separatorIndex(line);
            if (split >= 0) {
               String key = line.substring(0, split).strip().toLowerCase(Locale.ROOT);
               String value = line.substring(split + 1).strip();
               switch (key) {
                  case "direction":
                     parsed.direction = normalizeDirection(value);
                     break;
                  case "phase":
                     parsed.phase = normalizePhase(value);
                     break;
                  case "channel":
                     parsed.channel = value.isBlank() ? parsed.channel : value;
                     break;
                  case "packetid":
                  case "packet_id":
                     parsed.packetId = parseIntOr(value, -1);
                     break;
                  case "bodyhex":
                  case "body_hex":
                     bodyHex = value;
                     break;
                  case "logicalhex":
                  case "logical_hex":
                     logicalHex = value;
                     break;
                  case "packetclass":
                  case "packet_class":
                     parsed.payloadClassName = value;
                     break;
                  case "sourceprotocol":
                  case "source_protocol":
                     parsed.sourceProtocol = value;
               }
            }
         }
      }

      try {
         if (logicalHex != null && !logicalHex.isBlank()) {
            PayloadEditorModel.DecodedPayload logical = decodeLogical(logicalHex);
            if (parsed.packetId >= 0 && logical.packetId != parsed.packetId) {
               throw new IllegalArgumentException("Logical packet id does not match packetId");
            }

            parsed.packetId = logical.packetId;
            parsed.channel = logical.channel;
            parsed.setBody(logical.body, PayloadEditorModel.PayloadProvenance.USER_EDITED);
         } else if (bodyHex != null) {
            parsed.setBody(hexToBytes(bodyHex), PayloadEditorModel.PayloadProvenance.USER_EDITED);
         }
      } catch (Throwable t) {
         this.error = AutismPayloadSupport.describeError(t);
         this.bumpRevision();
         return false;
      }

      this.copyFrom(parsed);
      this.provenance = PayloadEditorModel.PayloadProvenance.USER_EDITED;
      this.error = "";
      this.bumpRevision();
      return true;
   }

   public String toScript() {
      if (this.scriptRevision == this.revision) {
         return this.cachedScript;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("direction = ").append(normalizeDirection(this.direction)).append('\n');
      sb.append("phase = ").append(normalizePhase(this.phase)).append('\n');
      sb.append("channel = ").append(this.channel != null && !this.channel.isBlank() ? this.channel.strip() : "minecraft:brand").append('\n');
      if (this.packetId >= 0) {
         sb.append("packetId = ").append(this.packetId).append('\n');
      }

      if (this.payloadClassName != null && !this.payloadClassName.isBlank()) {
         sb.append("packetClass = ").append(this.payloadClassName).append('\n');
      }

      if (this.sourceProtocol != null && !this.sourceProtocol.isBlank()) {
         sb.append("sourceProtocol = ").append(this.sourceProtocol).append('\n');
      }

      sb.append('\n');
      sb.append(PayloadFieldCodec.formatFields(this.fields));
      this.cachedScript = sb.toString();
      this.scriptRevision = this.revision;
      return this.cachedScript;
   }

   public String toHexDump() {
      if (this.bodyBytes != null && this.bodyBytes.length != 0) {
         if (this.hexDumpRevision == this.revision) {
            return this.cachedHexDump;
         }

         StringBuilder sb = new StringBuilder(this.bodyBytes.length * 3 + this.bodyBytes.length / 16 * 10);

         for (int i = 0; i < this.bodyBytes.length; i++) {
            if (i > 0) {
               sb.append((char)(i % 16 == 0 ? '\n' : ' '));
            }

            int value = this.bodyBytes[i] & 255;
            sb.append(HEX_DIGITS[value >>> 4]).append(HEX_DIGITS[value & 15]);
         }

         this.cachedHexDump = sb.toString();
         this.hexDumpRevision = this.revision;
         return this.cachedHexDump;
      } else {
         return "";
      }
   }

   public String utf8Text() {
      if (this.readCacheRevision == this.revision) {
         return this.cachedRead;
      }

      String text = AutismPayloadSupport.decodePrintableUtf8(this.bodyBytes);
      this.cachedRead = text == null ? "" : text;
      this.readCacheRevision = this.revision;
      return this.cachedRead;
   }

   public String logicalText() {
      if (this.writeCacheRevision == this.revision) {
         return this.cachedWrite;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("direction = ").append(normalizeDirection(this.direction)).append('\n');
      sb.append("phase = ").append(normalizePhase(this.phase)).append('\n');
      sb.append("channel = ").append(this.channel == null ? "" : this.channel).append('\n');
      sb.append("packetId = ").append(this.packetId >= 0 ? this.packetId : "unavailable").append('\n');
      if (this.payloadClassName != null && !this.payloadClassName.isBlank()) {
         sb.append("packetClass = ").append(this.payloadClassName).append('\n');
      }

      if (this.sourceProtocol != null && !this.sourceProtocol.isBlank()) {
         sb.append("sourceProtocol = ").append(this.sourceProtocol).append('\n');
      }

      sb.append("provenance = ").append(this.provenance == null ? PayloadEditorModel.PayloadProvenance.USER_EDITED.id() : this.provenance.id()).append('\n');
      sb.append("bodyBytes = ").append(this.bodyBytes == null ? 0 : this.bodyBytes.length).append('\n');
      sb.append("decoded = ").append(this.decodedSummary()).append('\n');
      sb.append("bodyHex = ").append(AutismPayloadSupport.toHexString(this.bodyBytes)).append('\n');
      if (this.packetId >= 0) {
         sb.append("logicalHex = ").append(AutismPayloadSupport.toHexString(this.logicalPacketBytes())).append('\n');
      } else {
         sb.append("logicalHex = unavailable; packet id was not captured/resolved\n");
      }

      this.cachedWrite = sb.toString();
      this.writeCacheRevision = this.revision;
      return this.cachedWrite;
   }

   public byte[] logicalPacketBytes() {
      if (this.packetId < 0) {
         return new byte[0];
      }

      byte[] packet = encodeVarInt(this.packetId);
      byte[] channelBytes = AutismPayloadSupport.encodeMcString(this.channel == null ? "" : this.channel);
      byte[] body = this.bodyBytes == null ? new byte[0] : this.bodyBytes;
      byte[] out = new byte[packet.length + channelBytes.length + body.length];
      System.arraycopy(packet, 0, out, 0, packet.length);
      System.arraycopy(channelBytes, 0, out, packet.length, channelBytes.length);
      System.arraycopy(body, 0, out, packet.length + channelBytes.length, body.length);
      return out;
   }

   public String decodedSummary() {
      return this.fields != null && !this.fields.isEmpty() ? this.fields.get(0).init_Str() : "empty";
   }

   private boolean rebuildBody(boolean strict) {
      PayloadFieldCodec.PayloadTemplate template = this.toSpec();
      PayloadFieldCodec.EncodeResult built = template.encode();
      if (!built.isValid()) {
         this.error = String.join("; ", built.errors());
         if (strict) {
            this.bumpRevision();
            return false;
         }
      }

      this.channel = template.channel();
      this.direction = template.direction().name();
      this.phase = template.phase().name();
      this.encodingMode = template.encodingMode().name();
      this.bodyBytes = built.bytes();
      this.baselineBody = (byte[])this.bodyBytes.clone();
      this.bodyKind = guessBodyKind(this.bodyBytes);
      this.detectCommandApi();
      this.error = "";
      this.bumpRevision();
      return true;
   }

   private void setBody(byte[] bytes, PayloadEditorModel.PayloadProvenance source) {
      this.bodyBytes = bytes == null ? new byte[0] : (byte[])bytes.clone();
      this.baselineBody = (byte[])this.bodyBytes.clone();
      this.fields = new ArrayList<>(AutismPayloadSupport.inferPayloadFields(this.bodyBytes));
      this.encodingMode = AutismPayloadSupport.inferEncodingMode(this.channel, this.bodyBytes);
      this.bodyKind = guessBodyKind(this.bodyBytes);
      this.provenance = source == null ? PayloadEditorModel.PayloadProvenance.USER_EDITED : source;
      this.detectCommandApi();
      this.error = "";
      this.bumpRevision();
   }

   private void detectCommandApi() {
      Integer parsed = AutismPayloadSupport.detectCommandApiValue(null, this.channel, this.bodyBytes);
      if (parsed != null) {
         this.hasCommandApiValue = true;
         if (!this.commandApiOverride) {
            this.commandApiValue = parsed;
         }
      } else if (!this.commandApiOverride) {
         this.hasCommandApiValue = false;
      }
   }

   private String encodingMode() {
      return this.encodingMode != null && !this.encodingMode.isBlank() ? this.encodingMode : AutismPayloadSupport.inferEncodingMode(this.channel, this.bodyBytes);
   }

   private static String guessBodyKind(byte[] bytes) {
      if (AutismPayloadSupport.decodeMcString(bytes) != null) {
         return "Minecraft String";
      }

      if (AutismPayloadSupport.decodeJavaUtf(bytes) != null) {
         return "Java writeUTF";
      }

      String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
      return utf8 != null && !utf8.isBlank() ? "Raw UTF-8" : "Binary Hex";
   }

   private PayloadEditorModel copy() {
      PayloadEditorModel copy = new PayloadEditorModel();
      copy.copyFrom(this);
      return copy;
   }

   private void copyFrom(PayloadEditorModel other) {
      this.direction = other.direction;
      this.phase = other.phase;
      this.channel = other.channel;
      this.payloadClassName = other.payloadClassName;
      this.packetId = other.packetId;
      this.sourceProtocol = other.sourceProtocol;
      this.encodingMode = other.encodingMode;
      this.fields = new ArrayList<>(other.fields == null ? List.of() : other.fields);
      this.bodyBytes = other.bodyBytes == null ? new byte[0] : (byte[])other.bodyBytes.clone();
      this.baselineBody = other.baselineBody == null ? new byte[0] : (byte[])other.baselineBody.clone();
      this.provenance = other.provenance;
      this.error = other.error;
      this.bodyKind = other.bodyKind;
      this.hasCommandApiValue = other.hasCommandApiValue;
      this.commandApiOverride = other.commandApiOverride;
      this.commandApiValue = other.commandApiValue;
      this.bumpRevision();
   }

   private static byte[] hexToBytes(String text) {
      String value = text == null ? "" : text.strip();
      if (value.isEmpty()) {
         return new byte[0];
      }

      String compact = value.replaceAll("(?i)0x", "").replaceAll("\\s+", "");
      if (compact.isEmpty()) {
         return new byte[0];
      }

      if ((compact.length() & 1) != 0) {
         throw new IllegalArgumentException("Hex needs byte pairs");
      }

      if (!compact.matches("(?i)[0-9a-f]+")) {
         throw new IllegalArgumentException("Only hex digits are allowed");
      }

      byte[] out = new byte[compact.length() / 2];

      for (int i = 0; i < compact.length(); i += 2) {
         out[i / 2] = (byte)Integer.parseInt(compact.substring(i, i + 2), 16);
      }

      return out;
   }

   private static PayloadEditorModel.DecodedPayload decodeLogical(String hex) {
      byte[] bytes = hexToBytes(hex);
      int[] index = new int[]{0};
      Integer packetId = readVarInt(bytes, index);
      if (packetId != null && packetId >= 0) {
         String channel = readMcString(bytes, index);
         byte[] body = new byte[Math.max(0, bytes.length - index[0])];
         if (body.length > 0) {
            System.arraycopy(bytes, index[0], body, 0, body.length);
         }

         return new PayloadEditorModel.DecodedPayload(packetId, channel, body);
      } else {
         throw new IllegalArgumentException("Missing logical packet id");
      }
   }

   private static String readMcString(byte[] bytes, int[] index) {
      Integer len = readVarInt(bytes, index);
      if (len != null && len >= 0) {
         if (index[0] + len > bytes.length) {
            throw new IllegalArgumentException("Channel length exceeds logical packet");
         } else {
            String value = new String(bytes, index[0], len, StandardCharsets.UTF_8);
            index[0] += len;
            if (value.isBlank()) {
               throw new IllegalArgumentException("Channel cannot be blank");
            } else {
               return value;
            }
         }
      } else {
         throw new IllegalArgumentException("Invalid channel length");
      }
   }

   private static Integer readVarInt(byte[] bytes, int[] indexRef) {
      int value = 0;
      int position = 0;
      int index = Math.max(0, indexRef[0]);

      while (index < bytes.length) {
         byte currentByte = bytes[index++];
         value |= (currentByte & 127) << position;
         if ((currentByte & 128) == 0) {
            indexRef[0] = index;
            return value;
         }

         position += 7;
         if (position >= 32) {
            return null;
         }
      }

      return null;
   }

   private static byte[] encodeVarInt(int value) {
      byte[] out = new byte[5];
      int index = 0;

      while ((value & -128) != 0) {
         out[index++] = (byte)(value & 127 | 128);
         value >>>= 7;
      }

      out[index++] = (byte)value;
      byte[] trimmed = new byte[index];
      System.arraycopy(out, 0, trimmed, 0, index);
      return trimmed;
   }

   private static int separatorIndex(String line) {
      int eq = line.indexOf(61);
      int colon = line.indexOf(58);
      if (eq < 0) {
         return colon;
      } else {
         return colon < 0 ? eq : Math.min(eq, colon);
      }
   }

   private static String normalizeDirection(String raw) {
      return "S2C".equalsIgnoreCase(raw == null ? "" : raw.strip()) ? "S2C" : "C2S";
   }

   private static String normalizePhase(String raw) {
      String value = raw == null ? "" : raw.strip().toUpperCase(Locale.ROOT);
      return value.contains("CONFIG") ? "CONFIGURATION" : "PLAY";
   }

   private static int parseIntOr(String value, int fallback) {
      try {
         return Integer.parseInt(value == null ? "" : value.strip());
      } catch (Throwable ignored) {
         return fallback;
      }
   }

   private record DecodedPayload(int packetId, String channel, byte[] body) {
   }

   public enum PayloadProvenance {
      CAPTURED_EXACT("capturedExact"),
      REENCODED_FROM_PAYLOAD("reencodedFromPayload"),
      USER_EDITED("userEdited");

      private final String init_Str;

      PayloadProvenance(String tag) {
         this.init_Str = tag;
      }

      public String id() {
         return this.init_Str;
      }

      public static PayloadEditorModel.PayloadProvenance parse(String raw) {
         if (raw != null && !raw.isBlank()) {
            String normalized = raw.strip().toLowerCase(Locale.ROOT);

            for (PayloadEditorModel.PayloadProvenance value : values()) {
               if (value.init_Str.equalsIgnoreCase(normalized) || value.name().equalsIgnoreCase(normalized)) {
                  return value;
               }
            }

            return USER_EDITED;
         } else {
            return USER_EDITED;
         }
      }
   }
}
