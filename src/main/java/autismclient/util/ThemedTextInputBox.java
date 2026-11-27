package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ThemedTextInputBox {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private final Minecraft mc;
   private final Font font;
   private final autismclient.gui.ui.widgets.TextInputWidget widget = new autismclient.gui.ui.widgets.TextInputWidget().load_BitSetUtil_I(3).write_BitSetUtil(1);
   private int x;
   private int y;
   private int width;
   private int height;
   private boolean submitOnEnter;
   private boolean multiline;
   private boolean enterInsertsNewline;
   private boolean spaceKeyInsertsSpace;
   private boolean suppressNextSpaceChar;
   private Predicate<String> submitHandler;
   private String placeholderText = "";

   public ThemedTextInputBox(Minecraft mc, Font textRenderer, int x, int y, int width, int height, boolean submitOnEnter) {
      this.mc = mc;
      this.font = textRenderer;
      this.x = x;
      this.y = y;
      this.width = Math.max(1, width);
      this.height = Math.max(1, height);
      this.submitOnEnter = submitOnEnter;
      this.widget.init_BitSetUtil_Supplier(() -> this.mc.gui != null ? this.mc.gui.hud.getChat().getRecentChat() : List.of());
      this.syncWidgetBounds();
   }

   public void setSubmitOnEnter(boolean submitOnEnter) {
      this.submitOnEnter = submitOnEnter;
   }

   public void setSubmitHandler(Predicate<String> submitHandler) {
      this.submitHandler = submitHandler;
   }

   public void setChangedListener(Consumer<String> changedListener) {
      this.widget.init_BitSetUtil_Consumer(changedListener);
   }

   public void setDisplayTextProvider(Function<String, Component> displayTextProvider) {
      this.widget.init_BitSetUtil_Function(displayTextProvider);
   }

   public void setRevisionProvider(LongSupplier revisionProvider) {
      this.widget.init_BitSetUtil_LongSupplier(revisionProvider);
   }

   public void setMultiline(boolean multiline) {
      this.multiline = multiline;
      this.widget.update(multiline);
   }

   public void setEnterInsertsNewline(boolean enterInsertsNewline) {
      this.enterInsertsNewline = enterInsertsNewline;
   }

   public void setHoverEffectsEnabled(boolean hoverEffectsEnabled) {
      this.widget.read_BitSetUtil_Z(hoverEffectsEnabled);
   }

   public void setFocusEffectsEnabled(boolean focusEffectsEnabled) {
      this.widget.write(focusEffectsEnabled);
   }

   public void setBackgroundColorOverride(int backgroundColorOverride) {
      this.widget.sync(backgroundColorOverride);
   }

   public void setSpaceKeyInsertsSpace(boolean spaceKeyInsertsSpace) {
      this.spaceKeyInsertsSpace = spaceKeyInsertsSpace;
   }

   public void setHistoryNavigationEnabled(boolean historyNavigationEnabled) {
      this.widget.flush(historyNavigationEnabled);
   }

   public void setPlaceholder(Component placeholder) {
      this.placeholderText = placeholder == null ? "" : ThemedTextHelper.init(placeholder.getString());
      this.widget.load_BitSetUtil_Str(this.placeholderText);
   }

   public String getPlaceholder() {
      return this.placeholderText;
   }

   public void setDrawsBackground(boolean drawsBackground) {
      this.widget.load_BitSetUtil_Z(drawsBackground);
   }

   public void setEditable(boolean editable) {
      this.widget.init_BitSetUtil_Z(editable);
   }

   public void setFilter(Predicate<String> filter) {
      this.widget.init_BitSetUtil_Predicate(filter);
   }

   public void setIntegerOnlyFilter(boolean allowNegative) {
      this.widget.init_BitSetUtil_Predicate(next -> {
         if (next != null && !next.isEmpty()) {
            for (int i = 0; i < next.length(); i++) {
               char chr = next.charAt(i);
               if ((!allowNegative || chr != '-' || i != 0) && !Character.isDigit(chr)) {
                  return false;
               }
            }

            return true;
         } else {
            return true;
         }
      });
   }

   public boolean isFocused() {
      return this.widget.load_Z();
   }

   public void setFocused(boolean focused) {
      this.widget.init_V_Z(focused);
   }

   public void setMaxLength(int maxLength) {
      this.widget.init_BitSetUtil_I(maxLength);
   }

   public void setValue(String text) {
      this.widget.init_BitSetUtil_Str(text);
   }

   public String getValue() {
      return this.widget.init_Str();
   }

   public void insertTextIfFocused(String content) {
      if (content != null && !content.isEmpty() && this.widget.load_Z() && this.widget.init_Z()) {
         this.widget.write(content);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         this.syncWidgetBounds();
         if (!this.isMouseOver(mouseX, mouseY)) {
            this.widget.init_V_Z(false);
            return false;
         } else {
            return this.widget.init_Z_SyncManager_2(this.createInputLayoutContext(), (float)mouseX, (float)mouseY, button);
         }
      }
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean ignored) {
      return this.mouseClicked(click.x(), click.y(), 0);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.syncWidgetBounds();
      return this.widget.load_Z_SyncManager(this.createInputLayoutContext(), (float)mouseX, (float)mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      this.syncWidgetBounds();
      return this.widget.init_Z_SyncManager(this.createInputLayoutContext(), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      this.syncWidgetBounds();
      return !this.isMouseOver(mouseX, mouseY) ? false : this.widget.init_Z_SyncManager(this.createInputLayoutContext(), (float)mouseX, (float)mouseY, (float)amount);
   }

   public boolean keyPressed(KeyEvent keyInput) {
      if (!this.widget.load_Z()) {
         return false;
      }

      int key = Compat263.keyEventGlfwKey(keyInput);
      if (key != 257 && key != 335) {
         if (this.spaceKeyInsertsSpace && key == 32) {
            int mods = Compat263.keyEventGlfwModifiers(keyInput);
            if ((mods & 14) == 0) {
               this.suppressNextSpaceChar = true;
               return this.widget.init_Z_SyncManager_2(this.createInputLayoutContext(), ' ', mods);
            }
         }

         return this.widget.init_Z_SyncManager_3(this.createInputLayoutContext(), key, keyInput.keycode(), Compat263.keyEventGlfwModifiers(keyInput));
      } else {
         if (this.submitOnEnter) {
            return this.submitCurrentText();
         }

         if (this.multiline && this.enterInsertsNewline) {
            return this.widget.init_Z_SyncManager_3(this.createInputLayoutContext(), key, keyInput.keycode(), Compat263.keyEventGlfwModifiers(keyInput));
         }

         this.widget.init_V_Z(false);
         return true;
      }
   }

   public boolean charTyped(CharacterEvent charInput) {
      if (charInput == null) {
         return false;
      }

      if (charInput.codepoint() == 32) {
         if (this.suppressNextSpaceChar) {
            this.suppressNextSpaceChar = false;
            return true;
         }
      } else {
         this.suppressNextSpaceChar = false;
      }

      return this.widget.load_Z() && this.widget.init_Z_SyncManager_2(this.createInputLayoutContext(), (char)charInput.codepoint(), 0);
   }

   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.syncWidgetBounds();
      this.widget.init_V_SyncManager(this.createRenderLayoutContext(context, mouseX, mouseY, delta));
   }

   public int getX() {
      return this.x;
   }

   public void setX(int x) {
      this.x = x;
      this.syncWidgetBounds();
   }

   public int getY() {
      return this.y;
   }

   public void setY(int y) {
      this.y = y;
      this.syncWidgetBounds();
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = Math.max(1, width);
      this.syncWidgetBounds();
   }

   public int getHeight() {
      return this.height;
   }

   public void setHeight(int height) {
      this.height = Math.max(1, height);
      this.syncWidgetBounds();
   }

   public void setCursorToEnd(int selectionEnd) {
      this.widget.flush(selectionEnd);
   }

   public String getSelectedText() {
      return this.widget.load_Str();
   }

   public void insertText(String text) {
      this.widget.write(text);
   }

   private void syncWidgetBounds() {
      this.widget.init_TableIndex_F(this.x, this.y, this.width, this.height);
   }

   private boolean isMouseOver(double mouseX, double mouseY) {
      return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
   }

   private static boolean auditIsKeyCommand(String message) {
      if (!autismclient.commands.CommandManager.hasCommandPrefix(message)) {
         return false;
      }
      String body = autismclient.commands.CommandManager.stripPrefix(message);
      if (body == null) {
         return false;
      }
      String[] parts = body.trim().split("\\s+");
      return parts.length >= 2 && "ai".equalsIgnoreCase(parts[0]) && "key".equalsIgnoreCase(parts[1]);
   }

   private boolean submitCurrentText() {
      String value = this.widget.init_Str().trim();
      if (value.isEmpty()) {
         return true;
      } else if (autismclient.modules.aD.read_Z()) {
         this.widget.init_BitSetUtil_Str("");
         this.widget.init_V_Z(false);
         return true;
      } else {
         boolean sensitive = autismclient.commands.CommandManager.shouldSuppressInHiddenMode(value) || auditIsKeyCommand(value);
         boolean recordBeforeDispatch = this.submitHandler == null && autismclient.commands.CommandManager.hasCommandPrefix(value) && !sensitive;
         if (recordBeforeDispatch) {
            AutismClientMessaging.rememberRecentChat(value);
            this.widget.read_BitSetUtil_Str(value);
         }

         boolean submitted = this.submitHandler != null ? this.submitHandler.test(value) : this.dispatchChatOrCommand(value);
         if (!submitted) {
            return true;
         } else if (sensitive) {
            this.widget.init_BitSetUtil_Str("");
            this.widget.init_V_Z(false);
            return true;
         } else {
            if (!recordBeforeDispatch) {
               AutismClientMessaging.rememberRecentChat(value);
               this.widget.read_BitSetUtil_Str(value);
            }

            this.widget.init_BitSetUtil_Str("");
            this.widget.init_V_Z(false);
            return true;
         }
      }
   }

   private boolean dispatchChatOrCommand(String message) {
      if (autismclient.modules.aD.read_Z()) {
         return true;
      }

      if (autismclient.commands.CommandManager.hasCommandPrefix(message)) {
         if (autismclient.commands.CommandManager.shouldSuppressInHiddenMode(message)) {
            return true;
         }

         String body = autismclient.commands.CommandManager.stripPrefix(message);
         if (!body.isBlank()) {
            autismclient.commands.CommandManager.execute(body);
         }

         return true;
      } else {
         if (this.mc.getConnection() == null) {
            AutismClientAddon.LOG.warn("Failed to send Autism chat message because network handler was null.");
            return false;
         }

         if (message.startsWith("/") && message.length() > 1) {
            this.mc.getConnection().sendCommand(message.substring(1));
         } else {
            this.mc.getConnection().sendChat(message);
         }

         return true;
      }
   }

   private autismclient.gui.ui.layout.LayoutContext createInputLayoutContext() {
      return new autismclient.gui.ui.layout.LayoutContext(
         null, this.font, autismclient.gui.ui.layout.LayoutViewport.create(2.0F), THEME, 0.0F, 0.0F, 0.0F
      );
   }

   private autismclient.gui.ui.layout.LayoutContext createRenderLayoutContext(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
      return new autismclient.gui.ui.layout.LayoutContext(
         drawContext, this.font, autismclient.gui.ui.layout.LayoutViewport.create(2.0F), THEME, mouseX, mouseY, delta
      );
   }
}
