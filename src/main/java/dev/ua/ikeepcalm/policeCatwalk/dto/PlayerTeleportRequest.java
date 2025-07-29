package dev.ua.ikeepcalm.policeCatwalk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiProperty;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiSchema;
import io.javalin.openapi.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSchema
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiSchema(
        description = "Police player-to-player teleport request data",
        properties = {
                @ApiProperty(
                        name = "playerName",
                        type = "string",
                        description = "Name of the police officer to teleport",
                        required = true,
                        example = "ikeepcalm"
                ),
                @ApiProperty(
                        name = "targetPlayerName",
                        type = "string",
                        description = "Name of the target player to teleport to",
                        required = true,
                        example = "suspect123"
                ),
                @ApiProperty(
                        name = "reason",
                        type = "string",
                        description = "Reason for teleportation",
                        required = false,
                        example = "Investigation required"
                ),
                @ApiProperty(
                        name = "requesterDiscordId",
                        type = "string",
                        description = "Discord ID of the requester",
                        required = false,
                        example = "123456789"
                ),
                @ApiProperty(
                        name = "requesterDiscordName",
                        type = "string",
                        description = "Discord name of the requester",
                        required = false,
                        example = "Officer#1234"
                )
        }
)
public class PlayerTeleportRequest {
    
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("targetPlayerName")
    private String targetPlayerName;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("requesterDiscordId")
    private String requesterDiscordId;
    
    @JsonProperty("requesterDiscordName")
    private String requesterDiscordName;
}