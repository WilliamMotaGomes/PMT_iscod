package com.william.pmt.controller;

import com.william.pmt.dto.AssignTaskRequest;
import com.william.pmt.dto.UpdateTaskRequest;
import com.william.pmt.model.*;
import com.william.pmt.repository.ProjectRepository;
import com.william.pmt.repository.TaskRepository;
import com.william.pmt.service.MembershipService;
import com.william.pmt.service.TaskService;
import com.william.pmt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    private static void setId(Object entity, long id)
    {
        try
        {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private UserService users;
    private ProjectRepository projects;
    private TaskRepository tasksRepo;
    private TaskService tasks;
    private MembershipService membership;
    private TaskController ctrl;
    private User current;
    private Task task;
    private Project project;

    @BeforeEach
    void setup()
    {
        users = mock(UserService.class);
        projects = mock(ProjectRepository.class);
        tasksRepo = mock(TaskRepository.class);
        tasks = mock(TaskService.class);
        membership = mock(MembershipService.class);

        ctrl = new TaskController(users, projects, tasksRepo, tasks, membership);

        current = new User(); current.setUsername("me"); setId(current, 42L);
        when(users.findById(42L)).thenReturn(Optional.of(current));

        project = new Project(); setId(project, 7L);
        task = new Task(); task.setProject(project);
        when(tasksRepo.findById(100L)).thenReturn(Optional.of(task));
    }


    @Test
    void get_returns_task_when_member_or_above()
    {
        when(membership.hasAtLeast(7L, 42L, Role.OBSERVER)).thenReturn(true);

        Task res = ctrl.get(42L, 100L);

        assertSame(task, res);
        verify(membership).hasAtLeast(7L, 42L, Role.OBSERVER);
    }

    @Test
    void get_forbidden_when_not_member()
    {
        when(membership.hasAtLeast(7L, 42L, Role.OBSERVER)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ctrl.get(42L, 100L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void update_calls_service_when_member()
    {
        when(membership.hasAtLeast(7L, 42L, Role.MEMBER)).thenReturn(true);
        Task updated = new Task(); when(tasks.update(anyLong(), any(), any(), any(), any(), any(), any(), any())).thenReturn(updated);

        UpdateTaskRequest req = new UpdateTaskRequest(
                "N","D", LocalDate.now().plusDays(1),
                TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, Instant.now()
        );

        Task res = ctrl.update(42L, 100L, req);

        assertSame(updated, res);
        verify(tasks).update(eq(100L), eq(req.name()), eq(req.description()), eq(req.dueDate()),
                eq(req.priority()), eq(req.status()), eq(req.completedAt()), same(current));
    }

    @Test
    void update_forbidden_when_not_member()
    {
        when(membership.hasAtLeast(7L, 42L, Role.MEMBER)).thenReturn(false);

        UpdateTaskRequest req = new UpdateTaskRequest(
                "N","D", LocalDate.now(), TaskPriority.LOW, TaskStatus.TODO, null
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ctrl.update(42L, 100L, req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verifyNoInteractions(tasks);
    }

    @Test
    void assign_calls_service_when_member()
    {
        when(membership.hasAtLeast(7L, 42L, Role.MEMBER)).thenReturn(true);
        Task after = new Task(); when(tasks.assign(100L, 5L, current)).thenReturn(after);

        Task res = ctrl.assign(42L, 100L, new AssignTaskRequest(5L));

        assertSame(after, res);
        verify(tasks).assign(100L, 5L, current);
    }

    @Test
    void assign_forbidden_when_not_member()
    {
        when(membership.hasAtLeast(7L, 42L, Role.MEMBER)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ctrl.assign(42L, 100L, new AssignTaskRequest(5L)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(tasks, never()).assign(anyLong(), anyLong(), any());
    }
}
