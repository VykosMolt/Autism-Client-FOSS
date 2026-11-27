package autismclient.ai;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class AiSettingsView {
   private static final List<AiSettingsView.Inner_DataUtil> init_List = List.of(
      new AiSettingsView.Inner_DataUtil("plan", "Plan mode (approve before acting)", s -> s.planMode, (s, v) -> s.planMode = v),
      new AiSettingsView.Inner_DataUtil("verify", "Verify outcomes (strict, slower)", s -> s.verifyOutcomes, (s, v) -> s.verifyOutcomes = v),
      new AiSettingsView.Inner_DataUtil("quick", "Use built-in when possible", s -> s.hostIntents, (s, v) -> s.hostIntents = v),
      new AiSettingsView.Inner_DataUtil("vision", "Vision (screenshots)", s -> s.vision, (s, v) -> s.vision = v),
      new AiSettingsView.Inner_DataUtil("search", "Provider web search", s -> s.nativeSearch, (s, v) -> s.nativeSearch = v),
      new AiSettingsView.Inner_DataUtil("respawn", "Auto-respawn", s -> s.autoRespawn, (s, v) -> s.autoRespawn = v),
      new AiSettingsView.Inner_DataUtil("rejoin", "Auto-rejoin", s -> s.autoRejoin, (s, v) -> s.autoRejoin = v),
      new AiSettingsView.Inner_DataUtil("input", "Stop on input", s -> s.stopOnInput, (s, v) -> s.stopOnInput = v)
   );
   private static final List<AiSettingsView.Inner_DataUtil> load_List = List.of(
      new AiSettingsView.Inner_DataUtil("teleport", "Teleport", s -> s.allowTeleport, (s, v) -> s.allowTeleport = v),
      new AiSettingsView.Inner_DataUtil("clips", "Clips", s -> s.allowClips, (s, v) -> s.allowClips = v),
      new AiSettingsView.Inner_DataUtil("fly", "Flight", s -> s.allowFly, (s, v) -> s.allowFly = v),
      new AiSettingsView.Inner_DataUtil("aim", "Silent aim", s -> s.aiSilentAim, (s, v) -> s.aiSilentAim = v)
   );
   private static final List<AiSettingsView.Inner_DataUtil> read_List = List.of(
      new AiSettingsView.Inner_DataUtil("paths", "Calculated paths", s -> s.visualizePaths, (s, v) -> s.visualizePaths = v),
      new AiSettingsView.Inner_DataUtil("targets", "Mining outlines and active placement", s -> s.visualizeTargets, (s, v) -> s.visualizeTargets = v),
      new AiSettingsView.Inner_DataUtil("ghosts", "Textured schematic previews", s -> s.visualizeGhosts, (s, v) -> s.visualizeGhosts = v)
   );
   private static final AiSettingsView.Inner_DataUtil init_cf_DataUtil = new AiSettingsView.Inner_DataUtil(
      "scripts", "Unrestricted scripts", s -> s.unrestrictedScripts, (s, v) -> s.unrestrictedScripts = v
   );
   private static final Set<String> init_Set = Set.of("plan", "verify", "quick", "vision", "search");
   private static final List<String> write_List = List.of("OpenAI", "DashScope", "Moonshot", "DeepSeek", "Gemini", "Anthropic");
   private static final String init_Str = "Built-In";
   private static final List<String> update_List = Stream.concat(Stream.of("Built-In"), write_List.stream()).toList();
   private static final String[] init_StrArr = new String[]{
      "Provider", "API key", "Model", "Context", "Output", "Requests", "Tokens", "Minutes", "Endpoint", "Search key", "Stop key"
   };
   private static final long init_J = 1500L;
   private static final long load_J = 3000L;
   private static final int init_I = 2;
   private final AiService init_cc;
   private final AiViewState init_cF;
   private final AiAssistantOverlay init_AiAssistantOverlay;
   final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = init_BitSetUtil();
   final autismclient.gui.ui.widgets.TextInputWidget load_BitSetUtil = init_BitSetUtil();
   final autismclient.gui.ui.widgets.TextInputWidget read_BitSetUtil = init_BitSetUtil_I(512);
   final autismclient.gui.ui.widgets.TextInputWidget write_BitSetUtil = load_BitSetUtil();
   final autismclient.gui.ui.widgets.TextInputWidget update_BitSetUtil = load_BitSetUtil();
   final autismclient.gui.ui.widgets.TextInputWidget sync_BitSetUtil = load_BitSetUtil();
   final autismclient.gui.ui.widgets.TextInputWidget flush_BitSetUtil = load_BitSetUtil();
   final autismclient.gui.ui.widgets.TextInputWidget close = load_BitSetUtil();
   private final autismclient.gui.ui.widgets.DropdownWidget init_SyncManager;
   private final autismclient.gui.ui.widgets.DropdownWidget load_SyncManager;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> sync_List;
   private final AiModelCatalog init_av = new AiModelCatalog();
   private String load_Str = "";
   private List<String> flush_List = List.of();
   private String read_Str = "";
   private boolean init_Z;
   private boolean load_Z;
   private long read_J;
   private long write_J;
   private String write_Str = "";
   private final EnumSet<AiSettingsView.Inner_CachePool> init_EnumSet = EnumSet.of(AiSettingsView.Inner_CachePool.LIMITS, AiSettingsView.Inner_CachePool.ADVANCED);
   private final Map<String, AiSettingsView.Inner_StreamUtil> init_Map = new HashMap<>();
   private final AiUiKit.Inner_StreamUtil init_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(false);
   private AiSettings.Provider init_AiSettings_Provide = AiSettings.Provider.OPENAI;
   private boolean read_Z;
   private String[] load_StrArr = new String[0];
   private String update_Str = "";
   private boolean write_Z;
   private int load_I = -2147483648;
   private long update_J;
   private long sync_J;
   private int read_I;
   private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private boolean update_Z;
   private int write_I = -1;
   private long flush_J;

   AiSettingsView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.init_SyncManager = new autismclient.gui.ui.widgets.DropdownWidget(0, 0, 10, 10, update_List, 0, index -> {
         if (index == 0) {
            this.read_V();
         } else {
            this.init_V_AiSettings_Provide(AiSettings.Provider.values()[index - 1]);
         }
      });
      this.load_SyncManager = new autismclient.gui.ui.widgets.DropdownWidget(0, 0, 10, 10, List.of(), 0, this::init_V_I);
      this.load_SyncManager.active = false;
      this.sync_List = List.of(this.init_SyncManager, this.load_SyncManager);
      this.init_BitSetUtil.setOnChange(text -> this.write_V());
      this.read_BitSetUtil.setOnChange(text -> this.sync_V());

      for (autismclient.gui.ui.widgets.TextInputWidget field : List.of(
         this.init_BitSetUtil,
         this.load_BitSetUtil,
         this.read_BitSetUtil,
         this.write_BitSetUtil,
         this.update_BitSetUtil,
         this.sync_BitSetUtil,
         this.flush_BitSetUtil,
         this.close
      )) {
         field.setOnSubmit(text -> this.close());
      }
   }

   List<autismclient.gui.ui.widgets.TextInputWidget> init_List() {
      return List.of(
         this.init_BitSetUtil,
         this.load_BitSetUtil,
         this.read_BitSetUtil,
         this.write_BitSetUtil,
         this.update_BitSetUtil,
         this.sync_BitSetUtil,
         this.flush_BitSetUtil,
         this.close
      );
   }

   void init_V() {
      this.init_EnumSet.addAll(EnumSet.allOf(AiSettingsView.Inner_CachePool.class));
      this.init_EnumSet.remove(AiSettingsView.Inner_CachePool.VISUALS);
      this.init_cE_StreamUtil.scrollToTop();
   }

   private static autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil_I(int max) {
      return new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(max);
   }

   private static autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil() {
      autismclient.gui.ui.widgets.TextInputWidget field = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(2048);
      field.setDisplayTextProvider(text -> Component.literal("*".repeat(Math.min(64, text.length()))));
      return field;
   }

   private static autismclient.gui.ui.widgets.TextInputWidget load_BitSetUtil() {
      return new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(12).setInputFilter(v -> v.chars().allMatch(Character::isDigit));
   }

   void init_V_AiSettings(AiSettings s) {
      this.load_Z = true;
      this.init_av.init_V();
      this.write_Str = "";
      this.init_AiSettings_Provide = s.provider;
      this.read_Z = s.builtIn;
      this.init_SyncManager.setSelectedIndex(this.init_I());
      this.init_BitSetUtil.setValue("");
      this.load_BitSetUtil.setValue("");
      this.load_Str = s.model == null ? "" : s.model.trim();
      this.read_BitSetUtil.setValue(s.endpoint);
      this.write_BitSetUtil.setValue(Integer.toString(s.contextTokens));
      this.update_BitSetUtil.setValue(Integer.toString(s.outputTokens));
      this.sync_BitSetUtil.setValue(Integer.toString(s.maxRequests));
      this.flush_BitSetUtil.setValue(Long.toString(s.maxTokens));
      this.close.setValue(Integer.toString(s.maxMinutes));
      this.load_StrArr = this.init_StrArr();
      this.update_Str = "";
      this.write_Z = false;
      this.sync_J = 0L;
      autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
      this.load_Z = false;
      this.update_V();
      if (!this.read_Z) {
         this.init_V_Z(false);
      }
   }

   private int init_I() {
      return this.read_Z ? 0 : this.init_AiSettings_Provide.ordinal() + 1;
   }

   private String[] init_StrArr() {
      return new String[]{
         this.read_Z ? "Built-In" : this.init_AiSettings_Provide.name(),
         this.init_BitSetUtil.getValue(),
         this.load_BitSetUtil.getValue(),
         this.load_Str,
         this.read_BitSetUtil.getValue().trim(),
         this.write_BitSetUtil.getValue(),
         this.update_BitSetUtil.getValue(),
         this.sync_BitSetUtil.getValue(),
         this.flush_BitSetUtil.getValue(),
         this.close.getValue()
      };
   }

   private boolean sync_Z() {
      return !Arrays.equals(this.load_StrArr, this.init_StrArr());
   }

   private boolean flush_Z() {
      AiSettings live = this.init_cF.settings;
      return this.read_Z != live.builtIn
         || this.init_AiSettings_Provide != live.provider
         || !this.init_BitSetUtil.getValue().isBlank()
         || !this.load_Str.equals(live.model)
         || !this.read_BitSetUtil.getValue().trim().equals(live.endpoint);
   }

   private void read_V() {
      if (!this.read_Z) {
         this.read_Z = true;
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
         this.update_Str = "";
      }
   }

   private void init_V_AiSettings_Provide(AiSettings.Provider next) {
      if (this.read_Z) {
         this.read_Z = false;
         this.update_Str = "";
         if (next == this.init_AiSettings_Provide) {
            this.init_V_Z(false);
            return;
         }
      }

      if (next != this.init_AiSettings_Provide) {
         this.load_Z = true;
         this.init_AiSettings_Provide = next;
         this.load_Str = "";
         this.update_BitSetUtil.setValue(Integer.toString(AiSettings.defaultOutputTokens(next)));
         if (AiSettings.shouldAutofillEndpoint(this.read_BitSetUtil.getValue())) {
            this.read_BitSetUtil.setValue(next.endpoint);
         }

         this.init_BitSetUtil.setValue("");
         this.write_Str = "";
         this.sync_J = 0L;
         this.load_Z = false;
         this.sync_V();
         this.init_V_Z(false);
      }
   }

   private void write_V() {
      if (!this.load_Z) {
         String typed = this.init_BitSetUtil.getValue().trim();
         if (!typed.isEmpty() && AiKeyResolver.init_Z(typed)) {
            this.load_V_AiSettings_Provide(AiKeyResolver.init_List(typed).getFirst());
         }

         this.sync_V();
      }
   }

   private void load_V_AiSettings_Provide(AiSettings.Provider next) {
      if (next != this.init_AiSettings_Provide) {
         this.load_Z = true;
         String stockOutput = Integer.toString(AiSettings.defaultOutputTokens(this.init_AiSettings_Provide));
         this.init_AiSettings_Provide = next;
         this.read_Z = false;
         this.init_SyncManager.setSelectedIndex(this.init_I());
         if (this.update_BitSetUtil.getValue().trim().equals(stockOutput)) {
            this.update_BitSetUtil.setValue(Integer.toString(AiSettings.defaultOutputTokens(next)));
         }

         if (AiSettings.shouldAutofillEndpoint(this.read_BitSetUtil.getValue())) {
            this.read_BitSetUtil.setValue(next.endpoint);
         }

         this.load_Str = "";
         this.load_Z = false;
      }
   }

   private static String init_Str_Str(String endpoint) {
      try {
         return URI.create(endpoint).getHost();
      } catch (RuntimeException e) {
         return endpoint;
      }
   }

   private void init_V_I(int index) {
      this.flush_V();
      if (index >= 0 && index < this.flush_List.size()) {
         this.load_Str = this.flush_List.get(index);
         this.update_V();
      }
   }

   private void update_V() {
      this.flush_List = this.init_av.init_List();
      this.load_SyncManager.setOptions(this.flush_List);
      this.load_SyncManager.setSelectedIndex(Math.max(0, this.flush_List.indexOf(this.load_Str)));
      this.load_SyncManager.active = !this.init_av.load() && !this.flush_List.isEmpty();
      this.load_SyncManager
         .setLabelOverride(
            this.flush_List.contains(this.load_Str)
               ? ""
               : (this.init_av.load() ? "Scanning models..." : (this.flush_List.isEmpty() ? "Scan for models" : "Select a model"))
         );
   }

   private void sync_V() {
      if (!this.load_Z) {
         this.init_av.init_V();
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
         this.read_Str = "Scan to load models for this connection";
         this.init_Z = false;
         this.read_J = System.currentTimeMillis() + 700L;
         this.update_V();
      }
   }

   private String init_Str() {
      if (!this.init_BitSetUtil.getValue().isBlank()) {
         return this.init_BitSetUtil.getValue().trim();
      }

      AiSettings saved = this.init_cc.getSettings();
      return !this.write_Str.isEmpty() && this.write_Str.equals(saved.keyMarker(this.init_AiSettings_Provide.name()))
         ? ""
         : saved.key(this.init_AiSettings_Provide.name());
   }

   private void flush_V() {
      try {
         if (this.init_av.init_Z(AiModelCatalog.Inner_DataUtil.of(this.init_AiSettings_Provide, this.read_BitSetUtil.getValue(), this.init_Str()))) {
            autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
            this.read_Str = "Scan to load models for this connection";
            this.init_Z = false;
            this.read_J = System.currentTimeMillis() + 700L;
            this.update_V();
         }
      } catch (RuntimeException ex) {
         this.init_av.init_V();
         this.read_Str = "Check the endpoint and saved credential, then scan again";
         this.init_Z = true;
         this.update_V();
      }
   }

   private void init_V_Z(boolean force) {
      this.read_J = 0L;
      AiSettings.Provider p = this.init_AiSettings_Provide;

      String k;
      AiModelCatalog.Inner_DataUtil identity;
      try {
         k = this.init_Str();
         identity = AiModelCatalog.Inner_DataUtil.of(p, this.read_BitSetUtil.getValue(), k);
      } catch (RuntimeException ex) {
         this.init_av.init_V();
         this.read_Str = "Check the endpoint and saved credential, then scan again";
         this.init_Z = true;
         this.update_V();
         return;
      }

      this.init_av.init_Z(identity);
      if (force || !this.init_av.init_Z()) {
         long generation = this.init_av.init_J(identity);
         this.read_Str = "";
         this.init_Z = false;
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
         this.update_V();
         if (k.isBlank()) {
            this.init_av.init(generation, identity, new AiModels.Inner_CachePool(List.of(), "Add an API key to scan for models"));
            this.read_Str = this.init_av.init_Str();
            this.init_Z = true;
            this.update_V();
         } else {
            boolean typed = !this.init_BitSetUtil.getValue().isBlank();
            String endpointText = this.read_BitSetUtil.getValue();
            Thread worker = new Thread(
               () -> {
                  AiKeyResolver.Inner_DataUtil resolution = AiKeyResolver.init(k, p, endpointText, typed, () -> {
                     if (!this.init_av.init(generation)) {
                        throw new CancellationException("Model scan superseded");
                     }
                  });
                  AiModels.Inner_CachePool result = new AiModels.Inner_CachePool(resolution.models(), resolution.error(), resolution.endpoint(), resolution.provider());
                  Minecraft.getInstance()
                     .execute(
                        () -> {
                           try {
                              if (!identity.equals(AiModelCatalog.Inner_DataUtil.of(this.init_AiSettings_Provide, this.read_BitSetUtil.getValue(), this.init_Str()))) {
                                 return;
                              }
                           } catch (RuntimeException ignored) {
                              return;
                           }

                           if (this.init_av.init(generation, identity, result)) {
                              this.init_Z = !result.ok();
                              if (result.ok()) {
                                 boolean detected = resolution.provider() != this.init_AiSettings_Provide;
                                 if (detected) {
                                    this.load_V_AiSettings_Provide(resolution.provider());
                                 }

                                 boolean moved = !identity.endpoint().equals(resolution.endpoint());
                                 if (moved || detected) {
                                    this.load_Z = true;
                                    this.read_BitSetUtil.setValue(resolution.endpoint());
                                    this.load_Z = false;
                                 }

                                 if (!result.models().contains(this.load_Str)) {
                                    this.load_Str = resolution.model();
                                 }

                                 String name = AiKeyResolver.init(resolution.provider());
                                 this.read_Str = result.models().isEmpty()
                                    ? name + " returned no chat models for this key"
                                    : (detected ? "Detected " + name + " key: " : name + ": ")
                                       + result.models().size()
                                       + " models"
                                       + (this.load_Str.isEmpty() ? "" : ", " + this.load_Str + " selected")
                                       + (moved ? " via " + init_Str_Str(resolution.endpoint()) : "");
                              } else {
                                 this.read_Str = "Scan failed: " + result.error();
                              }

                              this.update_V();
                              this.init_cF.invalidate();
                           }
                        }
                     );
               },
               "ai-model-scan"
            );
            worker.setDaemon(true);
            worker.start();
         }
      }
   }

   boolean init_Z() {
      return autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.sync_List);
   }

   boolean init_Z_D_D(double x, double y) {
      return autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.sync_List, x, y);
   }

   boolean init_Z_D_2(double x, double y, int button) {
      this.flush_V();
      return this.init_Z() && autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.sync_List, x, y, button);
   }

   boolean init_Z_D(double x, double y, double amount) {
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.sync_List, x, y, amount)) {
         return true;
      }

      autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
      return false;
   }

   boolean load_Z() {
      boolean used = this.update_Z;
      if (this.update_Z) {
         this.update_Z = false;
         int value = this.write_I;
         this.init_cc.editSettingsAsync(s -> s.ghostOpacity = value);
         this.flush_J = System.currentTimeMillis() + 1500L;
         this.init_cF.invalidate();
      }

      return autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.sync_List) || used;
   }

   boolean load(double x, double y, int button) {
      if (this.update_Z) {
         this.init_V_D(x);
         return true;
      } else {
         return this.init_Z() && autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.sync_List, x, y, button);
      }
   }

   boolean read_Z() {
      boolean used = this.load_Z();
      return autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List) || used;
   }

   boolean write_Z() {
      return this.write_Z;
   }

   void load_V() {
      this.write_Z = false;
   }

   boolean update_Z() {
      if (!this.write_Z) {
         return false;
      }

      this.write_Z = false;
      return true;
   }

   boolean init_Z_I(int keyCode) {
      if (!this.write_Z) {
         return false;
      } else {
         this.write_Z = false;
         if (keyCode != 256 && keyCode != -1) {
            this.load_I = keyCode;
            this.update_J = System.currentTimeMillis() + 1500L;
            this.init_cc.editSettingsAsync(s -> s.emergencyKey = keyCode);
            this.init_cF.invalidate();
            return true;
         } else {
            return true;
         }
      }
   }

   void init_V_cE(AiUiKit kit) {
      autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(kit.graphics, kit.font, this.sync_List, kit.rawMouseX, kit.rawMouseY);
   }

   void init_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (now >= this.write_J) {
         this.write_J = now + 1000L;
         this.flush_V();
      }

      if (this.read_J > 0L && now >= this.read_J && !this.read_Z) {
         this.init_V_Z(false);
      }

      int ctrl = AiUiKit.toolbarHeight();
      autismclient.gui.f.NetHelper footer = autismclient.gui.f.NetHelper.init(area.read(), area.load() - ctrl, area.update(), ctrl);
      autismclient.gui.f.NetHelper list = autismclient.gui.f.NetHelper.init(
         area.read(), area.write(), area.update(), Math.max(0, footer.write() - 2 - area.write())
      );
      this.init_cE_StreamUtil.layout(list, this.read_I);
      kit.registerScrollPane(this.init_cE_StreamUtil);
      kit.pushClip(list);
      int x = list.read();
      int w = list.update() - 6 - 3;
      int labelW = 0;

      for (String label : init_StrArr) {
         labelW = Math.max(labelW, kit.measureTextWidth(label));
      }

      labelW = Math.min(labelW + 10, w * 2 / 5);
      int y = list.write() - this.init_cE_StreamUtil.offset;
      int start = y;
      AiSettings live = this.init_cF.settings;
      boolean hasKey = this.init_cc.hasProviderKey(this.init_AiSettings_Provide);
      y = this.init(kit, AiSettingsView.Inner_CachePool.PROVIDER, x, y, w);
      if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.PROVIDER)) {
         autismclient.gui.f.NetHelper field = this.init(kit, "Provider", x, y, w, labelW);
         this.init_SyncManager.setBounds(field.read(), field.write(), field.update(), field.sync());
         if (kit.isVisibleInClip(field)) {
            kit.addClickRegion(
               field,
               () -> autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(
                  this.sync_List, this.init_AiAssistantOverlay.read_I(), this.init_AiAssistantOverlay.write_I(), 0
               )
            );
         }

         y += ctrl + 2;
         if (this.read_Z) {
            kit.drawEllipsizedText("Runs in the client. Instant, free, no API.", x + 5, y + 1, w - 10, kit.colors.textSecondary);
            y += AiUiKit.lineHeight();
            autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(kit.graphics, kit.font, List.of(this.init_SyncManager), kit.mouseX, kit.mouseY);
            y += 4;
         }
      }

      if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.PROVIDER) && !this.read_Z) {
         this.init_BitSetUtil.setPlaceholder(hasKey ? "Key saved - type to replace" : "Paste your API key");
         kit.drawTextInput(this.init_BitSetUtil, this.init(kit, "API key", x, y, w, labelW));
         y += ctrl + 2;
         autismclient.gui.f.NetHelper modelBounds = this.init(kit, "Model", x, y, w - (ctrl + 2), labelW);
         this.load_SyncManager.setBounds(modelBounds.read(), modelBounds.write(), modelBounds.update(), modelBounds.sync());
         if (kit.isVisibleInClip(modelBounds)) {
            kit.addInteractiveRegion(
               modelBounds,
               () -> autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(
                  this.sync_List, this.init_AiAssistantOverlay.read_I(), this.init_AiAssistantOverlay.write_I(), 0
               ),
               null,
               this.flush_List.isEmpty() ? "Scan to list models returned for this key and endpoint" : this.flush_List.size() + " models to choose from"
            );
         }

         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(modelBounds.init() + 2, y, ctrl, ctrl),
            autismclient.util.GuiIconAtlas.REFRESH,
            this.init_av.load(),
            false,
            this.init_av.load() ? "Scanning models..." : "Scan for models",
            () -> this.init_V_Z(true)
         );
         autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(kit.graphics, kit.font, this.sync_List, kit.mouseX, kit.mouseY);
         y += ctrl + 2;
         String status = this.init_av.load() ? "Scanning models..." : this.read_Str;
         if (!status.isBlank()) {
            kit.drawEllipsizedText(status, x + 5, y + 1, w - 10, this.init_Z ? kit.colors.danger : kit.colors.textSecondary);
            y += AiUiKit.lineHeight() + 2;
         }

         y += 2;
      }

      y = this.init(kit, AiSettingsView.Inner_CachePool.BEHAVIOR, x, y, w);
      if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.BEHAVIOR)) {
         for (AiSettingsView.Inner_DataUtil flag : init_List) {
            if (!this.read_Z || !init_Set.contains(flag.key())) {
               y = this.init_I_cE(kit, flag, live, x, y, w, now);
            }
         }
      }

      y += 2;
      y = this.init(kit, AiSettingsView.Inner_CachePool.VISUALS, x, y, w);
      if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.VISUALS)) {
         for (AiSettingsView.Inner_DataUtil flag : read_List) {
            y = this.init_I_cE(kit, flag, live, x, y, w, now);
         }

         int opacity = this.write_I >= 0 && (this.update_Z || now < this.flush_J) ? this.write_I : AiVisualization.load(live.ghostOpacity);
         kit.drawCenteredRowText("Ghost opacity: " + opacity + "%", autismclient.gui.f.NetHelper.init(x, y, w, ctrl), x + 5, w, kit.colors.textPrimary);
         y += ctrl + 2;
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, ctrl);
         if (kit.isVisibleInClip(this.init_NetHelper)) {
            autismclient.gui.ui.widgets.SliderRenderer.init(kit.renderContext, this.init_NetHelper, opacity / 100.0, kit.isHovered(this.init_NetHelper));
            kit.addInteractiveRegion(this.init_NetHelper, () -> {
               this.update_Z = true;
               this.init_V_D(this.init_AiAssistantOverlay.read_I());
            }, null, "Drag to adjust all textured ghost blocks");
         }

         y += ctrl + 2;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, y, w, ctrl),
            "Ghost limit: " + AiVisualization.init_I(live.visualizationBlocks),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               this.init_cc.editSettingsAsync(s -> s.visualizationBlocks = s.visualizationBlocks < 1024 ? 1024 : (s.visualizationBlocks < 2048 ? 2048 : 512));
               this.init_cF.invalidate();
            },
            "512 / 1024 / 2048 blocks. Large plans keep their full bounding frame."
         );
         y += ctrl + 2;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, y, w, ctrl),
            "Visual distance: " + live.visualizationDistance + " blocks",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               this.init_cc.editSettingsAsync(s -> s.visualizationDistance = s.visualizationDistance < 96 ? 96 : (s.visualizationDistance < 160 ? 160 : 64));
               this.init_cF.invalidate();
            }
         );
         y += ctrl + 2;
         String[] legend = new String[]{
            "Green: already correct", "Blue: clear cell, ready to place", "Orange: existing block must be removed", "Pink: unloaded or unavailable"
         };
         int[] colors = new int[]{-12067963, -11093505, -20923, -885830};

         for (int i = 0; i < legend.length; i++) {
            kit.drawCenteredRowText(legend[i], autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.rowHeight()), x + 5, w - 10, colors[i]);
            y += AiUiKit.rowHeight();
         }
      }

      y += 2;
      y = this.init(kit, AiSettingsView.Inner_CachePool.MOVEMENT, x, y, w);
      if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.MOVEMENT)) {
         for (AiSettingsView.Inner_DataUtil flag : load_List) {
            y = this.init_I_cE(kit, flag, live, x, y, w, now);
         }

         kit.drawEllipsizedText("Off = legit human movement only", x + 5, y + 1, w - 10, kit.colors.textSecondary);
         y += AiUiKit.lineHeight() + 2;
      }

      y += 2;
      if (!this.read_Z) {
         y = this.init(kit, AiSettingsView.Inner_CachePool.LIMITS, x, y, w);
         if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.LIMITS)) {
            y = this.init_I_cE_2(kit, "Context", this.write_BitSetUtil, x, y, w, labelW);
            y = this.init_I_cE_2(kit, "Output", this.update_BitSetUtil, x, y, w, labelW);
            y = this.init_I_cE_2(kit, "Requests", this.sync_BitSetUtil, x, y, w, labelW);
            y = this.init_I_cE_2(kit, "Tokens", this.flush_BitSetUtil, x, y, w, labelW);
            y = this.init_I_cE_2(kit, "Minutes", this.close, x, y, w, labelW);
         }

         y += 2;
      }

      y = this.init(kit, AiSettingsView.Inner_CachePool.ADVANCED, x, y, w);
      if (!this.init_EnumSet.contains(AiSettingsView.Inner_CachePool.ADVANCED)) {
         if (!this.read_Z) {
            this.read_BitSetUtil.setPlaceholder(this.init_AiSettings_Provide.endpoint);
            kit.drawTextInput(this.read_BitSetUtil, this.init(kit, "Endpoint", x, y, w, labelW));
            y += ctrl + 2;
            this.load_BitSetUtil.setPlaceholder(live.hasKey("BRAVE") ? "Key saved - type to replace" : "Brave Search API key");
            kit.drawTextInput(this.load_BitSetUtil, this.init(kit, "Search key", x, y, w, labelW));
            y += ctrl + 2;
         }

         autismclient.gui.f.NetHelper stopRow = autismclient.gui.f.NetHelper.init(x, y, w, ctrl);
         kit.drawCenteredRowText("Stop key", stopRow, x + 5, labelW - 5, kit.colors.textPrimary);
         autismclient.gui.f.NetHelper bind = autismclient.gui.f.NetHelper.init(x + labelW, y, Math.max(34, kit.measureButtonWidth("F10") + 10), ctrl);
         autismclient.gui.ui.widgets.KeybindButtonRenderer.init(kit.renderContext, bind, this.init_I_AiSettings_J(live, now), this.write_Z, kit.isHovered(bind));
         kit.addInteractiveRegion(bind, () -> this.write_Z = !this.write_Z, null, this.write_Z ? "Press a key, Esc cancels" : "Emergency stop key");
         y += ctrl + 2;
         if (!this.read_Z) {
            y = this.init_I_cE(kit, init_cf_DataUtil, live, x, y, w, now);
            kit.drawEllipsizedText("Scripts can read and change anything on this PC", x + 5, y + 1, w - 10, kit.colors.danger);
            y += AiUiKit.lineHeight() + 2;
         }

         if (hasKey && !this.read_Z) {
            boolean armed = now - this.sync_J < 3000L;
            String label = armed ? "Click again to remove" : "Remove " + write_List.get(this.init_AiSettings_Provide.ordinal()) + " key";
            kit.drawIconLabelButton(
               autismclient.gui.f.NetHelper.init(x, y, kit.measureButtonWidth(label) + 12, ctrl),
               autismclient.util.GuiIconAtlas.DELETE,
               label,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
               this::poll
            );
            y += ctrl + 2;
         }
      }

      if (!this.read_Z) {
         y += 4;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, y, kit.measureButtonWidth("Manage memory"), ctrl),
            "Manage memory",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            this.init_AiAssistantOverlay::read_V
         );
         y += ctrl + 2;
      }

      this.read_I = y - start;
      kit.popClip();
      kit.drawScrollbar(this.init_cE_StreamUtil);
      this.init_V_cE_2(kit, footer, live);
   }

   private autismclient.gui.f.NetHelper init(AiUiKit kit, String label, int x, int y, int w, int labelW) {
      autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.toolbarHeight());
      kit.drawCenteredRowText(label, row, x + 5, labelW - 5, kit.colors.textPrimary);
      return autismclient.gui.f.NetHelper.init(x + labelW, y, Math.max(20, w - labelW), AiUiKit.toolbarHeight());
   }

   private int init_I_cE_2(AiUiKit kit, String label, autismclient.gui.ui.widgets.TextInputWidget field, int x, int y, int w, int labelW) {
      kit.drawTextInput(field, this.init(kit, label, x, y, w, labelW));
      return y + AiUiKit.toolbarHeight() + 2;
   }

   private int init(AiUiKit kit, AiSettingsView.Inner_CachePool section, int x, int y, int w) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, w, autismclient.gui.ui.UiDensityMetrics.scaledHeight(12));
      boolean folded = this.init_EnumSet.contains(section);
      autismclient.gui.ui.widgets.GroupHeaderRenderer.init(kit.renderContext, bounds, section.init_Str, folded, kit.isHovered(bounds));
      kit.addClickRegion(bounds, () -> {
         if (!this.init_EnumSet.remove(section)) {
            this.init_EnumSet.add(section);
         }

         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.sync_List);
      });
      return y + bounds.sync() + 2;
   }

   private int init_I_cE(AiUiKit kit, AiSettingsView.Inner_DataUtil flag, AiSettings live, int x, int y, int w, long now) {
      boolean value = this.init_Z_cf_DataUtil(flag, live, now);
      autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.inputHeight());
      kit.drawToggleSwitch(row, flag.label(), value, flag.key(), () -> {
         boolean next = !value;
         this.init_Map.put(flag.key(), new AiSettingsView.Inner_StreamUtil(next, System.currentTimeMillis() + 1500L));
         this.init_cc.editSettingsAsync(s -> flag.set().accept(s, next));
         this.init_cF.invalidate();
      });
      return y + row.sync() + 2;
   }

   private void init_V_D(double x) {
      this.write_I = (int)autismclient.gui.ui.widgets.SliderRenderer.init(x, this.init_NetHelper.read() + 3, this.init_NetHelper.update() - 6, 0.0, 100.0, 1.0);
      AiVisualization.init_V(this.write_I);
   }

   private boolean init_Z_cf_DataUtil(AiSettingsView.Inner_DataUtil flag, AiSettings live, long now) {
      boolean actual = flag.get().test(live);
      AiSettingsView.Inner_StreamUtil p = this.init_Map.get(flag.key());
      if (p == null) {
         return actual;
      }

      if (now <= p.until() && p.value() != actual) {
         return p.value();
      }

      this.init_Map.remove(flag.key());
      return actual;
   }

   private int init_I_AiSettings_J(AiSettings live, long now) {
      if (this.load_I != -2147483648 && (now > this.update_J || live.emergencyKey == this.load_I)) {
         this.load_I = -2147483648;
      }

      return this.load_I != -2147483648 ? this.load_I : live.emergencyKey;
   }

   private void init_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper footer, AiSettings live) {
      int ctrl = footer.sync();
      int x = footer.read();
      boolean dirty = this.sync_Z();
      int saveW = kit.measureButtonWidth("Save");
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x, footer.write(), saveW, ctrl),
         "Save",
         dirty ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         this::close,
         dirty ? null : "Nothing to save"
      );
      x += saveW + 2;
      if (!this.read_Z) {
         int testW = kit.measureButtonWidth("Test");
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, footer.write(), testW, ctrl),
            "Test",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            this::open,
            "Run a two-request round trip"
         );
         x += testW + 6;
      } else {
         x += 4;
      }

      int limit = footer.init() - 10;
      String text;
      int color;
      if (!this.update_Str.isEmpty()) {
         text = this.update_Str;
         color = kit.colors.danger;
      } else if (this.read_Z) {
         text = live.builtIn ? "Built-In: no API, no tokens" : "Save to use Built-In";
         color = live.builtIn ? kit.colors.success : kit.colors.textSecondary;
      } else {
         String var10000;
         switch (this.init_cF.providerTestState) {
            case OK:
               var10000 = "Connected";
               break;
            case FAILED:
               var10000 = "Failed: " + this.init_cF.providerTestDetail;
               break;
            case TESTING:
               var10000 = "Testing...";
               break;
            case UNKNOWN:
               var10000 = this.init_cF.hasProviderKey ? "Not tested" : "No API key";
               break;
            default:
               throw new MatchException(null, null);
         }

         text = var10000;
         int var16;
         switch (this.init_cF.providerTestState) {
            case OK:
               var16 = kit.colors.success;
               break;
            case FAILED:
               var16 = kit.colors.danger;
               break;
            case TESTING:
               var16 = kit.colors.textSecondary;
               break;
            case UNKNOWN:
               var16 = this.init_cF.hasProviderKey ? kit.colors.textSecondary : -14249;
               break;
            default:
               throw new MatchException(null, null);
         }

         color = var16;
      }

      if (limit - x >= 20) {
         kit.drawStatusDot(x + 3, footer.write() + ctrl / 2, 2.5F, color);
         autismclient.gui.f.NetHelper status = autismclient.gui.f.NetHelper.init(x, footer.write(), limit - x, ctrl);
         kit.drawCenteredRowText(text, status, x + 9, limit - x - 9, color);
         String detail = this.update_Str.isEmpty() ? this.init_cF.providerTestDetail : this.update_Str;
         if (!detail.isBlank()) {
            kit.addTooltipRegion(status, detail);
         }
      }
   }

   private void close() {
      if (this.sync_Z()) {
         AiSettings next = this.init_cc.getSettings();

         for (AiSettingsView.Inner_DataUtil flag : load_List()) {
            AiSettingsView.Inner_StreamUtil p = this.init_Map.get(flag.key());
            if (p != null) {
               flag.set().accept(next, p.value());
            }
         }

         if (this.load_I != -2147483648) {
            next.emergencyKey = this.load_I;
         }

         if (this.write_I >= 0 && (this.update_Z || System.currentTimeMillis() < this.flush_J)) {
            next.ghostOpacity = this.write_I;
         }

         try {
            next.builtIn = this.read_Z;
            if (!this.read_Z) {
               if (next.provider != this.init_AiSettings_Provide) {
                  next = next.withProvider(this.init_AiSettings_Provide);
                  next.builtIn = false;
               }

               String e = this.read_BitSetUtil.getValue().trim();
               this.flush_V();
               if (!this.flush_List.contains(this.load_Str)) {
                  throw new IllegalArgumentException("Scan and select a model returned for this connection");
               }

               next.model = this.load_Str;
               next.endpoint = AiModels.init_Str(this.init_AiSettings_Provide, e);
            }

            next.contextTokens = init_I_BitSetUtil_Str(this.write_BitSetUtil, "Context");
            next.outputTokens = init_I_BitSetUtil_Str(this.update_BitSetUtil, "Output");
            next.maxRequests = init_I_BitSetUtil_Str(this.sync_BitSetUtil, "Requests");
            next.maxTokens = init_J_BitSetUtil_Str(this.flush_BitSetUtil, "Tokens");
            next.maxMinutes = init_I_BitSetUtil_Str(this.close, "Minutes");
            if (!this.init_BitSetUtil.getValue().isBlank()) {
               next.setKey(this.init_AiSettings_Provide.name(), this.init_BitSetUtil.getValue().trim());
            }

            if (!this.load_BitSetUtil.getValue().isBlank()) {
               next.setKey("BRAVE", this.load_BitSetUtil.getValue().trim());
            }

            load_V_AiSettings(next);
            next.validate();
         } catch (RuntimeException ex) {
            this.update_Str = ex.getMessage() == null ? "Invalid settings" : ex.getMessage();
            return;
         }

         this.init_cc.saveSettings(next);
         this.load_BitSetUtil.setValue("");
         this.load_Str = next.model;
         this.load_Z = true;
         this.read_BitSetUtil.setValue(next.endpoint);
         this.load_Z = false;
         this.load_StrArr = this.init_StrArr();
         this.update_Str = "";
         this.update_V();
         if (!this.read_Z) {
            this.init_V_Z(false);
         }

         this.init_cF.invalidate();
         autismclient.util.bi.load("AI settings saved");
      }
   }

   private static List<AiSettingsView.Inner_DataUtil> load_List() {
      List<AiSettingsView.Inner_DataUtil> all = new ArrayList<>(init_List);
      all.addAll(load_List);
      all.addAll(read_List);
      all.add(init_cf_DataUtil);
      return all;
   }

   private void open() {
      if (this.sync_Z() && this.flush_Z()) {
         this.close();
      } else if (!this.init_cF.hasProviderKey && this.init_BitSetUtil.getValue().isBlank()) {
         this.update_Str = "Add an API key first";
      } else {
         this.update_Str = "";
         this.init_cc.startConnectionTest();
         this.init_cF.invalidate();
      }
   }

   private void poll() {
      long now = System.currentTimeMillis();
      if (now - this.sync_J >= 3000L) {
         this.sync_J = now;
      } else {
         this.sync_J = 0L;
         this.write_Str = this.init_cc.getSettings().keyMarker(this.init_AiSettings_Provide.name());
         this.load_Z = true;
         this.init_BitSetUtil.setValue("");
         this.load_Z = false;
         this.sync_V();
         this.init_cc.removeProviderKey(this.init_AiSettings_Provide.name());
         this.init_cF.invalidate();
         autismclient.util.bi.load("Key removed");
      }
   }

   private static void load_V_AiSettings(AiSettings s) {
      if (s.contextTokens < 16000 || s.contextTokens > 2000000) {
         throw new IllegalArgumentException("Context must be 16000 to 2000000");
      } else if (s.outputTokens < 256 || s.outputTokens > s.contextTokens / 2) {
         throw new IllegalArgumentException("Output must be 256 to half the context");
      } else if (s.maxRequests < 1) {
         throw new IllegalArgumentException("Requests must be at least 1");
      } else if (s.maxTokens < 1000L) {
         throw new IllegalArgumentException("Tokens must be at least 1000");
      } else if (s.maxMinutes < 1 || s.maxMinutes > 525600) {
         throw new IllegalArgumentException("Minutes must be 1 to 525600");
      }
   }

   private static int init_I_BitSetUtil_Str(autismclient.gui.ui.widgets.TextInputWidget field, String name) {
      long value = init_J_BitSetUtil_Str(field, name);
      if (value > 2147483647L) {
         throw new IllegalArgumentException(name + " is too large");
      } else {
         return (int)value;
      }
   }

   private static long init_J_BitSetUtil_Str(autismclient.gui.ui.widgets.TextInputWidget field, String name) {
      try {
         return Long.parseLong(field.getValue().trim());
      } catch (NumberFormatException e) {
         throw new IllegalArgumentException(name + " must be a number");
      }
   }

   private enum Inner_CachePool {
      PROVIDER("Provider"),
      BEHAVIOR("Behavior"),
      VISUALS("World visualization"),
      MOVEMENT("Movement"),
      LIMITS("Limits"),
      ADVANCED("Advanced");

      final String init_Str;

      Inner_CachePool(String title) {
         this.init_Str = title;
      }
   }

   private record Inner_DataUtil(String key, String label, Predicate<AiSettings> get, BiConsumer<AiSettings, Boolean> set) {
   }

   private record Inner_StreamUtil(boolean value, long until) {
   }
}
