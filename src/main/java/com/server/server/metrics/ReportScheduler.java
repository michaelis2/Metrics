package com.server.server.metrics;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportScheduler {

    @Autowired
    private SystemMetricRepository metricRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String RECIPIENT = "amanda.irawan@gmail.com";


    @Scheduled(cron = "0 */10 * * * *") // every 10 minutes
    public void generateAndSendReport() {
        try {
            String htmlReport = createHtmlReport();
            sendEmailWithReport(htmlReport);
            System.out.println("✅ Sent 7-day system report to " + RECIPIENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createHtmlReport() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        StringBuilder html = new StringBuilder();

        html.append("<html><body>");
        html.append("<h2>System Metrics Report (Last 7 Days)</h2>");
        html.append("<p>Generated: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("</p>");

        for (MetricType type : MetricType.values()) {
            List<SystemMetric> metrics = metricRepository.findByTypeAndTimestampAfter(type, sevenDaysAgo);

            if (metrics.isEmpty()) continue;

            html.append("<h3>").append(type.name()).append("</h3>");
            html.append("<table border='1' cellspacing='0' cellpadding='5' style='border-collapse:collapse;'>");
            html.append("<tr>")
                    .append("<th>Client IP</th>")
                    .append("<th>Type</th>")
                    .append("<th>Value</th>")
                    .append("<th>Timestamp</th>")
                    .append("</tr>");

            for (SystemMetric metric : metrics) {
                html.append("<tr>")
                        .append("<td>").append(metric.getClientIp()).append("</td>")
                        .append("<td>").append(metric.getType()).append("</td>")
                        .append("<td>").append(metric.getValue()).append("</td>")
                        .append("<td>").append(metric.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td>")
                        .append("</tr>");
            }

            html.append("</table><br>");
        }

        html.append("<p>— Monitoring System</p>");
        html.append("</body></html>");

        return html.toString();
    }


    private void sendEmailWithReport(String htmlReport) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(RECIPIENT);
        helper.setSubject("System Metrics Report - Last 7 Days");
        helper.setText(htmlReport, true);

        mailSender.send(message);
    }
}
