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
            fileStorageService.generateInvoice(order);
            String invoicePath = "invoices/invoice_" + order.getId() + ".txt";
            order.complete(invoicePath);

            order.getItems().forEach(item ->
                    library.addGame(item.getGame(), item.getActivationKey()));

            libraryRepository.save(library);
        } catch (Exception e) {
            throw new BusinessException("Failed to complete order: " + e.getMessage());
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        User buyer = getCurrentUser();
        Order order = getOrderForBuyer(orderId, buyer);
        order.cancel();
        return OrderResponse.from(orderRepository.save(order));
    }

    public List<OrderResponse> getMyOrders() {
        User buyer = getCurrentUser();
        return orderRepository.findByBuyerOrderByCreatedAtDesc(buyer)
                .stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getOrderById(UUID orderId) {
        User buyer = getCurrentUser();
        return OrderResponse.from(getOrderForBuyer(orderId, buyer));
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
