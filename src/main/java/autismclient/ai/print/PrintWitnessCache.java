package autismclient.ai.print;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.IntConsumer;

final class PrintWitnessCache {
   private static final int[] init_IArr = new int[0];
   private final int init_I;
   private final int load;
   private final int read;
   private final IntConsumer init_IntConsumer;
   private final Int2ObjectLinkedOpenHashMap<PrintWitnessCache.Inner_DataUtil> init_Int2ObjectLinkedOp = new Int2ObjectLinkedOpenHashMap<>();
   private final Long2ObjectOpenHashMap<IntOpenHashSet> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private int write;

   PrintWitnessCache(int maxEntries, int maxLinks, int maxDependencies) {
      this(maxEntries, maxLinks, maxDependencies, ignored -> {});
   }

   PrintWitnessCache(int maxEntries, int maxLinks, int maxDependencies, IntConsumer evicted) {
      if (maxEntries >= 1 && maxLinks >= 1 && maxDependencies >= 1) {
         this.init_I = maxEntries;
         this.load = maxLinks;
         this.read = Math.min(maxDependencies, maxLinks);
         this.init_IntConsumer = Objects.requireNonNull(evicted);
      } else {
         throw new IllegalArgumentException("Witness cache limits");
      }
   }

   PrintCore.aM witness(int id) {
      PrintWitnessCache.Inner_DataUtil entry = this.init_Int2ObjectLinkedOp.getAndMoveToLast(id);
      return entry == null ? null : entry.witness();
   }

   PrintCore.aM load(int id) {
      PrintWitnessCache.Inner_DataUtil entry = this.init_Int2ObjectLinkedOp.get(id);
      return entry == null ? null : entry.witness();
   }

   boolean init_Z_I(int id) {
      return this.init_Int2ObjectLinkedOp.containsKey(id);
   }

   int size() {
      return this.init_Int2ObjectLinkedOp.size();
   }

   boolean init_Z() {
      return this.init_Int2ObjectLinkedOp.isEmpty();
   }

   int load() {
      return this.write;
   }

   int read() {
      return this.init_Long2ObjectOpenHas.size();
   }

   int cellCount(int id) {
      PrintWitnessCache.Inner_DataUtil entry = this.init_Int2ObjectLinkedOp.get(id);
      return entry == null ? 0 : entry.cells().length;
   }

   PrintCore.aM init(int id, PrintCore.aM witness) {
      PrintCore.aM previous = this.read(id);
      if (witness == null) {
         return previous;
      }

      if (witness.required().size() <= this.read && witness.keep().size() <= this.read) {
         LongOpenHashSet cells = new LongOpenHashSet(Math.min(this.read, witness.required().size() + witness.keep().size()));

         for (long cell : witness.required().keySet()) {
            cells.add(cell);
         }

         for (long cell : witness.keep()) {
            cells.add(cell);
            if (cells.size() > this.read) {
               return previous;
            }
         }

         while (this.init_Int2ObjectLinkedOp.size() >= this.init_I || this.write + cells.size() > this.load) {
            int idToEvict = this.init_Int2ObjectLinkedOp.firstIntKey();
            this.read(idToEvict);
            this.init_IntConsumer.accept(idToEvict);
         }

         long[] indexed = cells.toLongArray();
         this.init_Int2ObjectLinkedOp.put(id, new PrintWitnessCache.Inner_DataUtil(witness, indexed));

         for (long cell : indexed) {
            this.init_Long2ObjectOpenHas.computeIfAbsent(cell, ignored -> new IntOpenHashSet()).add(id);
         }

         this.write += indexed.length;
         return previous;
      } else {
         return previous;
      }
   }

   PrintCore.aM read(int id) {
      PrintWitnessCache.Inner_DataUtil removed = this.init_Int2ObjectLinkedOp.remove(id);
      if (removed == null) {
         return null;
      }

      this.init(id, removed);
      return removed.witness();
   }

   private void init(int id, PrintWitnessCache.Inner_DataUtil entry) {
      for (long cell : entry.cells()) {
         IntOpenHashSet ids = this.init_Long2ObjectOpenHas.get(cell);
         ids.remove(id);
         if (ids.isEmpty()) {
            this.init_Long2ObjectOpenHas.remove(cell);
         }
      }

      this.write = this.write - entry.cells().length;
   }

   void clear() {
      this.init_Int2ObjectLinkedOp.clear();
      this.init_Long2ObjectOpenHas.clear();
      this.write = 0;
   }

   void init_V_UiHelper_aM(PrintCore.aM marker) {
      ObjectBidirectionalIterator<Entry<PrintWitnessCache.Inner_DataUtil>> iterator = this.init_Int2ObjectLinkedOp.int2ObjectEntrySet().fastIterator();

      while (iterator.hasNext()) {
         Entry<PrintWitnessCache.Inner_DataUtil> entry = iterator.next();
         if (entry.getValue().witness() == marker) {
            this.init(entry.getIntKey(), entry.getValue());
            iterator.remove();
         }
      }
   }

   int[] witnessesAt(long cell) {
      IntOpenHashSet ids = this.init_Long2ObjectOpenHas.get(cell);
      return ids == null ? init_IArr : ids.toIntArray();
   }

   Collection<PrintCore.aM> init_Collection() {
      return new AbstractCollection<PrintCore.aM>() {
         @Override
         public int size() {
            return PrintWitnessCache.this.init_Int2ObjectLinkedOp.size();
         }

         @Override
         public Iterator<PrintCore.aM> iterator() {
            final ObjectIterator<PrintWitnessCache.Inner_DataUtil> iterator = PrintWitnessCache.this.init_Int2ObjectLinkedOp.values().iterator();
            return new Iterator<PrintCore.aM>() {
               @Override
               public boolean hasNext() {
                  return iterator.hasNext();
               }

               public PrintCore.aM next() {
                  return this.init();
               }

               public PrintCore.aM init() {
                  return iterator.next().witness();
               }
            };
         }
      };
   }

   private record Inner_DataUtil(PrintCore.aM witness, long[] cells) {
   }
}
