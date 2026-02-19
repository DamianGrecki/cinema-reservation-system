package pl.dgrecki.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketPdfDto(
        String movieTitle,
        String movieFormat,
        String movieLanguage,
        LocalDateTime screeningStart,
        String cinemaName,
        String city,
        String hall,
        int row,
        int seat,
        BigDecimal price,
        UUID ticketNumber,
        String qrCodeBase64) {}
