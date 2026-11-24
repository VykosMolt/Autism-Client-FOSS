package autismclient.gui.f.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class HashUtil extends TableIndex {
   protected final List<TableIndex> init_List = new ArrayList<>();

   public <T extends TableIndex> T init_TableIndex_TableIndex(T child) {
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

   public List<TableIndex> init_List_2() {
      return Collections.unmodifiableList(this.init_List);
   }

   protected abstract void load_V(SyncManager var1);

   @Override
   public void read_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         this.load_V(context);

         for (TableIndex child : this.init_List) {
            if (child != null && child.write_Z()) {
               child.read_V_SyncManager(context);
            }
         }
      }
   }

   @Override
   public void init_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         for (TableIndex child : this.init_List) {
            if (child != null && child.write_Z()) {
               child.init_V_SyncManager(context);
            }
         }
      }
   }

   @Override
   public boolean init_Z_SyncManager_2(SyncManager context, float mouseX, float mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         TableIndex child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_F_F(mouseX, mouseY) && child.init_Z_SyncManager_2(context, mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean load_Z_SyncManager(SyncManager context, float mouseX, float mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         TableIndex child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.load_Z_SyncManager(context, mouseX, mouseY, button)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager(SyncManager context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         TableIndex child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_SyncManager(context, mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager(SyncManager context, float mouseX, float mouseY, float amount) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         TableIndex child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_F_F(mouseX, mouseY) && child.init_Z_SyncManager(context, mouseX, mouseY, amount)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager_3(SyncManager context, int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         TableIndex child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_SyncManager_3(context, keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean init_Z_SyncManager_2(SyncManager context, char chr, int modifiers) {
      if (!this.init_Z) {
         return false;
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         TableIndex child = this.init_List.get(i);
         if (child != null && child.write_Z() && child.init_Z_SyncManager_2(context, chr, modifiers)) {
            return true;
         }
      }

      return false;
   }
}
