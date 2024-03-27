package e6eo.finalproject.dto;

import e6eo.finalproject.entity.CategoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryMapper extends MongoRepository<CategoryEntity, String> {

    @Query("{ '_id' :  ?0}")
    @Update("{ '$set' : { 'categories.?1' : ?2 }}")
    void addCategory(String id, String key, String value);

    @Query("{ '_id' :  ?0 }")
    @Update("{ '$set' : { 'categories.e6eo' : '?1' }}")
    void insertDefault(String id, String nickname);

    @Query("{ '_id' :  ?0}")
    @Update("{ '$set' : { 'categories.?1' : ?2 }}")
    void updateCategory(String id, String key, String value);
    
    @Query("{ '_id' :  ?0}")
    @Update("{ '$unset' : { 'categories.?1' : 1 }}")
    void deleteCategory(String id, String key);

    @Query("{ '_id' :  ?0}")
    @Update("{ '$set' : { 'categories.?1' : '' }}")
    void deleteCategoryGoogle(String id, String key);
}
