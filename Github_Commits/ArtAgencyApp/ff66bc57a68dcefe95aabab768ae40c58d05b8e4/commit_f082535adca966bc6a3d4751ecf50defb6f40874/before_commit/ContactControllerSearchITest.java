package com.immpresariat.ArtAgencyApp.controllers;

import com.immpresariat.ArtAgencyApp.models.Contact;
import com.immpresariat.ArtAgencyApp.models.ContactPerson;
import com.immpresariat.ArtAgencyApp.models.Event;
import com.immpresariat.ArtAgencyApp.models.Institution;
import com.immpresariat.ArtAgencyApp.payload.EventDTO;
import com.immpresariat.ArtAgencyApp.repository.ContactPersonRepository;
import com.immpresariat.ArtAgencyApp.repository.ContactRepository;
import com.immpresariat.ArtAgencyApp.repository.EventRepository;
import com.immpresariat.ArtAgencyApp.repository.InstitutionRepository;
import com.immpresariat.ArtAgencyApp.service.ContactService;
import com.immpresariat.ArtAgencyApp.service.EventService;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ContactControllerSearchITest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private ContactService contactService;
    @Autowired
    private InstitutionRepository institutionRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    private ContactPersonRepository contactPersonRepository;

    @BeforeEach
    public void setup() {
        cleanDB();
    }

    @Test
    public void givenName_whenSearchByName_thenReturnTwoContacts() throws Exception {
        //given - precondition or setup
        generateTestData();
        String name = "dni";

        //when - action or the behavior that we are going to test
        ResultActions response = mockMvc.perform(get("/api/v1/contacts/search?name=" + name)
                .contentType(MediaType.APPLICATION_JSON));

        //then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", CoreMatchers.is(2)));
        //spr czy są dwa

    }
    //znajdź po nazwie
    //znajdź po miesiącu
    //znajdź po miesiącu i nazwie
    //negative scenario - pusty



    private void generateTestData(){

        Contact contact = contactRepository.save(new Contact());
        Long contactId = contact.getId();

        EventDTO event1 = EventDTO.builder()
                .name("Dni Wiązowny")
                .description("Bardzo miła impreza")
                .monthWhenOrganized(6)
                .build();

        eventService.create(event1, contactId);

        EventDTO event2 = EventDTO.builder()
                .name("Dni Białołęki")
                .description("")
                .monthWhenOrganized(8)
                .build();
        eventService.create(event2, contactId);

        EventDTO event3 = EventDTO.builder()
                .name("Sylwester z jedynką")
                .description("No raczej tam nie zagramy")
                .monthWhenOrganized(8)
                .build();
        eventService.create(event3, contactId);

    }

    private void cleanDB() {
        List<Contact> contacts = contactRepository.findAll();
        for(Contact contact : contacts){
            contactService.deleteWithAssociatedData(contact.getId());
        }
    }
}
