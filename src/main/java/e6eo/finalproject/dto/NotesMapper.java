package e6eo.finalproject.dto;

import e6eo.finalproject.entity.NotesEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotesMapper extends MongoRepository<NotesEntity, String> {

    @Query(value = "{ 'category_id': { '$regex': '^?0.*' } }", delete = true)
    void deleteAllByUserId(String UserId);

    @Query(value = "{ _id : ?0, category_id: {'$regex': '^?1.*'}}", delete = true)
    void deleteByIdWithUserId(String id, String UserId);

    @Query(value = "{ 'category_id' :  { '$regex': '^?0#.*?1$' }}", delete = true)
    void deleteByCategoryId(String UserId, String categoryId);

    @Query(value = "{ 'category_id' : { '$regex': '^?0' }}")
    List<NotesEntity> findByUserId(String userId);

    @Query(value = "{ 'category_id' :  { '$regex': '^?0#.*?3$' }, " +
            "$and : [{$or: [{'start_time': { $gte: ?1, $lte: ?2}},{ 'end_time': { $gte: ?1, $lte: ?2 }}]}]}")
    List<NotesEntity> getNotes(String userId, String startTime, String endTime, String categoryIds);

}
