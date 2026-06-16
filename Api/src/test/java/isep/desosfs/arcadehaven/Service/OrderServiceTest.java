package isep.desosfs.arcadehaven.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import isep.desosfs.arcadehaven.Domain.Game;
import isep.desosfs.arcadehaven.Domain.Library;
import isep.desosfs.arcadehaven.Domain.Order;
import isep.desosfs.arcadehaven.Domain.OrderItem;
import isep.desosfs.arcadehaven.Domain.User;
import isep.desosfs.arcadehaven.Domain.Enums.OrderStatus;
import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.CreateOrderRequest;
import isep.desosfs.arcadehaven.Exception.BusinessException;
import isep.desosfs.arcadehaven.Exception.ResourceNotFoundException;
import isep.desosfs.arcadehaven.Repository.GameRepository;
import isep.desosfs.arcadehaven.Repository.LibraryRepository;
import isep.desosfs.arcadehaven.Repository.OrderRepository;
import isep.desosfs.arcadehaven.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    private static final String VALID_KEY = "1A2B3C4D5E6F7890ABCDEF1234567890";

    @Mock OrderRepository orderRepository;
    @Mock GameRepository gameRepository;
    @Mock UserRepository userRepository;
    @Mock LibraryRepository libraryRepository;
    @Mock FileStorageService fileStorageService;

    @InjectMocks OrderService orderService;

    private User buyer;
    private Library library;

    @BeforeEach
    void setup() {
        buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer", null)
        );

        library = Library.create(buyer);
    }

    @Test
    void shouldCreateOrder() {
        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CreateOrderRequest req = new CreateOrderRequest(List.of(UUID.randomUUID()));

        var result = orderService.createOrder(req);

        assertNotNull(result);
    }

    @Test
    void shouldFailWhenGameNotActive() {
        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.remove();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(new CreateOrderRequest(List.of(UUID.randomUUID()))));
    }

    @Test
    void shouldCompleteOrder() throws Exception {
        UUID buyerId = UUID.randomUUID();

        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);
        Order order = Order.create(buyer);
        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        order.addItem(item);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(orderRepository.save(any())).thenReturn(order);
        when(fileStorageService.generateInvoice(any())).thenReturn("invoices/invoice.txt");
        when(fileStorageService.saveActivationKeysFile(any())).thenReturn("keys/keys.txt");

        orderService.completeOrder(UUID.randomUUID());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void shouldCancelOrder() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);
        Order order = Order.create(buyer);
        ReflectionTestUtils.setField(order.getBuyer(), "id", buyerId);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.cancelOrder(UUID.randomUUID());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void shouldCalculateTotalWithMultipleItems() {
        User buyer = User.create("b", "e", "p", Role.BUYER);
        Order order = Order.create(buyer);

        Game g1 = Game.create("g1", "d", BigDecimal.TEN, "r", null, buyer);
        Game g2 = Game.create("g2", "d", BigDecimal.valueOf(20), "r", null, buyer);
        g1.approve();
        g2.approve();

        order.addItem(OrderItem.of(g1, BigDecimal.TEN));
        order.addItem(OrderItem.of(g2, BigDecimal.valueOf(20)));

        assertEquals(BigDecimal.valueOf(30), order.calculateTotal());
    }

    @Test
    void shouldThrowWhenCancelNonPendingOrder() {
        User buyer = User.create("b", "e", "p", Role.BUYER);
        Order order = Order.create(buyer);

        order.cancel();

        assertThrows(IllegalStateException.class, order::cancel);
    }

    @Test
    void shouldThrowWhenCompleteNonPendingOrder() {
        User buyer = User.create("b", "e", "p", Role.BUYER);
        Order order = Order.create(buyer);

        order.cancel();

        assertThrows(IllegalStateException.class,
                () -> order.complete("path"));
    }

    @Test
    void shouldReturnFalseWhenGameNotOwned() {
        User user = User.create("u", "e", "p", Role.BUYER);
        Library library = Library.create(user);

        assertFalse(library.ownsGame(UUID.randomUUID()));
    }

    @Test
    void shouldReturnUserOrders() {
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(buyer));
        when(orderRepository.findByBuyerOrderByCreatedAtDesc(buyer))
                .thenReturn(List.of());

        var result = orderService.getMyOrders();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetOrderById() {
        UUID buyerId = UUID.randomUUID();

        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);
        ReflectionTestUtils.setField(order, "buyer", buyer);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        var result = orderService.getOrderById(UUID.randomUUID());

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenOrderNotOwned() {
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        User other = User.create("other", "mail2", "pass", Role.BUYER);

        Order order = Order.create(other);

        when(userRepository.findByUsername(any())).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(Exception.class,
                () -> orderService.getOrderById(UUID.randomUUID()));
    }

    @Test
    void shouldAddItemToOrder() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);
        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));
        when(orderRepository.save(any())).thenReturn(order);

        var result = orderService.addItemToOrder(UUID.randomUUID(), UUID.randomUUID());

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenAddingInactiveGameToOrder() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);
        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));

        assertThrows(BusinessException.class,
                () -> orderService.addItemToOrder(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldRemoveItemFromOrder() {
        UUID buyerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();
        ReflectionTestUtils.setField(game, "id", gameId);

        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        var result = orderService.removeItemFromOrder(UUID.randomUUID(), gameId);

        assertNotNull(result);
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void shouldDownloadInvoice() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);
        ReflectionTestUtils.setField(order, "invoicePath", "invoices/inv.txt");

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(fileStorageService.downloadFile("invoices/inv.txt")).thenReturn("data".getBytes());

        byte[] result = orderService.downloadInvoice(UUID.randomUUID());

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenInvoiceNotYetGenerated() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.downloadInvoice(UUID.randomUUID()));
    }

    @Test
    void shouldDownloadKeyCard() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);
        order.complete("invoices/inv.txt");

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        byte[] result = orderService.downloadKeyCard(UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void shouldThrowWhenKeyCardRequestedForNonCompletedOrder() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.downloadKeyCard(UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenGameNotActive() {

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.reject(); // ou remove/estado não ACTIVE

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));

        CreateOrderRequest req =
                new CreateOrderRequest(List.of(UUID.randomUUID()));

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(req));
    }

    @Test
    void shouldThrowWhenGameNotFoundInCreateOrder() {

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.empty());

        CreateOrderRequest req =
                new CreateOrderRequest(List.of(UUID.randomUUID()));

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(req));
    }

    @Test
    void shouldThrowWhenGameAlreadyInOrder() {

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();

        Order order = Order.create(buyer);
        order.addItem(OrderItem.of(game, BigDecimal.TEN));

        lenient().when(userRepository.findByUsername("buyer"))
                .thenReturn(Optional.of(buyer));

        lenient().when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));

        lenient().when(libraryRepository.findByUser(buyer))
                .thenReturn(Optional.of(library));

        lenient().when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        assertThrows(NullPointerException.class,
                () -> orderService.addItemToOrder(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenInvoicePathNull() {

        Order order = Order.create(buyer); // invoicePath = null

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(NullPointerException.class,
                () -> orderService.downloadInvoice(UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenOrderNotBelongsToUser() {

        User other = User.create("other", "mail", "pass", Role.BUYER);
        Order order = Order.create(other);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(NullPointerException.class,
                () -> orderService.getOrderById(UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenCurrentUserNotFound() {

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getMyOrders());
    }

    @Test
    void shouldThrowWhenInvoicePathIsNull() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.downloadInvoice(UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenOrderBelongsToOtherUser() {
        UUID buyerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        User other = User.create("other", "mail2", "pass", Role.BUYER);
        ReflectionTestUtils.setField(other, "id", otherId);

        Order order = Order.create(other);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.getOrderById(UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenBuyerAlreadyOwnsGameInCreateOrder() {
        UUID gameId = UUID.randomUUID();

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();
        ReflectionTestUtils.setField(game, "id", gameId);

        library.addGame(game, VALID_KEY);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(new CreateOrderRequest(List.of(gameId))));
    }

    @Test
    void shouldThrowWhenBuyerAlreadyOwnsGameInAddItem() {
        UUID buyerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Game game = Game.create("g", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();
        ReflectionTestUtils.setField(game, "id", gameId);

        library.addGame(game, VALID_KEY);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.of(game));

        assertThrows(BusinessException.class,
                () -> orderService.addItemToOrder(UUID.randomUUID(), gameId));
    }

    @Test
    void shouldDownloadKeyCardWithItems() {
        UUID buyerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Game game = Game.create("My Game", "d", BigDecimal.TEN, "r", null, buyer);
        game.approve();
        ReflectionTestUtils.setField(game, "id", gameId);

        Order order = Order.create(buyer);
        OrderItem item = OrderItem.of(game, BigDecimal.TEN);
        order.addItem(item);
        order.complete("invoices/inv.txt");

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        byte[] result = orderService.downloadKeyCard(UUID.randomUUID());

        assertNotNull(result);
        String content = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(content.contains("My Game"));
        assertTrue(content.contains("ArcadeHaven"));
    }

    @Test
    void shouldThrowWhenLibraryNotFoundOnComplete() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.completeOrder(UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenLibraryNotFoundOnAddItem() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.addItemToOrder(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenGameNotFoundOnAddItem() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.create("buyer", "mail", "pass", Role.BUYER);
        ReflectionTestUtils.setField(buyer, "id", buyerId);

        Order order = Order.create(buyer);

        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(libraryRepository.findByUser(buyer)).thenReturn(Optional.of(library));
        when(gameRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.addItemToOrder(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(userRepository.findByUsername("buyer")).thenReturn(Optional.of(buyer));
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById(UUID.randomUUID()));
    }
}