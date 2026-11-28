package autismclient.protector;

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

public final class OutboundPacketFilterHandler extends ChannelOutboundHandlerAdapter {
   private static final int init_I = 4096;
   private static final Map<Connection, Map<Packet<?>, Boolean>> init_Map = new IdentityHashMap<>();
   private static int pendingCount;
   private static int consumeAttempts;
   private final Connection connection;

   public OutboundPacketFilterHandler(Connection connection) {
      this.connection = connection;
   }

   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (!(msg instanceof ServerboundCustomPayloadPacket) && !(msg instanceof ServerboundCustomQueryAnswerPacket)) {
         super.write(ctx, msg, promise);
      } else {
         Packet<?> packet = (Packet<?>)msg;
         if (consumePending(this.connection, packet)) {
            super.write(ctx, msg, promise);
         } else {
            OutboundChannelPolicy.Verdict verdict = OutboundChannelPolicy.verdict(this.connection, packet);
            switch (verdict.kind) {
               case PASS:
                  ProtectorFeatureFlags.unmarkDirect(packet);
                  super.write(ctx, msg, promise);
                  break;
               case REPLACE:
                  ProtectorFeatureFlags.unmarkDirect(packet);
                  super.write(ctx, verdict.replacement, promise);
                  break;
               case DROP:
                  ProtectorFeatureFlags.unmarkDirect(packet);
                  promise.setSuccess();
            }
         }
      }
   }

   public static boolean handleOutbound(Connection connection, Channel channel, Packet<?> packet) {
      if (connection != null && isLive(channel) && (packet instanceof ServerboundCustomPayloadPacket || packet instanceof ServerboundCustomQueryAnswerPacket)) {
         OutboundChannelPolicy.Verdict verdict = OutboundChannelPolicy.verdict(connection, packet);
         Packet var10000;
         switch (verdict.kind) {
            case PASS:
               var10000 = packet;
               break;
            case REPLACE:
               var10000 = verdict.replacement;
               break;
            case DROP:
               var10000 = null;
               break;
            default:
               throw new MatchException(null, null);
         }

         Packet<?> outbound = var10000;
         ProtectorFeatureFlags.unmarkDirect(packet);
         if (outbound == null) {
            return false;
         }

         boolean filtered = isHandlerInstalled(connection, channel);
         if (filtered && !markPending(connection, outbound)) {
            return false;
         }

         try {
            ChannelFuture future = channel.writeAndFlush(outbound);
            if (future == null) {
               if (filtered) {
                  consumePending(connection, outbound);
               }

               return false;
            } else {
               if (filtered) {
                  future.addListener(ignored -> consumePending(connection, outbound));
               }

               return !future.isDone() || future.isSuccess();
            }
         } catch (RuntimeException | Error failure) {
            if (filtered) {
               consumePending(connection, outbound);
            }

            throw failure;
         }
      } else {
         return false;
      }
   }

   public static boolean filterSend(Connection connection, Channel channel, Packet<?> packet, ChannelFutureListener listener, boolean flush) {
      if (!(packet instanceof ServerboundCustomPayloadPacket) && !(packet instanceof ServerboundCustomQueryAnswerPacket)) {
         return true;
      }

      OutboundChannelPolicy.Verdict verdict = OutboundChannelPolicy.verdict(connection, packet);
      ProtectorFeatureFlags.unmarkDirect(packet);
      switch (verdict.kind) {
         case PASS:
            boolean marked = false;

            try {
               if (!isHandlerInstalled(connection, channel)) {
                  return true;
               }

               return markPending(connection, packet);
            } catch (RuntimeException | Error failure) {
               if (marked) {
                  consumePending(connection, packet);
               }

               throw failure;
            }
         case REPLACE:
            Packet<?> replacement = verdict.replacement;
            if (replacement != null && isLive(channel)) {
               boolean markedRep = false;

               try {
                  if (isHandlerInstalled(connection, channel)) {
                     markedRep = markPending(connection, replacement);
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
                        consumePending(connection, replacement);
                     }

                     return false;
                  }

                  boolean cleanupMarked = markedRep;
                  future.addListener(ignored -> {
                     if (cleanupMarked) {
                        consumePending(connection, replacement);
                     }
                  });
                  if (listener != null) {
                     future.addListener(listener);
                  }

                  return false;
               } catch (RuntimeException | Error failure) {
                  if (markedRep) {
                     consumePending(connection, replacement);
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

   private static boolean isLive(Channel channel) {
      return channel != null && channel.isOpen() && channel.isActive();
   }

   public static boolean isPayloadPacket(Object packet) {
      return packet instanceof ServerboundCustomPayloadPacket || packet instanceof ServerboundCustomQueryAnswerPacket;
   }

   public static ChannelFuture init(Connection connection, Object packet, ChannelFuture future) {
      if (future == null) {
         consumePending(connection, (Packet<?>)packet);
      } else {
         future.addListener(ignored -> consumePending(connection, (Packet<?>)packet));
      }

      return future;
   }

   public static void init(Connection connection, Object packet) {
      consumePending(connection, (Packet<?>)packet);
   }

   private static boolean isHandlerInstalled(Connection connection, Channel channel) {
      if (connection != null && isLive(channel)) {
         ChannelPipeline pipeline = channel.pipeline();
         if (pipeline == null) {
            return false;
         }

         OutboundPacketFilterHandler filter = pipeline.get(OutboundPacketFilterHandler.class);
         return filter != null && filter.connection == connection;
      } else {
         return false;
      }
   }

   private static boolean markPending(Connection connection, Packet<?> packet) {
      if (connection != null && packet != null) {
         synchronized (init_Map) {
            Map<Packet<?>, Boolean> byPacket = init_Map.get(connection);
            if (byPacket != null && byPacket.containsKey(packet)) {
               return true;
            }

            if (pendingCount >= 4096) {
               return false;
            }

            if (byPacket == null) {
               byPacket = new IdentityHashMap<>();
               init_Map.put(connection, byPacket);
            }

            byPacket.put(packet, Boolean.TRUE);
            pendingCount++;
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean consumePending(Connection connection, Packet<?> packet) {
      synchronized (init_Map) {
         consumeAttempts++;
         Map<Packet<?>, Boolean> byPacket = init_Map.get(connection);
         if (byPacket != null && byPacket.remove(packet) != null) {
            pendingCount--;
            if (byPacket.isEmpty()) {
               init_Map.remove(connection);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   static void forget(Connection connection) {
      synchronized (init_Map) {
         Map<Packet<?>, Boolean> removed = init_Map.remove(connection);
         if (removed != null) {
            pendingCount = pendingCount - removed.size();
         }
      }
   }

   static int init_I() {
      synchronized (init_Map) {
         return pendingCount;
      }
   }

   static int consumeAttemptCount() {
      synchronized (init_Map) {
         return consumeAttempts;
      }
   }

   static void init_V() {
      synchronized (init_Map) {
         init_Map.clear();
         pendingCount = 0;
         consumeAttempts = 0;
      }
   }
}
