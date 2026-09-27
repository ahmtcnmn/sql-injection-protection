# Exception Katmanı — Kullanım Kılavuzu

Bu döküman, `Exception_Katmani_Rehberi.md` dokümanına göre kurulan hata yönetimi
katmanının **projede nasıl kullanılacağını** anlatır. Yeni bir controller/servis
yazarken buraya bakın.

---

## 1. Katmanın Parçaları — Ne Nerede?

```
common/exception/
├── MessageType.java        → Hata kodu enum'u (mesaj + HttpStatus taşır)
├── ErrorMessage.java        → MessageType'tan insan-okunur mesaj üretir
└── ErrorResponse.java       → İstemciye dönen standart hata gövdesi (record)

common/exceptionController/
├── AppException.java              → Soyut temel exception (tüm alt tipler bunu extend eder)
├── ResourceNotFoundException.java → 404
├── DuplicateResourceException.java→ 409
├── UnauthorizedException.java     → 401
├── ForbiddenException.java        → 403
├── ValidationException.java       → 400
├── AgentOfflineException.java     → 409
├── CommandExecutionException.java → 500
├── ExternalServiceException.java  → 502 / 503
└── RootEntity.java                → Başarı cevabı zarfı ({result, messages, data})

common/handler/
└── GlobalExceptionHandler.java → @RestControllerAdvice, tüm exception'ları yakalar

common/RestBaseController.java → Controller'ların extend ettiği yardımcı sınıf (ok(...) vb.)
```

**Akış özetle:** Controller/servis → `MessageType` seçilir → ilgili `AppException`
alt sınıfı fırlatılır → `GlobalExceptionHandler` yakalar → `ErrorResponse` üretip
doğru HTTP status ile döner.

---

## 2. Yeni Bir Hata Fırlatmak İstiyorsanız

### Adım 1 — `MessageType`'da kod var mı kontrol edin

`common/exception/MessageType.java` içindeki enum listesine bakın. İhtiyacınız
olan durum zaten tanımlıysa (örn. `AGENT_NOT_FOUND`) yeni bir şey eklemenize
gerek yok.

Eğer yoksa, enum'a yeni bir değer ekleyin — **mesaj + doğru HttpStatus** ile:

```java
AGENT_NOT_FOUND("Agent not found", HttpStatus.NOT_FOUND),
```

HTTP kodunu seçerken `Exception_Katmani_Rehberi.md` §4'teki tabloyu referans
alın (400/401/403/404/409/422/429/500/502/503).

### Adım 2 — Doğru `AppException` alt sınıfını seçin

| Durum | Fırlatılacak sınıf |
|---|---|
| Kaynak bulunamadı | `ResourceNotFoundException` |
| Kaynak zaten var / çakışma | `DuplicateResourceException` |
| Kimlik doğrulama yok/geçersiz | `UnauthorizedException` |
| Yetki yok | `ForbiddenException` |
| Girdi doğrulama hatası (elle fırlatılan) | `ValidationException` |
| Agent çevrimdışı | `AgentOfflineException` |
| Komut çalıştırma hatası | `CommandExecutionException` |
| Dış servis hatası/erişilemez | `ExternalServiceException` |

### Adım 3 — Servis/Controller içinde fırlatın

```java
if (agent == null) {
    throw new ResourceNotFoundException(MessageType.AGENT_NOT_FOUND, "id=" + id);
}
```

İki constructor seçeneği var:
- `new ResourceNotFoundException(MessageType.AGENT_NOT_FOUND)` → sadece enum mesajı
- `new ResourceNotFoundException(MessageType.AGENT_NOT_FOUND, "id=" + id)` → enum
  mesajına ek detay (loglarda ve response `message` alanında görünür)

**Önemli:** `detail` parametresine asla stack trace, SQL sorgusu, dosya yolu gibi
iç sistem bilgisi koymayın — sadece iş bağlamı (id, hostname gibi) ekleyin.

---

## 3. `GlobalExceptionHandler` Ne Yakalıyor?

`common/handler/GlobalExceptionHandler.java` şu sırayla devrededir:

1. **`AppException` (ve tüm alt tipleri)** — kendi `HttpStatus` + `code`'unu
   taşıdığı için tek handler yeterli. Ekstra bir şey yapmanıza gerek yok, yeni
   bir alt sınıf eklediğinizde otomatik yakalanır.
2. **`MethodArgumentNotValidException`** — `@Valid` ile işaretli `@RequestBody`
   doğrulaması başarısız olduğunda (bkz. §4). Alan bazlı hatalar toplanıp 400
   olarak döner.
3. **`MissingServletRequestParameterException` / `MethodArgumentTypeMismatchException`**
   — eksik/yanlış tipte path veya query parametresi (örn. `/api/agents/abc`
   gibi `Long` beklenen yere string gönderilmesi). 400 döner, iç detay
   sızdırılmaz, sadece sunucu logunda tutulur.
4. **`AuthenticationException` / `AccessDeniedException`** (Spring Security) —
   sırasıyla 401 / 403 olarak standart formata çevrilir.
5. **Catch-all `Exception`** — yukarıdakilerin hiçbiri eşleşmezse (beklenmeyen
   `NullPointerException`, DB bağlantı hatası vb.) 500 döner. **`e.getMessage()`
   istemciye asla gönderilmez** — sadece `MessageType.INTERNAL_ERROR`'ın genel
   mesajı gider, gerçek hata `log.error(...)` ile sunucu loguna yazılır.

Yeni bir handler eklemeniz gerekmez — mevcut `AppException` hiyerarşisini
kullanmak yeterlidir. Sadece Spring'in fırlattığı **yeni bir framework
exception**'ını (örn. yeni bir `HttpMessageNotReadableException` senaryosu)
özel olarak ele almak isterseniz `GlobalExceptionHandler`'a yeni bir
`@ExceptionHandler` metodu ekleyin.

---

## 4. `@Valid` ile Otomatik Doğrulama Kullanmak

Request body doğrulamasını manuel exception fırlatarak değil, Jakarta
Validation anotasyonlarıyla yapın — `MethodArgumentNotValidException` otomatik
yakalanır:

```java
public record CreateAgentRequest(
        @NotBlank(message = "hostname boş olamaz") String hostname,
        @Min(value = 1, message = "severity 1-5 arası olmalı")
        @Max(value = 5, message = "severity 1-5 arası olmalı") int severity) {}

@PostMapping
public RootEntity<AgentDto> create(@Valid @RequestBody CreateAgentRequest request) {
    ...
}
```

Doğrulama başarısız olursa istemci şunu alır:

```json
{
  "timestamp": "2026-07-28T09:20:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "EVENT_VALIDATION_FAILED",
  "message": "hostname: hostname boş olamaz; severity: severity 1-5 arası olmalı",
  "path": "/api/agents"
}
```

---

## 5. Başarı Cevabı — `RootEntity` / `RestBaseController`

Controller'lar `RestBaseController`'ı extend ederse `ok(...)` yardımcı metodunu
kullanabilir:

```java
@RestController
@RequestMapping("/api/agents")
public class AgentController extends RestBaseController implements IAgentController {

    @GetMapping("/{id}")
    public RootEntity<AgentDto> getAgentById(@PathVariable Long id) {
        AgentDto dto = agentService.findById(id); // bulunamazsa servis exception fırlatır
        return ok(dto);
    }
}
```

Başarı cevabı formatı:

```json
{ "result": true, "messages": null, "data": { ... } }
```

**Not:** `RestBaseController.error(...)` metotları hâlâ mevcut (manuel
`RootEntity.failure(...)` üretmek için) ama **tercih edilen yol exception
fırlatmaktır** — `error(...)` metodunu kullanırsanız `GlobalExceptionHandler`
devreye girmez, HTTP status'u siz elle ayarlamak zorunda kalırsınız. Yeni kodda
`error(...)` yerine ilgili `AppException` alt sınıfını fırlatın.

---

## 6. Örnek Uçtan Uca Akış (Bu Oturumda Eklenen Endpoint)

`Agent/controller/AgentController.java` içinde `GET /api/agents/{id}`:

```java
@GetMapping("/{id}")
public RootEntity<String> getAgentById(@PathVariable Long id) {
    if (id == null || id != KNOWN_AGENT_ID) {
        throw new ResourceNotFoundException(MessageType.AGENT_NOT_FOUND, "id=" + id);
    }
    return ok("Agent#" + id);
}
```

Gerçek test sonuçları:

| İstek | HTTP | Cevap |
|---|---|---|
| `GET /api/agents/1` | 200 | `{"result":true,"messages":null,"data":"Agent#1"}` |
| `GET /api/agents/999` | 404 | `{"timestamp":"...","status":404,"error":"Not Found","code":"AGENT_NOT_FOUND","message":"Agent not found : id=999","path":"/api/agents/999"}` |
| `GET /api/agents/abc` | 400 | `{"timestamp":"...","status":400,"error":"Bad Request","code":"BAD_REQUEST","message":"İstek parametreleri geçersiz","path":"/api/agents/abc"}` |
| Auth header yok | 401 | Spring Security'nin kendi 401'i (henüz özelleştirilmedi) |

Bu endpoint, gerçek `Agent` entity/repository katmanı yazılana kadar geçici bir
demo amaçlıdır — repository/service katmanı eklendiğinde `KNOWN_AGENT_ID`
kontrolü kaldırılıp gerçek DB sorgusuna bağlanmalıdır.

---

## 7. Sık Yapılan Hatalar (Yapmayın)

- **`RuntimeException` veya generic `Exception` fırlatmayın.** Her zaman
  `AppException` alt sınıflarından birini kullanın — aksi halde catch-all
  handler'a düşer ve 500 + generic mesaj döner (yanlış HTTP kodu, kaybolan
  bağlam).
- **Controller içinde manuel `try/catch` + `ResponseEntity.status(...)`
  yazmayın.** Bu, `GlobalExceptionHandler`'ın var olma amacını (tek yerden
  yönetim) baypas eder.
- **`detail` alanına stack trace veya exception mesajı (`ex.getMessage()`)
  koymayın.** Sadece sizin yazdığınız, iş bağlamı taşıyan kısa string'ler.
- **Yeni bir `MessageType` eklerken `HttpStatus`'u unutmayın.** Enum
  constructor'ı ikisini birlikte zorunlu kılar, ama yanlış status seçmek
  (örn. çakışma senaryosuna 400 vermek) dokümanın §4 tablosunu ihlal eder.

---

## 8. Agent Tarafı (İleride, Faz 9+)

Bu kılavuz Manager (sunucu) tarafını kapsar. Agent, Manager'a istek atan bir
istemci olduğunda `Exception_Katmani_Rehberi.md` §7'deki ayrımı uygulamalı:
- 401/403/404 gibi kalıcı hatalarda retry yapılmaz.
- 429/502/503 gibi geçici hatalarda retry (Resilience4j vb.) uygulanır.
- Manager'a hiç ulaşılamıyorsa event yerel kuyruğa alınır.

Bu kısım henüz kodlanmadı; Agent tarafı yazılırken ayrı bir oturumda ele
alınmalı.
