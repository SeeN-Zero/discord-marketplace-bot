package seen.discord.marketplace.discord;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;

@ApplicationScoped
public class DiscordBotLifecycle {

    private static final Logger LOG = Logger.getLogger(DiscordBotLifecycle.class);
    private final MarketSlashCommandListener marketListener;
    private final String botToken;
    private final Optional<String> guildId;

    private JDA jda;

    public DiscordBotLifecycle(
        MarketSlashCommandListener marketListener,
        @ConfigProperty(name = "discord.bot.token") String botToken,
        @ConfigProperty(name = "discord.guild.id") Optional<String> guildId
    ) {
        this.marketListener = marketListener;
        this.botToken = botToken;
        this.guildId = guildId;
    }

    void onStart(@Observes StartupEvent event) {
        if (botToken == null || botToken.isBlank()) {
            LOG.warn("discord.bot.token is missing. Discord bot startup skipped.");
            return;
        }

        try {
            jda = JDABuilder.createDefault(botToken)
                .addEventListeners(marketListener)
                .build()
                .awaitReady();

            registerCommands();
            LOG.info("Discord bot started successfully.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.error("Discord bot startup interrupted", ex);
        } catch (Exception ex) {
            LOG.error("Failed to start Discord bot", ex);
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        if (jda != null) {
            jda.shutdown();
        }
    }

    private void registerCommands() {
        var marketCommand = Commands.slash("market", "Marketplace commands")
            .addSubcommands(
                new SubcommandData("list", "Create a market listing")
                    .addOption(OptionType.STRING, "type", "BUY or SELL", true)
                    .addOption(OptionType.STRING, "description", "Item details", true),
                new SubcommandData("delist", "Remove all your listings"),
                new SubcommandData("show", "Show all active listings"),
                new SubcommandData("commands", "Show available marketplace commands")
            );

        String configuredGuildId = guildId.map(String::trim).orElse("");
        if (!configuredGuildId.isBlank()) {
            Guild guild = jda.getGuildById(configuredGuildId);
            if (guild == null) {
                LOG.warnf("Guild %s was not found. Falling back to global command registration.", configuredGuildId);
                jda.updateCommands().addCommands(marketCommand).queue();
            } else {
                guild.updateCommands().addCommands(marketCommand).queue();
                LOG.infof("Registered slash commands in guild %s", configuredGuildId);
            }
        } else {
            jda.updateCommands().addCommands(marketCommand).queue();
            LOG.info("Registered slash commands globally.");
        }
    }
}
