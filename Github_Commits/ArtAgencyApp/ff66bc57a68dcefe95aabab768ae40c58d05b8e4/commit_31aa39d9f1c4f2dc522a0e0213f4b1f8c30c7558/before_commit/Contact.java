package com.immpresariat.ArtAgencyApp.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.net.URL;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;

    private boolean alreadyCooperated;

    private Date updated;

    private String webPage;

    @Pattern(regexp = "^[\\\\+]?[(]?[0-9]{2}[)]?-?[0-9]{3}[-\\\\s\\\\.]?[0-9]{4,6}$",
            message = "Invalid phone number. Proper structure: \"+\"[dial number][number]. Eg: +481111222333")
    private String phone;

    @Email(message = "Invalid email")
    private String email;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Event> events;

    @OneToMany(fetch = FetchType.EAGER)
    private List<ContactPerson> contactPeople;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Institution> institutions;

}
