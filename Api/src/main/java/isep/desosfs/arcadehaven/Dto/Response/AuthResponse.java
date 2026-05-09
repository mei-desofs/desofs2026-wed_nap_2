package isep.desosfs.arcadehaven.Dto.Response;

public record AuthResponse(
        String token,
        String username,
        String role
) {}
