# Krav — BFF för VAH:s processvy (PVAHBF)

## Bakgrund och syfte

Handläggaren ska se var i VAH-processen ärendet befinner sig när hen arbetar med en uppgift.
Processen äger sin processvy och levererar den som en egen mikrofrontend
(`rimfrost-process-vah-fe`), som portalen laddar bredvid uppgiftens mikrofrontend. Den här
BFF:en är mikrofrontendens backend: den hämtar processens steg från process-vah och lämnar dem
vidare, på samma sätt som reglernas BFF:er gör för reglernas mikrofrontends.

---

## Intressenter och aktörer

| Aktör | Roll |
|---|---|
| `rimfrost-process-vah-fe` | Anropar BFF:en för att visa processens steg |
| `rimfrost-process-vah` | Bakomliggande tjänst som beskriver processens steg (endpoint från `rimfrost-framework-process`) |
| Handläggare | Ser stegen i portalen. BFF:en skickar vidare handläggarens bearer-token |

---

## Funktionella krav

### PVAHBF-FR-01 — Processens steg

- **PVAHBF-FR-01.1** BFF:en ska bygga på `rimfrost-framework-bff`.
- **PVAHBF-FR-01.2** BFF:en ska erbjuda en endpoint som, givet ett aktivitets-ID, hämtar stegen
  från process-vah enligt `rimfrost-framework-process-openapi` och returnerar dem till
  mikrofrontenden.
- **PVAHBF-FR-01.3** Handläggarens bearer-token ska skickas vidare till process-vah, via
  framework-bff.
- **PVAHBF-FR-01.4** 404 från process-vah ska returneras som 404. Övriga fel ska mappas enligt
  framework-bff:s felhantering.

---

## Icke-funktionella krav

### PVAHBF-NFR-01 — Drift

- **PVAHBF-NFR-01.1** BFF:en ska ha en hälsokontroll mot process-vah.

---

## API-gränssnitt (översikt)

| API | Målgrupp | Specifikationsartefakt |
|---|---|---|
| `GET /api/steg?aktivitetId={uuid}` | `rimfrost-process-vah-fe` | Svaret följer `GetProcessvyStegResponse` i `rimfrost-framework-process-openapi` |

---

## Integration med process-vah

BFF:en anropar `GET /processvy/steg?aktivitetId={uuid}` på process-vah via en REST-klient som
ärver det genererade gränssnittet `ProcessvyControllerApi` från
`rimfrost-framework-process-openapi-jaxrs-spec`. Svaret returneras enligt `GetProcessvyStegResponse`.
Valfria fält som saknas, till exempel stegets `beskrivning`, utelämnas i svaret.
