package br.edu.reserva.service;

import br.edu.reserva.model.Reserva;
import br.edu.reserva.model.Usuario;
import br.edu.reserva.strategy.PoliticaDeReserva;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IServicoDeReserva {

    Optional<Reserva> criarReserva(Usuario usuario, String salaId,
                                   LocalDateTime inicio, LocalDateTime fim);

    boolean modificarReserva(String reservaId,
                             LocalDateTime novoInicio, LocalDateTime novoFim);

    boolean cancelarReserva(String reservaId);

    void setPolitica(PoliticaDeReserva politica);

    PoliticaDeReserva getPolitica();
}
