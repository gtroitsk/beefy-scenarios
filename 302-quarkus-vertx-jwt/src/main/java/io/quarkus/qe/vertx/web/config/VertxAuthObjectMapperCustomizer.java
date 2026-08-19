package io.quarkus.qe.vertx.web.config;

import io.quarkus.jackson.JsonMapperBuilderCustomizer;

import jakarta.inject.Singleton;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Singleton
public class VertxAuthObjectMapperCustomizer implements JsonMapperBuilderCustomizer {

    @Override
    public void customize(JsonMapper.Builder builder) {
        builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}
