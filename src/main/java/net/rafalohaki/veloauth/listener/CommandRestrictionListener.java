package net.rafalohaki.veloauth.command;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import net.rafalohaki.veloauth.VeloAuth;
import net.rafalohaki.veloauth.cache.AuthCache;
import net.kyori.adventure.text.Component;

public class CommandRestrictionListener {

    private final VeloAuth plugin;
    private final AuthCache authCache;

    public CommandRestrictionListener(VeloAuth plugin) {
        this.plugin = plugin;
        this.authCache = plugin.getAuthCache();
    }

    @Subscribe
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        // Vérifier si le joueur est sur le serveur "auth"
        if (player.getCurrentServer().isEmpty()) return;

        String server = player.getCurrentServer().get().getServerInfo().getName();
        if (!server.equalsIgnoreCase("auth")) {
            return; // pas sur auth → ne rien bloquer
        }

        // Si déjà authentifié → laisser passer
        if (authCache.isPlayerAuthorized(player.getUniqueId(), player.getRemoteAddress().getAddress())) {
            return;
        }

        String raw = event.getCommand().trim().toLowerCase();

        // Autoriser /login <arg>
        if (raw.startsWith("login ")) {
            String[] args = raw.split(" ");
            if (args.length == 2) return;
        }

        // Autoriser /register <arg> <arg>
        if (raw.startsWith("register ")) {
            String[] args = raw.split(" ");
            if (args.length == 3) return;
        }

        // Bloquer tout le reste
        event.setResult(CommandExecuteEvent.CommandResult.denied());
        player.sendMessage(Component.text("§cVous devez vous connecter avec /login ou /register."));
    }
}
