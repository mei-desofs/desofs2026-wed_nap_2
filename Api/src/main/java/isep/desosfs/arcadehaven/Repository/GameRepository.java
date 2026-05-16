package isep.desosfs.arcadehaven.Repository;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByPublisher(User publisher);
    List<Game> findByPublisherId(UUID publisherId);
    Optional<Game> findByIdAndPublisher(UUID id, User publisher);
}
