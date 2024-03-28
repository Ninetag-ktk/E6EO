package e6eo.finalproject.controller

import e6eo.finalproject.dao.NotesDAO
import e6eo.finalproject.entity.NoteData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notes")
class NotesController {
    @Autowired
    private lateinit var notesDAO: NotesDAO

    @PostMapping("") // 노트 리스트를 프론트로 반환
    fun getNotes(@RequestBody request: Map<String, Any>): ResponseEntity<*> {
        return ResponseEntity.ok(notesDAO.notesGet(request))
    }

    @PostMapping("/note") // 노트 인서트
    fun noteInsert(@RequestBody noteData: NoteData): ResponseEntity<*> {
        try {
            notesDAO.insertNote(noteData)
            return ResponseEntity.ok(true)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e)
        }
    }

    @PatchMapping("/note") // 노트 수정
    fun notePatch(@RequestBody noteData: NoteData): ResponseEntity<*> {
        try {
            notesDAO.insertNote(noteData)
            return ResponseEntity.ok(true)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e)
        }
    }

    @DeleteMapping("/note") // 노트 삭제
    fun noteDelete(@RequestBody noteData: NoteData): ResponseEntity<*> {
        try {
            notesDAO.deleteNote(noteData)
            return ResponseEntity.ok(true)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e)
        }
    }
}
