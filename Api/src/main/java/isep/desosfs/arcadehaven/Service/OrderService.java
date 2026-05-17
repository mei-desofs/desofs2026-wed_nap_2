package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.GameStatus;
import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Domain.OrderItem;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Dto.Request.CreateOrderRequest;
import isep.desosfs.arcadehaven.Dto.Response.OrderResponse;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.OrderRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final FileStorageService fileStorageService;

    public OrderService(OrderRepository orderRepository, GameRepository gameRepository,
                        UserRepository userRepository, LibraryRepository libraryRepository,
                        FileStorageService fileStorageService) {
        this.orderRepository = orderRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.libraryRepository = libraryRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User buyer = getCurrentUser();
        Library library = libraryRepository.findByUser(buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found"));

        Order order = Order.create(buyer);

        for (UUID gameId : request.gameIds()) {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));

            if (game.getStatus() != GameStatus.ACTIVE) {
                throw new BusinessException("Game is not available: " + game.getTitle());
            }
            if (library.ownsGame(gameId)) {
                throw new BusinessException("You already own this game: " + game.getTitle());
            }

            order.addItem(OrderItem.of(game, game.getPrice()));
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse completeOrder(UUID orderId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);

        Library library = libraryRepository.findByUser(buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found"));

        try {
            String invoicePath = fileStorageService.generateInvoice(order);
            order.complete(invoicePath);

            order.getItems().forEach(item ->
                    library.addGame(item.getGame(), item.getActivationKey()));

            libraryRepository.save(library);
            fileStorageService.saveActivationKeysFile(order);
        } catch (Exception e) {
            throw new BusinessException("Failed to complete order: " + e.getMessage());
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse addItemToOrder(UUID orderId, UUID gameId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);

        Library library = libraryRepository.findByUser(buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Library not found"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameId));

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new BusinessException("Game is not available: " + game.getTitle());
        }
        if (library.ownsGame(gameId)) {
            throw new BusinessException("You already own this game: " + game.getTitle());
        }
        boolean alreadyInOrder = order.getItems().stream()
                .anyMatch(i -> i.getGame().getId().equals(gameId));
        if (alreadyInOrder) {
            throw new BusinessException("Game is already in this order: " + game.getTitle());
        }

        order.addItem(OrderItem.of(game, game.getPrice()));
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse removeItemFromOrder(UUID orderId, UUID gameId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);
        order.removeItem(gameId);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);
        order.cancel();
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User buyer = getCurrentUser();
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer)
                .stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        User buyer = getCurrentUser();
        return OrderResponse.from(getOrderForBuyer(orderId, buyer));
    }

    @Transactional(readOnly = true)
    public byte[] downloadInvoice(UUID orderId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);
        if (order.getInvoicePath() == null) {
            throw new BusinessException("Invoice not yet generated for this order");
        }
        return fileStorageService.downloadFile(order.getInvoicePath());
    }

    @Transactional(readOnly = true)
    public byte[] downloadKeyCard(UUID orderId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);
        if (order.getStatus() != isep.desosfs.arcadehaven.Domain.Enums.OrderStatus.COMPLETED) {
            throw new BusinessException("Key card is only available for completed orders");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ArcadeHaven — Activation Key Card\n");
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Date: ").append(order.getCreatedAt()).append("\n\n");
        // ASVS V1.1.2 — strip control chars before embedding user-controlled data in text file
        order.getItems().forEach(item ->
                sb.append(item.getGame().getTitle().replaceAll("[\\r\\n\\t]", " ").trim())
                  .append(": ")
                  .append(item.getActivationKey())
                  .append("\n"));
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private Order getOrderForBuyer(UUID orderId, User buyer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new BusinessException("Order does not belong to you");
        }
        return order;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
