package org.thmasker.resource;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.thmasker.persistence.Address;
import org.thmasker.persistence.Person;
import org.thmasker.persistence.PersonRepository;

@ApplicationScoped
@Path("/resource")
public class Resource {

    private final PersonRepository repository;

    public Resource(PersonRepository repository) {
        this.repository = repository;
    }

    @POST
    @WithTransaction
    public Uni<Person> post() {
        final var address = new Address("city", "street", "zip", "country");
        final var person = new Person("name", "surname", address);
        return repository.persist(person);
    }

}
