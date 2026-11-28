package autismclient.protector;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.ShowEntity;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class PacketComponentOriginTracker {
   private static final ThreadLocal<int[]> depthCounter = new ThreadLocal<>();

   private PacketComponentOriginTracker() {
   }

   public static PacketComponentOriginTracker.OriginScope init_StreamUtil_DataUti() {
      int[] depth = depthCounter.get();
      if (depth == null) {
         depth = new int[1];
         depthCounter.set(depth);
      }

      depth[0]++;
      return new PacketComponentOriginTracker.OriginScope();
   }

   public static boolean isDecoding() {
      int[] depth = depthCounter.get();
      return depth != null && depth[0] != 0;
   }

   public static boolean load() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft == null) {
         return true;
      }

      ConnectionPrivacyRegistry.ConnectionSession context = ConnectionPrivacyRegistry.getByListener(minecraft.getConnection());
      return context == null || context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA;
   }

   public static void tag(Component component) {
      ConnectionPrivacyRegistry.ConnectionSession origin = PacketDecodeOriginStack.current();
      markComponentFromPacket(component, origin == null ? originOf(component) : origin);
   }

   public static void markComponentFromPacket(Component component, ConnectionPrivacyRegistry.ConnectionSession origin) {
      markTreeFromPacket(component, Collections.newSetFromMap(new IdentityHashMap<>()), origin);
   }

   public static boolean isPacketComponent(Component component) {
      return treeFromPacket(component, Collections.newSetFromMap(new IdentityHashMap<>()));
   }

   public static boolean isFromPacket(ComponentContents contents) {
      return treeFromPacket(contents, Collections.newSetFromMap(new IdentityHashMap<>()));
   }

   public static void propagateFromContents(ComponentContents source, Component result) {
      if (source != null && result != null) {
         if (isFromPacket(source)) {
            markComponentFromPacket(result, originOf(source));
         } else {
            init_Z((Object)result);
         }
      }
   }

   public static void propagate(Component source, Component result) {
      if (source != null && result != null) {
         if (isPacketComponent(source)) {
            markComponentFromPacket(result, originOf(source));
         } else {
            init_Z((Object)result);
         }
      }
   }

   private static boolean init_Z(Object root) {
      ConnectionPrivacyRegistry.ConnectionSession origin = originOf(root);
      Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      boolean marking = false;

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               pushComponent(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access) {
                  if (!marking && access.autism$isFromPacket()) {
                     marking = true;

                     for (Object seen : visited) {
                        markFromPacket(seen, origin);
                     }
                  } else if (marking) {
                     access.autism$setFromPacket(origin);
                  }
               }

               pushContents(contents, pending);
            }
         }
      }

      return marking;
   }

   private static void markFromPacket(Object value, ConnectionPrivacyRegistry.ConnectionSession origin) {
      if (value instanceof ComponentContents contents && contents instanceof PacketOriginDuck access) {
         access.autism$setFromPacket(origin);
      }
   }

   private static void markTreeFromPacket(Object root, Set<Object> visited, ConnectionPrivacyRegistry.ConnectionSession origin) {
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               pushComponent(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access) {
                  access.autism$setFromPacket(origin);
               }

               pushContents(contents, pending);
            }
         }
      }
   }

   private static ConnectionPrivacyRegistry.ConnectionSession originOf(Object root) {
      Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      ConnectionPrivacyRegistry.ConnectionSession origin = null;

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               pushComponent(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access && access.autism$isFromPacket()) {
                  ConnectionPrivacyRegistry.ConnectionSession candidate = access.autism$packetOrigin();
                  if (candidate != null) {
                     if (origin != null && origin != candidate) {
                        return null;
                     }

                     origin = candidate;
                  }
               }

               pushContents(contents, pending);
            }
         }
      }

      return origin;
   }

   private static boolean treeFromPacket(Object root, Set<Object> visited) {
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               pushComponent(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access && access.autism$isFromPacket()) {
                  return true;
               }

               pushContents(contents, pending);
            }
         }
      }

      return false;
   }

   private static void pushComponent(Component component, ArrayDeque<Object> pending) {
      push((Object)component.getContents(), pending);

      for (Component sibling : component.getSiblings()) {
         push((Object)sibling, pending);
      }

      HoverEvent hover = component.getStyle().getHoverEvent();
      if (hover instanceof ShowText showText) {
         push((Object)showText.value(), pending);
      } else if (hover instanceof ShowEntity showEntity) {
         pushOptional(showEntity.entity().name, pending);
      }
   }

   private static void pushContents(ComponentContents contents, ArrayDeque<Object> pending) {
      if (contents instanceof TranslatableContents translatable) {
         for (Object argument : translatable.getArgs()) {
            if (argument instanceof Component component) {
               push((Object)component, pending);
            }
         }
      } else if (contents instanceof NbtContents nbt) {
         pushOptional(nbt.separator(), pending);
      } else if (contents instanceof SelectorContents selector) {
         pushOptional(selector.separator(), pending);
      } else if (contents instanceof ObjectContents object) {
         pushOptional(object.fallback(), pending);
      }
   }

   private static void pushOptional(Optional<Component> component, ArrayDeque<Object> pending) {
      if (component != null && component.isPresent()) {
         push((Object)component.get(), pending);
      }
   }

   private static void push(Object value, ArrayDeque<Object> pending) {
      if (value != null) {
         pending.push(value);
      }
   }

   public static final class OriginScope implements AutoCloseable {
      private boolean init;

      private OriginScope() {
      }

      @Override
      public void close() {
         if (!this.init) {
            this.init = true;
            int[] depth = PacketComponentOriginTracker.depthCounter.get();
            if (depth != null) {
               if (depth[0] > 0) {
                  depth[0]--;
               }

               if (depth[0] == 0) {
                  PacketComponentOriginTracker.depthCounter.remove();
               }
            }
         }
      }
   }
}
