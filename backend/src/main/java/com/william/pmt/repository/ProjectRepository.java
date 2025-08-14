package com.william.pmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.william.pmt.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {}
