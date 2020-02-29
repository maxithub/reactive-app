package max.lab.r2app.reactiveapp.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import max.lab.r2app.reactiveapp.domain.AppUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.data.r2dbc.query.Criteria.where;
import static org.springframework.util.StringUtils.isEmpty;

@RequiredArgsConstructor
@Repository
@Slf4j
public class AppUserRepository {
    private final DatabaseClient databaseClient;

    public Mono<Void> insert(AppUser appUser) {
        return databaseClient.insert().into(AppUser.class).using(appUser).then();
    }

    public Mono<Void> update(AppUser appUser) {
        return databaseClient.update().table(AppUser.class).using(appUser).then();
    }

    public Mono<AppUser> findById(String id) {
        return databaseClient.select().from(AppUser.class).matching(where("id").is(id)).fetch().one();
    }

    public Flux<AppUser> findAll(Pageable page) {
        return find(Optional.empty(), Optional.empty(), Optional.empty(), page);
    }

    public Flux<AppUser> find(Optional<String> province, Optional<String> city, Optional<Integer> age, Pageable page) {
        Criteria where = null;
        if (province.isPresent() && !isEmpty(province.get())) {
            where = (where == null ? where("province").is(province.get()) : where.and("province").is(province.get()));
        }
        if (city.isPresent() && !isEmpty(city.get())) {
            where = (where == null ? where("city").is(city.get()) : where.and("city").is(city.get()));
        }
        if (age.isPresent()) {
            where = (where == null ? where("age").is(age.get()) : where.and("age").is(age.get()));
        }
        var query = databaseClient.select().from(AppUser.class).page(page);
        if (where != null) {
            query = query.matching(where);
        }
        return query.fetch().all();
    }

    public Mono<Void> deleteAll() {
        return databaseClient.delete().from(AppUser.class).then();
    }
}
