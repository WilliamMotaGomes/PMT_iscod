package com.william.pmt.service;

import com.william.pmt.model.Task;
import com.william.pmt.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationService
{
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Nullable
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String appFrom;

    @Value("${spring.mail.username:}")
    private String smtpUser;

    public NotificationService(@Autowired(required = false) JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    private String fallbackFrom()
    {
        if (StringUtils.hasText(appFrom)) return appFrom;

        if (StringUtils.hasText(smtpUser)) return smtpUser;

        return "no-reply@pmt.local";
    }

    /** Envoie un mail à la personne(to) avec un titre(subject) et le contenue(text) */
    public void sendEmailFrom(@Nullable String actorEmail, String to, String subject, String text)
    {
        if (mailSender == null)
        {
            log.info("[no-mail] to='{}' subject='{}' text='{}' (from='{}')", to, subject, text, actorEmail);

            return;
        }

        String initialFrom = StringUtils.hasText(actorEmail) ? actorEmail : fallbackFrom();

        try
        {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            msg.setFrom(initialFrom);

            if (StringUtils.hasText(actorEmail))
            {
                msg.setReplyTo(actorEmail);
            }

            mailSender.send(msg);
        }
        catch (Exception e1)
        {
            log.warn("Echec envoie du mail avec from='{}' ({}). Essaye avec fallback mail", initialFrom, e1.getMessage());

            try
            {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(to);
                msg.setSubject(subject);
                msg.setText(text);
                String fb = fallbackFrom();
                msg.setFrom(fb);

                if (StringUtils.hasText(actorEmail))
                {
                    msg.setReplyTo(actorEmail);
                }

                mailSender.send(msg);
            }
            catch (Exception e2)
            {
                log.error("Echec envoie mail (fallback). to='{}' subject='{}' err='{}'", to, subject, e2.getMessage());
                log.info("[mail-fallback] to='{}' subject='{}' text='{}' from='{}'", to, subject, text, initialFrom);
            }
        }
    }

    /** Prepare le mail à envoyer quand une tache est assigné à un utilisateur */
    public void sendTaskAssigned(User actor, User assignee, Task task)
    {
        if (assignee == null || !StringUtils.hasText(assignee.getEmail()))
        {
            log.info("[no-mail] pas de mail trouvé #{}", (task != null ? task.getId() : null));

            return;
        }

        String actorEmail = (actor != null) ? actor.getEmail() : null;
        String subject = "Nouvelle tâche assignée : #" + task.getId() + " — " + task.getName();
        StringBuilder body = new StringBuilder();
        body.append("Bonjour !").append(assignee.getUsername() != null ? assignee.getUsername() : "").append(",\n\n")
            .append("Vous avez été assigné à la tâche #").append(task.getId()).append(" (").append(task.getName()).append(").\n")
            .append("Projet : ").append(task.getProject() != null ? task.getProject().getName() : "-").append("\n")
            .append("Statut : ").append(task.getStatus()).append("\n");

        if (task.getDueDate() != null)
        {
            body.append("Échéance: ").append(task.getDueDate()).append("\n");
        }

        body.append("\nMessage automatique — vous pouvez répondre à cet e-mail, la réponse ira à l'assignant.");
        sendEmailFrom(actorEmail, assignee.getEmail(), subject, body.toString());
    }
}