package autismclient.util.macro;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public class aJ implements CacheEntry, MacroAction {
   public aJ.Inner_DataUtil waitMode = aJ.Inner_DataUtil.init;
   public String guiType = "ANY";
   public String title = "";
   public UtilCore.Inner_DataUtil matchMode = UtilCore.Inner_DataUtil.init;
   public String saveAs = "";
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_H_StreamGate;
   }

   public boolean init_Z_3(Minecraft mc) {
      return this.init_Optional(mc).isPresent();
   }

   public Optional<Map<String, l>> init_Optional(Minecraft mc) {
      if (mc != null && f.init(mc.gui.screen(), this.guiType, "")) {
         Screen screen = mc.gui.screen();
         String actual = screen.getTitle() == null ? "" : screen.getTitle().getString();
         Map<String, l> values = new LinkedHashMap<>();
         if (this.matchMode == UtilCore.Inner_DataUtil.init) {
            j.Inner_CachePool resolved = n.init_j_CachePool_Str_Minecraft(this.title, mc);
            if (!resolved.init_Z()) {
               return Optional.empty();
            }

            if (!resolved.init_Str().isBlank() && !f.init(screen, this.guiType, resolved.init_Str())) {
               return Optional.empty();
            }
         } else {
            Optional<UtilCore.Inner_StreamUtil> result = UtilCore.init(this.matchMode, this.title, actual);
            if (result.isEmpty()) {
               return Optional.empty();
            }

            values.putAll(result.get().init_Map());
         }

         if (this.saveAs != null && !this.saveAs.isBlank()) {
            values.put(this.saveAs, l.init(l.Inner_DataUtil.poll, actual, Map.of("title", l.init_l_Obj(actual), "type", l.init_l_Obj(f.init_Str(screen)))));
         }

         return Optional.of(values);
      } else {
         return Optional.empty();
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "WAIT_GUI_TYPE");
      tag.putString("waitMode", this.waitMode.name());
      tag.putString("guiType", this.guiType);
      tag.putString("title", this.title);
      tag.putString("matchMode", (this.matchMode == null ? UtilCore.Inner_DataUtil.init : this.matchMode).name());
      tag.putString("saveAs", this.saveAs == null ? "" : this.saveAs);
      tag.putInt("timeoutMs", this.timeoutMs);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.waitMode = i.init(aJ.Inner_DataUtil.class, tag.getStringOr("waitMode", "OPEN"), aJ.Inner_DataUtil.init);
      this.guiType = tag.getStringOr("guiType", "ANY");
      this.title = tag.getStringOr("title", "");
      this.matchMode = i.init(UtilCore.Inner_DataUtil.class, tag.getStringOr("matchMode", "MATCH"), UtilCore.Inner_DataUtil.init);
      this.saveAs = tag.getStringOr("saveAs", "");
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      p.load(tag, this);
   }

   @Override
   public String getDisplayName() {
      return "Wait GUI " + this.guiType;
   }

   @Override
   public String getIcon() {
      return "W";
   }

   @Override
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String value) {
      this.saveAs = value == null ? "" : value;
   }

   public enum Inner_DataUtil {
      init,
      load;
   }
}
