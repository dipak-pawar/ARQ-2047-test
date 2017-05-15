package org.acme.jpa;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class GameTest {

    private static final String[] GAME_TITLES = {"Super Mario Brothers",
        "Mario Kart", "F-Zero"};

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test1.war")
            .addPackage(Game.class.getPackage())
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    EntityManager em;

    @Resource
    UserTransaction utx;

    @javax.inject.Inject
    Executor executor;

    @Before
    public void clearDB() throws Exception {
        clearDatabase();
    }

    @After
    public void commitTransaction() throws Exception {
        utx.commit();
    }

    @Test
    public void shouldFindAllGamesUsingExplicitJpqlQuery() throws Exception {

        // here we are calling a method with runnable as parameter defined in Executor class.
        insertData();
        startTransaction();

        // given
        String fetchingAllGamesInJpql = "select g from Game g order by g.id";

        // when

        List<Game> games = em.createQuery(fetchingAllGamesInJpql, Game.class)
            .getResultList();

        // then
        assertContainsAllGames(games);
    }

    // Private utility methods
    private static void assertContainsAllGames(Collection<Game> retrievedGames) {
        assertEquals(GAME_TITLES.length, retrievedGames.size());
        final Set<String> retrievedGameTitles = new HashSet<String>();
        for (Game game : retrievedGames) {
            retrievedGameTitles.add(game.getTitle());
        }
        assertTrue(retrievedGameTitles.containsAll(Arrays.asList(GAME_TITLES)));
    }

    private void clearDatabase() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.createQuery("delete from Game").executeUpdate();
        utx.commit();
    }

    private void startTransaction() throws Exception {
        utx.begin();
        em.joinTransaction();
    }

    private void insertData() throws Exception {
        executor.execute(() -> {
            try {
                startTransaction();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            for (String title : GAME_TITLES) {
                Game game = new Game(title);
                em.persist(game);
            }
            try {
                utx.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

