package io.schreib.server;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final TokenStorageService tokenStorage;
    private final MicrosoftGraphService microsoftGraphService;

	public HomeController(final TokenStorageService tokenStorage, final MicrosoftGraphService microsoftGraphService) {
		this.tokenStorage = tokenStorage;
		this.microsoftGraphService = microsoftGraphService;
	}

	@GetMapping("/")
    public String home(final Model model,
			@RegisteredOAuth2AuthorizedClient("azure") final OAuth2AuthorizedClient authorizedClient,
                       @AuthenticationPrincipal final OAuth2User oauth2User) {
        
        // After successful login, store the user ID and refresh token
		try {
			tokenStorage.registerClient(authorizedClient);
		}catch (Exception e) {}
        model.addAttribute("userName", oauth2User.getAttributes().get("name"));
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("hasRefreshToken", tokenStorage.getRefreshToken() != null);

        return "home";
    }
}
