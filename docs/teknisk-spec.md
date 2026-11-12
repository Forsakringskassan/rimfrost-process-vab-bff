# Teknisk spec — BFF för VAB:s processvy (PVABBF)

## Översikt

Quarkus-tjänst (synkron REST BFF) utan egen datalagring och utan meddelandeintegration. Hämtar
processens steg från process-vab åt mikrofrontenden `rimfrost-process-vab-fe`. Felhantering,
vidarebefordran av `Authorization`-headern, loggkontext och hälsokontrollen kommer från
`rimfrost-framework-bff`.

## Komponentstruktur

```text
src/main/java/se/fk/github/processvabbff
├── ProcessVabBFFController         # GET /api/steg, GET /api/health
├── integration/ProcessVabClient    # REST-klient som ärver ProcessvyControllerApi
└── health/HealthCheckProducer      # Readiness-kontroll mot process-vab
```

## API-specifikationer

| Metod | Sökväg | Beskrivning |
|---|---|---|
| GET | `/api/steg?aktivitetId={uuid}` | Processens steg för aktiviteten. Svar enligt `GetProcessvyStegResponse` i `rimfrost-framework-process-openapi` (från och med 1.1.0), inklusive understeg |
| GET | `/api/health` | Egen hälsokontroll |

### Understeg

Ett steg kan ha understeg i fältet `steg` (samma typ, `ProcessSteg`), högst två nivåer under
svarets `steg[]`. BFF:en returnerar process-vabs svar som den genererade kontraktsklassen
`GetProcessvyStegResponse`, så understegen skickas vidare oförändrade på alla nivåer utan egen
mappning. Med `quarkus.jackson.serialization-inclusion=non_null` utelämnas `steg` för steg utan
understeg, precis som i process-vabs svar.

### Felsvar från `/api/steg`

| Situation | Status | Kropp |
|---|---|---|
| `aktivitetId` saknas eller är inte ett UUID | `400` | Valideringsfel. process-vab anropas inte |
| process-vab svarar `404` (okänd aktivitet eller ingen processbeskrivning) | `404` | `{"error": "Upstream error"}` |
| process-vab svarar med annat fel | Samma status | `{"error": "Upstream error"}` |
| process-vab går inte att nå | `502` eller `500` | Enligt `GlobalExceptionMapper` i framework-bff |

## Kafka-integration

Ingen.

## Konfiguration

| Egenskap | Beskrivning | Standardvärde |
|---|---|---|
| `quarkus.rest-client.process-vab.url` (`PROCESS_VAB_URL`) | Bas-URL till process-vab, utan avslutande snedstreck | `http://localhost:8080` |
| `CORS_ORIGINS` | Tillåtna ursprung för CORS | Lokala mikrofrontend-portar |
| `CORS_ALLOW_CREDENTIALS` | Tillåt cookies i CORS-anrop | `false` |
| `quarkus.http.port` | Lyssningsport | `9006` |

## Liveness och readiness

`/api/health` (egen), samt `/q/health`. Readiness (`/q/health/ready`) innehåller kontrollen
`process-vab`, som är `DOWN` när process-vab inte svarar inom 2 sekunder.
