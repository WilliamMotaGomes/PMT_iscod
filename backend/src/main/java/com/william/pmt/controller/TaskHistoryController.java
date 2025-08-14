package com.william.pmt.controller;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import com.william.pmt.dto.TaskHistoryDto;
import com.william.pmt.model.TaskHistory;
import com.william.pmt.repository.TaskHistoryRepository;

@RestController
@RequestMapping("/api/tasks")
public class TaskHistoryController
{
    private final TaskHistoryRepository repository;

    public TaskHistoryController(TaskHistoryRepository repository)
    {
        this.repository = repository;
    }

    /** Récupère l'historique d'une tâche depuis son ID */
    @GetMapping("/{taskId}/history")
    public List<TaskHistoryDto> history(@PathVariable Long taskId)
    {
        return repository.findByTaskIdOrderByAtDesc(taskId)
                .stream()
                .map(TaskHistoryDto::from)
                .collect(Collectors.toList());
    }

    /** Ajoute une ligne dans l'historique d'une tâche depuis son ID */
    @PostMapping("/{taskId}/history")
    public TaskHistoryDto append(@PathVariable Long taskId, @RequestBody CreateRequest body)
    {
        TaskHistory h = new TaskHistory();

        h.setTaskId(taskId);
        h.setAuthorId(body.authorId);
        h.setAt(Instant.now());
        h.setAction(body.action);
        h.setField(body.field);
        h.setOldValue(body.oldValue);
        h.setNewValue(body.newValue);
        TaskHistory saved = repository.save(h);

        return TaskHistoryDto.from(saved);
    }

    public static class CreateRequest
    {
        public Long authorId;
        public String action;
        public String field;
        public String oldValue;
        public String newValue;
    }
}
