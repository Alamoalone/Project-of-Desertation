package com.notes.keep.controller;

import com.notes.keep.model.Notes;
import com.notes.keep.service.NotesService;
import com.notes.keep.utils.Loggers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notes")
@PreAuthorize("USER")
public class NotesController {

    @Autowired
    private NotesService notesService;

    @PostMapping("/add")
    public ResponseEntity<?> createNote(@RequestBody Notes note) throws Exception {
        Loggers.info("NOTE CREATED");
        System.out.println(note);
        Notes notes = notesService.createNote(note);
        if (notes == null) {
            return ResponseEntity.status(500).header("msg", "SOMETHING WENT WRONG").build();
        }
        return ResponseEntity.ok(notesService.createNote(note));
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllNotes() {
        Loggers.info("ALL NOTE FETCHED");
        List<Notes> notesList = notesService.notesList();
        if(notesList.isEmpty()){
            return ResponseEntity.status(204).header("msg", "NOTES LIST IS EMPTY").build();
        }
        return ResponseEntity.ok(notesList);
    }


    @GetMapping("/getNote/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable Integer id) {
        Loggers.info("NOTE WITH ID " + id + " REQUESTED");
        Notes note = notesService.findByNoteId(id);
        if(note == null){
            return ResponseEntity.status(204).header("msg", "NO NOTE FOUND WITH THIS ID").build();
        }
        return ResponseEntity.ok(notesService.findByNoteId(id));
    }

    @PutMapping("/noteId/{id}")
    public ResponseEntity<?> updateNoteById(@PathVariable Integer id, @RequestBody Notes notes) {
        Loggers.info("NOTE WITH ID " + id + " UPDATED");
        if(notesService.updateNoteById(id, notes) == null){
            return ResponseEntity.status(400).header("msg", "BAD REQUEST").build();
        }
        return ResponseEntity.ok(notesService.updateNoteById(id, notes));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        Loggers.info("NOTE WITH ID " + id + " DELETED");
        notesService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getByTitle/{title}")
    public ResponseEntity<?> findByTitle(@PathVariable String title) {
        Loggers.info("NOTE WITH TITLE " + title + " REQUESTED");
        if (notesService.findByTitle(title) == null) {
            return ResponseEntity.status(204).header("msg", "NO NOTES FOUND WITH THIS TITLE").build();
        }
        return ResponseEntity.ok(notesService.findByTitle(title));
    }

    @GetMapping("/userId/{id}")
    public ResponseEntity<?> getAllByUserId(@PathVariable Integer id) {
        List<Notes> notesList = notesService.findAllByUserUserId(id);
        if (notesList.isEmpty()) {
            return ResponseEntity.status(204).header("msg", "NO NOTES FOUND WITH THIS USER ID").build();
        }
        return ResponseEntity.ok(notesList);
    }

}
