/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import model.Artist;
import model.Event;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

import javax.persistence.Query;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * Although ORMStandaloneTestCase is perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing your reproducer using this method
 * simplifies the process.
 *
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
public class ORMUnitTestCase extends BaseCoreFunctionalTestCase {

	// Add your entities here.
	@Override
	protected Class[] getAnnotatedClasses() {
		return new Class[] {
				Event.class,
				Artist.class
		};
	}

	// Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
	@Override
	protected void configure(Configuration configuration) {
		super.configure( configuration );

		configuration.setProperty( AvailableSettings.SHOW_SQL, Boolean.TRUE.toString() );
		configuration.setProperty( AvailableSettings.FORMAT_SQL, Boolean.TRUE.toString() );
		//configuration.setProperty( AvailableSettings.GENERATE_STATISTICS, "true" );
	}

	// Add your tests, using standard JUnit.
	@Test
	public void hhh123Test() throws Exception {
		// BaseCoreFunctionalTestCase automatically creates the SessionFactory and provides the Session.
		Session s = openSession();
		Transaction tx = s.beginTransaction();
		System.out.println(" CREATE TEST DATA ");
		Event event = new Event("Event1", "Event1_dsc");
		session.save(event);
		String[] dataStr = new String[] { "One", "Two" };
		Artist artist;
		for (String str : dataStr) {
			artist = new Artist("Artist"+str, "Artist"+str+"_dsc");
			session.save(artist);
			event.getArtists().add(artist);
		}
		Long idEvent = event.getIdEvent();
		tx.commit();
		s.close();


		s = openSession();
		tx = s.beginTransaction();

		/*System.out.println(" RETRIEVE EVENT FROM NAME - HQL - OK");
		Query query = s.createQuery("FROM Event e WHERE e.name = :name");
		query.setParameter("name", "Event1");
		List<Event> events = query.getResultList();
		assertEquals(events.size(),1);
		event = events.get(0);
		System.out.println("Event found: "+event.toString());

		System.out.println(" RETRIEVE EVENT AND ARTISTS FROM ID - HQL - OK");
		query = s.createQuery("FROM Event e LEFT JOIN FETCH e.artists WHERE e.idEvent = :id");
		query.setParameter("id", idEvent);
		events = query.getResultList();
		event = events.get(0);
		System.out.println("Event found: "+event.toString());
		System.out.println("Artists for this event: "+event.getArtists().toString());*/

		System.out.println(" RETRIEVE EVENT AND ARTISTS FROM ID - NATIVE QUERY - KO but it's normal");
		// http://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#sql-entity-associations-query
		// No exception but data badly retrieved because there is a conflict of names
		// since the two entities are mapped to the same column names (id, name, description).
		// So we get Artist{description='Event1_dsc', idArtist=2, name='Event1'} instead of
		// Artist{description='ArtistOne_dsc', idArtist=2, name='ArtistOne'}

		NativeQuery nativeQuery = session.createNativeQuery("SELECT e.*, a.* "+
				"FROM Event e "+
				"LEFT JOIN EventArtists ea ON e.idEvent = ea.idEvent "+
				"LEFT JOIN Artist a ON a.idArtist = ea.idArtist "+
				"WHERE e.idEvent = :id");
		nativeQuery.addEntity("e", Event.class)
				.addJoin("a", "e.artists")
				.setParameter("id", idEvent);
		List<Object[]> listTmp = nativeQuery.getResultList();
		event = (Event) (listTmp.get(0)[0]);
		System.out.println("Event found: "+event.toString());
		System.out.println("Artists for this event: "+event.getArtists().toString());

		System.out.println(" RETRIEVE EVENT AND ARTISTS FROM ID - NATIVE QUERY USING ALIASES - KO");
		/* Same column names appear in more than one table -> use of aliases.
		Generated request is not correct. Hibernate looks for columns "a.idEvent as idEvent1_2_0__, a.idArtist as idArtist2_2_0__", which are EventArtists's columns
		SELECT
        e.idEvent as idEvent1_1_0_,
        e.description as descript2_1_0_,
        e.name as name3_1_0_,
        a.idEvent as idEvent1_2_0__,
        a.idArtist as idArtist2_2_0__,
        a.idArtist as idArtist1_0_1_,
        a.description as descript2_0_1_,
        a.name as name3_0_1_
		FROM
			Event e
		LEFT JOIN
			EventArtists ea
				ON e.idEvent = ea.idEvent
		LEFT JOIN
			Artist a
				ON a.idArtist = ea.idArtist
		WHERE
			e.idEvent = ?
		Exception Caused by: org.h2.jdbc.JdbcSQLException: Column "A.IDEVENT" not found; SQL statement:*/

		nativeQuery = session.createNativeQuery("SELECT {e.*}, {a.*} "+
				"FROM Event e "+
				"LEFT JOIN EventArtists ea ON e.idEvent = ea.idEvent "+
				"LEFT JOIN Artist a ON a.idArtist = ea.idArtist "+
				"WHERE e.idEvent = :id");
		nativeQuery.addEntity("e", Event.class)
				   .addJoin("a", "e.artists")
				   .setParameter("id", event.getIdEvent());
		listTmp = nativeQuery.getResultList();
		event = (Event) (listTmp.get(0)[0]);
		System.out.println("Event found: "+event.toString());
		System.out.println("Artists for this event: "+event.getArtists().toString());

		tx.commit();
		s.close();
	}
}
