package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private static final Map<Long, User> users = new HashMap<>();

    public static List<User> getUsers() {
        return users.values().stream().toList();
    }

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (users.values().stream().anyMatch(x -> x.getEmail().equals(user.getEmail()))) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(user.getId())) {
            if (users.values().stream().filter(x -> x.getId() != user.getId()).anyMatch(x -> x.getEmail().equals(user.getEmail()))) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            if (user.getEmail() != null) {
                users.get(user.getId()).setEmail(user.getEmail());
            }
            if (user.getUsername() != null) {
                users.get(user.getId()).setUsername(user.getUsername());
            }
            if (user.getPassword() != null) {
                users.get(user.getId()).setPassword(user.getPassword());
            }
            return users.get(user.getId());
        }
        throw new NotFoundException("User с id = " + user.getId() + " не найден");

    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public User findById(Long id) {
        if (users.get(id) == null) {
            throw new NotFoundException("Can not find user with id=" + id);
        }
        return users.get(id);
    }
}
