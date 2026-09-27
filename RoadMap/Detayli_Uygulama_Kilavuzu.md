# Güvenlik İzleme Sistemi — Detaylı Uygulama Kılavuzu (Spring Boot)

**Bu döküman nasıl kullanılır:** Her faz, küçük ve somut **adımlara** bölünmüştür. Her adımda ne yapacağınız net yazılıdır. Zor/kritik yerlerde (is_online, API key üretimi, ilişki kurma gibi) **örnek kod** verilmiştir — bunları anlayarak uygulayın, körü körüne kopyalamayın. Kolay/tekrar eden yerlerde sadece yönlendirme + araştırma kelimesi vardır.

**Efsane:**
- 📝 = Sizin yazacağınız/karar vereceğiniz kısım
- 💡 = İpucu / dikkat edilecek nokta
- 🔍 = Araştırma anahtar kelimesi
- ⌨️ = Örnek kod (anlayarak kullanın)

---

# FAZ 2: Manager İskeleti (Spring Boot + JPA + MySQL)

## Adım 2.1 — Projeyi Oluştur
1. `start.spring.io`'ya git
2. Project: Maven, Language: Java, Spring Boot: 3.x (en güncel stable)
3. Bağımlılıklar: **Spring Web, Spring Data JPA, MySQL Driver, Validation, Lombok, Spring Boot DevTools**
4. Generate → indir → IntelliJ'de aç

## Adım 2.2 — MySQL Bağlantısını Yapılandır
📝 `src/main/resources/application.yml` (veya `.properties`) dosyasına veritabanı bağlantısını ekle.

⌨️ Örnek (`application.yml`):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/security_manager
    username: ${DB_USER}      # ortam değişkeninden okunur, koda gömülmez
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update        # tabloları otomatik oluşturur/günceller (geliştirme için)
    show-sql: true            # çalışan SQL'i konsola yazar (öğrenirken faydalı)
    properties:
      hibernate:
        format_sql: true
```

💡 MySQL'de önce `security_manager` veritabanını ve projeye özel bir kullanıcı oluşturmalısın (root kullanma):
⌨️
```sql
CREATE DATABASE security_manager;
CREATE USER 'sec_user'@'localhost' IDENTIFIED BY 'guclu_sifre';
GRANT ALL PRIVILEGES ON security_manager.* TO 'sec_user'@'localhost';
FLUSH PRIVILEGES;
```

💡 Ortam değişkenlerini (`DB_USER`, `DB_PASSWORD`) IntelliJ'de Run Configuration → Environment variables kısmından verebilirsin.

## Adım 2.3 — Health Check Endpoint'i
📝 `controller` paketinde bir `HealthController` oluştur, `GET /api/health` isteğine `{"status":"ok"}` dönsün.

⌨️ Örnek:
```java
@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
```

✅ **Test:** Uygulamayı çalıştır, tarayıcıdan `http://localhost:8080/api/health` → `{"status":"ok"}` görmelisin.

## Adım 2.4 — Agent Entity'si
📝 `entity` paketinde `Agent` sınıfını oluştur.

⌨️ Örnek (temel iskelet):
```java
@Entity
@Table(name = "agents")
@Getter @Setter              // Lombok — getter/setter otomatik
@NoArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hostname;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "last_seen")
    private Instant lastSeen;   // UTC — zaman dilimi tutarlılığı için Instant

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    // is_online mantığı — Adım 2.6'da ekleyeceğiz
}
```

## Adım 2.5 — Event Entity'si
📝 `entity` paketinde `Event` sınıfını oluştur, `Agent` ile `@ManyToOne` ilişki kur.

⌨️ Örnek:
```java
@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;          // hangi agent'tan geldiği — İLİŞKİ BURADA kurulur

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Instant timestamp;

    @Min(1) @Max(5)              // severity 1-5 arası — Bean Validation
    private Integer severity;

    @Column(columnDefinition = "TEXT")
    private String rawData;
}
```

💡 **İlişki nasıl çalışıyor?** `Event` tarafındaki `@ManyToOne` + `@JoinColumn(name="agent_id")` → events tablosunda `agent_id` diye bir foreign key sütunu oluşturur. `Agent` tarafındaki `@OneToMany(mappedBy="agent")` → "bu ilişkinin sahibi Event'teki `agent` alanı" der. Yani ilişkiyi **Event tarafı sahiplenir**, Agent tarafı sadece "yansıması"dır.

## Adım 2.6 — is_online Mantığı (TAKILDIĞIN YER)
📝 Bir agent'ın "online" olup olmadığı, `lastSeen`'in son 60 saniye içinde olup olmadığına bakılarak hesaplanır. Bunu **entity içinde bir metod** olarak yaz (veritabanında saklanan bir alan DEĞİL, anlık hesaplanan bir değer).

⌨️ `Agent` sınıfının içine ekle:
```java
@Transient   // Bu alan veritabanına KAYDEDİLMEZ, sadece hesaplanır
public boolean isOnline() {
    if (lastSeen == null) {
        return false;
    }
    return lastSeen.isAfter(Instant.now().minusSeconds(60));
}
```

💡 `@Transient` anotasyonu kritik: "bu metodun sonucunu veritabanına bir sütun olarak yazma, sadece Java tarafında hesapla" demek. Python'daki `hybrid_property`'nin en basit karşılığı bu.

💡 **Eğer SQL sorgusunda da "online olanları getir" demek istersen** (örn. sadece online agent'ları listeleme), o zaman repository'de özel bir sorgu yazarsın (Adım 2.8'de).

## Adım 2.7 — Repository'ler
📝 `repository` paketinde iki interface: `AgentRepository` ve `EventRepository`. İkisi de `JpaRepository`'yi extend etsin (JPA Repository'yi zaten biliyorsun).

⌨️ Örnek:
```java
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByHostname(String hostname);
    Optional<Agent> findByApiKey(String apiKey);   // API key doğrulama için (Faz 3)
}
```

💡 `findByHostname`, `findByApiKey` gibi metod isimlerini Spring Data JPA otomatik olarak SQL'e çevirir — sen sadece isim verirsin, gövde yazmazsın. Bu, "query method" özelliğidir.

## Adım 2.8 — (İsteğe bağlı) Online Agent'ları SQL'de Sorgulama
📝 Eğer veritabanı seviyesinde online filtrelemek istersen:
⌨️
```java
@Query("SELECT a FROM Agent a WHERE a.lastSeen > :threshold")
List<Agent> findOnlineAgents(@Param("threshold") Instant threshold);
```
Çağırırken: `agentRepository.findOnlineAgents(Instant.now().minusSeconds(60));`

✅ **Faz 2 Testi:** Uygulamayı çalıştır. MySQL'e bak — `agents` ve `events` tabloları otomatik oluşmuş olmalı. Health endpoint çalışmalı.

---

# FAZ 3: Agent Kayıt ve API Key Doğrulama

## Adım 3.1 — Kayıt İçin DTO'lar
📝 İki DTO oluştur (`dto` paketinde):
- `RegisterRequest` — agent'ın gönderdiği veri (sadece `hostname`)
- `RegisterResponse` — geri döndürülen veri (`agentId` + `apiKey`)

⌨️ Örnek:
```java
public record RegisterRequest(
    @NotBlank String hostname     // boş olamaz — Bean Validation
) {}

public record RegisterResponse(
    Long agentId,
    String apiKey
) {}
```

💡 `record` (Java 17+), sadece veri taşıyan sınıflar için kısa yol — getter'lar, constructor, equals/hashCode otomatik gelir.

## Adım 3.2 — Güvenli API Key Üretimi (TAKILDIĞIN YER)
📝 Agent kayıt olurken, tahmin edilemez bir API key üretmelisin. `Math.random()` veya `UUID` YETERİNCE GÜVENLİ DEĞİL — `SecureRandom` kullan.

⌨️ Örnek bir yardımcı metod (örn. `service` katmanında):
```java
private static final SecureRandom secureRandom = new SecureRandom();

public String generateApiKey() {
    byte[] randomBytes = new byte[32];        // 32 byte = 256 bit, güçlü
    secureRandom.nextBytes(randomBytes);
    // URL-safe base64 string'e çevir (header'da taşınabilir olsun)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
}
```

💡 Neden `SecureRandom`? Normal `Random`, tahmin edilebilir bir algoritma kullanır — saldırgan bir key'i bilirse diğerlerini tahmin edebilir. `SecureRandom` kriptografik olarak güvenlidir.

💡 **İleri seviye (Faz 9):** Key'i veritabanında düz metin yerine hash'leyerek saklamak daha güvenli (`BCrypt`). Şimdilik düz metin ile başla, Faz 9'da sıkılaştırırsın.

## Adım 3.3 — Kayıt Servisi
📝 `service` paketinde `AgentService` oluştur. `register` metodu:
1. Aynı hostname var mı kontrol et (varsa hata fırlat — `DuplicateResourceException`)
2. Yeni bir `Agent` nesnesi oluştur, API key üret, `hostname`'i ata
3. `agentRepository.save(agent)` ile kaydet
4. `RegisterResponse` döndür

📝 **Karar senin:** Aynı hostname ile tekrar kayıt gelirse ne olsun? (Hata fırlat / yeni key üret / eski kaydı güncelle) — bilinçli seç.

## Adım 3.4 — Kayıt Controller'ı
📝 `controller` paketinde `AgentController`, `POST /api/agents/register` endpoint'i.

⌨️ Örnek:
```java
@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor        // Lombok — final alanlar için constructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return agentService.register(request);
    }
}
```

✅ **Test:** Postman/curl ile `POST /api/agents/register` → body: `{"hostname":"test-1"}` → cevapta bir `apiKey` dönmeli. Aynı hostname'i tekrar gönder → seçtiğin davranışı (hata vb.) gör.

## Adım 3.5 — API Key Doğrulama Mekanizması (KRİTİK)
📝 Heartbeat ve event endpoint'lerine gelen her istekte, header'daki API key'in geçerli bir agent'a ait olduğunu doğrulaman gerekiyor. Bunu her endpoint'te tekrar yazmamak için **ortak bir yapı** kur. İki yaygın yol:

**Yol A — HandlerInterceptor (başlangıç için daha basit):**
⌨️ Mantık:
```java
@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final AgentRepository agentRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey == null) {
            response.setStatus(401);
            return false;   // isteği durdur
        }
        Optional<Agent> agent = agentRepository.findByApiKey(apiKey);
        if (agent.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        // Doğrulanan agent'ı request'e ekle ki controller kullanabilsin
        request.setAttribute("authenticatedAgent", agent.get());
        return true;   // devam et
    }
}
```

💡 Sonra bu interceptor'ı bir config sınıfında kaydedip, hangi path'lere uygulanacağını belirtirsin (`/api/heartbeat`, `/api/events` gibi — ama `/api/agents/register`'a UYGULAMA, çünkü kayıt olurken henüz key yok).

🔍 `spring boot webmvcconfigurer add interceptor path patterns`

**Yol B — Spring Security filter** (daha güçlü, Faz 9'da geçebilirsin). Başlangıçta Yol A yeterli.

✅ **Test:** Doğru key ile korumalı bir endpoint'e istek → geçer. Yanlış/eksik key → 401.

---

# FAZ 4: Heartbeat ve Event Toplama

## Adım 4.1 — Heartbeat Endpoint'i
📝 `POST /api/heartbeat`. Interceptor zaten agent'ı doğruladı ve request'e ekledi. Controller:
1. Doğrulanan agent'ı al (`request.getAttribute("authenticatedAgent")`)
2. `lastSeen`'i `Instant.now()` yap
3. Kaydet

⌨️ Agent'ı controller'da almak için örnek:
```java
@PostMapping("/heartbeat")
public ResponseEntity<Void> heartbeat(HttpServletRequest request) {
    Agent agent = (Agent) request.getAttribute("authenticatedAgent");
    agent.setLastSeen(Instant.now());
    agentRepository.save(agent);
    return ResponseEntity.ok().build();
}
```

💡 Daha temiz bir yol: custom bir `@AuthenticationPrincipal` benzeri argument resolver yazmak — ama başlangıçta `request.getAttribute` yeterli.

## Adım 4.2 — Event DTO'su
📝 `EventRequest` DTO'su: `eventType`, `severity`, `rawData`, (opsiyonel) `timestamp`.
⌨️
```java
public record EventRequest(
    @NotBlank String eventType,
    @Min(1) @Max(5) Integer severity,
    String rawData
) {}
```

## Adım 4.3 — Event Kaydetme Endpoint'i
📝 `POST /api/events`. Doğrulanan agent'ı al, yeni `Event` oluştur, agent ile ilişkilendir, `timestamp`'i sunucuda ata, kaydet.

💡 **Karar:** `timestamp`'i agent mı gönderiyor, sunucu mu atıyor? Öneri: sunucu (`Instant.now()`) — agent'ın saati yanlış olabilir. Agent'ın kendi tespit zamanını da önemsiyorsan, iki ayrı alan tutabilirsin (`detectedAt` = agent, `receivedAt` = sunucu).

## Adım 4.4 — Agent Tarafı (Spring Boot Client)
📝 Ayrı `agent` projesinde:
1. `application.yml`'ye Manager adresi + API key ekle
2. `@Scheduled` ile periyodik heartbeat gönderen bir bileşen yaz
3. Manager'a HTTP için `RestClient` kullan

⌨️ Zamanlanmış heartbeat örneği:
```java
@Component
@RequiredArgsConstructor
public class HeartbeatTask {

    private final RestClient restClient;   // config'de tanımlanır

    @Value("${manager.api-key}")
    private String apiKey;

    @Scheduled(fixedRate = 30000)          // her 30 saniyede bir
    public void sendHeartbeat() {
        try {
            restClient.post()
                .uri("/api/heartbeat")
                .header("X-API-KEY", apiKey)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            // Manager erişilemez — logla, çökme (Faz 9'da offline kuyruk)
            log.warn("Heartbeat gönderilemedi: {}", e.getMessage());
        }
    }
}
```

💡 `@Scheduled`'ın çalışması için ana uygulama sınıfına `@EnableScheduling` eklemen gerekir.

🔍 `spring boot restclient bean configuration base url`
🔍 `spring boot enablescheduling`

✅ **Test:** Agent'ı çalıştır. Manager'da ilgili agent'ın `lastSeen`'inin her 30 saniyede güncellendiğini, `isOnline()`'ın `true` döndüğünü gör.

---

# FAZ 5: Agent — Log Takibi ve SQL Injection Tespiti

## Adım 5.1 — Test Ortamı
📝 VDS'te bir web sunucusu (Nginx) kur, erişim logunun yerini bul (`/var/log/nginx/access.log`).

## Adım 5.2 — Log "Tail" Bileşeni
📝 Log dosyasını sürekli izleyen bir bileşen. Sıfırdan yazmak yerine **Apache Commons IO'nun `Tailer`** sınıfını kullan.
🔍 `apache commons io tailer example listener`

💡 `Tailer`, sana her yeni satır geldiğinde tetiklenen bir `TailerListener` verir — sen sadece "yeni satır gelince ne yapılacağını" yazarsın.

## Adım 5.3 — SQL Injection Dedektörü
📝 `detector` paketinde bir `SqliDetector`. Regex paternlerini bir listede tut, her satırı kontrol et.
⌨️ Örnek:
```java
@Component
public class SqliDetector {

    private static final List<Pattern> PATTERNS = List.of(
        Pattern.compile("('|\")\\s*(or|and)\\s*('|\")?\\d+('|\")?\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("union\\s+(all\\s+)?select", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(--|#|/\\*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(drop\\s+table|xp_cmdshell|sleep\\s*\\()", Pattern.CASE_INSENSITIVE)
    );

    public boolean isSuspicious(String logLine) {
        return PATTERNS.stream().anyMatch(p -> p.matcher(logLine).find());
    }
}
```

💡 False positive dengesi: `(--|#)` gibi paternler normal metinlerde de geçebilir. Başlangıçta geniş tut, gerçek loglarla test edip daralt.

## Adım 5.4 — Tespit Edileni Manager'a Gönder
📝 Şüpheli satır bulununca, `EventRequest` oluşturup Manager'ın `POST /api/events`'ine gönder (Faz 4'teki `RestClient` mantığıyla).

## Adım 5.5 — (İleri) GraalVM Native Image
📝 Agent'ın RAM'ini düşürmek için native image derlemeyi araştır.
🔍 `spring boot 3 graalvm native image maven`
💡 Bu opsiyonel ve biraz karmaşık — önce her şey çalışsın, sonra optimize et.

✅ **Test:** Nginx'e tarayıcıdan bir SQLi denemesi (`?id=1' OR '1'='1`) gönder. Agent'ın bunu yakalayıp Manager'a event gönderdiğini, Manager'da event'in kaydedildiğini gör.

---

# FAZ 6: Agent — Veritabanı ve Sistem Logları

## Adım 6.1 — DB Log Takibi
📝 MySQL/PostgreSQL error logunun yerini bul, aynı `Tailer` mantığıyla izle. Başarısız login denemeleri, yetkisiz erişim gibi satırları yakala, farklı bir `eventType` ("db_auth_failure") ile gönder.

## Adım 6.2 — Sistem Logları (journalctl)
📝 `ProcessBuilder` ile `journalctl -f` başlat, çıktısını satır satır oku.
⌨️ Örnek iskelet:
```java
ProcessBuilder pb = new ProcessBuilder("journalctl", "-f", "-n", "0");
pb.redirectErrorStream(true);
Process process = pb.start();
try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream()))) {
    String line;
    while ((line = reader.readLine()) != null) {   // her yeni satırda döner
        // satırı analiz et, gerekirse event gönder
    }
}
```

💡 Bu okuma **bloke edicidir** (satır gelene kadar bekler). Ana uygulamayı kilitlememek için ayrı bir thread'de çalıştır.

## Adım 6.3 — Paralel Çalıştırma
📝 Nginx log, DB log, journalctl — üçü aynı anda izlenmeli. Her birini ayrı bir görev olarak çalıştır.
🔍 `spring boot @async enableasync executor`
🔍 `java 21 virtual threads executors`

💡 Java 21 virtual threads, bu tür "çok sayıda bloke edici I/O görevi" için ideal — her izleyiciye bir virtual thread verebilirsin.

---

# FAZ 7: Dashboard

## Adım 7.1 — Yaklaşım Seçimi
📝 Karar: Thymeleaf (sunucu render, hızlı) mı, ayrı REST + frontend (esnek, ürün için) mi?
💡 Öğrenme/hız için Thymeleaf ile başla, ileride ayrı frontend'e geçebilirsin.

## Adım 7.2 — Veri Endpoint'leri
📝 Dashboard'un göstereceği veriler için endpoint'ler:
- `GET /api/agents` — tüm agent'lar + online durumu
- `GET /api/events` — son event'ler (sayfalama ile — `Pageable`)
🔍 `spring data jpa pageable pagination`

## Adım 7.3 — Görselleştirme
📝 Thymeleaf template'i ile agent listesi + event tablosu. Online agent'ları yeşil, offline'ları gri göster.

## Adım 7.4 — (İleri) Canlı Log Akışı
📝 Event'lerin canlı akması için `SseEmitter` (Server-Sent Events).
🔍 `spring boot sseemitter example`
💡 Bu, Python prototipinde yaptığın SSE'nin Java karşılığı.

---

# FAZ 8: Uzaktan Komut / Politika Gönderme

## Adım 8.1 — Command Entity'si
📝 `Command` entity: id, agent (ManyToOne), commandType, payload, status (enum: PENDING/DELIVERED/DONE), createdAt.
💡 `status` için bir Java `enum` kullan, String yerine.

## Adım 8.2 — Komut Oluşturma
📝 Manager'da `POST /api/agents/{id}/commands` — belirli bir agent'a komut sıraya koyar (status=PENDING).

## Adım 8.3 — Agent'ın Komut Sorgulaması
📝 Agent, heartbeat'e ek olarak `GET /api/commands` ile bekleyen komutları çeker.
⌨️ Repository sorgusu:
```java
List<Command> findByAgentAndStatus(Agent agent, CommandStatus status);
```

## Adım 8.4 — Komut Uygulama ve Geri Bildirim
📝 Agent komutu uygular, sonra Manager'a `POST /api/commands/{id}/result` ile "DONE" bilgisini gönderir.

💡 **Komut türlerini** basit tut başta: "restart_watcher", "update_config" gibi. `block_ip` komutu Faz 10'da eklenecek.

---

# FAZ 9: Güvenlik Sıkılaştırma (Spring Security)

## Adım 9.1 — API Key'i Hash'le
📝 Faz 3'te düz metin sakladığın API key'i artık `BCrypt` ile hash'le. Kayıtta hash'le sakla, doğrulamada `matches()` ile karşılaştır.
🔍 `spring security bcryptpasswordencoder encode matches`

## Adım 9.2 — Panel Kullanıcı Yönetimi
📝 Sen (ve ileride müşteriler) panele giriş yapacak. `User` entity + rol + Spring Security ile login.
🔍 `spring security jwt authentication tutorial`
🔍 `spring security userdetailsservice`

## Adım 9.3 — Rate Limiting
📝 Bir agent'ın Manager'ı boğmasını engelle. Bucket4j kütüphanesi.
🔍 `spring boot bucket4j rate limiting filter`

## Adım 9.4 — HTTPS
📝 Manager-Agent iletişimini şifrele. Geliştirmede self-signed sertifika yeterli.
🔍 `spring boot enable https ssl self signed`

## Adım 9.5 — Agent Offline Kuyruğu
📝 Manager erişilemezse event kaybolmasın. Agent'ta gömülü **H2** veritabanı (dosya modunda) veya basit bir dosya kuyruğu.
1. Event gönderilemezse → yerel kuyruğa yaz
2. Periyodik olarak (`@Scheduled`) kuyruktaki bekleyenleri göndermeyi dene
3. Başarılı gönderilenleri kuyruktan sil
🔍 `spring boot embedded h2 file mode`
🔍 `resilience4j retry spring boot`

💡 Faz 4'te heartbeat'te yazdığın `try/catch`'in "çökme" davranışını, burada "kuyruğa al" davranışına dönüştürüyorsun.

---

# FAZ 10: Aktif Engelleme (IPS Benzeri Otomatik Blokaj)

## Adım 10.1 — Manager'da Eşik Mantığı
📝 Bir SQLi event'i geldiğinde, aynı kaynak IP'den son X saniyede kaç event geldiğini say. Eşiği aşarsa → `block_ip` komutu üret (Faz 8 kuyruğuna ekle).
💡 Kaynak IP'yi event'in `rawData`'sından parse etmen gerekebilir — log formatına göre.

## Adım 10.2 — Whitelist (KRİTİK GÜVENLİK)
📝 Engellemeden ÖNCE, IP'nin whitelist'te olup olmadığını kontrol et. Whitelist'te: Manager IP'si, senin test/geliştirme IP'lerin.
💡 Bunu atlarsan, DSM'de yaşadığın gibi kendini kilitleyebilirsin.

## Adım 10.3 — Agent'ta Engelleme
📝 Agent, `block_ip` komutunu alınca `ProcessBuilder` ile `ufw` veya `iptables` çalıştırır.
⌨️ Örnek:
```java
new ProcessBuilder("sudo", "ufw", "deny", "from", targetIp).start();
```
💡 Agent'ın `sudo` yetkisi olması gerekir (dikkatli yapılandır).

## Adım 10.4 — Süreli Engelleme
📝 Kalıcı engelleme yönetimi zorlaştırır. Belirli süre sonra engeli kaldıran bir mekanizma yaz (zamanlayıcı veya "süresi dolanları kontrol et" döngüsü).

## Adım 10.5 — Engelleme Olayını Bildir
📝 Her engellemeyi Manager'a event olarak gönder (`event_type: "ip_blocked"`), dashboard'da görünsün.

---

# FAZ 11: Çoklu Müşteri / Üst-Katman Panel (Ticari Hedef)

## Adım 11.1 — Multi-Tenancy Kararı
📝 Her müşterinin verisi izole olmalı. İki strateji:
- **Tek DB, tenant_id sütunu:** Her tabloya `tenant_id` ekle, her sorguda filtrele. Basit ama izolasyon zayıf.
- **Müşteri başına ayrı şema/DB:** Güçlü izolasyon, daha karmaşık yönetim.
💡 Başlangıç için tek DB + tenant_id yeterli, ölçek büyüyünce yeniden değerlendir.
🔍 `spring boot multi tenancy discriminator column`

## Adım 11.2 — Müşteri Manager'larına Bağlanma
📝 Eğer müşterilerin kendi Manager'ları varsa, senin üst-katmanın onların REST API'lerine bağlanıp veri toplar. Her müşteri için bir "connector" yapılandırması (URL, kimlik bilgisi).

## Adım 11.3 — Dayanıklılık (Resilience4j)
📝 Bir müşterinin Manager'ı çökerse/yavaşlarsa senin panelin etkilenmemeli.
🔍 `resilience4j circuit breaker timeout retry spring boot`
💡 Circuit breaker: bir servis sürekli hata veriyorsa, bir süre ona istek atmayı bırak (boşuna bekleme).

## Adım 11.4 — Canlı Terminal / Toplu Komut (WebSocket)
📝 Birden fazla agent'a aynı anda komut + canlı çıktı. Polling yetmez, **WebSocket** gerekir.
🔍 `spring websocket stomp tutorial`
🔍 `spring boot websocket broadcast multiple sessions`
💡 Bu en karmaşık faz — temel sistem tamamen oturmadan buraya girme.

---

# GENEL İLERLEME KONTROL LİSTESİ

- [x] Faz 1 — Ortam ve ağ
- [ ] Faz 2 — Manager iskeleti (entity, repository, is_online, health)
- [ ] Faz 3 — Agent kayıt + API key üretimi + doğrulama interceptor
- [ ] Faz 4 — Heartbeat + event toplama + agent client
- [ ] Faz 5 — Nginx log takibi + SQLi tespiti
- [ ] Faz 6 — DB/sistem logları + paralel izleme
- [ ] Faz 7 — Dashboard
- [ ] Faz 8 — Komut kuyruğu
- [ ] Faz 9 — Güvenlik (BCrypt, kullanıcı yönetimi, rate limit, HTTPS, offline kuyruk)
- [ ] Faz 10 — Otomatik IP engelleme + whitelist
- [ ] Faz 11 — Multi-tenancy + dış Manager entegrasyonu + WebSocket

---

# NASIL İLERLEMELİ?

1. Her adımı **sırayla** yap, atlama
2. Her fazın sonundaki ✅ **testi geçmeden** bir sonraki faza geçme
3. Örnek kodları **anlayarak** kullan — "bu satır ne yapıyor?" diye sorabildiğin kadar sor
4. Takıldığın yerde: hangi faz, hangi adım, tam olarak ne denedin, hangi hatayı aldın — bunları söyle, birlikte çözelim
5. Bir şey çalıştığında, **neden çalıştığını** anladığından emin ol — kopyala-yapıştır çalışması ama öğrenmemiş olursun
