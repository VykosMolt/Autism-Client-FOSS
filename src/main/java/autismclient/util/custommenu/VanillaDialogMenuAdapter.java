package autismclient.util.custommenu;

import autismclient.api.custommenu.CustomMenuAdapter;
import autismclient.api.custommenu.CustomMenuButton;
import autismclient.api.custommenu.CustomMenuEvent;
import autismclient.api.custommenu.CustomMenuInput;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.ClickEvent.Custom;
import net.minecraft.network.chat.ClickEvent.OpenUrl;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.ClickEvent.ShowDialog;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.ButtonListDialog;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogListDialog;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.MultiActionDialog;
import net.minecraft.server.dialog.SimpleDialog;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CommandTemplate;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.action.StaticAction;
import net.minecraft.server.dialog.action.Action.ValueGetter;
import net.minecraft.server.dialog.input.BooleanInput;
import net.minecraft.server.dialog.input.NumberRangeInput;
import net.minecraft.server.dialog.input.SingleOptionInput;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.server.dialog.input.NumberRangeInput.RangeInfo;
import net.minecraft.server.dialog.input.SingleOptionInput.Entry;

public final class VanillaDialogMenuAdapter implements CustomMenuAdapter {
   public static final String init = "minecraft:dialog";

   @Override
   public String id() {
      return "minecraft:dialog";
   }

   @Override
   public boolean acceptsInbound(Packet<?> packet) {
      return packet instanceof ClientboundShowDialogPacket || packet instanceof ClientboundClearDialogPacket;
   }

   @Override
   public CustomMenuEvent inspectInbound(Packet<?> packet, String phase) {
      if (packet instanceof ClientboundClearDialogPacket) {
         return CustomMenuEvent.CLEAR;
      } else {
         return packet instanceof ClientboundShowDialogPacket show && show.dialog() != null
            ? CustomMenuEvent.open(load(show.dialog().value(), phase))
            : CustomMenuEvent.NONE;
      }
   }

   public static CustomMenuSnapshot snapshot(Dialog dialog, String phase) {
      return dialog == null ? null : load(dialog, phase);
   }

   private static CustomMenuSnapshot load(Dialog dialog, String phase) {
      List<CustomMenuInput> inputs = new ArrayList<>();
      int inputIndex = 1;

      for (Input input : dialog.common().inputs()) {
         inputs.add(init(inputIndex++, input));
      }

      List<ActionButton> actions = init(dialog);
      List<CustomMenuButton> buttons = new ArrayList<>();

      for (int i = 0; i < actions.size(); i++) {
         buttons.add(init(i + 1, actions.get(i)));
      }

      return new CustomMenuSnapshot(
         "minecraft:dialog", phase, 0L, dialog.common().title().getString(), inputs, buttons, new VanillaDialogMenuAdapter.Inner_DataUtil(dialog, actions)
      );
   }

   private static CustomMenuInput init(int index, Input input) {
      String key = input.key();
      if (input.control() instanceof TextInput text) {
         return new CustomMenuInput(index, key, text.label().getString(), CustomMenuInput.Kind.TEXT, text.initial(), text.maxLength(), 0.0, 0.0, 0.0, List.of());
      } else if (input.control() instanceof BooleanInput bool) {
         return new CustomMenuInput(
            index,
            key,
            bool.label().getString(),
            CustomMenuInput.Kind.BOOLEAN,
            Boolean.toString(bool.initial()),
            0,
            0.0,
            0.0,
            0.0,
            List.of(bool.onFalse(), bool.onTrue())
         );
      } else if (input.control() instanceof NumberRangeInput number) {
         RangeInfo range = number.rangeInfo();
         float initial = range.initial().orElse((range.start() + range.end()) / 2.0F);
         return new CustomMenuInput(
            index,
            key,
            number.label().getString(),
            CustomMenuInput.Kind.NUMBER,
            init(initial),
            0,
            Math.min(range.start(), range.end()),
            Math.max(range.start(), range.end()),
            range.step().orElse(0.0F).floatValue(),
            List.of()
         );
      } else if (input.control() instanceof SingleOptionInput option) {
         String initial = option.initial().orElse(option.entries().getFirst()).id();
         return new CustomMenuInput(
            index, key, option.label().getString(), CustomMenuInput.Kind.OPTION, initial, 0, 0.0, 0.0, 0.0, option.entries().stream().map(Entry::id).toList()
         );
      } else {
         return new CustomMenuInput(index, key, "", CustomMenuInput.Kind.TEXT, "", 0, 0.0, 0.0, 0.0, List.of());
      }
   }

   private static List<ActionButton> init(Dialog dialog) {
      List<ActionButton> result = new ArrayList<>();
      if (dialog instanceof SimpleDialog simple) {
         result.addAll(simple.mainActions());
      } else if (dialog instanceof MultiActionDialog multi) {
         result.addAll(multi.actions());
         multi.exitAction().ifPresent(result::add);
      } else if (dialog instanceof DialogListDialog list) {
         list.dialogs()
            .stream()
            .forEach(
               holder -> result.add(
                  new ActionButton(
                     new CommonButtonData(holder.value().common().computeExternalTitle(), list.buttonWidth()),
                     Optional.of(new StaticAction(new ShowDialog((Holder<Dialog>)holder)))
                  )
               )
            );
         list.exitAction().ifPresent(result::add);
      } else if (dialog instanceof ButtonListDialog list) {
         list.exitAction().ifPresent(result::add);
      }

      return List.copyOf(result);
   }

   private static CustomMenuButton init(int index, ActionButton button) {
      String label = button.button().label().getString();
      String color = init(button.button().label());
      if (button.action().isEmpty()) {
         return new CustomMenuButton(index, label, "", CustomMenuButton.Kind.EMPTY, color);
      }

      Action action = button.action().get();
      String actionId = action instanceof CustomAll custom ? custom.id().toString() : "";
      CustomMenuButton.Kind kind = CustomMenuButton.Kind.OTHER;
      if (action instanceof CustomAll) {
         kind = CustomMenuButton.Kind.CUSTOM;
      } else if (action instanceof CommandTemplate) {
         kind = CustomMenuButton.Kind.COMMAND;
      } else if (action instanceof StaticAction stat) {
         ClickEvent click = stat.value();
         if (click instanceof Custom custom) {
            kind = CustomMenuButton.Kind.CUSTOM;
            actionId = custom.id().toString();
         } else if (click instanceof RunCommand) {
            kind = CustomMenuButton.Kind.COMMAND;
         } else if (click instanceof ShowDialog) {
            kind = CustomMenuButton.Kind.DIALOG;
         } else if (click instanceof OpenUrl) {
            kind = CustomMenuButton.Kind.URL;
         } else if (click instanceof CopyToClipboard) {
            kind = CustomMenuButton.Kind.CLIPBOARD;
         }
      }

      return new CustomMenuButton(index, label, actionId, kind, color);
   }

   private static String init(Component label) {
      if (label == null) {
         return "";
      }

      Style style = label.getStyle();
      TextColor color = style == null ? null : style.getColor();
      if (color != null) {
         return color.serialize().toLowerCase(Locale.ROOT);
      }

      for (Component sibling : label.getSiblings()) {
         String nested = init(sibling);
         if (!nested.isEmpty()) {
            return nested;
         }
      }

      return "";
   }

   @Override
   public CustomMenuSubmitResult submit(CustomMenuSnapshot snapshot, CustomMenuSubmission submission) {
      if (!(snapshot.adapterState() instanceof VanillaDialogMenuAdapter.Inner_DataUtil state)) {
         return CustomMenuSubmitResult.failure("Invalid dialog state");
      } else if (submission != null && submission.button() != null) {
         int index = submission.button().index() - 1;
         if (index >= 0 && index < state.init_List().size()) {
            ActionButton selected = state.init_List().get(index);
            if (selected.action().isEmpty()) {
               return CustomMenuSubmitResult.failure("Dialog button has no action");
            }

            Map<String, ValueGetter> getters = new LinkedHashMap<>();

            for (Input input : state.init_Dialog().common().inputs()) {
               String raw = submission.values().get(input.key());
               getters.put(input.key(), init(input, raw));
            }

            Optional<ClickEvent> click = selected.action().get().createAction(getters);
            if (click.isEmpty()) {
               return CustomMenuSubmitResult.failure("Dialog action produced no response");
            } else if (click.get() instanceof Custom custom) {
               return CustomMenuSubmitResult.packets(List.of(new ServerboundCustomClickActionPacket(custom.id(), custom.payload())));
            } else if (click.get() instanceof RunCommand command) {
               if (!"PLAY".equalsIgnoreCase(snapshot.phase())) {
                  return CustomMenuSubmitResult.failure("Commands cannot be sent during configuration");
               }

               String value = command.command();
               if (value.startsWith("/")) {
                  value = value.substring(1);
               }

               return CustomMenuSubmitResult.packets(List.of(new ServerboundChatCommandPacket(value)));
            } else {
               return click.get() instanceof ShowDialog nested
                  ? CustomMenuSubmitResult.replacement(load(nested.dialog().value(), snapshot.phase()), click.get())
                  : CustomMenuSubmitResult.failure("Dialog action is not safe for automation");
            }
         } else {
            return CustomMenuSubmitResult.failure("Dialog button is unavailable");
         }
      } else {
         return CustomMenuSubmitResult.failure("No dialog button selected");
      }
   }

   private static ValueGetter init(Input input, String supplied) {
      if (input.control() instanceof TextInput text) {
         String value = supplied == null ? text.initial() : supplied;
         if (value.length() > text.maxLength()) {
            throw new IllegalArgumentException("Input '" + input.key() + "' exceeds max length");
         } else {
            return init(StringTag.escapeWithoutQuotes(value), StringTag.valueOf(value));
         }
      } else if (input.control() instanceof BooleanInput bool) {
         boolean value = supplied == null ? bool.initial() : init_Z(input.key(), supplied);
         return init(value ? bool.onTrue() : bool.onFalse(), ByteTag.valueOf(value));
      } else if (input.control() instanceof NumberRangeInput number) {
         RangeInfo range = number.rangeInfo();
         float value = supplied == null ? range.initial().orElse((range.start() + range.end()) / 2.0F) : init_F(input.key(), supplied);
         float min = Math.min(range.start(), range.end());
         float max = Math.max(range.start(), range.end());
         if (!(value < min) && !(value > max)) {
            if (range.step().isPresent()) {
               float origin = range.initial().orElse(range.start());
               float quotient = (value - origin) / range.step().get();
               if (Math.abs(quotient - Math.round(quotient)) > 1.0E-4F) {
                  throw new IllegalArgumentException("Input '" + input.key() + "' does not match its step");
               }
            }

            return init(init(value), FloatTag.valueOf(value));
         } else {
            throw new IllegalArgumentException("Input '" + input.key() + "' is outside its range");
         }
      } else if (input.control() instanceof SingleOptionInput option) {
         String value = supplied == null ? option.initial().orElse(option.entries().getFirst()).id() : supplied;
         if (option.entries().stream().noneMatch(entry -> entry.id().equals(value))) {
            throw new IllegalArgumentException("Input '" + input.key() + "' is not a valid option");
         } else {
            return init(value, StringTag.valueOf(value));
         }
      } else {
         throw new IllegalArgumentException("Unsupported input '" + input.key() + "'");
      }
   }

   private static ValueGetter init(final String text, final Tag tag) {
      return new ValueGetter() {
         @Override
         public String asTemplateSubstitution() {
            return text;
         }

         @Override
         public Tag asTag() {
            return tag;
         }
      };
   }

   private static boolean init_Z(String key, String raw) {
      boolean var10000;
      switch (raw.trim().toLowerCase(Locale.ROOT)) {
         case "true":
         case "1":
         case "yes":
         case "on":
            var10000 = true;
            break;
         case "false":
         case "0":
         case "no":
         case "off":
            var10000 = false;
            break;
         default:
            throw new IllegalArgumentException("Input '" + key + "' is not a boolean");
      }

      return var10000;
   }

   private static float init_F(String key, String raw) {
      try {
         return Float.parseFloat(raw.trim());
      } catch (NumberFormatException error) {
         throw new IllegalArgumentException("Input '" + key + "' is not a number");
      }
   }

   private static String init(float value) {
      int integer = (int)value;
      return integer == value ? Integer.toString(integer) : Float.toString(value);
   }

   private record Inner_DataUtil(Dialog init_Dialog, List<ActionButton> init_List) {
   }
}
