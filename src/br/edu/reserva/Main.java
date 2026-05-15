package br.edu.reserva;

import br.edu.reserva.decorator.ReservaBase;
import br.edu.reserva.decorator.ReservaComLimpeza;
import br.edu.reserva.decorator.ReservaComMultimidia;
import br.edu.reserva.decorator.ServicoAdicional;
import br.edu.reserva.factory.SalaEstudoIndividualFactory;
import br.edu.reserva.factory.SalaFactory;
import br.edu.reserva.factory.SalaLaboratorioFactory;
import br.edu.reserva.factory.SalaTrabalhoGrupoFactory;
import br.edu.reserva.model.Estudante;
import br.edu.reserva.model.Professor;
import br.edu.reserva.model.Reserva;
import br.edu.reserva.model.Sala;
import br.edu.reserva.model.Usuario;
import br.edu.reserva.observer.GerenciadorEventos;
import br.edu.reserva.observer.NotificadorUsuario;
import br.edu.reserva.observer.ServicoRelatorio;
import br.edu.reserva.proxy.ServicoDeReservaProxy;
import br.edu.reserva.service.IServicoDeReserva;
import br.edu.reserva.service.ServicoDeReserva;
import br.edu.reserva.singleton.RepositorioReservas;
import br.edu.reserva.strategy.PoliticaPrimeiroChegarPrimeiroPago;
import br.edu.reserva.strategy.PoliticaPrioridadeDocente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        RepositorioReservas repo = RepositorioReservas.getInstancia();

        SalaFactory indFactory = new SalaEstudoIndividualFactory();
        SalaFactory grupoFactory = new SalaTrabalhoGrupoFactory();
        SalaFactory labFactory = new SalaLaboratorioFactory("20 computadores, projetores 4K");

        repo.adicionarSala(indFactory.criar("S01", "Cabine A", 1));
        repo.adicionarSala(indFactory.criar("S02", "Cabine B", 1));
        repo.adicionarSala(grupoFactory.criar("S03", "Sala Verde", 6));
        repo.adicionarSala(grupoFactory.criar("S04", "Sala Azul", 10));
        repo.adicionarSala(labFactory.criar("S05", "Lab. Informatica", 30));

        Estudante e1 = new Estudante("U01", "Ana Silva", "ana@uni.edu", "154649");
        Estudante e2 = new Estudante("U02", "Bruno Costa", "bruno@uni.edu", "163457");
        Professor prof = new Professor("U03", "Prof. Maria Santos", "maria@uni.edu", "166575");
        Usuario[] usuarios = {e1, e2, prof};

        ServicoRelatorio relatorio = new ServicoRelatorio();
        GerenciadorEventos.getInstancia().adicionarObservador(new NotificadorUsuario());
        GerenciadorEventos.getInstancia().adicionarObservador(relatorio);

        IServicoDeReserva servico = new ServicoDeReservaProxy(
                new ServicoDeReserva(new PoliticaPrimeiroChegarPrimeiroPago()));

        Scanner sc = new Scanner(System.in);
        String opcao = "";

        while (!opcao.equals("0")) {
            System.out.println("\n--- RESERVA DE SALAS ---");
            System.out.println("Politica: " + servico.getPolitica().getNome());
            System.out.println("1 - Listar salas disponiveis");
            System.out.println("2 - Criar reserva");
            System.out.println("3 - Modificar reserva");
            System.out.println("4 - Cancelar reserva");
            System.out.println("5 - Ver todas as reservas");
            System.out.println("6 - Relatorio do dia");
            System.out.println("7 - Trocar politica");
            System.out.println("8 - Demo");
            System.out.println("0 - Sair");
            System.out.print("Opcao: ");
            opcao = sc.nextLine().trim();

            if (opcao.equals("1")) {
                System.out.print("Inicio (dd/MM/yyyy HH:mm): ");
                LocalDateTime ini = parseDateTime(sc.nextLine());
                System.out.print("Fim (dd/MM/yyyy HH:mm): ");
                LocalDateTime fim = parseDateTime(sc.nextLine());
                if (ini == null || fim == null) { System.out.println("Data invalida."); continue; }
                List<Sala> salas = repo.listarSalasDisponiveis(ini, fim);
                if (salas.isEmpty()) System.out.println("Nenhuma sala disponivel.");
                else salas.forEach(s -> System.out.println(s));

            } else if (opcao.equals("2")) {
                for (int i = 0; i < usuarios.length; i++)
                    System.out.println((i + 1) + " - " + usuarios[i]);
                System.out.print("Usuario: ");
                int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
                repo.listarSalas().forEach(s -> System.out.println(s));
                System.out.print("ID da sala: ");
                String salaId = sc.nextLine().trim();
                System.out.print("Inicio (dd/MM/yyyy HH:mm): ");
                LocalDateTime ini = parseDateTime(sc.nextLine());
                System.out.print("Fim (dd/MM/yyyy HH:mm): ");
                LocalDateTime fim = parseDateTime(sc.nextLine());
                if (ini == null || fim == null) { System.out.println("Data invalida."); continue; }
                var reserva = servico.criarReserva(usuarios[idx], salaId, ini, fim);
                if (reserva.isPresent()) {
                    System.out.print("Adicionar servicos extras? (s/n): ");
                    if (sc.nextLine().trim().equalsIgnoreCase("s")) {
                        ServicoAdicional sa = new ReservaBase(reserva.get().getId());
                        System.out.print("Multimidia? (s/n): ");
                        if (sc.nextLine().trim().equalsIgnoreCase("s")) sa = new ReservaComMultimidia(sa);
                        System.out.print("Limpeza? (s/n): ");
                        if (sc.nextLine().trim().equalsIgnoreCase("s")) sa = new ReservaComLimpeza(sa);
                        System.out.println(sa.getDescricao() + " | Custo: R$ " + sa.getCusto());
                    }
                }

            } else if (opcao.equals("3")) {
                System.out.print("ID da reserva: ");
                String id = sc.nextLine().trim();
                System.out.print("Novo inicio (dd/MM/yyyy HH:mm): ");
                LocalDateTime ini = parseDateTime(sc.nextLine());
                System.out.print("Novo fim (dd/MM/yyyy HH:mm): ");
                LocalDateTime fim = parseDateTime(sc.nextLine());
                if (ini == null || fim == null) { System.out.println("Data invalida."); continue; }
                servico.modificarReserva(id, ini, fim);

            } else if (opcao.equals("4")) {
                System.out.print("ID da reserva: ");
                servico.cancelarReserva(sc.nextLine().trim());

            } else if (opcao.equals("5")) {
                List<Reserva> lista = repo.listarTodas();
                if (lista.isEmpty()) System.out.println("Nenhuma reserva.");
                else lista.forEach(r -> System.out.println(r));

            } else if (opcao.equals("6")) {
                relatorio.gerarRelatorioDiario(LocalDate.now());

            } else if (opcao.equals("7")) {
                System.out.println("1 - FCFS  |  2 - Prioridade Docente");
                System.out.print("Escolha: ");
                String p = sc.nextLine().trim();
                if (p.equals("1")) servico.setPolitica(new PoliticaPrimeiroChegarPrimeiroPago());
                else if (p.equals("2")) servico.setPolitica(new PoliticaPrioridadeDocente());

            } else if (opcao.equals("8")) {
                LocalDateTime h9  = LocalDate.now().atTime(9, 0);
                LocalDateTime h10 = LocalDate.now().atTime(10, 0);
                LocalDateTime h11 = LocalDate.now().atTime(11, 0);
                LocalDateTime h12 = LocalDate.now().atTime(12, 0);

                System.out.println("\n-- Demo --");
                System.out.println("Ana reserva S03 das 9h as 11h:");
                servico.criarReserva(e1, "S03", h9, h11);

                System.out.println("\nBruno tenta S03 das 10h as 12h (deve ser bloqueado):");
                servico.criarReserva(e2, "S03", h10, h12);

                System.out.println("\nTrocando para Prioridade Docente...");
                servico.setPolitica(new PoliticaPrioridadeDocente());

                System.out.println("\nProfessor reserva S03 das 10h as 12h (cancela Ana):");
                servico.criarReserva(prof, "S03", h10, h12);

                System.out.println("\nDecorator - multimidia + limpeza:");
                ServicoAdicional sa = new ReservaComLimpeza(new ReservaComMultimidia(new ReservaBase("RSV-DEMO")));
                System.out.println(sa.getDescricao() + " | Custo: R$ " + sa.getCusto());

                System.out.println("\nRelatorio do dia:");
                relatorio.gerarRelatorioDiario(LocalDate.now());

                System.out.println("Ultima reserva alterada (pull): " + relatorio.getUltimaReservaAlterada());

                System.out.println("\n-- Proxy (Controle de Acesso) --");
                System.out.println("Ana (estudante) tenta reservar Lab. Informatica (deve ser NEGADO):");
                servico.criarReserva(e1, "S05", h9, h10);

                System.out.println("\nAna tenta reservar Sala Verde por 6 horas (deve ser NEGADO):");
                servico.criarReserva(e1, "S03", h9, LocalDate.now().atTime(15, 0));

                System.out.println("\nProfessor reserva Lab. Informatica (deve ser PERMITIDO):");
                servico.criarReserva(prof, "S05", h9, h10);
            }
        }

        System.out.println("Encerrando...");
        sc.close();
    }

    private static LocalDateTime parseDateTime(String text) {
        try {
            String[] parts = text.trim().split(" ");
            String[] d = parts[0].split("/");
            String[] t = parts[1].split(":");
            return LocalDateTime.of(
                Integer.parseInt(d[2]), Integer.parseInt(d[1]), Integer.parseInt(d[0]),
                Integer.parseInt(t[0]), Integer.parseInt(t[1]));
        } catch (Exception e) {
            return null;
        }
    }
}