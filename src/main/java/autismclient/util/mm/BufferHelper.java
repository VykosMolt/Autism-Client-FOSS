package autismclient.util.mm;

import autismclient.AutismClientAddon;
import autismclient.modules.aD;
import autismclient.util.AutismHttp;
import autismclient.util.L;
import autismclient.util.bi;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.security.PublicKey;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;

public final class BufferHelper {
   private static final BufferHelper init_BufferHelper = new BufferHelper();
   private static final int init_I = 300;
   private static final long init_J = 250L;
   private static final long load_J = 8000L;
   private static final long read_J = 60000L;
   private static final long write_J = 10000L;
   private static final int load_I = 2000;
   private static final int read_I = 2000;
   private final autismclient.util.mm.a.CachePool init_CachePool = autismclient.util.mm.a.CachePool.init_CachePool();
   private final EventBus init_EventBus = EventBus.init_EventBus();
   private final TextFormat init_TextFormat = new TextFormat();
   private final String init_Str = this.init_CachePool
      .init_Str()
      .replace(autismclient.e.DataUtil.init(new int[]{-1798794841, -1798794870}), autismclient.e.DataUtil.init(new int[]{-1798794841}))
      .toLowerCase(Locale.ROOT);
   private final Map<String, RenderState> init_Map = new ConcurrentHashMap<>();
   private final Map<String, StreamUtil> load_Map = new ConcurrentHashMap<>();
   private static final String load_Str = String.valueOf(
      autismclient.e.DataUtil.init(
         new int[]{
            -1798794553,
            -1798794577,
            71943790,
            -2064712356,
            418424747,
            86266874,
            -1620697690,
            1297978821,
            1582638962,
            1817166890,
            497599851,
            -1754276136,
            1452013239,
            2062738723,
            -479609663,
            -1982998757,
            -1343416811,
            1303754150,
            1761674321,
            -1222672291,
            751863168,
            1832924925,
            986094541,
            504158359,
            -1550957301,
            -835033305,
            -116227320,
            691921367,
            -25275256,
            -783529864,
            1480374919,
            -1353016806,
            1006154758,
            1126029493,
            547177032,
            -217382063,
            1851089806,
            1549212138
         }
      )
   );
   private static final long update_J = 6000L;
   private static final long sync_J = 10000L;
   private ScheduledExecutorService init_ScheduledExecutorS;
   private volatile long flush_J;
   private final Deque<JsonUtil> init_Deque = new ArrayDeque<>();
   private volatile List<RenderState> init_List = List.of();
   private volatile long close_J;
   private volatile List<StreamUtil> load_List = List.of();
   private volatile long open_J;
   private volatile int write_I;
   private volatile autismclient.util.mm.c.CachePool init_CachePool_2;
   private volatile DataUtil init_DataUtil;
   private volatile autismclient.util.mm.a.BufferHelper init_BufferHelper_2;
   private volatile boolean init_Z;
   private volatile long poll_J;
   private volatile long push_J;
   private volatile long pop;
   private volatile String read_Str = autismclient.e.DataUtil.init(new int[]{-1798794839});
   private volatile long tick;
   private volatile boolean load_Z;
   private volatile boolean read_Z;
   private volatile BufferHelper.Inner_StreamUtil init_BufferHelper_Strea = BufferHelper.Inner_StreamUtil.init;
   private volatile String write_Str = autismclient.e.DataUtil.init(new int[]{-1798794839});
   private volatile boolean write_Z;
   private volatile boolean update_Z;
   private volatile long run;
   private volatile boolean sync_Z;
   private volatile boolean flush_Z;
   private volatile long get;
   private volatile boolean close_Z;
   private volatile long set;
   private volatile boolean open_Z;
   private volatile long put;
   private static final long add = 10000L;
   private volatile long clear;
   private volatile boolean poll_Z;
   private static final long reset = 12000L;
   private static final long start = 2000L;
   private static final long stop = 15000L;
   private ScheduledExecutorService load_ScheduledExecutorS;
   private ScheduledFuture<?> init_ScheduledFuture;
   private long next;
   private volatile byte[] init_BArr;
   private volatile int update_I;
   private volatile List<BufferHelper.Inner_CachePool> read_List = List.of();
   private static final int sync_I = 2;
   private volatile long prev;
   private volatile long valid;
   private volatile boolean push_Z;
   private volatile long size;
   private volatile long data;
   private static final long value = 60000L;
   private static final int flush_I = 64;
   private final Map<String, BufferHelper.Inner_ConfigStore> read_Map = new ConcurrentHashMap<>();
   private final Set<String> init_Set = ConcurrentHashMap.newKeySet();
   private final Set<String> load_Set = ConcurrentHashMap.newKeySet();
   private final Set<String> read_Set = ConcurrentHashMap.newKeySet();
   private final Map<String, String> write_Map = new ConcurrentHashMap<>();
   private final Set<String> write_Set = ConcurrentHashMap.newKeySet();
   private volatile int close_I;
   private volatile String update_Str = autismclient.e.DataUtil.init(new int[]{-1798794837});
   private volatile List<BufferHelper.Inner_BufferHelper> write_List = List.of();
   private volatile long cache;
   private static final String sync_Str = autismclient.e.DataUtil.init(
      new int[]{
         -1798794553,
         -1798794576,
         71943785,
         -2064712357,
         418424801,
         86266790,
         -1620697677,
         1297978759,
         1582638896,
         1817166949,
         497599871,
         -1754276135,
         1452013227,
         2062738788,
         -479609645,
         -1982998781,
         -1343416830,
         1303754147,
         1761674315,
         -1222672299,
         751863181,
         1832924901,
         986094474,
         504158353,
         -1550957302,
         -835033282,
         -116227319,
         691921369,
         -25275242,
         -783529887,
         1480375004
      }
   );
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean();
   private static final long state = 1000L;
   private volatile long queue;

   public static BufferHelper init_BufferHelper() {
      return init_BufferHelper;
   }

   private static AutismHttp.Inner_DataUtil init_AutismHttp_DataUti_Str_JsonObject(String endpoint, JsonObject body) {
      return init_AutismHttp_DataUti_Str(endpoint, body, 15000);
   }

   private static AutismHttp.Inner_DataUtil init_AutismHttp_DataUti_Str(String endpoint, JsonObject body, int timeoutMs) {
      String suffix = endpoint.startsWith(autismclient.e.DataUtil.init(new int[]{-1798794847, -1798794866})) ? endpoint : "/" + endpoint;
      String json = body == null ? autismclient.e.DataUtil.init(new int[]{-1798794847, -1798794790, 71934717}) : body.toString();
      return AutismHttp.init(
         load_Str + suffix,
         json,
         timeoutMs,
         L.init(autismclient.e.DataUtil.init(new int[]{-1798794845, -1798794765, 71934705, -2064992937, 409725739}), "/lobbies" + suffix, json)
      );
   }

   private static JsonObject init_JsonObject_Str(String jwt) {
      return AutismHttp.init_JsonObject(
         load_Str,
         jwt,
         L.init(
            autismclient.e.DataUtil.init(new int[]{-1798794845, -1798794780, 71934715, -2064992944}),
            autismclient.e.DataUtil.init(
               new int[]{-1798794843, -1798794870, 71934608, -2064990999, 409785439, -181554427, -1333217001, 1619949101, -1321184564}
            ),
            autismclient.e.DataUtil.init(new int[]{-1798794843})
         )
      );
   }

   private BufferHelper() {
   }

   public EventBus init_EventBus() {
      return this.init_EventBus;
   }

   public TextFormat init_TextFormat() {
      return this.init_TextFormat;
   }

   public DataUtil init_DataUtil() {
      return this.init_DataUtil;
   }

   public boolean init_Z() {
      return this.init_DataUtil != null;
   }

   public String init_Str() {
      DataUtil lb = this.init_DataUtil;
      return lb == null ? autismclient.e.DataUtil.init(new int[]{-1798794837}) : lb.update;
   }

   public String load_Str() {
      return this.init_CachePool.init_Str();
   }

   public String read_Str() {
      return this.init_Str;
   }

   public boolean load_Z() {
      return this.pop_Z();
   }

   public boolean read_Z() {
      DataUtil lb = this.init_DataUtil;
      return lb != null && (!lb.load_Z || lb.read_Z);
   }

   public boolean write_Z() {
      DataUtil lb = this.init_DataUtil;
      return lb != null && lb.load_Z && L.read_Z() && lb.sync.equals(L.push());
   }

   public String write_Str() {
      DataUtil lb = this.init_DataUtil;
      return lb == null ? autismclient.e.DataUtil.init(new int[]{-1798794835}) : this.update_Str;
   }

   private boolean pop_Z() {
      return this.init_DataUtil != null && !this.update_Str.isEmpty() && this.update_Str.equals(this.init_Str);
   }

   public boolean update_Z() {
      DataUtil lb = this.init_DataUtil;
      return lb != null && this.read_Set.contains(lb.init_Str);
   }

   public boolean sync_Z() {
      DataUtil lb = this.init_DataUtil;
      return lb != null && this.read_Set.contains(lb.init_Str) ? this.init_EventBus.init_Z_Str(lb.init_Str) : this.init_EventBus.init_Z();
   }

   public boolean flush_Z() {
      DataUtil lb = this.init_DataUtil;
      return lb != null && this.read_Set.contains(lb.init_Str) ? this.init_EventBus.load_Z(lb.init_Str) : this.init_EventBus.load_Z();
   }

   public boolean close_Z() {
      return this.init_DataUtil == null || System.currentTimeMillis() - this.cache < 25000L;
   }

   private boolean load_Z_Str(String fp) {
      return fp != null && !this.init_Set.contains(fp);
   }

   public static String update_Str() {
      try {
         ServerData sd = Minecraft.getInstance().getCurrentServer();
         return sd != null && sd.ip != null && !sd.ip.isBlank() ? sd.ip.trim() : null;
      } catch (Throwable t) {
         return null;
      }
   }

   public static boolean init_Z_RenderState(RenderState peer) {
      if (peer != null && peer.init_Z && peer.update_Str != null && !peer.update_Str.isBlank()) {
         String mine = update_Str();
         return mine != null && mine.equalsIgnoreCase(peer.update_Str.trim());
      } else {
         return false;
      }
   }

   public static boolean init_Z_Str(String ip) {
      if (ip != null && !ip.isBlank()) {
         String mine = update_Str();
         return mine != null && mine.equalsIgnoreCase(ip.trim());
      } else {
         return false;
      }
   }

   public List<autismclient.util.mm.c.BufferHelper> init_List() {
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      return rm == null ? List.of() : rm.init_List();
   }

   public String sync_Str() {
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      if (rm == null) {
         return autismclient.e.DataUtil.init(new int[]{-1798794835, -1798794812, 71934864, -2064983326, 410023760});
      }

      int connected = 0;
      List<autismclient.util.mm.c.BufferHelper> statuses = rm.init_List();

      for (autismclient.util.mm.c.BufferHelper status : statuses) {
         if (status.init_Z) {
            connected++;
         }
      }

      return "lobby=" + this.init_Z() + " relays=" + connected + "/" + statuses.size();
   }

   public BufferHelper.Inner_DataUtil init_BufferHelper_DataU() {
      if (this.init_CachePool_2 == null) {
         return BufferHelper.Inner_DataUtil.init;
      }

      long now = System.currentTimeMillis();
      if (this.add_Z()) {
         this.poll_Z = true;
         this.clear = 0L;
         return BufferHelper.Inner_DataUtil.load;
      }

      if (this.clear == 0L) {
         this.clear = now;
      }

      long offlineFor = now - this.clear;
      if (!this.poll_Z && offlineFor >= 2000L && !this.flush_Str().isEmpty()) {
         return BufferHelper.Inner_DataUtil.read;
      } else {
         return offlineFor < 12000L ? BufferHelper.Inner_DataUtil.init : BufferHelper.Inner_DataUtil.read;
      }
   }

   public boolean open_Z() {
      BufferHelper.Inner_DataUtil s = this.init_BufferHelper_DataU();
      return s == BufferHelper.Inner_DataUtil.load || this.poll_Z && s == BufferHelper.Inner_DataUtil.init;
   }

   public String flush_Str() {
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      if (rm == null) {
         return autismclient.e.DataUtil.init(new int[]{-1798794833});
      }

      for (autismclient.util.mm.c.BufferHelper s : rm.init_List()) {
         if (s.load_Str != null && !s.load_Str.isEmpty()) {
            return s.load_Str;
         }
      }

      return autismclient.e.DataUtil.init(new int[]{-1798794833});
   }

   public void init_V() {
      this.clear = 0L;
      this.init_CachePool().init_V();
   }

   public void load_V() {
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      if (rm != null) {
         rm.load();
      }
   }

   public BufferHelper.Inner_StreamUtil init_BufferHelper_Strea() {
      return this.init_BufferHelper_Strea;
   }

   public String close_Str() {
      return this.write_Str;
   }

   public boolean poll_Z() {
      return this.update_Z;
   }

   public long init_J() {
      return this.run;
   }

   public synchronized void read_V() {
      this.init_BufferHelper_Strea = BufferHelper.Inner_StreamUtil.init;
      this.write_Str = autismclient.e.DataUtil.init(new int[]{-1798794831});
      if (this.init_DataUtil != null) {
         this.close_V();
      } else {
         this.add_V();
      }
   }

   public RenderState init_RenderState_Str(String fpHex) {
      if (fpHex == null) {
         return null;
      }

      RenderState live = this.init_Map.get(fpHex);
      if (live != null) {
         return live;
      }

      for (BufferHelper.Inner_BufferHelper e : this.write_List) {
         if (fpHex.equalsIgnoreCase(e.load_Str())) {
            byte[] fp = init_BArr_Str(e.load_Str());
            if (fp == null) {
               return null;
            }

            RenderState rosterPeer = new RenderState(fp);
            rosterPeer.flush = e.init_Str();
            rosterPeer.close = HashUtil.init(e.read(), 32);
            rosterPeer.read_Z = e.init_Z();
            rosterPeer.write_Z = e.load_Z();
            rosterPeer.open = e.write();
            return rosterPeer;
         }
      }

      return null;
   }

   public List<RenderState> load_List() {
      long now = System.currentTimeMillis();
      List<RenderState> cache = this.init_List;
      if (cache != null && now - this.close_J < 250L) {
         return cache;
      }

      List<BufferHelper.Inner_BufferHelper> roster = this.write_List;
      List<RenderState> out;
      if (this.init_DataUtil != null && !roster.isEmpty()) {
         out = new ArrayList<>(roster.size());

         for (BufferHelper.Inner_BufferHelper e : roster) {
            String fp = e.load_Str() == null ? autismclient.e.DataUtil.init(new int[]{-1798794831}) : e.load_Str().toLowerCase(Locale.ROOT);
            if (!fp.equals(this.init_Str)) {
               RenderState live = fp.isEmpty() ? null : this.init_Map.get(fp);
               if (live != null) {
                  String discordName = HashUtil.init(e.read(), 32);
                  if (!discordName.isBlank()) {
                     live.close = discordName;
                  }

                  if (e.init_Str() != null && !e.init_Str().isBlank()) {
                     live.flush = e.init_Str();
                  }

                  live.read_Z = e.init_Z();
                  live.write_Z = e.load_Z();
                  live.open = e.write();
                  out.add(live);
               } else {
                  byte[] fpb = init_BArr_Str(fp);
                  RenderState ghost = new RenderState(fpb != null ? fpb : new byte[8]);
                  ghost.close = HashUtil.init(e.read(), 32);
                  ghost.flush = e.init_Str() == null ? autismclient.e.DataUtil.init(new int[]{-1798794829}) : e.init_Str();
                  ghost.read_Z = e.init_Z();
                  ghost.write_Z = e.load_Z();
                  ghost.open = e.write();
                  out.add(ghost);
               }
            }
         }
      } else {
         out = new ArrayList<>(this.init_Map.values());
         out.removeIf(p -> now - p.init_J > 60000L);
      }

      out.sort(Comparator.comparing(RenderState::init, String.CASE_INSENSITIVE_ORDER));
      this.init_List = out;
      this.close_J = now;
      return out;
   }

   public int init_I() {
      List<BufferHelper.Inner_BufferHelper> roster = this.write_List;
      if (this.init_DataUtil != null && !roster.isEmpty()) {
         return roster.size();
      }

      long now = System.currentTimeMillis();
      int live = 0;

      for (RenderState p : this.init_Map.values()) {
         if (now - p.init_J <= 60000L) {
            live++;
         }
      }

      return live + (this.init_DataUtil != null ? 1 : 0);
   }

   public long load_J() {
      DataUtil lb = this.init_DataUtil;
      return this.valid * 1001L + (lb == null ? 0 : lb.load_I);
   }

   public int load_I() {
      return this.write_I;
   }

   public List<StreamUtil> read_List() {
      long now = System.currentTimeMillis();
      List<StreamUtil> cache = this.load_List;
      if (cache != null && now - this.open_J < 250L) {
         return cache;
      }

      List<StreamUtil> out = new ArrayList<>(this.load_Map.values());
      out.removeIf(l -> !l.init_Z_J(now) || this.load_Set.contains(l.init_Str));
      out.sort(StreamUtil.init_Comparator);
      this.load_List = out;
      this.open_J = now;
      return out;
   }

   public synchronized List<JsonUtil> write_List() {
      return new ArrayList<>(this.init_Deque);
   }

   private synchronized autismclient.util.mm.c.CachePool init_CachePool() {
      if (this.init_CachePool_2 == null) {
         List<autismclient.util.mm.c.StreamUtil> set = new ArrayList<>();
         init_V_List_Supplier(
            set,
            () -> autismclient.util.mm.c.DataUtil.init(
               autismclient.e.DataUtil.init(new int[]{-1798794555, -1798794584, 71943601}),
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794555,
                     -1798794574,
                     71943599,
                     -2064714283,
                     418365223,
                     84419752,
                     -1677955983,
                     -477028603,
                     -1902981646,
                     1137108583,
                     890626689,
                     1839626591,
                     1193847893,
                     -1645422494,
                     531507473,
                     -703135103,
                     -322348352,
                     -1402865247,
                     -539148403,
                     466271435,
                     1569511755,
                     1410223099,
                     767239752,
                     -1985370605,
                     -1416943672,
                     -975581892,
                     -178266937,
                     -1231306657,
                     484197976,
                     2125234023,
                     1457742622
                  }
               ),
               () -> L.freshJwt(180000L),
               L::push
            )
         );
         this.init_CachePool_2 = new autismclient.util.mm.c.CachePool(set);
      }

      this.pop_V();
      this.tick_V();
      return this.init_CachePool_2;
   }

   private static void init_V_List_Supplier(List<autismclient.util.mm.c.StreamUtil> set, Supplier<autismclient.util.mm.c.StreamUtil> factory) {
      try {
         set.add(factory.get());
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .warn(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794829,
                     -1798794786,
                     71935171,
                     -2064977612,
                     410202397,
                     -168625136,
                     -932408740,
                     1160098743,
                     1603323528,
                     -1836574953,
                     -1099247653,
                     283059958,
                     184921114,
                     1437589901,
                     1615617300,
                     -1455470276,
                     2125062548,
                     1452432594,
                     2075736794,
                     -76669831,
                     1918199571
                  }
               ),
               t
            );
      }
   }

   private synchronized void pop_V() {
      if (this.load_ScheduledExecutorS == null) {
         this.load_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(
            r -> {
               Thread t = new Thread(
                  r,
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794557, -1798794578, 71943667, -2064716279, 418305594, 82572779, -1735214265, 2042931343, -1093635185, 457050146}
                  )
               );
               t.setDaemon(true);
               return t;
            }
         );
         this.load_ScheduledExecutorS.scheduleAtFixedRate(this::stop, 2L, 2L, TimeUnit.SECONDS);
      }
   }

   private synchronized void tick_V() {
      if (this.init_ScheduledExecutorS == null) {
         this.init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(
            r -> {
               Thread t = new Thread(
                  r,
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794557,
                        -1798794578,
                        71943667,
                        -2064716279,
                        418305595,
                        82572780,
                        -1735214254,
                        2042931331,
                        -1093635198,
                        457050163,
                        1283653837,
                        1138562010,
                        935682330
                     }
                  )
               );
               t.setDaemon(true);
               return t;
            }
         );
         this.init_ScheduledExecutorS.scheduleWithFixedDelay(this::run_V, 1L, 6000L, TimeUnit.MILLISECONDS);
      }
   }

   private void run_V() {
      try {
         DataUtil lb = this.init_DataUtil;
         if (lb != null) {
            long now = System.currentTimeMillis();
            if (now - this.flush_J >= 10000L) {
               this.flush_J = this.init_Z_DataUtil(lb) ? now : 0L;
            }
         }

         if (this.init_Z || this.tick_Z()) {
            this.start();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .debug(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794827,
                     -1798794792,
                     71935105,
                     -2064975690,
                     410262089,
                     -166778082,
                     -875150564,
                     -1359861187,
                     793977036,
                     -1156516531,
                     -1492274785,
                     984124520,
                     443086664,
                     850783963,
                     604499974,
                     1559633333,
                     1103994320,
                     -135915306,
                     81592020,
                     -1765613573,
                     1100551239,
                     -242650611,
                     1067769113,
                     -1258893038
                  }
               ),
               t
            );
      }
   }

   public synchronized void write_V() {
      if (L.load_Z() && !this.init_Z) {
         this.init_Z = true;
         this.put_V();
         this.init_CachePool();
         long now = System.currentTimeMillis();
         if (now - this.poll_J > 3000L) {
            this.poll_J = now;
            this.clear_V();
         }
      }
   }

   public synchronized void update_V() {
      if (this.init_Z) {
         this.init_Z = false;
         this.set_V();
      }
   }

   private boolean tick_Z() {
      return this.init_EventBus.read_Z() && !this.write_Z && this.init_DataUtil == null && !this.init_EventBus.write_Z() && L.load_Z();
   }

   private void get_V() {
      if (this.tick_Z()
         && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.load
         && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.read) {
         StreamUtil official = null;

         for (StreamUtil l : this.load_Map.values()) {
            if (l.load_Z) {
               official = l;
               break;
            }
         }

         if (official != null) {
            this.write_Z = true;
            this.init_V_Str(official.init_Str);
         }
      }
   }

   public void sync_V() {
      this.write_Z = false;
   }

   public void flush_V() {
      if (this.tick_Z()) {
         this.pop_V();
         this.tick_V();
      }
   }

   private void set_V() {
      if (this.init_DataUtil == null
         && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.load
         && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.read
         && this.init_CachePool_2 != null) {
         this.put_V();
         autismclient.util.mm.c.CachePool expected = this.init_CachePool_2;
         long generation = this.next;
         this.pop_V();
         this.init_ScheduledFuture = this.load_ScheduledExecutorS.schedule(() -> this.init_V_CachePool_J(expected, generation), 15000L, TimeUnit.MILLISECONDS);
      }
   }

   private void put_V() {
      this.next++;
      ScheduledFuture<?> pending = this.init_ScheduledFuture;
      this.init_ScheduledFuture = null;
      if (pending != null) {
         pending.cancel(false);
      }
   }

   private void add_V() {
      autismclient.util.mm.c.CachePool expected = this.init_CachePool_2;
      this.put_V();
      this.init_V_CachePool_J(expected, this.next);
   }

   private synchronized void init_V_CachePool_J(autismclient.util.mm.c.CachePool expected, long generation) {
      if (generation == this.next) {
         this.init_ScheduledFuture = null;
         if (this.init_DataUtil == null
            && !this.init_Z
            && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.load
            && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.read) {
            autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
            if (rm != null && rm == expected) {
               this.init_CachePool_2 = null;
               this.sync_Z = false;
               this.get = 0L;
               this.close_Z = false;
               this.set = 0L;
               this.open_Z = false;
               this.init_V_CachePool_2(rm);
            }
         }
      }
   }

   private void init_V_CachePool_2(autismclient.util.mm.c.CachePool rm) {
      Runnable close = rm::load;
      ScheduledExecutorService ex = this.load_ScheduledExecutorS;
      if (ex != null) {
         try {
            ex.execute(close);
            return;
         } catch (Throwable var5) {
         }
      }

      Thread t = new Thread(
         close,
         autismclient.e.DataUtil.init(
            new int[]{
               -1798794827,
               -1798794792,
               71935105,
               -2064975685,
               410262111,
               -166778094,
               -875150590,
               -1359861191,
               793977046,
               -1156516588,
               -1492274797,
               984124534,
               443086686,
               850783880,
               604499991
            }
         )
      );
      t.setDaemon(true);
      t.start();
   }

   private void clear_V() {
      ScheduledExecutorService ex = this.init_ScheduledExecutorS;
      if (ex != null) {
         try {
            ex.execute(this::start);
         } catch (Throwable var3) {
         }
      }
   }

   private void reset() {
      this.init_V_Runnable(() -> {
         DataUtil lb = this.init_DataUtil;
         if (lb != null) {
            this.flush_J = this.init_Z_DataUtil(lb) ? System.currentTimeMillis() : 0L;
         }
      });
   }

   private boolean init_Z_DataUtil(DataUtil lb) {
      String jwt = L.tick();
      if (jwt != null && !jwt.isBlank()) {
         JsonObject body = new JsonObject();
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794825, -1798794787, 71935325, -2064973716}), jwt);
         body.addProperty(
            autismclient.e.DataUtil.init(new int[]{-1798794825, -1798794789, 71935301, -2064973702, 410321545, -164931072, -817892123, 415146174}), lb.init_Str
         );
         body.addProperty(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794823,
                  -1798794805,
                  71935239,
                  -2064971799,
                  410381277,
                  -163084002,
                  -760633960,
                  -2104813814,
                  -824716210,
                  203600207,
                  2016638495,
                  -1908713609,
                  959417538,
                  -322828143
               }
            ),
            this.valid
         );
         if (this.pop_Z() && lb.init_Z) {
            body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794823, -1798794793, 71935241, -2064971785, 410381260}), lb.load_Str);
            body.addProperty(
               autismclient.e.DataUtil.init(new int[]{-1798794821, -1798794808, 71935427, -2064969874, 410440721, -161236968, -703375526}), lb.read_Str
            );
            body.addProperty(
               autismclient.e.DataUtil.init(new int[]{-1798794821, -1798794805, 71935434, -2064969879, 410440704, -161236972, -703375546, -329806419}),
               lb.write
            );
            body.addProperty(
               autismclient.e.DataUtil.init(
                  new int[]{-1798794819, -1798794795, 71935371, -2064967955, 410500433, -159389893, -646117357, 1445200912, 1851557826}
               ),
               this.init_EventBus.load_I()
            );
            body.addProperty(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794819,
                     -1798794802,
                     71935361,
                     -2064967956,
                     410500435,
                     -159389926,
                     -646117356,
                     1445200947,
                     1851557839,
                     1563716896,
                     1230584474,
                     -506584505,
                     1475748429
                  }
               ),
               this.init_EventBus.init_Z()
            );
         }

         AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
            autismclient.e.DataUtil.init(
               new int[]{-1798794817, -1798794864, 71935555, -2064966066, 410559885, -157542674, -588858927, -1074759028, 1042211078, -2051192026}
            ),
            body
         );
         if (r.init_I() == 404) {
            this.init_V_DataUtil(lb);
            return false;
         } else if (r.init_Z() && r.init_JsonObject() != null) {
            this.init_V_JsonObject(r.init_JsonObject());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void init_V_DataUtil(DataUtil lb) {
      String jwt = L.tick();
      if (jwt != null && !jwt.isBlank()) {
         JsonObject body = new JsonObject();
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794817, -1798794795, 71935573, -2064966060}), jwt);
         if (lb.init_Z) {
            body.addProperty(
               autismclient.e.DataUtil.init(new int[]{-1798794815, -1798794835, 71935503, -2064964160, 410619587, -155695622, -531600725, 700248320}),
               lb.init_Str
            );
         } else {
            body.addProperty(
               autismclient.e.DataUtil.init(
                  new int[]{-1798794815, -1798794837, 71935503, -2064964149, 410619599, -155695657, -531600755, 700248335, 232864582, -1371133653}
               ),
               lb.update
            );
         }

         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794813, -1798794843, 71935726}), this.init_Str);
         AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
            autismclient.e.DataUtil.init(new int[]{-1798794813, -1798794772, 71935732, -2064962229, 410679094, -153848597}), body
         );
         if (r.init_I() != -1) {
            if (r.init_Z() && r.init_JsonObject() != null) {
               synchronized (this) {
                  if (this.init_DataUtil == lb) {
                     JsonObject b = r.init_JsonObject();
                     if (b.has(autismclient.e.DataUtil.init(new int[]{-1798794811, -1798794833, 71935659, -2064960302}))
                        && b.has(autismclient.e.DataUtil.init(new int[]{-1798794811, -1798794848, 71935652, -2064960298}))) {
                        L.init_V(
                           b.get(autismclient.e.DataUtil.init(new int[]{-1798794809, -1798794835, 71935853, -2064958372})).getAsString(),
                           b.get(autismclient.e.DataUtil.init(new int[]{-1798794809, -1798794846, 71935842, -2064958376})).getAsLong()
                        );
                     }

                     this.init_V_JsonObject(b);
                     autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
                     if (rm != null) {
                        rm.init_V();
                     }
                  }
               }
            } else {
               String err = r.init_Str();
               synchronized (this) {
                  if (this.init_DataUtil == lb) {
                     bi.init(
                        err.equals(
                              autismclient.e.DataUtil.init(
                                 new int[]{
                                    -1798794807,
                                    -1798794843,
                                    71935799,
                                    -2064956472,
                                    410857979,
                                    -148307470,
                                    -302567547,
                                    -789656818,
                                    1290445434,
                                    1349099811,
                                    -1127577550,
                                    -595164341,
                                    -1270226183
                                 }
                              )
                           )
                           ? autismclient.e.DataUtil.init(
                              new int[]{
                                 -1798794805,
                                 -1798794862,
                                 71935993,
                                 -2064954535,
                                 410917495,
                                 -146460422,
                                 -245309059,
                                 985350556,
                                 481098684,
                                 2029158255,
                                 -1520604440,
                                 105900245,
                                 -1012060874,
                                 -1309114754,
                                 -1927854998,
                                 366034711,
                                 -1537823047,
                                 -427873037,
                                 -379163144,
                                 1130841670,
                                 696352816,
                                 112102392,
                                 -819796224,
                                 356121730,
                                 -1845124727,
                                 -1364288970,
                                 656715990,
                                 -1116642408,
                                 -256175345,
                                 648501622,
                                 -1371289018,
                                 439712280,
                                 746181788,
                                 1656801269
                              }
                           )
                           : (
                              err.equals(autismclient.e.DataUtil.init(new int[]{-1798794807, -1798794833, 71935789, -2064956474, 410857973}))
                                 ? autismclient.e.DataUtil.init(
                                    new int[]{
                                       -1798794805,
                                       -1798794872,
                                       71935993,
                                       -2064954535,
                                       410917435,
                                       -146460439,
                                       -245309066,
                                       985350601,
                                       481098669,
                                       2029158255,
                                       -1520604440,
                                       105900245,
                                       -1012060879,
                                       -1309114754,
                                       -1927854987,
                                       366034716,
                                       -1537822989,
                                       -427873037,
                                       -379163182,
                                       1130841691,
                                       696352829,
                                       112102391,
                                       -819796135,
                                       356121814,
                                       -1845124728,
                                       -1364288988,
                                       656715906,
                                       -1116642338,
                                       -256175338,
                                       648501621,
                                       -1371289016,
                                       439712340
                                    }
                                 )
                                 : autismclient.e.DataUtil.init(
                                    new int[]{
                                       -1798794803,
                                       -1798794855,
                                       71935932,
                                       -2064952625,
                                       410977121,
                                       -144613457,
                                       -188050886,
                                       -1534609377,
                                       -328247819,
                                       -1585750733,
                                       -1913631583,
                                       806964754,
                                       -753895312,
                                       -1895920800,
                                       1355995002,
                                       -913828966,
                                       1736076020,
                                       -2016220939,
                                       1921659312,
                                       -558102055,
                                       -121295501,
                                       534804208,
                                       -600941509,
                                       -1449316598,
                                       -1979138113,
                                       -1223740360,
                                       718755478,
                                       806585691,
                                       -765648312,
                                       2034705015,
                                       -1348656893,
                                       1141312158,
                                       1020941519,
                                       1584414648,
                                       1872214986
                                    }
                                 )
                           ),
                        -42149
                     );
                     this.close_V();
                  }
               }
            }
         }
      }
   }

   private synchronized void init_V_JsonObject(JsonObject b) {
      if (this.init_DataUtil != null && b != null) {
         this.cache = System.currentTimeMillis();
         if (b.has(autismclient.e.DataUtil.init(new int[]{-1798794803, -1798794841, 71935907, -2064952614}))
            && b.has(autismclient.e.DataUtil.init(new int[]{-1798794801, -1798794838, 71936106, -2064950720}))) {
            L.init_V(
               b.get(autismclient.e.DataUtil.init(new int[]{-1798794801, -1798794843, 71936101, -2064950716})).getAsString(),
               b.get(autismclient.e.DataUtil.init(new int[]{-1798794799, -1798794828, 71936040, -2064948798})).getAsLong()
            );
         }

         if (b.has(autismclient.e.DataUtil.init(new int[]{-1798794799, -1798794845, 71936063, -2064948770, 411096308}))) {
            L.init_V(init_Str_JsonObject_Str(b, autismclient.e.DataUtil.init(new int[]{-1798794797, -1798794847, 71936225, -2064946856, 411155754})));
         }

         String contentTopic = init_Str_JsonObject_Str(
            b,
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794797,
                  -1798794832,
                  71936225,
                  -2064946854,
                  411155771,
                  -139072272,
                  -16275842,
                  -504554622,
                  1538679429,
                  454424376,
                  1202254818,
                  -1384808751,
                  20600880
               }
            )
         );
         if (contentTopic.isBlank()) {
            contentTopic = init_Str_JsonObject_Str(
               b, autismclient.e.DataUtil.init(new int[]{-1798794795, -1798794847, 71936163, -2064944954, 411215460, -137225228})
            );
         }

         this.init_V_Str(
            contentTopic,
            init_Str_JsonObject_Str(
               b,
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794795,
                     -1798794826,
                     71936163,
                     -2064944936,
                     411215481,
                     -137225243,
                     40982305,
                     1270452756,
                     729332955,
                     1134482742,
                     809227680,
                     -683744173,
                     278766450
                  }
               )
            ),
            init_Str_JsonObject_Str(
               b,
               autismclient.e.DataUtil.init(new int[]{-1798794793, -1798794844, 71936371, -2064943029, 411274911, -135378186, 98240764, -1249507181, -80013778})
            )
         );
         DataUtil active = this.init_DataUtil;
         if (active != null) {
            if (b.has(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794793,
                     -1798794844,
                     71936378,
                     -2064943011,
                     411274922,
                     -135378190,
                     98240745,
                     -1249507192,
                     -80013797,
                     1814541118,
                     416200828,
                     17320399,
                     536931750,
                     -534983566,
                     595376126
                  }
               )
            )) {
               active.load_I = b.get(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794791,
                           -1798794838,
                           71936312,
                           -2064941089,
                           411334632,
                           -133531152,
                           155498927,
                           525500174,
                           -889360291,
                           -1800367816,
                           23173694,
                           718384973,
                           795097316,
                           -1121789584,
                           -415741280
                        }
                     )
                  )
                  .getAsInt();
            }

            if (b.has(autismclient.e.DataUtil.init(new int[]{-1798794791, -1798794822, 71936297, -2064941100, 411334618, -133531138, 155498916, 525500184}))) {
               boolean nextCanSend = b.get(
                     autismclient.e.DataUtil.init(new int[]{-1798794789, -1798794824, 71936487, -2064939182, 411394068, -131684104, 212757350, -1994459750})
                  )
                  .getAsBoolean();
               boolean permissionChanged = active.read_Z != nextCanSend;
               active.read_Z = nextCanSend;
               if (permissionChanged && this.init_CachePool_2 != null) {
                  this.init_CachePool_2.init_V();
               }
            }

            if (this.read_Z && b.has(autismclient.e.DataUtil.init(new int[]{-1798794789, -1798794831, 71936497, -2064939192})) && this.init_CachePool_2 != null
               )
             {
               this.read_Z = false;
               this.init_CachePool_2.init_V();
            }

            this.init_V_Str_Integer(
               init_Str_JsonObject_Str(
                  b, autismclient.e.DataUtil.init(new int[]{-1798794787, -1798794827, 71936427, -2064937267, 411453809, -129837095, 270015542})
               ),
               b.has(autismclient.e.DataUtil.init(new int[]{-1798794787, -1798794839, 71936417, -2064937268, 411453800}))
                  ? b.get(autismclient.e.DataUtil.init(new int[]{-1798794785, -1798794837, 71936615, -2064935374, 411513262})).getAsInt()
                  : null
            );
            int ke = b.has(
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794785, -1798794828, 71936615, -2064935367, 411513222, -127990063, 327273963, 1555555169, 977567021}
                  )
               )
               ? b.get(
                     autismclient.e.DataUtil.init(
                        new int[]{-1798794783, -1798794870, 71936549, -2064933445, 411572932, -126143021, 384532141, -964404761, 168220523}
                     )
                  )
                  .getAsInt()
               : -1;
            if (ke >= 0 && (ke != this.update_I || this.init_BArr == null)) {
               byte[] k = resolveLobbyKey(b);
               if (k != null && k.length == 32) {
                  if (this.init_BArr != null) {
                     List<BufferHelper.Inner_CachePool> old = new ArrayList<>(2);
                     old.add(new BufferHelper.Inner_CachePool(this.update_I, this.init_BArr));

                     for (BufferHelper.Inner_CachePool o : this.read_List) {
                        if (old.size() >= 2) {
                           break;
                        }

                        old.add(o);
                     }

                     this.read_List = List.copyOf(old);
                  }

                  this.init_BArr = k;
                  this.update_I = ke;
               }
            }

            long rv = b.has(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794783,
                        -1798794861,
                        71936559,
                        -2064933455,
                        411572981,
                        -126143034,
                        384532144,
                        -964404782,
                        168220518,
                        919865623,
                        -1548934345,
                        -772324049,
                        1827759082,
                        825953609
                     }
                  )
               )
               ? b.get(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794781,
                           -1798794863,
                           71936529,
                           -2064931529,
                           411632459,
                           -124296000,
                           441790578,
                           810602576,
                           -641126236,
                           1599923989,
                           -1941961231,
                           -71259487,
                           2085924396,
                           239147591
                        }
                     )
                  )
                  .getAsLong()
               : -1L;
            if (b.has(autismclient.e.DataUtil.init(new int[]{-1798794781, -1798794863, 71936529, -2064931529, 411632459, -124296000, 441790578}))
               && b.get(autismclient.e.DataUtil.init(new int[]{-1798794779, -1798794857, 71936723, -2064929611, 411691913, -122448958, 499048780}))
                  .isJsonArray()) {
               List<BufferHelper.Inner_BufferHelper> fresh = new ArrayList<>();

               for (JsonElement el : b.getAsJsonArray(
                  autismclient.e.DataUtil.init(new int[]{-1798794779, -1798794857, 71936723, -2064929611, 411691913, -122448958, 499048780})
               )) {
                  if (el.isJsonObject()) {
                     JsonObject o = el.getAsJsonObject();
                     fresh.add(
                        new BufferHelper.Inner_BufferHelper(
                           init_Str_JsonObject_Str(o, autismclient.e.DataUtil.init(new int[]{-1798794777, -1798794877, 71936659, -2064927700})),
                           init_Str_JsonObject_Str(o, autismclient.e.DataUtil.init(new int[]{-1798794777, -1798794879, 71936650})).toLowerCase(Locale.ROOT),
                           init_Str_JsonObject_Str(o, autismclient.e.DataUtil.init(new int[]{-1798794775, -1798794873, 71936857, -2064925785, 411811100})),
                           o.has(autismclient.e.DataUtil.init(new int[]{-1798794775, -1798794872, 71936860, -2064925785, 411811088, -118754875}))
                              && o.get(autismclient.e.DataUtil.init(new int[]{-1798794773, -1798794870, 71936786, -2064923871, 411870814, -116907837}))
                                 .getAsBoolean(),
                           o.has(
                                 autismclient.e.DataUtil.init(
                                    new int[]{-1798794773, -1798794856, 71936774, -2064923863, 411870806, -116907834, 670823581, -679302532}
                                 )
                              )
                              && o.get(
                                    autismclient.e.DataUtil.init(
                                       new int[]{-1798794771, -1798794850, 71936964, -2064921941, 411930260, -115060796, 728082003, 1095704802}
                                    )
                                 )
                                 .getAsBoolean(),
                           init_Str_JsonObject_Str(o, autismclient.e.DataUtil.init(new int[]{-1798794771, -1798794849, 71936987, -2064921950, 411930256}))
                        )
                     );
                  }
               }

               if (rv < 0L) {
                  this.write_List = fresh;
               } else if (rv >= this.valid) {
                  if (this.push_Z && rv > this.valid) {
                     this.init_V_List_List(this.write_List, fresh);
                  }

                  this.write_List = fresh;
                  this.valid = rv;
                  this.push_Z = true;
                  this.close_J = 0L;
               }

               for (BufferHelper.Inner_BufferHelper e : fresh) {
                  if (!e.init_Str().isBlank()
                     && e.load_Str()
                        .matches(
                           autismclient.e.DataUtil.init(
                              new int[]{
                                 -1798794769,
                                 -1798794828,
                                 71936962,
                                 -2064919939,
                                 411989898,
                                 -113213744,
                                 785340249,
                                 -1424255116,
                                 -1202238616,
                                 1385306888,
                                 -5156153,
                                 -159839262,
                                 -660050230
                              }
                           )
                        )) {
                     this.write_Map.put(e.load_Str(), e.init_Str());
                  }
               }
            } else if (rv > this.valid) {
               this.data();
            }
         }
      }
   }

   private void init_V_Str(String contentTopic, String controlTopic, String sysTopic) {
      DataUtil lb = this.init_DataUtil;
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      if (lb != null && rm != null && !contentTopic.isBlank() && !controlTopic.isBlank() && !sysTopic.isBlank()) {
         autismclient.util.mm.a.ConfigStore old = lb.init_ConfigStore;
         if (!contentTopic.equals(old.read()) || !controlTopic.equals(old.write()) || !sysTopic.equals(old.update())) {
            try {
               autismclient.util.mm.a.ConfigStore updated = autismclient.util.mm.a.ConfigStore.init(
                  lb.init_Str, contentTopic, controlTopic, sysTopic, lb.init_Z
               );
               if (!contentTopic.equals(old.read())) {
                  rm.init_V_Str_Consumer(contentTopic, this::init_V_BArr);
               }

               if (!controlTopic.equals(old.write())) {
                  rm.init_V_Str_Consumer(controlTopic, this::load_V_BArr);
               }

               if (!sysTopic.equals(old.update())) {
                  rm.load(sysTopic, this::read_V_BArr);
               }

               lb.init(updated);
               if (!contentTopic.equals(old.read())) {
                  rm.init(old.read());
               }

               if (!controlTopic.equals(old.write())) {
                  rm.init(old.write());
               }

               if (!sysTopic.equals(old.update())) {
                  rm.load(old.update());
               }

               if (!this.read_Z) {
                  rm.init_V();
               }
            } catch (IllegalArgumentException badTopic) {
               AutismClientAddon.LOG
                  .warn(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794769,
                           -1798794874,
                           71936917,
                           -2064920002,
                           411989980,
                           -113213757,
                           785340177,
                           -1424255114,
                           -1202238699,
                           1385306906,
                           -5156200,
                           -159839326,
                           -660050218,
                           1013278809,
                           1346870801,
                           -1196674830,
                           1557783065,
                           1046635674,
                           -1914029669,
                           794625324,
                           -1136417320,
                           -869199407,
                           -1175378671,
                           -2076996997,
                           37599574,
                           1165585938,
                           1773428880,
                           -858276165,
                           -836756321,
                           -169641143,
                           -963908369,
                           183608302,
                           1396887592,
                           353843100,
                           -1915762654,
                           740898836,
                           1493029809,
                           -960715616,
                           282588138,
                           170294609,
                           984164140,
                           444318199,
                           888958831,
                           1787916683,
                           -409154903
                        }
                     ),
                     badTopic
                  );
            }
         }
      }
   }

   private void init_V_Str_Integer(String hostFpHex, Integer term) {
      String hf = hostFpHex == null ? autismclient.e.DataUtil.init(new int[]{-1798794767}) : hostFpHex.toLowerCase(Locale.ROOT);
      if (hf.matches(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794767,
                  -1798794838,
                  71937024,
                  -2064918017,
                  412049480,
                  -111366702,
                  842598559,
                  350752242,
                  -2011585362,
                  2065365262,
                  -398183163,
                  541225312,
                  -401884920
               }
            )
         )
         && !hf.equals(this.update_Str)) {
         boolean wasHost = this.pop_Z();
         this.update_Str = hf;
         if (term != null) {
            this.close_I = term;
         }

         if (!wasHost && this.pop_Z()) {
            this.run(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794765,
                     -1798794838,
                     71937025,
                     -2064916191,
                     412109071,
                     -109519660,
                     899856770,
                     2125759603,
                     1474035345,
                     -1549543677,
                     -791210214,
                     1242289853,
                     -143719405,
                     -160333231,
                     -675363685,
                     538565097,
                     -484353599,
                     -2130060069,
                     -1607351784,
                     1711705139,
                     1523253056
                  }
               )
            );
         } else if (!this.pop_Z()) {
            RenderState p = this.init_Map.get(hf);
            if (p != null) {
               this.run(this.init_Str_Str(hf) + " is the host now");
            }
         }
      } else if (term != null) {
         this.close_I = term;
      }
   }

   private void init_V_List_List(List<BufferHelper.Inner_BufferHelper> before, List<BufferHelper.Inner_BufferHelper> after) {
      Set<String> old = new HashSet<>();

      for (BufferHelper.Inner_BufferHelper e : before) {
         old.add(e.init_Str());
      }

      Set<String> now = new HashSet<>();

      for (BufferHelper.Inner_BufferHelper e : after) {
         now.add(e.init_Str());
      }

      for (BufferHelper.Inner_BufferHelper e : after) {
         if (!old.contains(e.init_Str()) && !e.load_Str().equals(this.init_Str)) {
            this.run(this.init_Str_BufferHelper_Buffe(e) + " joined");
         }
      }

      for (BufferHelper.Inner_BufferHelper e : before) {
         if (!now.contains(e.init_Str()) && !e.load_Str().equals(this.init_Str)) {
            this.run(this.init_Str_BufferHelper_Buffe(e) + " left");
         }
      }
   }

   private String init_Str_BufferHelper_Buffe(BufferHelper.Inner_BufferHelper e) {
      String n = HashUtil.init(e.read(), 32);
      if (!n.isBlank()) {
         return n;
      }

      RenderState live = e.load_Str().isEmpty() ? null : this.init_Map.get(e.load_Str());
      return live != null
         ? live.init()
         : autismclient.e.DataUtil.init(
            new int[]{
               -1798794765,
               -1798794825,
               71937031,
               -2064916185,
               412109132,
               -109519654,
               899856770,
               2125759602,
               1474035345,
               -1549543654,
               -791210217,
               1242289845,
               -143719343,
               -160333220,
               -675363706
            }
         );
   }

   private void start() {
      if (this.init_Z || this.tick_Z()) {
         String jwt = L.tick();
         if (jwt != null && !jwt.isBlank()) {
            this.update_Z = true;

            try {
               JsonObject resp = init_JsonObject_Str(jwt);
               if (resp == null
                  || !resp.has(
                     autismclient.e.DataUtil.init(new int[]{-1798794763, -1798794855, 71937219, -2064914252, 412168591, -107672610, 957115211, -394200341})
                  )) {
                  return;
               }

               long now = System.currentTimeMillis();
               Map<String, StreamUtil> fresh = new HashMap<>();
               Set<String> seenIds = new HashSet<>();
               Set<String> flaggedOfficial = new HashSet<>();

               for (JsonElement el : resp.getAsJsonArray(
                  autismclient.e.DataUtil.init(new int[]{-1798794763, -1798794855, 71937219, -2064914252, 412168591, -107672610, 957115211, -394200341})
               )) {
                  if (el.isJsonObject()) {
                     JsonObject o = el.getAsJsonObject();
                     String lid = o.has(
                           autismclient.e.DataUtil.init(
                              new int[]{-1798794761, -1798794853, 71937157, -2064912326, 412228297, -105825600, 1014373413, 1380807038}
                           )
                        )
                        ? o.get(
                              autismclient.e.DataUtil.init(
                                 new int[]{-1798794761, -1798794853, 71937157, -2064912326, 412228297, -105825600, 1014373413, 1380807038}
                              )
                           )
                           .getAsString()
                        : autismclient.e.DataUtil.init(new int[]{-1798794759});
                     if (lid != null && !lid.isBlank()) {
                        boolean official = o.has(
                              autismclient.e.DataUtil.init(
                                 new int[]{-1798794759, -1798794858, 71937358, -2064910404, 412287744, -103978536, 1071631811, -1139152899, -954004601}
                              )
                           )
                           && o.get(
                                 autismclient.e.DataUtil.init(
                                    new int[]{-1798794757, -1798794860, 71937280, -2064908486, 412347470, -102131490, 1128889985, 635854463, -1763351099}
                                 )
                              )
                              .getAsBoolean();
                        seenIds.add(lid);
                        if (official) {
                           flaggedOfficial.add(lid);
                        }

                        if (!lid.equals(this.push_Str())) {
                           String hostDid = o.has(
                                 autismclient.e.DataUtil.init(
                                    new int[]{-1798794757, -1798794861, 71937289, -2064908497, 412347475, -102131463, 1128889985, 635854458}
                                 )
                              )
                              ? o.get(
                                    autismclient.e.DataUtil.init(
                                       new int[]{-1798794755, -1798794859, 71937483, -2064906579, 412406929, -100284421, 1186148431, -1884105532}
                                    )
                                 )
                                 .getAsString()
                              : autismclient.e.DataUtil.init(new int[]{-1798794755});
                           if (!this.init_EventBus.update_Z(hostDid)) {
                              byte[] fpb = init_BArr_Str(load_Str_Str(lid));
                              StreamUtil l = new StreamUtil(lid, fpb != null ? fpb : new byte[8]);
                              l.load_Str = HashUtil.init(
                                 init_Str_JsonObject_Str(o, autismclient.e.DataUtil.init(new int[]{-1798794753, -1798794863, 71937411, -2064904691, 412466630})),
                                 48
                              );
                              l.init_I = init_I_JsonObject_Str(
                                 o,
                                 autismclient.e.DataUtil.init(
                                    new int[]{-1798794753, -1798794862, 71937415, -2064904691, 412466625, -98437468, 1243406614, -109098159}
                                 )
                              );
                              l.load_I = init_I_JsonObject_Str(
                                 o,
                                 autismclient.e.DataUtil.init(
                                    new int[]{
                                       -1798794751,
                                       -1798794644,
                                       71937601,
                                       -2064902758,
                                       412526124,
                                       -96590426,
                                       1300665039,
                                       1665909190,
                                       103576198,
                                       -1084102153,
                                       752568151
                                    }
                                 )
                              );
                              l.init_Z = o.has(
                                    autismclient.e.DataUtil.init(
                                       new int[]{
                                          -1798794751,
                                          -1798794638,
                                          71937605,
                                          -2064902768,
                                          412526103,
                                          -96590426,
                                          1300665040,
                                          1665909239,
                                          103576203,
                                          -1084102172,
                                          752568150,
                                          1854774531,
                                          1663438593
                                       }
                                    )
                                 )
                                 && o.get(
                                       autismclient.e.DataUtil.init(
                                          new int[]{
                                             -1798794749,
                                             -1798794640,
                                             71937595,
                                             -2064900842,
                                             412585833,
                                             -94743392,
                                             1357923218,
                                             -854050699,
                                             -705770295,
                                             -404043802,
                                             359541008,
                                             -1739128179,
                                             1921604167
                                          }
                                       )
                                    )
                                    .getAsBoolean();
                              l.read_Str = HashUtil.init(
                                 init_Str_JsonObject_Str(
                                    o,
                                    autismclient.e.DataUtil.init(new int[]{-1798794749, -1798794640, 71937595, -2064900842, 412585833, -94743392, 1357923218})
                                 ),
                                 48
                              );
                              l.write = HashUtil.init(
                                 init_Str_JsonObject_Str(
                                    o,
                                    autismclient.e.DataUtil.init(
                                       new int[]{-1798794747, -1798794635, 71937776, -2064898925, 412645306, -92896338, 1415181680, 920956635}
                                    )
                                 ),
                                 64
                              );
                              l.read_I = o.has(
                                    autismclient.e.DataUtil.init(
                                       new int[]{-1798794747, -1798794643, 71937779, -2064898923, 412645289, -92896381, 1415181675, 920956632, -1515116998}
                                    )
                                 )
                                 ? o.get(
                                       autismclient.e.DataUtil.init(
                                          new int[]{-1798794745, -1798794641, 71937717, -2064896997, 412705007, -91049331, 1472439849, -1599003302, 1970503800}
                                       )
                                    )
                                    .getAsInt()
                                 : -1;
                              l.read_Z = o.has(
                                    autismclient.e.DataUtil.init(
                                       new int[]{
                                          -1798794745,
                                          -1798794650,
                                          71937716,
                                          -2064897018,
                                          412705012,
                                          -91049284,
                                          1472439858,
                                          -1599003319,
                                          1970503800,
                                          956072934,
                                          -426512965,
                                          -336999038,
                                          -1857032213
                                       }
                                    )
                                 )
                                 && o.get(
                                       autismclient.e.DataUtil.init(
                                          new int[]{
                                             -1798794743,
                                             -1798794648,
                                             71937910,
                                             -2064895100,
                                             412764470,
                                             -89202242,
                                             1529698292,
                                             176004047,
                                             1161157054,
                                             1636131296,
                                             -819539847,
                                             364065536,
                                             -1598866903
                                          }
                                       )
                                    )
                                    .getAsBoolean();
                              l.load_Z = official;
                              l.flush = hostDid;
                              l.init_J = now;
                              fresh.put(lid, l);
                           }
                        }
                     }
                  }
               }

               this.load_Set.removeIf(fresh::containsKey);
               synchronized (this) {
                  this.load_Map.keySet().retainAll(fresh.keySet());
                  this.load_Map.putAll(fresh);
                  this.read_Set.removeAll(seenIds);
                  this.read_Set.addAll(flaggedOfficial);
               }

               this.run = now;
               return;
            } catch (Throwable t) {
               AutismClientAddon.LOG
                  .debug(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794743,
                           -1798794652,
                           71937909,
                           -2064895030,
                           412764477,
                           -89202270,
                           1529698280,
                           176004041,
                           1161157048,
                           1636131321,
                           -819539853,
                           364065564,
                           -1598866908,
                           1974735215,
                           1087248622,
                           -655033519,
                           1168796333,
                           1872949602,
                           -2068106939,
                           313193170,
                           1119054596,
                           330956961,
                           1669732816,
                           222108379,
                           -1704576111
                        }
                     ),
                     t
                  );
            } finally {
               this.update_Z = false;
            }

            return;
         }
      }
   }

   private static String init_Str_JsonObject_Str(JsonObject o, String k) {
      try {
         return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : autismclient.e.DataUtil.init(new int[]{-1798794741});
      } catch (Throwable t) {
         return autismclient.e.DataUtil.init(new int[]{-1798794741});
      }
   }

   private static int init_I_JsonObject_Str(JsonObject o, String k) {
      try {
         return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsInt() : 0;
      } catch (Throwable t) {
         return 0;
      }
   }

   private static byte[] resolveLobbyKey(JsonObject b) {
      try {
         String kb64 = init_Str_JsonObject_Str(b, autismclient.e.DataUtil.init(new int[]{-1798794739, -1798794650, 71938033, -2064891241}));
         if (kb64.isEmpty()) {
            kb64 = init_Str_JsonObject_Str(
               b, autismclient.e.DataUtil.init(new int[]{-1798794739, -1798794641, 71938037, -2064891235, 412883632, -85508220, 1644214899, -568948535})
            );
         }

         if (kb64.isEmpty()) {
            return null;
         }

         byte[] k = Base64.getDecoder().decode(kb64);
         return k.length == 32 ? k : null;
      } catch (Throwable t) {
         return null;
      }
   }

   private String push_Str() {
      DataUtil lb = this.init_DataUtil;
      return lb == null ? null : lb.init_Str;
   }

   private synchronized void stop() {
      try {
         long now = System.currentTimeMillis();
         this.init_Map.values().removeIf(p -> now - p.init_J > 60000L);
         this.load_Map.values().removeIf(l -> !l.init_Z_J(now));
         if (this.init_Map.size() > 2000) {
            init_V_Map(this.init_Map, p -> p.init_J, 2000);
         }

         if (this.load_Map.size() > 2000) {
            init_V_Map(this.load_Map, l -> l.init_J, 2000);
         }

         this.read_Map.values().removeIf(sc -> now - sc.init_J > 60000L);
         if (this.read_Map.size() > 64) {
            init_V_Map(this.read_Map, sc -> sc.init_J, 64);
         }

         if (this.init_BufferHelper_Strea == BufferHelper.Inner_StreamUtil.read && this.init_DataUtil != null && this.add_Z()) {
            this.init_BufferHelper_Strea = BufferHelper.Inner_StreamUtil.init;
            this.prev();
            this.push_J = now;
         }

         if (this.init_DataUtil != null && !L.load_Z()) {
            this.run(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794737,
                     -1798794660,
                     71937979,
                     -2064889321,
                     412943357,
                     -83661132,
                     1701473072,
                     1206058770,
                     -1266882694,
                     -618660890,
                     -1998621022,
                     -1827707942,
                     -824370505,
                     214317081,
                     -1946103235,
                     -199657264,
                     -1894408595,
                     1402873143,
                     539393454,
                     -458670832,
                     -1333890562,
                     1599062521,
                     -1968670416,
                     -899239339,
                     -2106616449,
                     -880603399,
                     -1528904537,
                     -151398702
                  }
               )
            );
            this.close_V();
         }

         this.get_V();
         DataUtil lb = this.init_DataUtil;
         if (lb != null) {
            long presenceInterval = !lb.load_Z && lb.init_I <= 100 ? 8000L : 20000L;
            if (now - this.push_J >= presenceInterval || !this.open_Str().equals(this.read_Str)) {
               this.push_J = now;
               this.prev();
            }

            if (this.init_BArr == null) {
               this.data();
            }

            if (this.load_Z && now - this.tick > 10000L) {
               this.load_Z = false;
               if (this.init_Map.isEmpty() && !this.pop_Z()) {
                  bi.init(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794737,
                           -1798794666,
                           71937981,
                           -2064889339,
                           412943284,
                           -83661149,
                           1701473073,
                           1206058770,
                           -1266882700,
                           -618660865,
                           -1998620999,
                           -1827708006,
                           -824370446,
                           214317173,
                           -1946103248,
                           -199657261,
                           -1894408597,
                           1402873202,
                           539393524,
                           -458670760,
                           -1333890616,
                           1599062449,
                           -1968670403,
                           -899239352,
                           -2106616456,
                           -880603461,
                           -1528904534,
                           -151398764,
                           -398391046,
                           534776701,
                           -601792509,
                           -1475695663,
                           1498074181,
                           -804341638,
                           835210178,
                           121712884,
                           -521871412,
                           1001854145,
                           992708365,
                           709190956,
                           510083923,
                           -1367268918,
                           564335250,
                           314526305,
                           1160377991,
                           1611980309,
                           -1568215539,
                           -1370038615,
                           478476889,
                           1947880267,
                           254744975,
                           -692837466
                        }
                     ),
                     -14249
                  );
               }
            }

            if (this.init_EventBus.sync_Z() && now - this.put >= 16000L) {
               this.put = now;
               StringBuilder sb = new StringBuilder(
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794735, -1798794678, 71938173, -2064887393, 413002876, -81814089, 1758731515, -1313901099, -2076229452, 61397448}
                  )
               );
               autismclient.util.mm.c.CachePool rmDiag = this.init_CachePool_2;
               if (rmDiag != null) {
                  for (autismclient.util.mm.c.BufferHelper s : rmDiag.init_List()) {
                     sb.append(' ')
                        .append(s.init_Str)
                        .append(
                           s.init_Z
                              ? autismclient.e.DataUtil.init(new int[]{-1798794735, -1798794694})
                              : autismclient.e.DataUtil.init(new int[]{-1798794733, -1798794690})
                        )
                        .append(autismclient.e.DataUtil.init(new int[]{-1798794733, -1798794701, 71938107, -2064885500, 413062450}))
                        .append(s.init_AtomicLong.get())
                        .append(autismclient.e.DataUtil.init(new int[]{-1798794731, -1798794699, 71938280, -2064883559, 413121978, -78120007, 1873248051}))
                        .append(s.load_AtomicLong.get())
                        .append(
                           autismclient.e.DataUtil.init(
                              new int[]{-1798794731, -1798794699, 71938301, -2064883534, 413121983, -78120008, 1873248126, -2058853755}
                           )
                        )
                        .append(s.read.get())
                        .append(s.load_Str.isEmpty() ? autismclient.e.DataUtil.init(new int[]{-1798794729}) : " err=" + s.load_Str);
                  }
               }

               sb.append(
                     autismclient.e.DataUtil.init(
                        new int[]{-1798794729, -1798794697, 71938230, -2064881576, 413181691, -76272964, 1930506281, -283846328, -209301890, 2101572518}
                     )
                  )
                  .append(this.init_Map.size())
                  .append(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794727, -1798794695, 71938403, -2064879713, 413241136, -74425954, 1987764730, 1491161043, -1018648664, -1513336331, 331211313
                        }
                     )
                  )
                  .append(this.update_I)
                  .append(autismclient.e.DataUtil.init(new int[]{-1798794727, -1798794695, 71938403, -2064879713, 413241136, -74425882}))
                  .append(this.init_BArr != null)
                  .append(autismclient.e.DataUtil.init(new int[]{-1798794725, -1798794693, 71938350, -2064877805, 413300852, -72578903, 2045022965}))
                  .append(this.pop_Z())
                  .append(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794725,
                           -1798794693,
                           71938357,
                           -2064877810,
                           413300849,
                           -72578929,
                           2045022887,
                           -1028798899,
                           -1827995139,
                           -833277958,
                           -61815752,
                           -1916287683
                        }
                     )
                  )
                  .append(this.write_List.size())
                  .append(
                     autismclient.e.DataUtil.init(
                        new int[]{-1798794723, -1798794691, 71938551, -2064875892, 413360307, -70731885, 2102281327, 746208430, 1657625388, -153219684}
                     )
                  )
                  .append(this.close_Z())
                  .append(autismclient.e.DataUtil.init(new int[]{-1798794723, -1798794691, 71938529, -2064875875, 413360301, -70731856, 2102281275}))
                  .append(this.size)
                  .append('/')
                  .append(this.data)
                  .append(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794721,
                           -1798794689,
                           71938494,
                           -2064873824,
                           413420007,
                           -68884845,
                           -2135427797,
                           -1773751502,
                           848278821,
                           526838736,
                           -847869774,
                           -514158491,
                           1240953067,
                           -185163776,
                           -1445106571
                        }
                     )
                  )
                  .append(this.init_TextFormat.load_AtomicLong.get())
                  .append(
                     autismclient.e.DataUtil.init(
                        new int[]{-1798794721, -1798794689, 71938480, -2064873755, 413420019, -68884851, -2135427803, -1773751493, 848278840}
                     )
                  )
                  .append(this.init_TextFormat.init_AtomicLong.get())
                  .append(autismclient.e.DataUtil.init(new int[]{-1798794719, -1798794751, 71938662, -2064871826, 413479470, -67037812, -2078169370, 1255929}))
                  .append(this.init_TextFormat.read_AtomicLong.get())
                  .append(autismclient.e.DataUtil.init(new int[]{-1798794719, -1798794751, 71938657, -2064871817, 413479477, -67037813, -2078169409}))
                  .append(this.init_TextFormat.update.get())
                  .append(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794717, -1798794749, 71938641, -2064869902, 413538970, -65190761, -2020911181, 1776263215, -770414341, 1886955458, -1633923713
                        }
                     )
                  )
                  .append(this.init_TextFormat.write_AtomicLong.get());
               AutismClientAddon.LOG.info(sb.toString());
            }
         }

         this.next();
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .debug(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794717,
                     -1798794674,
                     71938643,
                     -2064869980,
                     413538955,
                     -65190772,
                     -2020911197,
                     1776263213,
                     -770414431,
                     1886955458,
                     -1633923792,
                     887970682,
                     1757283948,
                     -1358775781
                  }
               ),
               t
            );
      }
   }

   private void next() {
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      if (rm != null && this.init_DataUtil != null) {
         long now = System.currentTimeMillis();
         if (this.get == 0L) {
            this.get = now;
         }

         boolean anyConnected = false;

         for (autismclient.util.mm.c.BufferHelper s : rm.init_List()) {
            if (s.init_Z) {
               anyConnected = true;
               break;
            }
         }

         if (!this.sync_Z) {
            if (anyConnected) {
               this.sync_Z = true;
               this.flush_Z = true;
               this.close_Z = false;
            } else if (!this.close_Z && now - this.get > 9000L) {
               this.close_Z = true;
               bi.init(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794715,
                        -1798794650,
                        71938589,
                        -2064867992,
                        413598618,
                        -63343725,
                        -1963652898,
                        -743696710,
                        -1579761062,
                        -1727953464,
                        -2026950685,
                        1589035234,
                        2015449313,
                        -1945581818,
                        -183490973,
                        -1393255880,
                        -241258720,
                        1110915397,
                        78638313,
                        -1857182609,
                        -1738089042,
                        1953815174,
                        438731624,
                        715775415,
                        714202217,
                        665431185,
                        -846468661,
                        -470726662,
                        -1707627860,
                        -1396855280,
                        -352837535,
                        1946935668,
                        225462578,
                        -1600593830,
                        1921197817,
                        -572413084,
                        -564938771,
                        -333233772,
                        -1740309151,
                        1884988598,
                        -1694894559,
                        -1002122276,
                        -1001020429,
                        -966865170,
                        91948216,
                        -1444571884,
                        -1832052100,
                        -959043470
                     }
                  ),
                  -42149
               );
            }
         } else {
            if (anyConnected != this.flush_Z) {
               this.flush_Z = anyConnected;
               if (anyConnected) {
                  if (this.init_DataUtil != null) {
                     this.prev();
                     this.push_J = now;
                  }

                  if (this.open_Z) {
                     bi.init(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794715,
                              -1798794648,
                              71938589,
                              -2064867982,
                              413598686,
                              -63343729,
                              -1963652973,
                              -743696727,
                              -1579761068,
                              -1727953472,
                              -2026950674,
                              1589035245,
                              2015449339,
                              -1945581749,
                              -183490960,
                              -1393255895,
                              -241258720,
                              1110915394,
                              78638314,
                              -1857182624,
                              -1738089056,
                              1953815180,
                              438731634,
                              715775413,
                              714202147,
                              665431185,
                              -846468612,
                              -470726659,
                              -1707627799,
                              -1396855295,
                              -352837521,
                              1946935608,
                              225462570,
                              -1600593844,
                              1921197823,
                              -572413128
                           }
                        ),
                        -13248397
                     );
                  }

                  this.set = 0L;
                  this.open_Z = false;
               } else {
                  this.set = now;
               }
            }

            if (!anyConnected && !this.open_Z && this.set != 0L && now - this.set > 10000L) {
               this.open_Z = true;
               bi.init(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794713,
                        -1798794646,
                        71938779,
                        -2064866052,
                        413658136,
                        -61496703,
                        -1906394543,
                        1031310635,
                        1905859478,
                        -1047895102,
                        1874989520,
                        -2004867477,
                        -2021352609,
                        1762579458,
                        -1194608346,
                        1621847720,
                        -1262326931,
                        -477432506,
                        -1915506516,
                        748840949,
                        1739229869,
                        -1918450223,
                        657586185,
                        -1089662921,
                        580188790,
                        805979840,
                        -784429145,
                        1452501363,
                        2077866466,
                        -10651898,
                        -330205336,
                        -1646431839,
                        500222310,
                        -1672980391,
                        -322785555,
                        -1416418580,
                        -959305557,
                        326297738,
                        1525298729,
                        39621428,
                        1228265577,
                        -578475627
                     }
                  ),
                  -42149
               );
            }
         }
      } else {
         this.sync_Z = false;
         this.get = 0L;
         this.close_Z = false;
         this.set = 0L;
         this.open_Z = false;
      }
   }

   private static <V> void init_V_Map(Map<String, V> map, ToLongFunction<V> ts, int cap) {
      int over = map.size() - cap;
      if (over > 0) {
         map.entrySet()
            .stream()
            .sorted(Comparator.comparingLong(e -> ts.applyAsLong(e.getValue())))
            .limit(over)
            .map(Entry::getKey)
            .toList()
            .forEach(map::remove);
      }
   }

   public synchronized void init_V_CachePool(CachePool s) {
      if (this.run_Z()) {
         this.init_CachePool();
         CachePool settings = s;
         this.init_V_Runnable(() -> this.load_V_CachePool(settings));
      }
   }

   public synchronized void init_V_Str(String lobbyId) {
      String id = lobbyId == null ? autismclient.e.DataUtil.init(new int[]{-1798794713}) : lobbyId.trim();
      if (!id.isEmpty() && this.run_Z()) {
         this.init_CachePool();
         this.init_V_Runnable(() -> this.init_V_Str_Str(id, null));
      }
   }

   public synchronized void init_V_CArr(char[] passkey) {
      String code = passkey == null ? autismclient.e.DataUtil.init(new int[]{-1798794711}) : new String(passkey).trim();
      if (!code.isEmpty() && this.run_Z()) {
         this.init_CachePool();
         this.init_V_Runnable(() -> this.init_V_Str_Str(null, code));
      }
   }

   private boolean run_Z() {
      if (!this.put_Z()) {
         return false;
      } else if (this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.load && this.init_BufferHelper_Strea != BufferHelper.Inner_StreamUtil.read) {
         this.init_BufferHelper_Strea = BufferHelper.Inner_StreamUtil.load;
         this.write_Str = autismclient.e.DataUtil.init(new int[]{-1798794711});
         return true;
      } else {
         return false;
      }
   }

   private void init_V_Runnable(Runnable task) {
      ScheduledExecutorService ex = this.init_ScheduledExecutorS;
      if (ex != null) {
         try {
            ex.execute(task);
         } catch (Throwable var4) {
         }
      }
   }

   private synchronized void pop(String error) {
      this.write_Str = error != null && !error.isEmpty()
         ? error
         : autismclient.e.DataUtil.init(new int[]{-1798794709, -1798794675, 71938903, -2064862235, 413777307, -57802616, -1791877924});
      this.init_BufferHelper_Strea = BufferHelper.Inner_StreamUtil.write;
      this.add_V();
   }

   private void load_V_CachePool(CachePool s) {
      String jwt = L.tick();
      if (jwt != null && !jwt.isBlank()) {
         JsonObject body = new JsonObject();
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794707, -1798794681, 71938819, -2064860294}), jwt);
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794707, -1798794685, 71938837, -2064860317, 413837008}), s.init_Str);
         body.addProperty(
            autismclient.e.DataUtil.init(new int[]{-1798794705, -1798794682, 71939009, -2064858432, 413896454, -54108525, -1677361320, -458594501, -1331527018}),
            s.init_Z
         );
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794705, -1798794676, 71939027, -2064858400}), s.close);
         body.addProperty(
            autismclient.e.DataUtil.init(new int[]{-1798794703, -1798794686, 71938965, -2064856480, 413956167, -52261482, -1620103168}),
            s.init_Z ? s.load_Str : autismclient.e.DataUtil.init(new int[]{-1798794703})
         );
         body.addProperty(
            autismclient.e.DataUtil.init(new int[]{-1798794701, -1798794685, 71939138, -2064854559, 414015624, -50414436, -1562844706, -1203547099}),
            s.init_Z ? s.read_Str : autismclient.e.DataUtil.init(new int[]{-1798794701})
         );
         body.addProperty(
            autismclient.e.DataUtil.init(new int[]{-1798794699, -1798794659, 71939075, -2064852635, 414075353, -48567373, -1505586533, 571460264, 535400522}),
            this.init_EventBus.load_I()
         );
         body.addProperty(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794699,
                  -1798794682,
                  71939081,
                  -2064852636,
                  414075355,
                  -48567406,
                  -1505586532,
                  571460235,
                  535400519,
                  -582453800,
                  -876199678,
                  -1392382721,
                  -214194475
               }
            ),
            this.init_EventBus.init_Z()
         );
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794697, -1798794671, 71939290}), this.init_Str);
         body.addProperty(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794697,
                  -1798794666,
                  71939268,
                  -2064850698,
                  414134788,
                  -46720372,
                  -1448328126,
                  -1948499655,
                  -273946232,
                  97604566,
                  -1269226549,
                  -691318158,
                  43970843
               }
            ),
            s.load_Z
         );
         AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
            autismclient.e.DataUtil.init(new int[]{-1798794695, -1798794730, 71939211, -2064848792, 414194508, -44873318, -1391069922, -173492295}), body
         );
         if (r.init_I() == -1) {
            this.pop(autismclient.e.DataUtil.init(new int[]{-1798794695, -1798794665, 71939213, -2064848786, 414194526, -44873324, -1391069928, -173492297}));
         } else if (r.init_Z() && r.init_JsonObject() != null) {
            this.init_V_JsonObject(r.init_JsonObject(), true, null);
         } else {
            this.pop(r.init_Str());
         }
      } else {
         this.pop(autismclient.e.DataUtil.init(new int[]{-1798794709, -1798794683, 71938899, -2064862216, 413777280, -57802622, -1791877942, 286358053}));
      }
   }

   private void init_V_Str_Str(String publicId, String privateToken) {
      String jwt = L.tick();
      if (jwt != null && !jwt.isBlank()) {
         JsonObject body = new JsonObject();
         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794693, -1798794671, 71939409, -2064846872}), jwt);
         if (publicId != null) {
            body.addProperty(
               autismclient.e.DataUtil.init(new int[]{-1798794691, -1798794671, 71939339, -2064844932, 414313671, -41179258, -1276553297, -918444924}),
               publicId
            );
         }

         if (privateToken != null) {
            body.addProperty(
               autismclient.e.DataUtil.init(
                  new int[]{-1798794691, -1798794665, 71939339, -2064844937, 414313675, -41179221, -1276553335, -918444917, 1592981314, 2137779631}
               ),
               privateToken
            );
         }

         body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794689, -1798794663, 71939538}), this.init_Str);
         AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
            autismclient.e.DataUtil.init(new int[]{-1798794689, -1798794736, 71939528, -2064843057, 414373130, -39331985}), body
         );
         if (r.init_I() == -1) {
            this.pop(autismclient.e.DataUtil.init(new int[]{-1798794687, -1798794705, 71939461, -2064841130, 414432854, -37484948, -1162036720, -1663397489}));
         } else if (r.init_Z() && r.init_JsonObject() != null) {
            this.init_V_JsonObject(r.init_JsonObject(), false, privateToken);
         } else {
            this.pop(r.init_Str());
         }
      } else {
         this.pop(autismclient.e.DataUtil.init(new int[]{-1798794693, -1798794667, 71939395, -2064846872, 414253968, -43026286, -1333811494, 1601515061}));
      }
   }

   private synchronized void init_V_JsonObject(JsonObject b, boolean isHost, String joinTokenUsed) {
      try {
         if (b.has(autismclient.e.DataUtil.init(new int[]{-1798794687, -1798794709, 71939479, -2064841130}))
            && b.has(autismclient.e.DataUtil.init(new int[]{-1798794685, -1798794714, 71939686, -2064839212}))) {
            L.init_V(
               b.get(autismclient.e.DataUtil.init(new int[]{-1798794685, -1798794711, 71939689, -2064839216})).getAsString(),
               b.get(autismclient.e.DataUtil.init(new int[]{-1798794683, -1798794720, 71939620, -2064837290})).getAsLong()
            );
         }

         String lobbyId = b.get(
               autismclient.e.DataUtil.init(new int[]{-1798794683, -1798794711, 71939635, -2064837308, 414552063, -33790850, -1047520105, 1886617228})
            )
            .getAsString();
         boolean isPublic = b.has(
               autismclient.e.DataUtil.init(
                  new int[]{-1798794681, -1798794706, 71939817, -2064835336, 414611502, -31943829, -990261648, -633342717, 1841215422}
               )
            )
            && b.get(
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794681, -1798794706, 71939817, -2064835336, 414611502, -31943829, -990261648, -633342717, 1841215422}
                  )
               )
               .getAsBoolean();
         int cap = b.has(autismclient.e.DataUtil.init(new int[]{-1798794679, -1798794710, 71939769, -2064833446}))
            ? b.get(autismclient.e.DataUtil.init(new int[]{-1798794679, -1798794710, 71939769, -2064833446})).getAsInt()
            : 500;
         int term = b.has(autismclient.e.DataUtil.init(new int[]{-1798794677, -1798794689, 71939955, -2064831522, 414730682}))
            ? b.get(autismclient.e.DataUtil.init(new int[]{-1798794677, -1798794689, 71939955, -2064831522, 414730682})).getAsInt()
            : 0;
         String hostFp = isHost
            ? this.init_Str
            : init_Str_JsonObject_Str(
               b, autismclient.e.DataUtil.init(new int[]{-1798794675, -1798794715, 71939899, -2064829603, 414790369, -26402743, -818486874})
            );
         String privateToken = init_Str_JsonObject_Str(
            b,
            autismclient.e.DataUtil.init(
               new int[]{-1798794675, -1798794713, 71939899, -2064829625, 414790395, -26402725, -818486855, 396712091, -586824334, -1011688001}
            )
         );
         if (privateToken.isBlank() && joinTokenUsed != null) {
            privateToken = joinTokenUsed.trim();
         }

         String shareCode = isPublic ? lobbyId : privateToken;
         String contentTopic = init_Str_JsonObject_Str(
            b,
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794673,
                  -1798794708,
                  71940093,
                  -2064827682,
                  414849831,
                  -24555660,
                  -761228422,
                  -2123247866,
                  -1396171135,
                  -331629636,
                  -1690583322,
                  -868477859,
                  -1153011148
               }
            )
         );
         if (contentTopic.isBlank()) {
            contentTopic = init_Str_JsonObject_Str(
               b, autismclient.e.DataUtil.init(new int[]{-1798794673, -1798794693, 71940093, -2064827712, 414849850, -24555662})
            );
         }

         autismclient.util.mm.a.ConfigStore key = autismclient.util.mm.a.ConfigStore.init(
            lobbyId,
            contentTopic,
            init_Str_JsonObject_Str(
               b,
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794671,
                     -1798794702,
                     71940031,
                     -2064825764,
                     414909541,
                     -22708639,
                     -703970243,
                     -348240488,
                     2089449671,
                     348428730,
                     -2083610460,
                     -167413281,
                     -894845578
                  }
               )
            ),
            init_Str_JsonObject_Str(
               b,
               autismclient.e.DataUtil.init(
                  new int[]{-1798794671, -1798794718, 71940009, -2064825791, 414909509, -22708612, -703970270, -348240483, 2089449712}
               )
            ),
            isPublic
         );
         boolean announcement = b.has(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794669,
                     -1798794702,
                     71940192,
                     -2064823846,
                     414968992,
                     -20861600,
                     -646711810,
                     1426766869,
                     1280102964,
                     1028487098,
                     1818329975,
                     533651286,
                     -636680281
                  }
               )
            )
            && b.get(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794669,
                        -1798794702,
                        71940192,
                        -2064823846,
                        414968992,
                        -20861600,
                        -646711810,
                        1426766869,
                        1280102964,
                        1028487098,
                        1818329975,
                        533651286,
                        -636680281
                     }
                  )
               )
               .getAsBoolean();
         String ownerDid = init_Str_JsonObject_Str(
            b,
            autismclient.e.DataUtil.init(new int[]{-1798794667, -1798794694, 71940155, -2064821928, 415028712, -19014555, -589453686, -1093193071, 470756459})
         );
         int speakerVersion = init_I_JsonObject_Str(
            b,
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794667,
                  -1798794714,
                  71940156,
                  -2064821933,
                  415028716,
                  -19014532,
                  -589453653,
                  -1093193078,
                  470756441,
                  1708545468,
                  1425302818,
                  1234715849,
                  -378514696,
                  1150943220,
                  1319498684
               }
            )
         );
         boolean canSend = !announcement
            || b.has(autismclient.e.DataUtil.init(new int[]{-1798794665, -1798794700, 71940331, -2064820010, 415088152, -17167492, -532195230, 681814302}))
               && b.get(autismclient.e.DataUtil.init(new int[]{-1798794665, -1798794700, 71940331, -2064820010, 415088152, -17167492, -532195230, 681814302}))
                  .getAsBoolean();
         this.init(
            key,
            init_Str_JsonObject_Str(b, autismclient.e.DataUtil.init(new int[]{-1798794663, -1798794697, 71940265, -2064818089, 415147884})),
            isPublic,
            cap,
            isHost,
            init_Str_JsonObject_Str(
               b, autismclient.e.DataUtil.init(new int[]{-1798794663, -1798794710, 71940269, -2064818104, 415147903, -15320450, -474937032})
            ),
            init_Str_JsonObject_Str(
               b, autismclient.e.DataUtil.init(new int[]{-1798794661, -1798794709, 71940458, -2064816183, 415207328, -13473420, -417678618, -63138291})
            ),
            hostFp,
            shareCode,
            term,
            announcement,
            ownerDid,
            speakerVersion,
            canSend
         );
         this.init_V_JsonObject(b);
         this.prev();
         if (isHost) {
            this.run(
               (
                     isPublic
                        ? autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794661,
                              -1798794728,
                              71940468,
                              -2064816167,
                              415207334,
                              -13473431,
                              -417678611,
                              -63138278,
                              -1957283479,
                              -546246737,
                              246221951,
                              -957057758,
                              395981479,
                              -609474872,
                              -1713852945,
                              -1589836766,
                              -2040300511,
                              1175194892,
                              2071306092,
                              -214023514,
                              1955206582,
                              481862661,
                              2052841650
                           }
                        )
                        : autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794659,
                              -1798794722,
                              71940406,
                              -2064814245,
                              415267044,
                              -11626389,
                              -360420445,
                              1711869284,
                              1528337191,
                              133811601,
                              -146805190,
                              -255993173,
                              654147071,
                              -1196280894,
                              1569996990,
                              1425266913,
                              1233598507,
                              -413153015,
                              77161251,
                              -1902967260,
                              1137558255,
                              904564574,
                              -2023270930,
                              1703109674
                           }
                        )
                  )
                  + init_Str_JsonObject_Str(b, autismclient.e.DataUtil.init(new int[]{-1798794659, -1798794701, 71940389, -2064814253, 415267040}))
                  + "\"  ("
                  + (
                     isPublic
                        ? autismclient.e.DataUtil.init(new int[]{-1798794657, -1798794692, 71940589, -2064812380, 415326502, -9779429, -303162076})
                        : autismclient.e.DataUtil.init(
                           new int[]{-1798794657, -1798794698, 71940588, -2064812362, 415326506, -9779371, -303162015, -808090696, 718990565}
                        )
                  )
                  + shareCode
                  + ")"
            );
         } else {
            this.run(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794655,
                     -1798794709,
                     71940527,
                     -2064810453,
                     415386223,
                     -7932346,
                     -245903834,
                     966916644,
                     -90355985,
                     1493928330,
                     -932859226,
                     1146135972,
                     1170477948
                  }
               )
            );
         }

         this.init_BufferHelper_Strea = BufferHelper.Inner_StreamUtil.read;
         autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
         if (rm != null) {
            rm.init_V();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .warn(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794655,
                     -1798794752,
                     71940528,
                     -2064810446,
                     415386221,
                     -7932326,
                     -245903774,
                     966916727,
                     -90355994,
                     1493928343,
                     -932859214,
                     1146135971,
                     1170477943,
                     1925074311,
                     -452237526,
                     -1134460441,
                     -808538139,
                     705118475,
                     383839153,
                     -985887382,
                     -497738641,
                     1749968202,
                     -1585561565,
                     -1907766944,
                     988764398,
                     586922121
                  }
               ),
               t
            );
         this.pop(
            isHost
               ? autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794653,
                     -1798794752,
                     71940492,
                     -2064808543,
                     415445726,
                     -6085295,
                     -188645403,
                     -1553043239,
                     -899702745,
                     -2120980602,
                     -1325886101,
                     1847200548,
                     1428643238,
                     1338268365
                  }
               )
               : autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794653,
                     -1798794743,
                     71940497,
                     -2064808531,
                     415445713,
                     -6085254,
                     -188645402,
                     -1553043225,
                     -899702744,
                     -2120980597,
                     -1325886105,
                     1847200556
                  }
               )
         );
      }
   }

   private synchronized void init(
      autismclient.util.mm.a.ConfigStore key,
      String name,
      boolean isPublic,
      int maxPlayers,
      boolean isHost,
      String server,
      String plugins,
      String hostFpHex,
      String shareCode,
      int hostTermInit,
      boolean announcement,
      String ownerDid,
      int speakerVersion,
      boolean canSend
   ) {
      if (this.put_Z()) {
         this.close_V();
         autismclient.util.mm.c.CachePool rm = this.init_CachePool();
         this.init_DataUtil = new DataUtil(key, name, isPublic, maxPlayers, server, plugins, shareCode, announcement, ownerDid, speakerVersion, canSend);
         this.write_Z = false;
         this.init_BufferHelper_2 = new autismclient.util.mm.a.BufferHelper();
         this.init_Map.clear();
         this.init_Set.clear();
         this.write_Map.clear();
         this.write_Set.clear();
         this.read_Map.clear();
         this.size = 0L;
         this.data = 0L;
         this.close_I = hostTermInit;
         this.update_Str = hostFpHex;
         this.valid = 0L;
         this.push_Z = false;
         this.push_J = 0L;
         this.flush_J = 0L;
         this.init_V_CachePool_ConfigStore(rm, key);
         this.tick = System.currentTimeMillis();
         this.cache = this.tick;
         this.load_Z = !isHost;
      }
   }

   private void init_V_CachePool_ConfigStore(autismclient.util.mm.c.CachePool rm, autismclient.util.mm.a.ConfigStore key) {
      rm.init_V_Str_Consumer(key.read(), this::init_V_BArr);
      rm.init_V_Str_Consumer(key.write(), this::load_V_BArr);
      rm.load(key.update(), this::read_V_BArr);
   }

   public synchronized void close_V() {
      this.init_V_Z_Str(
         true,
         autismclient.e.DataUtil.init(
            new int[]{-1798794651, -1798794711, 71940697, -2064806624, 415505161, -4238329, -131387182, 221964135, -1709049443, -1440922229, -1718913223}
         )
      );
   }

   private void init_V_Z_Str(boolean notifyServer, String message) {
      DataUtil lb = this.init_DataUtil;
      if (lb != null) {
         if (notifyServer) {
            this.tick(lb.init_Str);
         }

         if (this.init_CachePool_2 != null) {
            this.init_CachePool_2.init(lb.init());
            this.init_CachePool_2.init(lb.load());
            this.init_CachePool_2.load(lb.read());
         }

         if (message != null && !message.isBlank()) {
            this.run(message);
         }

         this.init_DataUtil = null;
         this.init_BufferHelper_2 = null;
         this.init_Map.clear();
         this.init_Set.clear();
         this.write_Map.clear();
         this.write_Set.clear();
         this.read_Map.clear();
         this.size = 0L;
         this.data = 0L;
         this.close_I = 0;
         this.update_Str = autismclient.e.DataUtil.init(new int[]{-1798794651});
         this.init_BArr = null;
         this.read_List = List.of();
         this.update_I = 0;
         this.valid = 0L;
         this.push_Z = false;
         this.write_List = List.of();
         this.init_List = List.of();
         this.close_J = 0L;
         this.cache = 0L;
         this.load_Z = false;
         this.read_Z = false;
         this.load_Map.clear();
         this.add_V();
      }
   }

   public synchronized void open_V() {
      this.load_Map.clear();
      this.init_Z = true;
      this.put_V();
      this.init_CachePool();
      this.clear_V();
   }

   public void poll_V() {
      if (this.init_AtomicBoolean.compareAndSet(false, true)) {
         DataUtil lb = this.init_DataUtil;
         String jwt = L.pop();
         if (lb != null && jwt != null && !jwt.isBlank()) {
            try {
               JsonObject body = new JsonObject();
               body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794649, -1798794739, 71940621, -2064804676}), jwt);
               body.addProperty(
                  autismclient.e.DataUtil.init(new int[]{-1798794649, -1798794741, 71940629, -2064804694, 415564889, -2391216, -74128971, 1996971502}),
                  lb.init_Str
               );
               init_AutismHttp_DataUti_Str(
                  autismclient.e.DataUtil.init(new int[]{-1798794647, -1798794682, 71940820, -2064802769, 415624344, -544163, -16870561}), body, 2000
               );
            } catch (Throwable var6) {
            }
         }

         autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
         if (rm != null) {
            try {
               rm.load();
            } catch (Throwable var5) {
            }
         }
      }
   }

   private void tick(String lobbyId) {
      String jwt = L.pop();
      this.init_V_Runnable(
         () -> {
            if (jwt != null && !jwt.isBlank() && lobbyId != null) {
               JsonObject body = new JsonObject();
               body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794565, -1798794543, 71943377, -2064723864}), jwt);
               body.addProperty(
                  autismclient.e.DataUtil.init(new int[]{-1798794563, -1798794543, 71943307, -2064721924, 418126919, 77031430, -1906989265, 1012876548}),
                  lobbyId
               );
               AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
                  autismclient.e.DataUtil.init(new int[]{-1798794563, -1798794606, 71943304, -2064721925, 418126916, 77031433, -1906989309}), body
               );
               if (r.init_Z()
                  && r.init_JsonObject() != null
                  && r.init_JsonObject().has(autismclient.e.DataUtil.init(new int[]{-1798794561, -1798794539, 71943509, -2064720044}))
                  && r.init_JsonObject().has(autismclient.e.DataUtil.init(new int[]{-1798794561, -1798794534, 71943514, -2064720048}))) {
                  L.init_V(
                     r.init_JsonObject().get(autismclient.e.DataUtil.init(new int[]{-1798794559, -1798794581, 71943447, -2064718122})).getAsString(),
                     r.init_JsonObject().get(autismclient.e.DataUtil.init(new int[]{-1798794559, -1798794588, 71943448, -2064718126})).getAsLong()
                  );
               }
            }
         }
      );
   }

   private boolean get_Z() {
      if (this.init_EventBus.write_Z()) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794647,
                  -1798794718,
                  71940817,
                  -2064802778,
                  415624341,
                  -544245,
                  -16870583,
                  -522988421,
                  967224594,
                  -80805479,
                  1790000095,
                  -344573018,
                  -2091827747,
                  -422149696,
                  -201739184,
                  -1958947906,
                  -597844385,
                  -1353305822,
                  997194920,
                  848272179,
                  526634867,
                  -854191782,
                  -710142676,
                  -539585677,
                  452710307,
                  1149116570,
                  1262878114,
                  494516912,
                  -1849843669
               }
            ),
            -14249
         );
         return true;
      } else {
         return aD.read_Z();
      }
   }

   private boolean init_Z_UiHelper_BArr(UiHelper type, byte[] payload) {
      DataUtil lb = this.init_DataUtil;
      autismclient.util.mm.a.BufferHelper se = this.init_BufferHelper_2;
      if (lb == null || se == null) {
         return false;
      }

      if (type.read() && !this.read_Z()) {
         return false;
      }

      if (!this.init_TextFormat.init_Z_UiHelper_I(type, payload.length)) {
         return false;
      }

      try {
         byte[] key = this.init_BArr;
         if (key == null) {
            return false;
         }

         byte[] frame = SyncManager.init_BArr_CachePool(this.init_CachePool, se, lb.init_ConfigStore, key, type, payload);
         String topic = type.load() ? lb.load() : lb.init();
         boolean published = this.init_CachePool_2.init_Z_Str(topic, frame, type.write());
         if (!published) {
            AutismClientAddon.LOG
               .warn(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794645,
                        -1798794746,
                        71940763,
                        -2064800788,
                        415684053,
                        1302879,
                        40387607,
                        1252018927,
                        157878109,
                        599252876,
                        1396972955,
                        356491555,
                        -1833662257,
                        -1008955759,
                        -1212856552,
                        1056155744,
                        -1618912686,
                        1353313539,
                        -996949874,
                        -840671567,
                        -291013669,
                        -431490033,
                        -491287904,
                        1949943277,
                        318696764,
                        1289665239,
                        1324917860,
                        -1877222358,
                        1935650466,
                        -124376620,
                        439291361,
                        733127482,
                        1252114481,
                        160843796,
                        691193705,
                        -47834628,
                        -1482874453,
                        1275533148,
                        886821114,
                        1721647353,
                        1831457580,
                        940611039,
                        -905827732,
                        1984108303,
                        1377817785,
                        -237320991,
                        1232983038,
                        -432234937,
                        -514382303,
                        1234019298,
                        -400110217,
                        481481879,
                        2041038831,
                        -1152306579,
                        -1361766677,
                        734903259,
                        1307162993,
                        1867346277,
                        2053156971,
                        -776646116,
                        1693776566,
                        967466938,
                        -73295427
                     }
                  ),
                  type,
                  frame.length
               );
         } else if (this.clear_Z() && init_Z_UiHelper(type)) {
            String id = SyncManager.load(frame);
            if (id != null) {
               this.read_Map.put(id, new BufferHelper.Inner_ConfigStore(type.name(), System.currentTimeMillis()));
            }
         }

         return published;
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .debug(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794645,
                     -1798794746,
                     71940763,
                     -2064800788,
                     415684053,
                     1302879,
                     40387607,
                     1252018927,
                     157878109,
                     599252876,
                     1396972955,
                     356491555,
                     -1833662257,
                     -1008955759,
                     -1212856550,
                     1056155763,
                     -1618912684,
                     1353313567,
                     -996949861,
                     -840671568
                  }
               ),
               t
            );
         return false;
      }
   }

   private boolean set_Z() {
      long now = System.currentTimeMillis();
      if (now - this.queue < 1000L) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794643,
                  -1798794690,
                  71940952,
                  -2064798943,
                  415743490,
                  3149839,
                  97646034,
                  -1267940993,
                  -651468672,
                  1279311263,
                  1003946006,
                  1057556210,
                  -1575496888,
                  -1595761773,
                  2070993623,
                  -223707919,
                  1654986120,
                  -235034362,
                  1303872861,
                  1765351985,
                  -1108662248,
                  -8788201,
                  -272433202,
                  144504957,
                  184683149,
                  1430213849,
                  1386957363,
                  46005695,
                  1426177506,
                  1261826772,
                  461923500,
                  1434727416,
                  1526874157
               }
            ),
            -14249
         );
         return false;
      } else {
         this.queue = now;
         return true;
      }
   }

   private boolean put_Z() {
      if (L.load_Z()) {
         return true;
      }

      bi.init(
         autismclient.e.DataUtil.init(
            new int[]{
               -1798794643,
               -1798794690,
               71940957,
               -2064798935,
               415743515,
               3149839,
               97646047,
               -1267940994,
               -651468585,
               1279311238,
               1003946065,
               1057556134,
               -1575496943,
               -1595761773,
               2070993662,
               -223707907,
               1654986120,
               -235034346,
               1303872851,
               1765351972,
               -1108662247,
               -8788201,
               -272433206,
               144504951,
               184683231,
               1430213772,
               1386957363,
               46005695,
               1426177441,
               1261826774,
               461923491,
               1434727400,
               1526874208,
               88457237,
               -1552789719,
               -891840193,
               -1877241106,
               1935064662,
               -142538328,
               -123719865,
               459650345
            }
         ),
         -14249
      );
      return false;
   }

   public synchronized void load_V_Str(String text) {
      if (text != null && !text.isBlank() && this.init_DataUtil != null) {
         if (this.init_EventBus.write_Z()) {
            bi.init(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794641,
                     -1798794716,
                     71940891,
                     -2064796996,
                     415803231,
                     4996881,
                     154904199,
                     507066341,
                     -1460815140,
                     1959369607,
                     610918933,
                     1758620732,
                     -1317331433,
                     2112399578,
                     1059876246,
                     -1503571656,
                     633917849,
                     -1823382236,
                     -690271969,
                     76408246,
                     -1926310561,
                     413913680,
                     -53578606,
                     -1660933379,
                     50669725,
                     1570762388,
                     1448996888,
                     1969233722,
                     916704273
                  }
               ),
               -14249
            );
         } else if (this.set_Z()) {
            String trimmed = text.length() > 1024 ? text.substring(0, 1024) : text;
            if (this.load(UiHelper.load, new autismclient.util.mm.b.DataUtil.StreamUtil(trimmed).init_BArr())) {
               this.init_V_JsonUtil(new JsonUtil(this.init_Str, this.poll_Str(), trimmed, true, false));
            }
         }
      }
   }

   private boolean load(UiHelper type, byte[] payload) {
      if (!this.read_Z()) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794641,
                  -1798794720,
                  71940892,
                  -2064796996,
                  415803210,
                  4996881,
                  154904213,
                  507066358,
                  -1460815144,
                  1959369626,
                  610918936,
                  1758620711,
                  -1317331433,
                  2112399572,
                  1059876246,
                  -1503571598,
                  633917849,
                  -1823382250,
                  -690271990,
                  76408232,
                  -1926310583,
                  413913686,
                  -53578614,
                  -1660933377,
                  50669785,
                  1570762459,
                  1448996877,
                  1969233708,
                  916704346,
                  -1646937124,
                  484555883,
                  2136327291,
                  1801633715,
                  16070668,
                  498194338,
                  -1735845757,
                  2023359266,
                  -1700371153,
                  -1171897564,
                  -1969087023,
                  -912157024,
                  1787904621,
                  -409534750,
                  189321796,
                  1574009391,
                  1549651616,
                  794561146,
                  -1138411709,
                  -931025433
               }
            ),
            -14249
         );
         return false;
      }

      if (this.init_Z_UiHelper_BArr(type, payload)) {
         return true;
      }

      if (this.init_BArr == null) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794639,
                  -1798794693,
                  71941087,
                  -2064795077,
                  415862687,
                  6843994,
                  212162652,
                  -2012893581,
                  2024805459,
                  -1655539303,
                  217892059,
                  -1835281996,
                  -1059166057,
                  1525593550,
                  48759064,
                  1511531960,
                  -387150557,
                  883237131,
                  1610550721,
                  -1612535430,
                  1551008152,
                  836615516,
                  165276251,
                  828595573,
                  -83343851,
                  1711311059
               }
            ),
            -14249
         );
      } else if (!this.add_Z()) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794639,
                  -1798794689,
                  71941087,
                  -2064795098,
                  415862737,
                  6843984,
                  212162653,
                  -2012893574,
                  2024805405,
                  -1655539312,
                  217892055,
                  -1835282014,
                  -1059166064,
                  1525593555,
                  48759064,
                  1511531960,
                  -387150557,
                  883237131,
                  1610550721,
                  -1612535430,
                  1551008152,
                  836615516,
                  165276251,
                  828595573,
                  -83343851,
                  1711311059
               }
            ),
            -14249
         );
      } else {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794637,
                  -1798794704,
                  71940993,
                  -2064793183,
                  415922371,
                  8691025,
                  269420830,
                  -237886287,
                  1215458885,
                  -975480873,
                  -175135103,
                  -1134217411,
                  -801000483,
                  938787549,
                  -962358182,
                  231668282,
                  -1408218783,
                  -705110775,
                  -383594097,
                  993488124,
                  733359830,
                  1259317338,
                  384130841,
                  -976842761,
                  -217357225,
                  1851859665
               }
            ),
            -2075846
         );
      }

      return false;
   }

   private boolean add_Z() {
      autismclient.util.mm.c.CachePool rm = this.init_CachePool_2;
      if (rm == null) {
         return false;
      }

      for (autismclient.util.mm.c.BufferHelper s : rm.init_List()) {
         if (s.init_Z) {
            return true;
         }
      }

      return false;
   }

   public synchronized void init_V_DataUtil_NetHelper(autismclient.util.mm.b.DataUtil.NetHelper m) {
      if (m != null && this.init_DataUtil != null && !this.get_Z() && this.set_Z() && this.load(UiHelper.read, m.init_BArr())) {
         this.init_V_JsonUtil(JsonUtil.init(this.init_Str, this.poll_Str(), true, m));
      }
   }

   public synchronized void init_V_DataUtil_StateCach(autismclient.util.mm.b.DataUtil.StateCache p) {
      if (p != null && this.init_DataUtil != null && !this.get_Z() && this.set_Z() && this.load(UiHelper.update, p.init_BArr())) {
         this.init_V_JsonUtil(JsonUtil.init(this.init_Str, this.poll_Str(), true, p));
      }
   }

   public synchronized boolean init_Z_DataUtil_DataUtil(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if (b == null || this.init_DataUtil == null || this.get_Z()) {
         return false;
      }

      if (!this.set_Z()) {
         return false;
      }

      byte[] payload = b.init_BArr();
      if (!this.init_TextFormat.init_Z_UiHelper_I(UiHelper.close, payload.length)) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794637,
                  -1798794720,
                  71940998,
                  -2064793163,
                  415922397,
                  8691024,
                  269420880,
                  -237886238,
                  1215458910,
                  -975480936,
                  -175135022,
                  -1134217420,
                  -801000494,
                  938787531,
                  -962358253,
                  231668351,
                  -1408218859,
                  -705110769,
                  -383594087,
                  993488124,
                  733359812,
                  1259317336,
                  384130838,
                  -976842758,
                  -217357289
               }
            ),
            -2075846
         );
         return false;
      }

      if (!this.load(UiHelper.close, payload)) {
         return false;
      }

      this.init_V_JsonUtil(JsonUtil.init(this.init_Str, this.poll_Str(), true, b));
      return true;
   }

   public synchronized void read_V_Str(String typedLine) {
      if (typedLine != null && !typedLine.isBlank() && this.init_DataUtil != null && !this.get_Z() && this.set_Z()) {
         autismclient.util.mm.b.DataUtil.CachePool offer = init_DataUtil_CachePool_Str(typedLine.trim());
         if (this.load(UiHelper.write, offer.init_BArr())) {
            this.init_V_JsonUtil(JsonUtil.init(this.init_Str, this.poll_Str(), true, offer));
         }
      }
   }

   public static autismclient.util.mm.b.DataUtil.CachePool init_DataUtil_CachePool_Str(String line) {
      if (autismclient.commands.DataUtil.init_Z(line)) {
         return new autismclient.util.mm.b.DataUtil.CachePool((byte)1, autismclient.commands.DataUtil.init_Str(line));
      } else {
         return line.startsWith(autismclient.e.DataUtil.init(new int[]{-1798794635, -1798794662}))
            ? new autismclient.util.mm.b.DataUtil.CachePool((byte)0, line.substring(1).trim())
            : new autismclient.util.mm.b.DataUtil.CachePool((byte)2, line);
      }
   }

   public static String init_Str_DataUtil_CachePool(autismclient.util.mm.b.DataUtil.CachePool offer) {
      if (offer == null) {
         return autismclient.e.DataUtil.init(new int[]{-1798794635});
      }

      String var10000;
      switch (offer.write) {
         case 0:
            var10000 = "/" + offer.init_Str;
            break;
         case 1:
            var10000 = autismclient.commands.DataUtil.init_Str() + offer.init_Str;
            break;
         default:
            var10000 = offer.init_Str;
      }

      return var10000;
   }

   public void init_V_DataUtil_CachePool(autismclient.util.mm.b.DataUtil.CachePool offer) {
      if (offer != null && !this.init_EventBus.write_Z()) {
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         switch (offer.write) {
            case 0:
               if (conn != null) {
                  conn.sendCommand(offer.init_Str);
               }
               break;
            case 1:
               if (read_Z_Str(offer.init_Str)) {
                  bi.init(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794633,
                           -1798794699,
                           71941126,
                           -2064789321,
                           416041544,
                           12385106,
                           383937417,
                           -982838786,
                           -403234409,
                           384635867,
                           -961189091,
                           267911732,
                           -284669618,
                           -234824497,
                           1310374549,
                           1966908282,
                           844611729,
                           413160732,
                           -76916195,
                           1910567821,
                           -901937058,
                           2104720928,
                           821840282,
                           -292752186,
                           -485384250,
                           2132956963,
                           1697155349,
                           1072211205,
                           -1121188007,
                           -397090846,
                           575084684,
                           647759366,
                           -1394295078,
                           -273475345,
                           112195611,
                           -816900474,
                           445892188,
                           937754924,
                           -994367524,
                           -760621101,
                           -2104418862,
                           -812475778,
                           583051020,
                           894715980,
                           1966392655,
                           828629695,
                           -82282651,
                           1744202056,
                           -1764311896,
                           1140906999,
                           1008375458,
                           1194867103,
                           -1613822927,
                           1511097457,
                           -400619936,
                           465681115,
                           1551211446,
                           842915186,
                           360564196,
                           -1707414760,
                           -1390247134,
                           -147986955,
                           -292629128,
                           -481571772,
                           -2043823450,
                           1065983715,
                           -1314245393,
                           -2086904677,
                           -269532609,
                           234424112,
                           -1322785693,
                           1943313876,
                           113187262,
                           -786161611
                        }
                     ),
                     -42149
                  );
                  return;
               }

               autismclient.commands.DataUtil.read(offer.init_Str);
               break;
            default:
               if (conn != null) {
                  conn.sendChat(offer.init_Str);
               }
         }
      }
   }

   private static boolean read_Z_Str(String body) {
      if (body == null) {
         return false;
      }

      String norm = body.trim();

      while (norm.startsWith(autismclient.e.DataUtil.init(new int[]{-1798794633, -1798794664}))) {
         norm = norm.substring(1).trim();
      }

      if (norm.isEmpty()) {
         return false;
      }

      String[] parts = norm.split(autismclient.e.DataUtil.init(new int[]{-1798794631, -1798794715, 71941339, -2064787343}));
      String head = parts[0].toLowerCase(Locale.ROOT);
      if (head.equals(autismclient.e.DataUtil.init(new int[]{-1798794631, -1798794743, 71941338, -2064787393, 416101007, 14232146, 441195858}))) {
         return true;
      }

      if (head.equals(autismclient.e.DataUtil.init(new int[]{-1798794629, -1798794737, 71941257, -2064785477, 416160704, 16079185, 498454029}))
         || head.equals(autismclient.e.DataUtil.init(new int[]{-1798794629, -1798794730, 71941257, -2064785480, 416160722, 16079185, 498454029}))) {
         for (int i = 1; i < parts.length; i++) {
            if (aD.init(parts[i])) {
               return true;
            }
         }
      }

      return false;
   }

   public void write_V(String name) {
      String n = name == null ? autismclient.e.DataUtil.init(new int[]{-1798794627}) : name.trim();
      if (!n.matches(
         autismclient.e.DataUtil.init(
            new int[]{
               -1798794627,
               -1798794714,
               71941477,
               -2064783501,
               416220223,
               17926238,
               555712395,
               47215962,
               1463693015,
               -1870156244,
               -2140270063,
               -1923861827,
               489826612,
               -1995242568,
               -1722977125,
               -1872682872,
               2076373978,
               -56915533,
               -1764382895
            }
         )
      )) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794625,
                  -1798794692,
                  71941379,
                  -2064781682,
                  416279812,
                  19773237,
                  612970692,
                  1822223350,
                  654346485,
                  -1190097854,
                  1761670236,
                  -1222797244,
                  747992147,
                  1712918701,
                  1560872841,
                  1142420818,
                  1055305609,
                  -1645263369,
                  536439558,
                  -550239863,
                  122436434,
                  -499439060,
                  1697259166,
                  1075429005,
                  -1021438242,
                  -1599815893,
                  1945313792,
                  175188749,
                  1135886914,
                  852755368,
                  665613776,
                  -840808377,
                  -295256638,
                  -563021599,
                  -273802923,
                  102044804,
                  -1131575021,
                  -719086288,
                  -816837420,
                  447844804,
                  998286548,
                  882111103,
                  1575637023,
                  1600110096,
                  -1936191465,
                  107607704,
                  -959126639,
                  331848536,
                  1697369052,
                  1078832116,
                  -915945720,
                  1670457241,
                  244569949,
                  -1008267219,
                  -1191511332,
                  1717855439,
                  1713910478,
                  1591616894,
                  2095480332,
                  535384338,
                  -582951330,
                  -891622992,
                  -1870507419,
                  2143809040
               }
            ),
            -14249
         );
      } else {
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         if (conn != null) {
            conn.sendCommand("tpa " + n);
         }
      }
   }

   public void update(String name) {
      String n = name == null ? autismclient.e.DataUtil.init(new int[]{-1798794625}) : name.trim();
      if (!n.matches(
         autismclient.e.DataUtil.init(
            new int[]{
               -1798794623,
               -1798794534,
               71941601,
               -2064779697,
               416339387,
               21620258,
               670229007,
               -697736610,
               -155000237,
               -510039512,
               1368643229,
               -521732679,
               1006157496,
               1126112700,
               549755671,
               -137442940,
               34237270,
               1061355967,
               -1457705003
            }
         )
      )) {
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794623,
                  -1798794558,
                  71941569,
                  -2064779764,
                  416339398,
                  21620279,
                  670228994,
                  -697736624,
                  -155000303,
                  -510039452,
                  1368643264,
                  -521732733,
                  1006157535,
                  1126112743,
                  549755730,
                  -137442880,
                  34237190,
                  1061356029,
                  -1457705080,
                  2055783706,
                  -695212155,
                  -76737238,
                  1916114003,
                  -730009271,
                  -1155451879,
                  -1459267286,
                  2007353567,
                  2098416846,
                  626413699,
                  -2056008530,
                  688246083,
                  -139208491,
                  -20497073,
                  -635408136,
                  1777181123,
                  -741960676,
                  -1525941802,
                  -59554777,
                  -1846196718,
                  -1397522347,
                  -373520813,
                  1305757795,
                  1823783637,
                  702716826,
                  309387932,
                  1001094067,
                  969146134,
                  -21239819,
                  -658436322,
                  1063313399,
                  -1397025827,
                  -358128871,
                  1782909835,
                  -564366534,
                  -315492260,
                  -1190326456,
                  1754585179,
                  -1442433176,
                  -1765757745,
                  1096084123,
                  -381127537,
                  1069951665,
                  -1191235233,
                  1726412461,
                  1979179290,
                  1225019023
               }
            ),
            -14249
         );
      } else {
         ClientPacketListener conn = Minecraft.getInstance().getConnection();
         if (conn != null) {
            conn.sendCommand("trade " + n);
         }
      }
   }

   private void prev() {
      autismclient.util.mm.a.BufferHelper se = this.init_BufferHelper_2;
      if (se != null) {
         String nick = this.open_Str();
         this.read_Str = nick;
         autismclient.util.mm.b.DataUtil.SyncManager p = new autismclient.util.mm.b.DataUtil.SyncManager(nick);
         boolean ks = this.init_EventBus.write_Z();
         boolean mayShare = this.read_Z();
         boolean shareServer = this.sync_Z();
         boolean shareLocation = this.flush_Z();
         if (!ks && mayShare && shareServer) {
            try {
               ServerData sd = Minecraft.getInstance().getCurrentServer();
               if (sd != null && sd.ip != null && !sd.ip.isBlank()) {
                  p.init_Z = true;
                  p.read = sd.ip;
                  p.load_Str = sd.name == null ? sd.ip : sd.name;
               }
            } catch (Throwable var9) {
            }
         }

         if (!ks && mayShare && shareLocation) {
            p.load_Z = true;
         }

         p.init_I = mayShare ? this.init_EventBus.load_I() : -1;
         p.write = L.open();
         this.init_Z_UiHelper_BArr(UiHelper.init_UiHelper, p.init_BArr());
         if (!ks && mayShare && shareLocation) {
            this.valid();
         }
      }
   }

   private void valid() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null) {
            return;
         }

         String dim = mc.player.level().dimension().identifier().toString();
         autismclient.util.mm.b.DataUtil.TaskQueue loc = new autismclient.util.mm.b.DataUtil.TaskQueue(
            dim, mc.player.getX(), mc.player.getY(), mc.player.getZ()
         );
         this.init_Z_UiHelper_BArr(UiHelper.sync, loc.init_BArr());
      } catch (Throwable var4) {
      }
   }

   private void init_V_BArr(byte[] frame) {
      this.init_V_BArr_Z(frame, false);
   }

   private void load_V_BArr(byte[] frame) {
      this.init_V_BArr_Z(frame, true);
   }

   private synchronized void init_V_BArr_Z(byte[] frame, boolean controlTopic) {
      try {
         DataUtil lb = this.init_DataUtil;
         if (lb == null) {
            return;
         }

         String fp = SyncManager.init_Str_BArr(frame);
         if (fp == null) {
            return;
         }

         UiHelper peeked = UiHelper.init(SyncManager.init_I_BArr(frame));
         if (peeked == null || peeked.load() != controlTopic) {
            return;
         }

         if (fp.equals(this.init_Str)) {
            this.write(frame);
            return;
         }

         if (this.init_Set.contains(fp)) {
            return;
         }

         if (!this.init_TextFormat.init()) {
            return;
         }

         boolean content = peeked != null && !peeked.init();
         if (content && (this.write_Set.contains(fp) || this.init_EventBus.read_Z(fp))) {
            return;
         }

         byte[] k1 = this.init_BArr;
         if (k1 == null) {
            this.data();
            return;
         }

         SyncManager env = SyncManager.init_SyncManager_BArr_BArr(frame, k1);
         if (env == null) {
            for (BufferHelper.Inner_CachePool o : this.read_List) {
               env = SyncManager.init_SyncManager_BArr_BArr(frame, o.init_BArr());
               if (env != null) {
                  break;
               }
            }
         }

         if (env == null) {
            this.init_TextFormat.update.incrementAndGet();
            this.data();
            return;
         }

         if (!this.init_TextFormat.init_Z_SyncManager(env)) {
            return;
         }

         UiHelper type = env.init_UiHelper();
         if (type == null) {
            return;
         }

         if (type.load() != controlTopic) {
            return;
         }

         if (type.read() && !this.write_Z(fp)) {
            return;
         }

         if (!type.init() && this.init_EventBus.write_Z()) {
            return;
         }

         switch (type) {
            case init_UiHelper:
               this.read_V_SyncManager(env);
               break;
            case load:
               this.write(env);
               break;
            case read:
               this.update(env);
               break;
            case write:
               this.sync(env);
               break;
            case update:
               this.flush(env);
               break;
            case close:
               this.close(env);
               break;
            case sync:
               this.open(env);
               break;
            case flush:
               this.poll(env);
               break;
            case open:
               this.push(env);
               break;
            case poll:
               this.init_V_SyncManager(env);
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .debug(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794621,
                     -1798794514,
                     71941555,
                     -2064777788,
                     416399094,
                     23467307,
                     727487234,
                     1077270729,
                     -964346796,
                     170018921,
                     975616134,
                     179331912,
                     1264323019,
                     539306664,
                     -461361654,
                     -1417306546,
                     -986831031,
                     -526991890,
                     843117460,
                     366840012,
                     -1512860478,
                     345964607,
                     2134968602,
                     1759519681,
                     -1289465253
                  }
               ),
               t
            );
      }
   }

   private boolean write_Z(String fp) {
      DataUtil lb = this.init_DataUtil;
      if (lb != null && lb.load_Z) {
         for (BufferHelper.Inner_BufferHelper e : this.write_List) {
            if (e.load_Z() && fp.equalsIgnoreCase(e.load_Str())) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private synchronized void read_V_BArr(byte[] payload) {
      try {
         DataUtil lb = this.init_DataUtil;
         if (lb == null) {
            return;
         }

         PacketUtil ev = PacketUtil.init_PacketUtil_BArr(payload);
         if (ev == null) {
            return;
         }

         PublicKey serverKey = StateCache.init_PublicKey();
         if (serverKey == null) {
            this.data();
            return;
         }

         if (!ev.init_Z_PublicKey_Str(serverKey, lb.read())) {
            return;
         }

         long last = this.valid;
         if (ev.init_J <= last) {
            return;
         }

         if (ev.init_J > last + 1L) {
            this.data();
            return;
         }

         byte[] key = this.init_BArr_I(ev.init_I);
         PacketUtil.Inner_DataUtil body = key == null ? null : ev.init_PacketUtil_DataUti_BArr_Str(key, lb.read());
         if (body == null) {
            this.data();
            return;
         }

         this.valid = ev.init_J;
         this.init_V_PacketUtil_DataUti(body);
         if (ev.load_I > this.update_I) {
            this.data();
         }
      } catch (Throwable t) {
         AutismClientAddon.LOG
            .debug(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794621,
                     -1798794514,
                     71941555,
                     -2064777788,
                     416399084,
                     23467324,
                     727487251,
                     1077270662,
                     -964346812,
                     170018929,
                     975616135,
                     179331846,
                     1264323031,
                     539306729,
                     -461361663,
                     -1417306536,
                     -986831017,
                     -526991900,
                     843117460
                  }
               ),
               t
            );
      }
   }

   private void init_V_PacketUtil_DataUti(PacketUtil.Inner_DataUtil b) {
      String fp = b.read_Str() == null ? autismclient.e.DataUtil.init(new int[]{-1798794619}) : b.read_Str().toLowerCase(Locale.ROOT);
      boolean self = fp.equals(this.init_Str);
      String _kind = b.init_Str();
      if (_kind.equals(autismclient.e.DataUtil.init(new int[]{-1798794619, -1798794513, 71941747, -2064775921, 416458547}))) {
         if (self) {
            return;
         }

         if (!fp.isBlank() && b.load_Str() != null && !b.load_Str().isBlank()) {
            this.write_Map.put(fp, b.load_Str());
         }

         List<BufferHelper.Inner_BufferHelper> fresh = new ArrayList<>(this.write_List);
         if (fresh.removeIf(ex -> ex.init_Str().equals(b.load_Str()))) {
            fresh.add(new BufferHelper.Inner_BufferHelper(b.load_Str(), fp, b.write(), b.init_Z(), b.load_Z(), b.open()));
            this.write_List = fresh;
         } else {
            BufferHelper.Inner_BufferHelper e = new BufferHelper.Inner_BufferHelper(b.load_Str(), fp, b.write(), b.init_Z(), b.load_Z(), b.open());
            fresh.add(e);
            this.write_List = fresh;
            this.run(this.init_Str_BufferHelper_Buffe(e) + " joined");
         }

         this.close_J = 0L;
      } else if (_kind.equals(autismclient.e.DataUtil.init(new int[]{-1798794617, -1798794517, 71941695, -2064774007, 416518253, 27161388}))
         || _kind.equals(autismclient.e.DataUtil.init(new int[]{-1798794617, -1798794516, 71941683, -2064774005, 416518256}))) {
         boolean kick = b.init_Str().equals(autismclient.e.DataUtil.init(new int[]{-1798794611, -1798794522, 71942013, -2064768243, 416696894}));
         if (self) {
            if (kick) {
               this.size();
            }

            return;
         }

         BufferHelper.Inner_BufferHelper gone = null;
         List<BufferHelper.Inner_BufferHelper> fresh = new ArrayList<>(this.write_List);
         Iterator<BufferHelper.Inner_BufferHelper> it = fresh.iterator();

         while (it.hasNext()) {
            BufferHelper.Inner_BufferHelper e = it.next();
            if (e.init_Str().equals(b.load_Str())) {
               gone = e;
               it.remove();
            }
         }

         this.write_List = fresh;
         this.close_J = 0L;
         String name = gone != null
            ? this.init_Str_BufferHelper_Buffe(gone)
            : this.init_Str_BufferHelper_Buffe(new BufferHelper.Inner_BufferHelper(b.load_Str(), fp, b.write(), b.init_Z(), b.load_Z(), b.open()));
         if (!fp.isEmpty()) {
            if (kick) {
               this.init_Set.add(fp);
            }

            this.init_Map.remove(fp);
            this.write_Map.remove(fp);
            this.init_TextFormat.init_V_Str(fp);
         }

         this.run(kick ? name + " was removed by the host" : name + " left");
      } else if (_kind.equals(autismclient.e.DataUtil.init(new int[]{-1798794615, -1798794527, 71941879, -2064772071, 416577709}))) {
         this.init_V_Str_Integer(b.sync(), b.init_I());
      } else if (_kind.equals(
         autismclient.e.DataUtil.init(new int[]{-1798794615, -1798794502, 71941864, -2064772081, 416577720, 29008416, 899262335, 2107325534})
      )) {
         DataUtil lb = this.init_DataUtil;
         if (lb == null || !lb.load_Z || b.load_I() < lb.load_I) {
            return;
         }

         lb.load_I = b.load_I();
         List<BufferHelper.Inner_BufferHelper> fresh = new ArrayList<>(this.write_List.size());
         BufferHelper.Inner_BufferHelper changed = null;

         for (BufferHelper.Inner_BufferHelper e : this.write_List) {
            if (e.init_Str().equals(b.load_Str())) {
               changed = new BufferHelper.Inner_BufferHelper(e.init_Str(), e.load_Str(), e.read(), e.init_Z(), e.init_Z() || b.read_Z(), e.write());
               fresh.add(changed);
            } else {
               fresh.add(e);
            }
         }

         this.write_List = fresh;
         if (changed != null && !changed.load_Str().isBlank()) {
            RenderState peer = this.init_Map.get(changed.load_Str());
            if (peer != null) {
               peer.write_Z = changed.load_Z();
            }
         }

         boolean selfDid = b.load_Str().equals(L.push());
         if (selfDid && !b.read_Z()) {
            lb.read_Z = false;
         }

         String oldContent = lb.init();
         String oldControl = lb.load();
         boolean announcedRotation = !oldContent.equals(b.flush()) || !oldControl.equals(b.close());
         if (selfDid || announcedRotation) {
            this.read_Z = true;
         }

         this.init_V_Str(b.flush(), b.close(), lb.read());
         boolean rotated = !oldContent.equals(lb.init()) || !oldControl.equals(lb.load());
         if (selfDid || rotated) {
            this.data();
         }

         this.close_J = 0L;
         if (changed != null) {
            this.run(
               this.init_Str_BufferHelper_Buffe(changed)
                  + (
                     changed.load_Z()
                        ? autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794611,
                              -1798794579,
                              71942007,
                              -2064768241,
                              416696891,
                              32702575,
                              1013778917,
                              1362372949,
                              -716112711,
                              -724656523,
                              -989518792,
                              -610312548,
                              -1739817162,
                              1900243876
                           }
                        )
                        : autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794609,
                              -1798794577,
                              71941937,
                              -2064766319,
                              416756605,
                              34549617,
                              1071037114,
                              -1157586979,
                              -1525459275,
                              -44598145,
                              -1382545863,
                              90752026,
                              -1481651600,
                              1313437872,
                              2061869482,
                              -506554090,
                              1476693482,
                              -1467144718,
                              1763151156,
                              -1176887908
                           }
                        )
                  )
            );
         }
      } else if (_kind.equals(autismclient.e.DataUtil.init(new int[]{-1798794613, -1798794503, 71941817, -2064770176, 416637426}))) {
         List<BufferHelper.Inner_BufferHelper> fresh = new ArrayList<>(this.write_List.size());
         BufferHelper.Inner_BufferHelper changed = null;

         for (BufferHelper.Inner_BufferHelper e : this.write_List) {
            if (e.init_Str().equals(b.load_Str())) {
               changed = new BufferHelper.Inner_BufferHelper(e.init_Str(), e.load_Str(), e.read(), e.init_Z(), e.load_Z(), b.open());
               fresh.add(changed);
            } else {
               fresh.add(e);
            }
         }

         this.write_List = fresh;
         if (changed != null && !changed.load_Str().isBlank()) {
            RenderState peer = this.init_Map.get(changed.load_Str());
            if (peer != null) {
               peer.open = changed.write();
            }
         }

         if (b.load_Str() != null && b.load_Str().equals(L.push())) {
            L.init_V(b.open());
         }

         this.close_J = 0L;
      } else if (_kind.equals(autismclient.e.DataUtil.init(new int[]{-1798794613, -1798794520, 71941818, -2064770173, 416637412, 30855464}))) {
         this.init_V_Z_Str(
            false,
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794609,
                  -1798794557,
                  71941949,
                  -2064766318,
                  416756593,
                  34549544,
                  1071037172,
                  -1157586991,
                  -1525459207,
                  -44598148,
                  -1382545883,
                  90752017,
                  -1481651597,
                  1313437935,
                  2061869560,
                  -506554046,
                  1476693489,
                  -1467144718,
                  1763151226,
                  -1176887920,
                  -2123783820,
                  -1412791766,
                  -846870360,
                  -483175782,
                  -2093546255,
                  -475426946,
                  -1853336520,
                  -1618856184,
                  1355069489
               }
            )
         );
      }
   }

   private void size() {
      DataUtil lb = this.init_DataUtil;
      if (lb != null) {
         if (lb.init_Str != null && !lb.init_Str.isBlank()) {
            this.load_Set.add(lb.init_Str);
         }

         this.load_Map.remove(lb.init_Str);
         bi.init(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794607,
                  -1798794552,
                  71942143,
                  -2064764409,
                  416816118,
                  36396577,
                  1128295543,
                  617420308,
                  1960161329,
                  635460212,
                  -1775572742,
                  791816600,
                  -1223486288,
                  726631859,
                  1050752310,
                  -1786417698,
                  455624997,
                  1239474678,
                  -230993419,
                  1429135706,
                  1353534893,
                  -990089933,
                  -628015501,
                  2006353231,
                  2067407483,
                  -334878351,
                  -1791296781,
                  304371868,
                  845596351,
                  443684006,
                  869304112
               }
            ),
            -42149
         );
         this.run(
            autismclient.e.DataUtil.init(
               new int[]{
                  -1798794607,
                  -1798794552,
                  71942143,
                  -2064764409,
                  416816113,
                  36396580,
                  1128295543,
                  617420358,
                  1960161334,
                  635460149,
                  -1775572762,
                  791816595,
                  -1223486280,
                  726631864,
                  1050752352,
                  -1786417699,
                  455625011,
                  1239474617,
                  -230993410,
                  1429135624,
                  1353534902,
                  -990089930,
                  -628015558,
                  2006353224,
                  2067407411,
                  -334878344,
                  -1791296836,
                  304371858,
                  845596338,
                  443684029,
                  869304188,
                  1178623161,
                  -2117387393,
                  -1214500669,
                  1005183961,
                  1095930018,
                  -385908843,
                  921730059,
                  -1491136441,
                  1019409508,
                  1536922635,
                  399964234,
                  -486011689,
                  2113505394,
                  1094154443
               }
            )
         );
         this.close_V();
      }
   }

   private byte[] init_BArr_I(int epoch) {
      if (this.init_BArr != null && epoch == this.update_I) {
         return this.init_BArr;
      }

      for (BufferHelper.Inner_CachePool o : this.read_List) {
         if (o.init_I() == epoch) {
            return o.init_BArr();
         }
      }

      return null;
   }

   private RenderState init_RenderState_SyncManager(SyncManager env) {
      return this.init_Map.computeIfAbsent(env.init_Str(), k -> new RenderState(env.init_BArr));
   }

   private void write(byte[] frame) {
      byte[] k1 = this.init_BArr;
      if (k1 != null) {
         SyncManager env = SyncManager.init_SyncManager_BArr_BArr(frame, k1);
         if (env == null) {
            for (BufferHelper.Inner_CachePool o : this.read_List) {
               env = SyncManager.init_SyncManager_BArr_BArr(frame, o.init_BArr());
               if (env != null) {
                  break;
               }
            }
         }

         if (env != null) {
            if (this.size++ == 0L && this.init_EventBus.sync_Z()) {
               AutismClientAddon.LOG
                  .info(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794605,
                           -1798794552,
                           71942051,
                           -2064762471,
                           416875682,
                           38243641,
                           1185553727,
                           -1902539559,
                           1150814817,
                           1315518581,
                           2126367411,
                           1492881179,
                           -965320712,
                           139825796,
                           39635060,
                           1228685896,
                           -565443206,
                           -348873234,
                           2069829048,
                           -259808104,
                           535886522,
                           -567388119,
                           -409160918,
                           200914903,
                           1933394025,
                           -194329793,
                           -1729257300,
                           -2067367195,
                           336123369,
                           1829887368,
                           891936314,
                           1880223019,
                           -1842627720,
                           -1286887225,
                           -1238799388,
                           251924516,
                           -780275473,
                           1581261584,
                           1774471426,
                           -825957718,
                           165115464,
                           823610964,
                           -237865334,
                           1216112040,
                           -955233394,
                           452536535,
                           1143731268,
                           1095931944,
                           -385846953,
                           923644868,
                           -1431780979,
                           -1435534487,
                           -1551898923,
                           -864227763,
                           -1021255867,
                           -1594159632,
                           2120657929,
                           1315887452,
                           2137803935,
                           1847415026,
                           1435288024,
                           1544254354,
                           627247646,
                           -2030156997,
                           1489643343,
                           -1065697578,
                           1323112398,
                           -1933187527,
                           200727697,
                           1927592277,
                           -374180588,
                           1285306547,
                           1189798482,
                           -1770953659,
                           935010955,
                           -1079428120,
                           897464263,
                           2051587801,
                           -825286597,
                           185921041,
                           1468587285,
                           -1718433389,
                           -1731826285,
                           -2147004054,
                           -2132618542,
                           -1686666225,
                           -747042047,
                           -1683463170,
                           -647751596,
                           1394536282,
                           280950935,
                           119547586,
                           -588993058,
                           -1078916493,
                           913323612,
                           -1751738099,
                           1530696531,
                           206951651,
                           2120532874,
                           1312013388,
                           2017708246,
                           -1875554314,
                           1987355040,
                           1478463117,
                           -1412280796,
                           -831030722,
                           7851810,
                           243405274,
                           -1044372570,
                           1984189802,
                           1380338913,
                           -159169533,
                           -639284941,
                           1657003950,
                           -172483979,
                           -1052039412,
                           1746517672,
                           -1692525832,
                           -928696326,
                           1275181518,
                           875922479,
                           1383793981,
                           -52058773,
                           -1613824803,
                           1511037358,
                           -402481054,
                           407986853
                        }
                     )
                  );
            }
         } else {
            this.data++;
         }
      }
   }

   private void init_V_SyncManager(SyncManager env) {
      autismclient.util.mm.b.DataUtil.UiHelper r = autismclient.util.mm.b.DataUtil.JsonUtil.init(new autismclient.util.mm.b.DataUtil.UiHelper(), env.update);
      if (r != null) {
         BufferHelper.Inner_ConfigStore sc = this.read_Map.get(autismclient.util.mm.a.StreamUtil.init_Str_BArr(r.init));
         if (sc != null) {
            String fp = env.init_Str();
            if (sc.init_Set.add(fp) && this.init_EventBus.sync_Z()) {
               RenderState peer = this.init_Map.get(fp);
               AutismClientAddon.LOG
                  .info(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794605,
                           -1798794552,
                           71942051,
                           -2064762471,
                           416875682,
                           38243623,
                           1185553717,
                           -1902539563,
                           1150814836,
                           1315518590,
                           2126367394,
                           1492881164,
                           -965320754,
                           139825913,
                           39634991,
                           1228685895,
                           -565443275,
                           -348873217,
                           2069829043,
                           -259808112,
                           535886590,
                           -567388117,
                           -409160899,
                           200914892,
                           1933394044,
                           -194329733,
                           -1729257222,
                           -2067367180,
                           336123380,
                           1829887425,
                           891936295,
                           1880223039,
                           -1842627779,
                           -1286887285,
                           -1238799451,
                           251924601,
                           -780275521,
                           1581261576,
                           1774471429,
                           -825957717,
                           165115459,
                           823610946,
                           -237865328,
                           1216112032,
                           -955233328,
                           452536512,
                           1143731268,
                           1095931945,
                           -385846966,
                           923644869,
                           -1431780992,
                           -1435534491,
                           -1551898923,
                           -864227840,
                           -1021255923,
                           -1594159702,
                           2120657925,
                           1315887375,
                           2137803921,
                           1847415021,
                           1435288006,
                           1544254338,
                           627247692,
                           -2030157005,
                           1489643339,
                           -1065697593,
                           1323112331,
                           -1933187482,
                           200727695,
                           1927592198,
                           -374180587,
                           1285306552,
                           1189798485,
                           -1770953698
                        }
                     ),
                     sc.init_Str,
                     peer != null
                        ? this.init_Str_Str(peer.init_Str)
                        : autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794603,
                              -1798794543,
                              71942245,
                              -2064760571,
                              416935214,
                              40090680,
                              1242812156,
                              -127532196,
                              341468143,
                              1995576948,
                              1733340533,
                              -2101021545,
                              -707155405,
                              -446980162,
                              -971482144
                           }
                        ),
                     sc.init_Set.size(),
                     System.currentTimeMillis() - sc.init_J
                  );
            }
         }
      }
   }

   private void load_V_SyncManager(SyncManager env) {
      if (this.clear_Z()) {
         try {
            this.init_Z_UiHelper_BArr(UiHelper.poll, new autismclient.util.mm.b.DataUtil.UiHelper(env.read).init_BArr());
         } catch (Throwable var3) {
         }
      }
   }

   private boolean clear_Z() {
      DataUtil lb = this.init_DataUtil;
      return lb != null && !lb.load_Z && this.init_I() <= 100;
   }

   private static boolean init_Z_UiHelper(UiHelper type) {
      boolean var10000;
      switch (type) {
         case load:
         case read:
         case write:
         case update:
         case close:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private void read_V_SyncManager(SyncManager env) {
      autismclient.util.mm.b.DataUtil.SyncManager p = autismclient.util.mm.b.DataUtil.JsonUtil.init(
         new autismclient.util.mm.b.DataUtil.SyncManager(), env.update
      );
      if (p != null) {
         String fp = env.init_Str();
         StateCache.Inner_DataUtil id = StateCache.init(p.write, env.write);
         if (id != null) {
            this.write_Map.put(fp, id.init());
         }

         String did = id != null ? id.init() : this.write_Map.get(fp);
         if (did != null && this.pop_Z() && this.init_EventBus.sync_Z(did)) {
            byte[] fpb = init_BArr_Str(fp);
            if (fpb != null) {
               this.init_Z_UiHelper_BArr(UiHelper.open, new autismclient.util.mm.b.DataUtil.BufferHelper(fpb).init_BArr());
            }

            this.init_Set.add(fp);
            this.init_Map.remove(fp);
            if (this.init_DataUtil != null) {
               this.load(this.init_DataUtil.init_Str, did, fp);
            }
         } else {
            boolean isNew = !this.init_Map.containsKey(fp);
            RenderState peer = this.init_RenderState_SyncManager(env);
            peer.flush = did == null ? autismclient.e.DataUtil.init(new int[]{-1798794603}) : did;

            for (BufferHelper.Inner_BufferHelper e : this.write_List) {
               if (fp.equalsIgnoreCase(e.load_Str())) {
                  peer.read_Z = e.init_Z();
                  peer.write_Z = e.load_Z();
                  peer.open = e.write();
                  if (peer.flush.isBlank()) {
                     peer.flush = e.init_Str();
                  }
                  break;
               }
            }

            if (id != null) {
               peer.close = HashUtil.init(id.load(), 32);
               if (!peer.close.isBlank()) {
                  this.init_EventBus.read(did, peer.close);
               }
            }

            boolean blockedNow = did != null && this.init_EventBus.update_Z(did) || this.init_EventBus.read_Z(fp);
            peer.update_Z = blockedNow;
            if (blockedNow) {
               this.write_Set.add(fp);
            } else {
               this.write_Set.remove(fp);
            }

            peer.read_Str = HashUtil.init(p.init_Str, 32);
            peer.init_J = System.currentTimeMillis();
            boolean senderMayShare = this.write_Z(fp);
            peer.init_Z = senderMayShare && p.init_Z;
            peer.write_Str = senderMayShare ? HashUtil.init(p.load_Str, 48) : autismclient.e.DataUtil.init(new int[]{-1798794601});
            peer.update_Str = senderMayShare ? HashUtil.init(p.read, 64) : autismclient.e.DataUtil.init(new int[]{-1798794601});
            peer.init_I = senderMayShare ? p.init_I : -1;
            if (isNew) {
               long nowMs = System.currentTimeMillis();
               DataUtil lb = this.init_DataUtil;
               boolean largeRoom = lb != null && (lb.load_Z || lb.init_I > 100);
               if (!largeRoom && nowMs - this.pop > 1500L) {
                  this.pop = nowMs;
                  this.prev();
                  this.push_J = nowMs;
               }
            }
         }
      }
   }

   private void write(SyncManager env) {
      autismclient.util.mm.b.DataUtil.StreamUtil c = autismclient.util.mm.b.DataUtil.JsonUtil.init(new autismclient.util.mm.b.DataUtil.StreamUtil(), env.update);
      if (c != null) {
         RenderState peer = this.init_RenderState_SyncManager(env);
         peer.init_J = System.currentTimeMillis();
         this.init_V_JsonUtil(new JsonUtil(peer.init_Str, this.init_Str_Str(peer.init_Str), HashUtil.init(c.init, 512), false, false));
         this.load_V_SyncManager(env);
      }
   }

   private void update(SyncManager env) {
      autismclient.util.mm.b.DataUtil.NetHelper m = autismclient.util.mm.b.DataUtil.JsonUtil.init(new autismclient.util.mm.b.DataUtil.NetHelper(), env.update);
      if (m != null) {
         m.init_Str = HashUtil.init(m.init_Str, 48);
         m.load = HashUtil.init(m.load, 64);
         RenderState peer = this.init_RenderState_SyncManager(env);
         peer.init_J = System.currentTimeMillis();
         this.init_V_JsonUtil(JsonUtil.init(peer.init_Str, this.init_Str_Str(peer.init_Str), false, m));
         this.load_V_SyncManager(env);
      }
   }

   private void sync(SyncManager env) {
      autismclient.util.mm.b.DataUtil.CachePool c = autismclient.util.mm.b.DataUtil.JsonUtil.init(new autismclient.util.mm.b.DataUtil.CachePool(), env.update);
      if (c != null) {
         RenderState peer = this.init_RenderState_SyncManager(env);
         peer.init_J = System.currentTimeMillis();
         this.init_V_JsonUtil(JsonUtil.init(peer.init_Str, this.init_Str_Str(peer.init_Str), false, c));
         this.load_V_SyncManager(env);
      }
   }

   private void flush(SyncManager env) {
      autismclient.util.mm.b.DataUtil.StateCache p = autismclient.util.mm.b.DataUtil.JsonUtil.init(new autismclient.util.mm.b.DataUtil.StateCache(), env.update);
      if (p != null) {
         p.init = HashUtil.init(p.init, 48);
         RenderState peer = this.init_RenderState_SyncManager(env);
         peer.init_J = System.currentTimeMillis();
         this.init_V_JsonUtil(JsonUtil.init(peer.init_Str, this.init_Str_Str(peer.init_Str), false, p));
         this.load_V_SyncManager(env);
      }
   }

   private void close(SyncManager env) {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = autismclient.util.mm.b.DataUtil.JsonUtil.init(
         new autismclient.util.mm.b.DataUtil.Inner_DataUtil(), env.update
      );
      if (b != null) {
         b.load = HashUtil.init(b.load, 64);
         RenderState peer = this.init_RenderState_SyncManager(env);
         peer.init_J = System.currentTimeMillis();
         if (autismclient.e.DataUtil.init(new int[]{-1798794599, -1798794508, 71942375, -2064756706, 417054396, 43784759, 1357328751}).equals(b.init_Str)
            && ConfigStore.init_ConfigStore_CacheP(b) == null) {
            this.load_V_SyncManager(env);
         } else {
            this.init_V_JsonUtil(JsonUtil.init(peer.init_Str, this.init_Str_Str(peer.init_Str), false, b));
            this.load_V_SyncManager(env);
         }
      }
   }

   private void open(SyncManager env) {
      autismclient.util.mm.b.DataUtil.TaskQueue loc = autismclient.util.mm.b.DataUtil.JsonUtil.init(new autismclient.util.mm.b.DataUtil.TaskQueue(), env.update);
      if (loc != null) {
         RenderState peer = this.init_RenderState_SyncManager(env);
         peer.load_Z = true;
         peer.sync = HashUtil.init(loc.init_Str, 48);
         peer.init_D = loc.init_D;
         peer.load_D = loc.load;
         peer.read_D = loc.read;
         peer.init_J = System.currentTimeMillis();
      }
   }

   private void poll(SyncManager env) {
      this.init_Map.remove(env.init_Str());
      this.init_TextFormat.init_V_Str(env.init_Str());
   }

   public synchronized void init_V_Str_Z(String targetFpHex, boolean allowed) {
      if (this.write_Z() && this.init_DataUtil != null && targetFpHex != null) {
         BufferHelper.Inner_BufferHelper target = null;

         for (BufferHelper.Inner_BufferHelper e : this.write_List) {
            if (targetFpHex.equalsIgnoreCase(e.load_Str())) {
               target = e;
               break;
            }
         }

         if (target != null && !target.init_Str().isBlank() && !target.init_Z()) {
            String lobbyId = this.init_DataUtil.init_Str;
            String targetDid = target.init_Str();
            int expectedVersion = this.init_DataUtil.load_I;
            this.init_V_Runnable(
               () -> {
                  String jwt = L.tick();
                  if (jwt != null && !jwt.isBlank()) {
                     JsonObject body = new JsonObject();
                     body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794573, -1798794535, 71943129, -2064731552}), jwt);
                     body.addProperty(
                        autismclient.e.DataUtil.init(new int[]{-1798794571, -1798794535, 71943043, -2064729612, 417888591, 69643278, -2136022489, -1792185540}),
                        lobbyId
                     );
                     body.addProperty(
                        autismclient.e.DataUtil.init(
                           new int[]{-1798794571, -1798794559, 71943053, -2064729628, 417888586, 69643282, -2136022502, -1792185572, 276824006, -8391075}
                        ),
                        targetDid
                     );
                     body.addProperty(
                        autismclient.e.DataUtil.init(new int[]{-1798794569, -1798794538, 71943238, -2064727692, 417948036, 71490318, -2078764087, -17178178}),
                        allowed
                     );
                     body.addProperty(
                        autismclient.e.DataUtil.init(
                           new int[]{
                              -1798794569,
                              -1798794556,
                              71943258,
                              -2064727683,
                              417948042,
                              71490322,
                              -2078764087,
                              -17178200,
                              -532522693,
                              671667294,
                              -653151396,
                              1227141871,
                              -613310330,
                              -1832747886,
                              -980606242
                           }
                        ),
                        expectedVersion
                     );
                     AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
                        autismclient.e.DataUtil.init(
                           new int[]{-1798794567, -1798794602, 71943195, -2064725782, 418007756, 73337370, -2021505919, 1757829177, -1341869223}
                        ),
                        body
                     );
                     if (r.init_Z() && r.init_JsonObject() != null) {
                        this.init_V_JsonObject(r.init_JsonObject());
                        this.reset();
                     } else if (r.init_I() == 409) {
                        this.reset();
                        bi.init(
                           autismclient.e.DataUtil.init(
                              new int[]{
                                 -1798794567,
                                 -1798794518,
                                 71943192,
                                 -2064725761,
                                 418007752,
                                 73337360,
                                 -2021505905,
                                 1757829166,
                                 -1341869301,
                                 1351725645,
                                 -1046178551,
                                 1928206444,
                                 -355144768,
                                 1875413398,
                                 -1991723619,
                                 -1613887597,
                                 1509094470,
                                 -462712402,
                                 -1459182050,
                                 2009999313,
                                 -2114532207,
                                 -1125987872,
                                 -545888614,
                                 257324805,
                                 -612862627,
                                 -1818873566,
                                 -550504687,
                                 114227010,
                                 -753930866,
                                 -1897019097,
                                 1321949058,
                                 -1969248614,
                                 -917162418,
                                 1632735465,
                                 -924809191,
                                 1395689165,
                                 316690136,
                                 1227458786,
                                 -603486060,
                                 -1528195518
                              }
                           ),
                           -14249
                        );
                     } else {
                        bi.init(
                           autismclient.e.DataUtil.init(
                              new int[]{
                                 -1798794565,
                                 -1798794504,
                                 71943369,
                                 -2064723863,
                                 418067211,
                                 75184409,
                                 -1964247544,
                                 -762130768,
                                 2143751302,
                                 2031784011,
                                 -1439205494,
                                 -1665696317,
                                 -96979453,
                                 1288607584,
                                 1292126530,
                                 1401216005,
                                 488025992,
                                 -2051060253,
                                 841640669,
                                 321055572,
                                 1362786570,
                                 -703286044,
                                 -327033765,
                                 -1548113533,
                                 -746876285,
                                 -1678324889,
                                 -488464958,
                                 2037455053,
                                 -1263404031,
                                 -510815644,
                                 1344581469,
                                 -1267648743,
                                 -642402938,
                                 1560348834,
                                 1126174937,
                                 551683650,
                                 -77676711
                              }
                           ),
                           -42149
                        );
                     }
                  }
               }
            );
         }
      }
   }

   public synchronized void sync(String fpHex) {
      if (this.pop_Z() && fpHex != null && !fpHex.equals(this.init_Str)) {
         String did = this.write_Map.get(fpHex);
         RenderState target = this.init_Map.get(fpHex);
         if (did != null) {
            this.init_EventBus.load_V_Str_Str(did, target != null ? target.close : autismclient.e.DataUtil.init(new int[]{-1798794599}));
         }

         String who = this.init_Str_Str(fpHex);
         this.init_Set.add(fpHex);
         this.init_Map.remove(fpHex);
         this.init_TextFormat.init_V_Str(fpHex);
         byte[] fp = init_BArr_Str(fpHex);
         if (fp != null) {
            this.init_Z_UiHelper_BArr(UiHelper.open, new autismclient.util.mm.b.DataUtil.BufferHelper(fp).init_BArr());
         }

         if (this.init_DataUtil != null) {
            this.load(this.init_DataUtil.init_Str, did, fpHex);
            this.reset();
         }

         this.run("Banned " + who + " from the lobby");
      }
   }

   private void load(String lobbyId, String targetDid, String targetFp) {
      this.init_V_Runnable(
         () -> {
            String jwt = L.tick();
            if (jwt != null && !jwt.isBlank()) {
               JsonObject body = new JsonObject();
               body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794577, -1798794555, 71942981, -2064735388}), jwt);
               body.addProperty(
                  autismclient.e.DataUtil.init(new int[]{-1798794577, -1798794557, 71943005, -2064735374, 417709713, 64102152, 1987170045, 1472726966}),
                  lobbyId
               );
               if (targetDid != null) {
                  body.addProperty(
                     autismclient.e.DataUtil.init(
                        new int[]{-1798794575, -1798794555, 71942929, -2064733472, 417769430, 65949206, 2044428166, -1047233008, 1895517274, -1368507823}
                     ),
                     targetDid
                  );
               }

               if (targetFp != null) {
                  body.addProperty(
                     autismclient.e.DataUtil.init(
                        new int[]{-1798794575, -1798794555, 71942929, -2064733472, 417769430, 65949206, 2044428166, -1047233006, 1895517251}
                     ),
                     targetFp
                  );
               }

               init_AutismHttp_DataUti_Str_JsonObject(autismclient.e.DataUtil.init(new int[]{-1798794573, -1798794596, 71943116, -2064731531, 417828865}), body);
            }
         }
      );
   }

   public synchronized void flush(String targetFpHex) {
      if (this.pop_Z()) {
         String t = targetFpHex == null ? autismclient.e.DataUtil.init(new int[]{-1798794597}) : targetFpHex.trim().toLowerCase(Locale.ROOT);
         if (!t.isEmpty()
            && !t.equals(this.init_Str)
            && t.matches(
               autismclient.e.DataUtil.init(
                  new int[]{
                     -1798794597,
                     -1798794560,
                     71942390,
                     -2064754735,
                     417114046,
                     45631804,
                     1414586981,
                     902522584,
                     -2086571692,
                     -259215260,
                     554259451,
                     2172342,
                     67341046
                  }
               )
            )) {
            RenderState p = this.init_Map.get(t);
            if (p != null && this.load_Z_Str(t)) {
               String targetDid = this.write_Map.get(t);
               if (targetDid != null && this.init_DataUtil != null) {
                  this.load(this.init_DataUtil.init_Str, targetDid);
                  this.run("Made " + this.init_Str_Str(p.init_Str) + " the host");
               } else {
                  this.run(
                     autismclient.e.DataUtil.init(
                        new int[]{
                           -1798794595,
                           -1798794530,
                           71942501,
                           -2064752880,
                           417173602,
                           47478827,
                           1471845286,
                           -1617437395,
                           1399048870,
                           420843082,
                           161232493,
                           703236898,
                           325506337,
                           1500763020,
                           -720983815,
                           -875665232,
                           -1375817755,
                           299321733,
                           689040194,
                           -114592023,
                           742611513,
                           1546121031,
                           685112824,
                           -236342227,
                           1263326397,
                           508413248,
                           -1419059082,
                           -1041161629,
                           2083725108,
                           170969743,
                           1005097650,
                           1093255093,
                           -468829770,
                           -1648820007,
                           426186042
                        }
                     )
                  );
               }
            } else {
               this.run(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794595,
                        -1798794530,
                        71942501,
                        -2064752880,
                        417173602,
                        47478827,
                        1471845286,
                        -1617437395,
                        1399048870,
                        420843082,
                        161232493,
                        703236898,
                        325506337,
                        1500763020,
                        -720983815,
                        -875665232,
                        -1375817755,
                        299321733,
                        689040194,
                        -114592023,
                        742611513,
                        1546121031,
                        685112828,
                        -236342227,
                        1263326382,
                        508413261,
                        -1419059095,
                        -1041161692
                     }
                  )
               );
            }
         }
      }
   }

   private void load(String lobbyId, String targetDid) {
      this.init_V_Runnable(
         () -> {
            String jwt = L.tick();
            if (jwt != null && !jwt.isBlank()) {
               JsonObject body = new JsonObject();
               body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794581, -1798794559, 71942849, -2064739208}), jwt);
               body.addProperty(
                  autismclient.e.DataUtil.init(new int[]{-1798794581, -1798794553, 71942873, -2064739218, 417590549, 60408084, 1872653425, -2077287766}),
                  lobbyId
               );
               body.addProperty(
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794579, -1798794535, 71942805, -2064737284, 417650258, 62255114, 1929911554, -302280428, -780756770, 1566342741}
                  ),
                  targetDid
               );
               AutismHttp.Inner_DataUtil r = init_AutismHttp_DataUti_Str_JsonObject(
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794579, -1798794622, 71942784, -2064737284, 417650260, 62255105, 1929911557, -302280394, -780756782, 1566342723}
                  ),
                  body
               );
               if (r.init_Z() && r.init_JsonObject() != null) {
                  this.init_V_JsonObject(r.init_JsonObject());
               }
            }
         }
      );
   }

   private void push(SyncManager env) {
      DataUtil lb = this.init_DataUtil;
      if (lb != null && env.init_Str().equals(this.write_Str())) {
         autismclient.util.mm.b.DataUtil.BufferHelper k = autismclient.util.mm.b.DataUtil.JsonUtil.init(
            new autismclient.util.mm.b.DataUtil.BufferHelper(), env.update
         );
         if (k != null) {
            String fp = autismclient.util.mm.a.StreamUtil.init_Str_BArr(k.init);
            if (fp.equals(this.init_Str)) {
               this.size();
            } else {
               this.init_Set.add(fp);
               this.init_Map.remove(fp);
               this.init_TextFormat.init_V_Str(fp);
            }
         }
      }
   }

   private void data() {
      long now = System.currentTimeMillis();
      if (now - this.prev >= 3000L) {
         this.prev = now;
         this.reset();
      }
   }

   private static String load_Str_Str(String code) {
      return code != null
            && code.length() > 17
            && code.charAt(16) == ':'
            && code.substring(0, 16)
               .matches(
                  autismclient.e.DataUtil.init(
                     new int[]{
                        -1798794593,
                        -1798794556,
                        71942514,
                        -2064750803,
                        417233210,
                        49325824,
                        1529103593,
                        157569956,
                        589702340,
                        1100901390,
                        -231794688,
                        1404301529,
                        583671932,
                        913957076,
                        -1732101122,
                        2139438395
                     }
                  )
               )
         ? code.substring(0, 16).toLowerCase(Locale.ROOT)
         : autismclient.e.DataUtil.init(new int[]{-1798794593});
   }

   private static byte[] init_BArr_Str(String hex) {
      if (hex != null && hex.length() == 16) {
         try {
            byte[] out = new byte[8];

            for (int i = 0; i < 8; i++) {
               out[i] = (byte)Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            }

            return out;
         } catch (Exception e) {
            return null;
         }
      } else {
         return null;
      }
   }

   private synchronized void init_V_JsonUtil(JsonUtil line) {
      this.init_Deque.addLast(line);

      while (this.init_Deque.size() > 300) {
         this.init_Deque.removeFirst();
      }

      this.write_I++;
      if (this.init_EventBus.update_Z()) {
         NetHelper.init_V(line);
      }
   }

   public synchronized void push_V() {
      this.init_Deque.clear();
      this.write_I++;
   }

   public synchronized boolean push_Z() {
      return !this.init_Deque.isEmpty();
   }

   private void run(String text) {
      this.init_V_JsonUtil(JsonUtil.init(text));
   }

   public synchronized void close(String fpHex) {
      RenderState peer = this.init_Map.get(fpHex);
      String did = this.write_Map.get(fpHex);
      if (did != null) {
         this.init_EventBus.init_V_Str_Str(did, peer != null ? peer.close : autismclient.e.DataUtil.init(new int[]{-1798794591}));
         this.load_Map.values().removeIf(l -> did.equals(l.flush));
      } else {
         this.init_EventBus.sync_V(fpHex);
      }

      this.write_Set.add(fpHex);
      if (peer != null) {
         peer.update_Z = true;
      }
   }

   public synchronized void open(String did) {
      if (did != null) {
         this.init_EventBus.poll(did);
         this.write_Set.removeIf(fp -> did.equals(this.write_Map.get(fp)));

         for (RenderState p : this.init_Map.values()) {
            if (did.equals(p.flush)) {
               p.update_Z = false;
            }
         }
      }
   }

   public synchronized void poll(String fpHex) {
      String did = this.write_Map.get(fpHex);
      if (did != null) {
         this.init_EventBus.poll(did);
      } else {
         this.init_EventBus.flush_V_Str(fpHex);
      }

      this.write_Set.remove(fpHex);
      RenderState p = this.init_Map.get(fpHex);
      if (p != null) {
         p.update_Z = false;
      }
   }

   public synchronized void push(String did) {
      if (did != null) {
         this.init_EventBus.push(did);

         for (Entry<String, String> e : this.write_Map.entrySet()) {
            if (did.equals(e.getValue())) {
               this.init_Set.remove(e.getKey());
            }
         }

         DataUtil lb = this.init_DataUtil;
         if (lb != null && this.pop_Z()) {
            this.read(lb.init_Str, did);
         }
      }
   }

   private void read(String lobbyId, String targetDid) {
      this.init_V_Runnable(
         () -> {
            String jwt = L.tick();
            if (jwt != null && !jwt.isBlank()) {
               JsonObject body = new JsonObject();
               body.addProperty(autismclient.e.DataUtil.init(new int[]{-1798794585, -1798794547, 71942733, -2064743044}), jwt);
               body.addProperty(
                  autismclient.e.DataUtil.init(new int[]{-1798794585, -1798794549, 71942741, -2064743062, 417471385, 56714000, 1758136821, -1332335186}),
                  lobbyId
               );
               body.addProperty(
                  autismclient.e.DataUtil.init(
                     new int[]{-1798794583, -1798794531, 71942681, -2064741128, 417531102, 58561038, 1815394958, 442672136, 837936466, 206225993}
                  ),
                  targetDid
               );
               init_AutismHttp_DataUti_Str_JsonObject(
                  autismclient.e.DataUtil.init(new int[]{-1798794583, -1798794618, 71942669, -2064741148, 417531099, 58561034, 1815394964}), body
               );
            }
         }
      );
   }

   public String open_Str() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null) {
            String inWorld = mc.player.getGameProfile().name();
            if (inWorld != null && !inWorld.isBlank()) {
               return inWorld;
            }
         }

         String mcName = mc.getUser().getName();
         if (mcName != null && !mcName.isBlank()) {
            return mcName;
         }
      } catch (Throwable var3) {
      }

      return autismclient.e.DataUtil.init(new int[]{-1798794591});
   }

   public String poll_Str() {
      String proofName = HashUtil.init(StateCache.init_Str(), 32);
      if (!proofName.isBlank()) {
         return proofName;
      }

      String loginName = HashUtil.init(L.write_Str(), 32);
      return loginName.isBlank()
         ? autismclient.e.DataUtil.init(
            new int[]{
               -1798794589,
               -1798794521,
               71942615,
               -2064746889,
               417352220,
               53019914,
               1643620146,
               -587382622,
               -1028990943,
               -1833949110,
               -1017848665,
               -1488536603,
               1100002785,
               -259655028,
               540631606
            }
         )
         : loginName;
   }

   public String init_Str_Str(String fpHex) {
      if (fpHex != null && fpHex.equalsIgnoreCase(this.init_Str)) {
         return this.poll_Str();
      }

      RenderState peer = this.init_RenderState_Str(fpHex);
      if (peer != null && peer.close != null && !peer.close.isBlank()) {
         return peer.init();
      }

      if (fpHex != null) {
         for (BufferHelper.Inner_BufferHelper e : this.write_List) {
            if (fpHex.equalsIgnoreCase(e.load_Str())) {
               String name = HashUtil.init(e.read(), 32);
               if (!name.isBlank()) {
                  return name;
               }
            }
         }
      }

      return autismclient.e.DataUtil.init(
         new int[]{
            -1798794589,
            -1798794521,
            71942615,
            -2064746889,
            417352220,
            53019914,
            1643620146,
            -587382622,
            -1028990943,
            -1833949110,
            -1017848665,
            -1488536603,
            1100002785,
            -259655028,
            540631606
         }
      );
   }

   public static String init_Str_Str_Z(String fpHex, boolean self) {
      if (self) {
         return L.update_Str();
      } else if (fpHex != null && !fpHex.isEmpty()) {
         BufferHelper mm = init_BufferHelper();
         RenderState peer = mm == null ? null : mm.init_RenderState_Str(fpHex);
         String role = peer == null ? null : peer.open;
         return role != null && !role.isBlank() ? role : autismclient.e.DataUtil.init(new int[]{-1798794587, -1798794544, 71942543, -2064744989, 417411919});
      } else {
         return autismclient.e.DataUtil.init(new int[]{-1798794587, -1798794544, 71942543, -2064744989, 417411919});
      }
   }

   public static boolean init_Z_Str_Z(String fpHex, boolean self) {
      return FileCache.init_Z(init_Str_Str_Z(fpHex, self));
   }

   public static int init_I_Str_Z(String fpHex, boolean self) {
      return FileCache.init_I(init_Str_Str_Z(fpHex, self));
   }

   public static int init(String fpHex, boolean self, int index, int length) {
      return FileCache.init_I_Str(init_Str_Str_Z(fpHex, self), index, length);
   }

   public record Inner_BufferHelper(String init_Str, String load_Str, String read, boolean init_Z, boolean load_Z, String write) {
   }

   private record Inner_CachePool(int init_I, byte[] init_BArr) {
   }

   private static final class Inner_ConfigStore {
      final String init_Str;
      final long init_J;
      final Set<String> init_Set = ConcurrentHashMap.newKeySet();

      Inner_ConfigStore(String type, long sentMs) {
         this.init_Str = type;
         this.init_J = sentMs;
      }
   }

   public enum Inner_DataUtil {
      init,
      load,
      read;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
