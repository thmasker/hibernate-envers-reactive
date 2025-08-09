package org.thmasker.persistence;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Audited
public class Address {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "id", sequenceName = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id")
    private int id;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "zip")
    private String zip;

    @Column(name = "country")
    private String country;

    @OneToMany(mappedBy = "address")
    private List<Person> people;

    public Address() {
    }

    public Address(String city, String street, String zip, String country) {
        this.city = city;
        this.street = street;
        this.zip = zip;
        this.country = country;
    }

}
