package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.util.cA;
import autismclient.util.OverlayScreenController;
import autismclient.util.ThemedTextInputBox;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class PromptDialogScreen extends VirtualScaledScreen {
   private static final int init_I = 17;
   private static final int load_I = 5;
   private static final int read = 7;
   private static final int write = 16;
   private final List<String> lines;
   private final List<PromptDialogScreen.Choice> choices;
   private final Runnable onDismiss;
   private ThemedTextInputBox input;
   private final String initialValue;
   private final Consumer<String> onRename;
   private boolean closed;

   public PromptDialogScreen(String title, List<String> lines, List<PromptDialogScreen.Choice> choices, Runnable dismissed) {
      super(Component.literal(title));
      this.lines = List.copyOf(lines);
      this.choices = List.copyOf(choices);
      this.onDismiss = dismissed;
      this.initialValue = null;
      this.onRename = null;
   }

   public PromptDialogScreen(String title, String value, Consumer<String> rename) {
      super(Component.literal(title));
      this.onRename = rename;
      this.lines = List.of();
      this.onDismiss = () -> {};
      this.initialValue = value;
      this.choices = List.of(
         new PromptDialogScreen.Choice("Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::onClose),
         new PromptDialogScreen.Choice("Rename", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::submitRename)
      );
   }

   @Override
   protected void init() {
      if (this.initialValue != null && this.input == null) {
         this.input = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 100, 16, false);
         this.input.setMaxLength(240);
         this.input.setValue(this.initialValue);
         this.input.setFocused(true);
         this.input.keyPressed(Compat263.newGlfwKeyEvent(65, 0, 2));
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private autismclient.gui.f.NetHelper panelBounds() {
      int width = Math.max(1, Math.min(this.initialValue == null ? 268 : 220, this.virtualWidth() - 12));
      int height = 52 + (this.initialValue == null ? this.lines.size() * 12 : 16);
      return autismclient.gui.f.NetHelper.init((this.virtualWidth() - width) / 2, (this.virtualHeight() - height) / 2, width, height);
   }

   private autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper panel, int index) {
      int space = panel.update() - 14 - (this.choices.size() - 1) * 5;
      int left = panel.read() + 7 + space * index / this.choices.size() + index * 5;
      return autismclient.gui.f.NetHelper.init(
         left, panel.write() + panel.sync() - 7 - 16, space * (index + 1) / this.choices.size() - space * index / this.choices.size(), 16
      );
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(g);

      try {
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(g, this.font, mx, my);
         autismclient.gui.f.NetHelper p = this.panelBounds();
         autismclient.gui.ui.widgets.DialogFrameRenderer.draw(ui, p, 17, this.getTitle().getString(), false);
         int top = p.write() + 17 + 7;

         for (int i = 0; i < this.lines.size(); i++) {
            String line = ui.init_PoolEntry().trimToWidth(this.lines.get(i), p.update() - 14);
            ui.init_PoolEntry()
               .draw(
                  g, line, p.read() + 7, top + 12 * i, i == 0 ? ui.init_TableIndex().colors().textPrimary : ui.init_TableIndex().colors().textSecondary
               );
         }

         if (this.input != null) {
            this.input.setX(p.read() + 7);
            this.input.setY(top);
            this.input.setWidth(p.update() - 14);
            this.input.render(g, mx, my, delta);
         }

         for (int i = 0; i < this.choices.size(); i++) {
            PromptDialogScreen.Choice choice = this.choices.get(i);
            autismclient.gui.f.NetHelper bounds = this.init(p, i);
            autismclient.gui.f.b.CachePool.init(ui, bounds, choice.label(), choice.style(), bounds.init_Z(mx, my), false);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(g);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return false;
      }

      MouseButtonEvent mouse = toVirtual(event);
      autismclient.gui.f.NetHelper p = this.panelBounds();
      if (autismclient.gui.ui.widgets.DialogFrameRenderer.isOverHeader(p, 17, (int)mouse.x(), (int)mouse.y())) {
         this.onClose();
         return true;
      }

      for (int i = 0; i < this.choices.size(); i++) {
         if (this.init(p, i).init_Z((int)mouse.x(), (int)mouse.y())) {
            this.choices.get(i).action().run();
            return true;
         }
      }

      return this.input != null && this.input.mouseClicked(mouse.x(), mouse.y(), Compat263.mouseEventGlfwButton(mouse));
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent mouse = toVirtual(event);
      return this.input != null && this.input.mouseReleased(mouse.x(), mouse.y(), Compat263.mouseEventGlfwButton(mouse));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent mouse = toVirtual(event);
      return this.input != null && this.input.mouseDragged(mouse.x(), mouse.y(), Compat263.mouseEventGlfwButton(mouse), cA.init_D_D(dx), cA.init_D_D(dy));
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (Compat263.keyEventGlfwKey(event) == 256) {
         this.onClose();
         return true;
      }

      if (this.input == null) {
         return false;
      }

      if (Compat263.keyEventGlfwKey(event) != 257 && Compat263.keyEventGlfwKey(event) != 335) {
         return this.input.keyPressed(event);
      }

      this.submitRename();
      return true;
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return this.input != null && this.input.charTyped(event);
   }

   private void submitRename() {
      String value = this.input.getValue().strip();
      if (!value.isEmpty()) {
         this.closeThen(() -> this.onRename.accept(value));
      }
   }

   public void closeThen(Runnable action) {
      if (!this.closed) {
         this.closed = true;
         if (this.input != null) {
            this.input.setFocused(false);
         }

         OverlayScreenController.closeModalScreen(this.minecraft, this);
         action.run();
      }
   }

   @Override
   public void onClose() {
      this.closeThen(this.onDismiss);
   }

   @Override
   public void removed() {
      OverlayScreenController.discardModalScreen(this);
      if (this.input != null) {
         this.input.setFocused(false);
      }

      if (!this.closed) {
         this.closed = true;
         this.onDismiss.run();
      }
   }

   public record Choice(String label, autismclient.gui.f.b.CachePool.Inner_DataUtil style, Runnable action) {
   }
}
