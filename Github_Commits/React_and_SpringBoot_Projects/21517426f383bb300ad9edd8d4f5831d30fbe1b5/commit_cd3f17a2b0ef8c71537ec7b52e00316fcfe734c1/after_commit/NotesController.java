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
        Notes notes = notesService.createNote(note);
        if (notes == null) {
            return ResponseEntity.status(200).header("msg", "FIELDS ARE EMPTY").build();
        }
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllNotes() throws NullPointerException {
        Loggers.info("ALL NOTE FETCHED");
        List<Notes> notesList = notesService.notesList();
        if(notesList.isEmpty()){
            return ResponseEntity.noContent().header("msg", "NO NOTE FOUND").build();
        }
        return ResponseEntity.ok(notesList);
    }


    @GetMapping("/getNote/{id}")
    public ResponseEntity<?> getNoteById(@PathVariable Integer id) {
        Loggers.info("NOTE WITH ID " + id + " REQUESTED");
        Notes note = notesService.findByNoteId(id);
        if(note == null){
            return ResponseEntity.noContent().header("msg", "NO NOTE FOUND WITH THIS ID").build();
        }
        return ResponseEntity.ok(note);
    }

    @PutMapping("/noteId/{id}")
    public ResponseEntity<?> updateNoteById(@PathVariable Integer id, @RequestBody Notes notes) {
        Loggers.info("NOTE WITH ID " + id + " UPDATED");
        Notes note = notesService.updateNoteById(id, notes);
        if(note == null){
            return ResponseEntity.status(400).header("msg", "BAD REQUEST").build();
        }
        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        Loggers.info("NOTE WITH ID " + id + " DELETED");
        Notes note = notesService.findByNoteId(id);
        if(note == null){
            return ResponseEntity.noContent().header("msg", "NO CONTENT DELETED BECAUSE IT DOESN'T EXIST").build();
        }
        notesService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getByTitle/{title}")
    public ResponseEntity<?> findByTitle(@PathVariable String title) {
        Loggers.info("NOTE WITH TITLE " + title + " REQUESTED");
        List<Notes> notesList = notesService.findByTitle(title);
        if (notesList == null) {
            return ResponseEntity.status(204).header("msg", "NO NOTES FOUND WITH THIS TITLE").build();
        }
        return ResponseEntity.ok(notesList);
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
