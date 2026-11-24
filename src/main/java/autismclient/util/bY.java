package autismclient.util;

import autismclient.AutismClientAddon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class bY extends df<bW> implements Iterable<bW> {
   private static final bY init_bY = new bY();
   private final AtomicBoolean init_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicBoolean load_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private final AtomicInteger load_AtomicInteger = new AtomicInteger();
   private final AtomicBoolean read_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicBoolean write_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicInteger read_AtomicInteger = new AtomicInteger();
   private final AtomicInteger write_AtomicInteger = new AtomicInteger();
   private final AtomicInteger update_AtomicInteger = new AtomicInteger();
   private final AtomicBoolean update_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicBoolean sync_AtomicBoolean = new AtomicBoolean(false);
   private final AtomicBoolean flush_AtomicBoolean = new AtomicBoolean(false);
   private int init_I = 3000;
   private int load_I = 64;
   private int read_I = 0;
   private boolean init_Z_2 = true;
   private boolean load_Z = true;
   private volatile boolean auditGeoLookup;
   private int write_I = 2000;
   private int update_I = 0;
   private volatile long init_J_2;
   private volatile long load_J;
   private volatile long read_J;
   private volatile long write_J;
   private volatile long update_J;
   private volatile long sync_J;
   private volatile long flush_J;
   private volatile long close = -9223372036854775808L;
   private volatile long open;
   private volatile long poll;
   private volatile long push;
   private volatile boolean read_Z;
   private volatile ExecutorService init_ExecutorService;
   private volatile Thread init_Thread;
   private volatile List<bW> load_List = List.of();
   private static final Pattern init_Pattern = Pattern.compile(
      "^(?:([\\w\\s]+)=)?((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])(?i:@(socks[45]))?$",
      8
   );
   private static final Pattern load_Pattern = Pattern.compile(
      "^((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]):([^:]+)(?::(.+))?$",
      8
   );
   private static final Pattern read_Pattern = Pattern.compile(
      "^(?:(?<type>socks|socks4|socks5)://)?(?:(?<user>[\\w~-]+)(:(?<pass>[\\w~-]+))?@)?(?<addr>(?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(?<port>\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$",
      8
   );
   private static final int sync_I = 100;

   private bY() {
   }

   public static bY init_bY() {
      init_bY.load_V();
      aV.init();
      init_bY.write_V();
      return init_bY;
   }

   private synchronized void write_V() {
      boolean changed = false;

      for (bW proxy : this.init_List) {
         if (proxy != null) {
            proxy.write();
            changed |= proxy.load_Z;
            proxy.load_Z = false;
         }
      }

      if (changed) {
         this.read_V();
      }
   }

   public synchronized bW init_bW_Str(String id) {
      if (id != null && !id.isBlank()) {
         for (bW proxy : this.init_List) {
            if (id.equals(proxy.write())) {
               return proxy;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   protected File init_File() {
      return new File(Minecraft.getInstance().gameDirectory, "autism-proxies.nbt");
   }

   @Override
   protected String init_Str() {
      return "proxies";
   }

   @Override
   protected bW init_Obj_CompoundTag(CompoundTag tag) {
      return new bW().init_bW_CompoundTag(tag);
   }

   @Override
   protected CompoundTag init_CompoundTag_Obj(bW item) {
      return item.init_CompoundTag();
   }

   @Override
   protected String load_Str() {
      return "Autism proxies";
   }

   @Override
   protected void init_V_CompoundTag(CompoundTag tag) {
      this.init_I = tag.getIntOr("timeoutMs", this.init_I);
      this.load_I = tag.getIntOr("threads", this.load_I);
      this.read_I = tag.getIntOr("retries", this.read_I);
      this.init_Z_2 = tag.getBooleanOr("sortByLatency", this.init_Z_2);
      this.load_Z = tag.getBooleanOr("pruneDead", this.load_Z);
      this.write_I = tag.getIntOr("pruneLatency", this.write_I);
      this.update_I = tag.getIntOr("pruneToCount", this.update_I);
      this.auditGeoLookup = tag.getBooleanOr("geoLookup", false);
   }

   @Override
   protected void load_V_CompoundTag(CompoundTag tag) {
      tag.putInt("timeoutMs", this.init_I);
      tag.putInt("threads", this.load_I);
      tag.putInt("retries", this.read_I);
      tag.putBoolean("sortByLatency", this.init_Z_2);
      tag.putBoolean("pruneDead", this.load_Z);
      tag.putInt("pruneLatency", this.write_I);
      tag.putInt("pruneToCount", this.update_I);
      tag.putBoolean("geoLookup", this.auditGeoLookup);
   }

   private void update_V() {
      this.read_Z = true;
      if (this.update_AtomicBoolean.compareAndSet(false, true)) {
         Thread thread = new Thread(() -> {
            try {
               while (true) {
                  this.read_Z = false;
                  this.read_V();
                  if (!this.read_Z) {
                     break;
                  }
               }
            } finally {
               this.update_AtomicBoolean.set(false);
               if (this.read_Z) {
                  this.update_V();
               }
            }
         }, "Autism-Proxy-Save");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public synchronized int init_I_2() {
      int count = 0;

      for (bW proxy : this.init_List) {
         if (proxy.init_Z && proxy.init_Z()) {
            count++;
         }
      }

      return count;
   }

   public long init_J() {
      return this.open;
   }

   public synchronized boolean init_Z_bW(bW proxy) {
      if (proxy != null && proxy.init_Z() && !this.init_List.contains(proxy)) {
         if (this.init_List.isEmpty()) {
            proxy.init_Z = true;
         }

         this.init_List.add(proxy);
         this.flush_V();
         this.update_V();
         this.init_Z_Z(false);
         return true;
      } else {
         return false;
      }
   }

   public synchronized void init_V_bW(bW proxy) {
      if (this.init_List.remove(proxy)) {
         this.flush_V();
         this.update_V();
      }
   }

   public synchronized int load_I() {
      if (!this.init_AtomicBoolean.get() && !this.read_AtomicBoolean.get() && !this.init_List.isEmpty()) {
         int removed = this.init_List.size();
         this.init_List.clear();
         this.flush_V();
         this.update_V();
         return removed;
      } else {
         return 0;
      }
   }

   public synchronized boolean init_Z_bW_bW(bW existing, bW updated) {
      if (existing != null && updated != null && updated.init_Z()) {
         int index = -1;

         for (int i = 0; i < this.init_List.size(); i++) {
            bW proxy = this.init_List.get(i);
            if (proxy == existing) {
               index = i;
               break;
            }
         }

         if (index < 0) {
            return false;
         }

         for (int i = 0; i < this.init_List.size(); i++) {
            if (i != index && this.init_List.get(i).equals(updated)) {
               return false;
            }
         }

         boolean identityChanged = existing.init_bZ != updated.init_bZ || existing.init_I != updated.init_I || !Objects.equals(existing.read, updated.read);
         existing.load_Str = updated.load_Str;
         existing.init_bZ = updated.init_bZ;
         existing.read = updated.read;
         existing.init_I = updated.init_I;
         existing.write = updated.write;
         existing.update = updated.update;
         if (identityChanged) {
            existing.init_bW_CachePool = bW.Inner_CachePool.init;
            existing.init_J = 0L;
            existing.init_V();
         }

         this.flush_V();
         this.update_V();
         if (identityChanged) {
            this.init_Z_Z(false);
         }

         return true;
      } else {
         return false;
      }
   }

   public synchronized void init_V_bW_Z(bW proxy, boolean enabled) {
      for (bW current : this.init_List) {
         current.init_Z = false;
      }

      if (proxy != null) {
         proxy.init_Z = enabled;
      }

      this.flush_V();
      this.update_V();
   }

   public synchronized bW init_bW() {
      for (bW proxy : this.init_List) {
         if (proxy.init_Z && proxy.init_Z()) {
            return proxy;
         }
      }

      return null;
   }

   public boolean init_Z() {
      return this.init_AtomicBoolean.get();
   }

   public bY.Inner_TaskQueue init_bY_TaskQueue() {
      boolean running = this.init_AtomicBoolean.get();
      boolean canceling = !running && System.currentTimeMillis() < this.read_J;
      return new bY.Inner_TaskQueue(running, canceling, this.init_AtomicInteger.get(), this.load_AtomicInteger.get(), this.init_J_2, this.load_J);
   }

   public bY.Inner_BufferHelper init_bY_BufferHelper() {
      boolean running = this.read_AtomicBoolean.get();
      boolean canceling = !running && System.currentTimeMillis() < this.sync_J;
      boolean canceled = !running && this.write_J == this.close;
      return new bY.Inner_BufferHelper(
         running,
         canceling,
         this.read_AtomicInteger.get(),
         this.write_AtomicInteger.get(),
         this.update_AtomicInteger.get(),
         this.write_J,
         this.update_J,
         canceled
      );
   }

   public int read_I() {
      return this.init_I;
   }

   public void init_V_I(int v) {
      this.init_I = Math.max(1, v);
      this.update_V();
   }

   public int write_I() {
      return this.load_I;
   }

   public void load_V_I(int v) {
      this.load_I = Math.max(1, v);
      this.update_V();
   }

   public int update_I() {
      return this.read_I;
   }

   public void read_V_I(int v) {
      this.read_I = Math.max(0, v);
      this.update_V();
   }

   public boolean load_Z() {
      return this.init_Z_2;
   }

   public synchronized void init_V_Z(boolean v) {
      this.init_Z_2 = v;
      if (v) {
         this.close_V();
      }

      this.flush_V();
      this.update_V();
   }

   public boolean read_Z() {
      return this.load_Z;
   }

   public void load_V_Z(boolean v) {
      this.load_Z = v;
      this.update_V();
   }

   public int sync_I() {
      return this.write_I;
   }

   public void write(int v) {
      this.write_I = Math.max(0, v);
      this.update_V();
   }

   public int flush_I() {
      return this.update_I;
   }

   public void update(int v) {
      this.update_I = Math.max(0, v);
      this.update_V();
   }

   public synchronized boolean write_Z() {
      if (!this.init_AtomicBoolean.get() && !this.read_AtomicBoolean.get() && this.init_List.size() >= 2) {
         this.close_V();
         this.flush_V();
         this.update_V();
         return true;
      } else {
         return false;
      }
   }

   public void read_V_Z(boolean all) {
      this.load_Z_Z(all);
   }

   public boolean auditGeoLookup() {
      return this.auditGeoLookup;
   }

   public void auditSetGeoLookup(boolean v) {
      this.auditGeoLookup = v;
      this.update_V();
      if (v) {
         this.init_Z_Z(false);
      }
   }

   public boolean init_Z_Z(boolean force) {
      if (!this.auditGeoLookup) {
         return false;
      }

      this.load_V();
      if (!this.sync_AtomicBoolean.compareAndSet(false, true)) {
         this.flush_AtomicBoolean.set(true);
         return false;
      }

      List<bY.Inner_StreamUtil> targets;
      long generation;
      synchronized (this) {
         targets = this.init_List_Z(force);
         if (targets.isEmpty()) {
            this.sync_AtomicBoolean.set(false);
            return false;
         }

         generation = this.flush_J + 1L;
         this.flush_J = generation;
         long now = System.currentTimeMillis();

         for (bY.Inner_StreamUtil target : targets) {
            target.init_bW().init_V_J(now);
         }

         this.flush_V();
      }

      Thread thread = new Thread(() -> this.load(generation, targets), "Autism-Proxy-Geo");
      thread.setDaemon(true);
      thread.start();
      return true;
   }

   public boolean load_Z_Z(boolean all) {
      return this.load(all, "", 0);
   }

   public boolean init(boolean all, String host, int port) {
      String destination = host == null ? "" : host.trim();
      return !destination.isBlank() && port > 0 && port <= 65535 ? this.load(all, destination, port) : false;
   }

   private boolean load(boolean all, String destinationHost, int destinationPort) {
      List<bY.Inner_NetHelper> targets = new ArrayList<>();
      List<bW> active = new ArrayList<>();
      ExecutorService executor;
      long generation;
      int timeout;
      int retryCount;
      int workerCount;
      synchronized (this) {
         if (this.init_AtomicBoolean.get() || this.read_AtomicBoolean.get() || this.init_List.isEmpty()) {
            return false;
         }

         for (bW proxy : this.init_List) {
            if (proxy != null && proxy.init_Z() && (all || proxy.init_bW_CachePool == bW.Inner_CachePool.init)) {
               targets.add(new bY.Inner_NetHelper(proxy, init_Str_bW(proxy)));
               active.add(proxy);
            }
         }

         if (targets.isEmpty()) {
            return false;
         }

         generation = this.init_J_2 + 1L;
         this.init_J_2 = generation;
         this.load_AtomicBoolean.set(false);
         this.read_J = 0L;
         this.init_AtomicInteger.set(0);
         this.load_AtomicInteger.set(targets.size());
         this.load_List = List.copyOf(active);

         for (bW proxy : active) {
            proxy.init_bW_CachePool = bW.Inner_CachePool.load;
            proxy.init_J = 0L;
         }

         timeout = this.load_I_I(targets.size());
         retryCount = this.read_I_I(targets.size());
         workerCount = this.init_I_I(targets.size());
         executor = Executors.newFixedThreadPool(workerCount, runnable -> {
            Thread worker = new Thread(runnable, "Autism-Proxy-Worker");
            worker.setDaemon(true);
            return worker;
         });
         this.init_ExecutorService = executor;
         this.init_AtomicBoolean.set(true);
         this.update(true);
      }

      Thread thread = new Thread(
         () -> this.init(generation, executor, targets, timeout, retryCount, workerCount, destinationHost, destinationPort), "Autism-Proxy-Refresh"
      );
      thread.setDaemon(true);
      thread.start();
      return true;
   }

   public boolean update_Z() {
      ExecutorService executor;
      synchronized (this) {
         if (!this.init_AtomicBoolean.get()) {
            return false;
         }

         this.load_AtomicBoolean.set(true);
         this.init_AtomicBoolean.set(false);
         this.read_J = System.currentTimeMillis() + 800L;
         executor = this.init_ExecutorService;
         this.init_ExecutorService = null;
         List<bW> active = this.load_List;
         this.load_List = List.of();

         for (bW proxy : active) {
            if (proxy.init_bW_CachePool == bW.Inner_CachePool.load) {
               proxy.init_bW_CachePool = bW.Inner_CachePool.init;
               proxy.init_J = 0L;
            }
         }

         if (this.init_Z_2) {
            this.close_V();
            this.flush_V();
            this.update_V();
         }

         this.update(true);
      }

      if (executor != null) {
         executor.shutdownNow();
      }

      return true;
   }

   public boolean init_Z_File(File file) {
      return file != null && file.isFile() ? this.init_Z_bY_CachePool(() -> new BufferedReader(new FileReader(file))) : false;
   }

   public boolean init_Z_Str(String text) {
      return text != null && !text.isBlank() ? this.init_Z_bY_CachePool(() -> new BufferedReader(new StringReader(text))) : false;
   }

   private boolean init_Z_bY_CachePool(bY.Inner_CachePool source) {
      long generation;
      synchronized (this) {
         if (this.read_AtomicBoolean.get() || this.init_AtomicBoolean.get()) {
            return false;
         }

         generation = this.write_J + 1L;
         this.write_J = generation;
         this.close = -9223372036854775808L;
         this.write_AtomicBoolean.set(false);
         this.sync_J = 0L;
         this.read_AtomicInteger.set(0);
         this.write_AtomicInteger.set(0);
         this.update_AtomicInteger.set(0);
         this.read_AtomicBoolean.set(true);
         this.write(true);
      }

      Thread thread = new Thread(() -> this.init_V_J_bY_CachePool(generation, source), "Autism-Proxy-Import");
      thread.setDaemon(true);
      this.init_Thread = thread;
      thread.start();
      return true;
   }

   public boolean sync_Z() {
      Thread thread;
      synchronized (this) {
         if (!this.read_AtomicBoolean.get()) {
            return false;
         }

         this.write_AtomicBoolean.set(true);
         this.read_AtomicBoolean.set(false);
         this.close = this.write_J;
         this.sync_J = System.currentTimeMillis() + 800L;
         thread = this.init_Thread;
         this.init_Thread = null;
         this.write(true);
      }

      if (thread != null) {
         thread.interrupt();
      }

      return true;
   }

   private void init_V_J_bY_CachePool(long generation, bY.Inner_CachePool source) {
      List<bW> parsed = new ArrayList<>();
      Set<String> knownKeys;
      synchronized (this) {
         knownKeys = new HashSet<>(this.init_List.size() + 1024);

         for (bW proxy : this.init_List) {
            knownKeys.add(init_Str_bW(proxy));
         }
      }

      try {
         String line;
         try (BufferedReader reader = source.open()) {
            for (; this.init_Z_J(generation) && (line = reader.readLine()) != null; this.write(false)) {
               this.read_AtomicInteger.incrementAndGet();
               bW proxy = load_bW_Str(line.trim());
               if (proxy != null && proxy.init_Z() && knownKeys.add(init_Str_bW(proxy))) {
                  parsed.add(proxy);
                  this.write_AtomicInteger.set(parsed.size());
               }
            }
         } catch (Exception e) {
            if (this.init_Z_J(generation)) {
               AutismClientAddon.LOG.error("Failed to import proxies", e);
            }
         }
      } finally {
         this.init_V_J_List(generation, parsed);
      }
   }

   private synchronized void init_V_J_List(long generation, List<bW> parsed) {
      if (this.write_J == generation && this.read_AtomicBoolean.get()) {
         int added = 0;
         if (!this.write_AtomicBoolean.get() && parsed != null && !parsed.isEmpty()) {
            Set<String> knownKeys = new HashSet<>(this.init_List.size() + parsed.size() + 16);

            for (bW proxy : this.init_List) {
               knownKeys.add(init_Str_bW(proxy));
            }

            for (bW proxy : parsed) {
               if (knownKeys.add(init_Str_bW(proxy))) {
                  proxy.init_Z = false;
                  this.init_List.add(proxy);
                  added++;
               }
            }
         }

         this.update_AtomicInteger.set(added);
         this.read_AtomicBoolean.set(false);
         this.write_AtomicBoolean.set(false);
         this.init_Thread = null;
         if (added > 0) {
            this.flush_V();
            this.update_V();
            this.init_Z_Z(false);
         }

         this.write(true);
      }
   }

   private boolean init_Z_J(long generation) {
      return this.read_AtomicBoolean.get() && this.write_J == generation && !this.write_AtomicBoolean.get();
   }

   private void write(boolean force) {
      long now = System.currentTimeMillis();
      if (force || now - this.push >= 150L) {
         this.push = now;
         this.update_J++;
      }
   }

   private void init(
      long generation,
      ExecutorService executor,
      List<bY.Inner_NetHelper> targets,
      int timeout,
      int retryCount,
      int workerCount,
      String destinationHost,
      int destinationPort
   ) {
      boolean completed = false;

      try {
         AtomicInteger nextIndex = new AtomicInteger();

         for (int i = 0; i < workerCount && this.load_Z_J(generation); i++) {
            executor.execute(() -> {
               while (this.load_Z_J(generation)) {
                  int index = nextIndex.getAndIncrement();
                  if (index < targets.size()) {
                     this.init(generation, targets.get(index), timeout, retryCount, destinationHost, destinationPort);
                     continue;
                  }
                  break;
               }
            });
         }

         executor.shutdown();

         do {
            if (!this.load_Z_J(generation)) {
               executor.shutdownNow();
               return;
            }
         } while (!executor.awaitTermination(100L, TimeUnit.MILLISECONDS));

         completed = true;
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         executor.shutdownNow();
         return;
      } catch (RejectedExecutionException e) {
         if (this.load_Z_J(generation)) {
            AutismClientAddon.LOG.error("Proxy refresh rejected a check task", e);
         }

         executor.shutdownNow();
         return;
      } catch (RuntimeException e) {
         AutismClientAddon.LOG.error("Proxy refresh failed", e);
         executor.shutdownNow();
         return;
      } finally {
         this.init_V_J_Z(generation, completed && this.load_Z_J(generation));
      }
   }

   private void init(long generation, bY.Inner_NetHelper target, int timeout, int retryCount, String destinationHost, int destinationPort) {
      if (this.load_Z_J(generation)) {
         bY.Inner_ConfigStore probe = init(target.init_bW(), timeout, destinationHost, destinationPort);

         for (int attempts = 0;
            probe.init_bW_DataUtil().init_bW_CachePool() != bW.Inner_CachePool.write && attempts < retryCount && this.load_Z_J(generation);
            attempts++
         ) {
            probe = init(target.init_bW(), timeout, destinationHost, destinationPort);
         }

         this.init(generation, target, probe);
      }
   }

   private static bY.Inner_ConfigStore init(bW proxy, int timeout, String destinationHost, int destinationPort) {
      if (destinationHost != null && !destinationHost.isBlank() && destinationPort > 0) {
         ca.Inner_DataUtil verified = ca.init(proxy, destinationHost, destinationPort, timeout);
         bW.Inner_DataUtil result = verified.init_Z()
            ? new bW.Inner_DataUtil(bW.Inner_CachePool.write, verified.init_J(), 1)
            : new bW.Inner_DataUtil(bW.Inner_CachePool.read, 0L, 2);
         return new bY.Inner_ConfigStore(result, verified.init_bZ());
      } else {
         return new bY.Inner_ConfigStore(proxy.init_bW_DataUtil_I(timeout), null);
      }
   }

   public synchronized boolean init(bW proxy, bW.Inner_DataUtil result, bZ workingType) {
      if (proxy != null && result != null && !this.init_AtomicBoolean.get() && this.init_List.contains(proxy)) {
         if (result.init_bW_CachePool() == bW.Inner_CachePool.write && workingType != null) {
            proxy.init_bZ = workingType;
         }

         proxy.init_V_bW_DataUtil(result);
         this.close_V();
         this.flush_V();
         this.update_V();
         return true;
      } else {
         return false;
      }
   }

   private synchronized void init(long generation, bY.Inner_NetHelper target, bY.Inner_ConfigStore probe) {
      if (this.init_J_2 == generation && this.init_AtomicBoolean.get() && !this.load_AtomicBoolean.get()) {
         bW proxy = target.init_bW();
         if (this.init_List.contains(proxy) && target.init_Str().equals(init_Str_bW(proxy))) {
            if (probe.init_bW_DataUtil().init_bW_CachePool() == bW.Inner_CachePool.write && probe.init_bZ() != null) {
               proxy.init_bZ = probe.init_bZ();
            }

            proxy.init_V_bW_DataUtil(probe.init_bW_DataUtil());
         }

         this.init_AtomicInteger.incrementAndGet();
         this.update(false);
      }
   }

   private synchronized void init_V_J_Z(long generation, boolean completed) {
      if (this.init_J_2 == generation && this.init_AtomicBoolean.get()) {
         this.init_AtomicBoolean.set(false);
         this.load_AtomicBoolean.set(false);
         this.init_ExecutorService = null;
         this.load_List = List.of();
         if (completed && this.init_Z_2) {
            this.close_V();
         }

         if (completed && this.init_Z_2) {
            this.flush_V();
            this.update_V();
         }

         if (completed) {
            this.init_Z_Z(false);
         }

         this.update(true);
      }
   }

   private boolean load_Z_J(long generation) {
      return this.init_AtomicBoolean.get() && this.init_J_2 == generation && !this.load_AtomicBoolean.get();
   }

   private synchronized List<bY.Inner_StreamUtil> init_List_Z(boolean force) {
      long now = System.currentTimeMillis();
      List<bY.Inner_StreamUtil> targets = new ArrayList<>();

      for (bW proxy : this.init_List) {
         if (proxy != null && proxy.init_Z_J_Z(now, force)) {
            targets.add(new bY.Inner_StreamUtil(proxy, init_Str_bW(proxy)));
         }
      }

      return targets;
   }

   private void load(long generation, List<bY.Inner_StreamUtil> targets) {
      if (!AutismLiteVariant.enabled()) {
         try {
            Map<String, List<bY.Inner_StreamUtil>> byIp = new LinkedHashMap<>();
            List<bY.Inner_DataUtil> immediate = new ArrayList<>();

            for (bY.Inner_StreamUtil target : targets) {
               if (!this.read_Z_J(generation)) {
                  return;
               }

               bX.Inner_StreamUtil resolved = bX.init_bX_StreamUtil_Str(target.init_bW().read);
               if (resolved.init_bX_DataUtil() != null) {
                  immediate.add(new bY.Inner_DataUtil(target, resolved.init_bX_DataUtil()));
                  if (immediate.size() >= 100) {
                     this.read(generation, immediate);
                     immediate.clear();
                  }
               } else if (!resolved.init_Str().isBlank()) {
                  byIp.computeIfAbsent(resolved.init_Str(), ignored -> new ArrayList<>()).add(target);
               }
            }

            if (!immediate.isEmpty()) {
               this.read(generation, immediate);
            }

            List<String> batch = new ArrayList<>(100);

            for (String ip : byIp.keySet()) {
               if (!this.read_Z_J(generation)) {
                  return;
               }

               batch.add(ip);
               if (batch.size() >= 100) {
                  this.init(generation, batch, byIp);
                  batch.clear();
                  sync_V();
               }
            }

            if (!batch.isEmpty() && this.read_Z_J(generation)) {
               this.init(generation, batch, byIp);
            }
         } finally {
            this.init_V_J(generation);
         }
      }
   }

   private void init(long generation, List<String> batch, Map<String, List<bY.Inner_StreamUtil>> byIp) {
      Map<String, bX.Inner_DataUtil> results = bX.init_Map_List(List.copyOf(batch));
      List<bY.Inner_DataUtil> applications = new ArrayList<>();

      for (String ip : batch) {
         bX.Inner_DataUtil result = results.getOrDefault(ip, bX.Inner_DataUtil.init(ip, System.currentTimeMillis()));
         List<bY.Inner_StreamUtil> targets = byIp.get(ip);
         if (targets != null) {
            for (bY.Inner_StreamUtil target : targets) {
               applications.add(new bY.Inner_DataUtil(target, result));
            }
         }
      }

      this.read(generation, applications);
   }

   private synchronized void read(long generation, List<bY.Inner_DataUtil> applications) {
      if (this.flush_J == generation && applications != null && !applications.isEmpty()) {
         int changed = 0;

         for (bY.Inner_DataUtil application : applications) {
            bY.Inner_StreamUtil target = application.init_bY_StreamUtil();
            bW proxy = target.init_bW();
            if (this.init_List.contains(proxy) && target.init_Str().equals(init_Str_bW(proxy))) {
               proxy.init_V_bX_DataUtil(application.init_bX_DataUtil());
               changed++;
            }
         }

         if (changed > 0) {
            this.flush_V();
            this.update_V();
         }
      }
   }

   private void init_V_J(long generation) {
      boolean runAgain = false;
      synchronized (this) {
         if (this.flush_J == generation) {
            runAgain = this.flush_AtomicBoolean.getAndSet(false);
            this.sync_AtomicBoolean.set(false);
         }
      }

      if (runAgain) {
         this.init_Z_Z(false);
      }
   }

   private boolean read_Z_J(long generation) {
      return this.sync_AtomicBoolean.get() && this.flush_J == generation;
   }

   private static void sync_V() {
      try {
         Thread.sleep(120L);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   private void update(boolean force) {
      long now = System.currentTimeMillis();
      if (force || now - this.poll >= 150L || this.init_AtomicInteger.get() >= this.load_AtomicInteger.get()) {
         this.poll = now;
         this.load_J++;
      }
   }

   private void flush_V() {
      this.open++;
      this.load_J++;
   }

   private int init_I_I(int targetCount) {
      int configured = Math.max(1, this.load_I);
      int floor = targetCount >= 2000 ? 256 : (targetCount >= 500 ? 128 : (targetCount >= 100 ? 64 : (targetCount >= 32 ? 32 : configured)));
      return Math.max(1, Math.min(targetCount, Math.max(configured, floor)));
   }

   private int load_I_I(int targetCount) {
      int configured = Math.max(1, this.init_I);
      if (targetCount >= 500) {
         return Math.min(configured, 2500);
      } else {
         return targetCount >= 100 ? Math.min(configured, 3000) : configured;
      }
   }

   private int read_I_I(int targetCount) {
      return Math.max(1, targetCount >= 100 ? 0 : Math.max(0, this.read_I));
   }

   private static String init_Str_bW(bW proxy) {
      return proxy == null ? "" : (proxy.init_bZ == null ? "" : proxy.init_bZ.name()) + "\u0000" + proxy.read + "\u0000" + proxy.init_I;
   }

   public synchronized void init_V_2() {
      if (!this.init_AtomicBoolean.get() && !this.read_AtomicBoolean.get()) {
         int before = this.init_List.size();
         this.init_List.removeIf(proxy -> this.load_Z && proxy.init_bW_CachePool == bW.Inner_CachePool.read);
         this.init_List.removeIf(proxy -> this.write_I > 0 && proxy.init_bW_CachePool == bW.Inner_CachePool.write && proxy.init_J >= this.write_I);
         List<bW> sorted = new ArrayList<>(this.init_List);
         sorted.sort(bY::init_I_bW_bW);
         if (this.update_I > 0 && sorted.size() > this.update_I) {
            sorted.subList(this.update_I, sorted.size()).clear();
            this.init_List.removeIf(proxy -> !sorted.contains(proxy));
         }

         if (this.init_Z_2) {
            this.init_List.clear();
            this.init_List.addAll(sorted);
         }

         if (before != this.init_List.size() || this.init_Z_2) {
            this.flush_V();
         }

         this.update_V();
      }
   }

   public int init_I_File(File file) {
      List<bW> parsed = new ArrayList<>();
      int added = 0;
      Set<String> knownKeys;
      synchronized (this) {
         knownKeys = new HashSet<>(this.init_List.size() + 1024);

         for (bW proxy : this.init_List) {
            knownKeys.add(init_Str_bW(proxy));
         }
      }

      String line;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         while ((line = reader.readLine()) != null) {
            bW proxy = load_bW_Str(line.trim());
            if (proxy != null && proxy.init_Z() && knownKeys.add(init_Str_bW(proxy))) {
               parsed.add(proxy);
            }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("Failed to import proxies", e);
      }

      if (parsed.isEmpty()) {
         return 0;
      }

      synchronized (this) {
         Set<String> currentKeys = new HashSet<>(this.init_List.size() + parsed.size() + 16);

         for (bW proxy : this.init_List) {
            currentKeys.add(init_Str_bW(proxy));
         }

         for (bW proxy : parsed) {
            if (currentKeys.add(init_Str_bW(proxy))) {
               if (this.init_List.isEmpty() && added == 0) {
                  proxy.init_Z = true;
               }

               this.init_List.add(proxy);
               added++;
            }
         }

         if (added > 0) {
            this.flush_V();
            this.update_V();
            this.init_Z_Z(false);
         }

         return added;
      }
   }

   private void close_V() {
      this.init_List.sort(bY::init_I_bW_bW);
   }

   private static int init_I_bW_bW(bW a, bW b) {
      boolean aliveA = a != null && a.init_bW_CachePool == bW.Inner_CachePool.write;
      boolean aliveB = b != null && b.init_bW_CachePool == bW.Inner_CachePool.write;
      if (aliveA != aliveB) {
         return aliveA ? -1 : 1;
      } else {
         return aliveA ? Long.compare(a.init_J, b.init_J) : 0;
      }
   }

   private static bW load_bW_Str(String line) {
      if (!line.isBlank() && !line.startsWith("#")) {
         Matcher m = init_Pattern.matcher(line);
         if (m.find()) {
            return init(m.group(1), init_Str_Str(m.group(2)), Integer.parseInt(m.group(3)), m.group(4), bZ.init);
         }

         m = load_Pattern.matcher(line);
         if (m.find()) {
            bW proxy = init(null, init_Str_Str(m.group(1)), Integer.parseInt(m.group(2)), null, bZ.load);
            if (m.group(3) != null) {
               proxy.write = m.group(3);
            }

            if (m.group(4) != null) {
               proxy.update = m.group(4);
            }

            return proxy;
         } else {
            m = read_Pattern.matcher(line);
            if (!m.find()) {
               return null;
            }

            String typeName = m.group("type");
            bZ defaultType = m.group("pass") == null && !"socks".equals(typeName) ? bZ.init : bZ.load;
            bW proxy = init(null, init_Str_Str(m.group("addr")), Integer.parseInt(m.group("port")), typeName, defaultType);
            if (m.group("user") != null) {
               proxy.write = m.group("user");
            }

            if (m.group("pass") != null) {
               proxy.update = m.group("pass");
            }

            return proxy;
         }
      } else {
         return null;
      }
   }

   private static String init_Str_Str(String address) {
      return address == null ? "" : address.replaceAll("\\b0+\\B", "");
   }

   private static bW init(String name, String address, int port, String typeName, bZ defaultType) {
      bW proxy = new bW();
      proxy.load_Str = name == null ? "" : name.trim();
      proxy.read = address;
      proxy.init_I = port;
      proxy.init_bZ = defaultType == null ? bZ.load : defaultType;
      if (typeName != null) {
         String lower = typeName.toLowerCase();
         if (lower.contains("4")) {
            proxy.init_bZ = bZ.init;
         } else if (lower.contains("5")) {
            proxy.init_bZ = bZ.load;
         }
      }

      return proxy;
   }

   @Override
   public Iterator<bW> iterator() {
      return this.init_List().iterator();
   }

   public record Inner_BufferHelper(boolean init_Z, boolean load_Z, int init_I, int load_I, int read_I, long init_J, long load_J, boolean read_Z) {
   }

   private interface Inner_CachePool {
      BufferedReader open() throws IOException;
   }

   private record Inner_ConfigStore(bW.Inner_DataUtil init_bW_DataUtil, bZ init_bZ) {
   }

   private record Inner_DataUtil(bY.Inner_StreamUtil init_bY_StreamUtil, bX.Inner_DataUtil init_bX_DataUtil) {
   }

   private record Inner_NetHelper(bW init_bW, String init_Str) {
   }

   private record Inner_StreamUtil(bW init_bW, String init_Str) {
   }

   public record Inner_TaskQueue(boolean init_Z, boolean load_Z, int init_I, int load_I, long init_J, long load_J) {
   }
}
