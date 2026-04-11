package com.example.LoanManagementApp.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom LocalDate Deserializer
 * Supports multiple date formats for flexible JSON parsing
 * Formats supported:
 * - ISO format: 2000-01-01 or 2000-1-1
 * - US format: 01/01/2000 or 1/1/2000
 * - European format: 01.01.2000 or 1.1.2000
 * - Text format: 2000-01-01
 */
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    // Supported date formatters in order of preference
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE,           // 2000-01-01
        DateTimeFormatter.ofPattern("yyyy-M-d"),    // 2000-1-1
        DateTimeFormatter.ofPattern("M/d/yyyy"),    // 1/1/2000
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),  // 01/01/2000
        DateTimeFormatter.ofPattern("d/M/yyyy"),    // 1/1/2000 (European)
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),  // 01/01/2000 (European)
        DateTimeFormatter.ofPattern("d.M.yyyy"),    // 1.1.2000 (European)
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),  // 01.01.2000 (European)
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),  // 2000-01-01
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),  // 01-01-2000
    };

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getValueAsString();
        
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        dateString = dateString.trim();

        // Try each formatter in order
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Continue to next formatter
                continue;
            }
        }

        // If no formatter works, throw detailed error
        throw new IOException(
            "Unable to parse date: '" + dateString + "'. " +
            "Supported formats: " +
            "ISO (2000-01-01), US (1/1/2000 or 01/01/2000), " +
            "European (1.1.2000 or 01.01.2000), " +
            "Dash separated (01-01-2000)"
        );
    }
}
