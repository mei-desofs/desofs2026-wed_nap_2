package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Dto.Request.AddOrderItemRequest;
import isep.desosfs.arcadehaven.Dto.Request.CreateOrderRequest;
import isep.desosfs.arcadehaven.Dto.Response.OrderResponse;
import isep.desosfs.arcadehaven.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // RF-15: Add game to existing pending order
    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponse> addItem(@PathVariable UUID id,
                                                  @Valid @RequestBody AddOrderItemRequest request) {
        return ResponseEntity.ok(orderService.addItemToOrder(id, request.gameId()));
    }

    // RF-16: Remove game from existing pending order
    @DeleteMapping("/{id}/items/{gameId}")
    public ResponseEntity<OrderResponse> removeItem(@PathVariable UUID id,
                                                     @PathVariable UUID gameId) {
        return ResponseEntity.ok(orderService.removeItemFromOrder(id, gameId));
    }

    // RF-25: Download invoice file for a completed order
    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable UUID id) {
        byte[] data = orderService.downloadInvoice(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice_" + id + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    // RF-26: Download activation key card for a completed order
    @GetMapping("/{id}/keycard")
    public ResponseEntity<byte[]> downloadKeyCard(@PathVariable UUID id) {
        byte[] data = orderService.downloadKeyCard(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"keycard_" + id + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }
}
