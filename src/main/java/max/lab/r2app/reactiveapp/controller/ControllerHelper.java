package max.lab.r2app.reactiveapp.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.util.Assert;
import org.springframework.validation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public final class ControllerHelper {
    private ControllerHelper() { }

    public static <T> void validate(Validator validator, ExtraValidator<T> functionalValidator, T t) {
        Assert.notNull(validator, "validator must NOT be null");
        Assert.notNull(functionalValidator, "functionalValidator must NOT be null");
        Assert.notNull(t, "t must NOT be null");

        Errors errors = new BeanPropertyBindingResult(t, t.getClass().getName());
        validator.validate(t, errors);
        functionalValidator.validate(t, errors);

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
    }

    public static <T> Mono<T> validateAsync(Validator validator, ReactiveValidator<T> functionalValidator, T t) {
        Assert.notNull(validator, "validator must NOT be null");
        Assert.notNull(functionalValidator, "functionalValidator must NOT be null");
        Assert.notNull(t, "t must NOT be null");

        Errors errors = new BeanPropertyBindingResult(t, t.getClass().getName());
        validator.validate(t, errors);
        Mono<Errors> asyncErrors = functionalValidator.validate(t, errors);
        return asyncErrors.doOnNext((theErrors -> {
            if (errors.hasErrors()) {
                throw new ValidationException(errors);
            }
        })).then(Mono.just(t));
    }

    public static <T> void validate(Validator validator, @NotNull T t) {
        validate(validator, NOOPS_VALIDATOR, t);
    }

    public static Mono<ServerResponse> handleException(Throwable t, Logger log, String message, Object... args) {
        if (t instanceof ValidationException) {
            var errors = ((ValidationException) t).errors;
            Map<String, Object> payload = new HashMap<>();
            if (errors.hasFieldErrors()) {
                payload.put("InvalidFields",
                        errors.getFieldErrors().stream()
                                .collect(groupingBy(FieldError::getField, toList()))
                                .entrySet().stream()
                                    .map(entry -> new SimpleEntry(entry.getKey(),
                                            entry.getValue().stream().map(FieldError::getDefaultMessage).collect(toList())))
                                .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue))
                );
            }
            if (errors.hasGlobalErrors()) {
                payload.put("errors",
                        errors.getGlobalErrors().stream()
                                .map(ObjectError::getDefaultMessage).collect(toList()));
            }

            return ServerResponse.badRequest().bodyValue(payload);
        }

        log.error(message, args);
        return ServerResponse.status(INTERNAL_SERVER_ERROR).bodyValue(message);
    }

    @RequiredArgsConstructor
    static class ValidationException extends RuntimeException {
        final Errors errors;
    }

    @FunctionalInterface
    public interface ExtraValidator<T> {
        void validate(T t, Errors errors);
    }

    @FunctionalInterface
    public interface ReactiveValidator<T> {
        Mono<Errors> validate(T t, Errors errors);
    }

    private static ExtraValidator NOOPS_VALIDATOR = (object, errors) -> {};
}
