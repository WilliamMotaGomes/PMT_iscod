package com.william.pmt.service;

import com.william.pmt.model.Project;
import com.william.pmt.model.ProjectMembership;
import com.william.pmt.model.Role;
import com.william.pmt.model.User;
import com.william.pmt.repository.ProjectMembershipRepository;
import com.william.pmt.repository.ProjectRepository;
import com.william.pmt.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ProjectService
{
    private final ProjectRepository projectRepo;
    private final ProjectMembershipRepository membershipRepo;
    private final UserRepository userRepo;
    private final NotificationService notification;

    public ProjectService(ProjectRepository projectRepo,
                          ProjectMembershipRepository membershipRepo,
                          UserRepository userRepo,
                          NotificationService notification)
    {
        this.projectRepo = projectRepo;
        this.membershipRepo = membershipRepo;
        this.userRepo = userRepo;
        this.notification = notification;
    }

    /** Créer un projet et rends son createur ADMIN dessus */
    public Project createProject(Long creatorId, String name, String description, LocalDate startDate)
    {
        Project p = new Project();
        p.setName(name);
        p.setDescription(description);
        p.setStartDate(startDate);
        p = projectRepo.save(p);

        User creator = userRepo.findById(creatorId).orElseThrow();
        ProjectMembership m = new ProjectMembership();
        m.setProject(p);
        m.setUser(creator);
        m.setRole(Role.ADMIN);
        membershipRepo.save(m);

        return p;
    }

    /** Invite un utilisateur à rejoindre le projet depuis son mail d'inscription sur la plateforme*/
    public void inviteMember(Project project, String email)
    {
        if (email == null || email.isBlank())
            return;

        Optional<User> maybeUser = userRepo.findByEmail(email);

        if (maybeUser.isPresent())
        {
            User user = maybeUser.get();

            if (membershipRepo.findByProjectIdAndUserId(project.getId(), user.getId()).isEmpty())
            {
                ProjectMembership m = new ProjectMembership();
                m.setProject(project);
                m.setUser(user);
                m.setRole(Role.OBSERVER);
                membershipRepo.save(m);
            }
        }
    }
}