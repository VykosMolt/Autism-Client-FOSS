package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.AutismConfig;
import autismclient.util.AutismHttp;
import autismclient.util.MicrosoftAuth;
import autismclient.util.cA;
import autismclient.util.ct;
import autismclient.util.cz;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.util.UndashedUuid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineEditBox.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.Model.Simple;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.PlayerSkinRenderCache.RenderInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

public class StreamUtil extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -10519297;
   private static final int load_I = -7429889;
   private static final int read_I = 262;
   private static final int write_I = 148;
   private static final int update_I = 414;
   private static final int sync_I = 3;
   private static final int flush_I = 240;
   private static final int close_I = 10;
   private static final long init_J = 5000L;
   private static final String init_Str = "textures";
   private static final int open = -65536;
   private static final int poll = -15395557;
   private final Screen init_Screen;
   private final List<autismclient.gui.f.b.ByteReader> init_List = new ArrayList<>();
   private final List<StreamUtil.Inner_DataUtil> load_List = new ArrayList<>();
   private final Map<String, StreamUtil.Inner_BufferHelper> init_Map = new LinkedHashMap<>(16, 0.75F, true);
   private final autismclient.gui.f.b.CacheEntry init_CacheEntry = new autismclient.gui.f.b.CacheEntry();
   private autismclient.util.MethodUtil init_MethodUtil = null;
   private EditBox init_EditBox;
   private EditBox load_EditBox;
   private EditBox read_EditBox;
   private autismclient.util.BitSetUtil init_BitSetUtil;
   private Simple init_Model_Simple;
   private Simple load_Model_Simple;
   private autismclient.util.MethodUtil load_MethodUtil = autismclient.util.MethodUtil.init;
   private autismclient.util.BitSetUtil load_BitSetUtil;
   private String load_Str = "";
   private String read_Str = "";
   private boolean init_Z;
   private StreamUtil.Inner_CachePool init_StreamUtil_CachePo = StreamUtil.Inner_CachePool.init;
   private int push;
   private long load_J;
   private Future<?> init_Future;
   private autismclient.gui.f.b.ByteReader init_ByteReader;
   private autismclient.gui.f.b.ByteReader load_ByteReader;
   private int pop;
   private boolean load_Z;
   private int tick = -2147483648;
   private int run;
   private boolean read_Z;
   private float init_F = -5.0F;
   private float load_F = 30.0F;
   private double init_D = Blaze3D.getTime();
   private double load_D;
   private double read_D;
   private boolean write_Z = true;
   private long read_J;
   private List<autismclient.util.BitSetUtil> read_List = List.of();
   private long write_J = -9223372036854775808L;
   private String write_Str = "";
   private int get = -2147483648;
   private List<autismclient.util.BitSetUtil> write_List = List.of();
   private List<autismclient.util.BitSetUtil> update_List;
   private List<StreamUtil.Inner_StreamUtil> sync_List = List.of();
   private static final int set = 0;
   private static final int put = 1;
   private static final int add = 2;
   private int clear = 0;
   private int reset;
   private EditBox write_EditBox;
   private EditBox update_EditBox;
   private MultiLineEditBox init_MultiLineEditBox;
   private String update_Str = "";
   private final List<autismclient.gui.f.b.ByteReader> flush_List = new ArrayList<>();
   private final List<autismclient.gui.f.b.SyncManager> close_List = new ArrayList<>();
   private volatile boolean update_Z;

   public StreamUtil(Screen parent) {
      super(Component.literal("Accounts"));
      this.init_Screen = parent;
   }

   @Override
   protected void init() {
      int fieldX = this.next_I();
      int fieldW = this.prev_I();
      int ctl = this.set_I_3();
      this.init_EditBox = new StateCache(this.font, fieldX, this.data(), fieldW, ctl, Component.literal("Username"));
      this.init_EditBox.setHint(Component.literal("Username"));
      this.init_EditBox.setMaxLength(256);
      this.addRenderableWidget(this.init_EditBox);
      this.load_EditBox = new StateCache(this.font, fieldX, this.data(), fieldW, ctl, Component.literal("Token"));
      this.load_EditBox.setHint(Component.literal("Token"));
      this.load_EditBox.setMaxLength(4096);
      this.addRenderableWidget(this.load_EditBox);
      this.read_EditBox = new StateCache(this.font, fieldX, this.cache(), fieldW, ctl, Component.literal("Search accounts"));
      this.read_EditBox.setHint(Component.literal("Search nickname..."));
      this.read_EditBox.setMaxLength(64);
      this.read_EditBox.setResponder(value -> {
         this.read_Str = init_Str_Str(value);
         this.init_Z = true;
      });
      this.addRenderableWidget(this.read_EditBox);
      this.init_Model_Simple = new Simple(
         this.minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), net.minecraft.client.renderer.rendertype.CachePool::init
      );
      this.load_Model_Simple = new Simple(
         this.minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), net.minecraft.client.renderer.rendertype.CachePool::init
      );
      this.write_EditBox = new StateCache(this.font, 0, 0, 58, ctl, Component.literal("Count"));
      this.write_EditBox.setHint(Component.literal("Count"));
      this.write_EditBox.setMaxLength(4);
      this.write_EditBox.setValue("10");
      this.update_EditBox = new StateCache(this.font, 0, 0, this.flush_I_7(), ctl, Component.literal("Password"));
      this.update_EditBox.setHint(Component.literal("Password"));
      this.update_EditBox.setMaxLength(16);
      this.update_EditBox.setValue(AutismConfig.getGlobal().accountGenSharedPassword);
      this.init_MultiLineEditBox = new Builder()
         .setX(0)
         .setY(0)
         .setPlaceholder(Component.literal("Paste names here, one per line or name:password"))
         .build(this.font, this.flush_I_7(), this.poll_I_3(), Component.literal("Account name list"));
      this.push_V_3();
      this.load_V_20();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.init_Z) {
         this.init_Z = false;
         this.load_Str = this.read_Str;
         this.pop = 0;
         this.init_CacheEntry.load(0, 0);
         this.load_V_20();
      }
   }

   private void load_V_20() {
      this.init_List.clear();
      this.load_List.clear();
      this.load_ByteReader = null;
      this.tick = this.block();
      int ctl = this.set_I_3();
      int gap = 2;
      int backX = this.update_Z_2() ? this.buffer() : (this.sync_Z() ? this.buffer() + 4 : this.next_I());
      int backY = this.update_Z_2() ? this.stop_I() : (this.sync_Z() ? this.count() : this.valid_I());
      this.init_List
         .add(
            autismclient.gui.f.b.ByteReader.init(
                  backX, backY, this.load_I_Str_3("Back"), ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.init_Screen)
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init)
         );
      if (!this.sync_Z()) {
         int formX = this.next_I();
         int formW = this.prev_I();
         int providerW = Math.max(1, (formW - gap * 3) / 4);
         int providerY = this.size();
         this.init_V_I_2(formX, providerY, providerW, autismclient.util.MethodUtil.init, "Cracked");
         this.init_V_I_2(formX + providerW + gap, providerY, providerW, autismclient.util.MethodUtil.write, "Altening");
         this.init_V_I_2(formX + (providerW + gap) * 2, providerY, providerW, autismclient.util.MethodUtil.load, "Session");
         this.init_V_I_2(formX + (providerW + gap) * 3, providerY, providerW, autismclient.util.MethodUtil.read, "Microsoft");
         boolean microsoft = this.load_MethodUtil == autismclient.util.MethodUtil.read;
         int actionY = microsoft ? this.data() : this.value();
         int addWidth = microsoft ? formW : this.load_I_Str_3(this.read_Str_3());
         autismclient.gui.f.b.ByteReader add = autismclient.gui.f.b.ByteReader.init(
               formX, actionY, addWidth, ctl, Component.literal(this.read_Str_3()), b -> this.read_V_13()
            )
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write);
         add.init_Z = !this.init_Z_10();
         this.init_List.add(add);
         if (!microsoft) {
            autismclient.gui.f.b.ByteReader clear = autismclient.gui.f.b.ByteReader.init(
                  formX + addWidth + gap, this.value(), this.load_I_Str_3("Clear"), ctl, Component.literal("Clear"), b -> this.poll_V_3()
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init);
            clear.init_Z = !this.init_Z_10();
            this.init_List.add(clear);
         }

         int cancelWidth = microsoft ? formW : this.load_I_Str_3("Cancel");
         int cancelX = microsoft ? formX : formX + formW - cancelWidth;
         autismclient.gui.f.b.ByteReader cancel = autismclient.gui.f.b.ByteReader.init(
               cancelX, this.value(), cancelWidth, ctl, Component.literal("Cancel"), b -> this.close_V_5()
            )
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load);
         cancel.load_Z = this.load_Z_4();
         this.init_ByteReader = cancel;
         this.init_List.add(cancel);
         autismclient.util.MethodUtil[] filterTypes = new autismclient.util.MethodUtil[]{
            autismclient.util.MethodUtil.init,
            autismclient.util.MethodUtil.read,
            autismclient.util.MethodUtil.load,
            autismclient.util.MethodUtil.write,
            autismclient.util.MethodUtil.update
         };
         String[] filterLabels = new String[]{"Cracked", "Microsoft", "Session", "Altening", "Generated"};
         int[] filterWidths = this.init_IArr_I_3(formW, gap, filterLabels);
         int filterX = formX;

         for (int i = 0; i < filterTypes.length; i++) {
            this.load(filterX, this.state(), filterWidths[i], filterTypes[i], filterLabels[i]);
            filterX += filterWidths[i] + gap;
         }
      }

      if (!this.flush_Z()) {
         boolean checking = this.init_StreamUtil_CachePo == StreamUtil.Inner_CachePool.update;
         int headerY = this.count();
         int cursor = this.bind();
         int checkWidth = this.parse();
         cursor -= checkWidth;
         autismclient.gui.f.b.ByteReader check = autismclient.gui.f.b.ByteReader.init(
            cursor, headerY, checkWidth, ctl, Component.literal(checking ? "Checking" : "Check"), b -> this.sync_V_8()
         );
         check.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init);
         check.init_Z = !this.init_Z_10() && !this.read_List_2().isEmpty();
         this.init_List.add(check);
         this.load_ByteReader = check;
         int clearExpiredWidth = this.load_I_Str_3("Clear Expired");
         cursor -= gap + clearExpiredWidth;
         autismclient.gui.f.b.ByteReader clearExpired = autismclient.gui.f.b.ByteReader.init(
            cursor, headerY, clearExpiredWidth, ctl, Component.literal("Clear Expired"), b -> this.flush_V_7()
         );
         clearExpired.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load);
         clearExpired.init_Z = !this.init_Z_10() && this.read_I_13() > 0;
         this.init_List.add(clearExpired);
         int generateWidth = this.load_I_Str_3("Generate");
         cursor -= gap + generateWidth;
         autismclient.gui.f.b.ByteReader generate = autismclient.gui.f.b.ByteReader.init(
            cursor, headerY, generateWidth, ctl, Component.literal("Generate"), b -> this.pop_V_3()
         );
         generate.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init);
         this.init_List.add(generate);
         int clearListWidth = this.load_I_Str_3("Clear List");
         cursor -= gap + clearListWidth;
         autismclient.gui.f.b.ByteReader clearList = autismclient.gui.f.b.ByteReader.init(
            cursor, headerY, clearListWidth, ctl, Component.literal("Clear List"), b -> this.run_V_3()
         );
         clearList.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load);
         clearList.init_Z = !this.read_List_2().isEmpty();
         this.init_List.add(clearList);
      }

      List<StreamUtil.Inner_StreamUtil> displayAccounts = this.load_List_5();
      if (!this.flush_Z()) {
         int maxScroll = this.load_I_I_4(displayAccounts.size());
         int pitch = this.add_I_2();
         int rowH = this.put_I_2();
         int btnH = this.clear_I_2();
         int loginWidth = this.scan();
         int top = this.frame();
         int bottom = this.block();
         this.pop = init_I_I_3(this.pop, pitch, maxScroll);
         this.init_CacheEntry.load(this.pop, maxScroll);
         int firstVisible = this.pop / pitch;
         int rowY = top - this.pop % pitch;

         for (int i = firstVisible; i < displayAccounts.size() && rowY + rowH <= bottom; i++) {
            StreamUtil.Inner_StreamUtil displayRow = displayAccounts.get(i);
            autismclient.util.BitSetUtil account = displayRow.init_BitSetUtil();
            if (rowY + rowH <= top) {
               rowY += pitch;
            } else {
               boolean current = displayRow.init_Z() ? this.write_Z_2() : this.read_Z_BitSetUtil(account);
               boolean renameable = !displayRow.init_Z() && account.init_MethodUtil == autismclient.util.MethodUtil.init;
               int btnY = this.load(rowY, btnH);
               int cursor = this.fill() - 4;
               autismclient.gui.f.b.ByteReader delete = null;
               autismclient.gui.f.b.ByteReader share = null;
               autismclient.gui.f.b.ByteReader rename = null;
               if (!displayRow.init_Z()) {
                  cursor -= btnH;
                  delete = autismclient.gui.f.b.ByteReader.init(cursor, btnY, btnH, btnH, Component.empty(), b -> this.read_V_BitSetUtil(account));
                  delete.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load).init_ByteReader_Identifier(cz.open);
                  delete.init_Z = !this.init_Z_10();
                  cursor -= 2;
                  if (renameable) {
                     cursor -= btnH;
                     rename = autismclient.gui.f.b.ByteReader.init(cursor, btnY, btnH, btnH, Component.empty(), b -> this.update(account));
                     rename.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init).init_ByteReader_Identifier(cz.poll);
                     rename.init_Z = !this.init_Z_10();
                     cursor -= 2;
                  }
               }

               cursor -= loginWidth;
               autismclient.gui.f.b.ByteReader login = autismclient.gui.f.b.ByteReader.init(
                  cursor, btnY, loginWidth, btnH, Component.literal(current ? "Active" : "Login"), b -> {
                     if (displayRow.init_Z()) {
                        this.update_V_10();
                     } else {
                        this.load_V_BitSetUtil(account);
                     }
                  }
               );
               login.init_ByteReader_ByteReader_StreamU(
                  current ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.read : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write
               );
               login.load(current).init_ByteReader_Str("acct-active:" + (displayRow.init_Z() ? "default" : account.write_Str()));
               login.init_Z = !this.init_Z_10() && !current;
               cursor -= 2;
               if (load_Z_BitSetUtil(account) && !load_Str_BitSetUtil(account).isBlank()) {
                  share = autismclient.gui.f.b.ByteReader.init(cursor - btnH, btnY, btnH, btnH, Component.empty(), b -> this.write_V_BitSetUtil(account));
                  share.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init);
                  share.init_Z = !this.init_Z_10();
               }

               this.load_List.add(new StreamUtil.Inner_DataUtil(account, rowY, login, delete, share, rename, displayRow.init_Z()));
               rowY += pitch;
            }
         }
      }
   }

   private void init_V_I_2(int x, int y, int width, autismclient.util.MethodUtil provider, String label) {
      autismclient.gui.f.b.ByteReader button = autismclient.gui.f.b.ByteReader.init(x, y, width, this.set_I_3(), Component.literal(label), b -> {
         if (!this.init_Z_10()) {
            this.load_MethodUtil = provider;
            this.valid_V();
            this.push_V_3();
            this.load_V_20();
         }
      });
      button.init_Z = !this.init_Z_10();
      button.init_ByteReader_ByteReader_StreamU(
         this.load_MethodUtil == provider ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.read : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init
      );
      this.init_List.add(button);
   }

   private void load(int x, int y, int width, autismclient.util.MethodUtil provider, String label) {
      autismclient.gui.f.b.ByteReader button = autismclient.gui.f.b.ByteReader.init(
         x, y, width, this.set_I_3(), Component.literal(label), b -> this.init_V_MethodUtil(provider)
      );
      button.init_ByteReader_ByteReader_StreamU(
         this.init_MethodUtil == provider ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.sync : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.flush
      );
      this.init_List.add(button);
   }

   private void read_V_13() {
      if (!this.init_Z_10()) {
         if (this.init_BitSetUtil != null) {
            String newName = init_Str_Str(this.init_EditBox.getValue());
            if (newName.isBlank()) {
               this.init_V_Str_I("Enter a username first.", -14249);
            } else {
               boolean wasCurrent = this.read_Z_BitSetUtil(this.init_BitSetUtil);
               if (autismclient.util.TokenStore.init_TokenStore().init(this.init_BitSetUtil.write_Str(), newName)) {
                  this.init_V_Str_I("Renamed to " + newName + ". Password and proxies carried over.", ValueSink.open);
                  if (wasCurrent) {
                     autismclient.util.BitSetUtil renamed = autismclient.util.TokenStore.init_TokenStore().init_BitSetUtil(this.init_BitSetUtil.write_Str());
                     if (renamed != null) {
                        autismclient.util.TokenStore.init_TokenStore().write(renamed);
                     }
                  }

                  this.init_BitSetUtil = null;
                  this.poll_V_3();
               } else {
                  this.init_V_Str_I("An account with that name already exists.", -14249);
               }

               this.prev_V();
               this.load_V_20();
            }
         } else {
            autismclient.util.BitSetUtil account = new autismclient.util.BitSetUtil();
            account.init_MethodUtil = this.load_MethodUtil;
            if (this.load_MethodUtil == autismclient.util.MethodUtil.init) {
               account.load_Str = init_Str_Str(this.init_EditBox.getValue());
               if (account.load_Str.isBlank()) {
                  this.init_V_Str_I("Enter a username first.", -14249);
                  return;
               }

               this.init_V_BitSetUtil(account);
            } else if (this.load_MethodUtil == autismclient.util.MethodUtil.load) {
               account.read_Str = init_Str_Str(this.load_EditBox.getValue());
               if (account.read_Str.isBlank()) {
                  this.init_V_Str_I("Paste a token first.", -14249);
                  return;
               }

               this.init_V_BitSetUtil(account);
            } else if (this.load_MethodUtil == autismclient.util.MethodUtil.write) {
               account.read_Str = init_Str_Str(this.load_EditBox.getValue());
               if (account.read_Str.isBlank()) {
                  this.init_V_Str_I("Paste a token first.", -14249);
                  return;
               }

               this.init_V_BitSetUtil(account);
            } else if (this.load_MethodUtil == autismclient.util.MethodUtil.read) {
               this.write_V_11();
            }
         }
      }
   }

   private void init_V_BitSetUtil(autismclient.util.BitSetUtil account) {
      int id = this.init_I_StreamUtil_CachePo(StreamUtil.Inner_CachePool.load);
      this.init_Future = autismclient.util.ClassCache.init_Future_Str_Runnable("Autism-Account-Add", () -> {
         boolean fetched = account.init_Z();
         if (!this.init_Z_I_3(id)) {
            if (!fetched) {
               this.init_V_I(id, false, "Couldn't add account.", false);
            } else if (autismclient.util.TokenStore.init_TokenStore().init_Z_Obj(account)) {
               this.init_V_I(id, false, "Account already exists.", false);
            } else {
               autismclient.util.TokenStore.init_TokenStore().load(account);
               this.load_BitSetUtil = account;
               boolean loggedIn = account.read_Z();
               if (!this.init_Z_I_3(id)) {
                  if (!loggedIn) {
                     this.init_V_I(id, true, "Added, but login failed.", true);
                  } else {
                     this.init_V_I(id, true, "Logged in as " + account.init_Str() + ".", true);
                  }
               }
            }
         }
      });
   }

   private void write_V_11() {
      int id = this.init_I_StreamUtil_CachePo(StreamUtil.Inner_CachePool.write);
      MicrosoftAuth.init(refreshToken -> {
         if (!this.init_Z_I_3(id)) {
            if (refreshToken != null && !refreshToken.isBlank()) {
               autismclient.util.BitSetUtil account = new autismclient.util.BitSetUtil();
               account.init_MethodUtil = autismclient.util.MethodUtil.read;
               account.load_Str = refreshToken;
               this.init_Future = autismclient.util.ClassCache.init_Future_Str_Runnable("Autism-Microsoft-Add", () -> {
                  boolean fetched = account.init_Z();
                  if (!this.init_Z_I_3(id)) {
                     if (!fetched) {
                        this.init_V_I(id, false, init_Str_BitSetUtil(account), false);
                     } else if (autismclient.util.TokenStore.init_TokenStore().init_Z_Obj(account)) {
                        this.init_V_I(id, false, "Account already exists.", false);
                     } else {
                        boolean loggedIn = account.read_Z();
                        if (!this.init_Z_I_3(id)) {
                           if (!loggedIn) {
                              this.init_V_I(id, false, init_Str_BitSetUtil(account), false);
                           } else {
                              autismclient.util.TokenStore.init_TokenStore().load(account);
                              this.load_BitSetUtil = account;
                              this.init_V_I(id, true, "Logged in as " + account.init_Str() + ".", true);
                           }
                        }
                     }
                  }
               });
            } else {
               this.init_V_I(id, false, "Login cancelled.", false);
            }
         }
      });
   }

   private void load_V_BitSetUtil(autismclient.util.BitSetUtil account) {
      if (!this.init_Z_10() && account != null && !this.read_Z_BitSetUtil(account)) {
         this.load_BitSetUtil = account;
         int id = this.init_I_StreamUtil_CachePo(StreamUtil.Inner_CachePool.read);
         this.init_Future = autismclient.util.ClassCache.init_Future_Str_Runnable("Autism-Account-Login", () -> {
            boolean fetched = account.init_Z();
            if (!this.init_Z_I_3(id)) {
               if (!fetched) {
                  if (account.init_MethodUtil == autismclient.util.MethodUtil.read && init_Z_BitSetUtil(account)) {
                     this.init_V_BitSetUtil_I(account, id);
                  } else {
                     // A refresh can rotate the token even when a later step fails; keep it on disk.
                     autismclient.util.TokenStore.init_TokenStore().read_V();
                     this.init_V_I(id, false, "Token expired.", false);
                  }
               } else {
                  boolean loggedIn = account.read_Z();
                  if (!this.init_Z_I_3(id)) {
                     autismclient.util.TokenStore.init_TokenStore().read_V();
                     if (loggedIn) {
                        this.init_V_I(id, true, "Logged in as " + account.init_Str() + ".", false);
                     } else {
                        this.init_V_I(id, false, init_Str_BitSetUtil(account), false);
                     }
                  }
               }
            }
         });
      }
   }

   private static boolean init_Z_BitSetUtil(autismclient.util.BitSetUtil account) {
      String error = account == null ? null : account.load_Str();
      if (error != null && !error.isBlank()) {
         String lower = error.toLowerCase(Locale.ROOT);
         return lower.contains("invalid_grant") || lower.contains("expired");
      } else {
         return false;
      }
   }

   private void init_V_BitSetUtil_I(autismclient.util.BitSetUtil account, int id) {
      this.minecraft.execute(() -> {
         if (!this.init_Z_I_3(id)) {
            MicrosoftAuth.init(refreshToken -> {
               if (!this.init_Z_I_3(id)) {
                  if (refreshToken != null && !refreshToken.isBlank()) {
                     account.load_Str = refreshToken;
                     this.init_Future = autismclient.util.ClassCache.init_Future_Str_Runnable("Autism-Microsoft-Relogin", () -> {
                        boolean fetched = account.init_Z();
                        if (!this.init_Z_I_3(id)) {
                           if (!fetched) {
                              autismclient.util.TokenStore.init_TokenStore().read_V();
                              this.init_V_I(id, false, init_Str_BitSetUtil(account), false);
                           } else {
                              boolean loggedIn = account.read_Z();
                              if (!this.init_Z_I_3(id)) {
                                 autismclient.util.TokenStore.init_TokenStore().read_V();
                                 if (loggedIn) {
                                    this.init_V_I(id, true, "Logged in as " + account.init_Str() + ".", false);
                                 } else {
                                    this.init_V_I(id, false, init_Str_BitSetUtil(account), false);
                                 }
                              }
                           }
                        }
                     });
                  } else {
                     this.init_V_I(id, false, "Token expired.", false);
                  }
               }
            });
         }
      });
   }

   private static String init_Str_BitSetUtil(autismclient.util.BitSetUtil account) {
      String reason = account == null ? "" : account.load_Str();
      return reason != null && !reason.isBlank() ? "Login failed: " + reason : "Login failed.";
   }

   private void update_V_10() {
      if (!this.init_Z_10() && !this.write_Z_2()) {
         autismclient.util.BitSetUtil account = this.load_BitSetUtil();
         if (account != null) {
            this.load_BitSetUtil = account;
            int id = this.init_I_StreamUtil_CachePo(StreamUtil.Inner_CachePool.read);
            this.init_Future = autismclient.util.ClassCache.init_Future_Str_Runnable("Autism-Default-Account-Login", () -> {
               boolean loggedIn = autismclient.util.FieldCache.init(autismclient.util.FieldCache.init_User());
               if (!this.init_Z_I_3(id)) {
                  if (loggedIn) {
                     this.init_V_I(id, true, "Logged in as " + account.init_Str() + ".", false);
                  } else {
                     this.init_V_I(id, false, "Login failed.", false);
                  }
               }
            });
         }
      }
   }

   private void read_V_BitSetUtil(autismclient.util.BitSetUtil account) {
      if (!this.init_Z_10() && account != null) {
         autismclient.util.TokenStore.init_TokenStore().read(account);
         this.prev_V();
         if (account.equals(this.load_BitSetUtil)) {
            this.load_BitSetUtil = null;
         }

         this.init_V_Str_I("Deleted " + account.init_Str() + ".", ValueSink.close);
         this.load_V_20();
      }
   }

   private void sync_V_8() {
      if (!this.init_Z_10()) {
         List<autismclient.util.BitSetUtil> accounts = new ArrayList<>();

         for (autismclient.util.BitSetUtil account : this.read_List_2()) {
            if (account.init_MethodUtil == autismclient.util.MethodUtil.init) {
               account.init_BitSetUtil_DataUti = autismclient.util.BitSetUtil.Inner_DataUtil.init;
            } else {
               accounts.add(account);
            }
         }

         if (accounts.isEmpty()) {
            this.init_V_Str_I("No accounts to check.", ValueSink.close);
         } else {
            for (autismclient.util.BitSetUtil account : accounts) {
               if (account.init_BitSetUtil_DataUti == autismclient.util.BitSetUtil.Inner_DataUtil.init) {
                  account.init_BitSetUtil_DataUti = autismclient.util.BitSetUtil.Inner_DataUtil.load;
               }
            }

            int id = this.init_I_StreamUtil_CachePo(StreamUtil.Inner_CachePool.update);
            this.init_Future = autismclient.util.ClassCache.init_Future_Str_Runnable(
               "Autism-Account-Check-Coordinator",
               () -> {
                  ExecutorService pool = Executors.newFixedThreadPool(Math.min(6, Math.max(1, accounts.size())), runnable -> {
                     Thread worker = new Thread(runnable, "Autism-Account-Check");
                     worker.setDaemon(true);
                     return worker;
                  });
                  AtomicInteger valid = new AtomicInteger();
                  AtomicInteger expired = new AtomicInteger();

                  try {
                     List<Future<?>> futures = new ArrayList<>();

                     for (autismclient.util.BitSetUtil accountx : accounts) {
                        futures.add(
                           pool.submit(
                              () -> {
                                 if (!this.init_Z_I_3(id)) {
                                    boolean ok;
                                    try {
                                       ok = accountx.init_Z();
                                    } catch (Throwable t) {
                                       ok = false;
                                    }

                                    if (!this.init_Z_I_3(id)) {
                                       accountx.init_BitSetUtil_DataUti = ok
                                          ? autismclient.util.BitSetUtil.Inner_DataUtil.read
                                          : autismclient.util.BitSetUtil.Inner_DataUtil.write;
                                       (ok ? valid : expired).incrementAndGet();
                                    }
                                 }
                              }
                           )
                        );
                     }

                     for (Future<?> future : futures) {
                        try {
                           future.get();
                        } catch (Exception var13) {
                        }
                     }
                  } finally {
                     pool.shutdownNow();
                  }

                  if (!this.init_Z_I_3(id)) {
                     autismclient.util.TokenStore.init_TokenStore().read_V();
                     this.init_V_I(id, true, valid.get() + " valid, " + expired.get() + " expired.", false);
                  }
               }
            );
         }
      }
   }

   private int read_I_13() {
      int count = 0;

      for (autismclient.util.BitSetUtil account : this.read_List_2()) {
         if (account.init_BitSetUtil_DataUti == autismclient.util.BitSetUtil.Inner_DataUtil.write) {
            count++;
         }
      }

      return count;
   }

   private void flush_V_7() {
      if (!this.init_Z_10()) {
         if (this.load_BitSetUtil != null && this.load_BitSetUtil.init_BitSetUtil_DataUti == autismclient.util.BitSetUtil.Inner_DataUtil.write) {
            this.load_BitSetUtil = null;
         }

         int removed = autismclient.util.TokenStore.init_TokenStore().init_I();
         if (removed == 0) {
            this.init_V_Str_I("No expired accounts.", ValueSink.close);
         } else {
            this.prev_V();
            this.init_V_Str_I("Cleared " + removed + " expired.", ValueSink.close);
            this.load_V_20();
         }
      }
   }

   private static int init_I_BitSetUtil(autismclient.util.BitSetUtil account, boolean active, boolean defaultAccount) {
      if (active) {
         return ValueSink.open;
      }

      if (defaultAccount) {
         return -7429889;
      }

      if (account != null) {
         if (account.init_BitSetUtil_DataUti == autismclient.util.BitSetUtil.Inner_DataUtil.write) {
            return ValueSink.poll;
         }

         if (account.init_BitSetUtil_DataUti == autismclient.util.BitSetUtil.Inner_DataUtil.load) {
            return -14249;
         }
      }

      return ValueSink.flush;
   }

   private static String init_Str_BitSetUtil_DataUti(autismclient.util.BitSetUtil.Inner_DataUtil status) {
      if (status == null) {
         return "";
      }

      String var10000;
      switch (status) {
         case load:
            var10000 = "CHECK…";
            break;
         case read:
            var10000 = "VALID";
            break;
         case write:
            var10000 = "EXPIRED";
            break;
         case init:
            var10000 = "";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int init_I_BitSetUtil_DataUti(autismclient.util.BitSetUtil.Inner_DataUtil status) {
      if (status == null) {
         return ValueSink.close;
      }

      int var10000;
      switch (status) {
         case load:
            var10000 = -14249;
            break;
         case read:
            var10000 = ValueSink.open;
            break;
         case write:
            var10000 = ValueSink.poll;
            break;
         case init:
            var10000 = ValueSink.close;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean load_Z_BitSetUtil(autismclient.util.BitSetUtil account) {
      return account != null
         && (
            account.init_MethodUtil == autismclient.util.MethodUtil.read
               || account.init_MethodUtil == autismclient.util.MethodUtil.load
               || account.init_MethodUtil == autismclient.util.MethodUtil.write
         );
   }

   private static String load_Str_BitSetUtil(autismclient.util.BitSetUtil account) {
      if (!load_Z_BitSetUtil(account)) {
         return "";
      } else {
         return account.init_MethodUtil == autismclient.util.MethodUtil.write ? init_Str_Str(account.write) : init_Str_Str(account.read_Str);
      }
   }

   private void write_V_BitSetUtil(autismclient.util.BitSetUtil account) {
      if (load_Z_BitSetUtil(account)) {
         String sessionToken = load_Str_BitSetUtil(account);
         if (sessionToken.isBlank()) {
            this.init_V_Str_I("No session token.", -14249);
         } else if (this.minecraft != null && this.minecraft.keyboardHandler != null) {
            try {
               this.minecraft.keyboardHandler.setClipboard(sessionToken);
            } catch (Exception e) {
               this.init_V_Str_I("Failed to copy token.", ValueSink.poll);
               return;
            }

            this.init_V_Str_I("Copied session token.", ValueSink.open);
         } else {
            this.init_V_Str_I("Clipboard unavailable.", ValueSink.poll);
         }
      }
   }

   private int init_I_StreamUtil_CachePo(StreamUtil.Inner_CachePool next) {
      this.push++;
      this.init_StreamUtil_CachePo = next;
      this.load_J = System.currentTimeMillis();
      this.valid_V();
      this.push_V_3();
      this.load_V_20();
      return this.push;
   }

   private void init_V_I(int id, boolean success, String message, boolean clearOnSuccess) {
      this.minecraft.execute(() -> {
         if (id == this.push) {
            this.init_StreamUtil_CachePo = StreamUtil.Inner_CachePool.init;
            this.load_J = 0L;
            this.init_Future = null;
            this.init_V_Str_I(message, success ? ValueSink.open : ValueSink.poll);
            if (success && clearOnSuccess) {
               this.poll_V_3();
            }

            this.push_V_3();
            this.valid_V();
            this.prev_V();
            this.load_V_20();
         }
      });
   }

   private void close_V_5() {
      if (this.init_Z_10()) {
         this.push++;
         Future<?> task = this.init_Future;
         if (task != null) {
            task.cancel(true);
         }

         if (this.init_StreamUtil_CachePo == StreamUtil.Inner_CachePool.write) {
            MicrosoftAuth.init_V();
         }

         if (this.init_StreamUtil_CachePo == StreamUtil.Inner_CachePool.update) {
            for (autismclient.util.BitSetUtil account : this.read_List_2()) {
               if (account.init_BitSetUtil_DataUti == autismclient.util.BitSetUtil.Inner_DataUtil.load) {
                  account.init_BitSetUtil_DataUti = autismclient.util.BitSetUtil.Inner_DataUtil.init;
               }
            }
         }

         this.init_StreamUtil_CachePo = StreamUtil.Inner_CachePool.init;
         this.load_J = 0L;
         this.init_Future = null;
         this.valid_V();
         this.init_V_Str_I("Cancelled.", -14249);
         this.push_V_3();
         this.load_V_20();
      }
   }

   private boolean init_Z_I_3(int id) {
      return id != this.push || Thread.currentThread().isInterrupted();
   }

   private boolean init_Z_10() {
      return this.init_StreamUtil_CachePo != StreamUtil.Inner_CachePool.init;
   }

   private boolean load_Z_4() {
      return this.init_Z_10() && this.load_J > 0L && System.currentTimeMillis() - this.load_J >= 5000L;
   }

   private void open_V_4() {
      if (this.init_ByteReader != null) {
         this.init_ByteReader.load_Z = this.load_Z_4();
      }
   }

   private void poll_V_3() {
      this.init_BitSetUtil = null;
      if (this.init_EditBox != null) {
         this.init_EditBox.setValue("");
      }

      if (this.load_EditBox != null) {
         this.load_EditBox.setValue("");
      }
   }

   private void update(autismclient.util.BitSetUtil account) {
      if (account != null && account.init_MethodUtil == autismclient.util.MethodUtil.init && !this.init_Z_10()) {
         this.init_BitSetUtil = account;
         this.load_MethodUtil = autismclient.util.MethodUtil.init;
         this.valid_V();
         this.push_V_3();
         this.load_V_20();
         if (this.init_EditBox != null) {
            this.init_EditBox.setValue(account.init_Str());
            this.init_EditBox.setFocused(true);
            this.setFocused(this.init_EditBox);
            this.init_EditBox.moveCursorToEnd(false);
         }
      }
   }

   private void push_V_3() {
      if (this.init_EditBox != null && this.load_EditBox != null) {
         boolean showForm = !this.sync_Z();
         boolean labelVisible = showForm && this.load_MethodUtil == autismclient.util.MethodUtil.init;
         boolean tokenVisible = showForm
            && (this.load_MethodUtil == autismclient.util.MethodUtil.load || this.load_MethodUtil == autismclient.util.MethodUtil.write);
         this.init_EditBox.setVisible(labelVisible);
         this.load_EditBox.setVisible(tokenVisible);
         this.init_EditBox.active = labelVisible && !this.init_Z_10();
         this.load_EditBox.active = tokenVisible && !this.init_Z_10();
         if (!labelVisible || this.init_Z_10()) {
            this.init_EditBox.setFocused(false);
         }

         if (!tokenVisible || this.init_Z_10()) {
            this.load_EditBox.setFocused(false);
         }

         if (this.read_EditBox != null) {
            this.read_EditBox.visible = showForm;
            this.read_EditBox.active = showForm;
         }

         this.init_EditBox.setHint(Component.literal("Username"));
         this.load_EditBox
            .setHint(Component.literal(this.load_MethodUtil == autismclient.util.MethodUtil.write ? "TheAltening token" : "Minecraft access token"));
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      this.open_V_4();
      if (this.tick != this.block()) {
         this.load_V_20();
      }

      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -15987181);
         int panelX = this.reset_I();
         int previewX = this.node();
         if (!this.sync_Z()) {
            this.init_V_GuiGraphicsExtract_2(graphics, panelX, this.stop_I(), 262, this.queue(), -401402080);
            this.init_V_GuiGraphicsExtract_2(graphics, previewX, this.stop_I(), 148, this.queue(), -1206840035);
         }

         int listHeight = this.task();
         this.init_V_GuiGraphicsExtract_2(graphics, this.buffer(), this.index(), this.entry(), listHeight, -401402080);
         int ctl = this.set_I_3();
         if (!this.sync_Z()) {
            this.init_V_GuiGraphicsExtract_4(
               graphics, "Accounts", this.next_I() + this.load_I_Str_3("Back") + 4, this.init_I_I_I_8(this.valid_I(), ctl), ValueSink.flush, false
            );
            this.init_V_GuiGraphicsExtract(graphics, previewX, virtualMouseX, virtualMouseY, delta);
         }

         List<StreamUtil.Inner_StreamUtil> displayAccounts = this.load_List_5();
         if (this.flush_Z()) {
            this.init_V_GuiGraphicsExtract_2(
               graphics, "Window too small.", this.buffer() + 4, this.init_I_I_I_8(this.level(), 12), ValueSink.close, false, Math.max(0, this.entry() - 8)
            );
         }

         int firstVisibleRow = this.pop / this.add_I_2();
         String listTitle = displayAccounts.size() <= this.chunk()
            ? "Accounts"
            : "Accounts  showing "
               + (firstVisibleRow + 1)
               + "-"
               + Math.min(displayAccounts.size(), firstVisibleRow + this.chunk())
               + " / "
               + displayAccounts.size();
         int titleX = this.buffer() + 4 + (this.sync_Z() && !this.update_Z_2() ? this.load_I_Str_3("Back") + 4 : 0);
         int titleRight = this.flush_Z() ? this.buffer() + this.entry() - 4 : this.a() - 4;
         k.load(
            graphics,
            this.font,
            listTitle,
            init_CharEncoder.init_Identifier_l(l.read),
            ValueSink.flush,
            titleX,
            this.init_I_I_I_8(this.count(), ctl),
            Math.max(20, titleRight - titleX),
            false
         );
         if (!this.flush_Z()) {
            for (StreamUtil.Inner_DataUtil row : this.load_List) {
               this.init_V_GuiGraphicsExtract_3(graphics, row, virtualMouseX, virtualMouseY);
            }
         }

         int emptyY = this.init_I_I_I_8(this.level() + this.add_I_2(), this.put_I_2());
         if (this.read_List_2().isEmpty()) {
            this.init_V_GuiGraphicsExtract_2(graphics, "No extra accounts saved yet.", this.drain() + 4, emptyY, ValueSink.close, false, this.mark() - 4);
         } else if (this.init_List_10().isEmpty()) {
            this.init_V_GuiGraphicsExtract_2(
               graphics, "No accounts match the current search or filters.", this.drain() + 4, emptyY, ValueSink.close, false, this.mark() - 4
            );
         }

         for (autismclient.gui.f.b.ByteReader button : this.init_List) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, button, virtualMouseX, virtualMouseY);
         }

         if (!this.flush_Z()) {
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_I_4(displayAccounts.size());
            autismclient.gui.f.b.PoolEntry.init(graphics, scrollbar, scrollbar.init(virtualMouseX, virtualMouseY), this.load_Z);
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
         if (this.clear != 0) {
            this.init_V_GuiGraphicsExtract_2(graphics, virtualMouseX, virtualMouseY, delta);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public void removed() {
      super.removed();
   }

   private void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor graphics, StreamUtil.Inner_DataUtil row, int mouseX, int mouseY) {
      autismclient.util.BitSetUtil account = row.init_BitSetUtil;
      boolean active = row.init_Z ? this.write_Z_2() : this.read_Z_BitSetUtil(account);
      boolean selected = account.equals(this.load_BitSetUtil);
      int x = this.drain();
      int y = row.init_I;
      int w = this.mark();
      int h = this.put_I_2();
      int fill = active ? write_I_I(858052714) : (row.init_Z ? 808137323 : (selected ? read_I_I_2(606804509) : 403771667));
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), fill);
      if (active) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 2, h), write_I_I(ValueSink.sync));
      } else if (selected) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 2, h), read_I_I_2(-10866632));
      }

      PlayerSkin rowSkin = this.init_StreamUtil_BufferH_BitSetUtil(account).load();
      this.init_V_GuiGraphicsExtract_2(graphics, rowSkin, x + 4, y + (h - 16) / 2, 16);
      String name = account.init_Str().isBlank() ? "(unknown)" : account.init_Str();
      String meta = row.init_Z ? "Default Minecraft" : account.init_MethodUtil.name();
      int nameX = x + 4 + 16 + 4 + 2;
      int ty = this.init_I_I_I_8(y, h);
      if (this.sync_Z()) {
         int leftmost = row.read == null ? row.init_ByteReader.init_I() : row.read.init_I();
         int textRight = Math.max(nameX + 1, leftmost - 4);
         this.init_V_GuiGraphicsExtract_2(graphics, name, nameX, ty, init_I_BitSetUtil(account, active, row.init_Z), false, Math.max(1, textRight - nameX));
         autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.init_ByteReader, mouseX, mouseY);
         if (row.write != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.write, mouseX, mouseY);
         }

         if (row.load != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.load, mouseX, mouseY);
         }

         this.init_V_GuiGraphicsExtract_4(graphics, row.read, mouseX, mouseY);
      } else {
         int btnH = this.clear_I_2();
         int groupW = btnH * 3 + this.scan() + 6;
         int badgeW = Math.max(this.init_I_Str_16("CURRENT"), this.init_I_Str_16("EXPIRED"));
         int badgeX = this.fill() - 4 - groupW - 4 - badgeW;
         int metaW = Math.min(this.init_I_Str_16("Default Minecraft"), Math.max(20, (badgeX - nameX) / 3));
         int metaX = badgeX - 8 - metaW;
         int nameMaxWidth = Math.max(20, metaX - 4 - nameX);
         this.init_V_GuiGraphicsExtract_2(graphics, name, nameX, ty, init_I_BitSetUtil(account, active, row.init_Z), false, nameMaxWidth);
         this.init_V_GuiGraphicsExtract_2(graphics, meta, metaX, ty, row.init_Z ? -7429889 : ValueSink.close, false, metaW);
         if (active) {
            this.init_V_GuiGraphicsExtract_2(graphics, "CURRENT", badgeX, ty, ValueSink.open, false, badgeW);
         } else if (!row.init_Z) {
            String statusLabel = init_Str_BitSetUtil_DataUti(account.init_BitSetUtil_DataUti);
            if (!statusLabel.isEmpty()) {
               this.init_V_GuiGraphicsExtract_2(graphics, statusLabel, badgeX, ty, init_I_BitSetUtil_DataUti(account.init_BitSetUtil_DataUti), false, badgeW);
            }
         }

         autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.init_ByteReader, mouseX, mouseY);
         if (row.write != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.write, mouseX, mouseY);
         }

         if (row.load != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.load, mouseX, mouseY);
         }

         this.init_V_GuiGraphicsExtract_4(graphics, row.read, mouseX, mouseY);
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor graphics, autismclient.gui.f.b.ByteReader button, int mouseX, int mouseY) {
      if (button != null) {
         autismclient.gui.f.b.ByteReader.init(graphics, this.font, button, mouseX, mouseY);
         autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(button.init_I(), button.load(), button.read(), button.write());
         this.init_V_GuiGraphicsExtract_4(graphics, bounds, button.init_Z ? -855310 : -9016466);
      }
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor graphics, autismclient.gui.f.NetHelper button, int color) {
      int pageW = 7;
      int pageH = 7;
      int offset = 3;
      int ox = button.read() + (button.update() - (pageW + offset)) / 2;
      int oy = button.write() + (button.sync() - (pageH + offset)) / 2;
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(ox + offset, oy, pageW, pageH), color);
      autismclient.gui.f.NetHelper front = autismclient.gui.f.NetHelper.init(ox, oy + offset, pageW, pageH);
      autismclient.gui.f.PacketUtil.init(graphics, front, -15395557);
      autismclient.gui.f.PacketUtil.load(graphics, front, color);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int x, int mouseX, int mouseY, float delta) {
      autismclient.util.BitSetUtil account = this.init_BitSetUtil();
      int innerX = x + 4;
      int innerW = 140;
      int line = 12;
      int headerTextY = this.init_I_I_I_8(this.valid_I(), this.set_I_3());
      int modelTop = this.valid_I() + this.set_I_3() + 2;
      int bottom = this.stop_I() + this.queue() - 4;
      this.init_V_GuiGraphicsExtract_4(graphics, "Skin preview", innerX, headerTextY, ValueSink.flush, false);
      if (account == null) {
         this.init_V_GuiGraphicsExtract_2(graphics, "No account selected", innerX, this.init_I_I_I_8(modelTop + 4, line), ValueSink.close, false, innerW);
         this.init_V_GuiGraphicsExtract_2(
            graphics, "Click a row to preview it.", innerX, this.init_I_I_I_8(modelTop + 4 + line, line), ValueSink.close, false, innerW
         );
      } else {
         StreamUtil.Inner_BufferHelper lookup = this.init_StreamUtil_BufferH_BitSetUtil(account);
         PlayerSkin skin = lookup.load();
         int modelH = Math.max(1, bottom - modelTop);
         int modelW = Math.min(innerW / 2, Math.round(modelH * 0.55F));
         int modelX = innerX + innerW - modelW;
         this.init_V_GuiGraphicsExtract_5(graphics, skin, modelX, modelTop, modelX + modelW, bottom);
         int textW = Math.max(1, modelX - 2 - innerX);
         String display = account.init_Str().isBlank() ? "(unknown)" : account.init_Str();
         boolean defaultAccount = this.write_Z_BitSetUtil(account);
         boolean active = defaultAccount ? this.write_Z_2() : this.read_Z_BitSetUtil(account);
         this.init_V_GuiGraphicsExtract_2(
            graphics,
            defaultAccount ? "Default Minecraft" : account.init_MethodUtil.name(),
            innerX,
            this.init_I_I_I_8(modelTop, line),
            defaultAccount ? -7429889 : ValueSink.close,
            false,
            textW
         );
         this.init_V_GuiGraphicsExtract_2(
            graphics, display, innerX, this.init_I_I_I_8(modelTop + line, line), active ? ValueSink.open : ValueSink.flush, false, textW
         );
         if (lookup.init_Z()) {
            this.init_V_GuiGraphicsExtract_2(graphics, "Loading skin...", innerX, this.init_I_I_I_8(modelTop + line * 2, line), -14249, false, textW);
         }

         if (active) {
            int currentW = this.init_I_Str_16("CURRENT");
            this.init_V_GuiGraphicsExtract_2(graphics, "CURRENT", innerX + innerW - currentW, headerTextY, ValueSink.open, false, currentW + 2);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor graphics, PlayerSkin skin, int x0, int y0, int x1, int y1) {
      if (this.init_Model_Simple != null && this.load_Model_Simple != null) {
         Simple model = skin.model().name().equalsIgnoreCase("slim") ? this.load_Model_Simple : this.init_Model_Simple;
         float drawScale = cA.init_F();
         int scaledX0 = Math.round(x0 * drawScale);
         int scaledY0 = Math.round(y0 * drawScale);
         int scaledX1 = Math.round(x1 * drawScale);
         int scaledY1 = Math.round(y1 * drawScale);
         float scale = 0.97F * Math.max(1, scaledY1 - scaledY0) / 2.125F;
         graphics.skin(model, skin.body().texturePath(), scale, this.init_F, this.init_F(), -1.0625F, scaledX0, scaledY0, scaledX1, scaledY1);
      }
   }

   private float init_F() {
      return this.read_Z ? this.load_F : this.load_F + (float)((Blaze3D.getTime() - this.init_D) * 18.0);
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, PlayerSkin skin, int x, int y, int size) {
      Identifier texture = skin.body().texturePath();
      graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 8.0F, 8.0F, size, size, 8, 8, 64, 64);
      graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 40.0F, 8.0F, size, size, 8, 8, 64, 64);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) != 0) {
         return super.mouseClicked(virtualEvent, doubleClick);
      }

      if (this.clear != 0) {
         this.init_V_MouseButtonEvent_Z(virtualEvent, doubleClick);
         return true;
      }

      if (this.flush_Z()) {
         return super.mouseClicked(virtualEvent, doubleClick);
      }

      autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_I_4(this.load_List_5().size());
      if (scrollbar.init_Z() && scrollbar.init(virtualEvent.x(), virtualEvent.y())) {
         this.load_Z = true;
         this.run = scrollbar.load(virtualEvent.x(), virtualEvent.y())
            ? Math.max(0, (int)Math.round(virtualEvent.y()) - scrollbar.update())
            : scrollbar.sync() / 2;
         this.pop = init_I_I_3(autismclient.gui.f.b.PoolEntry.init(scrollbar, virtualEvent.y(), this.run), this.add_I_2(), scrollbar.flush());
         this.init_CacheEntry.load(this.pop, scrollbar.flush());
         this.load_V_20();
         this.valid_V();
         return true;
      }

      if (this.init_Z_D_D(virtualEvent.x(), virtualEvent.y())) {
         this.load_F = this.init_F();
         this.init_D = Blaze3D.getTime();
         this.read_Z = true;
         this.load_D = virtualEvent.x();
         this.read_D = virtualEvent.y();
         this.valid_V();
         return true;
      }

      for (autismclient.gui.f.b.ByteReader button : this.init_List) {
         if (autismclient.gui.f.b.ByteReader.init(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }
      }

      for (StreamUtil.Inner_DataUtil row : this.load_List) {
         if (row.write != null
            && autismclient.gui.f.b.ByteReader.init(row.write, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }

         if (autismclient.gui.f.b.ByteReader.init(row.load, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }

         if (autismclient.gui.f.b.ByteReader.init(row.read, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }

         if (autismclient.gui.f.b.ByteReader.init(row.init_ByteReader, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
            return true;
         }

         if (virtualEvent.x() >= this.drain()
            && virtualEvent.x() < this.fill()
            && virtualEvent.y() >= row.init_I
            && virtualEvent.y() < row.init_I + this.put_I_2()) {
            this.load_BitSetUtil = row.init_BitSetUtil();
            return true;
         }
      }

      this.valid_V();
      return super.mouseClicked(virtualEvent, doubleClick);
   }

   private void init_V_MethodUtil(autismclient.util.MethodUtil filterType) {
      this.init_MethodUtil = this.init_MethodUtil == filterType ? null : filterType;
      this.pop = 0;
      this.init_CacheEntry.load(0, 0);
      this.load_V_20();
   }

   private void pop_V_3() {
      this.clear = 1;
      this.update_Str = "";
      this.valid_V();
      this.put_V_2();
   }

   private void run_V_3() {
      this.clear = 2;
      this.valid_V();
      this.put_V_2();
   }

   private void get_V_3() {
      this.set_V_3();
      this.clear = 0;
      this.update_Str = "";
      this.close_List.clear();
      if (this.write_EditBox != null) {
         this.write_EditBox.setFocused(false);
      }

      if (this.update_EditBox != null) {
         this.update_EditBox.setFocused(false);
      }

      if (this.init_MultiLineEditBox != null) {
         this.init_MultiLineEditBox.setFocused(false);
      }
   }

   private void set_V_3() {
      if (this.update_EditBox != null) {
         AutismConfig config = AutismConfig.getGlobal();
         String value = this.update_EditBox.getValue();
         if (!Objects.equals(config.accountGenSharedPassword, value)) {
            config.accountGenSharedPassword = value;
            config.save();
         }
      }
   }

   private int write_I_11() {
      return Math.max(6, (this.init_I() - this.sync_I_8()) / 2);
   }

   private int update_I_10() {
      return Math.max(6, (this.load_I() - this.tick_I_13()) / 2);
   }

   private int sync_I_8() {
      return Math.max(120, Math.min(240, this.init_I() - 12));
   }

   private int flush_I_7() {
      return this.sync_I_8() - 8;
   }

   private int close_I_5() {
      return 18;
   }

   private int open_I_4() {
      return this.close_I_5() + this.set_I_3() + 4;
   }

   private int init_I_I_13(int row) {
      return this.open_I_4() + row * (this.set_I_3() + 2);
   }

   private int poll_I_3() {
      return this.set_I_3() * 4 + 6;
   }

   private int push_I_3() {
      return this.open_I_4() + this.poll_I_3() + 2;
   }

   private int pop_I_3() {
      return this.clear == 2 ? 58 : this.push_I_3() + 12 + 2;
   }

   private int tick_I_13() {
      return this.pop_I_3() + this.set_I_3() + 4;
   }

   private void put_V_2() {
      this.flush_List.clear();
      this.close_List.clear();
      int px = this.write_I_11();
      int py = this.update_I_10();
      int ctl = this.set_I_3();
      int left = px + 4;
      int right = px + this.sync_I_8() - 4;
      int actionY = py + this.pop_I_3();
      if (this.clear == 1) {
         int modeWidth = Math.max(this.load_I_Str_3("Random"), this.load_I_Str_3("From List"));
         int modeY = py + this.close_I_5();
         autismclient.gui.f.b.ByteReader random = autismclient.gui.f.b.ByteReader.init(left, modeY, modeWidth, ctl, Component.literal("Random"), b -> {
            this.reset = 0;
            this.update_Str = "";
            this.put_V_2();
         });
         random.init_ByteReader_ByteReader_StreamU(
            this.reset == 0 ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init
         );
         this.flush_List.add(random);
         autismclient.gui.f.b.ByteReader fromList = autismclient.gui.f.b.ByteReader.init(
            left + modeWidth + 2, modeY, modeWidth, ctl, Component.literal("From List"), b -> {
               this.reset = 1;
               this.update_Str = "";
               this.put_V_2();
            }
         );
         fromList.init_ByteReader_ByteReader_StreamU(
            this.reset == 1 ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init
         );
         this.flush_List.add(fromList);
         if (this.reset == 0) {
            AutismConfig config = AutismConfig.getGlobal();
            this.flush_List
               .add(autismclient.gui.f.b.ByteReader.init(left, py + this.init_I_I_13(1), this.flush_I_7(), ctl, Component.literal("Set Password"), b -> {
                  AutismConfig cfg = AutismConfig.getGlobal();
                  cfg.accountGenSetPassword = !cfg.accountGenSetPassword;
                  cfg.save();
                  this.put_V_2();
               }).init_ByteReader_Z(config.accountGenSetPassword));
            if (config.accountGenSetPassword) {
               boolean setForAll = "Set For All".equalsIgnoreCase(config.accountGenPasswordMode);
               List<String> passwordModes = List.of("Generate", "Set For All");
               this.close_List
                  .add(
                     new autismclient.gui.f.b.SyncManager(
                        left, py + this.init_I_I_13(2), this.init_I_List_4(passwordModes), ctl, passwordModes, setForAll ? 1 : 0, index -> {
                           AutismConfig cfg = AutismConfig.getGlobal();
                           cfg.accountGenPasswordMode = index == 1 ? "Set For All" : "Generate";
                           cfg.save();
                           this.put_V_2();
                        }
                     )
                  );
            }

            autismclient.gui.f.b.ByteReader generate = autismclient.gui.f.b.ByteReader.init(
                  left,
                  actionY,
                  Math.max(this.load_I_Str_3("Working..."), this.load_I_Str_3("Generate")),
                  ctl,
                  Component.literal(this.update_Z ? "Working..." : "Generate"),
                  b -> this.add_V_2()
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write);
            generate.init_Z = !this.update_Z;
            this.flush_List.add(generate);
         } else {
            int pasteWidth = this.load_I_Str_3("Paste");
            this.flush_List
               .add(
                  autismclient.gui.f.b.ByteReader.init(left, actionY, pasteWidth, ctl, Component.literal("Paste"), b -> this.start_V())
                     .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init)
               );
            autismclient.gui.f.b.ByteReader addAccounts = autismclient.gui.f.b.ByteReader.init(
                  left + pasteWidth + 2,
                  actionY,
                  Math.max(this.load_I_Str_3("Working..."), this.load_I_Str_3("Add Accounts")),
                  ctl,
                  Component.literal(this.update_Z ? "Working..." : "Add Accounts"),
                  b -> this.clear_V_2()
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write);
            addAccounts.init_Z = !this.update_Z;
            this.flush_List.add(addAccounts);
         }

         int closeWidth = this.load_I_Str_3("Close");
         this.flush_List
            .add(
               autismclient.gui.f.b.ByteReader.init(right - closeWidth, actionY, closeWidth, ctl, Component.literal("Close"), b -> this.get_V_3())
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init)
            );
      } else if (this.clear == 2) {
         autismclient.gui.f.b.ByteReader clear = autismclient.gui.f.b.ByteReader.init(
               left, actionY, this.load_I_Str_3("Clear"), ctl, Component.literal("Clear"), b -> this.stop_V()
            )
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load);
         clear.init_Z = this.run_I_3() > 0;
         this.flush_List.add(clear);
         int cancelWidth = this.load_I_Str_3("Cancel");
         this.flush_List
            .add(
               autismclient.gui.f.b.ByteReader.init(right - cancelWidth, actionY, cancelWidth, ctl, Component.literal("Cancel"), b -> this.get_V_3())
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init)
            );
      }
   }

   private void add_V_2() {
      if (!this.update_Z) {
         this.set_V_3();
         int count = 10;

         try {
            count = Integer.parseInt(init_Str_Str(this.write_EditBox.getValue()));
         } catch (NumberFormatException var3) {
         }

         int wanted = count;
         this.reset_V();
         this.init_V_Supplier(
            () -> {
               List<String> names = autismclient.util.CharEncoder.init(wanted);
               int added = autismclient.util.CharEncoder.init(names);
               return names.isEmpty()
                  ? "Nothing to add."
                  : (added >= names.size() ? added + " accounts added." : added + " added, " + (names.size() - added) + " already existed.");
            }
         );
      }
   }

   private void clear_V_2() {
      if (!this.update_Z) {
         this.set_V_3();
         String raw = this.init_MultiLineEditBox == null ? "" : this.init_MultiLineEditBox.getValue();
         if (raw != null && !raw.isBlank()) {
            this.reset_V();
            this.init_V_Supplier(() -> {
               autismclient.util.CharEncoder.Inner_DataUtil result = autismclient.util.CharEncoder.init(raw);
               int added = autismclient.util.CharEncoder.init(result.init_List());
               StringBuilder message = new StringBuilder("Added ").append(added).append(" accounts");
               int existed = result.init_List().size() - added;
               if (existed > 0) {
                  message.append(", ").append(existed).append(" existed");
               }

               if (result.init_I() > 0) {
                  message.append(", ").append(result.init_I()).append(" invalid");
               }

               if (result.load() > 0) {
                  message.append(", ").append(result.load()).append(" dupes");
               }

               return message.toString();
            });
         } else {
            this.update_Str = "Paste a list first.";
         }
      }
   }

   private void reset_V() {
      this.update_Z = true;
      this.update_Str = "Generating...";
      this.put_V_2();
   }

   private void init_V_Supplier(Supplier<String> work) {
      Thread thread = new Thread(() -> {
         String result;
         try {
            result = work.get();
         } catch (Throwable t) {
            result = "Generation failed.";
         }

         String message = result;
         Runnable done = () -> {
            this.update_Z = false;
            this.update_Str = message;
            this.prev_V();
            this.load_V_20();
            this.put_V_2();
         };
         if (this.minecraft != null) {
            this.minecraft.execute(done);
         } else {
            done.run();
         }
      }, "autism-account-gen");
      thread.setDaemon(true);
      thread.start();
   }

   private void start_V() {
      if (this.minecraft != null && this.init_MultiLineEditBox != null) {
         this.init_MultiLineEditBox.setValue(this.minecraft.keyboardHandler.getClipboard());
      }
   }

   private void stop_V() {
      int removed = 0;

      for (autismclient.util.BitSetUtil account : new ArrayList<>(autismclient.util.TokenStore.init_TokenStore().init_List())) {
         if (this.init_MethodUtil == null || account.init_MethodUtil == this.init_MethodUtil) {
            autismclient.util.TokenStore.init_TokenStore().read(account);
            removed++;
         }
      }

      this.get_V_3();
      this.init_V_Str_I("Cleared " + removed + " accounts.", ValueSink.close);
      this.prev_V();
      this.load_V_20();
   }

   private int run_I_3() {
      int count = 0;

      for (autismclient.util.BitSetUtil account : autismclient.util.TokenStore.init_TokenStore().init_List()) {
         if (this.init_MethodUtil == null || account.init_MethodUtil == this.init_MethodUtil) {
            count++;
         }
      }

      return count;
   }

   private String init_Str_8() {
      if (this.init_MethodUtil != null) {
         return this.init_MethodUtil.name();
      }

      StringBuilder names = new StringBuilder();

      for (autismclient.util.MethodUtil type : autismclient.util.MethodUtil.values()) {
         if (names.length() > 0) {
            names.append(", ");
         }

         names.append(type.name());
      }

      return names.toString();
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int rawMouseX = mouseX;
      int rawMouseY = mouseY;
      if (autismclient.gui.f.b.SyncManager.init_Z_List(this.close_List)) {
         mouseX = -2147483648;
         mouseY = -2147483648;
      }

      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -1610612736);
      int px = this.write_I_11();
      int py = this.update_I_10();
      int left = px + 4;
      int innerW = this.flush_I_7();
      int line = 12;
      int titleY = this.init_I_I_I_8(py + 4, 12);
      this.init_V_GuiGraphicsExtract_2(graphics, px, py, this.sync_I_8(), this.tick_I_13(), -401402080);
      if (this.clear == 2) {
         int textTop = py + 4 + 12 + 2;
         this.init_V_GuiGraphicsExtract_4(graphics, "Clear Accounts?", left, titleY, ValueSink.flush, false);
         this.init_V_GuiGraphicsExtract_2(
            graphics, this.run_I_3() + " accounts will be deleted in:", left, this.init_I_I_I_8(textTop, line), ValueSink.close, false, innerW
         );
         this.init_V_GuiGraphicsExtract_2(graphics, this.init_Str_8(), left, this.init_I_I_I_8(textTop + line, line), -14249, false, innerW);
         this.init_V_GuiGraphicsExtract_2(graphics, "This cannot be undone.", left, this.init_I_I_I_8(textTop + line * 2, line), ValueSink.close, false, innerW);
      } else {
         int ctl = this.set_I_3();
         this.init_V_GuiGraphicsExtract_4(graphics, "Generate Accounts", left, titleY, ValueSink.flush, false);
         if (this.reset == 0) {
            int countY = py + this.init_I_I_13(0);
            this.init_V_GuiGraphicsExtract_4(graphics, "Count:", left, this.init_I_I_I_8(countY, ctl), ValueSink.close, false);
            this.write_EditBox.setX(left + k.init(this.font, "Count:", init_CharEncoder.init_Identifier_l(l.read), 0) + 8);
            this.write_EditBox.setY(countY);
            this.write_EditBox.setHeight(ctl);
            this.write_EditBox.extractRenderState(graphics, mouseX, mouseY, delta);
            if (this.read_Z_2()) {
               this.update_EditBox.setX(left);
               this.update_EditBox.setY(py + this.init_I_I_13(3));
               this.update_EditBox.setWidth(innerW);
               this.update_EditBox.setHeight(ctl);
               this.update_EditBox.extractRenderState(graphics, mouseX, mouseY, delta);
            }
         } else {
            this.init_MultiLineEditBox.setX(left);
            this.init_MultiLineEditBox.setY(py + this.open_I_4());
            this.init_MultiLineEditBox.setWidth(innerW);
            this.init_MultiLineEditBox.setHeight(this.poll_I_3());
            this.init_MultiLineEditBox.extractRenderState(graphics, mouseX, mouseY, delta);
         }

         if (!this.update_Str.isBlank()) {
            this.init_V_GuiGraphicsExtract_2(graphics, this.update_Str, left, this.init_I_I_I_8(py + this.push_I_3(), line), ValueSink.open, false, innerW);
         }
      }

      for (autismclient.gui.f.b.ByteReader button : this.flush_List) {
         autismclient.gui.f.b.ByteReader.init(graphics, this.font, button, mouseX, mouseY);
      }

      autismclient.gui.f.b.SyncManager.load(graphics, this.font, this.close_List, rawMouseX, rawMouseY);
      autismclient.gui.f.b.SyncManager.read(graphics, this.font, this.close_List, rawMouseX, rawMouseY);
   }

   private boolean read_Z_2() {
      AutismConfig config = AutismConfig.getGlobal();
      return this.clear == 1 && this.reset == 0 && config.accountGenSetPassword && "Set For All".equalsIgnoreCase(config.accountGenPasswordMode);
   }

   private void init_V_MouseButtonEvent_Z(MouseButtonEvent event, boolean doubleClick) {
      if (this.clear != 1 || !autismclient.gui.f.b.SyncManager.init_Z_List_2(this.close_List, event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
         for (autismclient.gui.f.b.ByteReader button : this.flush_List) {
            if (autismclient.gui.f.b.ByteReader.init(button, event.x(), event.y(), Compat263.mouseEventGlfwButton(event))) {
               return;
            }
         }

         if (this.clear == 1) {
            if (this.reset == 0) {
               if (this.read_Z_2() && this.update_EditBox != null) {
                  if (this.update_EditBox.mouseClicked(event, doubleClick)) {
                     this.update_EditBox.setFocused(true);
                     if (this.write_EditBox != null) {
                        this.write_EditBox.setFocused(false);
                     }

                     return;
                  }

                  this.update_EditBox.setFocused(false);
               }

               if (this.write_EditBox != null && this.write_EditBox.mouseClicked(event, doubleClick)) {
                  this.write_EditBox.setFocused(true);
                  if (this.update_EditBox != null) {
                     this.update_EditBox.setFocused(false);
                  }

                  return;
               }

               if (this.write_EditBox != null) {
                  this.write_EditBox.setFocused(false);
               }
            } else if (this.reset == 1 && this.init_MultiLineEditBox != null) {
               if (this.init_MultiLineEditBox.mouseClicked(event, doubleClick)) {
                  this.init_MultiLineEditBox.setFocused(true);
                  return;
               }

               this.init_MultiLineEditBox.setFocused(false);
            }
         }
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.clear != 0) {
         if (Compat263.keyEventGlfwKey(event) == 256) {
            this.get_V_3();
            return true;
         }

         if (this.clear == 1) {
            if (this.reset == 0 && this.read_Z_2() && this.update_EditBox != null && this.update_EditBox.keyPressed(event)) {
               return true;
            }

            if (this.reset == 0 && this.write_EditBox != null && this.write_EditBox.keyPressed(event)) {
               return true;
            }

            if (this.reset == 1 && this.init_MultiLineEditBox != null && this.init_MultiLineEditBox.keyPressed(event)) {
               return true;
            }
         }

         return true;
      } else {
         return super.keyPressed(event);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      if (this.clear != 0) {
         if (this.clear == 1) {
            if (this.reset == 0 && this.read_Z_2() && this.update_EditBox != null && this.update_EditBox.charTyped(event)) {
               return true;
            }

            if (this.reset == 0 && this.write_EditBox != null && this.write_EditBox.charTyped(event)) {
               return true;
            }

            if (this.reset == 1 && this.init_MultiLineEditBox != null && this.init_MultiLineEditBox.charTyped(event)) {
               return true;
            }
         }

         return true;
      } else {
         return super.charTyped(event);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.load_Z) {
         this.load_Z = false;
         return true;
      } else if (this.read_Z) {
         this.init_D = Blaze3D.getTime();
         this.read_Z = false;
         return true;
      } else {
         return super.mouseReleased(init_MouseButtonEvent_MouseButtonEvent(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.load_Z) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_I_4(this.load_List_5().size());
         this.pop = init_I_I_3(autismclient.gui.f.b.PoolEntry.init(scrollbar, virtualEvent.y(), this.run), this.add_I_2(), scrollbar.flush());
         this.init_CacheEntry.load(this.pop, scrollbar.flush());
         this.load_V_20();
         return true;
      } else if (this.read_Z) {
         this.init_F = Math.max(-50.0F, Math.min(50.0F, this.init_F - (float)cA.init_D_D(dy) * 2.5F));
         this.load_F = this.load_F + (float)cA.init_D_D(dx) * 2.5F;
         this.load_D = virtualEvent.x();
         this.read_D = virtualEvent.y();
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dx), cA.init_D_D(dy));
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      x = cA.init_D_D(x);
      y = cA.init_D_D(y);
      if (this.clear != 0) {
         autismclient.gui.f.b.SyncManager.init_Z_List(this.close_List, x, y, scrollY);
         return true;
      }

      if (this.flush_Z()) {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }

      if (!(x < this.buffer()) && !(x >= this.buffer() + this.entry()) && !(y < this.index()) && !(y >= this.index() + this.task())) {
         int maxScroll = this.load_I_I_4(this.load_List_5().size());
         if (maxScroll <= 0) {
            return true;
         }

         int next = this.pop - (int)Math.signum(scrollY) * this.add_I_2();
         this.pop = init_I_I_3(next, this.add_I_2(), maxScroll);
         this.init_CacheEntry.init_V_I_I(this.pop, maxScroll);
         this.load_V_20();
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.close_V_5();
      this.load_Z = false;
      this.next_V();
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private StreamUtil.Inner_BufferHelper init_StreamUtil_BufferH_BitSetUtil(autismclient.util.BitSetUtil account) {
      String key = this.read_Str_BitSetUtil(account);
      synchronized (this.init_Map) {
         StreamUtil.Inner_BufferHelper lookup = this.init_Map.computeIfAbsent(key, ignored -> this.load_StreamUtil_BufferH_BitSetUtil(account));
         this.next_V();
         return lookup;
      }
   }

   private StreamUtil.Inner_BufferHelper load_StreamUtil_BufferH_BitSetUtil(autismclient.util.BitSetUtil account) {
      UUID id = this.init_UUID_BitSetUtil(account);
      String name = account != null && !account.init_Str().isBlank() ? account.init_Str() : "Autism";
      if (account != null && account.init_MethodUtil == autismclient.util.MethodUtil.init && !name.isBlank() && this.minecraft != null) {
         UUID offlineId = UUIDUtil.createOfflinePlayerUUID(name);
         PlayerSkin defaultSkin = DefaultPlayerSkin.get(offlineId);

         try {
            CompletableFuture<Optional<PlayerSkin>> future = CompletableFuture.<Optional<GameProfile>>supplyAsync(
                  () -> this.init_Optional_Str(name), Util.nonCriticalIoPool()
               )
               .thenCompose(profilex -> profilex.map(this.minecraft.getSkinManager()::get).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
            return new StreamUtil.Inner_BufferHelper(() -> {
               try {
                  return future.getNow(Optional.empty()).orElse(defaultSkin);
               } catch (Exception ignored) {
                  return defaultSkin;
               }
            }, future, defaultSkin);
         } catch (Exception ignored) {
            return new StreamUtil.Inner_BufferHelper(() -> defaultSkin, CompletableFuture.completedFuture(Optional.empty()), defaultSkin);
         }
      } else if (account != null && !name.isBlank() && this.minecraft != null && this.write_Z_BitSetUtil(account)) {
         UUID offlineId = UUIDUtil.createOfflinePlayerUUID(name);
         PlayerSkin defaultSkin = DefaultPlayerSkin.get(id == null ? offlineId : id);
         ResolvableProfile profile = id == null ? ResolvableProfile.createUnresolved(name) : ResolvableProfile.createUnresolved(id);

         try {
            RenderInfo defaultInfo = this.minecraft.playerSkinRenderCache().getOrDefault(profile);
            Supplier<RenderInfo> lookup = this.minecraft.playerSkinRenderCache().createLookup(profile);
            CompletableFuture<Optional<RenderInfo>> future = this.minecraft.playerSkinRenderCache().lookup(profile);
            return new StreamUtil.Inner_BufferHelper(() -> {
               try {
                  RenderInfo info = lookup.get();
                  return info == null ? defaultSkin : info.playerSkin();
               } catch (Exception ignored) {
                  return defaultSkin;
               }
            }, future, defaultInfo.playerSkin());
         } catch (Exception ignored) {
            return new StreamUtil.Inner_BufferHelper(() -> defaultSkin, CompletableFuture.completedFuture(Optional.empty()), defaultSkin);
         }
      } else {
         if (id == null) {
            UUID fallbackId = UUIDUtil.createOfflinePlayerUUID(name);
            PlayerSkin fallback = DefaultPlayerSkin.get(fallbackId);
            return new StreamUtil.Inner_BufferHelper(() -> fallback, CompletableFuture.completedFuture(Optional.empty()), fallback);
         }

         try {
            PlayerSkin fallback = DefaultPlayerSkin.get(id);
            CompletableFuture<Optional<PlayerSkin>> future = CompletableFuture.<GameProfile>supplyAsync(
                  () -> this.minecraft.services().profileResolver().fetchById(id).orElse(new GameProfile(id, name)), Util.nonCriticalIoPool()
               )
               .thenCompose(profilex -> this.minecraft.getSkinManager().get(profilex));
            return new StreamUtil.Inner_BufferHelper(() -> {
               try {
                  return future.getNow(Optional.empty()).orElse(fallback);
               } catch (Exception ignoredx) {
                  return fallback;
               }
            }, future, fallback);
         } catch (Exception ignored) {
            PlayerSkin fallback = DefaultPlayerSkin.get(id);
            return new StreamUtil.Inner_BufferHelper(() -> fallback, CompletableFuture.completedFuture(Optional.empty()), fallback);
         }
      }
   }

   private Optional<GameProfile> init_Optional_Str(String name) {
      String username = init_Str_Str(name);
      if (init_Z_Str_4(username) && this.minecraft != null) {
         try {
            Optional<GameProfile> resolved = this.minecraft.services().profileResolver().fetchByName(username);
            if (resolved.isPresent() && resolved.get().properties().containsKey("textures")) {
               return resolved;
            }

            Optional<GameProfile> withTextures = resolved.flatMap(this::init_Optional_GameProfile);
            if (withTextures.isPresent()) {
               return withTextures;
            }
         } catch (Exception var5) {
         }

         return this.load_Optional_Str(username);
      } else {
         return Optional.empty();
      }
   }

   private Optional<GameProfile> load_Optional_Str(String username) {
      try {
         String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
         JsonObject profile = AutismHttp.load_JsonObject("https://api.mojang.com/users/profiles/minecraft/" + encoded, null);
         if (profile != null && profile.has("id") && profile.has("name")) {
            UUID id = UndashedUuid.fromStringLenient(profile.get("id").getAsString());
            String resolvedName = profile.get("name").getAsString();
            return this.init_Optional_GameProfile(new GameProfile(id, resolvedName));
         } else {
            return Optional.empty();
         }
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   private Optional<GameProfile> init_Optional_GameProfile(GameProfile profile) {
      if (profile != null && profile.id() != null) {
         if (profile.properties().containsKey("textures")) {
            return Optional.of(profile);
         }

         try {
            String id = UndashedUuid.toString(profile.id());
            JsonObject textureProfile = AutismHttp.load_JsonObject("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false", null);
            if (textureProfile == null || !textureProfile.has("properties") || !textureProfile.get("properties").isJsonArray()) {
               return Optional.empty();
            }

            for (JsonElement element : textureProfile.getAsJsonArray("properties")) {
               if (element.isJsonObject()) {
                  JsonObject property = element.getAsJsonObject();
                  String propertyName = init_Str_JsonObject_Str(property, "name");
                  String value = init_Str_JsonObject_Str(property, "value");
                  String signature = init_Str_JsonObject_Str(property, "signature");
                  if ("textures".equals(propertyName) && !value.isBlank() && !signature.isBlank()) {
                     String resolvedName = init_Str_JsonObject_Str(textureProfile, "name");
                     GameProfile texturedProfile = new GameProfile(profile.id(), resolvedName.isBlank() ? profile.name() : resolvedName);
                     texturedProfile.properties().put("textures", new Property("textures", value, signature));
                     return Optional.of(texturedProfile);
                  }
               }
            }
         } catch (Exception var12) {
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   private static boolean init_Z_Str_4(String username) {
      if (username != null && username.length() >= 3 && username.length() <= 16) {
         for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_') {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static String init_Str_JsonObject_Str(JsonObject object, String key) {
      if (object != null && key != null && object.has(key)) {
         JsonElement element = object.get(key);
         return element != null && element.isJsonPrimitive() ? element.getAsString() : "";
      } else {
         return "";
      }
   }

   private String read_Str_BitSetUtil(autismclient.util.BitSetUtil account) {
      return account == null
         ? "empty"
         : (this.write_Z_BitSetUtil(account) ? "default" : account.init_MethodUtil.name())
            + ":"
            + init_Str_Str(account.update)
            + ":"
            + init_Str_Str(account.sync)
            + ":"
            + init_Str_Str(account.load_Str);
   }

   private void next_V() {
      synchronized (this.init_Map) {
         if (this.init_Map.size() > 10) {
            Set<String> protectedKeys = this.init_Set_2();
            int limit = Math.max(10, protectedKeys.size());
            Iterator<Entry<String, StreamUtil.Inner_BufferHelper>> iterator = this.init_Map.entrySet().iterator();

            while (this.init_Map.size() > limit && iterator.hasNext()) {
               Entry<String, StreamUtil.Inner_BufferHelper> entry = iterator.next();
               if (!protectedKeys.contains(entry.getKey())) {
                  iterator.remove();
               }
            }
         }
      }
   }

   private Set<String> init_Set_2() {
      Set<String> keys = new HashSet<>();
      autismclient.util.BitSetUtil preview = this.init_BitSetUtil();
      this.init_V_Set_BitSetUtil(keys, preview);

      for (StreamUtil.Inner_DataUtil row : this.load_List) {
         this.init_V_Set_BitSetUtil(keys, row.init_BitSetUtil);
      }

      autismclient.util.BitSetUtil defaultAccount = this.load_BitSetUtil();
      this.init_V_Set_BitSetUtil(keys, defaultAccount);
      return keys;
   }

   private void init_V_Set_BitSetUtil(Set<String> keys, autismclient.util.BitSetUtil account) {
      if (account != null) {
         String key = this.read_Str_BitSetUtil(account);
         keys.add(key);
      }
   }

   private List<autismclient.util.BitSetUtil> init_List_10() {
      List<autismclient.util.BitSetUtil> accounts = this.read_List_2();
      String query = load_Str_Str_6(this.load_Str);
      int filterMask = this.get_I_3();
      if (this.write_J == this.read_J && query.equals(this.write_Str) && filterMask == this.get) {
         return this.write_List;
      }

      this.write_J = this.read_J;
      this.write_Str = query;
      this.get = filterMask;
      this.update_List = null;
      if (query.isEmpty() && this.init_MethodUtil == null) {
         this.write_List = accounts;
         return this.write_List;
      }

      List<autismclient.util.BitSetUtil> filtered = new ArrayList<>();

      for (autismclient.util.BitSetUtil account : accounts) {
         if (account != null
            && (this.init_MethodUtil == null || account.init_MethodUtil == this.init_MethodUtil)
            && (query.isEmpty() || this.init_Z_BitSetUtil_Str(account, query))) {
            filtered.add(account);
         }
      }

      this.write_List = List.copyOf(filtered);
      return this.write_List;
   }

   private List<StreamUtil.Inner_StreamUtil> load_List_5() {
      List<autismclient.util.BitSetUtil> filtered = this.init_List_10();
      if (this.update_List == filtered) {
         return this.sync_List;
      }

      List<StreamUtil.Inner_StreamUtil> rows = new ArrayList<>();
      autismclient.util.BitSetUtil defaultAccount = this.load_BitSetUtil();
      if (defaultAccount != null) {
         rows.add(new StreamUtil.Inner_StreamUtil(defaultAccount, true));
      }

      for (autismclient.util.BitSetUtil account : filtered) {
         rows.add(new StreamUtil.Inner_StreamUtil(account, false));
      }

      this.update_List = filtered;
      this.sync_List = List.copyOf(rows);
      return this.sync_List;
   }

   private List<autismclient.util.BitSetUtil> read_List_2() {
      if (!this.write_Z) {
         return this.read_List;
      }

      this.read_List = List.copyOf(autismclient.util.TokenStore.init_TokenStore().init_List());
      this.write_Z = false;
      this.read_J++;
      this.write_J = -9223372036854775808L;
      this.update_List = null;
      return this.read_List;
   }

   private void prev_V() {
      this.write_Z = true;
   }

   private int get_I_3() {
      return this.init_MethodUtil == null ? -1 : 1 << this.init_MethodUtil.ordinal();
   }

   private boolean init_Z_BitSetUtil_Str(autismclient.util.BitSetUtil account, String query) {
      String name = load_Str_Str_6(account == null ? "" : account.init_Str());
      if (query.isEmpty()) {
         return true;
      } else {
         return name.contains(query) ? true : init_Z_Str_Str_2(name, query);
      }
   }

   private static boolean init_Z_Str_Str_2(String value, String query) {
      if (value != null && query != null && !query.isEmpty()) {
         int valueIndex = 0;
         int queryIndex = 0;
         int misses = 0;
         int maxMisses = Math.max(1, query.length() / 3);

         while (valueIndex < value.length() && queryIndex < query.length()) {
            if (value.charAt(valueIndex) == query.charAt(queryIndex)) {
               queryIndex++;
            } else if (queryIndex > 0) {
               misses++;
            }

            if (misses > maxMisses) {
               return false;
            }

            valueIndex++;
         }

         return queryIndex == query.length();
      } else {
         return true;
      }
   }

   private static String load_Str_Str_6(String value) {
      return init_Str_Str(value).toLowerCase(Locale.ROOT);
   }

   private UUID init_UUID_BitSetUtil(autismclient.util.BitSetUtil account) {
      if (account != null && account.sync != null && !account.sync.isBlank()) {
         try {
            return UndashedUuid.fromStringLenient(account.sync);
         } catch (Exception ignored) {
            try {
               return UUID.fromString(account.sync);
            } catch (Exception ignoredAgain) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private autismclient.util.BitSetUtil init_BitSetUtil() {
      if (this.load_BitSetUtil != null) {
         return this.load_BitSetUtil;
      }

      autismclient.util.BitSetUtil defaultAccount = this.load_BitSetUtil();
      if (defaultAccount != null && this.write_Z_2()) {
         return defaultAccount;
      }

      List<autismclient.util.BitSetUtil> accounts = this.read_List_2();

      for (autismclient.util.BitSetUtil account : accounts) {
         if (this.read_Z_BitSetUtil(account)) {
            return account;
         }
      }

      if (defaultAccount != null) {
         return defaultAccount;
      } else {
         return accounts.isEmpty() ? null : accounts.get(0);
      }
   }

   private boolean read_Z_BitSetUtil(autismclient.util.BitSetUtil account) {
      if (account != null && this.minecraft != null && this.minecraft.getUser() != null) {
         UUID accountId = this.init_UUID_BitSetUtil(account);
         if (accountId != null) {
            return accountId.equals(this.minecraft.getUser().getProfileId());
         }

         String currentName = this.minecraft.getUser().getName();
         String accountName = init_Str_Str(account.update);
         if (accountName.isBlank()) {
            accountName = init_Str_Str(account.load_Str);
         }

         return currentName != null && !accountName.isBlank() && currentName.equals(accountName);
      } else {
         return false;
      }
   }

   private boolean write_Z_2() {
      User original = autismclient.util.FieldCache.init_User();
      User current = this.minecraft == null ? null : this.minecraft.getUser();
      return original != null && current != null && original.getProfileId().equals(current.getProfileId()) && original.getName().equals(current.getName());
   }

   private boolean write_Z_BitSetUtil(autismclient.util.BitSetUtil account) {
      autismclient.util.BitSetUtil defaultAccount = this.load_BitSetUtil();
      return defaultAccount != null
         && account != null
         && defaultAccount.sync.equals(account.sync)
         && defaultAccount.update.equals(account.update)
         && account.load_Str != null
         && account.load_Str.startsWith("Default Minecraft:");
   }

   private autismclient.util.BitSetUtil load_BitSetUtil() {
      User user = autismclient.util.FieldCache.init_User();
      if (user == null) {
         return null;
      }

      autismclient.util.BitSetUtil account = new autismclient.util.BitSetUtil();
      account.init_MethodUtil = init_Str_Str(user.getAccessToken()).isBlank() ? autismclient.util.MethodUtil.init : autismclient.util.MethodUtil.read;
      account.load_Str = "Default Minecraft: " + user.getName();
      account.update = user.getName();
      account.sync = user.getProfileId().toString();
      account.read_Str = user.getAccessToken();
      return account;
   }

   private int set_I_3() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int put_I_2() {
      return autismclient.gui.f.RenderState.init(18);
   }

   private int add_I_2() {
      return this.put_I_2() + 2;
   }

   private int clear_I_2() {
      return this.set_I_3();
   }

   private int init_I_I_I_8(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).init_I(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int init_I_Str_16(String text) {
      return k.init(this.font, text == null ? "" : text, init_CharEncoder.init_Identifier_l(l.read), 0);
   }

   private int reset_I() {
      return autismclient.gui.f.c.NetHelper.load(this.init_I(), this.start_I(), 6);
   }

   private int start_I() {
      return autismclient.gui.f.c.NetHelper.init(this.init_I(), 6, 414);
   }

   private int stop_I() {
      return 6;
   }

   private int next_I() {
      return this.reset_I() + 4;
   }

   private int prev_I() {
      return 254;
   }

   private int valid_I() {
      return this.stop_I() + 4;
   }

   private int size() {
      return this.valid_I() + this.set_I_3() + 2;
   }

   private int data() {
      return this.size() + this.set_I_3() + 2;
   }

   private int value() {
      return this.data() + this.set_I_3() + 2;
   }

   private int cache() {
      return this.value() + this.set_I_3() + 2;
   }

   private int state() {
      return this.cache() + this.set_I_3() + 2;
   }

   private int queue() {
      return this.state() + this.set_I_3() + 4 - this.stop_I();
   }

   private int buffer() {
      return this.reset_I();
   }

   private int entry() {
      return Math.max(1, this.start_I());
   }

   private int node() {
      return this.reset_I() + 262 + 4;
   }

   private boolean init_Z_D_D(double x, double y) {
      if (this.sync_Z()) {
         return false;
      }

      int previewX = this.node();
      return x >= previewX && x < previewX + 148 && y >= this.stop_I() && y < this.stop_I() + this.queue();
   }

   private int task() {
      int cap = Math.max(3, this.load_I() * 65 / 100 / this.add_I_2());
      int shown = Math.max(3, Math.min(cap, this.load_List_5().size()));
      int content = this.flags() + shown * this.add_I_2() - 2 + 4;
      int available = Math.max(1, this.load_I() - 6 - this.index());
      return Math.min(available, content);
   }

   private int pool() {
      return this.stop_I() + this.queue() + 4;
   }

   private int index() {
      if (!this.sync_Z()) {
         return this.pool();
      } else {
         return this.update_Z_2() ? this.stop_I() + this.set_I_3() + 2 : this.stop_I();
      }
   }

   private boolean update_Z_2() {
      return this.sync_Z() && this.entry() >= 190 && this.a() - 4 - 20 < this.buffer() + 4 + this.load_I_Str_3("Back") + 4;
   }

   private int count() {
      return this.index() + 4;
   }

   private int flags() {
      return 4 + this.set_I_3() + 2;
   }

   private int level() {
      return this.index() + this.flags();
   }

   private int frame() {
      return this.level();
   }

   private int block() {
      return this.index() + this.task() - 4;
   }

   private int chunk() {
      return Math.max(1, (this.block() - this.frame() + 2) / this.add_I_2());
   }

   private int table() {
      return this.chunk() * this.add_I_2() - 2;
   }

   private int load_I_I_4(int savedRows) {
      return Math.max(0, savedRows * this.add_I_2() - 2 - this.table());
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_I_4(int savedRows) {
      int contentPixels = Math.max(0, savedRows * this.add_I_2() - 2);
      int viewPixels = this.table();
      int trackY = this.frame();
      int trackHeight = this.table();
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         contentPixels, viewPixels, this.store(), trackY, 6, trackHeight, this.init_CacheEntry.init_I_F_I(0.0F, this.load_I_I_4(savedRows))
      );
   }

   private int store() {
      return this.buffer() + this.entry() - 4 - 6;
   }

   private int drain() {
      return this.buffer() + 4;
   }

   private int fill() {
      return this.store() - 2;
   }

   private int mark() {
      return Math.max(1, this.fill() - this.drain());
   }

   private int load(int rowY, int buttonHeight) {
      return rowY + Math.max(0, (this.put_I_2() - buttonHeight) / 2);
   }

   private boolean sync_Z() {
      return this.start_I() < 414 || this.load_I() < this.pool() + this.flags() + this.add_I_2() * 2 + 4 + 6;
   }

   private boolean flush_Z() {
      return this.entry() < 190 || this.task() < this.flags() + this.put_I_2();
   }

   private static int init_I_I_3(int value, int step, int maxScroll) {
      int clamped = Math.max(0, Math.min(maxScroll, value));
      if (step <= 0) {
         return clamped;
      }

      int rounded = Math.round((float)clamped / step) * step;
      return Math.max(0, Math.min(maxScroll, rounded));
   }

   private void init_V_GuiGraphicsExtract_4(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center) {
      this.init_V_GuiGraphicsExtract_2(graphics, text, x, y, color, center, 2147483647);
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center, int maxWidth) {
      Font renderer = this.font;
      Identifier font = init_CharEncoder.init_Identifier_l(l.read);
      String value = text == null ? "" : text;
      if (maxWidth != 2147483647 && !center) {
         k.init(graphics, renderer, value, font, color, x, y, Math.max(1, maxWidth), false);
      } else {
         if (maxWidth != 2147483647) {
            value = k.init(renderer, value, maxWidth, font, color);
         }

         int w = k.init(renderer, value, font, color);
         int drawX = center ? x - w / 2 : x;
         k.init(graphics, renderer, value, font, color, drawX, y, false);
      }
   }

   private int load_I_Str_3(String label) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int scan() {
      return Math.max(this.load_I_Str_3("Login"), this.load_I_Str_3("Active"));
   }

   private int[] init_IArr_I_3(int availableWidth, int gap, String... labels) {
      int[] widths = new int[labels.length];
      int total = Math.max(0, labels.length - 1) * gap;

      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.load_I_Str_3(labels[i]);
         total += widths[i];
      }

      if (total <= availableWidth) {
         return widths;
      }

      int room = Math.max(labels.length, availableWidth - Math.max(0, labels.length - 1) * gap);
      int preferred = total - Math.max(0, labels.length - 1) * gap;
      int used = 0;

      for (int i = 0; i < widths.length; i++) {
         widths[i] = i == widths.length - 1 ? Math.max(1, room - used) : Math.max(1, widths[i] * room / preferred);
         used += widths[i];
      }

      return widths;
   }

   private int init_I_List_4(List<String> options) {
      int widest = 0;

      for (String option : options) {
         widest = Math.max(widest, k.init(this.font, option, init_CharEncoder.init_Identifier_l(l.read), 0));
      }

      return Math.max(56, widest + 26);
   }

   private int parse() {
      return Math.max(this.load_I_Str_3("Check"), this.load_I_Str_3("Checking"));
   }

   private int bind() {
      return this.fill() - 4;
   }

   private int a() {
      return this.bind() - this.parse() - 2 - this.load_I_Str_3("Clear Expired") - 2 - this.load_I_Str_3("Generate") - 2 - this.load_I_Str_3("Clear List");
   }

   private void valid_V() {
      if (this.init_EditBox != null) {
         this.init_EditBox.setFocused(false);
      }

      if (this.load_EditBox != null) {
         this.load_EditBox.setFocused(false);
      }

      if (this.read_EditBox != null) {
         this.read_EditBox.setFocused(false);
      }

      this.setFocused(null);
   }

   private String load_Str_5() {
      String var10000;
      switch (this.load_MethodUtil) {
         case init:
         case update:
            var10000 = "Cracked username";
            break;
         case write:
            var10000 = "TheAltening token";
            break;
         case load:
            var10000 = "Session access token";
            break;
         case read:
            var10000 = "";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String read_Str_3() {
      if (this.init_BitSetUtil != null) {
         return "Rename";
      }

      String var10000;
      switch (this.load_MethodUtil) {
         case init:
         case update:
            var10000 = "Add Cracked";
            break;
         case write:
            var10000 = "Add Altening";
            break;
         case load:
            var10000 = "Add Session";
            break;
         case read:
            var10000 = "Login with Microsoft";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int read_I_I_2(int argb) {
      return ct.init_I_I_ct_DataUtil(argb, ct.Inner_DataUtil.load);
   }

   private static int write_I_I(int argb) {
      return ct.init_I_I_ct_DataUtil(argb, ct.Inner_DataUtil.sync);
   }

   private record Inner_BufferHelper(Supplier<PlayerSkin> init_Supplier, CompletableFuture<?> init_CompletableFuture, PlayerSkin init_PlayerSkin) {
      private PlayerSkin load() {
         try {
            PlayerSkin skin = this.init_Supplier.get();
            return skin == null ? this.init_PlayerSkin : skin;
         } catch (Exception ignored) {
            return this.init_PlayerSkin;
         }
      }

      private boolean init_Z() {
         return this.init_CompletableFuture != null && !this.init_CompletableFuture.isDone();
      }
   }

   private enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update;
   }

   private record Inner_DataUtil(
      autismclient.util.BitSetUtil init_BitSetUtil,
      int init_I,
      autismclient.gui.f.b.ByteReader init_ByteReader,
      autismclient.gui.f.b.ByteReader load,
      autismclient.gui.f.b.ByteReader read,
      autismclient.gui.f.b.ByteReader write,
      boolean init_Z
   ) {
   }

   private record Inner_StreamUtil(autismclient.util.BitSetUtil init_BitSetUtil, boolean init_Z) {
   }
}
