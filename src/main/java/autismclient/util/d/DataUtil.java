package autismclient.util.d;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class DataUtil {
   protected final StreamUtil init_StreamUtil;
   protected String init_Str;

   public DataUtil(StreamUtil type, String sessionId) {
      this.init_StreamUtil = type;
      this.init_Str = sessionId != null ? sessionId : "";
   }

   public StreamUtil init_StreamUtil() {
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

   public static DataUtil init(StreamUtil type, String sessionId) {
      switch (type) {
         case init_StreamUtil:
            return new DataUtil.CharEncoder(sessionId);
         case load:
            return new DataUtil.TokenStore(sessionId, 0L, "");
         case read:
            return new DataUtil.JsonUtil(sessionId, "");
         case write:
            return new DataUtil.TableIndex(sessionId);
         case update:
            return new DataUtil.Inner_StreamUtil(sessionId, new ArrayList<>());
         case sync:
            return new DataUtil.StateCache(sessionId, "");
         case flush:
            return new DataUtil.RingBuffer(sessionId, "");
         case close:
            return new DataUtil.RenderState(sessionId, "", "", "", (byte)0);
         case open:
            return new DataUtil.EventBus(sessionId, "", "");
         case poll:
            return new DataUtil.FileCache(sessionId, "", new ArrayList<>());
         case push:
            return new DataUtil.PoolEntry(sessionId, "", "");
         case pop:
            return new DataUtil.IoWorker(sessionId, "", "", "");
         case tick:
            return new DataUtil.Inner_DataUtil(sessionId, "", "");
         case run:
            return new DataUtil.MethodUtil(sessionId, "", 0L);
         case get:
            return new DataUtil.PacketUtil(sessionId, new HashMap<>());
         case set:
            return new DataUtil.CachePool(sessionId, "", "", 0);
         case put:
            return new DataUtil.HashUtil(sessionId, 0L, false, "", "", "");
         case add:
            return new DataUtil.BufferHelper(sessionId, 0L, "");
         case clear:
            return new DataUtil.ConfigStore(sessionId, 0L, 0L);
         case reset:
            return new DataUtil.TaskQueue(sessionId, 0L);
         case start:
            return new DataUtil.NetHelper(sessionId, "", 0L);
         case stop:
            return new DataUtil.FieldCache(sessionId, 0, "");
         case next:
            return new DataUtil.BitSetUtil(sessionId, "", false, "", "");
         case prev:
            return new DataUtil.ByteReader(sessionId, "", "", 0);
         case valid:
            return new DataUtil.TextFormat(sessionId, "", 0, 0, "");
         case size:
            return new DataUtil.UiHelper(sessionId, "", "", (byte)0, 0, 0, new byte[0]);
         case data:
            return new DataUtil.SyncManager(sessionId, "", "");
         default:
            return null;
      }
   }

   public static class BitSetUtil extends DataUtil {
      public String load;
      public boolean init_Z;
      public String read;
      public String write;
      public int init_I = 0;

      public BitSetUtil(String sessionId, String senderUsername, boolean isMacro, String macroName, String command) {
         super(StreamUtil.next, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.init_Z = isMacro;
         this.read = macroName != null ? macroName : "";
         this.write = command != null ? command : "";
      }

      public BitSetUtil() {
         super(StreamUtil.next, "");
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

   public static class BufferHelper extends DataUtil {
      public long init_J;
      public String load;

      public BufferHelper(String sessionId, long executionId, String senderUsername) {
         super(StreamUtil.add, sessionId);
         this.init_J = executionId;
         this.load = senderUsername;
      }

      public BufferHelper() {
         super(StreamUtil.add, "");
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

   public static class ByteReader extends DataUtil {
      public String load;
      public String read;
      public int init_I;

      public ByteReader(String sessionId, String senderUsername, String targetUsername, int offsetMs) {
         super(StreamUtil.prev, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.read = targetUsername != null ? targetUsername : "";
         this.init_I = offsetMs;
      }

      public ByteReader() {
         super(StreamUtil.prev, "");
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

   public static class CachePool extends DataUtil {
      public String load;
      public String read;
      public int init_I;

      public CachePool(String sessionId, String senderUsername, String targetUsername, int offsetMs) {
         super(StreamUtil.set, sessionId);
         this.load = senderUsername;
         this.read = targetUsername;
         this.init_I = offsetMs;
      }

      public CachePool() {
         super(StreamUtil.set, "");
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

   public static class CharEncoder extends DataUtil {
      public CharEncoder(String sessionId) {
         super(StreamUtil.init_StreamUtil, sessionId);
      }

      public CharEncoder() {
         super(StreamUtil.init_StreamUtil, "");
      }

      @Override
      public void init(DataOutputStream out) {
      }

      @Override
      public void init(DataInputStream in) {
      }
   }

   public static class ConfigStore extends DataUtil {
      public long init_J;
      public long load;

      public ConfigStore(String sessionId, long executionId, long targetTime) {
         super(StreamUtil.clear, sessionId);
         this.init_J = executionId;
         this.load = targetTime;
      }

      public ConfigStore() {
         super(StreamUtil.clear, "");
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

   public static class EventBus extends DataUtil {
      public String load;
      public String read;

      public EventBus(String sessionId, String senderUsername, String macroName) {
         super(StreamUtil.open, sessionId);
         this.load = senderUsername;
         this.read = macroName;
      }

      public EventBus() {
         super(StreamUtil.open, "");
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

   public static class FieldCache extends DataUtil {
      public int init_I;
      public String load;

      public FieldCache(String sessionId, int stateOrdinal, String detail) {
         super(StreamUtil.stop, sessionId);
         this.init_I = stateOrdinal;
         this.load = detail != null ? detail : "";
      }

      public FieldCache() {
         super(StreamUtil.stop, "");
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

   public static class FileCache extends DataUtil {
      public String load;
      public List<String> init_List = new ArrayList<>();

      public FileCache(String sessionId, String senderUsername, List<String> macroNames) {
         super(StreamUtil.poll, sessionId);
         this.load = senderUsername;
         this.init_List = macroNames;
      }

      public FileCache() {
         super(StreamUtil.poll, "");
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

   public static class HashUtil extends DataUtil {
      public long init_J;
      public boolean init_Z;
      public String load_Str;
      public String read;
      public String write;
      public String update = "";
      public int init_I = 0;
      public long load_J = -1L;

      public HashUtil(String sessionId, long executionId, boolean isMacro, String targetName, String queueData, String senderUsername) {
         super(StreamUtil.put, sessionId);
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
         super(StreamUtil.put, "");
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

   public static class Inner_DataUtil extends DataUtil {
      public String load;
      public String read;

      public Inner_DataUtil(String sessionId, String senderUsername, String message) {
         super(StreamUtil.tick, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.read = message != null ? message : "";
      }

      public Inner_DataUtil() {
         super(StreamUtil.tick, "");
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

   public static class Inner_StreamUtil extends DataUtil {
      public List<DataUtil.Inner_StreamUtil.Inner_DataUtil> init_List = new ArrayList<>();
      public int init_I = 0;

      public Inner_StreamUtil(String sessionId, List<DataUtil.Inner_StreamUtil.Inner_DataUtil> clients) {
         super(StreamUtil.update, sessionId);
         this.init_List = clients;
      }

      public Inner_StreamUtil(String sessionId, List<DataUtil.Inner_StreamUtil.Inner_DataUtil> clients, int executionMethod) {
         this(sessionId, clients);
         this.init_I = executionMethod;
      }

      public Inner_StreamUtil() {
         super(StreamUtil.update, "");
      }

      @Override
      public void init(DataOutputStream out) throws IOException {
         out.writeInt(this.init_List.size());

         for (DataUtil.Inner_StreamUtil.Inner_DataUtil client : this.init_List) {
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
            this.init_List.add(new DataUtil.Inner_StreamUtil.Inner_DataUtil(in.readUTF(), in.readBoolean()));
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

   public static class IoWorker extends DataUtil {
      public String load;
      public String read;
      public String write;

      public IoWorker(String sessionId, String senderUsername, String presetName, String presetJson) {
         super(StreamUtil.pop, sessionId);
         this.load = senderUsername;
         this.read = presetName;
         this.write = presetJson;
      }

      public IoWorker() {
         super(StreamUtil.pop, "");
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

   public static class JsonUtil extends DataUtil {
      public String load;

      public JsonUtil(String sessionId, String username) {
         super(StreamUtil.read, sessionId);
         this.load = username;
      }

      public JsonUtil() {
         super(StreamUtil.read, "");
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

   public static class MethodUtil extends DataUtil {
      public String load_Str;
      public long init_J;
      public int init_I = 0;
      public long load_J = -1L;
      public long read = -1L;
      public boolean init_Z = false;

      public MethodUtil(String sessionId, String senderUsername, long executeNanoTime) {
         super(StreamUtil.run, sessionId);
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
         super(StreamUtil.run, "");
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

   public static class NetHelper extends DataUtil {
      public String load;
      public long init_J;

      public NetHelper(String sessionId, String senderUsername, long timestamp) {
         super(StreamUtil.start, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.init_J = timestamp;
      }

      public NetHelper() {
         super(StreamUtil.start, "");
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

   public static class PacketUtil extends DataUtil {
      public Map<String, Integer> init_Map = new HashMap<>();

      public PacketUtil(String sessionId, Map<String, Integer> offsets) {
         super(StreamUtil.get, sessionId);
         this.init_Map = offsets != null ? new HashMap<>(offsets) : new HashMap<>();
      }

      public PacketUtil() {
         super(StreamUtil.get, "");
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

   public static class PoolEntry extends DataUtil {
      public String load;
      public String read;

      public PoolEntry(String sessionId, String queueData, String senderUsername) {
         super(StreamUtil.push, sessionId);
         this.load = queueData;
         this.read = senderUsername;
      }

      public PoolEntry() {
         super(StreamUtil.push, "");
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

   public static class RenderState extends DataUtil {
      public String load;
      public String read;
      public String write;
      public byte init_B;

      public RenderState(String sessionId, String senderUsername, String macroName, String nbtData, byte sourceType) {
         super(StreamUtil.close, sessionId);
         this.load = senderUsername;
         this.read = macroName;
         this.write = nbtData != null ? nbtData : "";
         this.init_B = sourceType;
      }

      public RenderState() {
         super(StreamUtil.close, "");
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

   public static class RingBuffer extends DataUtil {
      public String load;

      public RingBuffer(String sessionId, String macroName) {
         super(StreamUtil.flush, sessionId);
         this.load = macroName;
      }

      public RingBuffer() {
         super(StreamUtil.flush, "");
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

   public static class StateCache extends DataUtil {
      public String load;

      public StateCache(String sessionId, String username) {
         super(StreamUtil.sync, sessionId);
         this.load = username;
      }

      public StateCache() {
         super(StreamUtil.sync, "");
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

   public static class SyncManager extends DataUtil {
      public String load = "";
      public String read = "";

      public SyncManager(String sessionId, String senderUsername, String assignments) {
         super(StreamUtil.data, sessionId);
         this.load = senderUsername != null ? senderUsername : "";
         this.read = assignments != null ? assignments : "";
      }

      public SyncManager() {
         super(StreamUtil.data, "");
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

   public static class TableIndex extends DataUtil {
      public TableIndex(String sessionId) {
         super(StreamUtil.write, sessionId);
      }

      public TableIndex() {
         super(StreamUtil.write, "");
      }

      @Override
      public void init(DataOutputStream out) {
      }

      @Override
      public void init(DataInputStream in) {
      }
   }

   public static class TaskQueue extends DataUtil {
      public long init_J;
      public int init_I = 0;
      public long load = -1L;

      public TaskQueue(String sessionId, long executionId) {
         super(StreamUtil.reset, sessionId);
         this.init_J = executionId;
      }

      public TaskQueue(String sessionId, long executionId, int executionMethod, long targetTick) {
         this(sessionId, executionId);
         this.init_I = executionMethod;
         this.load = targetTick;
      }

      public TaskQueue() {
         super(StreamUtil.reset, "");
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

   public static class TextFormat extends DataUtil {
      public String load_Str;
      public int init_I;
      public int load_I;
      public String read;

      public TextFormat(String sessionId, String senderUsername, int completedStep, int totalSteps, String macroName) {
         super(StreamUtil.valid, sessionId);
         this.load_Str = senderUsername != null ? senderUsername : "";
         this.init_I = completedStep;
         this.load_I = totalSteps;
         this.read = macroName != null ? macroName : "";
      }

      public TextFormat() {
         super(StreamUtil.valid, "");
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

   public static class TokenStore extends DataUtil {
      public long init_J;
      public String load;
      public byte init_B;

      public TokenStore(String sessionId, long startTime, String hostName, byte transportType) {
         super(StreamUtil.load, sessionId);
         this.init_J = startTime;
         this.load = hostName;
         this.init_B = transportType;
      }

      public TokenStore(String sessionId, long startTime, String hostName) {
         this(sessionId, startTime, hostName, (byte)1);
      }

      public TokenStore() {
         super(StreamUtil.load, "");
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

   public static class UiHelper extends DataUtil {
      public String load_Str;
      public static final int init_I = 64000;
      public String read_Str;
      public byte init_B;
      public int load_I;
      public int read_I;
      public byte[] init_BArr;

      public UiHelper(String sessionId, String senderUsername, String macroName, byte sourceType, int chunkIndex, int totalChunks, byte[] chunkData) {
         super(StreamUtil.size, sessionId);
         this.load_Str = senderUsername != null ? senderUsername : "";
         this.read_Str = macroName != null ? macroName : "";
         this.init_B = sourceType;
         this.load_I = chunkIndex;
         this.read_I = totalChunks;
         this.init_BArr = chunkData != null ? chunkData : new byte[0];
      }

      public UiHelper() {
         super(StreamUtil.size, "");
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
