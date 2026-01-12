package az.abb.loan.exception.test.controller;

import az.abb.loan.exception.test.enums.BadRequestExceptionCode;
import az.abb.loan.exception.test.enums.FeignClientErrorCode;
import az.abb.loan.exception.test.enums.NotFoundExceptionCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestConroller {


    @PostMapping("/v1/send")
    public TestClazz send(@Valid @RequestBody TestClazz test){
        throw FeignClientErrorCode.CLIENT_ERROR_CODE.exceptionWithMessage("test for custom message");
    }

    @PostMapping("/v2/send")
    public TestClazz sendv2(@Valid @RequestBody TestClazz test){
        throw BadRequestExceptionCode.BAD_REQUEST_EXCEPTION_CODE.exception();
    }

    @PostMapping("/v3/send")
    public TestClazz send3(@Valid @RequestBody TestClazz test){
        throw NotFoundExceptionCode.USER_NOT_FOUND.exception(new Object[]{25});
    }

    @PostMapping("/v4/send")
    public TestClazz send4(@Valid @RequestBody TestClazz test){
        throw BadRequestExceptionCode.BAD_REQUEST_EXCEPTION_CODE.exception();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestClazz {

        @NotNull
        private String name;

        @NotEmpty
        private String surname;

        @Min(100)
        private BigDecimal amount;
    }
}
