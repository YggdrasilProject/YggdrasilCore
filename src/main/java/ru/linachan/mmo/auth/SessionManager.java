package ru.linachan.mmo.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.linachan.mmo.auth.user.Session;
import ru.linachan.mmo.auth.user.Token;
import ru.linachan.mmo.auth.user.User;

import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SessionManager {

    private static Random randomGenerator = new Random();
    private static Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private List<Session> sessions = new CopyOnWriteArrayList<>();

    public Session createSession(User user) {
        byte[] token = new byte[128];
        randomGenerator.nextBytes(token);

        Session userSession = new Session(new Token(token), user);
        logger.info("SESSION({}): Session initialized", user.getLogin());

        sessions.stream()
            .filter(session -> session.getUser().getLogin().equals(user.getLogin()))
            .collect(Collectors.toList()).stream()
            .forEach(session -> closeSession(session.getToken()));

        sessions.add(userSession);

        return userSession;
    }

    public Session getSession(Token token) {
        final Session[] userSession = { null };

        sessions.stream()
            .filter(session -> session.getToken().equals(token))
            .forEach(session -> userSession[0] = session);

        return userSession[0];
    }

    public Session getSession(SelectionKey selectionKey) {
        final Session[] userSession = { null };

        sessions.stream()
            .filter(session -> selectionKey.equals(session.getAttribute("key", null)))
            .forEach(session -> userSession[0] = session);

        return userSession[0];
    }

    public void closeSession(Token token) {
        sessions.stream()
            .filter(session -> session.getToken().equals(token))
            .collect(Collectors.toList()).stream()
            .forEach(session -> {
                logger.info("SESSION({}): Session closed ({}s)", session.getUser().getLogin(), session.getDuration() / 1000.0);
                sessions.remove(session);
            });
    }

    public void closeSession(SelectionKey selectionKey) {
        sessions.stream()
            .filter(session -> selectionKey.equals(session.getAttribute("key", null)))
            .collect(Collectors.toList()).stream()
            .forEach(session -> {
                logger.info("SESSION({}): Session closed ({}s)", session.getUser().getLogin(), session.getDuration() / 1000.0);
                sessions.remove(session);
            });
    }
}
