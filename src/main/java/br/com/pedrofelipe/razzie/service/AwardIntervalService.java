package br.com.pedrofelipe.razzie.service;

import br.com.pedrofelipe.razzie.model.Movie;
import br.com.pedrofelipe.razzie.repository.MovieRepository;
import br.com.pedrofelipe.razzie.dto.AwardInterval;
import br.com.pedrofelipe.razzie.dto.AwardIntervalResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Aqui mora a regra de negócio do desafio: entre todos os produtores que já
 * venceram, descobrir quem teve o menor e quem teve o maior intervalo entre dois
 * prêmios consecutivos.
 *
 * <p>O fluxo é em três passos: agrupo os anos de vitória por produtor, calculo os
 * intervalos entre prêmios seguidos, e no fim filtro quem ficou com o menor e o
 * maior. Deixei tudo em memória de propósito — são poucos vencedores, não vale a
 * pena complicar com query.
 */
@Service
public class AwardIntervalService {

    private final MovieRepository movieRepository;
    private final ProducerParser producerParser;

    public AwardIntervalService(MovieRepository movieRepository, ProducerParser producerParser) {
        this.movieRepository = movieRepository;
        this.producerParser = producerParser;
    }

    public AwardIntervalResult getAwardIntervals() {
        // Passo 1: produtor -> anos em que venceu (já ordenados e sem repetição).
        Map<String, TreeSet<Integer>> winYearsByProducer = groupWinYearsByProducer();

        // Passo 2: pra cada produtor, gero os intervalos entre prêmios consecutivos
        // e jogo todos num saco só.
        List<AwardInterval> allIntervals = new ArrayList<>();
        for (Map.Entry<String, TreeSet<Integer>> entry : winYearsByProducer.entrySet()) {
            allIntervals.addAll(intervalsFor(entry.getKey(), entry.getValue()));
        }

        // Caso ninguém tenha vencido duas vezes, não há intervalo nenhum. Devolvo
        // listas vazias em vez de deixar o getAsInt() lá embaixo estourar.
        if (allIntervals.isEmpty()) {
            return new AwardIntervalResult(List.of(), List.of());
        }

        // Passo 3: acho o menor e o maior valor de intervalo...
        int minInterval = allIntervals.stream().mapToInt(AwardInterval::interval).min().getAsInt();
        int maxInterval = allIntervals.stream().mapToInt(AwardInterval::interval).max().getAsInt();

        // ...e devolvo TODOS que empataram nesses valores (por isso são listas).
        return new AwardIntervalResult(
                intervalsWithValue(allIntervals, minInterval),
                intervalsWithValue(allIntervals, maxInterval)
        );
    }

    /**
     * Monta o mapa produtor -> anos de vitória.
     *
     * <p>Uso {@link TreeSet} de propósito: ele já me dá os anos ordenados (preciso
     * disso pra olhar os pares consecutivos depois) e ainda elimina repetição — se
     * por acaso um produtor vencesse duas vezes no mesmo ano, eu não quero um
     * intervalo de zero ano aparecendo do nada. E o {@link TreeMap} deixa a saída em
     * ordem alfabética, o que torna o resultado previsível (bom pros testes não
     * ficarem instáveis).
     */
    private Map<String, TreeSet<Integer>> groupWinYearsByProducer() {
        Map<String, TreeSet<Integer>> winYearsByProducer = new TreeMap<>();
        // Só puxo os vencedores do banco — o resto dos filmes não interessa aqui.
        for (Movie movie : movieRepository.findByWinnerTrue()) {
            for (String producer : producerParser.parse(movie.getProducers())) {
                winYearsByProducer
                        .computeIfAbsent(producer, key -> new TreeSet<>())
                        .add(movie.getYear());
            }
        }
        return winYearsByProducer;
    }

    /**
     * Dado um produtor e seus anos de vitória já ordenados, gera um intervalo pra
     * cada par de prêmios seguidos. Repara que é par a par ({@code ano[i] -
     * ano[i-1]}), não o maior menos o menor — um produtor pode ter vários intervalos.
     */
    private List<AwardInterval> intervalsFor(String producer, TreeSet<Integer> sortedYears) {
        // Com menos de duas vitórias não dá pra falar em intervalo.
        if (sortedYears.size() < 2) {
            return List.of();
        }

        List<Integer> years = new ArrayList<>(sortedYears);
        List<AwardInterval> intervals = new ArrayList<>();
        for (int i = 1; i < years.size(); i++) {
            int previousWin = years.get(i - 1);
            int followingWin = years.get(i);
            intervals.add(new AwardInterval(producer, followingWin - previousWin, previousWin, followingWin));
        }
        return intervals;
    }

    // Filtra os intervalos que bateram exatamente no valor que eu quero (o menor ou
    // o maior). É isso que me garante pegar todos os empates.
    private List<AwardInterval> intervalsWithValue(List<AwardInterval> intervals, int value) {
        return intervals.stream()
                .filter(interval -> interval.interval() == value)
                .toList();
    }
}
