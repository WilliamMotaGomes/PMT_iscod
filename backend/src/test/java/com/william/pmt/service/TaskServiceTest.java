package com.william.pmt.service;

import com.william.pmt.model.Task;
import com.william.pmt.model.TaskPriority;
import com.william.pmt.model.TaskStatus;
import com.william.pmt.model.User;
import com.william.pmt.repository.TaskEventRepository;
import com.william.pmt.repository.TaskHistoryRepository;
import com.william.pmt.repository.TaskRepository;
import com.william.pmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest
{
    private final TaskRepository taskRepo = mock(TaskRepository.class);
    private final TaskHistoryRepository historyRepo = mock(TaskHistoryRepository.class);
    private final TaskEventRepository eventRepo = mock(TaskEventRepository.class);
    private final UserRepository userRepo = mock(UserRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private TaskService service;

    @BeforeEach
    void setup() throws Exception
    {
        service = new TaskService(taskRepo, historyRepo, userRepo, notificationService);
        Field f = TaskService.class.getDeclaredField("eventRepo");
        f.setAccessible(true);
        f.set(service, eventRepo);
    }

    @Test
    void assign_should_persist_and_log()
    {
        Task t = new Task();
        User assignee = new User(); assignee.setUsername("alice");
        User actor = new User(); actor.setUsername("owner");

        when(taskRepo.findById(1L)).thenReturn(Optional.of(t));
        when(userRepo.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepo.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        service.assign(1L, 2L, actor);

        verify(taskRepo, atLeastOnce()).save(any(Task.class));
        verify(eventRepo, atLeastOnce()).save(any());
        verify(historyRepo, never()).save(any());
    }

    @Test
    void update_throws_whenTaskNotFound()
    {
        when(taskRepo.findById(404L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                service.update(404L, "n", "d", LocalDate.now(),
                        TaskPriority.LOW, TaskStatus.TODO, Instant.now(), new User()));

        verify(taskRepo, never()).save(any());
        verify(historyRepo, never()).save(any());
        verify(eventRepo, never()).save(any());
    }

    @Test
    void assign_throws_whenAssigneeNotFound()
    {
        when(taskRepo.findById(1L)).thenReturn(Optional.of(new Task()));
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () ->
                service.assign(1L, 2L, new User()));

        verify(taskRepo, never()).save(any());
        verify(historyRepo, never()).save(any());
        verify(eventRepo, never()).save(any());
    }
}
