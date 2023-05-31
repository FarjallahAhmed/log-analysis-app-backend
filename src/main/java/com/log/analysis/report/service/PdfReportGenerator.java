package com.log.analysis.report.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.log.analysis.elasticsearch.model.ReportData;

@Service
public class PdfReportGenerator {
	
	
	public void generateReport(ReportData reportData, String outputPath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Add content to the PDF document
            addContent(document, reportData);

            document.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
	
	
	public void addContent(Document document, ReportData reportData) throws DocumentException {
		
		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        // Add title
        Paragraph title = new Paragraph("Analysis Report", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Add total logs and stack trace logs
        PdfPTable logsTable = new PdfPTable(2);
        logsTable.setWidthPercentage(100);
        logsTable.setSpacingBefore(5f);
        logsTable.addCell(createTableCell("Total Logs:", true, subtitleFont));
        logsTable.addCell(createTableCell(Integer.toString(reportData.getTotalLogs()), false, contentFont));
        logsTable.addCell(createTableCell("Stack Trace Logs:", true, subtitleFont));
        logsTable.addCell(createTableCell(Integer.toString(reportData.getStackTraceLogs()), false, contentFont));
        document.add(logsTable);
        document.add(Chunk.NEWLINE);

        // Add date range
        PdfPTable dateRangeTable = new PdfPTable(1);
        dateRangeTable.setWidthPercentage(100);
        dateRangeTable.setSpacingBefore(5f);
        dateRangeTable.addCell(createTableCell("Date Range:", true, subtitleFont));
        dateRangeTable.addCell(createTableCell(reportData.getEarliestDate().substring(0, 10) + " - " + reportData.getLatestDate().substring(0, 10), false, contentFont));
        document.add(dateRangeTable);
        document.add(Chunk.NEWLINE);

        // Add error logs
        PdfPTable errorLogsTable = new PdfPTable(1);
        errorLogsTable.setWidthPercentage(100);
        errorLogsTable.setSpacingBefore(5f);
        errorLogsTable.addCell(createTableCell("Error Logs:", true, subtitleFont));
        errorLogsTable.addCell(createTableCell(Integer.toString(reportData.getErrorLogs()), false, contentFont));
        document.add(errorLogsTable);
        document.add(Chunk.NEWLINE);


        for (String logger : reportData.getTopLoggers()) {
            Paragraph loggerParagraph = new Paragraph(logger, contentFont);
            loggerParagraph.setIndentationLeft(20f);
            document.add(loggerParagraph);
        }

        document.add(Chunk.NEWLINE);

        // Add log level percentages
        Paragraph logLevelParagraph = new Paragraph("Log Level Percentages:", subtitleFont);
        logLevelParagraph.setSpacingBefore(5f);
        document.add(logLevelParagraph);

        PdfPTable logLevelTable = new PdfPTable(2);
        logLevelTable.setWidthPercentage(100);
        logLevelTable.setSpacingBefore(5f);
        logLevelTable.addCell(createTableCell("Log Level", true, subtitleFont));
        logLevelTable.addCell(createTableCell("Percentage", true, subtitleFont));

        for (Map.Entry<String, Double> entry : reportData.getLogLevelPercentages().entrySet()) {
            logLevelTable.addCell(createTableCell(entry.getKey(), false, contentFont));
            logLevelTable.addCell(createTableCell(decimalFormat.format(entry.getValue()) + "%", false, contentFont));
        }

        document.add(logLevelTable);
        document.add(Chunk.NEWLINE);

        // Add error message percentages
        Paragraph errorMessageParagraph = new Paragraph("Error Message Percentages:", subtitleFont);
        errorMessageParagraph.setSpacingBefore(5f);
        document.add(errorMessageParagraph);

        PdfPTable errorMessageTable = new PdfPTable(2);
        errorMessageTable.setWidthPercentage(100);
        errorMessageTable.setSpacingBefore(5f);
        errorMessageTable.addCell(createTableCell("Error Message", true, subtitleFont));
        errorMessageTable.addCell(createTableCell("Percentage", true, subtitleFont));

        
        
        for (Map.Entry<String, Double> entry : reportData.getErrorMessagePercentages().entrySet()) {
            errorMessageTable.addCell(createTableCell(entry.getKey(), false, contentFont));
            errorMessageTable.addCell(createTableCell( decimalFormat.format(entry.getValue()) + "%", false, contentFont));
        }

        document.add(errorMessageTable);
    }

    public PdfPCell createTableCell(String text, boolean isHeader, Font font) {
    	PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBackgroundColor(isHeader ? BaseColor.LIGHT_GRAY : BaseColor.WHITE);
        return cell;
    }
	
	@SuppressWarnings("unchecked")
	public ReportData retrieveData(Map<String, Object> logAnalysisReport) {

	    ReportData reportData = new ReportData();

	    // Add Total Logs
	    int totalLogs = (int) logAnalysisReport.get("totalLogs");
	    reportData.setTotalLogs(totalLogs);

	    // Add Stack Trace Logs
	    int stackTraceLogs = (int) logAnalysisReport.get("stackTraceLogs");
	    reportData.setStackTraceLogs(stackTraceLogs);

	    // Add Latest Date
	    String latestDate = (String) logAnalysisReport.get("latestDate");
	    reportData.setLatestDate(latestDate);

	    // Add Earliest Date
	    String earliestDate = (String) logAnalysisReport.get("earliestDate");
	    reportData.setEarliestDate(earliestDate);

	    // Add Error Logs
	    int errorLogs = (int) logAnalysisReport.get("errorLogs");
	    reportData.setErrorLogs(errorLogs);

	    // Add Top Loggers
	    List<String> topLoggers = (List<String>) logAnalysisReport.get("topLoggers");
	    reportData.setTopLoggers(topLoggers);

	    // Add Log Level Percentages
	    Map<String, Double> logLevelPercentages = (Map<String, Double>) logAnalysisReport.get("logLevelPercentages");
	    reportData.setLogLevelPercentages(logLevelPercentages);

	    // Add Error Message Percentages
	    Map<String, Double> errorMessagePercentages = (Map<String, Double>) logAnalysisReport.get("errorMessagePercentages");
	    reportData.setErrorMessagePercentages(errorMessagePercentages);

	    return reportData;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
