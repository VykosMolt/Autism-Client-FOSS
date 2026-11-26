package autismclient.gui.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.SyncManager;

public final class UiLayerHost {
   private final Map<UiLayer, List<UiLayerComponent>> init = new EnumMap<>(UiLayer.class);

   public void init() {
      this.init.clear();
   }

   public void init(UiLayer layer, UiLayerComponent component) {
      if (layer != null && component != null) {
         this.init.computeIfAbsent(layer, ignored -> new ArrayList<>()).add(component);
      }
   }

   public void load(UiLayer layer, UiLayerComponent component) {
      if (layer != null && component != null) {
         List<UiLayerComponent> components = this.init.get(layer);
         if (components != null) {
            components.remove(component);
            if (components.isEmpty()) {
               this.init.remove(layer);
            }
         }
      }
   }

   public void read(UiLayer layer, UiLayerComponent component) {
      if (layer != null && component != null) {
         this.load(layer, component);
         this.init(layer, component);
      }
   }

   public void init_V_TextFormat(UiLayer layer) {
      if (layer != null) {
         this.init.remove(layer);
      }
   }

   public void init_V_SyncManager(SyncManager context) {
      boolean first = true;

      for (UiLayer layer : UiLayer.values()) {
         List<UiLayerComponent> components = this.init.get(layer);
         if (components != null) {
            for (UiLayerComponent component : components) {
               if (!first) {
                  context.init_GuiGraphicsExtract().nextStratum();
               }

               first = false;
               UiClipStack.getInstance().clear(context.init_GuiGraphicsExtract());

               try {
                  component.init_V_SyncManager(context);
               } finally {
                  UiClipStack.getInstance().clear(context.init_GuiGraphicsExtract());
               }
            }
         }
      }
   }

   public UiInputResult init_EventBus_I_2(int mouseX, int mouseY, int button) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.init_EventBus_I_2(mouseX, mouseY, button));
   }

   public UiInputResult load(int mouseX, int mouseY, int button) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.load(mouseX, mouseY, button));
   }

   public UiInputResult init(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.init_EventBus_I(mouseX, mouseY, button, deltaX, deltaY));
   }

   public UiInputResult init_EventBus_I(int mouseX, int mouseY, double amount) {
      return this.init_EventBus_I_3(mouseX, mouseY, component -> component.init_EventBus_I(mouseX, mouseY, amount));
   }

   public UiInputResult read(int key, int scanCode, int modifiers) {
      return this.init_EventBus_ByteReader_DataUti(component -> component.read(key, scanCode, modifiers));
   }

   public UiInputResult init_EventBus_C(char chr) {
      return this.init_EventBus_ByteReader_DataUti(component -> component.init_EventBus_C(chr));
   }

   private UiInputResult init_EventBus_ByteReader_DataUti(UiLayerHost.Inner_DataUtil call) {
      UiLayer[] order = UiLayer.values();

      for (int layerIndex = order.length - 1; layerIndex >= 0; layerIndex--) {
         UiLayer layer = order[layerIndex];
         List<UiLayerComponent> components = this.init.get(layer);
         if (components != null && !components.isEmpty()) {
            for (int i = components.size() - 1; i >= 0; i--) {
               if (call.invoke(components.get(i)) == UiInputResult.HANDLED) {
                  return UiInputResult.HANDLED;
               }
            }

            if (layer == UiLayer.MODAL) {
               return UiInputResult.HANDLED;
            }
         }
      }

      return UiInputResult.IGNORED;
   }

   private UiInputResult init_EventBus_I_3(int mouseX, int mouseY, UiLayerHost.Inner_DataUtil call) {
      UiLayer[] order = UiLayer.values();

      for (int layerIndex = order.length - 1; layerIndex >= 0; layerIndex--) {
         UiLayer layer = order[layerIndex];
         List<UiLayerComponent> components = this.init.get(layer);
         if (components != null && !components.isEmpty()) {
            for (int i = components.size() - 1; i >= 0; i--) {
               UiLayerComponent component = components.get(i);
               NetHelper bounds = component.load_NetHelper();
               if (bounds != null && bounds.init_Z(mouseX, mouseY) && call.invoke(component) == UiInputResult.HANDLED) {
                  return UiInputResult.HANDLED;
               }
            }

            if (init_Z_TextFormat(layer)) {
               return UiInputResult.HANDLED;
            }
         }
      }

      return UiInputResult.IGNORED;
   }

   private static boolean init_Z_TextFormat(UiLayer layer) {
      return layer == UiLayer.DROPDOWN || layer == UiLayer.MODAL;
   }

   @FunctionalInterface
   private interface Inner_DataUtil {
      UiInputResult invoke(UiLayerComponent var1);
   }
}
