package autismclient.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class bW {
   public String init_Str = UUID.randomUUID().toString();
   public String load_Str = "";
   public ProxyProtocolType init_bZ = ProxyProtocolType.Socks5;
   public String read = "";
   public int init_I = 0;
   public boolean init_Z = false;
   public String write = "";
   public String update = "";
   public volatile bW.Inner_CachePool init_bW_CachePool = bW.Inner_CachePool.UNCHECKED;
   public volatile long init_J = 0L;
   public volatile bW.Inner_StreamUtil init_bW_StreamUtil = bW.Inner_StreamUtil.init;
   public volatile String sync = "";
   public volatile String flush = "";
   public volatile String close = "";
   public volatile String open = "";
   public volatile String poll = "";
   public volatile String push = "";
   public volatile long load_J = 0L;
   transient boolean load_Z;

   public bW() {
   }

   public bW(Tag tag) {
      if (tag instanceof CompoundTag compoundTag) {
         this.init_bW_CompoundTag(compoundTag);
      }
   }

   public boolean init_Z() {
      return this.read != null && !this.read.isBlank() && this.init_I > 0 && this.init_I <= 65535;
   }

   public String init_Str() {
      String label = this.load_Str != null && !this.load_Str.isBlank() ? this.load_Str : this.read;
      return label == null ? "" : label;
   }

   public String load() {
      bW.Inner_StreamUtil state = this.init_bW_StreamUtil == null ? bW.Inner_StreamUtil.init : this.init_bW_StreamUtil;
      if (state == bW.Inner_StreamUtil.load) {
         return "Geo...";
      } else if (state != bW.Inner_StreamUtil.read) {
         return "Unknown";
      } else {
         String code = init_Str_Str(this.flush).toUpperCase(Locale.ROOT);
         String region = init_Str_Str(this.open);
         if (!code.isBlank() && !region.isBlank()) {
            return code + " " + region;
         } else if (!code.isBlank()) {
            return code;
         } else {
            return !region.isBlank() ? region : "Unknown";
         }
      }
   }

   public int init_I() {
      bW.Inner_StreamUtil state = this.init_bW_StreamUtil == null ? bW.Inner_StreamUtil.init : this.init_bW_StreamUtil;
      int var10000;
      switch (state) {
         case init:
         case write:
            var10000 = -6645094;
            break;
         case load:
            var10000 = -7429889;
            break;
         case read:
            var10000 = -855310;
            break;
         case update:
            var10000 = -6645094;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public String read() {
      return String.join(
         " ",
         this.load(),
         init_Str_Str(this.sync),
         init_Str_Str(this.flush),
         init_Str_Str(this.close),
         init_Str_Str(this.open),
         init_Str_Str(this.poll),
         init_Str_Str(this.push)
      );
   }

   public boolean init_Z_J_Z(long now, boolean force) {
      if (!this.init_Z()) {
         return false;
      }

      if (force) {
         return true;
      }

      bW.Inner_StreamUtil state = this.init_bW_StreamUtil == null ? bW.Inner_StreamUtil.init : this.init_bW_StreamUtil;
      long age = this.load_J <= 0L ? 9223372036854775807L : Math.max(0L, now - this.load_J);
      boolean var10000;
      switch (state) {
         case init:
            var10000 = true;
            break;
         case load:
            var10000 = age > 30000L;
            break;
         case read:
         case update:
            var10000 = age > 604800000L;
            break;
         case write:
            var10000 = age > 21600000L;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public void init_V_J(long now) {
      this.init_bW_StreamUtil = bW.Inner_StreamUtil.load;
      this.load_J = Math.max(0L, now);
   }

   public void init_V() {
      this.init_bW_StreamUtil = bW.Inner_StreamUtil.init;
      this.sync = "";
      this.flush = "";
      this.close = "";
      this.open = "";
      this.poll = "";
      this.push = "";
      this.load_J = 0L;
   }

   public void init_V_bX_DataUtil(bX.Inner_DataUtil result) {
      if (result == null) {
         this.init_bW_StreamUtil = bW.Inner_StreamUtil.write;
         this.load_J = System.currentTimeMillis();
      } else {
         this.init_bW_StreamUtil = result.init_bW_StreamUtil();
         this.sync = init_Str_Str(result.init_Str());
         this.flush = init_Str_Str(result.load()).toUpperCase(Locale.ROOT);
         this.close = init_Str_Str(result.read());
         this.open = init_Str_Str(result.write());
         this.poll = init_Str_Str(result.update());
         this.push = init_Str_Str(result.sync());
         this.load_J = result.init_J() <= 0L ? System.currentTimeMillis() : result.init_J();
      }
   }

   public synchronized int init_I_I(int timeoutMs) {
      this.init_bW_CachePool = bW.Inner_CachePool.CHECKING;
      bW.Inner_DataUtil result = this.init_bW_DataUtil_I(timeoutMs);
      this.init_V_bW_DataUtil(result);
      return result.init_I();
   }

   public synchronized bW.Inner_DataUtil init_bW_DataUtil_I(int timeoutMs) {
      ProxyConnectivityTester.Inner_DataUtil verified = ProxyConnectivityTester.init(this, "example.com", 80, timeoutMs);
      if (!verified.success()) {
         return new bW.Inner_DataUtil(bW.Inner_CachePool.DEAD, 0L, 2);
      }

      if (verified.protocol() != null) {
         this.init_bZ = verified.protocol();
      }

      return new bW.Inner_DataUtil(bW.Inner_CachePool.ALIVE, verified.latencyMs(), 1);
   }

   public synchronized void init_V_bW_DataUtil(bW.Inner_DataUtil result) {
      if (result == null) {
         this.init_bW_CachePool = bW.Inner_CachePool.UNCHECKED;
         this.init_J = 0L;
      } else {
         this.init_bW_CachePool = result.init_bW_CachePool();
         this.init_J = result.init_bW_CachePool() == bW.Inner_CachePool.ALIVE ? result.init_J() : 0L;
      }
   }

   private bW.Inner_BufferHelper init_bW_BufferHelper_bZ_I(ProxyProtocolType checkType, int timeoutMs) {
      try {
         Instant before = Instant.now();
         boolean alive = checkType == ProxyProtocolType.Socks4 ? this.init_Z_I(timeoutMs) : this.load_Z_I(timeoutMs);
         if (alive) {
            return new bW.Inner_BufferHelper(true, false, Duration.between(before, Instant.now()).toMillis());
         }
      } catch (SocketTimeoutException e) {
         return new bW.Inner_BufferHelper(false, true, 0L);
      } catch (IOException var6) {
      }

      return new bW.Inner_BufferHelper(false, false, 0L);
   }

   private boolean init_Z_I(int timeoutMs) throws UnknownHostException, IOException {
      byte[] user = init_Str_Str(this.write).getBytes();
      ByteBuffer bb;
      if (init_Z_Str(this.read)) {
         bb = ByteBuffer.allocate(9 + user.length)
            .put((byte)4)
            .put((byte)1)
            .putShort((short)this.init_I)
            .put(InetAddress.getByName(this.read).getAddress())
            .put(user)
            .put((byte)0);
      } else {
         byte[] addr = init_Str_Str(this.read).getBytes();
         bb = ByteBuffer.allocate(10 + user.length + addr.length)
            .put((byte)4)
            .put((byte)1)
            .putShort((short)this.init_I)
            .put(new byte[]{0, 0, 0, 1})
            .put(user)
            .put((byte)0)
            .put(addr)
            .put((byte)0);
      }

      byte[] data = this.init(bb.array(), 8, timeoutMs);
      return data.length >= 2 && data[0] == 0 && data[1] == 90;
   }

   private boolean load_Z_I(int timeoutMs) throws IOException {
      ByteBuffer bb = ByteBuffer.allocate(4).put((byte)5).put((byte)2).put((byte)0).put((byte)2);
      byte[] data = this.init(bb.array(), 2, timeoutMs);
      return data.length >= 2 && data[0] == 5 && (data[1] == 0 || data[1] == 2);
   }

   private byte[] init(byte[] data, int read, int timeoutMs) throws IOException, SocketException {
      try (Socket socket = new Socket()) {
         int timeout = Math.max(1, timeoutMs);
         socket.setSoTimeout(timeout);
         socket.connect(new InetSocketAddress(this.read, this.init_I), timeout);
         OutputStream output = socket.getOutputStream();
         output.write(data);
         return socket.getInputStream().readNBytes(read);
      }
   }

   private static boolean init_Z_Str(String value) {
      if (value == null) {
         return false;
      }

      String[] parts = value.split("\\.");
      if (parts.length != 4) {
         return false;
      }

      for (String part : parts) {
         try {
            int i = Integer.parseInt(part);
            if (i < 0 || i > 255) {
               return false;
            }
         } catch (NumberFormatException e) {
            return false;
         }
      }

      return true;
   }

   private static String init_Str_Str(String value) {
      return value == null ? "" : value;
   }

   private static String load_Str_Str(String value) {
      if (value != null && !value.isEmpty()) {
         byte[] sealed = autismclient.util.mm.a.DataUtil.init(value.getBytes(StandardCharsets.UTF_8));
         return sealed == null ? "" : Base64.getEncoder().encodeToString(sealed);
      } else {
         return "";
      }
   }

   private static String init_Str_Str_Str(String encoded, String plainFallback) {
      String fallback = plainFallback == null ? "" : plainFallback;
      if (encoded != null && !encoded.isEmpty()) {
         try {
            byte[] plain = autismclient.util.mm.a.DataUtil.read(Base64.getDecoder().decode(encoded));
            return plain == null ? fallback : new String(plain, StandardCharsets.UTF_8);
         } catch (Throwable t) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   public CompoundTag init_CompoundTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("id", this.write());
      tag.putString("name", init_Str_Str(this.load_Str));
      tag.putString("type", this.init_bZ == null ? ProxyProtocolType.Socks5.name() : this.init_bZ.name());
      tag.putString("address", init_Str_Str(this.read));
      tag.putInt("port", this.init_I);
      tag.putBoolean("enabled", this.init_Z);
      tag.putString("encUsername", load_Str_Str(this.write));
      tag.putString("encPassword", load_Str_Str(this.update));
      tag.putString("geoStatus", this.init_bW_StreamUtil == null ? bW.Inner_StreamUtil.init.name() : this.init_bW_StreamUtil.name());
      tag.putString("geoResolvedIp", init_Str_Str(this.sync));
      tag.putString("geoCountryCode", init_Str_Str(this.flush));
      tag.putString("geoCountryName", init_Str_Str(this.close));
      tag.putString("geoRegion", init_Str_Str(this.open));
      tag.putString("geoCity", init_Str_Str(this.poll));
      tag.putString("geoSubdivision", init_Str_Str(this.push));
      tag.putLong("geoCheckedAt", this.load_J);
      return tag;
   }

   public bW init_bW_CompoundTag(CompoundTag tag) {
      String storedId = tag.getStringOr("id", "");
      if (storedId.isBlank()) {
         this.init_Str = UUID.randomUUID().toString();
         this.load_Z = true;
      } else {
         this.init_Str = storedId;
      }

      this.load_Str = tag.getStringOr("name", "");
      String typeName = tag.getStringOr("type", ProxyProtocolType.Socks5.name());

      try {
         this.init_bZ = ProxyProtocolType.valueOf(typeName);
      } catch (IllegalArgumentException e) {
         this.init_bZ = typeName.toLowerCase(Locale.ROOT).contains("4") ? ProxyProtocolType.Socks4 : ProxyProtocolType.Socks5;
      }

      this.read = tag.getStringOr("address", "");
      this.init_I = tag.getIntOr("port", 0);
      this.init_Z = tag.getBooleanOr("enabled", false);
      this.write = init_Str_Str_Str(tag.getStringOr("encUsername", ""), tag.getStringOr("username", ""));
      this.update = init_Str_Str_Str(tag.getStringOr("encPassword", ""), tag.getStringOr("password", ""));

      try {
         this.init_bW_StreamUtil = bW.Inner_StreamUtil.valueOf(tag.getStringOr("geoStatus", bW.Inner_StreamUtil.init.name()));
      } catch (IllegalArgumentException e) {
         this.init_bW_StreamUtil = bW.Inner_StreamUtil.init;
      }

      this.sync = tag.getStringOr("geoResolvedIp", "");
      this.flush = tag.getStringOr("geoCountryCode", "");
      this.close = tag.getStringOr("geoCountryName", "");
      this.open = tag.getStringOr("geoRegion", "");
      this.poll = tag.getStringOr("geoCity", "");
      this.push = tag.getStringOr("geoSubdivision", "");
      this.load_J = tag.getLongOr("geoCheckedAt", 0L);
      this.init_bW_CachePool = bW.Inner_CachePool.UNCHECKED;
      this.init_J = 0L;
      return this;
   }

   public String write() {
      if (this.init_Str == null || this.init_Str.isBlank()) {
         this.init_Str = UUID.randomUUID().toString();
         this.load_Z = true;
      }

      return this.init_Str;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof bW proxy ? this.init_I == proxy.init_I && Objects.equals(this.read, proxy.read) && this.init_bZ == proxy.init_bZ : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.init_bZ, this.read, this.init_I);
   }

   private record Inner_BufferHelper(boolean init_Z, boolean load, long init_J) {
   }

   public enum Inner_CachePool {
      UNCHECKED,
      CHECKING,
      DEAD,
      ALIVE;

      public String init_Str() {
         String var10000;
         switch (this) {
            case UNCHECKED:
               var10000 = "";
               break;
            case CHECKING:
               var10000 = "...";
               break;
            case DEAD:
               var10000 = "X";
               break;
            case ALIVE:
               var10000 = "O";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      public int init_I() {
         int var10000;
         switch (this) {
            case UNCHECKED:
            case CHECKING:
               var10000 = -5723992;
               break;
            case DEAD:
               var10000 = -42406;
               break;
            case ALIVE:
               var10000 = -10813551;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   public record Inner_DataUtil(bW.Inner_CachePool init_bW_CachePool, long init_J, int init_I) {
      public Inner_DataUtil {
         init_bW_CachePool = init_bW_CachePool == null ? bW.Inner_CachePool.DEAD : init_bW_CachePool;
         init_J = Math.max(0L, init_J);
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
