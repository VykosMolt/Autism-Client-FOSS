package autismclient.util.macro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class CandidatePoolSelector {
   private CandidatePoolSelector() {
   }

   public static String init(CandidatePoolSelector.Inner_DataUtil mode, String text) {
      if (mode == CandidatePoolSelector.Inner_DataUtil.REGEX && text != null && !text.isBlank()) {
         try {
            Pattern.compile(text);
            return "";
         } catch (PatternSyntaxException invalid) {
            return "Invalid filter regex";
         }
      } else {
         return "";
      }
   }

   public static List<String> init(List<String> candidates, CandidatePoolSelector.Inner_DataUtil mode, String text) {
      if (candidates != null && !candidates.isEmpty()) {
         CandidatePoolSelector.Inner_DataUtil effective = mode == null ? CandidatePoolSelector.Inner_DataUtil.NONE : mode;
         if (effective != CandidatePoolSelector.Inner_DataUtil.NONE && text != null && !text.isBlank()) {
            if (effective == CandidatePoolSelector.Inner_DataUtil.REGEX) {
               Pattern pattern;
               try {
                  pattern = Pattern.compile(text);
               } catch (PatternSyntaxException invalid) {
                  return List.of();
               }

               List<String> matched = new ArrayList<>();

               for (String candidate : candidates) {
                  if (candidate != null && pattern.matcher(candidate).find()) {
                     matched.add(candidate);
                  }
               }

               return matched;
            } else {
               String needle = text.trim().toLowerCase(Locale.ROOT);
               List<String> matched = new ArrayList<>();

               for (String candidate : candidates) {
                  if (candidate != null) {
                     String lower = candidate.toLowerCase(Locale.ROOT);
                     boolean var10000;
                     switch (effective) {
                        case PREFIX:
                           var10000 = lower.startsWith(needle);
                           break;
                        case SUFFIX:
                           var10000 = lower.endsWith(needle);
                           break;
                        case CONTAINS:
                           var10000 = lower.contains(needle);
                           break;
                        case NOT_CONTAINS:
                           var10000 = !lower.contains(needle);
                           break;
                        default:
                           var10000 = true;
                     }

                     boolean hit = var10000;
                     if (hit) {
                        matched.add(candidate);
                     }
                  }
               }

               return matched;
            }
         } else {
            return candidates;
         }
      } else {
         return List.of();
      }
   }

   public static List<String> init(List<String> pool, String excludeText) {
      if (pool != null && !pool.isEmpty() && excludeText != null && !excludeText.isBlank()) {
         List<String> tokens = new ArrayList<>();

         for (String token : excludeText.split(",")) {
            String trimmed = token.trim().toLowerCase(Locale.ROOT);
            if (!trimmed.isEmpty()) {
               tokens.add(trimmed);
            }
         }

         if (tokens.isEmpty()) {
            return pool;
         }

         List<String> kept = new ArrayList<>(pool.size());

         for (String entry : pool) {
            if (entry != null) {
               String lower = entry.toLowerCase(Locale.ROOT);
               boolean excluded = false;

               for (String token : tokens) {
                  if (lower.contains(token)) {
                     excluded = true;
                     break;
                  }
               }

               if (!excluded) {
                  kept.add(entry);
               }
            }
         }

         return kept;
      } else {
         return pool == null ? List.of() : pool;
      }
   }

   public static String init(String value, CandidatePoolSelector.Inner_DataUtil mode, String text) {
      if (value != null && text != null) {
         String match = text.trim();
         if (match.isEmpty()) {
            return value;
         } else if (mode == CandidatePoolSelector.Inner_DataUtil.PREFIX && value.regionMatches(true, 0, match, 0, match.length())) {
            return value.substring(match.length());
         } else {
            return mode == CandidatePoolSelector.Inner_DataUtil.SUFFIX
                  && value.length() >= match.length()
                  && value.regionMatches(true, value.length() - match.length(), match, 0, match.length())
               ? value.substring(0, value.length() - match.length())
               : value;
         }
      } else {
         return value;
      }
   }

   public static Optional<CandidatePoolSelector.Inner_StreamUtil> init(List<String> pool, CandidatePoolSelector.Inner_CachePool selection, CandidatePoolSelector.Inner_BufferHelper state) {
      return init(pool, selection, 1, state);
   }

   public static Optional<CandidatePoolSelector.Inner_StreamUtil> init(
      List<String> pool, CandidatePoolSelector.Inner_CachePool selection, int position, CandidatePoolSelector.Inner_BufferHelper state
   ) {
      if (pool != null && !pool.isEmpty()) {
         CandidatePoolSelector.Inner_CachePool effective = selection == null ? CandidatePoolSelector.Inner_CachePool.RANDOM : selection;
         int var10000;
         switch (effective) {
            case RANDOM:
               var10000 = state == null ? 0 : ThreadLocalRandom.current().nextInt(pool.size());
               break;
            case SEQUENTIAL:
               if (state == null) {
                  var10000 = 0;
               } else {
                  int next = Math.floorMod(state.init_I, pool.size());
                  state.init_I = next + 1;
                  var10000 = next;
               }
               break;
            case RANDOM_NO_REPEAT:
               if (state == null) {
                  var10000 = 0;
               } else {
                  List<Integer> unused = new ArrayList<>();

                  for (int i = 0; i < pool.size(); i++) {
                     if (!state.init_Set.contains(init(pool.get(i)))) {
                        unused.add(i);
                     }
                  }

                  if (unused.isEmpty()) {
                     state.init_Set.clear();

                     for (int i = 0; i < pool.size(); i++) {
                        unused.add(i);
                     }
                  }

                  int picked = unused.get(ThreadLocalRandom.current().nextInt(unused.size()));
                  state.init_Set.add(init(pool.get(picked)));
                  var10000 = picked;
               }
               break;
            case FIRST:
               var10000 = 0;
               break;
            case LAST:
               var10000 = pool.size() - 1;
               break;
            case POSITION:
               var10000 = Math.max(1, Math.min(position, pool.size())) - 1;
               break;
            default:
               throw new MatchException(null, null);
         }

         int index = var10000;
         return Optional.of(new CandidatePoolSelector.Inner_StreamUtil(pool.get(index), index, pool));
      } else {
         return Optional.empty();
      }
   }

   private static String init(String value) {
      return value == null ? "" : value.toLowerCase(Locale.ROOT);
   }

   public static final class Inner_BufferHelper {
      int init_I;
      final Set<String> init_Set = new HashSet<>();
   }

   public enum Inner_CachePool {
      RANDOM,
      SEQUENTIAL,
      RANDOM_NO_REPEAT,
      FIRST,
      LAST,
      POSITION;
   }

   public enum Inner_DataUtil {
      NONE,
      PREFIX,
      SUFFIX,
      CONTAINS,
      NOT_CONTAINS,
      REGEX;
   }

   public record Inner_StreamUtil(String value, int index, List<String> init_List) {
   }
}
