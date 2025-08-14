package com.william.pmt.service;

import com.william.pmt.model.*;
import com.william.pmt.repository.TaskEventRepository;
import com.william.pmt.repository.TaskHistoryRepository;
import com.william.pmt.repository.TaskRepository;
import com.william.pmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TaskService
{
    private final TaskRepository taskRepo;
    private final TaskHistoryRepository historyRepo;
    private final UserRepository userRepo;
    private final NotificationService notification;

    @Autowired
    private TaskEventRepository eventRepo;

    public TaskService(TaskRepository taskRepo,
                       TaskHistoryRepository historyRepo,
                       UserRepository userRepo,
                       NotificationService notification)
    {
        this.taskRepo = taskRepo;
        this.historyRepo = historyRepo;
        this.userRepo = userRepo;
        this.notification = notification;
    }

    private String labelOf(String field)
    {
        if ("name".equals(field)) return "titre";
        if ("description".equals(field)) return "description";
        if ("dueDate".equals(field)) return "échéance";
        if ("priority".equals(field)) return "priorité";
        if ("status".equals(field)) return "statut";
        if ("completedAt".equals(field)) return "date de fin";
        if ("assignedTo".equals(field)) return "assignée à";
        return field;
    }

    private String sval(Object v)
    {
        if (v == null) return "—";
        return String.valueOf(v);
    }

    /** Crée l'objet qui sera envoye dans la DB */
    private void logEvent(Task t, String field, Object oldVal, Object newVal, String actorUsername)
    {
        TaskEvent e = new TaskEvent();

        e.setTask(t);
        e.setField(field);
        e.setOldValue(sval(oldVal));
        e.setNewValue(sval(newVal));
        e.setMessage(labelOf(field) + " : " + sval(oldVal) + " -> " + sval(newVal));
        e.setCreatedAt(Instant.now());
        e.setActorUsername(actorUsername);
        eventRepo.save(e);
    }

    /** Crée une tache */
    public Task create(Project p, String name, String description, LocalDate dueDate, TaskPriority priority, User actor)
    {
        Task t = new Task();

        t.setProject(p);
        t.setName(name);
        t.setDescription(description);
        t.setDueDate(dueDate);
        t.setPriority(priority);
        t = taskRepo.save(t);
        logEvent(t, "created", null, "Création par " + actor.getUsername(), actor.getUsername());

        return t;
    }

    /** Modifie le contenu d'une tache */
    public Task update(Long taskId, String name, String description, LocalDate dueDate,
                       TaskPriority priority, TaskStatus status, Instant completedAt, User actor)
    {
        Task t = taskRepo.findById(taskId).orElseThrow();

        String oldName = t.getName();
        String oldDesc = t.getDescription();
        LocalDate oldDue = t.getDueDate();
        TaskPriority oldPrio = t.getPriority();
        TaskStatus oldStatus = t.getStatus();
        Instant oldCompleted = t.getCompletedAt();

        if (name != null) t.setName(name);
        if (description != null) t.setDescription(description);
        if (dueDate != null) t.setDueDate(dueDate);
        if (priority != null) t.setPriority(priority);
        if (status != null) t.setStatus(status);
        if (completedAt != null) t.setCompletedAt(completedAt);
        t = taskRepo.save(t);

        if (name != null && !Objects.equals(oldName, t.getName())) logEvent(t, "name", oldName, t.getName(), actor.getUsername());
        if (description != null && !Objects.equals(oldDesc, t.getDescription())) logEvent(t, "description", oldDesc, t.getDescription(), actor.getUsername());
        if (dueDate != null && !Objects.equals(oldDue, t.getDueDate())) logEvent(t, "dueDate", oldDue, t.getDueDate(), actor.getUsername());
        if (priority != null && !Objects.equals(oldPrio, t.getPriority())) logEvent(t, "priority", oldPrio, t.getPriority(), actor.getUsername());
        if (status != null && !Objects.equals(oldStatus, t.getStatus())) logEvent(t, "status", oldStatus, t.getStatus(), actor.getUsername());
        if (completedAt != null && !Objects.equals(oldCompleted, t.getCompletedAt())) logEvent(t, "completedAt", oldCompleted, t.getCompletedAt(), actor.getUsername());

        return t;
    }

    /** Assigne une tache à un utilisateur */
    public Task assign(Long taskId, Long assigneeUserId, User actor)
    {
        Task t = taskRepo.findById(taskId).orElseThrow();
        User assignee = userRepo.findById(assigneeUserId).orElseThrow();
        User old = t.getAssignedTo();
        t.setAssignedTo(assignee);
        t = taskRepo.save(t);

        notification.sendTaskAssigned(actor, assignee, t);

        String oldName = (old != null) ? old.getUsername() : null;
        String newName = (assignee != null) ? assignee.getUsername() : null;
        logEvent(t, "assignedTo", oldName, newName, actor.getUsername());

        return t;
    }

    /** Recupère l'historique de la tache */
    public List<TaskHistory> history(Long taskId)
    {
        return historyRepo != null ? historyRepo.findByTaskId(taskId) : java.util.Collections.emptyList();
    }
}