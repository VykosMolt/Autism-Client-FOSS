package autismclient.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class AutismProfilesOverlay extends DraggableOverlayWindow {
   private static final String init_Str_2 = "autism-profiles";
   private static final String load_Str = "Profiles Library";
   private static final int flush_I = 380;
   private static final int close_I = 1;
   private final autismclient.gui.profiles.ProfilesLibraryView init_StreamUtil;
   private final Font init_Font;
   private boolean write_Z;
   private double init_D;
   private double load_D;
   private boolean update_Z;
   private int open;
   private int poll;
   private int push;
   private int pop;

   public AutismProfilesOverlay(Font font) {
      super("autism-profiles", 300, 170);
      this.init_Font = font;
      this.init_StreamUtil = new autismclient.gui.profiles.ProfilesLibraryView(font);
      this.windowX = 90;
      this.windowY = 40;
   }

   public void write_V_22() {
      this.load(false);
      this.setVisible(!this.windowVisible);
      if (this.windowVisible) {
         OverlayManager.getInstance().bringToFront(this);
      }
   }

   public void open() {
      this.load(false);
      this.setVisible(true);
   }

   public void load(boolean v) {
      if (v != this.update_Z) {
         if (v) {
            this.open = this.windowX;
            this.poll = this.windowY;
            this.push = this.windowWidth;
            this.pop = this.windowHeight;
            this.windowCollapsed = false;
         } else {
            this.windowX = this.open;
            this.windowY = this.poll;
            this.windowWidth = this.push;
            this.windowHeight = this.pop;
         }

         this.update_Z = v;
      }
   }

   @Override
   public void setVisible(boolean v) {
      if (!v && this.windowVisible) {
         this.init_StreamUtil.reset();
      }

      if (!v && this.update_Z) {
         this.windowX = this.open;
         this.windowY = this.poll;
         this.windowWidth = this.push;
         this.windowHeight = this.pop;
         this.update_Z = false;
      }

      super.setVisible(v);
   }

   @Override
   public int getMinWidth() {
      return 160;
   }

   @Override
   public int getMinHeight() {
      return 100;
   }

   @Override
   public AutismOverlay.RenderLayer getRenderLayer() {
      return AutismOverlay.RenderLayer.HOST_SCREEN;
   }

   @Override
   public boolean isPersistent() {
      return false;
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return !this.update_Z;
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.init_StreamUtil.hasFocusedInput();
   }

   @Override
   public void clearInputFocus() {
      this.init_StreamUtil.blurInputs();
   }

   @Override
   public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.windowVisible || this.isClosing()) {
         if (this.update_Z) {
            this.load(ctx, mx, my, delta);
         } else {
            this.windowWidth = this.init_StreamUtil.preferredWidth() + 2;
            int want = this.init_StreamUtil.init(this.windowWidth - 2) + 14 + 1;
            int maxH = Math.min(cA.load_I() - 16, 380);
            this.windowHeight = Math.max(this.getMinHeight(), Math.min(want, maxH));
            OverlayBounds bounds = this.clampCurrentBounds(this);
            this.windowX = bounds.x;
            this.windowY = bounds.y;
            this.windowWidth = bounds.width;
            this.windowHeight = bounds.height;
            autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(ctx, this.init_Font, mx, my).init_SyncManager();
            this.renderWindowFrame(context, this.getBounds(), "Profiles Library", this.windowCollapsed, this.write_Z);
            if (this.windowVisible && !this.isVisuallyCollapsed(this.windowCollapsed)) {
               boolean clipped = this.beginBodyClip(ctx, this.getBounds(), this.windowCollapsed);

               try {
                  this.init_V_SyncManager_2(context, 14, delta);
               } finally {
                  this.endBodyClip(ctx, clipped);
               }
            }
         }
      }
   }

   private void load(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      int sw = cA.init_I();
      int sh = cA.load_I();
      autismclient.gui.f.PacketUtil.init(ctx, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), -1073741824);
      int margin = 6;
      int headerHeight = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int availableHeight = Math.max(1, sh - autismclient.gui.ui.UiDensityMetrics.scaledHeight(44));
      this.windowWidth = Math.max(1, Math.min(sw - margin * 2, this.init_StreamUtil.preferredWidth() + 2));
      this.windowHeight = Math.min(availableHeight, this.init_StreamUtil.init(this.windowWidth - 2) + headerHeight + 1);
      this.windowX = (sw - this.windowWidth) / 2;
      this.windowY = (availableHeight - this.windowHeight) / 2;
      autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(ctx, this.init_Font, mx, my).init_SyncManager();
      autismclient.gui.ui.widgets.WindowFrameRenderer.init(
         context,
         autismclient.gui.f.NetHelper.init(this.windowX, this.windowY, this.windowWidth, this.windowHeight),
         "Profiles Library",
         false,
         false,
         false,
         false,
         true,
         4,
         4,
         headerHeight
      );
      this.init_V_SyncManager_2(context, headerHeight, delta);
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager context, int headerHeight, float delta) {
      autismclient.gui.f.NetHelper body = autismclient.gui.f.NetHelper.init(this.windowX, this.windowY, this.windowWidth, this.windowHeight).load(1, headerHeight, 1, 1);
      if (body.update() != 0 && body.sync() != 0) {
         autismclient.gui.ui.UiClipStack.getInstance().push(context.init_GuiGraphicsExtract(), body);

         try {
            this.init_StreamUtil.render(context, body, delta);
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(context.init_GuiGraphicsExtract());
         }
      }
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      if (!this.windowVisible) {
         return false;
      } else {
         OverlayBounds bounds = this.getBounds();
         if (!this.update_Z && this.isOverHeaderCloseButton(mx, my, bounds)) {
            this.setVisible(false);
            this.write_Z = false;
            return true;
         } else if (!this.update_Z && button == 0 && this.isOverHeader(mx, my)) {
            this.write_Z = true;
            this.init_D = mx - this.windowX;
            this.load_D = my - this.windowY;
            return true;
         } else if (this.windowCollapsed) {
            return false;
         } else {
            return this.init_StreamUtil.mouseClicked((int)Math.round(mx), (int)Math.round(my), button) ? true : this.containsPoint(mx, my);
         }
      }
   }

   @Override
   public boolean mouseReleased(double mx, double my, int button) {
      if (this.write_Z) {
         this.write_Z = false;
         this.saveBounds();
         return true;
      } else {
         return this.init_StreamUtil.load((int)Math.round(mx), (int)Math.round(my), button);
      }
   }

   @Override
   public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
      if (this.write_Z) {
         OverlayBounds c = this.clampToScreen(
            this, new OverlayBounds((int)Math.round(mx - this.init_D), (int)Math.round(my - this.load_D), this.windowWidth, this.windowHeight, this.windowVisible, this.windowCollapsed)
         );
         this.windowX = c.x;
         this.windowY = c.y;
         return true;
      } else {
         return this.windowCollapsed ? false : this.init_StreamUtil.mouseDragged((int)Math.round(mx), (int)Math.round(my), button, dx, dy);
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      return this.windowVisible && !this.windowCollapsed && this.containsPoint(mx, my) ? this.init_StreamUtil.mouseScrolled((int)Math.round(mx), (int)Math.round(my), amount) : false;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.windowVisible && !this.windowCollapsed && this.init_StreamUtil.read(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return this.windowVisible && !this.windowCollapsed && this.init_StreamUtil.charTyped(chr, modifiers);
   }
}
