package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.ImportKeyRequest;
import isep.desosfs.arcadehaven.Dto.Response.LibraryResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public LibraryService(LibraryRepository libraryRepository, UserRepository userRepository,
                          GameRepository gameRepository) {
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional(readOnly = true)
    public LibraryResponse getMyLibrary() {
        User user = getCurrentUser();
        Library library = libraryRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found"));
        return LibraryResponse.from(library);
    }

    @Transactional
    public LibraryResponse importGameKey(ImportKeyRequest request) {
        User user = getCurrentUser();
        Library library = libraryRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found"));

        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new BusinessException("Game is not available: " + game.getTitle());
        }
        if (library.ownsGame(request.gameId())) {
            throw new BusinessException("You already own this game: " + game.getTitle());
        }

        library.addGame(game, request.activationKey());
        return LibraryResponse.from(libraryRepository.save(library));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
