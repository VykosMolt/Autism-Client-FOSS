package autismclient.ai;

import java.util.List;
import java.util.Locale;

final class AiKeyResolver {
   private static final List<AiSettings.Provider> RACE_PROVIDERS = List.of(
      AiSettings.Provider.OPENAI, AiSettings.Provider.DASHSCOPE, AiSettings.Provider.MOONSHOT, AiSettings.Provider.DEEPSEEK
   );

   private AiKeyResolver() {
   }

   static List<AiSettings.Provider> providersFor(String key) {
      String k = key == null ? "" : key.trim();
      if (k.startsWith("sk-ant-")) {
         return List.of(AiSettings.Provider.ANTHROPIC);
      } else if (k.startsWith("AIza")) {
         return List.of(AiSettings.Provider.GEMINI);
      } else if (k.startsWith("sk-proj-") || k.startsWith("sk-svcacct-") || k.startsWith("sk-admin-")) {
         return List.of(AiSettings.Provider.OPENAI);
      } else if (!k.startsWith("sk-sp-") && !k.startsWith("sk-ws")) {
         return k.matches("sk-[0-9a-f]{32}")
            ? List.of(AiSettings.Provider.DEEPSEEK, AiSettings.Provider.DASHSCOPE, AiSettings.Provider.MOONSHOT, AiSettings.Provider.OPENAI)
            : RACE_PROVIDERS;
      } else {
         return List.of(AiSettings.Provider.DASHSCOPE);
      }
   }

   static boolean isUnambiguous(String key) {
      return providersFor(key).size() == 1;
   }

   static AiKeyResolver.Inner_DataUtil resolve(String key, AiSettings.Provider chosen, String endpoint, boolean raceOthers, Runnable checkpoint) {
      return resolveWith(key, chosen, endpoint, raceOthers, null, checkpoint);
   }

   static AiKeyResolver.Inner_DataUtil resolveWith(String key, AiSettings.Provider chosen, String endpoint, boolean raceOthers, AiModels.Inner_DataUtil fetch) {
      return resolveWith(key, chosen, endpoint, raceOthers, fetch, () -> {});
   }

   private static AiKeyResolver.Inner_DataUtil resolveWith(
      String key, AiSettings.Provider chosen, String endpoint, boolean raceOthers, AiModels.Inner_DataUtil fetch, Runnable checkpoint
   ) {
      String k = key == null ? "" : key.trim();
      if (k.isEmpty()) {
         return AiKeyResolver.Inner_DataUtil.failed("Add an API key to scan for models");
      }

      boolean custom = !AiSettings.shouldAutofillEndpoint(endpoint);
      // A key is never sent to several vendors: only one that matches a single vendor overrides the choice.
      AiSettings.Provider only = chosen == null || !custom && raceOthers && isUnambiguous(k) ? providersFor(k).getFirst() : chosen;
      AiModels.Inner_CachePool result = probe(only, custom ? endpoint : only.endpoint, k, fetch, checkpoint);
      return result.ok() ? toResolution(only, result) : AiKeyResolver.Inner_DataUtil.failed(result.error());
   }

   private static AiModels.Inner_CachePool probe(AiSettings.Provider p, String endpoint, String key, AiModels.Inner_DataUtil fetch, Runnable checkpoint) {
      return fetch == null ? AiModels.fetchModels(p, endpoint, key, checkpoint) : AiModels.fetchModelsWith(p, endpoint, key, (url, provider, credential) -> {
         checkpoint.run();
         return fetch.get(url, provider, credential);
      });
   }

   private static AiKeyResolver.Inner_DataUtil toResolution(AiSettings.Provider p, AiModels.Inner_CachePool result) {
      String endpoint = result.endpoint() != null && !result.endpoint().isBlank() ? result.endpoint() : p.endpoint;
      return new AiKeyResolver.Inner_DataUtil(p, endpoint, result.models(), pickModel(p, result.models()), "");
   }

   static String displayName(AiSettings.Provider p) {
      String var10000;
      switch (p) {
         case OPENAI:
            var10000 = "OpenAI";
            break;
         case DASHSCOPE:
            var10000 = "DashScope";
            break;
         case MOONSHOT:
            var10000 = "Moonshot";
            break;
         case DEEPSEEK:
            var10000 = "DeepSeek";
            break;
         case GEMINI:
            var10000 = "Gemini";
            break;
         case ANTHROPIC:
            var10000 = "Anthropic";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static String pickModel(AiSettings.Provider p, List<String> models) {
      if (models != null && !models.isEmpty()) {
         String stock = AiSettings.defaultModel(p);
         if (models.contains(stock)) {
            return stock;
         }

         List var10000;
         switch (p) {
            case OPENAI:
               var10000 = List.of("gpt-6", "gpt-5", "gpt-4");
               break;
            case DASHSCOPE:
               var10000 = List.of("qwen3.8-max", "kimi-k3", "kimi/kimi-k3", "qwen3", "qwen-max", "qwen-plus");
               break;
            case MOONSHOT:
               var10000 = List.of("kimi-k3", "kimi-k2", "kimi-");
               break;
            case DEEPSEEK:
               var10000 = List.of("deepseek-flash", "deepseek-v4", "deepseek-chat");
               break;
            case GEMINI:
               var10000 = List.of("gemini-3.8-flash", "gemini-3", "gemini-flash-latest", "gemini-2.5-flash");
               break;
            case ANTHROPIC:
               var10000 = List.of("claude-opus-", "claude-sonnet-", "claude-haiku-");
               break;
            default:
               throw new MatchException(null, null);
         }

         for (String family : (List<String>)var10000) {
            for (String model : models) {
               if (model.toLowerCase(Locale.ROOT).startsWith(family)) {
                  return model;
               }
            }
         }

         return models.getFirst();
      } else {
         return "";
      }
   }

   record Inner_DataUtil(AiSettings.Provider provider, String endpoint, List<String> models, String model, String error) {
      boolean ok() {
         return this.error == null || this.error.isEmpty();
      }

      static AiKeyResolver.Inner_DataUtil failed(String error) {
         return new AiKeyResolver.Inner_DataUtil(null, "", List.of(), "", error);
      }
   }
}
