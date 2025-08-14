package com.william.pmt.dto;

import java.time.LocalDate;

public record CreateProjectRequest(String name, String description, LocalDate dueDate) {}