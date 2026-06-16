package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.FileType;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.GameFile;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.CreateGameRequest;
import isep.desosfs.arcadehaven.Dto.Request.GameFilterRequest;
import isep.desosfs.arcadehaven.Dto.Request.UpdateGameRequest;
import isep.desosfs.arcadehaven.Dto.Response.FileDownloadResult;
import isep.desosfs.arcadehaven.Dto.Response.GameMetricsResponse;
import isep.desosfs.arcadehaven.Dto.Response.GameResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Exception.StorageException;
import isep.desosfs.arcadehaven.Integration.RawgApiClient;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.OrderRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import isep.desosfs.arcadehaven.Security.ClamAVService;
import isep.desosfs.arcadehaven.Validation.FileValidator;
import org.apache.tika.Tika;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    private static final long MAX_UNITS_SOLD = 1_000_000_000L;
    private static final BigDecimal MAX_REVENUE = new BigDecimal("10000000.00");

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final OrderRepository orderRepository;
    private final FileValidator fileValidator;
    private final ClamAVService clamAVService;
    private final RawgApiClient rawgApiClient;

    public GameService(GameRepository gameRepository, UserRepository userRepository,
                       FileStorageService fileStorageService, OrderRepository orderRepository,
                       FileValidator fileValidator, ClamAVService clamAVService,
                       RawgApiClient rawgApiClient) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.orderRepository = orderRepository;
        this.fileValidator = fileValidator;
        this.clamAVService = clamAVService;
        this.rawgApiClient = rawgApiClient;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getAllActiveGames(GameFilterRequest request) {
        if (request.minPrice() != null && request.maxPrice() != null && request.minPrice().compareTo(request.maxPrice()) > 0) {
            throw new BusinessException("Minimum price cannot exceed maximum price");
        }

        return gameRepository.findActiveWithFilters(request.title(), request.category(), request.minPrice(), request.maxPrice())
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
                request.rawgApiId(), request.category(), publisher);

        // RF-12 — enrich description, category and cover image from RAWG when rawgApiId is supplied
        if (request.rawgApiId() != null && !request.rawgApiId().isBlank()) {
            rawgApiClient.fetchGame(request.rawgApiId()).ifPresent(rawgData ->
                    game.enrichFromRawg(rawgData.description(), rawgData.category(), rawgData.coverImageUrl()));
        }

        return GameResponse.from(gameRepository.save(game));
    }

    @Transactional
    public GameResponse updateGame(UUID id, UpdateGameRequest request) {
        User publisher = getCurrentUser();
        Game game = gameRepository.findByIdAndPublisher(id, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));

        if (request.price() != null) game.updatePrice(request.price());
        game.updateDetails(request.title(), request.description(), request.category());
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
        //ASVS V5.2.1
        this.fileValidator.validateFileSize(file);

        //ASVS V5.2.2
        this.fileValidator.validateExtension(file, fileType);
        this.fileValidator.validateMimeType(file, fileType);

        //ASVS V5.4.3
        this.clamAVService.scan(file);

        //ASVS V5.3.1
        String safeFilename = UUID.randomUUID() + ".png"; // or based on validated type

        User publisher = getCurrentUser();
        Game game = gameRepository.findByIdAndPublisher(id, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));

        String path = fileStorageService.saveFile(file, "games/images");
        game.addFile(UUID.randomUUID(), safeFilename, path, fileType);
        return GameResponse.from(gameRepository.save(game));
    }

    // ASVS 5.4.2
    @Transactional(readOnly = true)
    public FileDownloadResult downloadGameFile(UUID gameId, UUID fileId) {
        User publisher = getCurrentUser();

        Game game = gameRepository.findByIdAndPublisher(gameId, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));

        GameFile file = game.getFiles().stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        byte[] data = fileStorageService.downloadFile(file.getPath());

        return new FileDownloadResult(file.getFilename(), data);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getMyGames() {
        User publisher = getCurrentUser();
        return gameRepository.findByPublisher(publisher)
                .stream().map(GameResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public GameMetricsResponse getGameMetrics(UUID gameId) {
        User publisher = getCurrentUser();
        Game game = gameRepository.findByIdAndPublisher(gameId, publisher)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found or not owned by you"));
        List<Object[]> rows = orderRepository.findMetricsByGameId(gameId);
        Object[] row = rows.isEmpty() ? new Object[]{0L, BigDecimal.ZERO} : rows.get(0);

        long unitsSold = row[0] != null ? ((Number) row[0]).longValue() : 0L;
        BigDecimal totalRevenue = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;

        if (unitsSold < 0 || unitsSold > MAX_UNITS_SOLD) {
            throw new BusinessException("Invalid unitSold value exceeded maximum enforced limit");
        }

        if (totalRevenue.compareTo(BigDecimal.ZERO) < 0 ||
                totalRevenue.compareTo(MAX_REVENUE) > 0) {
            throw new BusinessException("Invalid totalRevenue value exceeded maximum enforced limit");
        }

        return new GameMetricsResponse(gameId, game.getTitle(), unitsSold, totalRevenue);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
