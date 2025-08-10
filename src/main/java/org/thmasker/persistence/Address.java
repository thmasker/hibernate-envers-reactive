package org.thmasker.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.hibernate.envers.Audited;

import java.util.Objects;
import java.util.Set;

@Entity
public class Address {

    @Id
    @GeneratedValue
    private int id;

    @Audited
    private String streetName;

    @Audited
    private Integer houseNumber;

    @Audited
    private Integer flatNumber;

    @Audited
    @OneToMany(mappedBy = "address")
    private Set<Person> persons;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public Integer getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(Integer houseNumber) {
        this.houseNumber = houseNumber;
    }

    public Integer getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(Integer flatNumber) {
        this.flatNumber = flatNumber;
    }

    public Set<Person> getPersons() {
        return persons;
    }

    public void setPersons(Set<Person> persons) {
        this.persons = persons;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;

        if (id != address.id) return false;
        if (!Objects.equals(flatNumber, address.flatNumber)) return false;
        if (!Objects.equals(houseNumber, address.houseNumber)) return false;
        return Objects.equals(streetName, address.streetName);
    }

    public int hashCode() {
        int result;
        result = id;
        result = 31 * result + (streetName != null ? streetName.hashCode() : 0);
        result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0);
        result = 31 * result + (flatNumber != null ? flatNumber.hashCode() : 0);
        return result;
    }

}
