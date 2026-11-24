package autismclient.gui.d;

import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismConfig;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class TokenStore extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   public static final int init_I = 16;
   public static final int load_I = 9;
   private static final int read = 6;
   private static final int write = 4;
   private static final int update = 10;
   private static final String init_Str = "Multi: login password needed";
   private final Screen init_Screen;
   private EditBox init_EditBox;
   private boolean init_Z = true;
   private String load_Str = "";
   private int sync;
   private int flush;
   private int close;
   private List<FormattedCharSequence> init_List;
   private static final int open = 5;

   public TokenStore(Screen parent) {
      super(Component.literal("Multi Login Password"));
      this.sync = ValueSink.close;
      this.flush = 220;
      this.close = 1;
      this.init_List = List.of();
      this.init_Screen = parent;
   }

   @Override
   protected void init() {
      this.clearWidgets();
      String macroName = autismclient.util.g.PacketUtil.init_PacketUtil().write_Str();
      String macroLabel = "Login Macro: " + (macroName != null && !macroName.isBlank() ? macroName : "none");
      boolean autoCaptcha = AutismConfig.getGlobal().multiAutoSolveCaptcha;
      String captchaLabel = "Auto-solve Captchas: " + (autoCaptcha ? "On" : "Off");
      this.init(macroLabel, captchaLabel);
      int ctl = read_I_15();
      int gap = 2;
      int w = this.open();
      int x = (this.init_I() - w) / 2;
      int inner = w - 8;
      int fieldY = this.update_I_12();
      int revealW = Math.max(this.init_I_Str_18("Hide"), this.init_I_Str_18("Reveal"));
      this.init_EditBox = new StateCache(this.font, x + 4, fieldY, inner - revealW - gap, ctl, Component.literal("Password"));
      this.init_EditBox.setMaxLength(16);
      this.init_EditBox.setHint(Component.literal("Password for all accounts"));
      this.read_V_15();
      this.addRenderableWidget(this.init_EditBox);
      this.init(x + 4 + inner - revealW, fieldY, revealW, this.init_Z ? "Hide" : "Reveal", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> {
         this.init_Z = !this.init_Z;
         this.read_V_15();
      });
      int pitch = ctl + gap;
      int y = this.sync_I_10();
      this.load(x, w, y, "Use This Password for All Accounts", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.write_V_13());
      y += pitch;
      this.load(x, w, y, "Generate Random for Each Account", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.update_V_12());
      y += pitch;
      this.load(
         x,
         w,
         y,
         macroLabel,
         macroName != null && !macroName.isBlank() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         b -> this.load_V_22()
      );
      y += pitch;
      this.load(
         x, w, y, captchaLabel, autoCaptcha ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> {
            AutismConfig config = AutismConfig.getGlobal();
            config.multiAutoSolveCaptcha = !config.multiAutoSolveCaptcha;
            config.save();
            this.init();
         }
      );
      y += pitch;
      this.load(x, w, y, "Not Now", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, b -> this.onClose());
      this.setFocused(this.init_EditBox);
      this.init_EditBox.setFocused(true);
   }

   private void init(String macroLabel, String captchaLabel) {
      int want = k.init(this.font, "Multi: login password needed", init_CharEncoder.init_Identifier_l(l.init), 0);

      for (String label : new String[]{"Use This Password for All Accounts", "Generate Random for Each Account", macroLabel, captchaLabel, "Not Now"}) {
         want = Math.max(want, this.init_I_Str_18(label));
      }

      this.flush = Math.max(1, Math.min(this.init_I() - 12, Math.max(220, Math.min(320, want + 8))));
      this.init_List = this.font.split(FormattedText.of(this.init_Str_11()), Math.max(1, this.flush - 8));
      int statusH = autismclient.gui.f.RenderState.init(12);
      int stackH = 5 * (read_I_15() + 2) - 2;
      this.close = this.sync_I_10() - this.push() + stackH + 2 + statusH + 4;
   }

   private String init_Str_11() {
      return "A server login screen appeared but no password is stored for this profile. Set one password for every account, or generate a different random one per account (9-16 characters). Waiting login macros continue automatically.";
   }

   private static int read_I_15() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int init_I_Str_18(String text) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, text, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int write_I_13() {
      return this.push() + 4 + autismclient.gui.f.RenderState.init(14) + 2;
   }

   private int update_I_12() {
      return this.write_I_13() + this.init_List.size() * 10 + 4;
   }

   private int sync_I_10() {
      return this.update_I_12() + read_I_15() + 4;
   }

   private int flush() {
      return this.sync_I_10() + 5 * (read_I_15() + 2);
   }

   private void load_V_22() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new BitSetUtil(this, autismclient.util.g.PacketUtil.init_PacketUtil().write_Str(), name -> {
            autismclient.util.g.PacketUtil.init_PacketUtil().write_V_Str(name == null ? "" : name);
            this.init();
         }));
      }
   }

   private void read_V_15() {
      if (this.init_EditBox != null) {
         this.init_EditBox.addFormatter((value, offset) -> FormattedCharSequence.forward(this.init_Z ? value : "*".repeat(value.length()), Style.EMPTY));
      }
   }

   private void write_V_13() {
      String password = this.init_EditBox == null ? "" : this.init_EditBox.getValue();
      if (password.isBlank()) {
         this.load("Type a password first (or generate random ones).", ValueSink.poll);
      } else if (password.length() > 16) {
         this.load("Password must be at most 16 characters.", ValueSink.poll);
      } else {
         int updated = autismclient.util.g.PacketUtil.init_PacketUtil().load_I_Str(password);
         if (updated <= 0) {
            this.load("No accounts to store the password for.", ValueSink.poll);
         } else {
            this.load(
               "Password set for " + updated + " account(s).",
               password.length() < 9 ? " Note: it is shorter than 9 characters; some servers may reject it." : ""
            );
         }
      }
   }

   private void update_V_12() {
      int updated = autismclient.util.g.PacketUtil.init_PacketUtil().sync_I();
      if (updated <= 0) {
         this.load("No accounts to generate passwords for.", ValueSink.poll);
      } else {
         this.sync_V_10();
         bi.init("Generated a random password for " + updated + " account(s).", ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync));
         autismclient.util.g.FieldCache snapshot = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
         if (snapshot != null && this.minecraft != null) {
            autismclient.util.g.DataUtil draft = autismclient.util.g.PathHelper.init(snapshot, (Set<String>)null);
            this.minecraft
               .gui
               .setScreen(
                  new UiHelper(
                     this.init_Screen,
                     draft,
                     null,
                     saved -> autismclient.util.g.PacketUtil.init_PacketUtil()
                        .init_V_FieldCache(autismclient.util.g.PathHelper.init_FieldCache(saved, snapshot)),
                     false
                  )
               );
         } else {
            this.onClose();
         }
      }
   }

   private void load(String message, String note) {
      this.sync_V_10();
      bi.init(message + note, ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync));
      this.onClose();
   }

   private void sync_V_10() {
      autismclient.util.g.PacketUtil manager = autismclient.util.g.PacketUtil.init_PacketUtil();
      if (manager.load_Z() && manager.read_Z()) {
         manager.init_PacketUtil_CachePo_Set_Z(Set.of(), true);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(
            graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update)
         );
         int w = this.open();
         int x = (this.init_I() - w) / 2;
         int y = this.push();
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(x, y, w, this.poll()),
            6,
            ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close),
            ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load)
         );
         int textColor = ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
         int mutedColor = ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
         int headerH = autismclient.gui.f.RenderState.init(14);
         k.init(
            graphics,
            this.font,
            "Multi: login password needed",
            init_CharEncoder.init_Identifier_l(l.init),
            textColor,
            x + 4,
            j.init(y + 4, headerH, k.init(init_CharEncoder.init_Identifier_l(l.init)), 0),
            false
         );
         int ty = this.write_I_13();

         for (FormattedCharSequence line : this.init_List) {
            graphics.text(this.font, line, x + 4, ty, mutedColor, false);
            ty += 10;
         }

         if (!this.load_Str.isBlank()) {
            int statusY = j.init(this.flush(), autismclient.gui.f.RenderState.init(12), 9, 0);
            Iterator var21 = this.font.split(FormattedText.of(this.load_Str), w - 8).iterator();
            if (var21.hasNext()) {
               FormattedCharSequence line = (FormattedCharSequence)var21.next();
               graphics.text(this.font, line, x + 4, statusY, this.close(), false);
            }
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int close() {
      if (this.sync == ValueSink.poll) {
         return ct.init_I_I_ct_DataUtil(ValueSink.poll, ct.Inner_DataUtil.flush);
      } else {
         return this.sync == ValueSink.open
            ? ct.init_I_I_ct_DataUtil(ValueSink.open, ct.Inner_DataUtil.sync)
            : ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
      }
   }

   private int open() {
      return this.flush;
   }

   private int poll() {
      return this.close;
   }

   private int push() {
      return Math.max(6, (this.load_I() - this.poll()) / 2);
   }

   private void load(String text, int color) {
      this.load_Str = text == null ? "" : text;
      this.sync = color;
   }

   private void init(int x, int y, int w, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      this.addRenderableWidget(new StreamGate(x, y, Math.max(1, w), read_I_15(), Component.literal(label), tone, press));
   }

   private void load(int panelX, int panelWidth, int y, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      int bw = this.init_I_Str_18(label);
      bw = Math.min(bw, panelWidth - 8);
      this.init(panelX + (panelWidth - bw) / 2, y, bw, label, tone, press);
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }
}
