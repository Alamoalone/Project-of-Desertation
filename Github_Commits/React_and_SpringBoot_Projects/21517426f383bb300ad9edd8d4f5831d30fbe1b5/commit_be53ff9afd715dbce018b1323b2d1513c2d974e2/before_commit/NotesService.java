package com.notes.keep.service;

import com.notes.keep.model.Notes;
import com.notes.keep.model.User;
import com.notes.keep.repository.NotesRepository;
import com.notes.keep.repository.UserRepository;
import com.notes.keep.utils.EncryptionUtil;
import com.notes.keep.utils.FormatDateTime;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotesService {
    private final NotesRepository notesRepository;

    @Autowired
    private UserRepository userRepository;

    public BCryptPasswordEncoder encoder;


    @Autowired
    public NotesService(NotesRepository notesRepository) {
        this.notesRepository = notesRepository;
    }

    @Autowired
    private EncryptionUtil encryptionUtil;


    public Notes createNote(Notes note) throws Exception {
        try{
            Optional<User> user = userRepository.findById(note.getUser().getUserId());
            note.setUser(user.get());
            note.setDate(FormatDateTime.parseStandardDate(note.getDate()));
            note.setTitle(encryptionUtil.encrypt(note.getTitle()));
            note.setDescription(encryptionUtil.encrypt(note.getDescription()));
        }catch (Exception e){
            throw new Exception(e);
        }
        return notesRepository.save(note);
    }

    public List<Notes> notesList() {
        List<Notes> notesList = notesRepository.findAll();

//        notesList.stream()
//                .forEach(ele -> ele.setTitle(encryptionUtil.decrypt(ele.getTitle())));
//
//        notesList.forEach(System.out::println);

        Notes notes = notesList.get(0);
        System.out.println(encryptionUtil.decrypt(notes.getTitle() + " " + encryptionUtil.decrypt(notes.getDescription())));

        List<Notes> collected = null;
        return collected;
    }

    public Notes findByNoteId(Integer id) {
        return notesRepository.findByNoteId(id);
    }

    public Notes updateNoteById(Integer id, Notes notes) {
        Notes oldNote = notesRepository.findByNoteId(id);
        if(oldNote == null){
            return null;
        }
//        oldNote.setColor(notes.getColor());
        oldNote.setDate(FormatDateTime.parseStandardDate(notes.getDate()));
        oldNote.setTitle(notes.getTitle());
        oldNote.setDescription(notes.getDescription());
        oldNote.setCompleted(notes.isCompleted());
        notesRepository.save(oldNote);
        return oldNote;
    }

    public void deleteById(Integer id) {
        notesRepository.deleteById(id);
    }

    public List<Notes> findByTitle(String title){
        return notesRepository.findByTitle(title);
    }


    public List<Notes> findAllByUserUserId(Integer userId){
        return notesRepository.findAllByUserUserId(userId);
    }
}

