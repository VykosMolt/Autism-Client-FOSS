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
   private static final List<AiSettingsView.Inner_DataUtil> MODE_FLAGS = List.of(
      new AiSettingsView.Inner_DataUtil("plan", "Plan mode (approve before acting)", s -> s.planMode, (s, v) -> s.planMode = v),
      new AiSettingsView.Inner_DataUtil("verify", "Verify outcomes (strict, slower)", s -> s.verifyOutcomes, (s, v) -> s.verifyOutcomes = v),
      new AiSettingsView.Inner_DataUtil("quick", "Use built-in when possible", s -> s.hostIntents, (s, v) -> s.hostIntents = v),
      new AiSettingsView.Inner_DataUtil("vision", "Vision (screenshots)", s -> s.vision, (s, v) -> s.vision = v),
      new AiSettingsView.Inner_DataUtil("search", "Provider web search", s -> s.nativeSearch, (s, v) -> s.nativeSearch = v),
      new AiSettingsView.Inner_DataUtil("respawn", "Auto-respawn", s -> s.autoRespawn, (s, v) -> s.autoRespawn = v),
      new AiSettingsView.Inner_DataUtil("rejoin", "Auto-rejoin", s -> s.autoRejoin, (s, v) -> s.autoRejoin = v),
      new AiSettingsView.Inner_DataUtil("input", "Stop on input", s -> s.stopOnInput, (s, v) -> s.stopOnInput = v)
   );
   private static final List<AiSettingsView.Inner_DataUtil> PERMISSION_FLAGS = List.of(
      new AiSettingsView.Inner_DataUtil("teleport", "Teleport", s -> s.allowTeleport, (s, v) -> s.allowTeleport = v),
      new AiSettingsView.Inner_DataUtil("clips", "Clips", s -> s.allowClips, (s, v) -> s.allowClips = v),
      new AiSettingsView.Inner_DataUtil("fly", "Flight", s -> s.allowFly, (s, v) -> s.allowFly = v),
      new AiSettingsView.Inner_DataUtil("aim", "Silent aim", s -> s.aiSilentAim, (s, v) -> s.aiSilentAim = v)
   );
   private static final List<AiSettingsView.Inner_DataUtil> VISUAL_FLAGS = List.of(
      new AiSettingsView.Inner_DataUtil("paths", "Calculated paths", s -> s.visualizePaths, (s, v) -> s.visualizePaths = v),
      new AiSettingsView.Inner_DataUtil("targets", "Mining outlines and active placement", s -> s.visualizeTargets, (s, v) -> s.visualizeTargets = v),
      new AiSettingsView.Inner_DataUtil("ghosts", "Textured schematic previews", s -> s.visualizeGhosts, (s, v) -> s.visualizeGhosts = v)
   );
   private static final AiSettingsView.Inner_DataUtil SCRIPTS_FLAG = new AiSettingsView.Inner_DataUtil(
      "scripts", "Unrestricted scripts", s -> s.unrestrictedScripts, (s, v) -> s.unrestrictedScripts = v
   );
   private static final Set<String> init_Set = Set.of("plan", "verify", "quick", "vision", "search");
   private static final List<String> PROVIDER_NAMES = List.of("OpenAI", "DashScope", "Moonshot", "DeepSeek", "Gemini", "Anthropic");
   private static final String BUILT_IN = "Built-In";
   private static final List<String> PROVIDER_OPTIONS = Stream.concat(Stream.of("Built-In"), PROVIDER_NAMES.stream()).toList();
   private static final String[] FIELD_LABELS = new String[]{
      "Provider", "API key", "Model", "Context", "Output", "Requests", "Tokens", "Minutes", "Endpoint", "Search key", "Stop key"
   };
   private static final long init_J = 1500L;
   private static final long load_J = 3000L;
   private static final int init_I = 2;
   private final AiService service;
   private final AiViewState view;
   private final AiAssistantOverlay host;
   final autismclient.gui.ui.widgets.TextInputWidget apiKeyField = secretField();
   final autismclient.gui.ui.widgets.TextInputWidget searchKeyField = secretField();
   final autismclient.gui.ui.widgets.TextInputWidget endpointField = textField(512);
   final autismclient.gui.ui.widgets.TextInputWidget contextField = digitField();
   final autismclient.gui.ui.widgets.TextInputWidget outputField = digitField();
   final autismclient.gui.ui.widgets.TextInputWidget requestsField = digitField();
   final autismclient.gui.ui.widgets.TextInputWidget tokensField = digitField();
   final autismclient.gui.ui.widgets.TextInputWidget close = digitField();
   private final autismclient.gui.ui.widgets.DropdownWidget providerDropdown;
   private final autismclient.gui.ui.widgets.DropdownWidget modelDropdown;
   private final List<autismclient.gui.ui.widgets.DropdownWidget> dropdowns;
   private final AiModelCatalog modelCatalog = new AiModelCatalog();
   private String selectedModel = "";
   private List<String> models = List.of();
   private String scanMessage = "";
   private boolean init_Z;
   private boolean load_Z;
   private long read_J;
   private long write_J;
   private String write_Str = "";
   private final EnumSet<AiSettingsView.Inner_CachePool> collapsedSections = EnumSet.of(AiSettingsView.Inner_CachePool.LIMITS, AiSettingsView.Inner_CachePool.ADVANCED);
   private final Map<String, AiSettingsView.Inner_StreamUtil> pendingFlags = new HashMap<>();
   private final AiUiKit.ScrollPane scroll = new AiUiKit.ScrollPane(false);
   private AiSettings.Provider provider = AiSettings.Provider.OPENAI;
   private boolean builtIn;
   private String[] savedSnapshot = new String[0];
   private String errorMessage = "";
   private boolean capturingKey;
   private int keyCode = -2147483648;
   private long update_J;
   private long sync_J;
   private int read_I;
   private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private boolean update_Z;
   private int ghostLimit = -1;
   private long flush_J;

   AiSettingsView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.service = service;
      this.view = view;
      this.host = host;
      this.providerDropdown = new autismclient.gui.ui.widgets.DropdownWidget(0, 0, 10, 10, PROVIDER_OPTIONS, 0, index -> {
         if (index == 0) {
            this.read_V();
         } else {
            this.selectProvider(AiSettings.Provider.values()[index - 1]);
         }
      });
      this.modelDropdown = new autismclient.gui.ui.widgets.DropdownWidget(0, 0, 10, 10, List.of(), 0, this::selectModel);
      this.modelDropdown.active = false;
      this.dropdowns = List.of(this.providerDropdown, this.modelDropdown);
      this.apiKeyField.setOnChange(text -> this.write_V());
      this.endpointField.setOnChange(text -> this.sync_V());

      for (autismclient.gui.ui.widgets.TextInputWidget field : List.of(
         this.apiKeyField,
         this.searchKeyField,
         this.endpointField,
         this.contextField,
         this.outputField,
         this.requestsField,
         this.tokensField,
         this.close
      )) {
         field.setOnSubmit(text -> this.close());
      }
   }

   List<autismclient.gui.ui.widgets.TextInputWidget> inputs() {
      return List.of(
         this.apiKeyField,
         this.searchKeyField,
         this.endpointField,
         this.contextField,
         this.outputField,
         this.requestsField,
         this.tokensField,
         this.close
      );
   }

   void init_V() {
      this.collapsedSections.addAll(EnumSet.allOf(AiSettingsView.Inner_CachePool.class));
      this.collapsedSections.remove(AiSettingsView.Inner_CachePool.VISUALS);
      this.scroll.scrollToTop();
   }

   private static autismclient.gui.ui.widgets.TextInputWidget textField(int max) {
      return new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(max);
   }

   private static autismclient.gui.ui.widgets.TextInputWidget secretField() {
      autismclient.gui.ui.widgets.TextInputWidget field = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(2048);
      field.setDisplayTextProvider(text -> Component.literal("*".repeat(Math.min(64, text.length()))));
      return field;
   }

   private static autismclient.gui.ui.widgets.TextInputWidget digitField() {
      return new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(12).setInputFilter(v -> v.chars().allMatch(Character::isDigit));
   }

   void loadSettings(AiSettings s) {
      this.load_Z = true;
      this.modelCatalog.reset();
      this.write_Str = "";
      this.provider = s.provider;
      this.builtIn = s.builtIn;
      this.providerDropdown.setSelectedIndex(this.providerIndex());
      this.apiKeyField.setValue("");
      this.searchKeyField.setValue("");
      this.selectedModel = s.model == null ? "" : s.model.trim();
      this.endpointField.setValue(s.endpoint);
      this.contextField.setValue(Integer.toString(s.contextTokens));
      this.outputField.setValue(Integer.toString(s.outputTokens));
      this.requestsField.setValue(Integer.toString(s.maxRequests));
      this.tokensField.setValue(Long.toString(s.maxTokens));
      this.close.setValue(Integer.toString(s.maxMinutes));
      this.savedSnapshot = this.formSnapshot();
      this.errorMessage = "";
      this.capturingKey = false;
      this.sync_J = 0L;
      autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
      this.load_Z = false;
      this.refreshModels();
      if (!this.builtIn) {
         this.scanModels(false);
      }
   }

   private int providerIndex() {
      return this.builtIn ? 0 : this.provider.ordinal() + 1;
   }

   private String[] formSnapshot() {
      return new String[]{
         this.builtIn ? "Built-In" : this.provider.name(),
         this.apiKeyField.getValue(),
         this.searchKeyField.getValue(),
         this.selectedModel,
         this.endpointField.getValue().trim(),
         this.contextField.getValue(),
         this.outputField.getValue(),
         this.requestsField.getValue(),
         this.tokensField.getValue(),
         this.close.getValue()
      };
   }

   private boolean isDirty() {
      return !Arrays.equals(this.savedSnapshot, this.formSnapshot());
   }

   private boolean differsFromLive() {
      AiSettings live = this.view.settings;
      return this.builtIn != live.builtIn
         || this.provider != live.provider
         || !this.apiKeyField.getValue().isBlank()
         || !this.selectedModel.equals(live.model)
         || !this.endpointField.getValue().trim().equals(live.endpoint);
   }

   private void read_V() {
      if (!this.builtIn) {
         this.builtIn = true;
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
         this.errorMessage = "";
      }
   }

   private void selectProvider(AiSettings.Provider next) {
      if (this.builtIn) {
         this.builtIn = false;
         this.errorMessage = "";
         if (next == this.provider) {
            this.scanModels(false);
            return;
         }
      }

      if (next != this.provider) {
         this.load_Z = true;
         this.provider = next;
         this.selectedModel = "";
         this.outputField.setValue(Integer.toString(AiSettings.defaultOutputTokens(next)));
         if (AiSettings.shouldAutofillEndpoint(this.endpointField.getValue())) {
            this.endpointField.setValue(next.endpoint);
         }

         this.apiKeyField.setValue("");
         this.write_Str = "";
         this.sync_J = 0L;
         this.load_Z = false;
         this.sync_V();
         this.scanModels(false);
      }
   }

   private void write_V() {
      if (!this.load_Z) {
         String typed = this.apiKeyField.getValue().trim();
         if (!typed.isEmpty() && AiKeyResolver.isUnambiguous(typed)) {
            this.load_V_AiSettings_Provide(AiKeyResolver.providersFor(typed).getFirst());
         }

         this.sync_V();
      }
   }

   private void load_V_AiSettings_Provide(AiSettings.Provider next) {
      if (next != this.provider) {
         this.load_Z = true;
         String stockOutput = Integer.toString(AiSettings.defaultOutputTokens(this.provider));
         this.provider = next;
         this.builtIn = false;
         this.providerDropdown.setSelectedIndex(this.providerIndex());
         if (this.outputField.getValue().trim().equals(stockOutput)) {
            this.outputField.setValue(Integer.toString(AiSettings.defaultOutputTokens(next)));
         }

         if (AiSettings.shouldAutofillEndpoint(this.endpointField.getValue())) {
            this.endpointField.setValue(next.endpoint);
         }

         this.selectedModel = "";
         this.load_Z = false;
      }
   }

   private static String hostOf(String endpoint) {
      try {
         return URI.create(endpoint).getHost();
      } catch (RuntimeException e) {
         return endpoint;
      }
   }

   private void selectModel(int index) {
      this.flush_V();
      if (index >= 0 && index < this.models.size()) {
         this.selectedModel = this.models.get(index);
         this.refreshModels();
      }
   }

   private void refreshModels() {
      this.models = this.modelCatalog.models();
      this.modelDropdown.setOptions(this.models);
      this.modelDropdown.setSelectedIndex(Math.max(0, this.models.indexOf(this.selectedModel)));
      this.modelDropdown.active = !this.modelCatalog.isScanning() && !this.models.isEmpty();
      this.modelDropdown
         .setLabelOverride(
            this.models.contains(this.selectedModel)
               ? ""
               : (this.modelCatalog.isScanning() ? "Scanning models..." : (this.models.isEmpty() ? "Scan for models" : "Select a model"))
         );
   }

   private void sync_V() {
      if (!this.load_Z) {
         this.modelCatalog.reset();
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
         this.scanMessage = "Scan to load models for this connection";
         this.init_Z = false;
         this.read_J = System.currentTimeMillis() + 700L;
         this.refreshModels();
      }
   }

   private String effectiveKey() {
      if (!this.apiKeyField.getValue().isBlank()) {
         return this.apiKeyField.getValue().trim();
      }

      AiSettings saved = this.service.getSettings();
      return !this.write_Str.isEmpty() && this.write_Str.equals(saved.keyMarker(this.provider.name()))
         ? ""
         : saved.key(this.provider.name());
   }

   private void flush_V() {
      try {
         if (this.modelCatalog.retarget(AiModelCatalog.CatalogKey.of(this.provider, this.endpointField.getValue(), this.effectiveKey()))) {
            autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
            this.scanMessage = "Scan to load models for this connection";
            this.init_Z = false;
            this.read_J = System.currentTimeMillis() + 700L;
            this.refreshModels();
         }
      } catch (RuntimeException ex) {
         this.modelCatalog.reset();
         this.scanMessage = "Check the endpoint and saved credential, then scan again";
         this.init_Z = true;
         this.refreshModels();
      }
   }

   private void scanModels(boolean force) {
      this.read_J = 0L;
      AiSettings.Provider p = this.provider;

      String k;
      AiModelCatalog.CatalogKey identity;
      try {
         k = this.effectiveKey();
         identity = AiModelCatalog.CatalogKey.of(p, this.endpointField.getValue(), k);
      } catch (RuntimeException ex) {
         this.modelCatalog.reset();
         this.scanMessage = "Check the endpoint and saved credential, then scan again";
         this.init_Z = true;
         this.refreshModels();
         return;
      }

      this.modelCatalog.retarget(identity);
      if (force || !this.modelCatalog.init_Z()) {
         long generation = this.modelCatalog.beginFetch(identity);
         this.scanMessage = "";
         this.init_Z = false;
         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
         this.refreshModels();
         if (k.isBlank()) {
            this.modelCatalog.init(generation, identity, new AiModels.Inner_CachePool(List.of(), "Add an API key to scan for models"));
            this.scanMessage = this.modelCatalog.message();
            this.init_Z = true;
            this.refreshModels();
         } else {
            boolean typed = !this.apiKeyField.getValue().isBlank();
            String endpointText = this.endpointField.getValue();
            Thread worker = new Thread(
               () -> {
                  AiKeyResolver.Inner_DataUtil resolution = AiKeyResolver.init(k, p, endpointText, typed, () -> {
                     if (!this.modelCatalog.isCurrent(generation)) {
                        throw new CancellationException("Model scan superseded");
                     }
                  });
                  AiModels.Inner_CachePool result = new AiModels.Inner_CachePool(resolution.models(), resolution.error(), resolution.endpoint(), resolution.provider());
                  Minecraft.getInstance()
                     .execute(
                        () -> {
                           try {
                              if (!identity.equals(AiModelCatalog.CatalogKey.of(this.provider, this.endpointField.getValue(), this.effectiveKey()))) {
                                 return;
                              }
                           } catch (RuntimeException ignored) {
                              return;
                           }

                           if (this.modelCatalog.init(generation, identity, result)) {
                              this.init_Z = !result.ok();
                              if (result.ok()) {
                                 boolean detected = resolution.provider() != this.provider;
                                 if (detected) {
                                    this.load_V_AiSettings_Provide(resolution.provider());
                                 }

                                 boolean moved = !identity.endpoint().equals(resolution.endpoint());
                                 if (moved || detected) {
                                    this.load_Z = true;
                                    this.endpointField.setValue(resolution.endpoint());
                                    this.load_Z = false;
                                 }

                                 if (!result.models().contains(this.selectedModel)) {
                                    this.selectedModel = resolution.model();
                                 }

                                 String name = AiKeyResolver.init(resolution.provider());
                                 this.scanMessage = result.models().isEmpty()
                                    ? name + " returned no chat models for this key"
                                    : (detected ? "Detected " + name + " key: " : name + ": ")
                                       + result.models().size()
                                       + " models"
                                       + (this.selectedModel.isEmpty() ? "" : ", " + this.selectedModel + " selected")
                                       + (moved ? " via " + hostOf(resolution.endpoint()) : "");
                              } else {
                                 this.scanMessage = "Scan failed: " + result.error();
                              }

                              this.refreshModels();
                              this.view.invalidate();
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

   boolean hasOpenDropdown() {
      return autismclient.gui.ui.widgets.DropdownWidget.hasOpenMenu(this.dropdowns);
   }

   boolean isOverPopup(double x, double y) {
      return autismclient.gui.ui.widgets.DropdownWidget.isPointInAnyPopup(this.dropdowns, x, y);
   }

   boolean clickPopup(double x, double y, int button) {
      this.flush_V();
      return this.hasOpenDropdown() && autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(this.dropdowns, x, y, button);
   }

   boolean scrollPopup(double x, double y, double amount) {
      if (autismclient.gui.ui.widgets.DropdownWidget.handleMouseScroll(this.dropdowns, x, y, amount)) {
         return true;
      }

      autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
      return false;
   }

   boolean load_Z() {
      boolean used = this.update_Z;
      if (this.update_Z) {
         this.update_Z = false;
         int value = this.ghostLimit;
         this.service.editSettingsAsync(s -> s.ghostOpacity = value);
         this.flush_J = System.currentTimeMillis() + 1500L;
         this.view.invalidate();
      }

      return autismclient.gui.ui.widgets.DropdownWidget.handleMouseRelease(this.dropdowns) || used;
   }

   boolean load(double x, double y, int button) {
      if (this.update_Z) {
         this.dragGhostSlider(x);
         return true;
      } else {
         return this.hasOpenDropdown() && autismclient.gui.ui.widgets.DropdownWidget.handleMouseDrag(this.dropdowns, x, y, button);
      }
   }

   boolean closePopups() {
      boolean used = this.load_Z();
      return autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns) || used;
   }

   boolean isCapturingKey() {
      return this.capturingKey;
   }

   void cancelKeyCapture() {
      this.capturingKey = false;
   }

   boolean consumeKeyCapture() {
      if (!this.capturingKey) {
         return false;
      }

      this.capturingKey = false;
      return true;
   }

   boolean captureKey(int keyCode) {
      if (!this.capturingKey) {
         return false;
      } else {
         this.capturingKey = false;
         if (keyCode != 256 && keyCode != -1) {
            this.keyCode = keyCode;
            this.update_J = System.currentTimeMillis() + 1500L;
            this.service.editSettingsAsync(s -> s.emergencyKey = keyCode);
            this.view.invalidate();
            return true;
         } else {
            return true;
         }
      }
   }

   void renderPopups(AiUiKit kit) {
      autismclient.gui.ui.widgets.DropdownWidget.renderOpenPopup(kit.graphics, kit.font, this.dropdowns, kit.rawMouseX, kit.rawMouseY);
   }

   void render(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (now >= this.write_J) {
         this.write_J = now + 1000L;
         this.flush_V();
      }

      if (this.read_J > 0L && now >= this.read_J && !this.builtIn) {
         this.scanModels(false);
      }

      int ctrl = AiUiKit.toolbarHeight();
      autismclient.gui.f.NetHelper footer = autismclient.gui.f.NetHelper.init(area.read(), area.load() - ctrl, area.update(), ctrl);
      autismclient.gui.f.NetHelper list = autismclient.gui.f.NetHelper.init(
         area.read(), area.write(), area.update(), Math.max(0, footer.write() - 2 - area.write())
      );
      this.scroll.layout(list, this.read_I);
      kit.registerScrollPane(this.scroll);
      kit.pushClip(list);
      int x = list.read();
      int w = list.update() - 6 - 3;
      int labelW = 0;

      for (String label : FIELD_LABELS) {
         labelW = Math.max(labelW, kit.measureTextWidth(label));
      }

      labelW = Math.min(labelW + 10, w * 2 / 5);
      int y = list.write() - this.scroll.offset;
      int start = y;
      AiSettings live = this.view.settings;
      boolean hasKey = this.service.hasProviderKey(this.provider);
      y = this.init(kit, AiSettingsView.Inner_CachePool.PROVIDER, x, y, w);
      if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.PROVIDER)) {
         autismclient.gui.f.NetHelper field = this.init(kit, "Provider", x, y, w, labelW);
         this.providerDropdown.setBounds(field.read(), field.write(), field.update(), field.sync());
         if (kit.isVisibleInClip(field)) {
            kit.addClickRegion(
               field,
               () -> autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(
                  this.dropdowns, this.host.contextMenuX(), this.host.contextMenuY(), 0
               )
            );
         }

         y += ctrl + 2;
         if (this.builtIn) {
            kit.drawEllipsizedText("Runs in the client. Instant, free, no API.", x + 5, y + 1, w - 10, kit.colors.textSecondary);
            y += AiUiKit.lineHeight();
            autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(kit.graphics, kit.font, List.of(this.providerDropdown), kit.mouseX, kit.mouseY);
            y += 4;
         }
      }

      if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.PROVIDER) && !this.builtIn) {
         this.apiKeyField.setPlaceholder(hasKey ? "Key saved - type to replace" : "Paste your API key");
         kit.drawTextInput(this.apiKeyField, this.init(kit, "API key", x, y, w, labelW));
         y += ctrl + 2;
         autismclient.gui.f.NetHelper modelBounds = this.init(kit, "Model", x, y, w - (ctrl + 2), labelW);
         this.modelDropdown.setBounds(modelBounds.read(), modelBounds.write(), modelBounds.update(), modelBounds.sync());
         if (kit.isVisibleInClip(modelBounds)) {
            kit.addInteractiveRegion(
               modelBounds,
               () -> autismclient.gui.ui.widgets.DropdownWidget.handleMouseClick(
                  this.dropdowns, this.host.contextMenuX(), this.host.contextMenuY(), 0
               ),
               null,
               this.models.isEmpty() ? "Scan to list models returned for this key and endpoint" : this.models.size() + " models to choose from"
            );
         }

         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(modelBounds.init() + 2, y, ctrl, ctrl),
            autismclient.util.GuiIconAtlas.REFRESH,
            this.modelCatalog.isScanning(),
            false,
            this.modelCatalog.isScanning() ? "Scanning models..." : "Scan for models",
            () -> this.scanModels(true)
         );
         autismclient.gui.ui.widgets.DropdownWidget.renderTriggers(kit.graphics, kit.font, this.dropdowns, kit.mouseX, kit.mouseY);
         y += ctrl + 2;
         String status = this.modelCatalog.isScanning() ? "Scanning models..." : this.scanMessage;
         if (!status.isBlank()) {
            kit.drawEllipsizedText(status, x + 5, y + 1, w - 10, this.init_Z ? kit.colors.danger : kit.colors.textSecondary);
            y += AiUiKit.lineHeight() + 2;
         }

         y += 2;
      }

      y = this.init(kit, AiSettingsView.Inner_CachePool.BEHAVIOR, x, y, w);
      if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.BEHAVIOR)) {
         for (AiSettingsView.Inner_DataUtil flag : MODE_FLAGS) {
            if (!this.builtIn || !init_Set.contains(flag.key())) {
               y = this.drawFlagRow(kit, flag, live, x, y, w, now);
            }
         }
      }

      y += 2;
      y = this.init(kit, AiSettingsView.Inner_CachePool.VISUALS, x, y, w);
      if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.VISUALS)) {
         for (AiSettingsView.Inner_DataUtil flag : VISUAL_FLAGS) {
            y = this.drawFlagRow(kit, flag, live, x, y, w, now);
         }

         int opacity = this.ghostLimit >= 0 && (this.update_Z || now < this.flush_J) ? this.ghostLimit : AiVisualization.clampPercent(live.ghostOpacity);
         kit.drawCenteredRowText("Ghost opacity: " + opacity + "%", autismclient.gui.f.NetHelper.init(x, y, w, ctrl), x + 5, w, kit.colors.textPrimary);
         y += ctrl + 2;
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, w, ctrl);
         if (kit.isVisibleInClip(this.init_NetHelper)) {
            autismclient.gui.ui.widgets.SliderRenderer.init(kit.renderContext, this.init_NetHelper, opacity / 100.0, kit.isHovered(this.init_NetHelper));
            kit.addInteractiveRegion(this.init_NetHelper, () -> {
               this.update_Z = true;
               this.dragGhostSlider(this.host.contextMenuX());
            }, null, "Drag to adjust all textured ghost blocks");
         }

         y += ctrl + 2;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, y, w, ctrl),
            "Ghost limit: " + AiVisualization.clampGhostLimit(live.visualizationBlocks),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               this.service.editSettingsAsync(s -> s.visualizationBlocks = s.visualizationBlocks < 1024 ? 1024 : (s.visualizationBlocks < 2048 ? 2048 : 512));
               this.view.invalidate();
            },
            "512 / 1024 / 2048 blocks. Large plans keep their full bounding frame."
         );
         y += ctrl + 2;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, y, w, ctrl),
            "Visual distance: " + live.visualizationDistance + " blocks",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               this.service.editSettingsAsync(s -> s.visualizationDistance = s.visualizationDistance < 96 ? 96 : (s.visualizationDistance < 160 ? 160 : 64));
               this.view.invalidate();
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
      if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.MOVEMENT)) {
         for (AiSettingsView.Inner_DataUtil flag : PERMISSION_FLAGS) {
            y = this.drawFlagRow(kit, flag, live, x, y, w, now);
         }

         kit.drawEllipsizedText("Off = legit human movement only", x + 5, y + 1, w - 10, kit.colors.textSecondary);
         y += AiUiKit.lineHeight() + 2;
      }

      y += 2;
      if (!this.builtIn) {
         y = this.init(kit, AiSettingsView.Inner_CachePool.LIMITS, x, y, w);
         if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.LIMITS)) {
            y = this.drawFieldRow(kit, "Context", this.contextField, x, y, w, labelW);
            y = this.drawFieldRow(kit, "Output", this.outputField, x, y, w, labelW);
            y = this.drawFieldRow(kit, "Requests", this.requestsField, x, y, w, labelW);
            y = this.drawFieldRow(kit, "Tokens", this.tokensField, x, y, w, labelW);
            y = this.drawFieldRow(kit, "Minutes", this.close, x, y, w, labelW);
         }

         y += 2;
      }

      y = this.init(kit, AiSettingsView.Inner_CachePool.ADVANCED, x, y, w);
      if (!this.collapsedSections.contains(AiSettingsView.Inner_CachePool.ADVANCED)) {
         if (!this.builtIn) {
            this.endpointField.setPlaceholder(this.provider.endpoint);
            kit.drawTextInput(this.endpointField, this.init(kit, "Endpoint", x, y, w, labelW));
            y += ctrl + 2;
            this.searchKeyField.setPlaceholder(live.hasKey("BRAVE") ? "Key saved - type to replace" : "Brave Search API key");
            kit.drawTextInput(this.searchKeyField, this.init(kit, "Search key", x, y, w, labelW));
            y += ctrl + 2;
         }

         autismclient.gui.f.NetHelper stopRow = autismclient.gui.f.NetHelper.init(x, y, w, ctrl);
         kit.drawCenteredRowText("Stop key", stopRow, x + 5, labelW - 5, kit.colors.textPrimary);
         autismclient.gui.f.NetHelper bind = autismclient.gui.f.NetHelper.init(x + labelW, y, Math.max(34, kit.measureButtonWidth("F10") + 10), ctrl);
         autismclient.gui.ui.widgets.KeybindButtonRenderer.init(kit.renderContext, bind, this.init_I_AiSettings_J(live, now), this.capturingKey, kit.isHovered(bind));
         kit.addInteractiveRegion(bind, () -> this.capturingKey = !this.capturingKey, null, this.capturingKey ? "Press a key, Esc cancels" : "Emergency stop key");
         y += ctrl + 2;
         if (!this.builtIn) {
            y = this.drawFlagRow(kit, SCRIPTS_FLAG, live, x, y, w, now);
            kit.drawEllipsizedText("Scripts can read and change anything on this PC", x + 5, y + 1, w - 10, kit.colors.danger);
            y += AiUiKit.lineHeight() + 2;
         }

         if (hasKey && !this.builtIn) {
            boolean armed = now - this.sync_J < 3000L;
            String label = armed ? "Click again to remove" : "Remove " + PROVIDER_NAMES.get(this.provider.ordinal()) + " key";
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

      if (!this.builtIn) {
         y += 4;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, y, kit.measureButtonWidth("Manage memory"), ctrl),
            "Manage memory",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            this.host::showMemory
         );
         y += ctrl + 2;
      }

      this.read_I = y - start;
      kit.popClip();
      kit.drawScrollbar(this.scroll);
      this.drawFooter(kit, footer, live);
   }

   private autismclient.gui.f.NetHelper init(AiUiKit kit, String label, int x, int y, int w, int labelW) {
      autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.toolbarHeight());
      kit.drawCenteredRowText(label, row, x + 5, labelW - 5, kit.colors.textPrimary);
      return autismclient.gui.f.NetHelper.init(x + labelW, y, Math.max(20, w - labelW), AiUiKit.toolbarHeight());
   }

   private int drawFieldRow(AiUiKit kit, String label, autismclient.gui.ui.widgets.TextInputWidget field, int x, int y, int w, int labelW) {
      kit.drawTextInput(field, this.init(kit, label, x, y, w, labelW));
      return y + AiUiKit.toolbarHeight() + 2;
   }

   private int init(AiUiKit kit, AiSettingsView.Inner_CachePool section, int x, int y, int w) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, w, autismclient.gui.ui.UiDensityMetrics.scaledHeight(12));
      boolean folded = this.collapsedSections.contains(section);
      autismclient.gui.ui.widgets.GroupHeaderRenderer.init(kit.renderContext, bounds, section.init_Str, folded, kit.isHovered(bounds));
      kit.addClickRegion(bounds, () -> {
         if (!this.collapsedSections.remove(section)) {
            this.collapsedSections.add(section);
         }

         autismclient.gui.ui.widgets.DropdownWidget.closeAll(this.dropdowns);
      });
      return y + bounds.sync() + 2;
   }

   private int drawFlagRow(AiUiKit kit, AiSettingsView.Inner_DataUtil flag, AiSettings live, int x, int y, int w, long now) {
      boolean value = this.flagValue(flag, live, now);
      autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.inputHeight());
      kit.drawToggleSwitch(row, flag.label(), value, flag.key(), () -> {
         boolean next = !value;
         this.pendingFlags.put(flag.key(), new AiSettingsView.Inner_StreamUtil(next, System.currentTimeMillis() + 1500L));
         this.service.editSettingsAsync(s -> flag.set().accept(s, next));
         this.view.invalidate();
      });
      return y + row.sync() + 2;
   }

   private void dragGhostSlider(double x) {
      this.ghostLimit = (int)autismclient.gui.ui.widgets.SliderRenderer.init(x, this.init_NetHelper.read() + 3, this.init_NetHelper.update() - 6, 0.0, 100.0, 1.0);
      AiVisualization.setGhostLimit(this.ghostLimit);
   }

   private boolean flagValue(AiSettingsView.Inner_DataUtil flag, AiSettings live, long now) {
      boolean actual = flag.get().test(live);
      AiSettingsView.Inner_StreamUtil p = this.pendingFlags.get(flag.key());
      if (p == null) {
         return actual;
      }

      if (now <= p.until() && p.value() != actual) {
         return p.value();
      }

      this.pendingFlags.remove(flag.key());
      return actual;
   }

   private int init_I_AiSettings_J(AiSettings live, long now) {
      if (this.keyCode != -2147483648 && (now > this.update_J || live.emergencyKey == this.keyCode)) {
         this.keyCode = -2147483648;
      }

      return this.keyCode != -2147483648 ? this.keyCode : live.emergencyKey;
   }

   private void drawFooter(AiUiKit kit, autismclient.gui.f.NetHelper footer, AiSettings live) {
      int ctrl = footer.sync();
      int x = footer.read();
      boolean dirty = this.isDirty();
      int saveW = kit.measureButtonWidth("Save");
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x, footer.write(), saveW, ctrl),
         "Save",
         dirty ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         this::close,
         dirty ? null : "Nothing to save"
      );
      x += saveW + 2;
      if (!this.builtIn) {
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
      if (!this.errorMessage.isEmpty()) {
         text = this.errorMessage;
         color = kit.colors.danger;
      } else if (this.builtIn) {
         text = live.builtIn ? "Built-In: no API, no tokens" : "Save to use Built-In";
         color = live.builtIn ? kit.colors.success : kit.colors.textSecondary;
      } else {
         String var10000;
         switch (this.view.providerTestState) {
            case OK:
               var10000 = "Connected";
               break;
            case FAILED:
               var10000 = "Failed: " + this.view.providerTestDetail;
               break;
            case TESTING:
               var10000 = "Testing...";
               break;
            case UNKNOWN:
               var10000 = this.view.hasProviderKey ? "Not tested" : "No API key";
               break;
            default:
               throw new MatchException(null, null);
         }

         text = var10000;
         int var16;
         switch (this.view.providerTestState) {
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
               var16 = this.view.hasProviderKey ? kit.colors.textSecondary : -14249;
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
         String detail = this.errorMessage.isEmpty() ? this.view.providerTestDetail : this.errorMessage;
         if (!detail.isBlank()) {
            kit.addTooltipRegion(status, detail);
         }
      }
   }

   private void close() {
      if (this.isDirty()) {
         AiSettings next = this.service.getSettings();

         for (AiSettingsView.Inner_DataUtil flag : allFlags()) {
            AiSettingsView.Inner_StreamUtil p = this.pendingFlags.get(flag.key());
            if (p != null) {
               flag.set().accept(next, p.value());
            }
         }

         if (this.keyCode != -2147483648) {
            next.emergencyKey = this.keyCode;
         }

         if (this.ghostLimit >= 0 && (this.update_Z || System.currentTimeMillis() < this.flush_J)) {
            next.ghostOpacity = this.ghostLimit;
         }

         try {
            next.builtIn = this.builtIn;
            if (!this.builtIn) {
               if (next.provider != this.provider) {
                  next = next.withProvider(this.provider);
                  next.builtIn = false;
               }

               String e = this.endpointField.getValue().trim();
               this.flush_V();
               if (!this.models.contains(this.selectedModel)) {
                  throw new IllegalArgumentException("Scan and select a model returned for this connection");
               }

               next.model = this.selectedModel;
               next.endpoint = AiModels.baseUrl(this.provider, e);
            }

            next.contextTokens = parseIntField(this.contextField, "Context");
            next.outputTokens = parseIntField(this.outputField, "Output");
            next.maxRequests = parseIntField(this.requestsField, "Requests");
            next.maxTokens = parseNumber(this.tokensField, "Tokens");
            next.maxMinutes = parseIntField(this.close, "Minutes");
            if (!this.apiKeyField.getValue().isBlank()) {
               next.setKey(this.provider.name(), this.apiKeyField.getValue().trim());
            }

            if (!this.searchKeyField.getValue().isBlank()) {
               next.setKey("BRAVE", this.searchKeyField.getValue().trim());
            }

            validate(next);
            next.validate();
         } catch (RuntimeException ex) {
            this.errorMessage = ex.getMessage() == null ? "Invalid settings" : ex.getMessage();
            return;
         }

         this.service.saveSettings(next);
         this.searchKeyField.setValue("");
         this.selectedModel = next.model;
         this.load_Z = true;
         this.endpointField.setValue(next.endpoint);
         this.load_Z = false;
         this.savedSnapshot = this.formSnapshot();
         this.errorMessage = "";
         this.refreshModels();
         if (!this.builtIn) {
            this.scanModels(false);
         }

         this.view.invalidate();
         autismclient.util.bi.load("AI settings saved");
      }
   }

   private static List<AiSettingsView.Inner_DataUtil> allFlags() {
      List<AiSettingsView.Inner_DataUtil> all = new ArrayList<>(MODE_FLAGS);
      all.addAll(PERMISSION_FLAGS);
      all.addAll(VISUAL_FLAGS);
      all.add(SCRIPTS_FLAG);
      return all;
   }

   private void open() {
      if (this.isDirty() && this.differsFromLive()) {
         this.close();
      } else if (!this.view.hasProviderKey && this.apiKeyField.getValue().isBlank()) {
         this.errorMessage = "Add an API key first";
      } else {
         this.errorMessage = "";
         this.service.startConnectionTest();
         this.view.invalidate();
      }
   }

   private void poll() {
      long now = System.currentTimeMillis();
      if (now - this.sync_J >= 3000L) {
         this.sync_J = now;
      } else {
         this.sync_J = 0L;
         this.write_Str = this.service.getSettings().keyMarker(this.provider.name());
         this.load_Z = true;
         this.apiKeyField.setValue("");
         this.load_Z = false;
         this.sync_V();
         this.service.removeProviderKey(this.provider.name());
         this.view.invalidate();
         autismclient.util.bi.load("Key removed");
      }
   }

   private static void validate(AiSettings s) {
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

   private static int parseIntField(autismclient.gui.ui.widgets.TextInputWidget field, String name) {
      long value = parseNumber(field, name);
      if (value > 2147483647L) {
         throw new IllegalArgumentException(name + " is too large");
      } else {
         return (int)value;
      }
   }

   private static long parseNumber(autismclient.gui.ui.widgets.TextInputWidget field, String name) {
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
