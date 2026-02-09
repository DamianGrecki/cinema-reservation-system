package pl.dgrecki.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;
import pl.dgrecki.models.TicketPdf;
import pl.dgrecki.models.entities.*;

@Component
public class TicketPdfMapper {

    public TicketPdf map(Ticket ticket, String qrCodeBase64, ZoneId zoneId) {
        Reservation reservation = ticket.getReservation();
        Screening screening = reservation.getScreening();
        MovieVersion movieVersion = screening.getMovieVersion();
        CinemaHall cinemaHall = screening.getCinemaHall();
        Cinema cinema = cinemaHall.getCinema();
        Seat seat = reservation.getSeat();

        return new TicketPdf(
                movieVersion.getMovie().getTitle(),
                movieVersion.getMovieFormat().toString(),
                movieVersion.getAudioLanguage().toString(),
                LocalDateTime.ofInstant(screening.getStartTime(), zoneId),
                cinema.getName(),
                cinema.getCity(),
                cinemaHall.getName(),
                seat.getHallRow().getRowNumber(),
                seat.getSeatNumber(),
                ticket.getPrice(),
                ticket.getId(),
                qrCodeBase64);
    }
}
