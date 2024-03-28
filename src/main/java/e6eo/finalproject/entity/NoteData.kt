package e6eo.finalproject.entity

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class NoteData(
    var observe: String? = null,
    var categoryId: String? = null,
    var note: Map<String, Any>? = null
)
