package com.william.pmt.service;

import com.william.pmt.model.Project;
import com.william.pmt.model.ProjectMembership;
import com.william.pmt.model.Role;
import com.william.pmt.model.User;
import com.william.pmt.repository.ProjectMembershipRepository;
import com.william.pmt.repository.ProjectRepository;
import com.william.pmt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest
{

    @Test
    void createProject_saves_entity()
    {
        ProjectRepository pRepo = mock(ProjectRepository.class);
        ProjectMembershipRepository mRepo = mock(ProjectMembershipRepository.class);
        UserRepository uRepo = mock(UserRepository.class);
        NotificationService notif = mock(NotificationService.class);

        ProjectService svc = new ProjectService(pRepo, mRepo, uRepo, notif);

        User owner = new User();
        owner.setUsername("owner");
        when(uRepo.findById(anyLong())).thenReturn(Optional.of(owner));
        when(pRepo.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        Project res = svc.createProject(1L, "N", "D", LocalDate.now().plusDays(1));

        assertEquals("N", res.getName());
        verify(pRepo).save(any(Project.class));
    }

    @Test
    void createProject_creates_admin_membership_for_owner()
    {
        var projects = mock(ProjectRepository.class);
        var memberships = mock(ProjectMembershipRepository.class);
        var users = mock(UserRepository.class);
        var notifications = mock(NotificationService.class);
        var service = new ProjectService(projects, memberships, users, notifications);

        Long ownerId = 9L;
        var owner = new User();
        when(users.findById(ownerId)).thenReturn(Optional.of(owner));

        var savedProject = new Project();
        when(projects.save(any(Project.class))).thenReturn(savedProject);

        service.createProject(ownerId, "New", "Desc", LocalDate.now().plusDays(10));

        verify(projects).save(any(Project.class));

        var cap = ArgumentCaptor.forClass(ProjectMembership.class);
        verify(memberships).save(cap.capture());
        var m = cap.getValue();

        assertEquals(Role.ADMIN, m.getRole());
        assertEquals(savedProject, m.getProject());
        assertEquals(owner, m.getUser());
    }

    @Test
    void inviteMember_returns_early_when_email_null_or_blank()
    {
        var projects = mock(ProjectRepository.class);
        var memberships = mock(ProjectMembershipRepository.class);
        var users = mock(UserRepository.class);
        var notifications = mock(NotificationService.class);
        var service = new ProjectService(projects, memberships, users, notifications);

        var p = new Project();

        service.inviteMember(p, null);
        service.inviteMember(p, "  ");

        verifyNoInteractions(users);
        verifyNoInteractions(memberships);
    }

    @Test
    void inviteMember_does_nothing_when_user_not_found()
    {
        var projects = mock(ProjectRepository.class);
        var memberships = mock(ProjectMembershipRepository.class);
        var users = mock(UserRepository.class);
        var notifications = mock(NotificationService.class);
        var service = new ProjectService(projects, memberships, users, notifications);

        var p = new Project();
        when(users.findByEmail("test@example.com")).thenReturn(Optional.empty());

        service.inviteMember(p, "test@example.com");

        verify(users).findByEmail("test@example.com");
        verifyNoInteractions(memberships);
    }

    @Test
    void inviteMember_does_nothing_when_membership_already_exists()
    {
        var projects = mock(ProjectRepository.class);
        var memberships = mock(ProjectMembershipRepository.class);
        var users = mock(UserRepository.class);
        var notifications = mock(NotificationService.class);
        var service = new ProjectService(projects, memberships, users, notifications);

        Project p = mock(Project.class);
        when(p.getId()).thenReturn(5L);

        User u = mock(User.class);
        when(u.getId()).thenReturn(77L);

        when(users.findByEmail("test@example.com")).thenReturn(Optional.of(u));
        when(memberships.findByProjectIdAndUserId(5L, 77L))
                .thenReturn(Optional.of(new ProjectMembership()));

        service.inviteMember(p, "test@example.com");

        verify(users).findByEmail("test@example.com");
        verify(memberships).findByProjectIdAndUserId(5L, 77L);
        verify(memberships, never()).save(any(ProjectMembership.class));
    }

    @Test
    void inviteMember_creates_observer_membership_when_absent()
    {
        var projects = mock(ProjectRepository.class);
        var memberships = mock(ProjectMembershipRepository.class);
        var users = mock(UserRepository.class);
        var notifications = mock(NotificationService.class);
        var service = new ProjectService(projects, memberships, users, notifications);

        Project p = mock(Project.class);
        when(p.getId()).thenReturn(42L);

        User u = mock(User.class);
        when(u.getId()).thenReturn(100L);

        when(users.findByEmail("test@example.com")).thenReturn(Optional.of(u));
        when(memberships.findByProjectIdAndUserId(42L, 100L)).thenReturn(Optional.empty());

        service.inviteMember(p, "test@example.com");

        var cap = ArgumentCaptor.forClass(ProjectMembership.class);
        verify(memberships).save(cap.capture());
        var saved = cap.getValue();

        assertEquals(Role.OBSERVER, saved.getRole());
        assertEquals(p, saved.getProject());
        assertEquals(u, saved.getUser());
    }
}
