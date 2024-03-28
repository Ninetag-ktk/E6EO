package e6eo.finalproject.dto

import e6eo.finalproject.entity.NotesEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NotesMapper : MongoRepository<NotesEntity, String> {
    @Query(value = "{ 'category_id': { '\$regex': '^?0.*' } }", delete = true)
    fun deleteAllByUserId(UserId: String)

    @Query(value = "{ _id : ?0, category_id: {'\$regex': '^?1.*'}}", delete = true)
    fun deleteByIdWithUserId(id: String, UserId: String)

    @Query(value = "{ 'category_id' :  { '\$regex': '^?0#.*?1$' }}", delete = true)
    fun deleteByCategoryId(UserId: String, categoryId: String)

    @Query(value = "{ 'category_id' : { '\$regex': '^?0' }}")
    fun findByUserId(userId: String): List<NotesEntity>?

    @Query(
        value = "{ 'category_id' :  { '\$regex': '^?0#.*?3$' }, " +
                "\$and : [{\$or: [{'start_time': { \$gte: ?1, \$lte: ?2}},{ 'end_time': { \$gte: ?1, \$lte: ?2 }}]}]}"
    )
    fun getNotes(userId: String, startTime: String, endTime: String, categoryIds: String): List<NotesEntity>?
}
