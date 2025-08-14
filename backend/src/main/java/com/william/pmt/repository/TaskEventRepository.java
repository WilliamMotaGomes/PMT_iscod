package com.william.pmt.repository;

import com.william.pmt.model.TaskEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskEventRepository extends JpaRepository<TaskEvent, Long>
{
    List<TaskEvent> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}