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
| GET | `/api/steg?aktivitetId={uuid}` | Processens steg för aktiviteten. Svar enligt `GetProcessvyStegResponse` i `rimfrost-framework-process-openapi` |
| GET | `/api/health` | Egen hälsokontroll |

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
