package autismclient.gui.d;

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
   private final List<String> init_List;
   private final List<PromptDialogScreen.Inner_DataUtil> load_List;
   private final Runnable init_Runnable;
   private ThemedTextInputBox init_k;
   private final String init_Str;
   private final Consumer<String> init_Consumer;
   private boolean init_Z;

   public PromptDialogScreen(String title, List<String> lines, List<PromptDialogScreen.Inner_DataUtil> choices, Runnable dismissed) {
      super(Component.literal(title));
      this.init_List = List.copyOf(lines);
      this.load_List = List.copyOf(choices);
      this.init_Runnable = dismissed;
      this.init_Str = null;
      this.init_Consumer = null;
   }

   public PromptDialogScreen(String title, String value, Consumer<String> rename) {
      super(Component.literal(title));
      this.init_Consumer = rename;
      this.init_List = List.of();
      this.init_Runnable = () -> {};
      this.init_Str = value;
      this.load_List = List.of(
         new PromptDialogScreen.Inner_DataUtil("Cancel", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::onClose),
         new PromptDialogScreen.Inner_DataUtil("Rename", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::load_V_17)
      );
   }

   @Override
   protected void init() {
      if (this.init_Str != null && this.init_k == null) {
         this.init_k = new ThemedTextInputBox(this.minecraft, this.font, 0, 0, 100, 16, false);
         this.init_k.load(240);
         this.init_k.init_V_Str(this.init_Str);
         this.init_k.push(true);
         this.init_k.init_Z_KeyEvent(Compat263.newGlfwKeyEvent(65, 0, 2));
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private autismclient.gui.f.NetHelper init_NetHelper_2() {
      int width = Math.max(1, Math.min(this.init_Str == null ? 268 : 220, this.init_I() - 12));
      int height = 52 + (this.init_Str == null ? this.init_List.size() * 12 : 16);
      return autismclient.gui.f.NetHelper.init((this.init_I() - width) / 2, (this.load_I() - height) / 2, width, height);
   }

   private autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper panel, int index) {
      int space = panel.update() - 14 - (this.load_List.size() - 1) * 5;
      int left = panel.read() + 7 + space * index / this.load_List.size() + index * 5;
      return autismclient.gui.f.NetHelper.init(
         left, panel.write() + panel.sync() - 7 - 16, space * (index + 1) / this.load_List.size() - space * index / this.load_List.size(), 16
      );
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(g);

      try {
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(g, this.font, mx, my);
         autismclient.gui.f.NetHelper p = this.init_NetHelper_2();
         autismclient.gui.f.b.DialogFrameRenderer.init(ui, p, 17, this.getTitle().getString(), false);
         int top = p.write() + 17 + 7;

         for (int i = 0; i < this.init_List.size(); i++) {
            String line = ui.init_PoolEntry().init_Str(this.init_List.get(i), p.update() - 14);
            ui.init_PoolEntry()
               .init_V_GuiGraphicsExtract_2(
                  g, line, p.read() + 7, top + 12 * i, i == 0 ? ui.init_TableIndex().init_JsonUtil().prev : ui.init_TableIndex().init_JsonUtil().valid
               );
         }

         if (this.init_k != null) {
            this.init_k.read(p.read() + 7);
            this.init_k.write(top);
            this.init_k.update(p.update() - 14);
            this.init_k.init_V(g, mx, my, delta);
         }

         for (int i = 0; i < this.load_List.size(); i++) {
            PromptDialogScreen.Inner_DataUtil choice = this.load_List.get(i);
            autismclient.gui.f.NetHelper bounds = this.init(p, i);
            autismclient.gui.f.b.CachePool.init(ui, bounds, choice.init_Str(), choice.init_CachePool_DataUtil(), bounds.init_Z(mx, my), false);
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

      MouseButtonEvent mouse = init_MouseButtonEvent_MouseButtonEvent(event);
      autismclient.gui.f.NetHelper p = this.init_NetHelper_2();
      if (autismclient.gui.f.b.DialogFrameRenderer.init(p, 17, (int)mouse.x(), (int)mouse.y())) {
         this.onClose();
         return true;
      }

      for (int i = 0; i < this.load_List.size(); i++) {
         if (this.init(p, i).init_Z((int)mouse.x(), (int)mouse.y())) {
            this.load_List.get(i).init_Runnable().run();
            return true;
         }
      }

      return this.init_k != null && this.init_k.init_Z_D_2(mouse.x(), mouse.y(), Compat263.mouseEventGlfwButton(mouse));
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent mouse = init_MouseButtonEvent_MouseButtonEvent(event);
      return this.init_k != null && this.init_k.load(mouse.x(), mouse.y(), Compat263.mouseEventGlfwButton(mouse));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent mouse = init_MouseButtonEvent_MouseButtonEvent(event);
      return this.init_k != null && this.init_k.init(mouse.x(), mouse.y(), Compat263.mouseEventGlfwButton(mouse), cA.init_D_D(dx), cA.init_D_D(dy));
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (Compat263.keyEventGlfwKey(event) == 256) {
         this.onClose();
         return true;
      }

      if (this.init_k == null) {
         return false;
      }

      if (Compat263.keyEventGlfwKey(event) != 257 && Compat263.keyEventGlfwKey(event) != 335) {
         return this.init_k.init_Z_KeyEvent(event);
      }

      this.load_V_17();
      return true;
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return this.init_k != null && this.init_k.init_Z_CharacterEvent(event);
   }

   private void load_V_17() {
      String value = this.init_k.load_Str().strip();
      if (!value.isEmpty()) {
         this.init_V_Runnable(() -> this.init_Consumer.accept(value));
      }
   }

   public void init_V_Runnable(Runnable action) {
      if (!this.init_Z) {
         this.init_Z = true;
         if (this.init_k != null) {
            this.init_k.push(false);
         }

         OverlayScreenController.load(this.minecraft, this);
         action.run();
      }
   }

   @Override
   public void onClose() {
      this.init_V_Runnable(this.init_Runnable);
   }

   @Override
   public void removed() {
      OverlayScreenController.init_V(this);
      if (this.init_k != null) {
         this.init_k.push(false);
      }

      if (!this.init_Z) {
         this.init_Z = true;
         this.init_Runnable.run();
      }
   }

   public record Inner_DataUtil(String init_Str, autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil, Runnable init_Runnable) {
   }
}
