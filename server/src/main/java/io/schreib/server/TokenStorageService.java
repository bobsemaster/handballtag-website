package io.schreib.server;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TokenStorageService {
	public static final String TMP_TOKENS_DAT = "/tmp/tokens.dat";
	private OAuth2AuthorizedClient client;

	public TokenStorageService() {
		if (Files.exists(Path.of(TMP_TOKENS_DAT))) {
			try (final var input = new ObjectInputStream(Files.newInputStream(Path.of(TMP_TOKENS_DAT)))) {
				this.client = (OAuth2AuthorizedClient) input.readObject();
			} catch (final IOException | ClassNotFoundException e) {
			}
		}
	}

	public void registerClient(final OAuth2AuthorizedClient client) {
		this.client = client;
		try (final var output = new ObjectOutputStream(Files.newOutputStream(Path.of(TMP_TOKENS_DAT)))) {
			output.writeObject(client);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getRefreshToken() {
		return client.getRefreshToken().getTokenValue();
	}

	public String getBearerToken() {
		return client.getAccessToken().getTokenValue();
	}
}
