package com.william.pmt.repository;

import com.william.pmt.model.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long>
{
    List<TaskHistory> findByTaskIdOrderByAtDesc(Long taskId);

    default List<TaskHistory> findByTaskId(Long taskId) {
        return findByTaskIdOrderByAtDesc(taskId);
    }
}
