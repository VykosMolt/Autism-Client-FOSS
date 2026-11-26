package autismclient.gui.f.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LayoutContainer extends LayoutNode {
   protected final List<LayoutNode> init_List = new ArrayList<>();

   public <T extends LayoutNode> T init_TableIndex_TableIndex(T child) {
      if (child != null) {
         this.init_List.add(child);
         this.load_V();
      }

      return child;
   }

   public void init_V_2() {
      if (!this.init_List.isEmpty()) {
         this.init_List.clear();
         this.load_V();
      }
   }

   public List<LayoutNode> init_List_2() {
      return Collections.unmodifiableList(this.init_List);
   }

   protected abstract void load_V(LayoutContext var1);

   @Override
   public void read_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         this.load_V(context);

         for (LayoutNode child : this.init_List) {
            if (child != null && child.write_Z()) {
               child.read_V_SyncManager(context);
            }
         }
      }
   }

   @Override
   public void init_V_SyncManager(LayoutContext context) {
      if (this.init_Z) {
         for (LayoutNode child : this.init_List) {
            if (child != null && child.write_Z()) {
               child.init_V_SyncManager(context);
            }
         }
      }
   }

   @Override
   public boolean init_Z_SyncManager_2(LayoutContext context, float mouseX, float mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         LayoutNode child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_F_F(mouseX, mouseY) && child.init_Z_SyncManager_2(context, mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean load_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         LayoutNode child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.load_Z_SyncManager(context, mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         LayoutNode child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_SyncManager(context, mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager(LayoutContext context, float mouseX, float mouseY, float amount) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         LayoutNode child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_F_F(mouseX, mouseY) && child.init_Z_SyncManager(context, mouseX, mouseY, amount)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager_3(LayoutContext context, int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         LayoutNode child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_SyncManager_3(context, keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager_2(LayoutContext context, char chr, int modifiers) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         LayoutNode child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_SyncManager_2(context, chr, modifiers)) {
            return true;
         }
      }

      return false;
   }
}
