package com.william.pmt.dto;

import java.time.*;
import com.william.pmt.model.*;

public record UpdateTaskRequest(String name, String description, LocalDate dueDate, TaskPriority priority, TaskStatus status, Instant completedAt) {}
