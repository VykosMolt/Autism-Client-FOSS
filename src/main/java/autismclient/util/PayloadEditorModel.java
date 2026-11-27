package autismclient.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PayloadEditorModel {
   private static final char[] init_CArr = "0123456789ABCDEF".toCharArray();
   public String init_Str = "C2S";
   public String load_Str = "PLAY";
   public String read_Str = "minecraft:brand";
   public String write_Str = "";
   public int init_I = -1;
   public String update_Str = "";
   public String sync = "";
   public List<PayloadFieldCodec.Inner_CachePool> init_List = new ArrayList<>();
   public byte[] init_BArr = new byte[0];
   public byte[] load_BArr = new byte[0];
   public PayloadEditorModel.Inner_StreamUtil init_bE_StreamUtil = PayloadEditorModel.Inner_StreamUtil.USER_EDITED;
   public String flush = "";
   public String close = "Binary";
   public boolean init_Z = false;
   public boolean load_Z = false;
   public int load_I = 2147483639;
   private long init_J = 1L;
   private long load_J = -9223372036854775808L;
   private String open = "";
   private long read_J = -9223372036854775808L;
   private String poll = "";
   private long write_J = -9223372036854775808L;
   private String push = "";
   private long update_J = -9223372036854775808L;
   private String pop = "";

   public long init_J() {
      return this.init_J;
   }

   public void init_V() {
      this.init_J++;
   }

   public static PayloadEditorModel init_bE_H(autismclient.util.macro.PayloadAction action) {
      PayloadEditorModel model = new PayloadEditorModel();
      if (action == null) {
         model.init_List = new ArrayList<>(PayloadFieldCodec.defaultBrandTemplate().fields());
         model.init_Z_Z(false);
         return model;
      }

      model.init_Str = init_Str_Str(action.payloadDirection != null && !action.payloadDirection.isBlank() ? action.payloadDirection : action.sourceDirection);
      model.load_Str = load_Str_Str(action.payloadPhase);
      model.read_Str = action.channel != null && !action.channel.isBlank() ? action.channel.strip() : "minecraft:brand";
      model.write_Str = action.payloadClassName == null ? "" : action.payloadClassName;
      model.init_I = action.payloadPacketId;
      model.update_Str = action.sourceProtocol == null ? "" : action.sourceProtocol;
      model.sync = action.payloadEncodingMode == null ? "" : action.payloadEncodingMode;
      model.init_bE_StreamUtil = PayloadEditorModel.Inner_StreamUtil.init(action.payloadProvenance);
      model.init_Z = action.commandApiRecognized;
      model.load_Z = action.commandApiOverride;
      model.load_I = action.commandApiValue;
      byte[] raw = new byte[0];
      boolean haveRaw = false;
      if (action.payloadData != null && !action.payloadData.isBlank()) {
         try {
            raw = AutismPayloadSupport.parseHexOrBase64(action.payloadData);
            haveRaw = true;
         } catch (Throwable var5) {
         }
      }

      List<PayloadFieldCodec.Inner_CachePool> fields = PayloadFieldCodec.parseFields(action.payloadFields);
      if (!fields.isEmpty()) {
         model.init_List = new ArrayList<>(fields);
         if (!model.init_Z_Z(false) && haveRaw) {
            model.init_V_BArr_bE_StreamUtil(raw, PayloadEditorModel.Inner_StreamUtil.init(action.payloadProvenance));
         }
      } else {
         model.init_V_BArr_bE_StreamUtil(raw, PayloadEditorModel.Inner_StreamUtil.init(action.payloadProvenance));
      }

      return model;
   }

   public autismclient.util.macro.PayloadAction init_H_H(autismclient.util.macro.PayloadAction base) {
      autismclient.util.macro.PayloadAction action = new autismclient.util.macro.PayloadAction();
      if (base != null) {
         action.fromTag(base.toTag());
      }

      action.channel = this.read_Str != null && !this.read_Str.isBlank() ? this.read_Str.strip() : "minecraft:brand";
      action.payloadDirection = init_Str_Str(this.init_Str);
      action.sourceDirection = action.payloadDirection;
      action.payloadPhase = load_Str_Str(this.load_Str);
      action.sourceProtocol = this.update_Str != null && !this.update_Str.isBlank()
         ? this.update_Str
         : ("CONFIGURATION".equals(action.payloadPhase) ? "CONFIGURATION" : "");
      action.payloadClassName = this.write_Str == null ? "" : this.write_Str;
      action.payloadPacketId = this.init_I;
      action.payloadProvenance = this.init_bE_StreamUtil == null ? PayloadEditorModel.Inner_StreamUtil.USER_EDITED.init() : this.init_bE_StreamUtil.init();
      action.payloadEncodingMode = this.sync();
      action.payloadFields = PayloadFieldCodec.formatFields(this.init_List);
      action.payloadData = AutismPayloadSupport.toHexString(this.init_BArr);
      action.payloadJson = "";
      action.commandApiRecognized = this.init_Z;
      action.commandApiOverride = this.load_Z;
      action.commandApiValue = this.load_I;
      action.payloadScriptEnabled = false;
      action.javaSource = "";
      return action;
   }

   public PayloadFieldCodec.Inner_JsonUtil init_bJ_JsonUtil() {
      return new PayloadFieldCodec.Inner_JsonUtil(
         this.read_Str,
         PayloadFieldCodec.Inner_ConfigStore.parse(this.init_Str),
         PayloadFieldCodec.Inner_TaskQueue.parse(this.load_Str),
         PayloadFieldCodec.Inner_StreamUtil.parse(this.sync(), this.read_Str),
         this.init_List == null ? List.of() : this.init_List
      );
   }

   public boolean init_Z_Str(String script) {
      PayloadEditorModel parsed = this.init_bE();
      List<String> writerLines = new ArrayList<>();
      String[] lines = (script == null ? "" : script).replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (String raw : lines) {
         String line = raw == null ? "" : raw.strip();
         if (line.isEmpty()) {
            writerLines.add(raw == null ? "" : raw);
         } else {
            int split = init_I_Str(line);
            if (split > 0) {
               String key = line.substring(0, split).strip().toLowerCase(Locale.ROOT);
               String value = line.substring(split + 1).strip();
               switch (key) {
                  case "direction":
                     parsed.init_Str = init_Str_Str(value);
                     break;
                  case "phase":
                     parsed.load_Str = load_Str_Str(value);
                     break;
                  case "channel":
                     parsed.read_Str = value.isBlank() ? "minecraft:brand" : value;
                     break;
                  case "packetclass":
                  case "packet_class":
                     parsed.write_Str = value;
                     break;
                  case "packetid":
                  case "packet_id":
                     parsed.init_I = init_I_Str_I(value, -1);
                     break;
                  case "sourceprotocol":
                  case "source_protocol":
                     parsed.update_Str = value;
                     break;
                  default:
                     writerLines.add(raw == null ? "" : raw);
               }
            } else {
               writerLines.add(raw == null ? "" : raw);
            }
         }
      }

      parsed.init_List = new ArrayList<>(PayloadFieldCodec.parseFields(String.join("\n", writerLines)));
      if (!parsed.init_Z_Z(true)) {
         this.flush = parsed.flush;
         this.init_V();
         return false;
      } else {
         this.init_V_bE(parsed);
         this.init_bE_StreamUtil = PayloadEditorModel.Inner_StreamUtil.USER_EDITED;
         this.init_V();
         return true;
      }
   }

   public boolean load_Z_Str(String hex) {
      try {
         this.init_V_BArr_bE_StreamUtil(init_BArr_Str(hex), PayloadEditorModel.Inner_StreamUtil.USER_EDITED);
         this.flush = "";
         return true;
      } catch (Throwable t) {
         this.flush = AutismPayloadSupport.describeError(t);
         this.init_V();
         return false;
      }
   }

   public boolean read(String text) {
      this.init_V_BArr_bE_StreamUtil((text == null ? "" : text).getBytes(StandardCharsets.UTF_8), PayloadEditorModel.Inner_StreamUtil.USER_EDITED);
      return true;
   }

   public boolean write(String text) {
      PayloadEditorModel parsed = this.init_bE();
      String bodyHex = null;
      String logicalHex = null;
      String[] lines = (text == null ? "" : text).replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

      for (String raw : lines) {
         String line = raw == null ? "" : raw.strip();
         if (!line.isEmpty() && !line.startsWith("#")) {
            int split = init_I_Str(line);
            if (split >= 0) {
               String key = line.substring(0, split).strip().toLowerCase(Locale.ROOT);
               String value = line.substring(split + 1).strip();
               switch (key) {
                  case "direction":
                     parsed.init_Str = init_Str_Str(value);
                     break;
                  case "phase":
                     parsed.load_Str = load_Str_Str(value);
                     break;
                  case "channel":
                     parsed.read_Str = value.isBlank() ? parsed.read_Str : value;
                     break;
                  case "packetid":
                  case "packet_id":
                     parsed.init_I = init_I_Str_I(value, -1);
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
                     parsed.write_Str = value;
                     break;
                  case "sourceprotocol":
                  case "source_protocol":
                     parsed.update_Str = value;
               }
            }
         }
      }

      try {
         if (logicalHex != null && !logicalHex.isBlank()) {
            PayloadEditorModel.Inner_DataUtil logical = init_bE_DataUtil_Str(logicalHex);
            if (parsed.init_I >= 0 && logical.init_I != parsed.init_I) {
               throw new IllegalArgumentException("Logical packet id does not match packetId");
            }

            parsed.init_I = logical.init_I;
            parsed.read_Str = logical.init_Str;
            parsed.init_V_BArr_bE_StreamUtil(logical.init_BArr, PayloadEditorModel.Inner_StreamUtil.USER_EDITED);
         } else if (bodyHex != null) {
            parsed.init_V_BArr_bE_StreamUtil(init_BArr_Str(bodyHex), PayloadEditorModel.Inner_StreamUtil.USER_EDITED);
         }
      } catch (Throwable t) {
         this.flush = AutismPayloadSupport.describeError(t);
         this.init_V();
         return false;
      }

      this.init_V_bE(parsed);
      this.init_bE_StreamUtil = PayloadEditorModel.Inner_StreamUtil.USER_EDITED;
      this.flush = "";
      this.init_V();
      return true;
   }

   public String init_Str() {
      if (this.load_J == this.init_J) {
         return this.open;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("direction = ").append(init_Str_Str(this.init_Str)).append('\n');
      sb.append("phase = ").append(load_Str_Str(this.load_Str)).append('\n');
      sb.append("channel = ").append(this.read_Str != null && !this.read_Str.isBlank() ? this.read_Str.strip() : "minecraft:brand").append('\n');
      if (this.init_I >= 0) {
         sb.append("packetId = ").append(this.init_I).append('\n');
      }

      if (this.write_Str != null && !this.write_Str.isBlank()) {
         sb.append("packetClass = ").append(this.write_Str).append('\n');
      }

      if (this.update_Str != null && !this.update_Str.isBlank()) {
         sb.append("sourceProtocol = ").append(this.update_Str).append('\n');
      }

      sb.append('\n');
      sb.append(PayloadFieldCodec.formatFields(this.init_List));
      this.open = sb.toString();
      this.load_J = this.init_J;
      return this.open;
   }

   public String load_Str() {
      if (this.init_BArr != null && this.init_BArr.length != 0) {
         if (this.read_J == this.init_J) {
            return this.poll;
         }

         StringBuilder sb = new StringBuilder(this.init_BArr.length * 3 + this.init_BArr.length / 16 * 10);

         for (int i = 0; i < this.init_BArr.length; i++) {
            if (i > 0) {
               sb.append((char)(i % 16 == 0 ? '\n' : ' '));
            }

            int value = this.init_BArr[i] & 255;
            sb.append(init_CArr[value >>> 4]).append(init_CArr[value & 15]);
         }

         this.poll = sb.toString();
         this.read_J = this.init_J;
         return this.poll;
      } else {
         return "";
      }
   }

   public String read() {
      if (this.write_J == this.init_J) {
         return this.push;
      }

      String text = AutismPayloadSupport.decodePrintableUtf8(this.init_BArr);
      this.push = text == null ? "" : text;
      this.write_J = this.init_J;
      return this.push;
   }

   public String write() {
      if (this.update_J == this.init_J) {
         return this.pop;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("direction = ").append(init_Str_Str(this.init_Str)).append('\n');
      sb.append("phase = ").append(load_Str_Str(this.load_Str)).append('\n');
      sb.append("channel = ").append(this.read_Str == null ? "" : this.read_Str).append('\n');
      sb.append("packetId = ").append(this.init_I >= 0 ? this.init_I : "unavailable").append('\n');
      if (this.write_Str != null && !this.write_Str.isBlank()) {
         sb.append("packetClass = ").append(this.write_Str).append('\n');
      }

      if (this.update_Str != null && !this.update_Str.isBlank()) {
         sb.append("sourceProtocol = ").append(this.update_Str).append('\n');
      }

      sb.append("provenance = ").append(this.init_bE_StreamUtil == null ? PayloadEditorModel.Inner_StreamUtil.USER_EDITED.init() : this.init_bE_StreamUtil.init()).append('\n');
      sb.append("bodyBytes = ").append(this.init_BArr == null ? 0 : this.init_BArr.length).append('\n');
      sb.append("decoded = ").append(this.update()).append('\n');
      sb.append("bodyHex = ").append(AutismPayloadSupport.toHexString(this.init_BArr)).append('\n');
      if (this.init_I >= 0) {
         sb.append("logicalHex = ").append(AutismPayloadSupport.toHexString(this.init_BArr())).append('\n');
      } else {
         sb.append("logicalHex = unavailable; packet id was not captured/resolved\n");
      }

      this.pop = sb.toString();
      this.update_J = this.init_J;
      return this.pop;
   }

   public byte[] init_BArr() {
      if (this.init_I < 0) {
         return new byte[0];
      }

      byte[] packet = init_BArr_I(this.init_I);
      byte[] channelBytes = AutismPayloadSupport.encodeMcString(this.read_Str == null ? "" : this.read_Str);
      byte[] body = this.init_BArr == null ? new byte[0] : this.init_BArr;
      byte[] out = new byte[packet.length + channelBytes.length + body.length];
      System.arraycopy(packet, 0, out, 0, packet.length);
      System.arraycopy(channelBytes, 0, out, packet.length, channelBytes.length);
      System.arraycopy(body, 0, out, packet.length + channelBytes.length, body.length);
      return out;
   }

   public String update() {
      return this.init_List != null && !this.init_List.isEmpty() ? this.init_List.get(0).init_Str() : "empty";
   }

   private boolean init_Z_Z(boolean strict) {
      PayloadFieldCodec.Inner_JsonUtil template = this.init_bJ_JsonUtil();
      PayloadFieldCodec.Inner_DataUtil built = template.encode();
      if (!built.isValid()) {
         this.flush = String.join("; ", built.errors());
         if (strict) {
            this.init_V();
            return false;
         }
      }

      this.read_Str = template.channel();
      this.init_Str = template.direction().name();
      this.load_Str = template.phase().name();
      this.sync = template.encodingMode().name();
      this.init_BArr = built.bytes();
      this.load_BArr = (byte[])this.init_BArr.clone();
      this.close = init_Str_BArr(this.init_BArr);
      this.load_V();
      this.flush = "";
      this.init_V();
      return true;
   }

   private void init_V_BArr_bE_StreamUtil(byte[] bytes, PayloadEditorModel.Inner_StreamUtil source) {
      this.init_BArr = bytes == null ? new byte[0] : (byte[])bytes.clone();
      this.load_BArr = (byte[])this.init_BArr.clone();
      this.init_List = new ArrayList<>(AutismPayloadSupport.inferPayloadFields(this.init_BArr));
      this.sync = AutismPayloadSupport.inferEncodingMode(this.read_Str, this.init_BArr);
      this.close = init_Str_BArr(this.init_BArr);
      this.init_bE_StreamUtil = source == null ? PayloadEditorModel.Inner_StreamUtil.USER_EDITED : source;
      this.load_V();
      this.flush = "";
      this.init_V();
   }

   private void load_V() {
      Integer parsed = AutismPayloadSupport.detectCommandApiValue(null, this.read_Str, this.init_BArr);
      if (parsed != null) {
         this.init_Z = true;
         if (!this.load_Z) {
            this.load_I = parsed;
         }
      } else if (!this.load_Z) {
         this.init_Z = false;
      }
   }

   private String sync() {
      return this.sync != null && !this.sync.isBlank() ? this.sync : AutismPayloadSupport.inferEncodingMode(this.read_Str, this.init_BArr);
   }

   private static String init_Str_BArr(byte[] bytes) {
      if (AutismPayloadSupport.decodeMcString(bytes) != null) {
         return "Minecraft String";
      }

      if (AutismPayloadSupport.decodeJavaUtf(bytes) != null) {
         return "Java writeUTF";
      }

      String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
      return utf8 != null && !utf8.isBlank() ? "Raw UTF-8" : "Binary Hex";
   }

   private PayloadEditorModel init_bE() {
      PayloadEditorModel copy = new PayloadEditorModel();
      copy.init_V_bE(this);
      return copy;
   }

   private void init_V_bE(PayloadEditorModel other) {
      this.init_Str = other.init_Str;
      this.load_Str = other.load_Str;
      this.read_Str = other.read_Str;
      this.write_Str = other.write_Str;
      this.init_I = other.init_I;
      this.update_Str = other.update_Str;
      this.sync = other.sync;
      this.init_List = new ArrayList<>(other.init_List == null ? List.of() : other.init_List);
      this.init_BArr = other.init_BArr == null ? new byte[0] : (byte[])other.init_BArr.clone();
      this.load_BArr = other.load_BArr == null ? new byte[0] : (byte[])other.load_BArr.clone();
      this.init_bE_StreamUtil = other.init_bE_StreamUtil;
      this.flush = other.flush;
      this.close = other.close;
      this.init_Z = other.init_Z;
      this.load_Z = other.load_Z;
      this.load_I = other.load_I;
      this.init_V();
   }

   private static byte[] init_BArr_Str(String text) {
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

   private static PayloadEditorModel.Inner_DataUtil init_bE_DataUtil_Str(String hex) {
      byte[] bytes = init_BArr_Str(hex);
      int[] index = new int[]{0};
      Integer packetId = init_Integer_BArr_IArr(bytes, index);
      if (packetId != null && packetId >= 0) {
         String channel = init_Str_BArr_IArr(bytes, index);
         byte[] body = new byte[Math.max(0, bytes.length - index[0])];
         if (body.length > 0) {
            System.arraycopy(bytes, index[0], body, 0, body.length);
         }

         return new PayloadEditorModel.Inner_DataUtil(packetId, channel, body);
      } else {
         throw new IllegalArgumentException("Missing logical packet id");
      }
   }

   private static String init_Str_BArr_IArr(byte[] bytes, int[] index) {
      Integer len = init_Integer_BArr_IArr(bytes, index);
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

   private static Integer init_Integer_BArr_IArr(byte[] bytes, int[] indexRef) {
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

   private static byte[] init_BArr_I(int value) {
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

   private static int init_I_Str(String line) {
      int eq = line.indexOf(61);
      int colon = line.indexOf(58);
      if (eq < 0) {
         return colon;
      } else {
         return colon < 0 ? eq : Math.min(eq, colon);
      }
   }

   private static String init_Str_Str(String raw) {
      return "S2C".equalsIgnoreCase(raw == null ? "" : raw.strip()) ? "S2C" : "C2S";
   }

   private static String load_Str_Str(String raw) {
      String value = raw == null ? "" : raw.strip().toUpperCase(Locale.ROOT);
      return value.contains("CONFIG") ? "CONFIGURATION" : "PLAY";
   }

   private static int init_I_Str_I(String value, int fallback) {
      try {
         return Integer.parseInt(value == null ? "" : value.strip());
      } catch (Throwable ignored) {
         return fallback;
      }
   }

   private record Inner_DataUtil(int init_I, String init_Str, byte[] init_BArr) {
   }

   public enum Inner_StreamUtil {
      CAPTURED_EXACT("capturedExact"),
      REENCODED_FROM_PAYLOAD("reencodedFromPayload"),
      USER_EDITED("userEdited");

      private final String init_Str;

      Inner_StreamUtil(String tag) {
         this.init_Str = tag;
      }

      public String init() {
         return this.init_Str;
      }

      public static PayloadEditorModel.Inner_StreamUtil init(String raw) {
         if (raw != null && !raw.isBlank()) {
            String normalized = raw.strip().toLowerCase(Locale.ROOT);

            for (PayloadEditorModel.Inner_StreamUtil value : values()) {
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
