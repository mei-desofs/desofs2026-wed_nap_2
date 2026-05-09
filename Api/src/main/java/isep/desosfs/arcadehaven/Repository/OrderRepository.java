package isep.desosfs.arcadehaven.Repository;

import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByBuyer(User buyer);
    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);
}
