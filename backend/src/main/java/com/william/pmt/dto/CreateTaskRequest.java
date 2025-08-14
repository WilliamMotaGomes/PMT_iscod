package com.william.pmt.dto;

import java.time.LocalDate;
import com.william.pmt.model.TaskPriority;

public record CreateTaskRequest(String name, String description, LocalDate dueDate, TaskPriority priority) {}
