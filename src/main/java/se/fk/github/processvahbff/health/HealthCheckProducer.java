package se.fk.github.processvahbff.health;

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
   HealthCheck processVahHealthCheck()
   {
      return UpstreamHealthCheck.fromConfig("process-vah", "quarkus.rest-client.process-vah.url", 2000);
   }
}
