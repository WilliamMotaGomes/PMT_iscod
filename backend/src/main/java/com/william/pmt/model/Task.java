package com.william.pmt.model;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity @Table(name="tasks")
public class Task
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="project_id")
    private Project project;

    private String name;

    @Column(columnDefinition="TEXT")
    private String description;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @ManyToOne @JoinColumn(name="assigned_to")
    private User assignedTo;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant completedAt;

    @PreUpdate
    public void touch(){ this.updatedAt = Instant.now(); }

    public Long getId(){ return id; }
    public Project getProject(){ return project; }
    public void setProject(Project p){ this.project = p; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getDescription(){ return description; }
    public void setDescription(String d){ this.description = d; }
    public LocalDate getDueDate(){ return dueDate; }
    public void setDueDate(LocalDate dd){ this.dueDate = dd; }
    public TaskPriority getPriority(){ return priority; }
    public void setPriority(TaskPriority p){ this.priority = p; }
    public TaskStatus getStatus(){ return status; }
    public void setStatus(TaskStatus s){ this.status = s; }
    public User getAssignedTo(){ return assignedTo; }
    public void setAssignedTo(User u){ this.assignedTo = u; }
    public Instant getCompletedAt(){ return completedAt; }
    public void setCompletedAt(Instant c){ this.completedAt = c; }
}
