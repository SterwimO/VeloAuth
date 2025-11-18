package net.rafalohaki.veloauth.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SimpleMessages {
    private final Messages messages;

    public SimpleMessages(Messages messages) {
        this.messages = messages;
    }

    public Component key(String key, NamedTextColor color, Object... args) {
        return Component.text(messages.get(key, args), color);
    }

    public Component loginSuccess() {
        return key("auth.login.success", NamedTextColor.GREEN);
    }

    public Component loginFailed() {
        return key("auth.login.incorrect_password", NamedTextColor.RED);
    }

    public Component registerSuccess() {
        return key("auth.register.success", NamedTextColor.GREEN);
    }

    public Component alreadyLogged() {
        return key("auth.login.already_logged_in", NamedTextColor.YELLOW);
    }

    public Component notRegistered() {
        return key("auth.login.not_registered", NamedTextColor.RED);
    }

    public Component errorDatabase() {
        return key("error.database.query", NamedTextColor.RED);
    }

    public Component errorGeneric() {
        return key("error.unknown_command", NamedTextColor.RED);
    }

    public Component bruteforce(int minutes) {
        return key("security.brute_force.blocked", NamedTextColor.YELLOW, minutes);
    }

    public Component passwordShort(int min) {
        return key("auth.register.password_too_short", NamedTextColor.RED, min);
    }

    public Component passwordMismatch() {
        return key("auth.register.passwords_no_match", NamedTextColor.RED);
    }

    public Component usageLogin() {
        return key("auth.login.usage", NamedTextColor.YELLOW);
    }

    public Component usageRegister() {
        return key("auth.register.usage", NamedTextColor.YELLOW);
    }

    public Component usageChangePassword() {
        return key("auth.changepassword.usage", NamedTextColor.YELLOW);
    }

    public Component nickConflict() {
        return key("player.conflict.header", NamedTextColor.YELLOW);
    }

    public Component nickReserved() {
        return key("player.conflict.description", NamedTextColor.YELLOW);
    }

    public Component invalidUsername() {
        return key("validation.username.invalid", NamedTextColor.RED);
    }

    public Component sessionExpired() {
        return key("security.session.expired", NamedTextColor.YELLOW);
    }
}
