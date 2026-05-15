package br.edu.reserva.proxy;

import br.edu.reserva.model.Estudante;
import br.edu.reserva.model.Reserva;
import br.edu.reserva.model.SalaLaboratorio;
import br.edu.reserva.model.Usuario;
import br.edu.reserva.service.IServicoDeReserva;
import br.edu.reserva.service.ServicoDeReserva;
import br.edu.reserva.singleton.RepositorioReservas;
import br.edu.reserva.strategy.PoliticaDeReserva;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class ServicoDeReservaProxy implements IServicoDeReserva {

    private static final int MAX_HORAS_ESTUDANTE = 4;

    private final ServicoDeReserva servicoReal;
    private final RepositorioReservas repositorio = RepositorioReservas.getInstancia();

    public ServicoDeReservaProxy(ServicoDeReserva servicoReal) {
        this.servicoReal = servicoReal;
    }

    @Override
    public Optional<Reserva> criarReserva(Usuario usuario, String salaId,
                                          LocalDateTime inicio, LocalDateTime fim) {
        System.out.println("[Proxy] Verificando acesso: " + usuario.getTipo()
                + " '" + usuario.getNome() + "' → sala " + salaId);

        if (usuario instanceof Estudante) {
            var salaOpt = repositorio.buscarSalaPorId(salaId);
            if (salaOpt.isPresent() && salaOpt.get() instanceof SalaLaboratorio) {
                System.out.println("[Proxy] ACESSO NEGADO: Estudantes nao podem reservar laboratorios.");
                return Optional.empty();
            }

            long horas = Duration.between(inicio, fim).toHours();
            if (horas > MAX_HORAS_ESTUDANTE) {
                System.out.println("[Proxy] ACESSO NEGADO: Estudantes nao podem reservar por mais de "
                        + MAX_HORAS_ESTUDANTE + " hora(s). Solicitado: " + horas + "h.");
                return Optional.empty();
            }
        }

        System.out.println("[Proxy] Acesso autorizado. Delegando ao servico real...");
        return servicoReal.criarReserva(usuario, salaId, inicio, fim);
    }

    @Override
    public boolean modificarReserva(String reservaId,
                                    LocalDateTime novoInicio, LocalDateTime novoFim) {
        System.out.println("[Proxy] Solicitacao de modificacao para reserva: " + reservaId);
        return servicoReal.modificarReserva(reservaId, novoInicio, novoFim);
    }

    @Override
    public boolean cancelarReserva(String reservaId) {
        System.out.println("[Proxy] Solicitacao de cancelamento para reserva: " + reservaId);
        return servicoReal.cancelarReserva(reservaId);
    }

    @Override
    public void setPolitica(PoliticaDeReserva politica) {
        servicoReal.setPolitica(politica);
    }

    @Override
    public PoliticaDeReserva getPolitica() {
        return servicoReal.getPolitica();
    }
}
