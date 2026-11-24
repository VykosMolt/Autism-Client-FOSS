package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class SyncManager implements MacroAction {
   public final List<SyncManager.Inner_DataUtil> entries = new ArrayList<>();

   public void init_V_8() {
      this.entries.clear();
   }

   public boolean init_Z_5() {
      return this.entries.stream().anyMatch(SyncManager.Inner_DataUtil::init_Z);
   }

   public List<SyncManager.Inner_DataUtil> init_List_6() {
      List<SyncManager.Inner_DataUtil> copies = new ArrayList<>(this.entries.size());

      for (SyncManager.Inner_DataUtil entry : this.entries) {
         if (entry != null) {
            copies.add(entry.init_SyncManager_DataUt());
         }
      }

      return copies;
   }

   public void init(List<SyncManager.Inner_DataUtil> values) {
      this.entries.clear();
      if (values != null) {
         for (SyncManager.Inner_DataUtil entry : values) {
            if (entry != null) {
               this.entries.add(entry.init_SyncManager_DataUt());
            }
         }
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player == null || mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("No network connection!");
      } else if (!this.init_Z_5()) {
         AutismClientMessaging.sendPrefixed("No craft recipe selected!");
      } else {
         for (SyncManager.Inner_DataUtil entry : this.entries) {
            if (entry != null && entry.init_Z()) {
               autismclient.util.J.Inner_TaskQueue option = autismclient.util.J.init(mc, entry.init_Str);
               if (option == null) {
                  option = autismclient.util.J.init(mc, entry.init_I);
               }

               if (option == null) {
                  AutismClientMessaging.sendPrefixed("§cRecipe not found: " + entry.load_Str);
                  return;
               }

               int desiredAmount = autismclient.util.J.init(option, entry.read_I, entry.init_Z);
               if (desiredAmount <= 0) {
                  AutismClientMessaging.sendPrefixed("§cNo space or materials for " + entry.load_Str + ".");
                  return;
               }

               autismclient.util.J.Inner_CachePool result = autismclient.util.J.init_J_CachePool(mc, entry.init_Str, entry.init_I, desiredAmount);
               if (!result.init_Z) {
                  AutismClientMessaging.sendPrefixed("§c" + result.init_Str);
                  return;
               }
            }
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "CRAFT");
      ListTag entryTags = new ListTag();

      for (SyncManager.Inner_DataUtil entry : this.entries) {
         if (entry != null && entry.init_Z()) {
            entryTags.add(entry.init_CompoundTag());
         }
      }

      tag.put("entries", entryTags);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.entries.clear();
      if (tag != null) {
         if (tag.contains("entries")) {
            for (Tag element : tag.getList("entries").orElse(new ListTag())) {
               if (element instanceof CompoundTag entryTag) {
                  SyncManager.Inner_DataUtil entry = SyncManager.Inner_DataUtil.init(entryTag);
                  if (entry.init_Z()) {
                     this.entries.add(entry);
                  }
               }
            }
         }

         if (this.entries.isEmpty()) {
            SyncManager.Inner_DataUtil legacyEntry = SyncManager.Inner_DataUtil.init(tag);
            if (legacyEntry.init_Z()) {
               this.entries.add(legacyEntry);
            }
         }
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.get;
   }

   @Override
   public String getDisplayName() {
      if (!this.init_Z_5()) {
         return "Craft (empty)";
      }

      SyncManager.Inner_DataUtil first = this.entries.get(0);
      if (this.entries.size() == 1) {
         return "Craft " + first.init_Str();
      }

      String firstName = first.load_Str != null && !first.load_Str.isBlank() ? first.load_Str : "item";
      return "Craft " + firstName + " (+" + (this.entries.size() - 1) + ")";
   }

   @Override
   public String getIcon() {
      return "C";
   }

   public static final class Inner_DataUtil {
      public int init_I = -1;
      public String init_Str = "";
      public String load_Str = "";
      public String read_Str = "";
      public int load_I = 1;
      public int read_I = 1;
      public boolean init_Z = false;

      public SyncManager.Inner_DataUtil init_SyncManager_DataUt() {
         SyncManager.Inner_DataUtil copy = new SyncManager.Inner_DataUtil();
         copy.init_I = this.init_I;
         copy.init_Str = this.init_Str;
         copy.load_Str = this.load_Str;
         copy.read_Str = this.read_Str;
         copy.load_I = this.load_I;
         copy.read_I = this.read_I;
         copy.init_Z = this.init_Z;
         return copy;
      }

      public boolean init_Z() {
         return (this.init_I >= 0 || !this.init_Str.isBlank()) && !this.load_Str.isBlank();
      }

      public String init_Str() {
         if (!this.init_Z()) {
            return "unset";
         } else {
            return this.init_Z ? this.load_Str + " [Max]" : this.load_Str + " x" + Math.max(1, this.read_I);
         }
      }

      public Component init_Component() {
         Component rich = MacroExecutor.init_Component_Str(this.read_Str);
         return rich != null && !rich.getString().isBlank() ? rich.copy() : Component.literal(this.load_Str == null ? "" : this.load_Str);
      }

      public CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putInt("recipeId", this.init_I);
         tag.putString("recipeKey", this.init_Str == null ? "" : this.init_Str);
         tag.putString("resultName", this.load_Str == null ? "" : this.load_Str);
         if (this.read_Str != null && !this.read_Str.isBlank()) {
            tag.putString("resultNameJson", this.read_Str);
         }

         tag.putInt("resultCount", Math.max(1, this.load_I));
         tag.putInt("amount", Math.max(1, this.read_I));
         tag.putBoolean("useMaxAmount", this.init_Z);
         return tag;
      }

      public static SyncManager.Inner_DataUtil init(CompoundTag tag) {
         SyncManager.Inner_DataUtil entry = new SyncManager.Inner_DataUtil();
         if (tag == null) {
            return entry;
         }

         entry.init_I = tag.getIntOr("recipeId", -1);
         entry.init_Str = tag.getStringOr("recipeKey", "");
         entry.load_Str = tag.getStringOr("resultName", "");
         entry.read_Str = tag.getStringOr("resultNameJson", "");
         entry.load_I = Math.max(1, tag.getIntOr("resultCount", 1));
         entry.read_I = Math.max(1, tag.getIntOr("amount", 1));
         entry.init_Z = tag.getBooleanOr("useMaxAmount", false);
         return entry;
      }

      public static SyncManager.Inner_DataUtil init(autismclient.util.J.Inner_TaskQueue option, int amount, boolean useMaxAmount) {
         SyncManager.Inner_DataUtil entry = new SyncManager.Inner_DataUtil();
         if (option == null) {
            return entry;
         }

         entry.init_I = option.init_I;
         entry.init_Str = option.init_Str;
         entry.load_Str = option.load_Str;
         entry.read_Str = MacroExecutor.init_Str_Component(option.init_Component);
         entry.load_I = Math.max(1, option.init_ItemStack.getCount());
         entry.read_I = Math.max(1, amount);
         entry.init_Z = useMaxAmount;
         return entry;
      }
   }
}
