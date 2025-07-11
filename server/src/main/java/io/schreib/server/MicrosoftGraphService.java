package io.schreib.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.drives.item.DriveItemRequestBuilder;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Subscription;
import com.microsoft.graph.models.odataerrors.ODataError;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;
import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MicrosoftGraphService {
	private final static Logger log = LoggerFactory.getLogger(MicrosoftGraphService.class);

	private final OAuth2AuthorizedClientService authorizedClientService;
	@Value("${spring.security.oauth2.client.registration.azure.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.azure.client-secret}")
	private String clientSecret;

	@Value("${spring.security.oauth2.client.provider.azure.token-uri}")
	private String tokenEndpoint;

	@Value("${spring.security.oauth2.client.registration.azure.scope}")
	private String scopes;

	@Value("${local.download-path}")
	private String downloadPath;

	@Value("${onedrive.folder-path}")
	private String folderPath;

	private final HashMap<String, DriveFile> driveItems;

	private final TokenStorageService tokenStorage;
	private final ObjectMapper objectMapper;

	public MicrosoftGraphService(final TokenStorageService tokenStorage,
			final OAuth2AuthorizedClientService authorizedClientService,
			@Value("${local.download-path}") final String downloadPath, final ObjectMapper objectMapper) {
		this.tokenStorage = tokenStorage;
		this.authorizedClientService = authorizedClientService;
		this.downloadPath = downloadPath;
		this.objectMapper = objectMapper;
		if (Files.exists(Paths.get(downloadPath).resolve("items.json"))) {
			HashMap<String, DriveFile> temp = new HashMap<>();
			final HashMap<String, DriveFile> input;
			try {
				input = objectMapper.readValue(Files.newInputStream(Paths.get(downloadPath).resolve("items.json")),
						new TypeReference<>() {
						});
				temp = input;
			} catch (IOException e) {
				log.error("Error while reading items.json", e);
			}
			driveItems = temp;
		} else {
			driveItems = new HashMap<>();
		}
	}

	@Scheduled(fixedRate = 10000)
	private void refreshHandballtagFolder() {
		List<DriveFile> driveFiles = listChildren();
		final Path path = Path.of(folderPath);
		for (final Path file : path) {
			final var dFile = driveFiles.stream()
					.filter(driveFile -> driveFile.name().equals(file.toString()))
					.findFirst();
			try {
				if (dFile.isPresent()) {
					driveFiles = listChildren(dFile.get());
				}
			} catch (final Exception e) {
				log.error("Error while listing children for file: {}", file, e);
			}
		}
		driveFiles.forEach(driveFile -> {
			try {
				downloadFile(driveFile, Paths.get(downloadPath).resolve(driveFile.path().replaceFirst("^/", "")));
			} catch (final Exception e) {
				log.error("Error while downloading file: {}", driveFile, e);
			}
		});
		log.info("");
	}

	private GraphServiceClient getGraphClient() {
		if (tokenStorage.getRefreshToken() == null) {
			throw new IllegalStateException("Refresh token is not available. Please login first.");
		}

		final AuthenticationProvider authProvider = new BaseBearerTokenAuthenticationProvider(
				new AccessTokenProvider() {
					@NotNull
					@Override
					public String getAuthorizationToken(@NotNull final URI uri,
							@Nullable final Map<String, Object> additionalAuthenticationContext) {
						return tokenStorage.getBearerToken();
					}

					@NotNull
					@Override
					public AllowedHostsValidator getAllowedHostsValidator() {
						return null;
					}
				});

		return new GraphServiceClient(authProvider);
	}

	private DriveItemRequestBuilder drive() {
		return getGraphClient().drives().byDriveId("me");
	}

	record DriveFile(String path, String id, String name, String webUrl, boolean isFolder, String etag) {
		DriveFile {
			if (id == null || name == null || webUrl == null || etag == null) {
				throw new IllegalArgumentException("DriveItem fields cannot be null");
			}
		}
	}

	public void downloadFile(final DriveFile driveFile, final Path downloadPath) {
		if (driveItems.containsKey(driveFile.id()) && Objects.equals(driveItems.get(driveFile.id()).etag(),
				driveFile.etag())) {
			log.info("File {} already downloaded, skipping download.", driveFile.name());
			return;

		}
		final var downloadDir = downloadPath.getParent();
		if (!Files.exists(downloadPath)) {
			try {
				Files.createDirectories(downloadDir);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			final InputStream inputStream = drive().items().byDriveItemId(driveFile.id()).content().get();
			Files.copy(inputStream, downloadPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException | ODataError e) {
			log.error("Could not download file {}", driveFile, e);
		}
		addDriveItemToCache(driveFile);
	}

	private void addDriveItemToCache(final DriveFile driveFile) {
		driveItems.put(driveFile.id(), driveFile);
		try {
			objectMapper.writeValue(Files.newOutputStream(Path.of(downloadPath).resolve("items.json")), driveItems);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DriveFile> listChildren() {
		final var root = drive().root().get();
		final var rootItem = mapToDriveItem(root, null);
		final var children = drive().items()
				.byDriveItemId(rootItem.id())
				.children()
				.get()
				.getValue()
				.stream()
				.map(item -> mapToDriveItem(item, rootItem))
				.toList();
		log.info("Root Item: {}", rootItem);
		return children;
	}

	private DriveFile mapToDriveItem(final DriveItem item, final DriveFile parent) {
		final String path;
		if (parent == null) {
			path = "/";
		} else {
			path = parent.path + item.getName() + (item.getFolder() == null ? "" : "/");
		}
		return new DriveFile(path, item.getId(), item.getName(), item.getWebUrl(), item.getFolder() != null,
				item.getETag());
	}

	public List<DriveFile> listChildren(final DriveFile item) {
		if (!item.isFolder()) {
			return List.of();
		}
		final var children = drive().items()
				.byDriveItemId(item.id())
				.children()
				.get()
				.getValue()
				.stream()
				.map(child -> mapToDriveItem(child, item))
				.toList();
		log.info("Root Item: {}", children);
		return children;
	}

	public Subscription createOneDriveSubscription(final String notificationUrl) {
		final Subscription subscription = new Subscription();
		subscription.setChangeType("updated");
		subscription.setNotificationUrl(notificationUrl);
		subscription.setResource("/me/drive/root");
		subscription.setExpirationDateTime(OffsetDateTime.now().plusHours(1));
		subscription.setClientState("customClientState");

		return getGraphClient().subscriptions().post(subscription);
	}

}
