package com.william.pmt.dto;

import java.time.LocalDate;

public record CreateProjectDto(String name, String description, LocalDate dueDate) { }
