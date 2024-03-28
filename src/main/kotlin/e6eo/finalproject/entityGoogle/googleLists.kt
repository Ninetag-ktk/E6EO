package e6eo.finalproject.entityGoogle

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class googleLists(
        var kind: String?,
        var etag: String?,
        var items: Any?
)