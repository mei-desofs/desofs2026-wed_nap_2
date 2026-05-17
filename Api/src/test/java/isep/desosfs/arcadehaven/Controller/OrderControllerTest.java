package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Dto.Request.AddOrderItemRequest;
import isep.desosfs.arcadehaven.Dto.Request.CreateOrderRequest;
import isep.desosfs.arcadehaven.Dto.Response.OrderResponse;
import isep.desosfs.arcadehaven.Service.OrderService;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController controller;

    @Test
    void shouldGetMyOrders() {
        List<OrderResponse> orders = List.of(createOrderResponse());

        when(orderService.getMyOrders()).thenReturn(orders);

        var response = controller.getMyOrders();

        assertEquals(orders, response.getBody());

        verify(orderService).getMyOrders();
    }

    @Test
    void shouldGetOrderById() {
        UUID id = UUID.randomUUID();

        OrderResponse order = createOrderResponse();

        when(orderService.getOrderById(id)).thenReturn(order);

        var response = controller.getOrder(id);

        assertEquals(order, response.getBody());

        verify(orderService).getOrderById(id);
    }

    @Test
    void shouldCreateOrder() {
        CreateOrderRequest request =
                new CreateOrderRequest(List.of(UUID.randomUUID()));

        OrderResponse order = createOrderResponse();

        when(orderService.createOrder(request)).thenReturn(order);

        var response = controller.createOrder(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(order, response.getBody());

        verify(orderService).createOrder(request);
    }

    @Test
    void shouldCompleteOrder() {
        UUID id = UUID.randomUUID();

        OrderResponse order = createOrderResponse();

        when(orderService.completeOrder(id)).thenReturn(order);

        var response = controller.completeOrder(id);

        assertEquals(order, response.getBody());

        verify(orderService).completeOrder(id);
    }

    @Test
    void shouldCancelOrder() {
        UUID id = UUID.randomUUID();

        OrderResponse order = createOrderResponse();

        when(orderService.cancelOrder(id)).thenReturn(order);

        var response = controller.cancelOrder(id);

        assertEquals(order, response.getBody());

        verify(orderService).cancelOrder(id);
    }

    @Test
    void shouldAddItemToOrder() {
        UUID id = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        AddOrderItemRequest request = new AddOrderItemRequest(gameId);

        OrderResponse order = createOrderResponse();

        when(orderService.addItemToOrder(id, gameId)).thenReturn(order);

        var response = controller.addItem(id, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(order, response.getBody());
        verify(orderService).addItemToOrder(id, gameId);
    }

    @Test
    void shouldRemoveItemFromOrder() {
        UUID id = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        OrderResponse order = createOrderResponse();

        when(orderService.removeItemFromOrder(id, gameId)).thenReturn(order);

        var response = controller.removeItem(id, gameId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(order, response.getBody());
        verify(orderService).removeItemFromOrder(id, gameId);
    }

    @Test
    void shouldDownloadInvoice() {
        UUID id = UUID.randomUUID();
        byte[] data = "invoice content".getBytes();

        when(orderService.downloadInvoice(id)).thenReturn(data);

        var response = controller.downloadInvoice(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(data, response.getBody());
        verify(orderService).downloadInvoice(id);
    }

    @Test
    void shouldDownloadKeyCard() {
        UUID id = UUID.randomUUID();
        byte[] data = "keycard content".getBytes();

        when(orderService.downloadKeyCard(id)).thenReturn(data);

        var response = controller.downloadKeyCard(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(data, response.getBody());
        verify(orderService).downloadKeyCard(id);
    }

    private OrderResponse createOrderResponse() {
        return new OrderResponse(
                UUID.randomUUID(),
                "PENDING",
                BigDecimal.TEN,
                List.of(),
                LocalDateTime.now()
        );
    }
}
