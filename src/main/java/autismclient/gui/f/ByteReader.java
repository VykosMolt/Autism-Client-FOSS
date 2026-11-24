package autismclient.gui.f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ByteReader {
   private final Map<TextFormat, List<StateCache>> init = new EnumMap<>(TextFormat.class);

   public void init() {
      this.init.clear();
   }

   public void init(TextFormat layer, StateCache component) {
      if (layer != null && component != null) {
         this.init.computeIfAbsent(layer, ignored -> new ArrayList<>()).add(component);
      }
   }

   public void load(TextFormat layer, StateCache component) {
      if (layer != null && component != null) {
         List<StateCache> components = this.init.get(layer);
         if (components != null) {
            components.remove(component);
            if (components.isEmpty()) {
               this.init.remove(layer);
            }
         }
      }
   }

   public void read(TextFormat layer, StateCache component) {
      if (layer != null && component != null) {
         this.load(layer, component);
         this.init(layer, component);
      }
   }

   public void init_V_TextFormat(TextFormat layer) {
      if (layer != null) {
         this.init.remove(layer);
      }
   }

   public void init_V_SyncManager(SyncManager context) {
      boolean first = true;

      for (TextFormat layer : TextFormat.values()) {
         List<StateCache> components = this.init.get(layer);
         if (components != null) {
            for (StateCache component : components) {
               if (!first) {
                  context.init_GuiGraphicsExtract().nextStratum();
               }

               first = false;
               HashUtil.init_HashUtil().load(context.init_GuiGraphicsExtract());

               try {
                  component.init_V_SyncManager(context);
               } finally {
                  HashUtil.init_HashUtil().load(context.init_GuiGraphicsExtract());
               }
            }
         }
      }
   }

   public EventBus init_EventBus_I_2(int mouseX, int mouseY, int button) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.init_EventBus_I_2(mouseX, mouseY, button));
   }

   public EventBus load(int mouseX, int mouseY, int button) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.load(mouseX, mouseY, button));
   }

   public EventBus init(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.init_EventBus_I(mouseX, mouseY, button, deltaX, deltaY));
   }

   public EventBus init_EventBus_I(int mouseX, int mouseY, double amount) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.init_EventBus_I(mouseX, mouseY, amount));
   }

   public EventBus read(int key, int scanCode, int modifiers) {
      return this.init_EventBus_ByteReader_DataUti(component -> component.read(key, scanCode, modifiers));
   }

   public EventBus init_EventBus_C(char chr) {
      return this.init_EventBus_ByteReader_DataUti(component -> component.init_EventBus_C(chr));
   }

   private EventBus init_EventBus_ByteReader_DataUti(ByteReader.Inner_DataUtil call) {
      TextFormat[] order = TextFormat.values();

      for (int layerIndex = order.length - 1; layerIndex >= 0; layerIndex--) {
         TextFormat layer = order[layerIndex];
         List<StateCache> components = this.init.get(layer);
         if (components != null && !components.isEmpty()) {
            for (int i = components.size() - 1; i >= 0; i--) {
               if (call.invoke(components.get(i)) == EventBus.load) {
                  return EventBus.load;
               }
            }

            if (layer == TextFormat.update) {
               return EventBus.load;
            }
         }
      }

      return EventBus.init;
   }

   private EventBus init_EventBus_I_3(int mouseX, int mouseY, ByteReader.Inner_DataUtil call) {
      TextFormat[] order = TextFormat.values();

      for (int layerIndex = order.length - 1; layerIndex >= 0; layerIndex--) {
         TextFormat layer = order[layerIndex];
         List<StateCache> components = this.init.get(layer);
         if (components != null && !components.isEmpty()) {
            for (int i = components.size() - 1; i >= 0; i--) {
               StateCache component = components.get(i);
               NetHelper bounds = component.load_NetHelper();
               if (bounds != null && bounds.init_Z(mouseX, mouseY) && call.invoke(component) == EventBus.load) {
                  return EventBus.load;
               }
            }

            if (init_Z_TextFormat(layer)) {
               return EventBus.load;
            }
         }
      }

      return EventBus.init;
   }

   private static boolean init_Z_TextFormat(TextFormat layer) {
      return layer == TextFormat.read || layer == TextFormat.update;
   }

   @FunctionalInterface
   private interface Inner_DataUtil {
      EventBus invoke(StateCache var1);
   }
}
