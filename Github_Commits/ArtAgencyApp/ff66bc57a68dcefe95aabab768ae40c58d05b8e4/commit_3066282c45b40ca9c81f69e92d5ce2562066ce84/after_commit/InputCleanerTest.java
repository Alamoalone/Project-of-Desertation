package com.immpresariat.ArtAgencyApp.utils;

import com.immpresariat.ArtAgencyApp.models.Contact;
import com.immpresariat.ArtAgencyApp.models.ContactPerson;
import com.immpresariat.ArtAgencyApp.models.Event;
import com.immpresariat.ArtAgencyApp.models.Institution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InputCleanerTest {
    InputCleaner inputCleaner;

    @BeforeEach
    public void setup() {
        inputCleaner = new InputCleaner();
    }

    @DisplayName("JUnit test for clean institution method")
    @Test
    public void givenInstitutionObject_whenClean_thenReturnCleanedInstitutionObject() {
        //given - precondition or setup
        Institution institution = Institution.builder()
                .name(" Dom Kultury ")
                .notes("   Notatki    ")
                .city(" Jakieś miasto  ")
                .category(" dom kultury ")
                .build();

        //when - action or the behavior that we are going to test
        Institution cleanedInstitution = inputCleaner.clean(institution);


        //then - verify the output
        assertEquals("Dom Kultury", cleanedInstitution.getName());
        assertEquals("Notatki", cleanedInstitution.getNotes());
        assertEquals("Jakieś miasto", cleanedInstitution.getCity());
        assertEquals("dom kultury", cleanedInstitution.getCategory());

    }

    @DisplayName("JUnit test for clean event method")
    @Test
    public void givenEventObject_whenClean_thenReturnCleanedEventObject() {
        //given - precondition or setup
        Event event = Event.builder()
                .name("  Biba")
                .description("Cool biiiba  ")
                .build();


        //when - action or the behavior that we are going to test
        Event cleanedEvent = inputCleaner.clean(event);


        //then - verify the output
        assertEquals("Biba", cleanedEvent.getName());
        assertEquals("Cool biiiba", cleanedEvent.getDescription());

    }

    @DisplayName("JUnit test for clean contactPerson method")
    @Test
    public void givenContactPersonObject_whenClean_thenReturnCleanedContactPersonObject() {
        //given - precondition or setup
        ContactPerson contactPerson = ContactPerson.builder()
                .role(" Dyrektor")
                .email("mail@mail.pl ")
                .phone(" +48777222666 ")
                .firstName("   Jan  ")
                .lastName(" Kowalski   ")
                .build();

        //when - action or the behavior that we are going to test
        ContactPerson cleanedContactPerson = inputCleaner.clean(contactPerson);


        //then - verify the output
        assertEquals("dyrektor", cleanedContactPerson.getRole());
        assertEquals("mail@mail.pl", cleanedContactPerson.getEmail());
        assertEquals("+48777222666", cleanedContactPerson.getPhone());
        assertEquals("Jan", cleanedContactPerson.getFirstName());
        assertEquals("Kowalski", cleanedContactPerson.getLastName());

    }

    @DisplayName("JUnit test for clean contact method (all fields field)")
    @Test
    public void givenContactObject_whenTrim_thenReturnCleanedContactObject() {
        //given - precondition or setup
        Contact contact = Contact.builder()
                .title(" DK Chotomów  ")
                .description("   Opis  ")
                .webPage(" https://www.google.com/search ")
                .phone(" +48938475970 ")
                .email(" abc@.gmail.com")
                .build();

        //when - action or the behavior that we are going to test
        Contact cleanedContact = inputCleaner.clean(contact);


        //then - verify the output
        assertEquals("DK Chotomów", cleanedContact.getTitle());
        assertEquals("Opis", cleanedContact.getDescription());
        assertEquals("+48938475970", cleanedContact.getPhone());
        assertEquals("https://www.google.com/search", cleanedContact.getWebPage());
        assertEquals("abc@.gmail.com", cleanedContact.getEmail());
    }


    @DisplayName("JUnit test for clean contact method (only title)")
    @Test
    public void givenContactObjectWithTitleOnly_whenTrim_thenReturnCleanedContactObject() {
        //given - precondition or setup
        Contact contact = Contact.builder()
                .title(" DK Chotomów  ")
                .build();

        //when - action or the behavior that we are going to test
        Contact cleanedContact = inputCleaner.clean(contact);

        //then - verify the output
        assertNotNull(cleanedContact);
        assertEquals("DK Chotomów", cleanedContact.getTitle());
    }

}