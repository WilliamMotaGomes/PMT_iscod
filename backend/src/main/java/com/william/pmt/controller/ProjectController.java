package com.william.pmt.controller;

import com.william.pmt.dto.CreateProjectRequest;
import com.william.pmt.model.*;
import com.william.pmt.repository.ProjectMembershipRepository;
import com.william.pmt.repository.ProjectRepository;
import com.william.pmt.repository.TaskRepository;
import com.william.pmt.repository.UserRepository;
import com.william.pmt.service.ProjectService;
import com.william.pmt.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin
public class ProjectController {

    private final ProjectRepository projectRepo;
    private final UserRepository users;
    private final ProjectMembershipRepository memberships;
    private final ProjectService projects;
    private final TaskRepository tasks;
    private final TaskService taskService;

    public ProjectController(ProjectRepository projectRepo,
                             UserRepository users,
                             ProjectMembershipRepository memberships,
                             ProjectService projects,
                             TaskRepository tasks,
                             TaskService taskService)
    {
        this.projectRepo = projectRepo;
        this.users = users;
        this.memberships = memberships;
        this.projects = projects;
        this.tasks = tasks;
        this.taskService = taskService;
    }

    /**  Création d’un projet */
    @PostMapping
    public ResponseEntity<Project> create(@RequestHeader("X-User-Id") Long userId,
                                          @RequestBody CreateProjectRequest dto)
    {
        LocalDate due = dto.dueDate();
        Project created = projects.createProject(userId, dto.name(), dto.description(), due);

        return ResponseEntity.ok(created);
    }

    /**  Liste des projets de l’utilisateur  */
    @GetMapping
    public List<Project> myProjects(@RequestHeader("X-User-Id") Long userId)
    {
        User me = users.findById(userId).orElseThrow();
        List<Project> out = new ArrayList<>();

        for (ProjectMembership m : memberships.findByUserId(me.getId()))
        {
            out.add(m.getProject());
        }

        return out;
    }

    /** Renvoie le role de l’utilisateur dans ce projet */
    @GetMapping("/{projectId}/me")
    public ResponseEntity<ProjectMembership> meInProject(@RequestHeader("X-User-Id") Long userId,
                                                         @PathVariable Long projectId)
    {
        return memberships.findByProjectIdAndUserId(projectId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Liste des membres du projet */
    @GetMapping("/{projectId}/members")
    public List<ProjectMembership> members(@PathVariable Long projectId)
    {
        return memberships.findByProjectId(projectId);
    }

    /** Recupere les taches */
    @GetMapping("/{projectId}/tasks")
    public List<Task> listTasks(@PathVariable Long projectId,
                                @RequestParam(name = "status", required = false) TaskStatus status)
    {
        if (status == null)
        {
            return tasks.findByProjectId(projectId);
        }

        return tasks.findByProjectIdAndStatus(projectId, status);
    }

    /** Création d’une tâche dans le projet */
    @PostMapping("/{projectId}/tasks")
    public Task createTask(@RequestHeader("X-User-Id") Long userId,
                           @PathVariable Long projectId,
                           @RequestBody CreateTaskBody body)
    {
        User actor = users.findById(userId).orElseThrow();
        Project project = projectRepo.findById(projectId).orElseThrow();
        LocalDate due = body.dueDate;
        TaskPriority prio = body.priority != null ? body.priority : TaskPriority.LOW;

        return taskService.create(project, body.name, body.description, due, prio, actor);
    }

    /**  Invitation d'un utilisateur dans le projet */
    @PostMapping("/{projectId}/invite")
    public ResponseEntity<ProjectMembership> invite(@RequestHeader("X-User-Id") Long actorId,
                                                    @PathVariable Long projectId,
                                                    @RequestBody InviteBody body)
    {
        var project = projectRepo.findById(projectId).orElseThrow();
        var user = users.findByEmail(body.email).orElseThrow();
        var existing = memberships.findByProjectIdAndUserId(projectId, user.getId());

        if (existing.isPresent())
            return ResponseEntity.ok(existing.get());

        var m = new ProjectMembership();
        m.setProject(project);
        m.setUser(user);
        m.setRole(Role.MEMBER);

        return ResponseEntity.ok(memberships.save(m));
    }

    /** Change le rôle d’un utilisateur dans le projet */
    @PostMapping("/{projectId}/roles")
    public ResponseEntity<ProjectMembership> setRole(@RequestHeader("X-User-Id") Long actorId,
                                                     @PathVariable Long projectId,
                                                     @RequestBody SetRoleBody body)
    {
        var project = projectRepo.findById(projectId).orElseThrow();
        var user    = users.findById(body.userId).orElseThrow();
        var membership = memberships.findByProjectIdAndUserId(projectId, body.userId).orElseGet(() ->
                {
                    var m = new ProjectMembership();
                    m.setProject(project);
                    m.setUser(user);
                    return m;
                });

        membership.setRole(body.role);

        return ResponseEntity.ok(memberships.save(membership));
    }

    /** Corps de la requete qui change le role d'un user */
    public static class SetRoleBody {
        public Long userId;
        public Role role;
    }

    /** Corps de la requete d'invitation a un projet */
    public static class InviteBody
    {
        public String email;
    }

    /** Corps de la requete de creation de tâche */
    public static class CreateTaskBody
    {
        public String name;
        public String description;
        public LocalDate dueDate;
        public TaskPriority priority;
    }
}
