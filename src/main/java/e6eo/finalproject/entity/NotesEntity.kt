package e6eo.finalproject.entity;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "notes")
@Data
@NoArgsConstructor
public class NotesEntity {
    @Id
    @Field(name = "_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Object id;
    @Field(name = "category_id")
    private Object categoryId;
    @Field(name = "type")
    private Object type;
    @Field(name = "status")
    private Object status;
    @Field(name = "start_time")
    private Object startTime;
    @Field(name = "end_time")
    private Object endTime;
    @Field(name = "title")
    private Object title;
    @Field(name = "contents")
    private Object contents;
    @Field(name = "etag")
    private Object etag;
    @Field(name = "have_repost")
    private Object haveRepost;

    @Builder
    public NotesEntity(Object id, Object categoryId, Object type, Object status, Object startTime, Object endTime, Object title, Object contents, Object etag, Object haveRepost) {
        this.id = id;
        this.categoryId = categoryId;
        this.type = type;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.contents = contents;
        this.etag = etag;
        this.haveRepost = haveRepost;
    }

    public NotesEntity eventParser(Map<String, Object> event, String userId, String category) {
        Map<String, String> start = (Map<String, String>) event.get("start");
        Map<String, String> end = (Map<String, String>) event.get("end");
        String kind = event.get("kind").toString().split("#")[1];
        NotesEntity note = NotesEntity.builder()
                .id(event.get("id"))
                .categoryId(category.startsWith("google^") ? userId + "#" + category.replaceAll("_", ".") : userId + "#google^calendar^" + category)
                .type(kind)
                .status(event.get("status"))
                .startTime(start.get("date") != null ? start.get("date") : start.get("dateTime"))
                .endTime(end.get("date") != null ? end.get("date") : end.get("dateTime"))
                .title(event.get("summary"))
                .contents(event.get("description"))
                .etag(event.get("etag"))
                .build();
        return note;
    }

    public NotesEntity taskParser(Map<String, Object> task, String userId, String category) {
        String kind = task.get("kind").toString().split("#")[1];
        NotesEntity note = NotesEntity.builder()
                .id(task.get("id"))
                .categoryId(category.startsWith("google^") ? userId + "#" + category.replaceAll("_", ".") : userId + "#google^tasks^" + category)
                .type(kind)
                .status(task.get("deleted") == null ? task.get("status") : "cancelled")
                .startTime(task.get("due"))
                .endTime(task.get("due"))
                .title(task.get("title"))
                .contents(task.get("notes"))
                .etag(task.get("etag"))
                .build();
        return note;
    }

    public NotesEntity dataParser(Map<String, Object> notedata, String userId) {
        NotesEntity note = NotesEntity.builder()
                .id(notedata.get("id") == null ? new ObjectId().toString() : notedata.get("id"))
                .categoryId(userId + "#" + notedata.get("categoryId"))
                .type(notedata.get("type"))
                .status(notedata.get("status"))
                .startTime(notedata.get("startTime"))
                .endTime(notedata.get("endTime"))
                .title(notedata.get("title"))
                .contents(notedata.get("contents"))
                .etag(notedata.get("etag"))
                .build();
        System.out.println(note);
        return note;
    }
}
