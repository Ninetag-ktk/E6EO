package e6eo.finalproject.entity

import lombok.Data
import lombok.NoArgsConstructor
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field


@Document(collection = "notes")
@Data
@NoArgsConstructor
class NotesEntity(
    @Id
    @Field(name = "_id")
    var id: Any? = null,

    @Field(name = "category_id")
    var categoryId: Any? = null,

    @Field(name = "type")
    var type: Any? = null,

    @Field(name = "status")
    var status: Any? = null,

    @Field(name = "start_time")
    var startTime: Any? = null,

    @Field(name = "end_time")
    var endTime: Any? = null,

    @Field(name = "title")
    var title: Any? = null,

    @Field(name = "contents")
    var contents: Any? = null,

    @Field(name = "etag")
    var etag: Any? = null,

    @Field(name = "have_repost")
    var haveRepost: Any? = null,
) {

    fun eventParser(event: Map<String, Any>, userId: String, category: String): NotesEntity {
        val start = event["start"] as Map<String, String>
        val end = event["end"] as Map<String, String>
        val kind = event["kind"].toString().split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val note = NotesEntity(
            id = event["id"],
            categoryId = if (category.startsWith("google^")) userId + "#" + category.replace(
                "_".toRegex(),
                "."
            ) else "$userId#google^calendar^$category",
            type = kind,
            status = event["status"],
            startTime = if (start["date"] != null) start["date"] else start["dateTime"],
            endTime = if (end["date"] != null) end["date"] else end["dateTime"],
            title = event["summary"],
            contents = event["description"],
            etag = event["etag"]
        )
        return note
    }

    fun taskParser(task: Map<String, Any>, userId: String, category: String): NotesEntity {
        val kind = task["kind"].toString().split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val note = NotesEntity(
            id = task["id"],
            categoryId = if (category.startsWith("google^")) userId + "#" + category.replace(
                "_".toRegex(),
                "."
            ) else "$userId#google^tasks^$category",
            type = kind,
            status = if (task["deleted"] == null) task["status"] else "cancelled",
            startTime = task["due"],
            endTime = task["due"],
            title = task["title"],
            contents = task["notes"],
            etag = task["etag"]
        )
        return note
    }

    fun dataParser(notedata: Map<String, Any>, userId: String): NotesEntity {
        val note = NotesEntity(
            id = if (notedata["id"] == null) ObjectId().toString() else notedata["id"],
            categoryId = "$userId#${notedata["categoryId"]}",
            type = notedata["type"],
            status = notedata["status"],
            startTime = notedata["startTime"],
            endTime = notedata["endTime"],
            title = notedata["title"],
            contents = notedata["contents"],
            etag = notedata["etag"]
        )
        return note
    }
}
