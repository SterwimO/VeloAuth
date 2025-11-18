package net.rafalohaki.veloauth.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;
import java.util.UUID;

/**
 * Model danych gracza zarejestrowanego w systemie autoryzacji.
 * Zaktualizowany schema z TOTP support i proper NOT NULL constraints.
 * <p>
 * Tabela AUTH zawiera wszystkie dane potrzebne do autoryzacji gracza.
 */
@DatabaseTable(tableName = "AUTH")
public class RegisteredPlayer {

    /**
     * Oryginalny nickname gracza (może zawierać duże/małe litery).
     */
    @DatabaseField(columnName = "NICKNAME", canBeNull = false)
    private String nickname;

    /**
     * Klucz główny - nickname w lowercase dla case-insensitive wyszukiwania.
     */
    @DatabaseField(columnName = "LOWERCASENICKNAME", id = true, canBeNull = false)
    private String lowercaseNickname;

    /**
     * BCrypt hash hasła użytkownika (nigdy nie przechowuj plaintext!).
     * Może być null dla graczy premium (limboauth compatibility).
     */
    @DatabaseField(columnName = "HASH")
    private String hash;

    /**
     * IP adres z którego gracz się zarejestrował.
     */
    @DatabaseField(columnName = "IP")
    private String ip;

    /**
     * Timestamp rejestracji w milisekundach (System.currentTimeMillis()).
     */
    @DatabaseField(columnName = "REGDATE")
    private long regDate;

    /**
     * UUID gracza Minecraft (może być różne od premium UUID).
     */
    @DatabaseField(columnName = "UUID")
    private String uuid;


    /**
     * IP adres ostatniego logowania gracza (deprecated - will be removed).
     */
    @DatabaseField(columnName = "LOGINIP")
    private String loginIp;

    /**
     * Timestamp ostatniego logowania w milisekundach.
     */
    @DatabaseField(columnName = "LOGINDATE")
    private long loginDate;

    /**
     * Premium UUID gracza (limboauth compatibility).
     * Różni się od UUID - używane do weryfikacji statusu premium.
     */
    @DatabaseField(columnName = "PREMIUMUUID")
    private String premiumUuid;

    /**
     * TOTP token dla dwuetapowej autoryzacji (limboauth compatibility).
     */
    @DatabaseField(columnName = "TOTPTOKEN")
    private String totpToken;

    /**
     * Timestamp wydania tokenu/premium statusu (limboauth compatibility).
     */
    @DatabaseField(columnName = "ISSUEDTIME")
    private long issuedTime;

    /**
     * Konstruktor bezparametrowy wymagany przez ORMLite.
     */
    public RegisteredPlayer() {
        // ORMLite wymaga pustego konstruktora
    }

    /**
     * Tworzy nowego zarejestrowanego gracza.
     *
     * @param nickname Oryginalny nickname gracza
     * @param hash     BCrypt hash hasła (null dla graczy premium)
     * @param ip       IP adres rejestracji
     * @param uuid     UUID gracza Minecraft
     */
    public RegisteredPlayer(String nickname, String hash, String ip, String uuid) {
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("Nickname nie może być pusty");
        }
        // Hash może być null dla graczy premium (limboauth compatibility)

        this.nickname = nickname;
        this.lowercaseNickname = nickname.toLowerCase();
        this.hash = hash;
        this.ip = ip;
        this.uuid = uuid;
        this.loginIp = ip; // Początkowo IP logowania = IP rejestracji

        long currentTime = System.currentTimeMillis();
        this.regDate = currentTime;
        this.loginDate = currentTime;
    }

    // Gettery i settery z walidacją

    /**
     * Zwraca oryginalny nickname gracza.
     *
     * @return Nickname gracza
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Ustawia nickname gracza i automatycznie aktualizuje lowercase version.
     *
     * @param nickname Nowy nickname
     */
    public void setNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalArgumentException("Nickname nie może być pusty");
        }
        this.nickname = nickname;
        this.lowercaseNickname = nickname.toLowerCase();
    }

    /**
     * Zwraca lowercase nickname (klucz główny).
     *
     * @return Lowercase nickname
     */
    public String getLowercaseNickname() {
        return lowercaseNickname;
    }

    /**
     * Zwraca BCrypt hash hasła.
     *
     * @return Hash hasła
     */
    public String getHash() {
        return hash;
    }

    /**
     * Ustawia nowy hash hasła.
     *
     * @param hash Nowy BCrypt hash (null dla graczy premium)
     */
    public void setHash(String hash) {
        // Hash może być null dla graczy premium (limboauth compatibility)
        this.hash = hash;
    }

    /**
     * Zwraca IP adres rejestracji.
     *
     * @return IP rejestracji
     */
    public String getIp() {
        return ip;
    }

    /**
     * Ustawia IP adres rejestracji.
     *
     * @param ip IP rejestracji
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Zwraca IP adres ostatniego logowania.
     *
     * @return IP ostatniego logowania
     */
    public String getLoginIp() {
        return loginIp;
    }

    /**
     * Ustawia IP adres ostatniego logowania.
     *
     * @param loginIp IP ostatniego logowania
     */
    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    /**
     * Zwraca UUID gracza Minecraft.
     *
     * @return UUID gracza
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Ustawia UUID gracza Minecraft.
     *
     * @param uuid UUID gracza
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    /**
     * Zwraca timestamp rejestracji.
     *
     * @return Czas rejestracji w milisekundach
     */
    public long getRegDate() {
        return regDate;
    }

    /**
     * Ustawia timestamp rejestracji.
     *
     * @param regDate Czas rejestracji w milisekundach
     */
    public void setRegDate(long regDate) {
        this.regDate = regDate;
    }

    /**
     * Zwraca timestamp ostatniego logowania.
     *
     * @return Czas ostatniego logowania w milisekundach
     */
    public long getLoginDate() {
        return loginDate;
    }

    /**
     * Ustawia timestamp ostatniego logowania.
     *
     * @param loginDate Czas ostatniego logowania w milisekundach
     */
    public void setLoginDate(long loginDate) {
        this.loginDate = loginDate;
    }

    /**
     * Zwraca premium UUID gracza.
     *
     * @return Premium UUID gracza
     */
    public String getPremiumUuid() {
        return premiumUuid;
    }

    /**
     * Ustawia premium UUID gracza.
     *
     * @param premiumUuid Premium UUID gracza
     */
    public void setPremiumUuid(String premiumUuid) {
        this.premiumUuid = premiumUuid;
    }

    /**
     * Zwraca TOTP token gracza.
     *
     * @return TOTP token
     */
    public String getTotpToken() {
        return totpToken;
    }

    /**
     * Ustawia TOTP token gracza.
     *
     * @param totpToken TOTP token
     */
    public void setTotpToken(String totpToken) {
        this.totpToken = totpToken;
    }

    /**
     * Zwraca timestamp wydania tokenu/premium statusu.
     *
     * @return Timestamp wydania w milisekundach
     */
    public long getIssuedTime() {
        return issuedTime;
    }

    /**
     * Ustawia timestamp wydania tokenu/premium statusu.
     *
     * @param issuedTime Timestamp wydania w milisekundach
     */
    public void setIssuedTime(long issuedTime) {
        this.issuedTime = issuedTime;
    }

    /**
     * Sprawdza czy gracz ma konto premium.
     *
     * @return false - premium status should be checked through DatabaseManager
     * @deprecated Use DatabaseManager.isPremium(nickname) instead - this method doesn't have access to PREMIUM_UUIDS table
     * NOTE: This method is still used throughout the codebase during migration.
     * Do not remove until all callers are updated to use DatabaseManager.isPremium().
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public boolean isPremium() {
        // Premium status is now handled by PREMIUM_UUIDS table
        // Use DatabaseManager.isPremium(nickname) for proper lookup
        return false;
    }

    /**
     * Aktualizuje dane logowania - IP i timestamp.
     *
     * @param loginIp IP adres logowania
     */
    public void updateLoginData(String loginIp) {
        this.loginIp = loginIp;
        this.loginDate = System.currentTimeMillis();
    }

    /**
     * Zwraca UUID gracza jako obiekt UUID.
     *
     * @return UUID object lub null jeśli UUID jest nieprawidłowy
     */
    public UUID getUuidAsUUID() {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RegisteredPlayer that = (RegisteredPlayer) obj;
        return Objects.equals(lowercaseNickname, that.lowercaseNickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lowercaseNickname);
    }

    @Override
    public String toString() {
        return "RegisteredPlayer{" +
                "nickname='" + nickname + '\'' +
                ", lowercaseNickname='" + lowercaseNickname + '\'' +
                ", ip='" + ip + '\'' +
                ", loginIp='" + loginIp + '\'' +
                ", uuid='" + uuid + '\'' +
                ", regDate=" + regDate +
                ", loginDate=" + loginDate +
                ", isPremium=" + isPremium() +
                '}';
    }
}
