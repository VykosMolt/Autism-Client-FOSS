package autismclient.util.mm.b;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class DataUtil {
   private DataUtil() {
   }

   public static final class BufferHelper extends DataUtil.JsonUtil {
      public byte[] init = new byte[8];

      public BufferHelper() {
      }

      public BufferHelper(byte[] bannedFp) {
         if (bannedFp != null && bannedFp.length == 8) {
            this.init = (byte[])bannedFp.clone();
         }
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.open;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.write(this.init, 0, 8);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         byte[] b = new byte[8];
         in.readFully(b);
         this.init = b;
      }
   }

   public static final class CachePool extends DataUtil.JsonUtil {
      public static final byte init_B = 0;
      public static final byte load = 1;
      public static final byte read = 2;
      public byte write;
      public String init_Str = "";

      public CachePool() {
      }

      public CachePool(byte kind, String body) {
         this.write = kind;
         this.init_Str = body != null ? body : "";
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.write;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeByte(this.write);
         out.writeUTF(this.init_Str);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.write = in.readByte();
         this.init_Str = in.readUTF();
      }
   }

   public static final class ConfigStore extends DataUtil.JsonUtil {
      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.flush;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
      }
   }

   public static final class Inner_DataUtil extends DataUtil.JsonUtil {
      public String init_Str = "";
      public String load = "";
      public int init_I;
      public String read = "";

      public Inner_DataUtil() {
      }

      public Inner_DataUtil(String kind, String friendlyName, int count, String data) {
         this.init_Str = kind == null ? "" : kind;
         this.load = friendlyName == null ? "" : friendlyName;
         this.init_I = count;
         this.read = data == null ? "" : data;
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.close;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.init_Str);
         out.writeUTF(this.load);
         out.writeInt(this.init_I);
         byte[] d = this.read.getBytes(StandardCharsets.UTF_8);
         out.writeInt(d.length);
         out.write(d);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.init_Str = in.readUTF();
         this.load = in.readUTF();
         this.init_I = in.readInt();
         int len = in.readInt();
         if (len >= 0 && len <= 4000000) {
            byte[] d = new byte[len];
            in.readFully(d);
            this.read = new String(d, StandardCharsets.UTF_8);
         } else {
            throw new IOException("blob too large");
         }
      }
   }

   public abstract static class JsonUtil {
      public abstract autismclient.util.mm.UiHelper init_UiHelper();

      protected abstract void init(DataOutputStream var1) throws IOException;

      protected abstract void init(DataInputStream var1) throws IOException;

      public final byte[] init_BArr() {
         try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            this.init(new DataOutputStream(bos));
            return bos.toByteArray();
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }

      public static <T extends DataUtil.JsonUtil> T init(T target, byte[] payload) {
         try {
            target.init(new DataInputStream(new ByteArrayInputStream(payload)));
            return target;
         } catch (Throwable t) {
            return null;
         }
      }
   }

   public static final class NetHelper extends DataUtil.JsonUtil {
      public String init_Str = "";
      public int init_I;
      public String load = "";
      public String read = "";

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.read;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.init_Str);
         out.writeInt(this.init_I);
         out.writeUTF(this.load);
         out.writeUTF(this.read);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.init_Str = in.readUTF();
         this.init_I = in.readInt();
         this.load = in.readUTF();
         this.read = in.readUTF();
      }
   }

   public static final class StateCache extends DataUtil.JsonUtil {
      public String init = "";
      public String load = "";
      public String read = "";

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.update;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.init);
         out.writeUTF(this.load);
         out.writeUTF(this.read);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.init = in.readUTF();
         this.load = in.readUTF();
         this.read = in.readUTF();
      }
   }

   public static final class StreamUtil extends DataUtil.JsonUtil {
      public String init = "";

      public StreamUtil() {
      }

      public StreamUtil(String text) {
         this.init = text != null ? text : "";
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.load;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.init);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.init = in.readUTF();
      }
   }

   public static final class SyncManager extends DataUtil.JsonUtil {
      public String init_Str = "";
      public boolean init_Z;
      public String load_Str = "";
      public String read = "";
      public boolean load_Z;
      public int init_I = -1;
      public String write = "";

      public SyncManager() {
      }

      public SyncManager(String nickname) {
         this.init_Str = nickname != null ? nickname : "";
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.init_UiHelper;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.init_Str);
         out.writeBoolean(this.init_Z);
         out.writeUTF(this.load_Str);
         out.writeUTF(this.read);
         out.writeBoolean(this.load_Z);
         out.writeInt(this.init_I);
         out.writeUTF(this.write);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.init_Str = in.readUTF();
         this.init_Z = in.readBoolean();
         this.load_Str = in.readUTF();
         this.read = in.readUTF();
         this.load_Z = in.readBoolean();
         if (in.available() > 0) {
            this.init_I = in.readInt();
         }

         if (in.available() > 0) {
            this.write = in.readUTF();
         }
      }
   }

   public static final class TaskQueue extends DataUtil.JsonUtil {
      public String init_Str = "";
      public double init_D;
      public double load;
      public double read;

      public TaskQueue() {
      }

      public TaskQueue(String dimension, double x, double y, double z) {
         this.init_Str = dimension != null ? dimension : "";
         this.init_D = x;
         this.load = y;
         this.read = z;
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.sync;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.writeUTF(this.init_Str);
         out.writeDouble(this.init_D);
         out.writeDouble(this.load);
         out.writeDouble(this.read);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         this.init_Str = in.readUTF();
         this.init_D = in.readDouble();
         this.load = in.readDouble();
         this.read = in.readDouble();
      }
   }

   public static final class UiHelper extends DataUtil.JsonUtil {
      public byte[] init = new byte[16];

      public UiHelper() {
      }

      public UiHelper(byte[] msgId) {
         if (msgId != null && msgId.length == 16) {
            this.init = (byte[])msgId.clone();
         }
      }

      @Override
      public autismclient.util.mm.UiHelper init_UiHelper() {
         return autismclient.util.mm.UiHelper.poll;
      }

      @Override
      protected void init(DataOutputStream out) throws IOException {
         out.write(this.init, 0, 16);
      }

      @Override
      protected void init(DataInputStream in) throws IOException {
         byte[] b = new byte[16];
         in.readFully(b);
         this.init = b;
      }
   }
}
