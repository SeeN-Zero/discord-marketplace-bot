package seen.discord.marketplace.listing;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MarketListingRepository implements PanacheRepository<MarketListing> {

    public long countByUserId(String userId) {
        return count("userId", userId);
    }

    public long deleteByUserId(String userId) {
        return delete("userId", userId);
    }

    public List<MarketListing> findAllActiveNewestFirst() {
        return listAll(Sort.descending("createdAt"));
    }

    public Optional<MarketListing> findDuplicateByUserAndContent(String userId, ListingType type, String description) {
        return find("userId = ?1 and type = ?2 and description = ?3", userId, type, description).firstResultOptional();
    }
}
