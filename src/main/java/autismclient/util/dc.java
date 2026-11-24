package autismclient.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.LongSupplier;

public final class dc {
   private static final int init_I = 64;
   private final LongSupplier init_LongSupplier;
   private final int load;
   private final Map<Long, dc.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private int read;

   public dc(LongSupplier clock) {
      this(clock, 64);
   }

   dc(LongSupplier clock, int terminalRetention) {
      this.init_LongSupplier = clock;
      this.load = Math.max(0, terminalRetention);
   }

   public synchronized dc.Inner_StreamUtil init(long id, String owner, long timeoutMs) {
      if (this.init_Map.containsKey(id)) {
         throw new IllegalArgumentException("duplicate job id: " + id);
      }

      dc.Inner_StreamUtil state = new dc.Inner_StreamUtil(
         id, owner == null ? "" : owner, dc.Inner_DataUtil.init, true, false, this.init_LongSupplier.getAsLong() + Math.max(1L, timeoutMs)
      );
      this.init_Map.put(id, state);
      return state;
   }

   public synchronized boolean init_Z(long id) {
      dc.Inner_StreamUtil state = this.init_Map.get(id);
      if (state != null && !state.init_Z()) {
         this.init_Map.put(id, new dc.Inner_StreamUtil(state.init_J(), state.init_Str(), dc.Inner_DataUtil.init, false, true, state.load_J()));
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean init(long id, dc.Inner_DataUtil completion) {
      dc.Inner_StreamUtil state = this.init_Map.get(id);
      if (state != null && !state.init_Z() && completion != null && completion != dc.Inner_DataUtil.init) {
         this.init_Map.put(id, new dc.Inner_StreamUtil(state.init_J(), state.init_Str(), completion, false, false, state.load_J()));
         this.read++;
         this.init_V();
         return true;
      } else {
         return false;
      }
   }

   private void init_V() {
      Iterator<Entry<Long, dc.Inner_StreamUtil>> iterator = this.init_Map.entrySet().iterator();

      while (this.read > this.load && iterator.hasNext()) {
         if (iterator.next().getValue().init_Z()) {
            iterator.remove();
            this.read--;
         }
      }
   }

   public synchronized dc.Inner_StreamUtil init_dc_StreamUtil(long id) {
      dc.Inner_StreamUtil state = this.init_Map.get(id);
      return state == null ? new dc.Inner_StreamUtil(id, "", dc.Inner_DataUtil.read, false, false, 0L) : state;
   }

   public synchronized List<Long> init_List() {
      long now = this.init_LongSupplier.getAsLong();
      List<Long> expired = new ArrayList<>();

      for (dc.Inner_StreamUtil state : this.init_Map.values()) {
         if (!state.init_Z() && now >= state.load_J()) {
            expired.add(state.init_J());
         }
      }

      return expired;
   }

   public synchronized List<Long> load() {
      List<Long> pending = new ArrayList<>();

      for (dc.Inner_StreamUtil state : this.init_Map.values()) {
         if (!state.init_Z()) {
            pending.add(state.init_J());
         }
      }

      return pending;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   public record Inner_StreamUtil(long init_J, String init_Str, dc.Inner_DataUtil init_dc_DataUtil, boolean init_Z, boolean load_Z, long load_J) {
      public boolean init_Z() {
         return this.init_dc_DataUtil != dc.Inner_DataUtil.init;
      }

      public boolean load_Z() {
         return this.init_Z;
      }

      public boolean read() {
         return this.load_Z;
      }
   }
}
