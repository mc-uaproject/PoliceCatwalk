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
        description = "Police teleport request data",
        properties = {
                @ApiProperty(
                        name = "playerName",
                        type = "string",
                        description = "Name of the police officer to teleport",
                        required = true,
                        example = "ikeepcalm"
                ),
                @ApiProperty(
                        name = "targetX",
                        type = "double",
                        description = "Target X coordinate",
                        required = true,
                        example = "100.5"
                ),
                @ApiProperty(
                        name = "targetY",
                        type = "double",
                        description = "Target Y coordinate",
                        required = true,
                        example = "64.0"
                ),
                @ApiProperty(
                        name = "targetZ",
                        type = "double",
                        description = "Target Z coordinate",
                        required = true,
                        example = "-200.3"
                ),
                @ApiProperty(
                        name = "targetWorld",
                        type = "string",
                        description = "Target world name",
                        required = true,
                        example = "world"
                ),
                @ApiProperty(
                        name = "reason",
                        type = "string",
                        description = "Reason for teleportation",
                        required = false,
                        example = "Traffic violation"
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
public class TeleportRequest {
    
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("targetX")
    private Double targetX;
    
    @JsonProperty("targetY")
    private Double targetY;
    
    @JsonProperty("targetZ")
    private Double targetZ;
    
    @JsonProperty("targetWorld")
    private String targetWorld;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("requesterDiscordId")
    private String requesterDiscordId;
    
    @JsonProperty("requesterDiscordName")
    private String requesterDiscordName;
}