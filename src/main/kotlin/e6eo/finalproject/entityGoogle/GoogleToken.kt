package e6eo.finalproject.entityGoogle

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class GoogleToken(
    var access_token: String?,
    var expires_in: Int?,
    var refresh_token: String?,
    var scope: String?,
    var token_type: String?,
    var id_token: String?
)
