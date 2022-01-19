package no.unit.nva.bare;

import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.core.JsonUtils;

public final class ApplicationConfig {

    public static final ObjectMapper defaultRestObjectMapper =  JsonUtils.dtoObjectMapper;

    private ApplicationConfig() {
    }
}
