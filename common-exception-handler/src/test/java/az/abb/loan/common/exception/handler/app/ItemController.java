package az.abb.loan.common.exception.handler.app;

import az.abb.loan.common.exception.handler.error.CommonErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    public record ItemRequest(@NotBlank String name, @Min(1) int amount) {
    }

    @GetMapping("/items/{id}")
    public String get(@PathVariable long id) {
        if (id == 500) {
            throw new IllegalStateException("database password=secret leaked?");
        }
        throw CommonErrorCode.RESOURCE_NOT_FOUND.exception(id);
    }

    @PostMapping(value = "/items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ItemRequest create(@Valid @RequestBody ItemRequest request) {
        return request;
    }

    @GetMapping("/search")
    public String search(@RequestParam @Min(1) int page) {
        return "page " + page;
    }
}
