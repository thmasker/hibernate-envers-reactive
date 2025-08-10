package org.thmasker.console;

import io.smallrye.mutiny.Uni;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.reactive.mutiny.Mutiny;
import org.thmasker.persistence.Address;
import org.thmasker.persistence.Person;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Console {

    private final Mutiny.SessionFactory sessionFactory;

    public Console(EntityManagerFactory entityManagerFactory) {
        this.sessionFactory = entityManagerFactory.unwrap(Mutiny.SessionFactory.class);
    }

    private String convertString(String s, String def) {
        if ("NULL".equals(s)) {
            return null;
        }
        if ("".equals(s)) {
            return def;
        }
        return s;
    }

    private int convertStringToInteger(String s, int def) {
        if ("".equals(s)) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number, returning 0.");
            return 0;
        }
    }

    private void printPerson(StringBuilder sb, Person p) {
        sb.append("id = ").append(p.getId()).append(", name = ").append(p.getName())
                .append(", surname = ").append(p.getSurname());

        Address a = p.getAddress();
        if (a != null) {
            sb.append(", address = <").append(a.getId()).append("> ").append(a.getStreetName()).append(" ")
                    .append(a.getHouseNumber()).append("/").append(a.getFlatNumber());
        }
    }

    private Uni<Void> printPersons(StringBuilder sb) {
        return sessionFactory.getCurrentSession()
                .createQuery("select p from Person p order by p.id", Person.class).getResultList()
                .map(persons -> {
                    sb.append("Persons:\n");
                    for (Person p : persons) {
                        printPerson(sb, p);
                        sb.append("\n");
                    }
                    return persons;
                }).replaceWithVoid();
    }

//    todo
//    private void printPersonHistory(StringBuilder sb, int personId) {
//        AuditReader reader = AuditReaderFactory.get(entityManager);
//
//        List personHistory = reader.createQuery()
//                .forRevisionsOfEntity(Person.class, false, true)
//                .add(AuditEntity.id().eq(personId))
//                .getResultList();
//
//        if (personHistory.isEmpty()) {
//            sb.append("A person with id ").append(personId).append(" does not exist.\n");
//        } else {
//            for (Object historyObj : personHistory) {
//                Object[] history = (Object[]) historyObj;
//                DefaultRevisionEntity revision = (DefaultRevisionEntity) history[1];
//                sb.append("revision = ").append(revision.getId()).append(", ");
//                printPerson(sb, (Person) history[0]);
//                sb.append(" (").append(revision.getRevisionDate()).append(")\n");
//            }
//        }
//    }

//     todo
//    private void printPersonAtRevision(StringBuilder sb, int personId, int revision) {
//        AuditReader reader = AuditReaderFactory.get(entityManager);
//
//        Person p = reader.find(Person.class, personId, revision);
//        if (p == null) {
//            sb.append("This person does not exist at that revision.");
//        } else {
//            printPerson(sb, p);
//        }
//    }

    private Uni<Void> readAndSetAddress(Scanner scanner, Person p) {
        Address old = p.getAddress();

        String input = scanner.nextLine();
        if ("NULL".equals(input)) {
            p.setAddress(null);
            if (old != null) {
                old.getPersons().remove(p);
            }
        } else if ("".equals(input)) {
        } else {
            Integer id = null;
            try {
                id = Integer.valueOf(input);
            } catch (NumberFormatException e) {
                System.err.println("Invalid address id, setting to NULL.");
                p.setAddress(null);
                if (old != null) {
                    old.getPersons().remove(p);
                }
            }

            return sessionFactory.getCurrentSession().find(Address.class, id).map(address -> {
                if (address == null) {
                    System.err.println("Unknown address id, setting to NULL.");
                    p.setAddress(null);
                } else {
                    p.setAddress(address);
                    address.getPersons().add(p);
                }
                if (old != null) {
                    old.getPersons().remove(p);
                }
                return address;
            }).replaceWithVoid();
        }
        return Uni.createFrom().voidItem();
    }

    private Person readNewPerson(PrintStream out, Scanner scanner) {
        Person p = new Person();

        out.print("Person name (NULL for null): ");
        p.setName(convertString(scanner.nextLine(), ""));

        out.print("Person surname (NULL for null): ");
        p.setSurname(convertString(scanner.nextLine(), ""));

        out.print("Person address id (NULL for null): ");
        readAndSetAddress(scanner, p);

        return p;
    }

    private Uni<Void> readModifyPerson(PrintStream out, Scanner scanner, int personId) {
        return sessionFactory.getCurrentSession().find(Person.class, personId).map(person -> {
            if (person == null) {
                out.println("Person with id " + personId + " does not exist.");
                return null;
            }

            out.print("Person name (NULL for null, enter for no change, current - " + person.getName() + "): ");
            person.setName(convertString(scanner.nextLine(), person.getName()));

            out.print("Person surname (NULL for null, enter for no change, current - " + person.getSurname() + "): ");
            person.setSurname(convertString(scanner.nextLine(), person.getSurname()));

            out.print("Person address id (NULL for null, enter for no change, current - " +
                    (person.getAddress() == null ? "NULL" : person.getAddress().getId()) + "): ");
            readAndSetAddress(scanner, person);
            return person;
        }).replaceWithVoid();
    }

    private void printAddress(StringBuilder sb, Address a) {
        sb.append("id = ").append(a.getId()).append(", streetName = ").append(a.getStreetName())
                .append(", houseNumber = ").append(a.getHouseNumber())
                .append(", flatNumber = ").append(a.getFlatNumber())
                .append(", persons = (");

        Iterator<Person> iter = a.getPersons().iterator();
        while (iter.hasNext()) {
            Person p = iter.next();
            sb.append("<").append(p.getId()).append("> ").append(p.getName()).append(" ").append(p.getSurname());
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(")");
    }

    private Uni<Void> printAddresses(StringBuilder sb) {
        return sessionFactory.getCurrentSession()
                .createQuery("select a from Address a order by a.id", Address.class).getResultList()
                .map(addresses -> {
                    sb.append("Addresses:\n");
                    for (Address a : addresses) {
                        printAddress(sb, a);
                        sb.append("\n");
                    }
                    return addresses;
                }).replaceWithVoid();
    }

//    todo
//    private void printAddressHistory(StringBuilder sb, int addressId) {
//        AuditReader reader = AuditReaderFactory.get(entityManager);
//
//        List addressHistory = reader.createQuery()
//                .forRevisionsOfEntity(Address.class, false, true)
//                .add(AuditEntity.id().eq(addressId))
//                .getResultList();
//
//        if (addressHistory.isEmpty()) {
//            sb.append("A address with id ").append(addressId).append(" does not exist.\n");
//        } else {
//            for (Object historyObj : addressHistory) {
//                Object[] history = (Object[]) historyObj;
//                DefaultRevisionEntity revision = (DefaultRevisionEntity) history[1];
//                sb.append("revision = ").append(revision.getId()).append(", ");
//                printAddress(sb, (Address) history[0]);
//                sb.append(" (").append(revision.getRevisionDate()).append(")\n");
//            }
//        }
//    }

//    todo
//    private void printAddressAtRevision(StringBuilder sb, int addressId, int revision) {
//        AuditReader reader = AuditReaderFactory.get(entityManager);
//
//        Address a = reader.find(Address.class, addressId, revision);
//        if (a == null) {
//            sb.append("This address does not exist at that revision.");
//        } else {
//            printAddress(sb, a);
//        }
//    }

    private Address readNewAddress(PrintStream out, Scanner scanner) {
        Address a = new Address();

        out.print("Street name (NULL for null): ");
        a.setStreetName(convertString(scanner.nextLine(), ""));

        out.print("House number: ");
        a.setHouseNumber(convertStringToInteger(scanner.nextLine(), 0));

        out.print("Flat number: ");
        a.setFlatNumber(convertStringToInteger(scanner.nextLine(), 0));

        a.setPersons(new HashSet<>());

        return a;
    }

    private Uni<Void> readModifyAddress(PrintStream out, Scanner scanner, int addressId) {
        return sessionFactory.getCurrentSession().find(Address.class, addressId).map(address -> {
            if (address == null) {
                out.println("Address with id " + addressId + " does not exist.");
                return null;
            }

            out.print("Street name (NULL for null, enter for no change, current - " + address.getStreetName() + "): ");
            address.setStreetName(convertString(scanner.nextLine(), address.getStreetName()));

            out.print("House number (enter for no change, current - " + address.getHouseNumber() + "): ");
            address.setHouseNumber(convertStringToInteger(scanner.nextLine(), address.getHouseNumber()));

            out.print("Flat number (enter for no change, current - " + address.getFlatNumber() + "): ");
            address.setFlatNumber(convertStringToInteger(scanner.nextLine(), address.getFlatNumber()));

            return address;
        }).replaceWithVoid();
    }

    private void start() {
        Scanner scanner = new Scanner(System.in);
        PrintStream out = System.out;

        AtomicBoolean quit = new AtomicBoolean(false);
        while (!quit.get()) {
            out.println("-----------------------------------------------");
            out.println("1 - list persons             5 - list addresses");
            out.println("2 - list person history      6 - list addresses history");
            out.println("3 - new person               7 - new address");
            out.println("4 - modify person            8 - modify address");
            out.println("9 - get person at revision  10 - get address at revision");
            out.println("                             0 - end");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                // continuing
            }

            int finalChoice = choice;
            sessionFactory.withSession(session -> {
                StringBuilder sb;
                int personId;
                int addressId;
                int revision;
                switch (finalChoice) {
                    case 1:
                        sb = new StringBuilder();
                        printPersons(sb);
                        out.println(sb);
                        break;
                    case 2:
                        out.print("Person id: ");
                        personId = scanner.nextInt();
                        scanner.nextLine();
                        sb = new StringBuilder();
//                        todo
//                        printPersonHistory(sb, personId);
                        out.println(sb);
                        break;
                    case 3:
                        Person p = readNewPerson(out, scanner);
                        session.persist(p);
                        break;
                    case 4:
                        out.print("Person id: ");
                        personId = scanner.nextInt();
                        scanner.nextLine();
                        return readModifyPerson(out, scanner, personId);
                    case 5:
                        sb = new StringBuilder();
                        printAddresses(sb);
                        out.println(sb);
                        break;
                    case 6:
                        out.print("Address id: ");
                        addressId = scanner.nextInt();
                        scanner.nextLine();
                        sb = new StringBuilder();
//                        todo
//                        printAddressHistory(sb, addressId);
                        out.println(sb);
                        break;
                    case 7:
                        Address a = readNewAddress(out, scanner);
                        session.persist(a);
                        break;
                    case 8:
                        out.print("Address id: ");
                        addressId = scanner.nextInt();
                        scanner.nextLine();
                        readModifyAddress(out, scanner, addressId);
                        break;
                    case 9:
                        out.print("Person id: ");
                        personId = scanner.nextInt();
                        scanner.nextLine();
                        out.print("Revision number: ");
                        revision = scanner.nextInt();
                        scanner.nextLine();
                        if (revision <= 0) {
                            System.out.println("Revision must be greater than 0");
                            break;
                        }
                        sb = new StringBuilder();
//                        todo
//                        printPersonAtRevision(sb, personId, revision);
                        out.println(sb);
                        break;
                    case 10:
                        out.print("Address id: ");
                        addressId = scanner.nextInt();
                        scanner.nextLine();
                        out.print("Revision number: ");
                        revision = scanner.nextInt();
                        scanner.nextLine();
                        if (revision <= 0) {
                            System.out.println("Revision must be greater than 0");
                            break;
                        }
                        sb = new StringBuilder();
//                        todo
//                        printAddressAtRevision(sb, addressId, revision);
                        out.println(sb);
                        break;
                    default:
                        quit.set(true);
                }
                return null;
            });
        }
    }

    private void stop() {
        sessionFactory.close();
    }

    private Uni<Boolean> hasData() {
        Mutiny.Session session = sessionFactory.getCurrentSession();

        Uni<Long> addressCount = session.createQuery("select count(a) from Address a", Long.class).getSingleResult();
        Uni<Long> personCount = session.createQuery("select count(p) from Person p", Long.class).getSingleResult();

        return Uni.combine().all().unis(addressCount, personCount).asTuple()
                .onItem().transform(tuple -> tuple.getItem1() + tuple.getItem2() > 0);
    }

    private Uni<Void> populateTestData() {
        return sessionFactory.withSession(session ->
                hasData().map(hasData -> {
                    if (Boolean.TRUE.equals(hasData)) {
                        Person p1 = new Person();
                        Person p2 = new Person();
                        Person p3 = new Person();

                        Address a1 = new Address();
                        Address a2 = new Address();

                        p1.setName("James");
                        p1.setSurname("Bond");
                        p1.setAddress(a1);

                        p2.setName("John");
                        p2.setSurname("McClane");
                        p2.setAddress(a2);

                        p3.setName("Holly");
                        p3.setSurname("Gennaro");
                        p3.setAddress(a2);

                        a1.setStreetName("MI6");
                        a1.setHouseNumber(18);
                        a1.setFlatNumber(25);
                        a1.setPersons(new HashSet<>());
                        a1.getPersons().add(p1);

                        a2.setStreetName("Nakatomi Plaza");
                        a2.setHouseNumber(10);
                        a2.setFlatNumber(34);
                        a2.setPersons(new HashSet<>());
                        a2.getPersons().add(p2);
                        a2.getPersons().add(p3);

                        session.persist(a1);
                        session.persist(a2);

                        session.persist(p1);
                        session.persist(p2);
                        session.persist(p3);

                        System.out.println("The DB was populated with example data.");
                    }
                    return null;
                }));
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ConsolePU");
        var console = new Console(emf);

        System.out.println();

        console.populateTestData();
//        console.start();
//        console.stop();
        emf.close();
    }

}
