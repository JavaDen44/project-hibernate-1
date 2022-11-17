package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "12345");
        this.sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        String sql = "SELECT * FROM rpg.player";
        try (Session session = sessionFactory.openSession()){
            NativeQuery<Player> nativeQuery = session.createNativeQuery(sql, Player.class);
            nativeQuery.setFirstResult(pageNumber * pageSize);
            nativeQuery.setMaxResults(pageSize);
            return nativeQuery.getResultList();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()){
            Query<Long> namedQuery = session.createNamedQuery("GetAllCountPlayer", Long.class);
            return Math.toIntExact(namedQuery.uniqueResult());
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.save(player);
            transaction.commit();
            return player;
        }
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            session.merge(player);
            transaction.commit();
            return player;
        }
    }

    @Override
    public Optional<Player> findById(long id) {
        Player player;
        try (Session session = sessionFactory.openSession()){
            player = session.find(Player.class,id);
        }
        return Optional.ofNullable(player);
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()){
            Transaction transaction = session.beginTransaction();
            player = session.find(Player.class, player.getId());
            session.remove(player);
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}