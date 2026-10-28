package se.fk.github.processvahbff;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.processvahbff.integration.ProcessVahClient;
import se.fk.rimfrost.framework.bff.logging.LogContext;
import se.fk.rimfrost.framework.process.jaxrsspec.controllers.generatedsource.model.GetProcessvyStegResponse;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class ProcessVahBFFController
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ProcessVahBFFController.class);

   // Validated as a string: a UUID-typed query param that fails conversion gives 404 in JAX-RS,
   // which the frontend can't tell apart from an unknown aktivitetId.
   private static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

   @Inject
   @RestClient
   private ProcessVahClient processVahClient;

   @GET
   @Path("/health")
   public Response health()
   {
      return Response.ok(Map.of("status", "ok", "timestamp", Instant.now().toString())).build();
   }

   // Errors from process-vah (including 404 for an unknown aktivitetId) propagate to the
   // framework's GlobalExceptionMapper, which keeps the upstream status code.
   @GET
   @Path("/steg")
   public GetProcessvyStegResponse getSteg(
         @QueryParam("aktivitetId") @NotNull @Pattern(regexp = UUID_PATTERN) String aktivitetId)
   {
      LOGGER.debug("GET /api/steg aktivitetId={}", aktivitetId);
      try (LogContext ignored = LogContext.put("aktivitetId", aktivitetId))
      {
         return processVahClient.getProcessvySteg(UUID.fromString(aktivitetId));
      }
   }
}
