package org.thmasker;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.tool.schema.Action;

import org.hibernate.testing.transaction.TransactionUtil2;

import org.thmasker.persistence.Address;
import org.thmasker.persistence.Person;

/**
 * @author Steve Ebersole
 */
public class SimpleTest {
	public static void main(String[] args) {
		try ( final SessionFactoryImplementor sessionFactory = createSessionFactory().unwrap( SessionFactoryImplementor.class ) ) {
			TransactionUtil2.inStatelessTransaction( sessionFactory, (statelessSession) -> {
				final Address address = new Address();
				address.setHouseNumber( 123 );
				address.setStreetName( "Main St" );

				statelessSession.insert( address );
			} );
		}
	}

	private static SessionFactoryImplementor createSessionFactory() {
		// have to use Configuration instead of JPA PersistenceConfiguration
		// because of the presence of the reactive provider in the main persistence.xml
		return new Configuration()
				.setJdbcUrl( "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000" )
				.setProperty( JdbcSettings.JAKARTA_JDBC_USER, "sa" )
				.setProperty( JdbcSettings.JAKARTA_JDBC_PASSWORD, "" )
				.setProperty( JdbcSettings.JAKARTA_JDBC_DRIVER, "org.h2.Driver" )
				.showSql( true, true, true )
				.addAnnotatedClasses( Person.class, Address.class )
				.setSchemaExportAction( Action.CREATE_DROP )
				.buildSessionFactory()
				.unwrap( SessionFactoryImplementor.class );
	}
}
