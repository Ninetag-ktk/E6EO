package e6eo.finalproject.dto

import e6eo.finalproject.entity.CategoryEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update
import org.springframework.stereotype.Repository

@Repository
interface CategoryMapper : MongoRepository<CategoryEntity, String> {
    @Query("{ '_id' :  ?0}")
    @Update("{ '\$set' : { 'categories.?1' : ?2 }}")
    fun addCategory(id: String, key: String, value: String)

    @Query("{ '_id' :  ?0 }")
    @Update("{ '\$set' : { 'categories.e6eo' : '?1' }}")
    fun saveDefault(id: String, nickname: String)

    @Query("{ '_id' :  ?0}")
    @Update("{ '\$set' : { 'categories.1' : ?2 }}")
    fun updateCategory(id: String, key: String, value: String)

    @Query("{ '_id' :  ?0}")
    @Update("{ '\$unset' : { 'categories.?1' : 1 }}")
    fun deleteCategory(id: String, key: String)

    @Query("{ '_id' :  ?0}")
    @Update("{ '\$set' : { 'categories.?1' : '' }}")
    fun deleteCategoryGoogle(id: String, key: String)
}
