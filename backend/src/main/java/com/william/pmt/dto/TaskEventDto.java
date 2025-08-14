package com.william.pmt.dto;

import java.time.Instant;

public class TaskEventDto
{
    public Long id;
    public String changedField;
    public String oldValue;
    public String newValue;
    public String text;
    public Instant at;
    public String actor;

    public TaskEventDto() {}

    public TaskEventDto(Long id, String changedField, String oldValue, String newValue, String text, Instant at, String actor)
    {
        this.id = id;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.text = text;
        this.at = at;
        this.actor = actor;
    }
}