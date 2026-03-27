package seen.discord.marketplace.discord;

import jakarta.enterprise.context.ApplicationScoped;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jboss.logging.Logger;
import seen.discord.marketplace.listing.MarketListing;
import seen.discord.marketplace.listing.MarketListingService;
import seen.discord.marketplace.listing.MarketValidationException;

import java.util.List;

@ApplicationScoped
public class MarketSlashCommandListener extends ListenerAdapter {

    private static final Logger LOG = Logger.getLogger(MarketSlashCommandListener.class);
    private final MarketListingService listingService;

    public MarketSlashCommandListener(MarketListingService listingService) {
        this.listingService = listingService;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!"market".equals(event.getName())) {
            return;
        }

        try {
            String subcommand = event.getSubcommandName();
            if (subcommand == null) {
                event.reply("Unknown market command.").setEphemeral(true).queue();
                return;
            }

            switch (subcommand) {
                case "list" -> handleList(event);
                case "delist" -> handleDelist(event);
                case "show" -> handleShow(event);
                case "commands" -> handleCommands(event);
                default -> event.reply("Unsupported subcommand: " + subcommand).setEphemeral(true).queue();
            }
        } catch (MarketValidationException ex) {
            event.reply(ex.getMessage()).setEphemeral(true).queue();
        } catch (Exception ex) {
            LOG.error("Unhandled error while processing market command", ex);
            event.reply("Something went wrong while processing this command.").setEphemeral(true).queue();
        }
    }

    private void handleList(SlashCommandInteractionEvent event) {
        OptionMapping typeOption = event.getOption("type");
        OptionMapping descriptionOption = event.getOption("description");
        if (typeOption == null || descriptionOption == null) {
            event.reply("Missing required options: type and description.").setEphemeral(true).queue();
            return;
        }

        User user = event.getUser();
        listingService.createListing(
            user.getId(),
            user.getName(),
            typeOption.getAsString(),
            descriptionOption.getAsString()
        );

        event.reply("Listing created.").setEphemeral(true).queue();
    }

    private void handleDelist(SlashCommandInteractionEvent event) {
        long removed = listingService.delistAllByUser(event.getUser().getId());
        event.reply("Removed " + removed + " listing(s).").setEphemeral(true).queue();
    }

    private void handleShow(SlashCommandInteractionEvent event) {
        List<MarketListing> listings = listingService.showAllListings();
        event.reply(MarketMessageFormatter.formatListings(listings)).setEphemeral(false).queue();
    }

    private void handleCommands(SlashCommandInteractionEvent event) {
        event.reply(MarketMessageFormatter.formatAvailableCommands())
            .setEphemeral(true)
            .queue();
    }
}
