# rimfrost-process-vab-bff

Backend-for-frontend till VAB:s processvy (`rimfrost-process-vab-fe`). BFF:en hämtar processens
steg från process-vab och lämnar dem till mikrofrontenden, som visar dem i stegindikatorn när
handläggaren har en uppgift öppen i portalen.

Bygger på `rimfrost-framework-bff`, som står för felhantering, vidarebefordran av
`Authorization`-headern och hälsokontrollen mot process-vab. Krav finns i
[docs/krav.md](docs/krav.md) och teknisk beskrivning i [docs/teknisk-spec.md](docs/teknisk-spec.md).

## API

### `GET /api/steg?aktivitetId={uuid}`

Hämtar processens steg för aktiviteten som uppgiften skapades i. Anropet skickas vidare till
process-vab (`GET /processvy/steg`) med handläggarens `Authorization`-header.

Svar `200`, enligt `GetProcessvyStegResponse` i `rimfrost-framework-process-openapi`:

```json
{
  "processId": "vab",
  "processnamn": "Vård av boskap",
  "steg": [
    { "id": "registrera-ansokan", "rubrik": "Registrera ansökan", "status": "KLAR" },
    { "id": "kontrollera-ratt-till-forsakring", "rubrik": "Kontrollera rätt till försäkring", "beskrivning": "Handläggaren kontrollerar att den försäkrade har rätt till ersättning.", "status": "PAGAR" },
    { "id": "besluta", "rubrik": "Besluta", "status": "KOMMANDE" }
  ]
}
```

`status` är `KLAR`, `PAGAR` eller `KOMMANDE`. `beskrivning` är valfri och utelämnas när steget saknar beskrivning.

Fel:

- `400` om `aktivitetId` saknas eller inte är ett UUID.
- `404` om process-vab inte känner till aktiviteten, eller saknar processbeskrivning. Mikrofrontenden ska då visa ett neutralt meddelande eller dölja processvyn.
- Övriga fel från process-vab returneras med samma status och `{"error": "Upstream error"}`. Går process-vab inte att nå blir det `502` (eller `500`), enligt framework-bff.

### `GET /api/health`

Enkel hälsokontroll för BFF:en själv. Readiness mot process-vab finns på `/q/health/ready`.

## Konfiguration

| Miljövariabel | Beskrivning | Standardvärde |
|---|---|---|
| `PROCESS_VAB_URL` | Bas-URL till process-vab, utan avslutande snedstreck | `http://localhost:8080` |
| `CORS_ORIGINS` | Tillåtna ursprung för CORS | `http://localhost:3000,http://localhost:3030` |
| `CORS_ALLOW_CREDENTIALS` | Tillåt cookies i CORS-anrop | `false` |

BFF:en lyssnar på port `9006`.

## Bygga och köra

Bygget hämtar beroenden från Försäkringskassans GitHub Packages och kräver en Maven `settings.xml` med åtkomst dit (samma som för övriga rimfrost-repon). Den ingår inte i repot.

```sh
./mvnw -s <sökväg till settings.xml> clean verify   # bygg och testa
./mvnw quarkus:dev                    # kör lokalt
```
