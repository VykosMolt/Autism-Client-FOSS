package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class CraftAction implements MacroAction {
   public final List<CraftAction.Inner_DataUtil> entries = new ArrayList<>();

   public void init_V_8() {
      this.entries.clear();
   }

   public boolean hasValidEntry() {
      return this.entries.stream().anyMatch(CraftAction.Inner_DataUtil::isValid);
   }

   public List<CraftAction.Inner_DataUtil> copyEntries() {
      List<CraftAction.Inner_DataUtil> copies = new ArrayList<>(this.entries.size());

      for (CraftAction.Inner_DataUtil entry : this.entries) {
         if (entry != null) {
            copies.add(entry.copy());
         }
      }

      return copies;
   }

   public void init(List<CraftAction.Inner_DataUtil> values) {
      this.entries.clear();
      if (values != null) {
         for (CraftAction.Inner_DataUtil entry : values) {
            if (entry != null) {
               this.entries.add(entry.copy());
            }
         }
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc.player == null || mc.getConnection() == null) {
         AutismClientMessaging.sendPrefixed("No network connection!");
      } else if (!this.hasValidEntry()) {
         AutismClientMessaging.sendPrefixed("No craft recipe selected!");
      } else {
         for (CraftAction.Inner_DataUtil entry : this.entries) {
            if (entry != null && entry.isValid()) {
               autismclient.util.AutoCraftingEngine.CraftOption option = autismclient.util.AutoCraftingEngine.findOptionByKey(mc, entry.recipeKey);
               if (option == null) {
                  option = autismclient.util.AutoCraftingEngine.findOptionById(mc, entry.recipeId);
               }

               if (option == null) {
                  AutismClientMessaging.sendPrefixed("§cRecipe not found: " + entry.resultName);
                  return;
               }

               int desiredAmount = autismclient.util.AutoCraftingEngine.resolveTargetAmount(option, entry.amount, entry.useMaxAmount);
               if (desiredAmount <= 0) {
                  AutismClientMessaging.sendPrefixed("§cNo space or materials for " + entry.resultName + ".");
                  return;
               }

               autismclient.util.AutoCraftingEngine.CraftResult result = autismclient.util.AutoCraftingEngine.craft(mc, entry.recipeKey, entry.recipeId, desiredAmount);
               if (!result.success) {
                  AutismClientMessaging.sendPrefixed("§c" + result.message);
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

      for (CraftAction.Inner_DataUtil entry : this.entries) {
         if (entry != null && entry.isValid()) {
            entryTags.add(entry.toTag());
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
                  CraftAction.Inner_DataUtil entry = CraftAction.Inner_DataUtil.init(entryTag);
                  if (entry.isValid()) {
                     this.entries.add(entry);
                  }
               }
            }
         }

         if (this.entries.isEmpty()) {
            CraftAction.Inner_DataUtil legacyEntry = CraftAction.Inner_DataUtil.init(tag);
            if (legacyEntry.isValid()) {
               this.entries.add(legacyEntry);
            }
         }
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.CRAFT;
   }

   @Override
   public String getDisplayName() {
      if (!this.hasValidEntry()) {
         return "Craft (empty)";
      }

      CraftAction.Inner_DataUtil first = this.entries.get(0);
      if (this.entries.size() == 1) {
         return "Craft " + first.init_Str();
      }

      String firstName = first.resultName != null && !first.resultName.isBlank() ? first.resultName : "item";
      return "Craft " + firstName + " (+" + (this.entries.size() - 1) + ")";
   }

   @Override
   public String getIcon() {
      return "C";
   }

   public static final class Inner_DataUtil {
      public int recipeId = -1;
      public String recipeKey = "";
      public String resultName = "";
      public String read_Str = "";
      public int load_I = 1;
      public int amount = 1;
      public boolean useMaxAmount = false;

      public CraftAction.Inner_DataUtil copy() {
         CraftAction.Inner_DataUtil copy = new CraftAction.Inner_DataUtil();
         copy.recipeId = this.recipeId;
         copy.recipeKey = this.recipeKey;
         copy.resultName = this.resultName;
         copy.read_Str = this.read_Str;
         copy.load_I = this.load_I;
         copy.amount = this.amount;
         copy.useMaxAmount = this.useMaxAmount;
         return copy;
      }

      public boolean isValid() {
         return (this.recipeId >= 0 || !this.recipeKey.isBlank()) && !this.resultName.isBlank();
      }

      public String init_Str() {
         if (!this.isValid()) {
            return "unset";
         } else {
            return this.useMaxAmount ? this.resultName + " [Max]" : this.resultName + " x" + Math.max(1, this.amount);
         }
      }

      public Component displayName() {
         Component rich = MacroExecutor.deserializeComponentJson(this.read_Str);
         return rich != null && !rich.getString().isBlank() ? rich.copy() : Component.literal(this.resultName == null ? "" : this.resultName);
      }

      public CompoundTag toTag() {
         CompoundTag tag = new CompoundTag();
         tag.putInt("recipeId", this.recipeId);
         tag.putString("recipeKey", this.recipeKey == null ? "" : this.recipeKey);
         tag.putString("resultName", this.resultName == null ? "" : this.resultName);
         if (this.read_Str != null && !this.read_Str.isBlank()) {
            tag.putString("resultNameJson", this.read_Str);
         }

         tag.putInt("resultCount", Math.max(1, this.load_I));
         tag.putInt("amount", Math.max(1, this.amount));
         tag.putBoolean("useMaxAmount", this.useMaxAmount);
         return tag;
      }

      public static CraftAction.Inner_DataUtil init(CompoundTag tag) {
         CraftAction.Inner_DataUtil entry = new CraftAction.Inner_DataUtil();
         if (tag == null) {
            return entry;
         }

         entry.recipeId = tag.getIntOr("recipeId", -1);
         entry.recipeKey = tag.getStringOr("recipeKey", "");
         entry.resultName = tag.getStringOr("resultName", "");
         entry.read_Str = tag.getStringOr("resultNameJson", "");
         entry.load_I = Math.max(1, tag.getIntOr("resultCount", 1));
         entry.amount = Math.max(1, tag.getIntOr("amount", 1));
         entry.useMaxAmount = tag.getBooleanOr("useMaxAmount", false);
         return entry;
      }

      public static CraftAction.Inner_DataUtil init(autismclient.util.AutoCraftingEngine.CraftOption option, int amount, boolean useMaxAmount) {
         CraftAction.Inner_DataUtil entry = new CraftAction.Inner_DataUtil();
         if (option == null) {
            return entry;
         }

         entry.recipeId = option.recipeKeyHash;
         entry.recipeKey = option.recipeKey;
         entry.resultName = option.outputName;
         entry.read_Str = MacroExecutor.serializeComponentJson(option.outputHoverName);
         entry.load_I = Math.max(1, option.outputStack.getCount());
         entry.amount = Math.max(1, amount);
         entry.useMaxAmount = useMaxAmount;
         return entry;
      }
   }
}
