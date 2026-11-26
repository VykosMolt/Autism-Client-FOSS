package autismclient.gui.d;

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
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 430;
   private static final int load_I = 286;
   private static final int read_I = 2;
   private final Screen init_Screen;
   private final Module init_Module;
   private final Setting<?, ?> init_Setting;
   private final NbtTextEditorScreen.Inner_DataUtil init_BufferHelper_DataU;
   private final autismclient.gui.f.b.TextInputWidget init_BitSetUtil = new autismclient.gui.f.b.TextInputWidget()
      .update(true)
      .init_BitSetUtil_I(1048576)
      .load_BitSetUtil_I(5)
      .read_BitSetUtil_Z(false)
      .write(true);
   private autismclient.gui.f.c.LayoutContext init_SyncManager;
   private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper load_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper read_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper write = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper update = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper sync = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private autismclient.gui.f.NetHelper flush = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private String init_Str = "";
   private boolean init_Z;

   public NbtTextEditorScreen(Screen parent, Module module, Setting<?, ?> option, NbtTextEditorScreen.Inner_DataUtil mode) {
      super(Component.literal("Edit " + (mode == null ? "Value" : mode.init_Str)));
      this.init_Screen = parent;
      this.init_Module = module;
      this.init_Setting = option;
      this.init_BufferHelper_DataU = mode == null ? NbtTextEditorScreen.Inner_DataUtil.RAW : mode;
      if (this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
         this.init_BitSetUtil.init_BitSetUtil_Function(this::init_Component_Str);
         this.init_BitSetUtil.sync(1376389388);
         this.init_BitSetUtil.read_BitSetUtil_Z(false);
         this.init_BitSetUtil.write(false);
      }

      this.init_BitSetUtil.init_BitSetUtil_Str(this.load_Str_Str_2(module != null && option != null ? module.value(option.id()) : ""));
      this.init_BitSetUtil.init_BitSetUtil_Consumer(ignored -> this.init_Str = "");
      this.init_BitSetUtil.init_V_Z(true);
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
         int sw = this.init_I();
         int sh = this.load_I();
         int margin = 6;
         int pad = 4;
         int headerH = autismclient.gui.f.UiDensityMetrics.init(14);
         int ctrlH = autismclient.gui.f.UiDensityMetrics.init(13);
         int w = Math.min(430, Math.max(180, sw - margin * 2));
         int h = Math.min(286, Math.max(130, sh - margin * 2));
         int x = Math.max(margin, (sw - w) / 2);
         int y = Math.max(margin, (sh - h) / 2);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1728053248);
         autismclient.gui.f.NetHelper panel = autismclient.gui.f.NetHelper.init(x, y, w, h);
         autismclient.gui.f.b.DialogFrameRenderer.init(
            ui, panel, headerH, "Edit " + this.init_BufferHelper_DataU.init_Str, mx >= x && mx < x + w && my >= y && my < y + headerH
         );
         this.init_NetHelper = autismclient.gui.f.b.DialogFrameRenderer.init(panel, headerH);
         int footerRows = this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT ? 2 : 1;
         int footerHeight = pad * 2 + footerRows * ctrlH + (footerRows - 1) * 2;
         int footerTop = y + h - footerHeight;
         int tipY = y + headerH + 2;
         List<String> tipLines = ui.init_PoolEntry().init_List(this.init_BufferHelper_DataU.load_Str, Math.max(1, w - pad * 2));
         int maxTipLines = Math.min(2, tipLines.size());
         int lineH = 9;

         for (int i = 0; i < maxTipLines; i++) {
            ui.init_PoolEntry().init_V_GuiGraphicsExtract_2(graphics, tipLines.get(i), x + pad, tipY + i * lineH, ui.init_TableIndex().init_JsonUtil().valid);
         }

         int editorY = tipY + Math.max(1, maxTipLines) * lineH + 2;
         int editorH = Math.max(42, footerTop + pad - 2 - editorY);
         this.init_BitSetUtil.init_TableIndex_F(x + pad, editorY, w - pad * 2, editorH);
         autismclient.gui.f.c.LayoutViewport viewport = autismclient.gui.f.c.LayoutViewport.init_RingBuffer_F(1.0F);
         this.init_SyncManager = new autismclient.gui.f.c.LayoutContext(graphics, this.font, viewport, init_CharEncoder, mx, my, delta);
         this.init_BitSetUtil.init_V_SyncManager(this.init_SyncManager);
         int buttonY = footerTop + pad;
         int saveW = autismclient.gui.f.UiDensityMetrics.load(ui.init_PoolEntry().init("Save"));
         int cancelW = autismclient.gui.f.UiDensityMetrics.load(ui.init_PoolEntry().init("Cancel"));
         int cancelX = x + w - pad - cancelW;
         int saveX = cancelX - 2 - saveW;
         if (this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
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
            String status = this.init_Str.isBlank()
               ? this.init_BitSetUtil.init_Str().length() + " / 1,048,576 chars  •  Ctrl+S save  •  Ctrl+Shift+F format"
               : this.init_Str;
            ui.init_PoolEntry()
               .load(
                  graphics,
                  status,
                  x + pad,
                  ui.init_PoolEntry().init_I(autismclient.gui.f.NetHelper.init(x, buttonY, w, ctrlH)),
                  Math.max(1, saveX - 4 - x - pad),
                  this.init_Str.isBlank()
                     ? ui.init_TableIndex().init_JsonUtil().valid
                     : (this.init_Z ? ui.init_TableIndex().init_JsonUtil().stop : ui.init_TableIndex().init_JsonUtil().data)
               );
         } else {
            this.write = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
            this.update = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
            this.sync = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
            this.flush = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
         }

         this.load_NetHelper = autismclient.gui.f.NetHelper.init(saveX, buttonY, saveW, ctrlH);
         this.read_NetHelper = autismclient.gui.f.NetHelper.init(cancelX, buttonY, cancelW, ctrlH);
         autismclient.gui.f.b.CachePool.init(
            ui, this.load_NetHelper, "Save", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, this.load_NetHelper.init_Z(mx, my), false
         );
         autismclient.gui.f.b.CachePool.init(
            ui, this.read_NetHelper, "Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this.read_NetHelper.init_Z(mx, my), false
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
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.load_NetHelper.init_Z(mx, my)) {
         this.load_V_3();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.read_NetHelper.init_Z(mx, my)) {
         this.onClose();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.write.init_Z(mx, my)) {
         this.read();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.update.init_Z(mx, my)) {
         this.init_Z_2();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.sync.init_Z(mx, my)) {
         this.write();
         return true;
      } else if (Compat263.mouseEventGlfwButton(event) == 0 && this.flush.init_Z(mx, my)) {
         this.update();
         return true;
      } else {
         return this.init_SyncManager == null
            || this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, mx, my, Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      return this.init_SyncManager == null
         || this.init_BitSetUtil
            .load_Z_SyncManager(this.init_SyncManager, cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      return this.init_SyncManager == null
         || this.init_BitSetUtil
            .init_Z_SyncManager(
               this.init_SyncManager, cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), (float)dx, (float)dy
            );
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return this.init_SyncManager == null
         || this.init_BitSetUtil.init_Z_SyncManager(this.init_SyncManager, cA.init_I_D(mouseX), cA.init_I_D(mouseY), (float)scrollY);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      boolean ctrl = (Compat263.keyEventGlfwModifiers(input) & 10) != 0;
      boolean shift = (Compat263.keyEventGlfwModifiers(input) & 1) != 0;
      if (this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT && ctrl && Compat263.keyEventGlfwKey(input) == 83) {
         this.load_V_3();
         return true;
      }

      if (this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT && ctrl && shift && Compat263.keyEventGlfwKey(input) == 70) {
         this.read();
         return true;
      }

      if (this.init_BufferHelper_DataU != NbtTextEditorScreen.Inner_DataUtil.FULL_NBT
         || !ctrl
         || Compat263.keyEventGlfwKey(input) != 257 && Compat263.keyEventGlfwKey(input) != 335) {
         if (this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT && Compat263.keyEventGlfwKey(input) == 258 && this.init_BitSetUtil.load_Z()) {
            this.init_BitSetUtil.write("  ");
            return true;
         } else if (Compat263.keyEventGlfwKey(input) == 256) {
            this.onClose();
            return true;
         } else {
            return this.init_SyncManager == null
               || this.init_BitSetUtil
                  .init_Z_SyncManager_3(this.init_SyncManager, Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
         }
      } else {
         this.init_Z_2();
         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.init_SyncManager == null || this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager, (char)input.codepoint(), 0);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void load_V_3() {
      if (this.init_Module != null && this.init_Setting != null) {
         String value = this.read(this.init_BitSetUtil.init_Str());
         if ("nbt-item-components".equals(this.init_Setting.id()) && this.init_Module instanceof f.Inner_DataUtil adminTools) {
            adminTools.load_V_2(value);
         } else if ("nbt-item-stack".equals(this.init_Setting.id()) && this.init_Module instanceof f.Inner_DataUtil adminTools) {
            if (!adminTools.init_Z(value)) {
               this.init_Z = false;
               this.init_Str = "Invalid full ItemStack SNBT";
               bi.write("Invalid full ItemStack SNBT.");
               return;
            }
         } else {
            this.init_Module.setValue(this.init_Setting.id(), value);
         }

         if (!"nbt-item-stack".equals(this.init_Setting.id()) && this.init_Module instanceof f.Inner_DataUtil adminTools) {
            adminTools.sync_V_9();
         }
      }

      bi.init(this.init_BufferHelper_DataU.init_Str + " updated.", -13248397);
      this.onClose();
   }

   private boolean init_Z_2() {
      if (this.init_BufferHelper_DataU != NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
         return true;
      }

      ItemStack stack = ItemSnbtCodec.init_ItemStack(this.init_BitSetUtil.init_Str());
      if (stack.isEmpty()) {
         this.init_Z = false;
         this.init_Str = "Invalid ItemStack SNBT";
         bi.write(this.init_Str);
         return false;
      }

      String error = ItemSnbtCodec.read(stack);
      this.init_Z = error.isBlank();
      this.init_Str = this.init_Z ? "Valid complete ItemStack" : error;
      if (this.init_Z) {
         bi.init("Full ItemStack NBT is valid.", -13248397);
      } else {
         bi.write(error);
      }

      return this.init_Z;
   }

   private void read() {
      if (this.init_BufferHelper_DataU == NbtTextEditorScreen.Inner_DataUtil.FULL_NBT) {
         String repaired = SnbtBraceRepairer.init_Str(this.init_BitSetUtil.init_Str());
         ItemStack stack = ItemSnbtCodec.init_ItemStack(repaired);
         if (stack.isEmpty()) {
            this.init_BitSetUtil.init_BitSetUtil_Str(ItemNbtInspector.init_Str_Str(repaired));
            this.init_Z = false;
            this.init_Str = "Formatted (structure repaired — still not a valid ItemStack; check ids/values)";
         } else {
            String error = ItemSnbtCodec.read(stack);
            this.init_BitSetUtil.init_BitSetUtil_Str(ItemNbtInspector.init_Str_Str(ItemSnbtCodec.load(stack)));
            this.init_Z = error.isBlank();
            this.init_Str = this.init_Z ? "Formatted and valid" : "Formatted — " + error;
         }
      }
   }

   private void write() {
      if (this.minecraft != null && this.minecraft.keyboardHandler != null) {
         String selected = this.init_BitSetUtil.load_Str();
         this.minecraft.keyboardHandler.setClipboard(selected.isEmpty() ? this.init_BitSetUtil.init_Str() : selected);
         bi.init(selected.isEmpty() ? "Copied full ItemStack NBT." : "Copied selection.");
      }
   }

   private void update() {
      if (this.minecraft != null && this.minecraft.keyboardHandler != null) {
         this.init_BitSetUtil.write(this.minecraft.keyboardHandler.getClipboard());
      }
   }

   private Component init_Component_Str(String source) {
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
      switch (this.init_BufferHelper_DataU) {
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
            var10000 = ItemNbtInspector.init_Str_Str(value);
      }

      return var10000;
   }

   private String read(String displayed) {
      String value = displayed == null ? "" : displayed;
      String var10000;
      switch (this.init_BufferHelper_DataU) {
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
