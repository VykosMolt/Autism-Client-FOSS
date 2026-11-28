package autismclient.util.macro;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public class WaitGuiTypeAction implements CaptureOutputAction, MacroAction {
   public WaitGuiTypeAction.WaitMode waitMode = WaitGuiTypeAction.WaitMode.OPEN;
   public String guiType = "ANY";
   public String title = "";
   public PatternCaptureMatcher.MatchMode matchMode = PatternCaptureMatcher.MatchMode.MATCH;
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

   public boolean matches(Minecraft mc) {
      return this.capture(mc).isPresent();
   }

   public Optional<Map<String, MacroVarValue>> capture(Minecraft mc) {
      if (mc != null && GuiScreenMatcher.init(mc.gui.screen(), this.guiType, "")) {
         Screen screen = mc.gui.screen();
         String actual = screen.getTitle() == null ? "" : screen.getTitle().getString();
         Map<String, MacroVarValue> values = new LinkedHashMap<>();
         if (this.matchMode == PatternCaptureMatcher.MatchMode.MATCH) {
            MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(this.title, mc);
            if (!resolved.success()) {
               return Optional.empty();
            }

            if (!resolved.value().isBlank() && !GuiScreenMatcher.init(screen, this.guiType, resolved.value())) {
               return Optional.empty();
            }
         } else {
            Optional<PatternCaptureMatcher.CaptureResult> result = PatternCaptureMatcher.init(this.matchMode, this.title, actual);
            if (result.isEmpty()) {
               return Optional.empty();
            }

            values.putAll(result.get().variables());
         }

         if (this.saveAs != null && !this.saveAs.isBlank()) {
            values.put(this.saveAs, MacroVarValue.of(MacroVarValue.ValueType.GUI, actual, Map.of("title", MacroVarValue.ofText(actual), "type", MacroVarValue.ofText(GuiScreenMatcher.screenTypeName(screen)))));
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
      tag.putString("matchMode", (this.matchMode == null ? PatternCaptureMatcher.MatchMode.MATCH : this.matchMode).name());
      tag.putString("saveAs", this.saveAs == null ? "" : this.saveAs);
      tag.putInt("timeoutMs", this.timeoutMs);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.waitMode = MacroNbtCodecHelper.parseEnum(WaitGuiTypeAction.WaitMode.class, tag.getStringOr("waitMode", "OPEN"), WaitGuiTypeAction.WaitMode.OPEN);
      this.guiType = tag.getStringOr("guiType", "ANY");
      this.title = tag.getStringOr("title", "");
      this.matchMode = MacroNbtCodecHelper.parseEnum(PatternCaptureMatcher.MatchMode.class, tag.getStringOr("matchMode", "MATCH"), PatternCaptureMatcher.MatchMode.MATCH);
      this.saveAs = tag.getStringOr("saveAs", "");
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      ListenDuringPreviousSerializer.readFromTag(tag, this);
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

   public enum WaitMode {
      OPEN,
      CLOSE;
   }
}
