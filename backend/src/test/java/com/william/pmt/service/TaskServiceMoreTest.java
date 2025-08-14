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
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceMoreTest
{
    private TaskRepository taskRepo = mock(TaskRepository.class);
    private TaskHistoryRepository historyRepo = mock(TaskHistoryRepository.class);
    private TaskEventRepository eventRepo = mock(TaskEventRepository.class);
    private UserRepository userRepo = mock(UserRepository.class);
    private NotificationService notificationService = mock(NotificationService.class);
    private TaskService service;

    @BeforeEach
    void init() throws Exception
    {
        service = new TaskService(taskRepo, historyRepo, userRepo, notificationService);
        Field f = TaskService.class.getDeclaredField("eventRepo");
        f.setAccessible(true);
        f.set(service, eventRepo);
    }

    @Test
    void update_should_use_full_signature()
    {
        Task t = new Task();
        when(taskRepo.findById(10L)).thenReturn(Optional.of(t));
        when(taskRepo.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        User actor = new User();
        actor.setUsername("bob");

        service.update(
                10L,
                "New name",
                "New desc",
                LocalDate.now().plusDays(5),
                TaskPriority.MEDIUM,
                TaskStatus.IN_PROGRESS,
                Instant.now(),
                actor
        );

        verify(taskRepo).save(any(Task.class));
        verify(eventRepo, atLeastOnce()).save(any());
    }
}
