package org.openmbee.docweb;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "MMS PE Service",
                version = "0.1.0",
                description = "Helper to make presentation elements in Donbot",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.txt")
        ),
        security = {@SecurityRequirement(name = "basicAuth"), @SecurityRequirement(name = "apiKey")}
)
/*@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic"
)*/ //can't get multiple security scheme to show
@SecurityScheme(
        name = "apiKey",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.QUERY,
        paramName = "alf_ticket"
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }
}