package io.github.gshahrza.exceptionhandler.decoder;

import io.github.gshahrza.exceptionhandler.error.CommonErrorCode;
import io.github.gshahrza.exceptionhandler.exception.BaseException;
import io.github.gshahrza.exceptionhandler.support.ProblemDetailParser;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converts error responses of downstream services called via Feign into {@code CLIENT_ERROR} exceptions,
 * keeping the downstream HTTP status and its Problem Details message.
 */
public class CommonFeignErrorDecoder implements ErrorDecoder {

    private static final int MAX_BODY_BYTES = 64 * 1024;

    private final ObjectMapper objectMapper;

    public CommonFeignErrorDecoder() {
        this(JsonMapper.builder().build());
    }

    public CommonFeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.resolve(response.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        ProblemDetailParser.DownstreamError error = ProblemDetailParser.parse(objectMapper, readBody(response));
        String message = error.message();
        if (message == null) {
            message = response.reason() != null
                    ? response.reason()
                    : "Downstream call " + methodKey + " failed with status " + response.status();
        }
        BaseException ex = CommonErrorCode.CLIENT_ERROR.exceptionWithMessage(status, message);
        if (error.key() != null) {
            ex.withProperty("downstreamKey", error.key());
        }
        return ex;
    }

    private static byte[] readBody(Response response) {
        if (response.body() == null) {
            return null;
        }
        try (InputStream is = response.body().asInputStream()) {
            return is.readNBytes(MAX_BODY_BYTES);
        } catch (IOException e) {
            return null;
        }
    }
}
