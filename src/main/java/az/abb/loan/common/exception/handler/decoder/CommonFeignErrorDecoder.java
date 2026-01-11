package az.abb.loan.common.exception.handler.decoder;

import az.abb.loan.common.exception.handler.error.CommonErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.util.StreamUtils;

public class CommonFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();

    public CommonFeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        HttpStatus status = HttpStatus.resolve(response.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (response.body() != null) {
            try (InputStream is = response.body().asInputStream()) {

                String body = StreamUtils.copyToString(is, StandardCharsets.UTF_8);

                if (isProblemDetail(body)) {
                    return mapProblemDetail(body, status);
                }

                return CommonErrorCode.CLIENT_ERROR
                        .exceptionWithMessage(status, response.reason());

            } catch (Exception e) {
                return CommonErrorCode.CLIENT_ERROR.exception();
            }
        }

        return CommonErrorCode.CLIENT_ERROR
                .exceptionWithMessage(status, "Feign client error: " + response.reason());
    }

    protected boolean isProblemDetail(String body) {
        return body.contains("\"type\"")
                && body.contains("\"title\"")
                && body.contains("\"status\"");
    }

    protected BaseException mapProblemDetail(String body, HttpStatus status) throws Exception {
        ProblemDetail pd = objectMapper.readValue(body, ProblemDetail.class);

        return CommonErrorCode.CLIENT_ERROR
                .exceptionWithMessage(
                        status,
                        pd.getDetail() != null ? pd.getDetail() : pd.getTitle()
                );
    }
}

