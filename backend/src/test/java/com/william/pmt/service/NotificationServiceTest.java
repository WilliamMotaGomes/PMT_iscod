package com.william.pmt.service;

import com.william.pmt.model.Task;
import com.william.pmt.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest
{

    private static void setField(Object target, String name, Object value) throws Exception
    {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void sendEmailFrom_uses_actor_email_as_from_and_replyTo()
    {
        JavaMailSender sender = mock(JavaMailSender.class);
        NotificationService svc = new NotificationService(sender);

        svc.sendEmailFrom("actor@example.com", "to@example.com", "Subj", "Body");

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(cap.capture());
        SimpleMailMessage msg = cap.getValue();
        assertArrayEquals(new String[]{"to@example.com"}, msg.getTo());
        assertEquals("Subj", msg.getSubject());
        assertEquals("Body", msg.getText());
        assertEquals("actor@example.com", msg.getFrom());
        assertEquals("actor@example.com", msg.getReplyTo());
    }

    @Test
    void sendEmailFrom_null_actor_uses_appFrom_and_no_replyTo() throws Exception
    {
        JavaMailSender sender = mock(JavaMailSender.class);
        NotificationService svc = new NotificationService(sender);
        setField(svc, "appFrom", "noreply@pmt.local");

        svc.sendEmailFrom(null, "to@example.com", "S", "B");

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(cap.capture());
        SimpleMailMessage msg = cap.getValue();
        assertEquals("noreply@pmt.local", msg.getFrom());
        assertNull(msg.getReplyTo());
    }

    @Test
    void sendEmailFrom_retries_with_fallback_when_first_send_fails() throws Exception
    {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new RuntimeException("boom")).doNothing().when(sender).send(any(SimpleMailMessage.class));

        NotificationService svc = new NotificationService(sender);
        setField(svc, "appFrom", "fallback@pmt.local");

        svc.sendEmailFrom("actor@example.com", "to@example.com", "S", "B");

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender, times(2)).send(cap.capture());
        SimpleMailMessage first = cap.getAllValues().get(0);
        SimpleMailMessage second = cap.getAllValues().get(1);

        assertEquals("actor@example.com", first.getFrom());
        assertEquals("actor@example.com", first.getReplyTo());
        assertEquals("fallback@pmt.local", second.getFrom());

        assertEquals("actor@example.com", second.getReplyTo());
    }

    @Test
    void sendTaskAssigned_skips_when_assignee_has_no_email()
    {
        JavaMailSender sender = mock(JavaMailSender.class);
        NotificationService svc = new NotificationService(sender);

        Task t = new Task();
        t.setName("T");
        User assignee = new User();
        svc.sendTaskAssigned(new User(), assignee, t);

        verifyNoInteractions(sender);
    }
}
