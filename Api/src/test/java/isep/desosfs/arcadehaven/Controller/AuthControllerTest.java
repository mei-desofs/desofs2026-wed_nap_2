package isep.desosfs.arcadehaven.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import isep.desosfs.arcadehaven.Domain.Enums.Role;
import isep.desosfs.arcadehaven.Dto.Request.LoginRequest;
import isep.desosfs.arcadehaven.Dto.Request.RegisterRequest;
import isep.desosfs.arcadehaven.Dto.Response.LoginResponse;
import isep.desosfs.arcadehaven.Dto.Response.RegisterResponse;
import isep.desosfs.arcadehaven.Service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void shouldLogin() {
        LoginRequest request = new LoginRequest("user", "password123456");
        LoginResponse responseDto = new LoginResponse("token", "refresh", "Bearer", 3600L);

        when(authService.login(request)).thenReturn(responseDto);

        var response = controller.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDto, response.getBody());
        verify(authService).login(request);
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest request = new RegisterRequest(
                "user",
                "user@mail.com",
                "password123",
                Role.BUYER
        );

        RegisterResponse responseDto =
                new RegisterResponse("user", "USER");

        when(authService.register(request)).thenReturn(responseDto);

        var response = controller.register(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(responseDto, response.getBody());

        verify(authService).register(request);
    }
}
