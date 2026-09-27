# Exception (Hata Yönetimi) Katmanı — Tasarım Rehberi

**Amaç:** Manager ve Agent uygulamalarında, hataları tutarlı, öngörülebilir ve istemci (Agent veya panel) için anlaşılır bir şekilde yönetmek. Bu döküman, hangi hata tiplerinin gerektiğini, hangi HTTP koduna karşılık geldiğini ve Spring Boot'ta merkezi hata yönetiminin nasıl kurulacağını anlatır.

---

## 1. Neden Merkezi Exception Katmanı?

Her controller/servis içinde ayrı ayrı `try/catch` yazmak yerine, Spring Boot'ta hataları **tek bir yerde** yakalayıp standart bir formata dönüştürebilirsiniz. Bunun için kullanılan mekanizma: `@RestControllerAdvice` (veya `@ControllerAdvice`) + `@ExceptionHandler`.

Avantajları:
- Kod tekrarı olmaz (her yerde try/catch yok)
- İstemci her zaman **aynı formatta** hata cevabı alır (tutarlılık)
- Yeni bir hata tipi eklemek tek bir yerde yapılır

---

## 2. Standart Hata Cevabı Formatı (Tasarlayın)

Her hata, istemciye şu yapıda dönmelidir (kendi alan adlarınızı belirleyebilirsiniz, ama tutarlı olun):

| Alan | Açıklama | Örnek |
|---|---|---|
| `timestamp` | Hatanın oluştuğu an | `2026-07-21T10:30:00Z` |
| `status` | HTTP durum kodu (sayı) | `404` |
| `error` | HTTP durum adı | `Not Found` |
| `code` | Uygulamaya özel hata kodu (aşağıdaki tablo) | `AGENT_NOT_FOUND` |
| `message` | İnsan tarafından okunabilir açıklama | `Belirtilen ID'ye sahip agent bulunamadı` |
| `path` | Hatanın oluştuğu endpoint | `/api/agents/42` |

**İpucu:** Bu yapıyı bir `record` (Java 17+) veya basit bir POJO olarak `ErrorResponse` adında tanımlayın. `@RestControllerAdvice` içindeki her handler bu nesneyi döndürsün.

**Güvenlik notu:** `message` alanında **asla** iç sistem detayı (stack trace, SQL sorgusu, dosya yolu) sızdırmayın — bu, saldırganlara bilgi verir. Genel, açıklayıcı ama detay içermeyen mesajlar kullanın. Detayları sadece sunucu loglarına yazın.

---

## 3. Uygulamaya Özel Exception Tipleri

Java'nın genel `RuntimeException`'ını kullanmak yerine, **anlamlı, kendi exception sınıflarınızı** tanımlayın. Bu, hangi hatanın ne anlama geldiğini kodda net gösterir ve `@ExceptionHandler`'da her birini ayrı ele almanızı sağlar.

### Önerilen Exception Hiyerarşisi

Bir temel sınıf (`AppException`) tanımlayıp, diğerlerini ondan türetmek iyi bir pratiktir — böylece hepsinde ortak alanlar (hata kodu, HTTP status) tutabilirsiniz.

```
AppException (temel, soyut)
├── ResourceNotFoundException      → 404
├── DuplicateResourceException     → 409
├── UnauthorizedException          → 401
├── ForbiddenException             → 403
├── ValidationException            → 400
├── AgentOfflineException          → 409 (veya 422)
├── CommandExecutionException      → 500
└── ExternalServiceException       → 502 / 503
```

**Düşünmeniz gereken:** Her exception sınıfı, kendi HTTP status'unu ve uygulama hata kodunu (`code`) taşımalı — bunları constructor'da alıp temel sınıfta saklayabilirsiniz. Böylece `@ExceptionHandler`'da tek bir handler ile `AppException`'ı yakalayıp, içindeki status/code bilgisine göre cevap üretebilirsiniz.

---

## 4. HTTP Durum Kodları — Ne Zaman Hangisi?

| HTTP Kodu | Adı | Ne Zaman Kullanılır | Sizin Senaryonuzda Örnek |
|---|---|---|---|
| **400** | Bad Request | İstemci geçersiz veri gönderdi (eksik alan, yanlış format) | Event gönderilirken `severity` alanı 1-5 dışında |
| **401** | Unauthorized | Kimlik doğrulama yok/geçersiz | Agent, geçersiz veya eksik API key ile istek attı |
| **403** | Forbidden | Kimlik doğrulandı ama yetki yok | Bir kullanıcı, başka müşterinin agent'ına erişmeye çalıştı (multi-tenancy) |
| **404** | Not Found | İstenen kaynak yok | `/api/agents/999` — böyle bir agent yok |
| **409** | Conflict | Kaynak durumu çakışması | Aynı hostname ile ikinci kez agent kaydı; ya da offline agent'a komut gönderme |
| **422** | Unprocessable Entity | Veri geçerli ama iş kuralına aykırı | Mantıksal doğrulama hatası (format doğru ama iş mantığı reddediyor) |
| **429** | Too Many Requests | Rate limit aşıldı | Bir agent çok sık istek atıyor (Faz 9 rate limiting) |
| **500** | Internal Server Error | Beklenmeyen sunucu hatası | Yakalanmamış bir exception, DB bağlantı hatası |
| **502** | Bad Gateway | Dış servis geçersiz cevap verdi | Müşteri Manager'ının API'si bozuk cevap döndü (Faz 11) |
| **503** | Service Unavailable | Dış servis erişilemez | Müşteri Manager'ı çevrimdışı (Faz 11, circuit breaker açık) |

---

## 5. Uygulamaya Özel Hata Kodları (`code` alanı)

HTTP kodu genel bir kategoridir; `code` alanı ise **tam olarak neyin yanlış gittiğini** söyler. İstemci (özellikle Agent) bu kodlara göre farklı davranabilir. Öneri kod seti:

### Agent / Kimlik Doğrulama
| Kod | HTTP | Anlamı |
|---|---|---|
| `AGENT_NOT_FOUND` | 404 | Belirtilen agent bulunamadı |
| `AGENT_ALREADY_EXISTS` | 409 | Bu hostname ile agent zaten kayıtlı |
| `INVALID_API_KEY` | 401 | API key geçersiz |
| `MISSING_API_KEY` | 401 | İstekte API key header'ı yok |
| `AGENT_OFFLINE` | 409 | Agent çevrimdışı, komut teslim edilemez |

### Event
| Kod | HTTP | Anlamı |
|---|---|---|
| `INVALID_EVENT_TYPE` | 400 | Bilinmeyen/geçersiz event tipi |
| `INVALID_SEVERITY` | 400 | Ciddiyet seviyesi 1-5 dışında |
| `EVENT_VALIDATION_FAILED` | 400 | Event verisi genel doğrulama hatası |

### Komut / İşlem
| Kod | HTTP | Anlamı |
|---|---|---|
| `COMMAND_NOT_FOUND` | 404 | Belirtilen komut bulunamadı |
| `COMMAND_EXECUTION_FAILED` | 500 | Agent komutu uygulayamadı |
| `INVALID_COMMAND` | 400 | Bilinmeyen/desteklenmeyen komut |

### Yetkilendirme / Multi-tenancy (Faz 11)
| Kod | HTTP | Anlamı |
|---|---|---|
| `ACCESS_DENIED` | 403 | Kullanıcının bu kaynağa yetkisi yok |
| `TENANT_MISMATCH` | 403 | Kaynak başka bir müşteriye ait |

### Sistem / Dış Servis
| Kod | HTTP | Anlamı |
|---|---|---|
| `RATE_LIMIT_EXCEEDED` | 429 | İstek limiti aşıldı |
| `INTERNAL_ERROR` | 500 | Beklenmeyen sunucu hatası |
| `EXTERNAL_SERVICE_ERROR` | 502 | Dış servis (müşteri Manager'ı) hata döndü |
| `EXTERNAL_SERVICE_UNAVAILABLE` | 503 | Dış servis erişilemez |

**İpucu:** Bu kodları bir `enum` olarak tanımlamak (örn. `ErrorCode`), yazım hatalarını önler ve tek yerden yönetim sağlar. Her enum değeri, ilgili HTTP status'u ve varsayılan mesajı da taşıyabilir.

---

## 6. Merkezi Handler'ın Yapısı (`@RestControllerAdvice`)

Global handler sınıfınızda şu tür handler'lar olmalı (mantığı; kodu kendiniz yazın):

1. **Kendi exception'larınız için:** `AppException`'ı (ve alt tiplerini) yakalayan handler'lar — exception içindeki status/code/message'ı `ErrorResponse`'a dönüştürür
2. **Spring'in fırlattığı doğrulama hataları için:** `MethodArgumentNotValidException` (`@Valid` başarısız olduğunda) — hangi alanların neden geçersiz olduğunu toplayıp 400 döner
3. **Kimlik/yetki hataları için:** Spring Security'nin `AuthenticationException`, `AccessDeniedException`'ları (Faz 9'da)
4. **Son çare (catch-all):** Genel `Exception` — yakalanmamış her şeyi 500'e çevirir, ama **detayını istemciye sızdırmaz**, sadece loglar

### Kritik Kural: Catch-all Handler Detay Sızdırmamalı
En alttaki genel `Exception` handler'ı, gerçek hata detayını (`e.getMessage()`, stack trace) **istemciye göndermemeli** — bunları `logger.error(...)` ile sunucu loguna yazın, istemciye sadece genel bir "Beklenmeyen bir hata oluştu" mesajı + `INTERNAL_ERROR` kodu dönün.

---

## 7. Agent Tarafında Hata Yönetimi (Farklı Bir Bakış)

Agent, bir **istemci** olduğu için hata yönetimi Manager'dan farklı düşünülmeli:

- **Manager'a istek atarken hata alırsa (401, 500 vb.):** Ne yapmalı? Örneğin 401 alırsa (API key geçersiz) tekrar denemenin anlamı yok — durup log yazmalı. Ama 503 alırsa (Manager geçici erişilemez), tekrar denemeli (retry).
- **Manager hiç erişilemezse (bağlantı hatası):** Event'i kaybetmemek için **yerel kuyruğa** almalı (Faz 9 offline kuyruk), sonra tekrar denemeli.
- **Retry mantığı:** Hangi hatalarda tekrar denenmeli, hangilerinde denenmemeli? (Geçici hatalar → retry; kalıcı hatalar → retry etme). Resilience4j bu ayrımı yapmayı kolaylaştırır.

**Düşünmeniz gereken:** HTTP durum kodlarını "geçici" (retry edilebilir: 429, 502, 503, timeout) ve "kalıcı" (retry edilemez: 400, 401, 403, 404) olarak iki gruba ayırın. Agent, bu ayrıma göre davranmalı.

---

## 8. Araştırma Anahtar Kelimeleri

- `spring boot restcontrolleradvice global exception handler`
- `spring boot custom exception hierarchy best practices`
- `spring boot error response record dto`
- `spring boot methodargumentnotvalidexception handle`
- `java enum error code http status mapping`
- `spring boot exception handling security accessdeniedexception`
- `resilience4j retry on specific http status` (Agent tarafı)

---

## 9. Uygulama Sırası (Öneri)

1. Önce `ErrorResponse` DTO'sunu (standart cevap formatı) tanımlayın
2. `ErrorCode` enum'unu (yukarıdaki kod tablosu) oluşturun
3. Temel `AppException` + birkaç alt sınıf (`ResourceNotFoundException`, `UnauthorizedException` gibi) yazın
4. `@RestControllerAdvice` global handler'ı yazıp bu exception'ları yakalayın
5. Bir endpoint'te bilerek hata fırlatıp (örn. olmayan bir agent'ı sorgulayıp), dönen cevabın standart formatta olduğunu test edin
6. `@Valid` doğrulama hatalarını yakalayan handler'ı ekleyin, test edin
7. En son catch-all (`Exception`) handler'ı ekleyin

---

## Kapanış Notu

İyi bir exception katmanı, projenin "görünmeyen kalitesidir" — kullanıcı fark etmez ama sistemin hata durumunda nasıl davrandığını belirler. Baştan kurmak, sonradan her yere try/catch serpiştirmekten çok daha temizdir. Kod tablolarını `enum` olarak merkezi tutmanız, projenin büyüdükçe tutarlı kalmasını sağlar. Agent tarafında ise hata yönetimi "istemci gözüyle" (retry, kuyruğa alma) düşünülmeli — bu, Manager'daki "sunucu gözüyle" yaklaşımdan farklıdır.
