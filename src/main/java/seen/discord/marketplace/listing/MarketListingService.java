package seen.discord.marketplace.listing;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class MarketListingService {

    private static final int MAX_LISTINGS_PER_USER = 3;
    private final MarketListingRepository repository;

    public MarketListingService(MarketListingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MarketListing createListing(String userId, String username, String rawType, String rawDescription) {
        String normalizedDescription = normalizeDescription(rawDescription);
        ListingType type = parseType(rawType);

        long currentCount = repository.countByUserId(userId);
        if (currentCount >= MAX_LISTINGS_PER_USER) {
            throw new MarketValidationException("You already have 3 active listings. Delist before creating a new one.");
        }

        boolean duplicate = repository.findDuplicateByUserAndContent(userId, type, normalizedDescription).isPresent();
        if (duplicate) {
            throw new MarketValidationException("Duplicate listing detected. Edit the description before posting again.");
        }

        MarketListing listing = new MarketListing();
        listing.userId = userId;
        listing.username = username;
        listing.type = type;
        listing.description = normalizedDescription;
        repository.persist(listing);
        return listing;
    }

    @Transactional
    public long delistAllByUser(String userId) {
        return repository.deleteByUserId(userId);
    }

    @Transactional
    public List<MarketListing> showAllListings() {
        return repository.findAllActiveNewestFirst();
    }

    private ListingType parseType(String rawType) {
        try {
            return ListingType.fromRaw(rawType);
        } catch (IllegalArgumentException ex) {
            throw new MarketValidationException("Type must be BUY or SELL.");
        }
    }

    private String normalizeDescription(String rawDescription) {
        if (rawDescription == null || rawDescription.isBlank()) {
            throw new MarketValidationException("Description must not be empty.");
        }
        String normalized = rawDescription.trim();
        if (normalized.length() > 500) {
            throw new MarketValidationException("Description is too long (max 500 characters).");
        }
        return normalized;
    }
}
