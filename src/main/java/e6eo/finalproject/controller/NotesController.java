package e6eo.finalproject.controller;

import e6eo.finalproject.dao.NotesDAO;
import e6eo.finalproject.entity.NoteData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    private NotesDAO notesDAO;

    @PostMapping("")
    public ResponseEntity<?> getNotes(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(notesDAO.notesGet(request));
    }

    @PostMapping("/note") // 노트 인서트
    public ResponseEntity<?> noteInsert(@RequestBody NoteData noteData) {
        try {
            notesDAO.insertNote(noteData);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @PatchMapping("/note") // 노트 수정
    public ResponseEntity<?> notePatch(@RequestBody NoteData noteData) {
        try {
            notesDAO.insertNote(noteData);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @DeleteMapping("/note") // 노트 삭제
    public ResponseEntity<?> noteDelete(@RequestBody NoteData noteData) {
        try {
            notesDAO.deleteNote(noteData);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }
}
