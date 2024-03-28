package e6eo.finalproject.dao

import e6eo.finalproject.entity.NoteData
import e6eo.finalproject.entity.NotesEntity
import e6eo.finalproject.entity.UsersEntity
import e6eo.finalproject.entityGoogle.googleLists
import lombok.RequiredArgsConstructor
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class NotesDAO : GoogleAPI() {
    // 전달받은 월 데이터 기준, 앞뒤를 포함해 3개월치의 데이터를 체크
    fun checkGoogleNotes(observe: String?, accessToken: String, date: String) {
        val user = usersMapper.findByObserveToken(observe)!!
        val notes = notesMapper.findByUserId(user.userId!!!!)!!
        val notesEtag: Map<Any, Any?> = notes.stream().collect(
            Collectors.toMap({ obj: NotesEntity -> obj.id },
                { obj: NotesEntity -> obj.etag })
        )
        val category = categoryMapper.findById(user.userId!!!!)
        var categories: Map<String, ArrayList<String>> = HashMap()
        if (!(category.isEmpty)) {
            categories = decodeCategory(category.get().categories!!.keys.toTypedArray<String>())
        } else {
//            System.out.println("카테고리 비어있음");
        }
        scanCalendarNotes(notesEtag, noteCalendar(user.userId!!!!, categories["calendar"]!!, accessToken, date))
        scanTasksNotes(notesEtag, noteTasks(user.userId!!!!, categories["tasks"]!!, accessToken, date))
    }

    private fun scanCalendarNotes(notesEtag: Map<Any, Any?>, noteCalendar: ArrayList<NotesEntity>) {
        var i = 0
        for (note in noteCalendar) {
//            System.out.println(note);
            if (note.status.toString() == "cancelled" && notesEtag[note.id] != null) {
//                System.out.println(note);
                notesMapper.delete(note)
                i++
            } else if (note.status.toString() != "cancelled" && note.etag != notesEtag[note.id]) {
//                System.out.println(note);
                notesMapper.save(note)
                i++
            }
        }
        //        System.out.println("캘린더 event 업데이트 개수 : " + i);
    }

    private fun scanTasksNotes(notesEtag: Map<Any, Any?>, noteTasks: ArrayList<NotesEntity>) {
        var i = 0
        for (note in noteTasks) {
//            System.out.println(note);
            if (note.status.toString() == "cancelled" && notesEtag[note.id] != null) {
//                System.out.println(note);
                notesMapper.delete(note)
                i++
            } else if (note.status.toString() != "cancelled" && note.etag != notesEtag[note.id]) {
//                System.out.println(note);
                notesMapper.save(note)
                i++
            }
        }
        //        System.out.println("태스트 task 업데이트 개수 : " + i);
    }

    // 데이터 최초로 가져오기
    fun getGoogleNotes(user: UsersEntity, accessToken: String) {
        val categories = categoryMapper.findById(user.userId!!).get().categories!!.keys.toTypedArray<String>()
        val categoryMap = decodeCategory(categories)
        notesMapper.saveAll(noteCalendar(user.userId!!, categoryMap["calendar"]!!, accessToken))
        notesMapper.saveAll(noteTasks(user.userId!!, categoryMap["tasks"]!!, accessToken))
        println("GetNotesFromGoogle_Complete")
    }

    private fun noteCalendar(userId: String, list: ArrayList<String>, accessToken: String): ArrayList<NotesEntity> {
        val notes = ArrayList<NotesEntity>()
        val webClient = WebClient.builder()
            .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
            .baseUrl("https://www.googleapis.com/calendar/v3/calendars/")
            .defaultHeaders(reqHeader(accessToken))
            .build()
        val dateTime = calcDateTime()
        // date 가 없으면 showDeleted = false
        val requestUrl: String = ("/events?orderBy=updated&showDeleted=false&singleEvents=true"
                + "&timeMin=" + dateTime["start"]
                + "&timeMax=" + dateTime["end"]
                + "&timeZone=GMT+9" //                + "&updatedMin=" + dateTime.get("update")
                + "&key=" + googleKey)
        for (calendar in list) {
            val json = webClient.get().uri(calendar + requestUrl)
                .retrieve().bodyToMono(googleLists::class.java).block()!!.items
            for (event in json as ArrayList<Map<String, Any>>) {
                val note = NotesEntity().eventParser(event, userId, calendar)
                notes.add(note)
            }
        }
        return notes
    }

    private fun noteCalendar(
        userId: String,
        list: ArrayList<String>,
        accessToken: String,
        date: String
    ): ArrayList<NotesEntity> {
        val notes = ArrayList<NotesEntity>()
        val webClient = WebClient.builder()
            .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
            .baseUrl("https://www.googleapis.com/calendar/v3/calendars/")
            .defaultHeaders(reqHeader(accessToken))
            .build()
        val dateTime = calcDateTime(date)
        val requestUrl = ("/events?orderBy=updated"
                + "&showDeleted=true&singleEvents=true"
                + "&timeMin=" + dateTime["start"]
                + "&timeMax=" + dateTime["end"]
                + "&timeZone=GMT+9&key=" + googleKey)
        for (calendar in list) {
            val json = webClient.get().uri(calendar + requestUrl)
                .retrieve().bodyToMono(googleLists::class.java).block()!!.items
            for (event in json as ArrayList<Map<String, Any>>) {
                val note = NotesEntity().eventParser(event, userId, calendar)
                notes.add(note)
            }
        }
        return notes
    }

    private fun noteTasks(userId: String, list: ArrayList<String>, accessToken: String): ArrayList<NotesEntity> {
        val notes = ArrayList<NotesEntity>()
        val webClient = WebClient.builder()
            .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
            .baseUrl("https://tasks.googleapis.com/tasks/v1/lists/")
            .defaultHeaders(reqHeader(accessToken))
            .build()
        val dateTime = calcDateTime()
        // date 가 없으면 showDeleted = false
        val requestUrl: String = ("/tasks"
                + "?dueMax=" + dateTime["end"]
                + "&dueMin=" + dateTime["start"]
                + "&showCompleted=true&showDeleted=false&showHidden=true" //                + "&updatedMin=" + dateTime.get("update")
                + "&key" + googleKey)
        for (tasklist in list) {
            val json = webClient.get().uri(tasklist + requestUrl)
                .retrieve().bodyToMono(googleLists::class.java).block()!!.items
            for (task in json as ArrayList<Map<String, Any>>) {
                val note = NotesEntity().taskParser(task, userId, tasklist)
                notes.add(note)
            }
        }
        return notes
    }

    private fun noteTasks(
        userId: String,
        list: ArrayList<String>,
        accessToken: String,
        date: String
    ): ArrayList<NotesEntity> {
        val notes = ArrayList<NotesEntity>()
        val webClient = WebClient.builder()
            .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
            .baseUrl("https://tasks.googleapis.com/tasks/v1/lists/")
            .defaultHeaders(reqHeader(accessToken))
            .build()
        val dateTime = calcDateTime(date)
        val requestUrl = ("/tasks"
                + "?dueMax=" + dateTime["end"]
                + "&dueMin=" + dateTime["start"]
                + "&showCompleted=true&showDeleted=true&showHidden=true"
                + "&key" + googleKey)
        for (tasklist in list) {
            val json = webClient.get().uri(tasklist + requestUrl)
                .retrieve().bodyToMono(googleLists::class.java).block()!!.items
            for (task in json as ArrayList<Map<String, Any>>) {
                val note = NotesEntity().taskParser(task, userId, tasklist)
                notes.add(note)
            }
        }
        return notes
    }

    fun notesGet(request: Map<String, Any>): List<NotesEntity> {
        val user = usersMapper.findByObserveToken(request["observe"].toString())!!
        //        System.out.println(user);
        val dateRange = calcDateTime(request["date"].toString())
        val categoryData = request["categoryData"] as List<List<Any>>
        val categoryIds = ArrayList<String>()
        for (category in categoryData) {
            if (category[2] as Boolean) {
                if (category[0].toString().contains("google")) {
                    val categoryCode =
                        category[0].toString().split("\\^".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    categoryIds.add(categoryCode[categoryCode.size - 1].replace("_".toRegex(), "."))
                } else {
                    categoryIds.add(category[0].toString())
                }
            }
        }
        //        System.out.println(categoryIds);
        val notesList: MutableList<NotesEntity> = ArrayList()
        for (categoryId in categoryIds) {
            notesList.addAll(
                notesMapper.getNotes(
                    user.userId!!,
                    dateRange["start"]!!,
                    dateRange["end"]!!,
                    categoryId
                )!!
            )
        }
        //        System.out.println(notesList);
        return notesList
    }

    fun insertNote(noteData: NoteData) {
        val user = usersMapper.findByObserveToken(noteData.observe)!!
        val note: NotesEntity =
            if (noteData.categoryId != null) {
                when (noteData.note!!["kind"].toString().split("#".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1]) {
                    "event" -> NotesEntity().eventParser(noteData.note!!, user.userId!!, noteData.categoryId!!)
                    "task" -> NotesEntity().taskParser(noteData.note!!, user.userId!!, noteData.categoryId!!)
                    else -> return println("에러")
                }
            } else {
                NotesEntity().dataParser(noteData.note!!, user.userId!!)
            }
        notesMapper.save(note)
//        println("체크 : $note")
    }

    fun deleteNote(noteData: NoteData) {
        val user = usersMapper.findByObserveToken(noteData.observe)!!
        // 유저 아이디를 기준으로 모든 노트 삭제
        notesMapper.deleteByIdWithUserId(noteData.note!!["id"].toString(), user.userId!!)
    }
}
