package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private GameRepository gameRepository;
    @Mock private UserRepository userRepository;
    @Mock private LibraryRepository libraryRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private User publisher;
    private Game activeGame;
    private Library library;

    @BeforeEach
    void setUp() {
        buyer = User.create("buyer", "buyer@test.com", "hash", Role.BUYER);
        publisher = User.create("pub", "pub@test.com", "hash", Role.PUBLISHER);
        activeGame = Game.create("Test Game", "desc", BigDecimal.TEN, null, publisher);
        activeGame.approve();
        library = Library.create(buyer);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("buyer");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
    }

    @Test
    void createOrder_success_returnsPendingOrder() {
        UUID gameId = activeGame.getId();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(List.of(gameId));
        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void createOrder_gameNotFound_throwsResourceNotFound() {
        UUID missing = UUID.randomUUID();
        when(gameRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(List.of(missing))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Game not found");
    }

    @Test
    void createOrder_gameNotActive_throwsBusinessException() {
        Game pendingGame = Game.create("Pending", "desc", BigDecimal.TEN, null, publisher);
        UUID gameId = pendingGame.getId();
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(pendingGame));

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(List.of(gameId))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Game is not available");
    }

    @Test
    void createOrder_alreadyOwned_throwsBusinessException() {
        UUID gameId = activeGame.getId();
        library.addGame(activeGame, "EXISTING-KEY");
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));

        assertThatThrownBy(() -> orderService.createOrder(new CreateOrderRequest(List.of(gameId))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("You already own this game");
    }

    @Test
    void cancelOrder_pendingOrder_returnsCancelled() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(activeGame, BigDecimal.TEN));
        UUID orderId = order.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(orderId);

        assertThat(response.status()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelOrder_orderNotBelongingToBuyer_throwsBusinessException() {
        User otherBuyer = User.create("other", "other@test.com", "hash", Role.BUYER);
        Order order = Order.create(otherBuyer);
        UUID orderId = order.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order does not belong to you");
    }

    @Test
    void addItemToOrder_success_addsGame() {
        Order order = Order.create(buyer);
        UUID orderId = order.getId();
        UUID gameId = activeGame.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.addItemToOrder(orderId, gameId);

        assertThat(response.items()).hasSize(1);
    }

    @Test
    void addItemToOrder_duplicateInOrder_throwsBusinessException() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(activeGame, BigDecimal.TEN));
        UUID orderId = order.getId();
        UUID gameId = activeGame.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(activeGame));

        assertThatThrownBy(() -> orderService.addItemToOrder(orderId, gameId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Game is already in this order");
    }

    @Test
    void removeItemFromOrder_success_removesGame() {
        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(activeGame, BigDecimal.TEN));
        UUID orderId = order.getId();
        UUID gameId = activeGame.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.removeItemFromOrder(orderId, gameId);

        assertThat(response.items()).isEmpty();
    }

    @Test
    void getMyOrders_returnsAllOrders() {
        Order order = Order.create(buyer);
        when(orderRepository.findByBuyerOrderByCreatedAtDesc(buyer))
                .thenReturn(List.of(order));

        List<OrderResponse> orders = orderService.getMyOrders();

        assertThat(orders).hasSize(1);
    }

    @Test
    void getOrderById_notOwned_throwsBusinessException() {
        User other = User.create("other", "other@test.com", "hash", Role.BUYER);
        Order order = Order.create(other);
        UUID orderId = order.getId();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order does not belong to you");
    }
}
