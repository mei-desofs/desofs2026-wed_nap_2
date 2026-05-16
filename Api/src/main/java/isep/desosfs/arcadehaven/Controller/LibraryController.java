package isep.desosfs.arcadehaven.Controller;

import isep.desosfs.arcadehaven.Dto.Request.ImportKeyRequest;
import isep.desosfs.arcadehaven.Dto.Response.LibraryResponse;
import isep.desosfs.arcadehaven.Service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<LibraryResponse> getMyLibrary() {
        return ResponseEntity.ok(libraryService.getMyLibrary());
    }

    @PostMapping("/import-key")
    public ResponseEntity<LibraryResponse> importGameKey(@Valid @RequestBody ImportKeyRequest request) {
        return ResponseEntity.ok(libraryService.importGameKey(request));
    }
}
