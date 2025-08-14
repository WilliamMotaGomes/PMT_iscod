package com.william.pmt.service;

import com.william.pmt.model.TaskHistory;
import com.william.pmt.repository.TaskHistoryRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TaskServiceHistoryTest
{

    @Test
    void history_should_delegate_to_repo()
    {
        var taskRepo = mock(com.william.pmt.repository.TaskRepository.class);
        var historyRepo = mock(TaskHistoryRepository.class);
        var userRepo = mock(com.william.pmt.repository.UserRepository.class);
        var notif = mock(NotificationService.class);

        var service = new TaskService(taskRepo, historyRepo, userRepo, notif);

        var expected = List.of(new TaskHistory(), new TaskHistory());
        when(historyRepo.findByTaskId(42L)).thenReturn(expected);

        var list = service.history(42L);
        assertEquals(2, list.size());
        verify(historyRepo).findByTaskId(42L);
    }
}
