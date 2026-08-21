package io.quarkus.qe.vertx.sql;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.qe.vertx.sql.services.DbPoolService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.vertx.mutiny.sqlclient.Pool;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/** Application is used as a main class in order to setup some global configuration */
@ApplicationScoped
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @ConfigProperty(name = "app.selected.db")
    String selectedDB;

    @ConfigProperty(name = "quarkus.flyway.schemas")
    String postgresqlDbName;

    @ConfigProperty(name = "quarkus.flyway.mysql.schemas")
    String mysqlDbName;

    @Inject
    Pool postgresql;

    @Inject
    @Named("mysql")
    @IfBuildProfile("mysql")
    Pool mysql;

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting with profiles " + ConfigUtils.getProfiles());
    }

    @Singleton
    @Produces
    @Named("sqlClient")
    @Typed(DbPoolService.class)
    synchronized DbPoolService pool() {
        switch (selectedDB) {
            case "mysql":
                return new DbPoolService(mysql, mysqlDbName, selectedDB);
            default:
                return new DbPoolService(postgresql, postgresqlDbName, selectedDB);
        }
    }
}
