package com.william.pmt.controller;

import com.william.pmt.dto.CreateProjectRequest;
import com.william.pmt.model.*;
import com.william.pmt.repository.ProjectMembershipRepository;
import com.william.pmt.repository.ProjectRepository;
import com.william.pmt.repository.TaskRepository;
import com.william.pmt.repository.UserRepository;
import com.william.pmt.service.ProjectService;
import com.william.pmt.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectControllerTest
{
    private ProjectRepository projectRepo;
    private UserRepository users;
    private ProjectMembershipRepository memberships;
    private ProjectService projects;
    private TaskRepository tasks;
    private TaskService taskService;
    private ProjectController ctrl;

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

    @BeforeEach
    void setUp()
    {
        projectRepo  = mock(ProjectRepository.class);
        users        = mock(UserRepository.class);
        memberships  = mock(ProjectMembershipRepository.class);
        projects     = mock(ProjectService.class);
        tasks        = mock(TaskRepository.class);
        taskService  = mock(TaskService.class);

        ctrl = new ProjectController(projectRepo, users, memberships, projects, tasks, taskService);
    }

    @Test
    void create_should_return_2xx_and_persist()
    {
        Long userId = 1L;
        CreateProjectRequest dto = new CreateProjectRequest("N", "D", LocalDate.now().plusDays(1));

        when(projects.createProject(eq(userId), eq("N"), eq("D"), any(LocalDate.class)))
                .thenReturn(new Project());

        var resp = ctrl.create(userId, dto);

        verify(projects).createProject(eq(userId), eq("N"), eq("D"), any(LocalDate.class));
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
    }

    @Test
    void listTasks_returns_all_when_status_is_null()
    {
        long projectId = 7L;
        when(tasks.findByProjectId(projectId)).thenReturn(List.of(new Task(), new Task()));

        List<Task> out = ctrl.listTasks(projectId, null);

        verify(tasks).findByProjectId(projectId);
        verify(tasks, never()).findByProjectIdAndStatus(anyLong(), any());
        assertEquals(2, out.size());
    }

    @Test
    void listTasks_filters_when_status_is_provided()
    {
        long projectId = 7L;
        when(tasks.findByProjectIdAndStatus(projectId, TaskStatus.TODO))
                .thenReturn(List.of(new Task()));

        List<Task> out = ctrl.listTasks(projectId, TaskStatus.TODO);

        verify(tasks).findByProjectIdAndStatus(projectId, TaskStatus.TODO);
        verify(tasks, never()).findByProjectId(anyLong());
        assertEquals(1, out.size());
    }

    @Test
    void meInProject_returns_200_when_membership_exists()
    {
        long userId = 5L, projectId = 9L;
        ProjectMembership m = new ProjectMembership();
        when(memberships.findByProjectIdAndUserId(projectId, userId)).thenReturn(Optional.of(m));

        ResponseEntity<ProjectMembership> resp = ctrl.meInProject(userId, projectId);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(m, resp.getBody());
    }

    @Test
    void meInProject_returns_404_when_membership_absent()
    {
        when(memberships.findByProjectIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        ResponseEntity<ProjectMembership> resp = ctrl.meInProject(1L, 2L);

        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void myProjects_returns_projects_from_memberships()
    {
        long userId = 11L;
        User me = new User(); setId(me, userId);
        when(users.findById(userId)).thenReturn(Optional.of(me));

        Project p1 = new Project(); setId(p1, 1L);
        Project p2 = new Project(); setId(p2, 2L);

        ProjectMembership m1 = new ProjectMembership(); m1.setProject(p1);
        ProjectMembership m2 = new ProjectMembership(); m2.setProject(p2);

        when(memberships.findByUserId(userId)).thenReturn(List.of(m1, m2));

        List<Project> out = ctrl.myProjects(userId);

        assertEquals(2, out.size());
        assertTrue(out.contains(p1));
        assertTrue(out.contains(p2));
    }

    @Test
    void members_returns_list_from_repo()
    {
        long projectId = 3L;
        when(memberships.findByProjectId(projectId)).thenReturn(List.of(new ProjectMembership(), new ProjectMembership()));

        List<ProjectMembership> out = ctrl.members(projectId);

        assertEquals(2, out.size());
        verify(memberships).findByProjectId(projectId);
    }

    @Test
    void createTask_uses_default_LOW_priority_when_null()
    {
        long userId = 4L, projectId = 8L;
        User actor = new User(); setId(actor, userId);
        Project project = new Project(); setId(project, projectId);

        when(users.findById(userId)).thenReturn(Optional.of(actor));
        when(projectRepo.findById(projectId)).thenReturn(Optional.of(project));
        when(taskService.create(any(), anyString(), anyString(), any(), any(), any()))
                .thenAnswer(i ->
                {
                    Task t = new Task();
                    t.setName(i.getArgument(1, String.class));
                    t.setPriority(i.getArgument(4, TaskPriority.class));
                    return t;
                });

        ProjectController.CreateTaskBody body = new ProjectController.CreateTaskBody();
        body.name = "T";
        body.description = "Desc";
        body.dueDate = LocalDate.now().plusDays(5);
        body.priority = null;

        Task created = ctrl.createTask(userId, projectId, body);

        ArgumentCaptor<TaskPriority> prioCap = ArgumentCaptor.forClass(TaskPriority.class);
        verify(taskService).create(eq(project), eq("T"), eq("Desc"), any(LocalDate.class), prioCap.capture(), eq(actor));
        assertEquals(TaskPriority.LOW, prioCap.getValue());
        assertEquals("T", created.getName());
    }

    @Test
    void invite_returns_existing_membership_when_present()
    {
        long projectId = 2L;
        Project p = new Project(); setId(p, projectId);
        when(projectRepo.findById(projectId)).thenReturn(Optional.of(p));

        User u = new User(); setId(u, 99L);
        when(users.findByEmail("a@x.io")).thenReturn(Optional.of(u));

        ProjectMembership existing = new ProjectMembership(); existing.setProject(p); existing.setUser(u);
        when(memberships.findByProjectIdAndUserId(projectId, 99L)).thenReturn(Optional.of(existing));

        ProjectController.InviteBody body = new ProjectController.InviteBody();
        body.email = "a@x.io";
        ResponseEntity<ProjectMembership> resp = ctrl.invite(1L, projectId, body);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(existing, resp.getBody());
        verify(memberships, never()).save(any());
    }

    @Test
    void invite_creates_membership_when_absent()
    {
        long projectId = 2L;
        Project p = new Project(); setId(p, projectId);
        when(projectRepo.findById(projectId)).thenReturn(Optional.of(p));

        User u = new User(); setId(u, 50L);
        when(users.findByEmail("b@b.com")).thenReturn(Optional.of(u));
        when(memberships.findByProjectIdAndUserId(projectId, 50L)).thenReturn(Optional.empty());

        ProjectMembership saved = new ProjectMembership();
        when(memberships.save(any(ProjectMembership.class))).thenReturn(saved);

        ProjectController.InviteBody body = new ProjectController.InviteBody();
        body.email = "b@b.com";
        ResponseEntity<ProjectMembership> resp = ctrl.invite(1L, projectId, body);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(saved, resp.getBody());

        ArgumentCaptor<ProjectMembership> cap = ArgumentCaptor.forClass(ProjectMembership.class);
        verify(memberships).save(cap.capture());
        assertEquals(Role.MEMBER, cap.getValue().getRole());
        assertSame(p, cap.getValue().getProject());
        assertSame(u, cap.getValue().getUser());
    }

    @Test
    void setRole_updates_when_membership_exists()
    {
        long projectId = 3L;
        Project p = new Project(); setId(p, projectId);
        when(projectRepo.findById(projectId)).thenReturn(Optional.of(p));

        User u = new User(); setId(u, 12L);
        when(users.findById(12L)).thenReturn(Optional.of(u));

        ProjectMembership existing = new ProjectMembership();
        existing.setProject(p); existing.setUser(u); existing.setRole(Role.OBSERVER);
        when(memberships.findByProjectIdAndUserId(projectId, 12L)).thenReturn(Optional.of(existing));
        when(memberships.save(existing)).thenReturn(existing);

        ProjectController.SetRoleBody body = new ProjectController.SetRoleBody();
        body.userId = 12L;
        body.role   = Role.ADMIN;

        ResponseEntity<ProjectMembership> resp = ctrl.setRole(1L, projectId, body);

        assertEquals(200, resp.getStatusCode().value());
        assertEquals(Role.ADMIN, existing.getRole());
        verify(memberships).save(existing);
    }

    @Test
    void setRole_creates_when_membership_absent()
    {
        long projectId = 4L;
        Project p = new Project(); setId(p, projectId);
        when(projectRepo.findById(projectId)).thenReturn(Optional.of(p));

        User u = new User(); setId(u, 33L);
        when(users.findById(33L)).thenReturn(Optional.of(u));

        when(memberships.findByProjectIdAndUserId(projectId, 33L)).thenReturn(Optional.empty());

        ProjectMembership saved = new ProjectMembership();
        when(memberships.save(any(ProjectMembership.class))).thenReturn(saved);

        ProjectController.SetRoleBody body = new ProjectController.SetRoleBody();
        body.userId = 33L;
        body.role   = Role.MEMBER;

        ResponseEntity<ProjectMembership> resp = ctrl.setRole(1L, projectId, body);

        assertEquals(200, resp.getStatusCode().value());
        assertSame(saved, resp.getBody());

        ArgumentCaptor<ProjectMembership> cap = ArgumentCaptor.forClass(ProjectMembership.class);
        verify(memberships).save(cap.capture());
        ProjectMembership m = cap.getValue();
        assertEquals(Role.MEMBER, m.getRole());
        assertSame(p, m.getProject());
        assertSame(u, m.getUser());
    }
}
