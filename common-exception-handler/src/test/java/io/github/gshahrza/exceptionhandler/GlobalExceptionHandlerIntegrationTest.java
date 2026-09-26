package io.github.gshahrza.exceptionhandler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.gshahrza.exceptionhandler.app.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void unknownUrlIs404NotInternalError() throws Exception {
        mvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.key").value("TEST3001"))
                .andExpect(jsonPath("$.path").value("/does-not-exist"));
    }

    @Test
    void baseExceptionUsesStatusOfErrorCodeAndServiceOverride() throws Exception {
        mvc.perform(get("/items/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Item missing"))
                .andExpect(jsonPath("$.detail").value("Requested resource not found"))
                .andExpect(jsonPath("$.key").value("TEST3001"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void messagesAreLocalizedFromLibraryDefaults() throws Exception {
        mvc.perform(post("/items").header("Accept-Language", "az")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validasiya xətası"));

        mvc.perform(post("/items").header("Accept-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"amount\":0}"))
                .andExpect(jsonPath("$.title").value("Ошибка валидации"));
    }

    @Test
    void bodyValidationReturnsFieldErrors() throws Exception {
        mvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("TEST1000"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')]").exists());
    }

    @Test
    void parameterValidationReturnsFieldErrors() throws Exception {
        mvc.perform(get("/search").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("TEST1000"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("page"));
    }

    @Test
    void malformedJson() throws Exception {
        mvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("TEST1001"));
    }

    @Test
    void typeMismatchAndMissingParameter() throws Exception {
        mvc.perform(get("/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.key").value("TEST1003"))
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter id"));

        mvc.perform(get("/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid value for parameter page"));
    }

    @Test
    void methodNotAllowedKeepsAllowHeader() throws Exception {
        mvc.perform(put("/items/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.key").value("TEST3002"));
    }

    @Test
    void unsupportedMediaType() throws Exception {
        mvc.perform(post("/items").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.key").value("TEST3003"));
    }

    @Test
    void unexpectedErrorDoesNotLeakDetails() throws Exception {
        mvc.perform(get("/items/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.key").value("TEST9999"))
                .andExpect(content().string(not(containsString("secret"))));
    }

    @Test
    void databaseExceptionsGetTheirOwnCodesWithoutLeakingSql() throws Exception {
        mvc.perform(get("/items/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.key").value("TEST2001"))
                .andExpect(content().string(not(containsString("items_pkey"))));

        mvc.perform(get("/items/concurrent"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.key").value("TEST2002"));
    }

    @Test
    void customPropertiesAreAddedToResponse() throws Exception {
        mvc.perform(get("/items/limit"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.maxAmount").value(5000));
    }

    @Test
    void timestampHasMillisecondPrecision() throws Exception {
        mvc.perform(get("/items/7"))
                .andExpect(jsonPath("$.timestamp").value(org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?Z")));
    }

    @Test
    void micrometerTraceIdFromMdcIsUsedWithoutHeaders() throws Exception {
        mvc.perform(get("/items/traced"))
                .andExpect(jsonPath("$.instance").value("trace:4bf92f3577b34da6"));
    }

    @Test
    void traceHeaderBecomesInstanceAndMalformedHeaderIsIgnored() throws Exception {
        mvc.perform(get("/items/7").header("traceparent", "00-abc-def-01"))
                .andExpect(jsonPath("$.instance").value("trace:00-abc-def-01"));

        mvc.perform(get("/items/7").header("X-Trace-Id", "bad value with spaces"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.instance").value("/items/7"));
    }
}
