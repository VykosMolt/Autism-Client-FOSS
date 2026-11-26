package autismclient.util.sync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class LanSyncPacket {
   protected final LanSyncPacketType init_StreamUtil;
   protected String init_Str;

   public LanSyncPacket(LanSyncPacketType type, String sessionId) {
      this.init_StreamUtil = type;
      this.init_Str = sessionId != null ? sessionId : "";
   }

   public LanSyncPacketType init_StreamUtil() {
      return this.init_StreamUtil;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public void init(String sessionId) {
      this.init_Str = sessionId;
   }

   public abstract void init(DataOutputStream var1) throws IOException;

   public abstract void init(DataInputStream var1) throws IOException;

   protected static int init(DataInputStream in, int limit) throws IOException {
      int count = in.readInt();
      if (count >= 0 && count <= limit) {
         return count;
      } else {
         throw new IOException("Collection length out of range: " + count);
      }
   }

   public static LanSyncPacket init(LanSyncPacketType type, String sessionId) {
      switch (type) {
         case SEARCH_REQUEST:
            return new LanSyncPacket.CharEncoder(sessionId);
         case SESSION:
            return new LanSyncPacket.TokenStore(sessionId, 0L, "");
         case JOIN:
            return new LanSyncPacket.JsonUtil(sessionId, "");
         case REQUEST_CLIENT_LIST:
            return new LanSyncPacket.TableIndex(sessionId);
         case CLIENT_LIST:
            return new LanSyncPacket.Inner_StreamUtil(sessionId, new ArrayList<>());
         case LEAVE:
            return new LanSyncPacket.StateCache(sessionId, "");
         case REQUEST_MACRO:
            return new LanSyncPacket.RingBuffer(sessionId, "");
         case MACRO_DATA:
            return new LanSyncPacket.RenderState(sessionId, "", "", "", (byte)0);
         case MACRO_DELETE:
            return new LanSyncPacket.EventBus(sessionId, "", "");
         case MACRO_LIST:
            return new LanSyncPacket.FileCache(sessionId, "", new ArrayList<>());
         case QUEUE_SYNC:
            return new LanSyncPacket.PoolEntry(sessionId, "", "");
         case PRESET_DATA:
            return new LanSyncPacket.IoWorker(sessionId, "", "", "");
         case CHAT_MESSAGE:
            return new LanSyncPacket.Inner_DataUtil(sessionId, "", "");
         case TCP_COMMAND_ACK:
            return new LanSyncPacket.MethodUtil(sessionId, "", 0L);
         case PLAYER_OFFSETS:
            return new LanSyncPacket.PacketUtil(sessionId, new HashMap<>());
         case CLIENT_OFFSET_UPDATE:
            return new LanSyncPacket.CachePool(sessionId, "", "", 0);
         case PREPARE_EXECUTION:
            return new LanSyncPacket.HashUtil(sessionId, 0L, false, "", "", "");
         case CLIENT_READY:
            return new LanSyncPacket.BufferHelper(sessionId, 0L, "");
         case EXECUTE_NOW:
            return new LanSyncPacket.ConfigStore(sessionId, 0L, 0L);
         case GO:
            return new LanSyncPacket.TaskQueue(sessionId, 0L);
         case HEARTBEAT:
            return new LanSyncPacket.NetHelper(sessionId, "", 0L);
         case SYNC_STATE_UPDATE:
            return new LanSyncPacket.FieldCache(sessionId, 0, "");
         case REQUEST_SYNC:
            return new LanSyncPacket.BitSetUtil(sessionId, "", false, "", "");
         case OFFSET_SYNC:
            return new LanSyncPacket.ByteReader(sessionId, "", "", 0);
         case MACRO_STEP_PROGRESS:
            return new LanSyncPacket.TextFormat(sessionId, "", 0, 0, "");
         case MACRO_DATA_CHUNK:
            return new LanSyncPacket.UiHelper(sessionId, "", "", (byte)0, 0, 0, new byte[0]);
         case MACRO_ASSIGNMENT_SYNC:
            return new LanSyncPacket.SyncManager(sessionId, "", "");
         default:
            return null;
      }
   }

   public static class BitSetUtil extends LanSyncPacket {
      public String load;
      public boolean init_Z;
      public String read;
      public String write;
      public int init_I = 0;

      public BitSetUtil(String sessionId, String senderUsername, boolean isMacro, String macroName, String command) {
         super(LanSyncPacketType.REQUEST_SYNC, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.init_Z = isMacro;
         this.read = macroName != null ? macroName : "";
         this.write = command != null ? command : "";
      }

      public BitSetUtil() {
         super(LanSyncPacketType.REQUEST_SYNC, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeBoolean(this.init_Z);
         out.writeUTF(this.read);
         out.writeUTF(this.write);
         out.writeInt(this.init_I);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.init_Z = in.readBoolean();
         this.read = in.readUTF();
         this.write = in.readUTF();

         try {
            this.init_I = in.readInt();
         } catch (EOFException e) {
            this.init_I = 0;
         }
      }
   }

   public static class BufferHelper extends LanSyncPacket {
      public long init_J;
      public String load;

      public BufferHelper(String sessionId, long executionId, String senderUsername) {
         super(LanSyncPacketType.CLIENT_READY, sessionId);
         this.init_J = executionId;
         this.load = senderUsername;
      }

      public BufferHelper() {
         super(LanSyncPacketType.CLIENT_READY, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeLong(this.init_J);
         out.writeUTF(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.init_J = in.readLong();
         this.load = in.readUTF();
      }
   }

   public static class ByteReader extends LanSyncPacket {
      public String load;
      public String read;
      public int init_I;

      public ByteReader(String sessionId, String senderUsername, String targetUsername, int offsetMs) {
         super(LanSyncPacketType.OFFSET_SYNC, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.read = targetUsername != null ? targetUsername : "";
         this.init_I = offsetMs;
      }

      public ByteReader() {
         super(LanSyncPacketType.OFFSET_SYNC, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
         out.writeInt(this.init_I);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
         this.init_I = in.readInt();
      }
   }

   public static class CachePool extends LanSyncPacket {
      public String load;
      public String read;
      public int init_I;

      public CachePool(String sessionId, String senderUsername, String targetUsername, int offsetMs) {
         super(LanSyncPacketType.CLIENT_OFFSET_UPDATE, sessionId);
         this.load = senderUsername;
         this.read = targetUsername;
         this.init_I = offsetMs;
      }

      public CachePool() {
         super(LanSyncPacketType.CLIENT_OFFSET_UPDATE, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
         out.writeInt(this.init_I);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
         this.init_I = in.readInt();
      }
   }

   public static class CharEncoder extends LanSyncPacket {
      public CharEncoder(String sessionId) {
         super(LanSyncPacketType.SEARCH_REQUEST, sessionId);
      }

      public CharEncoder() {
         super(LanSyncPacketType.SEARCH_REQUEST, "");
      }

      @Override
      public void init(DataOutputStream out) {
      }

      @Override
      public void init(DataInputStream in) {
      }
   }

   public static class ConfigStore extends LanSyncPacket {
      public long init_J;
      public long load;

      public ConfigStore(String sessionId, long executionId, long targetTime) {
         super(LanSyncPacketType.EXECUTE_NOW, sessionId);
         this.init_J = executionId;
         this.load = targetTime;
      }

      public ConfigStore() {
         super(LanSyncPacketType.EXECUTE_NOW, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeLong(this.init_J);
         out.writeLong(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.init_J = in.readLong();
         this.load = in.readLong();
      }
   }

   public static class EventBus extends LanSyncPacket {
      public String load;
      public String read;

      public EventBus(String sessionId, String senderUsername, String macroName) {
         super(LanSyncPacketType.MACRO_DELETE, sessionId);
         this.load = senderUsername;
         this.read = macroName;
      }

      public EventBus() {
         super(LanSyncPacketType.MACRO_DELETE, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
      }
   }

   public static class FieldCache extends LanSyncPacket {
      public int init_I;
      public String load;

      public FieldCache(String sessionId, int stateOrdinal, String detail) {
         super(LanSyncPacketType.SYNC_STATE_UPDATE, sessionId);
         this.init_I = stateOrdinal;
         this.load = detail != null ? detail : "";
      }

      public FieldCache() {
         super(LanSyncPacketType.SYNC_STATE_UPDATE, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeInt(this.init_I);
         out.writeUTF(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.init_I = in.readInt();
         this.load = in.readUTF();
      }
   }

   public static class FileCache extends LanSyncPacket {
      public String load;
      public List<String> init_List = new ArrayList<>();

      public FileCache(String sessionId, String senderUsername, List<String> macroNames) {
         super(LanSyncPacketType.MACRO_LIST, sessionId);
         this.load = senderUsername;
         this.init_List = macroNames;
      }

      public FileCache() {
         super(LanSyncPacketType.MACRO_LIST, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeInt(this.init_List.size());

         for (String name : this.init_List) {
            out.writeUTF(name);
         }
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         int size = init(in, 100000);
         this.init_List.clear();

         for (int i = 0; i < size; i++) {
            this.init_List.add(in.readUTF());
         }
      }
   }

   public static class HashUtil extends LanSyncPacket {
      public long init_J;
      public boolean init_Z;
      public String load_Str;
      public String read;
      public String write;
      public String update = "";
      public int init_I = 0;
      public long load_J = -1L;

      public HashUtil(String sessionId, long executionId, boolean isMacro, String targetName, String queueData, String senderUsername) {
         super(LanSyncPacketType.PREPARE_EXECUTION, sessionId);
         this.init_J = executionId;
         this.init_Z = isMacro;
         this.load_Str = targetName;
         this.read = queueData != null ? queueData : "";
         this.write = senderUsername;
      }

      public HashUtil(
         String sessionId, long executionId, boolean isMacro, String targetName, String queueData, String senderUsername, int executionMethod, long targetTick
      ) {
         this(sessionId, executionId, isMacro, targetName, queueData, senderUsername);
         this.init_I = executionMethod;
         this.load_J = targetTick;
      }

      public HashUtil() {
         super(LanSyncPacketType.PREPARE_EXECUTION, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeLong(this.init_J);
         out.writeBoolean(this.init_Z);
         out.writeUTF(this.load_Str);
         out.writeUTF(this.read);
         out.writeUTF(this.write);
         out.writeUTF(this.update);
         out.writeInt(this.init_I);
         out.writeLong(this.load_J);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.init_J = in.readLong();
         this.init_Z = in.readBoolean();
         this.load_Str = in.readUTF();
         this.read = in.readUTF();

         try {
            this.write = in.readUTF();
         } catch (EOFException e) {
            this.write = "";
            return;
         }

         try {
            this.update = in.readUTF();
         } catch (EOFException e) {
            this.update = "";
            return;
         }

         try {
            this.init_I = in.readInt();
            this.load_J = in.readLong();
         } catch (EOFException e) {
            this.init_I = 0;
            this.load_J = -1L;
         }
      }
   }

   public static class Inner_DataUtil extends LanSyncPacket {
      public String load;
      public String read;

      public Inner_DataUtil(String sessionId, String senderUsername, String message) {
         super(LanSyncPacketType.CHAT_MESSAGE, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.read = message != null ? message : "";
      }

      public Inner_DataUtil() {
         super(LanSyncPacketType.CHAT_MESSAGE, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
      }
   }

   public static class Inner_StreamUtil extends LanSyncPacket {
      public List<LanSyncPacket.Inner_StreamUtil.Inner_DataUtil> init_List = new ArrayList<>();
      public int init_I = 0;

      public Inner_StreamUtil(String sessionId, List<LanSyncPacket.Inner_StreamUtil.Inner_DataUtil> clients) {
         super(LanSyncPacketType.CLIENT_LIST, sessionId);
         this.init_List = clients;
      }

      public Inner_StreamUtil(String sessionId, List<LanSyncPacket.Inner_StreamUtil.Inner_DataUtil> clients, int executionMethod) {
         this(sessionId, clients);
         this.init_I = executionMethod;
      }

      public Inner_StreamUtil() {
         super(LanSyncPacketType.CLIENT_LIST, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeInt(this.init_List.size());

         for (LanSyncPacket.Inner_StreamUtil.Inner_DataUtil client : this.init_List) {
            out.writeUTF(client.init_Str);
            out.writeBoolean(client.init_Z);
         }

         out.writeInt(this.init_I);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         int size = init(in, 1024);
         this.init_List.clear();

         for (int i = 0; i < size; i++) {
            this.init_List.add(new LanSyncPacket.Inner_StreamUtil.Inner_DataUtil(in.readUTF(), in.readBoolean()));
         }

         try {
            this.init_I = in.readInt();
         } catch (EOFException e) {
            this.init_I = 0;
         }
      }

      public static class Inner_DataUtil {
         public String init_Str;
         public boolean init_Z;

         public Inner_DataUtil(String name, boolean isHost) {
            this.init_Str = name;
            this.init_Z = isHost;
         }
      }
   }

   public static class IoWorker extends LanSyncPacket {
      public String load;
      public String read;
      public String write;

      public IoWorker(String sessionId, String senderUsername, String presetName, String presetJson) {
         super(LanSyncPacketType.PRESET_DATA, sessionId);
         this.load = senderUsername;
         this.read = presetName;
         this.write = presetJson;
      }

      public IoWorker() {
         super(LanSyncPacketType.PRESET_DATA, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
         out.writeUTF(this.write);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
         this.write = in.readUTF();
      }
   }

   public static class JsonUtil extends LanSyncPacket {
      public String load;

      public JsonUtil(String sessionId, String username) {
         super(LanSyncPacketType.JOIN, sessionId);
         this.load = username;
      }

      public JsonUtil() {
         super(LanSyncPacketType.JOIN, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
      }
   }

   public static class MethodUtil extends LanSyncPacket {
      public String load_Str;
      public long init_J;
      public int init_I = 0;
      public long load_J = -1L;
      public long read = -1L;
      public boolean init_Z = false;

      public MethodUtil(String sessionId, String senderUsername, long executeNanoTime) {
         super(LanSyncPacketType.TCP_COMMAND_ACK, sessionId);
         this.load_Str = senderUsername != null ? senderUsername : "";
         this.init_J = executeNanoTime;
      }

      public MethodUtil(String sessionId, String senderUsername, long executeNanoTime, int executionMethod, long targetTick, long actualTick, boolean late) {
         this(sessionId, senderUsername, executeNanoTime);
         this.init_I = executionMethod;
         this.load_J = targetTick;
         this.read = actualTick;
         this.init_Z = late;
      }

      public MethodUtil() {
         super(LanSyncPacketType.TCP_COMMAND_ACK, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load_Str);
         out.writeLong(this.init_J);
         out.writeInt(this.init_I);
         out.writeLong(this.load_J);
         out.writeLong(this.read);
         out.writeBoolean(this.init_Z);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load_Str = in.readUTF();
         this.init_J = in.readLong();

         try {
            this.init_I = in.readInt();
            this.load_J = in.readLong();
            this.read = in.readLong();
            this.init_Z = in.readBoolean();
         } catch (EOFException e) {
            this.init_I = 0;
            this.load_J = -1L;
            this.read = -1L;
            this.init_Z = false;
         }
      }
   }

   public static class NetHelper extends LanSyncPacket {
      public String load;
      public long init_J;

      public NetHelper(String sessionId, String senderUsername, long timestamp) {
         super(LanSyncPacketType.HEARTBEAT, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.init_J = timestamp;
      }

      public NetHelper() {
         super(LanSyncPacketType.HEARTBEAT, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeLong(this.init_J);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.init_J = in.readLong();
      }
   }

   public static class PacketUtil extends LanSyncPacket {
      public Map<String, Integer> init_Map = new HashMap<>();

      public PacketUtil(String sessionId, Map<String, Integer> offsets) {
         super(LanSyncPacketType.PLAYER_OFFSETS, sessionId);
         this.init_Map = offsets != null ? new HashMap<>(offsets) : new HashMap<>();
      }

      public PacketUtil() {
         super(LanSyncPacketType.PLAYER_OFFSETS, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeInt(this.init_Map.size());

         for (Entry<String, Integer> entry : this.init_Map.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeInt(entry.getValue());
         }
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         int count = init(in, 1024);
         this.init_Map = new HashMap<>();

         for (int i = 0; i < count; i++) {
            String player = in.readUTF();
            int offset = in.readInt();
            this.init_Map.put(player, offset);
         }
      }
   }

   public static class PoolEntry extends LanSyncPacket {
      public String load;
      public String read;

      public PoolEntry(String sessionId, String queueData, String senderUsername) {
         super(LanSyncPacketType.QUEUE_SYNC, sessionId);
         this.load = queueData;
         this.read = senderUsername;
      }

      public PoolEntry() {
         super(LanSyncPacketType.QUEUE_SYNC, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
      }
   }

   public static class RenderState extends LanSyncPacket {
      public String load;
      public String read;
      public String write;
      public byte init_B;

      public RenderState(String sessionId, String senderUsername, String macroName, String nbtData, byte sourceType) {
         super(LanSyncPacketType.MACRO_DATA, sessionId);
         this.load = senderUsername;
         this.read = macroName;
         this.write = nbtData != null ? nbtData : "";
         this.init_B = sourceType;
      }

      public RenderState() {
         super(LanSyncPacketType.MACRO_DATA, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
         out.writeUTF(this.write);
         out.writeByte(this.init_B);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
         this.write = in.readUTF();

         try {
            this.init_B = in.readByte();
         } catch (EOFException e) {
            this.init_B = 0;
         }
      }
   }

   public static class RingBuffer extends LanSyncPacket {
      public String load;

      public RingBuffer(String sessionId, String macroName) {
         super(LanSyncPacketType.REQUEST_MACRO, sessionId);
         this.load = macroName;
      }

      public RingBuffer() {
         super(LanSyncPacketType.REQUEST_MACRO, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
      }
   }

   public static class StateCache extends LanSyncPacket {
      public String load;

      public StateCache(String sessionId, String username) {
         super(LanSyncPacketType.LEAVE, sessionId);
         this.load = username;
      }

      public StateCache() {
         super(LanSyncPacketType.LEAVE, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
      }
   }

   public static class SyncManager extends LanSyncPacket {
      public String load = "";
      public String read = "";

      public SyncManager(String sessionId, String senderUsername, String assignments) {
         super(LanSyncPacketType.MACRO_ASSIGNMENT_SYNC, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.read = assignments != null ? assignments : "";
      }

      public SyncManager() {
         super(LanSyncPacketType.MACRO_ASSIGNMENT_SYNC, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load);
         out.writeUTF(this.read);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load = in.readUTF();
         this.read = in.readUTF();
      }
   }

   public static class TableIndex extends LanSyncPacket {
      public TableIndex(String sessionId) {
         super(LanSyncPacketType.REQUEST_CLIENT_LIST, sessionId);
      }

      public TableIndex() {
         super(LanSyncPacketType.REQUEST_CLIENT_LIST, "");
      }

      @Override
      public void init(DataOutputStream out) {
      }

      @Override
      public void init(DataInputStream in) {
      }
   }

   public static class TaskQueue extends LanSyncPacket {
      public long init_J;
      public int init_I = 0;
      public long load = -1L;

      public TaskQueue(String sessionId, long executionId) {
         super(LanSyncPacketType.GO, sessionId);
         this.init_J = executionId;
      }

      public TaskQueue(String sessionId, long executionId, int executionMethod, long targetTick) {
         this(sessionId, executionId);
         this.init_I = executionMethod;
         this.load = targetTick;
      }

      public TaskQueue() {
         super(LanSyncPacketType.GO, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeLong(this.init_J);
         out.writeInt(this.init_I);
         out.writeLong(this.load);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.init_J = in.readLong();

         try {
            this.init_I = in.readInt();
            this.load = in.readLong();
         } catch (EOFException e) {
            this.init_I = 0;
            this.load = -1L;
         }
      }
   }

   public static class TextFormat extends LanSyncPacket {
      public String load_Str;
      public int init_I;
      public int load_I;
      public String read;

      public TextFormat(String sessionId, String senderUsername, int completedStep, int totalSteps, String macroName) {
         super(LanSyncPacketType.MACRO_STEP_PROGRESS, sessionId);
         this.load_Str = senderUsername != null ? senderUsername : "";
         this.init_I = completedStep;
         this.load_I = totalSteps;
         this.read = macroName != null ? macroName : "";
      }

      public TextFormat() {
         super(LanSyncPacketType.MACRO_STEP_PROGRESS, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load_Str);
         out.writeInt(this.init_I);
         out.writeInt(this.load_I);
         out.writeUTF(this.read);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load_Str = in.readUTF();
         this.init_I = in.readInt();
         this.load_I = in.readInt();
         this.read = in.readUTF();
      }
   }

   public static class TokenStore extends LanSyncPacket {
      public long init_J;
      public String load;
      public byte init_B;

      public TokenStore(String sessionId, long startTime, String hostName, byte transportType) {
         super(LanSyncPacketType.SESSION, sessionId);
         this.init_J = startTime;
         this.load = hostName;
         this.init_B = transportType;
      }

      public TokenStore(String sessionId, long startTime, String hostName) {
         this(sessionId, startTime, hostName, (byte)1);
      }

      public TokenStore() {
         super(LanSyncPacketType.SESSION, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeLong(this.init_J);
         out.writeUTF(this.load);
         out.writeByte(this.init_B);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.init_J = in.readLong();
         this.load = in.readUTF();

         try {
            this.init_B = in.readByte();
         } catch (EOFException e) {
            this.init_B = 1;
         }
      }
   }

   public static class UiHelper extends LanSyncPacket {
      public String load_Str;
      public static final int init_I = 64000;
      public String read_Str;
      public byte init_B;
      public int load_I;
      public int read_I;
      public byte[] init_BArr;

      public UiHelper(String sessionId, String senderUsername, String macroName, byte sourceType, int chunkIndex, int totalChunks, byte[] chunkData) {
         super(LanSyncPacketType.MACRO_DATA_CHUNK, sessionId);
         this.load_Str = senderUsername != null ? senderUsername : "";
         this.read_Str = macroName != null ? macroName : "";
         this.init_B = sourceType;
         this.load_I = chunkIndex;
         this.read_I = totalChunks;
         this.init_BArr = chunkData != null ? chunkData : new byte[0];
      }

      public UiHelper() {
         super(LanSyncPacketType.MACRO_DATA_CHUNK, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.load_Str);
         out.writeUTF(this.read_Str);
         out.writeByte(this.init_B);
         out.writeInt(this.load_I);
         out.writeInt(this.read_I);
         out.writeInt(this.init_BArr.length);
         out.write(this.init_BArr);
      }

      @Override
      public void init(DataInputStream in) throws IOException {
         this.load_Str = in.readUTF();
         this.read_Str = in.readUTF();
         this.init_B = in.readByte();
         this.load_I = in.readInt();
         this.read_I = in.readInt();
         int len = in.readInt();
         if (len >= 0 && len <= 64000) {
            this.init_BArr = new byte[len];
            in.readFully(this.init_BArr);
         } else {
            throw new IOException("Macro chunk length out of range: " + len);
         }
      }
   }
}
