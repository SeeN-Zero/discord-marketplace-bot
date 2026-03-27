package seen.discord.marketplace.listing;

public enum ListingType {
    BUY,
    SELL;

    public static ListingType fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Type is required.");
        }
        return ListingType.valueOf(raw.trim().toUpperCase());
    }
}
