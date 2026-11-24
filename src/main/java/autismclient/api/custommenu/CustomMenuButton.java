package autismclient.api.custommenu;

public record CustomMenuButton(int index, String label, String actionId, CustomMenuButton.Kind kind, String labelColor) {
   public CustomMenuButton(int index, String label, String actionId, CustomMenuButton.Kind kind) {
      this(index, label, actionId, kind, "");
   }

   public CustomMenuButton {
      label = label == null ? "" : label;
      actionId = actionId == null ? "" : actionId;
      kind = kind == null ? CustomMenuButton.Kind.OTHER : kind;
      labelColor = labelColor == null ? "" : labelColor;
   }

   public boolean serverRelevant() {
      return this.kind == CustomMenuButton.Kind.CUSTOM || this.kind == CustomMenuButton.Kind.COMMAND || this.kind == CustomMenuButton.Kind.DIALOG;
   }

   public enum Kind {
      CUSTOM,
      COMMAND,
      DIALOG,
      URL,
      CLIPBOARD,
      OTHER,
      EMPTY;
   }
}
