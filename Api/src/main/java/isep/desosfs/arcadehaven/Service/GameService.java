package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Exception.StorageException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.apache.tika.Tika;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public GameService(GameRepository gameRepository, UserRepository userRepository,
                       FileStorageService fileStorageService) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getAllActiveGames() {
        return gameRepository.findByStatus(GameStatus.ACTIVE)
                .stream().map(GameResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getAllGames() {
        return gameRepository.findAll().stream().map(GameResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GameResponse getGameById(UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        User publisher = getCurrentUser();
        Game game = Game.create(request.title(), request.description(), request.price(),
                request.rawgApiId(), publisher);
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse updateGame(UUID id, UpdateGameRequest request) {
        User publisher = getCurrentUser();
        Game game = gameRepository.findByIdAndPublisher(id, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));

        if (request.price() != null) game.updatePrice(request.price());
        game.updateDetails(request.title(), request.description());
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse approveGame(UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        game.approve();
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse rejectGame(UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        game.reject();
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse removeGame(UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
        game.remove();
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse uploadGameFile(UUID id, MultipartFile file, FileType fileType) {
        validateMimeType(file, fileType);

        User publisher = getCurrentUser();
        Game game = gameRepository.findByIdAndPublisher(id, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));

        String path = fileStorageService.saveFile(file, "games/images");
        game.addFile(file.getOriginalFilename(), path, fileType);
        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional(readOnly = true)
    public byte[] downloadGameFile(UUID gameId, String remotePath) {
        User publisher = getCurrentUser();
        gameRepository.findByIdAndPublisher(gameId, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));
        return fileStorageService.downloadFile(remotePath);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getMyGames() {
        User publisher = getCurrentUser();
        return gameRepository.findByPublisher(publisher)
                .stream().map(GameResponse::from).toList();
    }

    private void validateMimeType(MultipartFile file, FileType fileType) {
        Tika tika = new Tika();
        String detectedType;
        try {
            detectedType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new StorageException("Cannot read uploaded file for type detection", e);
        }
        if (detectedType == null) {
            throw new IllegalArgumentException("Cannot detect file type");
        }
        switch (fileType) {
            case IMAGE, SCREENSHOT, COVER -> {
                if (!detectedType.startsWith("image/")) {
                    throw new IllegalArgumentException("Invalid file type for " + fileType + ": " + detectedType);
                }
            }
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
