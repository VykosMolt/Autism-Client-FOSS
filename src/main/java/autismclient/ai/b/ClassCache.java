package autismclient.ai.b;

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

final class ClassCache {
   private static final int[] init_IArr = new int[0];
   private final int init_I;
   private final int load;
   private final int read;
   private final IntConsumer init_IntConsumer;
   private final Int2ObjectLinkedOpenHashMap<ClassCache.Inner_DataUtil> init_Int2ObjectLinkedOp = new Int2ObjectLinkedOpenHashMap<>();
   private final Long2ObjectOpenHashMap<IntOpenHashSet> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private int write;

   ClassCache(int maxEntries, int maxLinks, int maxDependencies) {
      this(maxEntries, maxLinks, maxDependencies, ignored -> {});
   }

   ClassCache(int maxEntries, int maxLinks, int maxDependencies, IntConsumer evicted) {
      if (maxEntries >= 1 && maxLinks >= 1 && maxDependencies >= 1) {
         this.init_I = maxEntries;
         this.load = maxLinks;
         this.read = Math.min(maxDependencies, maxLinks);
         this.init_IntConsumer = Objects.requireNonNull(evicted);
      } else {
         throw new IllegalArgumentException("Witness cache limits");
      }
   }

   UiHelper.aM init_UiHelper_aM_I(int id) {
      ClassCache.Inner_DataUtil entry = this.init_Int2ObjectLinkedOp.getAndMoveToLast(id);
      return entry == null ? null : entry.witness();
   }

   UiHelper.aM load(int id) {
      ClassCache.Inner_DataUtil entry = this.init_Int2ObjectLinkedOp.get(id);
      return entry == null ? null : entry.witness();
   }

   boolean init_Z_I(int id) {
      return this.init_Int2ObjectLinkedOp.containsKey(id);
   }

   int init_I() {
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

   int init_I_I(int id) {
      ClassCache.Inner_DataUtil entry = this.init_Int2ObjectLinkedOp.get(id);
      return entry == null ? 0 : entry.cells().length;
   }

   UiHelper.aM init(int id, UiHelper.aM witness) {
      UiHelper.aM previous = this.read(id);
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
         this.init_Int2ObjectLinkedOp.put(id, new ClassCache.Inner_DataUtil(witness, indexed));

         for (long cell : indexed) {
            this.init_Long2ObjectOpenHas.computeIfAbsent(cell, ignored -> new IntOpenHashSet()).add(id);
         }

         this.write += indexed.length;
         return previous;
      } else {
         return previous;
      }
   }

   UiHelper.aM read(int id) {
      ClassCache.Inner_DataUtil removed = this.init_Int2ObjectLinkedOp.remove(id);
      if (removed == null) {
         return null;
      }

      this.init(id, removed);
      return removed.witness();
   }

   private void init(int id, ClassCache.Inner_DataUtil entry) {
      for (long cell : entry.cells()) {
         IntOpenHashSet ids = this.init_Long2ObjectOpenHas.get(cell);
         ids.remove(id);
         if (ids.isEmpty()) {
            this.init_Long2ObjectOpenHas.remove(cell);
         }
      }

      this.write = this.write - entry.cells().length;
   }

   void init_V() {
      this.init_Int2ObjectLinkedOp.clear();
      this.init_Long2ObjectOpenHas.clear();
      this.write = 0;
   }

   void init_V_UiHelper_aM(UiHelper.aM marker) {
      ObjectBidirectionalIterator<Entry<ClassCache.Inner_DataUtil>> iterator = this.init_Int2ObjectLinkedOp.int2ObjectEntrySet().fastIterator();

      while (iterator.hasNext()) {
         Entry<ClassCache.Inner_DataUtil> entry = iterator.next();
         if (entry.getValue().witness() == marker) {
            this.init(entry.getIntKey(), entry.getValue());
            iterator.remove();
         }
      }
   }

   int[] init_IArr_J(long cell) {
      IntOpenHashSet ids = this.init_Long2ObjectOpenHas.get(cell);
      return ids == null ? init_IArr : ids.toIntArray();
   }

   Collection<UiHelper.aM> init_Collection() {
      return new AbstractCollection<UiHelper.aM>() {
         @Override
         public int size() {
            return ClassCache.this.init_Int2ObjectLinkedOp.size();
         }

         @Override
         public Iterator<UiHelper.aM> iterator() {
            final ObjectIterator<ClassCache.Inner_DataUtil> iterator = ClassCache.this.init_Int2ObjectLinkedOp.values().iterator();
            return new Iterator<UiHelper.aM>() {
               @Override
               public boolean hasNext() {
                  return iterator.hasNext();
               }

               public UiHelper.aM next() {
                  return this.init();
               }

               public UiHelper.aM init() {
                  return iterator.next().witness();
               }
            };
         }
      };
   }

   private record Inner_DataUtil(UiHelper.aM witness, long[] cells) {
   }
}
