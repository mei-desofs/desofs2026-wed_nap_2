package isep.desosfs.arcadehaven.Repository;

import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBuyer(User buyer);
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    @Query("SELECT COUNT(oi), COALESCE(SUM(oi.price), 0) FROM Order o JOIN o.items oi"
            + " WHERE oi.game.id = :gameId AND o.status = 'COMPLETED'")
    List<Object[]> findMetricsByGameId(@Param("gameId") UUID gameId);
}
