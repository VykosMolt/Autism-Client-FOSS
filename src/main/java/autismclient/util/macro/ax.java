package autismclient.util.macro;

import autismclient.api.custommenu.CustomMenuSnapshot;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public class ax implements MacroAction {
   public ax.Inner_DataUtil waitMode = ax.Inner_DataUtil.OPEN;
   public String guiType = "ANY";
   public String guiTitle = "";
   public UtilCore.Inner_DataUtil matchMode = UtilCore.Inner_DataUtil.MATCH;
   public String saveAs = "";
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   private boolean init = true;

   @Override
   public StreamGate getType() {
      return StreamGate.WAIT_GUI;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public String getDisplayName() {
      String prefix = this.waitMode == ax.Inner_DataUtil.CLOSE ? "Wait GUI Close: " : "Wait GUI Open: ";
      String type = this.guiType != null && !this.guiType.isBlank() && !"ANY".equals(this.guiType) ? this.guiType : "Any";
      return prefix + type + (this.guiTitle.isEmpty() ? "" : " \"" + this.guiTitle + "\"");
   }

   @Override
   public String getIcon() {
      return "GUI";
   }

   public boolean init(String search, String target) {
      if (search.isEmpty() || target.isEmpty()) {
         return false;
      }

      if (search.equals(target)) {
         return true;
      }

      if (target.toLowerCase().contains(search.toLowerCase())) {
         return true;
      }

      String[] searchWords = search.toLowerCase().split("\\s+");
      String[] targetWords = target.toLowerCase().split("\\s+");
      boolean allFound = true;

      for (String word : searchWords) {
         boolean found = false;

         for (String tWord : targetWords) {
            if (tWord.contains(word) || word.contains(tWord)) {
               found = true;
               break;
            }
         }

         if (!found) {
            allFound = false;
            break;
         }
      }

      return allFound;
   }

   public boolean init(Minecraft mc) {
      return mc != null && this.init(mc.gui.screen()).isPresent();
   }

   public Optional<Map<String, l>> init(Screen screen) {
      if (screen == null) {
         return this.init();
      }

      if (!f.init(screen, this.guiType, "")) {
         return Optional.empty();
      }

      String title = screen.getTitle() == null ? "" : screen.getTitle().getString();
      if (this.matchMode == UtilCore.Inner_DataUtil.MATCH) {
         j.Inner_CachePool resolved = n.init_j_CachePool_Str_Minecraft(this.guiTitle, Minecraft.getInstance());
         if (!resolved.init_Z()) {
            return Optional.empty();
         } else {
            return !resolved.init_Str().isBlank() && !f.init(screen, this.guiType, resolved.init_Str())
               ? Optional.empty()
               : Optional.of(this.init(screen, title, Map.of()));
         }
      } else {
         Optional<UtilCore.Inner_StreamUtil> matched = UtilCore.init(this.matchMode, this.guiTitle, title);
         return matched.isEmpty() ? Optional.empty() : Optional.of(this.init(screen, title, matched.get().init_Map()));
      }
   }

   private Optional<Map<String, l>> init() {
      CustomMenuSnapshot snapshot = autismclient.util.b.CachePool.init_CustomMenuSnapshot();
      boolean anyType = this.guiType == null || this.guiType.isBlank() || "ANY".equalsIgnoreCase(this.guiType);
      if (snapshot != null && (anyType || f.init_Z(this.guiType))) {
         String title = snapshot.title();
         Map<String, l> captures = Map.of();
         if (this.matchMode == UtilCore.Inner_DataUtil.MATCH) {
            j.Inner_CachePool resolved = n.init_j_CachePool_Str_Minecraft(this.guiTitle, Minecraft.getInstance());
            if (!resolved.init_Z() || !resolved.init_Str().isBlank() && !title.toLowerCase(Locale.ROOT).contains(resolved.init_Str().toLowerCase(Locale.ROOT))) {
               return Optional.empty();
            }
         } else {
            Optional<UtilCore.Inner_StreamUtil> matched = UtilCore.init(this.matchMode, this.guiTitle, title);
            if (matched.isEmpty()) {
               return Optional.empty();
            }

            captures = matched.get().init_Map();
         }

         Map<String, l> values = new LinkedHashMap<>(captures);
         if (this.saveAs != null && !this.saveAs.isBlank()) {
            values.put(this.saveAs, l.init(l.Inner_DataUtil.GUI, title, Map.of("title", l.init_l_Obj(title), "type", l.init_l_Obj("CUSTOM_MENU"))));
         }

         return Optional.of(values);
      } else {
         return Optional.empty();
      }
   }

   private Map<String, l> init(Screen screen, String title, Map<String, l> captures) {
      Map<String, l> values = new LinkedHashMap<>(captures);
      if (this.saveAs != null && !this.saveAs.isBlank()) {
         Map<String, l> properties = new LinkedHashMap<>();
         properties.put("title", l.init_l_Obj(title));
         properties.put("type", l.init_l_Obj(f.init_Str(screen)));
         values.put(this.saveAs, l.init(l.Inner_DataUtil.GUI, title, properties));
      }

      return values;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("waitMode", this.waitMode.name());
      tag.putString("guiType", this.guiType == null ? "ANY" : this.guiType);
      tag.putString("guiTitle", this.guiTitle);
      tag.putString("matchMode", (this.matchMode == null ? UtilCore.Inner_DataUtil.MATCH : this.matchMode).name());
      tag.putString("saveAs", this.saveAs == null ? "" : this.saveAs);
      tag.putInt("timeoutMs", this.timeoutMs);
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("waitMode")) {
         try {
            this.waitMode = ax.Inner_DataUtil.valueOf(tag.getStringOr("waitMode", "OPEN"));
         } catch (IllegalArgumentException ignored) {
            this.waitMode = ax.Inner_DataUtil.OPEN;
         }
      }

      if (tag.contains("guiTitle")) {
         this.guiTitle = tag.getStringOr("guiTitle", "");
      } else if (tag.contains("title")) {
         this.guiTitle = tag.getStringOr("title", "");
      }

      this.guiType = tag.getStringOr("guiType", "ANY");
      this.matchMode = i.init(UtilCore.Inner_DataUtil.class, tag.getStringOr("matchMode", "MATCH"), UtilCore.Inner_DataUtil.MATCH);
      this.saveAs = tag.getStringOr("saveAs", "");
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
   }

   public void init(CompoundTag tag) {
      this.fromTag(tag);
      this.guiType = tag.getStringOr("guiType", "ANY");
      this.guiTitle = tag.getStringOr("title", tag.getStringOr("guiTitle", ""));
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      OPEN,
      CLOSE;
   }
}
