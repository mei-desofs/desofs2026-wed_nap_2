package isep.desosfs.arcadehaven.Repository;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByPublisher(User publisher);
    List<Game> findByPublisherId(UUID publisherId);
    Optional<Game> findByIdAndPublisher(UUID id, User publisher);

    @Query("SELECT g FROM Game g WHERE g.status = 'ACTIVE'"
            + " AND (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%')))"
            + " AND (:minPrice IS NULL OR g.price >= :minPrice)"
            + " AND (:maxPrice IS NULL OR g.price <= :maxPrice)")
    List<Game> findActiveWithFilters(
            @Param("title") String title,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
