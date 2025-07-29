package dev.ua.ikeepcalm.policeCatwalk.api;

import dev.ua.ikeepcalm.policeCatwalk.PoliceCatwalk;
import dev.ua.ikeepcalm.policeCatwalk.dto.PlayerTeleportRequest;
import dev.ua.ikeepcalm.policeCatwalk.dto.TeleportRequest;
import dev.ua.ikeepcalm.policeCatwalk.dto.TeleportResponse;
import dev.ua.ikeepcalm.policeCatwalk.manager.TeleportManager;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeEventHandler;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeQueryParam;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeRequestBody;
import dev.ua.uaproject.catwalk.bridge.source.BridgeApiResponse;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

import java.util.concurrent.CompletableFuture;

public class PoliceTeleportEndpoint {

    private final TeleportManager teleportManager;

    public PoliceTeleportEndpoint(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @OpenApi(
            path = "/police/request/coordinates",
            methods = HttpMethod.POST,
            summary = "Request police teleport",
            description = "Initiates a teleport request for a police officer with notification period",
            tags = {"Police Teleport"},
            requestBody = @OpenApiRequestBody(
                    description = "Teleport request details",
                    required = true,
                    content = @OpenApiContent(
                            from = TeleportRequest.class,
                            mimeType = "application/json",
                            example = """
                                    {
                                      "playerName": "ikeepcalm",
                                      "targetX": 100.5,
                                      "targetY": 64.0,
                                      "targetZ": -200.3,
                                      "targetWorld": "world",
                                      "reason": "Traffic violation",
                                      "requesterDiscordId": "123456789",
                                      "requesterDiscordName": "Officer#1234"
                                    }
                                    """
                    )
            ),
            responses = {
                    @OpenApiResponse(status = "200", description = "Teleport request initiated successfully",
                            content = @OpenApiContent(
                                    from = TeleportResponse.class,
                                    mimeType = "application/json")),
                    @OpenApiResponse(status = "400", description = "Invalid request or player not found"),
                    @OpenApiResponse(status = "429", description = "Request cooldown active"),
                    @OpenApiResponse(status = "500", description = "Internal server error")
            }
    )
    @BridgeEventHandler(description = "Initiates police teleport request", logRequests = true, scopes = {"police"})
    public CompletableFuture<BridgeApiResponse<TeleportResponse>> requestTeleport(@BridgeRequestBody TeleportRequest request) {
        try {
            if (request == null || request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Player name is required", HttpStatus.BAD_REQUEST)
                );
            }

            if (request.getTargetWorld() == null || request.getTargetWorld().trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Target world is required", HttpStatus.BAD_REQUEST)
                );
            }

            TeleportResponse response = teleportManager.processTeleportRequest(request);

            if (!response.isSuccess()) {
                HttpStatus status = response.getMessage().contains("cooldown") ?
                        HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_REQUEST;
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error(response.getMessage(), status)
                );
            }

            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to process teleport request", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to process teleport request", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/police/return",
            methods = HttpMethod.POST,
            summary = "Return police officer",
            description = "Returns a teleported police officer to their original location",
            tags = {"Police Teleport"},
            queryParams = {
                    @OpenApiParam(
                            name = "player",
                            type = String.class,
                            description = "Player name to return",
                            example = "ikeepcalm",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Player returned successfully",
                            content = @OpenApiContent(
                                    from = TeleportResponse.class,
                                    mimeType = "application/json")),
                    @OpenApiResponse(status = "400", description = "Player not found or not teleported"),
                    @OpenApiResponse(status = "500", description = "Internal server error")
            }
    )
    @BridgeEventHandler(description = "Returns police officer to original location", logRequests = true, scopes = {"police"})
    public CompletableFuture<BridgeApiResponse<TeleportResponse>> returnPlayer(@BridgeQueryParam("player") String playerName) {
        try {
            if (playerName == null || playerName.trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Player name is required", HttpStatus.BAD_REQUEST)
                );
            }

            TeleportResponse response = teleportManager.returnPlayer(playerName.trim());

            if (!response.isSuccess()) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error(response.getMessage(), HttpStatus.BAD_REQUEST)
                );
            }

            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to return player", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to return player", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/police/request/player",
            methods = HttpMethod.POST,
            summary = "Request police teleport to player",
            description = "Initiates a teleport request for a police officer to teleport to another player",
            tags = {"Police Teleport"},
            requestBody = @OpenApiRequestBody(
                    description = "Player teleport request details",
                    required = true,
                    content = @OpenApiContent(
                            from = PlayerTeleportRequest.class,
                            mimeType = "application/json",
                            example = """
                                    {
                                      "playerName": "ikeepcalm",
                                      "targetPlayerName": "suspect123",
                                      "reason": "Investigation required",
                                      "requesterDiscordId": "123456789",
                                      "requesterDiscordName": "Officer#1234"
                                    }
                                    """
                    )
            ),
            responses = {
                    @OpenApiResponse(status = "200", description = "Teleport request initiated successfully",
                            content = @OpenApiContent(
                                    from = TeleportResponse.class,
                                    mimeType = "application/json")),
                    @OpenApiResponse(status = "400", description = "Invalid request or player not found"),
                    @OpenApiResponse(status = "429", description = "Request cooldown active"),
                    @OpenApiResponse(status = "500", description = "Internal server error")
            }
    )
    @BridgeEventHandler(description = "Initiates police teleport request to player", logRequests = true, scopes = {"police"})
    public CompletableFuture<BridgeApiResponse<TeleportResponse>> requestTeleportToPlayer(@BridgeRequestBody PlayerTeleportRequest request) {
        try {
            if (request == null || request.getPlayerName() == null || request.getPlayerName().trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Player name is required", HttpStatus.BAD_REQUEST)
                );
            }

            if (request.getTargetPlayerName() == null || request.getTargetPlayerName().trim().isEmpty()) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Target player name is required", HttpStatus.BAD_REQUEST)
                );
            }

            TeleportResponse response = teleportManager.processTeleportToPlayerRequest(request);

            if (!response.isSuccess()) {
                HttpStatus status = response.getMessage().contains("cooldown") ?
                        HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_REQUEST;
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error(response.getMessage(), status)
                );
            }

            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to process teleport to player request", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to process teleport to player request", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    private void logError(String message, Throwable e) {
        PoliceCatwalk.error(message);
        if (e != null) {
            PoliceCatwalk.error(e.getMessage());
            e.printStackTrace();
        }
    }
}