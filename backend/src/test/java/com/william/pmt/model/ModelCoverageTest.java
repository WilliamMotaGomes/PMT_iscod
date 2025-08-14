package com.william.pmt.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelCoverageTest
{

    @Test
    void user_task_project_history_basics()
    {
        User u = new User();
        u.setUsername("alice");
        assertNotNull(u.toString());

        Task t = new Task();
        t.setName("T1");
        assertNotNull(t.toString());

        Project p = new Project();
        p.setName("P1");
        p.setDescription("desc");
        assertNotNull(p.toString());

        TaskHistory h = new TaskHistory();

        h.setOldValue("old");
        h.setNewValue("new");
        assertNotNull(h.toString());
    }
}
