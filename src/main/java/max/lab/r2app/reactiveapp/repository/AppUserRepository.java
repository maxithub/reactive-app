package max.lab.r2app.reactiveapp.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.lab.r2app.reactiveapp.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.r2dbc.query.Criteria.where;

@RequiredArgsConstructor
@Repository
@Slf4j
public class AppUserRepository {
    private final DatabaseClient databaseClient;

    /**
     * Try insert first, and update the record if it already exists.
     *
     * @param appUser
     * @return
     */
    public Mono<Void> save(AppUser appUser) {
        return Mono.defer(() -> databaseClient.insert().into(AppUser.class).using(appUser).then())
                .onErrorResume(DataIntegrityViolationException.class,
                        t -> databaseClient.update().table(AppUser.class).using(appUser).then());
    }

    public Mono<AppUser> findById(String id) {
        return databaseClient.select().from(AppUser.class).matching(where("id").is(id)).fetch().one();
    }

    public Flux<AppUser> findAll() {
        return databaseClient.select().from(AppUser.class).fetch().all();
    }

    public Mono<Void> deleteAll() {
        return databaseClient.delete().from(AppUser.class).then();
    }
}
