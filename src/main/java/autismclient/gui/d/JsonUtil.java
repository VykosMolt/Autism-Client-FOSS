package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.L;
import autismclient.util.aJ;
import autismclient.util.bi;
import autismclient.util.cA;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class JsonUtil extends Screen {
   private static final Identifier init_Identifier = autismclient.gui.f.a.DataUtil.read;
   private static final Identifier load_Identifier = autismclient.gui.f.a.DataUtil.load;
   private static final String init_Str = "Early Access";
   private static final int init_I = -42149;
   private static final int load_I = 8;
   private static final int read_I = 320;
   private static final int write_I = 16;
   private static final int update_I = 4;
   private static final int sync_I = 5;
   private static final int flush = 6;
   private static final int close = 14;
   private static final float init_F = 4.5F;
   private static final int open = 8;
   private static final long init_J = 90L;
   private static final int poll = 300;
   private static final int push = 600;
   private static volatile autismclient.a.DataUtil.Inner_StreamUtil init_DataUtil_StreamUti;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final JsonUtil.Inner_DataUtil init_JsonUtil_DataUtil = new JsonUtil.Inner_DataUtil(
      "quit", "Quit game", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> this.minecraft.stop(), true
   );
   private List<JsonUtil.Inner_DataUtil> init_List = List.of();
   private List<String> load_List = List.of();
   private List<String> read_List = List.of();
   private String load_Str = "";
   private List<String> write_List = List.of();
   private boolean init_Z;
   private boolean load_Z;
   private String read_Str = "";
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private int pop = -1;
   private String write_Str = "";
   private int tick;
   private int run;
   private int get;
   private int set;
   private float load_F;
   private float read_F;
   private boolean sync_Z;

   public JsonUtil() {
      super(Component.literal("Early Access"));
   }

   public static void init_V_DataUtil_StreamUti(autismclient.a.DataUtil.Inner_StreamUtil state) {
      init_DataUtil_StreamUti = state;
   }

   @Override
   protected void init() {
      if (!this.read_Z) {
         this.read_Z = true;
         this.load(false);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void tick() {
      if (init_DataUtil_StreamUti == null && !this.init_Z && !this.load_Z) {
         autismclient.a.DataUtil.Inner_StreamUtil state = this.init_DataUtil_StreamUti();
         boolean autoOffline = state == autismclient.a.DataUtil.Inner_StreamUtil.write;
         boolean autoDonator = state == autismclient.a.DataUtil.Inner_StreamUtil.read && L.load_Z();
         if (!autoOffline && !autoDonator) {
            this.pop = -1;
         } else if (this.pop < 0) {
            this.pop = autoOffline ? 300 : 600;
         } else if (--this.pop <= 0) {
            this.pop = -1;
            this.load(true);
         }
      } else {
         this.pop = -1;
      }
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      this.minecraft.gameRenderer.panorama().extractRenderState(graphics, this.width, this.height);
      float uiMouseX = (float)cA.init_D_D(mouseX);
      float uiMouseY = (float)cA.init_D_D(mouseY);
      this.close();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int screenW = cA.init_I();
         int screenH = cA.load_I();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, screenW, screenH), -939524096);
         autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, this.font, (int)uiMouseX, (int)uiMouseY);
         autismclient.gui.f.b.BufferHelper.init(ctx, autismclient.gui.f.NetHelper.init(this.tick, this.run, this.get, this.set));
         int innerW = this.get - 16;
         int x = this.tick + 8;
         int y = this.run + 8;
         this.init(graphics, "Early Access", init_Identifier, this.init_CharEncoder.load(l.init), x, innerW, y);
         y += k.init(init_Identifier) + 5;
         if (this.sync_Z) {
            this.init(graphics, this.load_F, this.read_F);
            y += 14;
         }

         int lineH = k.init(load_Identifier) + 3;

         for (String line : this.load_List) {
            this.init(graphics, line, load_Identifier, this.init_CharEncoder.load(l.read), x, innerW, y);
            y += lineH;
         }

         if (!this.load_Str.isEmpty()) {
            this.init(graphics, this.load_Str, load_Identifier, this.init_CharEncoder.load(l.read), x, innerW, y);
            y += lineH;
         }

         for (String line : this.read_List) {
            this.init(graphics, line, load_Identifier, this.init_CharEncoder.load(l.write), x, innerW, y);
            y += lineH;
         }

         for (String line : this.write_List) {
            this.init(graphics, line, load_Identifier, -42149, x, innerW, y);
            y += lineH;
         }

         for (JsonUtil.Inner_DataUtil btn : this.init_List) {
            this.init(ctx, graphics, btn, uiMouseX, uiMouseY);
         }

         this.init(ctx, graphics, this.init_JsonUtil_DataUtil, uiMouseX, uiMouseY);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return false;
      }

      float mx = (float)cA.init_D_D(event.x());
      float my = (float)cA.init_D_D(event.y());
      this.close();

      for (JsonUtil.Inner_DataUtil btn : this.init_List) {
         if (btn.init(mx, my)) {
            if (btn.init_Z) {
               btn.init_Runnable.run();
            }

            return true;
         }
      }

      if (this.init_JsonUtil_DataUtil.init(mx, my)) {
         this.init_JsonUtil_DataUtil.init_Runnable.run();
         return true;
      } else {
         return false;
      }
   }

   private autismclient.a.DataUtil.Inner_StreamUtil init_DataUtil_StreamUti() {
      autismclient.a.DataUtil.Inner_StreamUtil forced = init_DataUtil_StreamUti;
      if (forced != null) {
         return forced;
      }

      autismclient.a.DataUtil.Inner_StreamUtil state = autismclient.a.DataUtil.init_DataUtil_StreamUti();
      return this.update_Z && state == autismclient.a.DataUtil.Inner_StreamUtil.load_DataUtil_StreamUti ? autismclient.a.DataUtil.Inner_StreamUtil.read : state;
   }

   private boolean init_Z() {
      return this.init_Z || this.load_Z;
   }

   private int init_I() {
      return this.pop < 0 ? -1 : (this.pop + 19) / 20;
   }

   private String init_Str_DataUtil_StreamUti(autismclient.a.DataUtil.Inner_StreamUtil state) {
      if (this.load_Z) {
         return "Waiting for you to authorize in the browser…";
      }

      if (this.init_Z) {
         String var2;
         switch (autismclient.a.DataUtil.init_DataUtil_DataUtil()) {
            case load_DataUtil_DataUtil:
               var2 = "Connecting to the auth server…";
               break;
            case read:
               var2 = "Refreshing your Discord session…";
               break;
            case write:
               var2 = "Verifying your early-access grant…";
               break;
            case init_DataUtil_DataUtil:
               var2 = "Checking early access…";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var2;
      } else {
         String var10000;
         switch (state) {
            case init_DataUtil_StreamUti:
               var10000 = "Checking early access…";
               break;
            case load_DataUtil_StreamUti:
               var10000 = "This build is donator-only during early access.";
               break;
            case read:
               var10000 = "This Discord account doesn't have the donator role.";
               break;
            case write:
               var10000 = autismclient.a.DataUtil.load_Z() ? "Couldn't confirm the donator role right now." : "Couldn't reach the auth server.";
               break;
            case update:
               var10000 = "Access verified. Loading…";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   private String load(autismclient.a.DataUtil.Inner_StreamUtil state) {
      if (this.load_Z) {
         return "Finish the Discord sign-in in your browser, then come back. If it didn't open, use Copy sign-in link or find the link in the console.";
      }

      if (this.init_Z) {
         return "";
      }

      String tamperNote = autismclient.a.DataUtil.update_Z() ? " Relaunch the game without debugging or modding tools attached." : "";
      int seconds = this.init_I();
      String var10000;
      switch (state) {
         case load_DataUtil_StreamUti:
            var10000 = this.init_Str() + "Sign in with the Discord account that has the donator role." + tamperNote;
            break;
         case read:
            var10000 = L.load_Z()
               ? "Get the donator role on the Discord, then re-check." + (seconds > 0 ? " Re-checking by itself in " + seconds + "s." : "")
               : "Get the donator role on the Discord, then sign in again.";
            break;
         case write:
            var10000 = (autismclient.a.DataUtil.load_Z() ? "The role check didn't go through." : "Check your connection.")
               + (seconds > 0 ? " Retrying by itself in " + seconds + "s." : "")
               + tamperNote;
            break;
         default:
            var10000 = "";
      }

      return var10000;
   }

   private String init_Str() {
      String denial = autismclient.a.DataUtil.init_Str();
      String var10000;
      switch (denial) {
         case "bad_session":
         case "expired_session":
            var10000 = "Your previous session ended. ";
            break;
         case "ip_changed":
            var10000 = "Signed in from a new network, so early access must be reconfirmed. ";
            break;
         case "banned":
            var10000 = "This Discord account is banned. ";
            break;
         case "not_member":
            var10000 = "Join the Discord first. ";
            break;
         case "old_version":
            var10000 = "This early-access build is outdated: grab the latest jar from the Discord. ";
            break;
         default:
            var10000 = "";
      }

      return var10000;
   }

   private List<JsonUtil.Inner_DataUtil> init_List_DataUtil_StreamUti(autismclient.a.DataUtil.Inner_StreamUtil state) {
      boolean busy = this.init_Z();
      if (this.load_Z) {
         return List.of(
            new JsonUtil.Inner_DataUtil(
               "copy-signin", "Copy sign-in link", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> this.init_V_Str(L.poll()), true
            ),
            new JsonUtil.Inner_DataUtil("cancel", "Cancel sign-in", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::read, true)
         );
      }

      List var10000;
      switch (state) {
         case load_DataUtil_StreamUti:
            var10000 = List.of(
               new JsonUtil.Inner_DataUtil(
                  "signin", busy ? "Checking…" : "Sign in with Discord", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::load, !busy
               ),
               new JsonUtil.Inner_DataUtil(
                  "copy-signin", "Copy sign-in link", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> this.init_V_Z(true), !busy
               )
            );
            break;
         case read:
            var10000 = L.load_Z()
               ? List.of(
                  new JsonUtil.Inner_DataUtil(
                     "discord",
                     "Open Discord",
                     autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
                     () -> aJ.init_V("https://discord.com/invite/JZ7XgUCtBu"),
                     true
                  ),
                  new JsonUtil.Inner_DataUtil(
                     "donate", "Donate", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> aJ.init_V("https://ko-fi.com/melonik"), true
                  ),
                  new JsonUtil.Inner_DataUtil("switch", "Switch account", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::write, !busy),
                  new JsonUtil.Inner_DataUtil(
                     "recheck", busy ? "Checking…" : "Re-check now", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::update, !busy
                  )
               )
               : List.of(
                  new JsonUtil.Inner_DataUtil(
                     "discord",
                     "Open Discord",
                     autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
                     () -> aJ.init_V("https://discord.com/invite/JZ7XgUCtBu"),
                     true
                  ),
                  new JsonUtil.Inner_DataUtil(
                     "donate", "Donate", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> aJ.init_V("https://ko-fi.com/melonik"), true
                  ),
                  new JsonUtil.Inner_DataUtil(
                     "signin", busy ? "Checking…" : "Sign in again", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::load, !busy
                  ),
                  new JsonUtil.Inner_DataUtil(
                     "copy-signin", "Copy sign-in link", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> this.init_V_Z(true), !busy
                  )
               );
            break;
         case write:
            var10000 = L.load_Z()
               ? List.of(
                  new JsonUtil.Inner_DataUtil("switch", "Switch account", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::write, !busy),
                  new JsonUtil.Inner_DataUtil(
                     "retry", busy ? "Connecting…" : "Retry now", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::update, !busy
                  )
               )
               : List.of(
                  new JsonUtil.Inner_DataUtil(
                     "retry", busy ? "Connecting…" : "Retry now", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::update, !busy
                  )
               );
            break;
         default:
            var10000 = List.of();
      }

      return var10000;
   }

   private void load() {
      this.init_V_Z(false);
   }

   private void init_V_Z(boolean copyLink) {
      if (!this.init_Z()) {
         this.load_Z = true;
         this.read_Str = "";
         this.update_Z = false;
         this.pop = -1;
         this.sync();
         L.init(code -> this.minecraft.execute(() -> this.load(code)), copyLink ? this::init_V_Str : aJ::init_V);
      }
   }

   private void init_V_Str(String url) {
      this.minecraft.keyboardHandler.setClipboard(url);
      bi.init("Discord sign-in link copied.");
   }

   private void load(String code) {
      this.load_Z = false;
      if (code != null && !code.isEmpty()) {
         if (!"cancelled".equals(code)) {
            if ("not_donator".equals(code)) {
               this.update_Z = true;
            } else {
               this.read_Str = L.init_Str(code);
            }
         }

         this.sync();
      } else {
         autismclient.a.DataUtil.init_V();
         if (autismclient.e.MethodUtil.init()) {
            this.flush();
         } else {
            this.sync();
         }
      }
   }

   private void read() {
      L.init_V();
   }

   private void write() {
      if (!this.init_Z()) {
         L.load_V();
         autismclient.a.DataUtil.load_V();
         this.read_Str = "";
         this.update_Z = false;
         this.pop = -1;
         this.sync();
      }
   }

   private void update() {
      this.load(true);
   }

   private void load(boolean force) {
      if (!this.init_Z()) {
         this.init_Z = true;
         this.read_Str = "";
         this.pop = -1;
         this.sync();
         Runnable done = () -> this.minecraft.execute(() -> {
            this.init_Z = false;
            this.sync();
            if (autismclient.e.ArrayUtil.init()) {
               this.flush();
            }
         });
         if (force) {
            autismclient.a.DataUtil.load(done);
         } else {
            autismclient.a.DataUtil.init(done);
         }
      }
   }

   private void sync() {
      this.write_Str = "";
   }

   private void flush() {
      if (!this.write_Z) {
         this.write_Z = true;
         this.minecraft.gui.setScreen(new TitleScreen());
      }
   }

   private void init(autismclient.gui.f.SyncManager ctx, GuiGraphicsExtractor graphics, JsonUtil.Inner_DataUtil btn, float mouseX, float mouseY) {
      boolean hovered = btn.init_Z && btn.init(mouseX, mouseY);
      autismclient.gui.f.b.CachePool.init(
         ctx, autismclient.gui.f.NetHelper.init(btn.init_I, btn.load_I, btn.read, btn.write), btn.load_Str, btn.init_CachePool_DataUtil, hovered, false
      );
      if (!btn.init_Z) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(btn.init_I, btn.load_I, btn.read, btn.write), 1711276032);
      }
   }

   private void init(GuiGraphicsExtractor graphics, float cx, float cy) {
      int accent = this.init_CharEncoder.load(l.update) & 16777215;
      int head = (int)(System.currentTimeMillis() / 90L % 8L);

      for (int i = 0; i < 8; i++) {
         int age = (head - i + 8) % 8;
         int alpha = 255 - age * 26;
         float radius = age == 0 ? 1.6F : 1.15F;
         double angle = 6.283185307179586 * i / 8.0 - 1.5707963267948966;
         float dx = (float)Math.cos(angle) * 4.5F;
         float dy = (float)Math.sin(angle) * 4.5F;
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, cx + dx, cy + dy, radius, alpha << 24 | accent);
      }
   }

   private void close() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      autismclient.a.DataUtil.Inner_StreamUtil state = this.init_DataUtil_StreamUti();
      String name = state == autismclient.a.DataUtil.Inner_StreamUtil.read ? L.write_Str() : "";
      if (name == null) {
         name = "";
      }

      boolean busy = this.init_Z();
      String key = screenW
         + "x"
         + screenH
         + "|"
         + state
         + "|"
         + this.init_Z
         + "|"
         + this.load_Z
         + "|"
         + autismclient.a.DataUtil.init_DataUtil_DataUtil()
         + "|"
         + this.read_Str
         + "|"
         + name
         + "|"
         + autismclient.a.DataUtil.init_Str()
         + "|"
         + this.init_I()
         + "|"
         + autismclient.a.DataUtil.update_Z();
      if (!key.equals(this.write_Str)) {
         this.write_Str = key;
         this.get = Math.max(1, Math.min(Math.max(1, screenW - 12), 320));
         this.tick = (screenW - this.get) / 2;
         int innerW = Math.max(1, this.get - 16);
         this.load_List = this.init(this.init_Str_DataUtil_StreamUti(state), innerW);
         this.read_List = this.init(this.load(state), innerW);
         this.load_Str = name.isEmpty() ? "" : "Signed in as " + name + ".";
         this.write_List = this.read_Str.isEmpty() ? List.of() : this.init(this.read_Str, innerW);
         this.init_List = this.init_List_DataUtil_StreamUti(state);
         this.sync_Z = busy;
         int lineH = k.init(load_Identifier) + 3;
         int contentH = k.init(init_Identifier)
            + 5
            + (busy ? 14 : 0)
            + this.load_List.size() * lineH
            + (this.load_Str.isEmpty() ? 0 : lineH)
            + this.read_List.size() * lineH
            + this.write_List.size() * lineH;
         int buttonRows = this.init_List.isEmpty() ? 0 : (this.init_List.size() + 1) / 2;
         int stateButtonsH = buttonRows == 0 ? 0 : 6 + buttonRows * 16 + (buttonRows - 1) * 4;
         this.set = 8 + contentH + stateButtonsH + 6 + 16 + 8;
         this.run = Math.max(6, (screenH - this.set) / 2);
         int x = this.tick + 8;
         this.load_F = x + innerW / 2.0F;
         this.read_F = this.run + 8 + k.init(init_Identifier) + 5 + 7.0F;
         int btnY = this.run + 8 + contentH;
         if (!this.init_List.isEmpty()) {
            btnY += 6;
            int cols = this.init_List.size() == 1 ? 1 : 2;
            int btnW = (innerW - (cols - 1) * 4) / cols;

            for (int i = 0; i < this.init_List.size(); i++) {
               int col = i % cols;
               int row = i / cols;
               int bx = x + col * (btnW + 4);
               int w = col == cols - 1 ? x + innerW - bx : btnW;
               this.init_List.get(i).init(bx, btnY + row * 20, w, 16);
            }
         }

         this.init_JsonUtil_DataUtil.init(x, this.run + this.set - 8 - 16, innerW, 16);
      }
   }

   private List<String> init(String text, int maxWidth) {
      List<String> lines = new ArrayList<>();
      if (text != null && !text.isEmpty()) {
         StringBuilder current = new StringBuilder();

         for (String word : text.split(" ")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (current.length() != 0 && k.init(this.font, candidate, load_Identifier, -1) > maxWidth) {
               lines.add(current.toString());
               current.setLength(0);
               current.append(word);
            } else {
               current.setLength(0);
               current.append(candidate);
            }
         }

         if (current.length() > 0) {
            lines.add(current.toString());
         }

         return lines;
      } else {
         return lines;
      }
   }

   private void init(GuiGraphicsExtractor graphics, String text, Identifier fontId, int color, int x, int width, int y) {
      int textW = k.init(this.font, text, fontId, color);
      k.init(graphics, this.font, text, fontId, color, x + (width - textW) / 2, y, false);
   }

   private static final class Inner_DataUtil {
      private final String init_Str;
      private final String load_Str;
      private final autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil;
      private final Runnable init_Runnable;
      private final boolean init_Z;
      private int init_I;
      private int load_I;
      private int read;
      private int write;

      private Inner_DataUtil(String id, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action, boolean enabled) {
         this.init_Str = id;
         this.load_Str = label;
         this.init_CachePool_DataUtil = tone;
         this.init_Runnable = action;
         this.init_Z = enabled;
      }

      private void init(int x, int y, int w, int h) {
         this.init_I = x;
         this.load_I = y;
         this.read = w;
         this.write = h;
      }

      private boolean init(float mx, float my) {
         return mx >= this.init_I && my >= this.load_I && mx < this.init_I + this.read && my < this.load_I + this.write;
      }
   }
}
