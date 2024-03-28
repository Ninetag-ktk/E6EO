package e6eo.finalproject.entity

import lombok.Data
import lombok.NoArgsConstructor
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "category")
@Data
@NoArgsConstructor
class CategoryEntity(
    @Field(name = "_id")
    var userId: String,
    @Field(name = "categories")
    var categories: Map<String, String>? = null // 문자열 구분은 공백없이 반점(,)만 사용할 것
) {
    fun CategoryEntity() {
    }
}