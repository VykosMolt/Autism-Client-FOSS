package autismclient.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Flow.Subscription;

final class AiHttp {
   private static final HttpClient init_HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15L)).followRedirects(Redirect.NEVER).build();
   static final String init_Str = "This is a Claude sign-in token (Claude Code / Claude app subscription), which Anthropic only accepts inside its own apps. Paste an Anthropic Console API key instead (console.anthropic.com, API keys)";

   private AiHttp() {
   }

   static Map<String, String> init_Map(AiSettings.Provider provider, String credential) {
      String key = credential == null ? "" : credential.trim();
      if (key.isEmpty()) {
         throw new IllegalArgumentException("Enter an API key in AI settings");
      }

      if (key.chars().anyMatch(c -> c <= 32 || c == 127)) {
         throw new IllegalArgumentException("The API credential contains whitespace or control characters; paste the credential again");
      }

      if (provider == AiSettings.Provider.ANTHROPIC) {
         if (init(key)) {
            throw new IllegalArgumentException(
               "This is a Claude sign-in token (Claude Code / Claude app subscription), which Anthropic only accepts inside its own apps. Paste an Anthropic Console API key instead (console.anthropic.com, API keys)"
            );
         } else {
            return Map.of("x-api-key", key, "anthropic-version", "2023-06-01");
         }
      } else {
         return provider == AiSettings.Provider.GEMINI ? Map.of("x-goog-api-key", key) : Map.of("Authorization", "Bearer " + key);
      }
   }

   static boolean init(String key) {
      return key != null && key.trim().startsWith("sk-ant-oat");
   }

   static String init_Str(AiSettings.Provider provider, String endpoint) {
      if (provider == null) {
         throw new IllegalArgumentException("Choose an AI provider");
      }

      String base = endpoint == null ? "" : endpoint.trim();
      if (base.isEmpty()) {
         base = provider.endpoint;
      }

      URI uri;
      try {
         uri = URI.create(base);
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("Invalid provider endpoint URL");
      }

      String scheme = uri.getScheme();
      String host = uri.getHost();
      if ("https".equalsIgnoreCase(scheme)
         || "http".equalsIgnoreCase(scheme) && host != null && Set.of("localhost", "127.0.0.1", "::1", "[::1]").contains(host.toLowerCase(Locale.ROOT))) {
         if (host == null) {
            throw new IllegalArgumentException("Provider endpoint requires a hostname");
         }

         if (uri.getUserInfo() == null && uri.getRawQuery() == null && uri.getRawFragment() == null) {
            base = base.replaceAll("/+$", "");
            String resource = provider == AiSettings.Provider.ANTHROPIC ? "/messages" : (provider == AiSettings.Provider.GEMINI ? "" : "/chat/completions");
            if (!resource.isEmpty() && base.endsWith(resource)) {
               base = base.substring(0, base.length() - resource.length());
            }

            if (provider == AiSettings.Provider.OPENAI && base.endsWith("/responses")) {
               base = base.substring(0, base.length() - "/responses".length());
            }

            if (base.endsWith("/models")) {
               base = base.substring(0, base.length() - 7);
            }

            if ((uri.getPath() == null || uri.getPath().isEmpty() || "/".equals(uri.getPath()))
               && host.equalsIgnoreCase(URI.create(provider.endpoint).getHost())) {
               base = base + URI.create(provider.endpoint).getPath();
            }

            return base;
         } else {
            throw new IllegalArgumentException("Endpoint must not contain credentials, query, or fragment");
         }
      } else {
         throw new IllegalArgumentException("Provider endpoint must use HTTPS (HTTP allowed for localhost)");
      }
   }

   static String init(String raw, String key) {
      String safe = raw == null ? "" : raw;
      if (key != null && !key.isBlank()) {
         safe = safe.replace(key.trim(), "[redacted]");
      }

      return safe.replaceAll("(?i)\\bBearer\\s+[^\\s\\\"'<>]+", "Bearer [redacted]")
         .replaceAll("\\bsk-[A-Za-z0-9_-]+", "[redacted]")
         .replaceAll("\\bAIza[A-Za-z0-9_-]+", "[redacted]")
         .replaceAll("(?i)([?&](?:key|api_key|access_token)=)[^\\s&\\\"'<>]+", "$1[redacted]");
   }

   static boolean load(String text) {
      String m = text == null ? "" : text.toLowerCase(Locale.ROOT);

      for (String needle : List.of(
         "exceeded your current quota",
         "insufficient_quota",
         "billing hard limit",
         "quota exceeded",
         "no quota",
         "out of quota",
         "insufficient balance",
         "no balance",
         "arrearage",
         "purchase credits",
         "no credits remaining",
         "out of credits",
         "insufficient credits",
         "credits exhausted"
      )) {
         if (m.contains(needle)) {
            return true;
         }
      }

      return false;
   }

   static boolean read(String text) {
      String m = text == null ? "" : text.toLowerCase(Locale.ROOT);
      boolean credit = m.contains("credit");
      boolean low = m.contains("too low") || m.contains("to low") || m.contains("is low") || m.contains("insufficient");
      return credit && low || m.contains("plans & billing") || m.contains("plans and billing");
   }

   static String init(AiSettings.Provider provider, int status, String raw, String key) {
      String detail = init(raw, key).replaceAll("\\s+", " ").trim();
      if (detail.length() > 700) {
         detail = detail.substring(0, 700);
      }

      String lower = detail.toLowerCase(Locale.ROOT);
      boolean gemini = provider == AiSettings.Provider.GEMINI;
      String advice;
      if (status == 402 || status != 401 && status != 403 && (load(lower) || read(lower))) {
         advice = gemini && status == 429
            ? "The key is valid but this model is not in its current quota (free tier); pick a Flash model or enable billing in AI Studio."
            : "The key is valid but the account has no credit or quota left; top up or raise the limit at the provider, then retry.";
      } else if (status == 401) {
         advice = provider == AiSettings.Provider.DASHSCOPE
            ? "DashScope rejected the key: it belongs to another region (the scan tries international, China and US) or was revoked; check it in Model Studio."
            : (
               provider == AiSettings.Provider.MOONSHOT
                  ? "Moonshot rejected the key on every host (moonshot.ai, moonshot.cn, Kimi Code); check it on the Kimi Open Platform."
                  : "The provider rejected this key as invalid or revoked; paste it again or create a new one."
            );
      } else if (status == 403) {
         advice = gemini
            ? "This Google API key authenticated but is restricted to another Google API or project; use a Gemini API key from AI Studio."
            : "The key authenticated but has no access to this resource or model; check its permissions and workspace.";
      } else if (status != 400 || !gemini || !lower.contains("api_key_invalid") && !lower.contains("api key not valid")) {
         if (status == 400 && lower.contains("not activated")) {
            advice = "This account has not activated the selected model product; activate it in Model Studio or pick another model.";
         } else if (status == 429) {
            advice = "Rate limited; wait for the window to reset and retry.";
         } else if (status == 404) {
            advice = "This model is not served for this key; scan models and select an available model ID.";
         } else if (status != 400 && status != 422) {
            advice = "Check the provider endpoint and account status.";
         } else {
            advice = "Check the selected model, request options, and output-token limit.";
         }
      } else {
         advice = "Google says this API key is not valid; check it in AI Studio.";
      }

      return provider.name() + " HTTP " + status + (detail.isEmpty() ? "" : ": " + detail) + "; " + advice;
   }

   static HttpResponse<byte[]> init(HttpRequest request, int limit, Runnable checkpoint) throws InterruptedException, TimeoutException, ExecutionException {
      checkpoint.run();
      CompletableFuture<HttpResponse<byte[]>> future = init_HttpClient.sendAsync(request, info -> new AiHttp.Inner_DataUtil(limit));
      long deadline = System.nanoTime() + request.timeout().orElse(Duration.ofSeconds(60L)).toNanos();

      try {
         while (true) {
            checkpoint.run();
            if (System.nanoTime() > deadline) {
               throw new TimeoutException("HTTP response deadline reached");
            }

            try {
               return future.get(100L, TimeUnit.MILLISECONDS);
            } catch (TimeoutException var10) {
            }
         }
      } finally {
         if (!future.isDone()) {
            future.cancel(true);
         }
      }
   }

   private static final class Inner_DataUtil implements BodySubscriber<byte[]> {
      private final BodySubscriber<byte[]> init_HttpResponse_BodyS = BodySubscribers.ofByteArray();
      private final int init_I;
      private long init_J;
      private Subscription init_Flow_Subscription;

      Inner_DataUtil(int limit) {
         this.init_I = limit;
      }

      @Override
      public CompletionStage<byte[]> getBody() {
         return this.init_HttpResponse_BodyS.getBody();
      }

      @Override
      public void onSubscribe(Subscription subscription) {
         this.init_Flow_Subscription = subscription;
         this.init_HttpResponse_BodyS.onSubscribe(subscription);
      }

      public void onNext(List<ByteBuffer> buffers) {
         this.init(buffers);
      }

      public void init(List<ByteBuffer> buffers) {
         for (ByteBuffer b : buffers) {
            this.init_J = this.init_J + b.remaining();
         }

         if (this.init_J > this.init_I) {
            this.init_Flow_Subscription.cancel();
            this.init_HttpResponse_BodyS.onError(new IOException("HTTP body exceeds " + this.init_I + " bytes"));
         } else {
            this.init_HttpResponse_BodyS.onNext(buffers);
         }
      }

      @Override
      public void onError(Throwable error) {
         this.init_HttpResponse_BodyS.onError(error);
      }

      @Override
      public void onComplete() {
         this.init_HttpResponse_BodyS.onComplete();
      }
   }
}
