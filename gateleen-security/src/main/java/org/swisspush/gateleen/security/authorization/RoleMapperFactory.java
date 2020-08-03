package org.swisspush.gateleen.security.authorization;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swisspush.gateleen.core.util.ResourcesUtils;
import org.swisspush.gateleen.core.util.StringUtils;
import org.swisspush.gateleen.core.validation.ValidationResult;
import org.swisspush.gateleen.validation.ValidationException;
import org.swisspush.gateleen.validation.Validator;

import java.util.*;
import java.util.regex.Pattern;

/**
 * RoleMapperFactory is used to parse RoleMapper resources.
 */
public class RoleMapperFactory {

    private final String mapperSchema;

    private static Logger LOGGER = LoggerFactory.getLogger(RoleMapperFactory.class);

    private final Map<String, Object> properties;

    /**
     * Factory loading, checking and initializing the Role Mappings for later usage
     *
     * @param properties The list of system properties given to the application. This is used to replace
     *                   system variables which might be defined within the mapping patterns and roles.
     *                   Could be null if no properties shall be treated here.
     */
    RoleMapperFactory(Map<String, Object> properties) {
        this.mapperSchema = ResourcesUtils.loadResource("gateleen_security_schema_rolemapper", true);
        this.properties = properties;
    }


    public List<RoleMapperHolder> parseRoleMapper(Buffer buffer) throws ValidationException {
        ValidationResult validationResult = Validator.validateStatic(buffer, mapperSchema, LOGGER);
        if (!validationResult.isSuccess()) {
            throw new ValidationException(validationResult);
        }

        List<RoleMapperHolder> result = new ArrayList<>();
        Mappings mappings = Json.decodeValue(buffer, Mappings.class);
        for (Mapping mapping : mappings.mappings) {
            if (properties != null) {
                mapping.pattern = StringUtils.replaceWildcardConfigs(mapping.pattern, properties);
                mapping.role = StringUtils.replaceWildcardConfigs(mapping.role, properties);
            }
            result.add(new RoleMapperHolder(Pattern.compile(mapping.pattern), mapping.role, mapping.keepOriginal, mapping.continueMapping));
        }
        return result;
    }

    public static class Mappings {

        public Mapping[] mappings;
    }

    public static class Mapping {
        public String pattern, role;
        public boolean keepOriginal, continueMapping;
    }

}
