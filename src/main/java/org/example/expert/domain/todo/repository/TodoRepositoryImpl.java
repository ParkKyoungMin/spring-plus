package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin() // User와 함께 조회
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Todo> findTodosWithUser(Pageable pageable) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(todo.count())
                        .from(todo)
                        .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(todos, pageable, () -> total);
    }

    @Override
    public Page<Todo> findAllByWeatherAndDateRange(String weather, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        QTodo todo = QTodo.todo;

        List<Todo> todos = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(
                        weather != null ? todo.weather.eq(weather) : null,
                        startDate != null ? todo.modifiedAt.goe(startDate) : null,
                        endDate != null ? todo.modifiedAt.loe(endDate) : null
                )
                .orderBy(todo.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                        .select(todo.count())
                        .from(todo)
                        .where(
                                weather != null ? todo.weather.eq(weather) : null,
                                startDate != null ? todo.modifiedAt.goe(startDate) : null,
                                endDate != null ? todo.modifiedAt.loe(endDate) : null
                        )
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(todos, pageable, total);
    }
}
