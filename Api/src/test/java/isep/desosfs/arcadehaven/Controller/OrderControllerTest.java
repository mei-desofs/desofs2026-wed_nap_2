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
