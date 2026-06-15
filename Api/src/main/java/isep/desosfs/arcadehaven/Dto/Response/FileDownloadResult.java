package isep.desosfs.arcadehaven.Dto.Response;

public record FileDownloadResult(
        String filename,
        byte[] data
) {}
