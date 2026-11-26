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
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final Minecraft init_Minecraft;
   private final Font init_Font;
   private final autismclient.gui.f.b.TextInputWidget init_BitSetUtil = new autismclient.gui.f.b.TextInputWidget().load_BitSetUtil_I(3).write_BitSetUtil(1);
   private int init_I;
   private int load_I;
   private int read_I;
   private int write_I;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update;
   private Predicate<String> init_Predicate;
   private String init_Str = "";

   public ThemedTextInputBox(Minecraft mc, Font textRenderer, int x, int y, int width, int height, boolean submitOnEnter) {
      this.init_Minecraft = mc;
      this.init_Font = textRenderer;
      this.init_I = x;
      this.load_I = y;
      this.read_I = Math.max(1, width);
      this.write_I = Math.max(1, height);
      this.init_Z = submitOnEnter;
      this.init_BitSetUtil.init_BitSetUtil_Supplier(() -> this.init_Minecraft.gui != null ? this.init_Minecraft.gui.hud.getChat().getRecentChat() : List.of());
      this.init_V();
   }

   public void init_V_Z(boolean submitOnEnter) {
      this.init_Z = submitOnEnter;
   }

   public void init_V_Predicate(Predicate<String> submitHandler) {
      this.init_Predicate = submitHandler;
   }

   public void init_V_Consumer(Consumer<String> changedListener) {
      this.init_BitSetUtil.init_BitSetUtil_Consumer(changedListener);
   }

   public void init_V_Function(Function<String, Component> displayTextProvider) {
      this.init_BitSetUtil.init_BitSetUtil_Function(displayTextProvider);
   }

   public void init_V_LongSupplier(LongSupplier revisionProvider) {
      this.init_BitSetUtil.init_BitSetUtil_LongSupplier(revisionProvider);
   }

   public void load(boolean multiline) {
      this.load_Z = multiline;
      this.init_BitSetUtil.update(multiline);
   }

   public void read(boolean enterInsertsNewline) {
      this.read_Z = enterInsertsNewline;
   }

   public void write(boolean hoverEffectsEnabled) {
      this.init_BitSetUtil.read_BitSetUtil_Z(hoverEffectsEnabled);
   }

   public void update(boolean focusEffectsEnabled) {
      this.init_BitSetUtil.write(focusEffectsEnabled);
   }

   public void init_V_I(int backgroundColorOverride) {
      this.init_BitSetUtil.sync(backgroundColorOverride);
   }

   public void sync(boolean spaceKeyInsertsSpace) {
      this.write_Z = spaceKeyInsertsSpace;
   }

   public void flush(boolean historyNavigationEnabled) {
      this.init_BitSetUtil.flush(historyNavigationEnabled);
   }

   public void init_V_Component(Component placeholder) {
      this.init_Str = placeholder == null ? "" : ThemedTextHelper.init(placeholder.getString());
      this.init_BitSetUtil.load_BitSetUtil_Str(this.init_Str);
   }

   public String init_Str() {
      return this.init_Str;
   }

   public void close(boolean drawsBackground) {
      this.init_BitSetUtil.load_BitSetUtil_Z(drawsBackground);
   }

   public void open(boolean editable) {
      this.init_BitSetUtil.init_BitSetUtil_Z(editable);
   }

   public void load(Predicate<String> filter) {
      this.init_BitSetUtil.init_BitSetUtil_Predicate(filter);
   }

   public void poll(boolean allowNegative) {
      this.init_BitSetUtil.init_BitSetUtil_Predicate(next -> {
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

   public boolean init_Z() {
      return this.init_BitSetUtil.load_Z();
   }

   public void push(boolean focused) {
      this.init_BitSetUtil.init_V_Z(focused);
   }

   public void load(int maxLength) {
      this.init_BitSetUtil.init_BitSetUtil_I(maxLength);
   }

   public void init_V_Str(String text) {
      this.init_BitSetUtil.init_BitSetUtil_Str(text);
   }

   public String load_Str() {
      return this.init_BitSetUtil.init_Str();
   }

   public void load(String content) {
      if (content != null && !content.isEmpty() && this.init_BitSetUtil.load_Z() && this.init_BitSetUtil.init_Z()) {
         this.init_BitSetUtil.write(content);
      }
   }

   public boolean init_Z_D_2(double mouseX, double mouseY, int button) {
      if (button != 0) {
         return false;
      } else {
         this.init_V();
         if (!this.init(mouseX, mouseY)) {
            this.init_BitSetUtil.init_V_Z(false);
            return false;
         } else {
            return this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager(), (float)mouseX, (float)mouseY, button);
         }
      }
   }

   public boolean init(MouseButtonEvent click, boolean ignored) {
      return this.init_Z_D_2(click.x(), click.y(), 0);
   }

   public boolean load(double mouseX, double mouseY, int button) {
      this.init_V();
      return this.init_BitSetUtil.load_Z_SyncManager(this.init_SyncManager(), (float)mouseX, (float)mouseY, button);
   }

   public boolean init(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      this.init_V();
      return this.init_BitSetUtil.init_Z_SyncManager(this.init_SyncManager(), (float)mouseX, (float)mouseY, button, (float)deltaX, (float)deltaY);
   }

   public boolean init_Z_D(double mouseX, double mouseY, double amount) {
      this.init_V();
      return !this.init(mouseX, mouseY) ? false : this.init_BitSetUtil.init_Z_SyncManager(this.init_SyncManager(), (float)mouseX, (float)mouseY, (float)amount);
   }

   public boolean init_Z_KeyEvent(KeyEvent keyInput) {
      if (!this.init_BitSetUtil.load_Z()) {
         return false;
      }

      int key = Compat263.keyEventGlfwKey(keyInput);
      if (key != 257 && key != 335) {
         if (this.write_Z && key == 32) {
            int mods = Compat263.keyEventGlfwModifiers(keyInput);
            if ((mods & 14) == 0) {
               this.update = true;
               return this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager(), ' ', mods);
            }
         }

         return this.init_BitSetUtil.init_Z_SyncManager_3(this.init_SyncManager(), key, keyInput.keycode(), Compat263.keyEventGlfwModifiers(keyInput));
      } else {
         if (this.init_Z) {
            return this.load_Z();
         }

         if (this.load_Z && this.read_Z) {
            return this.init_BitSetUtil.init_Z_SyncManager_3(this.init_SyncManager(), key, keyInput.keycode(), Compat263.keyEventGlfwModifiers(keyInput));
         }

         this.init_BitSetUtil.init_V_Z(false);
         return true;
      }
   }

   public boolean init_Z_CharacterEvent(CharacterEvent charInput) {
      if (charInput == null) {
         return false;
      }

      if (charInput.codepoint() == 32) {
         if (this.update) {
            this.update = false;
            return true;
         }
      } else {
         this.update = false;
      }

      return this.init_BitSetUtil.load_Z() && this.init_BitSetUtil.init_Z_SyncManager_2(this.init_SyncManager(), (char)charInput.codepoint(), 0);
   }

   public void init_V(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.init_V();
      this.init_BitSetUtil.init_V_SyncManager(this.init_SyncManager(context, mouseX, mouseY, delta));
   }

   public int init_I() {
      return this.init_I;
   }

   public void read(int x) {
      this.init_I = x;
      this.init_V();
   }

   public int load_I() {
      return this.load_I;
   }

   public void write(int y) {
      this.load_I = y;
      this.init_V();
   }

   public int read_I() {
      return this.read_I;
   }

   public void update(int width) {
      this.read_I = Math.max(1, width);
      this.init_V();
   }

   public int write() {
      return this.write_I;
   }

   public void sync(int height) {
      this.write_I = Math.max(1, height);
      this.init_V();
   }

   public void flush(int selectionEnd) {
      this.init_BitSetUtil.flush(selectionEnd);
   }

   public String read_Str() {
      return this.init_BitSetUtil.load_Str();
   }

   public void read(String text) {
      this.init_BitSetUtil.write(text);
   }

   private void init_V() {
      this.init_BitSetUtil.init_TableIndex_F(this.init_I, this.load_I, this.read_I, this.write_I);
   }

   private boolean init(double mouseX, double mouseY) {
      return mouseX >= this.init_I && mouseX < this.init_I + this.read_I && mouseY >= this.load_I && mouseY < this.load_I + this.write_I;
   }

   private static boolean auditIsKeyCommand(String message) {
      if (!autismclient.commands.CommandManager.init_Z(message)) {
         return false;
      }
      String body = autismclient.commands.CommandManager.init_Str(message);
      if (body == null) {
         return false;
      }
      String[] parts = body.trim().split("\\s+");
      return parts.length >= 2 && "ai".equalsIgnoreCase(parts[0]) && "key".equalsIgnoreCase(parts[1]);
   }

   private boolean load_Z() {
      String value = this.init_BitSetUtil.init_Str().trim();
      if (value.isEmpty()) {
         return true;
      } else if (autismclient.modules.aD.read_Z()) {
         this.init_BitSetUtil.init_BitSetUtil_Str("");
         this.init_BitSetUtil.init_V_Z(false);
         return true;
      } else {
         boolean sensitive = autismclient.commands.CommandManager.load_Z(value) || auditIsKeyCommand(value);
         boolean recordBeforeDispatch = this.init_Predicate == null && autismclient.commands.CommandManager.init_Z(value) && !sensitive;
         if (recordBeforeDispatch) {
            AutismClientMessaging.rememberRecentChat(value);
            this.init_BitSetUtil.read_BitSetUtil_Str(value);
         }

         boolean submitted = this.init_Predicate != null ? this.init_Predicate.test(value) : this.init_Z_Str(value);
         if (!submitted) {
            return true;
         } else if (sensitive) {
            this.init_BitSetUtil.init_BitSetUtil_Str("");
            this.init_BitSetUtil.init_V_Z(false);
            return true;
         } else {
            if (!recordBeforeDispatch) {
               AutismClientMessaging.rememberRecentChat(value);
               this.init_BitSetUtil.read_BitSetUtil_Str(value);
            }

            this.init_BitSetUtil.init_BitSetUtil_Str("");
            this.init_BitSetUtil.init_V_Z(false);
            return true;
         }
      }
   }

   private boolean init_Z_Str(String message) {
      if (autismclient.modules.aD.read_Z()) {
         return true;
      }

      if (autismclient.commands.CommandManager.init_Z(message)) {
         if (autismclient.commands.CommandManager.load_Z(message)) {
            return true;
         }

         String body = autismclient.commands.CommandManager.init_Str(message);
         if (!body.isBlank()) {
            autismclient.commands.CommandManager.read(body);
         }

         return true;
      } else {
         if (this.init_Minecraft.getConnection() == null) {
            AutismClientAddon.LOG.warn("Failed to send Autism chat message because network handler was null.");
            return false;
         }

         if (message.startsWith("/") && message.length() > 1) {
            this.init_Minecraft.getConnection().sendCommand(message.substring(1));
         } else {
            this.init_Minecraft.getConnection().sendChat(message);
         }

         return true;
      }
   }

   private autismclient.gui.f.c.LayoutContext init_SyncManager() {
      return new autismclient.gui.f.c.LayoutContext(
         null, this.init_Font, autismclient.gui.f.c.LayoutViewport.init_RingBuffer_F(2.0F), init_CharEncoder, 0.0F, 0.0F, 0.0F
      );
   }

   private autismclient.gui.f.c.LayoutContext init_SyncManager(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
      return new autismclient.gui.f.c.LayoutContext(
         drawContext, this.init_Font, autismclient.gui.f.c.LayoutViewport.init_RingBuffer_F(2.0F), init_CharEncoder, mouseX, mouseY, delta
      );
   }
}
