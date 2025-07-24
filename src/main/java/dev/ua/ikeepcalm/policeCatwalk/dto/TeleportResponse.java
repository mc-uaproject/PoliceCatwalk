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
        description = "Police teleport operation response",
        properties = {
                @ApiProperty(
                        name = "success",
                        type = "boolean",
                        description = "Whether the operation was successful",
                        required = true,
                        example = "true"
                ),
                @ApiProperty(
                        name = "message",
                        type = "string",
                        description = "Response message",
                        required = true,
                        example = "Teleport request initiated successfully"
                ),
                @ApiProperty(
                        name = "playerName",
                        type = "string",
                        description = "Name of the affected player",
                        required = true,
                        example = "ikeepcalm"
                ),
                @ApiProperty(
                        name = "timestamp",
                        type = "long",
                        description = "Unix timestamp of the operation",
                        required = true,
                        example = "1704067200000"
                ),
                @ApiProperty(
                        name = "teleportId",
                        type = "string",
                        description = "Unique identifier for the teleport operation",
                        required = false,
                        example = "tp_ikeepcalm_1704067200"
                )
        }
)
public class TeleportResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("playerName")
    private String playerName;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("teleportId")
    private String teleportId;
}