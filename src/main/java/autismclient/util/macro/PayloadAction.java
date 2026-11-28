package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismPayloadSupport;
import autismclient.util.PayloadJsonCodec;
import autismclient.util.PayloadScriptRunner;
import autismclient.util.PayloadFieldCodec;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class PayloadAction implements MacroAction {
   public String channel = "minecraft:brand";
   public String payloadData = "";
   public String payloadJson = "";
   public String payloadClassName = "";
   public String javaSource = "";
   public boolean commandApiRecognized = false;
   public boolean commandApiOverride = false;
   public int commandApiValue = 2147483639;
   public String sourceDirection = "C2S";
   public String sourceProtocol = "";
   public String payloadDirection = "C2S";
   public String payloadPhase = "PLAY";
   public String payloadEncodingMode = "";
   public String payloadFields = "";
   public int payloadPacketId = -1;
   public String payloadProvenance = "userEdited";
   public boolean payloadScriptEnabled = false;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z() && this.enabled) {
         if (mc != null && mc.getConnection() != null) {
            try {
               MacroTemplateResolver.ResolveResult channelResolution = MacroVariableContext.resolveTemplate(this.channel, mc);
               if (!channelResolution.success() || channelResolution.value().isBlank()) {
                  return;
               }

               String resolvedChannel = channelResolution.value().trim();
               String resolvedPayloadData = this.payloadData;
               if (MacroTemplateResolver.containsPlaceholder(this.payloadData)) {
                  MacroTemplateResolver.ResolveResult payloadResolution = MacroVariableContext.resolveTemplate(this.payloadData, mc);
                  if (!payloadResolution.success()) {
                     return;
                  }

                  resolvedPayloadData = payloadResolution.value();
               }

               boolean hasRawPayload = resolvedPayloadData != null && !resolvedPayloadData.isBlank();
               boolean hasStructuredPayload = this.payloadFields != null && !this.payloadFields.isBlank()
                  || this.payloadEncodingMode != null && !this.payloadEncodingMode.isBlank();
               boolean useJsonModel = !hasRawPayload
                  && !hasStructuredPayload
                  && this.payloadJson != null
                  && !this.payloadJson.isBlank()
                  && this.payloadClassName != null
                  && !this.payloadClassName.isBlank();
               String targetChannel = resolvedChannel;
               String sendProtocol = this.payloadPhase != null && !this.payloadPhase.isBlank()
                  ? this.payloadPhase
                  : (this.sourceProtocol == null ? "" : this.sourceProtocol);
               byte[] rawBytes;
               if (hasStructuredPayload) {
                  PayloadFieldCodec.Inner_JsonUtil template = PayloadFieldCodec.fromAction(this);
                  PayloadFieldCodec.Inner_DataUtil built = template.encode();
                  if (!built.isValid()) {
                     throw new IllegalArgumentException(String.join("; ", built.errors()));
                  }

                  if (!MacroTemplateResolver.containsPlaceholder(this.channel)) {
                     targetChannel = template.channel();
                  }

                  rawBytes = built.bytes();
                  if (sendProtocol.isBlank()) {
                     sendProtocol = template.phase().name();
                  }
               } else if (useJsonModel) {
                  PayloadJsonCodec.Inner_DataUtil encoded = PayloadJsonCodec.encode(this);
                  if (!MacroTemplateResolver.containsPlaceholder(this.channel)) {
                     targetChannel = encoded.channel();
                  }

                  rawBytes = encoded.bytes();
               } else {
                  rawBytes = AutismPayloadSupport.parseHexOrBase64(resolvedPayloadData);
               }

               if (!useJsonModel && !hasStructuredPayload && !hasRawPayload && AutismPayloadSupport.isBrandChannel(targetChannel)) {
                  rawBytes = AutismPayloadSupport.encodeMcString(AutismPayloadSupport.defaultClientBrand());
               }

               if (this.commandApiRecognized && this.commandApiOverride) {
                  rawBytes = AutismPayloadSupport.withLeadingBigEndianInt(rawBytes, this.commandApiValue);
               }

               PayloadScriptRunner.Inner_DataUtil context = new PayloadScriptRunner.Inner_DataUtil(
                  targetChannel, rawBytes, this.commandApiRecognized && this.commandApiOverride ? this.commandApiValue : null
               );
               PayloadScriptRunner.Inner_StreamUtil result = PayloadScriptRunner.init(this.payloadScriptEnabled ? this.javaSource : "", context);
               if (AutismPayloadSupport.sendCustomPayload(result.channel(), result.bytes(), sendProtocol)) {
                  AutismClientMessaging.sendPrefixed("Sent payload: " + result.channel());
               }
            } catch (Exception e) {
               AutismClientMessaging.sendPrefixed("§cPayload action failed: " + AutismPayloadSupport.describeError(e));
            }
         } else {
            AutismClientMessaging.sendPrefixed("§cCannot send payload while disconnected.");
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putBoolean("enabled", this.enabled);
      tag.putString("channel", this.channel == null ? "" : this.channel);
      tag.putString("payloadData", this.payloadData == null ? "" : this.payloadData);
      tag.putString("payloadJson", this.payloadJson == null ? "" : this.payloadJson);
      tag.putString("payloadClassName", this.payloadClassName == null ? "" : this.payloadClassName);
      tag.putString("javaSource", this.javaSource == null ? "" : this.javaSource);
      tag.putBoolean("payloadScriptEnabled", this.payloadScriptEnabled);
      tag.putBoolean("commandApiRecognized", this.commandApiRecognized);
      tag.putBoolean("commandApiOverride", this.commandApiOverride);
      tag.putInt("commandApiValue", this.commandApiValue);
      tag.putString("sourceDirection", this.sourceDirection == null ? "" : this.sourceDirection);
      tag.putString("sourceProtocol", this.sourceProtocol == null ? "" : this.sourceProtocol);
      tag.putString("payloadDirection", this.payloadDirection == null ? "" : this.payloadDirection);
      tag.putString("payloadPhase", this.payloadPhase == null ? "" : this.payloadPhase);
      tag.putString("payloadEncodingMode", this.payloadEncodingMode == null ? "" : this.payloadEncodingMode);
      tag.putString("payloadFields", this.payloadFields == null ? "" : this.payloadFields);
      tag.putInt("payloadPacketId", this.payloadPacketId);
      tag.putString("payloadProvenance", this.payloadProvenance == null ? "" : this.payloadProvenance);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.enabled = tag.getBooleanOr("enabled", true);
      this.channel = tag.getStringOr("channel", "minecraft:brand");
      this.payloadData = tag.getStringOr("payloadData", "");
      this.payloadJson = tag.getStringOr("payloadJson", "");
      this.payloadClassName = tag.getStringOr("payloadClassName", "");
      this.javaSource = tag.getStringOr("javaSource", "");
      this.payloadScriptEnabled = tag.getBooleanOr("payloadScriptEnabled", false);
      this.commandApiRecognized = tag.getBooleanOr("commandApiRecognized", false);
      this.commandApiOverride = tag.getBooleanOr("commandApiOverride", false);
      this.commandApiValue = tag.getIntOr("commandApiValue", 2147483639);
      this.sourceDirection = tag.getStringOr("sourceDirection", "C2S");
      this.sourceProtocol = tag.getStringOr("sourceProtocol", "");
      this.payloadDirection = tag.getStringOr(
         "payloadDirection", this.sourceDirection != null && !this.sourceDirection.isBlank() ? this.sourceDirection : "C2S"
      );
      this.payloadPhase = tag.getStringOr(
         "payloadPhase", this.sourceProtocol != null && this.sourceProtocol.toLowerCase(Locale.ROOT).contains("configuration") ? "CONFIGURATION" : "PLAY"
      );
      this.payloadEncodingMode = tag.getStringOr("payloadEncodingMode", "");
      this.payloadFields = tag.getStringOr("payloadFields", "");
      this.payloadPacketId = tag.getIntOr("payloadPacketId", -1);
      this.payloadProvenance = tag.getStringOr("payloadProvenance", "userEdited");
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PAYLOAD;
   }

   @Override
   public String getDisplayName() {
      return this.channel != null && !this.channel.isBlank() ? "Payload - " + this.channel : "Payload";
   }

   @Override
   public String getIcon() {
      return "network";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Override
   public void sanitizeForSharing() {
      this.payloadScriptEnabled = false;
      this.javaSource = "";
   }
}
