# Projeto: Sistema de Reserva de Salas de Estudo

Projeto em desenvolvimento para a disciplina de Projeto Orientado a Objetos. 

O objetivo deste sistema é gerenciar as reservas de salas de estudo do campus universitário, aplicando conceitos de Padrões de Projeto em Java.

## Autores
* **Caroline Maximo** - 163.650
* **João Victor dos Santos Silva** - 163.836
---

## Requisitos Funcionais Atendidos

O sistema atende a todo o ciclo de vida de uma reserva, contemplando as seguintes funcionalidades:
* RF-01: Listagem de salas disponíveis, filtrando as que já possuem reservas confirmadas no horário solicitado.
* RF-02: Criação, modificação e cancelamento de reservas.
* RF-03: Lógica de detecção e prevenção de colisão de horários na mesma sala.
* RF-04: Disparo de notificações imediatas para os usuários quando o status da reserva muda.
* RF-05: Emissão de um relatório diário com a ocupação das salas.

## Padrões de Projeto Utilizados

Para deixar o código organizado, escalável e com baixo acoplamento, aplicamos os seguintes padrões:

* Factory Method: Usado para instanciar os diferentes tipos de salas (Estudo Individual, Trabalho em Grupo e Laboratório). Isso centraliza a criação e abstrai as classes concretas.
* Strategy: Implementa as regras de colisão de horários. Criamos a política padrão (First-Come, First-Served) e a política de Prioridade Docente (onde professores podem reservar horários já ocupados por alunos, cancelando a reserva do aluno automaticamente).
* Observer: Sistema de notificações. Quando uma reserva sofre qualquer alteração (criada, modificada ou cancelada), os observadores (NotificadorUsuario e ServicoRelatorio) são avisados automaticamente para emitir alertas e registrar logs.
* Singleton: O RepositorioReservas foi criado com este padrão para garantir uma única base de dados em memória para toda a aplicação. Utilizamos ReadWriteLock para garantir segurança (thread-safety) em acessos simultâneos.
* Decorator: Permite "decorar" uma reserva com serviços extras dinâmicos, como adicionar taxas de Limpeza ou Equipamento Multimídia na hora da criação.
* **Proxy (extensão):** Um `ServicoDeReservaProxy` envolve o serviço real e aplica regras de controle de acesso antes de qualquer operação. Estudantes são impedidos de reservar laboratórios e de fazer reservas com duração superior a 4 horas. Todas as tentativas são registradas em log. O restante do sistema programa para a interface `IServicoDeReserva`, sem saber se está interagindo com o proxy ou com o serviço real.

## Funcionalidade Adicional — Extensão

**Controle de acesso por perfil de usuário via Padrão Proxy**

A extensão implementa um Proxy de Proteção sobre o `ServicoDeReserva`, aplicando regras de negócio específicas por perfil antes de qualquer reserva ser criada:

| Regra | Estudante | Professor |
|---|---|---|
| Reservar sala de laboratório | Negado | Permitido |
| Reservar por mais de 4 horas | Negado | Permitido |

Consulte `docs/funcionalidade_adicional.md` para a descrição completa e justificativa do padrão.

## Estrutura do Projeto

A organização dos pacotes foi feita da seguinte forma:

```text
src/br/edu/reserva/
├── decorator/   # Lógica de serviços adicionais
├── factory/     # Criação das salas
├── model/       # Entidades de domínio (Sala, Reserva, Usuários)
├── observer/    # Sistema de eventos e relatórios
├── proxy/       # Controle de acesso por perfil (extensão)
├── service/     # Interface IServicoDeReserva e ServicoDeReserva
├── singleton/   # Repositório em memória
├── strategy/    # Políticas de colisão e prioridade
└── Main.java    # Interface de linha de comando
```

## Como Compilar e Executar

**Pré-requisito:** JDK 11 ou superior instalado.

```bash
# Na raiz do projeto, compile todos os arquivos
javac -d bin -sourcepath src src/br/edu/reserva/Main.java

# Execute o sistema
java -cp bin br.edu.reserva.Main
```