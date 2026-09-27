# JPA Entity İlişkileri — Örneklerle

Projenizdeki senaryo: `Agent` bire-çok `Event`, `Agent` bire-çok `Command`.
Aşağıda dört ilişki tipi de bu senaryo üzerinden gösteriliyor.

---

## 1. `@OneToMany` / `@ManyToOne` — Bire-Çok (sizin asıl kullanacağınız)

Bir `Agent`'ın birden çok `Event`'i olur. İlişkinin "sahibi" (foreign key'i
tutan taraf) her zaman **çok** tarafıdır — yani `Event`.

```java
// "Bir" taraf — sahip olmayan taraf (mappedBy ile işaretlenir)
@Entity
@Table(name = "agent")
public class AgentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_name", nullable = false)
    private String hostName;

    // "events" -> Event entity'sindeki alan adı (aşağıda)
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventModel> events = new ArrayList<>();
}
```

```java
// "Çok" taraf — sahip taraf, foreign key burada oluşur
@Entity
@Table(name = "event")
public class EventModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    // Foreign key kolonu burada tanımlanır: event.agent_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private AgentModel agent;
}
```

**Kurallar:**
- `@JoinColumn` → foreign key kolonunu tanımlayan taraf **her zaman `@ManyToOne` tarafıdır**.
- `mappedBy` → "bir" tarafta yazılır, karşı taraftaki **alan adını** (kolon adını değil) gösterir.
- `@ManyToOne` her zaman `FetchType.LAZY` yapın — varsayılanı `EAGER`'dır ve N+1 sorgu problemine yol açar.
- `cascade = CascadeType.ALL` + `orphanRemoval = true` → Agent silinince event'leri de silinir. Bunu **sadece gerçekten "parent silinince child da silinsin" istiyorsanız** kullanın.

**İlişkiyi kurarken iki tarafı da senkron tutun** (yardımcı metot):

```java
// AgentModel içinde
public void addEvent(EventModel event) {
    events.add(event);
    event.setAgent(this);
}
```

---

## 2. `@ManyToOne` tek başına — Sadece bir yönü gerekiyorsa

`Agent`'tan `Command`'lara koleksiyon olarak erişmenize gerek yoksa (yani
"bu agent'ın komutları" listesini hiç sorgulamıyorsanız), karşı tarafta
`@OneToMany` tanımlamak zorunda değilsiniz — sadece `Command` tarafında
`@ManyToOne` yeterli:

```java
@Entity
@Table(name = "command")
public class CommandModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String command;
    private String status; // pending, delivered, done

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private AgentModel agent;
}
```

Bu durumda `Agent`'a ait komutları görmek isterseniz `CommandRepository`'de
sorgu yazarsınız (aşağıda), entity'yi şişirmezsiniz:

```java
public interface CommandRepository extends JpaRepository<CommandModel, Long> {
    List<CommandModel> findByAgentId(Long agentId);
}
```

**Tavsiye:** Gerçekten ihtiyacınız olmadıkça `@OneToMany` eklemeyin —
her eklenen koleksiyon, potansiyel bir N+1 sorgu kaynağıdır. Repository
sorgusu çoğu zaman yeterlidir.

---

## 3. `@OneToOne` — Birebir ilişki

Örnek: Her `Agent`'ın tek bir `AgentConfig`'i olsun.

```java
@Entity
@Table(name = "agent_config")
public class AgentConfigModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int heartbeatIntervalSeconds;

    // Foreign key + unique constraint burada oluşur: agent_config.agent_id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false, unique = true)
    private AgentModel agent;
}
```

Karşı taraftan erişmek isterseniz (`agent.getConfig()`):

```java
// AgentModel içinde
@OneToOne(mappedBy = "agent", cascade = CascadeType.ALL)
private AgentConfigModel config;
```

**Not:** `@OneToOne` çoğu zaman gereksizdir — `AgentModel`'e doğrudan alan
eklemek (`heartbeatIntervalSeconds`) daha basittir. Sadece config'i ayrı
yaşam döngüsünde tutmanız gerekiyorsa (örn. bazı agent'larda hiç olmaması,
ayrı tabloda versiyonlanması gibi) ayrı entity'ye değer.

---

## 4. `@ManyToMany` — Çoktan-çoğa

Örnek senaryo: Bir `Agent` birden çok `Tag`'e (etiket) sahip olabilir, bir
`Tag` birden çok `Agent`'a atanabilir.

```java
@Entity
@Table(name = "agent")
public class AgentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "agent_tag",                                   // ara tablo adı
        joinColumns = @JoinColumn(name = "agent_id"),          // bu entity'nin FK'i
        inverseJoinColumns = @JoinColumn(name = "tag_id")      // karşı entity'nin FK'i
    )
    private Set<TagModel> tags = new HashSet<>();
}
```

```java
@Entity
@Table(name = "tag")
public class TagModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<AgentModel> agents = new HashSet<>();
}
```

**Kurallar:**
- Ara tabloyu (`agent_tag`) `@JoinTable` tanımlayan taraf yönetir — buna
  **sahip (owning) taraf** denir. Karşı tarafta `mappedBy` kullanılır.
- Koleksiyon için `List` değil `Set` tercih edin (`@ManyToMany`'de
  duplicate/performans sorunlarını azaltır).
- `@ManyToMany`'de `cascade = CascadeType.ALL` **kullanmayın** — bir
  `Agent`'ı silmek isterken yanlışlıkla başka agent'ların da kullandığı
  `Tag`'i silebilirsiniz.

---

## 5. Sık Yapılan Hatalar

- **`@ManyToOne`/`@OneToMany`'yi `FetchType.EAGER` bırakmak** → her Agent
  çekildiğinde otomatik tüm Event'leri de çeker, performans sorunu yaratır.
  Her zaman `LAZY` yazın (varsayılan sadece `@OneToMany`/`@ManyToMany`'de
  zaten `LAZY`'dir; `@ManyToOne`/`@OneToOne`'da varsayılan `EAGER`'dır —
  bu ikisinde mutlaka elle `LAZY` belirtin).
- **`mappedBy`'ı yanlış tarafa yazmak** → `mappedBy`, foreign key'i
  **tutmayan** tarafa yazılır. Foreign key kolonunu (`@JoinColumn`) kim
  tanımlıyorsa, o taraf sahip taraftır ve `mappedBy` almaz.
- **İki tarafı senkronize etmemek** → `event.setAgent(agent)` yazıp
  `agent.getEvents().add(event)`'i unutmak, `equals`/`hashCode` ile
  ilgili tutarsızlıklara ve testlerde "neden koleksiyon boş görünüyor"
  sorununa yol açar. Yardımcı metotlarla (`addEvent`, `removeEvent`) her
  zaman iki tarafı birlikte güncelleyin.
- **`toString()`/`equals()`'da ilişkili entity'yi de yazdırmak** (Lombok
  `@Data` kullanıyorsanız dikkat!) → `Agent.toString()` → `Event.toString()`
  → tekrar `Agent.toString()` şeklinde sonsuz döngüye (StackOverflow)
  girebilir. Lombok'ta `@ToString.Exclude` / `@EqualsAndHashCode.Exclude`
  ile ilişki alanlarını hariç tutun:

```java
@Entity
@Data
public class EventModel {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private AgentModel agent;
}
```

---

## 6. Sizin Senaryonuz İçin Öneri

| İlişki | Tip | Sahip taraf |
|---|---|---|
| Agent — Event | `@OneToMany` / `@ManyToOne` | `Event` (agent_id kolonu) |
| Agent — Command | `@OneToMany` / `@ManyToOne` (ya da sadece `@ManyToOne` + repository sorgusu) | `Command` (agent_id kolonu) |

İkisi de aynı kalıp: "bir" tarafta koleksiyon + `mappedBy`, "çok" tarafta
`@ManyToOne` + `@JoinColumn`. `@ManyToMany`/`@OneToOne` şu an senaryonuzda
gerekmiyor, ileride "Tag" gibi bir kavram eklerseniz §4'teki kalıbı kullanın.
