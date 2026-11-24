package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.StringSetting;

public final class TokenStore extends Module {
   TokenStore() {
      super("auto-sign", "AutoSign", ae.read, "Fills signs with configured text.");
      this.add(new StringSetting("line-1", "Line 1", "").description("Text for sign line one.").build());
      this.add(new StringSetting("line-2", "Line 2", "").description("Text for sign line two.").build());
      this.add(new StringSetting("line-3", "Line 3", "").description("Text for sign line three.").build());
      this.add(new StringSetting("line-4", "Line 4", "").description("Text for sign line four.").build());
      this.add(new BoolSetting("edit-existing", "Also When Editing", false).description("Replace text when editing signs.").build());
      this.add(new BoolSetting("auto-done", "Auto Done", true).description("Close editor after filling text.").build());
   }

   public String[] init_StrArr() {
      return new String[]{this.text("line-1"), this.text("line-2"), this.text("line-3"), this.text("line-4")};
   }

   public boolean init_Z_22() {
      return this.bool("edit-existing");
   }

   public boolean load() {
      return this.bool("auto-done");
   }
}
