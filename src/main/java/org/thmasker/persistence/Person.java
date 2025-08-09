package org.thmasker.persistence;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class Person {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "id", sequenceName = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @JoinColumn(name = "address")
    @ManyToOne(cascade = CascadeType.ALL)
    private Address address;

    public Person() {
    }

    public Person(String name, String surname, Address address) {
        this.name = name;
        this.surname = surname;
        this.address = address;
    }

}
