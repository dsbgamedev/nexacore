package util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jakarta.servlet.ServletContext;

public class EmailUtil {

    private static Properties emailProperties = new Properties();
    private static boolean initialized = false;

    private EmailUtil() {}

    public static synchronized void initialize(ServletContext servletContext) {
        if (initialized) {
            System.out.println("EmailUtil já inicializado.");
            return;
        }

        InputStream input = null;
        try {
            // Tenta carregar usando o ServletContext (caminho preferencial para webapps)
            // O getResourceAsStream para ServletContext espera um caminho que começa com /
            // e se refere à raiz do contexto da web.
            // Se email.properties está em WEB-INF/classes, o caminho é "/WEB-INF/classes/email.properties"
            input = servletContext.getResourceAsStream("/WEB-INF/classes/email.properties");
            
            if (input == null) {
                // Se não encontrou via ServletContext, tenta carregar usando o ClassLoader
                // Isso é um fallback útil para ambientes de desenvolvimento onde 'resources'
                // pode ser diretamente no classpath.
                input = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties");
            }

            if (input == null) {
                System.err.println("Desculpe, não foi possível encontrar email.properties usando ServletContext ou ClassLoader.");
                throw new RuntimeException("Falha na inicialização do EmailUtil: email.properties não encontrado.");
            }
            
            // Usar try-with-resources AQUI para garantir que o InputStream seja fechado
            try (InputStream finalInput = input) { // Usa uma nova variável final para o try-with-resources
                emailProperties.load(finalInput);
            }
            System.out.println("Configurações de e-mail carregadas com sucesso.");
            initialized = true;
        } catch (IOException ex) {
            System.err.println("Erro ao carregar propriedades de e-mail: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Erro ao carregar propriedades de e-mail.", ex);
        } catch (Exception ex) {
            System.err.println("Erro inesperado na inicialização do EmailUtil: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Erro inesperado na inicialização do EmailUtil.", ex);
        }
    }

    public static void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        if (!initialized) {
            throw new IllegalStateException("EmailUtil não foi inicializado. Chame EmailUtil.initialize(ServletContext) primeiro.");
        }

        if (!emailProperties.containsKey("mail.smtp.host") ||
            !emailProperties.containsKey("mail.smtp.port") ||
            !emailProperties.containsKey("mail.smtp.username") ||
            !emailProperties.containsKey("mail.smtp.password")) {
            throw new MessagingException("Configurações de SMTP incompletas no email.properties.");
        }

        String smtpHost = emailProperties.getProperty("mail.smtp.host");
        String smtpPort = emailProperties.getProperty("mail.smtp.port");
        String smtpUsername = emailProperties.getProperty("mail.smtp.username");
        String smtpPassword = emailProperties.getProperty("mail.smtp.password");
        String fromName = emailProperties.getProperty("mail.smtp.from.name", "CadastroWeb");

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", emailProperties.getProperty("mail.smtp.auth", "true"));
        props.put("mail.smtp.starttls.enable", emailProperties.getProperty("mail.smtp.starttls.enable", "true"));
        props.put("mail.smtp.ssl.protocols", emailProperties.getProperty("mail.smtp.ssl.protocols", "TLSv1.2"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUsername, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8", "plain");

            Transport.send(message);
            System.out.println("E-mail enviado com sucesso para: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("Erro inesperado ao criar ou enviar e-mail: " + e.getMessage());
            e.printStackTrace();
            throw new MessagingException("Erro inesperado ao enviar e-mail.", e);
        }
    }
}

