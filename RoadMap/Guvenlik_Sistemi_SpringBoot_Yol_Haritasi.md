# Güvenlik İzleme Sistemi — Spring Boot Yol Haritası (Manager + Agent)

**Amaç:** Ubuntu sunucularda çalışan Agent'lardan güvenlik olaylarını (SQL Injection, sistem logları) toplayan, bunları merkezi bir Manager'da yöneten ve uzaktan komut/politika gönderebilen bir sistem. **Hem Manager hem Agent Spring Boot (Java) ile geliştirilecek.**

**Bu döküman nasıl kullanılır:** Her faz için *ne yapmanız gerektiği*, *hangi kavramları öğrenmeniz gerektiği* ve *ipuçları* var — hazır kod yok. Java bilginiz olduğu için her iki tarafta da hızlı ilerleyebilirsiniz.

---

## Neden İki Taraf da Spring Boot?

- **Tek dil, tek ekosistem:** İki farklı dil yönetmek yerine her şey Java — build, test, deployment araçları tek tip.
- **Kod paylaşımı:** Ortak DTO'lar, ortak modeller iki taraf arasında paylaşılabilir (örn. ayrı bir `shared` modülü).
- **Mevcut bilginiz:** Java bildiğiniz için öğrenme yükü minimum.

### Dikkat Edilecek Ödünleşim (Agent Kaynak Tüketimi)

Agent müşteri sunucularında çalışacağı için hafiflik önemli. Standart bir Spring Boot uygulaması JVM nedeniyle ~150-300 MB RAM tüketir. Bunu azaltmak için:
- **GraalVM Native Image** ile derleme (RAM ~30-50 MB'a iner, anında başlar) — Agent için ciddi olarak değerlendirin (Faz 5'te ele alınacak)
- Agent'ı minimal bağımlılıkla tutun (gereksiz Spring starter'ları eklemeyin)

---

## Genel Mimari

```
┌─────────────────────────┐                      ┌──────────────────────────┐
│   AGENT (Ubuntu VDS)     │                      │  MANAGER                 │
│   [Spring Boot / Java]   │   HTTPS / REST       │  [Spring Boot / Java]    │
│                          │ ───────────────────► │  - REST Controller'lar   │
│  - Nginx/Apache log      │                      │  - JPA (MySQL)           │
│    takibi                │ ◄─────────────────── │  - Spring Security       │
│  - MySQL/PostgreSQL log  │   (heartbeat +       │  - Komut kuyruğu         │
│  - journalctl/syslog     │    komut sorgulama)  │  - Web Dashboard         │
│  - SQLi regex tespiti    │                      │                          │
│  - Offline olay kuyruğu  │                      │                          │
└─────────────────────────┘                      └──────────────────────────┘
```

**İletişim modeli:** Agent → Manager yönünde polling (Agent periyodik bağlanır). Manager'ın Agent'a doğrudan bağlanmasına gerek yoktur; bu, Agent tarafında inbound port açma zorunluluğunu kaldırır.

---

## Faz 0: Ortam ve Temel Hazırlık

### Ortak kurulum (her iki proje için)

| İhtiyaç | Araç | Notlar |
|---|---|---|
| JDK | Java 21+ (LTS) | Virtual threads desteği için 21 önerilir |
| Build aracı | Maven veya Gradle | İkisinden birini seçin, tutarlı kalın |
| IDE | IntelliJ IDEA | Spring Boot desteği en iyi burada |
| Proje başlatma | Spring Initializr (start.spring.io) | İki ayrı proje: `manager` ve `agent` |

**Manager projesi için Spring bağımlılıkları:**
- Spring Web, Spring Data JPA, MySQL Driver, Spring Boot DevTools, Spring Security, Validation, Lombok

**Agent projesi için Spring bağımlılıkları (minimal tutun):**
- Spring Web (Manager'a HTTP isteği atmak için — `RestClient`/`WebClient`)
- Spring Boot DevTools
- Lombok
- (Veritabanı/Security gibi ağır bağımlılıkları Agent'a EKLEMEYİN — gerek yok, hafif kalsın)

### Araştırma Anahtar Kelimeleri
- `spring initializr getting started`
- `spring boot project structure explained`
- `spring boot restclient vs webclient`

---

## Faz 1: Geliştirme Ortamı ve Ağ ✅ (Tamamlandı)

- ✅ VDS'e SSH key ile şifresiz giriş
- ✅ Ters SSH tüneli (Agent VDS'te, Manager MacBook'ta)
- ✅ Git deposu + `.gitignore`

**Spring Boot'a özel `.gitignore` eklemeleri:** `target/`, `*.class`, `.idea/`, `*.iml`, `build/` (Gradle ise). Spring Initializr genelde hazır `.gitignore` ile gelir.

---

## Faz 2: Manager İskeleti (Spring Boot + JPA + MySQL)

### Yapılacaklar
1. Spring Initializr'dan `manager` projesini oluşturup IntelliJ'de açın
2. `application.yml` içinde MySQL bağlantısını yapılandırın — **bağlantı bilgilerini koda gömmeyin**, ortam değişkeni/profil kullanın
3. `GET /api/health` → `{"status":"ok"}` endpoint'i yazın
4. İki JPA Entity oluşturun:
   - `Agent` (id, hostname, apiKey, lastSeen)
   - `Event` (id, agent ilişkisi, eventType, timestamp, severity, rawData)
5. Her entity için bir `Repository` interface'i (`JpaRepository`)
6. Uygulamayı çalıştırıp health endpoint'ini test edin, tabloların oluştuğunu doğrulayın

### Düşünmeniz Gerekenler
- `Agent`–`Event` bire-çok ilişkisi: `@OneToMany`, `@ManyToOne`, `@JoinColumn`
- `lastSeen` için `Instant` (UTC tutarlılığı için tercih)
- `is_online` mantığı (son 60 sn'de haber verdi mi) — entity metodu mu, servis mi?
- `severity` 1-5 kısıtı için `@Min`, `@Max`

### Araştırma Anahtar Kelimeleri
- `spring boot jpa entity onetomany manytoone`
- `spring boot application yml mysql datasource`
- `spring jpa hibernate ddl-auto explained`
- `spring boot externalized configuration environment variables`

### İpucu
`ddl-auto=update` geliştirme için pratik ama üretimde riskli — ileride Flyway/Liquibase (migration araçları) araştırın.

---

## Faz 3: Agent Kayıt ve Kimlik Doğrulama

### Yapılacaklar
1. `POST /api/agents/register` — yeni agent kaydeder, güvenli API key üretip döner
2. API key için Java `SecureRandom`
3. Agent isteklerini doğrulama: gelen API key header'ını DB kaydıyla eşleştir
4. Doğrulamayı ortak bir yapıya çıkarın (`Filter`, `HandlerInterceptor` veya Spring Security filter chain)

### Düşünmeniz Gerekenler
- API key'i DB'de düz metin mi, hash'leyerek mi saklamalı? (`BCrypt` tercih edilir)
- Basit `HandlerInterceptor` mı yoksa Spring Security mi? (Başta interceptor basit, ölçekte Security)

### Araştırma Anahtar Kelimeleri
- `java securerandom generate token`
- `spring boot custom header authentication interceptor`
- `spring security api key filter`
- `spring boot bcrypt password encoder`

---

## Faz 4: Heartbeat ve Event Toplama

### Yapılacaklar
1. `POST /api/heartbeat` — doğrulanmış agent'ın `lastSeen`'ini günceller
2. `POST /api/events` — agent'ın gönderdiği olayı kaydeder
3. Girdi doğrulama (`@Valid` + Bean Validation)
4. Agent (Spring Boot) tarafını yazın: periyodik heartbeat + event gönderen client

### Agent Tarafı (Spring Boot) — Nasıl?
- Periyodik görev için `@Scheduled` anotasyonu (Spring'in zamanlanmış görev mekanizması — cron veya sabit aralık)
- Manager'a HTTP isteği atmak için `RestClient` (Spring 6.1+) veya `WebClient`
- Manager adresi/API key'i `application.yml`'den okuyun (koda gömmeyin)

### Düşünmeniz Gerekenler
- `timestamp`'i Manager mı (`Instant.now()`) yoksa Agent mı belirlemeli? (Agent saati yanlışsa sunucu tarafı daha güvenilir)
- DTO kullanın — entity'yi doğrudan API'ye açmak yerine ayrı request/response nesneleri

### Araştırma Anahtar Kelimeleri
- `spring boot @scheduled fixed rate cron`
- `spring boot restclient post json example`
- `spring boot request body dto validation @Valid`

---

## Faz 5: Agent — Log Takibi ve SQL Injection Tespiti

### Yapılacaklar
1. Log dosyasını sürekli izleyen bir bileşen (Java'da "tail -f" mantığı)
2. Regex tabanlı SQLi tespiti (Java `Pattern`/`Matcher`)
3. Tespit edilen olayı Manager'ın `POST /api/events`'ine gönder

### Java'da "tail -f" Nasıl?
- `java.nio` ile dosyayı sonundan itibaren okuyup yeni satırları yakalama
- Ya da bir kütüphane: Apache Commons IO'nun `Tailer` sınıfı (tam bu iş için var, tekerleği yeniden icat etmeyin)

### Regex Tespiti
- Java'da `Pattern.compile(regex, Pattern.CASE_INSENSITIVE)` + `matcher.find()`
- SQLi paternleri: tırnak+mantıksal operatör, `UNION SELECT`, yorum belirteçleri (`--`, `#`), tehlikeli anahtar kelimeler
- False positive dengesine dikkat (çok gevşek kural = meşru istekleri de yakalar)

### GraalVM Native Image (Agent Hafifliği İçin)
Agent'ı burada GraalVM native image olarak derlemeyi değerlendirin — RAM tüketimini ciddi düşürür. Spring Boot 3.x native image'ı yerleşik destekler ama bazı kütüphaneler (reflection kullananlar) ek yapılandırma gerektirebilir.

### Araştırma Anahtar Kelimeleri
- `apache commons io tailer example`
- `java pattern matcher case insensitive`
- `OWASP SQL injection patterns`
- `spring boot graalvm native image tutorial`

---

## Faz 6: Agent — Veritabanı ve Sistem Logları

### Yapılacaklar
1. MySQL/PostgreSQL error log takibi (başarısız login denemeleri)
2. `journalctl -f` çıktısını okuma — Java'da `ProcessBuilder` ile
3. Her log kaynağı için ayrı bir iş parçacığı (Java'da `@Async` veya `ExecutorService`, ya da Java 21 virtual threads)

### Java'da Sistem Komutu Çalıştırma
- `ProcessBuilder` ile `journalctl -f` başlatıp, process'in `InputStream`'ini satır satır okuma (Python'daki `subprocess.Popen`'in Java karşılığı)
- Uzun süre çalışan bu okuma işlemini ayrı bir thread'de yürütün ki ana uygulama bloke olmasın

### Araştırma Anahtar Kelimeleri
- `java processbuilder read output stream continuously`
- `spring boot @async executor`
- `java 21 virtual threads`

---

## Faz 7: Dashboard (Manager)

### Yapılacaklar
1. Event'leri ve agent durumlarını gösteren web arayüzü
2. İki yaklaşım:
   - **Thymeleaf** (Spring'in şablon motoru) ile sunucu tarafı HTML
   - **Ayrı frontend** (React/Vue) + REST API
3. Gösterilecekler: agent'lar (online/offline), son event'ler, filtreleme

### Düşünmeniz Gerekenler
- Ürün olacaksa (müşteri-facing) ayrı modern frontend daha esnek
- Sadece iç panel ise Thymeleaf hızlı çözüm
- Canlı log akışı için Server-Sent Events (Spring'de `SseEmitter`)

### Araştırma Anahtar Kelimeleri
- `spring boot thymeleaf tutorial`
- `spring boot sseemitter server sent events`
- `spring boot rest api react cors`

---

## Faz 8: Uzaktan Komut / Politika Gönderme

### Yapılacaklar
1. `Command` entity'si (id, agent ilişkisi, komut, durum: pending/delivered/done, zaman)
2. Manager'dan agent'a komut sıraya koyma
3. Agent, heartbeat sırasında `GET /api/commands` ile bekleyen komutu sorgular
4. Agent komutu uygulayıp sonucu geri bildirir

### Düşünmeniz Gerekenler
- Komut kuyruğu: DB tablosu (başlangıç) mı, mesaj kuyruğu (RabbitMQ/Redis, ölçek) mü?
- İleride "canlı terminal" için polling yetmez → WebSocket (Faz 11)

### Araştırma Anahtar Kelimeleri
- `spring boot command queue database pattern`
- `spring data jpa custom query status filter`

---

## Faz 9: Güvenlik Sıkılaştırma (Spring Security)

### Yapılacaklar
1. **API key doğrulama:** Tüm agent endpoint'leri geçerli key olmadan çalışmamalı
2. **HTTPS:** Manager-Agent iletişimini şifreleyin
3. **Rate limiting:** Bucket4j ile aşırı istek koruması
4. **Panel kullanıcı yönetimi:** Kullanıcı/rol/izin sistemi (Spring Security'nin güçlü olduğu alan)
5. **Agent offline kuyruğu:** Manager erişilemezse event'leri yerel olarak biriktirip sonra gönderme (Agent'ta gömülü H2 veya basit dosya kuyruğu)

### Araştırma Anahtar Kelimeleri
- `spring security jwt authentication tutorial`
- `spring security role based access control`
- `spring boot rate limiting bucket4j`
- `spring boot https ssl configuration`
- `resilience4j retry circuit breaker`

---

## Faz 10: Aktif Engelleme (IPS Benzeri Otomatik Blokaj)

### Yapılacaklar
1. Manager'da eşik mantığı: aynı IP'den kısa sürede N adet SQLi event'i → `block_ip` komutu üret (Faz 8 kuyruğunu kullanır)
2. Agent, komutu alıp `ufw`/`iptables` ile IP'yi geçici engeller (`ProcessBuilder` ile sistem komutu)
3. Süreli engelleme + otomatik kaldırma
4. Engelleme olayını Manager'a bildir

### ⚠️ Kritik Güvenlik Notları
- **Kendinizi/Manager'ı kilitlemeyin:** Whitelist (Manager IP'si, test IP'niz) her zaman kontrol edilmeli
- **Yanlış pozitif riski:** Başta sadece çok net saldırı paternlerini engelleyin
- **Her engelleme kararını loglayın**

### Araştırma Anahtar Kelimeleri
- `ufw command line block ip temporary`
- `java processbuilder run system command`
- `fail2ban architecture` (ilham için)

---

## Faz 11: Çoklu Müşteri / Üst-Katman Panel (İleri Seviye — Ticari Hedef)

Asıl ticari hedefiniz: birden fazla müşterinin sistemini tek panelden yönetmek. Temel sistem (Faz 1-10) oturduktan sonra.

### Yapılacaklar
1. **Çok kiracılı (multi-tenancy):** Her müşterinin verisi izole — hangi kullanıcı hangi müşterinin agent'larını görür?
2. **Müşteri Manager'larına bağlanma:** Müşterilerin kendi Manager'ları varsa, üst-katmanınız onların REST API'lerine bağlanıp veriyi toplar
3. **Dayanıklılık:** Bir müşterinin Manager'ı çökerse paneliniz etkilenmemeli (Resilience4j)
4. **Canlı terminal / toplu komut:** Birden fazla agent'a aynı anda komut, canlı çıktı → WebSocket

### Düşünmeniz Gerekenler
- Multi-tenancy: müşteri başına ayrı DB mi, tek DB'de tenant_id mi?
- Canlı terminal için WebSocket ölçeklendirme
- Müşteri Manager API'leri farklıysa bir "adapter" katmanı

### Araştırma Anahtar Kelimeleri
- `spring boot multi tenancy strategies`
- `spring websocket stomp tutorial`
- `resilience4j spring boot circuit breaker`
- `spring boot websocket broadcast multiple clients`

---

## Önerilen Proje Yapıları

### Manager
```
manager/
├── src/main/java/com/proje/manager/
│   ├── ManagerApplication.java
│   ├── controller/    (AgentController, EventController, CommandController)
│   ├── entity/        (Agent, Event, Command)
│   ├── repository/     (AgentRepository, EventRepository, CommandRepository)
│   ├── service/        (AgentService, EventService)
│   ├── dto/            (RegisterRequest, EventRequest)
│   ├── security/       (ApiKeyFilter)
│   └── config/
├── src/main/resources/application.yml
└── pom.xml
```

### Agent
```
agent/
├── src/main/java/com/proje/agent/
│   ├── AgentApplication.java
│   ├── watcher/        (LogTailer, JournalWatcher, DbLogWatcher)
│   ├── detector/       (SqliDetector)
│   ├── responder/      (IpBlocker)
│   ├── client/         (ManagerClient — Manager'a REST istekleri)
│   ├── scheduler/      (HeartbeatTask, CommandPollTask — @Scheduled)
│   └── config/
├── src/main/resources/application.yml
└── pom.xml
```

**İsteğe bağlı:** Ortak DTO'ları (`EventRequest`, `RegisterRequest` gibi iki tarafın da kullandığı sınıflar) `shared/` adında ayrı bir Maven/Gradle modülüne koyup her iki projeye bağımlılık olarak ekleyebilirsiniz — kod tekrarını önler.

---

## İlerleme Kontrol Listesi

- [x] Faz 1 — Ortam ve ağ (SSH key, ters tünel, git)
- [ ] Faz 0 — Spring Boot ortam kurulumu (JDK, IDE, iki Initializr projesi)
- [ ] Faz 2 — Manager iskeleti (Spring Boot + JPA + MySQL)
- [ ] Faz 3 — Agent kayıt + API key doğrulama
- [ ] Faz 4 — Heartbeat + event toplama (Agent client dahil)
- [ ] Faz 5 — Agent log takibi + SQLi tespiti
- [ ] Faz 6 — Agent DB/sistem logları
- [ ] Faz 7 — Dashboard
- [ ] Faz 8 — Uzaktan komut gönderme
- [ ] Faz 9 — Güvenlik sıkılaştırma (Spring Security)
- [ ] Faz 10 — Aktif IP engelleme
- [ ] Faz 11 — Çoklu müşteri / üst-katman panel (ticari hedef)

---

## Kapanış Notu

Her iki taraf da Spring Boot olduğu için tek ekosistemde, tutarlı araçlarla çalışacaksınız — ve Java bilginiz süreci hızlandıracak. İki uygulama arasındaki tek "sözleşme" REST API (JSON) olacak; bu sözleşmeyi net tutarsanız (hangi endpoint, hangi DTO alanları), iki taraf birbirinden bağımsız gelişebilir. Ortak DTO'lar için paylaşılan bir modül düşünün. Agent'ın müşteri sunucularında hafif kalması için GraalVM native image'ı erkenden değerlendirin. Her fazı bitirdiğinizde bir sonrakine geçmeden mevcut fazın gerçekten çalıştığından emin olun.
