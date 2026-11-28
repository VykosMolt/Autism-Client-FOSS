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
   private static final int[] EMPTY_IDS = new int[0];
   private final int maxEntries;
   private final int maxLinks;
   private final int maxDependencies;
   private final IntConsumer onEvict;
   private final Int2ObjectLinkedOpenHashMap<PrintWitnessCache.Inner_DataUtil> entries = new Int2ObjectLinkedOpenHashMap<>();
   private final Long2ObjectOpenHashMap<IntOpenHashSet> idsByCell = new Long2ObjectOpenHashMap<>();
   private int linkCount;

   PrintWitnessCache(int maxEntries, int maxLinks, int maxDependencies) {
      this(maxEntries, maxLinks, maxDependencies, ignored -> {});
   }

   PrintWitnessCache(int maxEntries, int maxLinks, int maxDependencies, IntConsumer evicted) {
      if (maxEntries >= 1 && maxLinks >= 1 && maxDependencies >= 1) {
         this.maxEntries = maxEntries;
         this.maxLinks = maxLinks;
         this.maxDependencies = Math.min(maxDependencies, maxLinks);
         this.onEvict = Objects.requireNonNull(evicted);
      } else {
         throw new IllegalArgumentException("Witness cache limits");
      }
   }

   PrintCore.StanceWitness witness(int id) {
      PrintWitnessCache.Inner_DataUtil entry = this.entries.getAndMoveToLast(id);
      return entry == null ? null : entry.witness();
   }

   PrintCore.StanceWitness peek(int id) {
      PrintWitnessCache.Inner_DataUtil entry = this.entries.get(id);
      return entry == null ? null : entry.witness();
   }

   boolean contains(int id) {
      return this.entries.containsKey(id);
   }

   int size() {
      return this.entries.size();
   }

   boolean isEmpty() {
      return this.entries.isEmpty();
   }

   int totalLinks() {
      return this.linkCount;
   }

   int cellCount() {
      return this.idsByCell.size();
   }

   int cellCount(int id) {
      PrintWitnessCache.Inner_DataUtil entry = this.entries.get(id);
      return entry == null ? 0 : entry.cells().length;
   }

   PrintCore.StanceWitness store(int id, PrintCore.StanceWitness witness) {
      PrintCore.StanceWitness previous = this.touch(id);
      if (witness == null) {
         return previous;
      }

      if (witness.required().size() <= this.maxDependencies && witness.keep().size() <= this.maxDependencies) {
         LongOpenHashSet cells = new LongOpenHashSet(Math.min(this.maxDependencies, witness.required().size() + witness.keep().size()));

         for (long cell : witness.required().keySet()) {
            cells.add(cell);
         }

         for (long cell : witness.keep()) {
            cells.add(cell);
            if (cells.size() > this.maxDependencies) {
               return previous;
            }
         }

         while (this.entries.size() >= this.maxEntries || this.linkCount + cells.size() > this.maxLinks) {
            int idToEvict = this.entries.firstIntKey();
            this.touch(idToEvict);
            this.onEvict.accept(idToEvict);
         }

         long[] indexed = cells.toLongArray();
         this.entries.put(id, new PrintWitnessCache.Inner_DataUtil(witness, indexed));

         for (long cell : indexed) {
            this.idsByCell.computeIfAbsent(cell, ignored -> new IntOpenHashSet()).add(id);
         }

         this.linkCount += indexed.length;
         return previous;
      } else {
         return previous;
      }
   }

   PrintCore.StanceWitness touch(int id) {
      PrintWitnessCache.Inner_DataUtil removed = this.entries.remove(id);
      if (removed == null) {
         return null;
      }

      this.unlinkCells(id, removed);
      return removed.witness();
   }

   private void unlinkCells(int id, PrintWitnessCache.Inner_DataUtil entry) {
      for (long cell : entry.cells()) {
         IntOpenHashSet ids = this.idsByCell.get(cell);
         ids.remove(id);
         if (ids.isEmpty()) {
            this.idsByCell.remove(cell);
         }
      }

      this.linkCount = this.linkCount - entry.cells().length;
   }

   void clear() {
      this.entries.clear();
      this.idsByCell.clear();
      this.linkCount = 0;
   }

   void init_V_UiHelper_aM(PrintCore.StanceWitness marker) {
      ObjectBidirectionalIterator<Entry<PrintWitnessCache.Inner_DataUtil>> iterator = this.entries.int2ObjectEntrySet().fastIterator();

      while (iterator.hasNext()) {
         Entry<PrintWitnessCache.Inner_DataUtil> entry = iterator.next();
         if (entry.getValue().witness() == marker) {
            this.unlinkCells(entry.getIntKey(), entry.getValue());
            iterator.remove();
         }
      }
   }

   int[] witnessesAt(long cell) {
      IntOpenHashSet ids = this.idsByCell.get(cell);
      return ids == null ? EMPTY_IDS : ids.toIntArray();
   }

   Collection<PrintCore.StanceWitness> witnesses() {
      return new AbstractCollection<PrintCore.StanceWitness>() {
         @Override
         public int size() {
            return PrintWitnessCache.this.entries.size();
         }

         @Override
         public Iterator<PrintCore.StanceWitness> iterator() {
            final ObjectIterator<PrintWitnessCache.Inner_DataUtil> iterator = PrintWitnessCache.this.entries.values().iterator();
            return new Iterator<PrintCore.StanceWitness>() {
               @Override
               public boolean hasNext() {
                  return iterator.hasNext();
               }

               public PrintCore.StanceWitness next() {
                  return this.nextWitness();
               }

               public PrintCore.StanceWitness nextWitness() {
                  return iterator.next().witness();
               }
            };
         }
      };
   }

   private record Inner_DataUtil(PrintCore.StanceWitness witness, long[] cells) {
   }
}
