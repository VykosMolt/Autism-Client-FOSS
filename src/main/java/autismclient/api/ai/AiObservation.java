package autismclient.api.ai;

public record AiObservation(String id, String session, String scope, long observedAt, String source, String dataJson) {
}
