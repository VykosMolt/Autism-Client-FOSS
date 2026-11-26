package autismclient.gui.d;

import autismclient.api.module.Kind;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.modules.aI;
import autismclient.modules.ap;
import autismclient.util.M;
import autismclient.util.Y;
import autismclient.util.cA;
import autismclient.util.ct;
import autismclient.util.di;
import autismclient.util.dn;
import autismclient.util.k;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class NodeWalker extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 460;
   private static final int load_I = 4;
   private static final int read_I = 16;
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/autism/empty_spawn_egg.png");
   private static final int write_I = -872086265;
   private static final int update = -1441722092;
   private static final int sync = -1441000940;
   private static final int flush = -791321;
   private static final int close = -4743522;
   private static final int open = -50373;
   private final Screen init_Screen;
   private final Module init_Module;
   private final Setting<?, ?> init_Setting;
   private final List<NodeWalker.Inner_DataUtil> init_List;
   private final Map<String, NodeWalker.Inner_DataUtil> init_Map = new HashMap<>();
   private final List<NodeWalker.Inner_StreamUtil> load_List = new ArrayList<>();
   private k init_k;
   private int poll;
   private int push;
   private boolean init_Z;
   private boolean load_Z;
   private int pop;
   private String init_Str;
   private String load_Str;
   private Set<String> init_Set = Set.of();
   private List<NodeWalker.Inner_ConfigStore> read_List = List.of();
   private List<NodeWalker.Inner_DataUtil> write_List = List.of();
   private int tick = -1;
   private int run;

   private static int read_I_10() {
      return ct.init_I_I_ct_DataUtil(-4743522, ct.Inner_DataUtil.TEXT);
   }

   public NodeWalker(Screen parent, Module module, Setting<?, ?> option) {
      super(Component.literal(option == null ? "Select" : "Select " + option.label()));
      this.init_Screen = parent;
      this.init_Module = module;
      this.init_Setting = option;
      this.init_List = this.init_List_Kind(option == null ? Kind.ITEM_LIST : option.kind());

      for (NodeWalker.Inner_DataUtil entry : this.init_List) {
         this.init_Map.put(entry.init_Str, entry);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         this.load_List.clear();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -872086265);
         int x = this.pop();
         int y = this.tick_I_11();
         int panelW = this.run();
         int panelH = this.get();
         this.init(graphics, x, y, panelW, panelH, this.load_Str_3(), mx, my);
         if (panelW < 260 || panelH < 100) {
            this.load(graphics, "Window too small.", x + 4, this.init_I_I_I_6(y + this.sync(), 12), read_I_10(), Math.max(0, panelW - 8));
            return;
         }

         this.init(x + 4, this.close(), panelW - 8, this.flush());
         this.init_k.init_V(graphics, mx, my, delta);
         NodeWalker.Inner_BufferHelper pane = this.init_NodeWalker_BufferH();
         this.init(graphics, pane, pane.init(), "Available", true, mx, my);
         this.init(graphics, pane, pane.load(), "Selected", false, mx, my);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, NodeWalker.Inner_BufferHelper pane, int x, String title, boolean available, int mx, int my) {
      int y = pane.read();
      int w = pane.write();
      int listTop = pane.sync();
      int listH = pane.flush();
      this.init(graphics, x, y, w, pane.update(), 5, -1442248437, init_CharEncoder.set());
      this.load(graphics, title, x + 4, this.init_I_I_I_6(y + 2, this.open()), -791321, w - 8);
      autismclient.gui.f.HashUtil.init_HashUtil().init(graphics, autismclient.gui.f.NetHelper.init(x + 1, listTop, Math.max(0, w - 2), Math.max(0, listH)));

      try {
         if (available) {
            this.init(graphics, x + 3, listTop, w - 12, listH, mx, my);
         } else {
            this.load(graphics, x + 3, listTop, w - 12, listH, mx, my);
         }
      } finally {
         autismclient.gui.f.HashUtil.init_HashUtil().init(graphics);
      }

      autismclient.gui.f.b.PoolEntry.Inner_DataUtil metrics = this.init(available, x, listTop, w, listH);
      autismclient.gui.f.b.PoolEntry.init(graphics, metrics, metrics.init(mx, my), available ? this.init_Z : this.load_Z);
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<NodeWalker.Inner_DataUtil> rows = this.init_List_8();
      int maxScroll = Math.max(0, this.init_I_List_3(rows) - h);
      this.poll = this.init(this.poll, 0, maxScroll);
      int rowY = y - this.poll;
      int rowH = this.poll();
      int groupH = this.push();
      String lastGroup = "";

      for (NodeWalker.Inner_DataUtil entry : rows) {
         if (init_Z_Setting(this.init_Setting) && !entry.read.equals(lastGroup)) {
            if (rowY + groupH > y && rowY < y + h) {
               this.init(graphics, entry.read, x, rowY, w, groupH);
            }

            rowY += groupH;
            lastGroup = entry.read;
         }

         if (rowY + rowH > y && rowY < y + h) {
            this.init(graphics, entry, x, rowY, w, NodeWalker.Inner_CachePool.ADD, entry.init_Str, mx, my, false);
         }

         rowY += rowH;
      }
   }

   private void load(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int mx, int my) {
      List<NodeWalker.Inner_ConfigStore> rows = this.load_List_4();
      int maxScroll = Math.max(0, this.write() - h);
      this.push = this.init(this.push, 0, maxScroll);
      int rowH = this.poll();
      if (rows.isEmpty()) {
         this.load(graphics, "Nothing selected.", x + 4, this.init_I_I_I_6(y, rowH), read_I_10(), w - 8);
      } else {
         int first = this.init(this.push / rowH, 0, Math.max(0, rows.size() - 1));
         int last = this.init((this.push + h - 1) / rowH, 0, rows.size() - 1);

         for (int i = first; i <= last; i++) {
            NodeWalker.Inner_ConfigStore row = rows.get(i);
            int rowY = y - this.push + i * rowH;
            if (rowY + rowH > y && rowY < y + h) {
               this.init(graphics, row.init_NodeWalker_DataUti(), x, rowY, w, NodeWalker.Inner_CachePool.REMOVE, row.init_Str, mx, my, row.init_Z);
            }
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, String label, int x, int y, int w, int groupH) {
      autismclient.gui.f.b.TableIndex.load(graphics, x, y, w, groupH - 1);
      this.load(graphics, label, x + 4, this.init_I_I_I_6(y, groupH - 1), read_I_10(), w - 8);
   }

   private void init(
      GuiGraphicsExtractor graphics,
      NodeWalker.Inner_DataUtil entry,
      int x,
      int y,
      int w,
      NodeWalker.Inner_CachePool type,
      String value,
      int mx,
      int my,
      boolean invalid
   ) {
      String label = entry == null ? value : entry.load;
      String id = entry == null ? value : entry.init_Str;
      int rowH = this.poll();
      boolean over = mx >= x && mx < x + w && my >= y && my < y + rowH;
      autismclient.gui.f.b.TableIndex.init(graphics, x, y, w, rowH - 1, over ? -1441000940 : -1441722092);
      int textX = x + 4;
      if (entry != null && !invalid) {
         this.init(graphics, entry, x + 2, y + Math.max(0, (rowH - 1 - 16) / 2));
         textX = x + 2 + 16 + 4;
      }

      int actionW = 13;
      int actionX = x + w - actionW - 2;
      int actionY = y + (rowH - 1 - actionW) / 2;
      int textMax = actionX - 4 - textX;
      int textY = this.init_I_I_I_6(y, rowH - 1);
      this.load(graphics, label, textX, textY, invalid ? ct.init_I_I_ct_DataUtil(-50373, ct.Inner_DataUtil.DANGER) : -791321, textMax);
      if (!id.equals(label)) {
         int labelW = Math.min(textMax, autismclient.gui.f.b.k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
         int idX = textX + labelW + 4;
         int idMax = textX + textMax - idX;
         if (idMax >= 16) {
            this.load(graphics, id, idX, textY, read_I_10(), idMax);
         }
      }

      if (type == NodeWalker.Inner_CachePool.ADD) {
         autismclient.gui.f.b.FileCache.init(graphics, actionX, actionY, actionW, actionW, "+", over, false);
      } else {
         autismclient.gui.f.b.FileCache.load(graphics, actionX, actionY, actionW, actionW, over);
      }

      this.load_List.add(new NodeWalker.Inner_StreamUtil(type, x, y, w, rowH, value));
   }

   private void init(GuiGraphicsExtractor graphics, NodeWalker.Inner_DataUtil entry, int x, int y) {
      if (entry.init_Z()) {
         graphics.blit(init_Identifier, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
      } else {
         ItemStack stack = entry.init_ItemStack();
         if (stack != null && !stack.isEmpty()) {
            graphics.item(stack, x, y);
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String title, int mx, int my) {
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      int headerH = this.sync();
      autismclient.gui.f.b.IoWorker.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my), bounds, headerH, title, mx >= x && mx < x + width && my >= y && my < y + headerH
      );
      autismclient.gui.f.NetHelper close = autismclient.gui.f.b.IoWorker.init(bounds, headerH);
      this.load_List.add(new NodeWalker.Inner_StreamUtil(NodeWalker.Inner_CachePool.CLOSE, close.read(), close.write(), close.update(), close.sync(), ""));
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.init_k != null && this.init_k.init_Z_D_2(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      autismclient.gui.f.b.PoolEntry.Inner_DataUtil availableMetrics = this.init_PoolEntry_DataUtil_Z(true);
      if (availableMetrics.init_Z() && availableMetrics.init(mx, my)) {
         this.init_Z = true;
         this.pop = availableMetrics.load(mx, my) ? my - availableMetrics.update() : availableMetrics.sync() / 2;
         this.poll = autismclient.gui.f.b.PoolEntry.init(availableMetrics, my, this.pop);
         return true;
      }

      autismclient.gui.f.b.PoolEntry.Inner_DataUtil selectedMetrics = this.init_PoolEntry_DataUtil_Z(false);
      if (selectedMetrics.init_Z() && selectedMetrics.init(mx, my)) {
         this.load_Z = true;
         this.pop = selectedMetrics.load(mx, my) ? my - selectedMetrics.update() : selectedMetrics.sync() / 2;
         this.push = this.init_I_I_10(autismclient.gui.f.b.PoolEntry.init(selectedMetrics, my, this.pop));
         return true;
      }

      for (int i = this.load_List.size() - 1; i >= 0; i--) {
         NodeWalker.Inner_StreamUtil hit = this.load_List.get(i);
         if (hit.init(mx, my)
            && (hit.init_NodeWalker_CachePo != NodeWalker.Inner_CachePool.ADD || this.init_Z_I_I_2(mx, my))
            && (hit.init_NodeWalker_CachePo != NodeWalker.Inner_CachePool.REMOVE || this.load(mx, my))) {
            switch (hit.init_NodeWalker_CachePo) {
               case ADD:
                  this.init_V_Str_6(hit.init_Str);
                  break;
               case REMOVE:
                  this.load_V_2(hit.init_Str);
                  break;
               case CLOSE:
                  this.onClose();
            }

            return true;
         }
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      this.load_Z = false;
      if (this.init_k != null) {
         this.init_k.load(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      }

      return true;
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_Z) {
         this.poll = autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_Z(true), my, this.pop);
         return true;
      } else if (this.load_Z) {
         this.push = this.init_I_I_10(autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_Z(false), my, this.pop));
         return true;
      } else {
         return this.init_k != null && this.init_k.init(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy) ? true : true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      int amount = scrollY < 0.0 ? this.poll() : -this.poll();
      if (this.init_Z_I_I_2(mx, my)) {
         this.poll += amount;
      } else {
         if (!this.load(mx, my)) {
            return true;
         }

         this.push += amount;
      }

      this.poll = this.init(this.poll, 0, this.init_PoolEntry_DataUtil_Z(true).flush());
      this.push = this.init(this.push, 0, this.init_PoolEntry_DataUtil_Z(false).flush());
      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return this.init_k != null && this.init_k.init_Z_KeyEvent(input) ? true : true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.init_k != null) {
         this.init_k.init_Z_CharacterEvent(input);
      }

      return true;
   }

   private void init(int x, int y, int w, int h) {
      if (this.init_k == null) {
         this.init_k = new k(Minecraft.getInstance(), this.font, x, y, w, h, false);
         this.init_k.init_V_Component(Component.literal("Search"));
         this.init_k.init_V_Consumer(value -> this.read_V_10());
         this.init_k.push(true);
      }

      this.init_k.read(x);
      this.init_k.write(y);
      this.init_k.update(w);
      this.init_k.sync(h);
   }

   private String init_Str_6() {
      return this.init_k == null ? "" : this.init_k.load_Str();
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private void init_V_Str_6(String id) {
      List<String> tokens = dn.init(this.init_Module.value(this.init_Setting.id()));
      LinkedHashSet<String> valid = this.init_LinkedHashSet_List(tokens);
      List<String> invalid = this.init_List_List_2(tokens);
      valid.add(id);
      this.init_V_Set_List(valid, invalid);
      this.read_V_10();
   }

   private void load_V_2(String id) {
      List<String> tokens = dn.init(this.init_Module.value(this.init_Setting.id()));
      LinkedHashSet<String> valid = this.init_LinkedHashSet_List(tokens);
      List<String> invalid = this.init_List_List_2(tokens);
      String normalized = di.init(id);
      valid.remove(normalized);
      invalid.removeIf(token -> token.equals(id) || di.init(token).equals(normalized));
      this.init_V_Set_List(valid, invalid);
      this.read_V_10();
   }

   private void init_V_Set_List(Set<String> valid, List<String> invalid) {
      List<String> out = new ArrayList<>(valid);
      out.addAll(invalid);
      String encoded = dn.init(out);
      if (this.init_Screen instanceof HashUtil moduleScreen && moduleScreen.init_Z_5()) {
         this.init_Module.setConfiguredValue(this.init_Setting.id(), encoded);
      } else {
         this.init_Module.setValue(this.init_Setting.id(), encoded);
      }
   }

   private Set<String> init_Set() {
      this.load_V_16();
      return this.init_Set;
   }

   private LinkedHashSet<String> init_LinkedHashSet_List(List<String> tokens) {
      LinkedHashSet<String> out = new LinkedHashSet<>();

      for (String token : tokens) {
         String normalized = di.init(token);
         if (di.init(this.init_Setting.kind(), normalized) && (!this.init_Z_8() || this.init_Map.containsKey(normalized))) {
            out.add(normalized);
         }
      }

      return out;
   }

   private List<String> init_List_List_2(List<String> tokens) {
      if (!this.init_Z_8()) {
         return di.init(this.init_Setting.kind(), tokens);
      }

      List<String> invalid = new ArrayList<>();

      for (String token : tokens) {
         String normalized = di.init(token);
         if (!di.init(this.init_Setting.kind(), normalized) || !this.init_Map.containsKey(normalized)) {
            invalid.add(token);
         }
      }

      return invalid;
   }

   private RegistryListSetting.BlockFilter init_RegistryListSettin() {
      return this.init_Setting instanceof RegistryListSetting registry ? registry.blockFilter() : RegistryListSetting.BlockFilter.NONE;
   }

   private boolean init_Z_8() {
      return this.init_RegistryListSettin() != RegistryListSetting.BlockFilter.NONE;
   }

   private List<NodeWalker.Inner_DataUtil> init_List_8() {
      this.load_V_16();
      return this.write_List;
   }

   private List<NodeWalker.Inner_ConfigStore> load_List_4() {
      this.load_V_16();
      return this.read_List;
   }

   private void load_V_16() {
      String rawValue = this.init_Module.value(this.init_Setting.id());
      String needle = this.init_Str_6().trim().toLowerCase(Locale.ROOT);
      if (!rawValue.equals(this.init_Str) || !needle.equals(this.load_Str)) {
         List<String> tokens = dn.init(this.init_Module.value(this.init_Setting.id()));
         LinkedHashSet<String> selected = this.init_LinkedHashSet_List(tokens);
         List<NodeWalker.Inner_ConfigStore> selectedRows = new ArrayList<>();

         for (String id : selected) {
            NodeWalker.Inner_DataUtil entry = this.init_NodeWalker_DataUti_Str(id);
            selectedRows.add(new NodeWalker.Inner_ConfigStore(id, entry == null ? this.load_NodeWalker_DataUti(id) : entry, false));
         }

         for (String invalid : this.init_List_List_2(tokens)) {
            selectedRows.add(new NodeWalker.Inner_ConfigStore(invalid, this.init_NodeWalker_DataUti_Str_Str("Invalid: " + invalid, invalid), true));
         }

         List<NodeWalker.Inner_DataUtil> available = new ArrayList<>();

         for (NodeWalker.Inner_DataUtil entry : this.init_List) {
            if (!selected.contains(entry.init_Str) && (needle.isEmpty() || entry.init_Str_2().contains(needle) || entry.init_Str.contains(needle))) {
               available.add(entry);
            }
         }

         if (!needle.isEmpty()) {
            available.sort(Comparator.comparingInt(entryx -> init_I_NodeWalker_DataUti_Str(entryx, needle)));
         }

         this.init_Str = rawValue;
         this.load_Str = needle;
         this.init_Set = Set.copyOf(selected);
         this.read_List = List.copyOf(selectedRows);
         this.write_List = List.copyOf(available);
      }
   }

   private NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_Str(String id) {
      return this.init_Map.get(id);
   }

   private static int init_I_NodeWalker_DataUti_Str(NodeWalker.Inner_DataUtil entry, String needle) {
      String path = entry.load();
      int colon = path.indexOf(58);
      if (colon >= 0 && colon + 1 < path.length()) {
         path = path.substring(colon + 1);
      }

      if (path.equals(needle)) {
         return 0;
      } else if (path.startsWith(needle)) {
         return 1;
      } else if (path.contains(needle)) {
         return 2;
      } else {
         String label = entry.init_Str_2();
         if (label.equals(needle)) {
            return 3;
         } else {
            return label.startsWith(needle) ? 4 : 5;
         }
      }
   }

   private List<NodeWalker.Inner_DataUtil> init_List_Kind(Kind type) {
      List<NodeWalker.Inner_DataUtil> out = new ArrayList<>();
      switch (type) {
         case ITEM_LIST:
            BuiltInRegistries.ITEM
               .forEach(
                  item -> {
                     if (item != Items.AIR) {
                        out.add(
                           new NodeWalker.Inner_DataUtil(
                              this.read(BuiltInRegistries.ITEM.getKey(item)),
                              item.getName(item.getDefaultInstance()).getString(),
                              "",
                              M.init_ItemStack(item),
                              false
                           )
                        );
                     }
                  }
               );
            break;
         case BLOCK_LIST:
            BuiltInRegistries.BLOCK.forEach(block -> {
               Identifier id = BuiltInRegistries.BLOCK.getKey(block);
               if (block != Blocks.AIR && block != Blocks.BARRIER && id != null && !id.getPath().endsWith("_wall_banner")) {
                  switch (this.init_RegistryListSettin()) {
                     case NONE:
                        break;
                     case PLACEABLE:
                        if (!aI.init_Z_Block_3(block)) {
                           return;
                        }
                        break;
                     case ORE_SIM:
                        if (!autismclient.util.oresim.CachePool.init(block)) {
                           return;
                        }
                        break;
                     case CROPS:
                        if (!Y.init_Z_Block(block)) {
                           return;
                        }
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  out.add(new NodeWalker.Inner_DataUtil(this.read(id), block.getName().getString(), "", M.init(block), false));
               }
            });
            break;
         case ENTITY_TYPE_LIST:
            Map<EntityType<?>, ItemStack> spawnEggs = this.init_Map();
            BuiltInRegistries.ENTITY_TYPE
               .forEach(
                  typeEntry -> {
                     if (typeEntry != EntityTypes.ITEM) {
                        ItemStack iconx = spawnEggs.getOrDefault(typeEntry, ItemStack.EMPTY);
                        out.add(
                           new NodeWalker.Inner_DataUtil(
                              this.read(BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)typeEntry)),
                              typeEntry.getDescription().getString(),
                              this.init_Str_EntityType((EntityType<?>)typeEntry),
                              iconx,
                              iconx.isEmpty()
                           )
                        );
                     }
                  }
               );
            break;
         case SOUND_EVENT_LIST:
            BuiltInRegistries.SOUND_EVENT
               .forEach(
                  sound -> {
                     Identifier soundId = BuiltInRegistries.SOUND_EVENT.getKey(sound);
                     if (soundId != null) {
                        out.add(
                           new NodeWalker.Inner_DataUtil(
                              this.read(soundId),
                              this.init_Str_Identifier(soundId),
                              this.load(soundId),
                              this.init_ItemStack_Identifier_Map(soundId, this.init_Map()),
                              false
                           )
                        );
                     }
                  }
               );
            break;
         case STORAGE_LIST:
            for (ap.Inner_BufferHelper target : ap.init_List) {
               ItemStack icon = target.init_ItemStack();
               out.add(new NodeWalker.Inner_DataUtil(target.init_Str, target.load_Str, target.read, icon, icon.isEmpty()));
            }
      }

      out.sort(Comparator.<NodeWalker.Inner_DataUtil, String>comparing(entry -> entry.read).thenComparing(NodeWalker.Inner_DataUtil::init_Str_2));
      return out;
   }

   private Map<EntityType<?>, ItemStack> init_Map() {
      Map<EntityType<?>, ItemStack> out = new HashMap<>();
      BuiltInRegistries.ITEM.forEach(item -> {
         TypedEntityData<EntityType<?>> data = item.components().get(DataComponents.ENTITY_DATA);
         if (data != null) {
            out.put(data.type(), M.init_ItemStack(item));
         }
      });
      return out;
   }

   private ItemStack init_ItemStack_Identifier_Map(Identifier soundId, Map<EntityType<?>, ItemStack> spawnEggs) {
      if (soundId == null) {
         return M.init_ItemStack(Items.NOTE_BLOCK);
      }

      String path = soundId.getPath().toLowerCase(Locale.ROOT);
      String normalized = path.replace('.', '_');
      String[] parts = normalized.split("_+");
      ItemStack known = this.init_ItemStack_Str(normalized);
      if (!known.isEmpty()) {
         return known;
      }

      if (path.startsWith("entity.")) {
         ItemStack entity = this.init_ItemStack_StrArr_Map(parts, spawnEggs);
         if (!entity.isEmpty()) {
            return entity;
         }
      }

      if (path.startsWith("block.")) {
         ItemStack block = this.init_ItemStack_StrArr(parts);
         if (!block.isEmpty()) {
            return block;
         }
      }

      if (path.startsWith("item.")) {
         ItemStack item = this.load(parts);
         if (!item.isEmpty()) {
            return item;
         }
      }

      ItemStack registry = this.load(parts, spawnEggs);
      return registry.isEmpty() ? M.init_ItemStack(Items.NOTE_BLOCK) : registry;
   }

   private ItemStack init_ItemStack_Str(String normalizedPath) {
      if (normalizedPath.contains("fishing_bobber")) {
         return M.init_ItemStack(Items.FISHING_ROD);
      } else if (normalizedPath.contains("lava")) {
         return M.init_ItemStack(Items.LAVA_BUCKET);
      } else if (normalizedPath.contains("water") || normalizedPath.contains("rain") || normalizedPath.contains("bubble")) {
         return M.init_ItemStack(Items.WATER_BUCKET);
      } else if (normalizedPath.contains("bucket_empty_lava") || normalizedPath.contains("bucket_fill_lava")) {
         return M.init_ItemStack(Items.LAVA_BUCKET);
      } else if (normalizedPath.contains("bucket_empty") || normalizedPath.contains("bucket_fill")) {
         return M.init_ItemStack(Items.BUCKET);
      } else if (normalizedPath.contains("firework")) {
         return M.init_ItemStack(Items.FIREWORK_ROCKET);
      } else if (normalizedPath.contains("experience_orb")) {
         return M.init_ItemStack(Items.EXPERIENCE_BOTTLE);
      } else if (normalizedPath.contains("lightning") || normalizedPath.contains("thunder")) {
         return M.init_ItemStack(Items.LIGHTNING_ROD.weathering().unaffected());
      } else if (normalizedPath.contains("music_disc_")) {
         return this.load_ItemStack(normalizedPath.substring(normalizedPath.indexOf("music_disc_")));
      } else if (normalizedPath.startsWith("music_") || normalizedPath.startsWith("record_")) {
         return M.init_ItemStack(Items.JUKEBOX);
      } else if (normalizedPath.contains("goat_horn")) {
         return M.init_ItemStack(Items.GOAT_HORN);
      } else if (normalizedPath.contains("note_block")) {
         return M.init_ItemStack(Items.NOTE_BLOCK);
      } else if (normalizedPath.contains("ui_") || normalizedPath.startsWith("ui")) {
         return M.init_ItemStack(Items.COMPASS);
      } else if (normalizedPath.contains("ambient_cave")) {
         return M.init_ItemStack(Items.TORCH);
      } else {
         return normalizedPath.contains("ambient_underwater") ? M.init_ItemStack(Items.WATER_BUCKET) : ItemStack.EMPTY;
      }
   }

   private ItemStack init_ItemStack_StrArr_Map(String[] parts, Map<EntityType<?>, ItemStack> spawnEggs) {
      for (String candidate : this.init_List_StrArr_I(parts, 1)) {
         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(candidate)).orElse(null);
         if (type != null) {
            ItemStack icon = spawnEggs.getOrDefault(type, ItemStack.EMPTY);
            if (!icon.isEmpty()) {
               return icon;
            }
         }
      }

      if (this.init_Z_StrArr_Str(parts, "player")) {
         return M.init_ItemStack(Items.PLAYER_HEAD);
      } else if (this.init_Z_StrArr_Str(parts, "arrow")) {
         return M.init_ItemStack(Items.ARROW);
      } else if (this.init_Z_StrArr_Str(parts, "trident")) {
         return M.init_ItemStack(Items.TRIDENT);
      } else if (this.init_Z_StrArr_Str(parts, "boat")) {
         return M.init_ItemStack(Items.OAK_BOAT);
      } else if (this.init_Z_StrArr_Str(parts, "minecart")) {
         return M.init_ItemStack(Items.MINECART);
      } else {
         return this.init_Z_StrArr_Str(parts, "item") ? M.init_ItemStack(Items.ITEM_FRAME) : ItemStack.EMPTY;
      }
   }

   private ItemStack init_ItemStack_StrArr(String[] parts) {
      for (String candidate : this.init_List_StrArr_I(parts, 1)) {
         ItemStack icon = this.read(candidate);
         if (!icon.isEmpty()) {
            return icon;
         }
      }

      return ItemStack.EMPTY;
   }

   private ItemStack load(String[] parts) {
      for (String candidate : this.init_List_StrArr_I(parts, 1)) {
         ItemStack icon = this.load_ItemStack(candidate);
         if (!icon.isEmpty()) {
            return icon;
         }
      }

      return ItemStack.EMPTY;
   }

   private ItemStack load(String[] parts, Map<EntityType<?>, ItemStack> spawnEggs) {
      for (String candidate : this.init_List_StrArr_I(parts, 0)) {
         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.withDefaultNamespace(candidate)).orElse(null);
         if (type != null) {
            ItemStack icon = spawnEggs.getOrDefault(type, ItemStack.EMPTY);
            if (!icon.isEmpty()) {
               return icon;
            }
         }

         ItemStack item = this.load_ItemStack(candidate);
         if (!item.isEmpty()) {
            return item;
         }

         ItemStack block = this.read(candidate);
         if (!block.isEmpty()) {
            return block;
         }
      }

      return ItemStack.EMPTY;
   }

   private List<String> init_List_StrArr_I(String[] parts, int start) {
      List<String> out = new ArrayList<>();

      for (int len = Math.min(4, parts.length - start); len >= 1; len--) {
         for (int i = start; i + len <= parts.length; i++) {
            String candidate = this.init(parts, i, len);
            if (!candidate.isBlank() && !this.init_Z_Str_2(candidate)) {
               out.add(candidate);
            }
         }
      }

      return out;
   }

   private String init(String[] parts, int start, int len) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < len; i++) {
         String part = parts[start + i];
         if (!part.isBlank()) {
            if (!builder.isEmpty()) {
               builder.append('_');
            }

            builder.append(part);
         }
      }

      return builder.toString();
   }

   private boolean init_Z_Str_2(String candidate) {
      boolean var10000;
      switch (candidate) {
         case "ambient":
         case "angry":
         case "attack":
         case "big":
         case "break":
         case "breathe":
         case "burp":
         case "charge":
         case "click":
         case "clicking":
         case "close":
         case "crack":
         case "death":
         case "dig":
         case "drink":
         case "eat":
         case "empty":
         case "fall":
         case "fill":
         case "flap":
         case "fly":
         case "growl":
         case "hit":
         case "hurt":
         case "idle":
         case "land":
         case "loop":
         case "open":
         case "place":
         case "pop":
         case "ready":
         case "shoot":
         case "small":
         case "splash":
         case "step":
         case "swim":
         case "throw":
         case "use":
         case "walk":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   private boolean init_Z_StrArr_Str(String[] parts, String part) {
      for (String value : parts) {
         if (part.equals(value)) {
            return true;
         }
      }

      return false;
   }

   private ItemStack load_ItemStack(String path) {
      Item item = BuiltInRegistries.ITEM.getOptional(Identifier.withDefaultNamespace(path)).orElse(Items.AIR);
      return item == Items.AIR ? ItemStack.EMPTY : M.init_ItemStack(item);
   }

   private ItemStack read(String path) {
      Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace(path)).orElse(Blocks.AIR);
      return block != Blocks.AIR && block.asItem() != Items.AIR ? M.init(block) : ItemStack.EMPTY;
   }

   private NodeWalker.Inner_DataUtil load_NodeWalker_DataUti(String id) {
      return this.init_NodeWalker_DataUti_Str_Str(id, id);
   }

   private NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_Str_Str(String label, String id) {
      return new NodeWalker.Inner_DataUtil(id, label, "", ItemStack.EMPTY, false);
   }

   private String init_Str_EntityType(EntityType<?> type) {
      MobCategory category = type.getCategory();
      String var10000;
      switch (category) {
         case CREATURE:
            var10000 = "Animals";
            break;
         case WATER_AMBIENT:
         case WATER_CREATURE:
         case UNDERGROUND_WATER_CREATURE:
         case AXOLOTLS:
            var10000 = "Water Animals";
            break;
         case MONSTER:
            var10000 = "Monsters";
            break;
         case AMBIENT:
            var10000 = "Ambient";
            break;
         default:
            var10000 = "Misc";
      }

      return var10000;
   }

   private String init_Str_Identifier(Identifier id) {
      if (id == null) {
         return "";
      }

      String path = id.getPath().replace('.', ' ').replace('_', ' ');
      String[] parts = path.split(" ");
      StringBuilder out = new StringBuilder();

      for (String part : parts) {
         if (!part.isBlank()) {
            if (!out.isEmpty()) {
               out.append(' ');
            }

            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
         }
      }

      return out.isEmpty() ? id.toString() : out.toString();
   }

   private String load(Identifier id) {
      if (id == null) {
         return "";
      } else {
         String path = id.getPath();
         if (path.startsWith("entity.")) {
            return "Entities";
         } else if (path.startsWith("block.")) {
            return "Blocks";
         } else if (path.startsWith("item.")) {
            return "Items";
         } else if (path.startsWith("music.") || path.startsWith("music_disc.") || path.startsWith("record.")) {
            return "Music";
         } else if (path.startsWith("weather.")) {
            return "Weather";
         } else if (path.startsWith("ambient.")) {
            return "Ambient";
         } else {
            return path.startsWith("ui.") ? "UI" : id.getNamespace();
         }
      }
   }

   private String read(Identifier id) {
      return id == null ? "" : id.toString().toLowerCase(Locale.ROOT);
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_Z(boolean available) {
      NodeWalker.Inner_BufferHelper pane = this.init_NodeWalker_BufferH();
      return this.init(available, available ? pane.init() : pane.load(), pane.sync(), pane.write(), pane.flush());
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init(boolean available, int paneX, int listTop, int paneW, int listH) {
      int contentH = available ? this.init_I_List_3(this.init_List_8()) : this.write();
      int scroll = available ? this.poll : this.push;
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(contentH, Math.max(1, listH), paneX + paneW - 6, listTop, 4, Math.max(1, listH), scroll);
   }

   private int init_I_List_3(List<NodeWalker.Inner_DataUtil> rows) {
      int rowH = this.poll();
      int groupH = this.push();
      int height = 0;
      String lastGroup = "";

      for (NodeWalker.Inner_DataUtil entry : rows) {
         if (this.init_Setting != null && init_Z_Setting(this.init_Setting) && !entry.read.equals(lastGroup)) {
            height += groupH;
            lastGroup = entry.read;
         }

         height += rowH;
      }

      return rows.isEmpty() ? 0 : height + 2;
   }

   private int write() {
      int size = this.load_List_4().size();
      return size == 0 ? 0 : size * this.poll() + 2;
   }

   private int update() {
      int key = this.poll() * 1000 + this.push();
      if (key != this.tick) {
         this.tick = key;
         this.run = this.init_I_List_3(this.init_List);
      }

      return Math.max(this.run, this.write());
   }

   private static boolean init_Z_Setting(Setting<?, ?> option) {
      return option.kind() == Kind.ENTITY_TYPE_LIST || option.kind() == Kind.SOUND_EVENT_LIST || option.kind() == Kind.STORAGE_LIST;
   }

   private boolean init_Z_I_I_2(int mx, int my) {
      NodeWalker.Inner_BufferHelper pane = this.init_NodeWalker_BufferH();
      return mx >= pane.init() && mx < pane.init() + pane.write() && my >= pane.sync() && my < pane.sync() + pane.flush();
   }

   private boolean load(int mx, int my) {
      NodeWalker.Inner_BufferHelper pane = this.init_NodeWalker_BufferH();
      return mx >= pane.load() && mx < pane.load() + pane.write() && my >= pane.sync() && my < pane.sync() + pane.flush();
   }

   private NodeWalker.Inner_BufferHelper init_NodeWalker_BufferH() {
      int panelX = this.pop();
      int panelY = this.tick_I_11();
      int paneY = this.close() + this.flush() + 2;
      int paneH = panelY + this.get() - paneY - 4;
      int paneW = (this.run() - 8 - 4) / 2;
      int leftX = panelX + 4;
      int rightX = leftX + paneW + 4;
      int listTop = paneY + 2 + this.open();
      int listH = paneH - 4 - this.open();
      return new NodeWalker.Inner_BufferHelper(leftX, rightX, paneY, paneW, paneH, listTop, listH);
   }

   private int sync() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private int flush() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int close() {
      return this.tick_I_11() + this.sync() + 4;
   }

   private int open() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int poll() {
      return autismclient.gui.f.RenderState.init(18);
   }

   private int push() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int init_I_I_I_6(int boxY, int boxH) {
      return j.init(boxY, boxH, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
   }

   private void read_V_10() {
      this.poll = this.init(this.poll, 0, this.init_PoolEntry_DataUtil_Z(true).flush());
      this.push = this.init(this.push, 0, this.init_PoolEntry_DataUtil_Z(false).flush());
   }

   private int init_I_I_10(int offset) {
      int rowH = this.poll();
      return Math.max(0, offset / rowH) * rowH;
   }

   private String load_Str_3() {
      return this.init_Setting == null ? "Select" : "Select " + this.init_Setting.label();
   }

   private int pop() {
      return autismclient.gui.f.c.NetHelper.load(this.init_I(), this.run(), 6);
   }

   private int tick_I_11() {
      return autismclient.gui.f.c.NetHelper.load(this.load_I(), this.get(), 6);
   }

   private int run() {
      return autismclient.gui.f.c.NetHelper.init(this.init_I(), 6, 460);
   }

   private int get() {
      int minList = 4 * this.poll();
      int maxList = Math.max(minList, (int)(this.load_I() * 0.65F));
      int list = this.init(this.update(), minList, maxList);
      int chrome = this.sync() + 4 + this.flush() + 2 + 4 + this.open() + 4;
      return autismclient.gui.f.c.NetHelper.init(this.load_I(), 6, chrome + list);
   }

   private int init(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int radius, int fill, int border) {
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), radius, fill, border);
   }

   private void load(GuiGraphicsExtractor graphics, String text, int x, int y, int color, int maxWidth) {
      String display = autismclient.gui.f.b.k.init(
         this.font, text == null ? "" : text, Math.max(0, maxWidth), init_CharEncoder.init_Identifier_l(l.read), color
      );
      autismclient.gui.f.b.k.init(graphics, this.font, display, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int w, int h, int color) {
      String display = autismclient.gui.f.b.k.init(this.font, text == null ? "" : text, Math.max(0, w - 4), init_CharEncoder.init_Identifier_l(l.read), color);
      int tw = autismclient.gui.f.b.k.init(this.font, display, init_CharEncoder.init_Identifier_l(l.read), color);
      int th = init_CharEncoder.init_I_l(l.read);
      int drawX = x + Math.max(2, (w - tw) / 2);
      int drawY = y + Math.max(1, (h - th + 1) / 2 + 1);
      autismclient.gui.f.b.k.init(graphics, this.font, display, init_CharEncoder.init_Identifier_l(l.read), color, drawX, drawY, false);
   }

   private record Inner_BufferHelper(int init, int load, int read, int write, int update, int sync, int flush) {
   }

   private enum Inner_CachePool {
      ADD,
      REMOVE,
      CLOSE;
   }

   private record Inner_ConfigStore(String init_Str, NodeWalker.Inner_DataUtil init_NodeWalker_DataUti, boolean init_Z) {
   }

   private record Inner_DataUtil(String init_Str, String load, String read, ItemStack init_ItemStack, boolean init_Z) {
      private Inner_DataUtil {
         load = load != null && !load.isBlank() ? load : init_Str;
         read = read == null ? "" : read;
         init_ItemStack = init_ItemStack == null ? ItemStack.EMPTY : init_ItemStack;
      }

      String init_Str_2() {
         return this.load.toLowerCase(Locale.ROOT);
      }

      public String load() {
         return this.init_Str;
      }

      public String read() {
         return this.load;
      }

      public String write() {
         return this.read;
      }
   }

   private record Inner_StreamUtil(NodeWalker.Inner_CachePool init_NodeWalker_CachePo, int init_I, int load, int read, int write, String init_Str) {
      boolean init(int mx, int my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
      }
   }
}
