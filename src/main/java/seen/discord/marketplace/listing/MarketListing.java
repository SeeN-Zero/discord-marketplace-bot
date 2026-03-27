package seen.discord.marketplace.listing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "market_listings")
public class MarketListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    public String userId;

    @Column(name = "username", nullable = false, length = 64)
    public String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    public ListingType type;

    @Column(name = "description", nullable = false, length = 500)
    public String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
