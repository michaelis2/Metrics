package com.server.server.metrics;

import jakarta.mail.MessagingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportScheduler {

    @Autowired
    private SystemMetricRepository metricRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String RECIPIENT = "amanda.irawan@gmail.com";


    @Scheduled(cron = "0 */30 * * * *") // every 30 minutes
    public void generateAndSendReport() {
        try {
            byte[] excelData = createExcelReport();
            sendEmailWithAttachment(excelData);
            System.out.println("✅ Sent 7-day system report to " + RECIPIENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] createExcelReport() throws IOException {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        XSSFWorkbook workbook = new XSSFWorkbook();

        for (MetricType type : MetricType.values()) {
            List<SystemMetric> metrics = metricRepository.findByTypeAndTimestampAfter(type, sevenDaysAgo);

            Sheet sheet = workbook.createSheet(type.name());
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Timestamp");
            header.createCell(1).setCellValue("Client IP");
            header.createCell(2).setCellValue("Value");

            int rowNum = 1;
            for (SystemMetric metric : metrics) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(metric.getTimestamp().toString());
                row.createCell(1).setCellValue(metric.getClientIp());
                row.createCell(2).setCellValue(metric.getValue());
            }

            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void sendEmailWithAttachment(byte[] excelData) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(RECIPIENT);
        helper.setSubject("System Metrics Report - Last 7 Days");
        helper.setText("Hello,\n\nAttached is the latest 7-day system metrics report.\n\nBest regards,\nMonitoring System");
        helper.addAttachment("SystemMetrics_Report_7days.xlsx",
                new org.springframework.core.io.ByteArrayResource(excelData));

        mailSender.send(message);
    }
}
