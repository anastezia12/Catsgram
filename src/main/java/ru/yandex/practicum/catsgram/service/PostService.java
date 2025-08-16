package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.SortOrder;

import java.time.Instant;
import java.util.*;

@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();

    public Collection<Post> findAll(SortOrder sortOrder, Integer size, Integer from) {
        if (size == null ) {
            size = 10;
        } else if (size < 0) {
            throw new ParameterNotValidException("size", "Некорректный размер выборки. Размер должен быть больше нуля");
        }
        if(from == null){
            from = 0;
        } else if(from < 0){
            throw new ParameterNotValidException("from", "Некорректный размер выборки. начало должено быть больше нуля");
        }
        List<Post> sortedPost = sortPost(sortOrder);
        return sortedPost.subList(from, from + size);
    }

    public List<Post> sortPost(SortOrder sortOrder) {
       if(sortOrder == SortOrder.DESCENDING) {
           return posts.values().stream().sorted(Comparator.comparing(Post::getPostDate)).toList().reversed();
       }
        return posts.values().stream().sorted(Comparator.comparing(Post::getPostDate)).toList();
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            isIdPresent(newPost.getAuthorId());
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Post getById(Long id) {
        isIdPresent(id);
        return posts.get(id);
    }

    private void isIdPresent(Long id) {
        if (UserService.getUsers().stream().noneMatch(x -> x.getId() == id)) {
            throw new NotFoundException("Can not find user with id=" + id);
        }
    }
}
