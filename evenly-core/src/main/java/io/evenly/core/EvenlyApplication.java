package io.evenly.core;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

@ApplicationPath("/")
@OpenAPIDefinition(
    info = @Info(
        title = "Evenly API",
        version = "1.0.0",
        description = "Shared Expense Tracker API"
    )
)
public class EvenlyApplication extends Application {
}
