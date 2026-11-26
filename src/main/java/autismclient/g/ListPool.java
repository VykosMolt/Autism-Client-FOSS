package autismclient.g;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;

public final class ListPool extends ChannelOutboundHandlerAdapter {
   private static final int init_I = 4096;
   private static final Map<Connection, Map<Packet<?>, Boolean>> init_Map = new IdentityHashMap<>();
   private static int load;
   private static int read;
   private final Connection init_Connection;

   public ListPool(Connection connection) {
      this.init_Connection = connection;
   }

   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (!(msg instanceof ServerboundCustomPayloadPacket) && !(msg instanceof ServerboundCustomQueryAnswerPacket)) {
         super.write(ctx, msg, promise);
      } else {
         Packet<?> packet = (Packet<?>)msg;
         if (load(this.init_Connection, packet)) {
            super.write(ctx, msg, promise);
         } else {
            ConfigStore.Inner_DataUtil verdict = ConfigStore.init(this.init_Connection, packet);
            switch (verdict.init_ConfigStore_DataUt) {
               case PASS:
                  HashUtil.load(packet);
                  super.write(ctx, msg, promise);
                  break;
               case REPLACE:
                  HashUtil.load(packet);
                  super.write(ctx, verdict.init_Packet, promise);
                  break;
               case DROP:
                  HashUtil.load(packet);
                  promise.setSuccess();
            }
         }
      }
   }

   public static boolean init(Connection connection, Channel channel, Packet<?> packet) {
      if (connection != null && init(channel) && (packet instanceof ServerboundCustomPayloadPacket || packet instanceof ServerboundCustomQueryAnswerPacket)) {
         ConfigStore.Inner_DataUtil verdict = ConfigStore.init(connection, packet);
         Packet var10000;
         switch (verdict.init_ConfigStore_DataUt) {
            case PASS:
               var10000 = packet;
               break;
            case REPLACE:
               var10000 = verdict.init_Packet;
               break;
            case DROP:
               var10000 = null;
               break;
            default:
               throw new MatchException(null, null);
         }

         Packet<?> outbound = var10000;
         HashUtil.load(packet);
         if (outbound == null) {
            return false;
         }

         boolean filtered = init(connection, channel);
         if (filtered && !init(connection, outbound)) {
            return false;
         }

         try {
            ChannelFuture future = channel.writeAndFlush(outbound);
            if (future == null) {
               if (filtered) {
                  load(connection, outbound);
               }

               return false;
            } else {
               if (filtered) {
                  future.addListener(ignored -> load(connection, outbound));
               }

               return !future.isDone() || future.isSuccess();
            }
         } catch (RuntimeException | Error failure) {
            if (filtered) {
               load(connection, outbound);
            }

            throw failure;
         }
      } else {
         return false;
      }
   }

   public static boolean init(Connection connection, Channel channel, Packet<?> packet, ChannelFutureListener listener, boolean flush) {
      if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
         return true;
      }

      ConfigStore.Inner_DataUtil verdict = ConfigStore.init(connection, packet);
      HashUtil.load(packet);
      switch (verdict.init_ConfigStore_DataUt) {
         case PASS:
            boolean marked = false;

            try {
               if (!init(connection, channel)) {
                  return true;
               }

               return init(connection, packet);
            } catch (RuntimeException | Error failure) {
               if (marked) {
                  load(connection, packet);
               }

               throw failure;
            }
         case REPLACE:
            Packet<?> replacement = verdict.init_Packet;
            if (replacement != null && init(channel)) {
               boolean markedRep = false;

               try {
                  if (init(connection, channel)) {
                     markedRep = init(connection, replacement);
                     if (!markedRep) {
                        return false;
                     }
                  }

                  ChannelFuture future;
                  if (listener == null) {
                     future = flush ? channel.writeAndFlush(replacement, channel.newPromise()) : channel.write(replacement, channel.newPromise());
                  } else {
                     future = flush ? channel.writeAndFlush(replacement) : channel.write(replacement);
                  }

                  if (future == null) {
                     if (markedRep) {
                        load(connection, replacement);
                     }

                     return false;
                  }

                  boolean cleanupMarked = markedRep;
                  future.addListener(ignored -> {
                     if (cleanupMarked) {
                        load(connection, replacement);
                     }
                  });
                  if (listener != null) {
                     future.addListener(listener);
                  }

                  return false;
               } catch (RuntimeException | Error failure) {
                  if (markedRep) {
                     load(connection, replacement);
                  }

                  throw failure;
               }
            }

            return false;
         case DROP:
            return false;
         default:
            return false;
      }
   }

   private static boolean init(Channel channel) {
      return channel != null && channel.isOpen() && channel.isActive();
   }

   public static boolean init(Object packet) {
      return packet instanceof ServerboundCustomPayloadPacket || packet instanceof ServerboundCustomQueryAnswerPacket;
   }

   public static ChannelFuture init(Connection connection, Object packet, ChannelFuture future) {
      if (future == null) {
         load(connection, (Packet<?>)packet);
      } else {
         future.addListener(ignored -> load(connection, (Packet<?>)packet));
      }

      return future;
   }

   public static void init(Connection connection, Object packet) {
      load(connection, (Packet<?>)packet);
   }

   private static boolean init(Connection connection, Channel channel) {
      if (connection != null && init(channel)) {
         ChannelPipeline pipeline = channel.pipeline();
         if (pipeline == null) {
            return false;
         }

         ListPool filter = pipeline.get(ListPool.class);
         return filter != null && filter.init_Connection == connection;
      } else {
         return false;
      }
   }

   private static boolean init(Connection connection, Packet<?> packet) {
      if (connection != null && packet != null) {
         synchronized (init_Map) {
            Map<Packet<?>, Boolean> byPacket = init_Map.get(connection);
            if (byPacket != null && byPacket.containsKey(packet)) {
               return true;
            }

            if (load >= 4096) {
               return false;
            }

            if (byPacket == null) {
               byPacket = new IdentityHashMap<>();
               init_Map.put(connection, byPacket);
            }

            byPacket.put(packet, Boolean.TRUE);
            load++;
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean load(Connection connection, Packet<?> packet) {
      synchronized (init_Map) {
         read++;
         Map<Packet<?>, Boolean> byPacket = init_Map.get(connection);
         if (byPacket != null && byPacket.remove(packet) != null) {
            load--;
            if (byPacket.isEmpty()) {
               init_Map.remove(connection);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   static void init(Connection connection) {
      synchronized (init_Map) {
         Map<Packet<?>, Boolean> removed = init_Map.remove(connection);
         if (removed != null) {
            load = load - removed.size();
         }
      }
   }

   static int init_I() {
      synchronized (init_Map) {
         return load;
      }
   }

   static int load() {
      synchronized (init_Map) {
         return read;
      }
   }

   static void init_V() {
      synchronized (init_Map) {
         init_Map.clear();
         load = 0;
         read = 0;
      }
   }
}
