package autismclient.gui.screens;

import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.modules.Module;
import autismclient.modules.f;
import autismclient.util.ItemSnbtCodec;
import autismclient.util.ItemNbtInspector;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.SnbtBraceRepairer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public final class NbtTextEditorScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 430;
   private static final int load_I = 286;
   private static final int read_I = 2;
   private final Screen parent;
   private final Module module;
   private final Setting<?, ?> setting;
   private final NbtTextEditorScreen.Inner_DataUtil mode;
   private final autismclient.gui.ui.widgets.TextInputWidget editor = new autismclient.gui.ui.widgets.TextInputWidget()
      .setMultiline(true)
      .setMaxLength(1048576)
      .setHorizontalPadding(5)
      .setHoverEffectsEnabled(false)
      .setFocusEffectsEnabled(true);
   private autismclient.gui.ui.layout.LayoutContext layoutContext;
   private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper saveButton = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper cancelButton = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper write = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper update = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper sync = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper flush = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private String errorText = "";
   private boolean init_Z;

   public NbtTextEditorScreen(Screen parent, Module module, Setting<?, ?> option, NbtTextEditorScreen.Inner_DataUtil mode) {
      super(Component.literal("Edit " + (mode == null ? "Value" : mode.init_Str)));
      this.parent = parent;
      this.module = module;
      this.setting = option;
      this.mode = mode == null ? NbtTextEditorScreen.Inner_DataUtil.RAW : mode;
      if (this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
         this.editor.setDisplayTextProvider(this::highlightSnbt);
         this.editor.setBackgroundColorOverride(1376389388);
         this.editor.setHoverEffectsEnabled(false);
         this.editor.setFocusEffectsEnabled(false);
      }

      this.editor.setValue(this.load_Str_Str_2(module != null && option != null ? module.value(option.id()) : ""));
      this.editor.setOnChange(ignored -> this.errorText = "");
      this.editor.setFocused(true);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int sw = this.virtualWidth();
         int sh = this.virtualHeight();
         int margin = 6;
         int pad = 4;
         int headerH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
         int ctrlH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
         int w = Math.min(430, Math.max(180, sw - margin * 2));
         int h = Math.min(286, Math.max(130, sh - margin * 2));
         int x = Math.max(margin, (sw - w) / 2);
         int y = Math.max(margin, (sh - h) / 2);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1728053248);
         autismclient.gui.f.NetHelper panel = autismclient.gui.f.NetHelper.init(x, y, w, h);
         autismclient.gui.ui.widgets.DialogFrameRenderer.init(
            ui, panel, headerH, "Edit " + this.mode.init_Str, mx >= x && mx < x + w && my >= y && my < y + headerH
         );
         this.init_NetHelper = autismclient.gui.ui.widgets.DialogFrameRenderer.init(panel, headerH);
         int footerRows = this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT ? 2 : 1;
         int footerHeight = pad * 2 + footerRows * ctrlH + (footerRows - 1) * 2;
         int footerTop = y + h - footerHeight;
         int tipY = y + headerH + 2;
         List<String> tipLines = ui.init_PoolEntry().wrapLines(this.mode.load_Str, Math.max(1, w - pad * 2));
         int maxTipLines = Math.min(2, tipLines.size());
         int lineH = 9;

         for (int i = 0; i < maxTipLines; i++) {
            ui.init_PoolEntry().draw(graphics, tipLines.get(i), x + pad, tipY + i * lineH, ui.init_TableIndex().colors().textSecondary);
         }

         int editorY = tipY + Math.max(1, maxTipLines) * lineH + 2;
         int editorH = Math.max(42, footerTop + pad - 2 - editorY);
         this.editor.setBounds(x + pad, editorY, w - pad * 2, editorH);
         autismclient.gui.ui.layout.LayoutViewport viewport = autismclient.gui.ui.layout.LayoutViewport.create(1.0F);
         this.layoutContext = new autismclient.gui.ui.layout.LayoutContext(graphics, this.font, viewport, THEME, mx, my, delta);
         this.editor.render(this.layoutContext);
         int buttonY = footerTop + pad;
         int saveW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ui.init_PoolEntry().measureWidth("Save"));
         int cancelW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(ui.init_PoolEntry().measureWidth("Cancel"));
         int cancelX = x + w - pad - cancelW;
         int saveX = cancelX - 2 - saveW;
         if (this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
            int toolbarX = x + pad;
            int toolbarW = Math.max(1, w - pad * 2);
            int cellW = Math.max(1, (toolbarW - 6) / 4);
            this.write = autismclient.gui.f.NetHelper.init(toolbarX, buttonY, cellW, ctrlH);
            this.update = autismclient.gui.f.NetHelper.init(this.write.init() + 2, buttonY, cellW, ctrlH);
            this.sync = autismclient.gui.f.NetHelper.init(this.update.init() + 2, buttonY, cellW, ctrlH);
            this.flush = autismclient.gui.f.NetHelper.init(this.sync.init() + 2, buttonY, Math.max(1, x + w - pad - this.sync.init() - 2), ctrlH);
            autismclient.gui.f.b.CachePool.init(ui, this.write, "Format", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.write.init_Z(mx, my), false);
            autismclient.gui.f.b.CachePool.init(
               ui, this.update, "Validate", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.update.init_Z(mx, my), false
            );
            autismclient.gui.f.b.CachePool.init(ui, this.sync, "Copy", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.sync.init_Z(mx, my), false);
            autismclient.gui.f.b.CachePool.init(ui, this.flush, "Paste", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.flush.init_Z(mx, my), false);
            buttonY += ctrlH + 2;
            String status = this.errorText.isBlank()
               ? this.editor.getValue().length() + " / 1,048,576 chars  •  Ctrl+S save  •  Ctrl+Shift+F format"
               : this.errorText;
            ui.init_PoolEntry()
               .drawFitted(
                  graphics,
                  status,
                  x + pad,
                  ui.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(x, buttonY, w, ctrlH)),
                  Math.max(1, saveX - 4 - x - pad),
                  this.errorText.isBlank()
                     ? ui.init_TableIndex().colors().textSecondary
                     : (this.init_Z ? ui.init_TableIndex().colors().success : ui.init_TableIndex().colors().danger)
               );
         } else {
            this.write = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
            this.update = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
            this.sync = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
            this.flush = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
         }

         this.saveButton = autismclient.gui.f.NetHelper.init(saveX, buttonY, saveW, ctrlH);
         this.cancelButton = autismclient.gui.f.NetHelper.init(cancelX, buttonY, cancelW, ctrlH);
         autismclient.gui.f.b.CachePool.init(
            ui, this.saveButton, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, this.saveButton.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.cancelButton, "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.cancelButton.init_Z(mx, my), false
         );
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) == 0 && this.init_NetHelper.init_Z(mx, my)) {
         this.onClose();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.saveButton.init_Z(mx, my)) {
         this.load_V_3();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.cancelButton.init_Z(mx, my)) {
         this.onClose();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.write.init_Z(mx, my)) {
         this.read();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.update.init_Z(mx, my)) {
         this.isValidForMode();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.sync.init_Z(mx, my)) {
         this.write();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.flush.init_Z(mx, my)) {
         this.update();
         return true;
      } else {
         return this.layoutContext == null
            || this.editor.mouseClicked(this.layoutContext, mx, my, Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      return this.layoutContext == null
         || this.editor
            .mouseReleased(this.layoutContext, cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      return this.layoutContext == null
         || this.editor
            .mouseDragged(
               this.layoutContext, cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy
            );
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return this.layoutContext == null
         || this.editor.mouseScrolled(this.layoutContext, cA.init_I_D(mouseX), cA.init_I_D(mouseY), (float)scrollY);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      boolean ctrl = (Compat263.keyEventGlfwModifiers(input) & 10) != 0;
      boolean shift = (Compat263.keyEventGlfwModifiers(input) & 1) != 0;
      if (this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT && ctrl && Compat263.keyEventGlfwKey(input) == 83) {
         this.load_V_3();
         return true;
      }

      if (this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT && ctrl && shift && Compat263.keyEventGlfwKey(input) == 70) {
         this.read();
         return true;
      }

      if (this.mode != NbtTextEditorScreen.Inner_DataUtil.FULL_NBT
         || !ctrl
         || Compat263.keyEventGlfwKey(input) != 257 && Compat263.keyEventGlfwKey(input) != 335) {
         if (this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT && Compat263.keyEventGlfwKey(input) == 258 && this.editor.isFocused()) {
            this.editor.insertText("  ");
            return true;
         } else if (Compat263.keyEventGlfwKey(input) == 256) {
            this.onClose();
            return true;
         } else {
            return this.layoutContext == null
               || this.editor
                  .keyPressed(this.layoutContext, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
         }
      } else {
         this.isValidForMode();
         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.layoutContext == null || this.editor.charTyped(this.layoutContext, (char)input.codepoint(), 0);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   private void load_V_3() {
      if (this.module != null && this.setting != null) {
         String value = this.read(this.editor.getValue());
         if ("nbt-item-components".equals(this.setting.id()) && this.module instanceof f.Inner_DataUtil adminTools) {
            adminTools.load_V_2(value);
         } else if ("nbt-item-stack".equals(this.setting.id()) && this.module instanceof f.Inner_DataUtil adminTools) {
            if (!adminTools.init_Z(value)) {
               this.init_Z = false;
               this.errorText = "Invalid full ItemStack SNBT";
               bi.write("Invalid full ItemStack SNBT.");
               return;
            }
         } else {
            this.module.setValue(this.setting.id(), value);
         }

         if (!"nbt-item-stack".equals(this.setting.id()) && this.module instanceof f.Inner_DataUtil adminTools) {
            adminTools.sync_V_9();
         }
      }

      bi.init(this.mode.init_Str + " updated.", -13248397);
      this.onClose();
   }

   private boolean isValidForMode() {
      if (this.mode != NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
         return true;
      }

      ItemStack stack = ItemSnbtCodec.parseStack(this.editor.getValue());
      if (stack.isEmpty()) {
         this.init_Z = false;
         this.errorText = "Invalid ItemStack SNBT";
         bi.write(this.errorText);
         return false;
      }

      String error = ItemSnbtCodec.validationError(stack);
      this.init_Z = error.isBlank();
      this.errorText = this.init_Z ? "Valid complete ItemStack" : error;
      if (this.init_Z) {
         bi.init("Full ItemStack NBT is valid.", -13248397);
      } else {
         bi.write(error);
      }

      return this.init_Z;
   }

   private void read() {
      if (this.mode == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
         String repaired = SnbtBraceRepairer.repair(this.editor.getValue());
         ItemStack stack = ItemSnbtCodec.parseStack(repaired);
         if (stack.isEmpty()) {
            this.editor.setValue(ItemNbtInspector.prettyPrint(repaired));
            this.init_Z = false;
            this.errorText = "Formatted (structure repaired — still not a valid ItemStack; check ids/values)";
         } else {
            String error = ItemSnbtCodec.validationError(stack);
            this.editor.setValue(ItemNbtInspector.prettyPrint(ItemSnbtCodec.snbt(stack)));
            this.init_Z = error.isBlank();
            this.errorText = this.init_Z ? "Formatted and valid" : "Formatted — " + error;
         }
      }
   }

   private void write() {
      if (this.minecraft != null && this.minecraft.keyboardHandler != null) {
         String selected = this.editor.getSelectedText();
         this.minecraft.keyboardHandler.setClipboard(selected.isEmpty() ? this.editor.getValue() : selected);
         bi.init(selected.isEmpty() ? "Copied full ItemStack NBT." : "Copied selection.");
      }
   }

   private void update() {
      if (this.minecraft != null && this.minecraft.keyboardHandler != null) {
         this.editor.insertText(this.minecraft.keyboardHandler.getClipboard());
      }
   }

   private Component highlightSnbt(String source) {
      String safe = source == null ? "" : source;
      if (safe.length() > 131072) {
         return Component.literal(safe);
      }

      MutableComponent out = Component.empty();
      char quote = 0;
      boolean escaped = false;
      int runStart = 0;
      int runColor = this.init(safe, 0, quote);

      for (int i = 0; i < safe.length(); i++) {
         char ch = safe.charAt(i);
         int color;
         if (quote != 0) {
            color = 15129460;
            if (escaped) {
               escaped = false;
            } else if (ch == '\\') {
               escaped = true;
            } else if (ch == quote) {
               quote = 0;
            }
         } else if (ch != '"' && ch != '\'') {
            color = this.init(safe, i, quote);
         } else {
            quote = ch;
            color = 15129460;
         }

         if (i == 0) {
            runColor = color;
         } else if (color != runColor) {
            this.init(out, safe.substring(runStart, i), runColor);
            runStart = i;
            runColor = color;
         }
      }

      if (runStart < safe.length()) {
         this.init(out, safe.substring(runStart), runColor);
      }

      return out;
   }

   private int init(String source, int index, char quote) {
      if (source != null && index >= 0 && index < source.length()) {
         char ch = source.charAt(index);
         if (Character.isWhitespace(ch)) {
            return 7829367;
         } else if ("{}[],:;".indexOf(ch) >= 0) {
            return 6740463;
         } else if (!Character.isDigit(ch) && ch != '-' && ch != '+') {
            return !Character.isLetter(ch) && ch != 95 && ch != 46 && ch != 33 ? 16316658 : 10936878;
         } else {
            return 11436543;
         }
      } else {
         return 16316658;
      }
   }

   private void init(MutableComponent out, String text, int color) {
      if (text != null && !text.isEmpty()) {
         out.append(Component.literal(text).withStyle(style -> style.withColor(color)));
      }
   }

   private String load_Str_Str_2(String raw) {
      String value = raw == null ? "" : raw;
      String var10000;
      switch (this.mode) {
         case LORE:
            var10000 = value.replace("|", "\n");
            break;
         case ENCHANTMENTS:
            var10000 = String.join("\n", load_List_Str(value));
            break;
         case ATTRIBUTES:
         default:
            var10000 = value;
            break;
         case FULL_NBT:
            var10000 = ItemNbtInspector.prettyPrint(value);
      }

      return var10000;
   }

   private String read(String displayed) {
      String value = displayed == null ? "" : displayed;
      String var10000;
      switch (this.mode) {
         case LORE:
            var10000 = String.join("|", init_List_Str(value));
            break;
         case ENCHANTMENTS:
            var10000 = String.join(",", init_List_Str(value));
            break;
         default:
            var10000 = value;
      }

      return var10000;
   }

   private static List<String> init_List_Str(String value) {
      List<String> out = new ArrayList<>();

      for (String line : value.split("\\R", -1)) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty()) {
            out.add(trimmed);
         }
      }

      return out;
   }

   private static List<String> load_List_Str(String raw) {
      List<String> out = new ArrayList<>();
      if (raw != null && !raw.isBlank()) {
         StringBuilder current = new StringBuilder();
         int depth = 0;
         boolean quoted = false;
         char quote = 0;
         boolean escaped = false;

         for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escaped) {
               current.append(c);
               escaped = false;
            } else if (c == '\\') {
               current.append(c);
               escaped = true;
            } else if (quoted) {
               current.append(c);
               if (c == quote) {
                  quoted = false;
               }
            } else if (c != '\'' && c != '"') {
               if (c == '{' || c == '[' || c == '(') {
                  depth++;
               } else if (c == '}' || c == ']' || c == ')') {
                  depth = Math.max(0, depth - 1);
               }

               if (c == ',' && depth == 0) {
                  String entry = current.toString().trim();
                  if (!entry.isEmpty()) {
                     out.add(entry);
                  }

                  current.setLength(0);
               } else {
                  current.append(c);
               }
            } else {
               current.append(c);
               quoted = true;
               quote = c;
            }
         }

         String entry = current.toString().trim();
         if (!entry.isEmpty()) {
            out.add(entry);
         }

         return out;
      } else {
         return out;
      }
   }

   public enum Inner_DataUtil {
      LORE("Lore", "One lore line per row. Order is preserved."),
      ENCHANTMENTS("Enchantments", "One enchantment per row: minecraft:id:level"),
      ATTRIBUTES("Attributes", "Raw attribute component rows. Advanced values stay exact."),
      FULL_NBT("Full ItemStack NBT", "Complete ItemStack SNBT. Saving validates every field before it can be applied."),
      RAW("Raw Data", "Exact raw value. Multiline editing does not discard data.");

      private final String init_Str;
      private final String load_Str;

      Inner_DataUtil(String title, String tip) {
         this.init_Str = title;
         this.load_Str = tip;
      }
   }
}
