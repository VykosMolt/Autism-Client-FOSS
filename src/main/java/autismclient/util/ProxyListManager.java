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

public final class ProxyListManager extends AtomicNbtListStore<bW> implements Iterable<bW> {
   private static final ProxyListManager INSTANCE = new ProxyListManager();
   private final AtomicBoolean refreshRunning = new AtomicBoolean(false);
   private final AtomicBoolean refreshCancelRequested = new AtomicBoolean(false);
   private final AtomicInteger refreshCheckedCount = new AtomicInteger();
   private final AtomicInteger refreshTotalCount = new AtomicInteger();
   private final AtomicBoolean importRunning = new AtomicBoolean(false);
   private final AtomicBoolean importCancelRequested = new AtomicBoolean(false);
   private final AtomicInteger importScannedCount = new AtomicInteger();
   private final AtomicInteger importParsedCount = new AtomicInteger();
   private final AtomicInteger importAddedCount = new AtomicInteger();
   private final AtomicBoolean saveWorkerRunning = new AtomicBoolean(false);
   private final AtomicBoolean geoLookupRunning = new AtomicBoolean(false);
   private final AtomicBoolean geoLookupRerunPending = new AtomicBoolean(false);
   private int timeoutMs = 3000;
   private int workerThreads = 64;
   private int retryCount = 0;
   private boolean sortByLatency = true;
   private boolean pruneDead = true;
   private volatile boolean auditGeoLookup;
   private int pruneLatencyMs = 2000;
   private int pruneToCount = 0;
   private volatile long refreshGeneration;
   private volatile long refreshRevision;
   private volatile long refreshCancelNoticeUntilMs;
   private volatile long importGeneration;
   private volatile long importRevision;
   private volatile long importCancelNoticeUntilMs;
   private volatile long geoLookupGeneration;
   private volatile long canceledImportGeneration = -9223372036854775808L;
   private volatile long listRevision;
   private volatile long lastRefreshNotifyMs;
   private volatile long lastImportNotifyMs;
   private volatile boolean savePending;
   private volatile ExecutorService refreshExecutor;
   private volatile Thread importThread;
   private volatile List<bW> activeRefreshProxies = List.of();
   private static final Pattern NAMED_OR_HOST_PORT_PATTERN = Pattern.compile(
      "^(?:([\\w\\s]+)=)?((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])(?i:@(socks[45]))?$",
      8
   );
   private static final Pattern HOST_PORT_AUTH_PATTERN = Pattern.compile(
      "^((?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]):([^:]+)(?::(.+))?$",
      8
   );
   private static final Pattern URI_PROXY_PATTERN = Pattern.compile(
      "^(?:(?<type>socks|socks4|socks5)://)?(?:(?<user>[\\w~-]+)(:(?<pass>[\\w~-]+))?@)?(?<addr>(?:0*(?:\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(?:\\.(?!:)|)){4}):(?!0)(?<port>\\d{1,4}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$",
      8
   );
   private static final int GEO_BATCH_SIZE = 100;

   private ProxyListManager() {
   }

   public static ProxyListManager getInstance() {
      INSTANCE.ensureLoaded();
      INSTANCE.normalizeLoadedProxies();
      return INSTANCE;
   }

   private synchronized void normalizeLoadedProxies() {
      boolean changed = false;

      for (bW proxy : this.items) {
         if (proxy != null) {
            proxy.write();
            changed |= proxy.load_Z;
            proxy.load_Z = false;
         }
      }

      if (changed) {
         this.save();
      }
   }

   public synchronized bW findById(String id) {
      if (id != null && !id.isBlank()) {
         for (bW proxy : this.items) {
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
   protected File getStoreFile() {
      return new File(Minecraft.getInstance().gameDirectory, "autism-proxies.nbt");
   }

   @Override
   protected String getListTagKey() {
      return "proxies";
   }

   @Override
   protected bW deserializeItem(CompoundTag tag) {
      return new bW().init_bW_CompoundTag(tag);
   }

   @Override
   protected CompoundTag serializeItem(bW item) {
      return item.init_CompoundTag();
   }

   @Override
   protected String getStoreLabel() {
      return "Autism proxies";
   }

   @Override
   protected void readMetadata(CompoundTag tag) {
      this.timeoutMs = tag.getIntOr("timeoutMs", this.timeoutMs);
      this.workerThreads = tag.getIntOr("threads", this.workerThreads);
      this.retryCount = tag.getIntOr("retries", this.retryCount);
      this.sortByLatency = tag.getBooleanOr("sortByLatency", this.sortByLatency);
      this.pruneDead = tag.getBooleanOr("pruneDead", this.pruneDead);
      this.pruneLatencyMs = tag.getIntOr("pruneLatency", this.pruneLatencyMs);
      this.pruneToCount = tag.getIntOr("pruneToCount", this.pruneToCount);
      this.auditGeoLookup = tag.getBooleanOr("geoLookup", false);
   }

   @Override
   protected void writeMetadata(CompoundTag tag) {
      tag.putInt("timeoutMs", this.timeoutMs);
      tag.putInt("threads", this.workerThreads);
      tag.putInt("retries", this.retryCount);
      tag.putBoolean("sortByLatency", this.sortByLatency);
      tag.putBoolean("pruneDead", this.pruneDead);
      tag.putInt("pruneLatency", this.pruneLatencyMs);
      tag.putInt("pruneToCount", this.pruneToCount);
      tag.putBoolean("geoLookup", this.auditGeoLookup);
   }

   private void scheduleSave() {
      this.savePending = true;
      if (this.saveWorkerRunning.compareAndSet(false, true)) {
         Thread thread = new Thread(() -> {
            try {
               while (true) {
                  this.savePending = false;
                  this.save();
                  if (!this.savePending) {
                     break;
                  }
               }
            } finally {
               this.saveWorkerRunning.set(false);
               if (this.savePending) {
                  this.scheduleSave();
               }
            }
         }, "Autism-Proxy-Save");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public synchronized int countEnabledValidProxies() {
      int count = 0;

      for (bW proxy : this.items) {
         if (proxy.init_Z && proxy.init_Z()) {
            count++;
         }
      }

      return count;
   }

   public long getListRevision() {
      return this.listRevision;
   }

   public synchronized boolean addProxy(bW proxy) {
      if (proxy != null && proxy.init_Z() && !this.items.contains(proxy)) {
         if (this.items.isEmpty()) {
            proxy.init_Z = true;
         }

         this.items.add(proxy);
         this.bumpListRevision();
         this.scheduleSave();
         this.startGeoLookup(false);
         return true;
      } else {
         return false;
      }
   }

   public synchronized void removeProxy(bW proxy) {
      if (this.items.remove(proxy)) {
         this.bumpListRevision();
         this.scheduleSave();
      }
   }

   public synchronized int clearAllProxies() {
      if (!this.refreshRunning.get() && !this.importRunning.get() && !this.items.isEmpty()) {
         int removed = this.items.size();
         this.items.clear();
         this.bumpListRevision();
         this.scheduleSave();
         return removed;
      } else {
         return 0;
      }
   }

   public synchronized boolean updateProxy(bW existing, bW updated) {
      if (existing != null && updated != null && updated.init_Z()) {
         int index = -1;

         for (int i = 0; i < this.items.size(); i++) {
            bW proxy = this.items.get(i);
            if (proxy == existing) {
               index = i;
               break;
            }
         }

         if (index < 0) {
            return false;
         }

         for (int i = 0; i < this.items.size(); i++) {
            if (i != index && this.items.get(i).equals(updated)) {
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
            existing.init_bW_CachePool = bW.Inner_CachePool.UNCHECKED;
            existing.init_J = 0L;
            existing.init_V();
         }

         this.bumpListRevision();
         this.scheduleSave();
         if (identityChanged) {
            this.startGeoLookup(false);
         }

         return true;
      } else {
         return false;
      }
   }

   public synchronized void setProxyEnabled(bW proxy, boolean enabled) {
      for (bW current : this.items) {
         current.init_Z = false;
      }

      if (proxy != null) {
         proxy.init_Z = enabled;
      }

      this.bumpListRevision();
      this.scheduleSave();
   }

   public synchronized bW getActiveProxy() {
      for (bW proxy : this.items) {
         if (proxy.init_Z && proxy.init_Z()) {
            return proxy;
         }
      }

      return null;
   }

   public boolean isRefreshing() {
      return this.refreshRunning.get();
   }

   public ProxyListManager.Inner_TaskQueue getRefreshStatus() {
      boolean running = this.refreshRunning.get();
      boolean canceling = !running && System.currentTimeMillis() < this.refreshCancelNoticeUntilMs;
      return new ProxyListManager.Inner_TaskQueue(running, canceling, this.refreshCheckedCount.get(), this.refreshTotalCount.get(), this.refreshGeneration, this.refreshRevision);
   }

   public ProxyListManager.Inner_BufferHelper getImportStatus() {
      boolean running = this.importRunning.get();
      boolean canceling = !running && System.currentTimeMillis() < this.importCancelNoticeUntilMs;
      boolean canceled = !running && this.importGeneration == this.canceledImportGeneration;
      return new ProxyListManager.Inner_BufferHelper(
         running,
         canceling,
         this.importScannedCount.get(),
         this.importParsedCount.get(),
         this.importAddedCount.get(),
         this.importGeneration,
         this.importRevision,
         canceled
      );
   }

   public int getTimeoutMs() {
      return this.timeoutMs;
   }

   public void setTimeoutMs(int v) {
      this.timeoutMs = Math.max(1, v);
      this.scheduleSave();
   }

   public int getWorkerThreads() {
      return this.workerThreads;
   }

   public void setWorkerThreads(int v) {
      this.workerThreads = Math.max(1, v);
      this.scheduleSave();
   }

   public int getRetryCount() {
      return this.retryCount;
   }

   public void setRetryCount(int v) {
      this.retryCount = Math.max(0, v);
      this.scheduleSave();
   }

   public boolean isSortByLatency() {
      return this.sortByLatency;
   }

   public synchronized void setSortByLatency(boolean v) {
      this.sortByLatency = v;
      if (v) {
         this.sortByLatencyInternal();
      }

      this.bumpListRevision();
      this.scheduleSave();
   }

   public boolean isPruneDead() {
      return this.pruneDead;
   }

   public void setPruneDead(boolean v) {
      this.pruneDead = v;
      this.scheduleSave();
   }

   public int getPruneLatencyMs() {
      return this.pruneLatencyMs;
   }

   public void setPruneLatencyMs(int v) {
      this.pruneLatencyMs = Math.max(0, v);
      this.scheduleSave();
   }

   public int getPruneToCount() {
      return this.pruneToCount;
   }

   public void setPruneToCount(int v) {
      this.pruneToCount = Math.max(0, v);
      this.scheduleSave();
   }

   public synchronized boolean sortNow() {
      if (!this.refreshRunning.get() && !this.importRunning.get() && this.items.size() >= 2) {
         this.sortByLatencyInternal();
         this.bumpListRevision();
         this.scheduleSave();
         return true;
      } else {
         return false;
      }
   }

   public void refreshAsync(boolean all) {
      this.startRefresh(all);
   }

   public boolean auditGeoLookup() {
      return this.auditGeoLookup;
   }

   public void auditSetGeoLookup(boolean v) {
      this.auditGeoLookup = v;
      this.scheduleSave();
      if (v) {
         this.startGeoLookup(false);
      }
   }

   public boolean startGeoLookup(boolean force) {
      if (!this.auditGeoLookup) {
         return false;
      }

      this.ensureLoaded();
      if (!this.geoLookupRunning.compareAndSet(false, true)) {
         this.geoLookupRerunPending.set(true);
         return false;
      }

      List<ProxyListManager.Inner_StreamUtil> targets;
      long generation;
      synchronized (this) {
         targets = this.collectGeoTargets(force);
         if (targets.isEmpty()) {
            this.geoLookupRunning.set(false);
            return false;
         }

         generation = this.geoLookupGeneration + 1L;
         this.geoLookupGeneration = generation;
         long now = System.currentTimeMillis();

         for (ProxyListManager.Inner_StreamUtil target : targets) {
            target.proxy().init_V_J(now);
         }

         this.bumpListRevision();
      }

      Thread thread = new Thread(() -> this.runGeoLookupWorker(generation, targets), "Autism-Proxy-Geo");
      thread.setDaemon(true);
      thread.start();
      return true;
   }

   public boolean startRefresh(boolean all) {
      return this.beginRefresh(all, "", 0);
   }

   public boolean startRefreshForTarget(boolean all, String host, int port) {
      String destination = host == null ? "" : host.trim();
      return !destination.isBlank() && port > 0 && port <= 65535 ? this.beginRefresh(all, destination, port) : false;
   }

   private boolean beginRefresh(boolean all, String destinationHost, int destinationPort) {
      List<ProxyListManager.Inner_NetHelper> targets = new ArrayList<>();
      List<bW> active = new ArrayList<>();
      ExecutorService executor;
      long generation;
      int timeout;
      int retryCount;
      int workerCount;
      synchronized (this) {
         if (this.refreshRunning.get() || this.importRunning.get() || this.items.isEmpty()) {
            return false;
         }

         for (bW proxy : this.items) {
            if (proxy != null && proxy.init_Z() && (all || proxy.init_bW_CachePool == bW.Inner_CachePool.UNCHECKED)) {
               targets.add(new ProxyListManager.Inner_NetHelper(proxy, buildIdentityKey(proxy)));
               active.add(proxy);
            }
         }

         if (targets.isEmpty()) {
            return false;
         }

         generation = this.refreshGeneration + 1L;
         this.refreshGeneration = generation;
         this.refreshCancelRequested.set(false);
         this.refreshCancelNoticeUntilMs = 0L;
         this.refreshCheckedCount.set(0);
         this.refreshTotalCount.set(targets.size());
         this.activeRefreshProxies = List.copyOf(active);

         for (bW proxy : active) {
            proxy.init_bW_CachePool = bW.Inner_CachePool.CHECKING;
            proxy.init_J = 0L;
         }

         timeout = this.resolveEffectiveTimeoutMs(targets.size());
         retryCount = this.resolveEffectiveRetries(targets.size());
         workerCount = this.resolveEffectiveWorkerCount(targets.size());
         executor = Executors.newFixedThreadPool(workerCount, runnable -> {
            Thread worker = new Thread(runnable, "Autism-Proxy-Worker");
            worker.setDaemon(true);
            return worker;
         });
         this.refreshExecutor = executor;
         this.refreshRunning.set(true);
         this.bumpRefreshRevision(true);
      }

      Thread thread = new Thread(
         () -> this.runRefreshPool(generation, executor, targets, timeout, retryCount, workerCount, destinationHost, destinationPort), "Autism-Proxy-Refresh"
      );
      thread.setDaemon(true);
      thread.start();
      return true;
   }

   public boolean cancelRefresh() {
      ExecutorService executor;
      synchronized (this) {
         if (!this.refreshRunning.get()) {
            return false;
         }

         this.refreshCancelRequested.set(true);
         this.refreshRunning.set(false);
         this.refreshCancelNoticeUntilMs = System.currentTimeMillis() + 800L;
         executor = this.refreshExecutor;
         this.refreshExecutor = null;
         List<bW> active = this.activeRefreshProxies;
         this.activeRefreshProxies = List.of();

         for (bW proxy : active) {
            if (proxy.init_bW_CachePool == bW.Inner_CachePool.CHECKING) {
               proxy.init_bW_CachePool = bW.Inner_CachePool.UNCHECKED;
               proxy.init_J = 0L;
            }
         }

         if (this.sortByLatency) {
            this.sortByLatencyInternal();
            this.bumpListRevision();
            this.scheduleSave();
         }

         this.bumpRefreshRevision(true);
      }

      if (executor != null) {
         executor.shutdownNow();
      }

      return true;
   }

   public boolean importFromFileAsync(File file) {
      return file != null && file.isFile() ? this.startImportAsync(() -> new BufferedReader(new FileReader(file))) : false;
   }

   public boolean importFromTextAsync(String text) {
      return text != null && !text.isBlank() ? this.startImportAsync(() -> new BufferedReader(new StringReader(text))) : false;
   }

   private boolean startImportAsync(ProxyListManager.Inner_CachePool source) {
      long generation;
      synchronized (this) {
         if (this.importRunning.get() || this.refreshRunning.get()) {
            return false;
         }

         generation = this.importGeneration + 1L;
         this.importGeneration = generation;
         this.canceledImportGeneration = -9223372036854775808L;
         this.importCancelRequested.set(false);
         this.importCancelNoticeUntilMs = 0L;
         this.importScannedCount.set(0);
         this.importParsedCount.set(0);
         this.importAddedCount.set(0);
         this.importRunning.set(true);
         this.bumpImportRevision(true);
      }

      Thread thread = new Thread(() -> this.runImportWorker(generation, source), "Autism-Proxy-Import");
      thread.setDaemon(true);
      this.importThread = thread;
      thread.start();
      return true;
   }

   public boolean cancelImport() {
      Thread thread;
      synchronized (this) {
         if (!this.importRunning.get()) {
            return false;
         }

         this.importCancelRequested.set(true);
         this.importRunning.set(false);
         this.canceledImportGeneration = this.importGeneration;
         this.importCancelNoticeUntilMs = System.currentTimeMillis() + 800L;
         thread = this.importThread;
         this.importThread = null;
         this.bumpImportRevision(true);
      }

      if (thread != null) {
         thread.interrupt();
      }

      return true;
   }

   private void runImportWorker(long generation, ProxyListManager.Inner_CachePool source) {
      List<bW> parsed = new ArrayList<>();
      Set<String> knownKeys;
      synchronized (this) {
         knownKeys = new HashSet<>(this.items.size() + 1024);

         for (bW proxy : this.items) {
            knownKeys.add(buildIdentityKey(proxy));
         }
      }

      try {
         String line;
         try (BufferedReader reader = source.open()) {
            for (; this.isImportActive(generation) && (line = reader.readLine()) != null; this.bumpImportRevision(false)) {
               this.importScannedCount.incrementAndGet();
               bW proxy = parseProxyLine(line.trim());
               if (proxy != null && proxy.init_Z() && knownKeys.add(buildIdentityKey(proxy))) {
                  parsed.add(proxy);
                  this.importParsedCount.set(parsed.size());
               }
            }
         } catch (Exception e) {
            if (this.isImportActive(generation)) {
               AutismClientAddon.LOG.error("Failed to import proxies", e);
            }
         }
      } finally {
         this.finishImport(generation, parsed);
      }
   }

   private synchronized void finishImport(long generation, List<bW> parsed) {
      if (this.importGeneration == generation && this.importRunning.get()) {
         int added = 0;
         if (!this.importCancelRequested.get() && parsed != null && !parsed.isEmpty()) {
            Set<String> knownKeys = new HashSet<>(this.items.size() + parsed.size() + 16);

            for (bW proxy : this.items) {
               knownKeys.add(buildIdentityKey(proxy));
            }

            for (bW proxy : parsed) {
               if (knownKeys.add(buildIdentityKey(proxy))) {
                  proxy.init_Z = false;
                  this.items.add(proxy);
                  added++;
               }
            }
         }

         this.importAddedCount.set(added);
         this.importRunning.set(false);
         this.importCancelRequested.set(false);
         this.importThread = null;
         if (added > 0) {
            this.bumpListRevision();
            this.scheduleSave();
            this.startGeoLookup(false);
         }

         this.bumpImportRevision(true);
      }
   }

   private boolean isImportActive(long generation) {
      return this.importRunning.get() && this.importGeneration == generation && !this.importCancelRequested.get();
   }

   private void bumpImportRevision(boolean force) {
      long now = System.currentTimeMillis();
      if (force || now - this.lastImportNotifyMs >= 150L) {
         this.lastImportNotifyMs = now;
         this.importRevision++;
      }
   }

   private void runRefreshPool(
      long generation,
      ExecutorService executor,
      List<ProxyListManager.Inner_NetHelper> targets,
      int timeout,
      int retryCount,
      int workerCount,
      String destinationHost,
      int destinationPort
   ) {
      boolean completed = false;

      try {
         AtomicInteger nextIndex = new AtomicInteger();

         for (int i = 0; i < workerCount && this.isRefreshActive(generation); i++) {
            executor.execute(() -> {
               while (this.isRefreshActive(generation)) {
                  int index = nextIndex.getAndIncrement();
                  if (index < targets.size()) {
                     this.checkRefreshTarget(generation, targets.get(index), timeout, retryCount, destinationHost, destinationPort);
                     continue;
                  }
                  break;
               }
            });
         }

         executor.shutdown();

         do {
            if (!this.isRefreshActive(generation)) {
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
         if (this.isRefreshActive(generation)) {
            AutismClientAddon.LOG.error("Proxy refresh rejected a check task", e);
         }

         executor.shutdownNow();
         return;
      } catch (RuntimeException e) {
         AutismClientAddon.LOG.error("Proxy refresh failed", e);
         executor.shutdownNow();
         return;
      } finally {
         this.finishRefresh(generation, completed && this.isRefreshActive(generation));
      }
   }

   private void checkRefreshTarget(long generation, ProxyListManager.Inner_NetHelper target, int timeout, int retryCount, String destinationHost, int destinationPort) {
      if (this.isRefreshActive(generation)) {
         ProxyListManager.Inner_ConfigStore probe = probeProxy(target.proxy(), timeout, destinationHost, destinationPort);

         for (int attempts = 0;
            probe.checkResult().init_bW_CachePool() != bW.Inner_CachePool.ALIVE && attempts < retryCount && this.isRefreshActive(generation);
            attempts++
         ) {
            probe = probeProxy(target.proxy(), timeout, destinationHost, destinationPort);
         }

         this.applyRefreshProbeResult(generation, target, probe);
      }
   }

   private static ProxyListManager.Inner_ConfigStore probeProxy(bW proxy, int timeout, String destinationHost, int destinationPort) {
      if (destinationHost != null && !destinationHost.isBlank() && destinationPort > 0) {
         ProxyConnectivityTester.Inner_DataUtil verified = ProxyConnectivityTester.init(proxy, destinationHost, destinationPort, timeout);
         bW.Inner_DataUtil result = verified.init_Z()
            ? new bW.Inner_DataUtil(bW.Inner_CachePool.ALIVE, verified.init_J(), 1)
            : new bW.Inner_DataUtil(bW.Inner_CachePool.DEAD, 0L, 2);
         return new ProxyListManager.Inner_ConfigStore(result, verified.init_bZ());
      } else {
         return new ProxyListManager.Inner_ConfigStore(proxy.init_bW_DataUtil_I(timeout), null);
      }
   }

   public synchronized boolean applyManualCheckResult(bW proxy, bW.Inner_DataUtil result, ProxyProtocolType workingType) {
      if (proxy != null && result != null && !this.refreshRunning.get() && this.items.contains(proxy)) {
         if (result.init_bW_CachePool() == bW.Inner_CachePool.ALIVE && workingType != null) {
            proxy.init_bZ = workingType;
         }

         proxy.init_V_bW_DataUtil(result);
         this.sortByLatencyInternal();
         this.bumpListRevision();
         this.scheduleSave();
         return true;
      } else {
         return false;
      }
   }

   private synchronized void applyRefreshProbeResult(long generation, ProxyListManager.Inner_NetHelper target, ProxyListManager.Inner_ConfigStore probe) {
      if (this.refreshGeneration == generation && this.refreshRunning.get() && !this.refreshCancelRequested.get()) {
         bW proxy = target.proxy();
         if (this.items.contains(proxy) && target.identityKey().equals(buildIdentityKey(proxy))) {
            if (probe.checkResult().init_bW_CachePool() == bW.Inner_CachePool.ALIVE && probe.detectedProtocol() != null) {
               proxy.init_bZ = probe.detectedProtocol();
            }

            proxy.init_V_bW_DataUtil(probe.checkResult());
         }

         this.refreshCheckedCount.incrementAndGet();
         this.bumpRefreshRevision(false);
      }
   }

   private synchronized void finishRefresh(long generation, boolean completed) {
      if (this.refreshGeneration == generation && this.refreshRunning.get()) {
         this.refreshRunning.set(false);
         this.refreshCancelRequested.set(false);
         this.refreshExecutor = null;
         this.activeRefreshProxies = List.of();
         if (completed && this.sortByLatency) {
            this.sortByLatencyInternal();
         }

         if (completed && this.sortByLatency) {
            this.bumpListRevision();
            this.scheduleSave();
         }

         if (completed) {
            this.startGeoLookup(false);
         }

         this.bumpRefreshRevision(true);
      }
   }

   private boolean isRefreshActive(long generation) {
      return this.refreshRunning.get() && this.refreshGeneration == generation && !this.refreshCancelRequested.get();
   }

   private synchronized List<ProxyListManager.Inner_StreamUtil> collectGeoTargets(boolean force) {
      long now = System.currentTimeMillis();
      List<ProxyListManager.Inner_StreamUtil> targets = new ArrayList<>();

      for (bW proxy : this.items) {
         if (proxy != null && proxy.init_Z_J_Z(now, force)) {
            targets.add(new ProxyListManager.Inner_StreamUtil(proxy, buildIdentityKey(proxy)));
         }
      }

      return targets;
   }

   private void runGeoLookupWorker(long generation, List<ProxyListManager.Inner_StreamUtil> targets) {
      if (!AutismLiteVariant.enabled()) {
         try {
            Map<String, List<ProxyListManager.Inner_StreamUtil>> byIp = new LinkedHashMap<>();
            List<ProxyListManager.Inner_DataUtil> immediate = new ArrayList<>();

            for (ProxyListManager.Inner_StreamUtil target : targets) {
               if (!this.isGeoLookupActive(generation)) {
                  return;
               }

               bX.Inner_StreamUtil resolved = bX.init_bX_StreamUtil_Str(target.proxy().read);
               if (resolved.init_bX_DataUtil() != null) {
                  immediate.add(new ProxyListManager.Inner_DataUtil(target, resolved.init_bX_DataUtil()));
                  if (immediate.size() >= 100) {
                     this.applyGeoResults(generation, immediate);
                     immediate.clear();
                  }
               } else if (!resolved.init_Str().isBlank()) {
                  byIp.computeIfAbsent(resolved.init_Str(), ignored -> new ArrayList<>()).add(target);
               }
            }

            if (!immediate.isEmpty()) {
               this.applyGeoResults(generation, immediate);
            }

            List<String> batch = new ArrayList<>(100);

            for (String ip : byIp.keySet()) {
               if (!this.isGeoLookupActive(generation)) {
                  return;
               }

               batch.add(ip);
               if (batch.size() >= 100) {
                  this.fetchGeoBatch(generation, batch, byIp);
                  batch.clear();
                  sleepBetweenGeoBatches();
               }
            }

            if (!batch.isEmpty() && this.isGeoLookupActive(generation)) {
               this.fetchGeoBatch(generation, batch, byIp);
            }
         } finally {
            this.finishGeoLookup(generation);
         }
      }
   }

   private void fetchGeoBatch(long generation, List<String> batch, Map<String, List<ProxyListManager.Inner_StreamUtil>> byIp) {
      Map<String, bX.Inner_DataUtil> results = bX.init_Map_List(List.copyOf(batch));
      List<ProxyListManager.Inner_DataUtil> applications = new ArrayList<>();

      for (String ip : batch) {
         bX.Inner_DataUtil result = results.getOrDefault(ip, bX.Inner_DataUtil.init(ip, System.currentTimeMillis()));
         List<ProxyListManager.Inner_StreamUtil> targets = byIp.get(ip);
         if (targets != null) {
            for (ProxyListManager.Inner_StreamUtil target : targets) {
               applications.add(new ProxyListManager.Inner_DataUtil(target, result));
            }
         }
      }

      this.applyGeoResults(generation, applications);
   }

   private synchronized void applyGeoResults(long generation, List<ProxyListManager.Inner_DataUtil> applications) {
      if (this.geoLookupGeneration == generation && applications != null && !applications.isEmpty()) {
         int changed = 0;

         for (ProxyListManager.Inner_DataUtil application : applications) {
            ProxyListManager.Inner_StreamUtil target = application.target();
            bW proxy = target.proxy();
            if (this.items.contains(proxy) && target.identityKey().equals(buildIdentityKey(proxy))) {
               proxy.init_V_bX_DataUtil(application.geoResult());
               changed++;
            }
         }

         if (changed > 0) {
            this.bumpListRevision();
            this.scheduleSave();
         }
      }
   }

   private void finishGeoLookup(long generation) {
      boolean runAgain = false;
      synchronized (this) {
         if (this.geoLookupGeneration == generation) {
            runAgain = this.geoLookupRerunPending.getAndSet(false);
            this.geoLookupRunning.set(false);
         }
      }

      if (runAgain) {
         this.startGeoLookup(false);
      }
   }

   private boolean isGeoLookupActive(long generation) {
      return this.geoLookupRunning.get() && this.geoLookupGeneration == generation;
   }

   private static void sleepBetweenGeoBatches() {
      try {
         Thread.sleep(120L);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   private void bumpRefreshRevision(boolean force) {
      long now = System.currentTimeMillis();
      if (force || now - this.lastRefreshNotifyMs >= 150L || this.refreshCheckedCount.get() >= this.refreshTotalCount.get()) {
         this.lastRefreshNotifyMs = now;
         this.refreshRevision++;
      }
   }

   private void bumpListRevision() {
      this.listRevision++;
      this.refreshRevision++;
   }

   private int resolveEffectiveWorkerCount(int targetCount) {
      int configured = Math.max(1, this.workerThreads);
      int floor = targetCount >= 2000 ? 256 : (targetCount >= 500 ? 128 : (targetCount >= 100 ? 64 : (targetCount >= 32 ? 32 : configured)));
      return Math.max(1, Math.min(targetCount, Math.max(configured, floor)));
   }

   private int resolveEffectiveTimeoutMs(int targetCount) {
      int configured = Math.max(1, this.timeoutMs);
      if (targetCount >= 500) {
         return Math.min(configured, 2500);
      } else {
         return targetCount >= 100 ? Math.min(configured, 3000) : configured;
      }
   }

   private int resolveEffectiveRetries(int targetCount) {
      return Math.max(1, targetCount >= 100 ? 0 : Math.max(0, this.retryCount));
   }

   private static String buildIdentityKey(bW proxy) {
      return proxy == null ? "" : (proxy.init_bZ == null ? "" : proxy.init_bZ.name()) + "\u0000" + proxy.read + "\u0000" + proxy.init_I;
   }

   public synchronized void pruneAndSort() {
      if (!this.refreshRunning.get() && !this.importRunning.get()) {
         int before = this.items.size();
         this.items.removeIf(proxy -> this.pruneDead && proxy.init_bW_CachePool == bW.Inner_CachePool.DEAD);
         this.items.removeIf(proxy -> this.pruneLatencyMs > 0 && proxy.init_bW_CachePool == bW.Inner_CachePool.ALIVE && proxy.init_J >= this.pruneLatencyMs);
         List<bW> sorted = new ArrayList<>(this.items);
         sorted.sort(ProxyListManager::compareByLatency);
         if (this.pruneToCount > 0 && sorted.size() > this.pruneToCount) {
            sorted.subList(this.pruneToCount, sorted.size()).clear();
            this.items.removeIf(proxy -> !sorted.contains(proxy));
         }

         if (this.sortByLatency) {
            this.items.clear();
            this.items.addAll(sorted);
         }

         if (before != this.items.size() || this.sortByLatency) {
            this.bumpListRevision();
         }

         this.scheduleSave();
      }
   }

   public int importFromFileSync(File file) {
      List<bW> parsed = new ArrayList<>();
      int added = 0;
      Set<String> knownKeys;
      synchronized (this) {
         knownKeys = new HashSet<>(this.items.size() + 1024);

         for (bW proxy : this.items) {
            knownKeys.add(buildIdentityKey(proxy));
         }
      }

      String line;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         while ((line = reader.readLine()) != null) {
            bW proxy = parseProxyLine(line.trim());
            if (proxy != null && proxy.init_Z() && knownKeys.add(buildIdentityKey(proxy))) {
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
         Set<String> currentKeys = new HashSet<>(this.items.size() + parsed.size() + 16);

         for (bW proxy : this.items) {
            currentKeys.add(buildIdentityKey(proxy));
         }

         for (bW proxy : parsed) {
            if (currentKeys.add(buildIdentityKey(proxy))) {
               if (this.items.isEmpty() && added == 0) {
                  proxy.init_Z = true;
               }

               this.items.add(proxy);
               added++;
            }
         }

         if (added > 0) {
            this.bumpListRevision();
            this.scheduleSave();
            this.startGeoLookup(false);
         }

         return added;
      }
   }

   private void sortByLatencyInternal() {
      this.items.sort(ProxyListManager::compareByLatency);
   }

   private static int compareByLatency(bW a, bW b) {
      boolean aliveA = a != null && a.init_bW_CachePool == bW.Inner_CachePool.ALIVE;
      boolean aliveB = b != null && b.init_bW_CachePool == bW.Inner_CachePool.ALIVE;
      if (aliveA != aliveB) {
         return aliveA ? -1 : 1;
      } else {
         return aliveA ? Long.compare(a.init_J, b.init_J) : 0;
      }
   }

   private static bW parseProxyLine(String line) {
      if (!line.isBlank() && !line.startsWith("#")) {
         Matcher m = NAMED_OR_HOST_PORT_PATTERN.matcher(line);
         if (m.find()) {
            return createProxyEntry(m.group(1), normalizeIpAddress(m.group(2)), Integer.parseInt(m.group(3)), m.group(4), ProxyProtocolType.Socks4);
         }

         m = HOST_PORT_AUTH_PATTERN.matcher(line);
         if (m.find()) {
            bW proxy = createProxyEntry(null, normalizeIpAddress(m.group(1)), Integer.parseInt(m.group(2)), null, ProxyProtocolType.Socks5);
            if (m.group(3) != null) {
               proxy.write = m.group(3);
            }

            if (m.group(4) != null) {
               proxy.update = m.group(4);
            }

            return proxy;
         } else {
            m = URI_PROXY_PATTERN.matcher(line);
            if (!m.find()) {
               return null;
            }

            String typeName = m.group("type");
            ProxyProtocolType defaultType = m.group("pass") == null && !"socks".equals(typeName) ? ProxyProtocolType.Socks4 : ProxyProtocolType.Socks5;
            bW proxy = createProxyEntry(null, normalizeIpAddress(m.group("addr")), Integer.parseInt(m.group("port")), typeName, defaultType);
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

   private static String normalizeIpAddress(String address) {
      return address == null ? "" : address.replaceAll("\\b0+\\B", "");
   }

   private static bW createProxyEntry(String name, String address, int port, String typeName, ProxyProtocolType defaultType) {
      bW proxy = new bW();
      proxy.load_Str = name == null ? "" : name.trim();
      proxy.read = address;
      proxy.init_I = port;
      proxy.init_bZ = defaultType == null ? ProxyProtocolType.Socks5 : defaultType;
      if (typeName != null) {
         String lower = typeName.toLowerCase();
         if (lower.contains("4")) {
            proxy.init_bZ = ProxyProtocolType.Socks4;
         } else if (lower.contains("5")) {
            proxy.init_bZ = ProxyProtocolType.Socks5;
         }
      }

      return proxy;
   }

   @Override
   public Iterator<bW> iterator() {
      return this.getItems().iterator();
   }

   public record Inner_BufferHelper(boolean running, boolean canceling, int scannedCount, int parsedCount, int addedCount, long generation, long revision, boolean canceled) {
   }

   private interface Inner_CachePool {
      BufferedReader open() throws IOException;
   }

   private record Inner_ConfigStore(bW.Inner_DataUtil checkResult, ProxyProtocolType detectedProtocol) {
   }

   private record Inner_DataUtil(ProxyListManager.Inner_StreamUtil target, bX.Inner_DataUtil geoResult) {
   }

   private record Inner_NetHelper(bW proxy, String identityKey) {
   }

   private record Inner_StreamUtil(bW proxy, String identityKey) {
   }

   public record Inner_TaskQueue(boolean running, boolean canceling, int checkedCount, int totalCount, long generation, long revision) {
   }
}
