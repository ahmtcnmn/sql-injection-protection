**Amaç:** Ubuntu sunucularda çalışan Agent'lardan güvenlik olaylarını (SQL Injection, sistem logları) toplayan, bunları merkezi bir Manager'da yöneten ve uzaktan komut/politika gönderebilen bir sistem. 


## Genel Mimari

```
┌──────────────────────────┐                      ┌──────────────────────────┐
│   AGENT (Ubuntu VDS)     │                      │  MANAGER                 │
│   [Spring Boot / Java]   │   HTTPS / REST       │  [Spring Boot / Java]    │
│                          │ ───────────────────► │  - REST Controller'lar   │
│  - Nginx/Apache log      │                      │  - JPA (MySQL)           │
│    takibi                │ ◄─────────────────── │  - Spring Security       │
│  - MySQL/PostgreSQL log  │   (heartbeat +       │  - Komut kuyruğu         │
│  - journalctl/syslog     │    komut sorgulama)  │  - Web Dashboard         │
│  - SQLi regex tespiti    │                      │                          │
│  - Offline olay kuyruğu  │                      │                          │
└──────────────────────────┘                      └──────────────────────────┘
```

**İletişim modeli:** Agent → Manager yönünde polling (Agent periyodik bağlanır). Manager'ın Agent'a doğrudan bağlanmasına gerek yoktur; bu, Agent tarafında inbound port açma zorunluluğunu kaldırır.