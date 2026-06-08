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
 * <p>O fluxo é em dois passos: agrupo os anos de vitória por produtor e, numa única
 * passada, jogo cada intervalo num {@link TreeMap} indexado pelo próprio valor do
 * intervalo. Como o TreeMap mantém as chaves ordenadas, o menor intervalo é o
 * {@code firstKey} e o maior é o {@code lastKey} — leio os dois direto, sem
 * varreduras extras. Deixei tudo em memória de propósito: são poucos vencedores.
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

        // Passo 2: numa passada só, agrupo os intervalos pelo VALOR de cada um.
        // O TreeMap mantém as chaves ordenadas, então depois pego menor/maior direto.
        TreeMap<Integer, List<AwardInterval>> intervalsByValue = new TreeMap<>();
        winYearsByProducer.forEach((producer, years) ->
                collectIntervals(producer, years, intervalsByValue));

        // Ninguém venceu duas vezes -> não há intervalo. Devolvo listas vazias.
        if (intervalsByValue.isEmpty()) {
            return new AwardIntervalResult(List.of(), List.of());
        }

        // Menor = primeira chave, maior = última. Acesso O(log n), sem nova varredura,
        // e cada balde já traz TODOS os produtores que empataram naquele valor.
        return new AwardIntervalResult(
                intervalsByValue.get(intervalsByValue.firstKey()),
                intervalsByValue.get(intervalsByValue.lastKey())
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
     * Para um produtor e seus anos já ordenados, gera um intervalo por par de prêmios
     * consecutivos ({@code ano[i] - ano[i-1]}) e já o coloca no balde do seu valor.
     * É par a par, não o maior menos o menor — um produtor pode ter vários intervalos.
     */
    private void collectIntervals(String producer, TreeSet<Integer> sortedYears,
                                  Map<Integer, List<AwardInterval>> intervalsByValue) {
        if (sortedYears.size() < 2) {
            return;
        }
        List<Integer> years = new ArrayList<>(sortedYears);
        for (int i = 1; i < years.size(); i++) {
            int previousWin = years.get(i - 1);
            int followingWin = years.get(i);
            int interval = followingWin - previousWin;
            intervalsByValue
                    .computeIfAbsent(interval, key -> new ArrayList<>())
                    .add(new AwardInterval(producer, interval, previousWin, followingWin));
        }
    }
}
