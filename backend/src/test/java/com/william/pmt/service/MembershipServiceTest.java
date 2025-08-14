package com.william.pmt.service;

import com.william.pmt.model.Project;
import com.william.pmt.model.ProjectMembership;
import com.william.pmt.model.Role;
import com.william.pmt.model.User;
import com.william.pmt.repository.ProjectMembershipRepository;
import com.william.pmt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.lang.reflect.Field;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MembershipServiceTest
{
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

    @Test
    void hasAtLeast_returnsFalse_whenNoMembership()
    {
        var mRepo = mock(ProjectMembershipRepository.class);
        when(mRepo.findByProjectIdAndUserId(7L, 2L)).thenReturn(Optional.empty());

        var svc = new MembershipService(mRepo, mock(UserRepository.class));
        assertFalse(svc.hasAtLeast(7L, 2L, Role.OBSERVER));
    }

    @Test
    void hasAtLeast_admin_onlyTrue_forAdmin()
    {
        var m = new ProjectMembership(); m.setRole(Role.ADMIN);
        var mRepo = mock(ProjectMembershipRepository.class);
        when(mRepo.findByProjectIdAndUserId(1L, 10L)).thenReturn(Optional.of(m));

        var svc = new MembershipService(mRepo, mock(UserRepository.class));

        assertTrue(svc.hasAtLeast(1L, 10L, Role.ADMIN));
        assertTrue(svc.hasAtLeast(1L, 10L, Role.MEMBER));
        assertTrue(svc.hasAtLeast(1L, 10L, Role.OBSERVER));
    }

    @Test
    void hasAtLeast_member_trueForMemberOrObserver_falseForAdmin()
    {
        var m = new ProjectMembership(); m.setRole(Role.MEMBER);
        var mRepo = mock(ProjectMembershipRepository.class);
        when(mRepo.findByProjectIdAndUserId(2L, 20L)).thenReturn(Optional.of(m));

        var svc = new MembershipService(mRepo, mock(UserRepository.class));

        assertFalse(svc.hasAtLeast(2L, 20L, Role.ADMIN));
        assertTrue(svc.hasAtLeast(2L, 20L, Role.MEMBER));
        assertTrue(svc.hasAtLeast(2L, 20L, Role.OBSERVER));
    }

    @Test
    void hasAtLeast_observer_onlyTrueForObserver()
    {
        var m = new ProjectMembership(); m.setRole(Role.OBSERVER);
        var mRepo = mock(ProjectMembershipRepository.class);
        when(mRepo.findByProjectIdAndUserId(3L, 30L)).thenReturn(Optional.of(m));

        var svc = new MembershipService(mRepo, mock(UserRepository.class));

        assertFalse(svc.hasAtLeast(3L, 30L, Role.ADMIN));
        assertFalse(svc.hasAtLeast(3L, 30L, Role.MEMBER));
        assertTrue(svc.hasAtLeast(3L, 30L, Role.OBSERVER));
    }

    @Test
    void setRole_updates_existing_membership()
    {
        var mRepo = mock(ProjectMembershipRepository.class);
        var uRepo = mock(UserRepository.class);
        var svc = new MembershipService(mRepo, uRepo);

        var project = new Project(); setId(project, 7L);
        Long userId = 3L;
        var user = new User(); setId(user, userId);

        var existing = new ProjectMembership();
        existing.setProject(project);
        existing.setUser(user);
        existing.setRole(Role.OBSERVER);

        when(uRepo.findById(userId)).thenReturn(Optional.of(user));
        when(mRepo.findByProjectIdAndUserId(7L, 3L)).thenReturn(Optional.of(existing));

        svc.setRole(project, userId, Role.ADMIN);

        verify(mRepo).save(existing);
        assertEquals(Role.ADMIN, existing.getRole());
    }

    @Test
    void setRole_creates_when_absent()
    {
        var mRepo = mock(ProjectMembershipRepository.class);
        var uRepo = mock(UserRepository.class);
        var svc = new MembershipService(mRepo, uRepo);

        var project = new Project(); setId(project, 7L);
        Long userId = 4L;
        var user = new User(); setId(user, userId);

        when(uRepo.findById(userId)).thenReturn(Optional.of(user));
        when(mRepo.findByProjectIdAndUserId(7L, 4L)).thenReturn(Optional.empty());

        svc.setRole(project, userId, Role.MEMBER);

        var cap = ArgumentCaptor.forClass(ProjectMembership.class);
        verify(mRepo).save(cap.capture());
        var saved = cap.getValue();

        assertEquals(Role.MEMBER, saved.getRole());
        assertEquals(project, saved.getProject());
        assertEquals(user, saved.getUser());
    }

    @Test
    void setRole_throws_whenUserMissing()
    {
        var memberships = mock(ProjectMembershipRepository.class);
        var users = mock(UserRepository.class);
        var svc = new MembershipService(memberships, users);

        when(users.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> svc.setRole(new Project(), 99L, Role.MEMBER));

        verify(memberships, never()).save(any());
    }
}
