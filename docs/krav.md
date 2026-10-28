# Krav — BFF för VAB:s processvy (PVABBF)

## Bakgrund och syfte

Handläggaren ska se var i VAB-processen ärendet befinner sig när hen arbetar med en uppgift.
Processen äger sin processvy och levererar den som en egen mikrofrontend
(`rimfrost-process-vab-fe`), som portalen laddar bredvid uppgiftens mikrofrontend. Den här
BFF:en är mikrofrontendens backend: den hämtar processens steg från process-vab och lämnar dem
vidare, på samma sätt som reglernas BFF:er gör för reglernas mikrofrontends.

---

## Intressenter och aktörer

| Aktör | Roll |
|---|---|
| `rimfrost-process-vab-fe` | Anropar BFF:en för att visa processens steg |
| `rimfrost-process-vab` | Bakomliggande tjänst som beskriver processens steg (endpoint från `rimfrost-framework-process`) |
| Handläggare | Ser stegen i portalen. BFF:en skickar vidare handläggarens bearer-token |

---

## Funktionella krav

### PVABBF-FR-01 — Processens steg

- **PVABBF-FR-01.1** BFF:en ska bygga på `rimfrost-framework-bff`.
- **PVABBF-FR-01.2** BFF:en ska erbjuda en endpoint som, givet ett aktivitets-ID, hämtar stegen
  från process-vab enligt `rimfrost-framework-process-openapi` och returnerar dem till
  mikrofrontenden.
- **PVABBF-FR-01.3** Handläggarens bearer-token ska skickas vidare till process-vab, via
  framework-bff.
- **PVABBF-FR-01.4** 404 från process-vab ska returneras som 404. Övriga fel ska mappas enligt
  framework-bff:s felhantering.

---

## Icke-funktionella krav

### PVABBF-NFR-01 — Drift

- **PVABBF-NFR-01.1** BFF:en ska ha en hälsokontroll mot process-vab.

---

## API-gränssnitt (översikt)

| API | Målgrupp | Specifikationsartefakt |
|---|---|---|
| `GET /api/steg?aktivitetId={uuid}` | `rimfrost-process-vab-fe` | Svaret följer `GetProcessvyStegResponse` i `rimfrost-framework-process-openapi` |

---

## Integration med process-vab

BFF:en anropar `GET /processvy/steg?aktivitetId={uuid}` på process-vab via en REST-klient som
ärver det genererade gränssnittet `ProcessvyControllerApi` från
`rimfrost-framework-process-openapi-jaxrs-spec`. Svaret returneras enligt `GetProcessvyStegResponse`.
Valfria fält som saknas, till exempel stegets `beskrivning`, utelämnas i svaret.
