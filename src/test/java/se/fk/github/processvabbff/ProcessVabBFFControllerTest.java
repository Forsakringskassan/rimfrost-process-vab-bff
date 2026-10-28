package se.fk.github.processvabbff;

import com.github.tomakehurst.wiremock.http.Fault;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class ProcessVabBFFControllerTest
{
   private static final String AKTIVITET_ID = "3f2c8a1e-6b4d-4c7a-9e21-5d8f0b7a1c34";
   private static final String STEG_PATH = "/processvy/steg?aktivitetId=" + AKTIVITET_ID;

   private static final String STEG_RESPONSE = """
         {
           "processId": "vab",
           "processnamn": "Vård av boskap",
           "steg": [
             { "id": "maskinell-kontroll", "rubrik": "Maskinell kontroll av rätt till försäkring", "status": "KLAR" },
             { "id": "manuell-kontroll", "rubrik": "Manuell kontroll av rätt till försäkring",
               "beskrivning": "Handläggaren kompletterar yrkandet vid behov och bedömer rätten till försäkring när den maskinella kontrollen inte räcker.", "status": "PAGAR" },
             { "id": "bekrafta-beslut", "rubrik": "Bekräfta beslut", "status": "KOMMANDE" }
           ]
         }
         """;

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
   }

   @Test
   @DisplayName("PVABBF-FR-01.2: stegen hämtas från process-vab och returneras")
   void getSteg_returnsStegFromProcessVab()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo(STEG_PATH))
            .willReturn(okJson(STEG_RESPONSE)));

      given()
            .queryParam("aktivitetId", AKTIVITET_ID)
            .when()
            .get("/api/steg")
            .then()
            .statusCode(200)
            .body("processId", equalTo("vab"))
            .body("processnamn", equalTo("Vård av boskap"))
            .body("steg", hasSize(3))
            .body("steg[0].status", equalTo("KLAR"))
            .body("steg[0]", not(hasKey("beskrivning")))
            .body("steg[1].id", equalTo("manuell-kontroll"))
            .body("steg[1].rubrik", equalTo("Manuell kontroll av rätt till försäkring"))
            .body("steg[1].beskrivning", equalTo(
                  "Handläggaren kompletterar yrkandet vid behov och bedömer rätten till försäkring när den maskinella kontrollen inte räcker."))
            .body("steg[1].status", equalTo("PAGAR"))
            .body("steg[2].status", equalTo("KOMMANDE"));
   }

   @Test
   @DisplayName("PVABBF-FR-01.3: handläggarens bearer-token skickas vidare till process-vab")
   void getSteg_forwardsAuthorizationHeader()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo(STEG_PATH))
            .willReturn(okJson(STEG_RESPONSE)));

      given()
            .header("Authorization", "Bearer test-token")
            .queryParam("aktivitetId", AKTIVITET_ID)
            .when()
            .get("/api/steg")
            .then()
            .statusCode(200);

      WireMockTestResource.getServer().verify(getRequestedFor(urlEqualTo(STEG_PATH))
            .withHeader("Authorization", com.github.tomakehurst.wiremock.client.WireMock.equalTo("Bearer test-token")));
   }

   @Test
   @DisplayName("PVABBF-FR-01.4: 404 från process-vab returneras som 404")
   void getSteg_returns404_whenProcessVabReturns404()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo(STEG_PATH))
            .willReturn(aResponse()
                  .withStatus(404)
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"code\": 404, \"message\": \"Aktiviteten finns inte i processbeskrivningen\"}")));

      given()
            .queryParam("aktivitetId", AKTIVITET_ID)
            .when()
            .get("/api/steg")
            .then()
            .statusCode(404)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   @DisplayName("PVABBF-FR-01.4: 5xx från process-vab mappas enligt framework-bff")
   void getSteg_returns500_whenProcessVabFails()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo(STEG_PATH))
            .willReturn(aResponse().withStatus(500)));

      given()
            .queryParam("aktivitetId", AKTIVITET_ID)
            .when()
            .get("/api/steg")
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   @DisplayName("PVABBF-FR-01.4: 503 från process-vab mappas enligt framework-bff")
   void getSteg_returns503_whenProcessVabUnavailable()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo(STEG_PATH))
            .willReturn(aResponse().withStatus(503)));

      given()
            .queryParam("aktivitetId", AKTIVITET_ID)
            .when()
            .get("/api/steg")
            .then()
            .statusCode(503)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   @DisplayName("PVABBF-FR-01.2: saknat aktivitets-ID ger 400 utan anrop till process-vab")
   void getSteg_returns400_whenAktivitetIdMissing()
   {
      given()
            .when()
            .get("/api/steg")
            .then()
            .statusCode(400);

      WireMockTestResource.getServer().verify(0, getRequestedFor(urlPathEqualTo("/processvy/steg")));
   }

   @Test
   @DisplayName("PVABBF-FR-01.2: ogiltigt aktivitets-ID ger 400 utan anrop till process-vab")
   void getSteg_returns400_whenAktivitetIdIsNotUuid()
   {
      given()
            .queryParam("aktivitetId", "inte-ett-uuid")
            .when()
            .get("/api/steg")
            .then()
            .statusCode(400);

      WireMockTestResource.getServer().verify(0, getRequestedFor(urlPathEqualTo("/processvy/steg")));
   }

   @Test
   @DisplayName("BFF:ens egen hälsokontroll svarar 200")
   void health_returns200WithStatusOk()
   {
      given()
            .when()
            .get("/api/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("ok"))
            .body("timestamp", notNullValue());
   }

   @Test
   @DisplayName("PVABBF-NFR-01.1: hälsokontrollen rapporterar process-vab som UP")
   void readiness_reportsProcessVabUp()
   {
      WireMockTestResource.getServer().stubFor(head(urlMatching(".*"))
            .willReturn(aResponse().withStatus(200)));

      given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks.find { it.name == 'process-vab' }.status", equalTo("UP"));
   }

   @Test
   @DisplayName("PVABBF-NFR-01.1: hälsokontrollen rapporterar process-vab som DOWN när den inte nås")
   void readiness_reportsProcessVabDown_whenUnreachable()
   {
      WireMockTestResource.getServer().stubFor(head(urlMatching(".*"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

      given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(503)
            .body("status", equalTo("DOWN"))
            .body("checks.find { it.name == 'process-vab' }.status", equalTo("DOWN"));
   }
}
