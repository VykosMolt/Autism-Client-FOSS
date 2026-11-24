package autismclient.util.a;

public enum BufferHelper {
   init_BufferHelper("Normal", 8.0, 6.5, 8.0, 1.8, 0.3, 0.1, 5.5, 7.5),
   load_BufferHelper("Jitter", 14.0, 9.0, 12.0, 0.8, 0.2, 0.05, 8.0, 12.0),
   read_BufferHelper("Butterfly", 20.0, 10.0, 15.0, 1.5, 0.28, 0.08, 5.0, 7.5),
   write_BufferHelper("Drag", 20.0, 12.0, 18.0, 1.0, 0.25, 0.06, 11.0, 17.0),
   update_BufferHelper("Scroll", 20.0, 10.0, 16.0, 1.0, 0.25, 0.05, 9.0, 15.0),
   sync_BufferHelper("Rage", 30.0, 20.0, 20.0, 1.0, 0.0, 0.0, 0.0, 0.0);

   public static final double init_D = 30.0;
   public final String init_Str;
   public final double load_D;
   public final double read_D;
   public final double write_D;
   final double update_D;
   final double sync_D;
   final double flush;
   final double close;
   final double open;

   BufferHelper(
      String label, double ceiling, double defaultLow, double defaultHigh, double tempoTau, double tempoSd, double clockSd, double comfortRate, double highRate
   ) {
      this.init_Str = label;
      this.load_D = ceiling;
      this.read_D = defaultLow;
      this.write_D = defaultHigh;
      this.update_D = tempoTau;
      this.sync_D = tempoSd;
      this.flush = clockSd;
      this.close = comfortRate;
      this.open = highRate;
   }

   public boolean init_Z() {
      return this != sync_BufferHelper;
   }

   public static BufferHelper init(String label) {
      if (label != null) {
         for (BufferHelper technique : values()) {
            if (technique.init_Str.equalsIgnoreCase(label.trim())) {
               return technique;
            }
         }
      }

      return init_BufferHelper;
   }

   public static String[] init_StrArr() {
      BufferHelper[] all = values();
      String[] labels = new String[all.length];

      for (int i = 0; i < all.length; i++) {
         labels[i] = all[i].init_Str;
      }

      return labels;
   }
}
