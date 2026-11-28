package autismclient.ai;

import autismclient.gui.ui.widgets.SearchFilterList;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;

final class AiActionMenu {
   private static final int init_I = 2;
   private static final int load_I = 3;
   private static final long init_J = 3000000L;
   private static final long load_J = 2000L;
   private final AiService service;
   private final AiViewState view;
   private final AiAssistantOverlay host;
   private final Runnable onRun;
   private final AiIntents.NameLookups waypointResolver = AiService.createWaypointResolver();
   final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(64);
   final autismclient.gui.ui.widgets.TextInputWidget countInput = new autismclient.gui.ui.widgets.TextInputWidget()
      .setMaxLength(4)
      .setInputFilter(s -> s.chars().allMatch(Character::isDigit));
   private final AiUiKit.ScrollPane listScroll = new AiUiKit.ScrollPane(false);
   private final SearchFilterList<AiActionMenu.TargetOption> searchList = new SearchFilterList<>(AiActionMenu.TargetOption::search);
   private final Map<AiActionMenu.MenuAction, AiActionMenu.OptionBuild> init_Map = new EnumMap<>(AiActionMenu.MenuAction.class);
   private AiActionMenu.MenuAction selectedAction;
   private List<AiActionMenu.MenuAction> cachedLevelEntries;
   private Object cachedLevel;
   private List<AiActionMenu.TargetOption> lastSearchBase;
   private List<AiActionMenu.TargetOption> filtered;
   private List<AiActionMenu.TargetOption> filteredRows;
   private String init_Str;
   private String rowsQuery;
   private AiActionMenu.MenuAction rowsAction;
   private int selectedIndex;
   private int visibleRowCount;
   private boolean init_Z;
   private static Map<EntityType<?>, ItemStack> spawnEggIcons;
   private AiActionMenu.MenuStep step;
   private AiActionMenu.TargetOption selectedTarget;
   private final Map<String, AiActionMenu.TargetOption> read_Map;
   private final Map<String, autismclient.gui.ui.widgets.TextInputWidget> write_Map;
   private List<String> typedTargetTypes;
   private Object load_Obj;
   private boolean extraItems;
   private boolean read_Z;
   private String issue;
   private AiSchematicPlacement schematicPlacement;
   private AiFarmPlacement farmPlacement;
   private AiActionMenu.TargetOption previousTarget;
   private boolean farmBuild;
   private String farmSubstrate;
   private final AiUiKit.ScrollPane actionListScroll;
   private final AiUiKit.ScrollPane optionsScroll;
   private AiActionMenu.MenuStep restoredStep;

   AiActionMenu(AiService service, AiViewState view, AiAssistantOverlay host, Runnable onRun) {
      this.selectedAction = AiActionMenu.MenuAction.GET;
      this.filteredRows = List.of();
      this.init_Str = "";
      this.rowsQuery = "";
      this.visibleRowCount = 3;
      this.step = AiActionMenu.MenuStep.HOME;
      this.read_Map = new LinkedHashMap<>();
      this.write_Map = new LinkedHashMap<>();
      this.typedTargetTypes = List.of();
      this.issue = "";
      this.farmSubstrate = "farmland";
      this.actionListScroll = new AiUiKit.ScrollPane(false);
      this.optionsScroll = new AiUiKit.ScrollPane(false);
      this.service = service;
      this.view = view;
      this.host = host;
      this.onRun = onRun;
      this.searchInput.setOnSubmit(text -> this.sync_V());
      this.countInput.setOnSubmit(text -> this.sync_V());
   }

   List<autismclient.gui.ui.widgets.TextInputWidget> inputs() {
      ArrayList<autismclient.gui.ui.widgets.TextInputWidget> fields = new ArrayList<>(List.of(this.searchInput, this.countInput));
      fields.addAll(this.write_Map.values());
      return fields;
   }

   void openAction(String actionName, String query) {
      this.restoredStep = null;
      if ("HOME".equals(actionName)) {
         this.resetToActions();
      } else {
         String[] parts = actionName.split(":", 2);
         if (parts[0].equals("FARM") && parts.length > 1 && parts[1].equals("REVIEW") && this.farmPlacement != null && this.farmPlacement.isComplete()) {
            this.step = AiActionMenu.MenuStep.REVIEW;
         } else {
            this.selectAction(AiActionMenu.MenuAction.valueOf(parts[0]));
            if (this.selectedAction == AiActionMenu.MenuAction.MINE && query != null && query.contains(",")) {
               this.typedTargetTypes = AiMiningTargets.parse(query).types();
               this.searchInput.setValue("");
            } else {
               this.searchInput.setValue(query == null ? "" : query);
            }

            if (parts.length > 1) {
               this.restoredStep = AiActionMenu.MenuStep.valueOf(parts[1]);
            }
         }
      }
   }

   void blur() {
      if (this.searchInput.isFocused()) {
         this.searchInput.setFocused(false);
      }

      if (this.countInput.isFocused()) {
         this.countInput.setFocused(false);
      }

      this.write_Map.values().forEach(field -> field.setFocused(false));
   }

   autismclient.gui.ui.widgets.TextInputWidget inputForChar(char c) {
      return this.step == AiActionMenu.MenuStep.TARGET && (Character.isLetterOrDigit(c) || c == ' ' || c == '-')
         ? this.searchInput
         : null;
   }

   boolean isOpen() {
      return this.step != AiActionMenu.MenuStep.HOME;
   }

   void cancel() {
      if (this.schematicPlacement != null) {
         this.schematicPlacement.cancel();
      }

      if (this.farmPlacement != null) {
         this.farmPlacement.cancel();
      }

      this.schematicPlacement = null;
      this.farmPlacement = null;
      if ((this.selectedAction == AiActionMenu.MenuAction.BUILD || this.selectedAction == AiActionMenu.MenuAction.FARM)
         && this.step != AiActionMenu.MenuStep.HOME) {
         this.step = AiActionMenu.MenuStep.TARGET;
      }
   }

   int preferredHeight() {
      int target = AiUiKit.toolbarHeight() * 2
         + AiUiKit.rowHeight() * 3
         + 3 * AiUiKit.inputHeight()
         + 10
         + 4
         + (this.selectedAction == AiActionMenu.MenuAction.MINE ? AiUiKit.toolbarHeight() + 2 : 0);
      return this.step == AiActionMenu.MenuStep.OPTIONS && this.selectedAction.init_Z
         ? Math.max(
            target,
            this.load_I()
               + (
                  this.selectedAction == AiActionMenu.MenuAction.MINE
                     ? Math.min(this.read_I(), AiUiKit.rowHeight() * 3 + AiUiKit.toolbarHeight() * 6 + 14)
                     : this.read_I()
               )
         )
         : target;
   }

   private int load_I() {
      return AiUiKit.rowHeight() * 3 + AiUiKit.toolbarHeight() + 8;
   }

   private int read_I() {
      if (this.selectedAction == AiActionMenu.MenuAction.FARM && this.farmBuild) {
         return AiUiKit.rowHeight() * 4 + AiUiKit.toolbarHeight() * 2 + 12;
      } else {
         return this.selectedAction == AiActionMenu.MenuAction.BUILD
            ? AiUiKit.rowHeight() * 6 + AiUiKit.toolbarHeight() * 4 + 16
            : (
               this.selectedAction.init_Z
                  ? AiUiKit.rowHeight() * 2
                     + AiUiKit.toolbarHeight() * 3
                     + 6
                     + (
                        this.selectedAction == AiActionMenu.MenuAction.MINE
                           ? AiUiKit.rowHeight() + 2 + this.read_Map.size() * (AiUiKit.toolbarHeight() + 2)
                           : 0
                     )
                  : AiUiKit.rowHeight() * 2 + 2
            );
      }
   }

   int heightFor(int width) {
      int h = AiUiKit.toolbarHeight();
      int line = AiUiKit.rowHeight();
      int var10000;
      switch (this.step) {
         case HOME:
            var10000 = line * 2 + 6 + (this.levelEntries().size() + (width >= 440 ? 2 : 1)) / (width >= 440 ? 3 : 2) * (h + AiUiKit.lineHeight() + 4 + 4);
            break;
         case TARGET:
            var10000 = 0;
            break;
         case OPTIONS:
            var10000 = this.load_I() + this.read_I();
            break;
         case REVIEW:
            var10000 = this.load_I()
               + line
                  * (
                     this.selectedAction == AiActionMenu.MenuAction.MINE
                        ? this.read_Map.size() + 3
                        : (this.farmPlacement != null ? 7 : (this.schematicPlacement == null ? 4 : 8 + this.schematicPlacement.missingMaterialLines().size()))
                  )
               + 8;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   int composerHeight(AiUiKit kit, int width, int room) {
      return Math.max(this.preferredHeight(), room);
   }

   void render(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (!this.typedTargetTypes.isEmpty() && this.step == AiActionMenu.MenuStep.TARGET) {
         List<AiActionMenu.TargetOption> rows = this.filteredActionRows();

         for (int i = 0; i < rows.size(); i++) {
            String key = this.mineKeyOf(rows.get(i));
            if (this.typedTargetTypes.contains(key) && !this.read_Map.containsKey(key)) {
               this.selectRow(i);
            }
         }

         if (this.read_Map.keySet().containsAll(this.typedTargetTypes)) {
            this.typedTargetTypes = List.of();
         }
      }

      if (this.restoredStep != null && this.typedTargetTypes.isEmpty()) {
         if (this.step == this.restoredStep) {
            this.restoredStep = null;
         } else if (this.step == AiActionMenu.MenuStep.TARGET) {
            if (this.selectedRow(this.filteredActionRows()) != null) {
               this.sync_V();
            }
         } else if (this.step == AiActionMenu.MenuStep.OPTIONS
            && (this.schematicPlacement == null || this.schematicPlacement.isReadyToConfirm())
            && (this.farmPlacement == null || this.farmPlacement.isComplete())) {
            this.sync_V();
         }
      }

      if (this.schematicPlacement != null && !this.schematicPlacement.isReadyToConfirm() && !this.schematicPlacement.getError().isEmpty()) {
         this.issue = this.schematicPlacement.getError();
      }

      if (this.farmPlacement != null && this.farmPlacement.isComplete() && !this.farmPlacement.isReadyToConfirm()) {
         this.issue = this.farmPlacement.getStatus();
      }

      if (this.load_Obj != null && this.load_Obj != Minecraft.getInstance().level) {
         this.resetToActions();
         this.issue = "World changed. Choose the action again.";
      }

      int h = AiUiKit.toolbarHeight();
      int line = AiUiKit.rowHeight();
      autismclient.gui.f.NetHelper title = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), line);
      kit.drawCenteredRowText(
         this.step == AiActionMenu.MenuStep.HOME
            ? "What would you like to do?"
            : this.selectedAction.init_Str + " / " + this.stepTitle(),
         title,
         title.read(),
         title.update(),
         kit.colors.textPrimary
      );
      int top = title.load() + 2;
      if (this.step != AiActionMenu.MenuStep.HOME) {
         autismclient.gui.f.NetHelper trail = autismclient.gui.f.NetHelper.init(area.read(), top, area.update(), line);
         kit.drawCenteredRowText("Choose action  /  Choose target  /  Options  /  Review", trail, trail.read(), trail.update(), kit.colors.textSecondary);
         top += line + 2;
      }

      autismclient.gui.f.NetHelper footer = autismclient.gui.f.NetHelper.init(area.read(), area.load() - h, area.update(), h);
      autismclient.gui.f.NetHelper note = autismclient.gui.f.NetHelper.init(
         area.read(), this.step == AiActionMenu.MenuStep.HOME ? area.load() - line : footer.write() - 2 - line, area.update(), line
      );
      autismclient.gui.f.NetHelper body = autismclient.gui.f.NetHelper.init(area.read(), top, area.update(), Math.max(0, note.write() - 2 - top));
      kit.pushClip(body);
      switch (this.step) {
         case HOME:
            this.init_V_cE_NetHelper(kit, body);
            break;
         case TARGET:
            int importW = this.selectedAction == AiActionMenu.MenuAction.BUILD ? kit.measureButtonWidth(this.init_Z ? "Loading..." : "Import") + 2 : 0;
            this.searchInput.setPlaceholder(this.selectedAction.read_Str);
            kit.drawTextInput(this.searchInput, autismclient.gui.f.NetHelper.init(body.read(), body.write(), body.update() - importW, h));
            if (importW > 0) {
               kit.drawButton(
                  autismclient.gui.f.NetHelper.init(body.init() - importW + 2, body.write(), importW - 2, h),
                  this.init_Z ? "Loading..." : "Import",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
                  this::pickAttachment
               );
            }

            int listTop = body.write() + h + 2;
            if (this.selectedAction == AiActionMenu.MenuAction.MINE) {
               int clearW = kit.measureButtonWidth("Clear");
               int selectedW = kit.measureButtonWidth(this.read_Z ? "Show all" : "Selected");
               autismclient.gui.f.NetHelper summary = autismclient.gui.f.NetHelper.init(
                  body.read(), listTop, Math.max(0, body.update() - clearW - selectedW - 4), h
               );
               String text = this.read_Map.isEmpty() ? "Click blocks to select them" : this.read_Map.size() + " selected: " + this.read_Str();
               kit.drawCenteredRowText(text, summary, summary.read(), summary.update(), kit.colors.textSecondary);
               kit.addTooltipRegion(summary, text);
               kit.drawButton(
                  autismclient.gui.f.NetHelper.init(body.init() - clearW - selectedW - 2, listTop, selectedW, h),
                  this.read_Z ? "Show all" : "Selected",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
                  () -> {
                     this.read_Z = !this.read_Z;
                     this.listScroll.scrollToTop();
                  }
               );
               kit.drawButton(
                  autismclient.gui.f.NetHelper.init(body.init() - clearW, listTop, clearW, h),
                  "Clear",
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
                  () -> {
                     this.read_Map.clear();
                     this.write_Map.clear();
                     this.issue = "";
                  }
               );
               listTop += h + 2;
            }

            List<AiActionMenu.TargetOption> rows = this.selectedAction == AiActionMenu.MenuAction.MINE && this.read_Z
               ? List.copyOf(this.read_Map.values())
               : this.filteredActionRows();
            this.visibleRowCount = Math.max(1, (body.load() - listTop) / AiUiKit.inputHeight());
            this.init_V_cE_2(kit, autismclient.gui.f.NetHelper.init(body.read(), listTop, body.update(), Math.max(0, body.load() - listTop)), rows);
            break;
         case OPTIONS:
            this.renderStepContent(kit, body);
            break;
         case REVIEW:
            this.read(kit, body);
      }

      kit.popClip();
      String noteText = !this.issue.isEmpty()
         ? this.issue
         : (
            !isInWorld()
               ? "Join a world to start an action."
               : (
                  this.step == AiActionMenu.MenuStep.TARGET
                        && this.selectedAction == AiActionMenu.MenuAction.MINE
                     ? "Click to toggle blocks. Nearest reachable first; amounts apply per block type."
                     : (
                        this.step == AiActionMenu.MenuStep.TARGET && this.selectedRow(this.filteredRows) != null
                           ? this.selectedRow(this.filteredRows).label()
                           : (
                              this.step == AiActionMenu.MenuStep.HOME
                                 ? "Runs locally. No API needed."
                                 : "Review your choices before starting."
                           )
                     )
               )
         );
      kit.drawCenteredRowText(noteText, note, note.read(), note.update(), this.issue.isEmpty() ? kit.colors.textSecondary : kit.colors.danger);
      kit.addTooltipRegion(note, noteText);
      if (this.step != AiActionMenu.MenuStep.HOME) {
         int w = kit.measureButtonWidth("Back");
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(footer.read(), footer.write(), w, h), "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, this::stepBack
         );
         String label = this.step == AiActionMenu.MenuStep.REVIEW ? (this.view.isWorking() ? "Replace task" : "Start") : "Next";
         int nextW = kit.measureButtonWidth(label);
         this.drawButton(
            kit,
            autismclient.gui.f.NetHelper.init(footer.init() - nextW, footer.write(), nextW, h),
            label,
            isInWorld()
               && (
                  this.step != AiActionMenu.MenuStep.TARGET
                     || (
                        this.selectedAction == AiActionMenu.MenuAction.MINE
                           ? !this.read_Map.isEmpty()
                           : this.selectedRow(this.filteredRows) != null
                     )
               )
               && (this.schematicPlacement == null || this.schematicPlacement.isReadyToConfirm() && !this.schematicPlacement.isSearching())
               && (this.farmPlacement == null || this.farmPlacement.isComplete()),
            this.step == AiActionMenu.MenuStep.REVIEW ? this::pollReview : this::sync_V
         );
      }
   }

   private String stepTitle() {
      String var10000;
      switch (this.step) {
         case HOME:
            var10000 = "Actions";
            break;
         case TARGET:
            var10000 = "Choose target";
            break;
         case OPTIONS:
            var10000 = this.selectedAction == AiActionMenu.MenuAction.BUILD ? "Placement" : "Options";
            break;
         case REVIEW:
            var10000 = "Review";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private void drawButton(AiUiKit kit, autismclient.gui.f.NetHelper b, String text, boolean enabled, Runnable action) {
      autismclient.gui.f.b.CachePool.init(
         kit.renderContext,
         b,
         text,
         enabled ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         enabled && kit.isHovered(b),
         enabled
      );
      if (enabled) {
         kit.addInteractiveRegion(b, action, null, null);
      } else {
         kit.blockClicks(b);
      }
   }

   private void init_V_cE_NetHelper(AiUiKit kit, autismclient.gui.f.NetHelper area) {
      List<AiActionMenu.MenuAction> actions = this.levelEntries();
      int columns = area.update() >= 440 ? 3 : 2;
      int rowH = AiUiKit.toolbarHeight() + AiUiKit.lineHeight() + 2 + 4;
      int rows = (actions.size() + columns - 1) / columns;
      this.actionListScroll.layout(area, rows * (rowH + 2));
      kit.registerScrollPane(this.actionListScroll);
      int available = area.update() - (this.actionListScroll.maxScroll() > 0 ? 7 : 0);
      int w = (available - 2 * (columns - 1)) / columns;

      for (int i = 0; i < actions.size(); i++) {
         AiActionMenu.MenuAction a = actions.get(i);
         autismclient.gui.f.NetHelper b = autismclient.gui.f.NetHelper.init(
            area.read() + i % columns * (w + 2), area.write() + i / columns * (rowH + 2) - this.actionListScroll.offset, w, rowH
         );
         if (b.load() > area.write() && b.write() < area.load()) {
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(b.read(), b.write(), b.update(), AiUiKit.toolbarHeight()),
               a.init_Str,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
               () -> this.selectAction(a),
               a.write_Str
            );
            kit.drawCenteredRowText(
               a.write_Str,
               autismclient.gui.f.NetHelper.init(b.read(), b.write() + AiUiKit.toolbarHeight(), b.update(), AiUiKit.lineHeight() + 4),
               b.read() + 3,
               b.update() - 6,
               kit.colors.textSecondary
            );
         }
      }

      kit.drawScrollbar(this.actionListScroll);
   }

   private void renderStepContent(AiUiKit kit, autismclient.gui.f.NetHelper area) {
      int h = AiUiKit.toolbarHeight();
      int line = AiUiKit.rowHeight();
      int contentH = this.read_I();
      this.optionsScroll.layout(area, contentH);
      kit.registerScrollPane(this.optionsScroll);
      int w = area.update() - (this.optionsScroll.maxScroll() > 0 ? 7 : 0);
      int x = area.read();
      int y = area.write() - this.optionsScroll.offset;
      kit.drawCenteredRowText(
         this.selectedTarget == null ? this.selectedAction.write_Str : this.read_Str(),
         autismclient.gui.f.NetHelper.init(x, y, w, line),
         x,
         w,
         kit.colors.textPrimary
      );
      y += line + 2;
      if (this.selectedAction == AiActionMenu.MenuAction.FARM && this.farmBuild) {
         kit.drawCenteredRowText("Crop: " + this.read_Str(), autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textPrimary);
         int var23 = y + line + 2;
         kit.drawToggleSwitch(
            autismclient.gui.f.NetHelper.init(x, var23, w, h),
            "Soul sand for nether wart",
            AiFarmArea.isSoulSand(this.farmSubstrate),
            "local-farm-substrate",
            () -> {
               this.farmSubstrate = AiFarmArea.isSoulSand(this.farmSubstrate) ? "farmland" : "soul_sand";
               if (AiFarmArea.isSoulSand(this.farmSubstrate)) {
                  this.previousTarget = this.selectedTarget;
                  this.selectedTarget = farmRow("nether wart", Items.NETHER_WART);
               } else {
                  this.selectedTarget = this.previousTarget == null
                     ? farmRow("wheat", Items.WHEAT_SEEDS)
                     : this.previousTarget;
               }

               if (this.farmPlacement != null) {
                  this.farmPlacement.cancel();
               }

               this.farmPlacement = null;
               this.issue = "";
            }
         );
         int var24 = var23 + h + 2;
         this.drawButton(
            kit,
            autismclient.gui.f.NetHelper.init(x, var24, w, h),
            this.farmPlacement == null ? "Choose two corners" : "Change corners",
            !this.view.isWorking() && !AiDirectOps.isDrivingInput(),
            this::runSelected
         );
         int var25 = var24 + h + 2;
         kit.drawCenteredRowText(
            this.farmPlacement == null ? "Select the exact field footprint in the world." : this.farmPlacement.getStatus(),
            autismclient.gui.f.NetHelper.init(x, var25, w, line),
            x,
            w,
            kit.colors.textSecondary
         );
         y = var25 + line + 2;
         kit.drawCenteredRowText(
            AiFarmArea.isSoulSand(this.farmSubstrate) ? "No water, slabs or hoe needed." : "Farmland crops use dirt, water and a hoe.",
            autismclient.gui.f.NetHelper.init(x, y, w, line),
            x,
            w,
            kit.colors.textSecondary
         );
      } else if (this.selectedAction == AiActionMenu.MenuAction.BUILD && this.schematicPlacement != null) {
         kit.drawCenteredRowText(this.schematicPlacement.getStatus(), autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textSecondary);
         y += line;
         kit.drawCenteredRowText(
            "Size " + this.schematicPlacement.sizeText() + " / origin " + this.schematicOriginText(),
            autismclient.gui.f.NetHelper.init(x, y, w, line),
            x,
            w,
            kit.colors.textSecondary
         );
         y += line + 2;
         int half = (w - 2) / 2;
         this.drawButton(
            kit,
            autismclient.gui.f.NetHelper.init(x, y, half, h),
            "Choose in world",
            this.schematicPlacement.isReadyToConfirm() && !this.view.isWorking() && !AiDirectOps.isDrivingInput(),
            () -> aP.init_V_bW_Runnable(this.schematicPlacement, () -> {})
         );
         if (this.view.isWorking() || AiDirectOps.isDrivingInput()) {
            kit.addTooltipRegion(autismclient.gui.f.NetHelper.init(x, y, half, h), "Pause the current action before choosing a placement in world");
         }

         this.drawButton(
            kit,
            autismclient.gui.f.NetHelper.init(x + half + 2, y, half, h),
            this.schematicPlacement.isSearching() ? "Searching..." : "Best nearby site",
            this.schematicPlacement.isReadyToConfirm(),
            this.schematicPlacement::confirmPlacement
         );
         int var18 = y + h + 2;
         kit.drawCenteredRowText(
            "Compare edits within 24 blocks. All four rotations.", autismclient.gui.f.NetHelper.init(x, var18, w, line), x, w, kit.colors.textSecondary
         );
         int var19 = var18 + line + 2;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x, var19, half, h),
            "Rotate: " + this.schematicPlacement.getRotation(),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
            this.schematicPlacement::rotateOnce
         );
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(x + half + 2, var19, half, h),
            "Mirror: " + this.schematicPlacement.getMirror(),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
            this.schematicPlacement::cycleMirror
         );
         int var20 = var19 + h + 2;
         kit.drawToggleSwitch(
            autismclient.gui.f.NetHelper.init(x, var20, w, h),
            "Clear natural terrain in empty space",
            this.schematicPlacement.isClearTerrain(),
            "local-terrain",
            () -> this.schematicPlacement.setClearTerrain(!this.schematicPlacement.isClearTerrain())
         );
         int var21 = var20 + h + 2;
         autismclient.gui.f.NetHelper flyToggle = autismclient.gui.f.NetHelper.init(x, var21, w, h);
         kit.drawToggleSwitch(flyToggle, "Assume Fly", this.schematicPlacement.isAssumeFly(), "local-assume-fly", () -> this.schematicPlacement.setAssumeFly(!this.schematicPlacement.isAssumeFly()));
         kit.addTooltipRegion(
            flyToggle,
            "Use native flight already granted in Survival, including singleplayer. Never grants flight or items. Without permission, use ground movement."
         );
         int var22 = var21 + h + 2;
         autismclient.gui.f.NetHelper flyHint = autismclient.gui.f.NetHelper.init(x, var22, w, line);
         kit.drawCenteredRowText(this.schematicPlacement.getFlyHint(), flyHint, x, w, kit.colors.textSecondary);
         kit.addTooltipRegion(flyHint, this.schematicPlacement.getFlyHint());
         y = var22 + line + 2;
         kit.drawCenteredRowText("Get missing materials before starting this build.", autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textSecondary);
      } else if (this.selectedAction.init_Z) {
         kit.drawCenteredRowText(
            (this.selectedAction == AiActionMenu.MenuAction.MINE ? "Default per block" : "Amount")
               + " (1 to "
               + this.write_I()
               + ")",
            autismclient.gui.f.NetHelper.init(x, y, w, line),
            x,
            w,
            kit.colors.textSecondary
         );
         y += line;
         this.countInput.setPlaceholder(this.selectedTarget == null ? "Auto" : this.write_Str());
         kit.drawTextInput(this.countInput, autismclient.gui.f.NetHelper.init(x, y, w, h));
         y += h + 2;
         String[] values = new String[]{"Auto", "1", "16", "64"};
         int cell = (w - 6) / 4;

         for (int i = 0; i < values.length; i++) {
            String value = values[i];
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(x + i * (cell + 2), y, cell, h), value, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, () -> {
                  this.countInput.setValue(value.equals("Auto") ? "" : value);
                  this.issue = "";
               }
            );
         }

         y += h + 2;
         if (this.selectedAction == AiActionMenu.MenuAction.GET) {
            kit.drawCenteredRowText(
               "Always collect the requested amount in addition to inventory.", autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textSecondary
            );
         } else if (this.read_Z()) {
            kit.drawToggleSwitch(
               autismclient.gui.f.NetHelper.init(x, y, w, h),
               this.selectedAction == AiActionMenu.MenuAction.CRAFT ? "Craft extra items" : "Smelt extra items",
               this.extraItems,
               "local-additional",
               () -> this.extraItems = !this.extraItems
            );
         } else {
            kit.drawCenteredRowText(
               this.selectedAction == AiActionMenu.MenuAction.MINE
                  ? "Each type has its own quota; nearest unfinished type first."
                  : "Uses the selected action's normal quantity rules.",
               autismclient.gui.f.NetHelper.init(x, y, w, line),
               x,
               w,
               kit.colors.textSecondary
            );
         }
      } else {
         kit.drawCenteredRowText(this.selectedAction.write_Str, autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textSecondary);
      }

      if (this.selectedAction == AiActionMenu.MenuAction.MINE) {
         y += line + 2;
         kit.drawCenteredRowText("Individual amounts (optional)", autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textSecondary);
         y += line + 2;
         int fieldW = Math.min(88, w / 3);

         for (Entry<String, AiActionMenu.TargetOption> entry : this.read_Map.entrySet()) {
            autismclient.gui.ui.widgets.TextInputWidget field = this.init_BitSetUtil_Str(entry.getKey());
            field.setPlaceholder(this.countInput.getValue().isBlank() ? this.write_Str() : this.countInput.getValue());
            kit.drawCenteredRowText(entry.getValue().label(), autismclient.gui.f.NetHelper.init(x, y, w - fieldW - 2, h), x, w - fieldW - 2, kit.colors.textPrimary);
            kit.drawTextInput(field, autismclient.gui.f.NetHelper.init(x + w - fieldW, y, fieldW, h));
            y += h + 2;
         }
      }

      kit.drawScrollbar(this.optionsScroll);
   }

   private void read(AiUiKit kit, autismclient.gui.f.NetHelper area) {
      int line = AiUiKit.rowHeight();
      int y = area.write();
      int x = area.read();
      int w = area.update();
      autismclient.gui.f.b.BufferHelper.init(kit.renderContext, area);
      x += 5;
      y += 4;
      w -= 10;
      String[] lines;
      if (this.farmPlacement != null) {
         AiFarmArea.Area farmArea = this.farmPlacement.getArea();
         lines = new String[]{
            this.selectedTarget.label(),
            "Soil: " + (AiFarmArea.isSoulSand(this.farmSubstrate) ? "soul sand" : "dirt and water"),
            "Area: " + farmArea.width() + "x" + farmArea.length() + " / " + farmArea.layout(this.farmSubstrate).plots() + " plots",
            "First: " + this.formatBlockPos(farmArea.from()),
            "Second: " + this.formatBlockPos(farmArea.to()),
            this.farmPlacement.getStatus(),
            this.view.isWorking() ? "Starting replaces the current local task." : "Ready to start."
         };
      } else if (this.schematicPlacement != null) {
         lines = new String[]{
            this.selectedTarget.label(),
            "Origin: " + this.schematicOriginText(),
            "Rotation: " + this.schematicPlacement.getRotation() + " / mirror: " + this.schematicPlacement.getMirror(),
            this.schematicPlacement.getCellCount() + " blocks / " + this.schematicPlacement.sizeText(),
            this.schematicPlacement.isClearTerrain() ? "Clear natural terrain in schematic air" : "Keep terrain in schematic air",
            this.schematicPlacement.getFlyHint(),
            "Assume Fly never grants flight or items",
            this.schematicPlacement.getStatus()
         };
      } else {
         String quantity = this.selectedAction.init_Z
            ? "Amount: " + (this.countInput.getValue().isBlank() ? this.write_Str() + " (automatic)" : this.countInput.getValue())
            : this.selectedAction.write_Str;
         lines = new String[]{
            this.selectedAction.init_Str + (this.selectedTarget == null ? "" : ": " + this.read_Str()),
            quantity,
            this.selectedAction == AiActionMenu.MenuAction.MINE
               ? "Per block type; nearest unfinished target first"
               : (this.read_Z() ? (this.write_Z() ? "Quantity adds to the inventory" : "Quantity is the inventory target") : ""),
            this.view.isWorking() ? "Starting replaces the current local task." : "Ready to start."
         };
      }

      if (this.selectedAction == AiActionMenu.MenuAction.MINE) {
         ArrayList<String> extended = new ArrayList<>();
         extended.add("Mine amounts");

         try {
            JsonObject quotas = this.buildRequest().arguments().getAsJsonObject("counts");
            this.read_Map.forEach((key, row) -> extended.add(row.label() + ": " + AiJson.getInt(quotas, key, 1)));
         } catch (RuntimeException e) {
            extended.add(e.getMessage());
         }

         extended.add("Nearest unfinished target first");
         extended.add(this.view.isWorking() ? "Starting replaces the current local task." : "Ready to start.");
         lines = extended.toArray(String[]::new);
      }

      if (this.schematicPlacement != null) {
         List<String> extended = new ArrayList<>(List.of(lines));
         extended.addAll(this.schematicPlacement.missingMaterialLines());
         lines = extended.toArray(String[]::new);
      }

      this.optionsScroll.layout(area, lines.length * line + 8);
      kit.registerScrollPane(this.optionsScroll);
      y -= this.optionsScroll.offset;

      for (String text : lines) {
         kit.drawCenteredRowText(text, autismclient.gui.f.NetHelper.init(x, y, w, line), x, w, kit.colors.textPrimary);
         kit.addTooltipRegion(autismclient.gui.f.NetHelper.init(x, y, w, line), text);
         y += line;
      }

      kit.drawScrollbar(this.optionsScroll);
   }

   private String schematicOriginText() {
      BlockPos p = this.schematicPlacement.getOrigin();
      return p.getX() + " " + p.getY() + " " + p.getZ();
   }

   private String formatBlockPos(BlockPos p) {
      return p.getX() + " " + p.getY() + " " + p.getZ();
   }

   private boolean read_Z() {
      return this.selectedAction == AiActionMenu.MenuAction.GET
         || this.selectedAction == AiActionMenu.MenuAction.CRAFT
         || this.selectedAction == AiActionMenu.MenuAction.SMELT;
   }

   private String read_Str() {
      return this.selectedAction == AiActionMenu.MenuAction.MINE
         ? this.read_Map.values().stream().map(AiActionMenu.TargetOption::label).collect(Collectors.joining(", "))
         : this.selectedTarget.label();
   }

   private String write_Str() {
      return this.selectedAction == AiActionMenu.MenuAction.MINE && this.read_Map.size() > 1
         ? "16"
         : this.selectedTarget.fallback();
   }

   private autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil_Str(String key) {
      return this.write_Map
         .computeIfAbsent(
            key,
            k -> {
               autismclient.gui.ui.widgets.TextInputWidget field = new autismclient.gui.ui.widgets.TextInputWidget()
                  .setMaxLength(3)
                  .setInputFilter(s -> s.chars().allMatch(Character::isDigit));
               field.setOnSubmit(text -> this.sync_V());
               return field;
            }
         );
   }

   private String mineKeyOf(AiActionMenu.TargetOption row) {
      return row.mineKey();
   }

   private List<AiActionMenu.TargetOption> load_List() {
      return this.selectedAction == AiActionMenu.MenuAction.MINE && this.read_Z
         ? List.copyOf(this.read_Map.values())
         : this.filteredActionRows();
   }

   private int write_I() {
      return this.selectedAction == AiActionMenu.MenuAction.MINE
         ? 640
         : (
            this.selectedAction != AiActionMenu.MenuAction.FIGHT
                  && this.selectedAction != AiActionMenu.MenuAction.CHOP
               ? (this.selectedAction == AiActionMenu.MenuAction.FARM ? 128 : 2304)
               : 64
         );
   }

   private void init_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper frame, List<AiActionMenu.TargetOption> rows) {
      if (frame.sync() >= 8) {
         autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(
            kit.graphics, frame.read(), frame.write(), frame.update(), frame.sync(), this.searchInput.isFocused()
         );
         autismclient.gui.f.NetHelper inner = frame.init(2);
         int rowH = AiUiKit.inputHeight();
         this.listScroll.layout(inner, rows.size() * rowH);
         kit.registerScrollPane(this.listScroll);
         if (rows.isEmpty()) {
            autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(kit.graphics, kit.font, this.statusMessage(), inner.read(), inner.write(), inner.update());
         } else {
            kit.pushClip(inner);
            int w = inner.update() - (this.listScroll.maxScroll() > 0 ? 7 : 0);

            for (int i = Math.max(0, this.listScroll.offset / rowH); i < rows.size(); i++) {
               int ry = inner.write() - this.listScroll.offset + i * rowH;
               if (ry >= inner.load()) {
                  break;
               }

               AiActionMenu.TargetOption row = rows.get(i);
               autismclient.gui.f.NetHelper b = autismclient.gui.f.NetHelper.init(inner.read(), ry, w, rowH);
               autismclient.gui.ui.widgets.ListRowRenderer.drawIconRow(
                  kit.graphics,
                  kit.font,
                  Component.literal(row.label()),
                  row.icon(),
                  b.read(),
                  b.write(),
                  b.update(),
                  b.sync(),
                  kit.isHovered(b),
                  this.selectedAction == AiActionMenu.MenuAction.MINE
                     ? this.read_Map.containsKey(this.mineKeyOf(row))
                     : i == this.selectedIndex,
                  kit.colors.textPrimary,
                  false
               );
               int index = i;
               kit.addInteractiveRegion(b, () -> this.selectRow(index), null, null);
            }

            kit.popClip();
            kit.drawScrollbar(this.listScroll);
         }
      }
   }

   private String statusMessage() {
      if (!isInWorld()) {
         return "Join a world first";
      } else if (this.init_Map.get(this.selectedAction) == null || this.init_Map.get(this.selectedAction).visible() == null) {
         return "Loading...";
      } else if (this.searchQueryText().isEmpty() && this.selectedAction == AiActionMenu.MenuAction.GOTO) {
         return "Type x y z";
      } else {
         return this.searchQueryText().isEmpty() && this.selectedAction == AiActionMenu.MenuAction.BUILD ? "No schematics yet" : "No matches";
      }
   }

   boolean handleKey(int key, boolean typing) {
      if (this.step != AiActionMenu.MenuStep.HOME && this.step != AiActionMenu.MenuStep.REVIEW) {
         switch (key) {
            case 257:
            case 335:
               if (!typing) {
                  return false;
               }

               this.sync_V();
               break;
            case 264:
               this.init_V_I(1);
               break;
            case 265:
               this.init_V_I(-1);
               break;
            case 266:
               this.init_V_I(-this.visibleRowCount);
               break;
            case 267:
               this.init_V_I(this.visibleRowCount);
               break;
            default:
               return false;
         }

         return true;
      } else {
         return false;
      }
   }

   boolean back() {
      if (this.step == AiActionMenu.MenuStep.TARGET && !this.searchInput.getValue().isEmpty()) {
         this.searchInput.setValue("");
         return true;
      }

      if (this.step == AiActionMenu.MenuStep.HOME) {
         return false;
      }

      this.stepBack();
      return true;
   }

   private void init_V_I(int delta) {
      if (this.selectedAction.isDirect()) {
         List<AiActionMenu.TargetOption> rows = this.load_List();
         if (!rows.isEmpty()) {
            this.selectedIndex = Math.max(0, Math.min(rows.size() - 1, this.selectedIndex + delta));
            int rowH = AiUiKit.inputHeight();
            int top = this.selectedIndex * rowH;
            int shown = this.listScroll.viewportBounds().sync();
            if (top < this.listScroll.offset) {
               this.listScroll.offset = top;
            } else if (top + rowH > this.listScroll.offset + shown) {
               this.listScroll.offset = top + rowH - shown;
            }

            this.listScroll.followBottom = false;
         }
      }
   }

   private void selectRow(int index) {
      this.selectedIndex = index;
      if (this.selectedAction == AiActionMenu.MenuAction.MINE) {
         List<AiActionMenu.TargetOption> rows = this.load_List();
         if (index < 0 || index >= rows.size()) {
            return;
         }

         AiActionMenu.TargetOption row = rows.get(index);
         String key = this.mineKeyOf(row);
         if (!key.isEmpty()) {
            if (this.read_Map.remove(key) == null) {
               this.read_Map.put(key, row);
               this.init_BitSetUtil_Str(key);
            } else {
               this.write_Map.remove(key);
            }
         }

         this.load_Obj = this.read_Map.isEmpty() ? null : Minecraft.getInstance().level;
         this.issue = "";
      }

      this.host.focusInput(this.searchInput);
   }

   private void selectAction(AiActionMenu.MenuAction next) {
      if (this.schematicPlacement != null) {
         this.schematicPlacement.cancel();
      }

      if (this.farmPlacement != null) {
         this.farmPlacement.cancel();
      }

      this.schematicPlacement = null;
      this.farmPlacement = null;
      this.selectedTarget = null;
      this.load_Obj = null;
      this.issue = "";
      this.extraItems = false;
      this.read_Map.clear();
      this.write_Map.clear();
      this.read_Z = false;
      this.typedTargetTypes = List.of();
      if (next != this.selectedAction) {
         this.selectedAction = next;
         this.searchInput.setValue("");
         this.countInput.setValue("");
         this.selectedIndex = 0;
         this.filteredRows = List.of();
         this.filtered = null;
         this.listScroll.scrollToTop();
      }

      this.step = next.isDirect() ? AiActionMenu.MenuStep.TARGET : AiActionMenu.MenuStep.REVIEW;
      this.optionsScroll.scrollToTop();
      if (next.isDirect()) {
         this.host.focusInput(this.searchInput);
      } else {
         this.blur();
      }
   }

   private void sync_V() {
      this.issue = "";
      if (!isInWorld()) {
         this.issue = "Join a world first";
      } else {
         if (this.step == AiActionMenu.MenuStep.TARGET) {
            this.selectedTarget = this.selectedAction == AiActionMenu.MenuAction.MINE
               ? this.read_Map.values().stream().findFirst().orElse(null)
               : this.selectedRow(this.filteredActionRows());
            if (this.selectedTarget == null) {
               this.issue = "Choose a target first";
               return;
            }

            this.load_Obj = Minecraft.getInstance().level;
            if (this.selectedAction == AiActionMenu.MenuAction.FARM) {
               AiIntents.IntentStep farmStep = this.planStep(
                  this.selectedTarget.request().apply(null), this.selectedAction.load_Str
               );
               if (farmStep == null) {
                  this.issue = "Choose the crop again";
                  return;
               }

               this.farmBuild = "build".equals(AiJson.getString(farmStep.arguments(), "action", "build"));
               this.previousTarget = null;
               this.farmSubstrate = AiFarmArea.defaultSubstrate(AiJson.getString(farmStep.arguments(), "crop", "minecraft:wheat"));
               if (this.farmPlacement != null) {
                  this.farmPlacement.cancel();
               }

               this.farmPlacement = null;
            }

            if (this.selectedAction == AiActionMenu.MenuAction.BUILD) {
               AiIntents.IntentStep step = this.planStep(
                  this.selectedTarget.request().apply(null), this.selectedAction.load_Str
               );
               if (step == null) {
                  this.issue = "Schematic is no longer available";
                  return;
               }

               try {
                  this.schematicPlacement = new AiSchematicPlacement(AiJson.getString(step.arguments(), "schematic", ""));
               } catch (RuntimeException e) {
                  this.issue = e.getMessage();
                  return;
               }
            }

            this.step = AiActionMenu.MenuStep.OPTIONS;
         } else if (this.step == AiActionMenu.MenuStep.OPTIONS) {
            try {
               if (this.selectedAction == AiActionMenu.MenuAction.FARM && this.farmBuild && (this.farmPlacement == null || !this.farmPlacement.isComplete())) {
                  this.runSelected();
                  return;
               }

               this.buildRequest();
               if (this.schematicPlacement != null) {
                  String issue = this.schematicPlacement.getStartIssue();
                  if (!issue.isEmpty()) {
                     this.issue = issue;
                     return;
                  }
               }

               this.step = AiActionMenu.MenuStep.REVIEW;
            } catch (RuntimeException e) {
               this.issue = e.getMessage();
            }
         }

         this.blur();
         this.optionsScroll.scrollToTop();
      }
   }

   private static AiActionMenu.TargetOption farmRow(String crop, Item icon) {
      return new AiActionMenu.TargetOption(
         "Build " + crop + " farm",
         crop + " farm build",
         autismclient.util.DisplayItemStackFactory.displayStack(icon),
         n -> "build a " + crop + " farm" + (n == null ? "" : " of " + n + " crops"),
         "crop",
         0,
         null
      );
   }

   private void runSelected() {
      if (!this.view.isWorking() && !AiDirectOps.isDrivingInput()) {
         try {
            AiIntents.IntentStep step = this.planStep(
               this.selectedTarget.request().apply(null), this.selectedAction.load_Str
            );
            if (step == null) {
               this.issue = "Choose the crop again";
               return;
            }

            String crop = AiJson.getString(step.arguments(), "crop", "minecraft:wheat");
            AiFarmArea.substrateFor(AiJson.objectOf("substrate", this.farmSubstrate), crop);
            if (this.farmPlacement == null || !this.farmPlacement.getCrop().equals(crop) || !this.farmPlacement.getSubstrate().equals(this.farmSubstrate)) {
               this.farmPlacement = new AiFarmPlacement(crop, this.farmSubstrate);
            }

            aP.init_V_E_Runnable(this.farmPlacement, () -> {});
         } catch (RuntimeException e) {
            this.issue = e.getMessage();
         }
      } else {
         this.issue = "Pause the current action before choosing farm corners";
      }
   }

   private void stepBack() {
      this.issue = "";
      this.blur();
      this.optionsScroll.scrollToTop();
      if (this.step == AiActionMenu.MenuStep.REVIEW) {
         this.step = this.selectedAction.isDirect() ? AiActionMenu.MenuStep.OPTIONS : AiActionMenu.MenuStep.HOME;
      } else if (this.step == AiActionMenu.MenuStep.OPTIONS) {
         if (this.schematicPlacement != null) {
            this.schematicPlacement.cancel();
         }

         if (this.farmPlacement != null) {
            this.farmPlacement.cancel();
         }

         this.schematicPlacement = null;
         this.farmPlacement = null;
         this.step = AiActionMenu.MenuStep.TARGET;
      } else {
         this.resetToActions();
      }
   }

   private void resetToActions() {
      if (this.schematicPlacement != null) {
         this.schematicPlacement.cancel();
      }

      if (this.farmPlacement != null) {
         this.farmPlacement.cancel();
      }

      this.schematicPlacement = null;
      this.farmPlacement = null;
      this.step = AiActionMenu.MenuStep.HOME;
      this.selectedTarget = null;
      this.load_Obj = null;
      this.issue = "";
      this.blur();
      this.read_Map.clear();
      this.write_Map.clear();
      this.read_Z = false;
      this.typedTargetTypes = List.of();
   }

   private AiIntents.IntentStep buildRequest() {
      Integer amount = this.selectedAction.init_Z ? AiLocalRequest.parseCount(this.countInput.getValue(), this.write_I()) : null;
      if (this.selectedAction == AiActionMenu.MenuAction.MINE) {
         List<JsonObject> targets = this.read_Map
            .values()
            .stream()
            .map(row -> this.planStep(row.request().apply(null), this.selectedAction.load_Str))
            .map(stepx -> {
               if (stepx == null) {
                  throw new IllegalArgumentException("A selected block is no longer available");
               }

               JsonObject argsx = stepx.arguments().deepCopy();
               String key = AiJson.getString(argsx, "block", "");
               Integer custom = AiLocalRequest.parseCount(this.init_BitSetUtil_Str(key).getValue(), 640);
               if (custom != null) {
                  argsx.addProperty("mineAmount", custom);
               }

               return argsx;
            })
            .toList();
         JsonObject args = AiLocalRequest.mineRequest(targets, amount);
         String label = "Mine "
            + args.getAsJsonObject("counts").entrySet().stream().map(e -> e.getValue().getAsInt() + " " + e.getKey()).collect(Collectors.joining(", "));
         return new AiIntents.IntentStep(label, this.selectedAction.load_Str, args, label);
      }

      String text = this.selectedAction.isDirect() ? this.selectedTarget.request().apply(amount) : this.selectedAction.update_Str;
      AiIntents.IntentStep step = this.planStep(text, this.selectedAction.load_Str);
      if (step == null) {
         throw new IllegalArgumentException("That option is unavailable. Choose the target again.");
      }

      JsonObject args = this.schematicPlacement == null ? step.arguments().deepCopy() : this.schematicPlacement.buildArguments();
      if (this.selectedAction == AiActionMenu.MenuAction.FARM && this.farmBuild) {
         if (this.farmPlacement == null || !this.farmPlacement.isComplete()) {
            throw new IllegalArgumentException("Choose both farm corners first");
         }

         JsonObject selectedArea = this.farmPlacement.areaJson();
         selectedArea.entrySet().forEach(entry -> args.add(entry.getKey(), entry.getValue()));
         String crop = AiJson.getString(args, "crop", "minecraft:wheat");
         AiFarmArea.substrateFor(args, crop);
         AiFarmArea.Area area = AiFarmArea.fromArgs(args);
         String issue = area.validate(this.farmSubstrate);
         if (!issue.isEmpty()) {
            throw new IllegalArgumentException(issue);
         }
      }

      if (this.read_Z()) {
         args.addProperty("additional", this.write_Z());
      }

      return new AiIntents.IntentStep(text, this.selectedAction.load_Str, args, step.describe());
   }

   private void pollReview() {
      if (this.step == AiActionMenu.MenuStep.REVIEW && isInWorld()) {
         if (this.load_Obj != null && this.load_Obj != Minecraft.getInstance().level) {
            this.resetToActions();
            this.issue = "World changed. Choose the action again.";
         } else {
            try {
               if (this.schematicPlacement != null) {
                  String issue = this.schematicPlacement.getStartIssue();
                  if (!issue.isEmpty()) {
                     this.issue = issue;
                     return;
                  }
               }

               AiIntents.IntentStep step = this.buildRequest();
               if (this.read_Z() && this.write_Z()) {
                  JsonObject args = AiLocalRequest.clampCount(
                     step.arguments(), AiItems.countInInventory(AiItems.resolveItem(AiJson.getString(step.arguments(), "item", ""))), this.write_I()
                  );
                  step = new AiIntents.IntentStep(step.clause() + " more", step.capability(), args, step.describe());
               }

               String label = this.farmPlacement != null
                  ? this.selectedTarget.label() + " / " + this.farmPlacement.getStatus()
                  : (
                     this.schematicPlacement == null
                        ? step.clause()
                        : "Build "
                           + this.selectedTarget.label()
                           + " at "
                           + this.schematicOriginText()
                           + " ("
                           + this.schematicPlacement.getRotation()
                           + " degrees, mirror "
                           + this.schematicPlacement.getMirror()
                           + ")"
                  );
               this.service.submitBuiltInAction(label, step.capability(), step.arguments());
               if (this.farmPlacement != null) {
                  this.farmPlacement.cancel();
                  this.farmPlacement = null;
               }

               if (this.schematicPlacement != null) {
                  this.schematicPlacement.handOff();
                  this.schematicPlacement = null;
               }

               this.resetToActions();
               this.onRun.run();
               this.view.invalidate();
            } catch (RuntimeException e) {
               this.issue = e.getMessage();
            }
         }
      }
   }

   void confirm() {
      if (this.selectedAction == AiActionMenu.MenuAction.FARM && this.farmBuild) {
         this.runSelected();
      } else if (this.schematicPlacement != null && this.schematicPlacement.isReadyToConfirm()) {
         aP.init_V_bW_Runnable(this.schematicPlacement, () -> {});
      } else if (this.farmPlacement != null) {
         aP.init_V_E_Runnable(this.farmPlacement, () -> {});
      }
   }

   void confirmPlacement() {
      if (this.schematicPlacement != null) {
         this.schematicPlacement.confirmPlacement();
      }
   }

   void update_V() {
      this.pollReview();
   }

   JsonObject statusJson() {
      return this.farmPlacement != null ? this.farmPlacement.statusJson() : (this.schematicPlacement == null ? new JsonObject() : this.schematicPlacement.statusJson());
   }

   private boolean write_Z() {
      return this.selectedAction == AiActionMenu.MenuAction.GET || this.extraItems;
   }

   private void stopTask() {
      this.service.stopTask();
      this.view.invalidate();
   }

   private void pickAttachment() {
      if (!this.init_Z) {
         this.init_Z = true;
         CompletableFuture.<AiAttachments.Inner_DataUtil>supplyAsync(() -> {
            try {
               String path = AiAttachments.pickFile();
               return path != null && !path.isBlank() ? AiAttachments.attach(Path.of(path), AiSchematic.directory()) : null;
            } catch (Exception e) {
               throw new CompletionException(e);
            }
         }).whenComplete((file, error) -> Minecraft.getInstance().execute(() -> {
            this.init_Z = false;
            if (error != null) {
               Throwable cause = error.getCause() == null ? error : error.getCause();
               autismclient.util.bi.write("Import failed: " + cause.getMessage());
            } else if (file != null) {
               this.init_Map.remove(AiActionMenu.MenuAction.BUILD);
               if (this.selectedAction == AiActionMenu.MenuAction.BUILD) {
                  this.searchInput.setValue(file.name());
                  this.host.focusInput(this.searchInput);
               }
            }
         }));
      }
   }

   private static boolean isInWorld() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && mc.level != null;
   }

   private AiActionMenu.TargetOption selectedRow(List<AiActionMenu.TargetOption> rows) {
      return rows.isEmpty() ? null : rows.get(Math.max(0, Math.min(rows.size() - 1, this.selectedIndex)));
   }

   private String searchQueryText() {
      return this.searchInput.getValue().trim().toLowerCase(Locale.ROOT);
   }

   private List<AiActionMenu.TargetOption> filteredActionRows() {
      List<AiActionMenu.TargetOption> base = isInWorld() ? this.entriesFor(this.selectedAction) : null;
      if (base == null) {
         base = List.of();
      }

      if (base != this.lastSearchBase) {
         this.searchList.setItems(base);
         this.lastSearchBase = base;
      }

      String q = this.searchQueryText();
      this.searchList.setQuery(q);
      List<AiActionMenu.TargetOption> filtered = this.searchList.filtered();
      if (filtered != this.filtered || !q.equals(this.init_Str)) {
         List<AiActionMenu.TargetOption> out = new ArrayList<>(filtered);
         if (!q.isEmpty()) {
            out.sort(Comparator.comparingInt(row -> matchRank(row.label().toLowerCase(Locale.ROOT), q)));
         }

         AiActionMenu.TargetOption place = this.selectedAction == AiActionMenu.MenuAction.GOTO
            ? this.typedTarget(this.searchInput.getValue().trim())
            : null;
         if (place != null) {
            out.add(0, place);
         }

         this.filteredRows = List.copyOf(out);
         this.filtered = filtered;
         this.init_Str = q;
      }

      if (!q.equals(this.rowsQuery) || this.selectedAction != this.rowsAction) {
         this.selectedIndex = 0;
         this.listScroll.scrollToTop();
         this.rowsQuery = q;
         this.rowsAction = this.selectedAction;
      }

      if (this.selectedIndex >= this.filteredRows.size()) {
         this.selectedIndex = Math.max(0, this.filteredRows.size() - 1);
      }

      return this.filteredRows;
   }

   private static int matchRank(String label, String q) {
      return label.startsWith(q) ? 0 : (label.contains(" " + q) ? 1 : 2);
   }

   private AiActionMenu.TargetOption typedTarget(String typed) {
      if (!typed.isEmpty() && !typed.chars().noneMatch(Character::isDigit)) {
         String text = "go to " + typed;
         return this.planStep(text, AiActionMenu.MenuAction.GOTO.load_Str) == null
            ? null
            : new AiActionMenu.TargetOption("Go to " + typed, "", autismclient.util.DisplayItemStackFactory.displayStack(Items.COMPASS), n -> text, "", 0, "");
      } else {
         return null;
      }
   }

   private List<AiActionMenu.TargetOption> entriesFor(AiActionMenu.MenuAction a) {
      Minecraft mc = Minecraft.getInstance();
      AiActionMenu.OptionBuild v = this.init_Map.get(a);
      long now = System.currentTimeMillis();
      Object world = mc.getConnection();
      if (v == null || v.world != world || isStale(a, v, now)) {
         List<AiActionMenu.TargetOption> previous = v != null && v.world == world ? v.visible() : null;
         boolean recipes = a == AiActionMenu.MenuAction.GET
            || a == AiActionMenu.MenuAction.CRAFT
            || a == AiActionMenu.MenuAction.SMELT;
         boolean missing = recipes && (a == AiActionMenu.MenuAction.SMELT ? AiToolsSuggest.smeltableIds() : AiToolsSuggest.craftableIds()).isEmpty();
         v = new AiActionMenu.OptionBuild(world, now, this.candidatesFor(a), previous, missing);
         this.init_Map.put(a, v);
      }

      if (v.finished == null) {
         this.buildMoreOptions(a, v);
      }

      return v.visible();
   }

   private static boolean isStale(AiActionMenu.MenuAction a, AiActionMenu.OptionBuild v, long now) {
      return v.finished != null && now - v.builtAt >= 2000L ? a == AiActionMenu.MenuAction.GOTO || a == AiActionMenu.MenuAction.BUILD || v.recipesMissing : false;
   }

   private void buildMoreOptions(AiActionMenu.MenuAction a, AiActionMenu.OptionBuild v) {
      long until = System.nanoTime() + 3000000L;
      boolean dedupe = a == AiActionMenu.MenuAction.GET || a == AiActionMenu.MenuAction.MINE;

      while (v.nextIndex < v.candidates.size()) {
         AiActionMenu.TargetOption row = this.toOption(a, v.candidates.get(v.nextIndex++), dedupe ? v.seenKeys : null);
         if (row != null) {
            v.building.add(row);
         }

         if (System.nanoTime() > until) {
            return;
         }
      }

      v.building.sort(Comparator.comparingInt(AiActionMenu.TargetOption::order).thenComparing(rowx -> rowx.label().toLowerCase(Locale.ROOT)));
      v.finished = List.copyOf(v.building);
   }

   private AiActionMenu.TargetOption toOption(AiActionMenu.MenuAction a, AiActionMenu.TargetCandidate c, Set<String> seen) {
      for (Function<Integer, String> form : c.forms()) {
         AiIntents.IntentStep step = this.planStep(form.apply(null), a.load_Str);
         if (step != null && (c.expect() == null || sameItemId(AiJson.getString(step.arguments(), c.key(), ""), c.expect()))) {
            if (seen != null && !seen.add(stepKey(step, c.key()))) {
               return null;
            }

            int amount;
            try {
               amount = AiJson.getInt(step.arguments(), "count", -1);
            } catch (RuntimeException e) {
               amount = -1;
            }

            return new AiActionMenu.TargetOption(
               c.label(),
               c.label() + " " + c.search(),
               c.icon(),
               form,
               amount > 0 ? Integer.toString(amount) : "Auto",
               c.order(),
               a == AiActionMenu.MenuAction.MINE ? AiJson.getString(step.arguments(), "block", "") : ""
            );
         }
      }

      return null;
   }

   private AiIntents.IntentStep planStep(String text, String capability) {
      try {
         AiIntents.IntentPlan plan = AiIntents.plan(text, this.waypointResolver);
         if (plan.steps().size() == 1 && plan.handledAll()) {
            AiIntents.IntentStep step = plan.steps().get(0);
            return capability.equals(step.capability()) && step.asks().isEmpty() ? step : null;
         } else {
            return null;
         }
      } catch (RuntimeException e) {
         return null;
      }
   }

   private static boolean sameItemId(String a, String b) {
      return a.replace("minecraft:", "").equalsIgnoreCase(b.replace("minecraft:", ""));
   }

   private static String stepKey(AiIntents.IntentStep step, String key) {
      JsonObject args = step.arguments().deepCopy();
      args.remove("count");
      String value = key == null ? null : AiJson.getString(args, key, null);
      if (value != null) {
         try {
            AiItems.ItemSpec target = AiItems.resolve(value);
            args.addProperty(key, String.join(",", target.itemIds()) + "|" + String.join(",", target.blockIds()));
         } catch (RuntimeException var5) {
         }
      }

      return step.capability() + args;
   }

   private List<AiActionMenu.MenuAction> levelEntries() {
      Object level = Minecraft.getInstance().level;
      if (this.cachedLevelEntries != null && this.cachedLevel == level) {
         return this.cachedLevelEntries;
      }

      List<AiActionMenu.MenuAction> out = new ArrayList<>();

      for (AiActionMenu.MenuAction a : AiActionMenu.MenuAction.values()) {
         try {
            if (AiDirectOps.catalogEntry(a.load_Str) == null) {
               continue;
            }
         } catch (RuntimeException e) {
            continue;
         }

         if (a.isDirect() || this.planStep(a.update_Str, a.load_Str) != null) {
            out.add(a);
         }
      }

      this.cachedLevel = level;
      if (out.remove(AiActionMenu.MenuAction.BUILD)) {
         out.add(Math.min(1, out.size()), AiActionMenu.MenuAction.BUILD);
      }

      return this.cachedLevelEntries = List.copyOf(out);
   }

   private List<AiActionMenu.TargetCandidate> candidatesFor(AiActionMenu.MenuAction a) {
      List<AiActionMenu.TargetCandidate> out = new ArrayList<>();
      switch (a) {
         case GET:
            Set<String> craft = new HashSet<>(AiToolsSuggest.craftableIds());
            Set<String> smelt = new HashSet<>(AiToolsSuggest.smeltableIds());
            Set<String> mined = AiObtain.mineableItems();

            for (Item item : BuiltInRegistries.ITEM) {
               if (item != Items.AIR) {
                  Identifier id = BuiltInRegistries.ITEM.getKey(item);
                  String shortId = AiToolsSuggest.shortId(id);
                  boolean route = craft.contains(shortId)
                     || smelt.contains(shortId)
                     || !AiItems.sourceEntitiesFor(item).isEmpty()
                     || mined.contains(id.toString()) && AiItems.sourceBlocksFor(item).stream().anyMatch(AiToolsSuggest::init);
                  if (route) {
                     out.add(itemCandidate("get", item, "item"));
                  }
               }
            }

            sortByLabel(out);
            List<AiActionMenu.TargetCandidate> families = new ArrayList<>();

            for (String family : AiItems.familyNames()) {
               families.add(familyCandidate("get", family, "item"));
            }

            sortByLabel(families);
            out.addAll(families);
            break;
         case MINE:
            List<AiActionMenu.TargetCandidate> familiesMine = new ArrayList<>();

            for (String id : AiToolsSuggest.write()) {
               Block block = blockById(id);
               if (block != null) {
                  out.add(blockCandidate("mine", block));
               } else {
                  familiesMine.add(familyCandidate("mine", id, "block"));
               }
            }

            sortByLabel(out);
            sortByLabel(familiesMine);
            out.addAll(familiesMine);
            break;
         case CHOP:
            out.add(
               new AiActionMenu.TargetCandidate(
                  "Nearest tree",
                  "tree log wood",
                  autismclient.util.DisplayItemStackFactory.displayStack(Items.OAK_SAPLING),
                  List.of(n -> "chop " + amountPrefix(n) + "logs"),
                  "block",
                  null,
                  0
               )
            );

            for (String id : AiToolsSuggest.update()) {
               Block block = id.endsWith("_log") && !id.startsWith("stripped_") ? blockById(id) : null;
               if (block != null) {
                  Identifier key = BuiltInRegistries.BLOCK.getKey(block);
                  String species = key.getPath().substring(0, key.getPath().length() - 4).replace('_', ' ');
                  out.add(
                     new AiActionMenu.TargetCandidate(
                        autismclient.util.RegistryDisplayNameCache.blockName(key.toString()),
                        species + " tree wood",
                        autismclient.util.DisplayItemStackFactory.forBlock(block),
                        List.of(n -> "chop " + amountPrefix(n) + species + " logs"),
                        "block",
                        key.toString(),
                        1
                     )
                  );
               }
            }
            break;
         case CRAFT:
            for (String id : AiToolsSuggest.craftableIds()) {
               Item item = itemById(id);
               if (item != null) {
                  out.add(itemCandidate("craft", item, "item"));
               }
            }
            break;
         case SMELT:
            for (String id : AiToolsSuggest.smeltableIds()) {
               Item item = itemById(id);
               if (item != null) {
                  out.add(itemCandidate("smelt", item, "item"));
               }
            }
            break;
         case GOTO:
            Minecraft mc = Minecraft.getInstance();

            for (AutismWaypoints.Waypoint wp : AutismWaypoints.getInstance().list(AuditWaypointScope.current(mc))) {
               String name = wp.name();
               out.add(
                  new AiActionMenu.TargetCandidate(
                     name + " (" + wp.x() + " " + wp.y() + " " + wp.z() + ")",
                     name,
                     autismclient.util.DisplayItemStackFactory.displayStack(wp.death() ? Items.RECOVERY_COMPASS : Items.COMPASS),
                     List.of(n -> "go to " + name),
                     null,
                     null,
                     1
                  )
               );
            }
            break;
         case FIGHT:
            for (String id : AiToolsSuggest.sync()) {
               if (id.equals("hostiles")) {
                  out.add(
                     new AiActionMenu.TargetCandidate(
                        "Hostile mobs",
                        "hostiles monsters",
                        autismclient.util.DisplayItemStackFactory.displayStack(Items.ZOMBIE_HEAD),
                        List.of(n -> "fight " + amountPrefix(n) + "hostiles"),
                        "target",
                        "hostiles",
                        0
                     )
                  );
               } else {
                  Identifier key = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
                  if (key != null) {
                     String words = key.getPath().replace('_', ' ');
                     EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
                     out.add(
                        new AiActionMenu.TargetCandidate(
                           autismclient.util.RegistryDisplayNameCache.entityName(key.toString()),
                           words,
                           type == null ? ItemStack.EMPTY : spawnEggIcon(type),
                           List.of(n -> "fight " + amountPrefix(n) + words),
                           "target",
                           key.toString(),
                           1
                        )
                     );
                  }
               }
            }
            break;
         case FARM:
            for (String option : AiIntents.CROPS) {
               String crop = option.toLowerCase(Locale.ROOT);
               String one = AiIntents.singular(crop);
               String id = AiIntents.cropId(crop);
               Block block = id == null ? null : blockById(id);
               ItemStack icon = block == null ? ItemStack.EMPTY : autismclient.util.DisplayItemStackFactory.forBlock(block);
               out.add(
                  new AiActionMenu.TargetCandidate(
                     "Build " + one + " farm",
                     crop + " farm build",
                     icon,
                     List.of(n -> "build a " + one + " farm" + (n == null ? "" : " of " + n + " crops")),
                     "crop",
                     id,
                     0
                  )
               );
               out.add(
                  new AiActionMenu.TargetCandidate(
                     "Harvest " + crop, crop + " harvest", icon, List.of(n -> "harvest " + amountPrefix(n) + crop), "crop", id, 1
                  )
               );
            }
            break;
         case BUILD:
            Path dir = AiSchematic.directory();
            if (Files.isDirectory(dir)) {
               try (Stream<Path> files = Files.list(dir)) {
                  files.filter(x$0 -> Files.isRegularFile(x$0))
                     .map(p -> p.getFileName().toString())
                     .filter(AiActionMenu::isSchematicFile)
                     .forEach(
                        namex -> out.add(
                           new AiActionMenu.TargetCandidate(namex, namex, ItemStack.EMPTY, List.of(n -> "build \"" + namex + "\""), "schematic", namex, 1)
                        )
                     );
               } catch (Exception var18) {
               }
            }
      }

      return out;
   }

   private static boolean isSchematicFile(String name) {
      String lower = name.toLowerCase(Locale.ROOT);

      for (String extension : AiSchematic.EXTENSIONS) {
         if (lower.endsWith(extension)) {
            return true;
         }
      }

      return false;
   }

   private static String amountPrefix(Integer n) {
      return n == null ? "" : n + " ";
   }

   private static void sortByLabel(List<AiActionMenu.TargetCandidate> list) {
      list.sort(Comparator.comparing(c -> c.label().toLowerCase(Locale.ROOT)));
   }

   private static AiActionMenu.TargetCandidate itemCandidate(String verb, Item item, String key) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      String path = id.getPath();
      String words = path.replace('_', ' ');
      return new AiActionMenu.TargetCandidate(
         autismclient.util.RegistryDisplayNameCache.itemName(id.toString()),
         words,
         autismclient.util.DisplayItemStackFactory.displayStack(item),
         List.of(n -> verb + " " + amountPrefix(n) + words, n -> verb + " " + amountPrefix(n) + path),
         key,
         id.toString(),
         1
      );
   }

   private static AiActionMenu.TargetCandidate blockCandidate(String verb, Block block) {
      Identifier id = BuiltInRegistries.BLOCK.getKey(block);
      String path = id.getPath();
      String words = path.replace('_', ' ');
      return new AiActionMenu.TargetCandidate(
         autismclient.util.RegistryDisplayNameCache.blockName(id.toString()),
         words,
         autismclient.util.DisplayItemStackFactory.forBlock(block),
         List.of(n -> verb + " " + amountPrefix(n) + words, n -> verb + " " + amountPrefix(n) + path),
         "block",
         id.toString(),
         1
      );
   }

   private static AiActionMenu.TargetCandidate familyCandidate(String verb, String family, String key) {
      String words = family.replace('_', ' ');
      ItemStack icon = ItemStack.EMPTY;

      try {
         AiItems.ItemSpec target = AiItems.resolve(family);
         if (!target.items().isEmpty()) {
            icon = autismclient.util.DisplayItemStackFactory.displayStack(target.items().iterator().next());
         }
      } catch (RuntimeException var6) {
      }

      return new AiActionMenu.TargetCandidate(
         "Any " + words,
         words + " any",
         icon,
         List.of(n -> verb + " " + amountPrefix(n) + words, n -> verb + " " + amountPrefix(n) + family),
         key,
         null,
         1
      );
   }

   private static Item itemById(String id) {
      Identifier key = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
      return key == null ? null : BuiltInRegistries.ITEM.getOptional(key).filter(item -> item != Items.AIR).orElse(null);
   }

   private static Block blockById(String id) {
      Identifier key = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
      return key == null ? null : BuiltInRegistries.BLOCK.getOptional(key).orElse(null);
   }

   private static ItemStack spawnEggIcon(EntityType<?> type) {
      if (spawnEggIcons == null) {
         Map<EntityType<?>, ItemStack> map = new HashMap<>();

         for (Item item : BuiltInRegistries.ITEM) {
            TypedEntityData<EntityType<?>> data = item.components().get(DataComponents.ENTITY_DATA);
            if (data != null) {
               map.putIfAbsent(data.type(), autismclient.util.DisplayItemStackFactory.displayStack(item));
            }
         }

         spawnEggIcons = map;
      }

      return spawnEggIcons.getOrDefault(type, ItemStack.EMPTY);
   }

   enum MenuStep {
      HOME,
      TARGET,
      OPTIONS,
      REVIEW;
   }

   private record TargetOption(String label, String search, ItemStack icon, Function<Integer, String> request, String fallback, int order, String mineKey) {
   }

   private static final class OptionBuild {
      final Object world;
      final long builtAt;
      final List<AiActionMenu.TargetCandidate> candidates;
      final List<AiActionMenu.TargetOption> previous;
      final List<AiActionMenu.TargetOption> building = new ArrayList<>();
      final Set<String> seenKeys = new HashSet<>();
      final boolean recipesMissing;
      int nextIndex;
      List<AiActionMenu.TargetOption> finished;

      OptionBuild(
         Object world, long builtAt, List<AiActionMenu.TargetCandidate> candidates, List<AiActionMenu.TargetOption> previous, boolean recipesMissing
      ) {
         this.world = world;
         this.builtAt = builtAt;
         this.candidates = candidates;
         this.previous = previous;
         this.recipesMissing = recipesMissing;
      }

      List<AiActionMenu.TargetOption> visible() {
         return this.finished != null ? this.finished : this.previous;
      }
   }

   enum MenuAction {
      GET("Get", "game:obtain", true, "Search items", "Obtain any item"),
      MINE("Mine", "game:mine", true, "Search blocks", "Mine nearby block types"),
      CHOP("Chop", "game:chop", true, "Search trees", "Fell trees for logs"),
      CRAFT("Craft", "game:craft", true, "Search recipes", "Craft an item"),
      SMELT("Smelt", "game:smelt_auto", true, "Search smelt results", "Smelt in a furnace"),
      GOTO("Go to", "game:goto", false, "Waypoint or x y z", "Walk to a place"),
      FIGHT("Fight", "game:fight", true, "Search mobs", "Fight a mob type"),
      FARM("Farm", "game:farm", true, "Search crops", "Build or harvest farms"),
      BUILD("Schematic", "game:build", false, "Search schematics", "Preview and build a schematic"),
      EAT("Eat", "game:eat", "eat", "Eat the best food"),
      SLEEP("Sleep", "game:sleep", "sleep", "Sleep in a bed"),
      FISH("Fish", "game:fish", "fish", "Fish until a catch"),
      TIDY("Tidy", "game:tidy", "clean my inventory", "Drop junk, keep valuables"),
      SURFACE("Surface", "game:surface", "surface", "Climb to the surface"),
      RECOVER("Recover", "game:recover", "recover my items", "Pick up death drops"),
      BEAT("Beat the game", "game:speedrun", "beat the game", "Play until the dragon dies");

      final String init_Str;
      final String load_Str;
      final String read_Str;
      final String write_Str;
      final String update_Str;
      final boolean init_Z;

      MenuAction(String label, String capability, boolean counted, String hint, String tip) {
         this.init_Str = label;
         this.load_Str = capability;
         this.init_Z = counted;
         this.read_Str = hint;
         this.write_Str = tip;
         this.update_Str = null;
      }

      MenuAction(String label, String capability, String fixed, String tip) {
         this.init_Str = label;
         this.load_Str = capability;
         this.init_Z = false;
         this.read_Str = "";
         this.write_Str = tip;
         this.update_Str = fixed;
      }

      boolean isDirect() {
         return this.update_Str == null;
      }
   }

   private record TargetCandidate(String label, String search, ItemStack icon, List<Function<Integer, String>> forms, String key, String expect, int order) {
   }
}
