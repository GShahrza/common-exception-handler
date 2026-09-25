package az.abb.loan.common.exception.handler.support;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Extracts a human-readable message from a downstream error body (RFC 9457 Problem Details or similar). */
public final class ProblemDetailParser {

    private ProblemDetailParser() {
    }

    /** Returns {@code detail}, then {@code title}, then {@code message} of a JSON object body, or null. */
    public static String extractMessage(ObjectMapper objectMapper, byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node == null || !node.isObject()) {
                return null;
            }
            for (String field : new String[] {"detail", "title", "message"}) {
                JsonNode value = node.get(field);
                if (value != null && value.isString() && !value.stringValue().isBlank()) {
                    return value.stringValue();
                }
            }
            return null;
        } catch (JacksonException e) {
            return null;
        }
    }
}
