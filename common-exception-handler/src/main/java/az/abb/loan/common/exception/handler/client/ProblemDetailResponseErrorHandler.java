package az.abb.loan.common.exception.handler.client;

import az.abb.loan.common.exception.handler.error.CommonErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import az.abb.loan.common.exception.handler.support.ProblemDetailParser;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link RestClient} counterpart of {@code CommonFeignErrorDecoder}, for services using RestClient or
 * HTTP interface clients ({@code @HttpExchange}):
 *
 * <pre>{@code
 * RestClient.builder()
 *     .defaultStatusHandler(HttpStatusCode::isError, new ProblemDetailResponseErrorHandler(jsonMapper))
 *     .build();
 * }</pre>
 */
public class ProblemDetailResponseErrorHandler implements RestClient.ResponseSpec.ErrorHandler {

    private static final int MAX_BODY_BYTES = 64 * 1024;

    private final ObjectMapper objectMapper;

    public ProblemDetailResponseErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatus status = HttpStatus.resolve(response.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        byte[] body;
        try (InputStream is = response.getBody()) {
            body = is.readNBytes(MAX_BODY_BYTES);
        }
        ProblemDetailParser.DownstreamError error = ProblemDetailParser.parse(objectMapper, body);
        String message = error.message() != null
                ? error.message()
                : request.getMethod() + " " + request.getURI() + " failed with status " + status.value();
        BaseException ex = CommonErrorCode.CLIENT_ERROR.exceptionWithMessage(status, message);
        if (error.key() != null) {
            ex.withProperty("downstreamKey", error.key());
        }
        throw ex;
    }
}
