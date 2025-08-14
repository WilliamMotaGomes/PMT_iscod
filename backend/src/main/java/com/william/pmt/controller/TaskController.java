package com.william.pmt.controller;

import com.william.pmt.dto.UpdateTaskRequest;
import com.william.pmt.dto.AssignTaskRequest;
import com.william.pmt.model.*;
import com.william.pmt.repository.ProjectRepository;
import com.william.pmt.repository.TaskRepository;
import com.william.pmt.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TaskController extends BaseController {

    private final ProjectRepository projectRepo;
    private final TaskRepository taskRepo;
    private final TaskService tasks;
    private final MembershipService membership;

    public TaskController(UserService users,
                          ProjectRepository projectRepo,
                          TaskRepository taskRepo,
                          TaskService tasks,
                          MembershipService membership)
    {
        super(users);

        this.projectRepo = projectRepo;
        this.taskRepo = taskRepo;
        this.tasks = tasks;
        this.membership = membership;
    }

    /** Récupère la tâche depuis son ID */
    @GetMapping("/tasks/{taskId}")
    public Task get(@RequestHeader(name = "X-User-Id") Long userId,
                    @PathVariable(name = "taskId") Long taskId)
    {
        var user = currentUser(userId);
        var task = taskRepo.findById(taskId).orElseThrow();
        var project = task.getProject();

        if (!membership.hasAtLeast(project.getId(), user.getId(), Role.OBSERVER))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return task;
    }

    /** Modifie la tâche depuis son ID */
    @PutMapping("/tasks/{taskId}")
    public Task update(@RequestHeader(name = "X-User-Id") Long userId,
                       @PathVariable(name = "taskId") Long taskId,
                       @RequestBody UpdateTaskRequest req)
    {
        var user = currentUser(userId);
        var existing = taskRepo.findById(taskId).orElseThrow();
        var project = existing.getProject();

        if (!membership.hasAtLeast(project.getId(), user.getId(), Role.MEMBER))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return tasks.update(taskId, req.name(), req.description(), req.dueDate(),
                req.priority(), req.status(), req.completedAt(), user);
    }

    /** Modifie le role d'un utilisateur sur un projet (ADMIN, MEMBER..) */
    @PostMapping("/tasks/{taskId}/assign")
    public Task assign(@RequestHeader(name = "X-User-Id") Long userId,
                       @PathVariable(name = "taskId") Long taskId,
                       @RequestBody AssignTaskRequest req)
    {
        var user = currentUser(userId);
        var existing = taskRepo.findById(taskId).orElseThrow();
        var project = existing.getProject();

        if (!membership.hasAtLeast(project.getId(), user.getId(), Role.MEMBER))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return tasks.assign(taskId, req.userId(), user);
    }
}