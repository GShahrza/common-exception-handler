package az.abb.loan.common.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import az.abb.loan.common.exception.handler.client.ProblemDetailResponseErrorHandler;
import az.abb.loan.common.exception.handler.decoder.CommonFeignErrorDecoder;
import az.abb.loan.common.exception.handler.error.CommonErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class ClientErrorHandlingTest {

    // A response produced by this very library: extra properties must not break parsing
    private static final String PROBLEM = """
            {"type":"about:blank","title":"Not Found","status":404,"detail":"Loan 42 not found",
             "key":"LOAN3001","path":"/loans/42","timestamp":"2026-01-11T07:42:47Z"}""";

    private final CommonFeignErrorDecoder decoder = new CommonFeignErrorDecoder(JsonMapper.builder().build());

    private static Response feignResponse(int status, String reason, String body) {
        Request request = Request.create(Request.HttpMethod.GET, "http://loan-ms/loans/42", Map.of(),
                null, StandardCharsets.UTF_8, null);
        Response.Builder builder = Response.builder().status(status).reason(reason).request(request).headers(Map.of());
        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return builder.build();
    }

    @Test
    void feignKeepsDownstreamStatusAndProblemDetail() {
        BaseException ex = (BaseException) decoder.decode("LoanClient#get", feignResponse(404, "Not Found", PROBLEM));
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getDetail()).isEqualTo("Loan 42 not found");
        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.CLIENT_ERROR);
        assertThat(ex.getProperties()).containsEntry("downstreamKey", "LOAN3001");
    }

    @Test
    void feignFallsBackToReasonForNonJsonOrEmptyBody() {
        BaseException html = (BaseException) decoder.decode("m", feignResponse(503, "Service Unavailable", "<html>"));
        assertThat(html.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(html.getDetail()).isEqualTo("Service Unavailable");

        BaseException empty = (BaseException) decoder.decode("LoanClient#get", feignResponse(500, null, null));
        assertThat(empty.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(empty.getDetail()).contains("LoanClient#get").contains("500");
    }

    @Test
    void restClientHandlerThrowsClientError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder
                .defaultStatusHandler(HttpStatusCode::isError,
                        new ProblemDetailResponseErrorHandler(JsonMapper.builder().build()))
                .build();
        server.expect(requestTo("http://loan-ms/loans/42"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(PROBLEM));

        assertThatThrownBy(() -> client.get().uri("http://loan-ms/loans/42").retrieve().body(String.class))
                .isInstanceOfSatisfying(BaseException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getDetail()).isEqualTo("Loan 42 not found");
                    assertThat(ex.getProperties()).containsEntry("downstreamKey", "LOAN3001");
                });
    }
}
