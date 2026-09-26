package az.abb.loan.common.exception.handler.support;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Extracts information from a downstream error body (RFC 9457 Problem Details or similar). */
public final class ProblemDetailParser {

    /** What could be read from a downstream error body; fields are null when absent. */
    public record DownstreamError(String message, String key) {
        static final DownstreamError EMPTY = new DownstreamError(null, null);
    }

    private ProblemDetailParser() {
    }

    /** Returns {@code detail}, then {@code title}, then {@code message} of a JSON object body, or null. */
    public static String extractMessage(ObjectMapper objectMapper, byte[] body) {
        return parse(objectMapper, body).message();
    }

    public static DownstreamError parse(ObjectMapper objectMapper, byte[] body) {
        if (body == null || body.length == 0) {
            return DownstreamError.EMPTY;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node == null || !node.isObject()) {
                return DownstreamError.EMPTY;
            }
            return new DownstreamError(firstText(node, "detail", "title", "message"), firstText(node, "key"));
        } catch (JacksonException e) {
            return DownstreamError.EMPTY;
        }
    }

    private static String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.get(field);
            if (value != null && value.isString() && !value.stringValue().isBlank()) {
                return value.stringValue();
            }
        }
        return null;
    }
}
