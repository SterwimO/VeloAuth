# VeloAuth v2.0.0 Migration Guide

This guide helps you upgrade from VeloAuth v1.x to v2.0.0.

## Overview

VeloAuth v2.0.0 includes critical bug fixes, performance improvements, and code cleanup. Most changes are internal and backward compatible, but there are some breaking changes for developers extending the plugin.

## Breaking Changes

### 1. DatabaseConfig API Changes

**Removed:** Deprecated 11-parameter constructor `forRemoteWithHikari()`

**Before (v1.x):**
```java
DatabaseConfig config = DatabaseConfig.forRemoteWithHikari(
    "MYSQL", "localhost", 3306, "veloauth",
    "root", "password", 20, 1800000,
    "?useSSL=false", null, false
);
```

**After (v2.0.0):**
```java
DatabaseConfig config = DatabaseConfig.forRemoteWithHikari(
    HikariConfigParams.builder()
        .storageType("MYSQL")
        .hostname("localhost")
        .port(3306)
        .database("veloauth")
        .user("root")
        .password("password")
        .connectionPoolSize(20)
        .maxLifetime(1800000)
        .connectionParameters("?useSSL=false")
        .debugEnabled(false)
        .build()
);
```

**Impact:** Only affects custom code that directly instantiates DatabaseConfig. Standard plugin configuration via `config.yml` is unaffected.

### 2. AuthListener Constructor Changes

**Changed:** Constructor now requires PreLoginHandler and PostLoginHandler parameters

**Before (v1.x):**
```java
AuthListener listener = new AuthListener(
    plugin, connectionManager, authCache, settings,
    databaseManager, messages
);
// Handlers set later via updateDependencies()
```

**After (v2.0.0):**
```java
PreLoginHandler preLoginHandler = new PreLoginHandler(...);
PostLoginHandler postLoginHandler = new PostLoginHandler(...);

AuthListener listener = new AuthListener(
    plugin, connectionManager, authCache, settings,
    preLoginHandler, postLoginHandler,  // NEW: Required parameters
    databaseManager, messages
);
```

**Impact:** Only affects custom code that instantiates AuthListener. The `updateDependencies()` method has been removed.

## New Features

### 1. Premium Cache TTL

Premium status is now cached with configurable expiration:

**Add to config.yml:**
```yaml
cache:
  premium-ttl-hours: 24  # Default: 24 hours
  premium-refresh-threshold: 0.8  # Default: 0.8 (80% TTL)
```

**Benefits:**
- Reduces external API calls to Mojang/Ashcon
- Automatic background refresh for stale entries
- LRU eviction prevents unbounded memory growth

**Migration:** These settings are optional. If not specified, defaults are used.

### 2. Graceful Shutdown

The plugin now properly shuts down the Virtual Thread executor:

**What Changed:**
- New tasks are rejected during shutdown with user-friendly messages
- 10-second graceful termination period for pending tasks
- Forced shutdown with logging if timeout expires

**Benefits:**
- No more `RejectedExecutionException` during server shutdown
- Clean termination of all async operations
- Diagnostic logging for troubleshooting

**Migration:** No configuration changes needed. This is automatic.

### 3. Enhanced Initialization Safety

Handler initialization is now guaranteed before event processing:

**What Changed:**
- Handlers created before AuthListener registration
- Null safety checks in constructors
- Defense-in-depth null checks in event handlers

**Benefits:**
- No more `NullPointerException` during plugin startup
- Safer concurrent initialization
- Better error messages if initialization fails

**Migration:** No configuration changes needed. This is automatic.

## Upgrade Instructions

### For Server Administrators

1. **Backup your data:**
   ```bash
   # Backup database
   mysqldump -u root -p veloauth > veloauth_backup.sql
   
   # Backup config
   cp plugins/VeloAuth/config.yml plugins/VeloAuth/config.yml.backup
   ```

2. **Stop Velocity server:**
   ```bash
   # Stop your Velocity proxy
   ```

3. **Replace plugin JAR:**
   ```bash
   # Remove old version
   rm plugins/VeloAuth-1.*.jar
   
   # Add new version
   cp VeloAuth-2.0.0.jar plugins/
   ```

4. **Update configuration (optional):**
   
   Add new cache settings to `config.yml`:
   ```yaml
   cache:
     ttl-minutes: 60
     max-size: 10000
     cleanup-interval-minutes: 5
     premium-ttl-hours: 24  # NEW
     premium-refresh-threshold: 0.8  # NEW
   ```

5. **Start Velocity server:**
   ```bash
   # Start your Velocity proxy
   ```

6. **Verify operation:**
   - Check logs for successful initialization
   - Test player login/register
   - Verify premium detection works
   - Test graceful shutdown

### For Plugin Developers

If you have custom code that extends VeloAuth:

1. **Update DatabaseConfig usage:**
   - Replace deprecated constructor calls with builder pattern
   - See "Breaking Changes" section above

2. **Update AuthListener instantiation:**
   - Create handlers before AuthListener
   - Pass handlers to constructor
   - Remove `updateDependencies()` calls

3. **Review VirtualThreadExecutorProvider usage:**
   - Check for `RejectedExecutionException` handling
   - Use `isShutdown()` to check executor state before submitting tasks

4. **Test thoroughly:**
   - Test plugin initialization
   - Test player login flow
   - Test graceful shutdown
   - Test premium detection

## Configuration Reference

### New Configuration Options

```yaml
cache:
  # Premium status cache expiration (hours)
  # Default: 24
  # Range: 1-168 (1 hour to 1 week)
  premium-ttl-hours: 24
  
  # Background refresh threshold (0.0-1.0)
  # Default: 0.8 (refresh at 80% TTL)
  # Range: 0.5-0.95
  premium-refresh-threshold: 0.8
```

### Existing Configuration (Unchanged)

All existing configuration options remain unchanged and backward compatible:

```yaml
database:
  storage-type: MYSQL
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

## Rollback Instructions

If you encounter issues with v2.0.0:

1. **Stop Velocity server**

2. **Restore old plugin:**
   ```bash
   rm plugins/VeloAuth-2.0.0.jar
   cp VeloAuth-1.*.jar plugins/
   ```

3. **Restore configuration:**
   ```bash
   cp plugins/VeloAuth/config.yml.backup plugins/VeloAuth/config.yml
   ```

4. **Restore database (if needed):**
   ```bash
   mysql -u root -p veloauth < veloauth_backup.sql
   ```

5. **Start Velocity server**

**Note:** Database schema is unchanged between v1.x and v2.0.0, so rollback is safe.

## Troubleshooting

### Issue: Plugin fails to start

**Symptoms:**
- Error in logs during initialization
- Players cannot connect

**Solution:**
1. Check logs for specific error message
2. Verify database connection settings
3. Ensure Java 21+ is installed
4. Check file permissions on plugin directory

### Issue: Premium detection not working

**Symptoms:**
- All players treated as cracked
- Premium players forced to /register

**Solution:**
1. Check `premium.check-enabled: true` in config
2. Verify external API connectivity (Mojang/Ashcon)
3. Check premium cache settings:
   ```yaml
   cache:
     premium-ttl-hours: 24
   ```
4. Clear premium cache: `/vauth cache-reset`

### Issue: High memory usage

**Symptoms:**
- Memory usage increases over time
- OutOfMemoryError in logs

**Solution:**
1. Reduce cache size:
   ```yaml
   cache:
     max-size: 5000  # Reduce from 10000
     premium-ttl-hours: 12  # Reduce from 24
   ```
2. Monitor with `/vauth stats`
3. Restart server to clear caches

### Issue: Slow shutdown

**Symptoms:**
- Server takes >10 seconds to stop
- "Executor did not terminate" warnings

**Solution:**
1. Check for long-running tasks in logs
2. Verify no infinite loops in custom code
3. Increase shutdown timeout (requires code change)

## Performance Tuning

### Optimize for High Traffic

```yaml
database:
  connection-pool-size: 50  # Increase from 20
  max-lifetime-millis: 900000  # Reduce from 1800000

cache:
  max-size: 20000  # Increase from 10000
  premium-ttl-hours: 48  # Increase from 24
```

### Optimize for Low Memory

```yaml
cache:
  max-size: 5000  # Reduce from 10000
  premium-ttl-hours: 12  # Reduce from 24
  cleanup-interval-minutes: 2  # Reduce from 5

database:
  connection-pool-size: 10  # Reduce from 20
```

## Support

If you encounter issues during migration:

1. **Check logs:** `logs/latest.log` for error messages
2. **Discord:** [Join our Discord](https://discord.gg/e2RkPbc3ZR)
3. **GitHub Issues:** [Report bugs](https://github.com/rafalohaki/veloauth/issues)

## Changelog Summary

### Added
- Premium cache TTL with configurable expiration
- Background refresh for stale premium cache entries
- LRU eviction policy for premium cache
- Graceful shutdown with timeout handling
- Enhanced initialization safety with null checks
- Cache invalidation coordination between DatabaseManager and AuthCache

### Changed
- DatabaseConfig now requires HikariConfigParams builder pattern
- AuthListener constructor requires PreLoginHandler and PostLoginHandler
- Improved JavaDoc documentation across all modified classes

### Removed
- Deprecated DatabaseConfig 11-parameter constructor
- AuthListener.updateDependencies() method
- Dead code and unused methods across codebase

### Fixed
- RejectedExecutionException during server shutdown
- NullPointerException in AuthListener during initialization
- Premium status cache never expiring
- Race conditions during plugin initialization

---

**VeloAuth v2.0.0** - Migration Guide  
Last Updated: 2024-11-19
