package autismclient.util.macro;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class t implements MacroAction {
   public static final String WAIT_UNTIL_READY = "Until ready";
   public static final String WAIT_FIXED_DELAY = "Fixed delay";
   public static final String WAIT_NO_WAIT = "No wait";
   public LinkedHashSet<String> accountIds = new LinkedHashSet<>();
   public ArrayList<String> accounts = new ArrayList<>();
   public int stepCount = 1;
   public boolean connectIfDown = true;
   public boolean disconnectAfter = false;
   public String waitMode = "Until ready";
   public int waitMs = 30000;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.put("accounts", i.init(this.init_List_12()));
      tag.putInt("stepCount", Math.max(0, this.stepCount));
      tag.putBoolean("connectIfDown", this.connectIfDown);
      tag.putBoolean("disconnectAfter", this.disconnectAfter);
      tag.putString("waitMode", init(this.waitMode));
      tag.putInt("waitMs", Math.max(0, this.waitMs));
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.accounts = i.init(tag.getList("accounts").orElse(new ListTag()));
      this.accounts.removeIf(s -> s == null || s.isBlank());
      this.accountIds = new LinkedHashSet<>();

      for (String id : this.accounts) {
         this.accountIds.add(id.trim());
      }

      this.stepCount = Math.max(0, tag.getIntOr("stepCount", 1));
      this.connectIfDown = tag.getBooleanOr("connectIfDown", true);
      this.disconnectAfter = tag.getBooleanOr("disconnectAfter", false);
      this.waitMode = init(tag.getStringOr("waitMode", "Until ready"));
      this.waitMs = Math.max(0, tag.getIntOr("waitMs", 30000));
   }

   public List<String> init_List_12() {
      ArrayList<String> out = new ArrayList<>();
      if (this.accounts != null) {
         for (String id : this.accounts) {
            if (id != null && !id.isBlank()) {
               String trimmed = id.trim();
               if (!out.contains(trimmed)) {
                  out.add(trimmed);
               }
            }
         }
      }

      if (out.isEmpty() && this.accountIds != null) {
         for (String id : this.accountIds) {
            if (id != null && !id.isBlank()) {
               out.add(id.trim());
            }
         }
      }

      return out;
   }

   public LinkedHashSet<String> init_LinkedHashSet() {
      return new LinkedHashSet<>(this.init_List_12());
   }

   public static String init(String raw) {
      return !"Fixed delay".equals(raw) && !"No wait".equals(raw) ? "Until ready" : raw;
   }

   public static String init(int index) {
      return "empty " + (index + 1);
   }

   public static String load(int index) {
      return "empty" + (index + 1);
   }

   @Override
   public void sanitizeForSharing() {
      ArrayList<String> sanitized = new ArrayList<>();

      for (int i = 0; i < this.init_List_12().size(); i++) {
         sanitized.add(load(i));
      }

      this.accounts = sanitized;
      this.accountIds = new LinkedHashSet<>(sanitized);
   }

   public int init(List<MacroAction> actions, int headerIndex) {
      if (actions != null && headerIndex >= 0 && headerIndex < actions.size()) {
         int max = Math.max(0, Math.min(this.stepCount, actions.size() - headerIndex - 1));
         int count = 0;

         for (int i = headerIndex + 1; i < actions.size() && count < max; i++) {
            MacroAction action = actions.get(i);
            if (action instanceof L || action instanceof O || action instanceof t || action instanceof B || action instanceof HashUtil) {
               break;
            }

            count++;
         }

         return count;
      } else {
         return 0;
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_W_StreamGate;
   }

   @Override
   public String getDisplayName() {
      int steps = Math.max(0, this.stepCount);
      int selected = this.init_List_12().size();
      return "Multi [" + steps + (steps == 1 ? " step" : " steps") + " on " + selected + (selected == 1 ? " account" : " accounts") + "]";
   }

   @Override
   public String getIcon() {
      return "Mlt";
   }
}
