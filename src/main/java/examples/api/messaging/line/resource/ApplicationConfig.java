package examples.api.messaging.line.resource;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("api")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        packages(getClass().getPackage().getName());
        property(ServerProperties.PROVIDER_SCANNING_RECURSIVE, false);
    }

}
