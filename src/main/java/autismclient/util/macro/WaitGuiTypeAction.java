package autismclient.util.macro;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public class WaitGuiTypeAction implements CaptureOutputAction, MacroAction {
   public WaitGuiTypeAction.Inner_DataUtil waitMode = WaitGuiTypeAction.Inner_DataUtil.OPEN;
   public String guiType = "ANY";
   public String title = "";
   public PatternCaptureMatcher.Inner_DataUtil matchMode = PatternCaptureMatcher.Inner_DataUtil.MATCH;
   public String saveAs = "";
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_GUI_TYPE;
   }

   public boolean init_Z_3(Minecraft mc) {
      return this.init_Optional(mc).isPresent();
   }

   public Optional<Map<String, MacroVarValue>> init_Optional(Minecraft mc) {
      if (mc != null && GuiScreenMatcher.init(mc.gui.screen(), this.guiType, "")) {
         Screen screen = mc.gui.screen();
         String actual = screen.getTitle() == null ? "" : screen.getTitle().getString();
         Map<String, MacroVarValue> values = new LinkedHashMap<>();
         if (this.matchMode == PatternCaptureMatcher.Inner_DataUtil.MATCH) {
            MacroTemplateResolver.Inner_CachePool resolved = MacroVariableContext.init_j_CachePool_Str_Minecraft(this.title, mc);
            if (!resolved.init_Z()) {
               return Optional.empty();
            }

            if (!resolved.init_Str().isBlank() && !GuiScreenMatcher.init(screen, this.guiType, resolved.init_Str())) {
               return Optional.empty();
            }
         } else {
            Optional<PatternCaptureMatcher.Inner_StreamUtil> result = PatternCaptureMatcher.init(this.matchMode, this.title, actual);
            if (result.isEmpty()) {
               return Optional.empty();
            }

            values.putAll(result.get().init_Map());
         }

         if (this.saveAs != null && !this.saveAs.isBlank()) {
            values.put(this.saveAs, MacroVarValue.init(MacroVarValue.Inner_DataUtil.GUI, actual, Map.of("title", MacroVarValue.init_l_Obj(actual), "type", MacroVarValue.init_l_Obj(GuiScreenMatcher.init_Str(screen)))));
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
      tag.putString("matchMode", (this.matchMode == null ? PatternCaptureMatcher.Inner_DataUtil.MATCH : this.matchMode).name());
      tag.putString("saveAs", this.saveAs == null ? "" : this.saveAs);
      tag.putInt("timeoutMs", this.timeoutMs);
      ListenDuringPreviousSerializer.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.waitMode = MacroNbtCodecHelper.init(WaitGuiTypeAction.Inner_DataUtil.class, tag.getStringOr("waitMode", "OPEN"), WaitGuiTypeAction.Inner_DataUtil.OPEN);
      this.guiType = tag.getStringOr("guiType", "ANY");
      this.title = tag.getStringOr("title", "");
      this.matchMode = MacroNbtCodecHelper.init(PatternCaptureMatcher.Inner_DataUtil.class, tag.getStringOr("matchMode", "MATCH"), PatternCaptureMatcher.Inner_DataUtil.MATCH);
      this.saveAs = tag.getStringOr("saveAs", "");
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      ListenDuringPreviousSerializer.load(tag, this);
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
      OPEN,
      CLOSE;
   }
}
