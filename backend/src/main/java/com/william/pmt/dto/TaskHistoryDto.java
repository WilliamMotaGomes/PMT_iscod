package com.william.pmt.dto;

import com.william.pmt.model.TaskHistory;
import java.time.Instant;

public class TaskHistoryDto
{
    public Long id;
    public Long authorId;
    public Instant at;
    public String action;
    public String field;
    public String oldValue;
    public String newValue;

    public static TaskHistoryDto from(TaskHistory h)
    {
        TaskHistoryDto d = new TaskHistoryDto();
        d.id = h.getId();
        d.authorId = h.getAuthorId();
        d.at = h.getAt();
        d.action = h.getAction();
        d.field = h.getField();
        d.oldValue = h.getOldValue();
        d.newValue = h.getNewValue();
        return d;
    }
}
