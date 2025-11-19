# VeloAuth v2.0.0

[![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/plugin/veloauth)
[![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/e2RkPbc3ZR)

**Complete Velocity Authentication Plugin** with BCrypt, Virtual Threads and multi-database support.

## What's New in v2.0.0

- ✨ **Graceful Shutdown** - Proper executor termination with timeout handling
- ✨ **Premium Cache TTL** - Configurable 24-hour cache expiration with background refresh
- ✨ **Enhanced Initialization** - Improved handler lifecycle and null safety
- ✨ **Code Cleanup** - Removed deprecated APIs and dead code
- ✨ **Cache Optimization** - LRU eviction and stale-while-revalidate pattern

## Description

VeloAuth is an **authorization manager for Velocity proxy** that handles player transfers between Velocity, PicoLimbo and backend servers. The plugin supports all authorization operations on the proxy.

### Key Features:
- ✅ **Authorization Cache** - logged in players bypass login
- ✅ **Premium Cache TTL** - 24-hour expiration with background refresh
- ✅ **Transfer via Velocity** - control transfers between servers
- ✅ **Proxy Commands** - `/login`, `/register`, `/changepassword`
- ✅ **BCrypt hashing** - secure password storage (cost 10)
- ✅ **LimboAuth Compatible** - shared database compatibility
- ✅ **Premium and Cracked** - support for both player types
- ✅ **Virtual Threads** - efficient I/O (Java 21+)
- ✅ **Graceful Shutdown** - proper cleanup with timeout handling
- ✅ **Multi-database** - PostgreSQL, MySQL, H2, SQLite

## Requirements

- **Java 21+** (Virtual Threads)
- **Velocity API 3.4.0-SNAPSHOT+**
- **Database**: PostgreSQL 12+, MySQL 8.0+, H2, or SQLite
- **PicoLimbo** or other lobby server

## Installation

### 1. Download

Download from releases

### 2. Install on Velocity
1. Copy `VeloAuth-1.0.0.jar` to `plugins/`
2. Start Velocity - `config.yml` will be created
3. Configure database in `plugins/VeloAuth/config.yml`
4. Restart Velocity

### 3. PicoLimbo Configuration
Add PicoLimbo to `velocity.toml`:
```toml
[servers]
lobby = "127.0.0.1:25566"  # PicoLimbo
survival = "127.0.0.1:25565"  # Backend server
```
Set `try = ["lobby"]` in `velocity.toml`.

## Configuration

### config.yml
```yaml
# VeloAuth Configuration
database:
  storage-type: MYSQL  # MYSQL, POSTGRESQL, H2, SQLITE
  hostname: localhost
  port: 3306
  database: veloauth
  user: veloauth
  password: password
  connection-pool-size: 20
  max-lifetime-millis: 1800000

cache:
  ttl-minutes: 60
  max-size: 10000
  cleanup-interval-minutes: 5
  premium-ttl-hours: 24  # Premium status cache expiration (default: 24 hours)
  premium-refresh-threshold: 0.8  # Background refresh at 80% TTL (default: 0.8)

picolimbo:
  server-name: lobby
  timeout-seconds: 300

security:
  bcrypt-cost: 10
  bruteforce-max-attempts: 5
  bruteforce-timeout-minutes: 5
  ip-limit-registrations: 3
  min-password-length: 4
  max-password-length: 72

premium:
  check-enabled: true
  online-mode-need-auth: false
  resolver:
    mojang-enabled: true
    ashcon-enabled: true
    wpme-enabled: false
    request-timeout-ms: 400
```

## Usage

### Player Commands

| Command | Description |
|---------|-------------|
| `/register <password> <confirm>` | Register new account |
| `/login <password>` | Login to account |
| `/changepassword <old> <new>` | Change password |

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/unregister <nickname>` | `veloauth.admin` | Remove player account |
| `/vauth reload` | `veloauth.admin` | Reload configuration |
| `/vauth cache-reset [player]` | `veloauth.admin` | Clear cache |
| `/vauth stats` | `veloauth.admin` | Show statistics |

## Authorization Algorithm

### 1. Player joins Velocity
```
ConnectionEvent → VeloAuth checks cache
├─ Cache HIT → Verification → Forward backend
└─ Cache MISS → Transfer to PicoLimbo
```

### 2. Player on PicoLimbo
```
Player types: /login password or /register password password
↓
VELOCITY INTERCEPTS COMMAND
↓
1. SELECT HASH WHERE LOWERCASENICKNAME = LOWER(nickname)
2. BCrypt.verify(password, HASH)
├─ MATCH → UPDATE LOGINDATE + Cache + Forward backend
└─ NO MATCH → Brute force counter (max 5 attempts, timeout 5 min)
```

### 3. Player on Backend
```
ConnectionEvent → Cache HIT → Direct Backend
```

## Technical Details

### Performance
- **Cache HIT:** 0 DB queries, ~20ms
- **Cache MISS:** 1 DB query, ~100ms
- **/login:** 1 SELECT + 1 UPDATE, ~150ms (BCrypt)
- **/register:** 1 SELECT + 1 INSERT, ~200ms (BCrypt)

### Thread Safety
- **ConcurrentHashMap** for cache
- **ReentrantLock** for critical operations
- **Virtual Threads** for I/O operations

### Security
- **BCrypt cost 10** with salt (at.favre.lib 0.10.2)
- **Brute Force Protection** - 5 attempts / 5 minutes timeout
- **SQL Injection Prevention** - ORMLite prepared statements
- **Rate Limiting** - Velocity command rate limiting
- **IP Registration Limit** - Max 3 accounts per IP

## Premium Status Caching

VeloAuth implements intelligent premium status caching to minimize external API calls:

- **TTL-based expiration**: Premium status cached for 24 hours (configurable)
- **Background refresh**: Stale entries (>80% TTL) trigger async refresh while serving cached value
- **LRU eviction**: Maximum 10,000 entries with least-recently-used eviction
- **Stale-while-revalidate**: Zero-latency updates using stale cache during background refresh

### Configuration
```yaml
cache:
  premium-ttl-hours: 24  # How long to cache premium status
  premium-refresh-threshold: 0.8  # Trigger background refresh at 80% TTL
```

## Graceful Shutdown

VeloAuth ensures clean shutdown without rejected tasks:

1. **Shutdown signal received** - New tasks are rejected with user-friendly messages
2. **Graceful wait** - 10-second timeout for pending tasks to complete
3. **Forced shutdown** - Tasks still running after timeout are cancelled
4. **Logging** - Dropped task count logged for diagnostics

This prevents `RejectedExecutionException` errors during server shutdown.

## Compatibility

VeloAuth is **100% compatible** with LimboAuth database - ignores `TOTPTOKEN` and `ISSUEDTIME` fields.

### Migration from LimboAuth
1. Stop LimboAuth
2. Install VeloAuth
3. Configure the same database
4. Start Velocity - VeloAuth will automatically detect existing accounts

### Upgrading from v1.x to v2.0
See [MIGRATION.md](MIGRATION.md) for detailed upgrade instructions and breaking changes.

## Development

### Project Structure
```
src/main/java/net/rafalohaki/veloauth/
├── VeloAuth.java              # Main plugin class
├── cache/AuthCache.java       # Thread-safe authorization cache
├── command/CommandHandler.java # Command handling
├── config/Settings.java       # YAML configuration
├── connection/ConnectionManager.java # Player transfers
├── database/DatabaseManager.java # ORMLite + connection pooling
├── listener/AuthListener.java # Velocity event handling
└── model/                     # Player models (ORMLite)
```

## License

MIT License - see [LICENSE](LICENSE) for details.

## Troubleshooting

### RejectedExecutionException during shutdown
**Fixed in v2.0.0** - The plugin now implements graceful shutdown with proper timeout handling.

### NullPointerException in AuthListener
**Fixed in v2.0.0** - Handlers are now initialized before event registration with null safety checks.

### Premium status not updating
Check cache TTL settings:
```yaml
cache:
  premium-ttl-hours: 24  # Reduce if you need faster updates
  premium-refresh-threshold: 0.8  # Lower for more frequent background refreshes
```

### Slow login times
- **Cache hit**: ~20ms (no DB query)
- **Cache miss**: ~100ms (1 DB query)
- **Premium check**: ~50-200ms (external API call, cached for 24h)

If experiencing slow logins, check:
1. Database connection pool size (`connection-pool-size: 20`)
2. Network latency to database
3. Premium resolver timeout (`request-timeout-ms: 400`)

### High memory usage
Adjust cache limits:
```yaml
cache:
  max-size: 10000  # Reduce if needed
  premium-ttl-hours: 12  # Shorter TTL = less memory
```

### Database connection issues
1. Verify database credentials in `config.yml`
2. Check database server is running and accessible
3. Verify connection pool settings:
```yaml
database:
  connection-pool-size: 20
  max-lifetime-millis: 1800000  # 30 minutes
```

## Support

- **Discord:** [\[Server link\]](https://discord.gg/e2RkPbc3ZR)
- **Issues:** [GitHub Issues](https://github.com/rafalohaki/veloauth/issues)

---

**VeloAuth v2.0.0** - Complete Velocity Authentication Plugin  
Author: rafalohaki | Java 21 + Virtual Threads + BCrypt + Multi-DB