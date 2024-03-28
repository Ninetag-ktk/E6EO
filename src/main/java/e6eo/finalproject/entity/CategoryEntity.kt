package e6eo.finalproject.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "category")
@Data
@NoArgsConstructor
public class CategoryEntity {
    @Id
    @Field(name = "_id")
    private String userId;
    @Field(name = "categories")
    private Map<String, String> categories; // 문자열 구분은 공백없이 반점(,)만 사용할 것

    @Builder
    public CategoryEntity(String userId) {
        this.userId = userId;
    }

}
