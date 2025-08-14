package com.william.pmt.service;

import com.william.pmt.model.*;
import com.william.pmt.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipService
{
    private final ProjectMembershipRepository memberships;
    private final UserRepository users;

    public MembershipService(ProjectMembershipRepository memberships, UserRepository users)
    {
        this.memberships = memberships;
        this.users = users;
    }

    /** Verifie si un utilisateur depuis son ID a bien un role sur un projet depuis ID aussi et le renvoie sinon
     * on renvoie false */
    public boolean hasAtLeast(Long projectId, Long userId, Role minRole)
    {
        return memberships.findByProjectIdAndUserId(projectId, userId)
            .map(m ->
            {
                Role r = m.getRole();

                return switch (minRole)
                {
                    case ADMIN -> r == Role.ADMIN;
                    case MEMBER -> r == Role.ADMIN || r == Role.MEMBER;
                    case OBSERVER -> r == Role.ADMIN || r == Role.MEMBER || r == Role.OBSERVER;
                };
            })
            .orElse(false);
    }

    /** Mets à jour le rôle d'un utilisateur pour un projet (où crée le role si il n'en possède pas) */
    @Transactional
    public ProjectMembership setRole(Project project, Long userId, Role role)
    {
        var user = users.findById(userId).orElseThrow();
        var membership = memberships.findByProjectIdAndUserId(project.getId(), userId)
            .orElseGet(() ->
            {
                var m = new ProjectMembership();
                m.setProject(project);
                m.setUser(user);

                return m;
            });

        membership.setRole(role);

        return memberships.save(membership);
    }
}
