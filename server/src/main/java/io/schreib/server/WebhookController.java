package io.schreib.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class WebhookController {

    private final MicrosoftGraphService graphService;

	public WebhookController(final MicrosoftGraphService graphService) {
		this.graphService = graphService;
	}

	@PostMapping("/webhook/onedrive")
    public ResponseEntity<String> handleOneDriveNotification(
        @RequestBody final JsonNode notification,
        @RequestParam(required = false) final String validationToken) {
        
        // Handle the initial webhook validation from Microsoft Graph
        if (validationToken != null) {
            System.out.println("Validation token received: " + validationToken);
            return ResponseEntity.ok(validationToken);
        }

        // Process the actual file change notification
        final JsonNode resource = notification.get("value").get(0).get("resource");
        final String itemId = resource.get("id").asText();
        System.out.println("Change notification received for item: " + itemId);



        return ResponseEntity.ok("Accepted");
    }
}
