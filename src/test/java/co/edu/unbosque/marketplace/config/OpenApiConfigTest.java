package co.edu.unbosque.marketplace.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    void testCustomOpenAPI() {

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getComponents());
    }

    @Test
    void testEsquemaSeguridadBearerAuth() {

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        SecurityScheme esquema = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertNotNull(esquema);
    }

    @Test
    void testTipoAutenticacionHttp() {

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        SecurityScheme esquema = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertEquals(SecurityScheme.Type.HTTP, esquema.getType());
    }

    @Test
    void testEsquemaBearer() {

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        SecurityScheme esquema = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertEquals("bearer", esquema.getScheme());
    }

    @Test
    void testFormatoTokenJWT() {

        OpenAPI openAPI = openApiConfig.customOpenAPI();

        SecurityScheme esquema = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertEquals("JWT", esquema.getBearerFormat());
    }
}
