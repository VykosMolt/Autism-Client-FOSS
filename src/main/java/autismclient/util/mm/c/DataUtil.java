package autismclient.util.mm.c;

import autismclient.AutismClientAddon;
import autismclient.util.mm.EventBus;
import autismclient.util.mm.IoWorker;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public final class DataUtil implements StreamUtil {
   private static final long init_J = 120000L;
   private static final long load_J = 20000L;
   private static final int init_I = 256;
   private static final long read = 90000L;
   private final String init_Str;
   private final BufferHelper init_BufferHelper;
   private final Map<String, Consumer<byte[]>> init_Map = new ConcurrentHashMap<>();
   private final Supplier<String> init_Supplier;
   private final Supplier<String> load_Supplier;
   private final ScheduledExecutorService init_ScheduledExecutorS;
   private final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private final String load_Str;
   private final ArrayDeque<DataUtil.Inner_DataUtil> init_ArrayDeque = new ArrayDeque<>();
   private volatile MqttAsyncClient init_MqttAsyncClient;
   private volatile ScheduledFuture<?> init_ScheduledFuture;
   private volatile boolean init_Z;

   private DataUtil(String name, String serverUri, Supplier<String> tokenSupplier, Supplier<String> usernameSupplier) {
      this.init_BufferHelper = new BufferHelper(name);
      this.init_Str = serverUri;
      this.init_Supplier = tokenSupplier;
      this.load_Supplier = usernameSupplier;
      this.load_Str = "mm" + autismclient.util.mm.a.StreamUtil.init_Str_BArr(autismclient.util.mm.a.StreamUtil.init_BArr_I(16));
      this.init_ScheduledExecutorS = Executors.newSingleThreadScheduledExecutor(r -> {
         Thread t = new Thread(r, "mm-mqtt-reconnect");
         t.setDaemon(true);
         return t;
      });
      this.init_ScheduledExecutorS.execute(this::read);
   }

   public static DataUtil init(String name, String serverUri, Supplier<String> tokenSupplier, Supplier<String> usernameSupplier) {
      return new DataUtil(name, serverUri, tokenSupplier, usernameSupplier);
   }

   @Override
   public String init_Str() {
      return this.init_BufferHelper.init_Str;
   }

   @Override
   public BufferHelper init_BufferHelper() {
      return this.init_BufferHelper;
   }

   private boolean init_Z() {
      return EventBus.init_EventBus().sync_Z();
   }

   private synchronized void read() {
      if (!this.init_Z) {
         MqttAsyncClient created = null;
         try {
            String jwt = this.init_Supplier.get();
            if (jwt == null || jwt.isBlank()) {
               this.write();
               return;
            }

            String user = this.load_Supplier == null ? null : this.load_Supplier.get();
            MqttAsyncClient old = this.init_MqttAsyncClient;
            if (old != null) {
               read_V_MqttAsyncClient(old);
            }

            final MqttAsyncClient c = new MqttAsyncClient(this.init_Str, this.load_Str, new MemoryPersistence());
            created = c;
            final java.util.concurrent.atomic.AtomicBoolean initialSubscribed = new java.util.concurrent.atomic.AtomicBoolean(false);
            c.setCallback(
               new MqttCallbackExtended() {
                  @Override
                  public void connectComplete(boolean reconnect, String uri) {
                     if (DataUtil.this.init_Z) {
                        DataUtil.read_V_MqttAsyncClient(c);
                     } else if (c == DataUtil.this.init_MqttAsyncClient) {
                        DataUtil.this.init_BufferHelper.init_Z = true;
                        DataUtil.this.init_BufferHelper.load_Str = "";
                        DataUtil.this.init_BufferHelper.init_V();

                        if (reconnect || initialSubscribed.compareAndSet(false, true)) {
                           for (String topic : DataUtil.this.init_Map.keySet()) {
                              DataUtil.this.read_V_Str(topic);
                           }
                        }

                        DataUtil.this.load(c);
                     }
                  }

                  @Override
                  public void connectionLost(Throwable cause) {
                     if (DataUtil.this.init_Z) {
                        DataUtil.read_V_MqttAsyncClient(c);
                     } else if (c == DataUtil.this.init_MqttAsyncClient) {
                        if (DataUtil.this.init_Z()) {
                           AutismClientAddon.LOG
                              .info(
                                 "[mm-relay] {} connection lost: {}",
                                 DataUtil.this.init_BufferHelper.init_Str,
                                 cause == null ? "?" : String.valueOf(cause.getMessage())
                              );
                        }

                        DataUtil.this.init_BufferHelper.init_Z = false;
                        DataUtil.this.init_BufferHelper.load_Str = cause == null ? "lost" : String.valueOf(cause.getMessage());
                        DataUtil.this.write();
                     }
                  }

                  @Override
                  public void messageArrived(String topic, MqttMessage message) {
                     Consumer<byte[]> consumer = DataUtil.this.init_Map.get(topic);
                     if (consumer != null) {
                        DataUtil.this.init_BufferHelper.load_AtomicLong.incrementAndGet();
                        DataUtil.this.init_BufferHelper.init_V();

                        try {
                           consumer.accept(message.getPayload());
                        } catch (Throwable t) {
                           AutismClientAddon.LOG.debug("MQTT inbound handler threw (dropped)", t);
                        }
                     }
                  }

                  @Override
                  public void deliveryComplete(IMqttDeliveryToken token) {
                  }
               }
            );
            this.init_MqttAsyncClient = c;
            MqttConnectOptions opts = new MqttConnectOptions();
            opts.setCleanSession(false);
            opts.setAutomaticReconnect(false);
            opts.setUserName(user != null && !user.isBlank() ? user : "jwt");
            opts.setPassword((jwt == null ? "" : jwt).toCharArray());
            opts.setConnectionTimeout(15);
            opts.setKeepAliveInterval(30);
            opts.setMaxInflight(64);
            final String presentedJwt = jwt;
            final long runwayS = jwt == null ? -1L : (init_J_Str(jwt) - IoWorker.init()) / 1000L;
            if (this.init_Z()) {
               AutismClientAddon.LOG
                  .info("[mm-relay] {} connecting (tokenRunway={}s, subs={})", this.init_BufferHelper.init_Str, runwayS, this.init_Map.size());
            }

            c.connect(
               opts,
               null,
               new IMqttActionListener() {
                  @Override
                  public void onSuccess(IMqttToken asyncActionToken) {
                     if (DataUtil.this.init_Z) {
                        DataUtil.read_V_MqttAsyncClient(c);
                     } else if (c == DataUtil.this.init_MqttAsyncClient) {
                        boolean resumed = false;

                        try {
                           resumed = asyncActionToken != null && asyncActionToken.getSessionPresent();
                        } catch (Throwable var5) {
                        }

                        if (DataUtil.this.init_Z()) {
                           AutismClientAddon.LOG
                              .info("[mm-relay] {} connected (sessionPresent={}, tokenRunway={}s)", DataUtil.this.init_BufferHelper.init_Str, resumed, runwayS);
                        }

                        DataUtil.this.init_AtomicInteger.set(0);
                        DataUtil.this.init_BufferHelper.init_Z = true;
                        DataUtil.this.init_BufferHelper.load_Str = "";
                        DataUtil.this.init_BufferHelper.init_V();

                        if (initialSubscribed.compareAndSet(false, true)) {
                           for (String topic : DataUtil.this.init_Map.keySet()) {
                              DataUtil.this.read_V_Str(topic);
                           }
                        }

                        DataUtil.this.load(c);
                        DataUtil.this.load(presentedJwt);
                     }
                  }

                  @Override
                  public void onFailure(IMqttToken asyncActionToken, Throwable e) {
                     if (DataUtil.this.init_Z) {
                        DataUtil.read_V_MqttAsyncClient(c);
                     } else if (c == DataUtil.this.init_MqttAsyncClient) {
                        if (DataUtil.this.init_Z()) {
                           AutismClientAddon.LOG
                              .info(
                                 "[mm-relay] {} connect FAILED: {}", DataUtil.this.init_BufferHelper.init_Str, e == null ? "?" : String.valueOf(e.getMessage())
                              );
                        }

                        DataUtil.this.init_BufferHelper.init_Z = false;
                        DataUtil.this.init_BufferHelper.load_Str = e == null ? "connect failed" : String.valueOf(e.getMessage());
                        DataUtil.this.write();
                     }
                  }
               }
            );
         } catch (Throwable t) {
            if (created != null) {
               if (this.init_MqttAsyncClient == created) {
                  this.init_MqttAsyncClient = null;
               }
               read_V_MqttAsyncClient(created);
            }
            this.init_BufferHelper.load_Str = String.valueOf(t.getMessage());
            this.write();
         }
      }
   }

   @Override
   public void init_V() {
      if (!this.init_Z) {
         this.init_AtomicInteger.set(0);
         this.init_ScheduledExecutorS.execute(this::read);
      }
   }

   private void write() {
      if (!this.init_Z) {
         int attempt = this.init_AtomicInteger.incrementAndGet();
         long ceil = Math.min(30000L, 1000L * (1L << Math.min(attempt, 5)));
         long delay = 250L + (long)(ThreadLocalRandom.current().nextDouble() * (ceil - 250L));

         try {
            this.init_ScheduledExecutorS.schedule(() -> {
               if (!this.init_Z) {
                  this.read();
               }
            }, delay, TimeUnit.MILLISECONDS);
         } catch (Throwable var7) {
         }
      }
   }

   private void load(String jwt) {
      long expMs = init_J_Str(jwt);
      if (expMs > 0L) {
         ScheduledFuture<?> prev = this.init_ScheduledFuture;
         if (prev != null) {
            prev.cancel(false);
         }

         long jitter = (long)(ThreadLocalRandom.current().nextDouble() * 20000.0);
         long runway = expMs - IoWorker.init();
         long delay = runway > 145000L ? runway - 120000L - jitter : Math.max(20000L, runway / 2L);

         try {
            this.init_ScheduledFuture = this.init_ScheduledExecutorS.schedule(() -> {
               if (!this.init_Z) {
                  this.init_AtomicInteger.set(0);
                  this.read();
               }
            }, delay, TimeUnit.MILLISECONDS);
            if (this.init_Z()) {
               AutismClientAddon.LOG.info("[mm-relay] {} token rotation in {}s", this.init_BufferHelper.init_Str, delay / 1000L);
            }
         } catch (Throwable var12) {
         }
      }
   }

   private static long init_J_Str(String jwt) {
      if (jwt != null && !jwt.isEmpty()) {
         try {
            int d1 = jwt.indexOf(46);
            int d2 = jwt.indexOf(46, d1 + 1);
            if (d1 > 0 && d2 > d1) {
               byte[] payload = Base64.getUrlDecoder().decode(jwt.substring(d1 + 1, d2));
               JsonObject o = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
               return o.has("exp") ? o.get("exp").getAsLong() * 1000L : 0L;
            } else {
               return 0L;
            }
         } catch (Throwable t) {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   @Override
   public void init(String topic, byte[] frame) {
      this.init(topic, frame, false);
   }

   @Override
   public void init(String topic, byte[] frame, boolean durable) {
      if (!this.init_Z) {
         MqttAsyncClient c = this.init_MqttAsyncClient;
         boolean up = c != null && c.isConnected();
         synchronized (this.init_ArrayDeque) {
            if (!up) {
               if (durable) {
                  this.load(topic, frame);
               }

               return;
            }

            if (!this.init_ArrayDeque.isEmpty()) {
               this.load(topic, frame);
               this.init_V_MqttAsyncClient(c);
               return;
            }
         }

         if (!this.init(c, topic, frame) && durable) {
            synchronized (this.init_ArrayDeque) {
               this.load(topic, frame);
            }
         }
      }
   }

   private void load(String topic, byte[] frame) {
      while (this.init_ArrayDeque.size() >= 256) {
         this.init_ArrayDeque.pollFirst();
         this.init_BufferHelper.read.incrementAndGet();
      }

      this.init_ArrayDeque.addLast(new DataUtil.Inner_DataUtil(topic, frame, System.currentTimeMillis()));
   }

   private void init_V_MqttAsyncClient(MqttAsyncClient c) {
      long now = System.currentTimeMillis();

      DataUtil.Inner_DataUtil q;
      while ((q = this.init_ArrayDeque.pollFirst()) != null) {
         if (now - q.init_J > 90000L) {
            this.init_BufferHelper.read.incrementAndGet();
         } else if (!this.init(c, q.init_Str, q.init_BArr)) {
            this.init_ArrayDeque.addFirst(q);
            return;
         }
      }
   }

   private void load(MqttAsyncClient c) {
      synchronized (this.init_ArrayDeque) {
         this.init_V_MqttAsyncClient(c);
      }
   }

   private boolean init(MqttAsyncClient c, String topic, byte[] frame) {
      try {
         MqttMessage m = new MqttMessage(frame);
         m.setQos(0);
         m.setRetained(false);
         c.publish(topic, m);
         this.init_BufferHelper.init_AtomicLong.incrementAndGet();
         this.init_BufferHelper.init_V();
         return true;
      } catch (Throwable t) {
         this.init_BufferHelper.load_Str = String.valueOf(t.getMessage());
         return false;
      }
   }

   @Override
   public void init(String topic, Consumer<byte[]> onFrame) {
      if (!this.init_Z) {
         this.init_Map.put(topic, onFrame);
         this.read_V_Str(topic);
      }
   }

   private void read_V_Str(final String topic) {
      MqttAsyncClient c = this.init_MqttAsyncClient;
      if (c != null && c.isConnected()) {
         try {
            c.subscribe(
               topic,
               0,
               null,
               new IMqttActionListener() {
                  @Override
                  public void onSuccess(IMqttToken t) {
                     int[] q = t == null ? null : t.getGrantedQos();
                     int granted = q != null && q.length > 0 ? q[0] : -1;
                     if (DataUtil.this.init_Z()) {
                        AutismClientAddon.LOG
                           .info("[mm-relay] subscribe …{} -> {}", DataUtil.init_Str_Str(topic), granted >= 128 ? "DENIED" : "granted qos" + granted);
                     }
                  }

                  @Override
                  public void onFailure(IMqttToken t, Throwable e) {
                     if (DataUtil.this.init_Z()) {
                        AutismClientAddon.LOG
                           .info("[mm-relay] subscribe …{} FAILED: {}", DataUtil.init_Str_Str(topic), e == null ? "?" : String.valueOf(e.getMessage()));
                     }
                  }
               }
            );
         } catch (Throwable t) {
            this.init_BufferHelper.load_Str = String.valueOf(t.getMessage());
         }
      }
   }

   private static String init_Str_Str(String topic) {
      return topic == null ? "?" : topic.substring(Math.max(0, topic.length() - 6));
   }

   @Override
   public void init_V_Str(String topic) {
      this.init_Map.remove(topic);
      MqttAsyncClient c = this.init_MqttAsyncClient;
      if (c != null && c.isConnected()) {
         try {
            c.unsubscribe(topic);
         } catch (Throwable var4) {
         }
      }
   }

   @Override
   public synchronized void load() {
      this.init_Z = true;
      ScheduledFuture<?> r = this.init_ScheduledFuture;
      if (r != null) {
         r.cancel(false);
      }

      try {
         this.init_ScheduledExecutorS.shutdownNow();
      } catch (Throwable var3) {
      }

      MqttAsyncClient c = this.init_MqttAsyncClient;
      this.init_MqttAsyncClient = null;
      if (c != null) {
         read_V_MqttAsyncClient(c);
      }

      this.init_BufferHelper.init_Z = false;
   }

   private static void read_V_MqttAsyncClient(MqttAsyncClient c) {
      try {
         c.disconnectForcibly(0L, 250L);
      } catch (Throwable var3) {
      }

      try {
         c.close(true);
      } catch (Throwable var2) {
      }
   }

   private static final class Inner_DataUtil {
      final String init_Str;
      final byte[] init_BArr;
      final long init_J;

      Inner_DataUtil(String topic, byte[] frame, long enqueuedMs) {
         this.init_Str = topic;
         this.init_BArr = frame;
         this.init_J = enqueuedMs;
      }
   }
}
