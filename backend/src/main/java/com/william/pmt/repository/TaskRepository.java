package com.william.pmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.william.pmt.model.*;
import java.util.*;

public interface TaskRepository extends JpaRepository<Task, Long>
{
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    List<Task> findByProjectId(Long projectId);
}
