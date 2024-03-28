package e6eo.finalproject.entityGoogle

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import java.math.BigInteger

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class googleUserInfo(
        var id: BigInteger,
        var email: String,
        var verified_email: Boolean,
        var name: String,
        var given_name: String,
        var family_name: String,
        var picture: String,
        var locale: String
)
