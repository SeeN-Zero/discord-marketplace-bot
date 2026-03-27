package seen.discord.marketplace.discord;

import seen.discord.marketplace.listing.MarketListing;

import java.util.List;

public final class MarketMessageFormatter {

    private MarketMessageFormatter() {
    }

    public static String formatListings(List<MarketListing> listings) {
        if (listings.isEmpty()) {
            return "No active listings right now.";
        }

        StringBuilder builder = new StringBuilder("Active market listings:\n");
        for (MarketListing listing : listings) {
            builder.append(listing.username)
                .append(' ')
                .append(toDisplayType(listing.type.name()))
                .append(' ')
                .append(listing.description)
                .append('\n');
        }
        return builder.toString().trim();
    }

    public static String formatAvailableCommands() {
        return """
            Available marketplace commands:
            - /market list <type> <description> : Create a listing (type: BUY or SELL)
            - /market delist : Remove all your listings
            - /market show : Show all active listings
            - /market commands : Show this command list
            """.trim();
    }

    private static String toDisplayType(String rawType) {
        String lower = rawType.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
