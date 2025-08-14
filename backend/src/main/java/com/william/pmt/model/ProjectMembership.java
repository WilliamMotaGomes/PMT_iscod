package com.william.pmt.model;

import jakarta.persistence.*;

@Entity @Table(name = "project_memberships",
  uniqueConstraints = @UniqueConstraint(columnNames = {"project_id","user_id"}))
public class ProjectMembership
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="project_id")
    private Project project;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Project getProject() { return project; }
    public void setProject(Project p) { this.project = p; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public Role getRole(){ return role; }
    public void setRole(Role r){ this.role = r; }
}
