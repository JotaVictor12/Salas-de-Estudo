package br.edu.reserva.service;

import br.edu.reserva.model.Reserva;
import br.edu.reserva.model.Sala;
import br.edu.reserva.model.StatusReserva;
import br.edu.reserva.model.Usuario;
import br.edu.reserva.observer.GerenciadorEventos;
import br.edu.reserva.singleton.RepositorioReservas;
import br.edu.reserva.strategy.PoliticaDeReserva;
import br.edu.reserva.strategy.PoliticaPrioridadeDocente;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServicoDeReserva implements IServicoDeReserva {

    private PoliticaDeReserva politica;                                   
    private final RepositorioReservas repositorio = RepositorioReservas.getInstancia(); 
    private final GerenciadorEventos  eventos     = GerenciadorEventos.getInstancia();  

    public ServicoDeReserva(PoliticaDeReserva politica) {
        this.politica = politica;
    }

public void setPolitica(PoliticaDeReserva politica) {
        System.out.println("[Sistema] Política alterada para: " + politica.getNome());
        this.politica = politica;
    }

    public PoliticaDeReserva getPolitica() { return politica; }

public Optional<Reserva> criarReserva(Usuario usuario, String salaId,
                                          LocalDateTime inicio, LocalDateTime fim) {
        Optional<Sala> salaOpt = repositorio.buscarSalaPorId(salaId);
        if (salaOpt.isEmpty()) {
            System.out.println("[ERRO] Sala não encontrada: " + salaId);
            return Optional.empty();
        }

        Sala sala  = salaOpt.get();
        List<Reserva> existentes = repositorio.listarTodas();

if (!politica.podeReservar(usuario, sala, inicio, fim, existentes)) {
            System.out.println("[ERRO] Conflito de horário detectado pela política '"
                + politica.getNome() + "'. Reserva não criada.");
            return Optional.empty();
        }

if (politica instanceof PoliticaPrioridadeDocente pd) {
            List<Reserva> paraCancel =
                pd.getReservasParaCancelar(usuario, sala, inicio, fim, existentes);
            paraCancel.forEach(r -> {
                r.setStatus(StatusReserva.CANCELADA);
                eventos.notificarObservadores(r, "CANCELADA_POR_DOCENTE"); 
            });
        }

        String id = "RSV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reserva reserva = new Reserva(id, sala, usuario, inicio, fim);
        repositorio.salvarReserva(reserva);

eventos.notificarObservadores(reserva, "CRIADA");
        System.out.println("[OK] " + reserva);
        return Optional.of(reserva);
    }

public boolean modificarReserva(String reservaId,
                                    LocalDateTime novoInicio, LocalDateTime novoFim) {
        Optional<Reserva> opt = repositorio.buscarReservaPorId(reservaId);
        if (opt.isEmpty()) {
            System.out.println("[ERRO] Reserva não encontrada: " + reservaId);
            return false;
        }

        Reserva reserva = opt.get();
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            System.out.println("[ERRO] Não é possível modificar uma reserva cancelada.");
            return false;
        }

List<Reserva> outras = repositorio.listarTodas().stream()
            .filter(r -> !r.getId().equals(reservaId))
            .toList();

        if (!politica.podeReservar(reserva.getUsuario(), reserva.getSala(),
                                   novoInicio, novoFim, outras)) {
            System.out.println("[ERRO] Conflito ao modificar reserva. Política: "
                + politica.getNome());
            return false;
        }

        reserva.setInicio(novoInicio);
        reserva.setFim(novoFim);
        reserva.setStatus(StatusReserva.MODIFICADA);

eventos.notificarObservadores(reserva, "MODIFICADA");
        System.out.println("[OK] Reserva modificada: " + reserva);
        return true;
    }

public boolean cancelarReserva(String reservaId) {
        Optional<Reserva> opt = repositorio.buscarReservaPorId(reservaId);
        if (opt.isEmpty()) {
            System.out.println("[ERRO] Reserva não encontrada: " + reservaId);
            return false;
        }

        Reserva reserva = opt.get();
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            System.out.println("[AVISO] Reserva já está cancelada.");
            return false;
        }

        reserva.setStatus(StatusReserva.CANCELADA);

eventos.notificarObservadores(reserva, "CANCELADA");
        System.out.println("[OK] Reserva cancelada: " + reserva);
        return true;
    }
}