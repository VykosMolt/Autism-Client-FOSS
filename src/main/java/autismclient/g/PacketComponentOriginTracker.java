package autismclient.g;

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
   private static final ThreadLocal<int[]> init = new ThreadLocal<>();

   private PacketComponentOriginTracker() {
   }

   public static PacketComponentOriginTracker.Inner_DataUtil init_StreamUtil_DataUti() {
      int[] depth = init.get();
      if (depth == null) {
         depth = new int[1];
         init.set(depth);
      }

      depth[0]++;
      return new PacketComponentOriginTracker.Inner_DataUtil();
   }

   public static boolean init_Z() {
      int[] depth = init.get();
      return depth != null && depth[0] != 0;
   }

   public static boolean load() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft == null) {
         return true;
      }

      ConnectionPrivacyRegistry.Inner_StreamUtil context = ConnectionPrivacyRegistry.init(minecraft.getConnection());
      return context == null || context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.Inner_CachePool.VANILLA;
   }

   public static void init_V(Component component) {
      ConnectionPrivacyRegistry.Inner_StreamUtil origin = PacketDecodeOriginStack.init_BufferHelper_Strea();
      init(component, origin == null ? init_BufferHelper_Strea(component) : origin);
   }

   public static void init(Component component, ConnectionPrivacyRegistry.Inner_StreamUtil origin) {
      init(component, Collections.newSetFromMap(new IdentityHashMap<>()), origin);
   }

   public static boolean init_Z(Component component) {
      return init(component, Collections.newSetFromMap(new IdentityHashMap<>()));
   }

   public static boolean init(ComponentContents contents) {
      return init(contents, Collections.newSetFromMap(new IdentityHashMap<>()));
   }

   public static void init(ComponentContents source, Component result) {
      if (source != null && result != null) {
         if (init(source)) {
            init(result, init_BufferHelper_Strea(source));
         } else {
            init_Z((Object)result);
         }
      }
   }

   public static void init(Component source, Component result) {
      if (source != null && result != null) {
         if (init_Z(source)) {
            init(result, init_BufferHelper_Strea(source));
         } else {
            init_Z((Object)result);
         }
      }
   }

   private static boolean init_Z(Object root) {
      ConnectionPrivacyRegistry.Inner_StreamUtil origin = init_BufferHelper_Strea(root);
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
               init(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access) {
                  if (!marking && access.autism$isFromPacket()) {
                     marking = true;

                     for (Object seen : visited) {
                        init(seen, origin);
                     }
                  } else if (marking) {
                     access.autism$setFromPacket(origin);
                  }
               }

               init(contents, pending);
            }
         }
      }

      return marking;
   }

   private static void init(Object value, ConnectionPrivacyRegistry.Inner_StreamUtil origin) {
      if (value instanceof ComponentContents contents && contents instanceof PacketOriginDuck access) {
         access.autism$setFromPacket(origin);
      }
   }

   private static void init(Object root, Set<Object> visited, ConnectionPrivacyRegistry.Inner_StreamUtil origin) {
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               init(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access) {
                  access.autism$setFromPacket(origin);
               }

               init(contents, pending);
            }
         }
      }
   }

   private static ConnectionPrivacyRegistry.Inner_StreamUtil init_BufferHelper_Strea(Object root) {
      Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      ConnectionPrivacyRegistry.Inner_StreamUtil origin = null;

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               init(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access && access.autism$isFromPacket()) {
                  ConnectionPrivacyRegistry.Inner_StreamUtil candidate = access.autism$packetOrigin();
                  if (candidate != null) {
                     if (origin != null && origin != candidate) {
                        return null;
                     }

                     origin = candidate;
                  }
               }

               init(contents, pending);
            }
         }
      }

      return origin;
   }

   private static boolean init(Object root, Set<Object> visited) {
      ArrayDeque<Object> pending = new ArrayDeque<>();
      if (root != null) {
         pending.push(root);
      }

      while (!pending.isEmpty()) {
         Object current = pending.pop();
         if (visited.add(current)) {
            if (current instanceof Component component) {
               init(component, pending);
            } else if (current instanceof ComponentContents contents) {
               if (contents instanceof PacketOriginDuck access && access.autism$isFromPacket()) {
                  return true;
               }

               init(contents, pending);
            }
         }
      }

      return false;
   }

   private static void init(Component component, ArrayDeque<Object> pending) {
      init((Object)component.getContents(), pending);

      for (Component sibling : component.getSiblings()) {
         init((Object)sibling, pending);
      }

      HoverEvent hover = component.getStyle().getHoverEvent();
      if (hover instanceof ShowText showText) {
         init((Object)showText.value(), pending);
      } else if (hover instanceof ShowEntity showEntity) {
         init(showEntity.entity().name, pending);
      }
   }

   private static void init(ComponentContents contents, ArrayDeque<Object> pending) {
      if (contents instanceof TranslatableContents translatable) {
         for (Object argument : translatable.getArgs()) {
            if (argument instanceof Component component) {
               init((Object)component, pending);
            }
         }
      } else if (contents instanceof NbtContents nbt) {
         init(nbt.separator(), pending);
      } else if (contents instanceof SelectorContents selector) {
         init(selector.separator(), pending);
      } else if (contents instanceof ObjectContents object) {
         init(object.fallback(), pending);
      }
   }

   private static void init(Optional<Component> component, ArrayDeque<Object> pending) {
      if (component != null && component.isPresent()) {
         init((Object)component.get(), pending);
      }
   }

   private static void init(Object value, ArrayDeque<Object> pending) {
      if (value != null) {
         pending.push(value);
      }
   }

   public static final class Inner_DataUtil implements AutoCloseable {
      private boolean init;

      private Inner_DataUtil() {
      }

      @Override
      public void close() {
         if (!this.init) {
            this.init = true;
            int[] depth = PacketComponentOriginTracker.init.get();
            if (depth != null) {
               if (depth[0] > 0) {
                  depth[0]--;
               }

               if (depth[0] == 0) {
                  PacketComponentOriginTracker.init.remove();
               }
            }
         }
      }
   }
}
