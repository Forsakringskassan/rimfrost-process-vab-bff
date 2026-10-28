package se.fk.github.processvabbff.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Readiness;
import se.fk.rimfrost.framework.bff.health.UpstreamHealthCheck;

@ApplicationScoped
public class HealthCheckProducer
{
   @Readiness
   @Produces
   HealthCheck processVabHealthCheck()
   {
      return UpstreamHealthCheck.fromConfig("process-vab", "quarkus.rest-client.process-vab.url", 2000);
   }
}
