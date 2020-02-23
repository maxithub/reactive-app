package max.lab.r2app.reactiveapp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.lab.r2app.reactiveapp.domain.AppUser;
import max.lab.r2app.reactiveapp.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static max.lab.r2app.reactiveapp.controller.ControllerHelper.handleException;
import static max.lab.r2app.reactiveapp.controller.ControllerHelper.validate;
import static max.lab.r2app.reactiveapp.controller.ControllerHelper.validateAsync;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class AppUserController {
    private final AppUserRepository appUserRepo;
    private final Validator validator;

    @Bean
    public RouterFunction<ServerResponse> routes() {
        return route()
                .POST("/appuser", this::create)
                .PUT("/appuser/{id}", this::update)
                .GET("/appusers", this::findAll)
                .GET("/appuser/{id}", this::findOne)
                .build();
    }

    private Mono<ServerResponse> findOne(ServerRequest request) {
        var id = request.pathVariable("id");
        return appUserRepo.findById(id)
                .flatMap(ok()::bodyValue)
                .switchIfEmpty(notFound().build())
                .onErrorResume(t -> handleException(t, log, "Failed to find AppUsers: " + id));
    }

    private Mono<ServerResponse> update(ServerRequest request) {
        var id = request.pathVariable("id");
        return appUserRepo.findById(id)
                .then(request.bodyToMono(AppUser.class))
                .doOnNext(appUser -> validate(validator, appUser))
                .flatMap(appUserRepo::save)
                .then(ok().build())
                .switchIfEmpty(notFound().build())
                .onErrorResume(t -> handleException(t, log, "Failed to update AppUser"));
    }

    private Mono<ServerResponse> findAll(ServerRequest request) {
        return ok().body(appUserRepo.findAll(), AppUser.class)
                .onErrorResume(t -> handleException(t, log, "Failed to find AppUsers"));
    }

    private Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(AppUser.class)
                .flatMap(appUser -> validateAsync(validator, (theAppUser, errors) -> {
                        String id = appUser.getId();
                        return appUserRepo.findById(id).hasElement().doOnNext(b -> {
                            if (b.booleanValue()) {
                                errors.reject("AppUser.alreadyExists", new Object[] { id }, "AppUser: {0} already exists");
                            }
                        }).then(Mono.just(errors));
                    }, appUser))
                .flatMap(appUserRepo::save)
                .then(ok().build())
                .onErrorResume(t -> handleException(t, log, "Failed to create AppUser"));
    }
}
