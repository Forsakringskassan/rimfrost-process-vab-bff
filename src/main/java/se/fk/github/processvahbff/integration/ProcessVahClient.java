package se.fk.github.processvahbff.integration;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.rimfrost.framework.process.jaxrsspec.controllers.generatedsource.ProcessvyControllerApi;

// The Authorization header is forwarded by rimfrost-framework-bff's OutgoingAuthorizationHeaderFilter.
@RegisterRestClient(configKey = "process-vah")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProcessVahClient extends ProcessvyControllerApi
{
}
