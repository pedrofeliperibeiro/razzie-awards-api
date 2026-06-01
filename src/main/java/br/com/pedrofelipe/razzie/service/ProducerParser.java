package br.com.pedrofelipe.razzie.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Pega o campo {@code producers} do CSV (que vem como uma string só) e devolve os
 * produtores separados, um a um.
 *
 * <p>O problema é que esse campo vem bagunçado: às vezes os nomes são separados por
 * vírgula, às vezes o último vem grudado com "and", e às vezes os dois aparecem
 * juntos (a famosa vírgula de Oxford). Eu quero que as três formas caiam no mesmo
 * resultado:
 * <pre>
 *   "A and B"        -> [A, B]
 *   "A, B and C"     -> [A, B, C]
 *   "A, B, and C"    -> [A, B, C]   // vírgula de Oxford
 * </pre>
 */
@Component
public class ProducerParser {

    // Esse regex é o coração da classe. São duas alternativas separadas pelo "|":
    //
    //   1) \s*,\s*(?:and\s+)?  -> uma vírgula (com espaços opcionais em volta),
    //      podendo vir seguida de "and". Esse "and" opcional é o pulo do gato: é ele
    //      que engole o ", and " inteiro de uma vez. Sem isso, eu separava só na
    //      vírgula e sobrava um "and Fulano" como se fosse um produtor — bug clássico.
    //
    //   2) \s+and\s+  -> um "and" sozinho, cercado de espaço, pro caso "A and B".
    //
    // Exijo espaço em volta do "and" de propósito: assim nomes que só CONTÊM "and"
    // no meio (tipo "Sandra Bullock") não são quebrados por engano.
    private static final Pattern SEPARATOR = Pattern.compile("\\s*,\\s*(?:and\\s+)?|\\s+and\\s+");

    public List<String> parse(String rawProducers) {
        // Defensivo: se vier nulo ou vazio, não tem produtor nenhum pra devolver.
        if (rawProducers == null || rawProducers.isBlank()) {
            return List.of();
        }
        return Arrays.stream(SEPARATOR.split(rawProducers))
                .map(String::trim)            // tira espaço sobrando nas pontas
                .filter(name -> !name.isEmpty())
                .distinct()                   // se o mesmo nome aparecer 2x, conta 1
                .toList();
    }
}
